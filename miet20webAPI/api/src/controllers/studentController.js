import httpStatus from 'http-status';
import { studentService } from '../services/studentService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';
import { ApiError } from '../utils/ApiError.js';

const ensureSelfOrPrivileged = (req, studentId) => {
  if (!req.user) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'Autenticación requerida');
  }

  if (req.user.role === 'alumno' && Number(req.user.id) !== Number(studentId)) {
    throw new ApiError(httpStatus.FORBIDDEN, 'No tenés permisos para operar sobre otro alumno');
  }
};

export const createStudent = catchAsync(async (req, res) => {
  const student = await studentService.createStudent(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: student });
});

export const listStudents = catchAsync(async (req, res) => {
  const { items = [], pagination = null } = await studentService.listStudents(req.query);

  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: items,
    meta: pagination
  });
});

export const getStudent = catchAsync(async (req, res) => {
  const student = await studentService.getStudent(Number(req.params.id));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: student });
});

export const updateStudent = catchAsync(async (req, res) => {
  const student = await studentService.updateStudent(Number(req.params.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: student });
});

export const deleteStudent = catchAsync(async (req, res) => {
  await studentService.deleteStudent(Number(req.params.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Alumno eliminado correctamente' }
  });
});

export const enrollStudent = catchAsync(async (req, res) => {
  const { enrollment, created } = await studentService.enrollStudent({
    alumnoId: Number(req.params.id),
    cursoId: Number(req.body.curso_id),
    estado: req.body.estado
  });
  const message = created
    ? 'Alumno inscripto correctamente'
    : 'El alumno ya se encontraba inscripto en el curso';
  sendSuccess({
    req,
    res,
    statusCode: created ? httpStatus.CREATED : httpStatus.OK,
    data: { enrollment, created, message }
  });
});

export const unenrollStudent = catchAsync(async (req, res) => {
  await studentService.unenrollStudent({
    alumnoId: Number(req.params.id),
    cursoId: Number(req.params.courseId)
  });
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Inscripción cancelada correctamente' }
  });
});

export const getStudentFamily = catchAsync(async (req, res) => {
  const studentId = Number(req.params.id);
  ensureSelfOrPrivileged(req, studentId);

  const family = await studentService.getFamilyData(studentId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: family });
});

export const updateStudentFamily = catchAsync(async (req, res) => {
  const studentId = Number(req.params.id);
  ensureSelfOrPrivileged(req, studentId);

  const family = await studentService.updateFamilyData(studentId, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: family });
});

export const updateStudentCensus = catchAsync(async (req, res) => {
  const studentId = Number(req.params.id);
  ensureSelfOrPrivileged(req, studentId);

  const student = await studentService.updateCensusData(studentId, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: student });
});

export const progressStudents = catchAsync(async (req, res) => {
  const result = await studentService.progressStudents({
    courseOriginId: req.body.curso_origen_id,
    students: req.body.alumnos
  });

  const message = result.errors.length
    ? 'La progresión se aplicó con advertencias'
    : 'Progresión aplicada con éxito';

  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: {
      ...result,
      message
    },
    meta: {
      processed: Array.isArray(result.processed) ? result.processed.length : 0,
      errors: Array.isArray(result.errors) ? result.errors.length : 0
    }
  });
});
