import httpStatus from 'http-status';

const normalizeErrorItem = (error) => {
  if (!error) {
    return { message: 'Unknown error' };
  }

  if (error instanceof Error) {
    return { message: error.message };
  }

  if (typeof error === 'string') {
    return { message: error };
  }

  if (typeof error === 'object') {
    return error.message ? { ...error } : { ...error, message: 'Unknown error' };
  }

  return { message: String(error) };
};

const normalizeErrors = (errors) => {
  if (!errors) {
    return [];
  }

  const array = Array.isArray(errors) ? errors : [errors];
  return array.map(normalizeErrorItem).filter(Boolean);
};

const buildMeta = (meta) => {
  if (!meta) {
    return null;
  }

  const entries = Object.entries(meta).filter(([, value]) => value !== undefined && value !== null);
  if (!entries.length) {
    return null;
  }

  return Object.fromEntries(entries);
};

export class ApiResponse {
  static success(data = null, meta = null) {
    const response = {
      status: 'success',
      data,
      errors: null
    };

    const normalizedMeta = buildMeta(meta);
    if (normalizedMeta) {
      response.meta = normalizedMeta;
    }

    return response;
  }

  static error({ errors, data = null, meta = null }) {
    const normalizedErrors = normalizeErrors(errors);
    const response = {
      status: 'error',
      data,
      errors: normalizedErrors.length ? normalizedErrors : [{ message: 'Unknown error' }]
    };

    const normalizedMeta = buildMeta(meta);
    if (normalizedMeta) {
      response.meta = normalizedMeta;
    }

    return response;
  }
}

export const withRequestMeta = (req, meta) => {
  const combinedMeta = { ...(meta || {}) };
  if (req?.id) {
    combinedMeta.requestId = req.id;
  }

  return buildMeta(combinedMeta);
};

export const sendSuccess = ({ req, res, statusCode = httpStatus.OK, data = null, meta } = {}) => {
  const payload = ApiResponse.success(data, withRequestMeta(req, meta));
  return res.status(statusCode).json(payload);
};

export const sendError = ({
  req,
  res,
  statusCode = httpStatus.BAD_REQUEST,
  errors,
  data = null,
  meta
} = {}) => {
  const payload = ApiResponse.error({ errors, data, meta: withRequestMeta(req, meta) });
  return res.status(statusCode).json(payload);
};
