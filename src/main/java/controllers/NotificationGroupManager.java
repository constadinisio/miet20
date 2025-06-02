package main.java.controllers;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.models.NotificationGroup;

/**
 * Controlador para la gestión de grupos de notificaciones personalizados.
 * Maneja la creación, modificación, eliminación y consulta de grupos, así como
 * la gestión de sus miembros.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class NotificationGroupManager {

    private static NotificationGroupManager instance;
    private final Connection connection;
    private final ExecutorService executorService;
    private final List<GroupChangeListener> listeners;

    // Cache para optimizar consultas frecuentes
    private final Map<Integer, List<NotificationGroup>> userGroupsCache;
    private final Map<Integer, NotificationGroup> groupCache;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 300000; // 5 minutos

    /**
     * Constructor privado para implementar patrón Singleton
     */
    private NotificationGroupManager() {
        this.connection = Conexion.getInstancia().verificarConexion();
        this.executorService = Executors.newFixedThreadPool(3);
        this.listeners = new ArrayList<>();
        this.userGroupsCache = new HashMap<>();
        this.groupCache = new HashMap<>();

        System.out.println("✅ NotificationGroupManager inicializado");
    }

    /**
     * Obtiene la instancia única del manager (Singleton)
     */
    public static synchronized NotificationGroupManager getInstance() {
        if (instance == null) {
            instance = new NotificationGroupManager();
        }
        return instance;
    }

    // ========================================
    // MÉTODOS PRINCIPALES DE GESTIÓN DE GRUPOS
    // ========================================
    /**
     * Crea un nuevo grupo de notificaciones
     *
     * @param nombre Nombre del grupo
     * @param descripcion Descripción del grupo
     * @param creadorId ID del usuario creador
     * @param miembros Lista de IDs de usuarios miembros
     * @return CompletableFuture con el ID del grupo creado, o -1 si hay error
     */
    public CompletableFuture<Integer> crearGrupo(String nombre, String descripcion,
            int creadorId, List<Integer> miembros) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📝 Creando grupo: " + nombre + " por usuario: " + creadorId);

                // Validar datos de entrada
                if (!validarDatosGrupo(nombre, descripcion, creadorId)) {
                    return -1;
                }

                // Verificar permisos del usuario
                if (!tienePermisosCrearGrupos(creadorId)) {
                    System.err.println("❌ Usuario sin permisos para crear grupos: " + creadorId);
                    return -1;
                }

                // Verificar nombre único para el usuario
                if (existeNombreGrupo(nombre, creadorId, -1)) {
                    System.err.println("❌ Ya existe un grupo con nombre: " + nombre);
                    return -1;
                }

                connection.setAutoCommit(false);

                try {
                    // Insertar grupo principal
                    String insertGroupQuery = """
                        INSERT INTO grupos_notificacion_personalizados 
                        (nombre, descripcion, creador_id, fecha_creacion, activo) 
                        VALUES (?, ?, ?, NOW(), 1)
                        """;

                    PreparedStatement psGroup = connection.prepareStatement(
                            insertGroupQuery, Statement.RETURN_GENERATED_KEYS);
                    psGroup.setString(1, nombre);
                    psGroup.setString(2, descripcion);
                    psGroup.setInt(3, creadorId);

                    psGroup.executeUpdate();
                    ResultSet keys = psGroup.getGeneratedKeys();

                    int groupId = -1;
                    if (keys.next()) {
                        groupId = keys.getInt(1);
                    }

                    // Agregar miembros si se proporcionaron
                    if (groupId > 0 && miembros != null && !miembros.isEmpty()) {
                        agregarMiembrosGrupo(groupId, miembros);
                    }

                    connection.commit();

                    // Invalidar cache
                    invalidarCache();

                    // Notificar listeners
                    notificarCambioGrupo(GroupChangeType.CREATED, groupId, creadorId);

                    System.out.println("✅ Grupo creado exitosamente. ID: " + groupId);
                    return groupId;

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }

            } catch (Exception e) {
                System.err.println("❌ Error creando grupo: " + e.getMessage());
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
     * Obtiene todos los grupos de un usuario
     *
     * @param usuarioId ID del usuario
     * @return CompletableFuture con la lista de grupos
     */
    public CompletableFuture<List<NotificationGroup>> obtenerGruposUsuario(int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verificar cache primero
                if (isCacheValido() && userGroupsCache.containsKey(usuarioId)) {
                    System.out.println("📋 Grupos obtenidos desde cache para usuario: " + usuarioId);
                    return new ArrayList<>(userGroupsCache.get(usuarioId));
                }

                System.out.println("📋 Cargando grupos para usuario: " + usuarioId);

                String query = """
                    SELECT ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                           ng.fecha_creacion, ng.fecha_modificacion,
                           COUNT(mgp.usuario_id) as cantidad_miembros
                    FROM grupos_notificacion_personalizados ng
                    LEFT JOIN miembros_grupos_personalizados mgp ON ng.id = mgp.grupo_id 
                        AND mgp.activo = 1
                    WHERE ng.creador_id = ? AND ng.activo = 1
                    GROUP BY ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                             ng.fecha_creacion, ng.fecha_modificacion
                    ORDER BY ng.fecha_modificacion DESC, ng.nombre
                    """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, usuarioId);
                ResultSet rs = ps.executeQuery();

                List<NotificationGroup> grupos = new ArrayList<>();

                while (rs.next()) {
                    NotificationGroup grupo = new NotificationGroup();
                    grupo.setId(rs.getInt("id"));
                    grupo.setNombre(rs.getString("nombre"));
                    grupo.setDescripcion(rs.getString("descripcion"));
                    grupo.setCreadorId(rs.getInt("creador_id"));
                    grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());

                    Timestamp fechaMod = rs.getTimestamp("fecha_modificacion");
                    if (fechaMod != null) {
                        grupo.setFechaModificacion(fechaMod.toLocalDateTime());
                    }

                    // Cargar miembros del grupo
                    List<Integer> miembros = obtenerMiembrosGrupoSync(grupo.getId());
                    grupo.setMiembros(miembros);

                    grupos.add(grupo);

                    // Actualizar cache individual
                    groupCache.put(grupo.getId(), grupo);
                }

                // Actualizar cache de usuario
                userGroupsCache.put(usuarioId, new ArrayList<>(grupos));
                lastCacheUpdate = System.currentTimeMillis();

                System.out.println("✅ Grupos cargados: " + grupos.size());
                return grupos;

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo grupos: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Obtiene un grupo específico por ID
     *
     * @param groupId ID del grupo
     * @return CompletableFuture con el grupo encontrado o null
     */
    public CompletableFuture<NotificationGroup> obtenerGrupoPorId(int groupId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verificar cache primero
                if (isCacheValido() && groupCache.containsKey(groupId)) {
                    System.out.println("🔍 Grupo obtenido desde cache: " + groupId);
                    return groupCache.get(groupId).copy(); // Retorna copia defensiva
                }

                System.out.println("🔍 Cargando grupo por ID: " + groupId);

                String query = """
                    SELECT ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                           ng.fecha_creacion, ng.fecha_modificacion
                    FROM grupos_notificacion_personalizados ng
                    WHERE ng.id = ? AND ng.activo = 1
                    """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    NotificationGroup grupo = new NotificationGroup();
                    grupo.setId(rs.getInt("id"));
                    grupo.setNombre(rs.getString("nombre"));
                    grupo.setDescripcion(rs.getString("descripcion"));
                    grupo.setCreadorId(rs.getInt("creador_id"));
                    grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());

                    Timestamp fechaMod = rs.getTimestamp("fecha_modificacion");
                    if (fechaMod != null) {
                        grupo.setFechaModificacion(fechaMod.toLocalDateTime());
                    }

                    // Cargar miembros
                    List<Integer> miembros = obtenerMiembrosGrupoSync(grupo.getId());
                    grupo.setMiembros(miembros);

                    // Actualizar cache
                    groupCache.put(groupId, grupo);

                    System.out.println("✅ Grupo encontrado: " + grupo.getNombre());
                    return grupo;
                }

                System.out.println("⚠️ Grupo no encontrado: " + groupId);
                return null;

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo grupo: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }, executorService);
    }

    /**
     * Actualiza un grupo existente
     *
     * @param groupId ID del grupo a actualizar
     * @param nombre Nuevo nombre
     * @param descripcion Nueva descripción
     * @param miembros Nueva lista de miembros
     * @param usuarioId ID del usuario que hace la modificación
     * @return CompletableFuture con true si se actualizó correctamente
     */
    public CompletableFuture<Boolean> actualizarGrupo(int groupId, String nombre,
            String descripcion, List<Integer> miembros,
            int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🔄 Actualizando grupo: " + groupId);

                // Verificar permisos
                if (!esCreadorDelGrupo(groupId, usuarioId)) {
                    System.err.println("❌ Usuario sin permisos para modificar grupo: " + usuarioId);
                    return false;
                }

                // Validar datos
                if (!validarDatosGrupo(nombre, descripcion, usuarioId)) {
                    return false;
                }

                // Verificar nombre único (excluyendo el grupo actual)
                if (existeNombreGrupo(nombre, usuarioId, groupId)) {
                    System.err.println("❌ Nombre ya existe: " + nombre);
                    return false;
                }

                connection.setAutoCommit(false);

                try {
                    // Actualizar información básica del grupo
                    String updateQuery = """
                        UPDATE grupos_notificacion_personalizados 
                        SET nombre = ?, descripcion = ?, fecha_modificacion = NOW()
                        WHERE id = ? AND creador_id = ? AND activo = 1
                        """;

                    PreparedStatement psUpdate = connection.prepareStatement(updateQuery);
                    psUpdate.setString(1, nombre);
                    psUpdate.setString(2, descripcion);
                    psUpdate.setInt(3, groupId);
                    psUpdate.setInt(4, usuarioId);

                    int rowsUpdated = psUpdate.executeUpdate();

                    if (rowsUpdated > 0) {
                        // Actualizar miembros si se proporcionaron
                        if (miembros != null) {
                            // Desactivar miembros actuales
                            desactivarMiembrosGrupo(groupId);

                            // Agregar nuevos miembros
                            if (!miembros.isEmpty()) {
                                agregarMiembrosGrupo(groupId, miembros);
                            }
                        }

                        connection.commit();

                        // Invalidar cache
                        invalidarCache();

                        // Notificar listeners
                        notificarCambioGrupo(GroupChangeType.UPDATED, groupId, usuarioId);

                        System.out.println("✅ Grupo actualizado exitosamente");
                        return true;
                    } else {
                        connection.rollback();
                        System.err.println("❌ No se pudo actualizar el grupo");
                        return false;
                    }

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }

            } catch (Exception e) {
                System.err.println("❌ Error actualizando grupo: " + e.getMessage());
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
     * Elimina un grupo (marca como inactivo)
     *
     * @param groupId ID del grupo a eliminar
     * @param usuarioId ID del usuario que solicita la eliminación
     * @return CompletableFuture con true si se eliminó correctamente
     */
    public CompletableFuture<Boolean> eliminarGrupo(int groupId, int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🗑️ Eliminando grupo: " + groupId);

                // Verificar permisos
                if (!esCreadorDelGrupo(groupId, usuarioId)) {
                    System.err.println("❌ Usuario sin permisos para eliminar grupo: " + usuarioId);
                    return false;
                }

                String deleteQuery = """
                    UPDATE grupos_notificacion_personalizados 
                    SET activo = 0, fecha_eliminacion = NOW()
                    WHERE id = ? AND creador_id = ? AND activo = 1
                    """;

                PreparedStatement ps = connection.prepareStatement(deleteQuery);
                ps.setInt(1, groupId);
                ps.setInt(2, usuarioId);

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated > 0) {
                    // También desactivar miembros del grupo
                    desactivarMiembrosGrupo(groupId);

                    // Invalidar cache
                    invalidarCache();

                    // Notificar listeners
                    notificarCambioGrupo(GroupChangeType.DELETED, groupId, usuarioId);

                    System.out.println("✅ Grupo eliminado exitosamente");
                    return true;
                } else {
                    System.err.println("❌ No se pudo eliminar el grupo");
                    return false;
                }

            } catch (SQLException e) {
                System.err.println("❌ Error eliminando grupo: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }

    // ========================================
    // MÉTODOS DE BÚSQUEDA Y CONSULTA
    // ========================================
    /**
     * Busca grupos por nombre
     *
     * @param nombreBusqueda Término de búsqueda
     * @param usuarioId ID del usuario (para buscar solo en sus grupos)
     * @return CompletableFuture con lista de grupos encontrados
     */
    public CompletableFuture<List<NotificationGroup>> buscarGruposPorNombre(
            String nombreBusqueda, int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🔍 Buscando grupos: '" + nombreBusqueda + "' para usuario: " + usuarioId);

                // CORREGIDO: Cambié las referencias de tabla incorrectas
                String query = """
                SELECT ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                       ng.fecha_creacion, ng.fecha_modificacion,
                       COUNT(mgp.usuario_id) as cantidad_miembros
                FROM grupos_notificacion_personalizados ng
                LEFT JOIN miembros_grupos_personalizados mgp ON ng.id = mgp.grupo_id 
                    AND mgp.activo = 1
                WHERE ng.creador_id = ? AND ng.activo = 1 
                AND (ng.nombre LIKE ? OR ng.descripcion LIKE ?)
                GROUP BY ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                         ng.fecha_creacion, ng.fecha_modificacion
                ORDER BY ng.nombre
                """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, usuarioId);
                String searchPattern = "%" + nombreBusqueda + "%";
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);

                ResultSet rs = ps.executeQuery();

                List<NotificationGroup> grupos = new ArrayList<>();

                while (rs.next()) {
                    NotificationGroup grupo = new NotificationGroup();
                    grupo.setId(rs.getInt("id"));
                    grupo.setNombre(rs.getString("nombre"));
                    grupo.setDescripcion(rs.getString("descripcion"));
                    grupo.setCreadorId(rs.getInt("creador_id"));
                    grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());

                    Timestamp fechaMod = rs.getTimestamp("fecha_modificacion");
                    if (fechaMod != null) {
                        grupo.setFechaModificacion(fechaMod.toLocalDateTime());
                    }

                    // Cargar miembros
                    List<Integer> miembros = obtenerMiembrosGrupoSync(grupo.getId());
                    grupo.setMiembros(miembros);

                    grupos.add(grupo);
                }

                System.out.println("✅ Grupos encontrados: " + grupos.size());
                return grupos;

            } catch (SQLException e) {
                System.err.println("❌ Error buscando grupos: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }
    // ========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================

    /**
     * Obtiene los miembros de un grupo de forma síncrona
     */
    private List<Integer> obtenerMiembrosGrupoSync(int groupId) {
        List<Integer> miembros = new ArrayList<>();

        try {
            String query = """
                SELECT mgp.usuario_id 
                FROM miembros_grupos_personalizados mgp
                INNER JOIN usuarios u ON mgp.usuario_id = u.id
                WHERE mgp.grupo_id = ? AND mgp.activo = 1 AND u.status = 1
                ORDER BY u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                miembros.add(rs.getInt("usuario_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo miembros del grupo: " + e.getMessage());
        }

        return miembros;
    }

    /**
     * Agrega múltiples miembros a un grupo
     */
    private void agregarMiembrosGrupo(int groupId, List<Integer> miembros) throws SQLException {
        String insertQuery = """
            INSERT INTO miembros_grupos_personalizados (grupo_id, usuario_id, fecha_agregado, activo) 
            VALUES (?, ?, NOW(), 1)
            ON DUPLICATE KEY UPDATE activo = 1, fecha_agregado = NOW()
            """;

        PreparedStatement ps = connection.prepareStatement(insertQuery);

        int miembrosValidos = 0;
        for (Integer miembroId : miembros) {
            if (existeUsuarioActivo(miembroId)) {
                ps.setInt(1, groupId);
                ps.setInt(2, miembroId);
                ps.addBatch();
                miembrosValidos++;
            }
        }

        if (miembrosValidos > 0) {
            ps.executeBatch();
            System.out.println("✅ Miembros agregados al grupo " + groupId + ": " + miembrosValidos);
        }
    }

    /**
     * Desactiva todos los miembros de un grupo
     */
    private void desactivarMiembrosGrupo(int groupId) throws SQLException {
        String deleteQuery = "UPDATE miembros_grupos_personalizados SET activo = 0 WHERE grupo_id = ?";
        PreparedStatement ps = connection.prepareStatement(deleteQuery);
        ps.setInt(1, groupId);
        ps.executeUpdate();
    }

    /**
     * Valida los datos básicos de un grupo
     */
    private boolean validarDatosGrupo(String nombre, String descripcion, int creadorId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("❌ Nombre del grupo no puede estar vacío");
            return false;
        }

        if (nombre.length() > 100) {
            System.err.println("❌ Nombre demasiado largo (máximo 100 caracteres)");
            return false;
        }

        if (descripcion != null && descripcion.length() > 500) {
            System.err.println("❌ Descripción demasiado larga (máximo 500 caracteres)");
            return false;
        }

        if (creadorId <= 0) {
            System.err.println("❌ ID de creador inválido");
            return false;
        }

        return true;
    }

    /**
     * Verifica si un usuario tiene permisos para crear grupos
     */
    private boolean tienePermisosCrearGrupos(int usuarioId) {
        try {
            // CORREGIDO: Adaptado a la estructura real de la BD con usuario_roles
            String query = """
            SELECT ur.rol_id 
            FROM usuarios u
            INNER JOIN usuario_roles ur ON u.id = ur.usuario_id
            WHERE u.id = ? AND u.status = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            // Roles autorizados: admin, preceptor, profesor, ATTP (1, 2, 3, 5)
            List<Integer> rolesAutorizados = Arrays.asList(1, 2, 3, 5);

            while (rs.next()) {
                int rolId = rs.getInt("rol_id");
                if (rolesAutorizados.contains(rolId)) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error verificando permisos: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe un grupo con el mismo nombre para el usuario
     */
    private boolean existeNombreGrupo(String nombre, int creadorId, int excludeGroupId) {
        try {
            String query = """
                SELECT COUNT(*) FROM grupos_notificacion_personalizados 
                WHERE nombre = ? AND creador_id = ? AND activo = 1
                """;

            // Si se proporciona un ID para excluir (en caso de actualización)
            if (excludeGroupId > 0) {
                query += " AND id != ?";
            }

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nombre);
            ps.setInt(2, creadorId);

            if (excludeGroupId > 0) {
                ps.setInt(3, excludeGroupId);
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando nombre duplicado: " + e.getMessage());
        }

        return false;
    }

    /**
     * Verifica si el usuario es creador del grupo
     */
    private boolean esCreadorDelGrupo(int groupId, int usuarioId) {
        try {
            String query = """
                SELECT COUNT(*) FROM grupos_notificacion_personalizados 
                WHERE id = ? AND creador_id = ? AND activo = 1
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ps.setInt(2, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando creador: " + e.getMessage());
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
            System.err.println("Error verificando usuario: " + e.getMessage());
        }

        return false;
    }

    // ========================================
    // MÉTODOS DE GESTIÓN DE CACHE
    // ========================================
    /**
     * Verifica si el cache es válido
     */
    private boolean isCacheValido() {
        return (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION;
    }

    /**
     * Invalida todo el cache
     */
    private void invalidarCache() {
        userGroupsCache.clear();
        groupCache.clear();
        lastCacheUpdate = 0;
        System.out.println("🔄 Cache invalidado");
    }

    /**
     * Invalida el cache para un usuario específico
     */
    private void invalidarCacheUsuario(int usuarioId) {
        userGroupsCache.remove(usuarioId);
        System.out.println("🔄 Cache invalidado para usuario: " + usuarioId);
    }

// ========================================
    // SISTEMA DE LISTENERS
    // ========================================
    /**
     * Interfaz para escuchar cambios en grupos
     */
    public interface GroupChangeListener {

        void onGroupChanged(GroupChangeType type, int groupId, int userId);
    }

    /**
     * Tipos de cambios en grupos
     */
    public enum GroupChangeType {
        CREATED, UPDATED, DELETED, MEMBER_ADDED, MEMBER_REMOVED
    }

    /**
     * Agrega un listener para cambios en grupos
     */
    public void agregarListener(GroupChangeListener listener) {
        listeners.add(listener);
        System.out.println("👂 Listener agregado. Total: " + listeners.size());
    }

    /**
     * Remueve un listener
     */
    public void removerListener(GroupChangeListener listener) {
        listeners.remove(listener);
        System.out.println("👂 Listener removido. Total: " + listeners.size());
    }

    /**
     * Notifica a todos los listeners sobre un cambio en grupo
     */
    private void notificarCambioGrupo(GroupChangeType type, int groupId, int userId) {
        SwingUtilities.invokeLater(() -> {
            for (GroupChangeListener listener : listeners) {
                try {
                    listener.onGroupChanged(type, groupId, userId);
                } catch (Exception e) {
                    System.err.println("Error notificando listener: " + e.getMessage());
                }
            }
        });
    }

    // ========================================
    // MÉTODOS DE GESTIÓN DE MIEMBROS ESPECÍFICOS
    // ========================================
    /**
     * Agrega un miembro específico a un grupo
     *
     * @param groupId ID del grupo
     * @param usuarioId ID del usuario a agregar
     * @param solicitanteId ID del usuario que hace la solicitud
     * @return CompletableFuture con true si se agregó correctamente
     */
    public CompletableFuture<Boolean> agregarMiembro(int groupId, int usuarioId, int solicitanteId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("👤 Agregando miembro " + usuarioId + " al grupo " + groupId);

                // Verificar permisos
                if (!esCreadorDelGrupo(groupId, solicitanteId)) {
                    System.err.println("❌ Sin permisos para agregar miembro");
                    return false;
                }

                // Verificar que el usuario existe y está activo
                if (!existeUsuarioActivo(usuarioId)) {
                    System.err.println("❌ Usuario no existe o no está activo: " + usuarioId);
                    return false;
                }

                // CORREGIDO: Query completamente equivocado - era un SELECT en lugar de INSERT
                String insertQuery = """
                INSERT INTO miembros_grupos_personalizados (grupo_id, usuario_id, fecha_agregado, activo) 
                VALUES (?, ?, NOW(), 1)
                ON DUPLICATE KEY UPDATE activo = 1, fecha_agregado = NOW()
                """;

                PreparedStatement ps = connection.prepareStatement(insertQuery);
                ps.setInt(1, groupId);
                ps.setInt(2, usuarioId);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    // Invalidar cache
                    invalidarCacheUsuario(solicitanteId);
                    groupCache.remove(groupId);

                    // Notificar listeners
                    notificarCambioGrupo(GroupChangeType.MEMBER_ADDED, groupId, solicitanteId);

                    System.out.println("✅ Miembro agregado exitosamente");
                    return true;
                }

                return false;

            } catch (SQLException e) {
                System.err.println("❌ Error agregando miembro: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }

    /**
     * Remueve un miembro específico de un grupo
     *
     * @param groupId ID del grupo
     * @param usuarioId ID del usuario a remover
     * @param solicitanteId ID del usuario que hace la solicitud
     * @return CompletableFuture con true si se removió correctamente
     */
    public CompletableFuture<Boolean> removerMiembro(int groupId, int usuarioId, int solicitanteId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("👤 Removiendo miembro " + usuarioId + " del grupo " + groupId);

                // Verificar permisos
                if (!esCreadorDelGrupo(groupId, solicitanteId)) {
                    System.err.println("❌ Sin permisos para remover miembro");
                    return false;
                }

                String updateQuery = """
                    UPDATE miembros_grupos_personalizados 
                    SET activo = 0
                    WHERE grupo_id = ? AND usuario_id = ? AND activo = 1
                    """;

                PreparedStatement ps = connection.prepareStatement(updateQuery);
                ps.setInt(1, groupId);
                ps.setInt(2, usuarioId);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    // Invalidar cache
                    invalidarCacheUsuario(solicitanteId);
                    groupCache.remove(groupId);

                    // Notificar listeners
                    notificarCambioGrupo(GroupChangeType.MEMBER_REMOVED, groupId, solicitanteId);

                    System.out.println("✅ Miembro removido exitosamente");
                    return true;
                }

                return false;

            } catch (SQLException e) {
                System.err.println("❌ Error removiendo miembro: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }

    /**
     * Obtiene información detallada de los miembros de un grupo
     *
     * @param groupId ID del grupo
     * @return CompletableFuture con lista de información básica de usuarios
     */
    public CompletableFuture<List<Map<String, Object>>> obtenerMiembrosDetallados(int groupId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("👥 Obteniendo miembros detallados del grupo: " + groupId);

                // CORREGIDO: Agregué JOINs para obtener el nombre del rol correctamente
                String query = """
                SELECT u.id, u.nombre, u.apellido, u.mail as email, 
                       ur.rol_id, r.nombre as rol_nombre, u.anio, u.division,
                       mgp.fecha_agregado
                FROM miembros_grupos_personalizados mgp
                INNER JOIN usuarios u ON mgp.usuario_id = u.id
                LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
                LEFT JOIN roles r ON ur.rol_id = r.id
                WHERE mgp.grupo_id = ? AND mgp.activo = 1 AND u.status = 1
                ORDER BY u.apellido, u.nombre
                """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();

                List<Map<String, Object>> miembros = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("id", rs.getInt("id"));
                    usuario.put("nombre", rs.getString("nombre"));
                    usuario.put("apellido", rs.getString("apellido"));
                    usuario.put("email", rs.getString("email"));
                    usuario.put("rol_id", rs.getInt("rol_id"));
                    usuario.put("rol_nombre", rs.getString("rol_nombre") != null ? rs.getString("rol_nombre") : "Sin rol");
                    usuario.put("anio", rs.getString("anio")); // Es varchar en tu BD
                    usuario.put("division", rs.getString("division"));
                    usuario.put("fecha_agregado", rs.getTimestamp("fecha_agregado"));

                    // Crear nombre completo
                    String nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                    usuario.put("nombre_completo", nombreCompleto);

                    miembros.add(usuario);
                }

                System.out.println("✅ Miembros detallados obtenidos: " + miembros.size());
                return miembros;

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo miembros detallados: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }

// 3. AGREGAR este método alternativo más simple:
    /**
     * Obtiene nombres de los miembros de un grupo (alternativa simple)
     *
     * @param groupId ID del grupo
     * @return CompletableFuture con lista de nombres de miembros
     */
    public CompletableFuture<List<String>> obtenerNombresMiembros(int groupId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("👥 Obteniendo nombres de miembros del grupo: " + groupId);

                // CORREGIDO: Agregué JOINs para obtener el nombre del rol correctamente
                String query = """
                SELECT CONCAT(u.apellido, ', ', u.nombre) as nombre_completo,
                       r.nombre as rol_nombre
                FROM miembros_grupos_personalizados mgp
                INNER JOIN usuarios u ON mgp.usuario_id = u.id
                LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
                LEFT JOIN roles r ON ur.rol_id = r.id
                WHERE mgp.grupo_id = ? AND mgp.activo = 1 AND u.status = 1
                ORDER BY u.apellido, u.nombre
                """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();

                List<String> miembros = new ArrayList<>();

                while (rs.next()) {
                    String nombreCompleto = rs.getString("nombre_completo");
                    String rolNombre = rs.getString("rol_nombre");

                    // Si no tiene rol, mostrar "Sin rol"
                    if (rolNombre == null || rolNombre.trim().isEmpty()) {
                        rolNombre = "Sin rol";
                    }

                    miembros.add(nombreCompleto + " (" + rolNombre + ")");
                }

                System.out.println("✅ Nombres de miembros obtenidos: " + miembros.size());
                return miembros;

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo nombres de miembros: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }

// 4. AGREGAR método para obtener información básica de un usuario:
    /**
     * Obtiene información básica de un usuario
     *
     * @param usuarioId ID del usuario
     * @return Map con información del usuario o null si no existe
     */
    public Map<String, Object> obtenerInfoUsuario(int usuarioId) {
        try {
            // CORREGIDO: Agregué JOINs para obtener el nombre del rol correctamente
            String query = """
            SELECT u.id, u.nombre, u.apellido, u.mail as email, 
                   ur.rol_id, r.nombre as rol_nombre, u.anio, u.division
            FROM usuarios u
            LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
            LEFT JOIN roles r ON ur.rol_id = r.id
            WHERE u.id = ? AND u.status = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("id", rs.getInt("id"));
                usuario.put("nombre", rs.getString("nombre"));
                usuario.put("apellido", rs.getString("apellido"));
                usuario.put("email", rs.getString("email"));
                usuario.put("rol_id", rs.getInt("rol_id"));
                usuario.put("rol_nombre", rs.getString("rol_nombre") != null ? rs.getString("rol_nombre") : "Sin rol");
                usuario.put("anio", rs.getString("anio")); // Es varchar en tu BD
                usuario.put("division", rs.getString("division"));

                String nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                usuario.put("nombre_completo", nombreCompleto);

                return usuario;
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo info de usuario: " + e.getMessage());
        }

        return null;
    }

    // ========================================
    // MÉTODOS DE ESTADÍSTICAS Y UTILIDADES
    // ========================================
    /**
     * Obtiene estadísticas de grupos para un usuario
     *
     * @param usuarioId ID del usuario
     * @return CompletableFuture con estadísticas
     */
    public CompletableFuture<EstadisticasGrupo> obtenerEstadisticas(int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EstadisticasGrupo stats = new EstadisticasGrupo();

                // CORREGIDO: Agregué COALESCE para manejar valores NULL
                String queryTotal = """
                SELECT COUNT(*) as total_grupos,
                       COALESCE(AVG(miembros_count.cantidad), 0) as promedio_miembros
                FROM grupos_notificacion_personalizados ng
                LEFT JOIN (
                    SELECT grupo_id, COUNT(*) as cantidad 
                    FROM miembros_grupos_personalizados 
                    WHERE activo = 1 
                    GROUP BY grupo_id
                ) miembros_count ON ng.id = miembros_count.grupo_id
                WHERE ng.creador_id = ? AND ng.activo = 1
                """;

                PreparedStatement psTotal = connection.prepareStatement(queryTotal);
                psTotal.setInt(1, usuarioId);
                ResultSet rsTotal = psTotal.executeQuery();

                if (rsTotal.next()) {
                    stats.totalGrupos = rsTotal.getInt("total_grupos");
                    stats.promedioMiembros = rsTotal.getDouble("promedio_miembros");
                }

                // Total de miembros únicos
                String queryMiembros = """
                SELECT COUNT(DISTINCT mgp.usuario_id) as miembros_unicos
                FROM grupos_notificacion_personalizados ng
                INNER JOIN miembros_grupos_personalizados mgp ON ng.id = mgp.grupo_id
                WHERE ng.creador_id = ? AND ng.activo = 1 AND mgp.activo = 1
                """;

                PreparedStatement psMiembros = connection.prepareStatement(queryMiembros);
                psMiembros.setInt(1, usuarioId);
                ResultSet rsMiembros = psMiembros.executeQuery();

                if (rsMiembros.next()) {
                    stats.miembrosUnicos = rsMiembros.getInt("miembros_unicos");
                }

                return stats;

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo estadísticas: " + e.getMessage());
                e.printStackTrace();
                return new EstadisticasGrupo();
            }
        }, executorService);
    }

    /**
     * Duplica un grupo existente
     *
     * @param groupId ID del grupo a duplicar
     * @param nuevoNombre Nombre para el grupo duplicado
     * @param usuarioId ID del usuario que solicita la duplicación
     * @return CompletableFuture con el ID del nuevo grupo o -1 si hay error
     */
    public CompletableFuture<Integer> duplicarGrupo(int groupId, String nuevoNombre, int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📋 Duplicando grupo: " + groupId + " con nombre: " + nuevoNombre);

                // Verificar permisos
                if (!esCreadorDelGrupo(groupId, usuarioId)) {
                    System.err.println("❌ Sin permisos para duplicar grupo");
                    return -1;
                }

                // Obtener datos del grupo original
                NotificationGroup grupoOriginal = obtenerGrupoPorId(groupId).get();
                if (grupoOriginal == null) {
                    System.err.println("❌ Grupo original no encontrado");
                    return -1;
                }

                // Crear descripción para la copia
                String nuevaDescripcion = "Copia de: " + grupoOriginal.getDescripcion();

                // Crear el nuevo grupo
                return crearGrupo(nuevoNombre, nuevaDescripcion, usuarioId,
                        grupoOriginal.getMiembros()).get();

            } catch (Exception e) {
                System.err.println("❌ Error duplicando grupo: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }, executorService);
    }

    // ========================================
    // CLASE INTERNA PARA ESTADÍSTICAS
    // ========================================
    /**
     * Clase para almacenar estadísticas de grupos
     */
    public static class EstadisticasGrupo {

        public int totalGrupos = 0;
        public double promedioMiembros = 0.0;
        public int miembrosUnicos = 0;

        @Override
        public String toString() {
            return String.format("Grupos: %d, Promedio miembros: %.1f, Miembros únicos: %d",
                    totalGrupos, promedioMiembros, miembrosUnicos);
        }
    }

    // ========================================
    // MÉTODOS DE LIMPIEZA Y CIERRE
    // ========================================
    /**
     * Limpia grupos inactivos antiguos (mantenimiento)
     *
     * @param diasAntiguo Número de días para considerar un grupo como antiguo
     * @return CompletableFuture con el número de grupos eliminados
     */
    public CompletableFuture<Integer> limpiarGruposAntiguos(int diasAntiguo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🧹 Limpiando grupos inactivos antiguos (>" + diasAntiguo + " días)");

                String deleteQuery = """
                    DELETE FROM grupos_notificacion_personalizados 
                    WHERE activo = 0 
                    AND fecha_eliminacion IS NOT NULL 
                    AND fecha_eliminacion < DATE_SUB(NOW(), INTERVAL ? DAY)
                    """;

                PreparedStatement ps = connection.prepareStatement(deleteQuery);
                ps.setInt(1, diasAntiguo);

                int gruposEliminados = ps.executeUpdate();

                if (gruposEliminados > 0) {
                    System.out.println("✅ Grupos antiguos eliminados: " + gruposEliminados);
                    invalidarCache();
                }

                return gruposEliminados;

            } catch (SQLException e) {
                System.err.println("❌ Error limpiando grupos antiguos: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }, executorService);
    }

    /**
     * Cierra el manager y libera recursos
     */
    public void shutdown() {
        try {
            System.out.println("🔄 Cerrando NotificationGroupManager...");

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                System.out.println("✅ ExecutorService cerrado");
            }

            if (listeners != null) {
                listeners.clear();
                System.out.println("✅ Listeners limpiados");
            }

            invalidarCache();

            System.out.println("✅ NotificationGroupManager cerrado correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error cerrando NotificationGroupManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para resetear la instancia singleton (útil para testing)
     */
    public static void resetInstance() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
            System.out.println("🔄 NotificationGroupManager instancia reseteada");
        }
    }

    /**
     * Obtiene información de debug del manager
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== NotificationGroupManager Debug ===\n");
        info.append("Cache válido: ").append(isCacheValido()).append("\n");
        info.append("Grupos en cache: ").append(groupCache.size()).append("\n");
        info.append("Usuarios en cache: ").append(userGroupsCache.size()).append("\n");
        info.append("Listeners activos: ").append(listeners.size()).append("\n");
        info.append("ExecutorService activo: ").append(!executorService.isShutdown()).append("\n");
        info.append("=======================================");
        return info.toString();
    }

    /**
     * NUEVO: Verifica si un usuario pertenece a un grupo específico
     */
    public boolean usuarioPerteneceAGrupo(int usuarioId, int groupId) {
        try {
            String query = """
            SELECT COUNT(*) 
            FROM miembros_grupos_personalizados mgp
            INNER JOIN usuarios u ON mgp.usuario_id = u.id
            WHERE mgp.grupo_id = ? AND mgp.usuario_id = ? 
            AND mgp.activo = 1 AND u.status = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ps.setInt(2, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando pertenencia a grupo: " + e.getMessage());
        }

        return false;
    }

    /**
     * NUEVO: Obtiene el número total de miembros activos de un grupo
     */
    public int contarMiembrosGrupo(int groupId) {
        try {
            String query = """
            SELECT COUNT(*) 
            FROM miembros_grupos_personalizados mgp
            INNER JOIN usuarios u ON mgp.usuario_id = u.id
            WHERE mgp.grupo_id = ? AND mgp.activo = 1 AND u.status = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error contando miembros del grupo: " + e.getMessage());
        }

        return 0;
    }

    /**
     * NUEVO: Obtiene grupos donde un usuario es miembro (no creador)
     */
    public CompletableFuture<List<NotificationGroup>> obtenerGruposComomiembro(int usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("👤 Obteniendo grupos donde " + usuarioId + " es miembro");

                String query = """
                SELECT DISTINCT ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                       ng.fecha_creacion, ng.fecha_modificacion,
                       COUNT(mgp2.usuario_id) as cantidad_miembros
                FROM grupos_notificacion_personalizados ng
                INNER JOIN miembros_grupos_personalizados mgp ON ng.id = mgp.grupo_id
                LEFT JOIN miembros_grupos_personalizados mgp2 ON ng.id = mgp2.grupo_id AND mgp2.activo = 1
                WHERE mgp.usuario_id = ? AND mgp.activo = 1 
                AND ng.activo = 1 AND ng.creador_id != ?
                GROUP BY ng.id, ng.nombre, ng.descripcion, ng.creador_id, 
                         ng.fecha_creacion, ng.fecha_modificacion
                ORDER BY ng.nombre
                """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, usuarioId);
                ps.setInt(2, usuarioId); // Excluir grupos donde es creador

                ResultSet rs = ps.executeQuery();

                List<NotificationGroup> grupos = new ArrayList<>();

                while (rs.next()) {
                    NotificationGroup grupo = new NotificationGroup();
                    grupo.setId(rs.getInt("id"));
                    grupo.setNombre(rs.getString("nombre"));
                    grupo.setDescripcion(rs.getString("descripcion"));
                    grupo.setCreadorId(rs.getInt("creador_id"));
                    grupo.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());

                    Timestamp fechaMod = rs.getTimestamp("fecha_modificacion");
                    if (fechaMod != null) {
                        grupo.setFechaModificacion(fechaMod.toLocalDateTime());
                    }

                    // Cargar miembros
                    List<Integer> miembros = obtenerMiembrosGrupoSync(grupo.getId());
                    grupo.setMiembros(miembros);

                    grupos.add(grupo);
                }

                System.out.println("✅ Grupos como miembro encontrados: " + grupos.size());
                return grupos;

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo grupos como miembro: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * NUEVO: Obtiene información del creador de un grupo
     */
    public Map<String, Object> obtenerInfoCreadorGrupo(int groupId) {
        try {
            String query = """
            SELECT u.id, u.nombre, u.apellido, u.mail as email,
                   r.nombre as rol_nombre
            FROM grupos_notificacion_personalizados ng
            INNER JOIN usuarios u ON ng.creador_id = u.id
            LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
            LEFT JOIN roles r ON ur.rol_id = r.id
            WHERE ng.id = ? AND ng.activo = 1 AND u.status = 1
            """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> creador = new HashMap<>();
                creador.put("id", rs.getInt("id"));
                creador.put("nombre", rs.getString("nombre"));
                creador.put("apellido", rs.getString("apellido"));
                creador.put("email", rs.getString("email"));
                creador.put("rol_nombre", rs.getString("rol_nombre") != null ? rs.getString("rol_nombre") : "Sin rol");

                String nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                creador.put("nombre_completo", nombreCompleto);

                return creador;
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo info del creador: " + e.getMessage());
        }

        return null;
    }

}
