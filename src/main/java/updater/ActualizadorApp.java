package main.java.updater;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActualizadorApp {
    // Versión actual de la aplicación
    public static final String VERSION_ACTUAL = "1.2.0";
    
    // Ruta base del servidor (red local)
    private static final String SERVER_BASE_URL = "http://10.120.1.109/miet20/actualizaciones/";
    
    // Nombre del archivo de manifiesto de versión
    private static final String VERSION_FILE = "version.json";
    
    // Nombre del archivo ejecutable de la aplicación
    private static final String APP_EXE_NAME = "MiET20.EXE";
    
    // Directorio temporal para descargas
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    
    private static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + "ET20Plataforma";
    
    
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
        } else {
            JOptionPane.showMessageDialog(
                null,
                "Tu aplicación está actualizada (Versión " + VERSION_ACTUAL + ")",
                "No hay actualizaciones disponibles",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            null, 
            "Error al verificar actualizaciones: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
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
    mensaje.append("Hay una nueva versión disponible:\n");
    mensaje.append("Versión actual: ").append(VERSION_ACTUAL).append("\n");
    mensaje.append("Nueva versión: ").append(nuevaVersion.getVersion()).append("\n\n");
    mensaje.append("Cambios en esta versión:\n");
    
    for (String cambio : nuevaVersion.getCambios()) {
        mensaje.append("• ").append(cambio).append("\n");
    }
    
    mensaje.append("\n¿Desea descargar e instalar esta actualización ahora?");
    
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
        // Obtener la ruta donde se ejecuta la aplicación
        String appPath = getAppPath();
        System.out.println("Ruta de la aplicación: " + appPath);
        
        // Determinar el nombre del archivo JAR original
        File appFile = new File(appPath);
        String originalFileName = appFile.getName();
        
        // Definir dónde se guardará la descarga temporal
        String tempDownloadPath = System.getProperty("java.io.tmpdir") + 
                              File.separator + originalFileName;
        
        System.out.println("URL de descarga: " + nuevaVersion.getDownloadUrl());
        System.out.println("Guardando temporalmente en: " + tempDownloadPath);
        
        // Mostrar un diálogo de progreso
        JDialog progressDialog = new JDialog((JFrame) null, "Descargando actualización...", true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Preparando descarga...");
        progressBar.setForeground(Color.GREEN);
        
        JLabel statusLabel = new JLabel("Conectando con el servidor...", SwingConstants.CENTER);
        
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.add(statusLabel, BorderLayout.SOUTH);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(null);
        
        // Worker para manejar la descarga
        final SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                URL url = new URL(nuevaVersion.getDownloadUrl());
                
                // Abrir conexión con timeout más largo
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000); // 15 segundos
                connection.setReadTimeout(30000);    // 30 segundos
                
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("El servidor respondió con código: " + responseCode);
                }
                
                // Obtener tamaño del archivo
                int contentLength = connection.getContentLength();
                
                // Crear directorio de destino si no existe
                File outputFile = new File(tempDownloadPath);
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                
                // Descargar archivo
                try (InputStream in = connection.getInputStream();
                     BufferedInputStream bis = new BufferedInputStream(in);
                     FileOutputStream fos = new FileOutputStream(outputFile)) {
                    
                    byte[] buffer = new byte[8192]; // Buffer más grande para mejor rendimiento
                    int bytesRead;
                    long totalBytesRead = 0;
                    
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        
                        // Actualizar progreso
                        if (contentLength > 0) {
                            int progress = (int) ((totalBytesRead * 100) / contentLength);
                            publish(progress);
                        }
                    }
                    
                    return true;
                }
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
                progressBar.setString(progress + "% completado");
                statusLabel.setText("Descargando actualización...");
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    Boolean success = get();
                    
                    if (success) {
                        int option = JOptionPane.showConfirmDialog(null,
                            "La actualización se ha descargado correctamente.\n\n" +
                            "¿Desea instalar esta actualización ahora?\n" +
                            "(La aplicación se cerrará durante el proceso)",
                            "Descarga completada",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                            
                        if (option == JOptionPane.YES_OPTION) {
                            // Crear e iniciar el proceso de actualización con privilegios elevados
                            createPowerShellUpdateScript(tempDownloadPath, appPath, true);
                            executeUpdateScript();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "No se pudo completar la descarga de la actualización.",
                            "Error de descarga",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    String errorMsg = "Error durante la descarga: " + e.getMessage();
                    System.err.println(errorMsg);
                    e.printStackTrace();
                    
                    JOptionPane.showMessageDialog(null, 
                        errorMsg + "\n\nDetalles técnicos:\n" + e.getClass().getName(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        // Iniciar la descarga
        worker.execute();
        progressDialog.setVisible(true);
        
    } catch (Exception e) {
        String errorMsg = "Error al iniciar la actualización: " + e.getMessage();
        System.err.println(errorMsg);
        e.printStackTrace();
        
        JOptionPane.showMessageDialog(null, 
            errorMsg,
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
    // Ruta típica de instalación de jpackage
    String defaultPath = "C:\\Program Files\\MiET20\\app\\et20-plataforma.jar";
    
    // Verificar si el archivo existe en la ruta típica
    File defaultFile = new File(defaultPath);
    if (defaultFile.exists()) {
        System.out.println("Aplicación encontrada en ruta de instalación predeterminada: " + defaultPath);
        return defaultPath;
    }
    
    // Si no existe en la ruta predeterminada, intentar detectar dinámicamente
    try {
        String jarPath = ActualizadorApp.class.getProtectionDomain()
                                       .getCodeSource()
                                       .getLocation()
                                       .toURI()
                                       .getPath();
        
        // Convertir a ruta Windows
        jarPath = jarPath.replace("/", "\\");
        if (jarPath.startsWith("\\")) {
            jarPath = jarPath.substring(1);
        }
        
        System.out.println("Aplicación detectada en: " + jarPath);
        return jarPath;
    } catch (Exception e) {
        System.err.println("Error al detectar ruta de aplicación, usando ruta por defecto: " + e.getMessage());
        return defaultPath;
    }
}

private static void createPowerShellUpdateScript(String downloadPath, String appPath, boolean requiereReinicio) throws IOException {
    String scriptPath = System.getProperty("java.io.tmpdir") + File.separator + "update_et20.ps1";

    try (FileWriter writer = new FileWriter(scriptPath)) {
        writer.write("$source = \"" + downloadPath + "\"\n");
        writer.write("$destination = \"" + appPath + "\"\n");
        writer.write("Write-Host \"Copiando archivo...\"\n");
        writer.write("Copy-Item -Path $source -Destination $destination -Force\n");

        if (requiereReinicio) {
            writer.write("Start-Sleep -Seconds 3\n");
            writer.write("Start-Process \"" + appPath + "\"\n");
        }

        writer.write("Remove-Item $source -Force\n");
        writer.write("Write-Host \"Actualización completada.\"\n");
    }

    System.out.println("Script PowerShell creado en: " + scriptPath);
}


private static void executeUpdateScript() {
    try {
        String scriptPath = System.getProperty("java.io.tmpdir") + File.separator + "update_et20.ps1";

        JOptionPane.showMessageDialog(null,
            "La aplicación se cerrará para instalar la actualización.\n" +
            "Se solicitarán permisos de administrador.",
            "Actualización",
            JOptionPane.INFORMATION_MESSAGE);

        // Ejecutar PowerShell con permisos de administrador
        String powershellPath = "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";
new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + powershellPath + "\"", "-WindowStyle", "Hidden",
        "-Command", "Start-Process", "powershell",
        "-ArgumentList", "'-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File \"" + scriptPath + "\"'",
        "-Verb", "RunAs")
        .start();

        // Salir de la aplicación actual
        System.exit(0);

    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error al ejecutar actualización: " + e.getMessage());
    }
}

    
    
    
    
    
    public static void verificarActualizacionesManual() {
    verificarActualizaciones();
}
    
   private static void createUpdateScript(String downloadPath, String appPath, boolean requiereReinicio) throws IOException {
    String scriptPath = TEMP_DIR + "/update.bat";
    
    try (FileWriter writer = new FileWriter(scriptPath)) {
        writer.write("@echo off\n");
        writer.write("echo Verificando permisos de administrador...\n");
        
        // Solicitar permisos de administrador
        writer.write(":checkAdmin\n");
        writer.write("NET SESSION >nul 2>&1\n");
        writer.write("IF %ERRORLEVEL% EQU 0 (\n");
        writer.write("    echo Ejecutando con permisos de administrador\n");
        writer.write(") ELSE (\n");
        writer.write("    echo Solicitando permisos de administrador...\n");
        writer.write("    powershell -Command \"Start-Process -FilePath '%~f0' -ArgumentList 'ELEVATED' -Verb RunAs\"\n");
        writer.write("    exit /b\n");
        writer.write(")\n\n");
        
        // Si se inicia con el argumento ELEVATED, continuar con la actualización
        writer.write("IF \"%1\"==\"ELEVATED\" goto :continue\n\n");
        
        writer.write(":continue\n");
        writer.write("echo Esperando que la aplicación se cierre...\n");
        writer.write("timeout /t 3 /nobreak > nul\n");
        
        // Reemplazar el archivo JAR
        writer.write("echo Actualizando archivos de la aplicación...\n");
        writer.write("copy /Y \"" + downloadPath + "\" \"" + appPath + "\" > %TEMP%\\update_log.txt 2>&1\n");
        writer.write("echo Resultado: %ERRORLEVEL% >> %TEMP%\\update_log.txt\n");
        
        // Si se requiere reinicio
        if (requiereReinicio) {
            writer.write("echo Reiniciando aplicación...\n");
            writer.write("start \"\" \"" + appPath + "\"\n");
        }
        
        // Limpieza
        writer.write("echo Limpiando archivos temporales...\n");
        writer.write("del \"" + downloadPath + "\"\n");
        writer.write("del \"%~f0\"\n");
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
    
    private static void guardarRutaInstalacion(String rutaInstalacion) {
    try {
        File configFile = new File(System.getProperty("user.home") + File.separator + ".et20config");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("installPath=" + rutaInstalacion);
        }
    } catch (IOException e) {
        System.err.println("Error al guardar configuración: " + e.getMessage());
    }
}

private static String obtenerRutaInstalacion() {
    try {
        File configFile = new File(System.getProperty("user.home") + File.separator + ".et20config");
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line = reader.readLine();
                if (line != null && line.startsWith("installPath=")) {
                    return line.substring("installPath=".length());
                }
            }
        }
    } catch (IOException e) {
        System.err.println("Error al leer configuración: " + e.getMessage());
    }
    return getAppPath(); // Método de respaldo
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