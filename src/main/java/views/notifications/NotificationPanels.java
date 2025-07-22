package main.java.views.notifications;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.Timer;
import main.java.database.Conexion;
import main.java.models.NotificationModels.NotificacionDestinatario;
import main.java.services.NotificationCore.NotificationService;
import main.java.services.NotificationCore.NotificationIntegrationUtil;

/**
 * Consolidación de todos los paneles específicos de notificaciones por rol.
 * Este archivo reemplaza múltiples archivos dispersos de paneles de notificaciones.
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Consolidado
 */
public class NotificationPanels {

    // ========================================
    // INTERFAZ COMÚN PARA LISTENERS
    // ========================================
    
    public interface ExpandableNotificationListener {
        void onNotificationToggled(NotificationPanels.ExpandableNotificationPanel panel);
        void onNotificationDeleted(NotificationPanels.ExpandableNotificationPanel panel);
        void onNotificationRead(NotificationPanels.ExpandableNotificationPanel panel);
    }

    // ========================================
    // PANEL EXPANDIBLE PARA NOTIFICACIONES
    // ========================================
    
    public static class ExpandableNotificationPanel extends JPanel {
        
        // Campos principales
        private final NotificacionDestinatario notification;
        private final NotificationService notificationService;
        private final ExpandableNotificationListener listener;
        private final int userId;

        // Estado del panel
        private volatile boolean isExpanded = false;
        private volatile boolean isAnimating = false;
        private volatile boolean isProcessingRead = false;

        // Dimensiones
        private static final int COLLAPSED_HEIGHT = 80;
        private static final int EXPANDED_HEIGHT = 180;
        private static final int ANIMATION_SPEED = 15;
        private static final int ANIMATION_STEP = 8;

        // Colores
        private static final Color UNREAD_BACKGROUND = new Color(255, 248, 220);
        private static final Color READ_BACKGROUND = Color.WHITE;
        private static final Color URGENT_BACKGROUND = new Color(255, 235, 235);
        private static final Color HIGH_BACKGROUND = new Color(255, 248, 220);
        private static final Color NORMAL_BACKGROUND = new Color(240, 248, 255);
        private static final Color URGENT_BORDER = new Color(220, 53, 69);
        private static final Color HIGH_BORDER = new Color(255, 193, 7);
        private static final Color NORMAL_BORDER = new Color(0, 123, 255);
        private static final Color LOW_BORDER = new Color(108, 117, 125);

        // Componentes UI
        private JPanel headerPanel;
        private JPanel expandedPanel;
        private JLabel statusIndicator;
        private JLabel titleLabel;
        private JLabel senderLabel;
        private JLabel timeLabel;
        private JLabel priorityLabel;
        private JButton expandButton;
        private JButton markReadButton;
        private JButton actionsButton;
        private JTextArea contentArea;

        // Control de animación
        private Timer animationTimer;
        private int currentHeight;
        private int targetHeight;

        public ExpandableNotificationPanel(NotificacionDestinatario notification,
                ExpandableNotificationListener listener, int userId) {
            
            if (notification == null) {
                throw new IllegalArgumentException("Notification no puede ser null");
            }
            if (userId <= 0) {
                throw new IllegalArgumentException("UserId debe ser mayor que 0: " + userId);
            }

            this.notification = notification;
            this.listener = listener;
            this.notificationService = NotificationService.getInstance();
            this.userId = userId;
            this.currentHeight = COLLAPSED_HEIGHT;

            try {
                setLayout(new BorderLayout());
                setMaximumSize(new Dimension(Integer.MAX_VALUE, COLLAPSED_HEIGHT));
                setPreferredSize(new Dimension(0, COLLAPSED_HEIGHT));

                initializeComponents();
                setupLayout();
                setupListeners();
                updateAppearance();

            } catch (Exception e) {
                System.err.println("❌ Error inicializando ExpandableNotificationPanel: " + e.getMessage());
                throw new RuntimeException("Error fatal en constructor", e);
            }
        }

        private void initializeComponents() {
            createHeaderPanel();
            createExpandedPanel();
            add(headerPanel, BorderLayout.NORTH);
        }

        private void createHeaderPanel() {
            headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));
            headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel leftPanel = createLeftPanel();
            JPanel rightPanel = createRightPanel();

