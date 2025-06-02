package main.java.views.users.Preceptor;

import com.toedter.calendar.JDateChooser;
import java.sql.*;
import java.time.LocalDate;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.*;
import main.java.views.users.common.AsistenciaPanel;
import main.java.database.Conexion;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.JFileChooser;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.apache.poi.xssf.usermodel.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import main.java.utils.ResponsiveUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * Panel de asistencia para preceptores, que permite gestionar y visualizar la
 * asistencia de los alumnos de un curso específico.
 *
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [3/12/2025]
 */
public class AsistenciaPreceptorPanel extends AsistenciaPanel {

    // ID del curso asociado al preceptor
    private int cursoId;

    // Mapa de colores para los diferentes estados de asistencia
    private Map<String, Color> colorEstados;

    // Modelo de tabla
    protected DefaultTableModel tableModel;

    /**
     * Constructor del panel de asistencia para preceptores.
     *
     * @param preceptorId Identificador del preceptor
     * @param cursoId Identificador del curso
     */
    public AsistenciaPreceptorPanel(int preceptorId, int cursoId) {
        // Constructor base
        super();

        // Inicializar conexión a base de datos
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Inicializar componentes y variables
        initComponents();
        this.usuarioId = preceptorId;
        this.cursoId = cursoId;
        this.fecha = LocalDate.now();

        // Ajustar a lunes de la semana actual
        while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
            fecha = fecha.minusDays(1);
        }

        // Establecer un tamaño preferido grande para forzar scrollbars
        setPreferredSize(new Dimension(1500, 1200));

        // Inicializar componentes y cargar datos
        inicializarBase();
        cargarDatosCurso();
        cargarAsistencias();
        configurarEventos();
        agregarFiltroBusqueda();
        agregarObservaciones();
        configurarDateChooser();
        configurarBotonesNavegacion();

        // Aplicar configuraciones para scrolling y responsividad
        configurarScrollingYTabla();
        

        // Asegurar visibilidad
        setVisible(true);

