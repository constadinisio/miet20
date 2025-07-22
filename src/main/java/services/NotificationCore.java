package main.java.services;

import java.awt.FlowLayout;
import java.sql.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.models.NotificationModels.*;
import main.java.views.notifications.NotificationUI.NotificationBellComponent;

/**
 * =========================================================================
 * NÚCLEO CONSOLIDADO DEL SISTEMA DE NOTIFICACIONES
 * =========================================================================
 * 
 * Este archivo consolida todos los servicios, managers y helpers del sistema
 * de notificaciones para reducir la cantidad de archivos y centralizar la lógica.
 * 
 * CONSOLIDACIÓN DE 5 ARCHIVOS:
 * - NotificationService.java
 * - NotificationManager.java  
 * - NotificationIntegrationUtil.java
 * - AcademicNotificationHelper.java
 * - TicketNotificationService.java
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 3.0 - Consolidado
 * @date 21/07/2025
 */
public class NotificationCore {

    // =========================================================================
    // 1. NOTIFICATION_SERVICE - SERVICIO PRINCIPAL
    // =========================================================================
    
    /**
     * Servicio principal para la gestión de notificaciones del sistema escolar
     * CON SOPORTE COMPLETO PARA GRUPOS PERSONALIZADOS
     */
    public static class NotificationService {

        private static NotificationService instance;
        private Connection connection;
        private ExecutorService executorService;
        private List<NotificationListener> listeners;

        // Constructor privado para Singleton
        private NotificationService() {
            this.connection = Conexion.getInstancia().verificarConexion();
            this.executorService = Executors.newFixedThreadPool(5);
            this.listeners = new ArrayList<>();

            System.out.println("✅ NotificationService v3.0 inicializado con soporte para grupos personalizados");
        }

        public static synchronized NotificationService getInstance() {
            if (instance == null) {
                instance = new NotificationService();
            }
            return instance;
        }

        // ========================================
        // MÉTODOS PRINCIPALES DE NOTIFICACIÓN
        // ========================================

        /**
         * Envía una notificación individual
         */
        public CompletableFuture<Boolean> enviarNotificacionIndividual(String titulo, String contenido,
                int remitenteId, int destinatarioId, String prioridad) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String insertNotification = """
                        INSERT INTO notificaciones (titulo, contenido, tipo_notificacion, remitente_id, 
                                                   fecha_creacion, prioridad, estado, icono, color) 
                        VALUES (?, ?, 'INDIVIDUAL', ?, NOW(), ?, 'ACTIVA', ?, ?)
                    """;

                    String color = getPriorityColor(prioridad);
                    String icono = getPriorityIcon(prioridad);

                    try (PreparedStatement ps = connection.prepareStatement(insertNotification, 
                            Statement.RETURN_GENERATED_KEYS)) {
                        
                        ps.setString(1, titulo);
                        ps.setString(2, contenido);
                        ps.setInt(3, remitenteId);
                        ps.setString(4, prioridad);
                        ps.setString(5, icono);
                        ps.setString(6, color);

                        int result = ps.executeUpdate();
                        if (result > 0) {
                            ResultSet rs = ps.getGeneratedKeys();
                            if (rs.next()) {
                                int notificationId = rs.getInt(1);
                                
                                // Crear registro de destinatario
                                String insertRecipient = """
                                    INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, 
                                                                             estado_lectura) 
                                    VALUES (?, ?, 'NO_LEIDA')
                                """;
                                
                                try (PreparedStatement psRecipient = connection.prepareStatement(insertRecipient)) {
                                    psRecipient.setInt(1, notificationId);
                                    psRecipient.setInt(2, destinatarioId);
                                    psRecipient.executeUpdate();
                                }
                                
                                // Notificar a listeners
                                notifyListeners(destinatarioId);
                                
                                System.out.println("✅ Notificación individual enviada: " + titulo);
                                return true;
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error enviando notificación individual: " + e.getMessage());
                }
                return false;
            });
        }

        /**
         * Envía notificación a un rol completo
         */
        public CompletableFuture<Boolean> enviarNotificacionARol(String titulo, String contenido,
                int remitenteId, int rolDestino, String prioridad) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Obtener usuarios del rol
                    List<Integer> usuariosRol = obtenerUsuariosPorRol(rolDestino);
                    if (usuariosRol.isEmpty()) {
                        System.out.println("No se encontraron usuarios para el rol: " + rolDestino);
                        return false;
                    }

                    // Crear notificación principal
                    String insertNotification = """
                        INSERT INTO notificaciones (titulo, contenido, tipo_notificacion, remitente_id, 
                                                   rol_destino, fecha_creacion, prioridad, estado, icono, color) 
                        VALUES (?, ?, 'ROL', ?, ?, NOW(), ?, 'ACTIVA', ?, ?)
                    """;

                    String color = getPriorityColor(prioridad);
                    String icono = getPriorityIcon(prioridad);

