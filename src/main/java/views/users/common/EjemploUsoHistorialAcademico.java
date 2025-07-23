package main.java.views.users.common;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de demostración para mostrar cómo integrar el panel de Historial Académico por Curso
 * Esta clase puede servir de guía para integrar la funcionalidad en el sistema principal
 */
public class EjemploUsoHistorialAcademico extends JFrame {
    
    public EjemploUsoHistorialAcademico() {
        inicializarVentana();
    }
    
    private void inicializarVentana() {
        setTitle("📋 Sistema de Consulta de Historial Académico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("📚 SISTEMA DE CONSULTA DE HISTORIAL ACADÉMICO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        lblTitulo.setForeground(new Color(0, 100, 0));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        // Descripción
        JTextArea descripcion = new JTextArea(
            "Esta funcionalidad permite consultar el historial académico completo de cualquier alumno:\n\n" +
            "✓ Seleccione primero el año académico\n" +
            "✓ Luego elija el curso específico\n" +
            "✓ Se mostrará la lista de todos los alumnos del curso seleccionado\n" +
            "✓ Seleccione un alumno y haga clic en 'Ver Historial Académico Completo'\n" +
            "✓ El sistema mostrará todo el historial de progresiones del alumno\n\n" +
            "CARACTERÍSTICAS PRINCIPALES:\n" +
            "• Consulta modular e independiente\n" +
            "• Reutilizable en diferentes partes del sistema\n" +
            "• Interfaz intuitiva con filtros por año y curso\n" +
            "• Historial académico completo con todas las progresiones\n" +
            "• Funciones de exportación e impresión (en desarrollo)"
        );
        descripcion.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descripcion.setEditable(false);
        descripcion.setOpaque(false);
        descripcion.setWrapStyleWord(true);
        descripcion.setLineWrap(true);
        descripcion.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Panel de historial académico - AQUÍ SE INTEGRA LA FUNCIONALIDAD
        // Usando usuarioId = 1 como ejemplo, en la implementación real usar el ID del usuario actual
        HistorialAcademicoPorCursoPanel panelHistorial = new HistorialAcademicoPorCursoPanel(1);
        
        // Instrucciones de integración
        JPanel panelInstrucciones = new JPanel(new BorderLayout());
        panelInstrucciones.setBorder(BorderFactory.createTitledBorder("💡 Cómo Integrar en su Sistema"));
        
        JTextArea instrucciones = new JTextArea(
            "INTEGRACIÓN EN MENÚS PRINCIPALES:\n" +
            "• Agregue una opción 'Consultar Historial Académico' en el menú de Administración\n" +
            "• Cree un JMenuItem que abra una ventana con este panel\n\n" +
            "INTEGRACIÓN EN VENTANAS EXISTENTES:\n" +
            "• Agregue una pestaña en ventanas de gestión de alumnos\n" +
            "• Incluya como panel lateral en interfaces de administración\n\n" +
            "CÓDIGO DE EJEMPLO:\n" +
            "HistorialAcademicoPorCursoPanel historialPanel = new HistorialAcademicoPorCursoPanel(usuarioActualId);\n" +
            "ventanaPrincipal.add(historialPanel, BorderLayout.CENTER);"
        );
        instrucciones.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        instrucciones.setEditable(false);
        instrucciones.setBackground(new Color(245, 245, 245));
        instrucciones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollInstrucciones = new JScrollPane(instrucciones);
        scrollInstrucciones.setPreferredSize(new Dimension(600, 120));
        
        panelInstrucciones.add(scrollInstrucciones, BorderLayout.CENTER);
        
        // Ensamblar la ventana
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(descripcion, BorderLayout.NORTH);
        panelSuperior.add(panelInstrucciones, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelHistorial, BorderLayout.CENTER);
        
        add(panelPrincipal);
        
        // Configurar ventana
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    /**
     * Método principal para demostración
     * En la implementación real, no usar este main
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EjemploUsoHistorialAcademico().setVisible(true);
        });
    }
}
