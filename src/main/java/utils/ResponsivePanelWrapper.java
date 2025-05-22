package main.java.utils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Utilidad para hacer que cualquier panel sea responsive y tenga scroll automático.
 * Esta clase envuelve cualquier panel y lo hace adaptable al tamaño de la ventana.
 */
public class ResponsivePanelWrapper extends JPanel {
    
    private JScrollPane scrollPane; // No final para poder inicializar en constructor
    private final JPanel contentPanel;
    private final Container parentContainer;
    private Dimension minSize = new Dimension(800, 600);
    private Dimension maxSize = new Dimension(1920, 1080);
    
    /**
     * Constructor que envuelve un panel existente.
     *
     * @param panel Panel a envolver
     * @param parent Contenedor padre para calcular tamaños
     */
    public ResponsivePanelWrapper(JPanel panel, Container parent) {
        this.contentPanel = panel;
        this.parentContainer = parent;
        
        setupLayout();
        setupScrollPane();
        addResizeListener();
    }
    
    /**
     * Configura el layout principal del wrapper.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
    }
    
    /**
     * Configura el JScrollPane con configuraciones optimizadas.
     */
    private void setupScrollPane() {
        // Crear scroll pane con configuraciones optimizadas
        scrollPane = new JScrollPane(contentPanel); // Ahora no es final, puede asignarse
        
        // Configuraciones de scroll
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Velocidad de scroll optimizada
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(64);
        
        // Scroll suave
        scrollPane.setWheelScrollingEnabled(true);
        
        // Configurar viewport
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getViewport().setOpaque(true);
        
        // Añadir al wrapper
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Añade listener para redimensionamiento automático.
     */
    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustContentSize();
            }
        });
    }
    
    /**
     * Ajusta el tamaño del contenido basado en el contenedor padre.
     */
    private void adjustContentSize() {
        if (parentContainer == null) return;
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Obtener dimensiones disponibles
                Dimension parentSize = parentContainer.getSize();
                Dimension availableSize = calculateAvailableSize(parentSize);
                
                // Ajustar tamaño del wrapper
                setPreferredSize(availableSize);
                
                // Ajustar contenido si es necesario
                adjustContentPanel(availableSize);
                
                // Forzar actualización
                revalidate();
                repaint();
                
            } catch (Exception ex) {
                System.err.println("Error al ajustar tamaño responsive: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Calcula el tamaño disponible considerando márgenes y otros componentes.
     */
    private Dimension calculateAvailableSize(Dimension parentSize) {
        int width = Math.max(minSize.width, 
                    Math.min(maxSize.width, parentSize.width - 20));
        int height = Math.max(minSize.height, 
                     Math.min(maxSize.height, parentSize.height - 20));
        
        return new Dimension(width, height);
    }
    
    /**
     * Ajusta el panel de contenido para que use el espacio disponible de manera óptima.
     */
    private void adjustContentPanel(Dimension availableSize) {
        if (contentPanel == null) return;
        
        // Calcular tamaño óptimo para el contenido
        Dimension contentSize = contentPanel.getPreferredSize();
        
        // Si el contenido es más pequeño que el área disponible, expandirlo
        if (contentSize.width < availableSize.width - 50) {
            contentSize.width = availableSize.width - 50; // Margen para scroll
        }
        
        if (contentSize.height < availableSize.height - 50) {
            contentSize.height = availableSize.height - 50; // Margen para scroll
        }
        
        // Establecer tamaño preferido del contenido
        contentPanel.setPreferredSize(contentSize);
        
        // Hacer responsive todas las tablas del panel usando try-catch por si la clase no existe
        try {
            ResponsiveTableUtils.makeAllTablesResponsive(contentPanel);
        } catch (Exception e) {
            // Si ResponsiveTableUtils no está disponible, usar método básico
            adjustTablesBasic(contentPanel);
        }
        
        // Forzar layout del contenido
        contentPanel.revalidate();
    }
    
    /**
     * Ajusta tablas de manera básica si ResponsiveTableUtils no está disponible.
     */
    private void adjustTablesBasic(Container container) {
        Component[] components = container.getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JTable) {
                makeTableResponsiveBasic((JTable) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) comp;
                if (sp.getViewport().getView() instanceof JTable) {
                    makeTableResponsiveBasic((JTable) sp.getViewport().getView());
                }
            } else if (comp instanceof Container) {
                adjustTablesBasic((Container) comp);
            }
        }
    }
    
    /**
     * Hace que una tabla específica sea responsive de manera básica.
     */
    private void makeTableResponsiveBasic(JTable table) {
        // Configurar auto-resize
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        // Ajustar altura de filas
        table.setRowHeight(Math.max(25, table.getRowHeight()));
        
        // Configurar header
        if (table.getTableHeader() != null) {
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setResizingAllowed(true);
        }
        
        // Forzar actualización
        table.revalidate();
    }
    
    /**
     * Establece el tamaño mínimo permitido.
     */
    public void setMinimumContentSize(Dimension minSize) {
        this.minSize = minSize;
        adjustContentSize();
    }
    
    /**
     * Establece el tamaño máximo permitido.
     */
    public void setMaximumContentSize(Dimension maxSize) {
        this.maxSize = maxSize;
        adjustContentSize();
    }
    
    /**
     * Obtiene el panel de contenido original.
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    /**
     * Obtiene el JScrollPane interno.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
    
    /**
     * Fuerza una actualización del tamaño.
     */
    public void forceResize() {
        adjustContentSize();
    }
    
    /**
     * Configura el scroll para ir a la parte superior.
     */
    public void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
                scrollPane.getHorizontalScrollBar().setValue(0);
            }
        });
    }
    
    /**
     * Método estático para crear un wrapper responsive rápidamente.
     */
    public static ResponsivePanelWrapper wrap(JPanel panel, Container parent) {
        return new ResponsivePanelWrapper(panel, parent);
    }
    
    /**
     * Método estático para crear un wrapper con tamaños personalizados.
     */
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
