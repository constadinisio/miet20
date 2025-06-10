package main.java.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import main.java.database.Conexion;
import main.java.utils.GoogleEmailIntegration;

/**
 * Utilidad para envío de boletines por email Versión 1.0 - Sistema de Gestión
 * Escolar ET20
 *
 * @author Sistema ET20
 */
public class EmailBoletinUtility {

    // Configuración por defecto del servidor SMTP
    private static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "587";
    private static final boolean DEFAULT_USE_TLS = true;
    private static final boolean DEFAULT_USE_AUTH = true;

    // Configuración de la escuela
    private static final String NOMBRE_ESCUELA = "Escuela Técnica N° 20";
    private static final String DIRECCION_ESCUELA = "Buenos Aires, Argentina";

    /**
     * Configuración de email almacenada
     */
    public static class ConfiguracionEmail {

        public String smtpHost;
        public String smtpPort;
        public boolean useTLS;
        public boolean useAuth;
        public String emailRemitente;
        public String passwordRemitente;
        public String nombreRemitente;

        // RESTAURAR CAMPOS OAUTH2
        public String accessToken;
        public String refreshToken;
        public String clientId;
        public String clientSecret;
        public boolean useOAuth2;

        public ConfiguracionEmail() {
            this.smtpHost = "smtp.gmail.com";
            this.smtpPort = "587";
            this.useTLS = true;
            this.useAuth = true;
            this.nombreRemitente = "Escuela Técnica N° 20";
            this.useOAuth2 = false; // Por defecto false
        }

        public boolean isCompleta() {
            if (useOAuth2) {
                // Para OAuth2: necesita email y access token
                boolean completa = emailRemitente != null && !emailRemitente.trim().isEmpty()
                        && accessToken != null && !accessToken.trim().isEmpty();

                System.out.println("🔍 Verificando configuración OAuth2:");
                System.out.println("  Email: " + (emailRemitente != null ? emailRemitente : "null"));
                System.out.println("  Access Token: " + (accessToken != null && !accessToken.isEmpty() ? "✅ Presente" : "❌ Faltante"));
                System.out.println("  Configuración completa: " + (completa ? "✅ Sí" : "❌ No"));

                return completa;
            } else {
                // Para SMTP tradicional: necesita email y contraseña
                boolean completa = emailRemitente != null && !emailRemitente.trim().isEmpty()
                        && passwordRemitente != null && !passwordRemitente.trim().isEmpty()
                        && smtpHost != null && !smtpHost.trim().isEmpty()
                        && smtpPort != null && !smtpPort.trim().isEmpty();

                System.out.println("🔍 Verificando configuración SMTP:");
                System.out.println("  Email: " + (emailRemitente != null ? emailRemitente : "null"));
                System.out.println("  Password: " + (passwordRemitente != null && !passwordRemitente.isEmpty() ? "✅ Presente" : "❌ Faltante"));
                System.out.println("  Configuración completa: " + (completa ? "✅ Sí" : "❌ No"));

                return completa;
            }
        }
    }

    public static void configurarEmailConOAuth2(String emailRemitente, String nombreRemitente,
            String accessToken, String refreshToken,
            String clientId, String clientSecret) {

        System.out.println("=== CONFIGURANDO EMAIL CON OAUTH2 ===");
        System.out.println("Email: " + emailRemitente);
        System.out.println("Nombre: " + nombreRemitente);
        System.out.println("Access Token: " + (accessToken != null && !accessToken.isEmpty() ? "✅ Presente" : "❌ Faltante"));

        configuracionActual.emailRemitente = emailRemitente;
        configuracionActual.nombreRemitente = nombreRemitente;
        configuracionActual.accessToken = accessToken;
        configuracionActual.refreshToken = refreshToken;
        configuracionActual.clientId = clientId;
        configuracionActual.clientSecret = clientSecret;
        configuracionActual.useOAuth2 = true;

        // Configurar parámetros SMTP para Gmail OAuth2
        configuracionActual.smtpHost = "smtp.gmail.com";
        configuracionActual.smtpPort = "587";
        configuracionActual.useTLS = true;
        configuracionActual.useAuth = true;

        System.out.println("✅ Configuración OAuth2 establecida exitosamente");
        System.out.println("✅ useOAuth2: " + configuracionActual.useOAuth2);
        System.out.println("✅ Configuración completa: " + configuracionActual.isCompleta());
    }

    /**
     * Información del destinatario
     */
    public static class DestinatarioEmail {

        public int alumnoId;
        public String nombreAlumno;
        public String emailAlumno;
        public String dniAlumno;
        public String curso;
        public String division;
        public String emailFamilia; // Para implementación futura
        public String nombreFamilia; // Para implementación futura

        public DestinatarioEmail(int alumnoId, String nombreAlumno, String emailAlumno,
                String dniAlumno, String curso, String division) {
            this.alumnoId = alumnoId;
            this.nombreAlumno = nombreAlumno;
            this.emailAlumno = emailAlumno;
            this.dniAlumno = dniAlumno;
            this.curso = curso;
            this.division = division;
        }

        public boolean tieneEmailValido() {
            return emailAlumno != null && !emailAlumno.trim().isEmpty()
                    && emailAlumno.contains("@") && emailAlumno.contains(".");
        }
    }

    /**
     * Resultado del envío
     */
    public static class ResultadoEnvio {

        public boolean exitoso;
        public String mensaje;
        public int totalEnviados;
        public int totalErrores;
        public List<String> erroresDetallados;

        public ResultadoEnvio() {
            this.erroresDetallados = new ArrayList<>();
        }
    }

    /**
     * Configuración actual del email
     */
    private static ConfiguracionEmail configuracionActual = new ConfiguracionEmail();

