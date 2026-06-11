package com.mycompany.parcial.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.CubicCurve2D;

public class StepPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        // Anti-Aliasing para bordes perfectos sin pixeles duros
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int ancho = getWidth();
        int alto = getHeight();

        if (ancho <= 0 || alto <= 0) {
            g2d.dispose();
            return;
        }

        // Fondo degradado
        g2d.setPaint(StyleUtils.obtenerGradienteFondo(alto));
        g2d.fillRect(0, 0, ancho, alto);

        // Malla de ondas de fondo
        dibujarMallaDecorativa(g2d, ancho, alto);

        // DIBUJO DEL LOGO CORREGIDO (Ensamblado geométrico de la UDi)
        dibujarLogoProgramatico(g2d, 35, 85);

        // Texto "STEP" central
        dibujarTextoCentral(g2d, ancho, alto);

        g2d.dispose();
    }

    /**
     * Construye de manera vectorial y exacta el logo "UDi".
     */
    private void dibujarLogoProgramatico(Graphics2D g2d, int x, int y) {
        // 1. Dibujar el bloque principal "UD"
        g2d.setColor(StyleUtils.COLOR_UDI_TEXT);
        g2d.setFont(StyleUtils.FUENTE_UDI_LARGE);
        g2d.drawString("UD", x, y);

        // Obtener las métricas del texto "UD" para calcular dónde empieza la "i"
        FontMetrics metricsUD = g2d.getFontMetrics(StyleUtils.FUENTE_UDI_LARGE);
        int udWidth = metricsUD.stringWidth("UD");
        int udiHeight = metricsUD.getAscent(); // Altura real desde la línea base al techo
        int logoTop = y - udiHeight; // Coordenada Y del borde superior del logo

        // 2. Construir la "i" minúscula integrada
        int gapEntreLetras = 4; // Pequeño espacio horizontal después de la 'D'
        int xI = x + udWidth + gapEntreLetras;

        int squareSize = 14; // Grosor del bastón y tamaño del cuadrado del punto

        // --- Dibujar el PUNTO de la "i" (Cuadrado Bicolor) en el techo del logo ---
        int xSquare = xI;
        int ySquare = logoTop + 2; // Alineado estéticamente con el borde superior de la U y D

        // Fondo base azul del cuadrado
        g2d.setColor(StyleUtils.COLOR_UDI_TEXT);
        g2d.fillRect(xSquare, ySquare, squareSize, squareSize);

        // Triángulo superior derecho amarillo
        g2d.setColor(StyleUtils.COLOR_UDI_SQUARE_YELLOW);
        int[] xPolPoints = { xSquare, xSquare + squareSize, xSquare + squareSize };
        int[] yPolPoints = { ySquare, ySquare, ySquare + squareSize };
        g2d.fillPolygon(xPolPoints, yPolPoints, 3);

        // --- Dibujar el BASTÓN de la "i" (Rectángulo Azul) ---
        int gapPuntoBastron = 4; // Separación exacta entre el punto y el cuerpo de la letra
        int yStem = ySquare + squareSize + gapPuntoBastron;
        int stemHeight = y - yStem; // El bastón baja exactamente hasta la línea base de la UD

        g2d.setColor(StyleUtils.COLOR_UDI_TEXT);
        g2d.fillRect(xI, yStem, squareSize, stemHeight);

        // 3. Dibujar el bloque de texto de la universidad a la derecha de la "i"
        g2d.setColor(StyleUtils.COLOR_UDI_DESCRIPTIVE_TEXT);
        g2d.setFont(StyleUtils.FUENTE_UDI_DESCRIPTIVE);
        FontMetrics metricsText = g2d.getFontMetrics(StyleUtils.FUENTE_UDI_DESCRIPTIVE);

        int gapTextoHorizontal = 14; // Espacio entre la "i" y el inicio del texto descriptivo
        int xText = xI + squareSize + gapTextoHorizontal;

        // Interlineado ajustado y posicionamiento inicial vertical
        int lineHeight = metricsText.getHeight() - 1;
        int yStartText = logoTop + metricsText.getAscent() + 1;

        // Renderizar las 3 líneas apiladas de forma exacta
        g2d.drawString(StyleUtils.TEXTO_UNIVERSIDAD, xText, yStartText);
        g2d.drawString(StyleUtils.TEXTO_INVESTIGACION, xText, yStartText + lineHeight);
        g2d.drawString(StyleUtils.TEXTO_DESARROLLO, xText, yStartText + (2 * lineHeight));
    }

    private void dibujarTextoCentral(Graphics2D g2d, int ancho, int alto) {
        // 1. Usa la nueva fuente gigante
        g2d.setFont(StyleUtils.FUENTE_STEP_INTRO);
        g2d.setColor(StyleUtils.COLOR_TEXTO_STEP);

        // 2. Calcula las métricas basándose en la fuente gigante para centrarlo
        // perfecto
        FontMetrics metrics = g2d.getFontMetrics(StyleUtils.FUENTE_STEP_INTRO);
        int xTexto = (ancho - metrics.stringWidth(StyleUtils.TEXTO_STEP)) / 2;
        int yTexto = ((alto - metrics.getHeight()) / 2) + metrics.getAscent();

        g2d.drawString(StyleUtils.TEXTO_STEP, xTexto, yTexto);
    }

    private void dibujarMallaDecorativa(Graphics2D g2d, int ancho, int alto) {
        g2d.setColor(new Color(255, 255, 255, 12));
        g2d.setStroke(new BasicStroke(0.6f));

        for (int i = 0; i < 18; i++) {
            CubicCurve2D curva1 = new CubicCurve2D.Double();
            curva1.setCurve(ancho - 280 + (i * 12), -50, ancho - 80 - (i * 10), alto * 0.35, ancho + 40,
                    alto * 0.65 + (i * 5), ancho - 120 + (i * 8), alto + 50);
            g2d.draw(curva1);

            CubicCurve2D curva2 = new CubicCurve2D.Double();
            curva2.setCurve(ancho - 40 + (i * 8), -50, ancho - 250, alto * 0.18 + (i * 8), ancho - 80 - (i * 18),
                    alto * 0.82, ancho + 70, alto + 50);
            g2d.draw(curva2);
        }
    }
}