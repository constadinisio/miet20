# Fase 3 · Cliente PHP reutilizable

La Fase 3 capitaliza la auditoría (Fase 1) y la ampliación de la API (Fase 2) para entregar un cliente PHP homogéneo que reemplace el acceso directo a MySQL en el legacy. Esta etapa tiene tres frentes: construir el núcleo HTTP, envolver cada dominio funcional y orquestar la sustitución en vistas/controladores.

## 1. Objetivos específicos
- **Consolidar un único gateway HTTP** (`App\\Api\\HttpClient`) que maneje autenticación, resiliencia y telemetría para todas las llamadas a la API Node.js.
- **Exponer servicios de dominio** (`App\\Api\\CursosService`, `NoticiasService`, `GaleriaService`, etc.) que respeten los contratos acordados en `docs/API.md` y abstraigan estructuras JSON a arrays/DTOs PHP.
- **Eliminar dependencias directas de SQL** en scripts PHP mediante helpers que sustituyan gradualmente a `backend/includes/db.php`, `public/includes/functions.php` y utilitarios afines.
- **Asegurar trazabilidad y calidad** con pruebas automatizadas, logging centralizado y métricas que permitan monitorear la adopción del cliente.

## 2. Entregables
| Hito | Resultado | Artefactos |
|------|-----------|------------|
| 3.1 Núcleo HTTP | Clase `App\\Api\\HttpClient` con soporte de configuración por entorno, autenticación (login/refresh), gestión de sesiones y políticas de reintentos. | `public/includes/api/HttpClient.php`, `.env`/`config/api.php`, documentación de inicialización. |
| 3.2 Servicios de dominio | Paquetes `App\\Api\\{Dominio}Service` (Usuarios, Cursos, Alumnos, Calificaciones, Asistencias, Finanzas, Noticias, Galería, Contactos, Integraciones externas) alineados con los endpoints REST existentes o recién incorporados. | Archivos en `public/includes/api/services/`, convenciones PHPDoc y ejemplos de uso. |
| 3.3 Helpers legacy compatibles | Adaptadores o facades (`public/includes/api/helpers.php`) que ofrecen funciones procedurales (`obtenerCursos()`, `guardarAsistencia()`, `listarNoticias()`, etc.) para acelerar la migración sin reescribir vistas de inmediato. | Helpers versionados, checklist de reemplazo, anotaciones `@deprecated`. |
| 3.4 Cobertura y observabilidad | Suite de pruebas PHPUnit, mocks de API, logging con Monolog y métricas básicas (tiempos de respuesta, ratio de errores, expiración de tokens). | `tests/Api/HttpClientTest.php`, `tests/Api/{Dominio}ServiceTest.php`, configuración CI/CD, dashboard de métricas. |

## 3. Secuencia de trabajo
1. **Preparación**
   - Confirmar dependencias Composer (GuzzleHttp o Symfony HttpClient). Crear `composer.json` si no existe y definir autoload PSR-4 bajo `App\\Api\\`.
   - Revisar `docs/API.md` y las nuevas rutas definidas en Fase 2 para mapear recursos necesarios (usuarios, cursos, noticias, galería, formularios de contacto, archivos, integraciones Google OAuth).
   - Definir convenciones de manejo de errores (`ApiException`, `ValidationException`, `AuthException`) y estructura de respuesta estándar (`['ok' => bool, 'data' => mixed, 'error' => ?array]`).

2. **Implementación del núcleo HTTP**
   - Encapsular configuración (`API_BASE_URL`, `API_TIMEOUT`, `API_CLIENT_ID`, `API_CLIENT_SECRET`) usando `config/api.php` o variables de entorno.
   - Implementar métodos `request()`, `get()`, `post()`, `put()`, `delete()` con manejo uniforme de encabezados, serialización JSON y trazado (requestId).
   - Añadir autenticación basada en JWT/refresh tokens: iniciar sesión con credenciales de servicio o del usuario autenticado, refrescar automáticamente al recibir `401` y proteger contra condiciones de carrera.
   - Incluir políticas de resiliencia (reintentos exponenciales para 5xx/timeouts, circuit breaker ligero) y registro estructurado de errores con Monolog.

