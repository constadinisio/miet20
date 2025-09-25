import { body, param, query } from 'express-validator';

const attendanceStates = ['P', 'A', 'T', 'AJ', 'NC'];
const isoDateRegex = /^\d{4}-\d{2}-\d{2}$/;

const validateDateList = (value) => {
  if (value === undefined || value === null || value === '') {
    return true;
  }

  const values = Array.isArray(value)
    ? value
    : typeof value === 'string'
      ? value
          .split(',')
          .map((item) => item.trim())
          .filter((item) => item !== '')
      : [];

  if (!values.length) {
    return true;
  }

  values.forEach((item) => {
    if (!isoDateRegex.test(item)) {
      throw new Error('La fecha es inválida');
    }
  });

  return true;
};

export const recordAttendanceValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  body('fecha').isISO8601().withMessage('La fecha es inválida'),
  body('registros').isArray({ min: 1 }).withMessage('Debe indicar los alumnos'),
  body('registros.*.alumno_id').isInt().withMessage('El alumno es obligatorio'),
  body('registros.*.estado')
    .isIn(attendanceStates)
    .withMessage('Estado inválido. Use P, A, T, AJ o NC'),
  body('registros.*.es_contraturno').optional().isInt({ min: 0, max: 1 }),
  body('es_contraturno').optional().isInt({ min: 0, max: 1 })
];

export const listAttendanceValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  query('fecha').optional().isISO8601().withMessage('La fecha es inválida')
];

export const courseGeneralAttendanceValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  query('fechas').optional().custom(validateDateList),
  query('desde').optional().custom((value) => {
    if (!isoDateRegex.test(value)) {
      throw new Error('La fecha desde es inválida');
    }
    return true;
  }),
  query('hasta').optional().custom((value) => {
    if (!isoDateRegex.test(value)) {
      throw new Error('La fecha hasta es inválida');
    }
    return true;
  })
];

export const courseDaySubjectsValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  query('fecha').optional().isISO8601().withMessage('La fecha es inválida')
];

export const updateAttendanceValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  param('studentId').isInt().withMessage('El alumno es obligatorio'),
  body('fecha').isISO8601().withMessage('La fecha es inválida'),
  body('estado')
    .optional()
    .isIn(attendanceStates)
    .withMessage('Estado inválido. Use P, A, T, AJ o NC'),
  body('es_contraturno').optional().isInt({ min: 0, max: 1 })
];

const courseSubjectParams = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  param('subjectId').isInt().withMessage('La materia es obligatoria')
];

export const allowedDaysValidator = [
  ...courseSubjectParams,
  query('fecha').optional().isISO8601().withMessage('La fecha es inválida')
];

export const weeklySubjectValidator = [
  ...courseSubjectParams,
  query('fecha').optional().isISO8601().withMessage('La fecha es inválida')
];

export const saveSubjectMatrixValidator = [
  ...courseSubjectParams,
  body('encabezados').isArray({ min: 1 }).withMessage('Debe incluir los encabezados de la tabla'),
  body('asistencias').isArray({ min: 1 }).withMessage('Debe incluir las asistencias')
];

export const importSubjectValidator = [
  ...courseSubjectParams,
  body('fecha').isISO8601().withMessage('La fecha es inválida')
];

export const importProfessorValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  body('fecha').isISO8601().withMessage('La fecha es inválida'),
  body('materia_ids').isArray({ min: 1 }).withMessage('Debe seleccionar materias'),
  body('materia_ids.*').isInt({ min: 1 }).withMessage('Materia inválida'),
  body('dry_run').optional().isBoolean().withMessage('El indicador de simulación es inválido')
];

export const courseSummaryValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio'),
  query('fecha').optional().isISO8601().withMessage('La fecha es inválida')
];

export const studentAttendanceValidator = [
  param('studentId').isInt().withMessage('El alumno es obligatorio')
];

export const courseGeneralRangeValidator = [
  param('courseId').isInt().withMessage('El curso es obligatorio')
];
