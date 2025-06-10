package main.java.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.java.database.Conexion;
import main.java.utils.pdf.PdfBoletinUtility;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utilidad para generar boletines usando plantilla Excel institucional. VERSIÓN
 * SERVIDOR: Los boletines se guardan automáticamente en el servidor sin
 * preguntar al usuario la ubicación.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 3.0 - Servidor Automático - CORREGIDA
 */
public class PlantillaBoletinUtility {

    // Configuración de la plantilla (ahora en el servidor)
    private static String RUTA_PLANTILLA_SERVIDOR = null; // Se inicializará dinámicamente

    // Mapeo de períodos para compatibilidad con BD
    private static final Map<String, String> MAPEO_PERIODOS = new HashMap<>();

    private static int USUARIO_ACTUAL_SISTEMA = -1;

    /**
     * Establece el usuario actual del sistema para el registro de boletines
     */
    public static void establecerUsuarioActual(int usuarioId) {
        USUARIO_ACTUAL_SISTEMA = usuarioId;
        System.out.println("Usuario actual establecido: " + usuarioId);
    }

    /**
     * Obtiene el usuario actual del sistema, buscando uno válido si no está
     * establecido
     */
    public static int obtenerUsuarioActual() {
        if (USUARIO_ACTUAL_SISTEMA > 0) {
            return USUARIO_ACTUAL_SISTEMA;
        }

        // Si no está establecido, buscar un usuario válido
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return -1;
            }

