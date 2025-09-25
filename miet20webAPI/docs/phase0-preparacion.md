# Fase 0 · Preparación de la migración

## 1. Inventario funcional del legacy PHP

| Área | Ruta principal | Submódulos / scripts destacados | Responsabilidad actual | Notas para migración |
|------|----------------|---------------------------------|------------------------|----------------------|
| Autenticación y sesión | `backend/includes` | `validar_login.php`, `logout.php`, `cambiar_rol.php` | Manejo de credenciales locales, cookies y cambio de rol manual. | Sustituir por endpoints de login/logout con tokens; revisar soporte multi-rol en API. |
| Configuración institucional | `backend/includes/guardar_configuracion.php`, `backend/utils/plantillas` | Formularios de datos de la institución, plantillas PDF/Excel. | Persistencia directa en base local y generación de plantillas. | Requiere endpoints para CRUD de configuración y generación de plantillas bajo demanda. |
| Registro Google | `backend/includes/guardar_registro_google.php` | Alta de usuarios/estudiantes mediante token Google. | Inserción directa en tablas de alumnos/usuarios. | API necesita endpoints específicos para onboarding vía Google. |
| Gestión SPEI | `backend/users/spei` | `index.php`, listados, carga de cursos, reportes. | Administración académica especializada (SPEI). | Confirmar correspondencia con endpoints de cursos/alumnos y crear recursos faltantes. |
| Gestión Administradores | `backend/users/admin` | Dashboard, alta/baja de usuarios, módulos de reportes. | CRUD de usuarios, métricas generales. | Requiere endpoints de usuarios, roles, reportes resumidos. |
| Gestión Profesores | `backend/users/profesor` | Asistencias, calificaciones, contenidos. | Operaciones sobre cursos/materias vinculadas al docente. | Necesita endpoints para asistencia, calificaciones, adjuntos. |
| Gestión Preceptores | `backend/users/preceptor` | Control diario, comunicaciones, avisos. | Manejo de asistencias, avisos a familias. | Alinear con endpoints de asistencia y notificaciones. |
| Notificaciones | `backend/notificaciones` | Envío de emails, push, configuración. | Orquesta envíos y plantillas desde PHP. | Trasladar a endpoints de notificaciones y colas de envío en API. |
| Sitio público | `public/` | `index.php`, `panelNoticias/`, `galeria/`, `contacto/`, `inscripciones/` | Muestra contenidos institucionales, formularios de contacto y noticias. | API debe exponer endpoints públicos/cacheables para noticias, galerías, formularios. |
| Utilidades compartidas | `backend/utils`, `public/includes` | Helpers de base de datos (`db.php`), plantillas, generación de reportes. | Acceso directo a MySQL y utilitarios comunes. | Sustituir por cliente API reusable (`apiClient.php`) y servicios especializados. |

## 2. Dependencias comunes y flujos transversales

- **Persistencia**: Todo el legacy depende de `backend/includes/db.php` para ejecutar queries manuales. Además, varios scripts abren conexiones directas usando `mysqli` embebido.
- **Sesión PHP**: Uso extensivo de `$_SESSION` para roles, permisos y contexto de usuario. Necesitamos un plan de transición a tokens JWT manejados por el cliente API.
- **Uploads y archivos**: Directorios `public/uploads`, `backend/utils/plantillas` generan o consumen archivos locales. La API deberá exponer endpoints para storage y firmar URLs.
- **Helpers de UI**: `public/includes` contiene componentes reutilizables (header, footer, menús) con lógica condicional basada en roles. Al migrar, deben depender de helpers API (por ejemplo, `obtenerPerfil()`).

## 3. Preparativos de infraestructura

1. **Entornos y configuración**
   - Definir archivos `.env` separados para API (variables de conexión, JWT_SECRET) y frontend PHP (URL base de la API, claves públicas).
   - Provisionar base de datos semilla para entornos `dev` y `staging` con datos de prueba.
2. **Herramientas de prueba**
   - Crear colección de Insomnia/Postman sincronizada con `docs/API.md` para validar endpoints existentes y los que se incorporen.
   - Configurar PHPUnit o Pest en el frontend para testear el cliente API (`public/includes/apiClient.php`).
3. **Pipeline y despliegue**
   - Documentar proceso de CI/CD (lint, pruebas, despliegue automatizado) tanto para `api/` como para el legado refactorizado.
   - Definir estrategia de feature flags para habilitar gradualmente el consumo de la API por módulo.

## 4. Resultado y próximos pasos

- Inventario funcional y de dependencias completado, habilitando la transición a la **Fase 2** enfocada en fortalecer y ampliar la API.
- Antes de iniciar Fase 2, validar con el equipo de negocio que el listado anterior cubre todos los módulos activos y agregar detalles específicos (requisitos de reportes, SLAs, integraciones externas).

