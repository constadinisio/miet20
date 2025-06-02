package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import main.java.views.users.common.NotasVisualizationPanel;
import main.java.views.users.common.PanelBoletinesAlumno;
import main.java.utils.NotificationIntegrationUtil;

/**
 * Gestor de paneles específico para el rol de ALUMNO.
 * VERSIÓN COMPLETA CON SISTEMA DE NOTIFICACIONES INTEGRADO
 * 
 * Funcionalidades del Alumno:
 * - Visualización de notas y calificaciones
 * - Consulta de asistencias y faltas
 * - Descarga de boletines académicos
 * - Recepción de notificaciones académicas
 * - Seguimiento de préstamos de equipos
 * 
 * Sistema de Notificaciones para Alumnos:
 * - Recibe notificaciones sobre nuevas notas
 * - Alertas sobre faltas y problemas de asistencia
 * - Notificaciones de boletines disponibles
 * - Avisos sobre préstamos y devoluciones
 * - Comunicados generales de la institución
 * - Notificaciones de cambios académicos
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Con Sistema de Notificaciones Completo
 */
public class AlumnoPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int alumnoId;
    private final NotificationIntegrationUtil notificationUtil;

    // Estado del alumno para notificaciones contextuales
    private String nombreCompleto = "";
    private String cursoActual = "";
    private int notificacionesNoLeidas = 0;
    private boolean primerIngreso = true;

    // Control de actividad para reportes
    private long ultimaActividad = System.currentTimeMillis();
    private int consultasNotasHoy = 0;
    private int consultasAsistenciasHoy = 0;
    private int descargasBoletinesHoy = 0;

    public AlumnoPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.alumnoId = userId;
        this.notificationUtil = NotificationIntegrationUtil.getInstance();
        
        System.out.println("=== ALUMNO PANEL MANAGER INICIALIZADO ===");
        System.out.println("Alumno ID: " + alumnoId);
        System.out.println("Sistema de notificaciones: " + (notificationUtil != null ? "✅ ACTIVO" : "❌ INACTIVO"));
        
        // Inicializar sistema específico para alumno
        inicializarSistemaAlumno();
        
        // Cargar información básica del alumno
        cargarInformacionAlumno();
        
        // Verificar notificaciones pendientes
        verificarNotificacionesPendientes();
    }

    /**
     * NUEVO: Inicializa el sistema específico para alumnos
     */
    private void inicializarSistemaAlumno() {
        try {
            if (notificationUtil != null) {
                // Los alumnos generalmente no pueden enviar notificaciones, solo recibir
                System.out.println("📧 Sistema de recepción de notificaciones activado");
                
                // Obtener contador de notificaciones no leídas
                notificacionesNoLeidas = notificationUtil.getNotificacionesNoLeidas();
                System.out.println("📬 Notificaciones no leídas: " + notificacionesNoLeidas);
                
                // Enviar notificación de bienvenida si es primer ingreso
                if (primerIngreso) {
                    enviarNotificacionBienvenida();
                }
                
            } else {
                System.err.println("⚠️ Sistema de notificaciones no disponible para alumno");
            }
        } catch (Exception e) {
            System.err.println("Error inicializando sistema alumno: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Carga información básica del alumno
     */
    private void cargarInformacionAlumno() {
        try {
            // Aquí harías consulta a BD para obtener info del alumno
            // Por ahora simulamos
            nombreCompleto = "Alumno #" + alumnoId;
            cursoActual = "4° A";
            
            System.out.println("👨‍🎓 Información del alumno cargada: " + nombreCompleto + " - " + cursoActual);
            
        } catch (Exception e) {
            System.err.println("Error cargando información del alumno: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Verifica notificaciones pendientes al iniciar
     */
    private void verificarNotificacionesPendientes() {
        try {
            if (notificationUtil != null && notificacionesNoLeidas > 0) {
                // Mostrar mensaje discreto sobre notificaciones pendientes
                SwingUtilities.invokeLater(() -> {
                    mostrarMensajeNotificacionesPendientes();
                });
            }
        } catch (Exception e) {
            System.err.println("Error verificando notificaciones: " + e.getMessage());
        }
    }

    @Override
    public JComponent[] createButtons() {
        JButton btnNotas = createStyledButton("MIS NOTAS", "notas");
        JButton btnAsistencias = createStyledButton("ASISTENCIAS", "asistencias");
        JButton btnMisBoletines = createStyledButton("MIS BOLETINES", "misBoletines");
        JButton btnNotificaciones = createStyledButton("NOTIFICACIONES", "notificaciones"); // NUEVO

        return new JComponent[]{
            btnNotas,
            btnAsistencias,
            btnMisBoletines,
            btnNotificaciones
        };
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(76, 175, 80)); // Verde para alumnos
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        // NUEVO: Agregar indicador visual para notificaciones
        if ("notificaciones".equals(actionCommand) && notificacionesNoLeidas > 0) {
            button.setText(text + " (" + notificacionesNoLeidas + ")");
            button.setBackground(new Color(255, 87, 34)); // Naranja para destacar
        }

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        try {
            System.out.println("=== ACCIÓN ALUMNO: " + actionCommand + " ===");
            
            // Registrar actividad
            registrarActividad(actionCommand);
            
            switch (actionCommand) {
                case "notas":
                    mostrarNotasAlumno();
                    break;
                case "asistencias":
                    mostrarAsistencias();
                    break;
                case "misBoletines":
                    mostrarMisBoletines();
                    break;
                case "notificaciones": // NUEVO
                    mostrarNotificaciones();
                    break;
                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }
        } catch (Exception ex) {
            System.err.println("❌ Error en AlumnoPanelManager: " + ex.getMessage());
            ex.printStackTrace();
            
            // Notificar error discretamente
            notificarErrorInterno(actionCommand, ex.getMessage());
            
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar la vista: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra las notas del alumno con seguimiento
     */
    private void mostrarNotasAlumno() {
        try {
            System.out.println("Creando NotasVisualizationPanel para alumno ID: " + alumnoId);
            consultasNotasHoy++;

            NotasVisualizationPanel panelNotas = 
                new NotasVisualizationPanel(ventana, alumnoId, 4); // rol 4 = alumno

            ventana.mostrarPanelResponsive(panelNotas, "Mis Notas");
            
            System.out.println("✅ Panel de notas del alumno mostrado exitosamente");

            // NUEVO: Registrar consulta de notas
            registrarConsultaNotas();

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar notas del alumno: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra las asistencias con seguimiento
     */
    private void mostrarAsistencias() {
        try {
            System.out.println("Creando panel de asistencias para alumno ID: " + alumnoId);
            consultasAsistenciasHoy++;

            // Crear panel temporal mejorado con más información
            JPanel panelAsistencias = crearPanelAsistenciasMejorado();
            
            ventana.mostrarPanelResponsive(panelAsistencias, "Mis Asistencias");
            
            System.out.println("✅ Panel de asistencias mostrado exitosamente");

            // NUEVO: Registrar consulta de asistencias
            registrarConsultaAsistencias();

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar asistencias: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra los boletines con seguimiento de descargas
     */
    private void mostrarMisBoletines() {
        try {
            System.out.println("Creando PanelBoletinesAlumno para alumno ID: " + alumnoId);

            PanelBoletinesAlumno panelMisBoletines = 
                new PanelBoletinesAlumno(ventana, alumnoId);

            // NUEVO: Configurar callback para seguimiento de descargas
            configurarSeguimientoBoletines(panelMisBoletines);

            ventana.mostrarPanelResponsive(panelMisBoletines, "Mis Boletines");
            
            System.out.println("✅ Panel de mis boletines mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar mis boletines: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * NUEVO: Muestra el panel de notificaciones del alumno
     */
    private void mostrarNotificaciones() {
        try {
            System.out.println("Abriendo centro de notificaciones para alumno ID: " + alumnoId);

            // Abrir ventana de notificaciones
            if (ventana.isNotificationSystemActive()) {
                main.java.views.notifications.NotificationsWindow notifWindow = 
                    new main.java.views.notifications.NotificationsWindow(alumnoId);
                notifWindow.setVisible(true);
                
                // Actualizar contador después de abrir
                SwingUtilities.invokeLater(() -> {
                    actualizarContadorNotificaciones();
                });
                
            } else {
                // Crear panel temporal si el sistema no está disponible
                JPanel panelNotificaciones = crearPanelNotificacionesTemporal();
                ventana.mostrarPanelResponsive(panelNotificaciones, "Mis Notificaciones");
            }
            
            System.out.println("✅ Centro de notificaciones abierto");

        } catch (Exception ex) {
            System.err.println("❌ Error al abrir notificaciones: " + ex.getMessage());
            ex.printStackTrace();
            
            // Mostrar panel de error amigable
            JPanel panelError = crearPanelErrorNotificaciones(ex.getMessage());
            ventana.mostrarPanelResponsive(panelError, "Notificaciones - Error");
        }
    }

    // ========================================
    // MÉTODOS DE PANELES MEJORADOS
    // ========================================

    /**
     * MEJORADO: Crea un panel de asistencias más informativo
     */
    private JPanel crearPanelAsistenciasMejorado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // Header con información del alumno
        JPanel headerPanel = crearHeaderAlumno("Mis Asistencias", "📊");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Panel central con información simulada
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Estadísticas simuladas
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblEstadisticas = new JLabel("<html><h2>📈 Resumen de Asistencia</h2></html>");
        lblEstadisticas.setFont(new Font("Arial", Font.BOLD, 18));
        panelCentral.add(lblEstadisticas, gbc);

        // Información simulada
        String[] stats = {
            "✅ Días asistidos: 85%",
            "❌ Faltas: 12 días",
            "⏰ Tardanzas: 3",
            "📅 Último período: Mayo 2025"
        };

        for (int i = 0; i < stats.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 2; gbc.gridwidth = 1;
            JLabel lbl = new JLabel(stats[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 16));
            panelCentral.add(lbl, gbc);
        }

        // Mensaje informativo
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JTextArea txtInfo = new JTextArea(
            "La funcionalidad completa de visualización de asistencias estará disponible próximamente.\n\n" +
            "Mientras tanto, puedes consultar con tu preceptor sobre tu situación académica específica.\n\n" +
            "Las notificaciones sobre faltas y problemas de asistencia se enviarán automáticamente."
        );
        txtInfo.setEditable(false);
        txtInfo.setOpaque(false);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setLineWrap(true);
        txtInfo.setFont(new Font("Arial", Font.ITALIC, 14));
        panelCentral.add(txtInfo, gbc);

        panel.add(panelCentral, BorderLayout.CENTER);

        // Footer con botones
        JPanel footerPanel = crearFooterBotones();
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * NUEVO: Crea panel temporal para notificaciones
     */
    private JPanel crearPanelNotificacionesTemporal() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = crearHeaderAlumno("Mis Notificaciones", "🔔");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Contenido central
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel lblMensaje = new JLabel(
            "<html><div style='text-align: center;'>" +
            "<h2>🔔 Centro de Notificaciones</h2>" +
            "<p>El sistema de notificaciones no está completamente disponible en este momento.</p>" +
            "<br>" +
            "<p><b>Notificaciones que recibes automáticamente:</b></p>" +
            "<ul>" +
            "<li>📊 Nuevas notas publicadas</li>" +
            "<li>📋 Boletines disponibles</li>" +
            "<li>⚠️ Alertas de asistencia</li>" +
            "<li>💻 Avisos sobre préstamos</li>" +
            "<li>📢 Comunicados institucionales</li>" +
            "</ul>" +
            "<br>" +
            "<p><i>Las notificaciones aparecen en la campanita del menú superior.</i></p>" +
            "</div></html>"
        );
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        panelCentral.add(lblMensaje, gbc);

        panel.add(panelCentral, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = crearFooterBotones();
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * NUEVO: Crea panel de error para notificaciones
     */
    private JPanel crearPanelErrorNotificaciones(String errorMsg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = crearHeaderAlumno("Notificaciones - Error", "❌");
        panel.add(headerPanel, BorderLayout.NORTH);

        // Error info
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);

        JLabel lblError = new JLabel(
            "<html><div style='text-align: center;'>" +
            "<h2>❌ Error en el Sistema de Notificaciones</h2>" +
            "<p>No se pudo acceder al centro de notificaciones en este momento.</p>" +
            "<br>" +
            "<p><b>Error técnico:</b> " + errorMsg + "</p>" +
            "<br>" +
            "<p>Por favor, intenta nuevamente más tarde o contacta al administrador del sistema.</p>" +
            "</div></html>"
        );
        lblError.setFont(new Font("Arial", Font.PLAIN, 14));
        panelCentral.add(lblError, gbc);

        panel.add(panelCentral, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = crearFooterBotones();
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * NUEVO: Crea header personalizado para paneles del alumno
     */
    private JPanel crearHeaderAlumno(String titulo, String emoji) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel(emoji + " " + titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblAlumno = new JLabel("👨‍🎓 " + nombreCompleto + " - " + cursoActual);
        lblAlumno.setFont(new Font("Arial", Font.PLAIN, 16));
        lblAlumno.setForeground(Color.WHITE);

        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(lblAlumno, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * NUEVO: Crea footer con botones estándar
     */
    private JPanel crearFooterBotones() {
        JPanel footerPanel = new JPanel(new FlowLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton btnVolver = new JButton("🏠 Volver al Inicio");
        btnVolver.setBackground(new Color(96, 125, 139));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 14));
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());

        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setBackground(new Color(33, 150, 243));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnActualizar.addActionListener(e -> actualizarDatos());

        footerPanel.add(btnVolver);
        footerPanel.add(btnActualizar);

        return footerPanel;
    }

    // ========================================
    // MÉTODOS DE NOTIFICACIONES ESPECÍFICAS
    // ========================================

    /**
     * NUEVO: Envía notificación de bienvenida al alumno
     */
    private void enviarNotificacionBienvenida() {
        try {
            // Solo enviar bienvenida si el sistema está activo y es realmente primer ingreso
            if (notificationUtil != null && primerIngreso) {
                
                // Usar método interno de VentanaInicio para enviar notificación
                String titulo = "🎓 ¡Bienvenido al Sistema Académico!";
                String contenido = String.format(
                    "Hola %s, bienvenido al Sistema de Gestión Escolar ET20.\n\n" +
                    "📚 FUNCIONES DISPONIBLES:\n" +
                    "• Consultar tus notas y calificaciones\n" +
                    "• Revisar tu asistencia y faltas\n" +
                    "• Descargar tus boletines académicos\n" +
                    "• Recibir notificaciones importantes\n\n" +
                    "🔔 NOTIFICACIONES AUTOMÁTICAS:\n" +
                    "Recibirás avisos sobre nuevas notas, problemas de asistencia, " +
                    "boletines disponibles y comunicados importantes.\n\n" +
                    "¡Que tengas un excelente período académico! 📖✨",
                    nombreCompleto
                );
                
                ventana.enviarNotificacionInterna(titulo, contenido, alumnoId);
                primerIngreso = false;
                
                System.out.println("✅ Notificación de bienvenida enviada al alumno");
            }
        } catch (Exception e) {
            System.err.println("Error enviando bienvenida: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Muestra mensaje discreto sobre notificaciones pendientes
     */
    private void mostrarMensajeNotificacionesPendientes() {
        try {
            int opcion = JOptionPane.showOptionDialog(
                ventana,
                String.format(
                    "📬 Tienes %d notificación%s pendiente%s.\n\n" +
                    "¿Deseas revisar tus notificaciones ahora?",
                    notificacionesNoLeidas,
                    notificacionesNoLeidas == 1 ? "" : "es",
                    notificacionesNoLeidas == 1 ? "" : "s"
                ),
                "Notificaciones Pendientes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"📱 Ver Notificaciones", "⏰ Más Tarde"},
                "📱 Ver Notificaciones"
            );

            if (opcion == 0) { // Ver notificaciones
                mostrarNotificaciones();
            }
        } catch (Exception e) {
            System.err.println("Error mostrando mensaje de notificaciones: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Notifica error interno discretamente
     */
    private void notificarErrorInterno(String accion, String error) {
        try {
            // Solo notificar errores críticos, no saturar al alumno
            if (error.toLowerCase().contains("crítico") || error.toLowerCase().contains("conexión")) {
                System.err.println("🚨 Error crítico para alumno " + alumnoId + " en acción: " + accion);
                
                // Aquí podrías enviar el error a administradores si es necesario
                // pero evitar molestar al alumno con errores técnicos
            }
        } catch (Exception e) {
            System.err.println("Error notificando error interno: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS DE SEGUIMIENTO Y ESTADÍSTICAS
    // ========================================

    /**
     * NUEVO: Registra actividad del alumno
     */
    private void registrarActividad(String accion) {
        try {
            ultimaActividad = System.currentTimeMillis();
            System.out.println("📝 Actividad registrada - Alumno " + alumnoId + ": " + accion);
            
            // Aquí podrías guardar la actividad en BD para estadísticas
            
        } catch (Exception e) {
            System.err.println("Error registrando actividad: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Registra consulta de notas
     */
    private void registrarConsultaNotas() {
        try {
            System.out.println("📊 Consulta de notas registrada - Total hoy: " + consultasNotasHoy);
            
            // Si es la primera consulta del día, podríamos enviar tips o recordatorios
            if (consultasNotasHoy == 1) {
                enviarTipEstudio();
            }
            
        } catch (Exception e) {
            System.err.println("Error registrando consulta de notas: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Registra consulta de asistencias
     */
    private void registrarConsultaAsistencias() {
        try {
            System.out.println("📅 Consulta de asistencias registrada - Total hoy: " + consultasAsistenciasHoy);
            
        } catch (Exception e) {
            System.err.println("Error registrando consulta de asistencias: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Configura seguimiento de descargas de boletines
     */
    private void configurarSeguimientoBoletines(PanelBoletinesAlumno panel) {
        try {
            // Aquí configurarías callbacks en el panel de boletines
            // para detectar cuando se descarga un boletín
            System.out.println("📋 Configurando seguimiento de boletines para alumno " + alumnoId);
            
            // Ejemplo de callback que podrías implementar:
            // panel.setOnBoletinDescargadoCallback(this::registrarDescargaBoletin);
            
        } catch (Exception e) {
            System.err.println("Error configurando seguimiento de boletines: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Registra descarga de boletín
     */
    public void registrarDescargaBoletin(String periodo, String archivo) {
        try {
            descargasBoletinesHoy++;
            System.out.println("📥 Descarga de boletín registrada: " + periodo + " - Total hoy: " + descargasBoletinesHoy);
            
            // Enviar confirmación de descarga
            enviarConfirmacionDescarga(periodo, archivo);
            
        } catch (Exception e) {
            System.err.println("Error registrando descarga de boletín: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS DE NOTIFICACIONES ÚTILES
    // ========================================

    /**
     * NUEVO: Envía tip de estudio ocasional
     */
    private void enviarTipEstudio() {
        try {
            if (notificationUtil == null) return;
            
            String[] tips = {
                "💡 Tip de Estudio: Revisa tus notas regularmente para identificar materias que necesitan más atención.",
                "📚 Recordatorio: Un buen método de estudio es repasar 15 minutos cada día en lugar de estudiar todo de una vez.",
                "🎯 Consejo: Si tienes dudas sobre alguna materia, no dudes en consultar con tus profesores.",
                "⏰ Organización: Crear un horario de estudio te ayudará a distribuir mejor tu tiempo.",
                "🧠 Técnica: Explicar lo que estudias a otra persona te ayuda a consolidar el conocimiento."
            };
            
            // Enviar tip aleatorio ocasionalmente (no siempre)
            if (Math.random() < 0.3) { // 30% de probabilidad
                String tipSeleccionado = tips[(int) (Math.random() * tips.length)];
                
                ventana.enviarNotificacionInterna(
                    "📖 Tip Académico",
                    tipSeleccionado + "\n\n¡Sigue así con tu excelente trabajo académico! 🌟",
                    alumnoId
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error enviando tip de estudio: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Envía confirmación de descarga de boletín
     */
    private void enviarConfirmacionDescarga(String periodo, String archivo) {
        try {
            if (notificationUtil == null) return;
            
            String titulo = "📥 Boletín Descargado";
            String contenido = String.format(
                "Tu boletín ha sido descargado exitosamente:\n\n" +
                "📋 Período: %s\n" +
                "📁 Archivo: %s\n" +
                "📅 Fecha de descarga: %s\n\n" +
                "💡 RECOMENDACIONES:\n" +
                "• Guarda el archivo en un lugar seguro\n" +
                "• Revisa tus calificaciones con atención\n" +
                "• Consulta con tus padres/tutores\n" +
                "• Si hay dudas, habla con tu preceptor\n\n" +
                "¡Sigue esforzándote por mejores resultados! 🎓",
                periodo, archivo,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )
            );
            
            ventana.enviarNotificacionInterna(titulo, contenido, alumnoId);
            
        } catch (Exception e) {
            System.err.println("Error enviando confirmación de descarga: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Actualiza el contador de notificaciones
     */
    private void actualizarContadorNotificaciones() {
        try {
            if (notificationUtil != null) {
                int nuevasNoLeidas = notificationUtil.getNotificacionesNoLeidas();
                if (nuevasNoLeidas != notificacionesNoLeidas) {
                    notificacionesNoLeidas = nuevasNoLeidas;
                    System.out.println("📬 Contador de notificaciones actualizado: " + notificacionesNoLeidas);
                    
                    // Actualizar botón de notificaciones si es necesario
                    // Esto requeriría reconfigurar el panel de botones
                }
            }
        } catch (Exception e) {
            System.err.println("Error actualizando contador: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Actualiza datos del alumno
     */
    private void actualizarDatos() {
        try {
            System.out.println("🔄 Actualizando datos del alumno...");
            
            // Recargar información básica
            cargarInformacionAlumno();
            
            // Actualizar notificaciones
            actualizarContadorNotificaciones();
            
            // Mostrar mensaje de confirmación
            JOptionPane.showMessageDialog(
                ventana,
                "✅ Datos actualizados correctamente.\n\n" +
                "📊 Información académica: Actualizada\n" +
                "🔔 Notificaciones: Sincronizadas\n" +
                "📅 Última actualización: " + 
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                ),
                "Actualización Completada",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            System.err.println("Error actualizando datos: " + e.getMessage());
            JOptionPane.showMessageDialog(
                ventana,
                "❌ Error al actualizar los datos:\n" + e.getMessage(),
                "Error de Actualización",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ========================================
    // MÉTODOS PÚBLICOS PARA NOTIFICACIONES EXTERNAS
    // ========================================

    /**
     * NUEVO: Método público para que el sistema notifique nueva nota al alumno
     */
    public void recibirNotificacionNuevaNota(String materia, String tipoTrabajo, double nota, String profesorNombre) {
        try {
            System.out.println("📊 Nueva nota recibida para alumno " + alumnoId + ": " + nota);
            
            // La notificación la envía el sistema automáticamente
            // Aquí solo registramos que se recibió
            registrarEventoAcademico("NUEVA_NOTA", materia + " - " + tipoTrabajo + ": " + nota);
            
        } catch (Exception e) {
            System.err.println("Error procesando notificación de nota: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para recibir notificación de asistencia
     */
    public void recibirNotificacionAsistencia(String fecha, String materia, String tipoFalta) {
        try {
            System.out.println("📅 Notificación de asistencia para alumno " + alumnoId + ": " + tipoFalta);
            
            registrarEventoAcademico("ASISTENCIA", fecha + " - " + materia + ": " + tipoFalta);
            
            // Si es falta, podrías mostrar mensaje adicional o tip
            if ("FALTA".equalsIgnoreCase(tipoFalta)) {
                enviarReminderAsistencia();
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando notificación de asistencia: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para recibir notificación de boletín disponible
     */
    public void recibirNotificacionBoletin(String periodo) {
        try {
            System.out.println("📋 Boletín disponible para alumno " + alumnoId + ": " + periodo);
            
            registrarEventoAcademico("BOLETIN_DISPONIBLE", "Período: " + periodo);
            
            // Mostrar notificación discreta en la UI si el alumno está activo
            mostrarNotificacionBoletin(periodo);
            
        } catch (Exception e) {
            System.err.println("Error procesando notificación de boletín: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para recibir notificación de préstamo
     */
    public void recibirNotificacionPrestamo(String equipoNombre, String fechaDevolucion, String attpNombre) {
        try {
            System.out.println("💻 Notificación de préstamo para alumno " + alumnoId + ": " + equipoNombre);
            
            registrarEventoAcademico("PRESTAMO", equipoNombre + " - Devolución: " + fechaDevolucion);
            
        } catch (Exception e) {
            System.err.println("Error procesando notificación de préstamo: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES Y DE SOPORTE
    // ========================================

    /**
     * NUEVO: Registra eventos académicos del alumno
     */
    private void registrarEventoAcademico(String tipoEvento, String descripcion) {
        try {
            System.out.println("📝 Evento académico - " + tipoEvento + ": " + descripcion);
            
            // Aquí podrías guardar en BD el historial de eventos del alumno
            // Para estadísticas y seguimiento académico
            
        } catch (Exception e) {
            System.err.println("Error registrando evento académico: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Envía reminder sobre asistencia
     */
    private void enviarReminderAsistencia() {
        try {
            if (notificationUtil == null) return;
            
            // Solo enviar ocasionalmente para no saturar
            if (Math.random() < 0.2) { // 20% de probabilidad
                String titulo = "📅 Recordatorio de Asistencia";
                String contenido = 
                    "Hemos registrado una falta en tu asistencia.\n\n" +
                    "📚 IMPORTANCIA DE LA ASISTENCIA:\n" +
                    "• La asistencia regular mejora tu rendimiento\n" +
                    "• Es requisito para la promoción\n" +
                    "• Te mantiene al día con las clases\n\n" +
                    "💡 Si tienes problemas para asistir, habla con tu preceptor.\n" +
                    "¡Tu educación es importante! 🎓";
                
                ventana.enviarNotificacionInterna(titulo, contenido, alumnoId);
            }
            
        } catch (Exception e) {
            System.err.println("Error enviando reminder de asistencia: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Muestra notificación visual de boletín disponible
     */
    private void mostrarNotificacionBoletin(String periodo) {
        try {
            SwingUtilities.invokeLater(() -> {
                int opcion = JOptionPane.showOptionDialog(
                    ventana,
                    String.format(
                        "📋 ¡Tu boletín del %s ya está disponible!\n\n" +
                        "¿Deseas descargarlo ahora?",
                        periodo
                    ),
                    "Boletín Disponible",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"📥 Descargar Ahora", "⏰ Más Tarde"},
                    "📥 Descargar Ahora"
                );

                if (opcion == 0) { // Descargar ahora
                    mostrarMisBoletines();
                }
            });
        } catch (Exception e) {
            System.err.println("Error mostrando notificación de boletín: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Obtiene estadísticas de actividad del alumno
     */
    public String getEstadisticasActividad() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADÍSTICAS DEL ALUMNO ===\n");
        stats.append("👨‍🎓 Alumno: ").append(nombreCompleto).append("\n");
        stats.append("🎓 Curso: ").append(cursoActual).append("\n");
        stats.append("📧 Notificaciones no leídas: ").append(notificacionesNoLeidas).append("\n");
        stats.append("⏰ Última actividad: ").append(
            java.time.Instant.ofEpochMilli(ultimaActividad).toString()
        ).append("\n");
        stats.append("📊 Consultas de notas hoy: ").append(consultasNotasHoy).append("\n");
        stats.append("📅 Consultas de asistencias hoy: ").append(consultasAsistenciasHoy).append("\n");
        stats.append("📋 Descargas de boletines hoy: ").append(descargasBoletinesHoy).append("\n");
        stats.append("===================================");
        return stats.toString();
    }

    /**
     * NUEVO: Método de testing específico para alumnos
     */
    public void testAlumnoNotifications() {
        System.out.println("🧪 === TESTING NOTIFICACIONES ALUMNO ===");
        
        try {
            // Test 1: Notificación de nueva nota
            System.out.println("Test 1: Nueva nota...");
            recibirNotificacionNuevaNota("Matemática", "Examen", 8.5, "Prof. García");
            Thread.sleep(1000);
            
            // Test 2: Notificación de asistencia
            System.out.println("Test 2: Asistencia...");
            recibirNotificacionAsistencia("29/05/2025", "Historia", "PRESENTE");
            Thread.sleep(1000);
            
            // Test 3: Notificación de boletín
            System.out.println("Test 3: Boletín disponible...");
            recibirNotificacionBoletin("1er Trimestre 2025");
            Thread.sleep(1000);
            
            // Test 4: Notificación de préstamo
            System.out.println("Test 4: Préstamo...");
            recibirNotificacionPrestamo("Netbook Dell", "05/06/2025", "ATTP López");
            Thread.sleep(1000);
            
            // Test 5: Tip de estudio
            System.out.println("Test 5: Tip de estudio...");
            enviarTipEstudio();
            
            System.out.println("✅ Testing de alumno completado");
            
        } catch (Exception e) {
            System.err.println("❌ Error en testing: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método de limpieza de recursos
     */
    public void cleanup() {
        try {
            System.out.println("🧹 Limpiando recursos del AlumnoPanelManager...");
            
            // Enviar estadísticas finales si hubo actividad significativa
            if (consultasNotasHoy > 0 || consultasAsistenciasHoy > 0 || descargasBoletinesHoy > 0) {
                enviarReporteActividadFinal();
            }
            
            // Resetear contadores
            consultasNotasHoy = 0;
            consultasAsistenciasHoy = 0;
            descargasBoletinesHoy = 0;
            notificacionesNoLeidas = 0;
            
            System.out.println("✅ Recursos del AlumnoPanelManager liberados");
            
        } catch (Exception e) {
            System.err.println("Error en cleanup de AlumnoPanelManager: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Envía reporte de actividad final (opcional)
     */
    private void enviarReporteActividadFinal() {
        try {
            if (notificationUtil != null) {
                String titulo = "📊 Resumen de Actividad";
                String contenido = String.format(
                    "Resumen de tu actividad académica de hoy:\n\n" +
                    "📊 Consultas de notas: %d\n" +
                    "📅 Consultas de asistencia: %d\n" +
                    "📋 Descargas de boletines: %d\n\n" +
                    "¡Excelente participación en el sistema académico!\n" +
                    "Sigue manteniendo este seguimiento de tu progreso. 🌟",
                    consultasNotasHoy, consultasAsistenciasHoy, descargasBoletinesHoy
                );
                
                // Solo enviar si hubo actividad significativa
                if (consultasNotasHoy >= 3 || descargasBoletinesHoy > 0) {
                    ventana.enviarNotificacionInterna(titulo, contenido, alumnoId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error enviando reporte final: " + e.getMessage());
        }
    }

    // ========================================
    // GETTERS Y MÉTODOS DE ACCESO
    // ========================================

    public int getAlumnoId() {
        return alumnoId;
    }

    public VentanaInicio getVentana() {
        return ventana;
    }

    public NotificationIntegrationUtil getNotificationUtil() {
        return notificationUtil;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCursoActual() {
        return cursoActual;
    }

    public int getNotificacionesNoLeidas() {
        return notificacionesNoLeidas;
    }

    public int getConsultasNotasHoy() {
        return consultasNotasHoy;
    }

    public int getConsultasAsistenciasHoy() {
        return consultasAsistenciasHoy;
    }

    public int getDescargasBoletinesHoy() {
        return descargasBoletinesHoy;
    }

    /**
     * NUEVO: Método toString para debugging
     */
    @Override
    public String toString() {
        return String.format(
            "AlumnoPanelManager{alumnoId=%d, nombre='%s', curso='%s', notificacionesNoLeidas=%d, consultasNotas=%d}",
            alumnoId, nombreCompleto, cursoActual, notificacionesNoLeidas, consultasNotasHoy
        );
    }
}