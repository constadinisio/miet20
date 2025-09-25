import httpStatus from 'http-status';
import { subjectRepository } from '../repositories/subjectRepository.js';
import { ApiError } from '../utils/ApiError.js';

const normalizeSubject = (subject) => {
  if (!subject) {
    return null;
  }

  const normalized = { ...subject };

  if (Object.prototype.hasOwnProperty.call(subject, 'categoria_id')) {
    normalized.categoria_id = subject.categoria_id !== null ? Number(subject.categoria_id) : null;
  }

  if (Object.prototype.hasOwnProperty.call(subject, 'es_contraturno')) {
    normalized.es_contraturno = Number(subject.es_contraturno ?? 0);
  }

  normalized.categoriaNombre = subject.categoria_nombre ?? null;

  if (Object.prototype.hasOwnProperty.call(subject, 'estado')) {
    normalized.estado = subject.estado;
  }

  return normalized;
};

const normalizeCategory = (category) => ({
  id: Number(category.id),
  nombre: category.nombre,
});

const sanitizeString = (value) => {
  if (typeof value !== 'string') {
    return value;
  }

  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : '';
};

const parseCategoriaId = (categoriaId) => {
  const parsed = Number(categoriaId);

  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'La categoría es obligatoria');
  }

  return parsed;
};

const parseEsContraturno = (value) => {
  if (value === undefined || value === null) {
    return 0;
  }

  if (typeof value === 'string') {
    const normalized = value.trim();
    if (normalized === '1' || normalized.toLowerCase() === 'true') {
      return 1;
    }
    return 0;
  }

  return Number(value) === 1 ? 1 : 0;
};

const ensureNombreDisponible = async (nombre, excludeId = null) => {
  const yaExiste = await subjectRepository.existsByName(nombre, excludeId);

  if (yaExiste) {
    throw new ApiError(httpStatus.CONFLICT, 'Ya existe una materia con ese nombre');
  }
};

export const subjectService = {
  async createSubject(payload) {
    const nombre = sanitizeString(payload.nombre);
    if (!nombre) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El nombre es obligatorio');
    }

    const categoriaId = parseCategoriaId(payload.categoria_id);

    await ensureNombreDisponible(nombre);

    const subjectToCreate = {
      nombre,
      codigo: sanitizeString(payload.codigo) || null,
      categoria_id: categoriaId,
      es_contraturno: parseEsContraturno(payload.es_contraturno),
      estado: 'activo'
    };

    const created = await subjectRepository.create(subjectToCreate);

    return normalizeSubject(created);
  },

  async listSubjects(filters) {
    const subjects = await subjectRepository.list(filters);

    return subjects.map((subject) => normalizeSubject(subject));
  },

  async getSubject(id) {
    const subject = await subjectRepository.findById(id);
    if (!subject) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia no encontrada');
    }
    return normalizeSubject(subject);
  },

  async updateSubject(id, updates) {
    const subject = await subjectRepository.findById(id);
    if (!subject) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia no encontrada');
    }

    const sanitizedUpdates = {};

    if (Object.prototype.hasOwnProperty.call(updates, 'nombre')) {
      const nombre = sanitizeString(updates.nombre);
      if (!nombre) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'El nombre es obligatorio');
      }
      await ensureNombreDisponible(nombre, id);
      sanitizedUpdates.nombre = nombre;
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'codigo')) {
      sanitizedUpdates.codigo = sanitizeString(updates.codigo) || null;
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'categoria_id')) {
      sanitizedUpdates.categoria_id = parseCategoriaId(updates.categoria_id);
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'es_contraturno')) {
      sanitizedUpdates.es_contraturno = parseEsContraturno(updates.es_contraturno);
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'estado')) {
      const nuevoEstado = updates.estado;
      if (!['activo', 'inactivo'].includes(nuevoEstado)) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'El estado es inválido');
      }
      sanitizedUpdates.estado = nuevoEstado;
    }

    if (Object.keys(sanitizedUpdates).length === 0) {
      return normalizeSubject(subject);
    }

    const updated = await subjectRepository.update(id, sanitizedUpdates);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia no encontrada');
    }
    return normalizeSubject(updated);
  },

  async deleteSubject(id) {
    const deleted = await subjectRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia no encontrada');
    }
    return true;
  },

  async listCategories() {
    const categories = await subjectRepository.listCategories();

    return categories.map((category) => normalizeCategory(category));
  },

  async toggleSubjectState(id, currentState = null) {
    const subject = await subjectRepository.findById(id);

    if (!subject) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia no encontrada');
    }

    const estadoActual = typeof currentState === 'string' && currentState.length > 0 ? currentState : subject.estado;

    const estadoNormalizado = estadoActual === 'inactivo' ? 'inactivo' : 'activo';
    const nuevoEstado = estadoNormalizado === 'activo' ? 'inactivo' : 'activo';

    const updated = await subjectRepository.update(id, { estado: nuevoEstado });

    return normalizeSubject(updated);
  }
};
