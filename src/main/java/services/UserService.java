package main.java.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import main.java.database.Conexion;

/**
 * UserService COMPLETAMENTE REESCRITO v3.0 Servicio para gestión de usuarios y
 * consultas relacionadas CON SOPORTE COMPLETO PARA SELECTOR JERÁRQUICO DE
 * DESTINATARIOS
 *
 * Características: - Obtención de usuarios organizados jerárquicamente por rol
 * - Agrupación de estudiantes por curso y división - Búsquedas avanzadas con
 * múltiples criterios - Validación de usuarios y permisos - Estadísticas
 * detalladas de usuarios - Soporte para grupos y filtros
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 3.0 - Con soporte completo para selector jerárquico
 */
public class UserService {

    private Connection connection;

    public UserService() {
        this.connection = Conexion.getInstancia().verificarConexion();
        System.out.println("✅ UserService v3.0 inicializado con soporte jerárquico");
    }

    // ========================================
    // MÉTODOS PARA SELECTOR JERÁRQUICO - NUEVOS
    // ========================================
    /**
     * NUEVO: Obtiene todos los usuarios organizados jerárquicamente por rol
     * Estructura: Map<RolId, Map<"info", RoleInfo>, Map<"usuarios",
     * List<UserInfo>>>
     */
    public Map<Integer, HierarchicalRoleData> obtenerUsuariosJerarquicos(int usuarioExcluir) {
        Map<Integer, HierarchicalRoleData> jerarquia = new HashMap<>();

        try {
            System.out.println("🏗️ Construyendo jerarquía de usuarios (excluyendo: " + usuarioExcluir + ")");

            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division, u.activo,
                       r.nombre as rol_nombre,
                       CASE 
                           WHEN u.rol = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                           THEN CONCAT(u.anio, '°', u.division)
                           ELSE NULL
                       END as curso_completo
                FROM usuarios u 
                INNER JOIN roles r ON u.rol = r.id 
                WHERE u.activo = 1 AND u.id != ?
                ORDER BY r.nombre, u.anio, u.division, u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioExcluir);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int rolId = rs.getInt("rol");
                String rolNombre = rs.getString("rol_nombre");

                // Inicializar rol si no existe
                if (!jerarquia.containsKey(rolId)) {
                    HierarchicalRoleData roleData = new HierarchicalRoleData();
                    roleData.setRolId(rolId);
                    roleData.setRolNombre(rolNombre);
                    roleData.setUsuarios(new ArrayList<>());
                    roleData.setCursos(new HashMap<>());
                    jerarquia.put(rolId, roleData);
                }

                // Crear información del usuario
                UserInfo userInfo = new UserInfo();
                userInfo.setId(rs.getInt("id"));
                userInfo.setNombre(rs.getString("nombre"));
                userInfo.setApellido(rs.getString("apellido"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setRol(rolId);
                userInfo.setRolNombre(rolNombre);
                userInfo.setAnio(rs.getInt("anio"));
                userInfo.setDivision(rs.getString("division"));
                userInfo.setCurso(rs.getString("curso_completo"));

                HierarchicalRoleData roleData = jerarquia.get(rolId);

                // Si es estudiante (rol 4), agrupar por curso
                if (rolId == 4 && userInfo.getCurso() != null && !userInfo.getCurso().isEmpty()) {
                    String curso = userInfo.getCurso();

                    if (!roleData.getCursos().containsKey(curso)) {
                        CourseInfo courseInfo = new CourseInfo();
                        courseInfo.setNombre(curso);
                        courseInfo.setAnio(rs.getInt("anio"));
                        courseInfo.setDivision(rs.getString("division"));
                        courseInfo.setEstudiantes(new ArrayList<>());
                        roleData.getCursos().put(curso, courseInfo);
                    }

                    roleData.getCursos().get(curso).getEstudiantes().add(userInfo);
                } else {
                    // Para otros roles, agregar directamente
                    roleData.getUsuarios().add(userInfo);
                }
            }

            // Calcular estadísticas por rol
            for (HierarchicalRoleData roleData : jerarquia.values()) {
                int totalUsuarios = roleData.getUsuarios().size();

                // Para estudiantes, contar también los que están en cursos
                if (roleData.getRolId() == 4) {
                    for (CourseInfo courseInfo : roleData.getCursos().values()) {
                        totalUsuarios += courseInfo.getEstudiantes().size();
                        courseInfo.setCantidadAlumnos(courseInfo.getEstudiantes().size());
                    }
                }

                roleData.setTotalUsuarios(totalUsuarios);
            }

            System.out.println("✅ Jerarquía construida - Roles: " + jerarquia.size());
            jerarquia.forEach((rolId, data) -> {
                System.out.println("  📋 " + data.getRolNombre() + ": " + data.getTotalUsuarios() + " usuarios");
                if (rolId == 4 && !data.getCursos().isEmpty()) {
                    System.out.println("      📚 Cursos: " + data.getCursos().size());
                }
            });

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios jerárquicos: " + e.getMessage());
            e.printStackTrace();
        }

        return jerarquia;
    }

