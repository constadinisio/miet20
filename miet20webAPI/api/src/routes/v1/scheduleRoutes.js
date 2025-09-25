import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createSchedule,
  createScheduleForAssignment,
  deleteSchedule,
  listSchedules,
  listSchedulesByAssignment,
  listTeacherAssignments,
  updateSchedule
} from '../../controllers/scheduleController.js';
import { assignmentIdParam, courseParam } from '../../validators/courseValidator.js';
import { scheduleCreateValidator, scheduleIdParam, teacherParam } from '../../validators/scheduleValidator.js';

const router = Router();

router.use(authenticate);

router
  .route('/cursos/:courseId')
  .get(authorizeRoles('admin', 'preceptor', 'profesor'), courseParam, validateRequest, listSchedules)
  .post(
    authorizeRoles('admin'),
    [...courseParam, ...scheduleCreateValidator],
    validateRequest,
    createSchedule
  );

router
  .route('/cursos/:courseId/:scheduleId')
  .put(authorizeRoles('admin'), [...courseParam, ...scheduleIdParam], validateRequest, updateSchedule)
  .delete(authorizeRoles('admin'), [...courseParam, ...scheduleIdParam], validateRequest, deleteSchedule);

router
  .route('/asignaciones/:assignmentId')
  .get(
    authorizeRoles('admin', 'preceptor', 'profesor'),
    assignmentIdParam,
    validateRequest,
    listSchedulesByAssignment
  )
  .post(
    authorizeRoles('admin'),
    [...assignmentIdParam, ...scheduleCreateValidator],
    validateRequest,
    createScheduleForAssignment
  );

router.delete(
  '/:scheduleId',
  authorizeRoles('admin'),
  scheduleIdParam,
  validateRequest,
  deleteSchedule
);

router
  .route('/profesores/:teacherId/asignaciones')
  .get(authorizeRoles('admin', 'preceptor', 'profesor'), teacherParam, validateRequest, listTeacherAssignments);

export default router;
