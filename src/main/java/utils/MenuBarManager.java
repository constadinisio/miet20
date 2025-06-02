package main.java.utils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import main.java.database.Conexion;
import main.java.views.login.LoginForm;
import main.java.updater.ActualizadorApp;
import main.java.views.users.common.RolPanelManagerFactory;
import main.java.views.users.common.VentanaInicio;
import main.java.views.notifications.NotificationSenderWindow;
import main.java.views.notifications.NotificationsWindow;
import main.java.controllers.NotificationGroupManager;
import java.util.Arrays;
import java.util.List;
import main.java.views.notifications.NotificationGroupWindow;
import main.java.tickets.TicketService;

/**
 * MenuBarManager COMPLETAMENTE REDISEÑADO v3.0 Sistema de notificaciones
 * integrado en el menú principal
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 3.0 - Sistema de notificaciones completo en menú
 */
public class MenuBarManager {

    private int userId;
    private JFrame currentFrame;
    private String rolColumnName = "rol_id";
    private int rolActual;

    // SISTEMA DE NOTIFICACIONES
    private NotificationManager notificationManager;
    private boolean notificationsEnabled = false;

    private TicketService ticketService;

    // Roles que pueden enviar notificaciones
    private static final List<Integer> SENDER_ROLES = Arrays.asList(1, 2, 3, 5); // Admin, Preceptor, Profesor, ATTP

    public MenuBarManager(int userId, JFrame currentFrame) {
        this.userId = userId;
        this.currentFrame = currentFrame;

        System.out.println("=== INICIALIZANDO MenuBarManager v3.0 ===");
        System.out.println("Usuario ID: " + userId);

        this.rolActual = obtenerRolActual();
        this.ticketService = TicketService.getInstance();
        System.out.println("Rol actual detectado: " + rolActual + " (" + obtenerTextoRol(rolActual) + ")");

        determinarNombreColumnaRol();
        initializeNotificationSystem();
        setupMenuBar();

        System.out.println("✅ MenuBarManager inicializado correctamente");
    }

    private void initializeNotificationSystem() {
        try {
            System.out.println("--- Inicializando Sistema de Notificaciones ---");

            notificationManager = NotificationManager.getInstance();

            if (!notificationManager.isInitialized()) {
                notificationManager.initialize(userId, rolActual);
            } else {
                notificationManager.updateUser(userId, rolActual);
            }

            if (notificationManager.getNotificationService() != null) {
                System.out.println("✅ NotificationService conectado");
                int unreadCount = notificationManager.getUnreadCount();
                System.out.println("📧 Notificaciones no leídas: " + unreadCount);
                notificationsEnabled = true;
            } else {
                System.err.println("⚠️ NotificationService no está disponible");
                notificationsEnabled = false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error inicializando sistema de notificaciones: " + e.getMessage());
            e.printStackTrace();
            notificationsEnabled = false;
        }
    }

    private int obtenerRolActual() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return 1;
            }

