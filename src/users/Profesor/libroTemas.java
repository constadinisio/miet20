package users.Profesor;

import java.sql.*;
import login.Conexion;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.LocalTime;

// SE NECESITA MÁS DESARROLLO

public class libroTemas extends javax.swing.JFrame {

    private Connection conect;
    private int profesorId;
    private profesor profMenu;

    public void setProfesorId(int profesorId) {
        this.profesorId = profesorId;
    }

    public libroTemas(profesor menuAnterior) {
        initComponents();
        this.profMenu = menuAnterior;
    }

    public libroTemas() {
        initComponents();
        probar_conexion();
        rsscalelabel.RSScaleLabel.setScaleLabel(bannerColor1, "images/banner-et20.png");
        rsscalelabel.RSScaleLabel.setScaleLabel(bannerColor2, "images/banner-et20.png");
    }

    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void firmarAsistencia() {
        try {
            LocalDate fechaActual = LocalDate.now();
            LocalTime horaActual = LocalTime.now();
            String diaSemana = fechaActual.getDayOfWeek().toString().toLowerCase();

            String consulta = "SELECT curso_id, materia_id FROM horarios_materia WHERE profesor_id = ? AND dia_semana = ? AND ? BETWEEN hora_inicio AND hora_fin";
            PreparedStatement ps = conect.prepareStatement(consulta);
            ps.setInt(1, profesorId);
            ps.setString(2, diaSemana);
            ps.setTime(3, Time.valueOf(horaActual));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cursoId = rs.getInt("curso_id");
                int materiaId = rs.getInt("materia_id");

                String insercion = "INSERT INTO firmas_asistencia (profesor_id, curso_id, materia_id, fecha, hora_firma) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psInsert = conect.prepareStatement(insercion);
                psInsert.setInt(1, profesorId);
                psInsert.setInt(2, cursoId);
                psInsert.setInt(3, materiaId);
                psInsert.setDate(4, Date.valueOf(fechaActual));
                psInsert.setTime(5, Time.valueOf(horaActual));

                psInsert.executeUpdate();
                JOptionPane.showMessageDialog(null, "Firma registrada exitosamente.");
            } else {
                JOptionPane.showMessageDialog(null, "No tienes clase en este horario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al registrar la firma.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionVolver() {
        Conexion.getInstancia().verificarConexion();
        this.setVisible(false);
        profMenu.setVisible(true);
    }
    
    private void abrirContenidos() {
        Conexion.getInstancia().verificarConexion();
        tablaContenidos tablaCont = new tablaContenidos(this);
        tablaCont.setVisible(true);
        this.setVisible(false);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bannerColor1 = new javax.swing.JLabel();
        bannerColor2 = new javax.swing.JLabel();
        btnVolver = new javax.swing.JButton();
        btnFirmar = new javax.swing.JButton();
        btnContenidos = new javax.swing.JButton();
        panelInfo = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bannerColor1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        bannerColor2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        btnVolver.setBackground(new java.awt.Color(51, 153, 255));
        btnVolver.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(255, 255, 255));
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });

        btnFirmar.setBackground(new java.awt.Color(51, 153, 255));
        btnFirmar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnFirmar.setForeground(new java.awt.Color(255, 255, 255));
        btnFirmar.setText("FIRMAR");
        btnFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirmarActionPerformed(evt);
            }
        });

        btnContenidos.setBackground(new java.awt.Color(51, 153, 255));
        btnContenidos.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnContenidos.setForeground(new java.awt.Color(255, 255, 255));
        btnContenidos.setText("CONTENIDOS");
        btnContenidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContenidosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 545, Short.MAX_VALUE)
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 435, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bannerColor1, javax.swing.GroupLayout.DEFAULT_SIZE, 903, Short.MAX_VALUE)
            .addComponent(bannerColor2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnContenidos, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bannerColor1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(137, 137, 137)
                        .addComponent(btnFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnContenidos, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addComponent(bannerColor2))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        accionVolver();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirmarActionPerformed
        firmarAsistencia();
    }//GEN-LAST:event_btnFirmarActionPerformed

    private void btnContenidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContenidosActionPerformed
        abrirContenidos();
    }//GEN-LAST:event_btnContenidosActionPerformed

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
            java.util.logging.Logger.getLogger(libroTemas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(libroTemas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(libroTemas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(libroTemas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new libroTemas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerColor1;
    private javax.swing.JLabel bannerColor2;
    private javax.swing.JButton btnContenidos;
    private javax.swing.JButton btnFirmar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JPanel panelInfo;
    // End of variables declaration//GEN-END:variables
}
