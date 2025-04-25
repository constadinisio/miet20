package main.java.utils;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import main.java.database.Conexion;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nico_
 */
public class AsignarRolesDialog extends javax.swing.JDialog {

    private int userId;
    private String nombreUsuario;
    private Connection conect;
    private DefaultTableModel tableModel;
    private boolean cambiosRealizados = false;
    private String rolColumnName = "rol_id"; // Valor por defecto que se actualizará al verificar la estructura

    // Componentes UI como variables de instancia
    private JTable tablaRoles;
    private JButton btnGuardar;
    private JButton btnCancelar;

    public AsignarRolesDialog(Frame parent, boolean modal, int userId, String nombreUsuario) {
        super(parent, modal);
        this.userId = userId;
        this.nombreUsuario = nombreUsuario;

        // IMPORTANTE: Inicializa la tabla antes de llamar a initComponents
        tablaRoles = new JTable();

        initComponents();
        setupUI(); // Nueva función para configurar UI
        probar_conexion();
        verificarEstructuraTabla(); // Verificar estructura de la tabla para obtener el nombre correcto de la columna
        inicializarTabla();
        cargarRoles();

        setTitle("Asignar Roles a " + nombreUsuario);
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        // Configurar el layout del diálogo
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Agregar descripción en la parte superior
        JLabel lblDescripcion = new JLabel("Seleccione los roles para el usuario " + nombreUsuario);
        lblDescripcion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(lblDescripcion, BorderLayout.NORTH);

        // Agregar tabla con scroll
        JScrollPane scrollPane = new JScrollPane(tablaRoles);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones en la parte inferior
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> guardarCambios());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPane.add(panelBotones, BorderLayout.SOUTH);
    }

    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Verificar la estructura real de la tabla usuario_roles
    private void verificarEstructuraTabla() {
        try {
            System.out.println("Verificando estructura de la tabla 'usuario_roles'...");
            DatabaseMetaData meta = conect.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "usuario_roles", null);

            boolean encontrada = false;

            // Buscar columna de rol (podría ser 'rol' o 'rol_id')
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                System.out.println("Columna encontrada: " + columnName);

                if (columnName.equalsIgnoreCase("rol")) {
                    rolColumnName = "rol";
                    encontrada = true;
                    System.out.println("Usando nombre de columna: 'rol'");
                    break;
                } else if (columnName.equalsIgnoreCase("rol_id")) {
                    rolColumnName = "rol_id";
                    encontrada = true;
                    System.out.println("Usando nombre de columna: 'rol_id'");
                    break;
                }
            }

            if (!encontrada) {
                System.out.println("ADVERTENCIA: No se encontró columna 'rol' o 'rol_id'");
                JOptionPane.showMessageDialog(this,
                        "La estructura de la tabla 'usuario_roles' no tiene una columna 'rol' o 'rol_id'. "
                        + "Es posible que se produzcan errores.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error al verificar estructura de tabla: " + ex.getMessage());
        }
    }

    // Constructor de fallback para el diseñador
    public AsignarRolesDialog(Frame parent, boolean modal) {
        this(parent, modal, -1, "Usuario de prueba");
    }

    private void inicializarTabla() {
        tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Solo la columna del checkbox es editable
            }
        };

        tableModel.addColumn("ID");
        tableModel.addColumn("Rol");
        tableModel.addColumn("Asignado");
        tableModel.addColumn("Predeterminado");

        tablaRoles.setModel(tableModel);

        // Ocultar columna ID
        tablaRoles.getColumnModel().getColumn(0).setMinWidth(0);
        tablaRoles.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaRoles.getColumnModel().getColumn(0).setWidth(0);

        // Ajustar ancho de columnas
        tablaRoles.getColumnModel().getColumn(1).setPreferredWidth(150); // Rol
        tablaRoles.getColumnModel().getColumn(2).setPreferredWidth(80);  // Asignado
        tablaRoles.getColumnModel().getColumn(3).setPreferredWidth(100); // Predeterminado

        // Configurar selección de filas
        tablaRoles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRoles.setRowHeight(25);
    }

    private void cargarRoles() {
        try {
            System.out.println("Conectando a la base de datos...");
            conect = Conexion.getInstancia().verificarConexion();

            // Ejecutar una consulta simple primero para probar la conexión
            Statement testStmt = conect.createStatement();
            ResultSet testRs = testStmt.executeQuery("SELECT id, nombre FROM roles");
            System.out.println("Roles disponibles:");
            while (testRs.next()) {
                System.out.println(testRs.getInt("id") + ": " + testRs.getString("nombre"));
            }

            // Usar el nombre de columna detectado
            String query
                    = "SELECT r.id, r.nombre, "
                    + "IF(ur.id IS NULL, false, true) as asignado, "
                    + "IF(ur.is_default = 1, 'Sí', 'No') as predeterminado "
                    + "FROM roles r "
                    + "LEFT JOIN usuario_roles ur ON r.id = ur." + rolColumnName + " AND ur.usuario_id = ? "
                    + "ORDER BY r.id";

            System.out.println("Ejecutando consulta: " + query.replace("?", String.valueOf(userId)));

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("Procesando resultados...");
            int contadorFilas = 0;

            // Limpiar tabla antes de cargar nuevos datos
            while (tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
            }

            while (rs.next()) {
                contadorFilas++;
                int rolId = rs.getInt("id");
                String rolNombre = rs.getString("nombre");
                boolean asignado = rs.getBoolean("asignado");
                String predeterminado = rs.getString("predeterminado");

                System.out.println("Rol: " + rolId + " - " + rolNombre
                        + " (Asignado: " + asignado + ", Pred: " + predeterminado + ")");

                Object[] row = {
                    rolId,
                    rolNombre,
                    asignado,
                    predeterminado
                };
                tableModel.addRow(row);
            }

            System.out.println("Total de roles cargados: " + contadorFilas);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar roles: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarCambios() {
        try {
            // Iniciar transacción
            conect.setAutoCommit(false);

            // Eliminar todas las asignaciones existentes
            String deleteQuery = "DELETE FROM usuario_roles WHERE usuario_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setInt(1, userId);
            deletePs.executeUpdate();

            // Usar el nombre de columna detectado para el insert
            String insertQuery = "INSERT INTO usuario_roles (usuario_id, " + rolColumnName + ", is_default) VALUES (?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);

            // Variable para verificar si hay al menos un rol predeterminado
            boolean hayRolPredeterminado = false;

            // Lista de roles asignados para validación
            List<Integer> rolesAsignados = new ArrayList<>();

            // Primera pasada: recopilar roles asignados
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                boolean asignado = (boolean) tableModel.getValueAt(i, 2);
                if (asignado) {
                    int rolId = (int) tableModel.getValueAt(i, 0);
                    rolesAsignados.add(rolId);
                }
            }

            // Si no hay roles asignados, mostrar error
            if (rolesAsignados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El usuario debe tener al menos un rol asignado.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                conect.rollback();
                return;
            }

            // Segunda pasada: insertar roles y marcar predeterminado
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                boolean asignado = (boolean) tableModel.getValueAt(i, 2);

                if (asignado) {
                    int rolId = (int) tableModel.getValueAt(i, 0);

                    // Si este es el primer rol, marcarlo como predeterminado si no hay otro
                    boolean isPredeterminado = false;

                    // Determinar si este rol es predeterminado
                    if (!hayRolPredeterminado && rolesAsignados.contains(rolId)) {
                        isPredeterminado = true;
                        hayRolPredeterminado = true;
                        // Actualizar la UI
                        tableModel.setValueAt("Sí", i, 3);
                    } else {
                        tableModel.setValueAt("No", i, 3);
                    }

                    // Insertar en la base de datos
                    insertPs.setInt(1, userId);
                    insertPs.setInt(2, rolId);
                    insertPs.setInt(3, isPredeterminado ? 1 : 0);
                    insertPs.executeUpdate();
                }
            }

            // Confirmar transacción
            conect.commit();

            // Actualizar también el rol principal en la tabla usuarios
            if (hayRolPredeterminado) {
                // Buscar el rol predeterminado
                int rolPredeterminado = -1;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    boolean asignado = (boolean) tableModel.getValueAt(i, 2);
                    String predeterminado = (String) tableModel.getValueAt(i, 3);

                    if (asignado && predeterminado.equals("Sí")) {
                        rolPredeterminado = (int) tableModel.getValueAt(i, 0);
                        break;
                    }
                }

                // Actualizar el rol en la tabla usuarios
                if (rolPredeterminado != -1) {
                    String updateQuery = "UPDATE usuarios SET rol = ? WHERE id = ?";
                    PreparedStatement updatePs = conect.prepareStatement(updateQuery);
                    updatePs.setInt(1, rolPredeterminado);
                    updatePs.setInt(2, userId);
                    updatePs.executeUpdate();
                }
            }

            cambiosRealizados = true;
            JOptionPane.showMessageDialog(this, "Roles asignados exitosamente");
            dispose();

        } catch (SQLException ex) {
            try {
                conect.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al guardar roles: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                conect.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean seCambiaronRoles() {
        return cambiosRealizados;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AsignarRolesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> {
            AsignarRolesDialog dialog = new AsignarRolesDialog(
                    new JFrame(), true, 1, "Usuario de Prueba");
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

