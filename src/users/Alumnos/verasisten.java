package users.Alumnos;

import java.sql.*;
import login.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Interfaz para visualizar registros de asistencia de alumnos.
 * 
 * Características principales:
 * - Muestra tabla de asistencias
 * - Recupera datos de asistencia desde base de datos
 * - Utiliza patrón Singleton para conexión
 * 
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [12/03/2025]
 */
public class verasisten extends javax.swing.JFrame {

    // Conexión a la base de datos
    private Connection conect;

    /**
     * Constructor de la interfaz de visualización de asistencias.
     * 
     * Inicializa componentes y carga datos:
     * - Obtiene conexión mediante Singleton
     * - Inicializa componentes de interfaz
     * - Muestra datos de asistencia
     */
    public verasisten() {
        // Obtener la conexión a través del Singleton
        conect = Conexion.getInstancia().getConexion(); // Aquí obtienes la conexión desde el Singleton
        initComponents();
        mostrarDatos();    // Mostrar datos al inicializar
    }

    /**
     * Carga y muestra los datos de asistencia en la tabla.
     * 
     * Pasos:
     * - Crea modelo de tabla
     * - Configura columnas
     * - Ejecuta consulta SQL para recuperar asistencias
     * - Añade filas al modelo de tabla
     */
    public void mostrarDatos() {
        DefaultTableModel tcliente = new DefaultTableModel();
        tcliente.addColumn("alumno_id");
        tcliente.addColumn("fecha");
        tcliente.addColumn("estado");
        tcliente.addColumn("docente_id");
        tablasisten.setModel(tcliente);

        String[] datos = new String[4];  // Cambié de 6 a 4 columnas

        // Usamos la conexión del Singleton (la conexión ya está abierta durante toda la instancia)
        try {
            Statement leer = conect.createStatement();
            ResultSet resultado = leer.executeQuery("SELECT * FROM asistencia");

            while (resultado.next()) {
                datos[0] = resultado.getString(2);
                datos[1] = resultado.getString(3);
                datos[2] = resultado.getString(4);
                datos[3] = resultado.getString(5);

                tcliente.addRow(datos);
            }

            tablasisten.setModel(tcliente);

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
        tablasisten = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();

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

        tablasisten.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablasisten);

        jLabel3.setFont(new java.awt.Font("Swis721 Lt BT", 1, 24)); // NOI18N
        jLabel3.setText("ASISTENCIAS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(296, 296, 296))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(68, 68, 68)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(68, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addComponent(jLabel3)
                .addGap(523, 523, 523)
                .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(174, 174, 174)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(189, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botregActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botregActionPerformed
        alumnos log = new alumnos();
        log.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_botregActionPerformed

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
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new verasisten().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botreg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablasisten;
    // End of variables declaration//GEN-END:variables
}
