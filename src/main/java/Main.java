package main.java;

import javax.swing.*;
import main.java.utils.ResourceManager;
import main.java.updater.ActualizadorApp;
import main.java.views.login.GoogleAuthenticator;
import main.java.views.login.LoginForm;
import main.java.services.NotificationCore;

public class Main {

    private static final String APP_NAME = "Sistema de Gestión Escolar ET20";
    private static final String APP_VERSION = "3.0 - Sistema Consolidado";

    public static void main(String args[]) {
        // Mostrar información de inicio
        mostrarInfoInicio();

        
        
        // Configurar look and feel
        configurarLookAndFeel();

        // Verificar actualizaciones antes de iniciar la aplicación
        //verificarYActualizarAplicacion();
        
        // Llamado al ResourceManager para que se ejecute y haga la comprobación de las imágenes
        try {
            // Inicializar el gestor de recursos
            ResourceManager.initialize();
        } catch (Exception e) {
            // Solo mostrar un diálogo en caso de error crítico
            JOptionPane.showMessageDialog(null,
                    "Error al inicializar recursos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Inicializar sistema de notificaciones básico (sin usuario específico)
        inicializarSistemaNotificaciones();

        // Agregar shutdown hook para cerrar sesión al salir
        agregarShutdownHook();

        // Iniciar la aplicación
        iniciarAplicacion();
    }

    private static void mostrarInfoInicio() {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🏫 " + APP_NAME);
        System.out.println("📋 Versión: " + APP_VERSION);
        System.out.println("📅 Fecha: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("☕ Java: " + System.getProperty("java.version"));
        System.out.println("💻 SO: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println();
    }

    private static void configurarLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private static void verificarYActualizarAplicacion() {
        // Verificar actualizaciones usando el ActualizadorApp
        ActualizadorApp.verificarActualizaciones();
    }

    private static void inicializarSistemaNotificaciones() {
        try {
            // Pre-inicialización del sistema de notificaciones
            // Esto asegura que los servicios estén disponibles desde el inicio
            System.out.println("🔔 Inicializando sistema de notificaciones...");
            
            // El sistema se inicializará completamente cuando el usuario haga login
            // Esta es solo una pre-inicialización para verificar que las clases estén disponibles
            NotificationCore.NotificationService.getInstance();
            
            System.out.println("✅ Sistema de notificaciones pre-inicializado correctamente");
            
        } catch (Exception e) {
            System.err.println("⚠️ Advertencia: Error pre-inicializando sistema de notificaciones: " + e.getMessage());
            // No es crítico, la aplicación puede continuar
        }
    }

    private static void agregarShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Application-Shutdown") {
            @Override
            public void run() {
                try {
                    System.out.println("🔄 Cerrando aplicación...");
                    
                    // Cerrar sistema de notificaciones consolidado
                    try {
                        NotificationCore.shutdownSystem();
                        System.out.println("✅ Sistema de notificaciones cerrado");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error cerrando sistema de notificaciones: " + e.getMessage());
                    }

                    // Cerrar autenticación
                    try {
                        GoogleAuthenticator authenticator = new GoogleAuthenticator();
                        authenticator.logout();
                        System.out.println("✅ Autenticación cerrada");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error cerrando autenticación: " + e.getMessage());
                    }

                    System.out.println("✅ Aplicación cerrada correctamente");
                    
                } catch (Exception e) {
                    System.err.println("❌ Error general al cerrar la aplicación: " + e.getMessage());
                }
            }
        });
    }

    private static void iniciarAplicacion() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("🚀 Iniciando interfaz de usuario...");
                    
                    JFrame loginFrame = new LoginForm();
                    loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    loginFrame.setVisible(true);
                    
                    System.out.println("✅ Aplicación iniciada correctamente");
                    
                } catch (Exception e) {
                    System.err.println("❌ Error iniciando la aplicación: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Mostrar error al usuario
                    JOptionPane.showMessageDialog(null,
                            "Error crítico al iniciar la aplicación:\n" + e.getMessage(),
                            "Error de Inicio", JOptionPane.ERROR_MESSAGE);
                            
                    System.exit(1);
                }
            }
        });
    }

}
