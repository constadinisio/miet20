package main.java.services;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.models.Notificacion;
import main.java.models.NotificacionDestinatario;

/**
 * NotificationService COMPLETAMENTE REESCRITO v3.0 Servicio principal para la
 * gestión de notificaciones del sistema escolar CON SOPORTE COMPLETO PARA
 * GRUPOS PERSONALIZADOS
 *
 * Características: - Gestión de notificaciones individuales, por rol y por
 * grupo - Soporte completo para grupos personalizados de usuarios - Gestión de
 * miembros de grupos - Filtros avanzados y búsquedas - Sistema de listeners
 * para actualizaciones en tiempo real - Estadísticas y reportes
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 3.0 - Con soporte completo para grupos personalizados
 */
public class NotificationService {

    private static NotificationService instance;
    private Connection connection;
    private ExecutorService executorService;
    private List<NotificationListener> listeners;

    // Constructor privado para Singleton
    private NotificationService() {
        this.connection = Conexion.getInstancia().verificarConexion();
        this.executorService = Executors.newFixedThreadPool(5); // Aumentado para grupos
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
    // MÉTODOS PRINCIPALES DE NOTIFICACIONES
    // ========================================
    /**
     * Envía una notificación a destinatarios específicos MEJORADO: Ahora
     * soporta grupos personalizados
     */
    public CompletableFuture<Boolean> enviarNotificacion(Notificacion notificacion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📤 Enviando notificación: " + notificacion.getTitulo());
                System.out.println("📋 Tipo: " + notificacion.getTipoNotificacion());

                // 1. Insertar la notificación principal
                int notificacionId = insertarNotificacion(notificacion);

                // 2. Resolver destinatarios según el tipo (MEJORADO CON GRUPOS)
                List<Integer> destinatarios = resolverDestinatarios(notificacion);

                if (destinatarios.isEmpty()) {
                    System.err.println("❌ No se encontraron destinatarios válidos");
                    return false;
                }

                // 3. Insertar destinatarios
                insertarDestinatarios(notificacionId, destinatarios);

                // 4. Notificar a listeners (para actualizar UI en tiempo real)
                notificarListeners(notificacionId, destinatarios);

                System.out.println("✅ Notificación enviada exitosamente. ID: " + notificacionId);
                System.out.println("👥 Destinatarios: " + destinatarios.size());
                return true;

            } catch (Exception e) {
                System.err.println("❌ Error enviando notificación: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }

    /**
     * Obtiene notificaciones no leídas de un usuario
     */
    public List<NotificacionDestinatario> obtenerNotificacionesNoLeidas(int usuarioId) {
        List<NotificacionDestinatario> notificaciones = new ArrayList<>();

        try {
            String query = """
                SELECT n.id, n.titulo, n.contenido, n.fecha_creacion, n.prioridad, 
                       n.icono, n.color, n.requiere_confirmacion,
                       COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema') as remitente_nombre,
                       nd.estado_lectura, nd.fecha_leida
                FROM notificaciones n 
                INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                LEFT JOIN usuarios u ON n.remitente_id = u.id
                WHERE nd.destinatario_id = ? 
                AND nd.estado_lectura = 'NO_LEIDA'
                AND n.estado = 'ACTIVA'
                AND (n.fecha_expiracion IS NULL OR n.fecha_expiracion > NOW())
                ORDER BY n.prioridad DESC, n.fecha_creacion DESC
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NotificacionDestinatario notif = new NotificacionDestinatario();
                notif.setId(rs.getInt("id"));
                notif.setTitulo(rs.getString("titulo"));
                notif.setContenido(rs.getString("contenido"));
                notif.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                notif.setPrioridad(rs.getString("prioridad"));
                notif.setIcono(rs.getString("icono"));
                notif.setColor(rs.getString("color"));
                notif.setRequiereConfirmacion(rs.getBoolean("requiere_confirmacion"));
                notif.setRemitenteNombre(rs.getString("remitente_nombre"));
                notif.setEstadoLectura(rs.getString("estado_lectura"));

                notificaciones.add(notif);
            }

            System.out.println("📬 Notificaciones no leídas para usuario " + usuarioId + ": " + notificaciones.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo notificaciones no leídas: " + e.getMessage());
            e.printStackTrace();
        }

        return notificaciones;
    }

    /**
     * Obtiene todas las notificaciones de un usuario (leídas y no leídas)
     * MEJORADO: Con mejor ordenamiento y filtros
     */
    public List<NotificacionDestinatario> obtenerTodasLasNotificaciones(int usuarioId, int limite) {
        List<NotificacionDestinatario> notificaciones = new ArrayList<>();

        try {
            String query = """
                SELECT n.id, n.titulo, n.contenido, n.fecha_creacion, n.prioridad, 
                       n.icono, n.color, n.requiere_confirmacion,
                       COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema') as remitente_nombre,
                       nd.estado_lectura, nd.fecha_leida, nd.fecha_confirmada
                FROM notificaciones n 
                INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                LEFT JOIN usuarios u ON n.remitente_id = u.id
                WHERE nd.destinatario_id = ? 
                AND n.estado = 'ACTIVA'
                ORDER BY 
                    CASE WHEN nd.estado_lectura = 'NO_LEIDA' THEN 0 ELSE 1 END,
                    n.prioridad DESC,
                    n.fecha_creacion DESC
                LIMIT ?
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ps.setInt(2, limite);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NotificacionDestinatario notif = new NotificacionDestinatario();
                notif.setId(rs.getInt("id"));
                notif.setTitulo(rs.getString("titulo"));
                notif.setContenido(rs.getString("contenido"));
                notif.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                notif.setPrioridad(rs.getString("prioridad"));
                notif.setIcono(rs.getString("icono"));
                notif.setColor(rs.getString("color"));
                notif.setRequiereConfirmacion(rs.getBoolean("requiere_confirmacion"));
                notif.setRemitenteNombre(rs.getString("remitente_nombre"));
                notif.setEstadoLectura(rs.getString("estado_lectura"));

                Timestamp fechaLeida = rs.getTimestamp("fecha_leida");
                if (fechaLeida != null) {
                    notif.setFechaLeida(fechaLeida.toLocalDateTime());
                }

                Timestamp fechaConfirmada = rs.getTimestamp("fecha_confirmada");
                if (fechaConfirmada != null) {
                    notif.setFechaConfirmada(fechaConfirmada.toLocalDateTime());
                }

                notificaciones.add(notif);
            }

            System.out.println("📋 Todas las notificaciones para usuario " + usuarioId + ": " + notificaciones.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo todas las notificaciones: " + e.getMessage());
            e.printStackTrace();
        }

        return notificaciones;
    }

    /**
     * NUEVO: Obtiene notificaciones por prioridad
     */
    public List<NotificacionDestinatario> obtenerNotificacionesPorPrioridad(int usuarioId, String prioridad) {
        List<NotificacionDestinatario> notificaciones = new ArrayList<>();

        try {
            String query = """
                SELECT n.id, n.titulo, n.contenido, n.fecha_creacion, n.prioridad, 
                       n.icono, n.color, n.requiere_confirmacion,
                       COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema') as remitente_nombre,
                       nd.estado_lectura, nd.fecha_leida
                FROM notificaciones n 
                INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                LEFT JOIN usuarios u ON n.remitente_id = u.id
                WHERE nd.destinatario_id = ? 
                AND n.prioridad = ?
                AND n.estado = 'ACTIVA'
                ORDER BY n.fecha_creacion DESC
                LIMIT 50
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ps.setString(2, prioridad);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NotificacionDestinatario notif = new NotificacionDestinatario();
                notif.setId(rs.getInt("id"));
                notif.setTitulo(rs.getString("titulo"));
                notif.setContenido(rs.getString("contenido"));
                notif.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                notif.setPrioridad(rs.getString("prioridad"));
                notif.setIcono(rs.getString("icono"));
                notif.setColor(rs.getString("color"));
                notif.setRequiereConfirmacion(rs.getBoolean("requiere_confirmacion"));
                notif.setRemitenteNombre(rs.getString("remitente_nombre"));
                notif.setEstadoLectura(rs.getString("estado_lectura"));

                notificaciones.add(notif);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo notificaciones por prioridad: " + e.getMessage());
            e.printStackTrace();
        }

        return notificaciones;
    }

    /**
     * NUEVO: Obtiene notificaciones de las últimas 24 horas
     */
    public List<NotificacionDestinatario> obtenerNotificacionesUltimas24h(int usuarioId) {
        List<NotificacionDestinatario> notificaciones = new ArrayList<>();

        try {
            String query = """
                SELECT n.id, n.titulo, n.contenido, n.fecha_creacion, n.prioridad, 
                       n.icono, n.color, n.requiere_confirmacion,
                       COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema') as remitente_nombre,
                       nd.estado_lectura, nd.fecha_leida
                FROM notificaciones n 
                INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                LEFT JOIN usuarios u ON n.remitente_id = u.id
                WHERE nd.destinatario_id = ? 
                AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
                AND n.estado = 'ACTIVA'
                ORDER BY n.fecha_creacion DESC
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NotificacionDestinatario notif = new NotificacionDestinatario();
                notif.setId(rs.getInt("id"));
                notif.setTitulo(rs.getString("titulo"));
                notif.setContenido(rs.getString("contenido"));
                notif.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                notif.setPrioridad(rs.getString("prioridad"));
                notif.setIcono(rs.getString("icono"));
                notif.setColor(rs.getString("color"));
                notif.setRequiereConfirmacion(rs.getBoolean("requiere_confirmacion"));
                notif.setRemitenteNombre(rs.getString("remitente_nombre"));
                notif.setEstadoLectura(rs.getString("estado_lectura"));

                notificaciones.add(notif);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo notificaciones últimas 24h: " + e.getMessage());
            e.printStackTrace();
        }

        return notificaciones;
    }

    /**
     * Marca una notificación como leída
     * @param notificacionId
     * @param usuarioId
     * @return 
     */
    public boolean marcarComoLeida(int notificacionId, int usuarioId) {
        // Validaciones de entrada
        if (notificacionId <= 0) {
            System.err.println("❌ ID de notificación inválido: " + notificacionId);
            return false;
        }

        if (usuarioId <= 0) {
            System.err.println("❌ ID de usuario inválido: " + usuarioId);
            return false;
        }

        System.out.println("📧 Marcando como leída - Notificación: " + notificacionId + ", Usuario: " + usuarioId);

        try {
            // PASO 1: Verificar que la notificación existe y el usuario es destinatario
            String verificarQuery = """
            SELECT nd.estado_lectura, n.titulo
            FROM notificaciones_destinatarios nd
            INNER JOIN notificaciones n ON nd.notificacion_id = n.id
            WHERE nd.notificacion_id = ? AND nd.destinatario_id = ? AND n.estado = 'ACTIVA'
            """;

            PreparedStatement psVerificar = connection.prepareStatement(verificarQuery);
            psVerificar.setInt(1, notificacionId);
            psVerificar.setInt(2, usuarioId);
            ResultSet rsVerificar = psVerificar.executeQuery();

            if (!rsVerificar.next()) {
                System.err.println("❌ No se encontró notificación válida con ID: " + notificacionId + " para usuario: " + usuarioId);
                return false;
            }

            String estadoActual = rsVerificar.getString("estado_lectura");
            String titulo = rsVerificar.getString("titulo");

            // Si ya está leída, considerar como exitoso
            if ("LEIDA".equals(estadoActual) || "CONFIRMADA".equals(estadoActual)) {
                System.out.println("ℹ️ Notificación ya estaba marcada como leída: " + titulo);

                // Notificar cambio de estado aunque ya estuviera leída
                notificarCambioEstado(usuarioId);
                return true;
            }

            System.out.println("📝 Actualizando estado de '" + titulo + "' de " + estadoActual + " a LEIDA");

            // PASO 2: Actualizar el estado a leída
            String updateQuery = """
            UPDATE notificaciones_destinatarios 
            SET estado_lectura = 'LEIDA', fecha_leida = NOW() 
            WHERE notificacion_id = ? AND destinatario_id = ? AND estado_lectura = 'NO_LEIDA'
            """;

            PreparedStatement psUpdate = connection.prepareStatement(updateQuery);
            psUpdate.setInt(1, notificacionId);
            psUpdate.setInt(2, usuarioId);

            int filasActualizadas = psUpdate.executeUpdate();

            // PASO 3: Verificar el resultado
            boolean exitoso = filasActualizadas > 0;

            if (exitoso) {
                System.out.println("✅ Notificación marcada como leída exitosamente");
                System.out.println("   - Título: " + titulo);
                System.out.println("   - Usuario: " + usuarioId);
                System.out.println("   - Filas actualizadas: " + filasActualizadas);

                // Notificar a listeners para actualizar contadores
                notificarCambioEstado(usuarioId);
            } else {
                System.err.println("❌ No se pudo actualizar el estado - Filas afectadas: " + filasActualizadas);
                System.err.println("   Posibles causas:");
                System.err.println("   - La notificación ya estaba leída");
                System.err.println("   - Problema de concurrencia");
                System.err.println("   - Estado de la notificación cambió");

                // PASO 4: VERIFICACIÓN ADICIONAL - Comprobar si ahora está leída
                String verificarFinalQuery = """
                SELECT estado_lectura 
                FROM notificaciones_destinatarios 
                WHERE notificacion_id = ? AND destinatario_id = ?
                """;

                PreparedStatement psVerificarFinal = connection.prepareStatement(verificarFinalQuery);
                psVerificarFinal.setInt(1, notificacionId);
                psVerificarFinal.setInt(2, usuarioId);
                ResultSet rsVerificarFinal = psVerificarFinal.executeQuery();

                if (rsVerificarFinal.next()) {
                    String estadoFinal = rsVerificarFinal.getString("estado_lectura");
                    System.out.println("🔍 Estado actual después del intento: " + estadoFinal);

                    // Si el estado final es LEIDA o CONFIRMADA, considerar como exitoso
                    if ("LEIDA".equals(estadoFinal) || "CONFIRMADA".equals(estadoFinal)) {
                        System.out.println("✅ Verificación confirma que está marcada como leída");
                        notificarCambioEstado(usuarioId);
                        return true;
                    }
                }
            }

            return exitoso;

        } catch (SQLException e) {
            System.err.println("❌ Error SQL marcando notificación como leída: " + e.getMessage());
            System.err.println("   - Notificación ID: " + notificacionId);
            System.err.println("   - Usuario ID: " + usuarioId);
            System.err.println("   - Error: " + e.getSQLState());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado marcando notificación como leída: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el contador de notificaciones no leídas de un usuario
     */
    public int contarNotificacionesNoLeidas(int usuarioId) {
        try {
            String query = """
                SELECT COUNT(*) as total
                FROM notificaciones n 
                INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                WHERE nd.destinatario_id = ? 
                AND nd.estado_lectura = 'NO_LEIDA'
                AND n.estado = 'ACTIVA'
                AND (n.fecha_expiracion IS NULL OR n.fecha_expiracion > NOW())
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error contando notificaciones no leídas: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // ========================================
    // MÉTODOS PARA GRUPOS PERSONALIZADOS - NUEVOS
    // ========================================
    /**
     * NUEVO: Crea un grupo personalizado de notificaciones
     */
    public CompletableFuture<Integer> crearGrupoPersonalizado(String nombre, String descripcion,
            int creadorId, List<Integer> miembros) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connection.setAutoCommit(false);

                System.out.println("📝 Creando grupo personalizado: " + nombre);
                System.out.println("👤 Creador: " + creadorId);
                System.out.println("👥 Miembros: " + miembros.size());

                // 1. Verificar que el nombre no esté duplicado para este usuario
                if (!esNombreGrupoUnico(nombre, creadorId, -1)) {
                    System.err.println("❌ Ya existe un grupo con el nombre: " + nombre);
                    connection.rollback();
                    return -1;
                }

                // 2. Insertar grupo
                String queryGrupo = """
                    INSERT INTO grupos_notificacion_personalizados 
                    (nombre, descripcion, creador_id, fecha_creacion, activo) 
                    VALUES (?, ?, ?, NOW(), 1)
                    """;

                PreparedStatement psGrupo = connection.prepareStatement(queryGrupo, Statement.RETURN_GENERATED_KEYS);
                psGrupo.setString(1, nombre);
                psGrupo.setString(2, descripcion);
                psGrupo.setInt(3, creadorId);

                psGrupo.executeUpdate();
                ResultSet rsKeys = psGrupo.getGeneratedKeys();

                int grupoId = -1;
                if (rsKeys.next()) {
                    grupoId = rsKeys.getInt(1);
                }

                // 3. Insertar miembros si los hay
                if (grupoId > 0 && miembros != null && !miembros.isEmpty()) {
                    insertarMiembrosGrupoPersonalizado(grupoId, miembros);
                }

                connection.commit();
                System.out.println("✅ Grupo personalizado creado exitosamente. ID: " + grupoId);
                return grupoId;

            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error en rollback: " + rollbackEx.getMessage());
                }
                System.err.println("Error creando grupo personalizado: " + e.getMessage());
                e.printStackTrace();
                return -1;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restaurando autocommit: " + e.getMessage());
                }
            }
        }, executorService);
    }

    /**
     * NUEVO: Obtiene todos los grupos personalizados de un usuario
     */
    public List<GrupoPersonalizado> obtenerGruposPersonalizados(int creadorId) {
        List<GrupoPersonalizado> grupos = new ArrayList<>();

        try {
            String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion, 
                       COUNT(mgp.usuario_id) as cantidad_miembros
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                WHERE gp.creador_id = ? AND gp.activo = 1
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                ORDER BY gp.fecha_creacion DESC
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, creadorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GrupoPersonalizado grupo = new GrupoPersonalizado();
                grupo.setId(rs.getInt("id"));
                grupo.setNombre(rs.getString("nombre"));
                grupo.setDescripcion(rs.getString("descripcion"));
                grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                grupo.setCantidadMiembros(rs.getInt("cantidad_miembros"));

                grupos.add(grupo);
            }

            System.out.println("📋 Grupos personalizados para usuario " + creadorId + ": " + grupos.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo grupos personalizados: " + e.getMessage());
            e.printStackTrace();
        }

        return grupos;
    }

    /**
     * NUEVO: Obtiene un grupo personalizado por ID con sus miembros
     */
    public GrupoPersonalizado obtenerGrupoPersonalizadoPorId(int grupoId) {
        try {
            String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.creador_id, gp.fecha_creacion,
                       COUNT(mgp.usuario_id) as cantidad_miembros
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                WHERE gp.id = ? AND gp.activo = 1
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.creador_id, gp.fecha_creacion
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, grupoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                GrupoPersonalizado grupo = new GrupoPersonalizado();
                grupo.setId(rs.getInt("id"));
                grupo.setNombre(rs.getString("nombre"));
                grupo.setDescripcion(rs.getString("descripcion"));
                grupo.setCreadorId(rs.getInt("creador_id"));
                grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                grupo.setCantidadMiembros(rs.getInt("cantidad_miembros"));

                // Cargar miembros
                grupo.setMiembros(obtenerMiembrosGrupoPersonalizado(grupoId));

                return grupo;
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo grupo personalizado por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * NUEVO: Obtiene los miembros de un grupo personalizado
     */
    public List<Integer> obtenerMiembrosGrupoPersonalizado(int grupoId) {
        List<Integer> miembros = new ArrayList<>();

        try {
            // CORREGIDO: La tabla usuarios usa 'status', no 'activo'
            String query = """
            SELECT mgp.usuario_id 
            FROM miembros_grupos_personalizados mgp
            INNER JOIN usuarios u ON mgp.usuario_id = u.id
            WHERE mgp.grupo_id = ? AND mgp.activo = 1 AND u.status = 1
            ORDER BY u.apellido, u.nombre
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, grupoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                miembros.add(rs.getInt("usuario_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo miembros del grupo personalizado: " + e.getMessage());
            e.printStackTrace();
        }

        return miembros;
    }

    /**
     * NUEVO: Actualiza un grupo personalizado
     */
    public CompletableFuture<Boolean> actualizarGrupoPersonalizado(int grupoId, String nombre,
            String descripcion, List<Integer> miembros,
            int creadorId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connection.setAutoCommit(false);

                System.out.println("📝 Actualizando grupo personalizado ID: " + grupoId);

                // 1. Verificar que el usuario es el creador
                if (!esCreadorDelGrupo(grupoId, creadorId)) {
                    System.err.println("❌ Usuario no es creador del grupo");
                    connection.rollback();
                    return false;
                }

                // 2. Verificar nombre único (excluyendo el grupo actual)
                if (!esNombreGrupoUnico(nombre, creadorId, grupoId)) {
                    System.err.println("❌ Ya existe un grupo con el nombre: " + nombre);
                    connection.rollback();
                    return false;
                }

                // 3. Actualizar información del grupo
                String queryUpdate = """
                    UPDATE grupos_notificacion_personalizados 
                    SET nombre = ?, descripcion = ?, fecha_modificacion = NOW()
                    WHERE id = ? AND creador_id = ?
                    """;

                PreparedStatement psUpdate = connection.prepareStatement(queryUpdate);
                psUpdate.setString(1, nombre);
                psUpdate.setString(2, descripcion);
                psUpdate.setInt(3, grupoId);
                psUpdate.setInt(4, creadorId);

                int rowsUpdated = psUpdate.executeUpdate();

                if (rowsUpdated > 0) {
                    // 4. Actualizar miembros si se proporcionaron
                    if (miembros != null) {
                        // Desactivar miembros actuales
                        desactivarTodosMiembrosGrupoPersonalizado(grupoId);

                        // Insertar nuevos miembros
                        if (!miembros.isEmpty()) {
                            insertarMiembrosGrupoPersonalizado(grupoId, miembros);
                        }
                    }

                    connection.commit();
                    System.out.println("✅ Grupo personalizado actualizado exitosamente");
                    return true;
                }

                connection.rollback();
                return false;

            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error en rollback: " + rollbackEx.getMessage());
                }
                System.err.println("Error actualizando grupo personalizado: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restaurando autocommit: " + e.getMessage());
                }
            }
        }, executorService);
    }

    /**
     * NUEVO: Elimina un grupo personalizado (marca como inactivo)
     */
    public CompletableFuture<Boolean> eliminarGrupoPersonalizado(int grupoId, int creadorId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verificar que el usuario es el creador
                if (!esCreadorDelGrupo(grupoId, creadorId)) {
                    System.err.println("❌ Usuario no es creador del grupo");
                    return false;
                }

                String query = """
                    UPDATE grupos_notificacion_personalizados 
                    SET activo = 0, fecha_eliminacion = NOW() 
                    WHERE id = ? AND creador_id = ?
                    """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, grupoId);
                ps.setInt(2, creadorId);

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated > 0) {
                    // También desactivar miembros
                    desactivarTodosMiembrosGrupoPersonalizado(grupoId);

                    System.out.println("✅ Grupo personalizado eliminado exitosamente (ID: " + grupoId + ")");
                    return true;
                }

