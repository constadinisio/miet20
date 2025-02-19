/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package users.Profesor;
// También necesitarás un convertidor de notas numéricas a conceptuales
public class ConvertidorNotas {
    public static String numeroAConcepto(double nota) {
        if (nota < 6) return "EP";
        if (nota < 8) return "S";
        return "A";
    }
    
    public static double conceptoANumero(String concepto) {
        switch (concepto) {
            case "EP": return 5.0;
            case "S": return 7.0;
            case "A": return 9.0;
            default: return 0.0;
        }
    }
}