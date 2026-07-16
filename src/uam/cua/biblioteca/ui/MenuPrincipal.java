package uam.cua.biblioteca.ui;

import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.modelo.Usuario;
import uam.cua.biblioteca.modelo.Bibliotecario;
import uam.cua.biblioteca.modelo.Prestamo;
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
        // 1. ELIMINACIÓN FÍSICA REAL: Forzamos la destrucción de archivos previos en el disco
        eliminarArchivoFisico("biblioteca.db4o");
        eliminarArchivoFisico("prueba.db4o");

        // 2. PRECARGA AISLADA: Inyectamos los datos desde cero absoluto
        precargarDatosEjemplo();

        int opcion = 0;
        do {
            System.out.println("\n========================================");
            System.out.println("   SISTEMA DE BIBLIOTECA - UAM CUA   ");
            System.out.println("========================================");
            System.out.println("1. Registrar un nuevo Libro (CRUD - Alta)");
            System.out.println("2. Listar todos los Libros (CRUD - Leer)");
            System.out.println("3. Registrar un Prestamo (Relaciones)");
            System.out.println("4. Registrar Devolucion de Libro");
            System.out.println("5. Buscar por Autor (Consulta QBE)");
            System.out.println("6. Búsqueda Combinada Autor/Categoria (Nativa)");
            System.out.println("7. Salir");
            System.out.print("Seleccione una opcion: ");
            
            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1: menuAltaLibro(); break;
                    case 2: menuListarLibros(); break;
                    case 3: menuRegistrarPrestamo(); break;
                    case 4: menuDevolucion(); break;
                    case 5: menuBuscarAutor(); break;
                    case 6: menuBuscarCombinado(); break;
                    case 7: System.out.println("Cerrando aplicacion... ¡Exito en tu entrega!"); break;
                    default: System.out.println("[Alerta] Opcion no valida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[Error] Ingrese un numero valido.");
            }
        } while (opcion != 7);
    }

    private static void eliminarArchivoFisico(String nombreArchivo) {
        try {
            File archivo = new File(nombreArchivo);
            if (archivo.exists()) {
                System.gc(); // Forzamos al recolector a liberar candados del archivo binario
                archivo.delete();
            }
        } catch (Exception e) {
            // Silencioso si no se puede borrar en el instante
        }
    }

    private static void precargarDatosEjemplo() {
        System.out.println("[Entorno] Creando base de datos limpia desde cero...");
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Insertamos los datos semilla utilizando exclusivamente esta instancia viva
            Usuario u = new Usuario("U001", "Omar Sosa", "omar@uam.mx", "551234", "Activo");
            Bibliotecario b = new Bibliotecario("B001", "Bibliotecario Cua", "staff@uam.mx", "554321", "EMP-01", "Matutino");
            db.store(u);
            db.store(b);
            
            Libro l1 = new Libro("978-Clean", "Clean Code", "Robert C. Martin", "Tecnologia");
            Libro l2 = new Libro("978-Rayuela", "Rayuela", "Julio Cortazar", "Novela");
            db.store(l1);
            db.store(l2);
            
            System.out.println("[Entorno] Inicializacion completa. Cero duplicados.");
        } finally {
            db.close(); // Al cerrar el contenedor aquí, limpiamos por completo la caché de la RAM
        }
    }

    private static void menuAltaLibro() {
        System.out.print("Ingrese ISBN: "); String isbn = scanner.nextLine();
        System.out.print("Ingrese Titulo: "); String titulo = scanner.nextLine();
        System.out.print("Ingrese Autor: "); String autor = scanner.nextLine();
        System.out.print("Ingrese Categoria: "); String cat = scanner.nextLine();
        
        libroServicio.registrarLibro(new Libro(isbn, titulo, autor, cat));
    }

    private static void menuListarLibros() {
        System.out.println("\n--- CATALOGO ACTUAL DE LIBROS ---");
        List<Libro> libros = libroServicio.listarTodo();
        if(libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            for (Libro l : libros) {
                System.out.println(l);
            }
        }
    }

    private static void menuRegistrarPrestamo() {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            List<Usuario> usuarios = db.query(Usuario.class);
            List<Bibliotecario> bibliotecarios = db.query(Bibliotecario.class);
            List<Libro> libros = db.query(Libro.class);

            if (usuarios.isEmpty() || bibliotecarios.isEmpty() || libros.isEmpty()) {
                System.out.println("[Error] Faltan entidades en la base de datos.");
                return;
            }

            System.out.println("Seleccione el ISBN del libro a prestar:");
            for (Libro l : libros) {
                if(l.isDisponible()) System.out.println(" -> " + l.getIsbn() + " : " + l.getTitulo());
            }
            String isbnEscogido = scanner.nextLine();

            Libro libroSeleccionado = null;
            for(Libro l : libros) {
                if(l.getIsbn().equals(isbnEscogido) && l.isDisponible()) { 
                    libroSeleccionado = l; 
                    break; 
                }
            }

            if (libroSeleccionado != null) {
                // Modificamos el estado del libro directamente sobre su referencia viva del disco
                libroSeleccionado.setDisponible(false);
                
                // Creamos el registro del prestamo asociando los grafos existentes
                Prestamo p = new Prestamo(usuarios.get(0), libroSeleccionado, bibliotecarios.get(0));
                
                // Guardamos todo dentro de la misma transaccion abierta para evitar duplicar en memoria
                db.store(p);
                db.store(libroSeleccionado);
                
                System.out.println("[SERVICIO] Prestamo registrado exitosamente en sesion unica.");
            } else {
                System.out.println("[Alerta] Libro no encontrado o no disponible.");
            }
        } catch(Exception e) {
            System.out.println("[Error] Fallo en el prestamo: " + e.getMessage());
        } finally {
            db.close();
        }
    }
    private static void menuDevolucion() {
        System.out.print("Ingrese el ISBN del libro a devolver: ");
        String isbn = scanner.nextLine();
        prestamoServicio.registrarDevolucion(isbn);
    }

    private static void menuBuscarAutor() {
        System.out.print("Ingrese el nombre del Autor: ");
        String autor = scanner.nextLine();
        List<Libro> res = libroConsulta.buscarPorAutor(autor);
        System.out.println("Resultados: " + res.size());
        for(Libro l : res) System.out.println(l);
    }

    private static void menuBuscarCombinado() {
        System.out.print("Ingrese el Autor: "); String autor = scanner.nextLine();
        System.out.print("Ingrese la Categoria: "); String cat = scanner.nextLine();
        List<Libro> res = libroConsulta.buscarPorAutorYCategoria(autor, cat);
        System.out.println("Resultados (2 Criterios): " + res.size());
        for(Libro l : res) System.out.println(l);
    }
}