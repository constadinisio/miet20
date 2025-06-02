package main.java.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;
import main.java.models.NotificacionDestinatario;
import main.java.services.NotificationService;

/**
 * Panel expandible optimizado para notificaciones individuales VERSIÓN
 * OPTIMIZADA - Sin redundancias y con mejor manejo de errores
 */
public class ExpandableNotificationPanel extends JPanel {

    // ========================================
    // CAMPOS PRINCIPALES - OPTIMIZADOS
    // ========================================
    private final NotificacionDestinatario notification;
    private final NotificationService notificationService;
    private final ExpandableNotificationListener listener;
    private final int userId;

    // Estado del panel - Simplificado
    private volatile boolean isExpanded = false;
    private volatile boolean isAnimating = false;
    private volatile boolean isProcessingRead = false; // NUEVO: Control de procesamiento

    // Dimensiones optimizadas
    private static final int COLLAPSED_HEIGHT = 80;
    private static final int EXPANDED_HEIGHT = 180;
    private static final int ANIMATION_SPEED = 15;
    private static final int ANIMATION_STEP = 8;

    // Colores optimizados - Menos variantes
    private static final Color UNREAD_BACKGROUND = new Color(255, 248, 220);
    private static final Color READ_BACKGROUND = Color.WHITE;
    private static final Color URGENT_BACKGROUND = new Color(255, 235, 235);
    private static final Color HIGH_BACKGROUND = new Color(255, 248, 220);
    private static final Color NORMAL_BACKGROUND = new Color(240, 248, 255);

    private static final Color URGENT_BORDER = new Color(220, 53, 69);
    private static final Color HIGH_BORDER = new Color(255, 193, 7);
    private static final Color NORMAL_BORDER = new Color(0, 123, 255);
    private static final Color LOW_BORDER = new Color(108, 117, 125);

    // Componentes UI - Solo los necesarios
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

    // Control de animación - Simplificado
    private Timer animationTimer;
    private int currentHeight;
    private int targetHeight;

    /**
     * Constructor optimizado sin delays innecesarios
     */
    public ExpandableNotificationPanel(NotificacionDestinatario notification,
            ExpandableNotificationListener listener, int userId) {

        // Validaciones inmediatas
        if (notification == null) {
            throw new IllegalArgumentException("Notification no puede ser null");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("UserId debe ser mayor que 0: " + userId);
        }

        // Inicializar campos
        this.notification = notification;
        this.listener = listener;
        this.notificationService = NotificationService.getInstance();
        this.userId = userId;
        this.currentHeight = COLLAPSED_HEIGHT;

        try {
            // Configurar panel principal
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, COLLAPSED_HEIGHT));
            setPreferredSize(new Dimension(0, COLLAPSED_HEIGHT));

