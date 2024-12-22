package Profesor;



import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class modnotas extends javax.swing.JFrame {
    
    // Mantén la conexión abierta a nivel de la clase
    private Connection conect;

    public modnotas() {
        initComponents();
        probar_conexion(); // Aquí obtienes la conexión desde el Singleton
        mostrarDatos();    // Mostrar datos al inicializar
    }

    private void probar_conexion() {
        // Obtenemos la conexión desde el Singleton
        conect = Conexion.getInstancia().getConexion();

        // Verificamos si la conexión es válida
        if (conect != null) {
            System.out.println("Conexión exitosa");
        } else {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    public void mostrarDatos() {
        DefaultTableModel tcliente = new DefaultTableModel();
        tcliente.addColumn("alumno_id");
        tcliente.addColumn("fecha");
        tcliente.addColumn("estado");
        tcliente.addColumn("docente_id");
        tablanotas.setModel(tcliente);

        String[] datos = new String[4];  // Cambié de 6 a 4 columnas

        // Usamos la conexión global (la mantenemos abierta durante toda la instancia)
        try {
            Statement leer = conect.createStatement();
            ResultSet resultado = leer.executeQuery("SELECT * FROM notas");

            while (resultado.next()) {
                datos[0] = resultado.getString(2);
                datos[1] = resultado.getString(3);
                datos[2] = resultado.getString(4);
                datos[3] = resultado.getString(5);

                tcliente.addRow(datos);
            }

            tablanotas.setModel(tcliente);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e + " Error en la consulta");
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label1 = new java.awt.Label();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablanotas = new javax.swing.JTable();
        botreg1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        botreg4 = new javax.swing.JButton();
        botreg5 = new javax.swing.JButton();
        materiaIdCampo = new javax.swing.JTextField();
        idAlumnoCampo = new javax.swing.JTextField();
        evaluacionCampo = new javax.swing.JTextField();
        notaCampo = new javax.swing.JTextField();
        fechaCampo = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();

        label1.setText("label1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        tablanotas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablanotas);

        botreg1.setBackground(new java.awt.Color(102, 153, 255));
        botreg1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botreg1.setForeground(new java.awt.Color(255, 255, 255));
        botreg1.setText("Volver");
        botreg1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botreg1ActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        botreg4.setBackground(new java.awt.Color(102, 153, 255));
        botreg4.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botreg4.setForeground(new java.awt.Color(255, 255, 255));
        botreg4.setText("Modificar");
        botreg4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botreg4ActionPerformed(evt);
            }
        });

        botreg5.setBackground(new java.awt.Color(102, 153, 255));
        botreg5.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botreg5.setForeground(new java.awt.Color(255, 255, 255));
        botreg5.setText("Borrar");
        botreg5.setMaximumSize(new java.awt.Dimension(96, 24));
        botreg5.setMinimumSize(new java.awt.Dimension(96, 24));
        botreg5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botreg5ActionPerformed(evt);
            }
        });

        materiaIdCampo.setText("  ID Materia");
        materiaIdCampo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        materiaIdCampo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                materiaIdCampoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                materiaIdCampoFocusLost(evt);
            }
        });
        materiaIdCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                materiaIdCampoActionPerformed(evt);
            }
        });

        idAlumnoCampo.setText("  ID Alumno");
        idAlumnoCampo.setToolTipText("asdsadsad");
        idAlumnoCampo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        idAlumnoCampo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                idAlumnoCampoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                idAlumnoCampoFocusLost(evt);
            }
        });
        idAlumnoCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idAlumnoCampoActionPerformed(evt);
            }
        });

        evaluacionCampo.setText("  Evaluacion");
        evaluacionCampo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        evaluacionCampo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                evaluacionCampoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                evaluacionCampoFocusLost(evt);
            }
        });
        evaluacionCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluacionCampoActionPerformed(evt);
            }
        });

        notaCampo.setText("  Nota");
        notaCampo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        notaCampo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                notaCampoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                notaCampoFocusLost(evt);
            }
        });
        notaCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notaCampoActionPerformed(evt);
            }
        });

        fechaCampo.setText("  Fecha");
        fechaCampo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        fechaCampo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fechaCampoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fechaCampoFocusLost(evt);
            }
        });
        fechaCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fechaCampoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(botreg4)
                .addGap(18, 18, 18)
                .addComponent(botreg5, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(materiaIdCampo, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .addComponent(evaluacionCampo, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .addComponent(notaCampo, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .addComponent(fechaCampo, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(idAlumnoCampo, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(materiaIdCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notaCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(evaluacionCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fechaCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botreg4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botreg5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(26, 26, 26)
                    .addComponent(idAlumnoCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(241, Short.MAX_VALUE)))
        );

        idAlumnoCampo.getAccessibleContext().setAccessibleName("sdasdsadsad");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
        jLabel3.setText("NOTAS - Modificación");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(14, Short.MAX_VALUE))
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(botreg1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(74, 74, 74))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(35, 35, 35))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(81, 81, 81))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addComponent(botreg1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botreg1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botreg1ActionPerformed
        vernotas log = new vernotas();
        log.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_botreg1ActionPerformed

    private void botreg4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botreg4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_botreg4ActionPerformed

    private void botreg5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botreg5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_botreg5ActionPerformed

    private void materiaIdCampoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_materiaIdCampoFocusGained
        if (materiaIdCampo.getText().equals("Nombre")) {
            materiaIdCampo.setText(null);
            materiaIdCampo.requestFocus();
        }
    }//GEN-LAST:event_materiaIdCampoFocusGained

    private void materiaIdCampoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_materiaIdCampoFocusLost
        if (materiaIdCampo.getText().length()==0) {
            materiaIdCampo.setText("Nombre");
        }
    }//GEN-LAST:event_materiaIdCampoFocusLost

    private void materiaIdCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_materiaIdCampoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_materiaIdCampoActionPerformed

    private void idAlumnoCampoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_idAlumnoCampoFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_idAlumnoCampoFocusGained

    private void idAlumnoCampoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_idAlumnoCampoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_idAlumnoCampoFocusLost

    private void idAlumnoCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idAlumnoCampoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idAlumnoCampoActionPerformed

    private void evaluacionCampoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_evaluacionCampoFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_evaluacionCampoFocusGained

    private void evaluacionCampoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_evaluacionCampoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_evaluacionCampoFocusLost

    private void evaluacionCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluacionCampoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_evaluacionCampoActionPerformed

    private void notaCampoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_notaCampoFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_notaCampoFocusGained

    private void notaCampoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_notaCampoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_notaCampoFocusLost

    private void notaCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notaCampoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_notaCampoActionPerformed

    private void fechaCampoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fechaCampoFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_fechaCampoFocusGained

    private void fechaCampoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fechaCampoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_fechaCampoFocusLost

    private void fechaCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fechaCampoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fechaCampoActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(modnotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(modnotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(modnotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(modnotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new modnotas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botreg1;
    private javax.swing.JButton botreg4;
    private javax.swing.JButton botreg5;
    private javax.swing.JTextField evaluacionCampo;
    private javax.swing.JTextField fechaCampo;
    private javax.swing.JTextField idAlumnoCampo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private java.awt.Label label1;
    private javax.swing.JTextField materiaIdCampo;
    private javax.swing.JTextField notaCampo;
    private javax.swing.JTable tablanotas;
    // End of variables declaration//GEN-END:variables
}
