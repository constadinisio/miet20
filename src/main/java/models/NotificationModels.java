package main.java.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.Serializable;

/**
 * =========================================================================
 * MODELOS CONSOLIDADOS DEL SISTEMA DE NOTIFICACIONES
 * =========================================================================
 * 
 * Este archivo consolida todos los modelos de datos del sistema de notificaciones
 * para reducir la cantidad de archivos y mejorar la organización.
 * 
 * CONSOLIDACIÓN DE 6 ARCHIVOS:
 * - Notificacion.java
 * - NotificacionDestinatario.java  
 * - NotificationGroup.java
 * - PersonalNotificationGroup.java
 * - GrupoNotificacion.java
 * - ConfiguracionNotificacion.java
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Consolidado
 * @date 21/07/2025
 */
public class NotificationModels {

    // =========================================================================
    // 1. NOTIFICACION - MODELO PRINCIPAL
    // =========================================================================
    
    /**
     * Modelo principal para una notificación
     */
    public static class Notificacion {
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
        public void setDestinatariosIndividuales(List<Integer> destinatariosIndividuales) { this.destinatariosIndividuales = destinatariosIndividuales; }
        
        public int getRolDestino() { return rolDestino; }
        public void setRolDestino(int rolDestino) { this.rolDestino = rolDestino; }
        
        public int getGrupoDestino() { return grupoDestino; }
        public void setGrupoDestino(int grupoDestino) { this.grupoDestino = grupoDestino; }
        
        public int getGrupoPersonalizadoId() { return grupoPersonalizadoId; }
        public void setGrupoPersonalizadoId(int grupoPersonalizadoId) { this.grupoPersonalizadoId = grupoPersonalizadoId; }
        
        // Métodos de utilidad
        public boolean isVencida() {
            return fechaExpiracion != null && LocalDateTime.now().isAfter(fechaExpiracion);
        }
        
        public boolean isUrgente() {
            return "URGENTE".equalsIgnoreCase(prioridad);
        }
        
        @Override
        public String toString() {
            return String.format("Notificacion[id=%d, titulo='%s', prioridad='%s']", 
                id, titulo, prioridad);
        }
    }

    // =========================================================================
    // 2. NOTIFICACION_DESTINATARIO - NOTIFICACIONES CON INFO DEL DESTINATARIO
    // =========================================================================
    
    /**
     * Modelo para notificación con información del destinatario
     */
    public static class NotificacionDestinatario {
        
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
        public NotificacionDestinatario() {}

        public NotificacionDestinatario(int notificacionId, int destinatarioId) {
            this.notificacionId = notificacionId;
            this.destinatarioId = destinatarioId;
            this.estadoLectura = "NO_LEIDA";
            this.fechaCreacion = LocalDateTime.now();
        }

        // Getters y Setters completos
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getNotificacionId() { return notificacionId; }
        public void setNotificacionId(int notificacionId) { this.notificacionId = notificacionId; }

        public int getDestinatarioId() { return destinatarioId; }
        public void setDestinatarioId(int destinatarioId) { this.destinatarioId = destinatarioId; }

