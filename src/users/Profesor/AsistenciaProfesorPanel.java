/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package users.Profesor;

import users.common.AsistenciaPanel;
import users.common.EstadoAsistenciaEditor;
import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.TableColumn;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;
import login.Conexion;
import java.util.List;
import java.util.Collections;




public class AsistenciaProfesorPanel extends AsistenciaPanel {
    private int cursoId;
    private int materiaId;
    private Set<LocalDate> diasClase;
    private String nombreMateria;
    private String nombreCurso;
    
    public AsistenciaProfesorPanel(int profesorId, int cursoId, int materiaId) {
    super();
    conect = Conexion.getInstancia().getConexion();
    if (conect == null) {
        JOptionPane.showMessageDialog(this, "Error de conexión.");
        return;
    }
    
    System.out.println("Creando panel con:");
    System.out.println("Profesor ID: " + profesorId);
    System.out.println("Curso ID: " + cursoId);
    System.out.println("Materia ID: " + materiaId);
    
    initComponents();
    this.usuarioId = profesorId;
    this.cursoId = cursoId;
    this.materiaId = materiaId;
    this.fecha = LocalDate.now();
    
    inicializarBase();
    this.diasClase = obtenerDiasClase();
    cargarDatosMateriaCurso();
    cargarAsistencias();
    configurarEventos();
}
    
    private void inicializarBase() {
        this.tableModel = new DefaultTableModel();
        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);
        }
        inicializarColores();
        configurarTabla();
    }
    
    private void configurarEventos() {
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (dateChooser.getDate() != null) {
                fecha = dateChooser.getDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
                cargarAsistencias();
            }
        });
        
        btnGuardar.addActionListener(e -> guardarAsistencias());
        btnCancelar.addActionListener(e -> cargarAsistencias());
    }
    private void inicializarColores() {
        colorEstados = new HashMap<>();
        colorEstados.put("P", new Color(144, 238, 144));  // Verde claro
        colorEstados.put("A", new Color(255, 182, 193));  // Rojo claro
        colorEstados.put("T", new Color(255, 255, 153));  // Amarillo claro
        colorEstados.put("AP", new Color(255, 218, 185)); // Naranja claro
        colorEstados.put("NC", Color.WHITE);              // No cargado
    }

    private Set<LocalDate> obtenerDiasClase() {
    Set<LocalDate> dias = new HashSet<>();
    try {
        // Obtener el lunes de la semana actual
        LocalDate inicioDeSemana = fecha;
        while (inicioDeSemana.getDayOfWeek() != DayOfWeek.MONDAY) {
            inicioDeSemana = inicioDeSemana.minusDays(1);
        }
        
        String query = "SELECT dia_semana FROM horarios_materia " +
                      "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?";
        
        System.out.println("Buscando horarios para:");
        System.out.println("Profesor ID: " + usuarioId);
        System.out.println("Curso ID: " + cursoId);
        System.out.println("Materia ID: " + materiaId);
        
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setInt(1, usuarioId);
        ps.setInt(2, cursoId);
        ps.setInt(3, materiaId);
        
        System.out.println("Ejecutando query: " + query);
        ResultSet rs = ps.executeQuery();
        
        System.out.println("ResultSet obtenido, buscando días...");
        
        while (rs.next()) {
            int diaSemana = rs.getInt("dia_semana");
            System.out.println("Encontrado día de semana: " + diaSemana);
            
            // diaSemana es 1 para Lunes, 2 para Martes, etc.
            LocalDate fechaClase = inicioDeSemana.plusDays(diaSemana - 1);
            dias.add(fechaClase);
            System.out.println("Agregada fecha: " + fechaClase.format(DateTimeFormatter.ofPattern("dd/MM (EEE)")));
        }
        
    } catch (SQLException ex) {
        System.out.println("Error SQL: " + ex.getMessage());
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al obtener días de clase: " + ex.getMessage());
    }
    
    System.out.println("Total días encontrados: " + dias.size());
    return dias;
}
    
    private void cargarDatosMateriaCurso() {
         try {
        // Cargar datos de materia y curso
        String query = "SELECT m.nombre as materia, CONCAT(c.anio, '°', c.division) as curso " +
                      "FROM materias m " +
                      "JOIN cursos c ON c.id = ? " +
                      "WHERE m.id = ?";
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setInt(1, cursoId);
        ps.setInt(2, materiaId);
        
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            nombreMateria = rs.getString("materia");
            nombreCurso = rs.getString("curso");
            lblMateria.setText("Materia: " + nombreMateria);
            lblCurso.setText("Curso: " + nombreCurso);
        }

        // Cargar y mostrar días de clase
        String diasQuery = "SELECT dia_semana, hora_inicio, hora_fin FROM horarios_materia " +
                          "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?";
        ps = conect.prepareStatement(diasQuery);
        ps.setInt(1, usuarioId);
        ps.setInt(2, cursoId);
        ps.setInt(3, materiaId);
        
        rs = ps.executeQuery();
        StringBuilder horarios = new StringBuilder("Horarios: ");
        while (rs.next()) {
            String dia = getDiaSemana(rs.getInt("dia_semana"));
            String horaInicio = rs.getTime("hora_inicio").toString();
            String horaFin = rs.getTime("hora_fin").toString();
            horarios.append(dia).append(" (").append(horaInicio).append(" - ").append(horaFin).append(") ");
        }
        lblHorario.setText(horarios.toString());
        
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage());
    }
}

