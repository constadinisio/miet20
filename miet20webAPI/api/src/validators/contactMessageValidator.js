import { body, param, query } from 'express-validator';

const allowedStatus = ['new', 'in_progress', 'resolved'];

const attachmentsValidator = body('attachments')
  .optional()
  .isArray({ max: 10 })
  .withMessage('Los adjuntos deben enviarse como lista')
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

const metadataValidator = body('metadata')
  .optional()
  .custom((value) => value !== null && typeof value === 'object' && !Array.isArray(value))
  .withMessage('La metadata debe ser un objeto JSON');

export const submitContactMessageValidator = [
  body('name').isString().isLength({ min: 2, max: 120 }).withMessage('Nombre inválido'),
  body('email').isEmail().withMessage('Email inválido'),
  body('subject').isString().isLength({ min: 3, max: 180 }).withMessage('Asunto inválido'),
  body('message').isString().isLength({ min: 10 }).withMessage('Mensaje inválido'),
  attachmentsValidator,
  metadataValidator
];

export const listContactMessagesValidator = [
  query('status').optional().isIn(allowedStatus).withMessage('Estado inválido'),
  query('page').optional().isInt({ min: 1 }).withMessage('El número de página debe ser positivo'),
  query('limit')
    .optional()
    .isInt({ min: 1, max: 50 })
    .withMessage('El límite debe estar entre 1 y 50'),
  query('search').optional().isString().isLength({ min: 1, max: 120 }),
  query('assignedTo').optional().isInt({ min: 1 })
];

export const contactMessageIdParamValidator = [
  param('messageId').isInt({ min: 1 }).withMessage('El identificador debe ser numérico')
];

export const updateContactMessageValidator = [
  body('status').optional().isIn(allowedStatus).withMessage('Estado inválido'),
  attachmentsValidator,
  metadataValidator,
  body('notes').optional().isString().isLength({ min: 1, max: 500 }).withMessage('Notas inválidas'),
  body('assignedTo').optional().isInt({ min: 1 }).withMessage('El responsable debe ser numérico')
];
