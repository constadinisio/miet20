package main.java.views.notifications;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import main.java.database.Conexion;
import main.java.models.Notificacion;
import main.java.models.PersonalNotificationGroup;
import main.java.services.NotificationService;
import main.java.services.NotificationGroupService;

/**
 * Ventana COMPLETA para componer y enviar notificaciones
 *
 * Características: - Composición de asunto y mensaje con editor enriquecido -
 * Selección de destinatarios jerárquica por roles y cursos - Soporte para
 * grupos personalizados - Envío a roles completos - Vista previa antes del
 * envío - Validación completa de datos - Integración con NotificationService
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Ventana completa de envío
 */
public class NotificationSenderWindow extends JFrame {

    // ========================================
    // CAMPOS PRINCIPALES
    // ========================================
    private final int usuarioId;
    private final int userRole;
    private final NotificationService notificationService;
    private final NotificationGroupService groupService;
    private final Connection connection;

    // Componentes de composición
    private JTextField asuntoField;
    private JTextArea mensajeArea;
    private JComboBox<String> prioridadCombo;
    private JCheckBox requiereConfirmacionCheck;
    private JTextField iconoField;
    private JTextField colorField;

    // Componentes de destinatarios
    private JRadioButton individualRadio;
    private JRadioButton grupoPersonalizadoRadio;
    private JRadioButton rolCompletoRadio;
    private JPanel destinatariosPanel;
    private CardLayout destinatariosCardLayout;

    // Selector jerárquico individual
    private JTree usuariosTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTextField busquedaField;
    private Set<Integer> usuariosSeleccionados;
    private JLabel contadorSeleccionLabel;

    // Selector de grupos personalizados
    private JComboBox<PersonalNotificationGroup> gruposCombo;
    private JTextArea grupoInfoArea;
    private JButton gestionarGruposButton;

    // Selector de rol completo
    private JComboBox<RolInfo> rolesCombo;
    private JLabel rolInfoLabel;

    // Botones principales
    private JButton vistaPreviaButton;
    private JButton enviarButton;
    private JButton cancelarButton;
    private JButton limpiarButton;

    // Estados y datos
    private Map<String, UsuarioInfo> todosUsuarios;
    private List<PersonalNotificationGroup> gruposPersonalizados;
    private List<RolInfo> rolesDisponibles;

