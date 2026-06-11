package com.mycompany.parcial.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mycompany.parcial.modelo.Usuario;
import com.mycompany.parcial.modelo.Evidencia;
import com.mycompany.parcial.controlador.GestorEvidencias;
import java.time.LocalDate;
import java.io.File;

/**
 * Panel principal del sistema que agrupa la gestión de estudiantes, tutores y asesores.
 *
 * <p>Incluye un menú lateral de navegación, una pantalla de bienvenida y varias vistas
 * que se activan según el rol del usuario.</p>
 */
public class PanelEstudiante extends JPanel {

    private CardLayout cardLayout;
    private JPanel panelContenidoDinamico;
    private JButton btnMenuEstudiantes;
    private JButton btnMenuTutores;
    private JButton btnMenuAsesores;
    private int selectedMenuY = -1;
    private JPanel sidebarPanel;
    private Usuario usuarioActual;
    private DefaultTableModel modeloTablaEstudiante;
    private JTable tablaEstudiante;
    private PanelLogin panelLoginInstancia;
    private PanelTutor panelTutor;
    private PanelAsesor panelAsesor;

    public PanelEstudiante() {
        setLayout(new BorderLayout());
        setBackground(StyleUtils.COLOR_CONTENT_BG);

        // 1. Menú lateral izquierdo (permanece fijo siempre)
        add(crearMenuLateral(), BorderLayout.WEST);

        // 2. Contenedor de la derecha (Header + Contenido que cambia)
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Agregar Header estático ("STEP")
        panelDerecho.add(crearHeader(), BorderLayout.NORTH);

        // Inicializar el CardLayout para alternar entre las vistas
        cardLayout = new CardLayout();
        panelContenidoDinamico = new JPanel(cardLayout);
        panelContenidoDinamico.setBackground(StyleUtils.COLOR_CONTENT_BG);

        // Registrar las vistas ("cartas")
        panelContenidoDinamico.add(crearVistaResultadosTabla(), "VISTA_RESULTADOS");

        // Instanciar los paneles de Tutor y Asesor y agregar SOLO su contenido
        panelTutor = new PanelTutor();
        panelTutor.setOnSalirListener(() -> loginFallido());
        panelContenidoDinamico.add(panelTutor.getContenidoDinamico(), "VISTA_TUTOR");

        panelAsesor = new PanelAsesor();
        panelAsesor.setOnSalirListener(() -> loginFallido());
        panelContenidoDinamico.add(panelAsesor.getContenidoDinamico(), "VISTA_ASESOR");
        
        JPanel vistaVacia = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setColor(StyleUtils.COLOR_CONTENT_BG);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Logo UDI centrado
                int logoX = (getWidth() - 350) / 2;
                int logoY = (getHeight() + 80) / 2;
                dibujarLogoCentral(g2d, logoX, logoY);

                g2d.dispose();
            }
        };
        vistaVacia.setBackground(StyleUtils.COLOR_CONTENT_BG);
        panelContenidoDinamico.add(vistaVacia, "VISTA_VACIA");

        // Por defecto mostramos la vista vacía
        cardLayout.show(panelContenidoDinamico, "VISTA_VACIA");

        panelDerecho.add(panelContenidoDinamico, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.CENTER);
    }

    /**
     * Maneja el ingreso exitoso de un usuario y muestra la vista correspondiente
     * según el rol seleccionado.
     *
     * @param rolUI rol elegido por el usuario en la interfaz
     * @param usuario datos del usuario autenticado
     */
    public void loginExitoso(String rolUI, Usuario usuario) {
        this.usuarioActual = usuario;
        
        if (panelTutor != null) {
            panelTutor.setUsuarioActual(usuario);
        }
        
        if ("Estudiante".equals(rolUI)) {
            btnMenuEstudiantes.setEnabled(true);
            btnMenuTutores.setEnabled(false);
            btnMenuAsesores.setEnabled(false);
            cargarDatosTablaEstudiante(); // Refrescar la tabla para el nuevo estudiante
            btnMenuEstudiantes.doClick();
        } else if ("Tutor académico".equals(rolUI)) {
            btnMenuEstudiantes.setEnabled(false);
            btnMenuTutores.setEnabled(true);
            btnMenuAsesores.setEnabled(false);
            btnMenuTutores.doClick();
        } else if ("Asesor pedagógico".equals(rolUI)) {
            btnMenuEstudiantes.setEnabled(false);
            btnMenuTutores.setEnabled(false);
            btnMenuAsesores.setEnabled(true);
            btnMenuAsesores.doClick();
        }
    }

    /**
     * Restablece el estado de la aplicación cuando el login falla o el usuario
     * cierra sesión.
     */
    public void loginFallido() {
        this.usuarioActual = null;
        if (panelLoginInstancia != null) {
            panelLoginInstancia.limpiarFormulario();
        }
        btnMenuEstudiantes.setEnabled(false);
        btnMenuTutores.setEnabled(false);
        btnMenuAsesores.setEnabled(false);
        selectedMenuY = -1;
        actualizarColoresMenu();
        sidebarPanel.repaint();
        cardLayout.show(panelContenidoDinamico, "VISTA_VACIA");
    }

    /**
     * VISTA: Panel de Resultados con Tabla y Scroll Horizontal (Imagen 5.png)
     */
    private JPanel crearVistaResultadosTabla() {
        JPanel vista = new JPanel(new BorderLayout()); // Usa BorderLayout para expandirse
        vista.setBackground(StyleUtils.COLOR_CONTENT_BG);
        vista.setBorder(new EmptyBorder(20, 30, 20, 30)); // Márgenes para evitar tocar bordes

        // --- Configuración de la Tabla de Evidencias ---
        String[] columnas = { "Id Evidencia", "Id Estudiante", "Nombre del Estudiante", "Nombre Evidencia", "Fecha de Carga", "Descripción", "Archivo", "Estado", "Calificación", "Profesor", "Fecha calificación", "Observación" };
        modeloTablaEstudiante = new DefaultTableModel(null, columnas);
        tablaEstudiante = new JTable(modeloTablaEstudiante) {
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

        // Estilo visual plano e idéntico al mockup (Líneas negras gruesas, celdas
        // blancas amplias)
        tablaEstudiante.setRowHeight(60); // Altura de fila grande para que se vea espacioso como el dibujo
        tablaEstudiante.setShowGrid(true);
        tablaEstudiante.setGridColor(Color.BLACK);
        tablaEstudiante.setBackground(Color.WHITE);
        tablaEstudiante.setSelectionBackground(new Color(230, 240, 255));

        // Estilizar los encabezados de la tabla
        JTableHeader header = tablaEstudiante.getTableHeader();
        header.setFont(StyleUtils.FUENTE_REGULAR);
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        header.setPreferredSize(new Dimension(0, 40));

        // CRUCIAL: Desactivar auto-resizing para forzar la barra de desplazamiento
        // horizontal
        tablaEstudiante.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Definir un tamaño ancho a las columnas para obligar al scroll horizontal a
        // aparecer
        int anchoColumna = 160;
        for (int i = 0; i < tablaEstudiante.getColumnCount(); i++) {
            tablaEstudiante.getColumnModel().getColumn(i).setPreferredWidth(anchoColumna);
        }

        // Centrar celdas de la tabla
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaEstudiante.getColumnCount(); i++) {
            tablaEstudiante.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Crear el Scroll Pane contenedor de la tabla
        JScrollPane scrollPane = new JScrollPane(tablaEstudiante);
        // Forzar políticas de barras de scroll exactas
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        StyleUtils.estilizarScrollBar(scrollPane); // Aplicar estilo estético

        vista.add(scrollPane, BorderLayout.CENTER); // Centro: se expande automáticamente

        // --- FILA DE BOTONES INFERIORES ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panelBotones.setBackground(StyleUtils.COLOR_CONTENT_BG);
        panelBotones.setBorder(new EmptyBorder(10, 0, 0, 0)); // Espacio entre tabla y botones

        int altoBoton = 35;

        JButton btnCancelar = crearBotonRedondeadoDialog("Cancelar", Color.WHITE, Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(120, altoBoton));
        agregarHoverAzul(btnCancelar);
        btnCancelar.addActionListener(e -> tablaEstudiante.clearSelection());
        panelBotones.add(btnCancelar);

        JButton btnNueEvidencia = crearBotonRedondeadoDialog("Nue. Evidencia", Color.WHITE, Color.BLACK);
        btnNueEvidencia.setPreferredSize(new Dimension(140, altoBoton));
        agregarHoverAzul(btnNueEvidencia);
        btnNueEvidencia.addActionListener(e -> abrirFormularioEvidencia(null));
        panelBotones.add(btnNueEvidencia);

        JButton btnModificar = crearBotonRedondeadoDialog("Modificar", Color.WHITE, Color.BLACK);
        btnModificar.setPreferredSize(new Dimension(120, altoBoton));
        agregarHoverAzul(btnModificar);
        btnModificar.addActionListener(e -> {
            int row = tablaEstudiante.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una evidencia primero.");
                return;
            }
            String estado = (String) modeloTablaEstudiante.getValueAt(row, 7);
            if (!"Sin revisar".equals(estado)) {
                JOptionPane.showMessageDialog(this, "Solo se pueden modificar evidencias que estén 'Sin revisar'.");
                return;
            }
            String id = (String) modeloTablaEstudiante.getValueAt(row, 0);
            Evidencia ev = null;
            for (Evidencia eList : GestorEvidencias.getInstancia().getEvidencias()) {
                if (eList.getIdEvidencia().equals(id)) {
                    ev = eList;
                    break;
                }
            }
            if (ev != null) abrirFormularioEvidencia(ev);
        });
        panelBotones.add(btnModificar);

        JButton btnEliminar = crearBotonRedondeadoDialog("Eliminar", Color.WHITE, Color.BLACK);
        btnEliminar.setPreferredSize(new Dimension(120, altoBoton));
        agregarHoverAzul(btnEliminar);
        btnEliminar.addActionListener(e -> {
            int row = tablaEstudiante.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una evidencia primero.");
                return;
            }
            String estado = (String) modeloTablaEstudiante.getValueAt(row, 7);
            if (!"Sin revisar".equals(estado)) {
                JOptionPane.showMessageDialog(this, "Solo se pueden eliminar evidencias que estén 'Sin revisar'.");
                return;
            }
            int conf = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar esta evidencia?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                String id = (String) modeloTablaEstudiante.getValueAt(row, 0);
                GestorEvidencias.getInstancia().getEvidencias().removeIf(eList -> eList.getIdEvidencia().equals(id));
                cargarDatosTablaEstudiante();
                if (panelTutor != null) panelTutor.cargarDatosTablaTutor();
                if (panelAsesor != null) panelAsesor.cargarDatosTablaAsesor();
                JOptionPane.showMessageDialog(this, "Evidencia eliminada exitosamente.");
            }
        });
        panelBotones.add(btnEliminar);

        JButton btnSalir = crearBotonRedondeadoDialog("Salir", Color.WHITE, Color.BLACK);
        btnSalir.setPreferredSize(new Dimension(120, altoBoton));
        agregarHoverAzul(btnSalir);
        btnSalir.addActionListener(e -> loginFallido());
        panelBotones.add(btnSalir);

        vista.add(panelBotones, BorderLayout.SOUTH); // Abajo: botones fijos
        cargarDatosTablaEstudiante();

        return vista;
    }

    public void cargarDatosTablaEstudiante() {
        modeloTablaEstudiante.setRowCount(0);
        if (usuarioActual != null) {
            for (Evidencia ev : GestorEvidencias.getInstancia().getEvidencias()) {
                if (ev.getIdEstudiante().equals(usuarioActual.getCedula())) {
                    modeloTablaEstudiante.addRow(new Object[]{
                        ev.getIdEvidencia(),
                        ev.getIdEstudiante(),
                        ev.getNombreEstudiante(),
                        ev.getNombreEvidencia(),
                        ev.getFechaCarga(),
                        ev.getDescripcion(),
                        ev.getPathArchivo(),
                        ev.getEstado(),
                        ev.getCalificacion(),
                        ev.getProfesor(),
                        ev.getFechaCalificacion(),
                        ev.getObservacion()
                    });
                }
            }
        }
        // Rellenar filas vacías si hay menos de 6 para mantener el diseño
        while (modeloTablaEstudiante.getRowCount() < 6) {
            modeloTablaEstudiante.addRow(new Object[]{"", "", "", "", "", "", "", "", "", "", "", ""});
        }
    }

    private void abrirFormularioEvidencia(Evidencia evidenciaOriginal) {
        if (usuarioActual == null) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión primero.");
            return;
        }

        boolean esModificacion = (evidenciaOriginal != null);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), esModificacion ? "Modificar Evidencia" : "Nueva Evidencia", Dialog.ModalityType.APPLICATION_MODAL);
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

        // --- Fila 0: Nombre Evidencia ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblNombre = new JLabel("Nombre Evidencia:");
        lblNombre.setFont(labelFont);
        formPanel.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        JTextField txtNombre = new JTextField();
        txtNombre.setFont(labelFont);
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        if (esModificacion) txtNombre.setText(evidenciaOriginal.getNombreEvidencia());
        formPanel.add(txtNombre, gbc);

        // --- Fila 1: Descripción ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setFont(labelFont);
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea txtDesc = new JTextArea();
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setFont(labelFont);
        txtDesc.setMargin(new Insets(6, 8, 6, 8));
        if (esModificacion) txtDesc.setText(evidenciaOriginal.getDescripcion());
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new javax.swing.border.LineBorder(new Color(180,180,180), 1, true));
        formPanel.add(scrollDesc, gbc);

        // --- Fila 2: Archivo ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblArch = new JLabel("Archivo:");
        lblArch.setFont(labelFont);
        formPanel.add(lblArch, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel filePanel = new JPanel(new BorderLayout(8, 0));
        filePanel.setBackground(Color.WHITE);

        final String[] pathSeleccionado = { esModificacion ? evidenciaOriginal.getPathArchivo() : "" };
        String nombreArchivoInicial = esModificacion ? new File(pathSeleccionado[0]).getName() : "Ningún archivo seleccionado";
        JLabel lblArchivo = new JLabel(nombreArchivoInicial);
        lblArchivo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblArchivo.setForeground(Color.DARK_GRAY);

        JButton btnExplorar = crearBotonRedondeadoDialog("Explorar", Color.WHITE, Color.BLACK);
        agregarHoverAzul(btnExplorar);

        filePanel.add(lblArchivo, BorderLayout.CENTER);
        filePanel.add(btnExplorar, BorderLayout.EAST);
        formPanel.add(filePanel, gbc);

        btnExplorar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                pathSeleccionado[0] = f.getAbsolutePath();
                lblArchivo.setText(f.getName());
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);

        // Botón Enviar: blanco por defecto, azul al pasar el mouse
        JButton btnEnviar = crearBotonRedondeadoDialog(esModificacion ? "Guardar Cambios" : "Enviar", Color.WHITE, Color.BLACK);
        agregarHoverAzul(btnEnviar);
        
        btnEnviar.addActionListener(e -> {
            if (txtNombre.getText().trim().isEmpty() || pathSeleccionado[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe llenar el nombre y seleccionar un archivo.");
                return;
            }
            
            Evidencia ev = esModificacion ? evidenciaOriginal : new Evidencia();
            
            if (!esModificacion) {
                ev.setIdEstudiante(usuarioActual.getCedula());
                ev.setNombreEstudiante(usuarioActual.getNombre() + " " + usuarioActual.getApellido());
                ev.setFechaCarga(LocalDate.now().toString());
                ev.setEstado("Sin revisar");
                ev.setCalificacion("");
                ev.setProfesor("");
                ev.setFechaCalificacion("");
                ev.setObservacion("");
            }
            
            ev.setNombreEvidencia(txtNombre.getText().trim());
            ev.setDescripcion(txtDesc.getText().trim());
            ev.setPathArchivo(pathSeleccionado[0]);

            if (!esModificacion) {
                GestorEvidencias.getInstancia().agregarEvidencia(ev);
                JOptionPane.showMessageDialog(dialog, "Evidencia guardada exitosamente con ID: " + ev.getIdEvidencia());
            } else {
                JOptionPane.showMessageDialog(dialog, "Evidencia modificada exitosamente.");
            }
            
            cargarDatosTablaEstudiante();
            
            // Refrescar también a los demás si estuvieran creados
            if (panelTutor != null) panelTutor.cargarDatosTablaTutor();
            if (panelAsesor != null) panelAsesor.cargarDatosTablaAsesor();

            dialog.dispose();
        });
        btnPanel.add(btnEnviar);

        JButton btnCancelar = crearBotonRedondeadoDialog("Cancelar", Color.WHITE, Color.BLACK);
        agregarHoverAzul(btnCancelar);
        btnCancelar.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCancelar);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Método auxiliar para crear botones limpios con bordes negros planos de manera
    // rápida
    private JButton crearBotónEstiloPlano(String texto, int ancho, int alto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setBackground(Color.WHITE);
        boton.setFont(StyleUtils.FUENTE_REGULAR);
        boton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        boton.setFocusPainted(false);
        return boton;
    }

    // Botón redondeado estético para los diálogos (Nueva Evidencia / Modificar)
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
        boton.setPreferredSize(new Dimension(140, 34));
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
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setColor(StyleUtils.COLOR_SIDEBAR_BG);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(StyleUtils.COLOR_SIDEBAR_TOP);
                g2d.fillRect(0, 0, getWidth(), 120);

                dibujarLogoSidebar(g2d, 15, 75);

                if (selectedMenuY != -1) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(StyleUtils.COLOR_SIDEBAR_SELECTED);
                    g2d.fillRoundRect(15, selectedMenuY, getWidth() - 30, 80, 20, 20);
                }

                g2d.dispose();
            }
        };
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setLayout(null);

        // Alineación perfecta con anchos definidos y un padding izquierdo en crearBotonMenu
        btnMenuEstudiantes = crearBotonMenu("", 280, "VISTA_RESULTADOS");
        btnMenuTutores = crearBotonMenu("", 360, "VISTA_TUTOR");
        btnMenuAsesores = crearBotonMenu("", 440, "VISTA_ASESOR");

        // Deshabilitados hasta que ingresen usuario y rol
        btnMenuEstudiantes.setEnabled(false);
        btnMenuTutores.setEnabled(false);
        btnMenuAsesores.setEnabled(false);

        sidebarPanel.add(btnMenuEstudiantes);
        sidebarPanel.add(btnMenuTutores);
        sidebarPanel.add(btnMenuAsesores);

        actualizarColoresMenu(); // Inicializa los textos de los botones

        // --- MINI LOGIN (Usuario y Rol) encapsulado en PanelLogin ---
        panelLoginInstancia = new PanelLogin(this);
        panelLoginInstancia.setBounds(15, 130, 220, 140);
        sidebarPanel.add(panelLoginInstancia);

        // Botón CERRAR con la misma estética que los demás ítems del menú
        JButton btnCerrar = new JButton();
        btnCerrar.setText("<html><table width='200' cellpadding='0'><tr><td width='35' valign='top' style='font-size:18px; text-align:left;'>🔒</td><td style='font-size:14px; color:white;'>Cerrar / Salir</td></tr></table></html>");
        btnCerrar.setBounds(0, 520, 250, 60);
        btnCerrar.setFont(StyleUtils.FUENTE_MENU);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> System.exit(0)); 
        sidebarPanel.add(btnCerrar);

        return sidebarPanel;
    }

    private void actualizarColoresMenu() {
        if(btnMenuEstudiantes != null) {
            btnMenuEstudiantes.setForeground(Color.WHITE);
            String txt = "<html><table width='200' cellpadding='0'><tr><td width='35' valign='top' style='font-size:18px; text-align:left;'>🎓</td><td style='font-size:14px; color:white;'>Estudiantes" 
                       + (selectedMenuY == 280 ? "<br><span style='font-size:11px; color:#cccccc;'>├─ Gestión de Evidencias</span>" : "") 
                       + "</td></tr></table></html>";
            btnMenuEstudiantes.setText(txt);
        }
        if(btnMenuTutores != null) {
            btnMenuTutores.setForeground(Color.WHITE);
            String txt = "<html><table width='200' cellpadding='0'><tr><td width='35' valign='top' style='font-size:18px; text-align:left;'>👨‍🏫</td><td style='font-size:14px; color:white;'>Tutores Académicos" 
                       + (selectedMenuY == 360 ? "<br><span style='font-size:11px; color:#cccccc;'>├─ Revisar Evidencias</span>" : "") 
                       + "</td></tr></table></html>";
            btnMenuTutores.setText(txt);
        }
        if(btnMenuAsesores != null) {
            btnMenuAsesores.setForeground(Color.WHITE);
            String txt = "<html><table width='200' cellpadding='0'><tr><td width='35' valign='top' style='font-size:18px; text-align:left;'>📖</td><td style='font-size:14px; color:white;'>Asesores Pedagógicos" 
                       + (selectedMenuY == 440 ? "<br><span style='font-size:11px; color:#cccccc;'>├─ Observaciones</span>" : "") 
                       + "</td></tr></table></html>";
            btnMenuAsesores.setText(txt);
        }
    }

    private JButton crearBotonMenu(String texto, int y, String vistaDestino) {
        JButton btn = new JButton(texto);
        btn.setBounds(0, y, 250, 80);
        btn.setFont(StyleUtils.FUENTE_MENU);
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0)); // Padding izquierdo de 30px
        btn.setContentAreaFilled(false);
        // btn.setBorderPainted(false); // Eliminado porque estamos usando EmptyBorder arriba
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            selectedMenuY = y;
            actualizarColoresMenu();
            sidebarPanel.repaint();
            
            // Resetear subpaneles a su tabla principal antes de mostrar y recargar datos
            if ("VISTA_TUTOR".equals(vistaDestino) && panelTutor != null) {
                panelTutor.cargarDatosTablaTutor();
                panelTutor.resetToTabla();
            } else if ("VISTA_ASESOR".equals(vistaDestino) && panelAsesor != null) {
                panelAsesor.cargarDatosTablaAsesor();
                panelAsesor.resetToTabla();
            } else if ("VISTA_RESULTADOS".equals(vistaDestino)) {
                cargarDatosTablaEstudiante();
            }
            
            cardLayout.show(panelContenidoDinamico, vistaDestino);
        });
        return btn;
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

    private void dibujarLogoCentral(Graphics2D g2d, int x, int y) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color darkBlue = StyleUtils.COLOR_SIDEBAR_BG;
        
        g2d.setColor(darkBlue);
        Font fontLarge = StyleUtils.FUENTE_UDI_LARGE.deriveFont(80f);
        g2d.setFont(fontLarge);
        g2d.drawString("UD", x, y);

        FontMetrics metricsUD = g2d.getFontMetrics(fontLarge);
        int udWidth = metricsUD.stringWidth("UD");
        int udiHeight = metricsUD.getAscent();
        int logoTop = y - udiHeight;

        int xI = x + udWidth + 5;
        int squareSize = 25;
        int xSquare = xI;
        int ySquare = logoTop + 10;

        g2d.setColor(darkBlue);
        g2d.fillRect(xSquare, ySquare, squareSize, squareSize);

        g2d.setColor(StyleUtils.COLOR_UDI_SQUARE_YELLOW);
        int[] xPolPoints = { xSquare, xSquare + squareSize, xSquare + squareSize };
        int[] yPolPoints = { ySquare, ySquare, ySquare + squareSize };
        g2d.fillPolygon(xPolPoints, yPolPoints, 3);

        int yStem = ySquare + squareSize + 8;
        int stemHeight = y - yStem;
        g2d.setColor(darkBlue);
        g2d.fillRect(xI, yStem, squareSize, stemHeight);

        g2d.setColor(darkBlue);
        Font fontDesc = StyleUtils.FUENTE_UDI_DESCRIPTIVE.deriveFont(20f);
        g2d.setFont(fontDesc);
        FontMetrics metricsText = g2d.getFontMetrics(fontDesc);

        int xText = xI + squareSize + 25;
        int lineHeight = metricsText.getHeight() - 2;
        int yStartText = logoTop + metricsText.getAscent() + 10;

        g2d.drawString(StyleUtils.TEXTO_UNIVERSIDAD, xText, yStartText);
        g2d.drawString(StyleUtils.TEXTO_INVESTIGACION, xText, yStartText + lineHeight);
        g2d.drawString(StyleUtils.TEXTO_DESARROLLO, xText, yStartText + (2 * lineHeight));
    }
}