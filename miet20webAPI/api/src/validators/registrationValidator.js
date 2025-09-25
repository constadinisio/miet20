import { body, param, query } from 'express-validator';

export const createRegistrationValidator = [
  body('mail').isEmail().withMessage('El correo electrónico es obligatorio').normalizeEmail(),
  body('nombre').isString().trim().notEmpty().withMessage('El nombre es obligatorio'),
  body('apellido').isString().trim().notEmpty().withMessage('El apellido es obligatorio'),
  body('dni').isString().trim().notEmpty().withMessage('El DNI es obligatorio'),
  body('telefono').optional().isString().trim(),
  body('direccion').optional().isString().trim(),
  body('fecha_nacimiento')
    .isISO8601()
    .withMessage('La fecha de nacimiento debe tener formato válido'),
  body('contrasena').isLength({ min: 6 }).withMessage('La contraseña debe tener al menos 6 caracteres')
];

export const listRegistrationsValidator = [
  query('status').optional().isIn(['pending', 'approved', 'rejected']),
  query('mail').optional().isEmail().normalizeEmail(),
  query('dni').optional().isString().trim()
];

export const registrationIdValidator = [
  param('registrationId').isInt({ min: 1 }).withMessage('El identificador de registro es inválido')
];

export const reviewRegistrationValidator = [
  ...registrationIdValidator,
  body('decision').isIn(['approve', 'reject']).withMessage('La decisión indicada no es válida'),
  body('rol')
    .if(body('decision').equals('approve'))
    .isInt({ min: 1 })
    .withMessage('Debe seleccionar un rol válido')
    .toInt(),
  body('permNoticia').optional().isBoolean().toBoolean(),
  body('permSubidaArch').optional().isBoolean().toBoolean(),
  body('telefono').optional().isString().trim(),
  body('direccion').optional().isString().trim(),
  body('fecha_nacimiento').optional().isISO8601().withMessage('La fecha de nacimiento debe tener formato válido')
];