            headerPanel.add(leftPanel, BorderLayout.CENTER);
            headerPanel.add(rightPanel, BorderLayout.EAST);
        }

        private JPanel createLeftPanel() {
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);

            // Indicador de estado
            statusIndicator = new JLabel("●");
            statusIndicator.setFont(new Font("Arial", Font.BOLD, 16));
            statusIndicator.setPreferredSize(new Dimension(20, 20));
            statusIndicator.setHorizontalAlignment(SwingConstants.CENTER);

            // Panel de contenido
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            // Título
            titleLabel = new JLabel(notification.getTitulo());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Info secundaria
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            infoPanel.setOpaque(false);
            infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            senderLabel = new JLabel("De: " + notification.getRemitenteCompleto());
            senderLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            senderLabel.setForeground(Color.GRAY);

            timeLabel = new JLabel(" • " + formatearFecha(notification.getFechaCreacion()));
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            timeLabel.setForeground(Color.GRAY);

            infoPanel.add(senderLabel);
            infoPanel.add(timeLabel);

            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(2));
            contentPanel.add(infoPanel);

            leftPanel.add(statusIndicator, BorderLayout.WEST);
            leftPanel.add(Box.createHorizontalStrut(8), BorderLayout.CENTER);
            leftPanel.add(contentPanel, BorderLayout.CENTER);

            return leftPanel;
        }

        private JPanel createRightPanel() {
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            rightPanel.setOpaque(false);

            // Indicador de prioridad
            priorityLabel = new JLabel(getPriorityIcon(notification.getPrioridad()));
            priorityLabel.setToolTipText("Prioridad: " + notification.getPrioridad());

            // Botón expandir
            expandButton = new JButton("▼");
            expandButton.setFont(new Font("Arial", Font.BOLD, 12));
            expandButton.setPreferredSize(new Dimension(30, 25));
            expandButton.setFocusPainted(false);
            expandButton.setBorderPainted(false);
            expandButton.setContentAreaFilled(false);

            rightPanel.add(priorityLabel);
            rightPanel.add(expandButton);

            return rightPanel;
        }

        private void createExpandedPanel() {
            expandedPanel = new JPanel(new BorderLayout());
            expandedPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
            expandedPanel.setVisible(false);

            // Contenido
            contentArea = new JTextArea(notification.getContenido());
            contentArea.setFont(new Font("Arial", Font.PLAIN, 13));
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setOpaque(false);

            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setPreferredSize(new Dimension(0, 60));

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);

            if (!notification.isLeida()) {
                markReadButton = new JButton("✓ Marcar como leído");
                markReadButton.setFont(new Font("Arial", Font.PLAIN, 11));
                markReadButton.setBackground(new Color(40, 167, 69));
                markReadButton.setForeground(Color.WHITE);
                markReadButton.setBorderPainted(false);
                markReadButton.setFocusPainted(false);
                buttonPanel.add(markReadButton);
            }

            actionsButton = new JButton("⚙ Acciones");
            actionsButton.setFont(new Font("Arial", Font.PLAIN, 11));
            actionsButton.setBackground(new Color(108, 117, 125));
            actionsButton.setForeground(Color.WHITE);
            actionsButton.setBorderPainted(false);
            actionsButton.setFocusPainted(false);
            buttonPanel.add(actionsButton);

            expandedPanel.add(scrollPane, BorderLayout.CENTER);
            expandedPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        private void setupLayout() {
            updateAppearance();
        }

        private void setupListeners() {
            // Click en header para expandir/contraer
            headerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!isAnimating) {
                        toggleExpanded();
                    }
                }
            });

            // Botón expandir
            expandButton.addActionListener(_ -> {
                if (!isAnimating) {
                    toggleExpanded();
                }
            });

            // Botón marcar como leído
            if (markReadButton != null) {
                markReadButton.addActionListener(_ -> markAsRead());
            }

            // Botón acciones
            actionsButton.addActionListener(_ -> showActionsMenu());
        }

        private void toggleExpanded() {
            if (isAnimating) return;

            isExpanded = !isExpanded;
            targetHeight = isExpanded ? EXPANDED_HEIGHT : COLLAPSED_HEIGHT;

            expandButton.setText(isExpanded ? "▲" : "▼");

            if (isExpanded && !notification.isLeida()) {
                markAsReadQuietly();
            }

            animateToHeight(targetHeight);

            if (listener != null) {
                listener.onNotificationToggled(this);
            }
        }

        private void animateToHeight(int newHeight) {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }

            isAnimating = true;
            targetHeight = newHeight;

            animationTimer = new Timer(ANIMATION_SPEED, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int diff = targetHeight - currentHeight;
                    
                    if (Math.abs(diff) <= ANIMATION_STEP) {
                        currentHeight = targetHeight;
                        animationTimer.stop();
                        isAnimating = false;
                        
                        // Mostrar/ocultar panel expandido
                        if (isExpanded) {
                            add(expandedPanel, BorderLayout.CENTER);
                            expandedPanel.setVisible(true);
                        } else {
                            expandedPanel.setVisible(false);
                            remove(expandedPanel);
                        }
                        
                    } else {
                        currentHeight += diff > 0 ? ANIMATION_STEP : -ANIMATION_STEP;
                    }

                    setPreferredSize(new Dimension(getWidth(), currentHeight));
                    setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
                    
                    revalidate();
                    repaint();
                }
            });

            // Mostrar panel expandido inmediatamente si se está expandiendo
            if (isExpanded) {
                add(expandedPanel, BorderLayout.CENTER);
                expandedPanel.setVisible(true);
            }

            animationTimer.start();
        }

        private void markAsRead() {
            if (isProcessingRead) return;
            
            isProcessingRead = true;
            
            CompletableFuture.runAsync(() -> {
                try {
                    notificationService.marcarComoLeida(notification.getId(), userId);
                    notification.setEstadoLectura("LEIDA");
                    notification.setFechaLeida(LocalDateTime.now());
                    
                    SwingUtilities.invokeLater(() -> {
                        updateAppearance();
                        if (markReadButton != null) {
                            markReadButton.setVisible(false);
                        }
                        
                        if (listener != null) {
                            listener.onNotificationRead(this);
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("Error marcando notificación como leída: " + e.getMessage());
                } finally {
                    isProcessingRead = false;
                }
            });
        }

        private void markAsUnread() {
            if (isProcessingRead) return;
            
            isProcessingRead = true;
            
            CompletableFuture.runAsync(() -> {
                try {
                    notificationService.marcarComoNoLeida(notification.getId(), userId);
                    notification.setEstadoLectura("NO_LEIDA");
                    notification.setFechaLeida(null);
                    
                    SwingUtilities.invokeLater(() -> {
                        updateAppearance();
                        
                        // Recrear botón de marcar como leído en el panel expandido
                        if (expandedPanel != null && isExpanded) {
                            recreateExpandedButtons();
                        }
                        
                        if (listener != null) {
                            listener.onNotificationRead(this);
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("Error marcando notificación como no leída: " + e.getMessage());
                } finally {
                    isProcessingRead = false;
                }
            });
        }

        private void recreateExpandedButtons() {
            // Buscar el panel de botones y actualizarlo
            Component[] components = expandedPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getLayout() instanceof FlowLayout) {
                        panel.removeAll();
                        
                        // Agregar botón de marcar como leído si no está leída
                        if (!notification.isLeida()) {
                            markReadButton = new JButton("✓ Marcar como leído");
                            markReadButton.setFont(new Font("Arial", Font.PLAIN, 11));
                            markReadButton.setBackground(new Color(40, 167, 69));
                            markReadButton.setForeground(Color.WHITE);
                            markReadButton.setBorderPainted(false);
                            markReadButton.setFocusPainted(false);
                            markReadButton.addActionListener(_ -> markAsRead());
                            panel.add(markReadButton);
                        }
                        
                        // Re-agregar botón de acciones
                        panel.add(actionsButton);
                        panel.revalidate();
                        panel.repaint();
                        break;
                    }
                }
            }
        }

        private void markAsReadQuietly() {
            if (!notification.isLeida() && !isProcessingRead) {
                markAsRead();
            }
        }

        private void showActionsMenu() {
            JPopupMenu menu = new JPopupMenu();
            
            // Opciones de lectura
            if (!notification.isLeida()) {
                JMenuItem markReadItem = new JMenuItem("✓ Marcar como leído");
                markReadItem.addActionListener(_ -> markAsRead());
                menu.add(markReadItem);
            } else {
                JMenuItem markUnreadItem = new JMenuItem("📧 Marcar como no leído");
                markUnreadItem.addActionListener(_ -> markAsUnread());
                menu.add(markUnreadItem);
            }
            
            menu.addSeparator();
            
            JMenuItem deleteItem = new JMenuItem("🗑 Eliminar");
            deleteItem.addActionListener(_ -> {
                int result = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea eliminar esta notificación?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION && listener != null) {
                    listener.onNotificationDeleted(this);
                }
            });
            menu.add(deleteItem);
            
            menu.show(actionsButton, 0, actionsButton.getHeight());
        }

        private void updateAppearance() {
            Color backgroundColor;
            Color borderColor;
            
            if (!notification.isLeida()) {
                backgroundColor = UNREAD_BACKGROUND;
                statusIndicator.setForeground(new Color(0, 123, 255));
            } else {
                backgroundColor = READ_BACKGROUND;
                statusIndicator.setForeground(Color.LIGHT_GRAY);
            }
            
            // Color por prioridad
            String prioridad = notification.getPrioridad();
            switch (prioridad.toLowerCase()) {
                case "urgente":
                    borderColor = URGENT_BORDER;
                    if (!notification.isLeida()) backgroundColor = URGENT_BACKGROUND;
                    break;
                case "alta":
                    borderColor = HIGH_BORDER;
                    if (!notification.isLeida()) backgroundColor = HIGH_BACKGROUND;
                    break;
                case "normal":
                    borderColor = NORMAL_BORDER;
                    if (!notification.isLeida()) backgroundColor = NORMAL_BACKGROUND;
                    break;
                default:
                    borderColor = LOW_BORDER;
                    break;
            }
            
            setBackground(backgroundColor);
            headerPanel.setBackground(backgroundColor);
            if (expandedPanel != null) {
                expandedPanel.setBackground(backgroundColor);
            }
            
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
        }

        private String getPriorityIcon(String prioridad) {
            switch (prioridad.toLowerCase()) {
                case "urgente": return "🔴";
                case "alta": return "🟡";
                case "normal": return "🔵";
                case "baja": return "⚪";
                default: return "⚫";
            }
        }

        private String formatearFecha(LocalDateTime fecha) {
            if (fecha == null) return "Sin fecha";
            
            LocalDateTime now = LocalDateTime.now();
            if (fecha.toLocalDate().equals(now.toLocalDate())) {
                return "Hoy " + fecha.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else if (fecha.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                return "Ayer " + fecha.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                return fecha.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            }
        }

        // Getters
        public NotificacionDestinatario getNotification() { return notification; }
        public boolean isExpanded() { return isExpanded; }
        public boolean isAnimating() { return isAnimating; }
    }

    // ========================================
    // PANEL ACADÉMICO PARA PROFESORES
    // ========================================
    
    public static class ProfesorAcademicNotificationsPanel extends JPanel {
        
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

        public ProfesorAcademicNotificationsPanel(int profesorId) {
            this.profesorId = profesorId;
            this.connection = Conexion.getInstancia().verificarConexion();
            this.notificationUtil = NotificationIntegrationUtil.getInstance();
            this.materiasMap = new HashMap<>();

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

        private JPanel createNotasRapidasPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Panel principal con formulario
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new TitledBorder("📊 Notificación Rápida de Notas"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;

            // Materia
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Materia:"), gbc);
            comboMaterias = new JComboBox<>();
            comboMaterias.setPreferredSize(new Dimension(200, 25));
            gbc.gridx = 1;
            formPanel.add(comboMaterias, gbc);

            // ID del Alumno
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("ID del Alumno:"), gbc);
            txtAlumnoId = new JTextField(15);
            gbc.gridx = 1;
            formPanel.add(txtAlumnoId, gbc);

            // Tipo de Trabajo
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Tipo de Trabajo:"), gbc);
            txtTipoTrabajo = new JTextField(15);
            gbc.gridx = 1;
            formPanel.add(txtTipoTrabajo, gbc);

            // Nota
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Nota:"), gbc);
            txtNota = new JTextField(15);
            gbc.gridx = 1;
            formPanel.add(txtNota, gbc);

            // Bimestre
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Bimestre:"), gbc);
            comboBimestre = new JComboBox<>(new String[]{"1er Bimestre", "2do Bimestre", "3er Bimestre", "4to Bimestre"});
            gbc.gridx = 1;
            formPanel.add(comboBimestre, gbc);

            // Observaciones
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Observaciones:"), gbc);
            txtObservaciones = new JTextArea(3, 15);
            txtObservaciones.setLineWrap(true);
            JScrollPane scrollObs = new JScrollPane(txtObservaciones);
            gbc.gridx = 1;
            formPanel.add(scrollObs, gbc);

            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnNotificar = new JButton("📤 Notificar Nota");
            btnNotificar.setBackground(new Color(0, 123, 255));
            btnNotificar.setForeground(Color.WHITE);
            btnNotificar.addActionListener(_ -> notificarNota());

            JButton btnLimpiar = new JButton("🧹 Limpiar");
            btnLimpiar.addActionListener(_ -> limpiarFormulario());

            buttonPanel.add(btnNotificar);
            buttonPanel.add(btnLimpiar);

            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
            formPanel.add(buttonPanel, gbc);

            panel.add(formPanel, BorderLayout.CENTER);

            // Panel de ayuda
            JPanel helpPanel = createHelpPanel();
            panel.add(helpPanel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createComunicacionMasivaPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Formulario de comunicación masiva
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new TitledBorder("📢 Envío Masivo de Comunicaciones"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;

            // Destinatarios
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Destinatarios:"), gbc);
            comboDestinatarios = new JComboBox<>(new String[]{
                "Todos mis alumnos", "Alumnos de materia específica", "Curso específico"
            });
            gbc.gridx = 1;
            formPanel.add(comboDestinatarios, gbc);

            // Tipo de mensaje
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Tipo de Mensaje:"), gbc);
            comboTipoMensaje = new JComboBox<>(new String[]{
                "Recordatorio de examen", "Aviso de evaluación", "Información general", "Mensaje urgente"
            });
            gbc.gridx = 1;
            formPanel.add(comboTipoMensaje, gbc);

            // Título
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Título:"), gbc);
            txtTituloMensaje = new JTextField(20);
            gbc.gridx = 1;
            formPanel.add(txtTituloMensaje, gbc);

            // Contenido
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Contenido:"), gbc);
            txtContenidoMensaje = new JTextArea(5, 20);
            txtContenidoMensaje.setLineWrap(true);
            JScrollPane scrollContent = new JScrollPane(txtContenidoMensaje);
            gbc.gridx = 1;
            formPanel.add(scrollContent, gbc);

            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnEnviar = new JButton("📤 Enviar Comunicación");
            btnEnviar.setBackground(new Color(40, 167, 69));
            btnEnviar.setForeground(Color.WHITE);
            btnEnviar.addActionListener(_ -> enviarComunicacionMasiva());

            JButton btnPrevisualizar = new JButton("👁 Previsualizar");
            btnPrevisualizar.addActionListener(_ -> previsualizarMensaje());

            buttonPanel.add(btnPrevisualizar);
            buttonPanel.add(btnEnviar);

            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            formPanel.add(buttonPanel, gbc);

            panel.add(formPanel, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createSeguimientoAcademicoPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Panel superior con tablas
            JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));

            // Tabla de trabajos pendientes
            modeloTrabajosPendientes = new DefaultTableModel(
                new String[]{"Alumno", "Materia", "Trabajo", "Fecha Límite", "Estado"}, 0);
            tablaTrabajosPendientes = new JTable(modeloTrabajosPendientes);
            JScrollPane scrollTrabajos = new JScrollPane(tablaTrabajosPendientes);
            scrollTrabajos.setBorder(new TitledBorder("📝 Trabajos Pendientes de Evaluación"));
            tablesPanel.add(scrollTrabajos);

            // Tabla de notificaciones enviadas
            modeloNotificacionesEnviadas = new DefaultTableModel(
                new String[]{"Fecha", "Destinatario", "Tipo", "Título", "Estado"}, 0);
            tablaNotificacionesEnviadas = new JTable(modeloNotificacionesEnviadas);
            JScrollPane scrollNotificaciones = new JScrollPane(tablaNotificacionesEnviadas);
            scrollNotificaciones.setBorder(new TitledBorder("📤 Historial de Notificaciones Enviadas"));
            tablesPanel.add(scrollNotificaciones);

            panel.add(tablesPanel, BorderLayout.CENTER);

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnActualizar = new JButton("🔄 Actualizar");
            btnActualizar.addActionListener(_ -> actualizarSeguimiento());

            JButton btnExportar = new JButton("📊 Exportar Reporte");
            btnExportar.addActionListener(_ -> exportarReporte());

            buttonPanel.add(btnActualizar);
            buttonPanel.add(btnExportar);

            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createEstadisticasProfesorPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Panel de estadísticas
            JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));

            // Estadística 1: Notificaciones enviadas
            JPanel notifPanel = createStatPanel("📤 Notificaciones Enviadas", "0", "Este mes");
            statsPanel.add(notifPanel);

            // Estadística 2: Notas registradas
            JPanel notasPanel = createStatPanel("📊 Notas Registradas", "0", "Este bimestre");
            statsPanel.add(notasPanel);

            // Estadística 3: Comunicaciones masivas
            JPanel comPanel = createStatPanel("📢 Comunicaciones Masivas", "0", "Este mes");
            statsPanel.add(comPanel);

            // Estadística 4: Alumnos notificados
            JPanel alumnosPanel = createStatPanel("👥 Alumnos Notificados", "0", "Únicos este mes");
            statsPanel.add(alumnosPanel);

            panel.add(statsPanel, BorderLayout.CENTER);

            // Gráfico simple (simulado)
            JPanel chartPanel = new JPanel();
            chartPanel.setBorder(new TitledBorder("📈 Actividad de Notificaciones (Últimos 7 días)"));
            chartPanel.setPreferredSize(new Dimension(0, 150));
            chartPanel.setBackground(Color.WHITE);
            panel.add(chartPanel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createStatPanel(String title, String value, String subtitle) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(15, 15, 15, 15)
            ));
            panel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            valueLabel.setForeground(new Color(0, 123, 255));

            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            subtitleLabel.setForeground(Color.GRAY);

            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(valueLabel, BorderLayout.CENTER);
            panel.add(subtitleLabel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createHelpPanel() {
            JPanel helpPanel = new JPanel(new BorderLayout());
            helpPanel.setBorder(new TitledBorder("ℹ️ Información de Uso"));
            helpPanel.setPreferredSize(new Dimension(0, 120));

            JTextArea helpText = new JTextArea(
                "• Complete todos los campos requeridos antes de enviar\n" +
                "• Las notificaciones se envían inmediatamente al alumno\n" +
                "• Se incluyen detalles de la materia y tipo de evaluación\n" +
                "• Las notas bajas incluyen mensajes de apoyo automáticamente\n\n" +
                "⚡ FUNCIONES RÁPIDAS:\n" +
                "• 'Registrar y Notificar': Guarda en BD y envía notificación\n" +
                "• 'Solo Notificar': Envía notificación sin guardar en BD\n" +
                "• Usar para comunicar notas ya cargadas en otro sistema"
            );
            helpText.setFont(new Font("Arial", Font.PLAIN, 11));
            helpText.setEditable(false);
            helpText.setOpaque(false);

            helpPanel.add(helpText, BorderLayout.CENTER);
            return helpPanel;
        }

        private void setupLayout() {
            add(tabbedPane, BorderLayout.CENTER);
        }

        private void setupListeners() {
            // Los listeners se configuran en cada método create
        }

        private void loadInitialData() {
            // Cargar materias del profesor
            cargarMaterias();
            // Cargar estudiantes
            cargarEstudiantes();
            // Actualizar seguimiento
            actualizarSeguimiento();
        }

        private void cargarMaterias() {
            try {
                String sql = "SELECT DISTINCT m.id, m.nombre FROM materias m " +
                           "JOIN profesor_materia pm ON m.id = pm.materia_id " +
                           "WHERE pm.profesor_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, profesorId);
                ResultSet rs = stmt.executeQuery();

                comboMaterias.removeAllItems();
                materiasMap.clear();

                while (rs.next()) {
                    String materia = rs.getString("nombre");
                    comboMaterias.addItem(materia);
                    materiasMap.put(materia, new MateriaInfo(rs.getInt("id"), materia));
                }

            } catch (SQLException e) {
                System.err.println("Error cargando materias: " + e.getMessage());
            }
        }

        private void cargarEstudiantes() {
            // Implementar carga de estudiantes
        }

        private void notificarNota() {
            try {
                // Validaciones
                if (comboMaterias.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this, "Seleccione una materia");
                    return;
                }

                String alumnoIdStr = txtAlumnoId.getText().trim();
                if (alumnoIdStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingrese el ID del alumno");
                    return;
                }

                String tipoTrabajo = txtTipoTrabajo.getText().trim();
                String notaStr = txtNota.getText().trim();

                if (tipoTrabajo.isEmpty() || notaStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Complete todos los campos");
                    return;
                }

                int alumnoId = Integer.parseInt(alumnoIdStr);
                double nota = Double.parseDouble(notaStr);
                String materia = (String) comboMaterias.getSelectedItem();

                // Enviar notificación
                notificationUtil.notificarNuevaNota(
                    alumnoId, materia, tipoTrabajo, nota, profesorId
                );

                JOptionPane.showMessageDialog(this, 
                    "✅ Notificación enviada correctamente al alumno " + alumnoId);

                limpiarFormulario();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID del alumno y nota deben ser números válidos");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error enviando notificación: " + e.getMessage());
            }
        }

        private void limpiarFormulario() {
            txtAlumnoId.setText("");
            txtTipoTrabajo.setText("");
            txtNota.setText("");
            txtObservaciones.setText("");
            comboBimestre.setSelectedIndex(0);
        }

        private void enviarComunicacionMasiva() {
            // Implementar envío masivo
            JOptionPane.showMessageDialog(this, "Función de comunicación masiva en desarrollo");
        }

        private void previsualizarMensaje() {
            String titulo = txtTituloMensaje.getText();
            String contenido = txtContenidoMensaje.getText();
            
            JOptionPane.showMessageDialog(this, 
                "TÍTULO: " + titulo + "\n\nCONTENIDO:\n" + contenido,
                "Previsualización del Mensaje",
                JOptionPane.INFORMATION_MESSAGE);
        }

        private void actualizarSeguimiento() {
            // Implementar actualización de seguimiento
        }

        private void exportarReporte() {
            // Implementar exportación
            JOptionPane.showMessageDialog(this, "Función de exportación en desarrollo");
        }

        // Clase auxiliar para materias
        private static class MateriaInfo {
            // Fields stored for future functionality
            @SuppressWarnings("unused")
            final int id;
            @SuppressWarnings("unused")
            final String nombre;

            MateriaInfo(int id, String nombre) {
                this.id = id;
                this.nombre = nombre;
            }
        }
    }

    // ========================================
    // PANELES PARA OTROS ROLES (STUB)
    // ========================================
    
    public static class AdminNotificationPanel extends JPanel {
        public AdminNotificationPanel(int adminId) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel label = new JLabel("Panel de Notificaciones de Administrador");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            add(label, BorderLayout.CENTER);
            
            System.out.println("✅ AdminNotificationPanel inicializado para usuario: " + adminId);
        }
    }

    public static class AlumnoNotificationPanel extends JPanel {
        public AlumnoNotificationPanel(int alumnoId) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel label = new JLabel("Panel de Notificaciones de Alumno");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            add(label, BorderLayout.CENTER);
            
            System.out.println("✅ AlumnoNotificationPanel inicializado para usuario: " + alumnoId);
        }
    }

    public static class PreceptorNotificationPanel extends JPanel {
        public PreceptorNotificationPanel(int preceptorId) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel label = new JLabel("Panel de Notificaciones de Preceptor");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            add(label, BorderLayout.CENTER);
            
            System.out.println("✅ PreceptorNotificationPanel inicializado para usuario: " + preceptorId);
        }
    }

    public static class AttpNotificationPanel extends JPanel {
        public AttpNotificationPanel(int attpId) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel label = new JLabel("Panel de Notificaciones de ATTP");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            add(label, BorderLayout.CENTER);
            
            System.out.println("✅ AttpNotificationPanel inicializado para usuario: " + attpId);
        }
    }
}
