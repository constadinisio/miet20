package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import main.java.database.Conexion;
import main.java.utils.MenuBarManager;
import main.java.utils.ResourceManager;
import main.java.utils.ResponsiveImageUtils;
import main.java.utils.ResponsiveUtils;
import main.java.utils.uiUtils;
import main.java.views.login.LoginForm;

/**
 * Ventana principal unificada para todos los roles del sistema.
 * Utiliza CardLayout para intercambiar entre vista principal y paneles específicos.
 */
public class VentanaInicio extends javax.swing.JFrame {

    // Conexión a base de datos
    protected Connection conect;

    // Identificador y rol del usuario
    protected int userId;
    protected int userRol;

    // Panel de menú lateral que cambiará según el rol
    protected JPanel panelBotones;

    // Gestor de paneles según el rol
    private RolPanelManager rolPanelManager;

    // Componentes UI
    private JLabel labelFotoPerfil;
    private JLabel labelNomApe;
    private JLabel labelRol;
    private JLabel labelCursoDiv; // Para alumnos

    // Gestión de contenido principal
    private CardLayout cardLayout;
    private JPanel panelContenido;
    private JPanel panelHome;
    private JPanel panelDinamico;

    // Constantes para CardLayout
    private static final String VISTA_HOME = "HOME";
    private static final String VISTA_DINAMICA = "DINAMICA";

    // Referencia al panel de fondo original
    private JLabel fondoHomeOriginal;

    /**
     * Constructor principal de la ventana unificada.
     *
     * @param userId ID del usuario
     * @param rolId Rol del usuario
     */
    public VentanaInicio(int userId, int rolId) {
        this.userId = userId;
        this.userRol = rolId;

        initComponents();
        configurarPanelPrincipal();
        uiUtils.configurarVentana(this);
        probar_conexion();

        // Escalar imágenes
        rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, ResourceManager.getImagePath("logo_et20_max.png"));
        rsscalelabel.RSScaleLabel.setScaleLabel(fondoHome, ResourceManager.getImagePath("5c994f25d361a_1200.jpg"));

        // Guardar referencia al fondo original
        fondoHomeOriginal = fondoHome;

        // Inicializar el gestor de menú
        new MenuBarManager(userId, this);

        // Inicializar el gestor de paneles según rol
        try {
    // Verificar dependencias primero
    RolPanelManagerFactory.inicializar();
    
    // Crear el gestor de paneles
    this.rolPanelManager = RolPanelManagerFactory.createManager(this, userId, userRol);
    
    if (this.rolPanelManager == null) {
        throw new RuntimeException("No se pudo crear el PanelManager para el rol: " + userRol);
    }
    
    System.out.println("RolPanelManager creado exitosamente para rol: " + userRol);
    
} catch (Exception e) {
    System.err.println("Error al crear RolPanelManager: " + e.getMessage());
    e.printStackTrace();
    
    // Mostrar error al usuario pero continuar con un manager básico
    JOptionPane.showMessageDialog(this,
            "Error al cargar la interfaz específica del rol.\n" +
            "La aplicación funcionará en modo básico.\n" +
            "Error: " + e.getMessage(),
            "Advertencia",
            JOptionPane.WARNING_MESSAGE);
    
    // Crear un manager de emergencia
    this.rolPanelManager = new AdminPanelManager(this, userId);
}

