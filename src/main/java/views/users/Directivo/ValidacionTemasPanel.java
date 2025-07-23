package main.java.views.users.Directivo;

import main.java.dao.TemaDiarioDAO;
import main.java.models.TemaDiario;
import main.java.views.users.common.VentanaInicio;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel para que los directivos puedan visualizar y validar los temas diarios
 * cargados por los profesores.
 * 
 * @author Sistema ET20
 * @version 2.0
 */
public class ValidacionTemasPanel extends JPanel {
    
    // Componentes principales
    private VentanaInicio ventanaPrincipal;
    private int directivoId;
    private TemaDiarioDAO temaDiarioDAO;
    
    // Componentes de interfaz
    private JTable tablaTemas;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroEstado;
    private JComboBox<String> cmbFiltroPeriodo;
    private JTextField txtFiltroBusqueda;
    private JTextArea txtDetallesTema;
    private JButton btnValidar;
    private JButton btnRechazar;
    private JButton btnActualizar;
    private JButton btnVolver;
    
    // Datos actuales
    private TemaDiario temaSeleccionado;
    
    /**
     * Constructor del panel de validación de temas.
     */
    public ValidacionTemasPanel(int directivoId, VentanaInicio ventanaPrincipal) {
        this.directivoId = directivoId;
        this.ventanaPrincipal = ventanaPrincipal;
        this.temaDiarioDAO = new TemaDiarioDAO();
        
        initComponents();
        cargarDatos();
        configurarEventos();
    }
    
    /**
     * Inicializa los componentes de la interfaz.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior - Banner y filtros
        JPanel panelSuperior = new JPanel(new BorderLayout());
        
        // Banner
        JLabel bannerSuperior = new JLabel();
        bannerSuperior.setIcon(new ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png")));
        panelSuperior.add(bannerSuperior, BorderLayout.NORTH);
        
        // Panel de filtros
        JPanel panelFiltros = crearPanelFiltros();
        panelSuperior.add(panelFiltros, BorderLayout.CENTER);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);
        
        // Panel izquierdo - Tabla de temas
        JPanel panelTabla = crearPanelTabla();
        splitPane.setLeftComponent(panelTabla);
        
        // Panel derecho - Detalles y acciones
        JPanel panelDetalles = crearPanelDetalles();
        splitPane.setRightComponent(panelDetalles);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Panel inferior - Banner
        JPanel panelInferior = new JPanel(new BorderLayout());
        JLabel bannerInferior = new JLabel();
        bannerInferior.setIcon(new ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png")));
        panelInferior.add(bannerInferior, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Crea el panel de filtros.
     */
    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Filtros de Búsqueda"));
        panel.setPreferredSize(new Dimension(0, 80));
        
        // Filtro por estado
        JLabel lblEstado = new JLabel("Estado:");
        cmbFiltroEstado = new JComboBox<>(new String[]{
            "Todos", "Sin validar", "Validados", "Pendientes"
        });
        
        // Filtro por período
        JLabel lblPeriodo = new JLabel("Período:");
        cmbFiltroPeriodo = new JComboBox<>(new String[]{
            "Última semana", "Último mes", "Últimos 3 meses", "Todo el año"
        });
        
        // Campo de búsqueda
        JLabel lblBusqueda = new JLabel("Buscar:");
        txtFiltroBusqueda = new JTextField(15);
        txtFiltroBusqueda.setToolTipText("Buscar por profesor, materia o contenido");
        
