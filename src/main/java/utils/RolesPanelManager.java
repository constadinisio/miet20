package main.java.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import main.java.database.Conexion;
import main.java.views.users.common.RolPanelManagerFactory;
import main.java.views.users.common.VentanaInicio;

/**
 * Clase utilitaria para gestionar el panel de selección de roles. Permite a
 * usuarios con múltiples roles cambiar entre ellos.
 */
public class RolesPanelManager {

    // Componentes
    private JComboBox<String> comboRoles;
    private JLabel lblRoles;
    private JButton btnCambiarRol;
    private JPanel panelContenedor;

    // Datos
    private int userId;
    private JFrame currentFrame;
    private String rolColumnName = "rol_id"; // Valor por defecto que se actualizará
    private boolean isInitializing = true; // Flag para evitar eventos durante la inicialización

    /**
     * Constructor que inicializa el gestor de roles.
     *
     * @param userId ID del usuario actual
     * @param panelContenedor Panel donde se agregará el combo de roles
     * @param currentFrame Ventana actual para cerrarla al cambiar de rol
     */
    public RolesPanelManager(int userId, JPanel panelContenedor, JFrame currentFrame) {
        this.userId = userId;
        this.panelContenedor = panelContenedor;
        this.currentFrame = currentFrame;

        // Validar parámetros
        if (userId <= 0) {
            System.err.println("ADVERTENCIA: ID de usuario inválido: " + userId);
        }
        if (panelContenedor == null) {
            System.err.println("ERROR: Panel contenedor es NULL");
            return;
        }
        if (currentFrame == null) {
            System.err.println("ERROR: Frame actual es NULL");
            return;
        }

        System.out.println("RolesPanelManager iniciado para userId=" + userId);

        // Determinar el nombre de la columna de rol en la base de datos
        determinarNombreColumnaRol();

        // Inicializar componentes UI
        initComponents();

        // Cargar roles - Esto hará visible el combo si tiene múltiples roles
        cargarRolesUsuario();

        // Terminar inicialización
        isInitializing = false;
    }

