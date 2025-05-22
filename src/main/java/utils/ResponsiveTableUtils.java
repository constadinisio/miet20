package main.java.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Utilidades para hacer que las tablas sean completamente responsive.
 * Esta clase se encarga de ajustar automáticamente las tablas según el tamaño de la ventana.
 */
public class ResponsiveTableUtils {
    
    /**
     * Hace que una tabla sea completamente responsive.
     * 
     * @param table La tabla a hacer responsive
     * @param container El contenedor padre para calcular tamaños
     */
    public static void makeTableResponsive(JTable table, Container container) {
        if (table == null) return;
        
        // Configuraciones básicas de responsive
        setupBasicResponsiveConfig(table);
        
        // Ajustar columnas dinámicamente
        setupDynamicColumnAdjustment(table, container);
        
        // Configurar renderizado responsive
        setupResponsiveRendering(table);
        
        // Añadir listener para cambios de tamaño
        addResizeListener(table, container);
    }
    
    /**
     * Configura las propiedades básicas responsive de la tabla.
     */
    private static void setupBasicResponsiveConfig(JTable table) {
        // Permitir que la tabla se redimensione automáticamente
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        // Configurar altura de filas adaptativa
        table.setRowHeight(Math.max(25, table.getRowHeight()));
        
        // Configurar selección
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Configurar header
        if (table.getTableHeader() != null) {
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setResizingAllowed(true);
            table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        }
        
        // Habilitar scroll horizontal cuando sea necesario
        table.setAutoCreateRowSorter(true);
    }
    
    /**
     * Configura el ajuste dinámico de columnas.
     */
    private static void setupDynamicColumnAdjustment(JTable table, Container container) {
        if (table.getColumnModel() == null) return;
        
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        
        if (columnCount == 0) return;
        
        // Calcular ancho disponible
        int availableWidth = container != null ? container.getWidth() - 50 : 800;
        
        // Ajustar según el número de columnas y ancho disponible
        adjustColumnWidths(table, availableWidth);
    }
    
    /**
     * Ajusta el ancho de las columnas de manera inteligente.
     */
    private static void adjustColumnWidths(JTable table, int availableWidth) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        
        if (columnCount == 0) return;
        
        // Anchos por defecto según el tipo de columna
        int[] columnWidths = calculateOptimalColumnWidths(table, availableWidth);
        
