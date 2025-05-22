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

/**
 * Gestor de paneles específico para el rol de Preceptor.
 * Versión mejorada con selector visual de cursos.
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
                    JOptionPane.showMessageDialog(ventana,
                            "Funcionalidad de exportación de asistencias en desarrollo.",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
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
                            "Error al cargar panel: " + ex.getMessage() + 
                            "\nDetalles: " + ex.getClass().getName() +
                            "\nSelección: " + cursoSeleccionado);
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
}