# Definir el directorio base del proyecto
$BASE_DIR = "."
$MAIN_PACKAGE = "src\main\java\com\et20\plataforma"

# Crear la estructura de directorios principal
Write-Host "Creando estructura de directorios..."
$dirs = @(
    "$MAIN_PACKAGE",
    "$MAIN_PACKAGE\config",
    "$MAIN_PACKAGE\controller",
    "$MAIN_PACKAGE\model",
    "$MAIN_PACKAGE\model\dto",
    "$MAIN_PACKAGE\repository",
    "$MAIN_PACKAGE\service",
    "$MAIN_PACKAGE\exception",
    "$MAIN_PACKAGE\ui",
    "$MAIN_PACKAGE\ui\common",
    "$MAIN_PACKAGE\ui\login",
    "$MAIN_PACKAGE\ui\alumno",
    "$MAIN_PACKAGE\ui\admin",
    "src\main\resources",
    "src\main\resources\static",
    "src\main\resources\static\images",
    "src\main\resources\static\css",
    "src\main\resources\templates",
    "src\main\resources\templates\reports",
    "src\test\java\com\et20\plataforma",
    "src\test\java\com\et20\plataforma\controller",
    "src\test\java\com\et20\plataforma\service",
    "src\test\java\com\et20\plataforma\repository"
)

foreach ($dir in $dirs) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force
        Write-Host "Creado directorio: $dir"
    }
}

# Función para mover archivo si existe
function Move-IfExists {
    param($source, $destination)
    if (Test-Path $source) {
        Write-Host "Moviendo $source a $destination"
        Move-Item -Path $source -Destination $destination -Force
    } else {
        Write-Host "Advertencia: No se encontró el archivo $source"
    }
}

# Mover archivos existentes a sus nuevas ubicaciones
Write-Host "Moviendo archivos a sus nuevas ubicaciones..."

# Mover archivos de login
Move-IfExists "login\GoogleAuthenticator.java" "$MAIN_PACKAGE\service\"
Move-IfExists "login\UserSession.java" "$MAIN_PACKAGE\model\"
Move-IfExists "login\login.java" "$MAIN_PACKAGE\ui\login\"

# Mover archivos de usuarios
Move-IfExists "users\Alumnos\alumnos.java" "$MAIN_PACKAGE\ui\alumno\"

# Mover imágenes si existen
if (Test-Path "images") {
    Write-Host "Moviendo imágenes..."
    Get-ChildItem "images" | Move-Item -Destination "src\main\resources\static\images\" -Force
}

# Crear application.properties si no existe
$propertiesPath = "src\main\resources\application.properties"
if (!(Test-Path $propertiesPath)) {
    Write-Host "Creando application.properties..."
    @"
# Configuración de la base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/et20plataforma
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Configuración del servidor
server.port=8080

# Configuración de Google OAuth2
spring.security.oauth2.client.registration.google.client-id=tu_client_id
spring.security.oauth2.client.registration.google.client-secret=tu_client_secret
spring.security.oauth2.client.registration.google.scope=email,profile
"@ | Out-File -FilePath $propertiesPath -Encoding UTF8
}

# Crear PlataformaApplication.java si no existe
$mainAppPath = "$MAIN_PACKAGE\PlataformaApplication.java"
if (!(Test-Path $mainAppPath)) {
    Write-Host "Creando PlataformaApplication.java..."
    @"
package com.et20.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PlataformaApplication {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(PlataformaApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);
    }
}
"@ | Out-File -FilePath $mainAppPath -Encoding UTF8
}

Write-Host "`n¡Reorganización completada!"
Write-Host "Por favor, verifica que todos los archivos se hayan movido correctamente."
Write-Host "Recuerda actualizar los packages en los archivos Java según su nueva ubicación."