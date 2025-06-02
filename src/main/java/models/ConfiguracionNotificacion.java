/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.models;


/**
 * Modelo para configuración de notificaciones por usuario
 */
public class ConfiguracionNotificacion {
    private int id;
    private int usuarioId;
    private boolean recibirEmail;
    private boolean recibirPush;
    private boolean soloUrgentes;
    private String horarioInicio;
    private String horarioFin;
    private String diasSemana;
    
    // Constructor por defecto con configuración estándar
    public ConfiguracionNotificacion() {
        this.recibirEmail = true;
        this.recibirPush = true;
        this.soloUrgentes = false;
        this.horarioInicio = "08:00:00";
        this.horarioFin = "18:00:00";
        this.diasSemana = "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES";
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    
    public boolean isRecibirEmail() { return recibirEmail; }
    public void setRecibirEmail(boolean recibirEmail) { this.recibirEmail = recibirEmail; }
    
    public boolean isRecibirPush() { return recibirPush; }
    public void setRecibirPush(boolean recibirPush) { this.recibirPush = recibirPush; }
    
    public boolean isSoloUrgentes() { return soloUrgentes; }
    public void setSoloUrgentes(boolean soloUrgentes) { this.soloUrgentes = soloUrgentes; }
    
    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }
    
    public String getHorarioFin() { return horarioFin; }
    public void setHorarioFin(String horarioFin) { this.horarioFin = horarioFin; }
    
    public String getDiasSemana() { return diasSemana; }
    public void setDiasSemana(String diasSemana) { this.diasSemana = diasSemana; }
}