        // Aplicar los anchos calculados
        for (int i = 0; i < Math.min(columnCount, columnWidths.length); i++) {
            TableColumn column = columnModel.getColumn(i);
            column.setPreferredWidth(columnWidths[i]);
            column.setMinWidth(Math.max(50, columnWidths[i] / 2)); // Mínimo 50px o la mitad
            column.setMaxWidth(columnWidths[i] * 2); // Máximo el doble
        }
    }
    
    /**
     * Calcula los anchos óptimos para las columnas basándose en su contenido y tipo.
     */
    private static int[] calculateOptimalColumnWidths(JTable table, int availableWidth) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        int[] widths = new int[columnCount];
        
        // Ancho base por columna
        int baseWidth = Math.max(80, availableWidth / columnCount);
        
        for (int i = 0; i < columnCount; i++) {
            String columnName = table.getColumnName(i).toLowerCase();
            
            // Asignar anchos según el tipo de columna
            if (columnName.contains("id") || columnName.contains("dni")) {
                widths[i] = Math.min(100, baseWidth);
            } else if (columnName.contains("nombre") || columnName.contains("apellido")) {
                widths[i] = Math.max(150, baseWidth);
            } else if (columnName.contains("email") || columnName.contains("mail")) {
                widths[i] = Math.max(200, baseWidth);
            } else if (columnName.contains("fecha") || columnName.contains("date")) {
                widths[i] = Math.max(120, baseWidth);
            } else if (columnName.contains("estado") || columnName.contains("status")) {
                widths[i] = Math.min(100, baseWidth);
            } else if (columnName.contains("descripcion") || columnName.contains("observaciones")) {
                widths[i] = Math.max(250, baseWidth);
            } else {
                widths[i] = baseWidth;
            }
        }
        
        // Ajustar si el total excede el ancho disponible
        int totalWidth = 0;
        for (int width : widths) {
            totalWidth += width;
        }
        
        if (totalWidth > availableWidth) {
            double factor = (double) availableWidth / totalWidth;
            for (int i = 0; i < widths.length; i++) {
                widths[i] = (int) (widths[i] * factor);
            }
        }
        
        return widths;
    }
    
    /**
     * Configura el renderizado responsive para las celdas.
     */
    private static void setupResponsiveRendering(JTable table) {
        // Renderer responsive que ajusta el texto según el ancho
        DefaultTableCellRenderer responsiveRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component comp = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    
                    // Ajustar alineación según el tipo de datos
                    if (value instanceof Number) {
                        label.setHorizontalAlignment(JLabel.RIGHT);
                    } else if (value instanceof Boolean) {
                        label.setHorizontalAlignment(JLabel.CENTER);
                    } else {
                        label.setHorizontalAlignment(JLabel.LEFT);
                    }
                    
                    // Tooltip para texto largo
                    String text = value != null ? value.toString() : "";
                    if (text.length() > 30) {
                        label.setToolTipText(text);
                    } else {
                        label.setToolTipText(null);
                    }
                    
                    // Ajustar fuente según el tamaño de la tabla
                    int tableWidth = table.getWidth();
                    Font font = table.getFont();
                    if (tableWidth < 600 && font.getSize() > 11) {
                        label.setFont(font.deriveFont(11f));
                    } else if (tableWidth >= 1000 && font.getSize() < 13) {
                        label.setFont(font.deriveFont(13f));
                    }
                }
                
                return comp;
            }
        };
        
        // Aplicar el renderer a todas las columnas
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(responsiveRenderer);
        }
    }
    
    /**
     * Añade listener para ajustar la tabla cuando cambia el tamaño del contenedor.
     */
    private static void addResizeListener(JTable table, Container container) {
        if (container == null) return;
        
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    adjustTableToNewSize(table, container);
                });
            }
        });
    }
    
    /**
     * Ajusta la tabla cuando cambia el tamaño del contenedor.
     */
    private static void adjustTableToNewSize(JTable table, Container container) {
        if (table == null || container == null) return;
        
        try {
            int newWidth = container.getWidth() - 50; // Margen para scrollbars
            
            // Reajustar columnas
            adjustColumnWidths(table, newWidth);
            
            // Cambiar modo de resize según el ancho
            if (newWidth < 600) {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            } else if (newWidth < 900) {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            }
            
            // Ajustar altura de filas
            int rowHeight = newWidth < 600 ? 20 : (newWidth < 900 ? 25 : 28);
            table.setRowHeight(rowHeight);
            
            // Forzar actualización
            table.revalidate();
            table.repaint();
            
        } catch (Exception ex) {
            System.err.println("Error al ajustar tabla responsive: " + ex.getMessage());
        }
    }
    
    /**
     * Hace responsive un JScrollPane que contiene una tabla.
     */
    public static void makeScrollPaneResponsive(JScrollPane scrollPane, Container container) {
        if (scrollPane == null || scrollPane.getViewport() == null) return;
        
        Component view = scrollPane.getViewport().getView();
        if (view instanceof JTable) {
            JTable table = (JTable) view;
            makeTableResponsive(table, container);
            
            // Configurar el scroll pane
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            // Optimizar velocidad de scroll
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        }
    }
    
    /**
     * Método de utilidad para hacer responsive todas las tablas en un contenedor.
     */
    public static void makeAllTablesResponsive(Container container) {
        makeAllTablesResponsiveRecursive(container, container);
    }
    
    /**
     * Método recursivo para procesar todas las tablas en un contenedor.
     */
    private static void makeAllTablesResponsiveRecursive(Container container, Container rootContainer) {
        if (container == null) return;
        
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) {
                makeTableResponsive((JTable) comp, rootContainer);
            } else if (comp instanceof JScrollPane) {
                makeScrollPaneResponsive((JScrollPane) comp, rootContainer);
            } else if (comp instanceof Container) {
                makeAllTablesResponsiveRecursive((Container) comp, rootContainer);
            }
        }
    }
}