package uam.cua.biblioteca.servicios;

import uam.cua.biblioteca.dao.PrestamoDAO;
import uam.cua.biblioteca.dao.PrestamoDb4oDAO;
import uam.cua.biblioteca.modelo.Prestamo;
import java.util.Date;
import java.util.List;

public class PrestamoServicio {
    private final PrestamoDAO prestamoDAO = new PrestamoDb4oDAO();

    public boolean generarPrestamo(Prestamo prestamo) {
        // REGLA DE NEGOCIO AVANZADA (Función A): Validar estado del usuario solicitante
        if (!prestamo.getUsuario().getEstado().equalsIgnoreCase("Activo")) {
            System.out.println("[NEGOCIO] Denegado: El usuario '" + prestamo.getUsuario().getNombre() + "' esta SANCIONADO o INACTIVO.");
            return false;
        }

        if (!prestamo.getLibro().isDisponible()) {
            System.out.println("[NEGOCIO] Denegado: El libro ya se encuentra prestado.");
            return false;
        }

        // Si pasa las reglas, se cambia la disponibilidad y se persiste
        prestamo.getLibro().setDisponible(false);
        prestamoDAO.registrar(prestamo);
        System.out.println("[NEGOCIO] Autorizado: Transaccion de prestamo registrada en el sistema.");
        return true;
    }

    public boolean registrarDevolucion(String isbnLibro) {
        Prestamo prestamoActive = prestamoDAO.buscarActivoPorIsbn(isbnLibro);
        if (prestamoActive != null) {
            prestamoActive.setEstado("Devuelto");
            prestamoActive.setFechaDevolucion(new Date());
            prestamoActive.getLibro().setDisponible(true);
            
            prestamoDAO.actualizar(prestamoActive);
            System.out.println("[NEGOCIO] Procesado: Devolucion asentada y libro liberado.");
            return true;
        }
        System.out.println("[NEGOCIO] Error: No se encontro un prestamo activo para el ISBN: " + isbnLibro);
        return false;
    }

    public List<Prestamo> listarTodos() {
        return prestamoDAO.obtenerTodos();
    }
}