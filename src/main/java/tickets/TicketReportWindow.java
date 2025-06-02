package main.java.tickets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import main.java.tickets.TicketService;

/**
 * Ventana para que los usuarios reporten errores o sugerencias
 * Disponible para todos los roles del sistema
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0 - Centro de Ayuda
 */
public class TicketReportWindow extends JFrame {
    
    private final int usuarioId;
    private final TicketService ticketService;
    
    // Componentes del formulario
    private JTextField asuntoField;
    private JTextArea descripcionArea;
    private JComboBox<String> categoriaCombo;
    private JComboBox<String> prioridadCombo;
    private JLabel archivoLabel;
    private JButton seleccionarArchivoButton;
    private JButton enviarButton;
    private JButton cancelarButton;
    private JButton limpiarButton;
    
    // Estado del archivo
    private File archivoSeleccionado;
    private String archivoUrl;
    
    // Constantes de diseño
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Font TITULO_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);

    public TicketReportWindow(int usuarioId) {
        this.usuarioId = usuarioId;
        this.ticketService = TicketService.getInstance();
        
        initializeComponents();
        setupLayout();
        setupListeners();
        
        setTitle("🎫 Reportar Error / Sugerencia - Centro de Ayuda");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        System.out.println("✅ TicketReportWindow inicializada para usuario: " + usuarioId);
    }
    
    private void initializeComponents() {
        // Campo de asunto
        asuntoField = new JTextField(40);
        asuntoField.setFont(LABEL_FONT);
        asuntoField.setToolTipText("Ingresa un asunto descriptivo del problema o sugerencia");
        
        // Área de descripción
        descripcionArea = new JTextArea(8, 40);
        descripcionArea.setFont(LABEL_FONT);
        descripcionArea.setLineWrap(true);
        descripcionArea.setWrapStyleWord(true);
        descripcionArea.setToolTipText("Describe detalladamente el problema o sugerencia");
        
        // Combo de categoría
        String[] categorias = {"ERROR", "MEJORA", "CONSULTA", "SUGERENCIA"};
        categoriaCombo = new JComboBox<>(categorias);
        categoriaCombo.setFont(LABEL_FONT);
        categoriaCombo.setToolTipText("Selecciona el tipo de reporte");
        
        // Combo de prioridad
        String[] prioridades = {"NORMAL", "ALTA", "URGENTE", "BAJA"};
        prioridadCombo = new JComboBox<>(prioridades);
        prioridadCombo.setFont(LABEL_FONT);
        prioridadCombo.setToolTipText("Selecciona la prioridad del reporte");
        
        // Componentes de archivo
        archivoLabel = new JLabel("Ningún archivo seleccionado");
        archivoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        archivoLabel.setForeground(Color.GRAY);
        
        seleccionarArchivoButton = createStyledButton("📎 Adjuntar Archivo", new Color(108, 117, 125));
        seleccionarArchivoButton.setToolTipText("Selecciona una imagen o archivo de log (opcional)");
        
        // Botones principales
        enviarButton = createStyledButton("📤 Enviar Reporte", SUCCESS_COLOR);
        cancelarButton = createStyledButton("❌ Cancelar", DANGER_COLOR);
        limpiarButton = createStyledButton("🔄 Limpiar", WARNING_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // === HEADER ===
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === FORMULARIO ===
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // === FOOTER ===
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Título principal
        JLabel titleLabel = new JLabel("🎫 Centro de Ayuda - Reportar Problema");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        // Subtítulo
        JLabel subtitleLabel = new JLabel("Ayúdanos a mejorar el sistema reportando errores o enviando sugerencias");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        
        // Panel de títulos
        JPanel titlesPanel = new JPanel(new BorderLayout());
        titlesPanel.setOpaque(false);
        titlesPanel.add(titleLabel, BorderLayout.NORTH);
        titlesPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Información adicional
        JLabel infoLabel = new JLabel("Tu reporte será revisado por el equipo de desarrollo");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titlesPanel, BorderLayout.WEST);
        headerPanel.add(infoLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "📝 Información del Reporte",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            TITULO_FONT
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Fila 1: Asunto
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Asunto:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(asuntoField, gbc);
        
        // Fila 2: Categoría y Prioridad
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        
        JPanel catPrioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        catPrioPanel.add(categoriaCombo);
        catPrioPanel.add(Box.createHorizontalStrut(20));
        catPrioPanel.add(new JLabel("Prioridad:"));
        catPrioPanel.add(Box.createHorizontalStrut(5));
        catPrioPanel.add(prioridadCombo);
        formPanel.add(catPrioPanel, gbc);
        
        // Fila 3: Descripción
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        
        JScrollPane descripcionScrollPane = new JScrollPane(descripcionArea);
        descripcionScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        formPanel.add(descripcionScrollPane, gbc);
        
        // Fila 4: Archivo adjunto
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Archivo (opcional):"), gbc);
        gbc.gridx = 1;
        
        JPanel archivoPanel = new JPanel(new BorderLayout());
        JPanel archivoButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        archivoButtonPanel.add(seleccionarArchivoButton);
        archivoPanel.add(archivoButtonPanel, BorderLayout.NORTH);
        archivoPanel.add(archivoLabel, BorderLayout.CENTER);
        formPanel.add(archivoPanel, gbc);
        
        return formPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Panel izquierdo con información
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ayudaLabel = new JLabel("💡 Consejo: Sé específico en la descripción para una mejor atención");
        ayudaLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        ayudaLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(ayudaLabel);
        
        // Panel derecho con botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesPanel.add(limpiarButton);
        botonesPanel.add(cancelarButton);
        botonesPanel.add(enviarButton);
        
        footerPanel.add(infoPanel, BorderLayout.WEST);
        footerPanel.add(botonesPanel, BorderLayout.EAST);
        
        return footerPanel;
    }
    
    private void setupListeners() {
        // Botón seleccionar archivo
        seleccionarArchivoButton.addActionListener(this::seleccionarArchivo);
        
        // Botones principales
        enviarButton.addActionListener(this::enviarTicket);
        cancelarButton.addActionListener(e -> dispose());
        limpiarButton.addActionListener(this::limpiarFormulario);
        
        // Validación en tiempo real
        asuntoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validarFormulario(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validarFormulario(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validarFormulario(); }
        });
        
        descripcionArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validarFormulario(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validarFormulario(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validarFormulario(); }
        });
        
        validarFormulario();
    }
    
    private void seleccionarArchivo(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo adjunto");
        
        // Filtros de archivo
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Imágenes (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos de log (*.log, *.txt)", "log", "txt"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Documentos (*.pdf, *.doc, *.docx)", "pdf", "doc", "docx"));
        
        fileChooser.setAcceptAllFileFilterUsed(true);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validar tamaño (máximo 10MB)
            if (selectedFile.length() > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                    "El archivo es demasiado grande. Máximo permitido: 10MB",
                    "Archivo muy grande", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            archivoSeleccionado = selectedFile;
            archivoLabel.setText("📎 " + selectedFile.getName() + 
                " (" + formatFileSize(selectedFile.length()) + ")");
            archivoLabel.setForeground(Color.BLACK);
            
            seleccionarArchivoButton.setText("📎 Cambiar Archivo");
        }
    }
    
    private void enviarTicket(ActionEvent e) {
        if (!validarDatos()) {
            return;
        }
        
        // Confirmar envío
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Enviar el reporte?\n\n" +
            "Asunto: " + asuntoField.getText() + "\n" +
            "Categoría: " + categoriaCombo.getSelectedItem() + "\n" +
            "Prioridad: " + prioridadCombo.getSelectedItem(),
            "Confirmar Envío",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Deshabilitar botones durante el envío
        setButtonsEnabled(false);
        enviarButton.setText("Enviando...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Procesar archivo si existe
        procesarArchivo();
        
        // Enviar ticket
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ticketService.crearTicket(
                    asuntoField.getText().trim(),
                    descripcionArea.getText().trim(),
                    (String) categoriaCombo.getSelectedItem(),
                    (String) prioridadCombo.getSelectedItem(),
                    usuarioId,
                    archivoUrl
                ).get();
            }
            
            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    setButtonsEnabled(true);
                    enviarButton.setText("📤 Enviar Reporte");
                    
                    try {
                        String ticketNumber = get();
                        if (ticketNumber != null) {
                            mostrarResultadoExitoso(ticketNumber);
                        } else {
                            mostrarResultadoError();
                        }
                    } catch (Exception ex) {
                        System.err.println("Error en envío: " + ex.getMessage());
                        mostrarResultadoError();
                    }
                });
            }
        };
        worker.execute();
    }
    
    private void procesarArchivo() {
        if (archivoSeleccionado != null) {
            try {
                // Crear directorio de tickets si no existe
                File ticketsDir = new File("tickets_attachments");
                if (!ticketsDir.exists()) {
                    ticketsDir.mkdirs();
                }
                
                // Generar nombre único para el archivo
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String extension = getFileExtension(archivoSeleccionado.getName());
                String newFileName = "ticket_" + usuarioId + "_" + timestamp + "." + extension;
                
                File destFile = new File(ticketsDir, newFileName);
                Files.copy(archivoSeleccionado.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                archivoUrl = destFile.getAbsolutePath();
                System.out.println("Archivo copiado a: " + archivoUrl);
                
            } catch (Exception e) {
                System.err.println("Error procesando archivo: " + e.getMessage());
                archivoUrl = null;
            }
        }
    }
    
    private void mostrarResultadoExitoso(String ticketNumber) {
        String mensaje = "✅ Tu reporte ha sido enviado exitosamente\n\n" +
                        "Número de ticket: " + ticketNumber + "\n\n" +
                        "Recibirás una respuesta a través del sistema de notificaciones.\n" +
                        "Gracias por ayudarnos a mejorar el sistema.";
        
        JOptionPane.showMessageDialog(this, mensaje, "Reporte Enviado", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar formulario y cerrar
        limpiarFormulario(null);
        dispose();
    }
    
    private void mostrarResultadoError() {
        JOptionPane.showMessageDialog(this,
            "❌ Error al enviar el reporte\n\n" +
            "Por favor, verifica tu conexión e intenta nuevamente.\n" +
            "Si el problema persiste, contacta al administrador.",
            "Error de Envío",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void limpiarFormulario(ActionEvent e) {
        asuntoField.setText("");
        descripcionArea.setText("");
        categoriaCombo.setSelectedIndex(0);
        prioridadCombo.setSelectedIndex(0);
        
        archivoSeleccionado = null;
        archivoUrl = null;
        archivoLabel.setText("Ningún archivo seleccionado");
        archivoLabel.setForeground(Color.GRAY);
        seleccionarArchivoButton.setText("📎 Adjuntar Archivo");
        
        validarFormulario();
    }
    
    private void validarFormulario() {
        boolean valido = !asuntoField.getText().trim().isEmpty() && 
                        !descripcionArea.getText().trim().isEmpty();
        
        enviarButton.setEnabled(valido);
    }
    
    private boolean validarDatos() {
        if (asuntoField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El asunto es obligatorio", "Validación", JOptionPane.WARNING_MESSAGE);
            asuntoField.requestFocus();
            return false;
        }
        
        if (descripcionArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La descripción es obligatoria", "Validación", JOptionPane.WARNING_MESSAGE);
            descripcionArea.requestFocus();
            return false;
        }
        
        if (asuntoField.getText().trim().length() < 10) {
            JOptionPane.showMessageDialog(this, "El asunto debe tener al menos 10 caracteres", "Validación", JOptionPane.WARNING_MESSAGE);
            asuntoField.requestFocus();
            return false;
        }
        
        if (descripcionArea.getText().trim().length() < 20) {
            JOptionPane.showMessageDialog(this, "La descripción debe tener al menos 20 caracteres", "Validación", JOptionPane.WARNING_MESSAGE);
            descripcionArea.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void setButtonsEnabled(boolean enabled) {
        enviarButton.setEnabled(enabled);
        cancelarButton.setEnabled(enabled);
        limpiarButton.setEnabled(enabled);
        seleccionarArchivoButton.setEnabled(enabled);
    }
    
    // === MÉTODOS AUXILIARES ===
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(originalColor.brighter());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "unknown";
    }
}