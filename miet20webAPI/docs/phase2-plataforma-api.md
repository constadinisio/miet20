# Fase 2 · Plataforma API

## Objetivos principales
- **Consolidar el núcleo de la API** agregando autenticación robusta, observabilidad y un contrato de respuesta homogéneo para todos los controladores.
- **Implementar los endpoints prioritarios** levantados en la fase 1 (usuarios, alumnos, cursos, calificaciones, asistencias) y ampliar la cobertura para noticias, galería, contactos, archivos públicos y autenticación social.
- **Garantizar calidad y documentación** a través de pruebas automatizadas, linters, especificaciones OpenAPI y colecciones de prueba compartidas.

## Entregables clave
| Hito | Descripción | Artefactos |
|------|-------------|------------|
| 2.1 Núcleo reforzado | Middleware de autenticación multirol con refresh tokens, control de permisos por recurso, logging estructurado y manejador de errores con formato único (`status`, `data`, `error`). | Código en `api/src/middlewares/`, guía de configuración `.env`, documentación en `docs/API.md` (sección de autenticación). |
| 2.2 Endpoints críticos | CRUD completo de usuarios, alumnos, cursos, materias, calificaciones, asistencias y asignaciones docentes según especificación `docs/API.md`. | Rutas en `api/src/routes/v1/`, servicios en `api/src/services/`, pruebas en `api/tests/`. |
| 2.3 Endpoints faltantes | Nuevos dominios: noticias, galería multimedia, contactos, páginas públicas, soporte Google OAuth y gestión de archivos adjuntos. | Especificación actualizada, migraciones/seeders, pruebas de integración. |
| 2.4 Calidad y DX | Cobertura de pruebas (>70%), pipelines CI con lint/test, colecciones Insomnia/Postman y ejemplos cURL. | `api/tests/`, `.github/workflows/api-ci.yml`, `docs/API.md`, `docs/postman/MIET20.postman_collection.json`. |

## Flujo de trabajo recomendado
1. **Revisión técnica inicial**
   - Auditar `api/` y validar dependencias (`express`, `knex`, `jsonwebtoken`, `winston`).
   - Definir convenciones de carpetas (controllers/services/repositories) y reglas de linting (ESLint + Prettier).
2. **Autenticación y seguridad**
   - Implementar login multirol, refresh tokens, rotación manual de contraseñas y cierre de sesiones (`token blacklist`).
   - Añadir middleware `requireRole([...])` para recursos protegidos.
   - Configurar rate limiting (por IP) y protección CSRF para endpoints sensibles.
3. **Infraestructura transversal**
   - Crear `ApiResponse` helper para unificar respuestas (éxito/error) y registrar auditoría con `requestId`.
   - Instrumentar logs (Winston + transports a archivo/console) y seguimiento de métricas (Prometheus/StatsD).
   - Configurar tests base con Jest + Supertest (fixtures de BD con knexfile separado para test).
4. **Implementación de dominios core**
   - Usuarios/Roles: CRUD, asignación de roles adicionales, restablecimiento de contraseña.
   - Alumnos/Matrículas: CRUD, alta/baja de inscripciones, historial académico.
   - Cursos/Materias/Horarios: CRUD, asignación de docentes, generación de horarios institucionales.
   - Calificaciones/Asistencias: endpoints para carga masiva y consulta histórica.
5. **Nuevos dominios priorizados**
   - **Noticias**: gestión de publicaciones internas, estado (borrador/publicada), destinatarios y confirmaciones.
   - **Galería multimedia**: álbumes, fotos, videos y visibilidad (privado/público).
   - **Contactos y páginas públicas**: formularios de contacto, contenidos institucionales, descarga de documentos.
   - **Archivos adjuntos**: servicio de almacenamiento (S3/local) con URLs firmadas y metadatos.
   - **Autenticación Google**: endpoint de `POST /auth/google` que valide tokens de Google y cree/ligue cuentas.
6. **Pruebas y documentación**
   - Redactar especificaciones OpenAPI (`api/openapi.yaml`) y exportar colecciones de prueba.
   - Automatizar pruebas unitarias/integración y cobertura en CI.
   - Actualizar `docs/API.md` tras cada incorporación y mantener changelog.
