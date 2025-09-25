# Runbooks operativos · Fase 3 cliente PHP

## Propósito
Estos runbooks documentan los procedimientos de soporte continuo para el cliente PHP unificado introducido en la Fase 3. Cubren monitoreo, respuesta a incidentes frecuentes y tareas de mantenimiento preventivo alineadas con la capa `App\Api`.

## Monitoreo y tableros
- **Fuente de métricas:** `storage/logs/api-metrics.ndjson` genera eventos en formato NDJSON por cada request (latencia, status, reintentos) y alertas del `TokenExpirationMonitor` (`path = tokens/expiration`).
- **Ingesta recomendada:** configurar Filebeat/Fluentd para leer el archivo y enviarlo a ELK o Prometheus. Cada línea contiene un objeto JSON listo para parseo.
- **Indicadores clave:**
  | Métrica | Descripción | Umbral/alerta |
  |---------|-------------|----------------|
  | `status` | Código HTTP devuelto por la API | Alertar con ≥5% de 5xx en 5 minutos |
  | `durationMs` | Tiempo total del request | Alertar cuando promedio > 400 ms |
  | `context.state` | `expiring` o `expired` según el monitor de tokens | Alertar inmediatamente en `expired`; abrir ticket cuando haya ≥3 `expiring` en una hora |

## Procedimientos
### Rotación de credenciales de servicio
1. Solicitar nuevas credenciales al equipo de identidad.
2. Actualizar `config/api.php` (o variables de entorno) en los entornos afectados.
3. Desplegar la configuración y limpiar caches (`php artisan config:clear` si aplica).
4. Validar en logs que las llamadas de autenticación retornen 200 y que el monitor no genere alertas `expired`.
5. Documentar la rotación en el registro de cambios operativo.

### Recuperación ante tokens expirados
1. Revisar en el dashboard los eventos `tokens/expiration` con estado `expired`.
2. Pedir al usuario afectado que vuelva a iniciar sesión. Si es un servicio, invocar `api_clear_stored_tokens()` y `api_store_tokens()` con credenciales válidas.
3. Confirmar que el siguiente request exitoso registra un evento `expiring` con `secondsRemaining` positivo.
4. Si el problema persiste, escalar a backend para revisar el endpoint `auth/refresh-token`.

### Manejo de timeouts y reintentos
1. Cuando se detecte una ráfaga de 503/504, verificar en el dashboard el valor de `context.reason` (`retryable_status` o `refresh_token`).
2. Ajustar temporalmente `retry_delay_ms` en `config/api.php` si la incidencia es masiva.
3. Registrar el incidente en el tablero de problemas conocidos y notificar al equipo API.

### Validación del workflow `PHP Client CI`
1. Crear una rama de prueba y abrir un Pull Request dummy.
2. Verificar en GitHub Actions que el job **PHP Client CI** ejecute `composer test` y `composer analyse` sin errores.
3. Ante fallas por dependencias, limpiar el cache del runner y reintentar.

## Checklist semanal
- [ ] Revisar métricas de latencia y tasa de error en el dashboard centralizado.
- [ ] Confirmar que no existan eventos `tokens/expiration` con estado `expired` sin resolver.
- [ ] Ejecutar `composer test` localmente en la rama principal para validar compatibilidad.
- [ ] Actualizar el registro de rotación de credenciales si hubo cambios en la semana.
