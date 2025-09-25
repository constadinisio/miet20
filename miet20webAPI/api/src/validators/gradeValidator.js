import { body, param, query } from 'express-validator';

export const studentGradesValidator = [
  param('studentId').isInt().withMessage('El alumno es obligatorio')
];

export const courseGradesValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  query('materia_id').optional().isInt({ min: 1 }).withMessage('La materia es inválida'),
  query('periodo').optional().isString().isLength({ min: 1, max: 50 }).withMessage('El período es inválido')
];

export const recordGradeValidator = [
  param('studentId').isInt().withMessage('El alumno es obligatorio'),
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  body('materia_id').isInt().withMessage('La materia es obligatoria'),
  body('periodo').notEmpty().withMessage('El período es obligatorio'),
  body('nota').isFloat({ min: 1, max: 10 }).withMessage('La nota debe estar entre 1 y 10'),
  body('promedio_actividades').optional().isFloat({ min: 0, max: 10 }).withMessage('El promedio de actividades es inválido')
];

export const updateGradeValidator = [
  param('gradeId').isInt().withMessage('La calificación es obligatoria'),
  body('nota').optional().isFloat({ min: 1, max: 10 }).withMessage('La nota debe estar entre 1 y 10'),
  body('periodo').optional().notEmpty().withMessage('El período es obligatorio'),
  body('promedio_actividades').optional().isFloat({ min: 0, max: 10 }).withMessage('El promedio de actividades es inválido')
];
