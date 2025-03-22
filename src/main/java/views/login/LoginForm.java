package main.java.views.login;

import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import main.java.views.users.Admin.admin;
import main.java.views.users.Alumnos.alumnos;
import main.java.views.users.Preceptor.preceptor;
import main.java.views.users.Profesor.profesor;
import main.java.views.users.Attp.attp;
import main.java.database.Conexion;

/**
 * Clase principal de inicio de sesión para la plataforma educativa.
 * Gestiona la autenticación de usuarios mediante credenciales locales o Google.
 * 
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [12/03/2025]
 */

public class LoginForm extends javax.swing.JFrame {

    // Conexión a la base de datos
    Connection conect;
    private int profesorId;

    /**
     * Método para probar la conexión a la base de datos.
     * Utiliza el patrón Singleton para obtener la conexión.
     */
    private void probar_conexion() {
        // Obtener la conexión desde el Singleton
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    /**
     * Constructor de la clase login.
     * Inicializa los componentes de la interfaz y configura el cierre de la aplicación.
     */
    public LoginForm() {
        initComponents();
        rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, "src/main/resources/images/logo_et20_min.png");
        rsscalelabel.RSScaleLabel.setScaleLabel(bannerImg1, "src/main/resources/images/banner-et20.png");
        rsscalelabel.RSScaleLabel.setScaleLabel(bannerImg2, "src/main/resources/images/banner-et20.png");
        
// Configurar el campo de contraseña para mostrar texto plano inicialmente
        campoContraseña.setText("****************");
        campoContraseña.setEchoChar((char)0);
        // Desactivar el comportamiento por defecto del botón X
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        // Agregar el listener para el botón de cerrar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                cerrarPrograma();
            }
        });
    }

    /**
     * Método para cerrar la aplicación de manera segura.
     * Realiza logout y termina la ejecución del programa.
     */
    private void cerrarPrograma() {
        try {
            GoogleAuthenticator authenticator = new GoogleAuthenticator();
            authenticator.logout();
            System.exit(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cerrar sesión: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        panel1 = new java.awt.Panel();
        panelLogin = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        campoNombre = new javax.swing.JTextField();
        campoContraseña = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        botonLogin = new javax.swing.JButton();
        botonGoogle = new javax.swing.JButton();
        bannerImg2 = new javax.swing.JLabel();
        bannerImg1 = new javax.swing.JLabel();
        imagenLogo = new javax.swing.JLabel();

        jLabel5.setText("jLabel5");

        jLabel4.setText("jLabel4");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 204));
        setExtendedState(6);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelLogin.setBackground(new java.awt.Color(249, 245, 232));
        panelLogin.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setForeground(new java.awt.Color(51, 204, 0));

        jLabel3.setFont(new java.awt.Font("Segoe UI Variable", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(21, 24, 43));
        jLabel3.setText("Usuario");

        campoNombre.setText("Ingrese su mail");
        campoNombre.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        campoNombre.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoNombreFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoNombreFocusLost(evt);
            }
        });
        campoNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNombreActionPerformed(evt);
            }
        });

        campoContraseña.setText("Constraseña");
        campoContraseña.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        campoContraseña.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoContraseñaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoContraseñaFocusLost(evt);
            }
        });
        campoContraseña.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoContraseñaActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI Variable", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(21, 24, 43));
        jLabel2.setText("Contraseña");

        botonLogin.setBackground(new java.awt.Color(103, 136, 255));
        botonLogin.setFont(new java.awt.Font("Segoe UI Variable", 1, 14)); // NOI18N
        botonLogin.setForeground(new java.awt.Color(255, 255, 255));
        botonLogin.setText("ENTRAR");
        botonLogin.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonLoginActionPerformed(evt);
            }
        });

        botonGoogle.setBackground(new java.awt.Color(103, 136, 255));
        botonGoogle.setFont(new java.awt.Font("Segoe UI Variable", 1, 14)); // NOI18N
        botonGoogle.setForeground(new java.awt.Color(255, 255, 255));
        botonGoogle.setText("GOOGLE");
        botonGoogle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonGoogle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonGoogle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonGoogleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(botonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonGoogle, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 146, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(149, 149, 149)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3)
                        .addComponent(campoNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addComponent(campoContraseña))
                    .addContainerGap(149, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(207, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonGoogle, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(45, 45, 45)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(campoNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(campoContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(117, Short.MAX_VALUE)))
        );

        panelLogin.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 250, -1, -1));

        bannerImg2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        panelLogin.add(bannerImg2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 790, 760, -1));

        bannerImg1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        panelLogin.add(bannerImg1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 760, -1));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/logo_et20_min.png"))); // NOI18N
        imagenLogo.setAutoscrolls(true);
        imagenLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelLogin.add(imagenLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 100, 130, 120));

        getContentPane().add(panelLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 760, 860));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void campoNombreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoNombreFocusGained
        if (campoNombre.getText().equals("Ingrese su mail")) {
            campoNombre.setText(null);
            campoNombre.requestFocus();
        }
    }//GEN-LAST:event_campoNombreFocusGained

    private void campoNombreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoNombreFocusLost
         if (campoNombre.getText().isEmpty()) {
            campoNombre.setText("Ingrese su mail");
        }
    }//GEN-LAST:event_campoNombreFocusLost

    private void campoNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNombreActionPerformed

    private void campoContraseñaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoContraseñaFocusGained
          String password = String.valueOf(campoContraseña.getPassword());
    if (password.equals("****************")) {
        campoContraseña.setText("");
        campoContraseña.setEchoChar('•'); // Activar caracteres ocultos
    }
    }//GEN-LAST:event_campoContraseñaFocusGained

    private void campoContraseñaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoContraseñaFocusLost
         String password = String.valueOf(campoContraseña.getPassword());
    if (password.isEmpty()) {
        campoContraseña.setText("****************");
        campoContraseña.setEchoChar((char)0); // Mostrar texto plano
    }
    }//GEN-LAST:event_campoContraseñaFocusLost

    private void campoContraseñaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoContraseñaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoContraseñaActionPerformed

    /**
     * Método de inicio de sesión con credenciales locales.
     * Valida el usuario, obtiene sus datos y redirige según su rol.
     * 
     * @param evt Evento de acción del botón de login
     */
    private void botonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonLoginActionPerformed
        try {
            // Obtener credenciales
            String mail = campoNombre.getText();
            String contrasena = new String(campoContraseña.getPassword());
        
            // Validar campos
            if (mail.equals("Nombre") || contrasena.equals("Contraseña")) {
                JOptionPane.showMessageDialog(this, 
                    "Por favor ingrese usuario y contraseña",
                    "Error de login",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

        // Validar conexión a base de datos    
        if (!validarConexion()) {
            return;
        }

        // Consulta para autenticación
        String query = "SELECT id, nombre, apellido, rol, foto_url FROM usuarios " +
                      "WHERE mail = ? AND contrasena = ?";
        
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setString(1, mail);
        ps.setString(2, contrasena);
        
        // Procesar resultado de autenticación
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            // Crear sesión de usuario
            String nombre = rs.getString("nombre");
            String apellido = rs.getString("apellido");
            int rol = rs.getInt("rol");
            String fotoUrl = rs.getString("foto_url");
            int profesorId = rs.getInt("id");  // Aquí se obtiene el ID del usuario
            
            // Crear sesión de usuario
            UserSession session = new UserSession(nombre, apellido, mail, rol, fotoUrl);
            
            // Manejar el login según el rol
            switch (rol) {
                case 5: // ATTP
                    manejarLoginATTP(session);
                    break;
                case 4: // Alumno
                    manejarLoginAlumno(session);
                    break;
                case 3: // Profesor
                    manejarLoginProfesor(session);
                    break;
                case 2: // Preceptor
                    manejarLoginPreceptor(session);
                    break;
                case 1: // Admin
                    manejarLoginAdmin(session);
                    break;
                case 0: // Pendiente
                    mostrarMensajePendiente();
                    break;
                default:
                    mostrarMensajeAccesoDenegado();
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Usuario o contraseña incorrectos",
                "Error de login",
                JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error al intentar iniciar sesión: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_botonLoginActionPerformed

    private void botonGoogleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonGoogleActionPerformed
        try {
            GoogleAuthenticator authenticator = new GoogleAuthenticator();
            UserSession session = authenticator.authenticateUser();

            if (!validarConexion()) {
                return;
            }

            switch (session.getRol()) {
                case 5: // ATTP
                    manejarLoginATTP(session);
                    break;
                case 4: // Alumno
                    manejarLoginAlumno(session);
                    break;
                case 3: // Profesor
                    manejarLoginProfesor(session);
                    break;
                case 2: // Preceptor
                    manejarLoginPreceptor(session);
                    break;
                case 1: // Admin
                    manejarLoginAdmin(session);
                    break;
                case 0: // Pendiente
                    mostrarMensajePendiente();
                    break;
                default:
                    mostrarMensajeAccesoDenegado();
                    break;
            }
        } catch (Exception ex) {
            manejarErrorAutenticacion(ex);
        }
    }

    private boolean validarConexion() {
        // Obtener una conexión fresca
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo establecer conexión con la base de datos",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void manejarLoginATTP(UserSession session) {
        try {
            // Obtener el ID del profesor
            int attpId = obtenerUsuarioId(session.getEmail());
            if (attpId != -1) {
                attp attpForm = new attp();
                attpForm.updateLabels(session.getNombre() + " " + session.getApellido());
                attpForm.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error al obtener el ID del ATTP",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar la interfaz del profesor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manejarLoginAlumno(UserSession session) {
        alumnos alumnoForm = new alumnos();
        updateUserInterface(alumnoForm, session);
        alumnoForm.setVisible(true);
        this.dispose();
    }

    private void manejarLoginProfesor(UserSession session) {
        try {
            // Obtener el ID del profesor
            int profesorId = obtenerUsuarioId(session.getEmail());
            if (profesorId != -1) {
                profesor profesorForm = new profesor(profesorId);
                profesorForm.updateLabels(session.getNombre() + " " + session.getApellido());
                profesorForm.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error al obtener el ID del profesor",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar la interfaz del profesor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manejarLoginPreceptor(UserSession session) {
        try {
            // Obtener el ID del preceptor
            int preceptorId = obtenerUsuarioId(session.getEmail());
            if (preceptorId != -1) {
                preceptor preceptorForm = new preceptor(preceptorId);
                preceptorForm.updateLabels(session.getNombre() + " " + session.getApellido());
                preceptorForm.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error al obtener el ID del preceptor",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar la interfaz del preceptor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manejarLoginAdmin(UserSession session) {
        // TODO: Implementar cuando esté lista la interfaz de admin

        admin adminForm = new admin(session.getNombre(), session.getApellido(), "Administrador");
        adminForm.setVisible(true);
        this.dispose();
    }

    private void mostrarMensajePendiente() {
        JOptionPane.showMessageDialog(null,
                "Tu cuenta está pendiente de aprobación por el administrador.",
                "Cuenta Pendiente",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarMensajeAccesoDenegado() {
        JOptionPane.showMessageDialog(null,
                "No tienes permisos para acceder al sistema.",
                "Acceso Denegado",
                JOptionPane.WARNING_MESSAGE);
    }

    private void manejarErrorAutenticacion(Exception ex) {
        JOptionPane.showMessageDialog(null,
                "Error durante la autenticación: " + ex.getMessage(),
                "Error de Autenticación",
                JOptionPane.ERROR_MESSAGE);
        System.out.println(ex.getMessage());
    }

    private int obtenerUsuarioId(String mail) throws SQLException {
        String query = "SELECT id FROM usuarios WHERE mail = ?";
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setString(1, mail);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    private void updateUserInterface(JFrame form, UserSession session) {
        try {
            PreparedStatement stmt = conect.prepareStatement(
                    "SELECT nombre, apellido, anio, division, foto_url FROM usuarios WHERE mail = ?"
            );
            stmt.setString(1, session.getEmail());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Obtener nombre y apellido
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");

                // Convertir rol numérico a texto
                String rolTexto;
                switch (session.getRol()) {
                    case 5:
                        rolTexto = "ATTP";
                        break;
                    case 4:
                        rolTexto = "Estudiante";
                        break;
                    case 3:
                        rolTexto = "Profesor";
                        break;
                    case 2:
                        rolTexto = "Preceptor";
                        break;
                    case 1:
                        rolTexto = "Administrador";
                        break;
                    default:
                        rolTexto = "Sin asignar";
                }

                // Obtener curso y división
                String cursoDiv = rs.getString("anio") + "°" + rs.getString("division");

                // Actualizar la interfaz
                if (form instanceof alumnos alumnoForm) {
                    alumnoForm.updateLabels(nombreCompleto, rolTexto, cursoDiv);
                    alumnoForm.updateFotoPerfil(session.getFotoUrl());  // Usar el nuevo método
                } else if (form instanceof admin adminForm) {
                    adminForm.updateLabels(nombreCompleto, rolTexto);
                    adminForm.updateFotoPerfil(session.getFotoUrl());  // Usar el nuevo método
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al cargar datos del usuario: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_botonGoogleActionPerformed

    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginForm().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerImg1;
    private javax.swing.JLabel bannerImg2;
    private javax.swing.JButton botonGoogle;
    private javax.swing.JButton botonLogin;
    private javax.swing.JPasswordField campoContraseña;
    private javax.swing.JTextField campoNombre;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private java.awt.Panel panel1;
    private javax.swing.JPanel panelLogin;
    // End of variables declaration//GEN-END:variables
}
