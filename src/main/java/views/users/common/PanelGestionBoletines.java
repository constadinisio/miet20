package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import main.java.database.Conexion;
import main.java.utils.GestorBoletines;

/**
 * Panel de Gestión de Boletines MEJORADO
 *
 * Permite a preceptores y administradores: - Ver todos los boletines generados
 * - Filtrar por año lectivo, curso, división y período - Abrir boletines
 * existentes - Eliminar boletines (solo admin/preceptor) - Ver estadísticas -
 * Generar nuevos boletines - Gestionar estructura de carpetas
 *
 * @author Sistema de Gestión Escolar
 * @version 2.0
 */
public class PanelGestionBoletines extends JPanel {

    private final VentanaInicio ventana;
    private final int userId;
    private final int userRol;
    private Connection conect;

    // Componentes de filtros
    private JComboBox<String> comboAnioLectivo;
    private JComboBox<String> comboCurso;
    private JComboBox<String> comboDivision;
    private JComboBox<String> comboPeriodo;
    private JTextField txtBuscarAlumno;

    // Componentes de acción
    private JButton btnBuscar;
    private JButton btnAbrir;
    private JButton btnEliminar;
    private JButton btnEstadisticas;
    private JButton btnActualizar;
    private JButton btnVolver;
    private JButton btnGenerarNuevo;
    private JButton btnConfigServidor;
    private JButton btnEstructura;
    private JButton btnLimpiarAntiguos;

    // Tabla de resultados
    private JTable tablaBoletines;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;

    // Datos
    private List<GestorBoletines.InfoBoletin> boletinesActuales;

    public PanelGestionBoletines(VentanaInicio ventana, int userId, int userRol) {
        this.ventana = ventana;
        this.userId = userId;
        this.userRol = userRol;
        this.boletinesActuales = new ArrayList<>();

        initializeConnection();
        initializeComponents();
        setupLayout();
        setupListeners();
        loadInitialData();

        System.out.println("PanelGestionBoletines MEJORADO inicializado para usuario: " + userId + ", rol: " + userRol);
    }

    /**
     * Inicializa la conexión a la base de datos.
     */
    private void initializeConnection() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this,
                    "Error de conexión a la base de datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void initializeComponents() {
        // Combos de filtros
        comboAnioLectivo = new JComboBox<>();
        comboCurso = new JComboBox<>();
        comboDivision = new JComboBox<>();
        comboPeriodo = new JComboBox<>();
        txtBuscarAlumno = new JTextField(20);
        txtBuscarAlumno.setToolTipText("Buscar por nombre o apellido del alumno");

        // Botones de acción
        btnBuscar = createStyledButton("Buscar", new Color(33, 150, 243));
        btnAbrir = createStyledButton("Abrir Boletín", new Color(76, 175, 80));
        btnEliminar = createStyledButton("Eliminar", new Color(244, 67, 54));
        btnEstadisticas = createStyledButton("Estadísticas", new Color(156, 39, 176));
        btnActualizar = createStyledButton("Actualizar", new Color(255, 152, 0));
        btnVolver = createStyledButton("Volver", new Color(96, 125, 139));
        btnGenerarNuevo = createStyledButton("Generar Nuevo", new Color(0, 150, 136));
        btnConfigServidor = createStyledButton("Config. Servidor", new Color(121, 85, 72));
        btnEstructura = createStyledButton("Crear Estructura", new Color(63, 81, 181));
        btnLimpiarAntiguos = createStyledButton("Limpiar Antiguos", new Color(255, 87, 34));

        // Tabla de boletines
        String[] columnas = {
            "Alumno", "DNI", "Curso", "Período", "Fecha Generación",
            "Estado", "Tamaño", "Archivo Existe", "Ruta"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };

        tablaBoletines = new JTable(modeloTabla);
        tablaBoletines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaBoletines.setRowHeight(30);
        tablaBoletines.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar anchos de columnas
        configurarAnchoColumnas();

