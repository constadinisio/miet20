/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.common;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import main.java.views.users.Admin.GestionCursosPanel;
import main.java.views.users.Admin.GestionUsuariosPanel;
import main.java.views.users.Admin.UsuariosPendientesPanel;

/**
 * Gestor de paneles específico para el rol de Administrador.
 */
public class AdminPanelManager implements RolPanelManager {
    
    private final VentanaInicio ventana;
    private final int userId;
    
    /**
     * Constructor del gestor de paneles para administradores.
     * 
     * @param ventana Ventana principal
     * @param userId ID del usuario
     */
    public AdminPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.userId = userId;
    }
    
    @Override
    public JComponent[] createButtons() {
        // Crear botones específicos para el rol de administrador
        JButton btnUsuariosPendientes = createStyledButton("USUARIOS PENDIENTES", "usuariosPendientes");
        JButton btnGestionUsuarios = createStyledButton("GESTIÓN USUARIOS", "gestionUsuarios");
        JButton btnGestionCursos = createStyledButton("GESTIÓN CURSOS", "gestionCursos");
        
        // Retornar array de botones
        return new JComponent[]{
            btnUsuariosPendientes,
            btnGestionUsuarios,
            btnGestionCursos
        };
    }
    
    /**
     * Crea un botón con el estilo estándar de la aplicación.
     * 
     * @param text Texto del botón
     * @param actionCommand Comando de acción para identificar el botón
     * @return Botón configurado
     */
    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));
        
        // Establecer dimensiones preferidas
        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));
        
        return button;
    }
    
    @Override
    public void handleButtonAction(String actionCommand) {
        // Obtener el panel principal
        javax.swing.JPanel panelPrincipal = ventana.getPanelPrincipal();
        
        // Remover el contenido actual
        panelPrincipal.removeAll();
        
        // Definir el layout adecuado
        panelPrincipal.setLayout(new BorderLayout());
        
        try {
            // Añadir el panel correspondiente según el comando de acción
            switch (actionCommand) {
                case "usuariosPendientes":
                    UsuariosPendientesPanel panelUsuariosPendientes = new UsuariosPendientesPanel();
                    panelPrincipal.add(panelUsuariosPendientes, BorderLayout.CENTER);
                    break;
                    
                case "gestionUsuarios":
                    GestionUsuariosPanel panelGestionUsuarios = new GestionUsuariosPanel();
                    panelPrincipal.add(panelGestionUsuarios, BorderLayout.CENTER);
                    break;
                    
                case "gestionCursos":
                    GestionCursosPanel panelGestionCursos = new GestionCursosPanel();
                    panelPrincipal.add(panelGestionCursos, BorderLayout.CENTER);
                    break;
                    
                default:
                    // Si no reconoce el comando, restaurar vista principal
                    ventana.restaurarVistaPrincipal();
                    break;
            }
            
            // Actualizar el panel
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana, 
                    "Error al cargar el panel: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            
            // En caso de error, restaurar la vista principal
            ventana.restaurarVistaPrincipal();
        }
    }
}