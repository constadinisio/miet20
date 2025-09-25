import { body, param, query } from 'express-validator';

export const createPendingSubjectValidator = [
  body('alumno_id')
    .exists().withMessage('El alumno es obligatorio')
    .bail()
    .isInt({ gt: 0 }).withMessage('El alumno debe ser un identificador válido'),
  body('materia_id')
    .exists().withMessage('La materia es obligatoria')
    .bail()
    .isInt({ gt: 0 }).withMessage('La materia debe ser un identificador válido')
];

export const pendingSubjectIdParam = [
  param('pendingId')
    .exists().withMessage('La materia pendiente es obligatoria')
    .bail()
    .isInt({ gt: 0 }).withMessage('La materia pendiente debe ser un identificador válido')
];

export const approvePendingSubjectValidator = [
  body('nota')
    .exists().withMessage('La nota es obligatoria')
    .bail()
    .isFloat().withMessage('La nota debe ser numérica'),
  body('profesor_id')
    .exists().withMessage('El profesor es obligatorio')
    .bail()
    .isInt({ gt: 0 }).withMessage('El profesor debe ser un identificador válido')
];

export const searchSubjectsValidator = [
  query('q').optional().isString().withMessage('El parámetro de búsqueda debe ser texto'),
  query('limit')
    .optional()
    .isInt({ gt: 0, lt: 51 })
    .withMessage('El límite debe ser un número positivo menor o igual a 50')
];
