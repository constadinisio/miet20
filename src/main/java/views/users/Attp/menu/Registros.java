package main.java.views.users.Attp.menu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import main.java.database.Conexion;
import main.java.views.users.Attp.attp;

public class Registros extends javax.swing.JFrame {
   Connection conect;
   
    public Registros() {
        initComponents();
        probar_conexion();
    }

    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        Boton_Volver3 = new javax.swing.JButton();
        Boton_Volver4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Campotxt_Registros = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Registro de movimiento del patrimonio");

        Boton_Volver3.setText("Volver");
        Boton_Volver3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_Volver3ActionPerformed(evt);
            }
        });

        Boton_Volver4.setText("Mostrar");
        Boton_Volver4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_Volver4ActionPerformed(evt);
            }
        });

        Campotxt_Registros.setColumns(20);
        Campotxt_Registros.setRows(5);
        jScrollPane1.setViewportView(Campotxt_Registros);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Boton_Volver3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Boton_Volver4)
                .addGap(365, 365, 365))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(316, 316, 316)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(jLabel1)
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 551, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Boton_Volver3)
                    .addComponent(Boton_Volver4))
                .addGap(27, 27, 27))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Boton_Volver3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_Volver3ActionPerformed
        this.setVisible(false);
        attp Attp = new attp();
        Attp.setVisible (true);
    }//GEN-LAST:event_Boton_Volver3ActionPerformed

    private void Boton_Volver4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_Volver4ActionPerformed
        try {
            FileReader leer = new FileReader("Registros.txt");
            BufferedReader buffertxt = new BufferedReader(leer);
            StringBuilder registros = new StringBuilder();
            String registro;

            while ((registro = buffertxt.readLine()) != null) {
                registros.append(registro).append("\n").append("\n");
            }

            Campotxt_Registros.setText(registros.toString());

            buffertxt.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Registros.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Registros.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_Boton_Volver4ActionPerformed

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
            java.util.logging.Logger.getLogger(Registros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Registros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Registros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Registros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Registros().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Boton_Volver3;
    private javax.swing.JButton Boton_Volver4;
    private javax.swing.JTextArea Campotxt_Registros;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
