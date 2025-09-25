import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch, upsertOnConflict } from '../utils/dbHelpers.js';

const TABLE_NAME = 'usuarios';
const ENROLLMENTS_TABLE = 'alumno_curso';
const FAMILY_TABLE = 'datos_familiares';
const LEGACY_STUDENTS_TABLE = 'alumnos';
const GENERAL_ATTENDANCE_TABLE = 'asistencia_general';
const SUBJECT_ATTENDANCE_TABLE = 'asistencia_materia';
const ASSIGNMENT_GRADES_TABLE = 'notas';
const BIMESTRAL_GRADES_TABLE = 'notas_bimestrales';
const REPORT_CARD_TABLE = 'boletin';
const REPORT_CARD_GRADES_TABLE = 'calificacion_boletin';
const NOTIFICATION_RECIPIENTS_TABLE = 'notificaciones_destinatarios';
const NOTIFICATION_GROUP_MEMBERS_TABLE = 'grupos_notificacion_miembros';
const USER_ROLES_TABLE = 'usuario_roles';

const normalizeEnrollment = (enrollment) => {
  if (!enrollment) {
    return null;
  }

  return {
    id: enrollment.id ? Number(enrollment.id) : null,
    alumno_id: Number(enrollment.alumno_id),
    curso_id: Number(enrollment.curso_id),
    estado: enrollment.estado
  };
};

