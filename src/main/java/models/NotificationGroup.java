package main.java.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.Serializable;

/**
 * Modelo que representa un grupo personalizado de usuarios para notificaciones.
 * Permite a los usuarios crear grupos personalizados para enviar notificaciones
 * a múltiples destinatarios de forma organizada.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class NotificationGroup implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // CAMPOS PRINCIPALES
    // ========================================
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

    // ========================================
    // CONSTRUCTORES
    // ========================================
    /**
     * Constructor por defecto
     */
    public NotificationGroup() {
        this.miembros = new ArrayList<>();
        this.miembrosDetalle = new HashMap<>();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.activo = true;
        this.categoria = "General";
        this.color = "#007bff"; // Azul por defecto
        this.totalNotificacionesEnviadas = 0;
        this.esFavorito = false;
    }

    /**
     * Constructor con datos básicos
     *
     * @param nombre Nombre del grupo
     * @param descripcion Descripción del grupo
     * @param creadorId ID del usuario creador
     */
    public NotificationGroup(String nombre, String descripcion, int creadorId) {
        this();

        // CORREGIDO: Validar parámetros en constructor
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del grupo no puede estar vacío");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre del grupo no puede exceder 100 caracteres");
        }
        if (creadorId <= 0) {
            throw new IllegalArgumentException("ID de creador inválido: " + creadorId);
        }
        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripción no puede exceder 500 caracteres");
        }

        this.nombre = nombre.trim();
        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
        this.creadorId = creadorId;
    }

    /**
     * Constructor completo
     *
     * @param id ID del grupo
     * @param nombre Nombre del grupo
     * @param descripcion Descripción del grupo
     * @param creadorId ID del usuario creador
     * @param miembros Lista de IDs de miembros
     */
    public NotificationGroup(int id, String nombre, String descripcion,
            int creadorId, List<Integer> miembros) {
        this(nombre, descripcion, creadorId);
        this.id = id;
        if (miembros != null) {
            this.miembros = new ArrayList<>(miembros);
        }
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        // CORREGIDO: Validar nombre antes de establecer
        if (!validarNombre(nombre)) {
            throw new IllegalArgumentException("Nombre de grupo inválido: " + nombre);
        }

        this.nombre = nombre.trim();
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        // CORREGIDO: Validar descripción antes de establecer
        if (!validarDescripcion(descripcion)) {
            throw new IllegalArgumentException("Descripción demasiado larga (máximo 500 caracteres)");
        }

        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
        this.fechaModificacion = LocalDateTime.now();
    }

    public int getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(int creadorId) {
        this.creadorId = creadorId;
    }

    public String getNombreCreador() {
        return nombreCreador;
    }

    public void setNombreCreador(String nombreCreador) {
        this.nombreCreador = nombreCreador;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getFechaUltimoUso() {
        return fechaUltimoUso;
    }

    public void setFechaUltimoUso(LocalDateTime fechaUltimoUso) {
        this.fechaUltimoUso = fechaUltimoUso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        this.fechaModificacion = LocalDateTime.now();
    }

    public List<Integer> getMiembros() {
        return new ArrayList<>(miembros); // Retorna copia defensiva
    }

    public void setMiembros(List<Integer> miembros) {
        this.miembros = miembros != null ? new ArrayList<>(miembros) : new ArrayList<>();
        this.fechaModificacion = LocalDateTime.now();

        // Actualizar mapa de detalles
        actualizarMapaMiembros();
    }

    public Map<Integer, MiembroGrupo> getMiembrosDetalle() {
        return new HashMap<>(miembrosDetalle); // Retorna copia defensiva
    }

    public void setMiembrosDetalle(Map<Integer, MiembroGrupo> miembrosDetalle) {
        this.miembrosDetalle = miembrosDetalle != null
                ? new HashMap<>(miembrosDetalle) : new HashMap<>();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        // CORREGIDO: Validar formato de color hexadecimal
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Color debe tener formato hexadecimal #RRGGBB: " + color);
        }

        this.color = (color != null) ? color.toUpperCase() : "#007BFF";
        this.fechaModificacion = LocalDateTime.now();
    }

    public int getTotalNotificacionesEnviadas() {
        return totalNotificacionesEnviadas;
    }

    public void setTotalNotificacionesEnviadas(int totalNotificacionesEnviadas) {
        this.totalNotificacionesEnviadas = totalNotificacionesEnviadas;
    }

    public boolean isEsFavorito() {
        return esFavorito;
    }

    public void setEsFavorito(boolean esFavorito) {
        this.esFavorito = esFavorito;
        this.fechaModificacion = LocalDateTime.now();
    }

    // ========================================
    // MÉTODOS DE GESTIÓN DE MIEMBROS
    // ========================================
    /**
     * Agrega un miembro al grupo
     *
     * @param usuarioId ID del usuario a agregar
     * @return true si se agregó exitosamente, false si ya existía
     */
    public boolean agregarMiembro(int usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido: " + usuarioId);
        }

        // CORREGIDO: Verificar límite de miembros para evitar grupos excesivamente grandes
        if (miembros.size() >= 1000) {
            throw new IllegalStateException("El grupo ha alcanzado el límite máximo de 1000 miembros");
        }

        if (!miembros.contains(usuarioId)) {
            miembros.add(usuarioId);

            // Crear entrada en el mapa de detalles
            MiembroGrupo miembro = new MiembroGrupo(usuarioId);
            miembrosDetalle.put(usuarioId, miembro);

            this.fechaModificacion = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Agrega un miembro con información adicional
     *
     * @param usuarioId ID del usuario
     * @param nombre Nombre del usuario
     * @param rol Rol del usuario
     * @return true si se agregó exitosamente
     */
    public boolean agregarMiembro(int usuarioId, String nombre, String rol) {
        if (agregarMiembro(usuarioId)) {
            MiembroGrupo miembro = miembrosDetalle.get(usuarioId);
            if (miembro != null) {
                miembro.setNombre(nombre);
                miembro.setRol(rol);
            }
            return true;
        }
        return false;
    }

    /**
     * Agrega múltiples miembros al grupo
     *
     * @param usuariosIds Lista de IDs de usuarios a agregar
     * @return Número de miembros agregados exitosamente
     */
    public int agregarMiembros(List<Integer> usuariosIds) {
        if (usuariosIds == null || usuariosIds.isEmpty()) {
            return 0;
        }

        // CORREGIDO: Verificar que no se exceda el límite
        if (miembros.size() + usuariosIds.size() > 1000) {
            throw new IllegalStateException("Agregar estos miembros excedería el límite de 1000 miembros");
        }

        int agregados = 0;
        List<Integer> usuariosValidos = new ArrayList<>();

        // CORREGIDO: Filtrar IDs válidos primero
        for (Integer usuarioId : usuariosIds) {
            if (usuarioId != null && usuarioId > 0 && !miembros.contains(usuarioId)) {
                usuariosValidos.add(usuarioId);
            }
        }

        // Agregar todos los válidos
        for (Integer usuarioId : usuariosValidos) {
            if (agregarMiembro(usuarioId)) {
                agregados++;
            }
        }

        return agregados;
    }

    /**
     * Remueve un miembro del grupo
     *
     * @param usuarioId ID del usuario a remover
     * @return true si se removió exitosamente, false si no existía
     */
    public boolean removerMiembro(int usuarioId) {
        boolean removido = miembros.remove(Integer.valueOf(usuarioId));
        if (removido) {
            miembrosDetalle.remove(usuarioId);
            this.fechaModificacion = LocalDateTime.now();
        }
        return removido;
    }

    /**
     * Remueve múltiples miembros del grupo
     *
     * @param usuariosIds Lista de IDs de usuarios a remover
     * @return Número de miembros removidos exitosamente
     */
    public int removerMiembros(List<Integer> usuariosIds) {
        if (usuariosIds == null || usuariosIds.isEmpty()) {
            return 0;
        }

        int removidos = 0;

        // CORREGIDO: Usar removeAll para mejor eficiencia con listas grandes
        List<Integer> idsARemover = usuariosIds.stream()
                .filter(Objects::nonNull)
                .filter(miembros::contains)
                .collect(java.util.stream.Collectors.toList());

        if (!idsARemover.isEmpty()) {
            miembros.removeAll(idsARemover);

            for (Integer id : idsARemover) {
                miembrosDetalle.remove(id);
                removidos++;
            }

            this.fechaModificacion = LocalDateTime.now();
        }

        return removidos;
    }

    /**
     * Verifica si un usuario es miembro del grupo
     *
     * @param usuarioId ID del usuario a verificar
     * @return true si es miembro, false en caso contrario
     */
    public boolean esMiembro(int usuarioId) {
        return miembros.contains(usuarioId);
    }

    /**
     * Limpia todos los miembros del grupo
     */
    public void limpiarMiembros() {
        miembros.clear();
        miembrosDetalle.clear();
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Obtiene el número de miembros del grupo
     *
     * @return Cantidad de miembros
     */
    public int getCantidadMiembros() {
        return miembros.size();
    }

    /**
     * Verifica si el grupo tiene miembros
     *
     * @return true si tiene miembros, false si está vacío
     */
    public boolean tieneMiembros() {
        return !miembros.isEmpty();
    }

    /**
     * Actualiza el mapa de detalles de miembros cuando cambia la lista
     */
    private void actualizarMapaMiembros() {
        // Remover miembros que ya no están en la lista
        miembrosDetalle.entrySet().removeIf(entry -> !miembros.contains(entry.getKey()));

        // Agregar nuevos miembros al mapa
        for (Integer usuarioId : miembros) {
            if (!miembrosDetalle.containsKey(usuarioId)) {
                miembrosDetalle.put(usuarioId, new MiembroGrupo(usuarioId));
            }
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD Y VALIDACIÓN
    // ========================================
    /**
     * Valida si el grupo tiene datos mínimos válidos
     *
     * @return true si es válido, false en caso contrario
     */
    public boolean isValido() {
        // CORREGIDO: Validaciones más completas
        return validarNombre(this.nombre)
                && validarDescripcion(this.descripcion)
                && this.creadorId > 0
                && this.fechaCreacion != null
                && (this.color == null || this.color.matches("^#[0-9A-Fa-f]{6}$"))
                && (this.categoria == null || this.categoria.length() <= 50)
                && this.totalNotificacionesEnviadas >= 0
                && this.miembros != null
                && this.miembrosDetalle != null;
    }

    /**
     * Registra el uso del grupo (actualiza fecha de último uso)
     */
    public void registrarUso() {
        this.fechaUltimoUso = LocalDateTime.now();
        this.totalNotificacionesEnviadas++;
    }

    /**
     * Verifica si el grupo ha sido usado recientemente (últimos 30 días)
     *
     * @return true si fue usado recientemente, false en caso contrario
     */
    public boolean fueUsadoRecientemente() {
        if (fechaUltimoUso == null) {
            return false;
        }
        return fechaUltimoUso.isAfter(LocalDateTime.now().minusDays(30));
    }

    /**
     * Obtiene un resumen del grupo para mostrar en listas
     *
     * @return String con información resumida
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append(nombre);
        resumen.append(" (").append(getCantidadMiembros()).append(" miembros)");

        if (categoria != null && !categoria.equals("General")) {
            resumen.append(" - ").append(categoria);
        }

        if (fechaUltimoUso != null) {
            resumen.append(" • Último uso: ");
            resumen.append(fechaUltimoUso.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        return resumen.toString();
    }

    /**
     * Obtiene información detallada del grupo
     *
     * @return String con información completa
     */
    public String getInformacionCompleta() {
        StringBuilder info = new StringBuilder();
        info.append("=== GRUPO: ").append(nombre).append(" ===\n");

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            info.append("Descripción: ").append(descripcion).append("\n");
        }

        info.append("Creado: ").append(fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");

        if (fechaModificacion != null && !fechaModificacion.equals(fechaCreacion)) {
            info.append("Modificado: ").append(fechaModificacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }

        info.append("Miembros: ").append(getCantidadMiembros()).append("\n");
        info.append("Categoría: ").append(categoria).append("\n");
        info.append("Notificaciones enviadas: ").append(totalNotificacionesEnviadas).append("\n");
        info.append("Estado: ").append(activo ? "Activo" : "Inactivo").append("\n");

        if (esFavorito) {
            info.append("⭐ Marcado como favorito\n");
        }

        if (fechaUltimoUso != null) {
            info.append("Último uso: ").append(fechaUltimoUso.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }

        return info.toString();
    }

    /**
     * Compara dos grupos por nombre (para ordenamiento)
     *
     * @param other Otro grupo a comparar
     * @return Resultado de la comparación
     */
    public int compararPorNombre(NotificationGroup other) {
        if (other == null) {
            return 1;
        }
        if (this.nombre == null && other.nombre == null) {
            return 0;
        }
        if (this.nombre == null) {
            return -1;
        }
        if (other.nombre == null) {
            return 1;
        }
        return this.nombre.compareToIgnoreCase(other.nombre);
    }

    /**
     * Compara dos grupos por fecha de creación (más reciente primero)
     *
     * @param other Otro grupo a comparar
     * @return Resultado de la comparación
     */
    public int compararPorFecha(NotificationGroup other) {
        if (other == null) {
            return 1;
        }
        if (this.fechaCreacion == null && other.fechaCreacion == null) {
            return 0;
        }
        if (this.fechaCreacion == null) {
            return -1;
        }
        if (other.fechaCreacion == null) {
            return 1;
        }
        return other.fechaCreacion.compareTo(this.fechaCreacion); // Más reciente primero
    }

    /**
     * Compara dos grupos por cantidad de miembros (más miembros primero)
     *
     * @param other Otro grupo a comparar
     * @return Resultado de la comparación
     */
    public int compararPorTamaño(NotificationGroup other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(other.getCantidadMiembros(), this.getCantidadMiembros());
    }

    // ========================================
    // MÉTODOS ESTÁNDAR
    // ========================================
    @Override
    public String toString() {
        return nombre + " (" + getCantidadMiembros() + " miembros)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        NotificationGroup that = (NotificationGroup) obj;
        return id == that.id && Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public NotificationGroup clone() {
        try {
            NotificationGroup cloned = (NotificationGroup) super.clone();

            // Clonar listas y mapas mutables
            cloned.miembros = new ArrayList<>(this.miembros);
            cloned.miembrosDetalle = new HashMap<>();

            for (Map.Entry<Integer, MiembroGrupo> entry : this.miembrosDetalle.entrySet()) {
                cloned.miembrosDetalle.put(entry.getKey(), entry.getValue().clone());
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error clonando NotificationGroup", e);
        }
    }

    // ========================================
    // CLASE INTERNA: MIEMBRO DEL GRUPO
    // ========================================
    /**
     * Clase interna que representa un miembro del grupo con información
     * adicional
     */
    public static class MiembroGrupo implements Serializable, Cloneable {

        private static final long serialVersionUID = 1L;

        private int usuarioId;
        private String nombre;
        private String apellido;
        private String rol;
        private String email;
        private LocalDateTime fechaAgregado;
        private boolean activo;

        public MiembroGrupo(int usuarioId) {
            // CORREGIDO: Validar ID de usuario
            if (usuarioId <= 0) {
                throw new IllegalArgumentException("ID de usuario inválido: " + usuarioId);
            }

            this.usuarioId = usuarioId;
            this.fechaAgregado = LocalDateTime.now();
            this.activo = true;
        }

        /**
         * NUEVO: Método para validar datos del miembro
         */
        public boolean isValid() {
            return this.usuarioId > 0
                    && this.fechaAgregado != null
                    && (this.email == null || this.email.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
        }

        /**
         * NUEVO: Método para obtener información resumida
         */
        public String getResumen() {
            StringBuilder resumen = new StringBuilder();
            resumen.append(getNombreCompleto());

            if (rol != null && !rol.trim().isEmpty()) {
                resumen.append(" - ").append(rol);
            }

            if (!activo) {
                resumen.append(" (Inactivo)");
            }

            return resumen.toString();
        }

        public MiembroGrupo(int usuarioId, String nombre, String rol) {
            this(usuarioId);
            this.nombre = nombre;
            this.rol = rol;
        }

        // Getters y Setters
        public int getUsuarioId() {
            return usuarioId;
        }

        public void setUsuarioId(int usuarioId) {
            this.usuarioId = usuarioId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getApellido() {
            return apellido;
        }

        public void setApellido(String apellido) {
            this.apellido = apellido;
        }

        public String getRol() {
            return rol;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDateTime getFechaAgregado() {
            return fechaAgregado;
        }

        public void setFechaAgregado(LocalDateTime fechaAgregado) {
            this.fechaAgregado = fechaAgregado;
        }

        public boolean isActivo() {
            return activo;
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
        }

        public String getNombreCompleto() {
            if (nombre != null && apellido != null
                    && !nombre.trim().isEmpty() && !apellido.trim().isEmpty()) {
                return apellido.trim() + ", " + nombre.trim();
            } else if (nombre != null && !nombre.trim().isEmpty()) {
                return nombre.trim();
            } else if (apellido != null && !apellido.trim().isEmpty()) {
                return apellido.trim();
            } else {
                return "Usuario " + usuarioId;
            }
        }

        @Override
        public String toString() {
            return getNombreCompleto() + (rol != null ? " (" + rol + ")" : "");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MiembroGrupo that = (MiembroGrupo) obj;
            return usuarioId == that.usuarioId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(usuarioId);
        }

        @Override
        public MiembroGrupo clone() {
            try {
                return (MiembroGrupo) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Error clonando MiembroGrupo", e);
            }
        }
    }

    public static NotificationGroup fromMap(Map<String, Object> data) {
        if (data == null) {
            throw new IllegalArgumentException("Los datos no pueden ser null");
        }

        NotificationGroup grupo = new NotificationGroup();

        if (data.containsKey("id")) {
            grupo.setId((Integer) data.get("id"));
        }

        if (data.containsKey("nombre")) {
            grupo.setNombre((String) data.get("nombre"));
        }

        if (data.containsKey("descripcion")) {
            grupo.setDescripcion((String) data.get("descripcion"));
        }

        if (data.containsKey("creadorId")) {
            grupo.setCreadorId((Integer) data.get("creadorId"));
        }

        // Agregar más campos según necesidad...
        return grupo;
    }

    /**
     * NUEVO: Método para convertir a Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", this.id);
        map.put("nombre", this.nombre);
        map.put("descripcion", this.descripcion);
        map.put("creadorId", this.creadorId);
        map.put("nombreCreador", this.nombreCreador);
        map.put("fechaCreacion", this.fechaCreacion);
        map.put("fechaModificacion", this.fechaModificacion);
        map.put("fechaUltimoUso", this.fechaUltimoUso);
        map.put("activo", this.activo);
        map.put("categoria", this.categoria);
        map.put("color", this.color);
        map.put("totalNotificacionesEnviadas", this.totalNotificacionesEnviadas);
        map.put("esFavorito", this.esFavorito);
        map.put("cantidadMiembros", this.getCantidadMiembros());
        map.put("miembros", new ArrayList<>(this.miembros));

        return map;
    }

    // ========================================
    // MÉTODOS ESTÁTICOS DE UTILIDAD
    // ========================================
    /**
     * Crea un grupo de ejemplo para testing
     *
     * @param nombre Nombre del grupo
     * @param creadorId ID del creador
     * @return Grupo de ejemplo
     */
    public static NotificationGroup crearGrupoEjemplo(String nombre, int creadorId) {
        NotificationGroup grupo = new NotificationGroup(nombre,
                "Grupo de ejemplo creado para testing", creadorId);
        grupo.setCategoria("Ejemplo");
        grupo.setColor("#28a745");
        return grupo;
    }

    /**
     * Valida un nombre de grupo
     *
     * @param nombre Nombre a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        String nombreTrimmed = nombre.trim();

        // CORREGIDO: Validaciones más estrictas pero flexibles
        return nombreTrimmed.length() >= 2
                && // Mínimo 2 caracteres (era 3)
                nombreTrimmed.length() <= 100
                && // CORREGIDO: Permitir más caracteres especiales comunes
                nombreTrimmed.matches("^[a-zA-Z0-9\\s\\-_.,()áéíóúñÁÉÍÓÚÑüÜ]+$")
                && // CORREGIDO: No permitir solo espacios o caracteres especiales
                nombreTrimmed.matches(".*[a-zA-Z0-9áéíóúñÁÉÍÓÚÑüÜ].*");
    }

    /**
     * Valida una descripción de grupo
     *
     * @param descripcion Descripción a validar
     * @return true si es válida, false en caso contrario
     */
    public static boolean validarDescripcion(String descripcion) {
        return descripcion == null || descripcion.length() <= 500;
    }

    /**
     * Crea una copia defensiva del objeto NotificationGroup. Esto es importante
     * para mantener la integridad del cache.
     *
     * @return Nueva instancia de NotificationGroup con los mismos datos
     */
    public NotificationGroup copy() {
        try {
            // CORREGIDO: Usar clone() para una copia más completa
            return this.clone();
        } catch (Exception e) {
            // CORREGIDO: Fallback manual si clone() falla
            NotificationGroup copia = new NotificationGroup();

            copia.setId(this.getId());
            copia.setNombre(this.getNombre());
            copia.setDescripcion(this.getDescripcion());
            copia.setCreadorId(this.getCreadorId());
            copia.setNombreCreador(this.getNombreCreador());
            copia.setFechaCreacion(this.getFechaCreacion());
            copia.setFechaModificacion(this.getFechaModificacion());
            copia.setFechaUltimoUso(this.getFechaUltimoUso());
            copia.setActivo(this.isActivo());
            copia.setCategoria(this.getCategoria());
            copia.setColor(this.getColor());
            copia.setTotalNotificacionesEnviadas(this.getTotalNotificacionesEnviadas());
            copia.setEsFavorito(this.isEsFavorito());

            // Crear copia profunda de miembros
            if (this.getMiembros() != null) {
                copia.setMiembros(new ArrayList<>(this.getMiembros()));
            }

            // Copiar detalles de miembros
            if (this.getMiembrosDetalle() != null) {
                Map<Integer, MiembroGrupo> detallesCopia = new HashMap<>();
                for (Map.Entry<Integer, MiembroGrupo> entry : this.getMiembrosDetalle().entrySet()) {
                    detallesCopia.put(entry.getKey(), entry.getValue().clone());
                }
                copia.setMiembrosDetalle(detallesCopia);
            }

            return copia;
        }
    }

    /**
     * NUEVO: Método para obtener miembros activos solamente
     */
    public List<Integer> getMiembrosActivos() {
        return miembrosDetalle.entrySet().stream()
                .filter(entry -> entry.getValue().isActivo())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * NUEVO: Método para obtener estadísticas del grupo
     */
    public GrupoEstadisticas getEstadisticas() {
        GrupoEstadisticas stats = new GrupoEstadisticas();
        stats.totalMiembros = this.getCantidadMiembros();
        stats.miembrosActivos = (int) miembrosDetalle.values().stream()
                .filter(MiembroGrupo::isActivo)
                .count();
        stats.diasDesdeCreacion = (int) java.time.temporal.ChronoUnit.DAYS.between(
                this.fechaCreacion.toLocalDate(),
                LocalDateTime.now().toLocalDate()
        );
        stats.notificacionesEnviadas = this.totalNotificacionesEnviadas;
        stats.esFavorito = this.esFavorito;
        stats.fueUsadoRecientemente = this.fueUsadoRecientemente();

        return stats;
    }

    /**
     * NUEVO: Método para buscar miembros por nombre/rol
     */
    public List<MiembroGrupo> buscarMiembros(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return new ArrayList<>(miembrosDetalle.values());
        }

        String criterioBusqueda = criterio.toLowerCase().trim();

        return miembrosDetalle.values().stream()
                .filter(miembro -> {
                    String nombre = miembro.getNombreCompleto().toLowerCase();
                    String rol = (miembro.getRol() != null) ? miembro.getRol().toLowerCase() : "";
                    String email = (miembro.getEmail() != null) ? miembro.getEmail().toLowerCase() : "";

                    return nombre.contains(criterioBusqueda)
                            || rol.contains(criterioBusqueda)
                            || email.contains(criterioBusqueda);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * NUEVO: Método para exportar información del grupo
     */
    public String exportarInformacion() {
        StringBuilder export = new StringBuilder();
        export.append("=== EXPORTACIÓN GRUPO DE NOTIFICACIONES ===\n");
        export.append("Fecha de exportación: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        export.append("ID: ").append(this.id).append("\n");
        export.append("Nombre: ").append(this.nombre).append("\n");
        export.append("Descripción: ").append(this.descripcion != null ? this.descripcion : "Sin descripción").append("\n");
        export.append("Creador ID: ").append(this.creadorId).append("\n");
        export.append("Nombre Creador: ").append(this.nombreCreador != null ? this.nombreCreador : "No especificado").append("\n");
        export.append("Categoría: ").append(this.categoria).append("\n");
        export.append("Color: ").append(this.color).append("\n");
        export.append("Estado: ").append(this.activo ? "Activo" : "Inactivo").append("\n");
        export.append("Es Favorito: ").append(this.esFavorito ? "Sí" : "No").append("\n");
        export.append("Total Notificaciones Enviadas: ").append(this.totalNotificacionesEnviadas).append("\n");
        export.append("Fecha Creación: ").append(this.fechaCreacion.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");

        if (this.fechaModificacion != null) {
            export.append("Fecha Modificación: ").append(this.fechaModificacion.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        }

        if (this.fechaUltimoUso != null) {
            export.append("Último Uso: ").append(this.fechaUltimoUso.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        }

        export.append("\n=== MIEMBROS (").append(this.getCantidadMiembros()).append(") ===\n");

        if (this.miembrosDetalle.isEmpty()) {
            export.append("No hay miembros en este grupo.\n");
        } else {
            int contador = 1;
            for (MiembroGrupo miembro : this.miembrosDetalle.values()) {
                export.append(contador).append(". ");
                export.append("ID: ").append(miembro.getUsuarioId());
                export.append(" | Nombre: ").append(miembro.getNombreCompleto());
                export.append(" | Rol: ").append(miembro.getRol() != null ? miembro.getRol() : "No especificado");
                export.append(" | Email: ").append(miembro.getEmail() != null ? miembro.getEmail() : "No especificado");
                export.append(" | Estado: ").append(miembro.isActivo() ? "Activo" : "Inactivo");
                export.append(" | Agregado: ").append(miembro.getFechaAgregado().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                export.append("\n");
                contador++;
            }
        }

        return export.toString();
    }

    /**
     * NUEVO: Método para validar integridad del grupo
     */
    public List<String> validarIntegridad() {
        List<String> problemas = new ArrayList<>();

        // Validar datos básicos
        if (!this.isValido()) {
            problemas.add("El grupo no pasa las validaciones básicas");
        }

        // Validar consistencia entre listas
        if (this.miembros.size() != this.miembrosDetalle.size()) {
            problemas.add("Inconsistencia entre lista de miembros y detalles de miembros");
        }

        // Validar que todos los miembros tengan detalles
        for (Integer miembroId : this.miembros) {
            if (!this.miembrosDetalle.containsKey(miembroId)) {
                problemas.add("Miembro " + miembroId + " no tiene detalles asociados");
            }
        }

        // Validar que todos los detalles correspondan a miembros
        for (Integer detalleId : this.miembrosDetalle.keySet()) {
            if (!this.miembros.contains(detalleId)) {
                problemas.add("Detalle para usuario " + detalleId + " sin miembro correspondiente");
            }
        }

        // Validar fechas
        if (this.fechaModificacion != null && this.fechaCreacion != null) {
            if (this.fechaModificacion.isBefore(this.fechaCreacion)) {
                problemas.add("Fecha de modificación es anterior a fecha de creación");
            }
        }

        if (this.fechaUltimoUso != null && this.fechaCreacion != null) {
            if (this.fechaUltimoUso.isBefore(this.fechaCreacion)) {
                problemas.add("Fecha de último uso es anterior a fecha de creación");
            }
        }

        return problemas;
    }

    /**
     * NUEVO: Método para reparar inconsistencias
     */
    public boolean repararInconsistencias() {
        boolean reparado = false;

        // Sincronizar lista de miembros con detalles
        List<Integer> miembrosValidos = new ArrayList<>();
        for (Integer miembroId : this.miembros) {
            if (miembroId != null && miembroId > 0) {
                miembrosValidos.add(miembroId);

                // Crear detalle si no existe
                if (!this.miembrosDetalle.containsKey(miembroId)) {
                    this.miembrosDetalle.put(miembroId, new MiembroGrupo(miembroId));
                    reparado = true;
                }
            } else {
                reparado = true; // Se removió un ID inválido
            }
        }

        // Actualizar lista de miembros con los válidos
        if (this.miembros.size() != miembrosValidos.size()) {
            this.miembros = miembrosValidos;
            reparado = true;
        }

        // Remover detalles huérfanos
        List<Integer> detallesARemover = new ArrayList<>();
        for (Integer detalleId : this.miembrosDetalle.keySet()) {
            if (!this.miembros.contains(detalleId)) {
                detallesARemover.add(detalleId);
            }
        }

        if (!detallesARemover.isEmpty()) {
            for (Integer id : detallesARemover) {
                this.miembrosDetalle.remove(id);
            }
            reparado = true;
        }

        // Corregir valores inválidos
        if (this.totalNotificacionesEnviadas < 0) {
            this.totalNotificacionesEnviadas = 0;
            reparado = true;
        }

        if (this.nombre != null) {
            String nombreLimpio = this.nombre.trim();
            if (!nombreLimpio.equals(this.nombre)) {
                this.nombre = nombreLimpio;
                reparado = true;
            }
        }

        if (this.descripcion != null) {
            String descripcionLimpia = this.descripcion.trim();
            if (!descripcionLimpia.equals(this.descripcion)) {
                this.descripcion = descripcionLimpia;
                reparado = true;
            }
        }

        if (reparado) {
            this.fechaModificacion = LocalDateTime.now();
        }

        return reparado;
    }

    /**
     * Clase para estadísticas del grupo
     */
    public static class GrupoEstadisticas {

        public int totalMiembros;
        public int miembrosActivos;
        public int diasDesdeCreacion;
        public int notificacionesEnviadas;
        public boolean esFavorito;
        public boolean fueUsadoRecientemente;

        @Override
        public String toString() {
            return String.format(
                    "Miembros: %d/%d activos, Creado hace %d días, %d notificaciones enviadas, %s%s",
                    miembrosActivos, totalMiembros, diasDesdeCreacion, notificacionesEnviadas,
                    esFavorito ? "⭐ Favorito" : "",
                    fueUsadoRecientemente ? ", ⏰ Usado recientemente" : ""
            );
        }
    }

}
