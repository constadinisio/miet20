/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package main.java.views.users.Admin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.*;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import main.java.database.Conexion;

/**
 *
 * @author nico_
 */
public class GestionCursosPanel extends javax.swing.JPanel {

    // Conexión a la base de datos
    private Connection conect;

    // Modelo de tabla para mostrar cursos
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    /**
     * Creates new form GestionCursosPanel
     */
    public GestionCursosPanel() {
        initComponents();
        inicializarTabla();
        
        tablaCursos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
tablaCursos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            actualizarBotonesPorSeleccion();
        }
    }
});
        
        inicializarComboBoxes();
        probar_conexion();
        cargarCursos();

        // Añadir ActionListeners para los filtros
        comboboxEstado.addActionListener(e -> cargarCursos());
        comboBoxAnio.addActionListener(e -> cargarCursos());

        // Añadir ActionListeners para los botones
        btnNuevoCurso.addActionListener(e -> nuevoCurso());
        btnAsignarAlumnos.addActionListener(e -> asignarAlumnos());
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnEditar.addActionListener(e -> editarCurso());
        btnVerAlumnos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerAlumnosActionPerformed(evt);
            }
        });
    }
    
    // Añade este método para activar/desactivar botones según haya selección
private void actualizarBotonesPorSeleccion() {
    boolean haySeleccion = tablaCursos.getSelectedRow() != -1;
    btnAsignarAlumnos.setEnabled(haySeleccion);
    btnCambiarEstado.setEnabled(haySeleccion);
    btnEditar.setEnabled(haySeleccion);
    btnVerAlumnos.setEnabled(haySeleccion);
}

    /**
     * Verifica la conexión a la base de datos.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa la tabla de cursos con sus columnas.
     */
    // Modifica el método inicializarTabla() para incluir el sorter
