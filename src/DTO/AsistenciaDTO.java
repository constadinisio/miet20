/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.time.LocalDate;

public class AsistenciaDTO {
    private int alumnoId;
    private String nombreAlumno;
    private LocalDate fecha;
    private String estado;
    private String observaciones;
    
    // Getters
    public int getAlumnoId() {
        return alumnoId;
    }
    
    public String getNombreAlumno() {
        return nombreAlumno;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    // Setters
    public void setAlumnoId(int alumnoId) {
        this.alumnoId = alumnoId;
    }
    
    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}