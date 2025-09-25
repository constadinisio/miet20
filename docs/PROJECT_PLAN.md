# Proyecto Miet20 - Plan Maestro para Aplicación de Escritorio JavaFX

## 0. Mapeo del Repositorio

- [x] **Raíz del proyecto**: artefactos heredados (`build`, `dist`), configuración NetBeans (`nbproject`), bibliotecas (`lib`), manifiesto (`manifest.mf`) e íconos.
- [x] **Aplicación de escritorio existente (Swing)**: código en `src/main/java`, entrada `Main`, componentes como `NotificationUI`.
- [x] **Aplicación web y API**: `miet20webAPI/public` (frontend web con Tailwind) y `miet20webAPI/api` (Node/Express, entry `src/server.js`, rutas versionadas `/api/v1`, especificación OpenAPI en `api/openapi.yaml`).

> ✅ **Checkpoint 0 completado:** Conocimiento inicial del repositorio y alcance global.

---

## 1. Fundaciones del Proyecto JavaFX

**Objetivo:** Crear la base del proyecto de escritorio moderno usando JavaFX.

### Tareas
- [x] Inicializar proyecto Gradle/Maven para JavaFX (JDK 21 recomendado).
- [x] Crear estructura de paquetes:
  - `com.miet20.app` (entry point JavaFX, `MainApp`).
  - `com.miet20.config` (configuraciones, carga de archivos externos).
  - `com.miet20.services` (consumo de API, clientes HTTP).
  - `com.miet20.viewmodels` (lógica de presentación, `ObservableProperty`).
  - `com.miet20.ui` (controladores JavaFX).
  - `com.miet20.ui.components` (componentes reutilizables).
  - `com.miet20.resources` (FXML, CSS, imágenes).
- [x] Configurar dependencias: JavaFX (Controls, FXML), Jackson, HTTP Client (JDK), ControlsFX, TestFX (test), JUnit 5, MockWebServer.
- [x] Añadir configuración `gradle.properties` para JavaFX modular y `build.gradle` con tasks `run`, `test`, `jpackage`.
- [x] Definir plantilla de recursos (`resources/application.json`, `resources/styles/theme.css`).

### Entregables de la etapa
- Proyecto compilable con `gradle run` que abre ventana base vacía.
- Documentación mínima (`README_DESKTOP.md`) con instrucciones para ejecutar.

> ⏭️ **Próximo checkpoint:** Proyecto JavaFX base creado y ejecutándose (`gradle run`).

---

## 2. Infraestructura de Red y Configuración

**Objetivo:** Implementar la capa de acceso a la API REST.

### Tareas
- [ ] Crear `config/application.json` con parámetros `apiBaseUrl`, `timeoutMs`, etc.
- [ ] Implementar `ApiConfig` que lea el archivo y permita reconfigurar la URL.
- [ ] Implementar `ApiClient` basado en `java.net.http.HttpClient` con manejo de encabezados y errores.
- [ ] Implementar `TokenManager` (persistencia en memoria y disco opcional) para `access` y `refresh` tokens.
- [ ] Crear `HealthService` para verificar `/health` y validar versión (`v1`).
- [ ] Añadir pruebas unitarias con `MockWebServer` para validar escenarios de red.

### Entregables
- Código capaz de consumir `/health` y reportar estado en consola/UI.
- Documentación de configuración (`docs/API_CONFIG.md`).

> ⏭️ **Checkpoint:** `HealthService` muestra versión compatible y maneja errores de conectividad.

---

## 3. Autenticación y Gestión de Sesión

**Objetivo:** Replicar el flujo de login de la web.

### Tareas
- [ ] Diseñar vista FXML de login (`resources/fxml/login.fxml`) con estilos de la web.
- [ ] Crear CSS (`resources/styles/login.css`) con gradientes y tipografías equivalentes.
- [ ] Implementar `LoginViewModel` con bindings (email, password, loading, error).
- [ ] Integrar `AuthService` (`POST /auth/login`, `/auth/google-login` si aplica).
- [ ] Manejar tokens y persistir sesión con `TokenManager`.
- [ ] Implementar recordatorios de contraseña/enlaces.

### Entregables
- Pantalla de login funcional que accede a la API real (sandbox) y navega al shell principal.

> ⏭️ **Checkpoint:** Login exitoso actualiza vista y guarda tokens.

---

## 4. Navegación Principal y Shell

**Objetivo:** Construir la estructura base de navegación.

### Tareas
- [ ] Diseñar shell principal (`MainAppShell.fxml`) con sidebar, header y contenedor central.
- [ ] Crear componentes: `SidebarView`, `HeaderView`, `ContentRouter`.
- [ ] Implementar navegación interna con `RouterService` y `ViewLoader` (FXML cacheado).
- [ ] Aplicar estilos responsive (colapso de sidebar en ventanas pequeñas).
- [ ] Integrar estado del usuario (nombre, avatar) en header.

