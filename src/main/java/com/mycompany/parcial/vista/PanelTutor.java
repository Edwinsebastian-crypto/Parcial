package com.mycompany.parcial.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import com.mycompany.parcial.modelo.Evidencia;
import com.mycompany.parcial.modelo.Usuario;
import com.mycompany.parcial.controlador.GestorEvidencias;

public class PanelTutor extends JPanel {

    // Variables para manejar la navegación entre la tabla y el formulario
    private CardLayout cardLayout;
    private JPanel panelContenidoDinamico;
    private PathFormPanel panelFormularioPath; // Asegúrate de tener creada esta clase
    private DefaultTableModel modeloTablaTutor;
    private JTable tablaTutor;
    private Usuario usuarioActual;
    
    // Callback para el botón Salir
    private Runnable onSalirListener;

    public void setOnSalirListener(Runnable listener) {
        this.onSalirListener = listener;
    }

    public void setUsuarioActual(Usuario u) {
        this.usuarioActual = u;
    }

    public PanelTutor() {
        setLayout(new BorderLayout());
        setBackground(StyleUtils.COLOR_CONTENT_BG);

        // 1. Menú lateral izquierdo (Con "Tutores Académicos" seleccionado)
        add(crearMenuLateral(), BorderLayout.WEST);

        // 2. Contenedor Derecho (Header + Contenido Central)
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Agregar Header estático ("STEP")
        panelDerecho.add(crearHeader(), BorderLayout.NORTH);

        // --- INICIO DE LA ACTUALIZACIÓN: CardLayout ---
        cardLayout = new CardLayout();
        panelContenidoDinamico = new JPanel(cardLayout);
        panelContenidoDinamico.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Vista 1: Tu contenido central modificado (La Tabla)
        panelContenidoDinamico.add(crearContenidoCentral(), "VISTA_TABLA");

        // Vista 2: El nuevo Formulario
        panelFormularioPath = new PathFormPanel();
        panelContenidoDinamico.add(panelFormularioPath, "VISTA_FORMULARIO");

        // Listener para regresar del formulario a la tabla (Cancelar)
        panelFormularioPath.addCancelarListener(e -> cardLayout.show(panelContenidoDinamico, "VISTA_TABLA"));

        // Listener para guardar cambios (Aceptar)
        panelFormularioPath.addAceptarListener(e -> {
            Evidencia ev = panelFormularioPath.getEvidenciaActual();
            if (ev != null) {
                try {
                    // Actualizar atributos de la evidencia con los valores del formulario
                    panelFormularioPath.guardarCambiosEnEvidencia();
                    
                    // Si se puso una calificación, guardamos el tutor y la fecha
                    if (ev.getCalificacion() != null && !ev.getCalificacion().trim().isEmpty() && usuarioActual != null) {
                        ev.setProfesor(usuarioActual.getCedula() + " - " + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
                        ev.setFechaCalificacion(LocalDate.now().toString());
                    }

                    cargarDatosTablaTutor();
                    JOptionPane.showMessageDialog(this, "Evidencia actualizada correctamente.");
                    cardLayout.show(panelContenidoDinamico, "VISTA_TABLA");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelDerecho.add(panelContenidoDinamico, BorderLayout.CENTER);
        // --- FIN DE LA ACTUALIZACIÓN ---

        add(panelDerecho, BorderLayout.CENTER);
    }

    public void resetToTabla() {
        cardLayout.show(panelContenidoDinamico, "VISTA_TABLA");
    }

    /**
     * Devuelve solo el contenido dinámico (tabla + formulario) sin sidebar ni
     * header,
     * para ser reutilizado dentro de otro panel principal.
     */
    public JPanel getContenidoDinamico() {
        return panelContenidoDinamico;
    }

    /**
     * Construye el área de contenido con la tabla y las etiquetas redondeadas
     * (Se le quitó el 'static' para poder usar el CardLayout)
     */
    public JPanel crearContenidoCentral() {
        JPanel vista = new JPanel(new BorderLayout());
        vista.setBackground(StyleUtils.COLOR_CONTENT_BG);
        vista.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Etiquetas Redondeadas Superiores Eliminadas ---

        // --- Configuración de la Tabla de Tutor ---
        String[] columnas = { "Id Evidencia", "Id Estudiante", "Nombre del Estudiante", "Path archivo", "Calificación",
                "Estado", "Observación" };

        // ACTUALIZACIÓN: Añadimos la palabra "Abrir" para que el botón se dibuje en
        // esas celdas

        // ACTUALIZACIÓN: DefaultTableModel personalizado para permitir solo clic en el
        // botón
        modeloTablaTutor = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Solo la columna "Path archivo" es editable para el botón
            }
        };

        tablaTutor = new JTable(modeloTablaTutor) {
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

        tablaTutor.setRowHeight(80); // Altura de fila grande para espacio visual
        tablaTutor.setShowGrid(true);
        tablaTutor.setGridColor(Color.BLACK);
        tablaTutor.setBackground(Color.WHITE);

        JTableHeader header = tablaTutor.getTableHeader();
        header.setFont(StyleUtils.FUENTE_REGULAR);
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        header.setPreferredSize(new Dimension(0, 40));

        tablaTutor.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Ocultar columna Id Evidencia (índice 0)
        tablaTutor.getColumnModel().getColumn(0).setMinWidth(0);
        tablaTutor.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaTutor.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Anchos de columna personalizados para ajustarse a los nuevos datos
        tablaTutor.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaTutor.getColumnModel().getColumn(2).setPreferredWidth(180);
        tablaTutor.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaTutor.getColumnModel().getColumn(4).setPreferredWidth(120);
        tablaTutor.getColumnModel().getColumn(5).setPreferredWidth(100);
        tablaTutor.getColumnModel().getColumn(6).setPreferredWidth(180);

        // ACTUALIZACIÓN: Configurar el renderizador y el editor para la columna de botones (índice 3)
        tablaTutor.getColumnModel().getColumn(3).setCellRenderer(new TutorButtonRenderer());
        tablaTutor.getColumnModel().getColumn(3).setCellEditor(new TutorButtonEditor());

        // Centrar las demás celdas
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaTutor.getColumnCount(); i++) {
            if (i != 3) {
                tablaTutor.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(tablaTutor);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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

        cargarDatosTablaTutor();

        return vista;
    }

    public void cargarDatosTablaTutor() {
        modeloTablaTutor.setRowCount(0);
        for (Evidencia ev : GestorEvidencias.getInstancia().getEvidencias()) {
            modeloTablaTutor.addRow(new Object[] {
                    ev.getIdEvidencia(),
                    ev.getIdEstudiante(),
                    ev.getNombreEstudiante(),
                    "Abrir",
                    ev.getCalificacion(),
                    ev.getEstado(),
                    ev.getObservacion()
            });
        }
        while (modeloTablaTutor.getRowCount() < 4) {
            modeloTablaTutor.addRow(new Object[] { "", "", "", "", "", "", "" });
        }
    }

    // --- NUEVAS CLASES INTERNAS PARA EL BOTÓN EN LA TABLA ---

    // 1. Renderizador visual del botón
    class TutorButtonRenderer extends JButton implements TableCellRenderer {
        public TutorButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value != null && !value.toString().isEmpty()) {
                setText(value.toString());
                setFont(StyleUtils.FUENTE_REGULAR);
                setForeground(Color.BLACK);
                return this;
            }
            return null; // Si no hay texto, no dibuja el botón
        }
    }

    // 2. Editor del botón (Lógica del Clic)
    class TutorButtonEditor extends DefaultCellEditor implements TableCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;

        public TutorButtonEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    isPushed = true;
                    stopCellEditing(); // Detener edición

                    if (tablaTutor != null) {
                        int row = tablaTutor.getSelectedRow();
                        if (row >= 0) {
                            String idEvidencia = (String) modeloTablaTutor.getValueAt(row, 0);
                            if (idEvidencia != null && !idEvidencia.isEmpty()) {
                                for (Evidencia ev : GestorEvidencias.getInstancia().getEvidencias()) {
                                    if (ev.getIdEvidencia().equals(idEvidencia)) {
                                        panelFormularioPath.cargarEvidencia(ev);
                                        cardLayout.show(panelContenidoDinamico, "VISTA_FORMULARIO");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setFont(StyleUtils.FUENTE_REGULAR);
            isPushed = false;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
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

    /**
     * Método auxiliar para crear los paneles de etiquetas con bordes redondeados
     */
    private static JPanel crearEtiquetaRedondeada(String texto, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Bordes redondos
                g2d.dispose();
            }
        };
        panel.setOpaque(false); // Para que el fondo real se vea a través de las esquinas

        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(StyleUtils.FUENTE_REGULAR);
        label.setForeground(Color.BLACK);
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Componentes estáticos fijos (Header y Sidebar)
     */
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

                // CRUCIAL: Mover el selector redondeado a la posición de "Tutores Académicos"
                // (Y: 210)
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(StyleUtils.COLOR_SIDEBAR_SELECTED);
                g2d.fillRoundRect(0, 210, getWidth(), 60, 20, 20); // Seleccionamos la segunda opción

                g2d.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(null);

        // Estudiantes (Ahora inactivo / texto blanco)
        JLabel lblEstudiantes = new JLabel("Estudiantes");
        lblEstudiantes.setFont(StyleUtils.FUENTE_MENU);
        lblEstudiantes.setForeground(Color.WHITE); // Texto blanco
        lblEstudiantes.setBounds(90, 140, 180, 50);
        sidebar.add(lblEstudiantes);

        // Tutores Académicos (Ahora activo / texto negro)
        JLabel lblTutores = new JLabel("<html><div style='text-align: center;'>Tutores<br>Académicos</div></html>");
        lblTutores.setFont(StyleUtils.FUENTE_MENU);
        lblTutores.setForeground(Color.BLACK); // Texto negro sobre el selector
        lblTutores.setBounds(90, 215, 180, 50);
        sidebar.add(lblTutores);

        // Asesores Pedagógicos (Inactivo)
        JLabel lblAsesores = new JLabel("<html><div style='text-align: center;'>Asesores<br>Pedagógicos</div></html>");
        lblAsesores.setFont(StyleUtils.FUENTE_MENU);
        lblAsesores.setForeground(Color.WHITE);
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
}