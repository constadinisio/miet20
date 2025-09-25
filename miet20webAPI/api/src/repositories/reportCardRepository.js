import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const REPORT_CARD_TABLE = 'boletin';
const REPORT_CARD_GRADES_TABLE = 'calificacion_boletin';

let isInitialized = false;

const ensureTables = async () => {
  if (isInitialized) {
    return;
  }

  const db = getDatabase();

  const hasReportCardTable = await db.schema.hasTable(REPORT_CARD_TABLE);
  if (!hasReportCardTable) {
    await db.schema.createTable(REPORT_CARD_TABLE, (table) => {
      table.increments('id').primary();
      table.integer('alumno_id').unsigned().notNullable();
      table.integer('curso_id').unsigned().notNullable();
      table.integer('anio_lectivo').unsigned().notNullable();
      table.string('periodo', 100).notNullable();
      table.string('estado', 50).notNullable().defaultTo('borrador');
      table.text('observaciones');
      table.timestamp('fecha_emision');
      table.integer('creado_por').unsigned();
      table.integer('actualizado_por').unsigned();
      table.timestamp('created_at').defaultTo(db.fn.now());
      table.timestamp('updated_at').defaultTo(db.fn.now());
    });
  }

  const hasGradesTable = await db.schema.hasTable(REPORT_CARD_GRADES_TABLE);
  if (!hasGradesTable) {
    await db.schema.createTable(REPORT_CARD_GRADES_TABLE, (table) => {
      table.increments('id').primary();
      table.integer('boletin_id').unsigned().notNullable();
      table.integer('materia_id').unsigned().notNullable();
      table.decimal('nota_numerica', 5, 2);
      table.string('nota_conceptual', 100);
      table.text('observaciones');
      table.timestamp('created_at').defaultTo(db.fn.now());
      table.timestamp('updated_at').defaultTo(db.fn.now());
    });
  }

  isInitialized = true;
};

const mapReportCardRow = (row) => {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    studentId: row.alumno_id,
    courseId: row.curso_id,
    academicYear: row.anio_lectivo,
    term: row.periodo,
    status: row.estado,
    observations: row.observaciones,
    issuedAt: row.fecha_emision,
    createdBy: row.creado_por,
    updatedBy: row.actualizado_por,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    student: row.alumno_id
      ? {
          id: row.alumno_id,
          firstName: row.alumno_nombre,
          lastName: row.alumno_apellido,
          dni: row.alumno_dni,
          code: row.alumno_codigo
        }
      : null,
    course: row.curso_id
      ? {
          id: row.curso_id,
          year: row.curso_anio,
          division: row.curso_division,
          turno: row.curso_turno
        }
      : null
  };
};

const mapGradeRow = (row) => ({
  id: row.id,
  reportCardId: row.boletin_id,
  subjectId: row.materia_id,
  subjectName: row.materia_nombre,
  numericGrade: row.nota_numerica !== null ? Number(row.nota_numerica) : null,
  conceptualGrade: row.nota_conceptual,
  observations: row.observaciones
});

