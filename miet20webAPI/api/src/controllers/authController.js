import httpStatus from 'http-status';
import { authService } from '../services/authService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const login = catchAsync(async (req, res) => {
  const result = await authService.login(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const loginWithGoogle = catchAsync(async (req, res) => {
  const result = await authService.loginWithGoogle(req.body.idToken, req.body.email);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const refreshToken = catchAsync(async (req, res) => {
  const result = await authService.refreshToken(req.body.refreshToken);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const switchRole = catchAsync(async (req, res) => {
  const result = await authService.switchRole(
    Number(req.user.id),
    Number(req.body.roleId),
    req.body.refreshToken
  );
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const logout = catchAsync(async (req, res) => {
  await authService.logout(Number(req.user.id), {
    refreshToken: req.body.refreshToken,
    allDevices: Boolean(req.body.allDevices),
    accessToken: req.auth?.accessToken || null
  });

  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: {
      message: 'Sesión finalizada correctamente'
    }
  });
});