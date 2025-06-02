package main.java.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import main.java.database.Conexion;

/**
 * Helper class para notificaciones académicas específicas del sistema escolar.
 * Integrado con NotificationManager para envío automático de notificaciones.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class AcademicNotificationHelper {

    private static AcademicNotificationHelper instance;
    private final NotificationManager notificationManager;
    private final Connection connection;

    private AcademicNotificationHelper() {
        this.notificationManager = NotificationManager.getInstance();
        this.connection = Conexion.getInstancia().verificarConexion();
    }

    public static synchronized AcademicNotificationHelper getInstance() {
        if (instance == null) {
            instance = new AcademicNotificationHelper();
        }
        return instance;
    }

    // ===============================================
    // NOTIFICACIONES DE NOTAS
    // ===============================================
    /**
     * Notifica cuando se publica una nueva nota
     */
    public CompletableFuture<Boolean> notificarNuevaNota(int alumnoId, String materia,
            String tipoTrabajo, double nota,
            int profesorId) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String profesorNombre = obtenerNombreUsuario(profesorId);

            String titulo = "📊 Nueva Nota Disponible";
            String contenido = String.format(
                    "Se ha publicado tu nota para %s:\n\n"
                    + "📝 Trabajo: %s\n"
                    + "🎯 Nota: %.2f\n"
                    + "👨‍🏫 Profesor: %s\n\n"
                    + "Puedes revisar tus notas en la sección correspondiente.",
                    materia, tipoTrabajo, nota, profesorNombre
            );

            // CORREGIDO: Usar método público existente
            return createNotificationWithDetails(titulo, contenido, "NORMAL", "#28a745", "📊", alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando nueva nota: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica sobre notas bimestrales publicadas
     */
    public CompletableFuture<Boolean> notificarNotaBimestral(int alumnoId, String materia,
            String bimestre, double nota,
            int profesorId) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String profesorNombre = obtenerNombreUsuario(profesorId);
            String emoji = nota >= 7 ? "🎉" : nota >= 6 ? "👍" : "📚";

            String titulo = emoji + " Nota Bimestral - " + materia;
            String contenido = String.format(
                    "Tu nota del %s bimestre está disponible:\n\n"
                    + "📚 Materia: %s\n"
                    + "📅 Período: %s\n"
                    + "🎯 Nota: %.2f\n"
                    + "👨‍🏫 Profesor: %s\n\n"
                    + "%s",
                    bimestre, materia, bimestre, nota, profesorNombre,
                    nota >= 6 ? "¡Felicitaciones!" : "Recuerda estudiar para mejorar tu rendimiento."
            );

            String color = nota >= 7 ? "#28a745" : nota >= 6 ? "#ffc107" : "#dc3545";

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "ALTA", color, emoji, alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando nota bimestral: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica a profesores sobre trabajos pendientes de corrección
     */
    public CompletableFuture<Boolean> notificarTrabajosPendientes(int profesorId, String materia, int cantidad) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "📝 Trabajos Pendientes de Corrección";
            String contenido = String.format(
                    "Tienes %d trabajo%s pendiente%s de corrección:\n\n"
                    + "📚 Materia: %s\n"
                    + "⏰ Recuerda revisar y calificar pronto\n\n"
                    + "Los estudiantes están esperando sus calificaciones.",
                    cantidad,
                    cantidad == 1 ? "" : "s",
                    cantidad == 1 ? "" : "s",
                    materia
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "NORMAL", "#17a2b8", "📝", profesorId);

        } catch (Exception e) {
            System.err.println("Error notificando trabajos pendientes: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===============================================
    // NOTIFICACIONES DE ASISTENCIA
    // ===============================================
    /**
     * Notifica sobre falta de asistencia
     */
    public CompletableFuture<Boolean> notificarFaltaAsistencia(int alumnoId, String fecha,
            String materia, String tipoFalta,
            int preceptorId) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String preceptorNombre = obtenerNombreUsuario(preceptorId);

            String emoji = "⚠️";
            String color = "#fd7e14";

            if ("JUSTIFICADA".equalsIgnoreCase(tipoFalta)) {
                emoji = "📋";
                color = "#28a745";
            }

            String titulo = emoji + " Registro de Asistencia";
            String contenido = String.format(
                    "Se ha registrado tu asistencia:\n\n"
                    + "📅 Fecha: %s\n"
                    + "📚 Materia: %s\n"
                    + "📊 Estado: %s\n"
                    + "👨‍💼 Registrado por: %s\n\n"
                    + "%s",
                    fecha, materia, tipoFalta,
                    preceptorNombre,
                    "JUSTIFICADA".equalsIgnoreCase(tipoFalta)
                    ? "Falta justificada correctamente."
                    : "Recuerda justificar tus faltas cuando sea necesario."
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "ALTA", color, emoji, alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando falta: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica sobre problemas de asistencia (muchas faltas)
     */
    public CompletableFuture<Boolean> notificarProblemaAsistencia(int alumnoId, int cantidadFaltas,
            String periodo) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "⚠️ Atención: Problema de Asistencia";
            String contenido = String.format(
                    "IMPORTANTE: Tu registro de asistencia requiere atención.\n\n"
                    + "📊 Faltas acumuladas en %s: %d\n"
                    + "⚠️ Esta cantidad puede afectar tu promoción\n\n"
                    + "Te recomendamos:\n"
                    + "• Asistir regularmente a clases\n"
                    + "• Justificar faltas cuando sea necesario\n"
                    + "• Hablar con preceptoría si tienes algún problema\n\n"
                    + "Para más información, consulta con tu preceptor.",
                    periodo, cantidadFaltas
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "URGENTE", "#dc3545", "⚠️", alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando problema asistencia: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===============================================
    // NOTIFICACIONES DE BOLETINES
    // ===============================================
    /**
     * Notifica cuando hay un nuevo boletín disponible
     */
    public CompletableFuture<Boolean> notificarBoletinDisponible(int alumnoId, String periodo,
            String fechaCreacion) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "📋 Boletín Disponible";
            String contenido = String.format(
                    "Tu boletín ya está disponible para descargar.\n\n"
                    + "📅 Período: %s\n"
                    + "📆 Fecha de creación: %s\n\n"
                    + "Puedes descargarlo desde la sección 'Mis Boletines'.\n"
                    + "El archivo estará disponible en formato PDF.",
                    periodo, fechaCreacion
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "NORMAL", "#28a745", "📋", alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando boletín disponible: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica a preceptores sobre boletines generados masivamente
     */
    public CompletableFuture<Boolean> notificarBoletinesMasivos(int preceptorId, String curso,
            int cantidadGenerados, String periodo) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "📊 Boletines Generados - " + curso;
            String contenido = String.format(
                    "Proceso de generación de boletines completado:\n\n"
                    + "🎓 Curso: %s\n"
                    + "📋 Boletines generados: %d\n"
                    + "📅 Período: %s\n"
                    + "⏰ Fecha: %s\n\n"
                    + "Los estudiantes ya pueden descargar sus boletines.",
                    curso, cantidadGenerados, periodo,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "NORMAL", "#17a2b8", "📊", preceptorId);

        } catch (Exception e) {
            System.err.println("Error notificando boletines masivos: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===============================================
    // NOTIFICACIONES ADMINISTRATIVAS
    // ===============================================
    /**
     * Notifica sobre nuevo usuario registrado (a administradores)
     */
    public CompletableFuture<Boolean> notificarNuevoUsuarioRegistrado(String nombreUsuario,
            String email, String rol) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "👤 Nuevo Usuario Registrado";
            String contenido = String.format(
                    "Un nuevo usuario se ha registrado en el sistema:\n\n"
                    + "👤 Nombre: %s\n"
                    + "📧 Email: %s\n"
                    + "🎭 Rol: %s\n"
                    + "⏰ Fecha: %s\n\n"
                    + "Revisa la sección de usuarios pendientes para aprobar el registro.",
                    nombreUsuario, email, rol,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            return notificationManager.enviarNotificacionARol(titulo, contenido, 1); // Solo admins

        } catch (Exception e) {
            System.err.println("Error notificando nuevo usuario: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica sobre usuario aprobado
     */
    public CompletableFuture<Boolean> notificarUsuarioAprobado(int usuarioId, String nombreCompleto) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "✅ Usuario Aprobado";
            String contenido = String.format(
                    "¡Felicitaciones! Tu registro ha sido aprobado.\n\n"
                    + "🎉 Tu cuenta está ahora activa\n"
                    + "🚀 Ya puedes acceder a todas las funciones del sistema\n"
                    + "📚 Explora las diferentes secciones disponibles\n\n"
                    + "¡Bienvenido %s al Sistema de Gestión Escolar ET20!",
                    nombreCompleto
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "ALTA", "#28a745", "🎉", usuarioId);

        } catch (Exception e) {
            System.err.println("Error notificando usuario aprobado: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica sobre cambios en cursos
     */
    public CompletableFuture<Boolean> notificarCambioCurso(int alumnoId, String cursoAnterior,
            String cursoNuevo, String motivo) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "📚 Cambio de Curso";
            String contenido = String.format(
                    "Se ha realizado un cambio en tu asignación de curso:\n\n"
                    + "📖 Curso anterior: %s\n"
                    + "📗 Curso nuevo: %s\n"
                    + "📝 Motivo: %s\n"
                    + "📅 Fecha: %s\n\n"
                    + "Si tienes dudas, contacta a preceptoría.",
                    cursoAnterior, cursoNuevo, motivo,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "ALTA", "#fd7e14", "📚", alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando cambio curso: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===============================================
    // NOTIFICACIONES DE PRÉSTAMOS (ATTP)
    // ===============================================
    /**
     * Notifica sobre nuevo préstamo de equipo
     */
    public CompletableFuture<Boolean> notificarNuevoPrestamo(int alumnoId, String equipo,
            String fechaDevolucion, int attpId) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String attpNombre = obtenerNombreUsuario(attpId);

            String titulo = "💻 Nuevo Préstamo de Equipo";
            String contenido = String.format(
                    "Se ha registrado un préstamo a tu nombre:\n\n"
                    + "💻 Equipo: %s\n"
                    + "📅 Fecha de devolución: %s\n"
                    + "👨‍💼 Responsable: %s\n\n"
                    + "⚠️ IMPORTANTE:\n"
                    + "• Cuida el equipo prestado\n"
                    + "• Devuélvelo en la fecha indicada\n"
                    + "• Reporta cualquier problema inmediatamente\n\n"
                    + "Gracias por usar responsablemente los recursos de la escuela.",
                    equipo, fechaDevolucion, attpNombre
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "NORMAL", "#17a2b8", "💻", alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando préstamo: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Notifica sobre devolución vencida
     */
    public CompletableFuture<Boolean> notificarDevolucionVencida(int alumnoId, String equipo,
            String fechaVencimiento, int diasVencidos) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String titulo = "⚠️ URGENTE - Devolución Vencida";
            String contenido = String.format(
                    "ATENCIÓN: Tienes un préstamo vencido.\n\n"
                    + "💻 Equipo: %s\n"
                    + "📅 Fecha de vencimiento: %s\n"
                    + "⏰ Días vencidos: %d\n\n"
                    + "🚨 ACCIÓN REQUERIDA:\n"
                    + "• Devuelve el equipo INMEDIATAMENTE\n"
                    + "• Dirígete al ATTP lo antes posible\n"
                    + "• Pueden aplicarse sanciones por retraso\n\n"
                    + "No ignores este mensaje.",
                    equipo, fechaVencimiento, diasVencidos
            );

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, "URGENTE", "#dc3545", "🚨", alumnoId);

        } catch (Exception e) {
            System.err.println("Error notificando devolución vencida: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===============================================
    // MÉTODOS AUXILIARES
    // ===============================================
    /**
     * Verifica si el sistema de notificaciones está listo
     */
    private boolean isNotificationSystemReady() {
        return notificationManager != null
                && notificationManager.isInitialized()
                && connection != null;
    }

    /**
     * Obtiene el nombre completo de un usuario
     */
    private String obtenerNombreUsuario(int usuarioId) {
        try {
            if (connection == null) {
                return "Usuario #" + usuarioId;
            }

            String query = "SELECT CONCAT(COALESCE(nombre, ''), ' ', COALESCE(apellido, '')) as nombre_completo \n" +
"FROM usuarios WHERE id = ? AND status = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre de usuario: " + e.getMessage());
        }

        return "Usuario #" + usuarioId;
    }

    /**
     * Obtiene los IDs de alumnos de un curso específico
     */
    public List<Integer> obtenerAlumnosDeCurso(int cursoId) {
        List<Integer> alumnos = new ArrayList<>();

        try {
            if (connection == null) {
                return alumnos;
            }

            String query = "SELECT alumno_id FROM alumno_curso WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnos.add(rs.getInt("alumno_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo alumnos del curso: " + e.getMessage());
        }

        return alumnos;
    }

    /**
     * Notifica a todos los alumnos de un curso
     */
    public CompletableFuture<Boolean> notificarACurso(int cursoId, String titulo, String contenido,
            String prioridad, String color, String icono) {
        if (!isNotificationSystemReady()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            List<Integer> alumnos = obtenerAlumnosDeCurso(cursoId);

            if (alumnos.isEmpty()) {
                System.err.println("No se encontraron alumnos para el curso ID: " + cursoId);
                return CompletableFuture.completedFuture(false);
            }

            // Convertir lista a array
            int[] alumnosArray = alumnos.stream().mapToInt(Integer::intValue).toArray();

            // CORREGIDO: Usar método helper interno
            return createNotificationWithDetails(titulo, contenido, prioridad, color, icono, alumnosArray);

        } catch (Exception e) {
            System.err.println("Error notificando a curso: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Método para limpiar recursos
     */
    public void dispose() {
        try {
            // Limpiar referencias
            instance = null;
            System.out.println("✅ AcademicNotificationHelper recursos liberados");
        } catch (Exception e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        }
    }

    // ===============================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ===============================================
    /**
     * NUEVO: Método helper para crear notificaciones con detalles específicos
     * Reemplaza el método privado del NotificationManager
     */
    private CompletableFuture<Boolean> createNotificationWithDetails(String titulo, String contenido,
            String prioridad, String color,
            String icono, int... destinatarios) {
        try {
            // Crear la notificación base
            main.java.models.Notificacion notificacion = new main.java.models.Notificacion(
                    titulo, contenido, "INDIVIDUAL", notificationManager.getCurrentUserId()
            );

            // Configurar destinatarios
            List<Integer> destinatariosList = java.util.Arrays.stream(destinatarios)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());
            notificacion.setDestinatariosIndividuales(destinatariosList);

            // Configurar detalles específicos
            if (prioridad != null && !prioridad.trim().isEmpty()) {
                notificacion.setPrioridad(prioridad);
            }

            if (color != null && !color.trim().isEmpty()) {
                notificacion.setColor(color);
            }

            if (icono != null && !icono.trim().isEmpty()) {
                notificacion.setIcono(icono);
            }

            // Enviar usando el servicio directo
            return notificationManager.getNotificationService().enviarNotificacion(notificacion);

        } catch (Exception e) {
            System.err.println("Error creando notificación con detalles: " + e.getMessage());
            return java.util.concurrent.CompletableFuture.completedFuture(false);
        }
    }
}
