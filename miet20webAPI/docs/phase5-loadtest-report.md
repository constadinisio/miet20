# Informe de pruebas de carga · Fase 5

## Resumen ejecutivo
- **Herramienta utilizada:** k6 0.46.0 ejecutado desde runner autoservicio con red 1 Gbps.
- **Duración total:** 60 minutos de prueba sostenida con rampa de 5 minutos.
- **Escenario:** mezcla 40% inscripciones, 35% calificaciones, 25% asistencia.
- **Resultado general:** sin errores críticos, latencias dentro de los SLO definidos luego de los ajustes de caché y pooling.

## Configuración
- **Usuarios virtuales pico:** 350 (rampa desde 50 VUs).
- **Tasa objetivo:** 180 rps estabilizados, con picos de 220 rps durante el segundo bloque.
- **Ambiente:** staging API v5.3.1 con caché Redis habilitado y base Postgres replicada.
- **Autenticación:** token de servicio con vigencia de 2h, renovado automáticamente por el escenario de prueba.
- **Protecciones activas:** rate limiting 300 rps por IP, circuit breaker en el cliente PHP (retry máximo 2).

## Métricas observadas
| Métrica | Inscripciones | Calificaciones | Asistencia |
|--------|---------------|----------------|------------|
| Latencia P50 | 180 ms | 160 ms | 145 ms |
| Latencia P95 | 340 ms | 310 ms | 295 ms |
| Latencia P99 | 410 ms | 380 ms | 360 ms |
| Errores HTTP 5xx | 0 | 0 | 0 |
| Errores HTTP 4xx | 8 (tokens expirados en rampa) | 3 (payload inválido) | 2 (asistencia duplicada) |
| Throughput promedio | 72 rps | 64 rps | 44 rps |

## Hallazgos clave
1. **Expiración de tokens durante la rampa** solucionada agregando renovación anticipada 60 s antes del vencimiento.
2. **Payload inválido en calificaciones** corregido actualizando fixtures de prueba con IDs activos.
3. **Validación de duplicados en asistencia** reforzada retornando error 409 manejado por el cliente PHP.
4. El uso de caché en `/inscripciones/listar` redujo P95 en 18% respecto a la prueba piloto de la Fase 4.

## Acciones posteriores
- Automatizar ejecución semanal con pipeline `ci/loadtest-phase5` y publicar resultados en `storage/reports/loadtests/`.
- Ajustar alertas de error rate para que ignoren 4xx esperados durante rampas.
- Mantener revisión mensual de índices en tablas `inscripciones` y `asistencias` para sostener P95 < 350 ms.

## Evidencias
- Artefactos k6 exportados en `storage/reports/loadtests/2024-06-ensayo/`.
- Capturas de dashboards en Grafana carpeta `Phase5/Loadtest-2024-06`.
- Ticket `OBS-221` con validación de soporte sobre el comportamiento durante la prueba.
