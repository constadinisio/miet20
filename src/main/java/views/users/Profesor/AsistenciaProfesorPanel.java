package main.java.views.users.Profesor;

import com.toedter.calendar.JDateChooser;
import main.java.views.users.common.AsistenciaPanel;
import main.java.views.users.common.EstadoAsistenciaEditor;
import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import java.util.Map;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel de asistencia específico para profesores.
 *
 * Permite gestionar la asistencia de alumnos para una materia y curso
 * específicos.
 *
 * Características principales: - Gestión de asistencia por materia y curso -
 * Visualización y edición de estados de asistencia - Importación de asistencias
 * generales - Validación de días de clase
 *
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [3/12/2025]
 */
public class AsistenciaProfesorPanel extends AsistenciaPanel {

    // Identificadores de curso, materia y profesor
    private int cursoId;
    private int materiaId;

    // Conjunto de días de clase para la materia actual
    private Set<LocalDate> diasClase;

    // Nombres de materia y curso
    private String nombreMateria;
    private String nombreCurso;

    /**
     * Constructor del panel de asistencia para profesores. Modificado para usar
     * buscarAsistencia().
     *
     * @param profesorId Identificador del profesor
     * @param cursoId Identificador del curso
     * @param materiaId Identificador de la materia
     */
    public AsistenciaProfesorPanel(int profesorId, int cursoId, int materiaId) {
        super();
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
        }

        System.out.println("Creando panel con:");
        System.out.println("Profesor ID: " + profesorId);
        System.out.println("Curso ID: " + cursoId);
        System.out.println("Materia ID: " + materiaId);

        initComponents();
        this.usuarioId = profesorId;
        this.cursoId = cursoId;
        this.materiaId = materiaId;
        this.fecha = LocalDate.now();

        inicializarBase();
        this.diasClase = obtenerDiasClase();
        cargarDatosMateriaCurso();
        // cargarAsistencias();  // Comentado para evitar duplicidad
        buscarAsistencia();     // Usar el nuevo método mejorado
        configurarEventos();
    }

    /**
     * Inicializa la base de la tabla de asistencia. Configura modelo de tabla,
     * colores y estructura.
     */
    private void inicializarBase() {
        this.tableModel = new DefaultTableModel();
        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);
        }
        inicializarColores();
        configurarTabla();
    }

    /**
     * Configura los eventos de interacción del panel. Maneja cambios en la
     * fecha y acciones de cancelación.
     */
    private void configurarEventos() {
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (dateChooser.getDate() != null) {
                fecha = dateChooser.getDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                buscarAsistencia(); // Usar buscarAsistencia en lugar de cargarAsistencias
            }
        });

        btnCancelar.addActionListener(e -> buscarAsistencia()); // Usar buscarAsistencia aquí también
    }

    /**
     * Inicializa un mapa de colores para representar los diferentes estados de
     * asistencia.
     */
    private void inicializarColores() {
        colorEstados = new HashMap<>();
        colorEstados.put("P", new Color(144, 238, 144));  // Verde claro
        colorEstados.put("A", new Color(255, 182, 193));  // Rojo claro
        colorEstados.put("T", new Color(255, 255, 153));  // Amarillo claro
        colorEstados.put("AP", new Color(255, 218, 185)); // Naranja claro
        colorEstados.put("NC", Color.WHITE);              // No cargado
    }

    /**
     * Obtiene los días de clase para la materia y curso actual.
     *
     * @return Conjunto de fechas correspondientes a los días de clase
     */
    private Set<LocalDate> obtenerDiasClase() {
        Set<LocalDate> dias = new HashSet<>();
        try {
            System.out.println("Obteniendo días de clase...");

            // Obtener el lunes de la semana actual
            LocalDate inicioDeSemana = fecha;
            while (inicioDeSemana.getDayOfWeek() != DayOfWeek.MONDAY) {
                inicioDeSemana = inicioDeSemana.minusDays(1);
            }

            System.out.println("Inicio de semana: " + inicioDeSemana);

            // Consulta a la base de datos
            String query = "SELECT dia_semana, hora_inicio, hora_fin FROM horarios_materia "
                    + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);

            ResultSet rs = ps.executeQuery();
            int contadorDias = 0;

            while (rs.next()) {
                contadorDias++;
                // Obtener el día como string para manejar ambos formatos (texto o número)
                String diaStr = rs.getString("dia_semana");

                // Convertir a un número de día de semana (1=Lunes, ..., 5=Viernes)
                int diaSemana;
                try {
                    // Intenta convertir directamente si es un número
                    diaSemana = Integer.parseInt(diaStr);
                } catch (NumberFormatException e) {
                    // Si es texto, conviértelo
                    diaSemana = convertirDiaSemanaANumero(diaStr);
                }

                System.out.println("Día encontrado: " + diaStr + " -> " + diaSemana);

                // Si es un día válido (1-5, Lunes a Viernes)
                if (diaSemana >= 1 && diaSemana <= 5) {
                    LocalDate fechaClase = inicioDeSemana.plusDays(diaSemana - 1);
                    dias.add(fechaClase);
                    System.out.println("Fecha de clase agregada: " + fechaClase);
                } else {
                    System.out.println("Día inválido: " + diaStr + " -> " + diaSemana);
                }
            }

            System.out.println("Total días de clase encontrados: " + contadorDias);
            System.out.println("Total fechas de clase en esta semana: " + dias.size());

        } catch (SQLException ex) {
            System.out.println("Error SQL: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al obtener días de clase: " + ex.getMessage());
        }

        return dias;
    }

