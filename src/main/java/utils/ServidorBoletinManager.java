package main.java.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import main.java.database.Conexion;

/**
 * Gestor para búsqueda y manejo de boletines PDF en el servidor web - VERSIÓN
 * CORREGIDA.
 *
 * CORRECCIONES PRINCIPALES: - Campo 'mail' en lugar de 'email' en consultas BD
 * - Manejo correcto de valores nulos para DNI y email - Constructor InfoAlumno
 * corregido - Optimización de consultas por curso específico
 *
 * ESTRUCTURA ESPERADA DEL SERVIDOR: /boletines/ /2025/ /11/ (1er año, 1ra
 * división) /1B/ -> Boletín_APELLIDO_Inicial_11_1B_2025-03-15.pdf /2B/ /3B/
 * /4B/ /12/ (1er año, 2da división) /1B/ ...
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.1 - Corregida
 */
public class ServidorBoletinManager {

    private static final String USER_AGENT = "JavaApp-BoletinManager/1.0";
    private static final int TIMEOUT_CONEXION = 10000; // 10 segundos
    private static final int TIMEOUT_LECTURA = 15000;  // 15 segundos

    /**
     * Información de un boletín PDF encontrado en el servidor
     */
    public static class BoletinPDF {

        public String nombreArchivo;
        public String urlCompleta;
        public String alumnoNombre;
        public String alumnoDni;
        public String alumnoEmail;
        public String curso;
        public String division;
        public String periodo;
        public long tamanioBytes;
        public boolean esAccesible;
        public String fechaModificacion;

        public BoletinPDF() {
            this.esAccesible = false;
            this.tamanioBytes = 0;
        }

        @Override
        public String toString() {
            return String.format("BoletinPDF{archivo='%s', alumno='%s', curso='%s%s', periodo='%s', tamaño=%d}",
                    nombreArchivo, alumnoNombre, curso, division, periodo, tamanioBytes);
        }
    }

    /**
     * Información de un alumno desde la base de datos - CORREGIDA
     */
    public static class InfoAlumno {

        public int id;
        public String nombreCompleto;
        public String dni;
        public String email;
        public String curso;
        public String division;
        public int cursoId; // NUEVO CAMPO

        public InfoAlumno(int id, String nombreCompleto, String dni, String email, String curso, String division) {
            this.id = id;
            this.nombreCompleto = nombreCompleto != null ? nombreCompleto : "";
            this.dni = dni != null && !dni.trim().isEmpty() ? dni : "Sin DNI";
            this.email = email != null && !email.trim().isEmpty() ? email : "Sin Email";
            this.curso = curso != null ? curso : "";
            this.division = division != null ? division : "";
            this.cursoId = -1; // Valor por defecto
        }

        @Override
        public String toString() {
            return String.format("InfoAlumno{id=%d, nombre='%s', dni='%s', email='%s', curso='%s%s'}",
                    id, nombreCompleto, dni, email, curso, division);
        }
    }

    /**
     * Busca todos los boletines PDF en el servidor según los filtros
     * especificados
     */
    public static List<BoletinPDF> buscarBoletinesPDF(String anioLectivo, String anio,
            String division, String periodo, String filtroNombre) {
        List<BoletinPDF> boletines = new ArrayList<>();

        try {
            System.out.println("=== BÚSQUEDA OPTIMIZADA DE BOLETINES PDF ===");
            System.out.println("Año lectivo: " + anioLectivo);
            System.out.println("Filtros - Año: " + anio + ", División: " + division + ", Período: " + periodo);

            // OPTIMIZACIÓN: Usar búsqueda específica por curso si es posible
            Map<String, InfoAlumno> mapaAlumnos;

            if (!"TODOS".equals(anio) && !"TODAS".equals(division)) {
                // Búsqueda optimizada para curso específico
                System.out.println("🎯 Usando búsqueda optimizada para curso específico");
                mapaAlumnos = obtenerAlumnosDeCursoEspecifico(Integer.parseInt(anio), Integer.parseInt(division));
            } else {
                // Búsqueda general (menos eficiente)
                System.out.println("🌐 Usando búsqueda general (menos eficiente)");
                mapaAlumnos = obtenerAlumnosDesdeDB();
            }

            if (mapaAlumnos.isEmpty()) {
                System.out.println("⚠️ No se encontraron alumnos para los filtros especificados");
                return boletines;
            }

            System.out.println("✅ Alumnos cargados: " + mapaAlumnos.size());

            // Generar rutas de búsqueda
            List<String> rutasBusqueda = construirRutasBusqueda(anioLectivo, anio, division, periodo);
            System.out.println("📁 Rutas de búsqueda generadas: " + rutasBusqueda.size());

            // Buscar en cada ruta
            for (String ruta : rutasBusqueda) {
                List<BoletinPDF> boletinesEnRuta = buscarPDFsEnRuta(ruta, mapaAlumnos);
                boletines.addAll(boletinesEnRuta);
                System.out.println("📋 Encontrados " + boletinesEnRuta.size() + " boletines en: " + ruta);
            }

            // Aplicar filtro por nombre si se especificó
            if (filtroNombre != null && !filtroNombre.trim().isEmpty()) {
                String filtroLower = filtroNombre.toLowerCase().trim();
                boletines = boletines.stream()
                        .filter(b -> b.alumnoNombre != null
                        && b.alumnoNombre.toLowerCase().contains(filtroLower))
                        .collect(java.util.stream.Collectors.toList());
            }

            System.out.println("✅ Total boletines encontrados: " + boletines.size());

            // Mostrar estadísticas de email y DNI
            long conEmail = boletines.stream()
                    .filter(b -> b.alumnoEmail != null && !b.alumnoEmail.isEmpty() && !"Sin Email".equals(b.alumnoEmail))
                    .count();
            long conDni = boletines.stream()
                    .filter(b -> b.alumnoDni != null && !b.alumnoDni.isEmpty() && !"Sin DNI".equals(b.alumnoDni))
                    .count();

            System.out.println("📧 Boletines con email: " + conEmail);
            System.out.println("🆔 Boletines con DNI: " + conDni);

        } catch (Exception e) {
            System.err.println("❌ Error en búsqueda de boletines: " + e.getMessage());
            e.printStackTrace();
        }

        return boletines;
    }

