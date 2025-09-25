# ADR 009 · Páginas públicas desde ContenidoService

- **Contexto:** Las páginas institucionales se mantenían como HTML estático con riesgo de desalineación frente a la API.
- **Decisión:** Consumir `GET /public/pages` y `GET /public/pages/{slug}` mediante `ContenidoService`, con plantillas PHP reutilizables.
- **Consecuencias:**
  - Se habilita versionado y telemetría del contenido público.
  - Los entornos sin API pueden desactivar `FEATURE_FLAG_PAGINAS_PUBLICAS_API_MODE` para volver al backup estático.
  - Se agrega dependencia de disponibilidad del endpoint `ContenidoService`; runbook cubre recuperación.
