import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  assignSubject,
  assignTeacher,
  createCourse,
  deleteCourse,
  getCourse,
  getCourseAssignment,
  listCourseAssignments,
  listCourseStudents,
  listCourses,
  removeAssignment,
  updateCourse
} from '../../controllers/courseController.js';
import {
  assignSubjectValidator,
  assignTeacherValidator,
  assignmentIdParam,
  courseIdParam,
  createCourseValidator,
  listAssignmentsQueryValidator
} from '../../validators/courseValidator.js';

const router = Router();

router.use(authenticate);

router.get(
  '/asignaciones',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  listAssignmentsQueryValidator,
  validateRequest,
  listCourseAssignments
);

router
  .route('/')
  .get(authorizeRoles('admin', 'preceptor'), listCourses)
  .post(authorizeRoles('admin'), createCourseValidator, validateRequest, createCourse);

router
  .route('/:id')
  .get(authorizeRoles('admin', 'preceptor', 'profesor'), courseIdParam, validateRequest, getCourse)
  .put(authorizeRoles('admin'), courseIdParam, validateRequest, updateCourse)
  .delete(authorizeRoles('admin'), courseIdParam, validateRequest, deleteCourse);

router.get(
  '/:id/alumnos',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  courseIdParam,
  validateRequest,
  listCourseStudents
);

router.post(
  '/:id/materias',
  authorizeRoles('admin'),
  assignSubjectValidator,
  validateRequest,
  assignSubject
);

router.post(
  '/:id/profesores',
  authorizeRoles('admin'),
  assignTeacherValidator,
  validateRequest,
  assignTeacher
);

router.delete(
  '/asignaciones/:assignmentId',
  authorizeRoles('admin'),
  assignmentIdParam,
  validateRequest,
  removeAssignment
);

router.get(
  '/asignaciones/:assignmentId',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  assignmentIdParam,
  validateRequest,
  getCourseAssignment
);

export default router;
