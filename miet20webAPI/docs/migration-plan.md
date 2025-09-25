# Plan de migración de frontend PHP a API REST MIET20

## 1. Alcance y supuestos
- **Objetivo**: reemplazar completamente el acceso directo a la base de datos desde el frontend/backoffice PHP por llamadas a la API Node.js documentada en `docs/API.md`.
- **Repositorios involucrados**: `api/` para el backend REST, `public/` y `backend/` para los sitios PHP heredados que deberán delegar en la API.
- **Supuestos**:
  - Se mantendrá la estructura de rutas y vistas actual mientras se desacopla la lógica de datos.
  - Los contratos de datos existentes en `docs/API.md` se respetan; los nuevos endpoints ampliarán esa especificación sin romper compatibilidad.
  - Se habilitarán entornos diferenciados (dev/staging/prod) para validar la migración gradualmente.

## 2. Fases de trabajo

### Fase 0 · Preparación
1. **Inventario funcional**
   - Catalogar cada módulo del legacy (`backend/users/*`, `backend/notificaciones`, `public/panelNoticias`, etc.) y mapear las acciones críticas (CRUD, reportes, exportaciones).
   - Identificar dependencias comunes (`backend/includes/db.php`, helpers utilitarios) y flujos compartidos (autenticación, permisos).
2. **Infraestructura de soporte**
   - Configurar entornos para la API (variables `.env`, base de datos semilla) y preparar herramientas de prueba (Postman/Insomnia collections, PHPUnit para clientes PHP).
   - Definir lineamientos de versionado y despliegue continuo para la API y el frontend refactorizado.

### Fase 1 · Auditoría y análisis de brechas
1. **Revisión de consultas SQL actuales**
   - Documentar consultas directamente embebidas en PHP, clasificarlas por módulo y determinar si existe un endpoint equivalente en `docs/API.md`.
2. **Gap analysis de endpoints**
   - Para cada acción sin cobertura, especificar el endpoint requerido (método, URL, payload/response, reglas de negocio, validaciones).
   - Priorizar las brechas según criticidad funcional (ej. asistencias, calificaciones, notificaciones > galería > configuraciones menores).
3. **Backlog técnico**
   - Consolidar historias de usuario/tareas técnicas con estimación y dependencias para planificar sprints iterativos.

### Fase 2 · Plataforma API
1. **Refuerzo del núcleo**
   - Implementar autenticación completa (refresh tokens, control de roles) y middleware de auditoría/logging.
   - Normalizar respuestas (`status`, `data`, `error`) y manejo de errores.
2. **Implementación de endpoints prioritarios**
   - Usuarios/roles, alumnos/matrículas, cursos/materias (ya definidos en `docs/API.md`).
   - Endpoints adicionales derivados del gap analysis (galería, noticias, contactos, archivos, etc.).
3. **Testing y documentación**
   - Añadir pruebas automatizadas (unitarias, integración) para los nuevos servicios.
   - Actualizar `docs/API.md` con cualquier ampliación y generar colección API.

### Fase 3 · Cliente PHP reutilizable
1. **Diseño del cliente**
   - Extender `public/includes/apiClient.php` para gestionar base URL, autenticación JWT, retries y manejo centralizado de errores.
   - Crear helpers de dominio (ej. `CursosApi`, `AlumnosApi`, `NoticiasApi`) con métodos de alto nivel (ej. `obtenerCursos()`, `guardarNota()`).
2. **Cobertura de utilitarios comunes**
   - Sustituir las funciones de `backend/includes/db.php` por llamadas al cliente API.
   - Implementar caching liviano si es necesario (transitorio) para listas que se consultan frecuentemente.
3. **Pruebas automáticas**
   - Preparar suites de integración en PHP (PHPUnit) que consuman un entorno controlado de la API.

### Fase 4 · Migración módulo a módulo
1. **Selección de piloto**
   - Elegir un módulo de alcance medio (ej. noticias o gestión de cursos) para validar el flujo de extremo a extremo.
   - Refactorizar controladores/vistas correspondientes para usar los helpers API.
   - Documentar hallazgos y ajustar plantillas de migración.
2. **Migración iterativa**
   - Para cada módulo restante:
     1. Revisar scripts PHP involucrados y su dependencia de SQL.
     2. Confirmar soporte en el cliente API; ampliar si falta algo.
     3. Refactorizar vistas/controladores.
     4. Validar funcionalmente (tests + QA manual).
   - Mantener bandera de configuración para alternar entre modo legacy/API durante la transición si fuese necesario.
3. **Monitoreo y métricas**
   - Instrumentar logs en la API para detectar errores tempranos.
   - Configurar alertas de rendimiento y disponibilidad.

### Fase 5 · Hardening y limpieza
1. **Deprecación de código legacy**
   - Retirar `backend/includes/db.php` y scripts que ya no se utilicen.
   - Actualizar documentación operativa y manuales de usuario.
2. **Optimización**
   - Revisar performance de endpoints críticos, aplicar indexación y caching donde corresponda.
   - Evaluar rate limiting y protección adicional (CSRF, throttling) en la API.
3. **Entrega final**
   - Ejecutar regresiones completas.
   - Consolidar checklist de despliegue y plan de rollback.

## 3. Plan de iteraciones sugerido
| Iteración | Duración | Objetivo principal |
|-----------|----------|--------------------|
| 0 | 1 semana | Inventario completo + backlog priorizado |
| 1 | 2 semanas | Fortalecer autenticación API + endpoints de usuarios/alumnos/cursos |
| 2 | 2 semanas | Implementar cliente PHP y migrar módulo piloto |
| 3 | 3 semanas | Migrar módulos críticos (notas, asistencias, notificaciones) |
| 4 | 3 semanas | Migrar módulos restantes (galería, noticias, dispositivos, configuraciones) |
| 5 | 1 semana | Limpieza legacy, optimización y cierre |

## 4. Siguientes pasos inmediatos
1. Completar inventario funcional detallado y mapa de dependencias.
2. Preparar plantilla de especificación de endpoints para las brechas detectadas.
3. Configurar entorno de desarrollo de la API y pruebas automatizadas iniciales.
4. Diseñar interfaz del cliente PHP y definir convenciones de nomenclatura para helpers.

