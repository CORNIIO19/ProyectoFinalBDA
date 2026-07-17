package uam.cua.biblioteca.ui;

import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.modelo.Usuario;
import uam.cua.biblioteca.modelo.Bibliotecario;
import uam.cua.biblioteca.modelo.Prestamo;
import uam.cua.biblioteca.modelo.Persona;
import uam.cua.biblioteca.servicios.LibroServicio;
import uam.cua.biblioteca.servicios.PrestamoServicio;
import uam.cua.biblioteca.consultas.LibroConsulta;
import uam.cua.biblioteca.persistencia.ConexionDb4o;

import com.db4o.ObjectContainer;
import java.io.File;
import java.util.List;
import java.util.Scanner;

public class MenuPrincipal {
    private static final LibroServicio libroServicio = new LibroServicio();
    private static final PrestamoServicio prestamoServicio = new PrestamoServicio();
    private static final LibroConsulta libroConsulta = new LibroConsulta();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Reiniciamos el entorno físico para asegurar pruebas limpias e independientes
        eliminarArchivoFisico("biblioteca.db4o");

        // Cargamos la infraestructura base con usuarios activos y sancionados para probar reglas
        precargarDatosInfraestructura();

        int opcion = 0;
        do {
            System.out.println("\n==================================================");
            System.out.println("   SISTEMA DE GESTION INSTITUCIONAL - UAM CUA   ");
            System.out.println("==================================================");
            System.out.println("1. Alta de Libro (CRUD - Create)");
            System.out.println("2. Ver Catalogo Completo (CRUD - Read)");
            System.out.println("3. Solicitar Prestamo (Reglas de Negocio DAO)");
            System.out.println("4. Devolucion de Ejemplar");
            System.out.println("5. Busqueda Avanzada por Autor (QBE)");
            System.out.println("6. Reporte Estadistico Global de Ocupacion (Nativo)");
            System.out.println("7. Salir del Sistema");
            System.out.print("Seleccione una opcion del menu: ");
            
            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1: menuAltaLibro(); break;
                    case 2: menuListarLibros(); break;
                    case 3: menuRegistrarPrestamo(); break;
                    case 4: menuDevolucion(); break;
                    case 5: menuBuscarAutor(); break;
                    case 6: menuMostrarEstadisticas(); break;
                    case 7: System.out.println("Cierre de sesion exitoso. ¡Excelente entrega!"); break;
                    default: System.out.println("[Alerta] Ingrese una opcion listada.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[Error] La entrada debe ser un digito entero.");
            }
        } while (opcion != 7);
    }

    private static void eliminarArchivoFisico(String nombreArchivo) {
        try {
            File archivo = new File(nombreArchivo);
            if (archivo.exists()) {
                System.gc(); 
                archivo.delete();
            }
        } catch (Exception e) {
            // Manejo silencioso en tiempo de ejecución
        }
    }

    private static void precargarDatosInfraestructura() {
        System.out.println("[Entorno] Configurando base de datos limpia...");
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Insertamos dos perfiles de usuario contrastantes para validar las nuevas directrices
            Usuario u1 = new Usuario("U001", "Omar Sosa (Activo)", "omar@uam.mx", "550011", "Activo");
            Usuario u2 = new Usuario("U002", "Alumno Sancionado", "bad@uam.mx", "550022", "Sancionado");
            Bibliotecario b = new Bibliotecario("B001", "Lic. Coordinador Cua", "staff@uam.mx", "5599", "EMP-01", "Matutino");
            
            db.store(u1);
            db.store(u2);
            db.store(b);

            db.store(new Libro("978-Clean", "Clean Code", "Robert C. Martin", "Tecnologia"));
            db.store(new Libro("978-Rayuela", "Rayuela", "Julio Cortazar", "Novela"));
            db.store(new Libro("978-Design", "Design Patterns", "Gang of Four", "Tecnologia"));
        } finally {
            db.close();
        }
        System.out.println("[Entorno] Infraestructura inicializada. Listo para operar.");
    }

    private static void menuAltaLibro() {
        System.out.print("ISBN: "); String isbn = scanner.nextLine();
        System.out.print("Título: "); String titulo = scanner.nextLine();
        System.out.print("Autor: "); String autor = scanner.nextLine();
        System.out.print("Categoría: "); String cat = scanner.nextLine();
        libroServicio.registrarLibro(new Libro(isbn, titulo, autor, cat));
        System.out.println("[UI] Registro procesado exitosamente.");
    }

    private static void menuListarLibros() {
        System.out.println("\n--- CATALOGO ACTUAL DE LIBROS ---");
        List<Libro> libros = libroServicio.listarTodo();
        for (Libro l : libros) System.out.println(l);
    }

