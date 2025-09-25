# Legacy MySQL helpers (archivado)

Este documento resume los componentes retirados durante la fase 5 de hardening.

## ¿Qué se archivó?
- `backend/includes/db.php` y los scripts que dependían de consultas directas con `mysqli`.
- Procedimientos internos que describían accesos directos a MySQL desde el backoffice PHP.

## Estado actual
- Todas las pantallas administrativas consumen la API REST mediante el cliente PHP (`public/includes/apiClient.php`).
- La API provee endpoints para cursos, materias, horarios, calificaciones y reportes académicos.
- Las políticas de hardening (rate limiting, antivirus, circuit breaker) se documentan en `docs/API.md` y `config/`.

## Acción requerida
- Cualquier referencia histórica a los scripts legacy debe consultarse en este archivo.
- Las guías operativas y runbooks deben enlazar únicamente a los flujos API vigentes.
