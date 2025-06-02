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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utilidad para generar boletines usando plantilla Excel institucional. VERSIÓN
 * SERVIDOR: Los boletines se guardan automáticamente en el servidor sin
 * preguntar al usuario la ubicación.
 *
 * @author Sistema de Gestión Escolar
 * @version 3.0 - Servidor Automático
 */
public class PlantillaBoletinUtility {

    // Configuración de la plantilla (ahora en el servidor)
    private static String RUTA_PLANTILLA_SERVIDOR = null; // Se inicializará dinámicamente

    // Mapeo de períodos para compatibilidad con BD
    private static final Map<String, String> MAPEO_PERIODOS = new HashMap<>();

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

            // Si es una URL web, usar ruta local por defecto para la plantilla
            if (servidorBase.startsWith("http://") || servidorBase.startsWith("https://")) {
                RUTA_PLANTILLA_SERVIDOR = System.getProperty("user.dir") + File.separator + "plantilla_boletin.xlsx";
            } else {
                // Si es ruta local, buscar plantilla en la carpeta del servidor
                RUTA_PLANTILLA_SERVIDOR = servidorBase + File.separator + "plantilla_boletin.xlsx";
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
        RUTA_PLANTILLA_SERVIDOR = nuevaRuta;
        System.out.println("Nueva ruta de plantilla configurada: " + nuevaRuta);
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
     * Genera el boletín usando la plantilla Excel
     */
    private static boolean generarBoletinConPlantilla(DatosEstudiante estudiante,
            Map<String, NotasMateria> notasPorMateria,
            String periodo,
            String rutaDestino) {
        try {
            System.out.println("Generando boletín con plantilla para: " + estudiante.apellidoNombre);

            // 1. Verificar que existe la plantilla
            String rutaPlantilla = obtenerRutaPlantilla();
            File archivoPlantilla = new File(rutaPlantilla);

            if (!archivoPlantilla.exists()) {
                System.err.println("❌ No se encontró la plantilla en: " + rutaPlantilla);
                return false;
            }

            // 2. Cargar la plantilla
            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(archivoPlantilla)) {
                workbook = new XSSFWorkbook(fis);
            }

            Sheet hoja = workbook.getSheetAt(0); // Asumir que la plantilla está en la primera hoja

            // 3. Llenar datos del estudiante
            llenarDatosEstudiante(hoja, estudiante, periodo);

            // 4. Llenar notas por materia
            llenarNotasMaterias(hoja, notasPorMateria, periodo);

            // 5. Calcular notas derivadas (cuatrimestres, anual, etc.)
            calcularNotasDerivadas(hoja, notasPorMateria);

            // 6. Guardar el archivo en la ruta de destino
            try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
                workbook.write(fos);
            }

            workbook.close();

            System.out.println("✅ Boletín generado y guardado en: " + rutaDestino);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error generando boletín con plantilla: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Llena los datos básicos del estudiante en la plantilla
     */
    private static void llenarDatosEstudiante(Sheet hoja, DatosEstudiante estudiante, String periodo) {
        try {
            // Buscar y llenar celdas específicas de la plantilla
            // Nota: Ajustar estas posiciones según la plantilla real

            setCellValue(hoja, 2, 1, estudiante.apellidoNombre); // Nombre del estudiante
            setCellValue(hoja, 3, 1, estudiante.dni); // DNI
            setCellValue(hoja, 4, 1, estudiante.curso + "° " + estudiante.division); // Curso
            setCellValue(hoja, 5, 1, estudiante.codigoMiEscuela); // Código Mi Escuela
            setCellValue(hoja, 6, 1, periodo); // Período
            setCellValue(hoja, 7, 1, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); // Fecha

            System.out.println("✅ Datos del estudiante completados");

        } catch (Exception e) {
            System.err.println("❌ Error llenando datos del estudiante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Llena las notas de las materias en la plantilla
     */
    private static void llenarNotasMaterias(Sheet hoja, Map<String, NotasMateria> notasPorMateria, String periodo) {
        try {
            int filaInicio = 10; // Fila donde empiezan las materias en la plantilla
            int filaActual = filaInicio;

            for (Map.Entry<String, NotasMateria> entry : notasPorMateria.entrySet()) {
                String nombreMateria = entry.getKey();
                NotasMateria notas = entry.getValue();

                // Llenar nombre de la materia
                setCellValue(hoja, filaActual, 0, nombreMateria);

                // Llenar notas bimestrales
                setCellValue(hoja, filaActual, 1, notas.bimestre1 > 0 ? notas.bimestre1 : "");
                setCellValue(hoja, filaActual, 2, notas.bimestre2 > 0 ? notas.bimestre2 : "");
                setCellValue(hoja, filaActual, 3, notas.bimestre3 > 0 ? notas.bimestre3 : "");
                setCellValue(hoja, filaActual, 4, notas.bimestre4 > 0 ? notas.bimestre4 : "");

                // Llenar cuatrimestres
                setCellValue(hoja, filaActual, 5, notas.cuatrimestre1 > 0 ? notas.cuatrimestre1 : "");
                setCellValue(hoja, filaActual, 6, notas.cuatrimestre2 > 0 ? notas.cuatrimestre2 : "");

                // Llenar exámenes
                setCellValue(hoja, filaActual, 7, notas.diciembre > 0 ? notas.diciembre : "");
                setCellValue(hoja, filaActual, 8, notas.febrero > 0 ? notas.febrero : "");

                // Observaciones
                setCellValue(hoja, filaActual, 9, notas.observaciones != null ? notas.observaciones : "");

                filaActual++;
            }

            System.out.println("✅ Notas de materias completadas (" + notasPorMateria.size() + " materias)");

        } catch (Exception e) {
            System.err.println("❌ Error llenando notas de materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calcula notas derivadas (cuatrimestres, anual, etc.)
     */
    private static void calcularNotasDerivadas(Sheet hoja, Map<String, NotasMateria> notasPorMateria) {
        try {
            for (Map.Entry<String, NotasMateria> entry : notasPorMateria.entrySet()) {
                NotasMateria notas = entry.getValue();

                // Calcular cuatrimestre 1 si no existe
                if (notas.cuatrimestre1 <= 0 && notas.bimestre1 > 0 && notas.bimestre2 > 0) {
                    notas.cuatrimestre1 = (notas.bimestre1 + notas.bimestre2) / 2;
                }

                // Calcular cuatrimestre 2 si no existe
                if (notas.cuatrimestre2 <= 0 && notas.bimestre3 > 0 && notas.bimestre4 > 0) {
                    notas.cuatrimestre2 = (notas.bimestre3 + notas.bimestre4) / 2;
                }

                // Calcular nota anual
                if (notas.cuatrimestre1 > 0 && notas.cuatrimestre2 > 0) {
                    double notaAnual = (notas.cuatrimestre1 + notas.cuatrimestre2) / 2;
                    // Aquí podrías escribir la nota anual en una celda específica
                }
            }

            System.out.println("✅ Notas derivadas calculadas");

        } catch (Exception e) {
            System.err.println("❌ Error calculando notas derivadas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar para establecer valor en una celda
     */
    private static void setCellValue(Sheet hoja, int fila, int columna, Object valor) {
        try {
            Row row = hoja.getRow(fila);
            if (row == null) {
                row = hoja.createRow(fila);
            }

            Cell cell = row.getCell(columna);
            if (cell == null) {
                cell = row.createCell(columna);
            }

            if (valor instanceof String) {
                cell.setCellValue((String) valor);
            } else if (valor instanceof Double) {
                cell.setCellValue((Double) valor);
            } else if (valor instanceof Integer) {
                cell.setCellValue((Integer) valor);
            } else if (valor != null) {
                cell.setCellValue(valor.toString());
            }

        } catch (Exception e) {
            System.err.println("Error estableciendo valor en celda [" + fila + "," + columna + "]: " + e.getMessage());
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

            String query = """
            SELECT u.nombre, u.apellido, u.dni, c.anio, c.division, u.codigo_miescuela
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

                rs.close();
                ps.close();

                System.out.println("✅ Datos de estudiante obtenidos: " + datos.apellidoNombre
                        + " - Curso: " + datos.curso + datos.division);
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
     * Obtiene las notas de un estudiante por materia
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

            // Obtener materias del curso
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

                NotasMateria notas = new NotasMateria();

                // Obtener notas bimestrales
                obtenerNotasBimestrales(alumnoDni, materiaId, notas);

                notasPorMateria.put(nombreMateria, notas);
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
     * Obtiene las notas bimestrales de una materia específica
     */
    private static void obtenerNotasBimestrales(String alumnoDni, int materiaId, NotasMateria notas) {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return;
            }

            String query = """
                SELECT periodo, nota, promedio_actividades, observaciones 
                FROM notas_bimestrales 
                WHERE alumno_id = ? AND materia_id = ?
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, alumnoDni);
            ps.setInt(2, materiaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String periodo = rs.getString("periodo");
                double nota = rs.getDouble("nota");
                String observaciones = rs.getString("observaciones");

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

                if (observaciones != null && !observaciones.trim().isEmpty()) {
                    notas.observaciones = observaciones;
                }
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas bimestrales: " + e.getMessage());
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
}
