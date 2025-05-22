/*
 * Panel simplificado para registro de netbooks según estructura de BD
 */
package main.java.views.users.Attp.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import main.java.views.users.common.VentanaInicio;

/**
 * Panel simplificado para registro de netbooks.
 * Solo maneja los campos básicos: carrito, número, serie, fecha, estado y observaciones.
 */
public class NetbookRegistrationPanel extends JPanel {

    // Conexión a la base de datos
    private Connection conect;
    
    // Referencia a la ventana principal
    private VentanaInicio ventanaPrincipal;
    
    // ID del usuario ATTP
    private int attpId;

    // Componentes de la interfaz
    private JButton btnRegistrar;
    private JButton btnVolver;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnActualizar;
    private JComboBox<String> comboCarrito;
    private JComboBox<String> comboNumeroNetbook;
    private JTextField txtNumeroSerie;
    private JTextField txtFechaAdquisicion;
    private JComboBox<String> comboEstado;
    private JTextArea txtObservaciones;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JTable tablaNetbooks;
    
    // Labels
    private JLabel lblTitulo;
    private JLabel lblCarrito;
    private JLabel lblNumeroNetbook;
    private JLabel lblNumeroSerie;
    private JLabel lblFechaAdquisicion;
    private JLabel lblEstado;
    private JLabel lblObservaciones;

    /**
     * Constructor del panel para registro de netbooks.
     */
    public NetbookRegistrationPanel(VentanaInicio ventanaPrincipal, int attpId) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.attpId = attpId;
        
        // Inicializar conexión primero
        probar_conexion();
        
        // Crear componentes de la interfaz
        initializeComponents();
        
        // Configurar layout
        setupLayout();
        
        // Configurar listeners
        setupListeners();
        
        // Cargar datos iniciales
        cargarTablaNetbooks();
        inicializarComboBoxes();

        // Establecer fecha actual por defecto
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaAdquisicion.setText(fechaActual.format(formateadorFecha));
        
        System.out.println("NetbookRegistrationPanel inicializado correctamente");
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void initializeComponents() {
        // Inicializar etiquetas
        lblTitulo = new JLabel("Registro de Netbooks", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(51, 153, 255));
        
        lblCarrito = new JLabel("Carrito:");
        lblNumeroNetbook = new JLabel("Número:");
        lblNumeroSerie = new JLabel("N° Serie:");
        lblFechaAdquisicion = new JLabel("Fecha Adquisición:");
        lblEstado = new JLabel("Estado:");
        lblObservaciones = new JLabel("Observaciones:");

        // Inicializar campos de texto
        txtNumeroSerie = new JTextField(20);
        txtFechaAdquisicion = new JTextField(15);

        // Inicializar combo boxes
        comboCarrito = new JComboBox<>();
        comboNumeroNetbook = new JComboBox<>();
        comboEstado = new JComboBox<>();

        // Inicializar área de texto
        txtObservaciones = new JTextArea(4, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        jScrollPane2 = new JScrollPane(txtObservaciones);

        // Inicializar tabla
        tablaNetbooks = new JTable();
        tablaNetbooks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1 = new JScrollPane(tablaNetbooks);
        jScrollPane1.setPreferredSize(new Dimension(600, 300));

        // Inicializar botones
        btnRegistrar = new JButton("Registrar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");
        btnVolver = new JButton("Volver");

        // Estilizar botones
        styleButton(btnRegistrar, new Color(46, 125, 50));
        styleButton(btnActualizar, new Color(255, 193, 7));
        styleButton(btnEliminar, new Color(244, 67, 54));
        styleButton(btnLimpiar, new Color(156, 156, 156));
        styleButton(btnVolver, new Color(63, 81, 181));
    }

    /**
     * Aplica estilo a un botón.
     */
    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
    }

