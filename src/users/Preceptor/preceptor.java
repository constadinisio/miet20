package users.Preceptor;

import java.sql.*;
import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.awt.Dimension;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Font;
import login.Conexion;
import users.Preceptor.AsistenciaPreceptorPanel;
import java.util.HashMap;
import java.util.Map;

/**
 * Interfaz principal para usuarios con rol de Preceptor.
 * 
 * Funcionalidades principales:
 * - Gestión de asistencias por curso
 * - Selección de curso y fecha
 * - Navegación entre diferentes módulos
 * 
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [3/12/2025]
 */

public class preceptor extends javax.swing.JFrame {

    // Conexión a base de datos
    private Connection conect;
    
    // Identificador del preceptor actual
    private int preceptorId;
    
    // Fecha seleccionada para consultas
    private LocalDate fechaSeleccionada;
    
    // Mapa para almacenar cursos y sus IDs
    private Map<String, Integer> cursosMap = new HashMap<>();

    /**
     * Constructor de la interfaz de Preceptor.
     * 
     * Inicializa componentes:
     * - Verifica conexión a base de datos
     * - Configura imágenes
     * - Carga componentes iniciales
     * 
     * @param preceptorId Identificador del preceptor que inicia sesión
     */
    public preceptor(int preceptorId) {
        this.preceptorId = preceptorId;
        this.fechaSeleccionada = LocalDate.now();
        initComponents();
        probar_conexion();

        // Escalar imágenes
        rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, "src/images/logo et20 buena calidad.png");

