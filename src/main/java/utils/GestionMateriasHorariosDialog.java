/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package main.java.utils;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;
import main.java.database.Conexion;

public class GestionMateriasHorariosDialog extends javax.swing.JDialog {

    private int userId;
    private String nombreUsuario;
    private String rolUsuario;
    private Connection conect;
    private JTabbedPane tabbedPane;
    private JTable tablaMaterias;
    private JTable tablaHorarios;
    private DefaultTableModel modeloMaterias;
    private DefaultTableModel modeloHorarios;
    private JComboBox<String> comboCursos;
    private Map<String, Integer> mapaCursos = new HashMap<>();

    public GestionMateriasHorariosDialog(Frame parent, boolean modal, int userId,
            String nombreUsuario, String rolUsuario) {
        super(parent, modal);
        this.userId = userId;
        this.nombreUsuario = nombreUsuario;
        this.rolUsuario = rolUsuario;

        initComponents(); // Este método debería estar creando tus componentes básicos

        // Inicializar la conexión primero
        probar_conexion();

        // Ahora crear componentes que podrían necesitar la conexión
        crearComponentes();

        // Inicializar los modelos de las tablas
        inicializarTablas();
        cargarCursos();

        setTitle("Gestión de Materias y Horarios - " + nombreUsuario);
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    /**
     * Método para establecer la conexión a la base de datos
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Crea e inicializa los componentes principales que no están en el diseño.
     */
    /**
     * Crea e inicializa los componentes principales que no están en el diseño.
     */
    private void crearComponentes() {
        // Crear tabbedPane
        tabbedPane = new JTabbedPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Panel de materias
        JPanel panelMaterias = new JPanel(new BorderLayout());
        tabbedPane.addTab("Materias", panelMaterias);

        // Panel de horarios
        JPanel panelHorarios = new JPanel(new BorderLayout());
        tabbedPane.addTab("Horarios", panelHorarios);

        // Panel de lista de materias
        JPanel panelListaMaterias = new JPanel(new BorderLayout());
        tabbedPane.addTab("Lista de Materias", panelListaMaterias);

        // Panel superior con filtros (común para ambas pestañas)
        JPanel panelFiltros = new JPanel();
        comboCursos = new JComboBox<>();
        panelFiltros.add(new JLabel("Curso:"));
        panelFiltros.add(comboCursos);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.addActionListener(e -> filtrarPorCurso());
        panelFiltros.add(btnFiltrar);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescarDatos());
        panelFiltros.add(btnRefrescar);

        getContentPane().add(panelFiltros, BorderLayout.NORTH);

        // Crear tablas
        tablaMaterias = new JTable();
        JScrollPane scrollMaterias = new JScrollPane(tablaMaterias);
        panelMaterias.add(scrollMaterias, BorderLayout.CENTER);

        // Panel de botones para materias
        JPanel panelBotonesMaterias = new JPanel();
        JButton btnAsignarMateria = new JButton("Asignar Materia");
        btnAsignarMateria.addActionListener(e -> asignarMateria());
        panelBotonesMaterias.add(btnAsignarMateria);

        JButton btnQuitarMateria = new JButton("Quitar Materia");
        btnQuitarMateria.addActionListener(e -> quitarMateria());
        panelBotonesMaterias.add(btnQuitarMateria);

        JButton btnCrearMateria = new JButton("Crear Nueva Materia");
        btnCrearMateria.addActionListener(e -> crearNuevaMateria());
        panelBotonesMaterias.add(btnCrearMateria);

        panelMaterias.add(panelBotonesMaterias, BorderLayout.SOUTH);

        // Configurar tabla de horarios
        tablaHorarios = new JTable();
        JScrollPane scrollHorarios = new JScrollPane(tablaHorarios);
        panelHorarios.add(scrollHorarios, BorderLayout.CENTER);

        // Panel de botones para horarios
        JPanel panelBotonesHorarios = new JPanel();
        JButton btnAgregarHorario = new JButton("Agregar Horario");
        btnAgregarHorario.addActionListener(e -> agregarHorario());
        panelBotonesHorarios.add(btnAgregarHorario);

        JButton btnEditarHorario = new JButton("Editar Horario");
        btnEditarHorario.addActionListener(e -> editarHorario());
        panelBotonesHorarios.add(btnEditarHorario);

        JButton btnEliminarHorario = new JButton("Eliminar Horario");
        btnEliminarHorario.addActionListener(e -> eliminarHorario());
        panelBotonesHorarios.add(btnEliminarHorario);

        panelHorarios.add(panelBotonesHorarios, BorderLayout.SOUTH);

        // Tabla de lista de materias
        JTable tablaListaMaterias = new JTable();
        DefaultTableModel modeloListaMaterias = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modeloListaMaterias.addColumn("ID");
        modeloListaMaterias.addColumn("Nombre");
        modeloListaMaterias.addColumn("Estado");

        tablaListaMaterias.setModel(modeloListaMaterias);
        tablaListaMaterias.getColumnModel().getColumn(0).setMinWidth(0);
        tablaListaMaterias.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollListaMaterias = new JScrollPane(tablaListaMaterias);
        panelListaMaterias.add(scrollListaMaterias, BorderLayout.CENTER);

        // Panel de botones para lista de materias
        JPanel panelBotonesListaMaterias = new JPanel();
        JButton btnNuevaMateria = new JButton("Nueva Materia");
        btnNuevaMateria.addActionListener(e -> crearNuevaMateria());
        panelBotonesListaMaterias.add(btnNuevaMateria);

        JButton btnEditarMateriaExistente = new JButton("Editar Materia");
        btnEditarMateriaExistente.addActionListener(e -> editarMateriaExistente(tablaListaMaterias));
        panelBotonesListaMaterias.add(btnEditarMateriaExistente);

        panelListaMaterias.add(panelBotonesListaMaterias, BorderLayout.SOUTH);

        // Cargar la lista de materias
        cargarListaMaterias(modeloListaMaterias);
    }