private String getDiaSemana(int dia) {
    switch(dia) {
        case 1: return "Lunes";
        case 2: return "Martes";
        case 3: return "Miércoles";
        case 4: return "Jueves";
        case 5: return "Viernes";
        default: return "";
    }
}
    
    protected void cargarAsistencias() {
    try {
        System.out.println("Iniciando carga de asistencias..."); // Debug
        
        // Limpiar tabla actual
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        // Configurar columnas fijas
        tableModel.addColumn("Alumno");
        tableModel.addColumn("DNI");
        
        // Agregar columnas para los días de clase
        List<LocalDate> diasOrdenados = new ArrayList<>(diasClase);
        Collections.sort(diasOrdenados); // Ordenar días cronológicamente
        
        System.out.println("Días de clase a mostrar: " + diasOrdenados.size()); // Debug
        
        for (LocalDate dia : diasOrdenados) {
            String columnName = dia.format(DateTimeFormatter.ofPattern("dd/MM (EEE)"));
            tableModel.addColumn(columnName);
            System.out.println("Agregando columna: " + columnName); // Debug
        }
        
        // Cargar alumnos
        String queryAlumnos = 
            "SELECT DISTINCT u.id, u.nombre, u.apellido, u.dni " +
            "FROM usuarios u " +
            "JOIN alumno_curso ac ON u.id = ac.alumno_id " +
            "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 " +
            "ORDER BY u.apellido, u.nombre";
            
        PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
        psAlumnos.setInt(1, cursoId);
        ResultSet rsAlumnos = psAlumnos.executeQuery();
        
        while (rsAlumnos.next()) {
            Object[] rowData = new Object[2 + diasOrdenados.size()];
            rowData[0] = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
            rowData[1] = rsAlumnos.getString("dni");
            
            // Inicializar estados como NC
            for (int i = 2; i < rowData.length; i++) {
                rowData[i] = "NC";
            }
            tableModel.addRow(rowData);
        }
        
        // Cargar asistencias existentes
        for (int diaIndex = 0; diaIndex < diasOrdenados.size(); diaIndex++) {
            LocalDate dia = diasOrdenados.get(diaIndex);
            String queryAsistencias = 
                "SELECT alumno_id, estado " +
                "FROM asistencia_materia " +
                "WHERE curso_id = ? AND materia_id = ? AND fecha = ?";
                
            PreparedStatement psAsistencias = conect.prepareStatement(queryAsistencias);
            psAsistencias.setInt(1, cursoId);
            psAsistencias.setInt(2, materiaId);
            psAsistencias.setDate(3, java.sql.Date.valueOf(dia));
            
            final int columnIndex = diaIndex + 2; // +2 por las columnas Alumno y DNI
            
            ResultSet rsAsistencias = psAsistencias.executeQuery();
            while (rsAsistencias.next()) {
                String dni = rsAsistencias.getString("alumno_id");
                String estado = rsAsistencias.getString("estado");
                
                // Buscar al alumno y actualizar su estado
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1).toString().equals(dni)) {
                        tableModel.setValueAt(estado, i, columnIndex);
                        break;
                    }
                }
            }
        }
        
        // Configurar el editor para las columnas de estado
        for (int i = 2; i < tableModel.getColumnCount(); i++) {
            TableColumn column = tablaAsistencia.getColumnModel().getColumn(i);
            column.setCellEditor(new EstadoAsistenciaEditor());
        }
        
        System.out.println("Carga de asistencias completada."); // Debug
        
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
    }
}
    
      @Override
    protected void guardarAsistencias() {
        try {
            // Primero eliminar asistencias existentes para esta fecha
            String deleteQuery = 
                "DELETE FROM asistencia_materia " +
                "WHERE fecha = ? AND curso_id = ? AND materia_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setDate(1, java.sql.Date.valueOf(fecha));
            deletePs.setInt(2, cursoId);
            deletePs.setInt(3, materiaId);
            deletePs.executeUpdate();
            
            // Insertar nuevas asistencias
            String insertQuery = 
                "INSERT INTO asistencia_materia " +
                "(alumno_id, curso_id, materia_id, fecha, estado, creado_por) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String estado = tableModel.getValueAt(i, 2).toString();
                if (!estado.equals("NC")) {
                    insertPs.setString(1, tableModel.getValueAt(i, 1).toString()); // alumno_id (DNI)
                    insertPs.setInt(2, cursoId);
                    insertPs.setInt(3, materiaId);
                    insertPs.setDate(4, java.sql.Date.valueOf(fecha));
                    insertPs.setString(5, estado);
                    insertPs.setInt(6, usuarioId);
                    insertPs.executeUpdate();
                }
            }
            
            JOptionPane.showMessageDialog(this, "Asistencias guardadas exitosamente");
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
        }
    }
    private void importarAsistenciaGeneral() {
    try {
        // Verificar si existe asistencia general para la fecha actual
        String query = "SELECT ag.alumno_id, ag.estado " +
                      "FROM asistencia_general ag " +
                      "WHERE ag.curso_id = ? AND ag.fecha = ?";
        
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setInt(1, cursoId);
        ps.setDate(2, java.sql.Date.valueOf(fecha));
        
        ResultSet rs = ps.executeQuery();
        boolean hayAsistencia = false;
        
        while (rs.next()) {
            hayAsistencia = true;
            String alumnoId = rs.getString("alumno_id");
            String estado = rs.getString("estado");
            
            // Actualizar la tabla con los estados
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 1).toString().equals(alumnoId)) {
                    // Encontrar la columna correspondiente a la fecha actual
                    for (int j = 2; j < tableModel.getColumnCount(); j++) {
                        String columnDate = tableModel.getColumnName(j).split(" ")[0]; // Obtener solo la fecha
                        if (columnDate.equals(fecha.format(DateTimeFormatter.ofPattern("dd/MM")))) {
                            tableModel.setValueAt(estado, i, j);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        if (!hayAsistencia) {
            JOptionPane.showMessageDialog(this, 
                "No hay asistencia general registrada para esta fecha",
                "Información", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Asistencia general importada correctamente",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error al importar asistencia general: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    }
    @Override
    protected boolean puedeEditarCelda(int row, int column) {
        return column == 2 && diasClase.contains(fecha);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        lblMateria = new javax.swing.JLabel();
        lblCurso = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        lblHorario = new javax.swing.JLabel();
        centralPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        tablaAsistencia = new javax.swing.JTable();
        bottomPanel = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnImportar = new javax.swing.JButton();

        lblMateria.setText("Materia");

        lblCurso.setText("Curso");

        lblFecha.setText("Fecha");

        lblHorario.setText("Horario");

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(lblMateria)
                .addGap(132, 132, 132)
                .addComponent(lblCurso)
                .addGap(122, 122, 122)
                .addComponent(lblHorario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFecha)
                .addGap(33, 33, 33)
                .addComponent(dateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMateria)
                        .addComponent(lblCurso)
                        .addComponent(lblFecha)
                        .addComponent(lblHorario)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        javax.swing.GroupLayout centralPanelLayout = new javax.swing.GroupLayout(centralPanel);
        centralPanel.setLayout(centralPanelLayout);
        centralPanelLayout.setHorizontalGroup(
            centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(centralPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        centralPanelLayout.setVerticalGroup(
            centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 453, Short.MAX_VALUE)
            .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(centralPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnImportar.setText("Importar Asistencia del Preceptor");
        btnImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(btnGuardar)
                .addGap(272, 272, 272)
                .addComponent(btnImportar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addComponent(btnCancelar)
                .addGap(27, 27, 27))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar)
                    .addComponent(btnImportar))
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       guardarAsistencias();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
       cargarAsistencias();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnImportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportarActionPerformed
        importarAsistenciaGeneral();
    }//GEN-LAST:event_btnImportarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnImportar;
    private javax.swing.JPanel centralPanel;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JLabel lblCurso;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblHorario;
    private javax.swing.JLabel lblMateria;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable tablaAsistencia;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
