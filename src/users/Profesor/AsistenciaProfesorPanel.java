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
import java.util.Map;

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

            String query = "SELECT dia_semana, hora_inicio, hora_fin FROM horarios_materia "
                    + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int diaSemana = rs.getInt("dia_semana");
                // Calcular la fecha para este día de la semana
                LocalDate fechaClase = inicioDeSemana.plusDays(diaSemana - 1);
                dias.add(fechaClase);
            }

        } catch (SQLException ex) {
            System.out.println("Error SQL: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al obtener días de clase: " + ex.getMessage());
        }

        return dias;
    }

    private boolean esDiaClaseValido(LocalDate fecha) {
        try {
            String query = "SELECT COUNT(*) FROM horarios_materia "
                    + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? "
                    + "AND dia_semana = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, usuarioId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);
            ps.setInt(4, fecha.getDayOfWeek().getValue()); // getValue() retorna 1 para Lunes, ... 7 para Domingo

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void cargarDatosMateriaCurso() {
        try {
            // Cargar datos de materia y curso
            String query = "SELECT m.nombre as materia, CONCAT(c.anio, '°', c.division) as curso "
                    + "FROM materias m "
                    + "JOIN cursos c ON c.id = ? "
                    + "WHERE m.id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ps.setInt(2, materiaId);

            System.out.println("Cargando datos de materia y curso...");
            System.out.println("CursoID: " + cursoId);
            System.out.println("MateriaID: " + materiaId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombreMateria = rs.getString("materia");
                nombreCurso = rs.getString("curso");
                lblMateria.setText("Materia: " + nombreMateria);
                lblCurso.setText("Curso: " + nombreCurso);

                System.out.println("Materia: " + nombreMateria);
                System.out.println("Curso: " + nombreCurso);
            }

            // Cargar y mostrar días de clase con horarios
            String diasQuery = "SELECT dia_semana, hora_inicio, hora_fin "
                    + "FROM horarios_materia "
                    + "WHERE profesor_id = ? AND curso_id = ? AND materia_id = ? "
                    + "ORDER BY dia_semana, hora_inicio";
            ps = conect.prepareStatement(diasQuery);
            ps.setInt(1, usuarioId);
            ps.setInt(2, cursoId);
            ps.setInt(3, materiaId);

            System.out.println("Consultando horarios para Profesor: " + usuarioId
                    + ", Curso: " + cursoId + ", Materia: " + materiaId);

            rs = ps.executeQuery();
            StringBuilder horarios = new StringBuilder("Horarios: ");
            boolean tieneHorarios = false;

            while (rs.next()) {
                tieneHorarios = true;
                String dia = getDiaSemana(rs.getInt("dia_semana"));
                String horaInicio = rs.getTime("hora_inicio").toString().substring(0, 5);
                String horaFin = rs.getTime("hora_fin").toString().substring(0, 5);
                horarios.append(dia).append(" (").append(horaInicio)
                        .append(" - ").append(horaFin).append(") ");

                System.out.println("Horario encontrado: " + dia + " de "
                        + horaInicio + " a " + horaFin);
            }

            if (!tieneHorarios) {
                horarios.append("No hay horarios configurados");
                System.out.println("¡ADVERTENCIA! No se encontraron horarios");
            }

            lblHorario.setText(horarios.toString());
            System.out.println("Texto final de horarios: " + horarios.toString());

        } catch (SQLException ex) {
            System.out.println("Error SQL: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage());
        }
    }

    private String getDiaSemana(int dia) {
        switch (dia) {
            case 1:
                return "Lunes";
            case 2:
                return "Martes";
            case 3:
                return "Miércoles";
            case 4:
                return "Jueves";
            case 5:
                return "Viernes";
            default:
                return "";
        }
    }

    protected void cargarAsistencias() {
        try {
            // Limpiar tabla actual
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            // Configurar columnas fijas
            tableModel.addColumn("Alumno");
            tableModel.addColumn("DNI");
            // Mostrar la fecha formateada como título de la columna
            tableModel.addColumn(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Verificar si es día de clase usando el nuevo método
            if (!esDiaClaseValido(fecha)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha seleccionada no es un día de clase para esta materia.",
                        "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Cargar alumnos
            String queryAlumnos
                    = "SELECT DISTINCT u.id as usuario_id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            // Crear un mapa para almacenar el ID del usuario junto con sus datos
            Map<String, Integer> dniToIdMap = new HashMap<>();

            while (rsAlumnos.next()) {
                Object[] rowData = new Object[3];
                rowData[0] = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                String dni = rsAlumnos.getString("dni");
                rowData[1] = dni;
                rowData[2] = "NC";  // Estado inicial
                tableModel.addRow(rowData);

                // Guardar la relación DNI -> ID
                dniToIdMap.put(dni, rsAlumnos.getInt("usuario_id"));
            }

            // Guardar el mapa como una propiedad de la tabla para usarlo al guardar
            tablaAsistencia.putClientProperty("dniToIdMap", dniToIdMap);

            // Cargar asistencias existentes
            String queryAsistencias
                    = "SELECT alumno_id, estado "
                    + "FROM asistencia_materia "
                    + "WHERE curso_id = ? AND materia_id = ? AND fecha = ?";

            PreparedStatement psAsistencias = conect.prepareStatement(queryAsistencias);
            psAsistencias.setInt(1, cursoId);
            psAsistencias.setInt(2, materiaId);
            psAsistencias.setDate(3, java.sql.Date.valueOf(fecha));

            ResultSet rsAsistencias = psAsistencias.executeQuery();
            while (rsAsistencias.next()) {
                int usuarioId = rsAsistencias.getInt("alumno_id");
                String estado = rsAsistencias.getString("estado");

                // Buscar el DNI correspondiente al ID y actualizar su estado
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String dni = (String) tableModel.getValueAt(i, 1);
                    if (dniToIdMap.get(dni) == usuarioId) {
                        tableModel.setValueAt(estado, i, 2);
                        break;
                    }
                }
            }

            // Configurar el editor para la columna de estado
            TableColumn column = tablaAsistencia.getColumnModel().getColumn(2);
            column.setCellEditor(new EstadoAsistenciaEditor());

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar asistencias: " + ex.getMessage());
        }
    }

    @Override
    protected void guardarAsistencias() {
        try {
            // Obtener el mapa DNI -> ID que guardamos en la tabla
            @SuppressWarnings("unchecked")
            Map<String, Integer> dniToIdMap = (Map<String, Integer>) tablaAsistencia.getClientProperty("dniToIdMap");

            if (dniToIdMap == null) {
                JOptionPane.showMessageDialog(this, "Error: No se encontró la información de los alumnos");
                return;
            }

            // Primero eliminar asistencias existentes para esta fecha
            String deleteQuery
                    = "DELETE FROM asistencia_materia "
                    + "WHERE fecha = ? AND curso_id = ? AND materia_id = ?";
            PreparedStatement deletePs = conect.prepareStatement(deleteQuery);
            deletePs.setDate(1, java.sql.Date.valueOf(fecha));
            deletePs.setInt(2, cursoId);
            deletePs.setInt(3, materiaId);
            deletePs.executeUpdate();

            // Insertar nuevas asistencias
            String insertQuery
                    = "INSERT INTO asistencia_materia "
                    + "(alumno_id, curso_id, materia_id, fecha, estado, creado_por) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conect.prepareStatement(insertQuery);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String estado = tableModel.getValueAt(i, 2).toString();
                if (!estado.equals("NC")) {
                    String dni = tableModel.getValueAt(i, 1).toString();
                    Integer usuarioId = dniToIdMap.get(dni);

                    if (usuarioId == null) {
                        JOptionPane.showMessageDialog(this,
                                "Error: No se encontró el ID para el alumno con DNI " + dni);
                        continue;
                    }

                    insertPs.setInt(1, usuarioId);  // Usar el ID del usuario, no el DNI
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar asistencias: " + ex.getMessage());
        }
    }

    private void importarAsistenciaGeneral() {
        try {
            // Verificar si existe asistencia general para la fecha actual
            String query = "SELECT ag.alumno_id, ag.estado "
                    + "FROM asistencia_general ag "
                    + "WHERE ag.curso_id = ? AND ag.fecha = ?";

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
