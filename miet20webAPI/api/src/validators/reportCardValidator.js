import { body, param, query } from 'express-validator';

export const listReportCardsValidator = [
  query('courseId').optional().isInt({ min: 1 }).toInt(),
  query('studentId').optional().isInt({ min: 1 }).toInt(),
  query('academicYear').optional().isInt({ min: 2000 }).toInt(),
  query('status').optional().isIn(['draft', 'published', 'archived'])
];

export const reportCardIdParamValidator = [
  param('reportCardId').isInt({ min: 1 }).withMessage('Identificador de boletín inválido')
];

const gradeValidators = [
  body('grades').optional().isArray(),
  body('grades.*.id').optional().isInt({ min: 1 }).toInt(),
  body('grades.*.subjectId').isInt({ min: 1 }).withMessage('La materia es obligatoria para cada calificación').toInt(),
  body('grades.*.numericGrade').optional().isFloat({ min: 1, max: 10 }).toFloat(),
  body('grades.*.conceptualGrade').optional().isString().trim(),
  body('grades.*.observations').optional().isString()
];

export const createReportCardValidator = [
  body('studentId').isInt({ min: 1 }).withMessage('El alumno es obligatorio').toInt(),
  body('courseId').isInt({ min: 1 }).withMessage('El curso es obligatorio').toInt(),
  body('academicYear').optional().isInt({ min: 2000 }).toInt(),
  body('term').optional().isString().trim(),
  body('observations').optional().isString(),
  body('status').optional().isIn(['draft', 'published', 'archived']),
  ...gradeValidators
];

export const updateReportCardValidator = [
  ...reportCardIdParamValidator,
  body('academicYear').optional().isInt({ min: 2000 }).toInt(),
  body('term').optional().isString().trim(),
  body('observations').optional().isString(),
  body('status').optional().isIn(['draft', 'published', 'archived']),
  ...gradeValidators
];

export const updateReportCardStatusValidator = [
  ...reportCardIdParamValidator,
  body('status').isIn(['draft', 'published', 'archived']).withMessage('Estado inválido')
];

export const reportCardExportValidator = reportCardIdParamValidator;

export const reportCardCourseSubjectsQueryValidator = [
  query('courseId').isInt({ min: 1 }).withMessage('El curso es obligatorio').toInt()
];

export const reportCardLatestTermQueryValidator = [
  query('courseId').isInt({ min: 1 }).withMessage('El curso es obligatorio').toInt(),
  query('studentId').isInt({ min: 1 }).withMessage('El alumno es obligatorio').toInt(),
  query('academicYear').optional().isInt({ min: 2000 }).toInt()
];

export const reportCardMissingSubjectsQueryValidator = [
  query('courseId').isInt({ min: 1 }).withMessage('El curso es obligatorio').toInt(),
  query('studentId').isInt({ min: 1 }).withMessage('El alumno es obligatorio').toInt(),
  query('term').isString().trim().notEmpty().withMessage('El período es obligatorio'),
  query('academicYear').optional().isInt({ min: 2000 }).toInt()
];
