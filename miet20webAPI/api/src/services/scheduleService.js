import httpStatus from 'http-status';
import { scheduleRepository } from '../repositories/scheduleRepository.js';
import { courseRepository } from '../repositories/courseRepository.js';
import { ApiError } from '../utils/ApiError.js';

const buildAllowedDaysMap = () => {
  const entries = [
    ['Lunes', ['lunes']],
    ['Martes', ['martes']],
    ['Miércoles', ['miercoles', 'miércoles']],
    ['Jueves', ['jueves']],
    ['Viernes', ['viernes']],
    ['Sábado', ['sabado', 'sábado']],
    ['Domingo', ['domingo']]
  ];

  const map = new Map();
  const normalizeKey = (value) =>
    value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();

  entries.forEach(([label, keys]) => {
    keys.forEach((key) => {
      map.set(normalizeKey(key), label);
    });
  });

  return map;
};

const allowedDays = buildAllowedDaysMap();

const sanitizeDayOfWeek = (value) => {
  if (typeof value !== 'string') {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El día es obligatorio');
  }

  const normalized = value.trim();
  if (!normalized) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El día es obligatorio');
  }

  const key = normalized
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();

  if (!allowedDays.has(key)) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El día seleccionado es inválido');
  }

  return allowedDays.get(key);
};

const sanitizeTime = (value, fieldName) => {
  if (typeof value !== 'string') {
    throw new ApiError(httpStatus.BAD_REQUEST, `La ${fieldName} es obligatoria`);
  }

  const trimmed = value.trim();
  if (!/^\d{2}:\d{2}(:\d{2})?$/.test(trimmed)) {
    throw new ApiError(httpStatus.BAD_REQUEST, `La ${fieldName} es inválida`);
  }

  const [hours, minutes, seconds = '00'] = trimmed.split(':');
  const h = Number(hours);
  const m = Number(minutes);
  const s = Number(seconds);

  if ([h, m, s].some((unit) => Number.isNaN(unit))) {
    throw new ApiError(httpStatus.BAD_REQUEST, `La ${fieldName} es inválida`);
  }

  if (h < 0 || h > 23 || m < 0 || m > 59 || s < 0 || s > 59) {
    throw new ApiError(httpStatus.BAD_REQUEST, `La ${fieldName} está fuera de rango`);
  }

  return [h, m, s].map((unit) => String(unit).padStart(2, '0')).join(':');
};

const sanitizeAula = (value) => {
  if (value === undefined || value === null) {
    return null;
  }

  if (typeof value !== 'string') {
    return null;
  }

  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }

  return trimmed.slice(0, 100);
};

const parseContraturno = (value, fallback = 0) => {
  if (value === undefined || value === null || value === '') {
    return Number(fallback) === 1 ? 1 : 0;
  }

  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    if (['1', 'true', 'si', 'sí'].includes(normalized)) {
      return 1;
    }
    if (['0', 'false', 'no'].includes(normalized)) {
      return 0;
    }
  }

  return Number(value) === 1 ? 1 : 0;
};

const ensureEndAfterStart = (start, end) => {
  const toSeconds = (time) => {
    const [h, m, s] = time.split(':').map(Number);
    return h * 3600 + m * 60 + s;
  };

  if (toSeconds(end) <= toSeconds(start)) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'La hora de fin debe ser posterior a la de inicio');
  }
};

const parseRequiredInt = (value, message) => {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new ApiError(httpStatus.BAD_REQUEST, message);
  }
  return parsed;
};

const parseOptionalInt = (value, message) => {
  if (value === undefined || value === null || value === '') {
    return null;
  }

  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new ApiError(httpStatus.BAD_REQUEST, message);
  }

  return parsed;
};

const sanitizeScheduleFields = (payload, contraturnoFallback = 0) => {
  const dia_semana = sanitizeDayOfWeek(payload.dia_semana);
  const hora_inicio = sanitizeTime(payload.hora_inicio, 'hora de inicio');
  const hora_fin = sanitizeTime(payload.hora_fin, 'hora de fin');

  ensureEndAfterStart(hora_inicio, hora_fin);

  return {
    dia_semana,
    hora_inicio,
    hora_fin,
    aula: sanitizeAula(payload.aula),
    es_contraturno: parseContraturno(payload.es_contraturno, contraturnoFallback)
  };
};