    // Constantes de diseño
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Font TITULO_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);

    private boolean isSearching = false;
    private String lastSearchText = "";
    private javax.swing.event.DocumentListener searchDocumentListener;

    /**
     * Constructor principal
     */
    public NotificationSenderWindow(int usuarioId, int userRole) {
        this.usuarioId = usuarioId;
        this.userRole = userRole;
        this.notificationService = NotificationService.getInstance();
        this.groupService = NotificationGroupService.getInstance();
        this.connection = Conexion.getInstancia().verificarConexion();
        this.usuariosSeleccionados = new HashSet<>();
        this.todosUsuarios = new HashMap<>();
        this.gruposPersonalizados = new ArrayList<>();
        this.rolesDisponibles = new ArrayList<>();

        try {
            System.out.println("=== INICIANDO NotificationSenderWindow CORREGIDA ===");
            System.out.println("Usuario ID: " + usuarioId + ", Rol: " + userRole);

            // ✅ ORDEN CRÍTICO CORREGIDO:
            // 1. Inicializar TODOS los componentes primero
            initializeComponents();

            // 2. Validar que todos los componentes críticos estén inicializados
            validateCriticalComponents();

            // 3. Configurar layout (ahora que todo está inicializado)
            setupLayout();

            // 4. Configurar listeners
            setupListeners();

            // 5. Cargar datos
            loadInitialData();

            // 6. Configurar ventana
            setTitle("📤 Enviar Notificación - Sistema Escolar CORREGIDO");
            setSize(1100, 800);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setIconImage(createWindowIcon());

            System.out.println("✅ NotificationSenderWindow CORREGIDA inicializada para usuario: " + usuarioId);

        } catch (Exception e) {
            System.err.println("❌ Error crítico inicializando NotificationSenderWindow: " + e.getMessage());
            e.printStackTrace();

            // Mostrar error al usuario
            JOptionPane.showMessageDialog(null,
                    "Error al abrir la ventana de envío de notificaciones:\n" + e.getMessage(),
                    "Error de Inicialización",
                    JOptionPane.ERROR_MESSAGE);

            // Cerrar ventana si no se pudo inicializar
            dispose();
        }
    }

    /**
     * Inicializa todos los componentes de la interfaz
     */
    private void initializeComponents() {
        // === COMPONENTES DE COMPOSICIÓN ===
        asuntoField = new JTextField(40);
        asuntoField.setFont(LABEL_FONT);
        asuntoField.setToolTipText("Ingresa el asunto de la notificación");

        mensajeArea = new JTextArea(8, 40);
        mensajeArea.setFont(LABEL_FONT);
        mensajeArea.setLineWrap(true);
        mensajeArea.setWrapStyleWord(true);
        mensajeArea.setToolTipText("Escribe el contenido de la notificación");

        String[] prioridades = {"NORMAL", "ALTA", "URGENTE", "BAJA"};
        prioridadCombo = new JComboBox<>(prioridades);
        prioridadCombo.setFont(LABEL_FONT);

        requiereConfirmacionCheck = new JCheckBox("Requiere confirmación");
        requiereConfirmacionCheck.setFont(LABEL_FONT);

        iconoField = new JTextField("📧", 5);
        iconoField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconoField.setToolTipText("Ícono de la notificación (emoji)");

        colorField = new JTextField("#007bff", 8);
        colorField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        colorField.setToolTipText("Color en formato hexadecimal (#RRGGBB)");

        // === COMPONENTES DE DESTINATARIOS ===
        individualRadio = new JRadioButton("Usuarios específicos", true);
        grupoPersonalizadoRadio = new JRadioButton("Grupo personalizado");
        rolCompletoRadio = new JRadioButton("Rol completo");

        ButtonGroup destinatariosGroup = new ButtonGroup();
        destinatariosGroup.add(individualRadio);
        destinatariosGroup.add(grupoPersonalizadoRadio);
        destinatariosGroup.add(rolCompletoRadio);

        // Configurar estilos de radio buttons
        for (AbstractButton radio : Collections.list(destinatariosGroup.getElements())) {
            radio.setFont(new Font("Arial", Font.BOLD, 13));
            radio.setForeground(new Color(60, 60, 60));
        }

        // ✅ CRÍTICO: Inicializar destinatariosCardLayout y destinatariosPanel AQUÍ
        destinatariosCardLayout = new CardLayout();
        destinatariosPanel = new JPanel(destinatariosCardLayout);
        destinatariosPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Seleccionar Destinatarios",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // ✅ CRÍTICO: Inicializar campos de búsqueda ANTES de usarlos
        busquedaField = new JTextField(20);
        busquedaField.setFont(LABEL_FONT);
        busquedaField.setToolTipText("Buscar por nombre, apellido o curso");

        // ✅ CRÍTICO: Inicializar componentes del árbol ANTES de usarlos
        rootNode = new DefaultMutableTreeNode("Sistema Escolar");
        treeModel = new DefaultTreeModel(rootNode);
        usuariosTree = new JTree(treeModel);
        usuariosTree.setShowsRootHandles(true);
        usuariosTree.setRootVisible(false);
        usuariosTree.setCellRenderer(new UsuarioTreeCellRenderer());
        usuariosTree.setRowHeight(25);
        usuariosTree.setFont(LABEL_FONT);

        // ✅ CRÍTICO: Inicializar contador de selección
        contadorSeleccionLabel = new JLabel("Destinatarios seleccionados: 0");
        contadorSeleccionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        contadorSeleccionLabel.setForeground(PRIMARY_COLOR);

        // ✅ CRÍTICO: Inicializar componentes de grupos
        gruposCombo = new JComboBox<>();
        gruposCombo.setFont(LABEL_FONT);
        gruposCombo.setPreferredSize(new Dimension(250, 30));

        grupoInfoArea = new JTextArea(12, 40);
        grupoInfoArea.setEditable(false);
        grupoInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        grupoInfoArea.setBackground(new Color(248, 249, 250));

        // ✅ CRÍTICO: Inicializar componentes de roles
        rolesCombo = new JComboBox<>();
        rolesCombo.setFont(LABEL_FONT);
        rolesCombo.setPreferredSize(new Dimension(200, 30));

        rolInfoLabel = new JLabel();
        rolInfoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        rolInfoLabel.setForeground(new Color(100, 100, 100));

        // ✅ IMPORTANTE: Inicializar paneles específicos INMEDIATAMENTE
        initializeIndividualPanel();
        initializeGrupoPanel();
        initializeRolPanel();

        // === BOTONES PRINCIPALES ===
        vistaPreviaButton = createStyledButton("👁️ Vista Previa", new Color(108, 117, 125));
        enviarButton = createStyledButton("📤 Enviar Notificación", SUCCESS_COLOR);
        cancelarButton = createStyledButton("❌ Cancelar", DANGER_COLOR);
        limpiarButton = createStyledButton("🔄 Limpiar", WARNING_COLOR);

        System.out.println("🔧 Componentes inicializados exitosamente");
    }

    /**
     * Inicializa el panel de selección individual
     */
    private void initializeIndividualPanel() {
        try {
            System.out.println("🔧 Inicializando panel individual con búsqueda mejorada...");

            JPanel individualPanel = new JPanel(new BorderLayout());

            // ✅ PANEL DE BÚSQUEDA CORREGIDO
            JPanel busquedaPanel = createSearchPanel();

            // Árbol jerárquico de usuarios (ya inicializado)
            JScrollPane treeScrollPane = new JScrollPane(usuariosTree);
            treeScrollPane.setPreferredSize(new Dimension(500, 300));
            treeScrollPane.setBorder(BorderFactory.createTitledBorder("Usuarios del Sistema"));

            // Panel de información y controles
            JPanel infoPanel = createInfoPanel();

            // Ensamblar panel
            individualPanel.add(busquedaPanel, BorderLayout.NORTH);
            individualPanel.add(treeScrollPane, BorderLayout.CENTER);
            individualPanel.add(infoPanel, BorderLayout.SOUTH);

            // ✅ CRÍTICO: Verificar que destinatariosPanel no sea null antes de agregar
            if (destinatariosPanel != null) {
                destinatariosPanel.add(individualPanel, "INDIVIDUAL");
                System.out.println("✅ Panel individual con búsqueda mejorada agregado exitosamente");
            } else {
                System.err.println("❌ CRITICAL ERROR: destinatariosPanel es null");
                throw new IllegalStateException("destinatariosPanel no puede ser null");
            }

        } catch (Exception e) {
            System.err.println("❌ Error crítico inicializando panel individual: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fatal en initializeIndividualPanel", e);
        }
    }

    private JPanel createSearchPanel() {
        JPanel busquedaPanel = new JPanel(new GridBagLayout());
        busquedaPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "🔍 Búsqueda de Usuarios",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Etiqueta de búsqueda
        JLabel searchLabel = new JLabel("Buscar usuario:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        busquedaPanel.add(searchLabel, gbc);

        // ✅ CAMPO DE BÚSQUEDA MEJORADO
        busquedaField.setText(""); // Limpiar cualquier texto previo
        busquedaField.setToolTipText("Escribe nombre, apellido, email o curso para buscar");
        busquedaField.setFont(new Font("Arial", Font.PLAIN, 12));
        busquedaField.setPreferredSize(new Dimension(200, 25));

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        busquedaPanel.add(busquedaField, gbc);

        // Botón para limpiar búsqueda
        JButton clearSearchButton = new JButton("❌");
        clearSearchButton.setToolTipText("Limpiar búsqueda");
        clearSearchButton.setPreferredSize(new Dimension(30, 25));
        clearSearchButton.setFont(new Font("Arial", Font.BOLD, 10));
        clearSearchButton.addActionListener(e -> limpiarBusqueda());

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        busquedaPanel.add(clearSearchButton, gbc);

        // Segunda fila: Botones de control del árbol
        JButton expandirButton = new JButton("📂 Expandir Todo");
        JButton contraerButton = new JButton("📁 Contraer Todo");

        expandirButton.setFont(new Font("Arial", Font.PLAIN, 10));
        contraerButton.setFont(new Font("Arial", Font.PLAIN, 10));
        expandirButton.setPreferredSize(new Dimension(120, 25));
        contraerButton.setPreferredSize(new Dimension(120, 25));

        // Listeners para botones
        expandirButton.addActionListener(e -> expandirTodo());
        contraerButton.addActionListener(e -> contraerTodo());

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        busquedaPanel.add(new JLabel("Controles:"), gbc);

        gbc.gridx = 1;
        busquedaPanel.add(expandirButton, gbc);

        gbc.gridx = 2;
        busquedaPanel.add(contraerButton, gbc);

        return busquedaPanel;
    }

// ========================================
// NUEVO MÉTODO: createInfoPanel()
// ========================================
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información de Selección"));

        // Panel izquierdo: contador
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(contadorSeleccionLabel);

        // Panel derecho: botones de selección
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton seleccionarTodoButton = new JButton("✅ Seleccionar Todo");
        JButton limpiarSeleccionButton = new JButton("❌ Limpiar Selección");

        seleccionarTodoButton.setFont(new Font("Arial", Font.PLAIN, 11));
        limpiarSeleccionButton.setFont(new Font("Arial", Font.PLAIN, 11));

        seleccionarTodoButton.addActionListener(e -> seleccionarTodosUsuarios());
        limpiarSeleccionButton.addActionListener(e -> limpiarSeleccionUsuarios());

        rightPanel.add(seleccionarTodoButton);
        rightPanel.add(limpiarSeleccionButton);

        infoPanel.add(leftPanel, BorderLayout.WEST);
        infoPanel.add(rightPanel, BorderLayout.EAST);

        return infoPanel;
    }

    /**
     * Inicializa el panel de grupos personalizados
     */
    private void initializeGrupoPanel() {
        try {
            System.out.println("🔧 Inicializando panel grupo...");

            JPanel grupoPanel = new JPanel(new BorderLayout());

            // Panel superior con selector
            JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            selectorPanel.add(new JLabel("📁 Seleccionar Grupo:"));
            selectorPanel.add(gruposCombo); // Ya inicializado

            gestionarGruposButton = createStyledButton("⚙️ Gestionar Grupos", new Color(108, 117, 125));
            selectorPanel.add(gestionarGruposButton);

            JButton actualizarGruposButton = new JButton("🔄");
            actualizarGruposButton.setFont(BUTTON_FONT);
            actualizarGruposButton.setToolTipText("Actualizar lista de grupos");
            actualizarGruposButton.addActionListener(e -> cargarGruposPersonalizados());
            selectorPanel.add(actualizarGruposButton);

            // Área de información del grupo (ya inicializada)
            grupoInfoArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("👥 Información del Grupo"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JScrollPane grupoScrollPane = new JScrollPane(grupoInfoArea);

            // Ensamblar panel
            grupoPanel.add(selectorPanel, BorderLayout.NORTH);
            grupoPanel.add(grupoScrollPane, BorderLayout.CENTER);

            // ✅ CRÍTICO: Verificar antes de agregar
            if (destinatariosPanel != null) {
                destinatariosPanel.add(grupoPanel, "GRUPO");
                System.out.println("✅ Panel grupo agregado exitosamente");
            } else {
                System.err.println("❌ CRITICAL ERROR: destinatariosPanel es null en initializeGrupoPanel");
                throw new IllegalStateException("destinatariosPanel no puede ser null");
            }

        } catch (Exception e) {
            System.err.println("❌ Error crítico inicializando panel grupo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fatal en initializeGrupoPanel", e);
        }
    }

    /**
     * Inicializa el panel de rol completo
     */
    private void initializeRolPanel() {
        try {
            System.out.println("🔧 Inicializando panel rol...");

            JPanel rolPanel = new JPanel(new BorderLayout());

            // Panel superior con selector
            JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            selectorPanel.add(new JLabel("👥 Seleccionar Rol:"));
            selectorPanel.add(rolesCombo); // Ya inicializado

            // Información del rol (ya inicializada)
            // Panel central con información
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBorder(BorderFactory.createTitledBorder("ℹ️ Información del Rol"));

            JTextArea rolDescripcionArea = new JTextArea(8, 40);
            rolDescripcionArea.setEditable(false);
            rolDescripcionArea.setFont(LABEL_FONT);
            rolDescripcionArea.setText(
                    "Selecciona un rol para enviar la notificación a todos los usuarios que pertenecen a ese rol.\n\n"
                    + "• Administrador: Acceso completo al sistema\n"
                    + "• Preceptor: Gestión de estudiantes y asistencia\n"
                    + "• Profesor: Gestión de notas y materias\n"
                    + "• Estudiante: Consulta de información académica\n"
                    + "• ATTP: Soporte técnico y equipamiento\n\n"
                    + "La notificación se enviará inmediatamente a todos los usuarios activos del rol seleccionado."
            );
            rolDescripcionArea.setBackground(new Color(248, 249, 250));
            rolDescripcionArea.setLineWrap(true);
            rolDescripcionArea.setWrapStyleWord(true);

            JScrollPane rolScrollPane = new JScrollPane(rolDescripcionArea);

            infoPanel.add(rolInfoLabel, BorderLayout.NORTH);
            infoPanel.add(rolScrollPane, BorderLayout.CENTER);

            // Ensamblar panel
            rolPanel.add(selectorPanel, BorderLayout.NORTH);
            rolPanel.add(infoPanel, BorderLayout.CENTER);

            // ✅ CRÍTICO: Verificar antes de agregar
            if (destinatariosPanel != null) {
                destinatariosPanel.add(rolPanel, "ROL");
                System.out.println("✅ Panel rol agregado exitosamente");
            } else {
                System.err.println("❌ CRITICAL ERROR: destinatariosPanel es null en initializeRolPanel");
                throw new IllegalStateException("destinatariosPanel no puede ser null");
            }

        } catch (Exception e) {
            System.err.println("❌ Error crítico inicializando panel rol: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fatal en initializeRolPanel", e);
        }
    }

    /**
     * Configura el layout principal de la ventana
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // === PANEL PRINCIPAL ===
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // === HEADER ===
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // === CONTENIDO CENTRAL ===
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Panel izquierdo - Composición
        JPanel composicionPanel = createComposicionPanel();

        // Panel derecho - Destinatarios
        JPanel destinatariosContainerPanel = createDestinatariosContainerPanel();

        // ✅ VALIDACIÓN CRÍTICA: Verificar que los paneles no sean null
        if (composicionPanel == null) {
            throw new IllegalStateException("composicionPanel no puede ser null");
        }
        if (destinatariosContainerPanel == null) {
            throw new IllegalStateException("destinatariosContainerPanel no puede ser null");
        }

        // Split pane para dividir composición y destinatarios
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                composicionPanel, destinatariosContainerPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.45);
        splitPane.setOneTouchExpandable(true);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // === FOOTER ===
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        System.out.println("📐 Layout configurado exitosamente");
    }

    /**
     * Crea el panel de encabezado
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Título principal
        JLabel titleLabel = new JLabel("📤 Componer Nueva Notificación");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // Subtítulo
        JLabel subtitleLabel = new JLabel("Envía notificaciones a usuarios, grupos o roles específicos");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(200, 220, 255));

        // Panel de títulos
        JPanel titlesPanel = new JPanel(new BorderLayout());
        titlesPanel.setOpaque(false);
        titlesPanel.add(titleLabel, BorderLayout.NORTH);
        titlesPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Información del usuario
        JLabel userInfoLabel = new JLabel("Usuario: " + obtenerNombreUsuario());
        userInfoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        userInfoLabel.setForeground(Color.WHITE);

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(userInfoLabel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Crea el panel de composición del mensaje
     */
    private JPanel createComposicionPanel() {
        JPanel composicionPanel = new JPanel(new BorderLayout());
        composicionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "✍️ Composición del Mensaje",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                TITULO_FONT
        ));

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Asunto
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Asunto:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(asuntoField, gbc);

        // Prioridad y confirmación en la misma fila
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Prioridad:"), gbc);
        gbc.gridx = 1;

        JPanel prioridadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        prioridadPanel.add(prioridadCombo);
        prioridadPanel.add(Box.createHorizontalStrut(20));
        prioridadPanel.add(requiereConfirmacionCheck);
        formPanel.add(prioridadPanel, gbc);

        // Personalización visual
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Personalización:"), gbc);
        gbc.gridx = 1;

        JPanel personalizacionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        personalizacionPanel.add(new JLabel("Ícono:"));
        personalizacionPanel.add(iconoField);
        personalizacionPanel.add(Box.createHorizontalStrut(10));
        personalizacionPanel.add(new JLabel("Color:"));
        personalizacionPanel.add(colorField);

        // Botón para probar color
        JButton probarColorButton = new JButton("🎨");
        probarColorButton.setToolTipText("Probar color");
        probarColorButton.addActionListener(this::mostrarSelectorColor);
        personalizacionPanel.add(probarColorButton);

        formPanel.add(personalizacionPanel, gbc);

        // Mensaje
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mensaje:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JScrollPane mensajeScrollPane = new JScrollPane(mensajeArea);
        mensajeScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        formPanel.add(mensajeScrollPane, gbc);

        // Panel de herramientas de texto
        JPanel herramientasPanel = createHerramientasTextoPanel();

        composicionPanel.add(formPanel, BorderLayout.CENTER);
        composicionPanel.add(herramientasPanel, BorderLayout.SOUTH);

        return composicionPanel;
    }

    /**
     * Crea el panel de herramientas de texto
     */
    private JPanel createHerramientasTextoPanel() {
        JPanel herramientasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        herramientasPanel.setBorder(BorderFactory.createTitledBorder("🔧 Herramientas"));

        // Plantillas rápidas
        JButton plantillaGeneral = new JButton("📝 General");
        JButton plantillaUrgente = new JButton("⚠️ Urgente");
        JButton plantillaEvento = new JButton("📅 Evento");
        JButton plantillaRecordatorio = new JButton("⏰ Recordatorio");

        plantillaGeneral.addActionListener(e -> aplicarPlantilla("general"));
        plantillaUrgente.addActionListener(e -> aplicarPlantilla("urgente"));
        plantillaEvento.addActionListener(e -> aplicarPlantilla("evento"));
        plantillaRecordatorio.addActionListener(e -> aplicarPlantilla("recordatorio"));

        herramientasPanel.add(new JLabel("Plantillas:"));
        herramientasPanel.add(plantillaGeneral);
        herramientasPanel.add(plantillaUrgente);
        herramientasPanel.add(plantillaEvento);
        herramientasPanel.add(plantillaRecordatorio);

        // Separador
        herramientasPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // Herramientas de formato
        JButton insertarFechaButton = new JButton("📅 Fecha");
        JButton insertarHoraButton = new JButton("🕐 Hora");
        JButton contarCaracteresButton = new JButton("🔢 Contar");

        insertarFechaButton.addActionListener(e -> insertarFechaActual());
        insertarHoraButton.addActionListener(e -> insertarHoraActual());
        contarCaracteresButton.addActionListener(e -> mostrarContadorCaracteres());

        herramientasPanel.add(insertarFechaButton);
        herramientasPanel.add(insertarHoraButton);
        herramientasPanel.add(contarCaracteresButton);

        return herramientasPanel;
    }

    /**
     * Crea el panel contenedor de destinatarios
     */
    private JPanel createDestinatariosContainerPanel() {
        // ✅ VALIDACIÓN CRÍTICA: Verificar que destinatariosPanel esté inicializado
        if (destinatariosPanel == null) {
            System.err.println("❌ ERROR: destinatariosPanel es null - inicializando de emergencia");

            // Inicialización de emergencia
            destinatariosCardLayout = new CardLayout();
            destinatariosPanel = new JPanel(destinatariosCardLayout);
            destinatariosPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    "Seleccionar Destinatarios",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14)
            ));

            // Inicializar paneles específicos de emergencia
            initializeIndividualPanel();
            initializeGrupoPanel();
            initializeRolPanel();
        }

        JPanel containerPanel = new JPanel(new BorderLayout());

        // Panel de radio buttons
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.setBorder(BorderFactory.createTitledBorder("📬 Tipo de Destinatario"));

        // ✅ VALIDACIÓN: Verificar que los radio buttons estén inicializados
        if (individualRadio != null) {
            radioPanel.add(individualRadio);
        }
        if (grupoPersonalizadoRadio != null) {
            radioPanel.add(grupoPersonalizadoRadio);
        }
        if (rolCompletoRadio != null) {
            radioPanel.add(rolCompletoRadio);
        }

        // Panel principal de destinatarios
        containerPanel.add(radioPanel, BorderLayout.NORTH);
        containerPanel.add(destinatariosPanel, BorderLayout.CENTER);

        return containerPanel;
    }

    /**
     * Crea el panel de pie de página con botones
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Panel izquierdo con información
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ayudaLabel = new JLabel("💡 Consejo: Usa la vista previa para verificar el mensaje antes de enviar");
        ayudaLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        ayudaLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(ayudaLabel);

        // Panel derecho con botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesPanel.add(limpiarButton);
        botonesPanel.add(vistaPreviaButton);
        botonesPanel.add(cancelarButton);
        botonesPanel.add(enviarButton);

        footerPanel.add(infoPanel, BorderLayout.WEST);
        footerPanel.add(botonesPanel, BorderLayout.EAST);

        return footerPanel;
    }

    /**
     * Configura todos los listeners de eventos
     */
    private void setupListeners() {
        System.out.println("👂 Configurando listeners mejorados...");

        // ✅ LISTENER DE BÚSQUEDA COMPLETAMENTE CORREGIDO
        setupSearchListener();

        // Radio buttons de tipo de destinatario
        individualRadio.addActionListener(e -> {
            destinatariosCardLayout.show(destinatariosPanel, "INDIVIDUAL");
            actualizarEstadoBotones();
        });

        grupoPersonalizadoRadio.addActionListener(e -> {
            destinatariosCardLayout.show(destinatariosPanel, "GRUPO");
            actualizarEstadoBotones();
        });

        rolCompletoRadio.addActionListener(e -> {
            destinatariosCardLayout.show(destinatariosPanel, "ROL");
            actualizarEstadoBotones();
        });

        // Árbol de usuarios - selección múltiple
        usuariosTree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    toggleSeleccionUsuario();
                }
            }
        });

        // Combo de grupos personalizados
        gruposCombo.addActionListener(e -> actualizarInfoGrupo());

        // Combo de roles
        rolesCombo.addActionListener(e -> actualizarInfoRol());

        // Botón gestionar grupos
        gestionarGruposButton.addActionListener(e -> abrirGestorGrupos());

        // Validación en tiempo real del formulario
        setupFormValidationListeners();

        // Botones principales
        vistaPreviaButton.addActionListener(this::mostrarVistaPrevia);
        enviarButton.addActionListener(this::enviarNotificacion);
        cancelarButton.addActionListener(e -> dispose());
        limpiarButton.addActionListener(this::limpiarFormulario);

        System.out.println("✅ Listeners configurados exitosamente");
    }

    private void setupSearchListener() {
        // ✅ REMOVER LISTENER EXISTENTE SI EXISTE (VERSIÓN COMPATIBLE)
        if (searchDocumentListener != null) {
            busquedaField.getDocument().removeDocumentListener(searchDocumentListener);
            searchDocumentListener = null;
        }

        // ✅ CREAR NUEVO DOCUMENT LISTENER OPTIMIZADO
        searchDocumentListener = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }
        };

        // Agregar el nuevo listener
        busquedaField.getDocument().addDocumentListener(searchDocumentListener);

        System.out.println("🔍 Listener de búsqueda configurado");
    }

