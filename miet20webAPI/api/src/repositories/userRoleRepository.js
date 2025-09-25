import { getDatabase } from '../config/database.js';

const TABLE_NAME = 'usuario_roles';

export const userRoleRepository = {
  async listIdsForUser(userId) {
    return getDatabase()(TABLE_NAME)
      .where({ usuario_id: userId })
      .pluck('rol_id');
  },

  async replaceUserRoles(userId, roleIds) {
    const db = getDatabase();
    const uniqueRoleIds = [...new Set(roleIds)].map(Number).filter((value) => Number.isInteger(value));

    await db.transaction(async (trx) => {
      await trx(TABLE_NAME).where({ usuario_id: userId }).del();

      if (uniqueRoleIds.length === 0) {
        return;
      }

      const rows = uniqueRoleIds.map((roleId) => ({
        usuario_id: userId,
        rol_id: roleId
      }));

      await trx(TABLE_NAME).insert(rows);
    });
  }
};
