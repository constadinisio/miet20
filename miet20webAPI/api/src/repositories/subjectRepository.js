import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'materias';
const CATEGORY_TABLE = 'categorias';

export const subjectRepository = {
  async create(subject) {
    return insertAndFetch(TABLE_NAME, subject, { primaryKey: 'id' });
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async remove(id) {
    return getDatabase()(TABLE_NAME).where({ id }).del();
  },
  async findById(id) {
    return getDatabase()(`${TABLE_NAME} as m`)
      .leftJoin(`${CATEGORY_TABLE} as c`, 'c.id', 'm.categoria_id')
      .where('m.id', id)
      .first(['m.*', getDatabase().raw('c.nombre as categoria_nombre')]);
  },
  async list(filters = {}) {
    const query = getDatabase()(`${TABLE_NAME} as m`).leftJoin(
      `${CATEGORY_TABLE} as c`,
      'c.id',
      'm.categoria_id'
    );
    Object.entries(filters).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') {
        return;
      }
      if (!/^[a-zA-Z0-9_]+$/.test(key)) {
        return;
      }
      query.andWhere(`m.${key}`, value);
    });
    return query.orderBy('m.nombre').select('m.*', getDatabase().raw('c.nombre as categoria_nombre'));
  },
  async listCategories() {
    return getDatabase()(CATEGORY_TABLE).orderBy('nombre').select('id', 'nombre');
  },
  async existsByName(nombre, excludeId = null) {
    const query = getDatabase()(TABLE_NAME)
      .whereRaw('LOWER(nombre) = LOWER(?)', [nombre]);

    if (excludeId !== null && excludeId !== undefined) {
      query.andWhereNot('id', excludeId);
    }

    const result = await query.count({ count: '*' }).first();
    const count = Number(result?.count ?? result?.['count(*)'] ?? 0);

    return count > 0;
  }
};
