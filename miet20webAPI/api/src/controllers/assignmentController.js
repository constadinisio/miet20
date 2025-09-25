import httpStatus from 'http-status';
import { assignmentService } from '../services/assignmentService.js';
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

export const createAssignment = catchAsync(async (req, res) => {
  const assignment = await assignmentService.createAssignment({
    materia_id: Number(req.body.materia_id),
    nombre: req.body.nombre,
    tipo: req.body.tipo,
    descripcion: req.body.descripcion
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: assignment });
});

export const listAssignments = catchAsync(async (req, res) => {
  const assignments = await assignmentService.listByCourse(Number(req.params.courseId), {
    subjectId: req.query.materia_id ? Number(req.query.materia_id) : undefined,
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : undefined
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: assignments });
});

export const updateAssignment = catchAsync(async (req, res) => {
  const assignment = await assignmentService.updateAssignment(Number(req.params.assignmentId), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: assignment });
});

export const deleteAssignment = catchAsync(async (req, res) => {
  await assignmentService.deleteAssignment(Number(req.params.assignmentId));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Trabajo eliminado correctamente' }
  });
});

export const submitAssignment = catchAsync(async (req, res) => {
  const submission = await assignmentService.createSubmission({
    trabajo_id: Number(req.params.assignmentId),
    alumno_id: Number(req.params.studentId),
    materia_id: Number(req.body.materia_id),
    nota: req.body.nota,
    profesor_id: Number(req.user?.id),
    rol: req.user?.role
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: submission });
});

export const listStudentAssignments = catchAsync(async (req, res) => {
  const studentId = Number(req.params.studentId);
  ensureStudentAccess(req, studentId);

  const submissions = await assignmentService.listStudentSubmissions(studentId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: submissions });
});

export const listCourseAssignmentGrades = catchAsync(async (req, res) => {
  const grades = await assignmentService.listCourseAssignmentGrades(Number(req.params.courseId), {
    subjectId: req.query.materia_id ? Number(req.query.materia_id) : undefined,
    studentIds: req.query.alumno_ids,
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : undefined
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: grades });
});
