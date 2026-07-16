package uam.cua.biblioteca.consultas;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import uam.cua.biblioteca.modelo.Libro;
import uam.cua.biblioteca.persistencia.ConexionDb4o;
import java.util.ArrayList;
import java.util.List;

public class LibroConsulta {

    /**
     * CONSULTA 1: Buscar libros por Autor usando Query By Example (QBE)
     */
    public List<Libro> buscarPorAutor(String autor) {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Libro> resultados = new ArrayList<>();
        try {
            Libro prototipo = new Libro();
            prototipo.setAutor(autor); // El objeto "ejemplo" solo lleva el filtro deseado
            
            ObjectSet<Libro> conjunto = db.queryByExample(prototipo);
            for (Libro l : conjunto) {
                resultados.add(l);
            }
        } finally {
            db.close();
        }
        return resultados;
    }

    /**
     * CONSULTA 2: Buscar libros por Categoría usando Query By Example (QBE)
     */
    public List<Libro> buscarPorCategoria(String categoria) {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Libro> resultados = new ArrayList<>();
        try {
            Libro prototipo = new Libro();
            prototipo.setCategoria(categoria);
            
            ObjectSet<Libro> conjunto = db.queryByExample(prototipo);
            for (Libro l : conjunto) {
                resultados.add(l);
            }
        } finally {
            db.close();
        }
        return resultados;
    }

    /**
     * CONSULTA 3: Buscar libros por Autor Y Categoría combinados
     * TECNICA: Consulta Nativa (Native Query) mediante Predicados anónimos.
     * Criterio obligatorio de la rúbrica ("2+ criterios combinados").
     */
    public List<Libro> buscarPorAutorYCategoria(final String autor, final String categoria) {
        ObjectContainer db = ConexionDb4o.abrir();
        try {
            // Evaluamos cada objeto de tipo Libro almacenado directamente con código Java puro
            List<Libro> resultado = db.query(new Predicate<Libro>() {
                @Override
                public boolean match(Libro libro) {
                    return libro.getAutor().equalsIgnoreCase(autor) 
                        && libro.getCategoria().equalsIgnoreCase(categoria);
                }
            });
            
            // Retornamos una copia local para poder cerrar el contenedor de forma segura
            return new ArrayList<>(resultado);
        } finally {
            db.close();
        }
    }

    /**
     * CONSULTA EXTRA: Mostrar únicamente libros que estén disponibles para préstamo
     */
    public List<Libro> buscarDisponibles() {
        ObjectContainer db = ConexionDb4o.abrir();
        List<Libro> resultados = new ArrayList<>();
        try {
            Libro prototipo = new Libro();
            prototipo.setDisponible(true);
            
            ObjectSet<Libro> conjunto = db.queryByExample(prototipo);
            for (Libro l : conjunto) {
                resultados.add(l);
            }
        } finally {
            db.close();
        }
        return resultados;
    }
}