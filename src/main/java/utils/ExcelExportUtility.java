package main.java.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utilidad para exportar datos de JTable a archivos Excel.
 * Soporta formateo avanzado y estilos personalizados.
 * 
 * @author Nicolas Bogarin
 * @version 1.0
 */
public class ExcelExportUtility {
    
    /**
     * Exporta una JTable a un archivo Excel con formateo profesional.
     * AGREGAR ESTE MÉTODO A LA CLASE ExcelExportUtility
     * 
     * @param table La tabla a exportar
     * @param titulo Título del reporte (opcional)
     * @param nombreArchivo Nombre sugerido para el archivo (opcional)
     * @return true si la exportación fue exitosa, false en caso contrario
     */
    public static boolean exportarTablaAExcel(JTable table, String titulo, String nombreArchivo) {
        if (table == null || table.getModel().getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, 
                "No hay datos para exportar", 
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Seleccionar ubicación y nombre del archivo
        File archivo = seleccionarArchivoDestino(nombreArchivo);
        if (archivo == null) {
            return false; // Usuario canceló
        }
        
        try {
            // Crear workbook y hoja
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Datos Exportados");
            
            // Crear estilos
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloDatos = crearEstiloDatos(workbook);
            CellStyle estiloAsistencia = crearEstiloAsistencia(workbook);
            
            int filaActual = 0;
            
            // Agregar título si se proporciona
            if (titulo != null && !titulo.trim().isEmpty()) {
                filaActual = agregarTitulo(sheet, titulo, estiloTitulo, table.getColumnCount());
                filaActual += 2; // Espaciado
            }
            
            // Agregar información adicional
            filaActual = agregarInformacionAdicional(sheet, filaActual, estiloDatos);
            filaActual += 1; // Espaciado
            
            // Agregar encabezados
            filaActual = agregarEncabezados(sheet, table, filaActual, estiloEncabezado);
            
            // Agregar datos
            agregarDatos(sheet, table, filaActual, estiloDatos, estiloAsistencia);
            
            // Ajustar ancho de columnas
            ajustarAnchoColumnas(sheet, table.getColumnCount());
            
            // Guardar archivo
            try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                workbook.write(fileOut);
            }
            
            workbook.close();
            
            // Mostrar mensaje de éxito
            int opcion = JOptionPane.showConfirmDialog(null,
                "Archivo exportado exitosamente a:\n" + archivo.getAbsolutePath() + 
                "\n\n¿Desea abrir el archivo?",
                "Exportación Exitosa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (opcion == JOptionPane.YES_OPTION) {
                abrirArchivo(archivo);
            }
            
            return true;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Error al exportar el archivo:\n" + e.getMessage(),
                "Error de Exportación",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Permite al usuario seleccionar la ubicación y nombre del archivo.
     */
    private static File seleccionarArchivoDestino(String nombreSugerido) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        
        // Nombre por defecto
        if (nombreSugerido == null || nombreSugerido.trim().isEmpty()) {
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
            nombreSugerido = "Reporte_" + ahora.format(formatter);
        }
        
        // Asegurar extensión .xlsx
        if (!nombreSugerido.toLowerCase().endsWith(".xlsx")) {
            nombreSugerido += ".xlsx";
        }
        
        fileChooser.setSelectedFile(new File(nombreSugerido));
        
        int userSelection = fileChooser.showSaveDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            
            // Asegurar extensión .xlsx
            if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
                archivo = new File(archivo.getAbsolutePath() + ".xlsx");
            }
            
            // Verificar si el archivo ya existe
            if (archivo.exists()) {
                int confirmacion = JOptionPane.showConfirmDialog(null,
                    "El archivo ya existe. ¿Desea sobrescribirlo?",
                    "Confirmar Sobrescritura",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirmacion != JOptionPane.YES_OPTION) {
                    return null;
                }
            }
            
            return archivo;
        }
        
