package main.java.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Modelo para grupos personalizados de notificaciones
 * 
 * Representa un grupo creado por un usuario autorizado para organizar
 * destinatarios de notificaciones de forma personalizada.
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class PersonalNotificationGroup {
    
    private int id;
    private String name;
    private String description;
    private int creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;
    private int memberCount;
    private boolean active;
    
    // ========================================
    // CONSTRUCTORES
    // ========================================
    
    /**
     * Constructor por defecto
     */
    public PersonalNotificationGroup() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.memberCount = 0;
    }
    
    /**
     * Constructor con datos básicos
     */
    public PersonalNotificationGroup(String name, String description, int creatorId) {
        this();
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
    }
    
    /**
     * Constructor completo
     */
    public PersonalNotificationGroup(int id, String name, String description, 
                                   int creatorId, LocalDateTime createdAt, 
                                   int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.active = true;
    }
    
    // ========================================
    // GETTERS Y SETTERS
    // ========================================
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================
    
    /**
     * Obtiene el nombre para mostrar en combos y listas
     */
    public String getDisplayName() {
        return String.format("%s (%d miembro%s)", 
                           name, 
                           memberCount, 
                           memberCount == 1 ? "" : "s");
    }
    
    /**
     * Obtiene una descripción corta para tooltips
     */
    public String getShortDescription() {
        if (description == null || description.trim().isEmpty()) {
            return "Sin descripción";
        }
        
        String desc = description.trim();
        if (desc.length() > 50) {
            return desc.substring(0, 47) + "...";
        }
        return desc;
    }
    
    /**
     * Obtiene la fecha de creación formateada
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "Fecha desconocida";
        }
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Obtiene la fecha de último uso formateada
     */
    public String getFormattedLastUsed() {
        if (lastUsed == null) {
            return "Nunca usado";
        }
        return lastUsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Indica si el grupo ha sido usado recientemente (últimos 30 días)
     */
    public boolean isRecentlyUsed() {
        if (lastUsed == null) {
            return false;
        }
        return lastUsed.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Indica si el grupo tiene miembros
     */
    public boolean hasMembers() {
        return memberCount > 0;
    }
    
    /**
     * Obtiene un resumen del grupo para mostrar
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Grupo: ").append(name).append("\n");
        summary.append("Miembros: ").append(memberCount).append("\n");
        summary.append("Creado: ").append(getFormattedCreatedAt()).append("\n");
        
        if (lastUsed != null) {
            summary.append("Último uso: ").append(getFormattedLastUsed()).append("\n");
        }
        
        if (description != null && !description.trim().isEmpty()) {
            summary.append("Descripción: ").append(getShortDescription());
        }
        
        return summary.toString();
    }
    
    /**
     * Valida si el grupo tiene datos mínimos válidos
     */
    public boolean isValid() {
        return name != null && 
               !name.trim().isEmpty() && 
               name.length() <= 100 &&
               creatorId > 0 &&
               (description == null || description.length() <= 500);
    }
    
    // ========================================
    // MÉTODOS ESTÁNDAR
    // ========================================
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        PersonalNotificationGroup that = (PersonalNotificationGroup) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Crea una copia del grupo (útil para edición)
     */
    public PersonalNotificationGroup copy() {
        PersonalNotificationGroup copy = new PersonalNotificationGroup();
        copy.setId(this.id);
        copy.setName(this.name);
        copy.setDescription(this.description);
        copy.setCreatorId(this.creatorId);
        copy.setCreatedAt(this.createdAt);
        copy.setLastUsed(this.lastUsed);
        copy.setMemberCount(this.memberCount);
        copy.setActive(this.active);
        return copy;
    }
}