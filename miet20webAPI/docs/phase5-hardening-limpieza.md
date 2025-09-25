# Fase 5 · Hardening, limpieza y cierre operativo

La Fase 5 consolida la migración completada en la Fase 4 removiendo definitivamente el código legacy dependiente de MySQL,
robusteciendo la plataforma API+PHP frente a incidentes y dejando listos los artefactos de operación continua. Esta etapa
asegura que el frontend/backoffice PHP opere exclusivamente mediante el cliente API y que la API cumpla los acuerdos de
nivel de servicio antes del traspaso formal a operación.

## 1. Objetivos específicos
- **Eliminar dependencias legacy**: retirar helpers de base de datos, librerías o scripts obsoletos y asegurar que no existan
  rutas que invoquen directamente consultas SQL.
- **Fortalecer la plataforma**: ejecutar hardening de seguridad, optimización de performance y ajustes de resiliencia tanto
en la API como en el cliente PHP.
- **Formalizar la operación**: entregar runbooks, monitoreo, métricas de SLO/SLA y checklists de despliegue/rollback validados.
- **Cerrar la transición**: documentar el estado final, capacitar a los equipos de soporte y definir un plan de mejora continua.

## 2. Entregables
| Hito | Resultado | Artefactos |
|------|-----------|------------|
| 5.1 Depuración legacy | Código PHP sin referencias a `backend/includes/db.php` ni helpers SQL, directorios legacy archivados o eliminados. | Commits de limpieza, reporte de búsqueda (`grep`/`phpstan`) certificando ausencia de llamadas directas. |
| 5.2 Hardening API y cliente | Auditoría de seguridad y performance aplicada, endpoints críticos optimizados, políticas de rate limit y retries documentadas. | Reportes de pentest, resultados de pruebas de carga, configuración actualizada en `config/`. |
| 5.3 Observabilidad y operación | Dashboards, alertas, runbooks y procedimientos de escalamiento validados en staging/producción. | Grafana/Datadog dashboards, `docs/runbooks/*.md`, checklist de guardias. |
| 5.4 Aceptación y cierre | Acta de aceptación del negocio, plan de mejora continua y backlog de optimizaciones post-migración. | `docs/phase5-report.md`, actas de capacitación y backlog residual priorizado. |

## 3. Secuencia de trabajo
1. **Scope freeze y auditoría final**
   - Congelar cambios funcionales; sólo se atienden defectos y tareas de hardening.
   - Ejecutar auditorías de código: buscar `require 'backend/includes/db.php'`, instancias de `mysqli_*`, queries SQL embebidas.
   - Revisar el cliente API para garantizar manejo centralizado de tokens, timeouts y errores.

2. **Depuración y refactorización residual**
   - Eliminar archivos legacy no utilizados (`backend/reportesLegacy/`, scripts duplicados) o moverlos a un repositorio de archivo.
   - Reemplazar cualquier helper local pendiente por funciones del cliente API; actualizar tests de integración.
   - Actualizar pipelines CI/CD para remover jobs de base de datos legacy, ajustando despliegues a la arquitectura API.

3. **Hardening de seguridad y performance**
   - Revisar políticas de autenticación/autorizarción: rotación de claves, expiración de tokens, mínimos privilegios.
   - Configurar rate limiting y circuit breakers en la API; revisar reintentos y backoff exponencial en el cliente PHP.
   - Ejecutar pruebas de carga (ej. k6, Artillery) sobre endpoints críticos y aplicar optimizaciones (indexado, caching HTTP).
   - Completar revisión OWASP (input validation, file uploads, manejo de errores) y documentar acciones correctivas.

4. **Observabilidad, operación y soporte**
   - Consolidar dashboards de métricas (latencia P50/P95, ratio de errores, throughput) y alertas de umbrales.
   - Crear runbooks para incidentes frecuentes (timeouts API, errores de autenticación, degradación de performance).
   - Definir procesos de escalamiento entre equipos (Niveles L1/L2/L3) y actualizar canales de soporte.

5. **Cierre y transición**
   - Ejecutar regresión funcional completa en staging y smoke tests en producción tras activar el modo API al 100%.
   - Capacitar a soporte y usuarios clave en la nueva operativa, incluyendo procedimientos de contingencia.
   - Firmar acta de aceptación con stakeholders, listar mejoras futuras y establecer cadencia de retros continuas.