3. **Servicios de dominio**
   - Crear servicios modulares que correspondan con cada sección inventariada en Fase 1:
     - **Usuarios y roles**: login, perfil, listado, creación/actualización, reset de contraseñas.
      - **Cursos y cohortes**: catálogo, detalle, docentes asignados, horarios.
     - **Alumnos**: inscripción, historial académico, actualización de datos, tutorías.
     - **Calificaciones y asistencias**: carga, consulta, reportes.
     - **Pagos/SPEI**: consulta de adeudos, conciliación, comprobantes.
     - **Contenido público**: noticias, galería multimedia, páginas estáticas, formularios de contacto.
     - **Integraciones**: Google OAuth, notificaciones push, gestión de archivos.
   - Mapear cada método al endpoint correspondiente (ej. `GET /api/noticias`, `POST /api/galeria/items`, `POST /api/integraciones/google/oauth/callback`). Validar parámetros, transformar respuestas a estructuras amigables y propagar errores semánticos (404, 409, 422).
   - Documentar contratos en PHPDoc y actualizar `docs/API.md` con ejemplos cuando se creen nuevos endpoints.

4. **Helpers y migración de vistas**
   - Construir una capa de compatibilidad en `public/includes/api/helpers.php` que exponga funciones procedurales utilizadas actualmente en las vistas/controladores. Cada helper delega en el servicio correspondiente y gestiona traducción de excepciones a códigos/strings esperados.
   - Actualizar gradualmente scripts:
     - Reemplazar `require 'backend/includes/db.php'` por `require 'public/includes/api/bootstrap.php'`.
     - Sustituir consultas SQL directas (`mysqli_query`, `PDO`) por llamadas al helper equivalente.
     - Revisar módulos críticos identificados en la auditoría (inscripciones, asistencias, noticias, galería, formularios) asegurando que la lógica de negocio reside ahora en la API.
   - Mantener un checklist de archivos migrados y de dependencias restantes para priorizar la Fase 4 (migración por módulos).

5. **Pruebas y observabilidad**
   - Crear stubs/mocks de la API con Guzzle Mock Handler o un servidor fake para validar respuestas y errores.
   - Implementar pruebas unitarias y de integración que cubran autenticación, reintentos, manejo de errores y métodos críticos de cada servicio.
   - Integrar el cliente en el pipeline CI (Github Actions) para ejecutar `composer test` y verificaciones estáticas (PHPStan/Psalm).
   - Centralizar logs y métricas (New Relic, ELK, Prometheus) con dashboards que permitan monitorear latencia, tasa de error y expiración de tokens.

## 4. Backlog priorizado
1. **Infraestructura y base**
   - [x] Crear/actualizar `composer.json` con dependencias (`guzzlehttp/guzzle`, `monolog/monolog`, `psr/log`).
   - [x] Configurar autoload PSR-4 y bootstrap (`public/includes/api/bootstrap.php`).
   - [x] Definir estructura de configuración (`config/api.php`) y manejo seguro de credenciales.

2. **Cliente HTTP**
   - [x] Implementar `App\\Api\\HttpClient` con soporte de autenticación y retries.
   - [x] Crear excepciones personalizadas y normalizador de respuestas.
   - [x] Integrar logging estructurado y métricas básicas.

3. **Servicios prioritarios** (según criticidad en Fase 1)
   - [x] `UsuariosService` y `AuthService` (login multirol, refresh, gestión de perfiles).
   - [x] `CursosService` y `AlumnosService` (listados, detalle, inscripción, matrículas activas).
   - [x] `CalificacionesService`, `AsistenciasService` y `FinanzasService` (SPEI).
   - [x] `NoticiasService`, `GaleriaService`, `ContactosService` (módulos públicos pendientes).
   - [x] `IntegracionesService` (Google OAuth, notificaciones, archivos compartidos).