        // Inicializar combo de cursos y fecha
        inicializarComponentes();
        cargarCursos();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Verifica la conexión a la base de datos.
     * Muestra un mensaje de error si no se puede establecer conexión.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().getConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }
    
    /**
     * Inicializa los componentes interactivos:
     * - Selector de fecha
     * - Combo de cursos
     * Configura listeners para cambios de selección
     */
    private void inicializarComponentes() {
         // Configurar selector de fecha
    dateChooser.setDate(java.sql.Date.valueOf(fechaSeleccionada));
    dateChooser.addPropertyChangeListener("date", evt -> {
        if ("date".equals(evt.getPropertyName())) {
            fechaSeleccionada = dateChooser.getDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            actualizarAsistencias();
        }
    });

    // Configurar combo de cursos
    comboCursos.addActionListener(evt -> actualizarAsistencias());
        
    }

    /**
     * Carga los cursos activos desde la base de datos en el combo de cursos.
     * Almacena la relación entre texto del curso y su ID en un mapa.
     */
   private void cargarCursos() {
    try {
        String query = 
            "SELECT id, anio, division " +
            "FROM cursos " +
            "WHERE estado = 'activo' " +  // Si tienes un campo estado
            "ORDER BY anio, division";
            
        System.out.println("Cargando todos los cursos");
        PreparedStatement ps = conect.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        
        comboCursos.removeAllItems();
        comboCursos.addItem("Seleccione un curso");
        cursosMap.clear();
        
        int cursosCount = 0;
        while (rs.next()) {
            cursosCount++;
            int id = rs.getInt("id");
            String texto = rs.getInt("anio") + "°" + rs.getInt("division");
            System.out.println("Agregando curso: " + texto + " (ID: " + id + ")");
            comboCursos.addItem(texto);
            cursosMap.put(texto, id);
        }
        
        System.out.println("Total de cursos cargados: " + cursosCount);
            
    } catch (SQLException ex) {
        System.out.println("Error al cargar cursos: " + ex.getMessage());
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error al cargar cursos: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

   /**
     * Actualiza el panel de asistencias según el curso y fecha seleccionados.
     * Crea un nuevo panel de asistencia para el preceptor y curso actual.
     */
    private void actualizarAsistencias() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        if (cursoSeleccionado == null || cursoSeleccionado.equals("Seleccione un curso")) {
            return;
        }

        // Obtener el ID del curso del map
        Integer cursoId = cursosMap.get(cursoSeleccionado);
        if (cursoId == null) {
            return;
        }

        AsistenciaPreceptorPanel panelAsistencia = new AsistenciaPreceptorPanel(
                preceptorId,
                cursoId
        );

        panelPrincipal.removeAll();
        panelAsistencia.setPreferredSize(new Dimension(panelPrincipal.getWidth(), panelPrincipal.getHeight()));
        panelPrincipal.add(panelAsistencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, panelPrincipal.getWidth(), panelPrincipal.getHeight()));
        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    // Clase auxiliar para el combo de cursos
    private static class CursoItem {

        private final int id;
        private final String texto;

        public CursoItem(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    /**
     * Actualiza las etiquetas de nombre y rol en la interfaz.
     * 
     * @param nombreCompleto Nombre completo del preceptor
     */
    public void updateLabels(String nombreCompleto) {
        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: Preceptor");
    }

    // Panel para mostrar resumen de asistencias
    private void mostrarResumen() {
        try {
            String cursoSeleccionado = (String) comboCursos.getSelectedItem();
            if (cursoSeleccionado == null || cursoSeleccionado.equals("Seleccione un curso")) {
                return;
            }

            Integer cursoId = cursosMap.get(cursoSeleccionado);
            if (cursoId == null) {
                return;
            }

            String query
                    = "SELECT "
                    + "COUNT(CASE WHEN estado = 'P' THEN 1 END) as presentes, "
                    + "COUNT(CASE WHEN estado = 'A' THEN 1 END) as ausentes, "
                    + "COUNT(CASE WHEN estado = 'T' THEN 1 END) as tarde, "
                    + "COUNT(CASE WHEN estado = 'AP' THEN 1 END) as ausente_presente "
                    + "FROM asistencia_general "
                    + "WHERE fecha = ? AND curso_id = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setDate(1, java.sql.Date.valueOf(fechaSeleccionada));
            ps.setInt(2, cursoId);

            // ... resto del código igual ...
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar resumen: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para exportar asistencias
    private void exportarAsistencias() {
        CursoItem cursoSeleccionado = (CursoItem) comboCursos.getSelectedItem();
        if (cursoSeleccionado == null || cursoSeleccionado.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un curso");
            return;
        }

        try {
            String query
                    = "SELECT u.apellido, u.nombre, ag.fecha, ag.estado "
                    + "FROM usuarios u "
                    + "JOIN asistencia_general ag ON u.id = ag.alumno_id "
                    + "WHERE ag.curso_id = ? "
                    + "AND ag.fecha BETWEEN ? AND ? "
                    + "ORDER BY u.apellido, u.nombre, ag.fecha";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoSeleccionado.getId());
            // Ejemplo: exportar el mes actual
            LocalDate inicio = fechaSeleccionada.withDayOfMonth(1);
            LocalDate fin = fechaSeleccionada.withDayOfMonth(fechaSeleccionada.lengthOfMonth());
            ps.setDate(2, java.sql.Date.valueOf(inicio));
            ps.setDate(3, java.sql.Date.valueOf(fin));

            ResultSet rs = ps.executeQuery();

            // Aquí puedes implementar la exportación a Excel o PDF
            JOptionPane.showMessageDialog(this, "Funcionalidad de exportación en desarrollo");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al exportar asistencias: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        imagenLogo = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        botpre = new javax.swing.JButton();
        botnot = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        labelNomApe = new javax.swing.JLabel();
        labelRol = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btnExprotar = new javax.swing.JButton();
        panelPrincipal = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome = new javax.swing.JLabel();
        comboCursos = new javax.swing.JComboBox<>();
        dateChooser = new com.toedter.calendar.JDateChooser();
        txtResumen = new javax.swing.JTextArea();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo et20 buena calidad.png"))); // NOI18N
        imagenLogo.setText("imagenLogo");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(imagenLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(imagenLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        botpre.setBackground(new java.awt.Color(51, 153, 255));
        botpre.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botpre.setForeground(new java.awt.Color(255, 255, 255));
        botpre.setText("ASISTENCIAS");
        botpre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botpreActionPerformed(evt);
            }
        });

        botnot.setBackground(new java.awt.Color(51, 153, 255));
        botnot.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botnot.setForeground(new java.awt.Color(255, 255, 255));
        botnot.setText("NOTAS");
        botnot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botnotActionPerformed(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-user-96.png"))); // NOI18N

        labelNomApe.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelNomApe.setForeground(new java.awt.Color(255, 255, 255));
        labelNomApe.setText("Constantino Di Nisio");

        labelRol.setBackground(new java.awt.Color(255, 255, 255));
        labelRol.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelRol.setForeground(new java.awt.Color(255, 255, 255));
        labelRol.setText("Rol: Alumno");

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/loogout48.png"))); // NOI18N
        jButton1.setText("CERRAR SESIÓN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnExprotar.setBackground(new java.awt.Color(51, 153, 255));
        btnExprotar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnExprotar.setForeground(new java.awt.Color(255, 255, 255));
        btnExprotar.setText("EXPORTAR");
        btnExprotar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExprotarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(92, 92, 92))))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelNomApe)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExprotar, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(labelRol)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelNomApe)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRol)
                .addGap(50, 50, 50)
                .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnExprotar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addGap(15, 15, 15))
        );

        panelPrincipal.setBackground(new java.awt.Color(204, 204, 204));
        panelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        panelPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/banner-et20.png"))); // NOI18N
        panelPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        panelPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        panelPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome);

        panelPrincipal.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        comboCursos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelPrincipal.add(comboCursos, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));
        panelPrincipal.add(dateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 100, -1, -1));

        txtResumen.setColumns(20);
        txtResumen.setRows(5);
        panelPrincipal.add(txtResumen, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botnotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botnotActionPerformed
        //vernotas verno = new vernotas();
        // verno.setVisible(true);
        // this.setVisible(false);
    }//GEN-LAST:event_botnotActionPerformed

    private void botpreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botpreActionPerformed
        // Limpiar el panel principal
    panelPrincipal.removeAll();
    
    // Crear panel de selección
    JPanel panelSeleccion = new JPanel();
    panelSeleccion.setBackground(new Color(204, 204, 204));
    
    // Agregar componentes al panel
    JLabel lblCurso = new JLabel("Seleccione un Curso:");
    lblCurso.setFont(new Font("Arial", Font.BOLD, 14));
    panelSeleccion.add(lblCurso);
    panelSeleccion.add(comboCursos);
    
    JLabel lblFecha = new JLabel("Fecha:");
    lblFecha.setFont(new Font("Arial", Font.BOLD, 14));
    panelSeleccion.add(lblFecha);
    panelSeleccion.add(dateChooser);
    
    // Agregar el panel al panel principal
    panelPrincipal.add(panelSeleccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, 50));
    
    // Cargar cursos en el combo
    cargarCursos();
    
    // Actualizar la vista
    panelPrincipal.revalidate();
    panelPrincipal.repaint();
    }//GEN-LAST:event_botpreActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnExprotarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExprotarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnExprotarActionPerformed

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
            java.util.logging.Logger.getLogger(preceptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(preceptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(preceptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(preceptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(preceptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Cambiar para incluir un ID de preceptor por defecto para pruebas
                new preceptor(1).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botnot;
    private javax.swing.JButton botpre;
    private javax.swing.JButton btnExprotar;
    private javax.swing.JComboBox<String> comboCursos;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JLabel fondoHome;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelNomApe;
    private javax.swing.JLabel labelRol;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JTextArea txtResumen;
    // End of variables declaration//GEN-END:variables
}
