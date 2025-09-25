import httpStatus from 'http-status';
import bcrypt from 'bcryptjs';
import { roleRepository } from '../repositories/roleRepository.js';
import { userRepository } from '../repositories/userRepository.js';
import { userRoleRepository } from '../repositories/userRoleRepository.js';
import { studentRepository } from '../repositories/studentRepository.js';
import { ApiError } from '../utils/ApiError.js';
import {
  buildRequiredFieldsResponse,
  resolveMissingRequiredFields,
  STUDENT_ROLE_ID
} from '../config/requiredFields.js';

const SALT_ROUNDS = 10;

const ROLE_NAME_TO_ID = {
  admin: 1,
  preceptor: 2,
  profesor: 3,
  alumno: 4,
  spei: 5
};

const sanitizeUser = (user) => {
  if (!user) {
    return user;
  }
  const { contrasena: _password, ...rest } = user;
  return rest;
};

const normalizeProfileValue = (field, value) => {
  if (value === undefined) {
    return undefined;
  }

  if (value === null) {
    return null;
  }

  if (typeof value === 'string') {
    const trimmed = value.trim();
    return trimmed === '' ? null : trimmed;
  }

  if (field === 'fecha_nacimiento') {
    return value ? new Date(value).toISOString().slice(0, 10) : null;
  }

  return value;
};

const toNumericId = (value) => {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
};

const toArrayParam = (value) => {
  if (value === null || value === undefined) {
    return [];
  }

  if (Array.isArray(value)) {
    return value;
  }

  if (typeof value === 'string') {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item !== '');
  }

  return [value];
};

const normalizeRoleValue = (value) => {
  if (value === null || value === undefined || value === '') {
    return null;
  }

  if (typeof value === 'string' && ROLE_NAME_TO_ID[value] !== undefined) {
    return ROLE_NAME_TO_ID[value];
  }

  const numeric = toNumericId(value);
  return numeric === null ? null : numeric;
};

const parseRoleList = (value) => {
  const normalized = new Set();
  toArrayParam(value).forEach((item) => {
    const normalizedRole = normalizeRoleValue(item);
    if (normalizedRole !== null) {
      normalized.add(Number(normalizedRole));
    }
  });
  return Array.from(normalized);
};

const parseIntegerList = (value) => {
  const normalized = new Set();
  toArrayParam(value).forEach((item) => {
    const parsed = toNumericId(item);
    if (parsed !== null) {
      normalized.add(Number(parsed));
    }
  });
  return Array.from(normalized);
};

const hydrateUser = async (user) => {
  const sanitized = sanitizeUser(user);
  if (!sanitized) {
    return sanitized;
  }

  const additionalRoles = (await roleRepository.findForUser(user.id)) ?? [];
  sanitized.rolesAdicionales = additionalRoles.map((role) => toNumericId(role.id)).filter((id) => id !== null);
  sanitized.rolesAdicionalesDetalle = additionalRoles.map((role) => ({
    id: toNumericId(role.id),
    nombre: role.nombre
  }));

  return sanitized;
};

const buildEmptyFamily = (userId) => ({
  usuario_id: Number(userId) || null,
  padre_nombre: null,
  padre_tel: null,
  padre_mail: null,
  madre_nombre: null,
  madre_tel: null,
  madre_mail: null,
  emergencia_nombre: null,
  emergencia_tel: null
});

