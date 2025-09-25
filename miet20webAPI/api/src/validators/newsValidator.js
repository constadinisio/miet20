import { body, param, query } from 'express-validator';

const statusValidator = body('status')
  .isString()
  .isIn(['draft', 'scheduled', 'published', 'archived'])
  .withMessage('Estado inválido');

const paginationValidators = [
  query('page').optional().isInt({ min: 1 }).toInt(),
  query('limit').optional().isInt({ min: 1, max: 50 }).toInt(),
  query('sortBy').optional().isIn(['created_at', 'published_at']),
  query('sortDirection').optional().isIn(['asc', 'desc'])
];

export const listNewsValidator = [
  ...paginationValidators,
  query('status').optional().isIn(['draft', 'scheduled', 'published', 'archived']),
  query('search').optional().isString().trim()
];

export const listPublicNewsValidator = paginationValidators;

export const newsIdParamValidator = [param('newsId').isInt({ min: 1 }).withMessage('Identificador de noticia inválido')];

export const createNewsValidator = [
  body('title').isString().trim().notEmpty().withMessage('El título es obligatorio'),
  body('content').optional().isString(),
  body('status').optional().isIn(['draft', 'scheduled', 'published', 'archived']),
  body('requiresConfirmation').optional().isBoolean().toBoolean(),
  body('publishFrom').optional().isISO8601(),
  body('publishUntil').optional().isISO8601(),
  body('audience').optional().isObject(),
  body('audience.roles').optional().isArray(),
  body('audience.courses').optional().isArray(),
  body('audience.users').optional().isArray()
];

export const updateNewsValidator = [
  ...newsIdParamValidator,
  body('title').optional().isString().trim().notEmpty(),
  body('content').optional().isString(),
  body('status').optional().isIn(['draft', 'scheduled', 'published', 'archived']),
  body('requiresConfirmation').optional().isBoolean().toBoolean(),
  body('publishFrom').optional().isISO8601(),
  body('publishUntil').optional().isISO8601(),
  body('audience').optional().isObject(),
  body('audience.roles').optional().isArray(),
  body('audience.courses').optional().isArray(),
  body('audience.users').optional().isArray()
];

export const updateNewsStatusValidator = [...newsIdParamValidator, statusValidator];

export const deleteNewsValidator = newsIdParamValidator;

export const updateNewsCoverValidator = [
  ...newsIdParamValidator,
  body('coverData').isString().withMessage('La imagen codificada es obligatoria'),
  body('coverMime')
    .isString()
    .isIn(['image/jpeg', 'image/png', 'image/webp'])
    .withMessage('El tipo de imagen no es válido'),
  body('coverName').optional().isString().trim()
];
