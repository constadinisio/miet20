package login;


import Alumnos.alumnos;
import conexion.conexionMysql;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class login extends javax.swing.JFrame {

    conexionMysql conexion = new conexionMysql();
    Connection conectar = conexion.conectar();

    public login() {
    initComponents();
    rsscalelabel.RSScaleLabel.setScaleLabel(jLabel1, "src/images/logo et20 buena calidad.png");
    rsscalelabel.RSScaleLabel.setScaleLabel(jLabel2, "src/Assets/mail-icon.png");
    
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
        jLabel8 = new javax.swing.JLabel();
        panel1 = new java.awt.Panel();
        panelLogin = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        campoNombre = new javax.swing.JTextField();
        campoContraseña = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        botonLogin = new javax.swing.JButton();
        botonGoogle = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        jLabel5.setText("jLabel5");

        jLabel4.setText("jLabel4");

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        jLabel8.setText("jLabel7");

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Segoe UI Variable", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(21, 24, 43));
        jLabel3.setText("Usuario");

        campoNombre.setText("Nombre");
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
        botonLogin.setText("Entrar");
        botonLogin.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonLoginActionPerformed(evt);
            }
        });

        botonGoogle.setText("GOOGLE");
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
                .addGap(366, 366, 366)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(152, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonGoogle)
                .addGap(54, 54, 54))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(149, 149, 149)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(botonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(campoNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addComponent(campoContraseña))
                    .addContainerGap(149, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonGoogle))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(225, 225, 225)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(67, 67, 67))
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
                    .addGap(35, 35, 35)
                    .addComponent(botonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(46, Short.MAX_VALUE)))
        );

        panelLogin.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 250, -1, -1));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        jLabel7.setText("jLabel7");
        panelLogin.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 790, -1, -1));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        jLabel9.setText("jLabel7");
        panelLogin.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo et20 buena calidad.png"))); // NOI18N
        jLabel1.setAutoscrolls(true);
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelLogin.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 130, 106, 104));

        getContentPane().add(panelLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 760, 860));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoNombreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoNombreFocusGained
        if (campoNombre.getText().equals("Nombre")) {
            campoNombre.setText(null);
            campoNombre.requestFocus();
        }
    }//GEN-LAST:event_campoNombreFocusGained

    private void campoNombreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoNombreFocusLost
        if (campoNombre.getText().length() == 0) {
            campoNombre.setText("Nombre");
        }
    }//GEN-LAST:event_campoNombreFocusLost

    private void campoNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNombreActionPerformed

    private void campoContraseñaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoContraseñaFocusGained
        if (campoContraseña.getText().equals("Contraseña")) {
            campoContraseña.setText(null);
            campoContraseña.requestFocus();
        }
    }//GEN-LAST:event_campoContraseñaFocusGained

    private void campoContraseñaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoContraseñaFocusLost
        if (campoContraseña.getText().length() == 0) {
            campoContraseña.setText("Contraseña");
        }
    }//GEN-LAST:event_campoContraseñaFocusLost

    private void campoContraseñaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoContraseñaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoContraseñaActionPerformed
    /*
    private void botonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonLoginActionPerformed
        ClaseLogin objetoClaseLogin = new ClaseLogin();
        objetoClaseLogin.comprobarUsuario(campoNombre, campoContraseña, this);
    }//GEN-LAST:event_botonLoginActionPerformed
*/
    private void botonGoogleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonGoogleActionPerformed
        
        try {
            GoogleAuthenticator authenticator = new GoogleAuthenticator();
            UserSession session = authenticator.authenticateUser();
            
            Connection conn = Conexion.getInstancia().getConexion();
            if (conn == null) {
                JOptionPane.showMessageDialog(null, 
                    "No se pudo establecer conexión con la base de datos",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if ("4".equals(session.getRole())) {
                alumnos.setAuthenticatedSession(true);
                alumnos alumnoForm = new alumnos();
                updateUserInterface(alumnoForm, session);
                alumnoForm.setVisible(true);
                this.dispose();
            } else if ("2".equals(session.getRole())) {
                // Aquí irá la lógica para profesores cuando la implementes
            } else if ("pending".equals(session.getRole())) {
                // Ya muestra el mensaje de pendiente automáticamente
            } else {
                JOptionPane.showMessageDialog(null, 
                    "No tienes permisos para acceder.",
                    "Acceso Denegado",
                    JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, 
                "Error durante la autenticación: " + ex.getMessage(),
                "Error de Autenticación",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUserInterface(JFrame form, UserSession session) {
        try {
            Connection conn = Conexion.getInstancia().getConexion();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.name, u.apellido, a.anio, a.division " +
                "FROM users u " +
                "LEFT JOIN alumnos a ON u.id = a.user_id " +
                "WHERE u.email = ?"
            );
            stmt.setString(1, session.getEmail());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nombreCompleto = rs.getString("name") + " " + rs.getString("apellido");
                String cursoDiv = rs.getInt("anio") + "°" + rs.getString("division") + "°";

                // Actualizar los labels en la forma correspondiente
                if (form instanceof alumnos) {
                    ((alumnos)form).updateLabels(nombreCompleto, session.getRole(), cursoDiv);
                }
                // Agregar más casos para otros tipos de formas (profesor, etc.)
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonGoogle;
    private javax.swing.JButton botonLogin;
    private javax.swing.JPasswordField campoContraseña;
    private javax.swing.JTextField campoNombre;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private java.awt.Panel panel1;
    private javax.swing.JPanel panelLogin;
    // End of variables declaration//GEN-END:variables
}