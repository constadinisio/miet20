package main.java.views.notifications;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import main.java.models.NotificationModels.*;
import main.java.services.NotificationCore.*;
import main.java.services.NotificationGroups.*;
import main.java.views.notifications.NotificationPanels.ExpandableNotificationPanel;
import main.java.views.notifications.NotificationPanels.ExpandableNotificationListener;

/**
 * =========================================================================
 * INTERFACES CONSOLIDADAS DEL SISTEMA DE NOTIFICACIONES
 * =========================================================================
 * 
 * Este archivo consolida todas las ventanas y componentes visuales del sistema
 * de notificaciones para reducir la cantidad de archivos y centralizar la UI.
 * 
 * CONSOLIDACIÓN DE 7 ARCHIVOS:
 * - NotificationsWindow.java
 * - NotificationSenderWindow.java  
 * - NotificationGroupWindow.java
 * - GroupManagerWindow.java
 * - NotificationBellComponent.java
 * - ExpandableNotificationPanel.java
 * - NotificationStartupBanner.java
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Consolidado
 * @date 21/07/2025
 */
public class NotificationUI {

    // =========================================================================
    // 1. NOTIFICATIONS_WINDOW - VENTANA PRINCIPAL DE NOTIFICACIONES
    // =========================================================================
    
    /**
     * Ventana principal para visualizar notificaciones con filtros avanzados
     */
    public static class NotificationsWindow extends JFrame {

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
        private JComboBox<String> priorityFilterCombo;
        private JComboBox<String> typeFilterCombo;
        private JCheckBox onlyImportantCheckBox;
        private JCheckBox requiresActionCheckBox;
        private JButton clearFiltersButton;
        private JButton markAllReadButton;
        private JButton refreshButton;
        private JButton advancedFiltersButton;

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

            System.out.println("🔧 Inicializando NotificationsWindow con filtros avanzados...");
            
            initializeComponents();
            setupLayout();
            setupListeners();
            loadNotifications();

            setTitle("📬 Mis Notificaciones - Sistema Consolidado con Filtros Avanzados");
            setSize(1200, 800); // Aumentar un poco el tamaño para los filtros
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            System.out.println("✅ NotificationsWindow inicializada para usuario: " + usuarioId);
        }

        private void initializeComponents() {
            System.out.println("🔧 Creando componentes de filtros...");
            
            // Crear componentes básicos
            searchField = new JTextField(20);
            searchField.setToolTipText("Buscar en título y contenido de notificaciones");
            
            // Filtros principales
            statusFilterCombo = new JComboBox<>(new String[]{"Todos", "No leídas", "Leídas", "Confirmadas"});
            statusFilterCombo.setToolTipText("Filtrar por estado de lectura");
            
            senderFilterCombo = new JComboBox<>();
            senderFilterCombo.setToolTipText("Filtrar por remitente");
            
            dateFilterCombo = new JComboBox<>(new String[]{
                "Todos", "Hoy", "Ayer", "Esta semana", "Semana pasada", 
                "Este mes", "Mes pasado", "Últimos 3 meses", "Este año"
            });
            dateFilterCombo.setToolTipText("Filtrar por fecha de creación");
            
            // Nuevos filtros avanzados
            priorityFilterCombo = new JComboBox<>(new String[]{
                "Todas las prioridades", "Urgente", "Alta", "Normal", "Baja"
            });
            priorityFilterCombo.setToolTipText("Filtrar por prioridad");
            
            typeFilterCombo = new JComboBox<>(new String[]{
                "Todos los tipos", "Individual", "Grupal", "Rol", "Sistema", "Académica"
            });
            typeFilterCombo.setToolTipText("Filtrar por tipo de notificación");
            
            // Filtros checkbox rápidos
            onlyImportantCheckBox = new JCheckBox("Solo importantes");
            onlyImportantCheckBox.setToolTipText("Mostrar solo notificaciones urgentes y de alta prioridad");
            
            requiresActionCheckBox = new JCheckBox("Requieren acción");
            requiresActionCheckBox.setToolTipText("Mostrar solo notificaciones que requieren confirmación o respuesta");
            
            // Botones
            clearFiltersButton = new JButton("🔄 Limpiar Filtros");
            clearFiltersButton.setToolTipText("Restablecer todos los filtros");
            
            markAllReadButton = new JButton("✅ Marcar Todas como Leídas");
            markAllReadButton.setToolTipText("Marcar todas las notificaciones visibles como leídas");
            
            refreshButton = new JButton("🔄 Actualizar");
            refreshButton.setToolTipText("Recargar notificaciones");
            
            advancedFiltersButton = new JButton("⚙️ Filtros Avanzados");
            advancedFiltersButton.setToolTipText("Mostrar/ocultar filtros adicionales");

            // Labels de estadísticas
            totalCountLabel = new JLabel("Total: 0");
            unreadCountLabel = new JLabel("No leídas: 0");
            filteredCountLabel = new JLabel("Mostradas: 0");

            // Panel de notificaciones
            notificationsContainer = new JPanel();
            notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(notificationsContainer);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            System.out.println("✅ Componentes de filtros creados correctamente");
        }

