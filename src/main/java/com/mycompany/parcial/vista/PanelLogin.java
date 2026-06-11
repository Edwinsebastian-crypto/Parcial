package com.mycompany.parcial.vista;

import javax.swing.*;
import java.awt.*;
import com.mycompany.parcial.controlador.ControladorUsuarios;
import com.mycompany.parcial.modelo.Usuario;

/**
 * Panel de inicio de sesión que solicita cédula y rol de usuario.
 *
 * <p>Se integra dentro de la barra lateral del panel principal y valida el
 * usuario contra una lista de usuarios predefinidos.</p>
 */
public class PanelLogin extends JPanel {

    private PanelEstudiante panelEstudiante;
    private ControladorUsuarios controladorUsuarios;
    private JTextField txtUsuario;
    private JComboBox<String> comboRol;

    public PanelLogin(PanelEstudiante panelEstudiante) {
        this.panelEstudiante = panelEstudiante;
        this.controladorUsuarios = new ControladorUsuarios();
        
        setLayout(null);
        setOpaque(false); // Transparente para que se vea el fondo de la barra lateral

        JLabel lblUsuario = new JLabel("Cédula:");
        lblUsuario.setFont(StyleUtils.FUENTE_REGULAR);
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setBounds(0, 0, 220, 20);
        add(lblUsuario);

        txtUsuario = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        txtUsuario.setBounds(0, 20, 220, 25);
        txtUsuario.setOpaque(false);
        txtUsuario.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(txtUsuario);

        JLabel lblRol = new JLabel("Rol:");
        lblRol.setFont(StyleUtils.FUENTE_REGULAR);
        lblRol.setForeground(Color.WHITE);
        lblRol.setBounds(0, 50, 220, 20);
        add(lblRol);

        String[] roles = {"Seleccione...", "Estudiante", "Tutor académico", "Asesor pedagógico"};
        comboRol = new JComboBox<>(roles) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paint(g2);
                g2.dispose();
            }
        };
        comboRol.setBounds(0, 70, 220, 25);
        comboRol.setOpaque(false);
        comboRol.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(comboRol);

        JButton btnIngresar = new JButton("Ingresar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnIngresar.setBounds(0, 105, 220, 35);
        btnIngresar.setBackground(StyleUtils.COLOR_SIDEBAR_SELECTED);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFont(StyleUtils.FUENTE_MENU);
        btnIngresar.setContentAreaFilled(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(btnIngresar);

        btnIngresar.addActionListener(e -> procesarLogin());
    }

    /**
     * Valida el usuario ingresado y notifica al panel principal si el login fue
     * exitoso o no.
     */
    private void procesarLogin() {
        String cedulaInput = txtUsuario.getText().trim();
        String rolUI = (String) comboRol.getSelectedItem();
        
        String rolModelo = "";
        if ("Estudiante".equals(rolUI)) rolModelo = "Estudiante";
        else if ("Tutor académico".equals(rolUI)) rolModelo = "Tutor";
        else if ("Asesor pedagógico".equals(rolUI)) rolModelo = "Asesor";

        if (cedulaInput.isEmpty() || rolModelo.isEmpty()) {
            Window ventana = SwingUtilities.getWindowAncestor(this);
            JOptionPane.showMessageDialog(ventana, "Debe ingresar su cédula y seleccionar un rol válido.");
            return;
        }

        Usuario usuarioValido = null;
        for (Usuario u : controladorUsuarios.getUsuariosGuardados()) {
            if (u.getCedula().equals(cedulaInput) && u.getRol().equals(rolModelo)) {
                usuarioValido = u;
                break;
            }
        }

        if (usuarioValido != null) {
            panelEstudiante.loginExitoso(rolUI, usuarioValido);
        } else {
            Window ventana = SwingUtilities.getWindowAncestor(this);
            JOptionPane.showMessageDialog(ventana, "Credenciales incorrectas o rol no coincidente.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            panelEstudiante.loginFallido();
        }
    }

    /**
     * Limpia los campos del formulario de login para una nueva sesión.
     */
    public void limpiarFormulario() {
        txtUsuario.setText("");
        comboRol.setSelectedIndex(0);
    }
}
