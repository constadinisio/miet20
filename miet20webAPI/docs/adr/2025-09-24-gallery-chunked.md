# ADR 008 · Galería con cargas chunked

- **Contexto:** Los uploads de la galería superan 5 MB y saturaban el endpoint base64 (`POST /gallery/items`).
- **Decisión:** Implementar sesiones de carga chunked (`/gallery/uploads`) desde el cliente PHP, con tamaño de bloque 512 KB y fallback automático al modo simple.
- **Consecuencias:**
  - Requiere monitorear `gallery.upload.fallbacks` para detectar incompatibilidades.
  - El cliente mantiene compatibilidad con ambientes que aún no exponen `uploads` gracias al fallback.
  - Se documentó plan de rollback en `docs/runbooks/fase4-operacion.md`.
