package com.mycompany.parcial.controlador;

import com.mycompany.parcial.modelo.Evidencia;
import java.util.ArrayList;
import java.util.List;

public class GestorEvidencias {
    private static GestorEvidencias instancia;
    private List<Evidencia> evidencias;
    private int contadorId = 1;

    private GestorEvidencias() {
        evidencias = new ArrayList<>();
    }

    public static GestorEvidencias getInstancia() {
        if (instancia == null) {
            instancia = new GestorEvidencias();
        }
        return instancia;
    }

    public void agregarEvidencia(Evidencia evidencia) {
        evidencia.setIdEvidencia("EV-" + String.format("%04d", contadorId++));
        evidencias.add(evidencia);
    }

    public List<Evidencia> getEvidencias() {
        return evidencias;
    }
}
