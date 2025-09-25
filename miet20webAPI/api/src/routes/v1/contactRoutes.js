import { Router } from 'express';
import {
  getContactMessage,
  listContactMessages,
  submitContactMessage,
  updateContactMessage
} from '../../controllers/contactMessageController.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  contactMessageIdParamValidator,
  listContactMessagesValidator,
  submitContactMessageValidator,
  updateContactMessageValidator
} from '../../validators/contactMessageValidator.js';

const router = Router();

router.post('/messages', submitContactMessageValidator, validateRequest, submitContactMessage);

router.use(authenticate, authorizeRoles('admin', 'preceptor'));

router.get('/messages', listContactMessagesValidator, validateRequest, listContactMessages);
router.get('/messages/:messageId', contactMessageIdParamValidator, validateRequest, getContactMessage);
router.patch(
  '/messages/:messageId',
  contactMessageIdParamValidator,
  updateContactMessageValidator,
  validateRequest,
  updateContactMessage
);

export default router;
