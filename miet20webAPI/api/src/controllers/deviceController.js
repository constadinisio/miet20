import httpStatus from 'http-status';
import { deviceService } from '../services/deviceService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const registerDevice = catchAsync(async (req, res) => {
  const device = await deviceService.registerDevice(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: device });
});

export const listDevices = catchAsync(async (req, res) => {
  const result = await deviceService.listDevices(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const getStockSummary = catchAsync(async (req, res) => {
  const summary = await deviceService.getStockSummary();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: summary });
});

export const updateDevice = catchAsync(async (req, res) => {
  const device = await deviceService.updateDevice(Number(req.params.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: device });
});

export const deleteDevice = catchAsync(async (req, res) => {
  await deviceService.removeDevice(Number(req.params.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Dispositivo eliminado correctamente' }
  });
});

export const createLoan = catchAsync(async (req, res) => {
  const loan = await deviceService.createLoan({
    ...req.body,
    dispositivo_id: req.params.id ? Number(req.params.id) : undefined
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: loan });
});

export const closeLoan = catchAsync(async (req, res) => {
  const loan = await deviceService.closeLoan(Number(req.params.loanId), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: loan });
});

export const deleteLoan = catchAsync(async (req, res) => {
  await deviceService.deleteLoan(Number(req.params.loanId));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Préstamo eliminado correctamente' }
  });
});

export const listLoans = catchAsync(async (req, res) => {
  const loans = await deviceService.listLoans(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: loans });
});

export const addNote = catchAsync(async (req, res) => {
  const note = await deviceService.addNote({
    ...req.body,
    dispositivo_id: Number(req.params.id),
    creado_por: req.user?.id
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: note });
});

export const listNotes = catchAsync(async (req, res) => {
  const notes = await deviceService.listNotes(Number(req.params.id));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: notes });
});

export const addBoardNote = catchAsync(async (req, res) => {
  const note = await deviceService.addBoardNote({
    ...req.body
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: note });
});

export const listBoardNotes = catchAsync(async (req, res) => {
  const notes = await deviceService.listBoardNotes();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: notes });
});

export const deleteBoardNote = catchAsync(async (req, res) => {
  await deviceService.deleteBoardNote(Number(req.params.noteId));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Nota eliminada correctamente' }
  });
});
