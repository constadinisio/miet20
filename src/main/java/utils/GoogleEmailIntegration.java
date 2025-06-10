package main.java.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import main.java.database.Conexion;

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JOptionPane;

/**
 * Integración OAuth2 con Gmail para envío de emails automático Versión 2.0 -
 * Sistema de Gestión Escolar ET20
 */
public class GoogleEmailIntegration {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CLIENT_SECRETS_JSON = "{\"installed\":{\"client_id\":\"523285040044-env81ob4crprhpc29oi1gran241paoku.apps.googleusercontent.com\",\"project_id\":\"rare-ethos-441901-a6\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"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\",\"redirect_uris\":[\"http://localhost\"]}}";
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + "et20_app_tokens";

    private final HttpTransport httpTransport;
    private final Connection conn;

    // Cache de credenciales y configuración
    private static Credential cachedCredential = null;
    private static String cachedUserEmail = null;
    private static long lastAuthTime = 0;
    private static final long AUTH_CACHE_DURATION = 30 * 60 * 1000; // 30 minutos

    public GoogleEmailIntegration() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.conn = Conexion.getInstancia().verificarConexion();
    }

    /**
     * Configura EmailBoletinUtility automáticamente usando OAuth2 del usuario
     * actual
     */
    public static boolean configurarEmailAutomaticamente(int userId) {
        try {
            System.out.println("=== CONFIGURACIÓN AUTOMÁTICA DE EMAIL OAUTH2 ===");

            // Obtener datos del usuario
            String emailUsuario = obtenerEmailUsuario(userId);
            String nombreUsuario = obtenerNombreCompletoUsuario(userId);

            System.out.println("📧 Email del usuario: " + emailUsuario);
            System.out.println("👤 Nombre del usuario: " + nombreUsuario);

            if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
                System.err.println("❌ No se pudo obtener email del usuario ID: " + userId);
                return false;
            }

            // Verificar si es email de Gmail/Google Workspace
            if (!emailUsuario.toLowerCase().contains("@gmail.")
                    && !emailUsuario.toLowerCase().contains("@bue.edu.ar")
                    && !emailUsuario.toLowerCase().contains("@google.")) {
                System.err.println("⚠️ Email no es de Google/Gmail: " + emailUsuario);
                System.err.println("💡 OAuth2 solo funciona con cuentas Google");
                return false;
            }

            // Obtener credenciales OAuth2
            System.out.println("🔑 Obteniendo credenciales OAuth2...");
            Credential credential = obtenerCredencialOAuth2(emailUsuario);

            if (credential == null) {
                System.err.println("❌ No se pudieron obtener credenciales OAuth2");
                return false;
            }

            // Verificar tokens
            String accessToken = credential.getAccessToken();
            String refreshToken = credential.getRefreshToken();

            System.out.println("🔍 Verificando tokens:");
            System.out.println("  Access Token: " + (accessToken != null && !accessToken.isEmpty() ? "✅ Disponible" : "❌ No disponible"));
            System.out.println("  Refresh Token: " + (refreshToken != null && !refreshToken.isEmpty() ? "✅ Disponible" : "❌ No disponible"));

            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("❌ Token de acceso vacío o nulo");
                return false;
            }

            // Configurar EmailBoletinUtility con OAuth2
            System.out.println("⚙️ Configurando EmailBoletinUtility...");
            EmailBoletinUtility.configurarEmailConOAuth2(
                    emailUsuario,
                    nombreUsuario,
                    accessToken,
                    refreshToken,
                    extraerClientId(),
                    extraerClientSecret()
            );

            // Verificar que la configuración se aplicó correctamente
            EmailBoletinUtility.ConfiguracionEmail config = EmailBoletinUtility.obtenerConfiguracion();
            System.out.println("🔍 Verificando configuración aplicada:");
            System.out.println("  useOAuth2: " + config.useOAuth2);
            System.out.println("  emailRemitente: " + config.emailRemitente);
            System.out.println("  isCompleta: " + config.isCompleta());

            if (config.isCompleta()) {
                System.out.println("✅ Email configurado automáticamente para: " + emailUsuario);
                System.out.println("✅ Método: OAuth2 (sin contraseña de aplicación)");
                return true;
            } else {
                System.err.println("❌ La configuración no se aplicó correctamente");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error en configuración automática: " + e.getMessage());
            System.err.println("❌ Tipo de error: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene credenciales OAuth2 existentes o solicita autorización
     */
    private static Credential obtenerCredencialOAuth2(String userEmail) {
        try {
            // Verificar cache
            if (cachedCredential != null && userEmail.equals(cachedUserEmail)
                    && (System.currentTimeMillis() - lastAuthTime) < AUTH_CACHE_DURATION) {

                // Verificar si el token sigue siendo válido
                if (cachedCredential.getAccessToken() != null) {
                    System.out.println("✅ Usando credencial en cache para: " + userEmail);
                    return cachedCredential;
                }
            }

            System.out.println("🔑 Obteniendo nuevas credenciales OAuth2 para: " + userEmail);

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new StringReader(CLIENT_SECRETS_JSON));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets,
                    Arrays.asList(
                            "https://www.googleapis.com/auth/gmail.send",
                            "https://www.googleapis.com/auth/userinfo.email",
                            "https://www.googleapis.com/auth/userinfo.profile"
                    ))
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .setApprovalPrompt("force") // Fuerza obtención de refresh token
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();

            System.out.println("🌐 Iniciando proceso de autorización OAuth2...");
            System.out.println("💡 Se abrirá una ventana del navegador para autorización");

            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize("user");

            // Actualizar cache
            cachedCredential = credential;
            cachedUserEmail = userEmail;
            lastAuthTime = System.currentTimeMillis();

            System.out.println("✅ Credenciales OAuth2 obtenidas exitosamente");
            System.out.println("🔑 Access Token: " + (credential.getAccessToken() != null ? "✅ Disponible" : "❌ No disponible"));
            System.out.println("🔑 Refresh Token: " + (credential.getRefreshToken() != null ? "✅ Disponible" : "❌ No disponible"));

            // Información adicional para debug
            if (credential.getExpiresInSeconds() != null) {
                System.out.println("⏰ Token expira en: " + credential.getExpiresInSeconds() + " segundos");
            }

            return credential;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo credenciales OAuth2: " + e.getMessage());
            System.err.println("❌ Tipo de error: " + e.getClass().getSimpleName());
            e.printStackTrace();

            // Sugerencias de solución
            if (e.getMessage().contains("Connection refused")) {
                System.err.println("💡 Posible solución: Verificar que el puerto 8888 esté libre");
            }
            if (e.getMessage().contains("invalid_client")) {
                System.err.println("💡 Posible solución: Verificar configuración en Google Cloud Console");
            }

            return null;
        }
    }

    /**
     * Obtiene email del usuario desde la base de datos
     */
    private static String obtenerEmailUsuario(int userId) {
        try {
            Connection conn = Conexion.getInstancia().verificarConexion();
            if (conn == null) {
                return null;
            }

            String query = "SELECT mail FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            String email = null;
            if (rs.next()) {
                email = rs.getString("mail");
            }

            rs.close();
            ps.close();

            return email;

        } catch (SQLException e) {
            System.err.println("Error obteniendo email del usuario: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene nombre completo del usuario
     */
    private static String obtenerNombreCompletoUsuario(int userId) {
        try {
            Connection conn = Conexion.getInstancia().verificarConexion();
            if (conn == null) {
                return "Escuela Técnica N° 20";
            }

            String query = "SELECT apellido, nombre FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            String nombreCompleto = "Escuela Técnica N° 20";
            if (rs.next()) {
                String apellido = rs.getString("apellido");
                String nombre = rs.getString("nombre");
                if (apellido != null && nombre != null) {
                    nombreCompleto = apellido + ", " + nombre + " - ET20";
                }
            }

            rs.close();
            ps.close();

            return nombreCompleto;

        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre del usuario: " + e.getMessage());
            return "Escuela Técnica N° 20";
        }
    }

    /**
     * Extrae Client ID de la configuración
     */
    private static String extraerClientId() {
        try {
            return "523285040044-env81ob4crprhpc29oi1gran241paoku.apps.googleusercontent.com";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extrae Client Secret de la configuración
     */
    private static String extraerClientSecret() {
        try {
            return "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";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Verifica si hay credenciales OAuth2 válidas
     */
    public static boolean tieneCredencialesValidas(int userId) {
        try {
            String email = obtenerEmailUsuario(userId);
            if (email == null) {
                return false;
            }

            Credential credential = obtenerCredencialOAuth2(email);
            return credential != null && credential.getAccessToken() != null;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Limpia credenciales en cache
     */
    public static void limpiarCache() {
        cachedCredential = null;
        cachedUserEmail = null;
        lastAuthTime = 0;
        System.out.println("🧹 Cache de credenciales limpiado");
    }

    /**
     * Renueva token de acceso usando refresh token
     */
    public static boolean renovarToken(int userId) {
        try {
            System.out.println("🔄 Renovando token de acceso...");

            limpiarCache(); // Limpiar cache para forzar renovación
            return configurarEmailAutomaticamente(userId);

        } catch (Exception e) {
            System.err.println("❌ Error renovando token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prueba la configuración OAuth2 enviando un email de prueba
     */
    public static boolean probarConfiguracionOAuth2(int userId, String emailPrueba) {
        try {
            System.out.println("🧪 Probando configuración OAuth2...");

            // Configurar automáticamente
            if (!configurarEmailAutomaticamente(userId)) {
                return false;
            }

            // Probar envío
            return EmailBoletinUtility.probarConfiguracionEmail(emailPrueba);

        } catch (Exception e) {
            System.err.println("❌ Error probando configuración OAuth2: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información de debug de OAuth2
     */
    public static String obtenerInformacionDebugOAuth2(int userId) {
        StringBuilder info = new StringBuilder();
        info.append("=== DEBUG OAUTH2 GMAIL ===\n");

        try {
            String email = obtenerEmailUsuario(userId);
            info.append("Email usuario: ").append(email != null ? email : "No encontrado").append("\n");

            if (email != null) {
                boolean tieneCredenciales = tieneCredencialesValidas(userId);
                info.append("Credenciales válidas: ").append(tieneCredenciales ? "Sí" : "No").append("\n");

                if (cachedCredential != null) {
                    info.append("Token en cache: ").append(cachedCredential.getAccessToken() != null ? "Sí" : "No").append("\n");
                    info.append("Refresh token: ").append(cachedCredential.getRefreshToken() != null ? "Sí" : "No").append("\n");

                    if (cachedCredential.getExpiresInSeconds() != null) {
                        info.append("Expira en: ").append(cachedCredential.getExpiresInSeconds()).append(" segundos\n");
                    }
                }
            }

            info.append("Directorio tokens: ").append(TOKENS_DIRECTORY_PATH).append("\n");
            info.append("Client ID configurado: ").append(!extraerClientId().isEmpty() ? "Sí" : "No").append("\n");

        } catch (Exception e) {
            info.append("Error obteniendo debug: ").append(e.getMessage()).append("\n");
        }

        return info.toString();
    }

    /**
     * Muestra diálogo de ayuda para configuración OAuth2
     */
    public static void mostrarAyudaOAuth2() {
        String mensaje = """
                🔧 CONFIGURACIÓN AUTOMÁTICA DE EMAIL
                
                ✅ OAuth2 con Gmail (Recomendado)
                • Usa tu cuenta Google existente
                • No requiere contraseña de aplicación
                • Más seguro y fácil de usar
                
                📋 Proceso automático:
                1. Se abre ventana de autorización Google
                2. Inicias sesión con tu cuenta @gmail.com
                3. Autorizas el acceso para envío de emails
                4. El sistema configura todo automáticamente
                
                ⚙️ Configuración manual (Alternativa)
                • Requiere contraseña de aplicación de Gmail
                • Más complejo de configurar
                • Para usuarios avanzados
                
                💡 Recomendación: Usar configuración automática
                """;

        JOptionPane.showMessageDialog(null, mensaje,
                "Ayuda - Configuración de Email",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
