import { body, param, query } from 'express-validator';

export const courseIdParam = [param('id').isInt().withMessage('El id debe ser numérico')];
export const courseParam = [param('courseId').isInt().withMessage('El curso es inválido')];
export const assignmentIdParam = [
  param('assignmentId').isInt().withMessage('La asignación es inválida')
];

export const createCourseValidator = [
  body('anio').isInt({ min: 1 }).withMessage('El año es obligatorio'),
  body('division').notEmpty().withMessage('La división es obligatoria'),
  body('turno').notEmpty().withMessage('El turno es obligatorio'),
  body('estado').optional().isString().withMessage('El estado es inválido')
];

export const assignSubjectValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('materia_id').isInt().withMessage('La materia es obligatoria'),
  body('profesor_id').optional().isInt().withMessage('El profesor es inválido'),
  body('es_contraturno').optional().isInt({ min: 0, max: 1 }).withMessage('El indicador de contraturno es inválido')
];

export const assignTeacherValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('materia_id').isInt().withMessage('La materia es obligatoria'),
  body('profesor_id').isInt().withMessage('El profesor es obligatorio'),
  body('es_contraturno').optional().isInt({ min: 0, max: 1 }).withMessage('El indicador de contraturno es inválido')
];

export const listAssignmentsQueryValidator = [
  query('profesor_id').optional().isInt().withMessage('El profesor es inválido'),
  query('curso_id').optional().isInt().withMessage('El curso es inválido'),
  query('materia_id').optional().isInt().withMessage('La materia es inválida'),
  query('estado').optional().isString().isLength({ min: 1, max: 20 }).withMessage('El estado es inválido')
];
