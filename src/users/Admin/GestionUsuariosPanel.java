/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package users.Admin;
import java.awt.Frame;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import login.Conexion;
/**
 *
 * @author nico_
 */
public class GestionUsuariosPanel extends javax.swing.JPanel {
private Connection conect;
    private DefaultTableModel tableModel;
    
    public GestionUsuariosPanel() {
        initComponents();
        inicializarTabla();
        inicializarComboFiltro();
        cargarUsuarios();
    }
    
    
    
    private void inicializarTabla() {
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        tableModel.addColumn("ID");
        tableModel.addColumn("Nombre");
        tableModel.addColumn("Apellido");
        tableModel.addColumn("Email");
        tableModel.addColumn("Rol");
        tableModel.addColumn("Estado");
        tableModel.addColumn("Fecha Registro");
        tablaUsuarios.setModel(tableModel);
    }
    
    private void inicializarComboFiltro() {
        comboFiltroRol.removeAllItems();
        comboFiltroRol.addItem("Todos");
        comboFiltroRol.addItem("Administrador");
        comboFiltroRol.addItem("Preceptor");
        comboFiltroRol.addItem("Profesor");
        comboFiltroRol.addItem("Alumno");
        comboFiltroRol.addItem("Pendiente");
    }
    
    private void cargarUsuarios() {
        try {
            conect = Conexion.getInstancia().getConexion();
            String filtroRol = obtenerFiltroRol();
            
            String query = "SELECT id, nombre, apellido, mail, rol, status FROM usuarios";
            if (!filtroRol.equals("")) {
                query += " WHERE rol = " + filtroRol;
            }
            
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("mail"),
                    convertirRol(rs.getInt("rol")),
                    rs.getInt("status") == 1 ? "Activo" : "Inactivo",
                    
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar usuarios: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String obtenerFiltroRol() {
        switch (comboFiltroRol.getSelectedIndex()) {
            case 1: return "1"; // Administrador
            case 2: return "2"; // Preceptor
            case 3: return "3"; // Profesor
            case 4: return "4"; // Alumno
            case 5: return "0"; // Pendiente
            default: return ""; // Todos
        }
    }
    
    private String convertirRol(int rol) {
        switch (rol) {
            case 1: return "Administrador";
            case 2: return "Preceptor";
            case 3: return "Profesor";
            case 4: return "Alumno";
            case 0: return "Pendiente";
            default: return "Desconocido";
        }
    }
    
    private void editarUsuario() {
    int filaSeleccionada = tablaUsuarios.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
        return;
    }
    
    // Obtener el ID del usuario seleccionado
    int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
    
    // Crear y mostrar el diálogo de edición
    EditarUsuarioDialog dialog = new EditarUsuarioDialog(
        (Frame) javax.swing.SwingUtilities.getWindowAncestor(this), 
        true, 
        idUsuario);
    dialog.setVisible(true);
    
    // Si se guardaron cambios, actualizar la tabla
    if (dialog.seGuardaronCambios()) {
        cargarUsuarios();
    }
}
    
    
    
    
    private void cambiarEstado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
            return;
        }
        
        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String estadoActual = (String) tablaUsuarios.getValueAt(filaSeleccionada, 5);
        int nuevoEstado = estadoActual.equals("Activo") ? 0 : 1;
        
        try {
            String query = "UPDATE usuarios SET status = ? WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Estado del usuario actualizado exitosamente");
            cargarUsuarios();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al actualizar estado: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
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

        lblTitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();
        comboFiltroRol = new javax.swing.JComboBox<>();
        lblFiltro = new javax.swing.JLabel();
        btnEditar = new javax.swing.JButton();
        btnCambiarEstado = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(758, 713));
        setPreferredSize(new java.awt.Dimension(758, 713));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setText("Gestion de Usuarios");
        add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(152, 45, 145, -1));

        tablaUsuarios.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tablaUsuarios);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 190, 375, 275));

        comboFiltroRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(comboFiltroRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 210, -1, -1));

        lblFiltro.setText("Filtro Rol");
        add(lblFiltro, new org.netbeans.lib.awtextra.AbsoluteConstraints(411, 184, 70, -1));

        btnEditar.setText("Editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });
        add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(403, 333, -1, -1));

        btnCambiarEstado.setText("Cambiar estado");
        btnCambiarEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCambiarEstadoActionPerformed(evt);
            }
        });
        add(btnCambiarEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 333, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        editarUsuario();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnCambiarEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCambiarEstadoActionPerformed
       cambiarEstado();
    }//GEN-LAST:event_btnCambiarEstadoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnEditar;
    private javax.swing.JComboBox<String> comboFiltroRol;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFiltro;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTable tablaUsuarios;
    // End of variables declaration//GEN-END:variables
}