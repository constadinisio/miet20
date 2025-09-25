import { body, param } from 'express-validator';

export const studentIdParam = [param('id').isInt().withMessage('El id debe ser numérico')];

export const createStudentValidator = [
  body('nombre').notEmpty().withMessage('El nombre es obligatorio'),
  body('apellido').notEmpty().withMessage('El apellido es obligatorio'),
  body('dni').notEmpty().withMessage('El DNI es obligatorio'),
  body('fecha_nacimiento').isISO8601().withMessage('La fecha de nacimiento es inválida'),
  body('contrasena').optional().isLength({ min: 6 }).withMessage('La contraseña debe tener al menos 6 caracteres'),
  body('mail').optional().isEmail().withMessage('El correo es inválido')
];

export const enrollStudentValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('curso_id').isInt().withMessage('El curso es obligatorio'),
  body('estado').optional().isString().withMessage('El estado es inválido')
];

export const unenrollStudentValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  param('courseId').isInt().withMessage('El curso es obligatorio')
];

export const studentFamilyValidator = [
  body('padre_nombre').optional().isString().withMessage('El nombre del padre es inválido'),
  body('padre_tel').optional().isString().withMessage('El teléfono del padre es inválido'),
  body('padre_mail').optional().isEmail().withMessage('El correo del padre es inválido'),
  body('madre_nombre').optional().isString().withMessage('El nombre de la madre es inválido'),
  body('madre_tel').optional().isString().withMessage('El teléfono de la madre es inválido'),
  body('madre_mail').optional().isEmail().withMessage('El correo de la madre es inválido'),
  body('emergencia_nombre').optional().isString().withMessage('El contacto de emergencia es inválido'),
  body('emergencia_tel').optional().isString().withMessage('El teléfono de emergencia es inválido')
];

export const updateStudentCensusValidator = [
  body('ficha_censal')
    .isString()
    .notEmpty()
    .withMessage('La ficha censal es obligatoria')
];

export const progressStudentsValidator = [
  body('curso_origen_id')
    .isInt({ gt: 0 })
    .withMessage('El curso de origen es obligatorio'),
  body('alumnos')
    .isArray({ min: 1 })
    .withMessage('Debe indicar al menos un alumno a procesar'),
  body('alumnos.*.id')
    .isInt({ gt: 0 })
    .withMessage('El identificador de alumno es inválido'),
  body('alumnos.*.destino')
    .custom((value) => {
      if (value === null || value === undefined || value === '') {
        throw new Error('El destino es obligatorio');
      }

      if (typeof value === 'string') {
        const normalized = value.trim().toUpperCase();
        if (normalized === 'EGRESO') {
          return true;
        }

        const numeric = Number(normalized);
        if (Number.isInteger(numeric) && numeric > 0) {
          return true;
        }
      }

      if (typeof value === 'number' && Number.isInteger(value) && value > 0) {
        return true;
      }

      throw new Error('El destino indicado es inválido');
    })
];
