package main.java.tickets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;
import main.java.tickets.Ticket;
import main.java.tickets.TicketService;
import main.java.tickets.UserTicketsWindow;

/**
 * Componente que muestra un resumen rápido de los tickets del usuario Se puede
 * integrar en la ventana principal para dar visibilidad al estado de tickets
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Centro de Ayuda
 */
public class TicketsSummaryComponent extends JPanel {

    private final int usuarioId;
    private final TicketService ticketService;

    // Componentes UI
    private JLabel statusLabel;
    private JLabel countLabel;
    private JButton viewDetailsButton;

    // Datos
    private List<Ticket> userTickets;
    private Timer refreshTimer;

    // Constantes de diseño
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 12);

    public TicketsSummaryComponent(int usuarioId) {
        this.usuarioId = usuarioId;
        this.ticketService = TicketService.getInstance();

        initializeComponents();
        setupLayout();
        setupListeners();
        startRefreshTimer();

        // Cargar datos inicial
        refreshTicketData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Etiqueta de estado
        statusLabel = new JLabel("🎫 Mis Tickets");
        statusLabel.setFont(TITLE_FONT);
        statusLabel.setForeground(PRIMARY_COLOR);

        // Contador de tickets
        countLabel = new JLabel("Cargando...");
        countLabel.setFont(LABEL_FONT);
        countLabel.setForeground(Color.GRAY);

        // Botón para ver detalles
        viewDetailsButton = new JButton("Ver Todos");
        viewDetailsButton.setFont(new Font("Arial", Font.PLAIN, 11));
        viewDetailsButton.setBackground(PRIMARY_COLOR);
        viewDetailsButton.setForeground(Color.WHITE);
        viewDetailsButton.setBorderPainted(false);
        viewDetailsButton.setFocusPainted(false);
        viewDetailsButton.setPreferredSize(new Dimension(80, 25));
        viewDetailsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupLayout() {
        // Panel superior con título
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(viewDetailsButton, BorderLayout.EAST);

        // Panel central con información
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        centerPanel.add(countLabel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(250, 60));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
    }

    private void setupListeners() {
        // Click en el botón de ver detalles
        viewDetailsButton.addActionListener(this::openTicketsWindow);

        // Click en todo el panel
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openTicketsWindow(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(248, 249, 250));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }
        });

        // Hover effect en botón
        viewDetailsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                viewDetailsButton.setBackground(PRIMARY_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                viewDetailsButton.setBackground(PRIMARY_COLOR);
            }
        });
    }

    private void startRefreshTimer() {
        // Refrescar cada 2 minutos
        refreshTimer = new Timer(120000, e -> refreshTicketData());
        refreshTimer.start();
    }

    private void refreshTicketData() {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<List<Ticket>, Void>() {
            @Override
            protected List<Ticket> doInBackground() throws Exception {
                return ticketService.obtenerTicketsUsuario(usuarioId);
            }

            @Override
            protected void done() {
                try {
                    userTickets = get();
                    updateDisplay();
                } catch (Exception e) {
                    System.err.println("Error cargando tickets para resumen: " + e.getMessage());
                    showErrorState();
                }
            }
        };
        worker.execute();
    }

    private void updateDisplay() {
        if (userTickets == null || userTickets.isEmpty()) {
            showNoTicketsState();
            return;
        }

        long pendientes = userTickets.stream()
                .filter(t -> "ABIERTO".equals(t.getEstado()) || "EN_REVISION".equals(t.getEstado()))
                .count();

        long resueltos = userTickets.stream()
                .filter(t -> "RESUELTO".equals(t.getEstado()) || "CERRADO".equals(t.getEstado()))
                .count();

        // Actualizar etiquetas según el estado
        if (pendientes > 0) {
            statusLabel.setText("🎫 Tickets Pendientes");
            statusLabel.setForeground(WARNING_COLOR);

            String countText = String.format("%d pendiente%s, %d total",
                    pendientes, pendientes == 1 ? "" : "s", userTickets.size());
            countLabel.setText(countText);
            countLabel.setForeground(WARNING_COLOR);

            // Cambiar color del borde para llamar la atención
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(WARNING_COLOR, 1),
                    new EmptyBorder(10, 10, 10, 10)
            ));

        } else if (resueltos > 0) {
            statusLabel.setText("🎫 Tickets Resueltos");
            statusLabel.setForeground(SUCCESS_COLOR);

            String countText = String.format("Todos resueltos (%d total)", userTickets.size());
            countLabel.setText(countText);
            countLabel.setForeground(SUCCESS_COLOR);

        } else {
            showNoTicketsState();
        }

        // Actualizar tooltip
        setToolTipText(String.format("Tienes %d ticket(s): %d pendiente(s), %d resuelto(s)",
                userTickets.size(), pendientes, resueltos));
    }

    private void showNoTicketsState() {
        statusLabel.setText("🎫 Sin Tickets");
        statusLabel.setForeground(Color.GRAY);
        countLabel.setText("No has reportado ningún ticket");
        countLabel.setForeground(Color.GRAY);
        setToolTipText("No tienes tickets reportados");
    }

    private void showErrorState() {
        statusLabel.setText("🎫 Error");
        statusLabel.setForeground(DANGER_COLOR);
        countLabel.setText("Error cargando tickets");
        countLabel.setForeground(DANGER_COLOR);
        setToolTipText("Error al cargar tus tickets");
    }

    private void openTicketsWindow(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            try {
                UserTicketsWindow ticketsWindow = new UserTicketsWindow(usuarioId);
                ticketsWindow.setVisible(true);
            } catch (Exception ex) {
                System.err.println("Error abriendo ventana de tickets: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Error al abrir la ventana de tickets: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * Fuerza una actualización de los datos
     */
    public void forceRefresh() {
        refreshTicketData();
    }

    /**
     * Obtiene el número de tickets pendientes del usuario
     */
    public int getPendingTicketsCount() {
        if (userTickets == null) {
            return 0;
        }

        return (int) userTickets.stream()
                .filter(t -> "ABIERTO".equals(t.getEstado()) || "EN_REVISION".equals(t.getEstado()))
                .count();
    }

    /**
     * Verifica si el usuario tiene tickets pendientes
     */
    public boolean hasPendingTickets() {
        return getPendingTicketsCount() > 0;
    }

    /**
     * Obtiene el total de tickets del usuario
     */
    public int getTotalTicketsCount() {
        return userTickets != null ? userTickets.size() : 0;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            refreshTicketData();
        }
    }

    /**
     * Limpia recursos al cerrar
     */
    public void dispose() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }
}
