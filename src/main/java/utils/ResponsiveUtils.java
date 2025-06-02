package main.java.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Utilidades para hacer componentes completamente responsive
 * Compatible con el sistema de VentanaInicio
 */
public class ResponsiveUtils {
    
    // Constantes para tablas responsive
    public static final int MIN_COLUMN_WIDTH = 80;
    public static final int MAX_COLUMN_WIDTH = 400;
    public static final int DEFAULT_ROW_HEIGHT = 25;
    public static final int HEADER_HEIGHT = 30;
    
    // Control para evitar loops de redimensionamiento
    private static boolean isAdjustingTables = false;
    private static long lastTableAdjustTime = 0;
    private static final long TABLE_ADJUST_COOLDOWN = 150; // 150ms entre ajustes

    /**
     * Hace que una tabla sea completamente responsive con scroll horizontal garantizado
     */
    public static void makeTableResponsive(JTable table, int availableWidth) {
        if (table == null) {
            return;
        }

        try {
            System.out.println("Configurando tabla responsive: " + table.getClass().getSimpleName());
            System.out.println("  Columnas: " + table.getColumnCount());
            System.out.println("  Filas: " + table.getRowCount());
            System.out.println("  Ancho disponible: " + availableWidth);

            // 1. Configuraciones básicas
            configureBasicTableProperties(table);

            // 2. Configurar ScrollPane padre
            configureParentScrollPane(table);

            // 3. Forzar anchos de columna para garantizar scroll horizontal
            forceColumnWidthsForScroll(table, availableWidth);

            // 4. Agregar listener para redimensionamiento
            addResizeListener(table);

            System.out.println("✅ Tabla configurada como responsive");

        } catch (Exception e) {
            System.err.println("Error configurando tabla responsive: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura las propiedades básicas de una tabla
     */
    private static void configureBasicTableProperties(JTable table) {
        // Configurar altura de filas
        table.setRowHeight(DEFAULT_ROW_HEIGHT);

        // Configurar header
        if (table.getTableHeader() != null) {
            table.getTableHeader().setPreferredSize(new Dimension(0, HEADER_HEIGHT));
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setResizingAllowed(true);
        }

        // Habilitar fill viewport
        table.setFillsViewportHeight(true);

        // Configurar selección
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar colores para mejor visibilidad
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);

        // Configurar fondo
        table.setBackground(Color.WHITE);
        table.setOpaque(true);

        // CRÍTICO: Desactivar auto-resize para permitir scroll horizontal
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    /**
     * Configura el ScrollPane padre de la tabla
     */
    private static void configureParentScrollPane(JTable table) {
        Container parent = table.getParent();
        while (parent != null && !(parent instanceof JScrollPane)) {
            parent = parent.getParent();
        }

        if (parent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) parent;
            configureScrollPane(scrollPane);
        }
    }

    /**
     * Configura un ScrollPane para mostrar scrollbars correctamente
     */
    public static void configureScrollPane(JScrollPane scrollPane) {
        System.out.println("Configurando ScrollPane...");

        // CRÍTICO: Políticas de scrollbar SIEMPRE visibles
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Configurar velocidades de scroll
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(100);

        // Habilitar scroll con rueda del mouse
        scrollPane.setWheelScrollingEnabled(true);

        // Configurar viewport
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        System.out.println("ScrollPane configurado - Políticas: V=ALWAYS, H=ALWAYS");
    }

    /**
     * Fuerza anchos de columna para garantizar scroll horizontal
     */
    private static void forceColumnWidthsForScroll(JTable table, int availableWidth) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();

        if (columnCount == 0) {
            return;
        }

        // Calcular ancho mínimo por columna para forzar scroll
        int minWidthPerColumn = Math.max(120, MIN_COLUMN_WIDTH);
        int totalForcedWidth = columnCount * minWidthPerColumn;

        // GARANTIZAR que el ancho total sea mayor que el disponible
        if (totalForcedWidth <= availableWidth) {
            totalForcedWidth = availableWidth + 300; // Forzar 300px adicionales
            minWidthPerColumn = totalForcedWidth / columnCount;
        }

        System.out.println("Forzando anchos de columna:");
        System.out.println("  Ancho disponible: " + availableWidth);
        System.out.println("  Ancho total forzado: " + totalForcedWidth);
        System.out.println("  Ancho por columna: " + minWidthPerColumn);

        // Aplicar anchos específicos por tipo de columna
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = columnModel.getColumn(i);
            String columnName = table.getColumnName(i);

            int optimalWidth = calculateOptimalColumnWidth(columnName, minWidthPerColumn);

            column.setMinWidth(Math.max(MIN_COLUMN_WIDTH, optimalWidth / 2));
            column.setPreferredWidth(optimalWidth);
            column.setMaxWidth(MAX_COLUMN_WIDTH);

            System.out.println("  Columna '" + columnName + "': " + optimalWidth + "px");
        }

        // FORZAR que la tabla tenga el ancho total calculado
        table.setPreferredScrollableViewportSize(
                new Dimension(totalForcedWidth, table.getPreferredScrollableViewportSize().height)
        );

        System.out.println("Tabla forzada a ancho: " + totalForcedWidth + "px");
    }

