package main.java.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo principal para una notificación
 */
public class Notificacion {
    private int id;
    private String titulo;
    private String contenido;
    private String tipoNotificacion; // INDIVIDUAL, ROL, GRUPO
    private int remitenteId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private String prioridad; // BAJA, NORMAL, ALTA, URGENTE
    private String estado; // ACTIVA, ARCHIVADA, ELIMINADA
    private boolean requiereConfirmacion;
    private String icono;
    private String color;
    
    // Para resolución de destinatarios
    private List<Integer> destinatariosIndividuales;
    private int rolDestino;
    private int grupoDestino;
      private int grupoPersonalizadoId;
    
    // Constructores
    public Notificacion() {}
    
    public Notificacion(String titulo, String contenido, String tipoNotificacion, int remitenteId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.tipoNotificacion = tipoNotificacion;
        this.remitenteId = remitenteId;
        this.fechaCreacion = LocalDateTime.now();
        this.prioridad = "NORMAL";
        this.estado = "ACTIVA";
        this.icono = "info";
        this.color = "#007bff";
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public String getTipoNotificacion() { return tipoNotificacion; }
    public void setTipoNotificacion(String tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }
    
    public int getRemitenteId() { return remitenteId; }
    public void setRemitenteId(int remitenteId) { this.remitenteId = remitenteId; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public boolean isRequiereConfirmacion() { return requiereConfirmacion; }
    public void setRequiereConfirmacion(boolean requiereConfirmacion) { this.requiereConfirmacion = requiereConfirmacion; }
    
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public List<Integer> getDestinatariosIndividuales() { return destinatariosIndividuales; }
    public void setDestinatariosIndividuales(List<Integer> destinatariosIndividuales) { 
        this.destinatariosIndividuales = destinatariosIndividuales; 
    }
    
    public int getRolDestino() { return rolDestino; }
    public void setRolDestino(int rolDestino) { this.rolDestino = rolDestino; }
    
    public int getGrupoDestino() { return grupoDestino; }
    public void setGrupoDestino(int grupoDestino) { this.grupoDestino = grupoDestino; }
    
    /**
     * Obtiene el ID del grupo personalizado al que se envía la notificación
     * @return ID del grupo personalizado, 0 si no aplica
     */
    public int getGrupoPersonalizadoId() {
        return grupoPersonalizadoId;
    }
    
    /**
     * Establece el ID del grupo personalizado al que se envía la notificación
     * @param grupoPersonalizadoId ID del grupo personalizado
     */
    public void setGrupoPersonalizadoId(int grupoPersonalizadoId) {
        this.grupoPersonalizadoId = grupoPersonalizadoId;
    }
}

