import { getDatabase } from '../config/database.js';

export const roleRepository = {
  listAll() {
    return getDatabase()('roles').select('id', 'nombre');
  },

  findById(id) {
    return getDatabase()('roles').where({ id }).first();
  },

  async findForUser(userId) {
    return getDatabase()('usuario_roles as ur')
      .join('roles as r', 'ur.rol_id', 'r.id')
      .where('ur.usuario_id', userId)
      .select('r.id', 'r.nombre');
  }
};