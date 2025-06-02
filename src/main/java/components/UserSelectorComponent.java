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
 * Componente avanzado para selección jerárquica de usuarios como destinatarios
 * de notificaciones. Organiza usuarios por roles y permite selección múltiple.
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0
 */
public class UserSelectorComponent extends JPanel {
    
    private final Connection connection;
    private final int currentUserId;
    private final int currentUserRole;
    
    // Componentes UI
    private JTree userTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTextField searchField;
    private JLabel selectedCountLabel;
    private JButton selectAllButton;
    private JButton clearAllButton;
    
    // Datos
    private Map<String, UserInfo> allUsers;
    private Set<Integer> selectedUserIds;
    private Map<String, Set<Integer>> roleGroups;
    
    public UserSelectorComponent(int currentUserId, int currentUserRole) {
        this.connection = Conexion.getInstancia().verificarConexion();
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
        this.allUsers = new HashMap<>();
        this.selectedUserIds = new HashSet<>();
        this.roleGroups = new HashMap<>();
        
        initializeComponents();
        setupLayout();
        setupListeners();
        loadUsers();
        
        System.out.println("✅ UserSelectorComponent inicializado para usuario: " + currentUserId);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Seleccionar Destinatarios"));
        setPreferredSize(new Dimension(400, 500));
        
        // Campo de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("🔍 Buscar: "), BorderLayout.WEST);
        
        searchField = new JTextField();
        searchField.setToolTipText("Buscar por nombre, apellido o rol");
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Árbol de usuarios
        rootNode = new DefaultMutableTreeNode("Usuarios del Sistema");
        treeModel = new DefaultTreeModel(rootNode);
        userTree = new JTree(treeModel);
        userTree.setShowsRootHandles(true);
        userTree.setRootVisible(true);
        userTree.setCellRenderer(new UserTreeCellRenderer());
        userTree.setRowHeight(25);
        
        // Habilitar selección múltiple con checkboxes
        userTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        userTree.setCellEditor(new CheckBoxTreeCellEditor());
        userTree.setEditable(true);
        
        JScrollPane treeScrollPane = new JScrollPane(userTree);
        treeScrollPane.setPreferredSize(new Dimension(380, 300));
        
        // Panel de controles
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        selectAllButton = new JButton("✅ Seleccionar Todo");
        selectAllButton.setFont(new Font("Arial", Font.PLAIN, 11));
        
        clearAllButton = new JButton("❌ Limpiar Todo");
        clearAllButton.setFont(new Font("Arial", Font.PLAIN, 11));
        
        selectedCountLabel = new JLabel("Seleccionados: 0");
        selectedCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        selectedCountLabel.setForeground(new Color(51, 153, 255));
        
        controlPanel.add(selectAllButton);
        controlPanel.add(clearAllButton);
        controlPanel.add(new JLabel(" | "));
        controlPanel.add(selectedCountLabel);
        
        // Ensamblar componentes
        add(searchPanel, BorderLayout.NORTH);
        add(treeScrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void setupLayout() {
        // Layout ya configurado en initializeComponents
    }
    
    private void setupListeners() {
        // Búsqueda en tiempo real
        searchField.addActionListener(e -> filterUsers());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
        });
        
        // Botones de control
        selectAllButton.addActionListener(e -> selectAllUsers());
        clearAllButton.addActionListener(e -> clearAllSelections());
        
        // Escucha de selecciones en el árbol
        userTree.addTreeSelectionListener(e -> updateSelectionCount());
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
                expandTree();
            }
        };
        worker.execute();
    }
    
    private void loadUsersFromDatabase() {
        try {
            allUsers.clear();
            roleGroups.clear();
            
            String query = """
                SELECT u.id, u.nombre, u.apellido, u.mail as email, u.rol, u.anio, u.division, 
                       u.rol as rol_nombre, u.status,
                       CASE 
                           WHEN u.rol = '4' THEN CONCAT(u.anio, '°', u.division)
                           ELSE ''
                       END as curso_info
                FROM usuarios u
                WHERE u.status = 1 AND u.id != ?
                ORDER BY u.rol, u.apellido, u.nombre
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
                
                // Agrupar por rol
                String roleName = user.roleName;
                roleGroups.computeIfAbsent(roleName, k -> new HashSet<>()).add(user.id);
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
                
                DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
                    new RoleNodeInfo(roleName, usersInRole.size())
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
                new CourseNodeInfo(courseName, studentsInCourse.size())
            );
            
            // Agregar estudiantes al curso
            for (UserInfo student : studentsInCourse) {
                DefaultMutableTreeNode studentNode = new DefaultMutableTreeNode(student);
                courseNode.add(studentNode);
            }
            
            roleNode.add(courseNode);
        }
    }
    
    private void expandTree() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < userTree.getRowCount(); i++) {
                userTree.expandRow(i);
            }
        });
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
                if (user.getFullName().toLowerCase().contains(searchText) ||
                    user.roleName.toLowerCase().contains(searchText) ||
                    user.email.toLowerCase().contains(searchText)) {
                    filteredUsers.add(user);
                }
            }
            
            // Crear nodo de resultados
            DefaultMutableTreeNode resultsNode = new DefaultMutableTreeNode(
                "Resultados de búsqueda (" + filteredUsers.size() + ")"
            );
            
            for (UserInfo user : filteredUsers) {
                DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(user);
                resultsNode.add(userNode);
            }
            
            rootNode.add(resultsNode);
            treeModel.reload();
            expandTree();
        });
    }
    
    private void selectAllUsers() {
        selectedUserIds.clear();
        for (UserInfo user : allUsers.values()) {
            selectedUserIds.add(user.id);
        }
        updateSelectionCount();
        userTree.repaint();
    }
    
    private void clearAllSelections() {
        selectedUserIds.clear();
        updateSelectionCount();
        userTree.repaint();
    }
    
    private void updateSelectionCount() {
        SwingUtilities.invokeLater(() -> {
            selectedCountLabel.setText("Seleccionados: " + selectedUserIds.size());
        });
    }
    
    // ===============================================
    // MÉTODOS PÚBLICOS PARA OBTENER SELECCIONES
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
     * Selecciona usuarios programáticamente
     */
    public void setSelectedUserIds(Set<Integer> userIds) {
        this.selectedUserIds.clear();
        this.selectedUserIds.addAll(userIds);
        updateSelectionCount();
        userTree.repaint();
    }
    
    /**
     * Obtiene resumen de la selección para mostrar al usuario
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
    // CLASES INTERNAS Y RENDERERS
    // ===============================================
    
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
        public final String roleName;
        public final int userCount;
        
        public RoleNodeInfo(String roleName, int userCount) {
            this.roleName = roleName;
            this.userCount = userCount;
        }
        
        @Override
        public String toString() {
            return "📂 " + roleName + " (" + userCount + ")";
        }
    }
    
    /**
     * Información de nodo de curso
     */
    private static class CourseNodeInfo {
        public final String courseName;
        public final int studentCount;
        
        public CourseNodeInfo(String courseName, int studentCount) {
            this.courseName = courseName;
            this.studentCount = studentCount;
        }
        
        @Override
        public String toString() {
            return "🎓 " + courseName + " (" + studentCount + ")";
        }
    }
    
    /**
     * Renderer personalizado para el árbol de usuarios
     */
    private class UserTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof UserInfo) {
                UserInfo user = (UserInfo) userObject;
                setIcon(getRoleIcon(user.roleId));
                
                // Cambiar color si está seleccionado
                if (selectedUserIds.contains(user.id)) {
                    setForeground(new Color(51, 153, 255));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            } else if (userObject instanceof RoleNodeInfo) {
                setIcon(new ImageIcon(getClass().getResource("/main/resources/images/folder.png")));
            } else if (userObject instanceof CourseNodeInfo) {
                setIcon(new ImageIcon(getClass().getResource("/main/resources/images/course.png")));
            }
            
            return this;
        }
        
        private Icon getRoleIcon(int roleId) {
            switch (roleId) {
                case 1: return new ImageIcon(getClass().getResource("/main/resources/images/admin.png"));
                case 2: return new ImageIcon(getClass().getResource("/main/resources/images/preceptor.png"));
                case 3: return new ImageIcon(getClass().getResource("/main/resources/images/profesor.png"));
                case 4: return new ImageIcon(getClass().getResource("/main/resources/images/student.png"));
                case 5: return new ImageIcon(getClass().getResource("/main/resources/images/attp.png"));
                default: return new ImageIcon(getClass().getResource("/main/resources/images/user.png"));
            }
        }
    }
    
    /**
     * Renderer con checkbox para selección múltiple
     */
    private class CheckBoxTreeCellRenderer extends JPanel implements TreeCellRenderer {
        private JCheckBox checkBox;
        private UserTreeCellRenderer defaultRenderer;
        
        public CheckBoxTreeCellRenderer() {
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            defaultRenderer = new UserTreeCellRenderer();
            add(checkBox, BorderLayout.WEST);
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            Component rendererComponent = defaultRenderer.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof UserInfo) {
                UserInfo user = (UserInfo) userObject;
                checkBox.setSelected(selectedUserIds.contains(user.id));
                checkBox.setVisible(true);
            } else {
                checkBox.setVisible(false);
            }
            
            removeAll();
            add(checkBox, BorderLayout.WEST);
            add(rendererComponent, BorderLayout.CENTER);
            
            return this;
        }
    }
    
    /**
     * Editor para manejar clics en checkboxes
     */
    private class CheckBoxTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {
        private CheckBoxTreeCellRenderer renderer;
        private JTree tree;
        
        public CheckBoxTreeCellEditor() {
            this.renderer = new CheckBoxTreeCellRenderer();
        }
        
        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                boolean isSelected, boolean expanded, boolean leaf, int row) {
            
            this.tree = tree;
            Component component = renderer.getTreeCellRendererComponent(
                tree, value, isSelected, expanded, leaf, row, true);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
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
            }
            
            return component;
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
        roleGroups.clear();
    }
}