package main.java.views.users.Profesor;

import main.java.dao.TemaDiarioDAO;
import main.java.services.UserService;
import main.java.services.UserService.UserInfo;
import main.java.models.TemaDiario;
import main.java.models.ConfiguracionBloque;
import main.java.models.ContadorClases;
import main.java.views.users.common.VentanaInicio;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Panel mejorado para la carga y visualización de temas diarios.
 * Incluye funcionalidad de numeración automática de clases, 
 * modos de carga simple/múltiple y sistema de validación.
 * 
 * @author Sistema ET20
 * @version 2.0
 */
public class CargaTemasPanel extends JPanel {
    
    // Componentes principales
    private VentanaInicio ventanaPrincipal;
    private int profesorId;
    private TemaDiarioDAO temaDiarioDAO;
    private UserService userService;
    private UserInfo usuarioActual;
    
    // Componentes de interfaz
    private JComboBox<ComboItem> cmbMateriasCursos;
    private JRadioButton rbModoSimple;
    private JRadioButton rbModoMultiple;
    private ButtonGroup grupoBotones;
    private JSpinner spnBloqueNumero;
    private JTextArea txtTema;
    private JTextArea txtActividadesDesarrolladas;  // Nuevo campo
    private JTextArea txtObservaciones;
    private JDateChooser fechaSelector;  // Selector de fecha
    private JComboBox<String> cmbCaracterClase;  // Selector de carácter de clase
    private JTable tablaTemas;
    private DefaultTableModel modeloTabla;
    private JLabel lblClasesInfo;
    private JLabel lblProximasClases;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnVolver;
    private JButton btnEditar;  // Nuevo botón editar
    private JButton btnCancelar; // Nuevo botón cancelar edición
    
    // Datos actuales
    private Map<String, Object> materiaActual;
    private ConfiguracionBloque configActual;
    private ContadorClases contadorActual;
    private List<TemaDiario> temasActuales; // Para poder editarlos
    private TemaDiario temaEnEdicion; // Para saber si estamos editando
    
    /**
     * Constructor del panel de carga de temas.
     */
    public CargaTemasPanel(int profesorId, VentanaInicio ventanaPrincipal) {
        this.profesorId = profesorId;
        this.ventanaPrincipal = ventanaPrincipal;
        this.temaDiarioDAO = new TemaDiarioDAO();
        this.userService = new UserService();
        
        // Obtener información del usuario para verificar rol
        this.usuarioActual = userService.obtenerInfoUsuario(profesorId);
        
        initComponents();
        cargarDatos();
        configurarEventos();
        configurarAccesoSegunRol();
    }
    
    /**
     * Inicializa los componentes de la interfaz.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior - Banners
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JLabel bannerSuperior = new JLabel();
        bannerSuperior.setIcon(new ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png")));
        panelSuperior.add(bannerSuperior, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel izquierdo - Formulario de carga
        JPanel panelFormulario = crearPanelFormulario();
        
        // Panel derecho - Tabla de temas
        JPanel panelTabla = crearPanelTabla();
        
        // Dividir el panel central
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelFormulario, panelTabla);
        splitPane.setDividerLocation(480); // Un poco más ancho para el formulario
        splitPane.setResizeWeight(0.45);
        
        panelCentral.add(splitPane, BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);
        
        // Panel inferior - Banners
        JPanel panelInferior = new JPanel(new BorderLayout());
        JLabel bannerInferior = new JLabel();
        bannerInferior.setIcon(new ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png")));
        panelInferior.add(bannerInferior, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Crea el panel de formulario para carga de temas.
     */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Cargar Nuevo Tema"));
        // Forzar altura mínima que definitivamente active el scroll
        panel.setPreferredSize(new Dimension(500, 900)); // Altura mayor
        panel.setMinimumSize(new Dimension(500, 900));
        
        // Selector de materia y curso
        panel.add(crearPanelSelectorMateria());
        panel.add(Box.createVerticalStrut(8)); // Menos espacio
        
        // Información de clases
        panel.add(crearPanelInfoClases());
        panel.add(Box.createVerticalStrut(8)); // Menos espacio
        
        // Modo de carga
        panel.add(crearPanelModoCarga());
        panel.add(Box.createVerticalStrut(8)); // Menos espacio
        
        // Formulario de tema
        panel.add(crearPanelFormularioTema());
        panel.add(Box.createVerticalStrut(10)); // Menos espacio
        
        // Botones
        panel.add(crearPanelBotones());
        
