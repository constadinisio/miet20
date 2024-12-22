package login; // O el paquete que prefieras

import login.login; // Ajusta según tu paquete de login

public class Main {
    public static void main(String args[]) {
        // Agregar shutdown hook para cerrar sesión al salir
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

        // Configurar look and feel
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

        // Iniciar la aplicación mostrando el login
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new login().setVisible(true);
            }
        });
    }
}