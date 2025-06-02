package main.java.utils;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import main.java.components.NotificationBellComponent;
import main.java.models.Notificacion;
import main.java.services.NotificationService;
import main.java.views.notifications.NotificationSenderWindow;

/**
 * Gestor de notificaciones que se integra con MenuBarManager Proporciona
 * métodos para envío rápido y gestión de notificaciones
 *
 * Patrón Singleton para asegurar una sola instancia en toda la aplicación
 */
public class NotificationManager {

    private static NotificationManager instance;
    private final NotificationService notificationService;
    private NotificationBellComponent bellComponent;
    private int currentUserId;
    private int currentUserRole;
    private boolean initialized = false;

    // ✅ CAMBIO PRINCIPAL: Inicialización lazy para evitar dependencia circular
    private main.java.tickets.TicketBellComponent ticketBellComponent;
    private main.java.tickets.TicketService ticketService; // No inicializar en constructor
    private boolean ticketServiceInitialized = false;

    // Roles permitidos para enviar notificaciones
    private static final List<Integer> SENDER_ROLES = Arrays.asList(1, 2, 3, 5); // Admin, Preceptor, Profesor, ATTP
    private static final int ADMIN_ROLE = 1;

    private NotificationManager() {
        this.notificationService = NotificationService.getInstance();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * ✅ NUEVO: Inicialización lazy del TicketService
     */
    private void initializeTicketServiceIfNeeded() {
        if (!ticketServiceInitialized) {
            try {
                this.ticketService = main.java.tickets.TicketService.getInstance();
                this.ticketServiceInitialized = true;
                System.out.println("✅ TicketService inicializado correctamente");
            } catch (Exception e) {
                System.err.println("⚠️ Error inicializando TicketService: " + e.getMessage());
                this.ticketService = null;
                this.ticketServiceInitialized = false;
            }
        }
    }

    /**
     * Inicializa el gestor con el usuario actual Debe llamarse después del
     * login exitoso
     */
    public void initialize(int userId, int userRole) {
        if (userId <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido: " + userId);
        }

        this.currentUserId = userId;
        this.currentUserRole = userRole;
        this.initialized = true;

        System.out.println("✅ NotificationManager inicializado para usuario: " + userId + ", rol: " + userRole);
    }

    /**
     * Verifica si el manager está inicializado
     */
    private void checkInitialized() {
        if (!initialized || currentUserId <= 0) {
            throw new IllegalStateException("NotificationManager no ha sido inicializado. Llame a initialize() primero.");
        }
    }

    /**
     * Crea e integra la campanita de notificaciones en la barra de menú
     */
    public void integrateWithMenuBar(JMenuBar menuBar) {
        checkInitialized();

        if (menuBar == null) {
            throw new IllegalArgumentException("MenuBar no puede ser null");
        }

        try {
            // Crear campanita de notificaciones normal
            bellComponent = new NotificationBellComponent(currentUserId);

            // Crear panel contenedor para los componentes de notificación
            JPanel notificationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            notificationPanel.setOpaque(false);

            // Agregar campanita normal
            notificationPanel.add(bellComponent);

            // ✅ NUEVO: Agregar campanita de tickets si es desarrollador (inicialización segura)
            if (isTicketDeveloper()) {
                try {
                    main.java.tickets.TicketBellComponent ticketBell
                            = new main.java.tickets.TicketBellComponent(currentUserId);
                    notificationPanel.add(ticketBell);

                    // Guardar referencia para poder refrescar
                    this.ticketBellComponent = ticketBell;
                    System.out.println("✅ Campanita de tickets agregada para desarrollador");
                } catch (Exception e) {
                    System.err.println("⚠️ Error creando campanita de tickets: " + e.getMessage());
                    // Continuar sin la campanita de tickets
                }
            }

            // Si el usuario puede enviar notificaciones, agregar botón de envío
            if (canSendNotifications()) {
                JButton sendButton = createSendNotificationButton();
                notificationPanel.add(sendButton);
            }

            // Agregar al extremo derecho de la barra de menú
            menuBar.add(javax.swing.Box.createHorizontalGlue());
            menuBar.add(notificationPanel);

            System.out.println("✅ Campanitas de notificaciones integradas en MenuBar");

        } catch (Exception e) {
            System.err.println("❌ Error integrando campanitas: " + e.getMessage());
            e.printStackTrace();

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Error al cargar el sistema de notificaciones.\nAlgunas funciones pueden no estar disponibles.",
                        "Error de Sistema",
                        JOptionPane.WARNING_MESSAGE);
            });
        }
    }

    /**
     * ✅ NUEVO: Fuerza actualización de la campanita de tickets
     */
    public void refreshTicketBell() {
        if (ticketBellComponent != null) {
            try {
                ticketBellComponent.forceRefresh();
            } catch (Exception e) {
                System.err.println("Error refrescando campanita de tickets: " + e.getMessage());
            }
        }
    }

    /**
     * ✅ NUEVO: Obtiene el componente de tickets (para acceso externo)
     */
    public main.java.tickets.TicketBellComponent getTicketBellComponent() {
        return ticketBellComponent;
    }

    /**
     * ✅ NUEVO: Obtiene el TicketService de forma segura
     */
    public main.java.tickets.TicketService getTicketService() {
        initializeTicketServiceIfNeeded();
        return ticketService;
    }

    /**
     * Crea el botón para enviar notificaciones
     */
    private JButton createSendNotificationButton() {
        JButton sendButton = new JButton("📤");
        sendButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 16));
        sendButton.setToolTipText("Enviar notificación");
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover effect
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                sendButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 18));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                sendButton.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 16));
            }
        });

        sendButton.addActionListener(e -> openNotificationSender());

        return sendButton;
    }

    /**
     * Abre la ventana para enviar notificaciones
     */
    private void openNotificationSender() {
        if (!canSendNotifications()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "No tienes permisos para enviar notificaciones.",
                        "Acceso denegado",
                        JOptionPane.WARNING_MESSAGE);
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                NotificationSenderWindow senderWindow = new NotificationSenderWindow(currentUserId, currentUserRole);
                senderWindow.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error abriendo ventana de envío: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Error al abrir la ventana de envío de notificaciones.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Verifica si el usuario actual puede enviar notificaciones
     */
    public boolean canSendNotifications() {
        return initialized && SENDER_ROLES.contains(currentUserRole);
    }

    /**
     * Verifica si el usuario actual puede gestionar completamente las
     * notificaciones
     */
    public boolean canManageNotifications() {
        return initialized && currentUserRole == ADMIN_ROLE;
    }

    // =======================
    // MÉTODOS DE ENVÍO RÁPIDO
    // =======================
    /**
     * Método de conveniencia para envío rápido de notificaciones individuales
     */
    public CompletableFuture<Boolean> enviarNotificacionRapida(String titulo, String contenido, int... destinatarios) {
        checkInitialized();

        if (!canSendNotifications()) {
            System.err.println("❌ El usuario actual no puede enviar notificaciones");
            return CompletableFuture.completedFuture(false);
        }

        if (titulo == null || titulo.trim().isEmpty()) {
            System.err.println("❌ El título no puede estar vacío");
            return CompletableFuture.completedFuture(false);
        }

        if (contenido == null || contenido.trim().isEmpty()) {
            System.err.println("❌ El contenido no puede estar vacío");
            return CompletableFuture.completedFuture(false);
        }

        if (destinatarios == null || destinatarios.length == 0) {
            System.err.println("❌ Debe especificar al menos un destinatario");
            return CompletableFuture.completedFuture(false);
        }

        try {
            Notificacion notificacion = new Notificacion(titulo, contenido, "INDIVIDUAL", currentUserId);

            // Convertir array de primitivos a Lista de Integer
            List<Integer> destinatariosList = Arrays.stream(destinatarios)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());

            notificacion.setDestinatariosIndividuales(destinatariosList);

            return notificationService.enviarNotificacion(notificacion)
                    .thenApply(exito -> {
                        if (exito) {
                            System.out.println("✅ Notificación rápida enviada exitosamente a " + destinatarios.length + " destinatario(s)");
                        } else {
                            System.err.println("❌ Error enviando notificación rápida");
                        }
                        return exito;
                    });

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando notificación rápida: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Envía notificación a un rol completo
     */
    public CompletableFuture<Boolean> enviarNotificacionARol(String titulo, String contenido, int rolDestino) {
        checkInitialized();

        if (!canSendNotifications()) {
            System.err.println("❌ El usuario actual no puede enviar notificaciones");
            return CompletableFuture.completedFuture(false);
        }

        if (titulo == null || titulo.trim().isEmpty() || contenido == null || contenido.trim().isEmpty()) {
            System.err.println("❌ Título y contenido son requeridos");
            return CompletableFuture.completedFuture(false);
        }

        if (rolDestino <= 0) {
            System.err.println("❌ Rol destino inválido: " + rolDestino);
            return CompletableFuture.completedFuture(false);
        }

        try {
            Notificacion notificacion = new Notificacion(titulo, contenido, "ROL", currentUserId);
            notificacion.setRolDestino(rolDestino);

            return notificationService.enviarNotificacion(notificacion)
                    .thenApply(exito -> {
                        if (exito) {
                            System.out.println("✅ Notificación a rol " + rolDestino + " enviada exitosamente");
                        } else {
                            System.err.println("❌ Error enviando notificación a rol " + rolDestino);
                        }
                        return exito;
                    });

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando notificación a rol: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Envía notificación urgente
     */
    public CompletableFuture<Boolean> enviarNotificacionUrgente(String titulo, String contenido, int... destinatarios) {
        checkInitialized();

        if (!canSendNotifications()) {
            System.err.println("❌ El usuario actual no puede enviar notificaciones");
            return CompletableFuture.completedFuture(false);
        }

        try {
            Notificacion notificacion = new Notificacion(titulo, contenido, "INDIVIDUAL", currentUserId);

            List<Integer> destinatariosList = Arrays.stream(destinatarios)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());

            notificacion.setDestinatariosIndividuales(destinatariosList);
            notificacion.setPrioridad("URGENTE");
            notificacion.setColor("#dc3545");
            notificacion.setIcono("⚠️");

            return notificationService.enviarNotificacion(notificacion)
                    .thenApply(exito -> {
                        if (exito) {
                            System.out.println("🚨 Notificación urgente enviada exitosamente");
                        } else {
                            System.err.println("❌ Error enviando notificación urgente");
                        }
                        return exito;
                    });

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando notificación urgente: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===================================
    // MÉTODOS ESPECÍFICOS DEL SISTEMA ESCOLAR
    // ===================================
    /**
     * Notifica sobre nuevas notas publicadas
     */
    public CompletableFuture<Boolean> notificarNuevaNota(int alumnoId, String materia, String trabajo) {
        String titulo = "Nueva nota disponible";
        String contenido = String.format("Se ha publicado tu nota para %s - %s", materia, trabajo);

        return enviarNotificacionConDetalles(titulo, contenido, "NORMAL", "#28a745", "📊", alumnoId);
    }

    /**
     * Notifica sobre falta de asistencia
     */
    public CompletableFuture<Boolean> notificarFaltaAsistencia(int alumnoId, String fecha, String materia) {
        String titulo = "Falta registrada";
        String contenido = String.format("Se registró tu falta el %s en %s", fecha, materia);

        return enviarNotificacionConDetalles(titulo, contenido, "ALTA", "#fd7e14", "❌", alumnoId);
    }

    /**
     * Notifica sobre boletín disponible
     */
    public CompletableFuture<Boolean> notificarBoletinDisponible(int alumnoId, String periodo) {
        String titulo = "Boletín disponible";
        String contenido = String.format("Tu boletín del %s ya está disponible para descargar", periodo);

        return enviarNotificacionConDetalles(titulo, contenido, "NORMAL", "#28a745", "📋", alumnoId);
    }

    /**
     * Notifica a profesores sobre trabajos pendientes de corrección
     */
    public CompletableFuture<Boolean> notificarTrabajosPendientes(int profesorId, int cantidadTrabajos, String materia) {
        String titulo = "Trabajos pendientes de corrección";
        String contenido = String.format("Tienes %d trabajo%s pendiente%s de corrección en %s",
                cantidadTrabajos,
                cantidadTrabajos == 1 ? "" : "s",
                cantidadTrabajos == 1 ? "" : "s",
                materia);

        return enviarNotificacionConDetalles(titulo, contenido, "NORMAL", "#17a2b8", "📝", profesorId);
    }

    /**
     * Notifica evento o aviso general a todos los roles
     */
    // ========================================
// MÉTODOS CORREGIDOS Y OPTIMIZADOS DEL NotificationManager
// ========================================
    /**
     * Notifica evento o aviso general a todos los roles - MÉTODO CORREGIDO
     * Línea aproximada: ~320
     */
    public CompletableFuture<Boolean> notificarEventoGeneral(String titulo, String contenido, String tipoEvento) {
        checkInitialized();

        if (!canSendNotifications()) {
            System.err.println("❌ El usuario actual no puede enviar notificaciones");
            return CompletableFuture.completedFuture(false);
        }

        try {
            // CORREGIDO: Crear una notificación por cada rol en lugar de reutilizar la misma
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();

            // Enviar a cada rol por separado
            for (int rol = 1; rol <= 5; rol++) {
                Notificacion notificacion = new Notificacion(titulo, contenido, "ROL", currentUserId);

                // Configurar según tipo de evento
                switch (tipoEvento.toLowerCase()) {
                    case "urgente":
                        notificacion.setPrioridad("URGENTE");
                        notificacion.setColor("#dc3545");
                        notificacion.setIcono("🚨");
                        break;
                    case "evento":
                        notificacion.setPrioridad("NORMAL");
                        notificacion.setColor("#6f42c1");
                        notificacion.setIcono("📅");
                        break;
                    case "mantenimiento":
                        notificacion.setPrioridad("ALTA");
                        notificacion.setColor("#fd7e14");
                        notificacion.setIcono("🔧");
                        break;
                    default:
                        notificacion.setPrioridad("NORMAL");
                        notificacion.setColor("#007bff");
                        notificacion.setIcono("ℹ️");
                }

                notificacion.setRolDestino(rol);
                futures.add(notificationService.enviarNotificacion(notificacion));
            }

            // Esperar a que todas las notificaciones se envíen
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        boolean todoExitoso = futures.stream()
                                .allMatch(future -> {
                                    try {
                                        return future.get();
                                    } catch (Exception e) {
                                        return false;
                                    }
                                });

                        if (todoExitoso) {
                            System.out.println("✅ Evento general enviado exitosamente a todos los roles");
                        } else {
                            System.err.println("❌ Error enviando evento general a algunos roles");
                        }
                        return todoExitoso;
                    });

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando evento general: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * NUEVO: Envía notificación con detalles específicos (MÉTODO PÚBLICO) Para
     * uso desde AcademicNotificationHelper y otros componentes externos
     */
    public CompletableFuture<Boolean> enviarNotificacionConDetalles(String titulo, String contenido,
            String prioridad, String color,
            String icono, int... destinatarios) {
        checkInitialized();

        if (!canSendNotifications()) {
            System.err.println("❌ El usuario actual no puede enviar notificaciones");
            return CompletableFuture.completedFuture(false);
        }

        if (titulo == null || titulo.trim().isEmpty()) {
            System.err.println("❌ El título no puede estar vacío");
            return CompletableFuture.completedFuture(false);
        }

        if (contenido == null || contenido.trim().isEmpty()) {
            System.err.println("❌ El contenido no puede estar vacío");
            return CompletableFuture.completedFuture(false);
        }

        if (destinatarios == null || destinatarios.length == 0) {
            System.err.println("❌ Debe especificar al menos un destinatario");
            return CompletableFuture.completedFuture(false);
        }

        try {
            Notificacion notificacion = new Notificacion(titulo, contenido, "INDIVIDUAL", currentUserId);

            // Convertir array de primitivos a Lista de Integer
            List<Integer> destinatariosList = Arrays.stream(destinatarios)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());

            notificacion.setDestinatariosIndividuales(destinatariosList);

            // Configurar detalles específicos
            if (prioridad != null && !prioridad.trim().isEmpty()) {
                notificacion.setPrioridad(prioridad);
            }

            if (color != null && !color.trim().isEmpty()) {
                notificacion.setColor(color);
            }

            if (icono != null && !icono.trim().isEmpty()) {
                notificacion.setIcono(icono);
            }

            return notificationService.enviarNotificacion(notificacion)
                    .thenApply(exito -> {
                        if (exito) {
                            System.out.println("✅ Notificación con detalles enviada exitosamente a " + destinatarios.length + " destinatario(s)");
                        } else {
                            System.err.println("❌ Error enviando notificación con detalles");
                        }
                        return exito;
                    });

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando notificación con detalles: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    // =======================
    // MÉTODOS DE ACCESO Y UTILIDAD
    // =======================
    /**
     * Obtiene la campanita de notificaciones para acceso directo
     */
    public NotificationBellComponent getBellComponent() {
        return bellComponent;
    }

    /**
     * Fuerza actualización del contador de notificaciones
     */
    public void forceRefresh() {
        if (bellComponent != null) {
            // El componente tiene su propio sistema de refresh automático
            // Pero podemos forzar una actualización
            System.out.println("🔄 Forzando actualización de notificaciones...");
        } else {
            System.out.println("⚠️ Campanita de notificaciones no inicializada");
        }
    }

    /**
     * Obtiene el servicio de notificaciones para uso avanzado
     */
    public NotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * Obtiene el ID del usuario actual
     */
    public int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Obtiene el rol del usuario actual
     */
    public int getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * Verifica si el manager está inicializado
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Actualiza el usuario y rol actual (para cambios de rol)
     */
    public void updateUser(int userId, int userRole) {
        checkInitialized();

        System.out.println("🔄 Actualizando usuario en NotificationManager");
        System.out.println("Usuario anterior: " + currentUserId + ", rol: " + currentUserRole);
        System.out.println("Usuario nuevo: " + userId + ", rol: " + userRole);

        this.currentUserId = userId;
        this.currentUserRole = userRole;

        // Si hay campanita activa, actualizarla
        if (bellComponent != null) {
            bellComponent.dispose();
            bellComponent = new NotificationBellComponent(userId);
        }

        // ✅ ACTUALIZAR: También actualizar campanita de tickets si existe
        if (ticketBellComponent != null) {
            ticketBellComponent.dispose();
            ticketBellComponent = null;
        }

        // Verificar si el nuevo usuario es desarrollador
        if (isTicketDeveloper()) {
            try {
                ticketBellComponent = new main.java.tickets.TicketBellComponent(userId);
                System.out.println("✅ Campanita de tickets actualizada para nuevo usuario");
            } catch (Exception e) {
                System.err.println("⚠️ Error actualizando campanita de tickets: " + e.getMessage());
            }
        }

        System.out.println("✅ Usuario actualizado en NotificationManager");
    }

    /**
     * Verifica si un rol específico puede enviar notificaciones
     */
    public boolean canSendNotifications(int role) {
        return SENDER_ROLES.contains(role);
    }

    /**
     * Obtiene el contador de notificaciones no leídas
     */
    public int getUnreadCount() {
        checkInitialized();

        try {
            return notificationService.contarNotificacionesNoLeidas(currentUserId);
        } catch (Exception e) {
            System.err.println("Error obteniendo contador de notificaciones: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene estadísticas del sistema de notificaciones (solo admin)
     */
    public String getNotificationStats() {
        if (!canManageNotifications()) {
            return "No tienes permisos para ver estadísticas de notificaciones.";
        }

        try {
            StringBuilder stats = new StringBuilder();
            stats.append("=== ESTADÍSTICAS DEL SISTEMA DE NOTIFICACIONES ===\n\n");

            // Estadísticas básicas
            int totalNotifications = getTotalNotifications();
            int unreadNotifications = getTotalUnreadNotifications();
            int usersWithNotifications = getUsersWithNotifications();

            stats.append("📊 Resumen General:\n");
            stats.append("  • Total de notificaciones: ").append(totalNotifications).append("\n");
            stats.append("  • Notificaciones no leídas: ").append(unreadNotifications).append("\n");
            stats.append("  • Usuarios con notificaciones: ").append(usersWithNotifications).append("\n\n");

            // Estadísticas por prioridad
            stats.append("🎯 Por Prioridad:\n");
            stats.append("  • Urgentes: ").append(getNotificationsByPriority("URGENTE")).append("\n");
            stats.append("  • Altas: ").append(getNotificationsByPriority("ALTA")).append("\n");
            stats.append("  • Normales: ").append(getNotificationsByPriority("NORMAL")).append("\n");
            stats.append("  • Bajas: ").append(getNotificationsByPriority("BAJA")).append("\n\n");

            // Usuario actual
            stats.append("👤 Tu cuenta:\n");
            stats.append("  • Notificaciones no leídas: ").append(getUnreadCount()).append("\n");
            stats.append("  • Rol actual: ").append(getCurrentUserRole()).append("\n");
            stats.append("  • Puede enviar: ").append(canSendNotifications() ? "Sí" : "No").append("\n");

            return stats.toString();

        } catch (Exception e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());
            return "Error al obtener estadísticas: " + e.getMessage();
        }
    }

    /**
     * Envía una notificación de prueba (para desarrollo)
     */
    public void enviarNotificacionPrueba() {
        if (!canSendNotifications()) {
            System.err.println("No tienes permisos para enviar notificaciones de prueba");
            return;
        }

        String titulo = "🧪 Notificación de Prueba";
        String contenido = "Esta es una notificación de prueba enviada desde el sistema. "
                + "Si ves este mensaje, el sistema está funcionando correctamente. "
                + "Hora: " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        enviarNotificacionRapida(titulo, contenido, currentUserId)
                .thenAccept(exito -> {
                    if (exito) {
                        System.out.println("✅ Notificación de prueba enviada exitosamente");
                    } else {
                        System.err.println("❌ Error enviando notificación de prueba");
                    }
                });
    }

    // Métodos auxiliares para estadísticas
    private int getTotalNotifications() {
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();
            String query = "SELECT COUNT(*) FROM notificaciones WHERE estado = 'ACTIVA'";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo total de notificaciones: " + e.getMessage());
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
        return 0;
    }

    private int getTotalUnreadNotifications() {
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();
            String query = """
            SELECT COUNT(*) FROM notificaciones_destinatarios nd 
            INNER JOIN notificaciones n ON nd.notificacion_id = n.id 
            WHERE nd.estado_lectura = 'NO_LEIDA' AND n.estado = 'ACTIVA'
            """;
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo notificaciones no leídas: " + e.getMessage());
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
        return 0;
    }

    private int getUsersWithNotifications() {
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();
            String query = """
            SELECT COUNT(DISTINCT nd.destinatario_id) FROM notificaciones_destinatarios nd 
            INNER JOIN notificaciones n ON nd.notificacion_id = n.id 
            WHERE n.estado = 'ACTIVA'
            """;
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo usuarios con notificaciones: " + e.getMessage());
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
        return 0;
    }

    private int getNotificationsByPriority(String priority) {
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();
            String query = "SELECT COUNT(*) FROM notificaciones WHERE prioridad = ? AND estado = 'ACTIVA'";
            ps = conn.prepareStatement(query);
            ps.setString(1, priority);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo notificaciones por prioridad: " + e.getMessage());
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Reinicia el manager (útil para cambio de usuario)
     */
    public void reset() {
        if (bellComponent != null) {
            bellComponent.dispose();
            bellComponent = null;
        }

        if (ticketBellComponent != null) {
            ticketBellComponent.dispose();
            ticketBellComponent = null;
        }

        currentUserId = 0;
        currentUserRole = 0;
        initialized = false;
        ticketServiceInitialized = false;
        ticketService = null;

        System.out.println("🔄 NotificationManager reiniciado");
    }

    /**
     * Limpia recursos al cerrar la aplicación
     */
    public void dispose() {
        try {
            if (bellComponent != null) {
                bellComponent.dispose();
                bellComponent = null;
            }

            if (notificationService != null) {
                notificationService.shutdown();
            }

            // ✅ NUEVO: Limpiar campanita de tickets
            if (ticketBellComponent != null) {
                ticketBellComponent.dispose();
                ticketBellComponent = null;
            }

            initialized = false;
            currentUserId = 0;
            currentUserRole = 0;
            ticketServiceInitialized = false;
            ticketService = null;

            System.out.println("✅ NotificationManager recursos liberados");

        } catch (Exception e) {
            System.err.println("❌ Error liberando recursos del NotificationManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Envía notificación a grupo personalizado
     */
    public CompletableFuture<Boolean> enviarNotificacionAGrupoPersonalizado(String titulo, String contenido,
            int grupoPersonalizadoId,
            String prioridad) {
        checkInitialized();

        if (!canSendNotifications()) {
            System.err.println("❌ El usuario actual no puede enviar notificaciones");
            return CompletableFuture.completedFuture(false);
        }

        if (titulo == null || titulo.trim().isEmpty() || contenido == null || contenido.trim().isEmpty()) {
            System.err.println("❌ Título y contenido son requeridos");
            return CompletableFuture.completedFuture(false);
        }

        if (grupoPersonalizadoId <= 0) {
            System.err.println("❌ ID de grupo personalizado inválido: " + grupoPersonalizadoId);
            return CompletableFuture.completedFuture(false);
        }

        try {
            Notificacion notificacion = new Notificacion();
            notificacion.setTitulo(titulo);
            notificacion.setContenido(contenido);
            notificacion.setTipoNotificacion("GRUPO_PERSONALIZADO");
            notificacion.setRemitenteId(currentUserId);
            notificacion.setGrupoPersonalizadoId(grupoPersonalizadoId);

            if (prioridad != null && !prioridad.trim().isEmpty()) {
                notificacion.setPrioridad(prioridad);
            } else {
                notificacion.setPrioridad("NORMAL");
            }

            return notificationService.enviarNotificacion(notificacion)
                    .thenApply(exito -> {
                        if (exito) {
                            System.out.println("✅ Notificación a grupo personalizado " + grupoPersonalizadoId + " enviada exitosamente");
                        } else {
                            System.err.println("❌ Error enviando notificación a grupo personalizado " + grupoPersonalizadoId);
                        }
                        return exito;
                    });

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando notificación a grupo personalizado: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * NUEVO: Obtiene estadísticas específicas del usuario actual
     */
    public CompletableFuture<UserNotificationStats> getUserNotificationStats() {
        checkInitialized();

        return CompletableFuture.supplyAsync(() -> {
            UserNotificationStats stats = new UserNotificationStats();
            java.sql.PreparedStatement ps = null;
            java.sql.ResultSet rs = null;

            try {
                java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();

                // Estadísticas básicas del usuario
                String query = """
                SELECT 
                    COUNT(*) as total_notificaciones,
                    SUM(CASE WHEN nd.estado_lectura = 'NO_LEIDA' THEN 1 ELSE 0 END) as no_leidas,
                    SUM(CASE WHEN nd.estado_lectura = 'LEIDA' THEN 1 ELSE 0 END) as leidas,
                    SUM(CASE WHEN nd.estado_lectura = 'CONFIRMADA' THEN 1 ELSE 0 END) as confirmadas
                FROM notificaciones_destinatarios nd
                INNER JOIN notificaciones n ON nd.notificacion_id = n.id
                WHERE nd.destinatario_id = ? AND n.estado = 'ACTIVA'
                """;

                ps = conn.prepareStatement(query);
                ps.setInt(1, currentUserId);
                rs = ps.executeQuery();

                if (rs.next()) {
                    stats.totalNotificaciones = rs.getInt("total_notificaciones");
                    stats.noLeidas = rs.getInt("no_leidas");
                    stats.leidas = rs.getInt("leidas");
                    stats.confirmadas = rs.getInt("confirmadas");
                }

                // Estadísticas por prioridad
                rs.close();
                ps.close();

                query = """
                SELECT n.prioridad, COUNT(*) as cantidad
                FROM notificaciones_destinatarios nd
                INNER JOIN notificaciones n ON nd.notificacion_id = n.id
                WHERE nd.destinatario_id = ? AND n.estado = 'ACTIVA'
                GROUP BY n.prioridad
                """;

                ps = conn.prepareStatement(query);
                ps.setInt(1, currentUserId);
                rs = ps.executeQuery();

                while (rs.next()) {
                    String prioridad = rs.getString("prioridad");
                    int cantidad = rs.getInt("cantidad");
                    stats.porPrioridad.put(prioridad, cantidad);
                }

            } catch (Exception e) {
                System.err.println("Error obteniendo estadísticas del usuario: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error cerrando recursos: " + e.getMessage());
                }
            }

            return stats;
        });
    }

    /**
     * NUEVO: Marca todas las notificaciones del usuario como leídas
     */
    public CompletableFuture<Boolean> marcarTodasComoLeidas() {
        checkInitialized();

        return CompletableFuture.supplyAsync(() -> {
            java.sql.PreparedStatement ps = null;
            try {
                java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();

                String query = """
                UPDATE notificaciones_destinatarios nd
                INNER JOIN notificaciones n ON nd.notificacion_id = n.id
                SET nd.estado_lectura = 'LEIDA', nd.fecha_leida = NOW()
                WHERE nd.destinatario_id = ? 
                AND nd.estado_lectura = 'NO_LEIDA'
                AND n.estado = 'ACTIVA'
                """;

                ps = conn.prepareStatement(query);
                ps.setInt(1, currentUserId);

                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    System.out.println("✅ " + filasAfectadas + " notificaciones marcadas como leídas");

                    // Forzar actualización de la campanita
                    if (bellComponent != null) {
                        SwingUtilities.invokeLater(() -> forceRefresh());
                    }

                    return true;
                }

                return false;

            } catch (Exception e) {
                System.err.println("Error marcando todas como leídas: " + e.getMessage());
                return false;
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error cerrando recursos: " + e.getMessage());
                }
            }
        });
    }

    /**
     * NUEVO: Obtiene las últimas notificaciones del usuario
     */
    public CompletableFuture<List<NotificationSummary>> getRecentNotifications(int limit) {
        checkInitialized();

        return CompletableFuture.supplyAsync(() -> {
            List<NotificationSummary> notifications = new ArrayList<>();
            java.sql.PreparedStatement ps = null;
            java.sql.ResultSet rs = null;

            try {
                java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();

                String query = """
                SELECT n.id, n.titulo, n.contenido, n.fecha_creacion, n.prioridad, 
                       n.icono, n.color, nd.estado_lectura
                FROM notificaciones_destinatarios nd
                INNER JOIN notificaciones n ON nd.notificacion_id = n.id
                WHERE nd.destinatario_id = ? AND n.estado = 'ACTIVA'
                ORDER BY n.fecha_creacion DESC
                LIMIT ?
                """;

                ps = conn.prepareStatement(query);
                ps.setInt(1, currentUserId);
                ps.setInt(2, limit);
                rs = ps.executeQuery();

                while (rs.next()) {
                    NotificationSummary notification = new NotificationSummary();
                    notification.id = rs.getInt("id");
                    notification.titulo = rs.getString("titulo");
                    notification.contenido = rs.getString("contenido");
                    notification.fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
                    notification.prioridad = rs.getString("prioridad");
                    notification.icono = rs.getString("icono");
                    notification.color = rs.getString("color");
                    notification.estadoLectura = rs.getString("estado_lectura");

                    notifications.add(notification);
                }

            } catch (Exception e) {
                System.err.println("Error obteniendo notificaciones recientes: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error cerrando recursos: " + e.getMessage());
                }
            }

            return notifications;
        });
    }

// ========================================
// CLASES AUXILIARES PARA ESTADÍSTICAS
// ========================================
    /**
     * Clase para estadísticas específicas del usuario
     */
    public static class UserNotificationStats {

        public int totalNotificaciones = 0;
        public int noLeidas = 0;
        public int leidas = 0;
        public int confirmadas = 0;
        public Map<String, Integer> porPrioridad = new HashMap<>();

        @Override
        public String toString() {
            return String.format("Total: %d, No leídas: %d, Leídas: %d, Confirmadas: %d",
                    totalNotificaciones, noLeidas, leidas, confirmadas);
        }
    }

    /**
     * Clase para resumen de notificaciones
     */
    public static class NotificationSummary {

        public int id;
        public String titulo;
        public String contenido;
        public java.time.LocalDateTime fechaCreacion;
        public String prioridad;
        public String icono;
        public String color;
        public String estadoLectura;

        public boolean isUnread() {
            return "NO_LEIDA".equals(estadoLectura);
        }

        public String getShortContent(int maxLength) {
            if (contenido == null) {
                return "";
            }
            if (contenido.length() <= maxLength) {
                return contenido;
            }
            return contenido.substring(0, maxLength - 3) + "...";
        }
    }

    /**
     * NUEVO: Método mejorado para la campanita con mejor manejo de errores
     */
    public void integrateWithMenuBarSafe(JMenuBar menuBar) {
        checkInitialized();

        if (menuBar == null) {
            throw new IllegalArgumentException("MenuBar no puede ser null");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Verificar si ya hay componentes de notificación
                removeExistingNotificationComponents(menuBar);

                // Crear campanita de notificaciones
                bellComponent = new NotificationBellComponent(currentUserId);

                // Crear panel contenedor para los componentes de notificación
                JPanel notificationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                notificationPanel.setOpaque(false);

                // Agregar campanita
                notificationPanel.add(bellComponent);

                // Si el usuario puede enviar notificaciones, agregar botón de envío
                if (canSendNotifications()) {
                    JButton sendButton = createSendNotificationButton();
                    notificationPanel.add(sendButton);
                }

                // Agregar al extremo derecho de la barra de menú
                menuBar.add(javax.swing.Box.createHorizontalGlue());
                menuBar.add(notificationPanel);

                // Refrescar la barra de menú
                menuBar.revalidate();
                menuBar.repaint();

                System.out.println("✅ Campanita de notificaciones integrada en MenuBar de forma segura");

            } catch (Exception e) {
                System.err.println("❌ Error integrando campanita de notificaciones: " + e.getMessage());
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "Error al cargar el sistema de notificaciones.\nAlgunas funciones pueden no estar disponibles.",
                        "Error de Sistema",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * NUEVO: Remueve componentes de notificación existentes
     */
    private void removeExistingNotificationComponents(JMenuBar menuBar) {
        // Buscar y remover componentes de notificación existentes
        for (int i = menuBar.getComponentCount() - 1; i >= 0; i--) {
            java.awt.Component component = menuBar.getComponent(i);
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                // Verificar si es un panel de notificaciones
                for (java.awt.Component child : panel.getComponents()) {
                    if (child instanceof NotificationBellComponent) {
                        menuBar.remove(component);
                        break;
                    }
                }
            } else if (component instanceof javax.swing.Box.Filler) {
                // Remover glue existente
                menuBar.remove(component);
            }
        }
    }

    /**
     * ✅ NUEVO: Verifica si el usuario actual es desarrollador de tickets
     */
    private boolean isTicketDeveloper() {
        try {
            initializeTicketServiceIfNeeded(); // Inicialización lazy
            return ticketService != null && ticketService.esDeveloper(currentUserId);
        } catch (Exception e) {
            System.err.println("Error verificando desarrollador de tickets: " + e.getMessage());
            return false;
        }
    }

}
