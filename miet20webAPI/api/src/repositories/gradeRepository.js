import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'notas_bimestrales';
const PROFESSOR_ASSIGNMENTS_TABLE = 'profesor_curso_materia';
const ENROLLMENTS_TABLE = 'alumno_curso';

const CUATRIMESTER_MAP = new Map([
  ['1ER CUATRIMESTRE', ['1er Bimestre', '2do Bimestre']],
  ['2DO CUATRIMESTRE', ['3er Bimestre', '4to Bimestre']]
]);

const normalizePeriodFilter = (period) => {
  if (!period && period !== 0) {
    return null;
  }

  const raw = period.toString().trim();
  if (!raw) {
    return null;
  }

  const mapped = CUATRIMESTER_MAP.get(raw.toUpperCase());
  if (mapped) {
    return mapped;
  }

  return raw;
};

export const gradeRepository = {
  async create(grade) {
    return insertAndFetch(TABLE_NAME, grade, { primaryKey: 'id' });
  },
  async findById(id) {
    return getDatabase()(TABLE_NAME).where({ id }).first();
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async listByStudent(studentId) {
    return getDatabase()(`${TABLE_NAME} as nb`)
      .leftJoin('materias as m', 'm.id', 'nb.materia_id')
      .leftJoin('usuarios as u', 'u.id', 'nb.alumno_id')
      .where('nb.alumno_id', studentId)
      .select('nb.*', 'm.nombre as materia_nombre', 'u.nombre as alumno_nombre', 'u.apellido as alumno_apellido')
      .orderBy('m.nombre')
      .orderBy('nb.periodo');
  },
  async listByCourse(courseId, filters = {}) {
    const query = getDatabase()(`${TABLE_NAME} as nb`)
      .join('alumno_curso as ac', 'ac.alumno_id', 'nb.alumno_id')
      .where('ac.curso_id', courseId)
      .andWhere('ac.estado', 'activo')
      .leftJoin('materias as m', 'm.id', 'nb.materia_id')
      .leftJoin('usuarios as u', 'u.id', 'nb.alumno_id')
      .select('nb.*', 'm.nombre as materia_nombre', 'u.nombre as alumno_nombre', 'u.apellido as alumno_apellido');

    if (filters.materia_id) {
      const subjectId = Number(filters.materia_id);
      if (!Number.isNaN(subjectId) && subjectId > 0) {
        query.andWhere('nb.materia_id', subjectId);
      }
    }

    if (filters.periodo) {
      const normalizedPeriod = normalizePeriodFilter(filters.periodo);
      if (Array.isArray(normalizedPeriod) && normalizedPeriod.length) {
        query.whereIn('nb.periodo', normalizedPeriod);
      } else if (typeof normalizedPeriod === 'string') {
        query.andWhere('nb.periodo', normalizedPeriod);
      }
    }

    return query;
  },

  async findProfessorAssignment({ cursoId, materiaId, profesorId }) {
    return getDatabase()(PROFESSOR_ASSIGNMENTS_TABLE)
      .where({
        curso_id: cursoId,
        materia_id: materiaId,
        profesor_id: profesorId,
        estado: 'activo'
      })
      .first();
  },

  async findActiveEnrollment(studentId, courseId) {
    return getDatabase()(ENROLLMENTS_TABLE)
      .where({ alumno_id: studentId, curso_id: courseId, estado: 'activo' })
      .first();
  },

  async findProfessorAssignmentForStudent({ alumnoId, materiaId, profesorId }) {
    const db = getDatabase();

    return db(`${ENROLLMENTS_TABLE} as ac`)
      .join(`${PROFESSOR_ASSIGNMENTS_TABLE} as pcm`, function join() {
        this.on('pcm.curso_id', 'ac.curso_id')
          .andOn('pcm.materia_id', '=', db.raw('?', [materiaId]))
          .andOn('pcm.profesor_id', '=', db.raw('?', [profesorId]))
          .andOn('pcm.estado', '=', db.raw('?', ['activo']));
      })
      .where('ac.alumno_id', alumnoId)
      .andWhere('ac.estado', 'activo')
      .first('ac.curso_id as curso_id', 'pcm.id as asignacion_id');
  }
};
