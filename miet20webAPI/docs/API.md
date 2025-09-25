# API RESTful MIET20

## Visión general

Esta API expone de manera desacoplada las funcionalidades existentes en la plataforma web escolar MIET20 para que puedan ser consumidas por clientes web, de escritorio y móviles. La especificación toma como referencia los flujos implementados actualmente en PHP para gestión de cursos, materias, usuarios, calificaciones, asistencias, notificaciones y dispositivos SPEI.【F:backend/users/admin/utils/admin_crear_materia.php†L1-L47】【F:backend/users/profesor/profesor_cargar_nota.php†L1-L51】【F:backend/users/profesor/guardar_asistencias_materia.php†L1-L120】【F:backend/notificaciones/listar.php†L1-L26】

- **Base URL**: `https://<dominio>/api/v1`
- **Autenticación**: JWT (`Authorization: Bearer <token>`)
- **Formato**: JSON
- **Versionado**: Prefijo `/api/v1` para permitir evolución futura (GraphQL, microservicios, nuevas versiones).

## Formato de respuestas

Todas las respuestas HTTP siguen un contrato homogéneo compuesto por cuatro claves principales:

- `status`: indica si la operación fue exitosa (`success`) o produjo un error (`error`).
- `data`: contiene la carga útil devuelta por el endpoint (o `null` cuando no corresponde contenido).
- `errors`: arreglo con los errores detectados. En respuestas exitosas se envía explícitamente `null`.
- `meta`: metadatos adicionales asociados a la petición (por ejemplo, `requestId` para trazabilidad).

Ejemplo (éxito):