// ========================================
// NUEVO MÉTODO: scheduleSearch()
// ========================================
    private void scheduleSearch() {
        if (isSearching) {
            return; // Evitar búsquedas concurrentes
        }

        // Usar SwingUtilities.invokeLater para evitar conflictos con EDT
        SwingUtilities.invokeLater(() -> {
            try {
                String currentText = busquedaField.getText();

                // Solo buscar si el texto ha cambiado realmente
                if (!currentText.equals(lastSearchText)) {
                    lastSearchText = currentText;
                    performSearch(currentText);
                }
            } catch (Exception e) {
                System.err.println("Error en búsqueda programada: " + e.getMessage());
            }
        });
    }

    /**
     * Carga los datos iniciales necesarios
     */
    private void loadInitialData() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Cargando usuarios...");
                cargarUsuarios();

                publish("Cargando grupos personalizados...");
                cargarGruposPersonalizados();

                publish("Cargando roles...");
                cargarRoles();

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Mostrar progreso al usuario si es necesario
                for (String mensaje : chunks) {
                    System.out.println(mensaje);
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Verificar si hubo excepciones
                    SwingUtilities.invokeLater(() -> {
                        construirArbolUsuarios();
                        validarFormulario();
                        System.out.println("✅ Datos iniciales cargados completamente");
                    });
                } catch (Exception e) {
                    System.err.println("Error cargando datos iniciales: " + e.getMessage());
                    JOptionPane.showMessageDialog(NotificationSenderWindow.this,
                            "Error al cargar datos iniciales: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Carga todos los usuarios del sistema
     */
    private void cargarUsuarios() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            todosUsuarios.clear();

            // CORREGIDO: Adaptado a la estructura real de la BD
            String query = """
            SELECT u.id, u.nombre, u.apellido, u.mail as email, 
                   ur.rol_id, r.nombre as rol_nombre, u.anio, u.division,
                   CASE 
                       WHEN ur.rol_id = 4 AND u.anio IS NOT NULL AND u.division IS NOT NULL 
                       THEN CONCAT(u.anio, '°', u.division)
                       ELSE ''
                   END as curso_info
            FROM usuarios u
            LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
            LEFT JOIN roles r ON ur.rol_id = r.id
            WHERE u.status = 1 AND u.id != ?
            ORDER BY r.nombre, u.anio, u.division, u.apellido, u.nombre
            """;

            ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            rs = ps.executeQuery();

            while (rs.next()) {
                UsuarioInfo usuario = new UsuarioInfo();
                usuario.id = rs.getInt("id");
                usuario.nombre = rs.getString("nombre");
                usuario.apellido = rs.getString("apellido");
                usuario.email = rs.getString("email");
                usuario.rol = rs.getInt("rol_id");
                usuario.rolNombre = rs.getString("rol_nombre");

                // CORREGIDO: Manejo de campos que pueden ser null
                String anioStr = rs.getString("anio");
                usuario.anio = (anioStr != null && !anioStr.isEmpty()) ? Integer.parseInt(anioStr) : 0;
                usuario.division = rs.getString("division");
                usuario.cursoInfo = rs.getString("curso_info");

                // Si no tiene rol, asignar valores por defecto
                if (usuario.rolNombre == null) {
                    usuario.rolNombre = "Sin rol";
                    usuario.rol = 0;
                }

                String key = usuario.id + "_" + usuario.getNombreCompleto();
                todosUsuarios.put(key, usuario);
            }

            System.out.println("👥 Usuarios cargados: " + todosUsuarios.size());

        } catch (SQLException e) {
            System.err.println("Error cargando usuarios: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Carga los grupos personalizados del usuario
     */
    private void cargarGruposPersonalizados() {
        try {
            gruposPersonalizados.clear();

            groupService.getUserGroups(usuarioId).thenAccept(grupos -> {
                SwingUtilities.invokeLater(() -> {
                    gruposPersonalizados.addAll(grupos);

                    gruposCombo.removeAllItems();
                    gruposCombo.addItem(null); // Opción vacía

                    for (PersonalNotificationGroup grupo : grupos) {
                        gruposCombo.addItem(grupo);
                    }

                    System.out.println("📁 Grupos personalizados cargados: " + grupos.size());
                });
            }).exceptionally(throwable -> {
                System.err.println("Error cargando grupos: " + throwable.getMessage());
                return null;
            });

        } catch (Exception e) {
            System.err.println("Error cargando grupos personalizados: " + e.getMessage());
        }
    }

    /**
     * Carga los roles disponibles
     */
    private void cargarRoles() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            rolesDisponibles.clear();

            // CORREGIDO: Adaptado a la estructura real con usuario_roles
            String query = """
            SELECT r.id, r.nombre, COUNT(DISTINCT u.id) as cantidad_usuarios
            FROM roles r
            LEFT JOIN usuario_roles ur ON r.id = ur.rol_id
            LEFT JOIN usuarios u ON ur.usuario_id = u.id AND u.status = 1
            GROUP BY r.id, r.nombre
            HAVING COUNT(DISTINCT u.id) > 0
            ORDER BY r.id
            """;

            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();

            rolesCombo.removeAllItems();

            while (rs.next()) {
                RolInfo rol = new RolInfo();
                rol.id = rs.getInt("id");
                rol.nombre = rs.getString("nombre");
                rol.cantidadUsuarios = rs.getInt("cantidad_usuarios");

                rolesDisponibles.add(rol);
                rolesCombo.addItem(rol);
            }

            System.out.println("👥 Roles cargados: " + rolesDisponibles.size());

        } catch (SQLException e) {
            System.err.println("Error cargando roles: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Construye nodos específicos para estudiantes agrupados por curso
     */
    private void construirNodosEstudiantes(DefaultMutableTreeNode rolNode, List<UsuarioInfo> estudiantes) {
        Map<String, List<UsuarioInfo>> estudiantesPorCurso = new HashMap<>();

        for (UsuarioInfo estudiante : estudiantes) {
            String curso = estudiante.cursoInfo.isEmpty() ? "Sin Curso Asignado" : estudiante.cursoInfo;
            estudiantesPorCurso.computeIfAbsent(curso, k -> new ArrayList<>()).add(estudiante);
        }

        for (Map.Entry<String, List<UsuarioInfo>> entry : estudiantesPorCurso.entrySet()) {
            String cursoNombre = entry.getKey();
            List<UsuarioInfo> estudiantesCurso = entry.getValue();

            DefaultMutableTreeNode cursoNode = new DefaultMutableTreeNode(
                    new NodoInfo("🎓 " + cursoNombre + " (" + estudiantesCurso.size() + ")", "CURSO")
            );

            for (UsuarioInfo estudiante : estudiantesCurso) {
                DefaultMutableTreeNode estudianteNode = new DefaultMutableTreeNode(
                        new NodoInfo(estudiante.getNombreCompleto(), "USUARIO", estudiante.id)
                );
                cursoNode.add(estudianteNode);
            }

            rolNode.add(cursoNode);
        }
    }

    /**
     * Filtra usuarios según el texto de búsqueda
     */
    private void filtrarUsuarios() {
        String textoBusqueda = busquedaField.getText().toLowerCase().trim();

        if (textoBusqueda.isEmpty()) {
            construirArbolUsuarios();
            return;
        }

        rootNode.removeAllChildren();

        List<UsuarioInfo> usuariosFiltrados = todosUsuarios.values().stream()
                .filter(usuario
                        -> usuario.getNombreCompleto().toLowerCase().contains(textoBusqueda)
                || usuario.email.toLowerCase().contains(textoBusqueda)
                || usuario.rolNombre.toLowerCase().contains(textoBusqueda)
                || (usuario.cursoInfo != null && usuario.cursoInfo.toLowerCase().contains(textoBusqueda))
                )
                .toList();

        if (!usuariosFiltrados.isEmpty()) {
            DefaultMutableTreeNode resultadosNode = new DefaultMutableTreeNode(
                    new NodoInfo("🔍 Resultados (" + usuariosFiltrados.size() + ")", "BUSQUEDA")
            );

            for (UsuarioInfo usuario : usuariosFiltrados) {
                String textoCompleto = usuario.getNombreCompleto() + " - " + usuario.rolNombre;
                if (!usuario.cursoInfo.isEmpty()) {
                    textoCompleto += " (" + usuario.cursoInfo + ")";
                }

                DefaultMutableTreeNode usuarioNode = new DefaultMutableTreeNode(
                        new NodoInfo(textoCompleto, "USUARIO", usuario.id)
                );
                resultadosNode.add(usuarioNode);
            }

            rootNode.add(resultadosNode);
        }

        treeModel.reload();
        expandirTodo();
    }

    /**
     * Maneja la selección/deselección de usuarios en el árbol
     */
    private void toggleSeleccionUsuario() {
        TreePath path = usuariosTree.getSelectionPath();
        if (path == null) {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof NodoInfo) {
            NodoInfo nodoInfo = (NodoInfo) userObject;

            if ("USUARIO".equals(nodoInfo.tipo)) {
                int userId = nodoInfo.userId;

                if (usuariosSeleccionados.contains(userId)) {
                    usuariosSeleccionados.remove(userId);
                } else {
                    usuariosSeleccionados.add(userId);
                }

                actualizarContadorSeleccion();
                usuariosTree.repaint();
            } else if ("ROL".equals(nodoInfo.tipo) || "CURSO".equals(nodoInfo.tipo)) {
                // Seleccionar/deseleccionar todos los usuarios del nodo
                toggleSeleccionNodo(node);
            }
        }
    }

    /**
     * Selecciona/deselecciona todos los usuarios de un nodo
     */
    private void toggleSeleccionNodo(DefaultMutableTreeNode node) {
        Set<Integer> usuariosNodo = new HashSet<>();
        recopilarUsuariosNodo(node, usuariosNodo);

        if (usuariosNodo.isEmpty()) {
            return;
        }

        boolean todosSeleccionados = usuariosSeleccionados.containsAll(usuariosNodo);

        if (todosSeleccionados) {
            usuariosSeleccionados.removeAll(usuariosNodo);
        } else {
            usuariosSeleccionados.addAll(usuariosNodo);
        }

        actualizarContadorSeleccion();
        usuariosTree.repaint();
    }

    /**
     * Recopila todos los IDs de usuario de un nodo recursivamente
     */
    private void recopilarUsuariosNodo(DefaultMutableTreeNode node, Set<Integer> usuarios) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object userObject = child.getUserObject();

            if (userObject instanceof NodoInfo) {
                NodoInfo nodoInfo = (NodoInfo) userObject;
                if ("USUARIO".equals(nodoInfo.tipo)) {
                    usuarios.add(nodoInfo.userId);
                } else {
                    recopilarUsuariosNodo(child, usuarios);
                }
            }
        }
    }

    /**
     * Actualiza el contador de usuarios seleccionados
     */
    private void actualizarContadorSeleccion() {
        contadorSeleccionLabel.setText("Destinatarios seleccionados: " + usuariosSeleccionados.size());
        validarFormulario();
    }

    /**
     * Selecciona todos los usuarios visibles
     */
    private void seleccionarTodosUsuarios() {
        usuariosSeleccionados.clear();
        for (UsuarioInfo usuario : todosUsuarios.values()) {
            usuariosSeleccionados.add(usuario.id);
        }
        actualizarContadorSeleccion();
        usuariosTree.repaint();
    }

    /**
     * Limpia la selección de usuarios
     */
    private void limpiarSeleccionUsuarios() {
        usuariosSeleccionados.clear();
        actualizarContadorSeleccion();
        usuariosTree.repaint();
    }

    /**
     * Actualiza la información del grupo seleccionado
     */
    private void actualizarInfoGrupo() {
        PersonalNotificationGroup grupoSeleccionado = (PersonalNotificationGroup) gruposCombo.getSelectedItem();

        if (grupoSeleccionado == null) {
            grupoInfoArea.setText("Selecciona un grupo para ver su información...");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("📁 GRUPO: ").append(grupoSeleccionado.getName()).append("\n\n");
        info.append("📝 Descripción: ").append(grupoSeleccionado.getDescription() != null
                ? grupoSeleccionado.getDescription() : "Sin descripción").append("\n\n");
        info.append("👥 Miembros: ").append(grupoSeleccionado.getMemberCount()).append("\n");
        info.append("📅 Creado: ").append(grupoSeleccionado.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        if (grupoSeleccionado.getLastUsed() != null) {
            info.append("🕐 Último uso: ").append(grupoSeleccionado.getLastUsed()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        }

        info.append("📋 Lista de miembros:\n");
        info.append("(Cargando información de miembros...)");

        grupoInfoArea.setText(info.toString());

        // Cargar información detallada de miembros de forma asíncrona
        cargarMiembrosGrupo(grupoSeleccionado.getId());
        validarFormulario();
    }

    /**
     * Carga la información de los miembros de un grupo
     */
    private void cargarMiembrosGrupo(int grupoId) {
        groupService.getGroupMembersPreview(grupoId).thenAccept(preview -> {
            SwingUtilities.invokeLater(() -> {
                String textoActual = grupoInfoArea.getText();
                String textoActualizado = textoActual.replace("(Cargando información de miembros...)", preview);
                grupoInfoArea.setText(textoActualizado);
                grupoInfoArea.setCaretPosition(0); // Scroll al inicio
            });
        }).exceptionally(throwable -> {
            System.err.println("Error cargando miembros del grupo: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Actualiza la información del rol seleccionado
     */
    private void actualizarInfoRol() {
        RolInfo rolSeleccionado = (RolInfo) rolesCombo.getSelectedItem();

        if (rolSeleccionado == null) {
            rolInfoLabel.setText("Selecciona un rol para ver la información");
            return;
        }

        String info = String.format("Se enviará a %d usuario(s) con rol '%s'",
                rolSeleccionado.cantidadUsuarios, rolSeleccionado.nombre);
        rolInfoLabel.setText(info);
        validarFormulario();
    }

    /**
     * Abre el gestor de grupos personalizados
     */
    private void abrirGestorGrupos() {
        // Implementar apertura del gestor de grupos
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de gestión de grupos en desarrollo.\n"
                + "Próximamente podrás crear y editar grupos personalizados desde aquí.",
                "Gestor de Grupos", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Valida el formulario y actualiza el estado de los botones
     */
    private void validarFormulario() {
        boolean formularioValido = true;
        StringBuilder errores = new StringBuilder();

        // Validar asunto
        if (asuntoField.getText().trim().isEmpty()) {
            formularioValido = false;
            errores.append("• El asunto es obligatorio\n");
        }

        // Validar mensaje
        if (mensajeArea.getText().trim().isEmpty()) {
            formularioValido = false;
            errores.append("• El mensaje es obligatorio\n");
        }

        // Validar destinatarios según el tipo seleccionado
        if (individualRadio.isSelected()) {
            if (usuariosSeleccionados.isEmpty()) {
                formularioValido = false;
                errores.append("• Debe seleccionar al menos un usuario\n");
            }
        } else if (grupoPersonalizadoRadio.isSelected()) {
            if (gruposCombo.getSelectedItem() == null) {
                formularioValido = false;
                errores.append("• Debe seleccionar un grupo\n");
            }
        } else if (rolCompletoRadio.isSelected()) {
            if (rolesCombo.getSelectedItem() == null) {
                formularioValido = false;
                errores.append("• Debe seleccionar un rol\n");
            }
        }

        // Validar color (formato hexadecimal)
        String color = colorField.getText().trim();
        if (!color.matches("#[0-9A-Fa-f]{6}")) {
            formularioValido = false;
            errores.append("• El color debe tener formato #RRGGBB\n");
        }

        actualizarEstadoBotones();
        enviarButton.setEnabled(formularioValido);
        vistaPreviaButton.setEnabled(asuntoField.getText().trim().length() > 0
                && mensajeArea.getText().trim().length() > 0);
    }

    /**
     * Actualiza el estado de los botones según el contexto
     */
    private void actualizarEstadoBotones() {
        // Los botones se actualizan en validarFormulario()
    }

    /**
     * Muestra la vista previa de la notificación
     */
    private void mostrarVistaPrevia(ActionEvent e) {
        if (!validarCamposBasicos()) {
            return;
        }

        StringBuilder preview = new StringBuilder();
        preview.append("=== VISTA PREVIA DE NOTIFICACIÓN ===\n\n");

        preview.append("📌 ASUNTO: ").append(asuntoField.getText().trim()).append("\n");
        preview.append("⚡ PRIORIDAD: ").append(prioridadCombo.getSelectedItem()).append("\n");
        preview.append("🎨 ÍCONO: ").append(iconoField.getText()).append("\n");
        preview.append("🌈 COLOR: ").append(colorField.getText()).append("\n");

        if (requiereConfirmacionCheck.isSelected()) {
            preview.append("✅ CONFIRMACIÓN: Requerida\n");
        }

        preview.append("\n📝 MENSAJE:\n");
        preview.append(mensajeArea.getText().trim()).append("\n\n");

        preview.append("👥 DESTINATARIOS:\n");
        preview.append(obtenerResumenDestinatarios()).append("\n\n");

        preview.append("👨‍💼 REMITENTE: Usuario ID ").append(usuarioId).append("\n");
        preview.append("📅 FECHA: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        mostrarDialogoVistaPrevia(preview.toString());
    }

    /**
     * Envía la notificación
     */
    private void enviarNotificacion(ActionEvent e) {
        if (!validarFormularioCompleto()) {
            return;
        }

        // Confirmación final
        String resumenDestinatarios = obtenerResumenDestinatarios();
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Confirma el envío de la notificación?\n\n"
                + "Asunto: " + asuntoField.getText().trim() + "\n"
                + "Destinatarios: " + resumenDestinatarios + "\n\n"
                + "Esta acción no se puede deshacer.",
                "Confirmar Envío",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Deshabilitar botones durante el envío
        enviarButton.setEnabled(false);
        enviarButton.setText("Enviando...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Crear y enviar la notificación
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return procesarEnvioNotificacion();
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    enviarButton.setText("📤 Enviar Notificación");
                    enviarButton.setEnabled(true);

                    try {
                        boolean exito = get();
                        mostrarResultadoEnvio(exito);
                    } catch (Exception ex) {
                        System.err.println("Error en envío: " + ex.getMessage());
                        mostrarResultadoEnvio(false);
                    }
                });
            }
        };
        worker.execute();
    }

    /**
     * Procesa el envío de la notificación
     */
    private boolean procesarEnvioNotificacion() {
        try {
            // Crear objeto notificación
            Notificacion notificacion = new Notificacion();
            notificacion.setTitulo(asuntoField.getText().trim());
            notificacion.setContenido(mensajeArea.getText().trim());
            notificacion.setRemitenteId(usuarioId);
            notificacion.setPrioridad((String) prioridadCombo.getSelectedItem());
            notificacion.setRequiereConfirmacion(requiereConfirmacionCheck.isSelected());
            notificacion.setIcono(iconoField.getText().trim());
            notificacion.setColor(colorField.getText().trim());

            // CORREGIDO: Configurar destinatarios según el tipo seleccionado
            if (individualRadio.isSelected()) {
                notificacion.setTipoNotificacion("INDIVIDUAL");
                notificacion.setDestinatariosIndividuales(new ArrayList<>(usuariosSeleccionados));
            } else if (grupoPersonalizadoRadio.isSelected()) {
                notificacion.setTipoNotificacion("GRUPO_PERSONALIZADO");
                PersonalNotificationGroup grupo = (PersonalNotificationGroup) gruposCombo.getSelectedItem();
                // CORREGIDO: Usar el método correcto para grupos personalizados
                notificacion.setGrupoPersonalizadoId(grupo.getId());
            } else if (rolCompletoRadio.isSelected()) {
                notificacion.setTipoNotificacion("ROL");
                RolInfo rol = (RolInfo) rolesCombo.getSelectedItem();
                notificacion.setRolDestino(rol.id);
            }

            // ✅ Enviar con timeout para evitar bloqueos indefinidos
            return notificationService.enviarNotificacion(notificacion)
                    .get(30, TimeUnit.SECONDS); // Timeout de 30 segundos

        } catch (TimeoutException e) {
            System.err.println("Timeout en envío de notificación: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "El envío está tomando más tiempo del esperado.\n"
                        + "La notificación podría enviarse en segundo plano.",
                        "Timeout", JOptionPane.WARNING_MESSAGE);
            });
            return false;
        } catch (Exception e) {
            System.err.println("Error procesando envío: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================
    private boolean validarCamposBasicos() {
        return !asuntoField.getText().trim().isEmpty()
                && !mensajeArea.getText().trim().isEmpty();
    }

    private boolean validarFormularioCompleto() {
        validarFormulario();
        return enviarButton.isEnabled();
    }

    private String obtenerResumenDestinatarios() {
        if (individualRadio.isSelected()) {
            return usuariosSeleccionados.size() + " usuario(s) seleccionado(s)";
        } else if (grupoPersonalizadoRadio.isSelected()) {
            PersonalNotificationGroup grupo = (PersonalNotificationGroup) gruposCombo.getSelectedItem();
            return grupo != null ? "Grupo: " + grupo.getName() + " (" + grupo.getMemberCount() + " miembros)" : "Ningún grupo";
        } else if (rolCompletoRadio.isSelected()) {
            RolInfo rol = (RolInfo) rolesCombo.getSelectedItem();
            return rol != null ? "Rol: " + rol.nombre + " (" + rol.cantidadUsuarios + " usuarios)" : "Ningún rol";
        }
        return "Sin destinatarios";
    }

    private void mostrarDialogoVistaPrevia(String contenido) {
        JTextArea previewArea = new JTextArea(contenido);
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        previewArea.setRows(20);
        previewArea.setColumns(70);

        JScrollPane scrollPane = new JScrollPane(previewArea);
        JOptionPane.showMessageDialog(this, scrollPane, "Vista Previa - Notificación", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra el resultado del envío
     */
    private void mostrarResultadoEnvio(boolean exito) {
        if (exito) {
            JOptionPane.showMessageDialog(this,
                    "✅ Notificación enviada exitosamente\n\n"
                    + "La notificación ha sido entregada a todos los destinatarios seleccionados.",
                    "Envío Exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpiar formulario después del envío exitoso
            limpiarFormulario(null);
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ Error al enviar la notificación\n\n"
                    + "Por favor, verifica la conexión e intenta nuevamente.",
                    "Error de Envío",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarFormulario(ActionEvent e) {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Limpiar todos los campos del formulario?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // Limpiar campos de composición
            asuntoField.setText("");
            mensajeArea.setText("");
            prioridadCombo.setSelectedIndex(0);
            requiereConfirmacionCheck.setSelected(false);
            iconoField.setText("📧");
            colorField.setText("#007bff");

            // Limpiar selecciones
            usuariosSeleccionados.clear();
            busquedaField.setText("");
            gruposCombo.setSelectedIndex(0);

            // Resetear a selección individual
            individualRadio.setSelected(true);
            destinatariosCardLayout.show(destinatariosPanel, "INDIVIDUAL");

            // Actualizar contadores y validaciones
            actualizarContadorSeleccion();
            construirArbolUsuarios();
            usuariosTree.repaint();

            System.out.println("🧹 Formulario limpiado");
        }
    }

    // ========================================
    // MÉTODOS DE HERRAMIENTAS DE TEXTO
    // ========================================
    /**
     * Aplica una plantilla predefinida al mensaje
     */
    private void aplicarPlantilla(String tipoPlantilla) {
        String asunto = "";
        String mensaje = "";
        String icono = "📧";
        String color = "#007bff";
        String prioridad = "NORMAL";

        switch (tipoPlantilla.toLowerCase()) {
            case "general":
                asunto = "Información General";
                mensaje = "Estimados usuarios,\n\n"
                        + "Les comunicamos que...\n\n"
                        + "Saludos cordiales,\n"
                        + "Administración Escolar";
                icono = "ℹ️";
                break;

            case "urgente":
                asunto = "URGENTE - Atención Inmediata";
                mensaje = "ATENCIÓN URGENTE:\n\n"
                        + "Se requiere su atención inmediata para...\n\n"
                        + "Por favor, tome las medidas necesarias.\n\n"
                        + "Gracias por su colaboración.";
                icono = "⚠️";
                color = "#dc3545";
                prioridad = "URGENTE";
                break;

            case "evento":
                asunto = "Próximo Evento Escolar";
                mensaje = "Les recordamos sobre el próximo evento:\n\n"
                        + "📅 Fecha: [FECHA]\n"
                        + "🕐 Hora: [HORA]\n"
                        + "📍 Lugar: [LUGAR]\n\n"
                        + "Su participación es importante.\n\n"
                        + "¡Los esperamos!";
                icono = "📅";
                color = "#6f42c1";
                break;

            case "recordatorio":
                asunto = "Recordatorio Importante";
                mensaje = "Este es un recordatorio sobre:\n\n"
                        + "• [PUNTO 1]\n"
                        + "• [PUNTO 2]\n"
                        + "• [PUNTO 3]\n\n"
                        + "Por favor, no olviden cumplir con lo solicitado.\n\n"
                        + "Gracias.";
                icono = "⏰";
                color = "#fd7e14";
                break;
        }

        // Aplicar plantilla
        asuntoField.setText(asunto);
        mensajeArea.setText(mensaje);
        iconoField.setText(icono);
        colorField.setText(color);
        prioridadCombo.setSelectedItem(prioridad);

        System.out.println("📝 Plantilla aplicada: " + tipoPlantilla);
    }

    /**
     * Inserta la fecha actual en el mensaje
     */
    private void insertarFechaActual() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        insertarTextoEnCursor("[" + fecha + "]");
    }

    /**
     * Inserta la hora actual en el mensaje
     */
    private void insertarHoraActual() {
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        insertarTextoEnCursor("[" + hora + "]");
    }

    /**
     * Inserta texto en la posición del cursor
     */
    private void insertarTextoEnCursor(String texto) {
        int caretPosition = mensajeArea.getCaretPosition();
        try {
            mensajeArea.getDocument().insertString(caretPosition, texto, null);
        } catch (Exception e) {
            mensajeArea.append(texto);
        }
        mensajeArea.requestFocus();
    }

    /**
     * Muestra un contador de caracteres del mensaje
     */
    private void mostrarContadorCaracteres() {
        String mensaje = mensajeArea.getText();
        int caracteres = mensaje.length();
        int palabras = mensaje.trim().isEmpty() ? 0 : mensaje.trim().split("\\s+").length;
        int lineas = mensaje.split("\n").length;

        String info = String.format(
                "📊 Estadísticas del mensaje:\n\n"
                + "Caracteres: %d\n"
                + "Palabras: %d\n"
                + "Líneas: %d\n\n"
                + "Recomendación: Mantén el mensaje claro y conciso.",
                caracteres, palabras, lineas
        );

        JOptionPane.showMessageDialog(this, info, "Contador de Caracteres", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un selector de color
     */
    private void mostrarSelectorColor(ActionEvent e) {
        Color colorActual;
        try {
            colorActual = Color.decode(colorField.getText());
        } catch (NumberFormatException ex) {
            colorActual = PRIMARY_COLOR;
        }

        Color nuevoColor = JColorChooser.showDialog(this, "Seleccionar Color", colorActual);
        if (nuevoColor != null) {
            String hexColor = String.format("#%02x%02x%02x",
                    nuevoColor.getRed(),
                    nuevoColor.getGreen(),
                    nuevoColor.getBlue());
            colorField.setText(hexColor);
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD Y HELPERS
    // ========================================
    /**
     * Crea un botón con estilo consistente
     */
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(originalColor.brighter());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    /**
     * Crea un ícono para la ventana
     */
    private Image createWindowIcon() {
        try {
            // Crear un ícono simple usando Graphics
            int size = 32;
            java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(
                    size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = icon.createGraphics();

            // Configurar antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dibujar fondo
            g2d.setColor(PRIMARY_COLOR);
            g2d.fillRoundRect(2, 2, size - 4, size - 4, 8, 8);

            // Dibujar símbolo de mensaje
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(8, 10, 16, 12, 4, 4);
            g2d.setColor(PRIMARY_COLOR);
            g2d.drawLine(10, 16, 22, 16);
            g2d.drawLine(10, 18, 18, 18);

            g2d.dispose();
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene el nombre del usuario actual
     */
    private String obtenerNombreUsuario() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // CORREGIDO: Campo correcto y manejo de recursos
            String query = "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo FROM usuarios WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setInt(1, usuarioId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre de usuario: " + e.getMessage());
        } finally {
            // ✅ IMPORTANTE: Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
        return "Usuario #" + usuarioId;
    }

    /**
     * Obtiene el ícono correspondiente a un rol
     */
    private String obtenerIconoRol(String rolNombre) {
        switch (rolNombre) {
            case "Administrador":
                return "👑";
            case "Preceptor":
                return "👨‍🏫";
            case "Profesor":
                return "👩‍🏫";
            case "Estudiante":
                return "🎓";
            case "ATTP":
                return "🔧";
            default:
                return "👤";
        }
    }

    // ========================================
    // CLASES INTERNAS PARA DATOS
    // ========================================
    /**
     * Información de usuario para el selector
     */
    private static class UsuarioInfo {

        int id;
        String nombre;
        String apellido;
        String email;
        int rol;
        String rolNombre;
        int anio;
        String division;
        String cursoInfo;

        String getNombreCompleto() {
            return apellido + ", " + nombre;
        }

        @Override
        public String toString() {
            // CORREGIDO: Validación de null para evitar errores
            String curso = (cursoInfo != null && !cursoInfo.isEmpty()) ? cursoInfo : "";
            return getNombreCompleto() + (curso.isEmpty() ? "" : " (" + curso + ")");
        }
    }

    /**
     * Información de rol para el selector
     */
    private static class RolInfo {

        int id;
        String nombre;
        int cantidadUsuarios;

        @Override
        public String toString() {
            return nombre + " (" + cantidadUsuarios + " usuarios)";
        }
    }

    /**
     * Información de nodo para el árbol
     */
    private static class NodoInfo {

        String texto;
        String tipo; // "ROL", "CURSO", "USUARIO", "BUSQUEDA"
        int userId = -1;

        NodoInfo(String texto, String tipo) {
            this.texto = texto;
            this.tipo = tipo;
        }

        NodoInfo(String texto, String tipo, int userId) {
            this.texto = texto;
            this.tipo = tipo;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    /**
     * Renderer personalizado para el árbol de usuarios
     */
    private class UsuarioTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof NodoInfo) {
                    NodoInfo nodoInfo = (NodoInfo) userObject;

                    // Cambiar color según si el usuario está seleccionado
                    if ("USUARIO".equals(nodoInfo.tipo) && usuariosSeleccionados.contains(nodoInfo.userId)) {
                        setForeground(PRIMARY_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
            }

            return this;
        }
    }

    // ========================================
    // LIMPIEZA DE RECURSOS
    // ========================================
    /**
     * Limpia recursos al cerrar la ventana
     */
    @Override
    public void dispose() {
        try {
            // Limpiar datos
            if (usuariosSeleccionados != null) {
                usuariosSeleccionados.clear();
            }
            if (todosUsuarios != null) {
                todosUsuarios.clear();
            }
            if (gruposPersonalizados != null) {
                gruposPersonalizados.clear();
            }
            if (rolesDisponibles != null) {
                rolesDisponibles.clear();
            }

            System.out.println("🧹 Recursos de NotificationSenderWindow liberados");
        } catch (Exception e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        } finally {
            super.dispose();
        }
    }

    private void validarDatosUsuarios() {
        todosUsuarios.entrySet().removeIf(entry -> {
            UsuarioInfo usuario = entry.getValue();

            // Remover usuarios con datos incompletos críticos
            if (usuario.nombre == null || usuario.apellido == null
                    || usuario.nombre.trim().isEmpty() || usuario.apellido.trim().isEmpty()) {
                System.out.println("⚠️ Usuario con datos incompletos removido: ID " + usuario.id);
                return true;
            }

            // Limpiar campos null
            if (usuario.email == null) {
                usuario.email = "";
            }
            if (usuario.rolNombre == null) {
                usuario.rolNombre = "Sin rol";
            }
            if (usuario.division == null) {
                usuario.division = "";
            }
            if (usuario.cursoInfo == null) {
                usuario.cursoInfo = "";
            }

            return false;
        });

        System.out.println("✅ Datos de usuarios validados. Total válidos: " + todosUsuarios.size());
    }

    /**
     * NUEVO: Método para obtener usuarios por rol específico (útil para
     * debugging)
     */
    private List<UsuarioInfo> obtenerUsuariosPorRol(int rolId) {
        return todosUsuarios.values().stream()
                .filter(usuario -> usuario.rol == rolId)
                .sorted((u1, u2) -> u1.getNombreCompleto().compareTo(u2.getNombreCompleto()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * NUEVO: Método para refrescar datos sin recargar toda la ventana
     */
    private void refrescarDatos() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Refrescando usuarios...");
                cargarUsuarios();
                validarDatosUsuarios();

                publish("Refrescando roles...");
                cargarRoles();

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String mensaje : chunks) {
                    System.out.println(mensaje);
                    // Opcional: mostrar en una barra de estado
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(() -> {
                        construirArbolUsuarios();
                        validarFormulario();
                        System.out.println("✅ Datos refrescados completamente");
                    });
                } catch (Exception e) {
                    System.err.println("Error refrescando datos: " + e.getMessage());
                    JOptionPane.showMessageDialog(NotificationSenderWindow.this,
                            "Error al refrescar datos: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * NUEVO: Método mejorado para construir nodos de estudiantes Maneja mejor
     * los casos donde no hay año/división asignados
     */
    private void construirNodosEstudiantesSeguro(DefaultMutableTreeNode rolNode, List<UsuarioInfo> estudiantes) {
        Map<String, List<UsuarioInfo>> estudiantesPorCurso = new HashMap<>();

        for (UsuarioInfo estudiante : estudiantes) {
            String curso;

            // CORREGIDO: Manejo seguro de año y división
            if (estudiante.anio > 0 && estudiante.division != null && !estudiante.division.trim().isEmpty()) {
                curso = estudiante.anio + "°" + estudiante.division;
            } else if (estudiante.anio > 0) {
                curso = estudiante.anio + "° (Sin División)";
            } else {
                curso = "Sin Curso Asignado";
            }

            estudiantesPorCurso.computeIfAbsent(curso, k -> new ArrayList<>()).add(estudiante);
        }

        // Ordenar cursos: primero los que tienen año/división, luego los sin asignar
        estudiantesPorCurso.entrySet().stream()
                .sorted((e1, e2) -> {
                    String curso1 = e1.getKey();
                    String curso2 = e2.getKey();

                    if (curso1.equals("Sin Curso Asignado")) {
                        return 1;
                    }
                    if (curso2.equals("Sin Curso Asignado")) {
                        return -1;
                    }

                    return curso1.compareTo(curso2);
                })
                .forEach(entry -> {
                    String cursoNombre = entry.getKey();
                    List<UsuarioInfo> estudiantesCurso = entry.getValue();

                    DefaultMutableTreeNode cursoNode = new DefaultMutableTreeNode(
                            new NodoInfo("🎓 " + cursoNombre + " (" + estudiantesCurso.size() + ")", "CURSO")
                    );

                    // Ordenar estudiantes por apellido
                    estudiantesCurso.sort((e1, e2) -> e1.getNombreCompleto().compareTo(e2.getNombreCompleto()));

                    for (UsuarioInfo estudiante : estudiantesCurso) {
                        DefaultMutableTreeNode estudianteNode = new DefaultMutableTreeNode(
                                new NodoInfo(estudiante.getNombreCompleto(), "USUARIO", estudiante.id)
                        );
                        cursoNode.add(estudianteNode);
                    }

                    rolNode.add(cursoNode);
                });
    }

    /**
     * NUEVO: Método para actualizar el método construirArbolUsuarios existente
     * Reemplaza la llamada a construirNodosEstudiantes con la versión segura
     */
    private void construirArbolUsuariosSeguro() {
        rootNode.removeAllChildren();

        // Validar datos antes de construir el árbol
        validarDatosUsuarios();

        // Agrupar usuarios por rol
        Map<String, List<UsuarioInfo>> usuariosPorRol = new HashMap<>();
        for (UsuarioInfo usuario : todosUsuarios.values()) {
            usuariosPorRol.computeIfAbsent(usuario.rolNombre, k -> new ArrayList<>()).add(usuario);
        }

        // Crear nodos por rol
        for (Map.Entry<String, List<UsuarioInfo>> entry : usuariosPorRol.entrySet()) {
            String rolNombre = entry.getKey();
            List<UsuarioInfo> usuarios = entry.getValue();

            String iconoRol = obtenerIconoRol(rolNombre);
            DefaultMutableTreeNode rolNode = new DefaultMutableTreeNode(
                    new NodoInfo(iconoRol + " " + rolNombre + " (" + usuarios.size() + ")", "ROL")
            );

            if ("Estudiante".equals(rolNombre)) {
                // CORREGIDO: Usar la versión segura para estudiantes
                construirNodosEstudiantesSeguro(rolNode, usuarios);
            } else {
                // Para otros roles, agregar directamente (ordenados)
                usuarios.sort((u1, u2) -> u1.getNombreCompleto().compareTo(u2.getNombreCompleto()));

                for (UsuarioInfo usuario : usuarios) {
                    DefaultMutableTreeNode usuarioNode = new DefaultMutableTreeNode(
                            new NodoInfo(usuario.getNombreCompleto(), "USUARIO", usuario.id)
                    );
                    rolNode.add(usuarioNode);
                }
            }

            rootNode.add(rolNode);
        }

        treeModel.reload();
        expandirTodo();
        System.out.println("🌳 Árbol de usuarios construido de forma segura");
    }

    /**
     * Valida que todos los componentes críticos estén inicializados
     */
    private void validateCriticalComponents() {
        System.out.println("🔍 Validando componentes críticos...");

        if (destinatariosCardLayout == null) {
            throw new IllegalStateException("destinatariosCardLayout no puede ser null");
        }

        if (destinatariosPanel == null) {
            throw new IllegalStateException("destinatariosPanel no puede ser null");
        }

        if (individualRadio == null) {
            throw new IllegalStateException("individualRadio no puede ser null");
        }

        if (grupoPersonalizadoRadio == null) {
            throw new IllegalStateException("grupoPersonalizadoRadio no puede ser null");
        }

        if (rolCompletoRadio == null) {
            throw new IllegalStateException("rolCompletoRadio no puede ser null");
        }

        // ✅ NUEVAS VALIDACIONES CRÍTICAS
        if (busquedaField == null) {
            throw new IllegalStateException("busquedaField no puede ser null");
        }

        if (usuariosTree == null) {
            throw new IllegalStateException("usuariosTree no puede ser null");
        }

        if (contadorSeleccionLabel == null) {
            throw new IllegalStateException("contadorSeleccionLabel no puede ser null");
        }

        if (gruposCombo == null) {
            throw new IllegalStateException("gruposCombo no puede ser null");
        }

        if (rolesCombo == null) {
            throw new IllegalStateException("rolesCombo no puede ser null");
        }

        System.out.println("✅ Todos los componentes críticos están inicializados");
    }

    // ========================================
// MÉTODO CORREGIDO: performSearch() (antes filtrarUsuarios)
// ========================================
    private void performSearch(String searchText) {
        if (isSearching) {
            return;
        }

        isSearching = true;

        try {
            System.out.println("🔍 Realizando búsqueda: '" + searchText + "'");

            String textoBusqueda = searchText.toLowerCase().trim();

            if (textoBusqueda.isEmpty()) {
                // Si no hay texto de búsqueda, mostrar árbol completo
                construirArbolUsuarios();
            } else {
                // Realizar búsqueda filtrada
                construirArbolFiltrado(textoBusqueda);
            }

            // Expandir resultados automáticamente
            SwingUtilities.invokeLater(() -> {
                expandirTodo();
                System.out.println("✅ Búsqueda completada");
            });

        } catch (Exception e) {
            System.err.println("❌ Error en búsqueda: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isSearching = false;
        }
    }

// ========================================
// NUEVO MÉTODO: construirArbolFiltrado()
// ========================================
    private void construirArbolFiltrado(String textoBusqueda) {
        System.out.println("🔍 Filtrando usuarios con: '" + textoBusqueda + "'");

        rootNode.removeAllChildren();

        // Filtrar usuarios que coincidan con la búsqueda
        List<UsuarioInfo> usuariosFiltrados = todosUsuarios.values().stream()
                .filter(usuario -> coincideConBusqueda(usuario, textoBusqueda))
                .sorted((u1, u2) -> u1.getNombreCompleto().compareToIgnoreCase(u2.getNombreCompleto()))
                .collect(java.util.stream.Collectors.toList());

        if (usuariosFiltrados.isEmpty()) {
            // No hay resultados
            DefaultMutableTreeNode noResultsNode = new DefaultMutableTreeNode(
                    new NodoInfo("❌ Sin resultados para: '" + textoBusqueda + "'", "NO_RESULTS")
            );
            rootNode.add(noResultsNode);
        } else {
            // Crear nodo de resultados
            DefaultMutableTreeNode resultadosNode = new DefaultMutableTreeNode(
                    new NodoInfo("🔍 Resultados (" + usuariosFiltrados.size() + ")", "BUSQUEDA")
            );

            // Agrupar por rol para mejor organización
            Map<String, List<UsuarioInfo>> usuariosPorRol = usuariosFiltrados.stream()
                    .collect(java.util.stream.Collectors.groupingBy(u -> u.rolNombre));

            for (Map.Entry<String, List<UsuarioInfo>> entry : usuariosPorRol.entrySet()) {
                String rolNombre = entry.getKey();
                List<UsuarioInfo> usuarios = entry.getValue();

                DefaultMutableTreeNode rolNode = new DefaultMutableTreeNode(
                        new NodoInfo("👥 " + rolNombre + " (" + usuarios.size() + ")", "ROL_FILTRADO")
                );

                for (UsuarioInfo usuario : usuarios) {
                    String textoCompleto = usuario.getNombreCompleto();
                    if (!usuario.cursoInfo.isEmpty()) {
                        textoCompleto += " - " + usuario.cursoInfo;
                    }
                    if (usuario.email != null && !usuario.email.isEmpty()) {
                        textoCompleto += " (" + usuario.email + ")";
                    }

                    DefaultMutableTreeNode usuarioNode = new DefaultMutableTreeNode(
                            new NodoInfo(textoCompleto, "USUARIO", usuario.id)
                    );
                    rolNode.add(usuarioNode);
                }

                resultadosNode.add(rolNode);
            }

            rootNode.add(resultadosNode);
        }

        // Actualizar modelo del árbol
        SwingUtilities.invokeLater(() -> {
            treeModel.reload();
            System.out.println("✅ Árbol filtrado actualizado");
        });
    }

// ========================================
// NUEVO MÉTODO: coincideConBusqueda()
// ========================================
    private boolean coincideConBusqueda(UsuarioInfo usuario, String textoBusqueda) {
        if (usuario == null || textoBusqueda == null || textoBusqueda.isEmpty()) {
            return false;
        }

        String texto = textoBusqueda.toLowerCase();

        // Buscar en nombre completo
        if (usuario.getNombreCompleto().toLowerCase().contains(texto)) {
            return true;
        }

        // Buscar en email
        if (usuario.email != null && usuario.email.toLowerCase().contains(texto)) {
            return true;
        }

        // Buscar en rol
        if (usuario.rolNombre != null && usuario.rolNombre.toLowerCase().contains(texto)) {
            return true;
        }

        // Buscar en información de curso
        if (usuario.cursoInfo != null && usuario.cursoInfo.toLowerCase().contains(texto)) {
            return true;
        }

        // Buscar en componentes separados
        if (usuario.nombre != null && usuario.nombre.toLowerCase().contains(texto)) {
            return true;
        }

        if (usuario.apellido != null && usuario.apellido.toLowerCase().contains(texto)) {
            return true;
        }

        return false;
    }

// ========================================
// NUEVO MÉTODO: limpiarBusqueda()
// ========================================
    private void limpiarBusqueda() {
        System.out.println("🧹 Limpiando búsqueda...");

        // Limpiar el campo de búsqueda
        busquedaField.setText("");
        lastSearchText = "";

        // Reconstruir árbol completo
        SwingUtilities.invokeLater(() -> {
            construirArbolUsuarios();
            expandirTodo();
            System.out.println("✅ Búsqueda limpiada, árbol restaurado");
        });
    }

// ========================================
// MÉTODO CORREGIDO: construirArbolUsuarios()
// ========================================
    private void construirArbolUsuarios() {
        try {
            System.out.println("🌳 Construyendo árbol completo de usuarios...");

            rootNode.removeAllChildren();

            // Validar datos antes de construir el árbol
            validarDatosUsuarios();

            // Agrupar usuarios por rol
            Map<String, List<UsuarioInfo>> usuariosPorRol = new HashMap<>();
            for (UsuarioInfo usuario : todosUsuarios.values()) {
                usuariosPorRol.computeIfAbsent(usuario.rolNombre, k -> new ArrayList<>()).add(usuario);
            }

            // Crear nodos por rol
            for (Map.Entry<String, List<UsuarioInfo>> entry : usuariosPorRol.entrySet()) {
                String rolNombre = entry.getKey();
                List<UsuarioInfo> usuarios = entry.getValue();

                String iconoRol = obtenerIconoRol(rolNombre);
                DefaultMutableTreeNode rolNode = new DefaultMutableTreeNode(
                        new NodoInfo(iconoRol + " " + rolNombre + " (" + usuarios.size() + ")", "ROL")
                );

                if ("Estudiante".equals(rolNombre)) {
                    // Para estudiantes, agrupar por curso
                    construirNodosEstudiantesSeguro(rolNode, usuarios);
                } else {
                    // Para otros roles, agregar directamente (ordenados)
                    usuarios.sort((u1, u2) -> u1.getNombreCompleto().compareToIgnoreCase(u2.getNombreCompleto()));

                    for (UsuarioInfo usuario : usuarios) {
                        DefaultMutableTreeNode usuarioNode = new DefaultMutableTreeNode(
                                new NodoInfo(usuario.getNombreCompleto(), "USUARIO", usuario.id)
                        );
                        rolNode.add(usuarioNode);
                    }
                }

                rootNode.add(rolNode);
            }

            // Actualizar modelo
            SwingUtilities.invokeLater(() -> {
                treeModel.reload();
                System.out.println("✅ Árbol de usuarios construido completamente");
            });

        } catch (Exception e) {
            System.err.println("❌ Error construyendo árbol de usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

// ========================================
// MÉTODO MEJORADO: expandirTodo()
// ========================================
    private void expandirTodo() {
        SwingUtilities.invokeLater(() -> {
            try {
                for (int i = 0; i < usuariosTree.getRowCount(); i++) {
                    usuariosTree.expandRow(i);
                }
                System.out.println("📂 Árbol expandido completamente");
            } catch (Exception e) {
                System.err.println("Error expandiendo árbol: " + e.getMessage());
            }
        });
    }

// ========================================
// MÉTODO MEJORADO: contraerTodo()
// ========================================
    private void contraerTodo() {
        SwingUtilities.invokeLater(() -> {
            try {
                for (int i = usuariosTree.getRowCount() - 1; i >= 0; i--) {
                    usuariosTree.collapseRow(i);
                }
                System.out.println("📁 Árbol contraído completamente");
            } catch (Exception e) {
                System.err.println("Error contrayendo árbol: " + e.getMessage());
            }
        });
    }

// ========================================
// NUEVO MÉTODO: setupFormValidationListeners()
// ========================================
    private void setupFormValidationListeners() {
        // Listener para el campo de asunto
        asuntoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> validarFormulario());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> validarFormulario());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> validarFormulario());
            }
        });

        // Listener para el área de mensaje
        mensajeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> validarFormulario());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> validarFormulario());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> validarFormulario());
            }
        });

        System.out.println("✅ Listeners de validación configurados");
    }

}
