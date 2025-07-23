package main.java.models;

import java.sql.Timestamp;

/**
 * Modelo para representar la configuración de bloques por materia.
 * Define la duración de los bloques y si permite carga múltiple de temas.
 * 
 * @author Sistema ET20
 * @version 2.0
 */
public class ConfiguracionBloque {
    
    private int id;
    private int materiaId;
    private int duracionBloque; // en minutos
    private boolean permiteCargaMultiple;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Campos adicionales para vistas
    private String materiaNombre;
    
    /**
     * Constructor vacío.
     */
    public ConfiguracionBloque() {
        this.duracionBloque = 40; // valor por defecto
        this.permiteCargaMultiple = false;
    }
    
    /**
     * Constructor con parámetros principales.
     */
    public ConfiguracionBloque(int materiaId, int duracionBloque, boolean permiteCargaMultiple) {
        this();
        this.materiaId = materiaId;
        this.duracionBloque = duracionBloque;
        this.permiteCargaMultiple = permiteCargaMultiple;
    }
    
    // Getters y Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getMateriaId() {
        return materiaId;
    }
    
    public void setMateriaId(int materiaId) {
        this.materiaId = materiaId;
    }
    
    public int getDuracionBloque() {
        return duracionBloque;
    }
    
    public void setDuracionBloque(int duracionBloque) {
        this.duracionBloque = duracionBloque;
    }
    
    public boolean isPermiteCargaMultiple() {
        return permiteCargaMultiple;
    }
    
    public void setPermiteCargaMultiple(boolean permiteCargaMultiple) {
        this.permiteCargaMultiple = permiteCargaMultiple;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getMateriaNombre() {
        return materiaNombre;
    }
    
    public void setMateriaNombre(String materiaNombre) {
        this.materiaNombre = materiaNombre;
    }
    
    /**
     * Calcula cuántos bloques se pueden dictar en una cantidad de minutos dada.
     */
    public int calcularBloques(int minutosTotales) {
        if (duracionBloque <= 0) return 0;
        return (int) Math.ceil((double) minutosTotales / duracionBloque);
    }
    
    @Override
    public String toString() {
        return "ConfiguracionBloque{" +
                "id=" + id +
                ", materiaId=" + materiaId +
                ", materiaNombre='" + materiaNombre + '\'' +
                ", duracionBloque=" + duracionBloque +
                ", permiteCargaMultiple=" + permiteCargaMultiple +
                '}';
    }
}
