/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package main.java.views.users.Attp.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import main.java.views.users.common.VentanaInicio;

/**
 *
 * @author nico_
 */
public class PrestamosPanel extends javax.swing.JPanel {

    // Conexión a la base de datos
    private Connection conect;
    
    // Referencia a la ventana principal
    private VentanaInicio ventanaPrincipal;
    
    // ID del usuario ATTP
    private int attpId;

    /**
     * Constructor del panel de Préstamos.
     *
     * @param ventanaPrincipal Referencia a la ventana principal para navegación
     * @param attpId ID del usuario ATTP
     */
    public PrestamosPanel(VentanaInicio ventanaPrincipal, int attpId) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.attpId = attpId;
        
        // Inicializar componentes generados automáticamente
        initComponents();
        
        // Conectar a la base de datos y cargar datos
        probar_conexion();
        mostrardatos();
        
        System.out.println("PrestamosPanel inicializado correctamente");
    }

    /**
     * Verifica la conexión a la base de datos.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carga y muestra los datos de préstamos en la tabla.
     */
    public void mostrardatos() {
        probar_conexion();

        // Crear modelo de tabla
        DefaultTableModel tablitasPre = new DefaultTableModel();
        tablitasPre.addColumn("ID");
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
        String[] datos_Pre = new String[9];
        
        try {
            vertablasPre = conect.createStatement();
            ResultSet mostrarsPre = vertablasPre.executeQuery("SELECT * FROM prestamos ORDER BY Fecha_Prestamo DESC, Hora_Prestamo DESC");
            
            while (mostrarsPre.next()) {
                datos_Pre[0] = mostrarsPre.getString("Prestamo_ID");
                datos_Pre[1] = mostrarsPre.getString("Netbook_ID");
                datos_Pre[2] = mostrarsPre.getString("Fecha_Prestamo");
                datos_Pre[3] = mostrarsPre.getString("Fecha_Devolucion");
                datos_Pre[4] = mostrarsPre.getString("Hora_Prestamo");
                datos_Pre[5] = mostrarsPre.getString("Hora_Devolucion");
                datos_Pre[6] = mostrarsPre.getString("Curso");
                datos_Pre[7] = mostrarsPre.getString("Alumno");
                datos_Pre[8] = mostrarsPre.getString("Tutor");
                tablitasPre.addRow(datos_Pre);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar datos de préstamos: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        TablasPre.setModel(tablitasPre);
    }

    /**
     * Registra en el archivo de log el estado inicial antes de una operación.
     */
    private void registrarLogInicial() {
        try {
            Statement statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM prestamos");

            try (FileWriter escrituraInicial = new FileWriter("Registros.txt", true)) {
                escrituraInicial.write("\n");
                escrituraInicial.write("--- ESTADO INICIAL DE PRÉSTAMOS ANTES DE OPERACIÓN ---\n");
                escrituraInicial.write("Fecha: " + LocalDate.now() + " Hora: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "\n");

                while (resultSet.next()) {
                    String prestamoID = resultSet.getString("Prestamo_ID");
                    String netbookID = resultSet.getString("Netbook_ID");
                    String fechaPrestamo = resultSet.getString("Fecha_Prestamo");
                    String fechaDevolucion = resultSet.getString("Fecha_Devolucion");
                    String horaPrestamo = resultSet.getString("Hora_Prestamo");
                    String horaDevolucion = resultSet.getString("Hora_Devolucion");
                    String curso = resultSet.getString("Curso");
                    String alumno = resultSet.getString("Alumno");
                    String tutor = resultSet.getString("Tutor");

                    escrituraInicial.write("Préstamo inicial: " + prestamoID + "|" + netbookID + "|" 
                            + fechaPrestamo + "|" + fechaDevolucion + "|" + horaPrestamo + "|" 
                            + horaDevolucion + "|" + curso + "|" + alumno + "|" + tutor + "\n");
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error al registrar log inicial: " + e.getMessage());
        }
    }

    /**
     * Registra en el archivo de log el estado final después de una operación.
     */
    private void registrarLogFinal() {
        try {
            Statement statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM prestamos");

            try (FileWriter escrituraFinal = new FileWriter("Registros.txt", true)) {
                escrituraFinal.write("\n");
                escrituraFinal.write("--- ESTADO FINAL DE PRÉSTAMOS DESPUÉS DE OPERACIÓN ---\n");
                escrituraFinal.write("Fecha: " + LocalDate.now() + " Hora: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "\n");

                while (resultSet.next()) {
                    String prestamoID = resultSet.getString("Prestamo_ID");
                    String netbookID = resultSet.getString("Netbook_ID");
                    String fechaPrestamo = resultSet.getString("Fecha_Prestamo");
                    String fechaDevolucion = resultSet.getString("Fecha_Devolucion");
                    String horaPrestamo = resultSet.getString("Hora_Prestamo");
                    String horaDevolucion = resultSet.getString("Hora_Devolucion");
                    String curso = resultSet.getString("Curso");
                    String alumno = resultSet.getString("Alumno");
                    String tutor = resultSet.getString("Tutor");

                    escrituraFinal.write("Préstamo final: " + prestamoID + "|" + netbookID + "|" 
                            + fechaPrestamo + "|" + fechaDevolucion + "|" + horaPrestamo + "|" 
                            + horaDevolucion + "|" + curso + "|" + alumno + "|" + tutor + "\n");
                }

                escrituraFinal.write("--- FIN DE OPERACIÓN ---\n\n");
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error al registrar log final: " + e.getMessage());
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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablasPre = new javax.swing.JTable();
        Boton_Volver4 = new javax.swing.JButton();
        Boton_Modificar2 = new javax.swing.JToggleButton();
        Boton_ingresar = new javax.swing.JToggleButton();
        Boton_borrar = new javax.swing.JButton();

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void Boton_Volver4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_Volver4ActionPerformed
       ventanaPrincipal.restaurarVistaPrincipal();
    }//GEN-LAST:event_Boton_Volver4ActionPerformed

    private void Boton_Modificar2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_Modificar2ActionPerformed
        int fila = TablasPre.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de la tabla antes de modificar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Registrar estado inicial
        registrarLogInicial();

        String prestamoID = TablasPre.getValueAt(fila, 0).toString();
        String netbookID = TablasPre.getValueAt(fila, 1).toString();
        String fechaPrestamo = TablasPre.getValueAt(fila, 2).toString();
        String fechaDevolucion = TablasPre.getValueAt(fila, 3) != null ? TablasPre.getValueAt(fila, 3).toString() : "";
        String horaPrestamo = TablasPre.getValueAt(fila, 4).toString();
        String horaDevolucion = TablasPre.getValueAt(fila, 5) != null ? TablasPre.getValueAt(fila, 5).toString() : "";
        String curso = TablasPre.getValueAt(fila, 6).toString();
        String alumno = TablasPre.getValueAt(fila, 7).toString();
        String tutor = TablasPre.getValueAt(fila, 8).toString();

        try {
            // La tabla es editable, así que tomamos los valores actuales de la fila
            PreparedStatement actu = conect.prepareStatement(
                "UPDATE prestamos SET Fecha_Prestamo = ?, Fecha_Devolucion = ?, " +
                "Hora_Prestamo = ?, Hora_Devolucion = ?, Curso = ?, Alumno = ?, Tutor = ? " +
                "WHERE Prestamo_ID = ?");
            
            actu.setString(1, fechaPrestamo);
            actu.setString(2, fechaDevolucion.isEmpty() ? null : fechaDevolucion);
            actu.setString(3, horaPrestamo);
            actu.setString(4, horaDevolucion.isEmpty() ? null : horaDevolucion);
            actu.setString(5, curso);
            actu.setString(6, alumno);
            actu.setString(7, tutor);
            actu.setString(8, prestamoID);
            
            int resultado = actu.executeUpdate();
            
            if (resultado > 0) {
                JOptionPane.showMessageDialog(this, "Registro actualizado exitosamente", 
                                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                mostrardatos(); // Recargar tabla
                
                // Registrar estado final
                registrarLogFinal();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el registro", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar préstamo: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_Boton_Modificar2ActionPerformed

    private void Boton_ingresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_ingresarActionPerformed
          // Navegar al panel de ingreso de préstamo
        IngresarPrestamoPanel ingresarPanel = new IngresarPrestamoPanel(ventanaPrincipal, attpId);
        
        // Obtener panel principal y configurarlo
        JPanel panelPrincipal = ventanaPrincipal.getPanelPrincipal();
        panelPrincipal.removeAll();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.add(ingresarPanel, BorderLayout.CENTER);
        
        // Actualizar vista
        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    /**
     * Maneja la acción del botón devolver netbook.
     */
    private void Boton_devolverActionPerformed() {
        int fila = TablasPre.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla para marcar como devuelto",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String prestamoID = TablasPre.getValueAt(fila, 0).toString();
        String fechaDevolucion = TablasPre.getValueAt(fila, 3) != null ? TablasPre.getValueAt(fila, 3).toString() : "";
        String horaDevolucion = TablasPre.getValueAt(fila, 5) != null ? TablasPre.getValueAt(fila, 5).toString() : "";

        // Verificar si ya fue devuelto
        if (!fechaDevolucion.isEmpty() && !horaDevolucion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta netbook ya fue devuelta el " + fechaDevolucion + " a las " + horaDevolucion,
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Confirma la devolución de esta netbook?",
                "Confirmar devolución", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                // Registrar estado inicial
                registrarLogInicial();

                // Obtener fecha y hora actual
                LocalDate fechaActual = LocalDate.now();
                LocalTime horaActual = LocalTime.now();
                DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter formateadorHora = DateTimeFormatter.ofPattern("HH:mm");

                String fechaDevolucionActual = fechaActual.format(formateadorFecha);
                String horaDevolucionActual = horaActual.format(formateadorHora);

                // Actualizar el préstamo con fecha y hora de devolución
                PreparedStatement ps = conect.prepareStatement(
                    "UPDATE prestamos SET Fecha_Devolucion = ?, Hora_Devolucion = ? WHERE Prestamo_ID = ?");
                
                ps.setString(1, fechaDevolucionActual);
                ps.setString(2, horaDevolucionActual);
                ps.setString(3, prestamoID);

                int resultado = ps.executeUpdate();

                if (resultado > 0) {
                    JOptionPane.showMessageDialog(this, "Devolución registrada exitosamente", 
                                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    mostrardatos(); // Recargar tabla
                    
                    // Registrar estado final
                    registrarLogFinal();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo registrar la devolución", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                System.err.println("Error al registrar devolución: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error al registrar devolución: " + e.getMessage(), 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_Boton_ingresarActionPerformed

    private void Boton_borrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Boton_borrarActionPerformed
        int fila = TablasPre.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de la tabla para eliminar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String prestamoID = TablasPre.getValueAt(fila, 0).toString();
        String netbookID = TablasPre.getValueAt(fila, 1).toString();
        String alumno = TablasPre.getValueAt(fila, 7).toString();

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el préstamo de la netbook " + netbookID + " para " + alumno + "?\n" +
                "Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                // Registrar estado inicial
                registrarLogInicial();

                // Eliminar el préstamo
                PreparedStatement ps = conect.prepareStatement("DELETE FROM prestamos WHERE Prestamo_ID = ?");
                ps.setString(1, prestamoID);

                int resultado = ps.executeUpdate();

                if (resultado > 0) {
                    JOptionPane.showMessageDialog(this, "Préstamo eliminado exitosamente", 
                                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    mostrardatos(); // Recargar tabla
                    
                    // Registrar estado final
                    registrarLogFinal();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el préstamo", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                System.err.println("Error al eliminar préstamo: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_Boton_borrarActionPerformed


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
