import httpStatus from 'http-status';
import { sendError } from '../utils/ApiResponse.js';

export const notFoundHandler = (req, res) => {
  sendError({
    req,
    res,
    statusCode: httpStatus.NOT_FOUND,
    errors: {
      code: 'not_found',
      message: `No se encontró el recurso solicitado: ${req.originalUrl}`
    }
  });
};
