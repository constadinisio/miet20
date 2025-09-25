import { body, param, query } from 'express-validator';

export const courseAssignmentParams = [
  param('courseId').isInt().withMessage('El curso es obligatorio')
];

export const assignmentIdParam = [
  param('assignmentId').isInt().withMessage('El trabajo es obligatorio')
];

export const assignmentStudentParam = [
  param('studentId').isInt().withMessage('El alumno es obligatorio')
];

const ASSIGNMENT_TYPES = ['tp', 'actividad'];

export const createAssignmentValidator = [
  ...courseAssignmentParams,
  body('materia_id').isInt().withMessage('La materia es obligatoria'),
  body('nombre').notEmpty().withMessage('El nombre es obligatorio'),
  body('tipo')
    .isIn(ASSIGNMENT_TYPES)
    .withMessage('El tipo es inválido, debe ser "tp" o "actividad"'),
  body('descripcion').optional().isString().withMessage('La descripción es inválida')
];

export const updateAssignmentValidator = [
  ...assignmentIdParam,
  body('nombre').optional().notEmpty(),
  body('tipo')
    .optional()
    .isIn(ASSIGNMENT_TYPES)
    .withMessage('El tipo es inválido, debe ser "tp" o "actividad"'),
  body('descripcion').optional().isString()
];

export const submitAssignmentValidator = [
  ...assignmentIdParam,
  param('studentId').isInt().withMessage('El alumno es obligatorio'),
  body('materia_id').isInt().withMessage('La materia es obligatoria'),
  body('nota').optional().isFloat({ min: 0, max: 10 }).withMessage('La nota es inválida')
];

export const listStudentAssignmentsValidator = [...assignmentStudentParam];

const validateStudentIds = (value) => {
  if (!value && value !== 0) {
    return true;
  }

  const values = Array.isArray(value)
    ? value
    : typeof value === 'string'
      ? value
          .split(',')
          .map((item) => item.trim())
          .filter((item) => item !== '')
      : [value];

  if (!values.length) {
    return true;
  }

  const invalid = values.some((item) => {
    const parsed = Number(item);
    return !Number.isInteger(parsed) || parsed <= 0;
  });

  if (invalid) {
    throw new Error('Los alumnos son inválidos');
  }

  return true;
};

export const courseAssignmentGradesValidator = [
  query('materia_id').optional().isInt({ min: 1 }).withMessage('La materia es inválida'),
  query('alumno_ids').optional().custom(validateStudentIds)
];
