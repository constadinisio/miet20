/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package users.Admin;
import java.awt.Frame;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import login.Conexion;
/**
 *
 * @author nico_
 */
public class GestionCursosPanel extends javax.swing.JPanel {
private Connection conect;
private DefaultTableModel tableModel;

// Constructor actualizado
public GestionCursosPanel() {
    initComponents();
    inicializarTabla();
    inicializarCombos();
    probar_conexion();
    cargarCursos();
}

private void inicializarTabla() {
    tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tableModel.addColumn("ID");
    tableModel.addColumn("Año");
    tableModel.addColumn("División");
    tableModel.addColumn("Alumnos");
    tableModel.addColumn("Profesores");
    tableModel.addColumn("Estado");
    tablaCursos.setModel(tableModel);
}

private void probar_conexion() {
    conect = Conexion.getInstancia().getConexion();
    if (conect == null) {
        JOptionPane.showMessageDialog(this, "Error de conexión.");
    }
}

private void inicializarCombos() {
    // Inicializar combo de años
    comboFiltroAnio.removeAllItems();
    comboFiltroAnio.addItem("Todos");
    for(int i = 1; i <= 6; i++) {
        comboFiltroAnio.addItem(String.valueOf(i));
    }
    
    // Inicializar combo de divisiones
    comboFiltroDivision.removeAllItems();
    comboFiltroDivision.addItem("Todas");
    for(int i = 1; i <= 5; i++) {
        comboFiltroDivision.addItem(String.valueOf(i));
    }
    
    // Inicializar combo de estado
    comboEstadoCurso.removeAllItems();
    comboEstadoCurso.addItem("Activo");
    comboEstadoCurso.addItem("Inactivo");
}

private void cargarCursos() {
    try {
        String filtroAnio = comboFiltroAnio.getSelectedItem().toString();
        String filtroDivision = comboFiltroDivision.getSelectedItem().toString();
        
        StringBuilder query = new StringBuilder(
            "SELECT c.id, c.anio, c.division, " +
            "(SELECT COUNT(*) FROM alumno_curso ac WHERE ac.curso_id = c.id AND ac.estado = 'activo') as cant_alumnos, " +
            "(SELECT COUNT(*) FROM profesor_curso pc WHERE pc.curso_id = c.id AND pc.estado = 'activo') as cant_profesores, " +
            "c.estado FROM cursos c WHERE 1=1"
        );
        
        if (!"Todos".equals(filtroAnio)) {
            query.append(" AND c.anio = ").append(filtroAnio);
        }
        if (!"Todas".equals(filtroDivision)) {
            query.append(" AND c.division = ").append(filtroDivision);
        }
        
        PreparedStatement ps = conect.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        
        tableModel.setRowCount(0);
        
        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getInt("anio"),
                rs.getInt("division"),
                rs.getInt("cant_alumnos"),
                rs.getInt("cant_profesores"),
                rs.getString("estado")
            };
            tableModel.addRow(row);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error al cargar cursos: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void crearCurso() {
    try {
        int anio = Integer.parseInt(txtAnio.getText().trim());
        int division = Integer.parseInt(txtDivision.getText().trim());
        
        if (anio < 1 || anio > 6 || division < 1 || division > 5) {
            JOptionPane.showMessageDialog(this, 
                "Año debe estar entre 1 y 6, División entre 1 y 5",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar si ya existe el curso
        String checkQuery = "SELECT id FROM cursos WHERE anio = ? AND division = ?";
        PreparedStatement checkPs = conect.prepareStatement(checkQuery);
        checkPs.setInt(1, anio);
        checkPs.setInt(2, division);
        
        if (checkPs.executeQuery().next()) {
            JOptionPane.showMessageDialog(this, 
                "Ya existe un curso con ese año y división",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Crear el nuevo curso
        String insertQuery = "INSERT INTO cursos (anio, division, estado) VALUES (?, ?, 'activo')";
        PreparedStatement insertPs = conect.prepareStatement(insertQuery);
        insertPs.setInt(1, anio);
        insertPs.setInt(2, division);
        insertPs.executeUpdate();
        
        JOptionPane.showMessageDialog(this, "Curso creado exitosamente");
        txtAnio.setText("");
        txtDivision.setText("");
        cargarCursos();
        
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, 
            "Por favor ingrese números válidos",
            "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error al crear curso: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void eliminarCurso() {
    int filaSeleccionada = tablaCursos.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
        return;
    }
    
    int confirmacion = JOptionPane.showConfirmDialog(this,
        "¿Está seguro que desea eliminar este curso?\n" +
        "Esta acción también eliminará todas las asignaciones de alumnos y profesores.",
        "Confirmar Eliminación",
        JOptionPane.YES_NO_OPTION);
        
    if (confirmacion == JOptionPane.YES_OPTION) {
        try {
            int cursoId = (int) tablaCursos.getValueAt(filaSeleccionada, 0);
            
            // Eliminar asignaciones
            String deleteAssignments = 
                "DELETE FROM alumno_curso WHERE curso_id = ?;" +
                "DELETE FROM profesor_curso WHERE curso_id = ?;";
            PreparedStatement psAssignments = conect.prepareStatement(deleteAssignments);
            psAssignments.setInt(1, cursoId);
            psAssignments.setInt(2, cursoId);
            psAssignments.executeUpdate();
            
            // Eliminar curso
            String deleteCourse = "DELETE FROM cursos WHERE id = ?";
            PreparedStatement psCourse = conect.prepareStatement(deleteCourse);
            psCourse.setInt(1, cursoId);
            psCourse.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Curso eliminado exitosamente");
            cargarCursos();
            txtAreaAlumnos.setText("");
            txtAreaProfesores.setText("");
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al eliminar curso: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void limpiarFiltros() {
    comboFiltroAnio.setSelectedItem("Todos");
    comboFiltroDivision.setSelectedItem("Todas");
    cargarCursos();
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
        jLabel2 = new javax.swing.JLabel();
        txtAnio = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDivision = new javax.swing.JTextField();
        btnCrearCurso = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        comboFiltroAnio = new javax.swing.JComboBox<>();
        comboFiltroDivision = new javax.swing.JComboBox<>();
        btnLimpiarFiltros = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaCursos = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnAsignarAlumnos = new javax.swing.JButton();
        btnAsignarProfesores = new javax.swing.JButton();
        btnEliminarCurso = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtAreaAlumnos = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtAreaProfesores = new javax.swing.JTextArea();
        comboEstadoCurso = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        btnGuardarCambios = new javax.swing.JButton();

        jLabel2.setText("Año");

        jLabel3.setText("Division");

        btnCrearCurso.setText("Crear Curso");
        btnCrearCurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearCursoActionPerformed(evt);
            }
        });

        jLabel1.setText("Gestion de Cursos");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(txtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(txtDivision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnCrearCurso))
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtDivision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCrearCurso))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jLabel4.setText("Filtrar Por");

        comboFiltroAnio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboFiltroAnio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFiltroAnioActionPerformed(evt);
            }
        });

        comboFiltroDivision.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboFiltroDivision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFiltroDivisionActionPerformed(evt);
            }
        });

        btnLimpiarFiltros.setText("Limpiar Filtro");
        btnLimpiarFiltros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarFiltrosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(174, 174, 174)
                        .addComponent(jLabel4))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(comboFiltroAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(comboFiltroDivision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiarFiltros)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboFiltroAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboFiltroDivision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiarFiltros))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        jScrollPane1.setViewportView(tablaCursos);

        btnAsignarAlumnos.setText("Asignar Alumnos");
        btnAsignarAlumnos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAsignarAlumnosActionPerformed(evt);
            }
        });

        btnAsignarProfesores.setText("Asignar Profesor");
        btnAsignarProfesores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAsignarProfesoresActionPerformed(evt);
            }
        });

        btnEliminarCurso.setText("Eliminar Curso");
        btnEliminarCurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarCursoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAsignarAlumnos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAsignarProfesores, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEliminarCurso, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAsignarAlumnos)
                    .addComponent(btnAsignarProfesores)
                    .addComponent(btnEliminarCurso))
                .addContainerGap(172, Short.MAX_VALUE))
        );

        jLabel7.setText("Alumnos");
        jScrollPane2.setViewportView(jLabel7);

        txtAreaAlumnos.setColumns(20);
        txtAreaAlumnos.setRows(5);
        jScrollPane3.setViewportView(txtAreaAlumnos);

        jLabel5.setText("Profesores");

        txtAreaProfesores.setColumns(20);
        txtAreaProfesores.setRows(5);
        jScrollPane4.setViewportView(txtAreaProfesores);

        comboEstadoCurso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Estado");

        btnGuardarCambios.setText("Guardar Cambios");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane4)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(comboEstadoCurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46)
                                .addComponent(btnGuardarCambios)))
                        .addGap(0, 161, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboEstadoCurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnGuardarCambios))
                        .addGap(133, 133, 133))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCrearCursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearCursoActionPerformed
        crearCurso();
    }//GEN-LAST:event_btnCrearCursoActionPerformed

    private void btnEliminarCursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarCursoActionPerformed
        eliminarCurso();
    }//GEN-LAST:event_btnEliminarCursoActionPerformed

    private void btnLimpiarFiltrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarFiltrosActionPerformed
         limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarFiltrosActionPerformed

    private void comboFiltroAnioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFiltroAnioActionPerformed
        cargarCursos();
    }//GEN-LAST:event_comboFiltroAnioActionPerformed

    private void comboFiltroDivisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFiltroDivisionActionPerformed
        cargarCursos();
    }//GEN-LAST:event_comboFiltroDivisionActionPerformed

    private void btnAsignarAlumnosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAsignarAlumnosActionPerformed
        int fila = tablaCursos.getSelectedRow();
    if (fila != -1) {
        int cursoId = (int) tablaCursos.getValueAt(fila, 0);
        AsignarAlumnosDialog dialog = new AsignarAlumnosDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this), 
            true, 
            cursoId);
        dialog.setVisible(true);
        if (dialog.seCambiaronAsignaciones()) {
            cargarCursos();
        }
    } else {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
    }
    }//GEN-LAST:event_btnAsignarAlumnosActionPerformed

    private void btnAsignarProfesoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAsignarProfesoresActionPerformed
         int fila = tablaCursos.getSelectedRow();
    if (fila != -1) {
        int cursoId = (int) tablaCursos.getValueAt(fila, 0);
        AsignarProfesoresDialog dialog = new AsignarProfesoresDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this), 
            true, 
            cursoId);
        dialog.setVisible(true);
        if (dialog.seCambiaronAsignaciones()) {
            cargarCursos();
        }
    } else {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
    }
    }//GEN-LAST:event_btnAsignarProfesoresActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAsignarAlumnos;
    private javax.swing.JButton btnAsignarProfesores;
    private javax.swing.JButton btnCrearCurso;
    private javax.swing.JButton btnEliminarCurso;
    private javax.swing.JButton btnGuardarCambios;
    private javax.swing.JButton btnLimpiarFiltros;
    private javax.swing.JComboBox<String> comboEstadoCurso;
    private javax.swing.JComboBox<String> comboFiltroAnio;
    private javax.swing.JComboBox<String> comboFiltroDivision;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable tablaCursos;
    private javax.swing.JTextField txtAnio;
    private javax.swing.JTextArea txtAreaAlumnos;
    private javax.swing.JTextArea txtAreaProfesores;
    private javax.swing.JTextField txtDivision;
    // End of variables declaration//GEN-END:variables
}
