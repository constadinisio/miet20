import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  getRegistration,
  listRegistrations,
  reviewRegistration,
  submitRegistration
} from '../../controllers/registrationController.js';
import {
  createRegistrationValidator,
  listRegistrationsValidator,
  registrationIdValidator,
  reviewRegistrationValidator
} from '../../validators/registrationValidator.js';

const router = Router();

router.post('/', createRegistrationValidator, validateRequest, submitRegistration);

router.use(authenticate);
router.use(authorizeRoles('admin'));

router.get('/', listRegistrationsValidator, validateRequest, listRegistrations);

router
  .route('/:registrationId')
  .get(registrationIdValidator, validateRequest, getRegistration)
  .patch(reviewRegistrationValidator, validateRequest, reviewRegistration);

export default router;
