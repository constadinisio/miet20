/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package users.Preceptor;

import java.sql.*;
import java.time.LocalDate;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.*;
import users.common.AsistenciaPanel;
import users.common.EstadoAsistenciaEditor;
import com.toedter.calendar.JDateChooser;
import login.Conexion;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import java.time.ZoneId;
import javax.swing.JButton;
import javax.swing.JLabel;
import com.toedter.calendar.JDateChooser;


/**
 *
 * @author nico_
 */
public class AsistenciaPreceptorPanel extends AsistenciaPanel {
    private int cursoId;
    
    public AsistenciaPreceptorPanel(int preceptorId, int cursoId) {
        super();
        conect = Conexion.getInstancia().getConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
        }
        
        initComponents();
        this.usuarioId = preceptorId;
        this.cursoId = cursoId;
        this.fecha = LocalDate.now();
        
        inicializarBase();
        cargarDatosCurso();
        cargarAsistencias();
        configurarEventos();
    }
    
    private void cargarDatosCurso() {
        try {
            String query = "SELECT CONCAT(c.anio, '°', c.division) as curso " +
                          "FROM cursos c WHERE c.id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblCurso.setText("Curso: " + rs.getString("curso"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage());
        }
    }
    
    private void inicializarBase() {
    // Inicializar tabla y modelo
    this.tableModel = new DefaultTableModel();
    if (tablaAsistencia != null) {
        tablaAsistencia.setModel(tableModel);
    }
    inicializarColores();
    configurarTabla();
}

private void inicializarColores() {
    colorEstados = new HashMap<>();
    colorEstados.put("P", new Color(144, 238, 144));  // Verde claro
    colorEstados.put("A", new Color(255, 182, 193));  // Rojo claro
    colorEstados.put("T", new Color(255, 255, 153));  // Amarillo claro
    colorEstados.put("AP", new Color(255, 218, 185)); // Naranja claro
    colorEstados.put("NC", Color.WHITE);
}

    @Override
    protected void configurarTabla() {
    if (tableModel == null) {
        tableModel = new DefaultTableModel();
    }
    
    tableModel.addColumn("Alumno");
    tableModel.addColumn("DNI");
    tableModel.addColumn("Estado");
    
    if (tablaAsistencia != null) {
        tablaAsistencia.setModel(tableModel);
    }
}

private void configurarEventos() {
    // Configurar event listener para el dateChooser
    if (dateChooser != null) {
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (dateChooser.getDate() != null) {
                fecha = dateChooser.getDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
                cargarAsistencias();
            }
        });
    }

    // Configurar event listeners para los botones
    if (btnGuardar != null) {
        btnGuardar.addActionListener(e -> guardarAsistencias());
    }
    
    if (btnCancelar != null) {
        btnCancelar.addActionListener(e -> cargarAsistencias());
    }
}
    
    protected void cargarAsistencias() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            
            // Configurar columnas
            tableModel.addColumn("Alumno");
            tableModel.addColumn("DNI");
            tableModel.addColumn("Estado");
            
            // Cargar alumnos
            String queryAlumnos = 
                "SELECT u.id, u.nombre, u.apellido, u.dni " +
                "FROM usuarios u " +
                "JOIN alumno_curso ac ON u.id = ac.alumno_id " +
                "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 " +
                "ORDER BY u.apellido, u.nombre";
                
            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();
            
            while (rsAlumnos.next()) {
                Object[] rowData = new Object[3];
                rowData[0] = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                rowData[1] = rsAlumnos.getString("dni");
                rowData[2] = "NC";
                tableModel.addRow(rowData);
            }
            
            // Cargar asistencias existentes
            String queryAsistencias = 
                "SELECT alumno_id, estado FROM asistencia_general " +
                "WHERE curso_id = ? AND fecha = ?";
                
            PreparedStatement psAsistencias = conect.prepareStatement(queryAsistencias);
            psAsistencias.setInt(1, cursoId);
            psAsistencias.setDate(2, java.sql.Date.valueOf(fecha));
            
            ResultSet rsAsistencias = psAsistencias.executeQuery();
            while (rsAsistencias.next()) {
                String dni = rsAsistencias.getString("alumno_id");
                String estado = rsAsistencias.getString("estado");
                
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1).toString().equals(dni)) {
                        tableModel.setValueAt(estado, i, 2);
                        break;
                    }
                }
            }
            
            // Configurar editor para la columna de estado
            TableColumn estadoColumn = tablaAsistencia.getColumnModel().getColumn(2);
            estadoColumn.setCellEditor(new EstadoAsistenciaEditor());
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
        }
    }
    
    protected void guardarAsistencias() {
        try {
            // Eliminar asistencias existentes
            String deleteQuery = 
                "DELETE FROM asistencia_general WHERE fecha = ? AND curso_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setDate(1, java.sql.Date.valueOf(fecha));
            deletePs.setInt(2, cursoId);
            deletePs.executeUpdate();
            
            // Insertar nuevas asistencias
            String insertQuery = 
                "INSERT INTO asistencia_general " +
                "(alumno_id, curso_id, fecha, estado, creado_por) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String estado = tableModel.getValueAt(i, 2).toString();
                if (!estado.equals("NC")) {
                    insertPs.setString(1, tableModel.getValueAt(i, 1).toString());
                    insertPs.setInt(2, cursoId);
                    insertPs.setDate(3, java.sql.Date.valueOf(fecha));
                    insertPs.setString(4, estado);
                    insertPs.setInt(5, usuarioId);
                    insertPs.executeUpdate();
                }
            }
            
            JOptionPane.showMessageDialog(this, "Asistencias guardadas exitosamente");
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
        }
    }
    
    protected boolean puedeEditarCelda(int row, int column) {
        return column == 2;  // Solo la columna de estado es editable
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
        lblCurso = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        tablaAsistencia = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        lblCurso.setText("Curso");
        jPanel1.add(lblCurso);

        lblFecha.setText("Fecha");
        jPanel1.add(lblFecha);
        jPanel1.add(dateChooser);

        jPanel2.setLayout(new java.awt.BorderLayout());

        tablaAsistencia.setModel(new javax.swing.table.DefaultTableModel(
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
        scrollPane.setViewportView(tablaAsistencia);

        jPanel2.add(scrollPane, java.awt.BorderLayout.CENTER);

        btnGuardar.setText("Guardar");
        jPanel3.add(btnGuardar);

        btnCancelar.setText("Cancelar");
        jPanel3.add(btnCancelar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 57, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblCurso;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable tablaAsistencia;
    // End of variables declaration//GEN-END:variables
}
