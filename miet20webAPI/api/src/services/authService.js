import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import httpStatus from 'http-status';
import { randomUUID } from 'crypto';
import { OAuth2Client } from 'google-auth-library';
import { userRepository } from '../repositories/userRepository.js';
import { roleRepository } from '../repositories/roleRepository.js';
import { tokenRepository } from '../repositories/tokenRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { env } from '../config/env.js';
import { hashToken } from '../utils/tokenUtils.js';

const ROLE_ID_TO_NAME = {
  1: 'admin',
  2: 'preceptor',
  3: 'profesor',
  4: 'alumno',
  5: 'spei'
};

const googleClient = env.google.clientId ? new OAuth2Client(env.google.clientId) : null;

const resolveRoleName = (user, rolesInfo) => {
  const { roles, primaryRole } = rolesInfo;
  if (primaryRole) {
    return ROLE_ID_TO_NAME[primaryRole.id] ?? primaryRole.nombre;
  }

  if (roles && roles.length > 0) {
    return ROLE_ID_TO_NAME[roles[0].id] ?? roles[0].nombre;
  }

  return ROLE_ID_TO_NAME[user.rol] ?? 'alumno';
};

const normalizeUser = (user, rolesInfo) => {
  if (!user) {
    return user;
  }

  const { contrasena: _password, ...rest } = user;
  const roles = rolesInfo.roles ?? [];
  const primaryRole = rolesInfo.primaryRole ?? null;

  return {
    ...rest,
    email: user.mail,
    role: resolveRoleName(user, rolesInfo),
    primaryRole,
    roles,
    permissions: {
      noticias: Boolean(user.permNoticia),
      galeria: Boolean(user.permSubidaArch),
      attp: roles.some((role) => role.id === 5)
    }
  };
};

const buildUserRoles = async (user) => {
  if (!user) {
    return { roles: [], primaryRole: null };
  }

  const roles = [];
  if (user.rol) {
    const mainRole = await roleRepository.findById(user.rol);
    if (mainRole) {
      roles.push({ id: Number(mainRole.id), nombre: mainRole.nombre });
    }
  }

  const additionalRoles = await roleRepository.findForUser(user.id);
  additionalRoles.forEach((role) => {
    const roleId = Number(role.id);
    if (!roles.some((storedRole) => storedRole.id === roleId)) {
      roles.push({ id: roleId, nombre: role.nombre });
    }
  });

  return {
    roles,
    primaryRole: roles.length > 0 ? roles[0] : null
  };
};

const generateToken = (payload, secret, expiresIn) => jwt.sign(payload, secret, { expiresIn });

const decodeExpiration = (token) => {
  const decoded = jwt.decode(token);
  if (!decoded?.exp) {
    return null;
  }
  return new Date(decoded.exp * 1000);
};

const ensureUserIsActive = (user) => {
  if (user.status !== 1 && user.status !== 'activo' && user.status !== 'active') {
    throw new ApiError(httpStatus.FORBIDDEN, 'La cuenta no está activa');
  }
};

const issueSessionTokens = async (user, rolesInfo, { roleOverride, previousRefreshTokenHash } = {}) => {
  const sessionId = randomUUID();
  const resolvedRole = roleOverride ?? resolveRoleName(user, rolesInfo);
  const payload = { id: user.id, role: resolvedRole, email: user.mail, sessionId };

  const accessToken = generateToken(payload, env.jwt.secret, env.jwt.expiresIn);
  const refreshPayload = { ...payload, type: 'refresh' };
  const refreshToken = generateToken(refreshPayload, env.jwt.refreshSecret, env.jwt.refreshExpiresIn);

  const refreshTokenHash = hashToken(refreshToken);
  const expiresAt = decodeExpiration(refreshToken);

  await tokenRepository.storeRefreshToken({
    userId: user.id,
    tokenHash: refreshTokenHash,
    sessionId,
    expiresAt,
    metadata: { role: resolvedRole }
  });

  if (previousRefreshTokenHash) {
    await tokenRepository.revokeRefreshToken(previousRefreshTokenHash, user.id, 'rotated');
  }

  return {
    user: normalizeUser(user, rolesInfo),
    tokens: {
      accessToken,
      refreshToken,
      expiresIn: env.jwt.expiresIn
    }
  };
};

const verifyGoogleIdToken = async (idToken) => {
  if (!idToken) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El token de Google es obligatorio');
  }

  if (!googleClient) {
    throw new ApiError(httpStatus.SERVICE_UNAVAILABLE, 'El login con Google no está configurado');
  }

  try {
    const ticket = await googleClient.verifyIdToken({
      idToken,
      audience:
        env.google.audience && env.google.audience.length > 0
          ? env.google.audience
          : env.google.clientId
            ? [env.google.clientId]
            : undefined
    });

    return ticket.getPayload();
  } catch (error) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'No se pudo validar el token de Google');
  }
};

const assertRefreshTokenActive = (record) => {
  if (!record) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'Refresh token inválido');
  }

  if (record.revokedAt) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'El refresh token ya no es válido');
  }

  if (record.expiresAt && record.expiresAt < new Date()) {
    throw new ApiError(httpStatus.UNAUTHORIZED, 'El refresh token expiró');
  }
};

