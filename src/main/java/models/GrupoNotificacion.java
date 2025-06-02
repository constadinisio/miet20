/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.models;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Modelo para grupos de notificación
 */
public class GrupoNotificacion {
    private int id;
    private String nombre;
    private String descripcion;
    private int creadorId;
    private String tipoGrupo; // CURSO, MATERIA, COMISION, PERSONALIZADO
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private List<Integer> miembros;
    
    // Constructores
    public GrupoNotificacion() {}
    
    public GrupoNotificacion(String nombre, String descripcion, int creadorId, String tipoGrupo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creadorId = creadorId;
        this.tipoGrupo = tipoGrupo;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public int getCreadorId() { return creadorId; }
    public void setCreadorId(int creadorId) { this.creadorId = creadorId; }
    
    public String getTipoGrupo() { return tipoGrupo; }
    public void setTipoGrupo(String tipoGrupo) { this.tipoGrupo = tipoGrupo; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public List<Integer> getMiembros() { return miembros; }
    public void setMiembros(List<Integer> miembros) { this.miembros = miembros; }
}