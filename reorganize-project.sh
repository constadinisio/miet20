#!/bin/bash

# Definir el directorio base del proyecto
BASE_DIR="."
MAIN_PACKAGE="src/main/java/com/et20/plataforma"

# Crear la estructura de directorios principal
echo "Creando estructura de directorios..."
mkdir -p "$MAIN_PACKAGE"/{config,controller,model,repository,service,ui/login}
mkdir -p "src/main/resources"

# Crear directorios para recursos
mkdir -p "src/main/resources/static/images"

# Función para mover archivo si existe
move_if_exists() {
    if [ -f "$1" ]; then
        echo "Moviendo $1 a $2"
        mv "$1" "$2"
    else
        echo "Advertencia: No se encontró el archivo $1"
    fi
}

# Mover archivos existentes a sus nuevas ubicaciones
echo "Moviendo archivos a sus nuevas ubicaciones..."

# Mover archivos de login
move_if_exists "login/GoogleAuthenticator.java" "$MAIN_PACKAGE/service/"
move_if_exists "login/UserSession.java" "$MAIN_PACKAGE/model/"
move_if_exists "login/login.java" "$MAIN_PACKAGE/ui/login/"

# Mover archivos de usuarios
move_if_exists "users/Alumnos/alumnos.java" "$MAIN_PACKAGE/ui/"

# Mover imágenes si existen
if [ -d "images" ]; then
    echo "Moviendo imágenes..."
    mv images/* "src/main/resources/static/images/" 2>/dev/null || true
fi

# Crear archivos necesarios si no existen
echo "Creando archivos base..."

# application.properties
if [ ! -f "src/main/resources/application.properties" ]; then
    echo "Creando application.properties..."
    cat > "src/main/resources/application.properties" << EOF
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
EOF
fi

# PlataformaApplication.java
if [ ! -f "$MAIN_PACKAGE/PlataformaApplication.java" ]; then
    echo "Creando PlataformaApplication.java..."
    cat > "$MAIN_PACKAGE/PlataformaApplication.java" << EOF
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
EOF
fi

echo "¡Reorganización completada!"
echo "Por favor, verifica que todos los archivos se hayan movido correctamente."
echo "Recuerda actualizar los packages en los archivos Java según su nueva ubicación."