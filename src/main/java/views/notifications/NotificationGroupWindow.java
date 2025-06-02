package main.java.views.notifications;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import main.java.controllers.NotificationGroupManager;
import main.java.models.NotificationGroup;

/**
 * Ventana para gestionar grupos de notificaciones personalizados
 *
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class NotificationGroupWindow extends JFrame {

    private final int userId;
    private final int userRole;
    private final NotificationGroupManager groupManager;

    // Componentes de la interfaz
    private JTable groupsTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton duplicateButton;
    private JButton refreshButton;
    private JTextField searchField;
    private JLabel statsLabel;

    public NotificationGroupWindow(int userId, int userRole) {
        this.userId = userId;
        this.userRole = userRole;
        this.groupManager = NotificationGroupManager.getInstance();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadGroups();

        setTitle("👥 Gestión de Grupos de Notificaciones");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        System.out.println("✅ NotificationGroupWindow inicializada para usuario: " + userId);
    }

    private void initializeComponents() {
        // Tabla de grupos
        String[] columnNames = {"ID", "Nombre", "Descripción", "Miembros", "Creado", "Modificado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };

        groupsTable = new JTable(tableModel);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsTable.getTableHeader().setReorderingAllowed(false);
        groupsTable.setRowHeight(25);

        // Ocultar columna ID
        groupsTable.getColumnModel().getColumn(0).setMinWidth(0);
        groupsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        groupsTable.getColumnModel().getColumn(0).setWidth(0);

        // Ajustar ancho de columnas
        groupsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Nombre
        groupsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
        groupsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Miembros
        groupsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Creado
        groupsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Modificado

        // Botones
        createButton = new JButton("➕ Crear Grupo");
        editButton = new JButton("✏️ Editar");
        deleteButton = new JButton("🗑️ Eliminar");
        duplicateButton = new JButton("📋 Duplicar");
        refreshButton = new JButton("🔄 Actualizar");

        // Campo de búsqueda
        searchField = new JTextField(20);
        searchField.setToolTipText("Buscar grupos por nombre o descripción");

        // Label de estadísticas
        statsLabel = new JLabel("Grupos: 0");

        // Estado inicial de botones
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        duplicateButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel superior - búsqueda y estadísticas
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("🔍 Buscar:"));
        searchPanel.add(searchField);
        searchPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(statsLabel, BorderLayout.EAST);

        // Panel central - tabla
        JScrollPane scrollPane = new JScrollPane(groupsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mis Grupos de Notificaciones"));

        // Panel inferior - botones
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.add(createButton);
        bottomPanel.add(editButton);
        bottomPanel.add(duplicateButton);
        bottomPanel.add(deleteButton);

        // Ensamblar
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        // Selección en tabla
        groupsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = groupsTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                duplicateButton.setEnabled(hasSelection);
            }
        });

        // Doble clic para editar
        groupsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && groupsTable.getSelectedRow() != -1) {
                    editGroup();
                }
            }
        });

        // Búsqueda en tiempo real
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchGroups();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchGroups();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchGroups();
            }
        });

        // Botones
        createButton.addActionListener(e -> createGroup());
        editButton.addActionListener(e -> editGroup());
        deleteButton.addActionListener(e -> deleteGroup());
        duplicateButton.addActionListener(e -> duplicateGroup());
        refreshButton.addActionListener(e -> loadGroups());
    }

    private void loadGroups() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        CompletableFuture<List<NotificationGroup>> future = groupManager.obtenerGruposUsuario(userId);

        future.thenAccept(grupos -> {
            SwingUtilities.invokeLater(() -> {
                updateTable(grupos);
                updateStats(grupos.size());
                setCursor(Cursor.getDefaultCursor());
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Error cargando grupos: " + throwable.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
    }

    private void searchGroups() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            loadGroups();
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        CompletableFuture<List<NotificationGroup>> future
                = groupManager.buscarGruposPorNombre(searchText, userId);

        future.thenAccept(grupos -> {
            SwingUtilities.invokeLater(() -> {
                updateTable(grupos);
                updateStats(grupos.size());
                setCursor(Cursor.getDefaultCursor());
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Error en búsqueda: " + throwable.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
    }

    private void updateTable(List<NotificationGroup> grupos) {
        tableModel.setRowCount(0);

        for (NotificationGroup grupo : grupos) {
            Object[] row = {
                grupo.getId(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                grupo.getMiembros().size(),
                grupo.getFechaCreacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                grupo.getFechaModificacion() != null
                ? grupo.getFechaModificacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"
            };
            tableModel.addRow(row);
        }
    }

    private void updateStats(int count) {
        statsLabel.setText("Grupos: " + count);
    }

    private void createGroup() {
        GroupFormDialog dialog = new GroupFormDialog(this, "Crear Nuevo Grupo", null, userId, groupManager);
        dialog.setVisible(true);

        if (dialog.isAccepted()) {
            loadGroups(); // Recargar la lista
        }
    }

    private void editGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int groupId = (Integer) tableModel.getValueAt(selectedRow, 0);

        // Obtener el grupo completo
        groupManager.obtenerGrupoPorId(groupId).thenAccept(grupo -> {
            SwingUtilities.invokeLater(() -> {
                if (grupo != null) {
                    GroupFormDialog dialog = new GroupFormDialog(this, "Editar Grupo", grupo, userId, groupManager);
                    dialog.setVisible(true);

                    if (dialog.isAccepted()) {
                        loadGroups(); // Recargar la lista
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo cargar la información del grupo.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    private void deleteGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String groupName = (String) tableModel.getValueAt(selectedRow, 1);
        int groupId = (Integer) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el grupo '" + groupName + "'?\n\n"
                + "Esta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            groupManager.eliminarGrupo(groupId, userId).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());

                    if (success) {
                        JOptionPane.showMessageDialog(this,
                                "Grupo eliminado exitosamente.",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        loadGroups();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error al eliminar el grupo.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }

    private void duplicateGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String originalName = (String) tableModel.getValueAt(selectedRow, 1);
        int groupId = (Integer) tableModel.getValueAt(selectedRow, 0);

        String newName = JOptionPane.showInputDialog(this,
                "Ingrese el nombre para la copia del grupo:",
                "Duplicar Grupo",
                JOptionPane.QUESTION_MESSAGE);

        if (newName != null && !newName.trim().isEmpty()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            groupManager.duplicarGrupo(groupId, newName.trim(), userId).thenAccept(newGroupId -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());

                    if (newGroupId > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Grupo duplicado exitosamente.",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        loadGroups();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error al duplicar el grupo. Verifique que el nombre no exista.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }

    /**
     * Diálogo interno para crear/editar grupos
     */
    private static class GroupFormDialog extends JDialog {

        private final NotificationGroup existingGroup;
        private final int userId;
        private final NotificationGroupManager groupManager;
        private boolean accepted = false;

        // Componentes
        private JTextField nameField;
        private JTextArea descriptionArea;
        private JList<UserInfo> availableUsersList;
        private JList<UserInfo> selectedUsersList;
        private DefaultListModel<UserInfo> availableModel;
        private DefaultListModel<UserInfo> selectedModel;
        private JTextField searchField;

        public GroupFormDialog(Window parent, String title, NotificationGroup group,
                int userId, NotificationGroupManager groupManager) {
            super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
            this.existingGroup = group;
            this.userId = userId;
            this.groupManager = groupManager;

            initializeDialogComponents();
            setupDialogLayout();
            setupDialogListeners();
            loadUsers();

            if (group != null) {
                loadGroupData(group);
            }

            setSize(700, 500);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        private void initializeDialogComponents() {
            nameField = new JTextField(20);
            descriptionArea = new JTextArea(3, 20);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);

            availableModel = new DefaultListModel<>();
            selectedModel = new DefaultListModel<>();

            availableUsersList = new JList<>(availableModel);
            selectedUsersList = new JList<>(selectedModel);

            availableUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            selectedUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            searchField = new JTextField(15);
            searchField.setToolTipText("Buscar usuarios...");
        }

        private void setupDialogLayout() {
            setLayout(new BorderLayout());

            // Panel superior - información básica
            JPanel topPanel = new JPanel(new GridBagLayout());
            topPanel.setBorder(BorderFactory.createTitledBorder("Información del Grupo"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            topPanel.add(new JLabel("Nombre:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            topPanel.add(nameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            topPanel.add(new JLabel("Descripción:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 0.3;
            topPanel.add(new JScrollPane(descriptionArea), gbc);

            // Panel central - selección de usuarios
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBorder(BorderFactory.createTitledBorder("Seleccionar Miembros"));

            // Panel de búsqueda
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.add(new JLabel("🔍 Buscar:"));
            searchPanel.add(searchField);

            // Panel de listas
            JPanel listsPanel = new JPanel(new GridLayout(1, 3, 10, 0));

            // Lista de usuarios disponibles
            JPanel availablePanel = new JPanel(new BorderLayout());
            availablePanel.add(new JLabel("Usuarios Disponibles", JLabel.CENTER), BorderLayout.NORTH);
            availablePanel.add(new JScrollPane(availableUsersList), BorderLayout.CENTER);

            // Panel de botones
            JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            JButton addButton = new JButton("➡️");
            JButton addAllButton = new JButton("⏩");
            JButton removeButton = new JButton("⬅️");
            JButton removeAllButton = new JButton("⏪");

            addButton.setToolTipText("Agregar seleccionados");
            addAllButton.setToolTipText("Agregar todos");
            removeButton.setToolTipText("Quitar seleccionados");
            removeAllButton.setToolTipText("Quitar todos");

            buttonsPanel.add(addButton);
            buttonsPanel.add(addAllButton);
            buttonsPanel.add(removeButton);
            buttonsPanel.add(removeAllButton);

            // Lista de usuarios seleccionados
            JPanel selectedPanel = new JPanel(new BorderLayout());
            selectedPanel.add(new JLabel("Miembros del Grupo", JLabel.CENTER), BorderLayout.NORTH);
            selectedPanel.add(new JScrollPane(selectedUsersList), BorderLayout.CENTER);

            listsPanel.add(availablePanel);
            listsPanel.add(buttonsPanel);
            listsPanel.add(selectedPanel);

            centerPanel.add(searchPanel, BorderLayout.NORTH);
            centerPanel.add(listsPanel, BorderLayout.CENTER);

            // Panel inferior - botones de acción
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton(existingGroup == null ? "Crear Grupo" : "Guardar Cambios");
            JButton cancelButton = new JButton("Cancelar");

            bottomPanel.add(cancelButton);
            bottomPanel.add(saveButton);

            // Ensamblar
            add(topPanel, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);

            // Listeners de botones
            addButton.addActionListener(e -> moveUsers(availableUsersList, selectedModel, availableModel));
            addAllButton.addActionListener(e -> moveAllUsers(availableModel, selectedModel));
            removeButton.addActionListener(e -> moveUsers(selectedUsersList, availableModel, selectedModel));
            removeAllButton.addActionListener(e -> moveAllUsers(selectedModel, availableModel));

            saveButton.addActionListener(e -> saveGroup());
            cancelButton.addActionListener(e -> dispose());
        }

        private void setupDialogListeners() {
            // Búsqueda en tiempo real
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    filterUsers();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    filterUsers();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    filterUsers();
                }
            });

            // Doble clic para mover usuarios
            availableUsersList.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        moveUsers(availableUsersList, selectedModel, availableModel);
                    }
                }
            });

            selectedUsersList.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        moveUsers(selectedUsersList, availableModel, selectedModel);
                    }
                }
            });
        }

        private void loadUsers() {
            // CORREGIDO: Consulta adaptada a la estructura real de la BD
            try {
                java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();

                // CORREGIDO: Adaptado a la estructura real con usuario_roles y roles
                String query = """
            SELECT u.id, u.nombre, u.apellido, u.mail as email, 
                   ur.rol_id, r.nombre as rol_nombre
            FROM usuarios u
            LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
            LEFT JOIN roles r ON ur.rol_id = r.id
            WHERE u.status = 1 AND u.id != ?
            ORDER BY u.apellido, u.nombre
            """;

                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, userId);
                java.sql.ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String rolNombre = rs.getString("rol_nombre");
                    // Si no tiene rol asignado, mostrar "Sin rol"
                    if (rolNombre == null || rolNombre.trim().isEmpty()) {
                        rolNombre = "Sin rol";
                    }

                    UserInfo user = new UserInfo(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("email"),
                            rolNombre
                    );
                    availableModel.addElement(user);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error cargando usuarios: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        private void loadGroupData(NotificationGroup group) {
            nameField.setText(group.getNombre());
            descriptionArea.setText(group.getDescripcion());

            // CORREGIDO: Mover miembros existentes a la lista seleccionada
            // usando los IDs de los miembros del grupo
            java.util.Set<Integer> memberIds = new java.util.HashSet<>(group.getMiembros());

            // Recorrer la lista disponible y mover los miembros
            for (int i = availableModel.size() - 1; i >= 0; i--) {
                UserInfo user = availableModel.get(i);
                if (memberIds.contains(user.getId())) {
                    selectedModel.addElement(user);
                    availableModel.remove(i);
                }
            }

            System.out.println("✅ Datos del grupo cargados. Miembros: " + selectedModel.size());
        }

        private void filterUsers() {
            String searchText = searchField.getText().toLowerCase().trim();

            if (searchText.isEmpty()) {
                // Si no hay texto de búsqueda, no hacer nada
                // (todos los usuarios ya están cargados)
                return;
            }

            // IMPLEMENTACIÓN COMPLETA del filtrado
            try {
                java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();

                String query = """
            SELECT u.id, u.nombre, u.apellido, u.mail as email, 
                   ur.rol_id, r.nombre as rol_nombre
            FROM usuarios u
            LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
            LEFT JOIN roles r ON ur.rol_id = r.id
            WHERE u.status = 1 AND u.id != ?
            AND (LOWER(u.nombre) LIKE ? OR LOWER(u.apellido) LIKE ? 
                 OR LOWER(u.mail) LIKE ? OR LOWER(r.nombre) LIKE ?)
            ORDER BY u.apellido, u.nombre
            """;

                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, userId);
                String searchPattern = "%" + searchText + "%";
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);
                ps.setString(4, searchPattern);
                ps.setString(5, searchPattern);

                java.sql.ResultSet rs = ps.executeQuery();

                // Limpiar la lista actual (pero preservar los ya seleccionados)
                java.util.Set<Integer> selectedUserIds = new java.util.HashSet<>();
                for (int i = 0; i < selectedModel.size(); i++) {
                    selectedUserIds.add(selectedModel.get(i).getId());
                }

                availableModel.clear();

                while (rs.next()) {
                    int userId = rs.getInt("id");

                    // Solo agregar si no está ya seleccionado
                    if (!selectedUserIds.contains(userId)) {
                        String rolNombre = rs.getString("rol_nombre");
                        if (rolNombre == null || rolNombre.trim().isEmpty()) {
                            rolNombre = "Sin rol";
                        }

                        UserInfo user = new UserInfo(
                                userId,
                                rs.getString("nombre"),
                                rs.getString("apellido"),
                                rs.getString("email"),
                                rolNombre
                        );
                        availableModel.addElement(user);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error filtrando usuarios: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void reloadAllUsers() {
            // Preservar usuarios ya seleccionados
            java.util.Set<Integer> selectedUserIds = new java.util.HashSet<>();
            for (int i = 0; i < selectedModel.size(); i++) {
                selectedUserIds.add(selectedModel.get(i).getId());
            }

            // Limpiar búsqueda y recargar
            searchField.setText("");
            availableModel.clear();

            try {
                java.sql.Connection conn = main.java.database.Conexion.getInstancia().verificarConexion();

                String query = """
            SELECT u.id, u.nombre, u.apellido, u.mail as email, 
                   ur.rol_id, r.nombre as rol_nombre
            FROM usuarios u
            LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id AND ur.is_default = 1
            LEFT JOIN roles r ON ur.rol_id = r.id
            WHERE u.status = 1 AND u.id != ?
            ORDER BY u.apellido, u.nombre
            """;

                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, userId);
                java.sql.ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int userIdFromDb = rs.getInt("id");

                    // Solo agregar si no está ya seleccionado
                    if (!selectedUserIds.contains(userIdFromDb)) {
                        String rolNombre = rs.getString("rol_nombre");
                        if (rolNombre == null || rolNombre.trim().isEmpty()) {
                            rolNombre = "Sin rol";
                        }

                        UserInfo user = new UserInfo(
                                userIdFromDb,
                                rs.getString("nombre"),
                                rs.getString("apellido"),
                                rs.getString("email"),
                                rolNombre
                        );
                        availableModel.addElement(user);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error recargando usuarios: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private boolean validateGroupData() {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre del grupo es obligatorio.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return false;
            }

            if (name.length() > 100) {
                JOptionPane.showMessageDialog(this,
                        "El nombre del grupo no puede exceder 100 caracteres.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return false;
            }

            String description = descriptionArea.getText().trim();
            if (description.length() > 500) {
                JOptionPane.showMessageDialog(this,
                        "La descripción no puede exceder 500 caracteres.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                descriptionArea.requestFocus();
                return false;
            }

            if (selectedModel.size() == 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "El grupo no tiene miembros seleccionados.\n¿Desea continuar?",
                        "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            return true;
        }

        private void moveUsers(JList<UserInfo> sourceList, DefaultListModel<UserInfo> targetModel,
                DefaultListModel<UserInfo> sourceModel) {
            java.util.List<UserInfo> selectedUsers = sourceList.getSelectedValuesList();

            for (UserInfo user : selectedUsers) {
                sourceModel.removeElement(user);
                targetModel.addElement(user);
            }
        }

        private void moveAllUsers(DefaultListModel<UserInfo> sourceModel, DefaultListModel<UserInfo> targetModel) {
            while (sourceModel.size() > 0) {
                UserInfo user = sourceModel.remove(0);
                targetModel.addElement(user);
            }
        }

        private void saveGroup() {
            // AGREGAR esta validación al inicio del método existente:
            if (!validateGroupData()) {
                return;
            }
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre del grupo es obligatorio.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener IDs de miembros seleccionados
            java.util.List<Integer> memberIds = new java.util.ArrayList<>();
            for (int i = 0; i < selectedModel.size(); i++) {
                memberIds.add(selectedModel.get(i).getId());
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            java.util.concurrent.CompletableFuture<Boolean> future;

            if (existingGroup == null) {
                // Crear nuevo grupo
                future = groupManager.crearGrupo(name, description, userId, memberIds)
                        .thenApply(groupId -> groupId > 0);
            } else {
                // Actualizar grupo existente
                future = groupManager.actualizarGrupo(existingGroup.getId(), name, description, memberIds, userId);
            }

            future.thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());

                    if (success) {
                        accepted = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error al guardar el grupo. Verifique que el nombre no exista.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this,
                            "Error: " + throwable.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        }

        public boolean isAccepted() {
            return accepted;
        }
    }

    /**
     * Clase auxiliar para información de usuario
     */
    private static class UserInfo {

        private final int id;
        private final String nombre;
        private final String apellido;
        private final String email;
        private final String rol;

        public UserInfo(int id, String nombre, String apellido, String email, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.rol = rol;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getApellido() {
            return apellido;
        }

        public String getEmail() {
            return email;
        }

        public String getRol() {
            return rol;
        }

        @Override
        public String toString() {
            return apellido + ", " + nombre + " (" + rol + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            UserInfo userInfo = (UserInfo) obj;
            return id == userInfo.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }

}
