package uam.cua.biblioteca.modelo;

import java.util.Date;

public class Prestamo {
    private Date fechaPrestamo;
    private Date fechaDevolucion;
    private String estado; // "Activo", "Devuelto"

    // RELACIONES DIRECTAS: Guardamos los objetos vivos completos en el grafo de datos
    private Usuario usuario;
    private Libro libro;
    private Bibliotecario bibliotecario;

    // Constructor vacío obligatorio para Db4o
    public Prestamo() {
    }

    // Constructor completo para inicializar un préstamo activo
    public Prestamo(Usuario usuario, Libro libro, Bibliotecario bibliotecario) {
        this.fechaPrestamo = new Date(); // Fecha actual del sistema en 2026
        this.fechaDevolucion = null;     // Aún no se devuelve
        this.estado = "Activo";
        this.usuario = usuario;
        this.libro = libro;
        this.bibliotecario = bibliotecario;
    }

    // Getters y Setters
    public Date getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(Date fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public Date getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Date fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public Bibliotecario getBibliotecario() { return bibliotecario; }
    public void setBibliotecario(Bibliotecario bibliotecario) { this.bibliotecario = bibliotecario; }

    @Override
    public String toString() {
        return "Prestamo [Estado=" + estado + 
               "\n  -> Libro: " + (libro != null ? libro.getTitulo() : "null") + 
               "\n  -> Solicitante: " + (usuario != null ? usuario.getNombre() : "null") + 
               "\n  -> Registro: " + (bibliotecario != null ? bibliotecario.getNombre() : "null") + 
               "\n  -> Desde: " + fechaPrestamo + ", Devuelto: " + fechaDevolucion + "]";
    }
}