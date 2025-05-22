package main.java.views.users.Alumnos;

import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;

/**
 * Interfaz para visualizar registros de asistencia de alumnos.
 * 
 * Características principales:
 * - Muestra tabla de asistencias
 * - Recupera datos de asistencia desde base de datos
 * - Utiliza patrón Singleton para conexión
 * 
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [12/03/2025]
 */
public class verasisten extends javax.swing.JFrame {

    // Conexión a la base de datos
    private Connection conect;
    // ID del alumno logueado
    private int alumnoId;
    
    /**
     * Constructor de la interfaz de visualización de asistencias.
     * 
     * Inicializa componentes y carga datos:
     * - Obtiene conexión mediante Singleton
     * - Recupera ID del alumno actualmente logueado
     * - Inicializa componentes de interfaz
     * - Muestra datos de asistencia del alumno
     */
    public verasisten() {
        // Obtener la conexión a través del Singleton
        conect = Conexion.getInstancia().verificarConexion();
        
        // Obtener el ID del alumno logueado desde la sesión actual
        alumnoId = SesionUsuario.getInstancia().getUsuarioId();
        
        initComponents();
        mostrarDatos();    // Mostrar datos al inicializar
    }
    
    /**
     * Carga y muestra los datos de asistencia en la tabla.
     * 
     * Pasos:
     * - Crea modelo de tabla
     * - Configura columnas
     * - Ejecuta consulta SQL para recuperar asistencias del alumno específico
     * - Añade filas al modelo de tabla con formato mejorado
     */
    public void mostrarDatos() {
        DefaultTableModel modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Fecha");
        modeloTabla.addColumn("Materia");
        modeloTabla.addColumn("Estado");
        modeloTabla.addColumn("Observaciones");
        tablasisten.setModel(modeloTabla);
        
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        
        try {
            // Consulta para obtener asistencias generales
            String consultaGeneral = 
                "SELECT ag.fecha, 'General' AS materia, ag.estado, oa.observacion " +
                "FROM asistencia_general ag " +
                "LEFT JOIN observaciones_asistencia oa ON ag.alumno_id = oa.alumno_id AND ag.fecha = oa.fecha " +
                "WHERE ag.alumno_id = ? " +
                "ORDER BY ag.fecha DESC";
            
            PreparedStatement psGeneral = conect.prepareStatement(consultaGeneral);
            psGeneral.setInt(1, alumnoId);
            ResultSet rsGeneral = psGeneral.executeQuery();
            
            // Consulta para obtener asistencias por materia
            String consultaMateria = 
                "SELECT am.fecha, m.nombre AS materia, am.estado, am.observaciones " +
                "FROM asistencia_materia am " +
                "INNER JOIN materias m ON am.materia_id = m.id " +
                "WHERE am.alumno_id = ? " +
                "ORDER BY am.fecha DESC, m.nombre";
            
            PreparedStatement psMateria = conect.prepareStatement(consultaMateria);
            psMateria.setInt(1, alumnoId);
            ResultSet rsMateria = psMateria.executeQuery();
            
            // Procesar asistencias generales
            while (rsGeneral.next()) {
                String[] fila = new String[4];
                
                // Formatear la fecha
                Date fecha = rsGeneral.getDate("fecha");
                fila[0] = fecha != null ? formatoFecha.format(fecha) : "N/A";
                
                fila[1] = rsGeneral.getString("materia"); // Siempre será "General"
                
                // Convertir el código de estado a texto explicativo
                String estadoCodigo = rsGeneral.getString("estado");
                fila[2] = convertirEstadoATexto(estadoCodigo);
                
                // Observaciones (puede ser null)
                fila[3] = rsGeneral.getString("observacion");
                if (fila[3] == null) {
                    fila[3] = "";
                }
                
                modeloTabla.addRow(fila);
            }
            
            // Procesar asistencias por materia
            while (rsMateria.next()) {
                String[] fila = new String[4];
                
                // Formatear la fecha
                Date fecha = rsMateria.getDate("fecha");
                fila[0] = fecha != null ? formatoFecha.format(fecha) : "N/A";
                
                fila[1] = rsMateria.getString("materia");
                
                // Convertir el código de estado a texto explicativo
                String estadoCodigo = rsMateria.getString("estado");
                fila[2] = convertirEstadoATexto(estadoCodigo);
                
                // Observaciones (puede ser null)
                fila[3] = rsMateria.getString("observaciones");
                if (fila[3] == null) {
                    fila[3] = "";
                }
                
                modeloTabla.addRow(fila);
            }
            
            // Si no hay registros, mostrar mensaje
            if (modeloTabla.getRowCount() == 0) {
                String[] fila = {"No hay registros", "", "", ""};
                modeloTabla.addRow(fila);
            }
            
            // Actualizar la tabla
            tablasisten.setModel(modeloTabla);
            
            // Cerrar resultsets
            rsGeneral.close();
            rsMateria.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar asistencias: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Convierte el código de estado de asistencia a una descripción textual.
     * 
     * @param codigo Código de estado (P, A, T, etc.)
     * @return Descripción textual del estado
     */
    private String convertirEstadoATexto(String codigo) {
        if (codigo == null) return "Desconocido";
        
        switch (codigo) {
            case "P": return "Presente";
            case "A": return "Ausente";
            case "T": return "Tarde";
            case "1/2": return "Media Falta";
            case "J": return "Justificada";
            default: return codigo;
        }
    }
    
    // Clase para manejar la sesión del usuario (igual que en el ejemplo anterior)
    public static class SesionUsuario {
        private static SesionUsuario instancia;
        private int usuarioId;
        
        private SesionUsuario() {}
        
        public static SesionUsuario getInstancia() {
            if (instancia == null) {
                instancia = new SesionUsuario();
            }
            return instancia;
        }
        
        public void setUsuarioId(int id) {
            this.usuarioId = id;
        }
        
        public int getUsuarioId() {
            return usuarioId;
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        botreg = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablasisten = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N

        botreg.setBackground(new java.awt.Color(102, 153, 255));
        botreg.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botreg.setForeground(new java.awt.Color(255, 255, 255));
        botreg.setText("Volver");
        botreg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botregActionPerformed(evt);
            }
        });

        tablasisten.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablasisten);

        jLabel3.setFont(new java.awt.Font("Swis721 Lt BT", 1, 24)); // NOI18N
        jLabel3.setText("ASISTENCIAS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(296, 296, 296))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(68, 68, 68)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(68, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addComponent(jLabel3)
                .addGap(523, 523, 523)
                .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(174, 174, 174)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(189, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botregActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botregActionPerformed
        
        this.setVisible(false);
    }//GEN-LAST:event_botregActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(verasisten.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new verasisten().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botreg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablasisten;
    // End of variables declaration//GEN-END:variables
}
