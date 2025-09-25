import { randomUUID } from 'crypto';
import httpStatus from 'http-status';
import { cargoRepository } from '../repositories/cargoRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { getDatabase } from '../config/database.js';

const normalizeSchedule = (schedule) => ({
  id: Number(schedule.id),
  cargo_id: Number(schedule.cargo_id),
  dia_semana: schedule.dia_semana,
  hora_inicio: schedule.hora_inicio,
  hora_fin: schedule.hora_fin,
  tipo: schedule.tipo
});

const normalizeRelations = (relations) => {
  const materiaMap = new Map();

  for (const relation of relations || []) {
    const materiaId = Number(relation?.materia_id);
    if (!Number.isInteger(materiaId) || materiaId <= 0) {
      continue;
    }

    if (!materiaMap.has(materiaId)) {
      materiaMap.set(materiaId, new Map());
    }

    const cursoId = Number(relation?.curso_id);
    if (!Number.isInteger(cursoId) || cursoId <= 0) {
      // Aún queremos exponer la materia aunque no tenga cursos asociados
      // pero si el curso es inválido seguimos con la siguiente fila.
      continue;
    }

    const cursosMap = materiaMap.get(materiaId);
    if (!cursosMap.has(cursoId)) {
      cursosMap.set(cursoId, new Set());
    }

    const horarioId = Number(relation?.horario_id);
    if (Number.isInteger(horarioId) && horarioId > 0) {
      cursosMap.get(cursoId).add(horarioId);
    }
  }

  return Array.from(materiaMap.entries()).map(([materiaId, cursosMap]) => ({
    materia_id: materiaId,
    cursos: Array.from(cursosMap.entries()).map(([cursoId, horariosSet]) => ({
      curso_id: cursoId,
      horarios: Array.from(horariosSet.values())
    }))
  }));
};

const formatRange = (inicio, fin) => {
  const safeStart = inicio ? inicio.slice(0, 5) : '';
  const safeEnd = fin ? fin.slice(0, 5) : '';
  return `${safeStart}-${safeEnd}`;
};

const normalizeScheduleEntries = (entries) => {
  if (!Array.isArray(entries)) {
    return [];
  }

  return entries
    .map((entry) => {
      const rawDays = Array.isArray(entry?.dias) ? entry.dias : [];
      const uniqueDays = Array.from(
        new Set(rawDays.map((dia) => String(dia).trim()).filter((dia) => dia.length > 0))
      );

      const horaInicio = entry?.hora_inicio ?? entry?.horaInicio;
      const horaFin = entry?.hora_fin ?? entry?.horaFin;
      const tipo = entry?.tipo ?? 'clase';

      if (!uniqueDays.length || !horaInicio || !horaFin) {
        return null;
      }

      const normalizedTipo = String(tipo ?? 'clase').trim();

      return {
        dias: uniqueDays,
        horaInicio: String(horaInicio),
        horaFin: String(horaFin),
        tipo: normalizedTipo.length ? normalizedTipo : 'clase'
      };
    })
    .filter(Boolean);
};

const toNullIfEmpty = (value) => {
  if (value === undefined || value === null) {
    return null;
  }

  if (typeof value === 'string' && value.trim() === '') {
    return null;
  }

  return value;
};

const toTrimmedOrNull = (value) => {
  const normalized = toNullIfEmpty(value);
  if (normalized === null) {
    return null;
  }

  return String(normalized).trim();
};

const normalizeCargoPayload = ({
  codigo,
  tipoCargoId,
  tipo,
  estado,
  observaciones,
  fechaInicio,
  fechaFin,
  docenteId
}) => {
  const normalizedDocente = toNullIfEmpty(docenteId);

  return {
    codigo: String(codigo ?? '').trim(),
    tipoCargoId: Number(tipoCargoId),
    tipo: String(tipo ?? '').trim(),
    estado: String(estado ?? 'activo').trim(),
    observaciones: toTrimmedOrNull(observaciones),
    fechaInicio: toTrimmedOrNull(fechaInicio),
    fechaFin: toTrimmedOrNull(fechaFin),
    docenteId: normalizedDocente === null ? null : Number(normalizedDocente)
  };
};

const mapCargoRecord = (cargo) => ({
  id: Number(cargo.id),
  codigo_cargo: cargo.codigo_cargo,
  tipo_cargo_id:
    cargo.tipo_cargo_id === null || cargo.tipo_cargo_id === undefined
      ? null
      : Number(cargo.tipo_cargo_id),
  tipo: cargo.tipo,
  estado: cargo.estado,
  observaciones: cargo.observaciones,
  fecha_inicio: cargo.fecha_inicio,
  fecha_fin: cargo.fecha_fin,
  docente_id:
    cargo.docente_id === null || cargo.docente_id === undefined
      ? null
      : Number(cargo.docente_id)
});

