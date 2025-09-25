import { body, param } from 'express-validator';

export const createGroupValidator = [
  body('nombre').notEmpty().withMessage('El nombre es obligatorio'),
  body('descripcion').optional().isString(),
  body('miembros')
    .optional()
    .isArray({ min: 0 })
    .withMessage('Los miembros deben enviarse como una lista'),
  body('miembros.*')
    .optional()
    .isInt()
    .withMessage('Cada miembro debe ser un identificador numérico válido')
];

export const groupIdParam = [
  param('groupId').isInt().withMessage('El grupo es inválido')
];

const NOTIFICATION_TYPES = ['INDIVIDUAL', 'ROL', 'GRUPO', 'CURSO', 'GLOBAL'];

export const sendNotificationValidator = [
  body('titulo').notEmpty().withMessage('El título es obligatorio'),
  body('mensaje').optional().isString(),
  body('contenido').optional().isString(),
  body('tipo')
    .optional()
    .isString()
    .custom((value) => NOTIFICATION_TYPES.includes(value.toUpperCase()))
    .withMessage('El tipo de notificación es inválido'),
  body('destinatario_id').optional().isInt().withMessage('El destinatario es inválido'),
  body('destinatarios')
    .optional()
    .isArray({ min: 0 })
    .withMessage('Los destinatarios deben enviarse como una lista'),
  body('destinatarios.*')
    .optional()
    .isInt()
    .withMessage('Cada destinatario debe ser numérico'),
  body('destinatario_ids')
    .optional()
    .isArray({ min: 0 })
    .withMessage('Los destinatarios deben enviarse como una lista'),
  body('destinatario_ids.*')
    .optional()
    .isInt()
    .withMessage('Cada destinatario debe ser numérico'),
  body('roles').optional().isArray({ min: 0 }).withMessage('Los roles deben ser una lista'),
  body('roles.*').optional().isInt().withMessage('Cada rol debe ser numérico'),
  body('destino').optional().isArray({ min: 0 }).withMessage('El destino debe ser una lista'),
  body('destino.*').optional().isInt().withMessage('Cada destino debe ser numérico'),
  body('grupos').optional().isArray({ min: 0 }).withMessage('Los grupos deben ser una lista'),
  body('grupos.*').optional().isInt().withMessage('Cada grupo debe ser numérico'),
  body('grupo_id').optional().isInt().withMessage('El grupo es inválido'),
  body('cursos').optional().isArray({ min: 0 }).withMessage('Los cursos deben ser una lista'),
  body('cursos.*').optional().isInt().withMessage('Cada curso debe ser numérico'),
  body('requiere_confirmacion')
    .optional()
    .isBoolean()
    .withMessage('El valor de confirmación debe ser booleano'),
  body('requiereConfirmacion')
    .optional()
    .isBoolean()
    .withMessage('El valor de confirmación debe ser booleano'),
  body().custom((value, { req }) => {
    const { contenido, mensaje } = req.body;
    if (!contenido && !mensaje) {
      throw new Error('El contenido es obligatorio');
    }
    return true;
  })
];

export const markRecipientValidator = [
  param('recipientId').isInt().withMessage('El destinatario es inválido')
];

export const confirmRecipientValidator = [
  param('recipientId').isInt().withMessage('El destinatario es inválido')
];
