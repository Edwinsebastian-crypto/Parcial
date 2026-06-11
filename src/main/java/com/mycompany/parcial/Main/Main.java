/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.parcial.Main;

import com.mycompany.parcial.vista.PanelEstudiante;
import com.mycompany.parcial.vista.StepPanel;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Punto de entrada principal de la aplicación.
 * <p>
 * Esta clase inicia la interfaz gráfica y muestra primero una pantalla de
 * introducción antes de cargar la ventana principal del sistema.
 */
public class Main {
    /**
     * Lanza la aplicación en el hilo de eventos de Swing.
     *
     * @param args argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        // Es buena práctica ejecutar la interfaz gráfica en el hilo de eventos de Swing
        // (EDT)
        SwingUtilities.invokeLater(() -> {
            crearYMostrarGUI();
        });
    }

    /**
     * Construye la ventana principal y muestra primero el panel de bienvenida.
     */
    private static void crearYMostrarGUI() {
        // Configurar la ventana principal
        JFrame frame = new JFrame("Plataforma UDI - STEP");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Un tamaño adecuado para que el dashboard (PanelEstudiante) se vea bien
        frame.setSize(1050, 600);
        frame.setLocationRelativeTo(null); // Centrar en la pantalla

        // 1. Mostrar primero el intro (Splash Screen)
        StepPanel panelIntro = new StepPanel();
        frame.setContentPane(panelIntro);
        frame.setVisible(true);

        // 2. Crear un Timer que se ejecute a los 4000 milisegundos (4 segundos)
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 3. Este código se ejecuta cuando pasan los 4 segundos
                PanelEstudiante panelEstudiante = new PanelEstudiante();

                // Cambiar el panel de la ventana
                frame.setContentPane(panelEstudiante);

                // Le decimos a la ventana que recalcule su diseño y se vuelva a pintar
                frame.revalidate();
                frame.repaint();
            }
        });

        // Asegurarnos de que el timer solo se dispare una vez
        timer.setRepeats(false);

        // Iniciar el conteo
        timer.start();
    }
}