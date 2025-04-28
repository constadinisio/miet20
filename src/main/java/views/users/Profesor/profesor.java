package main.java.views.users.Profesor;

import java.sql.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.JOptionPane;
import main.java.database.Conexion;
import main.java.views.users.Profesor.AsistenciaProfesorPanel;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import main.java.utils.MenuBarManager;
import main.java.utils.ResourceManager;
import main.java.utils.ResponsiveImageLabel;
import main.java.utils.RolesPanelManager;
import main.java.utils.uiUtils;

public class profesor extends javax.swing.JFrame {

    // Ahora solo declaramos la variable de conexión
    private Connection conect;
    private int profesorId;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private JLabel fondoHomeOriginal;  // Para guardar el fondo original
    private libroTema temario;

    // En profesor.java, en el constructor
    public profesor(int profesorId) {
        this.profesorId = profesorId;
        initComponents();
        uiUtils.configurarVentana(this);
        probar_conexion();
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Configurar layout principal para que sea responsivo
        getContentPane().setLayout(new BorderLayout());

        // Panel izquierdo (menú) - le damos un tamaño fijo
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(270, getHeight()));
        leftPanel.add(jPanel2, BorderLayout.NORTH); // Logo
        leftPanel.add(jPanel4, BorderLayout.CENTER); // Menú y botones

        // Configurar el panel principal para que sea scrolleable
        JScrollPane scrollPane = new JScrollPane(panelPrincipal);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Añadir los paneles al contenedor principal
        getContentPane().add(leftPanel, BorderLayout.WEST);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Otras configuraciones
        imagenLogo = new ResponsiveImageLabel(ResourceManager.getImagePath("logo_et20_max.png"));
        fondoHome = new ResponsiveImageLabel(ResourceManager.getImagePath("5c994f25d361a_1200.jpg"));

        // Guardar referencia al fondo original
        fondoHomeOriginal = fondoHome;

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Configurar un tamaño mínimo para la ventana
        setMinimumSize(new Dimension(1024, 768));
        // En el constructor de profesor, después de inicializar los componentes
        jPanel2.setVisible(true); // Asegúrate de que sea visible
        jPanel2.setPreferredSize(new Dimension(270, 250)); // Establece un tamaño preferido explícito
        imagenLogo.setVisible(true); // Asegúrate de que la imagen sea visible

// Si estás usando ResponsiveImageLabel, asegúrate de que carga correctamente
        if (imagenLogo instanceof ResponsiveImageLabel) {
            ((ResponsiveImageLabel) imagenLogo).refreshImage();
        }

        jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));
        Dimension buttonSize = new Dimension(160, 40);
        botnot.setPreferredSize(buttonSize);
        botpre.setPreferredSize(buttonSize);
        botpre1.setPreferredSize(buttonSize);

        // Asegurar que los componentes del panel4 se alineen correctamente
        labelFotoPerfil.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelNomApe.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        botnot.setAlignmentX(Component.CENTER_ALIGNMENT);
        botpre.setAlignmentX(Component.CENTER_ALIGNMENT);
        botpre1.setAlignmentX(Component.CENTER_ALIGNMENT);
        jButton1.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Añadir componentes con espaciado apropiado
        jPanel4.add(Box.createVerticalGlue());
        jPanel4.add(labelFotoPerfil);
        jPanel4.add(Box.createRigidArea(new Dimension(0, 10))); // Espacio después de la foto
        jPanel4.add(labelNomApe);
        jPanel4.add(labelRol);
        jPanel4.add(Box.createRigidArea(new Dimension(0, 30))); // Más espacio antes de los botones
        jPanel4.add(botnot);
        jPanel4.add(Box.createRigidArea(new Dimension(0, 15))); // Espacio entre botones
        jPanel4.add(botpre);
        jPanel4.add(Box.createRigidArea(new Dimension(0, 15))); // Espacio entre botones
        jPanel4.add(botpre1);
        jPanel4.add(Box.createVerticalGlue());
        jPanel4.add(jButton1);
        jPanel4.add(Box.createRigidArea(new Dimension(0, 10)));

