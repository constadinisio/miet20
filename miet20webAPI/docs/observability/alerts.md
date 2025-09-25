# Alertas operativas · Fase 5

## Configuración general
- Herramienta: Grafana Alerting + canal secundario en PagerDuty (`servicio: api-miet20`).
- Silencios permitidos únicamente por rol L2 o superior y por máximo 2 horas.
- Cada alerta incluye enlace directo a los runbooks documentados.

## Alertas activas
### `api-error-rate`
- **Condición:** ratio de errores 5xx > 1% durante 5 minutos.
- **Fuente:** métrica `api_requests_total{status=~"5.."}` / `api_requests_total`.
- **Acción:** notifica canal `#oncall-api` y crea incidente P2 en PagerDuty.
- **Runbook:** `docs/runbooks/phase5-incidentes.md#errores-5xx`.

### `api-latency-slo`
- **Condición:** Latencia P95 > 400 ms durante 10 minutos.
- **Fuente:** `histogram_quantile(0.95, sum(rate(api_request_duration_seconds_bucket[5m])) by (le, endpoint))`.
- **Acción:** abre ticket automático en Jira `SRE` y alerta vía SMS a guardia primaria.
- **Runbook:** `docs/runbooks/phase5-incidentes.md#degradacion-de-latencia`.

### `php-client-retries`
- **Condición:** más de 30 reintentos/minuto registrados por el cliente PHP.
- **Fuente:** log `php_client_retries_total` recolectado por Promtail.
- **Acción:** mensaje en Slack `#oncall-php` y correo a coordinación de soporte.
- **Runbook:** `docs/runbooks/phase5-incidentes.md#exceso-de-reintentos`.

### `infra-capacity`
- **Condición:** uso de CPU > 85% o memoria > 80% por 15 minutos en pods de API.
- **Fuente:** métricas `container_cpu_usage_seconds_total` y `container_memory_working_set_bytes`.
- **Acción:** PagerDuty P1 y creación de issue en backlog SRE.
- **Runbook:** `docs/runbooks/phase5-incidentes.md#capacidad-infra`.

## Procedimiento de revisión
- Reunión semanal SRE + Soporte para analizar alertas disparadas.
- Ajuste de umbrales y ventanas en función de tendencias.
- Registro de cambios en `docs/observability/changelog.md`.
