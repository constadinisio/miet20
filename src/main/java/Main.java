package main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import main.java.utils.ResponsiveUtils;
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

        // Configurar UI responsiva
        setupResponsiveUI();

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
                    GoogleAuthenticator authenticator = new GoogleAuthenticator();
                    authenticator.logout();
                } catch (Exception e) {
                    System.err.println("Error al cerrar sesión durante el apagado: " + e.getMessage());
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

    /**
     * Configura la UI para que sea responsive en todas las ventanas
     */
    public static void setupResponsiveUI() {
        // Interceptar la creación de ventanas para hacerlas responsivas
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event.getID() == WindowEvent.WINDOW_OPENED) {
                    Window window = (Window) event.getSource();
                    if (window instanceof JFrame) {
                        JFrame frame = (JFrame) window;

                        // Aplicar configuración responsive de forma recursiva
                        makeAllPanelsResponsive(frame.getContentPane());
                    }
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    private static void makeAllPanelsResponsive(Container container) {
        // Procesar este contenedor si es un panel
        if (container instanceof JPanel) {
            ResponsiveUtils.makeResponsive((JPanel) container);
        }

        // Procesar recursivamente todos los componentes
        for (Component comp : container.getComponents()) {
            if (comp instanceof Container) {
                makeAllPanelsResponsive((Container) comp);
            }
        }
    }

}
