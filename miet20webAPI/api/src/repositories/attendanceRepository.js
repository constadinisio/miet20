import { getDatabase } from '../config/database.js';
import { upsertOnConflict } from '../utils/dbHelpers.js';
import { mapDayValueToNumber } from '../utils/dayOfWeek.js';

const GENERAL_TABLE = 'asistencia_general';
const SUBJECT_TABLE = 'asistencia_materia';
const SCHEDULE_TABLE = 'horarios_materia';
const SUBJECTS_TABLE = 'materias';
const STUDENTS_TABLE = 'usuarios';
const ENROLLMENTS_TABLE = 'alumno_curso';
const COURSES_TABLE = 'cursos';

export const attendanceRepository = {
  async record(attendance) {
    return upsertOnConflict(
      GENERAL_TABLE,
      attendance,
      ['alumno_id', 'curso_id', 'fecha', 'es_contraturno'],
      {
        estado: attendance.estado,
        creado_por: attendance.creado_por,
        actualizado_en: new Date()
      }
    );
  },

  async update(criteria, updates) {
    const { alumno_id, curso_id, fecha, es_contraturno } = criteria;
    return getDatabase()(GENERAL_TABLE)
      .where({ alumno_id, curso_id, fecha, es_contraturno })
      .update(updates);
  },

  async listByCourseAndDate(courseId, date) {
    const query = getDatabase()(GENERAL_TABLE).where({ curso_id: courseId });
    if (date) {
      query.andWhere({ fecha: date });
    }
    return query.select('*');
  },

  async listByCourseAndDates(courseId, dates) {
    const query = getDatabase()(GENERAL_TABLE).where({ curso_id: courseId });

    if (Array.isArray(dates) && dates.length > 0) {
      query.whereIn('fecha', dates);
    }

    return query.select('alumno_id', 'curso_id', 'fecha', 'estado', 'es_contraturno');
  },

  async listByStudent(studentId) {
    return getDatabase()(`${GENERAL_TABLE} as ag`)
      .join(`${COURSES_TABLE} as c`, 'c.id', 'ag.curso_id')
      .where('ag.alumno_id', studentId)
      .orderBy('ag.fecha', 'desc')
      .select(
        'ag.fecha',
        'ag.estado',
        'ag.es_contraturno',
        'c.anio',
        'c.division'
      );
  },

  async hasTeacherAssignment(teacherId, courseId, subjectId) {
    const assignment = await getDatabase()(SCHEDULE_TABLE)
      .where({ profesor_id: teacherId, curso_id: courseId, materia_id: subjectId })
      .first();
    return Boolean(assignment);
  },

  async getTeacherAllowedWeekdays(teacherId, courseId, subjectId) {
    const rows = await getDatabase()(SCHEDULE_TABLE)
      .where({ profesor_id: teacherId, curso_id: courseId, materia_id: subjectId })
      .select('dia_semana');

    const allowed = rows
      .map((row) => mapDayValueToNumber(row.dia_semana))
      .filter((day) => day !== null);

    const unique = Array.from(new Set(allowed));
    unique.sort((a, b) => a - b);
    return unique;
  },

  async listCourseStudents(courseId) {
    return getDatabase()
      .select('u.id', 'u.nombre', 'u.apellido')
      .from(`${STUDENTS_TABLE} as u`)
      .join(`${ENROLLMENTS_TABLE} as ac`, 'ac.alumno_id', 'u.id')
      .where('ac.curso_id', courseId)
      .andWhere('ac.estado', 'activo')
      .andWhere('u.rol', 4)
      .orderBy('u.apellido')
      .orderBy('u.nombre');
  },

  async listSubjectAttendances(courseId, subjectId, startDate, endDate) {
    const db = getDatabase();
    const query = db(SUBJECT_TABLE)
      .where({ curso_id: courseId, materia_id: subjectId })
      .select('alumno_id', 'fecha', 'estado');

    if (startDate && endDate) {
      query.andWhereBetween('fecha', [startDate, endDate]);
    }

    return query;
  },

  async listSubjectAttendancesForDate(courseId, subjectIds, date, studentIds) {
    const db = getDatabase();
    const query = db(SUBJECT_TABLE)
      .where({ curso_id: courseId })
      .andWhere('fecha', date)
      .select('alumno_id', 'materia_id', 'estado');

    if (subjectIds && subjectIds.length) {
      query.whereIn('materia_id', subjectIds);
    }

    if (studentIds && studentIds.length) {
      query.whereIn('alumno_id', studentIds);
    }

    return query;
  },

  async upsertSubjectAttendance(record) {
    return upsertOnConflict(
      SUBJECT_TABLE,
      record,
      ['alumno_id', 'curso_id', 'materia_id', 'fecha'],
      {
        estado: record.estado,
        creado_por: record.creado_por,
        actualizado_en: new Date()
      }
    );
  },

  async deleteSubjectAttendanceByDate(courseId, subjectId, date) {
    return getDatabase()(SUBJECT_TABLE)
      .where({ curso_id: courseId, materia_id: subjectId, fecha: date })
      .del();
  },

  async getSubjectShift(subjectId) {
    return getDatabase()(SUBJECTS_TABLE)
      .where({ id: subjectId })
      .select('es_contraturno')
      .first();
  },

  async listGeneralSummary(courseId, date) {
    const db = getDatabase();
    return db(GENERAL_TABLE)
      .where({ curso_id: courseId })
      .andWhere(db.raw('DATE(fecha)'), date)
      .select(
        db.raw('COALESCE(es_contraturno, 0) as es_contraturno'),
        db.raw("SUM(CASE WHEN UPPER(TRIM(estado)) = 'P' THEN 1 ELSE 0 END) AS presentes"),
        db.raw("SUM(CASE WHEN UPPER(TRIM(estado)) = 'A' THEN 1 ELSE 0 END) AS ausentes"),
        db.raw("SUM(CASE WHEN UPPER(TRIM(estado)) = 'T' THEN 1 ELSE 0 END) AS tarde"),
        db.raw('COUNT(*) AS total')
      )
      .groupBy('es_contraturno');
  },

  async listGeneralByDateForShift(courseId, date, esContraturno) {
    const db = getDatabase();
    const query = db(GENERAL_TABLE)
      .where({ curso_id: courseId })
      .andWhere(db.raw('DATE(fecha)'), date)
      .select('alumno_id', 'estado');

    if (esContraturno !== null && esContraturno !== undefined) {
      if (Number(esContraturno) === 0) {
        query.andWhere((builder) => builder.where('es_contraturno', 0).orWhereNull('es_contraturno'));
      } else {
        query.andWhere('es_contraturno', esContraturno);
      }
    }

    return query;
  },

  async getCourseDateRange(courseId) {
    const db = getDatabase();
    return db(GENERAL_TABLE)
      .where({ curso_id: courseId })
      .select(db.raw('MIN(fecha) as inicio'), db.raw('MAX(fecha) as fin'))
      .first();
  }
};
