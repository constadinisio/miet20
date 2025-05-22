package main.java.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import main.java.database.Conexion;
import main.java.views.login.LoginForm;
import main.java.updater.ActualizadorApp;
import main.java.views.users.common.RolPanelManagerFactory;
import main.java.views.users.common.VentanaInicio;

/**
 * Clase utilitaria para crear y gestionar la barra de menú común que incluye la
 * funcionalidad de cambio de roles.
 * Versión corregida que actualiza correctamente el rol actual.
 */
public class MenuBarManager {

    private int userId;
    private JFrame currentFrame;
    private String rolColumnName = "rol_id"; // Valor por defecto que se actualizará
    private int rolActual; // Variable para trackear el rol actual

    /**
     * Constructor que inicializa el gestor de menú.
     *
     * @param userId ID del usuario actual
     * @param currentFrame Ventana actual donde se añadirá el menú
     */
    public MenuBarManager(int userId, JFrame currentFrame) {
        this.userId = userId;
        this.currentFrame = currentFrame;

        // Obtener el rol actual del usuario
        this.rolActual = obtenerRolActual();

        // Determinar el nombre correcto de la columna de rol en la base de datos
        determinarNombreColumnaRol();

        // Crear y configurar la barra de menú
        setupMenuBar();
    }

    /**
     * Obtiene el rol actual del usuario desde la base de datos.
     */
    private int obtenerRolActual() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return 1; // Rol por defecto
            }

            // Primero intentar obtener desde usuario_roles (rol por defecto)
            String query = "SELECT ur." + rolColumnName + " as rol_id " +
                          "FROM usuario_roles ur " +
                          "WHERE ur.usuario_id = ? AND ur.is_default = 1";
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int rol = rs.getInt("rol_id");
                System.out.println("Rol actual obtenido desde usuario_roles: " + rol);
                return rol;
            }

            // Si no hay rol por defecto en usuario_roles, obtener desde usuarios
            query = "SELECT rol FROM usuarios WHERE id = ?";
            ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                int rol = rs.getInt("rol");
                System.out.println("Rol actual obtenido desde usuarios: " + rol);
                return rol;
            }

        } catch (SQLException ex) {
            System.err.println("Error al obtener rol actual: " + ex.getMessage());
            ex.printStackTrace();
        }

        return 1; // Rol por defecto si hay error
    }

    /**
     * Actualiza el rol actual y refresca la barra de menú.
     * Este método debe ser llamado después de cambiar de rol.
     */
    public void actualizarRolActual(int nuevoRol) {
        this.rolActual = nuevoRol;
        System.out.println("MenuBarManager: Actualizando rol actual a: " + nuevoRol);
        
        // Refrescar la barra de menú para mostrar el nuevo rol como actual
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
                
                // Marcar el rol actual (no solo el default de la BD)
                if (rolId == rolActual) {
                    rolItem.setFont(rolItem.getFont().deriveFont(java.awt.Font.BOLD));
                    rolItem.setText(rolNombre + " (Actual)");
                    rolItem.setEnabled(false); // Deshabilitar el rol actual para evitar clicks innecesarios
                }

                // Añadir acción para cambiar a este rol (solo si no es el actual)
                if (rolId != rolActual) {
                    rolItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cambiarARol(rolId, rolNombre);
                        }
                    });
                }

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

        // Forzar actualización visual
        currentFrame.revalidate();
        currentFrame.repaint();

        System.out.println("Barra de menú configurada. Rol actual marcado: " + rolActual + " (" + obtenerTextoRol(rolActual) + ")");
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
     * Cambia la visualización al rol seleccionado. Versión adaptada para
     * VentanaInicio.
     *
     * @param rolId ID del rol
     */
    private void abrirPantallaPorRol(int rolId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return;
            }

            String query = "SELECT nombre, apellido, mail, anio, division, foto_url FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String nombreCompleto = nombre + " " + apellido;
                String fotoUrl = rs.getString("foto_url");
                System.out.println("Cambiando a rol: " + rolId + " - Usuario: " + nombreCompleto);

                // Si la ventana actual es VentanaInicio
                if (currentFrame instanceof VentanaInicio) {
                    VentanaInicio ventana = (VentanaInicio) currentFrame;

                    // IMPORTANTE: Actualizar el rol actual ANTES de refrescar la barra de menú
                    actualizarRolActual(rolId);

                    // Actualizar el gestor de paneles según el nuevo rol
                    ventana.setRolPanelManager(RolPanelManagerFactory.createManager(ventana, userId, rolId));

                    // Actualizar al nuevo rol usando métodos públicos
                    ventana.setUserRol(rolId);

                    // Reconfigurar los botones
                    ventana.configurePanelBotones();

                    // Actualizar etiquetas según el rol
                    if (rolId == 4) { // Alumno
                        // Obtener curso y división
                        int anio = rs.getInt("anio");
                        String division = rs.getString("division");
                        String cursoDiv = anio + "°" + division;

                        // Actualizar etiquetas de alumno
                        ventana.updateAlumnoLabels(nombreCompleto, obtenerTextoRol(rolId), cursoDiv);
                    } else {
                        // Actualizar etiquetas estándar
                        ventana.updateLabels(nombreCompleto, obtenerTextoRol(rolId));
                    }

                    // Actualizar foto de perfil si existe
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        ventana.updateFotoPerfil(fotoUrl);
                    }

                    // Restaurar la vista principal para el nuevo rol
                    ventana.restaurarVistaPrincipal();

                    System.out.println("Rol cambiado a: " + obtenerTextoRol(rolId));
                } else {
                    // Cerrar ventana actual y crear nueva ventana unificada
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }

                    // Crear nueva instancia de VentanaInicio
                    VentanaInicio nuevaVentana = new VentanaInicio(userId, rolId);

                    // Actualizar etiquetas según el rol
                    if (rolId == 4) { // Alumno
                        // Obtener curso y división
                        int anio = rs.getInt("anio");
                        String division = rs.getString("division");
                        String cursoDiv = anio + "°" + division;

                        // Actualizar etiquetas de alumno
                        nuevaVentana.updateAlumnoLabels(nombreCompleto, obtenerTextoRol(rolId), cursoDiv);
                    } else {
                        // Actualizar etiquetas estándar
                        nuevaVentana.updateLabels(nombreCompleto, obtenerTextoRol(rolId));
                    }

                    // Actualizar foto de perfil si existe
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        nuevaVentana.updateFotoPerfil(fotoUrl);
                    }

                    // Actualizar referencia a la ventana actual
                    currentFrame = nuevaVentana;

                    // IMPORTANTE: Actualizar el rol actual para la nueva ventana
                    actualizarRolActual(rolId);

                    // Mostrar la nueva ventana
                    nuevaVentana.setVisible(true);
                    System.out.println("Pantalla de " + obtenerTextoRol(rolId) + " abierta");
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
     * Obtiene el texto descriptivo del rol.
     *
     * @param rol Código numérico del rol
     * @return Descripción textual del rol
     */
    private String obtenerTextoRol(int rol) {
        switch (rol) {
            case 1:
                return "Administrador";
            case 2:
                return "Preceptor";
            case 3:
                return "Profesor";
            case 4:
                return "Estudiante";
            case 5:
                return "ATTP";
            default:
                return "Usuario";
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