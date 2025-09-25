# Dashboards Grafana · Fase 5

## Resumen
Se publicaron tres tableros en la carpeta `Phase5/Operacion` apuntados al stack Grafana + Prometheus. Cada dashboard
consolida métricas clave por módulo y está referenciado en los acuerdos de nivel de servicio.

## Tableros
### 1. `Phase5/API-Overview`
- **Paneles principales:**
  - Latencia P50/P95/P99 de endpoints críticos (`inscripciones`, `calificaciones`, `asistencia`).
  - Ratio de errores por categoría (4xx, 5xx) con breakdown por servicio.
  - Uso de caché Redis (hits/misses) y tiempos de respuesta de Postgres.
- **Variables dinámicas:** ambiente (`staging`, `produccion`), módulo, versión de despliegue.
- **Alertas vinculadas:** `api-error-rate` y `api-latency-slo`.

### 2. `Phase5/PHP-Client`
- Monitorea el cliente PHP en Apache/Nginx.
- **Métricas:** tiempo de renderizado por plantilla, consumo de memoria por worker, cantidad de retries activados por el
  circuit breaker, distribución de códigos HTTP recibidos desde la API.
- **Panel adicional:** seguimiento de latencia del login SSO con comparativa semanal.

### 3. `Phase5/Infra-Health`
- **Contenido:** recursos de CPU, memoria y disco de los pods Kubernetes, estado de readiness/liveness probes, conexiones
  activas en el pool de la API.
- **Integración:** enlace directo a logs en Loki y a traces en Tempo para facilitar investigaciones.

## Control de acceso
- Los tableros están compartidos con el grupo `soporte-app` (solo lectura) y `sre-core` (edición).
- SSO obligatorio y renovación de tokens de acceso cada 12 h.

## Mantenimiento
- Revisar mensualmente la vigencia de queries y paneles.
- Documentar cambios significativos en `docs/observability/changelog.md`.
- Exportar snapshots post deploy mayor para referencia histórica.