    /**
     * Carga todas las materias disponibles en el sistema.
     *
     * @param modelo El modelo de tabla donde se cargarán las materias
     */
    private void cargarListaMaterias(DefaultTableModel modelo) {
    try {
        modelo.setRowCount(0);

        // Modificar para incluir la columna categoría
        modelo.addColumn("ID");
        modelo.addColumn("Nombre");
        modelo.addColumn("Estado");
        modelo.addColumn("Categoría");  // Nueva columna

        String query = "SELECT id, nombre, estado, categoria FROM materias ORDER BY nombre";
        PreparedStatement ps = conect.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("estado"),
                rs.getString("categoria")  // Mostrar la categoría
            };
            modelo.addRow(row);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error al cargar la lista de materias: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    /**
     * Muestra un diálogo para editar una materia seleccionada.
     *
     * @param tabla La tabla de donde se obtiene la materia seleccionada
     */
    private void editarMateriaExistente(JTable tabla) {
    int filaSeleccionada = tabla.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una materia para editar");
        return;
    }

    int materiaId = (int) tabla.getValueAt(filaSeleccionada, 0);
    String nombreActual = (String) tabla.getValueAt(filaSeleccionada, 1);
    String estadoActual = (String) tabla.getValueAt(filaSeleccionada, 2);
    String categoriaActual = "";

    try {
        // Obtener la categoría actual
        String queryCategoría = "SELECT categoria FROM materias WHERE id = ?";
        PreparedStatement psCategoría = conect.prepareStatement(queryCategoría);
        psCategoría.setInt(1, materiaId);
        ResultSet rsCategoría = psCategoría.executeQuery();
        if (rsCategoría.next()) {
            categoriaActual = rsCategoría.getString("categoria");
        }

        // Preparar el diálogo
        JTextField txtNombreMateria = new JTextField(nombreActual, 20);
        JComboBox<String> comboEstado = new JComboBox<>(new String[]{"activo", "inactivo"});
        comboEstado.setSelectedItem(estadoActual);
        
        // Combo para categorías
        JComboBox<String> comboCategoria = new JComboBox<>();
        comboCategoria.addItem("TIC");
        comboCategoria.addItem("MULTIMEDIA");
        comboCategoria.addItem("PRIMER CICLO");
        comboCategoria.addItem("GENERAL DEL SEGUNDO CICLO");
        comboCategoria.addItem("TALLER PRIMER CICLO");
        
        // Seleccionar la categoría actual si existe
        if (categoriaActual != null && !categoriaActual.isEmpty()) {
            comboCategoria.setSelectedItem(categoriaActual);
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre de la materia:"));
        panel.add(txtNombreMateria);
        panel.add(new JLabel("Estado:"));
        panel.add(comboEstado);
        panel.add(new JLabel("Categoría:"));
        panel.add(comboCategoria);

        // Mostrar el diálogo
        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Materia",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Validar que se ingresó un nombre
            String nuevoNombre = txtNombreMateria.getText().trim();
            String nuevoEstado = comboEstado.getSelectedItem().toString();
            String nuevaCategoria = comboCategoria.getSelectedItem().toString();

            if (nuevoNombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre de la materia no puede estar vacío",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Actualizar la materia
            String queryUpdate = "UPDATE materias SET nombre = ?, estado = ?, categoria = ? WHERE id = ?";
            PreparedStatement psUpdate = conect.prepareStatement(queryUpdate);
            psUpdate.setString(1, nuevoNombre);
            psUpdate.setString(2, nuevoEstado);
            psUpdate.setString(3, nuevaCategoria);
            psUpdate.setInt(4, materiaId);
            psUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Materia actualizada correctamente");

            // Refrescar los datos
            refrescarDatos();
            DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
            cargarListaMaterias(modelo);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error al editar la materia: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void refrescarDatos() {
        cargarCursos(); // Recarga también materias y horarios

        // Actualizar la lista de materias si existe
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if ("Lista de Materias".equals(tabbedPane.getTitleAt(i))) {
                Component component = tabbedPane.getComponentAt(i);
                if (component instanceof JPanel) {
                    JPanel panel = (JPanel) component;
                    for (Component c : panel.getComponents()) {
                        if (c instanceof JScrollPane) {
                            JScrollPane scroll = (JScrollPane) c;
                            if (scroll.getViewport().getView() instanceof JTable) {
                                JTable tabla = (JTable) scroll.getViewport().getView();
                                cargarListaMaterias((DefaultTableModel) tabla.getModel());
                            }
                        }
                    }
                }
            }
        }
    }

    private void inicializarTablas() {
        // Inicializar modelo de materias
        modeloMaterias = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modeloMaterias.addColumn("ID");
        modeloMaterias.addColumn("Curso");
        modeloMaterias.addColumn("Materia");
        modeloMaterias.addColumn("Estado");

        tablaMaterias.setModel(modeloMaterias);

        // Ocultar columna ID
        tablaMaterias.getColumnModel().getColumn(0).setMinWidth(0);
        tablaMaterias.getColumnModel().getColumn(0).setMaxWidth(0);

        // Inicializar modelo de horarios
        modeloHorarios = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modeloHorarios.addColumn("ID");
        modeloHorarios.addColumn("Curso");
        modeloHorarios.addColumn("Materia");
        modeloHorarios.addColumn("Día");
        modeloHorarios.addColumn("Hora Inicio");
        modeloHorarios.addColumn("Hora Fin");

        tablaHorarios.setModel(modeloHorarios);

        // Ocultar columna ID
        tablaHorarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaHorarios.getColumnModel().getColumn(0).setMaxWidth(0);
    }

    private void cargarCursos() {
        try {
            conect = Conexion.getInstancia().verificarConexion();

            comboCursos.removeAllItems();
            comboCursos.addItem("Todos los cursos");
            mapaCursos.clear();

            String query = "SELECT id, CONCAT(anio, '°', division) as curso FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String curso = rs.getString("curso");
                int cursoId = rs.getInt("id");
                comboCursos.addItem(curso);
                mapaCursos.put(curso, cursoId);
            }

            // Cargar datos iniciales
            cargarMaterias();
            cargarHorarios();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar cursos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarMaterias() {
        try {
            modeloMaterias.setRowCount(0);

            // Construir consulta según filtro de curso
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT pcm.id, c.anio, c.division, m.nombre as materia, pcm.estado "
                    + "FROM profesor_curso_materia pcm "
                    + "JOIN cursos c ON pcm.curso_id = c.id "
                    + "JOIN materias m ON pcm.materia_id = m.id "
                    + "WHERE pcm.profesor_id = ? ");

            // Aplicar filtro de curso si está seleccionado
            if (comboCursos.getSelectedIndex() > 0) {
                String cursoSeleccionado = comboCursos.getSelectedItem().toString();
                int cursoId = mapaCursos.get(cursoSeleccionado);
                queryBuilder.append("AND pcm.curso_id = ").append(cursoId).append(" ");
            }

            queryBuilder.append("ORDER BY c.anio, c.division, m.nombre");

            PreparedStatement ps = conect.prepareStatement(queryBuilder.toString());
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getInt("anio") + "°" + rs.getString("division"),
                    rs.getString("materia"),
                    rs.getString("estado")
                };
                modeloMaterias.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar materias: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarHorarios() {
        try {
            modeloHorarios.setRowCount(0);

            // Construir consulta según filtro de curso
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT h.id, c.anio, c.division, m.nombre as materia, "
                    + "h.dia_semana, h.hora_inicio, h.hora_fin "
                    + "FROM horarios_materia h "
                    + "JOIN cursos c ON h.curso_id = c.id "
                    + "JOIN materias m ON h.materia_id = m.id "
                    + "WHERE h.profesor_id = ? ");

            // Aplicar filtro de curso si está seleccionado
            if (comboCursos.getSelectedIndex() > 0) {
                String cursoSeleccionado = comboCursos.getSelectedItem().toString();
                int cursoId = mapaCursos.get(cursoSeleccionado);
                queryBuilder.append("AND h.curso_id = ").append(cursoId).append(" ");
            }

            queryBuilder.append("ORDER BY c.anio, c.division, m.nombre");

            PreparedStatement ps = conect.prepareStatement(queryBuilder.toString());
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getInt("anio") + "°" + rs.getString("division"),
                    rs.getString("materia"),
                    rs.getString("dia_semana"),
                    rs.getTime("hora_inicio").toString(),
                    rs.getTime("hora_fin").toString()
                };
                modeloHorarios.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar horarios: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarPorCurso() {
        cargarMaterias();
        cargarHorarios();
    }

    // Métodos para gestión de materias
    private void asignarMateria() {
        try {
            // Preparar los datos para el diálogo
            JComboBox<String> comboCursoDialog = new JComboBox<>();
            JComboBox<String> comboMateriaDialog = new JComboBox<>();
            Map<String, Integer> mapaMaterias = new HashMap<>();

            // Cargar cursos
            String queryCursos = "SELECT id, CONCAT(anio, '°', division) as curso FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement psCursos = conect.prepareStatement(queryCursos);
            ResultSet rsCursos = psCursos.executeQuery();

            while (rsCursos.next()) {
                comboCursoDialog.addItem(rsCursos.getString("curso"));
                mapaCursos.put(rsCursos.getString("curso"), rsCursos.getInt("id"));
            }

            // Cargar materias
            String queryMaterias = "SELECT id, nombre FROM materias WHERE estado = 'activo' ORDER BY nombre";
            PreparedStatement psMaterias = conect.prepareStatement(queryMaterias);
            ResultSet rsMaterias = psMaterias.executeQuery();

            while (rsMaterias.next()) {
                comboMateriaDialog.addItem(rsMaterias.getString("nombre"));
                mapaMaterias.put(rsMaterias.getString("nombre"), rsMaterias.getInt("id"));
            }

            // Crear el panel del diálogo
            JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
            panel.add(new JLabel("Curso:"));
            panel.add(comboCursoDialog);
            panel.add(new JLabel("Materia:"));
            panel.add(comboMateriaDialog);

            // Mostrar el diálogo
            int result = JOptionPane.showConfirmDialog(this, panel, "Asignar Materia",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Verificar que se hayan seleccionado curso y materia
                if (comboCursoDialog.getSelectedIndex() == -1 || comboMateriaDialog.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar un curso y una materia");
                    return;
                }

                // Obtener IDs de curso y materia
                String cursoSeleccionado = comboCursoDialog.getSelectedItem().toString();
                String materiaSeleccionada = comboMateriaDialog.getSelectedItem().toString();

                int cursoId = mapaCursos.get(cursoSeleccionado);
                int materiaId = mapaMaterias.get(materiaSeleccionada);

                // Verificar si ya existe esta asignación
                String queryVerificar
                        = "SELECT id FROM profesor_curso_materia "
                        + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?";

                PreparedStatement psVerificar = conect.prepareStatement(queryVerificar);
                psVerificar.setInt(1, userId);
                psVerificar.setInt(2, cursoId);
                psVerificar.setInt(3, materiaId);

                ResultSet rsVerificar = psVerificar.executeQuery();

                if (rsVerificar.next()) {
                    // Ya existe, preguntar si desea reactivarla
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Esta asignación ya existe. ¿Desea reactivarla?",
                            "Confirmación", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        String queryUpdate
                                = "UPDATE profesor_curso_materia SET estado = 'activo' "
                                + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?";

                        PreparedStatement psUpdate = conect.prepareStatement(queryUpdate);
                        psUpdate.setInt(1, userId);
                        psUpdate.setInt(2, cursoId);
                        psUpdate.setInt(3, materiaId);
                        psUpdate.executeUpdate();
                    } else {
                        return;
                    }
                } else {
                    // No existe, crear nueva asignación
                    String queryInsert
                            = "INSERT INTO profesor_curso_materia (profesor_id, curso_id, materia_id, estado) "
                            + "VALUES (?, ?, ?, 'activo')";

                    PreparedStatement psInsert = conect.prepareStatement(queryInsert);
                    psInsert.setInt(1, userId);
                    psInsert.setInt(2, cursoId);
                    psInsert.setInt(3, materiaId);
                    psInsert.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Materia asignada correctamente");
                cargarMaterias(); // Recargar la tabla
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al asignar materia: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarMateria() {
        int filaSeleccionada = tablaMaterias.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una materia");
            return;
        }

        int idAsignacion = (int) modeloMaterias.getValueAt(filaSeleccionada, 0);
        String curso = (String) modeloMaterias.getValueAt(filaSeleccionada, 1);
        String materia = (String) modeloMaterias.getValueAt(filaSeleccionada, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de quitar la materia '" + materia + "' del curso " + curso + "?",
                "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Desactivar la asignación (no eliminarla)
                String query = "UPDATE profesor_curso_materia SET estado = 'inactivo' WHERE id = ?";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, idAsignacion);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Asignación desactivada correctamente");
                cargarMaterias(); // Recargar la tabla

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al quitar materia: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra un diálogo para crear una nueva materia en el sistema.
     */
    private void crearNuevaMateria() {
    try {
        // Preparar el diálogo
        JTextField txtNombreMateria = new JTextField(20);
        
        // Agregar ComboBox para categorías
        JComboBox<String> comboCategoria = new JComboBox<>();
        comboCategoria.addItem("TIC");
        comboCategoria.addItem("MULTIMEDIA");
        comboCategoria.addItem("PRIMER CICLO");
        comboCategoria.addItem("GENERAL DEL SEGUNDO CICLO");
        comboCategoria.addItem("TALLER PRIMER CICLO");

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre de la materia:"));
        panel.add(txtNombreMateria);
        panel.add(new JLabel("Categoría:"));
        panel.add(comboCategoria);

        // Mostrar el diálogo
        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Nueva Materia",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Validar que se ingresó un nombre
            String nombreMateria = txtNombreMateria.getText().trim();
            String categoria = comboCategoria.getSelectedItem().toString();
            
            if (nombreMateria.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar un nombre para la materia",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar si ya existe una materia con ese nombre
            String queryVerificar = "SELECT id FROM materias WHERE nombre = ?";
            PreparedStatement psVerificar = conect.prepareStatement(queryVerificar);
            psVerificar.setString(1, nombreMateria);
            ResultSet rsVerificar = psVerificar.executeQuery();

            if (rsVerificar.next()) {
                // Ya existe una materia con ese nombre
                int idMateria = rsVerificar.getInt("id");

                // Preguntar si desea reactivarla (si estaba inactiva) y actualizar su categoría
                String queryEstado = "SELECT estado FROM materias WHERE id = ?";
                PreparedStatement psEstado = conect.prepareStatement(queryEstado);
                psEstado.setInt(1, idMateria);
                ResultSet rsEstado = psEstado.executeQuery();

                if (rsEstado.next() && rsEstado.getString("estado").equalsIgnoreCase("inactivo")) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "La materia '" + nombreMateria + "' ya existe pero está inactiva. ¿Desea reactivarla y actualizar su categoría?",
                            "Materia existente", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Reactivar la materia y actualizar su categoría
                        String queryActivar = "UPDATE materias SET estado = 'activo', categoria = ? WHERE id = ?";
                        PreparedStatement psActivar = conect.prepareStatement(queryActivar);
                        psActivar.setString(1, categoria);
                        psActivar.setInt(2, idMateria);
                        psActivar.executeUpdate();

                        JOptionPane.showMessageDialog(this, "La materia ha sido reactivada y su categoría actualizada");
                    }
                } else {
                    // Si está activa, preguntar si desea actualizar su categoría
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Ya existe una materia con ese nombre. ¿Desea actualizar su categoría?",
                            "Materia existente", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        String queryUpdate = "UPDATE materias SET categoria = ? WHERE id = ?";
                        PreparedStatement psUpdate = conect.prepareStatement(queryUpdate);
                        psUpdate.setString(1, categoria);
                        psUpdate.setInt(2, idMateria);
                        psUpdate.executeUpdate();
                        
                        JOptionPane.showMessageDialog(this, "Categoría de la materia actualizada correctamente");
                    }
                }
            } else {
                // No existe, crear la materia con su categoría
                String queryInsert = "INSERT INTO materias (nombre, estado, categoria) VALUES (?, 'activo', ?)";
                PreparedStatement psInsert = conect.prepareStatement(queryInsert);
                psInsert.setString(1, nombreMateria);
                psInsert.setString(2, categoria);
                psInsert.executeUpdate();

                JOptionPane.showMessageDialog(this, "Materia creada exitosamente");
            }

            // Refrescar los datos
            refrescarDatos();
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error al crear la materia: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    // Métodos para gestión de horarios
    private void agregarHorario() {
        try {
            // Preparar los datos para el diálogo
            JComboBox<String> comboCursoDialog = new JComboBox<>();
            JComboBox<String> comboMateriaDialog = new JComboBox<>();
            JComboBox<String> comboDiaDialog = new JComboBox<>(
                    new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"});

            JSpinner spinnerHoraInicio = new JSpinner(new SpinnerDateModel());
            JSpinner spinnerHoraFin = new JSpinner(new SpinnerDateModel());

            // Configurar los spinners de hora
            JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerHoraInicio, "HH:mm");
            JSpinner.DateEditor editorFin = new JSpinner.DateEditor(spinnerHoraFin, "HH:mm");
            spinnerHoraInicio.setEditor(editorInicio);
            spinnerHoraFin.setEditor(editorFin);

            // Obtener las materias asignadas al profesor
            Map<String, Integer> mapaCursosAsignados = new HashMap<>();
            Map<String, Map<Integer, Integer>> mapaMaterias = new HashMap<>();

            String queryAsignaciones
                    = "SELECT c.id as curso_id, m.id as materia_id, "
                    + "CONCAT(c.anio, '°', c.division) as curso, m.nombre as materia "
                    + "FROM profesor_curso_materia pcm "
                    + "JOIN cursos c ON pcm.curso_id = c.id "
                    + "JOIN materias m ON pcm.materia_id = m.id "
                    + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY c.anio, c.division, m.nombre";

            PreparedStatement psAsignaciones = conect.prepareStatement(queryAsignaciones);
            psAsignaciones.setInt(1, userId);
            ResultSet rsAsignaciones = psAsignaciones.executeQuery();

            // Estructuras para almacenar cursos y materias por curso
            while (rsAsignaciones.next()) {
                String curso = rsAsignaciones.getString("curso");
                String materia = rsAsignaciones.getString("materia");
                int cursoId = rsAsignaciones.getInt("curso_id");
                int materiaId = rsAsignaciones.getInt("materia_id");

                // Añadir curso si es nuevo
                if (!mapaCursosAsignados.containsKey(curso)) {
                    mapaCursosAsignados.put(curso, cursoId);
                    comboCursoDialog.addItem(curso);
                    mapaMaterias.put(curso, new HashMap<>());
                }

                // Añadir materia para este curso
                mapaMaterias.get(curso).put(materiaId, materiaId);
            }

            // Añadir listener al combo de cursos para actualizar materias
            comboCursoDialog.addActionListener(e -> {
                comboMateriaDialog.removeAllItems();
                if (comboCursoDialog.getSelectedIndex() != -1) {
                    String cursoSeleccionado = comboCursoDialog.getSelectedItem().toString();

                    try {
                        // Obtener materias para este curso
                        String queryMateriasxCurso
                                = "SELECT m.id, m.nombre "
                                + "FROM profesor_curso_materia pcm "
                                + "JOIN materias m ON pcm.materia_id = m.id "
                                + "WHERE pcm.profesor_id = ? AND pcm.curso_id = ? AND pcm.estado = 'activo' "
                                + "ORDER BY m.nombre";

                        PreparedStatement psMateriasxCurso = conect.prepareStatement(queryMateriasxCurso);
                        psMateriasxCurso.setInt(1, userId);
                        psMateriasxCurso.setInt(2, mapaCursosAsignados.get(cursoSeleccionado));
                        ResultSet rsMateriasxCurso = psMateriasxCurso.executeQuery();

                        while (rsMateriasxCurso.next()) {
                            comboMateriaDialog.addItem(rsMateriasxCurso.getString("nombre"));
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Disparar el evento para cargar las materias del primer curso
            if (comboCursoDialog.getItemCount() > 0) {
                comboCursoDialog.setSelectedIndex(0);
            }

            // Crear el panel del diálogo
            JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
            panel.add(new JLabel("Curso:"));
            panel.add(comboCursoDialog);
            panel.add(new JLabel("Materia:"));
            panel.add(comboMateriaDialog);
            panel.add(new JLabel("Día:"));
            panel.add(comboDiaDialog);
            panel.add(new JLabel("Hora Inicio:"));
            panel.add(spinnerHoraInicio);
            panel.add(new JLabel("Hora Fin:"));
            panel.add(spinnerHoraFin);

            // Mostrar el diálogo
            int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Horario",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Verificar selecciones
                if (comboCursoDialog.getSelectedIndex() == -1
                        || comboMateriaDialog.getSelectedIndex() == -1
                        || comboDiaDialog.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(this, "Debe completar todos los campos");
                    return;
                }

                // Obtener los valores seleccionados
                String cursoSeleccionado = comboCursoDialog.getSelectedItem().toString();
                String materiaSeleccionada = comboMateriaDialog.getSelectedItem().toString();
                String diaSeleccionado = comboDiaDialog.getSelectedItem().toString();
                java.util.Date horaInicio = (java.util.Date) spinnerHoraInicio.getValue();
                java.util.Date horaFin = (java.util.Date) spinnerHoraFin.getValue();

                // Obtener solo las horas y minutos
                java.util.Calendar calInicio = java.util.Calendar.getInstance();
                calInicio.setTime(horaInicio);
                int horaInicioVal = calInicio.get(java.util.Calendar.HOUR_OF_DAY);
                int minInicioVal = calInicio.get(java.util.Calendar.MINUTE);

                java.util.Calendar calFin = java.util.Calendar.getInstance();
                calFin.setTime(horaFin);
                int horaFinVal = calFin.get(java.util.Calendar.HOUR_OF_DAY);
                int minFinVal = calFin.get(java.util.Calendar.MINUTE);

// Comparar usando valores numéricos
                int inicioMinutos = horaInicioVal * 60 + minInicioVal;
                int finMinutos = horaFinVal * 60 + minFinVal;

                if (finMinutos <= inicioMinutos) {
                    JOptionPane.showMessageDialog(this,
                            "La hora de fin debe ser posterior a la hora de inicio\n"
                            + "Inicio: " + horaInicioVal + ":" + minInicioVal
                            + " - Fin: " + horaFinVal + ":" + minFinVal,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Obtener IDs
                int cursoId = mapaCursosAsignados.get(cursoSeleccionado);

                // Obtener materia_id
                int materiaId = -1;
                String queryMateriaId = "SELECT id FROM materias WHERE nombre = ?";
                PreparedStatement psMateriaId = conect.prepareStatement(queryMateriaId);
                psMateriaId.setString(1, materiaSeleccionada);
                ResultSet rsMateriaId = psMateriaId.executeQuery();

                if (rsMateriaId.next()) {
                    materiaId = rsMateriaId.getInt("id");
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró la materia seleccionada");
                    return;
                }

                // Convertir horas a formato SQL Time
                Time sqlHoraInicio = new Time(horaInicio.getTime());
                Time sqlHoraFin = new Time(horaFin.getTime());

                // Verificar si ya existe un horario para este profesor, curso, materia y día
                String queryVerificar
                        = "SELECT id FROM horarios_materia "
                        + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? AND dia_semana = ?";

                PreparedStatement psVerificar = conect.prepareStatement(queryVerificar);
                psVerificar.setInt(1, userId);
                psVerificar.setInt(2, cursoId);
                psVerificar.setInt(3, materiaId);
                psVerificar.setString(4, diaSeleccionado);

                ResultSet rsVerificar = psVerificar.executeQuery();

                if (rsVerificar.next()) {
                    // Ya existe, preguntar si desea actualizarlo
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Ya existe un horario para esta materia en este día. ¿Desea actualizarlo?",
                            "Confirmación", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        String queryUpdate
                                = "UPDATE horarios_materia SET hora_inicio = ?, hora_fin = ? "
                                + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? AND dia_semana = ?";

                        PreparedStatement psUpdate = conect.prepareStatement(queryUpdate);
                        psUpdate.setTime(1, sqlHoraInicio);
                        psUpdate.setTime(2, sqlHoraFin);
                        psUpdate.setInt(3, userId);
                        psUpdate.setInt(4, cursoId);
                        psUpdate.setInt(5, materiaId);
                        psUpdate.setString(6, diaSeleccionado);
                        psUpdate.executeUpdate();
                    } else {
                        return;
                    }
                } else {
                    // No existe, insertar nuevo horario
                    String queryInsert
                            = "INSERT INTO horarios_materia (profesor_id, curso_id, materia_id, dia_semana, hora_inicio, hora_fin) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";

                    PreparedStatement psInsert = conect.prepareStatement(queryInsert);
                    psInsert.setInt(1, userId);
                    psInsert.setInt(2, cursoId);
                    psInsert.setInt(3, materiaId);
                    psInsert.setString(4, diaSeleccionado);
                    psInsert.setTime(5, sqlHoraInicio);
                    psInsert.setTime(6, sqlHoraFin);
                    psInsert.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Horario guardado correctamente");
                cargarHorarios(); // Recargar la tabla
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al agregar horario: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarHorario() {
        int filaSeleccionada = tablaHorarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un horario");
            return;
        }

        int idHorario = (int) modeloHorarios.getValueAt(filaSeleccionada, 0);

        try {
            // Obtener datos del horario seleccionado
            String query
                    = "SELECT h.dia_semana, h.hora_inicio, h.hora_fin, "
                    + "c.id as curso_id, m.id as materia_id, "
                    + "CONCAT(c.anio, '°', c.division) as curso, m.nombre as materia "
                    + "FROM horarios_materia h "
                    + "JOIN cursos c ON h.curso_id = c.id "
                    + "JOIN materias m ON h.materia_id = m.id "
                    + "WHERE h.id = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, idHorario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Preparar componentes del diálogo
                JLabel labelCurso = new JLabel(rs.getString("curso"));
                JLabel labelMateria = new JLabel(rs.getString("materia"));

                JComboBox<String> comboDia = new JComboBox<>(
                        new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"});
                comboDia.setSelectedItem(rs.getString("dia_semana"));

                // Preparar spinners para las horas
                JSpinner spinnerHoraInicio = new JSpinner(new SpinnerDateModel());
                JSpinner spinnerHoraFin = new JSpinner(new SpinnerDateModel());

                JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerHoraInicio, "HH:mm");
                JSpinner.DateEditor editorFin = new JSpinner.DateEditor(spinnerHoraFin, "HH:mm");
                spinnerHoraInicio.setEditor(editorInicio);
                spinnerHoraFin.setEditor(editorFin);

                // Establecer valores actuales
                java.util.Date horaInicio = rs.getTime("hora_inicio");
                java.util.Date horaFin = rs.getTime("hora_fin");

                spinnerHoraInicio.setValue(horaInicio);
                spinnerHoraFin.setValue(horaFin);

                // Crear panel del diálogo
                JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
                panel.add(new JLabel("Curso:"));
                panel.add(labelCurso);
                panel.add(new JLabel("Materia:"));
                panel.add(labelMateria);
                panel.add(new JLabel("Día:"));
                panel.add(comboDia);
                panel.add(new JLabel("Hora Inicio:"));
                panel.add(spinnerHoraInicio);
                panel.add(new JLabel("Hora Fin:"));
                panel.add(spinnerHoraFin);

                // Mostrar diálogo
                int result = JOptionPane.showConfirmDialog(this, panel, "Editar Horario",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    // Obtener valores seleccionados
                    String diaSeleccionado = comboDia.getSelectedItem().toString();
                    java.util.Date nuevaHoraInicio = (java.util.Date) spinnerHoraInicio.getValue();
                    java.util.Date nuevaHoraFin = (java.util.Date) spinnerHoraFin.getValue();

                    // Validar que la hora fin sea posterior a la hora inicio
                    if (nuevaHoraFin.before(nuevaHoraInicio) || nuevaHoraFin.equals(nuevaHoraInicio)) {
                        JOptionPane.showMessageDialog(this,
                                "La hora de fin debe ser posterior a la hora de inicio",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Convertir a SQL Time
                    Time sqlHoraInicio = new Time(nuevaHoraInicio.getTime());
                    Time sqlHoraFin = new Time(nuevaHoraFin.getTime());

                    // Actualizar horario
                    String queryUpdate
                            = "UPDATE horarios_materia SET dia_semana = ?, hora_inicio = ?, hora_fin = ? "
                            + "WHERE id = ?";

                    PreparedStatement psUpdate = conect.prepareStatement(queryUpdate);
                    psUpdate.setString(1, diaSeleccionado);
                    psUpdate.setTime(2, sqlHoraInicio);
                    psUpdate.setTime(3, sqlHoraFin);
                    psUpdate.setInt(4, idHorario);
                    psUpdate.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Horario actualizado correctamente");
                    cargarHorarios(); // Recargar la tabla
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al editar horario: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarHorario() {
        int filaSeleccionada = tablaHorarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un horario");
            return;
        }

        int idHorario = (int) modeloHorarios.getValueAt(filaSeleccionada, 0);
        String curso = (String) modeloHorarios.getValueAt(filaSeleccionada, 1);
        String materia = (String) modeloHorarios.getValueAt(filaSeleccionada, 2);
        String dia = (String) modeloHorarios.getValueAt(filaSeleccionada, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el horario de '" + materia + "' del curso " + curso + " el día " + dia + "?",
                "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Eliminar el horario
                String query = "DELETE FROM horarios_materia WHERE id = ?";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, idHorario);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Horario eliminado correctamente");
                cargarHorarios(); // Recargar la tabla

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar horario: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public GestionMateriasHorariosDialog(Frame parent, boolean modal) {
        this(parent, modal, -1, "Usuario de prueba", "Rol de prueba");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GestionMateriasHorariosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GestionMateriasHorariosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GestionMateriasHorariosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GestionMateriasHorariosDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GestionMateriasHorariosDialog dialog = new GestionMateriasHorariosDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
