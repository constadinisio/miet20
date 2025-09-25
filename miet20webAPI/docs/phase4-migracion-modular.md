# Fase 4 · Migración módulo a módulo

La Fase 4 materializa el reemplazo del acceso directo a MySQL en el legacy PHP trasladando, módulo por módulo, la lógica de datos hacia la API Node.js mediante el cliente reutilizable construido en Fase 3. Se apoya en la auditoría (Fase 1) y la ampliación de la API (Fase 2) para ejecutar migraciones controladas, con telemetría y validaciones continuas.

## 1. Objetivos específicos
- **Completar la migración funcional** de los módulos inventariados, sustituyendo consultas SQL y helpers legacy por servicios API.
- **Orquestar despliegues seguros** habilitando toggles `LEGACY_DB_MODE`/`API_MODE` por módulo para rollback rápido.
- **Capturar y resolver brechas residuales** de la API o del cliente reutilizable detectadas durante la migración.
- **Asegurar trazabilidad operativa** mediante métricas, logs y evidencia funcional de cada módulo migrado.

## 2. Entregables
| Hito | Resultado | Artefactos |
|------|-----------|------------|
| 4.1 Plan de migración modular | Checklist priorizado con dependencias, feature flags y responsables por módulo. | `docs/migracion-modulos-checklist.md`, tablero Kanban actualizado. |
| 4.2 Módulo piloto estabilizado | Módulo seleccionado (Noticias o Cursos) corriendo 100% sobre API, con fallback controlado. | Commits en `backend/` y `public/`, reporte de validación funcional. |
| 4.3 Migraciones iterativas | Módulos críticos (Inscripciones, Calificaciones, Asistencias, Finanzas) refactorizados y monitoreados. | PRs agrupadas por módulo, reportes de métricas y pruebas. |
| 4.4 Cierre de brechas | Endpoints y helpers adicionales implementados durante la fase, documentados en `docs/API.md` y pruebas actualizadas. | Actualizaciones en `docs/API.md`, `tests/`, ADRs sobre cambios relevantes. |
| 4.5 Informe de estado y soporte | Documentación de lecciones aprendidas, plan de soporte y runbooks de operación del modo API. | `docs/phase4-report.md`, runbooks en `docs/runbooks/`. |

## 3. Secuencia de trabajo
1. **Planificación y scope freeze**
   - Revisar el inventario de Fase 1 para clasificar módulos en tres oleadas: críticos (portal alumnos/docentes), medios (noticias, galería, contactos) y bajos (utilitarios, reportes secundarios).
   - Definir criterios de aceptación por módulo (paridad funcional, métricas de rendimiento, cobertura de pruebas).
   - Configurar feature toggles por módulo (`config/feature-flags.php`) y documentar cómo activarlos por ambiente.

2. **Ejecución del módulo piloto**
   - Seleccionar módulo con impacto medio y dependencias manejables (ej. Noticias).
   - Actualizar controladores y vistas (`backend/noticias/*`, `public/panelNoticias/*`) para consumir helpers API (`NoticiasService`).
   - Validar flujo end-to-end: autenticación, listados, CRUD, subida de archivos, cacheo y manejo de errores.
   - Registrar métricas base (latencia promedio, ratio de error, feedback de usuarios clave).

3. **Migraciones iterativas (oleadas)**
   - **Oleada 1 (críticos)**: Inscripciones (`backend/alumnos/*`), Calificaciones (`backend/calificaciones/*`), Asistencias (`backend/asistencias/*`), Finanzas/SPEI (`backend/pagos/*`).
     - Refactorizar scripts para usar `CursosService`, `AlumnosService`, `CalificacionesService`, `AsistenciasService`, `FinanzasService`.
     - Ajustar flujos especiales (reportes PDF/Excel) para consumir datos de la API y, si aplica, generar archivos en el servidor API.
   - **Oleada 2 (medios)**: Noticias, Galería (`backend/galeria/*`), Formularios de contacto (`public/contacto.php`), Contenido público (`public/pages/*`).
     - Incluir endpoints de carga de multimedia y notificaciones asociadas.
   - **Oleada 3 (bajos)**: Configuraciones (`backend/configuracion/*`), Dispositivos/notificaciones push, Integraciones externas (Google OAuth callbacks, webhooks).
   - Para cada módulo: ejecutar checklist (migrar helpers, reemplazar includes de `db.php`, actualizar validaciones, agregar pruebas), validar en staging y obtener sign-off.

4. **Gestión de brechas y soporte**
   - Documentar issues detectados (payloads faltantes, performance) y abrir tareas para el equipo API o del cliente PHP.
   - Implementar hotfixes coordinados manteniendo versionado semántico y notas de cambio.
   - Proveer soporte a usuarios finales durante ventanas de migración (comunicación y plan de contingencia).

5. **Cierre y transición a Fase 5**
   - Consolidar métricas de adopción y performance.
  - Desactivar toggles legacy en ambientes donde la migración esté completa.
  - Entregar informe final con pendientes residuales para hardening.

## 4. Backlog priorizado
1. **Preparación**
   - [x] Construir `docs/migracion-modulos-checklist.md` con estado inicial de cada módulo.
   - [x] Implementar `config/feature-flags.php` y helper `FeatureFlags::isEnabled($modulo)`.
   - [x] Configurar dashboards de monitoreo (Grafana/Datadog) con filtros por módulo y endpoint.

