package com.mycompany.parcial.modelo;

/**
 * Representa una entrega creada por el tutor para que el estudiante cargue su
 * archivo asociado.
 */
public class Entrega {
    private String id;
    private String fechaLimite;
    private String descripcion;

    public Entrega(String id, String fechaLimite, String descripcion) {
        this.id = id;
        this.fechaLimite = fechaLimite;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(String fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