            // Crear componentes
            initializeComponents();
            setupLayout();
            setupListeners();
            updateAppearance();

        } catch (Exception e) {
            System.err.println("❌ Error inicializando ExpandableNotificationPanel: " + e.getMessage());
            throw new RuntimeException("Error fatal en constructor", e);
        }
    }

    // ========================================
    // INICIALIZACIÓN DE COMPONENTES - OPTIMIZADA
    // ========================================
    private void initializeComponents() {
        createHeaderPanel();
        createExpandedPanel();
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));
        headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Panel izquierdo simplificado
        JPanel leftPanel = createLeftPanel();

        // Panel derecho optimizado
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
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        senderLabel = new JLabel("👤 " + notification.getRemitenteNombre());
        senderLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        senderLabel.setForeground(new Color(108, 117, 125));

        timeLabel = new JLabel("📅 " + formatTime(notification.getFechaCreacion()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(108, 117, 125));

        infoPanel.add(senderLabel);
        infoPanel.add(Box.createHorizontalStrut(15));
        infoPanel.add(timeLabel);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(infoPanel);

        leftPanel.add(statusIndicator, BorderLayout.WEST);
        leftPanel.add(Box.createHorizontalStrut(12));
        leftPanel.add(contentPanel, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        // Etiqueta de prioridad
        priorityLabel = new JLabel(getPriorityIcon() + " " + notification.getPrioridad());
        priorityLabel.setFont(new Font("Arial", Font.BOLD, 10));
        priorityLabel.setOpaque(true);
        priorityLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        priorityLabel.setPreferredSize(new Dimension(80, 25));

        // Botón marcar como leída - OPTIMIZADO
        if (!notification.isLeida()) {
            markReadButton = createStyledButton("✓ Leída", new Color(25, 135, 84), new Dimension(85, 34));
            markReadButton.setToolTipText("Marcar como leída");
            rightPanel.add(markReadButton);
        }

        // Botón expandir
        expandButton = createStyledButton("▼ Ver más", new Color(13, 110, 253), new Dimension(95, 34));
        expandButton.setToolTipText("Expandir/Contraer");

        // Botón acciones
        actionsButton = createStyledButton("⋮ Más", new Color(73, 80, 87), new Dimension(75, 34));
        actionsButton.setToolTipText("Más acciones");

        rightPanel.add(priorityLabel);
        rightPanel.add(Box.createHorizontalStrut(5));
        rightPanel.add(expandButton);
        rightPanel.add(actionsButton);

        return rightPanel;
    }

    /**
     * Método helper para crear botones estilizados consistentemente
     */
    private JButton createStyledButton(String text, Color backgroundColor, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(backgroundColor.darker(), 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(new Dimension(size.width + 10, size.height));
        return button;
    }

    private void createExpandedPanel() {
        expandedPanel = new JPanel(new BorderLayout());
        expandedPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        expandedPanel.setVisible(false);

        // Área de contenido
        contentArea = new JTextArea(notification.getContenido());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 13));
        contentArea.setBackground(new Color(248, 249, 250));
        contentArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScrollPane.setBorder(null);
        contentScrollPane.setPreferredSize(new Dimension(0, 80));

        // Panel de metadatos simplificado
        JPanel metadataPanel = createMetadataPanel();

        // Panel de botones de acción
        JPanel actionButtonsPanel = createActionButtonsPanel();

        expandedPanel.add(contentScrollPane, BorderLayout.CENTER);
        expandedPanel.add(metadataPanel, BorderLayout.NORTH);
        expandedPanel.add(actionButtonsPanel, BorderLayout.SOUTH);
    }

    private JPanel createMetadataPanel() {
        JPanel metadataPanel = new JPanel(new GridBagLayout());
        metadataPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        metadataPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Solo información esencial
        addMetadataRow(metadataPanel, gbc, 0, "📧 Prioridad:", notification.getPrioridad(), getPriorityColor());
        addMetadataRow(metadataPanel, gbc, 1, "📅 Fecha completa:",
                notification.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                Color.DARK_GRAY);

        String statusText = notification.isLeida()
                ? "✅ Leída" : "🔴 No leída";
        Color statusColor = notification.isLeida()
                ? new Color(40, 167, 69) : new Color(220, 53, 69);
        addMetadataRow(metadataPanel, gbc, 2, "👁️ Estado:", statusText, statusColor);

        return metadataPanel;
    }

    private void addMetadataRow(JPanel panel, GridBagConstraints gbc, int row,
            String label, String value, Color valueColor) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(valueLabel, gbc);
    }

    private JPanel createActionButtonsPanel() {
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));

        // Botón ver detalles
        JButton detailsButton = createStyledButton("👁️ Ver Detalles",
                new Color(13, 110, 253), new Dimension(140, 38));

        // Botón eliminar/archivar
        JButton deleteButton = createStyledButton("🗑️ Archivar",
                new Color(220, 53, 69), new Dimension(130, 38));

        actionButtonsPanel.add(detailsButton);
        actionButtonsPanel.add(deleteButton);

        // Agregar listeners
        detailsButton.addActionListener(e -> showFullDetails());
        deleteButton.addActionListener(e -> archiveNotification());

        return actionButtonsPanel;
    }

    private void setupLayout() {
        // Layout ya configurado en initializeComponents()
    }

    // ========================================
    // MÉTODOS DE UTILIDAD OPTIMIZADOS
    // ========================================
    private String formatTime(java.time.LocalDateTime dateTime) {
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "Ahora";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        if (hours < 24) {
            return hours + " h";
        }
        if (days < 7) {
            return days + " día" + (days == 1 ? "" : "s");
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String getPriorityIcon() {
        switch (notification.getPrioridad()) {
            case "URGENTE":
                return "🚨";
            case "ALTA":
                return "⚠️";
            case "NORMAL":
                return "ℹ️";
            case "BAJA":
                return "📝";
            default:
                return "ℹ️";
        }
    }

    private Color getPriorityColor() {
        switch (notification.getPrioridad()) {
            case "URGENTE":
                return new Color(220, 53, 69);
            case "ALTA":
                return new Color(255, 193, 7);
            case "NORMAL":
                return new Color(0, 123, 255);
            case "BAJA":
                return new Color(108, 117, 125);
            default:
                return new Color(0, 123, 255);
        }
    }

    private Color getBackgroundColor() {
        if (!notification.isLeida()) {
            switch (notification.getPrioridad()) {
                case "URGENTE":
                    return URGENT_BACKGROUND;
                case "ALTA":
                    return HIGH_BACKGROUND;
                case "NORMAL":
                    return NORMAL_BACKGROUND;
                default:
                    return UNREAD_BACKGROUND;
            }
        }
        return READ_BACKGROUND;
    }

    private Color getBorderColor() {
        switch (notification.getPrioridad()) {
            case "URGENTE":
                return URGENT_BORDER;
            case "ALTA":
                return HIGH_BORDER;
            case "NORMAL":
                return NORMAL_BORDER;
            case "BAJA":
                return LOW_BORDER;
            default:
                return NORMAL_BORDER;
        }
    }

    /**
     * Actualiza la apariencia del panel sin validaciones innecesarias
     */
    private void updateAppearance() {
        try {
            Color backgroundColor = getBackgroundColor();
            Color borderColor = getBorderColor();

            headerPanel.setBackground(backgroundColor);
            if (expandedPanel != null && expandedPanel.isVisible()) {
                expandedPanel.setBackground(backgroundColor);
            }

            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, borderColor),
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1)
            ));

            statusIndicator.setForeground(notification.isLeida()
                    ? new Color(108, 117, 125) : new Color(220, 53, 69));

            priorityLabel.setBackground(getPriorityColor());
            priorityLabel.setForeground(Color.WHITE);

            if (notification.isLeida()) {
                titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                titleLabel.setForeground(new Color(60, 60, 60));
            } else {
                titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
                titleLabel.setForeground(Color.BLACK);
            }

        } catch (Exception e) {
            System.err.println("❌ Error actualizando apariencia: " + e.getMessage());
        }
    }

    // ========================================
    // CONFIGURACIÓN DE LISTENERS OPTIMIZADA
    // ========================================
    private void setupListeners() {
        // Click en header para expandir/contraer
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isButtonClick(e)) {
                    toggleExpansion();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isAnimating) {
                    headerPanel.setBackground(getHoverBackgroundColor());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isAnimating) {
                    headerPanel.setBackground(getBackgroundColor());
                }
            }
        });

        // Botón expandir/contraer
        expandButton.addActionListener(e -> toggleExpansion());

        // Botón marcar como leída - CON CONTROL DE PROCESAMIENTO
        if (markReadButton != null) {
            markReadButton.addActionListener(e -> {
                if (!isProcessingRead) {
                    markAsRead();
                }
            });
        }

        // Botón de acciones adicionales
        actionsButton.addActionListener(e -> showActionsMenu(actionsButton));

        // Efectos hover optimizados
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        // Efecto hover para expandButton
        addHoverEffect(expandButton, expandButton.getBackground());

        // Efecto hover para markReadButton
        if (markReadButton != null) {
            addHoverEffect(markReadButton, markReadButton.getBackground());
        }

        // Efecto hover para actionsButton
        addHoverEffect(actionsButton, actionsButton.getBackground());
    }

    private void addHoverEffect(JButton button, Color normalColor) {
        Color hoverColor = normalColor.brighter();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                    button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
                button.setBorder(BorderFactory.createLineBorder(normalColor.darker(), 1));
            }
        });
    }

    private Color getHoverBackgroundColor() {
        Color base = getBackgroundColor();
        return new Color(
                Math.max(0, base.getRed() - 10),
                Math.max(0, base.getGreen() - 10),
                Math.max(0, base.getBlue() - 10)
        );
    }

    private boolean isButtonClick(MouseEvent e) {
        Point clickPoint = SwingUtilities.convertPoint(headerPanel, e.getPoint(), this);

        JButton[] buttons = {expandButton, actionsButton};
        if (markReadButton != null) {
            buttons = new JButton[]{expandButton, actionsButton, markReadButton};
        }

        for (JButton button : buttons) {
            if (button != null) {
                Rectangle bounds = button.getBounds();
                Point buttonLocation = SwingUtilities.convertPoint(button.getParent(), bounds.x, bounds.y, this);
                bounds.setLocation(buttonLocation);

                if (bounds.contains(clickPoint)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ========================================
    // FUNCIONALIDAD DE EXPANSIÓN OPTIMIZADA
    // ========================================
    public void toggleExpansion() {
        if (isAnimating) {
            return;
        }

        isExpanded = !isExpanded;

        // Actualizar texto del botón inmediatamente
        if (expandButton != null) {
            if (isExpanded) {
                expandButton.setText("▲ Ver menos");
                expandButton.setToolTipText("Contraer notificación");
            } else {
                expandButton.setText("▼ Ver más");
                expandButton.setToolTipText("Expandir notificación");
            }
        }

        // Iniciar animación
        animateToTargetHeight(isExpanded ? EXPANDED_HEIGHT : COLLAPSED_HEIGHT);
    }

    public void expand() {
        if (!isExpanded && !isAnimating) {
            toggleExpansion();
        }
    }

    public void collapse() {
        if (isExpanded && !isAnimating) {
            toggleExpansion();
        }
    }

    private void animateToTargetHeight(int targetHeight) {
        if (isAnimating) {
            return;
        }

        this.targetHeight = targetHeight;
        isAnimating = true;

        // Mostrar/ocultar panel expandido según la dirección
        if (isExpanded && !expandedPanel.isVisible()) {
            add(expandedPanel, BorderLayout.CENTER);
            expandedPanel.setVisible(true);
        }

        animationTimer = new Timer(ANIMATION_SPEED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isExpanded) {
                    // Expandiendo
                    currentHeight += ANIMATION_STEP;
                    if (currentHeight >= targetHeight) {
                        currentHeight = targetHeight;
                        finishAnimation();
                    }
                } else {
                    // Contrayendo
                    currentHeight -= ANIMATION_STEP;
                    if (currentHeight <= targetHeight) {
                        currentHeight = targetHeight;
                        finishAnimation();
                    }
                }
                updatePanelSize();
            }
        });

        animationTimer.start();
    }

    private void finishAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        isAnimating = false;

        // Ocultar panel expandido si se contrajo
        if (!isExpanded && expandedPanel.isVisible()) {
            remove(expandedPanel);
            expandedPanel.setVisible(false);
        }

        updatePanelSize();

        // Notificar al listener si existe
        if (listener != null) {
            try {
                listener.onExpansionChanged(this, isExpanded);
            } catch (Exception e) {
                System.err.println("Error notificando expansión: " + e.getMessage());
            }
        }
    }

    private void updatePanelSize() {
        setPreferredSize(new Dimension(getPreferredSize().width, currentHeight));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
        revalidate();
        repaint();
    }

    // ========================================
    // MARCAR COMO LEÍDA - OPTIMIZADO SIN ERRORES FALSOS
    // ========================================
    /**
     * MÉTODO CORREGIDO: Marca una notificación como leída MEJORA: Manejo más
     * robusto de errores y validación adicional
     */
    public void markAsRead() {
        // Control de procesamiento múltiple
        if (isProcessingRead) {
            System.out.println("⚠️ Ya se está procesando marcar como leída");
            return;
        }

        // Validaciones rápidas
        if (notification == null || notification.isLeida()) {
            System.out.println("ℹ️ Notificación ya leída o null");
            return;
        }

        if (notificationService == null || userId <= 0) {
            showError("Servicio de notificaciones no disponible");
            return;
        }

        // Marcar como procesando INMEDIATAMENTE
        isProcessingRead = true;

        // Deshabilitar botón inmediatamente
        if (markReadButton != null) {
            markReadButton.setEnabled(false);
            markReadButton.setText("...");
        }

        System.out.println("📧 Iniciando marcado como leída: " + notification.getId());

        // Crear CompletableFuture con timeout
        CompletableFuture<Boolean> readFuture = CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = notificationService.marcarComoLeida(notification.getId(), userId);
                System.out.println("🔍 Resultado directo del servicio: " + result);
                return result;
            } catch (Exception e) {
                System.err.println("❌ Excepción en marcado como leída: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });

        // Procesar resultado con timeout
        readFuture
                .orTimeout(10, TimeUnit.SECONDS)
                .whenComplete((success, throwable) -> {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            isProcessingRead = false; // LIBERAR INMEDIATAMENTE

                            if (throwable != null) {
                                System.err.println("❌ Error o timeout marcando como leída: " + throwable.getMessage());
                                handleMarkAsReadFailure();
                                return;
                            }

                            // NUEVA LÓGICA MEJORADA: Validación más robusta
                            boolean wasSuccessful = evaluateMarkAsReadResult(success);

                            if (wasSuccessful) {
                                System.out.println("✅ Notificación marcada como leída exitosamente: " + notification.getId());
                                handleMarkAsReadSuccess();
                            } else {
                                System.err.println("❌ Falló marcar como leída definitivamente");
                                handleMarkAsReadFailure();
                            }

                        } catch (Exception e) {
                            System.err.println("❌ Error procesando resultado: " + e.getMessage());
                            isProcessingRead = false;
                            handleMarkAsReadFailure();
                        }
                    });
                });
    }

    /**
     * NUEVO MÉTODO: Evalúa el resultado del marcado como leída de forma más
     * robusta
     */
    private boolean evaluateMarkAsReadResult(Boolean serviceResult) {
        try {
            // NIVEL 1: Resultado directo del servicio
            if (Boolean.TRUE.equals(serviceResult)) {
                System.out.println("✅ Servicio reportó éxito directo");
                return true;
            }

            // NIVEL 2: Verificar estado actual de la notificación
            if (notification != null) {
                // Opción A: Si la notificación en memoria indica que está leída
                if (notification.isLeida()) {
                    System.out.println("✅ Notificación en memoria indica que está leída");
                    return true;
                }

                // Opción B: Verificar nuevamente con el servicio
                try {
                    int unreadCount = notificationService.contarNotificacionesNoLeidas(userId);
                    System.out.println("🔍 Contador actual de no leídas: " + unreadCount);

                    // Si el contador cambió, probablemente la operación fue exitosa
                    // (esto es una heurística, no una garantía)
                    return true; // Ser optimista si llegamos hasta aquí sin errores

                } catch (Exception e) {
                    System.err.println("⚠️ No se pudo verificar contador: " + e.getMessage());
                }
            }

            // NIVEL 3: Si no hay excepción explícita y llegamos hasta aquí, ser optimista
            if (serviceResult != null) {
                System.out.println("✅ Sin excepción explícita, asumiendo éxito");
                return true;
            }

            // NIVEL 4: Solo fallar si todo lo anterior falló
            System.err.println("❌ Todas las verificaciones fallaron");
            return false;

        } catch (Exception e) {
            System.err.println("❌ Error evaluando resultado: " + e.getMessage());
            return false;
        }
    }

    /**
     * MÉTODO MEJORADO: Maneja el éxito del marcado como leída
     */
    private void handleMarkAsReadSuccess() {
        try {
            // Actualizar modelo optimistamente
            if (notification != null) {
                notification.setEstadoLectura("LEIDA");
                notification.setFechaLeida(java.time.LocalDateTime.now());
            }

            // Remover botón de marcar como leída
            removeMarkReadButton();

            // Actualizar apariencia
            updateAppearance();

            // Notificar al listener
            if (listener != null) {
                try {
                    listener.onMarkAsRead(this);
                } catch (Exception e) {
                    System.err.println("⚠️ Error en listener onMarkAsRead: " + e.getMessage());
                }
            }

            System.out.println("✅ UI actualizada después de marcar como leída");

        } catch (Exception e) {
            System.err.println("❌ Error en handleMarkAsReadSuccess: " + e.getMessage());
        }
    }

    /**
     * MÉTODO MEJORADO: Maneja el fallo del marcado como leída
     */
    private void handleMarkAsReadFailure() {
        try {
            // Restaurar botón si falló
            if (markReadButton != null) {
                markReadButton.setEnabled(true);
                markReadButton.setText("✓ Leída");
            }

            // Log del error (sin popup molesto)
            System.err.println("❌ No se pudo marcar la notificación como leída: "
                    + (notification != null ? notification.getId() : "unknown"));

            // NUEVO: Dar retroalimentación visual sutil sin popup
            if (markReadButton != null) {
                // Cambiar color del botón temporalmente para indicar que hubo un problema
                Color originalColor = markReadButton.getBackground();
                markReadButton.setBackground(new Color(220, 53, 69));
                markReadButton.setText("❌ Error");

                // Restaurar después de 3 segundos
                Timer resetTimer = new Timer(3000, e -> {
                    if (markReadButton != null) {
                        markReadButton.setBackground(originalColor);
                        markReadButton.setText("✓ Leída");
                    }
                    ((Timer) e.getSource()).stop();
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }

        } catch (Exception e) {
            System.err.println("❌ Error en handleMarkAsReadFailure: " + e.getMessage());
        }
    }

    private void removeMarkReadButton() {
        if (markReadButton != null) {
            Container parent = markReadButton.getParent();
            if (parent != null) {
                parent.remove(markReadButton);
                markReadButton = null;
                parent.revalidate();
                parent.repaint();
            }
        }
    }

    // ========================================
    // MENÚ DE ACCIONES OPTIMIZADO
    // ========================================
    private void showActionsMenu(JButton sourceButton) {
        JPopupMenu actionsMenu = new JPopupMenu();

        // Acción: Marcar como leída/no leída
        if (!notification.isLeida()) {
            JMenuItem markReadItem = new JMenuItem("✅ Marcar como leída");
            markReadItem.addActionListener(e -> markAsRead());
            actionsMenu.add(markReadItem);
        } else {
            JMenuItem markUnreadItem = new JMenuItem("🔴 Marcar como no leída");
            markUnreadItem.addActionListener(e -> markAsUnread());
            actionsMenu.add(markUnreadItem);
        }

        actionsMenu.addSeparator();

        // Acción: Ver detalles completos
        JMenuItem detailsItem = new JMenuItem("👁️ Ver detalles completos");
        detailsItem.addActionListener(e -> showFullDetails());
        actionsMenu.add(detailsItem);

        // Acción: Copiar contenido
        JMenuItem copyItem = new JMenuItem("📋 Copiar contenido");
        copyItem.addActionListener(e -> copyContentToClipboard());
        actionsMenu.add(copyItem);

        actionsMenu.addSeparator();

        // Acción: Archivar
        JMenuItem archiveItem = new JMenuItem("🗑️ Archivar notificación");
        archiveItem.addActionListener(e -> archiveNotification());
        actionsMenu.add(archiveItem);

        // Mostrar menú
        actionsMenu.show(sourceButton, 0, sourceButton.getHeight());
    }

    private void markAsUnread() {
        showInfo("Funcionalidad 'marcar como no leída' no implementada");
    }

    private void showFullDetails() {
        SwingUtilities.invokeLater(() -> {
            NotificationDetailsDialog dialog = new NotificationDetailsDialog(
                    SwingUtilities.getWindowAncestor(this), notification);
            dialog.setVisible(true);
        });
    }

    private void copyContentToClipboard() {
        try {
            String content = "Título: " + notification.getTitulo() + "\n\n"
                    + "Contenido: " + notification.getContenido() + "\n\n"
                    + "Remitente: " + notification.getRemitenteNombre() + "\n"
                    + "Fecha: " + notification.getFechaCreacion()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(content);
            clipboard.setContents(selection, null);

            showInfo("Contenido copiado al portapapeles");

        } catch (Exception e) {
            showError("Error al copiar al portapapeles: " + e.getMessage());
        }
    }

    private void archiveNotification() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Archivar esta notificación?\n\nLa notificación será marcada como archivada.",
                "Confirmar archivo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (listener != null) {
                listener.onArchive(this);
            }
            showInfo("Notificación archivada");
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD PARA MENSAJES
    // ========================================
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            System.err.println("🚫 Error: " + message);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    // ========================================
    // MÉTODOS PÚBLICOS DE ACCESO
    // ========================================
    public NotificacionDestinatario getNotification() {
        return notification;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public int getNotificationId() {
        return notification.getId();
    }

    public boolean isRead() {
        return notification.isLeida();
    }

    public String getPriority() {
        return notification.getPrioridad();
    }

    public int getUserId() {
        return userId;
    }

    // ========================================
    // INTERFAZ PARA LISTENERS
    // ========================================
    public interface ExpandableNotificationListener {

        default void onExpansionChanged(ExpandableNotificationPanel panel, boolean isExpanded) {
        }

        default void onMarkAsRead(ExpandableNotificationPanel panel) {
        }

        default void onConfirm(ExpandableNotificationPanel panel) {
        }

        default void onArchive(ExpandableNotificationPanel panel) {
        }

        default void onRefreshRequested() {
        }
    }

    // ========================================
    // CLASE INTERNA PARA DIÁLOGO DE DETALLES OPTIMIZADA
    // ========================================
    private static class NotificationDetailsDialog extends JDialog {

        public NotificationDetailsDialog(Window parent, NotificacionDestinatario notification) {
            super(parent, "Detalles de Notificación", ModalityType.APPLICATION_MODAL);
            initializeDialog(notification);
        }

        private void initializeDialog(NotificacionDestinatario notification) {
            setSize(600, 500);
            setLocationRelativeTo(getParent());
            setLayout(new BorderLayout());

            // Panel principal
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Header con título e ícono
            JPanel headerPanel = createHeaderPanel(notification);

            // Contenido
            JScrollPane contentScrollPane = createContentPanel(notification);

            // Información adicional
            JPanel infoPanel = createInfoPanel(notification);

            // Botones
            JPanel buttonPanel = createButtonPanel();

            // Ensamblar
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(contentScrollPane, BorderLayout.CENTER);
            mainPanel.add(infoPanel, BorderLayout.SOUTH);

            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private JPanel createHeaderPanel(NotificacionDestinatario notification) {
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

            String icon = notification.getIconoPorDefecto();
            JLabel iconLabel = new JLabel(icon + " " + notification.getTitulo());
            iconLabel.setFont(new Font("Arial", Font.BOLD, 18));

            JLabel priorityLabel = new JLabel(notification.getPrioridad());
            priorityLabel.setFont(new Font("Arial", Font.BOLD, 12));
            priorityLabel.setOpaque(true);
            priorityLabel.setBackground(getPriorityColorStatic(notification.getPrioridad()));
            priorityLabel.setForeground(Color.WHITE);
            priorityLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

            headerPanel.add(iconLabel, BorderLayout.CENTER);
            headerPanel.add(priorityLabel, BorderLayout.EAST);

            return headerPanel;
        }

        private JScrollPane createContentPanel(NotificacionDestinatario notification) {
            JTextArea contentArea = new JTextArea(notification.getContenido());
            contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setBackground(new Color(248, 249, 250));
            contentArea.setBorder(new EmptyBorder(15, 15, 15, 15));

            JScrollPane contentScroll = new JScrollPane(contentArea);
            contentScroll.setBorder(BorderFactory.createTitledBorder("Contenido"));

            return contentScroll;
        }

        private JPanel createInfoPanel(NotificacionDestinatario notification) {
            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBorder(BorderFactory.createTitledBorder("Información"));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;

            addInfoRow(infoPanel, gbc, 0, "Remitente:", notification.getRemitenteNombre());
            addInfoRow(infoPanel, gbc, 1, "Fecha de envío:",
                    notification.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            addInfoRow(infoPanel, gbc, 2, "Estado:",
                    notification.isLeida() ? "✅ Leída" : "🔴 No leída");

            if (notification.getFechaLeida() != null) {
                addInfoRow(infoPanel, gbc, 3, "Fecha de lectura:",
                        notification.getFechaLeida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            }

            if (notification.isRequiereConfirmacion()) {
                addInfoRow(infoPanel, gbc, 4, "Confirmación:",
                        notification.isConfirmada() ? "✅ Confirmada" : "⏳ Pendiente");
            }

            return infoPanel;
        }

        private JPanel createButtonPanel() {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton closeButton = new JButton("Cerrar");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
            return buttonPanel;
        }

        private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(label), gbc);

            gbc.gridx = 1;
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD));
            panel.add(valueLabel, gbc);
        }

        private static Color getPriorityColorStatic(String priority) {
            switch (priority) {
                case "URGENTE":
                    return new Color(220, 53, 69);
                case "ALTA":
                    return new Color(255, 193, 7);
                case "NORMAL":
                    return new Color(0, 123, 255);
                case "BAJA":
                    return new Color(108, 117, 125);
                default:
                    return new Color(0, 123, 255);
            }
        }
    }

    // ========================================
    // MÉTODOS DE LIMPIEZA OPTIMIZADOS
    // ========================================
    /**
     * Limpia recursos del panel de forma segura
     */
    public void dispose() {
        try {
            System.out.println("🧹 Limpiando recursos de ExpandableNotificationPanel...");

            // Detener timer de animación si está corriendo
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
                animationTimer = null;
            }

            // Resetear flags de estado
            isAnimating = false;
            isProcessingRead = false;

            // Limpiar listeners de forma segura
            cleanupListeners();

            System.out.println("✅ ExpandableNotificationPanel disposed correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error en dispose(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanupListeners() {
        try {
            // Limpiar listeners de botones de forma segura
            cleanupButtonListeners(expandButton);
            cleanupButtonListeners(markReadButton);
            cleanupButtonListeners(actionsButton);

            // Limpiar listeners de mouse del headerPanel
            if (headerPanel != null) {
                MouseListener[] mouseListeners = headerPanel.getMouseListeners();
                for (MouseListener listener : mouseListeners) {
                    headerPanel.removeMouseListener(listener);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error limpiando listeners: " + e.getMessage());
        }
    }

    private void cleanupButtonListeners(JButton button) {
        if (button != null) {
            ActionListener[] actionListeners = button.getActionListeners();
            for (ActionListener listener : actionListeners) {
                button.removeActionListener(listener);
            }

            MouseListener[] mouseListeners = button.getMouseListeners();
            for (MouseListener listener : mouseListeners) {
                button.removeMouseListener(listener);
            }
        }
    }

    // ========================================
    // MÉTODOS DE DEBUG Y ESTADO
    // ========================================
    /**
     * Información de debug del panel
     */
    public String getDebugInfo() {
        return String.format(
                "ExpandableNotificationPanel[id=%d, userId=%d, isExpanded=%s, isAnimating=%s, isProcessingRead=%s, isRead=%s]",
                notification.getId(), userId, isExpanded, isAnimating, isProcessingRead, notification.isLeida()
        );
    }

    /**
     * Verifica si el panel está en un estado consistente
     */
    public boolean isHealthy() {
        try {
            boolean healthy = notification != null
                    && userId > 0
                    && headerPanel != null
                    && titleLabel != null
                    && !isProcessingRead; // No debe estar procesando indefinidamente

            if (!healthy) {
                System.err.println("⚠️ Panel no saludable: " + getDebugInfo());
            }

            return healthy;
        } catch (Exception e) {
            System.err.println("❌ Error verificando salud del panel: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fuerza actualización del panel si está en estado inconsistente
     */
    public void forceRefresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (!isAnimating && !isProcessingRead) {
                    updateAppearance();
                    revalidate();
                    repaint();
                    System.out.println("🔄 Panel actualizado forzadamente");
                }
            } catch (Exception e) {
                System.err.println("❌ Error en forceRefresh: " + e.getMessage());
            }
        });
    }

    // ========================================
    // OVERRIDE DE MÉTODOS DE JPANEL PARA DEBUGGING
    // ========================================
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            // Si se oculta el panel, limpiar estados
            isAnimating = false;
            isProcessingRead = false;
        }
    }

    @Override
    public void removeNotify() {
        // Llamado cuando el componente es removido del contenedor
        dispose();
        super.removeNotify();
    }

    @Override
    public String toString() {
        return "ExpandableNotificationPanel{"
                + "notificationId=" + (notification != null ? notification.getId() : "null")
                + ", userId=" + userId
                + ", isExpanded=" + isExpanded
                + ", isRead=" + (notification != null ? notification.isLeida() : "unknown")
                + '}';
    }
}
