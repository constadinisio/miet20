package main.java.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Utilidad para hacer que cualquier panel sea responsive y tenga scroll
 * automático. VERSIÓN FINAL CORREGIDA - Soluciona problemas de scroll infinito
 * y contenido cortado.
 */
public class ResponsivePanelWrapper extends JPanel {

    private JScrollPane scrollPane;
    private final JPanel contentPanel;
    private final Container parentContainer;
    private Dimension minContentSize = new Dimension(800, 600);
    private Dimension maxContentSize = new Dimension(1920, 1080);

    // NUEVO: Control para evitar loops de redimensionamiento
    private boolean isAdjusting = false;
    private long lastAdjustTime = 0;
    private static final long ADJUST_COOLDOWN = 100; // 100ms entre ajustes

    /**
     * Constructor que envuelve un panel existente.
     */
    public ResponsivePanelWrapper(JPanel panel, Container parent) {
        this.contentPanel = panel;
        this.parentContainer = parent;

        setupLayout();
        setupScrollPane();
        addResizeListener();

        // Ajuste inicial con delay para evitar problemas
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.invokeLater(() -> {
                adjustContentSizeOnce();
            });
        });
    }

    /**
     * Configura el layout principal del wrapper.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // IMPORTANTE: NO establecer tamaños fijos para el wrapper
        // Dejar que se ajuste naturalmente
    }

    /**
     * Configura el JScrollPane con configuraciones optimizadas.
     */
    private void setupScrollPane() {
        // CRÍTICO: Asegurar que el contenido tenga su tamaño preferido original
        Dimension originalPreferred = contentPanel.getPreferredSize();
        if (originalPreferred.width <= 0 || originalPreferred.height <= 0) {
            // Solo si no tiene tamaño, establecer uno por defecto
            contentPanel.setPreferredSize(new Dimension(800, 600));
        }

        // Crear scroll pane
        scrollPane = new JScrollPane(contentPanel);

        // CONFIGURACIONES CRÍTICAS
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Velocidad de scroll
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(64);

        // Scroll suave
        scrollPane.setWheelScrollingEnabled(true);

        // Configurar viewport
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getViewport().setOpaque(true);

        // IMPORTANTE: No forzar tamaños en el viewport
        // scrollPane.setPreferredSize(...) <- NUNCA hacer esto
        // Añadir al wrapper
        add(scrollPane, BorderLayout.CENTER);

        System.out.println("ScrollPane configurado - Contenido original: " + contentPanel.getPreferredSize());
    }

    /**
     * Añade listener para redimensionamiento, pero controlado.
     */
    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // CONTROL: Solo ajustar si ha pasado suficiente tiempo
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAdjustTime > ADJUST_COOLDOWN && !isAdjusting) {
                    SwingUtilities.invokeLater(() -> adjustContentSizeOnce());
                }
            }
        });
    }

    /**
     * NUEVO: Ajusta el contenido UNA SOLA VEZ para evitar loops.
     */
    private void adjustContentSizeOnce() {
        if (isAdjusting) {
            return; // Ya se está ajustando, evitar loop
        }

        isAdjusting = true;
        lastAdjustTime = System.currentTimeMillis();

        try {
            adjustContentSizeInternal();
        } finally {
            isAdjusting = false;
        }
    }

    /**
     * Ajuste interno del contenido.
     */
    private void adjustContentSizeInternal() {
        if (parentContainer == null || contentPanel == null) {
            return;
        }

        try {
            // Obtener tamaños
            Dimension parentSize = getAvailableParentSize();
            Dimension contentOriginalSize = contentPanel.getPreferredSize();

            System.out.println("=== AJUSTE ÚNICO ===");
            System.out.println("Parent disponible: " + parentSize);
            System.out.println("Contenido original: " + contentOriginalSize);

            // CLAVE: Respetar el tamaño original del contenido si es más grande
            int finalWidth = Math.max(contentOriginalSize.width, minContentSize.width);
            int finalHeight = Math.max(contentOriginalSize.height, minContentSize.height);

            // Solo ajustar si el contenido es muy pequeño comparado con el espacio disponible
            if (finalWidth < parentSize.width - 100) {
                finalWidth = Math.min(parentSize.width - 50, maxContentSize.width);
            }

            Dimension finalContentSize = new Dimension(finalWidth, finalHeight);

            System.out.println("Tamaño final del contenido: " + finalContentSize);

            // CRÍTICO: Establecer el tamaño del contenido, NO del wrapper
            contentPanel.setPreferredSize(finalContentSize);
            contentPanel.setSize(finalContentSize);

            // Hacer responsive las tablas sin cambiar tamaños
            makeTablesResponsiveOnly(contentPanel);

            // Actualizar solo el contenido
            contentPanel.revalidate();
            contentPanel.repaint();

            // Actualizar scroll
            SwingUtilities.invokeLater(() -> {
                scrollPane.revalidate();
                scrollPane.repaint();
            });

        } catch (Exception ex) {
            System.err.println("Error en ajuste interno: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Obtiene el tamaño disponible del contenedor padre.
     */
    private Dimension getAvailableParentSize() {
        Dimension parentSize = parentContainer.getSize();

        // Si el padre no tiene tamaño válido, usar valores por defecto
        if (parentSize.width <= 0 || parentSize.height <= 0) {
            // Buscar en la jerarquía
            Container topLevel = parentContainer;
            while (topLevel.getParent() != null && !(topLevel instanceof JFrame)) {
                topLevel = topLevel.getParent();
            }
            if (topLevel instanceof JFrame) {
                JFrame frame = (JFrame) topLevel;
                parentSize = frame.getContentPane().getSize();
                // Descontar panel lateral y márgenes
                parentSize.width = Math.max(600, parentSize.width - 260);
                parentSize.height = Math.max(500, parentSize.height - 100);
            } else {
                // Fallback a tamaños por defecto
                parentSize = new Dimension(1000, 700);
            }
        }

        return parentSize;
    }

    /**
     * NUEVO: Solo hace responsive las tablas SIN cambiar tamaños de
     * contenedores.
     */
    private void makeTablesResponsiveOnly(Container container) {
        Component[] components = container.getComponents();

        for (Component comp : components) {
            if (comp instanceof JTable) {
                configureTableOnly((JTable) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) comp;
                Component view = sp.getViewport().getView();
                if (view instanceof JTable) {
                    configureTableOnly((JTable) view);
                }
                // Configurar scroll pane
                sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            } else if (comp instanceof Container) {
                makeTablesResponsiveOnly((Container) comp);
            }
        }
    }

    /**
     * NUEVO: Configura SOLO la tabla sin afectar contenedores.
     */
    private void configureTableOnly(JTable table) {
        if (table == null) {
            return;
        }

        try {
            // Configurar auto-resize
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

            // Ajustar altura de filas
            if (table.getRowHeight() < 25) {
                table.setRowHeight(25);
            }

            // Configurar header
            if (table.getTableHeader() != null) {
                table.getTableHeader().setReorderingAllowed(false);
                table.getTableHeader().setResizingAllowed(true);
            }

            // Habilitar viewport height
            table.setFillsViewportHeight(true);

            // NO hacer revalidate/repaint aquí para evitar loops
        } catch (Exception e) {
            System.err.println("Error configurando tabla: " + e.getMessage());
        }
    }

    // Métodos públicos para configuración
    public void setMinimumContentSize(Dimension minSize) {
        this.minContentSize = minSize;
        // NO llamar ajuste automáticamente
    }

    public void setMaximumContentSize(Dimension maxSize) {
        this.maxContentSize = maxSize;
        // NO llamar ajuste automáticamente
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * MODIFICADO: Solo fuerza un ajuste si no se está ajustando ya.
     */
    public void forceResize() {
        if (!isAdjusting) {
            SwingUtilities.invokeLater(() -> adjustContentSizeOnce());
        }
    }

    public void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
                scrollPane.getHorizontalScrollBar().setValue(0);
            }
        });
    }

    /**
     * NUEVO: Scroll hasta abajo para verificar que todo el contenido es
     * accesible.
     */
    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            }
        });
    }

    /**
     * NUEVO: Obtiene información de debug sobre el contenido.
     */
    public void printDebugInfo() {
        System.out.println("=== DEBUG INFO ===");
        System.out.println("Wrapper size: " + getSize());
        System.out.println("Content preferred: " + contentPanel.getPreferredSize());
        System.out.println("Content actual: " + contentPanel.getSize());
        System.out.println("ScrollPane size: " + scrollPane.getSize());
        System.out.println("Vertical scroll max: " + scrollPane.getVerticalScrollBar().getMaximum());
        System.out.println("Content component count: " + contentPanel.getComponentCount());

        // Mostrar información de componentes del contenido
        listComponents(contentPanel, 0);
    }

    private void listComponents(Container container, int level) {
        String indent = "  ".repeat(level);
        for (Component comp : container.getComponents()) {
            System.out.println(indent + "- " + comp.getClass().getSimpleName()
                    + " [" + comp.getBounds() + "] visible=" + comp.isVisible());
            if (comp instanceof Container && level < 3) {
                listComponents((Container) comp, level + 1);
            }
        }
    }

    // Métodos estáticos de utilidad
    public static ResponsivePanelWrapper wrap(JPanel panel, Container parent) {
        return new ResponsivePanelWrapper(panel, parent);
    }

    public static ResponsivePanelWrapper wrap(JPanel panel, Container parent,
            Dimension minSize, Dimension maxSize) {
        ResponsivePanelWrapper wrapper = new ResponsivePanelWrapper(panel, parent);
        wrapper.setMinimumContentSize(minSize);
        wrapper.setMaximumContentSize(maxSize);
        return wrapper;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
