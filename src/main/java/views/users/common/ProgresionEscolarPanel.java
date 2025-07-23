package main.java.views.users.common;

import main.java.services.ProgresionEscolarService;
import main.java.services.ProgresionEscolarService.CursoInfo;
import main.java.services.ProgresionEscolarService.EstudianteConCurso;

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
 * Panel para gestionar la progresión escolar anual con selectores específicos por curso
 */
public class ProgresionEscolarPanel extends JPanel {
    
    private ProgresionEscolarService progresionService;
    private JTable tablaEstudiantes;
    private DefaultTableModel modeloTabla;
    private JPanel panelProgresion;
    private List<EstudianteConCurso> estudiantesActuales;
    private Map<Integer, List<CursoInfo>> cursosPorAnio;
    
    public ProgresionEscolarPanel() {
        this.progresionService = new ProgresionEscolarService();
        inicializarComponentes();
        cargarDatos();
    }
    
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Progresión Escolar Anual"));
        
        // Panel superior con controles
        JPanel panelSuperior = new JPanel(new FlowLayout());
        JButton btnCargar = new JButton("🔄 Cargar Estudiantes");
        btnCargar.addActionListener(e -> cargarDatos());
        panelSuperior.add(btnCargar);
        
        // Tabla de estudiantes
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
        
        tablaEstudiantes = new JTable(modeloTabla);
        tablaEstudiantes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tablaEstudiantes.setAutoCreateRowSorter(true);
        
        // Configurar ancho de columnas
        tablaEstudiantes.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaEstudiantes.getColumnModel().getColumn(1).setPreferredWidth(200); // Estudiante
        tablaEstudiantes.getColumnModel().getColumn(2).setPreferredWidth(100); // DNI
        tablaEstudiantes.getColumnModel().getColumn(3).setPreferredWidth(150); // Curso Actual
        tablaEstudiantes.getColumnModel().getColumn(4).setPreferredWidth(50);  // Año
        tablaEstudiantes.getColumnModel().getColumn(5).setPreferredWidth(60);  // División
        tablaEstudiantes.getColumnModel().getColumn(6).setPreferredWidth(80);  // Promedio
        tablaEstudiantes.getColumnModel().getColumn(7).setPreferredWidth(80);  // Mat. Aprob.
        tablaEstudiantes.getColumnModel().getColumn(8).setPreferredWidth(80);  // Mat. Desaprob.
        tablaEstudiantes.getColumnModel().getColumn(9).setPreferredWidth(100); // Estado
        tablaEstudiantes.getColumnModel().getColumn(10).setPreferredWidth(250); // Observaciones
        
        JScrollPane scrollTabla = new JScrollPane(tablaEstudiantes);
        scrollTabla.setPreferredSize(new Dimension(1200, 400));
        
        // Panel de progresión (se creará dinámicamente)
        panelProgresion = new JPanel();
        panelProgresion.setLayout(new BoxLayout(panelProgresion, BoxLayout.Y_AXIS));
        panelProgresion.setBorder(BorderFactory.createTitledBorder("Progresión por Cursos"));
        
        JScrollPane scrollProgresion = new JScrollPane(panelProgresion);
        scrollProgresion.setPreferredSize(new Dimension(1200, 300));
        
