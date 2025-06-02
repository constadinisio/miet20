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
 * Muestra solo notificaciones relacionadas con tickets del sistema
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Centro de Ayuda
 */
public class TicketBellComponent extends JPanel {
    
    private final int usuarioId;
    private final TicketService ticketService;
    
    // Componentes UI
    private JLabel bellIcon;
    private JLabel counterLabel;
    private JPopupMenu ticketsPopup;
    private JPanel ticketsPanel;
    private JScrollPane scrollPane;
    
    // Estado
    private int pendingTicketsCount = 0;
    private boolean isPopupVisible = false;
    private Timer refreshTimer;
    private Timer animationTimer;
    
    // Constantes de diseño
    private static final Color TICKET_BELL_COLOR = new Color(255, 111, 97); // Naranja-rojizo para distinguir
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
        
        initializeComponents();
        setupLayout();
        setupListeners();
        startRefreshTimer();
        
        // Cargar contador inicial
        refreshTicketCount();
    }
    
    private void initializeComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Ícono de ticket (diferente a la campanita normal)
        bellIcon = new JLabel("🎫");
        bellIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bellIcon.setForeground(TICKET_BELL_COLOR);
        bellIcon.setToolTipText("Gestión de Tickets - Centro de Ayuda");
        
        // Contador de tickets pendientes
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
        
        // Popup de tickets
        createTicketsPopup();
    }
    
    private void setupLayout() {
        add(bellIcon);
        add(counterLabel);
        
        setPreferredSize(new Dimension(60, 30));
        setMinimumSize(new Dimension(60, 30));
    }
    
    private void setupListeners() {
        // Click en la campanita de tickets
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    toggleTicketsPopup();
                } else if (e.getClickCount() == 2) {
                    // Doble click abre ventana completa de gestión
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
        
        // Listener para cerrar popup
        ticketsPopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = true;
                bellIcon.setForeground(TICKET_ACTIVE_COLOR);
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = false;
                bellIcon.setForeground(pendingTicketsCount > 0 ? TICKET_URGENT_COLOR : TICKET_BELL_COLOR);
            }
            
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                isPopupVisible = false;
                bellIcon.setForeground(pendingTicketsCount > 0 ? TICKET_URGENT_COLOR : TICKET_BELL_COLOR);
            }
        });
    }
    
    private void createTicketsPopup() {
        ticketsPopup = new JPopupMenu();
        ticketsPopup.setPreferredSize(new Dimension(450, 400));
        
        // Panel principal del popup
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header del popup
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("🎫 Tickets Pendientes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Botón actualizar tickets
        JButton refreshBtn = new JButton("🔄");
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        refreshBtn.setToolTipText("Actualizar tickets");
        refreshBtn.addActionListener(e -> refreshTicketCount());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Panel de tickets con scroll
        ticketsPanel = new JPanel();
        ticketsPanel.setLayout(new BoxLayout(ticketsPanel, BoxLayout.Y_AXIS));
        ticketsPanel.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(ticketsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Footer con botón "Gestionar Todos"
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton manageAllBtn = new JButton("🎫 Gestionar Todos los Tickets");
        manageAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
        manageAllBtn.setBackground(TICKET_BELL_COLOR);
        manageAllBtn.setForeground(Color.WHITE);
        manageAllBtn.addActionListener(e -> openTicketManagement());
        footerPanel.add(manageAllBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        ticketsPopup.add(mainPanel);
    }
    
    private void toggleTicketsPopup() {
        if (ticketsPopup.isVisible()) {
            ticketsPopup.setVisible(false);
        } else {
            loadRecentTickets();
            // Mostrar popup debajo de la campanita
            Point locationOnScreen = getLocationOnScreen();
            ticketsPopup.show(this, -380, getHeight() + 5);
        }
    }
    
    private void loadRecentTickets() {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<List<Ticket>, Void>() {
            @Override
            protected List<Ticket> doInBackground() throws Exception {
                // Obtener solo tickets abiertos y en revisión
                List<Ticket> allTickets = ticketService.obtenerTodosLosTickets();
                return allTickets.stream()
                        .filter(ticket -> "ABIERTO".equals(ticket.getEstado()) || "EN_REVISION".equals(ticket.getEstado()))
                        .sorted((t1, t2) -> {
                            // Ordenar por prioridad y fecha
                            int prioCompare = getPriorityOrder(t2.getPrioridad()) - getPriorityOrder(t1.getPrioridad());
                            if (prioCompare != 0) return prioCompare;
                            return t2.getFechaCreacion().compareTo(t1.getFechaCreacion());
                        })
                        .limit(MAX_POPUP_TICKETS)
                        .collect(java.util.stream.Collectors.toList());
            }
            
            @Override
            protected void done() {
                try {
                    List<Ticket> tickets = get();
                    updateTicketsPanel(tickets);
                } catch (Exception e) {
                    System.err.println("Error cargando tickets: " + e.getMessage());
                    showErrorPanel();
                }
            }
        };
        worker.execute();
    }
    
    private int getPriorityOrder(String priority) {
        return switch (priority) {
            case "URGENTE" -> 4;
            case "ALTA" -> 3;
            case "NORMAL" -> 2;
            case "BAJA" -> 1;
            default -> 0;
        };
    }
    
    private void updateTicketsPanel(List<Ticket> tickets) {
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
            
            // Si hay más tickets, mostrar indicador
            if (pendingTicketsCount > MAX_POPUP_TICKETS) {
                JLabel moreLabel = new JLabel("... y " + (pendingTicketsCount - MAX_POPUP_TICKETS) + " más");
                moreLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                moreLabel.setForeground(Color.GRAY);
                moreLabel.setBorder(new EmptyBorder(5, 10, 5, 5));
                ticketsPanel.add(Box.createVerticalStrut(5));
                ticketsPanel.add(moreLabel);
            }
        }
        
        ticketsPanel.revalidate();
        ticketsPanel.repaint();
    }
    
    private JPanel createTicketItem(Ticket ticket) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
        itemPanel.setBorder(new EmptyBorder(12, 15, 12, 15));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        
        // Color de fondo según prioridad
        Color backgroundColor = switch (ticket.getPrioridad()) {
            case "URGENTE" -> new Color(255, 240, 240);
            case "ALTA" -> new Color(255, 248, 230);
            case "NORMAL" -> new Color(240, 255, 240);
            default -> Color.WHITE;
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
        
        // Título del ticket
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
        
        // Categoria
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
        
        // Panel derecho para el botón
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(actionBtn, BorderLayout.NORTH);
        
        // Ensamblar panel principal
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
        
        // Borde sutil
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(3, 8, 3, 8),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));
        
        return itemPanel;
    }
    
    private void showEmptyTicketsPanel() {
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
                    updateTicketCount(count);
                } catch (Exception e) {
                    System.err.println("Error obteniendo contador de tickets: " + e.getMessage());
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
                
                // Animar si hay nuevos tickets
                if (count > previousCount && previousCount >= 0) {
                    animateNewTicket();
                }
            } else {
                counterLabel.setVisible(false);
                if (!isPopupVisible) {
                    bellIcon.setForeground(TICKET_BELL_COLOR);
                }
            }
            
            // Actualizar tooltip
            bellIcon.setToolTipText(count > 0 ? 
                    "🎫 " + count + " ticket(s) pendiente(s) - Centro de Ayuda" : 
                    "🎫 Gestión de Tickets - Centro de Ayuda");
            
            revalidate();
            repaint();
        });
    }
    
    private void openTicketManagement() {
        ticketsPopup.setVisible(false);
        
        SwingUtilities.invokeLater(() -> {
            TicketManagementWindow window = new TicketManagementWindow(usuarioId);
            window.setVisible(true);
        });
    }
    
    private void openTicketManagementWithSelection(Ticket ticket) {
        // Por ahora abre la ventana normal, en una versión futura se podría
        // implementar selección automática del ticket
        openTicketManagement();
    }
    
    private void startRefreshTimer() {
        // Refrescar contador cada 45 segundos (diferente al normal para distinguir)
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
                    // Efecto de pulso más llamativo para tickets
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
     * Método para forzar actualización (útil cuando se actualiza un ticket)
     */
    public void forceRefresh() {
        refreshTicketCount();
    }
    
    /**
     * Limpia recursos al cerrar
     */
    public void dispose() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
    
    /**
     * Getter para pruebas
     */
    public int getPendingTicketsCount() {
        return pendingTicketsCount;
    }
}