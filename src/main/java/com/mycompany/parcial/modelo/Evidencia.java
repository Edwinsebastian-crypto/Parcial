package com.mycompany.parcial.modelo;

public class Evidencia {
    private String idEvidencia;
    private String idEstudiante;
    private String nombreEstudiante;
    private String nombreEvidencia;
    private String fechaCarga;
    private String descripcion;
    private String pathArchivo;
    private String estado;
    private String calificacion;
    private String profesor;
    private String fechaCalificacion;
    private String observacion;

    public Evidencia() {
    }

    public Evidencia(String idEvidencia, String idEstudiante, String nombreEstudiante, String nombreEvidencia,
                     String fechaCarga, String descripcion, String pathArchivo, String estado, String calificacion,
                     String profesor, String fechaCalificacion, String observacion) {
        this.idEvidencia = idEvidencia;
        this.idEstudiante = idEstudiante;
        this.nombreEstudiante = nombreEstudiante;
        this.nombreEvidencia = nombreEvidencia;
        this.fechaCarga = fechaCarga;
        this.descripcion = descripcion;
        this.pathArchivo = pathArchivo;
        this.estado = estado;
        this.calificacion = calificacion;
        this.profesor = profesor;
        this.fechaCalificacion = fechaCalificacion;
        this.observacion = observacion;
    }

    public String getIdEvidencia() { return idEvidencia; }
    public void setIdEvidencia(String idEvidencia) { this.idEvidencia = idEvidencia; }

    public String getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(String idEstudiante) { this.idEstudiante = idEstudiante; }

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }

    public String getNombreEvidencia() { return nombreEvidencia; }
    public void setNombreEvidencia(String nombreEvidencia) { this.nombreEvidencia = nombreEvidencia; }

    public String getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(String fechaCarga) { this.fechaCarga = fechaCarga; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPathArchivo() { return pathArchivo; }
    public void setPathArchivo(String pathArchivo) { this.pathArchivo = pathArchivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCalificacion() { return calificacion; }
    public void setCalificacion(String calificacion) { this.calificacion = calificacion; }

    public String getProfesor() { return profesor; }
    public void setProfesor(String profesor) { this.profesor = profesor; }

    public String getFechaCalificacion() { return fechaCalificacion; }
    public void setFechaCalificacion(String fechaCalificacion) { this.fechaCalificacion = fechaCalificacion; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