export const authService = {
  async login({ email, password }) {
    const normalizedEmail = email.trim().toLowerCase();
    const user = await userRepository.findByEmail(normalizedEmail);

    if (!user) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'Credenciales inválidas');
    }

    const storedPassword = user.contrasena ?? '';
    let isPasswordValid = false;
    let authenticatedUser = user;

    if (storedPassword) {
      isPasswordValid = await bcrypt.compare(password, storedPassword);
    }

    if (!isPasswordValid && password === storedPassword) {
      const newHash = await bcrypt.hash(password, 10);
      await userRepository.update(user.id, { contrasena: newHash });
      authenticatedUser = (await userRepository.findById(user.id)) ?? {
        ...user,
        contrasena: newHash
      };
      isPasswordValid = true;
    }

    if (!isPasswordValid) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'Credenciales inválidas');
    }

    ensureUserIsActive(authenticatedUser);

    const rolesInfo = await buildUserRoles(authenticatedUser);
    if (!rolesInfo.roles || rolesInfo.roles.length === 0) {
      throw new ApiError(httpStatus.FORBIDDEN, 'La cuenta no tiene roles asignados');
    }

    return issueSessionTokens(authenticatedUser, rolesInfo);
  },

  async loginWithGoogle(idToken, fallbackEmail) {
    const payload = await verifyGoogleIdToken(idToken);
    const email = payload?.email && payload.email_verified ? payload.email : fallbackEmail;

    if (!email) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'No se pudo determinar el correo electrónico del usuario');
    }

    if (env.google.hostedDomain && payload?.hd && payload.hd !== env.google.hostedDomain) {
      throw new ApiError(httpStatus.FORBIDDEN, 'El dominio del correo no está autorizado');
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await userRepository.findByEmail(normalizedEmail);

    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'No existe una cuenta asociada al correo proporcionado');
    }

    if (Number(user.rol) === 0 || user.status === 0 || user.status === 'pendiente') {
      throw new ApiError(httpStatus.FORBIDDEN, 'El registro aún está pendiente de aprobación');
    }

    ensureUserIsActive(user);

    const rolesInfo = await buildUserRoles(user);
    if (!rolesInfo.roles || rolesInfo.roles.length === 0) {
      throw new ApiError(httpStatus.FORBIDDEN, 'La cuenta no tiene roles asignados');
    }

    return issueSessionTokens(user, rolesInfo);
  },

  async refreshToken(token) {
    const refreshTokenHash = hashToken(token);
    const stored = await tokenRepository.findRefreshToken(refreshTokenHash);

    try {
      assertRefreshTokenActive(stored);
      const decoded = jwt.verify(token, env.jwt.refreshSecret);
      const user = await userRepository.findById(decoded.id);

      if (!user) {
        await tokenRepository.revokeRefreshToken(stored?.tokenHash ?? refreshTokenHash, stored?.userId, 'user_missing');
        throw new ApiError(httpStatus.UNAUTHORIZED, 'La sesión no es válida');
      }

      ensureUserIsActive(user);

      const rolesInfo = await buildUserRoles(user);
      return issueSessionTokens(user, rolesInfo, {
        roleOverride: decoded.role,
        previousRefreshTokenHash: stored.tokenHash ?? refreshTokenHash
      });
    } catch (error) {
      if (stored) {
        await tokenRepository.revokeRefreshToken(stored.tokenHash ?? refreshTokenHash, stored.userId, 'invalid');
      }
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError(httpStatus.UNAUTHORIZED, 'Refresh token inválido');
    }
  },

  async switchRole(userId, roleId, refreshToken) {
    if (!roleId || Number.isNaN(Number(roleId))) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El identificador de rol es obligatorio');
    }

    const user = await userRepository.findById(userId);
    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    ensureUserIsActive(user);

    const rolesInfo = await buildUserRoles(user);
    if (!rolesInfo.roles || rolesInfo.roles.length === 0) {
      throw new ApiError(httpStatus.FORBIDDEN, 'El usuario no tiene roles asignados');
    }

    const numericRoleId = Number(roleId);
    const desiredRole = rolesInfo.roles.find((role) => role.id === numericRoleId);

    if (!desiredRole) {
      throw new ApiError(httpStatus.FORBIDDEN, 'El rol seleccionado no está asignado al usuario');
    }

    let previousRefreshTokenHash = null;
    if (refreshToken) {
      const providedHash = hashToken(refreshToken);
      const stored = await tokenRepository.findRefreshToken(providedHash);
      assertRefreshTokenActive(stored);
      if (stored.userId !== userId) {
        throw new ApiError(httpStatus.UNAUTHORIZED, 'El refresh token no pertenece al usuario');
      }
      previousRefreshTokenHash = providedHash;
    }

    const orderedRoles = [desiredRole, ...rolesInfo.roles.filter((role) => role.id !== numericRoleId)];
    const updatedRolesInfo = { roles: orderedRoles, primaryRole: desiredRole };
    const tokenRole = ROLE_ID_TO_NAME[desiredRole.id] ?? desiredRole.nombre;

    return issueSessionTokens(user, updatedRolesInfo, {
      roleOverride: tokenRole,
      previousRefreshTokenHash
    });
  },

  async logout(userId, { refreshToken, allDevices = false, accessToken = null }) {
    if (!refreshToken) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El refresh token es obligatorio');
    }

    const refreshTokenHash = hashToken(refreshToken);
    const stored = await tokenRepository.findRefreshToken(refreshTokenHash);
    assertRefreshTokenActive(stored);

    if (stored.userId !== userId) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'El refresh token no pertenece al usuario');
    }

    await tokenRepository.revokeRefreshToken(refreshTokenHash, userId, 'logout');

    if (allDevices) {
      await tokenRepository.revokeUserRefreshTokens(userId, 'global_logout');
    }

    if (accessToken) {
      const accessHash = hashToken(accessToken);
      const decoded = jwt.decode(accessToken);
      const expiresAt = decoded?.exp ? new Date(decoded.exp * 1000) : new Date(Date.now() + 15 * 60 * 1000);
      await tokenRepository.addAccessTokenToBlacklist({
        tokenHash: accessHash,
        userId,
        expiresAt
      });
    }

    return { success: true };
  }
};
