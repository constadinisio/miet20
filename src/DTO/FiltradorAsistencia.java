/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author nico_
 */
public class FiltradorAsistencia {
    public List<AsistenciaDTO> filtrarPorFecha(List<AsistenciaDTO> asistencias, 
                                              LocalDate inicio, 
                                              LocalDate fin) {
        return asistencias.stream()
            .filter(a -> !a.getFecha().isBefore(inicio) && !a.getFecha().isAfter(fin))
            .collect(Collectors.toList());
    }
    
    public List<AsistenciaDTO> filtrarPorEstado(List<AsistenciaDTO> asistencias, 
                                               String estado) {
        return asistencias.stream()
            .filter(a -> a.getEstado().equals(estado))
            .collect(Collectors.toList());
    }
}

