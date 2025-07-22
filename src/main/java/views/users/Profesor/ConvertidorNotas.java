package main.java.views.users.Profesor;

/**
 * Utilidad para convertir notas entre representaciones numéricas y conceptuales.
 * 
 * Proporciona métodos estáticos para transformar calificaciones 
 * entre escalas numéricas y conceptuales.
 * 
 * Escala de conversión:
 * - Menos de 6: "En Proceso"
 * - Entre 6 y 8: "Suficiente"
 * - 8 o más: "Avanzado"
 * 
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [3/12/2025]
 */
public class ConvertidorNotas {
    /**
     * Convierte una nota numérica a su representación conceptual.
     * 
     * @param nota Calificación numérica
     * @return Concepto correspondiente:
     *         - "En Proceso" para notas menores a 6
     *         - "Suficiente" para notas entre 6 y 8
     *         - "Avanzado" para notas de 8 o más
     */
    public static String numeroAConcepto(double nota) {
        if (nota < 6) return "En Proceso";
        if (nota < 8) return "Suficiente";
        return "Avanzado";
    }
    
    /**
     * Convierte un concepto a su valor numérico representativo.
     * 
     * @param concepto Calificación conceptual
     * @return Valor numérico correspondiente:
     *         - 5.0 para "En Proceso"
     *         - 7.0 para "Suficiente"
     *         - 9.0 para "Avanzado"
     *         - 0.0 para conceptos no reconocidos
     */
    public static double conceptoANumero(String concepto) {
        switch (concepto) {
            case "En Proceso": return 5.0;
            case "Suficiente": return 7.0;
            case "Avanzado": return 9.0;
            default: return 0.0;
        }
    }
    
    /**
     * Verifica si un período debe usar valoraciones conceptuales en lugar de numéricas.
     * 
     * @param periodo El período de evaluación
     * @return true si debe usar valoraciones conceptuales, false si debe usar numéricas
     */
    public static boolean esPeridodoConceptual(String periodo) {
        return periodo.equals("1er Bimestre") || periodo.equals("3er Bimestre");
    }
    
    /**
     * Obtiene todas las valoraciones conceptuales disponibles.
     * 
     * @return Array con las valoraciones conceptuales
     */
    public static String[] getValoracionesConceptuales() {
        return new String[]{"Avanzado", "Suficiente", "En Proceso"};
    }
}