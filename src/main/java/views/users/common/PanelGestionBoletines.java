package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import main.java.database.Conexion;
import main.java.utils.GestorBoletines;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import main.java.utils.EmailBoletinUtility;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import main.java.utils.GoogleEmailIntegration;

/**
 * Panel de Gestión de Boletines CORREGIDO Y COMPLETO Versión final sin
 * redundancias y con identificación correcta de alumnos
 */
public class PanelGestionBoletines extends JPanel {

    private final VentanaInicio ventana;
    private final int userId;
    private final int userRol;
    private Connection conect;

    // Componentes de filtros
    private JComboBox<String> comboAnioLectivo;
    private JComboBox<String> comboAnio;
    private JComboBox<String> comboDivision;
    private JComboBox<String> comboPeriodo;
    private JTextField txtBuscarAlumno;

    // Componentes de acción
    private JButton btnBuscar;
    private JButton btnAbrir;
    private JButton btnDescargar;
    private JButton btnEnviarEmail;
    private JButton btnConfigEmail;
    private JButton btnEliminar;
    private JButton btnEstadisticas;
    private JButton btnActualizar;
    private JButton btnVolver;

    // Tabla de resultados
    private JTable tablaBoletines;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;

    // Datos y configuración
    private List<BoletinInfo> boletinesActuales;
    private Map<String, Integer> cursosDisponibles = new HashMap<>();

    // Configuración de email
    private String emailUsuario = "";
    private String passwordEmail = "";
    private String servidorSMTP = "smtp.gmail.com";
    private String puertoSMTP = "587";

    /**
     * Estructura para información de boletín PDF
     */
    public static class BoletinInfo {

        public String alumnoNombre;
        public String alumnoDni;
        public String alumnoEmail;
        public String curso;
        public String division;
        public String periodo;
        public String nombreArchivo;
        public String urlServidor;
        public long tamanioArchivo;
        public boolean esAccesible;
        public int cursoId;

        public BoletinInfo() {
            this.esAccesible = false;
            this.tamanioArchivo = 0;
            this.cursoId = -1;
        }
    }

    /**
     * Información de alumno con datos completos
     */
    public static class InfoAlumno {

        public int id;
        public String nombreCompleto;
        public String dni;
        public String email;
        public String curso;
        public String division;
        public int cursoId;

        public InfoAlumno(int id, String nombreCompleto, String dni, String email,
                String curso, String division, int cursoId) {
            this.id = id;
            this.nombreCompleto = nombreCompleto != null ? nombreCompleto : "";
            this.dni = dni != null ? dni : "Sin DNI";
            this.email = email != null ? email : "Sin Email";
            this.curso = curso != null ? curso : "";
            this.division = division != null ? division : "";
            this.cursoId = cursoId;
        }

        @Override
        public String toString() {
            return String.format("InfoAlumno{id=%d, nombre='%s', dni='%s', email='%s', curso='%s%s'}",
                    id, nombreCompleto, dni, email, curso, division);
        }
    }

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

