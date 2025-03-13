package users.Admin;

/**
 * Componente renderer personalizado para mostrar botones en celdas de tabla.
 * 
 * Esta clase permite renderizar botones dentro de las celdas de una tabla de Swing,
 * adaptando su apariencia según el estado de selección de la celda.
 * 
 * Características principales:
 * - Implementa TableCellRenderer para personalizar la visualización de botones
 * - Ajusta colores de fuente y fondo según el estado de selección
 * - Permite configurar texto personalizado para el botón
 * 
 * @author [Nicolas Bogarin]
 * @version 1.0
 * @since [12/03/2025]
 */
import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
public class ButtonRenderer extends JButton implements TableCellRenderer {
    
    /**
     * Constructor que permite establecer el texto del botón.
     * 
     * @param text Texto que se mostrará en el botón
     */
    public ButtonRenderer(String text) {
        setOpaque(true);
        setText(text);
    }
    
    /**
     * Método que configura la apariencia del botón para cada celda de la tabla.
     * 
     * Ajusta los colores de fuente y fondo según si la celda está seleccionada,
     * manteniendo la consistencia visual con el resto de la tabla.
     * 
     * @param table Tabla que contiene la celda
     * @param value Valor de la celda
     * @param isSelected Indica si la celda está seleccionada
     * @param hasFocus Indica si la celda tiene el foco
     * @param row Fila de la celda
     * @param column Columna de la celda
     * @return Componente renderizado (botón)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("Button.background"));
        }
        return this;
    }
}