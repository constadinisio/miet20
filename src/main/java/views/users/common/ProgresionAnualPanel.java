package main.java.views.users.common;

import main.java.services.ProgresionEscolarService;
import main.java.services.ProgresionEscolarService.EstudianteConCurso;
import main.java.services.ProgresionEscolarService.CursoInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

/**
 * Panel simplificado para gestionar la progresión anual de alumnos
 */
public class ProgresionAnualPanel extends JPanel {
    
    private ProgresionEscolarService progresionService;
    private int usuarioActualId;
    
    // Componentes de UI
    private JTable tablaAlumnos;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstado;
    private JPanel panelBotones;
    private List<EstudianteConCurso> estudiantesActuales;
    private Map<Integer, List<CursoInfo>> cursosPorAnio;
    
    public ProgresionAnualPanel(int usuarioId) {
        this.usuarioActualId = usuarioId;
        this.progresionService = new ProgresionEscolarService();
        inicializarComponentes();
        cargarVistaPrevia();
    }
    
    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Progresión Anual de Alumnos"));
        
        // Panel superior con controles
        JPanel panelSuperior = new JPanel(new FlowLayout());
        
        JButton btnCargarVista = new JButton("🔄 Cargar Vista Previa");
        btnCargarVista.addActionListener(e -> cargarVistaPrevia());
        
        lblEstado = new JLabel("Iniciando sistema...");
        lblEstado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        panelSuperior.add(btnCargarVista);
        panelSuperior.add(Box.createHorizontalStrut(20));
        panelSuperior.add(lblEstado);
        
        // Tabla de alumnos
        String[] columnas = {
            "ID", "Estudiante", "DNI", "Curso Actual", "Año", "División",
            "Promedio", "Mat. Aprob.", "Mat. Desaprob.", "Estado", "Observaciones"
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaAlumnos = new JTable(modeloTabla);
        tablaAlumnos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tablaAlumnos.setAutoCreateRowSorter(true);
        
        // Configurar anchos de columna
        tablaAlumnos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaAlumnos.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaAlumnos.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaAlumnos.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaAlumnos.getColumnModel().getColumn(4).setPreferredWidth(50);
        tablaAlumnos.getColumnModel().getColumn(5).setPreferredWidth(60);
        tablaAlumnos.getColumnModel().getColumn(6).setPreferredWidth(70);
        tablaAlumnos.getColumnModel().getColumn(7).setPreferredWidth(70);
        tablaAlumnos.getColumnModel().getColumn(8).setPreferredWidth(80);
        tablaAlumnos.getColumnModel().getColumn(9).setPreferredWidth(100);
        tablaAlumnos.getColumnModel().getColumn(10).setPreferredWidth(250);
        
        JScrollPane scrollTabla = new JScrollPane(tablaAlumnos);
        scrollTabla.setPreferredSize(new Dimension(1200, 400));
        
        // Panel de botones de progresión
        panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBorder(BorderFactory.createTitledBorder("Selectores de Progresión"));
        
        JScrollPane scrollBotones = new JScrollPane(panelBotones);
        scrollBotones.setPreferredSize(new Dimension(1200, 200));
        
        // Agregar componentes
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(scrollBotones, BorderLayout.SOUTH);
    }
    
    private void cargarVistaPrevia() {
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
                actualizarTablaAlumnos();
                crearPanelProgresion();
                lblEstado.setText("✅ Cargados " + estudiantesActuales.size() + " estudiantes");
                lblEstado.setForeground(Color.BLACK);
            }
        };
        
        worker.execute();
    }
    
    private void actualizarTablaAlumnos() {
        modeloTabla.setRowCount(0);
        
        if (estudiantesActuales != null) {
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
        
        System.out.println("✅ Tabla actualizada con " + modeloTabla.getRowCount() + " estudiantes");
    }
    
    private void crearPanelProgresion() {
        panelBotones.removeAll();
        
        if (cursosPorAnio == null || cursosPorAnio.isEmpty()) {
            JLabel lblSinCursos = new JLabel("❌ No se encontraron cursos activos");
            lblSinCursos.setHorizontalAlignment(SwingConstants.CENTER);
            panelBotones.add(lblSinCursos);
            panelBotones.revalidate();
            panelBotones.repaint();
            return;
        }
        
        // Agregar mensaje informativo
        JLabel lblInfo = new JLabel("💡 Seleccione el curso de destino para cada grupo de estudiantes:");
        lblInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panelBotones.add(lblInfo);
        
        // Crear selectores para cada año
        for (int anio = 1; anio <= 6; anio++) {
            List<CursoInfo> cursosDelAnio = cursosPorAnio.get(anio);
            if (cursosDelAnio != null && !cursosDelAnio.isEmpty()) {
                JPanel panelAnio = crearPanelParaAnio(anio, cursosDelAnio);
                if (panelAnio.getComponentCount() > 1) {
                    panelBotones.add(panelAnio);
                    panelBotones.add(Box.createVerticalStrut(5));
                }
            }
        }
        
        panelBotones.revalidate();
        panelBotones.repaint();
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
        JPanel panelCurso = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelCurso.setBorder(BorderFactory.createEtchedBorder());
        panelCurso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Etiqueta del curso origen
        JLabel lblOrigen = new JLabel(String.format("De: %s (%d estudiantes)", 
            cursoOrigen.nombre, cantidadEstudiantes));
        lblOrigen.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        lblOrigen.setPreferredSize(new Dimension(280, 25));
        
        // Selector de destino
        JComboBox<CursoInfo> cmbDestino = new JComboBox<>();
        cmbDestino.setPreferredSize(new Dimension(220, 25));
        
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
                ejecutarProgresionCurso(cursoOrigen, seleccionado);
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
    
    private void ejecutarProgresionCurso(CursoInfo cursoOrigen, CursoInfo cursoDestino) {
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
        
        // Confirmación
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
            procesarProgresion(estudiantes, cursoOrigen, cursoDestino, esRepeticion, esEgreso);
        }
    }
    
    private void procesarProgresion(List<EstudianteConCurso> estudiantes, CursoInfo cursoOrigen, 
                                  CursoInfo cursoDestino, boolean esRepeticion, boolean esEgreso) {
        
        // Crear diálogo de progreso
        JDialog dialogProgreso = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Procesando...", true);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(estudiantes.size());
        
        JLabel lblProgreso = new JLabel("Iniciando proceso...");
        lblProgreso.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(lblProgreso, BorderLayout.SOUTH);
        
        dialogProgreso.add(panel);
        dialogProgreso.setSize(400, 120);
        dialogProgreso.setLocationRelativeTo(this);
        dialogProgreso.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int exitosos = 0;
                
                for (int i = 0; i < estudiantes.size(); i++) {
                    EstudianteConCurso estudiante = estudiantes.get(i);
                    
                    try {
                        if (esEgreso) {
                            // Proceso de egreso - aquí se implementaría la lógica específica
                            publish("🎓 Egresando: " + estudiante.nombreCompleto);
                            Thread.sleep(100);
                            
                        } else if (esRepeticion) {
                            // Proceso de repetición - no cambiar curso
                            publish("🔄 Repetición: " + estudiante.nombreCompleto);
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
                    publish(String.format("Progreso: %d/%d", i + 1, estudiantes.size()));
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
                        ProgresionAnualPanel.this,
                        String.format("✅ Proceso completado!\n\nEstudiantes procesados exitosamente: %d de %d", 
                            exitosos, estudiantes.size()),
                        "Proceso Completado",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Recargar datos
                    cargarVistaPrevia();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        ProgresionAnualPanel.this,
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
}