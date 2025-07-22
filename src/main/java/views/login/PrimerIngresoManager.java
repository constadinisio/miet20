package main.java.views.login;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import main.java.database.Conexion;

/**
 * Manager para manejar el primer ingreso de usuarios con Google.
 * Solicita DNI primero para evitar usuarios duplicados.
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 1.0
 */
public class PrimerIngresoManager {
    
    private final Frame parentFrame;
    private final UserSession googleSession;
    private final Connection conn;
    private Runnable onCompletionCallback;
    private java.util.function.Consumer<UserSession> onSessionCompleteCallback;
    
    public PrimerIngresoManager(Frame parentFrame, UserSession googleSession) {
        this.parentFrame = parentFrame;
        this.googleSession = googleSession;
        this.conn = Conexion.getInstancia().verificarConexion();
    }
    
    public void setOnCompletionCallback(Runnable callback) {
        this.onCompletionCallback = callback;
    }
    
    public void setOnSessionCompleteCallback(java.util.function.Consumer<UserSession> callback) {
        this.onSessionCompleteCallback = callback;
    }
    
    /**
     * Inicia el proceso de primer ingreso con verificación inteligente
     */
    public void iniciarProceso() {
        // Primero verificar si el email de Google ya está vinculado a un usuario
        try {
            UsuarioExistente usuarioVinculado = buscarUsuarioPorEmail(googleSession.getEmail());
            
            if (usuarioVinculado != null) {
                // Email ya está vinculado - proceder directamente sin pedir DNI
                System.out.println("🔗 Email ya vinculado a usuario existente - saltando verificación DNI");
                procesarUsuarioVinculado(usuarioVinculado);
            } else {
                // Email no vinculado - solicitar DNI para verificar duplicados
                System.out.println("📧 Email no vinculado - solicitando DNI para verificación");
                solicitarDNI();
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al verificar email vinculado: " + ex.getMessage());
            // En caso de error, seguir el flujo normal con DNI
            solicitarDNI();
        }
    }
    
    /**
     * Solicita el DNI al usuario y verifica si ya existe en la base de datos
     */
    private void solicitarDNI() {
        JDialog dialog = new JDialog(parentFrame, "Verificación de Identidad", true);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de información
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("🔍 Verificación de Identidad", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(51, 153, 255));
        
        JLabel lblMensaje = new JLabel("<html><center>" +
            "Para evitar usuarios duplicados, necesitamos verificar tu DNI.<br>" +
            "Si ya tienes una cuenta en el sistema, la vincularemos con tu login de Google." +
            "</center></html>", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 12));
        
        infoPanel.add(lblTitulo, BorderLayout.NORTH);
        infoPanel.add(lblMensaje, BorderLayout.CENTER);
        
        // Panel de entrada de DNI
        JPanel dniPanel = new JPanel(new FlowLayout());
        JLabel lblDNI = new JLabel("DNI:");
        lblDNI.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtDNI = new JTextField(15);
        txtDNI.setFont(new Font("Arial", Font.PLAIN, 14));
        
        dniPanel.add(lblDNI);
        dniPanel.add(txtDNI);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnContinuar = new JButton("✓ Continuar");
        btnContinuar.setBackground(new Color(40, 167, 69));
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton btnCancelar = new JButton("✗ Cancelar");
        btnCancelar.setBackground(new Color(220, 53, 69));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        
        buttonPanel.add(btnContinuar);
        buttonPanel.add(btnCancelar);
        
        // Eventos
        btnContinuar.addActionListener(e -> {
            String dni = txtDNI.getText().trim();
            if (validarDNI(dni)) {
                dialog.dispose();
                procesarUsuarioPorDNI(dni);
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Por favor ingrese un DNI válido (solo números, 7-8 dígitos).",
                    "DNI Inválido",
                    JOptionPane.WARNING_MESSAGE);
                txtDNI.requestFocus();
            }
        });
        