        // Agregar espacio considerable al final para garantizar scroll
        panel.add(Box.createVerticalStrut(100)); // Más espacio
        panel.add(Box.createVerticalGlue()); // Espacio flexible que empuje el contenido hacia arriba
        
        return panel;
    }
    
    /**
     * Crea el panel selector de materia y curso.
     */
    private JPanel crearPanelSelectorMateria() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Materia y Curso"));
        
        JLabel lblMateria = new JLabel("Seleccionar:");
        cmbMateriasCursos = new JComboBox<>();
        cmbMateriasCursos.setPreferredSize(new Dimension(300, 25));
        
        panel.add(lblMateria);
        panel.add(cmbMateriasCursos);
        
        return panel;
    }
    
    /**
     * Crea el panel de información de clases.
     */
    private JPanel crearPanelInfoClases() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Información de Clases"));
        
        lblClasesInfo = new JLabel("Clases dictadas: -");
        lblClasesInfo.setFont(new Font("Arial", Font.BOLD, 12));
        
        lblProximasClases = new JLabel("Próximas clases: -");
        lblProximasClases.setFont(new Font("Arial", Font.BOLD, 12));
        lblProximasClases.setForeground(new Color(0, 100, 0));
        
        panel.add(lblClasesInfo);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblProximasClases);
        
        return panel;
    }
    
    /**
     * Crea el panel de modo de carga.
     */
    private JPanel crearPanelModoCarga() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Modo de Carga"));
        
        rbModoSimple = new JRadioButton("Carga Simple (un tema por jornada)", true);
        rbModoMultiple = new JRadioButton("Carga Múltiple (tema por bloque)");
        
        grupoBotones = new ButtonGroup();
        grupoBotones.add(rbModoSimple);
        grupoBotones.add(rbModoMultiple);
        
        JPanel panelBloque = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblBloque = new JLabel("Número de bloque:");
        spnBloqueNumero = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spnBloqueNumero.setEnabled(false);
        spnBloqueNumero.setPreferredSize(new Dimension(60, 25));
        
        panelBloque.add(lblBloque);
        panelBloque.add(spnBloqueNumero);
        
        panel.add(rbModoSimple);
        panel.add(rbModoMultiple);
        panel.add(panelBloque);
        
        return panel;
    }
    
    /**
     * Crea el panel del formulario de tema.
     */
    private JPanel crearPanelFormularioTema() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Contenido del Tema"));
        
        // Panel superior con fecha y carácter
        JPanel panelSuperior = new JPanel(new GridLayout(2, 2, 8, 3)); // Menos espaciado
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Menos padding
        panelSuperior.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Más compacto
        
        // Selector de fecha
        panelSuperior.add(new JLabel("Fecha de la clase:"));
        fechaSelector = new JDateChooser();
        fechaSelector.setDate(java.sql.Date.valueOf(LocalDate.now()));
        fechaSelector.setPreferredSize(new Dimension(150, 25));
        panelSuperior.add(fechaSelector);
        
        // Selector de carácter de clase
        panelSuperior.add(new JLabel("Carácter de la clase:"));
        String[] caracteresClase = {
            "Diagnóstica", "Explicativa", "Introductoria", "Motivadora", 
            "Reflexiva", "Analítica", "Evaluativa", "Investigativa", 
            "Elaborativa", "Participativa", "Práctica", "Consultiva", 
            "Interpretativa", "Dialógica", "Teórica", "Otros..."
        };
        cmbCaracterClase = new JComboBox<>(caracteresClase);
        cmbCaracterClase.setSelectedIndex(1); // "Explicativa" por defecto
        panelSuperior.add(cmbCaracterClase);
        
        // Campo para el tema - MÁS GRANDE Y VISIBLE
        JPanel panelTema = new JPanel(new BorderLayout());
        panelTema.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel lblTema = new JLabel("Tema y Actividades Desarrolladas (OBLIGATORIO):");
        lblTema.setFont(new Font("Arial", Font.BOLD, 12));
        lblTema.setForeground(new Color(0, 100, 0));
        panelTema.add(lblTema, BorderLayout.NORTH);
        
        txtTema = new JTextArea(5, 40);  // Menos filas para ser más compacto
        txtTema.setLineWrap(true);
        txtTema.setWrapStyleWord(true);
        txtTema.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        txtTema.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollTema = new JScrollPane(txtTema);
        scrollTema.setPreferredSize(new Dimension(460, 120)); // Más compacto
        scrollTema.setMinimumSize(new Dimension(460, 120));
        scrollTema.setBorder(BorderFactory.createTitledBorder("Escriba aquí el contenido del tema"));
        panelTema.add(scrollTema, BorderLayout.CENTER);
        
        // Campo para actividades desarrolladas (NUEVO)
        JPanel panelActividades = new JPanel(new BorderLayout());
        panelActividades.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JLabel lblActividades = new JLabel("Actividades Desarrolladas (OBLIGATORIO):");
        lblActividades.setFont(new Font("Arial", Font.BOLD, 11));
        lblActividades.setForeground(new Color(0, 100, 0));
        panelActividades.add(lblActividades, BorderLayout.NORTH);
        
        txtActividadesDesarrolladas = new JTextArea(5, 40);
        txtActividadesDesarrolladas.setLineWrap(true);
        txtActividadesDesarrolladas.setWrapStyleWord(true);
        txtActividadesDesarrolladas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        txtActividadesDesarrolladas.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollActividades = new JScrollPane(txtActividadesDesarrolladas);
        scrollActividades.setPreferredSize(new Dimension(460, 120));
        scrollActividades.setMinimumSize(new Dimension(460, 120));
        scrollActividades.setBorder(BorderFactory.createTitledBorder("Escriba aquí las actividades realizadas durante la clase"));
        panelActividades.add(scrollActividades, BorderLayout.CENTER);
        
        // Campo para observaciones
        JPanel panelObs = new JPanel(new BorderLayout());
        panelObs.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JLabel lblObs = new JLabel("Observaciones (opcional):");
        lblObs.setFont(new Font("Arial", Font.PLAIN, 11));
        panelObs.add(lblObs, BorderLayout.NORTH);
        
        txtObservaciones = new JTextArea(2, 40); // Más compacto
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));
        txtObservaciones.setFont(new Font("Arial", Font.PLAIN, 11));
        
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(460, 60)); // Más compacto
        scrollObs.setMinimumSize(new Dimension(460, 60));
        panelObs.add(scrollObs, BorderLayout.CENTER);
        
        // Agregar todos los paneles con menos espaciado
        panel.add(panelSuperior);
        panel.add(Box.createVerticalStrut(5)); // Menos espacio
        panel.add(panelTema);
        panel.add(Box.createVerticalStrut(5)); // Menos espacio
        panel.add(panelActividades);  // Nuevo panel
        panel.add(Box.createVerticalStrut(5)); // Menos espacio
        panel.add(panelObs);
        
        return panel;
    }
    
    /**
     * Crea el panel de botones.
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(new Color(248, 248, 248));
        
        btnGuardar = new JButton("GUARDAR TEMA");
        btnGuardar.setBackground(new Color(51, 153, 255));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.setPreferredSize(new Dimension(150, 40));
        
        btnEditar = new JButton("EDITAR SELECCIONADO");
        btnEditar.setBackground(new Color(34, 139, 34)); // Verde
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFont(new Font("Arial", Font.BOLD, 11));
        btnEditar.setPreferredSize(new Dimension(180, 40));
        btnEditar.setEnabled(false); // Deshabilitado hasta seleccionar
        
        btnActualizar = new JButton("ACTUALIZAR VISTA");
        btnActualizar.setBackground(new Color(255, 153, 51));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 11));
        btnActualizar.setPreferredSize(new Dimension(150, 40));
        
        btnVolver = new JButton("VOLVER");
        btnVolver.setBackground(new Color(153, 153, 153));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setPreferredSize(new Dimension(120, 40));
        
        btnCancelar = new JButton("CANCELAR");
        btnCancelar.setBackground(new Color(220, 53, 69)); // Rojo
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 11));
        btnCancelar.setPreferredSize(new Dimension(120, 40));
        btnCancelar.setVisible(false); // Oculto por defecto
        
        panel.add(btnGuardar);
        panel.add(btnEditar);
        panel.add(btnCancelar); // Agregar botón cancelar
        panel.add(btnActualizar);
        panel.add(btnVolver);
        
        return panel;
    }
    
    /**
     * Crea el panel de la tabla de temas.
     */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Temas Cargados"));
        
        // Crear modelo de tabla
        String[] columnas = {"Fecha", "Día", "Clases", "Carácter", "Tema", "Actividades Desarrolladas", "Observaciones", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaTemas = new JTable(modeloTabla);
        configurarTabla();
        
        JScrollPane scrollTabla = new JScrollPane(tablaTemas);
        scrollTabla.setPreferredSize(new Dimension(500, 400));
        
        panel.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel de filtros
        JPanel panelFiltros = crearPanelFiltros();
        panel.add(panelFiltros, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * Configura la apariencia de la tabla.
     */
    private void configurarTabla() {
        tablaTemas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTemas.setRowHeight(25);
        tablaTemas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaTemas.setFont(new Font("Arial", Font.PLAIN, 11));
        
        // Listener para habilitar/deshabilitar botón editar
        tablaTemas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaTemas.getSelectedRow();
                btnEditar.setEnabled(filaSeleccionada >= 0);
            }
        });
        
        // Configurar ancho de columnas
        TableColumnModel columnModel = tablaTemas.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // Fecha
        columnModel.getColumn(1).setPreferredWidth(80);  // Día
        columnModel.getColumn(2).setPreferredWidth(60);  // Clases
        columnModel.getColumn(3).setPreferredWidth(90);  // Carácter
        columnModel.getColumn(4).setPreferredWidth(150); // Tema
        columnModel.getColumn(5).setPreferredWidth(150); // Actividades Desarrolladas
        columnModel.getColumn(6).setPreferredWidth(120); // Observaciones
        columnModel.getColumn(7).setPreferredWidth(60);  // Estado
    }
    
    /**
     * Crea el panel de filtros para la tabla.
     */
    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel lblFiltro = new JLabel("Mostrar últimos:");
        JComboBox<String> cmbFiltro = new JComboBox<>(new String[]{
            "7 días", "15 días", "30 días", "Todo el mes", "Todo el año"
        });
        
        panel.add(lblFiltro);
        panel.add(cmbFiltro);
        
        return panel;
    }
    
    /**
     * Carga los datos iniciales.
     */
    private void cargarDatos() {
        cargarMateriasYCursos();
        actualizarTablaTemas();
    }
    
    /**
     * Carga las materias y cursos disponibles para el profesor.
     */
    private void cargarMateriasYCursos() {
        try {
            List<Map<String, Object>> materias = temaDiarioDAO.obtenerMateriasYCursosProfesor(profesorId);
            
            cmbMateriasCursos.removeAllItems();
            cmbMateriasCursos.addItem(new ComboItem("Seleccione materia y curso...", null));
            
            for (Map<String, Object> materia : materias) {
                String texto = String.format("%s - %d° %d° (%s)",
                    materia.get("materiaNombre"),
                    materia.get("cursoAnio"),
                    materia.get("cursoDivision"),
                    materia.get("cursoTurno")
                );
                
                cmbMateriasCursos.addItem(new ComboItem(texto, materia));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al cargar materias y cursos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Actualiza la información de clases cuando se selecciona una materia.
     */
    private void actualizarInfoClases() {
        if (materiaActual == null) {
            lblClasesInfo.setText("Clases dictadas: -");
            lblProximasClases.setText("Próximas clases: -");
            return;
        }
        
        try {
            int cursoId = (Integer) materiaActual.get("cursoId");
            int materiaId = (Integer) materiaActual.get("materiaId");
            
            // Obtener contador de clases
            contadorActual = temaDiarioDAO.obtenerContadorClases(profesorId, cursoId, materiaId);
            
            // Obtener configuración de bloques
            configActual = temaDiarioDAO.obtenerConfiguracionBloque(materiaId);
            
            // Calcular bloques del día específicamente para esta materia y curso
            int bloquesHoy = temaDiarioDAO.calcularBloquesDelDia(profesorId, materiaId, cursoId, LocalDate.now());
            
            // Obtener próximo rango de clases
            int[] proximoRango = temaDiarioDAO.obtenerProximoNumeroClase(profesorId, cursoId, materiaId, bloquesHoy);
            
            // Actualizar labels
            lblClasesInfo.setText(String.format("Clases dictadas: %d (última: %d)", 
                contadorActual.getTotalClases(), contadorActual.getUltimaClase()));
            
            if (bloquesHoy > 0) {
                String rangoTexto = proximoRango[0] == proximoRango[1] ? 
                    "Clase " + proximoRango[0] :
                    "Clases " + proximoRango[0] + " a " + proximoRango[1];
                
                lblProximasClases.setText("Próximas clases: " + rangoTexto + " (" + bloquesHoy + " bloques)");
            } else {
                lblProximasClases.setText("Sin clases programadas para hoy");
            }
            
            // Actualizar disponibilidad del modo múltiple
            rbModoMultiple.setEnabled(configActual.isPermiteCargaMultiple() && bloquesHoy > 1);
            if (!rbModoMultiple.isEnabled()) {
                rbModoSimple.setSelected(true);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            lblClasesInfo.setText("Error al cargar información");
            lblProximasClases.setText("Error al cargar información");
        }
    }
    
    /**
     * Actualiza la tabla de temas.
     */
    private void actualizarTablaTemas() {
        try {
            // Limpiar tabla
            modeloTabla.setRowCount(0);
            
            if (materiaActual != null) {
                int cursoId = (Integer) materiaActual.get("cursoId");
                int materiaId = (Integer) materiaActual.get("materiaId");
                
                // Obtener temas de los últimos 30 días y almacenarlos
                LocalDate fechaDesde = LocalDate.now().minusDays(30);
                temasActuales = temaDiarioDAO.obtenerTemasDiarios(
                    profesorId, fechaDesde, null, cursoId, materiaId);
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                
                for (TemaDiario tema : temasActuales) {
                    String observaciones = tema.getObservaciones();
                    String obsResumida = "";
                    if (observaciones != null && !observaciones.trim().isEmpty()) {
                        obsResumida = observaciones.length() > 30 ? 
                            observaciones.substring(0, 30) + "..." : observaciones;
                    }
                    
                    String actividades = tema.getActividadesDesarrolladas();
                    String actResumidas = "";
                    if (actividades != null && !actividades.trim().isEmpty()) {
                        actResumidas = actividades.length() > 40 ? 
                            actividades.substring(0, 40) + "..." : actividades;
                    }
                    
                    Object[] fila = new Object[]{
                        tema.getFechaClase().format(formatter),
                        tema.getDiaSemana(),
                        tema.getRangoClases(),
                        tema.getCaracterClase() != null ? tema.getCaracterClase() : "N/A",
                        tema.getTema().length() > 40 ? 
                            tema.getTema().substring(0, 40) + "..." : tema.getTema(),
                        actResumidas.isEmpty() ? "-" : actResumidas,
                        obsResumida.isEmpty() ? "-" : obsResumida,
                        tema.getEstadoVisual()
                    };
                    
                    modeloTabla.addRow(fila);
                }
            } else {
                temasActuales = new ArrayList<>();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al cargar tabla de temas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Configura los eventos de los componentes.
     */
    private void configurarEventos() {
        // Evento del selector de materia
        cmbMateriasCursos.addActionListener(e -> {
            ComboItem item = (ComboItem) cmbMateriasCursos.getSelectedItem();
            if (item != null && item.getData() != null) {
                materiaActual = (Map<String, Object>) item.getData();
                actualizarInfoClases();
                actualizarTablaTemas();
            } else {
                materiaActual = null;
                actualizarInfoClases();
                modeloTabla.setRowCount(0);
            }
        });
        
        // Evento del modo múltiple
        rbModoMultiple.addActionListener(e -> {
            spnBloqueNumero.setEnabled(rbModoMultiple.isSelected());
        });
        
        rbModoSimple.addActionListener(e -> {
            spnBloqueNumero.setEnabled(false);
        });
        
        // Evento del botón guardar
        btnGuardar.addActionListener(this::guardarTema);
        
        // Evento del botón editar
        btnEditar.addActionListener(e -> editarTemaSeleccionado());
        
        // Evento del botón cancelar
        btnCancelar.addActionListener(e -> cancelarEdicion());
        
        // Evento del botón actualizar
        btnActualizar.addActionListener(e -> {
            actualizarInfoClases();
            actualizarTablaTemas();
        });
        
        // Evento del botón volver
        btnVolver.addActionListener(e -> volverALibroTemas());
    }
    
    /**
     * Guarda un nuevo tema diario o actualiza uno existente.
     */
    private void guardarTema(ActionEvent e) {
        try {
            // Validaciones comunes
            if (materiaActual == null) {
                JOptionPane.showMessageDialog(this, 
                    "Debe seleccionar una materia y curso.", 
                    "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (txtTema.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Debe ingresar el contenido del tema.", 
                    "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (txtActividadesDesarrolladas.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Debe ingresar las actividades desarrolladas.", 
                    "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (fechaSelector.getDate() == null) {
                JOptionPane.showMessageDialog(this, 
                    "Debe seleccionar una fecha para la clase.", 
                    "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Obtener fecha seleccionada
            LocalDate fechaSeleccionada = fechaSelector.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            // Verificar si estamos en modo edición
            if (temaEnEdicion != null) {
                actualizarTemaExistente(fechaSeleccionada);
                return;
            }
            
            // Modo creación - código existente
            int cursoId = (Integer) materiaActual.get("cursoId");
            int materiaId = (Integer) materiaActual.get("materiaId");
            
            if (temaDiarioDAO.existeTemaPorFecha(profesorId, cursoId, materiaId, fechaSeleccionada)) {
                int respuesta = JOptionPane.showConfirmDialog(this, 
                    "Ya existe un tema cargado para esta fecha en esta materia.\\n¿Desea continuar de todas formas?", 
                    "Confirmación", 
                    JOptionPane.YES_NO_OPTION);
                
                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Calcular bloques y rango de clases para la materia seleccionada en la fecha elegida
            int bloquesEnFecha = temaDiarioDAO.calcularBloquesDelDia(profesorId, materiaId, cursoId, fechaSeleccionada);
            if (bloquesEnFecha == 0) {
                // Para fechas pasadas, permitir ingresar manualmente sin verificar horarios
                LocalDate hoy = LocalDate.now();
                if (fechaSeleccionada.isBefore(hoy)) {
                    int respuesta = JOptionPane.showConfirmDialog(this,
                        "No se encontraron horarios programados para esta fecha, pero puede cargar el tema de todas formas.\\n" +
                        "Como es una fecha pasada, se asignará numeración automática.\\n" +
                        "¿Desea continuar?",
                        "Fecha pasada - Confirmar carga",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (respuesta != JOptionPane.YES_OPTION) {
                        return;
                    }
                    bloquesEnFecha = 1; // Asignar al menos 1 bloque para fechas pasadas
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No tiene clases programadas para esta fecha.\\n" +
                        "Para cargar temas de fechas pasadas, seleccione una fecha anterior a hoy.", 
                        "Sin clases programadas", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            
            int[] rangoClases = temaDiarioDAO.obtenerProximoNumeroClase(profesorId, cursoId, materiaId, bloquesEnFecha);
            
            // Crear objeto TemaDiario
            TemaDiario nuevoTema = new TemaDiario();
            nuevoTema.setProfesorId(profesorId);
            nuevoTema.setCursoId(cursoId);
            nuevoTema.setMateriaId(materiaId);
            nuevoTema.setFechaClase(fechaSeleccionada);
            nuevoTema.setClaseDesde(rangoClases[0]);
            nuevoTema.setClaseHasta(rangoClases[1]);
            nuevoTema.setTema(txtTema.getText().trim());
            nuevoTema.setActividadesDesarrolladas(txtActividadesDesarrolladas.getText().trim());
            nuevoTema.setObservaciones(txtObservaciones.getText().trim());
            nuevoTema.setCaracterClase((String) cmbCaracterClase.getSelectedItem());
            
            if (rbModoMultiple.isSelected()) {
                nuevoTema.setModoCarga(TemaDiario.ModoCarga.MULTIPLE);
                nuevoTema.setBloqueNumero((Integer) spnBloqueNumero.getValue());
            } else {
                nuevoTema.setModoCarga(TemaDiario.ModoCarga.SIMPLE);
            }
            
            // Guardar en base de datos
            boolean guardado = temaDiarioDAO.guardarTemaDiario(nuevoTema);
            
            if (guardado) {
                JOptionPane.showMessageDialog(this, 
                    "Tema guardado exitosamente.\\nClases registradas: " + nuevoTema.getDescripcionClases(), 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Limpiar formulario
                limpiarFormulario();
                
                // Actualizar vista
                actualizarInfoClases();
                actualizarTablaTemas();
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error al guardar el tema. Inténtelo nuevamente.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error inesperado: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Actualiza un tema existente.
     */
    private void actualizarTemaExistente(LocalDate fechaSeleccionada) {
        try {
            // Actualizar campos del tema en edición
            temaEnEdicion.setFechaClase(fechaSeleccionada);
            temaEnEdicion.setTema(txtTema.getText().trim());
            temaEnEdicion.setActividadesDesarrolladas(txtActividadesDesarrolladas.getText().trim());
            temaEnEdicion.setObservaciones(txtObservaciones.getText().trim());
            temaEnEdicion.setCaracterClase((String) cmbCaracterClase.getSelectedItem());
            
            // Actualizar en base de datos
            boolean actualizado = temaDiarioDAO.actualizarTemaDiario(temaEnEdicion);
            
            if (actualizado) {
                JOptionPane.showMessageDialog(this, 
                    "Tema actualizado exitosamente.", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Cancelar modo edición
                cancelarEdicion();
                
                // Actualizar vista
                actualizarInfoClases();
                actualizarTablaTemas();
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error al actualizar el tema. Inténtelo nuevamente.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error inesperado: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Limpia el formulario.
     */
    private void limpiarFormulario() {
        txtTema.setText("");
        txtActividadesDesarrolladas.setText("");
        txtObservaciones.setText("");
        fechaSelector.setDate(java.sql.Date.valueOf(LocalDate.now()));
        cmbCaracterClase.setSelectedIndex(1); // "Explicativa" por defecto
    }
    
    /**
     * Edita el tema seleccionado en la tabla.
     */
    private void editarTemaSeleccionado() {
        int filaSeleccionada = tablaTemas.getSelectedRow();
        if (filaSeleccionada < 0 || temasActuales == null || filaSeleccionada >= temasActuales.size()) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un tema válido para editar.", 
                "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TemaDiario temaSeleccionado = temasActuales.get(filaSeleccionada);
        
        // Verificar que no esté validado
        if (temaSeleccionado.isValidado()) {
            JOptionPane.showMessageDialog(this, 
                "No se puede editar un tema que ya fue validado.", 
                "Tema validado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Cargar datos en el formulario
        cargarTemaParaEdicion(temaSeleccionado);
    }
    
    /**
     * Carga un tema en el formulario para edición.
     */
    private void cargarTemaParaEdicion(TemaDiario tema) {
        temaEnEdicion = tema;
        
        // Cargar datos en los campos
        fechaSelector.setDate(java.sql.Date.valueOf(tema.getFechaClase()));
        txtTema.setText(tema.getTema());
        txtActividadesDesarrolladas.setText(tema.getActividadesDesarrolladas() != null ? tema.getActividadesDesarrolladas() : "");
        txtObservaciones.setText(tema.getObservaciones() != null ? tema.getObservaciones() : "");
        
        // Seleccionar carácter de clase
        if (tema.getCaracterClase() != null) {
            cmbCaracterClase.setSelectedItem(tema.getCaracterClase());
        }
        
        // Cambiar interfaz a modo edición
        btnGuardar.setText("ACTUALIZAR TEMA");
        btnGuardar.setBackground(new Color(255, 140, 0)); // Naranja para indicar edición
        btnEditar.setVisible(false); // Ocultar botón editar
        btnCancelar.setVisible(true); // Mostrar botón cancelar
        
        JOptionPane.showMessageDialog(this, 
            "Tema cargado para edición. Modifique los campos y presione 'ACTUALIZAR TEMA'.", 
            "Modo Edición", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Cancela la edición y vuelve al modo de creación.
     */
    private void cancelarEdicion() {
        temaEnEdicion = null;
        
        // Limpiar formulario
        limpiarFormulario();
        
        // Restaurar interfaz
        btnGuardar.setText("GUARDAR TEMA");
        btnGuardar.setBackground(new Color(51, 153, 255));
        btnEditar.setVisible(true); // Mostrar botón editar
        btnCancelar.setVisible(false); // Ocultar botón cancelar
        
        // Limpiar selección de tabla
        tablaTemas.clearSelection();
        btnEditar.setEnabled(false); // Deshabilitar hasta nueva selección
    }
    
    /**
     * Vuelve al panel de libro de temas.
     */
    private void volverALibroTemas() {
        try {
            if (ventanaPrincipal != null) {
                // Crear nueva instancia del libro de temas
                libroTema libro = new libroTema(
                    ventanaPrincipal.getPanelPrincipal(), 
                    profesorId, 
                    ventanaPrincipal
                );
                
                // Obtener el panel padre
                Container parent = this.getParent();
                
                // Ocultar el panel actual
                this.setVisible(false);
                
                if (parent != null) {
                    // Eliminar el componente actual
                    parent.remove(this);
                    
                    // Agregar el nuevo panel
                    parent.add(libro, BorderLayout.CENTER);
                    
                    // Actualizar la interfaz
                    parent.revalidate();
                    parent.repaint();
                }
                
                // Hacer visible el nuevo panel
                libro.setVisible(true);
                
            } else {
                System.err.println("Error: ventanaPrincipal es null");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al volver: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Configura el acceso según el rol del usuario.
     * Directivo (rol 1): Ver todas las cargas, validar, editar
     * Preceptor (rol 2): Ver (solo lectura)
     * Profesor (rol 3): Funcionalidad completa para sus materias
     */
    private void configurarAccesoSegunRol() {
        if (usuarioActual == null) {
            // Si no se puede obtener el usuario, asumir rol de profesor
            return;
        }
        
        int rol = usuarioActual.getRol();
        
        switch (rol) {
            case 1: // Directivo
                configurarAccesoDirectivo();
                break;
            case 2: // Preceptor
                configurarAccesoPreceptor();
                break;
            case 3: // Profesor
                configurarAccesoProfesor();
                break;
            default:
                // Rol desconocido, acceso limitado
                configurarAccesoLimitado();
                break;
        }
    }
    
    /**
     * Configura acceso para Directivo (rol 1).
     * Puede ver todas las cargas, validar y editar.
     */
    private void configurarAccesoDirectivo() {
        // Los directivos tienen acceso completo
        // Agregar funcionalidad de validación
        agregarFuncionalidadValidacion();
    }
    
    /**
     * Configura acceso para Preceptor (rol 2).
     * Solo lectura, no puede editar ni crear.
     */  
    private void configurarAccesoPreceptor() {
        // Deshabilitar botones de edición
        btnGuardar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        // Cambiar textos para indicar solo lectura
        btnGuardar.setText("SOLO LECTURA");
        btnGuardar.setBackground(new Color(153, 153, 153));
        
        // Deshabilitar campos de entrada
        txtTema.setEditable(false);
        txtObservaciones.setEditable(false);
        fechaSelector.setEnabled(false);
        cmbCaracterClase.setEnabled(false);
        rbModoSimple.setEnabled(false);
        rbModoMultiple.setEnabled(false);
        spnBloqueNumero.setEnabled(false);
        
        JOptionPane.showMessageDialog(this, 
            "Acceso de solo lectura para Preceptores.\nPuede ver los temas pero no modificarlos.", 
            "Modo Solo Lectura", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Configura acceso para Profesor (rol 3).
     * Acceso completo a sus propias materias.
     */
    private void configurarAccesoProfesor() {
        // Los profesores tienen acceso completo por defecto
        // No necesita cambios adicionales
    }
    
    /**
     * Configura acceso limitado para roles no reconocidos.
     */
    private void configurarAccesoLimitado() {
        // Acceso muy limitado
        btnGuardar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        btnGuardar.setText("ACCESO LIMITADO");
        btnGuardar.setBackground(new Color(220, 53, 69));
        
        JOptionPane.showMessageDialog(this, 
            "Acceso limitado. Contacte al administrador del sistema.", 
            "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Agrega funcionalidad de validación para directivos.
     */
    private void agregarFuncionalidadValidacion() {
        // Agregar botón de validación
        JButton btnValidar = new JButton("VALIDAR TEMA");
        btnValidar.setBackground(new Color(40, 167, 69)); // Verde
        btnValidar.setForeground(Color.WHITE);
        btnValidar.setFont(new Font("Arial", Font.BOLD, 11));
        btnValidar.setPreferredSize(new Dimension(140, 40));
        btnValidar.setEnabled(false); // Habilitado solo con selección
        
        // Agregar listener para validar
        btnValidar.addActionListener(e -> validarTemaSeleccionado());
        
        // Agregar el botón al panel de botones
        Component parent = btnGuardar.getParent();
        if (parent instanceof JPanel) {
            JPanel panelBotones = (JPanel) parent;
            panelBotones.add(btnValidar, panelBotones.getComponentCount() - 1); // Antes del botón volver
            panelBotones.revalidate();
        }
        
        // Agregar listener adicional para el botón de validar
        tablaTemas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaTemas.getSelectedRow();
                if (filaSeleccionada >= 0 && temasActuales != null && filaSeleccionada < temasActuales.size()) {
                    TemaDiario temaSeleccionado = temasActuales.get(filaSeleccionada);
                    // Solo permitir validar si no está validado
                    btnValidar.setEnabled(!temaSeleccionado.isValidado());
                } else {
                    btnValidar.setEnabled(false);
                }
            }
        });
    }
    
    /**
     * Valida el tema seleccionado (solo para directivos).
     */
    private void validarTemaSeleccionado() {
        int filaSeleccionada = tablaTemas.getSelectedRow();
        if (filaSeleccionada < 0 || temasActuales == null || filaSeleccionada >= temasActuales.size()) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un tema válido para validar.", 
                "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TemaDiario temaSeleccionado = temasActuales.get(filaSeleccionada);
        
        if (temaSeleccionado.isValidado()) {
            JOptionPane.showMessageDialog(this, 
                "Este tema ya está validado.", 
                "Tema ya validado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea validar este tema?\n" +
            "Una vez validado, no podrá ser editado por el profesor.",
            "Confirmar Validación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            boolean validado = temaDiarioDAO.validarTemaDiario(temaSeleccionado.getId(), usuarioActual.getId());
            
            if (validado) {
                JOptionPane.showMessageDialog(this, 
                    "Tema validado exitosamente.", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Actualizar vista
                actualizarTablaTemas();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error al validar el tema. Inténtelo nuevamente.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clase auxiliar para items del combo.
     */
    private static class ComboItem {
        private String texto;
        private Object data;
        
        public ComboItem(String texto, Object data) {
            this.texto = texto;
            this.data = data;
        }
        
        public Object getData() {
            return data;
        }
        
        @Override
        public String toString() {
            return texto;
        }
    }
}