            // Buscar primer administrador activo
            String query = "SELECT id FROM usuarios WHERE rol = '1' AND status = 1 ORDER BY id LIMIT 1";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                USUARIO_ACTUAL_SISTEMA = userId;
                rs.close();
                ps.close();
                System.out.println("Usuario admin encontrado y establecido: " + userId);
                return userId;
            }

            rs.close();
            ps.close();

            // Si no hay admin, buscar cualquier usuario activo
            String queryFallback = "SELECT id FROM usuarios WHERE status = 1 ORDER BY id LIMIT 1";
            PreparedStatement psFallback = conect.prepareStatement(queryFallback);
            ResultSet rsFallback = psFallback.executeQuery();

            if (rsFallback.next()) {
                int userId = rsFallback.getInt("id");
                USUARIO_ACTUAL_SISTEMA = userId;
                rsFallback.close();
                psFallback.close();
                System.out.println("Usuario fallback encontrado y establecido: " + userId);
                return userId;
            }

            rsFallback.close();
            psFallback.close();

            System.err.println("❌ No se encontraron usuarios activos en el sistema");
            return -1;

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo usuario del sistema: " + e.getMessage());
            return -1;
        }
    }

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
     * Inicializa la ruta de la plantilla basada en el servidor configurado
     */
    private static void inicializarRutaPlantilla() {
        if (RUTA_PLANTILLA_SERVIDOR == null) {
            String servidorBase = GestorBoletines.obtenerRutaServidor();

            // CORREGIDO: Siempre usar la ruta del servidor para la plantilla
            if (servidorBase.startsWith("http://") || servidorBase.startsWith("https://")) {
                // Para servidor web, construir URL completa de la plantilla
                RUTA_PLANTILLA_SERVIDOR = servidorBase + "PlantillaBoletines.xlsx";
            } else {
                // Para servidor local, buscar plantilla en la carpeta del servidor
                RUTA_PLANTILLA_SERVIDOR = servidorBase + File.separator + "PlantillaBoletines.xlsx";
            }

            System.out.println("Ruta de plantilla inicializada: " + RUTA_PLANTILLA_SERVIDOR);
        }
    }

    /**
     * Obtiene la ruta de la plantilla
     */
    public static String obtenerRutaPlantilla() {
        inicializarRutaPlantilla();
        return RUTA_PLANTILLA_SERVIDOR;
    }

    /**
     * Configura la ruta de la plantilla
     */
    public static void configurarRutaPlantilla(String nuevaRuta) {
        // CORREGIDO: Si se proporciona una ruta, usarla; sino, usar la del servidor
        if (nuevaRuta != null && !nuevaRuta.trim().isEmpty()) {
            RUTA_PLANTILLA_SERVIDOR = nuevaRuta;
        } else {
            // Si no se especifica ruta, usar la del servidor automáticamente
            String servidorBase = GestorBoletines.obtenerRutaServidor();
            if (servidorBase.startsWith("http://") || servidorBase.startsWith("https://")) {
                RUTA_PLANTILLA_SERVIDOR = servidorBase + "PlantillaBoletines.xlsx";
            } else {
                RUTA_PLANTILLA_SERVIDOR = servidorBase + File.separator + "PlantillaBoletines.xlsx";
            }
        }

        System.out.println("Nueva ruta de plantilla configurada: " + RUTA_PLANTILLA_SERVIDOR);
    }

    /**
     * MÉTODO PRINCIPAL: Genera un boletín individual guardándolo
     * automáticamente en el servidor
     */
    public static boolean generarBoletinIndividualEnServidor(int alumnoId, int cursoId, String periodo) {
        try {
            System.out.println("=== GENERANDO BOLETÍN EN SERVIDOR (AUTOMÁTICO) ===");
            System.out.println("Alumno ID: " + alumnoId + ", Curso ID: " + cursoId + ", Período: " + periodo);

            // 1. Construir ruta de destino automáticamente
            String rutaDestino = construirRutaDestinoServidor(alumnoId, cursoId, periodo);
            if (rutaDestino == null) {
                System.err.println("❌ No se pudo construir la ruta de destino");
                return false;
            }

            // 2. Obtener datos del estudiante
            DatosEstudiante datosEstudiante = obtenerDatosEstudiante(alumnoId, cursoId);
            if (datosEstudiante == null) {
                System.err.println("❌ No se pudieron obtener los datos del estudiante");
                return false;
            }

            // 3. Obtener notas del estudiante
            Map<String, NotasMateria> notasPorMateria = obtenerNotasEstudiante(alumnoId, cursoId, periodo);

            // 4. Generar el boletín
            boolean exito = generarBoletinConPlantilla(datosEstudiante, notasPorMateria, periodo, rutaDestino);

            if (exito) {
                System.out.println("✅ Boletín generado exitosamente: " + rutaDestino);

                // 5. Registrar en base de datos
                String nombreArchivo = new File(rutaDestino).getName();
                GestorBoletines.registrarBoletinEnServidor(alumnoId, cursoId, periodo, nombreArchivo);

                return true;
            } else {
                System.err.println("❌ Error al generar el boletín");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinIndividualEnServidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Genera boletines para todo el curso automáticamente en el servidor
     */
    public static int generarBoletinesCursoEnServidor(int cursoId, String periodo) {
        try {
            System.out.println("=== GENERANDO BOLETINES DEL CURSO EN SERVIDOR ===");
            System.out.println("Curso ID: " + cursoId + ", Período: " + periodo);

            // Obtener lista de alumnos del curso
            List<Integer> alumnosIds = obtenerAlumnosDelCurso(cursoId);

            if (alumnosIds.isEmpty()) {
                System.out.println("⚠️ No se encontraron alumnos en el curso");
                return 0;
            }

            System.out.println("Alumnos encontrados: " + alumnosIds.size());

            int boletinesGenerados = 0;

            for (int i = 0; i < alumnosIds.size(); i++) {
                int alumnoId = alumnosIds.get(i);

                try {
                    System.out.println("Procesando alumno " + (i + 1) + "/" + alumnosIds.size() + " (ID: " + alumnoId + ")");

                    if (generarBoletinIndividualEnServidor(alumnoId, cursoId, periodo)) {
                        boletinesGenerados++;
                        System.out.println("✅ Boletín " + boletinesGenerados + "/" + alumnosIds.size() + " generado");
                    } else {
                        System.err.println("❌ Error generando boletín para alumno ID: " + alumnoId);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error con alumno " + alumnoId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("=== GENERACIÓN MASIVA COMPLETADA ===");
            System.out.println("Boletines generados: " + boletinesGenerados + "/" + alumnosIds.size());

            return boletinesGenerados;

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinesCursoEnServidor: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Construye automáticamente la ruta de destino en el servidor
     */
    private static String construirRutaDestinoServidor(int alumnoId, int cursoId, String periodo) {
        try {
            // Obtener información básica del alumno y curso
            DatosEstudiante datos = obtenerDatosEstudiante(alumnoId, cursoId);
            if (datos == null) {
                return null;
            }

            // Construir ruta de carpeta: servidor/año/curso/periodo/
            String servidorBase = GestorBoletines.obtenerRutaServidor();
            int anioActual = LocalDate.now().getYear();

            // CORREGIDO: Formato numérico para cursos (ej: "11", "12", "21", etc.)
            String nombreCurso = datos.curso + datos.division; // "1" + "1" = "11"

            String rutaCarpeta;
            if (servidorBase.startsWith("http://") || servidorBase.startsWith("https://")) {
                // Para servidor web, construir URL
                rutaCarpeta = servidorBase + anioActual + "/" + nombreCurso + "/" + periodo + "/";
            } else {
                // Para servidor local, construir ruta de archivo
                rutaCarpeta = servidorBase + File.separator + anioActual + File.separator
                        + nombreCurso + File.separator + periodo + File.separator;
            }

            // Crear la carpeta si no existe (solo para rutas locales)
            if (!servidorBase.startsWith("http")) {
                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) {
                    boolean creada = carpeta.mkdirs();
                    if (creada) {
                        System.out.println("✅ Carpeta creada: " + rutaCarpeta);
                    } else {
                        System.err.println("⚠️ No se pudo crear la carpeta: " + rutaCarpeta);
                    }
                }
            }

            // Generar nombre de archivo
            String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String apellidoLimpio = datos.apellidoNombre.split(",")[0].replaceAll("[^a-zA-Z0-9]", "_");
            String nombreArchivo = String.format("Boletin_%s_%s_%s_%s.xlsx",
                    apellidoLimpio, nombreCurso, periodo, fechaStr);

            String rutaCompleta = rutaCarpeta + nombreArchivo;
            System.out.println("✅ Ruta de destino construida: " + rutaCompleta);

            return rutaCompleta;

        } catch (Exception e) {
            System.err.println("❌ Error construyendo ruta de destino: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Genera el boletín usando la plantilla Excel - VERSIÓN CORREGIDA
     */
    private static boolean generarBoletinConPlantilla(DatosEstudiante estudiante,
            Map<String, NotasMateria> notasPorMateria,
            String periodo,
            String rutaDestinoServidor) {

        File archivoTemporal = null;

        try {
            System.out.println("=== GENERANDO BOLETÍN PARA SERVIDOR (VERSIÓN CORREGIDA) ===");
            System.out.println("Alumno: " + estudiante.apellidoNombre);
            System.out.println("Destino servidor: " + rutaDestinoServidor);

            // PASO 1: Crear archivo temporal local
            archivoTemporal = File.createTempFile("boletin_temp_", ".xlsx");
            String rutaTemporal = archivoTemporal.getAbsolutePath();
            System.out.println("📁 Archivo temporal creado: " + rutaTemporal);

            // PASO 2: Cargar plantilla desde servidor HTTP
            String rutaPlantilla = obtenerRutaPlantilla();
            Workbook workbook = cargarPlantillaDesdeServidor(rutaPlantilla);

            if (workbook == null) {
                return false;
            }

            Sheet hoja = workbook.getSheetAt(0);

            // PASO 3: Llenar datos del estudiante y notas CON LOS MÉTODOS CORREGIDOS
            llenarDatosEstudiante(hoja, estudiante, periodo);

            // Obtener IDs necesarios para las inasistencias
            int alumnoId = obtenerAlumnoIdDesdeDatos(estudiante);
            int cursoId = obtenerCursoIdDesdeDatos(estudiante);

            llenarNotasMaterias(hoja, notasPorMateria, periodo, alumnoId, cursoId);

            // PASO 4: Guardar en archivo temporal local
            try (FileOutputStream fos = new FileOutputStream(rutaTemporal)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("✅ Boletín generado localmente con datos completos");

            // PASO 5: Subir archivo al servidor
            boolean subidaExitosa = subirArchivoAServidor(rutaTemporal, rutaDestinoServidor);

            if (subidaExitosa) {
                System.out.println("✅ Boletín subido exitosamente al servidor");
                return true;
            } else {
                System.err.println("❌ Error al subir boletín al servidor");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error generando boletín: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            // PASO 6: Limpiar archivo temporal
            if (archivoTemporal != null && archivoTemporal.exists()) {
                boolean eliminado = archivoTemporal.delete();
                if (eliminado) {
                    System.out.println("🗑️ Archivo temporal eliminado");
                } else {
                    System.err.println("⚠️ No se pudo eliminar archivo temporal: " + archivoTemporal.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Carga la plantilla desde el servidor HTTP
     */
    private static Workbook cargarPlantillaDesdeServidor(String rutaPlantilla) {
        try {
            System.out.println("📥 Descargando plantilla desde: " + rutaPlantilla);

            java.net.URL url = new java.net.URL(rutaPlantilla);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000); // 15 segundos
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "JavaApp-BoletinGenerator/1.0");

            int responseCode = connection.getResponseCode();
            System.out.println("🌐 Código de respuesta HTTP: " + responseCode);

            if (responseCode == 200) {
                try (java.io.InputStream inputStream = connection.getInputStream()) {
                    Workbook workbook = new XSSFWorkbook(inputStream);
                    System.out.println("✅ Plantilla cargada exitosamente desde servidor");
                    return workbook;
                }
            } else {
                System.err.println("❌ Error HTTP " + responseCode + " al descargar plantilla");
                // Intentar leer mensaje de error
                try (java.io.InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        String errorMsg = new String(errorStream.readAllBytes());
                        System.err.println("Mensaje de error del servidor: " + errorMsg);
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo leer el mensaje de error del servidor");
                }
                return null;
            }

        } catch (java.net.MalformedURLException e) {
            System.err.println("❌ URL de plantilla malformada: " + rutaPlantilla);
            return null;
        } catch (java.io.IOException e) {
            System.err.println("❌ Error de conexión al descargar plantilla: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al cargar plantilla: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sube el archivo generado al servidor usando HTTP POST
     */
    private static boolean subirArchivoAServidor(String rutaArchivoLocal, String rutaDestinoServidor) {
        try {
            System.out.println("📤 Subiendo archivo al servidor...");
            System.out.println("Origen: " + rutaArchivoLocal);
            System.out.println("Destino: " + rutaDestinoServidor);

            // CORREGIDO: Construir URL del script de subida en la ubicación correcta
            java.net.URL urlDestino = new java.net.URL(rutaDestinoServidor);
            String baseUrl = urlDestino.getProtocol() + "://" + urlDestino.getHost();
            if (urlDestino.getPort() != -1) {
                baseUrl += ":" + urlDestino.getPort();
            }

            // CAMBIO PRINCIPAL: Agregar /miet20/ a la ruta del script
            String uploadScriptUrl = baseUrl + "/miet20/upload_boletin.php";
            System.out.println("Script de subida: " + uploadScriptUrl);

            // Leer archivo local
            File archivo = new File(rutaArchivoLocal);
            if (!archivo.exists()) {
                System.err.println("❌ El archivo local no existe: " + rutaArchivoLocal);
                return false;
            }

            // Extraer ruta relativa desde /miet20/boletines/
            String rutaRelativa = urlDestino.getPath();
            if (rutaRelativa.startsWith("/miet20/boletines/")) {
                rutaRelativa = rutaRelativa.substring("/miet20/boletines/".length());
            } else if (rutaRelativa.startsWith("/boletines/")) {
                rutaRelativa = rutaRelativa.substring("/boletines/".length());
            }

            System.out.println("Ruta relativa para PHP: " + rutaRelativa);

            // Preparar datos para envío
            String boundary = "----BoletinUpload" + System.currentTimeMillis();

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(uploadScriptUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("User-Agent", "JavaApp-BoletinGenerator/1.0");
            connection.setConnectTimeout(30000); // 30 segundos
            connection.setReadTimeout(60000);    // 60 segundos para subida

            try (java.io.OutputStream os = connection.getOutputStream(); java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"), true)) {

                // Enviar ruta de destino relativa
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"ruta_destino\"").append("\r\n");
                writer.append("\r\n");
                writer.append(rutaRelativa).append("\r\n");

                // Enviar archivo
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"" + archivo.getName() + "\"").append("\r\n");
                writer.append("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").append("\r\n");
                writer.append("\r\n").flush();

                // Copiar bytes del archivo
                try (java.io.FileInputStream fis = new java.io.FileInputStream(archivo)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                writer.append("\r\n");
                writer.append("--" + boundary + "--").append("\r\n");
            }

            // Verificar respuesta
            int responseCode = connection.getResponseCode();
            System.out.println("📡 Código de respuesta de subida: " + responseCode);

            if (responseCode == 200) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()))) {
                    String response = reader.readLine();
                    System.out.println("Respuesta del servidor: " + response);

                    // Verificar si la respuesta indica éxito
                    if (response != null && (response.contains("success") || response.contains("\"status\":\"success\""))) {
                        System.out.println("✅ Archivo subido exitosamente");
                        return true;
                    } else {
                        System.err.println("❌ El servidor indica error en la subida: " + response);
                        return false;
                    }
                }
            } else {
                System.err.println("❌ Error HTTP " + responseCode + " al subir archivo");

                // Leer mensaje de error si existe
                try (java.io.BufferedReader errorReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getErrorStream()))) {
                    String errorResponse = errorReader.readLine();
                    if (errorResponse != null) {
                        System.err.println("Error del servidor: " + errorResponse);
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo leer el error del servidor");
                }

                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error al subir archivo al servidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Llena los datos básicos del estudiante en la plantilla - VERSIÓN
     * CORREGIDA
     */
    private static void llenarDatosEstudiante(Sheet hoja, DatosEstudiante estudiante, String periodo) {
        try {
            System.out.println("=== LLENANDO DATOS DEL ESTUDIANTE (VERSIÓN CORREGIDA) ===");
            System.out.println("Nombre: " + estudiante.apellidoNombre);
            System.out.println("DNI: " + estudiante.dni);
            System.out.println("Curso: " + estudiante.curso + "°" + estudiante.division);
            System.out.println("Código Mi Escuela: " + estudiante.codigoMiEscuela);
            System.out.println("Período: " + periodo);

            // LLENAR DATOS EN POSICIONES ESPECÍFICAS SEGÚN TU DESCRIPCIÓN
            // C7: Año y división combinado
            String anioDivision = estudiante.curso + "°" + estudiante.division;
            setCellValueSafe(hoja, 6, 2, anioDivision); // Fila 7 (índice 6), Columna C (índice 2)
            System.out.println("✅ C7 - Año y División: " + anioDivision);

            // D7 a H7: Apellido y Nombre (combinado en las celdas)
            setCellValueSafe(hoja, 6, 3, estudiante.apellidoNombre); // D7
            System.out.println("✅ D7-H7 - Apellido y Nombre: " + estudiante.apellidoNombre);

            // J7: DNI
            setCellValueSafe(hoja, 6, 9, estudiante.dni); // Fila 7, Columna J (índice 9)
            System.out.println("✅ J7 - DNI: " + estudiante.dni);

            // L7: Código Mi Escuela
            if (estudiante.codigoMiEscuela != null && !estudiante.codigoMiEscuela.trim().isEmpty()) {
                setCellValueSafe(hoja, 6, 11, estudiante.codigoMiEscuela); // Fila 7, Columna L (índice 11)
                System.out.println("✅ L7 - Código Mi Escuela: " + estudiante.codigoMiEscuela);
            }

            // Agregar fecha actual (opcional, puedes ajustar la posición)
            String fechaActual = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Puedes agregar la fecha en alguna celda específica si es necesario
            // setCellValueSafe(hoja, filaFecha, columnaFecha, fechaActual);
            System.out.println("✅ Datos del estudiante completados en posiciones específicas");

        } catch (Exception e) {
            System.err.println("❌ Error llenando datos del estudiante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Llena las notas de las materias en la plantilla - VERSIÓN CORREGIDA
     * COMPLETA
     */
    private static void llenarNotasMaterias(Sheet hoja, Map<String, NotasMateria> notasPorMateria,
            String periodo, int alumnoId, int cursoId) {
        try {
            System.out.println("=== LLENANDO NOTAS DE MATERIAS (VERSIÓN CORREGIDA) ===");
            System.out.println("Materias a procesar: " + notasPorMateria.size());
            System.out.println("Alumno ID: " + alumnoId + ", Curso ID: " + cursoId);

            if (notasPorMateria.isEmpty()) {
                System.out.println("⚠️ No hay materias para llenar");
                return;
            }

            // CONFIGURACIÓN DE POSICIONES SEGÚN TU DESCRIPCIÓN
            int filaInicioMaterias = 11; // ABC 12-21 (índices 11-20)
            int filaFinMaterias = 20;    // Hasta la fila 21 (índice 20)

            // Columnas para las notas (según tu descripción)
            int colNombreMateria = 0;     // Columna A (índice 0) - Combinado ABC
            int col1Bimestre = 3;         // Columna D (índice 3) - 1° Bimestre
            int col2Bimestre = 4;         // Columna E (índice 4) - 2° Bimestre  
            int col1Cuatrimestre = 5;     // Columna F (índice 5) - 1° Cuatrimestre
            int col3Bimestre = 6;         // Columna G (índice 6) - 3° Bimestre
            int col4Bimestre = 7;         // Columna H (índice 7) - 4° Bimestre
            int col2Cuatrimestre = 8;     // Columna I (índice 8) - 2° Cuatrimestre
            int colCalifAnual = 9;        // Columna J (índice 9) - Calificación Anual
            int colDiciembre = 10;        // Columna K (índice 10) - Diciembre
            int colFebrero = 11;          // Columna L (índice 11) - Febrero
            int colDefinitiva = 12;       // Columna M (índice 12) - Definitiva

            // Listas para clasificar materias
            List<String> materiasGenerales = new ArrayList<>();
            List<String> materiasTroncales = new ArrayList<>();
            List<String> materiasEnProceso = new ArrayList<>();

            int filaActual = filaInicioMaterias;
            int materiasLlenadas = 0;

            System.out.println("Llenando materias desde fila " + (filaInicioMaterias + 1) + " hasta fila " + (filaFinMaterias + 1));

            for (Map.Entry<String, NotasMateria> entry : notasPorMateria.entrySet()) {
                if (filaActual > filaFinMaterias) {
                    System.out.println("⚠️ Se alcanzó el límite de filas para materias");
                    break;
                }

                String nombreMateria = entry.getKey();
                NotasMateria notas = entry.getValue();

                System.out.println("Procesando materia: " + nombreMateria + " en fila " + (filaActual + 1));

                // Llenar nombre de la materia en columnas combinadas A, B, C
                setCellValueSafe(hoja, filaActual, colNombreMateria, nombreMateria);

                // Llenar notas bimestrales
                if (notas.bimestre1 > 0) {
                    setCellValueSafe(hoja, filaActual, col1Bimestre, formatearNota(notas.bimestre1));
                    System.out.println("  1° Bim: " + notas.bimestre1);
                }
                if (notas.bimestre2 > 0) {
                    setCellValueSafe(hoja, filaActual, col2Bimestre, formatearNota(notas.bimestre2));
                    System.out.println("  2° Bim: " + notas.bimestre2);
                }
                if (notas.bimestre3 > 0) {
                    setCellValueSafe(hoja, filaActual, col3Bimestre, formatearNota(notas.bimestre3));
                    System.out.println("  3° Bim: " + notas.bimestre3);
                }
                if (notas.bimestre4 > 0) {
                    setCellValueSafe(hoja, filaActual, col4Bimestre, formatearNota(notas.bimestre4));
                    System.out.println("  4° Bim: " + notas.bimestre4);
                }

                // Llenar cuatrimestres (calcular si no existen)
                double cuatrimestre1 = notas.cuatrimestre1;
                if (cuatrimestre1 <= 0 && notas.bimestre1 > 0 && notas.bimestre2 > 0) {
                    cuatrimestre1 = (notas.bimestre1 + notas.bimestre2) / 2;
                }
                if (cuatrimestre1 > 0) {
                    setCellValueSafe(hoja, filaActual, col1Cuatrimestre, formatearNota(cuatrimestre1));
                    System.out.println("  1° Cuat: " + cuatrimestre1);
                }

                double cuatrimestre2 = notas.cuatrimestre2;
                if (cuatrimestre2 <= 0 && notas.bimestre3 > 0 && notas.bimestre4 > 0) {
                    cuatrimestre2 = (notas.bimestre3 + notas.bimestre4) / 2;
                }
                if (cuatrimestre2 > 0) {
                    setCellValueSafe(hoja, filaActual, col2Cuatrimestre, formatearNota(cuatrimestre2));
                    System.out.println("  2° Cuat: " + cuatrimestre2);
                }

                // Calcular calificación anual
                if (cuatrimestre1 > 0 && cuatrimestre2 > 0) {
                    double califAnual = (cuatrimestre1 + cuatrimestre2) / 2;
                    setCellValueSafe(hoja, filaActual, colCalifAnual, formatearNota(califAnual));
                    System.out.println("  Anual: " + califAnual);
                }

                // Llenar exámenes
                if (notas.diciembre > 0) {
                    setCellValueSafe(hoja, filaActual, colDiciembre, formatearNota(notas.diciembre));
                    System.out.println("  Diciembre: " + notas.diciembre);
                }
                if (notas.febrero > 0) {
                    setCellValueSafe(hoja, filaActual, colFebrero, formatearNota(notas.febrero));
                    System.out.println("  Febrero: " + notas.febrero);
                }

                // Calcular definitiva
                double definitiva = calcularNotaDefinitiva(notas, cuatrimestre1, cuatrimestre2);
                if (definitiva > 0) {
                    setCellValueSafe(hoja, filaActual, colDefinitiva, formatearNota(definitiva));
                    System.out.println("  Definitiva: " + definitiva);
                }

                // Clasificar materia para resumen
                clasificarMateria(nombreMateria, notas, materiasGenerales, materiasTroncales, materiasEnProceso);

                filaActual++;
                materiasLlenadas++;
            }

            System.out.println("✅ Materias llenadas: " + materiasLlenadas);

            // Llenar resúmenes de materias
            llenarResumenMaterias(hoja, materiasGenerales, materiasTroncales, materiasEnProceso);

            // Llenar inasistencias reales
            if (alumnoId > 0 && cursoId > 0) {
                llenarInasistencias(hoja, alumnoId, cursoId);
            } else {
                System.out.println("⚠️ No se pudieron obtener IDs para inasistencias");
            }

            System.out.println("✅ Notas de materias completadas");

        } catch (Exception e) {
            System.err.println("❌ Error llenando notas de materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formatea una nota para mostrar con hasta 2 decimales
     */
    private static String formatearNota(double nota) {
        if (nota <= 0) {
            return "";
        }

        // Si es un número entero, mostrar sin decimales
        if (nota == Math.floor(nota)) {
            return String.valueOf((int) nota);
        }

        // Sino, mostrar con hasta 2 decimales
        return String.format("%.2f", nota).replace(",", ".");
    }

    /**
     * Calcula la nota definitiva según la lógica del boletín
     */
    private static double calcularNotaDefinitiva(NotasMateria notas, double cuatrimestre1, double cuatrimestre2) {
        try {
            // Lógica básica: si hay nota anual (promedio de cuatrimestres)
            if (cuatrimestre1 > 0 && cuatrimestre2 > 0) {
                double notaAnual = (cuatrimestre1 + cuatrimestre2) / 2;

                // Si la nota anual es >= 6, esa es la definitiva
                if (notaAnual >= 6) {
                    return notaAnual;
                }

                // Si es < 6, verificar exámenes
                if (notas.diciembre > 0) {
                    return notas.diciembre;
                }

                if (notas.febrero > 0) {
                    return notas.febrero;
                }

                // Si no hay exámenes, devolver la anual
                return notaAnual;
            }

            return 0;

        } catch (Exception e) {
            System.err.println("Error calculando nota definitiva: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Clasifica una materia según su tipo y estado PREPARADO PARA BD -
     * Comentado hasta que implementes la clasificación en BD
     */
    private static void clasificarMateria(String nombreMateria, NotasMateria notas,
            List<String> materiasGenerales,
            List<String> materiasTroncales,
            List<String> materiasEnProceso) {
        try {
            // IMPORTANTE: Estas listas NO se usan para clasificar materias del año actual
            // Solo se mantienen por compatibilidad, pero los resúmenes A26/B26, F26/G26, A29/B29
            // son EXCLUSIVAMENTE para materias PREVIAS de años anteriores

            // TODO: IMPLEMENTAR CLASIFICACIÓN DESDE BD
            /*
            // Futura implementación desde BD:
            String tipoMateria = obtenerTipoMateriaDesdeDB(nombreMateria);
            boolean esTroncal = "TRONCAL".equals(tipoMateria);
            boolean esGeneral = "GENERAL".equals(tipoMateria);
            
            // Para materias previas, consultar tabla específica:
            boolean esPreviaAnterior = verificarMateriaPreviaDesdeDB(nombreMateria, alumnoId);
             */
            // CLASIFICACIÓN TEMPORAL (hasta que implementes la BD)
            String[] troncalesComunes = {
                "Matemática", "Matemáticas", "Lengua", "Lengua y Literatura",
                "Historia", "Geografía", "Biología", "Física", "Química"
            };

            boolean esTroncal = false;
            for (String troncal : troncalesComunes) {
                if (nombreMateria.toLowerCase().contains(troncal.toLowerCase())) {
                    esTroncal = true;
                    break;
                }
            }

            // IMPORTANTE: No clasificar materias del año actual en las listas
            // porque A26/B26, F26/G26, A29/B29 son SOLO para materias previas
            // Solo log para referencia, pero no llenar las listas
            if (esTroncal) {
                System.out.println("  Materia del año actual - " + nombreMateria + ": Troncal");
            } else {
                System.out.println("  Materia del año actual - " + nombreMateria + ": General");
            }

            // Las listas se mantienen vacías intencionalmente porque
            // los resúmenes son SOLO para materias previas de años anteriores
        } catch (Exception e) {
            System.err.println("Error clasificando materia: " + e.getMessage());
        }
    }

    /*
    // MÉTODOS FUTUROS PARA MATERIAS PREVIAS DE AÑOS ANTERIORES
    // Implementar cuando agregues la funcionalidad de materias previas
    
    private static int obtenerMateriasTroncalesPrevias(int alumnoId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return 0;
            
            // Ejemplo de consulta futura para materias troncales previas:
            String query = """
                SELECT COUNT(*) as total 
                FROM materias_previas mp 
                INNER JOIN materias m ON mp.materia_id = m.id 
                WHERE mp.alumno_id = ? AND mp.estado = 'ADEUDA' 
                AND m.tipo_materia = 'TRONCAL'
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("total");
            }
            
            rs.close();
            ps.close();
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo materias troncales previas: " + e.getMessage());
            return 0;
        }
    }
    
    private static List<String> obtenerListaMateriasTroncalesPrevias(int alumnoId) {
        List<String> materias = new ArrayList<>();
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return materias;
            
            String query = """
                SELECT m.nombre 
                FROM materias_previas mp 
                INNER JOIN materias m ON mp.materia_id = m.id 
                WHERE mp.alumno_id = ? AND mp.estado = 'ADEUDA' 
                AND m.tipo_materia = 'TRONCAL'
                ORDER BY m.nombre
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                materias.add(rs.getString("nombre"));
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo lista materias troncales previas: " + e.getMessage());
        }
        return materias;
    }
    
    private static int obtenerMateriasGeneralesPrevias(int alumnoId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return 0;
            
            String query = """
                SELECT COUNT(*) as total 
                FROM materias_previas mp 
                INNER JOIN materias m ON mp.materia_id = m.id 
                WHERE mp.alumno_id = ? AND mp.estado = 'ADEUDA' 
                AND m.tipo_materia = 'GENERAL'
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("total");
            }
            
            rs.close();
            ps.close();
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo materias generales previas: " + e.getMessage());
            return 0;
        }
    }
    
    private static List<String> obtenerListaMateriasGeneralesPrevias(int alumnoId) {
        List<String> materias = new ArrayList<>();
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return materias;
            
            String query = """
                SELECT m.nombre 
                FROM materias_previas mp 
                INNER JOIN materias m ON mp.materia_id = m.id 
                WHERE mp.alumno_id = ? AND mp.estado = 'ADEUDA' 
                AND m.tipo_materia = 'GENERAL'
                ORDER BY m.nombre
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                materias.add(rs.getString("nombre"));
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo lista materias generales previas: " + e.getMessage());
        }
        return materias;
    }
    
    private static int obtenerMateriasEnProceso2020(int alumnoId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return 0;
            
            String query = """
                SELECT COUNT(*) as total 
                FROM materias_previas mp 
                INNER JOIN materias m ON mp.materia_id = m.id 
                WHERE mp.alumno_id = ? AND mp.estado = 'ADEUDA' 
                AND mp.anio_adeudado = 2020
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("total");
            }
            
            rs.close();
            ps.close();
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo materias en proceso 2020: " + e.getMessage());
            return 0;
        }
    }
    
    private static List<String> obtenerListaMateriasEnProceso2020(int alumnoId) {
        List<String> materias = new ArrayList<>();
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return materias;
            
            String query = """
                SELECT m.nombre 
                FROM materias_previas mp 
                INNER JOIN materias m ON mp.materia_id = m.id 
                WHERE mp.alumno_id = ? AND mp.estado = 'ADEUDA' 
                AND mp.anio_adeudado = 2020
                ORDER BY m.nombre
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                materias.add(rs.getString("nombre"));
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo lista materias en proceso 2020: " + e.getMessage());
        }
        return materias;
    }
     */
    /**
     * Llena los resúmenes de materias según tu descripción
     */
    private static void llenarResumenMaterias(Sheet hoja, List<String> materiasGenerales,
            List<String> materiasTroncales, List<String> materiasEnProceso) {
        try {
            System.out.println("=== LLENANDO RESÚMENES DE MATERIAS ===");

            // A26: Número de materias troncales
            setCellValueSafe(hoja, 25, 0, String.valueOf(materiasTroncales.size())); // Fila 26 (índice 25), Columna A
            System.out.println("✅ A26 - Materias troncales: " + materiasTroncales.size());

            // B26: Materias troncales separadas por coma
            if (!materiasTroncales.isEmpty()) {
                String materiasTroncalesTexto = String.join(", ", materiasTroncales);
                setCellValueSafe(hoja, 25, 1, materiasTroncalesTexto); // Fila 26, Columna B
                System.out.println("✅ B26 - Lista troncales: " + materiasTroncalesTexto);
            }

            // F26: Número de materias generales  
            setCellValueSafe(hoja, 25, 5, String.valueOf(materiasGenerales.size())); // Fila 26, Columna F
            System.out.println("✅ F26 - Materias generales: " + materiasGenerales.size());

            // G26: Materias generales separadas por coma
            if (!materiasGenerales.isEmpty()) {
                String materiasGeneralesTexto = String.join(", ", materiasGenerales);
                setCellValueSafe(hoja, 25, 6, materiasGeneralesTexto); // Fila 26, Columna G
                System.out.println("✅ G26 - Lista generales: " + materiasGeneralesTexto);
            }

            // A29: Número de materias en proceso 2020 (ajustar año según corresponda)
            setCellValueSafe(hoja, 28, 0, String.valueOf(materiasEnProceso.size())); // Fila 29 (índice 28), Columna A
            System.out.println("✅ A29 - Materias en proceso: " + materiasEnProceso.size());

            // B29: Materias en proceso separadas por coma
            if (!materiasEnProceso.isEmpty()) {
                String materiasEnProcesoTexto = String.join(", ", materiasEnProceso);
                setCellValueSafe(hoja, 28, 1, materiasEnProcesoTexto); // Fila 29, Columna B
                System.out.println("✅ B29 - Lista en proceso: " + materiasEnProcesoTexto);
            }

            System.out.println("✅ Resúmenes de materias completados");

        } catch (Exception e) {
            System.err.println("❌ Error llenando resúmenes de materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Llena las inasistencias reales desde la base de datos (D22 en adelante)
     */
    private static void llenarInasistencias(Sheet hoja, int alumnoId, int cursoId) {
        try {
            System.out.println("=== LLENANDO INASISTENCIAS REALES ===");

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ No hay conexión para obtener inasistencias");
                return;
            }

            int filaInasistencias = 21; // Fila 22 (índice 21)
            int colInicioInasistencias = 3; // Columna D (índice 3)

            // Obtener inasistencias por período/materia
            String queryInasistencias = """
                SELECT 
                    COUNT(CASE WHEN estado = 'F' THEN 1 END) as faltas_totales,
                    COUNT(CASE WHEN estado = 'T' THEN 1 END) as tardanzas_totales,
                    COUNT(CASE WHEN estado = 'FJ' THEN 1 END) as faltas_justificadas
                FROM asistencia_general 
                WHERE alumno_id = ? AND curso_id = ?
                """;

            PreparedStatement ps = conect.prepareStatement(queryInasistencias);
            ps.setInt(1, alumnoId);
            ps.setInt(2, cursoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int faltasTotales = rs.getInt("faltas_totales");
                int tardanzasTotales = rs.getInt("tardanzas_totales");
                int faltasJustificadas = rs.getInt("faltas_justificadas");

                // Llenar según el diseño de tu boletín
                setCellValueSafe(hoja, filaInasistencias, colInicioInasistencias, String.valueOf(faltasTotales)); // D22 - Faltas
                setCellValueSafe(hoja, filaInasistencias, colInicioInasistencias + 1, String.valueOf(tardanzasTotales)); // E22 - Tardanzas
                setCellValueSafe(hoja, filaInasistencias, colInicioInasistencias + 2, String.valueOf(faltasJustificadas)); // F22 - Justificadas

                System.out.println("✅ Inasistencias: Faltas=" + faltasTotales + ", Tardanzas=" + tardanzasTotales + ", Justificadas=" + faltasJustificadas);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo inasistencias de BD: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error llenando inasistencias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método mejorado para establecer valor en celda de forma segura
     */
    private static void setCellValueSafe(Sheet hoja, int fila, int columna, String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                return; // No llenar celdas vacías
            }

            Row row = hoja.getRow(fila);
            if (row == null) {
                row = hoja.createRow(fila);
            }

            Cell cell = row.getCell(columna);
            if (cell == null) {
                cell = row.createCell(columna);
            }

            cell.setCellValue(valor);
            System.out.println("  ✓ Celda [" + (fila + 1) + "," + getColumnName(columna) + "] = '" + valor + "'");

        } catch (Exception e) {
            System.err.println("Error estableciendo valor en celda [" + fila + "," + columna + "]: " + e.getMessage());
        }
    }

    /**
     * Convierte índice de columna a nombre de columna (A, B, C, etc.)
     */
    private static String getColumnName(int columnIndex) {
        StringBuilder columnName = new StringBuilder();
        while (columnIndex >= 0) {
            columnName.insert(0, (char) ('A' + columnIndex % 26));
            columnIndex = columnIndex / 26 - 1;
        }
        return columnName.toString();
    }

    /**
     * Obtiene el ID del alumno desde los datos del estudiante
     */
    private static int obtenerAlumnoIdDesdeDatos(DatosEstudiante estudiante) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return -1;
            }

            String query = "SELECT id FROM usuarios WHERE dni = ? LIMIT 1";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, estudiante.dni);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                ps.close();
                return id;
            }

            rs.close();
            ps.close();
            return -1;

        } catch (SQLException e) {
            System.err.println("Error obteniendo alumno ID: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Obtiene el ID del curso desde los datos del estudiante
     */
    private static int obtenerCursoIdDesdeDatos(DatosEstudiante estudiante) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return -1;
            }

            String query = "SELECT id FROM cursos WHERE anio = ? AND division = ? LIMIT 1";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, Integer.parseInt(estudiante.curso));
            ps.setInt(2, Integer.parseInt(estudiante.division));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                ps.close();
                return id;
            }

            rs.close();
            ps.close();
            return -1;

        } catch (Exception e) {
            System.err.println("Error obteniendo curso ID: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Obtiene los datos básicos de un estudiante
     */
    private static DatosEstudiante obtenerDatosEstudiante(int alumnoId, int cursoId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return null;
            }

            // CONSULTA CORREGIDA: usar 'codigo_miescuela' y 'mail'
            String query = """
            SELECT u.nombre, u.apellido, u.dni, c.anio, c.division, u.codigo_miescuela, u.mail
            FROM usuarios u 
            INNER JOIN cursos c ON c.id = ?
            WHERE u.id = ?
            """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setInt(2, alumnoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                DatosEstudiante datos = new DatosEstudiante();
                datos.apellidoNombre = rs.getString("apellido") + ", " + rs.getString("nombre");
                datos.dni = rs.getString("dni");

                // CORREGIDO: Usar formato numérico para cursos
                int anio = rs.getInt("anio");
                int division = rs.getInt("division");

                datos.curso = String.valueOf(anio);      // "1", "2", "3", etc.
                datos.division = String.valueOf(division); // "1", "2", "3", etc.

                datos.codigoMiEscuela = rs.getString("codigo_miescuela");

                // NUEVO: También obtener email para referencia
                String email = rs.getString("mail"); // CORREGIDO: usar 'mail' no 'email'

                rs.close();
                ps.close();

                System.out.println("✅ Datos de estudiante obtenidos: " + datos.apellidoNombre);
                System.out.println("  Curso: " + datos.curso + datos.division);
                System.out.println("  DNI: " + (datos.dni != null ? datos.dni : "Sin DNI"));
                System.out.println("  Email: " + (email != null ? email : "Sin Email"));
                System.out.println("  Código Mi Escuela: " + (datos.codigoMiEscuela != null ? datos.codigoMiEscuela : "Sin código"));

                return datos;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo datos del estudiante: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * NUEVO MÉTODO: Obtener información completa del alumno incluyendo email
     * Para usar en sistemas que necesitan el email del alumno
     */
    public static DatosEstudianteCompleto obtenerDatosEstudianteCompleto(int alumnoId, int cursoId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return null;
            }

            String query = """
            SELECT u.id, u.nombre, u.apellido, u.dni, u.mail, u.telefono, u.direccion,
                   c.anio, c.division, u.codigo_miescuela
            FROM usuarios u 
            INNER JOIN cursos c ON c.id = ?
            WHERE u.id = ?
            """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setInt(2, alumnoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                DatosEstudianteCompleto datos = new DatosEstudianteCompleto();
                datos.id = rs.getInt("id");
                datos.apellidoNombre = rs.getString("apellido") + ", " + rs.getString("nombre");
                datos.dni = rs.getString("dni");
                datos.email = rs.getString("mail"); // CORREGIDO: usar 'mail'
                datos.telefono = rs.getString("telefono");
                datos.direccion = rs.getString("direccion");

                int anio = rs.getInt("anio");
                int division = rs.getInt("division");
                datos.curso = String.valueOf(anio);
                datos.division = String.valueOf(division);
                datos.codigoMiEscuela = rs.getString("codigo_miescuela");

                rs.close();
                ps.close();

                System.out.println("✅ Datos completos obtenidos para: " + datos.apellidoNombre);
                return datos;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo datos completos: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * CLASE EXTENDIDA: Datos completos del estudiante incluyendo email
     */
    public static class DatosEstudianteCompleto extends DatosEstudiante {

        public int id;
        public String email;      // NUEVO: campo email
        public String telefono;   // NUEVO: campo telefono  
        public String direccion;  // NUEVO: campo direccion

        public DatosEstudianteCompleto() {
            super();
            this.id = -1;
            this.email = "";
            this.telefono = "";
            this.direccion = "";
        }

        @Override
        public String toString() {
            return String.format("DatosEstudianteCompleto{id=%d, nombre='%s', dni='%s', email='%s', curso='%s%s'}",
                    id, apellidoNombre, dni, email, curso, division);
        }
    }

    /**
     * Obtiene las notas de un estudiante por materia - VERSIÓN MEJORADA
     */
    private static Map<String, NotasMateria> obtenerNotasEstudiante(int alumnoId, int cursoId, String periodo) {
        Map<String, NotasMateria> notasPorMateria = new HashMap<>();

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return notasPorMateria;
            }

            // Obtener DNI del alumno para búsquedas
            String queryDni = "SELECT dni FROM usuarios WHERE id = ?";
            PreparedStatement psDni = conect.prepareStatement(queryDni);
            psDni.setInt(1, alumnoId);
            ResultSet rsDni = psDni.executeQuery();

            String alumnoDni = null;
            if (rsDni.next()) {
                alumnoDni = rsDni.getString("dni");
            }
            rsDni.close();
            psDni.close();

            if (alumnoDni == null) {
                System.err.println("⚠️ No se encontró DNI para alumno ID: " + alumnoId);
                return notasPorMateria;
            }

            System.out.println("=== OBTENIENDO NOTAS COMPLETAS DEL ESTUDIANTE ===");
            System.out.println("Alumno ID: " + alumnoId + ", DNI: " + alumnoDni + ", Curso ID: " + cursoId);

            // Obtener TODAS las materias del curso (incluso si no tienen notas)
            String queryMaterias = """
                SELECT DISTINCT m.id, m.nombre 
                FROM materias m 
                INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id 
                WHERE pcm.curso_id = ? AND pcm.estado = 'activo' 
                ORDER BY m.nombre
                """;

            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            psMaterias.setInt(1, cursoId);
            ResultSet rsMaterias = psMaterias.executeQuery();

            while (rsMaterias.next()) {
                String nombreMateria = rsMaterias.getString("nombre");
                int materiaId = rsMaterias.getInt("id");

                System.out.println("Procesando materia: " + nombreMateria + " (ID: " + materiaId + ")");

                NotasMateria notas = new NotasMateria();

                // Obtener notas bimestrales COMPLETAS
                obtenerNotasBimestralesCompletas(alumnoDni, alumnoId, materiaId, notas);

                // Obtener notas de trabajos y calcular promedios
                obtenerNotasTrabajos(alumnoDni, alumnoId, materiaId, notas);

                // Calcular notas derivadas si no existen
                calcularNotasDerivadas(notas);

                notasPorMateria.put(nombreMateria, notas);

                System.out.println("  ✅ Materia procesada: " + nombreMateria);
                System.out.println("    Bimestres: " + notas.bimestre1 + ", " + notas.bimestre2 + ", " + notas.bimestre3 + ", " + notas.bimestre4);
                System.out.println("    Cuatrimestres: " + notas.cuatrimestre1 + ", " + notas.cuatrimestre2);
                System.out.println("    Exámenes: Dic=" + notas.diciembre + ", Feb=" + notas.febrero);
            }

            rsMaterias.close();
            psMaterias.close();

            System.out.println("✅ Notas obtenidas para " + notasPorMateria.size() + " materias");

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas del estudiante: " + e.getMessage());
            e.printStackTrace();
        }

        return notasPorMateria;
    }

    /**
     * Obtiene las notas bimestrales completas de una materia específica
     */
    private static void obtenerNotasBimestralesCompletas(String alumnoDni, int alumnoId, int materiaId, NotasMateria notas) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return;
            }

            // CONSULTA OPTIMIZADA: usar índices apropiados y limitar resultados
            String query = """
            SELECT periodo, nota, promedio_actividades, estado 
            FROM notas_bimestrales 
            WHERE materia_id = ? AND (alumno_id = ? OR alumno_id = CAST(? AS CHAR))
            ORDER BY 
                CASE periodo 
                    WHEN '1er Bimestre' THEN 1
                    WHEN '2do Bimestre' THEN 2  
                    WHEN '3er Bimestre' THEN 3
                    WHEN '4to Bimestre' THEN 4
                    WHEN '1er Cuatrimestre' THEN 5
                    WHEN '2do Cuatrimestre' THEN 6
                    WHEN 'Diciembre' THEN 7
                    WHEN 'Febrero' THEN 8
                    ELSE 9
                END
            """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, alumnoDni);
            ps.setInt(3, alumnoId);
            ResultSet rs = ps.executeQuery();

            System.out.println("  📊 Consultando notas para materia ID: " + materiaId);

            while (rs.next()) {
                String periodo = rs.getString("periodo");
                double nota = rs.getDouble("nota");
                String estado = rs.getString("estado");

                System.out.println("    " + periodo + ": " + nota + (estado != null && !"Normal".equals(estado) ? " (" + estado + ")" : ""));

                // Mapear período a campos de la clase
                switch (periodo) {
                    case "1er Bimestre":
                        notas.bimestre1 = nota;
                        break;
                    case "2do Bimestre":
                        notas.bimestre2 = nota;
                        break;
                    case "3er Bimestre":
                        notas.bimestre3 = nota;
                        break;
                    case "4to Bimestre":
                        notas.bimestre4 = nota;
                        break;
                    case "1er Cuatrimestre":
                        notas.cuatrimestre1 = nota;
                        break;
                    case "2do Cuatrimestre":
                        notas.cuatrimestre2 = nota;
                        break;
                    case "Diciembre":
                        notas.diciembre = nota;
                        break;
                    case "Febrero":
                        notas.febrero = nota;
                        break;
                }

                // Usar el campo convivencia para el estado
                if (estado != null && !estado.trim().isEmpty() && !"Normal".equals(estado)) {
                    notas.convivencia = estado;
                }
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas bimestrales: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene las notas de trabajos y calcula promedios
     */
    private static void obtenerNotasTrabajos(String alumnoDni, int alumnoId, int materiaId, NotasMateria notas) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return;
            }

            // Obtener todas las notas de trabajos para esta materia
            String query = """
            SELECT n.nota, t.nombre as trabajo_nombre
            FROM notas n 
            INNER JOIN trabajos t ON n.trabajo_id = t.id 
            WHERE t.materia_id = ? AND (n.alumno_id = ? OR n.alumno_id = CAST(? AS CHAR))
            """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, alumnoDni);
            ps.setInt(3, alumnoId);
            ResultSet rs = ps.executeQuery();

            List<Double> notasTrabajos = new ArrayList<>();
            while (rs.next()) {
                double nota = rs.getDouble("nota");
                String nombreTrabajo = rs.getString("trabajo_nombre");
                notasTrabajos.add(nota);
                System.out.println("    Trabajo: " + nombreTrabajo + " = " + nota);
            }

            rs.close();
            ps.close();

            // Calcular promedio de trabajos si hay notas
            if (!notasTrabajos.isEmpty()) {
                double sumaTrabajos = notasTrabajos.stream().mapToDouble(Double::doubleValue).sum();
                double promedioTrabajos = sumaTrabajos / notasTrabajos.size();

                // Usar el campo observaciones para almacenar info adicional
                notas.observaciones = "Promedio trabajos: " + String.format("%.2f", promedioTrabajos)
                        + " (" + notasTrabajos.size() + " trabajos)";

                System.out.println("    Promedio trabajos: " + promedioTrabajos + " (" + notasTrabajos.size() + " trabajos)");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas de trabajos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calcula notas derivadas que no estén ya cargadas
     */
    private static void calcularNotasDerivadas(NotasMateria notas) {
        try {
            // Calcular cuatrimestre 1 si no existe
            if (notas.cuatrimestre1 <= 0 && notas.bimestre1 > 0 && notas.bimestre2 > 0) {
                notas.cuatrimestre1 = Math.round(((notas.bimestre1 + notas.bimestre2) / 2) * 100.0) / 100.0;
                System.out.println("    Cuatrimestre 1 calculado: " + notas.cuatrimestre1);
            }

            // Calcular cuatrimestre 2 si no existe
            if (notas.cuatrimestre2 <= 0 && notas.bimestre3 > 0 && notas.bimestre4 > 0) {
                notas.cuatrimestre2 = Math.round(((notas.bimestre3 + notas.bimestre4) / 2) * 100.0) / 100.0;
                System.out.println("    Cuatrimestre 2 calculado: " + notas.cuatrimestre2);
            }

            // Validar consistencia de datos
            if (notas.cuatrimestre1 > 10 || notas.cuatrimestre2 > 10) {
                System.err.println("⚠️ Advertencia: Notas de cuatrimestre fuera del rango normal");
            }

        } catch (Exception e) {
            System.err.println("❌ Error calculando notas derivadas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la lista de alumnos de un curso
     */
    private static List<Integer> obtenerAlumnosDelCurso(int cursoId) {
        List<Integer> alumnosIds = new ArrayList<>();

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return alumnosIds;
            }

            String query = """
                SELECT u.id 
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id 
                WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = '4'
                ORDER BY u.apellido, u.nombre
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnosIds.add(rs.getInt("id"));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo alumnos del curso: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnosIds;
    }

    // ========================================================================
    // MÉTODOS PARA COMPATIBILIDAD CON INTERFAZ GRÁFICA
    // ========================================================================
    /**
     * Genera boletín individual con interfaz de progreso
     */
    public static void generarBoletinIndividualConServidorConInterfaz(int alumnoId, int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Iniciando generación de boletín...");
                return generarBoletinIndividualEnServidor(alumnoId, cursoId, periodo);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    System.out.println(message);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    String mensaje = exito
                            ? "Boletín generado y guardado exitosamente en el servidor"
                            : "Error al generar el boletín";

                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje,
                            "Generación de Boletín",
                            exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Error durante la generación: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Genera boletines para todo el curso con interfaz de progreso
     */
    public static void generarBoletinesCursoConServidorConInterfaz(int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {
        // Crear ventana de progreso
        javax.swing.JDialog progressDialog = new javax.swing.JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(parentComponent),
                "Generando Boletines", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Iniciando...");

        progressDialog.setLayout(new java.awt.BorderLayout());
        progressDialog.add(new javax.swing.JLabel("Generando boletines del curso...", javax.swing.SwingConstants.CENTER),
                java.awt.BorderLayout.NORTH);
        progressDialog.add(progressBar, java.awt.BorderLayout.CENTER);
        progressDialog.setSize(400, 100);
        progressDialog.setLocationRelativeTo(parentComponent);

        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                // Obtener total de alumnos para progreso
                List<Integer> alumnosIds = obtenerAlumnosDelCurso(cursoId);
                int totalAlumnos = alumnosIds.size();

                if (totalAlumnos == 0) {
                    publish("No se encontraron alumnos en el curso");
                    return 0;
                }

                publish("Encontrados " + totalAlumnos + " alumnos");

                int boletinesGenerados = 0;

                for (int i = 0; i < alumnosIds.size(); i++) {
                    int alumnoId = alumnosIds.get(i);

                    publish("Procesando alumno " + (i + 1) + "/" + totalAlumnos);
                    setProgress((i * 100) / totalAlumnos);

                    if (generarBoletinIndividualEnServidor(alumnoId, cursoId, periodo)) {
                        boletinesGenerados++;
                        publish("Boletín " + boletinesGenerados + " generado");
                    }
                }

                setProgress(100);
                return boletinesGenerados;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    progressBar.setString(message);
                    System.out.println(message);
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    int boletinesGenerados = get();

                    String mensaje = "Generación completada.\n"
                            + "Boletines generados: " + boletinesGenerados + "\n"
                            + "Guardados automáticamente en el servidor.";

                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje,
                            "Generación Masiva Completada",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Error durante la generación masiva: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Configurar progreso
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
        progressDialog.setVisible(true);
    }

    /**
     * Configura la plantilla con interfaz gráfica
     */
    public static void configurarPlantillaConInterfaz(javax.swing.JComponent parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Plantilla de Boletín");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));

        // Establecer directorio inicial
        String rutaActual = obtenerRutaPlantilla();
        if (rutaActual != null) {
            File archivoActual = new File(rutaActual);
            if (archivoActual.getParentFile() != null && archivoActual.getParentFile().exists()) {
                fileChooser.setCurrentDirectory(archivoActual.getParentFile());
            }
        }

        int userSelection = fileChooser.showOpenDialog(parentComponent);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();

            // Verificar que el archivo sea válido
            if (verificarPlantillaValida(archivoSeleccionado)) {
                configurarRutaPlantilla(archivoSeleccionado.getAbsolutePath());

                JOptionPane.showMessageDialog(parentComponent,
                        "Plantilla configurada exitosamente:\n" + archivoSeleccionado.getAbsolutePath(),
                        "Configuración Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentComponent,
                        "El archivo seleccionado no es una plantilla válida.\n"
                        + "Debe ser un archivo Excel (.xlsx) con el formato correcto.",
                        "Plantilla Inválida",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Verifica si una plantilla es válida
     */
    private static boolean verificarPlantillaValida(File archivo) {
        try {
            if (!archivo.exists() || !archivo.getName().toLowerCase().endsWith(".xlsx")) {
                return false;
            }

            // Intentar abrir el archivo para verificar que es un Excel válido
            try (FileInputStream fis = new FileInputStream(archivo); Workbook workbook = new XSSFWorkbook(fis)) {

                // Verificar que tiene al menos una hoja
                if (workbook.getNumberOfSheets() == 0) {
                    return false;
                }

                Sheet hoja = workbook.getSheetAt(0);

                // Verificaciones básicas de la estructura
                // (Aquí podrías agregar verificaciones más específicas según tu plantilla)
                return true;
            }

        } catch (Exception e) {
            System.err.println("Error verificando plantilla: " + e.getMessage());
            return false;
        }
    }

    // ========================================================================
    // CLASES DE DATOS
    // ========================================================================
    /**
     * Clase para almacenar datos del estudiante
     */
    public static class DatosEstudiante {

        public String apellidoNombre;
        public String dni;
        public String curso;
        public String division;
        public String codigoMiEscuela;

        public DatosEstudiante() {
            this.apellidoNombre = "";
            this.dni = "";
            this.curso = "";
            this.division = "";
            this.codigoMiEscuela = "";
        }
    }

    /**
     * Clase para almacenar notas por materia
     */
    public static class NotasMateria {

        public double bimestre1;
        public double bimestre2;
        public double bimestre3;
        public double bimestre4;
        public double cuatrimestre1;
        public double cuatrimestre2;
        public double diciembre;
        public double febrero;
        public String observaciones;
        public String convivencia;

        public NotasMateria() {
            this.bimestre1 = -1;
            this.bimestre2 = -1;
            this.bimestre3 = -1;
            this.bimestre4 = -1;
            this.cuatrimestre1 = -1;
            this.cuatrimestre2 = -1;
            this.diciembre = -1;
            this.febrero = -1;
            this.observaciones = "";
            this.convivencia = "";
        }
    }

    // ========================================================================
    // MÉTODOS DE UTILIDAD Y DIAGNÓSTICO
    // ========================================================================
    /**
     * Verifica la configuración actual del sistema
     */
    public static void verificarConfiguracion() {
        System.out.println("=== VERIFICACIÓN DE CONFIGURACIÓN ===");

        // Verificar ruta del servidor
        String rutaServidor = GestorBoletines.obtenerRutaServidor();
        System.out.println("Servidor configurado: " + rutaServidor);

        // Verificar plantilla
        String rutaPlantilla = obtenerRutaPlantilla();
        File plantilla = new File(rutaPlantilla);
        System.out.println("Plantilla configurada: " + rutaPlantilla);
        System.out.println("Plantilla existe: " + (plantilla.exists() ? "✅ Sí" : "❌ No"));

        if (plantilla.exists()) {
            System.out.println("Tamaño plantilla: " + (plantilla.length() / 1024) + " KB");
        }

        // Verificar conexión a BD
        Connection conect = Conexion.getInstancia().verificarConexion();
        System.out.println("Conexión BD: " + (conect != null ? "✅ Activa" : "❌ Error"));

        System.out.println("=== FIN VERIFICACIÓN ===");
    }

    /**
     * Método de prueba para generar un boletín de ejemplo
     *
     * @return
     */
    public static boolean generarBoletinPrueba() {
        try {
            System.out.println("=== GENERANDO BOLETÍN DE PRUEBA ===");

            // Usar datos ficticios para la prueba
            DatosEstudiante estudiante = new DatosEstudiante();
            estudiante.apellidoNombre = "ALUMNO, Prueba";
            estudiante.dni = "12345678";
            estudiante.curso = "1";
            estudiante.division = "A";
            estudiante.codigoMiEscuela = "TEST001";

            Map<String, NotasMateria> notas = new HashMap<>();

            // Agregar algunas materias de ejemplo
            NotasMateria matematicas = new NotasMateria();
            matematicas.bimestre1 = 8.5;
            matematicas.bimestre2 = 7.0;
            matematicas.bimestre3 = 9.0;
            matematicas.bimestre4 = 8.0;
            matematicas.observaciones = "Buen desempeño";
            notas.put("Matemáticas", matematicas);

            NotasMateria lengua = new NotasMateria();
            lengua.bimestre1 = 9.0;
            lengua.bimestre2 = 8.5;
            lengua.bimestre3 = 8.0;
            lengua.bimestre4 = 9.5;
            notas.put("Lengua y Literatura", lengua);

            // Generar archivo de prueba
            String rutaPrueba = System.getProperty("java.io.tmpdir") + File.separator + "boletin_prueba.xlsx";

            boolean exito = generarBoletinConPlantilla(estudiante, notas, "PRUEBA", rutaPrueba);

            if (exito) {
                System.out.println("✅ Boletín de prueba generado: " + rutaPrueba);
            } else {
                System.err.println("❌ Error generando boletín de prueba");
            }

            return exito;

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinPrueba: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Limpia archivos temporales de boletines
     */
    public static void limpiarArchivosTemporales() {
        try {
            String directorioTemp = System.getProperty("java.io.tmpdir");
            File carpetaTemp = new File(directorioTemp);

            File[] archivos = carpetaTemp.listFiles((dir, name)
                    -> name.startsWith("boletin_") && name.endsWith(".xlsx"));

            int archivosEliminados = 0;
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.delete()) {
                        archivosEliminados++;
                    }
                }
            }

            System.out.println("✅ Archivos temporales eliminados: " + archivosEliminados);

        } catch (Exception e) {
            System.err.println("Error limpiando archivos temporales: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas de uso del sistema
     *
     * @return
     */
    public static String obtenerEstadisticasUso() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("=== ESTADÍSTICAS DEL SISTEMA DE BOLETINES ===\n\n");

            // Información de configuración
            stats.append("📁 Servidor: ").append(GestorBoletines.obtenerRutaServidor()).append("\n");
            stats.append("📄 Plantilla: ").append(obtenerRutaPlantilla()).append("\n");

            File plantilla = new File(obtenerRutaPlantilla());
            stats.append("📊 Estado plantilla: ").append(plantilla.exists() ? "✅ Disponible" : "❌ No encontrada").append("\n");

            if (plantilla.exists()) {
                stats.append("📏 Tamaño plantilla: ").append(plantilla.length() / 1024).append(" KB\n");
            }

            // Estadísticas de base de datos
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect != null) {
                try {
                    String query = "SELECT COUNT(*) as total FROM boletin WHERE DATE(creado_at) = CURDATE()";
                    PreparedStatement ps = conect.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        stats.append("📈 Boletines generados hoy: ").append(rs.getInt("total")).append("\n");
                    }
                    rs.close();
                    ps.close();

                } catch (SQLException e) {
                    stats.append("⚠️ Error obteniendo estadísticas de BD\n");
                }
            }

            stats.append("\n🕒 Fecha de consulta: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            return stats.toString();

        } catch (Exception e) {
            return "Error obteniendo estadísticas: " + e.getMessage();
        }
    }

    // ========================================================================
// NUEVOS MÉTODOS PARA INTEGRACIÓN CON PDF - AGREGAR AL FINAL DE PlantillaBoletinUtility.java
// ========================================================================
    /**
     * NUEVO: Genera boletín completo (Excel + PDF) individual con servidor
     * automático
     *
     * * @param cursoId
     * @param periodo
     * @param alumnoId
     * @return
     */
    public static boolean generarBoletinCompletoIndividualEnServidor(int alumnoId, int cursoId, String periodo) {
        try {
            System.out.println("=== GENERANDO BOLETÍN COMPLETO INDIVIDUAL ===");

            // Usar el nuevo sistema integrado
            return PdfBoletinUtility.generarBoletinCompletoenServidor(alumnoId, cursoId, periodo);

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinCompletoIndividualEnServidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NUEVO: Genera boletines completos (Excel + PDF) para todo el curso
     */
    public static int generarBoletinesCursoCompletoEnServidor(int cursoId, String periodo) {
        try {
            System.out.println("=== GENERANDO BOLETINES COMPLETOS DEL CURSO ===");

            // Usar el nuevo sistema integrado
            return PdfBoletinUtility.generarBoletinesCursoCompletoEnServidor(cursoId, periodo);

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinesCursoCompletoEnServidor: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * NUEVO: Genera boletín completo individual con interfaz de progreso
     */
    public static void generarBoletinCompletoIndividualConServidorConInterfaz(int alumnoId, int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {

        PdfBoletinUtility.generarBoletinCompletoIndividualConInterfaz(alumnoId, cursoId, periodo, parentComponent);
    }

    /**
     * NUEVO: Genera boletines completos para todo el curso con interfaz de
     * progreso
     */
    public static void generarBoletinesCursoCompletoConServidorConInterfaz(int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {

        PdfBoletinUtility.generarBoletinesCursoCompletoConInterfaz(cursoId, periodo, parentComponent);
    }

    /**
     * NUEVO: Convierte boletines Excel existentes a PDF
     */
    public static void convertirBoletinesExistentesAPdf(int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {

        PdfBoletinUtility.convertirBoletinesExistentesACurso(cursoId, periodo, parentComponent);
    }

    /**
     * NUEVO: Verifica que el sistema PDF esté configurado correctamente
     */
    public static boolean verificarSistemaPdf() {
        try {
            System.out.println("=== VERIFICANDO SISTEMA PDF ===");

            // Verificar dependencias
            boolean testPdf = PdfBoletinUtility.testConversionPdf();

            if (testPdf) {
                System.out.println("✅ Sistema PDF configurado correctamente");
            } else {
                System.err.println("❌ Sistema PDF no disponible");
            }

            return testPdf;

        } catch (Exception e) {
            System.err.println("❌ Error verificando sistema PDF: " + e.getMessage());
            return false;
        }
    }

    public static void verificarEstructuraBD() {
        try {
            System.out.println("=== VERIFICACIÓN DE ESTRUCTURA BD ===");

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ No hay conexión a BD");
                return;
            }

            // Verificar campos en tabla usuarios
            System.out.println("📋 Verificando tabla 'usuarios':");
            String queryUsuarios = "DESCRIBE usuarios";
            PreparedStatement ps = conect.prepareStatement(queryUsuarios);
            ResultSet rs = ps.executeQuery();

            boolean tieneMail = false;
            boolean tieneCodigoMiescuela = false;
            boolean tieneDni = false;

            while (rs.next()) {
                String campo = rs.getString("Field");
                if ("mail".equals(campo)) {
                    tieneMail = true;
                }
                if ("codigo_miescuela".equals(campo)) {
                    tieneCodigoMiescuela = true;
                }
                if ("dni".equals(campo)) {
                    tieneDni = true;
                }
            }

            System.out.println("  Campo 'mail': " + (tieneMail ? "✅" : "❌"));
            System.out.println("  Campo 'codigo_miescuela': " + (tieneCodigoMiescuela ? "✅" : "❌"));
            System.out.println("  Campo 'dni': " + (tieneDni ? "✅" : "❌"));

            rs.close();
            ps.close();

            // Verificar datos de ejemplo
            if (tieneMail) {
                System.out.println("\n📊 Estadísticas de emails en usuarios:");
                String queryStats = """
                    SELECT 
                        COUNT(*) as total_usuarios,
                        COUNT(mail) as usuarios_con_mail,
                        COUNT(CASE WHEN rol = '4' THEN 1 END) as total_alumnos,
                        COUNT(CASE WHEN rol = '4' AND mail IS NOT NULL THEN 1 END) as alumnos_con_mail
                    FROM usuarios
                    """;

                PreparedStatement psStats = conect.prepareStatement(queryStats);
                ResultSet rsStats = psStats.executeQuery();

                if (rsStats.next()) {
                    int totalUsuarios = rsStats.getInt("total_usuarios");
                    int usuariosConMail = rsStats.getInt("usuarios_con_mail");
                    int totalAlumnos = rsStats.getInt("total_alumnos");
                    int alumnosConMail = rsStats.getInt("alumnos_con_mail");

                    System.out.println("  Total usuarios: " + totalUsuarios);
                    System.out.println("  Usuarios con email: " + usuariosConMail);
                    System.out.println("  Total alumnos: " + totalAlumnos);
                    System.out.println("  Alumnos con email: " + alumnosConMail);

                    if (totalAlumnos > 0) {
                        double porcentaje = (alumnosConMail * 100.0) / totalAlumnos;
                        System.out.println("  Porcentaje alumnos con email: " + String.format("%.1f%%", porcentaje));
                    }
                }

                rsStats.close();
                psStats.close();
            }

            System.out.println("\n=== FIN VERIFICACIÓN ===");

        } catch (Exception e) {
            System.err.println("❌ Error verificando estructura BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testearObtenerDatosEstudiante() {
        try {
            System.out.println("=== TEST: OBTENER DATOS ESTUDIANTE ===");

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ No hay conexión a BD");
                return;
            }

            // Obtener un alumno de ejemplo
            String queryTest = """
                SELECT u.id, ac.curso_id, u.apellido, u.nombre 
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id 
                WHERE u.rol = '4' AND ac.estado = 'activo' 
                LIMIT 1
                """;

            PreparedStatement ps = conect.prepareStatement(queryTest);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int alumnoId = rs.getInt("id");
                int cursoId = rs.getInt("curso_id");
                String nombre = rs.getString("apellido") + ", " + rs.getString("nombre");

                System.out.println("🧪 Probando con alumno: " + nombre + " (ID: " + alumnoId + ")");

                // Probar método corregido
                DatosEstudiante datos = obtenerDatosEstudiante(alumnoId, cursoId);

                if (datos != null) {
                    System.out.println("✅ Datos obtenidos exitosamente:");
                    System.out.println("  Nombre: " + datos.apellidoNombre);
                    System.out.println("  DNI: " + (datos.dni != null ? datos.dni : "Sin DNI"));
                    System.out.println("  Curso: " + datos.curso + "°" + datos.division);
                    System.out.println("  Código Mi Escuela: " + (datos.codigoMiEscuela != null ? datos.codigoMiEscuela : "Sin código"));
                } else {
                    System.err.println("❌ No se pudieron obtener los datos");
                }

                // Probar método completo
                DatosEstudianteCompleto datosCompletos = obtenerDatosEstudianteCompleto(alumnoId, cursoId);

                if (datosCompletos != null) {
                    System.out.println("\n✅ Datos completos obtenidos:");
                    System.out.println("  Email: " + (datosCompletos.email != null && !datosCompletos.email.isEmpty() ? datosCompletos.email : "Sin email"));
                    System.out.println("  Teléfono: " + (datosCompletos.telefono != null ? datosCompletos.telefono : "Sin teléfono"));
                }

            } else {
                System.out.println("⚠️ No se encontraron alumnos para probar");
            }

            rs.close();
            ps.close();

            System.out.println("\n=== FIN TEST ===");

        } catch (Exception e) {
            System.err.println("❌ Error en test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void corregirEmailsExistentes() {
        try {
            System.out.println("=== CORRECCIÓN DE EMAILS EXISTENTES ===");

            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ No hay conexión a BD");
                return;
            }

            // Verificar si existe un campo 'email' además de 'mail'
            String queryVerificar = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'email'";
            PreparedStatement ps = conect.prepareStatement(queryVerificar);
            ResultSet rs = ps.executeQuery();

            boolean existeEmail = false;
            if (rs.next()) {
                existeEmail = rs.getInt(1) > 0;
            }

            rs.close();
            ps.close();

            if (existeEmail) {
                System.out.println("📧 Se encontró campo 'email' además de 'mail'");
                System.out.println("💡 Esto puede causar confusión. Considera usar solo uno de los dos campos.");

                // Mostrar algunos ejemplos de diferencias
                String queryDiferencias = """
                    SELECT id, apellido, nombre, email, mail 
                    FROM usuarios 
                    WHERE rol = '4' AND (email IS NOT NULL OR mail IS NOT NULL) 
                    LIMIT 5
                    """;

                PreparedStatement psDif = conect.prepareStatement(queryDiferencias);
                ResultSet rsDif = psDif.executeQuery();

                System.out.println("\n📋 Ejemplos de datos actuales:");
                while (rsDif.next()) {
                    System.out.println(String.format("  %s - email: %s | mail: %s",
                            rsDif.getString("apellido") + ", " + rsDif.getString("nombre"),
                            rsDif.getString("email"),
                            rsDif.getString("mail")));
                }

                rsDif.close();
                psDif.close();
            } else {
                System.out.println("✅ Solo existe el campo 'mail', configuración correcta");
            }

            System.out.println("\n=== FIN CORRECCIÓN ===");

        } catch (Exception e) {
            System.err.println("❌ Error en corrección de emails: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
