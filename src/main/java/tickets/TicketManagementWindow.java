package main.java.tickets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import main.java.tickets.Ticket;
import main.java.tickets.TicketService;

/**
 * Ventana de gestión de tickets para desarrolladores Permite ver, responder y
 * gestionar todos los tickets del sistema
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Centro de Ayuda para Desarrolladores
 */
public class TicketManagementWindow extends JFrame {

    private final int usuarioId;
    private final TicketService ticketService;

    // Componentes principales
    private JTable ticketsTable;
    private DefaultTableModel tableModel;
    private JTextField busquedaField;
    private JComboBox<String> estadoFilterCombo;
    private JComboBox<String> categoriaFilterCombo;
    private JComboBox<String> prioridadFilterCombo;

    // Panel de detalles
    private JTextArea detallesArea;
    private JTextArea respuestaArea;
    private JComboBox<String> nuevoEstadoCombo;
    private JButton actualizarButton;
    private JButton archivoButton;
    private JLabel estadisticasLabel;

    // Datos
    private List<Ticket> todosLosTickets;
    private List<Ticket> ticketsFiltrados;
    private Ticket ticketSeleccionado;

    // Constantes de diseño
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Font TITULO_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);

    public TicketManagementWindow(int usuarioId) {
        this.usuarioId = usuarioId;
        this.ticketService = TicketService.getInstance();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadTickets();

        setTitle("🎫 Gestión de Tickets - Centro de Ayuda Desarrollador");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        System.out.println("✅ TicketManagementWindow inicializada para desarrollador: " + usuarioId);
    }

    private void initializeComponents() {
        // === FILTROS Y BÚSQUEDA ===
        busquedaField = new JTextField(20);
        busquedaField.setToolTipText("Buscar por número, asunto o usuario");

        String[] estadoOptions = {"Todos", "ABIERTO", "EN_REVISION", "RESUELTO", "CERRADO"};
        estadoFilterCombo = new JComboBox<>(estadoOptions);

        String[] categoriaOptions = {"Todas", "ERROR", "MEJORA", "CONSULTA", "SUGERENCIA"};
        categoriaFilterCombo = new JComboBox<>(categoriaOptions);

        String[] prioridadOptions = {"Todas", "URGENTE", "ALTA", "NORMAL", "BAJA"};
        prioridadFilterCombo = new JComboBox<>(prioridadOptions);

        // === TABLA DE TICKETS ===
        String[] columnNames = {"#", "Número", "Asunto", "Usuario", "Categoría", "Prioridad", "Estado", "Fecha", "Edad"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        ticketsTable = new JTable(tableModel);
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketsTable.setRowHeight(25);
        ticketsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar renderers de la tabla
        setupTableRenderers();

        // === PANEL DE DETALLES ===
        detallesArea = new JTextArea(8, 40);
        detallesArea.setEditable(false);
        detallesArea.setFont(LABEL_FONT);
        detallesArea.setLineWrap(true);
        detallesArea.setWrapStyleWord(true);
        detallesArea.setBackground(new Color(248, 249, 250));

        respuestaArea = new JTextArea(6, 40);
        respuestaArea.setFont(LABEL_FONT);
        respuestaArea.setLineWrap(true);
        respuestaArea.setWrapStyleWord(true);
        respuestaArea.setToolTipText("Escribe tu respuesta al usuario");

        String[] estadosResponse = {"ABIERTO", "EN_REVISION", "RESUELTO", "CERRADO"};
        nuevoEstadoCombo = new JComboBox<>(estadosResponse);
        nuevoEstadoCombo.setFont(LABEL_FONT);

        // === BOTONES ===
        actualizarButton = createStyledButton("💾 Actualizar Ticket", SUCCESS_COLOR);
        actualizarButton.setEnabled(false);

        archivoButton = createStyledButton("📎 Ver Archivo", new Color(108, 117, 125));
        archivoButton.setEnabled(false);

        // === ESTADÍSTICAS ===
        estadisticasLabel = new JLabel("Cargando estadísticas...");
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
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.6);

        // Panel izquierdo - Lista de tickets
        JPanel listPanel = createListPanel();
        splitPane.setLeftComponent(listPanel);

        // Panel derecho - Detalles y respuesta
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
        JLabel titleLabel = new JLabel("🎫 Centro de Ayuda - Gestión de Tickets");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // Información del desarrollador
        JLabel developerLabel = new JLabel("Desarrollador: Usuario ID " + usuarioId);
        developerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        developerLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(developerLabel, BorderLayout.EAST);

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
                "📋 Lista de Tickets",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                TITULO_FONT
        ));

        listPanel.add(tableScrollPane, BorderLayout.CENTER);

        return listPanel;
    }

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        filtersPanel.setBorder(BorderFactory.createTitledBorder("🔍 Filtros y Búsqueda"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 1: Búsqueda
        gbc.gridx = 0;
        gbc.gridy = 0;
        filtersPanel.add(new JLabel("Buscar:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
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

        // Fila 3: Más filtros y botones
        gbc.gridx = 0;
        gbc.gridy = 2;
        filtersPanel.add(new JLabel("Prioridad:"), gbc);
        gbc.gridx = 1;
        filtersPanel.add(prioridadFilterCombo, gbc);

        gbc.gridx = 2;
        JButton limpiarFiltrosButton = createStyledButton("🔄 Limpiar", WARNING_COLOR);
        limpiarFiltrosButton.addActionListener(this::limpiarFiltros);
        filtersPanel.add(limpiarFiltrosButton, gbc);

        gbc.gridx = 3;
        JButton actualizarListaButton = createStyledButton("↻ Actualizar", PRIMARY_COLOR);
        actualizarListaButton.addActionListener(e -> loadTickets());
        filtersPanel.add(actualizarListaButton, gbc);

        return filtersPanel;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout());

        // Panel de información del ticket
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "📄 Detalles del Ticket",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                TITULO_FONT
        ));

        JScrollPane detallesScrollPane = new JScrollPane(detallesArea);
        infoPanel.add(detallesScrollPane, BorderLayout.CENTER);

        // Panel de archivo adjunto
        JPanel archivoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        archivoPanel.add(archivoButton);
        infoPanel.add(archivoPanel, BorderLayout.SOUTH);

        detailsPanel.add(infoPanel, BorderLayout.CENTER);

        // Panel de respuesta
        JPanel responsePanel = createResponsePanel();
        detailsPanel.add(responsePanel, BorderLayout.SOUTH);

        return detailsPanel;
    }

    private JPanel createResponsePanel() {
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "💬 Responder al Ticket",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                TITULO_FONT
        ));

        // Área de respuesta
        JScrollPane respuestaScrollPane = new JScrollPane(respuestaArea);
        responsePanel.add(respuestaScrollPane, BorderLayout.CENTER);

        // Panel de controles
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        controlsPanel.add(new JLabel("Nuevo Estado:"), gbc);
        gbc.gridx = 1;
        controlsPanel.add(nuevoEstadoCombo, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        controlsPanel.add(actualizarButton, gbc);

        responsePanel.add(controlsPanel, BorderLayout.SOUTH);

        return responsePanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Panel izquierdo - Estadísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(estadisticasLabel);

        // Panel derecho - Acciones rápidas
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton exportarButton = createStyledButton("📊 Exportar", new Color(108, 117, 125));
        exportarButton.addActionListener(this::exportarTickets);

        JButton configuracionButton = createStyledButton("⚙️ Configuración", new Color(108, 117, 125));
        configuracionButton.addActionListener(this::abrirConfiguracion);

        actionsPanel.add(exportarButton);
        actionsPanel.add(configuracionButton);

        footerPanel.add(statsPanel, BorderLayout.WEST);
        footerPanel.add(actionsPanel, BorderLayout.EAST);

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
        prioridadFilterCombo.addActionListener(e -> aplicarFiltros());

        // Selección de ticket en tabla
        ticketsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetallesTicketSeleccionado();
            }
        });

        // Doble click en tabla para ver detalles
        ticketsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    mostrarDetallesCompletos();
                }
            }
        });

        // Botón actualizar ticket
        actualizarButton.addActionListener(this::actualizarTicket);

        // Botón ver archivo
        archivoButton.addActionListener(this::abrirArchivo);
    }

    private void setupTableRenderers() {
        // Renderer para prioridad con colores
        ticketsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && value != null) {
                    String prioridad = value.toString();
                    switch (prioridad) {
                        case "URGENTE":
                            c.setBackground(new Color(255, 230, 230));
                            break;
                        case "ALTA":
                            c.setBackground(new Color(255, 245, 230));
                            break;
                        case "NORMAL":
                            c.setBackground(new Color(230, 255, 230));
                            break;
                        case "BAJA":
                            c.setBackground(new Color(240, 240, 240));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        // Renderer para estado con colores
        ticketsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && value != null) {
                    String estado = value.toString();
                    switch (estado) {
                        case "ABIERTO":
                            c.setBackground(new Color(255, 230, 230));
                            break;
                        case "EN_REVISION":
                            c.setBackground(new Color(255, 250, 230));
                            break;
                        case "RESUELTO":
                            c.setBackground(new Color(230, 255, 230));
                            break;
                        case "CERRADO":
                            c.setBackground(new Color(240, 240, 240));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        // Ajustar anchos de columnas
        int[] columnWidths = {50, 100, 200, 120, 80, 80, 100, 120, 60};
        for (int i = 0; i < columnWidths.length && i < ticketsTable.getColumnCount(); i++) {
            ticketsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
    }

    private void loadTickets() {
        SwingWorker<List<Ticket>, Void> worker = new SwingWorker<List<Ticket>, Void>() {
            @Override
            protected List<Ticket> doInBackground() throws Exception {
                return ticketService.obtenerTodosLosTickets();
            }

            @Override
            protected void done() {
                try {
                    todosLosTickets = get();
                    aplicarFiltros();
                    actualizarEstadisticas();
                    System.out.println("✅ Tickets cargados: " + todosLosTickets.size());
                } catch (Exception e) {
                    System.err.println("Error cargando tickets: " + e.getMessage());
                    JOptionPane.showMessageDialog(TicketManagementWindow.this,
                            "Error al cargar los tickets: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void aplicarFiltros() {
        if (todosLosTickets == null) {
            return;
        }

        String busqueda = busquedaField.getText().toLowerCase().trim();
        String estadoFiltro = (String) estadoFilterCombo.getSelectedItem();
        String categoriaFiltro = (String) categoriaFilterCombo.getSelectedItem();
        String prioridadFiltro = (String) prioridadFilterCombo.getSelectedItem();

        ticketsFiltrados = todosLosTickets.stream()
                .filter(ticket -> {
                    // Filtro de búsqueda
                    if (!busqueda.isEmpty()) {
                        String searchableText = (ticket.getTicketNumber() + " "
                                + ticket.getAsunto() + " "
                                + ticket.getNombreCompleto()).toLowerCase();
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

                    // Filtro de prioridad
                    if (!"Todas".equals(prioridadFiltro) && !prioridadFiltro.equals(ticket.getPrioridad())) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

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
                ticket.getNombreCompleto(),
                ticket.getCategoria(),
                ticket.getPrioridad(),
                ticket.getEstado(),
                ticket.getFechaCreacionFormateada(),
                ticket.getTiempoTranscurrido()
            };
            tableModel.addRow(row);
        }

        // Actualizar título de la tabla
        String titulo = String.format("📋 Lista de Tickets (%d/%d)",
                ticketsFiltrados.size(), todosLosTickets.size());
        ((TitledBorder) ((JScrollPane) ticketsTable.getParent().getParent()).getBorder()).setTitle(titulo);

        repaint();
    }

    private void mostrarDetallesTicketSeleccionado() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < ticketsFiltrados.size()) {
            ticketSeleccionado = ticketsFiltrados.get(selectedRow);
            mostrarDetallesTicket(ticketSeleccionado);

            // Habilitar/deshabilitar controles
            actualizarButton.setEnabled(true);
            archivoButton.setEnabled(ticketSeleccionado.tieneArchivo());

            // Configurar estado actual en combo
            nuevoEstadoCombo.setSelectedItem(ticketSeleccionado.getEstado());

            // Mostrar respuesta existente si la hay
            if (ticketSeleccionado.tieneRespuesta()) {
                respuestaArea.setText(ticketSeleccionado.getRespuestaDeveloper());
            } else {
                respuestaArea.setText("");
            }
        } else {
            ticketSeleccionado = null;
            detallesArea.setText("Selecciona un ticket para ver los detalles.");
            respuestaArea.setText("");
            actualizarButton.setEnabled(false);
            archivoButton.setEnabled(false);
        }
    }

    private void mostrarDetallesTicket(Ticket ticket) {
        StringBuilder detalles = new StringBuilder();

        detalles.append("=== INFORMACIÓN DEL TICKET ===\n\n");
        detalles.append("📋 Número: ").append(ticket.getTicketNumber()).append("\n");
        detalles.append("📝 Asunto: ").append(ticket.getAsunto()).append("\n");
        detalles.append("👤 Usuario: ").append(ticket.getNombreCompleto()).append(" (ID: ").append(ticket.getUsuarioReportaId()).append(")\n");
        detalles.append("📂 Categoría: ").append(ticket.getIconoCategoria()).append(" ").append(ticket.getCategoria()).append("\n");
        detalles.append("⚡ Prioridad: ").append(ticket.getIconoPrioridad()).append(" ").append(ticket.getPrioridad()).append("\n");
        detalles.append("🔄 Estado: ").append(ticket.getEstado()).append("\n");
        detalles.append("📅 Creado: ").append(ticket.getFechaCreacionFormateada()).append("\n");
        detalles.append("🕐 Última actualización: ").append(ticket.getFechaActualizacionFormateada()).append("\n");
        detalles.append("⏱️ Tiempo transcurrido: ").append(ticket.getTiempoTranscurrido()).append("\n");

        if (ticket.tieneArchivo()) {
            detalles.append("📎 Archivo adjunto: Sí\n");
        }

        if (ticket.tieneRespuesta()) {
            detalles.append("💬 Respondido: ").append(ticket.getFechaRespuestaFormateada()).append("\n");
        }

        detalles.append("\n=== DESCRIPCIÓN ===\n\n");
        detalles.append(ticket.getDescripcion());

        if (ticket.tieneRespuesta()) {
            detalles.append("\n\n=== RESPUESTA ANTERIOR ===\n\n");
            detalles.append(ticket.getRespuestaDeveloper());
        }

        detallesArea.setText(detalles.toString());
        detallesArea.setCaretPosition(0);
    }

    private void actualizarTicket(ActionEvent e) {
        if (ticketSeleccionado == null) {
            return;
        }

        String nuevoEstado = (String) nuevoEstadoCombo.getSelectedItem();
        String respuesta = respuestaArea.getText().trim();

        if (respuesta.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Actualizar el ticket sin agregar respuesta?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Confirmar actualización
        int confirmacion = JOptionPane.showConfirmDialog(this,
                String.format("¿Actualizar ticket %s?\n\nNuevo estado: %s\n%s",
                        ticketSeleccionado.getTicketNumber(),
                        nuevoEstado,
                        respuesta.isEmpty() ? "Sin respuesta" : "Con respuesta"),
                "Confirmar Actualización",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // Deshabilitar botón durante actualización
            actualizarButton.setEnabled(false);
            actualizarButton.setText("Actualizando...");

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return ticketService.actualizarTicket(ticketSeleccionado.getId(), nuevoEstado, respuesta);
                }

                @Override
                protected void done() {
                    actualizarButton.setText("💾 Actualizar Ticket");
                    actualizarButton.setEnabled(true);

                    try {
                        boolean exito = get();
                        if (exito) {
                            JOptionPane.showMessageDialog(TicketManagementWindow.this,
                                    "✅ Ticket actualizado exitosamente",
                                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

                            // Recargar tickets y mantener selección
                            loadTickets();
                        } else {
                            JOptionPane.showMessageDialog(TicketManagementWindow.this,
                                    "❌ Error al actualizar el ticket",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TicketManagementWindow.this,
                                "Error: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
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

    private void actualizarEstadisticas() {
        if (todosLosTickets == null) {
            estadisticasLabel.setText("Sin datos");
            return;
        }

        Map<String, Integer> stats = ticketService.obtenerEstadisticas();

        String estadisticas = String.format(
                "📊 Total: %d | 🔴 Abiertos: %d | 🟡 En revisión: %d | 🟢 Resueltos: %d | ⚫ Cerrados: %d",
                stats.get("TOTAL"),
                stats.get("ABIERTO"),
                stats.get("EN_REVISION"),
                stats.get("RESUELTO"),
                stats.get("CERRADO")
        );

        estadisticasLabel.setText(estadisticas);
    }

    private void limpiarFiltros(ActionEvent e) {
        busquedaField.setText("");
        estadoFilterCombo.setSelectedIndex(0);
        categoriaFilterCombo.setSelectedIndex(0);
        prioridadFilterCombo.setSelectedIndex(0);
        aplicarFiltros();
    }

    private void exportarTickets(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de exportación en desarrollo.\n"
                + "Próximamente podrás exportar los tickets a Excel o PDF.",
                "Exportar Tickets", JOptionPane.INFORMATION_MESSAGE);
    }

    private void abrirConfiguracion(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Panel de configuración en desarrollo.\n"
                + "Próximamente podrás configurar:\n"
                + "• Desarrolladores autorizados\n"
                + "• Plantillas de respuesta\n"
                + "• Notificaciones automáticas",
                "Configuración", JOptionPane.INFORMATION_MESSAGE);
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
        loadTickets();
    }

    /**
     * Método para obtener estadísticas rápidas
     */
    public Map<String, Integer> getQuickStats() {
        return ticketService.obtenerEstadisticas();
    }

    @Override
    public void dispose() {
        try {
            System.out.println("🧹 Cerrando TicketManagementWindow");
            super.dispose();
        } catch (Exception e) {
            System.err.println("Error cerrando ventana: " + e.getMessage());
        }
    }
}
