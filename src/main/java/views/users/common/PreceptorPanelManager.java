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
import com.toedter.calendar.JDateChooser;
import main.java.database.Conexion;
import main.java.utils.PanelUtils; // Importación directa de PanelUtils
import main.java.views.users.Preceptor.AsistenciaPreceptorPanel;

/**
 * Gestor de paneles específico para el rol de Preceptor.
 * VERSIÓN SIMPLIFICADA para resolver problemas de visualización.
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
            System.out.println("Acción: " + actionCommand);
            
            switch (actionCommand) {
                case "notas":
                    JOptionPane.showMessageDialog(ventana,
                            "Funcionalidad de gestión de notas en desarrollo.",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "asistencias":
                    mostrarPanelAsistenciasDirect();
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
     * Método directo y simplificado para mostrar panel de asistencias.
     * Esta versión usa PanelUtils para garantizar la visualización correcta.
     */
    private void mostrarPanelAsistenciasDirect() {
        try {
            System.out.println("Iniciando mostrarPanelAsistenciasDirect...");
            
            // 1. Cargar cursos
            cargarCursos();
            
            // 2. Mostrar diálogo de selección simple
            String cursoStr = JOptionPane.showInputDialog(
                ventana,
                "Ingrese número de curso (por ejemplo, 4°2):",
                "Selección de Curso",
                JOptionPane.QUESTION_MESSAGE);
            
            if (cursoStr == null || cursoStr.trim().isEmpty()) {
                System.out.println("Selección de curso cancelada");
                return;
            }
            
            // 3. Buscar el ID del curso seleccionado
            Integer cursoId = obtenerIdCursoPorNombre(cursoStr.trim());
            
            if (cursoId == null) {
                JOptionPane.showMessageDialog(
                    ventana,
                    "No se encontró un curso con ese nombre. Por favor ingrese un formato válido (ej: 4°2)",
                    "Curso no encontrado",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 4. Obtener panel principal (donde mostraremos el panel de asistencia)
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            
            // 5. Crear un panel simple para navegación superior
            JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnVolver = new JButton("Volver al inicio");
            btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());
            
            JLabel lblCurso = new JLabel("Asistencias del curso: " + cursoStr);
            lblCurso.setFont(new Font("Arial", Font.BOLD, 14));
            
            navPanel.add(btnVolver);
            navPanel.add(lblCurso);
            
            // 6. Preparar panel para contenido
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());
            panelPrincipal.add(navPanel, BorderLayout.NORTH);
            
            // 7. Crear mensaje de carga mientras se inicializa el panel de asistencia
            final JLabel lblCargando = new JLabel("Cargando panel de asistencias, por favor espere...");
            lblCargando.setHorizontalAlignment(JLabel.CENTER);
            lblCargando.setFont(new Font("Arial", Font.BOLD, 18));
            panelPrincipal.add(lblCargando, BorderLayout.CENTER);
            
            // Actualizar UI para mostrar mensaje de carga
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
            
            // 8. Inicializar el panel de asistencia en segundo plano para evitar bloqueos
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("Creando AsistenciaPreceptorPanel...");
                    AsistenciaPreceptorPanel panelAsistencia = new AsistenciaPreceptorPanel(preceptorId, cursoId);
                    panelAsistencia.setPreferredSize(new Dimension(1200, 800));
                    
                    // Remover mensaje de carga
                    panelPrincipal.remove(lblCargando);
                    
                    // 9. Usar PanelUtils para añadir el panel al contenedor principal
                    PanelUtils.addPanelWithOriginalLayout(panelPrincipal, panelAsistencia, BorderLayout.CENTER);
                    
                    System.out.println("Panel de asistencia agregado correctamente");
                } catch (Exception ex) {
                    System.err.println("Error al crear panel de asistencia: " + ex.getMessage());
                    ex.printStackTrace();
                    panelPrincipal.remove(lblCargando);
                    
                    JLabel lblError = new JLabel("Error al cargar el panel de asistencias: " + ex.getMessage());
                    lblError.setHorizontalAlignment(JLabel.CENTER);
                    lblError.setForeground(Color.RED);
                    panelPrincipal.add(lblError, BorderLayout.CENTER);
                    
                    panelPrincipal.revalidate();
                    panelPrincipal.repaint();
                }
            });
            
        } catch (Exception ex) {
            System.err.println("Error en mostrarPanelAsistenciasDirect: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al mostrar panel de asistencias: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Obtiene el ID de un curso por su nombre.
     * 
     * @param nombreCurso Nombre del curso en formato "4°2"
     * @return ID del curso o null si no se encuentra
     */
    private Integer obtenerIdCursoPorNombre(String nombreCurso) {
        System.out.println("Buscando curso: " + nombreCurso);
        
        // Primero buscar directamente en el mapa (clave exacta)
        Integer id = cursosMap.get(nombreCurso);
        if (id != null) {
            return id;
        }
        
        // Si no lo encuentra, intentar con búsqueda parcial
        for (Map.Entry<String, Integer> entry : cursosMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(nombreCurso) || 
                entry.getKey().replace(" ", "").equalsIgnoreCase(nombreCurso.replace(" ", ""))) {
                return entry.getValue();
            }
        }
        
        // Si todo falla, intentar obtener el curso de la base de datos
        return obtenerIdCursoDesdeBD(nombreCurso);
    }
    
    /**
     * Intenta obtener el ID del curso directamente desde la BD.
     * 
     * @param nombreCurso Nombre del curso (se intentará parsear)
     * @return ID del curso o null si no se encuentra
     */
    private Integer obtenerIdCursoDesdeBD(String nombreCurso) {
        try {
            // Intentar extraer año y división del formato "4°2"
            // Aceptar formatos: "4°2", "4º2", "4 2", "42", etc.
            String cleaned = nombreCurso.replaceAll("[^0-9]", " ").trim();
            String[] parts = cleaned.split("\\s+");
            
            if (parts.length >= 2) {
                int anio = Integer.parseInt(parts[0]);
                String division = parts[1];
                
                String query = "SELECT id FROM cursos WHERE anio = ? AND division = ? AND estado = 'activo'";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, anio);
                ps.setString(2, division);
                
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("id");
                    System.out.println("Curso encontrado en BD: ID=" + id);
                    return id;
                }
            } else if (parts.length == 1 && parts[0].length() >= 2) {
                // Intentar con formato pegado "42" -> año=4, división=2
                int anio = Integer.parseInt(parts[0].substring(0, 1));
                String division = parts[0].substring(1);
                
                String query = "SELECT id FROM cursos WHERE anio = ? AND division = ? AND estado = 'activo'";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, anio);
                ps.setString(2, division);
                
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("id");
                    System.out.println("Curso encontrado en BD (formato compacto): ID=" + id);
                    return id;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar curso en BD: " + e.getMessage());
        }
        
        System.out.println("No se encontró el curso: " + nombreCurso);
        return null;
    }

    /**
     * Carga los cursos desde la base de datos.
     */
    private void cargarCursos() {
        try {
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
            
            System.out.println("Cursos cargados: " + cursosMap.size());
            // Imprimir los cursos para depuración
            cursosMap.forEach((nombre, id) -> System.out.println("  " + nombre + " -> ID=" + id));
            
        } catch (SQLException ex) {
            System.err.println("Error al cargar cursos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}