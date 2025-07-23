package main.java.models;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Modelo para representar un tema diario en el libro de temas.
 * Incluye funcionalidades mejoradas como numeración automática de clases,
 * validación y soporte para diferentes modos de carga.
 * 
 * @author Sistema ET20
 * @version 2.0
 */
public class TemaDiario {
    
    private int id;
    private int libroId;
    private int profesorId;
    private int cursoId;
    private int materiaId;
    private LocalDate fechaClase;
    private Timestamp fechaCarga;
    private int claseDesde;
    private int claseHasta;
    private String tema;
    private String actividadesDesarrolladas;  // Nuevo campo para actividades
    private String observaciones;
    private String caracterClase;  // Carácter de la clase (Diagnóstica, Explicativa, etc.)
    private boolean validado;
    private Timestamp fechaValidacion;
    private Integer validadoPor;
    private ModoCarga modoCarga;
    private Integer bloqueNumero;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Campos adicionales para vistas
    private String profesorNombre;
    private String profesorApellido;
    private String materiaNombre;
    private int cursoAnio;
    private int cursoDivision;
    private String cursoTurno;
    private String validadoPorNombre;
    private String diaSemana;
    private String rangoClases;
    private String estadoVisual;
    
    /**
     * Enumeración para los modos de carga de temas.
     */
    public enum ModoCarga {
        SIMPLE,   // Un tema por jornada completa
        MULTIPLE  // Un tema por cada bloque de 40 minutos
    }
    
    /**
     * Constructor vacío.
     */
    public TemaDiario() {
        this.modoCarga = ModoCarga.SIMPLE;
        this.validado = false;
    }
    
    /**
     * Constructor completo para crear un nuevo tema diario.
     */
    public TemaDiario(int libroId, int profesorId, int cursoId, int materiaId, 
                     LocalDate fechaClase, int claseDesde, int claseHasta, 
                     String tema, String observaciones, ModoCarga modoCarga) {
        this();
        this.libroId = libroId;
        this.profesorId = profesorId;
        this.cursoId = cursoId;
        this.materiaId = materiaId;
        this.fechaClase = fechaClase;
        this.claseDesde = claseDesde;
        this.claseHasta = claseHasta;
        this.tema = tema;
        this.observaciones = observaciones;
        this.modoCarga = modoCarga;
    }
    
    // Getters y Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getLibroId() {
        return libroId;
    }
    
    public void setLibroId(int libroId) {
        this.libroId = libroId;
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
    
    public LocalDate getFechaClase() {
        return fechaClase;
    }
    
    public void setFechaClase(LocalDate fechaClase) {
        this.fechaClase = fechaClase;
    }
    
    public Timestamp getFechaCarga() {
        return fechaCarga;
    }
    
    public void setFechaCarga(Timestamp fechaCarga) {
        this.fechaCarga = fechaCarga;
    }
    
    public int getClaseDesde() {
        return claseDesde;
    }
    
    public void setClaseDesde(int claseDesde) {
        this.claseDesde = claseDesde;
    }
    
    public int getClaseHasta() {
        return claseHasta;
    }
    
    public void setClaseHasta(int claseHasta) {
        this.claseHasta = claseHasta;
    }
    
    public String getTema() {
        return tema;
    }
    
    public void setTema(String tema) {
        this.tema = tema;
    }
    
    public String getActividadesDesarrolladas() {
        return actividadesDesarrolladas;
    }
    
    public void setActividadesDesarrolladas(String actividadesDesarrolladas) {
        this.actividadesDesarrolladas = actividadesDesarrolladas;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public String getCaracterClase() {
        return caracterClase;
    }
    
    public void setCaracterClase(String caracterClase) {
        this.caracterClase = caracterClase;
    }
    
    public boolean isValidado() {
        return validado;
    }
    
    public void setValidado(boolean validado) {
        this.validado = validado;
    }
    
    public Timestamp getFechaValidacion() {
        return fechaValidacion;
    }
    
    public void setFechaValidacion(Timestamp fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }
    
    public Integer getValidadoPor() {
        return validadoPor;
    }
    
    public void setValidadoPor(Integer validadoPor) {
        this.validadoPor = validadoPor;
    }
    
    public ModoCarga getModoCarga() {
        return modoCarga;
    }
    
    public void setModoCarga(ModoCarga modoCarga) {
        this.modoCarga = modoCarga;
    }
    
    public Integer getBloqueNumero() {
        return bloqueNumero;
    }
    
    public void setBloqueNumero(Integer bloqueNumero) {
        this.bloqueNumero = bloqueNumero;
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
    
    public String getValidadoPorNombre() {
        return validadoPorNombre;
    }
    
    public void setValidadoPorNombre(String validadoPorNombre) {
        this.validadoPorNombre = validadoPorNombre;
    }
    
    public String getDiaSemana() {
        return diaSemana;
    }
    
    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }
    
    public String getRangoClases() {
        return rangoClases;
    }
    
    public void setRangoClases(String rangoClases) {
        this.rangoClases = rangoClases;
    }
    
    public String getEstadoVisual() {
        return estadoVisual;
    }
    
    public void setEstadoVisual(String estadoVisual) {
        this.estadoVisual = estadoVisual;
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
     * Calcula la cantidad de clases que representa este tema.
     */
    public int getCantidadClases() {
        return claseHasta - claseDesde + 1;
    }
    
    /**
     * Verifica si el tema puede ser editado (no está validado).
     */
    public boolean puedeEditarse() {
        return !validado;
    }
    
    /**
     * Obtiene una descripción del rango de clases.
     */
    public String getDescripcionClases() {
        if (claseDesde == claseHasta) {
            return "Clase " + claseDesde;
        } else {
            return "Clases " + claseDesde + " a " + claseHasta;
        }
    }
    
    @Override
    public String toString() {
        return "TemaDiario{" +
                "id=" + id +
                ", fechaClase=" + fechaClase +
                ", clases=" + getDescripcionClases() +
                ", tema='" + (tema != null ? tema.substring(0, Math.min(50, tema.length())) + "..." : "") + '\'' +
                ", validado=" + validado +
                ", modoCarga=" + modoCarga +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TemaDiario that = (TemaDiario) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
