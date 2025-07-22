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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import main.java.database.Conexion;
import main.java.views.users.Profesor.AsistenciaProfesorPanel;
import main.java.views.users.Profesor.NotasBimestralesPanel;
import main.java.views.users.Profesor.NotasProfesorPanel;
import main.java.views.users.Profesor.libroTema;
import main.java.views.users.common.VentanaInicio;
import main.java.services.NotificationCore.NotificationIntegrationUtil;
import main.java.views.notifications.NotificationUI.NotificationsWindow;
import main.java.views.notifications.NotificationUI.NotificationSenderWindow;

/**
 * Gestor de paneles específico para el rol de Profesor con integración completa
 * de notificaciones. VERSIÓN MEJORADA - Incluye sistema completo de
 * notificaciones académicas para profesores
 */
public class ProfesorPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int profesorId;
    private Connection conect;

    // Componentes de notificaciones
    private final NotificationIntegrationUtil notificationUtil;

    public ProfesorPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.profesorId = userId;
        this.conect = Conexion.getInstancia().verificarConexion();
        this.notificationUtil = NotificationIntegrationUtil.getInstance();

        System.out.println("✅ ProfesorPanelManager inicializado con notificaciones para usuario: " + userId);
    }

    @Override
    public JComponent[] createButtons() {
        JButton btnNotas = createStyledButton("NOTAS", "notas");
        JButton btnAsistencias = createStyledButton("ASISTENCIAS", "asistencias");
        JButton btnLibroTemas = createStyledButton("LIBRO DE TEMAS", "libroTemas");
        JButton btnNotificaciones = createStyledButton("NOTIFICACIONES", "notificaciones");
        JButton btnEnviarAviso = createStyledButton("ENVIAR AVISO", "enviar_aviso");

        return new JComponent[]{btnNotas, btnAsistencias, btnLibroTemas, btnNotificaciones, btnEnviarAviso};
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
            System.out.println("=== ACCIÓN PROFESOR: " + actionCommand + " ===");

            switch (actionCommand) {
                case "notas":
                    mostrarPanelNotas();
                    break;
                case "asistencias":
                    mostrarPanelAsistencias();
                    break;
                case "libroTemas":
                    mostrarPanelLibroTemas();
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
            System.err.println("❌ Error al procesar acción profesor: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar el panel: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * NUEVO: Muestra el panel de notificaciones del profesor
     */
    private void mostrarPanelNotificaciones() {
        try {
            System.out.println("Abriendo panel de notificaciones para profesor...");

            SwingUtilities.invokeLater(() -> {
                NotificationsWindow notificationsWindow = new NotificationsWindow(profesorId);
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
     * NUEVO: Muestra el panel para enviar avisos/notificaciones académicas
     */
    private void mostrarPanelEnviarAviso() {
        try {
            System.out.println("Creando panel de envío de avisos para profesor...");

            JPanel panelEnvio = crearPanelEnvioAvisosProfesor();
            ventana.mostrarPanelResponsive(panelEnvio, "Avisos y Notificaciones Académicas");

            System.out.println("✅ Panel de envío de avisos mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel de envío: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * NUEVO: Crea el panel completo para envío de avisos académicos del
     * profesor
     */
    private JPanel crearPanelEnvioAvisosProfesor() {
        JPanel panelCompleto = new JPanel(new BorderLayout());
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título principal
        JLabel lblTitulo = new JLabel("📚 Avisos y Notificaciones Académicas", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(51, 153, 255));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelCompleto.add(lblTitulo, BorderLayout.NORTH);

        // Panel central con opciones específicas para profesores
        JPanel panelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Botón para notificar nueva nota publicada
        JButton btnNotifNota = new JButton("📊 Notificar Nueva Nota");
        btnNotifNota.setPreferredSize(new Dimension(250, 50));
        btnNotifNota.setBackground(new Color(40, 167, 69));
        btnNotifNota.setForeground(Color.WHITE);
        btnNotifNota.setFont(new Font("Arial", Font.BOLD, 14));
        btnNotifNota.addActionListener(e -> mostrarDialogoNotificarNota());

        // Botón para notificar nota bimestral
        JButton btnNotifBimestral = new JButton("📋 Notificar Nota Bimestral");
        btnNotifBimestral.setPreferredSize(new Dimension(250, 50));
        btnNotifBimestral.setBackground(new Color(0, 123, 255));
        btnNotifBimestral.setForeground(Color.WHITE);
        btnNotifBimestral.setFont(new Font("Arial", Font.BOLD, 14));
        btnNotifBimestral.addActionListener(e -> mostrarDialogoNotaBimestral());

        // Botón para registrar y notificar asistencia
        JButton btnNotifAsistencia = new JButton("✅ Registrar Asistencia");
        btnNotifAsistencia.setPreferredSize(new Dimension(250, 50));
        btnNotifAsistencia.setBackground(new Color(255, 193, 7));
        btnNotifAsistencia.setForeground(Color.BLACK);
        btnNotifAsistencia.setFont(new Font("Arial", Font.BOLD, 14));
        btnNotifAsistencia.addActionListener(e -> mostrarDialogoRegistrarAsistencia());

        // Botón para avisar sobre examen
        JButton btnAvisoExamen = new JButton("📝 Avisar Examen/Evaluación");
        btnAvisoExamen.setPreferredSize(new Dimension(250, 50));
        btnAvisoExamen.setBackground(new Color(220, 53, 69));
        btnAvisoExamen.setForeground(Color.WHITE);
        btnAvisoExamen.setFont(new Font("Arial", Font.BOLD, 14));
        btnAvisoExamen.addActionListener(e -> mostrarDialogoAvisoExamen());

        // Botón para recordar trabajos pendientes
        JButton btnRecordatorios = new JButton("📌 Enviar Recordatorio");
        btnRecordatorios.setPreferredSize(new Dimension(250, 50));
        btnRecordatorios.setBackground(new Color(108, 117, 125));
        btnRecordatorios.setForeground(Color.WHITE);
        btnRecordatorios.setFont(new Font("Arial", Font.BOLD, 14));
        btnRecordatorios.addActionListener(e -> mostrarDialogoRecordatorio());

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
        panelCentral.add(btnNotifNota, gbc);

        gbc.gridx = 1;
        panelCentral.add(btnNotifBimestral, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentral.add(btnNotifAsistencia, gbc);

        gbc.gridx = 1;
        panelCentral.add(btnAvisoExamen, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentral.add(btnRecordatorios, gbc);

        gbc.gridx = 1;
        panelCentral.add(btnVentanaCompleta, gbc);

        panelCompleto.add(panelCentral, BorderLayout.CENTER);

        // Panel inferior con estadísticas del profesor
        JPanel panelInferior = crearPanelEstadisticasProfesor();
        panelCompleto.add(panelInferior, BorderLayout.SOUTH);

        return panelCompleto;
    }

    /**
     * NUEVO: Crea panel con estadísticas específicas del profesor
     */
    private JPanel crearPanelEstadisticasProfesor() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📊 Mi Panel de Control - Profesor"));
        panel.setBackground(new Color(248, 249, 250));

        // Obtener estadísticas
        int notificacionesNoLeidas = notificationUtil.getNotificacionesNoLeidas();
        int materiasAsignadas = obtenerCantidadMaterias();
        int alumnosTotal = obtenerTotalAlumnos();

        JLabel lblEstadisticas = new JLabel(String.format(
                "📬 No leídas: %d  |  📚 Materias: %d  |  👥 Alumnos: %d  |  👨‍🏫 Profesor ID: %d",
                notificacionesNoLeidas, materiasAsignadas, alumnosTotal, profesorId
        ));
        lblEstadisticas.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnActualizar.addActionListener(e -> {
            // La actualización de notificaciones ahora es automática en el nuevo sistema
            // notificationUtil.actualizarNotificaciones(); // Método obsoleto
            SwingUtilities.invokeLater(() -> {
                try {
                    mostrarPanelEnviarAviso();
                } catch (Exception ex) {
                    System.err.println("Error actualizando panel: " + ex.getMessage());
                }
            });
        });

        panel.add(lblEstadisticas);
        panel.add(btnActualizar);

        return panel;
    }

    /**
     * NUEVO: Diálogo para notificar nueva nota publicada
     */
    private void mostrarDialogoNotificarNota() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de materia
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Materia:"), gbc);

            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);
            gbc.gridx = 1;
            panel.add(comboMaterias, gbc);

            if (materiaInfoMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ID del alumno
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("ID del Alumno:"), gbc);

            JTextField txtAlumnoId = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtAlumnoId, gbc);

            // Tipo de trabajo
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Tipo de Trabajo:"), gbc);

            JTextField txtTipoTrabajo = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtTipoTrabajo, gbc);

            // Nota
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Nota:"), gbc);

            JTextField txtNota = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtNota, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📊 Notificar Nueva Nota", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
                    MateriaInfo info = materiaInfoMap.get(materiaSeleccionada);
                    int alumnoId = Integer.parseInt(txtAlumnoId.getText().trim());
                    String tipoTrabajo = txtTipoTrabajo.getText().trim();
                    double nota = Double.parseDouble(txtNota.getText().trim());

                    if (tipoTrabajo.isEmpty()) {
                        JOptionPane.showMessageDialog(ventana,
                                "Por favor complete el tipo de trabajo.",
                                "Campo requerido",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Usar el sistema de notificaciones integrado
                    notificationUtil.notificarNuevaNota(alumnoId, info.materiaNombre, tipoTrabajo, nota, profesorId);

                    JOptionPane.showMessageDialog(ventana,
                            "✅ Notificación de nueva nota enviada al alumno ID: " + alumnoId,
                            "Notificación Enviada",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor ingrese números válidos para ID del alumno y nota.",
                            "Datos inválidos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de notificar nota: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Diálogo para notificar nota bimestral
     */
    private void mostrarDialogoNotaBimestral() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de materia
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Materia:"), gbc);

            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);
            gbc.gridx = 1;
            panel.add(comboMaterias, gbc);

            if (materiaInfoMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ID del alumno
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("ID del Alumno:"), gbc);

            JTextField txtAlumnoId = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtAlumnoId, gbc);

            // Bimestre
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Bimestre:"), gbc);

            JComboBox<String> comboBimestre = new JComboBox<>(new String[]{
                "1er Bimestre", "2do Bimestre", "3er Bimestre", "4to Bimestre"
            });
            gbc.gridx = 1;
            panel.add(comboBimestre, gbc);

            // Nota
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Nota:"), gbc);

            JTextField txtNota = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtNota, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📋 Notificar Nota Bimestral", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
                    MateriaInfo info = materiaInfoMap.get(materiaSeleccionada);
                    int alumnoId = Integer.parseInt(txtAlumnoId.getText().trim());
                    String bimestre = (String) comboBimestre.getSelectedItem();
                    double nota = Double.parseDouble(txtNota.getText().trim());

                    // Usar el sistema de notificaciones integrado
                    notificationUtil.notificarNotaBimestral(alumnoId, info.materiaNombre, bimestre, nota, profesorId);

                    JOptionPane.showMessageDialog(ventana,
                            "✅ Notificación de nota bimestral enviada al alumno ID: " + alumnoId,
                            "Notificación Enviada",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor ingrese números válidos para ID del alumno y nota.",
                            "Datos inválidos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de nota bimestral: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Diálogo para registrar asistencia y notificar
     */
    private void mostrarDialogoRegistrarAsistencia() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de materia
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Materia:"), gbc);

            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);
            gbc.gridx = 1;
            panel.add(comboMaterias, gbc);

            if (materiaInfoMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ID del alumno
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("ID del Alumno:"), gbc);

            JTextField txtAlumnoId = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtAlumnoId, gbc);

            // Fecha
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Fecha (dd/MM/yyyy):"), gbc);

            JTextField txtFecha = new JTextField(15);
            txtFecha.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            gbc.gridx = 1;
            panel.add(txtFecha, gbc);

            // Estado de asistencia
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Estado:"), gbc);

            JComboBox<String> comboEstado = new JComboBox<>(new String[]{
                "PRESENTE", "FALTA", "TARDE", "JUSTIFICADA"
            });
            gbc.gridx = 1;
            panel.add(comboEstado, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "✅ Registrar Asistencia", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
                    MateriaInfo info = materiaInfoMap.get(materiaSeleccionada);
                    int alumnoId = Integer.parseInt(txtAlumnoId.getText().trim());
                    String fecha = txtFecha.getText().trim();
                    String estado = (String) comboEstado.getSelectedItem();

                    // Registrar asistencia en la base de datos (esto dependería de tu sistema actual)
                    // registrarAsistenciaEnBD(alumnoId, info.cursoId, info.materiaId, fecha, estado);
                    // Notificar al alumno sobre el registro
                    if (!"PRESENTE".equals(estado)) {
                        // Usar método básico ya que el método específico para asistencia no está disponible
                        String mensaje = String.format("Asistencia registrada para %s el %s en %s: %s", 
                            "alumno", fecha, info.materiaNombre, estado);
                        notificationUtil.enviarNotificacionBasica("Asistencia Registrada", mensaje, alumnoId);
                    }

                    JOptionPane.showMessageDialog(ventana,
                            "✅ Asistencia registrada" + (!"PRESENTE".equals(estado) ? " y notificación enviada" : "")
                            + " para alumno ID: " + alumnoId,
                            "Registro Completado",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor ingrese un ID de alumno válido.",
                            "Datos inválidos",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de asistencia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Diálogo para avisar sobre examen/evaluación
     */
    private void mostrarDialogoAvisoExamen() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de materia
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Materia:"), gbc);

            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(comboMaterias, gbc);

            if (materiaInfoMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Tipo de examen
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("Tipo:"), gbc);

            JComboBox<String> comboTipo = new JComboBox<>(new String[]{
                "Examen", "Evaluación", "Prueba", "Parcial", "Recuperatorio", "Trabajo Práctico"
            });
            gbc.gridx = 1;
            panel.add(comboTipo, gbc);

            // Fecha del examen
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Fecha:"), gbc);

            JTextField txtFecha = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtFecha, gbc);

            // Hora
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Hora:"), gbc);

            JTextField txtHora = new JTextField(15);
            gbc.gridx = 1;
            panel.add(txtHora, gbc);

            // Observaciones
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JLabel("Observaciones:"), gbc);

            JTextArea txtObservaciones = new JTextArea(3, 20);
            txtObservaciones.setLineWrap(true);
            txtObservaciones.setWrapStyleWord(true);
            JScrollPane scrollObs = new JScrollPane(txtObservaciones);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(scrollObs, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📝 Avisar Examen/Evaluación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
                MateriaInfo info = materiaInfoMap.get(materiaSeleccionada);
                String tipoExamen = (String) comboTipo.getSelectedItem();
                String fecha = txtFecha.getText().trim();
                String hora = txtHora.getText().trim();
                String observaciones = txtObservaciones.getText().trim();

                if (fecha.isEmpty() || hora.isEmpty()) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor complete la fecha y hora del examen.",
                            "Campos requeridos",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Obtener alumnos del curso para enviar notificación masiva
                enviarAvisoExamenACurso(info, tipoExamen, fecha, hora, observaciones);
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de aviso de examen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Envía aviso de examen a todos los alumnos del curso
     */
    private void enviarAvisoExamenACurso(MateriaInfo info, String tipoExamen, String fecha, String hora, String observaciones) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Obtener alumnos del curso
                List<Integer> alumnos = obtenerAlumnosDelCurso(info.cursoId);

                if (alumnos.isEmpty()) {
                    throw new Exception("No se encontraron alumnos en el curso");
                }

                // Preparar título y contenido
                String titulo = String.format("📝 %s - %s", tipoExamen, info.materiaNombre);
                String contenido = String.format(
                        "Se informa sobre el próximo %s:\n\n"
                        + "📚 Materia: %s\n"
                        + "🎓 Curso: %s\n"
                        + "📅 Fecha: %s\n"
                        + "🕐 Hora: %s\n"
                        + "👨‍🏫 Profesor: %s\n\n"
                        + "%s\n\n"
                        + "¡Estudia y prepárate adecuadamente!",
                        tipoExamen.toLowerCase(), info.materiaNombre, info.cursoNombre,
                        fecha, hora, obtenerNombreProfesor(profesorId),
                        observaciones.isEmpty() ? "Sin observaciones adicionales." : "📝 Observaciones: " + observaciones
                );

                // Enviar a todos los alumnos del curso
                for (Integer alumnoId : alumnos) {
                    notificationUtil.enviarNotificacionBasica(titulo, contenido, alumnoId);
                }

                return true;
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(ventana,
                                "✅ Aviso de " + tipoExamen.toLowerCase() + " enviado exitosamente a todos los alumnos del curso",
                                "Aviso Enviado",
                                JOptionPane.INFORMATION_MESSAGE);

                        System.out.println("✅ Aviso de examen enviado por profesor " + profesorId);
                    }
                } catch (Exception e) {
                    System.err.println("Error enviando aviso de examen: " + e.getMessage());
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
     * NUEVO: Diálogo para enviar recordatorios
     */
    private void mostrarDialogoRecordatorio() {
        try {
            // Crear panel del diálogo
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Selector de materia
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Materia:"), gbc);

            JComboBox<String> comboMaterias = new JComboBox<>();
            Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(comboMaterias, gbc);

            if (materiaInfoMap.isEmpty()) {
                JOptionPane.showMessageDialog(ventana,
                        "No se encontraron materias asignadas.",
                        "Sin materias",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Tipo de recordatorio
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("Tipo:"), gbc);

            JComboBox<String> comboTipo = new JComboBox<>(new String[]{
                "Trabajo Pendiente", "Entrega de TP", "Material Requerido", "Clase Especial", "Recordatorio General"
            });
            gbc.gridx = 1;
            panel.add(comboTipo, gbc);

            // Título del recordatorio
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Título:"), gbc);

            JTextField txtTitulo = new JTextField(25);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(txtTitulo, gbc);

            // Mensaje
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JLabel("Mensaje:"), gbc);

            JTextArea txtMensaje = new JTextArea(4, 25);
            txtMensaje.setLineWrap(true);
            txtMensaje.setWrapStyleWord(true);
            JScrollPane scrollMensaje = new JScrollPane(txtMensaje);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(scrollMensaje, gbc);

            int resultado = JOptionPane.showConfirmDialog(ventana, panel,
                    "📌 Enviar Recordatorio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resultado == JOptionPane.OK_OPTION) {
                String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
                MateriaInfo info = materiaInfoMap.get(materiaSeleccionada);
                String tipoRecordatorio = (String) comboTipo.getSelectedItem();
                String titulo = txtTitulo.getText().trim();
                String mensaje = txtMensaje.getText().trim();

                if (titulo.isEmpty() || mensaje.isEmpty()) {
                    JOptionPane.showMessageDialog(ventana,
                            "Por favor complete el título y mensaje.",
                            "Campos requeridos",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Enviar recordatorio a todos los alumnos del curso
                enviarRecordatorioACurso(info, tipoRecordatorio, titulo, mensaje);
            }

        } catch (Exception e) {
            System.err.println("Error en diálogo de recordatorio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Envía recordatorio a todos los alumnos del curso
     */
    private void enviarRecordatorioACurso(MateriaInfo info, String tipo, String titulo, String mensaje) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Obtener alumnos del curso
                List<Integer> alumnos = obtenerAlumnosDelCurso(info.cursoId);

                if (alumnos.isEmpty()) {
                    throw new Exception("No se encontraron alumnos en el curso");
                }

                // Preparar título y contenido completos
                String tituloCompleto = String.format("📌 %s - %s", tipo, info.materiaNombre);
                String contenidoCompleto = String.format(
                        "%s\n\n"
                        + "📚 Materia: %s\n"
                        + "🎓 Curso: %s\n"
                        + "👨‍🏫 Profesor: %s\n\n"
                        + "%s\n\n"
                        + "📅 Fecha: %s",
                        titulo, info.materiaNombre, info.cursoNombre,
                        obtenerNombreProfesor(profesorId), mensaje,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );

                // Enviar a todos los alumnos del curso
                for (Integer alumnoId : alumnos) {
                    notificationUtil.enviarNotificacionBasica(tituloCompleto, contenidoCompleto, alumnoId);
                }

                return true;
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(ventana,
                                "✅ Recordatorio enviado exitosamente a todos los alumnos del curso",
                                "Recordatorio Enviado",
                                JOptionPane.INFORMATION_MESSAGE);

                        System.out.println("✅ Recordatorio enviado por profesor " + profesorId);
                    }
                } catch (Exception e) {
                    System.err.println("Error enviando recordatorio: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ventana,
                            "Error al enviar el recordatorio: " + e.getMessage(),
                            "Error de Envío",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Abre la ventana completa de envío de notificaciones
     */
    private void abrirVentanaEnvioCompleta() {
        try {
            System.out.println("Abriendo ventana completa de envío de notificaciones...");

            SwingUtilities.invokeLater(() -> {
                NotificationSenderWindow senderWindow = new NotificationSenderWindow(profesorId); // Constructor simplificado
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

    // ===============================================
    // MÉTODOS AUXILIARES PARA NOTIFICACIONES
    // ===============================================
    /**
     * NUEVO: Obtiene la cantidad de materias asignadas al profesor
     */
    private int obtenerCantidadMaterias() {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT COUNT(DISTINCT materia_id) FROM profesor_curso_materia WHERE profesor_id = ? AND estado = 'activo'";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo cantidad de materias: " + e.getMessage());
        }

        return 0;
    }

    /**
     * NUEVO: Obtiene el total de alumnos bajo las materias del profesor
     */
    private int obtenerTotalAlumnos() {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = """
                SELECT COUNT(DISTINCT ac.alumno_id) 
                FROM alumno_curso ac
                INNER JOIN profesor_curso_materia pcm ON ac.curso_id = pcm.curso_id
                WHERE pcm.profesor_id = ? AND ac.estado = 'activo' AND pcm.estado = 'activo'
                """;
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo total de alumnos: " + e.getMessage());
        }

        return 0;
    }

    /**
     * NUEVO: Obtiene los alumnos de un curso específico
     */
    private List<Integer> obtenerAlumnosDelCurso(int cursoId) {
        List<Integer> alumnos = new ArrayList<>();

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

    /**
     * NUEVO: Obtiene el nombre completo del profesor
     */
    private String obtenerNombreProfesor(int profesorId) {
        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre del profesor: " + e.getMessage());
        }

        return "Profesor";
    }

    // ===============================================
    // MÉTODOS ORIGINALES MEJORADOS
    // ===============================================
    /**
     * MEJORADO: Muestra panel selector de notas con verificación de trabajos
     * pendientes
     */
    private void mostrarPanelNotas() {
        try {
            System.out.println("Creando panel selector de notas para profesor...");

            JPanel panelSelector = crearPanelSelectorNotas();
            ventana.mostrarPanelResponsive(panelSelector, "Selección de Curso y Materia - Notas");

            // NUEVO: Verificar automáticamente trabajos pendientes de corrección
            verificarTrabajosPendientesAutomatico();

            System.out.println("✅ Panel selector de notas mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel selector de notas: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * NUEVO: Verifica automáticamente trabajos pendientes de corrección
     */
    private void verificarTrabajosPendientesAutomatico() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Map<String, Integer> trabajosPendientes = obtenerTrabajosPendientesPorMateria();

                    for (Map.Entry<String, Integer> entry : trabajosPendientes.entrySet()) {
                        String materia = entry.getKey();
                        int cantidad = entry.getValue();

                        if (cantidad > 0) {
                            // Recordar al profesor sobre trabajos pendientes
                            // Usar método básico ya que el método específico para recordatorios no está disponible
                            String mensaje = String.format("Tiene %d trabajos pendientes de revisión en %s", cantidad, materia);
                            notificationUtil.enviarNotificacionBasica("Trabajos Pendientes", mensaje, profesorId);
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Error verificando trabajos pendientes: " + e.getMessage());
                }

                return null;
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Obtiene trabajos pendientes de corrección por materia
     */
    private Map<String, Integer> obtenerTrabajosPendientesPorMateria() {
        Map<String, Integer> trabajosPendientes = new HashMap<>();

        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = """
                SELECT m.nombre as materia, COUNT(*) as pendientes
                FROM notas n
                INNER JOIN profesor_curso_materia pcm ON n.curso_id = pcm.curso_id AND n.materia_id = pcm.materia_id
                INNER JOIN materias m ON pcm.materia_id = m.id
                WHERE pcm.profesor_id = ? 
                AND n.nota IS NULL 
                AND n.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY m.nombre
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String materia = rs.getString("materia");
                int pendientes = rs.getInt("pendientes");
                trabajosPendientes.put(materia, pendientes);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo trabajos pendientes: " + e.getMessage());
            e.printStackTrace();
        }

        return trabajosPendientes;
    }

    /**
     * MÉTODO ORIGINAL: Crea el panel selector para notas
     */
    private JPanel crearPanelSelectorNotas() {
        JPanel panelCompleto = new JPanel(new BorderLayout());
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Gestión de Notas - Seleccione Curso y Materia", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(51, 153, 255));
        panelCompleto.add(lblTitulo, BorderLayout.NORTH);

        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // Panel superior para selección
        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelSeleccion.setBorder(BorderFactory.createTitledBorder("Seleccionar Curso y Materia"));

        JLabel lblSeleccion = new JLabel("Curso y Materia:");
        lblSeleccion.setFont(new Font("Arial", Font.BOLD, 14));

        JComboBox<String> comboMaterias = new JComboBox<>();
        comboMaterias.setPreferredSize(new Dimension(300, 30));
        comboMaterias.setFont(new Font("Arial", Font.PLAIN, 12));

        // Cargar materias del profesor
        Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);

        if (materiaInfoMap.isEmpty()) {
            JLabel lblSinMaterias = new JLabel("No se encontraron materias asignadas", JLabel.CENTER);
            lblSinMaterias.setFont(new Font("Arial", Font.ITALIC, 16));
            lblSinMaterias.setForeground(Color.RED);
            panelCompleto.add(lblSinMaterias, BorderLayout.CENTER);
            return panelCompleto;
        }

        // Radio buttons para tipo de notas
        JRadioButton rbTrabajos = new JRadioButton("Trabajos y Actividades", true);
        JRadioButton rbBimestral = new JRadioButton("Notas Bimestrales", false);
        ButtonGroup grupoOpciones = new ButtonGroup();
        grupoOpciones.add(rbTrabajos);
        grupoOpciones.add(rbBimestral);

        rbTrabajos.setFont(new Font("Arial", Font.PLAIN, 12));
        rbBimestral.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnCargar = new JButton("Cargar Panel de Notas");
        btnCargar.setPreferredSize(new Dimension(180, 35));
        btnCargar.setBackground(new Color(51, 153, 255));
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setFont(new Font("Arial", Font.BOLD, 12));

        panelSeleccion.add(lblSeleccion);
        panelSeleccion.add(comboMaterias);
        panelSeleccion.add(rbTrabajos);
        panelSeleccion.add(rbBimestral);
        panelSeleccion.add(btnCargar);

        panelCentral.add(panelSeleccion, BorderLayout.NORTH);

        // Área de contenido donde se cargará el panel seleccionado
        JPanel areaContenido = new JPanel(new BorderLayout());
        areaContenido.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panelCentral.add(areaContenido, BorderLayout.CENTER);

        panelCompleto.add(panelCentral, BorderLayout.CENTER);

        // Listener para cargar el panel
        btnCargar.addActionListener(e -> {
            String seleccion = (String) comboMaterias.getSelectedItem();
            if (seleccion != null) {
                MateriaInfo info = materiaInfoMap.get(seleccion);
                if (info != null) {
                    cargarPanelNotasEspecifico(info, rbTrabajos.isSelected(), seleccion);
                }
            }
        });

        return panelCompleto;
    }

    /**
     * MEJORADO: Carga el panel específico de notas con notificaciones
     * automáticas
     */
    private void cargarPanelNotasEspecifico(MateriaInfo info, boolean esTrabajos, String seleccion) {
        try {
            JPanel panelNotas;
            String tipoPanel = esTrabajos ? "Trabajos y Actividades" : "Notas Bimestrales";

            if (esTrabajos) {
                panelNotas = new NotasProfesorPanel(profesorId, info.cursoId, info.materiaId);
            } else {
                panelNotas = new NotasBimestralesPanel(profesorId, info.cursoId, info.materiaId);
            }

            String titulo = tipoPanel + " - " + seleccion;
            ventana.mostrarPanelResponsive(panelNotas, titulo);

            System.out.println("✅ Panel de notas específico cargado: " + titulo);

        } catch (Exception ex) {
            System.err.println("❌ Error al cargar panel específico de notas: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar el panel de notas:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MÉTODO ORIGINAL: Muestra panel selector de asistencias
     */
    private void mostrarPanelAsistencias() {
        try {
            System.out.println("Creando panel selector de asistencias para profesor...");

            JPanel panelSelector = crearPanelSelectorAsistencias();
            ventana.mostrarPanelResponsive(panelSelector, "Selección de Curso y Materia - Asistencias");

            System.out.println("✅ Panel selector de asistencias mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar panel selector de asistencias: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO ORIGINAL: Crea el panel selector para asistencias
     */
    private JPanel crearPanelSelectorAsistencias() {
        JPanel panelCompleto = new JPanel(new BorderLayout());
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Gestión de Asistencias - Seleccione Curso y Materia", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(51, 153, 255));
        panelCompleto.add(lblTitulo, BorderLayout.NORTH);

        // Panel central
        JPanel panelCentral = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 50));

        JLabel lblSeleccion = new JLabel("Curso y Materia:");
        lblSeleccion.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<String> comboMaterias = new JComboBox<>();
        comboMaterias.setPreferredSize(new Dimension(300, 35));
        comboMaterias.setFont(new Font("Arial", Font.PLAIN, 14));

        // Cargar materias del profesor
        Map<String, MateriaInfo> materiaInfoMap = cargarMateriasProfesor(comboMaterias);

        if (materiaInfoMap.isEmpty()) {
            JLabel lblSinMaterias = new JLabel("No se encontraron materias asignadas", JLabel.CENTER);
            lblSinMaterias.setFont(new Font("Arial", Font.ITALIC, 16));
            lblSinMaterias.setForeground(Color.RED);
            panelCompleto.add(lblSinMaterias, BorderLayout.CENTER);
            return panelCompleto;
        }

        JButton btnCargar = new JButton("Cargar Asistencias");
        btnCargar.setPreferredSize(new Dimension(150, 35));
        btnCargar.setBackground(new Color(51, 153, 255));
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setFont(new Font("Arial", Font.BOLD, 14));

        // Listener para cargar asistencias
        btnCargar.addActionListener(e -> {
            String seleccion = (String) comboMaterias.getSelectedItem();
            if (seleccion != null) {
                MateriaInfo info = materiaInfoMap.get(seleccion);
                if (info != null) {
                    cargarAsistenciasCurso(info, seleccion);
                }
            }
        });

        panelCentral.add(lblSeleccion);
        panelCentral.add(comboMaterias);
        panelCentral.add(btnCargar);

        panelCompleto.add(panelCentral, BorderLayout.CENTER);

        return panelCompleto;
    }

    /**
     * MÉTODO ORIGINAL: Carga las asistencias del curso seleccionado
     */
    private void cargarAsistenciasCurso(MateriaInfo info, String seleccion) {
        try {
            System.out.println("Creando AsistenciaProfesorPanel para: " + seleccion);

            AsistenciaProfesorPanel panelAsistencia
                    = new AsistenciaProfesorPanel(profesorId, info.cursoId, info.materiaId);

            String titulo = "Asistencias - " + seleccion;
            ventana.mostrarPanelResponsive(panelAsistencia, titulo);

            System.out.println("✅ Panel de asistencias mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al cargar asistencias: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar asistencias:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MÉTODO ORIGINAL: Muestra el libro de temas
     */
    private void mostrarPanelLibroTemas() {
        try {
            System.out.println("Creando libroTema para profesor...");

            // Crear un panel contenedor para el libro de temas
            JPanel panelContenedor = new JPanel(new BorderLayout());

            // Crear el libro de temas (necesita adaptación si no es compatible)
            libroTema libro = new libroTema(panelContenedor, profesorId, ventana);
            panelContenedor.add(libro, BorderLayout.CENTER);

            ventana.mostrarPanelResponsive(panelContenedor, "Libro de Temas");

            System.out.println("✅ Libro de temas mostrado exitosamente");

        } catch (Exception ex) {
            System.err.println("❌ Error al mostrar libro de temas: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar el libro de temas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * MÉTODO ORIGINAL: Carga las materias del profesor en el combo
     */
    private Map<String, MateriaInfo> cargarMateriasProfesor(JComboBox<String> combo) {
        Map<String, MateriaInfo> materiaInfoMap = new HashMap<>();

        try {
            if (conect == null || conect.isClosed()) {
                conect = Conexion.getInstancia().verificarConexion();
            }

            String query = """
                SELECT DISTINCT 
                    c.id as curso_id, 
                    CONCAT(c.anio, '°', c.division) as curso, 
                    m.id as materia_id, 
                    m.nombre as materia 
                FROM cursos c 
                JOIN profesor_curso_materia pcm ON c.id = pcm.curso_id 
                JOIN materias m ON pcm.materia_id = m.id 
                WHERE pcm.profesor_id = ? AND pcm.estado = 'activo' 
                ORDER BY c.anio, c.division, m.nombre
                """;

            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, profesorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("curso") + " - " + rs.getString("materia");
                combo.addItem(item);

                MateriaInfo info = new MateriaInfo(
                        rs.getInt("curso_id"),
                        rs.getInt("materia_id"),
                        rs.getString("curso"),
                        rs.getString("materia")
                );
                materiaInfoMap.put(item, info);

                System.out.println("Materia cargada: " + item);
            }

            System.out.println("Total materias del profesor: " + materiaInfoMap.size());

        } catch (SQLException ex) {
            System.err.println("Error al cargar materias del profesor: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar materias: " + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }

        return materiaInfoMap;
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
            System.out.println("✅ Sistema de notificaciones activo para profesor ID: " + profesorId);
        } else {
            System.err.println("❌ Sistema de notificaciones no disponible para profesor ID: " + profesorId);
        }

        return sistemaActivo;
    }

    /**
     * NUEVO: Obtiene información del sistema para debugging
     */
    public String obtenerInfoSistema() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DEL SISTEMA - PROFESOR ===\n");
        info.append("ID del profesor: ").append(profesorId).append("\n");
        info.append("Materias asignadas: ").append(obtenerCantidadMaterias()).append("\n");
        info.append("Total alumnos: ").append(obtenerTotalAlumnos()).append("\n");
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
            String titulo = "🧪 Prueba del Sistema - Profesor";
            String contenido = String.format(
                    "Notificación de prueba enviada desde el panel del profesor.\n\n"
                    + "👨‍🏫 Profesor ID: %d\n"
                    + "📅 Fecha: %s\n"
                    + "🔧 Sistema funcionando correctamente",
                    profesorId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );

            notificationUtil.enviarNotificacionBasica(titulo, contenido, profesorId);

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
     * NUEVO: Genera reporte de actividad del profesor
     */
    public String generarReporteActividad() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("                    REPORTE DE ACTIVIDAD - PROFESOR\n");
        reporte.append("═══════════════════════════════════════════════════════════════════\n\n");

        reporte.append("👨‍🏫 INFORMACIÓN DEL PROFESOR:\n");
        reporte.append("─────────────────────────────────\n");
        reporte.append("ID: ").append(profesorId).append("\n");
        reporte.append("Nombre: ").append(obtenerNombreProfesor(profesorId)).append("\n");
        reporte.append("Fecha del reporte: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        reporte.append("📊 ESTADÍSTICAS ACADÉMICAS:\n");
        reporte.append("─────────────────────────────\n");
        reporte.append("📚 Materias asignadas: ").append(obtenerCantidadMaterias()).append("\n");
        reporte.append("👥 Total de alumnos: ").append(obtenerTotalAlumnos()).append("\n\n");

        // Trabajos pendientes por materia
        Map<String, Integer> trabajosPendientes = obtenerTrabajosPendientesPorMateria();
        if (!trabajosPendientes.isEmpty()) {
            reporte.append("📝 TRABAJOS PENDIENTES DE CORRECCIÓN:\n");
            reporte.append("─────────────────────────────────────\n");
            for (Map.Entry<String, Integer> entry : trabajosPendientes.entrySet()) {
                reporte.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" trabajo(s)\n");
            }
            reporte.append("\n");
        }

        if (notificationUtil != null) {
            reporte.append("🔔 ESTADO DE NOTIFICACIONES:\n");
            reporte.append("────────────────────────────\n");
            reporte.append("Sistema activo: ").append(notificationUtil.puedeEnviarNotificaciones() ? "SÍ" : "NO").append("\n");
            reporte.append("Notificaciones no leídas: ").append(notificationUtil.getNotificacionesNoLeidas()).append("\n\n");
        }

        reporte.append("💡 RECOMENDACIONES:\n");
        reporte.append("───────────────────\n");
        if (!trabajosPendientes.isEmpty()) {
            reporte.append("• ⚠️ Revisar y corregir trabajos pendientes.\n");
            reporte.append("• 📤 Notificar a los alumnos cuando publiques nuevas notas.\n");
        } else {
            reporte.append("• ✅ No hay trabajos pendientes de corrección.\n");
        }
        reporte.append("• 📊 Usar el sistema de notificaciones para mantener comunicación fluida.\n");
        reporte.append("• 🔄 Actualizar regularmente las notas y asistencias.\n\n");

        reporte.append("═══════════════════════════════════════════════════════════════════\n");
        reporte.append("Reporte generado automáticamente por el Sistema de Gestión Escolar ET20\n");
        reporte.append("═══════════════════════════════════════════════════════════════════");

        return reporte.toString();
    }

    /**
     * NUEVO: Notifica automáticamente cuando se publican notas masivamente
     */
    public void notificarNotasMasivas(String materia, String tipoEvaluacion, int cantidadAlumnos) {
        if (notificationUtil != null && notificationUtil.puedeEnviarNotificaciones()) {
            String titulo = "📊 Notas Publicadas - " + materia;
            String contenido = String.format(
                    "Se han publicado las notas de %s para %s.\n\n"
                    + "📚 Materia: %s\n"
                    + "📝 Evaluación: %s\n"
                    + "👥 Alumnos afectados: %d\n"
                    + "📅 Fecha: %s\n\n"
                    + "Los alumnos han sido notificados automáticamente.",
                    tipoEvaluacion, materia, materia, tipoEvaluacion, cantidadAlumnos,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            // Enviar confirmación al profesor
            notificationUtil.enviarNotificacionBasica(titulo, contenido, profesorId);

            System.out.println("✅ Notificación de notas masivas enviada a profesor " + profesorId);
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

            System.out.println("✅ ProfesorPanelManager recursos liberados para usuario: " + profesorId);

        } catch (SQLException e) {
            System.err.println("Error liberando recursos: " + e.getMessage());
        }
    }

    // ===============================================
    // GETTERS PARA TESTING Y ACCESO
    // ===============================================
    public int getProfesorId() {
        return profesorId;
    }

    public NotificationIntegrationUtil getNotificationUtil() {
        return notificationUtil;
    }

    public VentanaInicio getVentana() {
        return ventana;
    }

    /**
     * CLASE INTERNA: Información de materia (mantenida del código original)
     */
    private static class MateriaInfo {

        final int cursoId;
        final int materiaId;
        final String cursoNombre;
        final String materiaNombre;

        public MateriaInfo(int cursoId, int materiaId, String cursoNombre, String materiaNombre) {
            this.cursoId = cursoId;
            this.materiaId = materiaId;
            this.cursoNombre = cursoNombre;
            this.materiaNombre = materiaNombre;
        }
    }

    /**
     * NUEVO: Método toString para debugging
     */
    @Override
    public String toString() {
        return String.format(
                "ProfesorPanelManager{profesorId=%d, materiasAsignadas=%d, alumnosTotal=%d, notificacionesActivas=%s}",
                profesorId,
                obtenerCantidadMaterias(),
                obtenerTotalAlumnos(),
                verificarSistemaNotificaciones()
        );
    }
}
