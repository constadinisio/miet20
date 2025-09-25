import { body, param } from 'express-validator';

export const scheduleIdParam = [param('scheduleId').isInt().withMessage('El horario es inválido')];
export const teacherParam = [param('teacherId').isInt().withMessage('El profesor es inválido')];

export const scheduleCreateValidator = [
  body('dia_semana').isString().trim().notEmpty().withMessage('El día es obligatorio'),
  body('hora_inicio').isString().trim().notEmpty().withMessage('La hora de inicio es obligatoria'),
  body('hora_fin').isString().trim().notEmpty().withMessage('La hora de fin es obligatoria'),
  body('profesor_id')
    .optional()
    .isInt()
    .withMessage('El profesor es inválido'),
  body('materia_id')
    .optional()
    .isInt()
    .withMessage('La materia es inválida'),
  body('aula').optional().isString().isLength({ max: 100 }).withMessage('El aula es inválida'),
  body('es_contraturno')
    .optional()
    .isInt({ min: 0, max: 1 })
    .withMessage('El indicador de contraturno es inválido')
];