export const userService = {
  async createUser(payload) {
    const existing = await userRepository.findByEmail(payload.mail);
    if (existing) {
      throw new ApiError(httpStatus.CONFLICT, 'El correo electrónico ya está registrado');
    }
    const hash = await bcrypt.hash(payload.contrasena, SALT_ROUNDS);
    const userToCreate = {
      nombre: payload.nombre,
      apellido: payload.apellido,
      mail: payload.mail,
      contrasena: hash,
      rol: ROLE_NAME_TO_ID[payload.rol] ?? 0,
      status: payload.status ?? 1
    };
    const created = await userRepository.create(userToCreate);
    return hydrateUser(created);
  },

  async listUsers(filters = {}) {
    const where = {};
    const queryOptions = {};

    const statusFilter = toNumericId(filters.status);
    if (statusFilter !== null) {
      where.status = statusFilter;
    }

    const statusInFilter = parseIntegerList(filters.statusIn ?? filters.status_in ?? filters.statusIds);
    if (statusInFilter.length > 0) {
      queryOptions.statusIn = statusInFilter;
    }

    const roleFilter = normalizeRoleValue(filters.rol ?? filters.role ?? filters.roleId);
    if (roleFilter !== null) {
      where.rol = roleFilter;
    }

    const roleInFilter = parseRoleList(filters.roleIn ?? filters.roles ?? filters.role_ids ?? filters.roleIds);
    if (roleInFilter.length > 0) {
      queryOptions.roleIn = roleInFilter;
    }

    const excludeRoleFilter = parseRoleList(
      filters.excludeRole ?? filters.excludeRoles ?? filters.exclude_role ?? filters.excludeRoleIds
    );
    if (excludeRoleFilter.length > 0) {
      queryOptions.excludeRole = excludeRoleFilter;
    }

    const searchTerm = typeof filters.search === 'string' ? filters.search.trim() : '';
    if (searchTerm) {
      queryOptions.search = searchTerm;
    }

    const limit = toNumericId(filters.limit);
    if (limit !== null && limit > 0) {
      queryOptions.limit = limit;
    }

    const offset = toNumericId(filters.offset);
    if (offset !== null && offset >= 0) {
      queryOptions.offset = offset;
    }

    return userRepository
      .list({ where, ...queryOptions })
      .then((users) => users.map(sanitizeUser));
  },

  async getUser(id) {
    const user = await userRepository.findById(id);
    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }
    return hydrateUser(user);
  },

  async updateUser(id, updates) {
    const data = { ...updates };
    let additionalRoles = null;
    if (updates.contrasena) {
      data.contrasena = await bcrypt.hash(updates.contrasena, SALT_ROUNDS);
    }
    if (updates.rol) {
      data.rol = ROLE_NAME_TO_ID[updates.rol] ?? updates.rol;
    }
    if (Object.prototype.hasOwnProperty.call(data, 'status')) {
      const normalizedStatus = toNumericId(data.status);
      if (normalizedStatus !== null) {
        data.status = normalizedStatus;
      }
    }
    if (Object.prototype.hasOwnProperty.call(data, 'permNoticia')) {
      data.permNoticia = data.permNoticia ? 1 : 0;
    }
    if (Object.prototype.hasOwnProperty.call(data, 'permSubidaArch')) {
      data.permSubidaArch = data.permSubidaArch ? 1 : 0;
    }
    if (Object.prototype.hasOwnProperty.call(data, 'rolesAdicionales')) {
      additionalRoles = Array.isArray(data.rolesAdicionales)
        ? data.rolesAdicionales.map(toNumericId).filter((id) => id !== null)
        : [];
      delete data.rolesAdicionales;
    }
    const updated = await userRepository.update(id, data);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }
    if (additionalRoles !== null) {
      const principalRoleId = toNumericId(updated.rol);
      const filteredRoles = additionalRoles.filter((roleId) => roleId !== principalRoleId);
      await userRoleRepository.replaceUserRoles(id, filteredRoles);
    }

    return hydrateUser(updated);
  },

  async deleteUser(id) {
    const deleted = await userRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }
    return true;
  },

  async updateProfile(id, updates) {
    const allowedFields = [
      'nombre',
      'apellido',
      'mail',
      'telefono',
      'direccion',
      'foto_url',
      'dni',
      'fecha_nacimiento',
      'ficha_censal'
    ];
    const data = {};

    allowedFields.forEach((field) => {
      if (Object.prototype.hasOwnProperty.call(updates, field)) {
        const normalized = normalizeProfileValue(field, updates[field]);
        if (normalized !== undefined) {
          data[field] = normalized;
        }
      }
    });

    if (Object.prototype.hasOwnProperty.call(data, 'mail') && data.mail) {
      const existing = await userRepository.findByEmail(data.mail);
      if (existing && Number(existing.id) !== Number(id)) {
        throw new ApiError(httpStatus.CONFLICT, 'El correo electrónico ya está registrado');
      }
    }

    if (!Object.keys(data).length) {
      const existing = await userRepository.findById(id);
      if (!existing) {
        throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
      }
      return hydrateUser(existing);
    }

    const updated = await userRepository.update(id, data);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    return hydrateUser(updated);
  },

  async updatePassword(id, { currentPassword, newPassword }) {
    const user = await userRepository.findById(id);
    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    const matches = await bcrypt.compare(currentPassword, user.contrasena || '');
    if (!matches) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La contraseña actual es incorrecta');
    }

    const hash = await bcrypt.hash(newPassword, SALT_ROUNDS);
    const updated = await userRepository.update(id, { contrasena: hash });
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    return hydrateUser(updated);
  },

  async approveUser(id, { rol, rolesAdicionales } = {}) {
    const updates = { status: 1 };

    if (rol !== undefined) {
      updates.rol = rol;
    }

    if (rolesAdicionales !== undefined) {
      updates.rolesAdicionales = rolesAdicionales;
    }

    return this.updateUser(id, updates);
  },

  async rejectUser(id) {
    const deleted = await userRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }
    return true;
  },

  async getFamily(id) {
    const user = await userRepository.findById(id);
    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    const family = await studentRepository.findFamilyByStudentId(id);
    if (!family) {
      return buildEmptyFamily(id);
    }

    return family;
  },

  async updateFamily(id, payload) {
    const user = await userRepository.findById(id);
    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    const sanitized = {
      padre_nombre: payload.padre_nombre ?? null,
      padre_tel: payload.padre_tel ?? null,
      padre_mail: payload.padre_mail ?? null,
      madre_nombre: payload.madre_nombre ?? null,
      madre_tel: payload.madre_tel ?? null,
      madre_mail: payload.madre_mail ?? null,
      emergencia_nombre: payload.emergencia_nombre ?? null,
      emergencia_tel: payload.emergencia_tel ?? null
    };

    return studentRepository.upsertFamily(id, sanitized);
  },

  async getRequiredProfileFields({ roleId } = {}) {
    const normalizedRole = toNumericId(roleId);
    return buildRequiredFieldsResponse(normalizedRole);
  },

  async getUserMissingProfileFields(id, { roleId } = {}) {
    const user = await userRepository.findById(id);
    if (!user) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
    }

    let resolvedRole = toNumericId(roleId);
    if (resolvedRole === null) {
      resolvedRole = toNumericId(user.rol);
    }

    if (resolvedRole === null) {
      const additionalRoles = await roleRepository.findForUser(id);
      const firstRole = additionalRoles?.find((role) => toNumericId(role.id) !== null);
      resolvedRole = firstRole ? toNumericId(firstRole.id) : null;
    }

    if (resolvedRole === null) {
      resolvedRole = STUDENT_ROLE_ID;
    }

    return resolveMissingRequiredFields(user, resolvedRole);
  }
};