    /**
     * Configura el layout del panel.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior con título
        JPanel panelTitulo = new JPanel(new FlowLayout());
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // Panel central con formulario y tabla
        JPanel panelCentral = new JPanel(new BorderLayout());
        
        // Panel de formulario
        JPanel panelFormulario = createFormPanel();
        panelCentral.add(panelFormulario, BorderLayout.WEST);
        
        // Panel de tabla
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Netbooks Registradas"));
        panelTabla.add(jScrollPane1, BorderLayout.CENTER);
        panelCentral.add(panelTabla, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = createButtonPanel();
        add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel del formulario.
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la Netbook"));
        panel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Carrito
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lblCarrito, gbc);
        gbc.gridx = 1;
        panel.add(comboCarrito, gbc);
        row++;

        // Número
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lblNumeroNetbook, gbc);
        gbc.gridx = 1;
        panel.add(comboNumeroNetbook, gbc);
        row++;

        // Número de serie
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lblNumeroSerie, gbc);
        gbc.gridx = 1;
        panel.add(txtNumeroSerie, gbc);
        row++;

        // Fecha de adquisición
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lblFechaAdquisicion, gbc);
        gbc.gridx = 1;
        panel.add(txtFechaAdquisicion, gbc);
        row++;

        // Estado
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lblEstado, gbc);
        gbc.gridx = 1;
        panel.add(comboEstado, gbc);
        row++;

        // Observaciones
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(lblObservaciones, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(jScrollPane2, gbc);

        return panel;
    }

    /**
     * Crea el panel de botones.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(btnRegistrar);
        panel.add(btnActualizar);
        panel.add(btnEliminar);
        panel.add(btnLimpiar);
        panel.add(btnVolver);

        return panel;
    }

    /**
     * Configura los listeners de los componentes.
     */
    private void setupListeners() {
        btnRegistrar.addActionListener(e -> btnRegistrarActionPerformed());
        btnActualizar.addActionListener(e -> btnActualizarActionPerformed());
        btnEliminar.addActionListener(e -> btnEliminarActionPerformed());
        btnLimpiar.addActionListener(e -> btnLimpiarActionPerformed());
        btnVolver.addActionListener(e -> btnVolverActionPerformed());
        
        // Listener para selección en tabla
        tablaNetbooks.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarDatosSeleccionados();
            }
        });
    }

    /**
     * Carga los datos de la fila seleccionada en el formulario.
     */
    private void cargarDatosSeleccionados() {
        int filaSeleccionada = tablaNetbooks.getSelectedRow();
        if (filaSeleccionada >= 0) {
            // Obtener datos de la tabla
            String carrito = tablaNetbooks.getValueAt(filaSeleccionada, 1).toString();
            String numero = tablaNetbooks.getValueAt(filaSeleccionada, 2).toString();
            String numeroSerie = tablaNetbooks.getValueAt(filaSeleccionada, 3).toString();
            String fechaAdquisicion = tablaNetbooks.getValueAt(filaSeleccionada, 4) != null ? 
                                    tablaNetbooks.getValueAt(filaSeleccionada, 4).toString() : "";
            String estado = tablaNetbooks.getValueAt(filaSeleccionada, 5).toString();

            // Cargar en el formulario
            comboCarrito.setSelectedItem(carrito);
            comboNumeroNetbook.setSelectedItem(numero);
            txtNumeroSerie.setText(numeroSerie);
            txtFechaAdquisicion.setText(fechaAdquisicion);
            comboEstado.setSelectedItem(estado);

            // Cargar observaciones desde la base de datos
            cargarObservaciones(carrito, numero);
        }
    }

    /**
     * Carga las observaciones desde la base de datos.
     */
    private void cargarObservaciones(String carrito, String numero) {
        try {
            String query = "SELECT observaciones FROM netbooks WHERE carrito = ? AND numero = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, carrito);
            ps.setString(2, numero);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String observaciones = rs.getString("observaciones");
                txtObservaciones.setText(observaciones != null ? observaciones : "");
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar observaciones: " + e.getMessage());
        }
    }

    /**
     * Verifica la conexión a la base de datos.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa los combobox con sus valores.
     */
    private void inicializarComboBoxes() {
        // Inicializar combobox de carritos (letras A a Z)
        String[] carritos = new String[26];
        for (int i = 0; i < 26; i++) {
            carritos[i] = String.valueOf((char) ('A' + i));
        }
        comboCarrito.setModel(new DefaultComboBoxModel<>(carritos));

        // Inicializar combobox de números de netbook (1 a 30)
        String[] numeros = new String[30];
        for (int i = 0; i < 30; i++) {
            numeros[i] = String.valueOf(i + 1);
        }
        comboNumeroNetbook.setModel(new DefaultComboBoxModel<>(numeros));

        // Inicializar combobox de estados
        comboEstado.setModel(new DefaultComboBoxModel<>(new String[]{"En uso", "Dañada", "Hurto", "Obsoleta"}));
    }

    /**
     * Carga la tabla de netbooks desde la base de datos.
     */
    private void cargarTablaNetbooks() {
        probar_conexion();
        DefaultTableModel modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("ID");
        modeloTabla.addColumn("Carrito");
        modeloTabla.addColumn("Número");
        modeloTabla.addColumn("N° Serie");
        modeloTabla.addColumn("Fecha Adquisición");
        modeloTabla.addColumn("Estado");

        tablaNetbooks.setModel(modeloTabla);

        Statement stmt;
        String[] datos = new String[6];

        try {
            stmt = conect.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM netbooks ORDER BY carrito, numero");

            while (rs.next()) {
                datos[0] = rs.getString("id");
                datos[1] = rs.getString("carrito");
                datos[2] = rs.getString("numero");
                datos[3] = rs.getString("numero_serie");
                datos[4] = rs.getString("fecha_adquisicion");
                datos[5] = rs.getString("estado");

                modeloTabla.addRow(datos);
            }

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al cargar datos de netbooks: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja la acción del botón volver.
     */
    private void btnVolverActionPerformed() {
        ventanaPrincipal.restaurarVistaPrincipal();
    }

    /**
     * Maneja la acción del botón limpiar.
     */
    private void btnLimpiarActionPerformed() {
        limpiarFormulario();
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        comboCarrito.setSelectedIndex(0);
        comboNumeroNetbook.setSelectedIndex(0);
        txtNumeroSerie.setText("");
        
        // Establecer fecha actual
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaAdquisicion.setText(fechaActual.format(formateadorFecha));

        comboEstado.setSelectedIndex(0);
        txtObservaciones.setText("");
        
        // Limpiar selección de tabla
        tablaNetbooks.clearSelection();
    }

    /**
     * Maneja la acción del botón registrar.
     */
    private void btnRegistrarActionPerformed() {
        // Validar que el número de serie no esté vacío
        if (txtNumeroSerie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo Número de Serie es obligatorio",
                    "Error de validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si ya existe una netbook con este carrito y número
        String carrito = comboCarrito.getSelectedItem().toString();
        String numero = comboNumeroNetbook.getSelectedItem().toString();

        try {
            PreparedStatement checkPs = conect.prepareStatement(
                    "SELECT COUNT(*) FROM netbooks WHERE carrito = ? AND numero = ?");
            checkPs.setString(1, carrito);
            checkPs.setString(2, numero);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe una netbook registrada con este carrito y número.\n" +
                        "Use el botón 'Actualizar' para modificar una netbook existente.",
                        "Netbook existente", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Si no existe, registrar nueva netbook
            registrarNetbook();

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al verificar netbook existente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja la acción del botón actualizar.
     */
    private void btnActualizarActionPerformed() {
        int filaSeleccionada = tablaNetbooks.getSelectedRow();
        
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una netbook de la tabla para actualizar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Validar campo obligatorio
        if (txtNumeroSerie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo Número de Serie es obligatorio",
                    "Error de validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String carrito = comboCarrito.getSelectedItem().toString();
        String numero = comboNumeroNetbook.getSelectedItem().toString();

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de actualizar esta netbook?",
                "Confirmar actualización", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            actualizarNetbook(carrito, numero);
        }
    }

    /**
     * Registra una nueva netbook.
     */
    private void registrarNetbook() {
        try {
            registrarLogInicial();

            PreparedStatement ps = conect.prepareStatement(
                    "INSERT INTO netbooks (carrito, numero, numero_serie, fecha_adquisicion, estado, observaciones) "
                    + "VALUES (?, ?, ?, ?, ?, ?)");

            ps.setString(1, comboCarrito.getSelectedItem().toString());
            ps.setString(2, comboNumeroNetbook.getSelectedItem().toString());
            ps.setString(3, txtNumeroSerie.getText().trim());
            ps.setString(4, txtFechaAdquisicion.getText().trim());
            ps.setString(5, comboEstado.getSelectedItem().toString());
            ps.setString(6, txtObservaciones.getText().trim());

            int resultado = ps.executeUpdate();

            if (resultado > 0) {
                JOptionPane.showMessageDialog(this, "Netbook registrada con éxito",
                        "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);

                registrarLogFinal();
                limpiarFormulario();
                cargarTablaNetbooks();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar la netbook",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al registrar netbook: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza una netbook existente.
     */
    private void actualizarNetbook(String carrito, String numero) {
        try {
            registrarLogInicial();

            PreparedStatement ps = conect.prepareStatement(
                    "UPDATE netbooks SET numero_serie = ?, fecha_adquisicion = ?, "
                    + "estado = ?, observaciones = ? WHERE carrito = ? AND numero = ?");

            ps.setString(1, txtNumeroSerie.getText().trim());
            ps.setString(2, txtFechaAdquisicion.getText().trim());
            ps.setString(3, comboEstado.getSelectedItem().toString());
            ps.setString(4, txtObservaciones.getText().trim());
            ps.setString(5, carrito);
            ps.setString(6, numero);

            int resultado = ps.executeUpdate();

            if (resultado > 0) {
                JOptionPane.showMessageDialog(this, "Netbook actualizada con éxito",
                        "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);

                registrarLogFinal();
                limpiarFormulario();
                cargarTablaNetbooks();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la netbook",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al actualizar netbook: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja la acción del botón eliminar.
     */
    private void btnEliminarActionPerformed() {
        int filaSeleccionada = tablaNetbooks.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una netbook de la tabla para eliminar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar esta netbook? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            String idNetbook = tablaNetbooks.getValueAt(filaSeleccionada, 0).toString();

            try {
                registrarLogInicial();

                PreparedStatement ps = conect.prepareStatement("DELETE FROM netbooks WHERE id = ?");
                ps.setString(1, idNetbook);

                int resultado = ps.executeUpdate();

                if (resultado > 0) {
                    JOptionPane.showMessageDialog(this, "Netbook eliminada con éxito",
                            "Eliminación exitosa", JOptionPane.INFORMATION_MESSAGE);

                    registrarLogFinal();
                    limpiarFormulario();
                    cargarTablaNetbooks();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la netbook",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, "Error al eliminar netbook: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Registra en el archivo de log el estado inicial antes de una operación.
     */
    private void registrarLogInicial() {
        try {
            Statement statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM netbooks");

            try (FileWriter escritor = new FileWriter("Registros.txt", true)) {
                escritor.write("\n");
                escritor.write("--- ESTADO INICIAL DE NETBOOKS ANTES DE OPERACIÓN ---\n");
                escritor.write("Fecha: " + LocalDate.now() + "\n");

                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String carrito = resultSet.getString("carrito");
                    String numero = resultSet.getString("numero");
                    String numeroSerie = resultSet.getString("numero_serie");
                    String fechaAdquisicion = resultSet.getString("fecha_adquisicion");
                    String estado = resultSet.getString("estado");

                    escritor.write("Netbook: " + id + "|" + carrito + "-" + numero + "|"
                            + numeroSerie + "|" + fechaAdquisicion + "|" + estado + "\n");
                }
            }
        } catch (SQLException | IOException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error al registrar log inicial: " + e.getMessage());
        }
    }

    /**
     * Registra en el archivo de log el estado final después de una operación.
     */
    private void registrarLogFinal() {
        try {
            Statement statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM netbooks");

            try (FileWriter escritor = new FileWriter("Registros.txt", true)) {
                escritor.write("\n");
                escritor.write("--- ESTADO FINAL DE NETBOOKS DESPUÉS DE OPERACIÓN ---\n");
                escritor.write("Fecha: " + LocalDate.now() + "\n");

                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String carrito = resultSet.getString("carrito");
                    String numero = resultSet.getString("numero");
                    String numeroSerie = resultSet.getString("numero_serie");
                    String fechaAdquisicion = resultSet.getString("fecha_adquisicion");
                    String estado = resultSet.getString("estado");

                    escritor.write("Netbook: " + id + "|" + carrito + "-" + numero + "|"
                            + numeroSerie + "|" + fechaAdquisicion + "|" + estado + "\n");
                }

                escritor.write("--- FIN DE OPERACIÓN ---\n\n");
            }
        } catch (SQLException | IOException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error al registrar log final: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