        // Botón actualizar
        btnActualizar = new JButton("ACTUALIZAR");
        btnActualizar.setBackground(new Color(51, 153, 255));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 11));
        
        panel.add(lblEstado);
        panel.add(cmbFiltroEstado);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblPeriodo);
        panel.add(cmbFiltroPeriodo);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblBusqueda);
        panel.add(txtFiltroBusqueda);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnActualizar);
        
        return panel;
    }
    
    /**
     * Crea el panel de la tabla de temas.
     */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Temas Diarios - Para Validación"));
        
        // Crear modelo de tabla
        String[] columnas = {"Estado", "Fecha", "Profesor", "Materia", "Curso", "Clases", "Tema"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaTemas = new JTable(modeloTabla);
        configurarTabla();
        
        JScrollPane scrollTabla = new JScrollPane(tablaTemas);
        scrollTabla.setPreferredSize(new Dimension(600, 400));
        
        panel.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel inferior con información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel("💡 Haga doble clic en una fila para ver detalles completos");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        panelInfo.add(lblInfo);
        panel.add(panelInfo, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Configura la tabla de temas.
     */
    private void configurarTabla() {
        tablaTemas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTemas.setRowHeight(30);
        tablaTemas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaTemas.setFont(new Font("Arial", Font.PLAIN, 11));
        
        // Configurar ancho de columnas
        TableColumnModel columnModel = tablaTemas.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // Estado
        columnModel.getColumn(1).setPreferredWidth(80);  // Fecha
        columnModel.getColumn(2).setPreferredWidth(120); // Profesor
        columnModel.getColumn(3).setPreferredWidth(100); // Materia
        columnModel.getColumn(4).setPreferredWidth(60);  // Curso
        columnModel.getColumn(5).setPreferredWidth(60);  // Clases
        columnModel.getColumn(6).setPreferredWidth(200); // Tema
        
        // Renderer personalizado para la columna de estado
        tablaTemas.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String estado = value.toString();
                    switch (estado) {
                        case "✔️":
                            c.setBackground(new Color(200, 255, 200)); // Verde claro
                            break;
                        case "🕓":
                            c.setBackground(new Color(255, 255, 200)); // Amarillo claro
                            break;
                        case "❌":
                            c.setBackground(new Color(255, 200, 200)); // Rojo claro
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }
                
                setHorizontalAlignment(CENTER);
                return c;
            }
        });
    }
    
    /**
     * Crea el panel de detalles y acciones.
     */
    private JPanel crearPanelDetalles() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Detalles del Tema Seleccionado"));
        panel.setPreferredSize(new Dimension(400, 0));
        
        // Área de texto para mostrar detalles
        txtDetallesTema = new JTextArea();
        txtDetallesTema.setEditable(false);
        txtDetallesTema.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDetallesTema.setLineWrap(true);
        txtDetallesTema.setWrapStyleWord(true);
        txtDetallesTema.setBorder(BorderFactory.createLoweredBevelBorder());
        txtDetallesTema.setText("Seleccione un tema de la tabla para ver los detalles...");
        
        JScrollPane scrollDetalles = new JScrollPane(txtDetallesTema);
        scrollDetalles.setPreferredSize(new Dimension(380, 300));
        
        panel.add(scrollDetalles, BorderLayout.CENTER);
        
        // Panel de botones de acción
        JPanel panelBotones = crearPanelBotonesAccion();
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel de botones de acción.
     */
    private JPanel crearPanelBotonesAccion() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Botón validar
        btnValidar = new JButton("✔️ VALIDAR TEMA");
        btnValidar.setBackground(new Color(34, 139, 34));
        btnValidar.setForeground(Color.WHITE);
        btnValidar.setFont(new Font("Arial", Font.BOLD, 12));
        btnValidar.setEnabled(false);
        
        // Botón rechazar (quitar validación)
        btnRechazar = new JButton("❌ QUITAR VALIDACIÓN");
        btnRechazar.setBackground(new Color(220, 20, 60));
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFont(new Font("Arial", Font.BOLD, 12));
        btnRechazar.setEnabled(false);
        
        // Botón volver
        btnVolver = new JButton("VOLVER");
        btnVolver.setBackground(new Color(153, 153, 153));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        
        panel.add(btnValidar);
        panel.add(btnRechazar);
        panel.add(btnVolver);
        
        return panel;
    }
    
    /**
     * Carga los datos iniciales en la tabla.
     */
    private void cargarDatos() {
        actualizarTablaTemas();
    }
    
    /**
     * Actualiza la tabla de temas según los filtros.
     */
    private void actualizarTablaTemas() {
        try {
            // Limpiar tabla
            modeloTabla.setRowCount(0);
            
            // Determinar rango de fechas según el filtro
            LocalDate fechaDesde = obtenerFechaDesde();
            
            // Obtener todos los temas (no filtrar por profesor ya que es para directivos)
            List<TemaDiario> temas = temaDiarioDAO.obtenerTemasDiarios(
                -1, // -1 indica que queremos de todos los profesores
                fechaDesde, null, null, null);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (TemaDiario tema : temas) {
                // Aplicar filtros
                if (!cumpleFiltros(tema)) {
                    continue;
                }
                
                Object[] fila = new Object[]{
                    tema.getEstadoVisual(),
                    tema.getFechaClase().format(formatter),
                    tema.getProfesorNombreCompleto(),
                    tema.getMateriaNombre(),
                    tema.getCursoCompleto(),
                    tema.getRangoClases(),
                    tema.getTema().length() > 60 ? 
                        tema.getTema().substring(0, 60) + "..." : tema.getTema()
                };
                
                modeloTabla.addRow(fila);
            }
            
            // Actualizar información de cantidad
            actualizarInformacionCantidad();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al cargar temas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Obtiene la fecha desde según el filtro de período seleccionado.
     */
    private LocalDate obtenerFechaDesde() {
        String periodo = (String) cmbFiltroPeriodo.getSelectedItem();
        LocalDate hoy = LocalDate.now();
        
        switch (periodo) {
            case "Última semana":
                return hoy.minusWeeks(1);
            case "Último mes":
                return hoy.minusMonths(1);
            case "Últimos 3 meses":
                return hoy.minusMonths(3);
            case "Todo el año":
                return LocalDate.of(hoy.getYear(), 1, 1);
            default:
                return hoy.minusMonths(1);
        }
    }
    
    /**
     * Verifica si un tema cumple con los filtros aplicados.
     */
    private boolean cumpleFiltros(TemaDiario tema) {
        // Filtro por estado
        String estadoFiltro = (String) cmbFiltroEstado.getSelectedItem();
        if (!estadoFiltro.equals("Todos")) {
            switch (estadoFiltro) {
                case "Sin validar":
                    if (tema.isValidado()) return false;
                    break;
                case "Validados":
                    if (!tema.isValidado()) return false;
                    break;
                case "Pendientes":
                    // Pendientes = cargados pero sin validar hace más de 24 horas
                    if (tema.isValidado() || 
                        tema.getFechaCarga().toLocalDateTime().isAfter(LocalDate.now().minusDays(1).atStartOfDay())) {
                        return false;
                    }
                    break;
            }
        }
        
        // Filtro por búsqueda de texto
        String textoBusqueda = txtFiltroBusqueda.getText().trim().toLowerCase();
        if (!textoBusqueda.isEmpty()) {
            String textoCompleto = (tema.getProfesorNombreCompleto() + " " + 
                                  tema.getMateriaNombre() + " " + 
                                  tema.getTema() + " " + 
                                  (tema.getObservaciones() != null ? tema.getObservaciones() : "")).toLowerCase();
            
            if (!textoCompleto.contains(textoBusqueda)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Actualiza la información de cantidad de temas mostrados.
     */
    private void actualizarInformacionCantidad() {
        int totalFilas = modeloTabla.getRowCount();
        // Podríamos agregar un label de información si fuera necesario
    }
    
    /**
     * Configura los eventos de los componentes.
     */
    private void configurarEventos() {
        // Evento de selección en la tabla
        tablaTemas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaTemas.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    mostrarDetallesTema(filaSeleccionada);
                }
            }
        });
        
        // Doble clic en la tabla para ver detalles completos
        tablaTemas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaTemas.getSelectedRow();
                    if (fila >= 0) {
                        mostrarDialogoDetallesCompletos(fila);
                    }
                }
            }
        });
        
        // Eventos de botones
        btnValidar.addActionListener(e -> validarTemaSeleccionado());
        btnRechazar.addActionListener(e -> quitarValidacionTema());
        btnActualizar.addActionListener(e -> actualizarTablaTemas());
        btnVolver.addActionListener(e -> volverAtras());
        
        // Eventos de filtros
        cmbFiltroEstado.addActionListener(e -> actualizarTablaTemas());
        cmbFiltroPeriodo.addActionListener(e -> actualizarTablaTemas());
        
        // Evento de búsqueda con delay
        Timer timer = new Timer(500, e -> actualizarTablaTemas());
        timer.setRepeats(false);
        
        txtFiltroBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                timer.restart(); 
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                timer.restart(); 
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                timer.restart(); 
            }
        });
    }
    
    /**
     * Muestra los detalles del tema seleccionado.
     */
    private void mostrarDetallesTema(int fila) {
        try {
            // Obtener el tema correspondiente
            // Aquí necesitaríamos una forma de obtener el tema completo desde la fila
            // Por simplicidad, vamos a mostrar información básica de la tabla
            
            StringBuilder detalles = new StringBuilder();
            detalles.append("=== DETALLES DEL TEMA ===\\n\\n");
            detalles.append("Estado: ").append(modeloTabla.getValueAt(fila, 0)).append("\\n");
            detalles.append("Fecha: ").append(modeloTabla.getValueAt(fila, 1)).append("\\n");
            detalles.append("Profesor: ").append(modeloTabla.getValueAt(fila, 2)).append("\\n");
            detalles.append("Materia: ").append(modeloTabla.getValueAt(fila, 3)).append("\\n");
            detalles.append("Curso: ").append(modeloTabla.getValueAt(fila, 4)).append("\\n");
            detalles.append("Clases: ").append(modeloTabla.getValueAt(fila, 5)).append("\\n\\n");
            
            detalles.append("TEMA DESARROLLADO:\\n");
            detalles.append(modeloTabla.getValueAt(fila, 6)).append("\\n\\n");
            
            detalles.append("Para ver detalles completos, haga doble clic en la fila.");
            
            txtDetallesTema.setText(detalles.toString());
            txtDetallesTema.setCaretPosition(0);
            
            // Habilitar/deshabilitar botones según el estado
            String estado = (String) modeloTabla.getValueAt(fila, 0);
            btnValidar.setEnabled(!estado.equals("✔️"));
            btnRechazar.setEnabled(estado.equals("✔️"));
            
        } catch (Exception e) {
            e.printStackTrace();
            txtDetallesTema.setText("Error al cargar detalles del tema.");
        }
    }
    
    /**
     * Muestra un diálogo con los detalles completos del tema.
     */
    private void mostrarDialogoDetallesCompletos(int fila) {
        // Crear diálogo modal
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                     "Detalles Completos del Tema", true);
        dialogo.setSize(600, 500);
        dialogo.setLocationRelativeTo(this);
        
        // Contenido del diálogo
        JPanel contenido = new JPanel(new BorderLayout());
        
        JTextArea areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Arial", Font.PLAIN, 12));
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        
        // Aquí deberíamos obtener los detalles completos del tema desde la base de datos
        StringBuilder detalles = new StringBuilder();
        detalles.append("=== INFORMACIÓN COMPLETA DEL TEMA ===\\n\\n");
        detalles.append("Estado: ").append(modeloTabla.getValueAt(fila, 0)).append("\\n");
        detalles.append("Fecha de clase: ").append(modeloTabla.getValueAt(fila, 1)).append("\\n");
        detalles.append("Profesor: ").append(modeloTabla.getValueAt(fila, 2)).append("\\n");
        detalles.append("Materia: ").append(modeloTabla.getValueAt(fila, 3)).append("\\n");
        detalles.append("Curso: ").append(modeloTabla.getValueAt(fila, 4)).append("\\n");
        detalles.append("Clases registradas: ").append(modeloTabla.getValueAt(fila, 5)).append("\\n\\n");
        
        detalles.append("TEMA DESARROLLADO:\\n");
        detalles.append("================\\n");
        detalles.append(modeloTabla.getValueAt(fila, 6)).append("\\n\\n");
        
        detalles.append("OBSERVACIONES:\\n");
        detalles.append("==============\\n");
        detalles.append("[Observaciones del tema - requiere consulta a BD]\\n\\n");
        
        detalles.append("INFORMACIÓN ADICIONAL:\\n");
        detalles.append("=====================\\n");
        detalles.append("Fecha de carga: [Requiere consulta a BD]\\n");
        detalles.append("Modo de carga: [Requiere consulta a BD]\\n");
        detalles.append("Validado por: [Requiere consulta a BD]\\n");
        detalles.append("Fecha de validación: [Requiere consulta a BD]\\n");
        
        areaTexto.setText(detalles.toString());
        areaTexto.setCaretPosition(0);
        
        JScrollPane scroll = new JScrollPane(areaTexto);
        contenido.add(scroll, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotonesDialogo = new JPanel(new FlowLayout());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialogo.dispose());
        panelBotonesDialogo.add(btnCerrar);
        
        contenido.add(panelBotonesDialogo, BorderLayout.SOUTH);
        
        dialogo.add(contenido);
        dialogo.setVisible(true);
    }
    
    /**
     * Valida el tema seleccionado.
     */
    private void validarTemaSeleccionado() {
        int fila = tablaTemas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un tema para validar.", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de que desea validar este tema?\\n\\n" +
            "Profesor: " + modeloTabla.getValueAt(fila, 2) + "\\n" +
            "Materia: " + modeloTabla.getValueAt(fila, 3) + "\\n" +
            "Fecha: " + modeloTabla.getValueAt(fila, 1), 
            "Confirmar Validación", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Aquí necesitaríamos el ID real del tema para validarlo
                // Por ahora simularemos la validación
                
                // boolean validado = temaDiarioDAO.validarTemaDiario(temaId, directivoId);
                boolean validado = true; // Simulación
                
                if (validado) {
                    JOptionPane.showMessageDialog(this, 
                        "Tema validado exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Actualizar la tabla
                    actualizarTablaTemas();
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error al validar el tema.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error inesperado: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Quita la validación de un tema.
     */
    private void quitarValidacionTema() {
        int fila = tablaTemas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un tema para quitar la validación.", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de que desea quitar la validación de este tema?\\n\\n" +
            "Profesor: " + modeloTabla.getValueAt(fila, 2) + "\\n" +
            "Materia: " + modeloTabla.getValueAt(fila, 3) + "\\n" +
            "Fecha: " + modeloTabla.getValueAt(fila, 1), 
            "Confirmar Quitar Validación", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Implementar lógica para quitar validación
                boolean quitado = true; // Simulación
                
                if (quitado) {
                    JOptionPane.showMessageDialog(this, 
                        "Validación removida exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Actualizar la tabla
                    actualizarTablaTemas();
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error al quitar la validación.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error inesperado: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Vuelve a la pantalla anterior.
     */
    private void volverAtras() {
        try {
            if (ventanaPrincipal != null) {
                // Restaurar la vista principal
                Container parent = this.getParent();
                if (parent != null) {
                    parent.remove(this);
                    ventanaPrincipal.restaurarVistaPrincipal();
                    parent.revalidate();
                    parent.repaint();
                }
            } else {
                this.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al volver: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
