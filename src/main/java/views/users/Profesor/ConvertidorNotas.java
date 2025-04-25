package main.java.views.users.Profesor;

/**
 * Utilidad para convertir notas entre representaciones numéricas y conceptuales.
 * 
 * Proporciona métodos estáticos para transformar calificaciones 
 * entre escalas numéricas y conceptuales.
 * 
 * Escala de conversión:
 * - Menos de 6: "EP" (En Proceso)
 * - Entre 6 y 8: "S" (Satisfactorio)
 * - 8 o más: "A" (Aprobado)
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
     *         - "EP" para notas menores a 6
     *         - "S" para notas entre 6 y 8
     *         - "A" para notas de 8 o más
     */
    public static String numeroAConcepto(double nota) {
        if (nota < 6) return "EP";
        if (nota < 8) return "S";
        return "A";
    }
    
    /**
     * Convierte un concepto a su valor numérico representativo.
     * 
     * @param concepto Calificación conceptual
     * @return Valor numérico correspondiente:
     *         - 5.0 para "EP"
     *         - 7.0 para "S"
     *         - 9.0 para "A"
     *         - 0.0 para conceptos no reconocidos
     */
    public static double conceptoANumero(String concepto) {
        switch (concepto) {
            case "EP": return 5.0;
            case "S": return 7.0;
            case "A": return 9.0;
            default: return 0.0;
        }
    }
}