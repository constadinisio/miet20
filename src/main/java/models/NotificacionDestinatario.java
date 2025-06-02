/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.models;

import java.time.LocalDateTime;

/**
 * Modelo para notificación con información del destinatario CORREGIDO: Campos
 * adicionales para compatibilidad con BD y componentes
 */
public class NotificacionDestinatario {

    // IDs principales
    private int id;                    // ID del registro en notificaciones_destinatarios
    private int notificacionId;       // ID de la notificación principal
    private int destinatarioId;       // ID del usuario destinatario
    private int remitenteId;          // ID del usuario remitente

    // Datos de la notificación
    private String titulo;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private String prioridad;
    private String icono;
    private String color;
    private String tipoNotificacion;  // INDIVIDUAL, ROL, GRUPO
    private String estado;            // ACTIVA, ARCHIVADA, ELIMINADA
    private boolean requiereConfirmacion;

    // Datos del remitente
    private String remitenteNombre;
    private String remitenteApellido;

    // Datos del destinatario
    private String estadoLectura;     // NO_LEIDA, LEIDA, CONFIRMADA
    private LocalDateTime fechaLeida;
    private LocalDateTime fechaConfirmada;

    // Constructores
    public NotificacionDestinatario() {
    }

    public NotificacionDestinatario(int notificacionId, int destinatarioId) {
        this.notificacionId = notificacionId;
        this.destinatarioId = destinatarioId;
        this.estadoLectura = "NO_LEIDA";
        this.fechaCreacion = LocalDateTime.now();
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

    public int getNotificacionId() {
        return notificacionId;
    }

    public void setNotificacionId(int notificacionId) {
        this.notificacionId = notificacionId;
    }

    public int getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(int destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public int getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(int remitenteId) {
        this.remitenteId = remitenteId;
    }

    // =====================================
    // DATOS DE LA NOTIFICACIÓN
    // =====================================
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(String tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isRequiereConfirmacion() {
        return requiereConfirmacion;
    }

    public void setRequiereConfirmacion(boolean requiereConfirmacion) {
        this.requiereConfirmacion = requiereConfirmacion;
    }

    // =====================================
    // DATOS DEL REMITENTE
    // =====================================
    public String getRemitenteNombre() {
        return remitenteNombre;
    }

    public void setRemitenteNombre(String remitenteNombre) {
        this.remitenteNombre = remitenteNombre;
    }

    public String getRemitenteApellido() {
        return remitenteApellido;
    }

    public void setRemitenteApellido(String remitenteApellido) {
        this.remitenteApellido = remitenteApellido;
    }

    /**
     * Retorna el nombre completo del remitente
     */
    public String getRemitenteNombreCompleto() {
        if (remitenteNombre == null) {
            return "Usuario desconocido";
        }

        String nombreCompleto = remitenteNombre;
        if (remitenteApellido != null && !remitenteApellido.trim().isEmpty()) {
            nombreCompleto += " " + remitenteApellido;
        }
        return nombreCompleto.trim();
    }

    // =====================================
    // DATOS DEL DESTINATARIO
    // =====================================
    public String getEstadoLectura() {
        return estadoLectura;
    }

    public void setEstadoLectura(String estadoLectura) {
        this.estadoLectura = estadoLectura;
    }

    public LocalDateTime getFechaLeida() {
        return fechaLeida;
    }

    public void setFechaLeida(LocalDateTime fechaLeida) {
        this.fechaLeida = fechaLeida;
    }

    public LocalDateTime getFechaConfirmada() {
        return fechaConfirmada;
    }

    public void setFechaConfirmada(LocalDateTime fechaConfirmada) {
        this.fechaConfirmada = fechaConfirmada;
    }

    // =====================================
    // MÉTODOS DE ESTADO Y UTILIDAD
    // =====================================
    /**
     * Indica si la notificación está leída
     */
    public boolean isLeida() {
        return "LEIDA".equals(estadoLectura) || "CONFIRMADA".equals(estadoLectura);
    }

    /**
     * Indica si la notificación está confirmada
     */
    public boolean isConfirmada() {
        return "CONFIRMADA".equals(estadoLectura);
    }

    /**
     * Indica si la notificación no está leída
     */
    public boolean isNoLeida() {
        return "NO_LEIDA".equals(estadoLectura);
    }

    /**
     * Indica si la notificación está activa y no expirada
     */
    public boolean isActiva() {
        if (!"ACTIVA".equals(estado)) {
            return false;
        }

        if (fechaExpiracion != null) {
            return LocalDateTime.now().isBefore(fechaExpiracion);
        }

        return true;
    }

    /**
     * Indica si la notificación ha expirado
     */
    public boolean isExpirada() {
        return fechaExpiracion != null && LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Retorna el color según la prioridad CORREGIDO: Colores actualizados para
     * mejor visibilidad
     */
    public String getColorPorPrioridad() {
        if (color != null && !color.isEmpty()) {
            return color;
        }

        if (prioridad == null) {
            return "#007bff"; // Azul por defecto
        }

        switch (prioridad) {
            case "URGENTE":
                return "#dc3545"; // Rojo
            case "ALTA":
                return "#fd7e14"; // Naranja
            case "NORMAL":
                return "#007bff"; // Azul
            case "BAJA":
                return "#6c757d"; // Gris
            default:
                return "#007bff"; // Azul por defecto
        }
    }

    /**
     * Retorna el ícono según el tipo o prioridad CORREGIDO: Íconos consistentes
     * con los componentes UI
     */
    public String getIconoPorDefecto() {
        if (icono != null && !icono.isEmpty()) {
            return icono;
        }

        if (prioridad == null) {
            return "🔔"; // Campana por defecto
        }

        switch (prioridad) {
            case "URGENTE":
                return "🚨"; // Sirena de emergencia
            case "ALTA":
                return "⚠️";  // Advertencia
            case "NORMAL":
                return "ℹ️";  // Información
            case "BAJA":
                return "📝"; // Nota
            default:
                return "🔔"; // Campana por defecto
        }
    }

    /**
     * Retorna una descripción corta de la prioridad
     */
    public String getDescripcionPrioridad() {
        if (prioridad == null) {
            return "Normal";
        }

        switch (prioridad) {
            case "URGENTE":
                return "Urgente";
            case "ALTA":
                return "Alta";
            case "NORMAL":
                return "Normal";
            case "BAJA":
                return "Baja";
            default:
                return "Normal";
        }
    }

    /**
     * Retorna una versión resumida del contenido
     */
    public String getContenidoResumido(int maxLength) {
        if (contenido == null) {
            return "";
        }

        if (contenido.length() <= maxLength) {
            return contenido;
        }

        return contenido.substring(0, maxLength - 3) + "...";
    }

    /**
     * Retorna información de debug del objeto
     */
    public String getDebugInfo() {
        return String.format(
                "NotificacionDestinatario[id=%d, notifId=%d, destId=%d, titulo='%s', prioridad=%s, estado=%s, estadoLectura=%s]",
                id, notificacionId, destinatarioId, titulo, prioridad, estado, estadoLectura
        );
    }

    // =====================================
    // MÉTODOS EQUALS, HASHCODE Y TOSTRING
    // =====================================
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        NotificacionDestinatario that = (NotificacionDestinatario) obj;
        return id == that.id
                && notificacionId == that.notificacionId
                && destinatarioId == that.destinatarioId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, notificacionId, destinatarioId);
    }

    @Override
    public String toString() {
        return String.format("NotificacionDestinatario{id=%d, titulo='%s', prioridad=%s, estadoLectura=%s}",
                id, titulo, prioridad, estadoLectura);
    }
}
