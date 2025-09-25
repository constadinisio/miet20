# Guía de despliegue de la API MIET20

Esta guía resume los pasos mínimos para preparar un entorno de ejecución de la API REST (`/api`) y dejarla lista para que el frontend PHP migrado consuma sus endpoints. Los pasos están pensados para entornos **staging** y **producción**, pero pueden reutilizarse en desarrollo cambiando las credenciales.

## 1. Requisitos previos

- **Node.js 20** o superior y `npm`.
- **Base de datos MySQL 8** (también se soporta PostgreSQL ajustando `DB_CLIENT`).
- Acceso a una cuenta de servicio de **Google Cloud** para OAuth (client ID).
- Espacio de almacenamiento accesible para archivos públicos (por defecto el repositorio guarda en disco en `public/`).

## 2. Variables de entorno

Crear un archivo `api/.env` tomando como referencia la siguiente tabla. Los valores marcados con `*` son obligatorios.

| Variable | Descripción |
|----------|-------------|
| `NODE_ENV` | `development`, `staging` o `production`. Afecta logs y manejo de errores. |
| `PORT` | Puerto HTTP donde se expondrá la API (por defecto 4000). |
| `APP_URL` | URL pública de la API (ej. `https://api.et20.edu`). |
| `DB_CLIENT` | `mysql2` (default) o `pg`. |
| `DB_HOST`* | Host o IP del servidor de base de datos. |
| `DB_PORT` | Puerto de la base de datos (3306 MySQL / 5432 PostgreSQL). |
| `DB_USER`* | Usuario con permisos de lectura/escritura. |
| `DB_PASSWORD`* | Contraseña del usuario. |
| `DB_NAME`* | Base de datos principal de MIET20. |
| `DB_NAME_TEST` | Base de datos auxiliar usada en `npm test`. |
| `JWT_SECRET`* | Clave usada para firmar los access tokens. |
| `JWT_EXPIRATION` | Duración de los access tokens (ej. `1h`, `12h`). |
| `REFRESH_TOKEN_SECRET`* | Clave para firmar refresh tokens. |
| `REFRESH_TOKEN_EXPIRATION` | Duración de los refresh tokens (ej. `7d`). |
| `GOOGLE_CLIENT_ID`* | Client ID de OAuth 2.0 configurado en Google Cloud. |
| `GOOGLE_OAUTH_AUDIENCE` | Lista de audiencias válidas separadas por coma. |
| `GOOGLE_HOSTED_DOMAIN` | Dominio permitido para cuentas institucionales (opcional). |

> 💡 La API almacena portadas de noticias, archivos adjuntos y elementos de galería en el propio repositorio (`public/panelNoticias/images`, `public/gallery/items`, `api/storage/uploads`). Asegurate de que el usuario del proceso Node.js tenga permisos de escritura y de servir estos directorios mediante el servidor web (Nginx/Apache) como estáticos.

## 3. Instalación

1. Clonar el repositorio y entrar a la carpeta `api/`.
2. Instalar dependencias: `npm install`.
3. Ejecutar migraciones de base: `npm run migrate` (usa las credenciales de `api/.env`).
4. (Opcional) Cargar datos semilla si el entorno los requiere.

## 4. Puesta en marcha

- **Desarrollo**: `npm run dev` levanta el servidor con `nodemon` y recarga ante cambios.
- **Producción/Staging**: usar `npm start` o administrar el proceso con `pm2`, `systemd` o Docker.

Se recomienda colocar un **reverse proxy** (Nginx) que maneje HTTPS, sirva archivos estáticos y reenvíe todo `/api` al puerto configurado en `PORT`.

## 5. Salud y monitoreo

- Confirmar que `GET /api/health` responda `200` tras el despliegue.
- Revisar los logs rotativos generados por Winston (`logs/application.log`) para detectar errores.
- Exponer métricas básicas (`/metrics`) si se integra Prometheus; la API ya dispone de hooks para instrumentación vía middlewares.

## 6. Pruebas y verificación

Antes de habilitar el entorno, ejecutar:

```bash
npm run lint
npm test
```

Para staging/producción, duplicar la configuración de `.env` en un fichero seguro del servidor CI/CD y ejecutar los mismos comandos. La especificación `api/openapi.yaml` centraliza el contrato para automatizar pruebas de humo, mientras que las colecciones Postman ubicadas en `docs/postman/` permiten validar rápidamente los endpoints críticos (autenticación, usuarios, asistencias, noticias, galería, archivos).

## 7. Consideraciones finales

- Mantener respaldos periódicos de la base de datos y de los directorios de archivos públicos.
- Rotar periódicamente `JWT_SECRET` y `REFRESH_TOKEN_SECRET` (sincronizando la rotación con el frontend para evitar cierres de sesión masivos).
- Registrar en el gestor de secretos de la organización (Vault, AWS Secrets Manager, etc.) todas las credenciales sensibles y evitar exponer `.env` en repositorios.

Con estos pasos completados, la API queda lista para soportar los módulos migrados en la Fase 3.