    /**
     * NUEVO: Obtiene estudiantes organizados por curso específicamente
     */
    public Map<String, List<UserInfo>> obtenerEstudiantesAgrupadosPorCurso() {
        Map<String, List<UserInfo>> estudiantesPorCurso = new HashMap<>();

        try {
            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.anio, u.division,
                       CONCAT(u.anio, '°', u.division) as curso_nombre
                FROM usuarios u 
                WHERE u.rol = 4 AND u.activo = 1 
                AND u.anio IS NOT NULL AND u.division IS NOT NULL
                ORDER BY u.anio, u.division, u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String cursoNombre = rs.getString("curso_nombre");

                UserInfo userInfo = new UserInfo();
                userInfo.setId(rs.getInt("id"));
                userInfo.setNombre(rs.getString("nombre"));
                userInfo.setApellido(rs.getString("apellido"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setRol(4); // Estudiante
                userInfo.setRolNombre("Estudiante");
                userInfo.setAnio(rs.getInt("anio"));
                userInfo.setDivision(rs.getString("division"));
                userInfo.setCurso(cursoNombre);

                estudiantesPorCurso.computeIfAbsent(cursoNombre, k -> new ArrayList<>()).add(userInfo);
            }

            System.out.println("📚 Estudiantes agrupados por curso - Cursos: " + estudiantesPorCurso.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo estudiantes por curso: " + e.getMessage());
            e.printStackTrace();
        }

        return estudiantesPorCurso;
    }

    /**
     * NUEVO: Obtiene todos los cursos disponibles con información detallada
     */
    public List<CourseInfo> obtenerTodosLosCursos() {
        List<CourseInfo> cursos = new ArrayList<>();

        try {
            String query = """
                SELECT DISTINCT u.anio, u.division, 
                       COUNT(u.id) as cantidad_alumnos,
                       CONCAT(u.anio, '°', u.division) as curso_nombre
                FROM usuarios u
                WHERE u.rol = 4 AND u.activo = 1 
                AND u.anio IS NOT NULL AND u.division IS NOT NULL
                GROUP BY u.anio, u.division
                ORDER BY u.anio, u.division
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CourseInfo courseInfo = new CourseInfo();
                courseInfo.setAnio(rs.getInt("anio"));
                courseInfo.setDivision(rs.getString("division"));
                courseInfo.setNombre(rs.getString("curso_nombre"));
                courseInfo.setCantidadAlumnos(rs.getInt("cantidad_alumnos"));

                cursos.add(courseInfo);
            }

            System.out.println("📋 Cursos encontrados: " + cursos.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo cursos: " + e.getMessage());
            e.printStackTrace();
        }

        return cursos;
    }

    /**
     * NUEVO: Busca usuarios con criterios múltiples para el selector jerárquico
     */
    public List<UserInfo> buscarUsuariosParaSelector(String searchTerm, int usuarioExcluir,
            List<Integer> rolesIncluir, int limite) {
        List<UserInfo> usuarios = new ArrayList<>();

        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("""
                SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division,
                       r.nombre as rol_nombre,
                       CASE 
                           WHEN u.rol = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                           THEN CONCAT(u.anio, '°', u.division)
                           ELSE NULL
                       END as curso_completo
                FROM usuarios u 
                INNER JOIN roles r ON u.rol = r.id 
                WHERE u.activo = 1 AND u.id != ?
                """);

            List<Object> parameters = new ArrayList<>();
            parameters.add(usuarioExcluir);

            // Filtro por roles si se especifica
            if (rolesIncluir != null && !rolesIncluir.isEmpty()) {
                String rolesPlaceholders = rolesIncluir.stream()
                        .map(r -> "?")
                        .collect(Collectors.joining(","));
                queryBuilder.append(" AND u.rol IN (").append(rolesPlaceholders).append(")");
                parameters.addAll(rolesIncluir);
            }

            // Filtro de búsqueda
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                queryBuilder.append("""
                     AND (
                        u.nombre LIKE ? OR 
                        u.apellido LIKE ? OR 
                        u.email LIKE ? OR
                        CONCAT(u.nombre, ' ', u.apellido) LIKE ? OR
                        CONCAT(u.apellido, ', ', u.nombre) LIKE ? OR
                        r.nombre LIKE ?
                    )
                    """);
                String searchPattern = "%" + searchTerm.trim() + "%";
                for (int i = 0; i < 6; i++) {
                    parameters.add(searchPattern);
                }
            }

            queryBuilder.append(" ORDER BY r.nombre, u.anio, u.division, u.apellido, u.nombre");

            if (limite > 0) {
                queryBuilder.append(" LIMIT ?");
                parameters.add(limite);
            }

            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(rs.getInt("id"));
                userInfo.setNombre(rs.getString("nombre"));
                userInfo.setApellido(rs.getString("apellido"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setRol(rs.getInt("rol"));
                userInfo.setRolNombre(rs.getString("rol_nombre"));
                userInfo.setAnio(rs.getInt("anio"));
                userInfo.setDivision(rs.getString("division"));
                userInfo.setCurso(rs.getString("curso_completo"));

                usuarios.add(userInfo);
            }

            System.out.println("🔍 Búsqueda usuarios selector - Encontrados: " + usuarios.size()
                    + " (término: '" + searchTerm + "')");

        } catch (SQLException e) {
            System.err.println("Error buscando usuarios para selector: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    /**
     * NUEVO: Obtiene información resumida de múltiples usuarios por IDs
     */
    public Map<Integer, UserInfo> obtenerUsuariosPorIds(List<Integer> userIds) {
        Map<Integer, UserInfo> usuarios = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return usuarios;
        }

        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("""
                SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division,
                       r.nombre as rol_nombre,
                       CASE 
                           WHEN u.rol = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                           THEN CONCAT(u.anio, '°', u.division)
                           ELSE NULL
                       END as curso_completo
                FROM usuarios u 
                INNER JOIN roles r ON u.rol = r.id 
                WHERE u.id IN (
                """);

            for (int i = 0; i < userIds.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append("?");
            }
            queryBuilder.append(") ORDER BY r.nombre, u.apellido, u.nombre");

            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < userIds.size(); i++) {
                ps.setInt(i + 1, userIds.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(rs.getInt("id"));
                userInfo.setNombre(rs.getString("nombre"));
                userInfo.setApellido(rs.getString("apellido"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setRol(rs.getInt("rol"));
                userInfo.setRolNombre(rs.getString("rol_nombre"));
                userInfo.setAnio(rs.getInt("anio"));
                userInfo.setDivision(rs.getString("division"));
                userInfo.setCurso(rs.getString("curso_completo"));

                usuarios.put(userInfo.getId(), userInfo);
            }

            System.out.println("📋 Usuarios obtenidos por IDs: " + usuarios.size() + "/" + userIds.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios por IDs: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    /**
     * NUEVO: Obtiene estadísticas detalladas de usuarios por rol
     */
    public Map<Integer, RoleStats> obtenerEstadisticasPorRol() {
        Map<Integer, RoleStats> estadisticas = new HashMap<>();

        try {
            String query = """
                SELECT r.id as rol_id, r.nombre as rol_nombre,
                       COUNT(u.id) as total_usuarios,
                       COUNT(CASE WHEN u.activo = 1 THEN 1 END) as usuarios_activos,
                       COUNT(CASE WHEN u.activo = 0 THEN 1 END) as usuarios_inactivos
                FROM roles r
                LEFT JOIN usuarios u ON r.id = u.rol
                GROUP BY r.id, r.nombre
                ORDER BY r.id
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RoleStats stats = new RoleStats();
                stats.setRolId(rs.getInt("rol_id"));
                stats.setRolNombre(rs.getString("rol_nombre"));
                stats.setTotalUsuarios(rs.getInt("total_usuarios"));
                stats.setUsuariosActivos(rs.getInt("usuarios_activos"));
                stats.setUsuariosInactivos(rs.getInt("usuarios_inactivos"));

                estadisticas.put(stats.getRolId(), stats);
            }

            // Estadísticas adicionales para estudiantes (cursos)
            RoleStats estudiantesStats = estadisticas.get(4);
            if (estudiantesStats != null) {
                String queryCursos = """
                    SELECT COUNT(DISTINCT CONCAT(anio, '-', division)) as total_cursos
                    FROM usuarios 
                    WHERE rol = 4 AND activo = 1 
                    AND anio IS NOT NULL AND division IS NOT NULL
                    """;

                PreparedStatement psCursos = connection.prepareStatement(queryCursos);
                ResultSet rsCursos = psCursos.executeQuery();

                if (rsCursos.next()) {
                    estudiantesStats.setTotalCursos(rsCursos.getInt("total_cursos"));
                }
            }

            System.out.println("📊 Estadísticas por rol calculadas: " + estadisticas.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo estadísticas por rol: " + e.getMessage());
            e.printStackTrace();
        }

        return estadisticas;
    }

    // ========================================
    // MÉTODOS ORIGINALES MEJORADOS
    // ========================================
    /**
     * Obtiene todos los usuarios de un rol específico MEJORADO: Con mejor
     * ordenamiento y información adicional
     */
    public List<Integer> obtenerUsuariosPorRol(int rolId) {
        List<Integer> usuarios = new ArrayList<>();

        try {
            String query = """
                SELECT u.id 
                FROM usuarios u
                WHERE u.rol = ? AND u.activo = 1 
                ORDER BY u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, rolId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                usuarios.add(rs.getInt("id"));
            }

            System.out.println("👥 Usuarios por rol " + rolId + ": " + usuarios.size());

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios por rol: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    /**
     * Obtiene información completa de un usuario MEJORADO: Con información de
     * curso para estudiantes
     */
    public UserInfo obtenerInfoUsuario(int userId) {
        try {
            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division, u.activo,
                       r.nombre as rol_nombre,
                       CASE 
                           WHEN u.rol = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                           THEN CONCAT(u.anio, '°', u.division)
                           ELSE NULL
                       END as curso_completo
                FROM usuarios u
                INNER JOIN roles r ON u.rol = r.id
                WHERE u.id = ?
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserInfo info = new UserInfo();
                info.setId(rs.getInt("id"));
                info.setNombre(rs.getString("nombre"));
                info.setApellido(rs.getString("apellido"));
                info.setEmail(rs.getString("email"));
                info.setRol(rs.getInt("rol"));
                info.setRolNombre(rs.getString("rol_nombre"));
                info.setAnio(rs.getInt("anio"));
                info.setDivision(rs.getString("division"));
                info.setCurso(rs.getString("curso_completo"));
                info.setActivo(rs.getBoolean("activo"));
                return info;
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo info de usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtiene usuarios agrupados por rol para el árbol jerárquico MEJORADO:
     * Optimizado para el nuevo sistema jerárquico
     */
    public Map<Integer, List<UserInfo>> obtenerUsuariosAgrupadosPorRol() {
        Map<Integer, List<UserInfo>> usuariosPorRol = new HashMap<>();

        try {
            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division,
                       r.nombre as rol_nombre,
                       CASE 
                           WHEN u.rol = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                           THEN CONCAT(u.anio, '°', u.division)
                           ELSE NULL
                       END as curso_completo
                FROM usuarios u 
                INNER JOIN roles r ON u.rol = r.id 
                WHERE u.activo = 1 
                ORDER BY u.rol, u.anio, u.division, u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int rolId = rs.getInt("rol");

                UserInfo userInfo = new UserInfo();
                userInfo.setId(rs.getInt("id"));
                userInfo.setNombre(rs.getString("nombre"));
                userInfo.setApellido(rs.getString("apellido"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setRol(rolId);
                userInfo.setRolNombre(rs.getString("rol_nombre"));
                userInfo.setAnio(rs.getInt("anio"));
                userInfo.setDivision(rs.getString("division"));
                userInfo.setCurso(rs.getString("curso_completo"));

                usuariosPorRol.computeIfAbsent(rolId, k -> new ArrayList<>()).add(userInfo);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios agrupados: " + e.getMessage());
            e.printStackTrace();
        }

        return usuariosPorRol;
    }

    /**
     * Busca usuarios por nombre o apellido MEJORADO: Búsqueda más inteligente
     * con múltiples criterios
     */
    public List<UserInfo> buscarUsuarios(String searchTerm) {
        return buscarUsuariosParaSelector(searchTerm, -1, null, 50);
    }

    /**
     * Obtiene los alumnos de un curso específico
     */
    public List<Integer> obtenerAlumnosDeCurso(int anio, String division) {
        List<Integer> alumnos = new ArrayList<>();

        try {
            String query = """
                SELECT u.id 
                FROM usuarios u
                WHERE u.rol = 4 AND u.activo = 1 
                AND u.anio = ? AND u.division = ?
                ORDER BY u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, anio);
            ps.setString(2, division);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnos.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo alumnos del curso: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnos;
    }

    /**
     * Verifica si un usuario existe y está activo
     */
    public boolean existeUsuario(int userId) {
        try {
            String query = "SELECT COUNT(*) FROM usuarios WHERE id = ? AND activo = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando existencia de usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Obtiene el nombre completo de un usuario
     */
    public String obtenerNombreCompleto(int userId) {
        try {
            String query = "SELECT CONCAT(apellido, ', ', nombre) as nombre_completo FROM usuarios WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre completo: " + e.getMessage());
            e.printStackTrace();
        }

        return "Usuario #" + userId;
    }

    /**
     * NUEVO: Valida múltiples IDs de usuario de forma optimizada
     */
    public Map<Integer, Boolean> validarUsuarios(List<Integer> userIds) {
        Map<Integer, Boolean> resultados = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return resultados;
        }

        try {
            // Inicializar todos como false
            for (Integer userId : userIds) {
                resultados.put(userId, false);
            }

            StringBuilder queryBuilder = new StringBuilder("SELECT id FROM usuarios WHERE activo = 1 AND id IN (");
            for (int i = 0; i < userIds.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append("?");
            }
            queryBuilder.append(")");

            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < userIds.size(); i++) {
                ps.setInt(i + 1, userIds.get(i));
            }

            ResultSet rs = ps.executeQuery();

            // Marcar como true los que existen
            while (rs.next()) {
                resultados.put(rs.getInt("id"), true);
            }

            int validados = (int) resultados.values().stream().mapToLong(b -> b ? 1 : 0).sum();
            System.out.println("✅ Usuarios validados: " + validados + "/" + userIds.size());

        } catch (SQLException e) {
            System.err.println("Error validando usuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return resultados;
    }

    /**
     * NUEVO: Obtiene información básica de múltiples usuarios optimizada
     */
    public Map<Integer, String> obtenerNombresUsuarios(List<Integer> userIds) {
        Map<Integer, String> nombres = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return nombres;
        }

        try {
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT id, CONCAT(apellido, ', ', nombre) as nombre_completo FROM usuarios WHERE id IN ("
            );
            for (int i = 0; i < userIds.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append("?");
            }
            queryBuilder.append(")");

            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < userIds.size(); i++) {
                ps.setInt(i + 1, userIds.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                nombres.put(rs.getInt("id"), rs.getString("nombre_completo"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo nombres de usuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return nombres;
    }

    // ========================================
    // CLASES INTERNAS PARA DATOS JERÁRQUICOS - NUEVAS
    // ========================================
    /**
     * NUEVA: Clase para datos jerárquicos de roles
     */
    public static class HierarchicalRoleData {

        private int rolId;
        private String rolNombre;
        private int totalUsuarios;
        private List<UserInfo> usuarios;
        private Map<String, CourseInfo> cursos; // Para estudiantes

        public HierarchicalRoleData() {
            this.usuarios = new ArrayList<>();
            this.cursos = new HashMap<>();
        }

        // Getters y Setters
        public int getRolId() {
            return rolId;
        }

        public void setRolId(int rolId) {
            this.rolId = rolId;
        }

        public String getRolNombre() {
            return rolNombre;
        }

        public void setRolNombre(String rolNombre) {
            this.rolNombre = rolNombre;
        }

        public int getTotalUsuarios() {
            return totalUsuarios;
        }

        public void setTotalUsuarios(int totalUsuarios) {
            this.totalUsuarios = totalUsuarios;
        }

        public List<UserInfo> getUsuarios() {
            return usuarios;
        }

        public void setUsuarios(List<UserInfo> usuarios) {
            this.usuarios = usuarios;
        }

        public Map<String, CourseInfo> getCursos() {
            return cursos;
        }

        public void setCursos(Map<String, CourseInfo> cursos) {
            this.cursos = cursos;
        }

        public String getDisplayName() {
            return getRolIcon(rolId) + " " + rolNombre + " (" + totalUsuarios + ")";
        }

        private String getRolIcon(int rolId) {
            switch (rolId) {
                case 1:
                    return "👑"; // Administrador
                case 2:
                    return "👨‍🏫"; // Preceptor
                case 3:
                    return "👩‍🏫"; // Profesor
                case 4:
                    return "🎓"; // Estudiante
                case 5:
                    return "🔧"; // ATTP
                default:
                    return "👤";
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    /**
     * NUEVA: Clase para estadísticas de roles
     */
    public static class RoleStats {

        private int rolId;
        private String rolNombre;
        private int totalUsuarios;
        private int usuariosActivos;
        private int usuariosInactivos;
        private int totalCursos; // Solo para estudiantes

        // Getters y Setters
        public int getRolId() {
            return rolId;
        }

        public void setRolId(int rolId) {
            this.rolId = rolId;
        }

        public String getRolNombre() {
            return rolNombre;
        }

        public void setRolNombre(String rolNombre) {
            this.rolNombre = rolNombre;
        }

        public int getTotalUsuarios() {
            return totalUsuarios;
        }

        public void setTotalUsuarios(int totalUsuarios) {
            this.totalUsuarios = totalUsuarios;
        }

        public int getUsuariosActivos() {
            return usuariosActivos;
        }

        public void setUsuariosActivos(int usuariosActivos) {
            this.usuariosActivos = usuariosActivos;
        }

        public int getUsuariosInactivos() {
            return usuariosInactivos;
        }

        public void setUsuariosInactivos(int usuariosInactivos) {
            this.usuariosInactivos = usuariosInactivos;
        }

        public int getTotalCursos() {
            return totalCursos;
        }

        public void setTotalCursos(int totalCursos) {
            this.totalCursos = totalCursos;
        }

        public double getPorcentajeActivos() {
            return totalUsuarios > 0 ? (usuariosActivos * 100.0) / totalUsuarios : 0;
        }

        @Override
        public String toString() {
            return String.format("%s: %d usuarios (%d activos, %.1f%%)",
                    rolNombre, totalUsuarios, usuariosActivos, getPorcentajeActivos());
        }
    }

    // ========================================
    // CLASES INTERNAS EXISTENTES MEJORADAS
    // ========================================
    /**
     * Clase para información de usuario MEJORADA
     */
    public static class UserInfo {

        private int id;
        private String nombre;
        private String apellido;
        private String email;
        private int rol;
        private String rolNombre;
        private boolean activo;
        private int anio;
        private String division;
        private String curso; // Para estudiantes

        // Constructores
        public UserInfo() {
        }

        public UserInfo(int id, String nombre, String apellido, int rol) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.rol = rol;
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

        public String getApellido() {
            return apellido;
        }

        public void setApellido(String apellido) {
            this.apellido = apellido;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getRol() {
            return rol;
        }

        public void setRol(int rol) {
            this.rol = rol;
        }

        public String getRolNombre() {
            return rolNombre;
        }

        public void setRolNombre(String rolNombre) {
            this.rolNombre = rolNombre;
        }

        public boolean isActivo() {
            return activo;
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
        }

        public int getAnio() {
            return anio;
        }

        public void setAnio(int anio) {
            this.anio = anio;
        }

        public String getDivision() {
            return division;
        }

        public void setDivision(String division) {
            this.division = division;
        }

        public String getCurso() {
            return curso;
        }

        public void setCurso(String curso) {
            this.curso = curso;
        }

        // Métodos de utilidad
        public String getNombreCompleto() {
            return apellido + ", " + nombre;
        }

        public String getNombreCompletoConRol() {
            return getNombreCompleto() + " (" + (rolNombre != null ? rolNombre : "Rol " + rol) + ")";
        }

        public String getDisplayNameForSelector() {
            String base = getNombreCompleto();
            if (rol == 4 && curso != null && !curso.isEmpty()) {
                base += " (" + curso + ")";
            }
            return base;
        }

        public String getRolWithIcon() {
            String icon = getRolIcon(rol);
            return icon + " " + (rolNombre != null ? rolNombre : "Usuario");
        }

        private String getRolIcon(int rolId) {
            switch (rolId) {
                case 1:
                    return "👑";
                case 2:
                    return "👨‍🏫";
                case 3:
                    return "👩‍🏫";
                case 4:
                    return "🎓";
                case 5:
                    return "🔧";
                default:
                    return "👤";
            }
        }

        @Override
        public String toString() {
            return getDisplayNameForSelector();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            UserInfo userInfo = (UserInfo) obj;
            return id == userInfo.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }

    /**
     * Clase para información de curso MEJORADA
     */
    public static class CourseInfo {

        private int id;
        private int anio;
        private String division;
        private String nombre;
        private int cantidadAlumnos;
        private List<UserInfo> estudiantes;

        // Constructores
        public CourseInfo() {
            this.estudiantes = new ArrayList<>();
        }

        public CourseInfo(int anio, String division) {
            this();
            this.anio = anio;
            this.division = division;
            this.nombre = anio + "°" + division;
        }

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getAnio() {
            return anio;
        }

        public void setAnio(int anio) {
            this.anio = anio;
        }

        public String getDivision() {
            return division;
        }

        public void setDivision(String division) {
            this.division = division;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getCantidadAlumnos() {
            return cantidadAlumnos;
        }

        public void setCantidadAlumnos(int cantidadAlumnos) {
            this.cantidadAlumnos = cantidadAlumnos;
        }

        public List<UserInfo> getEstudiantes() {
            return estudiantes;
        }

        public void setEstudiantes(List<UserInfo> estudiantes) {
            this.estudiantes = estudiantes != null ? estudiantes : new ArrayList<>();
            this.cantidadAlumnos = this.estudiantes.size();
        }

        // Métodos de utilidad
        public String getDisplayName() {
            return "🎓 " + nombre + " (" + cantidadAlumnos + " estudiante"
                    + (cantidadAlumnos == 1 ? "" : "s") + ")";
        }

        public String getNombreConCantidad() {
            return nombre + " (" + cantidadAlumnos + " alumno" + (cantidadAlumnos == 1 ? "" : "s") + ")";
        }

        public List<Integer> getEstudiantesIds() {
            return estudiantes.stream()
                    .map(UserInfo::getId)
                    .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CourseInfo that = (CourseInfo) obj;
            return anio == that.anio
                    && (division != null ? division.equals(that.division) : that.division == null);
        }

        @Override
        public int hashCode() {
            return Objects.hash(anio, division);
        }
    }

    // ========================================
    // MÉTODOS DE ESTADÍSTICAS AVANZADAS - NUEVOS
    // ========================================
    /**
     * NUEVO: Obtiene estadísticas generales de usuarios con detalles
     * adicionales
     */
    public UserStats obtenerEstadisticasUsuarios() {
        UserStats stats = new UserStats();

        try {
            // Estadísticas básicas
            String queryBasica = """
               SELECT 
                   COUNT(*) as total,
                   COUNT(CASE WHEN activo = 1 THEN 1 END) as activos,
                   COUNT(CASE WHEN activo = 0 THEN 1 END) as inactivos,
                   COUNT(DISTINCT rol) as roles_diferentes
               FROM usuarios
               """;

            PreparedStatement psBasica = connection.prepareStatement(queryBasica);
            ResultSet rsBasica = psBasica.executeQuery();

            if (rsBasica.next()) {
                stats.setTotalUsuarios(rsBasica.getInt("total"));
                stats.setUsuariosActivos(rsBasica.getInt("activos"));
                stats.setUsuariosInactivos(rsBasica.getInt("inactivos"));
                stats.setRolesDiferentes(rsBasica.getInt("roles_diferentes"));
            }

            // Usuarios por rol
            String queryRoles = """
               SELECT r.id, r.nombre, COUNT(u.id) as cantidad
               FROM roles r
               LEFT JOIN usuarios u ON r.id = u.rol AND u.activo = 1
               GROUP BY r.id, r.nombre
               ORDER BY r.id
               """;

            PreparedStatement psRoles = connection.prepareStatement(queryRoles);
            ResultSet rsRoles = psRoles.executeQuery();

            while (rsRoles.next()) {
                stats.getUsuariosPorRol().put(rsRoles.getInt("id"), rsRoles.getInt("cantidad"));
            }

            // Estudiantes por curso
            String queryCursos = """
               SELECT CONCAT(anio, '°', division) as curso, COUNT(*) as cantidad
               FROM usuarios
               WHERE rol = 4 AND activo = 1 
               AND anio IS NOT NULL AND division IS NOT NULL
               GROUP BY anio, division
               ORDER BY anio, division
               """;

            PreparedStatement psCursos = connection.prepareStatement(queryCursos);
            ResultSet rsCursos = psCursos.executeQuery();

            while (rsCursos.next()) {
                stats.getEstudiantesPorCurso().put(rsCursos.getString("curso"), rsCursos.getInt("cantidad"));
            }

            // Estadísticas adicionales
            String queryAdicionales = """
               SELECT 
                   COUNT(CASE WHEN rol = 4 AND anio IS NOT NULL AND division IS NOT NULL THEN 1 END) as estudiantes_con_curso,
                   COUNT(CASE WHEN rol = 4 AND (anio IS NULL OR division IS NULL) THEN 1 END) as estudiantes_sin_curso,
                   COUNT(DISTINCT CASE WHEN rol = 4 AND anio IS NOT NULL AND division IS NOT NULL 
                                  THEN CONCAT(anio, '-', division) END) as total_cursos_activos
               FROM usuarios 
               WHERE activo = 1
               """;

            PreparedStatement psAdicionales = connection.prepareStatement(queryAdicionales);
            ResultSet rsAdicionales = psAdicionales.executeQuery();

            if (rsAdicionales.next()) {
                stats.setEstudiantesConCurso(rsAdicionales.getInt("estudiantes_con_curso"));
                stats.setEstudiantesSinCurso(rsAdicionales.getInt("estudiantes_sin_curso"));
                stats.setTotalCursosActivos(rsAdicionales.getInt("total_cursos_activos"));
            }

            System.out.println("📊 Estadísticas generales calculadas: " + stats);

        } catch (SQLException e) {
            System.err.println("Error obteniendo estadísticas de usuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * NUEVO: Obtiene usuarios recientes (últimos registrados)
     */
    public List<UserInfo> obtenerUsuariosRecientes(int limite) {
        List<UserInfo> usuarios = new ArrayList<>();

        try {
            String query = """
               SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division,
                      r.nombre as rol_nombre, u.fecha_registro,
                      CASE 
                          WHEN u.rol = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                          THEN CONCAT(u.anio, '°', u.division)
                          ELSE NULL
                      END as curso_completo
               FROM usuarios u 
               INNER JOIN roles r ON u.rol = r.id 
               WHERE u.activo = 1 
               ORDER BY u.fecha_registro DESC 
               LIMIT ?
               """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(rs.getInt("id"));
                userInfo.setNombre(rs.getString("nombre"));
                userInfo.setApellido(rs.getString("apellido"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setRol(rs.getInt("rol"));
                userInfo.setRolNombre(rs.getString("rol_nombre"));
                userInfo.setAnio(rs.getInt("anio"));
                userInfo.setDivision(rs.getString("division"));
                userInfo.setCurso(rs.getString("curso_completo"));

                usuarios.add(userInfo);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios recientes: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    /**
     * Clase para estadísticas de usuarios MEJORADA
     */
    public static class UserStats {

        private int totalUsuarios;
        private int usuariosActivos;
        private int usuariosInactivos;
        private int rolesDiferentes;
        private int estudiantesConCurso;
        private int estudiantesSinCurso;
        private int totalCursosActivos;
        private Map<Integer, Integer> usuariosPorRol;
        private Map<String, Integer> estudiantesPorCurso;

        public UserStats() {
            this.usuariosPorRol = new HashMap<>();
            this.estudiantesPorCurso = new HashMap<>();
        }

        // Getters y Setters
        public int getTotalUsuarios() {
            return totalUsuarios;
        }

        public void setTotalUsuarios(int totalUsuarios) {
            this.totalUsuarios = totalUsuarios;
        }

        public int getUsuariosActivos() {
            return usuariosActivos;
        }

        public void setUsuariosActivos(int usuariosActivos) {
            this.usuariosActivos = usuariosActivos;
        }

        public int getUsuariosInactivos() {
            return usuariosInactivos;
        }

        public void setUsuariosInactivos(int usuariosInactivos) {
            this.usuariosInactivos = usuariosInactivos;
        }

        public int getRolesDiferentes() {
            return rolesDiferentes;
        }

        public void setRolesDiferentes(int rolesDiferentes) {
            this.rolesDiferentes = rolesDiferentes;
        }

        public int getEstudiantesConCurso() {
            return estudiantesConCurso;
        }

        public void setEstudiantesConCurso(int estudiantesConCurso) {
            this.estudiantesConCurso = estudiantesConCurso;
        }

        public int getEstudiantesSinCurso() {
            return estudiantesSinCurso;
        }

        public void setEstudiantesSinCurso(int estudiantesSinCurso) {
            this.estudiantesSinCurso = estudiantesSinCurso;
        }

        public int getTotalCursosActivos() {
            return totalCursosActivos;
        }

        public void setTotalCursosActivos(int totalCursosActivos) {
            this.totalCursosActivos = totalCursosActivos;
        }

        public Map<Integer, Integer> getUsuariosPorRol() {
            return usuariosPorRol;
        }

        public void setUsuariosPorRol(Map<Integer, Integer> usuariosPorRol) {
            this.usuariosPorRol = usuariosPorRol;
        }

        public Map<String, Integer> getEstudiantesPorCurso() {
            return estudiantesPorCurso;
        }

        public void setEstudiantesPorCurso(Map<String, Integer> estudiantesPorCurso) {
            this.estudiantesPorCurso = estudiantesPorCurso;
        }

        // Métodos de utilidad
        public double getPorcentajeActivos() {
            return totalUsuarios > 0 ? (usuariosActivos * 100.0) / totalUsuarios : 0;
        }

        public double getPromedioUsuariosPorRol() {
            return rolesDiferentes > 0 ? (double) usuariosActivos / rolesDiferentes : 0;
        }

        public double getPromedioEstudiantesPorCurso() {
            return totalCursosActivos > 0 ? (double) estudiantesConCurso / totalCursosActivos : 0;
        }

        @Override
        public String toString() {
            return String.format("Total: %d, Activos: %d (%.1f%%), Roles: %d, Cursos: %d",
                    totalUsuarios, usuariosActivos, getPorcentajeActivos(),
                    rolesDiferentes, totalCursosActivos);
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD Y VALIDACIÓN
    // ========================================
    /**
     * NUEVO: Método para limpiar y validar datos de usuarios
     */
    public ValidationResult validarDatosUsuario(UserInfo usuario) {
        ValidationResult result = new ValidationResult();

        // Validaciones básicas
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            result.addError("El nombre es requerido");
        }

        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            result.addError("El apellido es requerido");
        }

        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            result.addError("El email es requerido");
        } else if (!isValidEmail(usuario.getEmail())) {
            result.addError("El formato del email no es válido");
        }

        if (usuario.getRol() < 1 || usuario.getRol() > 5) {
            result.addError("El rol debe estar entre 1 y 5");
        }

        // Validaciones específicas para estudiantes
        if (usuario.getRol() == 4) {
            if (usuario.getAnio() < 1 || usuario.getAnio() > 7) {
                result.addError("El año debe estar entre 1 y 7 para estudiantes");
            }
            if (usuario.getDivision() == null || usuario.getDivision().trim().isEmpty()) {
                result.addError("La división es requerida para estudiantes");
            }
        }

        return result;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Clase para resultados de validación
     */
    public static class ValidationResult {

        private List<String> errors;
        private List<String> warnings;

        public ValidationResult() {
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!errors.isEmpty()) {
                sb.append("Errores: ").append(String.join(", ", errors));
            }
            if (!warnings.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("Advertencias: ").append(String.join(", ", warnings));
            }
            return sb.length() > 0 ? sb.toString() : "Válido";
        }
    }

    /**
     * Cierra la conexión si es necesario
     */
    public void dispose() {
        // No cerrar la conexión ya que es compartida
        System.out.println("✅ UserService dispose() llamado");
    }

    // Agregar import necesario para Objects.hash()
    private static class Objects {

        public static int hash(Object... values) {
            return java.util.Arrays.hashCode(values);
        }
    }
}
