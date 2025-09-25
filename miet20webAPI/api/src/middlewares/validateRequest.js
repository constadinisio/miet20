import { validationResult } from 'express-validator';
import httpStatus from 'http-status';
import { ApiError } from '../utils/ApiError.js';

export const validateRequest = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    const formattedErrors = errors.array().map((error) => ({
      field: error.param,
      message: error.msg
    }));
    return next(new ApiError(httpStatus.UNPROCESSABLE_ENTITY, 'Error de validación', true, '', formattedErrors));
  }
  return next();
};
