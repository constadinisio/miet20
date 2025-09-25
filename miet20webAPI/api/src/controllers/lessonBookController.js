import httpStatus from 'http-status';
import { lessonBookService } from '../services/lessonBookService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listLessonBookEntries = catchAsync(async (req, res) => {
  const entries = await lessonBookService.listEntries({
    profesorId: req.user?.id,
    cursoId: Number(req.query.curso_id),
    materiaId: Number(req.query.materia_id)
  });

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: entries });
});

export const getLessonBookEntry = catchAsync(async (req, res) => {
  const entry = await lessonBookService.getEntry(Number(req.params.entryId), req.user?.id);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: entry });
});

export const createLessonBookEntry = catchAsync(async (req, res) => {
  const created = await lessonBookService.createEntry({
    profesorId: req.user?.id,
    cursoId: Number(req.body.curso_id),
    materiaId: Number(req.body.materia_id),
    fechaClase: req.body.fecha_clase,
    caracterClase: req.body.caracter_clase,
    tema: req.body.tema,
    actividades: req.body.actividades ?? req.body.actividades_desarrolladas,
    observaciones: req.body.observaciones
  });

  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: created });
});

export const updateLessonBookEntry = catchAsync(async (req, res) => {
  const updated = await lessonBookService.updateEntry(Number(req.params.entryId), req.user?.id, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: updated });
});

export const deleteLessonBookEntry = catchAsync(async (req, res) => {
  await lessonBookService.deleteEntry(Number(req.params.entryId), req.user?.id);
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Tema eliminado correctamente' }
  });
});
