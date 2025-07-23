package main.java.views.users.common;

import main.java.services.ProgresionEscolarService;
import main.java.services.ProgresionEscolarService.EstudianteConCurso;
import main.java.services.ProgresionEscolarService.CursoInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel para consultar el historial académico de alumnos seleccionando primero el curso
 */
public class HistorialAcademicoPorCursoPanel extends JPanel {
    
    private ProgresionEscolarService progresionService;
    private int usuarioActualId;
    
    // Componentes de UI
    private JComboBox<String> cmbAnio;
    private JComboBox<CursoInfo> cmbCurso;
    private JTable tablaAlumnos;
    private DefaultTableModel modeloTablaAlumnos;
    private JLabel lblEstado;
    private JButton btnConsultarHistorial;
    
    private Map<Integer, List<CursoInfo>> cursosPorAnio;
    private List<EstudianteConCurso> alumnosDelCurso;

    public HistorialAcademicoPorCursoPanel(int usuarioId) {
        this.usuarioActualId = usuarioId;
        this.progresionService = new ProgresionEscolarService();
        inicializarComponentes();
        cargarCursos();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("📋 Consulta de Historial Académico por Curso"));

        // Panel superior - Selectores de curso
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de selectores
        JPanel panelSelectores = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel lblAnio = new JLabel("Año:");
        lblAnio.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        cmbAnio = new JComboBox<>();
        cmbAnio.setPreferredSize(new Dimension(80, 25));
        cmbAnio.addActionListener(e -> actualizarCursosPorAnio());
        
        JLabel lblCurso = new JLabel("Curso:");
        lblCurso.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        cmbCurso = new JComboBox<>();
        cmbCurso.setPreferredSize(new Dimension(250, 25));
        cmbCurso.addActionListener(e -> cargarAlumnosDelCurso());
        
        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.addActionListener(e -> cargarCursos());
        
        panelSelectores.add(lblAnio);
        panelSelectores.add(cmbAnio);
        panelSelectores.add(Box.createHorizontalStrut(20));
        panelSelectores.add(lblCurso);
        panelSelectores.add(cmbCurso);
        panelSelectores.add(Box.createHorizontalStrut(20));
        panelSelectores.add(btnActualizar);

        // Panel de estado
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblEstado = new JLabel("Seleccione un año para comenzar...");
        lblEstado.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        lblEstado.setForeground(Color.GRAY);
        panelEstado.add(lblEstado);

        panelSuperior.add(panelSelectores);
        panelSuperior.add(panelEstado);

