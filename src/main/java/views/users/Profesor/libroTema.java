/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package main.java.views.users.Profesor;


import java.awt.BorderLayout;
import java.awt.Container;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import main.java.database.Conexion;
import main.java.views.users.common.VentanaInicio;

/**
 * Panel para gestionar el libro de temas de un profesor.
 * Adaptado para funcionar con la nueva arquitectura VentanaInicio.
 *
 * @author nico_
 */
public class libroTema extends javax.swing.JPanel {

    private Connection conect;
    private int profesorId;
    
    // Referencia a la ventana principal (reemplaza la referencia a profesor)
    private VentanaInicio ventanaPrincipal;

    /**
     * Constructor simple para usar desde el diseñador
     */
    public libroTema() {
        initComponents();
    }

    /**
     * Constructor para uso desde VentanaInicio
     * 
     * @param panelPrincipal Panel donde se mostrará este componente
     * @param profesorId ID del profesor
     * @param ventanaPrincipal Referencia a la ventana principal
     */
    public libroTema(JPanel panelPrincipal, int profesorId, VentanaInicio ventanaPrincipal) {
        initComponents();
        this.ventanaPrincipal = ventanaPrincipal;
        this.profesorId = profesorId;
        probar_conexion();
        
        try {
            // Intenta cargar las imágenes si existen y el componente está inicializado
            if (bannerColor1 != null && bannerColor2 != null) {
                java.io.File file = new java.io.File("src/main/resources/images/banner-et20.png");
                if (file.exists()) {
                    rsscalelabel.RSScaleLabel.setScaleLabel(bannerColor1, file.getAbsolutePath());
                    rsscalelabel.RSScaleLabel.setScaleLabel(bannerColor2, file.getAbsolutePath());
                } else {
                    System.err.println("Archivo de imagen no encontrado: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar las imágenes: " + e.getMessage());
        }
    }

    /**
     * Configura el ID del profesor.
     * 
     * @param profesorId ID del profesor
     */
    public void setProfesorId(int profesorId) {
        this.profesorId = profesorId;
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
     * Registra la firma de asistencia del profesor según el horario actual.
     */
    public void firmarAsistencia() {
        try {
            LocalDate fechaActual = LocalDate.now();
            LocalTime horaActual = LocalTime.now();
            String diaSemana = fechaActual.getDayOfWeek().toString().toLowerCase();

            String consulta = "SELECT curso_id, materia_id FROM horarios_materia WHERE profesor_id = ? AND dia_semana = ? AND ? BETWEEN hora_inicio AND hora_fin";
            PreparedStatement ps = conect.prepareStatement(consulta);
            ps.setInt(1, profesorId);
            ps.setString(2, diaSemana);
            ps.setTime(3, Time.valueOf(horaActual));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cursoId = rs.getInt("curso_id");
                int materiaId = rs.getInt("materia_id");

                String insercion = "INSERT INTO firmas_asistencia (profesor_id, curso_id, materia_id, fecha, hora_firma) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psInsert = conect.prepareStatement(insercion);
                psInsert.setInt(1, profesorId);
                psInsert.setInt(2, cursoId);
                psInsert.setInt(3, materiaId);
                psInsert.setDate(4, Date.valueOf(fechaActual));
                psInsert.setTime(5, Time.valueOf(horaActual));

                psInsert.executeUpdate();
                JOptionPane.showMessageDialog(null, "Firma registrada exitosamente.");
            } else {
                JOptionPane.showMessageDialog(null, "No tienes clase en este horario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al registrar la firma.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Vuelve a la vista principal.
     * Adaptado para usar VentanaInicio en lugar de profesor.
     */
    private void accionVolver() {
        if (ventanaPrincipal != null) {
            // Obtener el contenedor padre (panel principal)
            Container parent = this.getParent();
            if (parent != null) {
                // Eliminar este panel
                parent.remove(this);
                
                // Restaurar la vista principal de VentanaInicio
                ventanaPrincipal.restaurarVistaPrincipal();
                
                // Actualizar la interfaz
                parent.revalidate();
                parent.repaint();
            }
        } else {
            System.err.println("Error: ventanaPrincipal es null");
            
            // Alternativa de emergencia: simplemente ocultar este panel
            this.setVisible(false);
        }
    }
    
    /**
     * Abre el panel de contenidos.
     * Adaptado para usar VentanaInicio en lugar de profesor.
     */
    private void abrirContenidos() {
        try {
            // Verificar la conexión a la base de datos
            Conexion.getInstancia().verificarConexion();
            
            // Crear una nueva instancia de tablaContenidos
            // 1. Usando la nueva arquitectura:
            tablaContenidos tablaCont = new tablaContenidos(profesorId, ventanaPrincipal);
            
            // Obtener el panel padre (el que contiene a libroTema)
            Container parent = this.getParent();
            
            // Ocultar el panel actual
            this.setVisible(false);
            
            // Si el padre es un contenedor con layout, podemos hacer esto:
            if (parent != null) {
                // Eliminar el componente actual (libroTema)
                parent.remove(this);
                
                // Agregar el nuevo panel al contenedor padre
                parent.add(tablaCont, BorderLayout.CENTER);
                
                // Actualizar la interfaz
                parent.revalidate();
                parent.repaint();
            }
            
            // Hacer visible el nuevo panel
            tablaCont.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Error al abrir contenidos: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bannerColor1 = new javax.swing.JLabel();
        bannerColor2 = new javax.swing.JLabel();
        btnVolver = new javax.swing.JButton();
        btnFirmar = new javax.swing.JButton();
        btnContenidos = new javax.swing.JButton();
        panelInfo = new javax.swing.JPanel();

        btnVolver.setBackground(new java.awt.Color(51, 153, 255));
        btnVolver.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(255, 255, 255));
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });

        btnFirmar.setBackground(new java.awt.Color(51, 153, 255));
        btnFirmar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnFirmar.setForeground(new java.awt.Color(255, 255, 255));
        btnFirmar.setText("FIRMAR");
        btnFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirmarActionPerformed(evt);
            }
        });

        btnContenidos.setBackground(new java.awt.Color(51, 153, 255));
        btnContenidos.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnContenidos.setForeground(new java.awt.Color(255, 255, 255));
        btnContenidos.setText("CONTENIDOS");
        btnContenidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContenidosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 545, Short.MAX_VALUE)
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 435, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bannerColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(bannerColor2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnContenidos, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bannerColor1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(137, 137, 137)
                        .addComponent(btnFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnContenidos, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addComponent(bannerColor2))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        accionVolver();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirmarActionPerformed
        firmarAsistencia();
    }//GEN-LAST:event_btnFirmarActionPerformed

    private void btnContenidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContenidosActionPerformed
        abrirContenidos();
    }//GEN-LAST:event_btnContenidosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerColor1;
    private javax.swing.JLabel bannerColor2;
    private javax.swing.JButton btnContenidos;
    private javax.swing.JButton btnFirmar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JPanel panelInfo;
    // End of variables declaration//GEN-END:variables
}
