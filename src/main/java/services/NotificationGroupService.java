package main.java.services;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.models.PersonalNotificationGroup;

/**
 * NotificationGroupService - Servicio para gestión de grupos personalizados de
 * notificaciones
 *
 * Permite a usuarios autorizados (admin, preceptores, profesores, ATTP) crear y
 * gestionar grupos personalizados de usuarios para envío masivo de
 * notificaciones.
 *
 * Funcionalidades: - Crear grupos personalizados con nombre y descripción -
 * Agregar/quitar miembros de grupos - Listar grupos del usuario - Buscar grupos
 * por nombre - Obtener miembros de un grupo - Validaciones y permisos
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Grupos Personalizados
 */
public class NotificationGroupService {

    private static NotificationGroupService instance;
    private Connection connection;
    private ExecutorService executorService;

    // Roles autorizados para crear grupos (admin, preceptor, profesor, ATTP)
    private static final List<Integer> AUTHORIZED_ROLES = Arrays.asList(1, 2, 3, 5);

    // Constructor privado para Singleton
    private NotificationGroupService() {
        this.connection = Conexion.getInstancia().verificarConexion();
        this.executorService = Executors.newFixedThreadPool(3);

        System.out.println("✅ NotificationGroupService inicializado");
    }

    public static synchronized NotificationGroupService getInstance() {
        if (instance == null) {
            instance = new NotificationGroupService();
        }
        return instance;
    }

