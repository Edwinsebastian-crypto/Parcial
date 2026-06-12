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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import com.mycompany.parcial.modelo.Evidencia;
import com.mycompany.parcial.modelo.Entrega;
import com.mycompany.parcial.modelo.Usuario;
import com.mycompany.parcial.controlador.GestorEvidencias;

/**
 * Panel dedicado a los tutores académicos.
 *
 * <p>Contiene una lista de evidencias cargadas y un formulario de revisión que
 * permite calificar y actualizar el estado de cada evidencia.</p>
 */
public class PanelTutor extends JPanel {

    // Variables para manejar la navegación entre la tabla y el formulario
    private CardLayout cardLayout;
    private JPanel panelContenidoDinamico;
    private PathFormPanel panelFormularioPath; // Asegúrate de tener creada esta clase
    private DefaultTableModel modeloTablaTutor;
    private JTable tablaTutor;
    private DefaultTableModel modeloTablaCalEntregas;
    private JTable tablaCalEntregas;
    private Usuario usuarioActual;
    private static final String VISTA_OPCIONES_TUTOR = "VISTA_OPCIONES_TUTOR";
    private static final String VISTA_TABLA = "VISTA_TABLA";
    private static final String VISTA_CAL_ENTREGAS = "VISTA_CAL_ENTREGAS";
    private static final String VISTA_FORMULARIO = "VISTA_FORMULARIO";
    
    // Callback para el botón Salir
    private Runnable onSalirListener;

    /**
     * Registra el callback que se ejecuta al cerrar sesión o salir del panel.
     *
     * @param listener acción a ejecutar cuando se presiona Salir
     */
    public void setOnSalirListener(Runnable listener) {
        this.onSalirListener = listener;
    }

    /**
     * Asigna el usuario actual que está utilizando el panel de tutor.
     *
     * @param u usuario autenticado
     */
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

        // Vista 1: opciones principales del tutor
        panelContenidoDinamico.add(crearVistaOpcionesTutor(), VISTA_OPCIONES_TUTOR);

        // Vista 2: tabla de revisiÃ³n de evidencias
        panelContenidoDinamico.add(crearContenidoCentral(), VISTA_TABLA);

        // Vista 3: mÃ³dulo Cal. Entregas
        panelContenidoDinamico.add(crearVistaCalEntregas(), VISTA_CAL_ENTREGAS);

        // Vista 4: El formulario
        panelFormularioPath = new PathFormPanel();
        panelContenidoDinamico.add(panelFormularioPath, VISTA_FORMULARIO);

        // Listener para regresar del formulario a la tabla (Cancelar)
        panelFormularioPath.addCancelarListener(e -> cardLayout.show(panelContenidoDinamico, VISTA_TABLA));

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
                    cardLayout.show(panelContenidoDinamico, VISTA_TABLA);
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
        cardLayout.show(panelContenidoDinamico, VISTA_OPCIONES_TUTOR);
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

        tablaTutor.setFillsViewportHeight(true);
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
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(StyleUtils.COLOR_CONTENT_BG);

        JButton btnRegresar = crearBotonRedondeado("Regresar", Color.WHITE, Color.BLACK);
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        agregarHoverAzul(btnRegresar);
        btnRegresar.addActionListener(e -> cardLayout.show(panelContenidoDinamico, VISTA_OPCIONES_TUTOR));
        panelBotones.add(btnRegresar);

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

