package uam.cua.biblioteca.dao;

import uam.cua.biblioteca.modelo.Libro;
import java.util.List;

public interface LibroDAO {
    void guardar(Libro libro);
    List<Libro> obtenerTodos();
    Libro buscarPorIsbn(String isbn);
    List<Libro> buscarPorAutor(String autor);
    List<Libro> buscarPorAutorYCategoria(String autor, String categoria);
    void actualizar(Libro libro);
    void eliminar(Libro libro);
}