    // ========================================
    // MÉTODOS PRINCIPALES DE GESTIÓN DE GRUPOS
    // ========================================
    /**
     * Crea un nuevo grupo personalizado de notificaciones
     */
    public CompletableFuture<Integer> createGroup(String name, String description,
            int creatorId, List<Integer> memberIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📝 Creando grupo: " + name + " para usuario: " + creatorId);

                // Validar datos de entrada
                if (!validateGroupData(name, description, creatorId)) {
                    return -1;
                }

                // Verificar permisos del usuario
                if (!hasPermissionToCreateGroups(creatorId)) {
                    System.err.println("❌ Usuario " + creatorId + " no tiene permisos para crear grupos");
                    return -1;
                }

                // Verificar que el nombre no esté duplicado
                if (isGroupNameExists(name, creatorId)) {
                    System.err.println("❌ Ya existe un grupo con el nombre: " + name);
                    return -1;
                }

                connection.setAutoCommit(false);

                try {
                    // Insertar el grupo
                    String insertGroupQuery = """
                        INSERT INTO grupos_notificacion_personalizados 
                        (nombre, descripcion, creador_id, fecha_creacion, activo) 
                        VALUES (?, ?, ?, NOW(), 1)
                        """;

                    PreparedStatement psGroup = connection.prepareStatement(insertGroupQuery,
                            Statement.RETURN_GENERATED_KEYS);
                    psGroup.setString(1, name);
                    psGroup.setString(2, description);
                    psGroup.setInt(3, creatorId);

                    psGroup.executeUpdate();
                    ResultSet rsKeys = psGroup.getGeneratedKeys();

                    int groupId = -1;
                    if (rsKeys.next()) {
                        groupId = rsKeys.getInt(1);
                    }

                    // Agregar miembros si se proporcionaron
                    if (groupId > 0 && memberIds != null && !memberIds.isEmpty()) {
                        addMembersToGroup(groupId, memberIds);
                    }

                    connection.commit();

                    System.out.println("✅ Grupo creado exitosamente. ID: " + groupId
                            + ", Miembros: " + (memberIds != null ? memberIds.size() : 0));
                    return groupId;

                } catch (SQLException ex) {
                    connection.rollback();
                    throw ex;
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
     */
    // ========================================
// MÉTODOS CORREGIDOS DEL NotificationGroupService
// ========================================
    /**
     * Obtiene todos los grupos de un usuario - MÉTODO CORREGIDO
     */
    public CompletableFuture<List<PersonalNotificationGroup>> getUserGroups(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            List<PersonalNotificationGroup> groups = new ArrayList<>();

            try {
                System.out.println("📋 Obteniendo grupos para usuario: " + userId);

                // CORREGIDO: Eliminé la referencia a notificaciones que no está bien relacionada
                String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion,
                       COUNT(mgp.usuario_id) as member_count
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                WHERE gp.creador_id = ? AND gp.activo = 1
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                ORDER BY gp.fecha_creacion DESC
                """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    PersonalNotificationGroup group = new PersonalNotificationGroup();
                    group.setId(rs.getInt("id"));
                    group.setName(rs.getString("nombre"));
                    group.setDescription(rs.getString("descripcion"));
                    group.setCreatorId(userId);
                    group.setCreatedAt(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                    group.setMemberCount(rs.getInt("member_count"));

                    // CORREGIDO: Eliminé el setLastUsed ya que no tenemos esa información disponible
                    // con la estructura actual de la BD
                    groups.add(group);
                }

                System.out.println("✅ Encontrados " + groups.size() + " grupos para usuario " + userId);

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo grupos del usuario: " + e.getMessage());
                e.printStackTrace();
            }

            return groups;
        }, executorService);
    }

    /**
     * Obtiene un grupo específico por ID
     */
    public CompletableFuture<PersonalNotificationGroup> getGroupById(int groupId) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            System.out.println("🔍 Obteniendo grupo por ID: " + groupId);

            // CORREGIDO: Eliminé el JOIN problemático con notificaciones
            // y corregí el LEFT JOIN con miembros que faltaba
            String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.creador_id, gp.fecha_creacion,
                       COUNT(mgp.usuario_id) as member_count
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                WHERE gp.id = ? AND gp.activo = 1
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.creador_id, gp.fecha_creacion
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                PersonalNotificationGroup group = new PersonalNotificationGroup();
                group.setId(rs.getInt("id"));
                group.setName(rs.getString("nombre"));
                group.setDescription(rs.getString("descripcion"));
                group.setCreatorId(rs.getInt("creador_id"));
                group.setCreatedAt(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                group.setMemberCount(rs.getInt("member_count"));

                System.out.println("✅ Grupo encontrado: " + group.getName());
                return group;
            }

            System.out.println("⚠️ Grupo no encontrado con ID: " + groupId);
            return null;

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo grupo por ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }, executorService);
}

    /**
     * Actualiza un grupo existente
     */
    public CompletableFuture<Boolean> updateGroup(int groupId, String name, String description,
            List<Integer> memberIds, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🔄 Actualizando grupo ID: " + groupId + " por usuario: " + userId);

                // Verificar que el usuario sea el creador del grupo
                if (!isGroupOwner(groupId, userId)) {
                    System.err.println("❌ Usuario " + userId + " no es propietario del grupo " + groupId);
                    return false;
                }

                // Validar datos
                if (!validateGroupData(name, description, userId)) {
                    return false;
                }

                connection.setAutoCommit(false);

                try {
                    // Actualizar información del grupo
                    String updateQuery = """
                        UPDATE grupos_notificacion_personalizados 
                        SET nombre = ?, descripcion = ?, fecha_modificacion = NOW()
                        WHERE id = ? AND creador_id = ?
                        """;

                    PreparedStatement psUpdate = connection.prepareStatement(updateQuery);
                    psUpdate.setString(1, name);
                    psUpdate.setString(2, description);
                    psUpdate.setInt(3, groupId);
                    psUpdate.setInt(4, userId);

                    int rowsUpdated = psUpdate.executeUpdate();

                    if (rowsUpdated > 0) {
                        // Actualizar miembros si se proporcionaron
                        if (memberIds != null) {
                            // Primero, desactivar todos los miembros actuales
                            deactivateAllGroupMembers(groupId);

                            // Luego, agregar los nuevos miembros
                            if (!memberIds.isEmpty()) {
                                addMembersToGroup(groupId, memberIds);
                            }
                        }

                        connection.commit();
                        System.out.println("✅ Grupo actualizado exitosamente");
                        return true;
                    } else {
                        connection.rollback();
                        System.err.println("❌ No se pudo actualizar el grupo");
                        return false;
                    }

                } catch (SQLException ex) {
                    connection.rollback();
                    throw ex;
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
     */
    public CompletableFuture<Boolean> deleteGroup(int groupId, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🗑️ Eliminando grupo ID: " + groupId + " por usuario: " + userId);

                // Verificar que el usuario sea el creador del grupo
                if (!isGroupOwner(groupId, userId)) {
                    System.err.println("❌ Usuario " + userId + " no es propietario del grupo " + groupId);
                    return false;
                }

                String deleteQuery = """
                    UPDATE grupos_notificacion_personalizados 
                    SET activo = 0, fecha_eliminacion = NOW() 
                    WHERE id = ? AND creador_id = ?
                    """;

                PreparedStatement ps = connection.prepareStatement(deleteQuery);
                ps.setInt(1, groupId);
                ps.setInt(2, userId);

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated > 0) {
                    // También desactivar todos los miembros
                    deactivateAllGroupMembers(groupId);

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

    /**
     * Obtiene los IDs de los miembros de un grupo
     */
    public CompletableFuture<List<Integer>> getGroupMemberIds(int groupId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Integer> memberIds = new ArrayList<>();

            try {
                System.out.println("👥 Obteniendo miembros del grupo: " + groupId);

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
                    memberIds.add(rs.getInt("usuario_id"));
                }

                System.out.println("✅ Encontrados " + memberIds.size() + " miembros en grupo " + groupId);

            } catch (SQLException e) {
                System.err.println("❌ Error obteniendo miembros del grupo: " + e.getMessage());
                e.printStackTrace();
            }

            return memberIds;
        }, executorService);
    }

    /**
     * Obtiene una vista previa de los miembros del grupo (nombres)
     */
    public CompletableFuture<String> getGroupMembersPreview(int groupId) {
    return CompletableFuture.supplyAsync(() -> {
        StringBuilder preview = new StringBuilder();

        try {
            // CORREGIDO: La tabla usuarios no tiene campo 'rol' directo,
            // necesitamos hacer JOIN con usuario_roles y roles
            String query = """
                SELECT CONCAT(u.apellido, ', ', u.nombre) as nombre_completo,
                       r.nombre as rol_nombre
                FROM miembros_grupos_personalizados mgp
                INNER JOIN usuarios u ON mgp.usuario_id = u.id
                LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
                LEFT JOIN roles r ON ur.rol_id = r.id
                WHERE mgp.grupo_id = ? AND mgp.activo = 1 AND u.status = 1
                ORDER BY u.apellido, u.nombre
                LIMIT 50
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                if (count > 0) {
                    preview.append("\n");
                }

                String nombreCompleto = rs.getString("nombre_completo");
                String rolNombre = rs.getString("rol_nombre");
                
                // Si no tiene rol asignado, mostrar "Sin rol"
                if (rolNombre == null || rolNombre.trim().isEmpty()) {
                    rolNombre = "Sin rol";
                }

                preview.append("• ").append(nombreCompleto).append(" (").append(rolNombre).append(")");
                count++;
            }

            if (count == 0) {
                preview.append("El grupo no tiene miembros asignados.");
            } else if (count >= 50) {
                preview.append("\n... y más miembros");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo vista previa de miembros: " + e.getMessage());
            preview.append("Error cargando miembros del grupo.");
        }

        return preview.toString();
    }, executorService);
}

