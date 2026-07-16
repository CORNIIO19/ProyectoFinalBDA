package uam.cua.biblioteca.servicios;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.persistencia.ConexionDb4o;
import java.util.ArrayList;
import java.util.List;

public class LibroServicio {

    /**
     * C: CREATE - Registra un nuevo libro en el contenedor.
     */
    public void registrarLibro(Libro libro) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            db.store(libro);
            System.out.println("[SERVICIO] Libro guardado de forma persistente: " + libro.getTitulo());
        } finally {
            db.close();
        }
    }

    /**
     * R: READ - Recupera absolutamente todos los libros del archivo.
     */
    public List<Libro> listarTodo() {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Libro> listaCompleta = new ArrayList<>();
        try {
            ObjectSet<Libro> resultado = db.query(Libro.class);
            // Copiamos a una lista local para poder cerrar el contenedor sin perder los datos
            for (Libro l : resultado) {
                listaCompleta.add(l);
            }
        } finally {
            db.close();
        }
        return listaCompleta;
    }

    /**
     * U: UPDATE - Modifica un libro existente basándose en su ISBN único.
     * REGLA DE ORO DE DB4O: Se debe modificar la referencia exacta que devolvió la BD.
     */
    public boolean actualizarDatos(String isbn, String nuevoTitulo, String nuevoAutor, String nuevaCategoria) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Buscamos el libro original por ejemplo usando un prototipo con el ISBN
            Libro prototipo = new Libro();
            prototipo.setIsbn(isbn);
            ObjectSet<Libro> resultado = db.queryByExample(prototipo);

            if (!resultado.isEmpty()) {
                Libro libroExistente = resultado.next(); // Tomamos la referencia viva del disco
                libroExistente.setTitulo(nuevoTitulo);
                libroExistente.setAutor(nuevoAutor);
                libroExistente.setCategoria(nuevaCategoria);
                
                db.store(libroExistente); // Guarda los cambios sobre la misma referencia
                System.out.println("[SERVICIO] Libro actualizado exitosamente.");
                return true;
            }
            System.out.println("[SERVICIO] No se encontro ningun libro con el ISBN proporcionado.");
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * D: DELETE - Eliminacion fisica directa del objeto de la base de datos.
     */
    public boolean eliminarFisico(String isbn) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            Libro prototipo = new Libro();
            prototipo.setIsbn(isbn);
            ObjectSet<Libro> resultado = db.queryByExample(prototipo);

            if (!resultado.isEmpty()) {
                Libro libroAEliminar = resultado.next();
                db.delete(libroAEliminar); // Borra el registro binario
                System.out.println("[SERVICIO] Objeto removido de la base de datos.");
                return true;
            }
            return false;
        } finally {
            db.close();
        }
    }
}