7. **Preparación para Fase 3**
   - Exponer contratos estables para el cliente PHP (nombres de campos, códigos de error).
   - Entregar ejemplos de uso (cURL, PHP Guzzle) y guías de migración para helpers.

## Backlog detallado
- [x] Configurar ESLint/Prettier, scripts `lint` y `test` en `package.json`.
- [x] Implementar `AuthController` con login, refresh token, logout y Google OAuth (requiere tablas `auth_refresh_tokens`, `auth_revoked_tokens` y configuración `GOOGLE_CLIENT_ID`).
- [x] Crear middleware `ensureAuthenticated`, `requireRole`, `errorHandler`, `requestLogger`.
- [x] Normalizar respuesta JSON (`status`, `data`, `errors`, `meta`).
- [x] Implementar repositorios y servicios para usuarios, roles, permisos.
- [x] Implementar repositorios/servicios para alumnos, matrículas y historial.
- [x] Implementar repositorios/servicios para cursos, materias, horarios, asignaciones docentes.
- [x] Crear endpoints de calificaciones (carga, edición, consulta) con validaciones.
- [x] Crear endpoints de asistencias con carga masiva y edición puntual.
- [x] Añadir endpoints `news` (CRUD, destinatarios, confirmaciones).
- [x] Añadir endpoints `gallery` (álbumes, ítems multimedia, publicación pública).
- [x] Añadir endpoints `contacts` (formularios, respuestas, archivos adjuntos).
- [x] Añadir endpoints `public-pages` (contenido institucional editable).
- [x] Añadir módulo de archivos (`/files`) con gestión de adjuntos y URLs firmadas.
- [x] Documentar los nuevos endpoints en `docs/API.md` y exportar colección Postman.
- [x] Configurar tests con Jest/Supertest y cobertura mínima acordada.
- [x] Integrar pipeline CI con lint + test (GitHub Actions).
- [x] Preparar scripts de migración de base de datos (Knex) para tablas nuevas (noticias, galería, adjuntos).
- [x] Redactar guía de despliegue (variables de entorno, storage, credenciales OAuth).

## Avances recientes
- Se habilitó el workflow `api-ci.yml` en GitHub Actions que instala dependencias, ejecuta `npm run lint` y `npm test` sobre la carpeta `api/`, habilitando retroalimentación continua para la calidad del código.
- Se versionaron las migraciones de Knex para `noticias`, `imagenes` y `archivos`, junto con scripts `npm run migrate` y `npm run migrate:rollback` para facilitar la preparación del esquema en cada entorno.
- Se agregaron suites unitarias para calificaciones y asistencias que validan la lógica de negocio sin acceder a la base de datos, facilitando la detección de regresiones en los servicios core.
- Se documentó el procedimiento de despliegue en `docs/deployment-guide.md`, incluyendo variables de entorno, comandos clave y recomendaciones operativas.
- Se consolidó la documentación de referencia con `api/openapi.yaml` y la colección `docs/postman/MIET20.postman_collection.json` para pruebas manuales.

## Riesgos y mitigaciones
| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Inconsistencia en contratos de datos entre API y frontend PHP | Alto | Definir contratos en OpenAPI y validarlos con pruebas de integración antes de exponerlos a Fase 3. |
| Falta de cobertura de seguridad (tokens no rotados, permisos débiles) | Alto | Implementar middleware obligatorio, auditoría y pruebas de seguridad (roles). |
| Sobrecarga en la base de datos por operaciones masivas (asistencias, calificaciones) | Medio | Optimizar queries con índices, paginación y operaciones batch, monitorear con métricas. |
| Gestión de archivos multimedia (almacenamiento, permisos) | Medio | Adoptar proveedor S3-compatible y generar URLs firmadas con expiración, fallback local en dev. |
| Dependencia en Google OAuth sin plan B | Bajo | Mantener login tradicional, registrar métricas de uso y manejar graceful degradation. |

## Métricas de éxito
- 100% de endpoints críticos implementados y documentados.
- Cobertura de pruebas ≥ 70% líneas/funciones en `api/`.
- Tiempos de respuesta promedio < 300 ms para endpoints CRUD bajo carga esperada.
- 0 vulnerabilidades críticas abiertas en análisis SAST/Dependabot.
- Satisfacción del equipo de frontend con los contratos expuestos (feedback positivo en revisión de Fase 3).