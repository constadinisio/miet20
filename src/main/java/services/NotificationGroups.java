package main.java.services;

import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.swing.*;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.models.NotificationModels.*;

/**
 * =========================================================================
 * GESTIÓN CONSOLIDADA DE GRUPOS DE NOTIFICACIONES
 * =========================================================================
 * 
 * Este archivo consolida toda la funcionalidad relacionada con grupos de notificaciones
 * para reducir la cantidad de archivos y centralizar la gestión de grupos.
 * 
 * CONSOLIDACIÓN DE 4 ARCHIVOS:
 * - NotificationGroupService.java
 * - NotificationGroupManager.java  
 * - UserSelectorComponent.java
 * - HierarchicalUserSelector.java (si existe)
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Consolidado
 * @date 21/07/2025
 */
public class NotificationGroups {

    // =========================================================================
    // 1. NOTIFICATION_GROUP_SERVICE - SERVICIO DE GRUPOS PERSONALIZADOS
    // =========================================================================
    
    /**
     * Servicio para gestión de grupos personalizados de notificaciones
     */
    public static class NotificationGroupService {

        private static NotificationGroupService instance;
        private Connection connection;


        // Roles autorizados para crear grupos (admin, preceptor, profesor, ATTP)
        private static final List<Integer> AUTHORIZED_ROLES = Arrays.asList(1, 2, 3, 5);

