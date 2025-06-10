package main.java.views.login;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.database.Conexion;

/**
 * Clase que gestiona la solicitud secuencial de datos complementarios que
 * faltan en el perfil del usuario.
 */
public class DatosComplementariosManager {

    private final Frame parentFrame;
    private final UserSession userSession;
    private final Connection conn;
    private Runnable onCompletionCallback;
    private int currentFormIndex = 0;

    // Lista de verificadores de datos
    private final List<DataVerifier> dataVerifiers = new ArrayList<>();

    /**
     * Constructor de la clase DatosComplementariosManager.
     *
     * @param parentFrame Frame padre para mostrar diálogos modales
     * @param userSession Sesión del usuario actual
     */
    public DatosComplementariosManager(Frame parentFrame, UserSession userSession) {
        this.parentFrame = parentFrame;
        this.userSession = userSession;
        this.conn = Conexion.getInstancia().verificarConexion();

        // Configurar los verificadores de datos
        setupDataVerifiers();
    }

    /**
     * Configura la acción a ejecutar cuando se completen todos los datos.
     *
     * @param callback Acción a ejecutar
     */
    public void setOnCompletionCallback(Runnable callback) {
        this.onCompletionCallback = callback;
    }

    /**
     * Configura los verificadores para cada campo requerido.
     */
    private void setupDataVerifiers() {
        // Datos generales para todos los usuarios

        // Primero verificar la contraseña
        dataVerifiers.add(new DataVerifier("contrasena",
                (userId) -> checkEmptyOrDefaultPassword(userId),
                () -> showPasswordForm()));

        dataVerifiers.add(new DataVerifier("dni",
                (userId) -> checkEmptyField("dni", userId),
                () -> showDNIForm()));

        dataVerifiers.add(new DataVerifier("telefono",
                (userId) -> checkEmptyField("telefono", userId),
                () -> showPhoneForm()));

        dataVerifiers.add(new DataVerifier("direccion",
                (userId) -> checkEmptyField("direccion", userId),
                () -> showAddressForm()));

        dataVerifiers.add(new DataVerifier("fecha_nacimiento",
                (userId) -> checkEmptyField("fecha_nacimiento", userId),
                () -> showBirthDateForm()));

        // Datos específicos según el rol
        if (userSession.getRol() != 4 && userSession.getRol() != 0) { // Si no es alumno (rol 4)
            dataVerifiers.add(new DataVerifier("ficha_censal",
                    (userId) -> checkEmptyField("ficha_censal", userId),
                    () -> showFichaCensalForm()));
        }
    }

