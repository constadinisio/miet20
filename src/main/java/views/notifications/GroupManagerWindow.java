package main.java.views.notifications;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import main.java.components.HierarchicalUserSelector;
import main.java.models.PersonalNotificationGroup;
import main.java.services.NotificationGroupService;

/**
 * Ventana completa para gestionar grupos personalizados de notificaciones
 * Permite crear, editar, eliminar y administrar grupos de usuarios para envío
 * de notificaciones
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Interfaz mejorada con selector jerárquico
 */
public class GroupManagerWindow extends JFrame {

    private final int userId;
    private final int userRole;
    private final NotificationGroupService groupService;

    // Componentes UI principales
    private JTable groupsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JButton createGroupButton;
    private JButton editGroupButton;
    private JButton deleteGroupButton;
    private JButton duplicateGroupButton;
    private JButton refreshButton;

    // Panel de detalles/edición
    private JPanel detailsPanel;
    private CardLayout detailsCardLayout;
    private JTextField groupNameField;
    private JTextArea groupDescriptionArea;
    private HierarchicalUserSelector userSelector;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statsLabel;

    // Estado de la ventana
    private List<PersonalNotificationGroup> currentGroups;
    private PersonalNotificationGroup editingGroup;
    private boolean isEditing = false;

    // Constantes de diseño
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);

    public GroupManagerWindow(int userId, int userRole) {
        this.userId = userId;
        this.userRole = userRole;
        this.groupService = NotificationGroupService.getInstance();
        this.currentGroups = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadGroups();

        setTitle("📁 Gestionar Grupos de Notificaciones");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        System.out.println("✅ GroupManagerWindow inicializado para usuario: " + userId);
    }

    /**
     * Inicializa todos los componentes de la interfaz
     */
    private void initializeComponents() {
        // Configurar tabla de grupos
        initializeGroupsTable();

        // Configurar paneles de control
        initializeControlPanels();

        // Configurar panel de detalles
        initializeDetailsPanel();

        System.out.println("🔧 Componentes inicializados");
    }

    /**
     * Configura la tabla de grupos
     */
    private void initializeGroupsTable() {
        String[] columns = {"Nombre", "Descripción", "Miembros", "Creado", "Último Uso", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2:
                        return Integer.class; // Miembros (para ordenamiento numérico)
                    case 3:
                    case 4:
                        return String.class; // Fechas como String formateadas
                    default:
                        return String.class;
                }
            }
        };

        groupsTable = new JTable(tableModel);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsTable.setRowHeight(28);
        groupsTable.setShowGrid(true);
        groupsTable.setGridColor(new Color(220, 220, 220));

        // Configurar sorter para ordenamiento
        tableSorter = new TableRowSorter<>(tableModel);
        groupsTable.setRowSorter(tableSorter);

        // Configurar anchos de columnas
        groupsTable.getColumnModel().getColumn(0).setPreferredWidth(180); // Nombre
        groupsTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Descripción
        groupsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Miembros
        groupsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Creado
        groupsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Último uso
        groupsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Estado

        // Estilo del header
        groupsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        groupsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        groupsTable.getTableHeader().setForeground(Color.BLACK);
    }

    /**
     * Configura los paneles de control
     */
    private void initializeControlPanels() {
        // Campo de búsqueda
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Buscar grupos por nombre o descripción");

        // Botones de acción principales
        createGroupButton = createStyledButton("➕ Crear Grupo", SUCCESS_COLOR);
        editGroupButton = createStyledButton("✏️ Editar", PRIMARY_COLOR);
        deleteGroupButton = createStyledButton("🗑️ Eliminar", DANGER_COLOR);
        duplicateGroupButton = createStyledButton("📋 Duplicar", SECONDARY_COLOR);
        refreshButton = createStyledButton("🔄 Actualizar", new Color(23, 162, 184));

        // Estados iniciales
        editGroupButton.setEnabled(false);
        deleteGroupButton.setEnabled(false);
        duplicateGroupButton.setEnabled(false);

        // Label de estadísticas
        statsLabel = new JLabel("Grupos: 0 • Total miembros: 0");
        statsLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statsLabel.setForeground(SECONDARY_COLOR);
    }

    /**
     * Crea un botón con estilo consistente
     */
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 32));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(originalColor.brighter());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    /**
     * Configura el panel de detalles con CardLayout
     */
    private void initializeDetailsPanel() {
        detailsCardLayout = new CardLayout();
        detailsPanel = new JPanel(detailsCardLayout);
        detailsPanel.setBorder(BorderFactory.createTitledBorder("📝 Detalles del Grupo"));
        detailsPanel.setPreferredSize(new Dimension(500, 0));

        // Crear tarjetas
        createEmptyCard();
        createEditingCard();

        // Mostrar tarjeta vacía inicialmente
        detailsCardLayout.show(detailsPanel, "EMPTY");
    }

    /**
     * Crea la tarjeta cuando no hay grupo seleccionado
     */
    private void createEmptyCard() {
        JPanel emptyCard = new JPanel(new BorderLayout());
        emptyCard.setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        JLabel emptyIcon = new JLabel("📁", SwingConstants.CENTER);
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        emptyIcon.setForeground(new Color(200, 200, 200));

        JLabel emptyText = new JLabel("Selecciona un grupo o crea uno nuevo", SwingConstants.CENTER);
        emptyText.setFont(new Font("Arial", Font.ITALIC, 14));
        emptyText.setForeground(SECONDARY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        centerPanel.add(emptyIcon, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(emptyText, gbc);

        emptyCard.add(centerPanel, BorderLayout.CENTER);
        detailsPanel.add(emptyCard, "EMPTY");
    }

    /**
     * Crea la tarjeta de edición de grupos
     */
    private void createEditingCard() {
        JPanel editingCard = new JPanel(new BorderLayout());
        editingCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel del formulario
        JPanel formPanel = createFormPanel();

        // Panel del selector de usuarios
        userSelector = new HierarchicalUserSelector(userId);
        userSelector.setBorder(BorderFactory.createTitledBorder("👥 Seleccionar Miembros"));

        // Panel de botones
        JPanel buttonPanel = createButtonPanel();

        // Ensamblar
        editingCard.add(formPanel, BorderLayout.NORTH);
        editingCard.add(userSelector, BorderLayout.CENTER);
        editingCard.add(buttonPanel, BorderLayout.SOUTH);

        detailsPanel.add(editingCard, "EDITING");
    }

    /**
     * Crea el panel del formulario básico
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("ℹ️ Información Básica"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre del grupo
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nombre:"), gbc);

        groupNameField = new JTextField(25);
        groupNameField.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(groupNameField, gbc);

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Descripción:"), gbc);

        groupDescriptionArea = new JTextArea(3, 25);
        groupDescriptionArea.setLineWrap(true);
        groupDescriptionArea.setWrapStyleWord(true);
        groupDescriptionArea.setFont(new Font("Arial", Font.PLAIN, 12));
        groupDescriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JScrollPane descScrollPane = new JScrollPane(groupDescriptionArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        formPanel.add(descScrollPane, gbc);

        return formPanel;
    }

    /**
     * Crea el panel de botones de acción
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        saveButton = createStyledButton("💾 Guardar", SUCCESS_COLOR);
        cancelButton = createStyledButton("❌ Cancelar", SECONDARY_COLOR);

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        return buttonPanel;
    }

    /**
     * Configura el layout principal de la ventana
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header con título y estadísticas
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Panel principal con split
        JSplitPane mainSplitPane = createMainSplitPane();
        add(mainSplitPane, BorderLayout.CENTER);

        // Footer con información adicional
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        System.out.println("📐 Layout configurado");
    }

    /**
     * Crea el panel de encabezado
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Título
        JLabel titleLabel = new JLabel("📁 Gestionar Grupos de Notificaciones");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // Subtítulo
        JLabel subtitleLabel = new JLabel("Crea y administra grupos personalizados para envío de notificaciones");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 255));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statsLabel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Crea el split pane principal
     */
    private JSplitPane createMainSplitPane() {
        // Panel izquierdo - Lista de grupos
        JPanel leftPanel = createGroupsListPanel();

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, detailsPanel);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);

        return splitPane;
    }

    /**
     * Crea el panel de lista de grupos
     */
    private JPanel createGroupsListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de controles superiores
        JPanel topControlsPanel = new JPanel(new BorderLayout());
        topControlsPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        // Búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("🔍"));
        searchPanel.add(searchField);
        topControlsPanel.add(searchPanel, BorderLayout.WEST);

        // Botones de acción
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionButtonsPanel.add(refreshButton);
        actionButtonsPanel.add(createGroupButton);
        topControlsPanel.add(actionButtonsPanel, BorderLayout.EAST);

        // Tabla en scroll pane
        JScrollPane tableScrollPane = new JScrollPane(groupsTable);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220))
        ));

        // Panel de botones inferiores
        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomButtonsPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        bottomButtonsPanel.add(editGroupButton);
        bottomButtonsPanel.add(duplicateGroupButton);
        bottomButtonsPanel.add(deleteGroupButton);

        panel.add(topControlsPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(bottomButtonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de pie de página
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel tipLabel = new JLabel("💡 Consejo: Haz doble clic en un grupo para editarlo rápidamente");
        tipLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        tipLabel.setForeground(SECONDARY_COLOR);

        footerPanel.add(tipLabel);
        return footerPanel;
    }

    /**
     * Configura todos los listeners de eventos
     */
    private void setupListeners() {
        // Búsqueda en tiempo real
        setupSearchListener();

        // Botones principales
        setupMainButtonListeners();

        // Tabla de grupos
        setupTableListeners();

        // Botones de edición
        setupEditingButtonListeners();

        System.out.println("👂 Listeners configurados");
    }

    /**
     * Configura el listener de búsqueda
     */
    private void setupSearchListener() {
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterGroups();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterGroups();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterGroups();
            }
        });
    }

    /**
     * Configura listeners de botones principales
     */
    private void setupMainButtonListeners() {
        createGroupButton.addActionListener(this::startCreatingGroup);
        editGroupButton.addActionListener(this::startEditingGroup);
        deleteGroupButton.addActionListener(this::deleteSelectedGroup);
        duplicateGroupButton.addActionListener(this::duplicateSelectedGroup);
        refreshButton.addActionListener(e -> loadGroups());
    }

    /**
     * Configura listeners de la tabla
     */
    private void setupTableListeners() {
        // Selección
        groupsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Doble clic para editar
        groupsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && !isEditing) {
                    startEditingGroup(null);
                }
            }
        });
    }

    /**
     * Configura listeners de botones de edición
     */
    private void setupEditingButtonListeners() {
        saveButton.addActionListener(this::saveGroup);
        cancelButton.addActionListener(this::cancelEditing);
    }

    /**
     * Carga los grupos del usuario desde el servicio
     */
    private void loadGroups() {
        SwingWorker<List<PersonalNotificationGroup>, Void> worker
                = new SwingWorker<List<PersonalNotificationGroup>, Void>() {
            @Override
            protected List<PersonalNotificationGroup> doInBackground() throws Exception {
                return groupService.getUserGroups(userId).get();
            }

            @Override
            protected void done() {
                try {
                    currentGroups = get();
                    updateTable();
                    updateStats();
                    System.out.println("✅ Grupos cargados: " + currentGroups.size());
                } catch (Exception e) {
                    System.err.println("Error cargando grupos: " + e.getMessage());
                    showErrorMessage("Error al cargar los grupos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Actualiza la tabla con los grupos actuales
     */
    private void updateTable() {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (PersonalNotificationGroup group : currentGroups) {
            String createdAt = group.getCreatedAt().format(formatter);
            String lastUsed = group.getLastUsed() != null
                    ? group.getLastUsed().format(formatter) : "Nunca";

            // Truncar descripción si es muy larga
            String description = group.getDescription();
            if (description != null && description.length() > 40) {
                description = description.substring(0, 37) + "...";
            } else if (description == null) {
                description = "";
            }

            String status = group.getMemberCount() > 0 ? "✅ Activo" : "⚠️ Vacío";

            Object[] row = {
                group.getName(),
                description,
                group.getMemberCount(),
                createdAt,
                lastUsed,
                status
            };

            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    /**
     * Actualiza las estadísticas mostradas
     */
    private void updateStats() {
        int totalMembers = currentGroups.stream()
                .mapToInt(PersonalNotificationGroup::getMemberCount)
                .sum();

        String statsText = String.format("Grupos: %d • Total miembros: %d",
                currentGroups.size(), totalMembers);
        statsLabel.setText(statsText);
        statsLabel.setForeground(Color.WHITE);
    }

    /**
     * Actualiza el estado de los botones según la selección
     */
    private void updateButtonStates() {
        boolean hasSelection = groupsTable.getSelectedRow() >= 0;
        editGroupButton.setEnabled(hasSelection && !isEditing);
        deleteGroupButton.setEnabled(hasSelection && !isEditing);
        duplicateGroupButton.setEnabled(hasSelection && !isEditing);
    }

    /**
     * Filtra los grupos según el texto de búsqueda
     */
    private void filterGroups() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    /**
     * Inicia la creación de un nuevo grupo
     */
    private void startCreatingGroup(ActionEvent e) {
        if (isEditing) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Hay cambios sin guardar. ¿Deseas cancelar la edición actual?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        editingGroup = null;
        isEditing = true;

        // Limpiar formulario
        groupNameField.setText("");
        groupDescriptionArea.setText("");
        userSelector.clearSelection();

        // Cambiar a modo edición
        detailsCardLayout.show(detailsPanel, "EDITING");
        detailsPanel.setBorder(BorderFactory.createTitledBorder("➕ Crear Nuevo Grupo"));
        saveButton.setText("💾 Crear Grupo");

        updateButtonStates();
        groupNameField.requestFocus();

        System.out.println("🆕 Iniciando creación de grupo");
    }

    /**
     * Inicia la edición del grupo seleccionado
     */
    private void startEditingGroup(ActionEvent e) {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarningMessage("Selecciona un grupo para editar");
            return;
        }

        if (isEditing) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Hay cambios sin guardar. ¿Deseas cancelar la edición actual?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Convertir índice de vista a modelo (por el filtro)
        int modelRow = groupsTable.convertRowIndexToModel(selectedRow);
        editingGroup = currentGroups.get(modelRow);
        isEditing = true;

        // Cargar datos del grupo
        groupNameField.setText(editingGroup.getName());
        groupDescriptionArea.setText(editingGroup.getDescription());

        // Cargar miembros seleccionados (async)
        loadGroupMembers(editingGroup.getId());

        // Cambiar a modo edición
        detailsCardLayout.show(detailsPanel, "EDITING");
        detailsPanel.setBorder(BorderFactory.createTitledBorder("✏️ Editar: " + editingGroup.getName()));
        saveButton.setText("💾 Actualizar Grupo");

        updateButtonStates();
        groupNameField.requestFocus();

        System.out.println("✏️ Iniciando edición de grupo: " + editingGroup.getName());
    }

    /**
     * Carga los miembros de un grupo de forma asíncrona
     */
    private void loadGroupMembers(int groupId) {
        SwingWorker<List<Integer>, Void> worker = new SwingWorker<List<Integer>, Void>() {
            @Override
            protected List<Integer> doInBackground() throws Exception {
                return groupService.getGroupMemberIds(groupId).get();
            }

            @Override
            protected void done() {
                try {
                    List<Integer> memberIds = get();
                    userSelector.setSelectedUserIds(Set.copyOf(memberIds));
                    System.out.println("✅ Miembros cargados: " + memberIds.size());
                } catch (Exception e) {
                    System.err.println("Error cargando miembros: " + e.getMessage());
                    showErrorMessage("Error cargando miembros del grupo");
                }
            }
        };
        worker.execute();
    }

    /**
     * Guarda el grupo (crear o actualizar)
     */
    private void saveGroup(ActionEvent e) {
        if (!validateGroupData()) {
            return;
        }

        String name = groupNameField.getText().trim();
        String description = groupDescriptionArea.getText().trim();
        Set<Integer> memberIds = userSelector.getSelectedUserIds();

        // Confirmar si el grupo está vacío
        if (memberIds.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "El grupo no tiene miembros seleccionados.\n¿Deseas crear un grupo vacío?",
                    "Grupo Vacío", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Deshabilitar botón mientras se guarda
        saveButton.setEnabled(false);
        saveButton.setText("Guardando...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (editingGroup == null) {
                    // Crear nuevo grupo
                    return groupService.createGroup(name, description, userId,
                            new ArrayList<>(memberIds)).get() > 0;
                } else {
                    // Actualizar grupo existente
                    return groupService.updateGroup(editingGroup.getId(), name, description,
                            new ArrayList<>(memberIds), userId).get();
                }
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                saveButton.setText(editingGroup == null ? "💾 Crear Grupo" : "💾 Actualizar Grupo");

                try {
                    boolean success = get();
                    if (success) {
                        String message = editingGroup == null
                                ? "✅ Grupo '" + name + "' creado exitosamente"
                                : "✅ Grupo '" + name + "' actualizado exitosamente";

                        showSuccessMessage(message);
                        cancelEditing(null);
                        loadGroups();
                    } else {
                        String action = editingGroup == null ? "crear" : "actualizar";
                        showErrorMessage("Error al " + action + " el grupo. Intenta nuevamente.");
                    }
                } catch (Exception ex) {
                    System.err.println("Error guardando grupo: " + ex.getMessage());
                    showErrorMessage("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Valida los datos del formulario
     */
    private boolean validateGroupData() {
        String name = groupNameField.getText().trim();

        if (name.isEmpty()) {
            showWarningMessage("El nombre del grupo es obligatorio");
            groupNameField.requestFocus();
            return false;
        }

        if (name.length() > 100) {
            showWarningMessage("El nombre del grupo no puede exceder 100 caracteres");
            groupNameField.requestFocus();
            return false;
        }

        String description = groupDescriptionArea.getText().trim();
        if (description.length() > 500) {
            showWarningMessage("La descripción no puede exceder 500 caracteres");
            groupDescriptionArea.requestFocus();
            return false;
        }

        // Verificar nombre único (excepto para el grupo actual en edición)
        for (PersonalNotificationGroup group : currentGroups) {
            if (group.getName().equalsIgnoreCase(name)
                    && (editingGroup == null || group.getId() != editingGroup.getId())) {
                showWarningMessage("Ya tienes un grupo con ese nombre. Elige otro nombre.");
                groupNameField.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Cancela la edición actual
     */
    private void cancelEditing(ActionEvent e) {
        if (isEditing) {
            // Verificar si hay cambios sin guardar
            if (hasUnsavedChanges()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Hay cambios sin guardar. ¿Deseas cancelar sin guardar?",
                        "Confirmar Cancelación", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        }

        editingGroup = null;
        isEditing = false;

        // Volver a vista vacía
        detailsCardLayout.show(detailsPanel, "EMPTY");
        detailsPanel.setBorder(BorderFactory.createTitledBorder("📝 Detalles del Grupo"));

        updateButtonStates();

        System.out.println("❌ Edición cancelada");
    }

    /**
     * Verifica si hay cambios sin guardar
     */
    private boolean hasUnsavedChanges() {
        if (editingGroup == null) {
            // Nuevo grupo - verificar si hay datos ingresados
            return !groupNameField.getText().trim().isEmpty()
                    || !groupDescriptionArea.getText().trim().isEmpty()
                    || !userSelector.getSelectedUserIds().isEmpty();
        } else {
            // Grupo existente - verificar cambios
            boolean nameChanged = !editingGroup.getName().equals(groupNameField.getText().trim());
            String currentDesc = groupDescriptionArea.getText().trim();
            String originalDesc = editingGroup.getDescription() != null ? editingGroup.getDescription() : "";
            boolean descChanged = !originalDesc.equals(currentDesc);

            return nameChanged || descChanged;
        }
    }

    /**
     * Elimina el grupo seleccionado
     */
    private void deleteSelectedGroup(ActionEvent e) {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarningMessage("Selecciona un grupo para eliminar");
            return;
        }

        int modelRow = groupsTable.convertRowIndexToModel(selectedRow);
        PersonalNotificationGroup selectedGroup = currentGroups.get(modelRow);

        // Confirmación de eliminación
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar el grupo '" + selectedGroup.getName() + "'?\n\n"
                + "Esta acción no se puede deshacer.\n"
                + "El grupo tiene " + selectedGroup.getMemberCount() + " miembro(s).",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Eliminar de forma asíncrona
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return groupService.deleteGroup(selectedGroup.getId(), userId).get();
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccessMessage("✅ Grupo '" + selectedGroup.getName() + "' eliminado exitosamente");

                        // Si estábamos editando este grupo, cancelar edición
                        if (isEditing && editingGroup != null
                                && editingGroup.getId() == selectedGroup.getId()) {
                            cancelEditing(null);
                        }

                        loadGroups();
                    } else {
                        showErrorMessage("Error al eliminar el grupo. Intenta nuevamente.");
                    }
                } catch (Exception ex) {
                    System.err.println("Error eliminando grupo: " + ex.getMessage());
                    showErrorMessage("Error eliminando el grupo: " + ex.getMessage());
                }
            }
        };
        worker.execute();

        System.out.println("🗑️ Eliminando grupo: " + selectedGroup.getName());
    }

    /**
     * Duplica el grupo seleccionado
     */
    private void duplicateSelectedGroup(ActionEvent e) {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarningMessage("Selecciona un grupo para duplicar");
            return;
        }

        int modelRow = groupsTable.convertRowIndexToModel(selectedRow);
        PersonalNotificationGroup selectedGroup = currentGroups.get(modelRow);

        // Solicitar nombre para la copia
        String newName = JOptionPane.showInputDialog(this,
                "Nombre para la copia del grupo '" + selectedGroup.getName() + "':",
                "Duplicar Grupo",
                JOptionPane.PLAIN_MESSAGE);

        if (newName == null || newName.trim().isEmpty()) {
            return;
        }

        final String finalNewName = newName.trim();

        // Verificar que el nombre no exista
        for (PersonalNotificationGroup group : currentGroups) {
            if (group.getName().equalsIgnoreCase(newName)) {
                showWarningMessage("Ya tienes un grupo con ese nombre. Elige otro nombre.");
                return;
            }
        }

        // Duplicar de forma asíncrona
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return groupService.duplicateGroup(selectedGroup.getId(), finalNewName, userId).get() > 0;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccessMessage("✅ Grupo '" + finalNewName + "' creado como copia de '"
                                + selectedGroup.getName() + "'");
                        loadGroups();
                    } else {
                        showErrorMessage("Error al duplicar el grupo. Intenta nuevamente.");
                    }
                } catch (Exception ex) {
                    System.err.println("Error duplicando grupo: " + ex.getMessage());
                    showErrorMessage("Error duplicando el grupo: " + ex.getMessage());
                }
            }
        };
        worker.execute();

        System.out.println("📋 Duplicando grupo: " + selectedGroup.getName() + " -> " + finalNewName);
    }

    // ===============================================
    // MÉTODOS DE UTILIDAD PARA MENSAJES
    // ===============================================
    /**
     * Muestra un mensaje de éxito
     */
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un mensaje de advertencia
     */
    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Muestra un mensaje de error
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===============================================
    // MÉTODOS PÚBLICOS PARA INTEGRACIÓN
    // ===============================================
    /**
     * Obtiene la lista actual de grupos (para uso externo)
     */
    public List<PersonalNotificationGroup> getCurrentGroups() {
        return new ArrayList<>(currentGroups);
    }

    /**
     * Refresca los datos desde el exterior
     */
    public void refreshData() {
        loadGroups();
    }

    /**
     * Verifica si la ventana está en modo edición
     */
    public boolean isInEditingMode() {
        return isEditing;
    }

    /**
     * Obtiene el grupo que se está editando actualmente
     */
    public PersonalNotificationGroup getCurrentEditingGroup() {
        return editingGroup;
    }

    // ===============================================
    // LIMPIEZA DE RECURSOS
    // ===============================================
    /**
     * Limpia recursos al cerrar la ventana
     */
    @Override
    public void dispose() {
        try {
            // Cancelar edición si está activa
            if (isEditing) {
                isEditing = false;
            }

            // Limpiar datos
            if (currentGroups != null) {
                currentGroups.clear();
            }

            // Limpiar selector de usuarios
            if (userSelector != null) {
                userSelector.dispose();
            }

            System.out.println("🧹 Recursos de GroupManagerWindow liberados");

        } catch (Exception e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        } finally {
            super.dispose();
        }
    }
}