    /**
     * Configura los parámetros de email
     */
    public static void configurarEmail(String smtpHost, String smtpPort, String emailRemitente,
            String passwordRemitente, String nombreRemitente,
            boolean useTLS, boolean useAuth) {
        configuracionActual.smtpHost = smtpHost;
        configuracionActual.smtpPort = smtpPort;
        configuracionActual.emailRemitente = emailRemitente;
        configuracionActual.passwordRemitente = passwordRemitente;
        configuracionActual.nombreRemitente = nombreRemitente;
        configuracionActual.useTLS = useTLS;
        configuracionActual.useAuth = useAuth;

        System.out.println("✅ Configuración de email actualizada");
        System.out.println("SMTP: " + smtpHost + ":" + smtpPort);
        System.out.println("Remitente: " + nombreRemitente + " <" + emailRemitente + ">");
    }

    /**
     * Obtiene la configuración actual
     */
    public static ConfiguracionEmail obtenerConfiguracion() {
        return configuracionActual;
    }

    /**
     * Envía un boletín individual por email
     */
    /**
     * CORRECCIÓN ESPECÍFICA: OAuth2 para Gmail de dominio educativo El problema
     * es que los dominios educativos como @bue.edu.ar pueden tener
     * restricciones especiales
     */
    private static Session crearSesionEmail() {
        Properties props = new Properties();
        props.put("mail.smtp.host", configuracionActual.smtpHost);
        props.put("mail.smtp.port", configuracionActual.smtpPort);
        props.put("mail.smtp.auth", String.valueOf(configuracionActual.useAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(configuracionActual.useTLS));
        props.put("mail.smtp.ssl.trust", configuracionActual.smtpHost);

        if (configuracionActual.useOAuth2) {
            // CONFIGURACIÓN ESPECIAL PARA OAUTH2 CON DOMINIOS EDUCATIVOS
            System.out.println("🔐 Configurando sesión OAuth2 para dominio educativo");
            System.out.println("📧 Email: " + configuracionActual.emailRemitente);
            System.out.println("🏫 Dominio: " + (configuracionActual.emailRemitente.contains("@bue.edu.ar") ? "Educativo (Buenos Aires)" : "Estándar"));

            // Configuraciones específicas para OAuth2
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtp.auth.xoauth2.disable", "false");

            // Configuraciones adicionales para dominios educativos
            if (configuracionActual.emailRemitente.contains("@bue.edu.ar")) {
                System.out.println("🎓 Aplicando configuración para dominio educativo Buenos Aires");
                props.put("mail.smtp.host", "smtp.gmail.com"); // Forzar Gmail
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.enable", "false");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

                // Configuraciones adicionales para Google Workspace
                props.put("mail.smtp.auth.xoauth2.two_legged", "false");
                props.put("mail.smtp.timeout", "60000");
                props.put("mail.smtp.connectiontimeout", "60000");
            } else {
                // Configuración estándar para Gmail personal
                props.put("mail.smtp.ssl.enable", "false");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            }

            // Habilitar debug si es necesario
            if (System.getProperty("mail.debug") != null) {
                props.put("mail.debug", "true");
                props.put("mail.smtp.debug", "true");
            }

            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("🔐 Autenticando con OAuth2...");
                    System.out.println("📧 Usuario: " + configuracionActual.emailRemitente);
                    System.out.println("🔑 Token length: " + (configuracionActual.accessToken != null ? configuracionActual.accessToken.length() : 0));

                    return new PasswordAuthentication(
                            configuracionActual.emailRemitente,
                            configuracionActual.accessToken
                    );
                }
            };

            Session session = Session.getInstance(props, auth);

            // Habilitar debug a nivel de sesión si es necesario
            if (System.getProperty("mail.debug") != null) {
                session.setDebug(true);
            }

            return session;

        } else {
            // CONFIGURACIÓN TRADICIONAL (fallback)
            System.out.println("🔐 Configurando sesión con SMTP tradicional");

            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.starttls.required", "true");

            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("🔐 Autenticando con contraseña tradicional...");
                    return new PasswordAuthentication(
                            configuracionActual.emailRemitente,
                            configuracionActual.passwordRemitente
                    );
                }
            };

            return Session.getInstance(props, auth);
        }
    }

