package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import main.java.utils.EmailBoletinUtility;
import main.java.utils.GoogleEmailIntegration;

/**
 * Panel para configurar el sistema de envío de emails Integrado con
 * EmailBoletinUtility existente
 */
public class ConfiguracionEmailPanel extends JPanel {

    private final VentanaInicio ventana;
    private final int userId;
    private final int userRol;

    // Componentes de configuración
    private JTextField txtServidorSMTP;
    private JTextField txtPuertoSMTP;
    private JTextField txtEmailRemitente;
    private JPasswordField txtPasswordRemitente;
    private JTextField txtNombreRemitente;
    private JCheckBox chkUsarTLS;
    private JCheckBox chkUsarAuth;
    private JTextField txtEmailPrueba;
    private JButton btnConfigAutomatica;
    private JButton btnRenovarToken;
    private JLabel lblEstadoOAuth2;
    private JPanel panelOAuth2;

    // Botones
    private JButton btnGuardarConfig;
    private JButton btnProbarConfig;
    private JButton btnEstadisticas;
    private JButton btnVolver;

    public ConfiguracionEmailPanel(VentanaInicio ventana, int userId, int userRol) {
        this.ventana = ventana;
        this.userId = userId;
        this.userRol = userRol;

        initializeComponents();
        setupLayout();
        setupListeners();
        cargarConfiguracionActual();

        System.out.println("✅ ConfiguracionEmailPanel inicializado para usuario: " + userId);
    }

