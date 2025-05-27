package main.java.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import main.java.database.Conexion;

/**
 * Gestor de Boletines - Sistema de organización automática CORREGIDO SOLO
 * funciona con servidor web, sin referencias a rutas locales
 */
public class GestorBoletines {

    // SOLO configuración de servidor web
    private static String RUTA_BASE_SERVIDOR = "http://10.120.1.109/miet20/boletines/";
    private static final String SEPARADOR = "/"; // Siempre usar / para URLs

    // Períodos válidos
    public static final String[] PERIODOS_VALIDOS = {
        "1B", "2B", "3B", "4B", "1C", "2C", "Final", "Diciembre", "Febrero"
    };

    // Mapeo de períodos para compatibilidad con BD
    private static final java.util.Map<String, String> MAPEO_PERIODOS = new java.util.HashMap<>();

    static {
        MAPEO_PERIODOS.put("1B", "1er Bimestre");
        MAPEO_PERIODOS.put("2B", "2do Bimestre");
        MAPEO_PERIODOS.put("3B", "3er Bimestre");
        MAPEO_PERIODOS.put("4B", "4to Bimestre");
        MAPEO_PERIODOS.put("1C", "1er Cuatrimestre");
        MAPEO_PERIODOS.put("2C", "2do Cuatrimestre");
        MAPEO_PERIODOS.put("Final", "Final");
        MAPEO_PERIODOS.put("Diciembre", "Diciembre");
        MAPEO_PERIODOS.put("Febrero", "Febrero");
    }

    /**
     * Estructura para información de boletín
     */
    public static class InfoBoletin {

        public int alumnoId;
        public String alumnoNombre;
        public String alumnoDni;
        public int cursoId;
        public String curso;
        public String division;
        public int anioLectivo;
        public String periodo;
        public String rutaArchivo;
        public String nombreArchivo;
        public long tamanioArchivo;
        public java.time.LocalDateTime fechaGeneracion;
        public String estadoBoletin;
        public boolean archivoExiste;

        public InfoBoletin() {
            this.fechaGeneracion = java.time.LocalDateTime.now();
            this.estadoBoletin = "Generado";
            this.archivoExiste = false;
        }
    }

    /**
     * Configura la ruta base del servidor (SOLO URLs)
     */
    public static void configurarRutaServidor(String rutaServidor) {
        if (rutaServidor != null && !rutaServidor.trim().isEmpty()) {
            // Asegurar que termine con /
            if (!rutaServidor.endsWith("/")) {
                rutaServidor += "/";
            }
            RUTA_BASE_SERVIDOR = rutaServidor;
            System.out.println("✅ Servidor configurado: " + RUTA_BASE_SERVIDOR);
        }
    }

    /**
     * Obtiene la ruta base del servidor
     */
    public static String obtenerRutaServidor() {
        return RUTA_BASE_SERVIDOR;
    }