        // Configurar el panel de botones
        configurePanelBotones();
        hacerLogoClickeable();
        aplicarResponsive();
    }

    /**
     * Configura el panel principal para usar CardLayout.
     * Esto permite intercambiar fácilmente entre la vista home y los paneles dinámicos.
     */
    private void configurarPanelPrincipal() {
        // Crear el CardLayout y el panel contenedor
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        // Crear el panel home (vista principal original)
        panelHome = new JPanel();
        panelHome.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        panelHome.setBackground(new java.awt.Color(204, 204, 204));

        // Mover todos los componentes originales al panel home
        configurarPanelHome();

        // Crear el panel dinámico (para contenido específico de roles)
        panelDinamico = new JPanel(new BorderLayout());
        panelDinamico.setBackground(new java.awt.Color(240, 240, 240));

        // Añadir ambos paneles al CardLayout
        panelContenido.add(panelHome, VISTA_HOME);
        panelContenido.add(panelDinamico, VISTA_DINAMICA);

        // Reemplazar jPanel1 con panelContenido
        getContentPane().remove(jPanel1);
        
        // Recrear el layout principal
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        // Mostrar inicialmente la vista home
        cardLayout.show(panelContenido, VISTA_HOME);
    }

    /**
     * Configura el panel home con los componentes originales.
     */
    private void configurarPanelHome() {
        // Añadir todos los componentes originales al panel home
        panelHome.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        panelHome.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));
        panelHome.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));
        panelHome.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));
        panelHome.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));
    }

    /**
     * Muestra un panel específico en el área de contenido dinámico.
     *
     * @param panel Panel a mostrar
     * @param titulo Título del panel (opcional)
     */
    public void mostrarPanel(JPanel panel, String titulo) {
        try {
            System.out.println("Mostrando panel: " + (titulo != null ? titulo : "Sin título"));
            
            // Limpiar el panel dinámico
            panelDinamico.removeAll();
            
            // Crear panel de navegación
            JPanel navPanel = crearPanelNavegacion(titulo);
            panelDinamico.add(navPanel, BorderLayout.NORTH);
            
            // Añadir el panel principal
            if (panel != null) {
                // Configurar el panel para que use todo el espacio disponible
                panel.setPreferredSize(new Dimension(750, 600));
                
                // Si el panel necesita scroll, añadirlo
                if (panel.getPreferredSize().height > 600) {
                    JScrollPane scrollPane = new JScrollPane(panel);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    panelDinamico.add(scrollPane, BorderLayout.CENTER);
                } else {
                    panelDinamico.add(panel, BorderLayout.CENTER);
                }
            } else {
                // Panel de error si no se pudo cargar
                JLabel lblError = new JLabel("Error: No se pudo cargar el panel", JLabel.CENTER);
                lblError.setForeground(java.awt.Color.RED);
                panelDinamico.add(lblError, BorderLayout.CENTER);
            }
            
            // Cambiar a la vista dinámica
            cardLayout.show(panelContenido, VISTA_DINAMICA);
            
            // Forzar actualización
            panelDinamico.revalidate();
            panelDinamico.repaint();
            panelContenido.revalidate();
            panelContenido.repaint();
            
            System.out.println("Panel mostrado correctamente");
            
        } catch (Exception ex) {
            System.err.println("Error al mostrar panel: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar el panel: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Crea un panel de navegación con botón de volver y título.
     *
     * @param titulo Título a mostrar
     * @return Panel de navegación configurado
     */
    private JPanel crearPanelNavegacion(String titulo) {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new java.awt.Color(51, 153, 255));
        navPanel.setPreferredSize(new Dimension(0, 50));

        // Botón volver
        JButton btnVolver = new JButton("← Volver al Inicio");
        btnVolver.setBackground(new java.awt.Color(40, 120, 200));
        btnVolver.setForeground(java.awt.Color.WHITE);
        btnVolver.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> restaurarVistaPrincipal());

        // Título
        JLabel lblTitulo = new JLabel(titulo != null ? titulo : "Panel de Trabajo");
        lblTitulo.setForeground(java.awt.Color.WHITE);
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);

        navPanel.add(btnVolver, BorderLayout.WEST);
        navPanel.add(lblTitulo, BorderLayout.CENTER);

        return navPanel;
    }

    /**
     * Carga de manera forzada la foto de perfil, independientemente del estado
     * previo. Este método utiliza un enfoque directo que garantiza la
     * actualización de la imagen.
     *
     * @param fotoUrl URL de la imagen de perfil
     * @return true si la carga fue exitosa, false en caso contrario
     */
    public boolean cargarFotoPerfilForced(String fotoUrl) {
        try {
            // 1. Asegurarse de que el componente existe
            if (labelFotoPerfil == null) {
                labelFotoPerfil = new JLabel();
                labelFotoPerfil.setHorizontalAlignment(JLabel.CENTER);
                labelFotoPerfil.setAlignmentX(CENTER_ALIGNMENT);
                System.out.println("Creada nueva instancia de labelFotoPerfil");
            }

            // 2. Limpiar cualquier icono previo para forzar actualización
            labelFotoPerfil.setIcon(null);

            // 3. Cargar la nueva imagen
            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                System.out.println("Intentando cargar imagen desde URL: " + fotoUrl);

                // Usar un método alternativo más directo para cargar la imagen
                ImageIcon icon = loadImageDirectly(fotoUrl);

                if (icon != null && icon.getIconWidth() > 0) {
                    // La imagen se cargó correctamente
                    labelFotoPerfil.setIcon(icon);
                    System.out.println("Imagen cargada exitosamente: " + icon.getIconWidth() + "x" + icon.getIconHeight());

                    // 4. Forzar actualización visual
                    labelFotoPerfil.revalidate();
                    labelFotoPerfil.repaint();

                    if (panelBotones != null) {
                        // Asegurarse de que la etiqueta está en el panel
                        boolean encontrada = false;
                        for (Component c : panelBotones.getComponents()) {
                            if (c == labelFotoPerfil) {
                                encontrada = true;
                                break;
                            }
                        }

                        if (!encontrada) {
                            // Si no está en el panel, agregarla al principio
                            panelBotones.add(labelFotoPerfil, 0);
                            System.out.println("Etiqueta añadida al panel");
                        }

                        panelBotones.revalidate();
                        panelBotones.repaint();
                    }

                    return true;
                } else {
                    System.err.println("La imagen se cargó como null o con dimensiones inválidas");
                }
            }

            // 5. Si llegamos aquí, usar imagen por defecto
            System.out.println("Usando imagen por defecto");
            ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/main/resources/images/icons8-user-96.png"));
            if (defaultIcon != null && defaultIcon.getIconWidth() > 0) {
                // Redimensionar al tamaño deseado
                Image img = defaultIcon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                labelFotoPerfil.setIcon(new ImageIcon(img));

                // Forzar actualización visual
                labelFotoPerfil.revalidate();
                labelFotoPerfil.repaint();
                return true;
            } else {
                System.err.println("No se pudo cargar la imagen por defecto");
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error crítico al cargar foto de perfil: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carga una imagen directamente usando un enfoque alternativo.
     *
     * @param imageUrl URL de la imagen
     * @return ImageIcon con la imagen cargada, o null si falló
     */
    private ImageIcon loadImageDirectly(String imageUrl) {
        try {
            // Método 1: Usar ImageIO
            URL url = new URL(imageUrl);
            Image img = ImageIO.read(url);

            if (img != null) {
                // Redimensionar a 96x96
                Image scaledImg = img.getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            // Método 2: Si el primero falla, intentar con otro enfoque
            ImageIcon icon = new ImageIcon(url);
            if (icon.getIconWidth() > 0) {
                // Redimensionar a 96x96
                Image scaledImg = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            // Método 3: Cargar localmente desde una ruta en el sistema de archivos
            if (imageUrl.startsWith("file:")) {
                String filePath = imageUrl.substring(5);
                icon = new ImageIcon(filePath);
                if (icon.getIconWidth() > 0) {
                    Image scaledImg = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImg);
                }
            }

            System.err.println("No se pudo cargar la imagen usando ningún método");
            return null;
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Establece un nuevo gestor de paneles para este rol.
     *
     * @param rolPanelManager Nuevo gestor de paneles
     */
    public void setRolPanelManager(RolPanelManager rolPanelManager) {
        this.rolPanelManager = rolPanelManager;
    }

    /**
     * Reconfigura el panel de botones con los del rol actual. Este método se
     * expone públicamente para permitir actualizaciones cuando cambia el rol
     * del usuario.
     */
    public void configurePanelBotones() {
        // Crear panel para los botones dinámicos
        panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(jPanel4.getBackground());

        // Inicializar etiquetas si son nulas
        if (labelFotoPerfil == null) {
            labelFotoPerfil = new JLabel(new ImageIcon(getClass().getResource("/main/resources/images/icons8-user-96.png")));
            labelFotoPerfil.setHorizontalAlignment(JLabel.CENTER);
            labelFotoPerfil.setAlignmentX(CENTER_ALIGNMENT);
        }

        if (labelNomApe == null) {
            labelNomApe = new JLabel("Usuario");
            labelNomApe.setFont(new java.awt.Font("Arial", 1, 14));
            labelNomApe.setForeground(new java.awt.Color(255, 255, 255));
            labelNomApe.setHorizontalAlignment(JLabel.CENTER);
            labelNomApe.setAlignmentX(CENTER_ALIGNMENT);
        }

        if (labelRol == null) {
            labelRol = new JLabel("Rol: -");
            labelRol.setFont(new java.awt.Font("Arial", 1, 14));
            labelRol.setForeground(new java.awt.Color(255, 255, 255));
            labelRol.setHorizontalAlignment(JLabel.CENTER);
            labelRol.setAlignmentX(CENTER_ALIGNMENT);
        }

        if (userRol == 4 && labelCursoDiv == null) {
            labelCursoDiv = new JLabel("Curso: -");
            labelCursoDiv.setFont(new java.awt.Font("Arial", 1, 14));
            labelCursoDiv.setForeground(new java.awt.Color(255, 255, 255));
            labelCursoDiv.setHorizontalAlignment(JLabel.CENTER);
            labelCursoDiv.setAlignmentX(CENTER_ALIGNMENT);
        }

        // Añadir espacio superior
        panelBotones.add(Box.createVerticalStrut(20));

        // Añadir las etiquetas al panel
        panelBotones.add(labelFotoPerfil);
        panelBotones.add(Box.createVerticalStrut(10));
        panelBotones.add(labelNomApe);
        panelBotones.add(Box.createVerticalStrut(5));
        panelBotones.add(labelRol);

        if (userRol == 4 && labelCursoDiv != null) {
            panelBotones.add(Box.createVerticalStrut(5));
            panelBotones.add(labelCursoDiv);
        }

        panelBotones.add(Box.createVerticalStrut(20));

        // Añadir los botones específicos del rol
        JComponent[] buttons = rolPanelManager.createButtons();

        // Centro todos los botones
        for (JComponent btn : buttons) {
            btn.setAlignmentX(CENTER_ALIGNMENT);
            // Añadir espacio antes de cada botón
            panelBotones.add(Box.createVerticalStrut(15));
            panelBotones.add(btn);
        }

        // Añadir espacio antes del botón de cerrar sesión
        panelBotones.add(Box.createVerticalGlue()); // Espacio flexible
        panelBotones.add(Box.createVerticalStrut(20));

        // Botón de cerrar sesión (común para todos los roles)
        JButton btnCerrarSesion = new JButton("CERRAR SESIÓN");
        btnCerrarSesion.setBackground(jPanel4.getBackground());
        btnCerrarSesion.setFont(new java.awt.Font("Arial", 1, 18));
        btnCerrarSesion.setForeground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setIcon(new ImageIcon(getClass().getResource("/main/resources/images/loogout48.png")));
        btnCerrarSesion.setAlignmentX(CENTER_ALIGNMENT);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelBotones.add(btnCerrarSesion);
        panelBotones.add(Box.createVerticalStrut(15));

        // Reemplazar el panel actual con el nuevo
        jPanel4.removeAll();
        jPanel4.setLayout(new BorderLayout());
        jPanel4.add(panelBotones, BorderLayout.CENTER);

        // Actualizar el panel
        jPanel4.revalidate();
        jPanel4.repaint();
    }

    /**
     * Inicializa las etiquetas de información de usuario. Este método debe
     * llamarse antes de usar las etiquetas.
     */
    private void inicializarEtiquetas() {
        // Foto de perfil
        if (labelFotoPerfil == null) {
            labelFotoPerfil = new JLabel();
            labelFotoPerfil.setHorizontalAlignment(JLabel.CENTER);
            labelFotoPerfil.setAlignmentX(CENTER_ALIGNMENT);
            // No configuramos aún el icono, se hará en cargarFotoPerfilForced
        }

        // Nombre y apellido
        if (labelNomApe == null) {
            labelNomApe = new JLabel("Usuario");
            labelNomApe.setFont(new java.awt.Font("Arial", 1, 14));
            labelNomApe.setForeground(new java.awt.Color(255, 255, 255));
            labelNomApe.setHorizontalAlignment(JLabel.CENTER);
            labelNomApe.setAlignmentX(CENTER_ALIGNMENT);
        }

        // Rol
        if (labelRol == null) {
            labelRol = new JLabel("Rol: Usuario");
            labelRol.setFont(new java.awt.Font("Arial", 1, 14));
            labelRol.setForeground(new java.awt.Color(255, 255, 255));
            labelRol.setHorizontalAlignment(JLabel.CENTER);
            labelRol.setAlignmentX(CENTER_ALIGNMENT);
        }

        // Curso y división (solo para alumnos)
        if (userRol == 4 && labelCursoDiv == null) {
            labelCursoDiv = new JLabel("Curso: -");
            labelCursoDiv.setFont(new java.awt.Font("Arial", 1, 14));
            labelCursoDiv.setForeground(new java.awt.Color(255, 255, 255));
            labelCursoDiv.setHorizontalAlignment(JLabel.CENTER);
            labelCursoDiv.setAlignmentX(CENTER_ALIGNMENT);
        }
    }

    /**
     * Verifica la conexión a la base de datos.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    /**
     * Actualiza las etiquetas de nombre y rol.
     *
     * @param nombreCompleto Nombre completo del usuario
     * @param rolTexto Texto descriptivo del rol
     */
    public void updateLabels(String nombreCompleto, String rolTexto) {
        if (labelNomApe == null || labelRol == null) {
            inicializarEtiquetas();
        }

        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: " + rolTexto);
    }

    /**
     * Actualiza las etiquetas para usuario alumno.
     *
     * @param nombreCompleto Nombre completo del alumno
     * @param rolTexto Texto descriptivo del rol
     * @param cursoDiv Curso y división
     */
    public void updateAlumnoLabels(String nombreCompleto, String rolTexto, String cursoDiv) {
        if (labelNomApe == null || labelRol == null || (userRol == 4 && labelCursoDiv == null)) {
            inicializarEtiquetas();
        }

        labelNomApe.setText(nombreCompleto);
        labelRol.setText("Rol: " + rolTexto);
        if (labelCursoDiv != null) {
            labelCursoDiv.setText("Curso: " + cursoDiv);
        }
    }

    /**
     * Actualiza la foto de perfil del usuario.
     *
     * @param fotoUrl URL de la imagen de perfil
     */
    public void updateFotoPerfil(String fotoUrl) {
        cargarFotoPerfilForced(fotoUrl); // Usa el método optimizado
    }

    // Añade este método para refrescar la foto después de cambiar de rol
    public void refrescarFotoPerfil(String fotoUrl) {
        // Primero asegúrate de que la etiqueta existe
        if (labelFotoPerfil == null) {
            inicializarEtiquetas();
        }

        // Luego actualiza su contenido
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            try {
                URL url = new URL(fotoUrl);
                Image imagen = ImageIO.read(url);
                if (imagen != null) {
                    Image imagenRedimensionada = imagen.getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                    labelFotoPerfil.setIcon(new ImageIcon(imagenRedimensionada));
                    // Forzar redibujado
                    labelFotoPerfil.repaint();
                    System.out.println("Foto actualizada con éxito: " + fotoUrl);
                } else {
                    System.err.println("Error: La imagen es nula");
                    usarImagenPorDefecto();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error al cargar la imagen: " + e.getMessage());
                usarImagenPorDefecto();
            }
        } else {
            usarImagenPorDefecto();
        }
    }

    private void usarImagenPorDefecto() {
        // Usar imagen por defecto
        ImageIcon iconoPorDefecto = new ImageIcon(getClass().getResource(ResourceManager.getImagePath("icons8-user-96.png")));
        if (iconoPorDefecto != null && iconoPorDefecto.getIconWidth() > 0) {
            labelFotoPerfil.setIcon(iconoPorDefecto);
            System.out.println("Usando imagen por defecto");
        } else {
            System.err.println("Error: No se pudo cargar la imagen por defecto");
        }
    }

    // Método para cambiar de rol
    public void cambiarARol(int nuevoRol) {
        // Guarda la foto actual
        ImageIcon fotoActual = null;
        if (labelFotoPerfil != null && labelFotoPerfil.getIcon() != null) {
            fotoActual = (ImageIcon) labelFotoPerfil.getIcon();
        }

        // Cambia el rol
        this.userRol = nuevoRol;

        // Actualiza el gestor de paneles
        this.rolPanelManager = RolPanelManagerFactory.createManager(this, userId, userRol);

        // Reconfigura el panel de botones
        configurePanelBotones();

        // Restaura la foto
        if (fotoActual != null && labelFotoPerfil != null) {
            labelFotoPerfil.setIcon(fotoActual);
            labelFotoPerfil.repaint();
        }
    }

    private void hacerLogoClickeable() {
        // Verificar que el componente imagenLogo existe
        if (imagenLogo != null) {
            // Cambiar el cursor al de tipo mano para indicar que es clickeable
            imagenLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Quitar listeners previos para evitar duplicados
            for (java.awt.event.MouseListener ml : imagenLogo.getMouseListeners()) {
                imagenLogo.removeMouseListener(ml);
            }

            // Añadir listener de click
            imagenLogo.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    // Restaurar a la vista principal al hacer clic
                    restaurarVistaPrincipal();
                }
            });
        }
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     */
    protected void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    /**
     * Restaura la vista principal del panel central.
     * Ahora usa CardLayout para cambiar a la vista home.
     */
    public void restaurarVistaPrincipal() {
        System.out.println("Restaurando a vista principal");
        
        try {
            // Cambiar a la vista home usando CardLayout
            cardLayout.show(panelContenido, VISTA_HOME);
            
            // Limpiar el panel dinámico
            panelDinamico.removeAll();
            
            // Forzar actualización
            panelContenido.revalidate();
            panelContenido.repaint();
            
            System.out.println("Vista principal restaurada correctamente");
            
        } catch (Exception ex) {
            System.err.println("Error al restaurar vista principal: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Aplica configuraciones responsive a la ventana.
     */
    private void aplicarResponsive() {
        // Solo hacer responsivos los paneles directamente sin añadir scrolls adicionales
        ResponsiveUtils.makeResponsive(panelContenido);
        ResponsiveUtils.makeResponsive(jPanel4);
        
        // Esto es importante: asegurar que el panel principal tenga un tamaño mínimo
        panelContenido.setMinimumSize(new Dimension(758, 600));
        
        // Guardar tamaño original para escalado
        originalWindowSize = getSize();
        
        // Agregar listener para redimensionamiento
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ajustarComponentesVentana();
            }
        });
        
        // Configurar imágenes responsivas
        if (imagenLogo != null) {
            ResponsiveImageUtils.setResponsiveImage(imagenLogo, 
                    ResourceManager.getImagePath("logo_et20_min.png"));
        }
        
        if (fondoHome != null) {
            ResponsiveImageUtils.setResponsiveImage(fondoHome, 
                    ResourceManager.getImagePath("5c994f25d361a_1200.jpg"));
        }
        
        // Imprimir tamaños para debug
        System.out.println("PanelContenido size: " + panelContenido.getSize());
        System.out.println("Panel4 size: " + jPanel4.getSize());
    }

    public void maximizarVentana() {
        // Obtener dimensiones de la pantalla
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        
        // Establecer tamaño casi completo
        setSize(screenWidth - 50, screenHeight - 50);
        setLocationRelativeTo(null);
        
        // Guardar tamaño como referencia
        originalWindowSize = getSize();
        
        System.out.println("Ventana maximizada a: " + getWidth() + "x" + getHeight());
    }

    /**
     * Ajusta los componentes de la ventana principal según su tamaño
     */
    private Dimension originalWindowSize;

    // Modificar ajustarComponentesVentana() para mejorar el reajuste
    private void ajustarComponentesVentana() {
        int anchoVentana = getWidth();
        int altoVentana = getHeight();
        
        System.out.println("Ajustando ventana: " + anchoVentana + "x" + altoVentana);
        
        // Ajustar panel lateral
        if (jPanel4 != null) {
            // Ancho fijo según el tamaño de la ventana
            int anchoPanel4 = anchoVentana < 800 ? 200 : 260;
            jPanel4.setPreferredSize(new Dimension(anchoPanel4, altoVentana));
            System.out.println("Ancho Panel4: " + anchoPanel4);
        }
        
        // Ajustar panel principal 
        if (panelContenido != null) {
            // Calcular espacio restante
            int anchoDisponible = anchoVentana;
            if (jPanel4 != null && jPanel4.isVisible()) {
                anchoDisponible -= jPanel4.getPreferredSize().width;
            }
            
            // Establecer tamaño del panel principal (con mínimo de 758)
            int anchoPanelPrincipal = Math.max(758, anchoDisponible);
            panelContenido.setPreferredSize(new Dimension(anchoPanelPrincipal, altoVentana));
            System.out.println("Tamaño PanelContenido: " + anchoPanelPrincipal + "x" + altoVentana);
        }
        
        // Forzar actualización
        revalidate();
        repaint();
    }

    // Método recursivo para forzar el reajuste de todos los componentes
    private void reajustarComponentesRecursivamente(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) {
                // Forzar reajuste de tablas
                JTable table = (JTable) comp;
                table.revalidate();
                
                // Ajustar columnas
                if (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                }
            } else if (comp instanceof JScrollPane) {
                // Si es un ScrollPane, procesar su contenido
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof Container) {
                    reajustarComponentesRecursivamente((Container) view);
                }
                scrollPane.revalidate();
            } else if (comp instanceof JPanel) {
                // Procesar paneles recursivamente
                JPanel panel = (JPanel) comp;
                panel.revalidate();
                reajustarComponentesRecursivamente(panel);
            } else if (comp instanceof Container) {
                // Otros contenedores
                Container subContainer = (Container) comp;
                subContainer.revalidate();
                reajustarComponentesRecursivamente(subContainer);
            }
        }
    }

    /**
     * Obtiene el panel principal donde se muestra el contenido.
     * MODIFICADO: Ahora retorna el panel dinámico en lugar del panel home.
     *
     * @return Panel dinámico para contenido específico de roles
     */
    public JPanel getPanelPrincipal() {
        // Método de compatibilidad: crear un panel temporal que delega al método mostrarPanel
        return new JPanel() {
            @Override
            public void removeAll() {
                // Override para interceptar cuando los PanelManager intentan limpiar
                panelDinamico.removeAll();
            }

            @Override
            public void add(Component comp, Object constraints) {
                // Override para interceptar cuando los PanelManager añaden componentes
                if (comp instanceof JPanel) {
                    String titulo = determinarTituloPanel(comp);
                    mostrarPanel((JPanel) comp, titulo);
                } else {
                    panelDinamico.add(comp, constraints);
                }
            }

            @Override
            public void revalidate() {
                panelDinamico.revalidate();
                panelContenido.revalidate();
            }

            @Override
            public void repaint() {
                panelDinamico.repaint();
                panelContenido.repaint();
            }

            @Override
            public void setLayout(java.awt.LayoutManager mgr) {
                // Ignorar cambios de layout del panel wrapper
                panelDinamico.setLayout(new BorderLayout());
            }
        };
    }

    /**
     * Determina el título del panel basándose en su clase.
     *
     * @param panel Panel a analizar
     * @return Título apropiado para el panel
     */
    private String determinarTituloPanel(Component panel) {
        String className = panel.getClass().getSimpleName();
        
        // Mapear nombres de clase a títulos legibles
        switch (className) {
            case "UsuariosPendientesPanel":
                return "Gestión de Usuarios Pendientes";
            case "GestionUsuariosPanel":
                return "Gestión de Usuarios";
            case "GestionCursosPanel":
                return "Gestión de Cursos";
            case "NotasProfesorPanel":
                return "Gestión de Notas";
            case "AsistenciaProfesorPanel":
                return "Gestión de Asistencias";
            case "AsistenciaPreceptorPanel":
                return "Control de Asistencias";
            case "StockPanel":
                return "Gestión de Stock";
            case "PrestamosPanel":
                return "Gestión de Préstamos";
            case "RegistrosPanel":
                return "Registros";
            case "NetbookRegistrationPanel":
                return "Registro de Netbooks";
            case "libroTema":
                return "Libro de Temas";
            case "NotasBimestralesPanel":
                return "Notas Bimestrales";
            default:
                return "Panel de Trabajo";
        }
    }

    /**
     * Retorna el gestor de paneles para este rol.
     *
     * @return Gestor de paneles actual
     */
    public RolPanelManager getRolPanelManager() {
        return rolPanelManager;
    }

    /**
     * Establece un nuevo rol para el usuario.
     *
     * @param userRol Nuevo rol del usuario
     */
    public void setUserRol(int userRol) {
        this.userRol = userRol;
    }

    /**
     * Obtiene el rol actual del usuario.
     *
     * @return El rol actual
     */
    public int getUserRol() {
        return this.userRol;
    }

    /**
     * Obtiene el ID del usuario.
     *
     * @return El ID del usuario
     */
    public int getUserId() {
        return this.userId;
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
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fondoHome = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        imagenLogo = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 642, -1, -1));

        jLabel6.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("\"Carolina Muzilli\"");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 370, 80));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Escuela Técnica 20 D.E. 20");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 540, 80));

        fondoHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/5c994f25d361a_1200.jpg"))); // NOI18N
        fondoHome.setPreferredSize(new java.awt.Dimension(700, 565));
        jScrollPane1.setViewportView(fondoHome);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 71, 758, -1));

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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

        /**
         * @param args the command line arguments
         */
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
                java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>

            /* Set the Nimbus look and feel */
            try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(VentanaInicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                // Crear la ventana con valores de prueba
                VentanaInicio ventana = new VentanaInicio(1, 1);
                ventana.setVisible(true);

                // Probar carga de imagen con una URL de ejemplo
                // Puedes usar una URL real o una imagen local
                ventana.cargarFotoPerfilForced("https://via.placeholder.com/96");

                // Opcional: Para simular un cambio de rol después de unos segundos
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Simulando cambio de rol...");
                        ventana.userRol = 4; // Cambiar a rol de alumno
                        ventana.rolPanelManager = RolPanelManagerFactory.createManager(ventana, ventana.userId, ventana.userRol);
                        ventana.configurePanelBotones();
                        // Volver a cargar la foto después del cambio
                        ventana.cargarFotoPerfilForced("https://via.placeholder.com/96?blue");
                    }
                },
                        5000 // 5 segundos de espera
                );
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fondoHome;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
