# Checklist de migración módulo a módulo

| Módulo           | Estado      | Modo            | Responsable             | Notas                                                                 | Última actualización |
|------------------|-------------|-----------------|-------------------------|-----------------------------------------------------------------------|----------------------|
| Noticias         | Completado  | API_MODE        | Comunicación            | Panel migrado al cliente API con soporte de multimedia.               | 2024-07-15           |
| Galería          | Completado  | API_MODE        | Comunicación            | Upload chunked (`/gallery/uploads`) con fallback automático activo.   | 2025-09-24           |
| Contactos        | Completado  | API_MODE        | Comunidad               | Formulario público con `ContactosService` y métricas de entrega L1.   | 2025-09-24           |
| Notificaciones   | Completado  | API_MODE        | Sistemas                | Confirmaciones y grupos vía `NotificationsService`, sin SQL legacy.   | 2025-09-24           |
| Configuración    | Completado  | API_MODE        | Sistemas                | Formularios por rol refrescan datos desde la API (sin `db.php`).       | 2025-09-24           |
| Inscripciones    | Completado  | API_MODE        | Secretaría              | Progresión anual y egresos via `students/{id}/cursos` + toggles API.   | 2025-09-25           |
| Calificaciones   | Completado  | API_MODE        | Coordinación académica  | Boletines gestionados por `ReportCardsService` sin dependencias SQL.  | 2025-09-25           |
| Asistencias      | Completado  | API_MODE        | Preceptoría             | Panel de preceptor migrado a AttendanceService con reportes y CSV API | 2025-09-23           |
| Finanzas/SPEI    | Completado  | API_MODE        | Administración          | UI de finanzas integrada (`listar_adeudos` / `obtener_pago`)          | 2025-09-23           |