private static void menuRegistrarPrestamo() {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            List<Usuario> usuarios = db.query(Usuario.class);
            List<Bibliotecario> bibliotecarios = db.query(Bibliotecario.class);
            List<Libro> libros = db.query(Libro.class);

            System.out.println("\n--- SELECCIONAR USUARIO SOLICITANTE ---");
            for(int i=0; i < usuarios.size(); i++) {
                System.out.println(i + ") " + usuarios.get(i).getNombre() + " [" + usuarios.get(i).getEstado() + "]");
            }
            System.out.print("Indice de usuario: ");
            int idxUser = Integer.parseInt(scanner.nextLine());

            System.out.println("\n--- SELECCIONAR LIBRO DISPONIBLE ---");
            for(Libro l : libros) {
                if(l.isDisponible()) System.out.println(" -> " + l.getIsbn() + " : " + l.getTitulo());
            }
            System.out.print("Escriba el ISBN exacto: ");
            String isbn = scanner.nextLine();

            Libro libroSel = null;
            for(Libro l : libros) {
                if(l.getIsbn().equals(isbn) && l.isDisponible()) { libroSel = l; break; }
            }

            if(libroSel != null && idxUser >= 0 && idxUser < usuarios.size()) {
                Bibliotecario staff = bibliotecarios.isEmpty() ? new Bibliotecario("B001", "Bibliotecario Cua", "staff@uam.mx", "554321", "EMP-01", "Matutino") : bibliotecarios.get(0);
                
                // Creamos un objeto temporal para transportar los datos a la capa de servicios
                Prestamo p = new Prestamo(usuarios.get(idxUser), libroSel, staff);
                
                db.close(); // Cerramos la lectura local inmediatamente
                
                // Dejamos que el servicio y el DAO manejen de forma limpia la persistencia sin clonar
                prestamoServicio.generarPrestamo(p);
            } else {
                System.out.println("[UI] Seleccion invalida o el libro no esta disponible.");
                db.close();
            }
        } catch(Exception e) {
            System.out.println("[UI] Error al procesar entrada: " + e.getMessage());
            db.close();
        }
    }

    private static void menuDevolucion() {
        System.out.print("ISBN del libro a retornar: ");
        String isbn = scanner.nextLine();
        prestamoServicio.registrarDevolucion(isbn);
    }

    private static void menuBuscarAutor() {
        System.out.print("Nombre del Autor: ");
        String autor = scanner.nextLine();
        List<Libro> res = libroConsulta.buscarPorAutor(autor);
        System.out.println("\nCoincidencias encontradas: " + res.size());
        for(Libro l : res) System.out.println(l);
    }

    // FUNCIÓN B: Reporte Estadístico Global (Consulta Agregada Polimórfica)
    private static void menuMostrarEstadisticas() {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            List<Libro> libros = db.query(Libro.class);
            List<Prestamo> prestamos = db.query(Prestamo.class);
            // Uso de Polimorfismo: recupera tanto Usuarios como Bibliotecarios en un único set
            List<Persona> totalPersonas = db.query(Persona.class); 
            List<Usuario> totalUsuarios = db.query(Usuario.class);

            int prestados = 0;
            for(Libro l : libros) {
                if(!l.isDisponible()) prestados++;
            }

            double porcentajeOcupacion = libros.isEmpty() ? 0.0 : ((double)prestados / libros.size()) * 100;

            System.out.println("\n==================================================");
            System.out.println("   METRICAS Y ESTADISTICAS GLOBALES DE LA BD   ");
            System.out.println("==================================================");
            System.out.println(" • Total de Libros en Inventario : " + libros.size());
            System.out.println(" • Libros en Prestamo Activo     : " + prestados);
            System.out.println(" • Ejemplares Disponibles        : " + (libros.size() - prestados));
            System.out.println(" • Tasa de Ocupacion Actual      : " + String.format("%.2f", porcentajeOcupacion) + "%");
            System.out.println(" ------------------------------------------------");
            System.out.println(" • Total de Usuarios Alumnos     : " + totalUsuarios.size());
            System.out.println(" • Personal Registrado (Staff)   : " + (totalPersonas.size() - totalUsuarios.size()));
            System.out.println(" • Historico Total de Prestamos  : " + prestamos.size());
            System.out.println("==================================================");
        } finally {
            db.close();
        }
    }
}