# Guía de onboarding · Cliente API + PHP

## Introducción
Este documento resume los pasos para que nuevos integrantes comprendan la arquitectura post migración y puedan operar el
cliente PHP respaldado por la API.

## Checklist de primer día
- Revisar `docs/migration-plan.md` secciones 3 y 5.
- Ejecutar entorno local siguiendo `docs/deployment-guide.md`.
- Correr `scripts/check-legacy-mysql.sh` para familiarizarse con validaciones.
- Leer runbooks clave (`docs/runbooks/phase5-incidentes.md`, `docs/runbooks/playbook-guardias.md`).

## Flujo de autenticación
1. Solicitar credenciales SSO y registrar aplicación en portal de seguridad.
2. Configurar variables en `.env` (`API_BASE_URL`, `API_CLIENT_ID`, `API_CLIENT_SECRET`).
3. Validar login ejecutando `php public/login.php --check` (modo CLI de verificación).

## Buenas prácticas
- No consumir bases de datos directamente; siempre usar `public/includes/apiClient.php`.
- Registrar métricas personalizadas usando helper `Observability::recordMetric`.
- Ante dudas, utilizar canal `#api-guild` y buscar en Confluence (`API > Knowledge Base`).

## Recursos adicionales
- Video introductorio: `https://media.example.com/watch/api-onboarding`.
- Tableros Grafana: ver `docs/observability/phase5-dashboards.md`.
- Ejemplos de integraciones en `docs/API.md`.
