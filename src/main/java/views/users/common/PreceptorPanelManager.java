package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import main.java.database.Conexion;
import main.java.views.users.Preceptor.AsistenciaPreceptorPanel;
import main.java.utils.ExcelExportUtility;
import main.java.services.NotificationCore.NotificationIntegrationUtil;
import main.java.views.notifications.NotificationUI.NotificationsWindow;
import main.java.views.notifications.NotificationUI.NotificationSenderWindow;

/**
 * Gestor de paneles específico para el rol de Preceptor con integración
 * completa de notificaciones. VERSIÓN MEJORADA - Incluye sistema completo de
 * notificaciones académicas
 */
public class PreceptorPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int preceptorId;
    private Connection conect;
    private Map<String, Integer> cursosMap = new HashMap<>();

    // Componentes de notificaciones
    private final NotificationIntegrationUtil notificationUtil;

    public PreceptorPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.preceptorId = userId;
        this.conect = Conexion.getInstancia().verificarConexion();
        this.notificationUtil = NotificationIntegrationUtil.getInstance();

        System.out.println("✅ PreceptorPanelManager inicializado con notificaciones para usuario: " + userId);
    }

    @Override
    public JComponent[] createButtons() {
        JButton btnNotas = createStyledButton("NOTAS", "notas");
        JButton btnAsistencias = createStyledButton("ASISTENCIAS", "asistencias");
        JButton btnExportar = createStyledButton("EXPORTAR", "exportar");
        JButton btnBoletines = createStyledButton("BOLETINES", "boletines");
        JButton btnNotificaciones = createStyledButton("NOTIFICACIONES", "notificaciones");
        JButton btnEnviarAviso = createStyledButton("ENVIAR AVISO", "enviar_aviso");

        return new JComponent[]{btnNotas, btnAsistencias, btnExportar, btnBoletines, btnNotificaciones, btnEnviarAviso};
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);

        // Estilo específico para botones de notificaciones
        if ("notificaciones".equals(actionCommand)) {
            button.setBackground(new Color(255, 193, 7)); // Amarillo
        } else if ("enviar_aviso".equals(actionCommand)) {
            button.setBackground(new Color(40, 167, 69)); // Verde
        } else {
            button.setBackground(new Color(51, 153, 255)); // Azul estándar
        }

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
            System.out.println("=== ACCIÓN PRECEPTOR: " + actionCommand + " ===");

            switch (actionCommand) {
                case "notas":
                    mostrarPanelNotas();
                    break;
                case "asistencias":
                    mostrarPanelAsistenciasSelector();
                    break;
                case "exportar":
                    mostrarDialogoExportacion();
                    break;
                case "boletines":
                    mostrarPanelBoletines();
                    break;
                case "notificaciones":
                    mostrarPanelNotificaciones();
                    break;
                case "enviar_aviso":
                    mostrarPanelEnviarAviso();
                    break;
                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }
        } catch (Exception ex) {
            System.err.println("❌ Error al procesar acción preceptor: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al procesar la acción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Muestra el panel de notificaciones del preceptor
     */
    private void mostrarPanelNotificaciones() {
        try {
            System.out.println("Abriendo panel de notificaciones para preceptor...");

            SwingUtilities.invokeLater(() -> {
                NotificationsWindow notificationsWindow = new NotificationsWindow(preceptorId);
                notificationsWindow.setVisible(true);
            });

            System.out.println("✅ Panel de notificaciones abierto exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de notificaciones: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * NUEVO: Muestra el panel para enviar avisos/notificaciones
     */
    private void mostrarPanelEnviarAviso() {
        try {
            System.out.println("Creando panel de envío de avisos para preceptor...");

            JPanel panelEnvio = crearPanelEnvioAvisos();
            ventana.mostrarPanelResponsive(panelEnvio, "Enviar Avisos y Notificaciones");

            System.out.println("✅ Panel de envío de avisos mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de envío: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * NUEVO: Crea el panel completo para envío de avisos académicos
     */
    private JPanel crearPanelEnvioAvisos() {
        JPanel panelCompleto = new JPanel(new BorderLayout());
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título principal
        JLabel lblTitulo = new JLabel("📢 Envío de Avisos y Notificaciones", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(40, 167, 69));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelCompleto.add(lblTitulo, BorderLayout.NORTH);

        // Panel central con opciones
        JPanel panelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Botón para aviso rápido a curso
        JButton btnAvisoCurso = new JButton("📚 Aviso Rápido a Curso");
        btnAvisoCurso.setPreferredSize(new Dimension(250, 50));
        btnAvisoCurso.setBackground(new Color(0, 123, 255));
        btnAvisoCurso.setForeground(Color.WHITE);
        btnAvisoCurso.setFont(new Font("Arial", Font.BOLD, 14));
        btnAvisoCurso.addActionListener(e -> mostrarDialogoAvisoCurso());

        // Botón para notificación de asistencia
        JButton btnNotifAsistencia = new JButton("📊 Notificar Problema de Asistencia");
        btnNotifAsistencia.setPreferredSize(new Dimension(250, 50));
        btnNotifAsistencia.setBackground(new Color(255, 193, 7));
        btnNotifAsistencia.setForeground(Color.BLACK);
        btnNotifAsistencia.setFont(new Font("Arial", Font.BOLD, 14));
        btnNotifAsistencia.addActionListener(e -> mostrarDialogoProblemaAsistencia());

        // Botón para notificar boletín generado
        JButton btnNotifBoletin = new JButton("📋 Notificar Boletín Generado");
        btnNotifBoletin.setPreferredSize(new Dimension(250, 50));
        btnNotifBoletin.setBackground(new Color(40, 167, 69));
        btnNotifBoletin.setForeground(Color.WHITE);
        btnNotifBoletin.setFont(new Font("Arial", Font.BOLD, 14));
        btnNotifBoletin.addActionListener(e -> mostrarDialogoBoletinGenerado());

        // Botón para notificar cambio de curso
        JButton btnCambioCurso = new JButton("🔄 Notificar Cambio de Curso");
        btnCambioCurso.setPreferredSize(new Dimension(250, 50));
        btnCambioCurso.setBackground(new Color(108, 117, 125));
        btnCambioCurso.setForeground(Color.WHITE);
        btnCambioCurso.setFont(new Font("Arial", Font.BOLD, 14));
        btnCambioCurso.addActionListener(e -> mostrarDialogoCambioCurso());

        // Botón para ventana completa de envío
        JButton btnVentanaCompleta = new JButton("✉️ Ventana Completa de Envío");
        btnVentanaCompleta.setPreferredSize(new Dimension(250, 50));
        btnVentanaCompleta.setBackground(new Color(111, 66, 193));
        btnVentanaCompleta.setForeground(Color.WHITE);
        btnVentanaCompleta.setFont(new Font("Arial", Font.BOLD, 14));
        btnVentanaCompleta.addActionListener(e -> abrirVentanaEnvioCompleta());

        // Organizar botones en grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentral.add(btnAvisoCurso, gbc);

        gbc.gridx = 1;
        panelCentral.add(btnNotifAsistencia, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentral.add(btnNotifBoletin, gbc);

        gbc.gridx = 1;
        panelCentral.add(btnCambioCurso, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelCentral.add(btnVentanaCompleta, gbc);

        panelCompleto.add(panelCentral, BorderLayout.CENTER);

        // Panel inferior con estadísticas
        JPanel panelInferior = crearPanelEstadisticasNotificaciones();
        panelCompleto.add(panelInferior, BorderLayout.SOUTH);

        return panelCompleto;
    }

    /**
     * NUEVO: Crea panel con estadísticas de notificaciones
     */
    private JPanel crearPanelEstadisticasNotificaciones() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📊 Mis Estadísticas de Notificaciones"));
        panel.setBackground(new Color(248, 249, 250));

        // Obtener estadísticas
        int notificacionesNoLeidas = notificationUtil.getNotificacionesNoLeidas();

        JLabel lblEstadisticas = new JLabel(String.format(
                "📬 Notificaciones no leídas: %d  |  👤 Usuario ID: %d  |  🎭 Rol: Preceptor",
                notificacionesNoLeidas, preceptorId
        ));
        lblEstadisticas.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnActualizar.addActionListener(e -> {
            notificationUtil.actualizarNotificaciones();
            // Recrear el panel para mostrar datos actualizados
            SwingUtilities.invokeLater(() -> {
                try {
                    mostrarPanelEnviarAviso();
                } catch (Exception ex) {
                    System.err.println("Error actualizando estadísticas: " + ex.getMessage());
                }
            });
        });

        panel.add(lblEstadisticas);
        panel.add(btnActualizar);

        return panel;
    }

    /**
     * NUEVO: Diálogo rápido para enviar aviso a un curso
     */
    private void mostrarDialogoAvisoCurso() {
        try {
            cargarCursos();

            if (cursosMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron cursos activos.",
                        "Sin cursos disponibles",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de curso
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Curso:"), gbc);

            JComboBox<String> comboCurso = new JComboBox<>();
            for (String curso : cursosMap.keySet()) {
                comboCurso.addItem(curso);
            }
            gbc.gridx = 1;
            panel.add(comboCurso, gbc);

            // Título del aviso
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Título:"), gbc);

            JTextField txtTitulo = new JTextField(30);
            gbc.gridx = 1;
            panel.add(txtTitulo, gbc);

            // Contenido del aviso
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JLabel("Mensaje:"), gbc);

            JTextArea txtMensaje = new JTextArea(5, 30);
            txtMensaje.setLineWrap(true);
            txtMensaje.setWrapStyleWord(true);
            JScrollPane scrollMensaje = new JScrollPane(txtMensaje);
            gbc.gridx = 1;
            panel.add(scrollMensaje, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📚 Enviar Aviso a Curso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                String cursoSeleccionado = (String) comboCurso.getSelectedItem();
                String titulo = txtTitulo.getText().trim();
                String mensaje = txtMensaje.getText().trim();

                if (titulo.isEmpty() || mensaje.isEmpty()) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor complete el título y mensaje.",
                            "Campos requeridos",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Enviar aviso usando el sistema de notificaciones
                enviarAvisoACurso(cursoSeleccionado, titulo, mensaje);
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de aviso a curso: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al crear el diálogo: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Envía aviso a todos los alumnos de un curso
     */
    private void enviarAvisoACurso(String curso, String titulo, String mensaje) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Integer cursoId = cursosMap.get(curso);
                if (cursoId == null) {
                    throw new Exception("No se pudo encontrar el curso seleccionado");
                }

                // Obtener alumnos del curso
                List<Integer> alumnos = obtenerAlumnosDeCurso(cursoId);

                if (alumnos.isEmpty()) {
                    throw new Exception("No se encontraron alumnos en el curso " + curso);
                }

                // Preparar título completo
                String tituloCompleto = "📢 Aviso de Preceptoría - " + curso + ": " + titulo;

                // Preparar mensaje completo
                String mensajeCompleto = String.format(
                        "Curso: %s\n\n%s\n\n"
                        + "📅 Fecha: %s\n"
                        + "👨‍💼 Enviado por: Preceptoría",
                        curso, mensaje,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );

                // Enviar a todos los alumnos del curso
                for (Integer alumnoId : alumnos) {
                    notificationUtil.enviarNotificacionBasica(tituloCompleto, mensajeCompleto, alumnoId);
                }

                return true;
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(ventana,
                                "✅ Aviso enviado exitosamente a todos los alumnos del curso " + curso,
                                "Aviso Enviado",
                                JOptionPane.INFORMATION_MESSAGE);

                        System.out.println("✅ Aviso enviado a curso " + curso + " por preceptor " + preceptorId);
                    }
                } catch (Exception e) {
                    System.err.println("Error enviando aviso: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ventana,
                            "Error al enviar el aviso: " + e.getMessage(),
                            "Error de Envío",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Diálogo para notificar problema de asistencia
     */
    private void mostrarDialogoProblemaAsistencia() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // ID del alumno
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("ID del Alumno:"), gbc);

            JTextField txtAlumnoId = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtAlumnoId, gbc);

            // Cantidad de faltas
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Cantidad de faltas:"), gbc);

            JTextField txtFaltas = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtFaltas, gbc);

            // Período
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Período:"), gbc);

            JComboBox<String> comboPeriodo = new JComboBox<>(new String[]{
                "1er Bimestre", "2do Bimestre", "3er Bimestre", "4to Bimestre", "Anual"
            });
            gbc.gridx = 1;
            panel.add(comboPeriodo, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📊 Notificar Problema de Asistencia", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    int alumnoId = Integer.parseInt(txtAlumnoId.getText().trim());
                    int cantidadFaltas = Integer.parseInt(txtFaltas.getText().trim());
                    String periodo = (String) comboPeriodo.getSelectedItem();

                    // Usar el sistema de notificaciones integrado
                    notificationUtil.notificarProblemaAsistencia(alumnoId, cantidadFaltas, periodo);

                    JOptionPane.showMessageDialog(ventana,
                            "✅ Notificación de problema de asistencia enviada al alumno ID: " + alumnoId,
                            "Notificación Enviada",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor ingrese números válidos para ID y cantidad de faltas.",
                            "Datos inválidos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de problema de asistencia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Diálogo para notificar boletín generado
     */
    private void mostrarDialogoBoletinGenerado() {
        try {
            cargarCursos();

            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de curso
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Curso:"), gbc);

            JComboBox<String> comboCurso = new JComboBox<>();
            for (String curso : cursosMap.keySet()) {
                comboCurso.addItem(curso);
            }
            gbc.gridx = 1;
            panel.add(comboCurso, gbc);

            // Período
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Período:"), gbc);

            JComboBox<String> comboPeriodo = new JComboBox<>(new String[]{
                "1er Bimestre", "2do Bimestre", "3er Bimestre", "4to Bimestre", "Anual"
            });
            gbc.gridx = 1;
            panel.add(comboPeriodo, gbc);

            // Cantidad generados
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Cantidad generados:"), gbc);

            JTextField txtCantidad = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtCantidad, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📋 Notificar Boletines Generados", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    String curso = (String) comboCurso.getSelectedItem();
                    String periodo = (String) comboPeriodo.getSelectedItem();
                    int cantidad = Integer.parseInt(txtCantidad.getText().trim());

                    // Notificar al preceptor sobre la generación masiva
                    notificationUtil.notificarBoletinesMasivos(preceptorId, curso, cantidad, periodo);

                    // También notificar a cada alumno individualmente
                    notificarBoletinesAAlumnos(curso, periodo);

                    JOptionPane.showMessageDialog(ventana,
                            "✅ Notificación de boletines generados enviada exitosamente",
                            "Notificación Enviada",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor ingrese un número válido para la cantidad.",
                            "Datos inválidos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de boletín generado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Notifica a alumnos sobre boletín disponible
     */
    private void notificarBoletinesAAlumnos(String curso, String periodo) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Integer cursoId = cursosMap.get(curso);
                if (cursoId != null) {
                    List<Integer> alumnos = obtenerAlumnosDeCurso(cursoId);
                    String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    for (Integer alumnoId : alumnos) {
                        notificationUtil.notificarBoletinGenerado(alumnoId, periodo, fechaActual);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                System.out.println("✅ Notificaciones de boletín enviadas a alumnos del curso " + curso);
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Diálogo para notificar cambio de curso
     */
    private void mostrarDialogoCambioCurso() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // ID del alumno
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("ID del Alumno:"), gbc);

            JTextField txtAlumnoId = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtAlumnoId, gbc);

            // Curso anterior
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Curso anterior:"), gbc);

            JTextField txtCursoAnterior = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtCursoAnterior, gbc);

            // Curso nuevo
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Curso nuevo:"), gbc);

            JTextField txtCursoNuevo = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtCursoNuevo, gbc);

            // Motivo
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Motivo:"), gbc);

            JTextField txtMotivo = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtMotivo, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "🔄 Notificar Cambio de Curso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    int alumnoId = Integer.parseInt(txtAlumnoId.getText().trim());
                    String cursoAnterior = txtCursoAnterior.getText().trim();
                    String cursoNuevo = txtCursoNuevo.getText().trim();
                    String motivo = txtMotivo.getText().trim();

                    if (cursoAnterior.isEmpty() || cursoNuevo.isEmpty() || motivo.isEmpty()) {
                        JOptionPane.showMessageDialog(ventana,
                                "Por favor complete todos los campos.",
                                "Campos requeridos",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Usar el sistema de notificaciones integrado
                    notificationUtil.notificarCambioCurso(alumnoId, cursoAnterior, cursoNuevo, motivo);

                    JOptionPane.showMessageDialog(ventana,
                            "✅ Notificación de cambio de curso enviada al alumno ID: " + alumnoId,
                            "Notificación Enviada",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor ingrese un ID de alumno válido.",
                            "Datos inválidos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de cambio de curso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Abre la ventana completa de envío de notificaciones
     */
    private void abrirVentanaEnvioCompleta() {
        try {
            System.out.println("Abriendo ventana completa de envío de notificaciones...");

            SwingUtilities.invokeLater(() -> {
                NotificationSenderWindow senderWindow = new NotificationSenderWindow(preceptorId);
                senderWindow.setVisible(true);
            });

            System.out.println("✅ Ventana de envío completa abierta exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al abrir ventana de envío: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al abrir la ventana de envío: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MEJORADO: Obtiene los alumnos de un curso específico
     */
    private List<Integer> obtenerAlumnosDeCurso(int cursoId) {
        java.util.List<Integer> alumnos = new java.util.ArrayList<>();

        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT alumno_id FROM alumno_curso WHERE curso_id = ? AND estado = 'activo'";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                alumnos.add(rs.getInt("alumno_id"));
            }

            System.out.println("✅ Obtenidos " + alumnos.size() + " alumnos para curso ID: " + cursoId);

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo alumnos del curso: " + e.getMessage());
            e.printStackTrace();
        }

        return alumnos;
    }

    // ===============================================
    // MÉTODOS ORIGINALES CON MEJORAS DE NOTIFICACIONES
    // ===============================================
    /**
     * MEJORADO: Muestra panel de notas con notificaciones integradas
     */
    private void mostrarPanelNotas() {
        try {
            System.out.println("Creando NotasVisualizationPanel para preceptor...");

            NotasVisualizationPanel panelNotas
                    = new NotasVisualizationPanel(ventana, preceptorId, 2); // rol 2 = preceptor

            ventana.mostrarPanelResponsive(panelNotas, "Visualización de Notas");

            System.out.println("✅ Panel de notas del preceptor mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de notas: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MEJORADO: Muestra panel selector de asistencias con notificaciones
     */
    private void mostrarPanelAsistenciasSelector() {
        try {
            System.out.println("Creando panel selector de asistencias...");

            // Crear panel dinámico con selector de curso
            JPanel panelSelector = crearPanelSelectorAsistencias();
            ventana.mostrarPanelResponsive(panelSelector, "Selección de Curso - Asistencias");

            System.out.println("✅ Panel selector de asistencias mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel selector: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MEJORADO: Crea el panel selector de cursos para asistencias
     */
    private JPanel crearPanelSelectorAsistencias() {
        JPanel panelCompleto = new JPanel(new BorderLayout());
        panelCompleto.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Seleccione el Curso para Ver Asistencias", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(51, 153, 255));
        panelCompleto.add(lblTitulo, BorderLayout.NORTH);

        // Panel central con selector
        JPanel panelCentral = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 50));

        JLabel lblSeleccion = new JLabel("Curso:");
        lblSeleccion.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<String> comboCursos = new JComboBox<>();
        comboCursos.setPreferredSize(new Dimension(200, 35));
        comboCursos.setFont(new Font("Arial", Font.PLAIN, 14));

        // Cargar cursos
        cargarCursos();
        for (String curso : cursosMap.keySet()) {
            comboCursos.addItem(curso);
        }

        if (cursosMap.isEmpty()) {
            JLabel lblSinCursos = new JLabel("No se encontraron cursos activos", JLabel.CENTER);
            lblSinCursos.setFont(new Font("Arial", Font.ITALIC, 16));
            lblSinCursos.setForeground(Color.RED);
            panelCompleto.add(lblSinCursos, BorderLayout.CENTER);
            return panelCompleto;
        }

        JButton btnCargar = new JButton("Cargar Asistencias");
        btnCargar.setPreferredSize(new Dimension(150, 35));
        btnCargar.setBackground(new Color(51, 153, 255));
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setFont(new Font("Arial", Font.BOLD, 14));

        // Listener para cargar asistencias
        btnCargar.addActionListener(e -> {
            String cursoSeleccionado = (String) comboCursos.getSelectedItem();
            if (cursoSeleccionado != null) {
                Integer cursoId = cursosMap.get(cursoSeleccionado);
                if (cursoId != null) {
                    mostrarAsistenciasCurso(cursoId, cursoSeleccionado);
                }
            }
        });

        panelCentral.add(lblSeleccion);
        panelCentral.add(comboCursos);
        panelCentral.add(btnCargar);

        panelCompleto.add(panelCentral, BorderLayout.CENTER);

        return panelCompleto;
    }

    /**
     * MEJORADO: Muestra las asistencias del curso seleccionado con
     * notificaciones
     */
    private void mostrarAsistenciasCurso(int cursoId, String cursoNombre) {
        try {
            System.out.println("Creando AsistenciaPreceptorPanel para curso: " + cursoNombre);

            AsistenciaPreceptorPanel panelAsistencia
                    = new AsistenciaPreceptorPanel(preceptorId, cursoId);

            String titulo = "Asistencias - Curso " + cursoNombre;
            ventana.mostrarPanelResponsive(panelAsistencia, titulo);

            // NUEVO: Verificar problemas de asistencia automáticamente
            verificarProblemasAsistenciaAutomatico(cursoId, cursoNombre);

            System.out.println("✅ Panel de asistencias mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar asistencias del curso: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar asistencias del curso " + cursoNombre + ":\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Verifica automáticamente problemas de asistencia
     */
    private void verificarProblemasAsistenciaAutomatico(int cursoId, String cursoNombre) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Obtener alumnos con problemas de asistencia
                    Map<Integer, Integer> alumnosConProblemas = obtenerAlumnosConProblemasAsistencia(cursoId);

                    if (!alumnosConProblemas.isEmpty()) {
                        System.out.println("⚠️ Detectados " + alumnosConProblemas.size()
                                + " alumnos con problemas de asistencia en " + cursoNombre);

                        // Notificar a cada alumno con problema
                        for (Map.Entry<Integer, Integer> entry : alumnosConProblemas.entrySet()) {
                            int alumnoId = entry.getKey();
                            int cantidadFaltas = entry.getValue();

                            if (cantidadFaltas >= 5) { // Umbral configurable
                                notificationUtil.notificarProblemaAsistencia(
                                        alumnoId, cantidadFaltas, "Bimestre actual"
                                );
                            }
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Error verificando problemas de asistencia: " + e.getMessage());
                }

                return null;
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Obtiene alumnos con problemas de asistencia
     */
    private Map<Integer, Integer> obtenerAlumnosConProblemasAsistencia(int cursoId) {
        Map<Integer, Integer> problemasAsistencia = new HashMap<>();

        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = """
                SELECT ag.alumno_id, COUNT(*) as total_faltas
                FROM asistencia_general ag
                INNER JOIN alumno_curso ac ON ag.alumno_id = ac.alumno_id
                WHERE ac.curso_id = ? 
                AND ag.estado IN ('FALTA', 'TARDE') 
                AND ag.fecha >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                GROUP BY ag.alumno_id
                HAVING total_faltas >= 3
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int alumnoId = rs.getInt("alumno_id");
                int totalFaltas = rs.getInt("total_faltas");
                problemasAsistencia.put(alumnoId, totalFaltas);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo problemas de asistencia: " + e.getMessage());
            e.printStackTrace();
        }

        return problemasAsistencia;
    }

    /**
     * Carga los cursos desde la base de datos
     */
    private void cargarCursos() {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            if (conect == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }

            cursosMap.clear();

            String query = "SELECT id, anio, division FROM cursos WHERE estado = 'activo' ORDER BY anio, division";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int anio = rs.getInt("anio");
                String division = rs.getString("division");

                String formato = anio + "°" + division;
                cursosMap.put(formato, id);
            }

            System.out.println("Cursos cargados para preceptor: " + cursosMap.size());
            cursosMap.forEach((nombre, id) -> System.out.println("  " + nombre + " -> ID=" + id));

        } catch (SQLException ex) {
            System.err.println("Error al cargar cursos: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar cursos: " + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MEJORADO: Muestra panel de boletines con notificaciones automáticas
     */
    private void mostrarPanelBoletines() {
        try {
            System.out.println("Creando PanelGestionBoletines para preceptor...");

            main.java.views.users.common.PanelGestionBoletines panelBoletines
                    = new main.java.views.users.common.PanelGestionBoletines(ventana, preceptorId, 2); // rol 2 = preceptor

            ventana.mostrarPanelResponsive(panelBoletines, "Gestión de Boletines");

            System.out.println("✅ Panel de boletines del preceptor mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de boletines: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MEJORADO: Muestra diálogo de exportación con notificaciones
     */
    private void mostrarDialogoExportacion() {
        try {
            System.out.println("Iniciando proceso de exportación para preceptor ID: " + preceptorId);

            if (cursosMap.isEmpty()) {
                cargarCursos();
            }

            if (cursosMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron cursos activos para exportar.",
                        "Sin datos para exportar",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] opciones = {"Exportar Curso Específico", "Exportar Todos los Cursos", "Cancelar"};
            int seleccion = JOptionPane.showOptionDialog(ventana,
                    "Seleccione qué desea exportar:",
                    "Exportar Asistencias",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]);

            switch (seleccion) {
                case 0:
                    exportarCursoEspecifico();
                    break;
                case 1:
                    exportarTodosLosCursos();
                    break;
                case 2:
                default:
                    System.out.println("Exportación cancelada por el usuario");
                    break;
            }

        } catch (Exception ex) {
            System.err.println("Error al mostrar diálogo de exportación: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al iniciar exportación: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MEJORADO: Exporta las asistencias de un curso específico con notificación
     */
    private void exportarCursoEspecifico() {
        try {
            String[] cursosArray = cursosMap.keySet().toArray(new String[0]);

            String cursoSeleccionado = (String) JOptionPane.showInputDialog(ventana,
                    "Seleccione el curso a exportar:",
                    "Seleccionar Curso",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    cursosArray,
                    cursosArray[0]);

            if (cursoSeleccionado != null) {
                Integer cursoId = cursosMap.get(cursoSeleccionado);
                if (cursoId != null) {
                    boolean exito = exportarAsistenciasCurso(cursoId, cursoSeleccionado);

                    // NUEVO: Enviar notificación de confirmación si es exitoso
                    if (exito) {
                        String mensaje = "Exportación de asistencias completada exitosamente para " + cursoSeleccionado;
                        notificationUtil.enviarNotificacionBasica(
                                "📊 Exportación Completada",
                                mensaje,
                                preceptorId
                        );
                    }
                }
            }

        } catch (Exception ex) {
            System.err.println("Error al exportar curso específico: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al exportar curso: " + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MEJORADO: Exporta las asistencias de todos los cursos con notificaciones
     */
    private void exportarTodosLosCursos() {
        try {
            int confirmacion = JOptionPane.showConfirmDialog(ventana,
                    "¿Está seguro de exportar las asistencias de todos los cursos?\n"
                    + "Esto puede generar archivos grandes y tomar tiempo.",
                    "Confirmar Exportación Completa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
                    @Override
                    protected Integer doInBackground() throws Exception {
                        int cursosExportados = 0;

                        for (Map.Entry<String, Integer> entry : cursosMap.entrySet()) {
                            String curso = entry.getKey();
                            Integer cursoId = entry.getValue();

                            try {
                                if (exportarAsistenciasCurso(cursoId, curso)) {
                                    cursosExportados++;
                                    publish("✅ Exportado: " + curso);
                                } else {
                                    publish("❌ Error en: " + curso);
                                }
                            } catch (Exception ex) {
                                System.err.println("Error al exportar curso " + curso + ": " + ex.getMessage());
                                publish("❌ Error en: " + curso + " - " + ex.getMessage());
                            }
                        }

                        return cursosExportados;
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        // Mostrar progreso (opcional)
                        for (String mensaje : chunks) {
                            System.out.println(mensaje);
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            int cursosExportados = get();

                            String mensaje = String.format(
                                    "Exportación masiva completada.\nCursos exportados: %d de %d",
                                    cursosExportados, cursosMap.size()
                            );

                            JOptionPane.showMessageDialog(ventana,
                                    mensaje,
                                    "Exportación Finalizada",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // NUEVO: Enviar notificación de confirmación
                            notificationUtil.enviarNotificacionBasica(
                                    "📊 Exportación Masiva Completada",
                                    mensaje,
                                    preceptorId
                            );

                        } catch (Exception e) {
                            System.err.println("Error en exportación masiva: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                };

                worker.execute();
            }

        } catch (Exception ex) {
            System.err.println("Error al exportar todos los cursos: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error durante la exportación masiva: " + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exporta las asistencias de un curso específico a Excel
     */
    private boolean exportarAsistenciasCurso(int cursoId, String cursoNombre) {
        try {
            System.out.println("Exportando asistencias para curso: " + cursoNombre + " (ID: " + cursoId + ")");

            // Crear modelo de tabla temporal para exportación
            javax.swing.table.DefaultTableModel modeloExportacion = new javax.swing.table.DefaultTableModel();

            // Configurar columnas
            modeloExportacion.addColumn("Alumno");
            modeloExportacion.addColumn("DNI");

            // Obtener fechas únicas para crear columnas dinámicas
            java.util.Set<String> fechas = obtenerFechasAsistencia(cursoId);
            java.util.List<String> fechasOrdenadas = new java.util.ArrayList<>(fechas);
            java.util.Collections.sort(fechasOrdenadas);

            // Agregar columnas de fechas
            for (String fecha : fechasOrdenadas) {
                modeloExportacion.addColumn(fecha);
            }

            // Cargar datos de alumnos y asistencias
            cargarDatosAsistenciaParaExportacion(modeloExportacion, cursoId, fechasOrdenadas);

            // Crear tabla temporal
            javax.swing.JTable tablaExportacion = new javax.swing.JTable(modeloExportacion);

            // Usar la utilidad de exportación
            String fechaActual = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            return ExcelExportUtility.exportarTablaAsistencia(
                    tablaExportacion,
                    cursoNombre,
                    fechaActual
            );

        } catch (Exception ex) {
            System.err.println("Error al exportar asistencias del curso " + cursoNombre + ": " + ex.getMessage());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(ventana,
                    "Error al exportar asistencias del curso " + cursoNombre + ":\n" + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }
    }

    /**
     * Obtiene las fechas únicas de asistencia para un curso
     */
    private java.util.Set<String> obtenerFechasAsistencia(int cursoId) {
        java.util.Set<String> fechas = new java.util.LinkedHashSet<>();

        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT DISTINCT DATE_FORMAT(fecha, '%d/%m/%Y') as fecha_formateada "
                    + "FROM asistencia_general "
                    + "WHERE curso_id = ? "
                    + "ORDER BY fecha";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, cursoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                fechas.add(rs.getString("fecha_formateada"));
            }

            if (fechas.isEmpty()) {
                fechas.add(java.time.LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

        } catch (SQLException ex) {
            System.err.println("Error al obtener fechas de asistencia: " + ex.getMessage());
            ex.printStackTrace();
        }

        return fechas;
    }

    /**
     * Carga los datos de asistencia en el modelo para exportación
     */
    private void cargarDatosAsistenciaParaExportacion(javax.swing.table.DefaultTableModel modelo,
            int cursoId, java.util.List<String> fechas) {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String queryAlumnos = "SELECT u.id, u.nombre, u.apellido, u.dni "
                    + "FROM usuarios u "
                    + "INNER JOIN alumno_curso ac ON u.id = ac.alumno_id "
                    + "WHERE ac.curso_id = ? AND ac.estado = 'activo' "
                    + "ORDER BY u.apellido, u.nombre";

            PreparedStatement psAlumnos = conect.prepareStatement(queryAlumnos);
            psAlumnos.setInt(1, cursoId);
            ResultSet rsAlumnos = psAlumnos.executeQuery();

            while (rsAlumnos.next()) {
                int alumnoId = rsAlumnos.getInt("id");
                String nombreCompleto = rsAlumnos.getString("apellido") + ", " + rsAlumnos.getString("nombre");
                String dni = rsAlumnos.getString("dni");

                Object[] fila = new Object[2 + fechas.size()];
                fila[0] = nombreCompleto;
                fila[1] = dni != null ? dni : "Sin DNI";

                for (int i = 0; i < fechas.size(); i++) {
                    String fecha = fechas.get(i);
                    String estado = obtenerEstadoAsistencia(alumnoId, cursoId, fecha);
                    fila[2 + i] = estado;
                }

                modelo.addRow(fila);
            }

            if (modelo.getRowCount() == 0) {
                Object[] filaSinDatos = new Object[2 + fechas.size()];
                filaSinDatos[0] = "No hay alumnos registrados";
                filaSinDatos[1] = "-";
                for (int i = 0; i < fechas.size(); i++) {
                    filaSinDatos[2 + i] = "-";
                }
                modelo.addRow(filaSinDatos);
            }

        } catch (SQLException ex) {
            System.err.println("Error al cargar datos de asistencia para exportación: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Obtiene el estado de asistencia de un alumno en una fecha específica
     */
    private String obtenerEstadoAsistencia(int alumnoId, int cursoId, String fecha) {
        try {
            String[] partesFecha = fecha.split("/");
            String fechaSQL = partesFecha[2] + "-" + partesFecha[1] + "-" + partesFecha[0];

            String query = "SELECT estado FROM asistencia_general "
                    + "WHERE alumno_id = ? AND curso_id = ? AND fecha = ?";

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, alumnoId);
            ps.setInt(2, cursoId);
            ps.setString(3, fechaSQL);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("estado");
            } else {
                return "NC"; // No corresponde / Sin registro
            }

        } catch (SQLException ex) {
            System.err.println("Error al obtener estado de asistencia: " + ex.getMessage());
            return "NC";
        }
    }

    // ===============================================
    // MÉTODOS DE UTILIDAD Y LIMPIEZA
    // ===============================================
    /**
     * NUEVO: Verifica el estado del sistema de notificaciones
     */
    public boolean verificarSistemaNotificaciones() {
        boolean sistemaActivo = notificationUtil != null
                && notificationUtil.puedeEnviarNotificaciones();

        if (sistemaActivo) {
            System.out.println("✅ Sistema de notificaciones activo para preceptor ID: " + preceptorId);
        } else {
            System.err.println("❌ Sistema de notificaciones no disponible para preceptor ID: " + preceptorId);
        }

        return sistemaActivo;
    }

    /**
     * NUEVO: Obtiene información del sistema para debugging
     */
    public String obtenerInfoSistema() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DEL SISTEMA - PRECEPTOR ===\n");
        info.append("ID del preceptor: ").append(preceptorId).append("\n");
        info.append("Cursos cargados: ").append(cursosMap.size()).append("\n");
        info.append("Sistema de notificaciones: ").append(verificarSistemaNotificaciones() ? "ACTIVO" : "INACTIVO").append("\n");

        if (notificationUtil != null) {
            info.append("Notificaciones no leídas: ").append(notificationUtil.getNotificacionesNoLeidas()).append("\n");
            info.append("Puede enviar notificaciones: ").append(notificationUtil.puedeEnviarNotificaciones()).append("\n");
        }

        info.append("========================================");
        return info.toString();
    }

    /**
     * NUEVO: Método para testing y desarrollo
     */
    public void enviarNotificacionPrueba() {
        if (notificationUtil != null && notificationUtil.puedeEnviarNotificaciones()) {
            String titulo = "🧪 Prueba del Sistema - Preceptor";
            String contenido = String.format(
                    "Notificación de prueba enviada desde el panel del preceptor.\n\n"
                    + "👨‍💼 Preceptor ID: %d\n"
                    + "📅 Fecha: %s\n"
                    + "🔧 Sistema funcionando correctamente",
                    preceptorId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );

            notificationUtil.enviarNotificacionBasica(titulo, contenido, preceptorId);

            JOptionPane.showMessageDialog(ventana,
                    "✅ Notificación de prueba enviada exitosamente",
                    "Prueba Completada",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(ventana,
                    "❌ Sistema de notificaciones no disponible o sin permisos",
                    "Error de Sistema",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NUEVO: Limpia recursos del panel manager
     */
    public void dispose() {
        try {
            // Cerrar conexión si está abierta
            if (conect != null && !conect.isClosed()) {
                conect.close();
            }

            // Limpiar maps
            if (cursosMap != null) {
                cursosMap.clear();
            }

            System.out.println("✅ PreceptorPanelManager recursos liberados para usuario: " + preceptorId);

        } catch (SQLException e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        }
    }

    // ===============================================
    // MÉTODOS AUXILIARES PARA NOTIFICACIONES AVANZADAS
    // ===============================================
    /**
     * NUEVO: Notifica eventos especiales del calendario escolar
     */
    public void notificarEventoEscolar(String tipoEvento, String descripcion, String fecha) {
        if (notificationUtil != null && notificationUtil.puedeEnviarNotificaciones()) {
            String titulo = "📅 Evento Escolar: " + tipoEvento;
            String contenido = String.format(
                    "%s\n\n📅 Fecha: %s\n👨‍💼 Informado por: Preceptoría\n\n"
                    + "Para más información, consulta con la administración.",
                    descripcion, fecha
            );

            // Enviar a todos los roles
            notificationUtil.enviarNotificacionARol(titulo, contenido, 1); // Administradores
            notificationUtil.enviarNotificacionARol(titulo, contenido, 3); // Profesores
            notificationUtil.enviarNotificacionARol(titulo, contenido, 4); // Alumnos
            notificationUtil.enviarNotificacionARol(titulo, contenido, 5); // ATTP
        }
    }

    /**
     * NUEVO: Notifica sobre reuniones de padres
     */
    public void notificarReunionPadres(String curso, String fecha, String hora, String aula) {
        if (notificationUtil != null && notificationUtil.puedeEnviarNotificaciones()) {
            Integer cursoId = cursosMap.get(curso);
            if (cursoId != null) {
                List<Integer> alumnos = obtenerAlumnosDeCurso(cursoId);

                String titulo = "👨‍👩‍👧‍👦 Reunión de Padres - " + curso;
                String contenido = String.format(
                        "Se convoca a reunión de padres para el curso %s:\n\n"
                        + "📅 Fecha: %s\n"
                        + "🕐 Hora: %s\n"
                        + "🏫 Aula: %s\n\n"
                        + "⚠️ IMPORTANTE: La asistencia es obligatoria.\n"
                        + "Por favor, confirme su asistencia con preceptoría.",
                        curso, fecha, hora, aula
                );

                // Enviar a todos los alumnos del curso
                for (Integer alumnoId : alumnos) {
                    notificationUtil.enviarNotificacionUrgente(titulo, contenido, alumnoId);
                }

                System.out.println("✅ Notificación de reunión de padres enviada a " + alumnos.size()
                        + " alumnos del curso " + curso);
            }
        }
    }

    /**
     * NUEVO: Notifica sobre exámenes o evaluaciones importantes
     */
    public void notificarExamenImportante(String curso, String materia, String tipoExamen,
            String fecha, String hora, String observaciones) {
        if (notificationUtil != null && notificationUtil.puedeEnviarNotificaciones()) {
            Integer cursoId = cursosMap.get(curso);
            if (cursoId != null) {
                List<Integer> alumnos = obtenerAlumnosDeCurso(cursoId);

                String titulo = "📝 " + tipoExamen + " - " + materia;
                String contenido = String.format(
                        "Se informa sobre %s de %s para el curso %s:\n\n"
                        + "📅 Fecha: %s\n"
                        + "🕐 Hora: %s\n"
                        + "📚 Materia: %s\n"
                        + "📋 Tipo: %s\n\n"
                        + "%s\n\n"
                        + "¡Estudia y prepárate adecuadamente!",
                        tipoExamen.toLowerCase(), materia, curso, fecha, hora,
                        materia, tipoExamen, observaciones != null ? observaciones : "Sin observaciones adicionales."
                );

                // Enviar a todos los alumnos del curso
                for (Integer alumnoId : alumnos) {
                    notificationUtil.enviarNotificacionBasica(titulo, contenido, alumnoId);
                }

                System.out.println("✅ Notificación de examen enviada a " + alumnos.size()
                        + " alumnos del curso " + curso);
            }
        }
    }

    /**
     * NUEVO: Notifica sobre suspensión de clases
     */
    public void notificarSuspensionClases(String motivo, String fecha, boolean diaCompleto, String horasAfectadas) {
        if (notificationUtil != null && notificationUtil.puedeEnviarNotificaciones()) {
            String titulo = "⚠️ IMPORTANTE: Suspensión de Clases";
            String contenido;

            if (diaCompleto) {
                contenido = String.format(
                        "Se informa la suspensión de clases para el día %s.\n\n"
                        + "❌ DÍA COMPLETO SIN CLASES\n"
                        + "📝 Motivo: %s\n\n"
                        + "Las clases se reanudarán normalmente al día siguiente, "
                        + "salvo nueva comunicación.\n\n"
                        + "👨‍💼 Comunicado por: Preceptoría",
                        fecha, motivo
                );
            } else {
                contenido = String.format(
                        "Se informa la suspensión parcial de clases para el día %s.\n\n"
                        + "⏰ Horas afectadas: %s\n"
                        + "📝 Motivo: %s\n\n"
                        + "Las demás clases se dictan normalmente.\n\n"
                        + "👨‍💼 Comunicado por: Preceptoría",
                        fecha, horasAfectadas != null ? horasAfectadas : "A confirmar", motivo
                );
            }

            // Enviar a todos los roles
            notificationUtil.enviarNotificacionUrgente(titulo, contenido, preceptorId);
            notificationUtil.enviarNotificacionARol(titulo, contenido, 1); // Administradores
            notificationUtil.enviarNotificacionARol(titulo, contenido, 3); // Profesores
            notificationUtil.enviarNotificacionARol(titulo, contenido, 4); // Alumnos
            notificationUtil.enviarNotificacionARol(titulo, contenido, 5); // ATTP

            System.out.println("🚨 Notificación de suspensión de clases enviada a todos los roles");
        }
    }

    /**
     * NUEVO: Genera reporte de notificaciones enviadas (solo para
     * administración)
     */
    public String generarReporteNotificaciones() {
        if (notificationUtil != null && notificationUtil.puedeGestionarNotificaciones()) {
            return notificationUtil.getEstadisticasNotificaciones();
        } else {
            return "📊 Reporte de Notificaciones - Preceptor ID: " + preceptorId + "\n\n"
                    + "Notificaciones no leídas: " + (notificationUtil != null ? notificationUtil.getNotificacionesNoLeidas() : "N/A") + "\n"
                    + "Estado del sistema: " + (verificarSistemaNotificaciones() ? "ACTIVO" : "INACTIVO") + "\n"
                    + "Última verificación: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
    }

    /**
     * NUEVO: Método de utilidad para obtener información del preceptor
     */
    public String obtenerInfoPreceptor() {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT nombre, apellido, email FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, preceptorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return String.format("%s %s (%s)",
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo información del preceptor: " + e.getMessage());
        }

        return "Preceptor ID: " + preceptorId;
    }

    // ===============================================
    // GETTERS Y SETTERS PARA TESTING
    // ===============================================
    public int getPreceptorId() {
        return preceptorId;
    }

    public Map<String, Integer> getCursosMap() {
        return new HashMap<>(cursosMap);
    }

    public NotificationIntegrationUtil getNotificationUtil() {
        return notificationUtil;
    }

    public VentanaInicio getVentana() {
        return ventana;
    }

    /**
     * NUEVO: Método toString para debugging
     */
    @Override
    public String toString() {
        return String.format(
                "PreceptorPanelManager{preceptorId=%d, cursosDisponibles=%d, notificacionesActivas=%s}",
                preceptorId,
                cursosMap.size(),
                verificarSistemaNotificaciones()
        );
    }
}