const generateAutoCode = () => `AUTO_${randomUUID().replace(/-/g, '').slice(0, 18)}`;

const copyPedagogicalData = async (trx, sourceCargoId, targetCargoId) => {
  await trx.raw(
    `INSERT INTO cargo_materias (cargo_id, materia_id)
     SELECT ?, materia_id
     FROM cargo_materias
     WHERE cargo_id = ?`,
    [targetCargoId, sourceCargoId]
  );

  await trx.raw(
    `INSERT INTO cargo_materia_curso (cargo_materia_id, curso_id)
     SELECT cm2.id, cmc.curso_id
     FROM cargo_materia_curso cmc
     JOIN cargo_materias cm ON cmc.cargo_materia_id = cm.id
     JOIN cargo_materias cm2 ON cm2.materia_id = cm.materia_id AND cm2.cargo_id = ?
     WHERE cm.cargo_id = ?`,
    [targetCargoId, sourceCargoId]
  );

  await trx.raw(
    `INSERT INTO cargas_horarias (cargo_id, dia_semana, hora_inicio, hora_fin, tipo)
     SELECT ?, dia_semana, hora_inicio, hora_fin, tipo
     FROM cargas_horarias
     WHERE cargo_id = ?`,
    [targetCargoId, sourceCargoId]
  );

  await trx.raw(
    `INSERT INTO cargo_materia_curso_horarios (cmc_id, horario_id)
     SELECT cmc2.id, cmch.horario_id
     FROM cargo_materia_curso_horarios cmch
     JOIN cargo_materia_curso cmc ON cmch.cmc_id = cmc.id
     JOIN cargo_materia_curso cmc2
       ON cmc2.curso_id = cmc.curso_id
      AND cmc2.cargo_materia_id IN (
        SELECT id FROM cargo_materias
        WHERE materia_id = (
          SELECT materia_id FROM cargo_materias WHERE id = cmc.cargo_materia_id LIMIT 1
        )
        AND cargo_id = ?
      )
     WHERE cmc.cargo_id = ?`,
    [targetCargoId, sourceCargoId]
  );
};

const createReplacementCargo = async (
  trx,
  { cargoId, tipoCargoId, nuevoTipo, fechaInicio, fechaFin }
) => {
  const replacementId = await cargoRepository.create(
    {
      codigo_cargo: generateAutoCode(),
      tipo_cargo_id: tipoCargoId,
      tipo: nuevoTipo,
      estado: 'activo',
      observaciones: null,
      fecha_inicio: fechaInicio ?? null,
      fecha_fin: fechaFin ?? null,
      docente_id: null
    },
    { trx }
  );

  await cargoRepository.insertReplacement({ cargoId, replacementId }, { trx });
  await copyPedagogicalData(trx, cargoId, replacementId);

  return replacementId;
};

const uniquePositiveIntegers = (values) => {
  const set = new Set();
  for (const value of values || []) {
    const parsed = Number(value);
    if (Number.isInteger(parsed) && parsed > 0) {
      set.add(parsed);
    }
  }

  return Array.from(set.values());
};

const normalizeRelationsPayload = (materias) => {
  if (!Array.isArray(materias)) {
    return [];
  }

  return materias
    .map((materia) => {
      const materiaId = Number(materia?.id ?? materia?.materia_id);
      if (!Number.isInteger(materiaId) || materiaId <= 0) {
        return null;
      }

      const cursos = Array.isArray(materia?.cursos)
        ? materia.cursos
            .map((curso) => {
              const cursoId = Number(curso?.id ?? curso?.curso_id);
              if (!Number.isInteger(cursoId) || cursoId <= 0) {
                return null;
              }

              return {
                curso_id: cursoId,
                horarios: uniquePositiveIntegers(curso?.horarios)
              };
            })
            .filter(Boolean)
        : [];

      return {
        materia_id: materiaId,
        cursos
      };
    })
    .filter(Boolean);
};

