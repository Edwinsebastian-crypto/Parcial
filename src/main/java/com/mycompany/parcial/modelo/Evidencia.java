package com.mycompany.parcial.modelo;

/**
 * Representa una evidencia cargada por un estudiante, junto con información de
 * revisión y calificación.
 */
public class Evidencia {
    /** Identificador único generado automáticamente. */
    private String idEvidencia;
    /** Identificador del estudiante que cargó la evidencia. */
    private String idEstudiante;
    /** Nombre completo del estudiante que subió la evidencia. */
    private String nombreEstudiante;
    /** Nombre o título de la evidencia. */
    private String nombreEvidencia;
    /** Fecha en la que se cargó la evidencia. */
    private String fechaCarga;
    /** Descripción de la evidencia. */
    private String descripcion;
    /** Ruta del archivo asociado a la evidencia. */
    private String pathArchivo;
    /** Estado de revisión de la evidencia. */
    private String estado;
    /** Calificación asignada por el tutor o asesor. */
    private String calificacion;
    /** Profesor o tutor responsable de la revisión. */
    private String profesor;
    /** Fecha en la que se realizó la calificación. */
    private String fechaCalificacion;
    /** Observaciones del tutor o asesor. */
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
