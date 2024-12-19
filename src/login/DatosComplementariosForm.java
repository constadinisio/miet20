package login;

import javax.swing.*;
import java.sql.*;
import java.awt.*;
import com.toedter.calendar.JDateChooser;

public class DatosComplementariosForm extends JDialog {
    private UserSession userSession;
    private JTextField txtApellido;
    private JTextField txtDNI;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JDateChooser dateChooser;
    private Connection conn;

    public DatosComplementariosForm(Frame parent, UserSession userSession) {
        super(parent, true);
        this.userSession = userSession;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setTitle("Datos Complementarios");
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos comunes
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        txtApellido = new JTextField(20);
        add(txtApellido, gbc);

        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1;
        txtDNI = new JTextField(20);
        add(txtDNI, gbc);

        // ... más campos ...

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarDatos());
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        add(btnGuardar, gbc);

        pack();
    }

    private void guardarDatos() {
        try {
            conn = Conexion.getInstancia().getConexion();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET apellido=?, dni=?, telefono=?, direccion=?, fecha_nacimiento=? WHERE email=?"
            );
            
            stmt.setString(1, txtApellido.getText());
            stmt.setString(2, txtDNI.getText());
            stmt.setString(3, txtTelefono.getText());
            stmt.setString(4, txtDireccion.getText());
            stmt.setDate(5, new java.sql.Date(dateChooser.getDate().getTime()));
            stmt.setString(6, userSession.getEmail());
            
            stmt.executeUpdate();
            
            // Si es alumno o profesor, guardar datos específicos
            if ("4".equals(userSession.getRole())) {
                guardarDatosAlumno();
            } else if ("2".equals(userSession.getRole())) {
                guardarDatosProfesor();
            }
            
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar los datos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}