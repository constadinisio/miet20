package login;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Collections;
import javax.swing.JOptionPane;

public class GoogleAuthenticator {
    private static final String APPLICATION_NAME = "School Management System";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "client_secret.json";
    private static final String USER_ROLE_QUERY = "SELECT rol FROM usuarios WHERE mail = ?";
    private static final String INSERT_NEW_USER = 
        "INSERT INTO usuarios (nombre, apellido, mail, rol, status, contrasena) " +
        "VALUES (?, ?, ?, 0, 1, 'default_password')";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private final HttpTransport httpTransport;
    private final Conexion dbConnection;

    public GoogleAuthenticator() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.dbConnection = new Conexion();
    }

    public UserSession authenticateUser() throws IOException {
        try {
            Connection conn = dbConnection.getConexion();
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos");
            }

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
            
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
            HttpRequest request = requestFactory.buildGetRequest(url);
            
            
            HttpResponse response = request.execute();
            
            JsonParser parser = JSON_FACTORY.createJsonParser(response.getContent());
            JsonToken currentToken = parser.getCurrentToken();
            
            String userEmail = null;
        String fullName = "Usuario de Google";  // valor por defecto
        
        while (currentToken != JsonToken.END_OBJECT) {
            if (currentToken == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();
                currentToken = parser.nextToken();
                if ("email".equals(fieldName)) {
                    userEmail = parser.getText();
                } else if ("name".equals(fieldName)) {
                    fullName = parser.getText();
                }
            }
            currentToken = parser.nextToken();
        }
        
        // Separar nombre y apellido
        String nombre, apellido;
        if (fullName.contains(" ")) {
            String[] partes = fullName.split(" ", 2);
            nombre = partes[0];
            apellido = partes[1];
        } else {
            nombre = fullName;
            apellido = "";
        }

        if (userEmail == null) {
            throw new IOException("No se pudo obtener el email del usuario");
        }

        return getUserSessionOrCreateUser(nombre, apellido, userEmail);
        
    } catch (SQLException e) {
        throw new IOException("Error de base de datos: " + e.getMessage());
        }
    }

    private UserSession getUserSessionOrCreateUser(String nombre, String apellido, String email) 
            throws SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = dbConnection.getConexion();
        // Primero verificar si el usuario ya existe
        stmt = conn.prepareStatement(USER_ROLE_QUERY);
        stmt.setString(1, email);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            int rol = rs.getInt("rol");
            if (rol == 0) {
                JOptionPane.showMessageDialog(null, 
                    "Tu cuenta está pendiente de asignación de rol.",
                    "Cuenta Pendiente",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            return new UserSession(nombre, apellido, email, rol);
        } else {
            // Crear nuevo usuario con rol 0 (pending)
            PreparedStatement insertStmt = conn.prepareStatement(INSERT_NEW_USER);
            insertStmt.setString(1, nombre);
            insertStmt.setString(2, email);
            insertStmt.setString(3, email);
            insertStmt.executeUpdate();
            
            JOptionPane.showMessageDialog(null, 
                "Te has registrado exitosamente. Por favor, espera a que el administrador te asigne un rol.",
                "Registro Exitoso",
                JOptionPane.INFORMATION_MESSAGE);
            
            return new UserSession(nombre, apellido, email, 0);
        }
    } finally {
        if (rs != null) rs.close();
        if (stmt != null) stmt.close();
    }
}

    public void logout() throws IOException {
        try {
            FileDataStoreFactory dataStoreFactory = 
                new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
            dataStoreFactory.getDataStore("StoredCredential").clear();
        } catch (IOException e) {
            System.err.println("Error al limpiar las credenciales: " + e.getMessage());
        }
        
        deleteTokenDirectory();
    }

    private void deleteTokenDirectory() {
        java.io.File tokenDirectory = new java.io.File(TOKENS_DIRECTORY_PATH);
        if (tokenDirectory.exists()) {
            for (java.io.File file : tokenDirectory.listFiles()) {
                file.delete();
            }
            tokenDirectory.delete();
        }
    }
}