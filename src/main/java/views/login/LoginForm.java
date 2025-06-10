package main.java.views.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.sql.*;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.utils.GoogleEmailIntegration;
import main.java.utils.ResourceManager;
import main.java.utils.ResponsiveBannerPanel;
import main.java.utils.uiUtils;
import main.java.views.users.common.VentanaInicio;

/**
 * Clase principal de inicio de sesión para la plataforma educativa. Gestiona la
 * autenticación de usuarios mediante credenciales locales o Google.
 *
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [12/03/2025]
 */
public class LoginForm extends javax.swing.JFrame {

    // Conexión a la base de datos
    Connection conect;

    /**
     * Método para probar la conexión a la base de datos. Utiliza el patrón
     * Singleton para obtener la conexión.
     */
    private void probar_conexion() {
        // Obtener la conexión desde el Singleton
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    /**
     * Constructor de la clase login. Inicializa los componentes de la interfaz
     * y configura el cierre de la aplicación.
     */
    public LoginForm() {
        initComponents();

        // Usar ResourceManager para las imágenes
        rsscalelabel.RSScaleLabel.setScaleLabel(imagenLogo, ResourceManager.getImagePath("logo_et20_min.png"));
        rsscalelabel.RSScaleLabel.setScaleLabel(bannerImg1, ResourceManager.getImagePath("banner-et20.png"));
        rsscalelabel.RSScaleLabel.setScaleLabel(bannerImg2, ResourceManager.getImagePath("banner-et20.png"));

        // Configurar el campo de contraseña para mostrar texto plano inicialmente
        campoContraseña.setText("****************");
        campoContraseña.setEchoChar((char) 0);

        // Desactivar el comportamiento por defecto del botón X
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        // Agregar el listener para el botón de cerrar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                cerrarPrograma();
            }
        });

        // Hacer que el login sea completamente responsivo y centrado
        aplicarDiseñoResponsivoCentrado();

        uiUtils.configurarVentana(this);
    }

    /**
     * Aplica un diseño responsivo y centrado a todo el formulario de login
     * Reemplaza el AbsoluteLayout con un enfoque basado en BorderLayout y
     * GridBagLayout
     */
    private void aplicarDiseñoResponsivoCentrado() {
        // 1. Preservar referencias a los componentes originales
        // (en caso de que ya no estén accesibles después de remover todo)
        if (imagenLogo == null || jPanel1 == null) {
            System.err.println("Error: Componentes críticos son nulos");
            return;
        }

        // 2. Limpiar el contenedor principal
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());

        // 3. Crear un panel principal con color de fondo específico
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(249, 245, 232)); // Mantener el color original

        // 4. Crear paneles para los banners superior e inferior usando la utilidad
        ResponsiveBannerPanel topBannerPanel = new ResponsiveBannerPanel(ResourceManager.getImagePath("banner-et20.png"));
        ResponsiveBannerPanel bottomBannerPanel = new ResponsiveBannerPanel(ResourceManager.getImagePath("banner-et20.png"));

        // 5. Crear un panel central para el contenido (logo y formulario)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // 6. Crear un panel que contendrá el logo y el formulario, uno encima del otro
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 7. Ajustar el contenedor del logo para que esté centrado
        JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoContainer.setOpaque(false);
        logoContainer.add(imagenLogo);

        // 8. Asegurarse que el formulario esté configurado correctamente
        jPanel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 9. Añadir elementos al panel de contenido en el orden correcto
        contentPanel.add(Box.createVerticalStrut(20)); // Espacio superior
        contentPanel.add(logoContainer);
        contentPanel.add(Box.createVerticalStrut(20)); // Espacio entre logo y formulario
        contentPanel.add(jPanel1);
        contentPanel.add(Box.createVerticalStrut(20)); // Espacio inferior

        // 10. Añadir el panel de contenido al panel central
        centerPanel.add(contentPanel);

        // 11. Montar la estructura completa en un orden específico
        mainPanel.add(topBannerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomBannerPanel, BorderLayout.SOUTH);

        // 12. Agregar el panel principal al contenedor
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // 13. Importante: forzar actualización de la jerarquía de componentes
        mainPanel.revalidate();
        mainPanel.repaint();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    /**
     * Método para cerrar la aplicación de manera segura. Realiza logout y
     * termina la ejecución del programa.
     */
    private void cerrarPrograma() {
        try {
            GoogleAuthenticator authenticator = new GoogleAuthenticator();
            authenticator.logout();
            System.exit(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cerrar sesión: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        panel1 = new java.awt.Panel();
        panelLogin = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        campoNombre = new javax.swing.JTextField();
        campoContraseña = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        botonLogin = new javax.swing.JButton();
        botonGoogle = new javax.swing.JButton();
        bannerImg2 = new javax.swing.JLabel();
        imagenLogo = new javax.swing.JLabel();
        bannerImg1 = new javax.swing.JLabel();

        jLabel5.setText("jLabel5");

        jLabel4.setText("jLabel4");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 204));
        setExtendedState(6);

        panelLogin.setBackground(new java.awt.Color(249, 245, 232));
        panelLogin.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setForeground(new java.awt.Color(51, 204, 0));

        jLabel3.setFont(new java.awt.Font("Segoe UI Variable", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(21, 24, 43));
        jLabel3.setText("Usuario");

        campoNombre.setText("Ingrese su mail");
        campoNombre.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        campoNombre.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoNombreFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoNombreFocusLost(evt);
            }
        });
        campoNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNombreActionPerformed(evt);
            }
        });

        campoContraseña.setText("Constraseña");
        campoContraseña.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        campoContraseña.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoContraseñaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoContraseñaFocusLost(evt);
            }
        });
        campoContraseña.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoContraseñaActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI Variable", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(21, 24, 43));
        jLabel2.setText("Contraseña");

        botonLogin.setBackground(new java.awt.Color(103, 136, 255));
        botonLogin.setFont(new java.awt.Font("Segoe UI Variable", 1, 14)); // NOI18N
        botonLogin.setForeground(new java.awt.Color(255, 255, 255));
        botonLogin.setText("ENTRAR");
        botonLogin.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonLoginActionPerformed(evt);
            }
        });

        botonGoogle.setBackground(new java.awt.Color(103, 136, 255));
        botonGoogle.setFont(new java.awt.Font("Segoe UI Variable", 1, 14)); // NOI18N
        botonGoogle.setForeground(new java.awt.Color(255, 255, 255));
        botonGoogle.setText("GOOGLE");
        botonGoogle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonGoogle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonGoogle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonGoogleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(botonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botonGoogle, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 146, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(149, 149, 149)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3)
                        .addComponent(campoNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addComponent(campoContraseña))
                    .addContainerGap(149, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(207, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonGoogle, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(45, 45, 45)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(campoNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(campoContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(117, Short.MAX_VALUE)))
        );

        panelLogin.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 290, -1, -1));

        bannerImg2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        panelLogin.add(bannerImg2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 790, 760, -1));

        imagenLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/logo_et20_min.png"))); // NOI18N
        imagenLogo.setAutoscrolls(true);
        imagenLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelLogin.add(imagenLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 90, 150, 160));

        bannerImg1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/main/resources/images/banner-et20.png"))); // NOI18N
        panelLogin.add(bannerImg1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 760, -1));

        getContentPane().add(panelLogin, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void campoNombreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoNombreFocusGained
        if (campoNombre.getText().equals("Ingrese su mail")) {
            campoNombre.setText(null);
            campoNombre.requestFocus();
        }
    }//GEN-LAST:event_campoNombreFocusGained

    private void campoNombreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoNombreFocusLost
        if (campoNombre.getText().isEmpty()) {
            campoNombre.setText("Ingrese su mail");
        }
    }//GEN-LAST:event_campoNombreFocusLost

    private void campoNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNombreActionPerformed

    private void campoContraseñaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoContraseñaFocusGained
        String password = String.valueOf(campoContraseña.getPassword());
        if (password.equals("****************")) {
            campoContraseña.setText("");
            campoContraseña.setEchoChar('•'); // Activar caracteres ocultos
        }
    }//GEN-LAST:event_campoContraseñaFocusGained

    private void campoContraseñaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoContraseñaFocusLost
        String password = String.valueOf(campoContraseña.getPassword());
        if (password.isEmpty()) {
            campoContraseña.setText("****************");
            campoContraseña.setEchoChar((char) 0); // Mostrar texto plano
        }
    }//GEN-LAST:event_campoContraseñaFocusLost

    private void campoContraseñaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoContraseñaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoContraseñaActionPerformed

    /**
     * Método de inicio de sesión con credenciales locales. Valida el usuario,
     * obtiene sus datos y redirige según su rol.
     *
     * @param evt Evento de acción del botón de login
     */
    private void botonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonLoginActionPerformed
        try {
            // Obtener credenciales
            String mail = campoNombre.getText();
            String contrasena = new String(campoContraseña.getPassword());

            // Validar campos
            if (mail.equals("Ingrese su mail") || contrasena.equals("****************")) {
                JOptionPane.showMessageDialog(this,
                        "Por favor ingrese usuario y contraseña",
                        "Error de login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar conexión a base de datos    
            if (!validarConexion()) {
                return;
            }

            // Hashear la contraseña antes de la consulta
            String contrasenaHasheada = hashPassword(contrasena);

            // Consulta para autenticación
            String query = "SELECT id, nombre, apellido, rol, foto_url FROM usuarios "
                    + "WHERE mail = ? AND contrasena = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setString(1, mail);
            ps.setString(2, contrasenaHasheada); // Usar contraseña hasheada

            // Procesar resultado de autenticación
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Obtener datos del usuario
                int userId = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                int rol = rs.getInt("rol");
                String fotoUrl = rs.getString("foto_url");

                // VERIFICAR SI EL USUARIO TIENE UN ROL ASIGNADO
                if (rol == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Tu cuenta está pendiente de asignación de rol.\n"
                            + "Por favor contacta al administrador para que te asigne un rol.",
                            "Cuenta Pendiente",
                            JOptionPane.INFORMATION_MESSAGE);
                    return; // Salir del método sin continuar
                }

                // Crear sesión de usuario con ID solo si tiene rol válido
                UserSession session = new UserSession(userId, nombre, apellido, mail, rol, fotoUrl);

                // Verificar datos complementarios
                verificarDatosComplementarios(session);
            } else {
                // Si no encuentra resultados, podría ser que la contraseña no esté hasheada en la DB
                // Intenta con la contraseña sin hashear (para compatibilidad con cuentas existentes)
                query = "SELECT id, nombre, apellido, rol, foto_url FROM usuarios "
                        + "WHERE mail = ? AND contrasena = ?";

                ps = conect.prepareStatement(query);
                ps.setString(1, mail);
                ps.setString(2, contrasena); // Usar contraseña sin hashear

                rs = ps.executeQuery();

                if (rs.next()) {
                    // Encontrado con contraseña sin hashear, actualizar a versión hasheada
                    int userId = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    String apellido = rs.getString("apellido");
                    int rol = rs.getInt("rol");
                    String fotoUrl = rs.getString("foto_url");

                    // VERIFICAR SI EL USUARIO TIENE UN ROL ASIGNADO
                    if (rol == 0) {
                        JOptionPane.showMessageDialog(this,
                                "Tu cuenta está pendiente de asignación de rol.\n"
                                + "Por favor contacta al administrador para que te asigne un rol.",
                                "Cuenta Pendiente",
                                JOptionPane.INFORMATION_MESSAGE);
                        return; // Salir del método sin continuar
                    }

                    // Actualizar la contraseña a la versión hasheada
                    String updateQuery = "UPDATE usuarios SET contrasena = ? WHERE id = ?";
                    PreparedStatement updatePs = conect.prepareStatement(updateQuery);
                    updatePs.setString(1, contrasenaHasheada);
                    updatePs.setInt(2, userId);
                    updatePs.executeUpdate();

                    // Crear sesión de usuario
                    UserSession session = new UserSession(userId, nombre, apellido, mail, rol, fotoUrl);

                    // Verificar datos complementarios
                    verificarDatosComplementarios(session);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Usuario o contraseña incorrectos",
                            "Error de login",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al intentar iniciar sesión: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_botonLoginActionPerformed

    private void botonGoogleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonGoogleActionPerformed
        try {
            GoogleAuthenticator authenticator = new GoogleAuthenticator();
            UserSession partialSession = authenticator.authenticateUser();

            if (!validarConexion()) {
                return;
            }

            // Obtener ID del usuario
            int userId = obtenerUsuarioId(partialSession.getEmail());
            if (userId == -1) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró una cuenta asociada con este correo.",
                        "Error de Autenticación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear sesión completa con ID
            UserSession session = new UserSession(
                    userId,
                    partialSession.getNombre(),
                    partialSession.getApellido(),
                    partialSession.getEmail(),
                    partialSession.getRol(),
                    partialSession.getFotoUrl()
            );

            // Verificar datos complementarios
            verificarDatosComplementarios(session);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error durante la autenticación: " + ex.getMessage(),
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Verifica si el usuario tiene todos los datos complementarios completos.
     * Si faltan datos, muestra formularios para completarlos.
     *
     * @param session Sesión del usuario autenticado
     */
    private void verificarDatosComplementarios(UserSession session) {
        // PRIMERA VERIFICACIÓN: Comprobar si el usuario tiene un rol válido
        if (session.getRol() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Tu cuenta está pendiente de asignación de rol.\n"
                    + "Por favor contacta al administrador para que te asigne un rol.",
                    "Cuenta Pendiente",
                    JOptionPane.INFORMATION_MESSAGE);
            return; // SALIR INMEDIATAMENTE sin continuar el proceso
        }

        // SEGUNDA VERIFICACIÓN: Proceder con datos complementarios solo si el rol es válido
        // Crear gestor de datos complementarios
        DatosComplementariosManager datosManager = new DatosComplementariosManager(this, session);

        // Configurar qué hacer cuando todos los datos estén completos
        datosManager.setOnCompletionCallback(() -> {
            // Usar el método unificado en lugar de los específicos por rol
            manejarLoginUsuario(session);
        });

        // Iniciar el proceso de verificación
        datosManager.startVerification();
    }

    private boolean validarConexion() {
        // Obtener una conexión fresca
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo establecer conexión con la base de datos",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Maneja la autenticación general para cualquier tipo de usuario. Este
     * método sustituye a todos los métodos específicos de cada rol.
     *
     * @param session Sesión de usuario autenticado
     */
    /**
     * Maneja la autenticación general para cualquier tipo de usuario. Este
     * método sustituye a todos los métodos específicos de cada rol.
     *
     * @param session Sesión de usuario autenticado
     */
    private void manejarLoginUsuario(UserSession session) {
        try {
            // Verificar si el usuario tiene un rol asignado
            if (session.getRol() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Tu cuenta está pendiente de asignación de rol.\n"
                        + "Por favor contacta al administrador para que te asigne un rol.",
                        "Cuenta Pendiente",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            System.out.println("✅ Creando VentanaInicio para usuario " + session.getUserId() + " con rol " + session.getRol());

            // Crear la ventana unificada
            VentanaInicio ventana = new VentanaInicio(session.getUserId(), session.getRol());

            // ESPERAR a que la ventana se inicialice completamente
            SwingUtilities.invokeLater(() -> {
                // Configurar el panel de botones primero
                ventana.configurePanelBotones();

                // LUEGO actualizar la información del usuario
                SwingUtilities.invokeLater(() -> {
                    if (session.getRol() == 4) { // Alumno
                        String cursoDiv = obtenerCursoDivisionAlumno(session.getUserId());
                        ventana.updateAlumnoLabels(
                                session.getNombre() + " " + session.getApellido(),
                                obtenerTextoRol(session.getRol()),
                                cursoDiv
                        );
                    } else {
                        ventana.updateLabels(
                                session.getNombre() + " " + session.getApellido(),
                                obtenerTextoRol(session.getRol())
                        );
                    }

                    // Actualizar foto de perfil al final
                    if (session.getFotoUrl() != null && !session.getFotoUrl().isEmpty()) {
                        ventana.updateFotoPerfil(session.getFotoUrl());
                    }

                    System.out.println("✅ Datos del usuario configurados completamente");
                });
            });

            ventana.setVisible(true);
            this.dispose();

            // NOTA: Se removió la configuración automática de email del login
            // Ahora solo se configura cuando el usuario intenta enviar emails
        } catch (Exception ex) {
            System.err.println("❌ Error al cargar la interfaz: " + ex.getMessage());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Error al cargar la interfaz: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obtiene el texto descriptivo del rol.
     *
     * @param rol Código numérico del rol
     * @return Descripción textual del rol
     */
    private String obtenerTextoRol(int rol) {
        switch (rol) {
            case 1:
                return "Administrador";
            case 2:
                return "Preceptor";
            case 3:
                return "Profesor";
            case 4:
                return "Estudiante";
            case 5:
                return "ATTP";
            default:
                return "Usuario";
        }
    }

    /**
     * Obtiene la información de curso y división para un alumno.
     *
     * @param alumnoId ID del alumno
     * @return String con formato "Año°División"
     */
    private String obtenerCursoDivisionAlumno(int alumnoId) {
        try {
            String query = "SELECT anio, division FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("anio") + "°" + rs.getString("division");
            } else {
                return "Sin asignar";
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener curso/división: " + e.getMessage());
            return "Error";
        }
    }

    private void manejarErrorAutenticacion(Exception ex) {
        JOptionPane.showMessageDialog(null,
                "Error durante la autenticación: " + ex.getMessage(),
                "Error de Autenticación",
                JOptionPane.ERROR_MESSAGE);
        System.out.println(ex.getMessage());
    }

    private int obtenerUsuarioId(String mail) throws SQLException {
        String query = "SELECT id FROM usuarios WHERE mail = ?";
        PreparedStatement ps = conect.prepareStatement(query);
        ps.setString(1, mail);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Convertir byte array a string hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println("Error al hashear contraseña: " + e.getMessage());
            // Si hay error en el hashing, retornar la contraseña sin hashear (no ideal)
            return password;
        }


    }//GEN-LAST:event_botonGoogleActionPerformed

    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginForm().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerImg1;
    private javax.swing.JLabel bannerImg2;
    private javax.swing.JButton botonGoogle;
    private javax.swing.JButton botonLogin;
    private javax.swing.JPasswordField campoContraseña;
    private javax.swing.JTextField campoNombre;
    private javax.swing.JLabel imagenLogo;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private java.awt.Panel panel1;
    private javax.swing.JPanel panelLogin;
    // End of variables declaration//GEN-END:variables
}
