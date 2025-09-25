import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createAssignment,
  deleteAssignment,
  listAssignments,
  listCourseAssignmentGrades,
  listStudentAssignments,
  submitAssignment,
  updateAssignment
} from '../../controllers/assignmentController.js';
import {
  createAssignmentValidator,
  courseAssignmentParams,
  courseAssignmentGradesValidator,
  listStudentAssignmentsValidator,
  submitAssignmentValidator,
  updateAssignmentValidator
} from '../../validators/assignmentValidator.js';

const router = Router();

router.use(authenticate);

router.get(
  '/cursos/:courseId',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  courseAssignmentParams,
  validateRequest,
  listAssignments
);

router.get(
  '/cursos/:courseId/notas',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  courseAssignmentParams,
  courseAssignmentGradesValidator,
  validateRequest,
  listCourseAssignmentGrades
);

router.post(
  '/cursos/:courseId',
  authorizeRoles('profesor'),
  createAssignmentValidator,
  validateRequest,
  createAssignment
);

router
  .route('/:assignmentId')
  .put(authorizeRoles('profesor'), updateAssignmentValidator, validateRequest, updateAssignment)
  .delete(authorizeRoles('profesor'), updateAssignmentValidator, validateRequest, deleteAssignment);

router.post(
  '/:assignmentId/alumnos/:studentId/notas',
  authorizeRoles('profesor', 'preceptor'),
  submitAssignmentValidator,
  validateRequest,
  submitAssignment
);

router.get(
  '/alumnos/:studentId/notas',
  authorizeRoles('admin', 'preceptor', 'profesor', 'alumno'),
  listStudentAssignmentsValidator,
  validateRequest,
  listStudentAssignments
);

export default router;