// Método para convertir nombres de días a números
    private int convertirDiaSemanaANumero(String dia) {
        if (dia == null) {
            return -1;
        }

        dia = dia.trim().toLowerCase();

        switch (dia) {
            case "lunes":
                return 1;
            case "martes":
                return 2;
            case "miércoles":
            case "miercoles":
                return 3;
            case "jueves":
                return 4;
            case "viernes":
                return 5;
            default:
                return -1;
        }
    }

    /**
     * Verifica si la fecha seleccionada es un día de clase válido para la
     * materia actual.
     *
     * @param fecha Fecha a validar
     * @return true si es un día de clase, false en caso contrario
     */
    private boolean esDiaClaseValido(LocalDate fecha) {
        try {
            int diaSemana = fecha.getDayOfWeek().getValue(); // 1 para Lunes, 7 para Domingo

            System.out.println("Verificando si es día válido: " + fecha + " (día " + diaSemana + ")");

            // Modificar la consulta para manejar tanto números como texto
            String query = "SELECT COUNT(*) FROM horarios_materia "
                    + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? "
                    + "AND (dia_semana = ? OR dia_semana = ?)";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setString(4, String.valueOf(diaSemana)); // Como número
            ps.setString(5, getDiaSemanaTexto(diaSemana)); // Como texto

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Coincidencias encontradas: " + count);
                return count > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void configurarTabla() {
        if (tableModel == null) {
            tableModel = new DefaultTableModel();
        }

        System.out.println("Configurando tabla de asistencia...");

        // Limpiar el modelo completamente
        tableModel.setColumnCount(0);
        tableModel.setRowCount(0);

        // Configurar columnas fijas
        tableModel.addColumn("Alumno");
        tableModel.addColumn("DNI");

        // Determinar el lunes de la semana actual
        LocalDate inicioDeSemana = fecha;
        while (inicioDeSemana.getDayOfWeek() != DayOfWeek.MONDAY) {
            inicioDeSemana = inicioDeSemana.minusDays(1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM (EEE)");

        // Agregar todos los días de la semana (lunes a viernes)
        // Sin depender de diasClase
        for (int i = 0; i < 5; i++) {
            LocalDate diaActual = inicioDeSemana.plusDays(i);
            String nombreColumna = diaActual.format(formatter);
            tableModel.addColumn(nombreColumna);
            System.out.println("Agregada columna para día: " + nombreColumna);
        }

        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);

            // Configurar renderizadores para mostrar colores según estado
            for (int i = 2; i < tablaAsistencia.getColumnCount(); i++) {
                TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);
                column.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                        if (!isSelected) {
                            String estado = value != null ? value.toString() : "NC";
                            c.setBackground(colorEstados.getOrDefault(estado, Color.WHITE));
                        }

                        return c;
                    }
                });

                // Configurar editor para permitir cambios
                column.setCellEditor(new EstadoAsistenciaEditor());
            }
        }

        System.out.println("Tabla configurada con " + tableModel.getColumnCount() + " columnas");
    }