export const cargoService = {
  async getCargo(cargoId) {
    const cargo = await cargoRepository.findById(cargoId);
    if (!cargo) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    const schedules = await cargoRepository.listSchedulesForCargo(cargoId);
    const relations = await cargoRepository.listRelations(cargoId);

    return {
      ...mapCargoRecord(cargo),
      horarios: schedules.map((schedule) => normalizeSchedule(schedule)),
      relaciones: normalizeRelations(relations)
    };
  },

  async listCargoTypes() {
    const types = await cargoRepository.listTypes();

    return types.map((type) => ({
      id: Number(type.id),
      nombre: type.nombre
    }));
  },

  async createCargo(payload) {
    const normalized = normalizeCargoPayload(payload);

    const cargoId = await getDatabase().transaction(async (trx) =>
      cargoRepository.create(
        {
          codigo_cargo: normalized.codigo,
          tipo_cargo_id: normalized.tipoCargoId,
          tipo: normalized.tipo,
          estado: normalized.estado,
          observaciones: normalized.observaciones,
          fecha_inicio: normalized.fechaInicio,
          fecha_fin: normalized.fechaFin,
          docente_id: normalized.docenteId
        },
        { trx }
      )
    );

    const cargo = await cargoRepository.findById(cargoId);
    return mapCargoRecord(cargo);
  },

  async updateCargo(cargoId, payload) {
    return getDatabase().transaction(async (trx) => {
      const existing = await cargoRepository.findById(cargoId, { trx, forUpdate: true });
      if (!existing) {
        throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
      }

      const normalized = normalizeCargoPayload(payload);

      await cargoRepository.update(
        cargoId,
        {
          codigo_cargo: normalized.codigo,
          tipo_cargo_id: normalized.tipoCargoId,
          tipo: normalized.tipo,
          estado: normalized.estado,
          observaciones: normalized.observaciones,
          fecha_inicio: normalized.fechaInicio,
          fecha_fin: normalized.fechaFin,
          docente_id: normalized.docenteId
        },
        { trx }
      );

      const previousType = existing.tipo;
      const previousEstado = existing.estado;
      const newEstado = normalized.estado;

      if (previousType === 'titular' && newEstado === 'licencia') {
        await createReplacementCargo(trx, {
          cargoId: Number(cargoId),
          tipoCargoId: normalized.tipoCargoId,
          nuevoTipo: 'suplente',
          fechaInicio: normalized.fechaInicio,
          fechaFin: normalized.fechaFin
        });
      } else if (previousType === 'titular' && newEstado === 'renuncia') {
        await createReplacementCargo(trx, {
          cargoId: Number(cargoId),
          tipoCargoId: normalized.tipoCargoId,
          nuevoTipo: 'interino',
          fechaInicio: normalized.fechaInicio,
          fechaFin: normalized.fechaFin
        });
      } else if (previousType === 'suplente' && newEstado === 'renuncia') {
        await createReplacementCargo(trx, {
          cargoId: Number(cargoId),
          tipoCargoId: normalized.tipoCargoId,
          nuevoTipo: 'suplente',
          fechaInicio: normalized.fechaInicio,
          fechaFin: normalized.fechaFin
        });
      } else if (previousType === 'interino' && newEstado === 'renuncia') {
        await createReplacementCargo(trx, {
          cargoId: Number(cargoId),
          tipoCargoId: normalized.tipoCargoId,
          nuevoTipo: 'interino',
          fechaInicio: normalized.fechaInicio,
          fechaFin: normalized.fechaFin
        });
      } else if (previousType === 'titular' && previousEstado !== 'activo' && newEstado === 'activo') {
        const replacements = await cargoRepository.listReplacementIds(cargoId, { trx });
        for (const replacement of replacements) {
          const replacementId = Number(replacement.cargo_reemplazo_id);
          if (!replacementId) {
            continue;
          }

          await cargoRepository.detachReplacementByReplacementId(replacementId, { trx });
          await cargoRepository.deleteById(replacementId, { trx });
        }
      } else if (previousType === 'interino' && newEstado === 'desplazado') {
        await createReplacementCargo(trx, {
          cargoId: Number(cargoId),
          tipoCargoId: normalized.tipoCargoId,
          nuevoTipo: 'titular',
          fechaInicio: normalized.fechaInicio,
          fechaFin: normalized.fechaFin
        });

        const today = new Date().toISOString().slice(0, 10);
        await cargoRepository.update(
          cargoId,
          {
            estado: 'inactivo',
            fecha_fin: today
          },
          { trx }
        );
      }

      const updatedCargo = await cargoRepository.findById(cargoId, { trx });
      return mapCargoRecord(updatedCargo);
    });
  },

  async deactivateCargo(cargoId) {
    const existing = await cargoRepository.findById(cargoId);
    if (!existing) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    await cargoRepository.softDelete(cargoId);
  },

  async reactivateCargo(cargoId) {
    const existing = await cargoRepository.findById(cargoId);
    if (!existing) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    await cargoRepository.reactivate(cargoId);
    const cargo = await cargoRepository.findById(cargoId);
    return mapCargoRecord(cargo);
  },

  async listCargos() {
    const cargos = await cargoRepository.listAll();
    const schedules = await cargoRepository.listAllSchedules();

    const scheduleMap = schedules.reduce((acc, schedule) => {
      const cargoId = Number(schedule.cargo_id);
      if (!acc[cargoId]) {
        acc[cargoId] = {};
      }

      const day = schedule.dia_semana;
      const range = formatRange(schedule.hora_inicio, schedule.hora_fin);

      if (!acc[cargoId][day]) {
        acc[cargoId][day] = [];
      }

      if (!acc[cargoId][day].includes(range)) {
        acc[cargoId][day].push(range);
      }

      return acc;
    }, {});

    return cargos.map((cargo) => ({
      id: Number(cargo.id),
      tipo_cargo_nombre: cargo.tipo_cargo_nombre ?? null,
      codigo_cargo: cargo.codigo_cargo,
      situacion: cargo.situacion,
      estado: cargo.estado,
      fecha_inicio: cargo.fecha_inicio,
      fecha_fin: cargo.fecha_fin,
      observaciones: cargo.observaciones,
      docente_nombre: cargo.docente_nombre,
      docente_apellido: cargo.docente_apellido,
      materia_nombre: cargo.materia_nombre ?? '',
      horarios: scheduleMap[Number(cargo.id)] ?? {}
    }));
  },

  async listSchedules(cargoId) {
    const cargo = await cargoRepository.findById(cargoId);
    if (!cargo) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    const schedules = await cargoRepository.listSchedulesForCargo(cargoId);
    return schedules.map((schedule) => normalizeSchedule(schedule));
  },

  async addSchedules(cargoId, { dias, horaInicio, horaFin, tipo }) {
    const cargo = await cargoRepository.findById(cargoId);
    if (!cargo) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    const normalizedEntries = normalizeScheduleEntries([
      { dias, hora_inicio: horaInicio, hora_fin: horaFin, tipo }
    ]);

    if (!normalizedEntries.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debés indicar al menos un día para el horario');
    }

    const db = getDatabase();

    await db.transaction(async (trx) => {
      const entry = normalizedEntries[0];
      for (const dia of entry.dias) {
        await cargoRepository.upsertSchedule({
          trx,
          cargoId,
          dia,
          horaInicio: entry.horaInicio,
          horaFin: entry.horaFin,
          tipo: entry.tipo ?? 'clase'
        });
      }
    });

    const schedules = await cargoRepository.listSchedulesForCargo(cargoId);
    return schedules.map((schedule) => normalizeSchedule(schedule));
  },

  async replaceSchedules(cargoId, horarios) {
    const cargo = await cargoRepository.findById(cargoId);
    if (!cargo) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    const normalizedSchedules = normalizeScheduleEntries(horarios);
    if (!normalizedSchedules.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debés indicar al menos un horario válido');
    }

    const db = getDatabase();

    const updatedSchedules = await db.transaction(async (trx) => {
      const existingSchedules = await cargoRepository.listSchedulesForCargo(cargoId, { trx });
      const scheduleById = new Map(
        existingSchedules.map((schedule) => [Number(schedule.id), normalizeSchedule(schedule)])
      );

      const relations = await cargoRepository.listRelationsDetailed(cargoId, { trx });

      const horarioMateriaDeletions = new Map();
      for (const relation of relations) {
        const schedule = scheduleById.get(Number(relation?.horario_id));
        const cursoId = Number(relation?.curso_id);
        const materiaId = Number(relation?.materia_id);

        if (!schedule || !cursoId || !materiaId) {
          continue;
        }

        const deletionKey = `${cursoId}|${materiaId}|${schedule.dia_semana}|${schedule.hora_inicio}|${schedule.hora_fin}`;
        if (!horarioMateriaDeletions.has(deletionKey)) {
          horarioMateriaDeletions.set(deletionKey, {
            cursoId,
            materiaId,
            dia: schedule.dia_semana,
            horaInicio: schedule.hora_inicio,
            horaFin: schedule.hora_fin
          });
        }
      }

      for (const deletion of horarioMateriaDeletions.values()) {
        await cargoRepository.deleteHorarioMateriaEntry(deletion, { trx });
      }

      await cargoRepository.deleteScheduleLinks(cargoId, { trx });
      await cargoRepository.deleteSchedules(cargoId, { trx });

      const insertedSchedules = new Map();

      for (const schedule of normalizedSchedules) {
        for (const dia of schedule.dias) {
          const key = `${dia}|${schedule.horaInicio}|${schedule.horaFin}|${schedule.tipo}`;
          if (insertedSchedules.has(key)) {
            continue;
          }

          const scheduleId = await cargoRepository.insertSchedule(
            {
              cargoId,
              dia,
              horaInicio: schedule.horaInicio,
              horaFin: schedule.horaFin,
              tipo: schedule.tipo ?? 'clase'
            },
            { trx }
          );

          insertedSchedules.set(key, scheduleId);
        }
      }

      for (const relation of relations) {
        const schedule = scheduleById.get(Number(relation?.horario_id));
        const cargoMateriaCursoId = Number(relation?.cargo_materia_curso_id);
        const cursoId = Number(relation?.curso_id);
        const materiaId = Number(relation?.materia_id);

        if (!schedule || !cargoMateriaCursoId || !cursoId || !materiaId) {
          continue;
        }

        const key = `${schedule.dia_semana}|${schedule.hora_inicio}|${schedule.hora_fin}|${schedule.tipo}`;
        const newScheduleId = insertedSchedules.get(key);
        if (!newScheduleId) {
          continue;
        }

        await cargoRepository.insertCargoMateriaCursoHorario(
          { cargoMateriaCursoId, horarioId: newScheduleId },
          { trx }
        );

        await cargoRepository.insertHorarioMateriaFromSchedule(
          { cursoId, materiaId, horarioId: newScheduleId },
          { trx }
        );
      }

      const schedules = await cargoRepository.listSchedulesForCargo(cargoId, { trx });
      return schedules.map((schedule) => normalizeSchedule(schedule));
    });

    return updatedSchedules;
  },

  async deleteSchedule(cargoId, scheduleId) {
    const cargo = await cargoRepository.findById(cargoId);
    if (!cargo) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    const schedule = await cargoRepository.findScheduleById(scheduleId);
    if (!schedule || Number(schedule.cargo_id) !== Number(cargoId)) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Horario no encontrado');
    }

    const db = getDatabase();

    await db.transaction(async (trx) => {
      const relations = await cargoRepository.listRelationsForSchedule(scheduleId, { trx });

      for (const relation of relations) {
        const cursoId = Number(relation?.curso_id);
        const materiaId = Number(relation?.materia_id);

        if (!cursoId || !materiaId) {
          continue;
        }

        await cargoRepository.deleteHorarioMateriaEntry(
          {
            cursoId,
            materiaId,
            dia: schedule.dia_semana,
            horaInicio: schedule.hora_inicio,
            horaFin: schedule.hora_fin
          },
          { trx }
        );
      }

      await cargoRepository.deleteScheduleLinksByScheduleId(scheduleId, { trx });
      await cargoRepository.deleteScheduleById(scheduleId, { trx });
    });

    return true;
  },
  async replaceRelations(cargoId, materias) {
    const cargo = await cargoRepository.findById(cargoId);
    if (!cargo) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Cargo no encontrado');
    }

    if (!Array.isArray(materias)) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debés indicar al menos una materia válida');
    }

    const normalizedMaterias = normalizeRelationsPayload(materias);
    if (!normalizedMaterias.length && materias.length > 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debés indicar al menos una materia válida');
    }

    const db = getDatabase();

    await db.transaction(async (trx) => {
      await cargoRepository.clearRelations(cargoId, { trx });

      if (!normalizedMaterias.length) {
        return;
      }

      for (const materia of normalizedMaterias) {
        const cargoMateriaId = await cargoRepository.insertCargoMateria(
          { cargoId, materiaId: materia.materia_id },
          { trx }
        );

        for (const curso of materia.cursos) {
          const cargoMateriaCursoId = await cargoRepository.insertCargoMateriaCurso(
            { cargoMateriaId, cursoId: curso.curso_id },
            { trx }
          );

          for (const horarioId of curso.horarios) {
            await cargoRepository.insertCargoMateriaCursoHorario(
              { cargoMateriaCursoId, horarioId },
              { trx }
            );

            await cargoRepository.insertHorarioMateriaFromSchedule(
              { cursoId: curso.curso_id, materiaId: materia.materia_id, horarioId },
              { trx }
            );
          }
        }
      }
    });

    return { materias: normalizedMaterias };
  }
};
