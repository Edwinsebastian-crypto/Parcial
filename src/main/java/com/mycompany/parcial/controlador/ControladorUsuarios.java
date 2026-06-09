package com.mycompany.parcial.controlador;

import com.mycompany.parcial.modelo.Usuario;
import java.util.ArrayList;
import java.util.List;

public class ControladorUsuarios {

    // Lista para guardar los usuarios "quemados"
    private List<Usuario> usuariosGuardados;

    public ControladorUsuarios() {
        this.usuariosGuardados = new ArrayList<>();
        cargarUsuariosQuemados();
    }

    private void cargarUsuariosQuemados() {
        // Estudiantes
        usuariosGuardados.add(new Usuario("1001", "Juan", "Perez", "juan.perez@estudiante.edu", "Estudiante"));

        // Tutores
        usuariosGuardados.add(new Usuario("2001", "Carlos", "Ramirez", "carlos.ramirez@tutor.edu", "Tutor"));

        // Asesores
        usuariosGuardados.add(new Usuario("3001", "Luis", "Fernandez", "luis.fernandez@asesor.edu", "Asesor"));
    }

    public List<Usuario> getUsuariosGuardados() {
        return usuariosGuardados;
    }

    // Método opcional para obtener usuarios por rol
    public List<Usuario> getUsuariosPorRol(String rol) {
        List<Usuario> filtrados = new ArrayList<>();
        for (Usuario u : usuariosGuardados) {
            if (u.getRol().equalsIgnoreCase(rol)) {
                filtrados.add(u);
            }
        }
        return filtrados;
    }
}