export const studentRepository = {
  async create(student) {
    return insertAndFetch(TABLE_NAME, student, { primaryKey: 'id' });
  },
  async update(id, updates) {
    return updateAndFetch(TABLE_NAME, id, updates, { primaryKey: 'id' });
  },
  async remove(id) {
    const db = getDatabase();

    const tablesToCheck = [
      ENROLLMENTS_TABLE,
      FAMILY_TABLE,
      GENERAL_ATTENDANCE_TABLE,
      SUBJECT_ATTENDANCE_TABLE,
      ASSIGNMENT_GRADES_TABLE,
      BIMESTRAL_GRADES_TABLE,
      REPORT_CARD_TABLE,
      REPORT_CARD_GRADES_TABLE,
      NOTIFICATION_RECIPIENTS_TABLE,
      NOTIFICATION_GROUP_MEMBERS_TABLE,
      USER_ROLES_TABLE,
      LEGACY_STUDENTS_TABLE
    ];

    const tableExists = {};
    for (const table of tablesToCheck) {
      tableExists[table] = await db.schema.hasTable(table);
    }

    return db.transaction(async (trx) => {
      if (tableExists[ENROLLMENTS_TABLE]) {
        await trx(ENROLLMENTS_TABLE).where({ alumno_id: id }).del();
      }

      if (tableExists[FAMILY_TABLE]) {
        await trx(FAMILY_TABLE).where({ usuario_id: id }).del();
      }

      if (tableExists[GENERAL_ATTENDANCE_TABLE]) {
        await trx(GENERAL_ATTENDANCE_TABLE).where({ alumno_id: id }).del();
      }

      if (tableExists[SUBJECT_ATTENDANCE_TABLE]) {
        await trx(SUBJECT_ATTENDANCE_TABLE).where({ alumno_id: id }).del();
      }

      if (tableExists[ASSIGNMENT_GRADES_TABLE]) {
        await trx(ASSIGNMENT_GRADES_TABLE).where({ alumno_id: id }).del();
      }

      if (tableExists[BIMESTRAL_GRADES_TABLE]) {
        await trx(BIMESTRAL_GRADES_TABLE).where({ alumno_id: id }).del();
      }

      if (tableExists[REPORT_CARD_TABLE]) {
        const reportCardIds = await trx(REPORT_CARD_TABLE).where({ alumno_id: id }).pluck('id');

        if (reportCardIds.length && tableExists[REPORT_CARD_GRADES_TABLE]) {
          await trx(REPORT_CARD_GRADES_TABLE).whereIn('boletin_id', reportCardIds).del();
        }

        if (reportCardIds.length) {
          await trx(REPORT_CARD_TABLE).whereIn('id', reportCardIds).del();
        } else {
          await trx(REPORT_CARD_TABLE).where({ alumno_id: id }).del();
        }
      }

      if (tableExists[NOTIFICATION_RECIPIENTS_TABLE]) {
        await trx(NOTIFICATION_RECIPIENTS_TABLE).where({ destinatario_id: id }).del();
      }

      if (tableExists[NOTIFICATION_GROUP_MEMBERS_TABLE]) {
        await trx(NOTIFICATION_GROUP_MEMBERS_TABLE).where({ usuario_id: id }).del();
      }

      if (tableExists[USER_ROLES_TABLE]) {
        await trx(USER_ROLES_TABLE).where({ usuario_id: id }).del();
      }

      if (tableExists[LEGACY_STUDENTS_TABLE]) {
        await trx(LEGACY_STUDENTS_TABLE).where({ id }).del();
      }

      const deleted = await trx(TABLE_NAME)
        .where({ id, rol: 4 })
        .del();

      return deleted;
    });
  },
  async findById(id) {
    return getDatabase()(TABLE_NAME).where({ id, rol: 4 }).first();
  },
  async list(filters = {}) {
    const db = getDatabase();

    const page = Number(filters.page) > 0 ? Number(filters.page) : 1;
    const limitRaw = Number(filters.limit) > 0 ? Number(filters.limit) : null;
    const limit = limitRaw ? Math.min(limitRaw, 100) : 10;
    const offset = (page - 1) * limit;

    const search = typeof filters.search === 'string' ? filters.search.trim() : '';

    const query = db(TABLE_NAME).where({ rol: 4 });

    if (filters.status !== undefined && filters.status !== null && filters.status !== '') {
      const normalizedStatus = Number(filters.status);
      query.andWhere('status', Number.isNaN(normalizedStatus) ? filters.status : normalizedStatus);
    }

    if (filters.anio !== undefined && filters.anio !== null && filters.anio !== '') {
      query.andWhere('anio', filters.anio);
    }

    if (filters.division !== undefined && filters.division !== null && filters.division !== '') {
      query.andWhere('division', filters.division);
    }

    if (search) {
      query.andWhere((builder) => {
        builder
          .where('nombre', 'like', `%${search}%`)
          .orWhere('apellido', 'like', `%${search}%`)
          .orWhere('dni', 'like', `%${search}%`);
      });
    }

    const countQuery = query.clone().clearSelect().clearOrder().count({ total: '*' }).first();
    const rowsQuery = query
      .clone()
      .orderBy('apellido')
      .orderBy('nombre')
      .offset(offset)
      .limit(limit)
      .select('*');

    const [countResult, rows] = await Promise.all([countQuery, rowsQuery]);
    const total = Number(countResult?.total ?? 0);
    const totalPages = total > 0 ? Math.ceil(total / limit) : 0;

    return {
      items: rows,
      pagination: {
        page,
        limit,
        total,
        totalPages
      }
    };
  },
  async listByCourse(courseId) {
    const db = getDatabase();
    return db('usuarios as u')
      .join('alumno_curso as ac', 'ac.alumno_id', 'u.id')
      .leftJoin('notas as n', 'n.alumno_id', 'u.id')
      .where('u.rol', 4)
      .andWhere('ac.curso_id', courseId)
      .andWhere('ac.estado', 'activo')
      .andWhere('u.status', 1)
      .groupBy('u.id', 'u.nombre', 'u.apellido', 'u.dni', 'u.estado_academico')
      .orderBy('u.apellido')
      .orderBy('u.nombre')
      .select('u.id', 'u.nombre', 'u.apellido', 'u.dni', 'u.estado_academico')
      .select(db.raw('ROUND(AVG(n.nota), 1) as promedio'));
  },
  async findEnrollment(studentId, courseId) {
    const enrollment = await getDatabase()(ENROLLMENTS_TABLE)
      .where({ alumno_id: studentId, curso_id: courseId })
      .first();
    return normalizeEnrollment(enrollment);
  },
  async enroll(data) {
    await getDatabase()(ENROLLMENTS_TABLE).insert(data);
    const created = await getDatabase()(ENROLLMENTS_TABLE)
      .where({ alumno_id: data.alumno_id, curso_id: data.curso_id })
      .first();
    return normalizeEnrollment(created);
  },
  async updateEnrollment(studentId, courseId, updates) {
    await getDatabase()(ENROLLMENTS_TABLE)
      .where({ alumno_id: studentId, curso_id: courseId })
      .update(updates);

    const refreshed = await getDatabase()(ENROLLMENTS_TABLE)
      .where({ alumno_id: studentId, curso_id: courseId })
      .first();

    return normalizeEnrollment(refreshed);
  },
  async unenroll(studentId, courseId) {
    return getDatabase()(ENROLLMENTS_TABLE)
      .where({ alumno_id: studentId, curso_id: courseId })
      .del();
  },

  async findFamilyByStudentId(studentId) {
    return getDatabase()(FAMILY_TABLE).where({ usuario_id: studentId }).first();
  },

  async upsertFamily(studentId, payload) {
    return upsertOnConflict(
      FAMILY_TABLE,
      {
        usuario_id: studentId,
        ...payload,
        updated_at: new Date()
      },
      ['usuario_id'],
      {
        ...payload,
        updated_at: new Date()
      }
    );
  },

  async updateCensus(studentId, payload) {
    return updateAndFetch(
      TABLE_NAME,
      studentId,
      {
        ...payload,
        updated_at: new Date()
      },
      { primaryKey: 'id' }
    );
  },

  async bulkProgress({ courseOriginId, movements }) {
    const db = getDatabase();
    const timestamp = new Date();

    return db.transaction(async (trx) => {
      const processed = [];
      const errors = [];

      const normalizeDestination = (rawDestination) => {
        if (rawDestination === null || rawDestination === undefined || rawDestination === '') {
          return { type: 'invalid', message: 'Destino no especificado' };
        }

        if (typeof rawDestination === 'string') {
          const normalized = rawDestination.trim();
          if (!normalized) {
            return { type: 'invalid', message: 'Destino no especificado' };
          }

          const upper = normalized.toUpperCase();
          if (upper === 'EGRESO') {
            return { type: 'egreso' };
          }

          const numeric = Number(normalized);
          if (Number.isInteger(numeric) && numeric > 0) {
            return { type: 'course', courseId: numeric };
          }

          return { type: 'invalid', message: 'Destino inválido' };
        }

        if (typeof rawDestination === 'number') {
          if (Number.isInteger(rawDestination) && rawDestination > 0) {
            return { type: 'course', courseId: rawDestination };
          }
          return { type: 'invalid', message: 'Destino inválido' };
        }

        return { type: 'invalid', message: 'Destino inválido' };
      };

      const appendError = (studentId, message) => {
        errors.push({ alumno_id: studentId, message });
      };

      for (const movement of movements) {
        const studentId = Number(movement.studentId);
        if (!Number.isInteger(studentId) || studentId <= 0) {
          appendError(null, 'Identificador de alumno inválido');
          continue;
        }

        try {
          const destination = normalizeDestination(movement.destination);
          if (destination.type === 'invalid') {
            appendError(studentId, destination.message);
            continue;
          }

          const student = await trx(TABLE_NAME).where({ id: studentId, rol: 4 }).first();
          if (!student) {
            appendError(studentId, 'El alumno no existe o no es válido');
            continue;
          }

          const enrollment = await trx(ENROLLMENTS_TABLE)
            .where({ alumno_id: studentId, curso_id: courseOriginId })
            .first();

          if (!enrollment) {
            appendError(studentId, 'El alumno no está inscripto en el curso de origen');
            continue;
          }

          if (destination.type === 'egreso') {
            await trx(TABLE_NAME)
              .where({ id: studentId })
              .update({ estado_academico: 'EGRESADO', updated_at: timestamp });

            const updatedEnrollments = await trx(ENROLLMENTS_TABLE)
              .where({ alumno_id: studentId, curso_id: courseOriginId })
              .update({ estado: 'inactivo', updated_at: timestamp });

            if (!updatedEnrollments) {
              appendError(studentId, 'No se pudo actualizar la inscripción del alumno');
              continue;
            }

            processed.push({ alumno_id: studentId, accion: 'egreso' });
            continue;
          }

          const course = await trx('cursos').where({ id: destination.courseId }).first();
          if (!course) {
            appendError(studentId, `El curso destino ${destination.courseId} no existe`);
            continue;
          }

          const updated = await trx(ENROLLMENTS_TABLE)
            .where({ alumno_id: studentId, curso_id: courseOriginId })
            .update({ curso_id: destination.courseId, estado: 'activo', updated_at: timestamp });

          if (!updated) {
            appendError(studentId, 'No se pudo actualizar la inscripción del alumno');
            continue;
          }

          processed.push({ alumno_id: studentId, accion: 'promocion', curso_destino_id: destination.courseId });
        } catch (error) {
          appendError(studentId, error?.message ?? 'Error desconocido al procesar al alumno');
        }
      }

      return { processed, errors };
    });
  }
};
