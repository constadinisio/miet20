package main.java.views.users.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.DefaultCellEditor;
import main.java.dao.TemaDiarioDAO;
import main.java.models.Curso;
import main.java.models.TemaDiario;
import main.java.models.Materia;

/**
 * Visor de Libro de Temas para roles Directivo y Preceptor
 * Muestra los temas cargados por profesores en un curso y materia específicos
 */
public class LibroTemasVisor extends JPanel {
    
    private int userId;
    private int userRol; // 1 = Directivo, 2 = Preceptor
    private Curso curso;
    private Materia materia;
    private TemaDiarioDAO temaDiarioDAO;
    private List<TemaDiario> temasActuales; // Para mantener referencia a los temas cargados
    
    // Componentes UI
    private JTable tablaTemas;
    private DefaultTableModel modeloTabla;
    private JButton btnVolver;
    private JButton btnActualizar;
    private JLabel lblInfo;
    private JScrollPane scrollPane;
    
    // Callbacks
    private Runnable onVolverCallback;
    private java.util.function.BiConsumer<JPanel, String> onMostrarPanelCallback;
    
    /**
     * Constructor del visor de libro de temas
     */
    public LibroTemasVisor(int userId, int userRol, Curso curso, Materia materia,
                          java.util.function.BiConsumer<JPanel, String> onMostrarPanelCallback,
                          Runnable onVolverCallback) {
        this.userId = userId;
        this.userRol = userRol;
        this.curso = curso;
        this.materia = materia;
        this.onMostrarPanelCallback = onMostrarPanelCallback;
        this.onVolverCallback = onVolverCallback;
        this.temaDiarioDAO = new TemaDiarioDAO();
        
        initComponents();
        cargarTemas();
        configurarEventos();
    }
    
    /**
     * Inicializa los componentes de la interfaz
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con información
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Título principal
        JLabel lblTitulo = new JLabel("Libro de Temas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(142, 68, 173));
        
        // Información del curso y materia
        String rolNombre = (userRol == 1) ? "Directivo" : "Preceptor";
        lblInfo = new JLabel(String.format(
            "<html><b>Curso:</b> %s &nbsp;&nbsp;&nbsp; <b>Materia:</b> %s &nbsp;&nbsp;&nbsp; <b>Rol:</b> %s</html>",
            curso.toString(), materia.getNombre(), rolNombre
        ));
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfo.setForeground(new Color(52, 73, 94));
        
        JPanel infoPrincipal = new JPanel(new BorderLayout());
        infoPrincipal.setBackground(Color.WHITE);
        infoPrincipal.add(lblTitulo, BorderLayout.NORTH);
        infoPrincipal.add(lblInfo, BorderLayout.SOUTH);
        
        panelSuperior.add(infoPrincipal, BorderLayout.CENTER);
        
        // Panel de botones superior
        JPanel panelBotonesSup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonesSup.setBackground(Color.WHITE);
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setPreferredSize(new Dimension(100, 35));
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.setBackground(new Color(52, 152, 219));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        panelBotonesSup.add(btnActualizar);
        
        panelSuperior.add(panelBotonesSup, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Tabla de temas
        String[] columnas = {
            "Fecha", "Día", "Tema Desarrollado", "Actividades Desarrolladas", "Clases", "Carácter", "Profesor", "Observaciones", "Estado"
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo los Directivos (rol 1) pueden editar observaciones (columna 7)
                return userRol == 1 && column == 7;
            }
        };
        
        tablaTemas = new JTable(modeloTabla);
        configurarTabla();
        
        scrollPane = new JScrollPane(tablaTemas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior con botones
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        
        // Botón Guardar Observaciones (solo para Directivos)
        if (userRol == 1) {
            JButton btnGuardarObservaciones = new JButton("Guardar Observaciones");
            btnGuardarObservaciones.setPreferredSize(new Dimension(180, 40));
            btnGuardarObservaciones.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnGuardarObservaciones.setBackground(new Color(46, 204, 113));
            btnGuardarObservaciones.setForeground(Color.WHITE);
            btnGuardarObservaciones.setFocusPainted(false);
            btnGuardarObservaciones.setBorderPainted(false);
            
            btnGuardarObservaciones.addActionListener(event -> guardarObservaciones());
            panelInferior.add(btnGuardarObservaciones);
            
            // Espaciador
            panelInferior.add(Box.createHorizontalStrut(20));
        }
        
        btnVolver = new JButton("Volver al Selector");
        btnVolver.setPreferredSize(new Dimension(150, 40));
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setBackground(new Color(149, 165, 166));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        panelInferior.add(btnVolver);
        
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Configura la apariencia de la tabla
     */
    private void configurarTabla() {
        // Configuración general
        tablaTemas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaTemas.setRowHeight(25);
        tablaTemas.setGridColor(new Color(236, 240, 241));
        tablaTemas.setShowGrid(true);
        tablaTemas.setIntercellSpacing(new Dimension(1, 1));
        tablaTemas.setSelectionBackground(new Color(232, 218, 239));
        tablaTemas.setSelectionForeground(Color.BLACK);
        
        // Header
        JTableHeader header = tablaTemas.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(142, 68, 173));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        
        // Anchos de columnas
        tablaTemas.getColumnModel().getColumn(0).setPreferredWidth(80);  // Fecha
        tablaTemas.getColumnModel().getColumn(1).setPreferredWidth(80);  // Día
        tablaTemas.getColumnModel().getColumn(2).setPreferredWidth(250); // Tema Desarrollado
        tablaTemas.getColumnModel().getColumn(3).setPreferredWidth(250); // Actividades Desarrolladas
        tablaTemas.getColumnModel().getColumn(4).setPreferredWidth(60);  // Clases
        tablaTemas.getColumnModel().getColumn(5).setPreferredWidth(80);  // Carácter
        tablaTemas.getColumnModel().getColumn(6).setPreferredWidth(120); // Profesor
        tablaTemas.getColumnModel().getColumn(7).setPreferredWidth(200); // Observaciones
        tablaTemas.getColumnModel().getColumn(8).setPreferredWidth(80);  // Estado
        
