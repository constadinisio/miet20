package main.java.tickets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import main.java.tickets.Ticket;
import main.java.tickets.TicketService;

/**
 * Ventana para que los usuarios vean el seguimiento de sus tickets reportados
 * Permite ver el historial, estado actual y respuestas del desarrollador
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Centro de Ayuda para Usuarios
 */
public class UserTicketsWindow extends JFrame {

    private final int usuarioId;
    private final TicketService ticketService;

    // Componentes principales
    private JTable ticketsTable;
    private DefaultTableModel tableModel;
    private JTextField busquedaField;
    private JComboBox<String> estadoFilterCombo;
    private JComboBox<String> categoriaFilterCombo;

    // Panel de detalles
    private JTextArea detallesArea;
    private JButton verArchivoButton;
    private JButton nuevoTicketButton;
    private JButton actualizarButton;
    private JLabel estadisticasLabel;

    // Datos
    private List<Ticket> misTickets;
    private List<Ticket> ticketsFiltrados;
    private Ticket ticketSeleccionado;

    // Constantes de diseño
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Font TITULO_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);

    public UserTicketsWindow(int usuarioId) {
        this.usuarioId = usuarioId;
        this.ticketService = TicketService.getInstance();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadMyTickets();

        setTitle("🎫 Mis Tickets - Centro de Ayuda");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        System.out.println("✅ UserTicketsWindow inicializada para usuario: " + usuarioId);
    }

    private void initializeComponents() {
        // === FILTROS Y BÚSQUEDA ===
        busquedaField = new JTextField(20);
        busquedaField.setToolTipText("Buscar por número de ticket o asunto");

        String[] estadoOptions = {"Todos", "ABIERTO", "EN_REVISION", "RESUELTO", "CERRADO"};
        estadoFilterCombo = new JComboBox<>(estadoOptions);

        String[] categoriaOptions = {"Todas", "ERROR", "MEJORA", "CONSULTA", "SUGERENCIA"};
        categoriaFilterCombo = new JComboBox<>(categoriaOptions);

        // === TABLA DE TICKETS ===
        String[] columnNames = {"#", "Número", "Asunto", "Categoría", "Prioridad", "Estado", "Fecha", "Respuesta"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        ticketsTable = new JTable(tableModel);
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketsTable.setRowHeight(30);
        ticketsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar renderers de la tabla
        setupTableRenderers();

        // === PANEL DE DETALLES ===
        detallesArea = new JTextArea(12, 50);
        detallesArea.setEditable(false);
        detallesArea.setFont(LABEL_FONT);
        detallesArea.setLineWrap(true);
        detallesArea.setWrapStyleWord(true);
        detallesArea.setBackground(new Color(248, 249, 250));

        // === BOTONES ===
        verArchivoButton = createStyledButton("📎 Ver Archivo", INFO_COLOR);
        verArchivoButton.setEnabled(false);

        nuevoTicketButton = createStyledButton("🎫 Nuevo Ticket", SUCCESS_COLOR);
        actualizarButton = createStyledButton("🔄 Actualizar", PRIMARY_COLOR);

        // === ESTADÍSTICAS ===
        estadisticasLabel = new JLabel("Cargando mis tickets...");
        estadisticasLabel.setFont(new Font("Arial", Font.BOLD, 12));
        estadisticasLabel.setForeground(PRIMARY_COLOR);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // === PANEL PRINCIPAL ===
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // === HEADER ===
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // === CONTENIDO CENTRAL ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);

        // Panel izquierdo - Lista de mis tickets
        JPanel listPanel = createListPanel();
        splitPane.setLeftComponent(listPanel);

        // Panel derecho - Detalles del ticket seleccionado
        JPanel detailsPanel = createDetailsPanel();
        splitPane.setRightComponent(detailsPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // === FOOTER ===
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Título principal
        JLabel titleLabel = new JLabel("🎫 Mis Tickets - Centro de Ayuda");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // Subtítulo explicativo
        JLabel subtitleLabel = new JLabel("Aquí puedes ver el estado y seguimiento de todos tus reportes");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 255));

        // Panel de títulos
        JPanel titlesPanel = new JPanel(new BorderLayout());
        titlesPanel.setOpaque(false);
        titlesPanel.add(titleLabel, BorderLayout.NORTH);
        titlesPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Información del usuario
        JLabel userLabel = new JLabel("Usuario ID: " + usuarioId);
        userLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        userLabel.setForeground(Color.WHITE);

        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());

        // Panel de filtros
        JPanel filtersPanel = createFiltersPanel();
        listPanel.add(filtersPanel, BorderLayout.NORTH);

        // Tabla con scroll
        JScrollPane tableScrollPane = new JScrollPane(ticketsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "📋 Mis Tickets Reportados",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                TITULO_FONT
        ));

        listPanel.add(tableScrollPane, BorderLayout.CENTER);

        return listPanel;
    }

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        filtersPanel.setBorder(BorderFactory.createTitledBorder("🔍 Buscar y Filtrar"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 1: Búsqueda
        gbc.gridx = 0;
        gbc.gridy = 0;
        filtersPanel.add(new JLabel("Buscar:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filtersPanel.add(busquedaField, gbc);

        // Fila 2: Filtros
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        filtersPanel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        filtersPanel.add(estadoFilterCombo, gbc);

        gbc.gridx = 2;
        filtersPanel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 3;
        filtersPanel.add(categoriaFilterCombo, gbc);

        // Botón limpiar filtros
        gbc.gridx = 4;
        gbc.anchor = GridBagConstraints.EAST;
        JButton limpiarButton = createStyledButton("🧹 Limpiar", WARNING_COLOR);
        limpiarButton.addActionListener(this::limpiarFiltros);
        filtersPanel.add(limpiarButton, gbc);

        return filtersPanel;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout());

        // Panel de información del ticket
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "📄 Detalles del Ticket Seleccionado",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                TITULO_FONT
        ));

        JScrollPane detallesScrollPane = new JScrollPane(detallesArea);
        infoPanel.add(detallesScrollPane, BorderLayout.CENTER);

        // Panel de acciones del ticket
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.add(verArchivoButton);
        infoPanel.add(actionsPanel, BorderLayout.SOUTH);

        detailsPanel.add(infoPanel, BorderLayout.CENTER);

        // Panel de ayuda
        JPanel helpPanel = createHelpPanel();
        detailsPanel.add(helpPanel, BorderLayout.SOUTH);

        return detailsPanel;
    }

    private JPanel createHelpPanel() {
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "💡 Información de Estados",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12)
        ));

        JTextArea helpText = new JTextArea("""
            📝 Estados de Tickets:
            
            🔴 ABIERTO: Tu reporte fue recibido y está pendiente de revisión
            🟡 EN_REVISION: El desarrollador está trabajando en tu reporte
            🟢 RESUELTO: Tu reporte fue solucionado (revisa la respuesta)
            ⚫ CERRADO: El ticket fue cerrado (completado o descartado)
            
            💡 Tip: Haz doble clic en un ticket para ver todos los detalles
            """);

        helpText.setEditable(false);
        helpText.setBackground(new Color(248, 249, 250));
        helpText.setFont(new Font("Arial", Font.PLAIN, 11));
        helpText.setBorder(new EmptyBorder(10, 10, 10, 10));

        helpPanel.add(helpText, BorderLayout.CENTER);
        helpPanel.setPreferredSize(new Dimension(0, 150));

        return helpPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Panel izquierdo con estadísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(estadisticasLabel);

        // Panel derecho con botones principales
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.add(actualizarButton);
        buttonsPanel.add(nuevoTicketButton);

        footerPanel.add(statsPanel, BorderLayout.WEST);
        footerPanel.add(buttonsPanel, BorderLayout.EAST);

        return footerPanel;
    }

    private void setupListeners() {
        // Búsqueda en tiempo real
        busquedaField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltros();
            }
        });

        // Filtros
        estadoFilterCombo.addActionListener(e -> aplicarFiltros());
        categoriaFilterCombo.addActionListener(e -> aplicarFiltros());

        // Selección de ticket en tabla
        ticketsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetallesTicketSeleccionado();
            }
        });

        // Doble click en tabla para ver detalles completos
        ticketsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    mostrarDetallesCompletos();
                }
            }
        });

        // Botones
        verArchivoButton.addActionListener(this::abrirArchivo);
        nuevoTicketButton.addActionListener(this::abrirNuevoTicket);
        actualizarButton.addActionListener(e -> loadMyTickets());
    }

    private void setupTableRenderers() {
        // Renderer para estado con colores
        ticketsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && value != null) {
                    String estado = value.toString();
                    switch (estado) {
                        case "ABIERTO" ->
                            c.setBackground(new Color(255, 230, 230));
                        case "EN_REVISION" ->
                            c.setBackground(new Color(255, 250, 230));
                        case "RESUELTO" ->
                            c.setBackground(new Color(230, 255, 230));
                        case "CERRADO" ->
                            c.setBackground(new Color(240, 240, 240));
                        default ->
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        // Renderer para prioridad con colores
        ticketsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && value != null) {
                    String prioridad = value.toString();
                    switch (prioridad) {
                        case "URGENTE" ->
                            c.setBackground(new Color(255, 230, 230));
                        case "ALTA" ->
                            c.setBackground(new Color(255, 245, 230));
                        case "NORMAL" ->
                            c.setBackground(new Color(230, 255, 230));
                        case "BAJA" ->
                            c.setBackground(new Color(240, 240, 240));
                        default ->
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        // Renderer para columna de respuesta (Sí/No)
        ticketsTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value != null) {
                    String respuesta = value.toString();
                    if ("Sí".equals(respuesta)) {
                        setForeground(SUCCESS_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(Color.GRAY);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }

                return c;
            }
        });

        // Ajustar anchos de columnas
        int[] columnWidths = {40, 100, 200, 80, 80, 100, 120, 80};
        for (int i = 0; i < columnWidths.length && i < ticketsTable.getColumnCount(); i++) {
            ticketsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
    }

    private void loadMyTickets() {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<List<Ticket>, Void>() {
            @Override
            protected List<Ticket> doInBackground() throws Exception {
                return ticketService.obtenerTicketsUsuario(usuarioId);
            }

            @Override
            protected void done() {
                try {
                    misTickets = get();
                    aplicarFiltros();
                    actualizarEstadisticas();
                    System.out.println("✅ Mis tickets cargados: " + misTickets.size());
                } catch (Exception e) {
                    System.err.println("Error cargando mis tickets: " + e.getMessage());
                    JOptionPane.showMessageDialog(UserTicketsWindow.this,
                            "Error al cargar tus tickets: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void aplicarFiltros() {
        if (misTickets == null) {
            return;
        }

        String busqueda = busquedaField.getText().toLowerCase().trim();
        String estadoFiltro = (String) estadoFilterCombo.getSelectedItem();
        String categoriaFiltro = (String) categoriaFilterCombo.getSelectedItem();

        ticketsFiltrados = misTickets.stream()
                .filter(ticket -> {
                    // Filtro de búsqueda
                    if (!busqueda.isEmpty()) {
                        String searchableText = (ticket.getTicketNumber() + " " + ticket.getAsunto()).toLowerCase();
                        if (!searchableText.contains(busqueda)) {
                            return false;
                        }
                    }

                    // Filtro de estado
                    if (!"Todos".equals(estadoFiltro) && !estadoFiltro.equals(ticket.getEstado())) {
                        return false;
                    }

                    // Filtro de categoría
                    if (!"Todas".equals(categoriaFiltro) && !categoriaFiltro.equals(ticket.getCategoria())) {
                        return false;
                    }

                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        actualizarTabla();
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);

        for (int i = 0; i < ticketsFiltrados.size(); i++) {
            Ticket ticket = ticketsFiltrados.get(i);
            Object[] row = {
                i + 1,
                ticket.getTicketNumber(),
                ticket.getAsunto(),
                ticket.getCategoria(),
                ticket.getPrioridad(),
                ticket.getEstado(),
                ticket.getFechaCreacionFormateada(),
                ticket.tieneRespuesta() ? "Sí" : "No"
            };
            tableModel.addRow(row);
        }

        // Actualizar título de la tabla
        String titulo = String.format("📋 Mis Tickets Reportados (%d/%d)",
                ticketsFiltrados.size(), misTickets.size());
        // Actualizar el título del borde si es necesario

        repaint();
    }

    private void mostrarDetallesTicketSeleccionado() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < ticketsFiltrados.size()) {
            ticketSeleccionado = ticketsFiltrados.get(selectedRow);
            mostrarDetallesTicket(ticketSeleccionado);

            // Habilitar/deshabilitar botones
            verArchivoButton.setEnabled(ticketSeleccionado.tieneArchivo());
        } else {
            ticketSeleccionado = null;
            detallesArea.setText("Selecciona un ticket para ver los detalles.");
            verArchivoButton.setEnabled(false);
        }
    }

    private void mostrarDetallesTicket(Ticket ticket) {
        StringBuilder detalles = new StringBuilder();

        detalles.append("=== INFORMACIÓN DE TU TICKET ===\n\n");
        detalles.append("📋 Número: ").append(ticket.getTicketNumber()).append("\n");
        detalles.append("📝 Asunto: ").append(ticket.getAsunto()).append("\n");
        detalles.append("📂 Categoría: ").append(ticket.getIconoCategoria()).append(" ").append(ticket.getCategoria()).append("\n");
        detalles.append("⚡ Prioridad: ").append(ticket.getIconoPrioridad()).append(" ").append(ticket.getPrioridad()).append("\n");
        detalles.append("🔄 Estado: ").append(ticket.getEstado()).append("\n");
        detalles.append("📅 Reportado: ").append(ticket.getFechaCreacionFormateada()).append("\n");
        detalles.append("🕐 Última actualización: ").append(ticket.getFechaActualizacionFormateada()).append("\n");
        detalles.append("⏱️ Tiempo transcurrido: ").append(ticket.getTiempoTranscurrido()).append("\n");

        if (ticket.tieneArchivo()) {
            detalles.append("📎 Archivo adjunto: Disponible\n");
        }

        detalles.append("\n=== TU DESCRIPCIÓN ORIGINAL ===\n\n");
        detalles.append(ticket.getDescripcion());

        if (ticket.tieneRespuesta()) {
            detalles.append("\n\n=== 💬 RESPUESTA DEL DESARROLLADOR ===\n");
            detalles.append("📅 Respondido: ").append(ticket.getFechaRespuestaFormateada()).append("\n\n");
            detalles.append(ticket.getRespuestaDeveloper());
        } else {
            detalles.append("\n\n=== ⏳ ESPERANDO RESPUESTA ===\n");
            detalles.append("Tu ticket está siendo revisado por el equipo de desarrollo.\n");
            detalles.append("Recibirás una notificación cuando haya una actualización.");
        }

        detallesArea.setText(detalles.toString());
        detallesArea.setCaretPosition(0);
    }

    private void mostrarDetallesCompletos() {
        if (ticketSeleccionado == null) {
            return;
        }

        JDialog detallesDialog = new JDialog(this, "Detalles Completos - " + ticketSeleccionado.getTicketNumber(), true);
        detallesDialog.setSize(800, 600);
        detallesDialog.setLocationRelativeTo(this);

        JTextArea detallesCompletos = new JTextArea();
        detallesCompletos.setEditable(false);
        detallesCompletos.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detallesCompletos.setText(ticketSeleccionado.getDebugInfo());

        JScrollPane scrollPane = new JScrollPane(detallesCompletos);
        detallesDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton cerrarButton = new JButton("Cerrar");
        cerrarButton.addActionListener(e -> detallesDialog.dispose());
        buttonPanel.add(cerrarButton);

        detallesDialog.add(buttonPanel, BorderLayout.SOUTH);
        detallesDialog.setVisible(true);
    }

    private void abrirArchivo(ActionEvent e) {
        if (ticketSeleccionado == null || !ticketSeleccionado.tieneArchivo()) {
            return;
        }

        try {
            File archivo = new File(ticketSeleccionado.getArchivoAdjuntoUrl());
            if (archivo.exists()) {
                Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(this,
                        "El archivo adjunto no se encontró en el sistema.\nRuta: "
                        + ticketSeleccionado.getArchivoAdjuntoUrl(),
                        "Archivo no encontrado", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirNuevoTicket(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            TicketReportWindow reportWindow = new TicketReportWindow(usuarioId);
            reportWindow.setVisible(true);
        });
    }

    private void limpiarFiltros(ActionEvent e) {
        busquedaField.setText("");
        estadoFilterCombo.setSelectedIndex(0);
        categoriaFilterCombo.setSelectedIndex(0);
        aplicarFiltros();
    }

    private void actualizarEstadisticas() {
        if (misTickets == null) {
            estadisticasLabel.setText("Sin datos");
            return;
        }

        long abiertos = misTickets.stream().filter(t -> "ABIERTO".equals(t.getEstado())).count();
        long enRevision = misTickets.stream().filter(t -> "EN_REVISION".equals(t.getEstado())).count();
        long resueltos = misTickets.stream().filter(t -> "RESUELTO".equals(t.getEstado())).count();
        long cerrados = misTickets.stream().filter(t -> "CERRADO".equals(t.getEstado())).count();

        String estadisticas = String.format(
                "📊 Total: %d | 🔴 Abiertos: %d | 🟡 En revisión: %d | 🟢 Resueltos: %d | ⚫ Cerrados: %d",
                misTickets.size(), abiertos, enRevision, resueltos, cerrados
        );

        estadisticasLabel.setText(estadisticas);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = button.getBackground();

            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(originalColor.brighter());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    /**
     * Método público para actualizar la lista de tickets (útil para llamar
     * desde otras ventanas)
     */
    public void refreshTickets() {
        loadMyTickets();
    }

    /**
     * Método para obtener estadísticas rápidas de los tickets del usuario
     */
    public String getQuickStats() {
        if (misTickets == null || misTickets.isEmpty()) {
            return "No tienes tickets reportados";
        }

        long pendientes = misTickets.stream()
                .filter(t -> "ABIERTO".equals(t.getEstado()) || "EN_REVISION".equals(t.getEstado()))
                .count();
        long resueltos = misTickets.stream()
                .filter(t -> "RESUELTO".equals(t.getEstado()) || "CERRADO".equals(t.getEstado()))
                .count();

        return String.format("Tienes %d ticket(s) pendiente(s) y %d resuelto(s)", pendientes, resueltos);
    }

    @Override
    public void dispose() {
        try {
            System.out.println("🧹 Cerrando UserTicketsWindow");
            super.dispose();
        } catch (Exception e) {
            System.err.println("Error cerrando ventana: " + e.getMessage());
        }
    }
}
