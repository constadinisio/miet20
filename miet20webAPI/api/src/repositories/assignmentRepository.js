import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch, upsertOnConflict } from '../utils/dbHelpers.js';

const TABLE_NAME = 'trabajos';
const SUBMISSIONS_TABLE = 'notas';
const SUBJECTS_TABLE = 'materias';

export const assignmentRepository = {
  async create(assignment) {
    return insertAndFetch(TABLE_NAME, assignment, { primaryKey: 'id' });
  },
  async findById(id) {
    return getDatabase()(TABLE_NAME).where({ id }).first();
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async remove(id) {
    return getDatabase()(TABLE_NAME).where({ id }).del();
  },
  async listByCourse(courseId, { subjectId, teacherId } = {}) {
    const db = getDatabase();
    const query = db(`${TABLE_NAME} as t`)
      .join('profesor_curso_materia as pcm', 'pcm.materia_id', 't.materia_id')
      .where('pcm.curso_id', courseId)
      .andWhere('pcm.estado', 'activo');

    if (teacherId) {
      query.andWhere('pcm.profesor_id', teacherId);
    }

    if (subjectId) {
      query.andWhere('t.materia_id', subjectId);
    }

    return query
      .distinct('t.id', 't.materia_id', 't.nombre', 't.tipo', 't.descripcion', 't.fecha_creacion')
      .orderBy('t.fecha_creacion')
      .orderBy('t.nombre');
  },
  async createSubmission(submission) {
    return upsertOnConflict(
      SUBMISSIONS_TABLE,
      {
        alumno_id: submission.alumno_id,
        trabajo_id: submission.trabajo_id,
        materia_id: submission.materia_id,
        nota: submission.nota,
        fecha_carga: new Date()
      },
      ['alumno_id', 'trabajo_id'],
      {
        nota: submission.nota,
        fecha_carga: new Date()
      }
    );
  },

  async listStudentSubmissions(studentId) {
    return getDatabase()(`${SUBMISSIONS_TABLE} as n`)
      .join(`${TABLE_NAME} as t`, 't.id', 'n.trabajo_id')
      .leftJoin(`${SUBJECTS_TABLE} as m`, 'm.id', 'n.materia_id')
      .where('n.alumno_id', studentId)
      .orderBy('m.nombre')
      .orderBy('n.fecha_carga', 'desc')
      .select(
        'n.id',
        'n.trabajo_id',
        'n.materia_id',
        'n.nota',
        'n.fecha_carga',
        't.nombre as trabajo_nombre',
        't.tipo as trabajo_tipo',
        'm.nombre as materia_nombre'
      );
  },

  async listCourseAssignmentGrades(courseId, { subjectIds, studentIds } = {}) {
    const db = getDatabase();
    const query = db(`${SUBMISSIONS_TABLE} as n`)
      .join(`${TABLE_NAME} as t`, 't.id', 'n.trabajo_id')
      .join('alumno_curso as ac', 'ac.alumno_id', 'n.alumno_id')
      .where('ac.curso_id', courseId)
      .andWhere('ac.estado', 'activo');

    if (Array.isArray(subjectIds) && subjectIds.length) {
      query.whereIn('t.materia_id', subjectIds);
    }

    if (Array.isArray(studentIds) && studentIds.length) {
      query.whereIn('n.alumno_id', studentIds);
    }

    return query
      .select('n.alumno_id', 'n.trabajo_id', 'n.nota', 'n.fecha_carga')
      .orderBy('n.alumno_id')
      .orderBy('n.trabajo_id');
  }
};
