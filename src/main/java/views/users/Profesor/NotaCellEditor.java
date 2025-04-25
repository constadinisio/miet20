/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.Profesor;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 *
 * @author nico_
 */
public class NotaCellEditor extends DefaultCellEditor {
    
    public NotaCellEditor() {
        super(new JComboBox<>(new String[]{"NC", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}));
        
        JComboBox<String> comboBox = (JComboBox<String>)getComponent();
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (!isSelected) {
                    String nota = value.toString();
                    if (nota.equals("NC")) {
                        c.setBackground(Color.LIGHT_GRAY);
                    } else {
                        int valorNota = Integer.parseInt(nota);
                        if (valorNota < 6) {
                            c.setBackground(new Color(255, 182, 193)); // Rojo claro
                        } else if (valorNota >= 6 && valorNota < 8) {
                            c.setBackground(new Color(255, 255, 153)); // Amarillo claro
                        } else {
                            c.setBackground(new Color(144, 238, 144)); // Verde claro
                        }
                    }
                }
                return c;
            }
        });
    }
}