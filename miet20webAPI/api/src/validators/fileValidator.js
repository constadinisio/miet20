import { body, param } from 'express-validator';

const base64Regex = /^(?:[A-Z0-9+/]{4})*(?:[A-Z0-9+/]{2}==|[A-Z0-9+/]{3}=)?$/i;

export const uploadFileValidator = [
  body('fileName').optional().isString().isLength({ min: 1, max: 255 }).withMessage('Nombre inválido'),
  body('fileMime').isString().isLength({ min: 3, max: 120 }).withMessage('MIME inválido'),
  body('fileData')
    .isString()
    .matches(base64Regex)
    .withMessage('El archivo debe estar codificado en base64'),
  body('isPublic').optional().isBoolean().withMessage('El flag isPublic debe ser booleano')
];

export const fileIdParamValidator = [
  param('fileId').isInt({ min: 1 }).withMessage('El identificador de archivo debe ser numérico')
];
