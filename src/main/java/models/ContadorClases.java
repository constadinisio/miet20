package main.java.models;

import java.sql.Timestamp;

/**
 * Modelo para representar el contador de clases por materia, curso y profesor.
 * Mantiene el seguimiento del número de clases dictadas y la última clase registrada.
 * 
 * @author Sistema ET20
 * @version 2.0
 */
public class ContadorClases {
    
    private int id;
    private int profesorId;
    private int cursoId;
    private int materiaId;
    private int anioLectivo;
    private int totalClases;
    private int ultimaClase;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Campos adicionales para vistas
    private String profesorNombre;
    private String profesorApellido;
    private String materiaNombre;
    private int cursoAnio;
    private int cursoDivision;
    private String cursoTurno;
    
    /**
     * Constructor vacío.
     */
    public ContadorClases() {
        this.totalClases = 0;
        this.ultimaClase = 0;
    }
    
    /**
     * Constructor con parámetros principales.
     */
    public ContadorClases(int profesorId, int cursoId, int materiaId, int anioLectivo) {
        this();
        this.profesorId = profesorId;
        this.cursoId = cursoId;
        this.materiaId = materiaId;
        this.anioLectivo = anioLectivo;
    }
    
    // Getters y Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getProfesorId() {
        return profesorId;
    }
    
    public void setProfesorId(int profesorId) {
        this.profesorId = profesorId;
    }
    
    public int getCursoId() {
        return cursoId;
    }
    
    public void setCursoId(int cursoId) {
        this.cursoId = cursoId;
    }
    
    public int getMateriaId() {
        return materiaId;
    }
    
    public void setMateriaId(int materiaId) {
        this.materiaId = materiaId;
    }
    
    public int getAnioLectivo() {
        return anioLectivo;
    }
    
    public void setAnioLectivo(int anioLectivo) {
        this.anioLectivo = anioLectivo;
    }
    
    public int getTotalClases() {
        return totalClases;
    }
    
    public void setTotalClases(int totalClases) {
        this.totalClases = totalClases;
    }
    
    public int getUltimaClase() {
        return ultimaClase;
    }
    
    public void setUltimaClase(int ultimaClase) {
        this.ultimaClase = ultimaClase;
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
    
    // Campos de vista
    
    public String getProfesorNombre() {
        return profesorNombre;
    }
    
    public void setProfesorNombre(String profesorNombre) {
        this.profesorNombre = profesorNombre;
    }
    
    public String getProfesorApellido() {
        return profesorApellido;
    }
    
    public void setProfesorApellido(String profesorApellido) {
        this.profesorApellido = profesorApellido;
    }
    
    public String getMateriaNombre() {
        return materiaNombre;
    }
    
    public void setMateriaNombre(String materiaNombre) {
        this.materiaNombre = materiaNombre;
    }
    
    public int getCursoAnio() {
        return cursoAnio;
    }
    
    public void setCursoAnio(int cursoAnio) {
        this.cursoAnio = cursoAnio;
    }
    
    public int getCursoDivision() {
        return cursoDivision;
    }
    
    public void setCursoDivision(int cursoDivision) {
        this.cursoDivision = cursoDivision;
    }
    
    public String getCursoTurno() {
        return cursoTurno;
    }
    
    public void setCursoTurno(String cursoTurno) {
        this.cursoTurno = cursoTurno;
    }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el nombre completo del profesor.
     */
    public String getProfesorNombreCompleto() {
        if (profesorNombre != null && profesorApellido != null) {
            return profesorApellido + ", " + profesorNombre;
        }
        return "";
    }
    
    /**
     * Obtiene la descripción del curso completa.
     */
    public String getCursoCompleto() {
        return cursoAnio + "° " + cursoDivision + "° " + (cursoTurno != null ? cursoTurno : "");
    }
    
    /**
     * Calcula el rango de la próxima clase/clases basado en la cantidad de bloques.
     */
    public String calcularProximoRango(int cantidadBloques) {
        int desde = ultimaClase + 1;
        int hasta = ultimaClase + cantidadBloques;
        
        if (desde == hasta) {
            return "Clase " + desde;
        } else {
            return "Clases " + desde + " a " + hasta;
        }
    }
    
    /**
     * Actualiza el contador después de agregar nuevas clases.
     */
    public void agregarClases(int cantidadClases) {
        this.ultimaClase += cantidadClases;
        this.totalClases += cantidadClases;
    }
    
    @Override
    public String toString() {
        return "ContadorClases{" +
                "id=" + id +
                ", profesorId=" + profesorId +
                ", cursoId=" + cursoId +
                ", materiaId=" + materiaId +
                ", anioLectivo=" + anioLectivo +
                ", totalClases=" + totalClases +
                ", ultimaClase=" + ultimaClase +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContadorClases that = (ContadorClases) obj;
        return profesorId == that.profesorId && 
               cursoId == that.cursoId && 
               materiaId == that.materiaId && 
               anioLectivo == that.anioLectivo;
    }
    
    @Override
    public int hashCode() {
        int result = profesorId;
        result = 31 * result + cursoId;
        result = 31 * result + materiaId;
        result = 31 * result + anioLectivo;
        return result;
    }
}
