DOCUMENTACIÓN SISTEMA DE GESTIÓN DE BIBLIOTECA ODBMS
Institución: UAM Cuajimalpa

Fecha de Entrega: 17 de julio de 2026

Tecnología Base: Java + Db4o (Object Database Management System)

1. Arquitectura y Patrones de Diseño Implementados
Para superar las limitaciones del acoplamiento clásico y cumplir con los más altos estándares de ingeniería de software, el sistema fue diseñado bajo una Arquitectura de Capas Rigurosa complementada con el patrón de diseño DAO (Data Access Object).

Capas del Sistema:
Capa de Interfaz de Usuario (UI - Presentación): MenuPrincipal.java. Se encarga exclusivamente de la interacción con el usuario mediante consola, la captura de datos y el formateo de los menús visuales.

Capa de Negocio (Servicios): LibroServicio.java y PrestamoServicio.java. Contiene las reglas operacionales e institucionales de la biblioteca (como validaciones de estado y restricciones de seguridad). No sabe cómo ni dónde se guardan los datos.

Capa de Acceso a Datos (DAO): Interfaces e Implementaciones (LibroDAO, LibroDb4oDAO, PrestamoDAO, PrestamoDb4oDAO). Centraliza las instrucciones específicas del motor ODBMS. Si en el futuro se cambia a una base de datos relacional o NoSQL, solo se modifica esta capa.

Capa de Componentes de Persistencia: ConexionDb4o.java. Administra la apertura, el cierre y la compartición segura del archivo de base de datos orientado a objetos.

Capa de Modelo (Entidades de Dominio): Clases puras de Java que definen la estructura y comportamiento del negocio (Libro, Persona, Usuario, Bibliotecario, Prestamo).

Patrón de Diseño DAO (Data Access Object)
Aísla por completo el código de bajo nivel del motor Db4o mediante contratos definidos en interfaces Java. Resuelve el problema del ciclo de vida de los objetos e impide colisiones de sesiones o bloqueos físicos recurrentes sobre el archivo binario del disco.

2. Tecnologías y Motor Orientado a Objetos (ODBMS)
Lenguaje: Java 8 (o superior).

Motor OODB: Db4o (Database for Objects) Versión 8.0.

Mecanismo de Persistencia: A diferencia de las bases de datos relacionales (SQL) que mapean datos en tablas, filas y columnas mediante un mapeador intermedio (ORM), Db4o almacena de forma nativa e íntegra grafos de objetos vivos de la memoria RAM directo al disco duro en un archivo binario .db4o.

Identidad de Objetos (Reference System): Db4o rastrea los objetos mediante su identidad única en memoria (referencia RAM), lo que obligó a implementar búsquedas explícitas previas en el disco antes de actualizar o alterar estados, garantizando que no existan duplicados conceptuales.

3. Modelo de Clases, Herencia y Relaciones
El diseño de dominio consta de 5 clases principales interconectadas a través de relaciones nativas de POJO (Plain Old Java Objects):

Superclase Abstracta Persona: Abstrae los atributos humanos comunes (id, nombre, email, telefono). Es abstracta porque no pueden existir humanos genéricos sin rol dentro del sistema.

Subclase Usuario (Hereda de Persona): Añade atributos propios del solicitante como fechaRegistro y estado (ej. "Activo", "Sancionado").

Subclase Bibliotecario (Hereda de Persona): Representa al personal administrador; añade numeroEmpleado y turno.

Clase Libro: Entidad independiente con isbn, titulo, autor, categoria y una bandera booleana disponible.

Clase Compleja Prestamo (Grafo de Objetos): En lugar de almacenar IDs numéricos o llaves foráneas como en SQL, contiene referencias directas a los tres objetos vivos involucrados en la transacción enlazados en memoria: un Usuario, un Libro y un Bibliotecario.

4. Descripción y Funcionamiento de los Flujos CRUD
El sistema implementa operaciones CRUD completas y lógicas avanzadas de negocio de la siguiente manera:

A. Alta de Libro (Create)
Funcionamiento: Instancia un objeto de tipo Libro a partir de los datos proporcionados por la consola y lo guarda directamente mediante el método nativo db.store(). El libro nace por defecto con la bandera disponible = true.

Requerimientos: Requiere un código ISBN único (no vacío), Título, Autor y Categoría.

B. Ver Catálogo Completo (Read - General)
Funcionamiento: Invoca a la capa DAO que ejecuta una consulta masiva sobre la clase mediante la técnica QBE (Query By Example) enviando una plantilla vacía, recuperando todas las instancias de tipo Libro en una colección indexada.

