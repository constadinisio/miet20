import { body } from 'express-validator';

export const loginValidator = [
  body('email').isEmail().withMessage('El correo electrónico es inválido'),
  body('password').isLength({ min: 6 }).withMessage('La contraseña es requerida')
];

export const googleLoginValidator = [
  body('idToken').isString().withMessage('El token de Google es obligatorio'),
  body('idToken').notEmpty().withMessage('El token de Google es obligatorio'),
  body('email').optional().isEmail().withMessage('El correo electrónico es inválido')
];

export const refreshTokenValidator = [
  body('refreshToken').notEmpty().withMessage('El refresh token es obligatorio')
];

export const switchRoleValidator = [
  body('roleId')
    .isInt({ gt: 0 })
    .withMessage('El identificador de rol es obligatorio y debe ser un entero positivo')
    .toInt()
    .bail(),
  body('refreshToken')
    .optional()
    .isString()
    .withMessage('El refresh token debe ser una cadena válida')
    .trim()
];

export const logoutValidator = [
  body('refreshToken').notEmpty().withMessage('El refresh token es obligatorio'),
  body('allDevices').optional().isBoolean().withMessage('El indicador allDevices debe ser booleano').toBoolean()
];