                    try (PreparedStatement ps = connection.prepareStatement(insertNotification, 
                            Statement.RETURN_GENERATED_KEYS)) {
                        
                        ps.setString(1, titulo);
                        ps.setString(2, contenido);
                        ps.setInt(3, remitenteId);
                        ps.setInt(4, rolDestino);
                        ps.setString(5, prioridad);
                        ps.setString(6, icono);
                        ps.setString(7, color);

                        int result = ps.executeUpdate();
                        if (result > 0) {
                            ResultSet rs = ps.getGeneratedKeys();
                            if (rs.next()) {
                                int notificationId = rs.getInt(1);
                                
                                // Crear registros para todos los destinatarios
                                String insertRecipients = """
                                    INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, 
                                                                             estado_lectura) 
                                    VALUES (?, ?, 'NO_LEIDA')
                                """;
                                
                                try (PreparedStatement psRecipients = connection.prepareStatement(insertRecipients)) {
                                    for (int usuarioId : usuariosRol) {
                                        psRecipients.setInt(1, notificationId);
                                        psRecipients.setInt(2, usuarioId);
                                        psRecipients.addBatch();
                                    }
                                    psRecipients.executeBatch();
                                }
                                
                                // Notificar a todos los listeners
                                usuariosRol.forEach(this::notifyListeners);
                                
                                System.out.println("✅ Notificación a rol enviada: " + titulo + " (" + usuariosRol.size() + " usuarios)");
                                return true;
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error enviando notificación a rol: " + e.getMessage());
                }
                return false;
            });
        }

        /**
         * Obtiene notificaciones no leídas de un usuario
         */
        public List<NotificacionDestinatario> obtenerNotificacionesNoLeidas(int usuarioId) {
            List<NotificacionDestinatario> notificaciones = new ArrayList<>();
            
            String query = """
                SELECT nd.id, nd.notificacion_id, nd.destinatario_id, nd.estado_lectura,
                       nd.fecha_leida,
                       n.titulo, n.contenido, n.remitente_id, n.fecha_creacion, n.fecha_expiracion,
                       n.prioridad, n.icono, n.color, n.tipo_notificacion, n.estado,
                       n.requiere_confirmacion,
                       u.nombre as remitente_nombre, u.apellido as remitente_apellido
                FROM notificaciones_destinatarios nd
                JOIN notificaciones n ON nd.notificacion_id = n.id
                LEFT JOIN usuarios u ON n.remitente_id = u.id
                WHERE nd.destinatario_id = ? 
                  AND nd.estado_lectura = 'NO_LEIDA'
                  AND n.estado = 'ACTIVA'
                  AND (n.fecha_expiracion IS NULL OR n.fecha_expiracion > NOW())
                ORDER BY n.fecha_creacion DESC
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    NotificacionDestinatario notif = new NotificacionDestinatario();
                    
                    // Campos principales
                    notif.setId(rs.getInt("id"));
                    notif.setNotificacionId(rs.getInt("notificacion_id"));
                    notif.setDestinatarioId(rs.getInt("destinatario_id"));
                    notif.setRemitenteId(rs.getInt("remitente_id"));
                    
                    // Datos de la notificación
                    notif.setTitulo(rs.getString("titulo"));
                    notif.setContenido(rs.getString("contenido"));
                    notif.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                    
                    Timestamp expiracion = rs.getTimestamp("fecha_expiracion");
                    if (expiracion != null) {
                        notif.setFechaExpiracion(expiracion.toLocalDateTime());
                    }
                    
                    notif.setPrioridad(rs.getString("prioridad"));
                    notif.setIcono(rs.getString("icono"));
                    notif.setColor(rs.getString("color"));
                    notif.setTipoNotificacion(rs.getString("tipo_notificacion"));
                    notif.setEstado(rs.getString("estado"));
                    notif.setRequiereConfirmacion(rs.getBoolean("requiere_confirmacion"));
                    
                    // Datos del remitente
                    notif.setRemitenteNombre(rs.getString("remitente_nombre"));
                    notif.setRemitenteApellido(rs.getString("remitente_apellido"));
                    
                    // Estado de lectura
                    notif.setEstadoLectura(rs.getString("estado_lectura"));
                    
                    Timestamp fechaLeida = rs.getTimestamp("fecha_leida");
                    if (fechaLeida != null) {
                        notif.setFechaLeida(fechaLeida.toLocalDateTime());
                    }
                    
                    notificaciones.add(notif);
                }
                
            } catch (SQLException e) {
                System.err.println("Error obteniendo notificaciones no leídas: " + e.getMessage());
            }
            
            return notificaciones;
        }

        /**
         * Obtiene todas las notificaciones de un usuario (leídas y no leídas)
         */
        public List<NotificacionDestinatario> obtenerTodasLasNotificaciones(int usuarioId) {
            List<NotificacionDestinatario> notificaciones = new ArrayList<>();
            
            String query = """
                SELECT nd.id, nd.notificacion_id, nd.destinatario_id, nd.estado_lectura,
                       nd.fecha_leida,
                       n.titulo, n.contenido, n.remitente_id, n.fecha_creacion, n.fecha_expiracion,
                       n.prioridad, n.icono, n.color, n.tipo_notificacion, n.estado,
                       n.requiere_confirmacion,
                       u.nombre as remitente_nombre, u.apellido as remitente_apellido
                FROM notificaciones_destinatarios nd
                JOIN notificaciones n ON nd.notificacion_id = n.id
                LEFT JOIN usuarios u ON n.remitente_id = u.id
                WHERE nd.destinatario_id = ? 
                  AND n.estado = 'ACTIVA'
                  AND (n.fecha_expiracion IS NULL OR n.fecha_expiracion > NOW())
                ORDER BY n.fecha_creacion DESC
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    NotificacionDestinatario notif = new NotificacionDestinatario();
                    
                    // Campos principales
                    notif.setId(rs.getInt("id"));
                    notif.setNotificacionId(rs.getInt("notificacion_id"));
                    notif.setDestinatarioId(rs.getInt("destinatario_id"));
                    notif.setRemitenteId(rs.getInt("remitente_id"));
                    
                    // Datos de la notificación
                    notif.setTitulo(rs.getString("titulo"));
                    notif.setContenido(rs.getString("contenido"));
                    notif.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                    
                    Timestamp expiracion = rs.getTimestamp("fecha_expiracion");
                    if (expiracion != null) {
                        notif.setFechaExpiracion(expiracion.toLocalDateTime());
                    }
                    
                    notif.setPrioridad(rs.getString("prioridad"));
                    notif.setIcono(rs.getString("icono"));
                    notif.setColor(rs.getString("color"));
                    notif.setTipoNotificacion(rs.getString("tipo_notificacion"));
                    notif.setEstado(rs.getString("estado"));
                    notif.setRequiereConfirmacion(rs.getBoolean("requiere_confirmacion"));
                    
                    // Datos del remitente
                    notif.setRemitenteNombre(rs.getString("remitente_nombre"));
                    notif.setRemitenteApellido(rs.getString("remitente_apellido"));
                    
                    // Estado de lectura
                    notif.setEstadoLectura(rs.getString("estado_lectura"));
                    
                    Timestamp fechaLeida = rs.getTimestamp("fecha_leida");
                    if (fechaLeida != null) {
                        notif.setFechaLeida(fechaLeida.toLocalDateTime());
                    }
                    
                    notificaciones.add(notif);
                }
                
            } catch (SQLException e) {
                System.err.println("Error obteniendo todas las notificaciones: " + e.getMessage());
            }
            
            return notificaciones;
        }

        /**
         * Marca una notificación como leída
         */
        public CompletableFuture<Boolean> marcarComoLeida(int notificacionId, int usuarioId) {
            return CompletableFuture.supplyAsync(() -> {
                String update = """
                    UPDATE notificaciones_destinatarios 
                    SET estado_lectura = 'LEIDA', fecha_leida = NOW() 
                    WHERE notificacion_id = ? AND destinatario_id = ?
                """;
                
                try (PreparedStatement ps = connection.prepareStatement(update)) {
                    ps.setInt(1, notificacionId);
                    ps.setInt(2, usuarioId);
                    
                    int result = ps.executeUpdate();
                    if (result > 0) {
                        notifyListeners(usuarioId);
                        System.out.println("✅ Notificación marcada como leída: " + notificacionId);
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("Error marcando como leída: " + e.getMessage());
                }
                return false;
            });
        }

        /**
         * Marca una notificación como no leída
         */
        public CompletableFuture<Boolean> marcarComoNoLeida(int notificacionId, int usuarioId) {
            return CompletableFuture.supplyAsync(() -> {
                String update = """
                    UPDATE notificaciones_destinatarios 
                    SET estado_lectura = 'NO_LEIDA', fecha_leida = NULL 
                    WHERE notificacion_id = ? AND destinatario_id = ?
                """;
                
                try (PreparedStatement ps = connection.prepareStatement(update)) {
                    ps.setInt(1, notificacionId);
                    ps.setInt(2, usuarioId);
                    
                    int result = ps.executeUpdate();
                    if (result > 0) {
                        notifyListeners(usuarioId);
                        System.out.println("✅ Notificación marcada como no leída: " + notificacionId);
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("Error marcando como no leída: " + e.getMessage());
                }
                return false;
            });
        }

        /**
         * Cuenta las notificaciones no leídas de un usuario
         */
        public int contarNotificacionesNoLeidas(int usuarioId) {
            String query = """
                SELECT COUNT(*) 
                FROM notificaciones_destinatarios nd
                JOIN notificaciones n ON nd.notificacion_id = n.id
                WHERE nd.destinatario_id = ? 
                  AND nd.estado_lectura = 'NO_LEIDA'
                  AND n.estado = 'ACTIVA'
                  AND (n.fecha_expiracion IS NULL OR n.fecha_expiracion > NOW())
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                System.err.println("Error contando notificaciones no leídas: " + e.getMessage());
            }
            
            return 0;
        }

        // ========================================
        // MÉTODOS AUXILIARES
        // ========================================

        private List<Integer> obtenerUsuariosPorRol(int rolId) {
            List<Integer> usuarios = new ArrayList<>();
            String query = "SELECT id FROM usuarios WHERE rol_id = ? AND activo = 1";
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, rolId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    usuarios.add(rs.getInt("id"));
                }
            } catch (SQLException e) {
                System.err.println("Error obteniendo usuarios por rol: " + e.getMessage());
            }
            
            return usuarios;
        }

        private String getPriorityColor(String prioridad) {
            switch (prioridad.toUpperCase()) {
                case "URGENTE": return "#dc3545";
                case "ALTA": return "#ffc107";
                case "NORMAL": return "#007bff";
                case "BAJA": return "#28a745";
                default: return "#007bff";
            }
        }

        private String getPriorityIcon(String prioridad) {
            switch (prioridad.toUpperCase()) {
                case "URGENTE": return "error";
                case "ALTA": return "warning";
                case "NORMAL": return "info";
                case "BAJA": return "check";
                default: return "info";
            }
        }

        // ========================================
        // SISTEMA DE LISTENERS
        // ========================================

        public interface NotificationListener {
            void onNotificationReceived(int usuarioId);
            void onNotificationRead(int usuarioId);
        }

        public void addNotificationListener(NotificationListener listener) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        public void removeNotificationListener(NotificationListener listener) {
            listeners.remove(listener);
        }

        private void notifyListeners(int usuarioId) {
            SwingUtilities.invokeLater(() -> {
                for (NotificationListener listener : listeners) {
                    try {
                        listener.onNotificationReceived(usuarioId);
                    } catch (Exception e) {
                        System.err.println("Error en listener de notificación: " + e.getMessage());
                    }
                }
            });
        }

        /**
         * Limpieza de recursos
         */
        public void shutdown() {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            listeners.clear();
            System.out.println("✅ NotificationService finalizado correctamente");
        }
    }

    // =========================================================================
    // 2. NOTIFICATION_MANAGER - GESTOR PRINCIPAL
    // =========================================================================
    
    /**
     * Gestor de notificaciones que se integra con MenuBarManager
     * Proporciona métodos para envío rápido y gestión de notificaciones
     */
    public static class NotificationManager {

        private static NotificationManager instance;
        private final NotificationService notificationService;
        private NotificationBellComponent bellComponent;
        private int currentUserId;
        private int currentUserRole;
        private boolean initialized = false;

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
         * Inicializa el manager con datos del usuario
         */
        public void initialize(int userId, int userRole, JMenuBar menuBar) {
            this.currentUserId = userId;
            this.currentUserRole = userRole;

            // Crear componente campanita solo si puede recibir notificaciones
            if (canReceiveNotifications()) {
                // Si ya hay una campanita y se proporciona un nuevo menuBar, recrearla
                if (menuBar != null) {
                    // Limpiar la campanita anterior si existe
                    if (bellComponent != null) {
                        try {
                            // Intentar remover del menuBar anterior
                            bellComponent.cleanup();
                        } catch (Exception e) {
                            System.err.println("Error limpiando campanita anterior: " + e.getMessage());
                        }
                    }
                    
                    // Crear nueva campanita
                    bellComponent = new NotificationBellComponent(userId);
                    
                    // Agregar al nuevo menuBar
                    JPanel notificationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                    notificationPanel.add(bellComponent);
                    menuBar.add(Box.createHorizontalGlue());
                    menuBar.add(notificationPanel);
                    
                    System.out.println("✅ Campanita de notificaciones agregada al menuBar");
                } else if (bellComponent == null) {
                    // Solo crear campanita sin menuBar si no existe
                    bellComponent = new NotificationBellComponent(userId);
                }
            }

            this.initialized = true;
            System.out.println("✅ NotificationManager inicializado para usuario " + userId);
        }

        /**
         * Envía notificación rápida a usuarios específicos
         */
        public void enviarNotificacionRapida(String titulo, String contenido, int... destinatarios) {
            if (!canSendNotifications()) {
                showError("No tienes permisos para enviar notificaciones");
                return;
            }

            for (int destinatario : destinatarios) {
                notificationService.enviarNotificacionIndividual(
                    titulo, contenido, currentUserId, destinatario, "NORMAL"
                ).thenAccept(success -> {
                    if (success) {
                        System.out.println("✅ Notificación enviada a usuario " + destinatario);
                    } else {
                        System.err.println("❌ Error enviando notificación a usuario " + destinatario);
                    }
                });
            }
        }

        /**
         * Compatibilidad: Envía notificación rápida retornando CompletableFuture
         */
        public CompletableFuture<Boolean> enviarNotificacionRapidaAsync(String titulo, String contenido, int... destinatarios) {
            if (!canSendNotifications()) {
                showError("No tienes permisos para enviar notificaciones");
                return CompletableFuture.completedFuture(false);
            }

            if (destinatarios.length == 0) {
                return CompletableFuture.completedFuture(false);
            }

            // Enviar a primer destinatario y retornar el resultado
            return notificationService.enviarNotificacionIndividual(
                titulo, contenido, currentUserId, destinatarios[0], "NORMAL"
            );
        }

        /**
         * Envía notificación a un rol completo
         */
        public void enviarNotificacionARol(String titulo, String contenido, int rolDestino) {
            if (!canSendNotifications()) {
                showError("No tienes permisos para enviar notificaciones");
                return;
            }

            notificationService.enviarNotificacionARol(
                titulo, contenido, currentUserId, rolDestino, "NORMAL"
            ).thenAccept(success -> {
                if (success) {
                    System.out.println("✅ Notificación enviada a rol " + rolDestino);
                } else {
                    System.err.println("❌ Error enviando notificación a rol " + rolDestino);
                }
            });
        }

        /**
         * Envía notificación urgente
         */
        public void enviarNotificacionUrgente(String titulo, String contenido, int... destinatarios) {
            if (!canSendNotifications()) {
                showError("No tienes permisos para enviar notificaciones urgentes");
                return;
            }

            for (int destinatario : destinatarios) {
                notificationService.enviarNotificacionIndividual(
                    titulo, contenido, currentUserId, destinatario, "URGENTE"
                ).thenAccept(success -> {
                    if (success) {
                        System.out.println("🚨 Notificación URGENTE enviada a usuario " + destinatario);
                    }
                });
            }
        }

        /**
         * Abre la ventana de envío de notificaciones
         */
        public void abrirVentanaEnvio() {
            if (!canSendNotifications()) {
                showError("No tienes permisos para enviar notificaciones");
                return;
            }

            // Por ahora mostrar un diálogo simple hasta que se consolide NotificationUI
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "Función de envío rápido disponible.\n" +
                    "La ventana completa estará disponible tras la consolidación de UI.",
                    "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
            });
        }

        /**
         * Obtiene estadísticas del sistema (solo admin)
         */
        public String getNotificationStats() {
            if (!canManageNotifications()) {
                return "No tienes permisos para ver estadísticas de notificaciones.";
            }

            try {
                StringBuilder stats = new StringBuilder();
                stats.append("=== ESTADÍSTICAS DEL SISTEMA DE NOTIFICACIONES ===\n\n");

                // Estadísticas básicas usando el servicio
                int unreadCount = notificationService.contarNotificacionesNoLeidas(currentUserId);
                
                stats.append("📊 Resumen General:\n");
                stats.append("  • Tu cuenta - Notificaciones no leídas: ").append(unreadCount).append("\n");
                stats.append("  • Rol actual: ").append(getCurrentUserRole()).append("\n");
                
                return stats.toString();
                
            } catch (Exception e) {
                System.err.println("Error obteniendo estadísticas: " + e.getMessage());
                return "Error obteniendo estadísticas del sistema.";
            }
        }

        // ========================================
        // MÉTODOS DE VERIFICACIÓN DE PERMISOS
        // ========================================

        public boolean canSendNotifications() {
            return initialized && SENDER_ROLES.contains(currentUserRole);
        }

        public boolean canManageNotifications() {
            return initialized && currentUserRole == ADMIN_ROLE;
        }

        public boolean canReceiveNotifications() {
            return initialized && currentUserId > 0;
        }

        public boolean isInitialized() {
            return initialized;
        }

        public int getUnreadCount() {
            if (!canReceiveNotifications()) {
                return 0;
            }
            return notificationService.contarNotificacionesNoLeidas(currentUserId);
        }

        private String getCurrentUserRolePrivate() {
            switch (currentUserRole) {
                case 1: return "Administrador";
                case 2: return "Preceptor";
                case 3: return "Profesor";
                case 4: return "Alumno";
                case 5: return "ATTP";
                default: return "Desconocido";
            }
        }

        private void showError(String message) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            });
        }

        /**
         * Obtiene el componente campanita para uso externo
         */
        public NotificationBellComponent getBellComponent() {
            return bellComponent;
        }

        // ========================================
        // MÉTODOS DE COMPATIBILIDAD CON API ANTERIOR
        // ========================================

        /**
         * Compatibilidad: Inicializar sin MenuBar
         */
        public void initialize(int userId, int userRole) {
            initialize(userId, userRole, null);
        }

        /**
         * Compatibilidad: Actualizar usuario
         */
        public void updateUser(int userId, int userRole) {
            this.currentUserId = userId;
            this.currentUserRole = userRole;
            
            // Actualizar campanita si existe
            if (bellComponent != null) {
                bellComponent.cleanup();
                bellComponent = null;
            }
            
            // Crear nueva campanita
            if (canReceiveNotifications()) {
                bellComponent = new NotificationBellComponent(userId);
            }
            
            System.out.println("✅ Usuario actualizado en NotificationManager: " + userId);
        }

        /**
         * Compatibilidad: Obtener ID de usuario actual
         */
        public int getCurrentUserId() {
            return currentUserId;
        }

        /**
         * Compatibilidad: Obtener rol de usuario actual (público)
         */
        public int getCurrentUserRole() {
            return currentUserRole;
        }

        /**
         * Compatibilidad: Obtener nombre del rol actual
         */
        public String getCurrentUserRoleName() {
            return getCurrentUserRolePrivate();
        }

        /**
         * Compatibilidad: Obtener servicio de notificaciones
         */
        public NotificationService getNotificationService() {
            return notificationService;
        }

        /**
         * Limpieza de recursos
         */
        public void shutdown() {
            if (bellComponent != null) {
                // Limpiar recursos del componente
                bellComponent = null;
            }
            initialized = false;
            System.out.println("✅ NotificationManager finalizado correctamente");
        }
    }

    // =========================================================================
    // 3. ACADEMIC_NOTIFICATION_HELPER - NOTIFICACIONES ACADÉMICAS
    // =========================================================================
    
    /**
     * Helper class para notificaciones académicas específicas del sistema escolar
     */
    public static class AcademicNotificationHelper {

        private static AcademicNotificationHelper instance;
        private final Connection connection;

        private AcademicNotificationHelper() {
            this.connection = Conexion.getInstancia().verificarConexion();
        }

        public static synchronized AcademicNotificationHelper getInstance() {
            if (instance == null) {
                instance = new AcademicNotificationHelper();
            }
            return instance;
        }

        /**
         * Notifica cuando se publica una nueva nota
         */
        public CompletableFuture<Boolean> notificarNuevaNota(int alumnoId, String materia,
                String tipoTrabajo, double nota, int profesorId) {
            
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String titulo = "📝 Nueva calificación publicada";
                    String contenido = String.format(
                        "Se ha publicado una nueva calificación en %s.\n\n" +
                        "Trabajo: %s\n" +
                        "Calificación: %.2f\n\n" +
                        "Puedes revisar los detalles en tu sección académica.",
                        materia, tipoTrabajo, nota
                    );

                    NotificationService.getInstance().enviarNotificacionIndividual(
                        titulo, contenido, profesorId, alumnoId, "NORMAL"
                    );

                    // También notificar a los padres si el alumno los tiene
                    notificarPadresNuevaNota(alumnoId, materia, tipoTrabajo, nota);

                    System.out.println("✅ Notificación de nueva nota enviada a alumno " + alumnoId);
                    return true;

                } catch (Exception e) {
                    System.err.println("Error enviando notificación de nueva nota: " + e.getMessage());
                    return false;
                }
            });
        }

        /**
         * Notifica sobre notas bimestrales publicadas
         */
        public CompletableFuture<Boolean> notificarNotaBimestral(int alumnoId, String materia,
                String periodo, double nota, int profesorId) {
            
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String titulo = "📊 Calificación bimestral disponible";
                    String contenido = String.format(
                        "Ya está disponible tu calificación bimestral de %s.\n\n" +
                        "Período: %s\n" +
                        "Calificación: %.2f\n\n" +
                        "Revisa tu boletín completo en la sección académica.",
                        materia, periodo, nota
                    );

                    NotificationService.getInstance().enviarNotificacionIndividual(
                        titulo, contenido, profesorId, alumnoId, "ALTA"
                    );

                    System.out.println("✅ Notificación bimestral enviada a alumno " + alumnoId);
                    return true;

                } catch (Exception e) {
                    System.err.println("Error enviando notificación bimestral: " + e.getMessage());
                    return false;
                }
            });
        }

        /**
         * Notifica sobre aprobación de usuario
         */
        public void notificarUsuarioAprobado(int usuarioId, String nombreCompleto) {
            try {
                String titulo = "✅ Cuenta aprobada - Bienvenido al sistema";
                String contenido = String.format(
                    "¡Hola %s!\n\n" +
                    "Tu cuenta ha sido aprobada exitosamente. " +
                    "Ya puedes acceder a todas las funcionalidades del sistema.\n\n" +
                    "¡Bienvenido/a!",
                    nombreCompleto
                );

                NotificationService.getInstance().enviarNotificacionIndividual(
                    titulo, contenido, 1, usuarioId, "ALTA" // Admin ID = 1
                );

                System.out.println("✅ Notificación de aprobación enviada a usuario " + usuarioId);

            } catch (Exception e) {
                System.err.println("Error enviando notificación de aprobación: " + e.getMessage());
            }
        }

        /**
         * Notifica sobre nuevo registro pendiente (a administradores)
         */
        public void notificarNuevoRegistroPendiente(String nombreCompleto, String rol) {
            try {
                String titulo = "👤 Nuevo registro pendiente de aprobación";
                String contenido = String.format(
                    "Se ha registrado un nuevo usuario que requiere aprobación:\n\n" +
                    "Usuario: %s\n" +
                    "Rol solicitado: %s\n\n" +
                    "Por favor, revisa y aprueba el registro en la sección de gestión de usuarios.",
                    nombreCompleto, rol
                );

                // Enviar a todos los administradores (rol 1)
                NotificationService.getInstance().enviarNotificacionARol(
                    titulo, contenido, 1, 1, "ALTA" // De admin (1) a admins (1)
                );

                System.out.println("✅ Notificación de nuevo registro enviada a administradores");

            } catch (Exception e) {
                System.err.println("Error enviando notificación de nuevo registro: " + e.getMessage());
            }
        }

        // ========================================
        // MÉTODOS AUXILIARES
        // ========================================

        private void notificarPadresNuevaNota(int alumnoId, String materia, String tipoTrabajo, double nota) {
            // Implementar lógica para obtener padres del alumno y notificarles
            // Esto dependería de cómo esté estructurada la relación alumno-padres en la BD
            try {
                String queryPadres = """
                    SELECT DISTINCT p.id 
                    FROM usuarios p 
                    JOIN relaciones_familiares rf ON p.id = rf.padre_id 
                    WHERE rf.alumno_id = ? AND p.activo = 1
                """;
                
                try (PreparedStatement ps = connection.prepareStatement(queryPadres)) {
                    ps.setInt(1, alumnoId);
                    ResultSet rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        int padreId = rs.getInt("id");
                        
                        String titulo = "📝 Nueva calificación de su hijo/a";
                        String contenido = String.format(
                            "Su hijo/a ha recibido una nueva calificación en %s.\n\n" +
                            "Trabajo: %s\n" +
                            "Calificación: %.2f\n\n" +
                            "Puede consultar más detalles en el sistema.",
                            materia, tipoTrabajo, nota
                        );

                        NotificationService.getInstance().enviarNotificacionIndividual(
                            titulo, contenido, 1, padreId, "NORMAL"
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error notificando a padres: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. NOTIFICATION_INTEGRATION_UTIL - UTILIDADES DE INTEGRACIÓN
    // =========================================================================
    
    /**
     * Utilidad para integrar fácilmente las notificaciones en los Panel Managers
     */
    public static class NotificationIntegrationUtil {
        
        private static NotificationIntegrationUtil instance;
        private final NotificationManager notificationManager;
        private final AcademicNotificationHelper academicHelper;
        
        private NotificationIntegrationUtil() {
            this.notificationManager = NotificationManager.getInstance();
            this.academicHelper = AcademicNotificationHelper.getInstance();
        }
        
        public static synchronized NotificationIntegrationUtil getInstance() {
            if (instance == null) {
                instance = new NotificationIntegrationUtil();
            }
            return instance;
        }
        
        // ===============================================
        // MÉTODOS GENERALES PARA TODOS LOS ROLES
        // ===============================================
        
        /**
         * Envía notificación básica a usuario específico
         */
        public void enviarNotificacionBasica(String titulo, String contenido, int destinatarioId) {
            if (isSystemReady()) {
                notificationManager.enviarNotificacionRapida(titulo, contenido, destinatarioId);
            }
        }
        
        /**
         * Envía notificación urgente
         */
        public void enviarNotificacionUrgente(String titulo, String contenido, int... destinatarios) {
            if (isSystemReady() && notificationManager.canSendNotifications()) {
                notificationManager.enviarNotificacionUrgente(titulo, contenido, destinatarios);
            }
        }
        
        /**
         * Envía notificación a un rol completo
         */
        public void enviarNotificacionARol(String titulo, String contenido, int rolDestino) {
            if (isSystemReady() && notificationManager.canSendNotifications()) {
                notificationManager.enviarNotificacionARol(titulo, contenido, rolDestino);
            }
        }

        // ===============================================
        // MÉTODOS PARA ADMINISTRADORES
        // ===============================================
        
        /**
         * Notifica sobre aprobación de usuario (Admin)
         */
        public void notificarUsuarioAprobado(int usuarioId, String nombreCompleto) {
            if (isSystemReady()) {
                academicHelper.notificarUsuarioAprobado(usuarioId, nombreCompleto);
            }
        }
        
        /**
         * Notifica sobre nuevo registro pendiente (Admin)
         */
        public void notificarNuevoRegistroPendiente(String nombreCompleto, String rol) {
            if (isSystemReady()) {
                academicHelper.notificarNuevoRegistroPendiente(nombreCompleto, rol);
            }
        }

        /**
         * Compatibilidad: Notifica sobre nuevo registro pendiente con email
         */
        public void notificarNuevoRegistroPendiente(String nombreCompleto, String email, String rol) {
            if (isSystemReady()) {
                academicHelper.notificarNuevoRegistroPendiente(nombreCompleto, rol);
            }
        }
        
        /**
         * Notifica sobre mantenimiento del sistema
         */
        public void notificarMantenimiento(String fecha, String hora, String duracion) {
            if (isSystemReady() && notificationManager.canManageNotifications()) {
                String titulo = "🔧 Mantenimiento Programado";
                String contenido = String.format(
                    "El sistema estará en mantenimiento el %s a las %s. Duración: %s", 
                    fecha, hora, duracion
                );
                // Enviar a todos los roles
                for (int rol = 1; rol <= 5; rol++) {
                    enviarNotificacionARol(titulo, contenido, rol);
                }
            }
        }

        // ===============================================
        // MÉTODOS PARA PROFESORES
        // ===============================================
        
        /**
         * Notifica nueva calificación publicada
         */
        public void notificarNuevaNota(int alumnoId, String materia, String tipoTrabajo, double nota, int profesorId) {
            if (isSystemReady()) {
                academicHelper.notificarNuevaNota(alumnoId, materia, tipoTrabajo, nota, profesorId);
            }
        }
        
        /**
         * Notifica calificación bimestral
         */
        public void notificarNotaBimestral(int alumnoId, String materia, String periodo, double nota, int profesorId) {
            if (isSystemReady()) {
                academicHelper.notificarNotaBimestral(alumnoId, materia, periodo, nota, profesorId);
            }
        }

        // ===============================================
        // MÉTODOS DE ESTADO Y UTILIDAD
        // ===============================================
        
        /**
         * Verifica si el sistema está listo para enviar notificaciones
         */
        private boolean isSystemReady() {
            return notificationManager != null && 
                   notificationManager.isInitialized() && 
                   academicHelper != null;
        }
        
        /**
         * Obtiene el número de notificaciones no leídas del usuario actual
         */
        public int getNotificacionesNoLeidas() {
            if (isSystemReady()) {
                return notificationManager.getUnreadCount();
            }
            return 0;
        }
        
        /**
         * Verifica si el usuario puede enviar notificaciones
         */
        public boolean puedeEnviarNotificaciones() {
            return isSystemReady() && notificationManager.canSendNotifications();
        }
        
        /**
         * Verifica si el usuario puede gestionar notificaciones
         */
        public boolean puedeGestionarNotificaciones() {
            return isSystemReady() && notificationManager.canManageNotifications();
        }

        // ===============================================
        // MÉTODOS DE COMPATIBILIDAD CON API ANTERIOR
        // ===============================================

        /**
         * Compatibilidad: Actualizar notificaciones (método genérico)
         */
        public void actualizarNotificaciones() {
            // Método stub para compatibilidad - no hace nada específico
            System.out.println("📡 Actualizando notificaciones del sistema...");
        }

        /**
         * Compatibilidad: Notificar problema de asistencia
         */
        public void notificarProblemaAsistencia(int alumnoId, int cantidadFaltas, String periodo) {
            if (isSystemReady()) {
                String titulo = "⚠️ Problema de asistencia detectado";
                String contenido = String.format(
                    "Se ha detectado un problema de asistencia.\n\n" +
                    "Cantidad de faltas: %d\n" +
                    "Período: %s\n\n" +
                    "Por favor, revisa la situación del estudiante.",
                    cantidadFaltas, periodo
                );
                enviarNotificacionBasica(titulo, contenido, alumnoId);
            }
        }

        /**
         * Compatibilidad: Notificar boletines masivos
         */
        public void notificarBoletinesMasivos(int preceptorId, String curso, int cantidad, String periodo) {
            if (isSystemReady()) {
                String titulo = "📊 Boletines generados masivamente";
                String contenido = String.format(
                    "Se han generado %d boletín(es) para el curso %s en el período %s",
                    cantidad, curso, periodo
                );
                enviarNotificacionBasica(titulo, contenido, preceptorId);
            }
        }

        /**
         * Compatibilidad: Notificar boletín generado
         */
        public void notificarBoletinGenerado(Integer alumnoId, String periodo, String fecha) {
            if (isSystemReady() && alumnoId != null) {
                String titulo = "📋 Boletín generado";
                String contenido = String.format(
                    "Tu boletín del período %s ha sido generado el %s",
                    periodo, fecha
                );
                enviarNotificacionBasica(titulo, contenido, alumnoId);
            }
        }

        /**
         * Compatibilidad: Notificar cambio de curso
         */
        public void notificarCambioCurso(int alumnoId, String cursoAnterior, String cursoNuevo, String motivo) {
            if (isSystemReady()) {
                String titulo = "🔄 Cambio de curso";
                String contenido = String.format(
                    "Has sido trasladado del curso %s al curso %s.\n\nMotivo: %s",
                    cursoAnterior, cursoNuevo, motivo
                );
                enviarNotificacionBasica(titulo, contenido, alumnoId);
            }
        }

        /**
         * Compatibilidad: Obtener estadísticas de notificaciones
         */
        public String getEstadisticasNotificaciones() {
            if (isSystemReady()) {
                return notificationManager.getNotificationStats();
            }
            return "Estadísticas no disponibles";
        }

        /**
         * Compatibilidad: Notificar nuevo préstamo
         */
        public void notificarNuevoPrestamo(int alumnoId, String equipoNombre, String fechaDevolucion, int attpId) {
            if (isSystemReady()) {
                String titulo = "📦 Nuevo préstamo de equipo";
                String contenido = String.format(
                    "Se te ha asignado el equipo: %s\n\nFecha de devolución: %s\n\n" +
                    "Por favor, cuida el equipo y devuélvelo en la fecha indicada.",
                    equipoNombre, fechaDevolucion
                );
                enviarNotificacionBasica(titulo, contenido, alumnoId);
            }
        }

        /**
         * Compatibilidad: Notificar devolución vencida
         */
        public void notificarDevolucionVencida(int alumnoId, String equipoNombre, String fechaVencimiento, int diasVencidos) {
            if (isSystemReady()) {
                String titulo = "⚠️ Devolución de equipo vencida";
                String contenido = String.format(
                    "El préstamo del equipo %s ha vencido.\n\n" +
                    "Fecha de vencimiento: %s\n" +
                    "Días vencidos: %d\n\n" +
                    "Por favor, devuelve el equipo lo antes posible.",
                    equipoNombre, fechaVencimiento, diasVencidos
                );
                enviarNotificacionUrgente(titulo, contenido, alumnoId);
            }
        }

        /**
         * Compatibilidad: Enviar notificación del sistema
         */
        public void enviarNotificacionSistema(String titulo, String contenido, String tipo) {
            if (isSystemReady() && notificationManager.canManageNotifications()) {
                // Enviar a administradores (rol 1)
                enviarNotificacionARol(titulo, contenido, 1);
            }
        }

        /**
         * Compatibilidad: Enviar notificación de prueba
         */
        public void enviarNotificacionPrueba() {
            if (isSystemReady()) {
                String titulo = "🧪 Notificación de prueba";
                String contenido = "Esta es una notificación de prueba del sistema. " +
                                 "Si recibes este mensaje, el sistema de notificaciones está funcionando correctamente.";
                enviarNotificacionBasica(titulo, contenido, notificationManager.getCurrentUserId());
            }
        }

        /**
         * Compatibilidad: Contexto aprobación usuario
         */
        public void contextoAprobacionUsuario(int usuarioId, String nombre, String apellido, String rol) {
            if (isSystemReady()) {
                String nombreCompleto = nombre + " " + apellido;
                notificarUsuarioAprobado(usuarioId, nombreCompleto);
            }
        }
    }

    // =========================================================================
    // 5. TICKET_NOTIFICATION_SERVICE - NOTIFICACIONES DE TICKETS
    // =========================================================================
    
    /**
     * Servicio especializado para notificaciones relacionadas con tickets de soporte
     */
    public static class TicketNotificationService {
        
        private static TicketNotificationService instance;
        private final NotificationManager notificationManager;
        
        private TicketNotificationService() {
            this.notificationManager = NotificationManager.getInstance();
        }
        
        public static synchronized TicketNotificationService getInstance() {
            if (instance == null) {
                instance = new TicketNotificationService();
            }
            return instance;
        }
        
        /**
         * Notifica cuando se crea un nuevo ticket
         */
        public void notificarNuevoTicket(int ticketId, String titulo, int creadorId, String prioridad) {
            try {
                String tituloNotif = "🎫 Nuevo ticket de soporte creado";
                String contenido = String.format(
                    "Se ha creado un nuevo ticket de soporte.\n\n" +
                    "ID: #%d\n" +
                    "Título: %s\n" +
                    "Prioridad: %s\n\n" +
                    "Por favor, revisa y asigna el ticket correspondiente.",
                    ticketId, titulo, prioridad
                );

                // Notificar a administradores y personal de soporte (ATTP)
                notificationManager.enviarNotificacionARol(tituloNotif, contenido, 1); // Admins
                notificationManager.enviarNotificacionARol(tituloNotif, contenido, 5); // ATTP

                System.out.println("✅ Notificación de nuevo ticket enviada");

            } catch (Exception e) {
                System.err.println("Error enviando notificación de nuevo ticket: " + e.getMessage());
            }
        }
        
        /**
         * Notifica cuando se actualiza un ticket
         */
        public void notificarActualizacionTicket(int ticketId, String titulo, int usuarioAfectadoId, String nuevoEstado) {
            try {
                String tituloNotif = "🔄 Actualización en tu ticket de soporte";
                String contenido = String.format(
                    "Tu ticket de soporte ha sido actualizado.\n\n" +
                    "ID: #%d\n" +
                    "Título: %s\n" +
                    "Nuevo estado: %s\n\n" +
                    "Puedes revisar los detalles en la sección de tickets.",
                    ticketId, titulo, nuevoEstado
                );

                notificationManager.enviarNotificacionRapida(tituloNotif, contenido, usuarioAfectadoId);

                System.out.println("✅ Notificación de actualización de ticket enviada a usuario " + usuarioAfectadoId);

            } catch (Exception e) {
                System.err.println("Error enviando notificación de actualización de ticket: " + e.getMessage());
            }
        }
        
        /**
         * Notifica cuando se cierra un ticket
         */
        public void notificarCierreTicket(int ticketId, String titulo, int usuarioAfectadoId, String solucion) {
            try {
                String tituloNotif = "✅ Tu ticket ha sido resuelto";
                String contenido = String.format(
                    "Tu ticket de soporte ha sido cerrado exitosamente.\n\n" +
                    "ID: #%d\n" +
                    "Título: %s\n" +
                    "Solución: %s\n\n" +
                    "Gracias por usar nuestro sistema de soporte.",
                    ticketId, titulo, solucion
                );

                notificationManager.enviarNotificacionRapida(tituloNotif, contenido, usuarioAfectadoId);

                System.out.println("✅ Notificación de cierre de ticket enviada a usuario " + usuarioAfectadoId);

            } catch (Exception e) {
                System.err.println("Error enviando notificación de cierre de ticket: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // MÉTODOS ESTÁTICOS DE ACCESO RÁPIDO
    // =========================================================================
    
    /**
     * Obtiene la instancia del servicio principal
     */
    public static NotificationService getService() {
        return NotificationService.getInstance();
    }
    
    /**
     * Obtiene la instancia del manager principal
     */
    public static NotificationManager getManager() {
        return NotificationManager.getInstance();
    }
    
    /**
     * Obtiene la instancia de las utilidades de integración
     */
    public static NotificationIntegrationUtil getIntegrationUtil() {
        return NotificationIntegrationUtil.getInstance();
    }
    
    /**
     * Obtiene la instancia del helper académico
     */
    public static AcademicNotificationHelper getAcademicHelper() {
        return AcademicNotificationHelper.getInstance();
    }
    
    /**
     * Obtiene la instancia del servicio de tickets
     */
    public static TicketNotificationService getTicketService() {
        return TicketNotificationService.getInstance();
    }

    /**
     * Inicialización completa del sistema de notificaciones
     */
    public static void initializeSystem(int userId, int userRole, JMenuBar menuBar) {
        NotificationManager.getInstance().initialize(userId, userRole, menuBar);
        System.out.println("🚀 Sistema de notificaciones completamente inicializado");
    }

    /**
     * Limpieza completa del sistema
     */
    public static void shutdownSystem() {
        NotificationManager.getInstance().shutdown();
        NotificationService.getInstance().shutdown();
        System.out.println("🔒 Sistema de notificaciones finalizado");
    }
}