        // Panel central - Tabla de alumnos del curso
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createTitledBorder("Alumnos del Curso Seleccionado"));

        String[] columnasAlumnos = {
            "ID", "Apellido", "Nombre", "DNI", "Promedio", 
            "Mat. Aprob.", "Mat. Desaprob.", "Estado", "Observaciones"
        };

        modeloTablaAlumnos = new DefaultTableModel(columnasAlumnos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAlumnos = new JTable(modeloTablaAlumnos);
        tablaAlumnos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaAlumnos.setAutoCreateRowSorter(true);

        // Configurar anchos de columna
        tablaAlumnos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaAlumnos.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaAlumnos.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaAlumnos.getColumnModel().getColumn(3).setPreferredWidth(80);
        tablaAlumnos.getColumnModel().getColumn(4).setPreferredWidth(70);
        tablaAlumnos.getColumnModel().getColumn(5).setPreferredWidth(80);
        tablaAlumnos.getColumnModel().getColumn(6).setPreferredWidth(80);
        tablaAlumnos.getColumnModel().getColumn(7).setPreferredWidth(100);
        tablaAlumnos.getColumnModel().getColumn(8).setPreferredWidth(200);

        JScrollPane scrollAlumnos = new JScrollPane(tablaAlumnos);
        scrollAlumnos.setPreferredSize(new Dimension(800, 250));

        panelCentral.add(scrollAlumnos, BorderLayout.CENTER);

        // Panel inferior - Botones de acción
        JPanel panelInferior = new JPanel(new FlowLayout());
        
        btnConsultarHistorial = new JButton("📚 Ver Historial Académico Completo");
        btnConsultarHistorial.setEnabled(false);
        btnConsultarHistorial.addActionListener(e -> mostrarHistorialAlumnoSeleccionado());
        
        JButton btnExportarListado = new JButton("📄 Exportar Listado del Curso");
        btnExportarListado.addActionListener(e -> exportarListadoCurso());
        
        panelInferior.add(btnConsultarHistorial);
        panelInferior.add(Box.createHorizontalStrut(20));
        panelInferior.add(btnExportarListado);

        // Listener para habilitar/deshabilitar botón según selección
        tablaAlumnos.getSelectionModel().addListSelectionListener(e -> {
            btnConsultarHistorial.setEnabled(tablaAlumnos.getSelectedRow() != -1);
        });

        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarCursos() {
        lblEstado.setText("🔄 Cargando cursos disponibles...");
        lblEstado.setForeground(Color.BLUE);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                cursosPorAnio = progresionService.obtenerCursosPorAnio();
                return null;
            }

            @Override
            protected void done() {
                actualizarComboAnios();
                lblEstado.setText("✅ Cursos cargados. Seleccione un año.");
                lblEstado.setForeground(Color.BLACK);
            }
        };

        worker.execute();
    }

    private void actualizarComboAnios() {
        cmbAnio.removeAllItems();
        cmbAnio.addItem("-- Seleccionar Año --");
        
        if (cursosPorAnio != null) {
            for (int anio = 1; anio <= 6; anio++) {
                if (cursosPorAnio.containsKey(anio) && !cursosPorAnio.get(anio).isEmpty()) {
                    cmbAnio.addItem(anio + "° Año");
                }
            }
        }
    }

    private void actualizarCursosPorAnio() {
        cmbCurso.removeAllItems();
        modeloTablaAlumnos.setRowCount(0);
        btnConsultarHistorial.setEnabled(false);

        String seleccionAnio = (String) cmbAnio.getSelectedItem();
        if (seleccionAnio == null || seleccionAnio.equals("-- Seleccionar Año --")) {
            lblEstado.setText("Seleccione un año para ver los cursos disponibles.");
            return;
        }

        // Extraer número del año
        int anio = Integer.parseInt(seleccionAnio.substring(0, 1));
        
        cmbCurso.addItem(null); // Opción vacía
        
        List<CursoInfo> cursosDelAnio = cursosPorAnio.get(anio);
        if (cursosDelAnio != null) {
            for (CursoInfo curso : cursosDelAnio) {
                cmbCurso.addItem(curso);
            }
        }

        lblEstado.setText("Seleccione un curso para ver sus alumnos.");
    }

    private void cargarAlumnosDelCurso() {
        CursoInfo cursoSeleccionado = (CursoInfo) cmbCurso.getSelectedItem();
        modeloTablaAlumnos.setRowCount(0);
        btnConsultarHistorial.setEnabled(false);

        if (cursoSeleccionado == null) {
            lblEstado.setText("Seleccione un curso para ver sus alumnos.");
            return;
        }

        lblEstado.setText("🔄 Cargando alumnos del curso: " + cursoSeleccionado.nombre);
        lblEstado.setForeground(Color.BLUE);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Obtener todos los alumnos y filtrar por curso
                List<EstudianteConCurso> todosLosEstudiantes = progresionService.obtenerAlumnosConCursos();
                alumnosDelCurso = todosLosEstudiantes.stream()
                    .filter(e -> e.cursoActualId == cursoSeleccionado.id)
                    .toList();
                return null;
            }

            @Override
            protected void done() {
                for (EstudianteConCurso estudiante : alumnosDelCurso) {
                    // Separar apellido y nombre
                    String[] nombreCompleto = estudiante.nombreCompleto.split(", ");
                    String apellido = nombreCompleto.length > 0 ? nombreCompleto[0] : "";
                    String nombre = nombreCompleto.length > 1 ? nombreCompleto[1] : "";

                    Object[] fila = {
                        estudiante.alumnoId,
                        apellido,
                        nombre,
                        estudiante.dni,
                        String.format("%.2f", estudiante.promedioGeneral),
                        estudiante.materiasAprobadas,
                        estudiante.materiasDesaprobadas,
                        estudiante.estadoAcademico,
                        estudiante.observaciones
                    };
                    modeloTablaAlumnos.addRow(fila);
                }

                lblEstado.setText("✅ Cargados " + alumnosDelCurso.size() + 
                                " alumnos de " + cursoSeleccionado.nombre);
                lblEstado.setForeground(Color.BLACK);
            }
        };

        worker.execute();
    }

    private void mostrarHistorialAlumnoSeleccionado() {
        int filaSeleccionada = tablaAlumnos.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un alumno de la tabla para ver su historial académico",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener datos del alumno seleccionado
        int alumnoId = (Integer) modeloTablaAlumnos.getValueAt(filaSeleccionada, 0);
        String apellido = (String) modeloTablaAlumnos.getValueAt(filaSeleccionada, 1);
        String nombre = (String) modeloTablaAlumnos.getValueAt(filaSeleccionada, 2);
        String nombreCompleto = apellido + ", " + nombre;

        // Crear y mostrar ventana de historial
        mostrarVentanaHistorialCompleto(alumnoId, nombreCompleto);
    }

    private void mostrarVentanaHistorialCompleto(int alumnoId, String nombreAlumno) {
        JDialog dialogHistorial = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "📚 Historial Académico Completo - " + nombreAlumno, true);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de encabezado
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        
        JLabel lblTitulo = new JLabel("📚 HISTORIAL ACADÉMICO COMPLETO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitulo.setForeground(new Color(0, 100, 0));
        
        JLabel lblAlumno = new JLabel("👨‍🎓 Alumno: " + nombreAlumno + " (ID: " + alumnoId + ")", SwingConstants.CENTER);
        lblAlumno.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        lblAlumno.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        panelEncabezado.add(lblTitulo, BorderLayout.NORTH);
        panelEncabezado.add(lblAlumno, BorderLayout.SOUTH);

        // Tabla de historial académico
        String[] columnasHistorial = {
            "Ciclo Lectivo", "Curso Cursado", "Estado Inicial", "Estado Final",
            "Promedio", "Faltas", "Mat. Aprob.", "Mat. Desaprob.",
            "Observaciones", "Fecha Procesamiento", "Procesado Por"
        };

        DefaultTableModel modeloHistorial = new DefaultTableModel(columnasHistorial, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setAutoCreateRowSorter(true);
        tablaHistorial.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        // Configurar anchos de columna
        tablaHistorial.getColumnModel().getColumn(0).setPreferredWidth(90);
        tablaHistorial.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaHistorial.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaHistorial.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaHistorial.getColumnModel().getColumn(4).setPreferredWidth(70);
        tablaHistorial.getColumnModel().getColumn(5).setPreferredWidth(60);
        tablaHistorial.getColumnModel().getColumn(6).setPreferredWidth(80);
        tablaHistorial.getColumnModel().getColumn(7).setPreferredWidth(80);
        tablaHistorial.getColumnModel().getColumn(8).setPreferredWidth(250);
        tablaHistorial.getColumnModel().getColumn(9).setPreferredWidth(130);
        tablaHistorial.getColumnModel().getColumn(10).setPreferredWidth(120);

        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);
        scrollHistorial.setPreferredSize(new Dimension(1200, 350));

        // Panel de carga
        JPanel panelCarga = new JPanel(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Cargando historial académico...");
        progressBar.setStringPainted(true);
        panelCarga.add(progressBar, BorderLayout.CENTER);

        panel.add(panelEncabezado, BorderLayout.NORTH);
        panel.add(panelCarga, BorderLayout.CENTER);

        // Cargar datos del historial
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return progresionService.obtenerHistorialAcademico(alumnoId);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> historial = get();
                    
                    // Reemplazar panel de carga con tabla
                    panel.remove(panelCarga);
                    panel.add(scrollHistorial, BorderLayout.CENTER);

                    // Llenar tabla
                    for (Map<String, Object> registro : historial) {
                        Object[] fila = {
                            registro.get("ciclo_lectivo"),
                            registro.get("curso"),
                            registro.get("estado_inicial"),
                            registro.get("estado_final"),
                            String.format("%.2f", (Double) registro.get("promedio_general")),
                            registro.get("total_faltas"),
                            registro.get("materias_aprobadas"),
                            registro.get("materias_desaprobadas"),
                            registro.get("observaciones"),
                            registro.get("fecha_procesamiento"),
                            registro.get("procesado_por")
                        };
                        modeloHistorial.addRow(fila);
                    }

                    if (historial.isEmpty()) {
                        Object[] fila = {
                            "Sin datos", "No hay historial académico registrado", "-", "-", "-", "-", "-", "-",
                            "Este alumno no tiene registros de progresiones anteriores en el sistema", "-", "-"
                        };
                        modeloHistorial.addRow(fila);
                    }

                    panel.revalidate();
                    panel.repaint();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(dialogHistorial,
                        "Error al cargar el historial académico: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnExportar = new JButton("📄 Exportar a PDF");
        btnExportar.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialogHistorial,
                "Funcionalidad de exportación PDF en desarrollo.\n\n" +
                "Por ahora puede seleccionar y copiar los datos de la tabla.",
                "En Desarrollo",
                JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnImprimir = new JButton("🖨️ Imprimir");
        btnImprimir.addActionListener(e -> {
            try {
                tablaHistorial.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogHistorial,
                    "Error al imprimir: " + ex.getMessage(),
                    "Error de Impresión",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCerrar = new JButton("❌ Cerrar");
        btnCerrar.addActionListener(e -> dialogHistorial.dispose());

        panelBotones.add(btnExportar);
        panelBotones.add(Box.createHorizontalStrut(10));
        panelBotones.add(btnImprimir);
        panelBotones.add(Box.createHorizontalStrut(20));
        panelBotones.add(btnCerrar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        dialogHistorial.add(panel);
        dialogHistorial.setSize(1300, 550);
        dialogHistorial.setLocationRelativeTo(this);
        dialogHistorial.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Iniciar carga
        worker.execute();
        dialogHistorial.setVisible(true);
    }

    private void exportarListadoCurso() {
        CursoInfo cursoSeleccionado = (CursoInfo) cmbCurso.getSelectedItem();
        
        if (cursoSeleccionado == null || alumnosDelCurso == null || alumnosDelCurso.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero debe seleccionar un curso y cargar sus alumnos",
                "Sin Datos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Funcionalidad de exportación de listado en desarrollo.\n\n" +
            "Curso: " + cursoSeleccionado.nombre + "\n" +
            "Alumnos: " + alumnosDelCurso.size(),
            "En Desarrollo",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
