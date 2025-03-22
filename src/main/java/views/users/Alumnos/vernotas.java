package main.java.views.users.Alumnos;

import java.sql.*;
import javax.swing.*;
import login.*;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;

public class vernotas extends javax.swing.JFrame {
    
    // Mantén la conexión abierta a nivel de la clase
    private Connection conect;
    private int alumnoId; // Variable para almacenar el ID del alumno logueado
    
    public vernotas() {
        initComponents();
        probar_conexion(); // Aquí obtienes la conexión desde el Singleton
        
        // Obtener el ID del alumno logueado desde la sesión actual
        alumnoId = SesionUsuario.getInstancia().getUsuarioId();
        
        mostrarDatos();    // Mostrar datos al inicializar
    }
    
    private void probar_conexion() {
        // Obtenemos la conexión desde el Singleton
        conect = Conexion.getInstancia().verificarConexion();
        // Verificamos si la conexión es válida
        if (conect != null) {
            System.out.println("Conexión exitosa");
        } else {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }
    
    public void mostrarDatos() {
        DefaultTableModel tcliente = new DefaultTableModel();
        tcliente.addColumn("Materia");
        tcliente.addColumn("Nota");
        tcliente.addColumn("Evaluación");
        tcliente.addColumn("Fecha");
        tablanotas.setModel(tcliente);
        
        try {
            // Primero, obtenemos todas las materias asignadas al alumno a través de su curso
            String consultaCurso = "SELECT curso_id FROM alumno_curso WHERE alumno_id = ? AND estado = 'activo'";
            PreparedStatement psCurso = conect.prepareStatement(consultaCurso);
            psCurso.setInt(1, alumnoId);
            ResultSet rsCurso = psCurso.executeQuery();
            
            if (rsCurso.next()) {
                int cursoId = rsCurso.getInt("curso_id");
                
                // Obtenemos todas las materias asociadas al curso del alumno
                String consultaMaterias = "SELECT m.id, m.nombre FROM materias m " +
                                         "INNER JOIN profesor_curso_materia pcm ON m.id = pcm.materia_id " +
                                         "WHERE pcm.curso_id = ? AND pcm.estado = 'activo'";
                PreparedStatement psMaterias = conect.prepareStatement(consultaMaterias);
                psMaterias.setInt(1, cursoId);
                ResultSet rsMaterias = psMaterias.executeQuery();
                
                // Para cada materia, buscamos si hay calificaciones del alumno
                while (rsMaterias.next()) {
                    int materiaId = rsMaterias.getInt("id");
                    String nombreMateria = rsMaterias.getString("nombre");
                    
                    String consultaCalificaciones = "SELECT c.nota, ae.nombre as evaluacion, c.fecha_carga FROM calificaciones c " +
                                                    "INNER JOIN actividades_evaluables ae ON c.actividad_id = ae.id " +
                                                    "WHERE c.alumno_id = ? AND ae.materia_id = ?";
                    PreparedStatement psCalificaciones = conect.prepareStatement(consultaCalificaciones);
                    psCalificaciones.setInt(1, alumnoId);
                    psCalificaciones.setInt(2, materiaId);
                    ResultSet rsCalificaciones = psCalificaciones.executeQuery();
                    
                    // Si hay calificaciones, las agregamos a la tabla
                    boolean tieneNotas = false;
                    while (rsCalificaciones.next()) {
                        tieneNotas = true;
                        String[] datos = new String[4];
                        datos[0] = nombreMateria;
                        datos[1] = rsCalificaciones.getString("nota");
                        datos[2] = rsCalificaciones.getString("evaluacion");
                        
                        // Formatear la fecha para presentación
                        Timestamp fechaCarga = rsCalificaciones.getTimestamp("fecha_carga");
                        datos[3] = (fechaCarga != null) ? fechaCarga.toString().substring(0, 10) : "N/A";
                        
                        tcliente.addRow(datos);
                    }
                    
                    // Si no tiene notas para esta materia, agregamos una fila con "Sin Nota"
                    if (!tieneNotas) {
                        String[] datos = new String[4];
                        datos[0] = nombreMateria;
                        datos[1] = "Sin Nota";
                        datos[2] = "N/A";
                        datos[3] = "N/A";
                        tcliente.addRow(datos);
                    }
                    
                    rsCalificaciones.close();
                }
                
                rsMaterias.close();
            } else {
                JOptionPane.showMessageDialog(this, "El alumno no está asignado a ningún curso activo.");
            }
            
            rsCurso.close();
            
            // Actualizar la tabla con los datos obtenidos
            tablanotas.setModel(tcliente);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error en la consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Necesitaremos una clase para manejar la sesión del usuario
    // Esto es un ejemplo, ajusta según tu implementación actual
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

        jFrame1 = new javax.swing.JFrame();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        botreg = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablanotas = new javax.swing.JTable();

        jFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 770, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 813, Short.MAX_VALUE)
        );

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

        jLabel3.setFont(new java.awt.Font("Swis721 Lt BT", 1, 24)); // NOI18N
        jLabel3.setText("NOTAS");

        tablanotas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablanotas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(336, 336, 336)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addComponent(jLabel3)
                .addGap(40, 40, 40)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(botreg, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel2))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botregActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botregActionPerformed
        alumnos log = new alumnos();
        log.setVisible(true);
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
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(vernotas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new vernotas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botreg;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablanotas;
    // End of variables declaration//GEN-END:variables
}
