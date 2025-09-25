import httpStatus from 'http-status';
import { subjectService } from '../services/subjectService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const createSubject = catchAsync(async (req, res) => {
  const subject = await subjectService.createSubject(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: subject });
});

export const listSubjects = catchAsync(async (req, res) => {
  const subjects = await subjectService.listSubjects(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: subjects });
});

export const listSubjectCategories = catchAsync(async (req, res) => {
  const categories = await subjectService.listCategories();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: categories });
});

export const getSubject = catchAsync(async (req, res) => {
  const subject = await subjectService.getSubject(Number(req.params.id));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: subject });
});

export const updateSubject = catchAsync(async (req, res) => {
  const subject = await subjectService.updateSubject(Number(req.params.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: subject });
});

export const deleteSubject = catchAsync(async (req, res) => {
  await subjectService.deleteSubject(Number(req.params.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Materia eliminada correctamente' }
  });
});

export const toggleSubjectState = catchAsync(async (req, res) => {
  const subject = await subjectService.toggleSubjectState(Number(req.params.id), req.body.estado);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: subject });
});
