import httpStatus from 'http-status';
import { roleService } from '../services/roleService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listRoles = catchAsync(async (req, res) => {
  const roles = await roleService.listRoles();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: roles });
});