    /**
     * Inicializa los componentes UI.
     */
    private void initComponents() {
        // Crear componentes
        comboRoles = new JComboBox<>();
        lblRoles = new JLabel("Cambiar rol:");
        btnCambiarRol = new JButton("Cambiar");

        // Configurar apariencia para mayor visibilidad
        lblRoles.setForeground(new Color(255, 255, 0)); // Amarillo para mayor visibilidad
        lblRoles.setFont(new Font("Arial", Font.BOLD, 14));

        comboRoles.setMaximumSize(new java.awt.Dimension(150, 25));
        comboRoles.setPreferredSize(new java.awt.Dimension(150, 25));

        btnCambiarRol.setBackground(new Color(255, 128, 0)); // Naranja para que destaque
        btnCambiarRol.setForeground(new Color(255, 255, 255));
        btnCambiarRol.setFont(new Font("Arial", Font.BOLD, 12));
        btnCambiarRol.setFocusPainted(false);

        // Crear un panel específico para los controles de cambio de rol con borde visible
        JPanel cambioRolPanel = new JPanel();
        cambioRolPanel.setBackground(new Color(60, 60, 60)); // Fondo oscuro para destacar
        cambioRolPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2)); // Borde rojo visible
        cambioRolPanel.add(lblRoles);
        cambioRolPanel.add(comboRoles);
        cambioRolPanel.add(btnCambiarRol);

        // Agregar el panel al principio del panel contenedor
        try {
            // Intentar colocar el panel al principio
            if (panelContenedor.getComponentCount() > 0) {
                // Insertar al inicio
                panelContenedor.add(cambioRolPanel, 0);
            } else {
                panelContenedor.add(cambioRolPanel);
            }
            System.out.println("Panel de roles añadido al panel contenedor");

            // Añadir un mensaje de información
            JLabel lblInfo = new JLabel("* Panel de cambio de roles activo");
            lblInfo.setForeground(Color.GREEN);
            panelContenedor.add(lblInfo, 1);
        } catch (Exception e) {
            System.err.println("Error al añadir panel de roles: " + e.getMessage());
            e.printStackTrace();
        }

        // Asegurarse de que los componentes sean visibles
        lblRoles.setVisible(true);
        comboRoles.setVisible(true);
        btnCambiarRol.setVisible(true);
        cambioRolPanel.setVisible(true);

        // Añadir listener al botón para cambiar de rol
        btnCambiarRol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarRolUsuario();
            }
        });

        System.out.println("Componentes UI inicializados con alta visibilidad");
    }

    /**
     * Determina el nombre correcto de la columna de rol en la tabla
     * usuario_roles. Busca si existe 'rol' o 'rol_id' en la estructura de la
     * tabla.
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
     * Carga los roles disponibles para el usuario actual.
     */
    private void cargarRolesUsuario() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return;
            }

            // Consulta para obtener roles del usuario - usando el nombre de columna detectado
            String query = "SELECT ur." + rolColumnName + " as rol_id, r.nombre AS rol_nombre, ur.is_default "
                    + "FROM usuario_roles ur "
                    + "JOIN roles r ON ur." + rolColumnName + " = r.id "
                    + "WHERE ur.usuario_id = ? "
                    + "ORDER BY ur.is_default DESC";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);

            System.out.println("Ejecutando consulta: " + query.replace("?", String.valueOf(userId)));
            ResultSet rs = ps.executeQuery();

            // Limpiar combo antes de agregar nuevos elementos
            comboRoles.removeAllItems();

            // Limpiar propiedades anteriores para evitar conflictos
            for (int i = 0; i < 10; i++) {
                comboRoles.putClientProperty("rol_" + i, null);
            }

            int rolesCount = 0;
            int defaultRolId = -1;
            String defaultRolNombre = "";

            // Procesar resultados
            while (rs.next()) {
                rolesCount++;
                String rolNombre = rs.getString("rol_nombre");
                int rolId = rs.getInt("rol_id");
                boolean isDefault = rs.getBoolean("is_default");

                System.out.println("Rol encontrado: " + rolNombre + " (ID: " + rolId + ", Default: " + isDefault + ")");

                // Agregar al combo y guardar ID como propiedad
                comboRoles.addItem(rolNombre);
                int currentIndex = comboRoles.getItemCount() - 1;
                String propertyKey = "rol_" + currentIndex;
                comboRoles.putClientProperty(propertyKey, Integer.valueOf(rolId));

                System.out.println("Guardada propiedad " + propertyKey + " con valor " + rolId);

                // Guardar información del rol predeterminado
                if (isDefault) {
                    defaultRolId = rolId;
                    defaultRolNombre = rolNombre;
                    // Seleccionar rol predeterminado
                    comboRoles.setSelectedIndex(currentIndex);
                    System.out.println("Seleccionado rol predeterminado: " + rolNombre);
                }
            }

            // Si hay más de un rol, mostrar los controles de cambio de rol
            boolean tieneMultiplesRoles = (rolesCount > 1);
            if (tieneMultiplesRoles) {
                // Asegurarse de que todos los componentes sean visibles
                for (Component comp : panelContenedor.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        // Si este panel contiene el combo de roles, hacerlo muy visible
                        if (panelContainsComponent(panel, comboRoles)) {
                            panel.setBorder(BorderFactory.createTitledBorder(
                                    BorderFactory.createLineBorder(Color.RED, 2),
                                    "CAMBIAR ROL")); // Añadir un título al borde
                            panel.setVisible(true);

                            // Hacer todos los componentes del panel visibles
                            for (Component c : panel.getComponents()) {
                                c.setVisible(true);
                            }
                        }
                    }
                }
            } else {
                // Solo ocultar si no hay múltiples roles
                hideRolePanel();
            }
            System.out.println("Total roles: " + rolesCount + " - Mostrar selector: " + tieneMultiplesRoles);

            // Establecer visibilidad basada en cantidad de roles
            lblRoles.setVisible(tieneMultiplesRoles);
            comboRoles.setVisible(tieneMultiplesRoles);
            btnCambiarRol.setVisible(tieneMultiplesRoles);

            // Forzar actualización de la UI
            SwingUtilities.invokeLater(() -> {
                panelContenedor.revalidate();
                panelContenedor.repaint();
            });

        } catch (SQLException ex) {
            System.err.println("ERROR al cargar roles: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR general: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean panelContainsComponent(Container container, Component component) {
        for (Component c : container.getComponents()) {
            if (c == component) {
                return true;
            }
        }
        return false;
    }

    private void hideRolePanel() {
        for (Component comp : panelContenedor.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panelContainsComponent(panel, comboRoles)) {
                    panel.setVisible(false);
                }
            }
        }
    }

    /**
     * Maneja el cambio de rol del usuario.
     */
    private void cambiarRolUsuario() {
        // Si estamos en inicialización, ignorar eventos
        if (isInitializing) {
            return;
        }

        int selectedIndex = comboRoles.getSelectedIndex();
        if (selectedIndex == -1) {
            System.out.println("No hay selección en el combo");
            return;
        }

        String propertyKey = "rol_" + selectedIndex;
        Object property = comboRoles.getClientProperty(propertyKey);

        System.out.println("Intentando obtener propiedad: " + propertyKey + ", valor: " + property);

        if (property == null) {
            System.err.println("No se encontró la propiedad para el índice " + selectedIndex);
            return;
        }

        int rolId;
        // Asegurarse de que el valor es un entero
        if (property instanceof Integer) {
            rolId = (Integer) property;
        } else {
            System.err.println("La propiedad no es de tipo Integer: " + property.getClass().getName());
            return;
        }

        String rolNombre = (String) comboRoles.getSelectedItem();

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
        } else {
            System.out.println("Usuario canceló cambio de rol");
            // No hacer nada, dejar al usuario en la pantalla actual
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
                // Usar el nombre de columna detectado
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
                throw ex;  // Re-lanzar la excepción para manejarla más arriba
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

}