// Añadir un borde para asegurar un margen uniforme
        jPanel4.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        new MenuBarManager(profesorId, this);

    }

    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    // Método para restaurar la vista original del panel principal
    public void restaurarVistaPrincipal() {
        panelPrincipal.removeAll();
        panelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        // Restaurar los componentes originales
        panelPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        panelPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));
        panelPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));
        panelPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        // Restaurar el fondo
        jScrollPane1.setViewportView(fondoHomeOriginal);
        panelPrincipal.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    // Clases auxiliares para el árbol
    private static class CursoNode {

        private final int id;
        private final String texto;

        public CursoNode(int id, String texto) {
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

    private static class MateriaNode {

        private final int id;
        private final String nombre;

        public MateriaNode(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public void updateLabels(String nombreCompleto) {
        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: Profesor");
    }

    private void abrirLibroDeTemas() {
        // Crear una nueva instancia utilizando el nuevo constructor
        libroTema libro = new libroTema(panelPrincipal, profesorId, this);

        // Limpiar el panel principal y agregar el nuevo componente
        panelPrincipal.removeAll();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.add(libro, BorderLayout.CENTER);
        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        imagenLogo = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        botpre = new javax.swing.JButton();
        botnot = new javax.swing.JButton();
        labelFotoPerfil = new javax.swing.JLabel();
        labelNomApe = new javax.swing.JLabel();
        labelRol = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        botpre1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelPrincipal.setBackground(new java.awt.Color(204, 204, 204));
        panelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        panelPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        panelPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        panelPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        panelPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome);

        panelPrincipal.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/logo_et20_min.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(imagenLogo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(55, Short.MAX_VALUE)
                .addComponent(imagenLogo)
                .addGap(46, 46, 46))
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

        labelFotoPerfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/icons8-user-96.png"))); // NOI18N

        labelNomApe.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelNomApe.setForeground(new java.awt.Color(255, 255, 255));

        labelRol.setBackground(new java.awt.Color(255, 255, 255));
        labelRol.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        labelRol.setForeground(new java.awt.Color(255, 255, 255));

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/loogout48.png"))); // NOI18N
        jButton1.setText("CERRAR SESIÓN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        botpre1.setBackground(new java.awt.Color(51, 153, 255));
        botpre1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        botpre1.setForeground(new java.awt.Color(255, 255, 255));
        botpre1.setText("LIBRO DE TEMAS");
        botpre1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botpre1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(labelNomApe)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelFotoPerfil)
                                .addGap(76, 76, 76))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(179, 179, 179)
                                .addComponent(labelRol)
                                .addGap(75, 75, 75))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botpre1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(48, 48, 48))))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jButton1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(labelNomApe)
                        .addGap(129, 129, 129))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(labelFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botnot, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(botpre, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botpre1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelRol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

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

    private void botpreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botpreActionPerformed
        try {
        // Panel superior para selección
        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
        JComboBox<String> comboMaterias = new JComboBox<>();
        Map<String, int[]> materiaIds = new HashMap<>();

        String query
                = "SELECT DISTINCT c.id as curso_id, CONCAT(c.anio, '°', c.division) as curso, "
                + "m.id as materia_id, m.nombre as materia "
                + "FROM cursos c "
                + "JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id "
                + "JOIN materias m ON pcm.materia_id = m.id "
                + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                + "ORDER BY c.anio, c.division, m.nombre";

        PreparedStatement ps = conect.prepareStatement(query);
        ps.setInt(1, profesorId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String item = rs.getString("curso") + " - " + rs.getString("materia");
            comboMaterias.addItem(item);
            int cursoId = rs.getInt("curso_id");
            int materiaId = rs.getInt("materia_id");
            materiaIds.put(item, new int[]{cursoId, materiaId});
            
            // Debug: Imprimir cada entrada agregada
            System.out.println("Agregando: " + item + ", Curso ID: " + cursoId + ", Materia ID: " + materiaId);
        }

        // Debug: Verificar el mapa completo
        System.out.println("Mapa de materias e IDs:");
        for (Map.Entry<String, int[]> entry : materiaIds.entrySet()) {
            System.out.println("Clave: " + entry.getKey() + ", Valores: [" + entry.getValue()[0] + ", " + entry.getValue()[1] + "]");
        }

        comboMaterias.setPreferredSize(new Dimension(300, 30));
        panelSeleccion.add(lblSeleccion);
        panelSeleccion.add(comboMaterias);

        // Listener para el combo
        comboMaterias.addActionListener(e -> {
            String seleccion = (String) comboMaterias.getSelectedItem();
            System.out.println("Selección: " + seleccion); // Debug

            if (seleccion != null) {
                try {
                    int[] ids = materiaIds.get(seleccion);
                    
                    if (ids == null) {
                        JOptionPane.showMessageDialog(this, "No se encontraron IDs asociados a la selección: " + seleccion);
                        return;
                    }
                    
                    System.out.println("CursoId: " + ids[0] + ", MateriaId: " + ids[1]); // Debug

                    AsistenciaProfesorPanel panelAsistencia = new AsistenciaProfesorPanel(
                            profesorId,
                            ids[0], // cursoId
                            ids[1]  // materiaId
                    );

                    panelPrincipal.removeAll();
                    panelPrincipal.setLayout(new BorderLayout()); // Importante: establecer el layout
                    panelPrincipal.add(panelAsistencia, BorderLayout.CENTER);
                    panelPrincipal.revalidate();
                    panelPrincipal.repaint();
                } catch (Exception ex) {
                    System.out.println("Error completo: "); // Debug
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "Error al cargar panel: " + ex.getMessage() 
                        + "\nDetalles: " + ex.getClass().getName()
                        + "\nSelección: " + seleccion);
                }
            }
        });

        panelPrincipal.removeAll();
        main.java.utils.PanelUtils.addPanelToContainer(panelPrincipal, panelSeleccion, BorderLayout.NORTH);
        panelPrincipal.revalidate();
        panelPrincipal.repaint();

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error al cargar los cursos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_botpreActionPerformed

    private void botnotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botnotActionPerformed
        try {
            // Panel superior para selección
            JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lblSeleccion = new JLabel("Seleccione Curso y Materia:");
            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, int[]> materiaIds = new HashMap<>();

            String query
                    = "SELECT DISTINCT c.id as curso_id, CONCAT(c.anio, '°', c.division) as curso, "
                    + "m.id as materia_id, m.nombre as materia "
                    + "FROM cursos c "
                    + "JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id "
                    + "JOIN materias m ON pcm.materia_id = m.id "
                    + "WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' "
                    + "ORDER BY c.anio, c.division, m.nombre";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("curso") + " - " + rs.getString("materia");
                comboMaterias.addItem(item);
                materiaIds.put(item, new int[]{rs.getInt("curso_id"), rs.getInt("materia_id")});
            }

            // Agregar radio buttons para elegir el tipo de panel de notas
            JRadioButton rbTrabajos = new JRadioButton("Trabajos y Actividades", true);
            JRadioButton rbBimestral = new JRadioButton("Notas Bimestrales", false);
            ButtonGroup grupoOpciones = new ButtonGroup();
            grupoOpciones.add(rbTrabajos);
            grupoOpciones.add(rbBimestral);

            panelSeleccion.add(lblSeleccion);
            panelSeleccion.add(comboMaterias);
            panelSeleccion.add(rbTrabajos);
            panelSeleccion.add(rbBimestral);

            // Botón para cargar el panel seleccionado
            JButton btnCargar = new JButton("Cargar");
            panelSeleccion.add(btnCargar);

            // Listener para el botón
            btnCargar.addActionListener(e -> {
                String seleccion = (String) comboMaterias.getSelectedItem();
                if (seleccion != null) {
                    try {
                        int[] ids = materiaIds.get(seleccion);
                        int cursoId = ids[0];
                        int materiaId = ids[1];

                        // Cargar el panel correspondiente según la selección
                        if (rbTrabajos.isSelected()) {
                            // Crear el panel de notas
                            NotasProfesorPanel panelNotas = new NotasProfesorPanel(profesorId, cursoId, materiaId);

                            // Usar la utilidad para hacerlo responsivo
                            main.java.utils.PanelUtils.addPanelToContainer(panelPrincipal, panelNotas, BorderLayout.CENTER);
                        } else {
                            NotasBimestralesPanel panelBimestral = new NotasBimestralesPanel(
                                    profesorId, cursoId, materiaId
                            );

                            // Usar la utilidad para hacerlo responsivo
                            main.java.utils.PanelUtils.addPanelToContainer(panelPrincipal, panelBimestral, BorderLayout.CENTER);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error al cargar panel: " + ex.getMessage());
                    }
                }
            });

            // Usar la utilidad para agregar el panel de selección
            main.java.utils.PanelUtils.addPanelToContainer(panelPrincipal, panelSeleccion, BorderLayout.NORTH);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los cursos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_botnotActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void botpre1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botpre1ActionPerformed
        abrirLibroDeTemas();
    }//GEN-LAST:event_botpre1ActionPerformed

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
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(profesor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Asumiendo que el ID 1 es un profesor válido para pruebas
                new profesor(1).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botnot;
    private javax.swing.JButton botpre;
    private javax.swing.JButton botpre1;
    private javax.swing.JLabel fondoHome;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelFotoPerfil;
    private javax.swing.JLabel labelNomApe;
    private javax.swing.JLabel labelRol;
    private javax.swing.JPanel panelPrincipal;
    // End of variables declaration//GEN-END:variables
}