            String query = "SELECT ur." + rolColumnName + " as rol_id "
                    + "FROM usuario_roles ur "
                    + "WHERE ur.usuario_id = ? AND ur.is_default = 1";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int rol = rs.getInt("rol_id");
                System.out.println("Rol actual obtenido desde usuario_roles: " + rol);
                return rol;
            }

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

    public void actualizarRolActual(int nuevoRol) {
        System.out.println("=== ACTUALIZANDO ROL ===");
        System.out.println("Rol anterior: " + rolActual + " (" + obtenerTextoRol(rolActual) + ")");
        System.out.println("Nuevo rol: " + nuevoRol + " (" + obtenerTextoRol(nuevoRol) + ")");

        this.rolActual = nuevoRol;

        // ACTUALIZAR SISTEMA DE NOTIFICACIONES CON NUEVO ROL
        if (notificationManager != null && notificationsEnabled) {
            try {
                notificationManager.updateUser(userId, nuevoRol);
                System.out.println("✅ Sistema de notificaciones actualizado para nuevo rol");

                if (notificationManager.canSendNotifications(nuevoRol)) {
                    SwingUtilities.invokeLater(() -> {
                        notificationManager.enviarNotificacionRapida(
                                "Cambio de Rol",
                                "Has cambiado tu rol a: " + obtenerTextoRol(nuevoRol),
                                userId
                        );
                    });
                }

            } catch (Exception e) {
                System.err.println("⚠️ Error actualizando sistema de notificaciones: " + e.getMessage());
            }
        }

        // Refrescar la barra de menú para mostrar el nuevo rol como actual
        setupMenuBar();

        System.out.println("✅ Cambio de rol completado");
    }

    private void determinarNombreColumnaRol() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("ERROR: No se pudo establecer conexión a la base de datos");
                return;
            }

            DatabaseMetaData meta = conect.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "usuario_roles", null);

            boolean encontrada = false;
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");

                if (columnName.equalsIgnoreCase("rol")) {
                    rolColumnName = "rol";
                    encontrada = true;
                    break;
                } else if (columnName.equalsIgnoreCase("rol_id")) {
                    rolColumnName = "rol_id";
                    encontrada = true;
                    break;
                }
            }

            if (!encontrada) {
                System.out.println("ADVERTENCIA: No se encontró columna 'rol' o 'rol_id'. Usando valor por defecto: " + rolColumnName);
            } else {
                System.out.println("✅ Columna de rol detectada: " + rolColumnName);
            }

        } catch (SQLException ex) {
            System.err.println("ERROR al verificar estructura de tabla usuario_roles: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * CONFIGURACIÓN COMPLETA DE LA BARRA DE MENÚ CON NOTIFICACIONES
     * REORGANIZADAS
     */
    private void setupMenuBar() {
        System.out.println("--- Configurando Barra de Menú ---");

        JMenuBar menuBar = new JMenuBar();

        // CREAR MENÚS PRINCIPALES
        createUserMenu(menuBar);
        createNotificationsMenu(menuBar); // MENÚ DE NOTIFICACIONES REORGANIZADO
        createHelpMenu(menuBar);

        // INTEGRAR CAMPANITA DE NOTIFICACIONES (solo el indicador visual)
        if (notificationsEnabled && notificationManager != null) {
            try {
                System.out.println("🔔 Integrando campanita de notificaciones...");
                notificationManager.integrateWithMenuBar(menuBar);
                System.out.println("✅ Campanita de notificaciones integrada exitosamente");
            } catch (Exception e) {
                System.err.println("❌ Error integrando notificaciones: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ Sistema de notificaciones no disponible");
        }

        currentFrame.setJMenuBar(menuBar);
        currentFrame.revalidate();
        currentFrame.repaint();

        System.out.println("✅ Barra de menú configurada completamente");
        System.out.println("📊 Estado: Rol=" + obtenerTextoRol(rolActual)
                + ", Notificaciones=" + (notificationsEnabled ? "Activas" : "Inactivas"));
    }

    /**
     * NUEVO: Crea el menú específico de notificaciones REORGANIZADO
     */
    private void createNotificationsMenu(JMenuBar menuBar) {
        JMenu notificationsMenu = new JMenu("📬 Notificaciones");

        // === SECCIÓN PARA TODOS LOS USUARIOS ===
        // Opción "Mis Notificaciones" - disponible para TODOS los roles
        JMenuItem myNotificationsItem = new JMenuItem("📬 Mis Notificaciones");
        myNotificationsItem.addActionListener(e -> openMyNotifications());
        notificationsMenu.add(myNotificationsItem);

        // ✅ NUEVO: Opción "Mis Tickets" - disponible para TODOS los roles
        JMenuItem myTicketsItem = new JMenuItem("🎫 Mis Tickets");
        myTicketsItem.addActionListener(e -> abrirMisTickets());
        notificationsMenu.add(myTicketsItem);

        // === SECCIÓN SOLO PARA ROLES AUTORIZADOS ===
        // Verificar si el usuario puede enviar notificaciones
        if (SENDER_ROLES.contains(rolActual)) {
            notificationsMenu.addSeparator();

            // Opción "Enviar Notificación"
            JMenuItem sendNotificationItem = new JMenuItem("📤 Enviar Notificación");
            sendNotificationItem.addActionListener(e -> openSendNotification());
            notificationsMenu.add(sendNotificationItem);

            // Opción "Gestionar Mis Grupos"
            JMenuItem manageGroupsItem = new JMenuItem("👥 Gestionar Mis Grupos");
            manageGroupsItem.addActionListener(e -> openGroupManager());
            notificationsMenu.add(manageGroupsItem);
        }

        // === SECCIÓN ADICIONAL PARA ADMINISTRADORES ===
        if (rolActual == 1) { // Solo admin
            notificationsMenu.addSeparator();

            JMenuItem statsItem = new JMenuItem("📊 Estadísticas del Sistema");
            statsItem.addActionListener(e -> showNotificationStats());
            notificationsMenu.add(statsItem);

            JMenuItem broadcastItem = new JMenuItem("📢 Envío Masivo");
            broadcastItem.addActionListener(e -> openBroadcastNotification());
            notificationsMenu.add(broadcastItem);
        }

        // === SECCIÓN PARA DESARROLLADORES (Gestión de Tickets) ===
        try {
            if (ticketService != null && ticketService.esDeveloper(userId)) {
                notificationsMenu.addSeparator();

                JMenuItem ticketManagementItem = new JMenuItem("🎫 Gestión de Tickets (Dev)");
                ticketManagementItem.addActionListener(e -> abrirGestionTickets());
                notificationsMenu.add(ticketManagementItem);

                // Mostrar contador de tickets pendientes
                int ticketsPendientes = ticketService.contarTicketsPendientes();
                if (ticketsPendientes > 0) {
                    JMenuItem ticketsCountItem = new JMenuItem("🔴 " + ticketsPendientes + " tickets pendientes");
                    ticketsCountItem.setForeground(new Color(220, 53, 69));
                    ticketsCountItem.addActionListener(e -> abrirGestionTickets());
                    notificationsMenu.add(ticketsCountItem);
                }
            }
        } catch (Exception e) {
            System.err.println("Error verificando desarrollador para menú: " + e.getMessage());
        }

        // Agregar menú a la barra
        menuBar.add(notificationsMenu);
    }

    /**
     * Abre la ventana de mis notificaciones
     */
    private void openMyNotifications() {
        SwingUtilities.invokeLater(() -> {
            try {
                NotificationsWindow window = new NotificationsWindow(userId);
                window.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo ventana de notificaciones: " + e.getMessage());
                JOptionPane.showMessageDialog(currentFrame,
                        "Error al abrir las notificaciones: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Abre la ventana para enviar notificaciones
     */
    private void openSendNotification() {
        if (!SENDER_ROLES.contains(rolActual)) {
            JOptionPane.showMessageDialog(currentFrame,
                    "No tienes permisos para enviar notificaciones.",
                    "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                NotificationSenderWindow senderWindow = new NotificationSenderWindow(userId, rolActual);
                senderWindow.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo ventana de envío: " + e.getMessage());
                JOptionPane.showMessageDialog(currentFrame,
                        "Error al abrir la ventana de envío: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Abre el gestor de grupos personalizados
     */
    private void openGroupManager() {
        if (!SENDER_ROLES.contains(rolActual)) {
            JOptionPane.showMessageDialog(currentFrame,
                    "No tienes permisos para gestionar grupos.",
                    "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // ✅ CORRECCIÓN: Usar NotificationGroupWindow en lugar de NotificationGroupManager
                NotificationGroupWindow groupWindow = new NotificationGroupWindow(userId, rolActual);
                groupWindow.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo gestor de grupos: " + e.getMessage());
                JOptionPane.showMessageDialog(currentFrame,
                        "Error al abrir el gestor de grupos: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Muestra estadísticas del sistema de notificaciones (solo admin)
     */
    private void showNotificationStats() {
        if (rolActual != 1) {
            JOptionPane.showMessageDialog(currentFrame,
                    "Solo los administradores pueden ver las estadísticas.",
                    "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (notificationManager != null && notificationManager.canManageNotifications()) {
            String stats = notificationManager.getNotificationStats();

            JTextArea textArea = new JTextArea(stats);
            textArea.setEditable(false);
            textArea.setRows(15);
            textArea.setColumns(60);
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(currentFrame,
                    scrollPane,
                    "Estadísticas del Sistema de Notificaciones",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(currentFrame,
                    "Sistema de notificaciones no disponible.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre ventana para envío masivo (solo admin)
     */
    private void openBroadcastNotification() {
        if (rolActual != 1) {
            JOptionPane.showMessageDialog(currentFrame,
                    "Solo los administradores pueden realizar envíos masivos.",
                    "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Formulario simple para envío masivo
        JPanel panel = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        JTextField titleField = new JTextField(30);
        JTextArea contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        String[] eventTypes = {"Información General", "Mantenimiento", "Urgente", "Evento"};
        JComboBox<String> eventTypeCombo = new JComboBox<>(eventTypes);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        panel.add(eventTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Contenido:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(contentArea), gbc);

        int result = JOptionPane.showConfirmDialog(currentFrame, panel,
                "Envío Masivo a Todos los Usuarios", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String eventType = (String) eventTypeCombo.getSelectedItem();

            if (!title.isEmpty() && !content.isEmpty()) {
                String tipoEvento = eventType.toLowerCase().replace(" ", "_");
                notificationManager.notificarEventoGeneral(title, content, tipoEvento);

                JOptionPane.showMessageDialog(currentFrame,
                        "Notificación enviada a todos los usuarios del sistema.",
                        "Envío Completado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(currentFrame,
                        "Por favor complete el título y contenido.",
                        "Campos Requeridos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Crea el menú de usuario con opciones de cambio de rol
     */
    private void createUserMenu(JMenuBar menuBar) {
        JMenu userMenu = new JMenu("👤 Usuario");

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();

            String query = "SELECT ur." + rolColumnName + " as rol_id, r.nombre AS rol_nombre, ur.is_default "
                    + "FROM usuario_roles ur "
                    + "JOIN roles r ON ur." + rolColumnName + " = r.id "
                    + "WHERE ur.usuario_id = ? "
                    + "ORDER BY ur.is_default DESC";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            int rolesCount = 0;
            JMenu rolesMenu = new JMenu("🔄 Cambiar Rol");

            while (rs.next()) {
                rolesCount++;
                final String rolNombre = rs.getString("rol_nombre");
                final int rolId = rs.getInt("rol_id");

                JMenuItem rolItem = new JMenuItem(rolNombre);

                if (rolId == rolActual) {
                    rolItem.setFont(rolItem.getFont().deriveFont(java.awt.Font.BOLD));
                    rolItem.setText(rolNombre + " (Actual)");
                    rolItem.setEnabled(false);
                }

                if (rolId != rolActual) {
                    rolItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cambiarARol(rolId, rolNombre);
                        }
                    });
                }

                rolesMenu.add(rolItem);
            }

            if (rolesCount > 1) {
                userMenu.add(rolesMenu);
                userMenu.addSeparator();
            }

            System.out.println("📋 Roles cargados en menú: " + rolesCount);

        } catch (SQLException ex) {
            System.err.println("ERROR al cargar roles para el menú: " + ex.getMessage());
            ex.printStackTrace();
        }

        // Opción de cerrar sesión
        JMenuItem logoutItem = new JMenuItem("🚪 Cerrar Sesión");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });
        userMenu.add(logoutItem);

        menuBar.add(userMenu);
    }

    /**
     * Crea el menú de ayuda
     */
    private void createHelpMenu(JMenuBar menuBar) {
        JMenu helpMenu = new JMenu("❓ Ayuda");

        // Opción para verificar actualizaciones
        JMenuItem updateItem = new JMenuItem("🔄 Verificar actualizaciones");
        updateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ActualizadorApp.verificarActualizaciones();
            }
        });
        helpMenu.add(updateItem);

        helpMenu.addSeparator();

        // === SECCIÓN DE TICKETS ===
        // ✅ NUEVO: Opción para ver mis tickets (TODOS LOS USUARIOS)
        JMenuItem misTicketsItem = new JMenuItem("🎫 Mis Tickets Reportados");
        misTicketsItem.addActionListener(e -> abrirMisTickets());
        helpMenu.add(misTicketsItem);

        // Mostrar contador de tickets del usuario si tiene algunos
        try {
            TicketService ticketService = TicketService.getInstance();
            List<main.java.tickets.Ticket> misTickets = ticketService.obtenerTicketsUsuario(userId);

            if (!misTickets.isEmpty()) {
                long pendientes = misTickets.stream()
                        .filter(t -> "ABIERTO".equals(t.getEstado()) || "EN_REVISION".equals(t.getEstado()))
                        .count();

                if (pendientes > 0) {
                    JMenuItem ticketsCountItem = new JMenuItem("🟡 " + pendientes + " ticket(s) pendiente(s)");
                    ticketsCountItem.setForeground(new Color(255, 193, 7));
                    ticketsCountItem.addActionListener(e -> abrirMisTickets());
                    helpMenu.add(ticketsCountItem);
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo contador de tickets del usuario: " + e.getMessage());
        }

        // === SECCIÓN PARA DESARROLLADORES (solo si es desarrollador) ===
        if (ticketService.esDeveloper(userId)) {
            helpMenu.addSeparator();

            JMenuItem ticketManagementItem = new JMenuItem("🎫 Gestión de Tickets (Desarrollador)");
            ticketManagementItem.addActionListener(e -> abrirGestionTickets());
            helpMenu.add(ticketManagementItem);

            // Mostrar contador de tickets pendientes para desarrollador
            int ticketsPendientes = ticketService.contarTicketsPendientes();
            if (ticketsPendientes > 0) {
                JMenuItem devTicketsCountItem = new JMenuItem("🔴 " + ticketsPendientes + " tickets pendientes (Dev)");
                devTicketsCountItem.setForeground(new Color(220, 53, 69));
                devTicketsCountItem.addActionListener(e -> abrirGestionTickets());
                helpMenu.add(devTicketsCountItem);
            }
        }

        // === OPCIÓN DE REPORTAR PARA TODOS ===
        helpMenu.addSeparator();
        JMenuItem reportarTicketItem = new JMenuItem("🎫 Reportar Error/Sugerencia");
        reportarTicketItem.addActionListener(e -> abrirReporteTicket());
        helpMenu.add(reportarTicketItem);

        // Opción de prueba de notificaciones (para desarrollo)
        if (notificationManager != null && notificationManager.canSendNotifications(rolActual)) {
            helpMenu.addSeparator();
            JMenuItem testNotifItem = new JMenuItem("🧪 Probar Notificaciones");
            testNotifItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    notificationManager.enviarNotificacionPrueba();
                }
            });
            helpMenu.add(testNotifItem);
        }

        helpMenu.addSeparator();

        // Opción de Acerca de con versión
        JMenuItem aboutItem = new JMenuItem("ℹ️ Acerca de");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mensaje = "Sistema de Gestión Escolar ET20\n"
                        + "Versión: " + ActualizadorApp.VERSION_ACTUAL + "\n\n"
                        + "Características:\n"
                        + "• Sistema de notificaciones completo\n"
                        + "• Sistema de tickets y seguimiento\n"
                        + "• Gestión de grupos personalizados\n"
                        + "• Selección jerárquica de destinatarios\n"
                        + "• Gestión multi-rol\n"
                        + "• Interface responsive\n"
                        + "• Gestión completa de usuarios";

                JOptionPane.showMessageDialog(
                        currentFrame,
                        mensaje,
                        "Acerca de",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);
    }

    /**
     * Cambia al rol seleccionado CON NOTIFICACIÓN DEL CAMBIO.
     */
    private void cambiarARol(int rolId, String rolNombre) {
        int confirm = JOptionPane.showConfirmDialog(
                currentFrame,
                "¿Cambiar al rol " + rolNombre + "?\n\n"
                + "Esto actualizará tu perfil y las funciones disponibles.",
                "Cambiar Rol",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("=== PROCESANDO CAMBIO DE ROL ===");
            System.out.println("Usuario: " + userId);
            System.out.println("Rol destino: " + rolNombre + " (ID: " + rolId + ")");

            try {
                actualizarRolPredeterminado(rolId);
                abrirPantallaPorRol(rolId);
                System.out.println("✅ Cambio de rol completado exitosamente");

            } catch (Exception e) {
                System.err.println("❌ Error durante cambio de rol: " + e.getMessage());
                e.printStackTrace();

                JOptionPane.showMessageDialog(currentFrame,
                        "Error al cambiar de rol: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Actualiza el rol predeterminado del usuario en la base de datos.
     */
    private void actualizarRolPredeterminado(int rolId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos");
            }

            conect.setAutoCommit(false);

            try {
                String resetQuery = "UPDATE usuario_roles SET is_default = 0 WHERE usuario_id = ?";
                PreparedStatement resetPs = conect.prepareStatement(resetQuery);
                resetPs.setInt(1, userId);
                int resetRows = resetPs.executeUpdate();

                String updateQuery = "UPDATE usuario_roles SET is_default = 1 WHERE usuario_id = ? AND " + rolColumnName + " = ?";
                PreparedStatement updatePs = conect.prepareStatement(updateQuery);
                updatePs.setInt(1, userId);
                updatePs.setInt(2, rolId);
                int updatedRows = updatePs.executeUpdate();

                String userUpdateQuery = "UPDATE usuarios SET rol = ? WHERE id = ?";
                PreparedStatement userUpdatePs = conect.prepareStatement(userUpdateQuery);
                userUpdatePs.setInt(1, rolId);
                userUpdatePs.setInt(2, userId);
                int userUpdatedRows = userUpdatePs.executeUpdate();

                conect.commit();

                System.out.println("📊 Base de datos actualizada:");
                System.out.println("  - Roles reseteados: " + resetRows);
                System.out.println("  - Rol predeterminado actualizado: " + updatedRows);
                System.out.println("  - Usuario actualizado: " + userUpdatedRows);

            } catch (SQLException ex) {
                try {
                    conect.rollback();
                    System.err.println("❌ Transacción revertida");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Error al revertir transacción: " + rollbackEx.getMessage());
                }
                throw ex;
            } finally {
                conect.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            System.err.println("❌ Error actualizando rol predeterminado: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error en base de datos: " + ex.getMessage());
        }
    }

    /**
     * Cambia la visualización al rol seleccionado CON NOTIFICACIONES.
     */
    private void abrirPantallaPorRol(int rolId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos");
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

                System.out.println("👤 Usuario: " + nombreCompleto);
                System.out.println("🎭 Cambiando a rol: " + obtenerTextoRol(rolId));

                if (currentFrame instanceof VentanaInicio) {
                    VentanaInicio ventana = (VentanaInicio) currentFrame;

                    actualizarRolActual(rolId);
                    ventana.setRolPanelManager(RolPanelManagerFactory.createManager(ventana, userId, rolId));
                    ventana.setUserRol(rolId);
                    ventana.configurePanelBotones();

                    if (rolId == 4) { // Alumno
                        int anio = rs.getInt("anio");
                        String division = rs.getString("division");
                        String cursoDiv = anio + "°" + division;
                        ventana.updateAlumnoLabels(nombreCompleto, obtenerTextoRol(rolId), cursoDiv);
                    } else {
                        ventana.updateLabels(nombreCompleto, obtenerTextoRol(rolId));
                    }

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        ventana.updateFotoPerfil(fotoUrl);
                    }

                    ventana.restaurarVistaPrincipal();
                    System.out.println("✅ Vista actualizada para rol: " + obtenerTextoRol(rolId));

                } else {
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }

                    VentanaInicio nuevaVentana = new VentanaInicio(userId, rolId);

                    if (rolId == 4) { // Alumno
                        int anio = rs.getInt("anio");
                        String division = rs.getString("division");
                        String cursoDiv = anio + "°" + division;
                        nuevaVentana.updateAlumnoLabels(nombreCompleto, obtenerTextoRol(rolId), cursoDiv);
                    } else {
                        nuevaVentana.updateLabels(nombreCompleto, obtenerTextoRol(rolId));
                    }

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        nuevaVentana.updateFotoPerfil(fotoUrl);
                    }

                    currentFrame = nuevaVentana;
                    actualizarRolActual(rolId);
                    nuevaVentana.setVisible(true);

                    System.out.println("✅ Nueva ventana creada para rol: " + obtenerTextoRol(rolId));
                }

                // ENVIAR NOTIFICACIÓN DE CONFIRMACIÓN DEL CAMBIO
                if (notificationManager != null && notificationsEnabled) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Thread.sleep(1000);
                            notificationManager.enviarNotificacionRapida(
                                    "Cambio de Rol Exitoso",
                                    "Has cambiado exitosamente al rol: " + obtenerTextoRol(rolId)
                                    + ". Ya tienes acceso a las nuevas funciones.",
                                    userId
                            );
                        } catch (Exception e) {
                            System.err.println("Error env notif cambio rol: " + e.getMessage());
                        }
                    });
                }

            } else {
                throw new SQLException("No se encontró información del usuario con ID: " + userId);
            }

        } catch (SQLException ex) {
            System.err.println("❌ Error cambiando pantalla por rol: " + ex.getMessage());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(currentFrame,
                    "Error al cambiar de rol: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obtiene el texto descriptivo del rol.
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
     * Cierra la sesión actual CON LIMPIEZA DE NOTIFICACIONES.
     */
    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(
                currentFrame,
                "¿Está seguro que desea cerrar sesión?\n\n"
                + "Se perderán las notificaciones no guardadas y se cerrará la aplicación.",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("=== CERRANDO SESIÓN ===");

            if (notificationManager != null) {
                try {
                    if (notificationsEnabled) {
                        notificationManager.enviarNotificacionRapida(
                                "Sesión Cerrada",
                                "Has cerrado sesión correctamente. ¡Hasta pronto!",
                                userId
                        );

                        Thread.sleep(500);
                    }

                    notificationManager.dispose();
                    System.out.println("✅ Recursos de notificaciones liberados");

                } catch (Exception e) {
                    System.err.println("⚠️ Error liberando recursos de notificaciones: " + e.getMessage());
                }
            }

            currentFrame.dispose();
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);

            System.out.println("✅ Sesión cerrada correctamente");
        }
    }

    // ========================================
    // MÉTODOS PÚBLICOS PARA USO EXTERNO
    // ========================================
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public void enviarNotificacionRapida(String titulo, String contenido, int... destinatarios) {
        if (notificationManager != null && notificationsEnabled) {
            notificationManager.enviarNotificacionRapida(titulo, contenido, destinatarios);
        } else {
            System.err.println("⚠️ Sistema de notificaciones no disponible");
        }
    }

    public void enviarNotificacionARol(String titulo, String contenido, int rolDestino) {
        if (notificationManager != null && notificationsEnabled) {
            notificationManager.enviarNotificacionARol(titulo, contenido, rolDestino);
        } else {
            System.err.println("⚠️ Sistema de notificaciones no disponible");
        }
    }

    public void enviarNotificacionUrgente(String titulo, String contenido, int... destinatarios) {
        if (notificationManager != null && notificationsEnabled) {
            notificationManager.enviarNotificacionUrgente(titulo, contenido, destinatarios);
        } else {
            System.err.println("⚠️ Sistema de notificaciones no disponible");
        }
    }

    // ========================================
    // MÉTODOS DE INFORMACIÓN Y ESTADO
    // ========================================
    public int getUserId() {
        return userId;
    }

    public int getRolActual() {
        return rolActual;
    }

    public String getRolActualTexto() {
        return obtenerTextoRol(rolActual);
    }

    public boolean puedeEnviarNotificaciones() {
        if (notificationManager != null && notificationsEnabled) {
            return notificationManager.canSendNotifications(rolActual);
        }
        return false;
    }

    public boolean puedeGestionarNotificaciones() {
        if (notificationManager != null && notificationsEnabled) {
            return notificationManager.canManageNotifications();
        }
        return false;
    }

    public int getNotificacionesNoLeidas() {
        if (notificationManager != null && notificationsEnabled) {
            return notificationManager.getUnreadCount();
        }
        return 0;
    }

    public boolean isNotificacionesHabilitadas() {
        return notificationsEnabled && notificationManager != null;
    }

    public void actualizarNotificaciones() {
        if (notificationManager != null && notificationsEnabled) {
            notificationManager.forceRefresh();
        }
    }

    // ========================================
    // MÉTODOS DE LIMPIEZA Y CIERRE
    // ========================================
    public void dispose() {
        System.out.println("=== LIMPIANDO RECURSOS MenuBarManager ===");

        try {
            if (notificationManager != null) {
                notificationManager.dispose();
                System.out.println("✅ NotificationManager limpiado");
            }

            notificationManager = null;
            currentFrame = null;

            System.out.println("✅ MenuBarManager limpiado correctamente");

        } catch (Exception e) {
            System.err.println("⚠️ Error durante limpieza: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    /**
     * Abre la ventana de gestión de tickets (solo desarrolladores)
     */
    private void abrirGestionTickets() {
        SwingUtilities.invokeLater(() -> {
            try {
                main.java.tickets.TicketManagementWindow ticketWindow
                        = new main.java.tickets.TicketManagementWindow(userId);
                ticketWindow.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo gestión de tickets: " + e.getMessage());
                JOptionPane.showMessageDialog(currentFrame,
                        "Error al abrir la gestión de tickets: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Abre la ventana de reporte de tickets (todos los usuarios)
     */
    private void abrirReporteTicket() {
        SwingUtilities.invokeLater(() -> {
            try {
                main.java.tickets.TicketReportWindow reportWindow
                        = new main.java.tickets.TicketReportWindow(userId);
                reportWindow.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo reporte de ticket: " + e.getMessage());
                JOptionPane.showMessageDialog(currentFrame,
                        "Error al abrir el reporte de tickets: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * ✅ NUEVO: Abre la ventana de seguimiento de tickets del usuario
     */
    private void abrirMisTickets() {
        SwingUtilities.invokeLater(() -> {
            try {
                main.java.tickets.UserTicketsWindow userTicketsWindow
                        = new main.java.tickets.UserTicketsWindow(userId);
                userTicketsWindow.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo mis tickets: " + e.getMessage());
                JOptionPane.showMessageDialog(currentFrame,
                        "Error al abrir el seguimiento de tickets: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
