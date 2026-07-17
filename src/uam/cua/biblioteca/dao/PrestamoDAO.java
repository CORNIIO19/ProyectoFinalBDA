package uam.cua.biblioteca.dao;

import uam.cua.biblioteca.modelo.Prestamo;
import java.util.List;

public interface PrestamoDAO {
    void registrar(Prestamo prestamo);
    List<Prestamo> obtenerTodos();
    Prestamo buscarActivoPorIsbn(String isbn);
    void actualizar(Prestamo prestamo);
}