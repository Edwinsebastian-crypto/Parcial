/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.parcial.Main;

import com.mycompany.parcial.controlador.ControladorUsuarios;
import com.mycompany.parcial.modelo.Usuario;
import java.util.List;

/**
 *
 * @author EdwinPruebas
 */
public class Main {

    public static void main(String[] args) {

        // Crear controlador
        ControladorUsuarios controlador = new ControladorUsuarios();

        // Obtener usuarios
        List<Usuario> usuarios = controlador.getUsuariosGuardados();

        // Imprimir usuarios
        for (Usuario u : usuarios) {
            System.out.println(u.getNombre() + " " + u.getApellido() + " - " + u.getRol());
        }
    }
}