export const reportCardRepository = {
  async list(filters = {}) {
    await ensureTables();
    const db = getDatabase();

    const query = db(`${REPORT_CARD_TABLE} as b`)
      .leftJoin('usuarios as u', 'u.id', 'b.alumno_id')
      .leftJoin('cursos as c', 'c.id', 'b.curso_id')
      .select(
        'b.*',
        'u.nombre as alumno_nombre',
        'u.apellido as alumno_apellido',
        'u.dni as alumno_dni',
        'u.codigo_miescuela as alumno_codigo',
        'c.anio as curso_anio',
        'c.division as curso_division',
        'c.turno as curso_turno'
      )
      .orderBy('b.anio_lectivo', 'desc')
      .orderBy('b.periodo', 'desc');

    if (filters.courseId) {
      query.where('b.curso_id', filters.courseId);
    }

    if (filters.studentId) {
      query.where('b.alumno_id', filters.studentId);
    }

    if (filters.status) {
      query.where('b.estado', filters.status);
    }

    if (filters.academicYear) {
      query.where('b.anio_lectivo', filters.academicYear);
    }

    const rows = await query;
    return rows.map(mapReportCardRow);
  },

  async findById(id) {
    await ensureTables();
    const db = getDatabase();

    const row = await db(`${REPORT_CARD_TABLE} as b`)
      .leftJoin('usuarios as u', 'u.id', 'b.alumno_id')
      .leftJoin('cursos as c', 'c.id', 'b.curso_id')
      .where('b.id', id)
      .select(
        'b.*',
        'u.nombre as alumno_nombre',
        'u.apellido as alumno_apellido',
        'u.dni as alumno_dni',
        'u.codigo_miescuela as alumno_codigo',
        'c.anio as curso_anio',
        'c.division as curso_division',
        'c.turno as curso_turno'
      )
      .first();

    return mapReportCardRow(row);
  },

  async create(payload) {
    await ensureTables();
    const record = await insertAndFetch(REPORT_CARD_TABLE, {
      alumno_id: payload.studentId,
      curso_id: payload.courseId,
      anio_lectivo: payload.academicYear,
      periodo: payload.term,
      estado: payload.status,
      observaciones: payload.observations ?? null,
      fecha_emision: payload.issuedAt ?? null,
      creado_por: payload.createdBy ?? null,
      actualizado_por: payload.updatedBy ?? null
    });

    return mapReportCardRow(record);
  },

  async update(id, payload) {
    await ensureTables();

    const updates = {
      ...(payload.studentId !== undefined ? { alumno_id: payload.studentId } : {}),
      ...(payload.courseId !== undefined ? { curso_id: payload.courseId } : {}),
      ...(payload.academicYear !== undefined ? { anio_lectivo: payload.academicYear } : {}),
      ...(payload.term !== undefined ? { periodo: payload.term } : {}),
      ...(payload.status !== undefined ? { estado: payload.status } : {}),
      ...(payload.observations !== undefined ? { observaciones: payload.observations ?? null } : {}),
      ...(payload.issuedAt !== undefined ? { fecha_emision: payload.issuedAt ?? null } : {}),
      ...(payload.updatedBy !== undefined ? { actualizado_por: payload.updatedBy ?? null } : {}),
      updated_at: getDatabase().fn.now()
    };

    const updated = await updateAndFetch(REPORT_CARD_TABLE, id, updates);
    return mapReportCardRow(updated);
  },

  async deleteGradesNotIn(reportCardId, idsToKeep) {
    await ensureTables();
    const db = getDatabase();

    const query = db(REPORT_CARD_GRADES_TABLE).where('boletin_id', reportCardId);
    if (Array.isArray(idsToKeep) && idsToKeep.length > 0) {
      query.whereNotIn('id', idsToKeep);
    }

    await query.del();
  },

  async upsertGrade(reportCardId, grade) {
    await ensureTables();

    if (grade.id) {
      const updated = await updateAndFetch(
        REPORT_CARD_GRADES_TABLE,
        grade.id,
        {
          materia_id: grade.subjectId,
          nota_numerica: grade.numericGrade ?? null,
          nota_conceptual: grade.conceptualGrade ?? null,
          observaciones: grade.observations ?? null,
          updated_at: getDatabase().fn.now()
        }
      );
      return mapGradeRow(updated);
    }

    const created = await insertAndFetch(REPORT_CARD_GRADES_TABLE, {
      boletin_id: reportCardId,
      materia_id: grade.subjectId,
      nota_numerica: grade.numericGrade ?? null,
      nota_conceptual: grade.conceptualGrade ?? null,
      observaciones: grade.observations ?? null
    });

    return mapGradeRow(created);
  },

  async listGrades(reportCardId) {
    await ensureTables();
    const db = getDatabase();

    const rows = await db(`${REPORT_CARD_GRADES_TABLE} as cb`)
      .leftJoin('materias as m', 'm.id', 'cb.materia_id')
      .where('cb.boletin_id', reportCardId)
      .orderBy('m.nombre')
      .select(
        'cb.*',
        'm.nombre as materia_nombre'
      );

    return rows.map(mapGradeRow);
  },

  async listCourseSubjects(courseId) {
    await ensureTables();
    const db = getDatabase();

    const subjectsFromSchedules = await db('horarios_materia as h')
      .join('materias as m', 'm.id', 'h.materia_id')
      .where('h.curso_id', courseId)
      .groupBy('m.id', 'm.nombre')
      .orderBy('m.nombre')
      .select('m.id', 'm.nombre');

    if (subjectsFromSchedules.length > 0) {
      return subjectsFromSchedules.map((row) => ({ id: row.id, name: row.nombre }));
    }

    const subjectsFromAssignments = await db('profesor_curso_materia as pcm')
      .join('materias as m', 'm.id', 'pcm.materia_id')
      .where('pcm.curso_id', courseId)
      .groupBy('m.id', 'm.nombre')
      .orderBy('m.nombre')
      .select('m.id', 'm.nombre');

    return subjectsFromAssignments.map((row) => ({ id: row.id, name: row.nombre }));
  },

  async findLatestTermWithGrades(courseId, studentId, academicYear) {
    await ensureTables();
    const db = getDatabase();

    const courseSubjects = db('cargo_materia_curso as cmc')
      .join('cargo_materias as cm', 'cmc.cargo_materia_id', 'cm.id')
      .where('cmc.curso_id', courseId)
      .distinct('cm.materia_id');

    const row = await db('notas_bimestrales as nb')
      .join(courseSubjects.as('materias_curso'), 'materias_curso.materia_id', 'nb.materia_id')
      .where('nb.alumno_id', studentId)
      .modify((queryBuilder) => {
        if (academicYear) {
          queryBuilder.whereRaw('YEAR(nb.fecha_carga) = ?', [academicYear]);
        }
      })
      .orderBy('nb.fecha_carga', 'desc')
      .select('nb.periodo')
      .first();

    return row ? row.periodo : null;
  },

  async listSubjectsMissingGrades({ courseId, studentId, term, academicYear }) {
    await ensureTables();
    const db = getDatabase();

    const courseSubjects = db('cargo_materia_curso as cmc')
      .join('cargo_materias as cm', 'cmc.cargo_materia_id', 'cm.id')
      .where('cmc.curso_id', courseId)
      .distinct('cm.materia_id');

    const rows = await db.from(courseSubjects.as('materias_curso'))
      .join('materias as m', 'm.id', 'materias_curso.materia_id')
      .whereNotExists(function () {
        this.select(1)
          .from('notas_bimestrales as nb')
          .where('nb.alumno_id', studentId)
          .andWhere('nb.materia_id', db.ref('materias_curso.materia_id'))
          .andWhere('nb.periodo', term)
          .modify((queryBuilder) => {
            if (academicYear) {
              queryBuilder.andWhereRaw('YEAR(nb.fecha_carga) = ?', [academicYear]);
            }
          });
      })
      .orderBy('m.nombre')
      .select('m.nombre');

    return rows.map((row) => row.nombre);
  }
};