        public int getRemitenteId() { return remitenteId; }
        public void setRemitenteId(int remitenteId) { this.remitenteId = remitenteId; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getContenido() { return contenido; }
        public void setContenido(String contenido) { this.contenido = contenido; }

        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

        public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
        public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

        public String getPrioridad() { return prioridad; }
        public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

        public String getIcono() { return icono; }
        public void setIcono(String icono) { this.icono = icono; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public String getTipoNotificacion() { return tipoNotificacion; }
        public void setTipoNotificacion(String tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public boolean isRequiereConfirmacion() { return requiereConfirmacion; }
        public void setRequiereConfirmacion(boolean requiereConfirmacion) { this.requiereConfirmacion = requiereConfirmacion; }

        public String getRemitenteNombre() { return remitenteNombre; }
        public void setRemitenteNombre(String remitenteNombre) { this.remitenteNombre = remitenteNombre; }

        public String getRemitenteApellido() { return remitenteApellido; }
        public void setRemitenteApellido(String remitenteApellido) { this.remitenteApellido = remitenteApellido; }

        public String getEstadoLectura() { return estadoLectura; }
        public void setEstadoLectura(String estadoLectura) { this.estadoLectura = estadoLectura; }

        public LocalDateTime getFechaLeida() { return fechaLeida; }
        public void setFechaLeida(LocalDateTime fechaLeida) { this.fechaLeida = fechaLeida; }

        public LocalDateTime getFechaConfirmada() { return fechaConfirmada; }
        public void setFechaConfirmada(LocalDateTime fechaConfirmada) { this.fechaConfirmada = fechaConfirmada; }

        // Métodos de utilidad
        public boolean isLeida() {
            return "LEIDA".equals(estadoLectura) || "CONFIRMADA".equals(estadoLectura);
        }

        public boolean isConfirmada() {
            return "CONFIRMADA".equals(estadoLectura);
        }

        public String getRemitenteCompleto() {
            if (remitenteNombre != null && remitenteApellido != null) {
                return remitenteNombre + " " + remitenteApellido;
            }
            return "Usuario " + remitenteId;
        }

        @Override
        public String toString() {
            return String.format("NotificacionDestinatario[id=%d, titulo='%s', estado='%s']", 
                id, titulo, estadoLectura);
        }
    }

    // =========================================================================
    // 3. NOTIFICATION_GROUP - GRUPOS AVANZADOS DE NOTIFICACIONES
    // =========================================================================
    
    /**
     * Modelo que representa un grupo personalizado de usuarios para notificaciones
     */
    public static class NotificationGroup implements Serializable, Cloneable {
        
        private static final long serialVersionUID = 1L;

        // Campos principales
        private int id;
        private String nombre;
        private String descripcion;
        private int creadorId;
        private String nombreCreador;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaModificacion;
        private LocalDateTime fechaUltimoUso;
        private boolean activo;

        // Lista de miembros del grupo
        private List<Integer> miembros;
        private Map<Integer, MiembroGrupo> miembrosDetalle;

        // Metadatos del grupo
        private String categoria;
        private String color;
        private int totalNotificacionesEnviadas;
        private boolean esFavorito;

        // Constructores
        public NotificationGroup() {
            this.miembros = new ArrayList<>();
            this.miembrosDetalle = new HashMap<>();
            this.fechaCreacion = LocalDateTime.now();
            this.activo = true;
            this.totalNotificacionesEnviadas = 0;
            this.esFavorito = false;
        }

        public NotificationGroup(String nombre, String descripcion, int creadorId) {
            this();
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.creadorId = creadorId;
        }

        // Getters y Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public int getCreadorId() { return creadorId; }
        public void setCreadorId(int creadorId) { this.creadorId = creadorId; }

        public String getNombreCreador() { return nombreCreador; }
        public void setNombreCreador(String nombreCreador) { this.nombreCreador = nombreCreador; }

        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

        public LocalDateTime getFechaModificacion() { return fechaModificacion; }
        public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

        public LocalDateTime getFechaUltimoUso() { return fechaUltimoUso; }
        public void setFechaUltimoUso(LocalDateTime fechaUltimoUso) { this.fechaUltimoUso = fechaUltimoUso; }

        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }

        public List<Integer> getMiembros() { return miembros; }
        public void setMiembros(List<Integer> miembros) { this.miembros = miembros; }

        public Map<Integer, MiembroGrupo> getMiembrosDetalle() { return miembrosDetalle; }
        public void setMiembrosDetalle(Map<Integer, MiembroGrupo> miembrosDetalle) { this.miembrosDetalle = miembrosDetalle; }

        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public int getTotalNotificacionesEnviadas() { return totalNotificacionesEnviadas; }
        public void setTotalNotificacionesEnviadas(int totalNotificacionesEnviadas) { this.totalNotificacionesEnviadas = totalNotificacionesEnviadas; }

        public boolean isEsFavorito() { return esFavorito; }
        public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }

        // Métodos de utilidad
        public void addMiembro(int usuarioId) {
            if (!miembros.contains(usuarioId)) {
                miembros.add(usuarioId);
                fechaModificacion = LocalDateTime.now();
            }
        }

        public void removeMiembro(int usuarioId) {
            miembros.remove(Integer.valueOf(usuarioId));
            miembrosDetalle.remove(usuarioId);
            fechaModificacion = LocalDateTime.now();
        }

        public int getCantidadMiembros() {
            return miembros != null ? miembros.size() : 0;
        }

        public void incrementarContadorNotificaciones() {
            this.totalNotificacionesEnviadas++;
            this.fechaUltimoUso = LocalDateTime.now();
        }

        @Override
        public NotificationGroup clone() {
            try {
                NotificationGroup cloned = (NotificationGroup) super.clone();
                cloned.miembros = new ArrayList<>(this.miembros);
                cloned.miembrosDetalle = new HashMap<>(this.miembrosDetalle);
                return cloned;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Error clonando NotificationGroup", e);
            }
        }

        @Override
        public String toString() {
            return String.format("NotificationGroup[id=%d, nombre='%s', miembros=%d]", 
                id, nombre, getCantidadMiembros());
        }

        // Clase interna para detalles de miembro
        public static class MiembroGrupo implements Serializable {
            private int usuarioId;
            private String nombre;
            private String apellido;
            private String rol;
            private LocalDateTime fechaAgregado;

            public MiembroGrupo() {}

            public MiembroGrupo(int usuarioId, String nombre, String apellido, String rol) {
                this.usuarioId = usuarioId;
                this.nombre = nombre;
                this.apellido = apellido;
                this.rol = rol;
                this.fechaAgregado = LocalDateTime.now();
            }

            // Getters y Setters
            public int getUsuarioId() { return usuarioId; }
            public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

            public String getNombre() { return nombre; }
            public void setNombre(String nombre) { this.nombre = nombre; }

            public String getApellido() { return apellido; }
            public void setApellido(String apellido) { this.apellido = apellido; }

            public String getRol() { return rol; }
            public void setRol(String rol) { this.rol = rol; }

            public LocalDateTime getFechaAgregado() { return fechaAgregado; }
            public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }

            public String getNombreCompleto() {
                return nombre + " " + apellido;
            }

            @Override
            public String toString() {
                return String.format("MiembroGrupo[id=%d, nombre='%s %s']", usuarioId, nombre, apellido);
            }
        }
    }