    private JPanel crearVistaOpcionesTutor() {
        JPanel vista = new JPanel(new GridBagLayout());
        vista.setBackground(StyleUtils.COLOR_CONTENT_BG);
        vista.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel panelOpciones = new JPanel(new GridLayout(1, 2, 35, 0));
        panelOpciones.setOpaque(false);

        JButton btnRevisar = crearBotonRedondeado("Revisar Evidencias", Color.WHITE, Color.BLACK);
        btnRevisar.setPreferredSize(new Dimension(220, 58));
        btnRevisar.setFont(StyleUtils.FUENTE_MENU);
        agregarHoverAzul(btnRevisar);
        btnRevisar.addActionListener(e -> {
            cargarDatosTablaTutor();
            cardLayout.show(panelContenidoDinamico, VISTA_TABLA);
        });

        JButton btnCalEntregas = crearBotonRedondeado("Cal. Entregas", Color.WHITE, Color.BLACK);
        btnCalEntregas.setPreferredSize(new Dimension(220, 58));
        btnCalEntregas.setFont(StyleUtils.FUENTE_MENU);
        agregarHoverAzul(btnCalEntregas);
        btnCalEntregas.addActionListener(e -> {
            cargarDatosTablaCalEntregas();
            cardLayout.show(panelContenidoDinamico, VISTA_CAL_ENTREGAS);
        });

        panelOpciones.add(btnRevisar);
        panelOpciones.add(btnCalEntregas);
        vista.add(panelOpciones);
        return vista;
    }

    private JPanel crearVistaCalEntregas() {
        JPanel vista = new JPanel(new BorderLayout());
        vista.setBackground(StyleUtils.COLOR_CONTENT_BG);
        vista.setBorder(new EmptyBorder(30, 30, 12, 30));

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(new Color(239, 239, 239));
        panelCentro.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columnas = {"ID", "Actividad", "Fecha límite", "Descripción"};
        modeloTablaCalEntregas = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        tablaCalEntregas = new JTable(modeloTablaCalEntregas) {
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
        tablaCalEntregas.setFillsViewportHeight(true);
        tablaCalEntregas.setRowHeight(44);
        tablaCalEntregas.setFont(StyleUtils.FUENTE_REGULAR);
        tablaCalEntregas.setShowGrid(true);
        tablaCalEntregas.setGridColor(Color.BLACK);
        tablaCalEntregas.setBackground(Color.WHITE);
        tablaCalEntregas.setSelectionBackground(new Color(224, 239, 255));

        JTableHeader header = tablaCalEntregas.getTableHeader();
        header.setFont(StyleUtils.FUENTE_REGULAR);
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        header.setPreferredSize(new Dimension(0, 32));

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaCalEntregas.getColumnCount(); i++) {
            tablaCalEntregas.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        tablaCalEntregas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaCalEntregas.getColumnModel().getColumn(1).setPreferredWidth(140);
        tablaCalEntregas.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaCalEntregas.getColumnModel().getColumn(3).setPreferredWidth(190);
        tablaCalEntregas.getColumnModel().getColumn(1).setCellRenderer(new CalArchivoRenderer());
        tablaCalEntregas.getColumnModel().getColumn(1).setCellEditor(new CalArchivoEditor());

        JScrollPane scrollPane = new JScrollPane(tablaCalEntregas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelCentro.add(scrollPane, gbc);

        vista.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        panelBotones.setBackground(StyleUtils.COLOR_CONTENT_BG);
        panelBotones.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnRegresar = crearBotonRedondeado("Regresar", Color.WHITE, Color.BLACK);
        btnRegresar.setPreferredSize(new Dimension(118, 34));
        agregarHoverAzul(btnRegresar);
        btnRegresar.addActionListener(e -> cardLayout.show(panelContenidoDinamico, VISTA_OPCIONES_TUTOR));
        panelBotones.add(btnRegresar);

        JButton btnEditar = crearBotonRedondeado("Editar", Color.WHITE, Color.BLACK);
        btnEditar.setPreferredSize(new Dimension(118, 34));
        agregarHoverAzul(btnEditar);
        btnEditar.addActionListener(e -> editarEntregaSeleccionada());
        panelBotones.add(btnEditar);

        JButton btnEliminar = crearBotonRedondeado("Eliminar", Color.WHITE, Color.BLACK);
        btnEliminar.setPreferredSize(new Dimension(118, 34));
        agregarHoverAzul(btnEliminar);
        btnEliminar.addActionListener(e -> eliminarEntregaSeleccionada());
        panelBotones.add(btnEliminar);

        JButton btnNueva = crearBotonRedondeado("Nueva Entrega", Color.WHITE, Color.BLACK);
        btnNueva.setPreferredSize(new Dimension(130, 34));
        agregarHoverAzul(btnNueva);
        btnNueva.addActionListener(e -> abrirDialogoEntrega(-1));
        panelBotones.add(btnNueva);

        JButton btnSalir = crearBotonRedondeado("Salir", Color.WHITE, Color.BLACK);
        btnSalir.setPreferredSize(new Dimension(118, 34));
        agregarHoverAzul(btnSalir);
        btnSalir.addActionListener(e -> {
            if (onSalirListener != null) {
                onSalirListener.run();
            }
        });
        panelBotones.add(btnSalir);

        vista.add(panelBotones, BorderLayout.SOUTH);
        cargarDatosTablaCalEntregas();
        return vista;
    }

    private void editarEntregaSeleccionada() {
        int fila = tablaCalEntregas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una entrega primero.");
            return;
        }
        abrirDialogoEntrega(tablaCalEntregas.convertRowIndexToModel(fila));
    }

    private void eliminarEntregaSeleccionada() {
        int fila = tablaCalEntregas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una entrega primero.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Desea eliminar esta entrega?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            String id = modeloTablaCalEntregas.getValueAt(tablaCalEntregas.convertRowIndexToModel(fila), 0).toString();
            GestorEvidencias.getInstancia().getEntregas().removeIf(entrega -> entrega.getId().equals(id));
            cargarDatosTablaCalEntregas();
        }
    }

