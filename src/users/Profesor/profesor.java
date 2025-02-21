package users.Profesor;

import java.sql.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.JOptionPane;
import login.Conexion;
import users.Profesor.AsistenciaProfesorPanel;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class profesor extends javax.swing.JFrame {

    // Ahora solo declaramos la variable de conexión
    private Connection conect;
    private int profesorId;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;

    public profesor(int profesorId) {
        this.profesorId = profesorId;
        initComponents();
        probar_conexion();
        rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, "src/images/logo et20 buena calidad.png");
        rsscalelabel.RSScaleLabel.setScaleLabel(fondoHome, "src/images/5c994f25d361a_1200.jpg");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        System.out.println("Profesor ID en Profesor.java: " + this.profesorId); // Para depuración
    }

    private void probar_conexion() {
        conect = Conexion.getInstancia().getConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    private void cargarCursosYMaterias() {
        try {
            String query
                    = "SELECT DISTINCT c.id as curso_id, c.anio, c.division, "
                    + "m.id as materia_id, m.nombre as materia_nombre "
                    + "FROM cursos c "
                    + "JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id "
                    + "JOIN materias m ON pcm.materia_id = m.id "
                    + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY c.anio, c.division, m.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            DefaultMutableTreeNode cursoNode = null;
            int currentCursoId = -1;

            while (rs.next()) {
                int cursoId = rs.getInt("curso_id");

                if (cursoId != currentCursoId) {
                    // Nuevo curso
                    String cursoText = rs.getInt("anio") + "°" + rs.getInt("division");
                    CursoNode cursoInfo = new CursoNode(cursoId, cursoText);
                    cursoNode = new DefaultMutableTreeNode(cursoInfo);
                    rootNode.add(cursoNode);
                    currentCursoId = cursoId;
                }

                // Agregar materia al curso actual
                int materiaId = rs.getInt("materia_id");
                String materiaNombre = rs.getString("materia_nombre");
                MateriaNode materiaInfo = new MateriaNode(materiaId, materiaNombre);
                cursoNode.add(new DefaultMutableTreeNode(materiaInfo));
            }

            treeModel.reload();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar cursos y materias: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clases auxiliares para el árbol
    private static class CursoNode {

        private final int id;
        private final String texto;

        public CursoNode(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private static class MateriaNode {

        private final int id;
        private final String nombre;

        public MateriaNode(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public void updateLabels(String nombreCompleto) {
        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: Profesor");
    }
    
    private void abrirLibroDeTemas() {
        libroTemas temario = new libroTemas();  // Pasamos el ID
        temario.setVisible(true);
        this.dispose();  // Opcional: cierra la ventana actual si no la necesitas abierta
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        imagenLogo = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        botpre = new javax.swing.JButton();
        botnot = new javax.swing.JButton();
        labelFotoPerfil = new javax.swing.JLabel();
        labelNomApe = new javax.swing.JLabel();
        labelRol = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        botpre1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelPrincipal.setBackground(new java.awt.Color(204, 204, 204));
        panelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        panelPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        panelPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        panelPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        panelPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome);

        panelPrincipal.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo et20 buena calidad.png"))); // NOI18N
        imagenLogo.setText("imagenLogo");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(imagenLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(imagenLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        botpre.setBackground(new java.awt.Color(51, 153, 255));
        botpre.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botpre.setForeground(new java.awt.Color(255, 255, 255));
        botpre.setText("ASISTENCIAS");
        botpre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botpreActionPerformed(evt);
            }
        });

        botnot.setBackground(new java.awt.Color(51, 153, 255));
        botnot.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botnot.setForeground(new java.awt.Color(255, 255, 255));
        botnot.setText("NOTAS");
        botnot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botnotActionPerformed(evt);
            }
        });

        labelFotoPerfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-user-96.png"))); // NOI18N

        labelNomApe.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelNomApe.setForeground(new java.awt.Color(255, 255, 255));

        labelRol.setBackground(new java.awt.Color(255, 255, 255));
        labelRol.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelRol.setForeground(new java.awt.Color(255, 255, 255));

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

        botpre1.setBackground(new java.awt.Color(51, 153, 255));
        botpre1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botpre1.setForeground(new java.awt.Color(255, 255, 255));
        botpre1.setText("LIBRO DE TEMAS");
        botpre1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botpre1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(jButton1)
                            .addGap(22, 22, 22))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(labelNomApe)
                            .addGap(77, 77, 77)
                            .addComponent(labelFotoPerfil)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(labelRol)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(botpre1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(58, 58, 58))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(labelNomApe))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(labelFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botpre1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botpreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botpreActionPerformed
      try {
        // Panel superior para selección
        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
        JComboBox<String> comboMaterias = new JComboBox<>();
        Map<String, int[]> materiaIds = new HashMap<>();

        String query = 
            "SELECT DISTINCT c.id as curso_id, CONCAT(c.anio, '°', c.division) as curso, " +
            "m.id as materia_id, m.nombre as materia " +
            "FROM cursos c " +
            "JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id " +
            "JOIN materias m ON pcm.materia_id = m.id " +
            "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' " +
            "ORDER BY c.anio, c.division, m.nombre";
            
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setInt(1, profesorId);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            String item = rs.getString("curso") + " - " + rs.getString("materia");
            comboMaterias.addItem(item);
            materiaIds.put(item, new int[]{rs.getInt("curso_id"), rs.getInt("materia_id")});
        }

        comboMaterias.setPreferredSize(new Dimension(300, 30));
        panelSeleccion.add(lblSeleccion);
        panelSeleccion.add(comboMaterias);

        // Listener para el combo
        comboMaterias.addActionListener(e -> {
    String seleccion = (String)comboMaterias.getSelectedItem();
    System.out.println("Selección: " + seleccion); // Debug

    if (seleccion != null) {
        try {
            int[] ids = materiaIds.get(seleccion);
            System.out.println("CursoId: " + ids[0] + ", MateriaId: " + ids[1]); // Debug
            
            AsistenciaProfesorPanel panelAsistencia = new AsistenciaProfesorPanel(
                profesorId,
                ids[0], // cursoId
                ids[1]  // materiaId
            );
            
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout()); // Importante: establecer el layout
            panelPrincipal.add(panelAsistencia, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
        } catch (Exception ex) {
            System.out.println("Error completo: "); // Debug
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar panel: " + ex.getMessage());
        }
    }
});

        panelPrincipal.removeAll();
        panelPrincipal.add(panelSeleccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, panelPrincipal.getWidth(), 50));
        panelPrincipal.revalidate();
        panelPrincipal.repaint();

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error al cargar los cursos: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_botpreActionPerformed

    private void botnotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botnotActionPerformed
        //    vernotas verno = new vernotas();
        //    verno.setVisible(true);
        // this.setVisible(false);
    }//GEN-LAST:event_botnotActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void botpre1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botpre1ActionPerformed
        abrirLibroDeTemas();
    }//GEN-LAST:event_botpre1ActionPerformed

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
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Asumiendo que el ID 1 es un profesor válido para pruebas
                new profesor(1).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botnot;
    private javax.swing.JButton botpre;
    private javax.swing.JButton botpre1;
    private javax.swing.JLabel fondoHome;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelFotoPerfil;
    private javax.swing.JLabel labelNomApe;
    private javax.swing.JLabel labelRol;
    private javax.swing.JPanel panelPrincipal;
    // End of variables declaration//GEN-END:variables
}