### Entregables
- Shell que permita cambiar entre vistas placeholder (Dashboard, Alumnos, Noticias, Ajustes).

> ⏭️ **Checkpoint:** Navegación fluida entre módulos y diseño responsive básico.

---

## 5. Módulos Funcionales Clave

**Objetivo:** Replicar las funcionalidades principales de la web.

### Iteraciones (repetir para cada módulo prioritario)
1. **Dashboard**
   - [ ] Servicio `DashboardService` (`/dashboard/metrics`).
   - [ ] Cards y gráficos (usar `ChartsFX` o `JavaFX Charts`).
   - [ ] Animaciones/indicadores de carga.

2. **Noticias**
   - [ ] Listar noticias (`/news`).
   - [ ] Detalle y creación/edición con formularios y subida de imágenes.

3. **Alumnos**
   - [ ] Tabla con paginación (`/students`).
   - [ ] Formulario de alta y edición (`/students/{id}`).

4. **Cursos/Asignaturas**
   - [ ] Árbol o tabla jerárquica (`/courses`, `/subjects`).

5. **Asistencias**
   - [ ] Registro y reportes (`/attendance`).

6. **Notas**
   - [ ] Ingreso y consulta (`/grades`).

7. **Notificaciones**
   - [ ] Migrar `NotificationUI` a JavaFX, integración con `/notifications`.

### Entregables por módulo
- Servicio API, ViewModel, vista JavaFX, pruebas unitarias (servicio) y manuales (UI).
- Documentar particularidades en `docs/MODULES.md`.

> ⏭️ **Checkpoint general:** Cada módulo clave en producción tiene checkboxes individuales completados en `docs/MODULES.md`.

---

## 6. Notificaciones y Tiempo Real

**Objetivo:** Experiencia moderna de notificaciones.

### Tareas
- [ ] Implementar polling o WebSocket según API.
- [ ] Crear componente JavaFX de lista expandible con filtros.
- [ ] Integrar con sistema de alertas en la app (toasts).
- [ ] Añadir configuración de intervalo en `application.json`.

> ⏭️ **Checkpoint:** Notificaciones en tiempo (semi)real funcionando y UI migrada desde Swing.

---

## 7. Configuraciones, Archivos y Extras

**Objetivo:** Cobertura de funcionalidades restantes.

### Tareas
- [ ] Implementar subida de archivos (`/files`) con progreso.
- [ ] Sección de configuración de usuario y roles.
- [ ] Formularios avanzados (validación, autocompletado).
- [ ] Accesibilidad (soporte teclado, contraste).

> ⏭️ **Checkpoint:** Todos los flujos secundarios completados y documentados.

---

## 8. Pruebas, Documentación y Empaquetado

**Objetivo:** Garantizar calidad y entrega.

### Tareas
- [ ] Diseñar plan de pruebas (unitarias, integración, UI) y ejecutarlo.
- [ ] Configurar CI (GitHub Actions) para `gradle build`, `gradle test`, API lint.
- [ ] Preparar empaquetado con `jpackage` para Windows (EXE/MSI) y guía de instalación.
- [ ] Elaborar manual de usuario y administrador.

> ⏭️ **Checkpoint final:** Build empaquetado, documentación final entregada y checklist de smoke tests firmado.

---

## Apéndices

### A. Integración API ↔ JavaFX (Resumen Técnico)
- `ApiConfig` carga `config/application.json`.
- `ApiClient` construye URLs manteniendo `/api/v1`.
- Servicios usan `ObjectMapper` de Jackson para serializar/deserializar.
- ViewModels actualizan la UI mediante `Platform.runLater`.
- Tokens gestionados por `TokenManager` con refresh automático.

### B. Coherencia Visual
- Estilos centralizados en `resources/styles/theme.css` replicando Tailwind (colores, tipografía `Inter`).
- Componentes reutilizables (botones, tarjetas, tablas) definen clases CSS (`.btn-primary`, `.card`, `.badge`).
- Uso de `FontAwesomeFX` para iconografía similar al web.

### C. Plan de Pruebas (Resumen)
- Unitarias: `ApiConfigTest`, `AuthServiceTest`, `StudentServiceTest`, etc.
- Integración: pruebas contra API en sandbox.
- UI: TestFX para flujos críticos.
- Manual: checklist de smoke test.

### D. Empaquetado Windows
- `gradle jpackage --type exe` con ícono `logo_et20_max.ico`.
- Incluir `config/application.json` editable.
- Documentar pasos en `docs/DEPLOY_WINDOWS.md` (a crear en etapa 8).

