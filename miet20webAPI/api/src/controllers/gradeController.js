import httpStatus from 'http-status';
import { gradeService } from '../services/gradeService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';
import { ApiError } from '../utils/ApiError.js';

const ensureStudentAccess = (req, studentId) => {
  if (!req.user) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'Autenticación requerida');
  }

  if (req.user.role === 'alumno' && Number(req.user.id) !== Number(studentId)) {
    throw new ApiError(httpStatus.FORBIDDEN, 'No tenés permisos para acceder a otro alumno');
  }
};

export const recordGrade = catchAsync(async (req, res) => {
  const grade = await gradeService.recordGrade({
    profesor_id: Number(req.user?.id),
    curso_id: Number(req.params.courseId),
    alumno_id: Number(req.params.studentId),
    materia_id: Number(req.body.materia_id),
    periodo: req.body.periodo,
    nota: req.body.nota,
    promedio_actividades: req.body.promedio_actividades
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: grade });
});

export const updateGrade = catchAsync(async (req, res) => {
  const grade = await gradeService.updateGrade(
    Number(req.params.gradeId),
    req.body,
    Number(req.user?.id)
  );
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: grade });
});

export const listGradesByStudent = catchAsync(async (req, res) => {
  const studentId = Number(req.params.studentId);
  ensureStudentAccess(req, studentId);

  const grades = await gradeService.listByStudent(studentId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: grades });
});

export const listGradesByCourse = catchAsync(async (req, res) => {
  const grades = await gradeService.listByCourse(Number(req.params.courseId), req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: grades });
});
