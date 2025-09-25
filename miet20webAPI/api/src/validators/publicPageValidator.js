import { body, param, query } from 'express-validator';

const slugRegex = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const allowedStatus = ['draft', 'published'];

const slugBodyValidator = body('slug')
  .isString()
  .trim()
  .matches(slugRegex)
  .withMessage('El slug debe contener solo minúsculas, números y guiones');

const titleValidator = body('title')
  .isString()
  .isLength({ min: 3, max: 180 })
  .withMessage('El título debe tener entre 3 y 180 caracteres');

const contentValidator = body('content')
  .isString()
  .isLength({ min: 10 })
  .withMessage('El contenido debe tener al menos 10 caracteres');

const statusValidator = body('status')
  .optional()
  .isIn(allowedStatus)
  .withMessage('Estado inválido');

const attachmentsValidator = body('attachments')
  .optional()
  .isArray({ max: 20 })
  .withMessage('Los adjuntos deben enviarse como arreglo')
  .custom((attachments) =>
    attachments.every((attachment) => {
      if (typeof attachment === 'number') {
        return attachment > 0;
      }

      if (typeof attachment === 'string') {
        return /^\d+$/.test(attachment);
      }

      if (attachment && typeof attachment === 'object') {
        return attachment.id !== undefined && Number(attachment.id) > 0;
      }

      return false;
    })
  )
  .withMessage('Cada adjunto debe contener un identificador numérico válido');

const sectionsValidator = body('sections')
  .optional()
  .isArray({ max: 20 })
  .withMessage('Las secciones deben enviarse como arreglo')
  .custom((sections) =>
    sections.every((section) =>
      section &&
      typeof section === 'object' &&
      (section.title === undefined || typeof section.title === 'string') &&
      (section.content === undefined || typeof section.content === 'string')
    )
  )
  .withMessage('Cada sección debe contener campos válidos');

const seoValidator = body('seo')
  .optional()
  .custom((seo) => seo && typeof seo === 'object' && !Array.isArray(seo))
  .withMessage('La metadata SEO debe ser un objeto');

export const listPublishedPagesValidator = [
  query('page').optional().isInt({ min: 1 }),
  query('limit').optional().isInt({ min: 1, max: 50 }),
  query('search').optional().isString().isLength({ min: 1, max: 120 })
];

export const createPublicPageValidator = [
  slugBodyValidator,
  titleValidator,
  contentValidator,
  statusValidator,
  attachmentsValidator,
  sectionsValidator,
  seoValidator
];

export const updatePublicPageValidator = [
  body('title').optional().isString().isLength({ min: 3, max: 180 }),
  body('content').optional().isString().isLength({ min: 10 }),
  statusValidator,
  attachmentsValidator,
  sectionsValidator,
  seoValidator
];

export const publicPageSlugParamValidator = [
  param('slug').isString().matches(slugRegex).withMessage('Slug inválido')
];
