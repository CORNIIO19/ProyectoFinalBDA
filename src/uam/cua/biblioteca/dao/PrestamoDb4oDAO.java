package uam.cua.biblioteca.dao;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.modelo.Prestamo;
import uam.cua.biblioteca.persistencia.ConexionDb4o;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDb4oDAO implements PrestamoDAO {

    @Override
    public void registrar(Prestamo prestamo) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // REGLA DE ORO: Buscar la referencia viva del libro en esta sesion antes de guardar
            Libro prototipoLibro = new Libro();
            prototipoLibro.setIsbn(prestamo.getLibro().getIsbn());
            ObjectSet<Libro> libros = db.queryByExample(prototipoLibro);
            
            if (!libros.isEmpty()) {
                Libro libroFisico = libros.next();
                libroFisico.setDisponible(false); // Modificamos el objeto del disco
                prestamo.setLibro(libroFisico);   // Enlazamos el objeto real al prestamo
                
                db.store(prestamo);    // Guarda el prestamo
                db.store(libroFisico); // Actualiza el libro existente sin clonar
            }
        } finally {
            db.close();
        }
    }

    @Override
    public List<Prestamo> obtenerTodos() {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Prestamo> lista = new ArrayList<>();
        try {
            ObjectSet<Prestamo> resultado = db.query(Prestamo.class);
            for (Prestamo p : resultado) {
                lista.add(p);
            }
        } finally {
            db.close();
        }
        return lista;
    }

    @Override
    public Prestamo buscarActivoPorIsbn(String isbn) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            ObjectSet<Prestamo> resultado = db.query(Prestamo.class);
            for (Prestamo p : resultado) {
                if (p.getLibro().getIsbn().equals(isbn) && p.getEstado().equals("Activo")) {
                    return p;
                }
            }
            return null;
        } finally {
            db.close();
        }
    }

    @Override
    public void actualizar(Prestamo prestamo) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Buscamos el prestamo almacenado por sus caracteristicas en la sesion actual
            ObjectSet<Prestamo> resultado = db.query(Prestamo.class);
            for (Prestamo p : resultado) {
                if (p.getLibro().getIsbn().equals(prestamo.getLibro().getIsbn()) && p.getEstado().equals("Activo")) {
                    p.setEstado(prestamo.getEstado());
                    p.setFechaDevolucion(prestamo.getFechaDevolucion());
                    
                    // Tambien buscamos el libro asociado en el disco para restaurar su estado
                    Libro libroAsociado = p.getLibro();
                    libroAsociado.setDisponible(true);
                    
                    db.store(p);
                    db.store(libroAsociado);
                    break;
                }
            }
        } finally {
            db.close();
        }
    }
}