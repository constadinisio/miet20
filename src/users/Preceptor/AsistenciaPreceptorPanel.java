package users.Preceptor;

import java.sql.*;
import java.time.LocalDate;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.*;
import users.common.AsistenciaPanel;
import login.Conexion;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JFileChooser;  // Para el selector de archivos
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import users.common.EstadoAsistenciaEditor;

/**
 * Panel de asistencia para preceptores, que permite gestionar y visualizar la
 * asistencia de los alumnos de un curso específico.
 *
 * Características principales: - Carga y muestra asistencias por curso -
 * Permite agregar y editar observaciones de asistencia - Exportación de datos a
 * Excel - Filtrado y búsqueda de alumnos
 *
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [3/12/2025]
 */
public class AsistenciaPreceptorPanel extends AsistenciaPanel {

    // ID del curso asociado al preceptor
    private int cursoId;

    /**
     * Constructor del panel de asistencia para preceptores.
     *
     * Inicializa componentes, configura la interfaz y carga datos iniciales: -
     * Conexión a base de datos - Configuración de componentes visuales - Carga
     * de datos del curso - Configuración de eventos
     *
     * @param preceptorId Identificador del preceptor
     * @param cursoId Identificador del curso
     */
    public AsistenciaPreceptorPanel(int preceptorId, int cursoId) {
        // Implementación original del constructor
        // Inicializa conexión, componentes, fechas y configuraciones
        super();
        
        conect = Conexion.getInstancia().verificarConexion();
        
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        initComponents();
        this.usuarioId = preceptorId;
        this.cursoId = cursoId;
        this.fecha = LocalDate.now();

        // Ajustar a lunes de la semana actual
        while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
            fecha = fecha.minusDays(1);
        }

        // Configurar tamaños y layouts
        setPreferredSize(new Dimension(1200, 800)); // Ajusta según necesites

