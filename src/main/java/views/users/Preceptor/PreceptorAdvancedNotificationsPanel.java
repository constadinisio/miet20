package main.java.views.users.Preceptor;

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
import main.java.utils.NotificationIntegrationUtil;
import main.java.views.notifications.NotificationsWindow;

/**
 * Panel avanzado de gestión de notificaciones específico para preceptores.
 * Incluye funcionalidades para envío masivo, plantillas y seguimiento.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class PreceptorAdvancedNotificationsPanel extends JPanel {

    private final int preceptorId;
    private final Connection connection;
    private final NotificationIntegrationUtil notificationUtil;

    // Componentes UI principales
    private JTabbedPane tabbedPane;
    private JPanel panelEnvioRapido;
    private JPanel panelPlantillas;
    private JPanel panelSeguimiento;
    private JPanel panelEstadisticas;

    // Componentes de envío rápido
    private JComboBox<String> comboCursos;
    private JComboBox<String> comboTipoNotificacion;
    private JTextField txtTitulo;
    private JTextArea txtContenido;
    private JComboBox<String> comboPrioridad;
    private JButton btnEnviar;

    // Componentes de plantillas
    private JList<String> listaPlantillas;
    private DefaultListModel<String> modeloPlantillas;
    private JTextArea txtVistaPrevia;
    private JButton btnUsarPlantilla;
    private JButton btnGuardarPlantilla;

    // Componentes de seguimiento
    private JTable tablaSeguimiento;
    private DefaultTableModel modeloSeguimiento;
    private JButton btnActualizarSeguimiento;

    // Componentes de estadísticas
    private JLabel lblNotificacionesEnviadas;
    private JLabel lblNotificacionesLeidas;
    private JLabel lblTasaLectura;
    private JButton btnGenerarReporte;

    // Datos
    private Map<String, Integer> cursosMap;
    private List<PlantillaNotificacion> plantillas;

    public PreceptorAdvancedNotificationsPanel(int preceptorId) {
        this.preceptorId = preceptorId;
        this.connection = Conexion.getInstancia().verificarConexion();
        this.notificationUtil = NotificationIntegrationUtil.getInstance();
        this.cursosMap = new HashMap<>();
        this.plantillas = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadInitialData();

        System.out.println("✅ PreceptorAdvancedNotificationsPanel inicializado para usuario: " + preceptorId);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Crear pestañas principales
        tabbedPane = new JTabbedPane();

        // Pestaña 1: Envío Rápido
        panelEnvioRapido = createEnvioRapidoPanel();
        tabbedPane.addTab("📤 Envío Rápido", panelEnvioRapido);

        // Pestaña 2: Plantillas
        panelPlantillas = createPlantillasPanel();
        tabbedPane.addTab("📋 Plantillas", panelPlantillas);

        // Pestaña 3: Seguimiento
        panelSeguimiento = createSeguimientoPanel();
        tabbedPane.addTab("📊 Seguimiento", panelSeguimiento);

        // Pestaña 4: Estadísticas
        panelEstadisticas = createEstadisticasPanel();
        tabbedPane.addTab("📈 Estadísticas", panelEstadisticas);
    }

    private JPanel createEnvioRapidoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Selector de curso
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Curso:"), gbc);

        comboCursos = new JComboBox<>();
        comboCursos.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        formPanel.add(comboCursos, gbc);

        // Tipo de notificación
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Tipo:"), gbc);

        comboTipoNotificacion = new JComboBox<>(new String[]{
            "Aviso General", "Problema de Asistencia", "Reunión de Padres",
            "Examen/Evaluación", "Evento Especial", "Recordatorio", "Urgente"
        });
        comboTipoNotificacion.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        formPanel.add(comboTipoNotificacion, gbc);

        // Prioridad
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Prioridad:"), gbc);

        comboPrioridad = new JComboBox<>(new String[]{"NORMAL", "ALTA", "URGENTE", "BAJA"});
        comboPrioridad.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        formPanel.add(comboPrioridad, gbc);

        // Título
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Título:"), gbc);

        txtTitulo = new JTextField(30);
        txtTitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtTitulo, gbc);

        // Contenido
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Contenido:"), gbc);

        txtContenido = new JTextArea(8, 40);
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setFont(new Font("Arial", Font.PLAIN, 12));
        txtContenido.setBorder(BorderFactory.createLoweredBevelBorder());

        JScrollPane scrollContenido = new JScrollPane(txtContenido);
        scrollContenido.setPreferredSize(new Dimension(400, 200));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(scrollContenido, gbc);

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnEnviar = new JButton("📤 Enviar Notificación");
        btnEnviar.setPreferredSize(new Dimension(180, 40));
        btnEnviar.setBackground(new Color(40, 167, 69));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnLimpiar = new JButton("🧹 Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(100, 40));
        btnLimpiar.setBackground(new Color(108, 117, 125));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JButton btnVistaPreviaForm = new JButton("👁️ Vista Previa");
        btnVistaPreviaForm.setPreferredSize(new Dimension(140, 40));
        btnVistaPreviaForm.setBackground(new Color(0, 123, 255));
        btnVistaPreviaForm.setForeground(Color.WHITE);
        btnVistaPreviaForm.setFont(new Font("Arial", Font.BOLD, 14));
        btnVistaPreviaForm.addActionListener(e -> mostrarVistaPrevia());

        buttonPanel.add(btnVistaPreviaForm);
        buttonPanel.add(btnLimpiar);
        buttonPanel.add(btnEnviar);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPlantillasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel izquierdo - Lista de plantillas
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Plantillas Disponibles"));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        modeloPlantillas = new DefaultListModel<>();
        listaPlantillas = new JList<>(modeloPlantillas);
        listaPlantillas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPlantillas.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPlantillas = new JScrollPane(listaPlantillas);
        scrollPlantillas.setPreferredSize(new Dimension(280, 300));

        // Botones de plantillas
        JPanel buttonPlantillasPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        btnUsarPlantilla = new JButton("📋 Usar Plantilla");
        btnUsarPlantilla.setFont(new Font("Arial", Font.BOLD, 12));
        btnUsarPlantilla.setBackground(new Color(40, 167, 69));
        btnUsarPlantilla.setForeground(Color.WHITE);

        btnGuardarPlantilla = new JButton("💾 Guardar Como Plantilla");
        btnGuardarPlantilla.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardarPlantilla.setBackground(new Color(0, 123, 255));
        btnGuardarPlantilla.setForeground(Color.WHITE);

        JButton btnEliminarPlantilla = new JButton("🗑️ Eliminar Plantilla");
        btnEliminarPlantilla.setFont(new Font("Arial", Font.BOLD, 12));
        btnEliminarPlantilla.setBackground(new Color(220, 53, 69));
        btnEliminarPlantilla.setForeground(Color.WHITE);
        btnEliminarPlantilla.addActionListener(e -> eliminarPlantilla());

        buttonPlantillasPanel.add(btnUsarPlantilla);
        buttonPlantillasPanel.add(btnGuardarPlantilla);
        buttonPlantillasPanel.add(btnEliminarPlantilla);

        leftPanel.add(scrollPlantillas, BorderLayout.CENTER);
        leftPanel.add(buttonPlantillasPanel, BorderLayout.SOUTH);

        // Panel derecho - Vista previa
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Vista Previa"));

        txtVistaPrevia = new JTextArea();
        txtVistaPrevia.setEditable(false);
        txtVistaPrevia.setLineWrap(true);
        txtVistaPrevia.setWrapStyleWord(true);
        txtVistaPrevia.setFont(new Font("Arial", Font.PLAIN, 12));
        txtVistaPrevia.setBackground(new Color(248, 249, 250));

        JScrollPane scrollVistaPrevia = new JScrollPane(txtVistaPrevia);
        rightPanel.add(scrollVistaPrevia, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSeguimientoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Título
        JLabel titulo = new JLabel("📊 Seguimiento de Notificaciones Enviadas", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titulo, BorderLayout.NORTH);

        // Tabla de seguimiento
        String[] columnas = {"Fecha", "Curso", "Título", "Destinatarios", "Leídas", "% Lectura", "Estado"};
        modeloSeguimiento = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaSeguimiento = new JTable(modeloSeguimiento);
        tablaSeguimiento.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSeguimiento.setRowHeight(25);
        tablaSeguimiento.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar anchos de columnas
        tablaSeguimiento.getColumnModel().getColumn(0).setPreferredWidth(100); // Fecha
        tablaSeguimiento.getColumnModel().getColumn(1).setPreferredWidth(80);  // Curso
        tablaSeguimiento.getColumnModel().getColumn(2).setPreferredWidth(200); // Título
        tablaSeguimiento.getColumnModel().getColumn(3).setPreferredWidth(90);  // Destinatarios
        tablaSeguimiento.getColumnModel().getColumn(4).setPreferredWidth(60);  // Leídas
        tablaSeguimiento.getColumnModel().getColumn(5).setPreferredWidth(80);  // % Lectura
        tablaSeguimiento.getColumnModel().getColumn(6).setPreferredWidth(80);  // Estado

        JScrollPane scrollSeguimiento = new JScrollPane(tablaSeguimiento);
        panel.add(scrollSeguimiento, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnActualizarSeguimiento = new JButton("🔄 Actualizar");
        btnActualizarSeguimiento.setFont(new Font("Arial", Font.BOLD, 12));
        btnActualizarSeguimiento.setBackground(new Color(0, 123, 255));
        btnActualizarSeguimiento.setForeground(Color.WHITE);

        JButton btnVerDetalles = new JButton("🔍 Ver Detalles");
        btnVerDetalles.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerDetalles.setBackground(new Color(108, 117, 125));
        btnVerDetalles.setForeground(Color.WHITE);
        btnVerDetalles.addActionListener(e -> verDetallesNotificacion());

        JButton btnExportarSeguimiento = new JButton("📊 Exportar");
        btnExportarSeguimiento.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportarSeguimiento.setBackground(new Color(40, 167, 69));
        btnExportarSeguimiento.setForeground(Color.WHITE);
        btnExportarSeguimiento.addActionListener(e -> exportarSeguimiento());

        buttonPanel.add(btnActualizarSeguimiento);
        buttonPanel.add(btnVerDetalles);
        buttonPanel.add(btnExportarSeguimiento);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Título
        JLabel titulo = new JLabel("📈 Estadísticas de Notificaciones", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        // Panel de métricas principales
        JPanel metricsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        metricsPanel.setBorder(new TitledBorder("Métricas Principales"));

        lblNotificacionesEnviadas = createMetricLabel("Notificaciones Enviadas", "0", new Color(0, 123, 255));
        lblNotificacionesLeidas = createMetricLabel("Notificaciones Leídas", "0", new Color(40, 167, 69));
        lblTasaLectura = createMetricLabel("Tasa de Lectura", "0%", new Color(255, 193, 7));

        JLabel lblPromedioPorDia = createMetricLabel("Promedio por Día", "0", new Color(111, 66, 193));

        metricsPanel.add(lblNotificacionesEnviadas);
        metricsPanel.add(lblNotificacionesLeidas);
        metricsPanel.add(lblTasaLectura);
        metricsPanel.add(lblPromedioPorDia);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(metricsPanel, gbc);

        // Panel de filtros de tiempo
        JPanel filtrosPanel = new JPanel(new FlowLayout());
        filtrosPanel.setBorder(new TitledBorder("Filtros de Tiempo"));

        JComboBox<String> comboFiltroTiempo = new JComboBox<>(new String[]{
            "Última semana", "Último mes", "Últimos 3 meses", "Último año", "Todo el tiempo"
        });
        comboFiltroTiempo.setPreferredSize(new Dimension(150, 30));

        JButton btnAplicarFiltro = new JButton("🔍 Aplicar Filtro");
        btnAplicarFiltro.setBackground(new Color(0, 123, 255));
        btnAplicarFiltro.setForeground(Color.WHITE);
        btnAplicarFiltro.setFont(new Font("Arial", Font.BOLD, 12));
        btnAplicarFiltro.addActionListener(e -> aplicarFiltroTiempo((String) comboFiltroTiempo.getSelectedItem()));

        filtrosPanel.add(new JLabel("Período:"));
        filtrosPanel.add(comboFiltroTiempo);
        filtrosPanel.add(btnAplicarFiltro);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(filtrosPanel, gbc);

        // Panel de gráfico (simplificado)
        JPanel graficoPanel = new JPanel(new BorderLayout());
        graficoPanel.setBorder(new TitledBorder("Evolución de Notificaciones"));
        graficoPanel.setPreferredSize(new Dimension(500, 200));
        graficoPanel.setBackground(Color.WHITE);

        // Placeholder para gráfico
        JLabel placeholderGrafico = new JLabel("📊 Gráfico de evolución temporal", JLabel.CENTER);
        placeholderGrafico.setFont(new Font("Arial", Font.ITALIC, 14));
        placeholderGrafico.setForeground(Color.GRAY);
        graficoPanel.add(placeholderGrafico, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(graficoPanel, gbc);

        // Botones de acción
        JPanel actionPanel = new JPanel(new FlowLayout());

        btnGenerarReporte = new JButton("📋 Generar Reporte Completo");
        btnGenerarReporte.setPreferredSize(new Dimension(200, 35));
        btnGenerarReporte.setBackground(new Color(40, 167, 69));
        btnGenerarReporte.setForeground(Color.WHITE);
        btnGenerarReporte.setFont(new Font("Arial", Font.BOLD, 12));

        JButton btnExportarEstadisticas = new JButton("📊 Exportar Estadísticas");
        btnExportarEstadisticas.setPreferredSize(new Dimension(180, 35));
        btnExportarEstadisticas.setBackground(new Color(0, 123, 255));
        btnExportarEstadisticas.setForeground(Color.WHITE);
        btnExportarEstadisticas.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportarEstadisticas.addActionListener(e -> exportarEstadisticas());

        actionPanel.add(btnGenerarReporte);
        actionPanel.add(btnExportarEstadisticas);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(actionPanel, gbc);

        return panel;
    }

    private JLabel createMetricLabel(String titulo, String valor, Color color) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(10, 10, 10, 10)
        ));
        container.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel(titulo, JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitulo.setForeground(color);

        JLabel lblValor = new JLabel(valor, JLabel.CENTER);
        lblValor.setFont(new Font("Arial", Font.BOLD, 24));
        lblValor.setForeground(color);

        container.add(lblTitulo, BorderLayout.NORTH);
        container.add(lblValor, BorderLayout.CENTER);

        return lblValor; // Retornamos el label del valor para poder actualizarlo
    }

    private void setupLayout() {
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // Listener para cambio de tipo de notificación
        comboTipoNotificacion.addActionListener(e -> actualizarPlantillaPorTipo());

        // Listener para selección de plantilla
        listaPlantillas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarVistaPrevia();
            }
        });

        // Listener para envío de notificación
        btnEnviar.addActionListener(e -> enviarNotificacion());

        // Listener para usar plantilla
        btnUsarPlantilla.addActionListener(e -> usarPlantilla());

        // Listener para guardar plantilla
        btnGuardarPlantilla.addActionListener(e -> guardarPlantilla());

        // Listener para actualizar seguimiento
        btnActualizarSeguimiento.addActionListener(e -> actualizarSeguimiento());

        // Listener para generar reporte
        btnGenerarReporte.addActionListener(e -> generarReporte());
    }

    private void loadInitialData() {
        // Cargar cursos
        cargarCursos();

        // Cargar plantillas
        cargarPlantillas();

        // Cargar seguimiento
        actualizarSeguimiento();

        // Cargar estadísticas
        actualizarEstadisticas();
    }

    private void cargarCursos() {
        try {
            comboCursos.removeAllItems();
            comboCursos.addItem("Todos los cursos");

            String query = "SELECT id, anio, division FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int anio = rs.getInt("anio");
                String division = rs.getString("division");
                String formato = anio + "°" + division;

                cursosMap.put(formato, id);
                comboCursos.addItem(formato);
            }

            System.out.println("✅ Cursos cargados: " + cursosMap.size());

        } catch (SQLException e) {
            System.err.println("Error cargando cursos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarPlantillas() {
        modeloPlantillas.clear();
        plantillas.clear();

        // Plantillas predefinidas
        plantillas.add(new PlantillaNotificacion("Aviso General",
                "📢 Aviso General - {CURSO}",
                "Estimados alumnos del curso {CURSO}:\n\n{CONTENIDO}\n\n📅 Fecha: {FECHA}\n👨‍💼 Preceptoría"));

        plantillas.add(new PlantillaNotificacion("Problema de Asistencia",
                "⚠️ Atención: Problema de Asistencia",
                "Se ha detectado un problema con tu asistencia:\n\n📊 Faltas registradas: {FALTAS}\n📅 Período: {PERIODO}\n\n⚠️ Te recomendamos mejorar tu asistencia para evitar complicaciones académicas.\n\nPara más información, consulta con preceptoría."));

        plantillas.add(new PlantillaNotificacion("Reunión de Padres",
                "👨‍👩‍👧‍👦 Convocatoria: Reunión de Padres - {CURSO}",
                "Se convoca a reunión de padres:\n\n🎓 Curso: {CURSO}\n📅 Fecha: {FECHA}\n🕐 Hora: {HORA}\n🏫 Lugar: {LUGAR}\n\n⚠️ La asistencia es obligatoria.\nConfirmar asistencia con preceptoría."));

        plantillas.add(new PlantillaNotificacion("Examen Importante",
                "📝 Examen Importante: {MATERIA}",
                "Se informa sobre el próximo examen:\n\n📚 Materia: {MATERIA}\n📝 Tipo: {TIPO_EXAMEN}\n📅 Fecha: {FECHA}\n🕐 Hora: {HORA}\n\n📖 Temario: {TEMARIO}\n\n¡Estudia y prepárate adecuadamente!"));

        plantillas.add(new PlantillaNotificacion("Evento Especial",
                "🎉 Evento Especial: {EVENTO}",
                "Te invitamos a participar del siguiente evento:\n\n🎉 Evento: {EVENTO}\n📅 Fecha: {FECHA}\n🕐 Hora: {HORA}\n📍 Lugar: {LUGAR}\n\n{DESCRIPCION}\n\n¡Te esperamos!"));

        // Agregar plantillas al modelo
        for (PlantillaNotificacion plantilla : plantillas) {
            modeloPlantillas.addElement(plantilla.getNombre());
        }

        // Cargar plantillas personalizadas desde base de datos
        cargarPlantillasPersonalizadas();
    }

    private void cargarPlantillasPersonalizadas() {
        try {
            String query = "SELECT nombre, titulo, contenido FROM plantillas_notificacion WHERE preceptor_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, preceptorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String titulo = rs.getString("titulo");
                String contenido = rs.getString("contenido");

                PlantillaNotificacion plantilla = new PlantillaNotificacion(nombre, titulo, contenido);
                plantilla.setPersonalizada(true);
                plantillas.add(plantilla);
                modeloPlantillas.addElement("🔧 " + nombre);
            }

        } catch (SQLException e) {
            System.err.println("Error cargando plantillas personalizadas: " + e.getMessage());
        }
    }

    private void actualizarSeguimiento() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    modeloSeguimiento.setRowCount(0);

                    String query = """
                        SELECT n.id, n.titulo, n.fecha_creacion, n.prioridad,
                               COUNT(nd.id) as total_destinatarios,
                               COUNT(CASE WHEN nd.estado_lectura = 'LEIDA' THEN 1 END) as leidas,
                               'Activa' as estado
                        FROM notificaciones n
                        LEFT JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                        WHERE n.remitente_id = ? AND n.estado = 'ACTIVA'
                        AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                        GROUP BY n.id, n.titulo, n.fecha_creacion, n.prioridad
                        ORDER BY n.fecha_creacion DESC
                        """;

                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, preceptorId);
                    ResultSet rs = ps.executeQuery();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                    while (rs.next()) {
                        String fecha = rs.getTimestamp("fecha_creacion").toLocalDateTime().format(formatter);
                        String titulo = rs.getString("titulo");
                        int totalDestinatarios = rs.getInt("total_destinatarios");
                        int leidas = rs.getInt("leidas");
                        String estado = rs.getString("estado");

                        double porcentajeLectura = totalDestinatarios > 0 ? (leidas * 100.0 / totalDestinatarios) : 0;
                        String porcentajeStr = String.format("%.1f%%", porcentajeLectura);

                        // Obtener curso (simplificado)
                        String curso = "General";
                        if (titulo.contains("°")) {
                            String[] partes = titulo.split("°");
                            if (partes.length > 0) {
                                curso = partes[0].substring(partes[0].lastIndexOf(" ") + 1) + "°"
                                        + (partes[1].contains(" ") ? partes[1].substring(0, partes[1].indexOf(" ")) : partes[1]);
                            }
                        }

                        Object[] fila = {fecha, curso, titulo, totalDestinatarios, leidas, porcentajeStr, estado};
                        modeloSeguimiento.addRow(fila);
                    }

                } catch (SQLException e) {
                    System.err.println("Error actualizando seguimiento: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                System.out.println("✅ Seguimiento actualizado: " + modeloSeguimiento.getRowCount() + " registros");
            }
        };

        worker.execute();
    }

    private void actualizarEstadisticas() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Notificaciones enviadas
                    String queryEnviadas = "SELECT COUNT(*) FROM notificaciones WHERE remitente_id = ? AND estado = 'ACTIVA'";
                    PreparedStatement psEnviadas = connection.prepareStatement(queryEnviadas);
                    psEnviadas.setInt(1, preceptorId);
                    ResultSet rsEnviadas = psEnviadas.executeQuery();
                    int enviadas = rsEnviadas.next() ? rsEnviadas.getInt(1) : 0;

                    // Notificaciones leídas
                    String queryLeidas = """
                        SELECT COUNT(*) FROM notificaciones n
                        INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                        WHERE n.remitente_id = ? AND nd.estado_lectura = 'LEIDA'
                        """;
                    PreparedStatement psLeidas = connection.prepareStatement(queryLeidas);
                    psLeidas.setInt(1, preceptorId);
                    ResultSet rsLeidas = psLeidas.executeQuery();
                    int leidas = rsLeidas.next() ? rsLeidas.getInt(1) : 0;

                    // Total de destinatarios
                    String queryTotal = """
                        SELECT COUNT(*) FROM notificaciones n
                        INNER JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                        WHERE n.remitente_id = ?
                        """;
                    PreparedStatement psTotal = connection.prepareStatement(queryTotal);
                    psTotal.setInt(1, preceptorId);
                    ResultSet rsTotal = psTotal.executeQuery();
                    int total = rsTotal.next() ? rsTotal.getInt(1) : 0;

                    // Actualizar UI en EDT
                    SwingUtilities.invokeLater(() -> {
                        lblNotificacionesEnviadas.setText(String.valueOf(enviadas));
                        lblNotificacionesLeidas.setText(String.valueOf(leidas));

                        double tasaLectura = total > 0 ? (leidas * 100.0 / total) : 0;
                        lblTasaLectura.setText(String.format("%.1f%%", tasaLectura));
                    });

                } catch (SQLException e) {
                    System.err.println("Error actualizando estadísticas: " + e.getMessage());
                }

                return null;
            }
        };

        worker.execute();
    }

    private void actualizarPlantillaPorTipo() {
        String tipoSeleccionado = (String) comboTipoNotificacion.getSelectedItem();

        // Buscar plantilla correspondiente al tipo
        for (int i = 0; i < plantillas.size(); i++) {
            PlantillaNotificacion plantilla = plantillas.get(i);
            if (plantilla.getNombre().toLowerCase().contains(tipoSeleccionado.toLowerCase().split(" ")[0])) {
                listaPlantillas.setSelectedIndex(i);
                break;
            }
        }
    }

    private void mostrarVistaPrevia() {
        int selectedIndex = listaPlantillas.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < plantillas.size()) {
            PlantillaNotificacion plantilla = plantillas.get(selectedIndex);

            String vistaPrevia = "TÍTULO: " + plantilla.getTitulo() + "\n\n";
            vistaPrevia += "CONTENIDO:\n" + plantilla.getContenido();

            txtVistaPrevia.setText(vistaPrevia);
            txtVistaPrevia.setCaretPosition(0);
        } else {
            // Vista previa del formulario actual
            String titulo = txtTitulo.getText().trim();
            String contenido = txtContenido.getText().trim();

            if (!titulo.isEmpty() || !contenido.isEmpty()) {
                String vistaPrevia = "TÍTULO: " + titulo + "\n\n";
                vistaPrevia += "CONTENIDO:\n" + contenido;
                txtVistaPrevia.setText(vistaPrevia);
                txtVistaPrevia.setCaretPosition(0);
            }
        }
    }

    private void usarPlantilla() {
        int selectedIndex = listaPlantillas.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < plantillas.size()) {
            PlantillaNotificacion plantilla = plantillas.get(selectedIndex);

            // Rellenar formulario con la plantilla
            txtTitulo.setText(plantilla.getTitulo());
            txtContenido.setText(plantilla.getContenido());

            // Cambiar a la pestaña de envío rápido
            tabbedPane.setSelectedIndex(0);

            JOptionPane.showMessageDialog(this,
                    "✅ Plantilla aplicada al formulario.\nPuedes personalizarla antes de enviar.",
                    "Plantilla Aplicada",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona una plantilla de la lista.",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardarPlantilla() {
        String titulo = txtTitulo.getText().trim();
        String contenido = txtContenido.getText().trim();

        if (titulo.isEmpty() || contenido.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete el título y contenido antes de guardar como plantilla.",
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombrePlantilla = JOptionPane.showInputDialog(this,
                "Ingrese un nombre para la plantilla:",
                "Guardar Plantilla",
                JOptionPane.PLAIN_MESSAGE);

        if (nombrePlantilla != null && !nombrePlantilla.trim().isEmpty()) {
            try {
                String query = "INSERT INTO plantillas_notificacion (preceptor_id, nombre, titulo, contenido) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, preceptorId);
                ps.setString(2, nombrePlantilla.trim());
                ps.setString(3, titulo);
                ps.setString(4, contenido);

                int result = ps.executeUpdate();

                if (result > 0) {
                    // Agregar a la lista local
                    PlantillaNotificacion nuevaPlantilla = new PlantillaNotificacion(nombrePlantilla.trim(), titulo, contenido);
                    nuevaPlantilla.setPersonalizada(true);
                    plantillas.add(nuevaPlantilla);
                    modeloPlantillas.addElement("🔧 " + nombrePlantilla.trim());

                    JOptionPane.showMessageDialog(this,
                            "✅ Plantilla guardada exitosamente.",
                            "Plantilla Guardada",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al guardar la plantilla.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                System.err.println("Error guardando plantilla: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Error al guardar la plantilla: " + e.getMessage(),
                        "Error de Base de Datos",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarPlantilla() {
        int selectedIndex = listaPlantillas.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < plantillas.size()) {
            PlantillaNotificacion plantilla = plantillas.get(selectedIndex);

            if (!plantilla.isPersonalizada()) {
                JOptionPane.showMessageDialog(this,
                        "No puedes eliminar plantillas predefinidas del sistema.",
                        "Acción no permitida",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de eliminar la plantilla '" + plantilla.getNombre() + "'?",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    String query = "DELETE FROM plantillas_notificacion WHERE preceptor_id = ? AND nombre = ?";
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, preceptorId);
                    ps.setString(2, plantilla.getNombre());

                    int result = ps.executeUpdate();

                    if (result > 0) {
                        // Remover de la lista local
                        plantillas.remove(selectedIndex);
                        modeloPlantillas.remove(selectedIndex);

                        JOptionPane.showMessageDialog(this,
                                "✅ Plantilla eliminada exitosamente.",
                                "Plantilla Eliminada",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (SQLException e) {
                    System.err.println("Error eliminando plantilla: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar la plantilla: " + e.getMessage(),
                            "Error de Base de Datos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona una plantilla para eliminar.",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void enviarNotificacion() {
        String curso = (String) comboCursos.getSelectedItem();
        String titulo = txtTitulo.getText().trim();
        String contenido = txtContenido.getText().trim();
        String prioridad = (String) comboPrioridad.getSelectedItem();

        if (titulo.isEmpty() || contenido.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete el título y contenido de la notificación.",
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar envío
        int confirmacion = JOptionPane.showConfirmDialog(this,
                String.format("¿Enviar notificación a %s?\n\nTítulo: %s\nPrioridad: %s",
                        curso, titulo, prioridad),
                "Confirmar Envío",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        if ("Todos los cursos".equals(curso)) {
                            // Enviar a todos los alumnos
                            notificationUtil.enviarNotificacionARol(titulo, contenido, 4); // Rol 4 = Alumnos
                        } else {
                            // Enviar a curso específico
                            Integer cursoId = cursosMap.get(curso);
                            if (cursoId != null) {
                                List<Integer> alumnos = obtenerAlumnosDeCurso(cursoId);
                                for (Integer alumnoId : alumnos) {
                                    notificationUtil.enviarNotificacionBasica(titulo, contenido, alumnoId);
                                }
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        System.err.println("Error enviando notificación: " + e.getMessage());
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean exito = get();
                        if (exito) {
                            JOptionPane.showMessageDialog(PreceptorAdvancedNotificationsPanel.this,
                                    "✅ Notificación enviada exitosamente.",
                                    "Envío Exitoso",
                                    JOptionPane.INFORMATION_MESSAGE);
                            limpiarFormulario();
                            actualizarSeguimiento();
                            actualizarEstadisticas();
                        } else {
                            JOptionPane.showMessageDialog(PreceptorAdvancedNotificationsPanel.this,
                                    "❌ Error al enviar la notificación.",
                                    "Error de Envío",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        System.err.println("Error en callback de envío: " + e.getMessage());
                    }
                }
            };

            worker.execute();
        }
    }

    private List<Integer> obtenerAlumnosDeCurso(int cursoId) {
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
            System.err.println("Error obteniendo alumnos del curso: " + e.getMessage());
        }

        return alumnos;
    }

    private void limpiarFormulario() {
        txtTitulo.setText("");
        txtContenido.setText("");
        comboCursos.setSelectedIndex(0);
        comboTipoNotificacion.setSelectedIndex(0);
        comboPrioridad.setSelectedIndex(0);
    }

    private void verDetallesNotificacion() {
        int selectedRow = tablaSeguimiento.getSelectedRow();
        if (selectedRow >= 0) {
            String fecha = (String) modeloSeguimiento.getValueAt(selectedRow, 0);
            String curso = (String) modeloSeguimiento.getValueAt(selectedRow, 1);
            String titulo = (String) modeloSeguimiento.getValueAt(selectedRow, 2);
            String destinatarios = modeloSeguimiento.getValueAt(selectedRow, 3).toString();
            String leidas = modeloSeguimiento.getValueAt(selectedRow, 4).toString();
            String porcentaje = (String) modeloSeguimiento.getValueAt(selectedRow, 5);

            StringBuilder detalles = new StringBuilder();
            detalles.append("📊 DETALLES DE LA NOTIFICACIÓN\n");
            detalles.append("═══════════════════════════════\n\n");
            detalles.append("📅 Fecha de envío: ").append(fecha).append("\n");
            detalles.append("🎓 Curso: ").append(curso).append("\n");
            detalles.append("📝 Título: ").append(titulo).append("\n");
            detalles.append("👥 Destinatarios: ").append(destinatarios).append("\n");
            detalles.append("👁️ Leídas: ").append(leidas).append("\n");
            detalles.append("📊 Tasa de lectura: ").append(porcentaje).append("\n\n");
            detalles.append("💡 Para ver más detalles, consulta el sistema completo de notificaciones.");

            JTextArea textArea = new JTextArea(detalles.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
            textArea.setRows(12);
            textArea.setColumns(50);

            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Detalles de Notificación",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona una notificación de la tabla.",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportarSeguimiento() {
        if (modeloSeguimiento.getRowCount() == 0) {
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
            csv.append("Fecha,Curso,Título,Destinatarios,Leídas,% Lectura,Estado\n");

            // Datos
            for (int i = 0; i < modeloSeguimiento.getRowCount(); i++) {
                for (int j = 0; j < modeloSeguimiento.getColumnCount(); j++) {
                    Object valor = modeloSeguimiento.getValueAt(i, j);
                    csv.append(valor != null ? valor.toString() : "");
                    if (j < modeloSeguimiento.getColumnCount() - 1) {
                        csv.append(",");
                    }
                }
                csv.append("\n");
            }

            // Guardar archivo
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("seguimiento_notificaciones_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write(csv.toString());
                }

                JOptionPane.showMessageDialog(this,
                        "✅ Seguimiento exportado exitosamente a:\n" + file.getAbsolutePath(),
                        "Exportación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            System.err.println("Error exportando seguimiento: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al exportar: " + e.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarFiltroTiempo(String filtro) {
        // Actualizar seguimiento con filtro
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    modeloSeguimiento.setRowCount(0);

                    String whereClause = "";
                    switch (filtro) {
                        case "Última semana":
                            whereClause = "AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
                            break;
                        case "Último mes":
                            whereClause = "AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
                            break;
                        case "Últimos 3 meses":
                            whereClause = "AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 90 DAY)";
                            break;
                        case "Último año":
                            whereClause = "AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 365 DAY)";
                            break;
                        case "Todo el tiempo":
                        default:
                            whereClause = "";
                            break;
                    }

                    String query = String.format("""
                        SELECT n.id, n.titulo, n.fecha_creacion, n.prioridad,
                               COUNT(nd.id) as total_destinatarios,
                               COUNT(CASE WHEN nd.estado_lectura = 'LEIDA' THEN 1 END) as leidas,
                               'Activa' as estado
                        FROM notificaciones n
                        LEFT JOIN notificaciones_destinatarios nd ON n.id = nd.notificacion_id
                        WHERE n.remitente_id = ? AND n.estado = 'ACTIVA' %s
                        GROUP BY n.id, n.titulo, n.fecha_creacion, n.prioridad
                        ORDER BY n.fecha_creacion DESC
                        """, whereClause);

                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, preceptorId);
                    ResultSet rs = ps.executeQuery();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                    while (rs.next()) {
                        String fecha = rs.getTimestamp("fecha_creacion").toLocalDateTime().format(formatter);
                        String titulo = rs.getString("titulo");
                        int totalDestinatarios = rs.getInt("total_destinatarios");
                        int leidas = rs.getInt("leidas");
                        String estado = rs.getString("estado");

                        double porcentajeLectura = totalDestinatarios > 0 ? (leidas * 100.0 / totalDestinatarios) : 0;
                        String porcentajeStr = String.format("%.1f%%", porcentajeLectura);

                        String curso = "General";
                        if (titulo.contains("°")) {
                            String[] partes = titulo.split("°");
                            if (partes.length > 0) {
                                curso = partes[0].substring(partes[0].lastIndexOf(" ") + 1) + "°"
                                        + (partes[1].contains(" ") ? partes[1].substring(0, partes[1].indexOf(" ")) : partes[1]);
                            }
                        }

                        Object[] fila = {fecha, curso, titulo, totalDestinatarios, leidas, porcentajeStr, estado};
                        modeloSeguimiento.addRow(fila);
                    }

                } catch (SQLException e) {
                    System.err.println("Error aplicando filtro: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                System.out.println("✅ Filtro aplicado: " + filtro + " - " + modeloSeguimiento.getRowCount() + " registros");
            }
        };

        worker.execute();
    }

    private void exportarEstadisticas() {
        try {
            String contenido = generarReporteEstadisticas();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("estadisticas_notificaciones_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write(contenido);
                }

                JOptionPane.showMessageDialog(this,
                        "✅ Estadísticas exportadas exitosamente a:\n" + file.getAbsolutePath(),
                        "Exportación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            System.err.println("Error exportando estadísticas: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al exportar estadísticas: " + e.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarReporte() {
        String reporte = generarReporteCompleto();

        JTextArea textArea = new JTextArea(reporte);
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        textArea.setRows(25);
        textArea.setColumns(80);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Reporte Completo de Notificaciones", true);
        dialog.setLayout(new BorderLayout());

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = new JButton("Guardar Reporte");
        btnGuardar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("reporte_completo_"
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

    private String generarReporteEstadisticas() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("                    ESTADÍSTICAS DE NOTIFICACIONES\n");
        reporte.append("                           PRECEPTORÍA\n");
        reporte.append("═══════════════════════════════════════════════════════════════════\n\n");

        reporte.append("👨‍💼 Preceptor ID: ").append(preceptorId).append("\n");
        reporte.append("📅 Fecha del reporte: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        reporte.append("📊 MÉTRICAS PRINCIPALES:\n");
        reporte.append("─────────────────────────\n");
        reporte.append("📤 Notificaciones enviadas: ").append(lblNotificacionesEnviadas.getText()).append("\n");
        reporte.append("👁️ Notificaciones leídas: ").append(lblNotificacionesLeidas.getText()).append("\n");
        reporte.append("📈 Tasa de lectura: ").append(lblTasaLectura.getText()).append("\n\n");

        if (notificationUtil != null) {
            reporte.append("🔔 ESTADO DEL SISTEMA:\n");
            reporte.append("─────────────────────────\n");
            reporte.append("Sistema activo: ").append(notificationUtil.puedeEnviarNotificaciones() ? "SÍ" : "NO").append("\n");
            reporte.append("Notificaciones no leídas: ").append(notificationUtil.getNotificacionesNoLeidas()).append("\n\n");
        }

        reporte.append("📋 PLANTILLAS DISPONIBLES:\n");
        reporte.append("─────────────────────────────\n");
        for (PlantillaNotificacion plantilla : plantillas) {
            reporte.append("• ").append(plantilla.getNombre());
            if (plantilla.isPersonalizada()) {
                reporte.append(" (Personalizada)");
            }
            reporte.append("\n");
        }

        reporte.append("\n═══════════════════════════════════════════════════════════════════\n");
        reporte.append("Reporte generado por el Sistema de Gestión Escolar ET20\n");
        reporte.append("═══════════════════════════════════════════════════════════════════");

        return reporte.toString();
    }

    private String generarReporteCompleto() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("                    REPORTE COMPLETO DE NOTIFICACIONES\n");
        reporte.append("                              PRECEPTORÍA\n");
        reporte.append("═══════════════════════════════════════════════════════════════════\n\n");

        // Información del preceptor
        reporte.append("👨‍💼 INFORMACIÓN DEL PRECEPTOR:\n");
        reporte.append("─────────────────────────────────\n");
        reporte.append("ID: ").append(preceptorId).append("\n");
        reporte.append("Fecha del reporte: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        reporte.append("Cursos gestionados: ").append(cursosMap.size()).append("\n\n");

        // Estadísticas generales
        reporte.append("📊 ESTADÍSTICAS GENERALES:\n");
        reporte.append("─────────────────────────────\n");
        reporte.append("📤 Total notificaciones enviadas: ").append(lblNotificacionesEnviadas.getText()).append("\n");
        reporte.append("👁️ Total notificaciones leídas: ").append(lblNotificacionesLeidas.getText()).append("\n");
        reporte.append("📈 Tasa global de lectura: ").append(lblTasaLectura.getText()).append("\n");
        reporte.append("🎓 Cursos atendidos: ").append(cursosMap.size()).append("\n\n");

        // Cursos gestionados
        reporte.append("🎓 CURSOS GESTIONADOS:\n");
        reporte.append("─────────────────────\n");
        for (String curso : cursosMap.keySet()) {
            reporte.append("• ").append(curso).append(" (ID: ").append(cursosMap.get(curso)).append(")\n");
        }
        reporte.append("\n");

        // Plantillas disponibles
        reporte.append("📋 PLANTILLAS DISPONIBLES:\n");
        reporte.append("──────────────────────────\n");
        for (PlantillaNotificacion plantilla : plantillas) {
            reporte.append("• ").append(plantilla.getNombre());
            if (plantilla.isPersonalizada()) {
                reporte.append(" [PERSONALIZADA]");
            } else {
                reporte.append(" [SISTEMA]");
            }
            reporte.append("\n");
        }
        reporte.append("\n");

        // Seguimiento reciente
        reporte.append("📊 SEGUIMIENTO RECIENTE (Últimas notificaciones):\n");
        reporte.append("─────────────────────────────────────────────────\n");
        if (modeloSeguimiento.getRowCount() > 0) {
            reporte.append(String.format("%-12s %-8s %-25s %-4s %-4s %-8s\n",
                    "FECHA", "CURSO", "TÍTULO", "DEST", "LEÍDA", "% LECT"));
            reporte.append("─────────────────────────────────────────────────────────────────\n");

            int maxRows = Math.min(10, modeloSeguimiento.getRowCount());
            for (int i = 0; i < maxRows; i++) {
                String fecha = ((String) modeloSeguimiento.getValueAt(i, 0)).split(" ")[0];
                String curso = (String) modeloSeguimiento.getValueAt(i, 1);
                String titulo = (String) modeloSeguimiento.getValueAt(i, 2);
                if (titulo.length() > 25) {
                    titulo = titulo.substring(0, 22) + "...";
                }
                String dest = modeloSeguimiento.getValueAt(i, 3).toString();
                String leidas = modeloSeguimiento.getValueAt(i, 4).toString();
                String porcentaje = (String) modeloSeguimiento.getValueAt(i, 5);

                reporte.append(String.format("%-12s %-8s %-25s %-4s %-4s %-8s\n",
                        fecha, curso, titulo, dest, leidas, porcentaje));
            }

            if (modeloSeguimiento.getRowCount() > 10) {
                reporte.append("... y ").append(modeloSeguimiento.getRowCount() - 10).append(" más.\n");
            }
        } else {
            reporte.append("No hay notificaciones recientes registradas.\n");
        }
        reporte.append("\n");

        // Estado del sistema
        if (notificationUtil != null) {
            reporte.append("🔧 ESTADO DEL SISTEMA:\n");
            reporte.append("──────────────────────\n");
            reporte.append("Sistema de notificaciones: ").append(notificationUtil.puedeEnviarNotificaciones() ? "ACTIVO" : "INACTIVO").append("\n");
            reporte.append("Permisos de envío: ").append(notificationUtil.puedeEnviarNotificaciones() ? "HABILITADO" : "DESHABILITADO").append("\n");
            reporte.append("Notificaciones pendientes: ").append(notificationUtil.getNotificacionesNoLeidas()).append("\n\n");
        }

        // Recomendaciones
        reporte.append("💡 RECOMENDACIONES:\n");
        reporte.append("───────────────────\n");

        try {
            double tasaLectura = Double.parseDouble(lblTasaLectura.getText().replace("%", ""));
            if (tasaLectura < 50) {
                reporte.append("• ⚠️ La tasa de lectura es baja. Considera usar títulos más atractivos.\n");
                reporte.append("• 📝 Revisa la relevancia del contenido de las notificaciones.\n");
                reporte.append("• 🕐 Verifica los horarios de envío para mejorar la visibilidad.\n");
            } else if (tasaLectura < 75) {
                reporte.append("• 📈 Buena tasa de lectura. Mantén la calidad del contenido.\n");
                reporte.append("• 🎯 Considera personalizar más las notificaciones por curso.\n");
            } else {
                reporte.append("• ✅ Excelente tasa de lectura. ¡Sigue así!\n");
                reporte.append("• 🌟 Comparte tus estrategias con otros preceptores.\n");
            }
        } catch (Exception e) {
            reporte.append("• 📊 Continúa monitoreando las estadísticas regularmente.\n");
        }

        reporte.append("• 🔄 Actualiza las plantillas periódicamente.\n");
        reporte.append("• 📋 Usa el seguimiento para identificar patrones de lectura.\n\n");

        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("Reporte generado automáticamente por el Sistema de Gestión Escolar ET20\n");
        reporte.append("Para soporte técnico, contacta al administrador del sistema.\n");
        reporte.append("═══════════════════════════════════════════════════════════════════");

        return reporte.toString();
    }

    // Método para limpiar recursos
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                // No cerrar la conexión ya que es compartida
            }
            System.out.println("✅ PreceptorAdvancedNotificationsPanel recursos liberados");
        } catch (SQLException e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        }
    }

    // Clase interna para plantillas
    private static class PlantillaNotificacion {

        private String nombre;
        private String titulo;
        private String contenido;
        private boolean personalizada = false;

        public PlantillaNotificacion(String nombre, String titulo, String contenido) {
            this.nombre = nombre;
            this.titulo = titulo;
            this.contenido = contenido;
        }

        // Getters y setters
        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

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

        public boolean isPersonalizada() {
            return personalizada;
        }

        public void setPersonalizada(boolean personalizada) {
            this.personalizada = personalizada;
        }
    }

    // Getters para testing
    public int getPreceptorId() {
        return preceptorId;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public Map<String, Integer> getCursosMap() {
        return new HashMap<>(cursosMap);
    }

    public List<PlantillaNotificacion> getPlantillas() {
        return new ArrayList<>(plantillas);
    }
}
