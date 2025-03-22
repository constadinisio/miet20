package main.java.views.users.Admin;

import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;

/**
 * Panel para gestionar usuarios pendientes de aprobación en el sistema administrativo.
 * 
 * Funcionalidades principales:
 * - Listar usuarios pendientes de asignación de rol
 * - Aprobar usuarios asignándoles un rol específico
 * - Rechazar usuarios cambiando su estado
 * 
 * @author [Nicolas Bogarin]
 * @version 1.0
 * @since [12/03/2025]
 */
public class UsuariosPendientesPanel extends javax.swing.JPanel {

   // Conexión a la base de datos
    private Connection conect;
    
    // Modelo de tabla para mostrar usuarios pendientes
    private DefaultTableModel tableModel;

    /**
     * Constructor del panel de usuarios pendientes.
     * 
     * Inicializa componentes:
     * - Tabla de usuarios
     * - Combo de selección de rol
     * - Carga inicial de usuarios pendientes
     */
    public UsuariosPendientesPanel() {
        initComponents();
        inicializarTabla();
        inicializarComboBox();
        cargarUsuariosPendientes();
    }

    /**
     * Inicializa la tabla de usuarios pendientes.
     * 
     * Configura columnas: ID, Nombre, Apellido, Email, Fecha Registro
     */
    private void inicializarTabla() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Nombre");
        tableModel.addColumn("Apellido");
        tableModel.addColumn("Email");
        tableModel.addColumn("Fecha Registro");
        tablaUsuarios.setModel(tableModel);
    }

    /**
     * Inicializa el combo box de roles.
     * 
     * Agrega opciones:
     * - Seleccionar Rol
     * - Administrador
     * - Preceptor
     * - Profesor
     * - Alumno
     */
    private void inicializarComboBox() {
        comboRol.removeAllItems();
        comboRol.addItem("Seleccionar Rol");
        comboRol.addItem("Administrador");
        comboRol.addItem("Preceptor");
        comboRol.addItem("Profesor");
        comboRol.addItem("Alumno");
    }

    /**
     * Carga usuarios pendientes desde la base de datos.
     * 
     * Recupera usuarios con rol 0 (pendiente) y status activo.
     * Muestra información en la tabla: ID, nombre, apellido, email.
     */
    private void cargarUsuariosPendientes() {
        System.out.println("Cargando usuarios pendientes..."); // Debug
        try {
            conect = Conexion.getInstancia().verificarConexion();
            String query = "SELECT id, nombre, apellido, mail  FROM usuarios WHERE rol = 0 AND status = 1;";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("mail"),};
                tableModel.addRow(row);
                System.out.println("Usuario agregado: " + rs.getString("nombre")); // Debug
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage()); // Debug
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios pendientes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Obtiene el código de rol seleccionado en el combo box.
     * 
     * @return Código numérico de rol
     */
    private int obtenerRolSeleccionado() {
        switch (comboRol.getSelectedIndex()) {
            case 1:
                return 1; // Administrador
            case 2:
                return 2; // Preceptor
            case 3:
                return 3; // Profesor
            case 4:
                return 4; // Alumno
            default:
                return 0;
        }
    }

    /**
     * Aprueba un usuario pendiente.
     * 
     * Valida selección de usuario y rol.
     * Actualiza el rol del usuario en la base de datos.
     */
    private void aprobarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
            return;
        }

        if (comboRol.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un rol");
            return;
        }

        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        int rol = obtenerRolSeleccionado();

        try {
            String query = "UPDATE usuarios SET rol = ? WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, rol);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Usuario aprobado exitosamente");
            cargarUsuariosPendientes();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al aprobar usuario: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Rechaza un usuario pendiente.
     * 
     * Solicita confirmación.
     * Cambia el estado del usuario a inactivo en la base de datos.
     */
    private void rechazarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea rechazar este usuario?",
                "Confirmar Rechazo",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);

            try {
                String query = "UPDATE usuarios SET status = 0 WHERE id = ?";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, idUsuario);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Usuario rechazado exitosamente");
                cargarUsuariosPendientes();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al rechazar usuario: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
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

        lblTitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();
        comboRol = new javax.swing.JComboBox<>();
        lblRol = new javax.swing.JLabel();
        btnAprobar = new javax.swing.JButton();
        btnRechazar = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(758, 713));
        setPreferredSize(new java.awt.Dimension(758, 713));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setText("Usuarios Pendientes");
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

        comboRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(comboRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(466, 181, -1, -1));

        lblRol.setText("Rol");
        add(lblRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(411, 184, 37, -1));

        btnAprobar.setText("Aprobar");
        btnAprobar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAprobarActionPerformed(evt);
            }
        });
        add(btnAprobar, new org.netbeans.lib.awtextra.AbsoluteConstraints(403, 333, -1, -1));

        btnRechazar.setText("Rechazar");
        btnRechazar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRechazarActionPerformed(evt);
            }
        });
        add(btnRechazar, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 333, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAprobarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAprobarActionPerformed
        aprobarUsuario();
    }//GEN-LAST:event_btnAprobarActionPerformed

    private void btnRechazarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRechazarActionPerformed
        rechazarUsuario();
    }//GEN-LAST:event_btnRechazarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAprobar;
    private javax.swing.JButton btnRechazar;
    private javax.swing.JComboBox<String> comboRol;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTable tablaUsuarios;
    // End of variables declaration//GEN-END:variables
}
