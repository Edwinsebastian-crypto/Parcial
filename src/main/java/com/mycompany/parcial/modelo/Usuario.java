package com.mycompany.parcial.modelo;

/**
 * Representa un usuario registrado en la plataforma con su rol asignado.
 */
public class Usuario {
    /** Cédula o identificador único del usuario. */
    private String cedula;
    /** Nombre del usuario. */
    private String nombre;
    /** Apellido del usuario. */
    private String apellido;
    /** Correo electrónico del usuario. */
    private String correo;
    /** Rol dentro del sistema (Estudiante, Tutor, Asesor). */
    private String rol; // Estudiante, Tutor, Asesor

    public Usuario(String cedula, String nombre, String apellido, String correo, String rol) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
