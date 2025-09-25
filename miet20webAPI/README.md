# MIET20 Web & API

Este repositorio contiene el código legado en PHP de la plataforma escolar MIET20 y un nuevo esqueleto de API RESTful en Node.js (Express) diseñado para desacoplar la lógica de negocio del frontend.

## Estructura

```
backend/          # Código PHP actual (se mantiene para referencia)
public/           # Recursos públicos de la aplicación existente
api/              # Nueva API RESTful (Express + Knex + JWT)
docs/             # Documentación de endpoints y arquitectura
```

## API Node.js

- **Stack**: Express, Knex, JWT, Winston, Helmet, CORS.
- **Autenticación**: JWT con refresh tokens.
- **Versionado**: `/api/v1`.
- **Módulos cubiertos**: usuarios, alumnos, cursos, materias, horarios, asistencias, calificaciones, trabajos, notificaciones, configuración institucional y dispositivos SPEI.
- **Documentación**: [docs/API.md](docs/API.md).

### Puesta en marcha

```bash
cd api
cp .env.example .env
npm install
npm run dev
```

## Próximos pasos

1. Implementar migraciones y modelos sobre la base de datos definitiva (PostgreSQL recomendado).
2. Completar los servicios con reglas de negocio específicas y añadir pruebas automatizadas (Jest/Supertest).
3. Generar clientes (Java, Android, iOS) reutilizando el contrato JSON de la API y JWT.
4. Incorporar herramientas de observabilidad y documentación OpenAPI.

## Licencia

MIT (o la que defina el proyecto original).
