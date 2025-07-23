package main.java.dao;

import main.java.database.Conexion;
import main.java.models.TemaDiario;
import main.java.models.ConfiguracionBloque;
import main.java.models.ContadorClases;
import main.java.models.Curso;
import main.java.models.Materia;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para el manejo de temas diarios del libro de temas.
 * Incluye funcionalidades mejoradas para numeración automática de clases,
 * cálculo de bloques y manejo de diferentes modos de carga.
 * 
 * @author Sistema ET20
 * @version 2.0
 */
public class TemaDiarioDAO {
    
    private static final Logger logger = Logger.getLogger(TemaDiarioDAO.class.getName());
    private Connection connection;
    
    public TemaDiarioDAO() {
        this.connection = Conexion.getInstancia().verificarConexion();
    }
    
    /**
     * Obtiene las materias y cursos asignados a un profesor en el día actual.
     */
    public List<Map<String, Object>> obtenerMateriasYCursosProfesor(int profesorId) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String query = """
            SELECT DISTINCT 
                hm.materia_id,
                hm.curso_id,
                m.nombre as materia_nombre,
                c.anio as curso_anio,
                c.division as curso_division,
                c.turno as curso_turno,
                COALESCE(cb.permite_carga_multiple, FALSE) as permite_carga_multiple,
                COALESCE(cb.duracion_bloque, 40) as duracion_bloque
            FROM horarios_materia hm
            JOIN materias m ON hm.materia_id = m.id
            JOIN cursos c ON hm.curso_id = c.id
            LEFT JOIN configuracion_bloques cb ON m.id = cb.materia_id
            WHERE hm.profesor_id = ?
            ORDER BY m.nombre, c.anio, c.division
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("materiaId", rs.getInt("materia_id"));
                item.put("cursoId", rs.getInt("curso_id"));
                item.put("materiaNombre", rs.getString("materia_nombre"));
                item.put("cursoAnio", rs.getInt("curso_anio"));
                item.put("cursoDivision", rs.getInt("curso_division"));
                item.put("cursoTurno", rs.getString("curso_turno"));
                item.put("permiteCargaMultiple", rs.getBoolean("permite_carga_multiple"));
                item.put("duracionBloque", rs.getInt("duracion_bloque"));
                
                resultado.add(item);
            }
            
            logger.info("Materias y cursos encontrados para profesor " + profesorId + ": " + resultado.size());
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener materias y cursos del profesor", e);
        }
        
        return resultado;
    }
    
    /**
     * Calcula cuántos bloques de clase tiene un profesor para una materia específica en una fecha.
     */
    public int calcularBloquesDelDia(int profesorId, int materiaId, int cursoId, LocalDate fecha) {
        String query = """
            SELECT 
                SUM(TIME_TO_SEC(TIMEDIFF(hora_fin, hora_inicio))) / 60 as minutos_totales
            FROM horarios_materia hm
            WHERE hm.profesor_id = ? 
            AND hm.materia_id = ?
            AND hm.curso_id = ?
            AND (
                LOWER(hm.dia_semana) = ? OR
                hm.dia_semana = ? OR
                LOWER(hm.dia_semana) = ?
            )
            """;
        
        try {
            // Obtener el día de la semana en diferentes formatos
            int diaSemanaNum = fecha.getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo
            String diaSemanaTexto = fecha.getDayOfWeek().getDisplayName(
                java.time.format.TextStyle.FULL, java.util.Locale.of("es")).toLowerCase();
            String diaSemanaIngles = fecha.getDayOfWeek().name().toLowerCase();
            
            logger.info(String.format("Buscando horarios para profesor %d, materia %d, curso %d en día: %s (%s, %s)", 
                profesorId, materiaId, cursoId, diaSemanaTexto, diaSemanaIngles, diaSemanaNum));
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ps.setInt(2, materiaId);
            ps.setInt(3, cursoId);
            ps.setString(4, diaSemanaTexto);      // "martes"
            ps.setString(5, String.valueOf(diaSemanaNum)); // "2"
            ps.setString(6, diaSemanaIngles);     // "tuesday"
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double minutosTotales = rs.getDouble("minutos_totales");
                
                if (minutosTotales == 0) {
                    logger.warning("No se encontraron horarios para esta combinación profesor-materia-curso-día");
                    return 0;
                }
                
                // Usar 40 minutos por bloque como estándar
                int duracionBloque = 40;
                // Usar división entera para bloques exactos (cada hora cátedra = 40 min exactos)
                int bloques = (int) (minutosTotales / duracionBloque);
                
                logger.info(String.format("Cálculo específico: %.2f minutos total / %d minutos por bloque = %d bloques (división entera)", 
                    minutosTotales, duracionBloque, bloques));
                
                return Math.max(0, bloques);
            } else {
                logger.warning("No hay resultados para la consulta de horarios");
                return 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al calcular bloques del día", e);
        }
        
        return 0;
    }
    
    /**
     * Obtiene el próximo número de clase para una materia y curso específicos.
     */
    public int[] obtenerProximoNumeroClase(int profesorId, int cursoId, int materiaId, int cantidadBloques) {
        // Intentar usar el procedimiento almacenado primero
        try {
            String callQuery = "CALL obtener_proximo_numero_clase(?, ?, ?, ?, ?, ?, ?)";
            CallableStatement cs = connection.prepareCall(callQuery);
            cs.setInt(1, profesorId);
            cs.setInt(2, cursoId);
            cs.setInt(3, materiaId);
            cs.setInt(4, LocalDate.now().getYear());
            cs.setInt(5, cantidadBloques);
            cs.registerOutParameter(6, Types.INTEGER); // clase_desde
            cs.registerOutParameter(7, Types.INTEGER); // clase_hasta
            
            cs.execute();
            
            int claseDesde = cs.getInt(6);
            int claseHasta = cs.getInt(7);
            
            logger.info(String.format("Procedimiento: Próximas clases %d-%d para profesor %d, materia %d, curso %d", 
                claseDesde, claseHasta, profesorId, materiaId, cursoId));
            
            return new int[]{claseDesde, claseHasta};
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error al usar procedimiento almacenado, usando método manual", e);
            // Fallback: calcular manualmente
            return calcularProximoNumeroClaseManual(profesorId, cursoId, materiaId, cantidadBloques);
        }
    }
    
    /**
     * Método fallback para calcular el próximo número de clase manualmente.
     */
    private int[] calcularProximoNumeroClaseManual(int profesorId, int cursoId, int materiaId, int cantidadBloques) {
        String query = """
            SELECT COALESCE(MAX(clase_hasta), 0) as ultima_clase
            FROM temas_diarios
            WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?
            AND YEAR(fecha_clase) = ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setInt(4, LocalDate.now().getYear());
            