4. **Helpers legacy y migración**
- [x] Publicar helpers procedurales equivalentes y documentar qué vistas los consumen.
- [x] Migrar scripts en `backend/` (administración) siguiendo el orden del backlog.
- [x] Reemplazar endpoints de `backend/notificaciones/*.php` para consumir el cliente API.
- [x] Migrar scripts en `public/` (sitio público, portal alumnos/docentes).
- [x] Migrar panel administrativo (configuración, cursos, alumnos y notificaciones) al cliente API y helpers reutilizables.
- [x] Completar migración del portal de alumnos (asistencias, notas y configuración) contra el cliente API.
- [x] Completar migración de portales de docentes y preceptores restantes.
- [x] Eliminar dependencias de `db.php` y marcar funciones obsoletas.

5. **Calidad y monitoreo**
   - [x] Escribir pruebas PHPUnit por dominio y escenario de error.
   - [x] Configurar CI/CD para ejecutar pruebas y análisis estático.
   - [x] Implementar dashboards de logs/metricas y alertas de expiración de tokens.
   - [x] Documentar plan de soporte y runbooks (rotación de claves, manejo de timeouts).

### Actualización de calidad y monitoreo (junio 2024)

- Se añadió el workflow `PHP Client CI` (`.github/workflows/php-client-ci.yml`) que ejecuta `composer test` y `composer analyse` en cada push o Pull Request que afecte al cliente PHP.
- El colector `AggregatingMetricsCollector` exporta métricas en formato **NDJSON** hacia `storage/logs/api-metrics.ndjson`, habilitando la ingesta automática en ELK/Prometheus.
- El `TokenExpirationMonitor` genera métricas y logs para expiraciones inminentes (`tokens/expiration`), facilitando la creación de alertas en dashboards.
- Los procedimientos operativos y runbooks están consolidados en `docs/phase3-runbooks.md`, incluyendo rotación de credenciales, tratamiento de tokens expirados y respuesta a timeouts.

## 5. Riesgos y mitigaciones
| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Contratos inconsistentes entre servicios PHP y API | Alto | Mantener esquemas JSON compartidos, pruebas contractuales y revisión conjunta con el equipo API antes de liberar cambios. |
| Latencia acumulada por múltiples llamadas secuenciales | Medio | Añadir caching local (APCu/Redis), agrupar endpoints (batch) y aprovechar endpoints masivos definidos en Fase 2. |
| Manejo incorrecto de expiración de tokens | Alto | Implementar refresh anticipado, pruebas específicas y alertas que detecten `401` recurrentes. |
| Resistencia de equipos a adoptar la nueva capa | Medio | Proporcionar helpers compatibles, guías prácticas, sesiones de capacitación y soporte durante el piloto. |
| Falta de visibilidad de errores en producción | Medio | Obligatoriedad de logging estructurado, correlación `requestId` y dashboards accesibles. |

## 6. Métricas de éxito
- ≥ 80% de los módulos priorizados consumen servicios API a través del cliente reutilizable (medido por telemetría).
- Cobertura de pruebas del cliente y servicios ≥ 70%, con suites ejecutándose en CI en cada merge.
- Latencia promedio por llamada (cliente + API) < 400 ms en staging para operaciones críticas.
- Cero incidentes P0/P1 relacionados con expiración de tokens o manejo de errores durante el piloto.
- Feedback ≥ 8/10 de desarrolladores y soporte respecto a la claridad de la nueva capa y documentación.

## 7. Entregables de documentación
- Guía de integración rápida para desarrolladores (cómo inicializar el cliente, ejemplos de uso por dominio).
- Checklist de migración por módulo con responsables y estado.
- Runbooks operativos (rotación de credenciales, respuesta ante caídas de la API, escalamiento de errores).
- Registro de decisiones arquitectónicas (ADR) sobre dependencias, manejo de errores y caching.
- `docs/phase3-runbooks.md` centraliza los procedimientos de soporte continuo y monitoreo.

## 8. Siguientes pasos inmediatos

1. **Orquestar dashboards productivos**: conectar `storage/logs/api-metrics.ndjson` con la plataforma corporativa (Grafana/ELK) y publicar paneles de latencia, tasa de error y alertas de expiración.
2. **Validar pipelines**: ejecutar el workflow `PHP Client CI` en un branch de prueba para verificar permisos y cacheo de dependencias en el repositorio principal.
3. **Socialización de runbooks**: coordinar con soporte y mesa de ayuda una sesión de repaso de `docs/phase3-runbooks.md` y ejercicios de respuesta ante tokens expirados.