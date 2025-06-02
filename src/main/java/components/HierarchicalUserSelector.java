package main.java.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;
import main.java.database.Conexion;

/**
 * Selector jerárquico de usuarios para notificaciones Organiza usuarios por
 * roles y permite selección múltiple con checkboxes
 */
    public class HierarchicalUserSelector extends JPanel {

    private final Connection connection;
    private final int currentUserId;

    // Componentes UI
    private JTree userTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTextField searchField;
    private JLabel selectedCountLabel;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private JButton expandAllButton;
    private JButton collapseAllButton;

    // Datos
    private Map<String, UserInfo> allUsers;
    private Set<Integer> selectedUserIds;
    private List<SelectionListener> selectionListeners;

    public HierarchicalUserSelector(int currentUserId) {
        this.connection = Conexion.getInstancia().verificarConexion();
        this.currentUserId = currentUserId;
        this.allUsers = new HashMap<>();
        this.selectedUserIds = new HashSet<>();
        this.selectionListeners = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupListeners();
        loadUsers();

        System.out.println("✅ HierarchicalUserSelector inicializado para usuario: " + currentUserId);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("👥 Seleccionar Destinatarios"));
        setPreferredSize(new Dimension(500, 400));

        // Panel superior con búsqueda y controles
        JPanel topPanel = new JPanel(new BorderLayout());

        // Campo de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("🔍 Buscar:"));

        searchField = new JTextField(20);
        searchField.setToolTipText("Buscar por nombre, apellido o rol");
        searchPanel.add(searchField);

        topPanel.add(searchPanel, BorderLayout.WEST);

        // Botones de control
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        expandAllButton = new JButton("📂 Expandir");
        expandAllButton.setFont(new Font("Arial", Font.PLAIN, 10));
        expandAllButton.setPreferredSize(new Dimension(80, 25));

        collapseAllButton = new JButton("📁 Contraer");
        collapseAllButton.setFont(new Font("Arial", Font.PLAIN, 10));
        collapseAllButton.setPreferredSize(new Dimension(80, 25));

        selectAllButton = new JButton("✅ Todo");
        selectAllButton.setFont(new Font("Arial", Font.PLAIN, 10));
        selectAllButton.setPreferredSize(new Dimension(70, 25));

        clearAllButton = new JButton("❌ Limpiar");
        clearAllButton.setFont(new Font("Arial", Font.PLAIN, 10));
        clearAllButton.setPreferredSize(new Dimension(70, 25));

        controlPanel.add(expandAllButton);
        controlPanel.add(collapseAllButton);
        controlPanel.add(selectAllButton);
        controlPanel.add(clearAllButton);

        topPanel.add(controlPanel, BorderLayout.EAST);

        // Árbol de usuarios con checkboxes
        rootNode = new DefaultMutableTreeNode("Usuarios del Sistema");
        treeModel = new DefaultTreeModel(rootNode);
        userTree = new JTree(treeModel);
        userTree.setShowsRootHandles(true);
        userTree.setRootVisible(false);
        userTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        userTree.setCellEditor(new CheckBoxTreeCellEditor());
        userTree.setEditable(true);
        userTree.setRowHeight(22);

        JScrollPane treeScrollPane = new JScrollPane(userTree);
        treeScrollPane.setPreferredSize(new Dimension(480, 280));

        // Panel inferior con información
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selectedCountLabel = new JLabel("Seleccionados: 0");
        selectedCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        selectedCountLabel.setForeground(new Color(51, 153, 255));
        bottomPanel.add(selectedCountLabel);

        add(topPanel, BorderLayout.NORTH);
        add(treeScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupLayout() {
        // Layout ya configurado en initializeComponents
    }

    private void setupListeners() {
        // Búsqueda en tiempo real
        searchField.addActionListener(e -> filterUsers());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> filterUsers());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> filterUsers());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> filterUsers());
            }
        });

        // Botones de control
        selectAllButton.addActionListener(e -> selectAllUsers());
        clearAllButton.addActionListener(e -> clearAllSelections());
        expandAllButton.addActionListener(e -> expandAllNodes());
        collapseAllButton.addActionListener(e -> collapseAllNodes());

        // Escucha de clics en el árbol
        userTree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                TreePath path = userTree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    handleNodeClick(node);
                }
            }
        });
    }

    private void loadUsers() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadUsersFromDatabase();
                return null;
            }

            @Override
            protected void done() {
                buildUserTree();
                expandAllNodes();
            }
        };
        worker.execute();
    }

    private void loadUsersFromDatabase() {
        try {
            allUsers.clear();

            String query = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.rol, u.anio, u.division, 
                       r.nombre as rol_nombre, u.activo,
                       CASE 
                           WHEN u.rol = 4 THEN CONCAT(u.anio, '°', u.division)
                           ELSE ''
                       END as curso_info
                FROM usuarios u
                LEFT JOIN roles r ON u.rol = r.id
                WHERE u.activo = 1 AND u.id != ?
                ORDER BY r.nombre, u.apellido, u.nombre
                """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, currentUserId); // Excluir usuario actual
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UserInfo user = new UserInfo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email"),
                        rs.getInt("rol"),
                        rs.getString("rol_nombre"),
                        rs.getInt("anio"),
                        rs.getString("division"),
                        rs.getString("curso_info")
                );

                String userKey = user.id + "_" + user.getFullName();
                allUsers.put(userKey, user);
            }

            System.out.println("✅ Usuarios cargados: " + allUsers.size());

        } catch (SQLException e) {
            System.err.println("Error cargando usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buildUserTree() {
        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();

            // Organizar por roles
            Map<String, List<UserInfo>> usersByRole = new HashMap<>();

            for (UserInfo user : allUsers.values()) {
                usersByRole.computeIfAbsent(user.roleName, k -> new ArrayList<>()).add(user);
            }

            // Crear nodos por rol
            for (Map.Entry<String, List<UserInfo>> entry : usersByRole.entrySet()) {
                String roleName = entry.getKey();
                List<UserInfo> usersInRole = entry.getValue();

                // Ícono según el rol
                String roleIcon = getRoleIcon(roleName);

                DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
                        new RoleNodeInfo(roleIcon + " " + roleName + " (" + usersInRole.size() + ")", roleName)
                );

                if ("Estudiante".equals(roleName)) {
                    // Para estudiantes, agrupar por curso
                    buildStudentNodes(roleNode, usersInRole);
                } else {
                    // Para otros roles, listar directamente
                    for (UserInfo user : usersInRole) {
                        DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(user);
                        roleNode.add(userNode);
                    }
                }

                rootNode.add(roleNode);
            }

            treeModel.reload();
        });
    }

    private void buildStudentNodes(DefaultMutableTreeNode roleNode, List<UserInfo> students) {
        Map<String, List<UserInfo>> studentsByCourse = new HashMap<>();

        // Agrupar estudiantes por curso
        for (UserInfo student : students) {
            String course = student.cursoInfo.isEmpty() ? "Sin Curso" : student.cursoInfo;
            studentsByCourse.computeIfAbsent(course, k -> new ArrayList<>()).add(student);
        }

        // Crear nodos por curso
        for (Map.Entry<String, List<UserInfo>> courseEntry : studentsByCourse.entrySet()) {
            String courseName = courseEntry.getKey();
            List<UserInfo> studentsInCourse = courseEntry.getValue();

            DefaultMutableTreeNode courseNode = new DefaultMutableTreeNode(
                    new CourseNodeInfo("🎓 " + courseName + " (" + studentsInCourse.size() + ")", courseName)
            );

            // Agregar estudiantes al curso
            for (UserInfo student : studentsInCourse) {
                DefaultMutableTreeNode studentNode = new DefaultMutableTreeNode(student);
                courseNode.add(studentNode);
            }

            roleNode.add(courseNode);
        }
    }

    private String getRoleIcon(String roleName) {
        switch (roleName) {
            case "Administrador":
                return "👑";
            case "Preceptor":
                return "👨‍🏫";
            case "Profesor":
                return "👩‍🏫";
            case "Estudiante":
                return "🎓";
            case "ATTP":
                return "🔧";
            default:
                return "👤";
        }
    }

    private void handleNodeClick(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();

        if (userObject instanceof UserInfo) {
            UserInfo user = (UserInfo) userObject;

            // Toggle selection
            if (selectedUserIds.contains(user.id)) {
                selectedUserIds.remove(user.id);
            } else {
                selectedUserIds.add(user.id);
            }

            updateSelectionCount();
            userTree.repaint();
            notifySelectionListeners();

        } else if (userObject instanceof RoleNodeInfo) {
            // Seleccionar/deseleccionar todos los usuarios del rol
            toggleRoleSelection(node);

        } else if (userObject instanceof CourseNodeInfo) {
            // Seleccionar/deseleccionar todos los estudiantes del curso
            toggleCourseSelection(node);
        }
    }

    private void toggleRoleSelection(DefaultMutableTreeNode roleNode) {
        Set<Integer> roleUserIds = new HashSet<>();
        collectUserIds(roleNode, roleUserIds);

        // Verificar si todos están seleccionados
        boolean allSelected = selectedUserIds.containsAll(roleUserIds);

        if (allSelected) {
            selectedUserIds.removeAll(roleUserIds);
        } else {
            selectedUserIds.addAll(roleUserIds);
        }

        updateSelectionCount();
        userTree.repaint();
        notifySelectionListeners();
    }

    private void toggleCourseSelection(DefaultMutableTreeNode courseNode) {
        Set<Integer> courseUserIds = new HashSet<>();
        collectUserIds(courseNode, courseUserIds);

        // Verificar si todos están seleccionados
        boolean allSelected = selectedUserIds.containsAll(courseUserIds);

        if (allSelected) {
            selectedUserIds.removeAll(courseUserIds);
        } else {
            selectedUserIds.addAll(courseUserIds);
        }

        updateSelectionCount();
        userTree.repaint();
        notifySelectionListeners();
    }

    private void collectUserIds(DefaultMutableTreeNode node, Set<Integer> userIds) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            Object childObject = childNode.getUserObject();

            if (childObject instanceof UserInfo) {
                userIds.add(((UserInfo) childObject).id);
            } else {
                // Recursivo para nodos anidados
                collectUserIds(childNode, userIds);
            }
        }
    }

    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            buildUserTree();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();

            // Filtrar usuarios que coincidan con la búsqueda
            List<UserInfo> filteredUsers = new ArrayList<>();

            for (UserInfo user : allUsers.values()) {
                if (user.getFullName().toLowerCase().contains(searchText)
                        || user.roleName.toLowerCase().contains(searchText)
                        || user.email.toLowerCase().contains(searchText)
                        || user.cursoInfo.toLowerCase().contains(searchText)) {
                    filteredUsers.add(user);
                }
            }

            // Crear nodo de resultados
            DefaultMutableTreeNode resultsNode = new DefaultMutableTreeNode(
                    "🔍 Resultados de búsqueda (" + filteredUsers.size() + ")"
            );

            for (UserInfo user : filteredUsers) {
                DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(user);
                resultsNode.add(userNode);
            }

            rootNode.add(resultsNode);
            treeModel.reload();
            expandAllNodes();
        });
    }

    private void selectAllUsers() {
        selectedUserIds.clear();
        for (UserInfo user : allUsers.values()) {
            selectedUserIds.add(user.id);
        }
        updateSelectionCount();
        userTree.repaint();
        notifySelectionListeners();
    }

    private void clearAllSelections() {
        selectedUserIds.clear();
        updateSelectionCount();
        userTree.repaint();
        notifySelectionListeners();
    }

    private void expandAllNodes() {
        for (int i = 0; i < userTree.getRowCount(); i++) {
            userTree.expandRow(i);
        }
    }

    private void collapseAllNodes() {
        for (int i = userTree.getRowCount() - 1; i >= 0; i--) {
            userTree.collapseRow(i);
        }
    }

    private void updateSelectionCount() {
        SwingUtilities.invokeLater(() -> {
            selectedCountLabel.setText("Seleccionados: " + selectedUserIds.size());
        });
    }

    private void notifySelectionListeners() {
        for (SelectionListener listener : selectionListeners) {
            listener.selectionChanged();
        }
    }

    // ===============================================
    // MÉTODOS PÚBLICOS
    // ===============================================
    /**
     * Obtiene los IDs de los usuarios seleccionados
     */
    public Set<Integer> getSelectedUserIds() {
        return new HashSet<>(selectedUserIds);
    }

    /**
     * Obtiene la información completa de los usuarios seleccionados
     */
    public List<UserInfo> getSelectedUsers() {
        List<UserInfo> selected = new ArrayList<>();
        for (UserInfo user : allUsers.values()) {
            if (selectedUserIds.contains(user.id)) {
                selected.add(user);
            }
        }
        return selected;
    }

    /**
     * Limpia toda la selección
     */
    public void clearSelection() {
        clearAllSelections();
    }

    /**
     * Selecciona usuarios programáticamente
     */
    public void setSelectedUserIds(Set<Integer> userIds) {
        this.selectedUserIds.clear();
        this.selectedUserIds.addAll(userIds);
        updateSelectionCount();
        userTree.repaint();
        notifySelectionListeners();
    }

    /**
     * Agrega un listener para cambios de selección
     */
    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }

    /**
     * Obtiene resumen de la selección
     */
    public String getSelectionSummary() {
        if (selectedUserIds.isEmpty()) {
            return "Ningún destinatario seleccionado";
        }

        Map<String, Integer> countByRole = new HashMap<>();
        for (UserInfo user : allUsers.values()) {
            if (selectedUserIds.contains(user.id)) {
                countByRole.merge(user.roleName, 1, Integer::sum);
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Destinatarios seleccionados (").append(selectedUserIds.size()).append("):\n");

        for (Map.Entry<String, Integer> entry : countByRole.entrySet()) {
            summary.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return summary.toString();
    }

    // ===============================================
    // CLASES INTERNAS
    // ===============================================
    /**
     * Interfaz para listeners de cambio de selección
     */
    public interface SelectionListener {

        void selectionChanged();
    }

    /**
     * Información de usuario para el selector
     */
    public static class UserInfo {

        public final int id;
        public final String nombre;
        public final String apellido;
        public final String email;
        public final int roleId;
        public final String roleName;
        public final int anio;
        public final String division;
        public final String cursoInfo;

        public UserInfo(int id, String nombre, String apellido, String email,
                int roleId, String roleName, int anio, String division, String cursoInfo) {
            this.id = id;
            this.nombre = nombre != null ? nombre : "";
            this.apellido = apellido != null ? apellido : "";
            this.email = email != null ? email : "";
            this.roleId = roleId;
            this.roleName = roleName != null ? roleName : "Usuario";
            this.anio = anio;
            this.division = division != null ? division : "";
            this.cursoInfo = cursoInfo != null ? cursoInfo : "";
        }

        public String getFullName() {
            return apellido + ", " + nombre;
        }

        @Override
        public String toString() {
            String base = getFullName();
            if (!cursoInfo.isEmpty()) {
                base += " (" + cursoInfo + ")";
            }
            return base;
        }
    }

    /**
     * Información de nodo de rol
     */
    private static class RoleNodeInfo {

        public final String displayName;
        public final String roleName;

        public RoleNodeInfo(String displayName, String roleName) {
            this.displayName = displayName;
            this.roleName = roleName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Información de nodo de curso
     */
    private static class CourseNodeInfo {

        public final String displayName;
        public final String courseName;

        public CourseNodeInfo(String displayName, String courseName) {
            this.displayName = displayName;
            this.courseName = courseName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Renderer personalizado con checkboxes
     */
    private class CheckBoxTreeCellRenderer extends DefaultTreeCellRenderer {

        private JCheckBox checkBox;
        private JPanel panel;

        public CheckBoxTreeCellRenderer() {
            checkBox = new JCheckBox();
            panel = new JPanel(new BorderLayout());
            panel.add(checkBox, BorderLayout.WEST);
            checkBox.setBackground(Color.WHITE);
            panel.setBackground(Color.WHITE);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            Component defaultComponent = super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof UserInfo) {
                UserInfo user = (UserInfo) userObject;
                checkBox.setSelected(selectedUserIds.contains(user.id));
                checkBox.setVisible(true);

                // Color según selección
                if (selectedUserIds.contains(user.id)) {
                    setForeground(new Color(51, 153, 255));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            } else if (userObject instanceof RoleNodeInfo || userObject instanceof CourseNodeInfo) {
                // Para nodos de grupo, mostrar estado parcial/completo
                Set<Integer> nodeUserIds = new HashSet<>();
                collectUserIds(node, nodeUserIds);

                boolean allSelected = !nodeUserIds.isEmpty() && selectedUserIds.containsAll(nodeUserIds);
                boolean noneSelected = nodeUserIds.stream().noneMatch(selectedUserIds::contains);

                if (allSelected) {
                    checkBox.setSelected(true);
                    setForeground(new Color(51, 153, 255));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (!noneSelected) {
                    checkBox.setSelected(false);
                    setForeground(new Color(255, 140, 0)); // Naranja para selección parcial
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    checkBox.setSelected(false);
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                checkBox.setVisible(true);
            } else {
                checkBox.setVisible(false);
            }

            panel.removeAll();
            panel.add(checkBox, BorderLayout.WEST);
            panel.add(defaultComponent, BorderLayout.CENTER);

            return panel;
        }
    }

    /**
     * Editor para manejar clics en checkboxes
     */
    private class CheckBoxTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

        private CheckBoxTreeCellRenderer renderer;

        public CheckBoxTreeCellEditor() {
            this.renderer = new CheckBoxTreeCellRenderer();
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                boolean isSelected, boolean expanded, boolean leaf, int row) {

            return renderer.getTreeCellRendererComponent(
                    tree, value, isSelected, expanded, leaf, row, true);
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    /**
     * Método para limpiar recursos
     */
    public void dispose() {
        selectedUserIds.clear();
        allUsers.clear();
        selectionListeners.clear();
    }
}
