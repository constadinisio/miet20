import httpStatus from 'http-status';
import { ApiError } from '../utils/ApiError.js';
import { logger } from '../utils/logger.js';
import { sendError } from '../utils/ApiResponse.js';

export const errorConverter = (err, req, res, next) => {
  let error = err;
  if (!(error instanceof ApiError)) {
    const statusCode = error.statusCode || httpStatus.INTERNAL_SERVER_ERROR;
    const message = error.message || httpStatus[statusCode];
    error = new ApiError(statusCode, message, false, err.stack);
  }
  next(error);
};

export const errorHandler = (err, req, res, next) => { // eslint-disable-line no-unused-vars
  const { statusCode = httpStatus.INTERNAL_SERVER_ERROR, message, details, code } = err;

  if (!err.isOperational) {
    logger.error(err);
  }

  const errorPayload = {
    code: code || statusCode,
    message,
    ...(details ? { details } : {})
  };

  sendError({
    req,
    res,
    statusCode,
    errors: errorPayload
  });
};
