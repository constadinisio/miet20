package users.common;

import javax.swing.*;
import java.awt.*;

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