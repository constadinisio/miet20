/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.common;

/**
 * Fábrica para crear gestores de paneles según el rol del usuario.
 * Versión corregida que maneja problemas de compilación y referencias.
 */
public class RolPanelManagerFactory {
    
    /**
     * Crea un gestor de paneles específico según el rol.
     * 
     * @param ventana Ventana principal
     * @param userId ID del usuario
     * @param rolId Rol del usuario
     * @return Gestor configurado para el rol
     */
    public static RolPanelManager createManager(VentanaInicio ventana, int userId, int rolId) {
        try {
            System.out.println("Creando PanelManager para rol: " + rolId + ", usuario: " + userId);
            
            switch (rolId) {
                case 1: 
                    System.out.println("Creando AdminPanelManager");
                    return new AdminPanelManager(ventana, userId);
                case 2: 
                    System.out.println("Creando PreceptorPanelManager");
                    return new PreceptorPanelManager(ventana, userId);
                case 3: 
                    System.out.println("Creando ProfesorPanelManager");
                    return new ProfesorPanelManager(ventana, userId);
                case 4: 
                    System.out.println("Creando AlumnoPanelManager");
                    return new AlumnoPanelManager(ventana, userId);
                case 5: 
                    System.out.println("Creando AttpPanelManager");
                    return new AttpPanelManager(ventana, userId);
                default:
                    System.err.println("Rol no soportado: " + rolId + ". Usando AdminPanelManager por defecto.");
                    return new AdminPanelManager(ventana, userId);
            }
        } catch (Exception e) {
            System.err.println("Error al crear PanelManager para rol " + rolId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Crear un PanelManager por defecto en caso de error
            try {
                System.out.println("Intentando crear AdminPanelManager como respaldo...");
                return new AdminPanelManager(ventana, userId);
            } catch (Exception fallbackError) {
                System.err.println("Error crítico: no se pudo crear ningún PanelManager: " + fallbackError.getMessage());
                fallbackError.printStackTrace();
                
                // Retornar un manager básico de emergencia
                return createEmergencyManager(ventana, userId);
            }
        }
    }
    
    /**
     * Crea un gestor de emergencia que funciona básicamente.
     * Este método es un respaldo cuando todo falla.
     * 
     * @param ventana Ventana principal
     * @param userId ID del usuario
     * @return Gestor básico de emergencia
     */
    private static RolPanelManager createEmergencyManager(VentanaInicio ventana, int userId) {
        return new RolPanelManager() {
            @Override
            public javax.swing.JComponent[] createButtons() {
                // Crear un botón básico de emergencia
                javax.swing.JButton btnEmergencia = new javax.swing.JButton("SISTEMA EN MODO EMERGENCIA");
                btnEmergencia.setBackground(new java.awt.Color(255, 100, 100));
                btnEmergencia.setForeground(java.awt.Color.WHITE);
                btnEmergencia.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
                btnEmergencia.setActionCommand("emergencia");
                btnEmergencia.addActionListener(e -> handleButtonAction("emergencia"));
                
                return new javax.swing.JComponent[]{btnEmergencia};
            }
            
            @Override
            public void handleButtonAction(String actionCommand) {
                if ("emergencia".equals(actionCommand)) {
                    javax.swing.JOptionPane.showMessageDialog(ventana,
                            "El sistema está ejecutándose en modo de emergencia.\n" +
                            "Por favor, contacte al administrador del sistema.\n" +
                            "Error: No se pudieron cargar los paneles específicos del rol.",
                            "Modo Emergencia",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                } else {
                    ventana.restaurarVistaPrincipal();
                }
            }
        };
    }
    
    /**
     * Método de utilidad para verificar si todas las clases necesarias están disponibles.
     * 
     * @return true si todas las clases están disponibles, false en caso contrario
     */
    public static boolean verificarDependencias() {
        try {
            // Verificar que todas las clases PanelManager estén disponibles
            Class.forName("main.java.views.users.common.AdminPanelManager");
            Class.forName("main.java.views.users.common.PreceptorPanelManager");
            Class.forName("main.java.views.users.common.ProfesorPanelManager");
            Class.forName("main.java.views.users.common.AlumnoPanelManager");
            Class.forName("main.java.views.users.common.AttpPanelManager");
            
            System.out.println("Todas las dependencias PanelManager están disponibles");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("Dependencia faltante: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Método estático para inicializar la factory y verificar dependencias.
     * Llamar este método al inicio de la aplicación.
     */
    public static void inicializar() {
        System.out.println("Inicializando RolPanelManagerFactory...");
        
        if (verificarDependencias()) {
            System.out.println("RolPanelManagerFactory inicializada correctamente");
        } else {
            System.err.println("ADVERTENCIA: RolPanelManagerFactory inicializada con dependencias faltantes");
        }
    }
}