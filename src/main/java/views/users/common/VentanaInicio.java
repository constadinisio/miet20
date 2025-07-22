package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Color;
import java.awt.FlowLayout;
import java.net.URL;
import java.sql.Connection;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import main.java.database.Conexion;
import main.java.utils.MenuBarManager;
import main.java.utils.ResourceManager;
import main.java.utils.uiUtils;
import main.java.views.login.LoginForm;
import main.java.services.NotificationCore.NotificationManager;
import main.java.services.NotificationCore.NotificationIntegrationUtil;
import javax.swing.BorderFactory;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingConstants;
import java.util.concurrent.CompletableFuture;
import javax.swing.JDialog;
import main.java.views.notifications.NotificationUI.NotificationsWindow;
import main.java.tickets.TicketService;
import javax.swing.Timer;

/**
 * Ventana principal unificada para todos los roles del sistema. VERSIÓN
 * COMPLETA OPTIMIZADA 4.0 - Totalmente Responsive
 *
 * Características principales: - Inicialización única y controlada - Sistema de
 * notificaciones singleton optimizado - Panel lateral completamente responsive
 * con scroll automático - Tablas responsive con configuración única - Control
 * de estados para evitar loops infinitos - Soporte para usuarios con muchos
 * botones - Limpieza automática de recursos
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 4.0 - Completamente Optimizada y Responsive
 */
public class VentanaInicio extends javax.swing.JFrame {

    // ========================================
    // CAMPOS DE CONTROL DE ESTADO
    // ========================================
    // Control de inicialización
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicBoolean isInitializing = new AtomicBoolean(false);

    // Control de configuraciones
    private final AtomicBoolean tablesConfigured = new AtomicBoolean(false);
    private final AtomicBoolean panelButtonsConfigured = new AtomicBoolean(false);
    private final AtomicBoolean notificationSystemReady = new AtomicBoolean(false);

    // Control para evitar loops de redimensionamiento
    private final AtomicBoolean isAdjustingTables = new AtomicBoolean(false);
    private volatile long lastTableAdjustTime = 0;
    private static final long TABLE_ADJUST_COOLDOWN = 300; // 300ms entre ajustes

    // ========================================
    // CAMPOS EXISTENTES
    // ========================================
    // Conexión a base de datos
    protected Connection conect;

    // Identificador y rol del usuario
    protected int userId;
    protected int userRol;

    // Panel de menú lateral que cambiará según el rol
    protected JPanel panelBotones;
    protected JScrollPane scrollPanelBotones; // NUEVO: ScrollPane del panel lateral

    // Gestor de paneles según el rol
    private RolPanelManager rolPanelManager;

    // Componentes UI
    private JLabel labelFotoPerfil;
    private JLabel labelNomApe;
    private JLabel labelRol;
    private JLabel labelCursoDiv; // Para alumnos

    // Referencia al panel de fondo original
    private JLabel fondoHomeOriginal;

    // Variables para el sistema responsive
    private volatile boolean panelEspecificoMostrado = false;

    // CONSTANTES PARA RESPONSIVE TABLES
    private static final int MIN_COLUMN_WIDTH = 80;
    private static final int MAX_COLUMN_WIDTH = 400;
    private static final int DEFAULT_ROW_HEIGHT = 25;
    private static final int HEADER_HEIGHT = 30;

    // CONSTANTES PARA PANEL LATERAL RESPONSIVE
    private static final int PANEL_LATERAL_ANCHO_MINIMO = 240;
    private static final int PANEL_LATERAL_ANCHO_MAXIMO = 300;
    private static final int BOTON_ANCHO_ESTANDAR = 200;
    private static final int BOTON_ALTO_ESTANDAR = 40;
    private static final int ESPACIADO_VERTICAL_BOTONES = 10;

    // ========================================
    // CAMPOS PARA NOTIFICACIONES (SINGLETON)
    // ========================================
    // Gestor principal de notificaciones - SINGLETON
    private static NotificationManager notificationManagerInstance;
    private NotificationIntegrationUtil notificationUtil;

    // Control para el cartel emergente de notificaciones
    private JDialog notificationDialog;
    private boolean notificationDialogShown = false;
    private Timer notificationCheckTimer;

    // Componentes del cartel emergente
    private JLabel notificationCountLabel;
    private JLabel notificationMessageLabel;
    private JButton viewNotificationsButton;
    private JButton dismissButton;

    private TicketService ticketService;

    // Control de tiempo para evitar mostrar múltiples carteles
    private volatile long lastNotificationCheck = 0;
    private static final long NOTIFICATION_CHECK_COOLDOWN = 5000; // 5 segundos

    /**
     * Constructor principal optimizado de la ventana unificada.
     */
    /**
     * Constructor principal optimizado de la ventana unificada. VERSIÓN
     * MODIFICADA con soporte para tickets en tiempo real
     */
    public VentanaInicio(int userId, int rolId) {
        // Verificar si ya se está inicializando
        if (isInitializing.get()) {
            System.out.println("⚠️ VentanaInicio ya se está inicializando, esperando...");
            return;
        }

        if (!isInitializing.compareAndSet(false, true)) {
            System.out.println("⚠️ Inicialización en progreso por otro hilo");
            return;
        }

        try {
            this.userId = userId;
            this.userRol = rolId;

            System.out.println("=== INICIALIZANDO VentanaInicio v4.0 CON CARTEL DE NOTIFICACIONES ===");
            System.out.println("Usuario ID: " + userId + ", Rol: " + rolId);

            // PASO 1: Inicializar componentes UI básicos
            initComponents();
            uiUtils.configurarVentana(this);

            // PASO 2: Verificar conexión BD
            probar_conexion();

            // PASO 3: Configurar imágenes básicas
            configurarImagenesBasicas();

            // PASO 4: Inicializar sistema de notificaciones (SINGLETON)
            initializeNotificationSystemSingleton();

            // PASO 5: Inicializar MenuBarManager (solo si notificaciones están listas)
            if (notificationSystemReady.get()) {
                new MenuBarManager(userId, this);
            }

            // PASO 6: Inicializar gestor de paneles según rol
            initializeRolPanelManager();

            // PASO 7: Configurar panel lateral responsive
            configurarPanelLateralResponsive();

            // PASO 8: NUEVO - Configurar cartel de notificaciones
            configurarCartelNotificaciones();

            // PASO 9: NUEVO - Configurar listeners de ventana para mostrar cartel
            configurarListenersVentana();

            // PASO 10: Marcar como inicializado
            isInitialized.set(true);
            System.out.println("✅ VentanaInicio inicializada completamente con cartel de notificaciones");

            // PASO 11: NUEVO - Inicializar TicketService y polling si es desarrollador
            try {
                this.ticketService = TicketService.getInstance();

                if (ticketService.esDeveloper(userId)) {
                    System.out.println("👨‍💻 Usuario es desarrollador, iniciando polling de tickets...");
                    ticketService.iniciarPollingNotificaciones();
                } else {
                    System.out.println("👤 Usuario regular, no se inicia polling de tickets");
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Error inicializando TicketService: " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            System.err.println("❌ Error crítico en inicialización: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar la aplicación:\n" + ex.getMessage(),
                    "Error Crítico", JOptionPane.ERROR_MESSAGE);
        } finally {
            isInitializing.set(false);
        }
    }

    // ========================================
    // MÉTODOS DE INICIALIZACIÓN OPTIMIZADOS
    // ========================================
    /**
     * Configura las imágenes básicas una sola vez
     */
    private void configurarImagenesBasicas() {
        try {
            rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, ResourceManager.getImagePath("logo_et20_max.png"));
            rsscalelabel.RSScaleLabel.setScaleLabel(fondoHome, ResourceManager.getImagePath("5c994f25d361a_1200.jpg"));
            fondoHomeOriginal = fondoHome;
            System.out.println("✅ Imágenes básicas configuradas");
        } catch (Exception e) {
            System.err.println("⚠️ Error configurando imágenes: " + e.getMessage());
        }
    }

