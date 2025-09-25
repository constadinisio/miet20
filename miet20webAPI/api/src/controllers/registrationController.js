import httpStatus from 'http-status';
import { registrationService } from '../services/registrationService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const submitRegistration = catchAsync(async (req, res) => {
  const registration = await registrationService.submitRegistration(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: registration });
});

export const listRegistrations = catchAsync(async (req, res) => {
  const registrations = await registrationService.listRegistrations(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: registrations });
});

export const getRegistration = catchAsync(async (req, res) => {
  const registrationId = Number(req.params.registrationId);
  const registration = await registrationService.getRegistration(registrationId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: registration });
});

export const reviewRegistration = catchAsync(async (req, res) => {
  const registrationId = Number(req.params.registrationId);
  const decision = await registrationService.reviewRegistration(registrationId, Number(req.user.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: decision });
});
