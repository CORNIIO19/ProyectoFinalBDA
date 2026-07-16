package uam.cua.biblioteca.modelo;

public class Bibliotecario extends Persona {
    private String numeroEmpleado;
    private String turno; // "Matutino", "Vespertino"

    // Constructor vacío obligatorio para Db4o
    public Bibliotecario() {
        super();
    }

    public Bibliotecario(String id, String nombre, String email, String telefono, String numeroEmpleado, String turno) {
        super(id, nombre, email, telefono);
        this.numeroEmpleado = numeroEmpleado;
        this.turno = turno;
    }

    // Getters y Setters específicos
    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    @Override
    public String toString() {
        return "Bibliotecario [ID=" + getId() + ", Nombre=" + getNombre() + ", No. Empleado=" + numeroEmpleado + ", Turno=" + turno + "]";
    }
}