import httpStatus from 'http-status';
import { configurationService } from '../services/configurationService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const getConfiguration = catchAsync(async (req, res) => {
  const configuration = await configurationService.getConfiguration();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: configuration });
});

export const updateConfiguration = catchAsync(async (req, res) => {
  const configuration = await configurationService.updateConfiguration(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: configuration });
});
