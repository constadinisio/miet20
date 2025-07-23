package main.java.services;

import main.java.database.Conexion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para gestionar la progresión anual de alumnos en el sistema educativo.
 */
public class ProgresionEscolarService {
    
    private Connection connection;
    
    // Configuración de criterios académicos
    private static final double PROMEDIO_MINIMO_PROMOCION = 6.0;
    private static final int MAX_MATERIAS_DESAPROBADAS = 2;
    private static final int MAX_FALTAS_PERMITIDAS = 30;
    private static final double ASISTENCIA_MINIMA = 0.75;
    
    public ProgresionEscolarService() {
        this.connection = Conexion.getInstancia().verificarConexion();
    }
    
    /**
     * Clase para representar información básica de un curso
     */
    public static class CursoInfo {
        public int id;
        public int anio;  
        public int division;
        public String turno;
        public String nombre;
        
        public CursoInfo() {
            this.nombre = "";
        }
        
        @Override
        public String toString() {
            return nombre;
        }
    }
    
    /**
     * Clase para representar un estudiante con su curso actual
     */
    public static class EstudianteConCurso {
        public int alumnoId;
        public String nombreCompleto;
        public String dni;
        public int cursoActualId;
        public String cursoActualNombre;
        public int anioActual;
        public int divisionActual;
        public String turnoActual;
        public double promedioGeneral;
        public int materiasAprobadas;
        public int materiasDesaprobadas;
        public int totalFaltas;
        public String estadoAcademico;
        public String observaciones;
        public boolean puedePromocionarse;
        
        public EstudianteConCurso() {
            this.puedePromocionarse = true;
            this.estadoAcademico = "SIN_EVALUAR";
            this.observaciones = "";
            this.totalFaltas = 0;
        }
    }
    
    /**
     * Obtiene todos los cursos activos agrupados por año
     */
    public Map<Integer, List<CursoInfo>> obtenerCursosPorAnio() {
        Map<Integer, List<CursoInfo>> cursosPorAnio = new HashMap<>();
        
        String sql = "SELECT id, anio, division, turno FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("🔍 DIAGNÓSTICO: Consultando cursos activos...");
            int totalCursos = 0;
            
            while (rs.next()) {
                CursoInfo curso = new CursoInfo();
                curso.id = rs.getInt("id");
                curso.anio = rs.getInt("anio");
                curso.division = rs.getInt("division");
                curso.turno = rs.getString("turno");
                curso.nombre = curso.anio + "° año " + curso.division + "° división";
                if (curso.turno != null && !curso.turno.isEmpty()) {
                    curso.nombre += " - " + curso.turno;
                }
                
                cursosPorAnio.computeIfAbsent(curso.anio, k -> new ArrayList<>()).add(curso);
                totalCursos++;
                
                System.out.println("📚 Curso encontrado: " + curso.nombre + " (ID: " + curso.id + ")");
            }
            
            System.out.println("✅ Total de cursos activos: " + totalCursos);
            System.out.println("📊 Cursos por año: " + cursosPorAnio.keySet());
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo cursos por año: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cursosPorAnio;
    }
    
