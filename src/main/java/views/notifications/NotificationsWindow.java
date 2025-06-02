package main.java.views.notifications;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import main.java.models.NotificacionDestinatario;
import main.java.services.NotificationService;
import main.java.components.ExpandableNotificationPanel;

/**
 * Ventana CORREGIDA para visualizar notificaciones con: - Vista expandible tipo
 * acordeón usando ExpandableNotificationPanel - Filtros avanzados (fecha,
 * remitente, estado) - Marcar como leída individualmente - Interfaz más
 * intuitiva y moderna
 *
 * @version 2.1 - Sistema de notificaciones corregido
 */
public class NotificationsWindow extends JFrame {

    private final int usuarioId;
    private final NotificationService notificationService;

    // Componentes UI principales
    private JPanel notificationsContainer;
    private JScrollPane scrollPane;

    // Filtros y controles
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> senderFilterCombo;
    private JComboBox<String> dateFilterCombo;
    private JButton clearFiltersButton;
    private JButton markAllReadButton;
    private JButton refreshButton;

    // Información y estadísticas
    private JLabel totalCountLabel;
    private JLabel unreadCountLabel;
    private JLabel filteredCountLabel;

    // Datos
    private List<NotificacionDestinatario> allNotifications;
    private List<NotificacionDestinatario> filteredNotifications;
    private List<ExpandableNotificationPanel> notificationPanels;

    public NotificationsWindow(int usuarioId) {
        this.usuarioId = usuarioId;
        this.notificationService = NotificationService.getInstance();
        this.allNotifications = new ArrayList<>();
        this.filteredNotifications = new ArrayList<>();
        this.notificationPanels = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadNotifications();

        setTitle("📬 Mis Notificaciones - Sistema Mejorado");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        System.out.println("✅ NotificationsWindow inicializada para usuario: " + usuarioId);
    }

