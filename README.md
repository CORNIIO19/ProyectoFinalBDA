# Documentación Sistema de Gestión de Biblioteca ODBMS


---

## 1. Arquitectura y Patrones de Diseño Implementados

Para superar las limitaciones del acoplamiento clásico y cumplir con los más altos estándares de ingeniería de software, el sistema fue diseñado bajo una **Arquitectura de Capas Rigurosa** complementada con el patrón de diseño **DAO (Data Access Object)**.

### Capas del Sistema
1. **Capa de Interfaz de Usuario (UI - Presentación):** `MenuPrincipal.java`. Se encarga exclusivamente de la interacción con el usuario mediante consola, la captura de datos y el formateo de los menús visuales.
2. **Capa de Negocio (Servicios):** `LibroServicio.java` y `PrestamoServicio.java`. Contiene las reglas operacionales e institucionales de la biblioteca (como validaciones de estado y restricciones de seguridad). No sabe cómo ni dónde se guardan los datos.
3. **Capa de Acceso a Datos (DAO):** Interfaces e Implementaciones (`LibroDAO`, `LibroDb4oDAO`, `PrestamoDAO`, `PrestamoDb4oDAO`). Centraliza las instrucciones específicas del motor ODBMS. Si en el futuro se cambia el motor de base de datos, solo se modifica esta capa.
4. **Capa de Componentes de Persistencia:** `ConexionDb4o.java`. Administra la apertura, el cierre y la compartición segura del archivo de base de datos orientado a objetos.
5. **Capa de Modelo (Entidades de Dominio):** Clases puras de Java que definen la estructura y comportamiento del negocio (`Libro`, `Persona`, `Usuario`, `Bibliotecario`, `Prestamo`).

### Patrón de Diseño DAO (Data Access Object)
Aísla por completo el código de bajo nivel del motor Db4o mediante contratos definidos en interfaces Java. Resuelve el problema del ciclo de vida de los objetos e impide colisiones de sesiones o bloqueos físicos recurrentes sobre el archivo binario del disco.

---

## 2. Tecnologías y Motor Orientado a Objetos (ODBMS)

* **Lenguaje:** Java 8 (o superior).
* **Motor OODB:** Db4o (Database for Objects) Versión 8.0.
* **Mecanismo de Persistencia:** Almacena de forma nativa e íntegra **grafos de objetos vivos** de la memoria RAM directo al disco duro en un archivo binario `.db4o`. No utiliza mapeadores relacionales intermedios (ORM) ni tablas.
* **Identidad de Objetos (Reference System):** Db4o rastrea los objetos mediante su identidad única en memoria (referencia RAM), garantizando que no existan duplicados conceptuales al requerir búsquedas explícitas previas a cualquier actualización física.

---

## 3. Modelo de Clases, Herencia y Relaciones

El diseño de dominio consta de **5 clases principales** interconectadas a través de relaciones nativas de POJO (Plain Old Java Objects):

* **Superclase Abstracta `Persona`:** Abstrae los atributos humanos comunes (`id`, `nombre`, `email`, `telefono`). Es abstracta porque no pueden existir humanos genéricos sin rol dentro del sistema.
* **Subclase `Usuario` (Hereda de `Persona`):** Añade atributos propios del solicitante como `fechaRegistro` y `estado` (ej. `"Activo"`, `"Sancionado"`).
* **Subclase `Bibliotecario` (Hereda de `Persona`):** Representa al personal administrador; añade `numeroEmpleado` y `turno`.
* **Clase `Libro`:** Entidad independiente con `isbn`, `titulo`, `autor`, `categoria` y una bandera booleana `disponible`.
* **Clase Compleja `Prestamo` (Grafo de Objetos):** Contiene **referencias directas a los tres objetos vivos** involucrados en la transacción enlazados en memoria (un `Usuario`, un `Libro` y un `Bibliotecario`), suprimiendo por completo el uso de llaves foráneas.

---

## 4. Descripción y Funcionamiento de los Flujos CRUD

### A. Alta de Libro (Create)
* **Funcionamiento:** Instancia un objeto de tipo `Libro` a partir de los datos proporcionados por la consola y lo guarda directamente mediante el método nativo `db.store()`. El libro nace por defecto con la bandera `disponible = true`.
* **Requerimientos:** Código ISBN único (no vacío), Título, Autor y Categoría.

### B. Ver Catálogo Completo (Read - General)
* **Funcionamiento:** Invoca a la capa DAO que ejecuta una consulta masiva mediante la técnica **QBE (Query By Example)** enviando una plantilla vacía, recuperando todas las instancias de tipo `Libro` en una colección indexada.
* **Requerimientos:** No requiere parámetros de entrada.

