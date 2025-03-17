/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package users.Profesor;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import login.Conexion;
import java.sql.*;

/**
 *
 * @author nico_
 */
public class NotasBimestralesPanel extends javax.swing.JPanel {

   private JTable tablaNotas;
   private DefaultTableModel tableModel;
   private Connection conect;
   private int profesorId;
   private int cursoId;
   private int materiaId;
    
    
    public NotasBimestralesPanel(int profesorId, int cursoId, int materiaId) {
       this.profesorId = profesorId;
       this.cursoId = cursoId;
       this.materiaId = materiaId;
       this.conect = Conexion.getInstancia().verificarConexion();

       setLayout(new BorderLayout(5, 5));

       JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
       JLabel lblPeriodo = new JLabel("Período:");
       JComboBox<String> comboPeriodo = new JComboBox<>(new String[] {
           "1er Bimestre", "2do Bimestre", "3er Bimestre", "4to Bimestre"
       });
       headerPanel.add(lblPeriodo);
       headerPanel.add(comboPeriodo);

       // Tabla con columnas: Alumno, DNI, Promedio Actividades, Nota Bimestral
       tableModel = new DefaultTableModel(
           new String[] {"Alumno", "DNI", "Promedio Actividades", "Nota Bimestral"}, 0
       ) {
           @Override
           public boolean isCellEditable(int row, int col) {
               return col == 3; // Solo nota bimestral editable
           }
       };
       
       tablaNotas = new JTable(tableModel);
       JScrollPane scrollPane = new JScrollPane(tablaNotas);

       JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
       JButton btnGuardar = new JButton("Guardar Notas");
       buttonPanel.add(btnGuardar);

       add(headerPanel, BorderLayout.NORTH);
       add(scrollPane, BorderLayout.CENTER);
       add(buttonPanel, BorderLayout.SOUTH);

       // Event Listeners
       comboPeriodo.addActionListener(e -> cargarNotasPeriodo((String)comboPeriodo.getSelectedItem()));
       btnGuardar.addActionListener(e -> guardarNotas((String)comboPeriodo.getSelectedItem()));

       cargarAlumnos();
   }

   private void cargarAlumnos() {
       try {
           tableModel.setRowCount(0);
           String query = "SELECT u.apellido, u.nombre, u.dni FROM usuarios u " +
                         "JOIN alumno_curso ac ON u.id = ac.alumno_id " +
                         "WHERE ac.curso_id = ? AND ac.estado = 'activo' " +
                         "ORDER BY u.apellido, u.nombre";
           
           PreparedStatement ps = conect.prepareStatement(query);
           ps.setInt(1, cursoId);
           ResultSet rs = ps.executeQuery();

           while(rs.next()) {
               String alumno = rs.getString("apellido") + ", " + rs.getString("nombre");
               String dni = rs.getString("dni");
               double promedioActividades = calcularPromedioActividades(dni);
               
               tableModel.addRow(new Object[] {
                   alumno, dni, promedioActividades, 0.0
               });
           }
       } catch (SQLException ex) {
           JOptionPane.showMessageDialog(this, "Error al cargar alumnos: " + ex.getMessage());
       }
   }

   private double calcularPromedioActividades(String dni) {
       try {
           String query = "SELECT AVG(nota) as promedio FROM notas " +
                         "WHERE alumno_id = ? AND materia_id = ?";
           PreparedStatement ps = conect.prepareStatement(query);
           ps.setString(1, dni);
           ps.setInt(2, materiaId);
           
           ResultSet rs = ps.executeQuery();
           if(rs.next()) {
               return Math.round(rs.getDouble("promedio") * 100.0) / 100.0;
           }
       } catch (SQLException ex) {
           ex.printStackTrace();
       }
       return 0.0;
   }

   private void cargarNotasPeriodo(String periodo) {
       try {
           String query = "SELECT alumno_id, nota FROM notas_bimestrales " +
                         "WHERE materia_id = ? AND periodo = ?";
           PreparedStatement ps = conect.prepareStatement(query);
           ps.setInt(1, materiaId);
           ps.setString(2, periodo);
           
           ResultSet rs = ps.executeQuery();
           Map<String, Double> notasPorAlumno = new HashMap<>();
           
           while(rs.next()) {
               notasPorAlumno.put(rs.getString("alumno_id"), rs.getDouble("nota"));
           }

           for(int i = 0; i < tableModel.getRowCount(); i++) {
               String dni = (String)tableModel.getValueAt(i, 1);
               Double nota = notasPorAlumno.get(dni);
               tableModel.setValueAt(nota != null ? nota : 0.0, i, 3);
           }
       } catch (SQLException ex) {
           JOptionPane.showMessageDialog(this, "Error al cargar notas: " + ex.getMessage());
       }
   }

   private void guardarNotas(String periodo) {
       try {
           conect.setAutoCommit(false);
           
           // Eliminar notas existentes del período
           String deleteQuery = "DELETE FROM notas_bimestrales " +
                              "WHERE materia_id = ? AND periodo = ?";
           PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
           deletePs.setInt(1, materiaId);
           deletePs.setString(2, periodo);
           deletePs.executeUpdate();

           // Insertar nuevas notas
           String insertQuery = "INSERT INTO notas_bimestrales " +
                              "(alumno_id, materia_id, periodo, nota, promedio_actividades) " +
                              "VALUES (?, ?, ?, ?, ?)";
           PreparedStatement insertPs = conect.prepareStatement(insertQuery);

           for(int i = 0; i < tableModel.getRowCount(); i++) {
               String dni = (String)tableModel.getValueAt(i, 1);
               double promedioAct = (double)tableModel.getValueAt(i, 2);
               double notaBimestral = (double)tableModel.getValueAt(i, 3);

               insertPs.setString(1, dni);
               insertPs.setInt(2, materiaId);
               insertPs.setString(3, periodo);
               insertPs.setDouble(4, notaBimestral);
               insertPs.setDouble(5, promedioAct);
               insertPs.executeUpdate();
           }

           conect.commit();
           JOptionPane.showMessageDialog(this, "Notas guardadas exitosamente");
       } catch (SQLException ex) {
           try {
               conect.rollback();
           } catch (SQLException rollbackEx) {
               rollbackEx.printStackTrace();
           }
           JOptionPane.showMessageDialog(this, "Error al guardar notas: " + ex.getMessage());
       } finally {
           try {
               conect.setAutoCommit(true);
           } catch (SQLException ex) {
               ex.printStackTrace();
           }
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
