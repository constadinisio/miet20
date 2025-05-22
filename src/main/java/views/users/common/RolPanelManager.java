/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.common;

import javax.swing.JComponent;

/**
 * Interfaz para gestores de paneles específicos de cada rol.
 */
public interface RolPanelManager {
     
    /**
     * Crea los botones específicos para un rol.
     * 
     * @return Array de componentes (botones) para el rol
     */
    JComponent[] createButtons();
    
    /**
     * Maneja la acción cuando se hace clic en un botón.
     * 
     * @param actionCommand Comando de acción del botón
     */
    void handleButtonAction(String actionCommand);
}