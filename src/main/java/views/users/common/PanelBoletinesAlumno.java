
package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import main.java.utils.GestorBoletines;

/**
 * Panel simplificado para que los alumnos vean solo sus propios boletines
 */
public class PanelBoletinesAlumno extends JPanel {
    
    private final VentanaInicio ventana;
    private final int alumnoId;
    
    private JComboBox<String> comboAnioLectivo;
    private JComboBox<String> comboPeriodo;
    private JButton btnBuscar;
    private JButton btnAbrir;
    private JButton btnVolver;
    
    private JTable tablaBoletines;
    private DefaultTableModel modeloTabla;
    private List<GestorBoletines.InfoBoletin> boletinesAlumno;
    
    public PanelBoletinesAlumno(VentanaInicio ventana, int alumnoId) {
        this.ventana = ventana;
        this.alumnoId = alumnoId;
        
        initializeComponents();
        setupLayout();
        setupListeners();
        loadInitialData();
    }
    
    private void initializeComponents() {
        // Combos simplificados
        comboAnioLectivo = new JComboBox<>();
        comboPeriodo = new JComboBox<>();
        
        // Botones
        btnBuscar = new JButton("Buscar");
        btnAbrir = new JButton("Abrir Boletín");
        btnVolver = new JButton("Volver");
        
        styleButton(btnBuscar, new Color(33, 150, 243));
        styleButton(btnAbrir, new Color(76, 175, 80));
        styleButton(btnVolver, new Color(96, 125, 139));
        
        // Tabla simplificada
        String[] columnas = {"Período", "Fecha Generación", "Estado", "Disponible"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaBoletines = new JTable(modeloTabla);
        tablaBoletines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaBoletines.setRowHeight(30);
    }
    
    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel lblTitulo = new JLabel("Mis Boletines", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(33, 150, 243));
        
        // Panel de filtros
        JPanel panelFiltros = new JPanel(new GridBagLayout());
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panelFiltros.add(new JLabel("Año Lectivo:"), gbc);
        gbc.gridx = 1;
        panelFiltros.add(comboAnioLectivo, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        panelFiltros.add(new JLabel("Período:"), gbc);
        gbc.gridx = 3;
        panelFiltros.add(comboPeriodo, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnBuscar);
        panelBotones.add(btnAbrir);
        panelBotones.add(btnVolver);
        
        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelFiltros, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);
        
        // Tabla
        JScrollPane scrollTabla = new JScrollPane(tablaBoletines);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }
    
    private void setupListeners() {
        btnBuscar.addActionListener(this::buscarBoletines);
        btnAbrir.addActionListener(this::abrirBoletin);
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());
        
        // Doble clic para abrir
        tablaBoletines.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirBoletin(null);
                }
            }
        });
    }
    
    private void loadInitialData() {
        // Cargar años
        comboAnioLectivo.removeAllItems();
        int anioActual = LocalDate.now().getYear();
        for (int i = 0; i < 3; i++) {
            comboAnioLectivo.addItem(String.valueOf(anioActual - i));
        }
        
        // Cargar períodos
        comboPeriodo.removeAllItems();
        comboPeriodo.addItem("TODOS");
        for (String periodo : GestorBoletines.PERIODOS_VALIDOS) {
            comboPeriodo.addItem(periodo);
        }
        
        // Buscar boletines inicial
        buscarBoletines(null);
    }
    
    private void buscarBoletines(ActionEvent e) {
        SwingWorker<List<GestorBoletines.InfoBoletin>, Void> worker = new SwingWorker<List<GestorBoletines.InfoBoletin>, Void>() {
            @Override
            protected List<GestorBoletines.InfoBoletin> doInBackground() throws Exception {
                String anioLectivo = (String) comboAnioLectivo.getSelectedItem();
                return GestorBoletines.obtenerBoletinesAlumno(alumnoId, Integer.parseInt(anioLectivo));
            }
            
            @Override
            protected void done() {
                try {
                    boletinesAlumno = get();
                    actualizarTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelBoletinesAlumno.this,
                            "Error al buscar boletines: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        
        String periodoFiltro = (String) comboPeriodo.getSelectedItem();
        
        for (GestorBoletines.InfoBoletin boletin : boletinesAlumno) {
            // Filtrar por período si no es "TODOS"
            if (!"TODOS".equals(periodoFiltro) && !boletin.periodo.equals(periodoFiltro)) {
                continue;
            }
            
            Object[] fila = {
                boletin.periodo,
                boletin.fechaGeneracion != null ? 
                    boletin.fechaGeneracion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : 
                    "Sin fecha",
                boletin.estadoBoletin,
                boletin.archivoExiste ? "✅ Disponible" : "❌ No disponible"
            };
            
            modeloTabla.addRow(fila);
        }
        
        if (modeloTabla.getRowCount() == 0) {
            modeloTabla.addRow(new Object[]{"Sin boletines", "", "", ""});
        }
    }
    
    private void abrirBoletin(ActionEvent e) {
        int filaSeleccionada = tablaBoletines.getSelectedRow();
        if (filaSeleccionada == -1 || boletinesAlumno.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un boletín",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Encontrar el boletín correspondiente considerando el filtro
        String periodoFiltro = (String) comboPeriodo.getSelectedItem();
        List<GestorBoletines.InfoBoletin> boletinesFiltrados = new java.util.ArrayList<>();
        
        for (GestorBoletines.InfoBoletin boletin : boletinesAlumno) {
            if ("TODOS".equals(periodoFiltro) || boletin.periodo.equals(periodoFiltro)) {
                boletinesFiltrados.add(boletin);
            }
        }
        
        if (filaSeleccionada >= boletinesFiltrados.size()) {
            JOptionPane.showMessageDialog(this,
                    "Selección inválida",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        GestorBoletines.InfoBoletin boletin = boletinesFiltrados.get(filaSeleccionada);
        
        try {
            File archivo = new File(boletin.rutaArchivo);
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this,
                        "El boletín no está disponible.\n" +
                        "Contacte con la administración si necesita acceder a este boletín.",
                        "Boletín no disponible",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Abrir archivo
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el boletín: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}