const normalizeSchedule = (schedule) => {
  if (!schedule) {
    return schedule;
  }

  return {
    ...schedule,
    id: Number(schedule.id),
    curso_id: Number(schedule.curso_id),
    materia_id: Number(schedule.materia_id),
    profesor_id:
      schedule.profesor_id === null || schedule.profesor_id === undefined
        ? null
        : Number(schedule.profesor_id),
    es_contraturno: Number(schedule.es_contraturno ?? 0),
    hora_inicio: schedule.hora_inicio ? schedule.hora_inicio.substring(0, 8) : null,
    hora_fin: schedule.hora_fin ? schedule.hora_fin.substring(0, 8) : null
  };
};

const ensureAssignmentExists = async (assignmentId) => {
  const assignment = await courseRepository.findAssignmentById(assignmentId);
  if (!assignment) {
    throw new ApiError(httpStatus.NOT_FOUND, 'Asignación no encontrada');
  }

  if (assignment.estado && assignment.estado !== 'activo') {
    throw new ApiError(httpStatus.BAD_REQUEST, 'La asignación no se encuentra activa');
  }

  return assignment;
};

const buildScheduleFromAssignment = (assignment, payload) => {
  const scheduleFields = sanitizeScheduleFields(payload, assignment.es_contraturno);

  return {
    ...scheduleFields,
    curso_id: Number(assignment.curso_id),
    materia_id: Number(assignment.materia_id),
    profesor_id:
      assignment.profesor_id === null || assignment.profesor_id === undefined
        ? null
        : Number(assignment.profesor_id)
  };
};

export const scheduleService = {
  async createSchedule(payload) {
    const scheduleFields = sanitizeScheduleFields(payload);
    const scheduleToCreate = {
      ...scheduleFields,
      curso_id: parseRequiredInt(payload.curso_id, 'El curso es inválido'),
      materia_id: parseOptionalInt(payload.materia_id, 'La materia es inválida'),
      profesor_id: parseOptionalInt(payload.profesor_id, 'El profesor es inválido')
    };

    const created = await scheduleRepository.create(scheduleToCreate);
    return normalizeSchedule(created);
  },

  async createScheduleForAssignment(assignmentId, payload) {
    const assignment = await ensureAssignmentExists(assignmentId);
    const scheduleToCreate = buildScheduleFromAssignment(assignment, payload);
    const created = await scheduleRepository.create(scheduleToCreate);
    return normalizeSchedule(created);
  },

  async listByCourse(courseId) {
    const schedules = await scheduleRepository.listByCourse(courseId);
    return schedules.map((schedule) => normalizeSchedule(schedule));
  },

  async listByAssignment(assignmentId) {
    const assignment = await ensureAssignmentExists(assignmentId);
    const schedules = await scheduleRepository.listByAssignment({
      curso_id: Number(assignment.curso_id),
      materia_id: Number(assignment.materia_id),
      profesor_id:
        assignment.profesor_id === null || assignment.profesor_id === undefined
          ? null
          : Number(assignment.profesor_id)
    });
    return schedules.map((schedule) => normalizeSchedule(schedule));
  },

  async listTeacherAssignments(teacherId) {
    const assignments = await scheduleRepository.listAssignmentsByTeacher(teacherId);
    return assignments.map((assignment) => ({
      ...assignment,
      horario_id: Number(assignment.horario_id),
      curso_id: Number(assignment.curso_id),
      materia_id: assignment.materia_id === null ? null : Number(assignment.materia_id),
      es_contraturno: Number(assignment.es_contraturno ?? 0)
    }));
  },

  async updateSchedule(id, updates) {
    const sanitizedUpdates = { ...updates };

    if (Object.prototype.hasOwnProperty.call(updates, 'dia_semana')) {
      sanitizedUpdates.dia_semana = sanitizeDayOfWeek(updates.dia_semana);
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'hora_inicio')) {
      sanitizedUpdates.hora_inicio = sanitizeTime(updates.hora_inicio, 'hora de inicio');
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'hora_fin')) {
      sanitizedUpdates.hora_fin = sanitizeTime(updates.hora_fin, 'hora de fin');
    }

    if (sanitizedUpdates.hora_inicio && sanitizedUpdates.hora_fin) {
      ensureEndAfterStart(sanitizedUpdates.hora_inicio, sanitizedUpdates.hora_fin);
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'aula')) {
      sanitizedUpdates.aula = sanitizeAula(updates.aula);
    }

    if (Object.prototype.hasOwnProperty.call(updates, 'es_contraturno')) {
      sanitizedUpdates.es_contraturno = parseContraturno(updates.es_contraturno);
    }

    const updated = await scheduleRepository.update(id, sanitizedUpdates);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Horario no encontrado');
    }
    return normalizeSchedule(updated);
  },

  async deleteSchedule(id) {
    const deleted = await scheduleRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Horario no encontrado');
    }
    return true;
  }
};
