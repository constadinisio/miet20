import httpStatus from 'http-status';
import { attendanceService } from '../services/attendanceService.js';
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

export const recordAttendance = catchAsync(async (req, res) => {
  const attendance = await attendanceService.recordAttendance({
    curso_id: Number(req.params.courseId),
    fecha: req.body.fecha,
    registros: req.body.registros.map((registro) => ({
      alumno_id: Number(registro.alumno_id),
      estado: registro.estado,
      es_contraturno: registro.es_contraturno
    })),
    es_contraturno: req.body.es_contraturno,
    creado_por: req.user?.id ? Number(req.user.id) : null
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: attendance });
});

export const updateAttendance = catchAsync(async (req, res) => {
  const attendance = await attendanceService.updateAttendance(
    {
      alumno_id: Number(req.params.studentId),
      curso_id: Number(req.params.courseId),
      fecha: req.body.fecha,
      es_contraturno: req.body.es_contraturno ?? 0
    },
    { estado: req.body.estado }
  );
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: attendance });
});

export const getCourseSubjectsForDate = catchAsync(async (req, res) => {
  const payload = await attendanceService.listCourseSubjectsForDate(
    Number(req.params.courseId),
    req.query.fecha
  );
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const listAttendance = catchAsync(async (req, res) => {
  const attendances = await attendanceService.listAttendance(Number(req.params.courseId), req.query.fecha);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: attendances });
});

export const listCourseGeneralAttendance = catchAsync(async (req, res) => {
  const payload = await attendanceService.listCourseGeneralAttendance(Number(req.params.courseId), {
    fechas: req.query.fechas,
    desde: req.query.desde,
    hasta: req.query.hasta
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const listStudentAttendance = catchAsync(async (req, res) => {
  const studentId = Number(req.params.studentId);
  ensureStudentAccess(req, studentId);

  const attendances = await attendanceService.listByStudent(studentId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: attendances });
});

export const getAllowedSubjectDays = catchAsync(async (req, res) => {
  const payload = await attendanceService.getAllowedDays({
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : null,
    courseId: Number(req.params.courseId),
    subjectId: Number(req.params.subjectId),
    baseDate: req.query.fecha
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const listWeeklySubjectAttendance = catchAsync(async (req, res) => {
  const payload = await attendanceService.getWeeklySubjectMatrix({
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : null,
    courseId: Number(req.params.courseId),
    subjectId: Number(req.params.subjectId),
    baseDate: req.query.fecha
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const saveSubjectAttendanceMatrix = catchAsync(async (req, res) => {
  const payload = await attendanceService.saveSubjectAttendanceMatrix({
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : null,
    courseId: Number(req.params.courseId),
    subjectId: Number(req.params.subjectId),
    encabezados: req.body.encabezados,
    asistencias: req.body.asistencias
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const importProfessorAttendance = catchAsync(async (req, res) => {
  const payload = await attendanceService.importProfessorAttendance({
    preceptorId: req.user?.role === 'preceptor' ? Number(req.user.id) : null,
    courseId: Number(req.params.courseId),
    subjectIds: req.body.materia_ids,
    date: req.body.fecha,
    dryRun: Boolean(req.body.dry_run)
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const importSubjectAttendance = catchAsync(async (req, res) => {
  const payload = await attendanceService.importSubjectAttendanceFromGeneral({
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : null,
    courseId: Number(req.params.courseId),
    subjectId: Number(req.params.subjectId),
    date: req.body.fecha
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const getCourseAttendanceSummary = catchAsync(async (req, res) => {
  const payload = await attendanceService.getCourseSummary(Number(req.params.courseId), req.query.fecha);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});

export const getCourseAttendanceRange = catchAsync(async (req, res) => {
  const payload = await attendanceService.getCourseAttendanceRange(Number(req.params.courseId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});
