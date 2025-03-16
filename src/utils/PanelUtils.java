package utils;

import java.awt.*;
import javax.swing.*;
import users.Preceptor.AsistenciaPreceptorPanel;

public class PanelUtils {

    /**
     * Hace que cualquier JPanel sea scrolleable cuando se agrega a un
     * contenedor
     *
     * @param panel El panel que se hará scrolleable
     * @return El ScrollPane que contiene el panel
     */
    public static JScrollPane makeScrollable(JPanel panel) {
        // Crear un ScrollPane y agregar el panel
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    /**
     * Agrega cualquier panel al contenedor especificado, haciéndolo scrolleable
     * y responsivo
     *
     * @param container El contenedor donde se agregará el panel
     * @param panel El panel a agregar
     * @param constraints Las restricciones de layout (como BorderLayout.CENTER)
     */
    public static void addPanelToContainer(Container container, JPanel panel, Object constraints) {
        // Limpiar el contenedor
        container.removeAll();

        // Configurar el layout del contenedor si es necesario
        if (!(container.getLayout() instanceof BorderLayout)) {
            container.setLayout(new BorderLayout());
        }

        // Crear ScrollPane y agregar el panel
        JScrollPane scrollPane = makeScrollable(panel);

        // Agregar el ScrollPane al contenedor
        container.add(scrollPane, constraints);

        // Actualizar la vista
        container.revalidate();
        container.repaint();
    }

    // Agregar este método a PanelUtils.java
    public static void addAsistenciaPanelToContainer(Container container, AsistenciaPreceptorPanel panel, Object constraints) {
        // Limpiar el contenedor
        container.removeAll();

        // Configurar el layout del contenedor
        if (!(container.getLayout() instanceof BorderLayout)) {
            container.setLayout(new BorderLayout());
        }

        // Ajustar el panel para scroll
        panel.ajustarPanelParaScroll();

        // Crear un panel contenedor con BoxLayout vertical para preservar la estructura
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.add(panel);

        // Crear ScrollPane y agregar el panel wrapper
        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Agregar el ScrollPane al contenedor
        container.add(scrollPane, constraints);

        // Actualizar la vista
        container.revalidate();
        container.repaint();
    }

// Agregar este método a PanelUtils.java
    public static void addPanelWithOriginalLayout(Container container, JPanel panel, Object constraints) {
        // Limpiar el contenedor
        container.removeAll();

        // Configurar el layout del contenedor
        if (!(container.getLayout() instanceof BorderLayout)) {
            container.setLayout(new BorderLayout());
        }

        // Si el panel no tiene tamaño preferido establecido, configurar uno
        if (panel.getPreferredSize().width <= 0 || panel.getPreferredSize().height <= 0) {
            panel.setPreferredSize(new Dimension(1200, 800));
        }

        // Crear ScrollPane con viewport que respete el tamaño preferido
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Importante: evitar que el viewport redimensione el panel
        scrollPane.getViewport().setViewSize(panel.getPreferredSize());

        // Agregar el ScrollPane al contenedor
        container.add(scrollPane, constraints);

        // Actualizar la vista
        container.revalidate();
        container.repaint();
    }

}
