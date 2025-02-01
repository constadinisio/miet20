/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.util.List;

/**
 *
 * @author nico_
 */
public class EstadisticasAsistencia {
    private int totalPresentes;
    private int totalAusentes;
    private int totalJustificados;
    private int totalTarde;
    private double porcentajeAsistencia;
    
    public EstadisticasAsistencia() {
        this.totalPresentes = 0;
        this.totalAusentes = 0;
        this.totalJustificados = 0;
        this.totalTarde = 0;
        this.porcentajeAsistencia = 0.0;
    }
    
    public void calcularEstadisticas(List<AsistenciaDTO> asistencias) {
        totalPresentes = 0;
        totalAusentes = 0;
        totalJustificados = 0;
        totalTarde = 0;
        
        for(AsistenciaDTO asistencia : asistencias) {
            switch(asistencia.getEstado()) {
                case "P": totalPresentes++; break;
                case "A": totalAusentes++; break;
                case "AP": totalJustificados++; break;
                case "T": totalTarde++; break;
            }
        }
        
        int total = totalPresentes + totalAusentes + totalJustificados + totalTarde;
        if(total > 0) {
            porcentajeAsistencia = (double)(totalPresentes + totalJustificados + totalTarde) / total * 100;
        }
    }
    
    // Getters
    public int getTotalPresentes() { return totalPresentes; }
    public int getTotalAusentes() { return totalAusentes; }
    public int getTotalJustificados() { return totalJustificados; }
    public int getTotalTarde() { return totalTarde; }
    public double getPorcentajeAsistencia() { return porcentajeAsistencia; }
}

