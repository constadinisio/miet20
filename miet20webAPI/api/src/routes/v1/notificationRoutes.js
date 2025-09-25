import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createGroup,
  deleteGroup,
  listGroups,
  listGroupMembers,
  listNotificationOptions,
  listNotifications,
  markRecipientAsRead,
  confirmRecipient,
  sendNotification
} from '../../controllers/notificationController.js';
import {
  createGroupValidator,
  sendNotificationValidator,
  markRecipientValidator,
  confirmRecipientValidator,
  groupIdParam
} from '../../validators/notificationValidator.js';

const router = Router();

router.use(authenticate);

router.get('/opciones', authorizeRoles('profesor'), listNotificationOptions);

router
  .route('/grupos')
  .get(authorizeRoles('admin', 'preceptor'), listGroups)
  .post(authorizeRoles('admin'), createGroupValidator, validateRequest, createGroup);

router.delete(
  '/grupos/:groupId',
  authorizeRoles('admin'),
  groupIdParam,
  validateRequest,
  deleteGroup
);

router.get(
  '/grupos/:groupId/miembros',
  authorizeRoles('admin', 'preceptor'),
  groupIdParam,
  validateRequest,
  listGroupMembers
);

router.post(
  '/',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  sendNotificationValidator,
  validateRequest,
  sendNotification
);

router.get('/', listNotifications);

router.patch(
  '/destinatarios/:recipientId/leida',
  markRecipientValidator,
  validateRequest,
  markRecipientAsRead
);

router.patch(
  '/destinatarios/:recipientId/confirmar',
  confirmRecipientValidator,
  validateRequest,
  confirmRecipient
);

export default router;
