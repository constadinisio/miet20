package main.java.models;

/**
 * Modelo para representar un Curso
 */
public class Curso {
    private int id;
    private int anio;
    private int division;
    private String turno;
    private String nombre; // Calculado como "1°A" por ejemplo
    
    public Curso() {}
    
    public Curso(int id, int anio, int division, String turno) {
        this.id = id;
        this.anio = anio;
        this.division = division;
        this.turno = turno;
        this.nombre = anio + "°" + division;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAnio() {
        return anio;
    }
    
    public void setAnio(int anio) {
        this.anio = anio;
        this.nombre = anio + "°" + division;
    }
    
    public int getDivision() {
        return division;
    }
    
    public void setDivision(int division) {
        this.division = division;
        this.nombre = anio + "°" + division;
    }
    
    public String getTurno() {
        return turno;
    }
    
    public void setTurno(String turno) {
        this.turno = turno;
    }
    
    public String getNombre() {
        if (nombre == null || nombre.isEmpty()) {
            nombre = anio + "°" + division;
        }
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    @Override
    public String toString() {
        return getNombre() + (turno != null ? " (" + turno + ")" : "");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Curso curso = (Curso) obj;
        return id == curso.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
