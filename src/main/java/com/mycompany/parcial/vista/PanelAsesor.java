/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parcial.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.mycompany.parcial.modelo.Evidencia;
import com.mycompany.parcial.controlador.GestorEvidencias;

/**
 * Panel dedicado a los asesores pedagógicos.
 *
 * <p>Permite consultar observaciones sobre los estudiantes y editar comentarios
 * en un formulario detallado.</p>
 */
public class PanelAsesor extends JPanel {

    private CardLayout cardLayout;
    private JPanel panelContenidoDinamico;
    private DefaultTableModel modeloTablaAsesor;
    
    // Variables de formulario
    private Evidencia evidenciaActual;
    private JTextField txtIdEstudianteForm;
    private JTextField txtNombresForm;
    private JTextArea txtObservacionesForm;
    
    // Callback para el botón Salir
    private Runnable onSalirListener;

    public void setOnSalirListener(Runnable listener) {
        this.onSalirListener = listener;
    }

    public PanelAsesor() {
        setLayout(new BorderLayout());
        setBackground(StyleUtils.COLOR_CONTENT_BG);

        // 1. Menú lateral izquierdo (Con "Asesores Pedagógicos" seleccionado)
        add(crearMenuLateral(), BorderLayout.WEST);

        // 2. Contenedor Derecho (Header + Contenido Central Dinámico)
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Agregar Header estático ("STEP")
        panelDerecho.add(crearHeader(), BorderLayout.NORTH);

        // 3. CardLayout para alternar entre Tabla y Formulario
        cardLayout = new CardLayout();
        panelContenidoDinamico = new JPanel(cardLayout);
        panelContenidoDinamico.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Agregar las dos vistas al CardLayout
        panelContenidoDinamico.add(crearVistaTabla(), "VISTA_TABLA");
        panelContenidoDinamico.add(crearVistaFormulario(), "VISTA_FORMULARIO");

        panelDerecho.add(panelContenidoDinamico, BorderLayout.CENTER);

        add(panelDerecho, BorderLayout.CENTER);
    }

    /**
     * Devuelve solo el contenido dinámico (tabla + formulario) sin sidebar ni header,
     * para ser reutilizado dentro de otro panel principal.
     */
    public JPanel getContenidoDinamico() {
        return panelContenidoDinamico;
    }

