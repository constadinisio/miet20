/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package main.java.views.users.Profesor;


import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JPanel;
import main.java.views.users.common.VentanaInicio;

/**
 * Panel para gestionar la tabla de contenidos.
 * Adaptado para funcionar con la nueva arquitectura VentanaInicio.
 * 
 * @author nico_
 */
public class tablaContenidos extends javax.swing.JPanel {
    
    private int profesorId;
    
    // Referencia a la ventana principal (reemplaza la referencia a profesor/libroTema)
    private VentanaInicio ventanaPrincipal;
    
    // Panel de libro de temas anterior (para volver)
    private libroTema panelLibroTema;

    /**
     * Constructor para el diseñador.
     */
    public tablaContenidos() {
        initComponents();
    }

    /**
     * Constructor adaptado para usar desde libroTema.
     * 
     * @param panelLibroTema Panel de libro de temas desde el que se invoca
     */
    public tablaContenidos(libroTema panelLibroTema) {
        initComponents();
        this.panelLibroTema = panelLibroTema;
    }
    
    /**
     * Constructor adaptado para la nueva arquitectura con VentanaInicio.
     * 
     * @param profesorId ID del profesor
     * @param ventanaPrincipal Referencia a la ventana principal
     */
    public tablaContenidos(int profesorId, VentanaInicio ventanaPrincipal) {
        initComponents();
        this.profesorId = profesorId;
        this.ventanaPrincipal = ventanaPrincipal;
    }

    /**
     * Acción para volver al panel de libro de temas.
     * Adaptado para usar VentanaInicio.
     */
    private void volverALibroTema() {
        try {
            if (ventanaPrincipal != null) {
                // Crear una nueva instancia de libro de temas
                libroTema libro = new libroTema(
                        ventanaPrincipal.getPanelPrincipal(), 
                        profesorId, 
                        ventanaPrincipal
                );
                
                // Obtener el panel padre
                Container parent = this.getParent();
                
                // Ocultar el panel actual
                this.setVisible(false);
                
                if (parent != null) {
                    // Eliminar el componente actual
                    parent.remove(this);
                    
                    // Agregar el nuevo panel
                    parent.add(libro, BorderLayout.CENTER);
                    
                    // Actualizar la interfaz
                    parent.revalidate();
                    parent.repaint();
                }
                
                // Hacer visible el nuevo panel
                libro.setVisible(true);
            } else if (panelLibroTema != null) {
                // Modo de compatibilidad con la implementación anterior
                Container parent = this.getParent();
                this.setVisible(false);
                
                if (parent != null) {
                    parent.remove(this);
                    parent.add(panelLibroTema, BorderLayout.CENTER);
                    parent.revalidate();
                    parent.repaint();
                }
                
                panelLibroTema.setVisible(true);
            } else {
                System.err.println("Error: no hay referencias para volver atrás");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al volver a libro de temas: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        bannerColor1 = new javax.swing.JLabel();
        bannerColor2 = new javax.swing.JLabel();
        btnVolver = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableContenido = new javax.swing.JTable();
        btnModificar = new javax.swing.JButton();
        btnAñadir = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblIdContenido = new javax.swing.JTextField();
        lblFechaContenido = new javax.swing.JTextField();
        lblContenidoVisto = new javax.swing.JTextField();
        lblObservaciones = new javax.swing.JTextField();

        bannerColor1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N

        bannerColor2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N

        btnVolver.setBackground(new java.awt.Color(51, 153, 255));
        btnVolver.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(255, 255, 255));
        btnVolver.setText("VOLVER");
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });

        tableContenido.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tableContenido);

        btnModificar.setBackground(new java.awt.Color(51, 153, 255));
        btnModificar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnModificar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificar.setText("EDITAR CONTENIDO");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnAñadir.setBackground(new java.awt.Color(51, 153, 255));
        btnAñadir.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnAñadir.setForeground(new java.awt.Color(255, 255, 255));
        btnAñadir.setText("AÑADIR CONTENIDO");
        btnAñadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAñadirActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(51, 153, 255));
        btnEliminar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("ELIMINAR CONTENIDO");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("ID Contenido");

        jLabel2.setText("Fecha de Contenido");

        jLabel3.setText("Contenido Visto");

        jLabel4.setText("Observaciones");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblIdContenido)
                    .addComponent(lblFechaContenido)
                    .addComponent(lblContenidoVisto)
                    .addComponent(lblObservaciones, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                .addContainerGap(94, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblIdContenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblFechaContenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblContenidoVisto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblObservaciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(70, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bannerColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(bannerColor2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAñadir, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnEliminar))
                            .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(130, 130, 130)
                        .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(bannerColor1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAñadir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(bannerColor2))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 758, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 528, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        volverALibroTema();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        //  modificarContenido();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAñadirActionPerformed
        //  añadirContenido();
    }//GEN-LAST:event_btnAñadirActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        // eliminarContenido();
    }//GEN-LAST:event_btnEliminarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerColor1;
    private javax.swing.JLabel bannerColor2;
    private javax.swing.JButton btnAñadir;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lblContenidoVisto;
    private javax.swing.JTextField lblFechaContenido;
    private javax.swing.JTextField lblIdContenido;
    private javax.swing.JTextField lblObservaciones;
    private javax.swing.JTable tableContenido;
    // End of variables declaration//GEN-END:variables
}