    /**
     * Registra un boletín en BD con URL del servidor
     */
    public static boolean registrarBoletinEnServidor(int alumnoId, int cursoId, String periodo, String nombreArchivo) {
        try {
            InfoBoletin info = crearInfoBoletinDesdeBD(alumnoId, cursoId, periodo);
            if (info == null) {
                return false;
            }

            // Generar URL completa del archivo en el servidor
            info.rutaArchivo = generarUrlBoletin(info, nombreArchivo);
            info.nombreArchivo = nombreArchivo;
            info.archivoExiste = true; // Asumimos que existe en servidor

            return registrarBoletinEnBD(info);

        } catch (Exception e) {
            System.err.println("❌ Error registrando boletín: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera la URL completa del boletín en el servidor
     */
    private static String generarUrlBoletin(InfoBoletin info, String nombreArchivo) {
        String nombreCurso = info.curso + info.division;
        return RUTA_BASE_SERVIDOR + info.anioLectivo + "/" + nombreCurso + "/" + info.periodo + "/" + nombreArchivo;
    }

    /**
     * Crea InfoBoletin desde BD (sin cambios)
     */
    /**
     * CORREGIDO: Genera estructura completa con formato numérico correcto Y
     * crea carpetas físicas en el servidor
     */
    public static boolean generarEstructuraCompleta(int anioLectivo) {
        try {
            System.out.println("=== GENERANDO ESTRUCTURA FÍSICA EN SERVIDOR ===");
            System.out.println("Año lectivo: " + anioLectivo);
            System.out.println("Servidor: " + RUTA_BASE_SERVIDOR);

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ Error de conexión a BD");
                return false;
            }

            // Obtener cursos activos con formato correcto
            String queryCursos = "SELECT DISTINCT anio, division FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement ps = conect.prepareStatement(queryCursos);
            ResultSet rs = ps.executeQuery();

            int estructurasCreadas = 0;
            int carpetasCreadas = 0;

            while (rs.next()) {
                int anio = rs.getInt("anio");
                int division = rs.getInt("division");

                // CORREGIDO: Usar formato numérico (ej: "11", "12", "21", etc.)
                String nombreCurso = String.valueOf(anio) + String.valueOf(division);

                System.out.println("Procesando curso: " + nombreCurso + " (Año: " + anio + ", División: " + division + ")");

                // Crear carpetas físicas para cada período
                for (String periodo : PERIODOS_VALIDOS) {
                    try {
                        // Crear ruta completa: servidor/año/curso/periodo
                        String rutaCarpeta = construirRutaCarpeta(anioLectivo, nombreCurso, periodo);

                        // Intentar crear la carpeta física
                        boolean carpetaCreada = crearCarpetaFisica(rutaCarpeta);

                        if (carpetaCreada) {
                            carpetasCreadas++;
                            System.out.println("✅ Carpeta creada: " + rutaCarpeta);
                        } else {
                            System.out.println("⚠️ No se pudo crear carpeta: " + rutaCarpeta);
                        }

                        estructurasCreadas++;

                    } catch (Exception e) {
                        System.err.println("❌ Error creando estructura para " + nombreCurso + "/" + periodo + ": " + e.getMessage());
                    }
                }
            }

            rs.close();
            ps.close();

            System.out.println("=== RESUMEN DE CREACIÓN ===");
            System.out.println("✅ Estructura registrada: " + estructurasCreadas + " configuraciones");
            System.out.println("📁 Carpetas físicas creadas: " + carpetasCreadas);
            System.out.println("🌐 Servidor: " + RUTA_BASE_SERVIDOR);

            return carpetasCreadas > 0; // Éxito si se creó al menos una carpeta

        } catch (Exception e) {
            System.err.println("❌ Error al generar estructura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NUEVO: Construye la ruta completa de una carpeta
     */
    private static String construirRutaCarpeta(int anioLectivo, String nombreCurso, String periodo) {
        // Para URL de servidor: http://servidor/año/curso/periodo
        return RUTA_BASE_SERVIDOR + anioLectivo + "/" + nombreCurso + "/" + periodo;
    }

    /**
     * NUEVO: Crea una carpeta física en el servidor usando diferentes métodos
     */
     private static boolean crearCarpetaFisica(String rutaCarpeta) {
        try {
            System.out.println("=== Creando carpeta física ===");
            System.out.println("Ruta completa: " + rutaCarpeta);
            System.out.println("Servidor base: " + RUTA_BASE_SERVIDOR);
            
            // Verificar tipo de servidor
            if (RUTA_BASE_SERVIDOR.startsWith("http://") || RUTA_BASE_SERVIDOR.startsWith("https://")) {
                
                System.out.println("Servidor web detectado, usando métodos web...");
                return crearCarpetaEnServidorWeb(rutaCarpeta);
                
            } else if (RUTA_BASE_SERVIDOR.startsWith("file://") || 
                       RUTA_BASE_SERVIDOR.startsWith("/") || 
                       RUTA_BASE_SERVIDOR.matches("^[A-Za-z]:\\\\.*")) {
                
                System.out.println("Ruta local detectada, creando carpeta directamente...");
                
                // Es una ruta local - crear carpeta directamente
                String rutaLocal = RUTA_BASE_SERVIDOR.replace("file://", "");
                String carpetaRelativa = rutaCarpeta.replace(RUTA_BASE_SERVIDOR, "");
                java.io.File carpeta = new java.io.File(rutaLocal + carpetaRelativa);
                
                boolean creada = carpeta.mkdirs();
                System.out.println("Carpeta local " + (creada || carpeta.exists() ? "OK" : "ERROR") + ": " + carpeta.getAbsolutePath());
                return creada || carpeta.exists();
                
            } else {
                System.err.println("❌ Formato de servidor no reconocido: " + RUTA_BASE_SERVIDOR);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear carpeta física: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
     
     /**
     * NUEVO: Método de prueba para verificar el script PHP
     */
    public static boolean probarScriptPHP() {
        try {
            System.out.println("=== PROBANDO SCRIPT PHP ===");
            
            String baseUrl = RUTA_BASE_SERVIDOR.replace("/boletines/", "/");
            String scriptUrl = baseUrl + "crear_carpeta.php";
            String testUrl = scriptUrl + "?carpeta=test/prueba";
            
            System.out.println("URL de prueba: " + testUrl);
            
            java.net.URL url = new java.net.URL(testUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            
            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();
                
                System.out.println("Respuesta: " + response);
                System.out.println("✅ Script PHP funciona correctamente");
                return true;
            } else {
                System.err.println("❌ Script PHP no responde correctamente");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error probando script PHP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NUEVO: Crea carpeta en servidor web usando diferentes métodos
     */
    private static boolean crearCarpetaEnServidorWeb(String rutaCarpeta) {
        try {
            System.out.println("Intentando crear carpeta en servidor web: " + rutaCarpeta);
            
            // Extraer la ruta relativa desde la URL completa
            String carpetaRelativa = rutaCarpeta.replace(RUTA_BASE_SERVIDOR, "");
            
            // URL del script PHP - CORREGIDA
            String baseUrl = RUTA_BASE_SERVIDOR.replace("/boletines/", "/");
            String scriptUrl = baseUrl + "crear_carpeta.php";
            
            System.out.println("Script URL: " + scriptUrl);
            System.out.println("Carpeta relativa: " + carpetaRelativa);
            
            // Método PHP (método principal)
            try {
                String fullScriptUrl = scriptUrl + "?carpeta=" + 
                    java.net.URLEncoder.encode(carpetaRelativa, "UTF-8");
                
                System.out.println("URL completa: " + fullScriptUrl);
                
                java.net.URL url = new java.net.URL(fullScriptUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 segundos
                connection.setReadTimeout(10000);
                
                // Agregar headers
                connection.setRequestProperty("User-Agent", "JavaApp-BoletinManager/1.0");
                connection.setRequestProperty("Accept", "application/json,text/plain,*/*");
                
                int responseCode = connection.getResponseCode();
                System.out.println("Código de respuesta PHP: " + responseCode);
                
                if (responseCode == 200) {
                    // Leer la respuesta
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()));
                    String response = reader.readLine();
                    reader.close();
                    
                    System.out.println("Respuesta del servidor: " + response);
                    System.out.println("✅ Carpeta procesada vía PHP: " + rutaCarpeta);
                    return true;
                } else {
                    // Leer error si hay
                    try {
                        java.io.BufferedReader errorReader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getErrorStream()));
                        String errorResponse = errorReader.readLine();
                        errorReader.close();
                        System.err.println("Error del servidor: " + errorResponse);
                    } catch (Exception e) {
                        System.err.println("No se pudo leer el error del servidor");
                    }
                }
                
            } catch (Exception phpError) {
                System.err.println("Error con script PHP: " + phpError.getMessage());
                phpError.printStackTrace();
            }
            
            // Método alternativo: WebDAV (si está habilitado)
            try {
                java.net.URL url = new java.net.URL(rutaCarpeta);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("MKCOL"); // WebDAV method
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 201 || responseCode == 405) { // 201 = Created, 405 = Already exists
                    System.out.println("✅ Carpeta creada vía WebDAV: " + rutaCarpeta);
                    return true;
                }
                
            } catch (java.net.ProtocolException webdavError) {
                System.out.println("WebDAV no soportado: " + webdavError.getMessage());
            } catch (Exception webdavError) {
                System.out.println("WebDAV falló: " + webdavError.getMessage());
            }
            
            // Si todos los métodos fallan, pero queremos continuar
            System.out.println("⚠️ No se pudo crear carpeta física, continuando con registro virtual: " + rutaCarpeta);
            return false; // Cambiar a false para mostrar que no se creó físicamente
            
        } catch (Exception e) {
            System.err.println("❌ Error en crearCarpetaEnServidorWeb: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NUEVO: Crea carpeta vía SSH (requiere configuración adicional)
     */
    private static boolean crearCarpetaViaSSH(String rutaCarpeta) {
        // Este método requeriría una librería SSH como JSch
        // Por ahora, solo mostrar lo que se haría
        System.out.println("SSH: mkdir -p " + rutaCarpeta.replace(RUTA_BASE_SERVIDOR, "/var/www/html/miet20/boletines/"));
        return false; // No implementado por defecto
    }

    /**
     * CORREGIDO: Crear InfoBoletin con formato numérico correcto
     */
    private static InfoBoletin crearInfoBoletinDesdeBD(int alumnoId, int cursoId, String periodo) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return null;
            }

            String query = """
                SELECT u.nombre, u.apellido, u.dni, c.anio, c.division
                FROM usuarios u 
                INNER JOIN cursos c ON c.id = ?
                WHERE u.id = ?
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setInt(2, alumnoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                InfoBoletin info = new InfoBoletin();
                info.alumnoId = alumnoId;
                info.alumnoNombre = rs.getString("apellido") + ", " + rs.getString("nombre");
                info.alumnoDni = rs.getString("dni");
                info.cursoId = cursoId;
                info.curso = String.valueOf(rs.getInt("anio"));

                // CORREGIDO: Usar formato numérico para división
                int divisionNum = rs.getInt("division");
                info.division = String.valueOf(divisionNum); // "1", "2", "3", etc.

                info.anioLectivo = LocalDate.now().getYear();
                info.periodo = periodo;

                System.out.println("InfoBoletin creado - Curso: " + info.curso + info.division + " (formato numérico)");
                return info;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo datos de BD: " + e.getMessage());
        }

        return null;
    }

    /**
     * Registra información del boletín en la base de datos
     */
    private static boolean registrarBoletinEnBD(InfoBoletin info) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return false;
            }

            String periodoParaBD = MAPEO_PERIODOS.getOrDefault(info.periodo, info.periodo);

            // Verificar si ya existe
            String queryVerificar = """
                SELECT id FROM boletin 
                WHERE alumno_id = ? AND curso_id = ? AND anio_lectivo = ? AND periodo = ?
                """;

            PreparedStatement psVerificar = conect.prepareStatement(queryVerificar);
            psVerificar.setInt(1, info.alumnoId);
            psVerificar.setInt(2, info.cursoId);
            psVerificar.setInt(3, info.anioLectivo);
            psVerificar.setString(4, periodoParaBD);
            ResultSet rs = psVerificar.executeQuery();

            boolean existe = rs.next();
            int boletinId = existe ? rs.getInt("id") : -1;

            rs.close();
            psVerificar.close();

            if (existe) {
                // Actualizar registro
                String queryUpdate = """
                    UPDATE boletin SET 
                    fecha_emision = NOW(),
                    estado = 'publicado',
                    publicado_at = NOW(),
                    observaciones = ?
                    WHERE id = ?
                    """;

                PreparedStatement psUpdate = conect.prepareStatement(queryUpdate);
                psUpdate.setString(1, "Boletín en servidor - URL: " + info.rutaArchivo);
                psUpdate.setInt(2, boletinId);

                int filasAfectadas = psUpdate.executeUpdate();
                psUpdate.close();
                return filasAfectadas > 0;

            } else {
                // Crear nuevo registro
                String queryInsert = """
                    INSERT INTO boletin (alumno_id, curso_id, anio_lectivo, periodo, fecha_emision, 
                                       observaciones, estado, publicado_at, creado_por) 
                    VALUES (?, ?, ?, ?, NOW(), ?, 'publicado', NOW(), 1)
                    """;

                PreparedStatement psInsert = conect.prepareStatement(queryInsert);
                psInsert.setInt(1, info.alumnoId);
                psInsert.setInt(2, info.cursoId);
                psInsert.setInt(3, info.anioLectivo);
                psInsert.setString(4, periodoParaBD);
                psInsert.setString(5, "Boletín en servidor - URL: " + info.rutaArchivo);

                int filasAfectadas = psInsert.executeUpdate();
                psInsert.close();
                return filasAfectadas > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al registrar boletín en BD: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene boletines disponibles (SIMPLIFICADO - solo desde BD)
     */
    public static List<InfoBoletin> obtenerBoletinesDisponibles(int anioLectivo, String curso, String division, String periodo) {
        List<InfoBoletin> boletines = new ArrayList<>();

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return boletines;
            }

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("""
                SELECT b.id, b.alumno_id, b.curso_id, b.anio_lectivo, b.periodo, b.fecha_emision, b.estado,
                       u.nombre, u.apellido, u.dni,
                       c.anio as curso_anio, c.division as curso_division
                FROM boletin b
                INNER JOIN usuarios u ON b.alumno_id = u.id
                INNER JOIN cursos c ON b.curso_id = c.id
                WHERE b.anio_lectivo = ?
                """);

            List<Object> parametros = new ArrayList<>();
            parametros.add(anioLectivo);

            if (curso != null && !curso.isEmpty() && !curso.equals("TODOS")) {
                queryBuilder.append(" AND c.anio = ?");
                parametros.add(Integer.parseInt(curso));
            }

            if (division != null && !division.isEmpty() && !division.equals("TODAS")) {
                queryBuilder.append(" AND c.division = ?");
                parametros.add(division.charAt(0) - 'A' + 1);
            }

            if (periodo != null && !periodo.isEmpty() && !periodo.equals("TODOS")) {
                String periodoParaBD = MAPEO_PERIODOS.getOrDefault(periodo, periodo);
                queryBuilder.append(" AND b.periodo = ?");
                parametros.add(periodoParaBD);
            }

            queryBuilder.append(" ORDER BY c.anio, c.division, u.apellido, u.nombre");

            PreparedStatement ps = conect.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                InfoBoletin info = new InfoBoletin();
                info.alumnoId = rs.getInt("alumno_id");
                info.alumnoNombre = rs.getString("apellido") + ", " + rs.getString("nombre");
                info.alumnoDni = rs.getString("dni");
                info.cursoId = rs.getInt("curso_id");
                info.curso = String.valueOf(rs.getInt("curso_anio"));

                int divisionNum = rs.getInt("curso_division");
                info.division = String.valueOf((char) ('A' + divisionNum - 1));

                info.anioLectivo = rs.getInt("anio_lectivo");
                info.estadoBoletin = rs.getString("estado");

                String periodoBD = rs.getString("periodo");
                info.periodo = mapearPeriodoDesdeBD(periodoBD);

                // Generar URL esperada (sin verificar si existe físicamente)
                info.rutaArchivo = generarUrlBoletinEsperada(info);
                info.archivoExiste = true; // Asumimos que existe en servidor
                info.tamanioArchivo = 0; // No podemos verificar tamaño desde URL

                boletines.add(info);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener boletines: " + e.getMessage());
        }

        return boletines;
    }

    /**
     * Genera URL esperada del boletín
     */
    private static String generarUrlBoletinEsperada(InfoBoletin info) {
        String nombreCurso = info.curso + info.division;
        String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nombreLimpio = info.alumnoNombre.replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "_");
        String nombreArchivo = String.format("%s_%s_%s_%s.xlsx", nombreLimpio, nombreCurso, info.periodo, fechaStr);

        return RUTA_BASE_SERVIDOR + info.anioLectivo + "/" + nombreCurso + "/" + info.periodo + "/" + nombreArchivo;
    }

    /**
     * Mapea período desde BD a formato corto
     */
    private static String mapearPeriodoDesdeBD(String periodoBD) {
        for (java.util.Map.Entry<String, String> entry : MAPEO_PERIODOS.entrySet()) {
            if (entry.getValue().equals(periodoBD)) {
                return entry.getKey();
            }
        }
        return periodoBD;
    }

    /**
     * Obtiene boletines de un alumno específico
     */
    public static List<InfoBoletin> obtenerBoletinesAlumno(int alumnoId, int anioLectivo) {
        return obtenerBoletinesDisponibles(anioLectivo, null, null, null)
                .stream()
                .filter(boletin -> boletin.alumnoId == alumnoId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Elimina un boletín (solo registro de BD)
     */
    public static boolean eliminarBoletin(InfoBoletin info) {
        try {
            System.out.println("🗑️ Eliminando registro de boletín: " + info.alumnoNombre + " - " + info.periodo);

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return false;
            }

            String periodoParaBD = MAPEO_PERIODOS.getOrDefault(info.periodo, info.periodo);

            String query = """
                DELETE FROM boletin 
                WHERE alumno_id = ? AND curso_id = ? AND anio_lectivo = ? AND periodo = ?
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, info.alumnoId);
            ps.setInt(2, info.cursoId);
            ps.setInt(3, info.anioLectivo);
            ps.setString(4, periodoParaBD);

            int filasEliminadas = ps.executeUpdate();
            ps.close();

            if (filasEliminadas > 0) {
                System.out.println("✅ Registro eliminado de BD");
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Error eliminando boletín: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas simplificadas
     */
    public static String obtenerEstadisticas(int anioLectivo) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return "Error de conexión";
            }

            StringBuilder stats = new StringBuilder();
            stats.append("=== ESTADÍSTICAS DE BOLETINES ===\n");
            stats.append("Año lectivo: ").append(anioLectivo).append("\n");
            stats.append("Servidor: ").append(RUTA_BASE_SERVIDOR).append("\n\n");

            // Total de boletines
            String queryTotal = "SELECT COUNT(*) as total FROM boletin WHERE anio_lectivo = ?";
            PreparedStatement ps = conect.prepareStatement(queryTotal);
            ps.setInt(1, anioLectivo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                stats.append("📊 Total boletines registrados: ").append(rs.getInt("total")).append("\n");
            }
            rs.close();
            ps.close();

            // Boletines por período
            String queryPeriodos = """
                SELECT periodo, COUNT(*) as cantidad 
                FROM boletin 
                WHERE anio_lectivo = ? 
                GROUP BY periodo 
                ORDER BY periodo
                """;

            ps = conect.prepareStatement(queryPeriodos);
            ps.setInt(1, anioLectivo);
            rs = ps.executeQuery();

            stats.append("\n📅 Boletines por período:\n");
            while (rs.next()) {
                String periodo = rs.getString("periodo");
                String periodoCorto = mapearPeriodoDesdeBD(periodo);
                stats.append("  ").append(periodoCorto).append(": ").append(rs.getInt("cantidad")).append("\n");
            }
            rs.close();
            ps.close();

            return stats.toString();

        } catch (SQLException e) {
            return "Error obteniendo estadísticas: " + e.getMessage();
        }
    }

    /**
     * Verifica estructura (simplificado - solo verifica BD)
     */
    public static boolean verificarEstructuraCarpetas(int anioLectivo) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return false;
            }

            String query = "SELECT COUNT(*) as total FROM cursos WHERE estado = 'activo'";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            boolean hayEstructura = false;
            if (rs.next()) {
                hayEstructura = rs.getInt("total") > 0;
            }

            rs.close();
            ps.close();

            System.out.println((hayEstructura ? "✅" : "❌") + " Estructura verificada para año: " + anioLectivo);
            return hayEstructura;

        } catch (SQLException e) {
            System.err.println("Error verificando estructura: " + e.getMessage());
            return false;
        }
    }

    /**
     * Limpieza simplificada (solo BD)
     */
    public static int limpiarBoletinesAntiguos(int aniosAntiguedad) {
        try {
            int anioLimite = LocalDate.now().getYear() - aniosAntiguedad;

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return 0;
            }

            String query = "DELETE FROM boletin WHERE anio_lectivo < ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, anioLimite);

            int eliminados = ps.executeUpdate();
            ps.close();

            System.out.println("✅ Registros antiguos eliminados: " + eliminados);
            return eliminados;

        } catch (SQLException e) {
            System.err.println("Error en limpieza: " + e.getMessage());
            return 0;
        }
    }

    /**
     * NUEVO: Guarda un boletín automáticamente en el servidor. Este método es
     * llamado por PlantillaBoletinUtility.
     *
     * @param rutaArchivoTemporal Ruta del archivo temporal generado
     * @param alumnoId ID del alumno
     * @param cursoId ID del curso
     * @param periodo Período del boletín
     * @return true si se guardó exitosamente
     */
    public static boolean guardarBoletinAutomatico(String rutaArchivoTemporal, int alumnoId, int cursoId, String periodo) {
        try {
            System.out.println("=== GUARDANDO BOLETÍN AUTOMÁTICO ===");
            System.out.println("Archivo temporal: " + rutaArchivoTemporal);
            System.out.println("Alumno ID: " + alumnoId + ", Curso ID: " + cursoId + ", Período: " + periodo);

            // Verificar que el archivo temporal existe
            java.io.File archivoTemporal = new java.io.File(rutaArchivoTemporal);
            if (!archivoTemporal.exists()) {
                System.err.println("❌ El archivo temporal no existe: " + rutaArchivoTemporal);
                return false;
            }

            // Obtener información del alumno y curso
            InfoBoletin info = crearInfoBoletinDesdeBD(alumnoId, cursoId, periodo);
            if (info == null) {
                System.err.println("❌ No se pudo obtener información del boletín");
                return false;
            }

            // Generar nombre final del archivo
            String fechaStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nombreLimpio = info.alumnoNombre.replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "_");
            String nombreFinal = String.format("Boletin_%s_%s%s_%s_%s.xlsx",
                    nombreLimpio, info.curso, info.division, periodo, fechaStr);

            // Para efectos de demostración, solo registramos en BD
            // En producción, aquí harías la subida real al servidor
            System.out.println("📁 Archivo que se subiría al servidor: " + nombreFinal);
            System.out.println("🌐 URL de destino: " + generarUrlBoletin(info, nombreFinal));

            // Registrar en base de datos
            info.rutaArchivo = generarUrlBoletin(info, nombreFinal);
            info.nombreArchivo = nombreFinal;
            info.archivoExiste = true;

            boolean registrado = registrarBoletinEnBD(info);

            if (registrado) {
                System.out.println("✅ Boletín registrado exitosamente en BD");

                // OPCIONAL: Mover el archivo temporal a una carpeta de "listos para subir"
                try {
                    String carpetaSubida = System.getProperty("java.io.tmpdir") + java.io.File.separator + "boletines_para_subir";
                    java.io.File dirSubida = new java.io.File(carpetaSubida);
                    if (!dirSubida.exists()) {
                        dirSubida.mkdirs();
                    }

                    java.io.File archivoDestino = new java.io.File(dirSubida, nombreFinal);
                    java.nio.file.Files.copy(archivoTemporal.toPath(), archivoDestino.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("📋 Archivo copiado para subida posterior: " + archivoDestino.getAbsolutePath());

                } catch (Exception e) {
                    System.err.println("⚠️ Error al copiar archivo para subida: " + e.getMessage());
                    // No es crítico, el registro en BD ya se hizo
                }

                return true;
            } else {
                System.err.println("❌ Error al registrar boletín en BD");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error en guardarBoletinAutomatico: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
