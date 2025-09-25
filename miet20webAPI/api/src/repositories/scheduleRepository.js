import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'horarios_materia';

const orderByDayAndTime = (queryBuilder) =>
  queryBuilder
    .orderByRaw(
      "FIELD(dia_semana, 'Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo')"
    )
    .orderBy('hora_inicio');

export const scheduleRepository = {
  async create(schedule) {
    return insertAndFetch(TABLE_NAME, schedule, { primaryKey: 'id' });
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async remove(id) {
    return getDatabase()(TABLE_NAME).where({ id }).del();
  },
  async listByCourse(courseId) {
    return orderByDayAndTime(getDatabase()(TABLE_NAME).where({ curso_id: courseId })).select('*');
  },

  async listByAssignment({ curso_id, materia_id, profesor_id }) {
    const db = getDatabase();
    return orderByDayAndTime(
      db(TABLE_NAME)
        .where({ curso_id, materia_id })
        .modify((query) => {
          if (profesor_id === null || profesor_id === undefined) {
            query.whereNull('profesor_id');
          } else {
            query.andWhere('profesor_id', profesor_id);
          }
        })
    ).select('*');
  },

  async listAssignmentsByTeacher(teacherId) {
    return getDatabase()(`${TABLE_NAME} as h`)
      .join('cursos as c', 'c.id', 'h.curso_id')
      .leftJoin('materias as m', 'm.id', 'h.materia_id')
      .where('h.profesor_id', teacherId)
      .select(
        'h.id as horario_id',
        'h.curso_id',
        'h.materia_id',
        'h.dia_semana',
        'h.hora_inicio',
        'h.hora_fin',
        'h.es_contraturno',
        'c.anio as curso_anio',
        'c.division as curso_division',
        'c.turno as curso_turno',
        'm.nombre as materia_nombre'
      )
      .orderBy('c.anio')
      .orderBy('c.division')
      .orderBy('m.nombre');
  },

  async listCourseSubjects(courseId, subjectIds = []) {
    const db = getDatabase();
    const query = db(`${TABLE_NAME} as h`)
      .join('materias as m', 'm.id', 'h.materia_id')
      .where('h.curso_id', courseId)
      .select(
        'm.id as materia_id',
        'm.nombre as materia_nombre',
        'm.es_contraturno',
        'h.dia_semana'
      );

    if (subjectIds && subjectIds.length) {
      query.whereIn('h.materia_id', subjectIds);
    }

    return query;
  }
};
