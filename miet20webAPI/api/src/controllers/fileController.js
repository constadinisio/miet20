import httpStatus from 'http-status';
import { fileService } from '../services/fileService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const uploadFile = catchAsync(async (req, res) => {
  const result = await fileService.uploadFile(req.user?.id ?? null, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: result });
});

export const getFile = catchAsync(async (req, res) => {
  const fileId = Number(req.params.fileId);
  const result = await fileService.getFile(fileId, { user: req.user });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const deleteFile = catchAsync(async (req, res) => {
  const fileId = Number(req.params.fileId);
  await fileService.deleteFile(fileId);
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Archivo eliminado correctamente' }
  });
});