    /**
     * Calcula el ancho óptimo para una columna basado en su nombre
     */
    private static int calculateOptimalColumnWidth(String columnName, int baseWidth) {
        String name = columnName.toLowerCase();

        // Anchos específicos por tipo de columna
        if (name.contains("id")) {
            return Math.max(80, baseWidth / 2);
        } else if (name.contains("dni")) {
            return Math.max(100, baseWidth);
        } else if (name.contains("nombre") || name.contains("alumno")) {
            return Math.max(200, baseWidth * 2);
        } else if (name.contains("fecha")) {
            return Math.max(140, baseWidth);
        } else if (name.contains("estado")) {
            return Math.max(100, baseWidth);
        } else if (name.contains("ruta") || name.contains("archivo")) {
            return Math.max(250, baseWidth * 2);
        } else if (name.contains("nota") || name.contains("promedio")) {
            return Math.max(80, baseWidth / 2);
        } else if (name.contains("materia") || name.contains("trabajo")) {
            return Math.max(150, baseWidth);
        } else if (name.contains("curso") || name.contains("división")) {
            return Math.max(80, baseWidth / 2);
        } else if (name.contains("período") || name.contains("periodo")) {
            return Math.max(100, baseWidth);
        } else if (name.contains("descripcion") || name.contains("observaciones")) {
            return Math.max(200, baseWidth * 2);
        } else {
            // Ancho por defecto
            return Math.max(120, baseWidth);
        }
    }

    /**
     * Agrega listener para redimensionamiento automático de tabla
     */
    private static void addResizeListener(JTable table) {
        // Buscar el contenedor top-level
        Container topContainer = table;
        while (topContainer.getParent() != null) {
            topContainer = topContainer.getParent();
            if (topContainer instanceof javax.swing.JFrame) {
                break;
            }
        }

        if (topContainer instanceof javax.swing.JFrame) {
            topContainer.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        if (!isAdjustingTables) {
                            // Obtener nuevo ancho disponible
                            int newWidth = e.getComponent().getWidth() - 260; // Panel lateral
                            forceColumnWidthsForScroll(table, newWidth);
                            configureParentScrollPane(table);
                        }
                    });
                }
            });
        }
    }

    /**
     * Hace responsive todas las tablas de un contenedor
     */
    public static void makeAllTablesResponsive(Container container, int availableWidth) {
        // Control para evitar llamadas excesivas
        long currentTime = System.currentTimeMillis();
        if (isAdjustingTables || (currentTime - lastTableAdjustTime) < TABLE_ADJUST_COOLDOWN) {
            return;
        }

        isAdjustingTables = true;
        lastTableAdjustTime = currentTime;

        try {
            List<JTable> tables = findAllTables(container);

            System.out.println("=== PROCESANDO TABLAS RESPONSIVE ===");
            System.out.println("Tablas encontradas: " + tables.size());
            System.out.println("Ancho disponible: " + availableWidth);

            for (JTable table : tables) {
                makeTableResponsive(table, availableWidth);
            }

            System.out.println("✅ Todas las tablas procesadas como responsive");

        } finally {
            isAdjustingTables = false;
        }
    }

    /**
     * Encuentra todas las tablas en un contenedor recursivamente
     */
    public static List<JTable> findAllTables(Container container) {
        List<JTable> tables = new ArrayList<>();

        Component[] components = container.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTable) {
                tables.add((JTable) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) comp;
                Component view = sp.getViewport().getView();
                if (view instanceof JTable) {
                    tables.add((JTable) view);
                }
            } else if (comp instanceof Container) {
                tables.addAll(findAllTables((Container) comp));
            }
        }

        return tables;
    }

    /**
     * Crea un botón con estilo responsive estándar
     */
    public static JButton createResponsiveButton(String text, String actionCommand, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Tamaños responsive
        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));
        button.setMinimumSize(new Dimension(150, 35));
        
        return button;
    }

    /**
     * Configura un panel para ser responsive
     */
    public static void makeContainerResponsive(Container container) {
        // Agregar listener para redimensionamiento
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int width = container.getWidth();
                    makeAllTablesResponsive(container, width - 50);
                });
            }
        });
    }

    /**
     * Fuerza la actualización de todas las tablas en un contenedor
     */
    public static void forceUpdateAllTables(Container container) {
        SwingUtilities.invokeLater(() -> {
            try {
                int availableWidth = Math.max(600, container.getWidth() - 50);
                makeAllTablesResponsive(container, availableWidth);
                
                // Forzar repaint
                container.revalidate();
                container.repaint();
                
                System.out.println("✅ Tablas actualizadas forzadamente");
                
            } catch (Exception ex) {
                System.err.println("Error al actualizar tablas: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}