import { body, param, query } from 'express-validator';

export const listLessonBookValidator = [
  query('curso_id').isInt().withMessage('El curso es obligatorio'),
  query('materia_id').isInt().withMessage('La materia es obligatoria')
];

export const lessonBookEntryParam = [
  param('entryId').isInt().withMessage('El tema es inválido')
];

export const createLessonBookValidator = [
  body('curso_id').isInt().withMessage('El curso es obligatorio'),
  body('materia_id').isInt().withMessage('La materia es obligatoria'),
  body('fecha_clase').isISO8601().withMessage('La fecha es inválida'),
  body('tema').isString().notEmpty().withMessage('El tema es obligatorio'),
  body('caracter_clase').optional().isString(),
  body('actividades').optional().isString(),
  body('actividades_desarrolladas').optional().isString(),
  body('observaciones').optional().isString()
];

export const updateLessonBookValidator = [
  ...lessonBookEntryParam,
  body('fecha_clase').optional().isISO8601().withMessage('La fecha es inválida'),
  body('caracter_clase').optional().isString(),
  body('tema').optional().isString(),
  body('actividades_desarrolladas').optional().isString(),
  body('observaciones').optional().isString()
];
