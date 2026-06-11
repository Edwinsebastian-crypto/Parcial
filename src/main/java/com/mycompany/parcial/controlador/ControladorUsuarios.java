package com.mycompany.parcial.controlador;

import com.mycompany.parcial.modelo.Usuario;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador simple para gestionar los usuarios predefinidos de la aplicación.
 *
 * <p>Este controlador mantiene una lista de usuarios "quemados" y ofrece métodos
 * para obtenerlos y filtrarlos por rol.</p>
 */
public class ControladorUsuarios {

    // Lista para guardar los usuarios "quemados"
    private List<Usuario> usuariosGuardados;

    /**
     * Crea el controlador y carga los usuarios de ejemplo en memoria.
     */
    public ControladorUsuarios() {
        this.usuariosGuardados = new ArrayList<>();
        cargarUsuariosQuemados();
    }

    /**
     * Agrega al sistema los usuarios de prueba definidos en el proyecto.
     */
    private void cargarUsuariosQuemados() {
        // Estudiantes
        usuariosGuardados.add(new Usuario("1001", "Juan", "Perez", "juan.perez@estudiante.edu", "Estudiante"));
        usuariosGuardados.add(new Usuario("1002", "Edwin", "Leal", "edwin.leal@estudiante.edu", "Estudiante"));

        // Tutores
        usuariosGuardados.add(new Usuario("2001", "Carlos", "Ramirez", "carlos.ramirez@tutor.edu", "Tutor"));

        // Asesores
        usuariosGuardados.add(new Usuario("3001", "Luis", "Fernandez", "luis.fernandez@asesor.edu", "Asesor"));
    }

    /**
     * Devuelve la lista de usuarios almacenados en memoria.
     *
     * @return lista de usuarios predefinidos
     */
    public List<Usuario> getUsuariosGuardados() {
        return usuariosGuardados;
    }

    /**
     * Busca y devuelve los usuarios que coinciden con el rol especificado.
     *
     * @param rol nombre del rol a filtrar (por ejemplo, "Estudiante")
     * @return lista de usuarios con el rol especificado
     */
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
