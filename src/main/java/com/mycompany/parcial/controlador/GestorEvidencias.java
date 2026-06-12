package com.mycompany.parcial.controlador;

import com.mycompany.parcial.modelo.Evidencia;
import com.mycompany.parcial.modelo.Entrega;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor singleton encargado de almacenar y administrar las evidencias.
 *
 * <p>
 * Utiliza un patrón de instancia única para que todas las pantallas accedan a
 * la misma colección de evidencias en memoria.
 * </p>
 */
public class GestorEvidencias {
    private static GestorEvidencias instancia;
    private List<Evidencia> evidencias;
    private List<Entrega> entregas;
    private int contadorId = 1;
    private int contadorEntregaId = 1;

    /**
     * Constructor privado para garantizar un único gestor de evidencias.
     */
    private GestorEvidencias() {
        evidencias = new ArrayList<>();
        entregas = new ArrayList<>();
    }

    /**
     * Obtiene la instancia única del gestor de evidencias.
     *
     * @return instancia compartida de GestorEvidencias
     */
    public static GestorEvidencias getInstancia() {
        if (instancia == null) {
            instancia = new GestorEvidencias();
        }
        return instancia;
    }

    /**
     * Asigna un identificador único a la evidencia y la agrega al repositorio en
     * memoria.
     *
     * @param evidencia evidencia a agregar
     */
    public void agregarEvidencia(Evidencia evidencia) {
        evidencia.setIdEvidencia("EV-" + String.format("%04d", contadorId++));
        evidencias.add(evidencia);
    }

    /**
     * Devuelve la lista completa de evidencias gestionadas por el sistema.
     *
     * @return colección de evidencias almacenadas
     */
    public List<Evidencia> getEvidencias() {
        return evidencias;
    }

    public List<Entrega> getEntregas() {
        return entregas;
    }

    public void agregarEntrega(Entrega entrega) {
        entrega.setId(String.valueOf(contadorEntregaId++));
        entregas.add(entrega);
    }
}