        // Constructor privado para Singleton
        private NotificationGroupService() {
            this.connection = Conexion.getInstancia().verificarConexion();

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
         * Crea un nuevo grupo personalizado
         */
        public CompletableFuture<Integer> crearGrupoPersonalizado(String nombre, String descripcion, 
                int creadorId, List<Integer> miembros) {
            
            return CompletableFuture.supplyAsync(() -> {
                if (!isAuthorizedToCreateGroups(creadorId)) {
                    System.err.println("Usuario no autorizado para crear grupos: " + creadorId);
                    return -1;
                }

                try {
                    // Insertar grupo principal
                    String insertGroup = """
                        INSERT INTO grupos_notificaciones_personalizados 
                        (nombre, descripcion, creador_id, fecha_creacion, activo) 
                        VALUES (?, ?, ?, NOW(), 1)
                    """;

                    try (PreparedStatement ps = connection.prepareStatement(insertGroup, 
                            Statement.RETURN_GENERATED_KEYS)) {
                        
                        ps.setString(1, nombre);
                        ps.setString(2, descripcion);
                        ps.setInt(3, creadorId);

                        int result = ps.executeUpdate();
                        if (result > 0) {
                            ResultSet rs = ps.getGeneratedKeys();
                            if (rs.next()) {
                                int groupId = rs.getInt(1);
                                
                                // Agregar miembros si se proporcionaron
                                if (miembros != null && !miembros.isEmpty()) {
                                    agregarMiembrosAGrupo(groupId, miembros);
                                }
                                
                                System.out.println("✅ Grupo personalizado creado: " + nombre + " (ID: " + groupId + ")");
                                return groupId;
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error creando grupo personalizado: " + e.getMessage());
                }
                return -1;
            });
        }

        /**
         * Obtiene todos los grupos personalizados de un usuario
         */
        public List<PersonalNotificationGroup> obtenerGruposUsuario(int usuarioId) {
            List<PersonalNotificationGroup> grupos = new ArrayList<>();
            
            String query = """
                SELECT id, nombre, descripcion, creador_id, fecha_creacion, fecha_ultimo_uso,
                       (SELECT COUNT(*) FROM miembros_grupos_personalizados WHERE grupo_id = gnp.id) as member_count,
                       activo
                FROM grupos_notificaciones_personalizados gnp
                WHERE creador_id = ? OR id IN (
                    SELECT grupo_id FROM miembros_grupos_personalizados WHERE usuario_id = ?
                )
                ORDER BY fecha_ultimo_uso DESC, fecha_creacion DESC
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, usuarioId);
                ps.setInt(2, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    PersonalNotificationGroup grupo = new PersonalNotificationGroup();
                    
                    grupo.setId(rs.getInt("id"));
                    grupo.setName(rs.getString("nombre"));
                    grupo.setDescription(rs.getString("descripcion"));
                    grupo.setCreatorId(rs.getInt("creador_id"));
                    
                    Timestamp created = rs.getTimestamp("fecha_creacion");
                    if (created != null) {
                        grupo.setCreatedAt(created.toLocalDateTime());
                    }
                    
                    Timestamp lastUsed = rs.getTimestamp("fecha_ultimo_uso");
                    if (lastUsed != null) {
                        grupo.setLastUsed(lastUsed.toLocalDateTime());
                    }
                    
                    grupo.setMemberCount(rs.getInt("member_count"));
                    grupo.setActive(rs.getBoolean("activo"));
                    
                    grupos.add(grupo);
                }
                
            } catch (SQLException e) {
                System.err.println("Error obteniendo grupos del usuario: " + e.getMessage());
            }
            
            return grupos;
        }

        /**
         * Agrega miembros a un grupo
         */
        public CompletableFuture<Boolean> agregarMiembrosAGrupo(int grupoId, List<Integer> miembros) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String insertMember = """
                        INSERT IGNORE INTO miembros_grupos_personalizados (grupo_id, usuario_id, fecha_agregado) 
                        VALUES (?, ?, NOW())
                    """;
                    
                    try (PreparedStatement ps = connection.prepareStatement(insertMember)) {
                        for (int miembro : miembros) {
                            ps.setInt(1, grupoId);
                            ps.setInt(2, miembro);
                            ps.addBatch();
                        }
                        
                        int[] results = ps.executeBatch();
                        int agregados = Arrays.stream(results).sum();
                        
                        // Actualizar fecha de último uso del grupo
                        actualizarFechaUltimoUso(grupoId);
                        
                        System.out.println("✅ " + agregados + " miembros agregados al grupo " + grupoId);
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("Error agregando miembros al grupo: " + e.getMessage());
                    return false;
                }
            });
        }

        /**
         * Obtiene los miembros de un grupo
         */
        public List<UserInfo> obtenerMiembrosGrupo(int grupoId) {
            List<UserInfo> miembros = new ArrayList<>();
            
            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, r.nombre as rol_nombre
                FROM miembros_grupos_personalizados mgp
                JOIN usuarios u ON mgp.usuario_id = u.id
                LEFT JOIN roles r ON u.rol_id = r.id
                WHERE mgp.grupo_id = ? AND u.activo = 1
                ORDER BY u.apellido, u.nombre
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, grupoId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    UserInfo user = new UserInfo();
                    user.id = rs.getInt("id");
                    user.nombre = rs.getString("nombre");
                    user.apellido = rs.getString("apellido");
                    user.email = rs.getString("email");
                    user.rol = rs.getString("rol_nombre");
                    
                    miembros.add(user);
                }
                
            } catch (SQLException e) {
                System.err.println("Error obteniendo miembros del grupo: " + e.getMessage());
            }
            
            return miembros;
        }

        /**
         * Elimina miembros de un grupo
         */
        public CompletableFuture<Boolean> eliminarMiembrosDeGrupo(int grupoId, List<Integer> miembros) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String deleteMember = """
                        DELETE FROM miembros_grupos_personalizados 
                        WHERE grupo_id = ? AND usuario_id = ?
                    """;
                    
                    try (PreparedStatement ps = connection.prepareStatement(deleteMember)) {
                        for (int miembro : miembros) {
                            ps.setInt(1, grupoId);
                            ps.setInt(2, miembro);
                            ps.addBatch();
                        }
                        
                        int[] results = ps.executeBatch();
                        int eliminados = Arrays.stream(results).sum();
                        
                        System.out.println("✅ " + eliminados + " miembros eliminados del grupo " + grupoId);
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("Error eliminando miembros del grupo: " + e.getMessage());
                    return false;
                }
            });
        }

        /**
         * Elimina un grupo completamente
         */
        public CompletableFuture<Boolean> eliminarGrupo(int grupoId, int usuarioId) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Verificar que el usuario es el creador o admin
                    if (!isOwnerOrAdmin(grupoId, usuarioId)) {
                        System.err.println("Usuario no autorizado para eliminar el grupo: " + grupoId);
                        return false;
                    }

                    // Eliminar miembros primero
                    String deleteMembers = "DELETE FROM miembros_grupos_personalizados WHERE grupo_id = ?";
                    try (PreparedStatement ps1 = connection.prepareStatement(deleteMembers)) {
                        ps1.setInt(1, grupoId);
                        ps1.executeUpdate();
                    }

                    // Eliminar grupo
                    String deleteGroup = "DELETE FROM grupos_notificaciones_personalizados WHERE id = ?";
                    try (PreparedStatement ps2 = connection.prepareStatement(deleteGroup)) {
                        ps2.setInt(1, grupoId);
                        int result = ps2.executeUpdate();
                        
                        if (result > 0) {
                            System.out.println("✅ Grupo " + grupoId + " eliminado completamente");
                            return true;
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error eliminando grupo: " + e.getMessage());
                }
                return false;
            });
        }

        // ========================================
        // MÉTODOS AUXILIARES
        // ========================================

        private boolean isAuthorizedToCreateGroups(int usuarioId) {
            String query = "SELECT rol_id FROM usuarios WHERE id = ? AND activo = 1";
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    int rolId = rs.getInt("rol_id");
                    return AUTHORIZED_ROLES.contains(rolId);
                }
            } catch (SQLException e) {
                System.err.println("Error verificando autorización: " + e.getMessage());
            }
            
            return false;
        }

        private boolean isOwnerOrAdmin(int grupoId, int usuarioId) {
            String query = """
                SELECT COUNT(*) FROM grupos_notificaciones_personalizados 
                WHERE id = ? AND (creador_id = ? OR ? IN (
                    SELECT id FROM usuarios WHERE rol_id = 1 AND activo = 1
                ))
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, grupoId);
                ps.setInt(2, usuarioId);
                ps.setInt(3, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                return rs.next() && rs.getInt(1) > 0;
            } catch (SQLException e) {
                System.err.println("Error verificando propietario: " + e.getMessage());
                return false;
            }
        }

        private void actualizarFechaUltimoUso(int grupoId) {
            String update = "UPDATE grupos_notificaciones_personalizados SET fecha_ultimo_uso = NOW() WHERE id = ?";
            
            try (PreparedStatement ps = connection.prepareStatement(update)) {
                ps.setInt(1, grupoId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error actualizando fecha de último uso: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 2. NOTIFICATION_GROUP_MANAGER - CONTROLADOR AVANZADO DE GRUPOS
    // =========================================================================
    
    /**
     * Controlador para la gestión avanzada de grupos de notificaciones personalizados
     */
    public static class NotificationGroupManager {

        private static NotificationGroupManager instance;
        private final Connection connection;
        private final List<GroupChangeListener> listeners;

        // Cache para optimizar consultas frecuentes
        private final Map<Integer, List<NotificationGroup>> userGroupsCache;
        private final Map<Integer, NotificationGroup> groupCache;
        private long lastCacheUpdate = 0;
        private static final long CACHE_DURATION = 300000; // 5 minutos

        private NotificationGroupManager() {
            this.connection = Conexion.getInstancia().verificarConexion();
            this.listeners = new ArrayList<>();
            this.userGroupsCache = new HashMap<>();
            this.groupCache = new HashMap<>();

            System.out.println("✅ NotificationGroupManager inicializado");
        }

        public static synchronized NotificationGroupManager getInstance() {
            if (instance == null) {
                instance = new NotificationGroupManager();
            }
            return instance;
        }

        // ========================================
        // MÉTODOS PRINCIPALES DE GESTIÓN AVANZADA
        // ========================================

        /**
         * Crea un grupo avanzado con todas las características
         */
        public CompletableFuture<Integer> crearGrupoAvanzado(NotificationGroup grupo) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String insertGroup = """
                        INSERT INTO notification_groups 
                        (nombre, descripcion, creador_id, fecha_creacion, categoria, color, activo) 
                        VALUES (?, ?, ?, NOW(), ?, ?, 1)
                    """;

                    try (PreparedStatement ps = connection.prepareStatement(insertGroup, 
                            Statement.RETURN_GENERATED_KEYS)) {
                        
                        ps.setString(1, grupo.getNombre());
                        ps.setString(2, grupo.getDescripcion());
                        ps.setInt(3, grupo.getCreadorId());
                        ps.setString(4, grupo.getCategoria());
                        ps.setString(5, grupo.getColor());

                        int result = ps.executeUpdate();
                        if (result > 0) {
                            ResultSet rs = ps.getGeneratedKeys();
                            if (rs.next()) {
                                int groupId = rs.getInt(1);
                                grupo.setId(groupId);
                                
                                // Agregar miembros si existen
                                if (grupo.getMiembros() != null && !grupo.getMiembros().isEmpty()) {
                                    agregarMiembrosConDetalle(groupId, grupo.getMiembros(), grupo.getMiembrosDetalle());
                                }
                                
                                // Invalidar cache
                                invalidateCache();
                                
                                // Notificar listeners
                                notifyGroupCreated(grupo);
                                
                                System.out.println("✅ Grupo avanzado creado: " + grupo.getNombre() + " (ID: " + groupId + ")");
                                return groupId;
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error creando grupo avanzado: " + e.getMessage());
                }
                return -1;
            });
        }

        /**
         * Obtiene grupos con cache para mejor rendimiento
         */
        public List<NotificationGroup> obtenerGruposConCache(int usuarioId) {
            // Verificar cache
            if (System.currentTimeMillis() - lastCacheUpdate < CACHE_DURATION) {
                List<NotificationGroup> cached = userGroupsCache.get(usuarioId);
                if (cached != null) {
                    return new ArrayList<>(cached);
                }
            }

            // Obtener de BD y actualizar cache
            List<NotificationGroup> grupos = obtenerGruposAvanzados(usuarioId);
            userGroupsCache.put(usuarioId, new ArrayList<>(grupos));
            lastCacheUpdate = System.currentTimeMillis();
            
            return grupos;
        }

        /**
         * Obtiene grupos avanzados con todos los detalles
         */
        public List<NotificationGroup> obtenerGruposAvanzados(int usuarioId) {
            List<NotificationGroup> grupos = new ArrayList<>();
            
            String query = """
                SELECT ng.id, ng.nombre, ng.descripcion, ng.creador_id, ng.fecha_creacion,
                       ng.fecha_modificacion, ng.fecha_ultimo_uso, ng.categoria, ng.color,
                       ng.total_notificaciones_enviadas, ng.es_favorito, ng.activo,
                       u.nombre as creador_nombre, u.apellido as creador_apellido
                FROM notification_groups ng
                LEFT JOIN usuarios u ON ng.creador_id = u.id
                WHERE ng.creador_id = ? OR ng.id IN (
                    SELECT grupo_id FROM notification_group_members WHERE usuario_id = ?
                )
                ORDER BY ng.es_favorito DESC, ng.fecha_ultimo_uso DESC
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, usuarioId);
                ps.setInt(2, usuarioId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    NotificationGroup grupo = new NotificationGroup();
                    
                    grupo.setId(rs.getInt("id"));
                    grupo.setNombre(rs.getString("nombre"));
                    grupo.setDescripcion(rs.getString("descripcion"));
                    grupo.setCreadorId(rs.getInt("creador_id"));
                    
                    String creadorNombre = rs.getString("creador_nombre");
                    String creadorApellido = rs.getString("creador_apellido");
                    if (creadorNombre != null && creadorApellido != null) {
                        grupo.setNombreCreador(creadorNombre + " " + creadorApellido);
                    }
                    
                    // Fechas
                    Timestamp created = rs.getTimestamp("fecha_creacion");
                    if (created != null) {
                        grupo.setFechaCreacion(created.toLocalDateTime());
                    }
                    
                    Timestamp modified = rs.getTimestamp("fecha_modificacion");
                    if (modified != null) {
                        grupo.setFechaModificacion(modified.toLocalDateTime());
                    }
                    
                    Timestamp lastUsed = rs.getTimestamp("fecha_ultimo_uso");
                    if (lastUsed != null) {
                        grupo.setFechaUltimoUso(lastUsed.toLocalDateTime());
                    }
                    
                    // Metadatos
                    grupo.setCategoria(rs.getString("categoria"));
                    grupo.setColor(rs.getString("color"));
                    grupo.setTotalNotificacionesEnviadas(rs.getInt("total_notificaciones_enviadas"));
                    grupo.setEsFavorito(rs.getBoolean("es_favorito"));
                    grupo.setActivo(rs.getBoolean("activo"));
                    
                    // Cargar miembros
                    cargarMiembrosDelGrupo(grupo);
                    
                    grupos.add(grupo);
                }
                
            } catch (SQLException e) {
                System.err.println("Error obteniendo grupos avanzados: " + e.getMessage());
            }
            
            return grupos;
        }

        /**
         * Actualiza un grupo existente
         */
        public CompletableFuture<Boolean> actualizarGrupo(NotificationGroup grupo) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String updateGroup = """
                        UPDATE notification_groups 
                        SET nombre = ?, descripcion = ?, categoria = ?, color = ?, 
                            fecha_modificacion = NOW(), es_favorito = ?
                        WHERE id = ?
                    """;

                    try (PreparedStatement ps = connection.prepareStatement(updateGroup)) {
                        ps.setString(1, grupo.getNombre());
                        ps.setString(2, grupo.getDescripcion());
                        ps.setString(3, grupo.getCategoria());
                        ps.setString(4, grupo.getColor());
                        ps.setBoolean(5, grupo.isEsFavorito());
                        ps.setInt(6, grupo.getId());

                        int result = ps.executeUpdate();
                        if (result > 0) {
                            // Invalidar cache
                            invalidateCache();
                            
                            // Notificar listeners
                            notifyGroupUpdated(grupo);
                            
                            System.out.println("✅ Grupo actualizado: " + grupo.getNombre());
                            return true;
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error actualizando grupo: " + e.getMessage());
                }
                return false;
            });
        }

        /**
         * Duplica un grupo existente
         */
        public CompletableFuture<Integer> duplicarGrupo(int grupoId, String nuevoNombre, int usuarioId) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Obtener grupo original
                    NotificationGroup grupoOriginal = obtenerGrupoPorId(grupoId);
                    if (grupoOriginal == null) {
                        return -1;
                    }

                    // Crear copia
                    NotificationGroup nuevoGrupo = grupoOriginal.clone();
                    nuevoGrupo.setId(0); // Reset ID
                    nuevoGrupo.setNombre(nuevoNombre);
                    nuevoGrupo.setCreadorId(usuarioId);
                    nuevoGrupo.setFechaCreacion(LocalDateTime.now());
                    nuevoGrupo.setFechaModificacion(null);
                    nuevoGrupo.setFechaUltimoUso(null);
                    nuevoGrupo.setTotalNotificacionesEnviadas(0);

                    // Crear el nuevo grupo
                    return crearGrupoAvanzado(nuevoGrupo).get();

                } catch (Exception e) {
                    System.err.println("Error duplicando grupo: " + e.getMessage());
                    return -1;
                }
            });
        }

        // ========================================
        // MÉTODOS AUXILIARES
        // ========================================

        private void agregarMiembrosConDetalle(int groupId, List<Integer> miembros, 
                Map<Integer, NotificationGroup.MiembroGrupo> detalles) {
            try {
                String insertMember = """
                    INSERT INTO notification_group_members (grupo_id, usuario_id, fecha_agregado) 
                    VALUES (?, ?, NOW())
                """;
                
                try (PreparedStatement ps = connection.prepareStatement(insertMember)) {
                    for (int miembro : miembros) {
                        ps.setInt(1, groupId);
                        ps.setInt(2, miembro);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            } catch (SQLException e) {
                System.err.println("Error agregando miembros con detalle: " + e.getMessage());
            }
        }

        private void cargarMiembrosDelGrupo(NotificationGroup grupo) {
            try {
                String query = """
                    SELECT ngm.usuario_id, u.nombre, u.apellido, r.nombre as rol
                    FROM notification_group_members ngm
                    JOIN usuarios u ON ngm.usuario_id = u.id
                    LEFT JOIN roles r ON u.rol_id = r.id
                    WHERE ngm.grupo_id = ? AND u.activo = 1
                """;
                
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, grupo.getId());
                    ResultSet rs = ps.executeQuery();
                    
                    List<Integer> miembros = new ArrayList<>();
                    Map<Integer, NotificationGroup.MiembroGrupo> detalles = new HashMap<>();
                    
                    while (rs.next()) {
                        int usuarioId = rs.getInt("usuario_id");
                        miembros.add(usuarioId);
                        
                        NotificationGroup.MiembroGrupo detalle = new NotificationGroup.MiembroGrupo(
                            usuarioId,
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("rol")
                        );
                        
                        detalles.put(usuarioId, detalle);
                    }
                    
                    grupo.setMiembros(miembros);
                    grupo.setMiembrosDetalle(detalles);
                }
            } catch (SQLException e) {
                System.err.println("Error cargando miembros del grupo: " + e.getMessage());
            }
        }

        private NotificationGroup obtenerGrupoPorId(int grupoId) {
            // Implementar obtención de grupo por ID
            return null; // Placeholder
        }

        private void invalidateCache() {
            userGroupsCache.clear();
            groupCache.clear();
            lastCacheUpdate = 0;
        }

        // ========================================
        // SISTEMA DE LISTENERS
        // ========================================

        public interface GroupChangeListener {
            void onGroupCreated(NotificationGroup group);
            void onGroupUpdated(NotificationGroup group);
            void onGroupDeleted(int groupId);
        }

        public void addGroupChangeListener(GroupChangeListener listener) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        public void removeGroupChangeListener(GroupChangeListener listener) {
            listeners.remove(listener);
        }

        private void notifyGroupCreated(NotificationGroup group) {
            SwingUtilities.invokeLater(() -> {
                for (GroupChangeListener listener : listeners) {
                    try {
                        listener.onGroupCreated(group);
                    } catch (Exception e) {
                        System.err.println("Error en listener de grupo creado: " + e.getMessage());
                    }
                }
            });
        }

        private void notifyGroupUpdated(NotificationGroup group) {
            SwingUtilities.invokeLater(() -> {
                for (GroupChangeListener listener : listeners) {
                    try {
                        listener.onGroupUpdated(group);
                    } catch (Exception e) {
                        System.err.println("Error en listener de grupo actualizado: " + e.getMessage());
                    }
                }
            });
        }
    }

    // =========================================================================
    // 3. USER_SELECTOR_COMPONENT - SELECTOR DE USUARIOS PARA GRUPOS
    // =========================================================================
    
    /**
     * Componente para seleccionar usuarios al crear/editar grupos
     */
    public static class UserSelectorComponent extends JPanel {
        
        private final DefaultListModel<UserInfo> availableUsersModel;
        private final DefaultListModel<UserInfo> selectedUsersModel;
        private final JList<UserInfo> availableUsersList;
        private final JList<UserInfo> selectedUsersList;
        private final JTextField searchField;
        private final JComboBox<String> roleFilterCombo;
        
        private List<UserInfo> allUsers;
        private final Connection connection;
        
        public UserSelectorComponent() {
            this.connection = Conexion.getInstancia().verificarConexion();
            this.allUsers = new ArrayList<>();
            
            // Inicializar modelos
            this.availableUsersModel = new DefaultListModel<>();
            this.selectedUsersModel = new DefaultListModel<>();
            
            // Inicializar listas
            this.availableUsersList = new JList<>(availableUsersModel);
            this.selectedUsersList = new JList<>(selectedUsersModel);
            
            // Inicializar filtros
            this.searchField = new JTextField();
            this.roleFilterCombo = new JComboBox<>(new String[]{"Todos", "Administrador", "Preceptor", "Profesor", "Alumno", "ATTP"});
            
            initializeComponent();
            loadUsers();
        }
        
        private void initializeComponent() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createTitledBorder("Seleccionar Usuarios"));
            
            // Panel de filtros
            JPanel filterPanel = new JPanel(new BorderLayout(5, 5));
            filterPanel.add(new JLabel("Buscar:"), BorderLayout.WEST);
            filterPanel.add(searchField, BorderLayout.CENTER);
            filterPanel.add(roleFilterCombo, BorderLayout.EAST);
            
            // Panel principal con listas
            JPanel listsPanel = new JPanel(new BorderLayout(10, 10));
            
            // Lista de usuarios disponibles
            JPanel availablePanel = new JPanel(new BorderLayout());
            availablePanel.setBorder(BorderFactory.createTitledBorder("Usuarios Disponibles"));
            availableUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            availablePanel.add(new JScrollPane(availableUsersList), BorderLayout.CENTER);
            
            // Botones de control
            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            
            JButton addButton = new JButton("Agregar →");
            JButton removeButton = new JButton("← Quitar");
            JButton addAllButton = new JButton("Agregar Todos →");
            JButton removeAllButton = new JButton("← Quitar Todos");
            
            buttonsPanel.add(Box.createVerticalGlue());
            buttonsPanel.add(addButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(removeButton);
            buttonsPanel.add(Box.createVerticalStrut(10));
            buttonsPanel.add(addAllButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(removeAllButton);
            buttonsPanel.add(Box.createVerticalGlue());
            
            // Lista de usuarios seleccionados
            JPanel selectedPanel = new JPanel(new BorderLayout());
            selectedPanel.setBorder(BorderFactory.createTitledBorder("Usuarios Seleccionados"));
            selectedUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            selectedPanel.add(new JScrollPane(selectedUsersList), BorderLayout.CENTER);
            
            // Ensamblar layout
            listsPanel.add(availablePanel, BorderLayout.WEST);
            listsPanel.add(buttonsPanel, BorderLayout.CENTER);
            listsPanel.add(selectedPanel, BorderLayout.EAST);
            
            add(filterPanel, BorderLayout.NORTH);
            add(listsPanel, BorderLayout.CENTER);
            
            // Configurar listeners
            setupListeners(addButton, removeButton, addAllButton, removeAllButton);
        }
        
        private void setupListeners(JButton addButton, JButton removeButton, 
                JButton addAllButton, JButton removeAllButton) {
            
            // Filtros
            searchField.addActionListener(e -> filterUsers());
            roleFilterCombo.addActionListener(e -> filterUsers());
            
            // Botones
            addButton.addActionListener(e -> {
                List<UserInfo> selected = availableUsersList.getSelectedValuesList();
                for (UserInfo user : selected) {
                    if (!selectedUsersModel.contains(user)) {
                        selectedUsersModel.addElement(user);
                        availableUsersModel.removeElement(user);
                    }
                }
            });
            
            removeButton.addActionListener(e -> {
                List<UserInfo> selected = selectedUsersList.getSelectedValuesList();
                for (UserInfo user : selected) {
                    selectedUsersModel.removeElement(user);
                    if (!availableUsersModel.contains(user) && matchesFilter(user)) {
                        availableUsersModel.addElement(user);
                    }
                }
            });
            
            addAllButton.addActionListener(e -> {
                while (availableUsersModel.getSize() > 0) {
                    UserInfo user = availableUsersModel.getElementAt(0);
                    selectedUsersModel.addElement(user);
                    availableUsersModel.removeElement(user);
                }
            });
            
            removeAllButton.addActionListener(e -> {
                selectedUsersModel.clear();
                filterUsers(); // Recargar usuarios disponibles
            });
        }
        
        private void loadUsers() {
            allUsers.clear();
            
            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, r.nombre as rol_nombre
                FROM usuarios u
                LEFT JOIN roles r ON u.rol_id = r.id
                WHERE u.activo = 1
                ORDER BY u.apellido, u.nombre
            """;
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    UserInfo user = new UserInfo();
                    user.id = rs.getInt("id");
                    user.nombre = rs.getString("nombre");
                    user.apellido = rs.getString("apellido");
                    user.email = rs.getString("email");
                    user.rol = rs.getString("rol_nombre");
                    
                    allUsers.add(user);
                }
                
                filterUsers();
                
            } catch (SQLException e) {
                System.err.println("Error cargando usuarios: " + e.getMessage());
            }
        }
        
        private void filterUsers() {
            availableUsersModel.clear();
            
            String searchText = searchField.getText().toLowerCase().trim();
            String selectedRole = (String) roleFilterCombo.getSelectedItem();
            
            for (UserInfo user : allUsers) {
                if (!selectedUsersModel.contains(user) && matchesFilter(user, searchText, selectedRole)) {
                    availableUsersModel.addElement(user);
                }
            }
        }
        
        private boolean matchesFilter(UserInfo user) {
            String searchText = searchField.getText().toLowerCase().trim();
            String selectedRole = (String) roleFilterCombo.getSelectedItem();
            return matchesFilter(user, searchText, selectedRole);
        }
        
        private boolean matchesFilter(UserInfo user, String searchText, String selectedRole) {
            // Filtro de texto
            if (!searchText.isEmpty()) {
                String fullName = (user.nombre + " " + user.apellido).toLowerCase();
                if (!fullName.contains(searchText) && !user.email.toLowerCase().contains(searchText)) {
                    return false;
                }
            }
            
            // Filtro de rol
            if (!"Todos".equals(selectedRole) && !selectedRole.equals(user.rol)) {
                return false;
            }
            
            return true;
        }
        
        /**
         * Obtiene la lista de usuarios seleccionados
         */
        public List<UserInfo> getSelectedUsers() {
            List<UserInfo> selected = new ArrayList<>();
            for (int i = 0; i < selectedUsersModel.getSize(); i++) {
                selected.add(selectedUsersModel.getElementAt(i));
            }
            return selected;
        }
        
        /**
         * Establece usuarios preseleccionados
         */
        public void setSelectedUsers(List<UserInfo> users) {
            selectedUsersModel.clear();
            for (UserInfo user : users) {
                selectedUsersModel.addElement(user);
            }
            filterUsers(); // Actualizar disponibles
        }
        
        /**
         * Limpia la selección
         */
        public void clearSelection() {
            selectedUsersModel.clear();
            filterUsers();
        }
    }

    // =========================================================================
    // 4. CLASES DE DATOS Y UTILIDADES
    // =========================================================================
    
    /**
     * Clase para información básica de usuario
     */
    public static class UserInfo {
        public int id;
        public String nombre;
        public String apellido;
        public String email;
        public String rol;
        
        @Override
        public String toString() {
            return apellido + ", " + nombre + " (" + rol + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            UserInfo userInfo = (UserInfo) obj;
            return id == userInfo.id;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    // =========================================================================
    // MÉTODOS ESTÁTICOS DE ACCESO RÁPIDO
    // =========================================================================
    
    /**
     * Obtiene la instancia del servicio de grupos
     */
    public static NotificationGroupService getGroupService() {
        return NotificationGroupService.getInstance();
    }
    
    /**
     * Obtiene la instancia del manager de grupos
     */
    public static NotificationGroupManager getGroupManager() {
        return NotificationGroupManager.getInstance();
    }
    
    /**
     * Crea un nuevo componente selector de usuarios
     */
    public static UserSelectorComponent createUserSelector() {
        return new UserSelectorComponent();
    }
}
