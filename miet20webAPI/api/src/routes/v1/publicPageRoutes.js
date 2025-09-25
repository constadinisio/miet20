import { Router } from 'express';
import {
  createPublicPage,
  deletePublicPage,
  getPublishedPage,
  listPublishedPages,
  updatePublicPage
} from '../../controllers/publicPageController.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createPublicPageValidator,
  listPublishedPagesValidator,
  publicPageSlugParamValidator,
  updatePublicPageValidator
} from '../../validators/publicPageValidator.js';

const router = Router();

router.get('/', listPublishedPagesValidator, validateRequest, listPublishedPages);
router.get('/:slug', publicPageSlugParamValidator, validateRequest, getPublishedPage);

router.use(authenticate, authorizeRoles('admin'));

router.post('/', createPublicPageValidator, validateRequest, createPublicPage);
router.put(
  '/:slug',
  publicPageSlugParamValidator,
  updatePublicPageValidator,
  validateRequest,
  updatePublicPage
);
router.delete('/:slug', publicPageSlugParamValidator, validateRequest, deletePublicPage);

export default router;
