package main.java.views.users.common;

import javax.swing.*;
import java.awt.*;

/**
 * Editor personalizado para selección de estados de asistencia.
 * 
 * Características principales:
 * - Proporciona un combo box para seleccionar estados de asistencia
 * - Renderiza cada estado con un color de fondo diferente
 * - Extiende DefaultCellEditor para uso en tablas
 * 
 * Estados de asistencia:
 * - P: Presente (Verde claro)
 * - A: Ausente (Rojo claro)
 * - T: Tarde (Amarillo claro)
 * - AP: Ausente con Permiso (Naranja claro)
 * - NC: No Corresponde (Blanco)
 * 
 * @author [Nicolas Bogarin]
 * @version 1.0
 * @since [13/03/2025]
 */
public class EstadoAsistenciaEditor extends DefaultCellEditor {
    private static final String[] ESTADOS = {"P", "A", "T", "AP", "NC"};
    
    public EstadoAsistenciaEditor() {
        super(new JComboBox<>(ESTADOS));
        
        JComboBox<String> comboBox = (JComboBox<String>)getComponent();
        comboBox.setBackground(Color.WHITE);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (!isSelected) {
                    switch (value.toString()) {
                        case "P":
                            c.setBackground(new Color(144, 238, 144)); break;
                        case "A":
                            c.setBackground(new Color(255, 182, 193)); break;
                        case "T":
                            c.setBackground(new Color(255, 255, 153)); break;
                        case "AP":
                            c.setBackground(new Color(255, 218, 185)); break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }
                
                return c;
            }
        });
    }
}