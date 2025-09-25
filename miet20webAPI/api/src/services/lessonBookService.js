import httpStatus from 'http-status';
import { lessonBookRepository } from '../repositories/lessonBookRepository.js';
import { ApiError } from '../utils/ApiError.js';

const sanitizeString = (value) => {
  if (typeof value !== 'string') {
    return '';
  }

  return value.trim();
};

export const lessonBookService = {
  async listEntries({ profesorId, cursoId, materiaId }) {
    return lessonBookRepository.listEntries(profesorId, cursoId, materiaId);
  },

  async getEntry(entryId, profesorId) {
    const entry = await lessonBookRepository.getEntryById(entryId);
    if (!entry || entry.profesor_id !== profesorId) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Tema no encontrado');
    }

    return entry;
  },

  async createEntry({
    profesorId,
    cursoId,
    materiaId,
    fechaClase,
    caracterClase,
    tema,
    actividades,
    observaciones
  }) {
    const book = await lessonBookRepository.ensureBook(profesorId, cursoId, materiaId);

    const entryPayload = {
      libro_id: book.id,
      profesor_id: profesorId,
      curso_id: cursoId,
      materia_id: materiaId,
      fecha_clase: fechaClase,
      caracter_clase: sanitizeString(caracterClase) || 'Explicativa',
      tema: sanitizeString(tema),
      actividades_desarrolladas: sanitizeString(actividades),
      observaciones: sanitizeString(observaciones) || null,
      fecha_carga: new Date()
    };

    return lessonBookRepository.createEntry(entryPayload);
  },

  async updateEntry(entryId, profesorId, updates) {
    const entry = await this.getEntry(entryId, profesorId);

    const updatePayload = {};
    if (updates.fecha_clase) {
      updatePayload.fecha_clase = updates.fecha_clase;
    }
    if (typeof updates.caracter_clase === 'string') {
      updatePayload.caracter_clase = sanitizeString(updates.caracter_clase);
    }
    if (typeof updates.tema === 'string') {
      updatePayload.tema = sanitizeString(updates.tema);
    }
    if (typeof updates.actividades_desarrolladas === 'string') {
      updatePayload.actividades_desarrolladas = sanitizeString(updates.actividades_desarrolladas);
    }
    if (typeof updates.observaciones === 'string') {
      updatePayload.observaciones = sanitizeString(updates.observaciones) || null;
    }

    if (!Object.keys(updatePayload).length) {
      return entry;
    }

    const updated = await lessonBookRepository.updateEntry(entryId, updatePayload);
    if (!updated) {
      throw new ApiError(httpStatus.INTERNAL_SERVER_ERROR, 'No fue posible actualizar el tema');
    }

    return updated;
  },

  async deleteEntry(entryId, profesorId) {
    await this.getEntry(entryId, profesorId);
    const deleted = await lessonBookRepository.deleteEntry(entryId);

    if (!deleted) {
      throw new ApiError(httpStatus.INTERNAL_SERVER_ERROR, 'No fue posible eliminar el tema');
    }

    return true;
  }
};
