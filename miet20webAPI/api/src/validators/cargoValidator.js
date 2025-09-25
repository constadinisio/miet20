import { body, param } from 'express-validator';

const sanitizeNullable = (value) => (value === '' ? null : value);
const sanitizeOptionalInt = (value) => {
  if (value === undefined || value === null || value === '') {
    return null;
  }

  return Number(value);
};

export const cargoIdParam = [
  param('cargoId').isInt({ min: 1 }).withMessage('El cargo es inválido')
];

const baseCargoValidator = [
  body('codigo_cargo')
    .isString()
    .trim()
    .notEmpty()
    .isLength({ max: 50 })
    .withMessage('El código del cargo es obligatorio'),
  body('tipo_cargo_id')
    .isInt({ min: 1 })
    .withMessage('El tipo de cargo es inválido'),
  body('estado')
    .optional({ values: [null, ''] })
    .isString()
    .trim()
    .isLength({ min: 1, max: 50 })
    .withMessage('El estado del cargo es inválido'),
  body('observaciones')
    .customSanitizer(sanitizeNullable)
    .optional({ values: [null] })
    .isString()
    .isLength({ max: 65535 })
    .withMessage('Las observaciones son inválidas'),
  body('docente_id')
    .customSanitizer(sanitizeNullable)
    .optional({ values: [null] })
    .isInt({ min: 1 })
    .withMessage('El docente indicado es inválido'),
  body('fecha_inicio')
    .customSanitizer(sanitizeNullable)
    .optional({ values: [null] })
    .isISO8601({ strict: true })
    .withMessage('La fecha de inicio es inválida'),
  body('fecha_fin')
    .customSanitizer(sanitizeNullable)
    .optional({ values: [null] })
    .isISO8601({ strict: true })
    .withMessage('La fecha de fin es inválida'),
  body('tipo')
    .customSanitizer(sanitizeNullable)
    .optional({ values: [null] })
    .isString()
    .trim()
    .isLength({ min: 1, max: 50 })
    .withMessage('La situación del cargo es inválida'),
  body('situacion')
    .customSanitizer(sanitizeNullable)
    .optional({ values: [null] })
    .isString()
    .trim()
    .isLength({ min: 1, max: 50 })
    .withMessage('La situación del cargo es inválida'),
  body().custom((value, { req }) => {
    if (!req.body.tipo && !req.body.situacion) {
      throw new Error('Debés indicar la situación del cargo');
    }
    return true;
  })
];

export const createCargoValidator = [...baseCargoValidator];

export const updateCargoValidator = [...cargoIdParam, ...baseCargoValidator];

export const addScheduleValidator = [
  ...cargoIdParam,
  body('dias')
    .isArray({ min: 1 })
    .withMessage('Tenés que seleccionar al menos un día'),
  body('dias.*').isString().trim().notEmpty().withMessage('El día indicado es inválido'),
  body('hora_inicio')
    .matches(/^\d{2}:\d{2}(?::\d{2})?$/)
    .withMessage('La hora de inicio es inválida'),
  body('hora_fin')
    .matches(/^\d{2}:\d{2}(?::\d{2})?$/)
    .withMessage('La hora de fin es inválida'),
  body('tipo')
    .optional()
    .isString()
    .isLength({ min: 1, max: 50 })
    .withMessage('El tipo indicado es inválido')
];

export const replaceSchedulesValidator = [
  ...cargoIdParam,
  body('horarios')
    .isArray({ min: 1 })
    .withMessage('Debés indicar al menos un horario'),
  body('horarios.*.dias')
    .isArray({ min: 1 })
    .withMessage('Cada horario debe incluir al menos un día'),
  body('horarios.*.dias.*')
    .isString()
    .trim()
    .notEmpty()
    .withMessage('El día indicado es inválido'),
  body('horarios.*.hora_inicio')
    .matches(/^\d{2}:\d{2}(?::\d{2})?$/)
    .withMessage('La hora de inicio es inválida'),
  body('horarios.*.hora_fin')
    .matches(/^\d{2}:\d{2}(?::\d{2})?$/)
    .withMessage('La hora de fin es inválida'),
  body('horarios.*.tipo')
    .optional({ values: [null, ''] })
    .isString()
    .trim()
    .isLength({ min: 1, max: 50 })
    .withMessage('El tipo indicado es inválido')
];

export const cargoScheduleIdParam = [
  ...cargoIdParam,
  param('horarioId').isInt({ min: 1 }).withMessage('El horario es inválido')
];

export const saveRelationsValidator = [
  ...cargoIdParam,
  body('materias')
    .isArray({ min: 1 })
    .withMessage('Debés indicar al menos una materia'),
  body('materias.*.id')
    .customSanitizer(sanitizeOptionalInt)
    .optional({ values: [null] })
    .isInt({ min: 1 })
    .withMessage('La materia indicada es inválida'),
  body('materias.*.cursos')
    .optional({ values: [null] })
    .isArray()
    .withMessage('Los cursos indicados son inválidos'),
  body('materias.*.cursos.*.id')
    .customSanitizer(sanitizeOptionalInt)
    .optional({ values: [null] })
    .isInt({ min: 1 })
    .withMessage('El curso indicado es inválido'),
  body('materias.*.cursos.*.horarios')
    .optional({ values: [null] })
    .isArray()
    .withMessage('Los horarios indicados son inválidos'),
  body('materias.*.cursos.*.horarios.*')
    .customSanitizer(sanitizeOptionalInt)
    .optional({ values: [null] })
    .isInt({ min: 1 })
    .withMessage('El horario indicado es inválido')
];