                return false;

            } catch (SQLException e) {
                System.err.println("Error eliminando grupo personalizado: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }

    /**
     * NUEVO: Busca grupos personalizados por nombre
     */
    public List<GrupoPersonalizado> buscarGruposPersonalizados(String nombreBusqueda, int creadorId) {
        List<GrupoPersonalizado> grupos = new ArrayList<>();

        try {
            String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion, 
                       COUNT(mgp.usuario_id) as cantidad_miembros
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                WHERE gp.creador_id = ? AND gp.activo = 1 
                AND (gp.nombre LIKE ? OR gp.descripcion LIKE ?)
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                ORDER BY gp.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, creadorId);
            String searchPattern = "%" + nombreBusqueda + "%";
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GrupoPersonalizado grupo = new GrupoPersonalizado();
                grupo.setId(rs.getInt("id"));
                grupo.setNombre(rs.getString("nombre"));
                grupo.setDescripcion(rs.getString("descripcion"));
                grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                grupo.setCantidadMiembros(rs.getInt("cantidad_miembros"));

                grupos.add(grupo);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando grupos personalizados: " + e.getMessage());
            e.printStackTrace();
        }

        return grupos;
    }

    /**
     * NUEVO: Duplica un grupo personalizado existente
     */
    public CompletableFuture<Integer> duplicarGrupoPersonalizado(int grupoId, String nuevoNombre, int creadorId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Obtener grupo original
                GrupoPersonalizado grupoOriginal = obtenerGrupoPersonalizadoPorId(grupoId);

                if (grupoOriginal == null || grupoOriginal.getCreadorId() != creadorId) {
                    System.err.println("❌ Grupo no encontrado o usuario sin permisos");
                    return -1;
                }

                // 2. Crear nuevo grupo con los mismos miembros
                String descripcionCopia = "Copia de: " + grupoOriginal.getDescripcion();
                List<Integer> miembrosOriginales = grupoOriginal.getMiembros();

                CompletableFuture<Integer> futureGrupoId = crearGrupoPersonalizado(
                        nuevoNombre, descripcionCopia, creadorId, miembrosOriginales
                );

                return futureGrupoId.get();

            } catch (Exception e) {
                System.err.println("Error duplicando grupo personalizado: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }, executorService);
    }

    // ========================================
    // MÉTODOS AUXILIARES PARA GRUPOS PERSONALIZADOS
    // ========================================
    /**
     * Inserta múltiples miembros en un grupo personalizado
     */
    private void insertarMiembrosGrupoPersonalizado(int grupoId, List<Integer> miembros) throws SQLException {
        String queryMiembros = """
           INSERT INTO miembros_grupos_personalizados (grupo_id, usuario_id, fecha_agregado, activo) 
           VALUES (?, ?, NOW(), 1)
           ON DUPLICATE KEY UPDATE activo = 1, fecha_agregado = NOW()
           """;

        PreparedStatement psMiembros = connection.prepareStatement(queryMiembros);

        for (Integer miembroId : miembros) {
            // Verificar que el usuario existe y está activo
            if (existeUsuarioActivo(miembroId)) {
                psMiembros.setInt(1, grupoId);
                psMiembros.setInt(2, miembroId);
                psMiembros.addBatch();
            }
        }

        psMiembros.executeBatch();
        System.out.println("✅ Insertados miembros en grupo personalizado ID: " + grupoId);
    }

    /**
     * Desactiva todos los miembros de un grupo personalizado
     */
    private void desactivarTodosMiembrosGrupoPersonalizado(int grupoId) throws SQLException {
        String query = "UPDATE miembros_grupos_personalizados SET activo = 0 WHERE grupo_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, grupoId);
        ps.executeUpdate();
    }

    /**
     * Verifica si un nombre de grupo es único para un usuario
     */
    private boolean esNombreGrupoUnico(String nombre, int creadorId, int grupoIdExcluir) {
        try {
            // CORREGIDO: La tabla grupos_notificacion_personalizados usa 'activo', no 'status'
            String query = """
            SELECT COUNT(*) FROM grupos_notificacion_personalizados 
            WHERE nombre = ? AND creador_id = ? AND activo = 1 AND id != ?
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nombre);
            ps.setInt(2, creadorId);
            ps.setInt(3, grupoIdExcluir);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            System.err.println("Error validando nombre de grupo: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Verifica si un usuario es el creador de un grupo
     */
    private boolean esCreadorDelGrupo(int grupoId, int usuarioId) {
        try {
            // CORREGIDO: La tabla grupos_notificacion_personalizados usa 'activo', no 'status'
            String query = """
            SELECT COUNT(*) FROM grupos_notificacion_personalizados 
            WHERE id = ? AND creador_id = ? AND activo = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, grupoId);
            ps.setInt(2, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando creador de grupo: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Verifica si un usuario existe y está activo
     */
    private boolean existeUsuarioActivo(int usuarioId) {
        try {
            String query = "SELECT COUNT(*) FROM usuarios WHERE id = ? AND status = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando usuario activo: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ========================================
    // MÉTODOS AUXILIARES PRINCIPALES
    // ========================================
    /**
     * Inserta la notificación principal en la base de datos
     */
    private int insertarNotificacion(Notificacion notificacion) throws SQLException {
        String query = """
           INSERT INTO notificaciones (titulo, contenido, tipo_notificacion, remitente_id, 
                                     fecha_creacion, fecha_expiracion, prioridad, estado,
                                     requiere_confirmacion, icono, color) 
           VALUES (?, ?, ?, ?, NOW(), ?, ?, 'ACTIVA', ?, ?, ?)
           """;

        PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, notificacion.getTitulo());
        ps.setString(2, notificacion.getContenido());
        ps.setString(3, notificacion.getTipoNotificacion());
        ps.setInt(4, notificacion.getRemitenteId());
        ps.setTimestamp(5, notificacion.getFechaExpiracion() != null
                ? Timestamp.valueOf(notificacion.getFechaExpiracion()) : null);
        ps.setString(6, notificacion.getPrioridad() != null ? notificacion.getPrioridad() : "NORMAL");
        ps.setBoolean(7, notificacion.isRequiereConfirmacion());
        ps.setString(8, notificacion.getIcono() != null ? notificacion.getIcono() : "ℹ️");
        ps.setString(9, notificacion.getColor() != null ? notificacion.getColor() : "#007bff");

        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {
            return rs.getInt(1);
        }

        throw new SQLException("No se pudo obtener el ID de la notificación insertada");
    }

    /**
     * Resuelve los destinatarios según el tipo de notificación MEJORADO: Ahora
     * incluye soporte para grupos personalizados
     */
    private List<Integer> resolverDestinatarios(Notificacion notificacion) throws SQLException {
        List<Integer> destinatarios = new ArrayList<>();
        String tipo = notificacion.getTipoNotificacion();

        System.out.println("🔍 Resolviendo destinatarios para tipo: " + tipo);

        switch (tipo) {
            case "INDIVIDUAL":
                if (notificacion.getDestinatariosIndividuales() != null) {
                    destinatarios.addAll(notificacion.getDestinatariosIndividuales());
                    System.out.println("👤 Destinatarios individuales: " + destinatarios.size());
                }
                break;

            case "ROL":
                destinatarios.addAll(obtenerUsuariosPorRol(notificacion.getRolDestino()));
                System.out.println("👥 Destinatarios por rol " + notificacion.getRolDestino() + ": " + destinatarios.size());
                break;

            case "GRUPO":
                destinatarios.addAll(obtenerUsuariosDeGrupo(notificacion.getGrupoDestino()));
                System.out.println("📁 Destinatarios de grupo " + notificacion.getGrupoDestino() + ": " + destinatarios.size());
                break;

            case "GRUPO_PERSONALIZADO":
                if (notificacion.getGrupoPersonalizadoId() > 0) {
                    destinatarios.addAll(obtenerMiembrosGrupoPersonalizado(notificacion.getGrupoPersonalizadoId()));
                    System.out.println("👥 Destinatarios de grupo personalizado: " + destinatarios.size());
                }
                break;

            default:
                System.err.println("❌ Tipo de notificación no reconocido: " + tipo);
                break;
        }

        // Eliminar duplicados y usuarios inactivos
        destinatarios = destinatarios.stream()
                .distinct()
                .filter(this::existeUsuarioActivo)
                .collect(java.util.stream.Collectors.toList());

        System.out.println("✅ Destinatarios finales (sin duplicados): " + destinatarios.size());
        return destinatarios;
    }

    /**
     * Obtiene usuarios por rol
     */
    private List<Integer> obtenerUsuariosPorRol(int rolId) throws SQLException {
        List<Integer> usuarios = new ArrayList<>();

        // CORREGIDO: Adaptado a la estructura real de la BD con usuario_roles
        String query = """
        SELECT u.id 
        FROM usuarios u
        INNER JOIN usuario_roles ur ON u.id = ur.usuario_id
        WHERE ur.rol_id = ? AND u.status = 1
        """;

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, rolId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            usuarios.add(rs.getInt("id"));
        }

        return usuarios;
    }

    /**
     * Obtiene usuarios de un grupo (sistema anterior)
     */
    private List<Integer> obtenerUsuariosDeGrupo(int grupoId) throws SQLException {
        List<Integer> usuarios = new ArrayList<>();

        // CORREGIDO: Según tu BD, los grupos están en la tabla grupos_notificacion
        String query = """
        SELECT gnm.usuario_id 
        FROM grupos_notificacion_miembros gnm
        INNER JOIN grupos_notificacion gn ON gnm.grupo_id = gn.id
        INNER JOIN usuarios u ON gnm.usuario_id = u.id
        WHERE gnm.grupo_id = ? AND gnm.activo = 1 AND gn.activo = 1 AND u.status = 1
        """;

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, grupoId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            usuarios.add(rs.getInt("usuario_id"));
        }

        return usuarios;
    }

    /**
     * Inserta los destinatarios de una notificación
     */
    private void insertarDestinatarios(int notificacionId, List<Integer> destinatarios) throws SQLException {
        // CORREGIDO: Eliminé la columna 'fecha_recibida' que no existe en tu BD
        String query = """
        INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) 
        VALUES (?, ?, 'NO_LEIDA')
        """;

        PreparedStatement ps = connection.prepareStatement(query);

        for (Integer destinatarioId : destinatarios) {
            ps.setInt(1, notificacionId);
            ps.setInt(2, destinatarioId);
            ps.addBatch();
        }

        ps.executeBatch();
        System.out.println("✅ Destinatarios insertados: " + destinatarios.size());
    }

    // ========================================
    // SISTEMA DE LISTENERS Y EVENTOS
    // ========================================
    /**
     * Agrega un listener para actualizaciones en tiempo real
     */
    public void agregarListener(NotificationListener listener) {
        listeners.add(listener);
        System.out.println("👂 Listener agregado. Total: " + listeners.size());
    }

    /**
     * Remueve un listener
     */
    public void removerListener(NotificationListener listener) {
        listeners.remove(listener);
        System.out.println("👂 Listener removido. Total: " + listeners.size());
    }

    /**
     * Notifica a todos los listeners sobre nueva notificación
     */
    private void notificarListeners(int notificacionId, List<Integer> destinatarios) {
        SwingUtilities.invokeLater(() -> {
            for (NotificationListener listener : listeners) {
                for (Integer destinatarioId : destinatarios) {
                    try {
                        listener.onNuevaNotificacion(destinatarioId, notificacionId);
                    } catch (Exception e) {
                        System.err.println("Error notificando listener: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Notifica cambio de estado de notificación
     */
    private void notificarCambioEstado(int usuarioId) {
        SwingUtilities.invokeLater(() -> {
            for (NotificationListener listener : listeners) {
                try {
                    listener.onCambioEstadoNotificacion(usuarioId);
                } catch (Exception e) {
                    System.err.println("Error notificando cambio estado: " + e.getMessage());
                }
            }
        });
    }

    // ========================================
    // INTERFACES Y CLASES INTERNAS
    // ========================================
    /**
     * Interfaz para listeners de notificaciones
     */
    public interface NotificationListener {

        void onNuevaNotificacion(int usuarioId, int notificacionId);

        void onCambioEstadoNotificacion(int usuarioId);
    }

    /**
     * Clase para grupos personalizados
     */
    public static class GrupoPersonalizado {

        private int id;
        private String nombre;
        private String descripcion;
        private int creadorId;
        private LocalDateTime fechaCreacion;
        private int cantidadMiembros;
        private List<Integer> miembros;

        // Constructores
        public GrupoPersonalizado() {
            this.miembros = new ArrayList<>();
        }

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public int getCreadorId() {
            return creadorId;
        }

        public void setCreadorId(int creadorId) {
            this.creadorId = creadorId;
        }

        public LocalDateTime getFechaCreacion() {
            return fechaCreacion;
        }

        public void setFechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
        }

        public int getCantidadMiembros() {
            return cantidadMiembros;
        }

        public void setCantidadMiembros(int cantidadMiembros) {
            this.cantidadMiembros = cantidadMiembros;
        }

        public List<Integer> getMiembros() {
            return miembros;
        }

        public void setMiembros(List<Integer> miembros) {
            this.miembros = miembros != null ? miembros : new ArrayList<>();
            this.cantidadMiembros = this.miembros.size();
        }

        @Override
        public String toString() {
            return nombre + " (" + cantidadMiembros + " miembros)";
        }
    }

    // ========================================
    // MÉTODOS DE ESTADÍSTICAS Y UTILIDADES
    // ========================================
    /**
     * NUEVO: Obtiene estadísticas de grupos personalizados de un usuario
     */
    public EstadisticasGrupos obtenerEstadisticasGrupos(int creadorId) {
        EstadisticasGrupos stats = new EstadisticasGrupos();

        try {
            // CORREGIDO: Agregué COALESCE para manejar valores NULL
            String queryTotal = """
            SELECT COUNT(*) as total_grupos,
                   COALESCE(AVG(miembros_count.cantidad), 0) as promedio_miembros
            FROM grupos_notificacion_personalizados gp
            LEFT JOIN (
                SELECT grupo_id, COUNT(*) as cantidad 
                FROM miembros_grupos_personalizados 
                WHERE activo = 1 
                GROUP BY grupo_id
            ) miembros_count ON gp.id = miembros_count.grupo_id
            WHERE gp.creador_id = ? AND gp.activo = 1
            """;

            PreparedStatement psTotal = connection.prepareStatement(queryTotal);
            psTotal.setInt(1, creadorId);
            ResultSet rsTotal = psTotal.executeQuery();

            if (rsTotal.next()) {
                stats.setTotalGrupos(rsTotal.getInt("total_grupos"));
                stats.setPromedioMiembrosPorGrupo(rsTotal.getDouble("promedio_miembros"));
            }

            // Total de miembros únicos
            String queryMiembros = """
            SELECT COUNT(DISTINCT mgp.usuario_id) as miembros_unicos
            FROM grupos_notificacion_personalizados gp
            INNER JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id
            WHERE gp.creador_id = ? AND gp.activo = 1 AND mgp.activo = 1
            """;

            PreparedStatement psMiembros = connection.prepareStatement(queryMiembros);
            psMiembros.setInt(1, creadorId);
            ResultSet rsMiembros = psMiembros.executeQuery();

            if (rsMiembros.next()) {
                stats.setMiembrosUnicos(rsMiembros.getInt("miembros_unicos"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo estadísticas de grupos: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Clase para estadísticas de grupos
     */
    public static class EstadisticasGrupos {

        private int totalGrupos;
        private double promedioMiembrosPorGrupo;
        private int miembrosUnicos;

        // Getters y Setters
        public int getTotalGrupos() {
            return totalGrupos;
        }

        public void setTotalGrupos(int totalGrupos) {
            this.totalGrupos = totalGrupos;
        }

        public double getPromedioMiembrosPorGrupo() {
            return promedioMiembrosPorGrupo;
        }

        public void setPromedioMiembrosPorGrupo(double promedioMiembrosPorGrupo) {
            this.promedioMiembrosPorGrupo = promedioMiembrosPorGrupo;
        }

        public int getMiembrosUnicos() {
            return miembrosUnicos;
        }

        public void setMiembrosUnicos(int miembrosUnicos) {
            this.miembrosUnicos = miembrosUnicos;
        }

        @Override
        public String toString() {
            return String.format("Grupos: %d, Promedio miembros: %.1f, Miembros únicos: %d",
                    totalGrupos, promedioMiembrosPorGrupo, miembrosUnicos);
        }
    }

    // ========================================
    // MÉTODOS DE LIMPIEZA Y CIERRE
    // ========================================
    /**
     * Cierra el servicio y libera recursos
     */
    public void shutdown() {
        try {
            System.out.println("🔄 Cerrando NotificationService...");

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                System.out.println("✅ ExecutorService cerrado");
            }

            if (listeners != null) {
                listeners.clear();
                System.out.println("✅ Listeners limpiados");
            }

            System.out.println("✅ NotificationService cerrado correctamente");

        } catch (Exception e) {
            System.err.println("Error cerrando NotificationService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> enviarNotificacionAGrupoPersonalizado(
            String titulo, String contenido, int grupoPersonalizadoId,
            int remitenteId, String prioridad) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📤 Enviando notificación a grupo personalizado ID: " + grupoPersonalizadoId);

                // 1. Verificar que el grupo existe y está activo
                if (!existeGrupoPersonalizado(grupoPersonalizadoId)) {
                    System.err.println("❌ Grupo personalizado no encontrado o inactivo");
                    return false;
                }

                // 2. Insertar la notificación principal (requiere modificación en BD)
                String queryNotif = """
                INSERT INTO notificaciones (titulo, contenido, tipo_notificacion, remitente_id, 
                                          grupo_personalizado_id, fecha_creacion, prioridad, estado,
                                          requiere_confirmacion, icono, color) 
                VALUES (?, ?, 'GRUPO_PERSONALIZADO', ?, ?, NOW(), ?, 'ACTIVA', 0, 'group', '#28a745')
                """;

                PreparedStatement psNotif = connection.prepareStatement(queryNotif, Statement.RETURN_GENERATED_KEYS);
                psNotif.setString(1, titulo);
                psNotif.setString(2, contenido);
                psNotif.setInt(3, remitenteId);
                psNotif.setInt(4, grupoPersonalizadoId);
                psNotif.setString(5, prioridad != null ? prioridad : "NORMAL");

                psNotif.executeUpdate();
                ResultSet rs = psNotif.getGeneratedKeys();

                int notificacionId = -1;
                if (rs.next()) {
                    notificacionId = rs.getInt(1);
                }

                if (notificacionId > 0) {
                    // 3. Obtener miembros del grupo y crear destinatarios
                    List<Integer> miembros = obtenerMiembrosGrupoPersonalizado(grupoPersonalizadoId);
                    insertarDestinatarios(notificacionId, miembros);

                    // 4. Notificar a listeners
                    notificarListeners(notificacionId, miembros);

                    System.out.println("✅ Notificación enviada a grupo personalizado. Destinatarios: " + miembros.size());
                    return true;
                }

                return false;

            } catch (SQLException e) {
                System.err.println("❌ Error enviando notificación a grupo personalizado: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }

    /**
     * Verifica si existe un grupo personalizado activo
     */
    private boolean existeGrupoPersonalizado(int grupoId) {
        try {
            String query = """
            SELECT COUNT(*) FROM grupos_notificacion_personalizados 
            WHERE id = ? AND activo = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, grupoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando grupo personalizado: " + e.getMessage());
        }

        return false;
    }

    /**
     * NUEVO MÉTODO: Verifica el estado de lectura de una notificación
     * específica Útil para confirmar si una operación de marcado como leída fue
     * exitosa
     */
    public boolean verificarEstadoLectura(int notificacionId, int usuarioId) {
        try {
            String query = """
            SELECT nd.estado_lectura 
            FROM notificaciones_destinatarios nd
            INNER JOIN notificaciones n ON nd.notificacion_id = n.id
            WHERE nd.notificacion_id = ? AND nd.destinatario_id = ? AND n.estado = 'ACTIVA'
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, notificacionId);
            ps.setInt(2, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String estado = rs.getString("estado_lectura");
                boolean isRead = "LEIDA".equals(estado) || "CONFIRMADA".equals(estado);

                System.out.println("🔍 Verificación estado lectura - Notificación: " + notificacionId
                        + ", Usuario: " + usuarioId + ", Estado: " + estado + ", EsLeída: " + isRead);

                return isRead;
            } else {
                System.err.println("❌ No se encontró la notificación en verificarEstadoLectura");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error SQL verificando estado de lectura: " + e.getMessage());
            return false;
        }
    }

    /**
     * MÉTODO MEJORADO: Obtiene información detallada de una notificación para
     * debugging
     *
     * @param notificacionId
     * @param usuarioId
     * @return
     */
    public String getNotificationDebugInfo(int notificacionId, int usuarioId) {
        try {
            String query = """
            SELECT n.id, n.titulo, n.estado as notif_estado, 
                   nd.estado_lectura, nd.fecha_leida,
                   CONCAT(u.nombre, ' ', u.apellido) as remitente
            FROM notificaciones n
            INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
            LEFT JOIN usuarios u ON n.remitente_id = u.id
            WHERE n.id = ? AND nd.destinatario_id = ?
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, notificacionId);
            ps.setInt(2, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return String.format(
                        "Notificación Debug Info:\n"
                        + "  ID: %d\n"
                        + "  Título: %s\n"
                        + "  Estado Notificación: %s\n"
                        + "  Estado Lectura: %s\n"
                        + "  Fecha Leída: %s\n"
                        + "  Remitente: %s",
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("notif_estado"),
                        rs.getString("estado_lectura"),
                        rs.getTimestamp("fecha_leida"),
                        rs.getString("remitente")
                );
            } else {
                return "Notificación no encontrada: ID=" + notificacionId + ", Usuario=" + usuarioId;
            }

        } catch (SQLException e) {
            return "Error obteniendo debug info: " + e.getMessage();
        }
    }

}
