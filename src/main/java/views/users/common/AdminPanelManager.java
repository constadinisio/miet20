/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import main.java.utils.GestorBoletines;
import main.java.views.users.Admin.GestionCursosPanel;
import main.java.views.users.Admin.GestionUsuariosPanel;
import main.java.views.users.Admin.UsuariosPendientesPanel;
import main.java.views.users.common.NotasVisualizationPanel;

/**
 * Gestor de paneles específico para el rol de Administrador.
 */
public class AdminPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int userId;

    /**
     * Constructor del gestor de paneles para administradores.
     *
     * @param ventana Ventana principal
     * @param userId ID del usuario
     */
    public AdminPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.userId = userId;
    }

    @Override
    public JComponent[] createButtons() {
        // Crear botones específicos para el rol de administrador
        JButton btnUsuariosPendientes = createStyledButton("USUARIOS PENDIENTES", "usuariosPendientes");
        JButton btnGestionUsuarios = createStyledButton("GESTIÓN USUARIOS", "gestionUsuarios");
        JButton btnGestionCursos = createStyledButton("GESTIÓN CURSOS", "gestionCursos");
        JButton btnVisualizarNotas = createStyledButton("VISUALIZAR NOTAS", "notas");
        JButton btnGestionBoletines = createStyledButton("GESTIÓN BOLETINES", "gestionBoletines");
        JButton btnEstructuraBoletines = createStyledButton("ESTRUCTURA BOLETINES", "estructuraBoletines");

        // Retornar array de botones
        return new JComponent[]{
            btnUsuariosPendientes,
            btnGestionUsuarios,
            btnGestionCursos,
            btnVisualizarNotas,
            btnGestionBoletines,
            btnEstructuraBoletines
        };
    }

    /**
     * Crea un botón con el estilo estándar de la aplicación.
     *
     * @param text Texto del botón
     * @param actionCommand Comando de acción para identificar el botón
     * @return Botón configurado
     */
    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        // Establecer dimensiones preferidas
        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        // Obtener el panel principal
        javax.swing.JPanel panelPrincipal = ventana.getPanelPrincipal();

        // Remover el contenido actual
        panelPrincipal.removeAll();

        // Definir el layout adecuado
        panelPrincipal.setLayout(new java.awt.BorderLayout());

        try {
            // Añadir el panel correspondiente según el comando de acción
            switch (actionCommand) {
                case "usuariosPendientes":
                    main.java.views.users.Admin.UsuariosPendientesPanel panelUsuariosPendientes
                            = new main.java.views.users.Admin.UsuariosPendientesPanel();
                    panelPrincipal.add(panelUsuariosPendientes, java.awt.BorderLayout.CENTER);
                    break;

                case "gestionUsuarios":
                    main.java.views.users.Admin.GestionUsuariosPanel panelGestionUsuarios
                            = new main.java.views.users.Admin.GestionUsuariosPanel();
                    panelPrincipal.add(panelGestionUsuarios, java.awt.BorderLayout.CENTER);
                    break;

                case "gestionCursos":
                    main.java.views.users.Admin.GestionCursosPanel panelGestionCursos
                            = new main.java.views.users.Admin.GestionCursosPanel();
                    panelPrincipal.add(panelGestionCursos, java.awt.BorderLayout.CENTER);
                    break;

                case "notas":
                    main.java.views.users.common.NotasVisualizationPanel panelNotas
                            = new main.java.views.users.common.NotasVisualizationPanel(ventana, userId, 1); // rol 1 = admin
                    panelPrincipal.add(panelNotas, java.awt.BorderLayout.CENTER);
                    break;
                case "gestionBoletines":
                    main.java.views.users.common.PanelGestionBoletines panelBoletines
                            = new main.java.views.users.common.PanelGestionBoletines(ventana, userId, 1); // rol 1 = admin
                    panelPrincipal.add(panelBoletines, java.awt.BorderLayout.CENTER);
                    break;
                case "estructuraBoletines":
                    mostrarGestionEstructuraBoletines();
                    break;

                default:
                    // Si no reconoce el comando, restaurar vista principal
                    ventana.restaurarVistaPrincipal();
                    break;
            }

            // Actualizar el panel
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(ventana,
                    "Error al cargar el panel: " + ex.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();

            // En caso de error, restaurar la vista principal
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de gestión de estructura de boletines (solo para
     * administradores)
     */
    private void mostrarGestionEstructuraBoletines() {
        try {
            System.out.println("=== GESTIÓN DE ESTRUCTURA DE BOLETINES (SERVIDOR) ===");

            // Crear panel principal
            JPanel panelPrincipal = new JPanel(new java.awt.BorderLayout());
            panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Título
            JLabel lblTitulo = new JLabel("Gestión de Estructura de Boletines - Servidor", JLabel.CENTER);
            lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            lblTitulo.setForeground(new java.awt.Color(51, 153, 255));
            panelPrincipal.add(lblTitulo, java.awt.BorderLayout.NORTH);

            // Panel central con opciones
            JPanel panelCentral = new JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(15, 15, 15, 15);
            gbc.anchor = java.awt.GridBagConstraints.CENTER;

            // Información actual
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JPanel panelInfo = crearPanelInformacionServidor();
            panelCentral.add(panelInfo, gbc);

            // Botones de acción - SIMPLIFICADOS
            gbc.gridwidth = 1;
            gbc.gridy = 1;

            gbc.gridx = 0;
            JButton btnConfigurarServidor = createStyledButton("CONFIGURAR SERVIDOR", "");
            btnConfigurarServidor.setPreferredSize(new java.awt.Dimension(200, 50));
            btnConfigurarServidor.addActionListener(e -> configurarServidorBoletines());
            panelCentral.add(btnConfigurarServidor, gbc);

            gbc.gridx = 1;
            JButton btnCrearEstructura = createStyledButton("CREAR ESTRUCTURA BD", "");
            btnCrearEstructura.setPreferredSize(new java.awt.Dimension(200, 50));
            btnCrearEstructura.addActionListener(e -> crearEstructuraBaseDatos());
            panelCentral.add(btnCrearEstructura, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            JButton btnVerificarConexion = createStyledButton("VERIFICAR SERVIDOR", "");
            btnVerificarConexion.setPreferredSize(new java.awt.Dimension(200, 50));
            btnVerificarConexion.addActionListener(e -> verificarConexionServidor());
            panelCentral.add(btnVerificarConexion, gbc);

            gbc.gridx = 1;
            JButton btnVolver = createStyledButton("VOLVER", "");
            btnVolver.setPreferredSize(new java.awt.Dimension(200, 50));
            btnVolver.setBackground(new java.awt.Color(96, 125, 139));
            btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());
            panelCentral.add(btnVolver, gbc);

            panelPrincipal.add(panelCentral, java.awt.BorderLayout.CENTER);

            // Mostrar en la ventana principal
            ventana.getPanelPrincipal().removeAll();
            ventana.getPanelPrincipal().add(panelPrincipal);
            ventana.getPanelPrincipal().revalidate();
            ventana.getPanelPrincipal().repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al mostrar gestión de estructura: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Crea el panel de información del servidor (CORREGIDO)
     */
    private JPanel crearPanelInformacionServidor() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Estado Actual del Sistema"));
        panel.setPreferredSize(new java.awt.Dimension(500, 150));

        // Obtener información actual
        String rutaServidor = main.java.utils.GestorBoletines.obtenerRutaServidor();
        int anioActual = java.time.LocalDate.now().getYear();

        StringBuilder info = new StringBuilder();
        info.append("🌐 Servidor de boletines: ").append(rutaServidor).append("\n");
        info.append("📅 Año actual: ").append(anioActual).append("\n");

        // Verificar si existe estructura en BD
        boolean estructuraExiste = main.java.utils.GestorBoletines.verificarEstructuraCarpetas(anioActual);
        if (estructuraExiste) {
            info.append("✅ Estructura BD ").append(anioActual).append(": Configurada\n");
        } else {
            info.append("❌ Estructura BD ").append(anioActual).append(": No configurada\n");
        }

        info.append("\n💡 IMPORTANTE:\n");
        info.append("• El sistema trabaja SOLO con servidor web\n");
        info.append("• No se crean carpetas locales\n");
        info.append("• Los boletines se suben al servidor configurado\n");

        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setBackground(panel.getBackground());
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

        panel.add(new JScrollPane(textArea), java.awt.BorderLayout.CENTER);

        return panel;
    }

    /**
     * Configura el servidor de boletines (CORREGIDO)
     */
    private void configurarServidorBoletines() {
        try {
            JPanel panel = new JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(5, 5, 5, 5);
            gbc.anchor = java.awt.GridBagConstraints.WEST;

            // Información actual
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(new JLabel("=== CONFIGURACIÓN DEL SERVIDOR DE BOLETINES ==="), gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            panel.add(new JLabel("URL actual del servidor:"), gbc);

            JTextField txtRutaActual = new JTextField(GestorBoletines.obtenerRutaServidor(), 50);
            txtRutaActual.setEditable(false);
            txtRutaActual.setBackground(java.awt.Color.LIGHT_GRAY);
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            panel.add(txtRutaActual, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            panel.add(new JLabel("Nueva URL del servidor:"), gbc);

            JTextField txtNuevaRuta = new JTextField(50);
            txtNuevaRuta.setToolTipText("Ejemplo: http://10.120.1.109/miet20/boletines/");
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            panel.add(txtNuevaRuta, gbc);

            // Opciones de configuración
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            panel.add(new JLabel("Configuración:"), gbc);

            JCheckBox chkCrearCarpetas = new JCheckBox("Crear carpetas físicas automáticamente", true);
            chkCrearCarpetas.setToolTipText("Si está marcado, intentará crear las carpetas en el servidor");
            gbc.gridx = 0;
            gbc.gridy = 6;
            panel.add(chkCrearCarpetas, gbc);

            JCheckBox chkValidarConexion = new JCheckBox("Validar conexión al servidor", true);
            chkValidarConexion.setToolTipText("Verificar que el servidor es accesible");
            gbc.gridx = 0;
            gbc.gridy = 7;
            panel.add(chkValidarConexion, gbc);

            // Información adicional
            gbc.gridx = 0;
            gbc.gridy = 8;
            gbc.gridwidth = 2;
            JLabel lblInfo = new JLabel("<html><i>Nota: Asegúrese de que el servidor tenga permisos de escritura<br>"
                    + "y que el script crear_carpeta.php esté disponible</i></html>");
            lblInfo.setForeground(java.awt.Color.BLUE);
            panel.add(lblInfo, gbc);

            // Mostrar diálogo
            int result = JOptionPane.showConfirmDialog(ventana, panel,
                    "Configuración del Servidor de Boletines",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String nuevaUrl = txtNuevaRuta.getText().trim();
                if (!nuevaUrl.isEmpty()) {
                    // Validar que sea una URL
                    if (!nuevaUrl.startsWith("http://") && !nuevaUrl.startsWith("https://")) {
                        JOptionPane.showMessageDialog(ventana,
                                "La URL debe comenzar con http:// o https://",
                                "URL Inválida",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Asegurar que termine con /
                    if (!nuevaUrl.endsWith("/")) {
                        nuevaUrl += "/";
                    }

                    // Validar conexión si está marcado
                    if (chkValidarConexion.isSelected()) {
                        if (!validarConexionServidor(nuevaUrl)) {
                            int continuar = JOptionPane.showConfirmDialog(ventana,
                                    "No se pudo validar la conexión al servidor.\n"
                                    + "¿Desea continuar de todos modos?",
                                    "Conexión no validada",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);

                            if (continuar != JOptionPane.YES_OPTION) {
                                return;
                            }
                        }
                    }

                    // Configurar la nueva URL
                    GestorBoletines.configurarRutaServidor(nuevaUrl);

                    String mensaje = "Servidor configurado exitosamente:\n" + nuevaUrl;

                    // Crear carpetas si está marcado
                    if (chkCrearCarpetas.isSelected()) {
                        mensaje += "\n\nCreando estructura de carpetas...";
                        JOptionPane.showMessageDialog(ventana, mensaje, "Configuración Completada", JOptionPane.INFORMATION_MESSAGE);

                        // Crear estructura para el año actual
                        int anioActual = java.time.LocalDate.now().getYear();
                        boolean estructuraCreada = GestorBoletines.generarEstructuraCompleta(anioActual);

                        if (estructuraCreada) {
                            JOptionPane.showMessageDialog(ventana,
                                    "Estructura de carpetas creada exitosamente para " + anioActual,
                                    "Carpetas Creadas",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(ventana,
                                    "El servidor se configuró, pero hubo problemas al crear las carpetas.\n"
                                    + "Puede crearlas manualmente o usar la opción 'CREAR ESTRUCTURA BD'.",
                                    "Carpetas - Advertencia",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(ventana, mensaje, "Configuración Completada", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // Actualizar panel de información
                    mostrarGestionEstructuraBoletines();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al configurar servidor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Valida si el servidor es accesible
     */
    private boolean validarConexionServidor(String urlServidor) {
        try {
            System.out.println("Validando conexión a: " + urlServidor);

            java.net.URL url = new java.net.URL(urlServidor);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 segundos
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            // 200 = OK, 403 = Forbidden (pero servidor existe), 404 = Not Found (pero servidor responde)
            boolean accesible = (responseCode >= 200 && responseCode < 500);

            System.out.println("Código de respuesta: " + responseCode + " - Accesible: " + accesible);
            return accesible;

        } catch (Exception e) {
            System.err.println("Error validando conexión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea la estructura en base de datos (CORREGIDO)
     */
    /**
     * Crear estructura completa (desde BD) - VERSIÓN CORREGIDA SOLO SERVIDOR
     */
    private void crearEstructuraBaseDatos() {
        try {
            String anioStr = JOptionPane.showInputDialog(ventana,
                    "Ingrese el año lectivo para configurar en BD:",
                    "Configurar Estructura en BD",
                    JOptionPane.QUESTION_MESSAGE);

            if (anioStr == null || anioStr.trim().isEmpty()) {
                return;
            }

            int anio;
            try {
                anio = Integer.parseInt(anioStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(ventana,
                        "Año inválido: " + anioStr,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(ventana,
                    "¿Configurar estructura en BD para " + anio + "?\n\n"
                    + "Esto preparará el sistema para generar boletines\n"
                    + "basándose en los cursos activos en la base de datos.\n\n"
                    + "Servidor: " + GestorBoletines.obtenerRutaServidor(),
                    "Confirmar Configuración",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion == JOptionPane.YES_OPTION) {
                // Crear barra de progreso
                javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar();
                progressBar.setStringPainted(true);
                progressBar.setString("Configurando...");
                progressBar.setIndeterminate(true);

                javax.swing.JDialog progressDialog = new javax.swing.JDialog(ventana, "Configurando Sistema", true);
                progressDialog.setLayout(new java.awt.BorderLayout());
                progressDialog.add(new JLabel("Configurando estructura de boletines...", JLabel.CENTER), java.awt.BorderLayout.NORTH);
                progressDialog.add(progressBar, java.awt.BorderLayout.CENTER);
                progressDialog.setSize(400, 100);
                progressDialog.setLocationRelativeTo(ventana);

                javax.swing.SwingWorker<Boolean, String> worker = new javax.swing.SwingWorker<Boolean, String>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        publish("Configurando estructura en BD...");
                        // SOLO configuración en BD, sin crear carpetas físicas
                        return GestorBoletines.generarEstructuraCompleta(anio);
                    }

                    @Override
                    protected void process(java.util.List<String> chunks) {
                        for (String message : chunks) {
                            progressBar.setString(message);
                        }
                    }

                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        try {
                            boolean exito = get();

                            if (exito) {
                                JOptionPane.showMessageDialog(ventana,
                                        "¡Estructura configurada exitosamente en BD!\n\n"
                                        + "Año: " + anio + "\n"
                                        + "Servidor: " + GestorBoletines.obtenerRutaServidor() + "\n\n"
                                        + "El sistema está listo para generar boletines.\n"
                                        + "Los archivos se subirán al servidor manualmente.",
                                        "Configuración Completada",
                                        JOptionPane.INFORMATION_MESSAGE);

                                mostrarGestionEstructuraBoletines();
                            } else {
                                JOptionPane.showMessageDialog(ventana,
                                        "Hubo errores durante la configuración.\n"
                                        + "Revise la consola para más detalles.",
                                        "Error en Configuración",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(ventana,
                                    "Error durante la configuración: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

                worker.execute();
                progressDialog.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al configurar estructura: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ejecuta la configuración de estructura
     */
    private void ejecutarConfiguracionEstructura(int anio) {
        javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Configurando estructura...");
        progressBar.setIndeterminate(true);

        javax.swing.JDialog progressDialog = new javax.swing.JDialog(ventana, "Configurando Sistema", true);
        progressDialog.setLayout(new java.awt.BorderLayout());
        progressDialog.add(new JLabel("Configurando estructura de boletines...", JLabel.CENTER), java.awt.BorderLayout.NORTH);
        progressDialog.add(progressBar, java.awt.BorderLayout.CENTER);
        progressDialog.setSize(400, 100);
        progressDialog.setLocationRelativeTo(ventana);

        javax.swing.SwingWorker<Boolean, String> worker = new javax.swing.SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Configurando estructura en BD...");
                return GestorBoletines.generarEstructuraCompleta(anio);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    progressBar.setString(message);
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean exito = get();

                    if (exito) {
                        JOptionPane.showMessageDialog(ventana,
                                "¡Estructura configurada exitosamente!\n\n"
                                + "Año: " + anio + "\n"
                                + "Servidor: " + GestorBoletines.obtenerRutaServidor() + "\n\n"
                                + "El sistema está listo para generar boletines.",
                                "Configuración Completada",
                                JOptionPane.INFORMATION_MESSAGE);

                        mostrarGestionEstructuraBoletines();
                    } else {
                        JOptionPane.showMessageDialog(ventana,
                                "Hubo errores durante la configuración.\n"
                                + "Revise la consola para más detalles.",
                                "Error en Configuración",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ventana,
                            "Error durante la configuración: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    /**
     * Verifica la conexión con el servidor
     */
    private void verificarConexionServidor() {
        try {
            String rutaServidor = GestorBoletines.obtenerRutaServidor();

            StringBuilder resultado = new StringBuilder();
            resultado.append("=== VERIFICACIÓN DEL SERVIDOR ===\n");
            resultado.append("URL del servidor: ").append(rutaServidor).append("\n\n");

            // Verificar formato de URL
            if (rutaServidor.startsWith("http://") || rutaServidor.startsWith("https://")) {
                resultado.append("✅ Formato de URL: Válido\n");
            } else {
                resultado.append("❌ Formato de URL: Inválido\n");
            }

            // Verificar estructura en BD
            int anioActual = java.time.LocalDate.now().getYear();
            boolean estructuraOK = GestorBoletines.verificarEstructuraCarpetas(anioActual);

            if (estructuraOK) {
                resultado.append("✅ Estructura en BD: Configurada\n");
            } else {
                resultado.append("❌ Estructura en BD: Falta configurar\n");
            }

            resultado.append("\n=== INFORMACIÓN DEL SISTEMA ===\n");
            resultado.append("• El sistema funciona SOLO con servidor web\n");
            resultado.append("• No se crean carpetas locales\n");
            resultado.append("• Los boletines se registran en BD\n");
            resultado.append("• Los archivos deben subirse al servidor manualmente\n");
            resultado.append("• Use la función 'Generar Boletines' para crear archivos temporales\n");

            JTextArea textArea = new JTextArea(resultado.toString());
            textArea.setEditable(false);
            textArea.setRows(15);
            textArea.setColumns(50);
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(ventana,
                    scrollPane,
                    "Verificación del Servidor",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al verificar servidor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
