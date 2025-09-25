import { getDatabase } from '../config/database.js';
import { insertAndFetch } from '../utils/dbHelpers.js';

const PENDING_TABLE = 'materias_pendientes_alumno';
const HISTORY_TABLE = 'materias_pendientes_historial';
const SUBJECTS_TABLE = 'materias';
const USERS_TABLE = 'usuarios';
const CARGOS_TABLE = 'cargos';
const CARGO_SUBJECTS_TABLE = 'cargo_materias';

export const pendingSubjectRepository = {
  async listByStudent(studentId) {
    return getDatabase()(PENDING_TABLE)
      .select(
        `${PENDING_TABLE}.id`,
        `${PENDING_TABLE}.alumno_id`,
        `${PENDING_TABLE}.materia_id`,
        `${PENDING_TABLE}.estado`,
        `${PENDING_TABLE}.ultima_nota`,
        `${PENDING_TABLE}.ultima_fecha`,
        `${PENDING_TABLE}.profesor_id`,
        `${SUBJECTS_TABLE}.nombre as materia`
      )
      .join(SUBJECTS_TABLE, `${SUBJECTS_TABLE}.id`, `${PENDING_TABLE}.materia_id`)
      .where(`${PENDING_TABLE}.alumno_id`, studentId)
      .orderBy(`${SUBJECTS_TABLE}.nombre`);
  },

  async findById(id, { trx } = {}) {
    const db = trx ?? getDatabase();
    return db(PENDING_TABLE).where({ id }).first();
  },

  async findByStudentAndSubject(studentId, subjectId, { trx } = {}) {
    const db = trx ?? getDatabase();
    return db(PENDING_TABLE).where({ alumno_id: studentId, materia_id: subjectId }).first();
  },

  async create(pendingSubject, { trx } = {}) {
    return insertAndFetch(PENDING_TABLE, pendingSubject, { primaryKey: 'id', trx });
  },

  async remove(id, { trx } = {}) {
    const db = trx ?? getDatabase();
    return db(PENDING_TABLE).where({ id }).del();
  },

  async addHistory(historyEntry, { trx } = {}) {
    return insertAndFetch(HISTORY_TABLE, historyEntry, { primaryKey: 'id', trx });
  },

  async listHistoryByStudent(studentId) {
    return getDatabase()(HISTORY_TABLE)
      .select(
        `${HISTORY_TABLE}.id`,
        `${HISTORY_TABLE}.pendiente_id`,
        `${HISTORY_TABLE}.materia_id`,
        `${HISTORY_TABLE}.alumno_id`,
        `${HISTORY_TABLE}.nota`,
        `${HISTORY_TABLE}.estado`,
        `${HISTORY_TABLE}.profesor_id`,
        `${HISTORY_TABLE}.fecha_resolucion`,
        `${SUBJECTS_TABLE}.nombre as materia`,
        `${USERS_TABLE}.nombre as profesor_nombre`,
        `${USERS_TABLE}.apellido as profesor_apellido`
      )
      .leftJoin(SUBJECTS_TABLE, `${SUBJECTS_TABLE}.id`, `${HISTORY_TABLE}.materia_id`)
      .leftJoin(USERS_TABLE, `${USERS_TABLE}.id`, `${HISTORY_TABLE}.profesor_id`)
      .where(`${HISTORY_TABLE}.alumno_id`, studentId)
      .orderBy(`${HISTORY_TABLE}.fecha_resolucion`, 'desc');
  },

  async listTeachersBySubject(subjectId) {
    return getDatabase()(USERS_TABLE)
      .select(`${USERS_TABLE}.id`, `${USERS_TABLE}.nombre`, `${USERS_TABLE}.apellido`)
      .join(CARGOS_TABLE, `${CARGOS_TABLE}.docente_id`, `${USERS_TABLE}.id`)
      .join(CARGO_SUBJECTS_TABLE, `${CARGO_SUBJECTS_TABLE}.cargo_id`, `${CARGOS_TABLE}.id`)
      .where(`${CARGO_SUBJECTS_TABLE}.materia_id`, subjectId)
      .orderBy(`${USERS_TABLE}.apellido`)
      .orderBy(`${USERS_TABLE}.nombre`)
      .distinct();
  },

  async searchSubjects(query, limit = 15) {
    const db = getDatabase();
    const normalizedLimit = Number.isInteger(limit) && limit > 0 ? Math.min(limit, 50) : 15;

    return db(SUBJECTS_TABLE)
      .select('id', 'nombre')
      .where('estado', 'activo')
      .modify((builder) => {
        const term = typeof query === 'string' ? query.trim() : '';
        if (term) {
          builder.andWhere('nombre', 'like', `%${term}%`);
        }
      })
      .orderBy('nombre')
      .limit(normalizedLimit);
  }
};
