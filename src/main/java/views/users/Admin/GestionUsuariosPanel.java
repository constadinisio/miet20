package main.java.views.users.Admin;

import java.awt.Frame;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import main.java.utils.AsignarRolesDialog;
import main.java.utils.GestionMateriasHorariosDialog;

/**
 * Panel de gestión de usuarios para la interfaz administrativa.
 *
 * Funcionalidades principales: - Listar usuarios con diferentes filtros -
 * Editar información de usuarios - Cambiar estado de usuarios (activo/inactivo)
 *
 * @author [Nicolas Bogarin]
 * @version 1.0
 * @since [12/03/2025]
 */
public class GestionUsuariosPanel extends javax.swing.JPanel {

    // Conexión a la base de datos
    private Connection conect;

    // Modelo de tabla para mostrar usuarios
    private DefaultTableModel tableModel;

    /**
     * Constructor del panel de gestión de usuarios.
     *
     * Inicializa componentes: - Tabla de usuarios - Combo de filtro de roles -
     * Carga inicial de usuarios
     */
    public GestionUsuariosPanel() {
        initComponents();
        inicializarTabla();
        inicializarComboFiltro();
        comboFiltroRol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cargarUsuarios(); // Recargar usuarios cuando cambie la selección
            }
        });

        cargarUsuarios();
    }

    /**
     * Inicializa la tabla de usuarios.
     *
     * Configura columnas y establece la tabla como no editable. Columnas: ID,
     * Nombre, Apellido, Email, Rol, Estado, Fecha Registro
     */
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

    /**
     * Inicializa el combo de filtro de roles.
     *
     * Agrega opciones: - Todos - Administrador - Preceptor - Profesor - Alumno
     * - Pendiente
     */
    private void inicializarComboFiltro() {
        comboFiltroRol.removeAllItems();
        comboFiltroRol.addItem("Todos");
        comboFiltroRol.addItem("Administrador");
        comboFiltroRol.addItem("Preceptor");
        comboFiltroRol.addItem("Profesor");
        comboFiltroRol.addItem("Alumno");
        comboFiltroRol.addItem("Pendiente");
    }

    /**
     * Carga usuarios desde la base de datos.
     *
     * Recupera usuarios según el filtro de rol seleccionado. Muestra
     * información en la tabla: ID, nombre, apellido, email, rol y estado.
     */
    private void cargarUsuarios() {
        try {
            conect = Conexion.getInstancia().verificarConexion();
            String filtroRol = obtenerFiltroRol();

            String query;
            PreparedStatement ps;

            if (!filtroRol.equals("")) {
                // Si hay un filtro de rol específico, buscar en la tabla de relaciones
                query = "SELECT DISTINCT u.id, u.nombre, u.apellido, u.mail, u.rol, u.status "
                        + "FROM usuarios u "
                        + "LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id "
                        + "WHERE u.rol = ? OR ur.rol_id = ?";
                ps = conect.prepareStatement(query);
                ps.setString(1, filtroRol);
                ps.setString(2, filtroRol);
            } else {
                // Si no hay filtro, mostrar todos los usuarios
                query = "SELECT id, nombre, apellido, mail, rol, status FROM usuarios";
                ps = conect.prepareStatement(query);
            }

            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("mail"),
                    convertirRol(rs.getInt("rol")),
                    rs.getString("status").equals("1") || rs.getString("status").equalsIgnoreCase("activo") ? "Activo" : "Inactivo",};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obtiene el filtro de rol seleccionado.
     *
     * @return Código de rol para filtrar usuarios
     */
    private String obtenerFiltroRol() {
        switch (comboFiltroRol.getSelectedIndex()) {
            case 1:
                return "1"; // Administrador
            case 2:
                return "2"; // Preceptor
            case 3:
                return "3"; // Profesor
            case 4:
                return "4"; // Alumno
            case 5:
                return "0"; // Pendiente
            default:
                return ""; // Todos
        }
    }

    /**
     * Convierte código de rol numérico a texto descriptivo.
     *
     * @param rol Código numérico de rol
     * @return Descripción textual del rol
     */
    private String convertirRol(int rolPrincipal) {
        try {
            // Primero, obtener la descripción del rol principal
            String descripcionRol = convertirRolIndividual(rolPrincipal);

            // Luego, verificar si hay roles adicionales
            String query = "SELECT ur.rol_id FROM usuario_roles ur WHERE ur.usuario_id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, (int) tablaUsuarios.getValueAt(tablaUsuarios.getSelectedRow(), 0));
            ResultSet rs = ps.executeQuery();

            StringBuilder rolesAdicionales = new StringBuilder();
            while (rs.next()) {
                int rolId = rs.getInt("rol_id");
                // Evitar duplicar el rol principal
                if (rolId != rolPrincipal) {
                    if (rolesAdicionales.length() > 0) {
                        rolesAdicionales.append(", ");
                    }
                    rolesAdicionales.append(convertirRolIndividual(rolId));
                }
            }

            // Si hay roles adicionales, añadirlos a la descripción
            if (rolesAdicionales.length() > 0) {
                descripcionRol += " + " + rolesAdicionales.toString();
            }

            return descripcionRol;
        } catch (SQLException ex) {
            // En caso de error, simplemente mostrar el rol principal
            return convertirRolIndividual(rolPrincipal);
        }
    }

    private String convertirRolIndividual(int rol) {
        switch (rol) {
            case 1:
                return "Administrador";
            case 2:
                return "Preceptor";
            case 3:
                return "Profesor";
            case 4:
                return "Alumno";
            case 0:
                return "Pendiente";
            default:
                return "Desconocido";
        }
    }

    /**
     * Abre diálogo para editar usuario seleccionado.
     *
     * Valida que se haya seleccionado un usuario. Muestra diálogo de edición y
     * actualiza tabla si hay cambios.
     */
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

    /**
     * Cambia el estado del usuario seleccionado.
     *
     * Alterna entre estados Activo e Inactivo. Actualiza el estado en base de
     * datos y recarga la tabla.
     */
    private void cambiarEstado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
            return;
        }

        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String estadoActual = (String) tablaUsuarios.getValueAt(filaSeleccionada, 5);
        String nuevoEstado = estadoActual.equals("Activo") ? "0" : "1"; // O puedes usar "inactivo" y "activo"

        try {
            String query = "UPDATE usuarios SET status = ? WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, nuevoEstado);
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
        btnAsignarRoles = new javax.swing.JButton();
        btnGestionarHorarios = new javax.swing.JButton();

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

        btnAsignarRoles.setText("Asignar Roles");
        btnAsignarRoles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAsignarRolesActionPerformed(evt);
            }
        });
        add(btnAsignarRoles, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 410, -1, -1));

        btnGestionarHorarios.setText("Gestionar Horarios");
        btnGestionarHorarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarHorariosActionPerformed(evt);
            }
        });
        add(btnGestionarHorarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 410, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        editarUsuario();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnCambiarEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCambiarEstadoActionPerformed
        cambiarEstado();
    }//GEN-LAST:event_btnCambiarEstadoActionPerformed

    private void btnAsignarRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAsignarRolesActionPerformed
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
            return;
        }

        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String nombreUsuario = tablaUsuarios.getValueAt(filaSeleccionada, 1) + " "
                + tablaUsuarios.getValueAt(filaSeleccionada, 2);

        // Crear y mostrar el diálogo de asignación de roles
        Frame parentFrame = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AsignarRolesDialog dialog = new AsignarRolesDialog(parentFrame, true, idUsuario, nombreUsuario);
        dialog.setVisible(true);

        // Actualizar la tabla si se realizaron cambios
        if (dialog.seCambiaronRoles()) {
            cargarUsuarios();
        }
    }//GEN-LAST:event_btnAsignarRolesActionPerformed

    private void btnGestionarHorariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarHorariosActionPerformed
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario");
            return;
        }

        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String nombreUsuario = tablaUsuarios.getValueAt(filaSeleccionada, 1) + " "
                + tablaUsuarios.getValueAt(filaSeleccionada, 2);
        String rol = (String) tablaUsuarios.getValueAt(filaSeleccionada, 4);

        // Solo permitir gestionar horarios para roles específicos
        if (rol.equals("Alumno")) {
            JOptionPane.showMessageDialog(this,
                    "La gestión de materias y horarios no aplica para alumnos.\n"
                    + "Por favor, use el panel de gestión de cursos para asignar alumnos.");
            return;
        }

        // Crear y mostrar el diálogo de gestión de horarios
        Frame parentFrame = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        GestionMateriasHorariosDialog dialog = new GestionMateriasHorariosDialog(
                parentFrame, true, idUsuario, nombreUsuario, rol);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnGestionarHorariosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAsignarRoles;
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnGestionarHorarios;
    private javax.swing.JComboBox<String> comboFiltroRol;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFiltro;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTable tablaUsuarios;
    // End of variables declaration//GEN-END:variables
}
