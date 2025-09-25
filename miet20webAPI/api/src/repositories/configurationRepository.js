import { getDatabase } from '../config/database.js';
import { upsertOnConflict } from '../utils/dbHelpers.js';

const TABLE_NAME = 'configuracion';

export const configurationRepository = {
  async upsert(key, value) {
    return upsertOnConflict(
      TABLE_NAME,
      { clave: key, valor: value, actualizado_en: new Date() },
      ['clave'],
      { valor: value, actualizado_en: new Date() }
    );
  },
  async list() {
    return getDatabase()(TABLE_NAME).select('*');
  }
};
