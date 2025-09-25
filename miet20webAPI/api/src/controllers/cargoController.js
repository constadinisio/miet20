import httpStatus from 'http-status';
import { cargoService } from '../services/cargoService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

const normalizeOptional = (value) =>
  value === undefined || value === null || value === '' ? null : value;

const parseNullableInt = (value) => {
  const normalized = normalizeOptional(value);
  if (normalized === null) {
    return null;
  }

  return Number(normalized);
};

const mapRequestToPayload = (body) => ({
  codigo: body.codigo_cargo,
  tipoCargoId: Number(body.tipo_cargo_id),
  tipo: body.tipo ?? body.situacion,
  estado: body.estado ?? 'activo',
  observaciones: normalizeOptional(body.observaciones),
  fechaInicio: normalizeOptional(body.fecha_inicio),
  fechaFin: normalizeOptional(body.fecha_fin),
  docenteId: parseNullableInt(body.docente_id)
});

export const createCargo = catchAsync(async (req, res) => {
  const payload = mapRequestToPayload(req.body);
  const cargo = await cargoService.createCargo(payload);

  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: cargo });
});

export const getCargo = catchAsync(async (req, res) => {
  const cargo = await cargoService.getCargo(Number(req.params.cargoId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: cargo });
});

export const updateCargo = catchAsync(async (req, res) => {
  const payload = mapRequestToPayload(req.body);
  const cargo = await cargoService.updateCargo(Number(req.params.cargoId), payload);

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: cargo });
});

export const deactivateCargo = catchAsync(async (req, res) => {
  await cargoService.deactivateCargo(Number(req.params.cargoId));
  res.status(httpStatus.NO_CONTENT).send();
});

export const reactivateCargo = catchAsync(async (req, res) => {
  const cargo = await cargoService.reactivateCargo(Number(req.params.cargoId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: cargo });
});

export const listCargos = catchAsync(async (req, res) => {
  const cargos = await cargoService.listCargos();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: cargos });
});

export const listCargoTypes = catchAsync(async (req, res) => {
  const types = await cargoService.listCargoTypes();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: types });
});

export const listCargoSchedules = catchAsync(async (req, res) => {
  const schedules = await cargoService.listSchedules(Number(req.params.cargoId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: schedules });
});

export const addCargoSchedules = catchAsync(async (req, res) => {
  const schedules = await cargoService.addSchedules(Number(req.params.cargoId), {
    dias: req.body.dias,
    horaInicio: req.body.hora_inicio,
    horaFin: req.body.hora_fin,
    tipo: req.body.tipo
  });

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: { horarios: schedules } });
});

export const replaceCargoSchedules = catchAsync(async (req, res) => {
  const schedules = await cargoService.replaceSchedules(Number(req.params.cargoId), req.body.horarios);

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: { horarios: schedules } });
});

export const deleteCargoSchedule = catchAsync(async (req, res) => {
  await cargoService.deleteSchedule(Number(req.params.cargoId), Number(req.params.horarioId));

  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Horario eliminado correctamente' }
  });
});

export const saveCargoRelations = catchAsync(async (req, res) => {
  const result = await cargoService.replaceRelations(Number(req.params.cargoId), req.body.materias);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});
