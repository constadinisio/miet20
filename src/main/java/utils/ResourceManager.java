package main.java.utils;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Utilidad para gestionar recursos locales de la aplicación.
 */
public class ResourceManager {

    // Directorio base de la aplicación (donde está instalada)
    private static Path appBasePath;

    // Carpeta de imágenes
    private static Path imagesPath;

    // URL base del servidor
    private static final String SERVER_URL = "http://10.120.1.109/miet20/resources/"; // Cambia esto a tu servidor

    // Lista de imágenes requeridas
    private static final Map<String, String> requiredImages = new HashMap<>();

    // Flag para indicar si está inicializado
    private static boolean initialized = false;

    // Inicializar la lista de imágenes requeridas
    static {
        // Formato: nombre de archivo, ruta relativa en el servidor
        requiredImages.put("logo_et20_min.png", "images/logo_et20_min.png");
        requiredImages.put("logo_et20_max.png", "images/logo_et20_max.png");
        requiredImages.put("banner-et20.png", "images/banner-et20.png");
        requiredImages.put("5c994f25d361a_1200.jpg", "images/5c994f25d361a_1200.jpg");
        requiredImages.put("loogout48.png", "images/loogout48.png");
        requiredImages.put("icons8-user-96.png", "images/icons8-user-96.png");
        // Añade todas las imágenes que necesites
    }

