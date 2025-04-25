package main.java.views.users.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * Clase abstracta para crear editores de celdas de tabla con botones personalizados.
 * 
 * Extiende DefaultCellEditor para proporcionar una implementación base 
 * de un editor de celdas que contiene un botón interactivo.
 * 
 * Características principales:
 * - Permite personalizar el comportamiento del botón en celdas de tabla
 * - Maneja eventos de selección y clic en el botón
 * - Proporciona un método abstracto para definir acciones de clic
 * 
 * @author [Nicolas Bogarin]
 * @version 1.0
 * @since [12/03/2025]
 */
public abstract class ButtonEditor extends DefaultCellEditor {
    // Botón que se mostrará en la celda de la tabla
    protected JButton button;
    
    // Indica si el botón ha sido presionado
    private boolean isPushed;
    
    /**
     * Constructor que inicializa el editor de botones.
     * 
     * @param checkBox Checkbox base para el editor de celdas
     */
    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }
    
    /**
     * Configura el componente de edición de la celda de la tabla.
     * 
     * Personaliza la apariencia del botón según la selección de la tabla
     * y establece su texto.
     * 
     * @param table Tabla que contiene la celda
     * @param value Valor de la celda
     * @param isSelected Indica si la celda está seleccionada
     * @param row Fila de la celda
     * @param column Columna de la celda
     * @return Componente de edición (botón)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        button.setText(value.toString());
        isPushed = true;
        return button;
    }
    
    /**
     * Obtiene el valor de la celda después de la edición.
     * 
     * Si el botón fue presionado, invoca el método buttonClicked() 
     * para realizar acciones personalizadas.
     * 
     * @return Texto del botón
     */
    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            buttonClicked();
        }
        isPushed = false;
        return button.getText();
    }
    
    /**
     * Detiene la edición de la celda.
     * 
     * Resetea el estado de presión del botón.
     * 
     * @return true si se puede detener la edición
     */
    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
    
    /**
     * Método abstracto que debe ser implementado por las subclases.
     * 
     * Define la acción a realizar cuando se hace clic en el botón.
     */
    protected abstract void buttonClicked();
}