2. **Módulo piloto**
   - [x] Migrar `backend/noticias/index.php` y vistas asociadas para usar `NoticiasService`.
   - [x] Actualizar formularios de creación/edición de noticias para subir archivos vía API (`POST /api/noticias/{id}/media`).
   - [x] Escribir pruebas funcionales (PHPUnit + HTTP mocks) que cubran listados, altas, bajas y errores comunes.

3. **Oleada 1 (críticos)**
  - [x] Refactorizar `backend/alumnos/*.php` para usar `AlumnosService` y `InscripcionesService`.
    - ✅ Progresión anual (`public/users/admin/progresion.php`) y egresos masivos aplican `api_students_unenroll`/`api_students_enroll`, eliminando `alumno_curso` directo.
  - [x] Reemplazar consultas en `backend/calificaciones/*.php` por `CalificacionesService` y sincronizar formatos de exportación.
    - Se migraron las vistas de preceptoría a los servicios API reutilizando `GradesService`, `CoursesService`, `SubjectsService` y telemetría de asistencias.
    - La exportación de boletines ahora consume `ReportCardsService::export`, eliminando dependencias directas a MySQL.
    - ✅ Edición/publicación de boletines (`backend/users/preceptor/utils/*.php`) opera con `ReportCardsService`, sin `db.php`.
   - [x] Migrar `backend/asistencias/*.php` incorporando validaciones de conflictos que expone la API.
    - ✅ Reportes semanales y exportaciones CSV disponibles desde `public/users/preceptor/asistencias.php`, alimentados por `AttendanceService`.
  - [x] Actualizar `backend/pagos/*.php` para SPEI/finanzas, aprovechando endpoints de conciliación.
    - ✅ Nuevos flujos `listar_adeudos.php` y `obtener_pago.php` exponen adeudos y detalle de pagos vía `FinancesService`.
    - ✅ UI financiera en `public/users/admin/finanzas.php` integrada a los endpoints API con validaciones de formularios y conciliaciones controladas.

4. **Oleada 2 (medios)**
   - [x] Sustituir `backend/galeria/*.php` y `public/galeria/*.php` por `GaleriaService` con soporte de uploads chunked.
     - ✅ Las cargas mayores a 1 MB utilizan sesiones segmentadas contra `POST /gallery/uploads`, con fallback automático al modo base64.
   - [x] Refactorizar formularios de contacto (`public/contacto.php`) para enviar tickets vía `ContactosService`.
   - [x] Migrar páginas públicas (`public/pages/*.php`) para consumir `ContenidoService`.
     - ✅ Se habilitó el listado dinámico `/public/pages/index.php` y plantillas por slug (`historia`, `reglamento-interno`).

5. **Oleada 3 (bajos)**
   - [x] Migrar configuraciones y catálogos (`backend/configuracion/*.php`) para usar `ConfiguracionesService`.
     - ✅ Formularios de configuración por rol consumen `api_users_get`/`api_users_update_password` sin `backend/includes/db.php`.
   - [x] Actualizar scripts de dispositivos/notificaciones (`backend/notificaciones/*.php`) al nuevo endpoint.
     - ✅ Confirmaciones y publicaciones emplean `NotificationsService`, sin queries manuales.
   - [x] Integrar callbacks de Google OAuth en `public/oauth/google/callback.php` con `IntegracionesService`.
     - ✅ El callback expone feedback operativo y reintentos orientados a soporte.

6. **Calidad y seguimiento**
   - [x] Instrumentar métricas por módulo (latencia, errores, uso de fallback).
   - [x] Completar runbooks de operación y soporte L1/L2.
   - [x] Registrar decisiones y hallazgos en ADRs y retrospectivas.

## 5. Riesgos y mitigaciones
| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Feature flags mal configurados provocan indisponibilidad del módulo | Alto | Automatizar pruebas de toggles, definir valores por defecto seguros y monitorear durante despliegues. |
| Dependencias no detectadas entre módulos (ej. reportes compartidos) | Medio | Revisiones cruzadas antes de migrar cada oleada y smoke tests completos del portal. |
| Brechas tardías en la API causan bloqueos | Alto | Mantener war-room con equipo API, priorizar hotfixes y documentar contratos faltantes en `docs/API.md`. |
| Degradación de performance por múltiples llamadas API | Medio | Implementar caching por módulo, batching y monitoreo de latencia para reaccionar con ajustes en la API. |
| Resistencia del usuario final durante la transición | Medio | Comunicar cronograma, ofrecer canales de soporte y habilitar fallback temporal cuando sea necesario. |

## 6. Métricas de éxito
- ≥ 90% de los módulos inventariados operan en modo API sin depender de `db.php`.
- Tiempo medio de migración por módulo ≤ 3 días hábiles desde kickoff hasta validación en staging.
- Latencia P95 de operaciones críticas ≤ 500 ms tras la migración.
- Tasa de errores por módulo (5xx + errores negocio) < 1% durante el piloto en producción.
- Satisfaction score ≥ 8/10 en encuesta a usuarios claves después de cada oleada.

## 7. Entregables de documentación
- Checklist vivo por módulo con estado, responsables, fecha de migración y métricas observadas.
- Guías de rollback/rollforward ligadas a feature flags.
- Reporte de resultados de la Fase 4 (`docs/phase4-report.md`) con métricas comparativas pre/post migración.
- Actas de retrospectivas y ADRs que capturen decisiones o ajustes importantes.

El resultado de esta fase es un frontend/backoffice PHP operando mayoritariamente contra la API, dejando listo el terreno para la Fase 5 de hardening y limpieza final del código legacy.
