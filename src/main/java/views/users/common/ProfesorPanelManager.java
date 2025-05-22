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
 * Versión corregida que maneja correctamente los IDs de curso y materia.
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
            System.err.println("Error en handleButtonAction: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana, 
                    "Error al cargar el panel: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }
    
    /**
     * Muestra el panel de notas con opciones de selección.
     */
    private void mostrarPanelNotas() {
        try {
            System.out.println("Cargando panel de notas para profesor ID: " + profesorId);
            
            // Crear el panel completo que será mostrado
            JPanel panelCompleto = new JPanel(new BorderLayout());
            
            // Panel superior para selección
            JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
            JComboBox<String> comboMaterias = new JComboBox<>();
            
            // Estructura mejorada para almacenar los datos
            Map<String, MateriaInfo> materiaInfoMap = new HashMap<>();

            String query = """
                SELECT DISTINCT 
                    c.id as curso_id, 
                    CONCAT(c.anio, '°', c.division) as curso, 
                    m.id as materia_id, 
                    m.nombre as materia 
                FROM cursos c 
                JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id 
                JOIN materias m ON pcm.materia_id = m.id 
                WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' 
                ORDER BY c.anio, c.division, m.nombre
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            int contadorMaterias = 0;
            while (rs.next()) {
                String item = rs.getString("curso") + " - " + rs.getString("materia");
                comboMaterias.addItem(item);
                
                // Crear objeto con la información completa
                MateriaInfo info = new MateriaInfo(
                    rs.getInt("curso_id"),
                    rs.getInt("materia_id"),
                    rs.getString("curso"),
                    rs.getString("materia")
                );
                materiaInfoMap.put(item, info);
                contadorMaterias++;
                
                System.out.println("Cargada materia: " + item + " - Curso ID: " + 
                                 info.cursoId + ", Materia ID: " + info.materiaId);
            }
            
            System.out.println("Total materias cargadas: " + contadorMaterias);

            if (contadorMaterias == 0) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas para este profesor.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
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

            // Agregar panel de selección al panel completo
            panelCompleto.add(panelSeleccion, BorderLayout.NORTH);

            // Panel central donde se mostrará el contenido
            JPanel panelContenido = new JPanel(new BorderLayout());
            panelCompleto.add(panelContenido, BorderLayout.CENTER);

            // Listener para el botón
            btnCargar.addActionListener(e -> {
                String seleccion = (String) comboMaterias.getSelectedItem();
                if (seleccion != null) {
                    try {
                        System.out.println("Procesando selección: " + seleccion);
                        
                        MateriaInfo info = materiaInfoMap.get(seleccion);
                        if (info == null) {
                            JOptionPane.showMessageDialog(ventana, 
                                "Error: No se encontró información para la selección: " + seleccion);
                            return;
                        }
                        
                        System.out.println("Info encontrada - Curso ID: " + info.cursoId + 
                                         ", Materia ID: " + info.materiaId);

                        // OCULTAR el panel de selección después de cargar
                        panelSeleccion.setVisible(false);

                        // Limpiar el panel de contenido
                        panelContenido.removeAll();

                        // Crear panel de navegación con información del curso seleccionado
                        JPanel panelNavegacion = new JPanel(new BorderLayout());
                        panelNavegacion.setBackground(new Color(51, 153, 255));
                        panelNavegacion.setPreferredSize(new Dimension(0, 40));

                        JLabel lblInfo = new JLabel("Notas - " + seleccion);
                        lblInfo.setForeground(Color.WHITE);
                        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
                        lblInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));

                        JButton btnCambiarMateria = new JButton("Cambiar Materia");
                        btnCambiarMateria.setFont(new Font("Arial", Font.PLAIN, 12));
                        btnCambiarMateria.setPreferredSize(new Dimension(120, 30));
                        btnCambiarMateria.addActionListener(evt -> {
                            // Mostrar nuevamente el panel de selección
                            panelSeleccion.setVisible(true);
                            // Limpiar el contenido
                            panelContenido.removeAll();
                            panelContenido.revalidate();
                            panelContenido.repaint();
                        });

                        panelNavegacion.add(lblInfo, BorderLayout.WEST);
                        panelNavegacion.add(btnCambiarMateria, BorderLayout.EAST);

                        panelContenido.add(panelNavegacion, BorderLayout.NORTH);

                        // Cargar el panel correspondiente según la selección
                        if (rbTrabajos.isSelected()) {
                            System.out.println("Creando NotasProfesorPanel...");
                            NotasProfesorPanel panelNotas = new NotasProfesorPanel(
                                profesorId, info.cursoId, info.materiaId);
                            panelContenido.add(panelNotas, BorderLayout.CENTER);
                        } else {
                            System.out.println("Creando NotasBimestralesPanel...");
                            NotasBimestralesPanel panelBimestral = new NotasBimestralesPanel(
                                profesorId, info.cursoId, info.materiaId);
                            panelContenido.add(panelBimestral, BorderLayout.CENTER);
                        }

                        panelContenido.revalidate();
                        panelContenido.repaint();
                        panelCompleto.revalidate();
                        panelCompleto.repaint();
                        System.out.println("Panel de notas cargado exitosamente y selector ocultado");
                        
                    } catch (Exception ex) {
                        System.err.println("Error al cargar panel de notas: " + ex.getMessage());
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ventana, 
                            "Error al cargar panel: " + ex.getMessage() + 
                            "\nDetalles: " + ex.getClass().getName() +
                            "\nSelección: " + seleccion);
                    }
                }
            });

            // Mostrar el panel completo
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panelCompleto, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (SQLException ex) {
            System.err.println("Error SQL al cargar notas: " + ex.getMessage());
            ex.printStackTrace();
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
            System.out.println("Cargando panel de asistencias para profesor ID: " + profesorId);
            
            // Crear el panel completo que será mostrado
            JPanel panelCompleto = new JPanel(new BorderLayout());
            
            // Panel superior para selección
            JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
            JComboBox<String> comboMaterias = new JComboBox<>();
            
            // Estructura mejorada para almacenar los datos
            Map<String, MateriaInfo> materiaInfoMap = new HashMap<>();

            String query = """
                SELECT DISTINCT 
                    c.id as curso_id, 
                    CONCAT(c.anio, '°', c.division) as curso, 
                    m.id as materia_id, 
                    m.nombre as materia 
                FROM cursos c 
                JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id 
                JOIN materias m ON pcm.materia_id = m.id 
                WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' 
                ORDER BY c.anio, c.division, m.nombre
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            int contadorMaterias = 0;
            while (rs.next()) {
                String item = rs.getString("curso") + " - " + rs.getString("materia");
                comboMaterias.addItem(item);
                
                // Crear objeto con la información completa
                MateriaInfo info = new MateriaInfo(
                    rs.getInt("curso_id"),
                    rs.getInt("materia_id"),
                    rs.getString("curso"),
                    rs.getString("materia")
                );
                materiaInfoMap.put(item, info);
                contadorMaterias++;
                
                System.out.println("Cargada materia para asistencias: " + item + 
                                 " - Curso ID: " + info.cursoId + ", Materia ID: " + info.materiaId);
            }
            
            System.out.println("Total materias para asistencias: " + contadorMaterias);

            if (contadorMaterias == 0) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas para este profesor.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            comboMaterias.setPreferredSize(new Dimension(300, 30));
            panelSeleccion.add(lblSeleccion);
            panelSeleccion.add(comboMaterias);

            // Botón para cargar el panel seleccionado
            JButton btnCargar = new JButton("Cargar");
            panelSeleccion.add(btnCargar);

            // Agregar panel de selección al panel completo
            panelCompleto.add(panelSeleccion, BorderLayout.NORTH);

            // Panel central donde se mostrará el contenido
            JPanel panelContenido = new JPanel(new BorderLayout());
            panelCompleto.add(panelContenido, BorderLayout.CENTER);

            // Listener para el botón
            btnCargar.addActionListener(e -> {
                String seleccion = (String) comboMaterias.getSelectedItem();
                if (seleccion != null) {
                    try {
                        System.out.println("Procesando selección de asistencias: " + seleccion);
                        
                        MateriaInfo info = materiaInfoMap.get(seleccion);
                        if (info == null) {
                            JOptionPane.showMessageDialog(ventana, 
                                "Error: No se encontró información para la selección: " + seleccion);
                            return;
                        }
                        
                        System.out.println("Info encontrada para asistencias - Curso ID: " + info.cursoId + 
                                         ", Materia ID: " + info.materiaId);
                        
                        // OCULTAR el panel de selección después de cargar
                        panelSeleccion.setVisible(false);

                        // Limpiar el panel de contenido
                        panelContenido.removeAll();

                        // Crear panel de navegación con información del curso seleccionado
                        JPanel panelNavegacion = new JPanel(new BorderLayout());
                        panelNavegacion.setBackground(new Color(51, 153, 255));
                        panelNavegacion.setPreferredSize(new Dimension(0, 40));

                        JLabel lblInfo = new JLabel("Asistencias - " + seleccion);
                        lblInfo.setForeground(Color.WHITE);
                        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
                        lblInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));

                        JButton btnCambiarMateria = new JButton("Cambiar Materia");
                        btnCambiarMateria.setFont(new Font("Arial", Font.PLAIN, 12));
                        btnCambiarMateria.setPreferredSize(new Dimension(120, 30));
                        btnCambiarMateria.addActionListener(evt -> {
                            // Mostrar nuevamente el panel de selección
                            panelSeleccion.setVisible(true);
                            // Limpiar el contenido
                            panelContenido.removeAll();
                            panelContenido.revalidate();
                            panelContenido.repaint();
                        });

                        panelNavegacion.add(lblInfo, BorderLayout.WEST);
                        panelNavegacion.add(btnCambiarMateria, BorderLayout.EAST);

                        panelContenido.add(panelNavegacion, BorderLayout.NORTH);

                        System.out.println("Creando AsistenciaProfesorPanel...");
                        AsistenciaProfesorPanel panelAsistencia = new AsistenciaProfesorPanel(
                                profesorId,
                                info.cursoId,
                                info.materiaId
                        );

                        panelContenido.add(panelAsistencia, BorderLayout.CENTER);
                        panelContenido.revalidate();
                        panelContenido.repaint();
                        panelCompleto.revalidate();
                        panelCompleto.repaint();
                        System.out.println("Panel de asistencias cargado exitosamente y selector ocultado");
                        
                    } catch (Exception ex) {
                        System.err.println("Error al cargar panel de asistencias: " + ex.getMessage());
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ventana, 
                            "Error al cargar panel: " + ex.getMessage() + 
                            "\nDetalles: " + ex.getClass().getName() +
                            "\nSelección: " + seleccion);
                    }
                }
            });

            // Mostrar el panel completo
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panelCompleto, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (SQLException ex) {
            System.err.println("Error SQL al cargar asistencias: " + ex.getMessage());
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
    
    /**
     * Clase interna para almacenar información de curso y materia.
     */
    private static class MateriaInfo {
        final int cursoId;
        final int materiaId;
        final String cursoNombre;
        final String materiaNombre;
        
        public MateriaInfo(int cursoId, int materiaId, String cursoNombre, String materiaNombre) {
            this.cursoId = cursoId;
            this.materiaId = materiaId;
            this.cursoNombre = cursoNombre;
            this.materiaNombre = materiaNombre;
        }
        
        @Override
        public String toString() {
            return "MateriaInfo{cursoId=" + cursoId + ", materiaId=" + materiaId + 
                   ", curso='" + cursoNombre + "', materia='" + materiaNombre + "'}";
        }
    }
}