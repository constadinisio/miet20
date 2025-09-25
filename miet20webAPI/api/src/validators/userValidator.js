import { body, param, query } from 'express-validator';

const ROLE_SLUGS = ['admin', 'preceptor', 'profesor', 'alumno', 'spei'];
const ROLE_IDS = [1, 2, 3, 4, 5];

const validateRole = (value) => {
  if (typeof value === 'string' && ROLE_SLUGS.includes(value)) {
    return true;
  }

  const numeric = Number(value);
  if (!Number.isNaN(numeric) && ROLE_IDS.includes(numeric)) {
    return true;
  }

  throw new Error('Rol inválido');
};

export const createUserValidator = [
  body('nombre').notEmpty().withMessage('El nombre es obligatorio'),
  body('apellido').notEmpty().withMessage('El apellido es obligatorio'),
  body('mail').isEmail().withMessage('El correo electrónico es inválido'),
  body('contrasena').isLength({ min: 6 }).withMessage('La contraseña debe tener al menos 6 caracteres'),
  body('rol').notEmpty().withMessage('El rol es obligatorio').bail().custom(validateRole)
];

export const updateUserValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('mail').optional().isEmail().withMessage('El correo electrónico es inválido'),
  body('contrasena').optional().isLength({ min: 6 }).withMessage('La contraseña debe tener al menos 6 caracteres'),
  body('rol').optional().custom(validateRole),
  body('status').optional().isInt().withMessage('El estado debe ser numérico'),
  body('permNoticia').optional().isIn([0, 1, true, false]).withMessage('Valor inválido para permNoticia'),
  body('permSubidaArch').optional().isIn([0, 1, true, false]).withMessage('Valor inválido para permSubidaArch'),
  body('rolesAdicionales').optional().isArray().withMessage('rolesAdicionales debe ser un arreglo'),
  body('rolesAdicionales.*').optional().isInt().withMessage('Cada rol adicional debe ser numérico')
];

export const updateUserProfileValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('nombre').optional().isString().withMessage('El nombre es inválido'),
  body('apellido').optional().isString().withMessage('El apellido es inválido'),
  body('mail').optional().isEmail().withMessage('El correo electrónico es inválido'),
  body('telefono').optional().isString().withMessage('El teléfono es inválido'),
  body('direccion').optional().isString().withMessage('La dirección es inválida'),
  body('foto_url').optional().isString().withMessage('La URL de la foto es inválida'),
  body('dni').optional().isString().withMessage('El DNI es inválido'),
  body('fecha_nacimiento')
    .optional({ checkFalsy: true })
    .isISO8601()
    .withMessage('La fecha de nacimiento debe tener un formato válido'),
  body('ficha_censal').optional().isString().withMessage('La ficha censal es inválida')
];

export const updateUserPasswordValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('currentPassword').notEmpty().withMessage('La contraseña actual es obligatoria'),
  body('newPassword')
    .isLength({ min: 6 })
    .withMessage('La nueva contraseña debe tener al menos 6 caracteres')
];

export const approveUserValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  body('rol').optional().custom(validateRole),
  body('rolesAdicionales').optional().isArray().withMessage('rolesAdicionales debe ser un arreglo'),
  body('rolesAdicionales.*')
    .optional()
    .isInt()
    .withMessage('Cada rol adicional debe ser numérico')
];

export const rejectUserValidator = [
  param('id').isInt().withMessage('El id debe ser numérico')
];

export const userIdParam = [param('id').isInt().withMessage('El id debe ser numérico')];

export const requiredFieldsQueryValidator = [
  query('roleId')
    .optional({ checkFalsy: true })
    .isInt()
    .withMessage('El rol debe ser numérico')
];

export const userMissingFieldsValidator = [
  param('id').isInt().withMessage('El id debe ser numérico'),
  query('roleId')
    .optional({ checkFalsy: true })
    .isInt()
    .withMessage('El rol debe ser numérico')
];
