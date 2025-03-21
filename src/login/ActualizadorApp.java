package login;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActualizadorApp {
    // Versión actual de la aplicación
    private static final String VERSION_ACTUAL = "";
    
    // Ruta base del servidor (red local)
    private static final String SERVER_BASE_URL = "http://192.168.1.100/actualizaciones/";
    
    // Nombre del archivo de manifiesto de versión
    private static final String VERSION_FILE = "version.json";
    
    // Nombre del archivo ejecutable de la aplicación
    private static final String APP_EXE_NAME = "";
    
    // Directorio temporal para descargas
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    // Método principal para verificar actualizaciones
    public static void verificarActualizaciones() {
        try {
            // Comprobar si hay una actualización disponible
            VersionInfo nuevaVersion = obtenerUltimaVersion();
            
            if (nuevaVersion != null && esVersionMayor(nuevaVersion.getVersion(), VERSION_ACTUAL)) {
                // Mostrar diálogo de actualización
                boolean actualizarAhora = mostrarDialogoActualizacion(nuevaVersion);
                
                if (actualizarAhora) {
                    // Descargar e instalar la actualización
                    descargarEInstalarActualizacion(nuevaVersion);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al verificar actualizaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static VersionInfo obtenerUltimaVersion() throws IOException {
        URL url = new URL(SERVER_BASE_URL + VERSION_FILE);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            
            // Parsear el archivo JSON
            String jsonContent = reader.lines().collect(Collectors.joining());
            
            // Usar un parser JSON simple para este ejemplo
            String version = extraerValorJson(jsonContent, "version");
            String downloadUrl = extraerValorJson(jsonContent, "downloadUrl");
            String[] cambios = extraerArrayJson(jsonContent, "cambios");
            boolean requiereReinicio = Boolean.parseBoolean(extraerValorJson(jsonContent, "requiereReinicio"));
            
            return new VersionInfo(version, downloadUrl, cambios, requiereReinicio);
        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor de actualizaciones: " + e.getMessage());
            return null;
        }
    }
    
    private static boolean esVersionMayor(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        
        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);
            
            if (num1 > num2) {
                return true;
            } else if (num1 < num2) {
                return false;
            }
        }
        
        // Si llegamos aquí, los números coinciden hasta el punto de comparación
        return parts1.length > parts2.length;
    }
    
    private static boolean mostrarDialogoActualizacion(VersionInfo nuevaVersion) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Hay una nueva versión disponible: ").append(nuevaVersion.getVersion()).append("\n\n");
        mensaje.append("Cambios en esta versión:\n");
        
        for (String cambio : nuevaVersion.getCambios()) {
            mensaje.append("• ").append(cambio).append("\n");
        }
        
        mensaje.append("\n¿Desea actualizar ahora?");
        
        int opcion = JOptionPane.showConfirmDialog(
            null,
            mensaje.toString(),
            "Actualización disponible",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        return opcion == JOptionPane.YES_OPTION;
    }
    
    private static void descargarEInstalarActualizacion(VersionInfo nuevaVersion) {
        try {
            // Mostrar diálogo de progreso
            JDialog progressDialog = new JDialog((JFrame) null, "Actualizando...", true);
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setString("Descargando actualización...");
            
            progressDialog.setLayout(new BorderLayout());
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(300, 80);
            progressDialog.setLocationRelativeTo(null);
            
            // Iniciar descarga en un hilo separado
            SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Ruta de la descarga
                    String downloadPath = TEMP_DIR + "/SistemaGestionEscolar_" + nuevaVersion.getVersion() + ".exe";
                    String appPath = getAppPath();
                    
                    // Descargar archivo
                    try (BufferedInputStream in = new BufferedInputStream(new URL(nuevaVersion.getDownloadUrl()).openStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(downloadPath)) {
                        
                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        long totalBytes = 0;
                        int contentLength = getContentLength(nuevaVersion.getDownloadUrl());
                        
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                            totalBytes += bytesRead;
                            
                            // Actualizar progreso
                            if (contentLength > 0) {
                                int progress = (int) ((totalBytes * 100) / contentLength);
                                publish(progress);
                            }
                        }
                    }
                    
                    // Crear script de instalación
                    createUpdateScript(downloadPath, appPath, nuevaVersion.isRequiereReinicio());
                    
                    return true;
                }
                
                @Override
                protected void process(List<Integer> chunks) {
                    // Actualizar barra de progreso
                    int lastProgress = chunks.get(chunks.size() - 1);
                    progressBar.setValue(lastProgress);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        progressDialog.dispose();
                        
                        if (success) {
                            // Ejecutar script de actualización
                            executeUpdateScript();
                        } else {
                            JOptionPane.showMessageDialog(null, 
                                "Error al descargar la actualización. Intente más tarde.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(null, 
                            "Error durante la actualización: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true); // Esto bloqueará hasta que worker.done() cierre el diálogo
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error al intentar actualizar: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static int getContentLength(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            return connection.getContentLength();
        } catch (Exception e) {
            return -1;
        }
    }
    
    private static String getAppPath() {
        try {
            // Para aplicaciones empaquetadas como .exe, necesitamos obtener la ruta del ejecutable actual
            return new File(System.getProperty("user.dir") + File.separator + APP_EXE_NAME).getPath();
        } catch (Exception e) {
            return "";
        }
    }
    
    private static void createUpdateScript(String downloadPath, String appPath, boolean requiereReinicio) throws IOException {
        // Para Windows y ejecutables .exe
        String scriptPath = TEMP_DIR + "/update.bat";
        
        try (FileWriter writer = new FileWriter(scriptPath)) {
            writer.write("@echo off\n");
            writer.write("echo Esperando que la aplicación se cierre...\n");
            writer.write("timeout /t 3 /nobreak > nul\n"); // Pausa de 3 segundos
            
            // Intenta primero con un reemplazo directo
            writer.write("echo Intentando actualizar la aplicación...\n");
            writer.write("copy /Y \"" + downloadPath + "\" \"" + appPath + "\"\n");
            
            // Si falla el reemplazo directo (por ejemplo, por permisos), intenta con un ejecutable de instalación
            writer.write("if %ERRORLEVEL% NEQ 0 (\n");
            writer.write("    echo El reemplazo directo falló, ejecutando instalador...\n");
            writer.write("    start \"\" \"" + downloadPath + "\"\n");
            writer.write(") else (\n");
            writer.write("    echo Actualización completada exitosamente!\n");
            
            // Si se requiere reinicio y el reemplazo fue exitoso
            if (requiereReinicio) {
                writer.write("    echo Reiniciando aplicación...\n");
                writer.write("    start \"\" \"" + appPath + "\"\n");
            }
            
            writer.write(")\n");
            
            // Limpieza
            writer.write("echo Limpiando archivos temporales...\n");
            writer.write("timeout /t 2 /nobreak > nul\n");
            writer.write("del \"" + downloadPath + "\"\n");
            writer.write("del \"%~f0\"\n"); // Auto-eliminar script
        }
    }
    
    private static void executeUpdateScript() {
        try {
            String scriptPath = TEMP_DIR + "/update.bat";
            
            // Usar ProcessBuilder para ejecutar el script en una nueva ventana
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/c", scriptPath);
            pb.start();
            
            // Cerrar la aplicación
            System.exit(0);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al ejecutar la actualización: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Método simple para extraer valores de JSON (en una implementación real, usa una biblioteca JSON)
    private static String extraerValorJson(String json, String clave) {
        String buscar = "\"" + clave + "\"\\s*:\\s*\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(buscar);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Para valores booleanos
        buscar = "\"" + clave + "\"\\s*:\\s*(true|false)";
        pattern = Pattern.compile(buscar);
        matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    private static String[] extraerArrayJson(String json, String clave) {
        String buscar = "\"" + clave + "\"\\s*:\\s*\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(buscar, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String contenido = matcher.group(1).trim();
            String[] elementos = contenido.split(",");
            for (int i = 0; i < elementos.length; i++) {
                elementos[i] = elementos[i].trim().replaceAll("\"", "");
            }
            return elementos;
        }
        return new String[0];
    }
}