    /**
     * Obtiene todos los estudiantes con sus cursos actuales
     */
    public List<EstudianteConCurso> obtenerAlumnosConCursos() {
        List<EstudianteConCurso> estudiantes = new ArrayList<>();
        
        // DIAGNÓSTICO DETALLADO PRIMERO
        realizarDiagnosticoDetallado();
        
        String sql = """
            SELECT DISTINCT 
                u.id as alumno_id,
                u.nombre,
                u.apellido,
                u.dni,
                c.id as curso_id,
                c.anio as anio_actual,
                c.division as division_actual,
                c.turno,
                ac.estado as estado_inscripcion
            FROM usuarios u
            INNER JOIN alumno_curso ac ON u.id = ac.alumno_id
            INNER JOIN cursos c ON ac.curso_id = c.id
            WHERE u.status = '1' 
            AND u.rol = 4
            AND ac.estado = 'activo'
            ORDER BY c.anio, c.division, u.apellido, u.nombre
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("🔍 DIAGNÓSTICO: Consultando estudiantes con sus cursos...");
            int totalEstudiantes = 0;
            
            while (rs.next()) {
                EstudianteConCurso estudiante = new EstudianteConCurso();
                estudiante.alumnoId = rs.getInt("alumno_id");
                estudiante.nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                estudiante.dni = rs.getString("dni");
                estudiante.cursoActualId = rs.getInt("curso_id");
                estudiante.anioActual = rs.getInt("anio_actual");
                estudiante.divisionActual = rs.getInt("division_actual");
                estudiante.turnoActual = rs.getString("turno");
                
                // Generar nombre del curso actual
                estudiante.cursoActualNombre = estudiante.anioActual + "° año " + 
                    estudiante.divisionActual + "° división";
                if (estudiante.turnoActual != null && !estudiante.turnoActual.isEmpty()) {
                    estudiante.cursoActualNombre += " - " + estudiante.turnoActual;
                }
                
                // Evaluar estado académico
                evaluarEstadoAcademico(estudiante);
                
                estudiantes.add(estudiante);
                totalEstudiantes++;
                
                System.out.println("👨‍🎓 Estudiante: " + estudiante.nombreCompleto + 
                    " - Curso: " + estudiante.cursoActualNombre);
            }
            
            System.out.println("✅ Total estudiantes encontrados: " + totalEstudiantes);
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo estudiantes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return estudiantes;
    }
    
    /**
     * Evalúa el estado académico de un estudiante
     */
    private void evaluarEstadoAcademico(EstudianteConCurso estudiante) {
        try {
            // Obtener promedio y materias del estudiante
            String sqlPromedio = """
                SELECT 
                    AVG(CASE WHEN F IS NOT NULL AND F > 0 THEN F ELSE 
                        (COALESCE(1B,0) + COALESCE(2B,0) + COALESCE(3B,0) + COALESCE(4B,0) + 
                         COALESCE(1C,0) + COALESCE(2C,0)) / 6 END) as promedio_general,
                    COUNT(*) as total_materias,
                    SUM(CASE WHEN 
                        (F IS NOT NULL AND F >= 6) OR 
                        ((COALESCE(1B,0) + COALESCE(2B,0) + COALESCE(3B,0) + COALESCE(4B,0) + 
                          COALESCE(1C,0) + COALESCE(2C,0)) / 6) >= 6 
                        THEN 1 ELSE 0 END) as materias_aprobadas
                FROM notas_bimestrales nb
                INNER JOIN materias m ON nb.materia_id = m.id
                WHERE nb.alumno_id = ? AND nb.curso_id = ?
            """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sqlPromedio)) {
                stmt.setInt(1, estudiante.alumnoId);
                stmt.setInt(2, estudiante.cursoActualId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        estudiante.promedioGeneral = rs.getDouble("promedio_general");
                        int totalMaterias = rs.getInt("total_materias");
                        estudiante.materiasAprobadas = rs.getInt("materias_aprobadas");
                        estudiante.materiasDesaprobadas = totalMaterias - estudiante.materiasAprobadas;
                        
                        // Determinar estado académico
                        if (estudiante.anioActual == 6) {
                            // Lógica para 6° año (egreso)
                            if (estudiante.materiasDesaprobadas == 0) {
                                estudiante.estadoAcademico = "EGRESADO";
                                estudiante.observaciones = "Egresa sin deuda académica";
                            } else if (estudiante.materiasDesaprobadas <= 2) {
                                estudiante.estadoAcademico = "CON_DEUDA";
                                estudiante.observaciones = "Egresa con " + estudiante.materiasDesaprobadas + " materia(s) adeudada(s)";
                            } else {
                                estudiante.estadoAcademico = "REPITENTE";
                                estudiante.observaciones = "Repite 6° año - " + estudiante.materiasDesaprobadas + " materias desaprobadas";
                                estudiante.puedePromocionarse = false;
                            }
                        } else {
                            // Lógica para 1° a 5° año
                            if (estudiante.promedioGeneral >= PROMEDIO_MINIMO_PROMOCION && 
                                estudiante.materiasDesaprobadas <= MAX_MATERIAS_DESAPROBADAS) {
                                estudiante.estadoAcademico = "REGULAR";
                                estudiante.observaciones = "Promociona a " + (estudiante.anioActual + 1) + "° año";
                            } else {
                                estudiante.estadoAcademico = "REPITENTE";
                                estudiante.observaciones = "Repite " + estudiante.anioActual + "° año - ";
                                if (estudiante.promedioGeneral < PROMEDIO_MINIMO_PROMOCION) {
                                    estudiante.observaciones += "Promedio insuficiente (" + 
                                        String.format("%.2f", estudiante.promedioGeneral) + ")";
                                }
                                if (estudiante.materiasDesaprobadas > MAX_MATERIAS_DESAPROBADAS) {
                                    estudiante.observaciones += " - " + estudiante.materiasDesaprobadas + " materias desaprobadas";
                                }
                                estudiante.puedePromocionarse = false;
                            }
                        }
                    } else {
                        // Sin notas registradas
                        estudiante.estadoAcademico = "SIN_DATOS";
                        estudiante.observaciones = "Sin calificaciones registradas";
                        estudiante.puedePromocionarse = false;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error evaluando estado académico del alumno " + estudiante.alumnoId + ": " + e.getMessage());
            estudiante.estadoAcademico = "ERROR";
            estudiante.observaciones = "Error al evaluar estado académico";
            estudiante.puedePromocionarse = false;
        }
    }
    
    /**
     * Progresa un alumno de un curso a otro, guardando el historial académico
     */
    public boolean progresarAlumno(int alumnoId, int cursoOrigenId, int cursoDestinoId) throws SQLException {
        connection.setAutoCommit(false);
        
        try {
            // 1. Obtener datos del estudiante antes de la progresión
            EstudianteConCurso estudiante = obtenerDatosEstudiantePorCurso(alumnoId, cursoOrigenId);
            if (estudiante == null) {
                throw new SQLException("No se encontró el estudiante en el curso especificado");
            }
            
            // 2. Determinar estado final según tipo de progresión
            String estadoFinal = determinarEstadoFinal(cursoOrigenId, cursoDestinoId, estudiante);
            
            // 3. Guardar en historial académico
            try {
                guardarHistorialAcademico(estudiante, estadoFinal);
            } catch (SQLException e) {
                System.err.println("⚠️ Advertencia: No se pudo guardar en historial académico: " + e.getMessage());
                // Continuar con la progresión aunque falle el historial
            }
            
            // 4. Desactivar inscripción actual
            String sqlDesactivar = "UPDATE alumno_curso SET estado = 'inactivo' WHERE alumno_id = ? AND curso_id = ? AND estado = 'activo'";
            try (PreparedStatement stmt = connection.prepareStatement(sqlDesactivar)) {
                stmt.setInt(1, alumnoId);
                stmt.setInt(2, cursoOrigenId);
                int updated = stmt.executeUpdate();
                
                if (updated == 0) {
                    throw new SQLException("No se pudo desactivar la inscripción actual del alumno");
                }
            }
            
            // 5. Crear nueva inscripción (solo si no es egreso)
            if (cursoDestinoId != -2) { // -2 significa egreso
                String sqlInscribir = "INSERT INTO alumno_curso (alumno_id, curso_id, estado) VALUES (?, ?, 'activo')";
                try (PreparedStatement stmt = connection.prepareStatement(sqlInscribir)) {
                    stmt.setInt(1, alumnoId);
                    stmt.setInt(2, cursoDestinoId);
                    stmt.executeUpdate();
                }
            }
            
            connection.commit();
            System.out.println("✅ Alumno " + alumnoId + " progresado del curso " + cursoOrigenId + 
                             " al curso " + cursoDestinoId + " - Estado: " + estadoFinal);
            return true;
            
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("❌ Error progresando alumno " + alumnoId + ": " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Busca o crea un curso para el año y división especificados
     */
    public int buscarProximoCurso(int anio, int division) throws SQLException {
        // Primero buscar si existe el curso
        String sqlBuscar = "SELECT id FROM cursos WHERE anio = ? AND division = ? AND estado = 'activo'";
        try (PreparedStatement stmt = connection.prepareStatement(sqlBuscar)) {
            stmt.setInt(1, anio);
            stmt.setInt(2, division);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        // Si no existe, crear el curso
        String sqlCrear = "INSERT INTO cursos (anio, division, estado) VALUES (?, ?, 'activo')";
        try (PreparedStatement stmt = connection.prepareStatement(sqlCrear, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, anio);
            stmt.setInt(2, division);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int nuevoCursoId = rs.getInt(1);
                    System.out.println("✅ Curso creado: " + anio + "° año " + division + "° división (ID: " + nuevoCursoId + ")");
                    return nuevoCursoId;
                }
            }
        }
        
        throw new SQLException("No se pudo crear el curso " + anio + "° año " + division + "° división");
    }
    
    /**
     * Realiza un diagnóstico detallado de las tablas para identificar por qué no aparecen estudiantes
     */
    private void realizarDiagnosticoDetallado() {
        System.out.println("🔬 === DIAGNÓSTICO DETALLADO DE BASE DE DATOS ===");
        
        try {
            // 1. Verificar total de usuarios
            String sqlUsuarios = "SELECT COUNT(*) as total, COUNT(CASE WHEN status = '1' THEN 1 END) as activos FROM usuarios";
            try (PreparedStatement stmt = connection.prepareStatement(sqlUsuarios);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("👥 USUARIOS: Total = " + rs.getInt("total") + 
                                     ", Activos = " + rs.getInt("activos"));
                }
            }
            
            // 2. Verificar total de registros en alumno_curso
            String sqlAlumnoCurso = "SELECT COUNT(*) as total, COUNT(CASE WHEN estado = 'activo' THEN 1 END) as activos FROM alumno_curso";
            try (PreparedStatement stmt = connection.prepareStatement(sqlAlumnoCurso);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("📝 ALUMNO_CURSO: Total = " + rs.getInt("total") + 
                                     ", Activos = " + rs.getInt("activos"));
                }
            }
            
            // 3. Verificar posibles valores del campo 'estado' en alumno_curso
            String sqlEstados = "SELECT DISTINCT estado, COUNT(*) as cantidad FROM alumno_curso GROUP BY estado";
            try (PreparedStatement stmt = connection.prepareStatement(sqlEstados);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("📊 ESTADOS EN ALUMNO_CURSO:");
                while (rs.next()) {
                    System.out.println("   - '" + rs.getString("estado") + "': " + rs.getInt("cantidad") + " registros");
                }
            }
            
            // 4. Verificar posibles valores del campo 'status' en usuarios  
            String sqlStatusUsuarios = "SELECT DISTINCT status, COUNT(*) as cantidad FROM usuarios GROUP BY status";
            try (PreparedStatement stmt = connection.prepareStatement(sqlStatusUsuarios);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("📊 STATUS EN USUARIOS:");
                while (rs.next()) {
                    System.out.println("   - '" + rs.getString("status") + "': " + rs.getInt("cantidad") + " registros");
                }
            }
            
            // 5. Verificar si hay usuarios que deberían ser estudiantes (rol = 4)
            String sqlEstudiantes = "SELECT COUNT(*) as total FROM usuarios WHERE rol = 4";
            try (PreparedStatement stmt = connection.prepareStatement(sqlEstudiantes);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("🎓 USUARIOS CON ROL ESTUDIANTE (4): " + rs.getInt("total"));
                }
            }
            
            // 6. Hacer un LEFT JOIN para ver qué usuarios no están en alumno_curso
            String sqlSinCurso = """
                SELECT COUNT(*) as sin_curso 
                FROM usuarios u 
                LEFT JOIN alumno_curso ac ON u.id = ac.alumno_id 
                WHERE u.rol = 4 AND u.status = '1' AND ac.alumno_id IS NULL
            """;
            try (PreparedStatement stmt = connection.prepareStatement(sqlSinCurso);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("❌ ESTUDIANTES ACTIVOS SIN CURSO ASIGNADO: " + rs.getInt("sin_curso"));
                }
            }
            
            // 7. Mostrar algunos ejemplos de usuarios que podrían ser estudiantes
            String sqlEjemplos = """
                SELECT u.id, u.nombre, u.apellido, u.rol, u.status, 
                       CASE WHEN ac.alumno_id IS NOT NULL THEN 'SÍ' ELSE 'NO' END as tiene_curso
                FROM usuarios u 
                LEFT JOIN alumno_curso ac ON u.id = ac.alumno_id AND ac.estado = 'activo'
                WHERE u.rol = 4 AND u.status = '1'
                LIMIT 5
            """;
            try (PreparedStatement stmt = connection.prepareStatement(sqlEjemplos);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("📋 EJEMPLOS DE ESTUDIANTES:");
                while (rs.next()) {
                    System.out.println("   ID:" + rs.getInt("id") + " - " + 
                                     rs.getString("apellido") + ", " + rs.getString("nombre") + 
                                     " [" + rs.getString("rol") + "/" + rs.getString("status") + 
                                     "] Tiene curso: " + rs.getString("tiene_curso"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error en diagnóstico: " + e.getMessage());
        }
        
        System.out.println("🔬 === FIN DIAGNÓSTICO ===\n");
    }
    
    /**
     * Obtiene los datos completos de un estudiante por curso específico
     */
    private EstudianteConCurso obtenerDatosEstudiantePorCurso(int alumnoId, int cursoId) throws SQLException {
        String sql = """
            SELECT DISTINCT 
                u.id as alumno_id,
                u.nombre,
                u.apellido,
                u.dni,
                c.id as curso_id,
                c.anio as anio_actual,
                c.division as division_actual,
                c.turno,
                ac.estado as estado_inscripcion
            FROM usuarios u
            INNER JOIN alumno_curso ac ON u.id = ac.alumno_id
            INNER JOIN cursos c ON ac.curso_id = c.id
            WHERE u.id = ? AND c.id = ? AND ac.estado = 'activo'
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, alumnoId);
            stmt.setInt(2, cursoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EstudianteConCurso estudiante = new EstudianteConCurso();
                    estudiante.alumnoId = rs.getInt("alumno_id");
                    estudiante.nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                    estudiante.dni = rs.getString("dni");
                    estudiante.cursoActualId = rs.getInt("curso_id");
                    estudiante.anioActual = rs.getInt("anio_actual");
                    estudiante.divisionActual = rs.getInt("division_actual");
                    estudiante.turnoActual = rs.getString("turno");
                    
                    // Generar nombre del curso actual
                    estudiante.cursoActualNombre = estudiante.anioActual + "° año " + 
                        estudiante.divisionActual + "° división";
                    if (estudiante.turnoActual != null && !estudiante.turnoActual.isEmpty()) {
                        estudiante.cursoActualNombre += " - " + estudiante.turnoActual;
                    }
                    
                    // Evaluar estado académico
                    evaluarEstadoAcademico(estudiante);
                    
                    return estudiante;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Determina el estado final según el tipo de progresión
     */
    private String determinarEstadoFinal(int cursoOrigenId, int cursoDestinoId, EstudianteConCurso estudiante) {
        // Si es el mismo curso = repetición
        if (cursoOrigenId == cursoDestinoId) {
            return "REPITENTE";
        }
        
        // Si cursoDestinoId == -2 = egreso
        if (cursoDestinoId == -2) {
            return "EGRESADO";
        }
        
        // Verificar si es promoción normal o con deuda
        if (estudiante.materiasDesaprobadas > 0) {
            return "CON_DEUDA";
        }
        
        // Promoción normal
        return "PROMOCIONADO";
    }
    
    /**
     * Guarda el historial académico del estudiante (versión simplificada)
     */
    private void guardarHistorialAcademico(EstudianteConCurso estudiante, String estadoFinal) throws SQLException {
        // Verificar si existen las tablas necesarias, si no, usar método alternativo
        if (!existeTablaHistorialAcademico()) {
            System.out.println("📝 Tabla historial_academico no disponible - usando método alternativo");
            guardarHistorialAlternativo(estudiante, estadoFinal);
            return;
        }
        
        // Obtener ciclo lectivo actual
        int cicloLectivoId = obtenerCicloLectivoActual();
        
        String sql = """
            INSERT INTO historial_academico (
                alumno_id, ciclo_lectivo_id, curso_id, estado_inicial, estado_final,
                promedio_general, total_faltas, materias_aprobadas, materias_desaprobadas,
                observaciones, procesado_por
            ) VALUES (?, ?, ?, 'REGULAR', ?, ?, ?, ?, ?, ?, 1)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, estudiante.alumnoId);
            stmt.setInt(2, cicloLectivoId);
            stmt.setInt(3, estudiante.cursoActualId);
            stmt.setString(4, estadoFinal);
            stmt.setDouble(5, estudiante.promedioGeneral);
            stmt.setInt(6, estudiante.totalFaltas);
            stmt.setInt(7, estudiante.materiasAprobadas);
            stmt.setInt(8, estudiante.materiasDesaprobadas);
            stmt.setString(9, estudiante.observaciones + " - Estado: " + estudiante.estadoAcademico);
            
            stmt.executeUpdate();
            
            System.out.println("📚 Historial guardado para " + estudiante.nombreCompleto + 
                             " - Estado final: " + estadoFinal);
        }
    }
    
    /**
     * Verifica si existe la tabla historial_academico
     */
    private boolean existeTablaHistorialAcademico() {
        try {
            String sql = "SELECT 1 FROM historial_academico LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Guarda historial usando método alternativo si no existe la tabla principal
     */
    private void guardarHistorialAlternativo(EstudianteConCurso estudiante, String estadoFinal) {
        System.out.println("📋 Registro de progresión - Alumno: " + estudiante.nombreCompleto + 
                         " | Curso: " + estudiante.cursoActualNombre + 
                         " | Estado final: " + estadoFinal + 
                         " | Promedio: " + String.format("%.2f", estudiante.promedioGeneral) +
                         " | Materias aprob/desaprob: " + estudiante.materiasAprobadas + "/" + estudiante.materiasDesaprobadas);
    }
    
    /**
     * Obtiene el ciclo lectivo actual o lo crea si no existe
     */
    private int obtenerCicloLectivoActual() throws SQLException {
        int anioActual = java.time.Year.now().getValue();
        
        // Buscar ciclo lectivo existente
        String sqlBuscar = "SELECT id FROM ciclo_lectivo WHERE anio = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sqlBuscar)) {
            stmt.setInt(1, anioActual);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️ No se pudo acceder a ciclo_lectivo: " + e.getMessage());
            return 1; // Valor por defecto
        }
        
        // Si no existe, crear uno nuevo
        try {
            String sqlCrear = "INSERT INTO ciclo_lectivo (anio, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, 'ACTIVO')";
            try (PreparedStatement stmt = connection.prepareStatement(sqlCrear, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, anioActual);
                stmt.setDate(2, java.sql.Date.valueOf(anioActual + "-03-01"));
                stmt.setDate(3, java.sql.Date.valueOf(anioActual + "-12-15"));
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int nuevoCicloId = rs.getInt(1);
                        System.out.println("📅 Ciclo lectivo " + anioActual + " creado (ID: " + nuevoCicloId + ")");
                        return nuevoCicloId;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️ No se pudo crear ciclo lectivo: " + e.getMessage());
            return 1; // Valor por defecto
        }
        
        return 1; // Valor por defecto
    }
    
    /**
     * Obtiene el historial académico completo de un alumno
     */
    public List<Map<String, Object>> obtenerHistorialAcademico(int alumnoId) {
        List<Map<String, Object>> historial = new ArrayList<>();
        
        String sql = """
            SELECT 
                ha.id,
                cl.anio as ciclo_lectivo,
                c.anio as curso_anio,
                c.division as curso_division,
                c.turno as curso_turno,
                ha.estado_inicial,
                ha.estado_final,
                ha.promedio_general,
                ha.total_faltas,
                ha.materias_aprobadas,
                ha.materias_desaprobadas,
                ha.observaciones,
                ha.fecha_procesamiento,
                up.nombre as procesado_por_nombre,
                up.apellido as procesado_por_apellido
            FROM historial_academico ha
            INNER JOIN ciclo_lectivo cl ON ha.ciclo_lectivo_id = cl.id
            INNER JOIN cursos c ON ha.curso_id = c.id
            INNER JOIN usuarios up ON ha.procesado_por = up.id
            WHERE ha.alumno_id = ?
            ORDER BY cl.anio DESC, c.anio DESC
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, alumnoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> registro = new HashMap<>();
                    registro.put("id", rs.getInt("id"));
                    registro.put("ciclo_lectivo", rs.getInt("ciclo_lectivo"));
                    registro.put("curso", rs.getInt("curso_anio") + "° año " + 
                               rs.getInt("curso_division") + "° división - " + 
                               rs.getString("curso_turno"));
                    registro.put("estado_inicial", rs.getString("estado_inicial"));
                    registro.put("estado_final", rs.getString("estado_final"));
                    registro.put("promedio_general", rs.getDouble("promedio_general"));
                    registro.put("total_faltas", rs.getInt("total_faltas"));
                    registro.put("materias_aprobadas", rs.getInt("materias_aprobadas"));
                    registro.put("materias_desaprobadas", rs.getInt("materias_desaprobadas"));
                    registro.put("observaciones", rs.getString("observaciones"));
                    registro.put("fecha_procesamiento", rs.getTimestamp("fecha_procesamiento"));
                    registro.put("procesado_por", rs.getString("procesado_por_apellido") + 
                               ", " + rs.getString("procesado_por_nombre"));
                    
                    historial.add(registro);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo historial académico: " + e.getMessage());
        }
        
        return historial;
    }
}