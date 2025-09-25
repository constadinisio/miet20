import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createUser,
  deleteUser,
  getUser,
  listUsers,
  updateUser,
  updateUserPassword,
  updateUserProfile,
  approveUser,
  rejectUser,
  getUserFamily,
  updateUserFamily,
  getRequiredProfileFields,
  getUserMissingProfileFields
} from '../../controllers/userController.js';
import {
  createUserValidator,
  updateUserPasswordValidator,
  updateUserProfileValidator,
  updateUserValidator,
  approveUserValidator,
  rejectUserValidator,
  userIdParam,
  requiredFieldsQueryValidator,
  userMissingFieldsValidator
} from '../../validators/userValidator.js';
import { studentFamilyValidator } from '../../validators/studentValidator.js';

const router = Router();

router.use(authenticate);

router
  .route('/')
  .get(authorizeRoles('admin'), listUsers)
  .post(authorizeRoles('admin'), createUserValidator, validateRequest, createUser);

router.get(
  '/required-fields',
  authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
  requiredFieldsQueryValidator,
  validateRequest,
  getRequiredProfileFields
);

router
  .route('/:id')
  .get(
    authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
    updateUserValidator,
    validateRequest,
    getUser
  )
  .put(authorizeRoles('admin'), updateUserValidator, validateRequest, updateUser)
  .patch(
    authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
    updateUserProfileValidator,
    validateRequest,
    updateUserProfile
  )
  .delete(authorizeRoles('admin'), updateUserValidator, validateRequest, deleteUser);

router.patch(
  '/:id/password',
  authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
  updateUserPasswordValidator,
  validateRequest,
  updateUserPassword
);

router.get(
  '/:id/missing-fields',
  authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
  userMissingFieldsValidator,
  validateRequest,
  getUserMissingProfileFields
);

router.post(
  '/:id/aprobar',
  authorizeRoles('admin'),
  approveUserValidator,
  validateRequest,
  approveUser
);

router.post(
  '/:id/rechazar',
  authorizeRoles('admin'),
  rejectUserValidator,
  validateRequest,
  rejectUser
);

router
  .route('/:id/familia')
  .get(
    authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
    userIdParam,
    validateRequest,
    getUserFamily
  )
  .put(
    authorizeRoles('admin', 'preceptor', 'profesor', 'alumno', 'spei'),
    userIdParam,
    studentFamilyValidator,
    validateRequest,
    updateUserFamily
  );

export default router;
