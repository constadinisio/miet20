/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
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

public class AsistenciaPreceptorPanel extends AsistenciaPanel {

    private int cursoId;

    public AsistenciaPreceptorPanel(int preceptorId, int cursoId) {
        super();
        conect = Conexion.getInstancia().getConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
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
        txtBuscar.setPreferredSize(new Dimension(400, 30));
        jPanel1.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inicializarBase();
        cargarDatosCurso();
        cargarAsistencias();
        configurarEventos();
        agregarFiltroBusqueda();
        mostrarEstadisticas();
        agregarObservaciones();
        configurarDateChooser();

        txtNuevaObservacion.setPreferredSize(new Dimension(500, 200));
        txtNuevaObservacion.setLineWrap(true);
        txtNuevaObservacion.setWrapStyleWord(true);

// Para los botones
        btnBuscar.setPreferredSize(new Dimension(100, 30));
        btnGuardarObs.setPreferredSize(new Dimension(150, 30));
        btnExportar.setPreferredSize(new Dimension(150, 30));

        btnEditarObservacion.setEnabled(false);  // Deshabilitar hasta que se seleccione una fila
        btnEliminarObservacion.setEnabled(false);

// Agregar listener para la selección en la tabla
        tablaAsistencia.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tablaAsistencia.getSelectedRow() != -1;
            btnEditarObservacion.setEnabled(haySeleccion);
            btnEliminarObservacion.setEnabled(haySeleccion);
        });

        setLayout(new BorderLayout(5, 5)); // 5 píxeles de separación entre componentes

// Configurar tamaños
        panelObservacionesCompleto.setPreferredSize(new Dimension(400, 0));
        jPanel2.setPreferredSize(new Dimension(1500, 0));

// Agregar componentes
        add(jPanel1, BorderLayout.NORTH);
        add(jPanel2, BorderLayout.CENTER);
        add(jPanel3, BorderLayout.SOUTH);
        add(panelObservacionesCompleto, BorderLayout.EAST);

    }

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

    private void inicializarBase() {
        // Inicializar tabla y modelo
        this.tableModel = new DefaultTableModel();
        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);
        }
        inicializarColores();
        configurarTabla();
    }

    private void inicializarColores() {
        colorEstados = new HashMap<>();
        colorEstados.put("P", new Color(144, 238, 144));  // Verde claro
        colorEstados.put("A", new Color(255, 182, 193));  // Rojo claro
        colorEstados.put("T", new Color(255, 255, 153));  // Amarillo claro
        colorEstados.put("AP", new Color(255, 218, 185)); // Naranja claro
        colorEstados.put("NC", Color.WHITE);
    }

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

        for (int i = 0; i < 5; i++) { // De lunes a viernes
            String nombreColumna = diaActual.format(formatter);
            tableModel.addColumn(nombreColumna );
            tableModel.addColumn(nombreColumna + " (Cont)");
            diaActual = diaActual.plusDays(1);
        }

        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);

            // Configurar el editor para las columnas de asistencia
            for (int i = 2; i < tablaAsistencia.getColumnCount(); i++) {
                TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);
                column.setCellEditor(new EstadoAsistenciaEditor());
            }
        }
    }

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

        // Configurar event listeners para los botones
        if (btnGuardar != null) {
            btnGuardar.addActionListener(e -> guardarAsistencias());
        }

        if (btnCancelar != null) {
            btnCancelar.addActionListener(e -> cargarAsistencias());
        }
        if (btnExportar != null) {
            btnExportar.addActionListener(e -> exportarDatos());
        }
    }

    protected void cargarAsistencias() {
        try {
            tableModel.setRowCount(0);

            // Cargar alumnos
            String queryAlumnos
                    = "SELECT u.id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
            LocalDate finSemana = inicioSemana.plusDays(4);

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

            // Cargar asistencias del turno normal
            String queryAsistencias
                    = "SELECT alumno_id, fecha, estado, es_contraturno "
                    + "FROM asistencia_general "
                    + "WHERE curso_id = ? AND fecha BETWEEN ? AND ?";

            PreparedStatement psAsistencias = conect.prepareStatement(queryAsistencias);
            psAsistencias.setInt(1, cursoId);
            psAsistencias.setDate(2, java.sql.Date.valueOf(inicioSemana));
            psAsistencias.setDate(3, java.sql.Date.valueOf(finSemana));

            ResultSet rsAsistencias = psAsistencias.executeQuery();
            while (rsAsistencias.next()) {
                String dni = rsAsistencias.getString("alumno_id");
                LocalDate fechaAsistencia = rsAsistencias.getDate("fecha").toLocalDate();
                String estado = rsAsistencias.getString("estado");
                boolean esContraturno = rsAsistencias.getBoolean("es_contraturno");

                // Calcular la columna correspondiente
                int diaOffset = fechaAsistencia.getDayOfWeek().getValue() - 1; // 0 para lunes, 4 para viernes
                int columnaBase = 2 + (diaOffset * 2); // 2 columnas iniciales + 2 por cada día
                int columna = columnaBase + (esContraturno ? 1 : 0); // Agregar 1 si es contraturno

                // Buscar la fila del alumno y actualizar el estado
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1).toString().equals(dni)) {
                        tableModel.setValueAt(estado, i, columna);
                        break;
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
        }
    }

    private void configurarDateChooser() {
        // Establecer la fecha inicial
        dateChooser.setDate(java.sql.Date.valueOf(fecha));

        // Agregar el listener
        dateChooser.addPropertyChangeListener("date", evt -> {
            if ("date".equals(evt.getPropertyName()) && dateChooser.getDate() != null) {
                // Actualizar la fecha
                fecha = dateChooser.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                // Ajustar al lunes de la semana
                while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                    fecha = fecha.minusDays(1);
                }

                // Recargar la tabla
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);
                configurarTabla();
                cargarAsistencias();

                // Debug
                System.out.println("Fecha seleccionada: " + fecha);
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
        JPanel panelBusqueda = new JPanel();
        JLabel lblBuscar = new JLabel("Buscar Alumno:");
        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(300, 30)); // Tamaño más grande

        btnBuscar = new JButton("Buscar");
        btnBuscar.setPreferredSize(new Dimension(100, 30));

        btnBuscar.addActionListener(e -> realizarBusqueda());
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

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        add(panelBusqueda, BorderLayout.NORTH);
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
            // Eliminar asistencias existentes
            String deleteQuery
                    = "DELETE FROM asistencia_general WHERE fecha = ? AND curso_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setDate(1, java.sql.Date.valueOf(fecha));
            deletePs.setInt(2, cursoId);
            deletePs.executeUpdate();

            // Insertar nuevas asistencias
            String insertQuery
                    = "INSERT INTO asistencia_general "
                    + "(alumno_id, curso_id, fecha, estado, creado_por) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String estado = tableModel.getValueAt(i, 2).toString();
                if (!estado.equals("NC")) {
                    insertPs.setString(1, tableModel.getValueAt(i, 1).toString());
                    insertPs.setInt(2, cursoId);
                    insertPs.setDate(3, java.sql.Date.valueOf(fecha));
                    insertPs.setString(4, estado);
                    insertPs.setInt(5, usuarioId);
                    insertPs.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Asistencias guardadas exitosamente");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
        }
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
