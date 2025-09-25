import httpStatus from 'http-status';

export class ApiError extends Error {
  constructor(
    statusCode = httpStatus.INTERNAL_SERVER_ERROR,
    message = 'Error interno del servidor',
    isOperational = true,
    stack = '',
    details = null
  ) {
    super(message);
    this.statusCode = statusCode;
    this.isOperational = isOperational;
    this.details = details;
    if (stack) {
      this.stack = stack;
    } else {
      Error.captureStackTrace(this, this.constructor);
    }
  }
}
