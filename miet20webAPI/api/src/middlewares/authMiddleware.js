import httpStatus from 'http-status';
import jwt from 'jsonwebtoken';
import { env } from '../config/env.js';
import { ApiError } from '../utils/ApiError.js';
import { tokenRepository } from '../repositories/tokenRepository.js';
import { hashToken } from '../utils/tokenUtils.js';

const extractAccessToken = (header = '') => {
  if (!header.startsWith('Bearer ')) {
    return null;
  }
  return header.slice(7).trim();
};

export const ensureAuthenticated = async (req, res, next) => {
  const rawToken = extractAccessToken(req.headers.authorization || '');

  if (!rawToken) {
    return next(new ApiError(httpStatus.UNAUTHORIZED, 'Autenticación requerida'));
  }

  try {
    const payload = jwt.verify(rawToken, env.jwt.secret);
    const tokenHash = hashToken(rawToken);
    const isRevoked = await tokenRepository.isAccessTokenBlacklisted(tokenHash);

    if (isRevoked) {
      return next(new ApiError(httpStatus.UNAUTHORIZED, 'La sesión ya no es válida'));
    }

    req.user = payload;
    req.auth = {
      accessToken: rawToken,
      tokenHash
    };

    return next();
  } catch (error) {
    return next(new ApiError(httpStatus.UNAUTHORIZED, 'Token inválido o expirado'));
  }
};

export const requireRole = (...allowedRoles) => (req, res, next) => {
  if (!req.user || (allowedRoles.length && !allowedRoles.includes(req.user.role))) {
    return next(new ApiError(httpStatus.FORBIDDEN, 'No tiene permisos para acceder a este recurso'));
  }
  return next();
};

export const authenticate = ensureAuthenticated;
export const authorizeRoles = requireRole;
