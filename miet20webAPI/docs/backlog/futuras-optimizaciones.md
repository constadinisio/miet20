# Backlog de optimizaciones futuras

## Prioridad Alta
1. **Reportes analíticos en tiempo real**
   - Implementar endpoints agregados para dashboards académicos.
   - Evaluar streaming hacia sistema BI existente.
2. **Autoscaling inteligente**
   - Incorporar métricas de latencia y colas al algoritmo de escalado.
   - Requiere PoC con KEDA + Prometheus Adapter.

## Prioridad Media
1. **Mejoras UX en inscripciones**
   - Simplificar formulario con autocompletado.
   - Añadir indicadores de progreso en pasos múltiples.
2. **Notificaciones push**
   - Integrar servicio Web Push para avisos de asistencia.
   - Definir opt-in claro y persistencia en la API.

## Prioridad Baja
1. **Integración con sistema de biblioteca**
   - Explorar API externa y definir alcance.
2. **Refactor de módulos legacy archivados**
   - Analizar viabilidad de migrar reportes históricos a nueva arquitectura.

## Seguimiento
- Responsable de grooming: Product Owner (María L.).
- Reuniones quincenales para priorización.
- Registrar avances en Jira tablero `API-Continuous-Improvement`.