// Método auxiliar para convertir número a texto de día
    private String getDiaSemanaTexto(int dia) {
        switch (dia) {
            case 1:
                return "Lunes";
            case 2:
                return "Martes";
            case 3:
                return "Miércoles";
            case 4:
                return "Jueves";
            case 5:
                return "Viernes";
            default:
                return "";
        }
    }

    /**
     * Carga todos los datos entre materias y cursos para mostrarlos en la
     * tabla.
     */
    private void cargarDatosMateriaCurso() {
        try {
            // Cargar datos de materia y curso
            String query = "SELECT m.nombre as materia, CONCAT(c.anio, '°', c.division) as curso "
                    + "FROM materias m "
                    + "JOIN cursos c ON c.id = ? "
                    + "WHERE m.id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setInt(2, materiaId);

            System.out.println("Cargando datos de materia y curso...");
            System.out.println("CursoID: " + cursoId);
            System.out.println("MateriaID: " + materiaId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombreMateria = rs.getString("materia");
                nombreCurso = rs.getString("curso");
                lblMateria.setText("Materia: " + nombreMateria);
                lblCurso.setText("Curso: " + nombreCurso);

                System.out.println("Materia: " + nombreMateria);
                System.out.println("Curso: " + nombreCurso);
            }

            // Cargar y mostrar días de clase con horarios
            String diasQuery = "SELECT dia_semana, hora_inicio, hora_fin "
                    + "FROM horarios_materia "
                    + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? "
                    + "ORDER BY dia_semana, hora_inicio";
            ps = conect.prepareStatement(diasQuery);
            ps.setInt(1, usuarioId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);

            System.out.println("Consultando horarios para Profesor: " + usuarioId
                    + ", Curso: " + cursoId + ", Materia: " + materiaId);

            rs = ps.executeQuery();
            StringBuilder horarios = new StringBuilder("Horarios: ");
            boolean tieneHorarios = false;

            while (rs.next()) {
                tieneHorarios = true;

                // Obtener el día como string para evitar el error de conversión
                String diaStr = rs.getString("dia_semana");

                // Determinar el nombre del día, ya sea que esté como número o como texto
                String nombreDia;
                try {
                    // Intenta convertir a entero si es posible
                    int diaNum = Integer.parseInt(diaStr);
                    nombreDia = getDiaSemanaTexto(diaNum);
                } catch (NumberFormatException e) {
                    // Si ya es texto, úsalo directamente
                    nombreDia = diaStr;
                }

                String horaInicio = rs.getTime("hora_inicio").toString().substring(0, 5);
                String horaFin = rs.getTime("hora_fin").toString().substring(0, 5);

                horarios.append(nombreDia).append(" (").append(horaInicio)
                        .append(" - ").append(horaFin).append(") ");

                System.out.println("Horario encontrado: " + nombreDia + " de "
                        + horaInicio + " a " + horaFin);
            }

            if (!tieneHorarios) {
                horarios.append("No hay horarios configurados");
                System.out.println("¡ADVERTENCIA! No se encontraron horarios");
            }

            lblHorario.setText(horarios.toString());
            System.out.println("Texto final de horarios: " + horarios.toString());

        } catch (SQLException ex) {
            System.out.println("Error SQL: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage());
        }
    }

    /**
     * Convierte un número de día de la semana a su representación en texto.
     *
     * @param dia Número de día (1-5)
     * @return Nombre del día en texto
     */
    private String getDiaSemana(int dia) {
        switch (dia) {
            case 1:
                return "Lunes";
            case 2:
                return "Martes";
            case 3:
                return "Miércoles";
            case 4:
                return "Jueves";
            case 5:
                return "Viernes";
            default:
                return "";
        }
    }

    protected void cargarAsistencias() {
        try {
            System.out.println("Iniciando carga de asistencias...");

            // Si diasClase es null, inicializarlo ahora
            if (diasClase == null) {
                diasClase = obtenerDiasClase();
            }

            // Recargar tabla si es necesario
            if (tableModel.getRowCount() == 0 || tableModel.getColumnCount() < 3) {
                configurarTabla();
            } else {
                tableModel.setRowCount(0); // Limpiar filas anteriores
            }

            // Determinar el lunes de la semana actual
            LocalDate inicioDeSemana = fecha;
            while (inicioDeSemana.getDayOfWeek() != DayOfWeek.MONDAY) {
                inicioDeSemana = inicioDeSemana.minusDays(1);
            }
            LocalDate finDeSemana = inicioDeSemana.plusDays(4); // Viernes

            System.out.println("Semana: " + inicioDeSemana + " a " + finDeSemana);

            // Cargar alumnos
            String queryAlumnos = "SELECT DISTINCT u.id as usuario_id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            // Crear un mapa para almacenar los IDs de usuarios
            Map<String, Integer> dniToIdMap = new HashMap<>();
            int contadorAlumnos = 0;

            // Agregar filas para cada alumno
            while (rsAlumnos.next()) {
                contadorAlumnos++;
                String nombreCompleto = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                String dni = rsAlumnos.getString("dni");
                int userId = rsAlumnos.getInt("usuario_id");

                System.out.println("Alumno #" + contadorAlumnos + ": " + nombreCompleto + " (DNI: " + dni + ")");

                // Crear array con datos iniciales
                Object[] rowData = new Object[tableModel.getColumnCount()];
                rowData[0] = nombreCompleto;
                rowData[1] = dni;

                // Inicializar todos los estados como "NC" (No Cargado)
                for (int i = 2; i < rowData.length; i++) {
                    rowData[i] = "NC";
                }

                tableModel.addRow(rowData);
                dniToIdMap.put(dni, userId);
            }

            System.out.println("Total de alumnos cargados: " + contadorAlumnos);

            // Guardar el mapa en la tabla para usarlo al guardar
            if (tablaAsistencia != null) {
                tablaAsistencia.putClientProperty("dniToIdMap", dniToIdMap);
            }

            // Preparar mapeo de columnas a fechas
            Map<Integer, LocalDate> columnToDateMap = new HashMap<>();
            for (int i = 0; i < 5; i++) {
                LocalDate diaActual = inicioDeSemana.plusDays(i);
                int columna = i + 2; // +2 por las columnas "Alumno" y "DNI"
                columnToDateMap.put(columna, diaActual);
                System.out.println("Columna " + columna + " -> Fecha: " + diaActual);
            }

            // Guardar el mapa en la tabla
            tablaAsistencia.putClientProperty("columnToDateMap", columnToDateMap);

            // Configurar editabilidad de columnas según los días de clase
            for (int i = 2; i < tablaAsistencia.getColumnCount(); i++) {
                LocalDate fechaColumna = columnToDateMap.get(i);
                TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);

                boolean esDiaDeClase = diasClase.contains(fechaColumna);
                System.out.println("Columna " + i + " (fecha " + fechaColumna + ") es día de clase: " + esDiaDeClase);

                // Solo permitir editar columnas correspondientes a días de clase
                if (esDiaDeClase) {
                    column.setCellEditor(new EstadoAsistenciaEditor());
                    // Opcional: resaltar visualmente estas columnas
                    column.setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            if (!isSelected) {
                                String estado = value != null ? value.toString() : "NC";
                                c.setBackground(colorEstados.getOrDefault(estado, Color.WHITE));
                                // Añadir un borde para indicar que es editable
                                ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLUE));
                            }
                            return c;
                        }
                    });
                } else {
                    // Para días que no son de clase, inhabilitar la edición
                    column.setCellEditor(null);
                    // Opcional: aplicar un renderer que indique que no es editable
                    column.setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            if (!isSelected) {
                                // Usar un fondo gris para indicar que no es editable
                                c.setBackground(new Color(240, 240, 240));
                                c.setForeground(Color.GRAY);
                            }
                            return c;
                        }
                    });
                }
            }

            // Si no hay días de clase, mostrar mensaje
            if (diasClase.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay días de clase configurados para esta materia en esta semana.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Cargar asistencias existentes para la semana completa
            String queryAsistencias = "SELECT alumno_id, fecha, estado "
                    + "FROM asistencia_materia "
                    + "WHERE curso_id = ? AND materia_id = ? AND fecha BETWEEN ? AND ?";

            PreparedStatement psAsistencias = conect.prepareStatement(queryAsistencias);
            psAsistencias.setInt(1, cursoId);
            psAsistencias.setInt(2, materiaId);
            psAsistencias.setDate(3, java.sql.Date.valueOf(inicioDeSemana));
            psAsistencias.setDate(4, java.sql.Date.valueOf(finDeSemana));

            ResultSet rsAsistencias = psAsistencias.executeQuery();
            int contadorAsistencias = 0;

            while (rsAsistencias.next()) {
                contadorAsistencias++;
                int alumnoId = rsAsistencias.getInt("alumno_id");
                LocalDate fechaAsistencia = rsAsistencias.getDate("fecha").toLocalDate();
                String estado = rsAsistencias.getString("estado");

                System.out.println("Asistencia: alumnoId=" + alumnoId + ", fecha=" + fechaAsistencia + ", estado=" + estado);

                // Encontrar la columna que corresponde a esta fecha
                for (Map.Entry<Integer, LocalDate> entry : columnToDateMap.entrySet()) {
                    if (entry.getValue().equals(fechaAsistencia)) {
                        int columna = entry.getKey();

                        // Encontrar la fila del alumno por su ID
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            String dni = tableModel.getValueAt(i, 1).toString();
                            Integer id = dniToIdMap.get(dni);
                            if (id != null && id == alumnoId) {
                                tableModel.setValueAt(estado, i, columna);
                                System.out.println("Estado " + estado + " aplicado a alumno con DNI " + dni + " para fecha " + fechaAsistencia);
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            System.out.println("Total de asistencias cargadas: " + contadorAsistencias);

            // Si la tabla está vacía después de cargar, mostrar mensaje
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay alumnos asignados a este curso.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            System.out.println("Error al cargar asistencias: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage());
        }
    }

    
/**
 * Guarda las asistencias actualizadas en la base de datos.
 * Versión adaptada para funcionar con la numeración secuencial.
 */
@Override
protected void guardarAsistencias() {
    try {
        System.out.println("Iniciando guardado de asistencias...");

        // Obtener los mapas guardados en la tabla
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> rowToIdMap = (Map<Integer, Integer>) tablaAsistencia.getClientProperty("rowToIdMap");

        @SuppressWarnings("unchecked")
        Map<Integer, LocalDate> columnToDateMap = (Map<Integer, LocalDate>) tablaAsistencia.getClientProperty("columnToDateMap");

        if (rowToIdMap == null || columnToDateMap == null) {
            JOptionPane.showMessageDialog(this, "Error: No se encontró la información necesaria para guardar");
            return;
        }

        // Primero, obtener todas las fechas que vamos a guardar
        Set<LocalDate> fechasParaGuardar = new HashSet<>(columnToDateMap.values());

        System.out.println("Fechas a guardar: " + fechasParaGuardar);

        // Eliminar asistencias existentes para estas fechas
        for (LocalDate fecha : fechasParaGuardar) {
            String deleteQuery = "DELETE FROM asistencia_materia "
                    + "WHERE fecha = ? AND curso_id = ? AND materia_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setDate(1, java.sql.Date.valueOf(fecha));
            deletePs.setInt(2, cursoId);
            deletePs.setInt(3, materiaId);
            int registrosEliminados = deletePs.executeUpdate();
            System.out.println("Registros eliminados para " + fecha + ": " + registrosEliminados);
        }

        // Insertar nuevas asistencias para cada columna y fila
        String insertQuery = "INSERT INTO asistencia_materia "
                + "(alumno_id, curso_id, materia_id, fecha, estado, creado_por) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement insertPs = conect.prepareStatement(insertQuery);

        int registrosInsertados = 0;

        // Para cada columna de fecha
        for (Map.Entry<Integer, LocalDate> columnEntry : columnToDateMap.entrySet()) {
            int columna = columnEntry.getKey();
            LocalDate fecha = columnEntry.getValue();

            // Para cada fila (alumno)
            for (int fila = 0; fila < tableModel.getRowCount(); fila++) {
                String estado = tableModel.getValueAt(fila, columna).toString();

                // Solo guardamos estados que no sean "NC"
                if (!estado.equals("NC")) {
                    // Obtener el número secuencial de la fila
                    int numeroSecuencial = Integer.parseInt(tableModel.getValueAt(fila, 0).toString());
                    
                    // Obtener el ID real del alumno usando el mapa
                    Integer alumnoId = rowToIdMap.get(numeroSecuencial);

                    if (alumnoId == null) {
                        System.out.println("Error: No se encontró ID para el alumno con número " + numeroSecuencial);
                        continue;
                    }

                    insertPs.setInt(1, alumnoId);
                    insertPs.setInt(2, cursoId);
                    insertPs.setInt(3, materiaId);
                    insertPs.setDate(4, java.sql.Date.valueOf(fecha));
                    insertPs.setString(5, estado);
                    insertPs.setInt(6, usuarioId);
                    insertPs.executeUpdate();
                    registrosInsertados++;
                }
            }
        }

        System.out.println("Total de registros insertados: " + registrosInsertados);
        JOptionPane.showMessageDialog(this, "Asistencias guardadas exitosamente");

        // Recargar para mostrar los cambios
        buscarAsistencia();  // Usar buscarAsistencia en lugar de cargarAsistencias

    } catch (SQLException ex) {
        System.out.println("Error al guardar asistencias: " + ex.getMessage());
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
    }
}

    private void importarAsistenciaGeneral() {
        try {
            // Verificar si hay datos en asistencia_general para este curso
            String queryVerificar = "SELECT COUNT(*) as total FROM asistencia_general "
                    + "WHERE curso_id = ? AND fecha BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY) AND CURRENT_DATE()";

            PreparedStatement psVerificar = conect.prepareStatement(queryVerificar);
            psVerificar.setInt(1, cursoId);
            ResultSet rsVerificar = psVerificar.executeQuery();

            if (rsVerificar.next() && rsVerificar.getInt("total") == 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay asistencias registradas por el preceptor para este curso en los últimos 30 días.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Opciones de importación: día actual o seleccionar fecha
            String[] opciones = {"Día actual", "Seleccionar fecha"};
            int seleccion = JOptionPane.showOptionDialog(this,
                    "¿Qué asistencia desea importar?",
                    "Importar Asistencia",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opciones, opciones[0]);

            LocalDate fechaImportar;

            if (seleccion == 0) {
                // Día actual
                fechaImportar = fecha;
            } else if (seleccion == 1) {
                // Mostrar selector de fecha
                JDateChooser dateChooserImport = new JDateChooser();
                dateChooserImport.setDate(java.sql.Date.valueOf(fecha));

                int resultado = JOptionPane.showConfirmDialog(this, dateChooserImport,
                        "Seleccione fecha a importar", JOptionPane.OK_CANCEL_OPTION);

                if (resultado != JOptionPane.OK_OPTION) {
                    return; // Cancelado
                }

                if (dateChooserImport.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar una fecha válida");
                    return;
                }

                fechaImportar = dateChooserImport.getDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                return; // Cancelado
            }

            System.out.println("Fecha seleccionada para importar: " + fechaImportar);

            // Verificar si hay asistencias para la fecha seleccionada
            String queryCheck = "SELECT COUNT(*) as total FROM asistencia_general "
                    + "WHERE curso_id = ? AND fecha = ?";

            PreparedStatement psCheck = conect.prepareStatement(queryCheck);
            psCheck.setInt(1, cursoId);
            psCheck.setDate(2, java.sql.Date.valueOf(fechaImportar));
            ResultSet rsCheck = psCheck.executeQuery();

            if (rsCheck.next() && rsCheck.getInt("total") == 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay asistencias registradas por el preceptor para el día "
                        + fechaImportar.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Encontrar la columna correspondiente a la fecha seleccionada
            @SuppressWarnings("unchecked")
            Map<Integer, LocalDate> columnToDateMap
                    = (Map<Integer, LocalDate>) tablaAsistencia.getClientProperty("columnToDateMap");

            if (columnToDateMap == null) {
                JOptionPane.showMessageDialog(this, "Error: No se encontró información de fechas en la tabla.");
                return;
            }

            // Buscar la columna que corresponde a la fecha
            Integer columnaFecha = null;
            for (Map.Entry<Integer, LocalDate> entry : columnToDateMap.entrySet()) {
                if (entry.getValue().equals(fechaImportar)) {
                    columnaFecha = entry.getKey();
                    break;
                }
            }

            if (columnaFecha == null) {
                JOptionPane.showMessageDialog(this,
                        "La fecha seleccionada (" + fechaImportar + ") no está visible en la tabla actual.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtener información de DNI a ID
            @SuppressWarnings("unchecked")
            Map<String, Integer> dniToIdMap
                    = (Map<String, Integer>) tablaAsistencia.getClientProperty("dniToIdMap");

            if (dniToIdMap == null) {
                JOptionPane.showMessageDialog(this, "Error: No se encontró información de alumnos.");
                return;
            }

            // Consultar asistencias del preceptor para esa fecha
            String query = "SELECT alumno_id, estado FROM asistencia_general "
                    + "WHERE curso_id = ? AND fecha = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setDate(2, java.sql.Date.valueOf(fechaImportar));

            ResultSet rs = ps.executeQuery();
            int contadorActualizados = 0;

            while (rs.next()) {
                int alumnoId = rs.getInt("alumno_id");
                String estado = rs.getString("estado");

                // Buscar el DNI correspondiente a este alumno_id
                String dniAlumno = null;
                for (Map.Entry<String, Integer> entry : dniToIdMap.entrySet()) {
                    if (entry.getValue() == alumnoId) {
                        dniAlumno = entry.getKey();
                        break;
                    }
                }

                if (dniAlumno != null) {
                    // Buscar la fila correspondiente a este DNI
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 1).toString().equals(dniAlumno)) {
                            tableModel.setValueAt(estado, i, columnaFecha);
                            contadorActualizados++;
                            break;
                        }
                    }
                }
            }

            if (contadorActualizados > 0) {
                JOptionPane.showMessageDialog(this,
                        "Se importaron " + contadorActualizados + " asistencias para el día "
                        + fechaImportar.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "Importación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron coincidencias de asistencias para importar.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            System.out.println("Error al importar asistencias: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al importar asistencias: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.out.println("Error inesperado: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

   /**
 * Busca y actualiza la información de asistencia en la tabla.
 * Versión corregida con desplegable, valores por defecto y numeración secuencial.
 */
private void buscarAsistencia() {
    try {
        // Obtener la fecha seleccionada
        java.util.Date fechaSeleccionada = null;
        
        if (dateChooser != null) {
            fechaSeleccionada = dateChooser.getDate();
            System.out.println("Fecha seleccionada: " + fechaSeleccionada);
        }
        
        // Si no hay fecha seleccionada, usar la fecha actual
        if (fechaSeleccionada == null) {
            fechaSeleccionada = new java.util.Date();
            System.out.println("Usando fecha actual como respaldo");
        }
        
        // Convertir a LocalDate para manipulación
        LocalDate localDate = fechaSeleccionada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate inicioSemana = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Formatear fechas para mostrar
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        // Calcular nombres de los días
        DayOfWeek[] diasSemana = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        String[] nombresDias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        
        // Crear array para los encabezados de columna
        String[] columnasHeaders = new String[7]; // Nro, Nombre, y 5 días de la semana
        columnasHeaders[0] = "Nro";
        columnasHeaders[1] = "Nombre";
        
        // Generar los encabezados para los días de la semana
        for (int i = 0; i < diasSemana.length; i++) {
            LocalDate diaSemana = inicioSemana.with(diasSemana[i]);
            columnasHeaders[i + 2] = nombresDias[i] + " " + diaSemana.format(formatter);
        }
        
        // Actualizar modelo de tabla
        DefaultTableModel model = (DefaultTableModel) tablaAsistencia.getModel();
        
        // Actualizar encabezados con las nuevas fechas
        model.setColumnIdentifiers(columnasHeaders);
        
        // Limpiar filas existentes
        model.setRowCount(0);
        
        // Cargar alumnos del curso
        String consultaAlumnos = "SELECT u.id, u.nombre, u.apellido FROM usuarios u " +
                                "JOIN alumno_curso ac ON u.id = ac.alumno_id " +
                                "WHERE ac.curso_id = ? AND u.rol = 4 AND ac.estado = 'activo' " +
                                "ORDER BY u.apellido, u.nombre";
        
        PreparedStatement psAlumnos = conect.prepareStatement(consultaAlumnos);
        psAlumnos.setInt(1, cursoId);
        ResultSet rsAlumnos = psAlumnos.executeQuery();
        
        // Mapa para guardar los IDs de usuarios
        Map<Integer, Integer> rowToIdMap = new HashMap<>();
        int contador = 1;
        
        // Por cada alumno, verificar asistencia en cada día de la semana
        while (rsAlumnos.next()) {
            int alumnoId = rsAlumnos.getInt("id");
            String nombreCompleto = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
            
            // Fila para este alumno (empezamos con número secuencial y nombre)
            Object[] fila = new Object[7];
            fila[0] = contador;  // Número secuencial en lugar del ID
            fila[1] = nombreCompleto;
            
            // Guardar el mapeo entre número secuencial y ID real
            rowToIdMap.put(contador, alumnoId);
            
            // Para cada día de la semana, buscar asistencia
            for (int i = 0; i < diasSemana.length; i++) {
                LocalDate diaSemana = inicioSemana.with(diasSemana[i]);
                java.sql.Date sqlDate = java.sql.Date.valueOf(diaSemana);
                
                // Primero intentar con asistencia_materia (específica para esta materia)
                String consultaAsistenciaMateria = 
                      "SELECT estado FROM asistencia_materia " +
                      "WHERE alumno_id = ? AND fecha = ? AND curso_id = ? AND materia_id = ?";
                
                PreparedStatement psAsistencia = conect.prepareStatement(consultaAsistenciaMateria);
                psAsistencia.setInt(1, alumnoId);
                psAsistencia.setDate(2, sqlDate);
                psAsistencia.setInt(3, cursoId);
                psAsistencia.setInt(4, materiaId);
                
                ResultSet rsAsistencia = psAsistencia.executeQuery();
                
                if (rsAsistencia.next()) {
                    fila[i + 2] = rsAsistencia.getString("estado");
                } else {
                    // Si no hay en específica, intentar con general
                    rsAsistencia.close();
                    psAsistencia.close();
                    
                    String consultaAsistenciaGeneral = 
                          "SELECT estado FROM asistencia_general " +
                          "WHERE alumno_id = ? AND fecha = ? AND curso_id = ?";
                    
                    psAsistencia = conect.prepareStatement(consultaAsistenciaGeneral);
                    psAsistencia.setInt(1, alumnoId);
                    psAsistencia.setDate(2, sqlDate);
                    psAsistencia.setInt(3, cursoId);
                    
                    rsAsistencia = psAsistencia.executeQuery();
                    
                    if (rsAsistencia.next()) {
                        fila[i + 2] = rsAsistencia.getString("estado");
                    } else {
                        fila[i + 2] = "NC";  // NC como valor por defecto
                    }
                    
                    rsAsistencia.close();
                    psAsistencia.close();
                }
            }
            
            model.addRow(fila);
            contador++;
        }
        
        // Guardar el mapa en la tabla para usarlo al guardar
        tablaAsistencia.putClientProperty("rowToIdMap", rowToIdMap);
        
        // Preparar mapeo de columnas a fechas
        Map<Integer, LocalDate> columnToDateMap = new HashMap<>();
        for (int i = 0; i < diasSemana.length; i++) {
            LocalDate diaActual = inicioSemana.with(diasSemana[i]);
            int columna = i + 2; // +2 por las columnas "Nro" y "Nombre"
            columnToDateMap.put(columna, diaActual);
            System.out.println("Columna " + columna + " -> Fecha: " + diaActual);
        }
        
        // Guardar el mapa en la tabla
        tablaAsistencia.putClientProperty("columnToDateMap", columnToDateMap);
        
        // Refrescar la vista de la tabla
        tablaAsistencia.setModel(model);
        
        // Configurar el editor de celdas para mostrar desplegable
        for (int i = 2; i < tablaAsistencia.getColumnCount(); i++) {
            TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);
            column.setCellEditor(new EstadoAsistenciaEditor());
            
            // También el renderizador para mostrar colores según estado
            column.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (!isSelected) {
                        String estado = value != null ? value.toString() : "NC";
                        c.setBackground(colorEstados.getOrDefault(estado, Color.WHITE));
                    }
                    
                    return c;
                }
            });
        }
        
        tablaAsistencia.revalidate();
        tablaAsistencia.repaint();
        
        // Forzar la actualización de encabezados
        tablaAsistencia.getTableHeader().repaint();
        
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al buscar asistencia: " + ex.getMessage());
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage());
    }
}


    @Override
    protected boolean puedeEditarCelda(int row, int column) {
        return column == 2 && diasClase.contains(fecha);
    }

    /**
     * Botón "Buscar" para permitir buscar asistencias explícitamente. Añadir
     * este método al listener del botón si existe.
     */
    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {
        buscarAsistencia();
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
        topPanel = new javax.swing.JPanel();
        lblMateria = new javax.swing.JLabel();
        lblCurso = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        lblHorario = new javax.swing.JLabel();
        centralPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        tablaAsistencia = new javax.swing.JTable();
        bottomPanel = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnImportar = new javax.swing.JButton();

        lblMateria.setText("Materia");

        lblCurso.setText("Curso");

        lblFecha.setText("Fecha");

        lblHorario.setText("Horario");

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(lblMateria)
                .addGap(132, 132, 132)
                .addComponent(lblCurso)
                .addGap(122, 122, 122)
                .addComponent(lblHorario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFecha)
                .addGap(33, 33, 33)
                .addComponent(dateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMateria)
                        .addComponent(lblCurso)
                        .addComponent(lblFecha)
                        .addComponent(lblHorario)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        javax.swing.GroupLayout centralPanelLayout = new javax.swing.GroupLayout(centralPanel);
        centralPanel.setLayout(centralPanelLayout);
        centralPanelLayout.setHorizontalGroup(
            centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(centralPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        centralPanelLayout.setVerticalGroup(
            centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 453, Short.MAX_VALUE)
            .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(centralPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnImportar.setText("Importar Asistencia del Preceptor");
        btnImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(btnGuardar)
                .addGap(272, 272, 272)
                .addComponent(btnImportar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addComponent(btnCancelar)
                .addGap(27, 27, 27))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar)
                    .addComponent(btnImportar))
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 910, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 584, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarAsistencias();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        buscarAsistencia();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnImportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportarActionPerformed
        importarAsistenciaGeneral();
    }//GEN-LAST:event_btnImportarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnImportar;
    private javax.swing.JPanel centralPanel;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCurso;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblHorario;
    private javax.swing.JLabel lblMateria;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable tablaAsistencia;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