        System.out.println("✅ PanelGestionBoletines CORREGIDO inicializado para usuario: " + userId);
    }

    private void initializeConnection() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this,
                    "Error de conexión a la base de datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponents() {
        // Combos de filtros
        comboAnioLectivo = new JComboBox<>();
        comboAnio = new JComboBox<>();
        comboDivision = new JComboBox<>();
        comboPeriodo = new JComboBox<>();
        txtBuscarAlumno = new JTextField(20);
        txtBuscarAlumno.setToolTipText("Buscar por nombre o apellido del alumno");

        // Cargar opciones iniciales
        cargarAniosDisponibles();
        cargarDivisionesDisponibles();
        cargarPeriodosDisponibles();
        cargarCursosDisponibles();

        // Botones de acción
        btnBuscar = createStyledButton("🔍 Buscar", new Color(33, 150, 243));
        btnAbrir = createStyledButton("📂 Abrir PDF", new Color(76, 175, 80));
        btnDescargar = createStyledButton("💾 Descargar", new Color(156, 39, 176));
        btnEnviarEmail = createStyledButton("📧 Enviar Email", new Color(255, 87, 34));
        btnConfigEmail = createStyledButton("⚙️ Config Email", new Color(121, 85, 72));
        btnEliminar = createStyledButton("🗑️ Eliminar", new Color(244, 67, 54));
        btnEstadisticas = createStyledButton("📊 Estadísticas", new Color(63, 81, 181));
        btnActualizar = createStyledButton("🔄 Actualizar", new Color(255, 152, 0));
        btnVolver = createStyledButton("← Volver", new Color(96, 125, 139));

        // Tabla de boletines
        String[] columnas = {
            "Seleccionar", "Alumno", "DNI", "Email", "Curso", "División",
            "Período", "Archivo PDF", "Tamaño", "Estado"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        tablaBoletines = new JTable(modeloTabla);
        tablaBoletines.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tablaBoletines.setRowHeight(30);
        tablaBoletines.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        configurarAnchoColumnas();
        scrollTabla = new JScrollPane(tablaBoletines);
        scrollTabla.setPreferredSize(new Dimension(1200, 400));
    }

    private void cargarCursosDisponibles() {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT id, anio, division FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            cursosDisponibles.clear();
            while (rs.next()) {
                int id = rs.getInt("id");
                int anio = rs.getInt("anio");
                int division = rs.getInt("division");
                String nombre = anio + "°" + division;
                cursosDisponibles.put(nombre, id);
            }

            rs.close();
            ps.close();
            System.out.println("✅ Cursos disponibles cargados: " + cursosDisponibles.size());

        } catch (SQLException e) {
            System.err.println("Error cargando cursos disponibles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarAniosDisponibles() {
        comboAnio.removeAllItems();
        comboAnio.addItem("TODOS");
        for (int i = 1; i <= 7; i++) {
            comboAnio.addItem(String.valueOf(i));
        }
    }

    private void cargarDivisionesDisponibles() {
        comboDivision.removeAllItems();
        comboDivision.addItem("TODAS");
        for (int i = 1; i <= 10; i++) {
            comboDivision.addItem(String.valueOf(i));
        }
    }

    private void cargarPeriodosDisponibles() {
        comboPeriodo.removeAllItems();
        comboPeriodo.addItem("TODOS");
        String[] periodos = {"1B", "2B", "3B", "4B", "1C", "2C", "Final", "Diciembre", "Febrero"};
        for (String periodo : periodos) {
            comboPeriodo.addItem(periodo);
        }
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 35));
        return button;
    }

    private void configurarAnchoColumnas() {
        int[] anchos = {80, 200, 100, 200, 60, 80, 100, 200, 80, 100};
        for (int i = 0; i < anchos.length && i < tablaBoletines.getColumnCount(); i++) {
            TableColumn columna = tablaBoletines.getColumnModel().getColumn(i);
            columna.setPreferredWidth(anchos[i]);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior - Título
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblTitulo = new JLabel("📋 Gestión de Boletines PDF - CORREGIDO");
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

    private JPanel createFiltrosPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("🔍 Filtros de Búsqueda"));
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
        panel.add(new JLabel("Año Curso:"), gbc);
        gbc.gridx = 3;
        panel.add(comboAnio, gbc);

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

    private JPanel createBotonesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));

        panel.add(btnBuscar);
        panel.add(btnAbrir);
        panel.add(btnDescargar);
        panel.add(btnEnviarEmail);

        if (userRol == 1 || userRol == 2) {
            panel.add(btnConfigEmail);
            panel.add(btnEliminar);
        }

        panel.add(btnEstadisticas);
        panel.add(btnActualizar);
        panel.add(btnVolver);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        JLabel lblInfo = new JLabel("💡 Búsqueda optimizada | Solo archivos PDF | Servidor: "
                + GestorBoletines.obtenerRutaServidor());
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);

        panel.add(lblInfo);
        return panel;
    }

    private void setupListeners() {
        btnBuscar.addActionListener(this::buscarBoletinesPDF);
        btnAbrir.addActionListener(this::abrirBoletinPDF);
        btnDescargar.addActionListener(this::descargarBoletines);
        btnEnviarEmail.addActionListener(this::enviarBoletinesPorEmail);
        btnConfigEmail.addActionListener(this::configurarEmail);
        btnEliminar.addActionListener(this::eliminarBoletin);
        btnEstadisticas.addActionListener(this::mostrarEstadisticas);
        btnActualizar.addActionListener(this::actualizarLista);
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());

        // Listeners para cambios en combos
        comboAnioLectivo.addActionListener(e -> buscarBoletinesPDF(null));
        comboAnio.addActionListener(e -> buscarBoletinesPDF(null));
        comboDivision.addActionListener(e -> buscarBoletinesPDF(null));

        // Doble clic en tabla para abrir PDF
        tablaBoletines.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirBoletinPDF(null);
                }
            }
        });

        // Enter en campo de búsqueda
        txtBuscarAlumno.addActionListener(this::buscarBoletinesPDF);
    }

    private void loadInitialData() {
        cargarAniosLectivos();
        buscarBoletinesPDF(null);
        verificarEstadoEmail();
    }

    private void cargarAniosLectivos() {
        try {
            comboAnioLectivo.removeAllItems();
            int anioActual = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                comboAnioLectivo.addItem(String.valueOf(anioActual - i));
            }
            comboAnioLectivo.setSelectedItem(String.valueOf(anioActual));
        } catch (Exception e) {
            System.err.println("Error cargando años lectivos: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRINCIPAL CORREGIDO: Búsqueda optimizada por curso
     */
    private void buscarBoletinesPDF(ActionEvent e) {
        SwingWorker<List<BoletinInfo>, Void> worker = new SwingWorker<List<BoletinInfo>, Void>() {
            @Override
            protected List<BoletinInfo> doInBackground() throws Exception {
                return buscarBoletinesEnServidorOptimizado();
            }

            @Override
            protected void done() {
                try {
                    boletinesActuales = get();
                    actualizarTablaBoletines();
                } catch (Exception ex) {
                    System.err.println("Error buscando boletines: " + ex.getMessage());
                    JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                            "Error al buscar boletines: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * MÉTODO CORREGIDO: Búsqueda optimizada por curso con DNI y Email
     */
    private List<BoletinInfo> buscarBoletinesEnServidorOptimizado() {
        List<BoletinInfo> boletines = new ArrayList<>();

        try {
            System.out.println("=== BÚSQUEDA OPTIMIZADA DE BOLETINES PDF ===");

            String anioLectivo = (String) comboAnioLectivo.getSelectedItem();
            String anioSeleccionado = (String) comboAnio.getSelectedItem();
            String divisionSeleccionada = (String) comboDivision.getSelectedItem();
            String periodoSeleccionado = (String) comboPeriodo.getSelectedItem();

            if (anioLectivo == null) {
                return boletines;
            }

            // PASO 1: Obtener alumnos del curso con DNI y Email
            Map<String, InfoAlumno> alumnosDelCurso = obtenerAlumnosDelCursoSeleccionado(
                    anioSeleccionado, divisionSeleccionada);

            if (alumnosDelCurso.isEmpty()) {
                System.out.println("⚠️ No se encontraron alumnos para el curso seleccionado");
                return boletines;
            }

            System.out.println("✅ Alumnos del curso cargados: " + alumnosDelCurso.size());

            // PASO 2: Construir rutas de búsqueda específicas
            List<String> rutasBusqueda = construirRutasBusquedaOptimizada(
                    anioLectivo, anioSeleccionado, divisionSeleccionada, periodoSeleccionado);

            System.out.println("📁 Rutas de búsqueda: " + rutasBusqueda.size());

            // PASO 3: Buscar archivos solo para alumnos del curso
            for (String rutaBusqueda : rutasBusqueda) {
                List<BoletinInfo> boletinesEnRuta = buscarPDFsEnRutaOptimizada(
                        rutaBusqueda, alumnosDelCurso);
                boletines.addAll(boletinesEnRuta);
            }

            // PASO 4: Filtrar por nombre de alumno si se especificó
            String filtroAlumno = txtBuscarAlumno.getText().toLowerCase().trim();
            if (!filtroAlumno.isEmpty()) {
                boletines = boletines.stream()
                        .filter(b -> b.alumnoNombre.toLowerCase().contains(filtroAlumno))
                        .collect(java.util.stream.Collectors.toList());
            }

            System.out.println("✅ Búsqueda optimizada completada: " + boletines.size() + " boletines encontrados");

        } catch (Exception e) {
            System.err.println("Error en búsqueda optimizada: " + e.getMessage());
            e.printStackTrace();
        }

        return boletines;
    }

    /**
     * MÉTODO CORREGIDO: Obtener alumnos con DNI y Email completos
     */
    private Map<String, InfoAlumno> obtenerAlumnosDelCursoSeleccionado(String anioSeleccionado, String divisionSeleccionada) {
        Map<String, InfoAlumno> alumnos = new HashMap<>();

        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            System.out.println("🔍 Obteniendo alumnos del curso específico: " + anioSeleccionado + "°" + divisionSeleccionada);

            // CONSULTA CORREGIDA CON MANEJO DE NULOS
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("""
                SELECT u.id, u.nombre, u.apellido, 
                       COALESCE(u.dni, 'Sin DNI') as dni,
                       COALESCE(u.mail, 'Sin Email') as mail,
                       c.id as curso_id, c.anio, c.division
                FROM usuarios u 
                INNER JOIN alumno_curso ac ON u.id = ac.alumno_id AND ac.estado = 'activo'
                INNER JOIN cursos c ON ac.curso_id = c.id
                WHERE u.rol = '4' AND u.status = 1
                """);

            List<Object> parametros = new ArrayList<>();

            // Filtrar por año si no es "TODOS"
            if (!"TODOS".equals(anioSeleccionado)) {
                queryBuilder.append(" AND c.anio = ?");
                parametros.add(Integer.parseInt(anioSeleccionado));
            }

            // Filtrar por división si no es "TODAS"
            if (!"TODAS".equals(divisionSeleccionada)) {
                queryBuilder.append(" AND c.division = ?");
                parametros.add(Integer.parseInt(divisionSeleccionada));
            }

            queryBuilder.append(" ORDER BY u.apellido, u.nombre");

            PreparedStatement ps = conect.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            System.out.println("📋 Consulta corregida: " + queryBuilder.toString());
            System.out.println("📋 Parámetros: " + parametros);

            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                String apellido = rs.getString("apellido");
                String nombre = rs.getString("nombre");
                String nombreCompleto = apellido + ", " + nombre;

                // OBTENER DNI Y EMAIL CON VALORES POR DEFECTO
                String dni = rs.getString("dni");
                String email = rs.getString("mail");

                // Verificar que no sean nulos
                if (dni == null || dni.trim().isEmpty()) {
                    dni = "Sin DNI";
                }
                if (email == null || email.trim().isEmpty()) {
                    email = "Sin Email";
                }

                String curso = rs.getString("anio");
                String division = rs.getString("division");
                int cursoId = rs.getInt("curso_id");

                InfoAlumno info = new InfoAlumno(
                        rs.getInt("id"),
                        nombreCompleto,
                        dni,
                        email,
                        curso != null ? curso : "",
                        division != null ? division : "",
                        cursoId
                );

                // DEBUG: Mostrar datos del alumno
                if (count < 3) {
                    System.out.println("DEBUG Alumno " + (count + 1) + ":");
                    System.out.println("  Nombre completo: " + nombreCompleto);
                    System.out.println("  DNI: " + dni);
                    System.out.println("  Email: " + email);
                    System.out.println("  Curso: " + curso + "°" + division);
                }

                // Crear múltiples claves de búsqueda para el mismo alumno
                String claveNormal = normalizarNombre(nombreCompleto);
                alumnos.put(claveNormal, info);

                // Clave adicional: Solo apellido
                if (apellido != null) {
                    String claveApellido = normalizarNombre(apellido);
                    alumnos.put(claveApellido, info);

                    // Clave adicional: Apellido + inicial del nombre
                    if (nombre != null && nombre.length() > 0) {
                        String inicial = String.valueOf(nombre.charAt(0)).toUpperCase();
                        String claveApellidoInicial = normalizarNombre(apellido + " " + inicial);
                        alumnos.put(claveApellidoInicial, info);

                        // Clave adicional: Formato con guión bajo
                        String claveConGuion = normalizarNombre(apellido + "_" + inicial);
                        alumnos.put(claveConGuion, info);
                    }
                }

                count++;
            }

            rs.close();
            ps.close();

            System.out.println("✅ Alumnos del curso específico cargados: " + count);
            System.out.println("✅ Total claves de búsqueda generadas: " + alumnos.size());

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo alumnos del curso específico: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnos;
    }

    /**
     * Construir rutas de búsqueda optimizadas
     */
    private List<String> construirRutasBusquedaOptimizada(String anioLectivo, String anio,
            String division, String periodo) {
        List<String> rutas = new ArrayList<>();
        String servidorBase = GestorBoletines.obtenerRutaServidor();

        // Determinar años a buscar
        List<String> aniosBuscar = new ArrayList<>();
        if ("TODOS".equals(anio)) {
            for (int i = 1; i <= 7; i++) {
                aniosBuscar.add(String.valueOf(i));
            }
        } else {
            aniosBuscar.add(anio);
        }

        // Determinar divisiones a buscar
        List<String> divisionesBuscar = new ArrayList<>();
        if ("TODAS".equals(division)) {
            for (int i = 1; i <= 10; i++) {
                divisionesBuscar.add(String.valueOf(i));
            }
        } else {
            divisionesBuscar.add(division);
        }

        // Determinar períodos a buscar
        List<String> periodosBuscar = new ArrayList<>();
        if ("TODOS".equals(periodo)) {
            periodosBuscar.add("1B");
            periodosBuscar.add("2B");
            periodosBuscar.add("3B");
            periodosBuscar.add("4B");
            periodosBuscar.add("1C");
            periodosBuscar.add("2C");
            periodosBuscar.add("Final");
            periodosBuscar.add("Diciembre");
            periodosBuscar.add("Febrero");
        } else {
            periodosBuscar.add(periodo);
        }

        // Construir rutas
        for (String a : aniosBuscar) {
            for (String d : divisionesBuscar) {
                for (String p : periodosBuscar) {
                    String curso = a + d; // Formato: "11", "12", "21", etc.
                    String ruta = servidorBase + anioLectivo + "/" + curso + "/" + p + "/";
                    rutas.add(ruta);
                }
            }
        }

        return rutas;
    }

    /**
     * Buscar PDFs en ruta optimizada para curso específico
     */
    private List<BoletinInfo> buscarPDFsEnRutaOptimizada(String rutaBase, Map<String, InfoAlumno> alumnosDelCurso) {
        List<BoletinInfo> boletines = new ArrayList<>();

        try {
            System.out.println("📁 Buscando en: " + rutaBase);
            System.out.println("👥 Alumnos del curso: " + alumnosDelCurso.size());

            // Método 1: Intentar listar directorio
            List<String> archivosEncontrados = listarArchivosEnDirectorio(rutaBase);

            if (!archivosEncontrados.isEmpty()) {
                System.out.println("✅ Listado exitoso: " + archivosEncontrados.size() + " archivos");

                for (String archivo : archivosEncontrados) {
                    if (archivo.toLowerCase().endsWith(".pdf")) {
                        BoletinInfo boletin = procesarArchivoPDFOptimizado(rutaBase + archivo, archivo, alumnosDelCurso);
                        if (boletin != null) {
                            boletines.add(boletin);
                        }
                    }
                }
            } else {
                System.out.println("⚠️ Listado no disponible, buscando por nombres específicos...");

                // Método 2: Búsqueda específica para cada alumno del curso
                for (InfoAlumno alumno : alumnosDelCurso.values()) {
                    if (alumno.nombreCompleto.contains(",")) {
                        List<String> nombresProbables = generarNombresProbablesPDF(alumno, rutaBase);

                        for (String nombreArchivo : nombresProbables) {
                            String urlCompleta = rutaBase + nombreArchivo;

                            if (verificarArchivoExiste(urlCompleta)) {
                                BoletinInfo boletin = procesarArchivoPDFOptimizado(urlCompleta, nombreArchivo, alumnosDelCurso);
                                if (boletin != null) {
                                    boletines.add(boletin);
                                    break; // Solo uno por alumno
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error buscando PDFs en ruta " + rutaBase + ": " + e.getMessage());
        }

        return boletines;
    }

    /**
     * MÉTODO CORREGIDO: Procesar archivo PDF con datos completos
     */
    private BoletinInfo procesarArchivoPDFOptimizado(String urlCompleta, String nombreArchivo,
            Map<String, InfoAlumno> alumnosDelCurso) {
        try {
            BoletinInfo boletin = new BoletinInfo();
            boletin.nombreArchivo = nombreArchivo;
            boletin.urlServidor = urlCompleta;
            boletin.periodo = extraerPeriodoDeLaRuta(urlCompleta);

            // Extraer curso y división de la ruta
            String curso = extraerCursoDeLaRuta(urlCompleta);
            if (curso.length() >= 2) {
                boletin.curso = String.valueOf(curso.charAt(0));
                boletin.division = String.valueOf(curso.charAt(1));
            }

            // IDENTIFICACIÓN MEJORADA DEL ALUMNO
            InfoAlumno alumno = identificarAlumnoPorNombreOptimizado(nombreArchivo, alumnosDelCurso);

            if (alumno != null) {
                // ASEGURAR QUE LOS DATOS SE COPIEN CORRECTAMENTE
                boletin.alumnoNombre = alumno.nombreCompleto;
                boletin.alumnoDni = alumno.dni != null ? alumno.dni : "Sin DNI";
                boletin.alumnoEmail = alumno.email != null ? alumno.email : "Sin Email";
                boletin.cursoId = alumno.cursoId;

                System.out.println("✅ Alumno identificado exitosamente:");
                System.out.println("  Nombre: " + boletin.alumnoNombre);
                System.out.println("  DNI: " + boletin.alumnoDni);
                System.out.println("  Email: " + boletin.alumnoEmail);
                System.out.println("  Curso: " + boletin.curso + boletin.division);
            } else {
                // MANEJO CUANDO NO SE IDENTIFICA - CON BÚSQUEDA ALTERNATIVA
                String apellidoExtraido = extraerApellidoDelArchivo(nombreArchivo);

                if (!apellidoExtraido.isEmpty()) {
                    InfoAlumno alumnoEncontrado = buscarAlumnoPorApellido(apellidoExtraido, alumnosDelCurso);

                    if (alumnoEncontrado != null) {
                        boletin.alumnoNombre = alumnoEncontrado.nombreCompleto;
                        boletin.alumnoDni = alumnoEncontrado.dni != null ? alumnoEncontrado.dni : "Sin DNI";
                        boletin.alumnoEmail = alumnoEncontrado.email != null ? alumnoEncontrado.email : "Sin Email";
                        boletin.cursoId = alumnoEncontrado.cursoId;
                        System.out.println("🔍 RECUPERADO por apellido: " + alumnoEncontrado.nombreCompleto);
                        System.out.println("  DNI: " + boletin.alumnoDni);
                        System.out.println("  Email: " + boletin.alumnoEmail);
                    } else {
                        // Como último recurso, usar datos extraídos del archivo
                        boletin.alumnoNombre = extraerNombreDelArchivo(nombreArchivo);
                        boletin.alumnoDni = "No encontrado";
                        boletin.alumnoEmail = "No encontrado";
                        boletin.cursoId = -1;
                        System.out.println("⚠️ Usando datos del archivo sin identificar alumno");
                    }
                } else {
                    boletin.alumnoNombre = extraerNombreDelArchivo(nombreArchivo);
                    boletin.alumnoDni = "No encontrado";
                    boletin.alumnoEmail = "No encontrado";
                    boletin.cursoId = -1;
                    System.out.println("❌ No se pudo extraer apellido del archivo: " + nombreArchivo);
                }
            }

            // Obtener información del archivo
            boletin.tamanioArchivo = obtenerTamanioArchivo(urlCompleta);
            boletin.esAccesible = boletin.tamanioArchivo > 0;

            return boletin;

        } catch (Exception e) {
            System.err.println("Error procesando PDF " + nombreArchivo + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * MÉTODO CORREGIDO: Identificar alumno por nombre mejorado
     */
    private InfoAlumno identificarAlumnoPorNombreOptimizado(String nombreArchivo, Map<String, InfoAlumno> alumnosDelCurso) {
        try {
            System.out.println("🔍 Identificando alumno para archivo: " + nombreArchivo);

            // PASO 1: Limpiar nombre del archivo
            String nombreLimpio = nombreArchivo
                    .replaceAll("\\.pdf$", "")
                    .replaceAll("Boletin_", "")
                    .replaceAll("boletin_", "")
                    .replaceAll("BOLETIN_", "")
                    .replaceAll("_\\d{4}-\\d{2}-\\d{2}$", "") // Remover fecha al final
                    .replaceAll("_\\d+[BC]$", "") // Remover período al final
                    .replaceAll("_\\d{2}$", ""); // Remover curso/división al final

            System.out.println("🧹 Nombre limpio: " + nombreLimpio);

            // PASO 2: Extraer apellido del archivo (primera parte antes del guión bajo)
            String[] partes = nombreLimpio.split("_");
            if (partes.length > 0) {
                String apellidoDelArchivo = partes[0];
                System.out.println("👤 Apellido extraído del archivo: " + apellidoDelArchivo);

                // PASO 3: Buscar por apellido en los alumnos del curso
                String apellidoNormalizado = normalizarNombre(apellidoDelArchivo);

                // Búsqueda exacta por apellido
                for (InfoAlumno infoAlumno : alumnosDelCurso.values()) {
                    if (infoAlumno.nombreCompleto.contains(",")) {
                        String apellidoAlumno = infoAlumno.nombreCompleto.split(",")[0].trim();
                        String apellidoAlumnoNormalizado = normalizarNombre(apellidoAlumno);

                        if (apellidoNormalizado.equals(apellidoAlumnoNormalizado)) {
                            System.out.println("🎯 COINCIDENCIA EXACTA POR APELLIDO: " + infoAlumno.nombreCompleto);
                            System.out.println("  DNI encontrado: " + infoAlumno.dni);
                            System.out.println("  Email encontrado: " + infoAlumno.email);
                            return infoAlumno;
                        }
                    }
                }

                // Búsqueda parcial por apellido
                for (InfoAlumno infoAlumno : alumnosDelCurso.values()) {
                    if (infoAlumno.nombreCompleto.contains(",")) {
                        String apellidoAlumno = infoAlumno.nombreCompleto.split(",")[0].trim();
                        String apellidoAlumnoNormalizado = normalizarNombre(apellidoAlumno);

                        if (apellidoAlumnoNormalizado.contains(apellidoNormalizado)
                                || apellidoNormalizado.contains(apellidoAlumnoNormalizado)) {
                            System.out.println("🔍 COINCIDENCIA PARCIAL: " + infoAlumno.nombreCompleto);
                            System.out.println("  DNI encontrado: " + infoAlumno.dni);
                            System.out.println("  Email encontrado: " + infoAlumno.email);
                            return infoAlumno;
                        }
                    }
                }
            }

            System.out.println("❌ No se identificó alumno para: " + nombreArchivo);

            // Debug: Mostrar algunos apellidos disponibles
            System.out.println("📋 Algunos apellidos disponibles en el curso:");
            int count = 0;
            for (InfoAlumno alumno : alumnosDelCurso.values()) {
                if (count < 5 && alumno.nombreCompleto.contains(",")) {
                    String apellido = alumno.nombreCompleto.split(",")[0].trim();
                    System.out.println("  - " + apellido + " (normalizado: " + normalizarNombre(apellido) + ")");
                    count++;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error identificando alumno: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Extraer apellido del archivo
     */
    private String extraerApellidoDelArchivo(String nombreArchivo) {
        try {
            String nombreLimpio = nombreArchivo
                    .replaceAll("\\.pdf$", "")
                    .replaceAll("Boletin_", "")
                    .replaceAll("boletin_", "")
                    .replaceAll("BOLETIN_", "");

            // Extraer primera parte (debería ser el apellido)
            String[] partes = nombreLimpio.split("_");
            if (partes.length > 0) {
                return partes[0];
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Buscar alumno por apellido solamente
     */
    private InfoAlumno buscarAlumnoPorApellido(String apellido, Map<String, InfoAlumno> alumnosDelCurso) {
        try {
            String apellidoNormalizado = normalizarNombre(apellido);

            for (InfoAlumno alumno : alumnosDelCurso.values()) {
                if (alumno.nombreCompleto.contains(",")) {
                    String apellidoAlumno = alumno.nombreCompleto.split(",")[0].trim();
                    String apellidoAlumnoNormalizado = normalizarNombre(apellidoAlumno);

                    if (apellidoNormalizado.equals(apellidoAlumnoNormalizado)) {
                        return alumno;
                    }
                }
            }

            // Búsqueda más flexible
            for (InfoAlumno alumno : alumnosDelCurso.values()) {
                if (alumno.nombreCompleto.contains(",")) {
                    String apellidoAlumno = alumno.nombreCompleto.split(",")[0].trim();
                    String apellidoAlumnoNormalizado = normalizarNombre(apellidoAlumno);

                    if (apellidoAlumnoNormalizado.contains(apellidoNormalizado)
                            || apellidoNormalizado.contains(apellidoAlumnoNormalizado)) {
                        return alumno;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error buscando por apellido: " + e.getMessage());
        }

        return null;
    }

    /**
     * Normalizar nombre para búsqueda
     */
    private String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }

        return nombre.toLowerCase()
                .replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i")
                .replaceAll("ó", "o").replaceAll("ú", "u").replaceAll("ñ", "n")
                .replaceAll("Á", "a").replaceAll("É", "e").replaceAll("Í", "i")
                .replaceAll("Ó", "o").replaceAll("Ú", "u").replaceAll("Ñ", "n")
                .replaceAll("[^a-zA-Z0-9\\s]", "") // Remover caracteres especiales
                .replaceAll("\\s+", " ") // Múltiples espacios a uno solo
                .trim();
    }

    /**
     * Actualizar tabla con información completa
     */
    private void actualizarTablaBoletines() {
        modeloTabla.setRowCount(0);

        System.out.println("📊 Actualizando tabla con " + boletinesActuales.size() + " boletines");

        for (BoletinInfo boletin : boletinesActuales) {
            // VERIFICAR QUE LOS DATOS ESTÉN CORRECTOS ANTES DE MOSTRAR
            String nombreMostrar = boletin.alumnoNombre != null ? boletin.alumnoNombre : "Sin nombre";
            String dniMostrar = (boletin.alumnoDni != null && !boletin.alumnoDni.isEmpty()) ? boletin.alumnoDni : "Sin DNI";
            String emailMostrar = (boletin.alumnoEmail != null && !boletin.alumnoEmail.isEmpty()) ? boletin.alumnoEmail : "Sin Email";

            System.out.println("Agregando a tabla: " + nombreMostrar + " | DNI: " + dniMostrar + " | Email: " + emailMostrar);

            Object[] fila = {
                false, // Checkbox
                nombreMostrar,
                dniMostrar,
                emailMostrar,
                boletin.curso,
                boletin.division,
                boletin.periodo,
                boletin.nombreArchivo,
                formatearTamanio(boletin.tamanioArchivo),
                boletin.esAccesible ? "✅ Disponible" : "❌ No accesible"
            };

            modeloTabla.addRow(fila);
        }

        if (modeloTabla.getRowCount() == 0) {
            modeloTabla.addRow(new Object[]{
                false, "No se encontraron boletines PDF para el curso seleccionado",
                "", "", "", "", "", "", "", ""
            });
        }

        // Estadísticas mejoradas
        int total = boletinesActuales.size();
        int conEmail = (int) boletinesActuales.stream()
                .filter(b -> b.alumnoEmail != null && !b.alumnoEmail.isEmpty() && !"Sin Email".equals(b.alumnoEmail))
                .count();
        int conDni = (int) boletinesActuales.stream()
                .filter(b -> b.alumnoDni != null && !b.alumnoDni.isEmpty() && !"Sin DNI".equals(b.alumnoDni))
                .count();

        System.out.println(String.format("📊 Panel actualizado: %d boletines, %d con email, %d con DNI",
                total, conEmail, conDni));
    }

    // ========================================
    // MÉTODOS DE UTILIDAD DEL SERVIDOR
    // ========================================
    /**
     * Listar archivos en directorio del servidor
     */
    private List<String> listarArchivosEnDirectorio(String url) {
        List<String> archivos = new ArrayList<>();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                String contentType = connection.getContentType();

                if (contentType != null && contentType.contains("text/html")) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String linea;
                        StringBuilder contenidoHTML = new StringBuilder();

                        while ((linea = reader.readLine()) != null) {
                            contenidoHTML.append(linea).append("\n");
                        }

                        archivos = extraerArchivosDeHTML(contenidoHTML.toString());
                    }
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            System.out.println("⚠️ No se puede listar directorio: " + e.getMessage());
        }

        return archivos;
    }

    private String obtenerEmailAlumno(int alumnoId) {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT mail FROM usuarios WHERE id = ? AND rol = '4'";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();

            String email = null;
            if (rs.next()) {
                email = rs.getString("mail");
            }

            rs.close();
            ps.close();
            return email;

        } catch (Exception e) {
            System.err.println("Error obteniendo email del alumno " + alumnoId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Extraer archivos de HTML
     */
    private List<String> extraerArchivosDeHTML(String html) {
        List<String> archivos = new ArrayList<>();

        try {
            Pattern[] patrones = {
                Pattern.compile("href=[\"']([^\"']+\\.pdf)[\"']", Pattern.CASE_INSENSITIVE),
                Pattern.compile(">([^<]+\\.pdf)<", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\"([^\"]+\\.pdf)\"", Pattern.CASE_INSENSITIVE)
            };

            for (Pattern patron : patrones) {
                Matcher matcher = patron.matcher(html);
                while (matcher.find()) {
                    String archivo = matcher.group(1);

                    if (archivo != null && archivo.toLowerCase().endsWith(".pdf")
                            && !archivo.contains("/") && !archivo.contains("..")
                            && archivo.length() > 4) {

                        try {
                            archivo = java.net.URLDecoder.decode(archivo, "UTF-8");
                        } catch (Exception e) {
                            // Usar tal como está
                        }

                        if (!archivos.contains(archivo)) {
                            archivos.add(archivo);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error extrayendo archivos de HTML: " + e.getMessage());
        }

        return archivos;
    }

    /**
     * Generar nombres probables de PDF
     */
    private List<String> generarNombresProbablesPDF(InfoAlumno alumno, String rutaBase) {
        List<String> nombres = new ArrayList<>();

        try {
            String periodo = extraerPeriodoDeLaRuta(rutaBase);
            String curso = extraerCursoDeLaRuta(rutaBase);
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String nombreLimpio = alumno.nombreCompleto
                    .replaceAll("[^a-zA-Z0-9\\s,]", "")
                    .replaceAll("\\s+", "_")
                    .replaceAll(",", "_");

            // Patrones más específicos para el curso
            nombres.add("Boletin_" + nombreLimpio + "_" + curso + "_" + periodo + "_" + fechaActual + ".pdf");
            nombres.add("Boletin_" + nombreLimpio + "_" + curso + "_" + periodo + ".pdf");
            nombres.add("Boletin_" + nombreLimpio + ".pdf");

            // Apellido + inicial
            String[] partes = alumno.nombreCompleto.split(",");
            if (partes.length >= 2) {
                String apellido = partes[0].trim().replaceAll("[^a-zA-Z0-9]", "_");
                String nombre = partes[1].trim();
                String inicial = nombre.length() > 0 ? String.valueOf(nombre.charAt(0)) : "";

                nombres.add("Boletin_" + apellido + "_" + inicial + "_" + curso + "_" + periodo + ".pdf");
                nombres.add("Boletin_" + apellido + "_" + inicial + ".pdf");
            }

            // Variaciones con fechas recientes
            for (int i = 0; i < 15; i++) {
                String fecha = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                nombres.add("Boletin_" + nombreLimpio + "_" + curso + "_" + periodo + "_" + fecha + ".pdf");
            }

        } catch (Exception e) {
            System.err.println("Error generando nombres probables: " + e.getMessage());
        }

        return nombres;
    }

    /**
     * Verificar si archivo existe
     */
    private boolean verificarArchivoExiste(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extraer período de la ruta
     */
    private String extraerPeriodoDeLaRuta(String ruta) {
        try {
            String[] partes = ruta.split("/");
            for (int i = partes.length - 1; i >= 0; i--) {
                String parte = partes[i];
                if (parte.matches("\\d+[BC]|Final|Diciembre|Febrero")) {
                    return parte;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo período: " + e.getMessage());
        }
        return "Desconocido";
    }

    /**
     * Extraer curso de la ruta
     */
    private String extraerCursoDeLaRuta(String ruta) {
        try {
            String[] partes = ruta.split("/");
            for (String parte : partes) {
                if (parte.matches("\\d{2,3}")) {
                    return parte;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo curso: " + e.getMessage());
        }
        return "";
    }

    /**
     * Extraer nombre del archivo
     */
    private String extraerNombreDelArchivo(String nombreArchivo) {
        try {
            String nombre = nombreArchivo
                    .replaceAll("\\.pdf$", "")
                    .replaceAll("Boletin_", "")
                    .replaceAll("_Boletin", "")
                    .replaceAll("_\\d+[BC]", "")
                    .replaceAll("_\\d{4}-\\d{2}-\\d{2}", "")
                    .replaceAll("_", " ");

            return nombre.trim();

        } catch (Exception e) {
            return nombreArchivo;
        }
    }

    /**
     * Obtener tamaño de archivo
     */
    private long obtenerTamanioArchivo(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                long tamanio = connection.getContentLengthLong();
                connection.disconnect();
                return tamanio > 0 ? tamanio : 1024;
            }

            connection.disconnect();
            return 0;

        } catch (Exception e) {
            return 0;
        }
    }

    // ========================================
    // MÉTODOS DE ACCIONES DEL USUARIO
    // ========================================
    /**
     * Abrir boletín PDF
     */
    private void abrirBoletinPDF(ActionEvent e) {
        int filaSeleccionada = tablaBoletines.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un boletín de la tabla",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (filaSeleccionada >= boletinesActuales.size()) {
            JOptionPane.showMessageDialog(this,
                    "Selección inválida",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        BoletinInfo boletin = boletinesActuales.get(filaSeleccionada);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return abrirPDFDesdeServidor(boletin.urlServidor);
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (!exito) {
                        JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                "No se pudo abrir el PDF: " + boletin.nombreArchivo,
                                "Error al abrir PDF",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                            "Error al abrir PDF: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Abrir PDF desde servidor
     */
    private boolean abrirPDFDesdeServidor(String url) {
        try {
            File tempFile = File.createTempFile("boletin_", ".pdf");
            tempFile.deleteOnExit();

            if (descargarArchivoDesdeServidor(url, tempFile.getAbsolutePath())) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error abriendo PDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Descargar archivo desde servidor
     */
    private boolean descargarArchivoDesdeServidor(String url, String rutaDestino) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream inputStream = connection.getInputStream(); FileOutputStream outputStream = new FileOutputStream(rutaDestino)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    return true;
                }
            }
            connection.disconnect();
            return false;
        } catch (Exception e) {
            System.err.println("Error descargando archivo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Descargar boletines seleccionados
     */
    private void descargarBoletines(ActionEvent e) {
        List<Integer> filasSeleccionadas = obtenerFilasSeleccionadas();

        if (filasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione al menos un boletín para descargar",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Seleccionar carpeta para guardar boletines");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File carpetaDestino = fileChooser.getSelectedFile();

            SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    int descargados = 0;
                    for (int fila : filasSeleccionadas) {
                        BoletinInfo boletin = boletinesActuales.get(fila);
                        publish("Descargando: " + boletin.alumnoNombre + "...");

                        String rutaDestino = carpetaDestino.getAbsolutePath()
                                + File.separator + boletin.nombreArchivo;

                        if (descargarArchivoDesdeServidor(boletin.urlServidor, rutaDestino)) {
                            descargados++;
                            publish("✅ Descargado: " + boletin.nombreArchivo);
                        } else {
                            publish("❌ Error: " + boletin.nombreArchivo);
                        }
                    }
                    return descargados;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String mensaje : chunks) {
                        System.out.println(mensaje);
                    }
                }

                @Override
                protected void done() {
                    try {
                        int descargados = get();
                        JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                String.format("Descarga completada.\n\nArchivos descargados: %d de %d\nUbicación: %s",
                                        descargados, filasSeleccionadas.size(), carpetaDestino.getAbsolutePath()),
                                "Descarga Completada",
                                JOptionPane.INFORMATION_MESSAGE);

                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(carpetaDestino);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelGestionBoletines.this,
                                "Error durante la descarga: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Obtener filas seleccionadas
     */
    private List<Integer> obtenerFilasSeleccionadas() {
        List<Integer> seleccionadas = new ArrayList<>();
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Boolean seleccionado = (Boolean) modeloTabla.getValueAt(i, 0);
            if (seleccionado != null && seleccionado) {
                seleccionadas.add(i);
            }
        }
        return seleccionadas;
    }

    /**
     * Formatear tamaño de archivo
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

    /**
     * Enviar boletines por email
     */
    private void enviarBoletinesPorEmail(ActionEvent e) {
        // VERIFICAR LIBRERÍAS PRIMERO
        if (!EmailBoletinUtility.verificarLibreriasEmail()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: Librerías de email no disponibles\n\n"
                    + "Faltan librerías requeridas para el envío de emails:\n"
                    + "• javax.mail-1.6.2.jar\n"
                    + "• activation-1.1.1.jar\n\n"
                    + "Contacte al administrador del sistema.",
                    "Librerías Faltantes",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Integer> filasSeleccionadas = obtenerFilasSeleccionadas();

        if (filasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione al menos un boletín para enviar por email.",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // VERIFICAR CONFIGURACIÓN SOLO CUANDO SEA NECESARIO
        EmailBoletinUtility.ConfiguracionEmail config = EmailBoletinUtility.obtenerConfiguracion();

        if (!config.isCompleta()) {
            System.out.println("⚠️ Email no configurado, mostrando opciones...");

            // Mostrar opciones de configuración
            EmailBoletinUtility.mostrarOpcionesConfiguracion(this, userId);

            // Verificar nuevamente después de la configuración
            config = EmailBoletinUtility.obtenerConfiguracion();
            if (!config.isCompleta()) {
                JOptionPane.showMessageDialog(this,
                        "❌ Email no configurado\n\n"
                        + "Para enviar boletines por email necesita configurar:\n"
                        + "• Servidor de email (SMTP)\n"
                        + "• Credenciales de acceso\n\n"
                        + "Use el botón 'Config Email' para configurar.",
                        "Configuración Requerida",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
    }

    private String obtenerPeriodoActual() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        int mes = hoy.getMonthValue();

        if (mes >= 3 && mes <= 4) {
            return "1er Bimestre";
        }
        if (mes >= 5 && mes <= 7) {
            return "2do Bimestre";
        }
        if (mes >= 8 && mes <= 9) {
            return "3er Bimestre";
        }
        if (mes >= 10 && mes <= 11) {
            return "4to Bimestre";
        }

        return "1er Bimestre";
    }

    private void configurarEmail(ActionEvent e) {
        // Usar el nuevo sistema de opciones
        EmailBoletinUtility.mostrarOpcionesConfiguracion(this, userId);
    }

    private void verificarEstadoEmail() {
        EmailBoletinUtility.ConfiguracionEmail config = EmailBoletinUtility.obtenerConfiguracion();

        if (config.isCompleta()) {
            String metodo = config.useOAuth2 ? "OAuth2" : "SMTP";
            System.out.println("✅ Email configurado (" + metodo + "): " + config.emailRemitente);

            // Actualizar tooltip del botón de email
            btnEnviarEmail.setToolTipText("Enviar por email - Configurado: " + config.emailRemitente);
            btnEnviarEmail.setEnabled(true);
        } else {
            System.out.println("⚠️ Email no configurado");
            btnEnviarEmail.setToolTipText("Enviar por email - Requiere configuración");
            btnEnviarEmail.setEnabled(true); // Mantener habilitado para mostrar opciones
        }
    }

    private void abrirConfiguracionEmail() {
        try {
            ConfiguracionEmailPanel panelConfig = new ConfiguracionEmailPanel(ventana, userId, userRol);

            // Crear ventana modal
            JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                    "Configuración de Email", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.add(panelConfig, BorderLayout.CENTER);
            dialog.setSize(700, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception ex) {
            System.err.println("Error abriendo configuración de email: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al abrir la configuración de email: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoEnvioEmail(List<GestorBoletines.InfoBoletin> boletines) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Información de configuración actual
        EmailBoletinUtility.ConfiguracionEmail config = EmailBoletinUtility.obtenerConfiguracion();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblConfigInfo = new JLabel(String.format(
                "<html><b>📧 Configuración de Email</b><br>"
                + "Método: %s<br>"
                + "Remitente: %s<br>"
                + "Nombre: %s</html>",
                config.useOAuth2 ? "OAuth2 (Google) ✅" : "SMTP tradicional ⚙️",
                config.emailRemitente,
                config.nombreRemitente));
        lblConfigInfo.setForeground(new Color(33, 150, 243));
        panel.add(lblConfigInfo, gbc);

        // Asunto personalizado
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Asunto:"), gbc);

        JTextField txtAsunto = new JTextField(40);
        txtAsunto.setText("Boletín de Calificaciones - " + obtenerPeriodoActual() + " - Escuela Técnica N° 20");
        gbc.gridx = 1;
        panel.add(txtAsunto, gbc);

        // Mensaje personalizado
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Mensaje adicional:"), gbc);

        JTextArea txtMensaje = new JTextArea(4, 40);
        txtMensaje.setText("Estimado/a estudiante,\n\n"
                + "Adjuntamos su boletín de calificaciones del período actual.\n\n"
                + "Por favor, revise la información y en caso de consultas comuníquese con la secretaría.\n\n"
                + "Saludos cordiales,\nEscuela Técnica N° 20");
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setLineWrap(true);
        gbc.gridx = 1;
        panel.add(new JScrollPane(txtMensaje), gbc);

        // Información de envío
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        // Contar alumnos con email válido
        int alumnosConEmail = 0;
        int alumnosSinEmail = 0;
        for (GestorBoletines.InfoBoletin boletin : boletines) {
            // Obtener email del alumno
            String email = obtenerEmailAlumno(boletin.alumnoId);
            if (email != null && !email.isEmpty() && email.contains("@")) {
                alumnosConEmail++;
            } else {
                alumnosSinEmail++;
            }
        }

        JLabel lblInfo = new JLabel(String.format(
                "<html><b>📊 Resumen de Envío</b><br>"
                + "Total seleccionados: %d boletines<br>"
                + "Con email válido: %d (se enviarán) ✅<br>"
                + "Sin email válido: %d (se omitirán) ⚠️<br><br>"
                + "<i>Solo se enviarán emails a estudiantes con direcciones válidas.</i></html>",
                boletines.size(), alumnosConEmail, alumnosSinEmail));

        if (alumnosSinEmail > 0) {
            lblInfo.setForeground(new Color(255, 152, 0)); // Naranja para advertencia
        } else {
            lblInfo.setForeground(new Color(76, 175, 80)); // Verde para todo OK
        }
        panel.add(lblInfo, gbc);

        // Mostrar diálogo
        int resultado = JOptionPane.showConfirmDialog(this, panel,
                "📧 Enviar Boletines por Email",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String asuntoPersonalizado = txtAsunto.getText().trim();
            String mensajePersonalizado = txtMensaje.getText().trim();

            // Confirmación final si hay alumnos sin email
            if (alumnosSinEmail > 0) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        String.format("⚠️ ADVERTENCIA\n\n"
                                + "%d estudiantes no tienen email válido y no recibirán el boletín.\n\n"
                                + "¿Desea continuar con el envío para los %d estudiantes con email válido?",
                                alumnosSinEmail, alumnosConEmail),
                        "Confirmar Envío",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Enviar emails usando EmailBoletinUtility
            EmailBoletinUtility.enviarBoletinesMasivosConInterfaz(
                    boletines, asuntoPersonalizado, mensajePersonalizado, this);
        }
    }

    private int obtenerAlumnoIdPorNombre(String nombreCompleto) {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT id FROM usuarios WHERE CONCAT(apellido, ', ', nombre) = ? AND rol = '4' LIMIT 1";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, nombreCompleto);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                ps.close();
                return id;
            }

            rs.close();
            ps.close();

            // Si no encuentra, buscar de forma más flexible
            String[] partes = nombreCompleto.split(",");
            if (partes.length >= 2) {
                String apellido = partes[0].trim();
                String nombre = partes[1].trim();

                String queryFlex = "SELECT id FROM usuarios WHERE apellido LIKE ? AND nombre LIKE ? AND rol = '4' LIMIT 1";
                PreparedStatement psFlex = conect.prepareStatement(queryFlex);
                psFlex.setString(1, "%" + apellido + "%");
                psFlex.setString(2, "%" + nombre + "%");
                ResultSet rsFlex = psFlex.executeQuery();

                if (rsFlex.next()) {
                    int id = rsFlex.getInt("id");
                    rsFlex.close();
                    psFlex.close();
                    return id;
                }

                rsFlex.close();
                psFlex.close();
            }

            System.err.println("⚠️ No se encontró ID para el alumno: " + nombreCompleto);
            return -1;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo ID del alumno " + nombreCompleto + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Eliminar boletín
     */
    private void eliminarBoletin(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de eliminación en desarrollo",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Mostrar estadísticas
     */
    private void mostrarEstadisticas(ActionEvent e) {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADÍSTICAS DE BOLETINES PDF ===\n\n");
        stats.append("Total boletines encontrados: ").append(boletinesActuales.size()).append("\n");

        Map<String, Integer> porPeriodo = new HashMap<>();
        long espacioTotal = 0;
        int conEmail = 0;
        int conDni = 0;

        for (BoletinInfo b : boletinesActuales) {
            porPeriodo.put(b.periodo, porPeriodo.getOrDefault(b.periodo, 0) + 1);
            espacioTotal += b.tamanioArchivo;
            if (b.alumnoEmail != null && !b.alumnoEmail.isEmpty() && !"Sin Email".equals(b.alumnoEmail)) {
                conEmail++;
            }
            if (b.alumnoDni != null && !b.alumnoDni.isEmpty() && !"Sin DNI".equals(b.alumnoDni)) {
                conDni++;
            }
        }

        stats.append("Espacio total: ").append(formatearTamanio(espacioTotal)).append("\n");
        stats.append("Alumnos con email: ").append(conEmail).append("\n");
        stats.append("Alumnos con DNI: ").append(conDni).append("\n\n");

        stats.append("Por período:\n");
        for (Map.Entry<String, Integer> entry : porPeriodo.entrySet()) {
            stats.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        JTextArea textArea = new JTextArea(stats.toString());
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(50);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "Estadísticas de Boletines PDF",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Actualizar lista
     */
    private void actualizarLista(ActionEvent e) {
        buscarBoletinesPDF(null);
    }
}
