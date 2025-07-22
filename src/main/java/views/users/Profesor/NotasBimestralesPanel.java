package main.java.views.users.Profesor;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import main.java.database.Conexion;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nico_
 */
public class NotasBimestralesPanel extends javax.swing.JPanel {

    private Connection conect;
    private int profesorId;
    private int cursoId;
    private int materiaId;
    private DefaultTableModel tableModel;

    public NotasBimestralesPanel(int profesorId, int cursoId, int materiaId) {
        this.profesorId = profesorId;
        this.cursoId = cursoId;
        this.materiaId = materiaId;
        this.conect = (Connection) Conexion.getInstancia().verificarConexion();


        setLayout(new BorderLayout(5, 5));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblPeriodo = new JLabel("Período:");

        // Agregar todos los períodos de evaluación
        String[] periodos = {
            "1er Bimestre",
            "2do Bimestre",
            "1er Cuatrimestre",
            "3er Bimestre",
            "4to Bimestre",
            "2do Cuatrimestre",
            "Recuperatorio Dic",
            "Recuperatorio Feb/Mar"
        };

        JComboBox<String> comboPeriodo = new JComboBox<>(periodos);
        headerPanel.add(lblPeriodo);
        headerPanel.add(comboPeriodo);
        
        // Separador visual
        headerPanel.add(new JLabel("   |   "));
        
        // Controles para asignar calificación a todos
        JLabel lblAsignarTodos = new JLabel("Asignar a todos:");
        JComboBox<String> comboAsignarTodos = new JComboBox<>(new String[]{"Seleccionar..."});
        JButton btnAsignarTodos = new JButton("Aplicar");
        
        headerPanel.add(lblAsignarTodos);
        headerPanel.add(comboAsignarTodos);
        headerPanel.add(btnAsignarTodos);

        // Tabla con columnas más descriptivas
        tableModel = new DefaultTableModel(
                new String[]{
                    "Alumno",
                    "DNI",
                    "Prom. Actividades",
                    "Calificación"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3; // Solo la calificación es editable
            }
        };

        tablaNotas = new JTable(tableModel);

        // Configurar renderer personalizado para los encabezados
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(220, 220, 220));
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setFont(new Font("Arial", Font.BOLD, 12));

        for (int i = 0; i < tablaNotas.getColumnCount(); i++) {
            tablaNotas.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Configurar ancho de columnas
        tablaNotas.getColumnModel().getColumn(0).setPreferredWidth(200); // Alumno
        tablaNotas.getColumnModel().getColumn(1).setPreferredWidth(80);  // DNI
        tablaNotas.getColumnModel().getColumn(2).setPreferredWidth(120); // Promedio
        tablaNotas.getColumnModel().getColumn(3).setPreferredWidth(100); // Calificación

        // Configurar editor para la columna de calificación
        JComboBox<String> comboCalificaciones = new JComboBox<>(
                new String[]{"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Ausente"}
        );

        tablaNotas.getColumnModel().getColumn(3).setCellEditor(
                new DefaultCellEditor(comboCalificaciones)
        );

        // Configurar renderer para colorear según nota
        tablaNotas.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (value == null || value.toString().isEmpty() || value.toString().equals("Ausente")) {
                        c.setBackground(Color.LIGHT_GRAY);
                    } else {
                        try {
                            int nota = Integer.parseInt(value.toString());
                            if (nota < 6) {
                                c.setBackground(new Color(255, 182, 193)); // Rojo claro
                            } else if (nota < 8) {
                                c.setBackground(new Color(255, 255, 153)); // Amarillo claro
                            } else {
                                c.setBackground(new Color(144, 238, 144)); // Verde claro
                            }
                        } catch (NumberFormatException e) {
                            c.setBackground(Color.WHITE);
                        }
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaNotas);
        scrollPane.setPreferredSize(new Dimension(700, 400));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardar = new JButton("Guardar Notas");
        buttonPanel.add(btnGuardar);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event Listeners
        comboPeriodo.addActionListener(e -> {
            String periodoSeleccionado = (String) comboPeriodo.getSelectedItem();
            
            // Configurar el editor según el período seleccionado
            configurarEditorCalificaciones(periodoSeleccionado);
            
            // Actualizar opciones del combo "Asignar a todos"
            actualizarComboAsignarTodos(comboAsignarTodos, periodoSeleccionado);
            
            if (periodoSeleccionado.contains("Cuatrimestre")) {
                cargarPromedioCuatrimestral(periodoSeleccionado);
            } else {
                cargarNotasPeriodo(periodoSeleccionado);
            }
        });
        
        btnAsignarTodos.addActionListener(e -> {
            String calificacionSeleccionada = (String) comboAsignarTodos.getSelectedItem();
            if (calificacionSeleccionada != null && !calificacionSeleccionada.equals("Seleccionar...")) {
                asignarCalificacionATodos(calificacionSeleccionada);
            }
        });
        
        btnGuardar.addActionListener(e -> guardarNotas((String) comboPeriodo.getSelectedItem()));

        cargarAlumnos();
        
        // Configurar el editor inicial con el primer período
        configurarEditorCalificaciones("1er Bimestre");
        
        // Configurar combo "Asignar a todos" inicial
        actualizarComboAsignarTodos(comboAsignarTodos, "1er Bimestre");
    }
    
