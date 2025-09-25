import httpStatus from 'http-status';
import { userService } from '../services/userService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';
import { ApiError } from '../utils/ApiError.js';

const ensureSelfOrAdmin = (req, userId) => {
  if (!req.user) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'Autenticación requerida');
  }

  if (req.user.role !== 'admin' && Number(req.user.id) !== Number(userId)) {
    throw new ApiError(httpStatus.FORBIDDEN, 'No tenés permisos para modificar otro usuario');
  }
};

export const createUser = catchAsync(async (req, res) => {
  const user = await userService.createUser(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: user });
});

export const listUsers = catchAsync(async (req, res) => {
  const users = await userService.listUsers(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: users });
});

export const getUser = catchAsync(async (req, res) => {
  const userId = Number(req.params.id);
  ensureSelfOrAdmin(req, userId);

  const user = await userService.getUser(userId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: user });
});

export const updateUser = catchAsync(async (req, res) => {
  const user = await userService.updateUser(Number(req.params.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: user });
});

export const deleteUser = catchAsync(async (req, res) => {
  await userService.deleteUser(Number(req.params.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Usuario eliminado correctamente' }
  });
});

export const updateUserProfile = catchAsync(async (req, res) => {
  const userId = Number(req.params.id);
  ensureSelfOrAdmin(req, userId);

  const user = await userService.updateProfile(userId, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: user });
});

export const updateUserPassword = catchAsync(async (req, res) => {
  const userId = Number(req.params.id);
  ensureSelfOrAdmin(req, userId);

  const user = await userService.updatePassword(userId, {
    currentPassword: req.body.currentPassword,
    newPassword: req.body.newPassword
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: user });
});

export const approveUser = catchAsync(async (req, res) => {
  const user = await userService.approveUser(Number(req.params.id), req.body || {});
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: user });
});

export const rejectUser = catchAsync(async (req, res) => {
  await userService.rejectUser(Number(req.params.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Usuario rechazado y eliminado correctamente' }
  });
});

export const getUserFamily = catchAsync(async (req, res) => {
  const userId = Number(req.params.id);
  ensureSelfOrAdmin(req, userId);

  const family = await userService.getFamily(userId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: family });
});

export const updateUserFamily = catchAsync(async (req, res) => {
  const userId = Number(req.params.id);
  ensureSelfOrAdmin(req, userId);

  const family = await userService.updateFamily(userId, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: family });
});

export const getRequiredProfileFields = catchAsync(async (req, res) => {
  const roleId = req.query.roleId ? Number(req.query.roleId) : undefined;
  const fields = await userService.getRequiredProfileFields({ roleId });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: fields });
});

export const getUserMissingProfileFields = catchAsync(async (req, res) => {
  const userId = Number(req.params.id);
  ensureSelfOrAdmin(req, userId);

  const roleId = req.query.roleId ? Number(req.query.roleId) : undefined;
  const payload = await userService.getUserMissingProfileFields(userId, { roleId });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: payload });
});
