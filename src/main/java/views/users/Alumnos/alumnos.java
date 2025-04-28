package main.java.views.users.Alumnos;

import java.sql.*;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import java.io.IOException;
import main.java.database.Conexion;
import main.java.utils.MenuBarManager;
import main.java.utils.ResourceManager;
import main.java.utils.uiUtils;

/**
 * Interfaz principal para usuarios con rol de Alumno.
 *
 * Características principales: - Muestra información del alumno - Gestiona
 * conexión a base de datos - Configura elementos visuales de la interfaz
 *
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [12/03/2025]
 */
public class alumnos extends javax.swing.JFrame {

    // Ahora solo declaramos la variable de conexión
    Connection conect;
    private int alumnoId;

    /**
     * Constructor principal de la interfaz de alumno.
     *
     * Inicializa componentes: - Verifica conexión a base de datos - Escala
     * imágenes de la interfaz
     */
    public alumnos(int alumnoId) {
        this.alumnoId = alumnoId;
        initComponents();
        uiUtils.configurarVentana(this);
        probar_conexion();  // Usamos la conexión aquí
        rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, ResourceManager.getImagePath("logo_et20_max.png"));
        rsscalelabel.RSScaleLabel.setScaleLabel(fondoHome, ResourceManager.getImagePath("5c994f25d361a_1200.jpg"));

        // Inicializar el gestor de roles
        new MenuBarManager(alumnoId, this);
    }
    // Mantén el constructor sin parámetros para compatibilidad

    public alumnos() {
        this(-1); // Usa un valor por defecto o inválido
    }

    private int obtenerIdAlumnoActual() {
        // Implementa la lógica para obtener el ID del alumno actual
        // Podrías usar una clase de sesión o recuperarlo de la base de datos
        return alumnoId;
    }

    /**
     * Actualiza las etiquetas de información del alumno.
     *
     * @param nombreCompleto Nombre completo del alumno
     * @param rolTexto Descripción del rol
     * @param cursoDiv Curso y división del alumno
     */
    public void updateLabels(String nombreCompleto, String rolTexto, String cursoDiv) {
        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: " + rolTexto);
        labelCursoDiv.setText("Curso: " + cursoDiv);
    }

    /**
     * Verifica la conexión a la base de datos.
     *
     * Utiliza el patrón Singleton para obtener la conexión. Muestra un mensaje
     * de error si no se puede establecer conexión.
     */
    private void probar_conexion() {
        // Obtenemos la conexión desde el Singleton
        conect = Conexion.getInstancia().verificarConexion();
        // Verificamos si la conexión es válida
        if (conect != null) {
        } else {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        imagenLogo = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        botpre = new javax.swing.JButton();
        botnot = new javax.swing.JButton();
        labelFotoPerfil = new javax.swing.JLabel();
        labelNomApe = new javax.swing.JLabel();
        labelRol = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        labelCursoDiv = new javax.swing.JLabel();
        labelRol1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/logo_et20_max.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(imagenLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(imagenLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));
        jPanel4.setPreferredSize(new java.awt.Dimension(278, 454));

        botpre.setBackground(new java.awt.Color(51, 153, 255));
        botpre.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botpre.setForeground(new java.awt.Color(255, 255, 255));
        botpre.setText("ASISTENCIAS");
        botpre.setMaximumSize(new java.awt.Dimension(193, 24));
        botpre.setMinimumSize(new java.awt.Dimension(193, 24));
        botpre.setPreferredSize(new java.awt.Dimension(193, 24));
        botpre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botpreActionPerformed(evt);
            }
        });

        botnot.setBackground(new java.awt.Color(51, 153, 255));
        botnot.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botnot.setForeground(new java.awt.Color(255, 255, 255));
        botnot.setText("NOTAS");
        botnot.setMaximumSize(new java.awt.Dimension(193, 24));
        botnot.setMinimumSize(new java.awt.Dimension(193, 24));
        botnot.setPreferredSize(new java.awt.Dimension(193, 24));
        botnot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botnotActionPerformed(evt);
            }
        });

        labelFotoPerfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/icons8-user-96.png"))); // NOI18N

        labelNomApe.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelNomApe.setForeground(new java.awt.Color(255, 255, 255));

        labelRol.setBackground(new java.awt.Color(255, 255, 255));
        labelRol.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelRol.setForeground(new java.awt.Color(255, 255, 255));
        labelRol.setText("Rol:");

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/loogout48.png"))); // NOI18N
        jButton1.setText("CERRAR SESIÓN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        labelCursoDiv.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelCursoDiv.setForeground(new java.awt.Color(255, 255, 255));
        labelCursoDiv.setText("Curso:");

        labelRol1.setBackground(new java.awt.Color(255, 255, 255));
        labelRol1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelRol1.setForeground(new java.awt.Color(255, 255, 255));
        labelRol1.setText("Nombre:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(labelNomApe)
                .addGap(18, 18, 18)
                .addComponent(labelFotoPerfil)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRol)
                            .addComponent(labelCursoDiv)
                            .addComponent(labelRol1))
                        .addGap(41, 41, 41))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(22, 22, 22))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(labelNomApe)
                        .addGap(50, 50, 50))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(labelFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(labelRol1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelCursoDiv)
                .addGap(18, 18, 18)
                .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(jButton1)
                .addGap(29, 29, 29))
        );

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 640, -1, 75));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botnotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botnotActionPerformed
        vernotas verno = new vernotas();
        verno.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_botnotActionPerformed

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
                labelFotoPerfil.setIcon(new ImageIcon(getClass().getResource(ResourceManager.getImagePath("icons8-user-96.png"))));
            }
        }
    }

    private void botpreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botpreActionPerformed
        verasisten veras = new verasisten();
        veras.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_botpreActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(alumnos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(alumnos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(alumnos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(alumnos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new alumnos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botnot;
    private javax.swing.JButton botpre;
    private javax.swing.JLabel fondoHome;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelCursoDiv;
    private javax.swing.JLabel labelFotoPerfil;
    private javax.swing.JLabel labelNomApe;
    private javax.swing.JLabel labelRol;
    private javax.swing.JLabel labelRol1;
    // End of variables declaration//GEN-END:variables
}