Requerimientos: No requiere parámetros de entrada.

C. Búsqueda Avanzada por Autor (Read - Filtrado QBE)
Funcionamiento: Implementa la técnica Query By Example. El DAO crea un "objeto prototipo" de la clase Libro y le inyecta únicamente la propiedad del Autor deseado. El motor de Db4o escanea el disco y devuelve de forma nativa todos los objetos que coincidan exactamente con la propiedad del prototipo.

Requerimientos: La cadena del nombre del autor.

D. Búsqueda por 2 Criterios Combinados (Read - Consulta Nativa)
Funcionamiento: Utiliza Consultas Nativas (Native Queries) de Db4o mediante la implementación de Predicados anónimos en Java (Predicate<Libro>). El motor evalúa dinámicamente el método match() con código Java puro compilado, aplicando un operador lógico && (AND) entre el autor y la categoría sin depender de strings SQL de bases de datos tradicionales.

Requerimientos: Autor y Categoría.

E. Solicitar Préstamo (Update Transaccional & Relaciones)
Funcionamiento (Función Avanzada A): Recupera las referencias del usuario y el libro seleccionados. La capa de Servicios ejecuta la regla de negocio: si el usuario tiene un estado diferente a "Activo" (ej. "Sancionado"), el préstamo es denegado inmediatamente. Si es aprobado, se recupera el objeto original del Libro del disco, se cambia su propiedad a disponible = false, se instancia un objeto Prestamo enlazando el grafo completo y se persisten ambos cambios en la misma sesión de base de datos para evitar la duplicidad.

Requerimientos: Índice del usuario listado y el ISBN exacto del libro disponible.

F. Devolución de Ejemplar (Update & Transacción)
Funcionamiento: La capa DAO ejecuta un escaneo buscando un objeto Prestamo cuyo atributo estado sea "Activo" y coincida con el ISBN ingresado. Al localizarlo, cambia el estado del préstamo a "Devuelto", estampa la fecha actual del sistema y accede directamente al objeto interno del Libro del grafo para restaurar su bandera a disponible = true. Ambos objetos son guardados sincrónicamente actualizando el registro original.

Requerimientos: ISBN del libro actualmente prestado.

G. Reporte Estadístico Global (Consulta Agregada Polimórfica - Función Avanzada B)
Funcionamiento: Realiza una lectura masiva polimórfica de la base de datos de objetos. Al consultar la clase base Persona.class, Db4o devuelve automáticamente todos los objetos hijos (Usuario y Bibliotecario) gracias al polimorfismo nativo. La aplicación realiza métricas agregadas en memoria calculando la tasa porcentual de ocupación de la biblioteca en tiempo real.

Requerimientos: No requiere parámetros.

5. Instrucciones de Ejecución en cualquier Máquina
Sigue este procedimiento técnico para asegurar la portabilidad y el arranque exitoso del sistema en entornos Linux (Debian/Ubuntu), Windows o macOS:

Requisitos Previos:
Tener instalado Java Development Kit (JDK) versión 8 o superior (Recomendado JDK 11 o superior). Puedes comprobarlo corriendo:

Bash
java -version
Tener instalado Visual Studio Code con la extensión oficial Extension Pack for Java de Microsoft.

Estructura Correcta del Proyecto en Disco:
Asegúrate de mantener intacta la jerarquía del proyecto antes de compilar:

Plaintext
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
Pasos para Ejecutar en VS Code:
Abre la carpeta raíz del proyecto (MiProyectoBiblioteca) desde la opción File > Open Folder en tu VS Code.

Al cargar el proyecto, la extensión de Java detectará automáticamente la carpeta src/.

Vincular la librería: En la barra lateral izquierda, busca la sección inferior llamada "Java Projects" (Proyectos de Java), despliega el menú, localiza la subcarpeta "Referenced Libraries" (Librerías Referenciadas), haz clic en el botón + y selecciona el archivo .jar de Db4o guardado en la carpeta lib/.

Abre el archivo fuente src/uam/cua/biblioteca/ui/MenuPrincipal.java.

Haz clic en el botón superior Run (o presiona la tecla F5).

El sistema se compilará y desplegará automáticamente la interfaz interactiva en la terminal integrada de VS Code.

Nota de Portabilidad y Limpieza: El código incluye un disparador de autodestrucción al arranque que purga archivos binarios corruptos o duplicados previos (biblioteca.db4o). Cada vez que un evaluador presione Run, el software levantará un entorno relacional de objetos de prueba completamente puro, limpio y listo para evaluarse de principio a fin.