        scrollTabla = new JScrollPane(tablaBoletines);
        scrollTabla.setPreferredSize(new Dimension(1200, 400));
    }

    /**
     * Crea un botón con estilo personalizado.
     */
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 32));
        return button;
    }

    /**
     * Configura el ancho de las columnas de la tabla.
     */
    private void configurarAnchoColumnas() {
        int[] anchos = {200, 100, 60, 80, 140, 80, 70, 80, 250};

        for (int i = 0; i < anchos.length && i < tablaBoletines.getColumnCount(); i++) {
            TableColumn columna = tablaBoletines.getColumnModel().getColumn(i);
            columna.setPreferredWidth(anchos[i]);
        }
    }

    /**
     * Configura el layout del panel.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior - Título
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblTitulo = new JLabel("Gestión de Boletines - Servidor");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(33, 150, 243));
        panelTitulo.add(lblTitulo);

        // Panel de filtros
        JPanel panelFiltros = createFiltrosPanel();

        // Panel de botones
        JPanel panelBotones = createBotonesPanel();

        // Panel central - Tabla
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(scrollTabla, BorderLayout.CENTER);

        // Panel de información
        JPanel panelInfo = createInfoPanel();

        // Ensamblar layout principal
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelFiltros, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInfo, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de filtros.
     */
    private JPanel createFiltrosPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Filtros de Búsqueda"));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Primera fila
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Año Lectivo:"), gbc);
        gbc.gridx = 1;
        panel.add(comboAnioLectivo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Curso:"), gbc);
        gbc.gridx = 3;
        panel.add(comboCurso, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("División:"), gbc);
        gbc.gridx = 5;
        panel.add(comboDivision, gbc);

        // Segunda fila
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Período:"), gbc);
        gbc.gridx = 1;
        panel.add(comboPeriodo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Buscar Alumno:"), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 3;
        panel.add(txtBuscarAlumno, gbc);

        return panel;
    }

    /**
     * Crea el panel de botones.
     */
    private JPanel createBotonesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));

        panel.add(btnBuscar);
        panel.add(btnAbrir);

        // Solo administradores y preceptores pueden eliminar
        if (userRol == 1 || userRol == 2) {
            panel.add(btnEliminar);
        }

        panel.add(btnEstadisticas);
        panel.add(btnActualizar);
        panel.add(btnGenerarNuevo);

        // Solo administradores pueden configurar servidor y estructura
        if (userRol == 1) {
            panel.add(btnConfigServidor);
            panel.add(btnEstructura);
            panel.add(btnLimpiarAntiguos);
        }

        panel.add(btnVolver);

        return panel;
    }

    /**
     * Crea el panel de información inferior.
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        JLabel lblInfo = new JLabel("Doble clic en una fila para abrir el boletín | Servidor: " + GestorBoletines.obtenerRutaServidor());
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);

        panel.add(lblInfo);

        return panel;
    }

    /**
     * Configura los listeners de los componentes.
     */
    private void setupListeners() {
        // Listeners de botones
        btnBuscar.addActionListener(this::buscarBoletines);
        btnAbrir.addActionListener(this::abrirBoletin);
        btnEliminar.addActionListener(this::eliminarBoletin);
        btnEstadisticas.addActionListener(this::mostrarEstadisticas);
        btnActualizar.addActionListener(this::actualizarLista);
        btnGenerarNuevo.addActionListener(this::generarNuevoBoletin);
        btnConfigServidor.addActionListener(this::configurarServidor);
        btnEstructura.addActionListener(this::crearEstructura);
        btnLimpiarAntiguos.addActionListener(this::limpiarBoletinesAntiguos);
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());

        // Listener para cambios en combos
        comboAnioLectivo.addActionListener(e -> actualizarCombosSecundarios());
        comboCurso.addActionListener(e -> actualizarComboDivision());

        // Doble clic en tabla para abrir boletín
        tablaBoletines.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirBoletin(null);
                }
            }
        });

        // Enter en campo de búsqueda
        txtBuscarAlumno.addActionListener(this::buscarBoletines);
    }

    /**
     * Carga los datos iniciales.
     */
    private void loadInitialData() {
        cargarAniosLectivos();
        cargarCursos();
        cargarPeriodos();
        buscarBoletines(null); // Carga inicial
    }

    /**
     * Carga los años lectivos disponibles.
     */
    private void cargarAniosLectivos() {
        try {
            comboAnioLectivo.removeAllItems();

            // Agregar años desde el actual hacia atrás
            int anioActual = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                comboAnioLectivo.addItem(String.valueOf(anioActual - i));
            }

            // Seleccionar año actual por defecto
            comboAnioLectivo.setSelectedItem(String.valueOf(anioActual));

        } catch (Exception e) {
            System.err.println("Error cargando años lectivos: " + e.getMessage());
        }
    }

    /**
     * Carga los cursos disponibles.
     */
    private void cargarCursos() {
        try {
            comboCurso.removeAllItems();
            comboCurso.addItem("TODOS");

            String query = "SELECT DISTINCT anio FROM cursos WHERE estado = 'activo' ORDER BY anio";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                comboCurso.addItem(String.valueOf(rs.getInt("anio")));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("Error cargando cursos: " + e.getMessage());
        }
    }

    /**
     * Abre el boletín seleccionado.
     */
    private void abrirBoletin(ActionEvent e) {
        int filaSeleccionada = tablaBoletines.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un boletín de la tabla",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        GestorBoletines.InfoBoletin boletin = boletinesActuales.get(filaSeleccionada);

        try {
            File archivo = new File(boletin.rutaArchivo);
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this,
                        "El archivo del boletín no existe en:\n" + boletin.rutaArchivo,
                        "Archivo no encontrado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Abrir archivo con aplicación predeterminada
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(archivo);
                    System.out.println("✅ Boletín abierto: " + archivo.getName());
                } else {
                    mostrarRutaArchivo(boletin.rutaArchivo);
                }
            } else {
                mostrarRutaArchivo(boletin.rutaArchivo);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el boletín:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra la ruta del archivo cuando no se puede abrir automáticamente.
     */
    private void mostrarRutaArchivo(String ruta) {
        JOptionPane.showMessageDialog(this,
                "No se pudo abrir automáticamente.\nRuta del archivo:\n" + ruta,
                "Información del Archivo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Elimina el boletín seleccionado (solo admin/preceptor).
     */
    private void eliminarBoletin(ActionEvent e) {
        if (userRol != 1 && userRol != 2) {
            JOptionPane.showMessageDialog(this,
                    "No tiene permisos para eliminar boletines",
                    "Permisos insuficientes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaSeleccionada = tablaBoletines.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un boletín para eliminar",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        GestorBoletines.InfoBoletin boletin = boletinesActuales.get(filaSeleccionada);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el boletín de:\n" + boletin.alumnoNombre
                + "\nPeríodo: " + boletin.periodo + "\n\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return GestorBoletines.eliminarBoletin(boletin);
                }

                @Override
                protected void done() {
                    try {
                        boolean eliminado = get();
                        if (eliminado) {
                            JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                    "Boletín eliminado exitosamente",
                                    "Eliminación completada",
                                    JOptionPane.INFORMATION_MESSAGE);
                            buscarBoletines(null); // Actualizar lista
                        } else {
                            JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                    "Error al eliminar el boletín",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                "Error durante la eliminación: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Muestra estadísticas de boletines.
     */
    private void mostrarEstadisticas(ActionEvent e) {
        String anioLectivo = (String) comboAnioLectivo.getSelectedItem();
        if (anioLectivo == null) {
            return;
        }

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return GestorBoletines.obtenerEstadisticas(Integer.parseInt(anioLectivo));
            }

            @Override
            protected void done() {
                try {
                    String estadisticas = get();

                    javax.swing.JTextArea textArea = new javax.swing.JTextArea(estadisticas);
                    textArea.setEditable(false);
                    textArea.setRows(20);
                    textArea.setColumns(60);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

                    JScrollPane scrollPane = new JScrollPane(textArea);

                    JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                            scrollPane,
                            "Estadísticas de Boletines - " + anioLectivo,
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                            "Error obteniendo estadísticas: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Actualiza la lista de boletines.
     */
    private void actualizarLista(ActionEvent e) {
        buscarBoletines(null);
    }

    /**
     * Abre el panel para generar un nuevo boletín.
     */
    private void generarNuevoBoletin(ActionEvent e) {
        // Redirigir al panel de notas para generar boletines
        JOptionPane.showMessageDialog(this,
                "Para generar nuevos boletines, vaya al panel de 'Notas'\n"
                + "y utilice el botón 'Generar Boletines'.",
                "Generar Nuevos Boletines",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Configurar el servidor de boletines (solo admin).
     */
    private void configurarServidor(ActionEvent e) {
        if (userRol != 1) {
            JOptionPane.showMessageDialog(this,
                    "Solo los administradores pueden configurar el servidor",
                    "Permisos insuficientes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        mostrarDialogoConfiguracionServidor();
    }

    /**
     * Crear estructura de carpetas (solo admin).
     */
    private void crearEstructura(ActionEvent e) {
        if (userRol != 1) {
            JOptionPane.showMessageDialog(this,
                    "Solo los administradores pueden crear la estructura",
                    "Permisos insuficientes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String anioLectivo = (String) comboAnioLectivo.getSelectedItem();
        if (anioLectivo == null) {
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Desea crear la estructura de carpetas para el año " + anioLectivo + "?\n"
                + "Esto creará todas las carpetas necesarias para cursos y períodos.",
                "Crear Estructura",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return GestorBoletines.generarEstructuraCompleta(Integer.parseInt(anioLectivo));
                }

                @Override
                protected void done() {
                    try {
                        boolean exito = get();
                        String mensaje = exito
                                ? "Estructura de carpetas creada exitosamente"
                                : "Hubo problemas al crear la estructura";

                        JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                mensaje,
                                "Estructura de Carpetas",
                                exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                "Error: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Limpiar boletines antiguos (solo admin).
     */
    private void limpiarBoletinesAntiguos(ActionEvent e) {
        if (userRol != 1) {
            JOptionPane.showMessageDialog(this,
                    "Solo los administradores pueden limpiar boletines antiguos",
                    "Permisos insuficientes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] opciones = {"2 años", "3 años", "5 años", "Cancelar"};
        int seleccion = JOptionPane.showOptionDialog(this,
                "¿Qué antigüedad de boletines desea eliminar?",
                "Limpiar Boletines Antiguos",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (seleccion >= 0 && seleccion < 3) {
            int aniosAntiguedad = seleccion == 0 ? 2 : (seleccion == 1 ? 3 : 5);

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar boletines con más de " + aniosAntiguedad + " años?\n"
                    + "Esta acción no se puede deshacer.",
                    "Confirmar Limpieza",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                    @Override
                    protected Integer doInBackground() throws Exception {
                        return GestorBoletines.limpiarBoletinesAntiguos(aniosAntiguedad);
                    }

                    @Override
                    protected void done() {
                        try {
                            int eliminados = get();
                            JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                    "Limpieza completada.\nBoletines eliminados: " + eliminados,
                                    "Limpieza Finalizada",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // Actualizar lista
                            buscarBoletines(null);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                    "Error durante la limpieza: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

                worker.execute();
            }
        }
    }

    /**
     * Muestra el diálogo de configuración del servidor.
     */
    private void mostrarDialogoConfiguracionServidor() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Ruta actual
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Ruta actual del servidor:"), gbc);

        JTextField txtRutaActual = new JTextField(GestorBoletines.obtenerRutaServidor(), 40);
        txtRutaActual.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(txtRutaActual, gbc);

        // Nueva ruta
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Nueva ruta:"), gbc);

        JTextField txtNuevaRuta = new JTextField(40);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(txtNuevaRuta, gbc);

        JButton btnExaminar = new JButton("Examinar...");
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(btnExaminar, gbc);

        // Listener para examinar
        btnExaminar.addActionListener(evt -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Seleccionar carpeta del servidor de boletines");

            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                txtNuevaRuta.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Mostrar diálogo
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Configuración del Servidor de Boletines",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nuevaRuta = txtNuevaRuta.getText().trim();
            if (!nuevaRuta.isEmpty()) {
                GestorBoletines.configurarRutaServidor(nuevaRuta);

                // Preguntar si generar estructura
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Desea generar la estructura de carpetas para el año actual?",
                        "Generar Estructura",
                        JOptionPane.YES_NO_OPTION);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            return GestorBoletines.generarEstructuraCompleta(LocalDate.now().getYear());
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean exito = get();
                                String mensaje = exito
                                        ? "Configuración actualizada y estructura generada exitosamente"
                                        : "Configuración actualizada, pero hubo problemas generando la estructura";

                                JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                        mensaje,
                                        "Configuración Completada",
                                        exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                                // Actualizar panel de información
                                actualizarInfoPanel();

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                        "Error: " + ex.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    };

                    worker.execute();
                } else {
                    // Solo actualizar la información del panel
                    actualizarInfoPanel();
                }
            }
        }
    }

    /**
     * Actualiza los combos secundarios cuando cambia el año lectivo.
     */
    private void actualizarCombosSecundarios() {
        String anioSeleccionado = (String) comboAnioLectivo.getSelectedItem();
        if (anioSeleccionado != null) {
            cargarCursos();
            cargarPeriodos();
        }
    }

    /**
     * Actualiza el combo de división cuando cambia el curso.
     */
    private void actualizarComboDivision() {
        try {
            comboDivision.removeAllItems();
            comboDivision.addItem("TODAS");

            String cursoSeleccionado = (String) comboCurso.getSelectedItem();
            if (cursoSeleccionado != null && !cursoSeleccionado.equals("TODOS")) {
                String query = "SELECT DISTINCT division FROM cursos WHERE anio = ? AND estado = 'activo' ORDER BY division";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, Integer.parseInt(cursoSeleccionado));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    comboDivision.addItem(String.valueOf(rs.getInt("division")));
                }

                rs.close();
                ps.close();
            }

        } catch (SQLException e) {
            System.err.println("Error cargando divisiones: " + e.getMessage());
        }
    }

    /**
     * Carga los períodos disponibles.
     */
    private void cargarPeriodos() {
        try {
            comboPeriodo.removeAllItems();
            comboPeriodo.addItem("TODOS");

            for (String periodo : GestorBoletines.PERIODOS_VALIDOS) {
                comboPeriodo.addItem(periodo);
            }

        } catch (Exception e) {
            System.err.println("Error cargando períodos: " + e.getMessage());
        }
    }

    /**
     * Busca boletines según los filtros seleccionados.
     */
    private void buscarBoletines(ActionEvent e) {
        javax.swing.SwingWorker<List<GestorBoletines.InfoBoletin>, Void> worker
                = new javax.swing.SwingWorker<List<GestorBoletines.InfoBoletin>, Void>() {

            @Override
            protected List<GestorBoletines.InfoBoletin> doInBackground() throws Exception {
                String anioLectivo = (String) comboAnioLectivo.getSelectedItem();
                String curso = (String) comboCurso.getSelectedItem();
                String division = (String) comboDivision.getSelectedItem();
                String periodo = (String) comboPeriodo.getSelectedItem();

                if (anioLectivo == null) {
                    return new ArrayList<>();
                }

                return GestorBoletines.obtenerBoletinesDisponibles(
                        Integer.parseInt(anioLectivo),
                        curso,
                        division,
                        periodo
                );
            }

            @Override
            protected void done() {
                try {
                    boletinesActuales = get();
                    actualizarTablaBoletines();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                            "Error al buscar boletines: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Actualiza la tabla con los boletines encontrados.
     */
    private void actualizarTablaBoletines() {
        modeloTabla.setRowCount(0);

        String filtroAlumno = txtBuscarAlumno.getText().toLowerCase().trim();

        for (GestorBoletines.InfoBoletin boletin : boletinesActuales) {
            // Filtrar por nombre de alumno si se especificó
            if (!filtroAlumno.isEmpty()
                    && !boletin.alumnoNombre.toLowerCase().contains(filtroAlumno)) {
                continue;
            }

            Object[] fila = {
                boletin.alumnoNombre,
                boletin.alumnoDni,
                boletin.curso + boletin.division,
                boletin.periodo,
                boletin.fechaGeneracion != null
                ? boletin.fechaGeneracion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Sin fecha",
                boletin.estadoBoletin,
                formatearTamanio(boletin.tamanioArchivo),
                boletin.archivoExiste ? "✅ Sí" : "❌ No",
                boletin.rutaArchivo
            };

            modeloTabla.addRow(fila);
        }

        if (modeloTabla.getRowCount() == 0) {
            modeloTabla.addRow(new Object[]{
                "Sin resultados", "", "", "", "", "", "", "", ""
            });
        }

        // Actualizar información
        actualizarInfoPanel();
    }

    /**
     * Actualiza el panel de información inferior.
     */
    /**
     * Actualiza el panel de información inferior con estadísticas actuales.
     */
    private void actualizarInfoPanel() {
        try {
            // Calcular estadísticas de los boletines mostrados actualmente
            int totalBoletines = boletinesActuales.size();
            int boletinesExistentes = 0;
            int boletinesFaltantes = 0;
            long espacioTotal = 0;

            // Contar por período
            Map<String, Integer> boletinesPorPeriodo = new HashMap<>();

            // Contar por estado
            Map<String, Integer> boletinesPorEstado = new HashMap<>();

            for (GestorBoletines.InfoBoletin boletin : boletinesActuales) {
                // Contar archivos existentes vs faltantes
                if (boletin.archivoExiste) {
                    boletinesExistentes++;
                    espacioTotal += boletin.tamanioArchivo;
                } else {
                    boletinesFaltantes++;
                }

                // Contar por período
                boletinesPorPeriodo.put(boletin.periodo,
                        boletinesPorPeriodo.getOrDefault(boletin.periodo, 0) + 1);

                // Contar por estado
                boletinesPorEstado.put(boletin.estadoBoletin,
                        boletinesPorEstado.getOrDefault(boletin.estadoBoletin, 0) + 1);
            }

            // Construir texto informativo
            StringBuilder infoText = new StringBuilder();

            // Información básica
            infoText.append("📊 Total: ").append(totalBoletines);
            infoText.append(" | ✅ Existentes: ").append(boletinesExistentes);
            infoText.append(" | ❌ Faltantes: ").append(boletinesFaltantes);

            if (espacioTotal > 0) {
                infoText.append(" | 💾 Espacio: ").append(formatearTamanio(espacioTotal));
            }

            // Información por período (solo si hay variedad)
            if (boletinesPorPeriodo.size() > 1) {
                infoText.append(" | 📅 Períodos: ");
                boolean primero = true;
                for (Map.Entry<String, Integer> entry : boletinesPorPeriodo.entrySet()) {
                    if (!primero) {
                        infoText.append(", ");
                    }
                    infoText.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
                    primero = false;
                }
            }

            infoText.append(" | 📁 Servidor: ").append(GestorBoletines.obtenerRutaServidor());

            // Buscar el panel de información existente y actualizarlo
            Container parent = this.getParent();
            if (parent != null) {
                Component[] components = this.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        // Buscar el panel que tiene el borde etched (panel de información)
                        if (panel.getBorder() instanceof javax.swing.border.EtchedBorder) {
                            // Encontramos el panel de información
                            panel.removeAll();

                            // Crear label principal con la información
                            JLabel lblInfo = new JLabel(infoText.toString());
                            lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
                            lblInfo.setForeground(Color.DARK_GRAY);

                            // Agregar información adicional si es útil
                            String filtroAlumno = txtBuscarAlumno.getText().trim();
                            if (!filtroAlumno.isEmpty()) {
                                JLabel lblFiltro = new JLabel(" | 🔍 Filtro: \"" + filtroAlumno + "\"");
                                lblFiltro.setFont(new Font("Arial", Font.ITALIC, 10));
                                lblFiltro.setForeground(Color.BLUE);

                                JPanel panelCompleto = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                                panelCompleto.add(lblInfo);
                                panelCompleto.add(lblFiltro);
                                panel.add(panelCompleto);
                            } else {
                                panel.add(lblInfo);
                            }

                            // Agregar información de ayuda
                            if (totalBoletines > 0) {
                                JLabel lblAyuda = new JLabel(" | 💡 Doble clic para abrir boletín");
                                lblAyuda.setFont(new Font("Arial", Font.ITALIC, 10));
                                lblAyuda.setForeground(Color.GRAY);
                                panel.add(lblAyuda);
                            }

                            panel.revalidate();
                            panel.repaint();
                            break;
                        }
                    }
                }
            }

            // Actualizar título de la ventana si es necesario
            String titulo = "Gestión de Boletines";
            if (totalBoletines > 0) {
                titulo += " (" + totalBoletines + " boletines)";
            }

            // Buscar el título y actualizarlo
            Component[] allComponents = this.getComponents();
            for (Component comp : allComponents) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] panelComponents = panel.getComponents();
                    for (Component subComp : panelComponents) {
                        if (subComp instanceof JLabel) {
                            JLabel label = (JLabel) subComp;
                            if (label.getText().contains("Gestión de Boletines")) {
                                // Actualizar solo si cambió
                                if (!label.getText().equals(titulo)) {
                                    label.setText(titulo);
                                    label.revalidate();
                                }
                                break;
                            }
                        }
                    }
                }
            }

            System.out.println("Panel de información actualizado: " + infoText.toString());

        } catch (Exception e) {
            System.err.println("Error al actualizar panel de información: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, mostrar información básica
            try {
                // Buscar y actualizar con información mínima
                Component[] components = this.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        if (panel.getBorder() instanceof javax.swing.border.EtchedBorder) {
                            panel.removeAll();
                            JLabel lblError = new JLabel("Error al actualizar información | Servidor: "
                                    + GestorBoletines.obtenerRutaServidor());
                            lblError.setFont(new Font("Arial", Font.ITALIC, 11));
                            lblError.setForeground(Color.RED);
                            panel.add(lblError);
                            panel.revalidate();
                            panel.repaint();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error crítico al actualizar información: " + ex.getMessage());
            }
        }
    }

    /**
     * Método auxiliar para crear información detallada por períodos.
     */
    private String crearInformacionDetallada() {
        try {
            if (boletinesActuales.isEmpty()) {
                return "No hay boletines para mostrar con los filtros actuales";
            }

            // Agrupar información más detallada
            Map<String, Map<String, Integer>> estadisticasDetalladas = new HashMap<>();

            for (GestorBoletines.InfoBoletin boletin : boletinesActuales) {
                String curso = boletin.curso + boletin.division;

                estadisticasDetalladas.computeIfAbsent(curso, k -> new HashMap<>())
                        .put(boletin.periodo,
                                estadisticasDetalladas.get(curso).getOrDefault(boletin.periodo, 0) + 1);
            }

            StringBuilder detalle = new StringBuilder();
            detalle.append("Detalle por curso: ");

            boolean primero = true;
            for (Map.Entry<String, Map<String, Integer>> cursoEntry : estadisticasDetalladas.entrySet()) {
                if (!primero) {
                    detalle.append(" | ");
                }
                detalle.append(cursoEntry.getKey()).append(":");

                int totalCurso = cursoEntry.getValue().values().stream().mapToInt(Integer::intValue).sum();
                detalle.append(totalCurso);

                primero = false;
            }

            return detalle.toString();

        } catch (Exception e) {
            return "Error al generar información detallada";
        }
    }

    /**
     * Actualiza estadísticas en tiempo real cuando cambian los filtros.
     */
    private void actualizarEstadisticasRapidas() {
        if (boletinesActuales == null) {
            return;
        }

        // Esta es una versión más liviana para actualizaciones frecuentes
        int total = boletinesActuales.size();
        int existentes = (int) boletinesActuales.stream().filter(b -> b.archivoExiste).count();

        // Buscar un componente donde mostrar estadísticas rápidas
        // (esto podría ser un JLabel específico para estadísticas rápidas)
        System.out.println("Estadísticas rápidas: " + existentes + "/" + total + " boletines disponibles");
    }

    /**
     * Formatea el tamaño del archivo en formato legible.
     */
    private String formatearTamanio(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }

        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