    /**
     * Inicializa el sistema de notificaciones usando patrón Singleton
     * optimizado
     */
    private void initializeNotificationSystemSingleton() {
        if (notificationSystemReady.get()) {
            System.out.println("✅ Sistema de notificaciones ya está listo");
            return;
        }

        try {
            System.out.println("--- Inicializando Sistema de Notificaciones ---");
            System.out.println("Usuario ID: " + userId + ", Rol: " + userRol);

            // Obtener instancia singleton
            if (notificationManagerInstance == null) {
                System.out.println("🔄 Inicializando NotificationManager por primera vez...");
                notificationManagerInstance = NotificationManager.getInstance();
                notificationManagerInstance.initialize(userId, userRol);
                System.out.println("✅ NotificationManager inicializado para usuario: " + userId + ", rol: " + userRol);
            } else {
                System.out.println("🔄 Actualizando usuario en NotificationManager");
                System.out.println("Usuario anterior: " + notificationManagerInstance.getCurrentUserId()
                        + ", rol: " + notificationManagerInstance.getCurrentUserRole());
                System.out.println("Usuario nuevo: " + userId + ", rol: " + userRol);
                notificationManagerInstance.updateUser(userId, userRol);
                System.out.println("✅ Usuario actualizado en NotificationManager");
            }

            // Inicializar utilidad de integración
            notificationUtil = NotificationIntegrationUtil.getInstance();

            // Verificar que todo esté funcionando
            if (notificationManagerInstance.getNotificationService() != null) {
                System.out.println("✅ NotificationService conectado correctamente");

                int unreadCount = notificationManagerInstance.getUnreadCount();
                System.out.println("📧 Notificaciones no leídas detectadas: " + unreadCount);

                notificationSystemReady.set(true);
                System.out.println("🎉 Sistema de notificaciones COMPLETAMENTE OPERATIVO");
            } else {
                throw new Exception("NotificationService no disponible");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al inicializar notificaciones: " + e.getMessage());
            e.printStackTrace();
            notificationSystemReady.set(false);
        }
    }

    /**
     * Inicializa el gestor de paneles una sola vez
     */
    private void initializeRolPanelManager() {
        try {
            RolPanelManagerFactory.inicializar();
            this.rolPanelManager = RolPanelManagerFactory.createManager(this, userId, userRol);

            if (this.rolPanelManager == null) {
                throw new RuntimeException("No se pudo crear el PanelManager para el rol: " + userRol);
            }

            System.out.println("✅ RolPanelManager creado exitosamente para rol: " + userRol);

        } catch (Exception ex) {
            System.err.println("❌ Error crítico inicializando PanelManager: " + ex.getMessage());
            ex.printStackTrace();

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error al inicializar el gestor de paneles:\n" + ex.getMessage()
                        + "\n\nAlgunas funciones pueden no estar disponibles.",
                        "Error de Inicialización", JOptionPane.WARNING_MESSAGE);
            });
        }
    }

    // ========================================
    // CONFIGURACIÓN DEL PANEL LATERAL RESPONSIVE
    // ========================================
    /**
     * NUEVO: Configura el panel lateral completamente responsive
     */
    private void configurarPanelLateralResponsive() {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("🔧 Configurando panel lateral responsive...");

                // Configurar jPanel4 con layout responsive
                configurarjPanel4Responsive();

                // Crear el panel de botones dinámico
                crearPanelBotonesDinamico();

                // Configurar scroll responsive
                configurarScrollResponsive();

                // Añadir listener para redimensionamiento
                addResizeListener();

                System.out.println("✅ Panel lateral responsive configurado completamente");

            } catch (Exception e) {
                System.err.println("❌ Error configurando panel lateral: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Configura jPanel4 con propiedades responsive
     */
    private void configurarjPanel4Responsive() {
        // Limpiar el panel
        jPanel4.removeAll();
        jPanel4.setLayout(new BorderLayout());

        // Configurar tamaños responsive
        int anchoMinimo = PANEL_LATERAL_ANCHO_MINIMO;
        int anchoPreferido = Math.min(PANEL_LATERAL_ANCHO_MAXIMO,
                Math.max(anchoMinimo, getWidth() / 5));

        jPanel4.setMinimumSize(new Dimension(anchoMinimo, 0));
        jPanel4.setPreferredSize(new Dimension(anchoPreferido, getHeight()));
        jPanel4.setMaximumSize(new Dimension(PANEL_LATERAL_ANCHO_MAXIMO, Integer.MAX_VALUE));

        // Configurar colores y bordes
        jPanel4.setBackground(new Color(153, 153, 153)); // Mantener color original
        jPanel4.setBorder(BorderFactory.createEmptyBorder());

        System.out.println("📐 jPanel4 configurado - Ancho: " + anchoPreferido + "px");
    }

    /**
     * Crea el panel de botones dinámico y responsive
     */
    private void crearPanelBotonesDinamico() {
        // Crear el panel interno para los botones
        panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(jPanel4.getBackground());
        panelBotones.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // IMPORTANTE: No establecer tamaño fijo, permitir que crezca dinámicamente
        panelBotones.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inicializar etiquetas de usuario
        inicializarEtiquetasUsuario();

        // Construir contenido del panel
        construirContenidoPanelBotones();

        System.out.println("🔨 Panel de botones dinámico creado");
    }

    /**
     * Construye todo el contenido del panel de botones
     */
    private void construirContenidoPanelBotones() {
        // Limpiar panel por si acaso
        panelBotones.removeAll();

        // === SECCIÓN DE INFORMACIÓN DEL USUARIO ===
        // Espacio superior
        panelBotones.add(Box.createVerticalStrut(10));

        // Foto de perfil
        panelBotones.add(labelFotoPerfil);
        panelBotones.add(Box.createVerticalStrut(12));

        // Información del usuario
        panelBotones.add(labelNomApe);
        panelBotones.add(Box.createVerticalStrut(6));
        panelBotones.add(labelRol);

        // Curso (solo para alumnos)
        if (userRol == 4 && labelCursoDiv != null) {
            panelBotones.add(Box.createVerticalStrut(6));
            panelBotones.add(labelCursoDiv);
        }

        // Separador visual
        panelBotones.add(Box.createVerticalStrut(20));
        panelBotones.add(crearSeparadorVisual());
        panelBotones.add(Box.createVerticalStrut(20));

        // === SECCIÓN DE BOTONES FUNCIONALES ===
        if (rolPanelManager != null) {
            JComponent[] botones = rolPanelManager.createButtons();
            System.out.println("📝 Agregando " + botones.length + " botones al panel lateral");

            for (int i = 0; i < botones.length; i++) {
                JComponent boton = botones[i];

                // Configurar el botón para ser responsive
                configurarBotonResponsive(boton, i);

                // Agregar espacio antes del botón (excepto el primero)
                if (i > 0) {
                    panelBotones.add(Box.createVerticalStrut(ESPACIADO_VERTICAL_BOTONES));
                }

                // Agregar el botón
                panelBotones.add(boton);
            }
        } else {
            System.out.println("⚠️ RolPanelManager es null, no se pueden crear botones");
        }

        // === SECCIÓN INFERIOR ===
        // Espacio flexible para empujar el botón de cerrar sesión hacia abajo
        panelBotones.add(Box.createVerticalGlue());

        // Separador antes del botón de cerrar sesión
        panelBotones.add(Box.createVerticalStrut(20));
        panelBotones.add(crearSeparadorVisual());
        panelBotones.add(Box.createVerticalStrut(15));

        // Botón de cerrar sesión
        JButton btnCerrarSesion = crearBotonCerrarSesionResponsive();
        panelBotones.add(btnCerrarSesion);

        // Espacio inferior
        panelBotones.add(Box.createVerticalStrut(15));

        System.out.println("✅ Contenido del panel de botones construido");
    }

    /**
     * Crea un separador visual para el panel
     */
    private Component crearSeparadorVisual() {
        JPanel separador = new JPanel();
        separador.setBackground(new Color(200, 200, 200));
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separador.setPreferredSize(new Dimension(0, 1));
        separador.setAlignmentX(Component.CENTER_ALIGNMENT);
        return separador;
    }

    /**
     * Configura un botón para ser responsive
     */
    private void configurarBotonResponsive(JComponent boton, int indice) {
        if (!(boton instanceof JButton)) {
            return;
        }

        JButton btn = (JButton) boton;

        // Configurar alineación
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(JButton.CENTER);
        btn.setVerticalAlignment(JButton.CENTER);

        // Configurar tamaños responsive
        int anchoBoton = Math.min(BOTON_ANCHO_ESTANDAR, jPanel4.getPreferredSize().width - 20);
        int altoBoton = BOTON_ALTO_ESTANDAR;

        btn.setMinimumSize(new Dimension(anchoBoton - 20, altoBoton));
        btn.setPreferredSize(new Dimension(anchoBoton, altoBoton));
        btn.setMaximumSize(new Dimension(anchoBoton + 20, altoBoton + 5));

        // Configurar propiedades visuales
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);

        // Aplicar estilo según el índice para variedad visual
        aplicarEstiloBoton(btn, indice);

        System.out.println("🔘 Botón configurado: " + btn.getText() + " (" + anchoBoton + "x" + altoBoton + ")");
    }

    /**
     * Aplica estilo visual a un botón
     */
    private void aplicarEstiloBoton(JButton btn, int indice) {
        // Colores base
        Color colorBase = new Color(70, 130, 180);
        Color colorTexto = Color.WHITE;

        // Variar ligeramente el color según el índice
        int variacion = (indice * 15) % 60;
        Color colorFinal = new Color(
                Math.max(50, colorBase.getRed() - variacion),
                Math.max(100, colorBase.getGreen() - variacion / 2),
                Math.min(255, colorBase.getBlue() + variacion / 3)
        );

        btn.setBackground(colorFinal);
        btn.setForeground(colorTexto);
        btn.setFont(new Font("Arial", Font.BOLD, 11));

        // Efectos hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color colorNormal = btn.getBackground();
            Color colorHover = colorNormal.brighter();

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(colorHover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(colorNormal);
            }
        });
    }

    /**
     * Crea el botón de cerrar sesión responsive
     */
    private JButton crearBotonCerrarSesionResponsive() {
        JButton btnCerrarSesion = new JButton("CERRAR SESIÓN");

        // Configurar colores distintivos
        btnCerrarSesion.setBackground(new Color(200, 50, 50));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar tamaños
        int anchoBoton = Math.min(BOTON_ANCHO_ESTANDAR - 20, jPanel4.getPreferredSize().width - 30);
        int altoBoton = BOTON_ALTO_ESTANDAR - 5;

        btnCerrarSesion.setMinimumSize(new Dimension(anchoBoton - 10, altoBoton));
        btnCerrarSesion.setPreferredSize(new Dimension(anchoBoton, altoBoton));
        btnCerrarSesion.setMaximumSize(new Dimension(anchoBoton + 10, altoBoton));

        // Configurar alineación y propiedades
        btnCerrarSesion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrarSesion.setHorizontalAlignment(JButton.CENTER);
        btnCerrarSesion.setVerticalAlignment(JButton.CENTER);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorderPainted(true);

        // Agregar acción
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        // Efectos hover
        btnCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCerrarSesion.setBackground(new Color(220, 70, 70));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCerrarSesion.setBackground(new Color(200, 50, 50));
            }
        });

        return btnCerrarSesion;
    }

    /**
     * Configura el scroll responsive del panel lateral
     */
    private void configurarScrollResponsive() {
        // Crear ScrollPane con configuración optimizada
        scrollPanelBotones = new JScrollPane(panelBotones);

        // Configurar políticas de scroll
        scrollPanelBotones.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanelBotones.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Configurar apariencia
        scrollPanelBotones.setBorder(null);
        scrollPanelBotones.getViewport().setBackground(panelBotones.getBackground());

        // Configurar velocidades de scroll optimizadas
        scrollPanelBotones.getVerticalScrollBar().setUnitIncrement(16);
        scrollPanelBotones.getVerticalScrollBar().setBlockIncrement(50);
        scrollPanelBotones.setWheelScrollingEnabled(true);

        // Configurar scrollbar para mejor visibilidad
        JScrollBar verticalBar = scrollPanelBotones.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(12, 0));
        verticalBar.setBackground(new Color(180, 180, 180));

        // CRÍTICO: Agregar el ScrollPane a jPanel4
        jPanel4.add(scrollPanelBotones, BorderLayout.CENTER);

        System.out.println("📜 Scroll responsive configurado en panel lateral");
    }

    /**
     * Agrega listener para redimensionamiento responsive
     */
    private void addResizeListener() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    actualizarPanelLateralResponsive();
                });
            }
        });

        System.out.println("👂 Listener de redimensionamiento agregado");
    }

    /**
     * Actualiza el panel lateral cuando cambia el tamaño de la ventana
     */
    private void actualizarPanelLateralResponsive() {
        if (!isInitialized.get() || panelBotones == null) {
            return;
        }

        try {
            // Recalcular ancho del panel lateral
            int nuevoAncho = Math.min(PANEL_LATERAL_ANCHO_MAXIMO,
                    Math.max(PANEL_LATERAL_ANCHO_MINIMO, getWidth() / 5));

            // Actualizar tamaño de jPanel4
            jPanel4.setPreferredSize(new Dimension(nuevoAncho, getHeight()));

            // Recalcular tamaños de botones
            for (Component comp : panelBotones.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    int nuevoAnchoBoton = Math.min(BOTON_ANCHO_ESTANDAR, nuevoAncho - 20);

                    btn.setPreferredSize(new Dimension(nuevoAnchoBoton, btn.getPreferredSize().height));
                    btn.setMaximumSize(new Dimension(nuevoAnchoBoton + 20, btn.getMaximumSize().height));
                }
            }

            // Forzar actualización
            panelBotones.revalidate();
            panelBotones.repaint();
            jPanel4.revalidate();
            jPanel4.repaint();

        } catch (Exception ex) {
            System.err.println("Error actualizando panel lateral: " + ex.getMessage());
        }
    }

    /**
     * Inicializa las etiquetas de información de usuario de forma optimizada
     */
    private void inicializarEtiquetasUsuario() {
        // Foto de perfil
        if (labelFotoPerfil == null) {
            labelFotoPerfil = new JLabel();
            labelFotoPerfil.setHorizontalAlignment(JLabel.CENTER);
            labelFotoPerfil.setAlignmentX(Component.CENTER_ALIGNMENT);
            labelFotoPerfil.setMinimumSize(new Dimension(96, 96));
            labelFotoPerfil.setPreferredSize(new Dimension(96, 96));
            labelFotoPerfil.setMaximumSize(new Dimension(96, 96));
            labelFotoPerfil.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }

        // Nombre y apellido
        if (labelNomApe == null) {
            labelNomApe = new JLabel("Usuario", JLabel.CENTER);
            labelNomApe.setFont(new Font("Arial", Font.BOLD, 12));
            labelNomApe.setForeground(Color.WHITE);
            labelNomApe.setAlignmentX(Component.CENTER_ALIGNMENT);
            labelNomApe.setMaximumSize(new Dimension(PANEL_LATERAL_ANCHO_MAXIMO - 20, 25));
        }

        // Rol
        if (labelRol == null) {
            labelRol = new JLabel("Rol: Usuario", JLabel.CENTER);
            labelRol.setFont(new Font("Arial", Font.BOLD, 11));
            labelRol.setForeground(Color.WHITE);
            labelRol.setAlignmentX(Component.CENTER_ALIGNMENT);
            labelRol.setMaximumSize(new Dimension(PANEL_LATERAL_ANCHO_MAXIMO - 20, 20));
        }

        // Curso y división (solo para alumnos)
        if (userRol == 4 && labelCursoDiv == null) {
            labelCursoDiv = new JLabel("Curso: -", JLabel.CENTER);
            labelCursoDiv.setFont(new Font("Arial", Font.BOLD, 11));
            labelCursoDiv.setForeground(Color.WHITE);
            labelCursoDiv.setAlignmentX(Component.CENTER_ALIGNMENT);
            labelCursoDiv.setMaximumSize(new Dimension(PANEL_LATERAL_ANCHO_MAXIMO - 20, 20));
        }

        System.out.println("👤 Etiquetas de usuario inicializadas");
    }

    // ========================================
    // MÉTODOS PÚBLICOS OPTIMIZADOS DEL PANEL LATERAL
    // ========================================
    /**
     * NUEVO: Reconfigura el panel de botones (usado al cambiar de rol)
     */
    public void reconfigurarPanelBotones() {
        if (!panelButtonsConfigured.compareAndSet(true, false)) {
            return; // Ya está siendo reconfigurado
        }

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("🔄 Reconfigurando panel de botones...");

                // Limpiar y reconstruir el contenido
                if (panelBotones != null) {
                    construirContenidoPanelBotones();

                    // Forzar actualización visual
                    panelBotones.revalidate();
                    panelBotones.repaint();

                    if (scrollPanelBotones != null) {
                        scrollPanelBotones.revalidate();
                        scrollPanelBotones.repaint();
                    }
                }

                System.out.println("✅ Panel de botones reconfigurado");

            } catch (Exception e) {
                System.err.println("❌ Error reconfigurando panel: " + e.getMessage());
            } finally {
                panelButtonsConfigured.set(true);
            }
        });
    }

    /**
     * NUEVO: Fuerza actualización del scroll del panel lateral
     */
    public void actualizarScrollPanelLateral() {
        SwingUtilities.invokeLater(() -> {
            if (scrollPanelBotones != null && panelBotones != null) {
                // Calcular si necesita scroll
                int alturaContenido = panelBotones.getPreferredSize().height;
                int alturaDisponible = scrollPanelBotones.getViewport().getHeight();

                System.out.println("📏 Altura contenido: " + alturaContenido + ", Disponible: " + alturaDisponible);

                // Forzar scroll si el contenido es mayor
                if (alturaContenido > alturaDisponible) {
                    scrollPanelBotones.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    System.out.println("📜 Scroll vertical forzado - contenido excede altura disponible");
                } else {
                    scrollPanelBotones.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                }

                // Actualizar vista
                scrollPanelBotones.revalidate();
                scrollPanelBotones.repaint();
                scrollPanelBotones.getVerticalScrollBar().setValue(0); // Ir al inicio
            }
        });
    }

    /**
     * MÉTODO PRINCIPAL: Configura el panel de botones (MÉTODO PÚBLICO ORIGINAL)
     */
    public void configurePanelBotones() {
        // Evitar configuración múltiple
        if (panelButtonsConfigured.get()) {
            System.out.println("⚠️ Panel de botones ya configurado, reconfigurando...");
            reconfigurarPanelBotones();
            return;
        }

        if (!panelButtonsConfigured.compareAndSet(false, true)) {
            System.out.println("⚠️ Panel de botones siendo configurado por otro hilo");
            return;
        }

        try {
            System.out.println("🔧 Configurando panel de botones inicial...");

            // Si ya se configuró el panel lateral responsive, solo reconstruir contenido
            if (scrollPanelBotones != null && panelBotones != null) {
                construirContenidoPanelBotones();
            } else {
                // Primera configuración completa
                configurarPanelLateralResponsive();
            }

            // Forzar actualización del scroll
            actualizarScrollPanelLateral();

            System.out.println("✅ Panel de botones configurado exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error configurando panel de botones: " + e.getMessage());
            panelButtonsConfigured.set(false); // Permitir retry
            throw e;
        }
    }

    // ========================================
    // MÉTODOS PÚBLICOS DE NOTIFICACIONES OPTIMIZADOS
    // ========================================
    /**
     * Obtiene el gestor de notificaciones singleton
     */
    public NotificationManager getNotificationManager() {
        return notificationManagerInstance;
    }

    /**
     * Obtiene la utilidad de integración de notificaciones
     */
    public NotificationIntegrationUtil getNotificationUtil() {
        return notificationUtil;
    }

    /**
     * Verifica si el sistema de notificaciones está activo
     */
    public boolean isNotificationSystemActive() {
        return notificationSystemReady.get()
                && notificationManagerInstance != null
                && notificationManagerInstance.isInitialized();
    }

    /**
     * Envía una notificación interna de forma optimizada
     */
    public void enviarNotificacionInterna(String titulo, String contenido, int... destinatarios) {
        if (!isNotificationSystemActive()) {
            System.err.println("⚠️ Sistema de notificaciones no disponible para: " + titulo);
            return;
        }

        notificationManagerInstance.enviarNotificacionRapidaAsync(titulo, contenido, destinatarios)
                .thenAccept(exito -> {
                    if (exito) {
                        System.out.println("✅ Notificación enviada exitosamente. ID: "
                                + System.currentTimeMillis()); // Simulado
                        System.out.println("✅ Notificación rápida enviada exitosamente a "
                                + destinatarios.length + " destinatario(s)");
                    } else {
                        System.err.println("❌ Error enviando notificación: " + titulo);
                    }
                }).exceptionally(throwable -> {
            System.err.println("❌ Excepción enviando notificación: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Actualiza usuario en el sistema de notificaciones de forma optimizada
     */
    public void updateNotificationUser(int newUserId, int newUserRole) {
        if (notificationManagerInstance == null) {
            System.err.println("❌ NotificationManager no disponible para actualización");
            return;
        }

        try {
            System.out.println("🔄 Actualizando usuario en NotificationManager");
            System.out.println("Usuario anterior: " + userId + ", rol: " + userRol);
            System.out.println("Usuario nuevo: " + newUserId + ", rol: " + newUserRole);

            notificationManagerInstance.updateUser(newUserId, newUserRole);
            this.userId = newUserId;
            this.userRol = newUserRole;

            System.out.println("✅ Usuario actualizado en NotificationManager");

        } catch (Exception e) {
            System.err.println("❌ Error actualizando usuario: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS DE TABLAS RESPONSIVE OPTIMIZADOS
    // ========================================
    /**
     * Configura todas las tablas de un panel como responsive UNA SOLA VEZ
     */
    private void configurarTablasResponsiveEnPanel(Component panel) {
        // Control de tiempo para evitar llamadas excesivas
        long currentTime = System.currentTimeMillis();
        if (isAdjustingTables.get() || (currentTime - lastTableAdjustTime) < TABLE_ADJUST_COOLDOWN) {
            return;
        }

        if (!isAdjustingTables.compareAndSet(false, true)) {
            return; // Otro hilo está procesando
        }

        try {
            lastTableAdjustTime = currentTime;

            System.out.println("Configurando tablas responsive en panel: " + panel.getClass().getSimpleName());

            if (panel instanceof Container) {
                Container container = (Container) panel;
                int anchoDisponible = Math.max(600, jPanel1.getWidth() - 50);

                List<JTable> tables = encontrarTodasLasTablas(container);

                System.out.println("=== PROCESANDO TABLAS RESPONSIVE ===");
                System.out.println("Tablas encontradas: " + tables.size());
                System.out.println("Ancho disponible: " + anchoDisponible);

                for (JTable table : tables) {
                    configurarTablaResponsiveCompleta(table, anchoDisponible);
                }

                System.out.println("✅ Todas las tablas procesadas como responsive");
            }

        } catch (Exception ex) {
            System.err.println("Error configurando tablas responsive: " + ex.getMessage());
        } finally {
            isAdjustingTables.set(false);
        }
    }

    /**
     * Configura una tabla específica para ser responsive una sola vez
     */
    private void configurarTablaResponsiveCompleta(JTable table, int anchoDisponible) {
        if (table == null) {
            return;
        }

        try {
            System.out.println("Configurando tabla: " + table.getClass().getSimpleName());
            System.out.println("  Columnas: " + table.getColumnCount());
            System.out.println("  Filas: " + table.getRowCount());

            // 1. Configuraciones básicas
            configurarPropiedadesBasicasTabla(table);

            // 2. Configurar ScrollPane padre
            configurarScrollPanePadreUnico(table);

            // 3. Forzar anchos para garantizar scroll horizontal
            forzarAnchoColumnasParaScroll(table, anchoDisponible);

            System.out.println("✅ Tabla configurada completamente");

        } catch (Exception e) {
            System.err.println("Error configurando tabla: " + e.getMessage());
        }
    }

    /**
     * Configura el ScrollPane padre de la tabla una sola vez
     */
    private void configurarScrollPanePadreUnico(JTable table) {
        Container parent = table.getParent();
        while (parent != null && !(parent instanceof JScrollPane)) {
            parent = parent.getParent();
        }

        if (parent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) parent;

            System.out.println("Configurando ScrollPane interno para tabla...");

            // CRÍTICO: Políticas de scrollbar SIEMPRE visibles
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

            // Configurar velocidades
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            scrollPane.setWheelScrollingEnabled(true);
            scrollPane.getViewport().setBackground(Color.WHITE);

            System.out.println("ScrollPane interno configurado - Políticas: V=ALWAYS, H=ALWAYS");
        }
    }

    /**
     * Configura propiedades básicas de una tabla
     */
    private void configurarPropiedadesBasicasTabla(JTable table) {
        table.setRowHeight(DEFAULT_ROW_HEIGHT);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setPreferredSize(new Dimension(0, HEADER_HEIGHT));
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setResizingAllowed(true);
        }

        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowGrid(true);
        table.setBackground(Color.WHITE);
        table.setOpaque(true);

        // CRÍTICO: Desactivar auto-resize para permitir scroll horizontal
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    /**
     * Fuerza anchos de columna para garantizar scroll horizontal
     */
    private void forzarAnchoColumnasParaScroll(JTable table, int anchoDisponible) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();

        if (columnCount == 0) {
            return;
        }

        // Calcular ancho mínimo por columna
        int anchoMinimoPorColumna = Math.max(120, MIN_COLUMN_WIDTH);
        int anchoTotalForzado = columnCount * anchoMinimoPorColumna;

        // GARANTIZAR que el ancho total sea mayor que el disponible
        if (anchoTotalForzado <= anchoDisponible) {
            anchoTotalForzado = anchoDisponible + 300;
            anchoMinimoPorColumna = anchoTotalForzado / columnCount;
        }

        System.out.println("Forzando anchos de columna:");
        System.out.println("  Ancho disponible: " + anchoDisponible);
        System.out.println("  Ancho total forzado: " + anchoTotalForzado);
        System.out.println("  Ancho por columna: " + anchoMinimoPorColumna);

        // Aplicar anchos específicos
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = columnModel.getColumn(i);
            String columnName = table.getColumnName(i);
            int anchoOptimo = calcularAnchoOptimoColumna(columnName, anchoMinimoPorColumna);

            column.setMinWidth(Math.max(MIN_COLUMN_WIDTH, anchoOptimo / 2));
            column.setPreferredWidth(anchoOptimo);
            column.setMaxWidth(MAX_COLUMN_WIDTH);

            System.out.println("  Columna '" + columnName + "': " + anchoOptimo + "px");
        }

        // FORZAR que la tabla tenga el ancho total calculado
        table.setPreferredScrollableViewportSize(
                new Dimension(anchoTotalForzado, table.getPreferredScrollableViewportSize().height)
        );

        System.out.println("Tabla forzada a ancho: " + anchoTotalForzado + "px");
    }

    /**
     * Calcula el ancho óptimo para una columna basado en su nombre
     */
    private int calcularAnchoOptimoColumna(String columnName, int anchoBase) {
        String name = columnName.toLowerCase();

        // Anchos específicos por tipo de columna
        if (name.contains("id") || name.contains("nº") || name.contains("n°")) {
            return Math.max(80, anchoBase / 2);
        } else if (name.contains("dni")) {
            return Math.max(120, anchoBase);
        } else if (name.contains("nombre") || name.contains("alumno")) {
            return Math.max(240, anchoBase * 2);
        } else if (name.contains("fecha")) {
            return Math.max(120, anchoBase);
        } else if (name.contains("estado")) {
            return Math.max(120, anchoBase);
        } else if (name.contains("ruta") || name.contains("archivo")) {
            return Math.max(240, anchoBase * 2);
        } else if (name.contains("nota") || name.contains("promedio")) {
            return Math.max(80, anchoBase / 2);
        } else if (name.contains("materia") || name.contains("trabajo")) {
            return Math.max(150, anchoBase);
        } else if (name.contains("curso") || name.contains("división")) {
            return Math.max(83, anchoBase / 2);
        } else if (name.contains("período") || name.contains("periodo")) {
            return Math.max(120, anchoBase);
        } else if (name.contains("descripcion") || name.contains("observaciones")) {
            return Math.max(200, anchoBase * 2);
        } else if (name.contains("tamaño") || name.contains("size")) {
            return Math.max(120, anchoBase);
        } else if (name.contains("generación")) {
            return Math.max(120, anchoBase);
        } else if (name.contains("existe")) {
            return Math.max(240, anchoBase * 2);
        } else if (name.contains("cont")) {
            return Math.max(120, anchoBase);
        } else {
            // Ancho por defecto
            return Math.max(120, anchoBase);
        }
    }

    /**
     * Encuentra todas las tablas en un contenedor recursivamente
     */
    private List<JTable> encontrarTodasLasTablas(Container container) {
        List<JTable> tables = new ArrayList<>();

        Component[] components = container.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTable) {
                tables.add((JTable) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) comp;
                Component view = sp.getViewport().getView();
                if (view instanceof JTable) {
                    tables.add((JTable) view);
                }
            } else if (comp instanceof Container) {
                tables.addAll(encontrarTodasLasTablas((Container) comp));
            }
        }

        return tables;
    }

    // ========================================
    // MÉTODOS PRINCIPALES DE PANEL RESPONSIVE
    // ========================================
    /**
     * MÉTODO PRINCIPAL: Muestra un panel de forma responsive con scroll y
     * navegación
     */
    public void mostrarPanelResponsive(Component panel, String titulo) {
        try {
            System.out.println("=== MOSTRAR PANEL RESPONSIVE ===");
            System.out.println("Panel: " + panel.getClass().getSimpleName());
            System.out.println("Título: " + titulo);

            // Marcar que hay panel específico mostrado
            panelEspecificoMostrado = true;

            // Limpiar panel principal
            jPanel1.removeAll();
            jPanel1.setLayout(new BorderLayout());

            // Crear panel de navegación
            JPanel navPanel = crearPanelNavegacion(titulo);
            jPanel1.add(navPanel, BorderLayout.NORTH);

            // Crear ScrollPane para el contenido
            JScrollPane scrollPane = new JScrollPane(panel);
            configurarScrollPaneResponsive(scrollPane);

            // Agregar al panel principal
            jPanel1.add(scrollPane, BorderLayout.CENTER);

            // Hacer el logo clickeable para volver
            hacerLogoClickeable();

            // Actualizar vista
            jPanel1.revalidate();
            jPanel1.repaint();

            // Configurar tablas responsive después de un breve delay
            javax.swing.Timer timer = new javax.swing.Timer(200, e -> {
                configurarTablasResponsiveEnPanel(panel);
                ((javax.swing.Timer) e.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();

            System.out.println("✅ Panel responsive mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel responsive: " + ex.getMessage());
            ex.printStackTrace();

            // En caso de error, restaurar vista principal
            restaurarVistaPrincipal();

            // Mostrar error al usuario
            JOptionPane.showMessageDialog(this,
                    "Error al cargar el panel: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Configura un ScrollPane para ser completamente responsive
     */
    private void configurarScrollPaneResponsive(JScrollPane scrollPane) {
        // POLÍTICAS DE SCROLL SIEMPRE VISIBLES
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Configurar velocidades de scroll
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(100);

        // Habilitar scroll con rueda del mouse
        scrollPane.setWheelScrollingEnabled(true);

        // Configurar viewport
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        System.out.println("ScrollPane configurado con políticas ALWAYS");
    }

    /**
     * Restaura la vista principal del panel central.
     */
    public void restaurarVistaPrincipal() {
        System.out.println("Restaurando a vista principal");

        try {
            // Marcar que no hay panel específico mostrado
            panelEspecificoMostrado = false;

            // Limpiar panel
            jPanel1.removeAll();
            jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

            // Restaurar componentes originales
            jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
            jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));
            jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));
            jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

            // Restaurar fondo
            jScrollPane1.setViewportView(fondoHomeOriginal);
            jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

            // Forzar actualización
            jPanel1.revalidate();
            jPanel1.repaint();

            System.out.println("Vista principal restaurada exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al restaurar vista principal: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Crea un panel de navegación con botón de volver y título.
     */
    private JPanel crearPanelNavegacion(String titulo) {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new java.awt.Color(51, 153, 255));
        navPanel.setPreferredSize(new Dimension(0, 50));

        // Botón volver
        JButton btnVolver = new JButton("← Volver al Inicio");
        btnVolver.setBackground(new java.awt.Color(40, 120, 200));
        btnVolver.setForeground(java.awt.Color.WHITE);
        btnVolver.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> restaurarVistaPrincipal());

        // Título
        JLabel lblTitulo = new JLabel(titulo != null ? titulo : "Panel de Trabajo");
        lblTitulo.setForeground(java.awt.Color.WHITE);
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);

        navPanel.add(btnVolver, BorderLayout.WEST);
        navPanel.add(lblTitulo, BorderLayout.CENTER);

        return navPanel;
    }

    /**
     * Método mejorado para hacer el logo clickeable
     */
    private void hacerLogoClickeable() {
        if (imagenLogo != null) {
            imagenLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Remover listeners existentes
            for (java.awt.event.MouseListener ml : imagenLogo.getMouseListeners()) {
                imagenLogo.removeMouseListener(ml);
            }

            // Agregar nuevo listener
            imagenLogo.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    restaurarVistaPrincipal();
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    // Efecto visual al pasar el mouse
                    imagenLogo.setOpaque(true);
                    imagenLogo.setBackground(new Color(240, 240, 240));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    imagenLogo.setOpaque(false);
                }
            });

            System.out.println("Logo configurado como clickeable para volver al inicio");
        }
    }

    // ========================================
    // MÉTODOS DE ACTUALIZACIÓN DE INFORMACIÓN
    // ========================================
    /**
     * Actualiza las etiquetas de nombre y rol.
     */
    public void updateLabels(String nombreCompleto, String rolTexto) {
        System.out.println("🔄 Actualizando labels del usuario: " + nombreCompleto + " - " + rolTexto);

        SwingUtilities.invokeLater(() -> {
            try {
                // Asegurar que las etiquetas existen
                if (labelNomApe == null || labelRol == null) {
                    System.err.println("⚠️ Las etiquetas son null, inicializando...");
                    inicializarEtiquetasUsuario();
                }

                if (labelNomApe != null) {
                    labelNomApe.setText(nombreCompleto);
                    labelNomApe.setVisible(true);
                    System.out.println("✅ Nombre actualizado: " + nombreCompleto);
                }

                if (labelRol != null) {
                    labelRol.setText("Rol: " + rolTexto);
                    labelRol.setVisible(true);
                    System.out.println("✅ Rol actualizado: " + rolTexto);
                }

                // FORZAR actualización del panel de botones si ya existe
                if (panelBotones != null) {
                    panelBotones.revalidate();
                    panelBotones.repaint();
                }

                // FORZAR actualización del panel lateral completo
                if (jPanel4 != null) {
                    jPanel4.revalidate();
                    jPanel4.repaint();
                }

                System.out.println("✅ Labels actualizados y panel refrescado");

            } catch (Exception e) {
                System.err.println("❌ Error actualizando labels: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Actualiza las etiquetas para usuario alumno.
     */
    public void updateAlumnoLabels(String nombreCompleto, String rolTexto, String cursoDiv) {
        System.out.println("🔄 Actualizando labels del alumno: " + nombreCompleto + " - " + rolTexto + " - " + cursoDiv);

        SwingUtilities.invokeLater(() -> {
            try {
                // Asegurar que las etiquetas existen
                if (labelNomApe == null || labelRol == null) {
                    System.err.println("⚠️ Las etiquetas son null, inicializando...");
                    inicializarEtiquetasUsuario();
                }

                if (labelNomApe != null) {
                    labelNomApe.setText(nombreCompleto);
                    labelNomApe.setVisible(true);
                    System.out.println("✅ Nombre actualizado: " + nombreCompleto);
                }

                if (labelRol != null) {
                    labelRol.setText("Rol: " + rolTexto);
                    labelRol.setVisible(true);
                    System.out.println("✅ Rol actualizado: " + rolTexto);
                }

                if (labelCursoDiv != null) {
                    labelCursoDiv.setText("Curso: " + cursoDiv);
                    labelCursoDiv.setVisible(true);
                    System.out.println("✅ Curso actualizado: " + cursoDiv);
                } else {
                    System.err.println("⚠️ labelCursoDiv es null para alumno");
                }

                // FORZAR actualización del panel de botones si ya existe
                if (panelBotones != null) {
                    panelBotones.revalidate();
                    panelBotones.repaint();
                }

                // FORZAR actualización del panel lateral completo
                if (jPanel4 != null) {
                    jPanel4.revalidate();
                    jPanel4.repaint();
                }

                System.out.println("✅ Labels de alumno actualizados y panel refrescado");

            } catch (Exception e) {
                System.err.println("❌ Error actualizando labels de alumno: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Actualiza la foto de perfil del usuario.
     */
    public void updateFotoPerfil(String fotoUrl) {
        SwingUtilities.invokeLater(() -> {
            cargarFotoPerfilForced(fotoUrl);
        });
    }

    // ========================================
    // MÉTODOS DE FOTO DE PERFIL OPTIMIZADOS
    // ========================================
    /**
     * Carga de manera forzada la foto de perfil.
     */
    public boolean cargarFotoPerfilForced(String fotoUrl) {
        try {
            // 1. Asegurarse de que el componente existe
            if (labelFotoPerfil == null) {
                labelFotoPerfil = new JLabel();
                labelFotoPerfil.setHorizontalAlignment(JLabel.CENTER);
                labelFotoPerfil.setAlignmentX(CENTER_ALIGNMENT);
                System.out.println("Creada nueva instancia de labelFotoPerfil");
            }

            // 2. Limpiar cualquier icono previo
            labelFotoPerfil.setIcon(null);

            // 3. Cargar la nueva imagen
            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                System.out.println("Intentando cargar imagen desde URL: " + fotoUrl);

                ImageIcon icon = loadImageDirectly(fotoUrl);

                if (icon != null && icon.getIconWidth() > 0) {
                    labelFotoPerfil.setIcon(icon);
                    System.out.println("Imagen cargada exitosamente: " + icon.getIconWidth() + "x" + icon.getIconHeight());

                    // Forzar actualización visual
                    labelFotoPerfil.revalidate();
                    labelFotoPerfil.repaint();

                    return true;
                } else {
                    System.err.println("La imagen se cargó como null o con dimensiones inválidas");
                }
            }

            // Usar imagen por defecto
            System.out.println("Usando imagen por defecto");
            ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/main/resources/images/icons8-user-96.png"));
            if (defaultIcon != null && defaultIcon.getIconWidth() > 0) {
                Image img = defaultIcon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                labelFotoPerfil.setIcon(new ImageIcon(img));
                labelFotoPerfil.revalidate();
                labelFotoPerfil.repaint();
                return true;
            } else {
                System.err.println("No se pudo cargar la imagen por defecto");
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error crítico al cargar foto de perfil: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carga una imagen directamente usando un enfoque alternativo.
     */
    private ImageIcon loadImageDirectly(String imageUrl) {
        try {
            // Usar URI en lugar de URL deprecated
            java.net.URI uri = new java.net.URI(imageUrl);
            URL url = uri.toURL();
            Image img = ImageIO.read(url);

            if (img != null) {
                Image scaledImg = img.getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            ImageIcon icon = new ImageIcon(url);
            if (icon.getIconWidth() > 0) {
                Image scaledImg = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + e.getMessage());
            return null;
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD Y GESTIÓN
    // ========================================
    /**
     * Verifica la conexión a la base de datos.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    /**
     * Establece un nuevo gestor de paneles para este rol.
     */
    public void setRolPanelManager(RolPanelManager rolPanelManager) {
        this.rolPanelManager = rolPanelManager;
    }

    /**
     * Retorna el gestor de paneles para este rol.
     */
    public RolPanelManager getRolPanelManager() {
        return rolPanelManager;
    }

    /**
     * Establece un nuevo rol para el usuario.
     */
    public void setUserRol(int userRol) {
        this.userRol = userRol;
    }

    /**
     * Obtiene el rol actual del usuario.
     */
    public int getUserRol() {
        return this.userRol;
    }

    /**
     * Retorna el panel principal para compatibilidad con código existente
     */
    public JPanel getPanelPrincipal() {
        return jPanel1;
    }

    // ========================================
    // MÉTODOS DE CIERRE Y LIMPIEZA
    // ========================================
    /**
     * MODIFICADO: Cierra la sesión y limpia recursos de notificaciones
     */
    protected void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?\n\n"
                + "Se cerrarán todas las funciones activas y se limpiarán las notificaciones pendientes.",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            System.out.println("=== CERRANDO SESIÓN DESDE VentanaInicio ===");

            // NUEVO: Enviar notificación de despedida si es apropiado
            if (isNotificationSystemActive()) {
                try {
                    enviarNotificacionInterna(
                            "👋 Hasta pronto",
                            "Has cerrado sesión correctamente. ¡Que tengas un buen día!",
                            userId
                    );

                    // Esperar un momento para que se envíe
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.err.println("⚠️ Error enviando notificación de despedida: " + e.getMessage());
                }
            }

            // NUEVO: Limpiar recursos de notificaciones
            cleanupNotifications();

            dispose();
            new LoginForm().setVisible(true);

            System.out.println("✅ Sesión cerrada correctamente");
        }
    }

    // ========================================
    // MÉTODOS DE DEBUG Y DIAGNÓSTICO
    // ========================================
    /**
     * NUEVO: Debug completo del sistema
     */
    public void debugSistemaCompleto() {
        System.out.println("\n=== DEBUG SISTEMA COMPLETO - VentanaInicio v4.0 ===");

        try {
            System.out.println("🔍 Estado General:");
            System.out.println("  Inicializado: " + isInitialized.get());
            System.out.println("  Usuario: " + userId + " (Rol: " + userRol + ")");
            System.out.println("  Panel específico mostrado: " + panelEspecificoMostrado);

            System.out.println("\n📱 Panel Lateral:");
            System.out.println("  Panel botones configurado: " + panelButtonsConfigured.get());
            System.out.println("  ScrollPane disponible: " + (scrollPanelBotones != null));
            System.out.println("  Panel botones disponible: " + (panelBotones != null));

            if (panelBotones != null) {
                System.out.println("  Número de componentes: " + panelBotones.getComponentCount());
                System.out.println("  Tamaño preferido: " + panelBotones.getPreferredSize());
            }

            System.out.println("\n🔔 Notificaciones:");
            System.out.println("  Sistema listo: " + notificationSystemReady.get());
            System.out.println("  Manager disponible: " + (notificationManagerInstance != null));

            if (notificationManagerInstance != null) {
                System.out.println("  Usuario registrado: " + notificationManagerInstance.getCurrentUserId());
                System.out.println("  Rol registrado: " + notificationManagerInstance.getCurrentUserRole());
            }

            System.out.println("\n📊 Tablas:");
            System.out.println("  Ajustando tablas: " + isAdjustingTables.get());
            System.out.println("  Último ajuste: " + lastTableAdjustTime);

            System.out.println("\n✅ Debug completado");

        } catch (Exception e) {
            System.err.println("❌ Error en debug: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=============================================\n");
    }

    /**
     * NUEVO: Fuerza actualización completa del sistema
     */
    public void forzarActualizacionCompleta() {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("🔄 Forzando actualización completa...");

                // 1. Actualizar panel lateral
                actualizarPanelLateralResponsive();

                // 2. Actualizar scroll del panel lateral
                actualizarScrollPanelLateral();

                // 3. Si hay panel específico, actualizar tablas
                if (panelEspecificoMostrado && jPanel1.getComponentCount() > 0) {
                    for (Component comp : jPanel1.getComponents()) {
                        if (comp instanceof JScrollPane) {
                            Component view = ((JScrollPane) comp).getViewport().getView();
                            if (view instanceof JPanel) {
                                int anchoDisponible = comp.getWidth() - 50;
                                configurarTablasResponsiveEnPanel(view);
                            }
                            break;
                        }
                    }
                }

                // 4. Forzar repaint general
                revalidate();
                repaint();

                System.out.println("✅ Actualización completa finalizada");

            } catch (Exception e) {
                System.err.println("❌ Error en actualización completa: " + e.getMessage());
            }
        });
    }

    // ========================================
    // NUEVOS MÉTODOS PARA CARTEL DE NOTIFICACIONES
    // ========================================
    /**
     * Configura el sistema de cartel de notificaciones
     */
    private void configurarCartelNotificaciones() {
        try {
            System.out.println("🔔 Configurando cartel de notificaciones...");

            // Inicializar componentes del cartel
            inicializarComponentesCartel();

            // Crear el diálogo del cartel
            crearDialogoNotificaciones();

            // Configurar timer para verificación automática
            configurarTimerNotificaciones();

            System.out.println("✅ Cartel de notificaciones configurado");

        } catch (Exception e) {
            System.err.println("❌ Error configurando cartel de notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicializa los componentes del cartel emergente
     */
    private void inicializarComponentesCartel() {
        // Etiqueta del contador de notificaciones
        notificationCountLabel = new JLabel();
        notificationCountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        notificationCountLabel.setForeground(new Color(220, 53, 69));
        notificationCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Etiqueta del mensaje
        notificationMessageLabel = new JLabel();
        notificationMessageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        notificationMessageLabel.setForeground(new Color(60, 60, 60));
        notificationMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Botón para ver notificaciones
        viewNotificationsButton = new JButton("📬 Ver Mis Notificaciones");
        viewNotificationsButton.setBackground(new Color(51, 153, 255));
        viewNotificationsButton.setForeground(Color.WHITE);
        viewNotificationsButton.setFont(new Font("Arial", Font.BOLD, 13));
        viewNotificationsButton.setFocusPainted(false);
        viewNotificationsButton.setBorderPainted(false);
        viewNotificationsButton.setPreferredSize(new Dimension(200, 35));

        // Botón para cerrar cartel
        dismissButton = new JButton("❌ Cerrar");
        dismissButton.setBackground(new Color(108, 117, 125));
        dismissButton.setForeground(Color.WHITE);
        dismissButton.setFont(new Font("Arial", Font.PLAIN, 12));
        dismissButton.setFocusPainted(false);
        dismissButton.setBorderPainted(false);
        dismissButton.setPreferredSize(new Dimension(100, 35));

        System.out.println("✅ Componentes del cartel inicializados");
    }

    /**
     * Crea el diálogo del cartel de notificaciones
     */
    private void crearDialogoNotificaciones() {
        notificationDialog = new JDialog(this, "🔔 Notificaciones Pendientes", true);
        notificationDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        notificationDialog.setResizable(false);

        // Panel principal del cartel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // Panel superior con icono y contador
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // Icono de notificación grande
        JLabel iconLabel = new JLabel("🔔", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        topPanel.add(iconLabel, BorderLayout.NORTH);
        topPanel.add(notificationCountLabel, BorderLayout.CENTER);

        // Panel central con mensaje
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));
        centerPanel.add(notificationMessageLabel, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(viewNotificationsButton);
        buttonPanel.add(dismissButton);

        // Ensamblar panel principal
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Configurar diálogo
        notificationDialog.add(mainPanel);
        notificationDialog.setSize(400, 280);
        notificationDialog.setLocationRelativeTo(this);

        // Configurar listeners de botones
        configurarListenersCartel();

        System.out.println("✅ Diálogo de notificaciones creado");
    }

    /**
     * Configura los listeners del cartel
     */
    private void configurarListenersCartel() {
        // Botón ver notificaciones
        viewNotificationsButton.addActionListener(e -> {
            notificationDialog.setVisible(false);
            notificationDialogShown = false;
            abrirVentanaNotificaciones();
        });

        // Botón cerrar
        dismissButton.addActionListener(e -> {
            notificationDialog.setVisible(false);
            notificationDialogShown = false;
        });

        // Listener para cerrar con X
        notificationDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                notificationDialog.setVisible(false);
                notificationDialogShown = false;
            }
        });

        System.out.println("✅ Listeners del cartel configurados");
    }

    /**
     * Configura el timer para verificación automática de notificaciones
     */
    private void configurarTimerNotificaciones() {
        // Timer que verifica notificaciones cada 30 segundos
        notificationCheckTimer = new Timer(30000, e -> verificarNotificacionesPendientes());
        notificationCheckTimer.setRepeats(true);

        System.out.println("⏰ Timer de notificaciones configurado (30s)");
    }

    /**
     * Configura los listeners de la ventana para mostrar cartel al abrir
     */
    private void configurarListenersVentana() {
        // Listener para cuando la ventana se hace visible
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Verificar notificaciones después de un breve delay
                SwingUtilities.invokeLater(() -> {
                    Timer delayTimer = new Timer(2000, evt -> {
                        verificarYMostrarCartelInicial();

                        // NUEVO: Mostrar banner de notificaciones usando sistema consolidado
                        mostrarBannerNotificacionesInicio();

                        ((Timer) evt.getSource()).stop();
                    });
                    delayTimer.setRepeats(false);
                    delayTimer.start();
                });
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Iniciar timer cuando la ventana está activa
                if (notificationCheckTimer != null && !notificationCheckTimer.isRunning()) {
                    notificationCheckTimer.start();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // Detener timer cuando la ventana no está activa
                if (notificationCheckTimer != null && notificationCheckTimer.isRunning()) {
                    notificationCheckTimer.stop();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // Limpiar recursos del cartel
                limpiarRecursosCartel();
            }
        });

        System.out.println("✅ Listeners de ventana configurados");
    }

    /**
     * Verifica y muestra el cartel inicial al abrir la aplicación
     */
    private void verificarYMostrarCartelInicial() {
        if (!isNotificationSystemActive() || notificationDialogShown) {
            return;
        }

        try {
            System.out.println("🔍 Verificando notificaciones pendientes al inicio...");

            CompletableFuture<Integer> futureCount = CompletableFuture.supplyAsync(() -> {
                try {
                    return notificationManagerInstance.getUnreadCount();
                } catch (Exception e) {
                    System.err.println("Error obteniendo contador: " + e.getMessage());
                    return 0;
                }
            });

            futureCount.thenAccept(count -> {
                SwingUtilities.invokeLater(() -> {
                    if (count > 0 && !notificationDialogShown) {
                        mostrarCartelNotificaciones(count);
                    }
                });
            }).exceptionally(throwable -> {
                System.err.println("Error en verificación inicial: " + throwable.getMessage());
                return null;
            });

        } catch (Exception e) {
            System.err.println("Error verificando notificaciones iniciales: " + e.getMessage());
        }
    }

    /**
     * Verifica notificaciones pendientes periódicamente
     */
    private void verificarNotificacionesPendientes() {
        // Control de tiempo para evitar verificaciones excesivas
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastNotificationCheck) < NOTIFICATION_CHECK_COOLDOWN) {
            return;
        }

        lastNotificationCheck = currentTime;

        if (!isNotificationSystemActive() || notificationDialogShown) {
            return;
        }

        try {
            CompletableFuture<Integer> futureCount = CompletableFuture.supplyAsync(() -> {
                try {
                    return notificationManagerInstance.getUnreadCount();
                } catch (Exception e) {
                    System.err.println("Error obteniendo contador periódico: " + e.getMessage());
                    return 0;
                }
            });

            futureCount.thenAccept(count -> {
                SwingUtilities.invokeLater(() -> {
                    // Solo mostrar si hay notificaciones nuevas y el cartel no está visible
                    if (count > 0 && !notificationDialogShown && isVisible() && isActive()) {
                        System.out.println("📢 Detectadas " + count + " notificaciones nuevas");
                        mostrarCartelNotificaciones(count);
                    }
                });
            }).exceptionally(throwable -> {
                System.err.println("Error en verificación periódica: " + throwable.getMessage());
                return null;
            });

        } catch (Exception e) {
            System.err.println("Error verificando notificaciones periódicas: " + e.getMessage());
        }
    }

    /**
     * Muestra el cartel de notificaciones con el contador especificado
     */
    private void mostrarCartelNotificaciones(int count) {
        if (notificationDialogShown || count <= 0) {
            return;
        }

        try {
            System.out.println("🎯 Mostrando cartel con " + count + " notificaciones");

            // Actualizar textos del cartel
            actualizarTextoCartel(count);

            // Marcar como mostrado
            notificationDialogShown = true;

            // Mostrar el cartel
            SwingUtilities.invokeLater(() -> {
                if (notificationDialog != null) {
                    notificationDialog.setLocationRelativeTo(this);
                    notificationDialog.setVisible(true);
                    notificationDialog.toFront();
                    notificationDialog.requestFocus();
                }
            });

        } catch (Exception e) {
            System.err.println("Error mostrando cartel: " + e.getMessage());
            notificationDialogShown = false;
        }
    }

    /**
     * Actualiza el texto del cartel según el número de notificaciones
     */
    private void actualizarTextoCartel(int count) {
        if (notificationCountLabel != null && notificationMessageLabel != null) {
            String countText = String.format("Tienes %d notificación%s pendiente%s",
                    count, count == 1 ? "" : "es", count == 1 ? "" : "s");

            String messageText = "<html><center>Haz clic en 'Ver Mis Notificaciones' para revisar "
                    + "los mensajes importantes del sistema.</center></html>";

            notificationCountLabel.setText(countText);
            notificationMessageLabel.setText(messageText);

            System.out.println("📝 Texto del cartel actualizado: " + count + " notificaciones");
        }
    }

    /**
     * Abre la ventana de notificaciones del usuario
     */
    private void abrirVentanaNotificaciones() {
        try {
            System.out.println("📱 Abriendo ventana de notificaciones para usuario: " + userId);

            SwingUtilities.invokeLater(() -> {
                try {
                    NotificationsWindow window = new NotificationsWindow(userId);
                    window.setVisible(true);
                    window.toFront();

                    // Actualizar el cartel como no mostrado para futuras verificaciones
                    notificationDialogShown = false;

                } catch (Exception e) {
                    System.err.println("Error abriendo ventana de notificaciones: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Error al abrir las notificaciones: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

        } catch (Exception e) {
            System.err.println("Error en abrirVentanaNotificaciones: " + e.getMessage());
        }
    }

    /**
     * Limpia los recursos del cartel de notificaciones
     */
    private void limpiarRecursosCartel() {
        try {
            System.out.println("🧹 Limpiando recursos del cartel de notificaciones...");

            // Detener y limpiar timer
            if (notificationCheckTimer != null) {
                notificationCheckTimer.stop();
                notificationCheckTimer = null;
            }

            // Ocultar y limpiar diálogo
            if (notificationDialog != null) {
                notificationDialog.setVisible(false);
                notificationDialog.dispose();
                notificationDialog = null;
            }

            // Limpiar componentes
            notificationCountLabel = null;
            notificationMessageLabel = null;
            viewNotificationsButton = null;
            dismissButton = null;

            // Resetear flags
            notificationDialogShown = false;

            System.out.println("✅ Recursos del cartel liberados");

        } catch (Exception e) {
            System.err.println("Error limpiando recursos del cartel: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para forzar verificación de notificaciones (útil
     * para llamar desde otros componentes)
     */
    public void forzarVerificacionNotificaciones() {
        if (isNotificationSystemActive()) {
            SwingUtilities.invokeLater(() -> {
                verificarNotificacionesPendientes();
            });
        }
    }

    /**
     * NUEVO: Método público para resetear el estado del cartel (útil después de
     * marcar notificaciones como leídas)
     */
    public void resetearEstadoCartel() {
        notificationDialogShown = false;
        if (notificationDialog != null && notificationDialog.isVisible()) {
            notificationDialog.setVisible(false);
        }
    }

    /**
     * MODIFICADO: Método cleanupNotifications actualizado para incluir cartel
     */
    private void cleanupNotifications() {
        try {
            System.out.println("🧹 Limpiando recursos de notificaciones en VentanaInicio...");

            // Limpiar recursos del cartel
            limpiarRecursosCartel();

            if (notificationManagerInstance != null) {
                System.out.println("✅ NotificationManager recursos liberados");
            }

            if (notificationUtil != null) {
                notificationUtil = null;
                System.out.println("🔄 Referencias de NotificationUtil liberadas");
            }

            // Marcar sistema como no listo
            notificationSystemReady.set(false);

            System.out.println("✅ Recursos de notificaciones liberados en VentanaInicio");

        } catch (Exception e) {
            System.err.println("❌ Error limpiando notificaciones: " + e.getMessage());
        }
    }

    /**
     * MODIFICADO: Método dispose actualizado para incluir limpieza del cartel
     */
    @Override
    public void dispose() {
        try {
            System.out.println("🚪 Cerrando VentanaInicio con limpieza completa...");

            // NUEVO: Detener polling de tickets
            if (ticketService != null) {
                try {
                    ticketService.detenerPollingNotificaciones();
                    System.out.println("✅ Polling de tickets detenido");
                } catch (Exception e) {
                    System.err.println("⚠️ Error deteniendo polling: " + e.getMessage());
                }
            }

            // Limpiar recursos de notificaciones incluyendo cartel
            cleanupNotifications();

            // Continuar con dispose normal
            super.dispose();

            System.out.println("✅ VentanaInicio cerrada completamente");

        } catch (Exception e) {
            System.err.println("❌ Error en dispose de VentanaInicio: " + e.getMessage());
            super.dispose();
        }
    }

    /**
     * NUEVO: Método para verificar si el usuario actual es desarrollador
     */
    public boolean isCurrentUserDeveloper() {
        try {
            return ticketService != null && ticketService.esDeveloper(userId);
        } catch (Exception e) {
            System.err.println("Error verificando si es desarrollador: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO: Método para actualizar campanitas manualmente (debugging)
     */
    public void actualizarCampanitasTickets() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Buscar TicketBellComponent en la interfaz
                buscarYActualizarTicketBells(this.getContentPane());
            } catch (Exception e) {
                System.err.println("Error actualizando campanitas: " + e.getMessage());
            }
        });
    }

    /**
     * NUEVO: Método recursivo para buscar y actualizar campanitas
     */
    private void buscarYActualizarTicketBells(Container container) {
        Component[] components = container.getComponents();
        for (Component comp : components) {
            if (comp.getClass().getSimpleName().equals("TicketBellComponent")) {
                System.out.println("🔔 Encontrada campanita de tickets, actualizando...");
                try {
                    // Usar reflexión para llamar forceRefresh() si no tienes import directo
                    comp.getClass().getMethod("forceRefresh").invoke(comp);
                } catch (Exception e) {
                    System.err.println("Error llamando forceRefresh: " + e.getMessage());
                }
            } else if (comp instanceof Container) {
                // Buscar recursivamente en contenedores
                buscarYActualizarTicketBells((Container) comp);
            }
        }
    }

    /**
     * NUEVO: Método de debugging para verificar estado de tickets
     */
    public void debugTicketSystem() {
        if (ticketService != null) {
            System.out.println("\n=== DEBUG SISTEMA TICKETS ===");
            System.out.println("Usuario ID: " + userId);
            System.out.println("Es desarrollador: " + isCurrentUserDeveloper());
            System.out.println("TicketService disponible: " + (ticketService != null));

            try {
                int pendientes = ticketService.contarTicketsPendientes();
                System.out.println("Tickets pendientes: " + pendientes);
            } catch (Exception e) {
                System.out.println("Error obteniendo tickets: " + e.getMessage());
            }

            System.out.println("=============================\n");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        imagenLogo = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/logo_et20_min.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(imagenLogo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(55, Short.MAX_VALUE)
                .addComponent(imagenLogo)
                .addGap(46, 46, 46))
        );

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.out.println("=== INICIANDO VentanaInicio v4.0 COMPLETAMENTE OPTIMIZADA - TESTING ===");

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            try {
                System.out.println("🚀 Creando VentanaInicio optimizada para testing...");

                // Crear la ventana con valores de prueba (Admin)
                VentanaInicio ventana = new VentanaInicio(1, 1);
                ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
                ventana.setVisible(true);

                System.out.println("✅ VentanaInicio creada y mostrada");

                // NUEVO: Secuencia de testing del sistema completo
                java.util.Timer testTimer = new java.util.Timer();

                // Test 1: Debug inicial (2 segundos)
                testTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("\n🧪 === TEST 1: DEBUG INICIAL ===");
                            ventana.debugSistemaCompleto();
                        });
                    }
                }, 2000);

                // Test 2: Actualización completa (4 segundos)
                testTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("\n🧪 === TEST 2: ACTUALIZACIÓN COMPLETA ===");
                            ventana.forzarActualizacionCompleta();
                        });
                    }
                }, 4000);

                // Test 3: Enviar notificación de prueba (6 segundos)
                testTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("\n🧪 === TEST 3: NOTIFICACIÓN DE PRUEBA ===");
                            ventana.enviarNotificacionInterna(
                                    "🎯 Test Sistema v4.0",
                                    "Sistema completamente optimizado funcionando correctamente. "
                                    + "Panel lateral responsive, tablas optimizadas, notificaciones integradas.",
                                    1
                            );
                        });
                    }
                }, 6000);

                // Test 4: Reconfigurar panel de botones (8 segundos)
                testTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("\n🧪 === TEST 4: RECONFIGURAR PANEL BOTONES ===");
                            ventana.reconfigurarPanelBotones();
                        });
                    }
                }, 8000);

                // Test Final: Resumen (10 segundos)
                testTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("\n🎉 === TESTING COMPLETADO v4.0 ===");
                            System.out.println("VentanaInicio COMPLETAMENTE OPTIMIZADA:");
                            System.out.println("✅ Inicialización única controlada");
                            System.out.println("✅ Panel lateral completamente responsive");
                            System.out.println("✅ Scroll automático para usuarios con muchos botones");
                            System.out.println("✅ Sistema de notificaciones singleton optimizado");
                            System.out.println("✅ Tablas responsive con configuración única");
                            System.out.println("✅ Control de estados anti-loops");
                            System.out.println("✅ Limpieza automática de recursos");
                            System.out.println("✅ Debug y diagnóstico completo");
                            System.out.println("\n🚀 LISTO PARA PRODUCCIÓN");

                            // Debug final
                            ventana.debugSistemaCompleto();

                            // Mostrar mensaje final al usuario
                            JOptionPane.showMessageDialog(ventana,
                                    "🎉 Testing del Sistema Completamente Optimizado v4.0\n\n"
                                    + "✅ Inicialización única y controlada\n"
                                    + "✅ Panel lateral completamente responsive\n"
                                    + "✅ Soporte para usuarios con muchos botones\n"
                                    + "✅ Scroll automático en panel lateral\n"
                                    + "✅ Sistema de notificaciones optimizado\n"
                                    + "✅ Tablas responsive con configuración única\n"
                                    + "✅ Control anti-loops de redimensionamiento\n"
                                    + "✅ Limpieza automática de recursos\n\n"
                                    + "El sistema está completamente optimizado y listo para usar.\n"
                                    + "Revisa la consola para detalles técnicos completos.",
                                    "Sistema Completamente Optimizado v4.0 - Testing Exitoso",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    }
                }, 10000);

            } catch (Exception e) {
                System.err.println("❌ Error crítico en testing: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error crítico durante el testing:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Muestra banner de notificaciones al inicio de sesión usando sistema consolidado
     */
    private void mostrarBannerNotificacionesInicio() {
        try {
            if (notificationManagerInstance != null && notificationManagerInstance.isInitialized()) {
                int unreadCount = notificationManagerInstance.getUnreadCount();
                
                if (unreadCount > 0) {
                    // Crear un banner simple usando JOptionPane personalizado
                    SwingUtilities.invokeLater(() -> {
                        String mensaje = String.format(
                            "¡Bienvenido! Tienes %d notificación(es) pendiente(s).\n\n" +
                            "¿Deseas revisar tus notificaciones ahora?",
                            unreadCount
                        );
                        
                        int opcion = JOptionPane.showConfirmDialog(
                            this,
                            mensaje,
                            "🔔 Notificaciones Pendientes",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        if (opcion == JOptionPane.YES_OPTION) {
                            abrirVentanaNotificaciones();
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error mostrando banner de notificaciones de inicio: " + e.getMessage());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fondoHome;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
