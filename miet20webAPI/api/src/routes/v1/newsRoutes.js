import { Router } from 'express';
import {
  listNews,
  listPublicNews,
  getNews,
  createNews,
  updateNews,
  updateNewsStatus,
  deleteNews,
  updateNewsCover
} from '../../controllers/newsController.js';
import {
  listNewsValidator,
  listPublicNewsValidator,
  newsIdParamValidator,
  createNewsValidator,
  updateNewsValidator,
  updateNewsStatusValidator,
  deleteNewsValidator,
  updateNewsCoverValidator
} from '../../validators/newsValidator.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';

const router = Router();

router.get('/public', listPublicNewsValidator, validateRequest, listPublicNews);

router.use(authenticate);

router.get('/', authorizeRoles('admin', 'preceptor', 'profesor'), listNewsValidator, validateRequest, listNews);
router.post('/', authorizeRoles('admin', 'preceptor', 'profesor'), createNewsValidator, validateRequest, createNews);
router.get('/:newsId', newsIdParamValidator, validateRequest, getNews);
router.put(
  '/:newsId',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  updateNewsValidator,
  validateRequest,
  updateNews
);
router.patch(
  '/:newsId/status',
  authorizeRoles('admin', 'preceptor'),
  updateNewsStatusValidator,
  validateRequest,
  updateNewsStatus
);
router.delete(
  '/:newsId',
  authorizeRoles('admin'),
  deleteNewsValidator,
  validateRequest,
  deleteNews
);
router.post(
  '/:newsId/cover',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  updateNewsCoverValidator,
  validateRequest,
  updateNewsCover
);

export default router;
