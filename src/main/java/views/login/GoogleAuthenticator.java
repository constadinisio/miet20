package main.java.views.login;

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
import main.java.database.Conexion;

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Arrays;

public class GoogleAuthenticator {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CLIENT_SECRETS_JSON = "{\"installed\":{\"client_id\":\"523285040044-env81ob4crprhpc29oi1gran241paoku.apps.googleusercontent.com\",\"project_id\":\"rare-ethos-441901-a6\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"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\",\"redirect_uris\":[\"http://localhost\"]}}";
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + "et20_app_tokens";
    private final HttpTransport httpTransport;

    public GoogleAuthenticator() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    public UserSession authenticateUser() throws IOException {
        try {
        Connection conn = Conexion.getInstancia().verificarConexion();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos");
        }

        // Cargar secretos desde la cadena JSON en lugar del archivo
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new StringReader(CLIENT_SECRETS_JSON));


            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets,
                    Arrays.asList(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email"
                    ))
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
            String userEmail = null;
            String nombre = null;
            String apellido = null;
            String fotoUrl = null;

            while (parser.nextToken() != null) {
                String fieldName = parser.getCurrentName();
                if (fieldName != null) {
                    parser.nextToken(); // mover al valor
                    switch (fieldName) {
                        case "email":
                            userEmail = parser.getText();
                            break;
                        case "given_name":
                            nombre = parser.getText();
                            break;
                        case "family_name":
                            apellido = parser.getText();
                            break;
                        case "picture":
                            fotoUrl = parser.getText();
                            break;
                    }
                }
            }

            if (userEmail == null) {
                throw new IOException("No se pudo obtener el email del usuario");
            }

            if (nombre == null) {
                nombre = userEmail.split("@")[0];
            }
            if (apellido == null) {
                apellido = "";
            }

            System.out.println("Datos obtenidos de Google - Email: " + userEmail
                    + ", Nombre: " + nombre
                    + ", Apellido: " + apellido
                    + ", Foto: " + fotoUrl);

            return getUserSessionOrCreateUser(nombre, apellido, userEmail, fotoUrl);

        } catch (SQLException e) {
            throw new IOException("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Error durante la autenticación: " + e.getMessage());
        }
    }

    private UserSession getUserSessionOrCreateUser(String nombre, String apellido, String email, String fotoUrl)
            throws SQLException {
        // NUEVO COMPORTAMIENTO: No crear ni modificar usuarios automáticamente
        // Solo retornar una sesión parcial con los datos de Google
        // El PrimerIngresoManager se encargará de toda la lógica de usuarios
        
        System.out.println("✅ Autenticación con Google exitosa - retornando sesión parcial");
        System.out.println("📧 Email: " + email);
        System.out.println("👤 Nombre: " + nombre + " " + apellido);
        
        // Retornar sesión parcial sin ID de usuario (será manejado por PrimerIngresoManager)
        return new UserSession(
            -1,        // ID temporal (-1 indica que es sesión parcial)
            nombre,    // Nombre de Google
            apellido,  // Apellido de Google  
            email,     // Email de Google
            -1,        // Rol temporal (-1 indica que será determinado después)
            fotoUrl    // Foto de Google
        );
    }

    public void logout() throws IOException {
        try {
            FileDataStoreFactory dataStoreFactory
                    = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
            dataStoreFactory.getDataStore("StoredCredential").clear();
        } catch (IOException e) {
            System.err.println("Error al limpiar las credenciales: " + e.getMessage());
        }

        deleteTokenDirectory();
    }

    private void deleteTokenDirectory() {
        java.io.File tokenDirectory = new java.io.File(TOKENS_DIRECTORY_PATH);
        if (tokenDirectory.exists()) {
            System.out.println("🧹 Limpiando directorio de tokens: " + TOKENS_DIRECTORY_PATH);
            java.io.File[] files = tokenDirectory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.delete()) {
                        System.out.println("✅ Token eliminado: " + file.getName());
                    } else {
                        System.err.println("⚠️ No se pudo eliminar: " + file.getName());
                    }
                }
            }
            if (tokenDirectory.delete()) {
                System.out.println("✅ Directorio de tokens eliminado completamente");
            } else {
                System.err.println("⚠️ No se pudo eliminar el directorio de tokens");
            }
        } else {
            System.out.println("ℹ️ No hay directorio de tokens para limpiar");
        }
    }
}
