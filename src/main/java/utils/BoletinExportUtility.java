package main.java.utils;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Utilidad para exportar boletines de calificaciones.
 *
 * VERSIÓN ACTUALIZADA: Ahora utiliza PlantillaBoletinUtility para generar
 * boletines basados en la plantilla Excel institucional prediseñada.
 *
 * Esta clase mantiene la compatibilidad con el código existente mientras delega
 * la funcionalidad real a la nueva implementación basada en plantilla.
 *
 * @author Sistema de Gestión Escolar
 * @version 2.0
 */
public class BoletinExportUtility {

    /**
     * Exporta el boletín de un alumno específico usando la plantilla
     * institucional.
     *
     * @param alumnoId ID del alumno
     * @param cursoId ID del curso
     * @param rutaDestino Ruta donde guardar el archivo
     * @return true si la exportación fue exitosa
     */
    public static boolean exportarBoletinAlumno(int alumnoId, int cursoId, String rutaDestino) {
    // DEPRECADO - Ahora usar el método automático del servidor
    System.out.println("⚠️ Método deprecado. Usando generación automática en servidor...");
    
    // Extraer período de la ruta de destino si es posible, sino usar actual
    String periodo = BoletinesUtils.obtenerPeriodoActual();
    
    return PlantillaBoletinUtility.generarBoletinIndividualEnServidor(alumnoId, cursoId, periodo);
}

    /**
     * Exporta boletines para todos los alumnos de un curso usando la plantilla
     * institucional.
     *
     * @param cursoId ID del curso
     * @param carpetaDestino Carpeta donde guardar los archivos
     * @return Número de boletines exportados exitosamente
     */
    public static int exportarBoletinesCurso(int cursoId, String carpetaDestino) {
    // DEPRECADO - Ahora usar el método automático del servidor
    System.out.println("⚠️ Método deprecado. Usando generación automática en servidor...");
    
    String periodo = BoletinesUtils.obtenerPeriodoActual();
    
    return PlantillaBoletinUtility.generarBoletinesCursoEnServidor(cursoId, periodo);
}