        return null;
    }
    
    /**
     * Crea el estilo para el título principal.
     */
    private static CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        
        estilo.setFont(font);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THICK);
        estilo.setBorderTop(BorderStyle.THICK);
        estilo.setBorderRight(BorderStyle.THICK);
        estilo.setBorderLeft(BorderStyle.THICK);
        
        return estilo;
    }
    
    /**
     * Crea el estilo para los encabezados de columna.
     */
    private static CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        
        estilo.setFont(font);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.MEDIUM);
        estilo.setBorderTop(BorderStyle.MEDIUM);
        estilo.setBorderRight(BorderStyle.MEDIUM);
        estilo.setBorderLeft(BorderStyle.MEDIUM);
        estilo.setWrapText(true);
        
        return estilo;
    }
    
    /**
     * Crea el estilo para los datos generales.
     */
    private static CellStyle crearEstiloDatos(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setFontHeightInPoints((short) 10);
        
        estilo.setFont(font);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setWrapText(true);
        
        return estilo;
    }
    
    /**
     * Crea el estilo para datos de asistencia con colores.
     */
    private static CellStyle crearEstiloAsistencia(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        
        estilo.setFont(font);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        
        return estilo;
    }
    
    /**
     * Agrega el título principal al archivo.
     */
    private static int agregarTitulo(Sheet sheet, String titulo, CellStyle estilo, int numColumnas) {
        Row filaTitulo = sheet.createRow(0);
        filaTitulo.setHeight((short) 600); // Altura aumentada
        
        Cell celdaTitulo = filaTitulo.createCell(0);
        celdaTitulo.setCellValue(titulo);
        celdaTitulo.setCellStyle(estilo);
        
        // Fusionar celdas para el título
        if (numColumnas > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, numColumnas - 1));
        }
        
        return 1;
    }
    
    /**
     * Agrega información adicional como fecha de generación.
     */
    private static int agregarInformacionAdicional(Sheet sheet, int filaInicio, CellStyle estilo) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        Row filaInfo = sheet.createRow(filaInicio);
        Cell celdaInfo = filaInfo.createCell(0);
        celdaInfo.setCellValue("Fecha de generación: " + ahora.format(formatter));
        celdaInfo.setCellStyle(estilo);
        
        return filaInicio + 1;
    }
    
    /**
     * Agrega los encabezados de columna.
     */
    private static int agregarEncabezados(Sheet sheet, JTable table, int filaInicio, CellStyle estilo) {
        Row filaEncabezados = sheet.createRow(filaInicio);
        filaEncabezados.setHeight((short) 400);
        
        TableModel model = table.getModel();
        
        for (int col = 0; col < model.getColumnCount(); col++) {
            Cell celda = filaEncabezados.createCell(col);
            celda.setCellValue(model.getColumnName(col));
            celda.setCellStyle(estilo);
        }
        
        return filaInicio + 1;
    }
    
    /**
     * Agrega los datos de la tabla.
     */
    private static void agregarDatos(Sheet sheet, JTable table, int filaInicio, 
                                   CellStyle estiloDatos, CellStyle estiloAsistencia) {
        TableModel model = table.getModel();
        
        for (int fila = 0; fila < model.getRowCount(); fila++) {
            Row filaExcel = sheet.createRow(filaInicio + fila);
            
            for (int col = 0; col < model.getColumnCount(); col++) {
                Cell celda = filaExcel.createCell(col);
                Object valor = model.getValueAt(fila, col);
                
                if (valor != null) {
                    String valorTexto = valor.toString();
                    celda.setCellValue(valorTexto);
                    
                    // Aplicar estilo especial para estados de asistencia
                    if (esEstadoAsistencia(valorTexto)) {
                        CellStyle estiloColoreado = aplicarColorAsistencia(sheet.getWorkbook(), 
                                                                         estiloAsistencia, valorTexto);
                        celda.setCellStyle(estiloColoreado);
                    } else {
                        celda.setCellStyle(estiloDatos);
                    }
                } else {
                    celda.setCellValue("");
                    celda.setCellStyle(estiloDatos);
                }
            }
        }
    }
    
    /**
     * Verifica si un valor es un estado de asistencia.
     */
    private static boolean esEstadoAsistencia(String valor) {
        return valor != null && (valor.equals("P") || valor.equals("A") || 
                               valor.equals("T") || valor.equals("AP") || valor.equals("NC"));
    }
    
    /**
     * Aplica colores específicos según el estado de asistencia.
     */
    private static CellStyle aplicarColorAsistencia(Workbook workbook, CellStyle estiloBase, String estado) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.cloneStyleFrom(estiloBase);
        
        switch (estado) {
            case "P": // Presente - Verde
                estilo.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case "A": // Ausente - Rojo
                estilo.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case "T": // Tarde - Amarillo
                estilo.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case "AP": // Ausente con Permiso - Naranja
                estilo.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
                estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case "NC": // No Corresponde - Gris claro
                estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
        }
        
        return estilo;
    }
    
    /**
     * Ajusta automáticamente el ancho de las columnas.
     */
    private static void ajustarAnchoColumnas(Sheet sheet, int numColumnas) {
        for (int col = 0; col < numColumnas; col++) {
            sheet.autoSizeColumn(col);
            
            // Establecer un ancho mínimo y máximo
            int anchoActual = sheet.getColumnWidth(col);
            int anchoMinimo = 2000; // ~2 caracteres
            int anchoMaximo = 8000; // ~8 caracteres
            
            if (anchoActual < anchoMinimo) {
                sheet.setColumnWidth(col, anchoMinimo);
            } else if (anchoActual > anchoMaximo) {
                sheet.setColumnWidth(col, anchoMaximo);
            }
        }
    }
    
    /**
     * Intenta abrir el archivo con la aplicación predeterminada del sistema.
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
            JOptionPane.showMessageDialog(null,
                "No se pudo abrir el archivo automáticamente.\n" +
                "Puede encontrarlo en: " + archivo.getAbsolutePath(),
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Método de conveniencia para exportar con título automático.
     */
     public static boolean exportarTablaAsistencia(JTable table, String curso, String fecha) {
        String titulo = "Reporte de Asistencias - " + curso;
        if (fecha != null && !fecha.isEmpty()) {
            titulo += " - " + fecha;
        }
        
        String nombreArchivo = "Asistencias_" + curso.replace("°", "") + "_" + 
                              (fecha != null ? fecha.replace("/", "-") : 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        return exportarTablaAExcel(table, titulo, nombreArchivo);
    }
}