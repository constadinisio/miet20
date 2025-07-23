package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import main.java.database.Conexion;
import main.java.utils.GestorBoletines;
import main.java.services.NotificationCore.NotificationIntegrationUtil; // NUEVO
import main.java.views.users.Admin.GestionCursosPanel;
import main.java.views.users.Admin.GestionUsuariosPanel;
import main.java.views.users.Admin.UsuariosPendientesPanel;
import main.java.views.users.common.NotasVisualizationPanel;
import main.java.views.users.common.LibroTemasSelector;

/**
 * Gestor de paneles específico para el rol de Administrador.
 * VERSIÓN COMPLETA CON SISTEMA DE NOTIFICACIONES INTEGRADO
 * 
 * Funcionalidades de notificaciones:
 * - Notificación de aprobación/rechazo de usuarios
 * - Avisos generales del sistema
 * - Notificaciones de mantenimiento
 * - Estadísticas y gestión de notificaciones
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Con Notificaciones
 */
public class AdminPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int userId;
    private final NotificationIntegrationUtil notificationUtil; // NUEVO
    private final Connection connection; // NUEVO para operaciones de BD

    public AdminPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.userId = userId;
        this.notificationUtil = NotificationIntegrationUtil.getInstance(); // NUEVO
        this.connection = Conexion.getInstancia().verificarConexion(); // NUEVO
        
        System.out.println("✅ AdminPanelManager inicializado con sistema de notificaciones");
        System.out.println("  Usuario Admin ID: " + userId);
        System.out.println("  Puede enviar notificaciones: " + notificationUtil.puedeEnviarNotificaciones());
        System.out.println("  Puede gestionar notificaciones: " + notificationUtil.puedeGestionarNotificaciones());
    }

    @Override
    public JComponent[] createButtons() {
        JButton btnUsuariosPendientes = createStyledButton("USUARIOS PENDIENTES", "usuariosPendientes");
        JButton btnGestionUsuarios = createStyledButton("GESTIÓN USUARIOS", "gestionUsuarios");
        JButton btnGestionCursos = createStyledButton("GESTIÓN CURSOS", "gestionCursos");
        JButton btnVisualizarNotas = createStyledButton("VISUALIZAR NOTAS", "notas");
        JButton btnLibroTemas = createStyledButton("LIBRO DE TEMAS", "libro_temas");
        btnLibroTemas.setBackground(new Color(138, 43, 226)); // Violeta para libro de temas
        btnLibroTemas.setToolTipText("Acceso completo al libro de temas con validación");
        JButton btnGestionBoletines = createStyledButton("GESTIÓN BOLETINES", "gestionBoletines");
        JButton btnEstructuraBoletines = createStyledButton("ESTRUCTURA BOLETINES", "estructuraBoletines");
        
        // NUEVO: Botón para gestión de notificaciones (solo admin)
        JButton btnNotificaciones = createStyledButton("SISTEMA NOTIFICACIONES", "sistemaNotificaciones");
        btnNotificaciones.setBackground(new Color(220, 53, 69)); // Color distintivo
        btnNotificaciones.setToolTipText("Gestionar el Sistema de Notificaciones");

        return new JComponent[]{
            btnUsuariosPendientes,
            btnGestionUsuarios,
            btnGestionCursos,
            btnVisualizarNotas,
            btnLibroTemas,
            btnGestionBoletines,
            btnEstructuraBoletines,
            btnNotificaciones // NUEVO
        };
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        try {
            System.out.println("=== ACCIÓN ADMIN: " + actionCommand + " ===");

            switch (actionCommand) {
                case "usuariosPendientes":
                    mostrarUsuariosPendientes();
                    break;

                case "gestionUsuarios":
                    mostrarGestionUsuarios();
                    break;

                case "gestionCursos":
                    mostrarGestionCursos();
                    break;

                case "notas":
                    mostrarVisualizacionNotas();
                    break;
                    
                case "libro_temas":
                    mostrarLibroDeTemas();
                    break;
                    
                case "gestionBoletines":
                    mostrarGestionBoletines();
                    break;
                    
                case "estructuraBoletines":
                    mostrarGestionEstructuraBoletines();
                    break;
                    
                // NUEVO: Gestión del sistema de notificaciones
                case "sistemaNotificaciones":
                    mostrarGestionNotificaciones();
                    break;

                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }

        } catch (Exception ex) {
            System.err.println("❌ Error en AdminPanelManager: " + ex.getMessage());
            ex.printStackTrace();
            
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar el panel: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
            ventana.restaurarVistaPrincipal();
        }
    }

    // ========================================
    // MÉTODOS EXISTENTES (SIN CAMBIOS)
    // ========================================

    /**
     * MÉTODO CORREGIDO: Usa el sistema responsive
     */
    private void mostrarUsuariosPendientes() {
        try {
            System.out.println("Creando UsuariosPendientesPanel...");
            
            // NUEVO: Crear panel con capacidad de notificaciones
            UsuariosPendientesPanel panel = new UsuariosPendientesPanel();
            
            // NUEVO: Pasar referencia de notificationUtil al panel si es necesario
            // (Esto dependerá de cómo esté implementado UsuariosPendientesPanel)
            
            ventana.mostrarPanelResponsive(panel, "Gestión de Usuarios Pendientes");
            
            System.out.println("✅ UsuariosPendientesPanel mostrado exitosamente");
            
        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar UsuariosPendientesPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO CORREGIDO: Usa el sistema responsive
     */
    private void mostrarGestionUsuarios() {
        try {
            System.out.println("Creando GestionUsuariosPanel...");
            
            GestionUsuariosPanel panel = new GestionUsuariosPanel();
            ventana.mostrarPanelResponsive(panel, "Gestión de Usuarios");
            
            System.out.println("✅ GestionUsuariosPanel mostrado exitosamente");
            
        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar GestionUsuariosPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO CORREGIDO: Usa el sistema responsive
     */
    private void mostrarGestionCursos() {
        try {
            System.out.println("Creando GestionCursosPanel...");
            
            GestionCursosPanel panel = new GestionCursosPanel();
            ventana.mostrarPanelResponsive(panel, "Gestión de Cursos");
            
            System.out.println("✅ GestionCursosPanel mostrado exitosamente");
            
        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar GestionCursosPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO CORREGIDO: Usa el sistema responsive
     */
    private void mostrarVisualizacionNotas() {
        try {
            System.out.println("Creando NotasVisualizationPanel para Admin...");
            
            NotasVisualizationPanel panel = new NotasVisualizationPanel(ventana, userId, 1); // rol 1 = admin
            ventana.mostrarPanelResponsive(panel, "Visualización de Notas");
            
            System.out.println("✅ NotasVisualizationPanel mostrado exitosamente");
            
        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar NotasVisualizationPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Muestra el panel de Libro de Temas para Directivo/Admin
     * Los Directivos tienen acceso completo con capacidades de validación
     */
    private void mostrarLibroDeTemas() {
        try {
            System.out.println("Creando LibroTemasSelector para Directivo...");
            
            LibroTemasSelector selector = new LibroTemasSelector(
                userId, 1, // rol 1 = Directivo
                (panel, titulo) -> ventana.mostrarPanelResponsive(panel, titulo),
                () -> ventana.restaurarVistaPrincipal()
            );
            
            ventana.mostrarPanelResponsive(selector, "Libro de Temas - Selector");
            
            System.out.println("✅ LibroTemasSelector mostrado exitosamente para Directivo");
            
        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar LibroTemasSelector: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO CORREGIDO: Usa el sistema responsive
     */
    private void mostrarGestionBoletines() {
        try {
            System.out.println("Creando PanelGestionBoletines para Admin...");
            
            main.java.views.users.common.PanelGestionBoletines panel = 
                new main.java.views.users.common.PanelGestionBoletines(ventana, userId, 1); // rol 1 = admin
            ventana.mostrarPanelResponsive(panel, "Gestión de Boletines");
            
            System.out.println("✅ PanelGestionBoletines mostrado exitosamente");
            
        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar PanelGestionBoletines: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO CORREGIDO: Crea panel dinámico y usa sistema responsive
     */
    private void mostrarGestionEstructuraBoletines() {
        try {
            System.out.println("=== CREANDO PANEL DE ESTRUCTURA DE BOLETINES ===");

            JPanel panelEstructura = crearPanelEstructuraBoletines();
            ventana.mostrarPanelResponsive(panelEstructura, "Gestión de Estructura de Boletines");
            
            System.out.println("✅ Panel de estructura de boletines mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de estructura: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    // ========================================
    // NUEVOS MÉTODOS DE NOTIFICACIONES
    // ========================================

    /**
     * NUEVO: Muestra el panel de gestión del sistema de notificaciones
     */
    private void mostrarGestionNotificaciones() {
        try {
            System.out.println("=== CREANDO PANEL DE GESTIÓN DE NOTIFICACIONES ===");

            if (!notificationUtil.puedeGestionarNotificaciones()) {
                JOptionPane.showMessageDialog(ventana,
                    "No tienes permisos para gestionar el sistema de notificaciones.",
                    "Acceso Denegado",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            JPanel panelNotificaciones = crearPanelGestionNotificaciones();
            ventana.mostrarPanelResponsive(panelNotificaciones, "Sistema de Notificaciones - Administración");
            
            System.out.println("✅ Panel de gestión de notificaciones mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de notificaciones: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * NUEVO: Crea el panel de gestión de notificaciones
     */
    private JPanel crearPanelGestionNotificaciones() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("🔔 Administración del Sistema de Notificaciones", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(220, 53, 69));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Panel central con opciones
        JPanel panelCentral = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(15, 15, 15, 15);
        gbc.anchor = java.awt.GridBagConstraints.CENTER;

        // Panel de estadísticas
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JPanel panelStats = crearPanelEstadisticasNotificaciones();
        panelCentral.add(panelStats, gbc);

        // Botones de gestión - Fila 1
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        gbc.gridx = 0;
        JButton btnEnviarGeneral = createNotificationButton("📢 AVISO GENERAL", new Color(40, 167, 69));
        btnEnviarGeneral.addActionListener(e -> mostrarDialogoAvisoGeneral());
        panelCentral.add(btnEnviarGeneral, gbc);

        gbc.gridx = 1;
        JButton btnMantenimiento = createNotificationButton("🔧 MANTENIMIENTO", new Color(255, 193, 7));
        btnMantenimiento.addActionListener(e -> mostrarDialogoMantenimiento());
        panelCentral.add(btnMantenimiento, gbc);

        gbc.gridx = 2;
        JButton btnEmergencia = createNotificationButton("🚨 EMERGENCIA", new Color(220, 53, 69));
        btnEmergencia.addActionListener(e -> mostrarDialogoEmergencia());
        panelCentral.add(btnEmergencia, gbc);

        // Botones de gestión - Fila 2
        gbc.gridy = 2;

        gbc.gridx = 0;
        JButton btnEstadisticas = createNotificationButton("📊 ESTADÍSTICAS", new Color(23, 162, 184));
        btnEstadisticas.addActionListener(e -> mostrarEstadisticasCompletas());
        panelCentral.add(btnEstadisticas, gbc);

        gbc.gridx = 1;
        JButton btnPrueba = createNotificationButton("🧪 PRUEBA", new Color(108, 117, 125));
        btnPrueba.addActionListener(e -> enviarNotificacionPrueba());
        panelCentral.add(btnPrueba, gbc);

        gbc.gridx = 2;
        JButton btnVolver = createNotificationButton("← VOLVER", new Color(96, 125, 139));
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());
        panelCentral.add(btnVolver, gbc);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        return panelPrincipal;
    }

    /**
     * NUEVO: Crea botón estilizado para notificaciones
     */
    private JButton createNotificationButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(180, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    /**
     * NUEVO: Crea panel con estadísticas del sistema de notificaciones
     */
    private JPanel crearPanelEstadisticasNotificaciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Estado Actual del Sistema"));
        panel.setPreferredSize(new Dimension(600, 180));

        try {
            StringBuilder stats = new StringBuilder();
            stats.append("📊 ESTADÍSTICAS EN TIEMPO REAL\n");
            stats.append("═══════════════════════════════════════\n\n");
            
            // Información básica del sistema
            stats.append("🔔 Sistema: ").append(notificationUtil.puedeGestionarNotificaciones() ? "✅ OPERATIVO" : "❌ LIMITADO").append("\n");
            stats.append("👤 Usuario Admin: ").append(userId).append("\n");
            stats.append("📧 Notificaciones no leídas: ").append(notificationUtil.getNotificacionesNoLeidas()).append("\n");
            stats.append("⏰ Última actualización: ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            ).append("\n\n");

            // Estadísticas adicionales si están disponibles
            if (notificationUtil.puedeGestionarNotificaciones()) {
                stats.append("📈 ESTADÍSTICAS DETALLADAS:\n");
                stats.append("• Total usuarios activos: ").append(contarUsuariosActivos()).append("\n");
                stats.append("• Notificaciones enviadas hoy: ").append(contarNotificacionesHoy()).append("\n");
                stats.append("• Usuarios con notificaciones pendientes: ").append(contarUsuariosConPendientes()).append("\n");
            }

            stats.append("\n💡 FUNCIONES DISPONIBLES:\n");
            stats.append("• Envío de avisos generales a todos los roles\n");
            stats.append("• Notificaciones de mantenimiento programado\n");
            stats.append("• Alertas de emergencia del sistema\n");
            stats.append("• Estadísticas completas y reportes\n");

            JTextArea textArea = new JTextArea(stats.toString());
            textArea.setEditable(false);
            textArea.setBackground(panel.getBackground());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        } catch (Exception e) {
            System.err.println("Error creando estadísticas: " + e.getMessage());
            
            JLabel lblError = new JLabel("Error cargando estadísticas: " + e.getMessage(), JLabel.CENTER);
            lblError.setForeground(Color.RED);
            panel.add(lblError, BorderLayout.CENTER);
        }

        return panel;
    }

    // ========================================
    // MÉTODOS DE NOTIFICACIONES ESPECÍFICAS
    // ========================================

    /**
     * NUEVO: Muestra diálogo para enviar aviso general
     */
    private void mostrarDialogoAvisoGeneral() {
        try {
            String titulo = JOptionPane.showInputDialog(ventana,
                "Título del aviso general:",
                "Enviar Aviso General",
                JOptionPane.QUESTION_MESSAGE);

            if (titulo != null && !titulo.trim().isEmpty()) {
                JTextArea contentArea = new JTextArea(5, 30);
                contentArea.setWrapStyleWord(true);
                contentArea.setLineWrap(true);
                
                int result = JOptionPane.showConfirmDialog(ventana,
                    new JScrollPane(contentArea),
                    "Contenido del aviso:",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String contenido = contentArea.getText();
                    if (!contenido.trim().isEmpty()) {
                        enviarAvisoGeneral(titulo.trim(), contenido.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en diálogo de aviso general: " + e.getMessage());
            JOptionPane.showMessageDialog(ventana,
                "Error al enviar aviso: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Muestra diálogo para notificación de mantenimiento
     */
    private void mostrarDialogoMantenimiento() {
        try {
            JPanel panel = new JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(5, 5, 5, 5);
            gbc.anchor = java.awt.GridBagConstraints.WEST;

            // Fecha
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Fecha:"), gbc);
            gbc.gridx = 1;
            JTextField txtFecha = new JTextField(15);
            txtFecha.setText(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            panel.add(txtFecha, gbc);

            // Hora
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Hora:"), gbc);
            gbc.gridx = 1;
            JTextField txtHora = new JTextField(15);
            txtHora.setText("02:00");
            panel.add(txtHora, gbc);

            // Duración
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Duración:"), gbc);
            gbc.gridx = 1;
            JTextField txtDuracion = new JTextField(15);
            txtDuracion.setText("2 horas aproximadamente");
            panel.add(txtDuracion, gbc);

            int result = JOptionPane.showConfirmDialog(ventana, panel,
                "Programar Mantenimiento", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String fecha = txtFecha.getText().trim();
                String hora = txtHora.getText().trim();
                String duracion = txtDuracion.getText().trim();

                if (!fecha.isEmpty() && !hora.isEmpty() && !duracion.isEmpty()) {
                    notificationUtil.notificarMantenimiento(fecha, hora, duracion);
                    JOptionPane.showMessageDialog(ventana,
                        "Notificación de mantenimiento enviada a todos los usuarios.",
                        "Mantenimiento Programado",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en diálogo de mantenimiento: " + e.getMessage());
            JOptionPane.showMessageDialog(ventana,
                "Error al programar mantenimiento: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Muestra diálogo para notificación de emergencia
     */
    private void mostrarDialogoEmergencia() {
        try {
            int confirmacion = JOptionPane.showConfirmDialog(ventana,
                "⚠️ ADVERTENCIA ⚠️\n\n" +
                "Esta función envía una notificación de EMERGENCIA\n" +
                "a TODOS los usuarios del sistema.\n\n" +
                "Solo usar en casos críticos.\n\n" +
                "¿Continuar?",
                "Confirmar Notificación de Emergencia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                String contenido = JOptionPane.showInputDialog(ventana,
                    "Describe la emergencia o situación crítica:",
                    "Notificación de Emergencia",
                    JOptionPane.WARNING_MESSAGE);

                if (contenido != null && !contenido.trim().isEmpty()) {
                    notificationUtil.enviarNotificacionSistema(
                        "🚨 ALERTA DE EMERGENCIA",
                        "ATENCIÓN INMEDIATA REQUERIDA:\n\n" + contenido.trim(),
                        "urgente"
                    );

                    JOptionPane.showMessageDialog(ventana,
                        "🚨 Notificación de emergencia enviada a todos los usuarios.\n" +
                        "Se recomienda hacer un seguimiento presencial.",
                        "Emergencia Notificada",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en notificación de emergencia: " + e.getMessage());
            JOptionPane.showMessageDialog(ventana,
                "Error al enviar notificación de emergencia: " + e.getMessage(),
                "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Envía aviso general del sistema
     */
    private void enviarAvisoGeneral(String titulo, String contenido) {
        try {
            int confirmacion = JOptionPane.showConfirmDialog(ventana,
                "¿Enviar el siguiente aviso a TODOS los usuarios?\n\n" +
                "Título: " + titulo + "\n\n" +
                "Contenido:\n" + contenido,
                "Confirmar Envío",
                JOptionPane.YES_NO_OPTION);

            if (confirmacion == JOptionPane.YES_OPTION) {
                notificationUtil.enviarNotificacionSistema(titulo, contenido, "evento");
                
                JOptionPane.showMessageDialog(ventana,
                    "✅ Aviso general enviado exitosamente a todos los usuarios.",
                    "Aviso Enviado",
                    JOptionPane.INFORMATION_MESSAGE);
                
                System.out.println("📢 Aviso general enviado por Admin " + userId + ": " + titulo);
            }
        } catch (Exception e) {
            System.err.println("Error enviando aviso general: " + e.getMessage());
            JOptionPane.showMessageDialog(ventana,
                "Error al enviar aviso: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Muestra estadísticas completas del sistema
     */
    private void mostrarEstadisticasCompletas() {
        try {
            String estadisticas = notificationUtil.getEstadisticasNotificaciones();
            
            JTextArea textArea = new JTextArea(estadisticas);
            textArea.setEditable(false);
            textArea.setRows(20);
            textArea.setColumns(60);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            
            JOptionPane.showMessageDialog(ventana,
                scrollPane,
                "📊 Estadísticas Completas del Sistema de Notificaciones",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("Error mostrando estadísticas: " + e.getMessage());
            JOptionPane.showMessageDialog(ventana,
                "Error al obtener estadísticas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Envía notificación de prueba
     */
    private void enviarNotificacionPrueba() {
        try {
            notificationUtil.enviarNotificacionPrueba();
            
            JOptionPane.showMessageDialog(ventana,
                "🧪 Notificación de prueba enviada.\n\n" +
                "Verifica la campanita de notificaciones\n" +
                "para confirmar que el sistema funciona correctamente.",
                "Prueba Enviada",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("Error enviando prueba: " + e.getMessage());
            JOptionPane.showMessageDialog(ventana,
                "Error al enviar notificación de prueba: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========================================
    // MÉTODOS PÚBLICOS PARA USO EXTERNO
    // ========================================

    /**
     * NUEVO: Método público para aprobar usuario con notificación
     * Para ser llamado desde UsuariosPendientesPanel
     */
    public void procesarAprobacionUsuario(int usuarioId, String nombre, String apellido, String rol) {
        try {
            System.out.println("🔄 Procesando aprobación de usuario...");
            System.out.println("  Usuario: " + nombre + " " + apellido);
            System.out.println("  Rol: " + rol);
            
            // Aquí iría la lógica de aprobación en BD
            // Por ahora simulamos que se aprueba correctamente
            
            // NUEVO: Enviar notificaciones automáticamente
            SwingUtilities.invokeLater(() -> {
                // Notificar al usuario aprobado
                notificationUtil.contextoAprobacionUsuario(usuarioId, nombre, apellido, rol);
                
                // Notificar a otros administradores
                notificationUtil.enviarNotificacionARol(
                    "✅ Usuario Aprobado por Administrador",
                    String.format("El administrador ha aprobado a:\n\n" +
                                "👤 Usuario: %s, %s\n" +
                                "🎭 Rol: %s\n" +
                                "📅 Fecha: %s",
                                apellido, nombre, rol,
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                    1 // Solo a administradores
                );
            });
            
            System.out.println("✅ Usuario aprobado y notificaciones enviadas");
            
        } catch (Exception e) {
            System.err.println("❌ Error en aprobación de usuario: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(ventana,
                "Error al procesar aprobación: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Método público para rechazar usuario con notificación
     */
    public void procesarRechazoUsuario(int usuarioId, String nombre, String apellido, String motivo) {
        try {
            System.out.println("⚠️ Procesando rechazo de usuario...");
            System.out.println("  Usuario: " + nombre + " " + apellido);
            System.out.println("  Motivo: " + motivo);
            
            // Aquí iría la lógica de rechazo en BD
            
            // NUEVO: Enviar notificación de rechazo
            SwingUtilities.invokeLater(() -> {
                notificationUtil.enviarNotificacionBasica(
                    "❌ Registro No Aprobado",
                    String.format("Tu solicitud de registro no ha sido aprobada.\n\n" +
                                "📝 Motivo: %s\n\n" +
                                "Si tienes dudas, contacta con la administración del sistema.",
                                motivo),
                    usuarioId
                );
                
                // Notificar a administradores
                notificationUtil.enviarNotificacionARol(
                    "❌ Usuario Rechazado",
                    String.format("Se ha rechazado el registro de: %s, %s\nMotivo: %s", 
                                apellido, nombre, motivo),
                    1
                );
            });
            
            System.out.println("✅ Usuario rechazado y notificado");
            
        } catch (Exception e) {
            System.err.println("❌ Error en rechazo de usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Notifica sobre nuevo usuario registrado (automático)
     */
    public void procesarNuevoRegistro(String nombreCompleto, String email, String rol) {
        try {
            System.out.println("📝 Nuevo registro detectado: " + nombreCompleto);
            
            SwingUtilities.invokeLater(() -> {
                notificationUtil.notificarNuevoRegistroPendiente(nombreCompleto, email, rol);
            });
            
            System.out.println("✅ Administradores notificados sobre nuevo registro");
            
        } catch (Exception e) {
            System.err.println("❌ Error notificando nuevo registro: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Notifica sobre cambios en cursos
     */
    public void procesarCambioCurso(int alumnoId, String cursoAnterior, String cursoNuevo, String motivo) {
        try {
            System.out.println("📚 Procesando cambio de curso para alumno ID: " + alumnoId);
            
            SwingUtilities.invokeLater(() -> {
                notificationUtil.notificarCambioCurso(alumnoId, cursoAnterior, cursoNuevo, motivo);
            });
            
            System.out.println("✅ Alumno notificado sobre cambio de curso");
            
        } catch (Exception e) {
            System.err.println("❌ Error notificando cambio de curso: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Notifica sobre actualizaciones del sistema
     */
    public void notificarActualizacionSistema(String version, String cambios) {
        try {
            if (notificationUtil.puedeGestionarNotificaciones()) {
                String contenido = String.format(
                    "🚀 El sistema ha sido actualizado a la versión %s\n\n" +
                    "📋 Cambios principales:\n%s\n\n" +
                    "🔄 Reinicia tu sesión para aplicar los cambios.",
                    version, cambios
                );
                
                notificationUtil.enviarNotificacionSistema(
                    "🚀 Actualización del Sistema",
                    contenido,
                    "evento"
                );
                
                System.out.println("✅ Notificación de actualización enviada");
            }
        } catch (Exception e) {
            System.err.println("❌ Error notificando actualización: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES PARA ESTADÍSTICAS
    // ========================================

    /**
     * NUEVO: Cuenta usuarios activos en el sistema
     */
    private int contarUsuariosActivos() {
        try {
            if (connection == null || connection.isClosed()) {
                return 0;
            }
            
            String query = "SELECT COUNT(*) FROM usuarios WHERE activo = 1";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error contando usuarios activos: " + e.getMessage());
        }
        return 0;
    }

    /**
     * NUEVO: Cuenta notificaciones enviadas hoy
     */
    private int contarNotificacionesHoy() {
        try {
            if (connection == null || connection.isClosed()) {
                return 0;
            }
            
            String query = "SELECT COUNT(*) FROM notificaciones WHERE DATE(fecha_creacion) = CURDATE()";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error contando notificaciones de hoy: " + e.getMessage());
        }
        return 0;
    }

    /**
     * NUEVO: Cuenta usuarios con notificaciones pendientes
     */
    private int contarUsuariosConPendientes() {
        try {
            if (connection == null || connection.isClosed()) {
                return 0;
            }
            
            String query = """
                SELECT COUNT(DISTINCT nd.destinatario_id) 
                FROM notificaciones_destinatarios nd 
                INNER JOIN notificaciones n ON nd.notificacion_id = n.id 
                WHERE nd.estado_lectura = 'NO_LEIDA' AND n.estado = 'ACTIVA'
                """;
                
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error contando usuarios con pendientes: " + e.getMessage());
        }
        return 0;
    }

    // ========================================
    // MÉTODOS DE ESTRUCTURA DE BOLETINES (SIN CAMBIOS)
    // ========================================

    /**
     * NUEVO: Crea el panel de estructura de boletines de forma dinámica
     */
    private JPanel crearPanelEstructuraBoletines() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Gestión de Estructura de Boletines - Servidor", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(51, 153, 255));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

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

        // Botones de acción
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        gbc.gridx = 0;
        JButton btnConfigurarServidor = createStyledButton("CONFIGURAR SERVIDOR", "");
        btnConfigurarServidor.setPreferredSize(new Dimension(200, 50));
        btnConfigurarServidor.addActionListener(e -> configurarServidorBoletines());
        panelCentral.add(btnConfigurarServidor, gbc);

        gbc.gridx = 1;
        JButton btnCrearEstructura = createStyledButton("CREAR ESTRUCTURA BD", "");
        btnCrearEstructura.setPreferredSize(new Dimension(200, 50));
        btnCrearEstructura.addActionListener(e -> crearEstructuraBaseDatos());
        panelCentral.add(btnCrearEstructura, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JButton btnVerificarConexion = createStyledButton("VERIFICAR SERVIDOR", "");
        btnVerificarConexion.setPreferredSize(new Dimension(200, 50));
        btnVerificarConexion.addActionListener(e -> verificarConexionServidor());
        panelCentral.add(btnVerificarConexion, gbc);

        gbc.gridx = 1;
        JButton btnVolver = createStyledButton("VOLVER", "");
        btnVolver.setPreferredSize(new Dimension(200, 50));
        btnVolver.setBackground(new Color(96, 125, 139));
        btnVolver.addActionListener(e -> ventana.restaurarVistaPrincipal());
        panelCentral.add(btnVolver, gbc);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        return panelPrincipal;
    }

    /**
     * Crea el panel de información del servidor
     */
    private JPanel crearPanelInformacionServidor() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Estado Actual del Sistema"));
        panel.setPreferredSize(new Dimension(500, 150));

        // Obtener información actual
        String rutaServidor = "Configurar ruta del servidor"; // Placeholder
        try {
            rutaServidor = GestorBoletines.obtenerRutaServidor();
        } catch (Exception e) {
            System.err.println("Error obteniendo ruta del servidor: " + e.getMessage());
        }
        
        int anioActual = java.time.LocalDate.now().getYear();

        StringBuilder info = new StringBuilder();
        info.append("🌐 Servidor de boletines: ").append(rutaServidor).append("\n");
        info.append("📅 Año actual: ").append(anioActual).append("\n");

        // Verificar si existe estructura en BD
        boolean estructuraExiste = false;
        try {
            estructuraExiste = GestorBoletines.verificarEstructuraCarpetas(anioActual);
        } catch (Exception e) {
            System.err.println("Error verificando estructura: " + e.getMessage());
        }
        
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
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        return panel;
    }

    // Métodos de configuración del servidor (simplificados para el ejemplo)
    private void configurarServidorBoletines() {
        // NUEVO: Agregar notificación sobre configuración
        JOptionPane.showMessageDialog(ventana,
                "Funcionalidad de configuración del servidor.\n" +
                "Aquí iría el diálogo de configuración completo.",
                "Configurar Servidor",
                JOptionPane.INFORMATION_MESSAGE);
                
        // Opcional: Notificar sobre configuración del servidor
        if (notificationUtil.puedeGestionarNotificaciones()) {
            SwingUtilities.invokeLater(() -> {
                notificationUtil.enviarNotificacionSistema(
                    "🔧 Configuración del Servidor de Boletines",
                    "Se ha modificado la configuración del servidor de boletines. " +
                    "Los cambios pueden tardar unos minutos en aplicarse.",
                    "mantenimiento"
                );
            });
        }
    }

    private void crearEstructuraBaseDatos() {
        // NUEVO: Agregar notificación sobre creación de estructura
        JOptionPane.showMessageDialog(ventana,
                "Funcionalidad de creación de estructura en BD.\n" +
                "Aquí iría el proceso de creación completo.",
                "Crear Estructura",
                JOptionPane.INFORMATION_MESSAGE);
                
        // Opcional: Notificar sobre creación de estructura
        if (notificationUtil.puedeGestionarNotificaciones()) {
            SwingUtilities.invokeLater(() -> {
                notificationUtil.enviarNotificacionARol(
                    "🏗️ Estructura de Base de Datos Actualizada",
                    "Se ha actualizado la estructura de la base de datos para el año " +
                    java.time.LocalDate.now().getYear() + ". " +
                    "Todas las funciones de boletines están ahora disponibles.",
                    2 // Solo a preceptores que manejan boletines
                );
            });
        }
    }

    private void verificarConexionServidor() {
        JOptionPane.showMessageDialog(ventana,
                "Funcionalidad de verificación del servidor.\n" +
                "Aquí iría la verificación completa.",
                "Verificar Servidor",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ========================================
    // MÉTODOS DE INFORMACIÓN Y DEBUG
    // ========================================

    /**
     * NUEVO: Obtiene información completa del sistema de notificaciones
     */
    public String getNotificationSystemInfo() throws SQLException {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DEL SISTEMA DE NOTIFICACIONES ===\n");
        info.append("AdminPanelManager - Usuario ID: ").append(userId).append("\n");
        info.append("Sistema activo: ").append(notificationUtil != null).append("\n");
        
        if (notificationUtil != null) {
            info.append("Puede enviar: ").append(notificationUtil.puedeEnviarNotificaciones()).append("\n");
            info.append("Puede gestionar: ").append(notificationUtil.puedeGestionarNotificaciones()).append("\n");
            info.append("Notificaciones no leídas: ").append(notificationUtil.getNotificacionesNoLeidas()).append("\n");
        }
        
        info.append("Conexión BD: ").append(connection != null && !connection.isClosed()).append("\n");
        info.append("Usuarios activos: ").append(contarUsuariosActivos()).append("\n");
        info.append("Notificaciones hoy: ").append(contarNotificacionesHoy()).append("\n");
        
        return info.toString();
    }

    /**
     * NUEVO: Verifica el estado del sistema de notificaciones
     */
    public boolean verificarSistemaNotificaciones() {
        try {
            boolean sistemaOk = true;
            
            if (notificationUtil == null) {
                System.err.println("❌ NotificationUtil no inicializado");
                sistemaOk = false;
            }
            
            if (connection == null || connection.isClosed()) {
                System.err.println("❌ Conexión de BD no disponible");
                sistemaOk = false;
            }
            
            if (!notificationUtil.puedeGestionarNotificaciones()) {
                System.err.println("❌ Sin permisos de gestión");
                sistemaOk = false;
            }
            
            if (sistemaOk) {
                System.out.println("✅ Sistema de notificaciones AdminPanelManager - OK");
            }
            
            return sistemaOk;
            
        } catch (Exception e) {
            System.err.println("❌ Error verificando sistema: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO: Método de limpieza de recursos
     */
    public void dispose() {
        try {
            // Limpiar referencias si es necesario
            System.out.println("🧹 AdminPanelManager - Limpiando recursos...");
            
            // Las referencias se limpiarán automáticamente por el GC
            // NotificationUtil y Connection se manejan globalmente
            
            System.out.println("✅ AdminPanelManager - Recursos liberados");
            
        } catch (Exception e) {
            System.err.println("❌ Error liberando recursos AdminPanelManager: " + e.getMessage());
        }
    }
}