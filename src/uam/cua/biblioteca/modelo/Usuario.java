package uam.cua.biblioteca.modelo;

import java.util.Date;

public class Usuario extends Persona {
    private Date fechaRegistro;
    private String estado; // "Activo", "Inactivo"

    // Constructor vacío obligatorio para Db4o
    public Usuario() {
        super();
    }

    public Usuario(String id, String nombre, String email, String telefono, String estado) {
        super(id, nombre, email, telefono); // Pasa los atributos comunes a la superclase
        this.fechaRegistro = new Date();
        this.estado = estado;
    }

    // Getters y Setters específicos
    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Usuario [ID=" + getId() + ", Nombre=" + getNombre() + ", Estado=" + estado + ", Registrado=" + fechaRegistro + "]";
    }
}