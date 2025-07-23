package main.java.views.users.common;

import main.java.services.ProgresionEscolarService;
import main.java.services.ProgresionEscolarService.CursoInfo;
import main.java.services.ProgresionEscolarService.EstudianteConCurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;

/**
 * Panel para gestionar la progresión escolar con selectores específicos por curso
 */
public class ProgresionSelectivaPanel extends JPanel {
    
    private ProgresionEscolarService progresionService;
    private JTable tablaEstudiantes;
    private DefaultTableModel modeloTabla;
    private JPanel panelProgresion;
    private JLabel lblEstado;
    private List<EstudianteConCurso> estudiantesActuales;
    private Map<Integer, List<CursoInfo>> cursosPorAnio;
    
    public ProgresionSelectivaPanel() {
        this.progresionService = new ProgresionEscolarService();
        inicializarComponentes();
        cargarDatos();
    }
    
    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Progresión Escolar - Selectores por Curso"));
        
        // Panel superior con controles
        JPanel panelSuperior = new JPanel(new FlowLayout());
        JButton btnCargar = new JButton("🔄 Recargar Datos");
        btnCargar.addActionListener(e -> cargarDatos());
        
        lblEstado = new JLabel("Iniciando...");
        lblEstado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        panelSuperior.add(btnCargar);
        panelSuperior.add(Box.createHorizontalStrut(20));
        panelSuperior.add(lblEstado);
        
        // Tabla de estudiantes
        String[] columnas = {
            "ID", "Estudiante", "DNI", "Curso Actual", "Año", "División", 
            "Promedio", "Aprob.", "Desaprob.", "Estado", "Observaciones"
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaEstudiantes = new JTable(modeloTabla);
        tablaEstudiantes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tablaEstudiantes.setAutoCreateRowSorter(true);
        
        // Configurar ancho de columnas
        tablaEstudiantes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaEstudiantes.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaEstudiantes.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaEstudiantes.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaEstudiantes.getColumnModel().getColumn(4).setPreferredWidth(50);
        tablaEstudiantes.getColumnModel().getColumn(5).setPreferredWidth(60);
        tablaEstudiantes.getColumnModel().getColumn(6).setPreferredWidth(70);
        tablaEstudiantes.getColumnModel().getColumn(7).setPreferredWidth(60);
        tablaEstudiantes.getColumnModel().getColumn(8).setPreferredWidth(70);
        tablaEstudiantes.getColumnModel().getColumn(9).setPreferredWidth(90);
        tablaEstudiantes.getColumnModel().getColumn(10).setPreferredWidth(250);
        
        JScrollPane scrollTabla = new JScrollPane(tablaEstudiantes);
        scrollTabla.setPreferredSize(new Dimension(1200, 350));
        
        // Panel de progresión
        panelProgresion = new JPanel();
        panelProgresion.setLayout(new BoxLayout(panelProgresion, BoxLayout.Y_AXIS));
        panelProgresion.setBorder(BorderFactory.createTitledBorder("Selectores de Progresión por Curso"));
        
        JScrollPane scrollProgresion = new JScrollPane(panelProgresion);
        scrollProgresion.setPreferredSize(new Dimension(1200, 250));
        
        // Agregar componentes
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(scrollProgresion, BorderLayout.SOUTH);
    }
    
    private void cargarDatos() {
        lblEstado.setText("🔄 Cargando estudiantes y cursos...");
        lblEstado.setForeground(Color.BLUE);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                estudiantesActuales = progresionService.obtenerAlumnosConCursos();
                cursosPorAnio = progresionService.obtenerCursosPorAnio();
                return null;
            }
            