    /**
     * Busca grupos por nombre
     */
    public CompletableFuture<List<PersonalNotificationGroup>> searchGroups(String searchTerm, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            List<PersonalNotificationGroup> groups = new ArrayList<>();

            try {
                System.out.println("🔍 Buscando grupos con término: '" + searchTerm + "' para usuario: " + userId);

                String query = """
                    SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion,
                           COUNT(mgp.usuario_id) as member_count
                    FROM grupos_notificacion_personalizados gp
                    LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                    WHERE gp.creador_id = ? AND gp.activo = 1 
                    AND (gp.nombre LIKE ? OR gp.descripcion LIKE ?)
                    GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                    ORDER BY gp.nombre
                    """;

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, userId);
                String searchPattern = "%" + searchTerm + "%";
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    PersonalNotificationGroup group = new PersonalNotificationGroup();
                    group.setId(rs.getInt("id"));
                    group.setName(rs.getString("nombre"));
                    group.setDescription(rs.getString("descripcion"));
                    group.setCreatorId(userId);
                    group.setCreatedAt(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                    group.setMemberCount(rs.getInt("member_count"));

                    groups.add(group);
                }

                System.out.println("✅ Encontrados " + groups.size() + " grupos con término: " + searchTerm);

            } catch (SQLException e) {
                System.err.println("❌ Error buscando grupos: " + e.getMessage());
                e.printStackTrace();
            }

