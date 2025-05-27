package main.java.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
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
import java.util.*;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.java.database.Conexion;

/**
 * Utilidad para generar boletines utilizando una plantilla de Excel prediseñada.
 * 
 * Esta clase trabaja con un archivo Excel específico que contiene el formato
 * institucional de la escuela, completando únicamente los campos de datos
 * sin alterar el diseño o estructura del documento.
 * 
 * @author Sistema de Gestión Escolar
 * @version 2.0
 */
public class PlantillaBoletinUtility {
    
    // Ruta por defecto de la plantilla (configurable)
    private static String RUTA_PLANTILLA_DEFAULT = "C:\\Users\\nico_\\OneDrive\\Documentos\\Pruebas\\PlantillaBoletines.xlsx";
    
    // Constantes para ubicaciones de celdas en la plantilla
    private static final String CELDA_ANIO_DIVISION = "C7";
    private static final String CELDA_APELLIDO_NOMBRE = "D7"; // Combinada hasta H7
    private static final String CELDA_DNI = "J7";
    private static final String CELDA_CODIGO_MIESCUELA = "L7";
    private static final String CELDA_INASISTENCIAS = "D22";
    private static final String CELDA_PENDIENTES_TRONCALES_CANT = "A26";
    private static final String CELDA_PENDIENTES_TRONCALES_NOMBRES = "B26"; // Combinada hasta E26
    private static final String CELDA_PENDIENTES_GENERALES_CANT = "F26";
    private static final String CELDA_PENDIENTES_GENERALES_NOMBRES = "H26"; // Combinada hasta M26
    private static final String CELDA_MATERIAS_PROCESO_CANT = "A29";
    private static final String CELDA_MATERIAS_PROCESO_NOMBRES = "B29"; // Combinada hasta M29
    
    // Filas donde van las materias (12-21)
    private static final int FILA_INICIO_MATERIAS = 12;
    private static final int FILA_FIN_MATERIAS = 21;
    private static final String COLUMNA_MATERIA = "A"; // Combinada hasta C
    
    // Columnas para las notas (D-M)
    private static final String[] COLUMNAS_NOTAS = {
        "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"
    };
    
    // Estructura para datos del estudiante
    public static class DatosEstudiante {
        public String apellidoNombre;
        public String dni;
        public String anio;
        public String division;
        public String codigoMiEscuela;
        public int inasistencias;
        public Map<String, NotasMateria> materias;
        public List<String> materiasPendientesTroncales;
        public List<String> materiasPendientesGenerales;
        public List<String> materiasEnProceso;
        
        public DatosEstudiante() {
            this.materias = new LinkedHashMap<>();
            this.materiasPendientesTroncales = new ArrayList<>();
            this.materiasPendientesGenerales = new ArrayList<>();
            this.materiasEnProceso = new ArrayList<>();
        }
    }
    
    // Estructura para notas de cada materia
    public static class NotasMateria {
        public double bimestre1 = -1;
        public double bimestre2 = -1;
        public double cuatrimestre1 = -1;
        public double bimestre3 = -1;
        public double bimestre4 = -1;
        public double cuatrimestre2 = -1;
        public double calificacionAnual = -1;
        public double diciembre = -1;
        public double febrero = -1;
        public double calificacionDefinitiva = -1;
        
        public NotasMateria() {}
    }
    
    /**
     * Configura la ruta de la plantilla a utilizar.
     */
    public static void configurarRutaPlantilla(String rutaPlantilla) {
        RUTA_PLANTILLA_DEFAULT = rutaPlantilla;
    }
    
    /**
     * Genera un boletín individual para un estudiante específico.
     */
    public static boolean generarBoletinIndividual(int alumnoId, int cursoId, String rutaDestino) {
        return generarBoletinIndividual(alumnoId, cursoId, rutaDestino, RUTA_PLANTILLA_DEFAULT);
    }
    
