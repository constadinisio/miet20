import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  getConfiguration,
  updateConfiguration
} from '../../controllers/configurationController.js';
import { updateConfigurationValidator } from '../../validators/configurationValidator.js';

const router = Router();

router.use(authenticate);
router.use(authorizeRoles('admin'));

router.get('/', getConfiguration);
router.put('/', updateConfigurationValidator, validateRequest, updateConfiguration);

export default router;