## 4. Backlog priorizado
1. **Auditoría y limpieza**
   - [x] Automatizar script que detecte `mysqli_query` o accesos directos a MySQL en `backend/` y `public/`.
  - [x] Eliminar `backend/includes/db.php` y publicar nota de deprecación.
  - [x] Archivar documentación antigua que refiera al modo legacy, actualizando README y guías internas.

2. **Hardening técnico**
  - [x] Implementar rate limiting configurable en la API (`config/security/rate-limit.js`).
  - [x] Agregar circuit breaker y retries con backoff en `public/includes/apiClient.php`.
   - [x] Ejecutar pruebas de carga de 1h sobre endpoints críticos (inscripciones, calificaciones, asistencia) con reportes.
  - [x] Reforzar validaciones de uploads (antivirus, tamaño, extensión) en endpoints de galería y noticias.

3. **Observabilidad y operación**
   - [x] Publicar dashboards en Grafana/Datadog con paneles por módulo (latencia, errores, uso de caché).
   - [x] Definir alertas paginadas para disponibilidad < 99.5% o ratio de errores > 1%.
   - [x] Documentar runbooks y procedimientos de rollback en `docs/runbooks/`.
   - [x] Actualizar playbooks de guardia y rotaciones de soporte.

4. **Adopción y mejora continua**
   - [x] Ejecutar capacitación final para equipos internos y generar material de onboarding.
   - [x] Preparar encuesta de satisfacción y recopilar feedback post-migración.
   - [x] Definir backlog de optimizaciones futuras (mejoras UX, reporting avanzado, nuevas integraciones) con prioridades.
   - [x] Programar retrospectiva de cierre y publicación de lecciones aprendidas.

## 5. Riesgos y mitigaciones
| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Código legacy residual provoca fallbacks inadvertidos | Alto | Automatizar escaneos en CI, bloquear merges con referencias a helpers legacy, revisión manual cruzada. |
| Brechas de seguridad descubiertas tarde | Alto | Ejecutar pentests tempranos, usar checklists OWASP y remediar antes del freeze productivo. |
| Cuellos de botella de performance post-migración | Medio | Pruebas de carga iterativas, tuning de consultas en la API, habilitar caching y observabilidad proactiva. |
| Falta de preparación operativa | Alto | Runbooks detallados, simulacros de incidentes, capacitación obligatoria para guardias y soporte. |
| Resistencia del negocio a adoptar el nuevo modelo | Medio | Comunicación continua, demos, documentación clara y soporte cercano en go-live. |

## 6. Métricas de éxito
- 0 referencias a `db.php`, `mysqli_*` u otros accesos directos a MySQL en el repositorio principal.
- ≥ 99.5% de disponibilidad mensual de la API y del frontend durante el período de estabilización.
- Latencia P95 de endpoints críticos ≤ 400 ms tras las optimizaciones.
- ≤ 1% de incidentes de severidad alta en las primeras 4 semanas posteriores al go-live completo.
- ≥ 90% de satisfacción en encuestas de usuarios clave y equipos internos respecto a la transición.

## 7. Entregables de documentación
- Reporte final de la Fase 5 (`docs/phase5-report.md`) con estado de limpieza, métricas y acciones de hardening aplicadas.
- Runbooks y playbooks en `docs/runbooks/` para operación, monitoreo, incidentes y mantenimiento.
- Checklist de despliegue/rollback actualizado (`docs/checklists/deploy-api-migrado.md`).
- Inventario de mejoras futuras y backlog residual priorizado compartido en herramienta de gestión (Jira/Trello).
- Actas de capacitación y comunicaciones de go-live enviadas a stakeholders.

## 8. Notas de actualización
- Se archivaron los procedimientos legacy vinculados a MySQL en `docs/archive/legacy-mysql.md`, dejando constancia del retiro de `backend/includes/db.php` y concentrando la documentación vigente en los flujos API.

El cierre exitoso de esta fase deja la plataforma estabilizada, observada y gobernada, con los equipos preparados para operar y
seguir evolucionando el ecosistema basado en la API.
