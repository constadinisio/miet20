/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package users.Admin;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import login.Conexion;

/**
 *
 * @author nico_
 */
public class admin extends javax.swing.JFrame {
private String rolUsuario;  // Nueva variable
private Connection conect;
private final javax.swing.JPanel mainPanel;
private javax.swing.JPanel usuariosPendientesPanel;
private javax.swing.JPanel gestionUsuariosPanel;
private final javax.swing.JPanel gestionCursosPanel;
    
    
    public admin() {
        initComponents();
    probar_conexion();
    
    // Escalar imágenes
    rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo1, "src/images/logo et20 buena calidad.png");
    rsscalelabel.RSScaleLabel.setScaleLabel(fondoHome1, "src/images/5c994f25d361a_1200.jpg");
    
    
        
        // Asegurarse que los labels tengan tamaño
    imagenLogo1.setSize(205, 212);
    fondoHome1.setSize(700, 565);
    
    // Escalar las imágenes
    rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo1, "src/images/logo et20 buena calidad.png");
    rsscalelabel.RSScaleLabel.setScaleLabel(fondoHome1, "src/images/5c994f25d361a_1200.jpg");
    
    // Inicializar paneles de funcionalidad
    mainPanel = new javax.swing.JPanel();
   mainPanel.setLayout(new java.awt.CardLayout());
    
   usuariosPendientesPanel = new javax.swing.JPanel();
    gestionUsuariosPanel = new javax.swing.JPanel();
    gestionCursosPanel = new javax.swing.JPanel();
    
    mainPanel.add(usuariosPendientesPanel, "usuariosPendientes");
    mainPanel.add(gestionUsuariosPanel, "gestionUsuarios");
    mainPanel.add(gestionCursosPanel, "gestionCursos");
    
    // Agregar mainPanel al panel principal
     jPanel1.add(mainPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        
        
        
        btnUsuariosPendientes = new javax.swing.JButton();
        btnUsuariosPendientes.setBackground(new java.awt.Color(51, 153, 255));
        btnUsuariosPendientes.setFont(new java.awt.Font("Arial", 1, 14));
        btnUsuariosPendientes.setForeground(new java.awt.Color(255, 255, 255));
        btnUsuariosPendientes.setText("USUARIOS PENDIENTES");
        btnUsuariosPendientes.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosPendientesActionPerformed(evt);
            }
        });

        btnGestionUsuarios = new javax.swing.JButton();
        btnGestionUsuarios.setBackground(new java.awt.Color(51, 153, 255));
        btnGestionUsuarios.setFont(new java.awt.Font("Arial", 1, 14));
        btnGestionUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        btnGestionUsuarios.setText("GESTIÓN USUARIOS");
        btnGestionUsuarios.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionUsuariosActionPerformed(evt);
            }
        });

        btnGestionCursos = new javax.swing.JButton();
        btnGestionCursos.setBackground(new java.awt.Color(51, 153, 255));
        btnGestionCursos.setFont(new java.awt.Font("Arial", 1, 14));
        btnGestionCursos.setForeground(new java.awt.Color(255, 255, 255));
        btnGestionCursos.setText("GESTIÓN CURSOS");
        btnGestionCursos.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionCursosActionPerformed(evt);
            }
        });
    }

    
    public admin(String nombre, String apellido, String rol) {
    // Llamar al constructor sin parámetros primero
    this();
    
    // Guardar y mostrar los datos del usuario
    this.rolUsuario = rol;
    labelNomApe.setText(nombre + " " + apellido);
    labelRol.setText("Rol: " + rol);
}

    public void updateLabels(String nombreCompleto, String rolTexto) {
        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: " + rolTexto);
    }
    public void updateFotoPerfil(String fotoUrl) {
    if (fotoUrl != null && !fotoUrl.isEmpty()) {
        try {
            URL url = new URL(fotoUrl);
            Image imagen = ImageIO.read(url);
            Image imagenRedimensionada = imagen.getScaledInstance(96, 96, Image.SCALE_SMOOTH);
            labelFotoPerfil.setIcon(new ImageIcon(imagenRedimensionada));  // asumiendo que jLabel2 es tu label de foto de perfil
        } catch (IOException e) {
            e.printStackTrace();
            // Si hay error, mantener una imagen por defecto
            labelFotoPerfil.setIcon(new ImageIcon(getClass().getResource("/images/icons8-user-96.png")));
        }
    }
}

    private void probar_conexion() {
        conect = Conexion.getInstancia().getConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    private void mostrarPanel(String nombrePanel) {
        java.awt.CardLayout cl = (java.awt.CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, nombrePanel);
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            dispose();
            new login.login().setVisible(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        labelFotoPerfil = new javax.swing.JLabel();
        labelNomApe = new javax.swing.JLabel();
        labelRol = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btnUsuariosPendientes = new javax.swing.JButton();
        btnGestionUsuarios = new javax.swing.JButton();
        btnGestionCursos = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        imagenLogo1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome1.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        labelFotoPerfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-user-96.png"))); // NOI18N

        labelNomApe.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelNomApe.setForeground(new java.awt.Color(255, 255, 255));

        labelRol.setBackground(new java.awt.Color(255, 255, 255));
        labelRol.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelRol.setForeground(new java.awt.Color(255, 255, 255));
        labelRol.setText("Rol:");

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/loogout48.png"))); // NOI18N
        jButton1.setText("CERRAR SESIÓN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnUsuariosPendientes.setBackground(new java.awt.Color(51, 153, 255));
        btnUsuariosPendientes.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnUsuariosPendientes.setForeground(new java.awt.Color(255, 255, 255));
        btnUsuariosPendientes.setText("USUARIOS PENDIENTES");
        btnUsuariosPendientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosPendientesActionPerformed(evt);
            }
        });

        btnGestionUsuarios.setBackground(new java.awt.Color(51, 153, 255));
        btnGestionUsuarios.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnGestionUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        btnGestionUsuarios.setText("GESTION USUARIOS");
        btnGestionUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionUsuariosActionPerformed(evt);
            }
        });

        btnGestionCursos.setBackground(new java.awt.Color(51, 153, 255));
        btnGestionCursos.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnGestionCursos.setForeground(new java.awt.Color(255, 255, 255));
        btnGestionCursos.setText("GESTION CURSOS");
        btnGestionCursos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionCursosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelNomApe)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(labelRol)))
                .addContainerGap(151, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGestionCursos, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGestionUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(jButton1)
                            .addGap(22, 22, 22))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(labelFotoPerfil)
                            .addGap(92, 92, 92)))))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addContainerGap(17, Short.MAX_VALUE)
                    .addComponent(btnUsuariosPendientes)
                    .addGap(61, 61, 61)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelNomApe)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGestionCursos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnGestionUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jButton1)
                .addGap(15, 15, 15))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addContainerGap(197, Short.MAX_VALUE)
                    .addComponent(btnUsuariosPendientes, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(221, 221, 221)))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        imagenLogo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo et20 buena calidad.png"))); // NOI18N
        imagenLogo1.setText("imagenLogo");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(imagenLogo1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(imagenLogo1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int confirmacion = JOptionPane.showConfirmDialog(this,
        "¿Está seguro que desea cerrar sesión?",
        "Confirmar Cierre de Sesión",
        JOptionPane.YES_NO_OPTION);
        
    if (confirmacion == JOptionPane.YES_OPTION) {
        dispose();
        new login.login().setVisible(true);
    }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnUsuariosPendientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosPendientesActionPerformed
     // Remover el panel actual
    jPanel1.removeAll();
    
    // Crear y configurar el panel de usuarios pendientes
    usuariosPendientesPanel = new GestionUsuariosPanel();
    
    // Agregar el nuevo panel usando AbsoluteLayout constraints
    jPanel1.add(usuariosPendientesPanel, 
        new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, 565));
    
    // Actualizar la vista
    jPanel1.revalidate();
    jPanel1.repaint();
    
    // Para debug
    System.out.println("Panel agregado");
    }//GEN-LAST:event_btnUsuariosPendientesActionPerformed

    private void btnGestionUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionUsuariosActionPerformed
          // Remover el panel actual
    jPanel1.removeAll();
    
    // Crear y configurar el panel de gestión de usuarios
    gestionUsuariosPanel = new GestionUsuariosPanel();
    
    // Agregar el nuevo panel usando AbsoluteLayout constraints
    jPanel1.add(gestionUsuariosPanel, 
        new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, 565));
    
    // Actualizar la vista
    jPanel1.revalidate();
    jPanel1.repaint();
    
    System.out.println("Panel de gestión de usuarios agregado");
    }//GEN-LAST:event_btnGestionUsuariosActionPerformed

    private void btnGestionCursosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionCursosActionPerformed
        mostrarPanel("gestionCursos");
    }//GEN-LAST:event_btnGestionCursosActionPerformed

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
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
            java.util.logging.Logger.getLogger(admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                       new admin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGestionCursos;
    private javax.swing.JButton btnGestionUsuarios;
    private javax.swing.JButton btnUsuariosPendientes;
    private javax.swing.JLabel fondoHome1;
    private javax.swing.JLabel imagenLogo1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelFotoPerfil;
    private javax.swing.JLabel labelNomApe;
    private javax.swing.JLabel labelRol;
    // End of variables declaration//GEN-END:variables
}