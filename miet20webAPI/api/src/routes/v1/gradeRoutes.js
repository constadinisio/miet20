import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  listGradesByCourse,
  listGradesByStudent,
  recordGrade,
  updateGrade
} from '../../controllers/gradeController.js';
import {
  courseGradesValidator,
  recordGradeValidator,
  studentGradesValidator,
  updateGradeValidator
} from '../../validators/gradeValidator.js';

const router = Router();

router.use(authenticate);

router.get(
  '/alumnos/:studentId',
  authorizeRoles('admin', 'preceptor', 'profesor', 'alumno'),
  studentGradesValidator,
  validateRequest,
  listGradesByStudent
);
router.get(
  '/cursos/:courseId',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  courseGradesValidator,
  validateRequest,
  listGradesByCourse
);

router.post(
  '/cursos/:courseId/alumnos/:studentId',
  authorizeRoles('profesor'),
  recordGradeValidator,
  validateRequest,
  recordGrade
);

router.put(
  '/:gradeId',
  authorizeRoles('profesor'),
  updateGradeValidator,
  validateRequest,
  updateGrade
);

export default router;
