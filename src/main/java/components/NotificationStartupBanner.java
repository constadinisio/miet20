package main.java.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;
import main.java.models.NotificacionDestinatario;
import main.java.services.NotificationService;
import main.java.views.notifications.NotificationsWindow;

/**
 * NotificationStartupBanner - Banner emergente al iniciar sesión
 *
 * Muestra un banner elegante y no intrusivo cuando el usuario tiene
 * notificaciones pendientes al iniciar sesión. Compatible con todos los roles.
 *
 * Características: - Aparece automáticamente si hay notificaciones no leídas -
 * Diseño elegante con animaciones suaves - Se posiciona en la esquina superior
 * derecha - Auto-desaparece después de un tiempo configurable - Permite
 * acciones rápidas (ver notificaciones, cerrar) - Integra con el sistema de
 * notificaciones existente
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class NotificationStartupBanner extends JWindow {

    private final int userId;
    private final int userRole;
    private final NotificationService notificationService;

    // Configuración del banner
    private static final int BANNER_WIDTH = 350;
    private static final int BANNER_HEIGHT = 120;
    private static final int AUTO_HIDE_DELAY = 8000; // 8 segundos
    private static final int SLIDE_SPEED = 15; // milisegundos entre frames de animación
    private static final int SLIDE_DISTANCE = 5; // píxeles por frame

    // Colores y estilos
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 250);
    private static final Color BORDER_COLOR = new Color(51, 153, 255);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 30);
    private static final Color TITLE_COLOR = new Color(51, 153, 255);
    private static final Color TEXT_COLOR = new Color(60, 60, 60);
    private static final Color BUTTON_COLOR = new Color(51, 153, 255);
    private static final Color CLOSE_BUTTON_COLOR = new Color(220, 53, 69);

    // Componentes UI
    private JPanel mainPanel;
    private JLabel iconLabel;
    private JLabel titleLabel;
    private JLabel messageLabel;
    private JLabel timeLabel;
    private JButton viewButton;
    private JButton closeButton;

    // Control de estado
    private Timer autoHideTimer;
    private Timer slideTimer;
    private boolean isSliding = false;
    private boolean isVisible = false;

    // Datos de notificaciones
    private int unreadCount = 0;
    private List<NotificacionDestinatario> recentNotifications;

    public NotificationStartupBanner(int userId, int userRole) {
        this.userId = userId;
        this.userRole = userRole;
        this.notificationService = NotificationService.getInstance();

        initializeComponents();
        setupLayout();
        setupListeners();

        // Hacer la ventana transparente y sin decoraciones
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        System.out.println("✅ NotificationStartupBanner inicializado para usuario: " + userId);
    }

    private void initializeComponents() {
        // Panel principal con sombra y bordes redondeados
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Habilitar antialiasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar sombra
                g2d.setColor(SHADOW_COLOR);
                g2d.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 15, 15);

                // Dibujar fondo principal
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 15, 15);

                // Dibujar borde
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 15, 15);

                g2d.dispose();
            }
        };

        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Ícono de notificación (lado izquierdo)
        iconLabel = new JLabel("🔔");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(50, 50));

        // Panel de contenido (centro)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // Título
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TITLE_COLOR);

        // Mensaje
        messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(TEXT_COLOR);

        // Tiempo
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        timeLabel.setForeground(Color.GRAY);

        // Panel de texto
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(messageLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(timeLabel);

        contentPanel.add(textPanel, BorderLayout.CENTER);

        // Panel de botones (lado derecho)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);

        // Botón ver notificaciones
        viewButton = new JButton("Ver");
        viewButton.setFont(new Font("Arial", Font.BOLD, 11));
        viewButton.setBackground(BUTTON_COLOR);
        viewButton.setForeground(Color.WHITE);
        viewButton.setBorderPainted(false);
        viewButton.setFocusPainted(false);
        viewButton.setPreferredSize(new Dimension(60, 25));
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Botón cerrar
        closeButton = new JButton("✕");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setBackground(CLOSE_BUTTON_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonsPanel.add(viewButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(closeButton);

        // Ensamblar panel principal
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(Box.createHorizontalStrut(15), BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private void setupLayout() {
        setSize(BANNER_WIDTH, BANNER_HEIGHT);

        // Posicionar en la esquina superior derecha de la pantalla
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
        );

        int x = screenSize.width - BANNER_WIDTH - insets.right - 20;
        int y = insets.top + 20;

        setLocation(x, y);
    }

    private void setupListeners() {
        // Botón ver notificaciones
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNotificationsWindow();
                hideBanner();
            }
        });

        // Botón cerrar
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideBanner();
            }
        });

        // Hover effects para botones
        setupButtonHoverEffects();

        // Click en el panel principal para ver notificaciones
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isButtonClick(e)) {
                    openNotificationsWindow();
                    hideBanner();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                pauseAutoHide();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                resumeAutoHide();
            }
        });

        // Listener para cerrar con escape
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Foco para capturar teclas
                requestFocus();
            }
        });

        // KeyListener para escape
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    hideBanner();
                }
            }
        });
    }

    private void setupButtonHoverEffects() {
        // Efecto hover para botón ver
        viewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                viewButton.setBackground(BUTTON_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                viewButton.setBackground(BUTTON_COLOR);
            }
        });

        // Efecto hover para botón cerrar
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(CLOSE_BUTTON_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(CLOSE_BUTTON_COLOR);
            }
        });
    }

    private boolean isButtonClick(MouseEvent e) {
        Point clickPoint = e.getPoint();
        Rectangle viewButtonBounds = viewButton.getBounds();
        Rectangle closeButtonBounds = closeButton.getBounds();

        return viewButtonBounds.contains(clickPoint) || closeButtonBounds.contains(clickPoint);
    }
    // =====================================
    // MÉTODOS PRINCIPALES DE FUNCIONALIDAD
    // =====================================

    /**
     * Verifica si hay notificaciones no leídas y muestra el banner si es
     * necesario
     */
    public void checkAndShowIfNeeded() {
        SwingWorker<NotificationData, Void> worker = new SwingWorker<NotificationData, Void>() {
            @Override
            protected NotificationData doInBackground() throws Exception {
                try {
                    // Obtener contador de no leídas (con consulta corregida)
                    int count = notificationService.contarNotificacionesNoLeidas(userId);

                    System.out.println("🔍 Usuario " + userId + " tiene " + count + " notificaciones no leídas");

                    if (count > 0) {
                        // Obtener las notificaciones recientes para mostrar información
                        List<NotificacionDestinatario> recent
                                = notificationService.obtenerNotificacionesNoLeidas(userId);

                        // Verificar que la lista no esté vacía (double-check)
                        if (recent != null && !recent.isEmpty()) {
                            System.out.println("📋 Obtenidas " + recent.size() + " notificaciones para el banner");
                            return new NotificationData(count, recent);
                        } else {
                            System.out.println("⚠️ Contador indica " + count + " pero lista vacía - inconsistencia de datos");
                            return new NotificationData(0, null);
                        }
                    }

                    return new NotificationData(0, null);

                } catch (Exception e) {
                    System.err.println("❌ Error en doInBackground del banner: " + e.getMessage());
                    e.printStackTrace();
                    // Retornar datos vacíos en caso de error
                    return new NotificationData(0, null);
                }
            }

            @Override
            protected void done() {
                try {
                    NotificationData data = get();

                    if (data != null && data.count > 0) {
                        unreadCount = data.count;
                        recentNotifications = data.notifications;

                        SwingUtilities.invokeLater(() -> {
                            try {
                                updateContent();
                                showBanner();
                                System.out.println("✅ Banner mostrado para " + unreadCount + " notificaciones");
                            } catch (Exception e) {
                                System.err.println("❌ Error mostrando banner: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    } else {
                        System.out.println("📬 No hay notificaciones pendientes - Banner no mostrado");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error verificando notificaciones para banner: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    /**
     * Actualiza el contenido del banner con la información de notificaciones
     */
    private void updateContent() {
        try {
            // Título según cantidad
            String title = unreadCount == 1
                    ? "Nueva notificación"
                    : unreadCount + " notificaciones nuevas";
            titleLabel.setText(title);

            // Mensaje basado en la notificación más reciente
            String message = "Tienes mensajes importantes pendientes";
            String icon = "🔔";
            Color iconColor = TITLE_COLOR; // Color por defecto

            if (recentNotifications != null && !recentNotifications.isEmpty()) {
                NotificacionDestinatario recent = recentNotifications.get(0);

                // Validar que la notificación tenga título
                String recentTitle = recent.getTitulo();
                if (recentTitle != null && !recentTitle.trim().isEmpty()) {
                    if (recentTitle.length() > 35) {
                        recentTitle = recentTitle.substring(0, 32) + "...";
                    }
                    message = "Más reciente: " + recentTitle;
                } else {
                    message = "Más reciente: Notificación sin título";
                }

                // Cambiar ícono y color según prioridad de la más reciente
                String prioridad = recent.getPrioridad();
                if (prioridad != null) {
                    switch (prioridad) {
                        case "URGENTE":
                            icon = "🚨";
                            iconColor = new Color(220, 53, 69);
                            break;
                        case "ALTA":
                            icon = "⚠️";
                            iconColor = new Color(255, 193, 7);
                            break;
                        case "NORMAL":
                            icon = "ℹ️";
                            iconColor = TITLE_COLOR;
                            break;
                        case "BAJA":
                            icon = "📝";
                            iconColor = new Color(108, 117, 125);
                            break;
                        default:
                            icon = "🔔";
                            iconColor = TITLE_COLOR;
                    }
                }
            }

            iconLabel.setText(icon);
            iconLabel.setForeground(iconColor);
            messageLabel.setText(message);

            // Timestamp
            String timeText = "Actualizado " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            timeLabel.setText(timeText);

            System.out.println("📋 Banner actualizado: " + unreadCount + " notificaciones");

        } catch (Exception e) {
            System.err.println("❌ Error actualizando contenido del banner: " + e.getMessage());
            e.printStackTrace();

            // Fallback: mostrar mensaje genérico
            titleLabel.setText("Notificaciones pendientes");
            messageLabel.setText("Error cargando detalles");
            iconLabel.setText("🔔");
            iconLabel.setForeground(TITLE_COLOR);
        }
    }

    /**
     * Muestra el banner con animación de deslizamiento
     */
    public void showBanner() {
        // Sincronización para evitar condiciones de carrera
        synchronized (this) {
            if (isVisible || isSliding) {
                System.out.println("⚠️ Banner ya visible o en animación, ignorando showBanner()");
                return;
            }
            isSliding = true; // Marcar inmediatamente para evitar dobles llamadas
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Posicionar fuera de la pantalla
                Point finalPosition = getLocation();
                setLocation(finalPosition.x + BANNER_WIDTH, finalPosition.y);

                setVisible(true);

                // Animación de deslizamiento hacia adentro
                slideTimer = new Timer(SLIDE_SPEED, new ActionListener() {
                    private int currentX = finalPosition.x + BANNER_WIDTH;
                    private final int targetX = finalPosition.x;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentX -= SLIDE_DISTANCE;

                        if (currentX <= targetX) {
                            currentX = targetX;
                            slideTimer.stop();

                            synchronized (NotificationStartupBanner.this) {
                                isSliding = false;
                                isVisible = true;
                            }

                            // Iniciar timer de auto-ocultado
                            startAutoHideTimer();
                            System.out.println("✅ Banner mostrado exitosamente");
                        }

                        setLocation(currentX, finalPosition.y);
                    }
                });

                slideTimer.start();

            } catch (Exception ex) {
                System.err.println("❌ Error en animación de mostrar banner: " + ex.getMessage());
                synchronized (NotificationStartupBanner.this) {
                    isSliding = false;
                }
            }
        });
    }

    /**
     * Oculta el banner con animación de deslizamiento
     */
    public void hideBanner() {
        synchronized (this) {
            if (!isVisible || isSliding) {
                System.out.println("⚠️ Banner no visible o ya en animación, ignorando hideBanner()");
                return;
            }
            isSliding = true; // Marcar inmediatamente
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Detener auto-hide timer
                if (autoHideTimer != null && autoHideTimer.isRunning()) {
                    autoHideTimer.stop();
                }

                Point currentPosition = getLocation();

                // Animación de deslizamiento hacia afuera
                slideTimer = new Timer(SLIDE_SPEED, new ActionListener() {
                    private int currentX = currentPosition.x;
                    private final int targetX = currentPosition.x + BANNER_WIDTH;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentX += SLIDE_DISTANCE;

                        if (currentX >= targetX) {
                            slideTimer.stop();
                            setVisible(false);

                            synchronized (NotificationStartupBanner.this) {
                                isSliding = false;
                                isVisible = false;
                            }

                            System.out.println("✅ Banner ocultado exitosamente");

                            // Liberar recursos si es necesario
                            SwingUtilities.invokeLater(() -> dispose());
                        }

                        setLocation(currentX, currentPosition.y);
                    }
                });

                slideTimer.start();

            } catch (Exception ex) {
                System.err.println("❌ Error en animación de ocultar banner: " + ex.getMessage());
                synchronized (NotificationStartupBanner.this) {
                    isSliding = false;
                    isVisible = false;
                }
                setVisible(false);
                dispose();
            }
        });
    }

    private void startAutoHideTimer() {
        autoHideTimer = new Timer(AUTO_HIDE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isVisible && !isSliding) {
                    hideBanner();
                }
            }
        });
        autoHideTimer.setRepeats(false);
        autoHideTimer.start();
    }

    private void pauseAutoHide() {
        if (autoHideTimer != null && autoHideTimer.isRunning()) {
            autoHideTimer.stop();
        }
    }

    private void resumeAutoHide() {
        if (isVisible && !isSliding) {
            startAutoHideTimer();
        }
    }

    private void openNotificationsWindow() {
        try {
            SwingUtilities.invokeLater(() -> {
                NotificationsWindow window = new NotificationsWindow(userId);
                window.setVisible(true);
                window.toFront();
                window.requestFocus();
            });

            System.out.println("📱 Abriendo ventana de notificaciones desde banner");

        } catch (Exception e) {
            System.err.println("Error abriendo ventana de notificaciones: " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Error al abrir las notificaciones: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================
    // MÉTODOS PÚBLICOS DE CONTROL
    // =====================================
    /**
     * Fuerza la actualización del banner con nuevos datos
     */
    public void forceRefresh() {
        try {
            if (isVisible && !isSliding) {
                System.out.println("🔄 Forzando actualización del banner...");
                checkAndShowIfNeeded();
            } else if (isSliding) {
                System.out.println("⏳ Banner en animación, refresh programado...");
                // Programar refresh después de la animación
                Timer delayedRefresh = new Timer(1000, e -> {
                    if (!isSliding) {
                        checkAndShowIfNeeded();
                    }
                });
                delayedRefresh.setRepeats(false);
                delayedRefresh.start();
            } else {
                System.out.println("👻 Banner no visible, verificando si debe mostrarse...");
                checkAndShowIfNeeded();
            }
        } catch (Exception e) {
            System.err.println("❌ Error en forceRefresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Oculta inmediatamente el banner sin animación
     */
    public void forceHide() {
        synchronized (this) {
            // Detener todos los timers
            if (autoHideTimer != null) {
                autoHideTimer.stop();
                autoHideTimer = null;
            }
            if (slideTimer != null) {
                slideTimer.stop();
                slideTimer = null;
            }

            // Actualizar estado
            isVisible = false;
            isSliding = false;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                setVisible(false);
                dispose();
                System.out.println("✅ Banner forzado a ocultar");
            } catch (Exception e) {
                System.err.println("❌ Error en forceHide: " + e.getMessage());
            }
        });
    }

    /**
     * Verifica si el banner está actualmente visible
     */
    public boolean isBannerVisible() {
        return isVisible;
    }

    /**
     * Obtiene el número de notificaciones no leídas actual
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    // =====================================
    // MÉTODOS ESTÁTICOS DE UTILIDAD
    // =====================================
    /**
     * Método estático para crear y mostrar banner automáticamente Este es el
     * método principal que debe llamarse desde VentanaInicio
     */
    public static void showIfNeeded(int userId, int userRole) {
        try {
            System.out.println("🔍 Verificando banner para usuario: " + userId + ", rol: " + userRole);

            // Validar parámetros
            if (userId <= 0) {
                System.err.println("❌ ID de usuario inválido: " + userId);
                return;
            }

            // Verificar que el servicio de notificaciones esté disponible
            NotificationService service = NotificationService.getInstance();
            if (service == null) {
                System.err.println("❌ NotificationService no disponible");
                return;
            }

            NotificationStartupBanner banner = new NotificationStartupBanner(userId, userRole);
            banner.checkAndShowIfNeeded();

        } catch (Exception e) {
            System.err.println("❌ Error creando banner de notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para mostrar banner de prueba (desarrollo)
     */
    public static void showTestBanner(int userId, int userRole) {
        try {
            NotificationStartupBanner banner = new NotificationStartupBanner(userId, userRole);
            banner.unreadCount = 3;
            banner.updateContent();
            banner.showBanner();

            System.out.println("🧪 Banner de prueba mostrado");

        } catch (Exception e) {
            System.err.println("Error mostrando banner de prueba: " + e.getMessage());
        }
    }

    // =====================================
    // CLASES INTERNAS Y DATOS
    // =====================================
    /**
     * Clase interna para encapsular datos de notificaciones
     */
    private static class NotificationData {

        final int count;
        final List<NotificacionDestinatario> notifications;

        NotificationData(int count, List<NotificacionDestinatario> notifications) {
            this.count = Math.max(0, count); // Asegurar que count no sea negativo
            this.notifications = notifications;
        }

        /**
         * Verifica si los datos son válidos
         */
        public boolean isValid() {
            return count >= 0 && (count == 0 || (notifications != null && !notifications.isEmpty()));
        }

        /**
         * Obtiene información de debug
         */
        public String getDebugInfo() {
            return String.format("NotificationData[count=%d, notifications=%s]",
                    count, notifications != null ? notifications.size() : "null");
        }
    }

    // =====================================
    // MÉTODOS DE LIMPIEZA Y CIERRE
    // =====================================
    @Override
    public void dispose() {
        try {
            // Detener todos los timers
            if (autoHideTimer != null && autoHideTimer.isRunning()) {
                autoHideTimer.stop();
                autoHideTimer = null;
            }

            if (slideTimer != null && slideTimer.isRunning()) {
                slideTimer.stop();
                slideTimer = null;
            }

            // Limpiar referencias
            recentNotifications = null;

            System.out.println("✅ NotificationStartupBanner recursos liberados");

        } catch (Exception e) {
            System.err.println("Error liberando recursos del banner: " + e.getMessage());
        } finally {
            super.dispose();
        }
    }

    // =====================================
    // MÉTODOS DE DEBUG Y UTILIDAD
    // =====================================
    /**
     * Información de debug del banner
     */
    public String getDebugInfo() {
        return String.format(
                "NotificationStartupBanner [userId=%d, userRole=%d, unreadCount=%d, isVisible=%s, isSliding=%s]",
                userId, userRole, unreadCount, isVisible, isSliding
        );
    }

    /**
     * Método para testing - simula notificaciones
     */
    public void simulateNotifications(int count) {
        this.unreadCount = count;
        updateContent();
        showBanner();
    }
}
