package main.java.tickets;

import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;
import main.java.tickets.Ticket;
import main.java.tickets.TicketService;
import main.java.tickets.TicketManagementWindow;

/**
 * Componente visual de campanita específica para tickets de desarrollador
 * VERSIÓN OPTIMIZADA - Basada en el patrón de NotificationBellComponent
 *
 * Características: - Popup creado UNA SOLA VEZ al inicio - Solo se recargan los
 * DATOS cuando es necesario - Toggle rápido usando setVisible() del popup
 * existente - Lazy loading del contenido al momento de mostrar
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Optimizado como NotificationBellComponent
 */
public class TicketBellComponent extends JPanel {

    private final int usuarioId;
    private final TicketService ticketService;

    // Componentes UI
    private JLabel bellIcon;
    private JLabel counterLabel;
    private JPopupMenu ticketsPopup; // Creado UNA VEZ, reutilizado siempre
    private JPanel ticketsPanel;
    private JScrollPane scrollPane;

    // Estado
    private int pendingTicketsCount = 0;
    private boolean isPopupVisible = false;
    private Timer refreshTimer;
    private Timer animationTimer;

    // Constantes de diseño
    private static final Color TICKET_BELL_COLOR = new Color(255, 111, 97);
    private static final Color TICKET_ACTIVE_COLOR = new Color(255, 87, 51);
    private static final Color TICKET_URGENT_COLOR = new Color(255, 51, 51);
    private static final Color COUNTER_BACKGROUND = new Color(255, 51, 51);
    private static final Color COUNTER_TEXT = Color.WHITE;
    private static final Font COUNTER_FONT = new Font("Arial", Font.BOLD, 10);
    private static final int MAX_POPUP_TICKETS = 5;

    public TicketBellComponent(int usuarioId) {
        this.usuarioId = usuarioId;
        this.ticketService = TicketService.getInstance();

        // Solo crear si el usuario es desarrollador
        if (!ticketService.esDeveloper(usuarioId)) {
            setVisible(false);
            return;
        }

        try {
            System.out.println("🚀 Inicializando TicketBellComponent OPTIMIZADO para desarrollador: " + usuarioId);

            // PASO 1: Inicialización básica inmediata
            initializeComponents();
            setupLayout();
            setupListeners();

            // PASO 2: Mostrar campanita inmediatamente
            setVisible(true);

            // PASO 3: Crear popup UNA SOLA VEZ en background
            SwingUtilities.invokeLater(() -> {
                createTicketsPopupOnce(); // Solo una vez!
                startRefreshTimer();
                refreshTicketCountBackground();
            });

            System.out.println("✅ TicketBellComponent optimizado iniciado");

        } catch (Exception e) {
            System.err.println("❌ Error crítico en constructor: " + e.getMessage());
            e.printStackTrace();
            createFallbackComponent();
        }
    }

    private void initializeComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Ícono de ticket
        bellIcon = new JLabel("🎫");
        bellIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bellIcon.setForeground(TICKET_BELL_COLOR);
        bellIcon.setToolTipText("Centro de Ayuda - Cargando...");

