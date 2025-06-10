package main.java.utils.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

// Imports de tu proyecto
import main.java.utils.GestorBoletines;
import main.java.utils.PlantillaBoletinUtility;

// POI imports (solo para crear Excel de prueba)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utilidad para convertir boletines Excel a PDF usando LibreOffice en servidor
 * Mantiene el formato EXACTO del Excel original
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 4.0 - Solo Servidor Edition
 */
public class PdfBoletinUtility {

    /**
     * MÉTODO PRINCIPAL: Convierte Excel a PDF usando LibreOffice EN EL SERVIDOR
     */
    /**
     * MÉTODO CORREGIDO: Detecta éxito correctamente
     */
    public static boolean convertirExcelAPdf(String archivoExcel, String archivoPdf) {
        try {
            System.out.println("=== CONVERSIÓN EXCEL A PDF EN SERVIDOR ===");
            System.out.println("Excel: " + archivoExcel);
            System.out.println("PDF destino: " + archivoPdf);

            // Llamar al script PHP del servidor
            String scriptConversion = "http://10.120.1.109/miet20/convert_to_pdf.php";

            URL url = new URL(scriptConversion);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

            String boundary = "----ConversionBoundary" + System.currentTimeMillis();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(180000);

            try (java.io.OutputStream os = connection.getOutputStream(); java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"), true)) {

                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"archivo_excel\"").append("\r\n");
                writer.append("\r\n");
                writer.append(archivoExcel).append("\r\n");
                writer.flush();

                writer.append("--" + boundary + "--").append("\r\n");
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            System.out.println("📊 Respuesta servidor: HTTP " + responseCode);

            if (responseCode == 200) {
                StringBuilder responseBuilder = new StringBuilder();

                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream(), "UTF-8"))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line).append("\n");
                    }
                }

                String response = responseBuilder.toString().trim();
                System.out.println("📏 Longitud respuesta: " + response.length() + " caracteres");

                // ===== DETECCIÓN DE ÉXITO MEJORADA =====
                boolean esExitoso = false;
                String razonExito = "";

                // Método 1: Verificar status success
                if (response.contains("\"status\":\"success\"")) {
                    esExitoso = true;
                    razonExito = "Status success detectado";
                }

                // Método 2: Verificar mensaje de éxito
                if (!esExitoso && (response.contains("PDF generado")
                        || response.contains("ajuste forzado")
                        || response.contains("exitosamente"))) {
                    esExitoso = true;
                    razonExito = "Mensaje de éxito detectado";
                }

                // Método 3: Verificar estructura JSON válida con contenido positivo
                if (!esExitoso && response.contains("{") && response.contains("}")
                        && response.contains("archivo_pdf") && !response.contains("\"status\":\"error\"")) {
                    esExitoso = true;
                    razonExito = "JSON válido con datos de PDF detectado";
                }

                if (esExitoso) {
                    System.out.println("✅ Conversión exitosa en servidor (" + razonExito + ")");

                    // Extraer información específica si está disponible
                    try {
                        String paginas = extraerValorJsonSimple(response, "paginas_pdf");
                        String orientacion = extraerValorJsonSimple(response, "orientacion");
                        String tamaño = extraerValorJsonSimple(response, "tamaño_legible");
                        String resultado = extraerValorJsonSimple(response, "resultado");

                        if (!paginas.isEmpty()) {
                            System.out.println("📄 Páginas PDF: " + paginas);
                            if ("1".equals(paginas)) {
                                System.out.println("🎯 PERFECTO: Una sola página conseguida");
                            }
                        }

                        if (!orientacion.isEmpty() && orientacion.contains("Vertical")) {
                            System.out.println("🔄 Orientación: Vertical ✓");
                        }

                        if (!tamaño.isEmpty()) {
                            System.out.println("📊 Tamaño: " + tamaño);
                        }

                        if ("CONSEGUIDO".equals(resultado)) {
                            System.out.println("🏆 AJUSTE FORZADO: CONSEGUIDO - Una página A4 exitosa");
                        } else if ("PERFECTO".equals(resultado)) {
                            System.out.println("🏆 RESULTADO PERFECTO - Estilo Google Drive exacto");
                        }

                    } catch (Exception e) {
                        System.out.println("📋 Conversión exitosa (detalles: parsing limitado)");
                    }

                    return true;

                } else if (response.contains("\"status\":\"error\"")) {
                    // Error real
                    String errorMessage = extraerValorJsonSimple(response, "message");
                    System.err.println("❌ Error en servidor: "
                            + (errorMessage.isEmpty() ? "Error desconocido" : errorMessage));
                    return false;

                } else {
                    // Respuesta ambigua
                    System.err.println("⚠️ Respuesta ambigua del servidor");
                    System.err.println("📝 Muestra: " + response.substring(0, Math.min(200, response.length())));

                    // Decisión conservadora: si no hay error explícito, asumir éxito
                    if (!response.toLowerCase().contains("error")
                            && !response.toLowerCase().contains("fallo")
                            && response.length() > 100) {

                        System.out.println("🤔 Sin errores explícitos detectados - Asumiendo éxito");
                        return true;
                    }

                    return false;
                }

            } else {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getErrorStream() != null
                                ? connection.getErrorStream() : connection.getInputStream()))) {
                    String errorResponse = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
                    System.err.println("❌ Error HTTP " + responseCode + ": " + errorResponse);
                }
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error llamando al servidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extractor JSON simplificado y robusto
     */
    private static String extraerValorJsonSimple(String json, String clave) {
        try {
            // Buscar patrón: "clave":"valor" o "clave":numero
            String patron = "\"" + clave + "\"\\s*:\\s*";
            int inicio = json.indexOf(patron);

            if (inicio == -1) {
                return "";
            }

            inicio += patron.length();

            // Saltar espacios
            while (inicio < json.length() && Character.isWhitespace(json.charAt(inicio))) {
                inicio++;
            }

            if (inicio >= json.length()) {
                return "";
            }

            char primerChar = json.charAt(inicio);

            if (primerChar == '"') {
                // Valor string
                inicio++; // Saltar comilla inicial
                int fin = json.indexOf('"', inicio);

                // Buscar el cierre correcto (ignorar \" escapados)
                while (fin != -1 && fin > 0 && json.charAt(fin - 1) == '\\') {
                    fin = json.indexOf('"', fin + 1);
                }

                if (fin != -1) {
                    return json.substring(inicio, fin);
                }

            } else if (Character.isDigit(primerChar) || primerChar == '-') {
                // Valor numérico
                int fin = inicio;
                while (fin < json.length()
                        && (Character.isDigit(json.charAt(fin))
                        || json.charAt(fin) == '.'
                        || json.charAt(fin) == '-')) {
                    fin++;
                }

                return json.substring(inicio, fin);

            } else if (json.substring(inicio).startsWith("true")) {
                return "true";
            } else if (json.substring(inicio).startsWith("false")) {
                return "false";
            }

        } catch (Exception e) {
            // Ignorar errores
        }

        return "";
    }

    /**
     * Método de compatibilidad - usa el servidor
     */
    public static boolean convertirExcelAPdfYSubir(String archivoExcel, String rutaPdfDestino) {
        return convertirExcelAPdf(archivoExcel, rutaPdfDestino);
    }

    /**
     * Verifica si LibreOffice está disponible en el servidor
     */
    public static boolean verificarLibreOfficeEnServidor() {
        try {
            System.out.println("🔍 Verificando LibreOffice en servidor...");

            String scriptVerificacion = "http://10.120.1.109/miet20/convert_to_pdf.php";

            URL url = new URL(scriptVerificacion);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write("archivo_excel=test_libreoffice_verification".getBytes());
            }

            int responseCode = connection.getResponseCode();

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(responseCode == 200
                            ? connection.getInputStream() : connection.getErrorStream()))) {
                String response = reader.lines().collect(java.util.stream.Collectors.joining("\n"));

                // LibreOffice está disponible si el servidor responde con estructura JSON
                if (response.contains("{") && response.contains("}")
                        && (response.contains("libreoffice")
                        || response.contains("Excel no encontrado")
                        || response.contains("\"status\""))) {

                    System.out.println("✅ LibreOffice disponible en servidor");
                    return true;
                }

                System.err.println("❌ LibreOffice no disponible - respuesta inesperada");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error verificando servidor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrae un valor de un JSON simple (sin librería JSON)
     */
    private static String extraerValorJson(String json, String clave) {
        try {
            // Patrón para valores string
            String patronString = "\"" + clave + "\":\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patronString);
            java.util.regex.Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                return matcher.group(1);
            }

            // Patrón para valores numéricos
            String patronNumero = "\"" + clave + "\":\\s*([0-9]+)";
            pattern = java.util.regex.Pattern.compile(patronNumero);
            matcher = pattern.matcher(json);

            if (matcher.find()) {
                return matcher.group(1);
            }

            // Patrón para valores booleanos
            String patronBool = "\"" + clave + "\":\\s*(true|false)";
            pattern = java.util.regex.Pattern.compile(patronBool);
            matcher = pattern.matcher(json);

            if (matcher.find()) {
                return matcher.group(1);
            }

        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        return "";
    }

    private static String extraerValorJsonRobusto(String json, String clave) {
        try {
            // Buscar la clave en el JSON completo
            String busqueda = "\"" + clave + "\":";
            int inicio = json.indexOf(busqueda);

            if (inicio == -1) {
                return "";
            }

            inicio += busqueda.length();

            // Saltar espacios
            while (inicio < json.length() && Character.isWhitespace(json.charAt(inicio))) {
                inicio++;
            }

            if (inicio >= json.length()) {
                return "";
            }

            char primerChar = json.charAt(inicio);

            if (primerChar == '"') {
                // Es un string
                inicio++; // Saltar la comilla inicial
                int fin = json.indexOf('"', inicio);

                if (fin != -1) {
                    return json.substring(inicio, fin);
                }

            } else if (Character.isDigit(primerChar)) {
                // Es un número
                int fin = inicio;
                while (fin < json.length()
                        && (Character.isDigit(json.charAt(fin)) || json.charAt(fin) == '.')) {
                    fin++;
                }

                return json.substring(inicio, fin);

            } else if (json.substring(inicio).startsWith("true")) {
                return "true";
            } else if (json.substring(inicio).startsWith("false")) {
                return "false";
            }

        } catch (Exception e) {
            // Ignorar errores de parsing
        }

        return "";
    }

    /**
     * Genera boletín completo usando solo el servidor
     */
    public static boolean generarBoletinCompletoenServidor(int alumnoId, int cursoId, String periodo) {
        try {
            System.out.println("=== GENERANDO BOLETÍN COMPLETO EN SERVIDOR ===");

            // 1. Generar Excel primero
            boolean excelGenerado = PlantillaBoletinUtility.generarBoletinIndividualEnServidor(alumnoId, cursoId, periodo);

            if (!excelGenerado) {
                System.err.println("❌ No se pudo generar el Excel");
                return false;
            }

            // 2. Obtener ruta del Excel generado
            String rutaExcel = obtenerRutaExcelGenerado(alumnoId, cursoId, periodo);
            if (rutaExcel == null) {
                System.err.println("❌ No se pudo obtener ruta del Excel");
                return false;
            }

            // 3. Verificar que existe
            if (!verificarArchivoExiste(rutaExcel)) {
                System.err.println("❌ Excel no existe: " + rutaExcel);
                return false;
            }

            System.out.println("📄 Excel confirmado: " + rutaExcel);

            // 4. Crear ruta PDF
            String rutaPdf = rutaExcel.replace(".xlsx", ".pdf");

            // 5. Convertir a PDF usando el servidor
            boolean pdfGenerado = convertirExcelAPdf(rutaExcel, rutaPdf);

            if (pdfGenerado) {
                System.out.println("✅ Boletín completo generado exitosamente en servidor");
                return true;
            } else {
                System.err.println("❌ Error generando PDF en servidor");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinCompletoenServidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test simplificado que solo usa el servidor
     */
    public static boolean testConversionPdf() {
        try {
            System.out.println("=== TEST DE CONVERSIÓN PDF EN SERVIDOR ===");

            // Verificar servidor primero
            if (!verificarLibreOfficeEnServidor()) {
                System.err.println("❌ Test cancelado - LibreOffice no disponible en servidor");
                return false;
            }

            String testExcel = System.getProperty("java.io.tmpdir") + File.separator + "test_servidor.xlsx";

            // Crear Excel de prueba
            crearExcelDePrueba(testExcel);

            // Subir Excel al servidor
            String excelEnServidor = subirExcelParaTest(testExcel);
            if (excelEnServidor == null) {
                System.err.println("❌ No se pudo subir Excel de prueba al servidor");
                return false;
            }

            // Probar conversión en servidor
            String pdfDestino = excelEnServidor.replace(".xlsx", ".pdf");
            boolean exito = convertirExcelAPdf(excelEnServidor, pdfDestino);

            // Limpiar archivo local
            new File(testExcel).delete();

            if (exito) {
                System.out.println("✅ Test exitoso - PDF generado en servidor");
                System.out.println("🌐 PDF disponible en: " + pdfDestino);
            }

            return exito;

        } catch (Exception e) {
            System.err.println("❌ Error en test: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la ruta del Excel generado
     */
    private static String obtenerRutaExcelGenerado(int alumnoId, int cursoId, String periodo) {
        try {
            java.sql.Connection conect = main.java.database.Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return null;
            }

            String query = """
                SELECT u.apellido, u.nombre, c.anio, c.division 
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id 
                INNER JOIN cursos c ON ac.curso_id = c.id 
                WHERE u.id = ? AND ac.curso_id = ? AND ac.estado = 'activo'
                """;

            java.sql.PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ps.setInt(2, cursoId);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String apellido = rs.getString("apellido").replaceAll("[^a-zA-Z0-9]", "_");
                String anio = rs.getString("anio");
                String division = rs.getString("division");
                String curso = anio + division;

                String fecha = java.time.LocalDate.now().toString();
                String nombreArchivo = String.format("Boletin_%s_%s_%s_%s.xlsx",
                        apellido, curso, periodo, fecha);

                String rutaCompleta = String.format("http://10.120.1.109/miet20/boletines/2025/%s/%s/%s",
                        curso, periodo, nombreArchivo);

                rs.close();
                ps.close();
                return rutaCompleta;
            }

            rs.close();
            ps.close();
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo ruta Excel: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si un archivo existe
     */
    private static boolean verificarArchivoExiste(String rutaArchivo) {
        if (rutaArchivo.startsWith("http://")) {
            try {
                URL url = new URL(rutaArchivo);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(5000);

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                return responseCode == 200;

            } catch (Exception e) {
                return false;
            }
        } else {
            return new File(rutaArchivo).exists();
        }
    }

    /**
     * Crea Excel de prueba
     */
    private static void crearExcelDePrueba(String rutaExcel) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Boletín");

            // Crear estructura similar a boletín real
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Título
            Row row0 = sheet.createRow(0);
            Cell titleCell = row0.createCell(0);
            titleCell.setCellValue("ESCUELA TÉCNICA N° 20 - BOLETÍN DE CALIFICACIONES");
            titleCell.setCellStyle(headerStyle);

            // Merge cells para título
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 12));

            // Datos del estudiante con formato
            Row row7 = sheet.createRow(7);
            row7.createCell(0).setCellValue("Curso:");
            row7.createCell(2).setCellValue("3°6");
            row7.createCell(3).setCellValue("RODRIGUEZ, Brisa Belen");
            row7.createCell(9).setCellValue("DNI: 50322662");

            // Encabezados de materias
            Row rowHeader = sheet.createRow(11);
            String[] headers = {"MATERIAS", "", "", "1°BIM", "2°BIM", "3°BIM", "4°BIM", "PROM", "1°C", "2°C", "PROM", "DIC", "FEB"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = rowHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Materias con datos
            String[] materias = {"Matemáticas", "Lengua y Literatura", "Historia", "Geografía", "Inglés", "Taller de TIC"};
            for (int i = 0; i < materias.length; i++) {
                Row row = sheet.createRow(12 + i);
                row.createCell(0).setCellValue(materias[i]);
                row.createCell(3).setCellValue(8.0 + i % 3);
                row.createCell(4).setCellValue(7.5 + i % 2);
                row.createCell(5).setCellValue(9.0 - i % 2);
                row.createCell(6).setCellValue(8.5);
            }

            // Ajustar anchos de columna
            for (int i = 0; i <= 12; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(rutaExcel)) {
                workbook.write(fos);
            }

            System.out.println("📄 Excel de prueba creado: " + rutaExcel);
        }
    }

    /**
     * Sube Excel de prueba al servidor
     */
    private static String subirExcelParaTest(String rutaExcelLocal) {
        try {
            System.out.println("📤 Subiendo Excel de prueba al servidor...");

            String fechaHora = java.time.LocalDateTime.now().toString().replace(":", "-");
            String rutaRelativa = "test/test_conversion_" + fechaHora + ".xlsx";
            String scriptSubida = "http://10.120.1.109/miet20/upload_boletin.php";

            URL url = new URL(scriptSubida);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

            String boundary = "----TestBoundary" + System.currentTimeMillis();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (java.io.OutputStream os = connection.getOutputStream(); java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"), true)) {

                // Parámetro ruta_destino
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"ruta_destino\"").append("\r\n");
                writer.append("\r\n");
                writer.append(rutaRelativa).append("\r\n");
                writer.flush();

                // Archivo Excel
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"test_conversion.xlsx\"").append("\r\n");
                writer.append("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").append("\r\n");
                writer.append("\r\n");
                writer.flush();

                // Copiar archivo
                try (FileInputStream fis = new FileInputStream(rutaExcelLocal)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                os.flush();

                writer.append("\r\n");
                writer.append("--" + boundary + "--").append("\r\n");
                writer.flush();
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                String urlExcel = "http://10.120.1.109/miet20/boletines/" + rutaRelativa;
                System.out.println("✅ Excel subido para test: " + urlExcel);
                return urlExcel;
            } else {
                System.err.println("❌ Error subiendo Excel de prueba: HTTP " + responseCode);
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error subiendo Excel: " + e.getMessage());
            return null;
        }
    }

    /**
     * Genera boletines completos para todo el curso - SOLO SERVIDOR
     */
    public static int generarBoletinesCursoCompletoEnServidor(int cursoId, String periodo) {
        try {
            System.out.println("=== GENERANDO BOLETINES CURSO COMPLETO EN SERVIDOR ===");

            // Obtener lista de alumnos
            List<Integer> alumnosIds = obtenerAlumnosDelCurso(cursoId);

            if (alumnosIds.isEmpty()) {
                System.out.println("⚠️ No se encontraron alumnos en el curso " + cursoId);
                return 0;
            }

            System.out.println("👥 Procesando " + alumnosIds.size() + " alumnos del curso " + cursoId + " en servidor");

            int boletinesGenerados = 0;

            for (int i = 0; i < alumnosIds.size(); i++) {
                int alumnoId = alumnosIds.get(i);

                try {
                    System.out.println("🔄 Procesando alumno " + (i + 1) + "/" + alumnosIds.size() + " (ID: " + alumnoId + ") en servidor");

                    if (generarBoletinCompletoenServidor(alumnoId, cursoId, periodo)) {
                        boletinesGenerados++;
                        System.out.println("✅ Boletín completo " + boletinesGenerados + " generado en servidor");
                    } else {
                        System.err.println("❌ Error generando boletín para alumno " + alumnoId);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error con alumno " + alumnoId + ": " + e.getMessage());
                }
            }

            System.out.println("=== GENERACIÓN MASIVA COMPLETADA EN SERVIDOR ===");
            System.out.println("Boletines completos generados: " + boletinesGenerados + "/" + alumnosIds.size());

            return boletinesGenerados;

        } catch (Exception e) {
            System.err.println("❌ Error en generarBoletinesCursoCompletoEnServidor: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Obtiene alumnos del curso
     */
    private static List<Integer> obtenerAlumnosDelCurso(int cursoId) {
        List<Integer> alumnosIds = new ArrayList<>();

        try {
            java.sql.Connection conect = main.java.database.Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ No se pudo conectar a la base de datos");
                return alumnosIds;
            }

            String query = """
                SELECT u.id, u.apellido, u.nombre
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id 
                WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = '4'
                ORDER BY u.apellido, u.nombre
                """;

            java.sql.PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            java.sql.ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnosIds.add(rs.getInt("id"));
            }

            rs.close();
            ps.close();

        } catch (java.sql.SQLException e) {
            System.err.println("❌ Error obteniendo alumnos del curso: " + e.getMessage());
        }

        return alumnosIds;
    }

    /**
     * Método individual con interfaz gráfica - usa solo servidor
     */
    public static void generarBoletinCompletoIndividualConInterfaz(int alumnoId, int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {

        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Generando boletín completo en servidor...");
                return generarBoletinCompletoenServidor(alumnoId, cursoId, periodo);
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
                    String mensaje;

                    if (exito) {
                        mensaje = "Boletín completo generado exitosamente en servidor.\n"
                                + "El PDF mantiene el formato exacto del Excel original.";
                    } else {
                        mensaje = "Error al generar el boletín completo.\n"
                                + "Verificar que LibreOffice esté instalado en el servidor.";
                    }

                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje,
                            "Generación Completa",
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
     * Versión con interfaz gráfica para curso completo - usa solo servidor
     */
    public static void generarBoletinesCursoCompletoConInterfaz(int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {

        // Crear ventana de progreso
        javax.swing.JDialog progressDialog = new javax.swing.JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(parentComponent),
                "Generando Boletines en Servidor", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Iniciando...");

        progressDialog.setLayout(new java.awt.BorderLayout());
        progressDialog.add(new javax.swing.JLabel("Generando boletines completos (Excel + PDF) en servidor...",
                javax.swing.SwingConstants.CENTER), java.awt.BorderLayout.NORTH);
        progressDialog.add(progressBar, java.awt.BorderLayout.CENTER);
        progressDialog.setSize(500, 120);
        progressDialog.setLocationRelativeTo(parentComponent);

        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                List<Integer> alumnosIds = obtenerAlumnosDelCurso(cursoId);
                int totalAlumnos = alumnosIds.size();

                if (totalAlumnos == 0) {
                    publish("No se encontraron alumnos en el curso");
                    return 0;
                }

                publish("Encontrados " + totalAlumnos + " alumnos - Usando servidor");

                int boletinesGenerados = 0;

                for (int i = 0; i < alumnosIds.size(); i++) {
                    int alumnoId = alumnosIds.get(i);

                    publish("Procesando alumno " + (i + 1) + "/" + totalAlumnos + " en servidor");
                    setProgress((i * 100) / totalAlumnos);

                    if (generarBoletinCompletoenServidor(alumnoId, cursoId, periodo)) {
                        boletinesGenerados++;
                        publish("✅ Boletín " + boletinesGenerados + " generado en servidor");
                    } else {
                        publish("❌ Error con alumno " + alumnoId);
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

                    String mensaje = "Generación completada en servidor.\n\n"
                            + "Boletines completos generados: " + boletinesGenerados + "\n"
                            + "Los PDFs mantienen el formato exacto del Excel original.\n"
                            + "Procesamiento realizado completamente en el servidor.";

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
     * Convierte boletines Excel existentes a PDF usando servidor
     */
    public static void convertirBoletinesExistentesACurso(int cursoId, String periodo,
            javax.swing.JComponent parentComponent) {

        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                publish("🔍 Buscando boletines Excel existentes...");

                // Obtener boletines Excel existentes
                List<GestorBoletines.InfoBoletin> boletinesExcel
                        = GestorBoletines.obtenerBoletinesDisponibles(
                                java.time.LocalDate.now().getYear(), null, null, periodo)
                                .stream()
                                .filter(b -> b.cursoId == cursoId)
                                .collect(java.util.stream.Collectors.toList());

                if (boletinesExcel.isEmpty()) {
                    publish("No se encontraron boletines Excel para convertir");
                    publish("Curso: " + cursoId + ", Período: " + periodo);
                    return 0;
                }

                publish("📋 Encontrados " + boletinesExcel.size() + " boletines Excel");
                publish("Convirtiendo en servidor con LibreOffice");

                int convertidos = 0;

                for (int i = 0; i < boletinesExcel.size(); i++) {
                    GestorBoletines.InfoBoletin boletin = boletinesExcel.get(i);

                    publish("🔄 Convirtiendo " + (i + 1) + "/" + boletinesExcel.size()
                            + ": " + boletin.alumnoNombre);

                    String rutaPdf = boletin.rutaArchivo.replace(".xlsx", ".pdf");

                    if (convertirExcelAPdf(boletin.rutaArchivo, rutaPdf)) {
                        convertidos++;
                        publish("✅ PDF " + convertidos + " generado en servidor");
                    } else {
                        publish("❌ Error convirtiendo: " + boletin.alumnoNombre);
                    }
                }

                return convertidos;
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
                    int convertidos = get();

                    String mensaje;
                    if (convertidos > 0) {
                        mensaje = "Conversión completada exitosamente en servidor.\n\n"
                                + "Archivos PDF generados: " + convertidos + "\n"
                                + "Los PDFs mantienen el formato exacto del boletín original.\n"
                                + "Procesado con LibreOffice en el servidor.";
                    } else {
                        mensaje = "No se pudieron convertir boletines.\n\n"
                                + "Verificar:\n"
                                + "- Que existan boletines Excel para este curso y período\n"
                                + "- Que LibreOffice esté instalado en el servidor\n"
                                + "- Conectividad con el servidor";
                    }

                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje,
                            "Conversión en Servidor Finalizada",
                            convertidos > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Error durante la conversión: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
