import { body, param } from 'express-validator';

export const subjectIdParam = [param('id').isInt().withMessage('La materia es inválida')];

export const createSubjectValidator = [
  body('nombre').notEmpty().withMessage('El nombre es obligatorio'),
  body('codigo').optional().isString(),
  body('categoria_id')
    .exists()
    .withMessage('La categoría es obligatoria')
    .bail()
    .isInt({ min: 1 })
    .withMessage('La categoría es inválida'),
  body('es_contraturno').optional().isInt({ min: 0, max: 1 }).withMessage('El indicador de contraturno es inválido')
];

export const toggleSubjectStateValidator = [
  body('estado')
    .optional()
    .isIn(['activo', 'inactivo'])
    .withMessage('El estado actual es inválido')
];
