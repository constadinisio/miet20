package main.java.views.users.Profesor;

import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import java.sql.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;

/**
 *
 * @author nico_
 */
public class NotasProfesorPanel extends javax.swing.JPanel {

    /**
     * Creates new form NotasProfesorPanel
     */
    private DefaultTableModel tableModel;
    private Connection conect;
    private int profesorId;
    private int cursoId;
    private int materiaId;
    private List<String> trabajos = new ArrayList<>();

    public NotasProfesorPanel(int profesorId, int cursoId, int materiaId) {
    initComponents();

    this.profesorId = profesorId;
    this.cursoId = cursoId;
    this.materiaId = materiaId;
    this.conect = (Connection) Conexion.getInstancia().verificarConexion();

    // En su lugar, configura cada panel individual
    jPanel2.setVisible(true);
    jPanel3.setVisible(true);
    jPanel4.setVisible(true);

    // Configurar la tabla para que se ajuste
    tablaNotas.setFillsViewportHeight(true);
    tablaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    // Configurar modelo de tabla
    tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int col) {
            return col > 1 && col < getColumnCount() - 1;
        }
    };

    // Listener para actualizar promedios
    tableModel.addTableModelListener(e -> {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column > 1 && column < tableModel.getColumnCount() - 1) {
                actualizarPromedio(row);
            }
        }
    });

    // Configurar tabla
    actualizarColumnas();
    tablaNotas.setModel(tableModel);

    // Cargar datos
    cargarTrabajos();
    cargarNotas();
}



    private void actualizarColumnas() {
        tableModel.setColumnCount(0);
        tableModel.addColumn("Alumno");
        tableModel.addColumn("DNI");
        for (String trabajo : trabajos) {
            tableModel.addColumn(trabajo);
        }
        tableModel.addColumn("Promedio");

        // Configurar el editor personalizado para las columnas de notas
        for (int i = 2; i < tableModel.getColumnCount() - 1; i++) {
            TableColumn column = tablaNotas.getColumnModel().getColumn(i);
            column.setCellEditor(new NotaCellEditor());
        }
    }

    private void agregarTrabajo() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del trabajo:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                String query = "INSERT INTO trabajos (materia_id, nombre) VALUES (?, ?)";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, materiaId);
                ps.setString(2, nombre);
                ps.executeUpdate();

                trabajos.add(nombre);
                actualizarColumnas();
                cargarNotas();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar trabajo: " + ex.getMessage());
            }
        }
    }

    private void cargarTrabajos() {
        try {
            String query = "SELECT nombre FROM trabajos WHERE materia_id = ? ORDER BY id";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ResultSet rs = ps.executeQuery();

            trabajos.clear();
            while (rs.next()) {
                trabajos.add(rs.getString("nombre"));
            }
            actualizarColumnas();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar trabajos: " + ex.getMessage());
        }
    }

    private void cargarNotas() {
        try {
            tableModel.setRowCount(0);

            String query = "SELECT u.apellido, u.nombre, u.dni FROM usuarios u "
                    + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? ORDER BY u.apellido, u.nombre";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[trabajos.size() + 3];
                row[0] = rs.getString("apellido") + ", " + rs.getString("nombre");
                row[1] = rs.getString("dni");
                for (int i = 2; i < row.length; i++) {
                    row[i] = 0.0;
                }
                tableModel.addRow(row);
            }
            cargarNotasExistentes();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar alumnos: " + ex.getMessage());
        }
    }

    private void cargarNotasExistentes() {
        try {
            String query = "SELECT alumno_id, trabajo_id, nota FROM notas "
                    + "WHERE materia_id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, materiaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String alumnoId = rs.getString("alumno_id");
                int trabajoId = rs.getInt("trabajo_id");
                double nota = rs.getDouble("nota");

                // Encontrar fila del alumno y columna del trabajo
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1).equals(alumnoId)) {
                        tableModel.setValueAt(nota, i, trabajoId + 1);
                        break;
                    }
                }
            }
            actualizarPromedios();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar notas: " + ex.getMessage());
        }
    }

    private void actualizarPromedio(int row) {
        double suma = 0;
        int cantNotas = 0;

        for (int j = 2; j < tableModel.getColumnCount() - 1; j++) {
            Object valor = tableModel.getValueAt(row, j);
            if (valor != null && !valor.toString().equals("NC")) {
                try {
                    suma += Double.parseDouble(valor.toString());
                    cantNotas++;
                } catch (NumberFormatException e) {
                    // Ignorar valores no numéricos
                }
            }
        }

        double promedio = cantNotas > 0 ? suma / cantNotas : 0;
        tableModel.setValueAt(Math.round(promedio * 100.0) / 100.0, row, tableModel.getColumnCount() - 1);
    }

    private void actualizarPromedios() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            actualizarPromedio(i);
        }
    }

    private void guardarNotas() {
        try {
            conect.setAutoCommit(false);

            // Primero aseguramos que existan los trabajos en la BD
            for (int j = 0; j < trabajos.size(); j++) {
                String nombreTrabajo = trabajos.get(j);

                // Verificar si el trabajo ya existe
                String queryVerificar = "SELECT id FROM trabajos WHERE materia_id = ? AND nombre = ?";
                PreparedStatement psVerificar = conect.prepareStatement(queryVerificar);
                psVerificar.setInt(1, materiaId);
                psVerificar.setString(2, nombreTrabajo);
                ResultSet rs = psVerificar.executeQuery();

                if (!rs.next()) {
                    // Si no existe, insertarlo
                    String queryInsertarTrabajo = "INSERT INTO trabajos (materia_id, nombre) VALUES (?, ?)";
                    PreparedStatement psInsertarTrabajo = conect.prepareStatement(queryInsertarTrabajo);
                    psInsertarTrabajo.setInt(1, materiaId);
                    psInsertarTrabajo.setString(2, nombreTrabajo);
                    psInsertarTrabajo.executeUpdate();
                }
            }

            // Ahora obtener los IDs de todos los trabajos
            Map<String, Integer> trabajoIds = new HashMap<>();
            String queryTrabajos = "SELECT id, nombre FROM trabajos WHERE materia_id = ?";
            PreparedStatement psTrabajos = conect.prepareStatement(queryTrabajos);
            psTrabajos.setInt(1, materiaId);
            ResultSet rsTrabajos = psTrabajos.executeQuery();

            while (rsTrabajos.next()) {
                trabajoIds.put(rsTrabajos.getString("nombre"), rsTrabajos.getInt("id"));
            }

            // Eliminar notas existentes
            String deleteQuery = "DELETE FROM notas WHERE materia_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setInt(1, materiaId);
            deletePs.executeUpdate();

            // Insertar nuevas notas
            String insertQuery = "INSERT INTO notas (alumno_id, materia_id, trabajo_id, nota) VALUES (?, ?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String alumnoId = tableModel.getValueAt(i, 1).toString();

                for (int j = 0; j < trabajos.size(); j++) {
                    String nombreTrabajo = trabajos.get(j);
                    Integer trabajoId = trabajoIds.get(nombreTrabajo);

                    if (trabajoId == null) {
                        throw new SQLException("No se encontró el ID para el trabajo: " + nombreTrabajo);
                    }

                    Object valorNota = tableModel.getValueAt(i, j + 2);
                    double nota;

                    if (valorNota.toString().equals("NC")) {
                        nota = 0;
                    } else {
                        nota = Double.parseDouble(valorNota.toString());
                    }

                    insertPs.setString(1, alumnoId);
                    insertPs.setInt(2, materiaId);
                    insertPs.setInt(3, trabajoId);
                    insertPs.setDouble(4, nota);
                    insertPs.executeUpdate();
                }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaNotas = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnExportar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnAgregarTrabajo = new javax.swing.JButton();

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
        jScrollPane1.setViewportView(tablaNotas);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
        );

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnExportar.setText("Exportar");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGuardar)
                .addGap(332, 332, 332)
                .addComponent(btnExportar)
                .addGap(98, 98, 98))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExportar)
                    .addComponent(btnGuardar))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        btnAgregarTrabajo.setText("Agregar Trabajo");
        btnAgregarTrabajo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarTrabajoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAgregarTrabajo)
                .addGap(71, 71, 71))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addComponent(btnAgregarTrabajo)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarNotas();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnAgregarTrabajoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTrabajoActionPerformed
        agregarTrabajo();
    }//GEN-LAST:event_btnAgregarTrabajoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarTrabajo;
    private javax.swing.JButton btnExportar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tablaNotas;
    // End of variables declaration//GEN-END:variables
}
