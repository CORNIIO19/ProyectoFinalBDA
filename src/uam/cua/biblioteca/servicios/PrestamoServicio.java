package uam.cua.biblioteca.servicios;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.modelo.Prestamo;
import uam.cua.biblioteca.persistencia.ConexionDb4o;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrestamoServicio {

    /**
     * C: CREATE - Registrar un préstamo vinculando las referencias vivas del disco.
     */
    public void registrarPrestamo(Prestamo prestamo) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // REGLA CRÍTICA: Cambiar el estado del libro vinculado a no disponible
            prestamo.getLibro().setDisponible(false);
            
            // Guardamos el préstamo (Db4o enlazará las referencias existentes)
            db.store(prestamo);
            // Actualizamos el libro en la base de datos para salvar su nuevo estado disponible = false
            db.store(prestamo.getLibro());
            
            System.out.println("[SERVICIO] Transaccion de prestamo completada con exito.");
        } finally {
            db.close();
        }
    }

    /**
     * R: READ - Listar todos los préstamos históricos
     */
    public List<Prestamo> listarTodo() {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Prestamo> lista = new ArrayList<>();
        try {
            // Nota de seguridad: Db4o por defecto carga hasta 5 niveles de profundidad de objetos en memoria.
            ObjectSet<Prestamo> resultado = db.query(Prestamo.class);
            for (Prestamo p : resultado) {
                lista.add(p);
            }
        } finally {
            db.close();
        }
        return lista;
    }

    /**
     * U: UPDATE - Registrar la devolución de un libro
     */
    public boolean registrarDevolucion(String isbnLibro) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Buscamos los préstamos activos
            ObjectSet<Prestamo> prestamos = db.query(Prestamo.class);
            Prestamo prestamoEncontrado = null;

            for (Prestamo p : prestamos) {
                if (p.getLibro().getIsbn().equals(isbnLibro) && p.getEstado().equals("Activo")) {
                    prestamoEncontrado = p;
                    break;
                }
            }

            if (prestamoEncontrado != null) {
                // 1. Modificar el préstamo
                prestamoEncontrado.setEstado("Devuelto");
                prestamoEncontrado.setFechaDevolucion(new Date());
                
                // 2. Liberar el libro ligado
                Libro libroAsociado = prestamoEncontrado.getLibro();
                libroAsociado.setDisponible(true);

                // Persistir ambos objetos actualizados
                db.store(prestamoEncontrado);
                db.store(libroAsociado);
                
                System.out.println("[SERVICIO] Devolucion procesada correctamente.");
                return true;
            }
            System.out.println("[SERVICIO] No hay un prestamo activo para el ISBN indicado.");
            return false;
        } finally {
            db.close();
        }
    }
}