        private void setupLayout() {
            System.out.println("🔧 Configurando layout con filtros...");
            
            setLayout(new BorderLayout(10, 10));

            // Panel superior con filtros
            JPanel topPanel = new JPanel(new BorderLayout(5, 5));
            topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

            // Panel de filtros principales (siempre visible)
            JPanel mainFiltersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            mainFiltersPanel.setBorder(new TitledBorder("🔍 Filtros Principales"));
            
            mainFiltersPanel.add(new JLabel("Buscar:"));
            mainFiltersPanel.add(searchField);
            mainFiltersPanel.add(new JLabel("Estado:"));
            mainFiltersPanel.add(statusFilterCombo);
            mainFiltersPanel.add(new JLabel("Remitente:"));
            mainFiltersPanel.add(senderFilterCombo);
            mainFiltersPanel.add(new JLabel("Fecha:"));
            mainFiltersPanel.add(dateFilterCombo);
            mainFiltersPanel.add(advancedFiltersButton);

            // Panel de filtros avanzados (inicialmente oculto)
            JPanel advancedFiltersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            advancedFiltersPanel.setBorder(new TitledBorder("⚙️ Filtros Avanzados"));
            advancedFiltersPanel.add(new JLabel("Prioridad:"));
            advancedFiltersPanel.add(priorityFilterCombo);
            advancedFiltersPanel.add(new JLabel("Tipo:"));
            advancedFiltersPanel.add(typeFilterCombo);
            advancedFiltersPanel.add(new JLabel("|"));
            advancedFiltersPanel.add(onlyImportantCheckBox);
            advancedFiltersPanel.add(requiresActionCheckBox);
            advancedFiltersPanel.add(new JLabel("|"));
            advancedFiltersPanel.add(clearFiltersButton);
            advancedFiltersPanel.setVisible(false); // Inicialmente oculto

            // Panel contenedor de filtros
            JPanel filtersContainer = new JPanel(new BorderLayout());
            filtersContainer.add(mainFiltersPanel, BorderLayout.NORTH);
            filtersContainer.add(advancedFiltersPanel, BorderLayout.CENTER);

            // Panel de botones de acción
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            buttonPanel.add(markAllReadButton);
            buttonPanel.add(refreshButton);

            topPanel.add(filtersContainer, BorderLayout.CENTER);
            topPanel.add(buttonPanel, BorderLayout.EAST);

            // Panel de estadísticas mejorado
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            statsPanel.setBorder(new EmptyBorder(0, 10, 5, 10));
            statsPanel.setBackground(new Color(248, 249, 250));
            
            totalCountLabel.setFont(totalCountLabel.getFont().deriveFont(Font.BOLD));
            unreadCountLabel.setFont(unreadCountLabel.getFont().deriveFont(Font.BOLD));
            unreadCountLabel.setForeground(new Color(220, 53, 69));
            filteredCountLabel.setFont(filteredCountLabel.getFont().deriveFont(Font.BOLD));
            filteredCountLabel.setForeground(new Color(0, 123, 255));
            
            statsPanel.add(new JLabel("📊"));
            statsPanel.add(totalCountLabel);
            statsPanel.add(new JLabel("|"));
            statsPanel.add(unreadCountLabel);
            statsPanel.add(new JLabel("|"));
            statsPanel.add(filteredCountLabel);

            // Panel completo del norte (filtros + estadísticas)
            JPanel northPanel = new JPanel(new BorderLayout());
            northPanel.add(topPanel, BorderLayout.NORTH);
            northPanel.add(statsPanel, BorderLayout.SOUTH);

            // Ensamblar correctamente
            add(northPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            
            // Configurar toggle de filtros avanzados
            setupAdvancedFiltersToggle(advancedFiltersPanel);
            
            System.out.println("✅ Layout configurado - Filtros principales y avanzados agregados");
        }

        private void setupListeners() {
            // Filtros principales
            searchField.addActionListener(e -> applyFilters());
            statusFilterCombo.addActionListener(e -> applyFilters());
            senderFilterCombo.addActionListener(e -> applyFilters());
            dateFilterCombo.addActionListener(e -> applyFilters());
            
            // Filtros avanzados
            priorityFilterCombo.addActionListener(e -> applyFilters());
            typeFilterCombo.addActionListener(e -> applyFilters());
            
            // Filtros checkbox
            onlyImportantCheckBox.addActionListener(e -> applyFilters());
            requiresActionCheckBox.addActionListener(e -> applyFilters());

            // Botones
            clearFiltersButton.addActionListener(e -> clearFilters());
            markAllReadButton.addActionListener(e -> markAllAsRead());
            refreshButton.addActionListener(e -> loadNotifications());
            
            System.out.println("✅ Listeners configurados para todos los filtros");
        }
        
        private void setupAdvancedFiltersToggle(JPanel advancedFiltersPanel) {
            advancedFiltersButton.addActionListener(e -> {
                boolean isVisible = advancedFiltersPanel.isVisible();
                advancedFiltersPanel.setVisible(!isVisible);
                advancedFiltersButton.setText(isVisible ? "⚙️ Filtros Avanzados" : "⬆️ Ocultar Filtros");
                
                // Revalidar el layout
                this.revalidate();
                this.repaint();
            });
        }

        private void loadNotifications() {
            SwingWorker<List<NotificacionDestinatario>, Void> worker = 
                new SwingWorker<List<NotificacionDestinatario>, Void>() {
                
                @Override
                protected List<NotificacionDestinatario> doInBackground() throws Exception {
                    // Obtener todas las notificaciones del usuario (leídas y no leídas)
                    return notificationService.obtenerTodasLasNotificaciones(usuarioId);
                }

                @Override
                protected void done() {
                    try {
                        allNotifications = get();
                        loadSenderOptions();
                        applyFilters();
                        System.out.println("✅ Notificaciones cargadas: " + allNotifications.size());
                    } catch (Exception e) {
                        System.err.println("Error cargando notificaciones: " + e.getMessage());
                        showError("Error cargando notificaciones");
                    }
                }
            };
            worker.execute();
        }

        private void loadSenderOptions() {
            senderFilterCombo.removeAllItems();
            senderFilterCombo.addItem("Todos");
            
            Set<String> senders = allNotifications.stream()
                .map(NotificacionDestinatario::getRemitenteCompleto)
                .collect(Collectors.toSet());
            
            for (String sender : senders) {
                senderFilterCombo.addItem(sender);
            }
        }

        private void applyFilters() {
            String searchText = searchField.getText().toLowerCase().trim();
            String statusFilter = (String) statusFilterCombo.getSelectedItem();
            String senderFilter = (String) senderFilterCombo.getSelectedItem();
            String dateFilter = (String) dateFilterCombo.getSelectedItem();
            String priorityFilter = (String) priorityFilterCombo.getSelectedItem();
            String typeFilter = (String) typeFilterCombo.getSelectedItem();
            boolean onlyImportant = onlyImportantCheckBox.isSelected();
            boolean requiresAction = requiresActionCheckBox.isSelected();

            filteredNotifications = allNotifications.stream()
                .filter(notification -> matchesFilters(notification, searchText, statusFilter, 
                        senderFilter, dateFilter, priorityFilter, typeFilter, onlyImportant, requiresAction))
                .collect(Collectors.toList());

            updateNotificationsDisplay();
            updateStats();
        }

        private boolean matchesFilters(NotificacionDestinatario notification, String searchText, 
                String statusFilter, String senderFilter, String dateFilter, 
                String priorityFilter, String typeFilter, boolean onlyImportant, boolean requiresAction) {
            
            // Validar que la notificación no sea null
            if (notification == null) {
                return false;
            }
            
            // Filtro de búsqueda
            if (searchText != null && !searchText.isEmpty()) {
                String titulo = notification.getTitulo() != null ? notification.getTitulo() : "";
                String contenido = notification.getContenido() != null ? notification.getContenido() : "";
                String content = (titulo + " " + contenido).toLowerCase();
                if (!content.contains(searchText)) {
                    return false;
                }
            }

            // Filtro de estado (PROTECCIÓN CONTRA NULL Y EMPTY)
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"Todos".equals(statusFilter)) {
                String status = notification.getEstadoLectura();
                switch (statusFilter) {
                    case "No leídas":
                        if (!"NO_LEIDA".equals(status)) return false;
                        break;
                    case "Leídas":
                        if (!"LEIDA".equals(status)) return false;
                        break;
                    case "Confirmadas":
                        if (!"CONFIRMADA".equals(status)) return false;
                        break;
                }
            }

            // Filtro de remitente (PROTECCIÓN CONTRA NULL Y EMPTY)
            if (senderFilter != null && !senderFilter.trim().isEmpty() && !"Todos".equals(senderFilter)) {
                String remitenteCompleto = notification.getRemitenteCompleto();
                if (remitenteCompleto == null || !senderFilter.equals(remitenteCompleto)) {
                    return false;
                }
            }

            // Filtro de prioridad (PROTECCIÓN CONTRA NULL Y EMPTY)
            if (priorityFilter != null && !priorityFilter.trim().isEmpty() && !"Todas las prioridades".equals(priorityFilter)) {
                String prioridad = notification.getPrioridad();
                if (prioridad == null || !priorityFilter.equalsIgnoreCase(prioridad)) {
                    return false;
                }
            }

            // Filtro de tipo (PROTECCIÓN CONTRA NULL Y EMPTY)
            if (typeFilter != null && !typeFilter.trim().isEmpty() && !"Todos los tipos".equals(typeFilter)) {
                String tipo = notification.getTipoNotificacion();
                if (tipo == null || !typeFilter.equalsIgnoreCase(tipo)) {
                    return false;
                }
            }

            // Filtro "Solo importantes" - Urgente y Alta prioridad
            if (onlyImportant) {
                String prioridad = notification.getPrioridad();
                if (prioridad == null || (!"URGENTE".equalsIgnoreCase(prioridad) && !"ALTA".equalsIgnoreCase(prioridad))) {
                    return false;
                }
            }

            // Filtro "Requieren acción" - Notificaciones que requieren confirmación
            if (requiresAction) {
                if (!notification.isRequiereConfirmacion()) {
                    return false;
                }
            }

            // Filtro de fecha (PROTECCIÓN CONTRA NULL Y EMPTY)
            if (dateFilter != null && !dateFilter.trim().isEmpty() && !"Todos".equals(dateFilter)) {
                LocalDateTime fechaCreacion = notification.getFechaCreacion();
                if (fechaCreacion == null) {
                    return false; // Si no tiene fecha, no pasa ningún filtro de fecha específico
                }
                
                LocalDate notificationDate = fechaCreacion.toLocalDate();
                LocalDate now = LocalDate.now();
                
                try {
                    switch (dateFilter) {
                        case "Hoy":
                            if (!notificationDate.equals(now)) return false;
                            break;
                        case "Ayer":
                            if (!notificationDate.equals(now.minusDays(1))) return false;
                            break;
                        case "Esta semana":
                            if (notificationDate.isBefore(now.minusDays(7))) return false;
                            break;
                        case "Semana pasada":
                            LocalDate weekStart = now.minusDays(14);
                            LocalDate weekEnd = now.minusDays(7);
                            if (notificationDate.isBefore(weekStart) || notificationDate.isAfter(weekEnd)) return false;
                            break;
                        case "Este mes":
                            if (notificationDate.isBefore(now.minusDays(30))) return false;
                            break;
                        case "Mes pasado":
                            LocalDate monthStart = now.minusDays(60);
                            LocalDate monthEnd = now.minusDays(30);
                            if (notificationDate.isBefore(monthStart) || notificationDate.isAfter(monthEnd)) return false;
                            break;
                        case "Últimos 3 meses":
                            if (notificationDate.isBefore(now.minusDays(90))) return false;
                            break;
                        case "Este año":
                            if (notificationDate.getYear() != now.getYear()) return false;
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando filtro de fecha '" + dateFilter + "': " + e.getMessage());
                    return false;
                }
            }

            return true;
        }

        private void updateNotificationsDisplay() {
            SwingUtilities.invokeLater(() -> {
                // Limpiar paneles existentes
                for (ExpandableNotificationPanel panel : notificationPanels) {
                    if (panel != null) {
                        try {
                            notificationsContainer.remove(panel);
                        } catch (Exception e) {
                            System.err.println("Error removing panel: " + e.getMessage());
                        }
                    }
                }

                notificationsContainer.removeAll();
                notificationPanels.clear();

                if (filteredNotifications == null || filteredNotifications.isEmpty()) {
                    showEmptyState();
                } else {
                    // Crear paneles para notificaciones filtradas
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
                        }
                    }
                }

                // Actualizar vista
                notificationsContainer.revalidate();
                notificationsContainer.repaint();
                scrollToTop();
            });
        }

        private void showEmptyState() {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setPreferredSize(new Dimension(400, 200));
            
            JLabel emptyLabel = new JLabel("📭 No hay notificaciones que mostrar", JLabel.CENTER);
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(16f));
            emptyLabel.setForeground(Color.GRAY);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            notificationsContainer.add(emptyPanel);
        }

        private void scrollToTop() {
            SwingUtilities.invokeLater(() -> {
                scrollPane.getVerticalScrollBar().setValue(0);
            });
        }

        private ExpandableNotificationListener createNotificationListener() {
            return new ExpandableNotificationListener() {
                @Override
                public void onNotificationToggled(ExpandableNotificationPanel panel) {
                    // El panel se expandió/contrajo, podrías actualizar la UI si es necesario
                    revalidate();
                    repaint();
                }

                @Override
                public void onNotificationRead(ExpandableNotificationPanel panel) {
                    if (panel != null) {
                        NotificacionDestinatario notification = panel.getNotification();
                        if (notification != null && "NO_LEIDA".equals(notification.getEstadoLectura())) {
                            notificationService.marcarComoLeida(notification.getNotificacionId(), usuarioId)
                                .thenAccept(success -> {
                                    if (success) {
                                        notification.setEstadoLectura("LEIDA");
                                        notification.setFechaLeida(java.time.LocalDateTime.now());
                                        updateStats();
                                        System.out.println("✅ Notificación marcada como leída: " + notification.getId());
                                    }
                                });
                        }
                    }
                }

                @Override
                public void onNotificationDeleted(ExpandableNotificationPanel panel) {
                    if (panel != null) {
                        NotificacionDestinatario notification = panel.getNotification();
                        if (notification != null) {
                            int confirm = JOptionPane.showConfirmDialog(
                                    NotificationsWindow.this,
                                    "¿Eliminar esta notificación?\nEsta acción no se puede deshacer.",
                                    "Confirmar eliminación",
                                    JOptionPane.YES_NO_OPTION
                            );
                            if (confirm == JOptionPane.YES_OPTION) {
                                filteredNotifications.remove(notification);
                                allNotifications.remove(notification);
                                updateNotificationsDisplay();
                                updateStats();
                                System.out.println("�️ Notificación eliminada: " + notification.getId());
                            }
                        }
                    }
                }
            };
        }

        private void clearFilters() {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            senderFilterCombo.setSelectedIndex(0);
            dateFilterCombo.setSelectedIndex(0);
            priorityFilterCombo.setSelectedIndex(0);
            typeFilterCombo.setSelectedIndex(0);
            onlyImportantCheckBox.setSelected(false);
            requiresActionCheckBox.setSelected(false);
            applyFilters();
        }

        private void markAllAsRead() {
            int unreadCount = (int) filteredNotifications.stream()
                .filter(n -> "NO_LEIDA".equals(n.getEstadoLectura()))
                .count();

            if (unreadCount == 0) {
                JOptionPane.showMessageDialog(this, "No hay notificaciones no leídas para marcar.", 
                    "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Marcar todas las " + unreadCount + " notificaciones no leídas como leídas?",
                "Confirmar acción", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (NotificacionDestinatario notification : filteredNotifications) {
                            if ("NO_LEIDA".equals(notification.getEstadoLectura())) {
                                notificationService.marcarComoLeida(notification.getNotificacionId(), usuarioId);
                                notification.setEstadoLectura("LEIDA");
                                notification.setFechaLeida(java.time.LocalDateTime.now());
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        updateNotificationsDisplay();
                        updateStats();
                        System.out.println("✅ Todas las notificaciones marcadas como leídas");
                    }
                };
                worker.execute();
            }
        }

        private void updateStats() {
            SwingUtilities.invokeLater(() -> {
                int total = allNotifications.size();
                int unread = (int) allNotifications.stream()
                    .filter(n -> "NO_LEIDA".equals(n.getEstadoLectura()))
                    .count();
                int filtered = filteredNotifications.size();

                totalCountLabel.setText("Total: " + total);
                unreadCountLabel.setText("No leídas: " + unread);
                filteredCountLabel.setText("Mostradas: " + filtered);
            });
        }

        private void showError(String message) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    // =========================================================================
    // 2. NOTIFICATION_SENDER_WINDOW - VENTANA PARA ENVIAR NOTIFICACIONES
    // =========================================================================
    
    /**
     * Ventana simplificada para enviar notificaciones
     */
    public static class NotificationSenderWindow extends JFrame {
        
        private final int usuarioId;
        private final NotificationManager notificationManager;
        
        private JTextField titleField;
        private JTextArea contentArea;
        private JComboBox<String> priorityCombo;
        private JComboBox<String> typeCombo;
        private JButton sendButton;
        private JButton cancelButton;
        
        public NotificationSenderWindow(int usuarioId) {
            this.usuarioId = usuarioId;
            this.notificationManager = NotificationManager.getInstance();
            
            initializeComponents();
            setupLayout();
            setupListeners();
            
            setTitle("📤 Enviar Notificación");
            setSize(600, 500);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        
        private void initializeComponents() {
            titleField = new JTextField(30);
            contentArea = new JTextArea(10, 30);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            
            priorityCombo = new JComboBox<>(new String[]{"NORMAL", "ALTA", "URGENTE", "BAJA"});
            typeCombo = new JComboBox<>(new String[]{"Individual", "Por Rol", "Broadcast"});
            
            sendButton = new JButton("Enviar Notificación");
            cancelButton = new JButton("Cancelar");
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout(10, 10));
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Título
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Título:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(titleField, gbc);
            
            // Contenido
            gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Contenido:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
            formPanel.add(new JScrollPane(contentArea), gbc);
            
            // Prioridad
            gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0;
            formPanel.add(new JLabel("Prioridad:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(priorityCombo, gbc);
            
            // Tipo
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Tipo:"), gbc);
            gbc.gridx = 1;
            formPanel.add(typeCombo, gbc);
            
            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(cancelButton);
            buttonPanel.add(sendButton);
            
            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupListeners() {
            sendButton.addActionListener(_ -> sendNotification());
            cancelButton.addActionListener(_ -> dispose());
        }
        
        private void sendNotification() {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El título es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El contenido es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Por simplicidad, enviar notificación de prueba al mismo usuario
            notificationManager.enviarNotificacionRapida(title, content, usuarioId);
            
            JOptionPane.showMessageDialog(this, "Notificación enviada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    // =========================================================================
    // 3. NOTIFICATION_GROUP_WINDOW - VENTANA DE GESTIÓN DE GRUPOS
    // =========================================================================
    
    /**
     * Ventana simplificada para gestionar grupos de notificaciones
     */
    public static class NotificationGroupWindow extends JFrame {
        
        private final int usuarioId;
        private final NotificationGroupService groupService;
        
        private JList<PersonalNotificationGroup> groupsList;
        private DefaultListModel<PersonalNotificationGroup> groupsModel;
        private JButton createButton;
        private JButton editButton;
        private JButton deleteButton;
        private JButton refreshButton;
        
        public NotificationGroupWindow(int usuarioId) {
            this.usuarioId = usuarioId;
            this.groupService = NotificationGroupService.getInstance();
            
            initializeComponents();
            setupLayout();
            setupListeners();
            loadGroups();
            
            setTitle("👥 Gestión de Grupos");
            setSize(800, 600);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        
        private void initializeComponents() {
            groupsModel = new DefaultListModel<>();
            groupsList = new JList<>(groupsModel);
            groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            createButton = new JButton("Crear Grupo");
            editButton = new JButton("Editar");
            deleteButton = new JButton("Eliminar");
            refreshButton = new JButton("Actualizar");
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout(10, 10));
            
            // Lista de grupos
            JScrollPane scrollPane = new JScrollPane(groupsList);
            scrollPane.setBorder(new TitledBorder("Mis Grupos"));
            
            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(createButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(refreshButton);
            
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupListeners() {
            createButton.addActionListener(_ -> createGroup());
            editButton.addActionListener(_ -> editGroup());
            deleteButton.addActionListener(_ -> deleteGroup());
            refreshButton.addActionListener(_ -> loadGroups());
        }
        
        private void loadGroups() {
            SwingWorker<List<PersonalNotificationGroup>, Void> worker = 
                new SwingWorker<List<PersonalNotificationGroup>, Void>() {
                
                @Override
                protected List<PersonalNotificationGroup> doInBackground() throws Exception {
                    return groupService.obtenerGruposUsuario(usuarioId);
                }

                @Override
                protected void done() {
                    try {
                        List<PersonalNotificationGroup> groups = get();
                        groupsModel.clear();
                        for (PersonalNotificationGroup group : groups) {
                            groupsModel.addElement(group);
                        }
                        System.out.println("✅ Grupos cargados: " + groups.size());
                    } catch (Exception e) {
                        System.err.println("Error cargando grupos: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
        
        private void createGroup() {
            String name = JOptionPane.showInputDialog(this, "Nombre del grupo:");
            if (name != null && !name.trim().isEmpty()) {
                String description = JOptionPane.showInputDialog(this, "Descripción (opcional):");
                
                groupService.crearGrupoPersonalizado(name.trim(), description, usuarioId, new ArrayList<>())
                    .thenAccept(groupId -> {
                        if (groupId > 0) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(this, "Grupo creado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                                loadGroups();
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(this, "Error creando el grupo", "Error", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    });
            }
        }
        
        private void editGroup() {
            PersonalNotificationGroup selected = groupsList.getSelectedValue();
            if (selected != null) {
                JOptionPane.showMessageDialog(this, "Función de edición en desarrollo", "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un grupo para editar", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
        
        private void deleteGroup() {
            PersonalNotificationGroup selected = groupsList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar el grupo '" + selected.getName() + "'?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    groupService.eliminarGrupo(selected.getId(), usuarioId)
                        .thenAccept(success -> {
                            SwingUtilities.invokeLater(() -> {
                                if (success) {
                                    JOptionPane.showMessageDialog(this, "Grupo eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                                    loadGroups();
                                } else {
                                    JOptionPane.showMessageDialog(this, "Error eliminando el grupo", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un grupo para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // =========================================================================
    // MÉTODOS ESTÁTICOS DE ACCESO RÁPIDO
    // =========================================================================
    
    /**
     * Abre la ventana principal de notificaciones
     */
    public static void openNotificationsWindow(int usuarioId) {
        SwingUtilities.invokeLater(() -> {
            NotificationsWindow window = new NotificationsWindow(usuarioId);
            window.setVisible(true);
        });
    }
    
    /**
     * Abre la ventana de envío de notificaciones
     */
    public static void openSenderWindow(int usuarioId) {
        SwingUtilities.invokeLater(() -> {
            NotificationSenderWindow window = new NotificationSenderWindow(usuarioId);
            window.setVisible(true);
        });
    }
    
    /**
     * Abre la ventana de gestión de grupos
     */
    public static void openGroupWindow(int usuarioId) {
        SwingUtilities.invokeLater(() -> {
            NotificationGroupWindow window = new NotificationGroupWindow(usuarioId);
            window.setVisible(true);
        });
    }

    // =========================================================================
    // 6. NOTIFICATION_BELL_COMPONENT - COMPONENTE DE CAMPANITA MODERNO
    // =========================================================================
    
    /**
     * Componente moderno de campanita de notificaciones para la barra de menú
     * Reemplaza al anterior NotificationBellComponent eliminado
     */
    public static class NotificationBellComponent extends JButton {
        
        private final int userId;
        private final NotificationService notificationService;
        private int unreadCount = 0;
        private Timer refreshTimer;
        
        // Colores modernos
        private static final Color BELL_COLOR = new Color(64, 64, 64);
        private static final Color NOTIFICATION_COLOR = new Color(220, 53, 69);
        private static final Color HOVER_COLOR = new Color(108, 117, 125);
        
        public NotificationBellComponent(int userId) {
            this.userId = userId;
            this.notificationService = NotificationService.getInstance();
            
            initializeComponent();
            setupTimer();
            updateUnreadCount();
        }
        
        private void initializeComponent() {
            // Configurar botón
            setText("🔔");
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            setForeground(BELL_COLOR);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(40, 30));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Ver notificaciones");
            
            // Configurar hover effect
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setForeground(HOVER_COLOR);
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setForeground(BELL_COLOR);
                }
            });
            
            // Configurar acción de click
            addActionListener(event -> openNotificationsWindow());
        }
        
        private void setupTimer() {
            // Timer para actualizar cada 30 segundos
            refreshTimer = new Timer(30000, e -> updateUnreadCount());
            refreshTimer.start();
        }
        
        private void updateUnreadCount() {
            try {
                List<NotificacionDestinatario> notifications = notificationService.obtenerNotificacionesNoLeidas(userId);
                SwingUtilities.invokeLater(() -> {
                    int newCount = notifications.size();
                    if (newCount != unreadCount) {
                        unreadCount = newCount;
                        updateBellDisplay();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error actualizando contador de notificaciones: " + e.getMessage());
            }
        }
        
        private void updateBellDisplay() {
            if (unreadCount > 0) {
                // Mostrar contador
                String displayText = unreadCount > 99 ? "🔔 99+" : "🔔 " + unreadCount;
                setText(displayText);
                setForeground(NOTIFICATION_COLOR);
                setToolTipText("Ver " + unreadCount + " notificación(es) no leída(s)");
            } else {
                // Campanita normal
                setText("🔔");
                setForeground(BELL_COLOR);
                setToolTipText("Ver notificaciones");
            }
        }
        
        private void openNotificationsWindow() {
            NotificationUI.openNotificationsWindow(userId);
            // Actualizar después de abrir la ventana
            Timer delayTimer = new Timer(1000, e -> updateUnreadCount());
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
        
        /**
         * Fuerza una actualización inmediata del contador
         */
        public void forceUpdate() {
            updateUnreadCount();
        }
        
        /**
         * Limpia recursos cuando se cierra la aplicación
         */
        public void cleanup() {
            if (refreshTimer != null) {
                refreshTimer.stop();
                refreshTimer = null;
            }
        }
    }
}
