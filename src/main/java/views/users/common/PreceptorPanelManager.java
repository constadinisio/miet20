package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.views.users.Preceptor.AsistenciaPreceptorPanel;
import main.java.utils.ExcelExportUtility;

/**
 * Gestor de paneles específico para el rol de Preceptor. Versión mejorada con
 * selector visual de cursos.
 */
public class PreceptorPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int preceptorId;
    private Connection conect;
    private Map<String, Integer> cursosMap = new HashMap<>();

    /**
     * Constructor del gestor de paneles para preceptores.
     */
    public PreceptorPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.preceptorId = userId;
        this.conect = Conexion.getInstancia().verificarConexion();
    }

    @Override
    public JComponent[] createButtons() {
        JButton btnNotas = createStyledButton("NOTAS", "notas");
        JButton btnAsistencias = createStyledButton("ASISTENCIAS", "asistencias");
        JButton btnExportar = createStyledButton("EXPORTAR", "exportar");

        return new JComponent[]{btnNotas, btnAsistencias, btnExportar};
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        // Establecer dimensiones preferidas
        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        try {
            System.out.println("Acción preceptor: " + actionCommand);

            switch (actionCommand) {
                case "notas":
                    JOptionPane.showMessageDialog(ventana,
                            "Funcionalidad de gestión de notas en desarrollo.",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "asistencias":
                    mostrarPanelAsistencias();
                    break;
                case "exportar":
                    mostrarDialogoExportacion();
                    break;
                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Error al procesar acción: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al procesar la acción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra el panel de asistencias con selector visual de cursos.
     */
    private void mostrarPanelAsistencias() {
        try {
            System.out.println("Cargando panel de asistencias para preceptor ID: " + preceptorId);

            // Crear el panel completo que será mostrado
            JPanel panelCompleto = new JPanel(new BorderLayout());

            // Panel superior para selección
            JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lblSeleccion = new JLabel("Seleccione Curso:");
            JComboBox<String> comboCursos = new JComboBox<>();

            // Cargar cursos desde la base de datos
            cargarCursos();

            // Llenar el ComboBox con los cursos disponibles
            for (String curso : cursosMap.keySet()) {
                comboCursos.addItem(curso);
                System.out.println("Curso añadido al selector: " + curso + " (ID: " + cursosMap.get(curso) + ")");
            }

            if (cursosMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron cursos activos en el sistema.",
                        "Sin cursos",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            comboCursos.setPreferredSize(new Dimension(200, 30));
            panelSeleccion.add(lblSeleccion);
            panelSeleccion.add(comboCursos);

            // Botón para cargar el panel seleccionado
            JButton btnCargar = new JButton("Cargar Asistencias");
            panelSeleccion.add(btnCargar);

            // Agregar panel de selección al panel completo
            panelCompleto.add(panelSeleccion, BorderLayout.NORTH);

            // Panel central donde se mostrará el contenido
            JPanel panelContenido = new JPanel(new BorderLayout());
            panelCompleto.add(panelContenido, BorderLayout.CENTER);

            // Listener para el botón
            btnCargar.addActionListener(e -> {
                String cursoSeleccionado = (String) comboCursos.getSelectedItem();
                if (cursoSeleccionado != null) {
                    try {
                        System.out.println("Procesando selección de curso: " + cursoSeleccionado);

                        Integer cursoId = cursosMap.get(cursoSeleccionado);
                        if (cursoId == null) {
                            JOptionPane.showMessageDialog(ventana,
                                    "Error: No se encontró información para el curso: " + cursoSeleccionado);
                            return;
                        }

                        System.out.println("Curso ID encontrado: " + cursoId);

                        // OCULTAR el panel de selección después de cargar
                        panelSeleccion.setVisible(false);

                        // Limpiar el panel de contenido
                        panelContenido.removeAll();

                        // Crear panel de navegación con información del curso seleccionado
                        JPanel panelNavegacion = new JPanel(new BorderLayout());
                        panelNavegacion.setBackground(new Color(51, 153, 255));
                        panelNavegacion.setPreferredSize(new Dimension(0, 40));

                        JLabel lblInfo = new JLabel("Asistencias - Curso " + cursoSeleccionado);
                        lblInfo.setForeground(Color.WHITE);
                        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
                        lblInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));

                        JButton btnCambiarCurso = new JButton("Cambiar Curso");
                        btnCambiarCurso.setFont(new Font("Arial", Font.PLAIN, 12));
                        btnCambiarCurso.setPreferredSize(new Dimension(120, 30));
                        btnCambiarCurso.addActionListener(evt -> {
                            // Mostrar nuevamente el panel de selección
                            panelSeleccion.setVisible(true);
                            // Limpiar el contenido
                            panelContenido.removeAll();
                            panelContenido.revalidate();
                            panelContenido.repaint();
                        });

                        panelNavegacion.add(lblInfo, BorderLayout.WEST);
                        panelNavegacion.add(btnCambiarCurso, BorderLayout.EAST);

                        panelContenido.add(panelNavegacion, BorderLayout.NORTH);

                        // Crear mensaje de carga
                        JLabel lblCargando = new JLabel("Cargando panel de asistencias, por favor espere...");
                        lblCargando.setHorizontalAlignment(JLabel.CENTER);
                        lblCargando.setFont(new Font("Arial", Font.BOLD, 16));
                        panelContenido.add(lblCargando, BorderLayout.CENTER);

                        // Actualizar UI para mostrar mensaje de carga
                        panelContenido.revalidate();
                        panelContenido.repaint();
                        panelCompleto.revalidate();
                        panelCompleto.repaint();

                        // Crear el panel de asistencia en segundo plano
                        SwingUtilities.invokeLater(() -> {
                            try {
                                System.out.println("Creando AsistenciaPreceptorPanel...");
                                AsistenciaPreceptorPanel panelAsistencia = new AsistenciaPreceptorPanel(preceptorId, cursoId);

                                // Remover mensaje de carga
                                panelContenido.remove(lblCargando);

                                // Añadir el panel de asistencia
                                panelContenido.add(panelAsistencia, BorderLayout.CENTER);

                                panelContenido.revalidate();
                                panelContenido.repaint();
                                panelCompleto.revalidate();
                                panelCompleto.repaint();

                                System.out.println("Panel de asistencias del preceptor cargado exitosamente y selector ocultado");

                            } catch (Exception ex) {
                                System.err.println("Error al crear panel de asistencia: " + ex.getMessage());
                                ex.printStackTrace();

                                // Remover mensaje de carga
                                panelContenido.remove(lblCargando);

                                JLabel lblError = new JLabel("Error al cargar el panel de asistencias: " + ex.getMessage());
                                lblError.setHorizontalAlignment(JLabel.CENTER);
                                lblError.setForeground(Color.RED);
                                panelContenido.add(lblError, BorderLayout.CENTER);

                                panelContenido.revalidate();
                                panelContenido.repaint();
                            }
                        });

                    } catch (Exception ex) {
                        System.err.println("Error al cargar panel de asistencias: " + ex.getMessage());
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ventana,
                                "Error al cargar panel: " + ex.getMessage()
                                + "\nDetalles: " + ex.getClass().getName()
                                + "\nSelección: " + cursoSeleccionado);
                    }
                }
            });

            // Mostrar el panel completo
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panelCompleto, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            System.err.println("Error al mostrar panel de asistencias: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al mostrar panel de asistencias: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carga los cursos desde la base de datos.
     */
    private void cargarCursos() {
        try {
            // Verificar conexión
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            if (conect == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }

            // Limpiar mapa actual
            cursosMap.clear();

            String query = "SELECT id, anio, division FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int anio = rs.getInt("anio");
                String division = rs.getString("division");

                String formato = anio + "°" + division;
                cursosMap.put(formato, id);
            }

            System.out.println("Cursos cargados para preceptor: " + cursosMap.size());
            // Imprimir los cursos para depuración
            cursosMap.forEach((nombre, id) -> System.out.println("  " + nombre + " -> ID=" + id));

        } catch (SQLException ex) {
            System.err.println("Error al cargar cursos: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar cursos: " + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra un diálogo para seleccionar qué exportar.
     */
    private void mostrarDialogoExportacion() {
        try {
            System.out.println("Iniciando proceso de exportación para preceptor ID: " + preceptorId);

            // Verificar si hay cursos disponibles
            if (cursosMap.isEmpty()) {
                cargarCursos(); // Intentar cargar cursos
            }

            if (cursosMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron cursos activos para exportar.",
                        "Sin datos para exportar",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear diálogo de selección
            String[] opciones = {"Exportar Curso Específico", "Exportar Todos los Cursos", "Cancelar"};
            int seleccion = JOptionPane.showOptionDialog(ventana,
                    "Seleccione qué desea exportar:",
                    "Exportar Asistencias",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]);

            switch (seleccion) {
                case 0: // Exportar curso específico
                    exportarCursoEspecifico();
                    break;
                case 1: // Exportar todos los cursos
                    exportarTodosLosCursos();
                    break;
                case 2: // Cancelar
                default:
                    System.out.println("Exportación cancelada por el usuario");
                    break;
            }

        } catch (Exception ex) {
            System.err.println("Error al mostrar diálogo de exportación: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al iniciar exportación: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exporta las asistencias de un curso específico.
     */
    private void exportarCursoEspecifico() {
        try {
            // Crear lista de cursos para selección
            String[] cursosArray = cursosMap.keySet().toArray(new String[0]);

            String cursoSeleccionado = (String) JOptionPane.showInputDialog(ventana,
                    "Seleccione el curso a exportar:",
                    "Seleccionar Curso",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    cursosArray,
                    cursosArray[0]);

            if (cursoSeleccionado != null) {
                Integer cursoId = cursosMap.get(cursoSeleccionado);
                if (cursoId != null) {
                    exportarAsistenciasCurso(cursoId, cursoSeleccionado);
                }
            }

        } catch (Exception ex) {
            System.err.println("Error al exportar curso específico: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al exportar curso: " + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exporta las asistencias de todos los cursos.
     */
    private void exportarTodosLosCursos() {
        try {
            int confirmacion = JOptionPane.showConfirmDialog(ventana,
                    "¿Está seguro de exportar las asistencias de todos los cursos?\n"
                    + "Esto puede generar archivos grandes y tomar tiempo.",
                    "Confirmar Exportación Completa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                int cursosExportados = 0;

                for (Map.Entry<String, Integer> entry : cursosMap.entrySet()) {
                    String curso = entry.getKey();
                    Integer cursoId = entry.getValue();

                    try {
                        if (exportarAsistenciasCurso(cursoId, curso)) {
                            cursosExportados++;
                        }
                    } catch (Exception ex) {
                        System.err.println("Error al exportar curso " + curso + ": " + ex.getMessage());
                        // Continuar con el siguiente curso
                    }
                }

                JOptionPane.showMessageDialog(ventana,
                        "Exportación completada.\n"
                        + "Cursos exportados exitosamente: " + cursosExportados + " de " + cursosMap.size(),
                        "Exportación Finalizada",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            System.err.println("Error al exportar todos los cursos: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error durante la exportación masiva: " + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exporta las asistencias de un curso específico a Excel.
     *
     * @param cursoId ID del curso
     * @param cursoNombre Nombre del curso
     * @return true si la exportación fue exitosa
     */
    private boolean exportarAsistenciasCurso(int cursoId, String cursoNombre) {
        try {
            System.out.println("Exportando asistencias para curso: " + cursoNombre + " (ID: " + cursoId + ")");

            // Crear modelo de tabla temporal para exportación
            javax.swing.table.DefaultTableModel modeloExportacion = new javax.swing.table.DefaultTableModel();

            // Configurar columnas
            modeloExportacion.addColumn("Alumno");
            modeloExportacion.addColumn("DNI");

            // Obtener fechas únicas para crear columnas dinámicas
            java.util.Set<String> fechas = obtenerFechasAsistencia(cursoId);
            java.util.List<String> fechasOrdenadas = new java.util.ArrayList<>(fechas);
            java.util.Collections.sort(fechasOrdenadas);

            // Agregar columnas de fechas
            for (String fecha : fechasOrdenadas) {
                modeloExportacion.addColumn(fecha);
            }

            // Cargar datos de alumnos y asistencias
            cargarDatosAsistenciaParaExportacion(modeloExportacion, cursoId, fechasOrdenadas);

            // Crear tabla temporal
            javax.swing.JTable tablaExportacion = new javax.swing.JTable(modeloExportacion);

            // Usar la utilidad de exportación
            String nombreArchivo = "Asistencias_" + cursoNombre.replace("°", "") + "_"
                    + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Usar el método correcto con los parámetros que espera
            String fechaActual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return main.java.utils.ExcelExportUtility.exportarTablaAsistencia(
                    tablaExportacion, // JTable 
                    cursoNombre, // String curso (ej: "1°A")
                    fechaActual // String fecha (ej: "22/05/2025")
            );

        } catch (Exception ex) {
            System.err.println("Error al exportar asistencias del curso " + cursoNombre + ": " + ex.getMessage());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(ventana,
                    "Error al exportar asistencias del curso " + cursoNombre + ":\n" + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }
    }

    /**
     * Obtiene las fechas únicas de asistencia para un curso.
     *
     * @param cursoId ID del curso
     * @return Set con las fechas de asistencia
     */
    private java.util.Set<String> obtenerFechasAsistencia(int cursoId) {
        java.util.Set<String> fechas = new java.util.LinkedHashSet<>();

        try {
            // Verificar conexión
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT DISTINCT DATE_FORMAT(fecha, '%d/%m/%Y') as fecha_formateada "
                    + "FROM asistencia_general "
                    + "WHERE curso_id = ? "
                    + "ORDER BY fecha";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                fechas.add(rs.getString("fecha_formateada"));
            }

            // Si no hay asistencias registradas, agregar fecha actual
            if (fechas.isEmpty()) {
                fechas.add(java.time.LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

        } catch (SQLException ex) {
            System.err.println("Error al obtener fechas de asistencia: " + ex.getMessage());
            ex.printStackTrace();
        }

        return fechas;
    }

    /**
     * Carga los datos de asistencia en el modelo para exportación.
     *
     * @param modelo Modelo de tabla destino
     * @param cursoId ID del curso
     * @param fechas Lista de fechas ordenadas
     */
    private void cargarDatosAsistenciaParaExportacion(javax.swing.table.DefaultTableModel modelo,
            int cursoId, java.util.List<String> fechas) {
        try {
            // Verificar conexión
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            // Obtener lista de alumnos del curso
            String queryAlumnos = "SELECT u.id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            while (rsAlumnos.next()) {
                int alumnoId = rsAlumnos.getInt("id");
                String nombreCompleto = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                String dni = rsAlumnos.getString("dni");

                // Crear fila para el alumno
                Object[] fila = new Object[2 + fechas.size()];
                fila[0] = nombreCompleto;
                fila[1] = dni != null ? dni : "Sin DNI";

                // Llenar asistencias por fecha
                for (int i = 0; i < fechas.size(); i++) {
                    String fecha = fechas.get(i);
                    String estado = obtenerEstadoAsistencia(alumnoId, cursoId, fecha);
                    fila[2 + i] = estado;
                }

                modelo.addRow(fila);
            }

            // Si no hay alumnos, agregar fila informativa
            if (modelo.getRowCount() == 0) {
                Object[] filaSinDatos = new Object[2 + fechas.size()];
                filaSinDatos[0] = "No hay alumnos registrados";
                filaSinDatos[1] = "-";
                for (int i = 0; i < fechas.size(); i++) {
                    filaSinDatos[2 + i] = "-";
                }
                modelo.addRow(filaSinDatos);
            }

        } catch (SQLException ex) {
            System.err.println("Error al cargar datos de asistencia para exportación: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Obtiene el estado de asistencia de un alumno en una fecha específica.
     *
     * @param alumnoId ID del alumno
     * @param cursoId ID del curso
     * @param fecha Fecha en formato dd/MM/yyyy
     * @return Estado de asistencia (P, A, T, AP, NC)
     */
    private String obtenerEstadoAsistencia(int alumnoId, int cursoId, String fecha) {
        try {
            // Convertir fecha de dd/MM/yyyy a yyyy-MM-dd para la consulta
            String[] partesFecha = fecha.split("/");
            String fechaSQL = partesFecha[2] + "-" + partesFecha[1] + "-" + partesFecha[0];

            String query = "SELECT estado FROM asistencia_general "
                    + "WHERE alumno_id = ? AND curso_id = ? AND fecha = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ps.setInt(2, cursoId);
            ps.setString(3, fechaSQL);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("estado");
            } else {
                return "NC"; // No corresponde / Sin registro
            }

        } catch (SQLException ex) {
            System.err.println("Error al obtener estado de asistencia: " + ex.getMessage());
            return "NC";
        }
    }

}