            @Override
            protected void done() {
                actualizarTablaEstudiantes();
                crearSelectoresProgresion();
                lblEstado.setText("✅ Cargados " + estudiantesActuales.size() + " estudiantes y " + 
                    cursosPorAnio.values().stream().mapToInt(List::size).sum() + " cursos");
                lblEstado.setForeground(Color.BLACK);
            }
        };
        
        worker.execute();
    }
    
    private void actualizarTablaEstudiantes() {
        modeloTabla.setRowCount(0);
        
        for (EstudianteConCurso estudiante : estudiantesActuales) {
            Object[] fila = {
                estudiante.alumnoId,
                estudiante.nombreCompleto,
                estudiante.dni,
                estudiante.cursoActualNombre,
                estudiante.anioActual,
                estudiante.divisionActual,
                String.format("%.2f", estudiante.promedioGeneral),
                estudiante.materiasAprobadas,
                estudiante.materiasDesaprobadas,
                estudiante.estadoAcademico,
                estudiante.observaciones
            };
            modeloTabla.addRow(fila);
        }
    }
    
    private void crearSelectoresProgresion() {
        panelProgresion.removeAll();
        
        if (cursosPorAnio.isEmpty()) {
            JLabel lblSinCursos = new JLabel("❌ No se encontraron cursos activos");
            lblSinCursos.setHorizontalAlignment(SwingConstants.CENTER);
            panelProgresion.add(lblSinCursos);
            panelProgresion.revalidate();
            panelProgresion.repaint();
            return;
        }
        
        // Crear selectores para cada año
        for (int anio = 1; anio <= 6; anio++) {
            List<CursoInfo> cursosDelAnio = cursosPorAnio.get(anio);
            if (cursosDelAnio != null && !cursosDelAnio.isEmpty()) {
                JPanel panelAnio = crearPanelParaAnio(anio, cursosDelAnio);
                if (panelAnio.getComponentCount() > 1) { // Más que solo el título
                    panelProgresion.add(panelAnio);
                    panelProgresion.add(Box.createVerticalStrut(5));
                }
            }
        }
        
        panelProgresion.revalidate();
        panelProgresion.repaint();
    }
    
    private JPanel crearPanelParaAnio(int anio, List<CursoInfo> cursosDelAnio) {
        JPanel panelAnio = new JPanel();
        panelAnio.setLayout(new BoxLayout(panelAnio, BoxLayout.Y_AXIS));
        panelAnio.setBorder(BorderFactory.createTitledBorder(anio + "° Año"));
        
        for (CursoInfo cursoOrigen : cursosDelAnio) {
            // Contar estudiantes en este curso
            long cantidadEstudiantes = estudiantesActuales.stream()
                .filter(e -> e.cursoActualId == cursoOrigen.id)
                .count();
            
            if (cantidadEstudiantes > 0) {
                JPanel panelCurso = crearSelectorParaCurso(anio, cursoOrigen, cantidadEstudiantes);
                panelAnio.add(panelCurso);
                panelAnio.add(Box.createVerticalStrut(3));
            }
        }
        
        return panelAnio;
    }
    
    private JPanel crearSelectorParaCurso(int anio, CursoInfo cursoOrigen, long cantidadEstudiantes) {
        JPanel panelCurso = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panelCurso.setBorder(BorderFactory.createEtchedBorder());
        panelCurso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Etiqueta del curso origen
        JLabel lblOrigen = new JLabel(String.format("De: %s (%d estudiantes)", 
            cursoOrigen.nombre, cantidadEstudiantes));
        lblOrigen.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        lblOrigen.setPreferredSize(new Dimension(250, 25));
        
        // Selector de destino
        JComboBox<CursoInfo> cmbDestino = new JComboBox<>();
        cmbDestino.setPreferredSize(new Dimension(200, 25));
        
        // Agregar opción por defecto
        CursoInfo opcionVacia = new CursoInfo();
        opcionVacia.nombre = "-- Seleccionar destino --";
        opcionVacia.id = -1;
        cmbDestino.addItem(opcionVacia);
        
        // Agregar opciones según el año
        if (anio < 6) {
            // Para años 1-5: promoción al siguiente año
            List<CursoInfo> cursosDestino = cursosPorAnio.get(anio + 1);
            if (cursosDestino != null) {
                for (CursoInfo curso : cursosDestino) {
                    cmbDestino.addItem(curso);
                }
            }
        }
        
        // Opción de repetir (para todos los años)
        CursoInfo opcionRepetir = new CursoInfo();
        opcionRepetir.id = cursoOrigen.id;
        opcionRepetir.nombre = "🔄 REPETIR: " + cursoOrigen.nombre;
        cmbDestino.addItem(opcionRepetir);
        
        // Para 6° año: opción de egreso
        if (anio == 6) {
            CursoInfo opcionEgreso = new CursoInfo();
            opcionEgreso.id = -2;
            opcionEgreso.nombre = "🎓 EGRESAR";
            cmbDestino.addItem(opcionEgreso);
        }
        
        // Botón de progresión
        JButton btnProgresar = new JButton("▶️ Ejecutar");
        btnProgresar.setPreferredSize(new Dimension(100, 25));
        btnProgresar.addActionListener(e -> {
            CursoInfo seleccionado = (CursoInfo) cmbDestino.getSelectedItem();
            if (seleccionado != null && seleccionado.id != -1) {
                confirmarYEjecutarProgresion(cursoOrigen, seleccionado);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Debe seleccionar un destino", 
                    "Selección Requerida", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Agregar componentes
        panelCurso.add(lblOrigen);
        panelCurso.add(new JLabel(" → "));
        panelCurso.add(cmbDestino);
        panelCurso.add(btnProgresar);
        
        return panelCurso;
    }
    
    private void confirmarYEjecutarProgresion(CursoInfo cursoOrigen, CursoInfo cursoDestino) {
        // Obtener estudiantes del curso
        List<EstudianteConCurso> estudiantes = estudiantesActuales.stream()
            .filter(e -> e.cursoActualId == cursoOrigen.id)
            .toList();
        
        if (estudiantes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay estudiantes en el curso seleccionado");
            return;
        }
        
        // Determinar tipo de operación
        boolean esRepeticion = cursoDestino.nombre.contains("REPETIR");
        boolean esEgreso = cursoDestino.id == -2;
        
        String tipoOperacion = esEgreso ? "egreso" : (esRepeticion ? "repetición" : "promoción");
        String destino = esEgreso ? "Egreso del sistema" : 
                        (esRepeticion ? cursoOrigen.nombre : cursoDestino.nombre);
        
        // Diálogo de confirmación
        String mensaje = String.format(
            "¿Confirma la %s de %d estudiantes?\n\n" +
            "🏫 Curso origen: %s\n" +
            "🎯 Destino: %s\n\n" +
            "⚠️ Esta acción modificará las inscripciones de todos los estudiantes del curso.",
            tipoOperacion, estudiantes.size(), cursoOrigen.nombre, destino
        );
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this, 
            mensaje, 
            "Confirmar " + tipoOperacion.toUpperCase(), 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            ejecutarProgresion(estudiantes, cursoOrigen, cursoDestino, esRepeticion, esEgreso);
        }
    }
    
    private void ejecutarProgresion(List<EstudianteConCurso> estudiantes, CursoInfo cursoOrigen, 
                                  CursoInfo cursoDestino, boolean esRepeticion, boolean esEgreso) {
        
        // Crear diálogo de progreso
        JDialog dialogProgreso = crearDialogoProgreso();
        JProgressBar progressBar = (JProgressBar) ((JPanel) dialogProgreso.getContentPane().getComponent(0)).getComponent(0);
        JLabel lblProgreso = (JLabel) ((JPanel) dialogProgreso.getContentPane().getComponent(0)).getComponent(1);
        
        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int exitosos = 0;
                int total = estudiantes.size();
                
                progressBar.setMaximum(total);
                
                for (int i = 0; i < estudiantes.size(); i++) {
                    EstudianteConCurso estudiante = estudiantes.get(i);
                    
                    try {
                        if (esEgreso) {
                            // Proceso de egreso
                            publish("🎓 Egresando: " + estudiante.nombreCompleto);
                            // Aquí se implementaría la lógica de egreso
                            Thread.sleep(100);
                            
                        } else if (esRepeticion) {
                            // Proceso de repetición
                            publish("🔄 Repetición: " + estudiante.nombreCompleto);
                            // Para repetición no se cambia el curso
                            Thread.sleep(50);
                            
                        } else {
                            // Proceso de promoción
                            publish("⬆️ Promocionando: " + estudiante.nombreCompleto);
                            progresionService.progresarAlumno(estudiante.alumnoId, cursoOrigen.id, cursoDestino.id);
                            Thread.sleep(100);
                        }
                        
                        exitosos++;
                        
                    } catch (Exception e) {
                        publish("❌ Error con " + estudiante.nombreCompleto + ": " + e.getMessage());
                    }
                    
                    progressBar.setValue(i + 1);
                    publish(String.format("Progreso: %d/%d (%d%%)", i + 1, total, ((i + 1) * 100 / total)));
                }
                
                return exitosos;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String mensaje : chunks) {
                    if (mensaje.startsWith("Progreso:")) {
                        lblProgreso.setText(mensaje);
                    } else {
                        System.out.println(mensaje);
                    }
                }
            }
            
            @Override
            protected void done() {
                dialogProgreso.dispose();
                
                try {
                    int exitosos = get();
                    
                    JOptionPane.showMessageDialog(
                        ProgresionSelectivaPanel.this,
                        String.format("✅ Proceso completado!\n\nEstudiantes procesados exitosamente: %d de %d", 
                            exitosos, estudiantes.size()),
                        "Proceso Completado",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Recargar datos
                    cargarDatos();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        ProgresionSelectivaPanel.this,
                        "Error durante el proceso: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
        dialogProgreso.setVisible(true);
    }
    
    private JDialog crearDialogoProgreso() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Procesando...", true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        
        JLabel lblProgreso = new JLabel("Iniciando proceso...");
        lblProgreso.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(lblProgreso, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setSize(400, 120);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        return dialog;
    }
}
