package main.java.utils;

import main.java.utils.AcademicNotificationHelper;
import main.java.utils.NotificationManager;

/**
 * Utilidad para integrar fácilmente las notificaciones en los Panel Managers.
 * Proporciona métodos de conveniencia para que cada rol pueda enviar notificaciones
 * específicas sin duplicar código.
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class NotificationIntegrationUtil {
    
    private static NotificationIntegrationUtil instance;
    private final NotificationManager notificationManager;
    private final AcademicNotificationHelper academicHelper;
    
    private NotificationIntegrationUtil() {
        this.notificationManager = NotificationManager.getInstance();
        this.academicHelper = AcademicNotificationHelper.getInstance();
    }
    
    public static synchronized NotificationIntegrationUtil getInstance() {
        if (instance == null) {
            instance = new NotificationIntegrationUtil();
        }
        return instance;
    }
    
    // ===============================================
    // MÉTODOS PARA ADMINISTRADORES
    // ===============================================
    
    /**
     * Notifica sobre aprobación de usuario (Admin)
     */
    public void notificarUsuarioAprobado(int usuarioId, String nombreCompleto) {
        if (isSystemReady()) {
            academicHelper.notificarUsuarioAprobado(usuarioId, nombreCompleto);
        }
    }
    
    /**
     * Notifica sobre nuevo registro pendiente (Admin)
     */
    public void notificarNuevoRegistroPendiente(String nombreUsuario, String email, String rol) {
        if (isSystemReady()) {
            academicHelper.notificarNuevoUsuarioRegistrado(nombreUsuario, email, rol);
        }
    }
    
    /**
     * Envía notificación de sistema a todos los roles (Admin)
     */
    public void enviarNotificacionSistema(String titulo, String contenido, String tipoEvento) {
        if (isSystemReady() && notificationManager.canManageNotifications()) {
            notificationManager.notificarEventoGeneral(titulo, contenido, tipoEvento);
        }
    }
    
    // ===============================================
    // MÉTODOS PARA PROFESORES
    // ===============================================
    
    /**
     * Notifica sobre nueva nota publicada (Profesor)
     */
    public void notificarNotaPublicada(int alumnoId, String materia, String tipoTrabajo, 
                                      double nota, int profesorId) {
        if (isSystemReady()) {
            academicHelper.notificarNuevaNota(alumnoId, materia, tipoTrabajo, nota, profesorId);
        }
    }
    
    /**
     * Notifica sobre nota bimestral (Profesor)
     */
    public void notificarNotaBimestral(int alumnoId, String materia, String bimestre, 
                                      double nota, int profesorId) {
        if (isSystemReady()) {
            academicHelper.notificarNotaBimestral(alumnoId, materia, bimestre, nota, profesorId);
        }
    }
    
    /**
     * Notifica sobre asistencia registrada (Profesor)
     */
    public void notificarAsistenciaRegistrada(int alumnoId, String fecha, String materia, 
                                            String estado, int profesorId) {
        if (isSystemReady()) {
            academicHelper.notificarFaltaAsistencia(alumnoId, fecha, materia, estado, profesorId);
        }
    }
    
    /**
     * Recuerda a profesor sobre trabajos pendientes
     */
    public void recordarTrabajosPendientes(int profesorId, String materia, int cantidad) {
        if (isSystemReady()) {
            academicHelper.notificarTrabajosPendientes(profesorId, materia, cantidad);
        }
    }
    
    // ===============================================
    // MÉTODOS PARA PRECEPTORES
    // ===============================================
    
    /**
     * Notifica sobre problema de asistencia (Preceptor)
     */
    public void notificarProblemaAsistencia(int alumnoId, int cantidadFaltas, String periodo) {
        if (isSystemReady()) {
            academicHelper.notificarProblemaAsistencia(alumnoId, cantidadFaltas, periodo);
        }
    }
    
    /**
     * Notifica sobre boletín generado (Preceptor)
     */
    public void notificarBoletinGenerado(int alumnoId, String periodo, String fechaCreacion) {
        if (isSystemReady()) {
            academicHelper.notificarBoletinDisponible(alumnoId, periodo, fechaCreacion);
        }
    }
    
    /**
     * Notifica sobre generación masiva de boletines (Preceptor)
     */
    public void notificarBoletinesMasivos(int preceptorId, String curso, int cantidad, String periodo) {
        if (isSystemReady()) {
            academicHelper.notificarBoletinesMasivos(preceptorId, curso, cantidad, periodo);
        }
    }
    
    /**
     * Notifica sobre cambio de curso (Preceptor)
     */
    public void notificarCambioCurso(int alumnoId, String cursoAnterior, String cursoNuevo, String motivo) {
        if (isSystemReady()) {
            academicHelper.notificarCambioCurso(alumnoId, cursoAnterior, cursoNuevo, motivo);
        }
    }
    
    // ===============================================
    // MÉTODOS PARA ATTP
    // ===============================================
    
    /**
     * Notifica sobre nuevo préstamo (ATTP)
     */
    public void notificarNuevoPrestamo(int alumnoId, String equipo, String fechaDevolucion, int attpId) {
        if (isSystemReady()) {
            academicHelper.notificarNuevoPrestamo(alumnoId, equipo, fechaDevolucion, attpId);
        }
    }
    
    /**
     * Notifica sobre devolución vencida (ATTP)
     */
    public void notificarDevolucionVencida(int alumnoId, String equipo, String fechaVencimiento, int diasVencidos) {
        if (isSystemReady()) {
            academicHelper.notificarDevolucionVencida(alumnoId, equipo, fechaVencimiento, diasVencidos);
        }
    }
    
    /**
     * Notifica sobre equipo dañado o perdido (ATTP)
     */
    public void notificarEquipoDaniado(int alumnoId, String equipo, String descripcionProblema, int attpId) {
        if (isSystemReady() && notificationManager.canSendNotifications()) {
            String titulo = "⚠️ Problema con Equipo Prestado";
            String contenido = String.format(
                "Se ha reportado un problema con tu equipo prestado:\n\n" +
                "💻 Equipo: %s\n" +
                "⚠️ Problema: %s\n\n" +
                "Por favor, contacta inmediatamente al ATTP para resolver esta situación.",
                equipo, descripcionProblema
            );
            
            notificationManager.enviarNotificacionUrgente(titulo, contenido, alumnoId);
        }
    }
    
    // ===============================================
    // MÉTODOS GENERALES PARA TODOS LOS ROLES
    // ===============================================
    
    /**
     * Envía notificación básica a usuario específico
     */
    public void enviarNotificacionBasica(String titulo, String contenido, int destinatarioId) {
        if (isSystemReady() && notificationManager.canSendNotifications()) {
            notificationManager.enviarNotificacionRapida(titulo, contenido, destinatarioId);
        }
    }
    
    /**
     * Envía notificación urgente
     */
    public void enviarNotificacionUrgente(String titulo, String contenido, int... destinatarios) {
        if (isSystemReady() && notificationManager.canSendNotifications()) {
            notificationManager.enviarNotificacionUrgente(titulo, contenido, destinatarios);
        }
    }
    
    /**
     * Envía notificación a un rol completo
     */
    public void enviarNotificacionARol(String titulo, String contenido, int rolDestino) {
        if (isSystemReady() && notificationManager.canSendNotifications()) {
            notificationManager.enviarNotificacionARol(titulo, contenido, rolDestino);
        }
    }
    
    /**
     * Notifica sobre mantenimiento del sistema
     */
    public void notificarMantenimiento(String fecha, String hora, String duracion) {
        if (isSystemReady() && notificationManager.canManageNotifications()) {
            notificationManager.notificarEventoGeneral(
                "🔧 Mantenimiento Programado",
                String.format("El sistema estará en mantenimiento el %s a las %s. Duración: %s", 
                             fecha, hora, duracion),
                "mantenimiento"
            );
        }
    }
    
    // ===============================================
    // MÉTODOS DE ESTADO Y UTILIDAD
    // ===============================================
    
    /**
     * Verifica si el sistema está listo para enviar notificaciones
     */
    private boolean isSystemReady() {
        return notificationManager != null && 
               notificationManager.isInitialized() && 
               academicHelper != null;
    }
    
    /**
     * Obtiene el número de notificaciones no leídas del usuario actual
     */
    public int getNotificacionesNoLeidas() {
        if (isSystemReady()) {
            return notificationManager.getUnreadCount();
        }
        return 0;
    }
    
    /**
     * Verifica si el usuario actual puede enviar notificaciones
     */
    public boolean puedeEnviarNotificaciones() {
        return isSystemReady() && notificationManager.canSendNotifications();
    }
    
    /**
     * Verifica si el usuario actual puede gestionar notificaciones (admin)
     */
    public boolean puedeGestionarNotificaciones() {
        return isSystemReady() && notificationManager.canManageNotifications();
    }
    
    /**
     * Obtiene información del usuario actual
     */
    public int getCurrentUserId() {
        if (isSystemReady()) {
            return notificationManager.getCurrentUserId();
        }
        return -1;
    }
    
    /**
     * Obtiene el rol del usuario actual
     */
    public int getCurrentUserRole() {
        if (isSystemReady()) {
            return notificationManager.getCurrentUserRole();
        }
        return -1;
    }
    
    /**
     * Fuerza actualización de notificaciones
     */
    public void actualizarNotificaciones() {
        if (isSystemReady()) {
            notificationManager.forceRefresh();
        }
    }
    
    /**
     * Envía notificación de prueba (para desarrollo)
     */
    public void enviarNotificacionPrueba() {
        if (isSystemReady() && notificationManager.canSendNotifications()) {
            notificationManager.enviarNotificacionPrueba();
        }
    }
    
    /**
     * Obtiene estadísticas del sistema (solo admin)
     */
    public String getEstadisticasNotificaciones() {
        if (isSystemReady() && notificationManager.canManageNotifications()) {
            return notificationManager.getNotificationStats();
        }
        return "No tienes permisos para ver estadísticas.";
    }
    
    // ===============================================
    // MÉTODOS ESPECÍFICOS POR CONTEXTO
    // ===============================================
    
    /**
     * Contexto: Al cargar notas en NotasProfesorPanel
     */
    public void contextoNotasProfesor(int profesorId, String materia, int cantidadTrabajos) {
        if (cantidadTrabajos > 0) {
            recordarTrabajosPendientes(profesorId, materia, cantidadTrabajos);
        }
    }
    
    /**
     * Contexto: Al aprobar usuario en AdminPanelManager
     */
    public void contextoAprobacionUsuario(int usuarioId, String nombre, String apellido, String rol) {
        String nombreCompleto = apellido + ", " + nombre;
        notificarUsuarioAprobado(usuarioId, nombreCompleto);
        
        // También notificar a otros admins
        if (isSystemReady()) {
            enviarNotificacionARol(
                "✅ Usuario Aprobado",
                String.format("Se ha aprobado el usuario: %s como %s", nombreCompleto, rol),
                1 // Solo a administradores
            );
        }
    }
    
    /**
     * Contexto: Al registrar asistencia
     */
    public void contextoRegistroAsistencia(int alumnoId, String fecha, String materia, 
                                         String estado, int registradoPorId) {
        notificarAsistenciaRegistrada(alumnoId, fecha, materia, estado, registradoPorId);
        
        // Si es falta, verificar si hay problema de asistencia
        if ("FALTA".equalsIgnoreCase(estado)) {
            // Aquí podrías agregar lógica para verificar el total de faltas
            // y enviar notificación de problema si es necesario
        }
    }
    
    /**
     * Contexto: Al generar boletín
     */
    public void contextoGeneracionBoletin(int alumnoId, String periodo, int generadoPorId) {
        String fechaActual = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        notificarBoletinGenerado(alumnoId, periodo, fechaActual);
    }
    
    /**
     * Contexto: Al realizar préstamo
     */
    public void contextoPrestamo(int alumnoId, String equipo, String fechaDevolucion, int attpId) {
        notificarNuevoPrestamo(alumnoId, equipo, fechaDevolucion, attpId);
    }
    
    // ===============================================
    // MÉTODOS DE LIMPIEZA
    // ===============================================
    
    /**
     * Limpia recursos
     */
    public void dispose() {
        try {
            instance = null;
            System.out.println("✅ NotificationIntegrationUtil recursos liberados");
        } catch (Exception e) {
            System.err.println("Error liberando NotificationIntegrationUtil: " + e.getMessage());
        }
    }
    
    /**
     * Información de debug
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== NotificationIntegrationUtil Debug ===\n");
        info.append("Sistema listo: ").append(isSystemReady()).append("\n");
        info.append("Usuario actual: ").append(getCurrentUserId()).append("\n");
        info.append("Rol actual: ").append(getCurrentUserRole()).append("\n");
        info.append("Puede enviar: ").append(puedeEnviarNotificaciones()).append("\n");
        info.append("Puede gestionar: ").append(puedeGestionarNotificaciones()).append("\n");
        info.append("No leídas: ").append(getNotificacionesNoLeidas()).append("\n");
        info.append("========================================");
        return info.toString();
    }
}