    // =========================================================================
    // 4. PERSONAL_NOTIFICATION_GROUP - GRUPOS PERSONALES SIMPLES
    // =========================================================================
    
    /**
     * Modelo para grupos personalizados de notificaciones (versión simplificada)
     */
    public static class PersonalNotificationGroup {
        
        private int id;
        private String name;
        private String description;
        private int creatorId;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsed;
        private int memberCount;
        private boolean active;
        
        // Constructores
        public PersonalNotificationGroup() {
            this.createdAt = LocalDateTime.now();
            this.active = true;
            this.memberCount = 0;
        }
        
        public PersonalNotificationGroup(String name, String description, int creatorId) {
            this();
            this.name = name;
            this.description = description;
            this.creatorId = creatorId;
        }
        
        // Getters y Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getCreatorId() { return creatorId; }
        public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getLastUsed() { return lastUsed; }
        public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
        
        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        // Métodos de utilidad
        public String getFormattedCreatedDate() {
            return createdAt != null ? 
                createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                "Fecha no disponible";
        }
        
        public String getFormattedLastUsed() {
            return lastUsed != null ? 
                lastUsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                "Nunca usado";
        }
        
        public boolean isRecentlyUsed() {
            if (lastUsed == null) return false;
            return lastUsed.isAfter(LocalDateTime.now().minusDays(7));
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PersonalNotificationGroup that = (PersonalNotificationGroup) obj;
            return id == that.id;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
        
        @Override
        public String toString() {
            return String.format("PersonalNotificationGroup[id=%d, name='%s', members=%d]", 
                id, name, memberCount);
        }
    }

    // =========================================================================
    // 5. GRUPO_NOTIFICACION - GRUPOS BASICOS DEL SISTEMA
    // =========================================================================
    
    /**
     * Modelo para grupos de notificación básicos del sistema
     */
    public static class GrupoNotificacion {
        private int id;
        private String nombre;
        private String descripcion;
        private int creadorId;
        private String tipoGrupo; // CURSO, MATERIA, COMISION, PERSONALIZADO
        private boolean activo;
        private LocalDateTime fechaCreacion;
        private List<Integer> miembros;
        
        // Constructores
        public GrupoNotificacion() {
            this.miembros = new ArrayList<>();
            this.activo = true;
            this.fechaCreacion = LocalDateTime.now();
        }
        
        public GrupoNotificacion(String nombre, String descripcion, int creadorId, String tipoGrupo) {
            this();
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.creadorId = creadorId;
            this.tipoGrupo = tipoGrupo;
        }
        
        // Getters y Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public int getCreadorId() { return creadorId; }
        public void setCreadorId(int creadorId) { this.creadorId = creadorId; }
        
        public String getTipoGrupo() { return tipoGrupo; }
        public void setTipoGrupo(String tipoGrupo) { this.tipoGrupo = tipoGrupo; }
        
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        
        public List<Integer> getMiembros() { return miembros; }
        public void setMiembros(List<Integer> miembros) { this.miembros = miembros; }
        
