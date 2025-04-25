package main.java.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import main.java.database.Conexion;
import main.java.views.login.LoginForm;
import main.java.views.users.Admin.admin;
import main.java.views.users.Alumnos.alumnos;
import main.java.views.users.Preceptor.preceptor;
import main.java.views.users.Profesor.profesor;
import main.java.views.users.Attp.attp;
import main.java.updater.ActualizadorApp;

/**
 * Clase utilitaria para crear y gestionar la barra de menú común que incluye la
 * funcionalidad de cambio de roles.
 */
public class MenuBarManager {

    private int userId;
    private JFrame currentFrame;
    private String rolColumnName = "rol_id"; // Valor por defecto que se actualizará

    /**
     * Constructor que inicializa el gestor de menú.
     *
     * @param userId ID del usuario actual
     * @param currentFrame Ventana actual donde se añadirá el menú
     */
    public MenuBarManager(int userId, JFrame currentFrame) {
        this.userId = userId;
        this.currentFrame = currentFrame;

        // Determinar el nombre correcto de la columna de rol en la base de datos
        determinarNombreColumnaRol();

        // Crear y configurar la barra de menú
        setupMenuBar();
    }

    /**
     * Determina el nombre correcto de la columna de rol en la tabla
     * usuario_roles.
     */
    private void determinarNombreColumnaRol() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return;
            }

            // Obtener metadatos de la base de datos
            DatabaseMetaData meta = conect.getMetaData();

            // Buscar columnas en la tabla usuario_roles
            ResultSet columns = meta.getColumns(null, null, "usuario_roles", null);

            boolean encontrada = false;

            System.out.println("Buscando nombre de columna para rol en la tabla usuario_roles:");

            // Buscar columna de rol (podría ser 'rol' o 'rol_id')
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                System.out.println("- Columna encontrada: " + columnName);

                if (columnName.equalsIgnoreCase("rol")) {
                    rolColumnName = "rol";
                    encontrada = true;
                    System.out.println("  → Usando nombre de columna: 'rol'");
                    break;
                } else if (columnName.equalsIgnoreCase("rol_id")) {
                    rolColumnName = "rol_id";
                    encontrada = true;
                    System.out.println("  → Usando nombre de columna: 'rol_id'");
                    break;
                }
            }

            if (!encontrada) {
                System.out.println("ADVERTENCIA: No se encontró columna 'rol' o 'rol_id'. Usando valor por defecto: " + rolColumnName);
            }

        } catch (SQLException ex) {
            System.err.println("ERROR al verificar estructura de tabla usuario_roles: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Configura y añade la barra de menú a la ventana actual.
     */
    private void setupMenuBar() {
    // Crear la barra de menú
    JMenuBar menuBar = new JMenuBar();

    // Menú de usuario
    JMenu userMenu = new JMenu("Usuario");

    // Obtener roles y crear submenú de roles si hay más de uno
    try {
        // Consulta para obtener roles disponibles
        Connection conect = Conexion.getInstancia().verificarConexion();

        String query = "SELECT ur." + rolColumnName + " as rol_id, r.nombre AS rol_nombre, ur.is_default "
                + "FROM usuario_roles ur "
                + "JOIN roles r ON ur." + rolColumnName + " = r.id "
                + "WHERE ur.usuario_id = ? "
                + "ORDER BY ur.is_default DESC";

        PreparedStatement ps = conect.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        // Contar roles y crear submenú si hay más de uno
        int rolesCount = 0;
        JMenu rolesMenu = new JMenu("Cambiar Rol");

        // Procesar roles
        while (rs.next()) {
            rolesCount++;
            final String rolNombre = rs.getString("rol_nombre");
            final int rolId = rs.getInt("rol_id");
            final boolean isDefault = rs.getBoolean("is_default");

            // Crear elemento de menú para este rol
            JMenuItem rolItem = new JMenuItem(rolNombre);
            if (isDefault) {
                rolItem.setFont(rolItem.getFont().deriveFont(java.awt.Font.BOLD));
                rolItem.setText(rolNombre + " (Actual)");
            }

            // Añadir acción para cambiar a este rol
            rolItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cambiarARol(rolId, rolNombre);
                }
            });

            // Añadir al submenú de roles
            rolesMenu.add(rolItem);
        }

        // Añadir submenú de roles si hay más de uno
        if (rolesCount > 1) {
            userMenu.add(rolesMenu);
        }

    } catch (SQLException ex) {
        System.err.println("ERROR al cargar roles para el menú: " + ex.getMessage());
        ex.printStackTrace();
    }

    // Añadir opción de cerrar sesión
    JMenuItem logoutItem = new JMenuItem("Cerrar Sesión");
    logoutItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            cerrarSesion();
        }
    });
    userMenu.add(logoutItem);

    // Añadir menú de usuario a la barra
    menuBar.add(userMenu);

    // Añadir menú de Ayuda con opción de actualizaciones
    JMenu helpMenu = new JMenu("Ayuda");
    
    // Opción para verificar actualizaciones
    JMenuItem updateItem = new JMenuItem("Verificar actualizaciones");
    updateItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ActualizadorApp.verificarActualizaciones();
        }
    });
    helpMenu.add(updateItem);
    
    // Opción de Acerca de con versión
    JMenuItem aboutItem = new JMenuItem("Acerca de");
    aboutItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(
                currentFrame,
                "Sistema de Gestión Escolar ET20\nVersión: " + ActualizadorApp.VERSION_ACTUAL,
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    });
    helpMenu.add(aboutItem);
    
    // Añadir menú de Ayuda a la barra
    menuBar.add(helpMenu);

    // Establecer la barra de menú en el frame actual
    currentFrame.setJMenuBar(menuBar);

    System.out.println("Barra de menú configurada con opciones de cambio de rol y verificación de actualizaciones");
}

    /**
     * Cambia al rol seleccionado.
     *
     * @param rolId ID del rol seleccionado
     * @param rolNombre Nombre del rol para mostrar en mensajes
     */
    private void cambiarARol(int rolId, String rolNombre) {
        int confirm = JOptionPane.showConfirmDialog(
                currentFrame,
                "¿Cambiar al rol " + rolNombre + "?",
                "Cambiar Rol",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("Usuario confirmó cambio al rol: " + rolNombre + " (ID: " + rolId + ")");
            actualizarRolPredeterminado(rolId);
            abrirPantallaPorRol(rolId);
        }
    }

    /**
     * Actualiza el rol predeterminado del usuario en la base de datos.
     *
     * @param rolId El ID del rol que se establecerá como predeterminado
     */
    private void actualizarRolPredeterminado(int rolId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return;
            }

            // Iniciar transacción
            conect.setAutoCommit(false);

            try {
                // Primero, establecer todos los roles como no predeterminados
                String resetQuery = "UPDATE usuario_roles SET is_default = 0 WHERE usuario_id = ?";
                PreparedStatement resetPs = conect.prepareStatement(resetQuery);
                resetPs.setInt(1, userId);
                int resetRows = resetPs.executeUpdate();
                System.out.println("Filas reseteadas: " + resetRows);

                // Luego, establecer el rol seleccionado como predeterminado
                String updateQuery = "UPDATE usuario_roles SET is_default = 1 WHERE usuario_id = ? AND " + rolColumnName + " = ?";
                PreparedStatement updatePs = conect.prepareStatement(updateQuery);
                updatePs.setInt(1, userId);
                updatePs.setInt(2, rolId);
                int updatedRows = updatePs.executeUpdate();
                System.out.println("Filas actualizadas: " + updatedRows);

                // Actualizar también la tabla usuarios
                String userUpdateQuery = "UPDATE usuarios SET rol = ? WHERE id = ?";
                PreparedStatement userUpdatePs = conect.prepareStatement(userUpdateQuery);
                userUpdatePs.setInt(1, rolId);
                userUpdatePs.setInt(2, userId);
                int userUpdatedRows = userUpdatePs.executeUpdate();
                System.out.println("Filas de usuario actualizadas: " + userUpdatedRows);

                // Confirmar transacción
                conect.commit();
                System.out.println("Transacción completada. Rol predeterminado actualizado a: " + rolId);

            } catch (SQLException ex) {
                // Revertir cambios en caso de error
                try {
                    conect.rollback();
                    System.err.println("Transacción revertida debido a un error");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al revertir la transacción: " + rollbackEx.getMessage());
                }
                throw ex;
            } finally {
                // Restaurar modo auto-commit
                conect.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            System.err.println("ERROR al actualizar rol predeterminado: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Abre la pantalla correspondiente al rol seleccionado.
     *
     * @param rolId ID del rol seleccionado
     */
    private void abrirPantallaPorRol(int rolId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return;
            }

            String query = "SELECT nombre, apellido, mail FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String nombreCompleto = nombre + " " + apellido;

                System.out.println("Abriendo pantalla para rol: " + rolId + " - Usuario: " + nombreCompleto);

                // Cerrar ventana actual
                if (currentFrame != null) {
                    currentFrame.dispose();
                }

                // Abrir la pantalla correspondiente según el rol
                switch (rolId) {
                    case 1: // Administrador
                        admin adminForm = new admin(nombre, apellido, "Administrador");
                        adminForm.setVisible(true);
                        System.out.println("Pantalla de Administrador abierta");
                        break;
                    case 2: // Preceptor
                        preceptor preceptorForm = new preceptor(userId);
                        preceptorForm.updateLabels(nombreCompleto);
                        preceptorForm.setVisible(true);
                        System.out.println("Pantalla de Preceptor abierta");
                        break;
                    case 3: // Profesor
                        profesor profesorForm = new profesor(userId);
                        profesorForm.updateLabels(nombreCompleto);
                        profesorForm.setVisible(true);
                        System.out.println("Pantalla de Profesor abierta");
                        break;
                    case 4: // Alumno
                        alumnos alumnoForm = new alumnos(userId);
                        alumnoForm.setVisible(true);
                        System.out.println("Pantalla de Alumno abierta");
                        break;
                    case 5: // ATTP
                        attp attpForm = new attp(userId);
                        attpForm.updateLabels(nombreCompleto);
                        attpForm.setVisible(true);
                        System.out.println("Pantalla de ATTP abierta");
                        break;
                    default:
                        JOptionPane.showMessageDialog(null,
                                "Rol no reconocido (ID: " + rolId + ")",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Rol no reconocido: " + rolId);
                        break;
                }
            } else {
                System.err.println("No se encontró el usuario con ID: " + userId);
                JOptionPane.showMessageDialog(null,
                        "No se encontró información del usuario",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            System.err.println("ERROR al cambiar de rol: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al cambiar de rol: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     */
    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(
                currentFrame,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            currentFrame.dispose();
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        }
    }
}
