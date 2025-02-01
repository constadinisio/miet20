/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author nico_
 */
public class ValidadorAsistencia {
    private static final List<String> ESTADOS_VALIDOS = 
        Arrays.asList("P", "A", "T", "AP", "NC");
        
    public boolean validarEstado(String estado) {
        return estado != null && ESTADOS_VALIDOS.contains(estado);
    }
    
    public boolean validarFecha(LocalDate fecha) {
        return fecha != null && !fecha.isAfter(LocalDate.now());
    }
    
    public boolean validarHorario(LocalTime hora, List<HorarioProfesor> horarios) {
        if (hora == null || horarios == null || horarios.isEmpty()) {
            return false;
        }
        
        return horarios.stream()
            .anyMatch(h -> !hora.isBefore(h.getHoraInicio()) && 
                          !hora.isAfter(h.getHoraFin()));
    }
}