    private void abrirDialogoEntrega(int filaModelo) {
        boolean esEdicion = filaModelo >= 0;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar Entrega" : "Nueva Entrega", Dialog.ModalityType.APPLICATION_MODAL);
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

        Date fechaInicial = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        if (esEdicion) {
            String fechaActual = modeloTablaCalEntregas.getValueAt(filaModelo, 2).toString();
            try {
                fechaInicial = formato.parse(fechaActual);
            } catch (Exception ex) {}
        }
        
        JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel(fechaInicial, null, null, java.util.Calendar.DAY_OF_MONTH));
        spinnerFecha.setEditor(new JSpinner.DateEditor(spinnerFecha, "dd/MM/yyyy"));
        spinnerFecha.setFont(labelFont);
        spinnerFecha.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JTextArea txtDescripcion = new JTextArea(esEdicion ? modeloTablaCalEntregas.getValueAt(filaModelo, 3).toString() : "");
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(labelFont);
        txtDescripcion.setMargin(new Insets(6, 8, 6, 8));
        
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setBorder(new javax.swing.border.LineBorder(new Color(180,180,180), 1, true));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblFecha = new JLabel("Fecha límite:");
        lblFecha.setFont(labelFont);
        formPanel.add(lblFecha, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        formPanel.add(spinnerFecha, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setFont(labelFont);
        formPanel.add(lblDesc, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollDesc, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botones.setBackground(Color.WHITE);
        
        JButton btnGuardar = crearBotonRedondeado(esEdicion ? "Guardar Cambios" : "Guardar", Color.WHITE, Color.BLACK);
        btnGuardar.setPreferredSize(new Dimension(140, 34));
        agregarHoverAzul(btnGuardar);
        btnGuardar.addActionListener(e -> {
            String fecha = formato.format((Date) spinnerFecha.getValue());
            String descripcion = txtDescripcion.getText().trim();
            if (fecha.isEmpty() || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe ingresar fecha y descripción.");
                return;
            }
            try {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                java.time.LocalDate fechaSeleccionada = java.time.LocalDate.parse(fecha, dtf);
                if (fechaSeleccionada.isBefore(java.time.LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "La fecha límite debe ser mayor o igual al día de hoy.");
                    return;
                }
            } catch (Exception ex) {}
            if (esEdicion) {
                String id = modeloTablaCalEntregas.getValueAt(filaModelo, 0).toString();
                Entrega entrega = buscarEntregaPorId(id);
                if (entrega != null) {
                    entrega.setFechaLimite(fecha);
                    entrega.setDescripcion(descripcion);
                }
            } else {
                GestorEvidencias.getInstancia().agregarEntrega(new Entrega("", fecha, descripcion));
            }
            cargarDatosTablaCalEntregas();
            dialog.dispose();
        });
        botones.add(btnGuardar);

        JButton btnCancelar = crearBotonRedondeado("Cancelar", Color.WHITE, Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(140, 34));
        agregarHoverAzul(btnCancelar);
        btnCancelar.addActionListener(e -> dialog.dispose());
        botones.add(btnCancelar);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(botones, BorderLayout.SOUTH);
        dialog.setVisible(true);
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

    public void cargarDatosTablaCalEntregas() {
        if (modeloTablaCalEntregas == null) {
            return;
        }
        modeloTablaCalEntregas.setRowCount(0);
        for (Entrega entrega : GestorEvidencias.getInstancia().getEntregas()) {
            modeloTablaCalEntregas.addRow(new Object[] {
                    entrega.getId(),
                    "Abrir Archivo",
                    entrega.getFechaLimite(),
                    entrega.getDescripcion()
            });
        }
    }

    private Entrega buscarEntregaPorId(String id) {
        for (Entrega entrega : GestorEvidencias.getInstancia().getEntregas()) {
            if (entrega.getId().equals(id)) {
                return entrega;
            }
        }
        return null;
    }

    private class CalArchivoRenderer extends JButton implements TableCellRenderer {
        CalArchivoRenderer() {
            setText("Abrir Archivo");
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setFont(StyleUtils.FUENTE_REGULAR);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setFocusPainted(false);
            setContentAreaFilled(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText(value == null ? "Abrir Archivo" : value.toString());
            return this;
        }
    }

    private class CalArchivoEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton boton;
        private int fila;

        CalArchivoEditor() {
            boton = new JButton("Abrir Archivo");
            boton.setBackground(Color.WHITE);
            boton.setForeground(Color.BLACK);
            boton.setFont(StyleUtils.FUENTE_REGULAR);
            boton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            boton.setFocusPainted(false);
            boton.addActionListener(e -> {
                fireEditingStopped();
                descargarArchivoEntrega(fila);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.fila = table.convertRowIndexToModel(row);
            boton.setText(value == null ? "Abrir Archivo" : value.toString());
            return boton;
        }

        @Override
        public Object getCellEditorValue() {
            return "Abrir Archivo";
        }
    }



    private void descargarArchivoEntrega(int filaModelo) {
        String idEntrega = modeloTablaCalEntregas.getValueAt(filaModelo, 0).toString();
        Evidencia evidencia = buscarEvidenciaPorEntrega(idEntrega);
        if (evidencia == null || evidencia.getPathArchivo() == null || evidencia.getPathArchivo().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aún no hay archivo cargado por el estudiante para esta entrega.");
            return;
        }

        File archivoOrigen = new File(evidencia.getPathArchivo());
        if (!archivoOrigen.exists()) {
            JOptionPane.showMessageDialog(this, "No se encontró el archivo original: " + evidencia.getPathArchivo());
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccione carpeta para descargar la actividad");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = new File(chooser.getSelectedFile(), archivoOrigen.getName());
        try {
            Files.copy(archivoOrigen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(destino);
            }
            JOptionPane.showMessageDialog(this, "Archivo descargado en: " + destino.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo descargar el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Evidencia buscarEvidenciaPorEntrega(String idEntrega) {
        String nombreEsperado = "Cal. Evidencia " + idEntrega;
        for (Evidencia ev : GestorEvidencias.getInstancia().getEvidencias()) {
            if (nombreEsperado.equalsIgnoreCase(ev.getNombreEvidencia())) {
                return ev;
            }
        }
        return null;
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
                                        cardLayout.show(panelContenidoDinamico, VISTA_FORMULARIO);
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