    /**
     * MÉTODO CORREGIDO: Obtiene información de todos los alumnos desde la base
     * de datos
     */
    private static Map<String, InfoAlumno> obtenerAlumnosDesdeDB() {
        Map<String, InfoAlumno> alumnos = new HashMap<>();

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("No hay conexión a la base de datos");
                return alumnos;
            }

            // CONSULTA CORREGIDA: usar 'mail' en lugar de 'email'
            String query = """
            SELECT u.id, u.nombre, u.apellido, u.dni, u.mail, c.anio, c.division, c.id as curso_id
            FROM usuarios u 
            LEFT JOIN alumno_curso ac ON u.id = ac.alumno_id AND ac.estado = 'activo'
            LEFT JOIN cursos c ON ac.curso_id = c.id
            WHERE u.rol = '4' AND u.status = 1
            ORDER BY u.apellido, u.nombre
            """;

            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            System.out.println("=== CARGANDO ALUMNOS DESDE BD (CORREGIDO) ===");
            int count = 0;

            while (rs.next()) {
                String nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                String dni = rs.getString("dni");
                String email = rs.getString("mail"); // CORREGIDO: era "email", ahora "mail"
                String curso = rs.getString("anio");
                String division = rs.getString("division");
                int cursoId = rs.getInt("curso_id");

                if (count < 5) { // Debug: mostrar primeros 5 alumnos
                    System.out.println("Alumno " + (count + 1) + ": " + nombreCompleto
                            + " | Email: " + (email != null ? email : "SIN EMAIL")
                            + " | DNI: " + (dni != null ? dni : "SIN DNI")
                            + " | Curso: " + (curso != null ? curso : "SIN CURSO")
                            + (division != null ? division : ""));
                }

                // Crear objeto InfoAlumno con información completa
                InfoAlumno info = new InfoAlumno(
                        rs.getInt("id"),
                        nombreCompleto,
                        dni,
                        email, // CORREGIDO: este campo ahora se llena
                        curso != null ? curso : "",
                        division != null ? division : ""
                );

                // Asignar cursoId después de crear el objeto
                info.cursoId = cursoId;

                // Crear múltiples claves de búsqueda para el mismo alumno
                String claveNormal = normalizarNombre(nombreCompleto);
                alumnos.put(claveNormal, info);

                // Claves alternativas para búsqueda más flexible
                String apellido = rs.getString("apellido");
                String nombre = rs.getString("nombre");

                if (apellido != null && nombre != null) {
                    // Solo apellido
                    alumnos.put(normalizarNombre(apellido), info);

                    // Apellido + inicial del nombre
                    String inicial = nombre.length() > 0 ? String.valueOf(nombre.charAt(0)).toUpperCase() : "";
                    alumnos.put(normalizarNombre(apellido + " " + inicial), info);

                    // Formato: APELLIDO_Inicial
                    alumnos.put(normalizarNombre(apellido + "_" + inicial), info);
                }

                count++;
            }

            rs.close();
            ps.close();

