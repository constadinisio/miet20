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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import main.java.database.Conexion;
import main.java.views.users.Profesor.AsistenciaProfesorPanel;
import main.java.views.users.Profesor.NotasBimestralesPanel;
import main.java.views.users.Profesor.NotasProfesorPanel;
import main.java.views.users.Profesor.libroTema;
import main.java.views.users.common.VentanaInicio;

/**
 * Gestor de paneles específico para el rol de Profesor.
 */
public class ProfesorPanelManager implements RolPanelManager {
    
    private final VentanaInicio ventana;
    private final int profesorId;
    private Connection conect;
    
    /**
     * Constructor del gestor de paneles para profesores.
     * 
     * @param ventana Ventana principal
     * @param userId ID del usuario (profesor)
     */
    public ProfesorPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.profesorId = userId;
        this.conect = Conexion.getInstancia().verificarConexion();
    }
    
    @Override
    public JComponent[] createButtons() {
        // Crear botones específicos para el rol de profesor
        JButton btnNotas = createStyledButton("NOTAS", "notas");
        JButton btnAsistencias = createStyledButton("ASISTENCIAS", "asistencias");
        JButton btnLibroTemas = createStyledButton("LIBRO DE TEMAS", "libroTemas");
        
        // Retornar array de botones
        return new JComponent[]{
            btnNotas,
            btnAsistencias,
            btnLibroTemas
        };
    }
    
    /**
     * Crea un botón con el estilo estándar de la aplicación.
     * 
     * @param text Texto del botón
     * @param actionCommand Comando de acción para identificar el botón
     * @return Botón configurado
     */
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
            switch (actionCommand) {
                case "notas":
                    mostrarPanelNotas();
                    break;
                case "asistencias":
                    mostrarPanelAsistencias();
                    break;
                case "libroTemas":
                    mostrarPanelLibroTemas();
                    break;
                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana, 
                    "Error al cargar el panel: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            ventana.restaurarVistaPrincipal();
        }
    }
    
    /**
     * Muestra el panel de notas con opciones de selección.
     */
    private void mostrarPanelNotas() {
        try {
            // Panel superior para selección
            JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, int[]> materiaIds = new HashMap<>();

            String query
                    = "SELECT DISTINCT c.id as curso_id, CONCAT(c.anio, '°', c.division) as curso, "
                    + "m.id as materia_id, m.nombre as materia "
                    + "FROM cursos c "
                    + "JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id "
                    + "JOIN materias m ON pcm.materia_id = m.id "
                    + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY c.anio, c.division, m.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("curso") + " - " + rs.getString("materia");
                comboMaterias.addItem(item);
                materiaIds.put(item, new int[]{rs.getInt("curso_id"), rs.getInt("materia_id")});
            }

            // Agregar radio buttons para elegir el tipo de panel de notas
            JRadioButton rbTrabajos = new JRadioButton("Trabajos y Actividades", true);
            JRadioButton rbBimestral = new JRadioButton("Notas Bimestrales", false);
            ButtonGroup grupoOpciones = new ButtonGroup();
            grupoOpciones.add(rbTrabajos);
            grupoOpciones.add(rbBimestral);

            panelSeleccion.add(lblSeleccion);
            panelSeleccion.add(comboMaterias);
            panelSeleccion.add(rbTrabajos);
            panelSeleccion.add(rbBimestral);

            // Botón para cargar el panel seleccionado
            JButton btnCargar = new JButton("Cargar");
            panelSeleccion.add(btnCargar);

            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());
            panelPrincipal.add(panelSeleccion, BorderLayout.NORTH);

            // Listener para el botón
            btnCargar.addActionListener(e -> {
                String seleccion = (String) comboMaterias.getSelectedItem();
                if (seleccion != null) {
                    try {
                        int[] ids = materiaIds.get(seleccion);
                        int cursoId = ids[0];
                        int materiaId = ids[1];

                        // Remover el panel anterior si existe
                        if (panelPrincipal.getComponentCount() > 1) {
                            panelPrincipal.remove(1);
                        }

                        // Cargar el panel correspondiente según la selección
                        if (rbTrabajos.isSelected()) {
                            // Crear el panel de notas
                            NotasProfesorPanel panelNotas = new NotasProfesorPanel(profesorId, cursoId, materiaId);
                            panelPrincipal.add(panelNotas, BorderLayout.CENTER);
                        } else {
                            NotasBimestralesPanel panelBimestral = new NotasBimestralesPanel(
                                    profesorId, cursoId, materiaId
                            );
                            panelPrincipal.add(panelBimestral, BorderLayout.CENTER);
                        }

                        panelPrincipal.revalidate();
                        panelPrincipal.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ventana, "Error al cargar panel: " + ex.getMessage());
                    }
                }
            });

            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar los cursos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Muestra el panel de asistencias.
     */
    private void mostrarPanelAsistencias() {
        try {
            // Panel superior para selección
            JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, int[]> materiaIds = new HashMap<>();

            String query
                    = "SELECT DISTINCT c.id as curso_id, CONCAT(c.anio, '°', c.division) as curso, "
                    + "m.id as materia_id, m.nombre as materia "
                    + "FROM cursos c "
                    + "JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id "
                    + "JOIN materias m ON pcm.materia_id = m.id "
                    + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY c.anio, c.division, m.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("curso") + " - " + rs.getString("materia");
                comboMaterias.addItem(item);
                int cursoId = rs.getInt("curso_id");
                int materiaId = rs.getInt("materia_id");
                materiaIds.put(item, new int[]{cursoId, materiaId});
            }

            comboMaterias.setPreferredSize(new Dimension(300, 30));
            panelSeleccion.add(lblSeleccion);
            panelSeleccion.add(comboMaterias);

            // Botón para cargar el panel seleccionado
            JButton btnCargar = new JButton("Cargar");
            panelSeleccion.add(btnCargar);

            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());
            panelPrincipal.add(panelSeleccion, BorderLayout.NORTH);

            // Listener para el botón
            btnCargar.addActionListener(e -> {
                String seleccion = (String) comboMaterias.getSelectedItem();
                if (seleccion != null) {
                    try {
                        int[] ids = materiaIds.get(seleccion);
                        
                        if (ids == null) {
                            JOptionPane.showMessageDialog(ventana, "No se encontraron IDs asociados a la selección: " + seleccion);
                            return;
                        }
                        
                        // Remover el panel anterior si existe
                        if (panelPrincipal.getComponentCount() > 1) {
                            panelPrincipal.remove(1);
                        }

                        AsistenciaProfesorPanel panelAsistencia = new AsistenciaProfesorPanel(
                                profesorId,
                                ids[0], // cursoId
                                ids[1]  // materiaId
                        );

                        panelPrincipal.add(panelAsistencia, BorderLayout.CENTER);
                        panelPrincipal.revalidate();
                        panelPrincipal.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ventana, 
                            "Error al cargar panel: " + ex.getMessage() 
                            + "\nDetalles: " + ex.getClass().getName()
                            + "\nSelección: " + seleccion);
                    }
                }
            });

            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar los cursos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Muestra el panel del libro de temas.
     */
    private void mostrarPanelLibroTemas() {
        try {
            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());
            
            // Crear y mostrar el panel de libro de temas usando la versión adaptada
            libroTema libro = new libroTema(panelPrincipal, profesorId, ventana);
            
            panelPrincipal.add(libro, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar el libro de temas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}