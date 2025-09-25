# Runbook · Operación módulos fase 4

## Galería institucional
- **Objetivo:** asegurar cargas chunked estables sobre `/gallery/uploads`.
- **KPIs a monitorear:**
  - `gallery.upload.duration_ms` (P95 < 1200 ms).
  - `gallery.upload.mode` (esperado `chunked` > 90% para archivos > 1 MB).
  - `gallery.upload.fallbacks` (alertar si > 3 eventos/hora).
- **Validación rápida:**
  1. Revisar `storage/logs/api-metrics.ndjson` filtrando `module="galeria"` y `operation="upload_chunk"`.
  2. Confirmar que el dashboard muestre el flag `fallback=false` en la mayoría de eventos.
  3. Ejecutar `php scripts/smoke/gallery-upload.php sample.jpg` (script manual) y validar respuesta 201.
- **Plan de rollback:**
  - Activar variable `FEATURE_FLAG_GALERIA_LEGACY_DB_MODE=1` temporalmente.
  - Publicar comunicado a usuarios para reintentar cargas cuando finalice la ventana.

## Páginas públicas (`ContenidoService`)
- **Objetivo:** garantizar la entrega dinámica de contenido institucional.
- **Tareas L1:**
  - Ante errores 5xx en `/public/pages/*`, limpiar cache CDN y reintentar.
  - Validar conectividad haciendo `php scripts/smoke/content-page.php historia` (esperado HTTP 200 y JSON con `data.slug`).
- **Monitoreo:** métricas `content.pages.render.duration_ms` y `content.pages.errors` en Datadog.
- **Rollback:** cambiar toggle `FEATURE_FLAG_PAGINAS_PUBLICAS_LEGACY_DB_MODE=1` y publicar estáticos desde `public/pages/static/*` (mantener actualizado el backup).

## Integraciones Google OAuth
- **Uso:** vinculación administrativa vía `public/oauth/google/callback.php`.
- **Procedimiento ante fallo:**
  1. Verificar variable `GOOGLE_REDIRECT_URI` en `config/.env` y credenciales del proyecto.
  2. Reejecutar el flujo y revisar la respuesta del callback (muestra JSON de la API para soporte).
  3. Si persiste, abrir ticket al backend adjuntando `requestId` (presente en la respuesta JSON).
- **Automatización sugerida:** programar `php scripts/smoke/google-oauth.php --dry-run` semanalmente para validar permisos.

## Checklist post despliegue
- [ ] Ejecutar `composer test` y `composer analyse` sin errores.
- [ ] Verificar tabla `docs/migracion-modulos-checklist.md` actualizada.
- [ ] Confirmar que `docs/adr/` tenga decisiones vigentes firmadas por el arquitecto asignado.