        // Editor personalizado para observaciones (solo para Directivos)
        if (userRol == 1) {
            JTextArea textArea = new JTextArea();
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JScrollPane scrollPaneEditor = new JScrollPane(textArea);
            scrollPaneEditor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField()) {
                private JTextArea editor = new JTextArea();
                private JScrollPane scrollPane = new JScrollPane(editor);
                
                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, 
                        boolean isSelected, int row, int column) {
                    editor.setText(value != null ? value.toString() : "");
                    editor.setWrapStyleWord(true);
                    editor.setLineWrap(true);
                    editor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    scrollPane.setPreferredSize(new Dimension(200, 80));
                    return scrollPane;
                }
                
                @Override
                public Object getCellEditorValue() {
                    return editor.getText();
                }
            };
            
            tablaTemas.getColumnModel().getColumn(7).setCellEditor(cellEditor);
        }
    }
    
    /**
     * Carga los temas del curso y materia seleccionados
     */
    private void cargarTemas() {
        try {
            System.out.println("🔍 Cargando temas para:");
            System.out.println("   - Curso ID: " + curso.getId() + " (" + curso.toString() + ")");
            System.out.println("   - Materia ID: " + materia.getId() + " (" + materia.getNombre() + ")");
            
            // Limpiar tabla
            modeloTabla.setRowCount(0);
            
            // Obtener temas de la base de datos
            List<TemaDiario> temas = temaDiarioDAO.obtenerTemasPorCursoYMateria(curso.getId(), materia.getId());
            this.temasActuales = temas; // Almacenar para uso posterior
            
            System.out.println("✅ Temas encontrados: " + temas.size());
            
            // Llenar tabla
            for (TemaDiario tema : temas) {
                Object[] fila = {
                    tema.getFechaClase(),
                    tema.getDiaSemana(),
                    tema.getTema(),
                    tema.getActividadesDesarrolladas() != null ? tema.getActividadesDesarrolladas() : "",
                    tema.getRangoClases(),
                    tema.getCaracterClase(),
                    tema.getProfesorNombre() + " " + tema.getProfesorApellido(),
                    tema.getObservaciones() != null ? tema.getObservaciones() : "",
                    determinarEstado(tema)
                };
                modeloTabla.addRow(fila);
            }
            
            // Actualizar información
            lblInfo.setText(String.format(
                "<html><b>Curso:</b> %s &nbsp;&nbsp;&nbsp; <b>Materia:</b> %s &nbsp;&nbsp;&nbsp; <b>Temas:</b> %d &nbsp;&nbsp;&nbsp; <b>Rol:</b> %s</html>",
                curso.toString(), materia.getNombre(), temas.size(), 
                (userRol == 1) ? "Directivo" : "Preceptor"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar temas: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al cargar los temas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Obtiene el nombre completo del profesor
     */
    /**
     * Determina el estado visual del tema
     */
    private String determinarEstado(TemaDiario tema) {
        String estado = tema.getEstadoVisual();
        if (estado == null || estado.trim().isEmpty()) {
            return "Normal";
        }
        return estado;
    }
    
    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarTemas();
            }
        });
        
        btnVolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volverAlSelector();
            }
        });
    }
    
    /**
     * Vuelve al selector de curso y materia
     */
    private void volverAlSelector() {
        try {
            LibroTemasSelector selector = new LibroTemasSelector(
                userId, userRol, onMostrarPanelCallback, onVolverCallback
            );
            
            if (onMostrarPanelCallback != null) {
                onMostrarPanelCallback.accept(selector, "Libro de Temas - Selector");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al volver al selector: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Guarda las observaciones editadas por el administrador
     */
    private void guardarObservaciones() {
        if (userRol != 1) {
            JOptionPane.showMessageDialog(this, 
                "Solo los Directivos pueden editar observaciones.",
                "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (temasActuales == null || temasActuales.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay temas cargados para editar.",
                "Sin Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            boolean cambiosGuardados = false;
            int errores = 0;
            
            System.out.println("🔍 Iniciando guardado de observaciones...");
            System.out.println("   - Filas en tabla: " + modeloTabla.getRowCount());
            System.out.println("   - Temas actuales: " + temasActuales.size());
            
            // Recorrer todas las filas de la tabla
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                String observacionTabla = (String) modeloTabla.getValueAt(i, 7); // Columna observaciones (ahora es la 7)
                TemaDiario tema = temasActuales.get(i);
                
                System.out.println("🔍 Fila " + i + ":");
                System.out.println("   - Tema ID: " + tema.getId());
                System.out.println("   - Observación original: '" + tema.getObservaciones() + "'");
                System.out.println("   - Observación en tabla: '" + observacionTabla + "'");
                
                // Verificar si la observación cambió
                String observacionOriginal = tema.getObservaciones() != null ? tema.getObservaciones() : "";
                String observacionNueva = observacionTabla != null ? observacionTabla.trim() : "";
                
                if (!observacionOriginal.equals(observacionNueva)) {
                    System.out.println("✏️ Cambio detectado - Actualizando tema ID: " + tema.getId());
                    
                    // Actualizar en la base de datos
                    boolean resultado = temaDiarioDAO.actualizarObservacionesTema(
                        tema.getId(), 
                        observacionNueva.isEmpty() ? null : observacionNueva, 
                        userId
                    );
                    
                    if (resultado) {
                        cambiosGuardados = true;
                        // Actualizar el objeto en memoria
                        tema.setObservaciones(observacionNueva.isEmpty() ? null : observacionNueva);
                        System.out.println("✅ Observación actualizada para tema ID: " + tema.getId());
                    } else {
                        errores++;
                        System.err.println("❌ Error al actualizar observación para tema ID: " + tema.getId());
                    }
                } else {
                    System.out.println("⚪ Sin cambios detectados para tema ID: " + tema.getId());
                }
            }
            
            // Mostrar resultado al usuario
            if (cambiosGuardados) {
                if (errores > 0) {
                    JOptionPane.showMessageDialog(this, 
                        String.format("Observaciones guardadas con %d errores.", errores),
                        "Guardado Parcial", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Todas las observaciones han sido guardadas correctamente.",
                        "Guardado Exitoso", JOptionPane.INFORMATION_MESSAGE);
                }
                // Recargar para refrescar la vista
                cargarTemas();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se detectaron cambios en las observaciones.",
                    "Sin Cambios", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar observaciones: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al guardar observaciones: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