        // Métodos de utilidad
        public void addMiembro(int usuarioId) {
            if (!miembros.contains(usuarioId)) {
                miembros.add(usuarioId);
            }
        }
        
        public void removeMiembro(int usuarioId) {
            miembros.remove(Integer.valueOf(usuarioId));
        }
        
        public int getCantidadMiembros() {
            return miembros != null ? miembros.size() : 0;
        }
        
        @Override
        public String toString() {
            return String.format("GrupoNotificacion[id=%d, nombre='%s', tipo='%s']", 
                id, nombre, tipoGrupo);
        }
    }

    // =========================================================================
    // 6. CONFIGURACION_NOTIFICACION - PREFERENCIAS DEL USUARIO
    // =========================================================================
    
    /**
     * Modelo para configuración de notificaciones por usuario
     */
    public static class ConfiguracionNotificacion {
        private int id;
        private int usuarioId;
        private boolean recibirEmail;
        private boolean recibirPush;
        private boolean soloUrgentes;
        private String horarioInicio;
        private String horarioFin;
        private String diasSemana;
        
        // Constructor con configuración estándar
        public ConfiguracionNotificacion() {
            this.recibirEmail = true;
            this.recibirPush = true;
            this.soloUrgentes = false;
            this.horarioInicio = "08:00:00";
            this.horarioFin = "18:00:00";
            this.diasSemana = "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES";
        }
        
        public ConfiguracionNotificacion(int usuarioId) {
            this();
            this.usuarioId = usuarioId;
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
        
        // Métodos de utilidad
        public boolean puedeRecibirNotificacionAhora() {
            // Implementar lógica de horarios si es necesario
            return !soloUrgentes || isHorarioPermitido();
        }
        
        private boolean isHorarioPermitido() {
            // Lógica simplificada - en producción implementar validación real
            return true;
        }
        
        public boolean puedeRecibirNotificacion(String prioridad) {
            if (soloUrgentes) {
                return "URGENTE".equalsIgnoreCase(prioridad);
            }
            return true;
        }
        
        @Override
        public String toString() {
            return String.format("ConfiguracionNotificacion[usuarioId=%d, email=%s, push=%s]", 
                usuarioId, recibirEmail, recibirPush);
        }
    }

    // =========================================================================
    // CLASES DE UTILIDAD Y ENUMS
    // =========================================================================
    
    /**
     * Enumeración para tipos de notificación
     */
    public enum TipoNotificacion {
        INDIVIDUAL("Individual"),
        ROL("Por Rol"),
        GRUPO("Por Grupo"),
        BROADCAST("Difusión General");
        
        private final String descripcion;
        
        TipoNotificacion(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Enumeración para prioridades
     */
    public enum Prioridad {
        BAJA("Baja", "#28a745", "info"),
        NORMAL("Normal", "#007bff", "info"),
        ALTA("Alta", "#ffc107", "warning"),
        URGENTE("Urgente", "#dc3545", "error");
        
        private final String descripcion;
        private final String color;
        private final String icono;
        
        Prioridad(String descripcion, String color, String icono) {
            this.descripcion = descripcion;
            this.color = color;
            this.icono = icono;
        }
        
        public String getDescripcion() { return descripcion; }
        public String getColor() { return color; }
        public String getIcono() { return icono; }
    }
    
    /**
     * Enumeración para estados de lectura
     */
    public enum EstadoLectura {
        NO_LEIDA("No Leída"),
        LEIDA("Leída"),
        CONFIRMADA("Confirmada");
        
        private final String descripcion;
        
        EstadoLectura(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }

    // =========================================================================
    // CLASES DTO PARA TRANSFERENCIA DE DATOS
    // =========================================================================
    
    /**
     * DTO para resumen de notificaciones
     */
    public static class NotificationSummary {
        public int id;
        public String titulo;
        public String contenido;
        public LocalDateTime fechaCreacion;
        public String prioridad;
        public String icono;
        public String color;
        public String estadoLectura;
        
        @Override
        public String toString() {
            return String.format("NotificationSummary[id=%d, titulo='%s']", id, titulo);
        }
    }
    
    /**
     * DTO para estadísticas de usuario
     */
    public static class UserNotificationStats {
        public int totalNotificaciones;
        public int noLeidas;
        public int leidas;
        public int confirmadas;
        public int urgentes;
        public LocalDateTime ultimaNotificacion;
        
        @Override
        public String toString() {
            return String.format("UserStats[total=%d, noLeidas=%d, urgentes=%d]", 
                totalNotificaciones, noLeidas, urgentes);
        }
    }
}
