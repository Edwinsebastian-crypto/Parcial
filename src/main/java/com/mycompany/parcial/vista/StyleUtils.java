package com.mycompany.parcial.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Utilidades de estilo compartidas para la aplicación.
 *
 * <p>Contiene colores, fuentes y métodos de estilo que garantizan una apariencia
 * consistente en todos los paneles.</p>
 */
public class StyleUtils {
    // --- COLORES DEL LOGO Y PRESENTACIÓN INICIAL ---
    public static final Color COLOR_GRADIENTE_TOP = new Color(10, 20, 50);
    public static final Color COLOR_GRADIENTE_BOTTOM = new Color(30, 50, 100);
    public static final Color COLOR_UDI_TEXT = new Color(20, 100, 230);
    public static final Color COLOR_UDI_SQUARE_YELLOW = new Color(250, 220, 120);
    public static final Color COLOR_UDI_DESCRIPTIVE_TEXT = Color.WHITE;
    public static final Color COLOR_TEXTO_STEP = Color.BLACK;

    // --- NUEVOS COLORES PARA EL PANEL ESTUDIANTE (DASHBOARD) ---
    public static final Color COLOR_SIDEBAR_TOP = new Color(14, 32, 76); // Azul muy oscuro (fondo del logo)
    public static final Color COLOR_SIDEBAR_BG = new Color(42, 105, 228); // Azul brillante (fondo del menú)
    public static final Color COLOR_SIDEBAR_SELECTED = new Color(28, 85, 190); // Azul brillante (ítem seleccionado)

    public static final Color COLOR_CONTENT_BG = new Color(250, 250, 250); // Gris muy claro (fondo principal)
    public static final Color COLOR_INNER_PANEL = new Color(240, 240, 240); // Gris claro (panel central)
    public static final Color COLOR_BUTTON_BLUE = new Color(210, 225, 245); // Azul clarito (botón nueva evidencia)
    public static final Color COLOR_BORDER = new Color(200, 200, 200); // Gris para bordes y líneas

    // NUEVO: Color para el resaltado del dropdown de estado en el formulario del
    // Tutor
    public static final Color COLOR_COMBO_HIGHLIGHT = new Color(220, 240, 255);

    // --- FUENTES ---
    public static final Font FUENTE_UDI_LARGE = new Font("SansSerif", Font.BOLD, 45); // Un poco más pequeño para el
                                                                                      // sidebar

    // NUEVA: Fuente gigante solo para la pantalla de carga (intro)
    public static final Font FUENTE_STEP_INTRO = new Font("Serif", Font.BOLD, 150); // <-- Modifica este número para
                                                                                    // cambiar el tamaño

    public static final Font FUENTE_UDI_DESCRIPTIVE = new Font("SansSerif", Font.BOLD, 10);
    public static final Font FUENTE_PRINCIPAL_STEP = new Font("Serif", Font.BOLD, 30); // Para el header

    public static final Font FUENTE_MENU = new Font("SansSerif", Font.PLAIN, 18);
    public static final Font FUENTE_REGULAR = new Font("SansSerif", Font.PLAIN, 14);

    // --- TEXTOS ---
    public static final String TEXTO_UNIVERSIDAD = "UNIVERSIDAD";
    public static final String TEXTO_INVESTIGACION = "DE INVESTIGACIÓN";
    public static final String TEXTO_DESARROLLO = "Y DESARROLLO";
    public static final String TEXTO_STEP = "STEP";

    // NUEVOS: Textos para el formulario de Path del Tutor
    public static final String TITULO_FORM_PATH = "Path archivo";
    public static final String LABEL_FORM_ID_ESTUDIANTE = "Id Estudiante:";
    public static final String LABEL_FORM_NOMBRES = "Nombres y apellidos estudiante:";
    public static final String LABEL_FORM_PATH = "Path al archivo:";
    public static final String TEXTO_BOTON_REVISAR = "Revisar";
    public static final String LABEL_FORM_ESTADO = "Estado:";
    public static final String LABEL_FORM_CALIFICACION = "Calificación:";
    public static final String TEXTO_BOTON_ACEPTAR = "Aceptar";
    public static final String TEXTO_COMBO_REVISADO = "Revisado";
    public static final String TEXTO_COMBO_SIN_REVISAR = "Sin revisar";

    public static GradientPaint obtenerGradienteFondo(int height) {
        return new GradientPaint(0, 0, COLOR_GRADIENTE_TOP, 0, height, COLOR_GRADIENTE_BOTTOM);
    }

    public static void estilizarScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
    }

    public static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(180, 190, 200); // Gris azulado estético
            this.trackColor = new Color(245, 245, 245); // Fondo casi blanco
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            Dimension zeroDim = new Dimension(0, 0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }
}