        btnCancelar.addActionListener(e -> {
            dialog.dispose();
            JOptionPane.showMessageDialog(parentFrame,
                "Proceso de login cancelado.",
                "Login Cancelado",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Enter para continuar
        txtDNI.addActionListener(e -> btnContinuar.doClick());
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(dniPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        txtDNI.requestFocus();
    }
    
    /**
     * Valida el formato del DNI
     */
    private boolean validarDNI(String dni) {
        return dni != null && dni.matches("\\d{7,8}");
    }
    
    /**
     * Procesa el usuario basado en el DNI ingresado
     */
    private void procesarUsuarioPorDNI(String dni) {
        try {
            // Buscar usuario existente por DNI
            UsuarioExistente usuarioExistente = buscarUsuarioPorDNI(dni);
            
            if (usuarioExistente != null) {
                // Usuario existe - actualizar datos con información de Google
                actualizarUsuarioExistente(usuarioExistente, dni);
            } else {
                // Usuario no existe - crear nuevo usuario
                crearNuevoUsuario(dni);
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al procesar usuario: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                "Error al verificar los datos: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Busca un usuario existente por DNI
     */
    private UsuarioExistente buscarUsuarioPorDNI(String dni) throws SQLException {
        String query = "SELECT id, nombre, apellido, mail, rol, contrasena FROM usuarios WHERE dni = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, dni);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return new UsuarioExistente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("mail"),
                rs.getString("rol"),
                rs.getString("contrasena")
            );
        }
        
        return null;
    }
    
    /**
     * Busca un usuario existente por email (para verificar si ya está vinculado)
     */
    private UsuarioExistente buscarUsuarioPorEmail(String email) throws SQLException {
        String query = "SELECT id, nombre, apellido, mail, rol, contrasena FROM usuarios WHERE mail = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return new UsuarioExistente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("mail"),
                rs.getString("rol"),
                rs.getString("contrasena")
            );
        }
        
        return null;
    }
    
    /**
     * Procesa un usuario que ya tiene su email vinculado (evita pedir DNI)
     */
    private void procesarUsuarioVinculado(UsuarioExistente usuario) {
        // Crear sesión con datos de BD (mantener nombre/apellido originales)
        UserSession sessionCompleta = new UserSession(
            usuario.id,
            usuario.nombre,  // Usar nombre de BD
            usuario.apellido, // Usar apellido de BD
            googleSession.getEmail(), // Email de Google (ya vinculado)
            obtenerRolNumerico(usuario.rol),
            googleSession.getFotoUrl()
        );
        
        System.out.println("🔗 Usuario ya vinculado - ID: " + usuario.id + 
                          ", Nombre: " + usuario.nombre + " " + usuario.apellido);
        
        // Verificar si el usuario tiene rol válido
        if (obtenerRolNumerico(usuario.rol) > 0) {
            // Usuario tiene rol - verificar datos complementarios y continuar
            verificarDatosComplementariosSinDNI(sessionCompleta);
        } else {
            // Usuario sin rol - mostrar mensaje y NO continuar login
            JOptionPane.showMessageDialog(parentFrame,
                "Su cuenta está pendiente de asignación de rol.\n" +
                "Por favor contacte al administrador para que le asigne un rol.",
                "Rol Pendiente",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Actualiza un usuario existente con datos de Google
     */
    private void actualizarUsuarioExistente(UsuarioExistente usuarioExistente, String dni) {
        try {
            // Mostrar diálogo de confirmación
            String mensaje = String.format(
                "Se encontró un usuario existente:\n\n" +
                "Nombre: %s %s\n" +
                "DNI: %s\n\n" +
                "¿Desea vincular esta cuenta con su login de Google?\n" +
                "Esto actualizará el email y otros datos faltantes.",
                usuarioExistente.nombre, usuarioExistente.apellido, dni
            );
            
            int respuesta = JOptionPane.showConfirmDialog(parentFrame,
                mensaje,
                "Usuario Encontrado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                // Actualizar datos del usuario existente, SIN modificar nombre/apellido
                // Solo actualizar email y DNI, manteniendo el nombre/apellido originales
                String updateQuery = "UPDATE usuarios SET mail = ?, dni = ?, modified_at = NOW() WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(updateQuery);
                ps.setString(1, googleSession.getEmail());
                ps.setString(2, dni); // Asegurar que el DNI esté guardado
                ps.setInt(3, usuarioExistente.id);
                ps.executeUpdate();
                
                // Crear sesión con ID del usuario existente
                // IMPORTANTE: Usar nombre/apellido de la BD, NO los de Google
                UserSession sessionCompleta = new UserSession(
                    usuarioExistente.id,
                    usuarioExistente.nombre,  // Mantener nombre original de BD
                    usuarioExistente.apellido, // Mantener apellido original de BD
                    googleSession.getEmail(),
                    obtenerRolNumerico(usuarioExistente.rol),
                    googleSession.getFotoUrl()
                );
                
                // Debug: verificar qué campos están faltando
                debugVerificarCamposFaltantes(usuarioExistente.id);
                
                // Verificar si el usuario tiene rol válido
                if (obtenerRolNumerico(usuarioExistente.rol) > 0) {
                    // Usuario tiene rol - verificar datos complementarios y continuar
                    verificarDatosComplementariosSinDNI(sessionCompleta);
                } else {
                    // Usuario sin rol - mostrar mensaje y NO continuar login
                    JOptionPane.showMessageDialog(parentFrame,
                        "Su cuenta ha sido vinculada exitosamente con Google.\n" +
                        "Sin embargo, su cuenta está pendiente de asignación de rol.\n" +
                        "Por favor contacte al administrador para que le asigne un rol.",
                        "Cuenta Vinculada - Rol Pendiente",
                        JOptionPane.INFORMATION_MESSAGE);
                    // No ejecutar callback porque no debe entrar al sistema sin rol
                }
                
                System.out.println("✅ Usuario existente actualizado con datos de Google (manteniendo nombre/apellido originales)");
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                    "Proceso cancelado. No se realizaron cambios.",
                    "Proceso Cancelado",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al actualizar usuario existente: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                "Error al actualizar el usuario: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Crea un nuevo usuario con DNI y datos de Google
     */
    private void crearNuevoUsuario(String dni) {
        try {
            // Solicitar datos adicionales
            DatosNuevoUsuario datosNuevos = solicitarDatosNuevoUsuario(dni);
            
            if (datosNuevos != null) {
                // Insertar nuevo usuario
                String insertQuery = "INSERT INTO usuarios (nombre, apellido, mail, dni, telefono, " +
                    "direccion, fecha_nacimiento, rol, contrasena, status, created_at, modified_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW(), NOW())";
                
                PreparedStatement ps = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, googleSession.getNombre());
                ps.setString(2, googleSession.getApellido());
                ps.setString(3, googleSession.getEmail());
                ps.setString(4, dni);
                ps.setString(5, datosNuevos.telefono);
                ps.setString(6, datosNuevos.direccion);
                ps.setDate(7, datosNuevos.fechaNacimiento);
                ps.setString(8, "0"); // Rol pendiente por defecto
                ps.setString(9, hashPassword(datosNuevos.contrasena));
                
                ps.executeUpdate();
                
                // Obtener ID del nuevo usuario
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int nuevoUserId = generatedKeys.getInt(1);
                    
                    // Crear sesión para el nuevo usuario
                    UserSession sessionCompleta = new UserSession(
                        nuevoUserId,
                        googleSession.getNombre(),
                        googleSession.getApellido(),
                        googleSession.getEmail(),
                        0, // Rol pendiente
                        googleSession.getFotoUrl()
                    );
                    
                    JOptionPane.showMessageDialog(parentFrame,
                        "Usuario creado exitosamente.\n" +
                        "Su cuenta está pendiente de asignación de rol.\n" +
                        "Contacte al administrador para activar su cuenta.",
                        "Usuario Creado",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    System.out.println("✅ Nuevo usuario creado con ID: " + nuevoUserId);
                }
            }
            
        } catch (SQLException ex) {
            System.err.println("Error al crear nuevo usuario: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                "Error al crear el usuario: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Solicita datos adicionales para un nuevo usuario
     */
    private DatosNuevoUsuario solicitarDatosNuevoUsuario(String dni) {
        JDialog dialog = new JDialog(parentFrame, "Completar Registro", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Complete su registro", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(51, 153, 255));
        mainPanel.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Teléfono
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        JTextField txtTelefono = new JTextField(15);
        mainPanel.add(txtTelefono, gbc);
        
        // Dirección
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1;
        JTextField txtDireccion = new JTextField(15);
        mainPanel.add(txtDireccion, gbc);
        
        // Fecha de nacimiento
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Fecha Nac. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField txtFechaNac = new JTextField(15);
        mainPanel.add(txtFechaNac, gbc);
        
        // Contraseña
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtContrasena = new JPasswordField(15);
        mainPanel.add(txtContrasena, gbc);
        
        // Botones
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        mainPanel.add(buttonPanel, gbc);
        
        final DatosNuevoUsuario[] resultado = {null};
        
        btnGuardar.addActionListener(e -> {
            try {
                // Validar datos
                if (txtTelefono.getText().trim().isEmpty() || 
                    txtContrasena.getPassword().length == 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "Teléfono y contraseña son obligatorios.",
                        "Datos Incompletos",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Crear objeto con datos
                resultado[0] = new DatosNuevoUsuario();
                resultado[0].telefono = txtTelefono.getText().trim();
                resultado[0].direccion = txtDireccion.getText().trim();
                resultado[0].contrasena = new String(txtContrasena.getPassword());
                
                // Parsear fecha
                String fechaStr = txtFechaNac.getText().trim();
                if (!fechaStr.isEmpty()) {
                    resultado[0].fechaNacimiento = java.sql.Date.valueOf(fechaStr);
                }
                
                dialog.dispose();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error en los datos: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        return resultado[0];
    }
    
    /**
     * Verifica datos complementarios excluyendo DNI (ya lo tenemos)
     */
    private void verificarDatosComplementariosSinDNI(UserSession session) {
        // El DNI ya debería estar guardado en la BD en este punto
        // El DatosComplementariosManager verificará automáticamente y saltará campos que ya existen
        System.out.println("🔍 Iniciando verificación de datos complementarios (DNI ya verificado)");
        
        DatosComplementariosManager datosManager = new DatosComplementariosManager(parentFrame, session);
        datosManager.setOnCompletionCallback(() -> {
            System.out.println("✅ Datos complementarios completados para usuario existente");
            // Ejecutar callback con la sesión completa
            if (onSessionCompleteCallback != null) {
                onSessionCompleteCallback.accept(session);
            } else if (onCompletionCallback != null) {
                onCompletionCallback.run();
            }
        });
        datosManager.startVerification();
    }
    
    /**
     * Convierte rol string a número
     */
    private int obtenerRolNumerico(String rolStr) {
        try {
            return Integer.parseInt(rolStr);
        } catch (NumberFormatException e) {
            return 0; // Rol pendiente por defecto
        }
    }
    
    /**
     * Hash de contraseña (mismo método que LoginForm)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al hashear contraseña: " + e.getMessage());
            return password;
        }
    }
    
    /**
     * Método de debugging para verificar qué campos están faltando
     */
    private void debugVerificarCamposFaltantes(int userId) {
        try {
            String query = "SELECT dni, telefono, direccion, fecha_nacimiento, contrasena FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                System.out.println("=== DEBUG: Estado de campos para usuario " + userId + " ===");
                System.out.println("DNI: " + (rs.getString("dni") != null ? "'" + rs.getString("dni") + "'" : "NULL"));
                System.out.println("Teléfono: " + (rs.getString("telefono") != null ? "'" + rs.getString("telefono") + "'" : "NULL"));
                System.out.println("Dirección: " + (rs.getString("direccion") != null ? "'" + rs.getString("direccion") + "'" : "NULL"));
                System.out.println("Fecha Nac: " + (rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toString() : "NULL"));
                System.out.println("Contraseña: " + (rs.getString("contrasena") != null ? "[PRESENTE]" : "NULL"));
                System.out.println("=== FIN DEBUG ===");
            }
        } catch (SQLException ex) {
            System.err.println("Error en debug: " + ex.getMessage());
        }
    }

    /**
     * Clase para almacenar datos de usuario existente
     */
    private static class UsuarioExistente {
        final int id;
        final String nombre;
        final String apellido;
        final String mail;
        final String rol;
        final String contrasena;
        
        UsuarioExistente(int id, String nombre, String apellido, String mail, String rol, String contrasena) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.mail = mail;
            this.rol = rol;
            this.contrasena = contrasena;
        }
    }
    
    /**
     * Clase para almacenar datos de nuevo usuario
     */
    private static class DatosNuevoUsuario {
        String telefono;
        String direccion;
        java.sql.Date fechaNacimiento;
        String contrasena;
    }
}
