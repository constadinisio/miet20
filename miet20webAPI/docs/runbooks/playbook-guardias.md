# Playbook de guardias · Fase 5

## Rotaciones
- **Semana A:** Ana V. (L1), Javier R. (L2), Lucía P. (L3/SRE).
- **Semana B:** Marcos D. (L1), Carla M. (L2), Julián S. (L3/SRE).
- **Semana C:** Sofía G. (L1), Diego T. (L2), Paula K. (L3/SRE).

## Cobertura
- Horario estándar: 08:00-22:00 ART (on-call rotativo).
- Guardia pasiva: 22:00-08:00 ART con respuesta en <30 minutos.
- Feriados se cubren con rotación B reforzada (dos L1 disponibles).

## Procedimiento
1. Confirmar disponibilidad en PagerDuty cada lunes antes de las 09:00.
2. Ejecutar checklist de handover en Notion (sección `Operación > Guardias`).
3. Revisar backlog de incidentes abiertos y estado de alertas silenciadas.
4. Documentar cualquier novedad en el canal `#guardia-daily`.

## Escalamiento
- **L1 -> L2:** si el incidente supera 15 minutos sin mitigación o requiere acceso a infraestructura.
- **L2 -> L3:** si existe impacto en disponibilidad >10% o se requieren cambios de configuración.
- **L3 -> Dirección:** incidentes P0 o afectaciones regulatorias; notificar al Director de Tecnología.

## KPIs de guardia
- Tiempo medio de reconocimiento (MTTA) objetivo < 4 minutos.
- Tiempo medio de resolución (MTTR) objetivo < 45 minutos.
- Nº de incidentes reabiertos por guardia <= 1 por mes.

## Capacitación
- Sesiones trimestrales de revisión de runbooks.
- Laboratorios de simulacro `GameDay` cada 6 semanas.