    /**
     * VISTA 1: Tabla principal del Asesor Pedagógico (Imagen 14)
     */
    private JPanel crearVistaTabla() {
        JPanel vista = new JPanel(new BorderLayout());
        vista.setBackground(StyleUtils.COLOR_CONTENT_BG);
        vista.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Configuración de la Tabla ---
        String[] columnas = { "Id Estudiante", "Nombre del Estudiante", "Observación" };

        modeloTablaAsesor = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Evita que se edite directamente en la tabla
            }
        };

        JTable tabla = new JTable(modeloTablaAsesor) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                if (rowIndex >= 0 && colIndex >= 0) {
                    Object value = getValueAt(rowIndex, colIndex);
                    if (value != null && !value.toString().isEmpty()) {
                        return "<html><p width='300'>" + value.toString().replaceAll("\n", "<br>") + "</p></html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };
        tabla.setRowHeight(80);
        tabla.setShowGrid(true);
        tabla.setGridColor(Color.BLACK);
        tabla.setBackground(Color.WHITE);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(StyleUtils.FUENTE_REGULAR);
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        header.setPreferredSize(new Dimension(0, 40));

        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(350);

        // Centrar el contenido de todas las celdas
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // EVENTO: Al hacer clic en una fila, se abre el formulario
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabla.getSelectedRow();
                if (row >= 0) {
                    // Buscar la Evidencia seleccionada usando GestorEvidencias
                    // Como el modelo de Asesor carga en orden, o podemos buscar por Id Estudiante
                    String idEst = (String) modeloTablaAsesor.getValueAt(row, 0);
                    for (Evidencia ev : GestorEvidencias.getInstancia().getEvidencias()) {
                        if (ev.getIdEstudiante().equals(idEst)) {
                            evidenciaActual = ev;
                            txtIdEstudianteForm.setText(ev.getIdEstudiante());
                            txtNombresForm.setText(ev.getNombreEstudiante());
                            txtObservacionesForm.setText(ev.getObservacion() != null ? ev.getObservacion() : "");
                            break;
                        }
                    }
                    cardLayout.show(panelContenidoDinamico, "VISTA_FORMULARIO");
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        StyleUtils.estilizarScrollBar(scrollPane); // Scroll estético

        vista.add(scrollPane, BorderLayout.CENTER);

        // --- Botón Salir Inferior Derecho ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        panelBotones.setBackground(StyleUtils.COLOR_CONTENT_BG);

        JButton btnSalir = crearBotonRedondeado("Salir", Color.WHITE, Color.BLACK);
        btnSalir.setPreferredSize(new Dimension(120, 35));
        agregarHoverAzul(btnSalir);
        btnSalir.addActionListener(e -> {
            if (onSalirListener != null) {
                onSalirListener.run();
            }
        });
        panelBotones.add(btnSalir);
        
        vista.add(panelBotones, BorderLayout.SOUTH);

        cargarDatosTablaAsesor();

        return vista;
    }

    public void cargarDatosTablaAsesor() {
        modeloTablaAsesor.setRowCount(0);
        for (Evidencia ev : GestorEvidencias.getInstancia().getEvidencias()) {
            modeloTablaAsesor.addRow(new Object[]{
                ev.getIdEstudiante(),
                ev.getNombreEstudiante(),
                ev.getObservacion()
            });
        }
        while (modeloTablaAsesor.getRowCount() < 5) {
            modeloTablaAsesor.addRow(new Object[]{"", "", ""});
        }
    }

    /**
     * VISTA 2: Formulario de detalle al hacer clic en un estudiante (Imagen 15)
     */
    private JPanel crearVistaFormulario() {
        JPanel vista = new JPanel(null);
        vista.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Contenedor "gris" central
        JPanel panelGris = new JPanel(null) {
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
        panelGris.setBounds(150, 60, 700, 400); // Igual que el tutor
        vista.add(panelGris);

        // --- Elementos dentro del panel gris ---
        Font fuenteLabels = StyleUtils.FUENTE_REGULAR;

        // 1. Id Estudiante
        JLabel lblId = new JLabel("Id Estudiante:");
        lblId.setFont(fuenteLabels);
        lblId.setHorizontalAlignment(SwingConstants.RIGHT);
        lblId.setBounds(50, 40, 200, 30);
        panelGris.add(lblId);

        txtIdEstudianteForm = new JTextField();
        txtIdEstudianteForm.setFont(fuenteLabels);
        txtIdEstudianteForm.setEditable(false);
        txtIdEstudianteForm.setHorizontalAlignment(JTextField.CENTER);
        txtIdEstudianteForm.setBackground(StyleUtils.COLOR_BUTTON_BLUE);
        txtIdEstudianteForm.setBorder(null);
        txtIdEstudianteForm.setBounds(270, 40, 300, 30);
        panelGris.add(txtIdEstudianteForm);

        // 2. Nombres y apellidos
        JLabel lblNombres = new JLabel("Nombres y apellidos estudiante:");
        lblNombres.setFont(fuenteLabels);
        lblNombres.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNombres.setBounds(30, 90, 220, 30);
        panelGris.add(lblNombres);

        txtNombresForm = new JTextField();
        txtNombresForm.setFont(fuenteLabels);
        txtNombresForm.setEditable(false);
        txtNombresForm.setHorizontalAlignment(JTextField.CENTER);
        txtNombresForm.setBackground(StyleUtils.COLOR_BUTTON_BLUE);
        txtNombresForm.setBorder(null);
        txtNombresForm.setBounds(270, 90, 300, 30);
        panelGris.add(txtNombresForm);

        // 3. Observaciones
        JLabel lblObs = new JLabel("Observaciones:");
        lblObs.setFont(fuenteLabels);
        lblObs.setHorizontalAlignment(SwingConstants.RIGHT);
        lblObs.setBounds(50, 140, 200, 30);
        panelGris.add(lblObs);

        txtObservacionesForm = new JTextArea();
        txtObservacionesForm.setFont(fuenteLabels);
        txtObservacionesForm.setLineWrap(true);
        txtObservacionesForm.setWrapStyleWord(true);
        txtObservacionesForm.setBackground(StyleUtils.COLOR_BUTTON_BLUE);
        txtObservacionesForm.setBorder(null);
        JScrollPane scrollObs = new JScrollPane(txtObservacionesForm);
        scrollObs.setBorder(null);
        scrollObs.setBounds(270, 140, 380, 100);
        panelGris.add(scrollObs);

        // --- Botones dentro del panel gris ---
        int yBotones = 300;
        
        JButton btnCancelar = crearBotonRedondeado("Cancelar", Color.WHITE, Color.BLACK);
        btnCancelar.setBounds(150, yBotones, 120, 35);
        agregarHoverAzul(btnCancelar);
        btnCancelar.addActionListener(e -> cardLayout.show(panelContenidoDinamico, "VISTA_TABLA"));
        panelGris.add(btnCancelar);

        JButton btnAceptarInterno = crearBotonRedondeado("Aceptar", Color.WHITE, Color.BLACK);
        btnAceptarInterno.setBounds(430, yBotones, 120, 35);
        agregarHoverAzul(btnAceptarInterno);
        btnAceptarInterno.addActionListener(e -> {
            if (evidenciaActual != null) {
                evidenciaActual.setObservacion(txtObservacionesForm.getText());
                cargarDatosTablaAsesor();
                JOptionPane.showMessageDialog(vista, "Observación guardada correctamente.");
                cardLayout.show(panelContenidoDinamico, "VISTA_TABLA");
            }
        });
        panelGris.add(btnAceptarInterno);

        return vista;
    }

    public void resetToTabla() {
        cardLayout.show(panelContenidoDinamico, "VISTA_TABLA");
    }

    /**
     * Componentes UI Auxiliares
     */
    private JPanel crearInputRedondeado(Color bgColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private static JPanel crearEtiquetaRedondeada(String texto, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);

        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(StyleUtils.FUENTE_REGULAR);
        label.setForeground(Color.BLACK);
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new MatteBorder(0, 0, 1, 0, StyleUtils.COLOR_BORDER));

        JLabel lblStep = new JLabel("STEP");
        lblStep.setFont(StyleUtils.FUENTE_PRINCIPAL_STEP);
        lblStep.setBorder(new EmptyBorder(0, 30, 0, 0));
        header.add(lblStep, BorderLayout.WEST);

        return header;
    }

    private JPanel crearMenuLateral() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(StyleUtils.COLOR_SIDEBAR_BG);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(StyleUtils.COLOR_SIDEBAR_TOP);
                g2d.fillRect(0, 0, getWidth(), 120);

                dibujarLogoSidebar(g2d, 15, 75);

                // Selector en "Asesores Pedagógicos" (Posición Y: 285 aprox)
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(StyleUtils.COLOR_SIDEBAR_SELECTED);
                g2d.fillRoundRect(0, 285, getWidth(), 60, 20, 20);

                g2d.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(null);

        // Estudiantes (Inactivo)
        JLabel lblEstudiantes = new JLabel("Estudiantes");
        lblEstudiantes.setFont(StyleUtils.FUENTE_MENU);
        lblEstudiantes.setForeground(Color.WHITE);
        lblEstudiantes.setBounds(90, 140, 180, 50);
        sidebar.add(lblEstudiantes);

        // Tutores Académicos (Inactivo)
        JLabel lblTutores = new JLabel("<html><div style='text-align: center;'>Tutores<br>Académicos</div></html>");
        lblTutores.setFont(StyleUtils.FUENTE_MENU);
        lblTutores.setForeground(Color.WHITE);
        lblTutores.setBounds(90, 215, 180, 50);
        sidebar.add(lblTutores);

        // Asesores Pedagógicos (Activo / texto negro)
        JLabel lblAsesores = new JLabel("<html><div style='text-align: center;'>Asesores<br>Pedagógicos</div></html>");
        lblAsesores.setFont(StyleUtils.FUENTE_MENU);
        lblAsesores.setForeground(Color.BLACK); // Texto negro sobre el resaltado
        lblAsesores.setBounds(90, 290, 180, 50);
        sidebar.add(lblAsesores);

        return sidebar;
    }

    private void dibujarLogoSidebar(Graphics2D g2d, int x, int y) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(StyleUtils.COLOR_UDI_TEXT);
        g2d.setFont(StyleUtils.FUENTE_UDI_LARGE);
        g2d.drawString("UD", x, y);

        FontMetrics metricsUD = g2d.getFontMetrics(StyleUtils.FUENTE_UDI_LARGE);
        int udWidth = metricsUD.stringWidth("UD");
        int udiHeight = metricsUD.getAscent();
        int logoTop = y - udiHeight;

        int xI = x + udWidth + 2;
        int squareSize = 10;
        int xSquare = xI;
        int ySquare = logoTop + 4;

        g2d.setColor(StyleUtils.COLOR_UDI_TEXT);
        g2d.fillRect(xSquare, ySquare, squareSize, squareSize);

        g2d.setColor(StyleUtils.COLOR_UDI_SQUARE_YELLOW);
        int[] xPolPoints = { xSquare, xSquare + squareSize, xSquare + squareSize };
        int[] yPolPoints = { ySquare, ySquare, ySquare + squareSize };
        g2d.fillPolygon(xPolPoints, yPolPoints, 3);

        int yStem = ySquare + squareSize + 3;
        int stemHeight = y - yStem;
        g2d.setColor(StyleUtils.COLOR_UDI_TEXT);
        g2d.fillRect(xI, yStem, squareSize, stemHeight);

        g2d.setColor(StyleUtils.COLOR_UDI_DESCRIPTIVE_TEXT);
        g2d.setFont(StyleUtils.FUENTE_UDI_DESCRIPTIVE);
        FontMetrics metricsText = g2d.getFontMetrics(StyleUtils.FUENTE_UDI_DESCRIPTIVE);

        int xText = xI + squareSize + 10;
        int lineHeight = metricsText.getHeight() - 1;
        int yStartText = logoTop + metricsText.getAscent() + 3;

        g2d.drawString(StyleUtils.TEXTO_UNIVERSIDAD, xText, yStartText);
        g2d.drawString(StyleUtils.TEXTO_INVESTIGACION, xText, yStartText + lineHeight);
        g2d.drawString(StyleUtils.TEXTO_DESARROLLO, xText, yStartText + (2 * lineHeight));
    }

    // Botón redondeado estético para los botones principales
    private JButton crearBotonRedondeado(String texto, Color bgColor, Color fgColor) {
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
}