package main.java.utils;

/**
 * Utilidades adicionales para el sistema de boletines
 */
public class BoletinesUtils {
    
    /**
     * Obtiene el período actual basado en la fecha.
     */
    public static String obtenerPeriodoActual() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        int mes = hoy.getMonthValue();
        
        if (mes >= 3 && mes <=4 ) return "1B";
        if (mes >= 5 && mes <= 7) return "2B";
        if (mes >= 8 && mes <= 9) return "3B";
        if (mes >= 10 && mes <= 11) return "4B";
        
        return "1B"; // Por defecto
    }
    
    /**
     * Valida si un período es válido.
     */
    public static boolean esPeriodoValido(String periodo) {
        for (String p : GestorBoletines.PERIODOS_VALIDOS) {
            if (p.equals(periodo)) return true;
        }
        return false;
    }
    
    /**
     * Obtiene la descripción larga de un período.
     */
    public static String obtenerDescripcionPeriodo(String periodo) {
        switch (periodo) {
            case "1B": return "Primer Bimestre";
            case "2B": return "Segundo Bimestre";
            case "3B": return "Tercer Bimestre";
            case "4B": return "Cuarto Bimestre";
            case "1C": return "Primer Cuatrimestre";
            case "2C": return "Segundo Cuatrimestre";
            case "Final": return "Nota Final";
            case "Diciembre": return "Examen Diciembre";
            case "Febrero": return "Examen Febrero";
            default: return periodo;
        }
    }
    
    /**
     * Genera un nombre de archivo estándar para boletines.
     */
    public static String generarNombreArchivoBoletin(String apellido, String nombre, 
                                                    String curso, String division, 
                                                    String periodo, java.time.LocalDate fecha) {
        String apellidoLimpio = apellido.replaceAll("[^a-zA-Z0-9]", "_");
        String fechaStr = fecha.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return String.format("Boletin_%s_%s_%s%s_%s_%s.xlsx",
            apellidoLimpio, nombre.charAt(0), curso, division, periodo, fechaStr);
    }
    
    /**
     * Verifica la integridad de la estructura de carpetas.
     */
    public static boolean verificarEstructuraCarpetas(int anioLectivo) {
        String rutaBase = GestorBoletines.obtenerRutaServidor();
        java.io.File directorioAnio = new java.io.File(rutaBase, String.valueOf(anioLectivo));
        
        if (!directorioAnio.exists()) {
            System.out.println("❌ No existe directorio para año: " + anioLectivo);
            return false;
        }
        
        // Verificar que hay al menos una carpeta de curso
        java.io.File[] cursos = directorioAnio.listFiles(java.io.File::isDirectory);
        if (cursos == null || cursos.length == 0) {
            System.out.println("❌ No hay carpetas de cursos en: " + anioLectivo);
            return false;
        }
        
        System.out.println("✅ Estructura de carpetas verificada para año: " + anioLectivo);
        return true;
    }
}