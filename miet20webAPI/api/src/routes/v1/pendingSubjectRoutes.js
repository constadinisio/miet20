import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  approvePendingSubject,
  createPendingSubject,
  listPendingSubjectHistory,
  listPendingSubjectTeachers,
  listPendingSubjects,
  searchPendingSubjects
} from '../../controllers/pendingSubjectController.js';
import {
  approvePendingSubjectValidator,
  createPendingSubjectValidator,
  pendingSubjectIdParam,
  searchSubjectsValidator
} from '../../validators/pendingSubjectValidator.js';
import { studentIdParam } from '../../validators/studentValidator.js';

const router = Router();

router.use(authenticate);

router.get(
  '/alumnos/:studentId',
  authorizeRoles('admin', 'preceptor'),
  studentIdParam,
  validateRequest,
  listPendingSubjects
);

router.get(
  '/alumnos/:studentId/historial',
  authorizeRoles('admin', 'preceptor'),
  studentIdParam,
  validateRequest,
  listPendingSubjectHistory
);

router.get(
  '/materias',
  authorizeRoles('admin', 'preceptor'),
  searchSubjectsValidator,
  validateRequest,
  searchPendingSubjects
);

router.post(
  '/',
  authorizeRoles('admin', 'preceptor'),
  createPendingSubjectValidator,
  validateRequest,
  createPendingSubject
);

router.post(
  '/:pendingId/aprobar',
  authorizeRoles('admin', 'preceptor'),
  pendingSubjectIdParam,
  approvePendingSubjectValidator,
  validateRequest,
  approvePendingSubject
);

router.get(
  '/:pendingId/docentes',
  authorizeRoles('admin', 'preceptor'),
  pendingSubjectIdParam,
  validateRequest,
  listPendingSubjectTeachers
);

export default router;
