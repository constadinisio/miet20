package main.java.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Utilidad para hacer que los componentes de Swing sean responsive
 */
public class ResponsiveUtils {
    
    /**
     * Aplica comportamiento responsive a un panel
     * @param panel El panel a hacer responsive
     */
    public static void makeResponsive(JPanel panel) {
    // Si el panel ya tiene un layout manager, respetarlo
    // Si no tiene, asignarle BorderLayout que es más flexible para redimensionamiento
    if (panel.getLayout() == null) {
        panel.setLayout(new BorderLayout());
    }
    
    // Hacer responsivas todas las tablas
    makeTablesResponsive(panel);
    
    // Añadir un listener que ajuste los componentes cuando el panel cambia de tamaño
    panel.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            adjustComponents(panel);
        }
    });
}
    
    /**
     * Configura todas las tablas dentro de un contenedor para ser responsivas
     * @param container El contenedor que tiene tablas
     */
    public static void makeTablesResponsive(Container container) {
        // Recorrer todos los componentes
        for (Component comp : container.getComponents()) {
            // Si es una tabla
            if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                table.setFillsViewportHeight(true);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                
                // Ajustar anchos de columnas
                adjustTableColumns(table);
            }
            // Si es un ScrollPane que contiene una tabla
            else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component viewComp = scrollPane.getViewport().getView();
                if (viewComp instanceof JTable) {
                    JTable table = (JTable) viewComp;
                    table.setFillsViewportHeight(true);
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    adjustTableColumns(table);
                }
            }
            // Si es un contenedor, procesarlo recursivamente
            else if (comp instanceof Container) {
                makeTablesResponsive((Container) comp);
            }
        }
    }
    
    /**
     * Ajusta las columnas de una tabla para que tengan anchos proporcionales
     * @param table La tabla a ajustar
     */
    public static void adjustTableColumns(JTable table) {
        if (table.getColumnCount() > 0) {
            TableColumnModel columnModel = table.getColumnModel();
            
            // Primera columna (generalmente nombre) - 30%
            if (columnModel.getColumnCount() > 0) {
                columnModel.getColumn(0).setPreferredWidth(30);
            }
            
            // Segunda columna (generalmente ID/DNI) - 15%
            if (columnModel.getColumnCount() > 1) {
                columnModel.getColumn(1).setPreferredWidth(15);
            }
            
            // Columnas del medio - repartir 40%
            int middleColumns = columnModel.getColumnCount() - 3; // menos primera, segunda y última
            if (middleColumns > 0) {
                int width = 40 / middleColumns;
                for (int i = 2; i < columnModel.getColumnCount() - 1; i++) {
                    columnModel.getColumn(i).setPreferredWidth(width);
                }
            }
            
            // Última columna (generalmente promedio/totales) - 15%
            if (columnModel.getColumnCount() > 2) {
                columnModel.getColumn(columnModel.getColumnCount() - 1).setPreferredWidth(15);
            }
        }
    }
    
    /**
     * Ajusta todos los componentes dentro de un panel
     * @param panel El panel cuyos componentes se ajustarán
     */
    private static void adjustComponents(JPanel panel) {
        // Recorrer todos los componentes
        for (Component comp : panel.getComponents()) {
            // Si es una tabla, ajustar sus columnas
            if (comp instanceof JTable) {
                adjustTableColumns((JTable) comp);
            }
            // Si es un scrollpane que contiene una tabla
            else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component viewComp = scrollPane.getViewport().getView();
                if (viewComp instanceof JTable) {
                    adjustTableColumns((JTable) viewComp);
                }
            }
            // Si es un contenedor, ajustar sus componentes recursivamente
            else if (comp instanceof Container) {
                Container container = (Container) comp;
                for (Component child : container.getComponents()) {
                    if (child instanceof JPanel) {
                        adjustComponents((JPanel) child);
                    }
                }
            }
        }
    }
}