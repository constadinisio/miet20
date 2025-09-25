import { body, query } from 'express-validator';

const paginationValidators = [
  query('page').optional().isInt({ min: 1 }).toInt(),
  query('limit').optional().isInt({ min: 1, max: 50 }).toInt(),
  query('sortDirection').optional().isIn(['asc', 'desc'])
];

export const listCategoriesValidator = [];

export const listItemsValidator = [
  ...paginationValidators,
  query('category').optional().isString().trim().isLength({ min: 1, max: 120 }),
  query('search').optional().isString().trim().isLength({ min: 1, max: 255 })
];

export const createGalleryItemValidator = [
  body('category').isString().trim().isLength({ min: 1, max: 120 }).withMessage('La categoría es obligatoria'),
  body('author').isString().trim().isLength({ min: 1, max: 255 }).withMessage('El autor es obligatorio'),
  body('description').isString().trim().isLength({ min: 1, max: 1000 }).withMessage('La descripción es obligatoria'),
  body('fileName').optional().isString().trim().isLength({ min: 1, max: 255 }),
  body('fileMime')
    .isString()
    .isIn(['image/jpeg', 'image/png', 'image/webp'])
    .withMessage('Tipo de archivo no permitido'),
  body('fileData').isString().withMessage('La imagen codificada es obligatoria')
];
