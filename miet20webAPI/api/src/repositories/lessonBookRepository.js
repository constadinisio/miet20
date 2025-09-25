import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const BOOKS_TABLE = 'libros_temas';
const ENTRIES_TABLE = 'temas_diarios';

const currentYear = () => new Date().getFullYear();

export const lessonBookRepository = {
  async findBook(profesorId, cursoId, materiaId, year = currentYear()) {
    return getDatabase()(BOOKS_TABLE)
      .where({
        profesor_id: profesorId,
        curso_id: cursoId,
        materia_id: materiaId,
        anio_lectivo: year
      })
      .first();
  },

  async ensureBook(profesorId, cursoId, materiaId) {
    const existing = await this.findBook(profesorId, cursoId, materiaId);
    if (existing) {
      return existing;
    }

    return insertAndFetch(
      BOOKS_TABLE,
      {
        profesor_id: profesorId,
        curso_id: cursoId,
        materia_id: materiaId,
        anio_lectivo: currentYear(),
        estado: 'activo'
      },
      { primaryKey: 'id' }
    );
  },

  async listEntries(profesorId, cursoId, materiaId) {
    return getDatabase()(ENTRIES_TABLE)
      .where({
        profesor_id: profesorId,
        curso_id: cursoId,
        materia_id: materiaId
      })
      .orderBy('fecha_clase', 'desc')
      .orderBy('id', 'desc');
  },

  async getEntryById(entryId) {
    return getDatabase()(ENTRIES_TABLE).where({ id: entryId }).first();
  },

  async createEntry(entry) {
    return insertAndFetch(ENTRIES_TABLE, entry, { primaryKey: 'id' });
  },

  async updateEntry(entryId, updates) {
    return updateAndFetch(ENTRIES_TABLE, entryId, updates, { primaryKey: 'id' });
  },

  async deleteEntry(entryId) {
    return getDatabase()(ENTRIES_TABLE).where({ id: entryId }).del();
  }
};
