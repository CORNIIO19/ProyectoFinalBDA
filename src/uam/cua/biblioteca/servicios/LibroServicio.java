package uam.cua.biblioteca.servicios;

import uam.cua.biblioteca.dao.LibroDAO;
import uam.cua.biblioteca.dao.LibroDb4oDAO;
import uam.cua.biblioteca.modelo.Libro;
import java.util.List;

public class LibroServicio {
    private final LibroDAO libroDAO = new LibroDb4oDAO();

    public void registrarLibro(Libro libro) {
        if (libro.getIsbn() == null || libro.getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("El ISBN no puede estar vacio.");
        }
        libroDAO.guardar(libro);
    }

    public List<Libro> listarTodo() {
        return libroDAO.obtenerTodos();
    }

    public boolean actualizarDatos(String isbn, String titulo, String autor, String categoria) {
        Libro libro = libroDAO.buscarPorIsbn(isbn);
        if (libro != null) {
            libro.setTitulo(titulo);
            libro.setAutor(autor);
            libro.setCategoria(categoria);
            libroDAO.actualizar(libro);
            return true;
        }
        return false;
    }

    public boolean eliminarFisico(String isbn) {
        Libro libro = libroDAO.buscarPorIsbn(isbn);
        if (libro != null) {
            libroDAO.eliminar(libro);
            return true;
        }
        return false;
    }
}