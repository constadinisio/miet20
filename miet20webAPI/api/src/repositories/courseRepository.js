import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'cursos';
const COURSE_ASSIGNMENTS_TABLE = 'profesor_curso_materia';

export const courseRepository = {
  async create(course) {
    return insertAndFetch(TABLE_NAME, course, { primaryKey: 'id' });
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async remove(id) {
    return getDatabase()(TABLE_NAME).where({ id }).del();
  },
  async findById(id) {
    return getDatabase()(TABLE_NAME).where({ id }).first();
  },
  async list(filters = {}) {
    const query = getDatabase()(TABLE_NAME);
    Object.entries(filters).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') {
        return;
      }
      query.andWhere(key, value);
    });
    return query.orderBy('anio').orderBy('division').select('*');
  },
  async assignSubject(payload) {
    return insertAndFetch(COURSE_ASSIGNMENTS_TABLE, payload, {
      primaryKey: 'id'
    });
  },
  async findAssignmentByProfessorCourseSubject({ curso_id, materia_id, profesor_id }) {
    const query = getDatabase()(COURSE_ASSIGNMENTS_TABLE)
      .where({ curso_id, materia_id });

    if (profesor_id === null || profesor_id === undefined) {
      query.whereNull('profesor_id');
    } else {
      query.andWhere('profesor_id', profesor_id);
    }

    return query.first();
  },
  async assignTeacher({ curso_id, materia_id, profesor_id, es_contraturno }) {
    const updated = await updateAndFetch(
      COURSE_ASSIGNMENTS_TABLE,
      { curso_id, materia_id },
      { profesor_id, es_contraturno },
      { primaryKey: 'id' }
    );
    if (updated) {
      return updated;
    }

    return insertAndFetch(
      COURSE_ASSIGNMENTS_TABLE,
      {
        curso_id,
        materia_id,
        profesor_id,
        estado: 'activo',
        es_contraturno: es_contraturno ?? 0
      },
      { primaryKey: 'id' }
    );
  },

  async removeAssignment(id) {
    return getDatabase()(COURSE_ASSIGNMENTS_TABLE).where({ id }).del();
  },

  async listAssignments(filters = {}) {
    const db = getDatabase();
    const query = db(`${COURSE_ASSIGNMENTS_TABLE} as pcm`)
      .join('cursos as c', 'c.id', 'pcm.curso_id')
      .join('materias as m', 'm.id', 'pcm.materia_id')
      .leftJoin('usuarios as u', 'u.id', 'pcm.profesor_id');

    Object.entries(filters).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') {
        return;
      }

      const numericValue = Number(value);

      switch (key) {
        case 'profesor_id':
          query.andWhere('pcm.profesor_id', numericValue);
          break;
        case 'curso_id':
          query.andWhere('pcm.curso_id', numericValue);
          break;
        case 'materia_id':
          query.andWhere('pcm.materia_id', numericValue);
          break;
        case 'estado':
          query.andWhere('pcm.estado', value);
          break;
        default:
          break;
      }
    });

    return query
      .select(
        'pcm.id',
        'pcm.profesor_id',
        'pcm.curso_id',
        'pcm.materia_id',
        'pcm.estado',
        'pcm.es_contraturno',
        'c.anio as curso_anio',
        'c.division as curso_division',
        'c.turno as curso_turno',
        'm.nombre as materia_nombre',
        'm.codigo as materia_codigo',
        'u.nombre as profesor_nombre',
        'u.apellido as profesor_apellido'
      )
      .orderBy('c.anio')
      .orderBy('c.division')
      .orderBy('m.nombre');
  },

  async findAssignmentById(id) {
    const db = getDatabase();
    return db(`${COURSE_ASSIGNMENTS_TABLE} as pcm`)
      .join('cursos as c', 'c.id', 'pcm.curso_id')
      .join('materias as m', 'm.id', 'pcm.materia_id')
      .leftJoin('usuarios as u', 'u.id', 'pcm.profesor_id')
      .where('pcm.id', id)
      .first(
        'pcm.id',
        'pcm.profesor_id',
        'pcm.curso_id',
        'pcm.materia_id',
        'pcm.estado',
        'pcm.es_contraturno',
        'c.anio as curso_anio',
        'c.division as curso_division',
        'c.turno as curso_turno',
        'm.nombre as materia_nombre',
        'm.codigo as materia_codigo',
        'u.nombre as profesor_nombre',
        'u.apellido as profesor_apellido'
      );
  }
};
