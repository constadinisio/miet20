import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  getAllowedSubjectDays,
  getCourseSubjectsForDate,
  getCourseAttendanceSummary,
  importProfessorAttendance,
  importSubjectAttendance,
  listAttendance,
  listCourseGeneralAttendance,
  listStudentAttendance,
  listWeeklySubjectAttendance,
  recordAttendance,
  saveSubjectAttendanceMatrix,
  updateAttendance,
  getCourseAttendanceRange
} from '../../controllers/attendanceController.js';
import {
  allowedDaysValidator,
  courseDaySubjectsValidator,
  courseSummaryValidator,
  courseGeneralAttendanceValidator,
  courseGeneralRangeValidator,
  importProfessorValidator,
  importSubjectValidator,
  listAttendanceValidator,
  studentAttendanceValidator,
  recordAttendanceValidator,
  saveSubjectMatrixValidator,
  updateAttendanceValidator,
  weeklySubjectValidator
} from '../../validators/attendanceValidator.js';

const router = Router();

router.use(authenticate);

router.get(
  '/alumnos/:studentId',
  authorizeRoles('admin', 'preceptor', 'profesor', 'alumno'),
  studentAttendanceValidator,
  validateRequest,
  listStudentAttendance
);

router.get(
  '/cursos/:courseId',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  listAttendanceValidator,
  validateRequest,
  listAttendance
);

router.get(
  '/cursos/:courseId/general',
  authorizeRoles('admin', 'preceptor'),
  courseGeneralAttendanceValidator,
  validateRequest,
  listCourseGeneralAttendance
);

router.get(
  '/cursos/:courseId/materias-del-dia',
  authorizeRoles('admin', 'preceptor'),
  courseDaySubjectsValidator,
  validateRequest,
  getCourseSubjectsForDate
);

router.post(
  '/cursos/:courseId',
  authorizeRoles('profesor', 'preceptor'),
  recordAttendanceValidator,
  validateRequest,
  recordAttendance
);

router.post(
  '/cursos/:courseId/importar-profesor',
  authorizeRoles('preceptor'),
  importProfessorValidator,
  validateRequest,
  importProfessorAttendance
);

router.put(
  '/cursos/:courseId/alumnos/:studentId',
  authorizeRoles('profesor', 'preceptor'),
  updateAttendanceValidator,
  validateRequest,
  updateAttendance
);

router.get(
  '/cursos/:courseId/materias/:subjectId/habilitados',
  authorizeRoles('profesor'),
  allowedDaysValidator,
  validateRequest,
  getAllowedSubjectDays
);

router.get(
  '/cursos/:courseId/materias/:subjectId/semanal',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  weeklySubjectValidator,
  validateRequest,
  listWeeklySubjectAttendance
);

router.post(
  '/cursos/:courseId/materias/:subjectId/matriz',
  authorizeRoles('profesor'),
  saveSubjectMatrixValidator,
  validateRequest,
  saveSubjectAttendanceMatrix
);

router.post(
  '/cursos/:courseId/materias/:subjectId/importar-general',
  authorizeRoles('profesor'),
  importSubjectValidator,
  validateRequest,
  importSubjectAttendance
);

router.get(
  '/cursos/:courseId/resumen',
  authorizeRoles('admin', 'preceptor', 'profesor'),
  courseSummaryValidator,
  validateRequest,
  getCourseAttendanceSummary
);

router.get(
  '/cursos/:courseId/rango',
  authorizeRoles('admin', 'preceptor'),
  courseGeneralRangeValidator,
  validateRequest,
  getCourseAttendanceRange
);

export default router;