    /**
     * Genera un boletín individual especificando la ruta de la plantilla.
     */
    public static boolean generarBoletinIndividual(int alumnoId, int cursoId, String rutaDestino, String rutaPlantilla) {
        try {
            System.out.println("=== GENERANDO BOLETÍN INDIVIDUAL ===");
            System.out.println("Alumno ID: " + alumnoId);
            System.out.println("Curso ID: " + cursoId);
            System.out.println("Plantilla: " + rutaPlantilla);
            System.out.println("Destino: " + rutaDestino);
            
            // Verificar que existe la plantilla
            File plantilla = new File(rutaPlantilla);
            if (!plantilla.exists()) {
                System.err.println("❌ No se encontró la plantilla en: " + rutaPlantilla);
                JOptionPane.showMessageDialog(null,
                    "No se encontró la plantilla de boletín en:\n" + rutaPlantilla +
                    "\n\nVerifique la ruta o configure una nueva plantilla.",
                    "Plantilla no encontrada",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Obtener datos del estudiante
            DatosEstudiante datos = obtenerDatosEstudiante(alumnoId, cursoId);
            if (datos == null) {
                System.err.println("❌ No se pudieron obtener los datos del estudiante");
                return false;
            }
            
            // Generar el boletín
            boolean exito = procesarPlantillaBoletin(plantilla, datos, rutaDestino);
            
            if (exito) {
                System.out.println("✅ Boletín generado exitosamente: " + rutaDestino);
            } else {
                System.err.println("❌ Error al generar el boletín");
            }
            
            return exito;
            
        } catch (Exception e) {
            System.err.println("❌ Error general al generar boletín: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Genera boletines para todos los estudiantes de un curso.
     */
    public static int generarBoletinesCurso(int cursoId, String carpetaDestino) {
        return generarBoletinesCurso(cursoId, carpetaDestino, RUTA_PLANTILLA_DEFAULT);
    }
    
    /**
     * Genera boletines para todos los estudiantes especificando la plantilla.
     */
    public static int generarBoletinesCurso(int cursoId, String carpetaDestino, String rutaPlantilla) {
        int boletinesGenerados = 0;
        
        try {
            System.out.println("=== GENERANDO BOLETINES DEL CURSO ===");
            System.out.println("Curso ID: " + cursoId);
            System.out.println("Carpeta destino: " + carpetaDestino);
            System.out.println("Plantilla: " + rutaPlantilla);
            
            // Verificar plantilla
            File plantilla = new File(rutaPlantilla);
            if (!plantilla.exists()) {
                System.err.println("❌ Plantilla no encontrada: " + rutaPlantilla);
                return 0;
            }
            
            // Obtener lista de estudiantes
            List<Integer> estudiantesIds = obtenerEstudiantesCurso(cursoId);
            System.out.println("Estudiantes a procesar: " + estudiantesIds.size());
            
            for (int alumnoId : estudiantesIds) {
                try {
                    // Obtener datos del estudiante
                    DatosEstudiante datos = obtenerDatosEstudiante(alumnoId, cursoId);
                    if (datos == null) {
                        System.err.println("❌ Saltando alumno ID: " + alumnoId + " (sin datos)");
                        continue;
                    }
                    
                    // Generar nombre de archivo
                    String nombreArchivo = generarNombreArchivo(datos);
                    String rutaCompleta = carpetaDestino + File.separator + nombreArchivo;
                    
                    // Generar boletín
                    if (procesarPlantillaBoletin(plantilla, datos, rutaCompleta)) {
                        boletinesGenerados++;
                        System.out.println("✅ Boletín creado: " + nombreArchivo);
                    } else {
                        System.err.println("❌ Error al crear boletín para: " + datos.apellidoNombre);
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Error procesando alumno ID " + alumnoId + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error general generando boletines del curso: " + e.getMessage());
            e.printStackTrace();
        }
        
        return boletinesGenerados;
    }
    
    /**
     * Procesa la plantilla Excel y completa los datos del estudiante.
     */
    private static boolean procesarPlantillaBoletin(File plantilla, DatosEstudiante datos, String rutaDestino) {
        try (FileInputStream inputStream = new FileInputStream(plantilla);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            
            XSSFSheet sheet = workbook.getSheetAt(0); // Primera hoja
            
            // Configurar fuente Nunito tamaño 10 para datos
            XSSFFont fonteDatos = workbook.createFont();
            fonteDatos.setFontName("Nunito");
            fonteDatos.setFontHeightInPoints((short) 10);
            
            XSSFCellStyle estiloDatos = workbook.createCellStyle();
            estiloDatos.setFont(fonteDatos);
            
            // Completar datos básicos del estudiante
            completarDatosBasicos(sheet, datos, estiloDatos);
            
            // Completar materias y notas
            completarMateriasYNotas(sheet, datos, estiloDatos);
            
            // Completar inasistencias
            completarInasistencias(sheet, datos, estiloDatos);
            
            // Completar materias pendientes y en proceso
            completarMateriasPendientes(sheet, datos, estiloDatos);
            
            // Guardar archivo
            try (FileOutputStream outputStream = new FileOutputStream(rutaDestino)) {
                workbook.write(outputStream);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error procesando plantilla: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Completa los datos básicos del estudiante en la plantilla.
     */
    private static void completarDatosBasicos(XSSFSheet sheet, DatosEstudiante datos, XSSFCellStyle estilo) {
        // Año y División (C7)
        setCellValue(sheet, CELDA_ANIO_DIVISION, datos.anio + "°" + datos.division, estilo);
        
        // Apellido y Nombre (D7)
        setCellValue(sheet, CELDA_APELLIDO_NOMBRE, datos.apellidoNombre, estilo);
        
        // DNI (J7)
        setCellValue(sheet, CELDA_DNI, datos.dni, estilo);
        
        // Código miEscuela (L7)
        setCellValue(sheet, CELDA_CODIGO_MIESCUELA, datos.codigoMiEscuela, estilo);
        
        System.out.println("✅ Datos básicos completados");
    }
    
    /**
     * Completa las materias y sus notas en la plantilla.
     * VERSIÓN CORREGIDA: Obtiene materias dinámicamente de la base de datos.
     */
    private static void completarMateriasYNotas(XSSFSheet sheet, DatosEstudiante datos, XSSFCellStyle estilo) {
        int filaActual = FILA_INICIO_MATERIAS;
        int materiasCompletadas = 0;
        
        System.out.println("=== COMPLETANDO MATERIAS DINÁMICAMENTE ===");
        System.out.println("Materias disponibles en datos: " + datos.materias.size());
        
        // **CAMBIO IMPORTANTE**: Usar las materias reales del curso sin orden predeterminado
        // Las materias ya vienen ordenadas de la consulta SQL (ORDER BY m.nombre)
        
        for (Map.Entry<String, NotasMateria> entry : datos.materias.entrySet()) {
            if (filaActual > FILA_FIN_MATERIAS) {
                System.out.println("⚠️ Se alcanzó el límite de filas en la plantilla. Materias restantes: " + 
                                 (datos.materias.size() - materiasCompletadas));
                break;
            }
            
            String nombreMateria = entry.getKey();
            NotasMateria notas = entry.getValue();
            
            System.out.println("Completando fila " + filaActual + ": " + nombreMateria);
            
            completarFilaMateria(sheet, filaActual, nombreMateria, notas, estilo);
            filaActual++;
            materiasCompletadas++;
        }
        
        // Si quedan filas vacías, limpiarlas (opcional)
        while (filaActual <= FILA_FIN_MATERIAS) {
            limpiarFilaMateria(sheet, filaActual, estilo);
            filaActual++;
        }
        
        System.out.println("✅ Materias completadas: " + materiasCompletadas + " de " + datos.materias.size());
        
        if (materiasCompletadas < datos.materias.size()) {
            System.out.println("⚠️ ADVERTENCIA: La plantilla solo permite " + (FILA_FIN_MATERIAS - FILA_INICIO_MATERIAS + 1) + 
                              " materias, pero el curso tiene " + datos.materias.size() + " materias.");
        }
    }
    
    /**
     * Limpia una fila de materia vacía en la plantilla.
     */
    private static void limpiarFilaMateria(XSSFSheet sheet, int fila, XSSFCellStyle estilo) {
        // Limpiar nombre de materia (columna A)
        setCellValue(sheet, COLUMNA_MATERIA + fila, "", estilo);
        
        // Limpiar todas las columnas de notas
        for (String columna : COLUMNAS_NOTAS) {
            setCellValue(sheet, columna + fila, "", estilo);
        }
        
        System.out.println("  Fila " + fila + " limpiada");
    }
    
    /**
     * Completa una fila específica con los datos de una materia.
     */
    private static void completarFilaMateria(XSSFSheet sheet, int fila, String nombreMateria, 
                                           NotasMateria notas, XSSFCellStyle estilo) {
        // Nombre de la materia (columna A, combinada hasta C)
        setCellValue(sheet, COLUMNA_MATERIA + fila, nombreMateria, estilo);
        
        // Notas en orden: 1°Bim, 2°Bim, 1°Cuat, 3°Bim, 4°Bim, 2°Cuat, Cal.Anual, Dic, Feb, Cal.Def
        double[] valoresNotas = {
            notas.bimestre1, notas.bimestre2, notas.cuatrimestre1,
            notas.bimestre3, notas.bimestre4, notas.cuatrimestre2,
            notas.calificacionAnual, notas.diciembre, notas.febrero, notas.calificacionDefinitiva
        };
        
        for (int i = 0; i < COLUMNAS_NOTAS.length && i < valoresNotas.length; i++) {
            String celda = COLUMNAS_NOTAS[i] + fila;
            if (valoresNotas[i] >= 0) {
                setCellValue(sheet, celda, String.format("%.1f", valoresNotas[i]), estilo);
            }
        }
    }
    
    /**
     * Completa las inasistencias en la plantilla.
     */
    private static void completarInasistencias(XSSFSheet sheet, DatosEstudiante datos, XSSFCellStyle estilo) {
        setCellValue(sheet, CELDA_INASISTENCIAS, String.valueOf(datos.inasistencias), estilo);
        System.out.println("✅ Inasistencias completadas: " + datos.inasistencias);
    }
    
    /**
     * Completa las materias pendientes y en proceso.
     */
    private static void completarMateriasPendientes(XSSFSheet sheet, DatosEstudiante datos, XSSFCellStyle estilo) {
        // Materias pendientes troncales
        setCellValue(sheet, CELDA_PENDIENTES_TRONCALES_CANT, 
                    String.valueOf(datos.materiasPendientesTroncales.size()), estilo);
        setCellValue(sheet, CELDA_PENDIENTES_TRONCALES_NOMBRES, 
                    String.join(", ", datos.materiasPendientesTroncales), estilo);
        
        // Materias pendientes generales
        setCellValue(sheet, CELDA_PENDIENTES_GENERALES_CANT, 
                    String.valueOf(datos.materiasPendientesGenerales.size()), estilo);
        setCellValue(sheet, CELDA_PENDIENTES_GENERALES_NOMBRES, 
                    String.join(", ", datos.materiasPendientesGenerales), estilo);
        
        // Materias en proceso
        setCellValue(sheet, CELDA_MATERIAS_PROCESO_CANT, 
                    String.valueOf(datos.materiasEnProceso.size()), estilo);
        setCellValue(sheet, CELDA_MATERIAS_PROCESO_NOMBRES, 
                    String.join(", ", datos.materiasEnProceso), estilo);
        
        System.out.println("✅ Materias pendientes y en proceso completadas");
    }
    
    /**
     * Establece el valor de una celda específica.
     */
    private static void setCellValue(XSSFSheet sheet, String cellAddress, String value, XSSFCellStyle style) {
        try {
            org.apache.poi.ss.util.CellReference cellRef = new org.apache.poi.ss.util.CellReference(cellAddress);
            Row row = sheet.getRow(cellRef.getRow());
            if (row == null) {
                row = sheet.createRow(cellRef.getRow());
            }
            
            Cell cell = row.getCell(cellRef.getCol());
            if (cell == null) {
                cell = row.createCell(cellRef.getCol());
            }
            
            cell.setCellValue(value != null ? value : "");
            if (style != null) {
                cell.setCellStyle(style);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error estableciendo valor en celda " + cellAddress + ": " + e.getMessage());
        }
    }
    
    /**
     * Obtiene los datos completos de un estudiante desde la base de datos.
     */
    private static DatosEstudiante obtenerDatosEstudiante(int alumnoId, int cursoId) {
        Connection conect = null;
        
        try {
            conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ No se pudo establecer conexión con la base de datos");
                return null;
            }
            
            DatosEstudiante datos = new DatosEstudiante();
            
            // Obtener datos básicos del estudiante
            if (!obtenerDatosBasicos(datos, alumnoId, cursoId, conect)) {
                return null;
            }
            
            // Obtener notas por materia
            obtenerNotasMaterias(datos, alumnoId, cursoId, conect);
            
            // Obtener inasistencias
            obtenerInasistencias(datos, alumnoId, cursoId, conect);
            
            // Obtener materias pendientes (esto dependería de tu lógica de negocio)
            obtenerMateriasPendientes(datos, alumnoId, cursoId, conect);
            
            return datos;
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo datos del estudiante: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene los datos básicos del estudiante.
     */
    private static boolean obtenerDatosBasicos(DatosEstudiante datos, int alumnoId, int cursoId, Connection conect) {
        try {
            String query = """
                SELECT u.nombre, u.apellido, u.dni, c.anio, c.division
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id 
                INNER JOIN cursos c ON ac.curso_id = c.id 
                WHERE u.id = ? AND ac.curso_id = ? AND ac.estado = 'activo'
                """;
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ps.setInt(2, cursoId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                datos.apellidoNombre = rs.getString("apellido") + ", " + rs.getString("nombre");
                datos.dni = rs.getString("dni");
                datos.anio = String.valueOf(rs.getInt("anio"));
                datos.division = String.valueOf(rs.getInt("division"));
                datos.codigoMiEscuela = generarCodigoMiEscuela(datos.dni, datos.anio, datos.division);
                
                System.out.println("✅ Datos básicos obtenidos: " + datos.apellidoNombre);
                return true;
            } else {
                System.err.println("❌ No se encontraron datos básicos para alumno ID: " + alumnoId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo datos básicos: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene las notas de todas las materias del estudiante.
     */
    private static void obtenerNotasMaterias(DatosEstudiante datos, int alumnoId, int cursoId, Connection conect) {
        try {
            // Obtener DNI para búsquedas
            String alumnoDni = datos.dni;
            
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
                obtenerNotasBimestrales(notas, materiaId, alumnoDni, conect);
                
                // Si no hay notas bimestrales, calcular desde trabajos
                if (notas.bimestre1 == -1 && notas.bimestre2 == -1 && 
                    notas.bimestre3 == -1 && notas.bimestre4 == -1) {
                    obtenerNotasTrabajos(notas, materiaId, alumnoDni, conect);
                }
                
                // Calcular notas derivadas
                calcularNotasDerivadas(notas);
                
                datos.materias.put(nombreMateria.toUpperCase(), notas);
            }
            
            System.out.println("✅ Notas de materias obtenidas: " + datos.materias.size() + " materias");
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas de materias: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene las notas bimestrales de una materia.
     */
    private static void obtenerNotasBimestrales(NotasMateria notas, int materiaId, String alumnoDni, Connection conect) {
        try {
            String query = """
                SELECT periodo, nota 
                FROM notas_bimestrales 
                WHERE materia_id = ? AND alumno_id = ?
                """;
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, alumnoDni);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String periodo = rs.getString("periodo").toLowerCase();
                double nota = rs.getDouble("nota");
                
                switch (periodo) {
                    case "1er bimestre":
                    case "1b":
                        notas.bimestre1 = nota;
                        break;
                    case "2do bimestre":
                    case "2b":
                        notas.bimestre2 = nota;
                        break;
                    case "3er bimestre":
                    case "3b":
                        notas.bimestre3 = nota;
                        break;
                    case "4to bimestre":
                    case "4b":
                        notas.bimestre4 = nota;
                        break;
                    case "diciembre":
                        notas.diciembre = nota;
                        break;
                    case "febrero":
                        notas.febrero = nota;
                        break;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas bimestrales: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene notas de trabajos para calcular promedios bimestrales.
     */
    private static void obtenerNotasTrabajos(NotasMateria notas, int materiaId, String alumnoDni, Connection conect) {
        try {
            String query = """
                SELECT n.nota, t.fecha_creacion
                FROM notas n
                INNER JOIN trabajos t ON n.trabajo_id = t.id
                WHERE t.materia_id = ? AND n.alumno_id = ?
                ORDER BY t.fecha_creacion
                """;
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, alumnoDni);
            ResultSet rs = ps.executeQuery();
            
            List<Double> notasBim1 = new ArrayList<>();
            List<Double> notasBim2 = new ArrayList<>();
            List<Double> notasBim3 = new ArrayList<>();
            List<Double> notasBim4 = new ArrayList<>();
            
            while (rs.next()) {
                double nota = rs.getDouble("nota");
                java.sql.Date fecha = rs.getDate("fecha_creacion");
                
                if (fecha != null) {
                    int mes = fecha.toLocalDate().getMonthValue();
                    
                    if (mes >= 3 && mes <= 5) {
                        notasBim1.add(nota);
                    } else if (mes >= 6 && mes <= 8) {
                        notasBim2.add(nota);
                    } else if (mes >= 9 && mes <= 11) {
                        notasBim3.add(nota);
                    } else if (mes == 12 || mes >= 1 && mes <= 2) {
                        notasBim4.add(nota);
                    }
                }
            }
            
            // Calcular promedios
            notas.bimestre1 = calcularPromedio(notasBim1);
            notas.bimestre2 = calcularPromedio(notasBim2);
            notas.bimestre3 = calcularPromedio(notasBim3);
            notas.bimestre4 = calcularPromedio(notasBim4);
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo notas de trabajos: " + e.getMessage());
        }
    }
    
    /**
     * Calcula el promedio de una lista de notas.
     */
    private static double calcularPromedio(List<Double> notas) {
        if (notas.isEmpty()) return -1;
        
        double suma = notas.stream().mapToDouble(Double::doubleValue).sum();
        return Math.round((suma / notas.size()) * 10.0) / 10.0;
    }
    
    /**
     * Calcula las notas derivadas (cuatrimestres, anual, definitiva).
     */
    private static void calcularNotasDerivadas(NotasMateria notas) {
        // Primer cuatrimestre
        if (notas.bimestre1 >= 0 && notas.bimestre2 >= 0) {
            notas.cuatrimestre1 = Math.round(((notas.bimestre1 + notas.bimestre2) / 2.0) * 10.0) / 10.0;
        } else if (notas.bimestre1 >= 0) {
            notas.cuatrimestre1 = notas.bimestre1;
        } else if (notas.bimestre2 >= 0) {
            notas.cuatrimestre1 = notas.bimestre2;
        }
        
        // Segundo cuatrimestre
        if (notas.bimestre3 >= 0 && notas.bimestre4 >= 0) {
            notas.cuatrimestre2 = Math.round(((notas.bimestre3 + notas.bimestre4) / 2.0) * 10.0) / 10.0;
        } else if (notas.bimestre3 >= 0) {
            notas.cuatrimestre2 = notas.bimestre3;
        } else if (notas.bimestre4 >= 0) {
            notas.cuatrimestre2 = notas.bimestre4;
        }
        
        // Calificación anual (promedio de cuatrimestres)
        if (notas.cuatrimestre1 >= 0 && notas.cuatrimestre2 >= 0) {
            notas.calificacionAnual = Math.round(((notas.cuatrimestre1 + notas.cuatrimestre2) / 2.0) * 10.0) / 10.0;
        } else if (notas.cuatrimestre1 >= 0) {
            notas.calificacionAnual = notas.cuatrimestre1;
        } else if (notas.cuatrimestre2 >= 0) {
            notas.calificacionAnual = notas.cuatrimestre2;
        }
        
        // Calificación definitiva (prioridad: febrero > diciembre > anual)
        if (notas.febrero >= 0) {
            notas.calificacionDefinitiva = notas.febrero;
        } else if (notas.diciembre >= 0) {
            notas.calificacionDefinitiva = notas.diciembre;
        } else if (notas.calificacionAnual >= 0) {
            notas.calificacionDefinitiva = notas.calificacionAnual;
        }
    }
    
    /**
     * Obtiene las inasistencias del estudiante.
     */
    private static void obtenerInasistencias(DatosEstudiante datos, int alumnoId, int cursoId, Connection conect) {
        try {
            String query = """
                SELECT COUNT(*) as total_inasistencias
                FROM asistencia_general 
                WHERE alumno_id = ? AND curso_id = ? AND estado IN ('A', 'AP')
                """;
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ps.setInt(2, cursoId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                datos.inasistencias = rs.getInt("total_inasistencias");
            } else {
                datos.inasistencias = 0;
            }
            
            System.out.println("✅ Inasistencias obtenidas: " + datos.inasistencias);
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo inasistencias: " + e.getMessage());
            datos.inasistencias = 0;
        }
    }
    
    /**
     * Obtiene las materias pendientes y en proceso.
     * Esta lógica depende de cómo tu sistema maneje estos estados.
     */
    private static void obtenerMateriasPendientes(DatosEstudiante datos, int alumnoId, int cursoId, Connection conect) {
        try {
            // Esta es una implementación de ejemplo. Ajusta según tu lógica de negocio.
            
            // Materias con calificación anual menor a 6 podrían considerarse pendientes
            for (Map.Entry<String, NotasMateria> entry : datos.materias.entrySet()) {
                String materia = entry.getKey();
                NotasMateria notas = entry.getValue();
                
                if (notas.calificacionAnual >= 0 && notas.calificacionAnual < 6.0) {
                    // Determinar si es troncal o general (esto depende de tu clasificación)
                    if (esMateriaTrancal(materia)) {
                        datos.materiasPendientesTroncales.add(materia);
                    } else {
                        datos.materiasPendientesGenerales.add(materia);
                    }
                }
                
                // Materias en proceso (ejemplo: sin calificación definitiva)
                if (notas.calificacionDefinitiva == -1 && notas.calificacionAnual >= 0) {
                    datos.materiasEnProceso.add(materia);
                }
            }
            
            System.out.println("✅ Materias pendientes obtenidas - Troncales: " + 
                             datos.materiasPendientesTroncales.size() + 
                             ", Generales: " + datos.materiasPendientesGenerales.size() +
                             ", En proceso: " + datos.materiasEnProceso.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo materias pendientes: " + e.getMessage());
        }
    }
    
    /**
     * Determina si una materia es troncal (materias principales).
     */
    private static boolean esMateriaTrancal(String materia) {
        String[] materiasTrancales = {
            "MATEMÁTICA", "LENGUA", "CIENCIAS SOCIALES", "CIENCIAS NATURALES"
        };
        
        return Arrays.asList(materiasTrancales).contains(materia.toUpperCase());
    }
    
    /**
     * Obtiene la lista de IDs de estudiantes de un curso.
     */
    private static List<Integer> obtenerEstudiantesCurso(int cursoId) {
        List<Integer> estudiantesIds = new ArrayList<>();
        
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) return estudiantesIds;
            
            String query = """
                SELECT u.id 
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id 
                WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4
                ORDER BY u.apellido, u.nombre
                """;
            
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                estudiantesIds.add(rs.getInt("id"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo estudiantes del curso: " + e.getMessage());
        }
        
        return estudiantesIds;
    }
    
    /**
     * Genera el nombre del archivo para el boletín.
     */
    private static String generarNombreArchivo(DatosEstudiante datos) {
        String apellido = datos.apellidoNombre.split(",")[0].trim();
        apellido = apellido.replaceAll("[^a-zA-Z0-9]", "_");
        
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return String.format("Boletin_%s_%s%s_%s.xlsx",
                apellido, datos.anio, datos.division, fecha);
    }
    
    /**
     * Genera un código miEscuela basado en los datos disponibles.
     */
    private static String generarCodigoMiEscuela(String dni, String anio, String division) {
        try {
            String codigo;
            
            if (dni != null && !dni.trim().isEmpty()) {
                String ultimosDigitos = dni.length() >= 4 ? dni.substring(dni.length() - 4)
                        : String.format("%04d", Integer.parseInt(dni));
                codigo = "ET20" + ultimosDigitos + anio + division;
            } else {
                String anioActual = String.valueOf(LocalDate.now().getYear());
                codigo = "ET20" + anioActual.substring(2) + "0000" + anio + division;
            }
            
            return codigo;
            
        } catch (Exception e) {
            System.err.println("❌ Error generando código miEscuela: " + e.getMessage());
            return "ET20" + anio + division + "001";
        }
    }
    
    // =================================================================
    // MÉTODOS PÚBLICOS PARA INTEGRACIÓN CON LA INTERFAZ
    // =================================================================
    
    /**
     * Método para integración con interfaz - Generar boletín individual con selector de archivo.
     */
    public static void generarBoletinIndividualConInterfaz(int alumnoId, int cursoId, 
                                                           javax.swing.JComponent parentComponent) {
        generarBoletinIndividualConInterfaz(alumnoId, cursoId, parentComponent, RUTA_PLANTILLA_DEFAULT);
    }
    
    /**
     * Método para integración con interfaz - Generar boletín individual especificando plantilla.
     */
    public static void generarBoletinIndividualConInterfaz(int alumnoId, int cursoId, 
                                                           javax.swing.JComponent parentComponent,
                                                           String rutaPlantilla) {
        try {
            // Verificar plantilla
            File plantilla = new File(rutaPlantilla);
            if (!plantilla.exists()) {
                // Permitir seleccionar plantilla manualmente
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Seleccionar plantilla de boletín");
                fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
                
                int resultado = fileChooser.showOpenDialog(parentComponent);
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    plantilla = fileChooser.getSelectedFile();
                    rutaPlantilla = plantilla.getAbsolutePath();
                } else {
                    return; // Usuario canceló
                }
            }
            
            // Obtener datos del estudiante para nombre sugerido
            DatosEstudiante datos = obtenerDatosEstudiante(alumnoId, cursoId);
            if (datos == null) {
                JOptionPane.showMessageDialog(parentComponent,
                        "No se pudieron obtener los datos del estudiante",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Seleccionar archivo de destino
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar boletín");
            fileChooser.setSelectedFile(new File(generarNombreArchivo(datos)));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
            
            if (fileChooser.showSaveDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                
                // Asegurar extensión .xlsx
                if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
                    archivo = new File(archivo.getAbsolutePath() + ".xlsx");
                }
                
                final String rutaFinalArchivo = archivo.getAbsolutePath();
                final String rutaFinalPlantilla = rutaPlantilla;
                
                // Exportar en hilo separado
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return generarBoletinIndividual(alumnoId, cursoId, rutaFinalArchivo, rutaFinalPlantilla);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            boolean exito = get();
                            if (exito) {
                                int opcion = JOptionPane.showConfirmDialog(parentComponent,
                                        "Boletín creado exitosamente en:\n" + rutaFinalArchivo +
                                        "\n\n¿Desea abrir el archivo?",
                                        "Exportación Exitosa",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.INFORMATION_MESSAGE);
                                
                                if (opcion == JOptionPane.YES_OPTION) {
                                    abrirArchivo(new File(rutaFinalArchivo));
                                }
                            } else {
                                JOptionPane.showMessageDialog(parentComponent,
                                        "Error al crear el boletín",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(parentComponent,
                                    "Error durante la exportación: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                
                worker.execute();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent,
                    "Error al iniciar exportación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Método para integración con interfaz - Generar boletines de todo el curso.
     */
    public static void generarBoletinesCursoConInterfaz(int cursoId, javax.swing.JComponent parentComponent) {
        generarBoletinesCursoConInterfaz(cursoId, parentComponent, RUTA_PLANTILLA_DEFAULT);
    }
    
    /**
     * Método para integración con interfaz - Generar boletines especificando plantilla.
     */
    public static void generarBoletinesCursoConInterfaz(int cursoId, javax.swing.JComponent parentComponent,
                                                        String rutaPlantilla) {
        try {
            // Verificar plantilla
            File plantilla = new File(rutaPlantilla);
            if (!plantilla.exists()) {
                // Permitir seleccionar plantilla manualmente
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Seleccionar plantilla de boletín");
                fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
                
                int resultado = fileChooser.showOpenDialog(parentComponent);
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    plantilla = fileChooser.getSelectedFile();
                    rutaPlantilla = plantilla.getAbsolutePath();
                } else {
                    return; // Usuario canceló
                }
            }
            
            // Seleccionar carpeta de destino
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar carpeta para guardar boletines");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            if (fileChooser.showSaveDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
                File carpetaDestino = fileChooser.getSelectedFile();
                
                // Crear subcarpeta con fecha
                String nombreSubcarpeta = "Boletines_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                File subcarpeta = new File(carpetaDestino, nombreSubcarpeta);
                
                if (!subcarpeta.exists()) {
                    subcarpeta.mkdirs();
                }
                
                final String rutaFinalSubcarpeta = subcarpeta.getAbsolutePath();
                final String rutaFinalPlantilla = rutaPlantilla;
                
                // Mostrar progreso y exportar
                SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
                    @Override
                    protected Integer doInBackground() throws Exception {
                        return generarBoletinesCurso(cursoId, rutaFinalSubcarpeta, rutaFinalPlantilla);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            int boletinesCreados = get();
                            
                            if (boletinesCreados > 0) {
                                int opcion = JOptionPane.showConfirmDialog(parentComponent,
                                        String.format("Se crearon %d boletines exitosamente en:\n%s" +
                                                     "\n\n¿Desea abrir la carpeta?",
                                                     boletinesCreados, rutaFinalSubcarpeta),
                                        "Exportación Completada",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.INFORMATION_MESSAGE);
                                
                                if (opcion == JOptionPane.YES_OPTION) {
                                    abrirCarpeta(new File(rutaFinalSubcarpeta));
                                }
                            } else {
                                JOptionPane.showMessageDialog(parentComponent,
                                        "No se pudieron crear boletines.\n" +
                                        "Verifique que haya estudiantes en el curso y que tengan datos suficientes.",
                                        "Sin boletines generados",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(parentComponent,
                                    "Error durante la exportación: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                
                worker.execute();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent,
                    "Error al iniciar exportación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Método para configurar la plantilla desde la interfaz.
     */
    public static void configurarPlantillaConInterfaz(javax.swing.JComponent parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar plantilla de boletín");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        
        // Establecer directorio inicial en la ruta actual de plantilla
        File plantillaActual = new File(RUTA_PLANTILLA_DEFAULT);
        if (plantillaActual.getParentFile() != null && plantillaActual.getParentFile().exists()) {
            fileChooser.setCurrentDirectory(plantillaActual.getParentFile());
        }
        
        int resultado = fileChooser.showOpenDialog(parentComponent);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File nuevaPlantilla = fileChooser.getSelectedFile();
            configurarRutaPlantilla(nuevaPlantilla.getAbsolutePath());
            
            JOptionPane.showMessageDialog(parentComponent,
                    "Plantilla configurada exitosamente:\n" + nuevaPlantilla.getAbsolutePath(),
                    "Configuración Actualizada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Obtiene la ruta actual de la plantilla.
     */
    public static String obtenerRutaPlantilla() {
        return RUTA_PLANTILLA_DEFAULT;
    }
    
    /**
     * Intenta abrir un archivo con la aplicación predeterminada del sistema.
     */
    private static void abrirArchivo(File archivo) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(archivo);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error abriendo archivo: " + e.getMessage());
        }
    }
    
    /**
     * Intenta abrir una carpeta con el explorador de archivos del sistema.
     */
    private static void abrirCarpeta(File carpeta) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(carpeta);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error abriendo carpeta: " + e.getMessage());
        }
    }
}