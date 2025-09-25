import httpStatus from 'http-status';
import { pendingSubjectService } from '../services/pendingSubjectService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listPendingSubjects = catchAsync(async (req, res) => {
  const studentId = Number(req.params.studentId);
  const pendingSubjects = await pendingSubjectService.listPendingByStudent(studentId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: pendingSubjects });
});

export const listPendingSubjectHistory = catchAsync(async (req, res) => {
  const studentId = Number(req.params.studentId);
  const history = await pendingSubjectService.listHistoryByStudent(studentId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: history });
});

export const searchPendingSubjects = catchAsync(async (req, res) => {
  const subjects = await pendingSubjectService.searchSubjects(
    req.query.q ?? req.query.search ?? '',
    req.query.limit
  );
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: subjects });
});

export const createPendingSubject = catchAsync(async (req, res) => {
  const pendingSubject = await pendingSubjectService.createPendingSubject({
    alumnoId: req.body.alumno_id,
    materiaId: req.body.materia_id
  });

  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: pendingSubject });
});

export const approvePendingSubject = catchAsync(async (req, res) => {
  const historyEntry = await pendingSubjectService.approvePendingSubject(Number(req.params.pendingId), {
    nota: req.body.nota,
    profesorId: req.body.profesor_id
  });

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: historyEntry });
});

export const listPendingSubjectTeachers = catchAsync(async (req, res) => {
  const teachers = await pendingSubjectService.listTeachersByPending(Number(req.params.pendingId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: teachers });
});