            ResultSet rs = ps.executeQuery();
            int ultimaClase = 0;
            if (rs.next()) {
                ultimaClase = rs.getInt("ultima_clase");
            }
            
            int claseDesde = ultimaClase + 1;
            int claseHasta = ultimaClase + cantidadBloques;
            
            logger.info(String.format("Cálculo manual: Última clase %d, próximas clases %d-%d (%d bloques)", 
                ultimaClase, claseDesde, claseHasta, cantidadBloques));
            
            // Actualizar o crear contador
            actualizarContadorClases(profesorId, cursoId, materiaId, claseHasta, cantidadBloques);
            
            return new int[]{claseDesde, claseHasta};
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error en cálculo manual de próximo número de clase", e);
            return new int[]{1, cantidadBloques};
        }
    }
    
    /**
     * Actualiza el contador de clases manualmente.
     */
    private void actualizarContadorClases(int profesorId, int cursoId, int materiaId, int claseHasta, int cantidadBloques) {
        String queryUpdate = """
            INSERT INTO contador_clases (profesor_id, curso_id, materia_id, anio_lectivo, total_clases, ultima_clase)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                ultima_clase = ?,
                total_clases = total_clases + ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(queryUpdate);
            ps.setInt(1, profesorId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setInt(4, LocalDate.now().getYear());
            ps.setInt(5, cantidadBloques); // total_clases inicial
            ps.setInt(6, claseHasta);      // ultima_clase inicial
            ps.setInt(7, claseHasta);      // ultima_clase para update
            ps.setInt(8, cantidadBloques); // incremento para total_clases
            
            ps.executeUpdate();
            logger.info("Contador de clases actualizado");
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error al actualizar contador de clases", e);
        }
    }
    
    /**
     * Guarda un nuevo tema diario.
     */
    public boolean guardarTemaDiario(TemaDiario tema) {
        logger.info("Iniciando guardado de tema diario");
        
        // Validar datos básicos
        if (tema.getProfesorId() <= 0 || tema.getCursoId() <= 0 || tema.getMateriaId() <= 0) {
            logger.warning("Datos inválidos: profesorId=" + tema.getProfesorId() + 
                          ", cursoId=" + tema.getCursoId() + ", materiaId=" + tema.getMateriaId());
            return false;
        }
        
        String query = """
            INSERT INTO temas_diarios 
            (libro_id, profesor_id, curso_id, materia_id, fecha_clase, 
             clase_desde, clase_hasta, tema, actividades_desarrolladas, observaciones, caracter_clase, modo_carga, bloque_numero)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try {
            // Primero obtener o crear el libro de temas
            int libroId = obtenerOCrearLibroTemas(tema.getProfesorId(), tema.getCursoId(), tema.getMateriaId());
            tema.setLibroId(libroId);
            
            // Validar que el libro se haya obtenido correctamente
            if (libroId <= 0) {
                logger.severe("No se pudo obtener o crear libro de temas");
                return false;
            }
            
            // Validar foreign keys antes de insertar
            if (!validarForeignKeys(tema.getProfesorId(), libroId, tema.getCursoId(), tema.getMateriaId())) {
                logger.severe("Validación de FK falló - cancelando inserción");
                logearInformacionFK(tema.getProfesorId(), libroId, tema.getCursoId(), tema.getMateriaId());
                return false;
            }
            
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, tema.getLibroId());
            ps.setInt(2, tema.getProfesorId());
            ps.setInt(3, tema.getCursoId());
            ps.setInt(4, tema.getMateriaId());
            ps.setDate(5, java.sql.Date.valueOf(tema.getFechaClase()));
            ps.setInt(6, tema.getClaseDesde());
            ps.setInt(7, tema.getClaseHasta());
            ps.setString(8, tema.getTema());
            ps.setString(9, tema.getActividadesDesarrolladas());
            ps.setString(10, tema.getObservaciones());
            ps.setString(11, tema.getCaracterClase() != null ? tema.getCaracterClase() : "Explicativa");
            ps.setString(12, tema.getModoCarga().name());
            ps.setObject(13, tema.getBloqueNumero());
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tema.setId(generatedKeys.getInt(1));
                }
                logger.info("Tema diario guardado exitosamente con ID: " + tema.getId());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al guardar tema diario: " + e.getMessage(), e);
            logearInformacionFK(tema.getProfesorId(), tema.getLibroId(), tema.getCursoId(), tema.getMateriaId());
        }
        
        return false;
    }
    
    /**
     * Obtiene o crea un libro de temas para la combinación profesor-curso-materia.
     */
    private int obtenerOCrearLibroTemas(int profesorId, int cursoId, int materiaId) {
        // Buscar libro existente
        String queryBuscar = """
            SELECT id FROM libros_temas 
            WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? AND anio_lectivo = ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(queryBuscar);
            ps.setInt(1, profesorId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setInt(4, LocalDate.now().getYear());
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int libroId = rs.getInt("id");
                logger.info("Libro de temas encontrado con ID: " + libroId);
                return libroId;
            }
            
            // Crear nuevo libro - Verificar primero si existen las FK
            String queryVerificar = """
                SELECT 
                    (SELECT COUNT(*) FROM usuarios WHERE id = ?) as profesor_existe,
                    (SELECT COUNT(*) FROM cursos WHERE id = ?) as curso_existe,
                    (SELECT COUNT(*) FROM materias WHERE id = ?) as materia_existe
                """;
            
            PreparedStatement psVerif = connection.prepareStatement(queryVerificar);
            psVerif.setInt(1, profesorId);
            psVerif.setInt(2, cursoId);
            psVerif.setInt(3, materiaId);
            
            ResultSet rsVerif = psVerif.executeQuery();
            if (rsVerif.next()) {
                if (rsVerif.getInt("profesor_existe") == 0) {
                    logger.severe("ERROR: Profesor con ID " + profesorId + " no existe");
                    return -1;
                }
                if (rsVerif.getInt("curso_existe") == 0) {
                    logger.severe("ERROR: Curso con ID " + cursoId + " no existe");
                    return -1;
                }
                if (rsVerif.getInt("materia_existe") == 0) {
                    logger.severe("ERROR: Materia con ID " + materiaId + " no existe");
                    return -1;
                }
            }
            
            // Crear nuevo libro
            String queryCrear = """
                INSERT INTO libros_temas (profesor_id, curso_id, materia_id, anio_lectivo, estado)
                VALUES (?, ?, ?, ?, 'activo')
                """;
            
            PreparedStatement psCrear = connection.prepareStatement(queryCrear, Statement.RETURN_GENERATED_KEYS);
            psCrear.setInt(1, profesorId);
            psCrear.setInt(2, cursoId);
            psCrear.setInt(3, materiaId);
            psCrear.setInt(4, LocalDate.now().getYear());
            
            int filasCreadas = psCrear.executeUpdate();
            if (filasCreadas > 0) {
                ResultSet generatedKeys = psCrear.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int nuevoId = generatedKeys.getInt(1);
                    logger.info("Nuevo libro de temas creado con ID: " + nuevoId);
                    return nuevoId;
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener o crear libro de temas", e);
            e.printStackTrace();
        }
        
        logger.severe("No se pudo crear o encontrar libro de temas");
        return -1;
    }
    
    /**
     * Obtiene los temas diarios de un profesor con filtros.
     */
    public List<TemaDiario> obtenerTemasDiarios(int profesorId, LocalDate fechaDesde, 
                                               LocalDate fechaHasta, Integer cursoId, Integer materiaId) {
        List<TemaDiario> temas = new ArrayList<>();
        
        StringBuilder query = new StringBuilder("""
            SELECT td.*, 
                   u.nombre as profesor_nombre,
                   u.apellido as profesor_apellido,
                   m.nombre as materia_nombre,
                   c.anio as curso_anio,
                   c.division as curso_division,
                   c.turno as curso_turno,
                   val.nombre as validado_por_nombre,
                   DAYNAME(td.fecha_clase) as dia_semana,
                   CONCAT(td.clase_desde, '-', td.clase_hasta) as rango_clases,
                   CASE 
                       WHEN td.validado = 1 THEN 'Validado'
                       ELSE 'Pendiente'
                   END as estado_visual
            FROM temas_diarios td
            JOIN usuarios u ON td.profesor_id = u.id
            JOIN materias m ON td.materia_id = m.id
            JOIN cursos c ON td.curso_id = c.id
            LEFT JOIN usuarios val ON td.validado_por = val.id
            WHERE td.profesor_id = ?
            """);
        
        List<Object> parametros = new ArrayList<>();
        parametros.add(profesorId);
        
        if (fechaDesde != null) {
            query.append(" AND td.fecha_clase >= ?");
            parametros.add(java.sql.Date.valueOf(fechaDesde));
        }
        
        if (fechaHasta != null) {
            query.append(" AND td.fecha_clase <= ?");
            parametros.add(java.sql.Date.valueOf(fechaHasta));
        }
        
        if (cursoId != null) {
            query.append(" AND td.curso_id = ?");
            parametros.add(cursoId);
        }
        
        if (materiaId != null) {
            query.append(" AND td.materia_id = ?");
            parametros.add(materiaId);
        }
        
        query.append(" ORDER BY td.fecha_clase DESC, td.clase_desde ASC");
        
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TemaDiario tema = mapearResultSetATemaDiario(rs);
                temas.add(tema);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener temas diarios", e);
        }
        
        return temas;
    }
    
    /**
     * Verifica si ya existe un tema cargado para una fecha y materia específica.
     */
    public boolean existeTemaPorFecha(int profesorId, int cursoId, int materiaId, LocalDate fecha) {
        String query = """
            SELECT COUNT(*) as cantidad
            FROM temas_diarios
            WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? AND fecha_clase = ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setDate(4, java.sql.Date.valueOf(fecha));
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cantidad") > 0;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al verificar existencia de tema", e);
        }
        
        return false;
    }
    
    /**
     * Actualiza un tema diario existente.
     */
    public boolean actualizarTemaDiario(TemaDiario tema) {
        String query = """
            UPDATE temas_diarios 
            SET fecha_clase = ?, tema = ?, actividades_desarrolladas = ?, observaciones = ?, caracter_clase = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND validado = FALSE
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setDate(1, java.sql.Date.valueOf(tema.getFechaClase()));
            ps.setString(2, tema.getTema());
            ps.setString(3, tema.getActividadesDesarrolladas());
            ps.setString(4, tema.getObservaciones());
            ps.setString(5, tema.getCaracterClase());
            ps.setInt(6, tema.getId());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar tema diario", e);
            return false;
        }
    }
    
    /**
     * Valida un tema diario (solo para directivos).
     */
    public boolean validarTemaDiario(int temaId, int validadoPor) {
        String query = """
            UPDATE temas_diarios 
            SET validado = TRUE, validado_por = ?, fecha_validacion = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, validadoPor);
            ps.setInt(2, temaId);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al validar tema diario", e);
            return false;
        }
    }
    
    /**
     * Obtiene la configuración de bloques para una materia.
     */
    public ConfiguracionBloque obtenerConfiguracionBloque(int materiaId) {
        String query = """
            SELECT cb.*, m.nombre as materia_nombre
            FROM configuracion_bloques cb
            JOIN materias m ON cb.materia_id = m.id
            WHERE cb.materia_id = ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, materiaId);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ConfiguracionBloque config = new ConfiguracionBloque();
                config.setId(rs.getInt("id"));
                config.setMateriaId(rs.getInt("materia_id"));
                config.setDuracionBloque(rs.getInt("duracion_bloque"));
                config.setPermiteCargaMultiple(rs.getBoolean("permite_carga_multiple"));
                config.setMateriaNombre(rs.getString("materia_nombre"));
                config.setCreatedAt(rs.getTimestamp("created_at"));
                config.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                return config;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener configuración de bloque", e);
        }
        
        // Devolver configuración por defecto si no existe
        ConfiguracionBloque configDefault = new ConfiguracionBloque();
        configDefault.setMateriaId(materiaId);
        return configDefault;
    }
    
    /**
     * Obtiene el contador de clases actual para una combinación específica.
     */
    public ContadorClases obtenerContadorClases(int profesorId, int cursoId, int materiaId) {
        String query = """
            SELECT cc.*, 
                   u.nombre as profesor_nombre, u.apellido as profesor_apellido,
                   m.nombre as materia_nombre,
                   c.anio as curso_anio, c.division as curso_division, c.turno as curso_turno
            FROM contador_clases cc
            JOIN usuarios u ON cc.profesor_id = u.id
            JOIN materias m ON cc.materia_id = m.id  
            JOIN cursos c ON cc.curso_id = c.id
            WHERE cc.profesor_id = ? AND cc.curso_id = ? AND cc.materia_id = ? AND cc.anio_lectivo = ?
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setInt(4, LocalDate.now().getYear());
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ContadorClases contador = new ContadorClases();
                contador.setId(rs.getInt("id"));
                contador.setProfesorId(rs.getInt("profesor_id"));
                contador.setCursoId(rs.getInt("curso_id"));
                contador.setMateriaId(rs.getInt("materia_id"));
                contador.setAnioLectivo(rs.getInt("anio_lectivo"));
                contador.setTotalClases(rs.getInt("total_clases"));
                contador.setUltimaClase(rs.getInt("ultima_clase"));
                contador.setCreatedAt(rs.getTimestamp("created_at"));
                contador.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                // Campos de vista
                contador.setProfesorNombre(rs.getString("profesor_nombre"));
                contador.setProfesorApellido(rs.getString("profesor_apellido"));
                contador.setMateriaNombre(rs.getString("materia_nombre"));
                contador.setCursoAnio(rs.getInt("curso_anio"));
                contador.setCursoDivision(rs.getInt("curso_division"));
                contador.setCursoTurno(rs.getString("curso_turno"));
                
                return contador;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener contador de clases", e);
        }
        
        // Devolver contador nuevo si no existe
        ContadorClases nuevoContador = new ContadorClases(profesorId, cursoId, materiaId, LocalDate.now().getYear());
        return nuevoContador;
    }
    
    // Métodos de utilidad privados
    
    /**
     * Valida que todas las FK existen antes de insertar en temas_diarios.
     */
    private boolean validarForeignKeys(int profesorId, int libroId, int cursoId, int materiaId) {
        try {
            // Validar usuario (profesor) - Simplificado: solo verificar que existe
            String queryUsuario = "SELECT id FROM usuarios WHERE id = ?";
            PreparedStatement ps1 = connection.prepareStatement(queryUsuario);
            ps1.setInt(1, profesorId);
            ResultSet rs1 = ps1.executeQuery();
            boolean profesorValido = rs1.next();
            rs1.close();
            ps1.close();
            
            if (!profesorValido) {
                logger.warning("Profesor con ID " + profesorId + " no encontrado en la base de datos");
                return false;
            }
            
            // Validar libro
            String queryLibro = "SELECT id FROM libros_temas WHERE id = ?";
            PreparedStatement ps2 = connection.prepareStatement(queryLibro);
            ps2.setInt(1, libroId);
            ResultSet rs2 = ps2.executeQuery();
            boolean libroValido = rs2.next();
            rs2.close();
            ps2.close();
            
            if (!libroValido) {
                logger.warning("Libro con ID " + libroId + " no encontrado");
                return false;
            }
            
            // Validar curso
            String queryCurso = "SELECT id FROM cursos WHERE id = ?";
            PreparedStatement ps3 = connection.prepareStatement(queryCurso);
            ps3.setInt(1, cursoId);
            ResultSet rs3 = ps3.executeQuery();
            boolean cursoValido = rs3.next();
            rs3.close();
            ps3.close();
            
            if (!cursoValido) {
                logger.warning("Curso con ID " + cursoId + " no encontrado");
                return false;
            }
            
            // Validar materia
            String queryMateria = "SELECT id FROM materias WHERE id = ?";
            PreparedStatement ps4 = connection.prepareStatement(queryMateria);
            ps4.setInt(1, materiaId);
            ResultSet rs4 = ps4.executeQuery();
            boolean materiaValida = rs4.next();
            rs4.close();
            ps4.close();
            
            if (!materiaValida) {
                logger.warning("Materia con ID " + materiaId + " no encontrada");
                return false;
            }
            
            logger.info("Todas las FK validadas correctamente");
            return true;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error validando foreign keys", e);
            return false;
        }
    }

    /**
     * Obtiene información detallada para debug de FK.
     */
    private void logearInformacionFK(int profesorId, int libroId, int cursoId, int materiaId) {
        try {
            logger.info("=== INFORMACIÓN FK PARA DEBUG ===");
            
            // Info del profesor
            String queryProfesor = "SELECT nombre, apellido, rol FROM usuarios WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(queryProfesor);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logger.info(String.format("Profesor: %s %s (Tipo: %s)", 
                    rs.getString("nombre"), rs.getString("apellido"), rs.getString("rol")));
            } else {
                logger.warning("Profesor con ID " + profesorId + " no encontrado");
            }
            
            // Info del curso
            String queryCurso = "SELECT anio, division FROM cursos WHERE id = ?";
            ps = connection.prepareStatement(queryCurso);
            ps.setInt(1, cursoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                logger.info(String.format("Curso: %s° %s", rs.getString("anio"), rs.getString("division")));
            }
            
            // Info de la materia
            String queryMateria = "SELECT nombre FROM materias WHERE id = ?";
            ps = connection.prepareStatement(queryMateria);
            ps.setInt(1, materiaId);
            rs = ps.executeQuery();
            if (rs.next()) {
                logger.info(String.format("Materia: %s", rs.getString("nombre")));
            }
            
            logger.info("=== FIN INFORMACIÓN FK ===");
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error obteniendo información FK para debug", e);
        }
    }
    
    private String obtenerDiaSemanaActual() {
        return LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("es")).toLowerCase();
    }
    
    private TemaDiario mapearResultSetATemaDiario(ResultSet rs) throws SQLException {
        TemaDiario tema = new TemaDiario();
        
        tema.setId(rs.getInt("id"));
        tema.setLibroId(rs.getInt("libro_id"));
        tema.setProfesorId(rs.getInt("profesor_id"));
        tema.setCursoId(rs.getInt("curso_id"));
        tema.setMateriaId(rs.getInt("materia_id"));
        tema.setFechaClase(rs.getDate("fecha_clase").toLocalDate());
        tema.setFechaCarga(rs.getTimestamp("fecha_carga"));
        tema.setClaseDesde(rs.getInt("clase_desde"));
        tema.setClaseHasta(rs.getInt("clase_hasta"));
        tema.setTema(rs.getString("tema"));
        tema.setActividadesDesarrolladas(rs.getString("actividades_desarrolladas"));
        tema.setObservaciones(rs.getString("observaciones"));
        tema.setCaracterClase(rs.getString("caracter_clase"));
        tema.setValidado(rs.getBoolean("validado"));
        tema.setFechaValidacion(rs.getTimestamp("fecha_validacion"));
        
        Integer validadoPor = rs.getObject("validado_por", Integer.class);
        tema.setValidadoPor(validadoPor);
        
        String modoCargaStr = rs.getString("modo_carga");
        if (modoCargaStr != null) {
            tema.setModoCarga(TemaDiario.ModoCarga.valueOf(modoCargaStr));
        }
        
        Integer bloqueNumero = rs.getObject("bloque_numero", Integer.class);
        tema.setBloqueNumero(bloqueNumero);
        
        tema.setCreatedAt(rs.getTimestamp("created_at"));
        tema.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // Campos de vista
        tema.setProfesorNombre(rs.getString("profesor_nombre"));
        tema.setProfesorApellido(rs.getString("profesor_apellido"));
        tema.setMateriaNombre(rs.getString("materia_nombre"));
        tema.setCursoAnio(rs.getInt("curso_anio"));
        tema.setCursoDivision(rs.getInt("curso_division"));
        tema.setCursoTurno(rs.getString("curso_turno"));
        tema.setValidadoPorNombre(rs.getString("validado_por_nombre"));
        tema.setDiaSemana(rs.getString("dia_semana"));
        tema.setRangoClases(rs.getString("rango_clases"));
        tema.setEstadoVisual(rs.getString("estado_visual"));
        
        return tema;
    }
    
    /**
     * Obtiene todos los cursos disponibles en el sistema
     * @return Lista de cursos
     */
    public List<Curso> obtenerTodosLosCursos() {
        List<Curso> cursos = new ArrayList<>();
        String query = """
            SELECT id, anio, division, turno
            FROM cursos
            ORDER BY anio, division
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Curso curso = new Curso();
                curso.setId(rs.getInt("id"));
                curso.setAnio(rs.getInt("anio"));
                curso.setDivision(rs.getInt("division"));
                curso.setTurno(rs.getString("turno"));
                cursos.add(curso);
            }
            
            logger.info("Cursos obtenidos: " + cursos.size());
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener cursos", e);
        }
        
        return cursos;
    }
    
    /**
     * Obtiene todas las materias disponibles para un curso específico
     * @param cursoId ID del curso
     * @return Lista de materias
     */
    public List<Materia> obtenerMateriasPorCurso(int cursoId) {
        List<Materia> materias = new ArrayList<>();
        String query = """
            SELECT DISTINCT m.id, m.nombre, m.categoria_id
            FROM materias m
            JOIN horarios_materia hm ON m.id = hm.materia_id
            WHERE hm.curso_id = ?
            ORDER BY m.nombre
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Materia materia = new Materia();
                materia.setId(rs.getInt("id"));
                materia.setNombre(rs.getString("nombre"));
                materia.setCategoriaId(rs.getInt("categoria_id"));
                // No establecemos descripcion ya que no existe en la BD
                materias.add(materia);
            }
            
            logger.info("Materias obtenidas para curso " + cursoId + ": " + materias.size());
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener materias por curso", e);
        }
        
        return materias;
    }
    
    /**
     * Obtiene todos los temas cargados para un curso y materia específicos
     * @param cursoId ID del curso
     * @param materiaId ID de la materia
     * @return Lista de temas diarios
     */
    public List<TemaDiario> obtenerTemasPorCursoYMateria(int cursoId, int materiaId) {
        List<TemaDiario> temas = new ArrayList<>();
        String query = """
            SELECT vtc.*, td.caracter_clase
            FROM vista_temas_completa vtc
            JOIN temas_diarios td ON vtc.id = td.id
            WHERE vtc.curso_id = ? AND vtc.materia_id = ?
            ORDER BY vtc.fecha_clase DESC, vtc.clase_desde ASC
            """;
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setInt(2, materiaId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TemaDiario tema = mapearResultSetATemaDiario(rs);
                temas.add(tema);
            }
            
            logger.info("Temas obtenidos para curso " + cursoId + " y materia " + materiaId + ": " + temas.size());
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener temas por curso y materia", e);
        }
        
        return temas;
    }
    
    /**
     * Actualiza solo las observaciones de un tema diario específico
     * Este método es utilizado por roles administrativos para agregar observaciones
     * sin modificar el contenido académico cargado por los profesores
     */
    public boolean actualizarObservacionesTema(int temaId, String observaciones, int validadoPor) {
        System.out.println("🔍 DAO: actualizarObservacionesTema llamado:");
        System.out.println("   - Tema ID: " + temaId);
        System.out.println("   - Observaciones: '" + observaciones + "'");
        System.out.println("   - Validado por: " + validadoPor);
        
        String query = """
            UPDATE temas_diarios 
            SET observaciones = ?, 
                validado = 1,
                validado_por = ?, 
                fecha_validacion = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, observaciones);
            ps.setInt(2, validadoPor);
            ps.setInt(3, temaId);
            
            int filasAfectadas = ps.executeUpdate();
            System.out.println("✅ DAO: Filas afectadas: " + filasAfectadas);
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ DAO: Error SQL: " + e.getMessage());
            logger.log(Level.SEVERE, "Error al actualizar observaciones del tema " + temaId, e);
            return false;
        }
    }
}