        System.out.println("Panel de asistencia creado. Tamaño: " + getPreferredSize());
    }

    /**
     * Carga los datos del curso asociado al preceptor.
     */
    private void cargarDatosCurso() {
        try {
            String query = "SELECT CONCAT(c.anio, '°', c.division) as curso "
                    + "FROM cursos c WHERE c.id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblCurso.setText("Curso: " + rs.getString("curso"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage());
        }
    }

    /**
     * Inicializa la estructura base de la tabla de asistencia.
     */
    private void inicializarBase() {
        // Inicializar tabla y modelo
        this.tableModel = new DefaultTableModel();
        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);
        }
        inicializarColores();
        configurarTabla();
    }

    @Override
    public boolean puedeEditarCelda(int row, int column) {
        return column == 2;  // Solo la columna de estado es editable
    }

    /**
     * Define los colores asociados a los diferentes estados de asistencia.
     */
    private void inicializarColores() {
        colorEstados = new HashMap<>();
        colorEstados.put("P", new Color(144, 238, 144));  // Verde claro
        colorEstados.put("A", new Color(255, 182, 193));  // Rojo claro
        colorEstados.put("T", new Color(255, 255, 153));  // Amarillo claro
        colorEstados.put("AP", new Color(255, 218, 185)); // Naranja claro
        colorEstados.put("NC", Color.WHITE);
    }

    /**
     * Configura la tabla de asistencias.
     */
    protected void configurarTabla() {
        if (tableModel == null) {
            tableModel = new DefaultTableModel();
        }

        // Configurar columnas
        tableModel.addColumn("Nº");  // Número incremental para mostrar en lugar del ID real
        tableModel.addColumn("Alumno");
        tableModel.addColumn("DNI");
        tableModel.addColumn("ID"); // Columna oculta para almacenar el ID real del alumno

        // Agregar columnas para cada día de la semana (turno y contraturno)
        LocalDate diaActual = fecha; // Empezar desde el lunes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM (EEE)");

        for (int i = 0; i < 5; i++) { // De lunes a viernes
            String nombreColumna = diaActual.format(formatter);
            tableModel.addColumn(nombreColumna);
            tableModel.addColumn(nombreColumna + " (Cont)");
            diaActual = diaActual.plusDays(1);
        }

        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);

            // Obtener el día de hoy para resaltarlo
            final int diaHoy = LocalDate.now().getDayOfWeek().getValue() - 1; // 0 para lunes, 4 para viernes
            final int columnaHoyNormal = 4 + (diaHoy * 2); // Ahora es 4 por las columnas añadidas
            final int columnaHoyContraturno = columnaHoyNormal + 1;

            // Configurar el renderizador para las columnas de asistencia
            for (int i = 4; i < tablaAsistencia.getColumnCount(); i++) { // Empezar desde 4 por las columnas añadidas
                final int columnaIndex = i;
                TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);

                // Configurar el renderizador para colores y resaltado
                column.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                        if (!isSelected) {
                            String estado = value != null ? value.toString() : "NC";
                            c.setBackground(colorEstados.getOrDefault(estado, Color.WHITE));

                            // Resaltar si es el día actual
                            if (columnaIndex == columnaHoyNormal || columnaIndex == columnaHoyContraturno) {
                                Font boldFont = c.getFont().deriveFont(Font.BOLD);
                                c.setFont(boldFont);

                                // Agregar un borde para resaltar más
                                if (c instanceof JComponent) {
                                    ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
                                }
                            }
                        }

                        return c;
                    }
                });

                // Configurar el editor para poder seleccionar el estado
                String[] opciones = {"P", "A", "T", "AP", "NC"};
                JComboBox<String> comboEstados = new JComboBox<>(opciones);
                column.setCellEditor(new DefaultCellEditor(comboEstados));
            }

            // Ocultar la columna ID real
            tablaAsistencia.getColumnModel().getColumn(3).setMinWidth(0);
            tablaAsistencia.getColumnModel().getColumn(3).setMaxWidth(0);
            tablaAsistencia.getColumnModel().getColumn(3).setWidth(0);

            // Ajustar anchos de columnas
            tablaAsistencia.getColumnModel().getColumn(0).setPreferredWidth(40);  // Nº incremental
            tablaAsistencia.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
            tablaAsistencia.getColumnModel().getColumn(2).setPreferredWidth(100); // DNI
        }
    }

    /**
     * Configura eventos para los componentes.
     */
    private void configurarEventos() {
        // Configurar event listener para el dateChooser
        if (dateChooser != null && dateChooser.getDate() != null) {
            fecha = dateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            // Ajustar a lunes de la semana seleccionada
            while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                fecha = fecha.minusDays(1);
            }
            cargarAsistencias();
        }
    }

    /**
     * Carga las asistencias desde la base de datos.
     */
    public void cargarAsistencias() {
        int intentos = 0;
        final int MAX_INTENTOS = 3;
        while (intentos < MAX_INTENTOS) {
            try {
                // Determinar la semana a partir de la fecha actual (que debe ser lunes)
                LocalDate inicioSemana = fecha;
                LocalDate finSemana = inicioSemana.plusDays(4); // Hasta el viernes

                System.out.println("Cargando asistencias para la semana: " + inicioSemana + " a " + finSemana);

                // Limpiar solo las filas, preservar las columnas
                tableModel.setRowCount(0);

                // Cargar alumnos
                String queryAlumnos = "SELECT u.id, u.nombre, u.apellido, u.dni "
                        + "FROM usuarios u "
                        + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                        + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 "
                        + "ORDER BY u.apellido, u.nombre";

                PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
                psAlumnos.setInt(1, cursoId);
                ResultSet rsAlumnos = psAlumnos.executeQuery();

                // Crear filas en la tabla para cada alumno
                int numeroFila = 1;
                while (rsAlumnos.next()) {
                    Object[] rowData = new Object[14]; // 4 columnas fijas + 5 días x 2 turnos
                    rowData[0] = numeroFila; // Número incremental para mostrar
                    rowData[1] = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                    rowData[2] = rsAlumnos.getString("dni");
                    rowData[3] = rsAlumnos.getInt("id"); // Guardar el ID real del alumno (columna oculta)

                    // Inicializar todos los estados como NC
                    for (int i = 4; i < rowData.length; i++) {
                        rowData[i] = "NC";
                    }

                    tableModel.addRow(rowData);
                    numeroFila++; // Incrementar para la siguiente fila
                }

                // Ahora, cargar asistencias de toda la semana (incluidas todas las fechas entre inicio y fin)
                String queryAsistencias = "SELECT a.alumno_id, a.fecha, a.estado, a.es_contraturno, u.dni "
                        + "FROM asistencia_general a "
                        + "JOIN usuarios u ON a.alumno_id = u.id "
                        + "WHERE a.curso_id = ? AND a.fecha BETWEEN ? AND ?";

                PreparedStatement psAsistencias = conect.prepareStatement(queryAsistencias);
                psAsistencias.setInt(1, cursoId);
                psAsistencias.setDate(2, java.sql.Date.valueOf(inicioSemana));
                psAsistencias.setDate(3, java.sql.Date.valueOf(finSemana));

                ResultSet rsAsistencias = psAsistencias.executeQuery();
                int contadorAsistencias = 0;

                while (rsAsistencias.next()) {
                    contadorAsistencias++;
                    int alumnoId = rsAsistencias.getInt("alumno_id");
                    LocalDate fechaAsistencia = rsAsistencias.getDate("fecha").toLocalDate();
                    String estado = rsAsistencias.getString("estado");
                    boolean esContraturno = rsAsistencias.getBoolean("es_contraturno");

                    // Calcular la columna correspondiente
                    // Primero, calcular el offset de días desde el inicio de la semana
                    int diaOffset = fechaAsistencia.getDayOfWeek().getValue() - 1; // 0 para lunes, 4 para viernes
                    int columnaBase = 4 + (diaOffset * 2); // 4 columnas iniciales + 2 por cada día
                    int columna = columnaBase + (esContraturno ? 1 : 0); // Agregar 1 si es contraturno

                    System.out.println("Cargando: ID=" + alumnoId + ", Fecha=" + fechaAsistencia
                            + ", Estado=" + estado + ", Contraturno=" + esContraturno
                            + ", Día=" + diaOffset + ", Columna=" + columna);

                    // Buscar la fila por ID y actualizar el estado
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        int idFila = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
                        if (idFila == alumnoId) {
                            tableModel.setValueAt(estado, i, columna);
                            break;
                        }
                    }
                }

                System.out.println("Total registros de asistencia cargados: " + contadorAsistencias);

                // Actualizar estadísticas para el día actual si está en la semana, o para el lunes
                LocalDate hoy = LocalDate.now();
                LocalDate diaParaEstadisticas;

                if (hoy.isAfter(inicioSemana.minusDays(1)) && hoy.isBefore(finSemana.plusDays(1))) {
                    diaParaEstadisticas = hoy;
                } else {
                    diaParaEstadisticas = inicioSemana;
                }

                actualizarEstadisticas(diaParaEstadisticas);
                return;
            } catch (SQLException ex) {
                intentos++;
                if (ex.getMessage().contains("Communications link failure")) {
                    System.out.println("Error de conexión a la BD. Intento " + intentos + " de " + MAX_INTENTOS);

                    // Intentar reconectar
                    conect = Conexion.getInstancia().verificarConexion();

                    // Si estamos en el último intento, mostrar error al usuario
                    if (intentos >= MAX_INTENTOS) {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo establecer conexión con la base de datos después de "
                                + MAX_INTENTOS + " intentos. Por favor, inténtelo más tarde.",
                                "Error de conexión", JOptionPane.ERROR_MESSAGE);
                    }

                    // Esperar un poco antes de reintentar
                    try {
                        Thread.sleep(1000 * intentos); // Espera progresiva
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Si es otro tipo de error SQL, mostrar y salir
                    JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
                    ex.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * Configura el selector de fecha.
     */
    private void configurarDateChooser() {
        // Establecer la fecha inicial
        dateChooser.setDate(java.sql.Date.valueOf(fecha));

        // Agregar el listener
        dateChooser.addPropertyChangeListener("date", evt -> {
            if ("date".equals(evt.getPropertyName()) && dateChooser.getDate() != null) {
                // Obtener la fecha seleccionada
                LocalDate fechaSeleccionada = dateChooser.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                // Ajustar al lunes de la semana
                LocalDate nuevoLunes = fechaSeleccionada;
                while (nuevoLunes.getDayOfWeek() != DayOfWeek.MONDAY) {
                    nuevoLunes = nuevoLunes.minusDays(1);
                }

                // Solo si cambia el lunes de la semana, recargar la tabla
                if (!nuevoLunes.equals(fecha)) {
                    fecha = nuevoLunes;
                    System.out.println("Cambiando a nueva semana: " + fecha);

                    // Recargar la tabla para la nueva semana
                    tableModel.setRowCount(0);
                    tableModel.setColumnCount(0);
                    configurarTabla();
                    cargarAsistencias();
                }
            }
        });
    }

    /**
     * Añade área para observaciones.
     */
    private void agregarObservaciones() {
        // Este método implementa la funcionalidad de observaciones
        // (Ya implementado a través del diseño del form en initComponents)
    }

    /**
     * Configura los botones de navegación entre semanas.
     */
    private void configurarBotonesNavegacion() {
        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton btnSemanaAnterior = new JButton("<<< Semana Anterior");
        JButton btnSemanaSiguiente = new JButton("Semana Siguiente >>>");

        btnSemanaAnterior.addActionListener(e -> {
            fecha = fecha.minusDays(7);
            // Actualizar el dateChooser para mantener sincronización
            dateChooser.setDate(java.sql.Date.valueOf(fecha));
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            configurarTabla();
            cargarAsistencias();
        });

        btnSemanaSiguiente.addActionListener(e -> {
            fecha = fecha.plusDays(7);
            // Actualizar el dateChooser para mantener sincronización
            dateChooser.setDate(java.sql.Date.valueOf(fecha));
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            configurarTabla();
            cargarAsistencias();
        });

        panelNavegacion.add(btnSemanaAnterior);
        panelNavegacion.add(btnSemanaSiguiente);

        // Añadir este panel al panel de asistencias (implementación específica según el diseño del panel)
    }

    /**
     * Añade funcionalidad de búsqueda y filtrado.
     */
    private void agregarFiltroBusqueda() {
        txtBuscar.setPreferredSize(new Dimension(300, 30)); // Tamaño más grande
        btnBuscar.setPreferredSize(new Dimension(100, 30));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                realizarBusqueda();
            }

            public void removeUpdate(DocumentEvent e) {
                realizarBusqueda();
            }

            public void insertUpdate(DocumentEvent e) {
                realizarBusqueda();
            }
        });
    }

    /**
     * Realiza la búsqueda según el texto ingresado.
     */
    private void realizarBusqueda() {
        String texto = txtBuscar.getText().toLowerCase();
        if (texto.isEmpty()) {
            tablaAsistencia.setRowSorter(null);
            return;
        }

        // Convertir texto de búsqueda a sin tildes
        texto = texto.replaceAll("[áàäâã]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöôõ]", "o")
                .replaceAll("[úùüû]", "u");

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        final String textoBusqueda = texto;

        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                String valor = entry.getStringValue(1).toLowerCase() // Columna Alumno
                        .replaceAll("[áàäâã]", "a")
                        .replaceAll("[éèëê]", "e")
                        .replaceAll("[íìïî]", "i")
                        .replaceAll("[óòöôõ]", "o")
                        .replaceAll("[úùüû]", "u");
                return valor.contains(textoBusqueda);
            }
        });

        tablaAsistencia.setRowSorter(sorter);
    }

    /**
     * Guarda las asistencias en la base de datos.
     */
    @Override
    protected void guardarAsistencias() {
        try {
            System.out.println("Iniciando guardado de asistencias para todos los días visibles en la tabla...");

            // Obtener el lunes de la semana actual
            LocalDate inicioDeSemana = fecha;
            while (inicioDeSemana.getDayOfWeek() != DayOfWeek.MONDAY) {
                inicioDeSemana = inicioDeSemana.minusDays(1);
            }

            // Recorrer cada día de la semana visible en la tabla (columnas 4+)
            for (int dia = 0; dia < 5; dia++) { // Lunes a viernes
                LocalDate fechaActual = inicioDeSemana.plusDays(dia);
                int columnaBase = 4 + (dia * 2); // Columna para el turno normal
                int columnaContraturno = columnaBase + 1; // Columna para el contraturno

                // Verificar que las columnas existen en la tabla
                if (columnaBase >= tableModel.getColumnCount()) {
                    continue; // No hay más columnas para procesar
                }

                System.out.println("Procesando día: " + fechaActual + " (columnas " + columnaBase + ", " + columnaContraturno + ")");

                // Eliminar registros existentes para esta fecha
                String deleteQuery = "DELETE FROM asistencia_general WHERE fecha = ? AND curso_id = ?";
                PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
                deletePs.setDate(1, java.sql.Date.valueOf(fechaActual));
                deletePs.setInt(2, cursoId);
                int registrosEliminados = deletePs.executeUpdate();
                System.out.println("Registros eliminados para " + fechaActual + ": " + registrosEliminados);

                // Insertar nuevos registros para cada alumno
                String insertQuery = "INSERT INTO asistencia_general "
                        + "(alumno_id, curso_id, fecha, estado, creado_por, es_contraturno) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conect.prepareStatement(insertQuery);

                int registrosInsertados = 0;

                // Para cada alumno en la tabla
                for (int fila = 0; fila < tableModel.getRowCount(); fila++) {
                    int alumnoId = Integer.parseInt(tableModel.getValueAt(fila, 3).toString()); // ID del alumno (columna oculta)

                    // Procesar turno normal
                    if (columnaBase < tableModel.getColumnCount()) {
                        String estadoNormal = tableModel.getValueAt(fila, columnaBase).toString();
                        if (!estadoNormal.equals("NC")) {
                            insertPs.setInt(1, alumnoId);
                            insertPs.setInt(2, cursoId);
                            insertPs.setDate(3, java.sql.Date.valueOf(fechaActual));
                            insertPs.setString(4, estadoNormal);
                            insertPs.setInt(5, usuarioId);
                            insertPs.setBoolean(6, false); // Es turno normal
                            insertPs.executeUpdate();
                            registrosInsertados++;
                        }
                    }

                    // Procesar contraturno si existe la columna
                    if (columnaContraturno < tableModel.getColumnCount()) {
                        String estadoContraturno = tableModel.getValueAt(fila, columnaContraturno).toString();
                        if (!estadoContraturno.equals("NC")) {
                            insertPs.setInt(1, alumnoId);
                            insertPs.setInt(2, cursoId);
                            insertPs.setDate(3, java.sql.Date.valueOf(fechaActual));
                            insertPs.setString(4, estadoContraturno);
                            insertPs.setInt(5, usuarioId);
                            insertPs.setBoolean(6, true); // Es contraturno
                            insertPs.executeUpdate();
                            registrosInsertados++;
                        }
                    }
                }

                System.out.println("Registros insertados para " + fechaActual + ": " + registrosInsertados);
            }

            JOptionPane.showMessageDialog(this, "Asistencias guardadas exitosamente para toda la semana");

            // Recargar para mostrar los cambios
            cargarAsistencias();

        } catch (SQLException ex) {
            System.out.println("Error al guardar asistencias: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
        }
    }

    /**
     * Actualiza las estadísticas de asistencia.
     *
     * @param fechaSeleccionada Fecha para la cual se mostrarán las estadísticas
     */
    private void actualizarEstadisticas(LocalDate fechaSeleccionada) {
        try {
            // Limpiar el panel de estadísticas
            panelEstadisticas.removeAll();

            System.out.println("Actualizando estadísticas para fecha: " + fechaSeleccionada);

            // Consultar estadísticas del día seleccionado - Turno normal
            String query = "SELECT "
                    + "COUNT(CASE WHEN estado = 'P' THEN 1 END) as presentes, "
                    + "COUNT(CASE WHEN estado = 'A' THEN 1 END) as ausentes, "
                    + "COUNT(CASE WHEN estado = 'T' THEN 1 END) as tarde, "
                    + "COUNT(CASE WHEN estado = 'AP' THEN 1 END) as ausente_presente, "
                    + "COUNT(*) as total "
                    + "FROM asistencia_general "
                    + "WHERE fecha = ? AND curso_id = ? AND es_contraturno = false";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setDate(1, java.sql.Date.valueOf(fechaSeleccionada));
            ps.setInt(2, cursoId);
            ResultSet rs = ps.executeQuery();

            // Crear panel para turno normal
            JPanel turnoNormalPanel = new JPanel();
            if (rs.next()) {
                int total = rs.getInt("total");

                // Solo mostrar si hay datos
                if (total > 0) {
                    int presentes = rs.getInt("presentes");
                    int ausentes = rs.getInt("ausentes");
                    int tarde = rs.getInt("tarde");
                    int ausentePresente = rs.getInt("ausente_presente");

                    System.out.println("Estadísticas Turno Normal: Total=" + total
                            + ", P=" + presentes + ", A=" + ausentes
                            + ", T=" + tarde + ", AP=" + ausentePresente);

                    double porcentajePresentes = (presentes * 100.0) / total;

                    JLabel lblEstadisticas = new JLabel(String.format(
                            "Turno Normal %s | Presentes: %d (%2.1f%%) | Ausentes: %d | Tarde: %d | AP: %d",
                            fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM")),
                            presentes, porcentajePresentes, ausentes, tarde, ausentePresente));

                    turnoNormalPanel.add(lblEstadisticas);
                } else {
                    JLabel lblNoData = new JLabel("No hay datos para el turno normal en "
                            + fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM")));
                    turnoNormalPanel.add(lblNoData);
                }
            }

            // Consultar estadísticas del contraturno
            String queryContraturno = "SELECT "
                    + "COUNT(CASE WHEN estado = 'P' THEN 1 END) as presentes, "
                    + "COUNT(CASE WHEN estado = 'A' THEN 1 END) as ausentes, "
                    + "COUNT(CASE WHEN estado = 'T' THEN 1 END) as tarde, "
                    + "COUNT(CASE WHEN estado = 'AP' THEN 1 END) as ausente_presente, "
                    + "COUNT(*) as total "
                    + "FROM asistencia_general "
                    + "WHERE fecha = ? AND curso_id = ? AND es_contraturno = true";

            ps = conect.prepareStatement(queryContraturno);
            ps.setDate(1, java.sql.Date.valueOf(fechaSeleccionada));
            ps.setInt(2, cursoId);
            ResultSet rsContraturno = ps.executeQuery();

            // Crear panel para contraturno
            JPanel contraturnoPanel = new JPanel();
            if (rsContraturno.next()) {
                int total = rsContraturno.getInt("total");

                // Solo mostrar si hay datos
                if (total > 0) {
                    int presentes = rsContraturno.getInt("presentes");
                    int ausentes = rsContraturno.getInt("ausentes");
                    int tarde = rsContraturno.getInt("tarde");
                    int ausentePresente = rsContraturno.getInt("ausente_presente");

                    System.out.println("Estadísticas Contraturno: Total=" + total
                            + ", P=" + presentes + ", A=" + ausentes
                            + ", T=" + tarde + ", AP=" + ausentePresente);

                    double porcentajePresentes = (presentes * 100.0) / total;

                    JLabel lblEstadisticas = new JLabel(String.format(
                            "Contraturno %s | Presentes: %d (%2.1f%%) | Ausentes: %d | Tarde: %d | AP: %d",
                            fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM")),
                            presentes, porcentajePresentes, ausentes, tarde, ausentePresente));

                    contraturnoPanel.add(lblEstadisticas);
                } else {
                    System.out.println("No hay datos de contraturno para mostrar");
                }
            }

            // Configurar el panel de estadísticas con layout BoxLayout vertical
            panelEstadisticas.setLayout(new BoxLayout(panelEstadisticas, BoxLayout.Y_AXIS));

            // Añadir paneles solo si tienen contenido
            if (turnoNormalPanel.getComponentCount() > 0) {
                panelEstadisticas.add(turnoNormalPanel);
            }

            if (contraturnoPanel.getComponentCount() > 0) {
                panelEstadisticas.add(contraturnoPanel);
                System.out.println("Se agregó panel de contraturno a las estadísticas");
            }

            // Actualizar la visualización
            panelEstadisticas.revalidate();
            panelEstadisticas.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al mostrar estadísticas: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Guarda una nueva observación para un alumno.
     */
    private void guardarNuevaObservacion() {
        int fila = tablaAsistencia.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un alumno");
            return;
        }

        String observacion = txtNuevaObservacion.getText().trim();
        if (observacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, escriba una observación");
            return;
        }

        try {
            // Obtener el ID del alumno directamente de la tabla
            int alumnoId = Integer.parseInt(tableModel.getValueAt(fila, 3).toString());

            // Guardar la observación
            String query = "INSERT INTO observaciones_asistencia (alumno_id, fecha, observacion, creado_por) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            ps.setString(3, observacion);
            ps.setInt(4, usuarioId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Observación guardada exitosamente");
            txtNuevaObservacion.setText("");
            cargarObservacionesAlumno();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar observación: " + ex.getMessage());
        }
    }

    /**
     * Carga las observaciones existentes de un alumno.
     */
    private void cargarObservacionesAlumno() {
        int fila = tablaAsistencia.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un alumno");
            return;
        }

        try {
            // Obtener el ID del alumno directamente de la tabla
            int alumnoId = Integer.parseInt(tableModel.getValueAt(fila, 3).toString());

            // Buscar las observaciones con el ID
            String query = "SELECT id, fecha, observacion FROM observaciones_asistencia WHERE alumno_id = ? ORDER BY fecha DESC";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();

            StringBuilder observaciones = new StringBuilder();
            while (rs.next()) {
                observaciones.append("ID: ").append(rs.getInt("id"))
                        .append(" | Fecha: ").append(rs.getDate("fecha"))
                        .append("\n")
                        .append(rs.getString("observacion"))
                        .append("\n\n");
            }

            txtObservacionesExistentes.setText(observaciones.toString());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar observaciones: " + ex.getMessage());
        }
    }

    /**
     * Edita una observación existente.
     */
    private void editarObservacionSeleccionada() {
        String texto = txtObservacionesExistentes.getText();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay observaciones para editar");
            return;
        }

        // Obtener ID de la observación
        String idStr = JOptionPane.showInputDialog("Ingrese el ID de la observación a editar:");
        if (idStr == null || idStr.isEmpty()) {
            return;
        }

        try {
            int observacionId = Integer.parseInt(idStr);
            String query = "SELECT observacion FROM observaciones_asistencia WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, observacionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String observacionActual = rs.getString("observacion");
                String nuevaObservacion = JOptionPane.showInputDialog(this,
                        "Editar observación:", observacionActual);

                if (nuevaObservacion != null && !nuevaObservacion.isEmpty()) {
                    query = "UPDATE observaciones_asistencia SET observacion = ? WHERE id = ?";
                    ps = conect.prepareStatement(query);
                    ps.setString(1, nuevaObservacion);
                    ps.setInt(2, observacionId);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Observación actualizada exitosamente");
                    cargarObservacionesAlumno();
                }
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error al editar observación: " + ex.getMessage());
        }
    }

    /**
     * Elimina una observación existente.
     */
    private void eliminarObservacionSeleccionada() {
        String texto = txtObservacionesExistentes.getText();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay observaciones para eliminar");
            return;
        }

        String idStr = JOptionPane.showInputDialog("Ingrese el ID de la observación a eliminar:");
        if (idStr == null || idStr.isEmpty()) {
            return;
        }

        try {
            int observacionId = Integer.parseInt(idStr);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea eliminar esta observación?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String query = "DELETE FROM observaciones_asistencia WHERE id = ?";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, observacionId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Observación eliminada exitosamente");
                cargarObservacionesAlumno();
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar observación: " + ex.getMessage());
        }
    }

    /**
     * Configura aspectos responsive del panel.
     */
    

    /**
     * Configura el scrolling y la tabla para asegurar visibilidad.
     */
    public void configurarScrollingYTabla() {
        // Establecer un tamaño grande para este panel
        setPreferredSize(new Dimension(1500, 1200));
        setMinimumSize(new Dimension(1000, 900));

        // Configurar la tabla y su ScrollPane
        if (tablaAsistencia != null && scrollPane != null) {
            // Configurar viewport para la tabla
            tablaAsistencia.setPreferredScrollableViewportSize(new Dimension(900, 400));
            tablaAsistencia.setFillsViewportHeight(true);

            // ScrollPane con barras siempre visibles
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

            // Modo de redimensionamiento para permitir scroll horizontal
            tablaAsistencia.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        // Asegurar visibilidad y tamaño de paneles internos
        if (jPanel1 != null) {
            jPanel1.setVisible(true);
        }

        if (jPanel2 != null) {
            jPanel2.setVisible(true);
            jPanel2.setPreferredSize(new Dimension(900, 400));
        }

        if (jPanel3 != null) {
            jPanel3.setVisible(true);
        }

        // Configuración especial para el panel de observaciones
        if (panelObservacionesCompleto != null) {
            panelObservacionesCompleto.setVisible(true);
            panelObservacionesCompleto.setPreferredSize(new Dimension(400, 600));
            panelObservacionesCompleto.setName("panelObservaciones");

            // Asegurar que todos los componentes sean visibles
            for (Component comp : panelObservacionesCompleto.getComponents()) {
                comp.setVisible(true);
            }
        }

        if (panelEstadisticas != null) {
            panelEstadisticas.setVisible(true);
        }

        // Forzar actualización
        revalidate();
        repaint();

        System.out.println("Panel configurado con tamaño: " + getPreferredSize().width + "x" + getPreferredSize().height);
    }

    /**
     * Ajusta los componentes según el tamaño del panel.
     */
    private void ajustarComponentesSegunTamano() {
        int anchoTotal = getWidth();

        // Ajustar panel de observaciones
        if (panelObservacionesCompleto != null) {
            int anchoObservaciones = Math.max(250, anchoTotal < 1000 ? anchoTotal / 4 : anchoTotal / 3);
            panelObservacionesCompleto.setPreferredSize(new Dimension(
                    anchoObservaciones, panelObservacionesCompleto.getHeight()));
        }

        // Ajustar tabla
        if (tablaAsistencia != null && tablaAsistencia.getColumnCount() > 0) {
            // Ajustar altura de filas
            tablaAsistencia.setRowHeight(anchoTotal < 800 ? 25 : 30);
        }

        // Actualizar UI
        revalidate();
        repaint();
    }

    public JDateChooser getDateChooser() {
        return this.dateChooser;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblCurso = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        tablaAsistencia = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnExportar = new javax.swing.JButton();
        btnImportarDeMateria = new javax.swing.JButton();
        panelObservacionesCompleto = new javax.swing.JPanel();
        lblObservaciones = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtNuevaObservacion = new javax.swing.JTextArea();
        lblObservaciones1 = new javax.swing.JLabel();
        btnGuardarObs = new javax.swing.JButton();
        scrollObservaciones = new javax.swing.JScrollPane();
        txtObservacionesExistentes = new javax.swing.JTextArea();
        btnVerObservaciones = new javax.swing.JButton();
        btnEditarObservacion = new javax.swing.JButton();
        btnEliminarObservacion = new javax.swing.JButton();
        panelEstadisticas = new javax.swing.JPanel();
        lblEstadisticas = new javax.swing.JLabel();

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 10));

        lblCurso.setText("Curso");
        jPanel1.add(lblCurso);

        lblFecha.setText("Fecha");
        jPanel1.add(lblFecha);
        jPanel1.add(dateChooser);

        lblBuscar.setText("Buscar: ");
        jPanel1.add(lblBuscar);

        txtBuscar.setPreferredSize(new java.awt.Dimension(300, 25));
        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });
        jPanel1.add(txtBuscar);

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });
        jPanel1.add(btnBuscar);

        jPanel2.setLayout(new java.awt.BorderLayout());

        tablaAsistencia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollPane.setViewportView(tablaAsistencia);

        jPanel2.add(scrollPane, java.awt.BorderLayout.CENTER);

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        jPanel3.add(btnGuardar);

        btnCancelar.setText("Cancelar");
        jPanel3.add(btnCancelar);

        btnExportar.setText("Exportar a Excel");
        btnExportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarActionPerformed(evt);
            }
        });
        jPanel3.add(btnExportar);

        btnImportarDeMateria.setText("Importar Asistencia de Materia");
        btnImportarDeMateria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportarDeMateriaActionPerformed(evt);
            }
        });
        jPanel3.add(btnImportarDeMateria);

        lblObservaciones.setText("Observaciones:");

        txtNuevaObservacion.setColumns(20);
        txtNuevaObservacion.setRows(5);
        jScrollPane1.setViewportView(txtNuevaObservacion);

        lblObservaciones1.setText("Ver Observaciones:");

        btnGuardarObs.setText("Guardar Observación");
        btnGuardarObs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarObsActionPerformed(evt);
            }
        });

        txtObservacionesExistentes.setEditable(false);
        txtObservacionesExistentes.setColumns(20);
        txtObservacionesExistentes.setRows(5);
        scrollObservaciones.setViewportView(txtObservacionesExistentes);

        btnVerObservaciones.setText("Ver Observaciones");
        btnVerObservaciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerObservacionesActionPerformed(evt);
            }
        });

        btnEditarObservacion.setText("Editar");
        btnEditarObservacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarObservacionActionPerformed(evt);
            }
        });

        btnEliminarObservacion.setText("Eliminar");
        btnEliminarObservacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarObservacionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelObservacionesCompletoLayout = new javax.swing.GroupLayout(panelObservacionesCompleto);
        panelObservacionesCompleto.setLayout(panelObservacionesCompletoLayout);
        panelObservacionesCompletoLayout.setHorizontalGroup(
            panelObservacionesCompletoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelObservacionesCompletoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1))
            .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                .addGroup(panelObservacionesCompletoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(panelObservacionesCompletoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrollObservaciones)
                            .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                                .addComponent(btnVerObservaciones)
                                .addGap(79, 79, 79)
                                .addComponent(btnEditarObservacion)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnEliminarObservacion))))
                    .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                        .addGroup(panelObservacionesCompletoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addComponent(btnGuardarObs))
                            .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                                .addGap(75, 75, 75)
                                .addComponent(lblObservaciones))
                            .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                                .addGap(177, 177, 177)
                                .addComponent(lblObservaciones1)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelObservacionesCompletoLayout.setVerticalGroup(
            panelObservacionesCompletoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelObservacionesCompletoLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblObservaciones)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnGuardarObs)
                .addGap(26, 26, 26)
                .addComponent(lblObservaciones1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(scrollObservaciones, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelObservacionesCompletoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVerObservaciones)
                    .addComponent(btnEditarObservacion)
                    .addComponent(btnEliminarObservacion))
                .addGap(13, 13, 13))
        );

        lblEstadisticas.setText("Estadisticas");

        javax.swing.GroupLayout panelEstadisticasLayout = new javax.swing.GroupLayout(panelEstadisticas);
        panelEstadisticas.setLayout(panelEstadisticasLayout);
        panelEstadisticasLayout.setHorizontalGroup(
            panelEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadisticasLayout.createSequentialGroup()
                .addGap(129, 129, 129)
                .addComponent(lblEstadisticas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEstadisticasLayout.setVerticalGroup(
            panelEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadisticasLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblEstadisticas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelEstadisticas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addComponent(panelObservacionesCompleto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(panelEstadisticas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(panelObservacionesCompleto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarObsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarObsActionPerformed
        guardarNuevaObservacion();
    }//GEN-LAST:event_btnGuardarObsActionPerformed

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        String textoBusqueda = txtBuscar.getText();
        if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textoBusqueda, 0)); // 0 es la columna de nombre
            tablaAsistencia.setRowSorter(sorter);
        } else {
            tablaAsistencia.setRowSorter(null); // Quita el filtro si el campo está vacío
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnExportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarActionPerformed
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Excel");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".xlsx")) {
                    file = new File(file.getAbsolutePath() + ".xlsx");
                }

                try ( // Crear el libro de Excel
                        XSSFWorkbook workbook = new XSSFWorkbook()) {
                    XSSFSheet sheet = workbook.createSheet("Asistencia");
                    // Crear estilo para el encabezado
                    XSSFCellStyle headerStyle = workbook.createCellStyle();
                    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    // Crear encabezados
                    Row headerRow = sheet.createRow(0);
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(tableModel.getColumnName(i));
                        cell.setCellStyle(headerStyle);
                        sheet.autoSizeColumn(i);
                    }
                    // Agregar datos
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        Row row = sheet.createRow(i + 1);
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                            Cell cell = row.createCell(j);
                            Object value = tableModel.getValueAt(i, j);
                            if (value != null) {
                                cell.setCellValue(value.toString());
                            }
                        }
                    }
                    // Escribir el archivo
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        workbook.write(outputStream);
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Archivo exportado exitosamente en:\n" + file.getAbsolutePath(),
                        "Exportación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al exportar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnExportarActionPerformed

    private void btnVerObservacionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerObservacionesActionPerformed
        cargarObservacionesAlumno();
    }//GEN-LAST:event_btnVerObservacionesActionPerformed

    private void btnEditarObservacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarObservacionActionPerformed
        editarObservacionSeleccionada();
    }//GEN-LAST:event_btnEditarObservacionActionPerformed

    private void btnEliminarObservacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarObservacionActionPerformed
        eliminarObservacionSeleccionada();
    }//GEN-LAST:event_btnEliminarObservacionActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        try {
            System.out.println("Botón Guardar presionado");
            guardarAsistencias();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(AsistenciaPreceptorPanel.this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnImportarDeMateriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportarDeMateriaActionPerformed
        //importarAsistenciaProfesor();
    }//GEN-LAST:event_btnImportarDeMateriaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEditarObservacion;
    private javax.swing.JButton btnEliminarObservacion;
    private javax.swing.JButton btnExportar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnGuardarObs;
    private javax.swing.JButton btnImportarDeMateria;
    private javax.swing.JButton btnVerObservaciones;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblCurso;
    private javax.swing.JLabel lblEstadisticas;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblObservaciones1;
    private javax.swing.JPanel panelEstadisticas;
    private javax.swing.JPanel panelObservacionesCompleto;
    private javax.swing.JScrollPane scrollObservaciones;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable tablaAsistencia;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextArea txtNuevaObservacion;
    private javax.swing.JTextArea txtObservacionesExistentes;
    // End of variables declaration//GEN-END:variables
}
