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
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import main.java.database.Conexion;
import main.java.utils.PasswordUtils;

/**
 * Panel de configuración completo para usuarios del sistema.
 * Permite gestionar datos personales, familiares y configuraciones específicas por rol.
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class ConfiguracionUsuarioPanel extends JPanel {
    
    private final int userId;
    private final VentanaInicio ventanaInicio;
    private Connection conect;
    
    // Datos del usuario
    private String rolActual;
    private int rolId;
    
    // Componentes del panel de datos personales
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDni;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextField txtFechaNacimiento;
    private JTextField txtAnio;
    private JTextField txtDivision;
    private JPasswordField txtContrasenaActual;
    private JPasswordField txtContrasenaNueva;
    private JPasswordField txtConfirmarContrasena;
    
    // Componentes del panel de datos familiares
    private JTextField txtNombrePadre;
    private JTextField txtTelefonoPadre;
    private JTextField txtEmailPadre;
    private JTextField txtNombreMadre;
    private JTextField txtTelefonoMadre;
    private JTextField txtEmailMadre;
    private JTextField txtContactoEmergencia;
    private JTextField txtTelefonoEmergencia;
    
    public ConfiguracionUsuarioPanel(int userId, VentanaInicio ventanaInicio) {
        this.userId = userId;
        this.ventanaInicio = ventanaInicio;
        this.conect = Conexion.getInstancia().verificarConexion();
        
        inicializarComponentes();
        cargarDatosUsuario();
    }
    
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Título principal
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Panel central con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Pestaña 1: Datos Personales
        JPanel panelDatosPersonales = createDatosPersonalesPanel();
        tabbedPane.addTab("👤 Datos Personales", panelDatosPersonales);
        
        // Pestaña 2: Datos Familiares
        JPanel panelDatosFamiliares = createDatosFamiliaresPanel();
        tabbedPane.addTab("👨‍👩‍👧‍👦 Datos Familiares", panelDatosFamiliares);
        
        // Pestaña 3: Ficha Ed. Física (solo para alumnos, por ahora vacía)
        if (esAlumno()) {
            JPanel panelFichaEducacionFisica = createFichaEducacionFisicaPanel();
            tabbedPane.addTab("🏃‍♂️ Ficha Ed. Física", panelFichaEducacionFisica);
        }
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitulo = new JLabel("⚙️ Configuración de Usuario", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(51, 153, 255));
        
        JLabel lblSubtitulo = new JLabel("Gestione sus datos personales y configuraciones", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.ITALIC, 14));
        lblSubtitulo.setForeground(Color.GRAY);
        
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        headerPanel.add(lblSubtitulo, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private JPanel createDatosPersonalesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Sección: Información Personal
        addSectionTitle(panel, gbc, "📋 Información Personal", row++);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panel.add(txtNombre, gbc);
        
        // Apellido
        gbc.gridx = 2;
        panel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 3;
        txtApellido = new JTextField(20);
        panel.add(txtApellido, gbc);
        row++;
        
        // DNI
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1;
        txtDni = new JTextField(20);
        panel.add(txtDni, gbc);
        
        // Fecha de Nacimiento
        gbc.gridx = 2;
        panel.add(new JLabel("Fecha Nacimiento:"), gbc);
        gbc.gridx = 3;
        txtFechaNacimiento = new JTextField(20);
        txtFechaNacimiento.setToolTipText("Formato: YYYY-MM-DD");
        panel.add(txtFechaNacimiento, gbc);
        row++;
        
        // Sección: Contacto
        addSectionTitle(panel, gbc, "📞 Información de Contacto", row++);
        
        // Email
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);
        
        // Teléfono
        gbc.gridx = 2;
        panel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 3;
        txtTelefono = new JTextField(20);
        panel.add(txtTelefono, gbc);
        row++;
        
        // Dirección
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtDireccion = new JTextField(20);
        panel.add(txtDireccion, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Sección: Información Académica (solo para alumnos)
        if (esAlumno()) {
            addSectionTitle(panel, gbc, "🎓 Información Académica", row++);
            
            // Año
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(new JLabel("Año:"), gbc);
            gbc.gridx = 1;
            txtAnio = new JTextField(20);
            panel.add(txtAnio, gbc);
            
            // División
            gbc.gridx = 2;
            panel.add(new JLabel("División:"), gbc);
            gbc.gridx = 3;
            txtDivision = new JTextField(20);
            panel.add(txtDivision, gbc);
            row++;
        }
        
        // Sección: Cambio de Contraseña
        addSectionTitle(panel, gbc, "🔒 Cambio de Contraseña", row++);
        
        // Contraseña actual
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Contraseña Actual:"), gbc);
        gbc.gridx = 1;
        txtContrasenaActual = new JPasswordField(20);
        panel.add(txtContrasenaActual, gbc);
        row++;
        
        // Nueva contraseña
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nueva Contraseña:"), gbc);
        gbc.gridx = 1;
        txtContrasenaNueva = new JPasswordField(20);
        panel.add(txtContrasenaNueva, gbc);
        
        // Confirmar contraseña
        gbc.gridx = 2;
        panel.add(new JLabel("Confirmar Contraseña:"), gbc);
        gbc.gridx = 3;
        txtConfirmarContrasena = new JPasswordField(20);
        panel.add(txtConfirmarContrasena, gbc);
        
        return panel;
    }
    
    private JPanel createDatosFamiliaresPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Sección: Datos del Padre
        addSectionTitle(panel, gbc, "👨 Datos del Padre", row++);
        
        // Nombre del padre
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtNombrePadre = new JTextField(30);
        panel.add(txtNombrePadre, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Teléfono y email del padre
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        txtTelefonoPadre = new JTextField(20);
        panel.add(txtTelefonoPadre, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        txtEmailPadre = new JTextField(20);
        panel.add(txtEmailPadre, gbc);
        row++;
        
        // Sección: Datos de la Madre
        addSectionTitle(panel, gbc, "👩 Datos de la Madre", row++);
        
        // Nombre de la madre
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtNombreMadre = new JTextField(30);
        panel.add(txtNombreMadre, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Teléfono y email de la madre
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        txtTelefonoMadre = new JTextField(20);
        panel.add(txtTelefonoMadre, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        txtEmailMadre = new JTextField(20);
        panel.add(txtEmailMadre, gbc);
        row++;
        
        // Sección: Contacto de Emergencia
        addSectionTitle(panel, gbc, "🚨 Contacto de Emergencia", row++);
        
        // Contacto de emergencia
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1;
        txtContactoEmergencia = new JTextField(20);
        panel.add(txtContactoEmergencia, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 3;
        txtTelefonoEmergencia = new JTextField(20);
        panel.add(txtTelefonoEmergencia, gbc);
        
        // Nota informativa
        row += 2;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 4;
        JLabel lblNota = new JLabel("<html><i>Nota: Los datos familiares se almacenarán cuando se implemente la tabla correspondiente en la base de datos.</i></html>");
        lblNota.setForeground(Color.GRAY);
        lblNota.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(lblNota, gbc);
        
        return panel;
    }
    
    private JPanel createFichaEducacionFisicaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel informativo temporal
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JLabel lblInfo = new JLabel("<html><div style='text-align: center'>" +
            "<h2>🏃‍♂️ Ficha de Educación Física</h2>" +
            "<p>Esta sección será implementada próximamente.</p>" +
            "<p>Aquí se podrán gestionar:</p>" +
            "<ul>" +
            "<li>• Datos médicos relevantes</li>" +
            "<li>• Restricciones físicas</li>" +
            "<li>• Autorizaciones para actividades</li>" +
            "<li>• Historial de aptitud física</li>" +
            "</ul>" +
            "</div></html>");
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        infoPanel.add(lblInfo);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton btnGuardar = new JButton("💾 Guardar Cambios");
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(160, 35));
        btnGuardar.addActionListener(e -> guardarCambios());
        
        JButton btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(220, 53, 69));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setPreferredSize(new Dimension(160, 35));
        btnCancelar.addActionListener(e -> ventanaInicio.restaurarVistaPrincipal());
        
        JButton btnRecargar = new JButton("🔄 Recargar");
        btnRecargar.setBackground(new Color(108, 117, 125));
        btnRecargar.setForeground(Color.WHITE);
        btnRecargar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRecargar.setPreferredSize(new Dimension(160, 35));
        btnRecargar.addActionListener(e -> cargarDatosUsuario());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnRecargar);
        buttonPanel.add(btnCancelar);
        
        return buttonPanel;
    }
    
    private void addSectionTitle(JPanel panel, GridBagConstraints gbc, String title, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(20, 8, 10, 8);
        
        JLabel lblSeccion = new JLabel(title);
        lblSeccion.setFont(new Font("Arial", Font.BOLD, 16));
        lblSeccion.setForeground(new Color(51, 153, 255));
        lblSeccion.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        panel.add(lblSeccion, gbc);
        
        // Restaurar configuración original
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 8, 8, 8);
    }
    
    private void cargarDatosUsuario() {
        try {
            String query = "SELECT * FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Cargar datos personales
                txtNombre.setText(rs.getString("nombre") != null ? rs.getString("nombre") : "");
                txtApellido.setText(rs.getString("apellido") != null ? rs.getString("apellido") : "");
                txtDni.setText(rs.getString("dni") != null ? rs.getString("dni") : "");
                txtEmail.setText(rs.getString("mail") != null ? rs.getString("mail") : "");
                txtTelefono.setText(rs.getString("telefono") != null ? rs.getString("telefono") : "");
                txtDireccion.setText(rs.getString("direccion") != null ? rs.getString("direccion") : "");
                
                // Fecha de nacimiento
                Date fechaNac = rs.getDate("fecha_nacimiento");
                if (fechaNac != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    txtFechaNacimiento.setText(sdf.format(fechaNac));
                } else {
                    txtFechaNacimiento.setText("");
                }
                
                // Datos académicos (si existen)
                if (txtAnio != null) txtAnio.setText(rs.getString("anio") != null ? rs.getString("anio") : "");
                if (txtDivision != null) txtDivision.setText(rs.getString("division") != null ? rs.getString("division") : "");
                
                // Obtener rol actual
                rolActual = rs.getString("rol");
                
                System.out.println("✅ Datos del usuario cargados exitosamente");
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron datos para el usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al cargar datos del usuario: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cargar los datos: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarCambios() {
        try {
            boolean cambiosRealizados = false;
            
            // Validar datos obligatorios
            if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "El nombre y apellido son obligatorios.",
                    "Datos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Actualizar datos personales
            String updateQuery = "UPDATE usuarios SET nombre = ?, apellido = ?, mail = ?, " +
                "telefono = ?, direccion = ?, fecha_nacimiento = ?, anio = ?, division = ?, " +
                "modified_at = NOW() WHERE id = ?";
            
            PreparedStatement ps = conect.prepareStatement(updateQuery);
            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            ps.setString(4, txtTelefono.getText().trim().isEmpty() ? null : txtTelefono.getText().trim());
            ps.setString(5, txtDireccion.getText().trim().isEmpty() ? null : txtDireccion.getText().trim());
            
            // Fecha de nacimiento
            String fechaNacStr = txtFechaNacimiento.getText().trim();
            if (!fechaNacStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date fechaNac = sdf.parse(fechaNacStr);
                    ps.setDate(6, new java.sql.Date(fechaNac.getTime()));
                } catch (Exception e) {
                    ps.setDate(6, null);
                }
            } else {
                ps.setDate(6, null);
            }
            
            ps.setString(7, txtAnio != null && !txtAnio.getText().trim().isEmpty() ? txtAnio.getText().trim() : null);
            ps.setString(8, txtDivision != null && !txtDivision.getText().trim().isEmpty() ? txtDivision.getText().trim() : null);
            ps.setInt(9, userId);
            
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                cambiosRealizados = true;
            }
            
            // Cambiar contraseña si se proporcionó
            if (!new String(txtContrasenaNueva.getPassword()).isEmpty()) {
                if (cambiarContrasena()) {
                    cambiosRealizados = true;
                } else {
                    return; // Error en cambio de contraseña
                }
            }
            
            if (cambiosRealizados) {
                JOptionPane.showMessageDialog(this,
                    "Los cambios se guardaron exitosamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Limpiar campos de contraseña
                txtContrasenaActual.setText("");
                txtContrasenaNueva.setText("");
                txtConfirmarContrasena.setText("");
                
                System.out.println("✅ Datos del usuario actualizados exitosamente");
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se realizaron cambios.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al guardar cambios: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al guardar los cambios: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean cambiarContrasena() {
        try {
            String contrasenaActual = new String(txtContrasenaActual.getPassword());
            String contrasenaNueva = new String(txtContrasenaNueva.getPassword());
            String confirmarContrasena = new String(txtConfirmarContrasena.getPassword());
            
            // Validaciones
            if (contrasenaActual.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Debe ingresar su contraseña actual para cambiarla.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (!contrasenaNueva.equals(confirmarContrasena)) {
                JOptionPane.showMessageDialog(this,
                    "Las contraseñas nuevas no coinciden.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (contrasenaNueva.length() < 6) {
                JOptionPane.showMessageDialog(this,
                    "La nueva contraseña debe tener al menos 6 caracteres.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Verificar contraseña actual
            String checkQuery = "SELECT contrasena FROM usuarios WHERE id = ?";
            PreparedStatement checkPs = conect.prepareStatement(checkQuery);
            checkPs.setInt(1, userId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                String hashActual = rs.getString("contrasena");
                
                // Hashear la contraseña actual ingresada para compararla
                String contrasenaActualHasheada = hashPassword(contrasenaActual);
                
                if (!contrasenaActualHasheada.equals(hashActual)) {
                    JOptionPane.showMessageDialog(this,
                        "La contraseña actual es incorrecta.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                
                // Hashear la nueva contraseña antes de guardarla
                String contrasenaNuevaHasheada = hashPassword(contrasenaNueva);
                
                // Actualizar contraseña
                String updateQuery = "UPDATE usuarios SET contrasena = ? WHERE id = ?";
                PreparedStatement updatePs = conect.prepareStatement(updateQuery);
                updatePs.setString(1, contrasenaNuevaHasheada);
                updatePs.setInt(2, userId);
                
                int rowsUpdated = updatePs.executeUpdate();
                return rowsUpdated > 0;
            }
            
            return false;
            
        } catch (SQLException ex) {
            System.err.println("Error al cambiar contraseña: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cambiar la contraseña: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Método para hashear contraseñas usando bcrypt
     */
    private String hashPassword(String password) {
        return PasswordUtils.hashPassword(password);
    }
    
    private boolean esAlumno() {
        try {
            // Obtener el rol del usuario para determinar si es alumno
            String query = "SELECT rol FROM usuarios WHERE id = ?";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String rol = rs.getString("rol");
                return "4".equals(rol) || "Alumno".equalsIgnoreCase(rol);
            }
        } catch (SQLException ex) {
            System.err.println("Error al verificar rol de usuario: " + ex.getMessage());
        }
        return false;
    }
}