```json
{
  "status": "success",
  "data": {
    "example": true
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

Ejemplo (error):

```json
{
  "status": "error",
  "data": null,
  "errors": [
    {
      "code": "validation_error",
      "message": "El campo email es obligatorio"
    }
  ],
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Arquitectura propuesta

```
api/
  src/
    config/          # configuración de entorno y conexión a BD
    controllers/     # lógica HTTP -> servicios
    middlewares/     # autenticación, validaciones y manejo de errores
    repositories/    # acceso a datos con Knex (PostgreSQL por defecto)
    routes/v1/       # definición de endpoints versionados
    services/        # reglas de negocio reutilizables
    utils/           # utilidades (logger, ApiError, etc.)
    validators/      # validaciones con express-validator
  .env.example       # variables de entorno sugeridas
```

### Recomendación de base de datos

Se propone **PostgreSQL** como motor principal por su robustez, soporte a JSONB, particiones y replicación nativa, lo que facilita escalar lecturas y análisis de datos académicos. El uso de Knex permite migrar a otros motores si fuese necesario. Para funcionalidades transaccionales críticas (notas, asistencias) se sugiere agregar índices compuestos y auditoría temporal. Para escalar verticalmente a microservicios, puede combinarse con colas (RabbitMQ/Kafka) y, eventualmente, expandir hacia almacenes especializados (por ejemplo, Redis para caché de horarios).

## Autenticación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Autentica usuarios existentes (admin, preceptor, profesor, spei) y devuelve tokens. |
| POST | `/auth/google-login` | Completa el login sin contraseña para cuentas aprobadas registradas vía Google utilizando `idToken`. |
| POST | `/auth/refresh-token` | Genera un nuevo access token usando el refresh token. |
| POST | `/auth/switch-role` | Recalcula la sesión JWT con el rol activo solicitado por el usuario autenticado. |
| POST | `/auth/logout` | Revoca el refresh token actual y agrega el access token a la lista negra (opcionalmente cierra todas las sesiones). |

**Ejemplo (login)**
```bash
curl -X POST https://api.miet20.edu/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "profesor@et20.edu", "password": "Secreta123"}'
```

Respuesta:
```json
{
  "status": "success",
  "data": {
    "user": {
      "id": "b6f7...",
      "email": "profesor@et20.edu",
      "role": "profesor",
      "primaryRole": {
        "id": 3,
        "nombre": "Profesor"
      },
      "roles": [
        {
          "id": 3,
          "nombre": "Profesor"
        }
      ],
      "permissions": {
        "noticias": false,
        "galeria": false,
        "attp": false
      },
      "status": "active"
    },
    "tokens": {
      "accessToken": "...",
      "refreshToken": "...",
      "expiresIn": "1d"
    }
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

**Ejemplo (cambiar rol activo)**
```bash
curl -X POST https://api.miet20.edu/api/v1/auth/switch-role \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleId": 5}'
```

Respuesta:
```json
{
  "status": "success",
  "data": {
    "user": {
      "id": "b6f7...",
      "email": "profesor@et20.edu",
      "role": "spei",
      "primaryRole": {
        "id": 5,
        "nombre": "SPEI"
      },
      "roles": [
        { "id": 5, "nombre": "SPEI" },
        { "id": 3, "nombre": "Profesor" }
      ],
      "permissions": {
        "noticias": false,
        "galeria": true,
        "attp": true
      }
    },
    "tokens": {
      "accessToken": "...",
      "refreshToken": "...",
      "expiresIn": "1d"
    }
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

**Ejemplo (login Google aprobado)**
```bash
curl -X POST https://api.miet20.edu/api/v1/auth/google-login \
  -H "Content-Type: application/json" \
  -d '{"idToken": "<google-id-token>"}'
```

Respuesta idéntica al login tradicional cuando la cuenta está activa y con roles asignados. En entornos de testing puede enviars
e adicionalmente `email` para forzar el correo cuando el `idToken` proviene de un mock.

**Ejemplo (logout)**

```bash
curl -X POST https://api.miet20.edu/api/v1/auth/logout \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "...", "allDevices": false}'
```

Respuesta:

```json
{
  "status": "success",
  "data": {
    "message": "Sesión finalizada correctamente"
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

Enviar `allDevices: true` revoca todos los refresh tokens asociados al usuario, forzando el cierre de sesión en otros navegadores
 o dispositivos.

## Registro y onboarding

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| POST | `/registrations` | público | Alta/actualización de datos de autogestión desde el formulario Google (queda en estado pendiente). |
| GET | `/registrations` | admin | Listado filtrable de solicitudes (`status=pending|approved|rejected`). |
| GET | `/registrations/{id}` | admin | Detalle de una solicitud con metadatos de contacto. |
| PATCH | `/registrations/{id}` | admin | Aprobar (`decision=approve`, `rol`) o rechazar (`decision=reject`) la solicitud. |

**Payload de registro**

```json
{
  "mail": "postulante@et20.edu",
  "nombre": "Juana",
  "apellido": "Pérez",
  "dni": "40123456",
  "telefono": "11-5555-5555",
  "direccion": "Av. Siempreviva 123",
  "fecha_nacimiento": "2006-05-18",
"contrasena": "MiClaveSegura"
}
```

## Inscripciones y progresión académica

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| POST | `/students/{id}/cursos` | admin, preceptor | Inscribe al alumno en un curso activo (`curso_id`, `estado`). |
| DELETE | `/students/{id}/cursos/{courseId}` | admin, preceptor | Da de baja la cursada vigente (utilizado para egresos/promociones). |
| PUT | `/students/{id}` | admin, preceptor | Actualiza atributos académicos (ej. `estado_academico=EGRESADO`). |

**Ejemplo: promover lote de alumnos**

```http
DELETE /api/v1/students/128/cursos/9 HTTP/1.1
Authorization: Bearer <token>

POST /api/v1/students/128/cursos HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "curso_id": 12,
  "estado": "activo"
}
```

> Tras moverlos, se puede marcar egresados llamando a `PUT /students/{id}` con `{"estado_academico": "EGRESADO"}`.

## Boletines académicos

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/report-cards` | admin, preceptor | Listado filtrable por curso, alumno, año lectivo o estado. |
| GET | `/report-cards/{id}` | admin, preceptor | Detalle del boletín con calificaciones y materias asociadas. |
| POST | `/report-cards` | preceptor | Crea un nuevo boletín en estado `draft` y pre-carga materias del curso. |
| PUT | `/report-cards/{id}` | preceptor | Actualiza observaciones y calificaciones del borrador. |
| PATCH | `/report-cards/{id}/status` | preceptor | Cambia el estado (`draft`, `published`, `archived`) y fija `issuedAt` al publicar. |
| GET | `/report-cards/{id}/export` | admin, preceptor | Devuelve payload enriquecido para exportar PDF/Excel. |

**Ejemplo: crear borrador de boletín**

```http
POST /api/v1/report-cards HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "studentId": 128,
  "courseId": 12,
  "academicYear": 2025,
  "term": "1er Bimestre",
  "grades": [
    {
      "subjectId": 4,
      "numericGrade": null,
      "conceptualGrade": null
    }
  ],
  "observations": "Familia notificada el 12/06"
}
```

Respuesta

```json
{
  "status": "success",
  "data": {
    "id": 41,
    "studentId": 128,
    "courseId": 12,
    "academicYear": 2025,
    "term": "1er Bimestre",
    "status": "draft",
    "grades": [
      {
        "id": 311,
        "subjectId": 4,
        "subjectName": "Matemática",
        "numericGrade": null,
        "conceptualGrade": null
      }
    ]
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Asistencias

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/attendance/cursos/{courseId}` | admin, preceptor, profesor | Consulta de asistencias generales por curso/fecha. |
| POST | `/attendance/cursos/{courseId}` | profesor, preceptor | Registro masivo de asistencias generales por fecha (panel de preceptoría). |
| PUT | `/attendance/cursos/{courseId}/alumnos/{studentId}` | profesor, preceptor | Ajuste puntual de asistencia general por alumno y fecha. |
| GET | `/attendance/cursos/{courseId}/materias/{subjectId}/habilitados` | profesor | Devuelve qué días puede editar el profesor según sus horarios activos. |
| GET | `/attendance/cursos/{courseId}/materias/{subjectId}/semanal` | admin, preceptor, profesor | Obtiene la grilla semanal (columnas por día, filas por alumno) que utiliza el panel de profesor. |
| POST | `/attendance/cursos/{courseId}/materias/{subjectId}/matriz` | profesor | Guarda la matriz completa enviada por el formulario heredado (`encabezados` + `asistencias`). |
| POST | `/attendance/cursos/{courseId}/materias/{subjectId}/importar-general` | profesor | Importa asistencias del preceptor (`asistencia_general`) para una fecha/turno y reescribe la materia. |
| GET | `/attendance/cursos/{courseId}/resumen` | admin, preceptor, profesor | Resumen diario de presentes/ausentes/tarde por turno y contraturno. |

**Consulta semanal por materia**

```http
GET /api/v1/attendance/cursos/12/materias/5/semanal?fecha=2024-05-20 HTTP/1.1
Authorization: Bearer <token>
```

Respuesta

```json
{
  "status": "success",
  "data": {
    "columnas": ["Nro", "Nombre", "20-05-2024", "21-05-2024", "22-05-2024", "23-05-2024", "24-05-2024"],
    "fechas_iso": ["2024-05-20", "2024-05-21", "2024-05-22", "2024-05-23", "2024-05-24"],
    "editable": [true, true, false, true, true],
    "filas": [
      [1, "Pérez, Ana", "P", "A", "NC", "P", "P"],
      [2, "Ramírez, Joel", "P", "P", "NC", "T", "P"]
    ],
    "alumno_ids": [45, 62]
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Trabajos prácticos y contenidos

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/assignments/courses/{courseId}` | admin, preceptor, profesor | Listado de trabajos y contenidos publicados. |
| POST | `/assignments/courses/{courseId}` | profesor | Crear trabajo (sustituye `crear_trabajo.php`). |
| PUT/DELETE | `/assignments/{assignmentId}` | profesor | Actualizar o eliminar trabajo. |
| POST | `/assignments/{assignmentId}/students/{studentId}/submissions` | profesor, preceptor | Registrar entrega/corrección.

## Notificaciones institucionales

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/notifications` | autenticado | Bandeja personal de notificaciones (reemplaza `notificaciones/listar.php`).【F:backend/notificaciones/listar.php†L1-L26】 |
| POST | `/notifications` | admin, preceptor, profesor | Publicar notificación a usuario individual o grupo. |
| PATCH | `/notifications/destinatarios/{recipientId}/leida` | autenticado | Marca una notificación como leída para el destinatario autenticado. |
| PATCH | `/notifications/destinatarios/{recipientId}/confirmar` | autenticado | Confirma la notificación cuando requiere confirmación. |
| GET/POST | `/notifications/groups` | admin (GET también para preceptor) | Gestión de grupos de difusión.

## Noticias institucionales

@@ -372,189 +459,307 @@ Content-Type: application/json
{
  "title": "Comienzan las inscripciones 2025",
  "content": "<p>El próximo lunes abrimos el formulario...</p>",
  "status": "published",
  "publishFrom": "2025-03-01T12:00:00-03:00"
}
```

Respuesta

```json
{
  "status": "success",
  "data": {
    "id": 14,
    "title": "Comienzan las inscripciones 2025",
    "content": "<p>El próximo lunes abrimos el formulario...</p>",
    "status": "published",
    "publishFrom": "2025-03-01T12:00:00.000Z",
    "audience": {
      "roles": [],
      "courses": [],
      "users": []
    },
    "coverUrl": null
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

**Actualizar portada**

```http
POST /api/v1/news/14/cover HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "coverName": "inscripciones.png",
  "coverMime": "image/png",
  "coverData": "iVBORw0KGgoAAAANSUhEUgAA..."
}
```

La respuesta devuelve la noticia con la URL pública de la portada.

**Listado público**

```http
GET /api/v1/news/public?limit=6 HTTP/1.1
```

```json
{
  "status": "success",
  "data": {
    "items": [
      {
        "id": 14,
        "title": "Comienzan las inscripciones 2025",
        "content": "<p>El próximo lunes abrimos el formulario...</p>",
        "coverUrl": "/panelNoticias/images/14.png",
        "publishFrom": "2025-03-01T12:00:00.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 6,
      "total": 1,
      "totalPages": 1
    }
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Galería multimedia

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/gallery/categories` | público | Listado de categorías disponibles con cantidad de ítems y miniatura más reciente. |
| GET | `/gallery/items` | público | Listado paginado de imágenes. Acepta filtros `category`, `page`, `limit`, `search`. |
| POST | `/gallery/items` | autenticado con permiso de galería | Sube una nueva imagen (JPG/PNG/WEBP) codificada en base64 y la asocia a una categoría. |
| POST | `/gallery/uploads` | autenticado con permiso de galería | Inicia una sesión de carga chunked. Espera `fileSize`, `chunkSize`, `totalChunks`. |
| POST | `/gallery/uploads/{id}/chunks` | autenticado con permiso de galería | Recibe un bloque base64 (`chunkData`) para la sesión indicada. |
| POST | `/gallery/uploads/{id}/complete` | autenticado con permiso de galería | Cierra la sesión y publica el recurso asociado. |

### Ejemplo: obtener categorías públicas

```http
GET /api/v1/gallery/categories HTTP/1.1
Host: miet20.local
Accept: application/json
```

```json
{
  "data": {
    "categories": [
      {
        "key": "Eventos",
        "name": "Eventos",
        "itemsCount": 12,
        "coverUrl": "/gallery/items/4d7ab3fd.jpg",
        "lastUploadedAt": "2024-11-30T18:22:11.000Z"
      }
    ]
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

### Ejemplo: cargar una nueva imagen

```http
POST /api/v1/gallery/items HTTP/1.1
Host: miet20.local
Authorization: Bearer <token>
Content-Type: application/json

{
  "category": "Eventos",
  "author": "Equipo de Comunicación",
  "description": "Acto de fin de año",
  "fileName": "acto.jpg",
  "fileMime": "image/jpeg",
  "fileData": "<cadena base64>"
}
```

```json
{
  "data": {
    "id": 31,
    "category": "Eventos",
    "author": "Equipo de Comunicación",
    "description": "Acto de fin de año",
    "fileUrl": "/gallery/items/a12bc34d.jpg",
    "uploadedAt": "2024-12-01T14:03:21.000Z"
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Contacto institucional y formularios

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| POST | `/contact/messages` | público | Enviar mensaje de contacto (nombre, email, asunto, mensaje, adjuntos opcionales). |
| GET | `/contact/messages` | admin, preceptor | Listado de mensajes recibidos con filtros por estado (`new`, `in_progress`, `resolved`). |
| GET | `/contact/messages/{id}` | admin, preceptor | Detalle del mensaje y auditoría. |
| PATCH | `/contact/messages/{id}` | admin, preceptor | Actualizar estado, asignar responsable, agregar notas. |

### Ejemplo: enviar mensaje de contacto

```http
POST /api/v1/contact/messages HTTP/1.1
Host: miet20.local
Content-Type: application/json

{
  "name": "María García",
  "email": "maria@example.com",
  "subject": "Consulta de inscripción",
  "message": "Hola, ¿cuándo comienzan las inscripciones para 1er año?",
  "attachments": [12, { "id": 18, "name": "planilla.pdf" }]
}
```

```json
{
  "status": "success",
  "data": {
    "id": 41,
    "name": "María García",
    "email": "maria@example.com",
    "subject": "Consulta de inscripción",
    "status": "new",
    "attachments": [
      { "id": 12 },
      { "id": 18, "name": "planilla.pdf" }
    ],
    "createdAt": "2025-02-10T14:22:11.000Z"
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Contenidos públicos institucionales

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/public/pages` | público | Listado de páginas informativas (ej. historia de la escuela, reglamentos). |
| POST | `/public/pages` | admin | Crear página pública con secciones estructuradas (título, contenido, adjuntos). |
| GET | `/public/pages/{slug}` | público | Obtener contenido publicado por slug. |
| PUT | `/public/pages/{slug}` | admin | Actualizar contenido, estado (`draft`, `published`) y metadatos SEO. |
| DELETE | `/public/pages/{slug}` | admin | Baja lógica o eliminación definitiva. |

### Ejemplo: obtener una página publicada

```http
GET /api/v1/public/pages/historia HTTP/1.1
Host: miet20.local
Accept: application/json
```

```json
{
  "status": "success",
  "data": {
    "slug": "historia",
    "title": "Nuestra Historia",
    "content": "La escuela fue fundada en 1985...",
    "sections": [
      {
        "title": "Fundación",
        "content": "Descripción del hito",
        "order": 1
      }
    ],
    "attachments": [{ "id": 12, "name": "reglamento.pdf" }],
    "publishedAt": "2025-01-30T10:00:00.000Z"
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Gestión de archivos y adjuntos

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| POST | `/files` | autenticado | Subir archivo y obtener metadatos + URL firmada (integración con S3/local). |
| GET | `/files/{id}` | autenticado/público según permisos | Obtener metadatos y URL firmada temporal. |
| DELETE | `/files/{id}` | autenticado con permisos | Eliminar archivo y revocar accesos. |

```http
POST /api/v1/files HTTP/1.1
Host: miet20.local
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileName": "documento.pdf",
  "fileMime": "application/pdf",
  "fileData": "<base64>"
}
```

```json
{
  "status": "success",
  "data": {
    "id": 7,
    "originalName": "documento.pdf",
    "mimeType": "application/pdf",
    "size": 15360,
    "publicUrl": "/files/0b2d4e56.pdf",
    "signedUrl": "/files/0b2d4e56.pdf?token=...&expires=1712851200",
    "expiresAt": 1712851200
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Integraciones externas

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| POST | `/auth/google` | público | Intercambia token de Google ID por JWT interno. Crea cuenta nueva o vincula existente manteniendo roles. |
| POST | `/integrations/google/sync` | admin | Sincroniza calendarios y listas de distribución con la cuenta institucional de Google Workspace. |



## Configuración institucional

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET | `/configuration` | admin | Obtener valores de configuración (logo, calendarios, integraciones Google). |
| PUT | `/configuration` | admin | Actualizar múltiples claves de configuración atómicas (equivale a `guardar_configuracion.php`).

## Dispositivos SPEI (netbooks y préstamos)

| Método | Endpoint | Permisos | Descripción |
|--------|----------|----------|-------------|
| GET/POST | `/devices` | spei, admin | Listado de netbooks con `items` y métricas de stock / alta de un dispositivo. |
| GET | `/devices/resumen` | spei, admin | Totales rápidos (disponibles, no disponibles, préstamos activos). |
| PUT/DELETE | `/devices/{id}` | spei, admin | Actualizar datos, estado u observaciones / dar de baja. |
| POST | `/devices/{id}/loans` | spei, admin | Registrar préstamo de netbook especificando alumno, curso y tutor. |
| POST | `/devices/loans` | spei, admin | Registrar préstamo indicando `dispositivo_id` en el cuerpo (alternativa sin path param). |
@@ -565,64 +770,75 @@ Content-Type: application/json
| DELETE | `/devices/pizarron/{noteId}` | spei, admin | Eliminar una nota del pizarrón. |
| GET/POST | `/devices/{id}/notes` | spei, admin | Historial granular de observaciones por dispositivo.

Las respuestas de listado devuelven envelopes estructurados, por ejemplo `GET /devices` retorna:

```json
{
  "status": "success",
  "data": {
    "items": [
      {
        "id": 12,
        "carrito": "A",
        "numero": "03",
        "numero_serie": "SN123",
        "estado": "En uso",
        "observaciones": null,
        "codigo": "A03"
      }
    ],
    "stats": {
      "disponibles": 14,
      "noDisponibles": 3,
      "prestamosActivos": 5
    }
  },
  "errors": null,
  "meta": {
    "requestId": "5c5fce3d-..."
  }
}
```

## Manejo de errores

La API responde con un envelope consistente:

```json
{
  "status": "error",
  "data": null,
  "errors": [
    {
      "code": "validation_error",
      "message": "Error de validación"
    }
  ],
  "meta": {
    "requestId": "..."
  }
}
```

Los códigos siguen la convención HTTP (`401` credenciales inválidas, `403` sin permisos, `404` recurso no encontrado, `409` conflicto).

## Buenas prácticas incluidas

- Middleware de autenticación y autorización por rol.
- Validaciones centralizadas con `express-validator`.
- Manejo unificado de errores (`ApiError`) y logs estructurados con Winston.
- Conexión a BD compartida vía Knex con pool e instrumentación para monitoreo.
- Versionado de endpoints y `healthcheck` (`/health`) para observabilidad.

## Roadmap de escalabilidad

1. **Versiones de escritorio (Java) y móviles (Android/iOS)**: El contrato JSON estable y el uso de JWT permite integrar clientes multiplataforma sin acoplarse a sesiones PHP. Recomendable generar SDKs compartidos y aprovechar OpenAPI/Swagger para autogenerar clientes.
2. **Microservicios / GraphQL**: separar dominios (académico, comunicaciones, inventario) en servicios independientes reutilizando la capa de repositorios. Añadir un gateway GraphQL si se requiere agregación avanzada.
3. **Observabilidad**: Integrar Prometheus/Grafana y trazas distribuidas (OpenTelemetry) para medir tiempos de respuesta de endpoints críticos (carga de notas, asistencia masiva).
4. **Optimización del código heredado**: consolidar scripts PHP duplicados (por ejemplo, `admin_eliminar_alumno.php` y `eliminar_alumno.php`) en endpoints REST y utilizar transacciones atómicas en vez de múltiples `prepare` dispersos.【F:backend/users/admin/utils/admin_eliminar_alumno.php†L1-L53】【F:backend/users/admin/utils/eliminar_alumno.php†L1-L57】

## Ejecución local

```bash
cd api
cp .env.example .env