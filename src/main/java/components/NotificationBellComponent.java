package main.java.components;

import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;
import main.java.models.NotificacionDestinatario;
import main.java.services.NotificationService;
import main.java.views.notifications.NotificationsWindow;

/**
 * Componente visual de campanita de notificaciones para la interfaz principal.
 * Muestra el número de notificaciones no leídas y permite acceso rápido a las
 * mismas.
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class NotificationBellComponent extends JPanel implements NotificationService.NotificationListener {

    private final int usuarioId;
    private final NotificationService notificationService;

    // Componentes UI
    private JLabel bellIcon;
    private JLabel counterLabel;
    private JPopupMenu notificationsPopup;
    private JPanel notificationsPanel;
    private JScrollPane scrollPane;

    // Estado
    private int unreadCount = 0;
    private boolean isPopupVisible = false;
    private Timer refreshTimer;
    private Timer animationTimer;

    // Constantes de diseño
    private static final Color BELL_COLOR = new Color(108, 117, 125);
    private static final Color BELL_ACTIVE_COLOR = new Color(40, 167, 69);
    private static final Color BELL_URGENT_COLOR = new Color(220, 53, 69);
    private static final Color COUNTER_BACKGROUND = new Color(220, 53, 69);
    private static final Color COUNTER_TEXT = Color.WHITE;
    private static final Font COUNTER_FONT = new Font("Arial", Font.BOLD, 10);
    private static final int MAX_POPUP_NOTIFICATIONS = 5;

    /**
     * Constructor del componente campanita
     *
     * @param usuarioId ID del usuario actual
     */
    public NotificationBellComponent(int usuarioId) {
        this.usuarioId = usuarioId;
        this.notificationService = NotificationService.getInstance();

        initializeComponents();
        setupLayout();
        setupListeners();
        startRefreshTimer();

        // Registrarse como listener para actualizaciones en tiempo real
        notificationService.agregarListener(this);

        // Cargar contador inicial
        refreshUnreadCount();
    }

    /**
     * Inicializa los componentes visuales
     */
    private void initializeComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Ícono de campanita
        bellIcon = new JLabel("🔔");
        bellIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bellIcon.setForeground(BELL_COLOR);

        // Contador de notificaciones no leídas
        counterLabel = new JLabel();
        counterLabel.setFont(COUNTER_FONT);
        counterLabel.setForeground(COUNTER_TEXT);
        counterLabel.setOpaque(true);
        counterLabel.setBackground(COUNTER_BACKGROUND);
        counterLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        counterLabel.setVisible(false);

        // Popup de notificaciones
        createNotificationsPopup();
    }

    /**
     * Configura el layout del componente
     */
    private void setupLayout() {
        add(bellIcon);
        add(counterLabel);

        setPreferredSize(new Dimension(60, 30));
        setMinimumSize(new Dimension(60, 30));
    }

    /**
     * Configura los listeners de eventos
     */
    private void setupListeners() {
        // Click en la campanita
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleNotificationsPopup();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (unreadCount > 0) {
                    bellIcon.setForeground(BELL_URGENT_COLOR);
                } else {
                    bellIcon.setForeground(BELL_ACTIVE_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isPopupVisible) {
                    bellIcon.setForeground(unreadCount > 0 ? BELL_URGENT_COLOR : BELL_COLOR);
                }
            }
        });

        // Listener para cerrar popup al hacer click fuera
        notificationsPopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = true;
                bellIcon.setForeground(BELL_ACTIVE_COLOR);
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = false;
                bellIcon.setForeground(unreadCount > 0 ? BELL_URGENT_COLOR : BELL_COLOR);
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = false;
                bellIcon.setForeground(unreadCount > 0 ? BELL_URGENT_COLOR : BELL_COLOR);
            }
        });
    }

    /**
     * Crea el popup de notificaciones
     */
    private void createNotificationsPopup() {
        notificationsPopup = new JPopupMenu();
        notificationsPopup.setPreferredSize(new Dimension(400, 350));

        // Panel principal del popup
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header del popup
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("🔔 Notificaciones");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Botón marcar todas como leídas
        JButton markAllReadBtn = new JButton("✅ Marcar todas");
        markAllReadBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        markAllReadBtn.addActionListener(e -> markAllAsRead());
        headerPanel.add(markAllReadBtn, BorderLayout.EAST);

        // Panel de notificaciones con scroll
        notificationsPanel = new JPanel();
        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(notificationsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Footer con botón "Ver todas"
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton viewAllBtn = new JButton("📋 Ver Todas las Notificaciones");
        viewAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewAllBtn.setBackground(new Color(0, 123, 255));
        viewAllBtn.setForeground(Color.WHITE);
        viewAllBtn.addActionListener(e -> openNotificationsWindow());
        footerPanel.add(viewAllBtn);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        notificationsPopup.add(mainPanel);
    }

    /**
     * Muestra/oculta el popup de notificaciones
     */
    private void toggleNotificationsPopup() {
        if (notificationsPopup.isVisible()) {
            notificationsPopup.setVisible(false);
        } else {
            loadRecentNotifications();
            // Mostrar popup debajo de la campanita
            Point locationOnScreen = getLocationOnScreen();
            notificationsPopup.show(this, -330, getHeight() + 5);
        }
    }

    /**
     * Carga y muestra las notificaciones recientes en el popup
     */
    private void loadRecentNotifications() {
        SwingWorker<List<NotificacionDestinatario>, Void> worker
                = new SwingWorker<List<NotificacionDestinatario>, Void>() {
            @Override
            protected List<NotificacionDestinatario> doInBackground() throws Exception {
                return notificationService.obtenerNotificacionesNoLeidas(usuarioId);
            }

            @Override
            protected void done() {
                try {
                    List<NotificacionDestinatario> notifications = get();
                    updateNotificationsPanel(notifications);
                } catch (Exception e) {
                    System.err.println("Error cargando notificaciones: " + e.getMessage());
                    showErrorPanel();
                }
            }
        };
        worker.execute();
    }

    /**
     * Actualiza el panel de notificaciones
     */
    private void updateNotificationsPanel(List<NotificacionDestinatario> notifications) {
        // Limpiar panel
        notificationsPanel.removeAll();

        if (notifications.isEmpty()) {
            showEmptyPanel();
        } else {
            // Mostrar máximo MAX_POPUP_NOTIFICATIONS notificaciones
            int maxToShow = Math.min(notifications.size(), MAX_POPUP_NOTIFICATIONS);

            for (int i = 0; i < maxToShow; i++) {
                NotificacionDestinatario notification = notifications.get(i);
                JPanel notificationItem = createNotificationItem(notification);
                notificationsPanel.add(notificationItem);

                if (i < maxToShow - 1) {
                    notificationsPanel.add(Box.createVerticalStrut(5));
                }
            }

            // Si hay más notificaciones, mostrar indicador
            if (notifications.size() > MAX_POPUP_NOTIFICATIONS) {
                JLabel moreLabel = new JLabel("... y " + (notifications.size() - MAX_POPUP_NOTIFICATIONS) + " más");
                moreLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                moreLabel.setForeground(Color.GRAY);
                moreLabel.setBorder(new EmptyBorder(5, 10, 5, 5));
                notificationsPanel.add(Box.createVerticalStrut(5));
                notificationsPanel.add(moreLabel);
            }
        }

        notificationsPanel.revalidate();
        notificationsPanel.repaint();
    }

    /**
     * Crea un item visual para una notificación
     */
    private JPanel createNotificationItem(NotificacionDestinatario notification) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
        itemPanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Altura fija

        // Color de fondo según prioridad - CORREGIDO: hacer variable final
        final Color backgroundColor;
        if ("URGENTE".equals(notification.getPrioridad())) {
            backgroundColor = new Color(255, 245, 245);
        } else if ("ALTA".equals(notification.getPrioridad())) {
            backgroundColor = new Color(255, 250, 240);
        } else {
            backgroundColor = Color.WHITE;
        }
        itemPanel.setBackground(backgroundColor);

        // Ícono de prioridad
        JLabel iconLabel = new JLabel(notification.getIconoPorDefecto());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        iconLabel.setPreferredSize(new Dimension(20, 60));

        // Contenido principal - COMPLETAMENTE REDISEÑADO
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(2, 5, 2, 5));

        // CORREGIDO: Título con validación null-safe
        String tituloTexto = notification.getTitulo();
        if (tituloTexto == null || tituloTexto.trim().isEmpty()) {
            tituloTexto = "Sin título";
        }
        if (tituloTexto.length() > 30) {
            tituloTexto = tituloTexto.substring(0, 27) + "...";
        }

        JLabel titleLabel = new JLabel(tituloTexto);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // CORREGIDO: Contenido sin HTML problemático
        String contenidoTexto = notification.getContenido();
        if (contenidoTexto == null || contenidoTexto.trim().isEmpty()) {
            contenidoTexto = "Sin contenido";
        }
        if (contenidoTexto.length() > 60) {
            contenidoTexto = contenidoTexto.substring(0, 57) + "...";
        }

        // IMPORTANTE: NO usar HTML aquí - es lo que causa el problema
        JLabel contentLabel = new JLabel(contenidoTexto);
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        contentLabel.setForeground(new Color(60, 60, 60));
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // OPCIONAL: Permitir texto en múltiples líneas si es necesario
        // contentLabel.setVerticalAlignment(SwingConstants.TOP);

        // CORREGIDO: Información de tiempo y remitente con validación
        String remitenteNombre = notification.getRemitenteNombre();
        if (remitenteNombre == null || remitenteNombre.trim().isEmpty()) {
            remitenteNombre = "Sistema";
        }
        if (remitenteNombre.length() > 20) {
            remitenteNombre = remitenteNombre.substring(0, 17) + "...";
        }

        String timeInfo = "";
        if (notification.getFechaCreacion() != null) {
            timeInfo = notification.getFechaCreacion().format(
                    DateTimeFormatter.ofPattern("HH:mm")
            );
        } else {
            timeInfo = "??:??";
        }

        JLabel infoLabel = new JLabel("👤 " + remitenteNombre + " • " + timeInfo);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ensamblar contenido con espaciado adecuado
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(contentLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalGlue()); // Para centrar verticalmente

        // Botón marcar como leída - MEJORADO
        JButton markReadBtn = new JButton("✓");
        markReadBtn.setFont(new Font("Arial", Font.BOLD, 11));
        markReadBtn.setPreferredSize(new Dimension(28, 28));
        markReadBtn.setMinimumSize(new Dimension(28, 28));
        markReadBtn.setMaximumSize(new Dimension(28, 28));
        markReadBtn.setToolTipText("Marcar como leída");
        markReadBtn.setBackground(new Color(40, 167, 69));
        markReadBtn.setForeground(Color.WHITE);
        markReadBtn.setBorderPainted(false);
        markReadBtn.setFocusPainted(false);
        markReadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // CORREGIDO: Listener con ID válido
        final int notificationId = notification.getId();
        markReadBtn.addActionListener(e -> {
            if (notificationId > 0) {
                markAsRead(notificationId);
            } else {
                System.err.println("❌ ID de notificación inválido: " + notificationId);
            }
        });

        // Panel derecho para el botón
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(markReadBtn, BorderLayout.NORTH);

        // Ensamblar panel principal
        itemPanel.add(iconLabel, BorderLayout.WEST);
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        itemPanel.add(rightPanel, BorderLayout.EAST);

        // CORREGIDO: Efectos hover más suaves
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Color hover más suave
                Color hoverColor = new Color(
                        backgroundColor.getRed() - 10,
                        backgroundColor.getGreen() - 10,
                        backgroundColor.getBlue() - 10
                );
                itemPanel.setBackground(hoverColor);
                itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(backgroundColor);
                itemPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Doble click abre ventana de notificaciones
                    openNotificationsWindow();
                }
            }
        });

        // OPCIONAL: Borde sutil para separar elementos
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 8, 5, 8),
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1)
        ));

        return itemPanel;
    }

    /**
     * Muestra panel cuando no hay notificaciones
     */
    private void showEmptyPanel() {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setPreferredSize(new Dimension(350, 150));

        JLabel emptyIcon = new JLabel("📭", SwingConstants.CENTER);
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emptyIcon.setForeground(Color.LIGHT_GRAY);

        JLabel emptyText = new JLabel("No hay notificaciones nuevas", SwingConstants.CENTER);
        emptyText.setFont(new Font("Arial", Font.ITALIC, 14));
        emptyText.setForeground(Color.GRAY);

        emptyPanel.add(emptyIcon, BorderLayout.CENTER);
        emptyPanel.add(emptyText, BorderLayout.SOUTH);

        notificationsPanel.add(emptyPanel);
    }

    /**
     * Muestra panel de error
     */
    private void showErrorPanel() {
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setPreferredSize(new Dimension(350, 150));

        JLabel errorIcon = new JLabel("⚠️", SwingConstants.CENTER);
        errorIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel errorText = new JLabel("Error cargando notificaciones", SwingConstants.CENTER);
        errorText.setFont(new Font("Arial", Font.ITALIC, 14));
        errorText.setForeground(Color.RED);

        errorPanel.add(errorIcon, BorderLayout.CENTER);
        errorPanel.add(errorText, BorderLayout.SOUTH);

        notificationsPanel.add(errorPanel);
    }

    /**
     * Marca una notificación como leída
     */
    private void markAsRead(int notificationId) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return notificationService.marcarComoLeida(notificationId, usuarioId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        refreshUnreadCount();
                        loadRecentNotifications(); // Recargar popup
                    }
                } catch (Exception e) {
                    System.err.println("Error marcando notificación como leída: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    private void markAllAsRead() {
        int confirm = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "¿Marcar todas las notificaciones como leídas?",
                "Confirmar acción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<NotificacionDestinatario> unreadNotifications
                            = notificationService.obtenerNotificacionesNoLeidas(usuarioId);

                    for (NotificacionDestinatario notification : unreadNotifications) {
                        notificationService.marcarComoLeida(notification.getId(), usuarioId);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    refreshUnreadCount();
                    loadRecentNotifications();
                }
            };
            worker.execute();
        }
    }

    /**
     * Abre la ventana completa de notificaciones
     */
    private void openNotificationsWindow() {
        notificationsPopup.setVisible(false);

        SwingUtilities.invokeLater(() -> {
            NotificationsWindow window = new NotificationsWindow(usuarioId);
            window.setVisible(true);
        });
    }

    /**
     * Refresca el contador de notificaciones no leídas
     */
    private void refreshUnreadCount() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return notificationService.contarNotificacionesNoLeidas(usuarioId);
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    updateUnreadCount(count);
                } catch (Exception e) {
                    System.err.println("Error obteniendo contador de notificaciones: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Actualiza el contador visual
     */
    private void updateUnreadCount(int count) {
        int previousCount = this.unreadCount;
        this.unreadCount = count;

        SwingUtilities.invokeLater(() -> {
            if (count > 0) {
                counterLabel.setText(count > 99 ? "99+" : String.valueOf(count));
                counterLabel.setVisible(true);
                bellIcon.setForeground(BELL_URGENT_COLOR);

                // Animar si hay nuevas notificaciones
                if (count > previousCount && previousCount >= 0) {
                    animateNewNotification();
                }
            } else {
                counterLabel.setVisible(false);
                if (!isPopupVisible) {
                    bellIcon.setForeground(BELL_COLOR);
                }
            }

            revalidate();
            repaint();
        });
    }

    /**
     * Inicia el timer de refresco automático
     */
    private void startRefreshTimer() {
        // Refrescar contador cada 30 segundos
        refreshTimer = new Timer(30000, e -> refreshUnreadCount());
        refreshTimer.start();
    }

    /**
     * Animación cuando llegan nuevas notificaciones
     */
    private void animateNewNotification() {
        if (animationTimer != null && animationTimer.isRunning()) {
            return; // Ya hay una animación en curso
        }

        SwingUtilities.invokeLater(() -> {
            final int[] step = {0};
            final Font originalFont = bellIcon.getFont();
            final Color originalColor = bellIcon.getForeground();

            animationTimer = new Timer(150, null);
            animationTimer.addActionListener(e -> {
                if (step[0] < 4) {
                    // Efecto de pulso
                    if (step[0] % 2 == 0) {
                        bellIcon.setFont(originalFont.deriveFont(22f));
                        bellIcon.setForeground(BELL_URGENT_COLOR.brighter());
                    } else {
                        bellIcon.setFont(originalFont);
                        bellIcon.setForeground(originalColor);
                    }
                    step[0]++;
                } else {
                    // Restaurar estado original
                    bellIcon.setFont(originalFont);
                    bellIcon.setForeground(unreadCount > 0 ? BELL_URGENT_COLOR : BELL_COLOR);
                    animationTimer.stop();
                }
                repaint();
            });
            animationTimer.start();
        });
    }

    // Implementación de NotificationListener
    @Override
    public void onNuevaNotificacion(int targetUsuarioId, int notificacionId) {
        if (targetUsuarioId == this.usuarioId) {
            refreshUnreadCount();
            animateNewNotification();
        }
    }

    @Override
    public void onCambioEstadoNotificacion(int targetUsuarioId) {
        if (targetUsuarioId == this.usuarioId) {
            refreshUnreadCount();
        }
    }

    /**
     * Método para limpiar recursos
     */
    public void dispose() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        notificationService.removerListener(this);
    }

    /**
     * Getter para pruebas
     */
    public int getUnreadCount() {
        return unreadCount;
    }
}
