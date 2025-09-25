import bcrypt from 'bcryptjs';
import httpStatus from 'http-status';
import { registrationRepository } from '../repositories/registrationRepository.js';
import { roleRepository } from '../repositories/roleRepository.js';
import { ApiError } from '../utils/ApiError.js';

const SALT_ROUNDS = 10;

const determineState = (registration) => {
  if (!registration) {
    return 'unknown';
  }

  const roleId = Number(registration.rol);
  const status = Number(registration.status);

  if (roleId === 0) {
    return 'pending';
  }

  if (status === 1 || registration.status === 'activo' || registration.status === 'active') {
    return 'approved';
  }

  if (status === -1) {
    return 'rejected';
  }

  return 'inactive';
};

const sanitizeRegistration = (registration) => {
  if (!registration) {
    return null;
  }

  const { contrasena: _password, ...rest } = registration;
  return {
    ...rest,
    state: determineState(registration)
  };
};

const normalizeOptional = (value) => {
  if (value === undefined || value === null) {
    return null;
  }

  const trimmed = String(value).trim();
  return trimmed === '' ? null : trimmed;
};

const ensureRegistrationExists = async (id) => {
  const registration = await registrationRepository.findById(id);
  if (!registration) {
    throw new ApiError(httpStatus.NOT_FOUND, 'La solicitud de registro no existe');
  }
  return registration;
};

export const registrationService = {
  async submitRegistration(payload) {
    const email = payload.mail.trim().toLowerCase();
    const dni = payload.dni.trim();

    const existingByDni = await registrationRepository.findByDni(dni);
    const existingByEmail = await registrationRepository.findByEmail(email);

    if (existingByEmail && (!existingByDni || existingByDni.id !== existingByEmail.id) && Number(existingByEmail.rol) !== 0) {
      throw new ApiError(httpStatus.CONFLICT, 'Ya existe un usuario activo registrado con este correo electrónico');
    }

    if (existingByDni && Number(existingByDni.rol) !== 0 && Number(existingByDni.status) === 1) {
      throw new ApiError(httpStatus.CONFLICT, 'Ya existe un usuario activo asociado a este DNI');
    }

    const passwordHash = await bcrypt.hash(payload.contrasena, SALT_ROUNDS);

    const data = {
      mail: email,
      nombre: payload.nombre.trim(),
      apellido: payload.apellido.trim(),
      dni,
      telefono: normalizeOptional(payload.telefono),
      direccion: normalizeOptional(payload.direccion),
      fecha_nacimiento: normalizeOptional(payload.fecha_nacimiento),
      contrasena: passwordHash,
      rol: 0,
      status: 0
    };

    if (existingByDni || existingByEmail) {
      const target = existingByDni ?? existingByEmail;
      const updated = await registrationRepository.update(target.id, data);
      return sanitizeRegistration(updated ?? { ...target, ...data });
    }

    const created = await registrationRepository.create(data);
    return sanitizeRegistration(created);
  },

  async listRegistrations(filters = {}) {
    const query = {};

    const status = filters.status ?? 'pending';
    if (status === 'pending') {
      query.rol = 0;
    } else if (status === 'approved') {
      query.status = 1;
    } else if (status === 'rejected') {
      query.status = -1;
    }

    if (filters.mail) {
      query.mail = filters.mail;
    }

    if (filters.dni) {
      query.dni = filters.dni;
    }

    const records = await registrationRepository.list(query);
    return records.map(sanitizeRegistration);
  },

  async getRegistration(id) {
    const registration = await ensureRegistrationExists(id);
    return sanitizeRegistration(registration);
  },

  async reviewRegistration(id, reviewerId, payload) {
    const registration = await ensureRegistrationExists(id);

    if (Number(registration.rol) !== 0 && Number(registration.status) === 1) {
      throw new ApiError(httpStatus.CONFLICT, 'La solicitud de registro ya fue aprobada');
    }

    if (payload.decision === 'approve') {
      const roleId = Number(payload.rol);
      if (!Number.isFinite(roleId) || roleId <= 0) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'El rol seleccionado es inválido');
      }

      const role = await roleRepository.findById(roleId);
      if (!role) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'El rol seleccionado no existe');
      }

      const updates = {
        rol: roleId,
        status: 1,
        permNoticia: payload.permNoticia !== undefined ? (payload.permNoticia ? 1 : 0) : undefined,
        permSubidaArch: payload.permSubidaArch !== undefined ? (payload.permSubidaArch ? 1 : 0) : undefined
      };

      if (payload.telefono !== undefined) {
        updates.telefono = normalizeOptional(payload.telefono);
      }

      if (payload.direccion !== undefined) {
        updates.direccion = normalizeOptional(payload.direccion);
      }

      if (payload.fecha_nacimiento !== undefined) {
        updates.fecha_nacimiento = normalizeOptional(payload.fecha_nacimiento);
      }

      const updated = await registrationRepository.update(id, updates);
      return sanitizeRegistration(updated ?? { ...registration, ...updates });
    }

    if (payload.decision === 'reject') {
      await registrationRepository.remove(id);
      return { id, state: 'rejected', removed: true };
    }

    throw new ApiError(httpStatus.BAD_REQUEST, 'La decisión indicada no es válida');
  }
};
