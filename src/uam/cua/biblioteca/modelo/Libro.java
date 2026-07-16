package uam.cua.biblioteca.modelo;

public class Libro {
    private String isbn;
    private String titulo;
    private String autor;
    private String categoria;
    private boolean disponible;

    // Constructor vacío obligatorio para que Db4o reconstruya objetos
    public Libro() {
    }

    // Constructor completo
    public Libro(String isbn, String titulo, String autor, String categoria) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.disponible = true;
    }

    // Getters y Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() {
        return "Libro [ISBN=" + isbn + ", Titulo=" + titulo + ", Autor=" + autor + 
               ", Categoria=" + categoria + ", Disponible=" + disponible + "]";
    }
}