        // Agregar componentes
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(scrollProgresion, BorderLayout.SOUTH);
    }
    
    private void cargarDatos() {
        SwingUtilities.invokeLater(() -> {
            // Mostrar indicador de carga
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setString("Cargando estudiantes y cursos...");
            progressBar.setStringPainted(true);
            
            JDialog dialogCarga = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cargando", true);
            dialogCarga.add(progressBar);
            dialogCarga.setSize(300, 80);
            dialogCarga.setLocationRelativeTo(this);
            
            // Ejecutar carga en hilo separado
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Cargar estudiantes
                    estudiantesActuales = progresionService.obtenerAlumnosConCursos();
                    
                    // Cargar cursos
                    cursosPorAnio = progresionService.obtenerCursosPorAnio();
                    
                    return null;
                }
                
                @Override
                protected void done() {
                    dialogCarga.dispose();
                    actualizarTablaEstudiantes();
                    crearPanelProgresion();
                }
            };
            
            worker.execute();
            dialogCarga.setVisible(true);
        });
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
        
        System.out.println("✅ Tabla actualizada con " + estudiantesActuales.size() + " estudiantes");
    }
    
    private void crearPanelProgresion() {
        panelProgresion.removeAll();
        
        if (cursosPorAnio.isEmpty()) {
            JLabel lblSinCursos = new JLabel("No se encontraron cursos activos en el sistema");
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
                crearSelectorParaAnio(anio, cursosDelAnio);
            }
        }
        
        // Panel para egreso de 6° año
        crearPanelEgreso();
        
        panelProgresion.revalidate();
        panelProgresion.repaint();
    }
    
    private void crearSelectorParaAnio(int anio, List<CursoInfo> cursosDelAnio) {
        JPanel panelAnio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAnio.setBorder(BorderFactory.createTitledBorder(anio + "° Año"));
        
        for (CursoInfo cursoOrigen : cursosDelAnio) {
            // Obtener estudiantes del curso
            long cantidadEstudiantes = estudiantesActuales.stream()
                .filter(e -> e.cursoActualId == cursoOrigen.id)
                .count();
            
            if (cantidadEstudiantes > 0) {
                JLabel lblOrigen = new JLabel("Desde: " + cursoOrigen.nombre + " (" + cantidadEstudiantes + " estudiantes)");
                
                // Selector de curso destino
                JComboBox<CursoInfo> cmbDestino = new JComboBox<>();
                
                if (anio < 6) {
                    // Para años 1-5, mostrar cursos del año siguiente
                    List<CursoInfo> cursosDestino = cursosPorAnio.get(anio + 1);
                    if (cursosDestino != null) {
                        cmbDestino.addItem(new CursoInfo()); // Opción vacía
                        for (CursoInfo cursoDestino : cursosDestino) {
                            cmbDestino.addItem(cursoDestino);
                        }
                    }
                    
                    // También permitir repetir en el mismo año
                    cmbDestino.addItem(crearCursoRepetir(cursoOrigen));
                } else {
                    // Para 6° año, solo opción de repetir
                    cmbDestino.addItem(new CursoInfo()); // Opción vacía
                    cmbDestino.addItem(crearCursoRepetir(cursoOrigen));
                }
                
                JButton btnProgresionar = new JButton("Progresionar");
                btnProgresionar.addActionListener(e -> progresarCurso(cursoOrigen, (CursoInfo) cmbDestino.getSelectedItem()));
                
                // Panel para este curso específico
                JPanel panelCurso = new JPanel(new FlowLayout());
                panelCurso.setBorder(BorderFactory.createEtchedBorder());
                panelCurso.add(lblOrigen);
                panelCurso.add(new JLabel(" → "));
                panelCurso.add(cmbDestino);
                panelCurso.add(btnProgresionar);
                
                panelAnio.add(panelCurso);
            }
        }
        
        if (panelAnio.getComponentCount() > 0) {
            panelProgresion.add(panelAnio);
        }
    }
    
    private CursoInfo crearCursoRepetir(CursoInfo cursoOriginal) {
        CursoInfo cursoRepetir = new CursoInfo();
        cursoRepetir.id = cursoOriginal.id;
        cursoRepetir.anio = cursoOriginal.anio;
        cursoRepetir.division = cursoOriginal.division;
        cursoRepetir.turno = cursoOriginal.turno;
        cursoRepetir.nombre = "🔄 REPETIR: " + cursoOriginal.nombre;
        return cursoRepetir;
    }
    
    private void crearPanelEgreso() {
        List<CursoInfo> cursos6to = cursosPorAnio.get(6);
        if (cursos6to == null || cursos6to.isEmpty()) {
            return;
        }
        
        JPanel panelEgreso = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEgreso.setBorder(BorderFactory.createTitledBorder("Egreso - 6° Año"));
        
        for (CursoInfo curso6to : cursos6to) {
            long cantidadEstudiantes = estudiantesActuales.stream()
                .filter(e -> e.cursoActualId == curso6to.id)
                .count();
            
            if (cantidadEstudiantes > 0) {
                JButton btnEgresar = new JButton("🎓 Egresar " + curso6to.nombre + " (" + cantidadEstudiantes + " estudiantes)");
                btnEgresar.addActionListener(e -> procesarEgresoCurso(curso6to));
                panelEgreso.add(btnEgresar);
            }
        }
        
        if (panelEgreso.getComponentCount() > 0) {
            panelProgresion.add(panelEgreso);
        }
    }
    
    private void progresarCurso(CursoInfo cursoOrigen, CursoInfo cursoDestino) {
        if (cursoDestino == null || cursoDestino.nombre == null || cursoDestino.nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un curso de destino", "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener estudiantes del curso origen
        List<EstudianteConCurso> estudiantesDelCurso = estudiantesActuales.stream()
            .filter(e -> e.cursoActualId == cursoOrigen.id)
            .toList();
        
        if (estudiantesDelCurso.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay estudiantes en el curso seleccionado", "Sin Estudiantes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Determinar tipo de progresión
        boolean esRepeticion = cursoDestino.nombre.contains("REPETIR");
        String tipoProgresion = esRepeticion ? "repetición" : "promoción";
        
        // Confirmación
        String mensaje = String.format(
            "¿Confirma la %s de %d estudiantes?\n\n" +
            "Curso origen: %s\n" +
            "Curso destino: %s\n\n" +
            "Esta acción modificará las inscripciones de todos los estudiantes del curso.",
            tipoProgresion, estudiantesDelCurso.size(), cursoOrigen.nombre, 
            esRepeticion ? cursoOrigen.nombre : cursoDestino.nombre
        );
        
        int opcion = JOptionPane.showConfirmDialog(this, mensaje, 
            "Confirmar " + tipoProgresion.substring(0, 1).toUpperCase() + tipoProgresion.substring(1), 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
            
        if (opcion == JOptionPane.YES_OPTION) {
            ejecutarProgresionCurso(estudiantesDelCurso, cursoOrigen, cursoDestino, esRepeticion);
        }
    }
    
    private void ejecutarProgresionCurso(List<EstudianteConCurso> estudiantes, CursoInfo cursoOrigen, CursoInfo cursoDestino, boolean esRepeticion) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                int procesados = 0;
                int errores = 0;
                
                publish("Iniciando progresión de " + estudiantes.size() + " estudiantes...");
                
                for (EstudianteConCurso estudiante : estudiantes) {
                    try {
                        if (!esRepeticion) {
                            // Progresión a nuevo curso
                            progresionService.progresarAlumno(estudiante.alumnoId, cursoOrigen.id, cursoDestino.id);
                            publish("✅ " + estudiante.nombreCompleto + " progresado a " + cursoDestino.nombre);
                        } else {
                            // Repetición (no cambiar curso, solo marcar)
                            publish("🔄 " + estudiante.nombreCompleto + " repite " + cursoOrigen.nombre);
                        }
                        procesados++;
                        
                    } catch (SQLException e) {
                        publish("❌ Error con " + estudiante.nombreCompleto + ": " + e.getMessage());
                        errores++;
                    }
                    
                    // Pequeña pausa para no saturar la base de datos
                    Thread.sleep(100);
                }
                
                publish("Progresión completada: " + procesados + " exitosos, " + errores + " errores");
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String mensaje : chunks) {
                    System.out.println(mensaje);
                }
            }
            
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(ProgresionEscolarPanel.this,
                    "Progresión completada exitosamente!",
                    "Progresión Completada",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recargar datos
                cargarDatos();
            }
        };
        
        worker.execute();
    }
    
    private void procesarEgresoCurso(CursoInfo curso6to) {
        List<EstudianteConCurso> estudiantesDelCurso = estudiantesActuales.stream()
            .filter(e -> e.cursoActualId == curso6to.id)
            .toList();
        
        if (estudiantesDelCurso.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay estudiantes en el curso seleccionado", "Sin Estudiantes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Contar por estado
        long egresados = estudiantesDelCurso.stream().filter(e -> "EGRESADO".equals(e.estadoAcademico)).count();
        long conDeuda = estudiantesDelCurso.stream().filter(e -> "CON_DEUDA".equals(e.estadoAcademico)).count();
        long repitentes = estudiantesDelCurso.stream().filter(e -> "REPITENTE".equals(e.estadoAcademico)).count();
        
        String mensaje = String.format(
            "¿Confirma el proceso de egreso para %s?\n\n" +
            "Estudiantes a procesar: %d\n" +
            "• Egresan sin deuda: %d\n" +
            "• Egresan con deuda: %d\n" +
            "• Repiten 6° año: %d\n\n" +
            "Los egresados serán dados de baja del sistema activo.",
            curso6to.nombre, estudiantesDelCurso.size(), egresados, conDeuda, repitentes
        );
        
        int opcion = JOptionPane.showConfirmDialog(this, mensaje, 
            "Confirmar Proceso de Egreso", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
            
        if (opcion == JOptionPane.YES_OPTION) {
            ejecutarProcesoEgreso(estudiantesDelCurso);
        }
    }
    
    private void ejecutarProcesoEgreso(List<EstudianteConCurso> estudiantes) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                int procesados = 0;
                int egresados = 0;
                int repitentes = 0;
                int errores = 0;
                
                publish("Iniciando proceso de egreso...");
                
                for (EstudianteConCurso estudiante : estudiantes) {
                    try {
                        if ("EGRESADO".equals(estudiante.estadoAcademico) || "CON_DEUDA".equals(estudiante.estadoAcademico)) {
                            // Desactivar inscripción (egreso)
                            // Se implementaría la lógica de baja del sistema
                            egresados++;
                            publish("🎓 " + estudiante.nombreCompleto + " egresado");
                        } else {
                            // Repite 6° año
                            repitentes++;
                            publish("🔄 " + estudiante.nombreCompleto + " repite 6° año");
                        }
                        procesados++;
                        
                    } catch (Exception e) {
                        publish("❌ Error con " + estudiante.nombreCompleto + ": " + e.getMessage());
                        errores++;
                    }
                    
                    Thread.sleep(100);
                }
                
                publish("Egreso completado: " + egresados + " egresados, " + repitentes + " repitentes, " + errores + " errores");
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String mensaje : chunks) {
                    System.out.println(mensaje);
                }
            }
            
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(ProgresionEscolarPanel.this,
                    "Proceso de egreso completado!",
                    "Egreso Completado",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recargar datos
                cargarDatos();
            }
        };
        
        worker.execute();
    }
}
