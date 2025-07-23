package main.java.views.users.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import main.java.dao.TemaDiarioDAO;
import main.java.models.Curso;
import main.java.models.Materia;
import main.java.services.UserService;
import main.java.views.users.Profesor.CargaTemasPanel;

/**
 * Panel selector para Libro de Temas - Directivos y Preceptores
 * Permite seleccionar curso y materia antes de abrir el libro de temas
 */
public class LibroTemasSelector extends JPanel {
    
    private int userId;
    private int userRol;
    private TemaDiarioDAO temaDiarioDAO;
    private UserService userService;
    
    // Componentes UI
    private JComboBox<Curso> comboCursos;
    private JComboBox<Materia> comboMaterias;
    private JButton btnAbrir;
    private JButton btnVolver;
    private JLabel lblInfo;
    
    // Callback para mostrar el panel principal
    private Runnable onVolverCallback;
    private java.util.function.BiConsumer<JPanel, String> onMostrarPanelCallback;
    
    /**
     * Constructor del selector de Libro de Temas
     */
    public LibroTemasSelector(int userId, int userRol, 
                             java.util.function.BiConsumer<JPanel, String> onMostrarPanelCallback,
                             Runnable onVolverCallback) {
        this.userId = userId;
        this.userRol = userRol;
        this.onMostrarPanelCallback = onMostrarPanelCallback;
        this.onVolverCallback = onVolverCallback;
        this.temaDiarioDAO = new TemaDiarioDAO();
        this.userService = new UserService();
        
        initComponents();
        cargarCursos();
        configurarEventos();
    }
    
    /**
     * Inicializa los componentes de la interfaz
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Título
        JLabel lblTitulo = new JLabel("Libro de Temas - Selector de Curso y Materia");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelPrincipal.add(lblTitulo, gbc);
        
        // Info del usuario
        String rolNombre = (userRol == 1) ? "Directivo" : "Preceptor";
        lblInfo = new JLabel("Accediendo como: " + rolNombre);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblInfo.setForeground(new Color(127, 140, 141));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panelPrincipal.add(lblInfo, gbc);
        
        // Espacio
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        panelPrincipal.add(Box.createVerticalStrut(20), gbc);
        
        // Selector de Curso
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel lblCurso = new JLabel("Seleccionar Curso:");
        lblCurso.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 3;
        panelPrincipal.add(lblCurso, gbc);
        
        comboCursos = new JComboBox<>();
        comboCursos.setPreferredSize(new Dimension(300, 35));
        comboCursos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(comboCursos, gbc);
        
        // Selector de Materia
        JLabel lblMateria = new JLabel("Seleccionar Materia:");
        lblMateria.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panelPrincipal.add(lblMateria, gbc);
        
        comboMaterias = new JComboBox<>();
        comboMaterias.setPreferredSize(new Dimension(300, 35));
        comboMaterias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboMaterias.setEnabled(false); // Inicialmente deshabilitado
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(comboMaterias, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setBackground(Color.WHITE);
        
        btnAbrir = new JButton("Abrir Libro de Temas");
        btnAbrir.setPreferredSize(new Dimension(180, 40));
        btnAbrir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAbrir.setBackground(new Color(142, 68, 173)); // Violeta
        btnAbrir.setForeground(Color.WHITE);
        btnAbrir.setFocusPainted(false);
        btnAbrir.setBorderPainted(false);
        btnAbrir.setEnabled(false); // Inicialmente deshabilitado
        panelBotones.add(btnAbrir);
        
        btnVolver = new JButton("Volver");
        btnVolver.setPreferredSize(new Dimension(120, 40));
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setBackground(new Color(149, 165, 166));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        panelBotones.add(btnVolver);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);
        panelPrincipal.add(panelBotones, gbc);
        
        add(panelPrincipal, BorderLayout.CENTER);
    }
    
    /**
     * Carga todos los cursos disponibles
     */
    private void cargarCursos() {
        try {
            List<Curso> cursos = temaDiarioDAO.obtenerTodosLosCursos();
            comboCursos.removeAllItems();
            comboCursos.addItem(null); // Item vacío
            
            for (Curso curso : cursos) {
                comboCursos.addItem(curso);
            }
            
            System.out.println("✅ Cursos cargados: " + cursos.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar cursos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al cargar la lista de cursos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carga las materias del curso seleccionado
     */
    private void cargarMaterias(Curso curso) {
        try {
            comboMaterias.removeAllItems();
            comboMaterias.addItem(null); // Item vacío
            
            if (curso != null) {
                List<Materia> materias = temaDiarioDAO.obtenerMateriasPorCurso(curso.getId());
                
                for (Materia materia : materias) {
                    comboMaterias.addItem(materia);
                }
                
                comboMaterias.setEnabled(true);
                System.out.println("✅ Materias cargadas para curso " + curso.getNombre() + ": " + materias.size());
            } else {
                comboMaterias.setEnabled(false);
            }
            
            // Validar botón Abrir
            validarBotonAbrir();
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar materias: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al cargar las materias: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Valida si el botón Abrir debe estar habilitado
     */
    private void validarBotonAbrir() {
        boolean habilitado = comboCursos.getSelectedItem() != null && 
                           comboMaterias.getSelectedItem() != null;
        btnAbrir.setEnabled(habilitado);
    }
    
    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Cambio de curso
        comboCursos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Curso cursoSeleccionado = (Curso) comboCursos.getSelectedItem();
                cargarMaterias(cursoSeleccionado);
            }
        });
        
        // Cambio de materia
        comboMaterias.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validarBotonAbrir();
            }
        });
        
        // Botón Abrir
        btnAbrir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirLibroDeTemas();
            }
        });
        
        // Botón Volver
        btnVolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onVolverCallback != null) {
                    onVolverCallback.run();
                }
            }
        });
    }
    
    /**
     * Abre el libro de temas con el curso y materia seleccionados
     */
    private void abrirLibroDeTemas() {
        try {
            Curso cursoSeleccionado = (Curso) comboCursos.getSelectedItem();
            Materia materiaSeleccionada = (Materia) comboMaterias.getSelectedItem();
            
            if (cursoSeleccionado == null || materiaSeleccionada == null) {
                JOptionPane.showMessageDialog(this, 
                    "Por favor seleccione un curso y una materia",
                    "Selección incompleta", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            System.out.println("🔍 Abriendo libro de temas:");
            System.out.println("   - Curso: " + cursoSeleccionado.getNombre());
            System.out.println("   - Materia: " + materiaSeleccionada.getNombre());
            System.out.println("   - Usuario: " + userId + " (rol " + userRol + ")");
            
            // Crear el panel de carga de temas con parámetros específicos
            LibroTemasVisor visor = new LibroTemasVisor(
                userId, userRol, 
                cursoSeleccionado, materiaSeleccionada,
                onMostrarPanelCallback, onVolverCallback
            );
            
            String titulo = "Libro de Temas - " + cursoSeleccionado.getNombre() + " - " + materiaSeleccionada.getNombre();
            
            if (onMostrarPanelCallback != null) {
                onMostrarPanelCallback.accept(visor, titulo);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al abrir libro de temas: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al abrir el libro de temas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
