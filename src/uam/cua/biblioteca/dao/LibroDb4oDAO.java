package uam.cua.biblioteca.dao;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.persistencia.ConexionDb4o;
import java.util.ArrayList;
import java.util.List;

public class LibroDb4oDAO implements LibroDAO {

    @Override
    public void guardar(Libro libro) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            db.store(libro);
        } finally {
            db.close();
        }
    }

    @Override
    public List<Libro> obtenerTodos() {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Libro> lista = new ArrayList<>();
        try {
            ObjectSet<Libro> resultado = db.query(Libro.class);
            for (Libro l : resultado) {
                lista.add(l);
            }
        } finally {
            db.close();
        }
        return lista;
    }

    @Override
    public Libro buscarPorIsbn(String isbn) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            Libro prototipo = new Libro();
            prototipo.setIsbn(isbn);
            ObjectSet<Libro> resultado = db.queryByExample(prototipo);
            return resultado.isEmpty() ? null : resultado.next();
        } finally {
            db.close();
        }
    }

    @Override
    public List<Libro> buscarPorAutor(String autor) {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Libro> lista = new ArrayList<>();
        try {
            Libro prototipo = new Libro();
            prototipo.setAutor(autor);
            ObjectSet<Libro> resultado = db.queryByExample(prototipo);
            for (Libro l : resultado) {
                lista.add(l);
            }
        } finally {
            db.close();
        }
        return lista;
    }

    @Override
    public List<Libro> buscarPorAutorYCategoria(final String autor, final String categoria) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            List<Libro> resultado = db.query(new Predicate<Libro>() {
                @Override
                public boolean match(Libro libro) {
                    return libro.getAutor().equalsIgnoreCase(autor) 
                        && libro.getCategoria().equalsIgnoreCase(categoria);
                }
            });
            return new ArrayList<>(resultado);
        } finally {
            db.close();
        }
    }

    @Override
    public void actualizar(Libro libro) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Db4o requiere la referencia exacta del contenedor activo para actualizar sin duplicar
            Libro prototipo = new Libro();
            prototipo.setIsbn(libro.getIsbn());
            ObjectSet<Libro> resultado = db.queryByExample(prototipo);
            
            if (!resultado.isEmpty()) {
                Libro persistido = resultado.next();
                persistido.setTitulo(libro.getTitulo());
                persistido.setAutor(libro.getAutor());
                persistido.setCategoria(libro.getCategoria());
                persistido.setDisponible(libro.isDisponible());
                db.store(persistido);
            }
        } finally {
            db.close();
        }
    }

    @Override
    public void eliminar(Libro libro) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            Libro prototipo = new Libro();
            prototipo.setIsbn(libro.getIsbn());
            ObjectSet<Libro> resultado = db.queryByExample(prototipo);
            if (!resultado.isEmpty()) {
                db.delete(resultado.next());
            }
        } finally {
            db.close();
        }
    }
}