    private void initializeComponents() {
        // Campos de configuración
        txtServidorSMTP = new JTextField(20);
        txtPuertoSMTP = new JTextField(10);
        txtEmailRemitente = new JTextField(25);
        txtPasswordRemitente = new JPasswordField(25);
        txtNombreRemitente = new JTextField(25);
        chkUsarTLS = new JCheckBox("Usar TLS (recomendado)", true);
        chkUsarAuth = new JCheckBox("Usar autenticación", true);
        txtEmailPrueba = new JTextField(25);

        // Valores por defecto
        txtServidorSMTP.setText("smtp.gmail.com");
        txtPuertoSMTP.setText("587");
        txtNombreRemitente.setText("Escuela Técnica N° 20");

        // Tooltips
        txtServidorSMTP.setToolTipText("Servidor SMTP (ej: smtp.gmail.com)");
        txtPuertoSMTP.setToolTipText("Puerto SMTP (587 para TLS, 465 para SSL)");
        txtEmailRemitente.setToolTipText("Email desde el cual se enviarán los boletines");
        txtPasswordRemitente.setToolTipText("Contraseña del email o contraseña de aplicación");
        txtEmailPrueba.setToolTipText("Email para enviar mensaje de prueba");

        // Botones
        btnGuardarConfig = createStyledButton("💾 Guardar Configuración", new Color(76, 175, 80));
        btnProbarConfig = createStyledButton("📧 Enviar Prueba", new Color(33, 150, 243));
        btnEstadisticas = createStyledButton("📊 Estadísticas", new Color(156, 39, 176));
        btnVolver = createStyledButton("← Volver", new Color(96, 125, 139));

        txtServidorSMTP = new JTextField(20);
        txtPuertoSMTP = new JTextField(10);
        txtEmailRemitente = new JTextField(25);
        txtPasswordRemitente = new JPasswordField(25);
        txtNombreRemitente = new JTextField(25);
        chkUsarTLS = new JCheckBox("Usar TLS (recomendado)", true);
        chkUsarAuth = new JCheckBox("Usar autenticación", true);
        txtEmailPrueba = new JTextField(25);

        // Valores por defecto
        txtServidorSMTP.setText("smtp.gmail.com");
        txtPuertoSMTP.setText("587");
        txtNombreRemitente.setText("Escuela Técnica N° 20");

        // NUEVOS COMPONENTES OAuth2
        btnConfigAutomatica = createStyledButton("🚀 Configuración Automática (OAuth2)", new Color(52, 168, 83));
        btnRenovarToken = createStyledButton("🔄 Renovar Token", new Color(255, 152, 0));
        lblEstadoOAuth2 = new JLabel("Estado OAuth2: No configurado");

        // Tooltips mejorados
        txtServidorSMTP.setToolTipText("Servidor SMTP (ej: smtp.gmail.com)");
        txtPuertoSMTP.setToolTipText("Puerto SMTP (587 para TLS, 465 para SSL)");
        txtEmailRemitente.setToolTipText("Email desde el cual se enviarán los boletines");
        txtPasswordRemitente.setToolTipText("⚠️ Contraseña de aplicación (NO su contraseña normal)");
        txtEmailPrueba.setToolTipText("Email para enviar mensaje de prueba");
        btnConfigAutomatica.setToolTipText("Configurar automáticamente usando su cuenta Google actual");
        btnRenovarToken.setToolTipText("Renovar token OAuth2 si ha expirado");

        // Botones existentes...
        btnGuardarConfig = createStyledButton("💾 Guardar Configuración", new Color(76, 175, 80));
        btnProbarConfig = createStyledButton("📧 Enviar Prueba", new Color(33, 150, 243));
        btnEstadisticas = createStyledButton("📊 Estadísticas", new Color(156, 39, 176));
        btnVolver = createStyledButton("← Volver", new Color(96, 125, 139));

        // Actualizar estado OAuth2
        actualizarEstadoOAuth2();
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 40));
        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel título
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblTitulo = new JLabel("⚙️ Configuración de Email para Boletines");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(33, 150, 243));
        panelTitulo.add(lblTitulo);

        // Panel principal con configuración
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(createOAuth2Panel(), BorderLayout.NORTH);
        panelPrincipal.add(createConfiguracionPanel(), BorderLayout.CENTER);
        panelPrincipal.add(createPruebaPanel(), BorderLayout.SOUTH);

        // Panel botones
        JPanel panelBotones = createBotonesPanel();

        // Ensamblar
        add(panelTitulo, BorderLayout.NORTH);
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel createOAuth2Panel() {
        panelOAuth2 = new JPanel();
        panelOAuth2.setBorder(BorderFactory.createTitledBorder("🚀 Configuración Automática (Recomendado)"));
        panelOAuth2.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblDescripcion = new JLabel(
                "<html><b>OAuth2 con Google:</b> Usa tu cuenta Google actual para enviar emails.<br>"
                + "✅ Más seguro (no requiere contraseña de aplicación)<br>"
                + "✅ Más fácil de configurar<br>"
                + "✅ Se configura automáticamente</html>");
        lblDescripcion.setForeground(new Color(76, 175, 80));
        panelOAuth2.add(lblDescripcion, gbc);

        // Estado actual
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelOAuth2.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panelOAuth2.add(lblEstadoOAuth2, gbc);

        // Botones
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panelOAuth2.add(btnConfigAutomatica, gbc);
        gbc.gridx = 1;
        panelOAuth2.add(btnRenovarToken, gbc);

        return panelOAuth2;
    }

    private JPanel createConfiguracionPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("⚙️ Configuración Manual (Alternativa)"));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblDescManual = new JLabel(
                "<html><b>Configuración SMTP tradicional:</b> Requiere contraseña de aplicación.<br>"
                + "⚠️ Más complejo de configurar<br>"
                + "⚠️ Requiere generar contraseña de aplicación en Google</html>");
        lblDescManual.setForeground(new Color(255, 152, 0));
        panel.add(lblDescManual, gbc);

        // Campos de configuración existentes...
        // (mantener el código original para servidor SMTP, puerto, etc.)
        // Servidor SMTP
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Servidor SMTP:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtServidorSMTP, gbc);

        // Puerto
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPuertoSMTP, gbc);

        // Email remitente
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Email remitente:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtEmailRemitente, gbc);

        // Password con advertencia
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel lblPassword = new JLabel("<html>Contraseña:<br><small><font color='red'>⚠️ Contraseña de app</font></small></html>");
        panel.add(lblPassword, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtPasswordRemitente, gbc);

        // Link de ayuda para contraseña de aplicación
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        JLabel lblAyuda = new JLabel(
                "<html><a href=''>¿Cómo generar una contraseña de aplicación en Gmail?</a></html>");
        lblAyuda.setForeground(Color.BLUE);
        lblAyuda.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblAyuda.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mostrarAyudaContrasenaApp();
            }
        });
        panel.add(lblAyuda, gbc);

        // Nombre remitente
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Nombre remitente:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtNombreRemitente, gbc);

        // Checkboxes
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        panel.add(chkUsarTLS, gbc);

        gbc.gridy = 8;
        panel.add(chkUsarAuth, gbc);

        return panel;
    }

    private JPanel createPruebaPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("🧪 Prueba de Configuración"));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Email de prueba:"), gbc);
        gbc.gridx = 1;
        panel.add(txtEmailPrueba, gbc);
        gbc.gridx = 2;
        panel.add(btnProbarConfig, gbc);

        return panel;
    }

    private JPanel createBotonesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        panel.add(btnGuardarConfig);
        panel.add(btnEstadisticas);
        panel.add(btnVolver);

        return panel;
    }

    private void setupListeners() {
        btnConfigAutomatica.addActionListener(this::configurarAutomaticamente);
        btnRenovarToken.addActionListener(this::renovarToken);
        btnGuardarConfig.addActionListener(this::guardarConfiguracion);
        btnProbarConfig.addActionListener(this::probarConfiguracion);
        btnEstadisticas.addActionListener(this::mostrarEstadisticas);
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());
    }

    private void configurarAutomaticamente(ActionEvent e) {
        btnConfigAutomatica.setEnabled(false);
        btnConfigAutomatica.setText("🔄 Configurando...");

        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Iniciando configuración automática...");
                return GoogleEmailIntegration.configurarEmailAutomaticamente(userId);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    System.out.println(message);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();

                    if (exito) {
                        JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                                "✅ Configuración automática exitosa\n\n"
                                + "Su email se configuró automáticamente usando OAuth2.\n"
                                + "Ya puede enviar boletines por email.",
                                "Configuración Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Actualizar interfaz
                        actualizarEstadoOAuth2();
                        cargarConfiguracionActual();

                    } else {
                        JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                                "❌ Error en configuración automática\n\n"
                                + "No se pudo configurar automáticamente.\n"
                                + "Puede intentar la configuración manual o revisar:\n"
                                + "• Conexión a internet\n"
                                + "• Permisos de la cuenta Google\n"
                                + "• Firewall/antivirus",
                                "Error de Configuración",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                            "Error durante la configuración: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnConfigAutomatica.setEnabled(true);
                    btnConfigAutomatica.setText("🚀 Configuración Automática (OAuth2)");
                }
            }
        };

        worker.execute();
    }

    private void renovarToken(ActionEvent e) {
        btnRenovarToken.setEnabled(false);
        btnRenovarToken.setText("🔄 Renovando...");

        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return GoogleEmailIntegration.renovarToken(userId);
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();

                    if (exito) {
                        JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                                "✅ Token renovado exitosamente",
                                "Renovación Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);

                        actualizarEstadoOAuth2();
                    } else {
                        JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                                "❌ Error renovando token\n\n"
                                + "Intente configurar nuevamente.",
                                "Error de Renovación",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnRenovarToken.setEnabled(true);
                    btnRenovarToken.setText("🔄 Renovar Token");
                }
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Actualizar estado OAuth2
     */
    private void actualizarEstadoOAuth2() {
        try {
            boolean tieneCredenciales = GoogleEmailIntegration.tieneCredencialesValidas(userId);
            EmailBoletinUtility.ConfiguracionEmail config = EmailBoletinUtility.obtenerConfiguracion();

            if (tieneCredenciales && config.useOAuth2) {
                lblEstadoOAuth2.setText("✅ OAuth2 configurado - " + config.emailRemitente);
                lblEstadoOAuth2.setForeground(new Color(76, 175, 80));
                btnRenovarToken.setEnabled(true);
            } else {
                lblEstadoOAuth2.setText("❌ OAuth2 no configurado");
                lblEstadoOAuth2.setForeground(Color.RED);
                btnRenovarToken.setEnabled(false);
            }

        } catch (Exception e) {
            lblEstadoOAuth2.setText("⚠️ Error verificando estado");
            lblEstadoOAuth2.setForeground(Color.ORANGE);
            btnRenovarToken.setEnabled(false);
        }
    }

    /**
     * NUEVO: Mostrar ayuda para contraseña de aplicación
     */
    private void mostrarAyudaContrasenaApp() {
        String mensaje = """
            📱 CÓMO GENERAR CONTRASEÑA DE APLICACIÓN EN GMAIL
            
            1️⃣ Ve a tu cuenta Google (myaccount.google.com)
            2️⃣ Seguridad → Verificación en 2 pasos (debe estar activada)
            3️⃣ Contraseñas de aplicaciones
            4️⃣ Selecciona "Correo" y "Windows Computer"
            5️⃣ Google generará una contraseña de 16 caracteres
            6️⃣ Copia esa contraseña (NO tu contraseña normal)
            7️⃣ Úsala en el campo "Contraseña" de este formulario
            
            ⚠️ IMPORTANTE:
            • Debes tener verificación en 2 pasos activada
            • La contraseña de aplicación es diferente a tu contraseña normal
            • Es más seguro usar la configuración automática OAuth2
            
            💡 RECOMENDACIÓN: Usa "Configuración Automática" en lugar de esto
            """;

        JTextArea textArea = new JTextArea(mensaje);
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(50);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this,
                new JScrollPane(textArea),
                "Ayuda - Contraseña de Aplicación Gmail",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarConfiguracionActual() {
        EmailBoletinUtility.ConfiguracionEmail config = EmailBoletinUtility.obtenerConfiguracion();

        if (config.smtpHost != null && !config.smtpHost.isEmpty()) {
            txtServidorSMTP.setText(config.smtpHost);
        }
        if (config.smtpPort != null && !config.smtpPort.isEmpty()) {
            txtPuertoSMTP.setText(config.smtpPort);
        }
        if (config.emailRemitente != null && !config.emailRemitente.isEmpty()) {
            txtEmailRemitente.setText(config.emailRemitente);
        }
        if (config.nombreRemitente != null && !config.nombreRemitente.isEmpty()) {
            txtNombreRemitente.setText(config.nombreRemitente);
        }

        chkUsarTLS.setSelected(config.useTLS);
        chkUsarAuth.setSelected(config.useAuth);

        System.out.println("✅ Configuración actual cargada en la interfaz");
    }

    private void guardarConfiguracion(ActionEvent e) {
        try {
            String smtpHost = txtServidorSMTP.getText().trim();
            String smtpPort = txtPuertoSMTP.getText().trim();
            String emailRemitente = txtEmailRemitente.getText().trim();
            String passwordRemitente = new String(txtPasswordRemitente.getPassword());
            String nombreRemitente = txtNombreRemitente.getText().trim();
            boolean useTLS = chkUsarTLS.isSelected();
            boolean useAuth = chkUsarAuth.isSelected();

            // Validaciones básicas
            if (smtpHost.isEmpty() || smtpPort.isEmpty() || emailRemitente.isEmpty()
                    || passwordRemitente.isEmpty() || nombreRemitente.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, complete todos los campos obligatorios.",
                        "Campos Incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validar email
            if (!EmailBoletinUtility.validarEmail(emailRemitente)) {
                JOptionPane.showMessageDialog(this,
                        "El email remitente no tiene un formato válido.",
                        "Email Inválido",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardar configuración
            EmailBoletinUtility.configurarEmail(smtpHost, smtpPort, emailRemitente,
                    passwordRemitente, nombreRemitente, useTLS, useAuth);

            JOptionPane.showMessageDialog(this,
                    "Configuración guardada exitosamente.\n\n"
                    + "Servidor: " + smtpHost + ":" + smtpPort + "\n"
                    + "Remitente: " + nombreRemitente + " <" + emailRemitente + ">",
                    "Configuración Guardada",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            System.err.println("Error guardando configuración: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al guardar la configuración: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void probarConfiguracion(ActionEvent e) {
        String emailPrueba = txtEmailPrueba.getText().trim();

        if (emailPrueba.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese un email para la prueba.",
                    "Email Requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!EmailBoletinUtility.validarEmail(emailPrueba)) {
            JOptionPane.showMessageDialog(this,
                    "El email de prueba no tiene un formato válido.",
                    "Email Inválido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Primero guardar la configuración actual
        guardarConfiguracion(null);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return EmailBoletinUtility.probarConfiguracionEmail(emailPrueba);
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    String mensaje = exito
                            ? "✅ Prueba exitosa!\n\nSe envió un email de prueba a:\n" + emailPrueba
                            + "\n\nVerifique su bandeja de entrada (y spam)."
                            : "❌ Error en la prueba.\n\nVerifique:\n"
                            + "• Configuración del servidor SMTP\n"
                            + "• Credenciales de email\n"
                            + "• Conexión a internet\n"
                            + "• Configuración de firewall";

                    JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                            mensaje,
                            "Resultado de la Prueba",
                            exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ConfiguracionEmailPanel.this,
                            "Error durante la prueba: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void mostrarEstadisticas(ActionEvent e) {
        String estadisticas = EmailBoletinUtility.obtenerEstadisticasEmail();

        javax.swing.JTextArea textArea = new javax.swing.JTextArea(estadisticas);
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(50);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this,
                new javax.swing.JScrollPane(textArea),
                "Estadísticas de Email",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
