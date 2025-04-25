package main.java.views.users.Attp.menu;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import main.java.views.users.Attp.attp;

/**
 * Interfaz para visualización de préstamos de dispositivos.
 *
 * Características principales: 
 * - Muestra tabla de préstamos de netbooks
 * - Gestiona conexión a base de datos 
 * - Recupera información de préstamos
 *
 * @author [División ATTP]
 * @version 1.0
 * @since [13/03/2025]
 */
public class Prestamos extends javax.swing.JFrame {

    // Conexión a la base de datos
    Connection conect;

    /**
     * Constructor principal de la interfaz de préstamos.
     *
     * Inicializa componentes: 
     * - Componentes de interfaz gráfica 
     * - Verifica conexión a base de datos 
     * - Carga datos de préstamos
     */
    public Prestamos() {
        initComponents();
        mostrardatos();
        probar_conexion();
    }

    /**
     * Verifica la conexión a la base de datos.
     *
     * Utiliza el patrón Singleton para obtener la conexión. 
     * Muestra un mensaje de error si no se puede establecer conexión.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    /**
     * Carga y muestra los datos de préstamos en la tabla.
     *
     * Pasos: 
     * - Crea modelo de tabla 
     * - Configura columnas de préstamos
     * - Ejecutaco consulta SQL para recuperar información 
     * - Añade filas al modelo de tabla
     */
    public void mostrardatos() {
        probar_conexion();

        // Crear modelo de tabla
        DefaultTableModel tablitasPre = new DefaultTableModel();
        tablitasPre.addColumn("Netbook_ID");
        tablitasPre.addColumn("Fecha_Prestamo");
        tablitasPre.addColumn("Fecha_Devolucion");
        tablitasPre.addColumn("Hora_Prestamo");
        tablitasPre.addColumn("Hora_Devolucion");
        tablitasPre.addColumn("Curso");
        tablitasPre.addColumn("Alumno");
        tablitasPre.addColumn("Tutor");
        TablasPre.setModel(tablitasPre);
        Statement vertablasPre;
        String[] datos_Pre = new String[8];
        try {
            vertablasPre = conect.createStatement();
            ResultSet mostrarsPre = vertablasPre.executeQuery("SELECT * FROM prestamos");
            while (mostrarsPre.next()) {
                datos_Pre[0] = mostrarsPre.getString(2);
                datos_Pre[1] = mostrarsPre.getString(3);
                datos_Pre[2] = mostrarsPre.getString(4);
                datos_Pre[3] = mostrarsPre.getString(5);
                datos_Pre[4] = mostrarsPre.getString(6);
                datos_Pre[5] = mostrarsPre.getString(7);
                datos_Pre[6] = mostrarsPre.getString(8);
                datos_Pre[7] = mostrarsPre.getString(9);
                tablitasPre.addRow(datos_Pre);
            }
        } catch (SQLException e) {

        }
        TablasPre.setModel(tablitasPre);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablasPre = new javax.swing.JTable();
        Boton_Volver4 = new javax.swing.JButton();
        Boton_Modificar2 = new javax.swing.JToggleButton();
        Boton_ingresar = new javax.swing.JToggleButton();
        Boton_borrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Registros de prestamos realizados");

        TablasPre.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(TablasPre);

        Boton_Volver4.setText("Volver");
        Boton_Volver4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_Volver4ActionPerformed(evt);
            }
        });

        Boton_Modificar2.setText("Modificar");
        Boton_Modificar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_Modificar2ActionPerformed(evt);
            }
        });

        Boton_ingresar.setText("Ingresar");
        Boton_ingresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_ingresarActionPerformed(evt);
            }
        });

        Boton_borrar.setText("Borrar");
        Boton_borrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_borrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(408, 408, 408)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 998, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(62, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Boton_borrar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Boton_Volver4)
                .addGap(18, 18, 18)
                .addComponent(Boton_Modificar2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Boton_ingresar)
                .addGap(351, 351, 351))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addGap(38, 38, 38)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 573, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Boton_Volver4)
                    .addComponent(Boton_Modificar2)
                    .addComponent(Boton_ingresar)
                    .addComponent(Boton_borrar))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Boton_Volver4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_Volver4ActionPerformed
        this.setVisible(false);
        attp attp = new attp();
        attp.setVisible(true);
    }//GEN-LAST:event_Boton_Volver4ActionPerformed

    private void Boton_Modificar2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_Modificar2ActionPerformed
        /**
         * Método que gestiona la operación de préstamos.
         *
         * Funcionalidades principales: 
         * - Recupera datos de préstamos desde la base de datos 
         * - Registra datos iniciales en un archivo de texto 
         * - Actualiza registros de préstamos 
         * - Genera un log de cambios
         *
         * Pasos del proceso: 
         * 1. Consulta todos los préstamos en la base de datos 
         * 2. Escribe datos iniciales en archivo de registro 
         * 3. Selecciona un registro de la tabla 
         * 4. Actualiza el registro en la base de datos
         * 5. Genera un log de los datos finales en un archivo TXT
         */
        Statement statement = null;
        try {
            statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM prestamos");
            while (resultSet.next()) {
                String Prestamo_ID = resultSet.getString("Prestamo_ID");
                String Netbook_ID = resultSet.getString("Netbook_ID");
                String Fecha_Prestamo = resultSet.getString("Fecha_Prestamo");
                String Fecha_Devolucion = resultSet.getString("Fecha_Devolucion");
                String Hora_Prestamo = resultSet.getString("Hora_Prestamo");
                String Hora_Devolucion = resultSet.getString("Hora_Devolucion");
                String Curso = resultSet.getString("Curso");
                String Alumno = resultSet.getString("Alumno");
                String Tutor = resultSet.getString("Tutor");

                try (FileWriter EscrituraInicial = new FileWriter("Registros.txt", true)) {
                    EscrituraInicial.write("\n");
                    EscrituraInicial.write("Datos iniciales de prestamos: " + Prestamo_ID + "|" + Netbook_ID + "|" + Fecha_Prestamo + "|" + Fecha_Devolucion + "|" + Hora_Prestamo + "|" + Hora_Devolucion + "|" + Curso + "|" + Alumno + "|" + Tutor);
                    EscrituraInicial.write("\n");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error al escribir en el archivo: " + e.getMessage());
                }
            }
        } catch (SQLException ex) {
        }

        int fila = TablasPre.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(null, "Seleccione el registro antes de apretar el botón");
        }

        String Netbook_ID = TablasPre.getValueAt(fila, 0).toString();
        String Fecha_Prestamo = TablasPre.getValueAt(fila, 1).toString();
        String Fecha_Devolucion = TablasPre.getValueAt(fila, 2).toString();
        String Hora_Prestamo = TablasPre.getValueAt(fila, 3).toString();
        String Hora_Devolucion = TablasPre.getValueAt(fila, 4).toString();
        String Curso = TablasPre.getValueAt(fila, 5).toString();
        String Alumno = TablasPre.getValueAt(fila, 6).toString();
        String Tutor = TablasPre.getValueAt(fila, 7).toString();
        JOptionPane.showMessageDialog(null, "¡Registro actualizado!");

        try {
            PreparedStatement actu = conect.prepareStatement("UPDATE prestamos SET Fecha_Prestamo= '" + Fecha_Prestamo + "',Fecha_Devolucion = '" + Fecha_Devolucion
                    + "',Hora_Prestamo = '" + Hora_Prestamo + "',Hora_Devolucion = '" + Hora_Devolucion + "',Curso = '" + Curso + "',Alumno = '" + Alumno + "',Tutor = '" + Tutor + "' WHERE Netbook_ID = '" + Netbook_ID + "'");
            actu.executeUpdate();
            mostrardatos();
        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, e + "No se pudo actualizar los datos");
        }
        try (FileWriter Escritura = new FileWriter("Registros.txt", true)) {
            Escritura.write("\n");
            Netbook_ID = TablasPre.getValueAt(fila, 0).toString();
            Fecha_Prestamo = TablasPre.getValueAt(fila, 1).toString();
            Fecha_Devolucion = TablasPre.getValueAt(fila, 2).toString();
            Curso = TablasPre.getValueAt(fila, 3).toString();
            Alumno = TablasPre.getValueAt(fila, 4).toString();
            Tutor = TablasPre.getValueAt(fila, 5).toString();
            String Registro2 = ("Datos finales de prestamos: " + Netbook_ID + "|" + Fecha_Prestamo + "|" + Fecha_Devolucion + "|" + Hora_Prestamo + "|" + Hora_Devolucion + "|" + Curso + "|" + Alumno + "|" + Tutor);
            Escritura.write(Registro2);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al escribir en el archivo: " + e.getMessage());
        }
    }//GEN-LAST:event_Boton_Modificar2ActionPerformed

    private void Boton_ingresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_ingresarActionPerformed
        this.setVisible(false);
        Ingresar_Prestamos Prestamo = new Ingresar_Prestamos();
        Prestamo.setVisible(true);
    }//GEN-LAST:event_Boton_ingresarActionPerformed

    private void Boton_borrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_borrarActionPerformed
        /**
        * Método para eliminar un registro de préstamo seleccionado.
        * 
        * Funcionalidades principales:
        * - Verifica selección de fila en la tabla
        * - Elimina registro de préstamo de la base de datos
        * - Actualiza la vista de tabla
        * 
        * Pasos del proceso:
        * 1. Obtener la fila seleccionada
        * 2. Validar que se haya seleccionado una fila
        * 3. Recuperar ID de préstamo
        * 4. Ejecutar consulta SQL de eliminación
        * 5. Actualizar tabla y mostrar mensaje de confirmación
        * 
        * Manejo de errores:
        * - Muestra mensaje si no se selecciona fila
        * - Captura y muestra errores de base de datos
        */
        int fila = TablasPre.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(null, "Seleccione el registro antes de apretar el botón");
        }
        String Prestamo_ID = TablasPre.getValueAt(fila, 0).toString();
        try {
            PreparedStatement actu = conect.prepareStatement("DELETE FROM prestamos WHERE Netbook_ID = '" + Prestamo_ID + "';");
            actu.executeUpdate();

            mostrardatos();
        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, e + "No se pudo actualizar los datos");
        }
        if (fila < 0) {
            JOptionPane.showMessageDialog(null, "Seleccione el registro antes de apretar el botón");
        } else {
            JOptionPane.showMessageDialog(null, "¡Registro actualizado!");
        }
    }//GEN-LAST:event_Boton_borrarActionPerformed

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
            java.util.logging.Logger.getLogger(Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Prestamos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton Boton_Modificar2;
    private javax.swing.JButton Boton_Volver4;
    private javax.swing.JButton Boton_borrar;
    private javax.swing.JToggleButton Boton_ingresar;
    private javax.swing.JTable TablasPre;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
