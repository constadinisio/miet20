import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createStudent,
  deleteStudent,
  enrollStudent,
  getStudent,
  getStudentFamily,
  listStudents,
  progressStudents,
  unenrollStudent,
  updateStudent,
  updateStudentCensus,
  updateStudentFamily
} from '../../controllers/studentController.js';
import {
  createStudentValidator,
  enrollStudentValidator,
  progressStudentsValidator,
  studentFamilyValidator,
  studentIdParam,
  unenrollStudentValidator,
  updateStudentCensusValidator
} from '../../validators/studentValidator.js';

const router = Router();

router.use(authenticate);

router
  .route('/')
  .get(authorizeRoles('admin', 'preceptor'), listStudents)
  .post(authorizeRoles('admin', 'preceptor'), createStudentValidator, validateRequest, createStudent);

router.post(
  '/progresion',
  authorizeRoles('admin'),
  progressStudentsValidator,
  validateRequest,
  progressStudents
);

router
  .route('/:id')
  .get(authorizeRoles('admin', 'preceptor'), studentIdParam, validateRequest, getStudent)
  .put(authorizeRoles('admin', 'preceptor'), studentIdParam, validateRequest, updateStudent)
  .delete(authorizeRoles('admin'), studentIdParam, validateRequest, deleteStudent);

router.post(
  '/:id/cursos',
  authorizeRoles('admin', 'preceptor'),
  enrollStudentValidator,
  validateRequest,
  enrollStudent
);

router.delete(
  '/:id/cursos/:courseId',
  authorizeRoles('admin', 'preceptor'),
  unenrollStudentValidator,
  validateRequest,
  unenrollStudent
);

router
  .route('/:id/familia')
  .get(authorizeRoles('admin', 'preceptor', 'alumno'), studentIdParam, validateRequest, getStudentFamily)
  .put(
    authorizeRoles('admin', 'preceptor', 'alumno'),
    studentIdParam,
    studentFamilyValidator,
    validateRequest,
    updateStudentFamily
  );

router
  .route('/:id/ficha-censal')
  .put(
    authorizeRoles('admin', 'preceptor', 'alumno'),
    studentIdParam,
    updateStudentCensusValidator,
    validateRequest,
    updateStudentCensus
  );

export default router;
