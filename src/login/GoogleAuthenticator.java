package login;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import conexion.conexionMysql;
import com.google.api.client.http.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GoogleAuthenticator {

    private static final String APPLICATION_NAME = "School Management System";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "client_secret.json";
    private static final String USER_ROLE_QUERY = "SELECT role FROM users WHERE email = ?";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String REDIRECT_URI = "http://localhost:8888/Callback";
    private final HttpTransport httpTransport;
    private final conexionMysql dbConnection;

    private static final String CHECK_USER_DATA_COMPLETE
            = "SELECT apellido, dni FROM users WHERE email = ? AND apellido IS NOT NULL AND dni IS NOT NULL";

    private static final String INSERT_NEW_USER
            = "INSERT INTO users (name, email, role, created_at) VALUES (?, ?, 'pending', CURRENT_TIMESTAMP)";

    public GoogleAuthenticator() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.dbConnection = new conexionMysql();
    }

    public UserSession authenticateUser() throws IOException {
        try {
            // Verificar la conexión a la base de datos primero
            Connection testConn = dbConnection.conectar();
            if (testConn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }
            testConn.close();

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets,
                    Collections.singleton("https://www.googleapis.com/auth/userinfo.email"))
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            // Obtener información del usuario
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();

            JsonParser parser = JSON_FACTORY.createJsonParser(response.getContent());
            JsonToken currentToken = parser.getCurrentToken();

            String userEmail = null;
            String userName = "Usuario de Google";

            while (currentToken != JsonToken.END_OBJECT) {
                if (currentToken == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    currentToken = parser.nextToken();
                    if ("email".equals(fieldName)) {
                        userEmail = parser.getText();
                    } else if ("name".equals(fieldName)) {
                        userName = parser.getText();
                    }
                }
                currentToken = parser.nextToken();
            }

            if (userEmail == null) {
                throw new IOException("No se pudo obtener el email del usuario");
            }

            // Obtener o crear la sesión del usuario
            UserSession session = getUserSessionOrCreateUser(userName, userEmail);

            // Verificar si necesita completar datos
            if (needsComplementaryData(session.getEmail())) {
                showComplementaryDataForm(session);
            }

            return session;

        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error de base de datos: " + e.getMessage());
        }
    }

    private boolean needsComplementaryData(String email) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.conectar();
            stmt = conn.prepareStatement(CHECK_USER_DATA_COMPLETE);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            return !rs.next(); // true si necesita completar datos
        } finally {
            // ... cerrar recursos ...
        }
    }

    private void showComplementaryDataForm(UserSession session) {
        SwingUtilities.invokeLater(() -> {
            JFrame parentFrame = new JFrame();
            DatosComplementariosForm form = new DatosComplementariosForm(parentFrame, session);
            form.setVisible(true);
        });
    }

    private void createUsersTable(Connection conn) throws SQLException {
        String createTableSQL
                = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "apellido VARCHAR(100), "
                + "email VARCHAR(100) NOT NULL UNIQUE, "
                + "dni VARCHAR(20), "
                + "telefono VARCHAR(20), "
                + "direccion VARCHAR(200), "
                + "fecha_nacimiento DATE, "
                + "role VARCHAR(20) NOT NULL, "
                + "local_password VARCHAR(255), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "modified_at TIMESTAMP"
                + ")";

        try (PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
            stmt.execute();
        }

    }

    private UserSession getUserSessionOrCreateUser(String name, String email) throws IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.conectar();
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }

            // Primero verificamos si la tabla existe
            try {
                stmt = conn.prepareStatement(USER_ROLE_QUERY);
            } catch (SQLException e) {
                // Si la tabla no existe, la creamos
                createUsersTable(conn);
                stmt = conn.prepareStatement(USER_ROLE_QUERY);
            }

            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                if ("pending".equals(role)) {
                    JOptionPane.showMessageDialog(null,
                            "Tu cuenta está pendiente de asignación de rol por el administrador.",
                            "Cuenta Pendiente",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                return new UserSession(name, email, role);
            } else {
                PreparedStatement insertStmt = null;
                try {
                    insertStmt = conn.prepareStatement(INSERT_NEW_USER);
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, email);
                    insertStmt.executeUpdate();

                    JOptionPane.showMessageDialog(null,
                            "Te has registrado exitosamente. Por favor, espera a que el administrador te asigne un rol.",
                            "Registro Exitoso",
                            JOptionPane.INFORMATION_MESSAGE);

                    return new UserSession(name, email, "pending");
                } finally {
                    if (insertStmt != null) {
                        insertStmt.close();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error de base de datos: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    

    public void logout() throws IOException {
        // Eliminar los tokens almacenados
        try {
            FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
            dataStoreFactory.getDataStore("StoredCredential").clear();
        } catch (IOException e) {
            System.err.println("Error al limpiar las credenciales: " + e.getMessage());
        }

        // Eliminar el directorio de tokens
        java.io.File tokenDirectory = new java.io.File(TOKENS_DIRECTORY_PATH);
        if (tokenDirectory.exists()) {
            deleteDirectory(tokenDirectory);
        }
    }

    private void deleteDirectory(java.io.File directory) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}

class UserSession {
    private final String name;
    private final String email;
    private final String role;
    private String apellido;
    private String dni;
    private String telefono;
    private String direccion;
    private java.util.Date fechaNacimiento;
    private String cursoDiv; // Para alumnos
    private String legajo;   // Para profesores y alumnos

    public UserSession(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters básicos
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    // Getters y setters adicionales
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public java.util.Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(java.util.Date fechaNacimiento) { 
        this.fechaNacimiento = fechaNacimiento; 
    }

    public String getCursoDiv() { return cursoDiv; }
    public void setCursoDiv(String cursoDiv) { this.cursoDiv = cursoDiv; }

    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }

    // Método para obtener nombre completo
    public String getNombreCompleto() {
        if (apellido != null && !apellido.isEmpty()) {
            return name + " " + apellido;
        }
        return name;
    }
}