        // Contador
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
    }

    private void setupLayout() {
        add(bellIcon);
        add(counterLabel);
        setPreferredSize(new Dimension(60, 30));
        setMinimumSize(new Dimension(60, 30));
    }

    private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    toggleTicketsPopup(); // MÉTODO SIMPLE Y RÁPIDO
                } else if (e.getClickCount() == 2) {
                    openTicketManagement();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (pendingTicketsCount > 0) {
                    bellIcon.setForeground(TICKET_URGENT_COLOR);
                } else {
                    bellIcon.setForeground(TICKET_ACTIVE_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isPopupVisible) {
                    bellIcon.setForeground(pendingTicketsCount > 0 ? TICKET_URGENT_COLOR : TICKET_BELL_COLOR);
                }
            }
        });
    }

    // ========================================
    // MÉTODO PRINCIPAL - SIMPLE Y RÁPIDO
    // ========================================
    /**
     * CORREGIDO: Toggle ultra-rápido con carga INMEDIATA (como
     * NotificationBellComponent)
     */
    private void toggleTicketsPopup() {
        try {
            System.out.println("🎯 Toggle popup rápido - visible=" + isPopupVisible);

            if (ticketsPopup == null) {
                System.out.println("⚠️ Popup no existe, creando...");
                createTicketsPopupOnce();
            }

            if (ticketsPopup.isVisible()) {
                // OCULTAR: Ultra-rápido
                ticketsPopup.setVisible(false);
                System.out.println("📋 Popup ocultado inmediatamente");
            } else {
                // MOSTRAR: Cargar datos INMEDIATAMENTE y mostrar
                loadRecentTicketsSync(); // SINCRÓNICO - como NotificationBellComponent

                // Mostrar inmediatamente en posición correcta
                Point locationOnScreen = getLocationOnScreen();
                ticketsPopup.show(this, -380, getHeight() + 5);

                System.out.println("📋 Popup mostrado inmediatamente con datos");
            }

        } catch (Exception e) {
            System.err.println("❌ Error en toggle: " + e.getMessage());
            // Fallback: abrir ventana completa
            openTicketManagement();
        }
    }

    // ========================================
    // CREACIÓN DEL POPUP - UNA SOLA VEZ
    // ========================================
    /**
     * CLAVE: Crea el popup UNA SOLA VEZ y lo reutiliza siempre
     */
    private void createTicketsPopupOnce() {
        if (ticketsPopup != null) {
            System.out.println("✅ Popup ya existe, no recrear");
            return; // Ya existe, no recrear
        }

        try {
            System.out.println("🏗️ Creando popup UNA SOLA VEZ...");

            ticketsPopup = new JPopupMenu();
            ticketsPopup.setPreferredSize(new Dimension(450, 400));

            // Panel principal del popup
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            // === HEADER ===
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("🎫 Tickets Pendientes");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
            headerPanel.add(titleLabel, BorderLayout.WEST);

            // Botón actualizar
            JButton refreshBtn = new JButton("🔄");
            refreshBtn.setFont(new Font("Arial", Font.PLAIN, 11));
            refreshBtn.setToolTipText("Actualizar tickets");
            refreshBtn.addActionListener(e -> {
                refreshTicketCount();
                loadRecentTicketsSync(); // INMEDIATO para refresh manual
            });
            headerPanel.add(refreshBtn, BorderLayout.EAST);

            // === CONTENIDO DINÁMICO ===
            ticketsPanel = new JPanel();
            ticketsPanel.setLayout(new BoxLayout(ticketsPanel, BoxLayout.Y_AXIS));
            ticketsPanel.setBackground(Color.WHITE);

            scrollPane = new JScrollPane(ticketsPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            // === FOOTER ===
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton manageAllBtn = new JButton("🎫 Gestionar Todos los Tickets");
            manageAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
            manageAllBtn.setBackground(TICKET_BELL_COLOR);
            manageAllBtn.setForeground(Color.WHITE);
            manageAllBtn.addActionListener(e -> {
                ticketsPopup.setVisible(false);
                openTicketManagement();
            });
            footerPanel.add(manageAllBtn);

            // === ENSAMBLAR ===
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(footerPanel, BorderLayout.SOUTH);

            ticketsPopup.add(mainPanel);

            // Configurar listeners UNA SOLA VEZ
            setupPopupListeners();

            System.out.println("✅ Popup creado exitosamente UNA VEZ");

        } catch (Exception e) {
            System.err.println("❌ Error creando popup: " + e.getMessage());
            e.printStackTrace();
            ticketsPopup = null;
        }
    }

    /**
     * Configura listeners del popup UNA SOLA VEZ
     */
    private void setupPopupListeners() {
        if (ticketsPopup == null) {
            return;
        }

        ticketsPopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = true;
                if (bellIcon != null) {
                    bellIcon.setForeground(TICKET_ACTIVE_COLOR);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = false;
                if (bellIcon != null) {
                    bellIcon.setForeground(pendingTicketsCount > 0 ? TICKET_URGENT_COLOR : TICKET_BELL_COLOR);
                }
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = false;
                if (bellIcon != null) {
                    bellIcon.setForeground(pendingTicketsCount > 0 ? TICKET_URGENT_COLOR : TICKET_BELL_COLOR);
                }
            }
        });

        System.out.println("✅ Listeners del popup configurados");
    }

    // ========================================
    // CARGA DE DATOS - INMEDIATA Y EN BACKGROUND
    // ========================================
    /**
     * NUEVO: Carga datos INMEDIATAMENTE (como NotificationBellComponent)
     */
    private void loadRecentTicketsSync() {
        try {
            System.out.println("⚡ Cargando tickets INMEDIATAMENTE...");

            // Cargar datos directamente - SIN SwingWorker
            List<Ticket> allTickets = ticketService.obtenerTodosLosTickets();
            List<Ticket> recentTickets = allTickets.stream()
                    .filter(ticket -> "ABIERTO".equals(ticket.getEstado()) || "EN_REVISION".equals(ticket.getEstado()))
                    .sorted((t1, t2) -> {
                        int prioCompare = getPriorityOrder(t2.getPrioridad()) - getPriorityOrder(t1.getPrioridad());
                        if (prioCompare != 0) {
                            return prioCompare;
                        }
                        return t2.getFechaCreacion().compareTo(t1.getFechaCreacion());
                    })
                    .limit(MAX_POPUP_TICKETS)
                    .collect(java.util.stream.Collectors.toList());

            // Actualizar panel inmediatamente
            updateTicketsPanel(recentTickets);
            System.out.println("📊 " + recentTickets.size() + " tickets cargados INMEDIATAMENTE");

        } catch (Exception e) {
            System.err.println("Error cargando tickets inmediatamente: " + e.getMessage());
            showErrorPanel();
        }
    }

    /**
     * OPCIONAL: Carga datos en background (para refresh automático)
     */
    private void loadRecentTicketsAsync() {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<List<Ticket>, Void>() {
            @Override
            protected List<Ticket> doInBackground() throws Exception {
                List<Ticket> allTickets = ticketService.obtenerTodosLosTickets();
                return allTickets.stream()
                        .filter(ticket -> "ABIERTO".equals(ticket.getEstado()) || "EN_REVISION".equals(ticket.getEstado()))
                        .sorted((t1, t2) -> {
                            int prioCompare = getPriorityOrder(t2.getPrioridad()) - getPriorityOrder(t1.getPrioridad());
                            if (prioCompare != 0) {
                                return prioCompare;
                            }
                            return t2.getFechaCreacion().compareTo(t1.getFechaCreacion());
                        })
                        .limit(MAX_POPUP_TICKETS)
                        .collect(java.util.stream.Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<Ticket> tickets = get();
                    SwingUtilities.invokeLater(() -> {
                        updateTicketsPanel(tickets);
                        System.out.println("📊 " + tickets.size() + " tickets cargados en background");
                    });
                } catch (Exception e) {
                    System.err.println("Error cargando tickets: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> showErrorPanel());
                }
            }
        };
        worker.execute();
    }

    private int getPriorityOrder(String priority) {
        return switch (priority) {
            case "URGENTE" ->
                4;
            case "ALTA" ->
                3;
            case "NORMAL" ->
                2;
            case "BAJA" ->
                1;
            default ->
                0;
        };
    }

    /**
     * OPTIMIZADO: Solo actualiza el contenido del panel, no recrea nada
     */
    private void updateTicketsPanel(List<Ticket> tickets) {
        if (ticketsPanel == null) {
            return;
        }

        // Limpiar solo el contenido
        ticketsPanel.removeAll();

        if (tickets.isEmpty()) {
            showEmptyTicketsPanel();
        } else {
            for (int i = 0; i < tickets.size(); i++) {
                Ticket ticket = tickets.get(i);
                JPanel ticketItem = createTicketItem(ticket);
                ticketsPanel.add(ticketItem);

                if (i < tickets.size() - 1) {
                    ticketsPanel.add(Box.createVerticalStrut(5));
                }
            }

            // Indicador de más tickets
            if (pendingTicketsCount > MAX_POPUP_TICKETS) {
                JLabel moreLabel = new JLabel("... y " + (pendingTicketsCount - MAX_POPUP_TICKETS) + " más");
                moreLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                moreLabel.setForeground(Color.GRAY);
                moreLabel.setBorder(new EmptyBorder(5, 10, 5, 5));
                ticketsPanel.add(Box.createVerticalStrut(5));
                ticketsPanel.add(moreLabel);
            }
        }

        // Solo refrescar el contenido
        ticketsPanel.revalidate();
        ticketsPanel.repaint();
    }

    private JPanel createTicketItem(Ticket ticket) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
        itemPanel.setBorder(new EmptyBorder(12, 15, 12, 15));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        // Color de fondo según prioridad
        Color backgroundColor = switch (ticket.getPrioridad()) {
            case "URGENTE" ->
                new Color(255, 240, 240);
            case "ALTA" ->
                new Color(255, 248, 230);
            case "NORMAL" ->
                new Color(240, 255, 240);
            default ->
                Color.WHITE;
        };
        itemPanel.setBackground(backgroundColor);

        // Ícono de prioridad
        JLabel iconLabel = new JLabel(ticket.getIconoPrioridad());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        iconLabel.setPreferredSize(new Dimension(25, 60));

        // Contenido principal
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(2, 5, 2, 5));

        // Título
        String tituloTexto = ticket.getAsunto();
        if (tituloTexto == null || tituloTexto.trim().isEmpty()) {
            tituloTexto = "Sin asunto";
        }
        if (tituloTexto.length() > 35) {
            tituloTexto = tituloTexto.substring(0, 32) + "...";
        }

        JLabel titleLabel = new JLabel(tituloTexto);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Número del ticket
        JLabel numberLabel = new JLabel("🎫 " + ticket.getTicketNumber());
        numberLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        numberLabel.setForeground(new Color(100, 100, 100));
        numberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Usuario y tiempo
        String userInfo = ticket.getNombreCompleto();
        if (userInfo.length() > 25) {
            userInfo = userInfo.substring(0, 22) + "...";
        }

        String timeInfo = ticket.getTiempoTranscurrido();

        JLabel infoLabel = new JLabel("👤 " + userInfo + " • " + timeInfo);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Categoría
        JLabel categoriaLabel = new JLabel(ticket.getIconoCategoria() + " " + ticket.getCategoria());
        categoriaLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        categoriaLabel.setForeground(new Color(120, 120, 120));
        categoriaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(numberLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(categoriaLabel);
        contentPanel.add(Box.createVerticalGlue());

        // Botón de acción rápida
        JButton actionBtn = new JButton("⚡");
        actionBtn.setFont(new Font("Arial", Font.BOLD, 11));
        actionBtn.setPreferredSize(new Dimension(30, 30));
        actionBtn.setToolTipText("Abrir ticket en gestión");
        actionBtn.setBackground(TICKET_BELL_COLOR);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setBorderPainted(false);
        actionBtn.setFocusPainted(false);
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        actionBtn.addActionListener(e -> {
            ticketsPopup.setVisible(false);
            openTicketManagementWithSelection(ticket);
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(actionBtn, BorderLayout.NORTH);

        // Ensamblar
        itemPanel.add(iconLabel, BorderLayout.WEST);
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        itemPanel.add(rightPanel, BorderLayout.EAST);

        // Efectos hover
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = new Color(
                        Math.max(backgroundColor.getRed() - 15, 0),
                        Math.max(backgroundColor.getGreen() - 15, 0),
                        Math.max(backgroundColor.getBlue() - 15, 0)
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
                    ticketsPopup.setVisible(false);
                    openTicketManagementWithSelection(ticket);
                }
            }
        });

        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(3, 8, 3, 8),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));

        return itemPanel;
    }

    private void showEmptyTicketsPanel() {
        if (ticketsPanel == null) {
            return;
        }

        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setPreferredSize(new Dimension(400, 150));

        JLabel emptyIcon = new JLabel("✅", SwingConstants.CENTER);
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emptyIcon.setForeground(new Color(40, 167, 69));

        JLabel emptyText = new JLabel("¡Excelente! No hay tickets pendientes", SwingConstants.CENTER);
        emptyText.setFont(new Font("Arial", Font.ITALIC, 14));
        emptyText.setForeground(new Color(40, 167, 69));

        emptyPanel.add(emptyIcon, BorderLayout.CENTER);
        emptyPanel.add(emptyText, BorderLayout.SOUTH);

        ticketsPanel.add(emptyPanel);
    }

    private void showErrorPanel() {
        if (ticketsPanel == null) {
            return;
        }

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setPreferredSize(new Dimension(400, 150));

        JLabel errorIcon = new JLabel("⚠️", SwingConstants.CENTER);
        errorIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel errorText = new JLabel("Error cargando tickets", SwingConstants.CENTER);
        errorText.setFont(new Font("Arial", Font.ITALIC, 14));
        errorText.setForeground(Color.RED);

        errorPanel.add(errorIcon, BorderLayout.CENTER);
        errorPanel.add(errorText, BorderLayout.SOUTH);

        ticketsPanel.add(errorPanel);
    }

    // ========================================
    // MÉTODOS DE CONTADOR Y REFRESH
    // ========================================
    private void refreshTicketCountBackground() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ticketService.contarTicketsPendientes();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    updateTicketCount(count);
                    System.out.println("🔔 TicketBellComponent: " + count + " tickets pendientes");
                } catch (Exception e) {
                    System.err.println("Error carga inicial tickets: " + e.getMessage());
                    updateTicketCount(0);
                }
            }
        };
        worker.execute();
    }

    private void refreshTicketCount() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ticketService.contarTicketsPendientes();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    int previousCount = TicketBellComponent.this.pendingTicketsCount;
                    updateTicketCount(count);

                    if (count > previousCount && previousCount >= 0) {
                        animateNewTicket();
                    }
                } catch (Exception e) {
                    System.err.println("Error obteniendo contador: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateTicketCount(int count) {
        int previousCount = this.pendingTicketsCount;
        this.pendingTicketsCount = count;

        SwingUtilities.invokeLater(() -> {
            if (count > 0) {
                counterLabel.setText(count > 99 ? "99+" : String.valueOf(count));
                counterLabel.setVisible(true);
                bellIcon.setForeground(TICKET_URGENT_COLOR);

                if (count > previousCount && previousCount >= 0) {
                    animateNewTicket();
                }
            } else {
                counterLabel.setVisible(false);
                if (!isPopupVisible) {
                    bellIcon.setForeground(TICKET_BELL_COLOR);
                }
            }

            bellIcon.setToolTipText(count > 0
                    ? "🎫 " + count + " ticket(s) pendiente(s) - Centro de Ayuda"
                    : "🎫 Gestión de Tickets - Centro de Ayuda");

            revalidate();
            repaint();
        });
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================
    private void openTicketManagement() {
        if (ticketsPopup != null && ticketsPopup.isVisible()) {
            ticketsPopup.setVisible(false);
        }

        SwingUtilities.invokeLater(() -> {
            TicketManagementWindow window = new TicketManagementWindow(usuarioId);
            window.setVisible(true);
        });
    }

    private void openTicketManagementWithSelection(Ticket ticket) {
        if (ticketsPopup != null && ticketsPopup.isVisible()) {
            ticketsPopup.setVisible(false);
        }
        openTicketManagement();
    }

    private void startRefreshTimer() {
        refreshTimer = new Timer(45000, e -> refreshTicketCount());
        refreshTimer.start();
    }

    private void animateNewTicket() {
        if (animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            final int[] step = {0};
            final Font originalFont = bellIcon.getFont();
            final Color originalColor = bellIcon.getForeground();

            animationTimer = new Timer(200, null);
            animationTimer.addActionListener(e -> {
                if (step[0] < 6) {
                    if (step[0] % 2 == 0) {
                        bellIcon.setFont(originalFont.deriveFont(24f));
                        bellIcon.setForeground(TICKET_URGENT_COLOR.brighter());
                    } else {
                        bellIcon.setFont(originalFont);
                        bellIcon.setForeground(originalColor);
                    }
                    step[0]++;
                } else {
                    bellIcon.setFont(originalFont);
                    bellIcon.setForeground(pendingTicketsCount > 0 ? TICKET_URGENT_COLOR : TICKET_BELL_COLOR);
                    animationTimer.stop();
                }
                repaint();
            });
            animationTimer.start();
        });
    }

    /**
     * OPTIMIZADO: Método para forzar actualización
     */
    public void forceRefresh() {
        System.out.println("🔄 Forzando actualización optimizada...");

        SwingUtilities.invokeLater(() -> {
            try {
                // Solo actualizar contador
                refreshTicketCount();

                // Si el popup está visible, recargar su contenido INMEDIATAMENTE
                if (isPopupVisible && ticketsPopup != null && ticketsPopup.isVisible()) {
                    loadRecentTicketsSync(); // INMEDIATO para que se vea el cambio
                }

                animateNewTicket();
                System.out.println("✅ Refresh optimizado completado");

            } catch (Exception e) {
                System.err.println("Error en forceRefresh: " + e.getMessage());
            }
        });
    }

    /**
     * Limpia recursos
     */
    public void dispose() {
        try {
            System.out.println("🧹 Limpiando TicketBellComponent optimizado...");

            if (refreshTimer != null && refreshTimer.isRunning()) {
                refreshTimer.stop();
                refreshTimer = null;
            }

            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
                animationTimer = null;
            }

            if (ticketsPopup != null) {
                ticketsPopup.setVisible(false);
                ticketsPopup = null;
            }

            System.out.println("✅ TicketBellComponent limpiado");

        } catch (Exception e) {
            System.err.println("Error limpiando: " + e.getMessage());
        }
    }

    public int getPendingTicketsCount() {
        return pendingTicketsCount;
    }

    /**
     * Componente de fallback en caso de errores
     */
    private void createFallbackComponent() {
        try {
            removeAll();
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(false);

            JLabel errorIcon = new JLabel("⚠️");
            errorIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            errorIcon.setToolTipText("Error en sistema de tickets - Haz clic para gestión manual");
            errorIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

            errorIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openTicketManagement();
                }
            });

            add(errorIcon);
            setVisible(true);

            System.out.println("⚠️ Componente de fallback creado");

        } catch (Exception e) {
            System.err.println("Error crítico creando fallback: " + e.getMessage());
            setVisible(false);
        }
    }
}