    /**
     * Método público para exportar boletines con interfaz de usuario.
     *
     * ACTUALIZADO: Utiliza la nueva implementación basada en plantilla.
     */
   public static void exportarBoletinesConInterfaz(int cursoId, javax.swing.JComponent parentComponent) {
    try {
        System.out.println("=== EXPORTACIÓN CON INTERFAZ - SERVIDOR AUTOMÁTICO ===");
        
        // Solicitar período al usuario
        String[] periodos = {"1B", "2B", "3B", "4B", "1C", "2C", "Final"};
        String periodoSeleccionado = (String) JOptionPane.showInputDialog(
                parentComponent,
                "Seleccione el período del boletín:",
                "Seleccionar Período",
                JOptionPane.PLAIN_MESSAGE,
                null,
                periodos,
                BoletinesUtils.obtenerPeriodoActual()
        );
        
        if (periodoSeleccionado == null) {
            return; // Usuario canceló
        }
        
        // Confirmar generación
        int confirmacion = JOptionPane.showConfirmDialog(parentComponent,
                "¿Generar boletines para todo el curso?\n" +
                "Período: " + periodoSeleccionado + "\n" +
                "Los boletines se guardarán automáticamente en el servidor.",
                "Confirmar Generación Masiva",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            // Usar el nuevo método con servidor automático
            PlantillaBoletinUtility.generarBoletinesCursoConServidorConInterfaz(
                    cursoId, periodoSeleccionado, parentComponent);
        }
        
    } catch (Exception e) {
        System.err.println("❌ Error en exportarBoletinesConInterfaz: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(parentComponent,
                "Error al iniciar exportación de boletines: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    /**
     * Método de utilidad para exportar un solo boletín con interfaz.
     *
     * ACTUALIZADO: Utiliza la nueva implementación basada en plantilla.
     */
    public static void exportarBoletinIndividualConInterfaz(int alumnoId, int cursoId,
        javax.swing.JComponent parentComponent) {
    try {
        System.out.println("=== EXPORTACIÓN INDIVIDUAL CON INTERFAZ - SERVIDOR AUTOMÁTICO ===");
        
        // Solicitar período al usuario
        String[] periodos = {"1B", "2B", "3B", "4B", "1C", "2C", "Final"};
        String periodoSeleccionado = (String) JOptionPane.showInputDialog(
                parentComponent,
                "Seleccione el período del boletín:",
                "Seleccionar Período",
                JOptionPane.PLAIN_MESSAGE,
                null,
                periodos,
                BoletinesUtils.obtenerPeriodoActual()
        );
        
        if (periodoSeleccionado == null) {
            return; // Usuario canceló
        }
        
        // Usar el nuevo método con servidor automático
        PlantillaBoletinUtility.generarBoletinIndividualConServidorConInterfaz(
                alumnoId, cursoId, periodoSeleccionado, parentComponent);
        
    } catch (Exception e) {
        System.err.println("❌ Error en exportarBoletinIndividualConInterfaz: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(parentComponent,
                "Error al iniciar exportación del boletín: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    // =================================================================
    // MÉTODOS DE COMPATIBILIDAD Y CONFIGURACIÓN
    // =================================================================
    /**
     * Configura la ruta de la plantilla de boletín.
     *
     * @param rutaPlantilla Ruta completa al archivo de plantilla Excel
     */
    public static void configurarPlantillaBoletin(String rutaPlantilla) {
        PlantillaBoletinUtility.configurarRutaPlantilla(rutaPlantilla);
        System.out.println("✅ Plantilla de boletín configurada: " + rutaPlantilla);
    }

    /**
     * Obtiene la ruta actual de la plantilla.
     *
     * @return Ruta de la plantilla configurada
     */
    public static String obtenerRutaPlantilla() {
        return PlantillaBoletinUtility.obtenerRutaPlantilla();
    }

    /**
     * Verifica si la plantilla existe y está disponible.
     *
     * @return true si la plantilla existe y es accesible
     */
    public static boolean verificarPlantilla() {
        String rutaPlantilla = PlantillaBoletinUtility.obtenerRutaPlantilla();
        File plantilla = new File(rutaPlantilla);

        boolean existe = plantilla.exists() && plantilla.canRead();

        if (existe) {
            System.out.println("✅ Plantilla verificada: " + rutaPlantilla);
        } else {
            System.err.println("❌ Plantilla no disponible: " + rutaPlantilla);
        }

        return existe;
    }

    /**
     * Permite configurar la plantilla con interfaz gráfica.
     *
     * @param parentComponent Componente padre para los diálogos
     */
    public static void configurarPlantillaConInterfaz(javax.swing.JComponent parentComponent) {
        PlantillaBoletinUtility.configurarPlantillaConInterfaz(parentComponent);
    }

    /**
     * Muestra información sobre la configuración actual de boletines.
     *
     * @param parentComponent Componente padre para el diálogo
     */
    public static void mostrarInformacionConfiguracion(javax.swing.JComponent parentComponent) {
        String rutaPlantilla = PlantillaBoletinUtility.obtenerRutaPlantilla();
        File plantilla = new File(rutaPlantilla);

        StringBuilder info = new StringBuilder();
        info.append("=== CONFIGURACIÓN DE BOLETINES ===\n\n");
        info.append("Versión: 2.0 (Basada en plantilla institucional)\n");
        info.append("Plantilla configurada: ").append(rutaPlantilla).append("\n");
        info.append("Estado: ").append(plantilla.exists() ? "✅ Disponible" : "❌ No encontrada").append("\n");

        if (plantilla.exists()) {
            info.append("Tamaño: ").append(plantilla.length() / 1024).append(" KB\n");
            info.append("Última modificación: ").append(
                    new java.util.Date(plantilla.lastModified()).toString()).append("\n");
        }

        info.append("\n=== CARACTERÍSTICAS ===\n");
        info.append("• Utiliza plantilla Excel prediseñada\n");
        info.append("• Mantiene formato institucional original\n");
        info.append("• Tipografía: Nunito 10pt\n");
        info.append("• Completa campos automáticamente desde BD\n");
        info.append("• Calcula notas derivadas (cuatrimestres, anual, definitiva)\n");
        info.append("• Incluye materias pendientes e inasistencias\n");

        JOptionPane.showMessageDialog(parentComponent,
                info.toString(),
                "Información de Configuración",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // =================================================================
    // MÉTODOS LEGACY (MANTENER PARA COMPATIBILIDAD)
    // =================================================================
    /**
     * Estructura para almacenar datos del alumno (LEGACY). Mantenido para
     * compatibilidad con código existente.
     *
     * @deprecated Usar PlantillaBoletinUtility.DatosEstudiante en su lugar
     */
    @Deprecated
    public static class DatosAlumno {

        public String apellidoNombre;
        public String dni;
        public String curso;
        public String division;
        public String codigoMiEscuela;
        public java.util.Map<String, NotasMateria> materias;

        public DatosAlumno() {
            this.materias = new java.util.HashMap<>();
        }
    }

    /**
     * Estructura para notas por materia (LEGACY). Mantenido para compatibilidad
     * con código existente.
     *
     * @deprecated Usar PlantillaBoletinUtility.NotasMateria en su lugar
     */
    @Deprecated
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

}
