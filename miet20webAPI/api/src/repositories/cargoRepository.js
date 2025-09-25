import { getDatabase } from '../config/database.js';

const ORDER_DAYS = "FIELD(dia_semana,'Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo')";

const resolveDb = (trx) => trx ?? getDatabase();

export const cargoRepository = {
  async findById(id, { trx, forUpdate = false } = {}) {
    const db = resolveDb(trx);
    const query = db('cargos').where('id', id);

    if (forUpdate && typeof query.forUpdate === 'function') {
      query.forUpdate();
    }

    return query.first();
  },

  async create(cargo, { trx } = {}) {
    const db = resolveDb(trx);
    const [id] = await db('cargos').insert(cargo);
    return Number(id);
  },

  async update(id, updates, { trx } = {}) {
    const db = resolveDb(trx);
    return db('cargos').where('id', id).update(updates);
  },

  async softDelete(id, { trx } = {}) {
    const db = resolveDb(trx);
    return db('cargos').where('id', id).update({ estado: 'inactivo' });
  },

  async reactivate(id, { trx } = {}) {
    const db = resolveDb(trx);
    return db('cargos').where('id', id).update({ estado: 'activo' });
  },

  async deleteById(id, { trx } = {}) {
    const db = resolveDb(trx);
    return db('cargos').where('id', id).del();
  },

  async insertReplacement({ cargoId, replacementId }, { trx } = {}) {
    const db = resolveDb(trx);
    return db('reemplazos').insert({
      cargo_id: cargoId,
      cargo_reemplazo_id: replacementId,
      fecha: db.fn.now()
    });
  },

  async listReplacementIds(cargoId, { trx } = {}) {
    const db = resolveDb(trx);
    return db('reemplazos').select('cargo_reemplazo_id').where('cargo_id', cargoId);
  },

  async detachReplacementByReplacementId(replacementId, { trx } = {}) {
    const db = resolveDb(trx);
    return db('reemplazos').where('cargo_reemplazo_id', replacementId).update({ cargo_reemplazo_id: null });
  },

  async listAll() {
    const db = getDatabase();

    return db('cargos as c')
      .leftJoin('usuarios as u', 'c.docente_id', 'u.id')
      .leftJoin('cargos_max_horas as cmh', 'c.tipo_cargo_id', 'cmh.id')
      .leftJoin('cargo_materias as cm', 'cm.cargo_id', 'c.id')
      .leftJoin('materias as m', 'cm.materia_id', 'm.id')
      .select(
        'c.id',
        'c.codigo_cargo',
        'c.tipo as situacion',
        'c.estado',
        'c.fecha_inicio',
        'c.fecha_fin',
        'c.observaciones',
        'c.docente_id',
        'cmh.nombre as tipo_cargo_nombre',
        'u.nombre as docente_nombre',
        'u.apellido as docente_apellido',
        db.raw("GROUP_CONCAT(DISTINCT m.nombre ORDER BY m.nombre SEPARATOR ', ') as materia_nombre")
      )
      .groupBy(
        'c.id',
        'c.codigo_cargo',
        'c.tipo',
        'c.estado',
        'c.fecha_inicio',
        'c.fecha_fin',
        'c.observaciones',
        'c.docente_id',
        'cmh.nombre',
        'u.nombre',
        'u.apellido'
      )
      .orderBy('c.id', 'desc');
  },

  async listAllSchedules() {
    const db = getDatabase();
    return db('cargas_horarias')
      .select('id', 'cargo_id', 'dia_semana', 'hora_inicio', 'hora_fin', 'tipo')
      .orderByRaw(`${ORDER_DAYS}, hora_inicio`);
  },

  async listSchedulesForCargo(cargoId, { trx } = {}) {
    const db = resolveDb(trx);
    return db('cargas_horarias')
      .select('id', 'cargo_id', 'dia_semana', 'hora_inicio', 'hora_fin', 'tipo')
      .where('cargo_id', cargoId)
      .orderByRaw(`${ORDER_DAYS}, hora_inicio`);
  },

  async findScheduleById(scheduleId, { trx } = {}) {
    const db = resolveDb(trx);
    return db('cargas_horarias').where('id', scheduleId).first();
  },

  async insertSchedule({ cargoId, dia, horaInicio, horaFin, tipo }, { trx } = {}) {
    const db = resolveDb(trx);
    const [id] = await db('cargas_horarias').insert({
      cargo_id: cargoId,
      dia_semana: dia,
      hora_inicio: horaInicio,
      hora_fin: horaFin,
      tipo
    });

    return Number(id);
  },

  async upsertSchedule({ trx, cargoId, dia, horaInicio, horaFin, tipo }) {
    const existing = await trx('cargas_horarias')
      .where({
        cargo_id: cargoId,
        dia_semana: dia,
        hora_inicio: horaInicio,
        hora_fin: horaFin
      })
      .first();

    if (existing) {
      await trx('cargas_horarias').where({ id: existing.id }).update({ tipo });
      return { id: existing.id, inserted: false };
    }

    const [insertId] = await trx('cargas_horarias').insert({
      cargo_id: cargoId,
      dia_semana: dia,
      hora_inicio: horaInicio,
      hora_fin: horaFin,
      tipo
    });

    await trx.raw(
      `INSERT INTO horarios_materia (curso_id, materia_id, dia_semana, hora_inicio, hora_fin)
       SELECT cmc.curso_id, cm.materia_id, ?, ?, ?
       FROM cargo_materias cm
       JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
       WHERE cm.cargo_id = ?`,
      [dia, horaInicio, horaFin, cargoId]
    );

    return { id: Number(insertId), inserted: true };
  },

  async clearRelations(cargoId, { trx } = {}) {
    const db = resolveDb(trx);

    await db.raw(
      `DELETE cmch
       FROM cargo_materia_curso_horarios cmch
       JOIN cargo_materia_curso cmc ON cmch.cmc_id = cmc.id
       JOIN cargo_materias cm ON cmc.cargo_materia_id = cm.id
       WHERE cm.cargo_id = ?`,
      [cargoId]
    );

    await db.raw(
      `DELETE cmc
       FROM cargo_materia_curso cmc
       JOIN cargo_materias cm ON cmc.cargo_materia_id = cm.id
       WHERE cm.cargo_id = ?`,
      [cargoId]
    );

    await db('cargo_materias').where('cargo_id', cargoId).del();
  },

  async insertCargoMateria({ cargoId, materiaId }, { trx } = {}) {
    const db = resolveDb(trx);
    const [id] = await db('cargo_materias').insert({ cargo_id: cargoId, materia_id: materiaId });
    return Number(id);
  },

  async insertCargoMateriaCurso({ cargoMateriaId, cursoId }, { trx } = {}) {
    const db = resolveDb(trx);
    const [id] = await db('cargo_materia_curso').insert({ cargo_materia_id: cargoMateriaId, curso_id: cursoId });
    return Number(id);
  },

  async insertCargoMateriaCursoHorario({ cargoMateriaCursoId, horarioId }, { trx } = {}) {
    const db = resolveDb(trx);
    const [id] = await db('cargo_materia_curso_horarios').insert({ cmc_id: cargoMateriaCursoId, horario_id: horarioId });
    return Number(id);
  },

  async insertHorarioMateriaFromSchedule({ cursoId, materiaId, horarioId }, { trx } = {}) {
    const db = resolveDb(trx);

    await db.raw(
      `INSERT INTO horarios_materia (curso_id, materia_id, dia_semana, hora_inicio, hora_fin)
       SELECT ?, ?, ch.dia_semana, ch.hora_inicio, ch.hora_fin
       FROM cargas_horarias ch
       WHERE ch.id = ?`,
      [cursoId, materiaId, horarioId]
    );
  },

  async listRelations(cargoId, { trx } = {}) {
    const db = resolveDb(trx);

    return db('cargo_materias as cm')
      .leftJoin('cargo_materia_curso as cmc', 'cm.id', 'cmc.cargo_materia_id')
      .leftJoin('cargo_materia_curso_horarios as cmch', 'cmc.id', 'cmch.cmc_id')
      .select('cm.materia_id', 'cmc.curso_id', 'cmch.horario_id')
      .where('cm.cargo_id', cargoId)
      .orderBy('cm.materia_id')
      .orderBy('cmc.curso_id');
  },

  async listRelationsDetailed(cargoId, { trx } = {}) {
    const db = resolveDb(trx);

    return db('cargo_materias as cm')
      .leftJoin('cargo_materia_curso as cmc', 'cm.id', 'cmc.cargo_materia_id')
      .leftJoin('cargo_materia_curso_horarios as cmch', 'cmc.id', 'cmch.cmc_id')
      .select(
        'cm.id as cargo_materia_id',
        'cm.materia_id',
        'cmc.id as cargo_materia_curso_id',
        'cmc.curso_id',
        'cmch.horario_id'
      )
      .where('cm.cargo_id', cargoId)
      .orderBy('cm.materia_id')
      .orderBy('cmc.curso_id');
  },

  async listRelationsForSchedule(scheduleId, { trx } = {}) {
    const db = resolveDb(trx);

    return db('cargo_materia_curso_horarios as cmch')
      .join('cargo_materia_curso as cmc', 'cmch.cmc_id', 'cmc.id')
      .join('cargo_materias as cm', 'cmc.cargo_materia_id', 'cm.id')
      .select('cmc.curso_id', 'cm.materia_id')
      .where('cmch.horario_id', scheduleId);
  },

  async deleteScheduleLinks(cargoId, { trx } = {}) {
    const db = resolveDb(trx);
    const scheduleIdsQuery = db('cargas_horarias').select('id').where('cargo_id', cargoId);

    await db('cargo_materia_curso_horarios').whereIn('horario_id', scheduleIdsQuery).del();
  },

  async deleteSchedules(cargoId, { trx } = {}) {
    const db = resolveDb(trx);
    await db('cargas_horarias').where('cargo_id', cargoId).del();
  },

  async deleteScheduleLinksByScheduleId(scheduleId, { trx } = {}) {
    const db = resolveDb(trx);
    await db('cargo_materia_curso_horarios').where('horario_id', scheduleId).del();
  },

  async deleteScheduleById(scheduleId, { trx } = {}) {
    const db = resolveDb(trx);
    await db('cargas_horarias').where('id', scheduleId).del();
  },

  async deleteHorarioMateriaEntry({ cursoId, materiaId, dia, horaInicio, horaFin }, { trx } = {}) {
    const db = resolveDb(trx);

    await db('horarios_materia')
      .where({
        curso_id: cursoId,
        materia_id: materiaId,
        dia_semana: dia,
        hora_inicio: horaInicio,
        hora_fin: horaFin
      })
      .del();
  },

  async listTypes() {
    const db = getDatabase();

    return db('cargos_max_horas').select('id', 'nombre').orderBy('nombre');
  }
};