        inicializarBase();
        cargarDatosCurso();
        cargarAsistencias();
        configurarEventos();
        agregarFiltroBusqueda();
        agregarObservaciones();
        configurarDateChooser();
    }

    public void ajustarPanelParaScroll() {
        // Asegurarnos que el panel tenga un tamaño preferido adecuado
        setPreferredSize(new Dimension(900, 700)); // Ajustar según necesidades

        // Asegurarnos que todos los sub-paneles sean visibles
        if (jPanel1 != null) {
            jPanel1.setVisible(true);
        }
        if (jPanel2 != null) {
            jPanel2.setVisible(true);
        }
        if (jPanel3 != null) {
            jPanel3.setVisible(true);
        }
        if (panelObservacionesCompleto != null) {
            panelObservacionesCompleto.setVisible(true);
        }
        if (panelEstadisticas != null) {
            panelEstadisticas.setVisible(true);
        }

        // Si el panel usa GroupLayout, necesitamos asegurarnos que tenga tamaño adecuado
        // Esto es crítico si estás usando el diseñador de NetBeans con GroupLayout
        validate();
    }

    /**
     * Carga los datos del curso asociado al preceptor.
     * Recupera el año y división del curso desde la base de datos.
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
     * Configura el modelo de tabla, colores y componentes visuales.
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


    // Agregar este método a la clase AsistenciaPreceptorPanel
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600); // Tamaño base que permitirá scroll si es necesario
    }
    
    /**
     * Define la configuración de la tabla usada en la interfaz.
     */
    @Override
    protected void configurarTabla() {
        if (tableModel == null) {
            tableModel = new DefaultTableModel();
        }

        // Configurar columnas
        tableModel.addColumn("Alumno");
        tableModel.addColumn("DNI");

        // Agregar columnas para cada día de la semana (turno y contraturno)
        LocalDate diaActual = fecha; // Empezar desde el lunes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM (EEE)");
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 5; i++) { // De lunes a viernes
            String nombreColumna = diaActual.format(formatter);
            tableModel.addColumn(nombreColumna);
            tableModel.addColumn(nombreColumna + " (Cont)");
            diaActual = diaActual.plusDays(1);
        }

        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);

            // Configurar el editor para las columnas de asistencia
            for (int i = 2; i < tablaAsistencia.getColumnCount(); i++) {
                TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);
                column.setCellEditor(new EstadoAsistenciaEditor());

                // Resaltar visualmente las columnas del día actual
                if (i >= 2) {
                    int diaColumna = ((i - 2) / 2) + 1; // Determinar el día (1-5) de la columna
                    LocalDate fechaColumna = fecha.plusDays(diaColumna - 1);

                    if (fechaColumna.equals(hoy)) {
                        // Aplicar un renderizador especial para resaltar el día actual
                        column.setCellRenderer(new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                                if (!isSelected) {
                                    // Aplicar el color del estado y un borde más grueso
                                    String estado = value != null ? value.toString() : "NC";
                                    c.setBackground(colorEstados.getOrDefault(estado, Color.WHITE));

                                    if (c instanceof JComponent) {
                                        ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                                    }
                                }

                                return c;
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Define los colores asociados a los diferentes estados de asistencia.
     */
    private void configurarEventos() {
        // Configurar event listener para el dateChooser
        if (dateChooser.getDate() != null) {
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

    protected void cargarAsistencias() {
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
            while (rsAlumnos.next()) {
                Object[] rowData = new Object[12]; // 2 columnas fijas + 5 días x 2 turnos
                rowData[0] = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                rowData[1] = rsAlumnos.getString("dni");

                // Inicializar todos los estados como NC
                for (int i = 2; i < rowData.length; i++) {
                    rowData[i] = "NC";
                }

                tableModel.addRow(rowData);
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
                String dni = rsAsistencias.getString("dni");
                LocalDate fechaAsistencia = rsAsistencias.getDate("fecha").toLocalDate();
                String estado = rsAsistencias.getString("estado");
                boolean esContraturno = rsAsistencias.getBoolean("es_contraturno");

                // Calcular la columna correspondiente
                // Primero, calcular el offset de días desde el inicio de la semana
                int diaOffset = fechaAsistencia.getDayOfWeek().getValue() - 1; // 0 para lunes, 4 para viernes
                int columnaBase = 2 + (diaOffset * 2); // 2 columnas iniciales + 2 por cada día
                int columna = columnaBase + (esContraturno ? 1 : 0); // Agregar 1 si es contraturno

                System.out.println("Cargando: DNI=" + dni + ", Fecha=" + fechaAsistencia
                        + ", Estado=" + estado + ", Contraturno=" + esContraturno
                        + ", Día=" + diaOffset + ", Columna=" + columna);

                // Buscar la fila por DNI y actualizar el estado
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1).toString().equals(dni)) {
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

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

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

     private void importarAsistenciaProfesor() {
        try {
            // Seleccionar día y materia
            LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
            String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
            JComboBox<String> comboDias = new JComboBox<>(dias);

            String queryMaterias
                    = "SELECT m.id, m.nombre FROM materias m "
                    + "JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id "
                    + "WHERE pcm.curso_id = ?";

            PreparedStatement ps = conect.prepareStatement(queryMaterias);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, Integer> materiaIds = new HashMap<>();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                comboMaterias.addItem(nombre);
                materiaIds.put(nombre, rs.getInt("id"));
            }

            JPanel panel = new JPanel(new GridLayout(4, 1));
            panel.add(new JLabel("Seleccione el día:"));
            panel.add(comboDias);
            panel.add(new JLabel("Seleccione la materia:"));
            panel.add(comboMaterias);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Importar Asistencia", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                int diaSeleccionado = comboDias.getSelectedIndex();
                String materiaSeleccionada = comboMaterias.getSelectedItem().toString();
                int materiaId = materiaIds.get(materiaSeleccionada);
                LocalDate fechaSeleccionada = inicioSemana.plusDays(diaSeleccionado);

                // Importar asistencias
                String queryImportar
                        = "INSERT INTO asistencia_general (alumno_id, curso_id, fecha, estado, creado_por, es_contraturno) "
                        + "SELECT alumno_id, curso_id, fecha, estado, creado_por, TRUE "
                        + "FROM asistencia_materia "
                        + "WHERE curso_id = ? AND materia_id = ? AND fecha = ?";

                PreparedStatement psImportar = conect.prepareStatement(queryImportar);
                psImportar.setInt(1, cursoId);
                psImportar.setInt(2, materiaId);
                psImportar.setDate(3, java.sql.Date.valueOf(fechaSeleccionada));

                psImportar.executeUpdate();

                JOptionPane.showMessageDialog(this, "Asistencias importadas exitosamente");
                cargarAsistencias();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al importar asistencias: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarObservaciones() {
        JPanel panelObservaciones = new JPanel();
        JTextArea txtObservaciones = new JTextArea(3, 40);
        JButton btnGuardarObs = new JButton("Guardar Observación");

        btnGuardarObs.addActionListener(e -> {
            int fila = tablaAsistencia.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un alumno");
                return;
            }

            try {
                String query = "INSERT INTO observaciones_asistencia (alumno_id, fecha, observacion, creado_por) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, Integer.parseInt(tableModel.getValueAt(fila, 1).toString())); // DNI/ID
                ps.setDate(2, java.sql.Date.valueOf(fecha));
                ps.setString(3, txtObservaciones.getText());
                ps.setInt(4, usuarioId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Observación guardada");
                txtObservaciones.setText("");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar observación: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelObservaciones.add(new JScrollPane(txtObservaciones));
        panelObservaciones.add(btnGuardarObs);
        add(panelObservaciones, BorderLayout.EAST);
    }

    private void exportarDatos() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Excel");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".xlsx")) {
                    file = new File(file.getAbsolutePath() + ".xlsx");
                }

                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet sheet = workbook.createSheet("Asistencia");

                // Crear encabezados
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(tableModel.getColumnName(i));
                }

                // Agregar datos
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(tableModel.getValueAt(i, j).toString());
                    }
                }

                // Guardar archivo
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                JOptionPane.showMessageDialog(this, "Datos exportados exitosamente");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al exportar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
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

    private void mostrarEstadisticas() {
        JPanel panelEstadisticas = new JPanel();
        int presentes = 0, ausentes = 0, tarde = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String estado = (String) tableModel.getValueAt(i, 2);
            switch (estado) {
                case "P":
                    presentes++;
                    break;
                case "A":
                    ausentes++;
                    break;
                case "T":
                    tarde++;
                    break;
            }
        }

        int total = tableModel.getRowCount();
        double porcentajePresentes = (presentes * 100.0) / total;

        JLabel lblEstadisticas = new JLabel(String.format(
                "Presentes: %d (%2.1f%%) | Ausentes: %d | Tarde: %d",
                presentes, porcentajePresentes, ausentes, tarde));

        panelEstadisticas.add(lblEstadisticas);
        add(panelEstadisticas, BorderLayout.SOUTH);
    }

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
                String valor = entry.getStringValue(0).toLowerCase()
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

    private void filtrarTabla(String texto) {
        texto = texto.toLowerCase();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0)); // Filtra por columna nombre
        tablaAsistencia.setRowSorter(sorter);
    }

    protected void guardarAsistencias() {
        try {
            // Determinar qué día se está modificando
            LocalDate fechaGuardar = getDiaSeleccionado();
            int diaOffset = fechaGuardar.getDayOfWeek().getValue() - 1; // 0 para lunes, 4 para viernes
            int diaActual = diaOffset + 1; // 1 para lunes, 5 para viernes

            System.out.println("Guardando asistencias para el día: " + diaActual
                    + " (Fecha a guardar: " + fechaGuardar + ")");

            // Verificar si se están modificando registros existentes
            String checkQuery = "SELECT COUNT(*) as count FROM asistencia_general "
                    + "WHERE fecha = ? AND curso_id = ?";
            PreparedStatement checkPs = conect.prepareStatement(checkQuery);
            checkPs.setDate(1, java.sql.Date.valueOf(fechaGuardar));
            checkPs.setInt(2, cursoId);
            ResultSet checkRs = checkPs.executeQuery();

            boolean modificandoExistentes = false;
            if (checkRs.next()) {
                modificandoExistentes = checkRs.getInt("count") > 0;
            }

            // Verificar si es una fecha pasada, pero solo si no estamos modificando registros existentes
            LocalDate hoy = LocalDate.now();
            if (fechaGuardar.isBefore(hoy) && !modificandoExistentes) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "Está intentando registrar asistencias para una fecha anterior ("
                        + fechaGuardar.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + "). ¿Desea continuar?",
                        "Confirmar registro", JOptionPane.YES_NO_OPTION);

                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Calcular la columna base para este día
            int columnaBase = 2 + (diaOffset * 2); // 2 columnas iniciales + 2 por cada día
            System.out.println("Columna base calculada: " + columnaBase);

            // Primero, eliminar asistencias existentes SOLO para esta fecha específica y curso
            String deleteQuery = "DELETE FROM asistencia_general WHERE fecha = ? AND curso_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setDate(1, java.sql.Date.valueOf(fechaGuardar));
            deletePs.setInt(2, cursoId);
            int registrosEliminados = deletePs.executeUpdate();
            System.out.println("Registros eliminados para fecha " + fechaGuardar + ": " + registrosEliminados);

            // Consulta para obtener el ID del usuario a partir de su DNI
            String queryUsuarioId = "SELECT id FROM usuarios WHERE dni = ?";
            PreparedStatement psUsuarioId = conect.prepareStatement(queryUsuarioId);

            // Insertar nuevas asistencias
            String insertQuery = "INSERT INTO asistencia_general "
                    + "(alumno_id, curso_id, fecha, estado, creado_por, es_contraturno) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);

            // Para cada alumno en la tabla
            int registrosInsertados = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String dni = tableModel.getValueAt(i, 1).toString();

                // Obtener el ID del alumno a partir del DNI
                psUsuarioId.setString(1, dni);
                ResultSet rsUsuario = psUsuarioId.executeQuery();

                if (rsUsuario.next()) {
                    int alumnoId = rsUsuario.getInt("id");

                    // Solo procesar columnas correspondientes al día actual
                    // Guardar asistencia del turno normal
                    if (columnaBase < tableModel.getColumnCount()) {
                        String estado = tableModel.getValueAt(i, columnaBase).toString();
                        if (!estado.equals("NC")) {
                            System.out.println("Guardando turno normal - DNI: " + dni
                                    + ", ID: " + alumnoId + ", Estado: " + estado);

                            insertPs.setInt(1, alumnoId);
                            insertPs.setInt(2, cursoId);
                            insertPs.setDate(3, java.sql.Date.valueOf(fechaGuardar));
                            insertPs.setString(4, estado);
                            insertPs.setInt(5, usuarioId);
                            insertPs.setBoolean(6, false); // No es contraturno
                            insertPs.executeUpdate();
                            registrosInsertados++;
                        }
                    }

                    // Guardar asistencia del contraturno
                    if (columnaBase + 1 < tableModel.getColumnCount()) {
                        String estado = tableModel.getValueAt(i, columnaBase + 1).toString();
                        if (!estado.equals("NC")) {
                            System.out.println("Guardando contraturno - DNI: " + dni
                                    + ", ID: " + alumnoId + ", Estado: " + estado);

                            insertPs.setInt(1, alumnoId);
                            insertPs.setInt(2, cursoId);
                            insertPs.setDate(3, java.sql.Date.valueOf(fechaGuardar));
                            insertPs.setString(4, estado);
                            insertPs.setInt(5, usuarioId);
                            insertPs.setBoolean(6, true); // Es contraturno
                            insertPs.executeUpdate();
                            registrosInsertados++;
                        }
                    }
                } else {
                    System.out.println("ADVERTENCIA: No se encontró ID para alumno con DNI: " + dni);
                }
            }

            System.out.println("Total de registros insertados: " + registrosInsertados);
            JOptionPane.showMessageDialog(this, "Asistencias guardadas exitosamente");

            // Recargar los datos para mostrar los cambios
            cargarAsistencias();

            // Actualizar estadísticas para la fecha que acabamos de guardar
            actualizarEstadisticas(fechaGuardar);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private LocalDate getDiaSeleccionado() {
        // Obtener la columna seleccionada actualmente
        int columnaSeleccionada = tablaAsistencia.getSelectedColumn();

        // Si no hay columna seleccionada o es una columna no de asistencia, usar la fecha actual
        if (columnaSeleccionada < 2) {
            LocalDate hoy = LocalDate.now();
            // Si hoy está en la semana actual, usar hoy, sino usar el lunes
            if (hoy.isAfter(fecha.minusDays(1)) && hoy.isBefore(fecha.plusDays(5))) {
                int diaOffset = hoy.getDayOfWeek().getValue() - 1; // 0=lunes, 1=martes, etc.
                return fecha.plusDays(diaOffset);
            } else {
                return fecha; // Usar el lunes como predeterminado
            }
        }

        // Calcular el día de la semana basado en la columna
        int diaOffset = (columnaSeleccionada - 2) / 2; // 0=lunes, 1=martes, etc.

        // Calcular la fecha: fecha base (lunes) + offset
        LocalDate diaSeleccionado = fecha.plusDays(diaOffset);
        return diaSeleccionado;
    }

    protected boolean puedeEditarCelda(int row, int column) {
        return column == 2;  // Solo la columna de estado es editable
    }

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
            // Primero obtener el ID del alumno usando su DNI
            String dni = tableModel.getValueAt(fila, 1).toString();
            String queryId = "SELECT id FROM usuarios WHERE dni = ?";
            PreparedStatement psId = conect.prepareStatement(queryId);
            psId.setString(1, dni);
            ResultSet rs = psId.executeQuery();

            if (rs.next()) {
                int alumnoId = rs.getInt("id");

                // Ahora sí, guardar la observación usando el ID correcto
                String query = "INSERT INTO observaciones_asistencia (alumno_id, fecha, observacion, creado_por) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, alumnoId); // Usamos el ID en lugar del DNI
                ps.setDate(2, java.sql.Date.valueOf(fecha));
                ps.setString(3, observacion);
                ps.setInt(4, usuarioId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Observación guardada exitosamente");
                txtNuevaObservacion.setText("");
                cargarObservacionesAlumno();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo encontrar el ID del alumno");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar observación: " + ex.getMessage());
        }
    }

    private void cargarObservacionesAlumno() {
        int fila = tablaAsistencia.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un alumno");
            return;
        }

        try {
            // Primero obtener el ID del alumno usando su DNI
            String dni = tableModel.getValueAt(fila, 1).toString();
            String queryId = "SELECT id FROM usuarios WHERE dni = ?";
            PreparedStatement psId = conect.prepareStatement(queryId);
            psId.setString(1, dni);
            ResultSet rsId = psId.executeQuery();

            if (rsId.next()) {
                int alumnoId = rsId.getInt("id");

                // Ahora buscar las observaciones con el ID correcto
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
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar observaciones: " + ex.getMessage());
        }
    }

    private void editarObservacionSeleccionada() {
        String texto = txtObservacionesExistentes.getText();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay observaciones para editar");
            return;
        }

        // Obtener ID de la observación seleccionada (puedes mejorar esto con una lista o tabla)
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
        txtObservacionesExistentes.setColumns(40);
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEditarObservacion;
    private javax.swing.JButton btnEliminarObservacion;
    private javax.swing.JButton btnExportar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnGuardarObs;
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