            return groups;
        }, executorService);
    }

    /**
     * Duplica un grupo existente
     */
    public CompletableFuture<Integer> duplicateGroup(int originalGroupId, String newName, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("📋 Duplicando grupo ID: " + originalGroupId + " con nombre: " + newName);

                // Verificar que el usuario sea el creador del grupo original
                if (!isGroupOwner(originalGroupId, userId)) {
                    System.err.println("❌ Usuario " + userId + " no es propietario del grupo " + originalGroupId);
                    return -1;
                }

                // Obtener datos del grupo original
                PersonalNotificationGroup originalGroup = getGroupById(originalGroupId).get();
                if (originalGroup == null) {
                    System.err.println("❌ Grupo original no encontrado");
                    return -1;
                }

                // Obtener miembros del grupo original
                List<Integer> originalMembers = getGroupMemberIds(originalGroupId).get();

                // Crear descripción para la copia
                String newDescription = "Copia de: " + originalGroup.getDescription();

                // Crear el nuevo grupo
                return createGroup(newName, newDescription, userId, originalMembers).get();

            } catch (Exception e) {
                System.err.println("❌ Error duplicando grupo: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }, executorService);
    }

    // ========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================
    /**
     * Agrega miembros a un grupo
     */
    private void addMembersToGroup(int groupId, List<Integer> memberIds) throws SQLException {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }

        String insertMemberQuery = """
            INSERT INTO miembros_grupos_personalizados (grupo_id, usuario_id, fecha_agregado, activo) 
            VALUES (?, ?, NOW(), 1)
            ON DUPLICATE KEY UPDATE activo = 1, fecha_agregado = NOW()
            """;

        PreparedStatement psMember = connection.prepareStatement(insertMemberQuery);

        int validMembers = 0;
        for (Integer memberId : memberIds) {
            // Verificar que el usuario existe y está activo
            if (isValidUser(memberId)) {
                psMember.setInt(1, groupId);
                psMember.setInt(2, memberId);
                psMember.addBatch();
                validMembers++;
            }
        }

        if (validMembers > 0) {
            psMember.executeBatch();
            System.out.println("✅ Agregados " + validMembers + " miembros al grupo " + groupId);
        }
    }

    /**
     * Desactiva todos los miembros de un grupo
     */
    private void deactivateAllGroupMembers(int groupId) throws SQLException {
        String deactivateQuery = """
            UPDATE miembros_grupos_personalizados 
            SET activo = 0 
            WHERE grupo_id = ?
            """;

        PreparedStatement ps = connection.prepareStatement(deactivateQuery);
        ps.setInt(1, groupId);
        ps.executeUpdate();

        System.out.println("🔄 Miembros del grupo " + groupId + " desactivados");
    }

    /**
     * Valida los datos básicos de un grupo
     */
    private boolean validateGroupData(String name, String description, int creatorId) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("❌ Nombre del grupo no puede estar vacío");
            return false;
        }

        if (name.length() > 100) {
            System.err.println("❌ Nombre del grupo demasiado largo (máximo 100 caracteres)");
            return false;
        }

        if (description != null && description.length() > 500) {
            System.err.println("❌ Descripción del grupo demasiado larga (máximo 500 caracteres)");
            return false;
        }

        if (creatorId <= 0) {
            System.err.println("❌ ID de creador inválido");
            return false;
        }

        return true;
    }

    /**
     * Verifica si un usuario tiene permisos para crear grupos
     */
    private boolean hasPermissionToCreateGroups(int userId) {
        try {
            // CORREGIDO: Adaptado a la estructura real de la BD con usuario_roles
            String query = """
            SELECT ur.rol_id 
            FROM usuarios u
            INNER JOIN usuario_roles ur ON u.id = ur.usuario_id
            WHERE u.id = ? AND u.status = 1
            """;
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int userRole = rs.getInt("rol_id");
                if (AUTHORIZED_ROLES.contains(userRole)) {
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
    private boolean isGroupNameExists(String name, int creatorId) {
        try {
            String query = """
                SELECT COUNT(*) FROM grupos_notificacion_personalizados 
                WHERE nombre = ? AND creador_id = ? AND activo = 1
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, creatorId);
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
     * Verifica si un usuario es propietario de un grupo
     */
    private boolean isGroupOwner(int groupId, int userId) {
        try {
            String query = """
                SELECT COUNT(*) FROM grupos_notificacion_personalizados 
                WHERE id = ? AND creador_id = ? AND activo = 1
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando propietario del grupo: " + e.getMessage());
        }

        return false;
    }

    /**
     * Verifica si un usuario es válido y está activo
     */
    private boolean isValidUser(int userId) {
        try {
            String query = "SELECT COUNT(*) FROM usuarios WHERE id = ? AND status = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando usuario válido: " + e.getMessage());
        }

        return false;
    }

    // ========================================
    // MÉTODOS DE ESTADÍSTICAS Y UTILIDADES
    // ========================================
    /**
     * Obtiene estadísticas de grupos para un usuario
     */
    public CompletableFuture<GroupStatistics> getGroupStatistics(int userId) {
    return CompletableFuture.supplyAsync(() -> {
        GroupStatistics stats = new GroupStatistics();

        try {
            // Estadísticas básicas
            String basicQuery = """
                SELECT COUNT(*) as total_groups,
                       COALESCE(AVG(member_count.count), 0) as avg_members
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN (
                    SELECT grupo_id, COUNT(*) as count 
                    FROM miembros_grupos_personalizados 
                    WHERE activo = 1 
                    GROUP BY grupo_id
                ) member_count ON gp.id = member_count.grupo_id
                WHERE gp.creador_id = ? AND gp.activo = 1
                """;

            PreparedStatement psBasic = connection.prepareStatement(basicQuery);
            psBasic.setInt(1, userId);
            ResultSet rsBasic = psBasic.executeQuery();

            if (rsBasic.next()) {
                stats.setTotalGroups(rsBasic.getInt("total_groups"));
                stats.setAverageMembers(rsBasic.getDouble("avg_members"));
            }

            // Total de miembros únicos
            String uniqueMembersQuery = """
                SELECT COUNT(DISTINCT mgp.usuario_id) as unique_members
                FROM grupos_notificacion_personalizados gp
                INNER JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id
                WHERE gp.creador_id = ? AND gp.activo = 1 AND mgp.activo = 1
                """;

            PreparedStatement psUnique = connection.prepareStatement(uniqueMembersQuery);
            psUnique.setInt(1, userId);
            ResultSet rsUnique = psUnique.executeQuery();

            if (rsUnique.next()) {
                stats.setUniqueMembers(rsUnique.getInt("unique_members"));
            }

            // CORREGIDO: Ahora buscamos notificaciones que usen grupos personalizados específicos del usuario
            // Si ya tienes la columna grupo_personalizado_id, usa esta versión:
            String usageQuery = """
                SELECT COUNT(DISTINCT n.id) as notifications_sent
                FROM grupos_notificacion_personalizados gp
                INNER JOIN notificaciones n ON n.grupo_personalizado_id = gp.id
                WHERE gp.creador_id = ? AND gp.activo = 1
                AND n.tipo_notificacion = 'GRUPO_PERSONALIZADO'
                AND n.estado = 'ACTIVA'
                """;

            // ALTERNATIVA: Si no tienes la columna grupo_personalizado_id todavía, usa esta versión:
            /*
            String usageQuery = """
                SELECT COUNT(*) as notifications_sent
                FROM notificaciones n
                WHERE n.remitente_id = ? 
                AND n.tipo_notificacion = 'GRUPO_PERSONALIZADO'
                AND n.estado = 'ACTIVA'
                """;
            */

            PreparedStatement psUsage = connection.prepareStatement(usageQuery);
            psUsage.setInt(1, userId);
            ResultSet rsUsage = psUsage.executeQuery();

            if (rsUsage.next()) {
                stats.setNotificationsSent(rsUsage.getInt("notifications_sent"));
            }

            System.out.println("✅ Estadísticas calculadas para usuario " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo estadísticas: " + e.getMessage());
            e.printStackTrace();
            
            // Si falla por la columna grupo_personalizado_id que no existe,
            // usar la consulta alternativa
            if (e.getMessage().contains("grupo_personalizado_id")) {
                try {
                    String usageQueryAlt = """
                        SELECT COUNT(*) as notifications_sent
                        FROM notificaciones n
                        WHERE n.remitente_id = ? 
                        AND n.tipo_notificacion = 'GRUPO_PERSONALIZADO'
                        AND n.estado = 'ACTIVA'
                        """;
                    
                    PreparedStatement psUsageAlt = connection.prepareStatement(usageQueryAlt);
                    psUsageAlt.setInt(1, userId);
                    ResultSet rsUsageAlt = psUsageAlt.executeQuery();
                    
                    if (rsUsageAlt.next()) {
                        stats.setNotificationsSent(rsUsageAlt.getInt("notifications_sent"));
                    }
                    
                    System.out.println("✅ Estadísticas calculadas (modo alternativo) para usuario " + userId);
                } catch (SQLException altE) {
                    System.err.println("❌ Error con consulta alternativa: " + altE.getMessage());
                    stats.setNotificationsSent(0);
                }
            } else {
                stats.setNotificationsSent(0);
            }
        }

        return stats;
    }, executorService);
}

    /**
     * Obtiene grupos más utilizados de un usuario
     */
    public CompletableFuture<List<PersonalNotificationGroup>> getMostUsedGroups(int userId, int limit) {
    return CompletableFuture.supplyAsync(() -> {
        List<PersonalNotificationGroup> groups = new ArrayList<>();

        try {
            // CORREGIDO: Agregamos información de uso real si existe la relación
            // Si tienes la columna grupo_personalizado_id:
            String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion,
                       COUNT(DISTINCT mgp.usuario_id) as member_count,
                       COUNT(DISTINCT n.id) as usage_count,
                       MAX(n.fecha_creacion) as last_used
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                LEFT JOIN notificaciones n ON n.grupo_personalizado_id = gp.id 
                                           AND n.tipo_notificacion = 'GRUPO_PERSONALIZADO'
                WHERE gp.creador_id = ? AND gp.activo = 1
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                ORDER BY usage_count DESC, member_count DESC, gp.fecha_creacion DESC
                LIMIT ?
                """;

            // ALTERNATIVA: Si no tienes la columna grupo_personalizado_id:
            /*
            String query = """
                SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion,
                       COUNT(mgp.usuario_id) as member_count
                FROM grupos_notificacion_personalizados gp
                LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                WHERE gp.creador_id = ? AND gp.activo = 1
                GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                ORDER BY member_count DESC, gp.fecha_creacion DESC
                LIMIT ?
                """;
            */

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PersonalNotificationGroup group = new PersonalNotificationGroup();
                group.setId(rs.getInt("id"));
                group.setName(rs.getString("nombre"));
                group.setDescription(rs.getString("descripcion"));
                group.setCreatorId(userId);
                group.setCreatedAt(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                group.setMemberCount(rs.getInt("member_count"));

                // Si existe la información de uso, agregarla
                try {
                    Timestamp lastUsed = rs.getTimestamp("last_used");
                    if (lastUsed != null) {
                        group.setLastUsed(lastUsed.toLocalDateTime());
                    }
                } catch (SQLException e) {
                    // Si no existe la columna last_used, ignorar
                }

                groups.add(group);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo grupos más utilizados: " + e.getMessage());
            
            // Si falla por la columna grupo_personalizado_id, usar consulta simplificada
            if (e.getMessage().contains("grupo_personalizado_id")) {
                try {
                    String queryAlt = """
                        SELECT gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion,
                               COUNT(mgp.usuario_id) as member_count
                        FROM grupos_notificacion_personalizados gp
                        LEFT JOIN miembros_grupos_personalizados mgp ON gp.id = mgp.grupo_id AND mgp.activo = 1
                        WHERE gp.creador_id = ? AND gp.activo = 1
                        GROUP BY gp.id, gp.nombre, gp.descripcion, gp.fecha_creacion
                        ORDER BY member_count DESC, gp.fecha_creacion DESC
                        LIMIT ?
                        """;

                    PreparedStatement psAlt = connection.prepareStatement(queryAlt);
                    psAlt.setInt(1, userId);
                    psAlt.setInt(2, limit);
                    ResultSet rsAlt = psAlt.executeQuery();

                    while (rsAlt.next()) {
                        PersonalNotificationGroup group = new PersonalNotificationGroup();
                        group.setId(rsAlt.getInt("id"));
                        group.setName(rsAlt.getString("nombre"));
                        group.setDescription(rsAlt.getString("descripcion"));
                        group.setCreatorId(userId);
                        group.setCreatedAt(rsAlt.getTimestamp("fecha_creacion").toLocalDateTime());
                        group.setMemberCount(rsAlt.getInt("member_count"));

                        groups.add(group);
                    }
                    
                    System.out.println("✅ Grupos más utilizados obtenidos (modo alternativo)");
                } catch (SQLException altE) {
                    System.err.println("❌ Error con consulta alternativa: " + altE.getMessage());
                }
            }
        }

        return groups;
    }, executorService);
}

    // ========================================
    // CLASES AUXILIARES
    // ========================================
    /**
     * Clase para estadísticas de grupos
     */
    public static class GroupStatistics {

        private int totalGroups;
        private double averageMembers;
        private int uniqueMembers;
        private int notificationsSent;

        // Constructores
        public GroupStatistics() {
        }

        // Getters y Setters
        public int getTotalGroups() {
            return totalGroups;
        }

        public void setTotalGroups(int totalGroups) {
            this.totalGroups = totalGroups;
        }

        public double getAverageMembers() {
            return averageMembers;
        }

        public void setAverageMembers(double averageMembers) {
            this.averageMembers = averageMembers;
        }

        public int getUniqueMembers() {
            return uniqueMembers;
        }

        public void setUniqueMembers(int uniqueMembers) {
            this.uniqueMembers = uniqueMembers;
        }

        public int getNotificationsSent() {
            return notificationsSent;
        }

        public void setNotificationsSent(int notificationsSent) {
            this.notificationsSent = notificationsSent;
        }

        @Override
        public String toString() {
            return String.format("Grupos: %d, Promedio miembros: %.1f, Miembros únicos: %d, Notificaciones enviadas: %d",
                    totalGroups, averageMembers, uniqueMembers, notificationsSent);
        }
    }

    // ========================================
    // MÉTODOS DE LIMPIEZA Y CIERRE
    // ========================================
    /**
     * Limpia grupos inactivos antiguos (opcional - para mantenimiento)
     */
    public CompletableFuture<Integer> cleanupOldInactiveGroups(int daysOld) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🧹 Limpiando grupos inactivos antiguos (>" + daysOld + " días)");

                String cleanupQuery = """
                    DELETE FROM grupos_notificacion_personalizados 
                    WHERE activo = 0 
                    AND fecha_eliminacion IS NOT NULL 
                    AND fecha_eliminacion < DATE_SUB(NOW(), INTERVAL ? DAY)
                    """;

                PreparedStatement ps = connection.prepareStatement(cleanupQuery);
                ps.setInt(1, daysOld);

                int deletedGroups = ps.executeUpdate();

                if (deletedGroups > 0) {
                    System.out.println("✅ Eliminados " + deletedGroups + " grupos antiguos");
                }

                return deletedGroups;

            } catch (SQLException e) {
                System.err.println("❌ Error limpiando grupos antiguos: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }, executorService);
    }

    /**
     * Cierra el servicio y libera recursos
     */
    public void shutdown() {
        try {
            System.out.println("🔄 Cerrando NotificationGroupService...");

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                System.out.println("✅ ExecutorService cerrado");
            }

            System.out.println("✅ NotificationGroupService cerrado correctamente");

        } catch (Exception e) {
            System.err.println("Error cerrando NotificationGroupService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método de limpieza para resetear la instancia singleton
     */
    public static void resetInstance() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
            System.out.println("🔄 NotificationGroupService instancia reseteada");
        }
    }
}