    private void initializeComponents() {
        // === PANEL DE FILTROS ===
        searchField = new JTextField(20);
        searchField.setToolTipText("Buscar por título o contenido");

        String[] statusOptions = {"Todas", "No leídas", "Leídas"};
        statusFilterCombo = new JComboBox<>(statusOptions);

        senderFilterCombo = new JComboBox<>();
        senderFilterCombo.addItem("Todos los remitentes");

        String[] dateOptions = {"Todas las fechas", "Hoy", "Esta semana", "Este mes", "Último mes"};
        dateFilterCombo = new JComboBox<>(dateOptions);

        clearFiltersButton = new JButton("🔄 Limpiar Filtros");
        clearFiltersButton.setBackground(new Color(108, 117, 125));
        clearFiltersButton.setForeground(Color.WHITE);
        clearFiltersButton.setBorderPainted(false);
        clearFiltersButton.setFocusPainted(false);

        // === BOTONES DE ACCIÓN ===
        markAllReadButton = new JButton("✅ Marcar Todas como Leídas");
        markAllReadButton.setBackground(new Color(40, 167, 69));
        markAllReadButton.setForeground(Color.WHITE);
        markAllReadButton.setFont(new Font("Arial", Font.BOLD, 12));
        markAllReadButton.setBorderPainted(false);
        markAllReadButton.setFocusPainted(false);

        refreshButton = new JButton("🔄 Actualizar");
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorderPainted(false);
        refreshButton.setFocusPainted(false);

        // === LABELS DE INFORMACIÓN ===
        totalCountLabel = new JLabel("Total: 0");
        totalCountLabel.setFont(new Font("Arial", Font.BOLD, 12));

        unreadCountLabel = new JLabel("No leídas: 0");
        unreadCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        unreadCountLabel.setForeground(new Color(220, 53, 69));

        filteredCountLabel = new JLabel("Mostrando: 0");
        filteredCountLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        filteredCountLabel.setForeground(Color.GRAY);

        // === CONTENEDOR DE NOTIFICACIONES ===
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // === PANEL SUPERIOR - FILTROS Y CONTROLES ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        topPanel.setBackground(new Color(248, 249, 250));

        // Panel de filtros
        JPanel filtersPanel = createFiltersPanel();
        topPanel.add(filtersPanel, BorderLayout.CENTER);

        // Panel de estadísticas
        JPanel statsPanel = createStatsPanel();
        topPanel.add(statsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // === PANEL CENTRAL - NOTIFICACIONES ===
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "📋 Lista de Notificaciones",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        add(scrollPane, BorderLayout.CENTER);

        // === PANEL INFERIOR - ACCIONES ===
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createFiltersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🔍 Filtros de Búsqueda"));
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 1: Búsqueda de texto
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Buscar:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchField, gbc);

        // Fila 2: Filtros por combo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        panel.add(statusFilterCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Remitente:"), gbc);
        gbc.gridx = 3;
        panel.add(senderFilterCombo, gbc);

        // Fila 3: Fecha y limpiar
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        panel.add(dateFilterCombo, gbc);

        gbc.gridx = 3;
        panel.add(clearFiltersButton, gbc);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        panel.add(totalCountLabel);
        panel.add(new JLabel(" | "));
        panel.add(unreadCountLabel);
        panel.add(new JLabel(" | "));
        panel.add(filteredCountLabel);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 15, 15));

        // Botones de acción
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.add(refreshButton);
        actionsPanel.add(markAllReadButton);

        // Información adicional
        JLabel infoLabel = new JLabel("💡 Haz clic en una notificación para expandir/contraer");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);

        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);

        return panel;
    }

    private void setupListeners() {
        // Filtros en tiempo real
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> applyFilters());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> applyFilters());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> applyFilters());
            }
        });

        statusFilterCombo.addActionListener(e -> applyFilters());
        senderFilterCombo.addActionListener(e -> applyFilters());
        dateFilterCombo.addActionListener(e -> applyFilters());

        // Botones
        clearFiltersButton.addActionListener(this::clearAllFilters);
        refreshButton.addActionListener(e -> loadNotifications());
        markAllReadButton.addActionListener(this::markAllAsRead);
    }

    private void loadNotifications() {
        // Mostrar indicador de carga
        showLoadingState();

        SwingWorker<List<NotificacionDestinatario>, Void> worker
                = new SwingWorker<List<NotificacionDestinatario>, Void>() {
            @Override
            protected List<NotificacionDestinatario> doInBackground() throws Exception {
                // CORREGIDO: Intentar obtener todas las notificaciones con fallback
                try {
                    return notificationService.obtenerTodasLasNotificaciones(usuarioId, 200);
                } catch (Exception e) {
                    System.err.println("Error obteniendo todas las notificaciones, intentando solo no leídas: " + e.getMessage());
                    // Fallback: obtener solo no leídas si falla
                    return notificationService.obtenerNotificacionesNoLeidas(usuarioId);
                }
            }

            @Override
            protected void done() {
                try {
                    allNotifications = get();
                    if (allNotifications == null) {
                        allNotifications = new ArrayList<>();
                    }

                    // CORREGIDO: Validar y limpiar notificaciones inválidas
                    allNotifications = allNotifications.stream()
                            .filter(n -> n != null && n.getTitulo() != null && !n.getTitulo().trim().isEmpty())
                            .collect(Collectors.toList());

                    loadSenderOptions();
                    applyFilters();
                    updateStats();
                    hideLoadingState();
                    System.out.println("✅ Notificaciones cargadas: " + allNotifications.size());
                } catch (Exception e) {
                    hideLoadingState();
                    e.printStackTrace();
                    showError("Error cargando notificaciones: " + e.getMessage());

                    // CORREGIDO: En caso de error, mostrar notificaciones vacías en lugar de fallar
                    allNotifications = new ArrayList<>();
                    filteredNotifications = new ArrayList<>();
                    updateNotificationsDisplay();
                    updateStats();
                }
            }
        };
        worker.execute();
    }

    private void showLoadingState() {
        SwingUtilities.invokeLater(() -> {
            notificationsContainer.removeAll();

            JPanel loadingPanel = new JPanel(new BorderLayout());
            loadingPanel.setBackground(Color.WHITE);
            loadingPanel.setPreferredSize(new Dimension(0, 200));

            JLabel loadingIcon = new JLabel("⏳", SwingConstants.CENTER);
            loadingIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

            JLabel loadingText = new JLabel("Cargando notificaciones...", SwingConstants.CENTER);
            loadingText.setFont(new Font("Arial", Font.ITALIC, 16));
            loadingText.setForeground(Color.GRAY);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBackground(Color.WHITE);
            textPanel.add(Box.createVerticalGlue());
            textPanel.add(loadingIcon);
            textPanel.add(Box.createVerticalStrut(10));
            textPanel.add(loadingText);
            textPanel.add(Box.createVerticalGlue());

            loadingPanel.add(textPanel, BorderLayout.CENTER);
            notificationsContainer.add(loadingPanel);
            notificationsContainer.revalidate();
            notificationsContainer.repaint();

            // Deshabilitar botones durante la carga
            refreshButton.setEnabled(false);
            markAllReadButton.setEnabled(false);
        });
    }

    /**
     * NUEVO: Método para ocultar estado de carga
     */
    private void hideLoadingState() {
        SwingUtilities.invokeLater(() -> {
            refreshButton.setEnabled(true);
            markAllReadButton.setEnabled(true);
        });
    }

    private void loadSenderOptions() {
        SwingUtilities.invokeLater(() -> {
            try {
                // ✅ CORREGIDO: Limpiar y re-agregar la opción por defecto de forma segura
                senderFilterCombo.removeAllItems();
                senderFilterCombo.addItem("Todos los remitentes");

                if (allNotifications != null && !allNotifications.isEmpty()) {
                    // ✅ CORREGIDO: Mejor filtrado y manejo de remitentes con validación null-safe
                    Set<String> uniqueSenders = new java.util.HashSet<>();

                    for (NotificacionDestinatario notification : allNotifications) {
                        if (notification != null) {
                            String sender = notification.getRemitenteNombre();

                            // Validar y limpiar el nombre del remitente
                            if (sender != null && !sender.trim().isEmpty() && !"null".equals(sender)) {
                                String cleanSender = sender.trim();
                                uniqueSenders.add(cleanSender);
                            }
                        }
                    }

                    // Agregar remitentes únicos ordenados
                    uniqueSenders.stream()
                            .sorted()
                            .forEach(senderFilterCombo::addItem);

                    System.out.println("📧 Remitentes cargados: " + uniqueSenders.size());
                }
            } catch (Exception e) {
                System.err.println("❌ Error cargando opciones de remitente: " + e.getMessage());
                e.printStackTrace();

                // En caso de error, asegurar que al menos esté la opción por defecto
                if (senderFilterCombo.getItemCount() == 0) {
                    senderFilterCombo.addItem("Todos los remitentes");
                }
            }
        });
    }

    private void applyFilters() {
        if (allNotifications == null) {
            filteredNotifications = new ArrayList<>();
            updateNotificationsDisplay();
            updateStats();
            return;
        }

        try {
            // ✅ CORREGIDO: Validación null-safe para todos los filtros
            String searchText = "";
            if (searchField != null && searchField.getText() != null) {
                searchText = searchField.getText().toLowerCase().trim();
                if (searchText.equals("buscar por título o contenido")) {
                    searchText = "";
                }
            }

            String statusFilter = "Todas";
            if (statusFilterCombo != null && statusFilterCombo.getSelectedItem() != null) {
                statusFilter = (String) statusFilterCombo.getSelectedItem();
            }

            String senderFilter = "Todos los remitentes";
            if (senderFilterCombo != null && senderFilterCombo.getSelectedItem() != null) {
                senderFilter = (String) senderFilterCombo.getSelectedItem();
            }

            String dateFilter = "Todas las fechas";
            if (dateFilterCombo != null && dateFilterCombo.getSelectedItem() != null) {
                dateFilter = (String) dateFilterCombo.getSelectedItem();
            }

            final String finalSearchText = searchText;
            final String finalStatusFilter = statusFilter;
            final String finalSenderFilter = senderFilter;
            final String finalDateFilter = dateFilter;

            // ✅ CORREGIDO: Usar stream normal en lugar de parallelStream para evitar problemas de concurrencia
            filteredNotifications = allNotifications.stream()
                    .filter(notification -> notification != null)
                    .filter(notification -> matchesFilters(notification, finalSearchText, finalStatusFilter, finalSenderFilter, finalDateFilter))
                    .collect(java.util.stream.Collectors.toList());

            // ✅ CORREGIDO: Ejecutar en EDT thread para actualización de UI
            SwingUtilities.invokeLater(() -> {
                updateNotificationsDisplay();
                updateStats();
            });

            System.out.println("🔍 Filtros aplicados - Mostrando: " + filteredNotifications.size() + " de " + allNotifications.size());

        } catch (Exception e) {
            System.err.println("❌ Error aplicando filtros: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, mostrar todas las notificaciones
            filteredNotifications = new ArrayList<>(allNotifications);
            SwingUtilities.invokeLater(() -> {
                updateNotificationsDisplay();
                updateStats();
            });
        }
    }

    private boolean matchesFilters(NotificacionDestinatario notification,
            String searchText, String statusFilter,
            String senderFilter, String dateFilter) {

        if (notification == null) {
            return false;
        }

        try {
            // ✅ CORREGIDO: Filtro de texto con validación null-safe
            if (searchText != null && !searchText.isEmpty()) {
                String title = notification.getTitulo();
                String content = notification.getContenido();

                title = (title != null) ? title.toLowerCase() : "";
                content = (content != null) ? content.toLowerCase() : "";

                if (!title.contains(searchText) && !content.contains(searchText)) {
                    return false;
                }
            }

            // ✅ CORREGIDO: Filtro de estado con validación null-safe
            if (statusFilter != null) {
                try {
                    if ("No leídas".equals(statusFilter) && notification.isLeida()) {
                        return false;
                    }
                    if ("Leídas".equals(statusFilter) && !notification.isLeida()) {
                        return false;
                    }
                } catch (Exception e) {
                    // Si hay error determinando el estado, considerar como no leída
                    if ("Leídas".equals(statusFilter)) {
                        return false;
                    }
                }
            }

            // ✅ CORREGIDO: Filtro de remitente con validación null-safe COMPLETA
            if (senderFilter != null && !"Todos los remitentes".equals(senderFilter)) {
                String notificationSender = notification.getRemitenteNombre();

                // Validar que tanto el filtro como el remitente de la notificación no sean null
                if (notificationSender == null) {
                    notificationSender = ""; // Valor por defecto si es null
                }

                // Comparar de forma segura
                if (!senderFilter.equals(notificationSender.trim())) {
                    return false;
                }
            }

            // ✅ CORREGIDO: Filtro de fecha con validación null-safe
            if (dateFilter != null) {
                return matchesDateFilter(notification, dateFilter);
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error en matchesFilters para notificación " + notification.getId() + ": " + e.getMessage());
            // En caso de error, incluir la notificación por defecto
            return true;
        }
    }

    private boolean matchesDateFilter(NotificacionDestinatario notification, String dateFilter) {
        // ✅ CORREGIDO: Validación null y valor por defecto
        if (dateFilter == null || "Todas las fechas".equals(dateFilter)) {
            return true;
        }

        if (notification == null || notification.getFechaCreacion() == null) {
            return false;
        }

        try {
            java.time.LocalDate notificationDate = notification.getFechaCreacion().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();

            switch (dateFilter) {
                case "Hoy":
                    return notificationDate.equals(today);
                case "Esta semana":
                    return notificationDate.isAfter(today.minusDays(7)) || notificationDate.equals(today);
                case "Este mes":
                    return notificationDate.getMonth() == today.getMonth()
                            && notificationDate.getYear() == today.getYear();
                case "Último mes":
                    return notificationDate.isAfter(today.minusDays(30)) || notificationDate.equals(today);
                default:
                    return true;
            }
        } catch (Exception e) {
            System.err.println("❌ Error procesando fecha de notificación: " + e.getMessage());
            return true; // En caso de error, incluir la notificación
        }
    }

    private void updateNotificationsDisplay() {
        SwingUtilities.invokeLater(() -> {
            // CORREGIDO: Limpiar paneles existentes correctamente para evitar memory leaks
            for (ExpandableNotificationPanel panel : notificationPanels) {
                if (panel != null) {
                    try {
                        panel.dispose();
                    } catch (Exception e) {
                        System.err.println("Error disposing panel: " + e.getMessage());
                    }
                }
            }

            notificationsContainer.removeAll();
            notificationPanels.clear();

            if (filteredNotifications == null || filteredNotifications.isEmpty()) {
                showEmptyState();
            } else {
                // CORREGIDO: Crear paneles con mejor manejo de errores
                for (NotificacionDestinatario notification : filteredNotifications) {
                    try {
                        if (notification != null) {
                            ExpandableNotificationPanel panel = new ExpandableNotificationPanel(
                                    notification,
                                    createNotificationListener(),
                                    this.usuarioId
                            );

                            notificationPanels.add(panel);
                            notificationsContainer.add(panel);
                            notificationsContainer.add(Box.createVerticalStrut(5));
                        }
                    } catch (Exception e) {
                        System.err.println("Error creando panel de notificación: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Actualizar vista
            notificationsContainer.revalidate();
            notificationsContainer.repaint();

            // Scroll to top
            scrollToTop();
        });
    }

    /**
     * NUEVO: Método separado para scroll to top
     */
    private void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (scrollPane != null && scrollPane.getVerticalScrollBar() != null) {
                    scrollPane.getVerticalScrollBar().setValue(0);
                    scrollPane.getViewport().setViewPosition(new Point(0, 0));
                }
            } catch (Exception e) {
                System.err.println("Error en scroll to top: " + e.getMessage());
            }
        });
    }

    private ExpandableNotificationPanel.ExpandableNotificationListener createNotificationListener() {
        return new ExpandableNotificationPanel.ExpandableNotificationListener() {
            @Override
            public void onMarkAsRead(ExpandableNotificationPanel panel) {
                if (panel == null) {
                    return;
                }

                try {
                    NotificacionDestinatario notification = panel.getNotification();
                    if (notification != null) {
                        // CORREGIDO: Actualizar en background thread
                        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                            @Override
                            protected Boolean doInBackground() throws Exception {
                                return notificationService.marcarComoLeida(notification.getId(), usuarioId);
                            }

                            @Override
                            protected void done() {
                                try {
                                    boolean success = get();
                                    if (success) {
                                        notification.setEstadoLectura("LEIDA");
                                        notification.setFechaLeida(java.time.LocalDateTime.now());
                                        updateStats();
                                        System.out.println("✅ Notificación marcada como leída: " + notification.getId());
                                    } else {
                                        showError("No se pudo marcar la notificación como leída");
                                    }
                                } catch (Exception e) {
                                    showError("Error marcando notificación como leída: " + e.getMessage());
                                }
                            }
                        };
                        worker.execute();
                    }
                } catch (Exception e) {
                    System.err.println("Error en onMarkAsRead: " + e.getMessage());
                    showError("Error procesando la acción");
                }
            }

            @Override
            public void onArchive(ExpandableNotificationPanel panel) {
                if (panel == null) {
                    return;
                }

                try {
                    NotificacionDestinatario notification = panel.getNotification();
                    if (notification != null) {
                        // CORREGIDO: Confirmar antes de archivar
                        int confirm = JOptionPane.showConfirmDialog(
                                NotificationsWindow.this,
                                "¿Archivar esta notificación?\nEsta acción no se puede deshacer.",
                                "Confirmar archivo",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (confirm == JOptionPane.YES_OPTION) {
                            filteredNotifications.remove(notification);
                            allNotifications.remove(notification);
                            updateNotificationsDisplay();
                            updateStats();
                            System.out.println("🗂️ Notificación archivada: " + notification.getId());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error en onArchive: " + e.getMessage());
                    showError("Error archivando notificación");
                }
            }

            @Override
            public void onConfirm(ExpandableNotificationPanel panel) {
                if (panel == null) {
                    return;
                }

                try {
                    NotificacionDestinatario notification = panel.getNotification();
                    if (notification != null) {
                        // CORREGIDO: Implementar confirmación real si el servicio lo soporta
                        notification.setEstadoLectura("CONFIRMADA");
                        notification.setFechaConfirmada(java.time.LocalDateTime.now());
                        updateStats();

                        // Aquí podrías agregar lógica para persistir la confirmación
                        // notificationService.confirmarNotificacion(notification.getId(), usuarioId);
                        System.out.println("☑️ Notificación confirmada: " + notification.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error en onConfirm: " + e.getMessage());
                    showError("Error confirmando notificación");
                }
            }

            @Override
            public void onRefreshRequested() {
                loadNotifications();
            }
        };
    }

    private void showEmptyState() {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setPreferredSize(new Dimension(0, 200));
        emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel emptyIcon = new JLabel("📭", SwingConstants.CENTER);
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emptyIcon.setForeground(Color.LIGHT_GRAY);

        JLabel emptyText = new JLabel("No se encontraron notificaciones", SwingConstants.CENTER);
        emptyText.setFont(new Font("Arial", Font.ITALIC, 16));
        emptyText.setForeground(Color.GRAY);

        JLabel emptySubtext = new JLabel("Intenta cambiar los filtros de búsqueda", SwingConstants.CENTER);
        emptySubtext.setFont(new Font("Arial", Font.PLAIN, 12));
        emptySubtext.setForeground(Color.LIGHT_GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(emptyIcon);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(emptyText);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(emptySubtext);
        textPanel.add(Box.createVerticalGlue());

        emptyPanel.add(textPanel, BorderLayout.CENTER);
        notificationsContainer.add(emptyPanel);
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            try {
                int total = (allNotifications != null) ? allNotifications.size() : 0;

                int unread = 0;
                if (allNotifications != null) {
                    unread = (int) allNotifications.stream()
                            .filter(n -> n != null)
                            .filter(n -> {
                                try {
                                    return !n.isLeida();
                                } catch (Exception e) {
                                    return true; // En caso de error, considerar como no leída
                                }
                            })
                            .count();
                }

                int filtered = (filteredNotifications != null) ? filteredNotifications.size() : 0;

                totalCountLabel.setText("Total: " + total);
                unreadCountLabel.setText("No leídas: " + unread);
                filteredCountLabel.setText("Mostrando: " + filtered);

                // CORREGIDO: Actualizar color según estado
                if (unread > 0) {
                    unreadCountLabel.setForeground(new Color(220, 53, 69)); // Rojo
                } else {
                    unreadCountLabel.setForeground(new Color(40, 167, 69)); // Verde
                }

            } catch (Exception e) {
                System.err.println("Error actualizando estadísticas: " + e.getMessage());
                // En caso de error, mostrar valores por defecto
                totalCountLabel.setText("Total: 0");
                unreadCountLabel.setText("No leídas: 0");
                filteredCountLabel.setText("Mostrando: 0");
            }
        });
    }

    /**
     * NUEVO: Método para exportar notificaciones a texto
     */
    public void exportarNotificaciones() {
        if (filteredNotifications == null || filteredNotifications.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay notificaciones para exportar",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("notificaciones_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("=== EXPORTACIÓN DE NOTIFICACIONES ===");
                writer.println("Fecha de exportación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                writer.println("Usuario ID: " + usuarioId);
                writer.println("Total de notificaciones: " + filteredNotifications.size());
                writer.println();

                for (int i = 0; i < filteredNotifications.size(); i++) {
                    NotificacionDestinatario n = filteredNotifications.get(i);
                    if (n != null) {
                        writer.println("--- Notificación " + (i + 1) + " ---");
                        writer.println("Título: " + (n.getTitulo() != null ? n.getTitulo() : "Sin título"));
                        writer.println("Contenido: " + (n.getContenido() != null ? n.getContenido() : "Sin contenido"));
                        writer.println("Remitente: " + (n.getRemitenteNombre() != null ? n.getRemitenteNombre() : "Desconocido"));
                        writer.println("Estado: " + (n.isLeida() ? "Leída" : "No leída"));
                        if (n.getFechaCreacion() != null) {
                            writer.println("Fecha: " + n.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        }
                        writer.println();
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Notificaciones exportadas exitosamente",
                        "Exportación completa", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                showError("Error exportando notificaciones: " + e.getMessage());
            }
        }
    }

    /**
     * NUEVO: Método para buscar notificaciones por ID específico
     */
    public void buscarPorId() {
        String idStr = JOptionPane.showInputDialog(this,
                "Ingrese el ID de la notificación a buscar:");

        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr.trim());

                NotificacionDestinatario found = allNotifications.stream()
                        .filter(n -> n != null && n.getId() == id)
                        .findFirst()
                        .orElse(null);

                if (found != null) {
                    // Limpiar filtros y mostrar solo la notificación encontrada
                    clearAllFilters(null);
                    searchField.setText(found.getTitulo());
                    applyFilters();

                    JOptionPane.showMessageDialog(this,
                            "Notificación encontrada y mostrada",
                            "Búsqueda exitosa", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró una notificación con ID: " + id,
                            "No encontrada", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, ingrese un número válido",
                        "ID inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void clearAllFilters(java.awt.event.ActionEvent e) {
        try {
            System.out.println("🧹 Limpiando todos los filtros...");

            // ✅ CORREGIDO: Validar que los componentes existan antes de limpiarlos
            if (searchField != null) {
                searchField.setText("");
            }

            if (statusFilterCombo != null && statusFilterCombo.getItemCount() > 0) {
                statusFilterCombo.setSelectedIndex(0);
            }

            if (senderFilterCombo != null && senderFilterCombo.getItemCount() > 0) {
                senderFilterCombo.setSelectedIndex(0);
            }

            if (dateFilterCombo != null && dateFilterCombo.getItemCount() > 0) {
                dateFilterCombo.setSelectedIndex(0);
            }

            // Aplicar filtros después de limpiar
            applyFilters();

            System.out.println("✅ Filtros limpiados exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error limpiando filtros: " + ex.getMessage());
            ex.printStackTrace();

            // En caso de error, mostrar todas las notificaciones
            if (allNotifications != null) {
                filteredNotifications = new ArrayList<>(allNotifications);
                updateNotificationsDisplay();
                updateStats();
            }
        }
    }

    private void markAllAsRead(ActionEvent e) {
        if (filteredNotifications == null || filteredNotifications.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay notificaciones para marcar como leídas.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<NotificacionDestinatario> unreadNotifications = filteredNotifications.stream()
                .filter(n -> n != null && !n.isLeida())
                .collect(Collectors.toList());

        if (unreadNotifications.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay notificaciones no leídas en la selección actual.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "¿Marcar " + unreadNotifications.size()
                + " notificación(es) como leídas?",
                "Confirmar acción",
                JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            // CORREGIDO: Mostrar progreso durante la operación
            markAllReadButton.setEnabled(false);
            markAllReadButton.setText("Procesando...");

            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    int processed = 0;
                    for (NotificacionDestinatario notification : unreadNotifications) {
                        if (notification != null) {
                            try {
                                boolean success = notificationService.marcarComoLeida(notification.getId(), usuarioId);
                                if (success) {
                                    processed++;
                                }
                            } catch (Exception e) {
                                System.err.println("Error marcando notificación " + notification.getId() + ": " + e.getMessage());
                            }
                        }
                    }
                    return processed;
                }

                @Override
                protected void done() {
                    try {
                        int processed = get();
                        loadNotifications(); // Recargar datos

                        String message = processed > 0
                                ? "✅ " + processed + " notificación(es) marcada(s) como leída(s)"
                                : "⚠️ No se pudieron procesar las notificaciones";

                        JOptionPane.showMessageDialog(NotificationsWindow.this,
                                message,
                                "Acción completada",
                                processed > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        showError("Error procesando notificaciones: " + ex.getMessage());
                    } finally {
                        markAllReadButton.setEnabled(true);
                        markAllReadButton.setText("✅ Marcar Todas como Leídas");
                    }
                }
            };
            worker.execute();
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Método público para refrescar las notificaciones desde el exterior
     */
    public void refreshNotifications() {
        loadNotifications();
    }

    /**
     * Método público para obtener el ID del usuario
     */
    public int getUsuarioId() {
        return usuarioId;
    }

    /**
     * Método de limpieza al cerrar la ventana
     */
    @Override
    public void dispose() {
        try {
            // Limpiar listas
            if (allNotifications != null) {
                allNotifications.clear();
            }
            if (filteredNotifications != null) {
                filteredNotifications.clear();
            }

            // Limpiar paneles de notificaciones
            if (notificationPanels != null) {
                for (ExpandableNotificationPanel panel : notificationPanels) {
                    if (panel != null) {
                        panel.dispose();
                    }
                }
                notificationPanels.clear();
            }

            System.out.println("✅ NotificationsWindow recursos liberados");
        } catch (Exception e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        } finally {
            super.dispose();
        }
    }
}
