package main.java.views.users.Profesor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import main.java.services.NotificationCore.NotificationIntegrationUtil;

/**
 * Panel académico avanzado de notificaciones específico para profesores.
 * Incluye gestión de notas, asistencias, recordatorios y comunicación con
 * alumnos.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class ProfesorAcademicNotificationsPanel extends JPanel {

    // ===============================================
    // ATRIBUTOS Y CONFIGURACIÓN
    // ===============================================
    private final int profesorId;
    private final Connection connection;
    private final NotificationIntegrationUtil notificationUtil;

    // Componentes UI principales
    private JTabbedPane tabbedPane;
    private JPanel panelNotasRapidas;
    private JPanel panelComunicacionMasiva;
    private JPanel panelSeguimientoAcademico;
    private JPanel panelEstadisticasProfesor;

    // Componentes de notas rápidas
    private JComboBox<String> comboMaterias;
    private JTextField txtAlumnoId;
    private JTextField txtTipoTrabajo;
    private JTextField txtNota;
    private JComboBox<String> comboBimestre;
    private JTextArea txtObservaciones;

    // Componentes de comunicación masiva
    private JComboBox<String> comboDestinatarios;
    private JComboBox<String> comboTipoMensaje;
    private JTextField txtTituloMensaje;
    private JTextArea txtContenidoMensaje;

    // Componentes de seguimiento
    private JTable tablaTrabajosPendientes;
    private DefaultTableModel modeloTrabajosPendientes;
    private JTable tablaNotificacionesEnviadas;
    private DefaultTableModel modeloNotificacionesEnviadas;

    // Datos
    private Map<String, MateriaInfo> materiasMap;
    private List<EstudianteInfo> estudiantesList;

    // ===============================================
    // CONSTRUCTOR E INICIALIZACIÓN
    // ===============================================
    public ProfesorAcademicNotificationsPanel(int profesorId) {
        this.profesorId = profesorId;
        this.connection = Conexion.getInstancia().verificarConexion();
        this.notificationUtil = NotificationIntegrationUtil.getInstance();
        this.materiasMap = new HashMap<>();
        this.estudiantesList = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadInitialData();

        System.out.println("✅ ProfesorAcademicNotificationsPanel inicializado para usuario: " + profesorId);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Crear pestañas principales
        tabbedPane = new JTabbedPane();

        // Pestaña 1: Notas Rápidas
        panelNotasRapidas = createNotasRapidasPanel();
        tabbedPane.addTab("📊 Notas Rápidas", panelNotasRapidas);

        // Pestaña 2: Comunicación Masiva
        panelComunicacionMasiva = createComunicacionMasivaPanel();
        tabbedPane.addTab("📢 Comunicación Masiva", panelComunicacionMasiva);

        // Pestaña 3: Seguimiento Académico
        panelSeguimientoAcademico = createSeguimientoAcademicoPanel();
        tabbedPane.addTab("📈 Seguimiento Académico", panelSeguimientoAcademico);

        // Pestaña 4: Estadísticas del Profesor
        panelEstadisticasProfesor = createEstadisticasProfesorPanel();
        tabbedPane.addTab("📊 Mis Estadísticas", panelEstadisticasProfesor);
    }

    private void setupLayout() {
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // Listener para cambio de tipo de mensaje
        comboTipoMensaje.addActionListener(e -> actualizarPlantillaMensaje());
    }

    private void loadInitialData() {
        // Cargar materias
        cargarMaterias();

        // Cargar destinatarios
        cargarDestinatarios();

        // Cargar datos iniciales
        actualizarTrabajosPendientes();
        actualizarNotificacionesEnviadas();
        actualizarEstadisticas();
    }

    // ===============================================
    // CREACIÓN DE PANELES - PESTAÑA 1: NOTAS RÁPIDAS
    // ===============================================
    private JPanel createNotasRapidasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        JLabel lblTitulo = new JLabel("📊 Registro Rápido de Notas y Notificación", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(51, 153, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(lblTitulo, gbc);

        // Selector de materia
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Materia:"), gbc);

        comboMaterias = new JComboBox<>();
        comboMaterias.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        mainPanel.add(comboMaterias, gbc);

        // ID del alumno
        gbc.gridx = 2;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("ID Alumno:"), gbc);

        txtAlumnoId = new JTextField(10);
        txtAlumnoId.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 3;
        mainPanel.add(txtAlumnoId, gbc);

        // Tipo de trabajo
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Tipo de Trabajo:"), gbc);

        txtTipoTrabajo = new JTextField(15);
        txtTipoTrabajo.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        mainPanel.add(txtTipoTrabajo, gbc);

        // Nota
        gbc.gridx = 2;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Nota:"), gbc);

        txtNota = new JTextField(5);
        txtNota.setPreferredSize(new Dimension(80, 30));
        gbc.gridx = 3;
        mainPanel.add(txtNota, gbc);

        // Bimestre (para notas bimestrales)
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Bimestre:"), gbc);

        comboBimestre = new JComboBox<>(new String[]{
            "No aplica", "1er Bimestre", "2do Bimestre", "3er Bimestre", "4to Bimestre"
        });
        comboBimestre.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        mainPanel.add(comboBimestre, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Observaciones:"), gbc);

        txtObservaciones = new JTextArea(3, 30);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(BorderFactory.createLoweredBevelBorder());
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(400, 80));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollObs, gbc);

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnRegistrarNotificar = new JButton("📊 Registrar y Notificar");
        btnRegistrarNotificar.setPreferredSize(new Dimension(180, 40));
        btnRegistrarNotificar.setBackground(new Color(40, 167, 69));
        btnRegistrarNotificar.setForeground(Color.WHITE);
        btnRegistrarNotificar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrarNotificar.addActionListener(e -> registrarYNotificarNota());

        JButton btnSoloNotificar = new JButton("📤 Solo Notificar");
        btnSoloNotificar.setPreferredSize(new Dimension(140, 40));
        btnSoloNotificar.setBackground(new Color(0, 123, 255));
        btnSoloNotificar.setForeground(Color.WHITE);
        btnSoloNotificar.setFont(new Font("Arial", Font.BOLD, 14));
        btnSoloNotificar.addActionListener(e -> soloNotificarNota());

        JButton btnLimpiar = new JButton("🧹 Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(100, 40));
        btnLimpiar.setBackground(new Color(108, 117, 125));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimpiar.addActionListener(e -> limpiarFormularioNotas());

        buttonPanel.add(btnRegistrarNotificar);
        buttonPanel.add(btnSoloNotificar);
        buttonPanel.add(btnLimpiar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        panel.add(mainPanel, BorderLayout.NORTH);

        // Panel de ayuda
        JPanel helpPanel = createHelpPanel();
        panel.add(helpPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("💡 Ayuda y Consejos"));

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setBackground(new Color(248, 249, 250));
        helpText.setFont(new Font("Arial", Font.PLAIN, 12));
        helpText.setText(
                "📌 CONSEJOS PARA USO EFECTIVO:\n\n"
                + "• ID del Alumno: Puedes encontrarlo en el sistema de gestión de alumnos\n"
                + "• Tipo de Trabajo: Especifica claramente (ej: 'Examen Parcial', 'TP N°1')\n"
                + "• Observaciones: Incluye comentarios constructivos para el alumno\n"
                + "• Bimestre: Selecciona solo para notas bimestrales finales\n\n"
                + "🔔 NOTIFICACIONES AUTOMÁTICAS:\n"
                + "• Los alumnos reciben notificación inmediata al publicar notas\n"
                + "• Se incluyen detalles de la materia y tipo de evaluación\n"
                + "• Las notas bajas incluyen mensajes de apoyo automáticamente\n\n"
                + "⚡ FUNCIONES RÁPIDAS:\n"
                + "• 'Registrar y Notificar': Guarda en BD y envía notificación\n"
                + "• 'Solo Notificar': Envía notificación sin guardar en BD\n"
                + "• Usar para comunicar notas ya cargadas en otro sistema"
        );

        JScrollPane scrollHelp = new JScrollPane(helpText);
        panel.add(scrollHelp, BorderLayout.CENTER);

        return panel;
    }

    // ===============================================
    // CREACIÓN DE PANELES - PESTAÑA 2: COMUNICACIÓN MASIVA
    // ===============================================
    private JPanel createComunicacionMasivaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        JLabel lblTitulo = new JLabel("📢 Comunicación Masiva con Alumnos", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(40, 167, 69));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(lblTitulo, gbc);

        // Destinatarios
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Enviar a:"), gbc);

        comboDestinatarios = new JComboBox<>();
        comboDestinatarios.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        formPanel.add(comboDestinatarios, gbc);

        // Tipo de mensaje
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Tipo de Mensaje:"), gbc);

        comboTipoMensaje = new JComboBox<>(new String[]{
            "Aviso General", "Examen/Evaluación", "Trabajo Práctico", "Recordatorio",
            "Felicitaciones", "Llamado de Atención", "Información Académica"
        });
        comboTipoMensaje.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        formPanel.add(comboTipoMensaje, gbc);

        // Título del mensaje
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Título:"), gbc);

        txtTituloMensaje = new JTextField(30);
        txtTituloMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtTituloMensaje, gbc);

        // Contenido del mensaje
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Contenido:"), gbc);

        txtContenidoMensaje = new JTextArea(8, 40);
        txtContenidoMensaje.setLineWrap(true);
        txtContenidoMensaje.setWrapStyleWord(true);
        txtContenidoMensaje.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollContenido = new JScrollPane(txtContenidoMensaje);
        scrollContenido.setPreferredSize(new Dimension(450, 200));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(scrollContenido, gbc);

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnEnviar = new JButton("📨 Enviar Mensaje");
        btnEnviar.setPreferredSize(new Dimension(160, 40));
        btnEnviar.setBackground(new Color(40, 167, 69));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEnviar.addActionListener(e -> enviarMensajeMasivo());

        JButton btnPrevisualizar = new JButton("👁️ Vista Previa");
        btnPrevisualizar.setPreferredSize(new Dimension(140, 40));
        btnPrevisualizar.setBackground(new Color(0, 123, 255));
        btnPrevisualizar.setForeground(Color.WHITE);
        btnPrevisualizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrevisualizar.addActionListener(e -> mostrarVistaPrevia());

        JButton btnLimpiarMensaje = new JButton("🧹 Limpiar");
        btnLimpiarMensaje.setPreferredSize(new Dimension(100, 40));
        btnLimpiarMensaje.setBackground(new Color(108, 117, 125));
        btnLimpiarMensaje.setForeground(Color.WHITE);
        btnLimpiarMensaje.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimpiarMensaje.addActionListener(e -> limpiarFormularioMensaje());

        buttonPanel.add(btnPrevisualizar);
        buttonPanel.add(btnEnviar);
        buttonPanel.add(btnLimpiarMensaje);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    // ===============================================
    // CREACIÓN DE PANELES - PESTAÑA 3: SEGUIMIENTO ACADÉMICO
    // ===============================================
    private JPanel createSeguimientoAcademicoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Título
        JLabel titulo = new JLabel("📈 Seguimiento Académico y Notificaciones", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titulo, BorderLayout.NORTH);

        // Panel con pestañas internas
        JTabbedPane innerTabs = new JTabbedPane();

        // Pestaña: Trabajos Pendientes
        JPanel trabajosPanel = new JPanel(new BorderLayout());

        // Tabla de trabajos pendientes
        String[] columnasTrab = {"Materia", "Curso", "Tipo", "Cantidad", "Fecha Límite", "Estado"};
        modeloTrabajosPendientes = new DefaultTableModel(columnasTrab, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaTrabajosPendientes = new JTable(modeloTrabajosPendientes);
        tablaTrabajosPendientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTrabajosPendientes.setRowHeight(25);

        JScrollPane scrollTrabajos = new JScrollPane(tablaTrabajosPendientes);
        scrollTrabajos.setPreferredSize(new Dimension(0, 200));

        JPanel buttonTrabPanel = new JPanel(new FlowLayout());
        JButton btnActualizarTrab = new JButton("🔄 Actualizar");
        btnActualizarTrab.addActionListener(e -> actualizarTrabajosPendientes());

        JButton btnNotificarTrab = new JButton("📤 Recordar Pendientes");
        btnNotificarTrab.addActionListener(e -> recordarTrabajosPendientes());

        buttonTrabPanel.add(btnActualizarTrab);
        buttonTrabPanel.add(btnNotificarTrab);

        trabajosPanel.add(scrollTrabajos, BorderLayout.CENTER);
        trabajosPanel.add(buttonTrabPanel, BorderLayout.SOUTH);

        innerTabs.addTab("📝 Trabajos Pendientes", trabajosPanel);

        // Pestaña: Notificaciones Enviadas
        JPanel notifPanel = new JPanel(new BorderLayout());

        String[] columnasNotif = {"Fecha", "Destinatario", "Tipo", "Título", "Estado", "Leída"};
        modeloNotificacionesEnviadas = new DefaultTableModel(columnasNotif, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaNotificacionesEnviadas = new JTable(modeloNotificacionesEnviadas);
        tablaNotificacionesEnviadas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaNotificacionesEnviadas.setRowHeight(25);

        JScrollPane scrollNotif = new JScrollPane(tablaNotificacionesEnviadas);
        scrollNotif.setPreferredSize(new Dimension(0, 200));

        JPanel buttonNotifPanel = new JPanel(new FlowLayout());
        JButton btnActualizarNotif = new JButton("🔄 Actualizar");
        btnActualizarNotif.addActionListener(e -> actualizarNotificacionesEnviadas());

        JButton btnExportarNotif = new JButton("📊 Exportar");
        btnExportarNotif.addActionListener(e -> exportarNotificaciones());

        buttonNotifPanel.add(btnActualizarNotif);
        buttonNotifPanel.add(btnExportarNotif);

        notifPanel.add(scrollNotif, BorderLayout.CENTER);
        notifPanel.add(buttonNotifPanel, BorderLayout.SOUTH);

        innerTabs.addTab("📤 Notificaciones Enviadas", notifPanel);

        panel.add(innerTabs, BorderLayout.CENTER);

        return panel;
    }

    // ===============================================
    // CREACIÓN DE PANELES - PESTAÑA 4: ESTADÍSTICAS
    // ===============================================
    private JPanel createEstadisticasProfesorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Título
        JLabel titulo = new JLabel("📊 Panel de Control del Profesor", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(new Color(51, 153, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        // Panel de métricas
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        metricsPanel.setBorder(new TitledBorder("Métricas Académicas"));

        JLabel lblMaterias = createMetricLabel("Materias Asignadas", "0", new Color(51, 153, 255));
        JLabel lblAlumnos = createMetricLabel("Total Alumnos", "0", new Color(40, 167, 69));
        JLabel lblNotificaciones = createMetricLabel("Notif. Enviadas", "0", new Color(255, 193, 7));
        JLabel lblTrabajosPend = createMetricLabel("Trabajos Pendientes", "0", new Color(220, 53, 69));
        JLabel lblTasaRespuesta = createMetricLabel("Tasa de Respuesta", "0%", new Color(111, 66, 193));
        JLabel lblPromedio = createMetricLabel("Promedio General", "0.0", new Color(23, 162, 184));

        metricsPanel.add(lblMaterias);
        metricsPanel.add(lblAlumnos);
        metricsPanel.add(lblNotificaciones);
        metricsPanel.add(lblTrabajosPend);
        metricsPanel.add(lblTasaRespuesta);
        metricsPanel.add(lblPromedio);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(metricsPanel, gbc);

        // Panel de acciones rápidas
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        quickActionsPanel.setBorder(new TitledBorder("Acciones Rápidas"));

        JButton btnNotifGeneral = new JButton("📢 Aviso General");
        btnNotifGeneral.setFont(new Font("Arial", Font.BOLD, 12));
        btnNotifGeneral.addActionListener(e -> crearAvisoGeneral());

        JButton btnRecordarExamen = new JButton("📝 Recordar Examen");
        btnRecordarExamen.setFont(new Font("Arial", Font.BOLD, 12));
        btnRecordarExamen.addActionListener(e -> recordarExamen());

        JButton btnFelicitar = new JButton("🎉 Felicitar Alumnos");
        btnFelicitar.setFont(new Font("Arial", Font.BOLD, 12));
        btnFelicitar.addActionListener(e -> felicitarAlumnos());

        JButton btnReporte = new JButton("📋 Generar Reporte");
        btnReporte.setFont(new Font("Arial", Font.BOLD, 12));
        btnReporte.addActionListener(e -> generarReporteCompleto());

        quickActionsPanel.add(btnNotifGeneral);
        quickActionsPanel.add(btnRecordarExamen);
        quickActionsPanel.add(btnFelicitar);
        quickActionsPanel.add(btnReporte);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(quickActionsPanel, gbc);

        return panel;
    }

    private JLabel createMetricLabel(String titulo, String valor, Color color) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(8, 8, 8, 8)
        ));
        container.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel(titulo, JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 10));
        lblTitulo.setForeground(color);

        JLabel lblValor = new JLabel(valor, JLabel.CENTER);
        lblValor.setFont(new Font("Arial", Font.BOLD, 20));
        lblValor.setForeground(color);

        container.add(lblTitulo, BorderLayout.NORTH);
        container.add(lblValor, BorderLayout.CENTER);

        return lblValor; // Retornamos el label del valor para poder actualizarlo
    }

    // ===============================================
    // MÉTODOS DE CARGA DE DATOS
    // ===============================================
    private void cargarMaterias() {
        try {
            comboMaterias.removeAllItems();
            materiasMap.clear();

            String query = """
                SELECT DISTINCT 
                    c.id as curso_id, 
                    CONCAT(c.anio, '°', c.division) as curso, 
                    m.id as materia_id, 
                    m.nombre as materia 
                FROM cursos c 
                JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id 
                JOIN materias m ON pcm.materia_id = m.id 
                WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' 
                ORDER BY m.nombre, c.anio, c.division
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("materia") + " - " + rs.getString("curso");
                comboMaterias.addItem(item);

                MateriaInfo info = new MateriaInfo(
                        rs.getInt("curso_id"),
                        rs.getInt("materia_id"),
                        rs.getString("curso"),
                        rs.getString("materia")
                );
                materiasMap.put(item, info);
            }

            System.out.println("✅ Materias cargadas: " + materiasMap.size());

        } catch (SQLException e) {
            System.err.println("Error cargando materias: " + e.getMessage());
        }
    }

    private void cargarDestinatarios() {
        comboDestinatarios.removeAllItems();

        // Agregar opciones de destinatarios
        comboDestinatarios.addItem("Todos mis alumnos");

        // Agregar por materia
        for (String materia : materiasMap.keySet()) {
            comboDestinatarios.addItem("Alumnos de: " + materia);
        }

        // Agregar opción individual
        comboDestinatarios.addItem("Alumno específico (usar ID)");
    }

    // ===============================================
    // MÉTODOS DE ACCIÓN - PESTAÑA 1: NOTAS RÁPIDAS
    // ===============================================
    private void registrarYNotificarNota() {
        if (!validarFormularioNota()) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Aquí iría la lógica para registrar en BD
                // Por ahora, solo simulamos el registro exitoso
                Thread.sleep(500); // Simular procesamiento

                // Enviar notificación
                return enviarNotificacionNota();
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(ProfesorAcademicNotificationsPanel.this,
                                "✅ Nota registrada y notificación enviada exitosamente",
                                "Operación Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        limpiarFormularioNotas();
                        actualizarEstadisticas();
                    } else {
                        JOptionPane.showMessageDialog(ProfesorAcademicNotificationsPanel.this,
                                "❌ Error en el proceso. Verifique los datos.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    System.err.println("Error en registro y notificación: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void soloNotificarNota() {
        if (!validarFormularioNota()) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return enviarNotificacionNota();
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(ProfesorAcademicNotificationsPanel.this,
                                "✅ Notificación enviada exitosamente",
                                "Notificación Enviada",
                                JOptionPane.INFORMATION_MESSAGE);
                        limpiarFormularioNotas();
                        actualizarEstadisticas();
                    }
                } catch (Exception e) {
                    System.err.println("Error enviando notificación: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private boolean validarFormularioNota() {
        if (comboMaterias.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una materia", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtAlumnoId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el ID del alumno", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            Integer.parseInt(txtAlumnoId.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID del alumno debe ser un número", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtTipoTrabajo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Especifique el tipo de trabajo", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtNota.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la nota", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            double nota = Double.parseDouble(txtNota.getText().trim());
            if (nota < 0 || nota > 10) {
                JOptionPane.showMessageDialog(this, "La nota debe estar entre 0 y 10", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La nota debe ser un número válido", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean enviarNotificacionNota() {
        try {
            String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
            MateriaInfo info = materiasMap.get(materiaSeleccionada);
            int alumnoId = Integer.parseInt(txtAlumnoId.getText().trim());
            String tipoTrabajo = txtTipoTrabajo.getText().trim();
            double nota = Double.parseDouble(txtNota.getText().trim());
            String bimestre = (String) comboBimestre.getSelectedItem();

            if (!"No aplica".equals(bimestre)) {
                // Es una nota bimestral
                notificationUtil.notificarNotaBimestral(alumnoId, info.materiaNombre, bimestre, nota, profesorId);
            } else {
                // Es una nota de trabajo/actividad
                notificationUtil.notificarNuevaNota(alumnoId, info.materiaNombre, tipoTrabajo, nota, profesorId);
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error enviando notificación de nota: " + e.getMessage());
            return false;
        }
    }

    private void limpiarFormularioNotas() {
        txtAlumnoId.setText("");
        txtTipoTrabajo.setText("");
        txtNota.setText("");
        txtObservaciones.setText("");
        comboBimestre.setSelectedIndex(0);
        if (comboMaterias.getItemCount() > 0) {
            comboMaterias.setSelectedIndex(0);
        }
    }

    // ===============================================
    // MÉTODOS DE ACCIÓN - PESTAÑA 2: COMUNICACIÓN MASIVA
    // ===============================================
    private void enviarMensajeMasivo() {
        if (!validarFormularioMensaje()) {
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Enviar mensaje a: " + comboDestinatarios.getSelectedItem() + "?",
                "Confirmar Envío Masivo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return procesarEnvioMasivo();
                }

                @Override
                protected void done() {
                    try {
                        boolean exito = get();
                        if (exito) {
                            JOptionPane.showMessageDialog(ProfesorAcademicNotificationsPanel.this,
                                    "✅ Mensaje enviado exitosamente",
                                    "Envío Completado",
                                    JOptionPane.INFORMATION_MESSAGE);
                            limpiarFormularioMensaje();
                            actualizarEstadisticas();
                        }
                    } catch (Exception e) {
                        System.err.println("Error en envío masivo: " + e.getMessage());
                    }
                }
            };

            worker.execute();
        }
    }

    private boolean validarFormularioMensaje() {
        if (txtTituloMensaje.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un título para el mensaje", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtContenidoMensaje.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el contenido del mensaje", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean procesarEnvioMasivo() {
        try {
            String destinatario = (String) comboDestinatarios.getSelectedItem();
            String tipoMensaje = (String) comboTipoMensaje.getSelectedItem();
            String titulo = txtTituloMensaje.getText().trim();
            String contenido = txtContenidoMensaje.getText().trim();

            // Preparar mensaje completo
            String tituloCompleto = obtenerEmojiPorTipo(tipoMensaje) + " " + titulo;
            String contenidoCompleto = formatearContenidoMensaje(contenido, tipoMensaje);

            if ("Todos mis alumnos".equals(destinatario)) {
                // Enviar a todos los alumnos del profesor
                List<Integer> todosAlumnos = obtenerTodosLosAlumnos();
                for (Integer alumnoId : todosAlumnos) {
                    notificationUtil.enviarNotificacionBasica(tituloCompleto, contenidoCompleto, alumnoId);
                }
            } else if (destinatario.startsWith("Alumnos de:")) {
                // Enviar a alumnos de una materia específica
                String materia = destinatario.substring("Alumnos de: ".length());
                MateriaInfo info = materiasMap.get(materia);
                if (info != null) {
                    List<Integer> alumnosMateria = obtenerAlumnosDeMateria(info.cursoId);
                    for (Integer alumnoId : alumnosMateria) {
                        notificationUtil.enviarNotificacionBasica(tituloCompleto, contenidoCompleto, alumnoId);
                    }
                }
            } else if ("Alumno específico (usar ID)".equals(destinatario)) {
                // Solicitar ID específico
                String idStr = JOptionPane.showInputDialog(this, "Ingrese el ID del alumno:", "ID Específico", JOptionPane.PLAIN_MESSAGE);
                if (idStr != null && !idStr.trim().isEmpty()) {
                    try {
                        int alumnoId = Integer.parseInt(idStr.trim());
                        notificationUtil.enviarNotificacionBasica(tituloCompleto, contenidoCompleto, alumnoId);
                    } catch (NumberFormatException e) {
                        throw new Exception("ID de alumno inválido: " + idStr);
                    }
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error procesando envío masivo: " + e.getMessage());
            return false;
        }
    }

    private String obtenerEmojiPorTipo(String tipo) {
        switch (tipo) {
            case "Aviso General":
                return "📢";
            case "Examen/Evaluación":
                return "📝";
            case "Trabajo Práctico":
                return "📋";
            case "Recordatorio":
                return "📌";
            case "Felicitaciones":
                return "🎉";
            case "Llamado de Atención":
                return "⚠️";
            case "Información Académica":
                return "📚";
            default:
                return "📨";
        }
    }

    private String formatearContenidoMensaje(String contenido, String tipo) {
        StringBuilder formatted = new StringBuilder();
        formatted.append(contenido).append("\n\n");
        formatted.append("👨‍🏫 Profesor: ").append(obtenerNombreProfesor()).append("\n");
        formatted.append("📅 Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        // Agregar mensaje contextual según el tipo
        switch (tipo) {
            case "Examen/Evaluación":
                formatted.append("📖 ¡Estudia y prepárate adecuadamente!");
                break;
            case "Trabajo Práctico":
                formatted.append("📋 Recuerda entregar en tiempo y forma.");
                break;
            case "Felicitaciones":
                formatted.append("🌟 ¡Sigue así, excelente trabajo!");
                break;
            case "Llamado de Atención":
                formatted.append("💪 Confío en que puedes mejorar.");
                break;
            default:
                formatted.append("📚 Para consultas adicionales, no dudes en preguntar.");
        }

        return formatted.toString();
    }

    private void mostrarVistaPrevia() {
        if (!validarFormularioMensaje()) {
            return;
        }

        String tipoMensaje = (String) comboTipoMensaje.getSelectedItem();
        String titulo = obtenerEmojiPorTipo(tipoMensaje) + " " + txtTituloMensaje.getText().trim();
        String contenido = formatearContenidoMensaje(txtContenidoMensaje.getText().trim(), tipoMensaje);

        StringBuilder preview = new StringBuilder();
        preview.append("VISTA PREVIA DEL MENSAJE\n");
        preview.append("═══════════════════════════════════════\n\n");
        preview.append("📧 DESTINATARIO: ").append(comboDestinatarios.getSelectedItem()).append("\n");
        preview.append("📝 TIPO: ").append(tipoMensaje).append("\n\n");
        preview.append("📌 TÍTULO:\n").append(titulo).append("\n\n");
        preview.append("📄 CONTENIDO:\n").append(contenido);

        JTextArea previewArea = new JTextArea(preview.toString());
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        previewArea.setRows(20);
        previewArea.setColumns(60);

        JScrollPane scrollPreview = new JScrollPane(previewArea);

        JOptionPane.showMessageDialog(this, scrollPreview,
                "Vista Previa del Mensaje",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarPlantillaMensaje() {
        String tipoSeleccionado = (String) comboTipoMensaje.getSelectedItem();

        // Plantillas predefinidas según el tipo
        switch (tipoSeleccionado) {
            case "Aviso General":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("Aviso General");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("Estimados alumnos,\n\nLes informo que...\n\nSaludos cordiales.");
                }
                break;
            case "Examen/Evaluación":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("Próximo Examen");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("Se informa que el próximo examen será:\n\nFecha: [COMPLETAR]\nHora: [COMPLETAR]\nTemario: [COMPLETAR]\n\n¡Estudien y prepárense bien!");
                }
                break;
            case "Trabajo Práctico":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("Nuevo Trabajo Práctico");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("Se asigna el siguiente trabajo práctico:\n\nTema: [COMPLETAR]\nFecha de entrega: [COMPLETAR]\nModalidad: [COMPLETAR]\n\nRecuerden entregar en tiempo y forma.");
                }
                break;
            case "Recordatorio":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("Recordatorio Importante");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("Les recuerdo que...\n\n[COMPLETAR RECORDATORIO]\n\nGracias por su atención.");
                }
                break;
            case "Felicitaciones":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("¡Felicitaciones!");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("¡Excelente trabajo!\n\nQuiero felicitarlos por...\n\n¡Sigan así, estoy muy orgulloso/a de ustedes!");
                }
                break;
            case "Llamado de Atención":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("Llamado de Atención");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("Necesito llamar su atención sobre...\n\n[EXPLICAR SITUACIÓN]\n\nConfío en que pueden mejorar y espero verlo reflejado pronto.");
                }
                break;
            case "Información Académica":
                if (txtTituloMensaje.getText().trim().isEmpty()) {
                    txtTituloMensaje.setText("Información Académica");
                }
                if (txtContenidoMensaje.getText().trim().isEmpty()) {
                    txtContenidoMensaje.setText("Les comparto la siguiente información académica:\n\n[COMPLETAR INFORMACIÓN]\n\nCualquier consulta, no duden en preguntar.");
                }
                break;
        }
    }

    private void limpiarFormularioMensaje() {
        txtTituloMensaje.setText("");
        txtContenidoMensaje.setText("");
        comboTipoMensaje.setSelectedIndex(0);
        comboDestinatarios.setSelectedIndex(0);
    }

    // ===============================================
    // MÉTODOS DE SEGUIMIENTO Y ACTUALIZACIÓN
    // ===============================================
    private void actualizarTrabajosPendientes() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                modeloTrabajosPendientes.setRowCount(0);

                try {
                    String query = """
                        SELECT m.nombre as materia, 
                               CONCAT(c.anio, '°', c.division) as curso,
                               'Notas sin publicar' as tipo,
                               COUNT(*) as cantidad,
                               MIN(n.fecha_creacion) as fecha_limite
                        FROM notas n
                        INNER JOIN profesor_curso_materia pcm ON n.curso_id = pcm.curso_id AND n.materia_id = pcm.materia_id
                        INNER JOIN materias m ON pcm.materia_id = m.id
                        INNER JOIN cursos c ON pcm.curso_id = c.id
                        WHERE pcm.profesor_id = ? 
                        AND n.nota IS NULL 
                        AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                        GROUP BY m.nombre, c.anio, c.division
                        ORDER BY fecha_limite ASC
                        """;

                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, profesorId);
                    ResultSet rs = ps.executeQuery();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    while (rs.next()) {
                        String materia = rs.getString("materia");
                        String curso = rs.getString("curso");
                        String tipo = rs.getString("tipo");
                        int cantidad = rs.getInt("cantidad");
                        String fechaLimite = rs.getTimestamp("fecha_limite").toLocalDateTime().format(formatter);
                        String estado = cantidad > 10 ? "Crítico" : cantidad > 5 ? "Alto" : "Normal";

                        Object[] fila = {materia, curso, tipo, cantidad, fechaLimite, estado};
                        modeloTrabajosPendientes.addRow(fila);
                    }

                } catch (SQLException e) {
                    System.err.println("Error actualizando trabajos pendientes: " + e.getMessage());
                }

                return null;
            }
        };

        worker.execute();
    }

    private void actualizarNotificacionesEnviadas() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                modeloNotificacionesEnviadas.setRowCount(0);

                try {
                    String query = """
                        SELECT n.fecha_creacion, n.titulo, n.prioridad,
                               COUNT(nd.id) as total_destinatarios,
                               COUNT(CASE WHEN nd.estado_lectura = 'LEIDA' THEN 1 END) as leidas,
                               CASE WHEN n.titulo LIKE '%Nota%' THEN 'Académica'
                                    WHEN n.titulo LIKE '%Examen%' THEN 'Examen'
                                    WHEN n.titulo LIKE '%Aviso%' THEN 'Aviso'
                                    ELSE 'General' END as tipo
                        FROM notificaciones n
                        LEFT JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                        WHERE n.remitente_id = ? AND n.estado = 'ACTIVA'
                        AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                        GROUP BY n.id, n.fecha_creacion, n.titulo, n.prioridad
                        ORDER BY n.fecha_creacion DESC
                        LIMIT 50
                        """;

                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, profesorId);
                    ResultSet rs = ps.executeQuery();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

                    while (rs.next()) {
                        String fecha = rs.getTimestamp("fecha_creacion").toLocalDateTime().format(formatter);
                        String titulo = rs.getString("titulo");
                        if (titulo.length() > 30) {
                            titulo = titulo.substring(0, 27) + "...";
                        }

                        int totalDest = rs.getInt("total_destinatarios");
                        int leidas = rs.getInt("leidas");
                        String tipo = rs.getString("tipo");
                        String estado = totalDest > 0 ? "Enviada" : "Pendiente";
                        String porcentajeLeido = totalDest > 0 ? String.format("%.0f%%", (leidas * 100.0 / totalDest)) : "0%";

                        Object[] fila = {fecha, totalDest + " alumnos", tipo, titulo, estado, porcentajeLeido};
                        modeloNotificacionesEnviadas.addRow(fila);
                    }

                } catch (SQLException e) {
                    System.err.println("Error actualizando notificaciones enviadas: " + e.getMessage());
                }

                return null;
            }
        };

        worker.execute();
    }

    private void actualizarEstadisticas() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Actualizar métricas en el panel de estadísticas
                // Esta implementación es básica y puede expandirse
                return null;
            }
        };

        worker.execute();
    }

    private void recordarTrabajosPendientes() {
        int selectedRow = tablaTrabajosPendientes.getSelectedRow();
        if (selectedRow >= 0) {
            String materia = (String) modeloTrabajosPendientes.getValueAt(selectedRow, 0);
            String curso = (String) modeloTrabajosPendientes.getValueAt(selectedRow, 1);
            int cantidad = (int) modeloTrabajosPendientes.getValueAt(selectedRow, 3);

            // Usar el sistema de notificaciones para recordar
            // Enviar recordatorio usando método genérico
            String titulo = "📚 Recordatorio: Trabajos pendientes";
            String contenido = String.format("Tienes %d trabajo(s) pendiente(s) de evaluar en %s", cantidad, materia);
            notificationUtil.enviarNotificacionBasica(titulo, contenido, profesorId);

            JOptionPane.showMessageDialog(this,
                    "✅ Recordatorio enviado sobre " + cantidad + " trabajos pendientes en " + materia,
                    "Recordatorio Enviado",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona una fila de la tabla.",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportarNotificaciones() {
        if (modeloNotificacionesEnviadas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay datos para exportar.",
                    "Sin datos",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Crear contenido CSV
            StringBuilder csv = new StringBuilder();

            // Encabezados
            csv.append("Fecha,Destinatario,Tipo,Título,Estado,Leída\n");

            // Datos
            for (int i = 0; i < modeloNotificacionesEnviadas.getRowCount(); i++) {
                for (int j = 0; j < modeloNotificacionesEnviadas.getColumnCount(); j++) {
                    Object valor = modeloNotificacionesEnviadas.getValueAt(i, j);
                    csv.append(valor != null ? valor.toString().replace(",", ";") : "");
                    if (j < modeloNotificacionesEnviadas.getColumnCount() - 1) {
                        csv.append(",");
                    }
                }
                csv.append("\n");
            }

            // Guardar archivo
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("notificaciones_profesor_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write(csv.toString());
                }

                JOptionPane.showMessageDialog(this,
                        "✅ Notificaciones exportadas exitosamente a:\n" + file.getAbsolutePath(),
                        "Exportación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            System.err.println("Error exportando notificaciones: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al exportar: " + e.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===============================================
    // ACCIONES RÁPIDAS DEL PANEL DE ESTADÍSTICAS
    // ===============================================
    private void crearAvisoGeneral() {
        // Cambiar a la pestaña de comunicación masiva
        tabbedPane.setSelectedIndex(1);

        // Preconfigurar formulario
        comboTipoMensaje.setSelectedItem("Aviso General");
        comboDestinatarios.setSelectedIndex(0); // Todos mis alumnos
        actualizarPlantillaMensaje();

        JOptionPane.showMessageDialog(this,
                "Se ha preconfigurado un aviso general.\nComplete los detalles y envíe.",
                "Aviso General",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void recordarExamen() {
        // Cambiar a la pestaña de comunicación masiva
        tabbedPane.setSelectedIndex(1);

        // Preconfigurar formulario
        comboTipoMensaje.setSelectedItem("Examen/Evaluación");
        actualizarPlantillaMensaje();

        JOptionPane.showMessageDialog(this,
                "Se ha preconfigurado un recordatorio de examen.\nComplete los detalles específicos.",
                "Recordatorio de Examen",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void felicitarAlumnos() {
        // Cambiar a la pestaña de comunicación masiva
        tabbedPane.setSelectedIndex(1);

        // Preconfigurar formulario
        comboTipoMensaje.setSelectedItem("Felicitaciones");
        actualizarPlantillaMensaje();

        JOptionPane.showMessageDialog(this,
                "Se ha preconfigurado un mensaje de felicitaciones.\nPersonalice el mensaje según corresponda.",
                "Felicitaciones",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void generarReporteCompleto() {
        String reporte = generarReporteProfesor();

        JTextArea textArea = new JTextArea(reporte);
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        textArea.setRows(25);
        textArea.setColumns(80);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Reporte Completo del Profesor", true);
        dialog.setLayout(new BorderLayout());

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = new JButton("Guardar Reporte");
        btnGuardar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("reporte_profesor_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt"));

            if (fileChooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                    writer.write(reporte);
                    JOptionPane.showMessageDialog(dialog, "✅ Reporte guardado exitosamente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar: " + ex.getMessage());
                }
            }
        });

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCerrar);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(750, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String generarReporteProfesor() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("                    REPORTE ACADÉMICO DEL PROFESOR\n");
        reporte.append("═══════════════════════════════════════════════════════════════════\n\n");

        reporte.append("👨‍🏫 INFORMACIÓN DEL PROFESOR:\n");
        reporte.append("─────────────────────────────────\n");
        reporte.append("ID: ").append(profesorId).append("\n");
        reporte.append("Nombre: ").append(obtenerNombreProfesor()).append("\n");
        reporte.append("Fecha del reporte: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        reporte.append("📊 ESTADÍSTICAS ACADÉMICAS:\n");
        reporte.append("─────────────────────────────\n");
        reporte.append("📚 Materias asignadas: ").append(materiasMap.size()).append("\n");
        reporte.append("👥 Total de alumnos: ").append(obtenerTotalAlumnos()).append("\n");
        reporte.append("📤 Notificaciones enviadas (último mes): ").append(modeloNotificacionesEnviadas.getRowCount()).append("\n");
        reporte.append("📝 Trabajos pendientes: ").append(modeloTrabajosPendientes.getRowCount()).append("\n\n");

        // Materias detalladas
        reporte.append("📚 MATERIAS ASIGNADAS:\n");
        reporte.append("─────────────────────\n");
        for (String materia : materiasMap.keySet()) {
            reporte.append("• ").append(materia).append("\n");
        }
        reporte.append("\n");

        // Trabajos pendientes
        if (modeloTrabajosPendientes.getRowCount() > 0) {
            reporte.append("📝 TRABAJOS PENDIENTES DE CORRECCIÓN:\n");
            reporte.append("─────────────────────────────────────\n");
            for (int i = 0; i < modeloTrabajosPendientes.getRowCount(); i++) {
                String materia = (String) modeloTrabajosPendientes.getValueAt(i, 0);
                String curso = (String) modeloTrabajosPendientes.getValueAt(i, 1);
                int cantidad = (int) modeloTrabajosPendientes.getValueAt(i, 3);
                String estado = (String) modeloTrabajosPendientes.getValueAt(i, 5);

                reporte.append("• ").append(materia).append(" (").append(curso).append("): ");
                reporte.append(cantidad).append(" trabajos - Estado: ").append(estado).append("\n");
            }
            reporte.append("\n");
        }

        // Últimas notificaciones
        if (modeloNotificacionesEnviadas.getRowCount() > 0) {
            reporte.append("📤 ÚLTIMAS NOTIFICACIONES ENVIADAS:\n");
            reporte.append("──────────────────────────────────\n");
            int maxRows = Math.min(5, modeloNotificacionesEnviadas.getRowCount());
            for (int i = 0; i < maxRows; i++) {
                String fecha = (String) modeloNotificacionesEnviadas.getValueAt(i, 0);
                String tipo = (String) modeloNotificacionesEnviadas.getValueAt(i, 2);
                String titulo = (String) modeloNotificacionesEnviadas.getValueAt(i, 3);
                String leida = (String) modeloNotificacionesEnviadas.getValueAt(i, 5);

                reporte.append("• ").append(fecha).append(" - ").append(tipo).append(": ");
                reporte.append(titulo).append(" (").append(leida).append(" leída)\n");
            }
            if (modeloNotificacionesEnviadas.getRowCount() > 5) {
                reporte.append("... y ").append(modeloNotificacionesEnviadas.getRowCount() - 5).append(" más.\n");
            }
            reporte.append("\n");
        }

        // Recomendaciones
        reporte.append("💡 RECOMENDACIONES:\n");
        reporte.append("───────────────────\n");
        if (modeloTrabajosPendientes.getRowCount() > 0) {
            reporte.append("• ⚠️ Revisar y corregir trabajos pendientes para mantener comunicación fluida.\n");
            reporte.append("• 📤 Usar el sistema de notificaciones para informar sobre nuevas notas.\n");
        } else {
            reporte.append("• ✅ Excelente gestión de correcciones. No hay trabajos pendientes.\n");
        }

        if (modeloNotificacionesEnviadas.getRowCount() > 10) {
            reporte.append("• 🌟 Muy buena comunicación con los alumnos a través de notificaciones.\n");
        } else {
            reporte.append("• 📢 Considera usar más las notificaciones para mejorar la comunicación.\n");
        }

        reporte.append("• 📊 Revisar regularmente las estadísticas de lectura de notificaciones.\n");
        reporte.append("• 🎯 Personalizar mensajes según el contexto académico.\n\n");

        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("Reporte generado automáticamente por el Sistema de Gestión Escolar ET20\n");
        reporte.append("Panel Académico de Notificaciones para Profesores v1.0\n");
        reporte.append("═══════════════════════════════════════════════════════════════════");

        return reporte.toString();
    }

    // ===============================================
    // MÉTODOS AUXILIARES
    // ===============================================
    private List<Integer> obtenerTodosLosAlumnos() {
        List<Integer> alumnos = new ArrayList<>();

        try {
            String query = """
                SELECT DISTINCT ac.alumno_id 
                FROM alumno_curso ac
                INNER JOIN profesor_curso_materia pcm ON ac.curso_id = pcm.curso_id
                WHERE pcm.profesor_id = ? AND ac.estado = 'activo' AND pcm.estado = 'activo'
                """;
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnos.add(rs.getInt("alumno_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los alumnos: " + e.getMessage());
        }

        return alumnos;
    }

    private List<Integer> obtenerAlumnosDeMateria(int cursoId) {
        List<Integer> alumnos = new ArrayList<>();

        try {
            String query = "SELECT alumno_id FROM alumno_curso WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnos.add(rs.getInt("alumno_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo alumnos de materia: " + e.getMessage());
        }

        return alumnos;
    }

    private int obtenerTotalAlumnos() {
        try {
            String query = """
                SELECT COUNT(DISTINCT ac.alumno_id) 
                FROM alumno_curso ac
                INNER JOIN profesor_curso_materia pcm ON ac.curso_id = pcm.curso_id
                WHERE pcm.profesor_id = ? AND ac.estado = 'activo' AND pcm.estado = 'activo'
                """;
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo total de alumnos: " + e.getMessage());
        }

        return 0;
    }

    private String obtenerNombreProfesor() {
        try {
            String query = "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo FROM usuarios WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre del profesor: " + e.getMessage());
        }

        return "Profesor";
    }

    // ===============================================
    // CLASES INTERNAS Y MÉTODOS DE LIMPIEZA
    // ===============================================
    // Método para limpiar recursos
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                // No cerrar la conexión ya que es compartida
            }
            System.out.println("✅ ProfesorAcademicNotificationsPanel recursos liberados");
        } catch (SQLException e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        }
    }

    // Clases internas para información
    private static class MateriaInfo {

        final int cursoId;
        final int materiaId;
        final String cursoNombre;
        final String materiaNombre;

        public MateriaInfo(int cursoId, int materiaId, String cursoNombre, String materiaNombre) {
            this.cursoId = cursoId;
            this.materiaId = materiaId;
            this.cursoNombre = cursoNombre;
            this.materiaNombre = materiaNombre;
        }
    }

    private static class EstudianteInfo {

        final int id;
        final String nombre;
        final String apellido;

        public EstudianteInfo(int id, String nombre, String apellido) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
        }

        public String getNombreCompleto() {
            return apellido + ", " + nombre;
        }
    }

    // ===============================================
    // GETTERS PARA TESTING Y ACCESO
    // ===============================================
    public int getProfesorId() {
        return profesorId;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public Map<String, MateriaInfo> getMateriasMap() {
        return new HashMap<>(materiasMap);
    }

    public NotificationIntegrationUtil getNotificationUtil() {
        return notificationUtil;
    }

    /**
     * Método toString para debugging
     */
    @Override
    public String toString() {
        return String.format(
                "ProfesorAcademicNotificationsPanel{profesorId=%d, materiasAsignadas=%d, alumnosTotal=%d}",
                profesorId,
                materiasMap.size(),
                obtenerTotalAlumnos()
        );
    }
}
