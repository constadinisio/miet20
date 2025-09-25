# Miet20 Desktop (JavaFX)

Este directorio (`desktop/`) contiene la nueva base para la aplicación de escritorio Miet20 desarrollada con JavaFX 22 y Gradle.

## Requisitos

- JDK 21 o superior.
- Gradle instalado localmente o el wrapper generado (ver nota siguiente).

## Comandos principales

```bash
cd desktop
./gradlew run        # Ejecuta la aplicación JavaFX
./gradlew test       # Ejecuta las pruebas unitarias
./gradlew jpackage   # Genera instalador empaquetado (usa jlink)
```

> **Notas:**
>
> - En entornos sin servidor gráfico puede ser necesario configurar Monocle u otra solución headless para mostrar la ventana de JavaFX.
> - Para evitar binarios en el repositorio, el archivo `gradle-wrapper.jar` no está versionado. Si se desea utilizar `./gradlew`, ejecutar una vez `gradle wrapper --gradle-version 8.6` dentro del directorio `desktop/` (requiere Gradle instalado globalmente) para que el jar se descargue localmente.

## Estructura de paquetes

- `com.miet20.app`: punto de entrada (`MainApp`).
- `com.miet20.config`: clases de configuración y carga de recursos.
- `com.miet20.services`: clientes y servicios para la API Miet20.
- `com.miet20.viewmodels`: lógica de presentación observable.
- `com.miet20.ui`: controladores JavaFX.
- `com.miet20.ui.components`: componentes reutilizables.
- `com.miet20.resources`: FXML, CSS, imágenes y configuraciones.

## Recursos

- `resources/application.json`: configuración inicial de la aplicación.
- `resources/styles/theme.css`: tema base para la interfaz.
- `resources/fxml/main-view.fxml`: pantalla base de bienvenida.

