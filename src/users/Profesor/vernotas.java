package Profesor;



import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class vernotas extends javax.swing.JFrame {
    
    // Mantén la conexión abierta a nivel de la clase
    private Connection conect;

    public vernotas() {
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        botreg = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablanotas = new javax.swing.JTable();
        botmod = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        botreg.setBackground(new java.awt.Color(102, 153, 255));
        botreg.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botreg.setForeground(new java.awt.Color(255, 255, 255));
        botreg.setText("Volver");
        botreg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botregActionPerformed(evt);
            }
        });

        tablanotas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablanotas);

        botmod.setBackground(new java.awt.Color(102, 153, 255));
        botmod.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botmod.setForeground(new java.awt.Color(255, 255, 255));
        botmod.setText("Modificar");
        botmod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botmodActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botmod, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(67, 67, 67)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botmod, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botregActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botregActionPerformed
        profesor log = new profesor();
        log.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_botregActionPerformed

    private void botmodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botmodActionPerformed
        modnotas veras = new modnotas();
        veras.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_botmodActionPerformed

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
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new vernotas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botmod;
    private javax.swing.JButton botreg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablanotas;
    // End of variables declaration//GEN-END:variables
}
