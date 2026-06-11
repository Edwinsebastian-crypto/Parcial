package com.mycompany.parcial.vista;

import com.mycompany.parcial.modelo.Evidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Formulario de revisión de evidencia con campos de estado, calificación y archivo.
 *
 * <p>Se reutiliza dentro del panel de tutor para leer y actualizar propiedades
 * de una evidencia seleccionada.</p>
 */
public class PathFormPanel extends JPanel {

    // Componentes del formulario
    private JLabel lblTitulo;
    private JPanel innerBox;
    private JTextField txtIdEstudiante;
    private JTextField txtNombres;
    private JButton btnRevisar;
    private JComboBox<String> comboEstado;
    private JTextField txtCalificacion;
    private JButton btnCancelarForm;
    private JButton btnAceptar;

    private Evidencia evidenciaActual;

    public PathFormPanel() {
        setLayout(null); // Layout nulo para posicionamiento absoluto exacto
        setBackground(StyleUtils.COLOR_CONTENT_BG);

        // --- Título Superior "Path archivo" ---
        lblTitulo = new JLabel(StyleUtils.TITULO_FORM_PATH);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22)); // Letra negrita y más grande
        lblTitulo.setBounds(30, 20, 200, 30);
        add(lblTitulo);

        // --- Panel Interno Plano de Formulario (Sin sombra) ---
        innerBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(StyleUtils.COLOR_INNER_PANEL);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        // Reducido el ancho y movido más al centro (antes era 30, 60, 1000, 400)
        innerBox.setBounds(150, 60, 700, 400); 

        // --- CAMPOS DE TEXTO ---
        int yFields = 40;
        int altoField = 30;

        txtIdEstudiante = crearCampoFormulario(innerBox, StyleUtils.LABEL_FORM_ID_ESTUDIANTE, yFields, 300, altoField, false);
        txtIdEstudiante.setHorizontalAlignment(JTextField.CENTER);
        yFields += 50;

        txtNombres = crearCampoFormulario(innerBox, StyleUtils.LABEL_FORM_NOMBRES, yFields, 300, altoField, false);
        txtNombres.setHorizontalAlignment(JTextField.CENTER);
        yFields += 50;

        btnRevisar = crearCampoFormularioConBoton(innerBox, StyleUtils.LABEL_FORM_PATH, yFields, altoField);
        btnRevisar.addActionListener(e -> abrirDialogoRevision());
        yFields += 50;

        // --- ESTADO Y CALIFICACIÓN ---
        int yExtra = yFields;
        // Etiqueta Estado
        JLabel lblEstado = new JLabel(StyleUtils.LABEL_FORM_ESTADO);
        lblEstado.setFont(StyleUtils.FUENTE_REGULAR);
        lblEstado.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEstado.setBounds(50, yExtra, 250, altoField);
        innerBox.add(lblEstado);

        // Dropdown Estado (JComboBox)
        comboEstado = new JComboBox<>(new String[] { "Seleccione...", StyleUtils.TEXTO_COMBO_REVISADO, StyleUtils.TEXTO_COMBO_SIN_REVISAR });
        comboEstado.setBounds(320, yExtra, 200, altoField);
        comboEstado.setBackground(StyleUtils.COLOR_BUTTON_BLUE);
        comboEstado.setFont(StyleUtils.FUENTE_REGULAR);
        comboEstado.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        innerBox.add(comboEstado);

        // Etiqueta Calificación
        yExtra += 50;
        JLabel lblCalificacion = new JLabel(StyleUtils.LABEL_FORM_CALIFICACION);
        lblCalificacion.setFont(StyleUtils.FUENTE_REGULAR);
        lblCalificacion.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCalificacion.setBounds(50, yExtra, 250, altoField);
        
        // Campo Calificación
        txtCalificacion = crearCampoFormulario(innerBox, "Calificación:", yExtra, 100, altoField, true);
        txtCalificacion.setHorizontalAlignment(JTextField.CENTER);
        txtCalificacion.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { autoCambiarEstado(); }
            public void removeUpdate(DocumentEvent e) { autoCambiarEstado(); }
            public void changedUpdate(DocumentEvent e) { autoCambiarEstado(); }
        });
        innerBox.add(txtCalificacion);

        add(innerBox);

        // --- BOTONES DE FORMULARIO INFERIORES ---
        int yBotones = 480; // Ajustado para estar debajo del innerBox más grande

        btnCancelarForm = crearBotonRedondeadoDialog("Cancelar", Color.WHITE, Color.BLACK);
        btnCancelarForm.setBounds(150, yBotones, 120, 35);
        agregarHoverAzul(btnCancelarForm);
        add(btnCancelarForm);

        btnAceptar = crearBotonRedondeadoDialog("Aceptar", Color.WHITE, Color.BLACK);
        btnAceptar.setBounds(730, yBotones, 120, 35);
        agregarHoverAzul(btnAceptar);
        add(btnAceptar);
    }

    public void cargarEvidencia(Evidencia ev) {
        this.evidenciaActual = ev;
        txtIdEstudiante.setText(ev.getIdEstudiante());
        txtNombres.setText(ev.getNombreEstudiante());
        txtCalificacion.setText(ev.getCalificacion());
        if ("Revisado".equals(ev.getEstado())) {
            comboEstado.setSelectedItem(StyleUtils.TEXTO_COMBO_REVISADO);
        } else if ("Sin revisar".equals(ev.getEstado())) {
            comboEstado.setSelectedItem(StyleUtils.TEXTO_COMBO_SIN_REVISAR);
        } else {
            comboEstado.setSelectedIndex(0);
        }
    }

    public Evidencia getEvidenciaActual() {
        return evidenciaActual;
    }

    public void guardarCambiosEnEvidencia() throws IllegalArgumentException {
        if (evidenciaActual != null) {
            String califStr = txtCalificacion.getText().trim();
            if (!califStr.isEmpty()) {
                try {
                    double v = Double.parseDouble(califStr);
                    if (v < 0 || v > 5) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("La calificación debe ser un número entre 0 y 5.");
                }
            }

            evidenciaActual.setCalificacion(califStr);
            String estadoSelec = (String) comboEstado.getSelectedItem();
            if (StyleUtils.TEXTO_COMBO_REVISADO.equals(estadoSelec)) {
                evidenciaActual.setEstado("Revisado");
            } else if (StyleUtils.TEXTO_COMBO_SIN_REVISAR.equals(estadoSelec)) {
                evidenciaActual.setEstado("Sin revisar");
            }
        }
    }

    private void autoCambiarEstado() {
        String califStr = txtCalificacion.getText().trim();
        if (califStr.matches("[0-5]")) {
            comboEstado.setSelectedItem(StyleUtils.TEXTO_COMBO_REVISADO);
        }
    }

    private void abrirDialogoRevision() {
        if (evidenciaActual == null) return;
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalles de la Evidencia", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(25, 25, 10, 25));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = StyleUtils.FUENTE_REGULAR;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblNombre = new JLabel("Nombre Evidencia:");
        lblNombre.setFont(labelFont);
        formPanel.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.3;
        JTextField txtNombre = new JTextField(evidenciaActual.getNombreEvidencia());
        txtNombre.setFont(labelFont);
        txtNombre.setHorizontalAlignment(JTextField.CENTER);
        txtNombre.setEditable(false);
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setFont(labelFont);
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea txtDesc = new JTextArea(evidenciaActual.getDescripcion());
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setFont(labelFont);
        txtDesc.setEditable(false);
        txtDesc.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new javax.swing.border.LineBorder(new Color(180,180,180), 1, true));
        formPanel.add(scrollDesc, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblArch = new JLabel("Archivo:");
        lblArch.setFont(labelFont);
        formPanel.add(lblArch, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel filePanel = new JPanel(new BorderLayout(8, 0));
        filePanel.setBackground(Color.WHITE);

        String path = evidenciaActual.getPathArchivo();
        String nombreArch = (path != null && !path.isEmpty()) ? new File(path).getName() : "Ningún archivo";
        JLabel lblArchivo = new JLabel(nombreArch);
        lblArchivo.setHorizontalAlignment(SwingConstants.CENTER);
        lblArchivo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblArchivo.setForeground(Color.DARK_GRAY);

        JButton btnDescargar = crearBotonRedondeadoDialog("Descargar", Color.WHITE, Color.BLACK);
        agregarHoverAzul(btnDescargar);
        btnDescargar.addActionListener(e -> {
            if (path == null || path.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No hay archivo asociado a esta evidencia.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File archivoOrigen = new File(path);
            if (!archivoOrigen.exists()) {
                JOptionPane.showMessageDialog(dialog, "El archivo original ya no existe en la ruta:\n" + path, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar archivo como...");
            chooser.setSelectedFile(new File(archivoOrigen.getName()));
            int resultado = chooser.showSaveDialog(dialog);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File destino = chooser.getSelectedFile();
                try {
                    Files.copy(archivoOrigen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(dialog, "Archivo descargado correctamente en:\n" + destino.getAbsolutePath(), "Descarga exitosa", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al descargar el archivo:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        filePanel.add(lblArchivo, BorderLayout.CENTER);
        filePanel.add(btnDescargar, BorderLayout.EAST);
        formPanel.add(filePanel, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnCerrar = crearBotonRedondeadoDialog("Cerrar", Color.WHITE, Color.BLACK);
        agregarHoverAzul(btnCerrar);
        btnCerrar.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCerrar);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Método auxiliar para campos de texto planos con fondo azul clarito
    private JTextField crearCampoFormulario(JPanel container, String label, int y, int anchoField, int alto, boolean editable) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(StyleUtils.FUENTE_REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Columna 1
        lbl.setBounds(50, y, 250, alto);
        container.add(lbl);

        JTextField txt = new JTextField();
        txt.setBounds(320, y, anchoField, alto); // Columna 2
        txt.setBackground(StyleUtils.COLOR_BUTTON_BLUE);
        txt.setBorder(null);
        txt.setEditable(editable);
        container.add(txt);
        return txt;
    }

    // Método auxiliar para campo con botón integrado (sin campo de texto)
    private JButton crearCampoFormularioConBoton(JPanel container, String label, int y, int alto) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(StyleUtils.FUENTE_REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Columna 1
        lbl.setBounds(50, y, 250, alto);
        container.add(lbl);

        JButton btn = crearBotonRedondeadoDialog(StyleUtils.TEXTO_BOTON_REVISAR, Color.WHITE, Color.BLACK);
        btn.setBounds(320, y, 120, alto); // Columna 2
        agregarHoverAzul(btn);
        container.add(btn);
        return btn;
    }

    // Botón redondeado estético para los botones principales
    private JButton crearBotonRedondeadoDialog(String texto, Color bgColor, Color fgColor) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(getForeground());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setFont(getFont());
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        boton.setBackground(bgColor);
        boton.setForeground(fgColor);
        boton.setFont(StyleUtils.FUENTE_REGULAR);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    // Efecto hover azul reutilizable para cualquier botón redondeado
    private void agregarHoverAzul(JButton boton) {
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(StyleUtils.COLOR_SIDEBAR_SELECTED);
                boton.setForeground(Color.WHITE);
                boton.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(Color.BLACK);
                boton.repaint();
            }
        });
    }

    // Permite al controlador externo añadir listener al botón cancelar
    public void addCancelarListener(ActionListener listener) {
        btnCancelarForm.addActionListener(listener);
    }
    
    // Permite al controlador externo añadir listener al botón aceptar
    public void addAceptarListener(ActionListener listener) {
        btnAceptar.addActionListener(listener);
    }
}

