package main.java.tickets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo para representar un ticket de soporte/reporte
 * Compatible con la tabla tickets de la base de datos
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class Ticket {
    
    // Campos principales
    private int id;
    private String ticketNumber;
    private String asunto;
    private String descripcion;
    private String categoria;
    private String prioridad;
    private String estado;
    private int usuarioReportaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaRespuesta;
    private String archivoAdjuntoUrl;
    private String respuestaDeveloper;
    private boolean activo;
    
    // Campos adicionales para la UI (no están en BD)
    private String nombreUsuario;
    private String apellidoUsuario;
    private String nombreCompleto;
    
    // Constantes para validación
    public static final String[] CATEGORIAS_VALIDAS = {"ERROR", "MEJORA", "CONSULTA", "SUGERENCIA"};
    public static final String[] PRIORIDADES_VALIDAS = {"BAJA", "NORMAL", "ALTA", "URGENTE"};
    public static final String[] ESTADOS_VALIDOS = {"ABIERTO", "EN_REVISION", "RESUELTO", "CERRADO"};
    
    // Constructores
    public Ticket() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "ABIERTO";
        this.activo = true;
    }
    
    public Ticket(String asunto, String descripcion, String categoria, String prioridad, int usuarioReportaId) {
        this();
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.usuarioReportaId = usuarioReportaId;
    }
    
    // =====================================
    // GETTERS Y SETTERS PRINCIPALES
    // =====================================
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTicketNumber() {
        return ticketNumber;
    }
    
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    public String getAsunto() {
        return asunto;
    }
    
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public String getPrioridad() {
        return prioridad;
    }
    
    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public int getUsuarioReportaId() {
        return usuarioReportaId;
    }
    
    public void setUsuarioReportaId(int usuarioReportaId) {
        this.usuarioReportaId = usuarioReportaId;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }
    
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
    
    public String getArchivoAdjuntoUrl() {
        return archivoAdjuntoUrl;
    }
    
    public void setArchivoAdjuntoUrl(String archivoAdjuntoUrl) {
        this.archivoAdjuntoUrl = archivoAdjuntoUrl;
    }
    
    public String getRespuestaDeveloper() {
        return respuestaDeveloper;
    }
    
    public void setRespuestaDeveloper(String respuestaDeveloper) {
        this.respuestaDeveloper = respuestaDeveloper;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    // =====================================
    // CAMPOS ADICIONALES PARA UI
    // =====================================
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public String getApellidoUsuario() {
        return apellidoUsuario;
    }
    
    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }
    
    public String getNombreCompleto() {
        if (nombreCompleto != null) {
            return nombreCompleto;
        }
        
        if (apellidoUsuario != null && nombreUsuario != null) {
            return apellidoUsuario + ", " + nombreUsuario;
        }
        
        return "Usuario #" + usuarioReportaId;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    // =====================================
    // MÉTODOS DE UTILIDAD Y VALIDACIÓN
    // =====================================
    
    /**
     * Indica si el ticket está abierto (pendiente de revisión)
     */
    public boolean isAbierto() {
        return "ABIERTO".equals(estado) || "EN_REVISION".equals(estado);
    }
    
    /**
     * Indica si el ticket está cerrado
     */
    public boolean isCerrado() {
        return "RESUELTO".equals(estado) || "CERRADO".equals(estado);
    }
    
    /**
     * Indica si el ticket es urgente
     */
    public boolean isUrgente() {
        return "URGENTE".equals(prioridad);
    }
    
    /**
     * Indica si el ticket tiene un archivo adjunto
     */
    public boolean tieneArchivo() {
        return archivoAdjuntoUrl != null && !archivoAdjuntoUrl.trim().isEmpty();
    }
    
    /**
     * Indica si el ticket tiene respuesta del desarrollador
     */
    public boolean tieneRespuesta() {
        return respuestaDeveloper != null && !respuestaDeveloper.trim().isEmpty();
    }
    
    /**
     * Obtiene la edad del ticket en días
     */
    public long getEdadEnDias() {
        if (fechaCreacion == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(fechaCreacion, LocalDateTime.now());
    }
    
    /**
     * Obtiene el color asociado a la prioridad
     */
    public String getColorPrioridad() {
        if (prioridad == null) {
            return "#6c757d"; // Gris para null
        }
        
        switch (prioridad) {
            case "URGENTE":
                return "#dc3545"; // Rojo
            case "ALTA":
                return "#fd7e14"; // Naranja
            case "NORMAL":
                return "#28a745"; // Verde
            case "BAJA":
                return "#6c757d"; // Gris
            default:
                return "#007bff"; // Azul por defecto
        }
    }
    
    /**
     * Obtiene el color asociado al estado
     */
    public String getColorEstado() {
        if (estado == null) {
            return "#6c757d";
        }
        
        switch (estado) {
            case "ABIERTO":
                return "#dc3545"; // Rojo
            case "EN_REVISION":
                return "#ffc107"; // Amarillo
            case "RESUELTO":
                return "#28a745"; // Verde
            case "CERRADO":
                return "#6c757d"; // Gris
            default:
                return "#007bff"; // Azul por defecto
        }
    }
    
    /**
     * Obtiene el ícono asociado a la categoría
     */
    public String getIconoCategoria() {
        if (categoria == null) {
            return "📄";
        }
        
        switch (categoria) {
            case "ERROR":
                return "🐛";
            case "MEJORA":
                return "✨";
            case "CONSULTA":
                return "❓";
            case "SUGERENCIA":
                return "💡";
            default:
                return "📄";
        }
    }
    
    /**
     * Obtiene el ícono asociado a la prioridad
     */
    public String getIconoPrioridad() {
        if (prioridad == null) {
            return "📌";
        }
        
        switch (prioridad) {
            case "URGENTE":
                return "🚨";
            case "ALTA":
                return "⚠️";
            case "NORMAL":
                return "📌";
            case "BAJA":
                return "🔽";
            default:
                return "📌";
        }
    }
    
    /**
     * Valida si los datos del ticket son válidos
     */
    public boolean esValido() {
        if (asunto == null || asunto.trim().length() < 5) {
            return false;
        }
        
        if (descripcion == null || descripcion.trim().length() < 10) {
            return false;
        }
        
        if (!esCategoriaValida(categoria)) {
            return false;
        }
        
        if (!esPrioridadValida(prioridad)) {
            return false;
        }
        
        if (usuarioReportaId <= 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida si una categoría es válida
     */
    public static boolean esCategoriaValida(String categoria) {
        if (categoria == null) return false;
        for (String cat : CATEGORIAS_VALIDAS) {
            if (cat.equals(categoria)) return true;
        }
        return false;
    }
    
    /**
     * Valida si una prioridad es válida
     */
    public static boolean esPrioridadValida(String prioridad) {
        if (prioridad == null) return false;
        for (String prio : PRIORIDADES_VALIDAS) {
            if (prio.equals(prioridad)) return true;
        }
        return false;
    }
    
    /**
     * Valida si un estado es válido
     */
    public static boolean esEstadoValido(String estado) {
        if (estado == null) return false;
        for (String est : ESTADOS_VALIDOS) {
            if (est.equals(estado)) return true;
        }
        return false;
    }
    
    /**
     * Obtiene el resumen del ticket para mostrar en listas
     */
    public String getResumen() {
        String desc = descripcion != null ? descripcion : "";
        if (desc.length() > 100) {
            desc = desc.substring(0, 97) + "...";
        }
        return desc;
    }
    
    /**
     * Obtiene información formateada de fechas
     */
    public String getFechaCreacionFormateada() {
        if (fechaCreacion == null) return "N/A";
        return fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public String getFechaActualizacionFormateada() {
        if (fechaActualizacion == null) return "N/A";
        return fechaActualizacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public String getFechaRespuestaFormateada() {
        if (fechaRespuesta == null) return "Sin respuesta";
        return fechaRespuesta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Obtiene tiempo transcurrido desde la creación en formato legible
     */
    public String getTiempoTranscurrido() {
        if (fechaCreacion == null) return "Desconocido";
        
        LocalDateTime ahora = LocalDateTime.now();
        long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaCreacion, ahora);
        long horas = java.time.temporal.ChronoUnit.HOURS.between(fechaCreacion, ahora);
        long minutos = java.time.temporal.ChronoUnit.MINUTES.between(fechaCreacion, ahora);
        
        if (dias > 0) {
            return dias + " día" + (dias == 1 ? "" : "s");
        } else if (horas > 0) {
            return horas + " hora" + (horas == 1 ? "" : "s");
        } else if (minutos > 0) {
            return minutos + " minuto" + (minutos == 1 ? "" : "s");
        } else {
            return "Recién creado";
        }
    }
    
    // =====================================
    // MÉTODOS EQUALS, HASHCODE Y TOSTRING
    // =====================================
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Ticket ticket = (Ticket) obj;
        return id == ticket.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    
    @Override
    public String toString() {
        return String.format("Ticket{id=%d, numero='%s', asunto='%s', estado='%s', prioridad='%s'}",
                id, ticketNumber, asunto, estado, prioridad);
    }
    
    /**
     * Obtiene información detallada del ticket para debug
     */
    public String getDebugInfo() {
        return String.format(
                "Ticket Debug Info:\n" +
                "  ID: %d\n" +
                "  Número: %s\n" +
                "  Asunto: %s\n" +
                "  Categoría: %s\n" +
                "  Prioridad: %s\n" +
                "  Estado: %s\n" +
                "  Usuario: %d (%s)\n" +
                "  Creado: %s\n" +
                "  Actualizado: %s\n" +
                "  Tiene archivo: %s\n" +
                "  Tiene respuesta: %s\n" +
                "  Edad: %d días",
                id, ticketNumber, asunto, categoria, prioridad, estado,
                usuarioReportaId, getNombreCompleto(),
                getFechaCreacionFormateada(), getFechaActualizacionFormateada(),
                tieneArchivo() ? "Sí" : "No",
                tieneRespuesta() ? "Sí" : "No",
                getEdadEnDias()
        );
    }
    
    /**
     * Crea una copia del ticket
     */
    public Ticket copy() {
        Ticket copia = new Ticket();
        copia.id = this.id;
        copia.ticketNumber = this.ticketNumber;
        copia.asunto = this.asunto;
        copia.descripcion = this.descripcion;
        copia.categoria = this.categoria;
        copia.prioridad = this.prioridad;
        copia.estado = this.estado;
        copia.usuarioReportaId = this.usuarioReportaId;
        copia.fechaCreacion = this.fechaCreacion;
        copia.fechaActualizacion = this.fechaActualizacion;
        copia.fechaRespuesta = this.fechaRespuesta;
        copia.archivoAdjuntoUrl = this.archivoAdjuntoUrl;
        copia.respuestaDeveloper = this.respuestaDeveloper;
        copia.activo = this.activo;
        copia.nombreUsuario = this.nombreUsuario;
        copia.apellidoUsuario = this.apellidoUsuario;
        copia.nombreCompleto = this.nombreCompleto;
        return copia;
    }
}