private void inicializarTabla() {
    tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Hacer la tabla no editable
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // Para que las columnas se ordenen correctamente según su tipo de dato
            if (columnIndex == 0) return Integer.class; // ID
            if (columnIndex == 1) return String.class;  // Año
            if (columnIndex == 2) return String.class;  // División
            if (columnIndex == 3) return String.class;  // Turno
            if (columnIndex == 4) return String.class;  // Estado
            if (columnIndex == 5) return Integer.class; // Cantidad Alumnos
            return Object.class;
        }
    };

    tableModel.addColumn("ID");
    tableModel.addColumn("Año");
    tableModel.addColumn("División");
    tableModel.addColumn("Turno");
    tableModel.addColumn("Estado");
    tableModel.addColumn("Cantidad Alumnos");
    tablaCursos.setModel(tableModel);
    
    // Crear y configurar el sorter
    sorter = new TableRowSorter<>(tableModel);
    tablaCursos.setRowSorter(sorter);
    
    // Configurar los comparadores para cada columna si es necesario
    sorter.setComparator(1, new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            // Comparador para la columna de año (quita el símbolo "°" antes de comparar)
            int año1 = Integer.parseInt(s1.replace("°", ""));
            int año2 = Integer.parseInt(s2.replace("°", ""));
            return Integer.compare(año1, año2);
        }
    });

    // Ocultar la columna ID
    tablaCursos.getColumnModel().getColumn(0).setMinWidth(0);
    tablaCursos.getColumnModel().getColumn(0).setMaxWidth(0);
    tablaCursos.getColumnModel().getColumn(0).setWidth(0);
    
    // Añadir listener para cambiar el cursor al pasar sobre encabezados
    tablaCursos.getTableHeader().addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
        @Override
        public void mouseMoved(java.awt.event.MouseEvent evt) {
            tablaCursos.getTableHeader().setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
}

    /**
     * Inicializa los ComboBox con sus opciones.
     */
    private void inicializarComboBoxes() {
        // Inicializar filtro de estado
        comboboxEstado.removeAllItems();
        comboboxEstado.addItem("Todos");
        comboboxEstado.addItem("Activo");
        comboboxEstado.addItem("Inactivo");

        // Inicializar filtro de año
        comboBoxAnio.removeAllItems();
        comboBoxAnio.addItem("Todos");
        for (int i = 1; i <= 6; i++) {
            comboBoxAnio.addItem(i + "°");
        }
    }

    /**
     * Carga los cursos desde la base de datos aplicando los filtros
     * seleccionados.
     */
    private void cargarCursos() {
        try {
            if (conect == null) {
                probar_conexion();
                if (conect == null) {
                    return;
                }
            }

            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT c.id, c.anio, c.division, c.turno, c.estado, "
                    + "(SELECT COUNT(*) FROM alumno_curso ac WHERE ac.curso_id = c.id AND ac.estado = 'activo') AS cantidad_alumnos "
                    + "FROM cursos c WHERE 1=1");

            // Aplicar filtro de estado
            if (comboboxEstado.getSelectedIndex() > 0) {
                String estadoFiltro = comboboxEstado.getSelectedItem().toString().toLowerCase();
                queryBuilder.append(" AND c.estado = '").append(estadoFiltro).append("'");
            }

            // Aplicar filtro de año
            if (comboBoxAnio.getSelectedIndex() > 0) {
                String anioFiltro = comboBoxAnio.getSelectedItem().toString().replace("°", "");
                queryBuilder.append(" AND c.anio = ").append(anioFiltro);
            }

            queryBuilder.append(" ORDER BY c.anio, c.division");

            PreparedStatement ps = conect.prepareStatement(queryBuilder.toString());
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getInt("anio") + "°",
                    rs.getString("division"),
                    rs.getString("turno"),
                    rs.getString("estado"),
                    rs.getInt("cantidad_alumnos")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar cursos: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int obtenerIdCursoSeleccionado() {
    int filaSeleccionada = tablaCursos.getSelectedRow();
    if (filaSeleccionada == -1) {
        return -1;  // Ninguna fila seleccionada
    }
    
    // Convertir el índice de fila del view (la tabla con ordenamiento) al índice en el modelo
    int filaModelo = tablaCursos.convertRowIndexToModel(filaSeleccionada);
    return (int) tableModel.getValueAt(filaModelo, 0);
}

    /**
     * Abre un diálogo para crear un nuevo curso.
     */
    private void nuevoCurso() {
        JTextField txtAnio = new JTextField(5);
        JTextField txtDivision = new JTextField(5);
        JComboBox<String> comboTurno = new JComboBox<>(new String[]{"Mañana", "Tarde", "Noche"});

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Año:"));
        panel.add(txtAnio);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("División:"));
        panel.add(txtDivision);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Turno:"));
        panel.add(comboTurno);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuevo Curso",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int anio = Integer.parseInt(txtAnio.getText().trim());
                int division = Integer.parseInt(txtDivision.getText().trim());
                String turno = (String) comboTurno.getSelectedItem();

                // Verificar que los datos sean válidos
                if (anio <= 0 || division <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Año y división deben ser números positivos",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Verificar si ya existe un curso con esos datos
                String checkQuery = "SELECT id FROM cursos WHERE anio = ? AND division = ? AND turno = ?";
                PreparedStatement checkPs = conect.prepareStatement(checkQuery);
                checkPs.setInt(1, anio);
                checkPs.setInt(2, division);
                checkPs.setString(3, turno);
                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Ya existe un curso con esos datos",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Insertar el nuevo curso
                String insertQuery = "INSERT INTO cursos (anio, division, turno, estado) VALUES (?, ?, ?, 'activo')";
                PreparedStatement insertPs = conect.prepareStatement(insertQuery);
                insertPs.setInt(1, anio);
                insertPs.setInt(2, division);
                insertPs.setString(3, turno);
                insertPs.executeUpdate();

                JOptionPane.showMessageDialog(this, "Curso creado exitosamente");
                cargarCursos();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Año y división deben ser números",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al crear curso: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Abre el diálogo para editar un curso existente.
     */
    
        /**
 * Abre el diálogo para editar un curso existente.
 */
private void editarCurso() {
    int filaSeleccionada = tablaCursos.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
        return;
    }

    // Convertir el índice de la vista al índice del modelo
    int filaModelo = tablaCursos.convertRowIndexToModel(filaSeleccionada);

    int cursoId = (int) tableModel.getValueAt(filaModelo, 0);
    String anioActual = (String) tableModel.getValueAt(filaModelo, 1);
    String divisionActual = (String) tableModel.getValueAt(filaModelo, 2);
    String turnoActual = (String) tableModel.getValueAt(filaModelo, 3);

    // Eliminar el símbolo "°" del año
    anioActual = anioActual.replace("°", "");

    JTextField txtAnio = new JTextField(anioActual, 5);
    JTextField txtDivision = new JTextField(divisionActual, 5);
    JComboBox<String> comboTurno = new JComboBox<>(new String[]{"Mañana", "Tarde", "Noche"});
    comboTurno.setSelectedItem(turnoActual);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    panel.add(new JLabel("Año:"));
    panel.add(txtAnio);
    panel.add(Box.createVerticalStrut(10));

    panel.add(new JLabel("División:"));
    panel.add(txtDivision);
    panel.add(Box.createVerticalStrut(10));

    panel.add(new JLabel("Turno:"));
    panel.add(comboTurno);

    int result = JOptionPane.showConfirmDialog(this, panel, "Editar Curso",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());
            int division = Integer.parseInt(txtDivision.getText().trim());
            String turno = (String) comboTurno.getSelectedItem();

            // Verificar que los datos sean válidos
            if (anio <= 0 || division <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Año y división deben ser números positivos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar si ya existe otro curso con esos datos
            String checkQuery = "SELECT id FROM cursos WHERE anio = ? AND division = ? AND turno = ? AND id <> ?";
            PreparedStatement checkPs = conect.prepareStatement(checkQuery);
            checkPs.setInt(1, anio);
            checkPs.setInt(2, division);
            checkPs.setString(3, turno);
            checkPs.setInt(4, cursoId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe otro curso con esos datos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Actualizar el curso
            String updateQuery = "UPDATE cursos SET anio = ?, division = ?, turno = ? WHERE id = ?";
            PreparedStatement updatePs = conect.prepareStatement(updateQuery);
            updatePs.setInt(1, anio);
            updatePs.setInt(2, division);
            updatePs.setString(3, turno);
            updatePs.setInt(4, cursoId);
            updatePs.executeUpdate();

            JOptionPane.showMessageDialog(this, "Curso actualizado exitosamente");
            cargarCursos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Año y división deben ser números",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar curso: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

    /**
     * Cambia el estado de un curso entre activo e inactivo.
     */
    private void cambiarEstado() {
        int filaSeleccionada = tablaCursos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
            return;
        }

        int cursoId = (int) tableModel.getValueAt(filaSeleccionada, 0);
        String estadoActual = (String) tableModel.getValueAt(filaSeleccionada, 4);
        String nuevoEstado = estadoActual.equalsIgnoreCase("activo") ? "inactivo" : "activo";

        try {
            String query = "UPDATE cursos SET estado = ? WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, cursoId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Estado del curso actualizado exitosamente");
            cargarCursos();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar estado: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre el diálogo para asignar alumnos al curso seleccionado.
     */
    private void asignarAlumnos() {
        int filaSeleccionada = tablaCursos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
            return;
        }

        int cursoId = (int) tableModel.getValueAt(filaSeleccionada, 0);

        try {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            AsignarAlumnosDialog dialog = new AsignarAlumnosDialog(parentFrame, true);

            // Verificar si el método setCursoId existe en AsignarAlumnosDialog
            try {
                java.lang.reflect.Method method = AsignarAlumnosDialog.class.getMethod("setCursoId", int.class);
                method.invoke(dialog, cursoId);
            } catch (NoSuchMethodException e) {
                // Si el método no existe, necesitamos modificar AsignarAlumnosDialog
                JOptionPane.showMessageDialog(this,
                        "La clase AsignarAlumnosDialog necesita un método setCursoId(int)",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            dialog.setVisible(true);

            // Recargar la tabla después de cerrar el diálogo
            if (dialog.seCambiaronAsignaciones()) {
                cargarCursos();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el diálogo de asignación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnVerAlumnosActionPerformed(java.awt.event.ActionEvent evt) {
        int filaSeleccionada = tablaCursos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
            return;
        }

        int cursoId = (int) tableModel.getValueAt(filaSeleccionada, 0);
        mostrarAlumnosCurso(cursoId);
    }

    /**
     * Muestra un diálogo con los alumnos del curso seleccionado.
     *
     * @param cursoId ID del curso seleccionado
     */
    private void mostrarAlumnosCurso(int cursoId) {
        try {
            // Crear un modelo de tabla para los alumnos
            DefaultTableModel modeloAlumnos = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            modeloAlumnos.addColumn("ID");
            modeloAlumnos.addColumn("Nombre");
            modeloAlumnos.addColumn("Apellido");
            modeloAlumnos.addColumn("DNI");

            // Consultar los alumnos del curso
            String query
                    = "SELECT u.id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            // Obtener información del curso
            String infoCurso = "";
            String queryCurso = "SELECT anio, division FROM cursos WHERE id = ?";
            PreparedStatement psCurso = conect.prepareStatement(queryCurso);
            psCurso.setInt(1, cursoId);
            ResultSet rsCurso = psCurso.executeQuery();

            if (rsCurso.next()) {
                infoCurso = rsCurso.getInt("anio") + "° " + rsCurso.getString("division");
            }

            // Llenar el modelo con los datos
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("dni")
                };
                modeloAlumnos.addRow(row);
            }

            // Crear el diálogo personalizado
            JDialog dialogoAlumnos = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Alumnos del curso " + infoCurso, true);
            dialogoAlumnos.setLayout(new BorderLayout());

            // Crear tabla para mostrar alumnos
            JTable tablaAlumnosCurso = new JTable(modeloAlumnos);
            JScrollPane scrollPane = new JScrollPane(tablaAlumnosCurso);

            // Ocultar la columna ID
            tablaAlumnosCurso.getColumnModel().getColumn(0).setMinWidth(0);
            tablaAlumnosCurso.getColumnModel().getColumn(0).setMaxWidth(0);

            // Añadir panel de botones
            JPanel panelBotones = new JPanel();
            JButton btnEditarAlumno = new JButton("Editar Alumno Seleccionado");
            JButton btnCerrar = new JButton("Cerrar");

            btnEditarAlumno.addActionListener(e -> {
                int filaSeleccionadaAlumno = tablaAlumnosCurso.getSelectedRow();
                if (filaSeleccionadaAlumno != -1) {
                    int alumnoId = (int) modeloAlumnos.getValueAt(filaSeleccionadaAlumno, 0);
                    editarAlumno(alumnoId);
                } else {
                    JOptionPane.showMessageDialog(dialogoAlumnos, "Por favor, seleccione un alumno");
                }
            });

            btnCerrar.addActionListener(e -> dialogoAlumnos.dispose());

            panelBotones.add(btnEditarAlumno);
            panelBotones.add(btnCerrar);

            // Añadir componentes al diálogo
            dialogoAlumnos.add(new JLabel("  Alumnos del curso " + infoCurso + ":"), BorderLayout.NORTH);
            dialogoAlumnos.add(scrollPane, BorderLayout.CENTER);
            dialogoAlumnos.add(panelBotones, BorderLayout.SOUTH);

            // Configurar y mostrar el diálogo
            dialogoAlumnos.setSize(600, 400);
            dialogoAlumnos.setLocationRelativeTo(this);
            dialogoAlumnos.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar alumnos del curso: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra un diálogo para editar los datos de un alumno.
     *
     * @param alumnoId ID del alumno a editar
     */
    private void editarAlumno(int alumnoId) {
        try {
            // Obtener datos actuales del alumno
            String query = "SELECT nombre, apellido, dni, telefono, direccion, fecha_nacimiento FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Crear componentes para el formulario
                JTextField txtNombre = new JTextField(rs.getString("nombre"), 20);
                JTextField txtApellido = new JTextField(rs.getString("apellido"), 20);
                JTextField txtDNI = new JTextField(rs.getString("dni") != null ? rs.getString("dni") : "", 20);
                JTextField txtTelefono = new JTextField(rs.getString("telefono") != null ? rs.getString("telefono") : "", 20);
                JTextField txtDireccion = new JTextField(rs.getString("direccion") != null ? rs.getString("direccion") : "", 20);

                // Panel para el formulario
                JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
                panel.add(new JLabel("Nombre:"));
                panel.add(txtNombre);
                panel.add(new JLabel("Apellido:"));
                panel.add(txtApellido);
                panel.add(new JLabel("DNI:"));
                panel.add(txtDNI);
                panel.add(new JLabel("Teléfono:"));
                panel.add(txtTelefono);
                panel.add(new JLabel("Dirección:"));
                panel.add(txtDireccion);

                // Mostrar diálogo
                int result = JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Editar Alumno",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                // Guardar cambios si se presiona OK
                if (result == JOptionPane.OK_OPTION) {
                    // Validar campos obligatorios
                    if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Nombre y apellido son obligatorios");
                        return;
                    }

                    // Actualizar datos
                    String updateQuery = "UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, telefono = ?, direccion = ? WHERE id = ?";
                    PreparedStatement updatePs = conect.prepareStatement(updateQuery);
                    updatePs.setString(1, txtNombre.getText().trim());
                    updatePs.setString(2, txtApellido.getText().trim());
                    updatePs.setString(3, txtDNI.getText().trim());
                    updatePs.setString(4, txtTelefono.getText().trim());
                    updatePs.setString(5, txtDireccion.getText().trim());
                    updatePs.setInt(6, alumnoId);

                    updatePs.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Datos actualizados correctamente");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el alumno seleccionado");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al editar alumno: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelTitulo = new javax.swing.JPanel();
        lblGestionCursos = new javax.swing.JLabel();
        panelFiltros = new javax.swing.JPanel();
        lblFiltros = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        comboboxEstado = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        comboBoxAnio = new javax.swing.JComboBox<>();
        btnVerAlumnos = new javax.swing.JButton();
        panelBotones = new javax.swing.JPanel();
        btnNuevoCurso = new javax.swing.JButton();
        btnAsignarAlumnos = new javax.swing.JButton();
        btnCambiarEstado = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        scrollTablaCursos = new javax.swing.JScrollPane();
        tablaCursos = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(758, 713));

        lblGestionCursos.setText("Gestion de Cursos");

        javax.swing.GroupLayout panelTituloLayout = new javax.swing.GroupLayout(panelTitulo);
        panelTitulo.setLayout(panelTituloLayout);
        panelTituloLayout.setHorizontalGroup(
            panelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTituloLayout.createSequentialGroup()
                .addGap(320, 320, 320)
                .addComponent(lblGestionCursos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelTituloLayout.setVerticalGroup(
            panelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTituloLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblGestionCursos)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        lblFiltros.setText("Filtros: ");

        lblEstado.setText("Estado: ");

        comboboxEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Año: ");

        comboBoxAnio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnVerAlumnos.setText("Ver Alumnos");

        javax.swing.GroupLayout panelFiltrosLayout = new javax.swing.GroupLayout(panelFiltros);
        panelFiltros.setLayout(panelFiltrosLayout);
        panelFiltrosLayout.setHorizontalGroup(
            panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFiltros)
                .addGap(18, 18, 18)
                .addComponent(lblEstado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboboxEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(168, 168, 168)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(comboBoxAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73)
                .addComponent(btnVerAlumnos)
                .addContainerGap(93, Short.MAX_VALUE))
        );
        panelFiltrosLayout.setVerticalGroup(
            panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFiltros)
                    .addComponent(lblEstado)
                    .addComponent(comboboxEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(comboBoxAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVerAlumnos))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnNuevoCurso.setText("Nuevo Curso");

        btnAsignarAlumnos.setText("Asignar Alumnos");

        btnCambiarEstado.setText("Cambiar Estado");

        btnEditar.setText("Editar");

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnNuevoCurso)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAsignarAlumnos)
                .addGap(84, 84, 84)
                .addComponent(btnCambiarEstado)
                .addGap(102, 102, 102)
                .addComponent(btnEditar)
                .addGap(23, 23, 23))
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotonesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevoCurso)
                    .addComponent(btnAsignarAlumnos)
                    .addComponent(btnCambiarEstado)
                    .addComponent(btnEditar))
                .addContainerGap())
        );

        tablaCursos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollTablaCursos.setViewportView(tablaCursos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrollTablaCursos)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTablaCursos, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAsignarAlumnos;
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnNuevoCurso;
    private javax.swing.JButton btnVerAlumnos;
    private javax.swing.JComboBox<String> comboBoxAnio;
    private javax.swing.JComboBox<String> comboboxEstado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFiltros;
    private javax.swing.JLabel lblGestionCursos;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelTitulo;
    private javax.swing.JScrollPane scrollTablaCursos;
    private javax.swing.JTable tablaCursos;
    // End of variables declaration//GEN-END:variables
}
