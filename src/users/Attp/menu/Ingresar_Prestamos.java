package users.Attp.menu;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import login.Conexion;

/**
 * Interfaz para ingreso de préstamos en el sistema ATTP.
 * 
 * Características principales:
 * - Gestión de conexión a base de datos
 * - Interfaz para registrar préstamos
 * 
 * @author [División ATTP]
 * @version 1.0
 * @since [13/03/2025]
 */
public class Ingresar_Prestamos extends javax.swing.JFrame {

    Connection conect;
    
    /**
     * Constructor principal de la interfaz de préstamos.
     * 
     * Inicializa componentes:
     * - Componentes de interfaz gráfica
     * - Verifica conexión a base de datos
     */
    public Ingresar_Prestamos() {
        initComponents();
        probar_conexion();
    }
    
    /**
     * Verifica la conexión a la base de datos.
     * 
     * Utiliza el patrón Singleton para obtener la conexión.
     * Muestra un mensaje de error si no se puede establecer conexión.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().getConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Campo_NetbookID = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        Campo_Curso = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        Campo_Alumno = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        Campo_Tutor = new javax.swing.JTextField();
        Boton_Back = new javax.swing.JButton();
        Boton_Ingreso = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        Campo_Fecha = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        Campo_Hora = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabel1.setText("Ingresar prestamo");

        jLabel3.setText("Netbook_ID");

        Campo_NetbookID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Campo_NetbookIDActionPerformed(evt);
            }
        });

        jLabel4.setText("Curso");

        Campo_Curso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Campo_CursoActionPerformed(evt);
            }
        });

        jLabel5.setText("Alumno");

        Campo_Alumno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Campo_AlumnoActionPerformed(evt);
            }
        });

        jLabel6.setText("Tutor");

        Campo_Tutor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Campo_TutorActionPerformed(evt);
            }
        });

        Boton_Back.setText("Volver");
        Boton_Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_BackActionPerformed(evt);
            }
        });

        Boton_Ingreso.setText("Ingreso");
        Boton_Ingreso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Boton_IngresoActionPerformed(evt);
            }
        });

        jLabel7.setText("Hora");

        Campo_Fecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Campo_FechaActionPerformed(evt);
            }
        });

        jLabel8.setText("Fecha");

        Campo_Hora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Campo_HoraActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(93, 93, 93)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Campo_Fecha, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(Campo_Hora, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel2))
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel5)
                                        .addComponent(jLabel6)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(Boton_Back)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(Boton_Ingreso))
                                        .addComponent(Campo_Tutor, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                        .addComponent(Campo_Alumno)
                                        .addComponent(Campo_Curso)
                                        .addComponent(Campo_NetbookID)))))))
                .addContainerGap(441, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Campo_NetbookID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Campo_Curso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Campo_Alumno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Campo_Tutor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Campo_Hora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Campo_Fecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Boton_Back)
                    .addComponent(Boton_Ingreso))
                .addGap(55, 55, 55))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Campo_NetbookIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Campo_NetbookIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Campo_NetbookIDActionPerformed

    private void Campo_CursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Campo_CursoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Campo_CursoActionPerformed

    private void Campo_AlumnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Campo_AlumnoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Campo_AlumnoActionPerformed

    private void Campo_TutorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Campo_TutorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Campo_TutorActionPerformed

    private void Boton_BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_BackActionPerformed
        this.setVisible(false);
        Prestamos Prestamo = new Prestamos();
        Prestamo.setVisible(true);        // TODO add your handling code here:
    }//GEN-LAST:event_Boton_BackActionPerformed

    private void Boton_IngresoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_IngresoActionPerformed
        
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
                 
                 try  (FileWriter EscrituraInicial = new FileWriter("Registros.txt", true)){
                    EscrituraInicial.write("\n");
       EscrituraInicial.write("Datos iniciales de prestamos: "+Prestamo_ID+"|"+Netbook_ID+"|"+Fecha_Prestamo+"|"+Hora_Prestamo+"|"+Hora_Devolucion+"|"+Fecha_Devolucion+"|"+Curso+"|"+Alumno+"|"+Tutor);
       EscrituraInicial.write("\n");
        }
        catch(IOException e){
            JOptionPane.showMessageDialog(null, "Error al escribir en el archivo: " + e.getMessage());
        }
             }
        } catch (SQLException ex) {

        }
        try {
            PreparedStatement enviar = conect.prepareStatement("INSERT INTO Prestamos (Netbook_ID, Curso, Alumno, Tutor,Hora_Prestamo,Fecha_Prestamo) VALUES (?,?,?,?,?,?)");
            enviar.setString(1, Campo_NetbookID.getText());
            enviar.setString(2, Campo_Curso.getText());
            enviar.setString(3, Campo_Alumno.getText());
            enviar.setString(4, Campo_Tutor.getText());
            enviar.setString(5,Campo_Hora.getText());
            enviar.setString(6, Campo_Fecha.getText());
            
            enviar.executeUpdate();
            JOptionPane.showMessageDialog(null, "Prestamo registrado");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e + " No se pudo registrar, ID de netbook incorrecta");

        }
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
                 
                 try  (FileWriter EscrituraInicial = new FileWriter("Registros.txt", true)){
                    EscrituraInicial.write("\n");
       EscrituraInicial.write("Datos nuevos ingresador en la tabla prestamos: "+Prestamo_ID+"|"+Netbook_ID+"|"+Fecha_Prestamo+"|"+Fecha_Devolucion+"|"+Hora_Prestamo+"|"+Hora_Devolucion+"|"+Curso+"|"+Alumno+"|"+Tutor);
       EscrituraInicial.write("\n");
        }
        catch(IOException e){
            JOptionPane.showMessageDialog(null, "Error al escribir en el archivo: " + e.getMessage());
        }
             }
        } catch (SQLException ex) {
        }
    }//GEN-LAST:event_Boton_IngresoActionPerformed

    private void Campo_FechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Campo_FechaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Campo_FechaActionPerformed

    private void Campo_HoraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Campo_HoraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Campo_HoraActionPerformed

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
            java.util.logging.Logger.getLogger(Ingresar_Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ingresar_Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ingresar_Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ingresar_Prestamos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ingresar_Prestamos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Boton_Back;
    private javax.swing.JButton Boton_Ingreso;
    private javax.swing.JTextField Campo_Alumno;
    private javax.swing.JTextField Campo_Curso;
    private javax.swing.JTextField Campo_Fecha;
    private javax.swing.JTextField Campo_Hora;
    private javax.swing.JTextField Campo_NetbookID;
    private javax.swing.JTextField Campo_Tutor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    // End of variables declaration//GEN-END:variables
}
