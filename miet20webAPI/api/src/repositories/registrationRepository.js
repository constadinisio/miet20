import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'usuarios';

const baseQuery = () => getDatabase()(TABLE_NAME);

export const registrationRepository = {
  async findById(id) {
    return baseQuery().where({ id }).first();
  },

  async findByEmail(mail) {
    if (!mail) {
      return null;
    }

    return baseQuery().whereRaw('LOWER(mail) = ?', [mail.toLowerCase()]).first();
  },

  async findByDni(dni) {
    if (!dni) {
      return null;
    }

    return baseQuery().where({ dni }).first();
  },

  async create(payload) {
    return insertAndFetch(TABLE_NAME, payload, { primaryKey: 'id' });
  },

  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },

  async remove(id) {
    return baseQuery().where({ id }).del();
  },

  async list(filters = {}) {
    return baseQuery().where(filters).select('*');
  }
};