### C. Búsqueda Avanzada por Autor (Read - Filtrado QBE)
* **Funcionamiento:** Implementa **Query By Example**. El DAO crea un "objeto prototipo" de la clase `Libro` y le inyecta únicamente la propiedad del Autor deseado. El motor devuelve de forma nativa todos los objetos que coincidan exactamente.
* **Requerimientos:** Cadena de texto con el nombre del autor.

### D. Búsqueda por 2 Criterios Combinados (Read - Consulta Nativa)
* **Funcionamiento:** Utiliza **Consultas Nativas (Native Queries)** mediante la implementación de **Predicados anónimos en Java** (`Predicate<Libro>`). El motor evalúa dinámicamente un operador lógico AND (`&&`) entre el autor y la categoría compilado en código puro, sin usar strings SQL.
* **Requerimientos:** Autor y Categoría.

### E. Solicitar Préstamo (Update Transaccional & Relaciones)
* **Funcionamiento:** La capa de Servicios valida la regla de negocio: si el usuario está `"Sancionado"`, el préstamo es denegado. Si es aprobado, se recupera el objeto original del `Libro` del disco, se cambia a `disponible = false`, se instancia el `Prestamo` enlazando el grafo completo y se persisten ambos cambios en la misma sesión de base de datos para evitar duplicidad.
* **Requerimientos:** Índice del usuario listado y el ISBN exacto del libro disponible.

### F. Devolución de Ejemplar (Update & Transacción)
* **Funcionamiento:** El DAO busca un `Prestamo` con el `estado` en `"Activo"` que coincida con el ISBN ingresado. Al localizarlo, cambia el estado a `"Devuelto"`, estampa la fecha actual y accede directamente al objeto interno del `Libro` para restaurar su bandera a `disponible = true`. Ambos objetos se guardan sincrónicamente.
* **Requerimientos:** ISBN del libro actualmente prestado.

### G. Reporte Estadístico Global (Consulta Agregada Polimórfica)
* **Funcionamiento:** Realiza una lectura masiva polimórfica. Al consultar la clase base `Persona.class`, Db4o devuelve automáticamente todos los objetos hijos (`Usuario` y `Bibliotecario`) gracias a su herencia nativa. Se realizan métricas agregadas en memoria, como el cálculo de la tasa porcentual de ocupación de la biblioteca en tiempo real.
* **Requerimientos:** No requiere parámetros.

---

## 5. Instrucciones de Ejecución

Para asegurar la portabilidad y el arranque exitoso del sistema en entornos Linux (Debian/Ubuntu), Windows o macOS, sigue este procedimiento:

### Requisitos Previos
* **Java Development Kit (JDK):** Versión 8 o superior (Recomendado JDK 11+). Comprobar en terminal con `java -version`.
* **Entorno de Desarrollo:** Visual Studio Code con la extensión oficial **Extension Pack for Java** de Microsoft instalada.

### Estructura Correcta del Proyecto en Disco
Asegúrate de mantener intacta esta jerarquía antes de compilar:

```text
MiProyectoBiblioteca/
├── lib/
│   └── db4o-8.0.276.16149-all-java5.jar   <-- Archivo JAR obligatorio
└── src/
    └── uam/
        └── cua/
            └── biblioteca/
                ├── modelo/         (Entidades)
                ├── persistencia/   (Conexión base)
                ├── dao/            (Interfaces e Impl del DAO)
                ├── servicios/      (Lógica de negocio)
                └── ui/             (MenuPrincipal.java)
Pasos para Ejecutar en VS Code
Abre la carpeta raíz del proyecto (MiProyectoBiblioteca) desde la opción File > Open Folder.

Al cargar el proyecto, la extensión de Java detectará automáticamente la carpeta src/.

En la barra lateral izquierda, busca la sección inferior llamada Java Projects.

Despliega el menú, localiza Referenced Libraries, haz clic en el botón + y selecciona el archivo .jar de Db4o ubicado en la carpeta lib/.

Abre el archivo fuente src/uam/cua/biblioteca/ui/MenuPrincipal.java.

Haz clic en el botón superior Run (o presiona F5).

Nota de Evaluación: El código incluye un disparador de limpieza al arranque que purga archivos binarios residuales (biblioteca.db4o). Cada vez que se ejecuta, el software levanta un entorno de base de datos limpio, garantizando pruebas reproducibles y sin colisiones de datos.