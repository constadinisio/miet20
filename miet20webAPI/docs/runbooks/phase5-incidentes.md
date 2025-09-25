# Runbook · Incidentes fase 5

## Alcance
Procedimientos de respuesta para incidentes comunes detectados luego del hardening de la fase 5. Cada sección incluye
los pasos para soporte L1, L2 y escalamiento a L3/SRE.

## Errores 5xx {#errores-5xx}
1. **L1:**
   - Validar estado de la API en dashboard `Phase5/API-Overview`.
   - Revisar últimos despliegues en canal `#deployments`.
   - Ejecutar `php scripts/smoke/api-health.php` y adjuntar resultado.
2. **L2:**
   - Revisar logs en Loki filtrando `level="error"` y `requestId` de la alerta.
   - Confirmar si hay cambios recientes en configuración (`config/api/*.php`).
   - Aplicar rollback con `scripts/deploy/api rollback --env=prod` si el incidente ocurre <30 min del último release.
3. **L3/SRE:**
   - Diagnóstico de base de datos (pool saturado, locks).
   - Coordinar comunicación con stakeholders y actualizar estado en Statuspage.

## Degradación de latencia {#degradacion-de-latencia}
1. **L1:**
   - Confirmar aumento en panel `Latency P95`.
   - Verificar si el cliente PHP presenta retries elevados (`Phase5/PHP-Client`).
2. **L2:**
   - Revisar métricas de Redis (hit rate) y Postgres (query time).
   - Incrementar temporalmente replicas de API (hasta +2) usando `kubectl scale`.
   - Notificar a negocio sobre posible degradación menor.
3. **L3:**
   - Ajustar índices SQL o invalidar caches globales.
   - Analizar traces en Tempo para detectar endpoints específicos.

## Exceso de reintentos {#exceso-de-reintentos}
1. **L1:**
   - Confirmar cantidad de reintentos en dashboard `Phase5/PHP-Client`.
   - Revisar healthcheck `php scripts/smoke/api-health.php` para asegurar disponibilidad de la API.
2. **L2:**
   - Inspeccionar logs de cliente PHP (`storage/logs/php-client.log`).
   - Revisar configuración de circuit breaker en `public/includes/apiClient.php`.
   - Reiniciar pool de workers si persisten los reintentos (>15 min).
3. **L3:**
   - Ajustar thresholds de circuit breaker y revisar dependencias externas.
   - Validar que no existan regresiones en despliegues recientes.

## Capacidad infraestructura {#capacidad-infra}
1. **L1:**
   - Verificar CPU/memoria en `Phase5/Infra-Health`.
   - Confirmar disponibilidad de nodos Kubernetes.
2. **L2:**
   - Ejecutar `kubectl describe pod api-*` para revisar throttling.
   - Agregar nodos mediante autoscaling manual si el cluster se acerca a 80% de uso.
   - Coordinar con operaciones para ampliar cuotas.
3. **L3:**
   - Revisar plan de capacidad trimestral.
   - Evaluar optimizaciones de código o caching adicional.

## Procedimiento de rollback {#rollback}
- Script principal: `scripts/deploy/api` con opciones `rollback` y `status`.
- Validar integridad de migraciones ejecutando `php artisan migrate:status` en la API.
- Confirmar reactivación de feature flags legacy en caso de fallback.

## Comunicación
- Actualizar siempre canal `#incident-bridge` con progreso.
- Documentar incidente en Jira usando plantilla `INC-POSTMORTEM` dentro de 48 h.
