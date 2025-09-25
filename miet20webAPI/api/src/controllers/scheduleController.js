import httpStatus from 'http-status';
import { scheduleService } from '../services/scheduleService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const createSchedule = catchAsync(async (req, res) => {
  const schedule = await scheduleService.createSchedule({
    curso_id: Number(req.params.courseId),
    profesor_id: req.body.profesor_id ? Number(req.body.profesor_id) : null,
    materia_id: req.body.materia_id ? Number(req.body.materia_id) : null,
    dia_semana: req.body.dia_semana,
    hora_inicio: req.body.hora_inicio,
    hora_fin: req.body.hora_fin,
    aula: req.body.aula,
    es_contraturno: req.body.es_contraturno ?? 0
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: schedule });
});

export const createScheduleForAssignment = catchAsync(async (req, res) => {
  const schedule = await scheduleService.createScheduleForAssignment(
    Number(req.params.assignmentId),
    req.body
  );
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: schedule });
});

export const listSchedules = catchAsync(async (req, res) => {
  const schedules = await scheduleService.listByCourse(Number(req.params.courseId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: schedules });
});

export const listSchedulesByAssignment = catchAsync(async (req, res) => {
  const schedules = await scheduleService.listByAssignment(Number(req.params.assignmentId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: schedules });
});

export const listTeacherAssignments = catchAsync(async (req, res) => {
  const assignments = await scheduleService.listTeacherAssignments(Number(req.params.teacherId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: assignments });
});

export const updateSchedule = catchAsync(async (req, res) => {
  const schedule = await scheduleService.updateSchedule(Number(req.params.scheduleId), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: schedule });
});

export const deleteSchedule = catchAsync(async (req, res) => {
  await scheduleService.deleteSchedule(Number(req.params.scheduleId));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Horario eliminado correctamente' }
  });
});
