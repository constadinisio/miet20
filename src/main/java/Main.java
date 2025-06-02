package main.java;

import javax.swing.*;
import main.java.utils.ResourceManager;
import main.java.updater.ActualizadorApp;
import main.java.views.login.GoogleAuthenticator;
import main.java.views.login.LoginForm;

public class Main {

    public static void main(String args[]) {
        // Configurar look and feel
        configurarLookAndFeel();

        // Verificar actualizaciones antes de iniciar la aplicación
        //verificarYActualizarAplicacion();
        //Llamado al ResourceManager para que se ejecute y haga la comprobación de las imagenes
        try {
            // Inicializar el gestor de recursos
            ResourceManager.initialize();
        } catch (Exception e) {
            // Solo mostrar un diálogo en caso de error crítico
            JOptionPane.showMessageDialog(null,
                    "Error al inicializar recursos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Agregar shutdown hook para cerrar sesión al salir
        agregarShutdownHook();

        // Iniciar la aplicación
        iniciarAplicacion();

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

    private static void agregarShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    // Cerrar sistema de notificaciones
                    main.java.utils.NotificationManager.getInstance().dispose();

                    // Cerrar autenticación
                    GoogleAuthenticator authenticator = new GoogleAuthenticator();
                    authenticator.logout();

                    System.out.println("Aplicación cerrada correctamente");
                } catch (Exception e) {
                    System.err.println("Error al cerrar la aplicación: " + e.getMessage());
                }
            }
        });
    }

    private static void iniciarAplicacion() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame loginFrame = new LoginForm();
                loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                loginFrame.setVisible(true);
            }
        });
    }

}
