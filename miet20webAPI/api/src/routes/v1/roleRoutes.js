import { Router } from 'express';
import { listRoles } from '../../controllers/roleController.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';

const router = Router();

router.get('/', authenticate, authorizeRoles('admin'), listRoles);

export default router;