// 3. ACTUALIZAR: Método enviarBoletinIndividual con prueba previa
    public static boolean enviarBoletinIndividual(String urlBoletin, DestinatarioEmail destinatario,
            String periodo, String asunto, String mensaje) {
        try {
            System.out.println("=== ENVIANDO BOLETÍN INDIVIDUAL ===");
            System.out.println("Destinatario: " + destinatario.nombreAlumno);
            System.out.println("Email: " + destinatario.emailAlumno);
            System.out.println("Boletín: " + urlBoletin);

            // Verificar librerías
            if (!verificarLibreriasEmail()) {
                System.err.println("❌ Librerías de email no disponibles");
                return false;
            }

            // Verificar configuración
            System.out.println("🔍 Verificando configuración de email...");
            if (!configuracionActual.isCompleta()) {
                System.err.println("❌ Configuración de email incompleta");
                return false;
            }

            if (!destinatario.tieneEmailValido()) {
                System.err.println("❌ Email del destinatario inválido: " + destinatario.emailAlumno);
                return false;
            }

            // NUEVA VERIFICACIÓN: Probar conexión SMTP primero
            System.out.println("🧪 Probando conexión SMTP antes del envío...");
            if (!probarConexionSMTP()) {
                System.err.println("❌ Falló la prueba de conexión SMTP");
                return false;
            }
            System.out.println("✅ Conexión SMTP verificada");

            // Descargar boletín temporal
            System.out.println("📥 Descargando boletín...");
            File archivoTemporal = descargarBoletinTemporal(urlBoletin, destinatario.nombreAlumno, periodo);
            if (archivoTemporal == null) {
                System.err.println("❌ No se pudo descargar el boletín");
                return false;
            }

            // Crear sesión de email
            System.out.println("📧 Creando sesión de email...");
            Session session = crearSesionEmail();
            if (session == null) {
                System.err.println("❌ No se pudo crear sesión de email");
                return false;
            }

            // Crear mensaje
            System.out.println("✍️ Creando mensaje...");
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(configuracionActual.emailRemitente, configuracionActual.nombreRemitente));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario.emailAlumno));

            // Asunto personalizado
            String asuntoFinal = (asunto != null && !asunto.trim().isEmpty())
                    ? asunto
                    : "Boletín de Calificaciones - " + periodo + " - " + destinatario.nombreAlumno;
            message.setSubject(asuntoFinal);

            // Crear contenido del mensaje
            Multipart multipart = new MimeMultipart();

            // Parte del texto
            BodyPart textPart = new MimeBodyPart();
            String contenidoHtml = crearContenidoEmail(destinatario, periodo, mensaje);
            textPart.setContent(contenidoHtml, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Adjuntar boletín
            BodyPart adjuntoPart = new MimeBodyPart();
            DataSource source = new FileDataSource(archivoTemporal);
            adjuntoPart.setDataHandler(new DataHandler(source));
            adjuntoPart.setFileName("Boletin_" + destinatario.nombreAlumno.replaceAll("[^a-zA-Z0-9]", "_")
                    + "_" + periodo + ".xlsx");
            multipart.addBodyPart(adjuntoPart);

            message.setContent(multipart);

            // Enviar
            System.out.println("📤 Enviando email...");
            Transport.send(message);

            // Limpiar archivo temporal
            if (archivoTemporal.exists()) {
                archivoTemporal.delete();
            }

            System.out.println("✅ Boletín enviado exitosamente a: " + destinatario.emailAlumno);

            // Registrar envío en BD
            registrarEnvioEnBD(destinatario.alumnoId, periodo, destinatario.emailAlumno, true, null);

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error enviando boletín: " + e.getMessage());
            System.err.println("❌ Tipo de error: " + e.getClass().getSimpleName());

            // Análisis de errores específicos
            if (e instanceof javax.mail.AuthenticationFailedException) {
                System.err.println("💡 Error de autenticación específico:");
                System.err.println("   - Para OAuth2: Token puede haber expirado o no tener permisos");
                System.err.println("   - Para dominios educativos: Verificar políticas de Google Workspace");
            }

            e.printStackTrace();

            // Registrar error en BD
            registrarEnvioEnBD(destinatario.alumnoId, periodo, destinatario.emailAlumno, false, e.getMessage());

            return false;
        }
    }

    /**
     * Envía boletines masivos a múltiples alumnos
     */
    public static ResultadoEnvio enviarBoletinesMasivos(List<GestorBoletines.InfoBoletin> boletines,
            String asuntoPersonalizado, String mensajePersonalizado) {
        ResultadoEnvio resultado = new ResultadoEnvio();

        try {
            System.out.println("=== ENVIANDO BOLETINES MASIVOS ===");
            System.out.println("Total de boletines: " + boletines.size());

            if (!configuracionActual.isCompleta()) {
                resultado.exitoso = false;
                resultado.mensaje = "Configuración de email incompleta";
                return resultado;
            }

            // Obtener emails de los alumnos
            List<DestinatarioEmail> destinatarios = obtenerDestinatariosDesdeBoletines(boletines);

            System.out.println("Destinatarios con email válido: " + destinatarios.size());

            if (destinatarios.isEmpty()) {
                resultado.exitoso = false;
                resultado.mensaje = "No se encontraron destinatarios con emails válidos";
                return resultado;
            }

            // Enviar a cada destinatario
            for (int i = 0; i < destinatarios.size(); i++) {
                DestinatarioEmail destinatario = destinatarios.get(i);

                // Buscar el boletín correspondiente
                GestorBoletines.InfoBoletin boletinCorrespondiente = null;
                for (GestorBoletines.InfoBoletin boletin : boletines) {
                    if (boletin.alumnoId == destinatario.alumnoId) {
                        boletinCorrespondiente = boletin;
                        break;
                    }
                }

                if (boletinCorrespondiente == null) {
                    resultado.totalErrores++;
                    resultado.erroresDetallados.add("No se encontró boletín para: " + destinatario.nombreAlumno);
                    continue;
                }

                try {
                    System.out.println("Enviando " + (i + 1) + "/" + destinatarios.size() + ": " + destinatario.nombreAlumno);

                    boolean enviado = enviarBoletinIndividual(boletinCorrespondiente.rutaArchivo,
                            destinatario, boletinCorrespondiente.periodo,
                            asuntoPersonalizado, mensajePersonalizado);

                    if (enviado) {
                        resultado.totalEnviados++;
                    } else {
                        resultado.totalErrores++;
                        resultado.erroresDetallados.add("Error enviando a: " + destinatario.nombreAlumno);
                    }

                    // Pausa entre envíos para evitar spam
                    Thread.sleep(2000);

                } catch (Exception e) {
                    resultado.totalErrores++;
                    resultado.erroresDetallados.add("Error con " + destinatario.nombreAlumno + ": " + e.getMessage());
                    System.err.println("Error enviando a " + destinatario.nombreAlumno + ": " + e.getMessage());
                }
            }

            resultado.exitoso = resultado.totalEnviados > 0;
            resultado.mensaje = String.format("Enviados: %d, Errores: %d",
                    resultado.totalEnviados, resultado.totalErrores);

            System.out.println("=== ENVÍO MASIVO COMPLETADO ===");
            System.out.println(resultado.mensaje);

        } catch (Exception e) {
            resultado.exitoso = false;
            resultado.mensaje = "Error general en envío masivo: " + e.getMessage();
            System.err.println("❌ " + resultado.mensaje);
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * Descarga un boletín temporalmente para adjuntar al email
     */
    private static File descargarBoletinTemporal(String urlBoletin, String nombreAlumno, String periodo) {
        try {
            System.out.println("📥 Descargando boletín: " + urlBoletin);

            URL url = new URL(urlBoletin);
            String nombreArchivo = "boletin_temp_" + nombreAlumno.replaceAll("[^a-zA-Z0-9]", "_")
                    + "_" + periodo + "_" + System.currentTimeMillis() + ".xlsx";

            File archivoTemporal = new File(System.getProperty("java.io.tmpdir"), nombreArchivo);

            try (InputStream inputStream = url.openStream(); FileOutputStream outputStream = new FileOutputStream(archivoTemporal)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            if (archivoTemporal.exists() && archivoTemporal.length() > 0) {
                System.out.println("✅ Boletín descargado: " + archivoTemporal.getAbsolutePath()
                        + " (" + archivoTemporal.length() + " bytes)");
                return archivoTemporal;
            } else {
                System.err.println("❌ Archivo descargado está vacío");
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error descargando boletín: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Crea el contenido HTML del email
     */
    private static String crearContenidoEmail(DestinatarioEmail destinatario, String periodo, String mensajePersonalizado) {
        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='es'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Boletín de Calificaciones</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 20px; margin-bottom: 30px; }");
        html.append(".logo { color: #007bff; font-size: 24px; font-weight: bold; margin-bottom: 10px; }");
        html.append(".info-box { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append(".student-info { display: flex; justify-content: space-between; margin: 10px 0; }");
        html.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }");
        html.append(".message { background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='container'>");

        // Encabezado
        html.append("<div class='header'>");
        html.append("<div class='logo'>").append(NOMBRE_ESCUELA).append("</div>");
        html.append("<p>Sistema de Gestión Escolar</p>");
        html.append("</div>");

        // Saludo
        html.append("<h2>Estimado/a ").append(destinatario.nombreAlumno).append(",</h2>");

        // Mensaje personalizado
        if (mensajePersonalizado != null && !mensajePersonalizado.trim().isEmpty()) {
            html.append("<div class='message'>");
            html.append("<p>").append(mensajePersonalizado).append("</p>");
            html.append("</div>");
        }

        // Información del boletín
        html.append("<p>Te enviamos adjunto tu boletín de calificaciones correspondiente al <strong>")
                .append(periodo).append("</strong>.</p>");

        // Información del estudiante
        html.append("<div class='info-box'>");
        html.append("<h3>Información del Estudiante</h3>");
        html.append("<div class='student-info'>");
        html.append("<span><strong>Estudiante:</strong> ").append(destinatario.nombreAlumno).append("</span>");
        html.append("</div>");
        html.append("<div class='student-info'>");
        html.append("<span><strong>DNI:</strong> ").append(destinatario.dniAlumno != null ? destinatario.dniAlumno : "No especificado").append("</span>");
        html.append("</div>");
        html.append("<div class='student-info'>");
        html.append("<span><strong>Curso:</strong> ").append(destinatario.curso).append("°").append(destinatario.division).append("</span>");
        html.append("</div>");
        html.append("<div class='student-info'>");
        html.append("<span><strong>Período:</strong> ").append(periodo).append("</span>");
        html.append("</div>");
        html.append("<div class='student-info'>");
        html.append("<span><strong>Fecha de envío:</strong> ").append(fechaActual).append("</span>");
        html.append("</div>");
        html.append("</div>");

        // Instrucciones
        html.append("<h3>Instrucciones</h3>");
        html.append("<ul>");
        html.append("<li>El boletín se encuentra adjunto a este correo en formato Excel.</li>");
        html.append("<li>Puedes abrirlo con Microsoft Excel, Google Sheets o cualquier programa compatible.</li>");
        html.append("<li>Si tienes problemas para abrir el archivo, contacta con la secretaría de la escuela.</li>");
        html.append("<li>Este boletín es oficial y tiene validez académica.</li>");
        html.append("</ul>");

        // Información de contacto
        html.append("<div class='info-box'>");
        html.append("<h3>Información de Contacto</h3>");
        html.append("<p><strong>").append(NOMBRE_ESCUELA).append("</strong><br>");
        html.append(DIRECCION_ESCUELA).append("<br>");
        html.append("Secretaría: Lunes a Viernes de 8:00 a 16:00 hs</p>");
        html.append("</div>");

        // Pie de página
        html.append("<div class='footer'>");
        html.append("<p>Este es un correo automático del Sistema de Gestión Escolar.<br>");
        html.append("Por favor, no responda a este mensaje.<br>");
        html.append("© ").append(LocalDateTime.now().getYear()).append(" ").append(NOMBRE_ESCUELA).append("</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Obtiene destinatarios con emails válidos desde una lista de boletines
     */
    private static List<DestinatarioEmail> obtenerDestinatariosDesdeBoletines(List<GestorBoletines.InfoBoletin> boletines) {
        List<DestinatarioEmail> destinatarios = new ArrayList<>();

        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                System.err.println("❌ Error de conexión a BD");
                return destinatarios;
            }

            for (GestorBoletines.InfoBoletin boletin : boletines) {
                String query = "SELECT mail FROM usuarios WHERE id = ? AND rol = '4'";
                PreparedStatement ps = conect.prepareStatement(query);
                ps.setInt(1, boletin.alumnoId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String email = rs.getString("mail");

                    DestinatarioEmail destinatario = new DestinatarioEmail(
                            boletin.alumnoId,
                            boletin.alumnoNombre,
                            email,
                            boletin.alumnoDni,
                            boletin.curso,
                            boletin.division
                    );

                    if (destinatario.tieneEmailValido()) {
                        destinatarios.add(destinatario);
                        System.out.println("✅ Destinatario válido: " + destinatario.nombreAlumno + " - " + destinatario.emailAlumno);
                    } else {
                        System.out.println("⚠️ Email inválido para: " + destinatario.nombreAlumno + " - " + email);
                    }
                }

                rs.close();
                ps.close();
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo destinatarios: " + e.getMessage());
            e.printStackTrace();
        }

        return destinatarios;
    }

    /**
     * Registra el envío en la base de datos
     */
    private static void registrarEnvioEnBD(int alumnoId, String periodo, String emailDestino,
            boolean exitoso, String mensajeError) {
        try {
            // Por ahora solo loggeamos, en el futuro se puede crear una tabla de envíos
            String status = exitoso ? "ENVIADO" : "ERROR";
            String logMessage = String.format("[%s] Email %s para alumno ID %d (%s) - Período: %s - Destino: %s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    status, alumnoId, status, periodo, emailDestino);

            if (!exitoso && mensajeError != null) {
                logMessage += " - Error: " + mensajeError;
            }

            System.out.println("📧 " + logMessage);

            // TODO: Crear tabla email_envios para registrar histórico
            /*
            CREATE TABLE email_envios (
                id INT AUTO_INCREMENT PRIMARY KEY,
                alumno_id INT NOT NULL,
                email_destino VARCHAR(255) NOT NULL,
                periodo VARCHAR(50) NOT NULL,
                fecha_envio DATETIME NOT NULL,
                exitoso BOOLEAN NOT NULL,
                mensaje_error TEXT,
                creado_por INT,
                FOREIGN KEY (alumno_id) REFERENCES usuarios(id)
            );
             */
        } catch (Exception e) {
            System.err.println("Error registrando envío en BD: " + e.getMessage());
        }
    }

    /**
     * Prueba la configuración de email enviando un mensaje de prueba
     */
    public static boolean probarConfiguracionEmail(String emailPrueba) {
        try {
            System.out.println("=== PROBANDO CONFIGURACIÓN DE EMAIL ===");

            if (!configuracionActual.isCompleta()) {
                System.err.println("❌ Configuración incompleta");
                return false;
            }

            Session session = crearSesionEmail();

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(configuracionActual.emailRemitente, configuracionActual.nombreRemitente));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailPrueba));
            message.setSubject("Prueba de configuración - Sistema de Gestión Escolar");

            String contenidoPrueba = "Este es un mensaje de prueba del sistema de envío de boletines.\n\n"
                    + "Si recibe este mensaje, la configuración está funcionando correctamente.\n\n"
                    + "Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n"
                    + "Servidor: " + configuracionActual.smtpHost + ":" + configuracionActual.smtpPort + "\n\n"
                    + "Saludos,\n" + configuracionActual.nombreRemitente;

            message.setText(contenidoPrueba);

            Transport.send(message);

            System.out.println("✅ Email de prueba enviado exitosamente a: " + emailPrueba);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error en prueba de email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean configurarDesdeUsuarioLogueado(int userId) {
        try {
            System.out.println("=== CONFIGURANDO EMAIL DESDE USUARIO OAUTH2 ===");

            // Usar la nueva integración OAuth2
            return GoogleEmailIntegration.configurarEmailAutomaticamente(userId);

        } catch (Exception e) {
            System.err.println("❌ Error configurando desde usuario OAuth2: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String obtenerAccessTokenDelUsuario(int userId) {
        // IMPLEMENTAR: Obtener el access token del usuario desde tu sistema OAuth2
        // Puede estar en:
        // - Sesión actual
        // - Base de datos
        // - Cache/memoria
        // - Archivo de configuración

        // Ejemplo de implementación (adapta según tu sistema):
        try {
            // Si guardas tokens en BD:
            Connection conect = Conexion.getInstancia().verificarConexion();
            String query = "SELECT oauth_access_token FROM oauth_tokens WHERE user_id = ? AND provider = 'google'";
            PreparedStatement ps = conect.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String token = rs.getString("oauth_access_token");
                rs.close();
                ps.close();
                return token;
            }
            rs.close();
            ps.close();

            // Si no tienes tabla de tokens, puedes obtenerlo de tu clase OAuth2:
            // return TuClaseOAuth2.getAccessToken(userId);
        } catch (Exception e) {
            System.err.println("Error obteniendo access token: " + e.getMessage());
        }

        return null;
    }

    private static String obtenerRefreshTokenDelUsuario(int userId) {
        // IMPLEMENTAR: Similar al access token pero para refresh token
        // El refresh token se usa para renovar el access token cuando expira
        return null; // IMPLEMENTAR
    }

    private static String obtenerClientIdDeConfig() {
        // IMPLEMENTAR: Retornar el Client ID de tu aplicación OAuth2
        // Esto debería estar en tu configuración de OAuth2 existente
        return "TU_CLIENT_ID_AQUI"; // CAMBIAR por tu client ID real
    }

    private static String obtenerClientSecretDeConfig() {
        // IMPLEMENTAR: Retornar el Client Secret de tu aplicación OAuth2
        // Esto debería estar en tu configuración de OAuth2 existente
        return "TU_CLIENT_SECRET_AQUI"; // CAMBIAR por tu client secret real
    }

    /**
     * Verifica si hay emails pendientes de configurar
     */
    public static int contarAlumnosSinEmail() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return -1;
            }

            String query = "SELECT COUNT(*) as total FROM usuarios WHERE rol = '4' AND (mail IS NULL OR mail = '')";
            PreparedStatement ps = conect.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            if (rs.next()) {
                count = rs.getInt("total");
            }

            rs.close();
            ps.close();

            return count;

        } catch (SQLException e) {
            System.err.println("Error contando alumnos sin email: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Prueba la conexión SMTP antes del envío real
     */
    public static boolean probarConexionSMTP() {
        try {
            System.out.println("🧪 Probando conexión SMTP...");

            if (!configuracionActual.isCompleta()) {
                System.err.println("❌ Configuración incompleta");
                return false;
            }

            Session session = crearSesionEmail();
            if (session == null) {
                System.err.println("❌ No se pudo crear sesión");
                return false;
            }

            // Crear mensaje de prueba simple
            MimeMessage testMessage = new MimeMessage(session);
            testMessage.setFrom(new InternetAddress(configuracionActual.emailRemitente, configuracionActual.nombreRemitente));
            testMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(configuracionActual.emailRemitente)); // Enviar a sí mismo
            testMessage.setSubject("Prueba de conexión SMTP - " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            testMessage.setText("Esta es una prueba de conexión SMTP.\n\nMétodo: " + (configuracionActual.useOAuth2 ? "OAuth2" : "SMTP tradicional"));

            // Intentar enviar
            System.out.println("📤 Enviando mensaje de prueba...");
            Transport.send(testMessage);

            System.out.println("✅ Conexión SMTP exitosa");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error en conexión SMTP: " + e.getMessage());
            System.err.println("❌ Tipo de error: " + e.getClass().getSimpleName());

            // Análisis específico de errores comunes
            if (e.getMessage().contains("authentication failed") || e.getMessage().contains("Username and Password not accepted")) {
                System.err.println("💡 Problema de autenticación - Revisar credenciales");
                if (configuracionActual.useOAuth2) {
                    System.err.println("💡 Para OAuth2: Verificar que el token sea válido y tenga permisos de envío");
                    System.err.println("💡 Para dominios educativos: Verificar que el administrador haya habilitado el acceso");
                }
            }

            if (e.getMessage().contains("Connection timed out") || e.getMessage().contains("Connection refused")) {
                System.err.println("💡 Problema de conexión - Verificar firewall/proxy");
            }

            if (e.getMessage().contains("XOAUTH2")) {
                System.err.println("💡 Problema específico de OAuth2 - Puede ser token expirado o permisos insuficientes");
            }

            e.printStackTrace();
            return false;
        }
    }

    /**
     * NUEVO: Método para mostrar opciones de configuración
     */
    public static void mostrarOpcionesConfiguracion(javax.swing.JComponent parentComponent, int userId) {
        String[] opciones = {
            "🚀 Configuración Automática (OAuth2 - Recomendado)",
            "⚙️ Configuración Manual (SMTP)",
            "❓ Ayuda"
        };

        int seleccion = javax.swing.JOptionPane.showOptionDialog(
                parentComponent,
                "¿Cómo desea configurar el envío de emails?\n\n"
                + "🚀 Automática: Usa tu cuenta Google actual, más fácil y seguro\n"
                + "⚙️ Manual: Configuración SMTP tradicional, requiere contraseña de app",
                "Configurar Email para Boletines",
                javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        switch (seleccion) {
            case 0: // Configuración automática
                configurarAutomaticamente(parentComponent, userId);
                break;
            case 1: // Configuración manual
                mostrarConfiguracionManual(parentComponent);
                break;
            case 2: // Ayuda
                GoogleEmailIntegration.mostrarAyudaOAuth2();
                break;
            default:
                // Cancelado
                break;
        }
    }

    /**
     * NUEVO: Configuración automática con OAuth2
     */
    private static void configurarAutomaticamente(javax.swing.JComponent parentComponent, int userId) {
        javax.swing.SwingWorker<Boolean, String> worker = new javax.swing.SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Configurando email automáticamente...");
                return GoogleEmailIntegration.configurarEmailAutomaticamente(userId);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    System.out.println(message);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();

                    if (exito) {
                        ConfiguracionEmail config = obtenerConfiguracion();

                        javax.swing.JOptionPane.showMessageDialog(parentComponent,
                                "✅ Email configurado automáticamente\n\n"
                                + "Método: OAuth2 (Google)\n"
                                + "Remitente: " + config.emailRemitente + "\n"
                                + "Nombre: " + config.nombreRemitente + "\n\n"
                                + "Ya puede enviar boletines por email.",
                                "Configuración Exitosa",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        int opcion = javax.swing.JOptionPane.showConfirmDialog(parentComponent,
                                "❌ No se pudo configurar automáticamente\n\n"
                                + "Posibles causas:\n"
                                + "• No hay cuenta Google asociada\n"
                                + "• Problemas de permisos\n"
                                + "• Error de conexión\n\n"
                                + "¿Desea intentar configuración manual?",
                                "Error de Configuración",
                                javax.swing.JOptionPane.YES_NO_OPTION,
                                javax.swing.JOptionPane.WARNING_MESSAGE);

                        if (opcion == javax.swing.JOptionPane.YES_OPTION) {
                            mostrarConfiguracionManual(parentComponent);
                        }
                    }

                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(parentComponent,
                            "Error durante la configuración: " + ex.getMessage(),
                            "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * NUEVO: Mostrar diálogo de configuración manual
     */
    private static void mostrarConfiguracionManual(javax.swing.JComponent parentComponent) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent,
                "📧 CONFIGURACIÓN MANUAL DE EMAIL\n\n"
                + "Para configurar manualmente necesitará:\n\n"
                + "🔧 Servidor SMTP: smtp.gmail.com\n"
                + "🔌 Puerto: 587 (TLS) o 465 (SSL)\n"
                + "📧 Email remitente: su email @gmail.com\n"
                + "🔑 Contraseña de aplicación (NO su contraseña normal)\n"
                + "👤 Nombre del remitente\n\n"
                + "⚠️ IMPORTANTE: Debe generar una contraseña de aplicación\n"
                + "desde su cuenta Google (no use su contraseña normal)\n\n"
                + "Use el botón 'Config Email' para abrir el panel de configuración.",
                "Configuración Manual",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * ACTUALIZAR: Método obtenerEstadisticasEmail
     */
    public static String obtenerEstadisticasEmail() {
        try {
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return "Error de conexión";
            }

            StringBuilder stats = new StringBuilder();
            stats.append("=== ESTADÍSTICAS DE EMAIL ===\n");

            // Total de alumnos
            String queryTotal = "SELECT COUNT(*) as total FROM usuarios WHERE rol = '4'";
            PreparedStatement psTotal = conect.prepareStatement(queryTotal);
            ResultSet rsTotal = psTotal.executeQuery();

            int totalAlumnos = 0;
            if (rsTotal.next()) {
                totalAlumnos = rsTotal.getInt("total");
            }
            rsTotal.close();
            psTotal.close();

            // Alumnos con email
            String queryConEmail = "SELECT COUNT(*) as total FROM usuarios WHERE rol = '4' AND mail IS NOT NULL AND mail != ''";
            PreparedStatement psConEmail = conect.prepareStatement(queryConEmail);
            ResultSet rsConEmail = psConEmail.executeQuery();

            int alumnosConEmail = 0;
            if (rsConEmail.next()) {
                alumnosConEmail = rsConEmail.getInt("total");
            }
            rsConEmail.close();
            psConEmail.close();

            stats.append("Total de alumnos: ").append(totalAlumnos).append("\n");
            stats.append("Alumnos con email: ").append(alumnosConEmail).append("\n");
            stats.append("Alumnos sin email: ").append(totalAlumnos - alumnosConEmail).append("\n");

            if (totalAlumnos > 0) {
                double porcentaje = (alumnosConEmail * 100.0) / totalAlumnos;
                stats.append("Cobertura de email: ").append(String.format("%.1f%%", porcentaje)).append("\n");
            }

            stats.append("\n=== CONFIGURACIÓN ACTUAL ===\n");
            stats.append("SMTP: ").append(configuracionActual.smtpHost).append(":").append(configuracionActual.smtpPort).append("\n");
            stats.append("Remitente: ").append(configuracionActual.nombreRemitente).append("\n");
            stats.append("Email: ").append(configuracionActual.emailRemitente).append("\n");
            stats.append("Configuración completa: ").append(configuracionActual.isCompleta() ? "Sí ✅" : "No ❌").append("\n");

            stats.append("\n=== MÉTODO DE AUTENTICACIÓN ===\n");
            if (configuracionActual.useOAuth2) {
                stats.append("Método: OAuth2 (Google) ✅\n");
                stats.append("Access Token: ").append(configuracionActual.accessToken != null && !configuracionActual.accessToken.isEmpty() ? "Configurado ✅" : "No configurado ❌").append("\n");
                stats.append("Refresh Token: ").append(configuracionActual.refreshToken != null && !configuracionActual.refreshToken.isEmpty() ? "Configurado ✅" : "No configurado ❌").append("\n");
                stats.append("Client ID: ").append(configuracionActual.clientId != null && !configuracionActual.clientId.isEmpty() ? "Configurado ✅" : "No configurado ❌").append("\n");
            } else {
                stats.append("Método: Contraseña de aplicación ⚙️\n");
                stats.append("Contraseña configurada: ").append(configuracionActual.passwordRemitente != null && !configuracionActual.passwordRemitente.isEmpty() ? "Sí ✅" : "No ❌").append("\n");
            }

            return stats.toString();

        } catch (SQLException e) {
            return "Error obteniendo estadísticas: " + e.getMessage();
        }
    }

    /**
     * Método para uso con interfaz gráfica - Envío individual con progreso
     */
    public static void enviarBoletinIndividualConInterfaz(String urlBoletin, DestinatarioEmail destinatario,
            String periodo, String asunto, String mensaje,
            javax.swing.JComponent parentComponent) {
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Configurando envío...");
                return enviarBoletinIndividual(urlBoletin, destinatario, periodo, asunto, mensaje);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    System.out.println(message);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    String mensaje = exito
                            ? "Boletín enviado exitosamente a:\n" + destinatario.emailAlumno
                            : "Error al enviar el boletín.\nVerifique la configuración de email y la conectividad.";

                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje,
                            "Envío de Boletín",
                            exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Error durante el envío: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Método para uso con interfaz gráfica - Envío masivo con progreso
     */
    public static void enviarBoletinesMasivosConInterfaz(List<GestorBoletines.InfoBoletin> boletines,
            String asuntoPersonalizado, String mensajePersonalizado,
            javax.swing.JComponent parentComponent) {
        // Crear ventana de progreso
        javax.swing.JDialog progressDialog = new javax.swing.JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(parentComponent),
                "Enviando Boletines por Email", true);

        javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Preparando envío...");

        progressDialog.setLayout(new java.awt.BorderLayout());
        progressDialog.add(new javax.swing.JLabel("Enviando boletines por email...",
                javax.swing.SwingConstants.CENTER), java.awt.BorderLayout.NORTH);
        progressDialog.add(progressBar, java.awt.BorderLayout.CENTER);
        progressDialog.setSize(400, 100);
        progressDialog.setLocationRelativeTo(parentComponent);

        SwingWorker<ResultadoEnvio, String> worker = new SwingWorker<ResultadoEnvio, String>() {
            @Override
            protected ResultadoEnvio doInBackground() throws Exception {
                // Obtener destinatarios
                publish("Obteniendo destinatarios...");
                List<DestinatarioEmail> destinatarios = obtenerDestinatariosDesdeBoletines(boletines);

                if (destinatarios.isEmpty()) {
                    ResultadoEnvio resultado = new ResultadoEnvio();
                    resultado.exitoso = false;
                    resultado.mensaje = "No se encontraron destinatarios con emails válidos";
                    return resultado;
                }

                ResultadoEnvio resultado = new ResultadoEnvio();

                for (int i = 0; i < destinatarios.size(); i++) {
                    DestinatarioEmail destinatario = destinatarios.get(i);

                    publish("Enviando " + (i + 1) + "/" + destinatarios.size() + ": " + destinatario.nombreAlumno);
                    setProgress((i * 100) / destinatarios.size());

                    // Buscar boletín correspondiente
                    GestorBoletines.InfoBoletin boletinCorrespondiente = null;
                    for (GestorBoletines.InfoBoletin boletin : boletines) {
                        if (boletin.alumnoId == destinatario.alumnoId) {
                            boletinCorrespondiente = boletin;
                            break;
                        }
                    }

                    if (boletinCorrespondiente != null) {
                        try {
                            boolean enviado = enviarBoletinIndividual(boletinCorrespondiente.rutaArchivo,
                                    destinatario, boletinCorrespondiente.periodo,
                                    asuntoPersonalizado, mensajePersonalizado);

                            if (enviado) {
                                resultado.totalEnviados++;
                                publish("✅ Enviado a: " + destinatario.nombreAlumno);
                            } else {
                                resultado.totalErrores++;
                                resultado.erroresDetallados.add("Error enviando a: " + destinatario.nombreAlumno);
                                publish("❌ Error con: " + destinatario.nombreAlumno);
                            }

                            // Pausa entre envíos
                            Thread.sleep(1500);

                        } catch (Exception e) {
                            resultado.totalErrores++;
                            resultado.erroresDetallados.add("Error con " + destinatario.nombreAlumno + ": " + e.getMessage());
                            publish("❌ Error con: " + destinatario.nombreAlumno);
                        }
                    } else {
                        resultado.totalErrores++;
                        resultado.erroresDetallados.add("No se encontró boletín para: " + destinatario.nombreAlumno);
                    }
                }

                setProgress(100);
                resultado.exitoso = resultado.totalEnviados > 0;
                resultado.mensaje = String.format("Enviados: %d, Errores: %d",
                        resultado.totalEnviados, resultado.totalErrores);

                return resultado;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    progressBar.setString(message);
                    System.out.println(message);
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    ResultadoEnvio resultado = get();

                    StringBuilder mensaje = new StringBuilder();
                    mensaje.append("Envío masivo completado:\n\n");
                    mensaje.append("✅ Enviados exitosamente: ").append(resultado.totalEnviados).append("\n");
                    mensaje.append("❌ Errores: ").append(resultado.totalErrores).append("\n");

                    if (!resultado.erroresDetallados.isEmpty() && resultado.erroresDetallados.size() <= 5) {
                        mensaje.append("\nErrores:\n");
                        for (String error : resultado.erroresDetallados) {
                            mensaje.append("• ").append(error).append("\n");
                        }
                    } else if (resultado.erroresDetallados.size() > 5) {
                        mensaje.append("\n").append(resultado.erroresDetallados.size()).append(" errores detectados.");
                        mensaje.append("\nConsulte la consola para más detalles.");
                    }

                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje.toString(),
                            "Envío Masivo Completado",
                            resultado.exitoso ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Error durante el envío masivo: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Configurar progreso
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
        progressDialog.setVisible(true);
    }

    /**
     * Valida una dirección de email
     */
    public static boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(regex);
    }

    /**
     * Limpia archivos temporales de boletines
     */
    public static void limpiarArchivosTemporales() {
        try {
            String directorioTemp = System.getProperty("java.io.tmpdir");
            java.io.File carpetaTemp = new java.io.File(directorioTemp);

            java.io.File[] archivos = carpetaTemp.listFiles((dir, name)
                    -> name.startsWith("boletin_temp_") && name.endsWith(".xlsx"));

            int archivosEliminados = 0;
            if (archivos != null) {
                for (java.io.File archivo : archivos) {
                    if (archivo.delete()) {
                        archivosEliminados++;
                    }
                }
            }

            System.out.println("✅ Archivos temporales de boletines eliminados: " + archivosEliminados);

        } catch (Exception e) {
            System.err.println("Error limpiando archivos temporales: " + e.getMessage());
        }
    }

    /**
     * Obtiene información de debug del sistema de email
     */
    public static String obtenerInformacionDebug() {
        StringBuilder info = new StringBuilder();
        info.append("=== DEBUG SISTEMA DE EMAIL ===\n");
        info.append("Configuración completa: ").append(configuracionActual.isCompleta()).append("\n");
        info.append("SMTP Host: ").append(configuracionActual.smtpHost).append("\n");
        info.append("SMTP Port: ").append(configuracionActual.smtpPort).append("\n");
        info.append("Use TLS: ").append(configuracionActual.useTLS).append("\n");
        info.append("Use Auth: ").append(configuracionActual.useAuth).append("\n");
        info.append("Email remitente configurado: ").append(configuracionActual.emailRemitente != null && !configuracionActual.emailRemitente.isEmpty()).append("\n");
        info.append("Password configurado: ").append(configuracionActual.passwordRemitente != null && !configuracionActual.passwordRemitente.isEmpty()).append("\n");
        info.append("Nombre remitente: ").append(configuracionActual.nombreRemitente).append("\n");

        // Información del sistema
        info.append("\nSistema:\n");
        info.append("Java Mail API disponible: ");
        try {
            Class.forName("javax.mail.Session");
            info.append("Sí\n");
        } catch (ClassNotFoundException e) {
            info.append("No\n");
        }

        info.append("Directorio temporal: ").append(System.getProperty("java.io.tmpdir")).append("\n");

        return info.toString();
    }

    public static boolean verificarLibreriasEmail() {
        try {
            // Verificar JavaMail
            Class.forName("javax.mail.Session");
            Class.forName("javax.mail.Transport");
            Class.forName("javax.mail.internet.MimeMessage");

            // Verificar Activation
            Class.forName("javax.activation.DataHandler");

            System.out.println("✅ Librerías de email verificadas correctamente");
            return true;

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Falta librería de email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Habilita debug detallado de JavaMail para diagnosticar problemas
     */
    public static void habilitarDebugEmail(boolean enable) {
        if (enable) {
            System.setProperty("mail.debug", "true");
            System.setProperty("mail.socket.debug", "true");
            System.out.println("🐛 Debug de JavaMail habilitado");
        } else {
            System.clearProperty("mail.debug");
            System.clearProperty("mail.socket.debug");
            System.out.println("🐛 Debug de JavaMail deshabilitado");
        }
    }

// 5. MÉTODO TEMPORAL: Fallback a configuración manual si OAuth2 falla
    /**
     * Fallback: Configurar manualmente si OAuth2 no funciona
     */
    public static void configurarFallbackManual(String email, String password) {
        System.out.println("⚠️ Configurando fallback manual para: " + email);

        configuracionActual.emailRemitente = email;
        configuracionActual.passwordRemitente = password;
        configuracionActual.nombreRemitente = "Escuela Técnica N° 20";
        configuracionActual.smtpHost = "smtp.gmail.com";
        configuracionActual.smtpPort = "587";
        configuracionActual.useTLS = true;
        configuracionActual.useAuth = true;
        configuracionActual.useOAuth2 = false; // IMPORTANTE: Deshabilitar OAuth2

        System.out.println("✅ Configuración manual establecida (SMTP tradicional)");
    }
}
