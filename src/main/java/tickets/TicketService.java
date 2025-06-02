package main.java.tickets;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.utils.NotificationManager;

/**
 * TicketService - Servicio principal del sistema de tickets Reutiliza al máximo
 * el sistema de notificaciones existente
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Centro de Ayuda Integrado
 */
public class TicketService {

    private static TicketService instance;
    private final Connection connection;
    private NotificationManager notificationManager;

    // Tu ID de usuario hardcodeado como fallback
    private static final int DEVELOPER_USER_ID = 960;

    private TicketService() {
        this.connection = Conexion.getInstancia().verificarConexion();
        this.notificationManager = null; // Se inicializará cuando sea necesario

        System.out.println("✅ TicketService inicializado para Centro de Ayuda");
    }

    public static synchronized TicketService getInstance() {
        if (instance == null) {
            instance = new TicketService();
        }
        return instance;
    }

    /**
     * ✅ NUEVO: Inicialización lazy del NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            try {
                notificationManager = NotificationManager.getInstance();
            } catch (Exception e) {
                System.err.println("⚠️ Error obteniendo NotificationManager: " + e.getMessage());
                return null;
            }
        }
        return notificationManager;
    }

    // ========================================
    // MÉTODOS PRINCIPALES
    // ========================================
    /**
     * Crea un nuevo ticket y envía notificación al desarrollador
     */
    public CompletableFuture<String> crearTicket(String asunto, String descripcion,
            String categoria, String prioridad,
            int usuarioReportaId, String archivoUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Generar número único de ticket
                String ticketNumber = generarNumeroTicket();

                // 2. Insertar ticket en base de datos
                String insertQuery = """
                    INSERT INTO tickets (ticket_number, asunto, descripcion, categoria, prioridad, 
                                       usuario_reporta_id, archivo_adjunto_url) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

                PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, ticketNumber);
                ps.setString(2, asunto);
                ps.setString(3, descripcion);
                ps.setString(4, categoria);
                ps.setString(5, prioridad);
                ps.setInt(6, usuarioReportaId);
                ps.setString(7, archivoUrl);

                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();

                int ticketId = -1;
                if (rs.next()) {
                    ticketId = rs.getInt(1);
                }

                // 3. Enviar notificación al desarrollador usando el sistema existente
                if (ticketId > 0) {
                    enviarNotificacionNuevoTicket(ticketId, ticketNumber, asunto, usuarioReportaId);
                    System.out.println("✅ Ticket creado: " + ticketNumber + " por usuario: " + usuarioReportaId);
                }

                return ticketNumber;

            } catch (Exception e) {
                System.err.println("❌ Error creando ticket: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Obtiene tickets de un usuario específico
     */
    public List<Ticket> obtenerTicketsUsuario(int usuarioId) {
        List<Ticket> tickets = new ArrayList<>();

        try {
            String query = """
                SELECT t.*, u.nombre, u.apellido 
                FROM tickets t
                INNER JOIN usuarios u ON t.usuario_reporta_id = u.id
                WHERE t.usuario_reporta_id = ? AND t.activo = 1
                ORDER BY t.fecha_creacion DESC
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tickets.add(mapearTicketDesdeResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo tickets del usuario: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * Obtiene TODOS los tickets (solo para desarrollador)
     */
    public List<Ticket> obtenerTodosLosTickets() {
        List<Ticket> tickets = new ArrayList<>();

        try {
            String query = """
                SELECT t.*, u.nombre, u.apellido,
                       CONCAT(u.apellido, ', ', u.nombre) as nombre_completo
                FROM tickets t
                INNER JOIN usuarios u ON t.usuario_reporta_id = u.id
                WHERE t.activo = 1
                ORDER BY 
                    CASE t.estado 
                        WHEN 'ABIERTO' THEN 1 
                        WHEN 'EN_REVISION' THEN 2 
                        WHEN 'RESUELTO' THEN 3 
                        WHEN 'CERRADO' THEN 4 
                    END,
                    t.fecha_creacion DESC
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ticket ticket = mapearTicketDesdeResultSet(rs);
                ticket.setNombreCompleto(rs.getString("nombre_completo"));
                tickets.add(ticket);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los tickets: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * Actualiza el estado de un ticket y envía notificación al usuario
     */
    public boolean actualizarTicket(int ticketId, String nuevoEstado, String respuestaDeveloper) {
        try {
            // 1. Obtener información del ticket actual
            Ticket ticketActual = obtenerTicketPorId(ticketId);
            if (ticketActual == null) {
                return false;
            }

            // 2. Actualizar ticket
            String updateQuery = """
                UPDATE tickets 
                SET estado = ?, respuesta_desarrollador = ?, fecha_respuesta = NOW(),
                    fecha_actualizacion = NOW()
                WHERE id = ?
                """;

            PreparedStatement ps = connection.prepareStatement(updateQuery);
            ps.setString(1, nuevoEstado);
            ps.setString(2, respuestaDeveloper);
            ps.setInt(3, ticketId);

            int rowsUpdated = ps.executeUpdate();

            // 3. Enviar notificación al usuario que reportó
            if (rowsUpdated > 0) {
                enviarNotificacionActualizacionTicket(ticketActual, nuevoEstado, respuestaDeveloper);
                System.out.println("✅ Ticket actualizado: " + ticketActual.getTicketNumber());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error actualizando ticket: " + e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene un ticket específico por ID
     */
    public Ticket obtenerTicketPorId(int ticketId) {
        try {
            String query = """
                SELECT t.*, u.nombre, u.apellido,
                       CONCAT(u.apellido, ', ', u.nombre) as nombre_completo
                FROM tickets t
                INNER JOIN usuarios u ON t.usuario_reporta_id = u.id
                WHERE t.id = ? AND t.activo = 1
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Ticket ticket = mapearTicketDesdeResultSet(rs);
                ticket.setNombreCompleto(rs.getString("nombre_completo"));
                return ticket;
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo ticket por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Verifica si un usuario es el desarrollador que recibe tickets
     */
    public boolean esDeveloper(int usuarioId) {
        // Primer check: hardcodeado (tu ID)
        if (usuarioId == DEVELOPER_USER_ID) {
            return true;
        }

        // Segundo check: base de datos (escalabilidad)
        try {
            String query = "SELECT COUNT(*) FROM configuracion_soporte WHERE desarrollador_user_id = ? AND activo = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error verificando desarrollador: " + e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene el ID del desarrollador principal
     */
    public int obtenerDeveloperUserId() {
        try {
            String query = "SELECT desarrollador_user_id FROM configuracion_soporte WHERE activo = 1 LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("desarrollador_user_id");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo developer user ID: " + e.getMessage());
        }

        return DEVELOPER_USER_ID; // Fallback
    }

    /**
     * Cuenta tickets pendientes (para mostrar en el menú)
     */
    public int contarTicketsPendientes() {
        try {
            String query = "SELECT COUNT(*) FROM tickets WHERE estado IN ('ABIERTO', 'EN_REVISION') AND activo = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error contando tickets pendientes: " + e.getMessage());
        }

        return 0;
    }

    // ========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================
    /**
     * Genera un número único de ticket
     */
    private synchronized String generarNumeroTicket() {
        try {
            // Obtener configuración
            String query = "SELECT prefijo_ticket, auto_numero FROM configuracion_soporte WHERE activo = 1 LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            String prefijo = "TK";
            int numero = 1;

            if (rs.next()) {
                prefijo = rs.getString("prefijo_ticket");
                numero = rs.getInt("auto_numero");
            }

            // Incrementar contador
            String updateQuery = "UPDATE configuracion_soporte SET auto_numero = auto_numero + 1 WHERE activo = 1";
            PreparedStatement psUpdate = connection.prepareStatement(updateQuery);
            psUpdate.executeUpdate();

            // Generar número con formato TK-001, TK-002, etc.
            return String.format("%s-%03d", prefijo, numero);

        } catch (SQLException e) {
            System.err.println("Error generando número de ticket: " + e.getMessage());
            // Fallback con timestamp
            return "TK-" + System.currentTimeMillis() % 10000;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Ticket
     */
    private Ticket mapearTicketDesdeResultSet(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setTicketNumber(rs.getString("ticket_number"));
        ticket.setAsunto(rs.getString("asunto"));
        ticket.setDescripcion(rs.getString("descripcion"));
        ticket.setCategoria(rs.getString("categoria"));
        ticket.setPrioridad(rs.getString("prioridad"));
        ticket.setEstado(rs.getString("estado"));
        ticket.setUsuarioReportaId(rs.getInt("usuario_reporta_id"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            ticket.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            ticket.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }

        Timestamp fechaRespuesta = rs.getTimestamp("fecha_respuesta");
        if (fechaRespuesta != null) {
            ticket.setFechaRespuesta(fechaRespuesta.toLocalDateTime());
        }

        ticket.setArchivoAdjuntoUrl(rs.getString("archivo_adjunto_url"));
        ticket.setRespuestaDeveloper(rs.getString("respuesta_desarrollador"));

        // Agregar nombres si están disponibles
        try {
            ticket.setNombreUsuario(rs.getString("nombre"));
            ticket.setApellidoUsuario(rs.getString("apellido"));
        } catch (SQLException e) {
            // Campos opcionales, no importa si no están
        }

        return ticket;
    }

    /**
     * Envía notificación de nuevo ticket al desarrollador REUTILIZA el sistema
     * de notificaciones existente
     */
    private void enviarNotificacionNuevoTicket(int ticketId, String ticketNumber,
            String asunto, int usuarioReportaId) {
        try {
            int developerId = obtenerDeveloperUserId();

            String titulo = "🎫 Centro de Ayuda: " + ticketNumber;
            String contenido = String.format(
                    "Nuevo reporte recibido:\n\n"
                    + "📋 Ticket: %s\n"
                    + "👤 Usuario: %s\n"
                    + "📝 Asunto: %s\n\n"
                    + "Revisa el Centro de Ayuda para más detalles.",
                    ticketNumber,
                    obtenerNombreUsuario(usuarioReportaId),
                    asunto
            );

            // ✅ USAR: Obtener NotificationManager de forma segura
            NotificationManager notifManager = getNotificationManager();
            if (notifManager != null) {
                notifManager.enviarNotificacionConDetalles(
                        titulo, contenido, "ALTA", "#ff6b35", "🎫", developerId
                ).thenAccept(exito -> {
                    if (exito) {
                        // También crear registro en notificaciones con relación al ticket
                        try {
                            insertarNotificacionConTicket(ticketId, titulo, contenido, developerId, usuarioReportaId);
                        } catch (Exception e) {
                            System.err.println("Error vinculando notificación con ticket: " + e.getMessage());
                        }
                    }
                });
            } else {
                System.err.println("⚠️ NotificationManager no disponible, creando notificación directamente");
                // Fallback: crear notificación directamente en BD
                try {
                    insertarNotificacionConTicket(ticketId, titulo, contenido, developerId, usuarioReportaId);
                } catch (Exception e) {
                    System.err.println("Error creando notificación fallback: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error enviando notificación de nuevo ticket: " + e.getMessage());
        }
    }

    /**
     * Envía notificación de actualización al usuario que reportó
     */
    private void enviarNotificacionActualizacionTicket(Ticket ticket, String nuevoEstado, String respuesta) {
        try {
            String titulo = "🔄 Actualización de tu reporte: " + ticket.getTicketNumber();
            String contenido = String.format(
                    "Tu reporte ha sido actualizado:\n\n"
                    + "📋 Ticket: %s\n"
                    + "📝 Asunto: %s\n"
                    + "🔄 Estado: %s\n"
                    + "%s"
                    + "\nGracias por usar el Centro de Ayuda.",
                    ticket.getTicketNumber(),
                    ticket.getAsunto(),
                    nuevoEstado,
                    (respuesta != null && !respuesta.trim().isEmpty())
                    ? "\n💬 Respuesta: " + respuesta + "\n" : ""
            );

            // Determinar color según estado
            String color = switch (nuevoEstado) {
                case "RESUELTO" ->
                    "#28a745";
                case "EN_REVISION" ->
                    "#ffc107";
                case "CERRADO" ->
                    "#6c757d";
                default ->
                    "#007bff";
            };

            // ✅ USAR: Obtener NotificationManager de forma segura
            NotificationManager notifManager = getNotificationManager();
            if (notifManager != null) {
                notifManager.enviarNotificacionConDetalles(
                        titulo, contenido, "NORMAL", color, "🔄", ticket.getUsuarioReportaId()
                ).thenAccept(exito -> {
                    if (exito) {
                        try {
                            insertarNotificacionConTicket(ticket.getId(), titulo, contenido,
                                    ticket.getUsuarioReportaId(), obtenerDeveloperUserId());
                        } catch (Exception e) {
                            System.err.println("Error vinculando notificación de actualización: " + e.getMessage());
                        }
                    }
                });
            } else {
                System.err.println("⚠️ NotificationManager no disponible, creando notificación directamente");
                // Fallback: crear notificación directamente en BD
                try {
                    insertarNotificacionConTicket(ticket.getId(), titulo, contenido,
                            ticket.getUsuarioReportaId(), obtenerDeveloperUserId());
                } catch (Exception e) {
                    System.err.println("Error creando notificación fallback: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error enviando notificación de actualización: " + e.getMessage());
        }
    }

    /**
     * Inserta notificación relacionada con ticket
     */
    private void insertarNotificacionConTicket(int ticketId, String titulo, String contenido,
            int destinatarioId, int remitenteId) {
        try {
            String insertQuery = """
                INSERT INTO notificaciones (titulo, contenido, tipo_notificacion, remitente_id, 
                                          prioridad, icono, color, ticket_id, tipo_especial) 
                VALUES (?, ?, 'INDIVIDUAL', ?, 'NORMAL', '🎫', '#ff6b35', ?, 'TICKET')
                """;

            PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, titulo);
            ps.setString(2, contenido);
            ps.setInt(3, remitenteId);
            ps.setInt(4, ticketId);

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                int notificacionId = rs.getInt(1);

                // Insertar destinatario
                String insertDestQuery = """
                    INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) 
                    VALUES (?, ?, 'NO_LEIDA')
                    """;

                PreparedStatement psDestino = connection.prepareStatement(insertDestQuery);
                psDestino.setInt(1, notificacionId);
                psDestino.setInt(2, destinatarioId);
                psDestino.executeUpdate();

                System.out.println("✅ Notificación de ticket vinculada: " + notificacionId);
            }

        } catch (SQLException e) {
            System.err.println("Error insertando notificación de ticket: " + e.getMessage());
        }
    }

    /**
     * Obtiene el nombre completo de un usuario
     */
    private String obtenerNombreUsuario(int usuarioId) {
        try {
            String query = "SELECT CONCAT(apellido, ', ', nombre) as nombre_completo FROM usuarios WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre de usuario: " + e.getMessage());
        }

        return "Usuario #" + usuarioId;
    }

    /**
     * Obtiene estadísticas básicas de tickets
     */
    public Map<String, Integer> obtenerEstadisticas() {
        Map<String, Integer> stats = new HashMap<>();

        try {
            String query = """
                SELECT estado, COUNT(*) as cantidad 
                FROM tickets 
                WHERE activo = 1 
                GROUP BY estado
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            // Inicializar con 0
            stats.put("ABIERTO", 0);
            stats.put("EN_REVISION", 0);
            stats.put("RESUELTO", 0);
            stats.put("CERRADO", 0);

            while (rs.next()) {
                stats.put(rs.getString("estado"), rs.getInt("cantidad"));
            }

            // Total
            stats.put("TOTAL", stats.values().stream().mapToInt(Integer::intValue).sum());

        } catch (SQLException e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Busca tickets por texto (asunto o descripción)
     */
    public List<Ticket> buscarTickets(String textoBusqueda, String estadoFiltro, String categoriaFiltro) {
        List<Ticket> tickets = new ArrayList<>();

        try {
            StringBuilder queryBuilder = new StringBuilder("""
                SELECT t.*, u.nombre, u.apellido,
                       CONCAT(u.apellido, ', ', u.nombre) as nombre_completo
                FROM tickets t
                INNER JOIN usuarios u ON t.usuario_reporta_id = u.id
                WHERE t.activo = 1
                """);

            List<Object> params = new ArrayList<>();

            // Filtro de texto
            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                queryBuilder.append(" AND (t.asunto LIKE ? OR t.descripcion LIKE ? OR t.ticket_number LIKE ?)");
                String searchPattern = "%" + textoBusqueda.trim() + "%";
                params.add(searchPattern);
                params.add(searchPattern);
                params.add(searchPattern);
            }

            // Filtro de estado
            if (estadoFiltro != null && !estadoFiltro.equals("TODOS")) {
                queryBuilder.append(" AND t.estado = ?");
                params.add(estadoFiltro);
            }

            // Filtro de categoría
            if (categoriaFiltro != null && !categoriaFiltro.equals("TODAS")) {
                queryBuilder.append(" AND t.categoria = ?");
                params.add(categoriaFiltro);
            }

            queryBuilder.append(" ORDER BY t.fecha_creacion DESC");

            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());

            // Establecer parámetros
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ticket ticket = mapearTicketDesdeResultSet(rs);
                ticket.setNombreCompleto(rs.getString("nombre_completo"));
                tickets.add(ticket);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando tickets: " + e.getMessage());
        }

        return tickets;
    }
}
