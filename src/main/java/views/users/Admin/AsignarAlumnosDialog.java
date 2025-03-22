/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package main.java.views.users.Admin;
import java.awt.Frame;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;

/**
 * Diálogo para asignar alumnos a cursos en el sistema administrativo.
 * 
 * Características principales:
 * - Permite visualizar y modificar la asignación de alumnos a cursos
 * - Gestiona la relación entre alumnos y cursos en la base de datos
 * - Proporciona interfaz para selección múltiple de alumnos
 * 
 */
public class AsignarAlumnosDialog extends javax.swing.JDialog {

    // Identificador del curso actual
    private int cursoId;
    
    // Conexión a la base de datos
    private Connection conect;
    
    // Modelo de tabla para gestionar datos de alumnos
    private DefaultTableModel tableModel;
    
    // Bandera para rastrear cambios realizados en las asignaciones
    private boolean cambiosRealizados = false;
    
    /**
     * Constructor del diálogo de asignación de alumnos.
     * 
     * Inicializa componentes, tabla y carga datos de alumnos.
     * 
     * @param parent Ventana padre del diálogo
     * @param modal Modo modal del diálogo
     */
    public AsignarAlumnosDialog(Frame parent, boolean modal) {
        super(parent, modal);
        this.cursoId = cursoId;
        initComponents();
        inicializarTabla();
        probar_conexion();
        cargarAlumnos();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Verifica la conexión a la base de datos.
     * Muestra un mensaje de error si no se puede establecer conexión.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Inicializa el modelo de la tabla de alumnos.
     * 
     * Configura columnas, tipos de datos y editabilidad de la tabla.
     */
    private void inicializarTabla() {
        tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Boolean.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Solo la columna del checkbox es editable
            }
        };
        
        tableModel.addColumn("ID");
        tableModel.addColumn("Nombre");
        tableModel.addColumn("Apellido");
        tableModel.addColumn("Asignado");
        tablaAlumnos.setModel(tableModel);
        
        // Ocultar la columna ID
        tablaAlumnos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaAlumnos.getColumnModel().getColumn(0).setMaxWidth(0);
    }
    
    /**
     * Carga los alumnos desde la base de datos.
     * 
     * Recupera todos los alumnos y marca aquellos ya asignados al curso.
     */
    private void cargarAlumnos() {
        try {
            String query = 
                "SELECT u.id, u.nombre, u.apellido, " +
                "CASE WHEN ac.alumno_id IS NOT NULL THEN true ELSE false END as asignado " +
                "FROM usuarios u " +
                "LEFT JOIN alumno_curso ac ON u.id = ac.alumno_id AND ac.curso_id = ? " +
                "WHERE u.rol = 4 " + // rol 4 = alumno
                "ORDER BY u.apellido, u.nombre";
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getBoolean("asignado")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar alumnos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Guarda los cambios de asignación de alumnos al curso.
     * 
     * Elimina las asignaciones existentes y crea nuevas según la selección.
     */
    private void guardarCambios() {
        try {
            // Primero eliminar todas las asignaciones existentes
            String deleteQuery = "DELETE FROM alumno_curso WHERE curso_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setInt(1, cursoId);
            deletePs.executeUpdate();
            
            // Luego insertar las nuevas asignaciones
            String insertQuery = 
                "INSERT INTO alumno_curso (alumno_id, curso_id, estado) VALUES (?, ?, 'activo')";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                boolean asignado = (boolean) tableModel.getValueAt(i, 3);
                if (asignado) {
                    int alumnoId = (int) tableModel.getValueAt(i, 0);
                    insertPs.setInt(1, alumnoId);
                    insertPs.setInt(2, cursoId);
                    insertPs.executeUpdate();
                }
            }
            
            cambiosRealizados = true;
            JOptionPane.showMessageDialog(this, "Cambios guardados exitosamente");
            dispose();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar cambios: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifica si se realizaron cambios en las asignaciones de alumnos.
     * 
     * @return true si se modificaron las asignaciones, false en caso contrario
     */
    public boolean seCambiaronAsignaciones() {
        return cambiosRealizados;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        tablaAlumnos = new javax.swing.JTable();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Asignar alumnos a cursos");

        tablaAlumnos.setModel(new javax.swing.table.DefaultTableModel(
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
        scrollPane.setViewportView(tablaAlumnos);

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(btnGuardar)
                        .addGap(64, 64, 64)
                        .addComponent(btnCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
         guardarCambios();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

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
            java.util.logging.Logger.getLogger(AsignarAlumnosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AsignarAlumnosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AsignarAlumnosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AsignarAlumnosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AsignarAlumnosDialog dialog = new AsignarAlumnosDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable tablaAlumnos;
    // End of variables declaration//GEN-END:variables
}
