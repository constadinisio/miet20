package login;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;

public class DatosComplementariosForm extends JDialog {
    private final UserSession userSession;
    private final JTextField txtDNI;
    private final JTextField txtTelefono;
    private final JTextField txtDireccion;
    private final JDateChooser fechaNacimiento;
    private final Connection conn;

    public DatosComplementariosForm(Frame parent, UserSession userSession) {
        super(parent, "Datos Complementarios", true);
        this.userSession = userSession;
        this.conn = new Conexion().getConexion();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtDNI = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtDireccion = new JTextField(20);
        fechaNacimiento = new JDateChooser();

        // Agregar componentes
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1;
        add(txtDNI, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1;
        add(txtDireccion, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Fecha de Nacimiento:"), gbc);
        gbc.gridx = 1;
        add(fechaNacimiento, gbc);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarDatos());

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(btnGuardar, gbc);

        pack();
        setLocationRelativeTo(parent);
    }

    private void guardarDatos() {
        try {
            String query = 
                "UPDATE usuarios SET dni = ?, telefono = ?, direccion = ?, fecha_nacimiento = ? " +
                "WHERE mail = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, txtDNI.getText());
            stmt.setString(2, txtTelefono.getText());
            stmt.setString(3, txtDireccion.getText());
            stmt.setDate(4, new java.sql.Date(fechaNacimiento.getDate().getTime()));
            stmt.setString(5, userSession.getEmail());
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Datos guardados exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar los datos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}