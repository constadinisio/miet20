package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;

/**
 * Panel para visualización de notas y reportes. Permite ver notas por materia,
 * por alumno y reportes bimestrales.
 */
public class NotasVisualizationPanel extends JPanel {

    private final VentanaInicio ventana;
    private final int userId;
    private final int userRol;
    private Connection conect;

    // Componentes de interfaz
    private JLabel lblTitulo;
    private JRadioButton rbPorMateria;
    private JRadioButton rbPorAlumno;
    private JRadioButton rbReporteBimestral;
    private ButtonGroup grupoVistas;

    private JPanel panelFiltros;
    private JComboBox<String> comboCursos;
    private JComboBox<String> comboMaterias;
    private JComboBox<String> comboAlumnos;
    private JComboBox<String> comboPeriodos;
    private JButton btnBuscar;
    private JButton btnExportar;
    private JButton btnVolver;
    private JButton btnBoletines;

    private JTable tablaNotas;
    private JScrollPane scrollTabla;

    // Mapas para almacenar IDs
    private Map<String, Integer> cursosMap = new HashMap<>();
    private Map<String, Integer> materiasMap = new HashMap<>();
    private Map<String, Integer> alumnosMap = new HashMap<>();

    public NotasVisualizationPanel(VentanaInicio ventana, int userId, int userRol) {
        this.ventana = ventana;
        this.userId = userId;
        this.userRol = userRol;

        initializeConnection();
        initializeComponents();
        setupLayout();
        setupListeners();
        loadInitialData();

        System.out.println("NotasVisualizationPanel inicializado para usuario: " + userId + ", rol: " + userRol);
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
        // Título
        lblTitulo = new JLabel("Visualización de Notas y Reportes", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(51, 153, 255));

        // Radio buttons para tipo de vista
        rbPorMateria = new JRadioButton("Ver por Materia", true);
        rbPorAlumno = new JRadioButton("Ver por Alumno");
        rbReporteBimestral = new JRadioButton("Reporte Bimestral");

        grupoVistas = new ButtonGroup();
        grupoVistas.add(rbPorMateria);
        grupoVistas.add(rbPorAlumno);
        grupoVistas.add(rbReporteBimestral);

        // Combos
        comboCursos = new JComboBox<>();
        comboMaterias = new JComboBox<>();
        comboAlumnos = new JComboBox<>();
        comboPeriodos = new JComboBox<>();

        // Botones
        btnBuscar = new JButton("Buscar");
        btnExportar = new JButton("Exportar");
        btnVolver = new JButton("Volver");
        btnBoletines = new JButton("Generar Boletines");
        styleButton(btnBoletines, new Color(156, 39, 176)); // Color púrpura distintivo

        styleButton(btnBuscar, new Color(46, 125, 50));
        styleButton(btnExportar, new Color(255, 193, 7));
        styleButton(btnVolver, new Color(63, 81, 181));

        // Tabla
        tablaNotas = new JTable();
        tablaNotas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaNotas.setRowHeight(25);
        scrollTabla = new JScrollPane(tablaNotas);
        scrollTabla.setPreferredSize(new Dimension(800, 400));
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

        // Panel central con controles y tabla
        JPanel panelCentral = new JPanel(new BorderLayout());

        // Panel de controles
        JPanel panelControles = createControlPanel();
        panelCentral.add(panelControles, BorderLayout.NORTH);

        // Tabla
        panelCentral.add(scrollTabla, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnBuscar);
        panelBotones.add(btnExportar);
        panelBotones.add(btnBoletines); // NUEVO BOTÓN
        panelBotones.add(btnVolver);
        add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de controles.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Opciones de Visualización"));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Primera fila - Radio buttons
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(rbPorMateria, gbc);
        gbc.gridx = 1;
        panel.add(rbPorAlumno, gbc);
        gbc.gridx = 2;
        panel.add(rbReporteBimestral, gbc);

        // Segunda fila - Filtros
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Curso:"), gbc);
        gbc.gridx = 1;
        panel.add(comboCursos, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(new JLabel("Materia:"), gbc);
        gbc.gridx = 3;
        panel.add(comboMaterias, gbc);

        // Tercera fila - Más filtros
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Alumno:"), gbc);
        gbc.gridx = 1;
        panel.add(comboAlumnos, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        panel.add(new JLabel("Período:"), gbc);
        gbc.gridx = 3;
        panel.add(comboPeriodos, gbc);

        return panel;
    }

    /**
     * Configura los listeners de los componentes.
     */
    private void setupListeners() {
        // Listeners para radio buttons
        rbPorMateria.addActionListener(e -> onViewModeChanged());
        rbPorAlumno.addActionListener(e -> onViewModeChanged());
        rbReporteBimestral.addActionListener(e -> onViewModeChanged());

        // Listeners para combos
        comboCursos.addActionListener(e -> onCursoChanged());

        // Listeners para botones
        btnBuscar.addActionListener(e -> buscarNotas());
        btnExportar.addActionListener(e -> exportarNotas());
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());

        // CORREGIDO: Usar el botón ya creado en initializeComponents()
        btnBoletines.addActionListener(e -> mostrarMenuBoletines());
    }

    /**
     * Carga los datos iniciales.
     */
    private void loadInitialData() {
        cargarCursos();
        cargarPeríodosReales();
        onViewModeChanged(); // Configurar visibilidad inicial
    }

    /**
     * Maneja el cambio de modo de visualización.
     */
    private void onViewModeChanged() {
        // Habilitar/deshabilitar combos según el modo
        boolean porMateria = rbPorMateria.isSelected();
        boolean porAlumno = rbPorAlumno.isSelected();
        boolean reporteBimestral = rbReporteBimestral.isSelected();

        System.out.println("Cambiando modo de vista: porMateria=" + porMateria
                + ", porAlumno=" + porAlumno + ", reporteBimestral=" + reporteBimestral);

        // Para reporte bimestral, mostrar todos los controles
        comboMaterias.setEnabled(porMateria || reporteBimestral);
        comboAlumnos.setEnabled(porAlumno || reporteBimestral);
        comboPeriodos.setEnabled(reporteBimestral);

        // Limpiar tabla de forma segura
        try {
            // Crear un modelo vacío básico
            DefaultTableModel modeloVacio = new DefaultTableModel();
            modeloVacio.addColumn("Seleccione una opción");
            modeloVacio.addRow(new Object[]{"Use los filtros para mostrar datos"});

            tablaNotas.setModel(modeloVacio);

            // Configurar para que no sea editable
            tablaNotas.setEnabled(false);

            System.out.println("Tabla limpiada correctamente");

        } catch (Exception e) {
            System.err.println("Error al limpiar tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el cambio de curso.
     */
    private void onCursoChanged() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        if (cursoSeleccionado != null) {
            Integer cursoId = cursosMap.get(cursoSeleccionado);
            if (cursoId != null) {
                cargarMaterias(cursoId);
                cargarAlumnos(cursoId);
            }
        }
    }

    /**
     * Carga los cursos disponibles según el rol del usuario.
     */
    private void cargarCursos() {
        try {
            cursosMap.clear();
            comboCursos.removeAllItems();

            String query = "";
            PreparedStatement ps = null;

            switch (userRol) {
                case 1: // Admin - ver todos los cursos
                    query = "SELECT id, CONCAT(anio, '°', division) as curso FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
                    ps = conect.prepareStatement(query);
                    break;

                case 2: // Preceptor - ver todos los cursos (los preceptores pueden ver cualquier curso)
                    query = "SELECT id, CONCAT(anio, '°', division) as curso FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
                    ps = conect.prepareStatement(query);
                    System.out.println("Cargando cursos para preceptor ID: " + userId);
                    break;

                case 3: // Profesor - cursos donde enseña
                    query = "SELECT DISTINCT c.id, CONCAT(c.anio, '°', c.division) as curso "
                            + "FROM cursos c INNER JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id "
                            + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                            + "ORDER BY c.anio, c.division";
                    ps = conect.prepareStatement(query);
                    ps.setInt(1, userId);
                    break;

                case 4: // Alumno - solo su curso
                    query = "SELECT c.id, CONCAT(c.anio, '°', c.division) as curso "
                            + "FROM cursos c INNER JOIN alumno_curso ac ON c.id = ac.curso_id "
                            + "WHERE ac.alumno_id = ? AND ac.estado = 'activo'";
                    ps = conect.prepareStatement(query);
                    ps.setInt(1, userId);
                    break;

                default:
                    System.err.println("Rol no reconocido para cargar cursos: " + userRol);
                    return;
            }

            ResultSet rs = ps.executeQuery();
            int cursosEncontrados = 0;

            while (rs.next()) {
                String curso = rs.getString("curso");
                int cursoId = rs.getInt("id");
                cursosMap.put(curso, cursoId);
                comboCursos.addItem(curso);
                cursosEncontrados++;

                System.out.println("Curso cargado para rol " + userRol + ": " + curso + " (ID: " + cursoId + ")");
            }

            System.out.println("Total de cursos cargados para rol " + userRol + ": " + cursosEncontrados);

            if (cursosEncontrados == 0) {
                System.out.println("ADVERTENCIA: No se encontraron cursos para el rol " + userRol + " y usuario " + userId);
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar cursos para rol " + userRol + ": " + e.getMessage());
            e.printStackTrace();

            // Mostrar información adicional para debugging
            try {
                if (conect != null && !conect.isClosed()) {
                    System.out.println("Conexión a BD está activa");
                } else {
                    System.err.println("Conexión a BD está cerrada o es null");
                }
            } catch (SQLException ex) {
                System.err.println("Error verificando conexión: " + ex.getMessage());
            }
        }
    }

    /**
     * Carga las materias de un curso específico.
     */
    private void cargarMaterias(int cursoId) {
        try {
            materiasMap.clear();
            comboMaterias.removeAllItems();

            String query = "";
            PreparedStatement ps = null;

            if (userRol == 3) { // Profesor - solo sus materias
                query = "SELECT DISTINCT m.id, m.nombre "
                        + "FROM materias m INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                        + "WHERE pcm.curso_id = ? AND pcm.profesor_id = ? AND pcm.estado = 'activo' "
                        + "ORDER BY m.nombre";
                ps = conect.prepareStatement(query);
                ps.setInt(1, cursoId);
                ps.setInt(2, userId);
            } else { // Admin, Preceptor, Alumno - todas las materias del curso
                query = "SELECT DISTINCT m.id, m.nombre "
                        + "FROM materias m INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                        + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' "
                        + "ORDER BY m.nombre";
                ps = conect.prepareStatement(query);
                ps.setInt(1, cursoId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String materia = rs.getString("nombre");
                int materiaId = rs.getInt("id");
                materiasMap.put(materia, materiaId);
                comboMaterias.addItem(materia);
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga los alumnos de un curso específico.
     */
    private void cargarAlumnos(int cursoId) {
        try {
            alumnosMap.clear();
            comboAlumnos.removeAllItems();

            if (userRol == 4) { // Alumno - solo él mismo
                String query = "SELECT u.id, CONCAT(u.apellido, ', ', u.nombre) as nombre_completo "
                        + "FROM usuarios u WHERE u.id = ?";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String nombreCompleto = rs.getString("nombre_completo");
                    alumnosMap.put(nombreCompleto, userId);
                    comboAlumnos.addItem(nombreCompleto);
                }
            } else { // Otros roles - todos los alumnos del curso
                String query = "SELECT u.id, CONCAT(u.apellido, ', ', u.nombre) as nombre_completo "
                        + "FROM usuarios u INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                        + "WHERE ac.curso_id = ? AND ac.estado = 'activo' "
                        + "ORDER BY u.apellido, u.nombre";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, cursoId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String nombreCompleto = rs.getString("nombre_completo");
                    int alumnoId = rs.getInt("id");
                    alumnosMap.put(nombreCompleto, alumnoId);
                    comboAlumnos.addItem(nombreCompleto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar alumnos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Busca y muestra las notas según los filtros seleccionados.
     */
    private void buscarNotas() {
        try {
            System.out.println("=== INICIANDO BÚSQUEDA DE NOTAS ===");

            // Habilitar la tabla
            tablaNotas.setEnabled(true);

            if (rbPorMateria.isSelected()) {
                System.out.println("Modo: Por Materia");
                mostrarNotasPorMateria();
            } else if (rbPorAlumno.isSelected()) {
                System.out.println("Modo: Por Alumno");
                mostrarNotasPorAlumno();
            } else if (rbReporteBimestral.isSelected()) {
                System.out.println("Modo: Reporte Bimestral");
                mostrarReporteBimestral();
            } else {
                System.err.println("No se seleccionó ningún modo de visualización");
                JOptionPane.showMessageDialog(this,
                        "Por favor, seleccione un tipo de visualización",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }

            System.out.println("=== BÚSQUEDA DE NOTAS COMPLETADA ===");

        } catch (Exception e) {
            System.err.println("Error en buscarNotas(): " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Error al buscar notas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // Crear tabla de error
            try {
                DefaultTableModel modeloError = new DefaultTableModel();
                modeloError.addColumn("Error");
                modeloError.addRow(new Object[]{"Error al cargar datos: " + e.getMessage()});
                tablaNotas.setModel(modeloError);
            } catch (Exception ex) {
                System.err.println("Error al crear tabla de error: " + ex.getMessage());
            }
        }
    }

    /**
     * Muestra las notas de una materia específica.
     */
    private void mostrarNotasPorMateria() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        String materiaSeleccionada = (String) comboMaterias.getSelectedItem();

        if (cursoSeleccionado == null || materiaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione curso y materia", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer cursoId = cursosMap.get(cursoSeleccionado);
        Integer materiaId = materiasMap.get(materiaSeleccionada);

        if (cursoId == null || materiaId == null) {
            JOptionPane.showMessageDialog(this, "Error: No se encontraron los IDs del curso o materia", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            System.out.println("Mostrando notas para curso ID: " + cursoId + ", materia ID: " + materiaId);

            // Crear modelo de tabla
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Alumno");
            modelo.addColumn("DNI");

            // Obtener trabajos de la materia para crear columnas dinámicas
            String queryTrabajos = "SELECT id, nombre FROM trabajos WHERE materia_id = ? ORDER BY id";
            PreparedStatement psTrabajos = conect.prepareStatement(queryTrabajos);
            psTrabajos.setInt(1, materiaId);
            ResultSet rsTrabajos = psTrabajos.executeQuery();

            java.util.List<Integer> trabajosIds = new java.util.ArrayList<>();
            java.util.List<String> trabajosNombres = new java.util.ArrayList<>();

            while (rsTrabajos.next()) {
                int trabajoId = rsTrabajos.getInt("id");
                String trabajoNombre = rsTrabajos.getString("nombre");
                trabajosIds.add(trabajoId);
                trabajosNombres.add(trabajoNombre);
                modelo.addColumn(trabajoNombre);
                System.out.println("Trabajo encontrado: ID=" + trabajoId + ", Nombre=" + trabajoNombre);
            }

            // Solo agregar columna de promedio si hay trabajos
            boolean hayTrabajos = !trabajosIds.isEmpty();
            if (hayTrabajos) {
                modelo.addColumn("Promedio");
                System.out.println("Se agregó columna de promedio. Total trabajos: " + trabajosIds.size());
            } else {
                System.out.println("No hay trabajos para esta materia");
            }

            // Obtener alumnos y sus notas
            String queryAlumnos = "SELECT u.id, CONCAT(u.apellido, ', ', u.nombre) as nombre_completo, u.dni "
                    + "FROM usuarios u INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = '4' "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            int contadorAlumnos = 0;
            while (rsAlumnos.next()) {
                // Crear fila con el tamaño correcto
                int tamanoFila = 2 + trabajosIds.size() + (hayTrabajos ? 1 : 0); // 2 fijas + trabajos + promedio (si hay trabajos)
                Object[] fila = new Object[tamanoFila];

                fila[0] = rsAlumnos.getString("nombre_completo");
                fila[1] = rsAlumnos.getString("dni");

                int alumnoId = rsAlumnos.getInt("id");
                double sumaNotas = 0;
                int cantidadNotas = 0;

                // Obtener notas para cada trabajo
                for (int i = 0; i < trabajosIds.size(); i++) {
                    String queryNota = "SELECT nota FROM notas WHERE alumno_id = ? AND trabajo_id = ?";
                    PreparedStatement psNota = conect.prepareStatement(queryNota);
                    psNota.setString(1, String.valueOf(alumnoId));
                    psNota.setInt(2, trabajosIds.get(i));
                    ResultSet rsNota = psNota.executeQuery();

                    if (rsNota.next()) {
                        double nota = rsNota.getDouble("nota");
                        fila[2 + i] = nota;
                        sumaNotas += nota;
                        cantidadNotas++;
                    } else {
                        fila[2 + i] = "-";
                    }
                    rsNota.close();
                    psNota.close();
                }

                // Calcular promedio solo si hay trabajos y notas
                if (hayTrabajos) {
                    if (cantidadNotas > 0) {
                        fila[fila.length - 1] = Math.round((sumaNotas / cantidadNotas) * 100.0) / 100.0;
                    } else {
                        fila[fila.length - 1] = "-";
                    }
                }

                modelo.addRow(fila);
                contadorAlumnos++;
            }

            rsAlumnos.close();
            psAlumnos.close();
            rsTrabajos.close();
            psTrabajos.close();

            System.out.println("Alumnos procesados: " + contadorAlumnos);
            System.out.println("Columnas en modelo: " + modelo.getColumnCount());
            System.out.println("Filas en modelo: " + modelo.getRowCount());

            // Verificar que el modelo tiene datos antes de asignarlo
            if (modelo.getColumnCount() >= 2) {
                tablaNotas.setModel(modelo);

                // Configuraciones adicionales de la tabla
                tablaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                tablaNotas.setRowHeight(25);

                // Ajustar anchos de columnas básicas
                if (tablaNotas.getColumnModel().getColumnCount() > 0) {
                    tablaNotas.getColumnModel().getColumn(0).setPreferredWidth(200); // Alumno
                }
                if (tablaNotas.getColumnModel().getColumnCount() > 1) {
                    tablaNotas.getColumnModel().getColumn(1).setPreferredWidth(100); // DNI
                }

                // Configurar scroll
                if (scrollTabla != null) {
                    scrollTabla.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollTabla.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                }

                System.out.println("Tabla configurada exitosamente");
            } else {
                System.err.println("Error: El modelo de tabla no tiene suficientes columnas");
                JOptionPane.showMessageDialog(this, "Error al crear la tabla de notas", "Error", JOptionPane.ERROR_MESSAGE);
            }

            // Mostrar mensaje si no hay datos
            if (contadorAlumnos == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron alumnos para el curso seleccionado",
                        "Sin datos",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (!hayTrabajos) {
                JOptionPane.showMessageDialog(this,
                        "No hay trabajos/actividades creados para esta materia.\n"
                        + "Los profesores deben crear trabajos primero para poder cargar notas.",
                        "Sin trabajos",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error al mostrar notas por materia: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar notas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.err.println("Error general al mostrar notas por materia: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error inesperado: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra las notas de un alumno específico.
     */
    private void mostrarNotasPorAlumno() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        String alumnoSeleccionado = (String) comboAlumnos.getSelectedItem();

        if (cursoSeleccionado == null || alumnoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione curso y alumno", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer cursoId = cursosMap.get(cursoSeleccionado);
        Integer alumnoId = alumnosMap.get(alumnoSeleccionado);

        if (cursoId == null || alumnoId == null) {
            JOptionPane.showMessageDialog(this, "Error: No se encontraron los IDs del curso o alumno", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            System.out.println("=== MOSTRAR NOTAS POR ALUMNO (CORREGIDO) ===");
            System.out.println("Mostrando notas para alumno ID: " + alumnoId + ", curso ID: " + cursoId);
            System.out.println("Alumno seleccionado: " + alumnoSeleccionado);

            // PASO 1: Obtener DNI del alumno para las consultas
            String queryAlumnoInfo = "SELECT dni, nombre, apellido FROM usuarios WHERE id = ?";
            PreparedStatement psAlumnoInfo = conect.prepareStatement(queryAlumnoInfo);
            psAlumnoInfo.setInt(1, alumnoId);
            ResultSet rsAlumnoInfo = psAlumnoInfo.executeQuery();

            String alumnoDni = null;
            String nombreCompleto = alumnoSeleccionado;

            if (rsAlumnoInfo.next()) {
                alumnoDni = rsAlumnoInfo.getString("dni");
                nombreCompleto = rsAlumnoInfo.getString("apellido") + ", " + rsAlumnoInfo.getString("nombre");
                System.out.println("Datos del alumno - ID: " + alumnoId + ", DNI: " + alumnoDni + ", Nombre: " + nombreCompleto);
            } else {
                System.err.println("⚠️ No se encontraron datos para el alumno ID: " + alumnoId);
            }

            rsAlumnoInfo.close();
            psAlumnoInfo.close();

            // PASO 2: Crear modelo de tabla
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Materia");
            modelo.addColumn("Trabajo/Actividad");
            modelo.addColumn("Nota");
            modelo.addColumn("Fecha");

            // PASO 3: Obtener todas las notas del alumno (CONSULTA CORREGIDA)
            String query = "SELECT m.nombre as materia, t.nombre as trabajo, n.nota, n.fecha_carga "
                    + "FROM notas n "
                    + "INNER JOIN trabajos t ON n.trabajo_id = t.id "
                    + "INNER JOIN materias m ON t.materia_id = m.id "
                    + "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' AND "
                    + "(n.alumno_id = ? OR n.alumno_id = CAST(? AS CHAR)) " // CORREGIDO: Buscar por DNI y por ID
                    + "ORDER BY m.nombre, t.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setString(2, alumnoDni != null ? alumnoDni : String.valueOf(alumnoId));  // Usar DNI si está disponible
            ps.setInt(3, alumnoId);                                                     // También probar con ID como int

            System.out.println("Ejecutando consulta de trabajos/actividades...");
            System.out.println("Parámetros: cursoId=" + cursoId + ", alumnoDni=" + alumnoDni + ", alumnoId=" + alumnoId);

            ResultSet rs = ps.executeQuery();

            int contadorNotas = 0;
            while (rs.next()) {
                Object[] fila = {
                    rs.getString("materia"),
                    rs.getString("trabajo"),
                    rs.getDouble("nota"),
                    rs.getTimestamp("fecha_carga") != null ? rs.getTimestamp("fecha_carga").toString() : "Sin fecha"
                };
                modelo.addRow(fila);
                contadorNotas++;
                System.out.println("  ✅ Nota encontrada: " + rs.getString("materia") + " - " + rs.getString("trabajo") + " = " + rs.getDouble("nota"));
            }

            rs.close();
            ps.close();

            System.out.println("Notas de trabajos/actividades encontradas: " + contadorNotas);

            // PASO 4: Obtener también las notas bimestrales (CONSULTA CORREGIDA)
            String queryBimestrales = "SELECT m.nombre as materia, nb.periodo, nb.nota, nb.fecha_carga "
                    + "FROM notas_bimestrales nb "
                    + "INNER JOIN materias m ON nb.materia_id = m.id "
                    + "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' AND "
                    + "(nb.alumno_id = ? OR nb.alumno_id = CAST(? AS CHAR)) " // CORREGIDO: Buscar por DNI y por ID
                    + "ORDER BY m.nombre, nb.periodo";

            PreparedStatement psBimestrales = conect.prepareStatement(queryBimestrales);
            psBimestrales.setInt(1, cursoId);
            psBimestrales.setString(2, alumnoDni != null ? alumnoDni : String.valueOf(alumnoId));  // Usar DNI si está disponible
            psBimestrales.setInt(3, alumnoId);                                                     // También probar con ID como int

            System.out.println("Ejecutando consulta de notas bimestrales...");
            ResultSet rsBimestrales = psBimestrales.executeQuery();

            int contadorBimestrales = 0;
            while (rsBimestrales.next()) {
                Object[] fila = {
                    rsBimestrales.getString("materia"),
                    "BIMESTRAL: " + rsBimestrales.getString("periodo"),
                    rsBimestrales.getDouble("nota"),
                    rsBimestrales.getTimestamp("fecha_carga") != null ? rsBimestrales.getTimestamp("fecha_carga").toString() : "Sin fecha"
                };
                modelo.addRow(fila);
                contadorBimestrales++;
                System.out.println("  ✅ Nota bimestral encontrada: " + rsBimestrales.getString("materia") + " - " + rsBimestrales.getString("periodo") + " = " + rsBimestrales.getDouble("nota"));
            }

            rsBimestrales.close();
            psBimestrales.close();

            System.out.println("Notas bimestrales encontradas: " + contadorBimestrales);
            System.out.println("Total de notas encontradas: " + (contadorNotas + contadorBimestrales));

            // PASO 5: Asignar modelo a la tabla
            tablaNotas.setModel(modelo);

            // Configurar tabla
            tablaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            tablaNotas.setRowHeight(25);

            // PASO 6: Mostrar resultado al usuario
            if (contadorNotas + contadorBimestrales == 0) {
                // Si no hay notas, ejecutar diagnóstico detallado
                System.out.println("❌ No se encontraron notas - Ejecutando diagnóstico...");
                diagnosticarAlumnoSinNotas(alumnoId, alumnoDni, cursoId, nombreCompleto);

                // Agregar fila informativa
                modelo.addRow(new Object[]{
                    "Sin notas",
                    "Este alumno no tiene notas registradas",
                    "-",
                    "-"
                });

                JOptionPane.showMessageDialog(this,
                        "El alumno seleccionado no tiene notas registradas.\n"
                        + "Verifique la consola para más información de diagnóstico.",
                        "Sin notas",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                String mensaje = String.format("Se encontraron %d notas para %s:\n"
                        + "- Trabajos/Actividades: %d\n"
                        + "- Notas Bimestrales: %d",
                        contadorNotas + contadorBimestrales, nombreCompleto, contadorNotas, contadorBimestrales);

                JOptionPane.showMessageDialog(this,
                        mensaje,
                        "Notas cargadas",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            System.out.println("=== FIN MOSTRAR NOTAS POR ALUMNO ===");

        } catch (SQLException e) {
            System.err.println("Error al mostrar notas por alumno: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar notas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);

            // Crear modelo de error
            DefaultTableModel modeloError = new DefaultTableModel();
            modeloError.addColumn("Error");
            modeloError.addRow(new Object[]{"Error al cargar notas del alumno: " + e.getMessage()});
            tablaNotas.setModel(modeloError);
        } catch (Exception e) {
            System.err.println("Error general al mostrar notas por alumno: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MÉTODO AUXILIAR: Diagnóstico detallado cuando no se encuentran notas
     * Agrega este método también a la clase NotasVisualizationPanel
     */
    private void diagnosticarAlumnoSinNotas(int alumnoId, String alumnoDni, int cursoId, String nombreCompleto) {
        try {
            System.out.println("=== DIAGNÓSTICO DETALLADO PARA ALUMNO SIN NOTAS ===");
            System.out.println("Alumno: " + nombreCompleto);
            System.out.println("ID: " + alumnoId + ", DNI: " + alumnoDni + ", Curso ID: " + cursoId);

            // 1. Verificar diferentes variaciones del alumno_id en la tabla notas
            String[] variaciones = {
                alumnoDni, // DNI del alumno
                String.valueOf(alumnoId), // ID como string
                String.format("%08d", alumnoId), // ID con ceros a la izquierda
                "0" + alumnoId // ID con un cero adelante
            };

            System.out.println("--- Verificando tabla 'notas' ---");
            for (int i = 0; i < variaciones.length; i++) {
                if (variaciones[i] == null) {
                    continue;
                }

                String queryTest = "SELECT COUNT(*) as total FROM notas WHERE alumno_id = ?";
                PreparedStatement psTest = conect.prepareStatement(queryTest);
                psTest.setString(1, variaciones[i]);
                ResultSet rsTest = psTest.executeQuery();

                if (rsTest.next()) {
                    int count = rsTest.getInt("total");
                    System.out.println("  Variación " + (i + 1) + " ('" + variaciones[i] + "'): " + count + " notas");

                    if (count > 0) {
                        // Si encontramos notas con esta variación, mostrar ejemplos
                        String queryEjemplos = "SELECT t.nombre, n.nota FROM notas n "
                                + "JOIN trabajos t ON n.trabajo_id = t.id WHERE n.alumno_id = ? LIMIT 3";
                        PreparedStatement psEjemplos = conect.prepareStatement(queryEjemplos);
                        psEjemplos.setString(1, variaciones[i]);
                        ResultSet rsEjemplos = psEjemplos.executeQuery();

                        System.out.println("    Ejemplos:");
                        while (rsEjemplos.next()) {
                            System.out.println("      - " + rsEjemplos.getString("nombre") + ": " + rsEjemplos.getDouble("nota"));
                        }
                        rsEjemplos.close();
                        psEjemplos.close();
                    }
                }
                rsTest.close();
                psTest.close();
            }

            System.out.println("--- Verificando tabla 'notas_bimestrales' ---");
            for (int i = 0; i < variaciones.length; i++) {
                if (variaciones[i] == null) {
                    continue;
                }

                String queryTest = "SELECT COUNT(*) as total FROM notas_bimestrales WHERE alumno_id = ?";
                PreparedStatement psTest = conect.prepareStatement(queryTest);
                psTest.setString(1, variaciones[i]);
                ResultSet rsTest = psTest.executeQuery();

                if (rsTest.next()) {
                    int count = rsTest.getInt("total");
                    System.out.println("  Variación " + (i + 1) + " ('" + variaciones[i] + "'): " + count + " notas bimestrales");

                    if (count > 0) {
                        // Si encontramos notas con esta variación, mostrar ejemplos
                        String queryEjemplos = "SELECT nb.periodo, nb.nota FROM notas_bimestrales nb "
                                + "WHERE nb.alumno_id = ? LIMIT 3";
                        PreparedStatement psEjemplos = conect.prepareStatement(queryEjemplos);
                        psEjemplos.setString(1, variaciones[i]);
                        ResultSet rsEjemplos = psEjemplos.executeQuery();

                        System.out.println("    Ejemplos:");
                        while (rsEjemplos.next()) {
                            System.out.println("      - " + rsEjemplos.getString("periodo") + ": " + rsEjemplos.getDouble("nota"));
                        }
                        rsEjemplos.close();
                        psEjemplos.close();
                    }
                }
                rsTest.close();
                psTest.close();
            }

            // 2. Verificar materias disponibles para el curso
            System.out.println("--- Materias del curso ---");
            String queryMaterias = "SELECT m.nombre FROM materias m "
                    + "JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo'";
            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            psMaterias.setInt(1, cursoId);
            ResultSet rsMaterias = psMaterias.executeQuery();

            while (rsMaterias.next()) {
                System.out.println("  - " + rsMaterias.getString("nombre"));
            }
            rsMaterias.close();
            psMaterias.close();

            // 3. Mostrar algunos ejemplos de alumno_id existentes en las tablas
            System.out.println("--- Ejemplos de alumno_id en base de datos ---");
            String queryEjemplosIds = "SELECT DISTINCT alumno_id FROM notas LIMIT 5";
            PreparedStatement psEjemplosIds = conect.prepareStatement(queryEjemplosIds);
            ResultSet rsEjemplosIds = psEjemplosIds.executeQuery();

            System.out.println("  Tabla notas:");
            while (rsEjemplosIds.next()) {
                System.out.println("    - '" + rsEjemplosIds.getString("alumno_id") + "'");
            }
            rsEjemplosIds.close();
            psEjemplosIds.close();

            System.out.println("=== FIN DIAGNÓSTICO ===");

        } catch (SQLException e) {
            System.err.println("Error en diagnóstico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra el reporte bimestral.
     */
    private void mostrarReporteBimestral() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        String periodoSeleccionado = (String) comboPeriodos.getSelectedItem();

        if (cursoSeleccionado == null || periodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione curso y período", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer cursoId = cursosMap.get(cursoSeleccionado);
        if (cursoId == null) {
            JOptionPane.showMessageDialog(this, "Error: No se encontró el ID del curso", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si hay filtros específicos
        String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
        String alumnoSeleccionado = (String) comboAlumnos.getSelectedItem();

        try {
            if (materiaSeleccionada != null && !materiaSeleccionada.isEmpty()) {
                // Reporte bimestral por materia específica
                mostrarReporteBimestralPorMateria(cursoId, periodoSeleccionado, materiaSeleccionada);
            } else if (alumnoSeleccionado != null && !alumnoSeleccionado.isEmpty()) {
                // Reporte bimestral por alumno específico
                mostrarReporteBimestralPorAlumno(cursoId, periodoSeleccionado, alumnoSeleccionado);
            } else {
                // Reporte bimestral general (todos los alumnos, todas las materias)
                mostrarReporteBimestralGeneral(cursoId, periodoSeleccionado);
            }
        } catch (Exception e) {
            System.err.println("Error en mostrarReporteBimestral: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar reporte bimestral: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método auxiliar para obtener el alumno_id correcto para las consultas.
     * Algunos datos pueden estar como INT y otros como STRING.
     */
    private String obtenerAlumnoIdParaConsulta(int alumnoId) {
        // Intentar primero como INT, si no funciona, como STRING
        try {
            // Verificar si existe como INT
            String queryTest = "SELECT COUNT(*) FROM notas_bimestrales WHERE alumno_id = ?";
            PreparedStatement psTest = conect.prepareStatement(queryTest);
            psTest.setInt(1, alumnoId);
            ResultSet rsTest = psTest.executeQuery();

            if (rsTest.next() && rsTest.getInt(1) > 0) {
                rsTest.close();
                psTest.close();
                return String.valueOf(alumnoId); // Retornar como string pero usar como INT en consultas
            }

            rsTest.close();
            psTest.close();

            // Si no encontró nada como INT, puede que esté como STRING
            return String.valueOf(alumnoId);

        } catch (SQLException e) {
            System.err.println("Error verificando tipo de alumno_id: " + e.getMessage());
            return String.valueOf(alumnoId);
        }
    }

    /**
     * Muestra reporte bimestral general (todos los alumnos, todas las
     * materias).
     */
    /**
     * Muestra reporte bimestral general (todos los alumnos, todas las
     * materias). VERSIÓN FINAL CORREGIDA con mapeo de períodos y búsqueda por
     * DNI.
     */
    private void mostrarReporteBimestralGeneral(int cursoId, String periodo) {
        try {
            // MAPEAR EL PERÍODO CORRECTAMENTE
            String periodoParaBD = mapearPeriodoParaBD(periodo);

            System.out.println("=== REPORTE BIMESTRAL GENERAL (VERSIÓN FINAL) ===");
            System.out.println("Curso ID: " + cursoId);
            System.out.println("Período seleccionado: " + periodo);
            System.out.println("Período para BD: " + periodoParaBD);

            // Crear modelo de tabla
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Alumno");
            modelo.addColumn("DNI");

            // Obtener materias del curso para crear columnas dinámicas
            String queryMaterias = "SELECT DISTINCT m.id, m.nombre "
                    + "FROM materias m INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY m.nombre";

            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            psMaterias.setInt(1, cursoId);
            ResultSet rsMaterias = psMaterias.executeQuery();

            java.util.List<Integer> materiasIds = new java.util.ArrayList<>();
            java.util.List<String> materiasNombres = new java.util.ArrayList<>();

            while (rsMaterias.next()) {
                int materiaId = rsMaterias.getInt("id");
                String materiaNombre = rsMaterias.getString("nombre");
                materiasIds.add(materiaId);
                materiasNombres.add(materiaNombre);
                modelo.addColumn(materiaNombre);
                System.out.println("Materia encontrada: " + materiaNombre + " (ID: " + materiaId + ")");
            }

            rsMaterias.close();
            psMaterias.close();

            // Solo agregar columna promedio si hay materias
            boolean hayMaterias = !materiasIds.isEmpty();
            if (hayMaterias) {
                modelo.addColumn("Promedio General");
                System.out.println("Se agregó columna Promedio General");
            }

            if (!hayMaterias) {
                modelo.addRow(new Object[]{"Sin materias", "No hay materias activas en este curso"});
                tablaNotas.setModel(modelo);
                JOptionPane.showMessageDialog(this,
                        "No se encontraron materias activas para este curso.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // VERIFICACIÓN PREVIA: ¿Hay datos para este período?
            String queryVerificacion = "SELECT COUNT(*) as total FROM notas_bimestrales WHERE periodo = ?";
            PreparedStatement psVerif = conect.prepareStatement(queryVerificacion);
            psVerif.setString(1, periodoParaBD);
            ResultSet rsVerif = psVerif.executeQuery();

            int totalRegistrosPeriodo = 0;
            if (rsVerif.next()) {
                totalRegistrosPeriodo = rsVerif.getInt("total");
            }
            rsVerif.close();
            psVerif.close();

            System.out.println("Total de registros para el período '" + periodoParaBD + "': " + totalRegistrosPeriodo);

            // Obtener alumnos con sus DNIs
            String queryAlumnos = "SELECT u.id, CONCAT(u.apellido, ', ', u.nombre) as nombre_completo, u.dni "
                    + "FROM usuarios u INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND (u.rol = '4' OR u.rol = 4) "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            int contadorAlumnos = 0;
            int alumnosConNotas = 0;

            while (rsAlumnos.next()) {
                int tamanoFila = 2 + materiasIds.size() + 1; // 2 fijas + materias + promedio
                Object[] fila = new Object[tamanoFila];

                fila[0] = rsAlumnos.getString("nombre_completo");
                fila[1] = rsAlumnos.getString("dni");

                int alumnoId = rsAlumnos.getInt("id");
                String alumnoDni = rsAlumnos.getString("dni");

                System.out.println("Procesando alumno: " + fila[0] + " (ID: " + alumnoId + ", DNI: " + alumnoDni + ")");

                double sumaNotas = 0;
                int cantidadMaterias = 0;
                boolean alumnoTieneNotas = false;

                // Obtener nota bimestral para cada materia - VERSIÓN CORREGIDA
                for (int i = 0; i < materiasIds.size(); i++) {
                    int materiaId = materiasIds.get(i);
                    String materiaNombre = materiasNombres.get(i);

                    // CONSULTA CORREGIDA: Buscar por DNI y período mapeado
                    String queryNota = "SELECT nota FROM notas_bimestrales "
                            + "WHERE materia_id = ? AND periodo = ? AND "
                            + "(alumno_id = ? OR alumno_id = CAST(? AS CHAR))";  // CORREGIDO: manejo consistente

                    PreparedStatement psNota = conect.prepareStatement(queryNota);
                    psNota.setInt(1, materiaId);
                    psNota.setString(2, periodoParaBD);
                    psNota.setString(3, alumnoDni);    // Usar DNI como string
                    psNota.setInt(4, alumnoId);        // También probar con ID como int

                    ResultSet rsNota = psNota.executeQuery();

                    if (rsNota.next()) {
                        double nota = rsNota.getDouble("nota");
                        fila[2 + i] = nota;
                        sumaNotas += nota;
                        cantidadMaterias++;
                        alumnoTieneNotas = true;
                        System.out.println("  ✅ " + materiaNombre + ": " + nota);
                    } else {
                        fila[2 + i] = "-";
                        System.out.println("  ❌ " + materiaNombre + ": Sin nota");
                    }
                    rsNota.close();
                    psNota.close();
                }

                // Calcular promedio general
                if (cantidadMaterias > 0) {
                    fila[fila.length - 1] = Math.round((sumaNotas / cantidadMaterias) * 100.0) / 100.0;
                    System.out.println("  📊 Promedio: " + fila[fila.length - 1]);
                } else {
                    fila[fila.length - 1] = "-";
                    System.out.println("  📊 Sin promedio (no hay notas)");
                }

                if (alumnoTieneNotas) {
                    alumnosConNotas++;
                }

                modelo.addRow(fila);
                contadorAlumnos++;
            }

            rsAlumnos.close();
            psAlumnos.close();

            System.out.println("=== REPORTE GENERAL COMPLETADO ===");
            System.out.println("Alumnos procesados: " + contadorAlumnos);
            System.out.println("Alumnos con notas: " + alumnosConNotas);
            System.out.println("Materias procesadas: " + materiasIds.size());

            // Asignar modelo
            tablaNotas.setModel(modelo);
            tablaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tablaNotas.setRowHeight(25);

            // Ajustar anchos de columnas básicas
            if (tablaNotas.getColumnModel().getColumnCount() > 0) {
                tablaNotas.getColumnModel().getColumn(0).setPreferredWidth(200); // Alumno
            }
            if (tablaNotas.getColumnModel().getColumnCount() > 1) {
                tablaNotas.getColumnModel().getColumn(1).setPreferredWidth(100); // DNI
            }

            if (contadorAlumnos == 0) {
                modelo.addRow(new Object[]{"Sin alumnos", "No hay alumnos en este curso"});
                JOptionPane.showMessageDialog(this,
                        "No se encontraron alumnos para el curso seleccionado.",
                        "Sin alumnos",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (alumnosConNotas == 0) {
                JOptionPane.showMessageDialog(this,
                        "Reporte generado con " + contadorAlumnos + " alumnos, pero ninguno tiene notas bimestrales.\n\n"
                        + "Datos de búsqueda:\n"
                        + "- Período: " + periodo + " → '" + periodoParaBD + "'\n"
                        + "- Registros totales en BD para este período: " + totalRegistrosPeriodo + "\n"
                        + "- Materias del curso: " + materiasIds.size() + "\n\n"
                        + "Posible causa: Los DNI de los alumnos del curso no coinciden con\n"
                        + "los alumno_id en la tabla notas_bimestrales",
                        "Diagnóstico detallado",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Reporte general cargado exitosamente:\n"
                        + "- Alumnos: " + contadorAlumnos + "\n"
                        + "- Con notas: " + alumnosConNotas + "\n"
                        + "- Sin notas: " + (contadorAlumnos - alumnosConNotas) + "\n"
                        + "- Materias: " + materiasIds.size(),
                        "Reporte completado",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error al mostrar reporte bimestral general: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar reporte: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra reporte bimestral por materia específica.
     */
    private void mostrarReporteBimestralPorMateria(int cursoId, String periodo, String materia) {
        try {
            Integer materiaId = materiasMap.get(materia);
            if (materiaId == null) {
                JOptionPane.showMessageDialog(this, "Error: No se encontró el ID de la materia", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // MAPEAR EL PERÍODO CORRECTAMENTE
            String periodoParaBD = mapearPeriodoParaBD(periodo);

            System.out.println("=== REPORTE BIMESTRAL POR MATERIA (VERSIÓN FINAL) ===");
            System.out.println("Materia: " + materia + " (ID: " + materiaId + ")");
            System.out.println("Período seleccionado: " + periodo);
            System.out.println("Período para BD: " + periodoParaBD);
            System.out.println("Curso ID: " + cursoId);

            // Crear modelo de tabla
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Alumno");
            modelo.addColumn("DNI");
            modelo.addColumn("Nota " + periodo);
            modelo.addColumn("Promedio Actividades");
            modelo.addColumn("Estado");

            // VERIFICACIÓN PREVIA: ¿Hay datos para esta materia y período?
            String queryVerificacion = "SELECT COUNT(*) as total FROM notas_bimestrales WHERE materia_id = ? AND periodo = ?";
            PreparedStatement psVerif = conect.prepareStatement(queryVerificacion);
            psVerif.setInt(1, materiaId);
            psVerif.setString(2, periodoParaBD);
            ResultSet rsVerif = psVerif.executeQuery();

            int totalRegistros = 0;
            if (rsVerif.next()) {
                totalRegistros = rsVerif.getInt("total");
            }
            rsVerif.close();
            psVerif.close();

            System.out.println("Registros encontrados para materia " + materiaId + " y período '" + periodoParaBD + "': " + totalRegistros);

            // Obtener alumnos del curso
            String queryAlumnos = "SELECT u.id, CONCAT(u.apellido, ', ', u.nombre) as nombre_completo, u.dni "
                    + "FROM usuarios u INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND (u.rol = '4' OR u.rol = 4) "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            int contadorAlumnos = 0;
            int notasEncontradas = 0;

            while (rsAlumnos.next()) {
                Object[] fila = new Object[5];
                fila[0] = rsAlumnos.getString("nombre_completo");
                fila[1] = rsAlumnos.getString("dni");

                int alumnoId = rsAlumnos.getInt("id");
                String alumnoDni = rsAlumnos.getString("dni");

                System.out.println("Procesando alumno: " + fila[0] + " (ID: " + alumnoId + ", DNI: " + alumnoDni + ")");

                // CONSULTA CORREGIDA: Buscar por DNI principalmente, ya que en tu BD el alumno_id parece ser el DNI
                String queryNota = "SELECT nota, promedio_actividades, estado FROM notas_bimestrales "
                        + "WHERE materia_id = ? AND periodo = ? AND "
                        + "(alumno_id = ? OR alumno_id = CAST(? AS CHAR))";  // CORREGIDO: manejo consistente

                PreparedStatement psNota = conect.prepareStatement(queryNota);
                psNota.setInt(1, materiaId);
                psNota.setString(2, periodoParaBD);
                psNota.setString(3, alumnoDni);    // Usar DNI como string  
                psNota.setInt(4, alumnoId);        // También probar con ID como int

                ResultSet rsNota = psNota.executeQuery();

                if (rsNota.next()) {
                    fila[2] = rsNota.getDouble("nota");
                    fila[3] = rsNota.getDouble("promedio_actividades");
                    fila[4] = rsNota.getString("estado");
                    notasEncontradas++;
                    System.out.println("  ✅ Nota encontrada: " + fila[2] + " (promedio: " + fila[3] + ", estado: " + fila[4] + ")");
                } else {
                    fila[2] = "-";
                    fila[3] = "-";
                    fila[4] = "Pendiente";
                    System.out.println("  ❌ Sin nota bimestral para este alumno");

                    // DEBUG: Mostrar qué valores se buscaron
                    System.out.println("      Buscado: materia_id=" + materiaId + ", periodo='" + periodoParaBD + "', alumno_id=" + alumnoDni + " o " + alumnoId);
                }

                rsNota.close();
                psNota.close();
                modelo.addRow(fila);
                contadorAlumnos++;
            }

            rsAlumnos.close();
            psAlumnos.close();

            System.out.println("=== REPORTE POR MATERIA COMPLETADO ===");
            System.out.println("Alumnos procesados: " + contadorAlumnos);
            System.out.println("Notas encontradas: " + notasEncontradas);

            // Asignar modelo
            tablaNotas.setModel(modelo);
            tablaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            tablaNotas.setRowHeight(25);

            if (contadorAlumnos == 0) {
                modelo.addRow(new Object[]{"Sin alumnos", "No hay alumnos en este curso", "-", "-", "-"});
                JOptionPane.showMessageDialog(this,
                        "No se encontraron alumnos para el curso seleccionado.",
                        "Sin alumnos",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (notasEncontradas == 0) {
                JOptionPane.showMessageDialog(this,
                        "Se encontraron " + contadorAlumnos + " alumnos, pero ninguno tiene notas bimestrales.\n\n"
                        + "Datos de búsqueda:\n"
                        + "- Materia: " + materia + " (ID: " + materiaId + ")\n"
                        + "- Período: " + periodo + " → '" + periodoParaBD + "'\n"
                        + "- Registros en BD para esta materia/período: " + totalRegistros + "\n\n"
                        + "Posibles causas:\n"
                        + "- Los DNI de los alumnos no coinciden con los alumno_id en notas_bimestrales\n"
                        + "- Los alumnos del curso no están en la tabla notas_bimestrales",
                        "Diagnóstico detallado",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Reporte cargado exitosamente:\n"
                        + "- Alumnos: " + contadorAlumnos + "\n"
                        + "- Con notas: " + notasEncontradas + "\n"
                        + "- Sin notas: " + (contadorAlumnos - notasEncontradas),
                        "Reporte completado",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error al mostrar reporte bimestral por materia: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar reporte: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra reporte bimestral por alumno específico.
     */
    private void mostrarReporteBimestralPorAlumno(int cursoId, String periodo, String alumno) {
        try {
            Integer alumnoId = alumnosMap.get(alumno);
            if (alumnoId == null) {
                JOptionPane.showMessageDialog(this, "Error: No se encontró el ID del alumno", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // MAPEAR EL PERÍODO CORRECTAMENTE
            String periodoParaBD = mapearPeriodoParaBD(periodo);

            System.out.println("=== REPORTE BIMESTRAL POR ALUMNO (VERSIÓN CORREGIDA) ===");
            System.out.println("Alumno: " + alumno + " (ID: " + alumnoId + ")");
            System.out.println("Período seleccionado: " + periodo);
            System.out.println("Período para BD: " + periodoParaBD);
            System.out.println("Curso ID: " + cursoId);

            // Obtener DNI del alumno para la búsqueda
            String queryAlumnoInfo = "SELECT dni FROM usuarios WHERE id = ?";
            PreparedStatement psAlumnoInfo = conect.prepareStatement(queryAlumnoInfo);
            psAlumnoInfo.setInt(1, alumnoId);
            ResultSet rsAlumnoInfo = psAlumnoInfo.executeQuery();

            String alumnoDni = null;
            if (rsAlumnoInfo.next()) {
                alumnoDni = rsAlumnoInfo.getString("dni");
                System.out.println("DNI del alumno: " + alumnoDni);
            }
            rsAlumnoInfo.close();
            psAlumnoInfo.close();

            // Crear modelo de tabla
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Materia");
            modelo.addColumn("Nota " + periodo);
            modelo.addColumn("Promedio Actividades");
            modelo.addColumn("Estado");

            // VERIFICACIÓN PREVIA: ¿Hay datos para este alumno y período?
            String queryVerificacion = "SELECT COUNT(*) as total FROM notas_bimestrales WHERE periodo = ? AND "
                    + "(alumno_id = ? OR alumno_id = ? OR alumno_id = CAST(? AS CHAR))";
            PreparedStatement psVerif = conect.prepareStatement(queryVerificacion);
            psVerif.setString(1, periodoParaBD);
            psVerif.setString(2, alumnoDni);
            psVerif.setInt(3, alumnoId);
            psVerif.setInt(4, alumnoId);
            ResultSet rsVerif = psVerif.executeQuery();

            int totalRegistrosAlumno = 0;
            if (rsVerif.next()) {
                totalRegistrosAlumno = rsVerif.getInt("total");
            }
            rsVerif.close();
            psVerif.close();

            System.out.println("Registros encontrados para el alumno en período '" + periodoParaBD + "': " + totalRegistrosAlumno);

            // Obtener materias del curso
            String queryMaterias = "SELECT DISTINCT m.id, m.nombre "
                    + "FROM materias m INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY m.nombre";

            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            psMaterias.setInt(1, cursoId);
            ResultSet rsMaterias = psMaterias.executeQuery();

            int contadorMaterias = 0;
            int notasEncontradas = 0;

            while (rsMaterias.next()) {
                Object[] fila = new Object[4];
                fila[0] = rsMaterias.getString("nombre");

                int materiaId = rsMaterias.getInt("id");
                String materiaNombre = rsMaterias.getString("nombre");

                System.out.println("Procesando materia: " + materiaNombre + " (ID: " + materiaId + ")");

                // CONSULTA CORREGIDA: Buscar por DNI y período mapeado
                String queryNota = "SELECT nota, promedio_actividades, estado FROM notas_bimestrales "
                        + "WHERE materia_id = ? AND periodo = ? AND "
                        + "(alumno_id = ? OR alumno_id = CAST(? AS CHAR))";  // CORREGIDO: manejo consistente

                PreparedStatement psNota = conect.prepareStatement(queryNota);
                psNota.setInt(1, materiaId);
                psNota.setString(2, periodoParaBD);
                psNota.setString(3, alumnoDni);    // Usar DNI como string
                psNota.setInt(4, alumnoId);        // También probar con ID como int

                ResultSet rsNota = psNota.executeQuery();

                if (rsNota.next()) {
                    fila[1] = rsNota.getDouble("nota");
                    fila[2] = rsNota.getDouble("promedio_actividades");
                    fila[3] = rsNota.getString("estado");
                    notasEncontradas++;
                    System.out.println("  ✅ Nota encontrada para " + materiaNombre + ": " + fila[1]
                            + " (promedio: " + fila[2] + ", estado: " + fila[3] + ")");
                } else {
                    fila[1] = "-";
                    fila[2] = "-";
                    fila[3] = "Pendiente";
                    System.out.println("  ❌ Sin nota para " + materiaNombre);

                    // DEBUG: Mostrar qué valores se buscaron
                    System.out.println("      Buscado: materia_id=" + materiaId + ", periodo='" + periodoParaBD
                            + "', alumno_id=" + alumnoDni + " o " + alumnoId);
                }

                rsNota.close();
                psNota.close();
                modelo.addRow(fila);
                contadorMaterias++;
            }

            rsMaterias.close();
            psMaterias.close();

            System.out.println("=== REPORTE POR ALUMNO COMPLETADO ===");
            System.out.println("Materias procesadas: " + contadorMaterias);
            System.out.println("Notas encontradas: " + notasEncontradas);

            // Asignar modelo
            tablaNotas.setModel(modelo);
            tablaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            tablaNotas.setRowHeight(25);

            if (contadorMaterias == 0) {
                modelo.addRow(new Object[]{"Sin materias", "No hay materias para este curso", "-", "-"});
                JOptionPane.showMessageDialog(this,
                        "No se encontraron materias para el curso seleccionado.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (notasEncontradas == 0) {
                JOptionPane.showMessageDialog(this,
                        "Se encontraron " + contadorMaterias + " materias, pero el alumno no tiene notas bimestrales.\n\n"
                        + "Datos de búsqueda:\n"
                        + "- Alumno: " + alumno + " (ID: " + alumnoId + ", DNI: " + alumnoDni + ")\n"
                        + "- Período: " + periodo + " → '" + periodoParaBD + "'\n"
                        + "- Registros en BD para este alumno/período: " + totalRegistrosAlumno + "\n\n"
                        + "Posibles causas:\n"
                        + "- El DNI del alumno no coincide con el alumno_id en notas_bimestrales\n"
                        + "- El alumno no tiene notas para las materias de este curso",
                        "Diagnóstico detallado",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Reporte del alumno cargado exitosamente:\n"
                        + "- Materias: " + contadorMaterias + "\n"
                        + "- Con notas: " + notasEncontradas + "\n"
                        + "- Sin notas: " + (contadorMaterias - notasEncontradas),
                        "Reporte completado",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error al mostrar reporte bimestral por alumno: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar reporte: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exporta las notas mostradas actualmente con opciones avanzadas.
     */
    private void exportarNotas() {
        if (tablaNotas.getModel().getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mostrar diálogo de opciones de exportación
        String[] opciones = {"Exportar Vista Actual", "Exportación Múltiple", "Cancelar"};
        int seleccion = JOptionPane.showOptionDialog(this,
                "Seleccione el tipo de exportación:",
                "Opciones de Exportación",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        switch (seleccion) {
            case 0: // Exportar vista actual
                exportarVistaActual();
                break;
            case 1: // Exportación múltiple
                mostrarDialogoExportacionMultiple();
                break;
            case 2: // Cancelar
            default:
                return;
        }
    }

    /**
     * Exporta solo la vista actual mostrada en la tabla.
     */
    private void exportarVistaActual() {
        String titulo = "";
        String nombreArchivo = "";

        if (rbPorMateria.isSelected()) {
            String curso = (String) comboCursos.getSelectedItem();
            String materia = (String) comboMaterias.getSelectedItem();
            titulo = "Notas por Materia - " + curso + " - " + materia;
            nombreArchivo = "Notas_" + curso.replace("°", "") + "_" + materia.replace(" ", "_");
        } else if (rbPorAlumno.isSelected()) {
            String curso = (String) comboCursos.getSelectedItem();
            String alumno = (String) comboAlumnos.getSelectedItem();
            titulo = "Notas por Alumno - " + curso + " - " + alumno;
            nombreArchivo = "Notas_Alumno_" + curso.replace("°", "") + "_" + alumno.split(",")[0];
        } else if (rbReporteBimestral.isSelected()) {
            String curso = (String) comboCursos.getSelectedItem();
            String periodo = (String) comboPeriodos.getSelectedItem();
            String materia = (String) comboMaterias.getSelectedItem();
            String alumno = (String) comboAlumnos.getSelectedItem();

            if (materia != null && !materia.isEmpty()) {
                titulo = "Reporte Bimestral por Materia - " + curso + " - " + materia + " - " + periodo;
                nombreArchivo = "Reporte_" + curso.replace("°", "") + "_" + materia.replace(" ", "_") + "_" + periodo;
            } else if (alumno != null && !alumno.isEmpty()) {
                titulo = "Reporte Bimestral por Alumno - " + curso + " - " + alumno + " - " + periodo;
                nombreArchivo = "Reporte_" + curso.replace("°", "") + "_" + alumno.split(",")[0] + "_" + periodo;
            } else {
                titulo = "Reporte Bimestral General - " + curso + " - " + periodo;
                nombreArchivo = "Reporte_" + curso.replace("°", "") + "_" + periodo;
            }
        }

        main.java.utils.ExcelExportUtility.exportarTablaAExcel(tablaNotas, titulo, nombreArchivo);
    }

    /**
     * Muestra el diálogo para exportación múltiple.
     */
    private void mostrarDialogoExportacionMultiple() {
        // Crear diálogo personalizado
        javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Exportación Múltiple", true);
        dialog.setLayout(new java.awt.BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        // Panel principal
        javax.swing.JPanel panelPrincipal = new javax.swing.JPanel(new java.awt.BorderLayout());

        // Título
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("Seleccione qué exportar:", javax.swing.SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(lblTitulo, java.awt.BorderLayout.NORTH);

        // Panel de opciones
        javax.swing.JPanel panelOpciones = new javax.swing.JPanel(new java.awt.GridBagLayout());
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        // Checkboxes para diferentes tipos de exportación
        javax.swing.JCheckBox chkNotasPorMateria = new javax.swing.JCheckBox("Notas por Materia (todas las materias)", true);
        javax.swing.JCheckBox chkReporteGeneral = new javax.swing.JCheckBox("Reporte Bimestral General", true);
        javax.swing.JCheckBox chkReportePorMaterias = new javax.swing.JCheckBox("Reportes Bimestrales por Materia", false);
        javax.swing.JCheckBox chkNotasPorAlumnos = new javax.swing.JCheckBox("Notas por Alumno (todos los alumnos)", false);

        // Combos para filtros
        javax.swing.JLabel lblCurso = new javax.swing.JLabel("Curso:");
        javax.swing.JComboBox<String> comboCursoExport = new javax.swing.JComboBox<>();
        for (String curso : cursosMap.keySet()) {
            comboCursoExport.addItem(curso);
        }
        if (comboCursos.getSelectedItem() != null) {
            comboCursoExport.setSelectedItem(comboCursos.getSelectedItem());
        }

        javax.swing.JLabel lblPeriodo = new javax.swing.JLabel("Período:");
        javax.swing.JComboBox<String> comboPeriodoExport = new javax.swing.JComboBox<>(new String[]{"1B", "2B", "3B", "4B", "1C", "2C", "Final"});
        if (comboPeriodos.getSelectedItem() != null) {
            comboPeriodoExport.setSelectedItem(comboPeriodos.getSelectedItem());
        }

        // Agregar componentes al panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelOpciones.add(chkNotasPorMateria, gbc);

        gbc.gridy = 1;
        panelOpciones.add(chkReporteGeneral, gbc);

        gbc.gridy = 2;
        panelOpciones.add(chkReportePorMaterias, gbc);

        gbc.gridy = 3;
        panelOpciones.add(chkNotasPorAlumnos, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panelOpciones.add(lblCurso, gbc);
        gbc.gridx = 1;
        panelOpciones.add(comboCursoExport, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panelOpciones.add(lblPeriodo, gbc);
        gbc.gridx = 1;
        panelOpciones.add(comboPeriodoExport, gbc);

        panelPrincipal.add(panelOpciones, java.awt.BorderLayout.CENTER);

        // Panel de botones
        javax.swing.JPanel panelBotones = new javax.swing.JPanel(new java.awt.FlowLayout());
        javax.swing.JButton btnExportar = new javax.swing.JButton("Exportar Seleccionados");
        javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");

        styleButton(btnExportar, new Color(46, 125, 50));
        styleButton(btnCancelar, new Color(156, 156, 156));

        panelBotones.add(btnExportar);
        panelBotones.add(btnCancelar);
        panelPrincipal.add(panelBotones, java.awt.BorderLayout.SOUTH);

        // Listeners
        btnCancelar.addActionListener(e -> dialog.dispose());

        btnExportar.addActionListener(e -> {
            String cursoSeleccionado = (String) comboCursoExport.getSelectedItem();
            String periodoSeleccionado = (String) comboPeriodoExport.getSelectedItem();

            if (cursoSeleccionado == null) {
                JOptionPane.showMessageDialog(dialog, "Seleccione un curso", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            dialog.dispose();

            // Ejecutar exportación múltiple en hilo separado
            javax.swing.SwingWorker<Void, String> worker = new javax.swing.SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    realizarExportacionMultiple(
                            cursoSeleccionado,
                            periodoSeleccionado,
                            chkNotasPorMateria.isSelected(),
                            chkReporteGeneral.isSelected(),
                            chkReportePorMaterias.isSelected(),
                            chkNotasPorAlumnos.isSelected()
                    );
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Para capturar excepciones
                        System.out.println("Exportación múltiple finalizada");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(NotasVisualizationPanel.this,
                                "Error durante la exportación: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        });

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }

    /**
     * Realiza la exportación múltiple según las opciones seleccionadas.
     */
    private void realizarExportacionMultiple(String curso, String periodo,
            boolean notasPorMateria, boolean reporteGeneral,
            boolean reportePorMaterias, boolean notasPorAlumnos) {
        try {
            Integer cursoId = cursosMap.get(curso);
            if (cursoId == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: No se encontró el curso seleccionado",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int exportacionesRealizadas = 0;
            StringBuilder resultado = new StringBuilder();
            resultado.append("=== REPORTE DE EXPORTACIÓN ===\n");
            resultado.append("Curso: ").append(curso).append("\n");
            resultado.append("Fecha: ").append(java.time.LocalDate.now()).append("\n\n");

            // 1. Exportar notas por materia
            if (notasPorMateria) {
                resultado.append("📚 NOTAS POR MATERIA:\n");
                try {
                    int materiasExportadas = exportarTodasLasMaterias(cursoId, curso);
                    exportacionesRealizadas += materiasExportadas;
                    resultado.append("   ✓ Materias exportadas: ").append(materiasExportadas).append("\n");
                } catch (Exception e) {
                    resultado.append("   ✗ Error: ").append(e.getMessage()).append("\n");
                    System.err.println("Error exportando materias: " + e.getMessage());
                }
            }

            // 2. Exportar reporte bimestral general
            if (reporteGeneral) {
                resultado.append("\n📊 REPORTE BIMESTRAL GENERAL:\n");
                try {
                    exportarReporteBimestralGeneral(cursoId, curso, periodo);
                    exportacionesRealizadas++;
                    resultado.append("   ✓ Reporte general exportado\n");
                } catch (Exception e) {
                    resultado.append("   ✗ Error: ").append(e.getMessage()).append("\n");
                    System.err.println("Error exportando reporte general: " + e.getMessage());
                }
            }

            // 3. Exportar reportes por materias
            if (reportePorMaterias) {
                resultado.append("\n📈 REPORTES POR MATERIA:\n");
                try {
                    int reportesMateria = exportarReportesPorMaterias(cursoId, curso, periodo);
                    exportacionesRealizadas += reportesMateria;
                    resultado.append("   ✓ Reportes por materia: ").append(reportesMateria).append("\n");
                } catch (Exception e) {
                    resultado.append("   ✗ Error: ").append(e.getMessage()).append("\n");
                    System.err.println("Error exportando reportes por materia: " + e.getMessage());
                }
            }

            // 4. Exportar notas por todos los alumnos
            if (notasPorAlumnos) {
                resultado.append("\n👥 NOTAS POR ALUMNO:\n");
                try {
                    int alumnosExportados = exportarNotasPorTodosLosAlumnos(cursoId, curso);
                    exportacionesRealizadas += alumnosExportados;
                    resultado.append("   ✓ Alumnos exportados: ").append(alumnosExportados).append("\n");

                    if (alumnosExportados == 0) {
                        resultado.append("   ⚠️ Posibles causas de 0 exportaciones:\n");
                        resultado.append("      - No hay alumnos en el curso\n");
                        resultado.append("      - No hay notas registradas\n");
                        resultado.append("      - Error en la base de datos\n");
                    }
                } catch (Exception e) {
                    resultado.append("   ✗ Error crítico: ").append(e.getMessage()).append("\n");
                    System.err.println("Error exportando notas por alumno: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Mostrar resultado final
            resultado.append("\n=== RESUMEN ===\n");
            resultado.append("Total de archivos generados: ").append(exportacionesRealizadas).append("\n");

            if (exportacionesRealizadas > 0) {
                resultado.append("✅ Exportación completada exitosamente");
            } else {
                resultado.append("⚠️ No se generaron archivos - Verifique los datos");
            }

            System.out.println(resultado.toString());

            // Mostrar resultado detallado al usuario
            final int totalExportadas = exportacionesRealizadas; // Variable final para usar en lambda
            SwingUtilities.invokeLater(() -> {
                javax.swing.JTextArea textArea = new javax.swing.JTextArea(resultado.toString());
                textArea.setEditable(false);
                textArea.setRows(15);
                textArea.setColumns(60);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);

                JOptionPane.showMessageDialog(this,
                        scrollPane,
                        "Resultado de Exportación Múltiple",
                        totalExportadas > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            });

        } catch (Exception e) {
            System.err.println("Error general en exportación múltiple: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error crítico durante la exportación múltiple:\n"
                        + e.getMessage() + "\n\n"
                        + "Revise la consola para más detalles.",
                        "Error Crítico",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    /**
     * NUEVO MÉTODO: Exporta un reporte completo de notas para cada alumno del
     * curso. Genera un archivo Excel individual por alumno con todas sus
     * materias y notas.
     */
    private int exportarNotasPorTodosLosAlumnos(int cursoId, String curso) {
        try {
            System.out.println("=== INICIANDO EXPORTACIÓN DE NOTAS POR ALUMNO ===");
            System.out.println("Curso: " + curso + " (ID: " + cursoId + ")");

            // Obtener todos los alumnos del curso
            java.util.List<AlumnoCompleto> alumnos = obtenerAlumnosDelCurso(cursoId);

            if (alumnos.isEmpty()) {
                System.out.println("No se encontraron alumnos en el curso " + curso);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "No se encontraron alumnos en el curso " + curso,
                            "Sin datos",
                            JOptionPane.INFORMATION_MESSAGE);
                });
                return 0;
            }

            System.out.println("Alumnos encontrados: " + alumnos.size());

            // Variable para contar exportaciones exitosas
            final int[] exportadas = {0};

            // Crear ventana de progreso
            javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar(0, alumnos.size());
            progressBar.setStringPainted(true);
            progressBar.setString("Preparando exportación...");

            javax.swing.JDialog progressDialog = new javax.swing.JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), "Exportando Notas", true);
            progressDialog.setLayout(new java.awt.BorderLayout());
            progressDialog.add(new javax.swing.JLabel("Exportando notas de alumnos...", javax.swing.SwingConstants.CENTER), java.awt.BorderLayout.NORTH);
            progressDialog.add(progressBar, java.awt.BorderLayout.CENTER);
            progressDialog.setSize(400, 100);
            progressDialog.setLocationRelativeTo(this);

            // Procesar cada alumno en hilo separado
            javax.swing.SwingWorker<Integer, String> worker = new javax.swing.SwingWorker<Integer, String>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    int count = 0;

                    for (int i = 0; i < alumnos.size(); i++) {
                        AlumnoCompleto alumno = alumnos.get(i);

                        publish("Procesando: " + alumno.nombreCompleto + " (" + (i + 1) + "/" + alumnos.size() + ")");

                        try {
                            if (exportarNotasIndividualAlumno(alumno, cursoId, curso)) {
                                count++;
                                System.out.println("✓ Exportado: " + alumno.nombreCompleto);
                            } else {
                                System.out.println("✗ Falló: " + alumno.nombreCompleto);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error exportando " + alumno.nombreCompleto + ": " + ex.getMessage());
                            ex.printStackTrace();
                        }

                        // Actualizar progreso
                        setProgress((i + 1) * 100 / alumnos.size());
                    }

                    return count;
                }

                @Override
                protected void process(java.util.List<String> chunks) {
                    for (String message : chunks) {
                        progressBar.setString(message);
                    }
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        int resultado = get();
                        exportadas[0] = resultado;

                        String mensaje = "Exportación completada.\n"
                                + "Archivos generados: " + resultado + " de " + alumnos.size() + " alumnos";

                        JOptionPane.showMessageDialog(NotasVisualizationPanel.this,
                                mensaje,
                                "Exportación Finalizada",
                                JOptionPane.INFORMATION_MESSAGE);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(NotasVisualizationPanel.this,
                                "Error durante la exportación: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            // Configurar progreso
            worker.addPropertyChangeListener(evt -> {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            });

            // Iniciar worker y mostrar diálogo
            worker.execute();
            progressDialog.setVisible(true);

            return exportadas[0];

        } catch (Exception e) {
            System.err.println("Error general en exportación de notas por alumno: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error al exportar notas por alumno: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
            return 0;
        }
    }

    /**
     * Obtiene la lista completa de alumnos de un curso. CORREGIDO: Agregado
     * diagnóstico para debug.
     */
    private java.util.List<AlumnoCompleto> obtenerAlumnosDelCurso(int cursoId) {
        java.util.List<AlumnoCompleto> alumnos = new java.util.ArrayList<>();

        try {
            System.out.println("Buscando alumnos del curso ID: " + cursoId);

            String query = "SELECT u.id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AlumnoCompleto alumno = new AlumnoCompleto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni")
                );
                alumnos.add(alumno);
                System.out.println("Alumno cargado: " + alumno.nombreCompleto + " (ID: " + alumno.id + ")");
            }

            // Si no hay alumnos, hacer diagnóstico
            if (alumnos.isEmpty()) {
                System.out.println("No se encontraron alumnos. Ejecutando diagnóstico...");
                diagnosticarCurso(cursoId);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener alumnos del curso: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnos;
    }

    /**
     * Diagnóstico para verificar por qué no se encuentran alumnos en un curso.
     */
    private void diagnosticarCurso(int cursoId) {
        try {
            System.out.println("=== DIAGNÓSTICO DEL CURSO ===");
            System.out.println("Curso ID: " + cursoId);

            // 1. Verificar si el curso existe
            String queryCurso = "SELECT id, anio, division FROM cursos WHERE id = ?";
            PreparedStatement psCurso = conect.prepareStatement(queryCurso);
            psCurso.setInt(1, cursoId);
            ResultSet rsCurso = psCurso.executeQuery();
            if (rsCurso.next()) {
                System.out.println("✓ Curso encontrado: " + rsCurso.getInt("anio") + "°" + rsCurso.getString("division"));
            } else {
                System.out.println("✗ Curso NO encontrado");
                return;
            }

            // 2. Verificar total de alumnos inscitos (incluyendo inactivos)
            String queryTotalAlumnos = "SELECT COUNT(*) as total FROM alumno_curso WHERE curso_id = ?";
            PreparedStatement psTotalAlumnos = conect.prepareStatement(queryTotalAlumnos);
            psTotalAlumnos.setInt(1, cursoId);
            ResultSet rsTotalAlumnos = psTotalAlumnos.executeQuery();
            if (rsTotalAlumnos.next()) {
                System.out.println("✓ Total alumnos inscitos (todos): " + rsTotalAlumnos.getInt("total"));
            }

            // 3. Verificar alumnos activos
            String queryActivos = "SELECT COUNT(*) as total FROM alumno_curso WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement psActivos = conect.prepareStatement(queryActivos);
            psActivos.setInt(1, cursoId);
            ResultSet rsActivos = psActivos.executeQuery();
            if (rsActivos.next()) {
                System.out.println("✓ Alumnos activos: " + rsActivos.getInt("total"));
            }

            // 4. Mostrar algunos ejemplos de alumnos
            String queryEjemplos = "SELECT u.id, u.nombre, u.apellido, ac.estado "
                    + "FROM usuarios u "
                    + "INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? LIMIT 5";
            PreparedStatement psEjemplos = conect.prepareStatement(queryEjemplos);
            psEjemplos.setInt(1, cursoId);
            ResultSet rsEjemplos = psEjemplos.executeQuery();

            System.out.println("--- Ejemplos de alumnos en el curso ---");
            while (rsEjemplos.next()) {
                System.out.println("  ID: " + rsEjemplos.getInt("id")
                        + " - " + rsEjemplos.getString("apellido") + ", " + rsEjemplos.getString("nombre")
                        + " (Estado: " + rsEjemplos.getString("estado") + ")");
            }

            System.out.println("=== FIN DIAGNÓSTICO DEL CURSO ===");

        } catch (SQLException e) {
            System.err.println("Error en diagnóstico del curso: " + e.getMessage());
        }
    }

    /**
     * Exporta las notas de un alumno individual a un archivo Excel.
     */
    private boolean exportarNotasIndividualAlumno(AlumnoCompleto alumno, int cursoId, String curso) {
        try {
            System.out.println("Exportando notas para: " + alumno.nombreCompleto);

            // Obtener todas las notas del alumno organizadas por materia
            java.util.Map<String, java.util.List<NotaDetalle>> notasPorMateria
                    = obtenerNotasAlumnoPorMateria(alumno.id, cursoId);

            if (notasPorMateria.isEmpty()) {
                System.out.println("Sin notas para " + alumno.nombreCompleto);
                // Crear archivo con mensaje de "sin notas"
                return crearArchivoSinNotas(alumno, curso);
            }

            // Crear modelo de tabla para exportación
            javax.swing.table.DefaultTableModel modelo = crearModeloNotasAlumno(notasPorMateria, alumno);

            if (modelo.getRowCount() == 0) {
                return crearArchivoSinNotas(alumno, curso);
            }

            // Crear tabla temporal para exportación
            javax.swing.JTable tablaExportacion = new javax.swing.JTable(modelo);
            configurarTablaExportacion(tablaExportacion);

            // Crear nombres de archivo y título
            String nombreArchivo = generarNombreArchivo(alumno, curso);
            String titulo = "Reporte de Notas - " + alumno.nombreCompleto + " - Curso " + curso;

            // Exportar usando ExcelExportUtility
            boolean exito = main.java.utils.ExcelExportUtility.exportarTablaAExcel(
                    tablaExportacion, titulo, nombreArchivo);

            if (exito) {
                System.out.println("✓ Archivo creado: " + nombreArchivo + ".xlsx");
            } else {
                System.out.println("✗ Error al crear archivo para: " + alumno.nombreCompleto);
            }

            return exito;

        } catch (Exception e) {
            System.err.println("Error al exportar notas de " + alumno.nombreCompleto + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todas las notas de un alumno organizadas por materia.
     */
    private java.util.Map<String, java.util.List<NotaDetalle>> obtenerNotasAlumnoPorMateria(int alumnoId, int cursoId) {
        java.util.Map<String, java.util.List<NotaDetalle>> notasPorMateria = new java.util.LinkedHashMap<>();

        try {
            // Obtener DNI del alumno para búsqueda en notas_bimestrales
            String queryAlumnoInfo = "SELECT dni FROM usuarios WHERE id = ?";
            PreparedStatement psAlumnoInfo = conect.prepareStatement(queryAlumnoInfo);
            psAlumnoInfo.setInt(1, alumnoId);
            ResultSet rsAlumnoInfo = psAlumnoInfo.executeQuery();

            String alumnoDni = null;
            if (rsAlumnoInfo.next()) {
                alumnoDni = rsAlumnoInfo.getString("dni");
            }
            rsAlumnoInfo.close();
            psAlumnoInfo.close();

            System.out.println("=== OBTENER NOTAS ALUMNO (CORREGIDO) ===");
            System.out.println("Alumno ID: " + alumnoId + ", DNI: " + alumnoDni + ", Curso ID: " + cursoId);

            // 1. OBTENER NOTAS DE TRABAJOS (tabla 'notas') - CORREGIDO
            String queryTrabajos = "SELECT "
                    + "m.nombre as materia, "
                    + "t.nombre as trabajo, "
                    + "n.nota, "
                    + "n.fecha_carga "
                    + "FROM notas n "
                    + "INNER JOIN trabajos t ON n.trabajo_id = t.id "
                    + "INNER JOIN materias m ON t.materia_id = m.id "
                    + "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' AND "
                    + "(n.alumno_id = ? OR n.alumno_id = CAST(? AS CHAR)) " // CORREGIDO: usar DNI consistentemente
                    + "ORDER BY m.nombre, t.nombre";

            PreparedStatement psTrabajos = conect.prepareStatement(queryTrabajos);
            psTrabajos.setInt(1, cursoId);
            psTrabajos.setString(2, alumnoDni);      // Usar DNI como string
            psTrabajos.setInt(3, alumnoId);          // También probar con ID como int
            ResultSet rsTrabajos = psTrabajos.executeQuery();

            int contadorTrabajos = 0;
            while (rsTrabajos.next()) {
                String materia = rsTrabajos.getString("materia");

                NotaDetalle nota = new NotaDetalle(
                        rsTrabajos.getString("trabajo"),
                        "Trabajo/Actividad",
                        rsTrabajos.getDouble("nota"),
                        rsTrabajos.getTimestamp("fecha_carga"),
                        null
                );

                notasPorMateria.computeIfAbsent(materia, k -> new java.util.ArrayList<>()).add(nota);
                contadorTrabajos++;
                System.out.println("  ✅ Trabajo: " + materia + " - " + rsTrabajos.getString("trabajo") + " = " + rsTrabajos.getDouble("nota"));
            }
            rsTrabajos.close();
            psTrabajos.close();

            System.out.println("Notas de trabajos encontradas: " + contadorTrabajos);

            // 2. OBTENER NOTAS BIMESTRALES (tabla 'notas_bimestrales') - CORREGIDO
            String queryBimestrales = "SELECT "
                    + "m.nombre as materia, "
                    + "nb.periodo, "
                    + "nb.nota, "
                    + "nb.promedio_actividades, "
                    + "nb.estado, "
                    + "nb.fecha_carga "
                    + "FROM notas_bimestrales nb "
                    + "INNER JOIN materias m ON nb.materia_id = m.id "
                    + "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' AND "
                    + "(nb.alumno_id = ? OR nb.alumno_id = CAST(? AS CHAR)) " // CORREGIDO: usar DNI consistentemente
                    + "ORDER BY m.nombre, nb.periodo";

            PreparedStatement psBimestrales = conect.prepareStatement(queryBimestrales);
            psBimestrales.setInt(1, cursoId);
            psBimestrales.setString(2, alumnoDni);   // Usar DNI como string
            psBimestrales.setInt(3, alumnoId);       // También probar con ID como int
            ResultSet rsBimestrales = psBimestrales.executeQuery();

            int contadorBimestrales = 0;
            while (rsBimestrales.next()) {
                String materia = rsBimestrales.getString("materia");
                String periodo = rsBimestrales.getString("periodo");
                double nota = rsBimestrales.getDouble("nota");
                double promedio = rsBimestrales.getDouble("promedio_actividades");
                String estado = rsBimestrales.getString("estado");

                String descripcion = String.format("Nota Bimestral - %s (Promedio actividades: %.2f, Estado: %s)",
                        periodo, promedio, estado);

                NotaDetalle notaBimestral = new NotaDetalle(
                        "BIMESTRAL: " + periodo,
                        descripcion,
                        nota,
                        rsBimestrales.getTimestamp("fecha_carga"),
                        "Estado: " + estado
                );

                notasPorMateria.computeIfAbsent(materia, k -> new java.util.ArrayList<>()).add(notaBimestral);
                contadorBimestrales++;
                System.out.println("  ✅ Bimestral: " + materia + " - " + periodo + " = " + nota);
            }
            rsBimestrales.close();
            psBimestrales.close();

            System.out.println("Notas bimestrales encontradas: " + contadorBimestrales);

            // 3. SI NO HAY NOTAS, HACER DIAGNÓSTICO DETALLADO
            if (notasPorMateria.isEmpty()) {
                System.out.println("⚠️ NO SE ENCONTRARON NOTAS - Ejecutando diagnóstico...");
                diagnosticarAlumnoSinNotas(alumnoId, alumnoDni, cursoId);
            }

            System.out.println("Total de materias con notas: " + notasPorMateria.size());
            for (java.util.Map.Entry<String, java.util.List<NotaDetalle>> entry : notasPorMateria.entrySet()) {
                System.out.println("  - " + entry.getKey() + ": " + entry.getValue().size() + " notas");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener notas por materia: " + e.getMessage());
            e.printStackTrace();
        }

        return notasPorMateria;
    }

    /**
     * MÉTODO DE DIAGNÓSTICO ADICIONAL Agrega este método también a la clase
     * NotasVisualizationPanel
     */
    private void diagnosticarAlumnoSinNotas(int alumnoId, String alumnoDni, int cursoId) {
        try {
            System.out.println("=== DIAGNÓSTICO DETALLADO ===");
            System.out.println("Alumno ID: " + alumnoId + ", DNI: " + alumnoDni + ", Curso ID: " + cursoId);

            // 1. Verificar datos básicos del alumno
            String queryAlumno = "SELECT * FROM usuarios WHERE id = ?";
            PreparedStatement psAlumno = conect.prepareStatement(queryAlumno);
            psAlumno.setInt(1, alumnoId);
            ResultSet rsAlumno = psAlumno.executeQuery();
            if (rsAlumno.next()) {
                System.out.println("✓ Alumno encontrado: " + rsAlumno.getString("nombre") + " " + rsAlumno.getString("apellido"));
                System.out.println("  - DNI en BD: " + rsAlumno.getString("dni"));
                System.out.println("  - Rol: " + rsAlumno.getString("rol"));
            } else {
                System.out.println("✗ Alumno NO encontrado en tabla usuarios");
            }
            rsAlumno.close();
            psAlumno.close();

            // 2. Verificar inscripción en el curso
            String queryInscripcion = "SELECT * FROM alumno_curso WHERE alumno_id = ? AND curso_id = ?";
            PreparedStatement psInscripcion = conect.prepareStatement(queryInscripcion);
            psInscripcion.setInt(1, alumnoId);
            psInscripcion.setInt(2, cursoId);
            ResultSet rsInscripcion = psInscripcion.executeQuery();
            if (rsInscripcion.next()) {
                System.out.println("✓ Alumno inscrito en el curso - Estado: " + rsInscripcion.getString("estado"));
            } else {
                System.out.println("✗ Alumno NO inscrito en el curso");
            }
            rsInscripcion.close();
            psInscripcion.close();

            // 3. Verificar notas en tabla 'notas' con diferentes variaciones del alumno_id
            System.out.println("--- Verificando tabla 'notas' ---");
            String[] variacionesId = {String.valueOf(alumnoId), alumnoDni, "0" + alumnoId};

            for (String variacion : variacionesId) {
                if (variacion == null) {
                    continue;
                }

                String queryNotas = "SELECT COUNT(*) as total FROM notas WHERE alumno_id = ?";
                PreparedStatement psNotas = conect.prepareStatement(queryNotas);
                psNotas.setString(1, variacion);
                ResultSet rsNotas = psNotas.executeQuery();
                if (rsNotas.next()) {
                    int count = rsNotas.getInt("total");
                    System.out.println("  Variación '" + variacion + "': " + count + " notas encontradas");
                }
                rsNotas.close();
                psNotas.close();
            }

            // 4. Verificar notas bimestrales
            System.out.println("--- Verificando tabla 'notas_bimestrales' ---");
            for (String variacion : variacionesId) {
                if (variacion == null) {
                    continue;
                }

                String queryBimestrales = "SELECT COUNT(*) as total FROM notas_bimestrales WHERE alumno_id = ?";
                PreparedStatement psBimestrales = conect.prepareStatement(queryBimestrales);
                psBimestrales.setString(1, variacion);
                ResultSet rsBimestrales = psBimestrales.executeQuery();
                if (rsBimestrales.next()) {
                    int count = rsBimestrales.getInt("total");
                    System.out.println("  Variación '" + variacion + "': " + count + " notas bimestrales encontradas");
                }
                rsBimestrales.close();
                psBimestrales.close();
            }

            // 5. Mostrar algunas notas de ejemplo para ver el formato
            System.out.println("--- Ejemplos de alumno_id en tabla 'notas' ---");
            String queryEjemplos = "SELECT DISTINCT alumno_id FROM notas LIMIT 5";
            PreparedStatement psEjemplos = conect.prepareStatement(queryEjemplos);
            ResultSet rsEjemplos = psEjemplos.executeQuery();
            while (rsEjemplos.next()) {
                System.out.println("  Ejemplo alumno_id: '" + rsEjemplos.getString("alumno_id") + "'");
            }
            rsEjemplos.close();
            psEjemplos.close();

            System.out.println("=== FIN DIAGNÓSTICO ===");

        } catch (SQLException e) {
            System.err.println("Error en diagnóstico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea el modelo de tabla para las notas de un alumno. VERSIÓN SIMPLIFICADA
     * para evitar campos que pueden no existir.
     */
    private javax.swing.table.DefaultTableModel crearModeloNotasAlumno(
            java.util.Map<String, java.util.List<NotaDetalle>> notasPorMateria,
            AlumnoCompleto alumno) {

        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel();

        // Configurar columnas
        modelo.addColumn("Materia");
        modelo.addColumn("Tipo/Actividad");
        modelo.addColumn("Nota");
        modelo.addColumn("Observaciones");
        modelo.addColumn("Fecha");

        // Agregar información del alumno
        modelo.addRow(new Object[]{
            "=== INFORMACIÓN DEL ALUMNO ===",
            "",
            "",
            "",
            ""
        });

        modelo.addRow(new Object[]{
            "Nombre:",
            alumno.nombreCompleto,
            "",
            "",
            ""
        });

        modelo.addRow(new Object[]{
            "DNI:",
            alumno.dni != null ? alumno.dni : "Sin DNI",
            "",
            "",
            ""
        });

        modelo.addRow(new Object[]{
            "Fecha de exportación:",
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            "",
            "",
            ""
        });

        // Agregar fila vacía de separación
        modelo.addRow(new Object[]{"", "", "", "", ""});

        if (notasPorMateria.isEmpty()) {
            modelo.addRow(new Object[]{
                "SIN NOTAS",
                "Este alumno no tiene notas registradas",
                "",
                "",
                ""
            });
            return modelo;
        }

        // Procesar cada materia
        for (java.util.Map.Entry<String, java.util.List<NotaDetalle>> entry : notasPorMateria.entrySet()) {
            String materia = entry.getKey();
            java.util.List<NotaDetalle> notas = entry.getValue();

            // Agregar encabezado de materia
            modelo.addRow(new Object[]{
                ">>> " + materia.toUpperCase() + " <<<",
                "",
                "",
                "",
                ""
            });

            // Calcular estadísticas
            double sumaNotas = 0;
            int cantidadNotas = 0;
            java.util.List<NotaDetalle> notasTrabajos = new java.util.ArrayList<>();
            java.util.List<NotaDetalle> notasBimestrales = new java.util.ArrayList<>();

            // Separar notas por tipo
            for (NotaDetalle nota : notas) {
                if (nota.trabajo.startsWith("BIMESTRAL:")) {
                    notasBimestrales.add(nota);
                } else {
                    notasTrabajos.add(nota);
                    sumaNotas += nota.nota;
                    cantidadNotas++;
                }
            }

            // Agregar notas de trabajos
            if (!notasTrabajos.isEmpty()) {
                modelo.addRow(new Object[]{
                    "",
                    "--- TRABAJOS Y ACTIVIDADES ---",
                    "",
                    "",
                    ""
                });

                for (NotaDetalle nota : notasTrabajos) {
                    modelo.addRow(new Object[]{
                        "", // Materia vacía para notas individuales
                        nota.trabajo,
                        nota.nota,
                        nota.descripcion != null ? nota.descripcion : "",
                        nota.fecha != null ? nota.fecha.toString() : ""
                    });
                }

                // Agregar promedio de trabajos si hay notas
                if (cantidadNotas > 0) {
                    double promedio = sumaNotas / cantidadNotas;
                    modelo.addRow(new Object[]{
                        "",
                        "PROMEDIO DE TRABAJOS:",
                        String.format("%.2f", promedio),
                        "Calculado con " + cantidadNotas + " trabajos",
                        ""
                    });
                }
            }

            // Agregar notas bimestrales
            if (!notasBimestrales.isEmpty()) {
                modelo.addRow(new Object[]{
                    "",
                    "--- NOTAS BIMESTRALES ---",
                    "",
                    "",
                    ""
                });

                for (NotaDetalle nota : notasBimestrales) {
                    modelo.addRow(new Object[]{
                        "",
                        nota.trabajo.replace("BIMESTRAL: ", ""),
                        nota.nota,
                        nota.observaciones != null ? nota.observaciones : "",
                        nota.fecha != null ? nota.fecha.toString() : ""
                    });
                }
            }

            // Agregar fila vacía de separación entre materias
            modelo.addRow(new Object[]{"", "", "", "", ""});
        }

        return modelo;
    }

    /**
     * Configura una tabla para exportación.
     */
    private void configurarTablaExportacion(javax.swing.JTable tabla) {
        tabla.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setRowHeight(25);

        if (tabla.getTableHeader() != null) {
            tabla.getTableHeader().setReorderingAllowed(false);
        }
    }

    /**
     * Genera el nombre de archivo para un alumno.
     */
    private String generarNombreArchivo(AlumnoCompleto alumno, String curso) {
        String apellidoLimpio = alumno.apellido.replaceAll("[^a-zA-Z0-9]", "_");
        String cursoLimpio = curso.replace("°", "").replace(" ", "_");
        String fecha = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return "Notas_" + apellidoLimpio + "_" + cursoLimpio + "_" + fecha;
    }

    /**
     * Crea un archivo indicando que el alumno no tiene notas.
     */
    private boolean crearArchivoSinNotas(AlumnoCompleto alumno, String curso) {
        try {
            javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel();
            modelo.addColumn("Información");
            modelo.addColumn("Detalle");
            modelo.addColumn("Estado");
            modelo.addColumn("Observaciones");

            modelo.addRow(new Object[]{"=== REPORTE SIN NOTAS ===", "", "", ""});
            modelo.addRow(new Object[]{"", "", "", ""});
            modelo.addRow(new Object[]{"Alumno:", alumno.nombreCompleto, "", ""});
            modelo.addRow(new Object[]{"DNI:", alumno.dni != null ? alumno.dni : "Sin DNI", "", ""});
            modelo.addRow(new Object[]{"Curso:", curso, "", ""});
            modelo.addRow(new Object[]{"Fecha generación:", java.time.LocalDate.now().toString(), "", ""});
            modelo.addRow(new Object[]{"", "", "", ""});
            modelo.addRow(new Object[]{"Estado:", "SIN NOTAS REGISTRADAS", "❌", ""});
            modelo.addRow(new Object[]{"", "", "", ""});
            modelo.addRow(new Object[]{"Verificado en:", "Tabla 'notas' (trabajos)", "✓", ""});
            modelo.addRow(new Object[]{"Verificado en:", "Tabla 'notas_bimestrales'", "✓", ""});
            modelo.addRow(new Object[]{"", "", "", ""});
            modelo.addRow(new Object[]{"Observaciones:", "El alumno no tiene notas registradas", "", ""});
            modelo.addRow(new Object[]{"", "en ninguna materia del curso.", "", ""});
            modelo.addRow(new Object[]{"", "Se verificaron tanto trabajos individuales", "", ""});
            modelo.addRow(new Object[]{"", "como notas bimestrales.", "", ""});

            javax.swing.JTable tabla = new javax.swing.JTable(modelo);

            String nombreArchivo = generarNombreArchivo(alumno, curso) + "_SIN_NOTAS";
            String titulo = "Sin Notas - " + alumno.nombreCompleto + " - Curso " + curso;

            return main.java.utils.ExcelExportUtility.exportarTablaAExcel(tabla, titulo, nombreArchivo);

        } catch (Exception e) {
            System.err.println("Error al crear archivo sin notas: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporta notas de todas las materias del curso.
     */
    private int exportarTodasLasMaterias(int cursoId, String curso) {
        int exportadas = 0;
        try {
            // Obtener todas las materias del curso
            String query = "SELECT DISTINCT m.id, m.nombre "
                    + "FROM materias m INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY m.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String materia = rs.getString("nombre");

                // Simular selección de materia y generar reporte
                String materiaOriginal = (String) comboMaterias.getSelectedItem();
                comboMaterias.setSelectedItem(materia);

                mostrarNotasPorMateria();

                if (tablaNotas.getModel().getRowCount() > 0) {
                    String titulo = "Notas por Materia - " + curso + " - " + materia;
                    String nombreArchivo = "Notas_" + curso.replace("°", "") + "_" + materia.replace(" ", "_");

                    if (main.java.utils.ExcelExportUtility.exportarTablaAExcel(tablaNotas, titulo, nombreArchivo)) {
                        exportadas++;
                    }
                }

                // Restaurar selección original
                comboMaterias.setSelectedItem(materiaOriginal);
            }

        } catch (SQLException e) {
            System.err.println("Error al exportar todas las materias: " + e.getMessage());
        }

        return exportadas;
    }

    /**
     * Exporta el reporte bimestral general.
     */
    private void exportarReporteBimestralGeneral(int cursoId, String curso, String periodo) {
        // Cambiar temporalmente a modo reporte bimestral
        boolean originalPorMateria = rbPorMateria.isSelected();
        boolean originalPorAlumno = rbPorAlumno.isSelected();
        boolean originalReporte = rbReporteBimestral.isSelected();

        rbReporteBimestral.setSelected(true);
        String periodoOriginal = (String) comboPeriodos.getSelectedItem();
        comboPeriodos.setSelectedItem(periodo);

        mostrarReporteBimestralGeneral(cursoId, periodo);

        if (tablaNotas.getModel().getRowCount() > 0) {
            String titulo = "Reporte Bimestral General - " + curso + " - " + periodo;
            String nombreArchivo = "Reporte_" + curso.replace("°", "") + "_" + periodo;
            main.java.utils.ExcelExportUtility.exportarTablaAExcel(tablaNotas, titulo, nombreArchivo);
        }

        // Restaurar estado original
        rbPorMateria.setSelected(originalPorMateria);
        rbPorAlumno.setSelected(originalPorAlumno);
        rbReporteBimestral.setSelected(originalReporte);
        comboPeriodos.setSelectedItem(periodoOriginal);
    }

    /**
     * Exporta reportes bimestrales por cada materia.
     */
    private int exportarReportesPorMaterias(int cursoId, String curso, String periodo) {
        int exportadas = 0;
        try {
            // Obtener todas las materias del curso
            String query = "SELECT DISTINCT m.id, m.nombre "
                    + "FROM materias m INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY m.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String materia = rs.getString("nombre");

                // Generar reporte bimestral por materia
                mostrarReporteBimestralPorMateria(cursoId, periodo, materia);

                if (tablaNotas.getModel().getRowCount() > 0) {
                    String titulo = "Reporte Bimestral por Materia - " + curso + " - " + materia + " - " + periodo;
                    String nombreArchivo = "Reporte_" + curso.replace("°", "") + "_" + materia.replace(" ", "_") + "_" + periodo;

                    if (main.java.utils.ExcelExportUtility.exportarTablaAExcel(tablaNotas, titulo, nombreArchivo)) {
                        exportadas++;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al exportar reportes por materias: " + e.getMessage());
        }

        return exportadas;
    }

    // CLASES DE APOYO
    /**
     * Clase para representar un alumno completo.
     */
    private static class AlumnoCompleto {

        final int id;
        final String nombre;
        final String apellido;
        final String dni;
        final String nombreCompleto;

        public AlumnoCompleto(int id, String nombre, String apellido, String dni) {
            this.id = id;
            this.nombre = nombre != null ? nombre : "";
            this.apellido = apellido != null ? apellido : "";
            this.dni = dni;
            this.nombreCompleto = this.apellido + ", " + this.nombre;
        }
    }

    /**
     * Clase para representar una nota con detalles.
     */
    private static class NotaDetalle {

        final String trabajo;
        final String descripcion;
        final double nota;
        final java.sql.Timestamp fecha;
        final String observaciones;

        public NotaDetalle(String trabajo, String descripcion, double nota,
                java.sql.Timestamp fecha, String observaciones) {
            this.trabajo = trabajo != null ? trabajo : "";
            this.descripcion = descripcion;
            this.nota = nota;
            this.fecha = fecha;
            this.observaciones = observaciones;
        }
    }

    /**
     * Método para mapear los períodos del ComboBox a los de la base de datos.
     * Agregar este método en NotasVisualizationPanel.java
     */
    private String mapearPeriodoParaBD(String periodoComboBox) {
        // Mapear los períodos del ComboBox a los que están en la BD
        switch (periodoComboBox) {
            case "1B":
                return "1er Bimestre";
            case "2B":
                return "2do Bimestre";
            case "3B":
                return "3er Bimestre";
            case "4B":
                return "4to Bimestre";
            case "1C":
                return "1er Cuatrimestre";
            case "2C":
                return "2do Cuatrimestre";
            case "Final":
                return "Final";
            default:
                // Si no encuentra mapeo, retornar tal como está
                System.out.println("ADVERTENCIA: Período no mapeado: " + periodoComboBox);
                return periodoComboBox;
        }
    }

    /**
     * Método para obtener los períodos reales de la base de datos. Reemplazar
     * el combo estático por uno dinámico.
     */
    private void cargarPeríodosReales() {
        try {
            // Limpiar combo actual
            comboPeriodos.removeAllItems();

            // Obtener períodos únicos de la base de datos
            String query = "SELECT DISTINCT periodo FROM notas_bimestrales ORDER BY periodo";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            System.out.println("Períodos encontrados en la BD:");
            while (rs.next()) {
                String periodo = rs.getString("periodo");
                comboPeriodos.addItem(periodo);
                System.out.println("  - " + periodo);
            }

            rs.close();
            ps.close();

            // Si no hay períodos en la BD, agregar los valores por defecto
            if (comboPeriodos.getItemCount() == 0) {
                System.out.println("No se encontraron períodos en la BD, usando valores por defecto");
                comboPeriodos.addItem("1B");
                comboPeriodos.addItem("2B");
                comboPeriodos.addItem("3B");
                comboPeriodos.addItem("4B");
                comboPeriodos.addItem("1C");
                comboPeriodos.addItem("2C");
                comboPeriodos.addItem("Final");
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar períodos: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, usar valores por defecto
            comboPeriodos.removeAllItems();
            comboPeriodos.addItem("1B");
            comboPeriodos.addItem("2B");
            comboPeriodos.addItem("3B");
            comboPeriodos.addItem("4B");
            comboPeriodos.addItem("1C");
            comboPeriodos.addItem("2C");
            comboPeriodos.addItem("Final");
        }
    }

    /**
     * Muestra el menú de opciones para generar boletines
     */
    private void mostrarMenuBoletines() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        if (cursoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un curso primero",
                    "Curso requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer cursoId = cursosMap.get(cursoSeleccionado);
        if (cursoId == null) {
            JOptionPane.showMessageDialog(this,
                    "Error: No se encontró el ID del curso seleccionado",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mostrar opciones
        String[] opciones = {
            "Generar Boletín Individual",
            "Generar Boletines de Todo el Curso",
            "Cancelar"
        };

        int seleccion = JOptionPane.showOptionDialog(this,
                "Seleccione una opción para generar boletines:",
                "Generar Boletines - Curso " + cursoSeleccionado,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        switch (seleccion) {
            case 0: // Boletín individual
                generarBoletinIndividual(cursoId);
                break;
            case 1: // Boletines de todo el curso
                generarBoletinesTodoCurso(cursoId);
                break;
            case 2: // Cancelar
            default:
                break;
        }
    }

    /**
     * Genera un boletín individual para un alumno específico
     */
    private void generarBoletinIndividual(int cursoId) {
        try {
            // Cargar alumnos del curso
            cargarAlumnos(cursoId);

            if (alumnosMap.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron alumnos en el curso seleccionado",
                        "Sin alumnos",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Seleccionar alumno
            String[] alumnosArray = alumnosMap.keySet().toArray(new String[0]);
            String alumnoSeleccionado = (String) JOptionPane.showInputDialog(this,
                    "Seleccione el alumno:",
                    "Seleccionar Alumno",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    alumnosArray,
                    alumnosArray[0]);

            if (alumnoSeleccionado != null) {
                Integer alumnoId = alumnosMap.get(alumnoSeleccionado);
                if (alumnoId != null) {
                    // Usar la utilidad de boletines
                    main.java.utils.BoletinExportUtility.exportarBoletinIndividualConInterfaz(
                            alumnoId, cursoId, this);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al generar boletín individual: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar boletín individual: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera boletines para todos los alumnos del curso
     */
    private void generarBoletinesTodoCurso(int cursoId) {
        try {
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea generar boletines para todos los alumnos del curso?\n"
                    + "Esto puede tomar varios minutos dependiendo de la cantidad de alumnos.",
                    "Confirmar Generación Masiva",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                // Usar la utilidad de boletines
                main.java.utils.BoletinExportUtility.exportarBoletinesConInterfaz(cursoId, this);
            }

        } catch (Exception e) {
            System.err.println("Error al generar boletines del curso: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar boletines del curso: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra estadísticas del curso antes de generar boletines
     */
    private void mostrarEstadisticasCurso(int cursoId) {
        try {
            StringBuilder estadisticas = new StringBuilder();
            estadisticas.append("=== ESTADÍSTICAS DEL CURSO ===\n\n");

            // Contar alumnos
            String queryAlumnos = "SELECT COUNT(*) as total FROM alumno_curso WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();
            if (rsAlumnos.next()) {
                estadisticas.append("👥 Total de alumnos: ").append(rsAlumnos.getInt("total")).append("\n");
            }

            // Contar materias
            String queryMaterias = "SELECT COUNT(*) as total FROM profesor_curso_materia WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            psMaterias.setInt(1, cursoId);
            ResultSet rsMaterias = psMaterias.executeQuery();
            if (rsMaterias.next()) {
                estadisticas.append("📚 Total de materias: ").append(rsMaterias.getInt("total")).append("\n");
            }

            // Contar notas bimestrales
            String queryNotasBim = "SELECT COUNT(*) as total FROM notas_bimestrales nb "
                    + "INNER JOIN materias m ON nb.materia_id = m.id "
                    + "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo'";
            PreparedStatement psNotasBim = conect.prepareStatement(queryNotasBim);
            psNotasBim.setInt(1, cursoId);
            ResultSet rsNotasBim = psNotasBim.executeQuery();
            if (rsNotasBim.next()) {
                estadisticas.append("📊 Notas bimestrales: ").append(rsNotasBim.getInt("total")).append("\n");
            }

            // Contar notas de trabajos
            String queryNotasTrab = "SELECT COUNT(*) as total FROM notas n "
                    + "INNER JOIN trabajos t ON n.trabajo_id = t.id "
                    + "INNER JOIN materias m ON t.materia_id = m.id "
                    + "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ? AND pcm.estado = 'activo'";
            PreparedStatement psNotasTrab = conect.prepareStatement(queryNotasTrab);
            psNotasTrab.setInt(1, cursoId);
            ResultSet rsNotasTrab = psNotasTrab.executeQuery();
            if (rsNotasTrab.next()) {
                estadisticas.append("📝 Notas de trabajos: ").append(rsNotasTrab.getInt("total")).append("\n");
            }

            estadisticas.append("\n¿Desea continuar con la generación de boletines?");

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    estadisticas.toString(),
                    "Estadísticas del Curso",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            // Si el usuario confirma, no hacer nada adicional aquí
            // El método que llama a este manejará la continuación
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un boletín individual CON INTEGRACIÓN AL SERVIDOR
     */
    private void generarBoletinIndividualConServidor(int cursoId) {
        try {
            // Cargar alumnos del curso
            cargarAlumnos(cursoId);

            if (alumnosMap.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron alumnos en el curso seleccionado",
                        "Sin alumnos",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Seleccionar alumno
            String[] alumnosArray = alumnosMap.keySet().toArray(new String[0]);
            String alumnoSeleccionado = (String) JOptionPane.showInputDialog(this,
                    "Seleccione el alumno:",
                    "Seleccionar Alumno",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    alumnosArray,
                    alumnosArray[0]);

            if (alumnoSeleccionado == null) {
                return; // Usuario canceló
            }
            // Seleccionar período
            String[] periodos = {"1B", "2B", "3B", "4B", "1C", "2C", "Final"};
            String periodoSeleccionado = (String) JOptionPane.showInputDialog(this,
                    "Seleccione el período del boletín:",
                    "Seleccionar Período",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    periodos,
                    main.java.utils.BoletinesUtils.obtenerPeriodoActual());

            if (periodoSeleccionado == null) {
                return; // Usuario canceló
            }
            Integer alumnoId = alumnosMap.get(alumnoSeleccionado);
            if (alumnoId != null) {
                // USAR LA NUEVA IMPLEMENTACIÓN CON SERVIDOR
                main.java.utils.PlantillaBoletinUtility.generarBoletinIndividualConServidorConInterfaz(
                        alumnoId, cursoId, periodoSeleccionado, this);
            }

        } catch (Exception e) {
            System.err.println("Error al generar boletín individual: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar boletín individual: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera boletines para todos los alumnos del curso CON INTEGRACIÓN AL
     * SERVIDOR
     */
    private void generarBoletinesTodoCursoConServidor(int cursoId) {
        try {
            // Seleccionar período
            String[] periodos = {"1B", "2B", "3B", "4B", "1C", "2C", "Final"};
            String periodoSeleccionado = (String) JOptionPane.showInputDialog(this,
                    "Seleccione el período del boletín:",
                    "Seleccionar Período",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    periodos,
                    main.java.utils.BoletinesUtils.obtenerPeriodoActual());

            if (periodoSeleccionado == null) {
                return; // Usuario canceló
            }
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de generar boletines para todos los alumnos del curso?\n"
                    + "Período: " + periodoSeleccionado + "\n"
                    + "Los boletines se guardarán automáticamente en el servidor.\n"
                    + "Esta operación puede tomar varios minutos.",
                    "Confirmar Generación Masiva",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                // USAR LA NUEVA IMPLEMENTACIÓN CON SERVIDOR
                main.java.utils.PlantillaBoletinUtility.generarBoletinesCursoConServidorConInterfaz(
                        cursoId, periodoSeleccionado, this);
            }

        } catch (Exception e) {
            System.err.println("Error al generar boletines del curso: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar boletines del curso: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre el panel de gestión de boletines existentes
     */
    private void abrirGestionBoletines() {
        try {
            // Crear y mostrar el panel de gestión de boletines
            main.java.views.users.common.PanelGestionBoletines panelGestion
                    = new main.java.views.users.common.PanelGestionBoletines(ventana, userId, userRol);

            // Obtener panel principal y configurarlo
            javax.swing.JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new java.awt.BorderLayout());
            panelPrincipal.add(panelGestion, java.awt.BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

            System.out.println("Panel de gestión de boletines abierto desde NotasVisualizationPanel");

        } catch (Exception e) {
            System.err.println("Error al abrir gestión de boletines: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al abrir gestión de boletines: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Configurar el servidor de boletines
     */
    private void configurarServidorBoletines() {
        try {
            // Solo administradores pueden configurar el servidor
            if (userRol != 1) {
                JOptionPane.showMessageDialog(this,
                        "Solo los administradores pueden configurar el servidor de boletines",
                        "Permisos insuficientes",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            mostrarDialogoConfiguracionServidor();

        } catch (Exception e) {
            System.err.println("Error al configurar servidor: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al configurar servidor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra el diálogo de configuración del servidor de boletines
     */
    private void mostrarDialogoConfiguracionServidor() {
        JPanel panel = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        // Información actual
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("=== CONFIGURACIÓN ACTUAL ==="), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Servidor de boletines:"), gbc);

        JTextField txtRutaActual = new JTextField(main.java.utils.GestorBoletines.obtenerRutaServidor(), 30);
        txtRutaActual.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtRutaActual, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Plantilla de boletines:"), gbc);

        JTextField txtPlantillaActual = new JTextField(main.java.utils.PlantillaBoletinUtility.obtenerRutaPlantilla(), 30);
        txtPlantillaActual.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtPlantillaActual, gbc);

        // Configuración nueva
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(new JLabel(" "), gbc); // Espaciador

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(new JLabel("=== NUEVA CONFIGURACIÓN ==="), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Nueva ruta del servidor:"), gbc);

        JTextField txtNuevaRuta = new JTextField(30);
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(txtNuevaRuta, gbc);

        JButton btnExaminarServidor = new JButton("Examinar...");
        gbc.gridx = 2;
        gbc.gridy = 5;
        panel.add(btnExaminarServidor, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Nueva plantilla:"), gbc);

        JTextField txtNuevaPlantilla = new JTextField(30);
        gbc.gridx = 1;
        gbc.gridy = 6;
        panel.add(txtNuevaPlantilla, gbc);

        JButton btnExaminarPlantilla = new JButton("Examinar...");
        gbc.gridx = 2;
        gbc.gridy = 6;
        panel.add(btnExaminarPlantilla, gbc);

        // Listeners para examinar
        btnExaminarServidor.addActionListener(evt -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Seleccionar carpeta del servidor de boletines");

            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                txtNuevaRuta.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnExaminarPlantilla.addActionListener(evt -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
            fileChooser.setDialogTitle("Seleccionar plantilla de boletines");

            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                txtNuevaPlantilla.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Mostrar diálogo
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Configuración del Sistema de Boletines",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Aplicar cambios
            boolean cambios = false;

            // Cambiar ruta del servidor si se especificó
            String nuevaRuta = txtNuevaRuta.getText().trim();
            if (!nuevaRuta.isEmpty()) {
                main.java.utils.GestorBoletines.configurarRutaServidor(nuevaRuta);
                cambios = true;
                System.out.println("✅ Ruta del servidor actualizada: " + nuevaRuta);
            }

            // Cambiar plantilla si se especificó
            String nuevaPlantilla = txtNuevaPlantilla.getText().trim();
            if (!nuevaPlantilla.isEmpty()) {
                main.java.utils.PlantillaBoletinUtility.configurarRutaPlantilla(nuevaPlantilla);
                cambios = true;
                System.out.println("✅ Plantilla actualizada: " + nuevaPlantilla);
            }

            if (cambios) {
                // Preguntar si generar estructura
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Desea generar la estructura de carpetas para el año actual?\n"
                        + "Esto creará las carpetas necesarias para organizar los boletines.",
                        "Generar Estructura",
                        JOptionPane.YES_NO_OPTION);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    try {
                        boolean exito = main.java.utils.GestorBoletines.generarEstructuraCompleta(
                                java.time.LocalDate.now().getYear());

                        String mensaje = exito
                                ? "Configuración actualizada y estructura generada exitosamente"
                                : "Configuración actualizada, pero hubo problemas generando la estructura";

                        JOptionPane.showMessageDialog(this,
                                mensaje,
                                "Configuración Completada",
                                exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "Error al generar estructura: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Configuración actualizada exitosamente",
                            "Configuración Completada",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se realizaron cambios",
                        "Sin cambios",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

// TAMBIÉN AGREGAR este método para completar la funcionalidad
    /**
     * Valida que el curso tenga datos suficientes para generar boletines
     */
    private boolean validarDatosParaBoletines(int cursoId) {
        try {
            // Verificar que hay alumnos
            String queryAlumnos = "SELECT COUNT(*) as total FROM alumno_curso WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            int totalAlumnos = 0;
            if (rsAlumnos.next()) {
                totalAlumnos = rsAlumnos.getInt("total");
            }

            if (totalAlumnos == 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay alumnos activos en el curso seleccionado",
                        "Sin alumnos",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Verificar que hay materias
            String queryMaterias = "SELECT COUNT(*) as total FROM profesor_curso_materia WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            psMaterias.setInt(1, cursoId);
            ResultSet rsMaterias = psMaterias.executeQuery();

            int totalMaterias = 0;
            if (rsMaterias.next()) {
                totalMaterias = rsMaterias.getInt("total");
            }

            if (totalMaterias == 0) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "No se encontraron materias activas en el curso.\n"
                        + "¿Desea continuar con la generación de boletines vacíos?",
                        "Sin materias",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                return confirmacion == JOptionPane.YES_OPTION;
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error al validar datos para boletines: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al validar datos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
