import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createSubject,
  deleteSubject,
  getSubject,
  listSubjectCategories,
  listSubjects,
  updateSubject,
  toggleSubjectState
} from '../../controllers/subjectController.js';
import {
  createSubjectValidator,
  subjectIdParam,
  toggleSubjectStateValidator
} from '../../validators/subjectValidator.js';

const router = Router();

router.use(authenticate);

router.get('/categorias', authorizeRoles('admin', 'preceptor', 'profesor'), listSubjectCategories);

router
  .route('/')
  .get(authorizeRoles('admin', 'preceptor', 'profesor'), listSubjects)
  .post(authorizeRoles('admin'), createSubjectValidator, validateRequest, createSubject);

router
  .route('/:id')
  .get(authorizeRoles('admin', 'preceptor', 'profesor'), subjectIdParam, validateRequest, getSubject)
  .put(authorizeRoles('admin'), subjectIdParam, validateRequest, updateSubject)
  .delete(authorizeRoles('admin'), subjectIdParam, validateRequest, deleteSubject);

router.patch(
  '/:id/toggle-estado',
  authorizeRoles('admin'),
  subjectIdParam,
  toggleSubjectStateValidator,
  validateRequest,
  toggleSubjectState
);

export default router;
