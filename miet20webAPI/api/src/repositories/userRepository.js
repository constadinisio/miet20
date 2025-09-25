import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'usuarios';

export const userRepository = {
  async findByEmail(email) {
    return getDatabase()(TABLE_NAME).where({ mail: email }).first();
  },
  async findById(id) {
    return getDatabase()(TABLE_NAME).where({ id }).first();
  },
  async list(filters = {}) {
    const db = getDatabase()(TABLE_NAME).select('*');

    const where = filters.where ?? {};
    Object.entries(where).forEach(([column, value]) => {
      if (value !== undefined && value !== null) {
        db.where(column, value);
      }
    });

    if (Array.isArray(filters.roleIn) && filters.roleIn.length > 0) {
      db.whereIn('rol', filters.roleIn);
    }

    if (Array.isArray(filters.excludeRole) && filters.excludeRole.length > 0) {
      db.whereNotIn('rol', filters.excludeRole);
    }

    if (Array.isArray(filters.statusIn) && filters.statusIn.length > 0) {
      db.whereIn('status', filters.statusIn);
    }

    if (typeof filters.search === 'string' && filters.search.trim() !== '') {
      const term = `%${filters.search.trim()}%`;
      db.where(function applySearch() {
        this.whereILike('nombre', term)
          .orWhereILike('apellido', term)
          .orWhereILike('mail', term);
      });
    }

    if (Number.isInteger(filters.limit) && filters.limit > 0) {
      db.limit(filters.limit);
    }

    if (Number.isInteger(filters.offset) && filters.offset >= 0) {
      db.offset(filters.offset);
    }

    if (Array.isArray(filters.orderBy) && filters.orderBy.length > 0) {
      filters.orderBy.forEach((order) => {
        if (order && order.column) {
          db.orderBy(order.column, order.direction ?? 'asc');
        }
      });
    } else {
      db.orderBy('apellido').orderBy('nombre').orderBy('id');
    }

    return db;
  },
  async create(user) {
    return insertAndFetch(TABLE_NAME, user, { primaryKey: 'id' });
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async remove(id) {
    return getDatabase()(TABLE_NAME).where({ id }).del();
  }
};
