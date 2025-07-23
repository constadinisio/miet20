package main.java.models;

/**
 * Modelo para representar una Materia
 */
public class Materia {
    private int id;
    private String nombre;
    private int categoriaId;
    private String descripcion;
    
    public Materia() {}
    
    public Materia(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
    public Materia(int id, String nombre, int categoriaId, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.categoriaId = categoriaId;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public int getCategoriaId() {
        return categoriaId;
    }
    
    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Materia materia = (Materia) obj;
        return id == materia.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