            System.out.println("✅ Total alumnos procesados: " + count);
            System.out.println("✅ Total claves de búsqueda generadas: " + alumnos.size());

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo alumnos de BD: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnos;
    }

    /**
     * MÉTODO CORREGIDO: Obtiene alumnos de un curso específico (optimizado)
     */
    public static Map<String, InfoAlumno> obtenerAlumnosDeCursoEspecifico(int anio, int division) {
        Map<String, InfoAlumno> alumnos = new HashMap<>();

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("No hay conexión a la base de datos");
                return alumnos;
            }

            System.out.println("🔍 Obteniendo alumnos del curso específico: " + anio + "°" + division);

            // CONSULTA OPTIMIZADA: filtrar directamente por curso
            String query = """
            SELECT u.id, u.nombre, u.apellido, u.dni, u.mail, c.anio, c.division, c.id as curso_id
            FROM usuarios u 
            INNER JOIN alumno_curso ac ON u.id = ac.alumno_id AND ac.estado = 'activo'
            INNER JOIN cursos c ON ac.curso_id = c.id
            WHERE u.rol = '4' AND u.status = 1 
            AND c.anio = ? AND c.division = ?
            ORDER BY u.apellido, u.nombre
            """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, anio);
            ps.setInt(2, division);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                String nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                String dni = rs.getString("dni");
                String email = rs.getString("mail"); // CORREGIDO: usar 'mail'
                String cursoStr = rs.getString("anio");
                String divisionStr = rs.getString("division");
                int cursoId = rs.getInt("curso_id");

                InfoAlumno info = new InfoAlumno(
                        rs.getInt("id"),
                        nombreCompleto,
                        dni,
                        email, // CORREGIDO: ahora se llena correctamente
                        cursoStr != null ? cursoStr : "",
                        divisionStr != null ? divisionStr : ""
                );

                // Asignar cursoId después de crear el objeto
                info.cursoId = cursoId;

                // Crear claves de búsqueda
                String claveNormal = normalizarNombre(nombreCompleto);
                alumnos.put(claveNormal, info);

                // Claves alternativas
                String apellido = rs.getString("apellido");
                String nombre = rs.getString("nombre");

                if (apellido != null && nombre != null) {
                    alumnos.put(normalizarNombre(apellido), info);
                    String inicial = nombre.length() > 0 ? String.valueOf(nombre.charAt(0)).toUpperCase() : "";
                    alumnos.put(normalizarNombre(apellido + " " + inicial), info);
                    alumnos.put(normalizarNombre(apellido + "_" + inicial), info);
                }

                count++;
            }

            rs.close();
            ps.close();

            System.out.println("✅ Alumnos del curso " + anio + "°" + division + " cargados: " + count);
            System.out.println("🔗 Claves de búsqueda generadas: " + alumnos.size());

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo alumnos del curso específico: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnos;
    }

    /**
     * Normaliza un nombre para usarlo como clave de búsqueda
     */
    private static String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }

        return nombre.toLowerCase()
                .replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i")
                .replaceAll("ó", "o").replaceAll("ú", "u").replaceAll("ñ", "n")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Construye las rutas de búsqueda según los filtros
     */
    private static List<String> construirRutasBusqueda(String anioLectivo, String anio,
            String division, String periodo) {
        List<String> rutas = new ArrayList<>();
        String servidorBase = GestorBoletines.obtenerRutaServidor();

        // Determinar años a buscar
        List<String> aniosBuscar = new ArrayList<>();
        if ("TODOS".equals(anio)) {
            for (int i = 1; i <= 7; i++) {
                aniosBuscar.add(String.valueOf(i));
            }
        } else {
            aniosBuscar.add(anio);
        }

        // Determinar divisiones a buscar
        List<String> divisionesBuscar = new ArrayList<>();
        if ("TODAS".equals(division)) {
            for (int i = 1; i <= 10; i++) {
                divisionesBuscar.add(String.valueOf(i));
            }
        } else {
            divisionesBuscar.add(division);
        }

        // Determinar períodos a buscar
        List<String> periodosBuscar = new ArrayList<>();
        if ("TODOS".equals(periodo)) {
            periodosBuscar.add("1B");
            periodosBuscar.add("2B");
            periodosBuscar.add("3B");
            periodosBuscar.add("4B");
            periodosBuscar.add("1C");
            periodosBuscar.add("2C");
            periodosBuscar.add("Final");
            periodosBuscar.add("Diciembre");
            periodosBuscar.add("Febrero");
        } else {
            periodosBuscar.add(periodo);
        }

        // Construir todas las combinaciones
        for (String a : aniosBuscar) {
            for (String d : divisionesBuscar) {
                for (String p : periodosBuscar) {
                    String curso = a + d; // Formato numérico: "11", "12", "21", etc.
                    String ruta = servidorBase + anioLectivo + "/" + curso + "/" + p + "/";
                    rutas.add(ruta);
                }
            }
        }

        return rutas;
    }

    /**
     * Busca archivos PDF en una ruta específica del servidor
     */
    private static List<BoletinPDF> buscarPDFsEnRuta(String rutaBase, Map<String, InfoAlumno> mapaAlumnos) {
        List<BoletinPDF> boletines = new ArrayList<>();

        try {
            // Método 1: Intentar listar directorio (si el servidor lo permite)
            List<String> archivosEncontrados = listarArchivosEnDirectorio(rutaBase);

            if (!archivosEncontrados.isEmpty()) {
                // El servidor permite listar directorio
                for (String archivo : archivosEncontrados) {
                    if (archivo.toLowerCase().endsWith(".pdf")) {
                        BoletinPDF boletin = procesarArchivoPDF(rutaBase + archivo, archivo, mapaAlumnos);
                        if (boletin != null) {
                            boletines.add(boletin);
                        }
                    }
                }
            } else {
                // Método 2: Búsqueda por nombres probables basados en alumnos
                for (InfoAlumno alumno : mapaAlumnos.values()) {
                    List<String> nombresProbables = generarNombresProbablesPDF(alumno, rutaBase);

                    for (String nombreArchivo : nombresProbables) {
                        String urlCompleta = rutaBase + nombreArchivo;

                        if (verificarArchivoExiste(urlCompleta)) {
                            BoletinPDF boletin = procesarArchivoPDF(urlCompleta, nombreArchivo, mapaAlumnos);
                            if (boletin != null) {
                                boletines.add(boletin);
                                break; // Solo uno por alumno
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error buscando PDFs en ruta " + rutaBase + ": " + e.getMessage());
        }

        return boletines;
    }

    /**
     * Intenta listar archivos en un directorio del servidor
     */
    private static List<String> listarArchivosEnDirectorio(String url) {
        List<String> archivos = new ArrayList<>();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_CONEXION);
            connection.setReadTimeout(TIMEOUT_LECTURA);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                String contentType = connection.getContentType();

                // Verificar si es HTML (listado de directorio)
                if (contentType != null && contentType.contains("text/html")) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String linea;
                        StringBuilder contenidoHTML = new StringBuilder();

                        while ((linea = reader.readLine()) != null) {
                            contenidoHTML.append(linea).append("\n");
                        }

                        // Extraer nombres de archivos del HTML
                        archivos = extraerArchivosDeHTML(contenidoHTML.toString());
                    }
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            // Es normal que falle si el servidor no permite listar directorios
            System.out.println("No se puede listar directorio: " + url);
        }

        return archivos;
    }

    /**
     * Extrae nombres de archivos de un listado HTML de directorio
     */
    private static List<String> extraerArchivosDeHTML(String html) {
        List<String> archivos = new ArrayList<>();

        try {
            // Patrones comunes para listados de directorio
            Pattern[] patrones = {
                Pattern.compile("href=\"([^\"]+\\.pdf)\"", Pattern.CASE_INSENSITIVE),
                Pattern.compile(">([^<]+\\.pdf)<", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\"([^\"]+\\.pdf)\"", Pattern.CASE_INSENSITIVE)
            };

            for (Pattern patron : patrones) {
                Matcher matcher = patron.matcher(html);
                while (matcher.find()) {
                    String archivo = matcher.group(1);

                    // Limpiar y validar el nombre del archivo
                    if (archivo != null && archivo.toLowerCase().endsWith(".pdf")
                            && !archivo.contains("/") && !archivo.contains("..")) {

                        // Decodificar URL si es necesario
                        try {
                            archivo = java.net.URLDecoder.decode(archivo, "UTF-8");
                        } catch (Exception e) {
                            // Usar tal como está si no se puede decodificar
                        }

                        if (!archivos.contains(archivo)) {
                            archivos.add(archivo);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error extrayendo archivos de HTML: " + e.getMessage());
        }

        return archivos;
    }

    /**
     * Genera nombres probables de archivos PDF para un alumno
     */
    private static List<String> generarNombresProbablesPDF(InfoAlumno alumno, String rutaBase) {
        List<String> nombres = new ArrayList<>();

        try {
            String periodo = extraerPeriodoDeLaRuta(rutaBase);
            String curso = extraerCursoDeLaRuta(rutaBase);
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Limpiar nombre para archivo
            String nombreLimpio = alumno.nombreCompleto
                    .replaceAll("[^a-zA-Z0-9\\s,]", "")
                    .replaceAll("\\s+", "_")
                    .replaceAll(",", "_");

            // Patrones de nombres más comunes
            nombres.add("Boletin_" + nombreLimpio + "_" + curso + "_" + periodo + "_" + fechaActual + ".pdf");
            nombres.add("Boletin_" + nombreLimpio + "_" + curso + "_" + periodo + ".pdf");
            nombres.add("Boletin_" + nombreLimpio + ".pdf");
            nombres.add(nombreLimpio + "_Boletin.pdf");
            nombres.add(nombreLimpio + ".pdf");

            // Intentar con solo apellido + inicial
            String[] partes = alumno.nombreCompleto.split(",");
            if (partes.length >= 2) {
                String apellido = partes[0].trim().replaceAll("[^a-zA-Z0-9]", "_");
                String nombre = partes[1].trim();
                String inicial = nombre.length() > 0 ? String.valueOf(nombre.charAt(0)) : "";

                nombres.add("Boletin_" + apellido + "_" + inicial + "_" + curso + "_" + periodo + ".pdf");
                nombres.add("Boletin_" + apellido + "_" + inicial + ".pdf");
                nombres.add(apellido + "_" + inicial + "_Boletin.pdf");
                nombres.add(apellido + "_" + inicial + ".pdf");
            }

            // Variaciones con fechas diferentes (últimos 30 días)
            for (int i = 0; i < 30; i++) {
                String fecha = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                nombres.add("Boletin_" + nombreLimpio + "_" + curso + "_" + periodo + "_" + fecha + ".pdf");
            }

        } catch (Exception e) {
            System.err.println("Error generando nombres probables: " + e.getMessage());
        }

        return nombres;
    }

    /**
     * MÉTODO CORREGIDO: Procesa un archivo PDF encontrado y crea el objeto
     * BoletinPDF
     */
    private static BoletinPDF procesarArchivoPDF(String urlCompleta, String nombreArchivo,
            Map<String, InfoAlumno> mapaAlumnos) {
        try {
            BoletinPDF boletin = new BoletinPDF();
            boletin.nombreArchivo = nombreArchivo;
            boletin.urlCompleta = urlCompleta;
            boletin.periodo = extraerPeriodoDeLaRuta(urlCompleta);

            // Extraer curso y división de la ruta
            String curso = extraerCursoDeLaRuta(urlCompleta);
            if (curso.length() >= 2) {
                boletin.curso = String.valueOf(curso.charAt(0));
                boletin.division = String.valueOf(curso.charAt(1));
            }

            // Intentar identificar al alumno por el nombre del archivo
            InfoAlumno alumno = identificarAlumnoPorNombre(nombreArchivo, mapaAlumnos);
            if (alumno != null) {
                boletin.alumnoNombre = alumno.nombreCompleto;
                boletin.alumnoDni = alumno.dni; // Ahora debería mostrar el DNI correcto
                boletin.alumnoEmail = alumno.email; // Ahora debería mostrar el email correcto

                // Debug: mostrar información del alumno identificado
                System.out.println("✅ Alumno identificado para " + nombreArchivo + ":");
                System.out.println("  Nombre: " + boletin.alumnoNombre);
                System.out.println("  DNI: " + boletin.alumnoDni);
                System.out.println("  Email: " + boletin.alumnoEmail);

                // Verificar consistencia de curso
                if (boletin.curso.isEmpty() && !alumno.curso.isEmpty()) {
                    boletin.curso = alumno.curso;
                }
                if (boletin.division.isEmpty() && !alumno.division.isEmpty()) {
                    boletin.division = alumno.division;
                }
            } else {
                // Si no se puede identificar, extraer del nombre del archivo
                boletin.alumnoNombre = extraerNombreDelArchivo(nombreArchivo);
                boletin.alumnoDni = "No encontrado";
                boletin.alumnoEmail = "No encontrado";
                System.out.println("⚠️ No se pudo identificar alumno para: " + nombreArchivo);
            }

            // Obtener información adicional del archivo
            boletin.tamanioBytes = obtenerTamanioArchivo(urlCompleta);
            boletin.esAccesible = boletin.tamanioBytes > 0;

            return boletin;

        } catch (Exception e) {
            System.err.println("❌ Error procesando PDF " + nombreArchivo + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Identifica a qué alumno pertenece un archivo basándose en su nombre
     */
    private static InfoAlumno identificarAlumnoPorNombre(String nombreArchivo, Map<String, InfoAlumno> mapaAlumnos) {
        try {
            // Limpiar el nombre del archivo para búsqueda
            String nombreLimpio = nombreArchivo
                    .replaceAll("\\.pdf$", "")
                    .replaceAll("Boletin_", "")
                    .replaceAll("_Boletin", "")
                    .toLowerCase();

            // Buscar coincidencia exacta
            InfoAlumno alumno = mapaAlumnos.get(normalizarNombre(nombreLimpio));
            if (alumno != null) {
                return alumno;
            }

            // Buscar coincidencias parciales
            for (Map.Entry<String, InfoAlumno> entry : mapaAlumnos.entrySet()) {
                String claveAlumno = entry.getKey();

                // Verificar si el nombre del archivo contiene el nombre del alumno
                if (nombreLimpio.contains(claveAlumno) || claveAlumno.contains(nombreLimpio)) {
                    return entry.getValue();
                }

                // Verificar por partes del nombre
                String[] partesArchivo = nombreLimpio.split("[_\\s-]+");
                String[] partesAlumno = claveAlumno.split("[_\\s-]+");

                int coincidencias = 0;
                for (String parteArchivo : partesArchivo) {
                    for (String parteAlumno : partesAlumno) {
                        if (parteArchivo.equals(parteAlumno) && parteArchivo.length() > 2) {
                            coincidencias++;
                        }
                    }
                }

                // Si hay suficientes coincidencias, considerarlo una match
                if (coincidencias >= 2) {
                    return entry.getValue();
                }
            }

        } catch (Exception e) {
            System.err.println("Error identificando alumno: " + e.getMessage());
        }

        return null;
    }

    /**
     * Extrae el período de la ruta
     */
    private static String extraerPeriodoDeLaRuta(String ruta) {
        try {
            String[] partes = ruta.split("/");
            for (int i = partes.length - 1; i >= 0; i--) {
                String parte = partes[i];
                if (parte.matches("\\d+[BC]|Final|Diciembre|Febrero")) {
                    return parte;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo período: " + e.getMessage());
        }
        return "Desconocido";
    }

    /**
     * Extrae el curso de la ruta
     */
    private static String extraerCursoDeLaRuta(String ruta) {
        try {
            String[] partes = ruta.split("/");
            for (String parte : partes) {
                if (parte.matches("\\d{2,3}")) { // Formato numérico: 11, 12, 21, etc.
                    return parte;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo curso: " + e.getMessage());
        }
        return "";
    }

    /**
     * Extrae el nombre del alumno del nombre del archivo
     */
    private static String extraerNombreDelArchivo(String nombreArchivo) {
        try {
            String nombre = nombreArchivo
                    .replaceAll("\\.pdf$", "")
                    .replaceAll("Boletin_", "")
                    .replaceAll("_Boletin", "")
                    .replaceAll("_\\d+[BC]", "")
                    .replaceAll("_\\d{4}-\\d{2}-\\d{2}", "")
                    .replaceAll("_", " ");

            return nombre.trim();

        } catch (Exception e) {
            return nombreArchivo;
        }
    }

    /**
     * Verifica si un archivo existe en el servidor
     */
    private static boolean verificarArchivoExiste(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_CONEXION);
            connection.setReadTimeout(TIMEOUT_LECTURA);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el tamaño de un archivo
     */
    private static long obtenerTamanioArchivo(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_CONEXION);
            connection.setReadTimeout(TIMEOUT_LECTURA);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                long tamanio = connection.getContentLengthLong();
                connection.disconnect();
                return tamanio > 0 ? tamanio : 0;
            }

            connection.disconnect();
            return 0;

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Descarga un archivo del servidor a una ubicación local
     */
    public static boolean descargarArchivo(String url, String rutaDestino) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_CONEXION);
            connection.setReadTimeout(30000); // 30 segundos para descarga

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream inputStream = connection.getInputStream(); FileOutputStream outputStream = new FileOutputStream(rutaDestino)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytes = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }

                    System.out.println("Archivo descargado: " + rutaDestino + " (" + totalBytes + " bytes)");
                    return true;
                }
            }

            connection.disconnect();
            return false;

        } catch (Exception e) {
            System.err.println("Error descargando archivo " + url + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea un archivo temporal con el contenido de un PDF del servidor
     */
    public static File crearArchivoTemporal(String url) {
        try {
            File tempFile = File.createTempFile("boletin_", ".pdf");
            tempFile.deleteOnExit();

            if (descargarArchivo(url, tempFile.getAbsolutePath())) {
                return tempFile;
            } else {
                tempFile.delete();
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error creando archivo temporal: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica la conectividad con el servidor de boletines
     */
    public static boolean verificarConectividadServidor() {
        try {
            String servidorBase = GestorBoletines.obtenerRutaServidor();

            HttpURLConnection connection = (HttpURLConnection) new URL(servidorBase).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_CONEXION);
            connection.setReadTimeout(TIMEOUT_LECTURA);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            boolean conectado = responseCode == 200 || responseCode == 403; // 403 es común si no permite listar

            System.out.println("Verificación de servidor: " + servidorBase
                    + " - Código: " + responseCode
                    + " - Estado: " + (conectado ? "CONECTADO" : "SIN ACCESO"));

            return conectado;

        } catch (Exception e) {
            System.err.println("Error verificando conectividad: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas de boletines PDF en el servidor
     */
    public static Map<String, Object> obtenerEstadisticasServidor(String anioLectivo) {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            System.out.println("Obteniendo estadísticas para año lectivo: " + anioLectivo);

            // Buscar todos los boletines del año
            List<BoletinPDF> todosLosBoletines = buscarBoletinesPDF(anioLectivo, "TODOS", "TODAS", "TODOS", null);

            estadisticas.put("totalBoletines", todosLosBoletines.size());

            // Agrupar por período
            Map<String, Integer> porPeriodo = new HashMap<>();
            // Agrupar por curso
            Map<String, Integer> porCurso = new HashMap<>();
            // Contar con email
            int conEmail = 0;
            // Espacio total
            long espacioTotal = 0;

            for (BoletinPDF boletin : todosLosBoletines) {
                // Por período
                porPeriodo.put(boletin.periodo, porPeriodo.getOrDefault(boletin.periodo, 0) + 1);

                // Por curso
                String curso = boletin.curso + boletin.division;
                porCurso.put(curso, porCurso.getOrDefault(curso, 0) + 1);

                // Con email (corregido para manejar "Sin Email")
                if (boletin.alumnoEmail != null && !boletin.alumnoEmail.trim().isEmpty()
                        && !"Sin Email".equals(boletin.alumnoEmail)) {
                    conEmail++;
                }

                // Espacio
                espacioTotal += boletin.tamanioBytes;
            }

            estadisticas.put("porPeriodo", porPeriodo);
            estadisticas.put("porCurso", porCurso);
            estadisticas.put("conEmail", conEmail);
            estadisticas.put("espacioTotal", espacioTotal);
            estadisticas.put("servidorBase", GestorBoletines.obtenerRutaServidor());
            estadisticas.put("fechaConsulta", LocalDate.now().toString());

        } catch (Exception e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());
            estadisticas.put("error", e.getMessage());
        }

        return estadisticas;
    }

    /**
     * Busca boletines de un alumno específico por ID
     */
    public static List<BoletinPDF> buscarBoletinesDeAlumno(int alumnoId, String anioLectivo) {
        List<BoletinPDF> boletines = new ArrayList<>();

        try {
            // Obtener información del alumno
            InfoAlumno alumno = obtenerAlumnoPorId(alumnoId);
            if (alumno == null) {
                System.err.println("No se encontró alumno con ID: " + alumnoId);
                return boletines;
            }

            // Buscar boletines que coincidan con este alumno
            List<BoletinPDF> todosLosBoletines = buscarBoletinesPDF(anioLectivo, "TODOS", "TODAS", "TODOS", null);

            for (BoletinPDF boletin : todosLosBoletines) {
                if (boletin.alumnoNombre != null
                        && normalizarNombre(boletin.alumnoNombre).equals(normalizarNombre(alumno.nombreCompleto))) {
                    boletines.add(boletin);
                }
            }

            System.out.println("Encontrados " + boletines.size() + " boletines para " + alumno.nombreCompleto);

        } catch (Exception e) {
            System.err.println("Error buscando boletines del alumno: " + e.getMessage());
        }

        return boletines;
    }

    /**
     * MÉTODO CORREGIDO: Obtiene información de un alumno por su ID
     */
    private static InfoAlumno obtenerAlumnoPorId(int alumnoId) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return null;
            }

            String query = """
                SELECT u.id, u.nombre, u.apellido, u.dni, u.mail, c.anio, c.division
                FROM usuarios u 
                LEFT JOIN alumno_curso ac ON u.id = ac.alumno_id AND ac.estado = 'activo'
                LEFT JOIN cursos c ON ac.curso_id = c.id
                WHERE u.id = ? AND u.rol = '4'
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nombreCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                String dni = rs.getString("dni");
                String email = rs.getString("mail"); // CORREGIDO: usar 'mail'
                String curso = rs.getString("anio");
                String division = rs.getString("division");

                InfoAlumno alumno = new InfoAlumno(
                        rs.getInt("id"),
                        nombreCompleto,
                        dni,
                        email, // CORREGIDO: ahora se llena correctamente
                        curso != null ? curso : "",
                        division != null ? division : ""
                );

                rs.close();
                ps.close();
                return alumno;
            }

            rs.close();
            ps.close();

        } catch (Exception e) {
            System.err.println("Error obteniendo alumno por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Verifica si un boletín específico existe en el servidor
     */
    public static boolean verificarBoletinExiste(String alumnoNombre, String curso, String division,
            String periodo, String anioLectivo) {
        try {
            String cursoCompleto = curso + division;
            String servidorBase = GestorBoletines.obtenerRutaServidor();
            String rutaBase = servidorBase + anioLectivo + "/" + cursoCompleto + "/" + periodo + "/";

            // Generar nombres posibles para este alumno
            List<String> nombresPosibles = new ArrayList<>();
            String nombreLimpio = alumnoNombre
                    .replaceAll("[^a-zA-Z0-9\\s,]", "")
                    .replaceAll("\\s+", "_")
                    .replaceAll(",", "_");

            nombresPosibles.add("Boletin_" + nombreLimpio + ".pdf");
            nombresPosibles.add(nombreLimpio + "_Boletin.pdf");
            nombresPosibles.add(nombreLimpio + ".pdf");

            // Probar cada nombre posible
            for (String nombre : nombresPosibles) {
                String urlCompleta = rutaBase + nombre;
                if (verificarArchivoExiste(urlCompleta)) {
                    System.out.println("Boletín encontrado: " + urlCompleta);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error verificando boletín: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca y descarga todos los boletines de un curso específico
     */
    public static List<File> descargarBoletinesDeCurso(String anioLectivo, String curso, String division,
            String periodo, String carpetaDestino) {
        List<File> archivosDescargados = new ArrayList<>();

        try {
            // Buscar boletines del curso específico
            List<BoletinPDF> boletines = buscarBoletinesPDF(anioLectivo, curso, division, periodo, null);

            File carpeta = new File(carpetaDestino);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            for (BoletinPDF boletin : boletines) {
                String rutaDestino = carpetaDestino + File.separator + boletin.nombreArchivo;

                if (descargarArchivo(boletin.urlCompleta, rutaDestino)) {
                    archivosDescargados.add(new File(rutaDestino));
                    System.out.println("Descargado: " + boletin.nombreArchivo);
                } else {
                    System.err.println("Error descargando: " + boletin.nombreArchivo);
                }
            }

        } catch (Exception e) {
            System.err.println("Error descargando boletines del curso: " + e.getMessage());
        }

        return archivosDescargados;
    }

    /**
     * Obtiene información detallada de un boletín específico
     */
    public static Map<String, Object> obtenerDetallesBoletin(String url) {
        Map<String, Object> detalles = new HashMap<>();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_CONEXION);
            connection.setReadTimeout(TIMEOUT_LECTURA);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                detalles.put("existe", true);
                detalles.put("tamanio", connection.getContentLengthLong());
                detalles.put("tipoContenido", connection.getContentType());
                detalles.put("fechaModificacion", new java.util.Date(connection.getLastModified()));
                detalles.put("servidor", connection.getHeaderField("Server"));
            } else {
                detalles.put("existe", false);
                detalles.put("codigoRespuesta", responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            detalles.put("existe", false);
            detalles.put("error", e.getMessage());
        }

        return detalles;
    }

    /**
     * Limpia archivos temporales creados por este manager
     */
    public static void limpiarArchivosTemporales() {
        try {
            File directorioTemp = new File(System.getProperty("java.io.tmpdir"));
            File[] archivosTemp = directorioTemp.listFiles((dir, name)
                    -> name.startsWith("boletin_") && name.endsWith(".pdf"));

            if (archivosTemp != null) {
                int eliminados = 0;
                for (File archivo : archivosTemp) {
                    if (archivo.delete()) {
                        eliminados++;
                    }
                }
                System.out.println("Archivos temporales eliminados: " + eliminados);
            }

        } catch (Exception e) {
            System.err.println("Error limpiando archivos temporales: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Buscar boletines de un curso específico (OPTIMIZADO)
     */
    public static List<BoletinPDF> buscarBoletinesDeCursoEspecifico(String anioLectivo, int anio, int division, String periodo) {
        List<BoletinPDF> boletines = new ArrayList<>();

        try {
            System.out.println("🎯 Búsqueda específica para curso " + anio + "°" + division);

            // Obtener solo alumnos del curso específico
            Map<String, InfoAlumno> alumnosDelCurso = obtenerAlumnosDeCursoEspecifico(anio, division);

            if (alumnosDelCurso.isEmpty()) {
                System.out.println("⚠️ No se encontraron alumnos en el curso " + anio + "°" + division);
                return boletines;
            }

            // Construir ruta específica
            String curso = String.valueOf(anio) + String.valueOf(division); // "11", "12", etc.
            String rutaBusqueda = GestorBoletines.obtenerRutaServidor() + anioLectivo + "/" + curso + "/" + periodo + "/";

            System.out.println("📁 Buscando en ruta específica: " + rutaBusqueda);

            // Buscar archivos
            List<BoletinPDF> boletinesEncontrados = buscarPDFsEnRuta(rutaBusqueda, alumnosDelCurso);
            boletines.addAll(boletinesEncontrados);

            System.out.println("✅ Encontrados " + boletines.size() + " boletines para el curso " + anio + "°" + division);

        } catch (Exception e) {
            System.err.println("❌ Error en búsqueda específica de curso: " + e.getMessage());
            e.printStackTrace();
        }

        return boletines;
    }

    /**
     * Método de testing para verificar la funcionalidad
     */
    public static void testearFuncionalidad() {
        System.out.println("=== TEST DE SERVIDOR BOLETIN MANAGER (CORREGIDO) ===");

        try {
            // Test 1: Verificar conectividad
            System.out.println("Test 1: Verificando conectividad...");
            boolean conectado = verificarConectividadServidor();
            System.out.println("Resultado: " + (conectado ? "✅ CONECTADO" : "❌ SIN CONEXIÓN"));

            // Test 2: Obtener alumnos de BD
            System.out.println("\nTest 2: Obteniendo alumnos de BD...");
            Map<String, InfoAlumno> alumnos = obtenerAlumnosDesdeDB();
            System.out.println("Resultado: " + alumnos.size() + " alumnos encontrados");

            // Test 3: Verificar datos de email y DNI
            System.out.println("\nTest 3: Verificando calidad de datos...");
            int alumnosConEmail = 0;
            int alumnosConDni = 0;

            for (InfoAlumno alumno : alumnos.values()) {
                if (alumno.email != null && !alumno.email.trim().isEmpty() && !"Sin Email".equals(alumno.email)) {
                    alumnosConEmail++;
                }
                if (alumno.dni != null && !alumno.dni.trim().isEmpty() && !"Sin DNI".equals(alumno.dni)) {
                    alumnosConDni++;
                }
            }

            System.out.println("Alumnos con email: " + alumnosConEmail);
            System.out.println("Alumnos con DNI: " + alumnosConDni);

            // Test 4: Búsqueda de boletines
            System.out.println("\nTest 4: Buscando boletines...");
            String anioActual = String.valueOf(LocalDate.now().getYear());
            List<BoletinPDF> boletines = buscarBoletinesPDF(anioActual, "TODOS", "TODAS", "TODOS", null);
            System.out.println("Resultado: " + boletines.size() + " boletines encontrados");

            // Test 5: Estadísticas
            System.out.println("\nTest 5: Obteniendo estadísticas...");
            Map<String, Object> stats = obtenerEstadisticasServidor(anioActual);
            System.out.println("Resultado: " + stats.size() + " métricas obtenidas");

            // Mostrar algunos resultados
            if (!boletines.isEmpty()) {
                System.out.println("\nPrimer boletín encontrado:");
                BoletinPDF primero = boletines.get(0);
                System.out.println("  Alumno: " + primero.alumnoNombre);
                System.out.println("  DNI: " + primero.alumnoDni);
                System.out.println("  Email: " + primero.alumnoEmail);
                System.out.println("  Archivo: " + primero.nombreArchivo);
                System.out.println("  URL: " + primero.urlCompleta);
                System.out.println("  Tamaño: " + primero.tamanioBytes + " bytes");
            }

            System.out.println("\n=== FIN DEL TEST CORREGIDO ===");

        } catch (Exception e) {
            System.err.println("Error en test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene información de configuración del sistema
     */
    public static String obtenerInformacionSistema() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DEL SISTEMA (CORREGIDO) ===\n");
        info.append("Servidor base: ").append(GestorBoletines.obtenerRutaServidor()).append("\n");
        info.append("Timeout conexión: ").append(TIMEOUT_CONEXION).append(" ms\n");
        info.append("Timeout lectura: ").append(TIMEOUT_LECTURA).append(" ms\n");
        info.append("User Agent: ").append(USER_AGENT).append("\n");
        info.append("Fecha actual: ").append(LocalDate.now()).append("\n");
        info.append("Conectividad: ").append(verificarConectividadServidor() ? "✅ OK" : "❌ ERROR").append("\n");
        info.append("Campo BD para email: mail (corregido)\n");
        info.append("Manejo de nulos: Activo\n");
        info.append("===============================");
        return info.toString();
    }
}