    /**
     * Verifica si la contraseña está vacía, es null, o tiene un valor
     * predeterminado.
     *
     * @param userId ID del usuario
     * @return true si la contraseña necesita ser configurada, false si ya está
     * configurada
     */
    private boolean checkEmptyOrDefaultPassword(int userId) {
        try {
            String query = "SELECT contrasena FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String password = rs.getString("contrasena");
                return password == null || password.isEmpty() || password.equals("test")
                        || password.equals("default_password");
            }
            return true; // Si no hay registro, necesita datos
        } catch (SQLException e) {
            System.err.println("Error verificando contraseña: " + e.getMessage());
            return true; // En caso de error, asumir que necesita datos
        }
    }

    /**
     * Muestra el formulario para establecer contraseña.
     */
    private void showPasswordForm() {
        PasswordForm form = new PasswordForm(
                parentFrame,
                true,
                userSession.getUserId(),
                () -> {
                    currentFormIndex++;
                    checkNextField();
                }
        );
        form.setVisible(true);
    }

    /**
     * Inicia el proceso de verificación de datos faltantes.
     */
    public void startVerification() {
        currentFormIndex = 0;
        checkNextField();
    }

    /**
     * Verifica el siguiente campo en la lista.
     */
    private void checkNextField() {
        if (currentFormIndex >= dataVerifiers.size()) {
            // Todos los datos están completos
            if (onCompletionCallback != null) {
                onCompletionCallback.run();
            }
            return;
        }

        DataVerifier verifier = dataVerifiers.get(currentFormIndex);
        if (verifier.needsData(userSession.getUserId())) {
            // El campo está vacío, mostrar formulario
            verifier.showForm();
        } else {
            // El campo ya tiene datos, pasar al siguiente
            currentFormIndex++;
            checkNextField();
        }
    }

    /**
     * Verifica si un campo específico está vacío en la base de datos.
     *
     * @param fieldName Nombre del campo a verificar
     * @param userId ID del usuario
     * @return true si el campo está vacío, false si contiene un valor
     */
    private boolean checkEmptyField(String fieldName, int userId) {
        try {
            String query = "SELECT " + fieldName + " FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Object value = rs.getObject(1);
                return value == null;
            }
            return true; // Si no hay registro, necesita datos
        } catch (SQLException e) {
            System.err.println("Error verificando campo " + fieldName + ": " + e.getMessage());
            return true; // En caso de error, asumir que necesita datos
        }
    }

    /**
     * Muestra el formulario para ingresar DNI.
     */
    private void showDNIForm() {
        SingleFieldForm form = new SingleFieldForm(
                parentFrame,
                "DNI",
                "Ingrese su número de DNI",
                "dni",
                () -> {
                    currentFormIndex++;
                    checkNextField();
                }
        );
        form.setVisible(true);
    }

    /**
     * Muestra el formulario para ingresar teléfono.
     */
    private void showPhoneForm() {
        SingleFieldForm form = new SingleFieldForm(
                parentFrame,
                "Teléfono",
                "Ingrese su número de teléfono",
                "telefono",
                () -> {
                    currentFormIndex++;
                    checkNextField();
                }
        );
        form.setVisible(true);
    }

    /**
     * Muestra el formulario para ingresar dirección.
     */
    private void showAddressForm() {
        SingleFieldForm form = new SingleFieldForm(
                parentFrame,
                "Dirección",
                "Ingrese su dirección completa",
                "direccion",
                () -> {
                    currentFormIndex++;
                    checkNextField();
                }
        );
        form.setVisible(true);
    }

    /**
     * Muestra el formulario para ingresar fecha de nacimiento.
     */
    private void showBirthDateForm() {
        DateFieldForm form = new DateFieldForm(
                parentFrame,
                "Fecha de Nacimiento",
                "Ingrese su fecha de nacimiento",
                "fecha_nacimiento",
                () -> {
                    currentFormIndex++;
                    checkNextField();
                }
        );
        form.setVisible(true);
    }

    /**
     * Muestra el formulario para ingresar ficha censal.
     */
    private void showFichaCensalForm() {
        FichaCensalForm form = new FichaCensalForm(
                parentFrame,
                () -> {
                    currentFormIndex++;
                    checkNextField();
                }
        );
        form.setVisible(true);
    }

    /**
     * Clase interna para verificar un campo específico.
     */
    private class DataVerifier {

        private final String fieldName;
        private final FieldChecker checker;
        private final Runnable formAction;

        public DataVerifier(String fieldName, FieldChecker checker, Runnable formAction) {
            this.fieldName = fieldName;
            this.checker = checker;
            this.formAction = formAction;
        }

        public boolean needsData(int userId) {
            return checker.checkField(userId);
        }

        public void showForm() {
            formAction.run();
        }
    }

    /**
     * Interfaz funcional para verificar si un campo necesita datos.
     */
    @FunctionalInterface
    private interface FieldChecker {

        boolean checkField(int userId);
    }

    /**
     * Clase para formularios de un solo campo de texto.
     */
    private class SingleFieldForm extends JDialog {

        private final JTextField textField;
        private final String fieldName;
        private final Runnable callback;

        public SingleFieldForm(Frame parent, String title, String prompt, String fieldName, Runnable callback) {
            super(parent, title, true);
            this.fieldName = fieldName;
            this.callback = callback;

            setLayout(new BorderLayout(10, 10));

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel(prompt), gbc);

            textField = new JTextField(20);
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(textField, gbc);

            JButton btnContinuar = new JButton("Continuar");
            btnContinuar.addActionListener(e -> saveData());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(btnContinuar);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }

        private void saveData() {
            String value = textField.getText().trim();

            if (value.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Este campo es requerido. Por favor complete la información.",
                        "Campo requerido",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String query = "UPDATE usuarios SET " + fieldName + " = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, value);
                ps.setInt(2, userSession.getUserId());
                ps.executeUpdate();

                dispose();
                if (callback != null) {
                    callback.run();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar los datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clase para formularios con selector de fecha.
     */
    private class DateFieldForm extends JDialog {

        private final com.toedter.calendar.JDateChooser dateChooser;
        private final String fieldName;
        private final Runnable callback;

        public DateFieldForm(Frame parent, String title, String prompt, String fieldName, Runnable callback) {
            super(parent, title, true);
            this.fieldName = fieldName;
            this.callback = callback;

            setLayout(new BorderLayout(10, 10));

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel(prompt), gbc);

            dateChooser = new com.toedter.calendar.JDateChooser();
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(dateChooser, gbc);

            JButton btnContinuar = new JButton("Continuar");
            btnContinuar.addActionListener(e -> saveData());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(btnContinuar);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }

        private void saveData() {
            if (dateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this,
                        "Por favor seleccione una fecha válida.",
                        "Fecha requerida",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String query = "UPDATE usuarios SET " + fieldName + " = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setDate(1, new java.sql.Date(dateChooser.getDate().getTime()));
                ps.setInt(2, userSession.getUserId());
                ps.executeUpdate();

                dispose();
                if (callback != null) {
                    callback.run();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar los datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clase para el formulario de Ficha Censal.
     */
    private class FichaCensalForm extends JDialog {

        private final JTextField txtFichaCensal;
        private final Runnable callback;

        public FichaCensalForm(Frame parent, Runnable callback) {
            super(parent, "Ficha Censal", true);
            this.callback = callback;

            setLayout(new BorderLayout(10, 10));

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Número de Ficha Censal:"), gbc);

            txtFichaCensal = new JTextField(20);
            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(txtFichaCensal, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;

            JButton btnContinuar = new JButton("Continuar");
            btnContinuar.addActionListener(e -> saveData());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(btnContinuar);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }

        private void saveData() {
            String fichaCensal = txtFichaCensal.getText().trim();

            if (fichaCensal.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor ingrese el número de ficha censal.",
                        "Campo requerido",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String query = "UPDATE usuarios SET ficha_censal = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, fichaCensal);
                ps.setInt(2, userSession.getUserId());
                ps.executeUpdate();

                dispose();
                if (callback != null) {
                    callback.run();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar los datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
