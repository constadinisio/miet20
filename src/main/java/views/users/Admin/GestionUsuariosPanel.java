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
    // Primero, obtener la descripción del rol principal 
    String descripcionRol = convertirRolIndividual(rolPrincipal);
    
    // Solo intentar obtener roles adicionales si hay una fila seleccionada
    int filaSeleccionada = tablaUsuarios.getSelectedRow();
    if (filaSeleccionada != -1) {
        try {
            // Luego, verificar si hay roles adicionales
            String query = "SELECT ur.rol_id FROM usuario_roles ur WHERE ur.usuario_id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, (int) tablaUsuarios.getValueAt(filaSeleccionada, 0));
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
        } catch (SQLException ex) {
            // En caso de error, simplemente mostrar el rol principal
        }
    }
    
    return descripcionRol;
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

        lblTitulo.setText("Gestion de Usuarios");

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

        comboFiltroRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblFiltro.setText("Filtro Rol");

        btnEditar.setText("Editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnCambiarEstado.setText("Cambiar estado");
        btnCambiarEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCambiarEstadoActionPerformed(evt);
            }
        });

        btnAsignarRoles.setText("Asignar Roles");
        btnAsignarRoles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAsignarRolesActionPerformed(evt);
            }
        });

        btnGestionarHorarios.setText("Gestionar Horarios");
        btnGestionarHorarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGestionarHorariosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 641, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(134, 134, 134)
                            .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(8, 8, 8)
                                    .addComponent(lblFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(7, 7, 7)
                                    .addComponent(comboFiltroRol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnEditar)
                                    .addGap(8, 8, 8)
                                    .addComponent(btnCambiarEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(7, 7, 7)
                                    .addComponent(btnAsignarRoles, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnGestionarHorarios, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(lblTitulo)
                    .addGap(123, 123, 123)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblFiltro)
                            .addGap(10, 10, 10)
                            .addComponent(comboFiltroRol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(101, 101, 101)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnEditar)
                                .addComponent(btnCambiarEstado))
                            .addGap(54, 54, 54)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnAsignarRoles)
                                .addComponent(btnGestionarHorarios))))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
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