    /**
     * Configura el editor de la columna de calificaciones según el período seleccionado.
     * Para 1° y 3° bimestre usa valoraciones conceptuales, para el resto usa números.
     */
    private void configurarEditorCalificaciones(String periodo) {
        JComboBox<String> comboCalificaciones;
        
        if (ConvertidorNotas.esPeridodoConceptual(periodo)) {
            // Para períodos conceptuales (1° y 3° bimestre)
            String[] valoraciones = new String[ConvertidorNotas.getValoracionesConceptuales().length + 2];
            valoraciones[0] = "";  // Opción vacía
            System.arraycopy(ConvertidorNotas.getValoracionesConceptuales(), 0, valoraciones, 1, ConvertidorNotas.getValoracionesConceptuales().length);
            valoraciones[valoraciones.length - 1] = "Ausente";
            
            comboCalificaciones = new JComboBox<>(valoraciones);
        } else {
            // Para períodos numéricos (cuatrimestres y otros bimestres)
            comboCalificaciones = new JComboBox<>(
                new String[]{"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Ausente"}
            );
        }
        
        tablaNotas.getColumnModel().getColumn(3).setCellEditor(
            new DefaultCellEditor(comboCalificaciones)
        );
    }

    private void cargarAlumnos() {
        try {
            tableModel.setRowCount(0);

            // Forzar la misma colación en la consulta
            String query = "SELECT u.apellido, u.nombre, u.dni FROM usuarios u "
                    + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' "
                    + "ORDER BY u.apellido COLLATE utf8mb4_general_ci, u.nombre COLLATE utf8mb4_general_ci";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String alumno = rs.getString("apellido") + ", " + rs.getString("nombre");
                String dni = rs.getString("dni");
                double promedioActividades = calcularPromedioActividades(dni);

                tableModel.addRow(new Object[]{
                    alumno, dni, promedioActividades, 0.0
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar alumnos: " + ex.getMessage());
            ex.printStackTrace(); // Para ver el detalle del error
        }
    }

    private double calcularPromedioActividades(String dni) {
        try {
            // Forzar la misma colación en la consulta
            String query = "SELECT AVG(nota) as promedio FROM notas "
                    + "WHERE alumno_id COLLATE utf8mb4_general_ci = ? AND materia_id = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, dni);
            ps.setInt(2, materiaId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Math.round(rs.getDouble("promedio") * 100.0) / 100.0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    private void cargarNotasPeriodo(String periodo) {
        try {
            // Consulta diferente según el tipo de período
            String query;
            if (ConvertidorNotas.esPeridodoConceptual(periodo)) {
                // Para períodos conceptuales, cargar la nota_conceptual
                query = "SELECT alumno_id, nota_conceptual, estado FROM notas_bimestrales "
                        + "WHERE materia_id = ? AND periodo COLLATE utf8mb4_general_ci = ?";
            } else {
                // Para períodos numéricos, cargar la nota numérica
                query = "SELECT alumno_id, nota, estado FROM notas_bimestrales "
                        + "WHERE materia_id = ? AND periodo COLLATE utf8mb4_general_ci = ?";
            }

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, periodo);

            ResultSet rs = ps.executeQuery();
            Map<String, Object> notasPorAlumno = new HashMap<>();

            while (rs.next()) {
                String estado = rs.getString("estado");
                String dni = rs.getString("alumno_id");
                
                if (estado != null && estado.equals("Ausente")) {
                    notasPorAlumno.put(dni, "Ausente");
                } else {
                    if (ConvertidorNotas.esPeridodoConceptual(periodo)) {
                        // Cargar valoración conceptual directamente
                        String notaConceptual = rs.getString("nota_conceptual");
                        notasPorAlumno.put(dni, notaConceptual != null ? notaConceptual : "");
                    } else {
                        // Cargar nota numérica
                        double notaNumerica = rs.getDouble("nota");
                        notasPorAlumno.put(dni, notaNumerica);
                    }
                }
            }

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String dni = (String) tableModel.getValueAt(i, 1);
                Object nota = notasPorAlumno.get(dni);
                if (nota == null) {
                    tableModel.setValueAt("", i, 3);
                } else {
                    tableModel.setValueAt(nota, i, 3);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar notas: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void guardarNotas(String periodo) {
        try {
            conect.setAutoCommit(false);

            // Eliminar notas existentes del período
            String deleteQuery = "DELETE FROM notas_bimestrales "
                    + "WHERE materia_id = ? AND periodo COLLATE utf8mb4_general_ci = ?";

            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setInt(1, materiaId);
            deletePs.setString(2, periodo);
            deletePs.executeUpdate();

            // Insertar nuevas notas - usar siempre la misma estructura
            String insertQuery = "INSERT INTO notas_bimestrales "
                    + "(alumno_id, materia_id, periodo, nota, promedio_actividades, estado, nota_conceptual) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement insertPs = conect.prepareStatement(insertQuery);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String dni = (String) tableModel.getValueAt(i, 1);
                double promedioAct = Double.parseDouble(tableModel.getValueAt(i, 2).toString());
                Object notaObj = tableModel.getValueAt(i, 3);

                if (notaObj == null || notaObj.toString().isEmpty()) {
                    continue; // No guardar filas sin calificación
                }

                String notaStr = notaObj.toString();

                insertPs.setString(1, dni);        // alumno_id
                insertPs.setInt(2, materiaId);     // materia_id
                insertPs.setString(3, periodo);    // periodo

                if (notaStr.equals("Ausente")) {
                    insertPs.setDouble(4, 0.0);           // nota (siempre 0 para ausentes)
                    insertPs.setDouble(5, promedioAct);   // promedio_actividades
                    insertPs.setString(6, "Ausente");     // estado
                    insertPs.setString(7, "Ausente");     // nota_conceptual
                } else {
                    if (ConvertidorNotas.esPeridodoConceptual(periodo)) {
                        // Período conceptual: nota=0, nota_conceptual=valoración textual
                        insertPs.setDouble(4, 0.0);           // nota (0 para conceptuales)
                        insertPs.setDouble(5, promedioAct);   // promedio_actividades
                        insertPs.setString(6, "Normal");      // estado
                        insertPs.setString(7, notaStr);       // nota_conceptual (Avanzado/Suficiente/En Proceso)
                    } else {
                        // Período numérico: nota=valor, nota_conceptual=NULL
                        double nota = Double.parseDouble(notaStr);
                        insertPs.setDouble(4, nota);          // nota (valor numérico)
                        insertPs.setDouble(5, promedioAct);   // promedio_actividades
                        insertPs.setString(6, "Normal");      // estado
                        insertPs.setNull(7, java.sql.Types.VARCHAR); // nota_conceptual (NULL para numéricos)
                    }
                }

                insertPs.executeUpdate();
            }

            conect.commit();
            JOptionPane.showMessageDialog(this, "Notas guardadas exitosamente");
        } catch (SQLException ex) {
            try {
                conect.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error al guardar notas: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                conect.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void cargarPromedioCuatrimestral(String periodo) {
        String primerBimestre, segundoBimestre;

        if (periodo.equals("1er Cuatrimestre")) {
            primerBimestre = "1er Bimestre";
            segundoBimestre = "2do Bimestre";
        } else if (periodo.equals("2do Cuatrimestre")) {
            primerBimestre = "3er Bimestre";
            segundoBimestre = "4to Bimestre";
        } else {
            // No es un cuatrimestre, cargar normal
            cargarNotasPeriodo(periodo);
            return;
        }

        try {
            // Cargar notas del primer bimestre
            String query = "SELECT alumno_id, nota, estado FROM notas_bimestrales "
                    + "WHERE materia_id = ? AND periodo COLLATE utf8mb4_general_ci = ?";

            Map<String, Double> notasPrimerBimestre = new HashMap<>();
            Map<String, Double> notasSegundoBimestre = new HashMap<>();
            Map<String, Boolean> ausentePrimerBimestre = new HashMap<>();
            Map<String, Boolean> ausenteSegundoBimestre = new HashMap<>();

            // Cargar primer bimestre
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, primerBimestre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String dni = rs.getString("alumno_id");
                String estado = rs.getString("estado");
                if (estado != null && estado.equals("Ausente")) {
                    ausentePrimerBimestre.put(dni, true);
                    notasPrimerBimestre.put(dni, 0.0);
                } else {
                    ausentePrimerBimestre.put(dni, false);
                    notasPrimerBimestre.put(dni, rs.getDouble("nota"));
                }
            }

            // Cargar segundo bimestre
            ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ps.setString(2, segundoBimestre);
            rs = ps.executeQuery();

            while (rs.next()) {
                String dni = rs.getString("alumno_id");
                String estado = rs.getString("estado");
                if (estado != null && estado.equals("Ausente")) {
                    ausenteSegundoBimestre.put(dni, true);
                    notasSegundoBimestre.put(dni, 0.0);
                } else {
                    ausenteSegundoBimestre.put(dni, false);
                    notasSegundoBimestre.put(dni, rs.getDouble("nota"));
                }
            }

            // Calcular promedios y mostrarlos
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String dni = (String) tableModel.getValueAt(i, 1);
                Double nota1 = notasPrimerBimestre.get(dni);
                Double nota2 = notasSegundoBimestre.get(dni);
                Boolean ausente1 = ausentePrimerBimestre.get(dni);
                Boolean ausente2 = ausenteSegundoBimestre.get(dni);

                if (nota1 == null || nota2 == null) {
                    tableModel.setValueAt("", i, 3);
                    continue;
                }

                // Si ambos son ausente, el resultado es ausente
                if ((ausente1 != null && ausente1) && (ausente2 != null && ausente2)) {
                    tableModel.setValueAt("Ausente", i, 3);
                    continue;
                }

                // Si alguno es ausente, considerar la otra nota
                if (ausente1 != null && ausente1) {
                    tableModel.setValueAt(Math.round(nota2), i, 3);
                    continue;
                }

                if (ausente2 != null && ausente2) {
                    tableModel.setValueAt(Math.round(nota1), i, 3);
                    continue;
                }

                // Calcular promedio si ambos tienen nota
                if (nota1 > 0 && nota2 > 0) {
                    double promedio = (nota1 + nota2) / 2.0;
                    tableModel.setValueAt(Math.round(promedio), i, 3);
                } else {
                    tableModel.setValueAt("", i, 3);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar notas: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Actualiza las opciones del combo "Asignar a todos" según el período seleccionado.
     */
    private void actualizarComboAsignarTodos(JComboBox<String> comboAsignarTodos, String periodo) {
        comboAsignarTodos.removeAllItems();
        comboAsignarTodos.addItem("Seleccionar...");
        
        if (ConvertidorNotas.esPeridodoConceptual(periodo)) {
            // Para períodos conceptuales
            String[] valoraciones = ConvertidorNotas.getValoracionesConceptuales();
            for (String valoracion : valoraciones) {
                comboAsignarTodos.addItem(valoracion);
            }
            comboAsignarTodos.addItem("Ausente");
        } else {
            // Para períodos numéricos
            String[] notas = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Ausente"};
            for (String nota : notas) {
                comboAsignarTodos.addItem(nota);
            }
        }
    }
    
    /**
     * Asigna la misma calificación a todos los alumnos en la tabla.
     */
    private void asignarCalificacionATodos(String calificacion) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(calificacion, i, 3); // Columna de calificación
        }
        
        // Refrescar la tabla para mostrar los cambios
        tablaNotas.repaint();
        
        // Mostrar mensaje informativo
        JOptionPane.showMessageDialog(this, 
            "Se asignó '" + calificacion + "' a todos los alumnos.\n" +
            "Puede modificar individualmente antes de guardar.",
            "Calificación Asignada", 
            JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblPeriodo = new javax.swing.JLabel();
        comboPeriodo = new javax.swing.JComboBox<>();
        tablaNotas = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();

        lblPeriodo.setText("Periodo: ");

        comboPeriodo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tablaNotas.setModel(new javax.swing.table.DefaultTableModel(
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(lblPeriodo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(comboPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(tablaNotas, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPeriodo)
                    .addComponent(comboPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tablaNotas, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnGuardar.setText("Guardar Notas");

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGuardar)
                .addGap(41, 41, 41))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(btnGuardar)
                .addGap(0, 19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JComboBox<String> comboPeriodo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblPeriodo;
    private javax.swing.JTable tablaNotas;
    // End of variables declaration//GEN-END:variables
}
