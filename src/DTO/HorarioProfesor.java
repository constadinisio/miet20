/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.time.*;

public class HorarioProfesor {
    private int materiaId;
    private int cursoId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    public HorarioProfesor(int materiaId, int cursoId, DayOfWeek diaSemana, 
                          LocalTime horaInicio, LocalTime horaFin) {
        this.materiaId = materiaId;
        this.cursoId = cursoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }
    
    // Getters
    public int getMateriaId() { return materiaId; }
    public int getCursoId() { return cursoId; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    
    // Setters
    public void setMateriaId(int materiaId) { this.materiaId = materiaId; }
    public void setCursoId(int cursoId) { this.cursoId = cursoId; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}