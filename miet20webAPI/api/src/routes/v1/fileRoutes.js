import { Router } from 'express';
import { deleteFile, getFile, uploadFile } from '../../controllers/fileController.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import { fileIdParamValidator, uploadFileValidator } from '../../validators/fileValidator.js';

const router = Router();

router.post('/', authenticate, uploadFileValidator, validateRequest, uploadFile);
router.get('/:fileId', fileIdParamValidator, validateRequest, getFile);
router.delete(
  '/:fileId',
  authenticate,
  authorizeRoles('admin'),
  fileIdParamValidator,
  validateRequest,
  deleteFile
);

export default router;
