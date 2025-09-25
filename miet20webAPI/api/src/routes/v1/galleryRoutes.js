import { Router } from 'express';
import {
  listGalleryCategories,
  listGalleryItems,
  createGalleryItem
} from '../../controllers/galleryController.js';
import {
  listCategoriesValidator,
  listItemsValidator,
  createGalleryItemValidator
} from '../../validators/galleryValidator.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import { authenticate } from '../../middlewares/authMiddleware.js';

const router = Router();

router.get('/categories', listCategoriesValidator, validateRequest, listGalleryCategories);
router.get('/items', listItemsValidator, validateRequest, listGalleryItems);
router.post('/items', authenticate, createGalleryItemValidator, validateRequest, createGalleryItem);

export default router;