    /**
     * Inicializa el ResourceManager, detectando la ubicación de la aplicación y
     * preparando las carpetas necesarias.
     *
     * @return true si la inicialización fue exitosa
     */
    /**
     * Inicializa el ResourceManager, detectando la ubicación de la aplicación y
     * preparando las carpetas necesarias.
     *
     * @return true si la inicialización fue exitosa
     */
    public static boolean initialize() {
        if (initialized) {
            return true; // Ya inicializado
        }

        try {
            System.out.println("Inicializando ResourceManager...");

            detectAppPath();
            System.out.println("Ruta de la aplicación detectada: " + appBasePath.toString());

            // Intentar crear la carpeta de imágenes si no existe
            createImagesFolder();

            System.out.println("Carpeta de imágenes: " + imagesPath.toString());

            // 1. Primero verificar si TODAS las imágenes ya están
            if (checkAllImagesExist()) {
                System.out.println("Todas las imágenes requeridas ya existen. No es necesario descargar.");
                initialized = true;
                return true;
            }

            // 2. Si faltan imágenes, verificar permisos
            if (!isDirectoryWritable(appBasePath)) {
                System.out.println("ADVERTENCIA: No hay permisos de escritura en la ruta de la aplicación.");

                boolean restarted = requestAdminPrivileges();
                if (!restarted) {
                    JOptionPane.showMessageDialog(null,
                            "La aplicación necesita permisos de administrador para instalar recursos.\n"
                            + "La aplicación se cerrará ahora.",
                            "Permisos insuficientes",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                return false;
            }

            // 3. Descargar imágenes faltantes
            boolean imagesReady = checkAndDownloadImages();

            if (imagesReady) {
                JOptionPane.showMessageDialog(null,
                        "Recursos instalados correctamente.\nLa aplicación continuará normalmente.",
                        "Instalación completada",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            initialized = true;
            return imagesReady;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error crítico al inicializar recursos: " + e.getMessage());

            JOptionPane.showMessageDialog(null,
                    "Error al inicializar recursos: " + e.getMessage()
                    + "\nLa aplicación necesita permisos de administrador para funcionar correctamente.",
                    "Error", JOptionPane.ERROR_MESSAGE);

            requestAdminPrivileges();
            initialized = false;
            return false;
        }
    }

    private static boolean checkAllImagesExist() {
        for (String fileName : requiredImages.keySet()) {
            Path imagePath = imagesPath.resolve(fileName);
            if (!Files.exists(imagePath)) {
                return false; // Si falta al menos una imagen, devolvemos false
            }
        }
        return true; // Todas las imágenes están
    }

    /**
     * Solicita permisos de administrador reiniciando la aplicación elevada.
     *
     * @return true si la aplicación se reinició con éxito
     */
    private static boolean requestAdminPrivileges() {
        try {
            int response = JOptionPane.showConfirmDialog(null,
                    "La aplicación necesita permisos de administrador para crear y descargar recursos.\n"
                    + "¿Desea continuar con la instalación de recursos?",
                    "Permisos requeridos",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response != JOptionPane.YES_OPTION) {
                return false;
            }

            // Ruta completa donde debe crearse la carpeta de imágenes
            String imagesDir = appBasePath.resolve("images").toString();

            // Crear archivo de marcador para indicar que la instalación se completó
            String markerCompletedPath = System.getProperty("java.io.tmpdir") + File.separator + "et20_install_completed.txt";

            // Crear script PowerShell
            String scriptPath = System.getProperty("java.io.tmpdir") + File.separator + "install_resources.ps1";

            try (FileWriter writer = new FileWriter(scriptPath)) {
                writer.write("# Script para instalar recursos con permisos elevados\n");
                writer.write("Write-Host \"Creando carpeta de imágenes en: " + imagesDir + "\"\n");
                writer.write("New-Item -Path \"" + imagesDir + "\" -ItemType Directory -Force\n\n");

                // Descargar imágenes
                writer.write("# Descargando imágenes\n");
                for (Map.Entry<String, String> entry : requiredImages.entrySet()) {
                    String fileName = entry.getKey();
                    String serverPath = entry.getValue();
                    String imageUrl = SERVER_URL + serverPath;
                    String localPath = imagesDir + "\\" + fileName;

                    writer.write("Write-Host \"Descargando " + fileName + "...\"\n");
                    writer.write("try {\n");
                    writer.write("    Invoke-WebRequest -Uri \"" + imageUrl + "\" -OutFile \"" + localPath + "\"\n");
                    writer.write("    Write-Host \"  - Descarga completada: " + fileName + "\"\n");
                    writer.write("} catch {\n");
                    writer.write("    Write-Host \"  - Error al descargar: " + fileName + " - $_\"\n");
                    writer.write("}\n\n");
                }

                // Crear marcador de finalización
                writer.write("# Crear archivo de marcador de finalización\n");
                writer.write("Set-Content -Path \"" + markerCompletedPath + "\" -Value \"completed\"\n");
                writer.write("Write-Host \"Instalación completada.\"\n");
            }

            // Intentar ejecutar PowerShell en varias ubicaciones posibles
            boolean success = false;
            Exception lastException = null;
            String[] possiblePaths = {
                "powershell.exe",
                "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe",
                "C:\\Windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe"
            };

            for (String psPath : possiblePaths) {
                try {
                    String command = "\"" + psPath + "\" -Command \"Start-Process PowerShell -WindowStyle Hidden -ArgumentList '-ExecutionPolicy Bypass -WindowStyle Hidden -File \"\""
                            + scriptPath + "\"\"' -Verb RunAs\"";

                    System.out.println("Intentando ejecutar: " + command);
                    Process process = Runtime.getRuntime().exec(command);

                    Thread.sleep(1000); // Esperar un segundo para ver si arranca

                    try {
                        int exitValue = process.exitValue();
                        System.out.println("El proceso terminó inmediatamente con código: " + exitValue);
                    } catch (IllegalThreadStateException e) {
                        // El proceso sigue ejecutándose: OK
                        success = true;
                        break;
                    }

                } catch (Exception e) {
                    System.out.println("Error usando " + psPath + ": " + e.getMessage());
                    lastException = e;
                }
            }

            if (!success) {
                throw lastException != null ? lastException : new IOException("No se pudo encontrar PowerShell para ejecutar el script.");
            }

            // Mostrar ventana de espera mientras se instala
            JDialog waitDialog = new JDialog((Frame) null, "Instalando recursos...", true);
            waitDialog.setLayout(new BorderLayout());
            JLabel msgLabel = new JLabel("Instalando recursos con permisos de administrador...");
            msgLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            waitDialog.add(msgLabel, BorderLayout.CENTER);
            waitDialog.setSize(300, 100);
            waitDialog.setLocationRelativeTo(null);

            AtomicBoolean completed = new AtomicBoolean(false); // Usamos AtomicBoolean ahora

            Thread waitThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < 30000) { // 30 segundos timeout
                    File completedMarker = new File(markerCompletedPath);
                    if (completedMarker.exists()) {
                        completed.set(true);
                        completedMarker.delete();
                        break;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    waitDialog.dispose();

                    if (completed.get()) {
                        JOptionPane.showMessageDialog(null,
                                "Los recursos se han instalado correctamente.\n"
                                + "La aplicación continuará normalmente.",
                                "Instalación completada",
                                JOptionPane.INFORMATION_MESSAGE);

                        imagesPath = appBasePath.resolve("images");
                        initialized = true;
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "La instalación de recursos está en progreso o ha fallado.\n"
                                + "La aplicación intentará continuar.\n\n"
                                + "Si persisten los problemas, intente ejecutar la aplicación como administrador.",
                                "Instalación en progreso",
                                JOptionPane.WARNING_MESSAGE);
                    }
                });
            });

            waitThread.start();
            waitDialog.setVisible(true); // Bloquea hasta que waitThread cierre el diálogo

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al solicitar permisos: " + e.getMessage()
                    + "\n\nPor favor, intente ejecutar la aplicación como administrador manualmente.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Obtiene la ruta completa al archivo JAR de la aplicación.
     */
    private static String getApplicationPath() {
        try {
            // Intentar obtener la ubicación del JAR
            String path = ResourceManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();

            // Decodificar la URL
            path = java.net.URLDecoder.decode(path, "UTF-8");

            // Ajustar la ruta para Windows
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            path = path.replace("/", File.separator);

            // Si es un directorio (en desarrollo), buscar el jar main
            File file = new File(path);
            if (file.isDirectory()) {
                // En desarrollo, retornar la ruta a la clase principal
                return System.getProperty("java.class.path");
            }

            return path;
        } catch (Exception e) {
            System.err.println("Error al obtener ruta de aplicación: " + e.getMessage());

            // Como alternativa, usar el classpath
            return System.getProperty("java.class.path");
        }
    }

    private static File createElevationScript(String javaBin, String jarPath) throws IOException {
        File vbsFile = File.createTempFile("elevate", ".vbs");
        vbsFile.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(new FileWriter(vbsFile))) {
            writer.println("Set UAC = CreateObject(\"Shell.Application\")");
            writer.println("UAC.ShellExecute \"" + javaBin.replace("\\", "\\\\")
                    + "\", \"-jar \"\"" + jarPath.replace("\\", "\\\\") + "\"\"\""
                    + ", \"\", \"runas\", 1");
        }

        return vbsFile;
    }

    private static void detectAppPath() {
        try {
            File jarFile = new File(ResourceManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            System.out.println("Ruta detectada inicial: " + jarFile.getAbsolutePath());

            if (jarFile.isFile()) {
                // Estamos ejecutando un .jar
                appBasePath = jarFile.getParentFile().toPath();
                // No subimos dos niveles automáticamente, verifiquemos la estructura primero
                System.out.println("Detectada ruta de JAR: " + appBasePath);

                // Si hay una carpeta /app/ en la ruta, subimos un nivel
                if (appBasePath.toString().toLowerCase().endsWith("\\app")
                        || appBasePath.toString().toLowerCase().endsWith("/app")) {
                    appBasePath = appBasePath.getParent();
                    System.out.println("Ajustada ruta subiendo un nivel: " + appBasePath);
                }
            } else {
                // Estamos en desarrollo (ejecutando desde IDE)
                appBasePath = jarFile.toPath();
                System.out.println("Detectada ruta de clase (desarrollo): " + appBasePath);
            }
        } catch (Exception e) {
            System.err.println("Error detectando ruta: " + e.getMessage());
            e.printStackTrace();

            // Fallback: usar el directorio de trabajo
            appBasePath = Paths.get(System.getProperty("user.dir"));
            System.out.println("Usando directorio actual como alternativa: " + appBasePath);
        }
    }

    /**
     * Verifica si un directorio tiene permisos de escritura.
     */
    private static boolean isDirectoryWritable(Path directory) {
        if (!Files.exists(directory)) {
            return false;
        }

        try {
            // Crear un archivo temporal para probar permisos
            Path testFile = Files.createTempFile(directory, "write_test", ".tmp");
            Files.delete(testFile);
            return true;
        } catch (Exception e) {
            System.out.println("No se puede escribir en el directorio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea la carpeta de imágenes si no existe.
     */
    private static void createImagesFolder() throws IOException {
        // Crear carpeta "images" dentro del directorio de la aplicación
        imagesPath = appBasePath.resolve("images");

        if (!Files.exists(imagesPath)) {
            Files.createDirectories(imagesPath);
            System.out.println("✓ Carpeta de imágenes creada: " + imagesPath.toString());
        } else {
            System.out.println("✓ Carpeta de imágenes ya existe: " + imagesPath.toString());
        }
    }

    /**
     * Usa una ubicación alternativa para almacenar las imágenes. Por lo
     * general, esto será una carpeta en el directorio de usuario.
     */
    private static void useAlternativeLocation() {
        try {
            // Obtener directorio de usuario
            String userHome = System.getProperty("user.home");
            Path userDir = Paths.get(userHome);

            // Crear carpeta de la aplicación en el directorio de usuario
            Path appDir = userDir.resolve(".miet20");
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }

            // Establecer esta como la nueva ruta base
            appBasePath = appDir;

            // Crear carpeta de imágenes dentro de esta ubicación
            imagesPath = appDir.resolve("images");
            if (!Files.exists(imagesPath)) {
                Files.createDirectories(imagesPath);
            }

            System.out.println("Usando ubicación alternativa: " + appBasePath);

            // Informar al usuario
            JOptionPane.showMessageDialog(null,
                    "La aplicación no tiene permisos para escribir en su ubicación de instalación.\n"
                    + "Se usará el directorio alternativo: " + appBasePath + "\n\n"
                    + "Para evitar este mensaje en el futuro, considere ejecutar la aplicación como administrador.",
                    "Ubicación alternativa",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            System.err.println("Error al utilizar ubicación alternativa: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el directorio de recursos", e);
        }
    }

    /**
     * Verifica y descarga las imágenes faltantes.
     *
     * @return true si todas las imágenes están disponibles
     */
    private static boolean checkAndDownloadImages() {
        boolean allImagesAvailable = true;
        List<String> failedImages = new ArrayList<>();

        System.out.println("Verificando imágenes requeridas...");

        for (Map.Entry<String, String> entry : requiredImages.entrySet()) {
            String fileName = entry.getKey();
            String serverPath = entry.getValue();

            Path localImagePath = imagesPath.resolve(fileName);

            // Verificar si la imagen existe localmente
            if (!Files.exists(localImagePath)) {
                try {
                    // La imagen no existe, descargarla
                    System.out.println("Descargando imagen: " + fileName);
                    String fullUrl = SERVER_URL + serverPath;
                    downloadFile(fullUrl, localImagePath.toString());
                    System.out.println("✓ Imagen descargada exitosamente: " + fileName);
                } catch (Exception e) {
                    System.err.println("❌ Error al descargar imagen " + fileName + ": " + e.getMessage());
                    e.printStackTrace();
                    allImagesAvailable = false;
                    failedImages.add(fileName);
                }
            } else {
                System.out.println("✓ Imagen ya existe localmente: " + fileName);
            }
        }

        // Resumen de resultados
        if (!allImagesAvailable) {
            System.err.println("\nResumen de errores:");
            System.err.println("Imágenes que no pudieron ser descargadas: " + failedImages.size());
            for (String img : failedImages) {
                System.err.println("- " + img);
            }
        } else {
            System.out.println("\nTodas las imágenes están disponibles.");
        }

        return allImagesAvailable;
    }

    /**
     * Descarga un archivo desde una URL a una ruta local.
     *
     * @param urlStr URL del archivo a descargar
     * @param outputPath Ruta local donde guardar el archivo
     */
    private static void downloadFile(String urlStr, String outputPath) throws IOException {
        System.out.println("Descargando: " + urlStr);
        System.out.println("Destino: " + outputPath);

        URL url = new URL(urlStr);
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream()); FileOutputStream fos = new FileOutputStream(outputPath)) {

            // Transferir desde la URL al archivo local
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            // Verificar que el archivo se creó y tiene contenido
            File file = new File(outputPath);
            if (!file.exists() || file.length() == 0) {
                throw new IOException("La descarga parece haber fallado. Archivo no creado o vacío.");
            }
        } catch (IOException e) {
            System.err.println("Error durante la descarga: " + e.getMessage());
            throw e; // Re-lanzar para manejo superior
        }
    }

    /**
     * Obtiene la ruta completa a un archivo de imagen. Si el ResourceManager no
     * ha sido inicializado, lo inicializa primero.
     *
     * @param imageName Nombre del archivo de imagen
     * @return Ruta completa al archivo de imagen
     */
    public static String getImagePath(String imageName) {
        if (!initialized) {
            // Intentar inicializar
            boolean success = initialize();
            if (!success) {
                System.err.println("No se pudo inicializar ResourceManager");
                return null;
            }
        }

        // Verificar que la ruta de imágenes exista
        if (imagesPath == null) {
            System.err.println("ERROR: La ruta de imágenes es null");
            return null;
        }

        Path imagePath = imagesPath.resolve(imageName);

        // Si la imagen existe, retornar su ruta de inmediato
        if (Files.exists(imagePath)) {
            try {
                // Asegurarse de que sea una ruta absoluta
                String absolutePath = imagePath.toAbsolutePath().toString();
                System.out.println("Imagen encontrada: " + absolutePath);
                return absolutePath;
            } catch (Exception e) {
                System.err.println("Error al convertir ruta a absoluta: " + e.getMessage());
                return imagePath.toString(); // Retornar al menos la ruta básica
            }
        }

        System.out.println("Imagen no encontrada: " + imagePath);

        // Intentar descargar la imagen si está en la lista
        if (requiredImages.containsKey(imageName)) {
            try {
                String serverPath = requiredImages.get(imageName);
                String fullUrl = SERVER_URL + serverPath;

                System.out.println("Intentando descargar: " + fullUrl);

                // Verificar si la carpeta existe
                if (!Files.exists(imagesPath)) {
                    try {
                        Files.createDirectories(imagesPath);
                        System.out.println("Carpeta de imágenes creada: " + imagesPath);
                    } catch (Exception e) {
                        System.err.println("No se pudo crear la carpeta de imágenes: " + e.getMessage());
                        return null;
                    }
                }

                // Intentar descargar
                downloadFile(fullUrl, imagePath.toString());

                // Verificar si la descarga fue exitosa
                if (Files.exists(imagePath) && Files.size(imagePath) > 0) {
                    System.out.println("Imagen descargada exitosamente: " + imagePath);
                    return imagePath.toAbsolutePath().toString();
                } else {
                    System.err.println("La descarga no creó correctamente el archivo");
                }
            } catch (Exception e) {
                System.err.println("Error al descargar imagen: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Imagen no registrada en la lista de recursos: " + imageName);
        }

        // Si llegamos aquí, no se pudo obtener la imagen
        return null;
    }

    /**
     * Obtiene la ruta base de la aplicación.
     *
     * @return Ruta base donde se ejecuta la aplicación
     */
    public static String getAppBasePath() {
        if (!initialized) {
            initialize();
        }
        return appBasePath.toString();
    }

}
