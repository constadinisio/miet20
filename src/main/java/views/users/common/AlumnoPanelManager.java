/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import main.java.views.users.Alumnos.verasisten;
import main.java.views.users.Alumnos.vernotas;
import main.java.views.users.common.NotasVisualizationPanel;

/**
 * Gestor de paneles específico para el rol de Alumno.
 */
public class AlumnoPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int alumnoId;

    /**
     * Constructor del gestor de paneles para alumnos.
     *
     * @param ventana Ventana principal
     * @param userId ID del usuario (alumno)
     */
    public AlumnoPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.alumnoId = userId;
    }

    @Override
    public JComponent[] createButtons() {
        // Crear botones específicos para el rol de alumno
        JButton btnNotas = createStyledButton("NOTAS", "notas");
        JButton btnAsistencias = createStyledButton("ASISTENCIAS", "asistencias");
        JButton btnMisBoletines = createStyledButton("MIS BOLETINES", "misBoletines"); // NUEVO

        // Retornar array de botones
        return new JComponent[]{
            btnNotas,
            btnAsistencias,
            btnMisBoletines // NUEVO
        };
    }

    /**
     * Muestra el panel de notas del alumno.
     */
    private void mostrarNotasAlumno() {
        try {
            System.out.println("Cargando panel de notas para alumno ID: " + alumnoId);

            // Crear el panel de visualización de notas
            main.java.views.users.common.NotasVisualizationPanel panelNotas
                    = new main.java.views.users.common.NotasVisualizationPanel(ventana, alumnoId, 4); // rol 4 = alumno

            // Obtener panel principal y configurarlo
            javax.swing.JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new java.awt.BorderLayout());
            panelPrincipal.add(panelNotas, java.awt.BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

            System.out.println("Panel de notas del alumno cargado exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al cargar panel de notas: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar notas: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // En caso de error, restaurar vista principal
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Crea un botón con el estilo estándar de la aplicación.
     *
     * @param text Texto del botón
     * @param actionCommand Comando de acción para identificar el botón
     * @return Botón configurado
     */
    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        // Establecer dimensiones preferidas para mantener consistencia
        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        try {
            switch (actionCommand) {
                case "notas":
                    mostrarNotasAlumno();
                    break;
                case "asistencias":
                    mostrarAsistencias();
                    break;
                case "misBoletines":
                    mostrarMisBoletines();
                    break;
                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar la vista: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de notas del alumno.
     */
    private void mostrarNotas() {
        // Obtener panel principal
        JPanel panelPrincipal = ventana.getPanelPrincipal();
        panelPrincipal.removeAll();
        panelPrincipal.setLayout(new BorderLayout());

        try {
            // Adaptación temporal mientras se actualiza vernotas
            JPanel panelNotas = new JPanel(new BorderLayout());
            panelNotas.add(new JLabel("Cargando notas para el alumno ID: " + alumnoId, JLabel.CENTER), BorderLayout.CENTER);

            // Añadir al panel principal
            panelPrincipal.add(panelNotas, BorderLayout.CENTER);

            // Muestra un mensaje explicativo
            JOptionPane.showMessageDialog(ventana,
                    "La funcionalidad de visualización de notas está en proceso de adaptación.\n"
                    + "Próximamente estará disponible en la nueva interfaz.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar notas: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // En caso de error, restaurar vista principal
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de asistencias del alumno.
     */
    private void mostrarAsistencias() {
        // Obtener panel principal
        JPanel panelPrincipal = ventana.getPanelPrincipal();
        panelPrincipal.removeAll();
        panelPrincipal.setLayout(new BorderLayout());

        try {
            // Adaptación temporal mientras se actualiza verasisten
            JPanel panelAsistencias = new JPanel(new BorderLayout());
            panelAsistencias.add(new JLabel("Cargando asistencias para el alumno ID: " + alumnoId, JLabel.CENTER), BorderLayout.CENTER);

            // Añadir al panel principal
            panelPrincipal.add(panelAsistencias, BorderLayout.CENTER);

            // Muestra un mensaje explicativo
            JOptionPane.showMessageDialog(ventana,
                    "La funcionalidad de visualización de asistencias está en proceso de adaptación.\n"
                    + "Próximamente estará disponible en la nueva interfaz.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar asistencias: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // En caso de error, restaurar vista principal
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de boletines específico para el alumno.
     */
    private void mostrarMisBoletines() {
        try {
            System.out.println("Cargando mis boletines para alumno ID: " + alumnoId);

            // CREAR el panel de boletines específico para alumnos
            // Este panel solo muestra los boletines del alumno actual
            main.java.views.users.common.PanelBoletinesAlumno panelMisBoletines
                    = new main.java.views.users.common.PanelBoletinesAlumno(ventana, alumnoId);

            // Obtener panel principal y configurarlo
            javax.swing.JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new java.awt.BorderLayout());
            panelPrincipal.add(panelMisBoletines, java.awt.BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

            System.out.println("Panel de mis boletines cargado exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al cargar mis boletines: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar mis boletines: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // En caso de error, restaurar vista principal
            ventana.restaurarVistaPrincipal();
        }
    }
}
