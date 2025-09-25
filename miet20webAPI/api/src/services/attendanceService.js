import httpStatus from 'http-status';
import { attendanceRepository } from '../repositories/attendanceRepository.js';
import { scheduleRepository } from '../repositories/scheduleRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { dayNumberToName, mapDayValueToNumber } from '../utils/dayOfWeek.js';

const formatTwoDigits = (value) => value.toString().padStart(2, '0');

const parseISODate = (value) => {
  if (!value || typeof value !== 'string') {
    return null;
  }
  const parts = value.split('-').map((segment) => Number.parseInt(segment, 10));
  if (parts.length !== 3 || parts.some((part) => Number.isNaN(part))) {
    return null;
  }
  const [year, month, day] = parts;
  return new Date(Date.UTC(year, month - 1, day));
};

const toUTCMidnight = (date) => new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));

const formatISO = (date) => `${date.getUTCFullYear()}-${formatTwoDigits(date.getUTCMonth() + 1)}-${formatTwoDigits(date.getUTCDate())}`;

const formatDisplayDate = (date) => `${formatTwoDigits(date.getUTCDate())}-${formatTwoDigits(date.getUTCMonth() + 1)}-${date.getUTCFullYear()}`;

const getWeekDates = (baseDate) => {
  const reference = baseDate ? parseISODate(baseDate) : toUTCMidnight(new Date());
  if (!reference || Number.isNaN(reference.getTime())) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'La fecha indicada es inválida');
  }

  const referenceUtc = toUTCMidnight(reference);
  const dayOfWeek = referenceUtc.getUTCDay(); // 0 = domingo ... 6 = sábado
  const distanceFromMonday = (dayOfWeek + 6) % 7;
  const monday = new Date(referenceUtc);
  monday.setUTCDate(referenceUtc.getUTCDate() - distanceFromMonday);

  return Array.from({ length: 5 }, (_, index) => {
    const date = new Date(monday);
    date.setUTCDate(monday.getUTCDate() + index);
    return date;
  });
};

const dayOfWeekFromDate = (date) => ((date.getUTCDay() + 6) % 7) + 1; // 1 = lunes

const resolveReferenceDate = (input) => {
  if (!input) {
    return toUTCMidnight(new Date());
  }

  const parsed = parseISODate(input);
  if (!parsed || Number.isNaN(parsed.getTime())) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'La fecha indicada es inválida');
  }

  return toUTCMidnight(parsed);
};

const parseHeaderDate = (header, fallbackYear) => {
  if (!header) {
    return null;
  }

  const trimmed = header.toString().trim();

  let match = trimmed.match(/^(\d{2})-(\d{2})-(\d{4})$/);
  if (match) {
    const [, dd, mm, yyyy] = match;
    return `${yyyy}-${mm}-${dd}`;
  }

  match = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (match) {
    return trimmed;
  }

  match = trimmed.match(/^(\d{2})\/(\d{2})$/);
  if (match) {
    const [, dd, mm] = match;
    const year = fallbackYear ?? new Date().getUTCFullYear();
    return `${year}-${mm}-${dd}`;
  }

  return null;
};

const normalizeState = (value) => {
  if (value === null || value === undefined) {
    return 'NC';
  }

  const normalized = value.toString().trim().toUpperCase();
  const mapping = {
    PRESENTE: 'P',
    P: 'P',
    AUSENTE: 'A',
    A: 'A',
    TARDE: 'T',
    T: 'T',
    NC: 'NC',
    'N/C': 'NC',
    AP: 'AJ',
    AJ: 'AJ',
    JUSTIFICADA: 'AJ',
    JUST: 'AJ'
  };

  return mapping[normalized] ?? 'NC';
};

const ATTENDANCE_PRIORITY = {
  A: 5,
  AJ: 4,
  T: 3,
  P: 2,
  NC: 1
};

const pickPriorityState = (current, incoming) => {
  const currentPriority = ATTENDANCE_PRIORITY[current] ?? 0;
  const incomingPriority = ATTENDANCE_PRIORITY[incoming] ?? 0;
  return incomingPriority > currentPriority ? incoming : current;
};

const toUniquePositiveIntegers = (values) => {
  if (!Array.isArray(values)) {
    return [];
  }

  const result = new Set();
  values.forEach((value) => {
    const parsed = Number.parseInt(value, 10);
    if (!Number.isNaN(parsed) && parsed > 0) {
      result.add(parsed);
    }
  });
  return Array.from(result.values());
};

const createEmptySummary = () => ({ P: 0, A: 0, AJ: 0, T: 0, NC: 0, total: 0 });

const buildCourseSubjectsForDate = async ({ courseId, subjectIds, referenceDate }) => {
  const dayNumber = dayOfWeekFromDate(referenceDate);
  const isoDate = formatISO(referenceDate);
  const displayDate = formatDisplayDate(referenceDate);
  const subjectFilter = subjectIds && subjectIds.length ? subjectIds : [];
  const schedules = await scheduleRepository.listCourseSubjects(courseId, subjectFilter);

  const subjectInfo = new Map();
  const warnings = new Set();

  schedules.forEach((row) => {
    const subjectId = Number(row.materia_id);
    const rawDay = row.dia_semana;
    const mappedDay = mapDayValueToNumber(rawDay);
    const info = subjectInfo.get(subjectId) || {
      id: subjectId,
      nombre: row.materia_nombre,
      es_contraturno: Number(row.es_contraturno ?? 0) === 1 ? 1 : 0,
      dias: [],
      matched: false
    };

    info.dias.push({ raw: rawDay, mapped: mappedDay });
    if (mappedDay === null) {
      warnings.add(`Día inválido en horarios para ${row.materia_nombre} (${rawDay ?? 'sin valor'}).`);
    } else if (mappedDay === dayNumber) {
      info.matched = true;
    }

    subjectInfo.set(subjectId, info);
  });

  const subjectIdSet = subjectFilter.length ? new Set(subjectFilter) : null;
  const subjects = [];
  const matchedInfo = new Map();

  subjectInfo.forEach((info) => {
    if (info.matched) {
      const subject = {
        id: info.id,
        nombre: info.nombre,
        es_contraturno: info.es_contraturno,
        tiene_clase: true
      };
      subjects.push(subject);
      matchedInfo.set(info.id, subject);
    } else if (subjectIdSet && subjectIdSet.has(info.id)) {
      const raw = info.dias.find((day) => day.raw)?.raw ?? 'sin día configurado';
      warnings.add(`La materia ${info.nombre} no tiene clase el ${displayDate} (${raw}).`);
    }
  });

  subjects.sort(
    (a, b) =>
      Number(a.es_contraturno) - Number(b.es_contraturno) ||
      a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
  );

  return {
    isoDate,
    displayDate,
    dayNumber,
    subjects,
    warnings: Array.from(warnings),
    matchedInfo
  };
};

const buildStudentRow = (student, isoDates, attendanceMap) => {
  const row = [student.nro, `${student.apellido}, ${student.nombre}`];
  isoDates.forEach((iso) => {
    const state = attendanceMap.get(`${student.id}-${iso}`) ?? 'NC';
    row.push(state);
  });
  return row;
};

const todayISO = () => {
  const now = new Date();
  const utc = toUTCMidnight(now);
  return formatISO(utc);
};

const normalizeDateValue = (value) => {
  if (!value) {
    return null;
  }

  if (value instanceof Date) {
    return formatISO(value);
  }

  if (typeof value !== 'string') {
    return null;
  }

  const parsed = parseISODate(value.trim());
  if (!parsed || Number.isNaN(parsed.getTime())) {
    return null;
  }

  return formatISO(parsed);
};

const parseDateList = (input) => {
  if (!input && input !== 0) {
    return [];
  }

  const rawValues = Array.isArray(input)
    ? input
    : typeof input === 'string'
      ? input
          .split(',')
          .map((value) => value.trim())
          .filter((value) => value !== '')
      : [];

  const isoDates = rawValues
    .map((value) => normalizeDateValue(value))
    .filter((value) => value !== null);

  const unique = Array.from(new Set(isoDates));
  unique.sort();
  return unique;
};

export const attendanceService = {
  async listCourseSubjectsForDate(courseId, date) {
    const parsedCourseId = Number(courseId);
    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    const referenceDate = resolveReferenceDate(date);
    const { isoDate, dayNumber, subjects, warnings } = await buildCourseSubjectsForDate({
      courseId: parsedCourseId,
      subjectIds: [],
      referenceDate
    });

    return {
      ok: true,
      curso_id: parsedCourseId,
      fecha: isoDate,
      dia_semana: dayNumber,
      dia_nombre: dayNumberToName(dayNumber),
      materias: subjects,
      warnings
    };
  },

  async recordAttendance(payload) {
    const registros = await Promise.all(
      payload.registros.map((registro) =>
        attendanceRepository.record({
          alumno_id: registro.alumno_id,
          curso_id: payload.curso_id,
          fecha: payload.fecha,
          estado: registro.estado,
          es_contraturno: registro.es_contraturno ?? (payload.es_contraturno ?? 0),
          creado_por: payload.creado_por
        })
      )
    );
    return registros;
  },

  async updateAttendance(criteria, updates) {
    return attendanceRepository.update(criteria, updates);
  },

  async listAttendance(courseId, date) {
    return attendanceRepository.listByCourseAndDate(courseId, date);
  },

  async listByStudent(studentId) {
    return attendanceRepository.listByStudent(studentId);
  },

  async listCourseGeneralAttendance(courseId, { fechas, desde, hasta } = {}) {
    const parsedCourseId = Number(courseId);
    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    let isoDates = parseDateList(fechas);

    if (!isoDates.length && (desde || hasta)) {
      const fromIso = normalizeDateValue(desde);
      const toIso = normalizeDateValue(hasta ?? desde);

      if (!fromIso || !toIso) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'El rango de fechas es inválido');
      }

      if (fromIso > toIso) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'La fecha desde debe ser anterior a la fecha hasta');
      }

      const start = parseISODate(fromIso);
      const end = parseISODate(toIso);
      const current = new Date(start);
      isoDates = [];

      while (current <= end) {
        isoDates.push(formatISO(current));
        current.setUTCDate(current.getUTCDate() + 1);
      }
    }

    if (!isoDates.length) {
      isoDates = [todayISO()];
    }

    const records = await attendanceRepository.listByCourseAndDates(parsedCourseId, isoDates);
    const normalized = [];
    const grouped = {};

    records.forEach((record) => {
      const alumnoId = Number(record.alumno_id);
      if (!alumnoId) {
        return;
      }

      const isoDate = normalizeDateValue(record.fecha);
      if (!isoDate) {
        return;
      }

      const rawState = record.estado ? record.estado.toString().trim().toUpperCase() : 'NC';
      const state = normalizeState(rawState);
      const esContraturno = Number(record.es_contraturno ?? 0) === 1 ? 1 : 0;
      const shift = esContraturno === 1 ? 'contraturno' : 'turno';

      normalized.push({
        alumno_id: alumnoId,
        curso_id: Number(record.curso_id) || parsedCourseId,
        fecha: isoDate,
        estado: state,
        es_contraturno: esContraturno
      });

      if (!grouped[alumnoId]) {
        grouped[alumnoId] = {};
      }
      if (!grouped[alumnoId][isoDate]) {
        grouped[alumnoId][isoDate] = {};
      }
      grouped[alumnoId][isoDate][shift] = state;
    });

    return {
      curso_id: parsedCourseId,
      fechas: isoDates,
      registros: normalized,
      mapa_por_alumno: grouped
    };
  },

  async getAllowedDays({ teacherId, courseId, subjectId, baseDate }) {
    if (!teacherId) {
      throw new ApiError(httpStatus.FORBIDDEN, 'Solo los profesores pueden consultar esta información');
    }

    const allowedWeekdays = await attendanceRepository.getTeacherAllowedWeekdays(teacherId, courseId, subjectId);
    const weekDates = getWeekDates(baseDate);
    const isoDates = weekDates.map((date) => formatISO(date));
    const referenceIso = baseDate ?? todayISO();
    const referenceDate = parseISODate(referenceIso);
    const referenceDow = referenceDate ? dayOfWeekFromDate(referenceDate) : null;

    const allowedToday = referenceDow ? (allowedWeekdays.length ? allowedWeekdays.includes(referenceDow) : true) : false;

    let nextValid = null;
    if (!allowedToday && allowedWeekdays.length && referenceDate) {
      for (let offset = 1; offset <= 14; offset += 1) {
        const candidate = new Date(referenceDate);
        candidate.setUTCDate(referenceDate.getUTCDate() + offset);
        const candidateDow = dayOfWeekFromDate(candidate);
        if (allowedWeekdays.includes(candidateDow)) {
          nextValid = formatISO(candidate);
          break;
        }
      }
    }

    const week = isoDates.map((iso, index) => {
      const dow = dayOfWeekFromDate(weekDates[index]);
      const habilitado = allowedWeekdays.length ? allowedWeekdays.includes(dow) : true;
      return {
        fecha: iso,
        dow,
        habilitado
      };
    });

    return {
      allowed_dows: allowedWeekdays,
      allowed_today: allowedToday,
      next_valid: nextValid,
      week
    };
  },

  async getWeeklySubjectMatrix({ teacherId, courseId, subjectId, baseDate }) {
    const weekDates = getWeekDates(baseDate);
    const isoDates = weekDates.map((date) => formatISO(date));
    const displayDates = weekDates.map((date) => formatDisplayDate(date));
    const allowedWeekdays = teacherId
      ? await attendanceRepository.getTeacherAllowedWeekdays(teacherId, courseId, subjectId)
      : [];

    const students = await attendanceRepository.listCourseStudents(courseId);
    const studentsWithIndex = students.map((student, index) => ({
      ...student,
      nro: index + 1
    }));

    const attendances = await attendanceRepository.listSubjectAttendances(
      courseId,
      subjectId,
      isoDates[0],
      isoDates[isoDates.length - 1]
    );

    const attendanceMap = new Map();
    attendances.forEach((record) => {
      if (!record.fecha) {
        return;
      }
      const iso = record.fecha instanceof Date ? formatISO(record.fecha) : record.fecha;
      attendanceMap.set(`${record.alumno_id}-${iso}`, (record.estado || 'NC').toString().toUpperCase());
    });

    const filas = studentsWithIndex.map((student) => buildStudentRow(student, isoDates, attendanceMap));
    const alumnoIds = studentsWithIndex.map((student) => student.id);

    const editable = isoDates.map((iso) => {
      const date = parseISODate(iso);
      if (!date) {
        return false;
      }
      const weekday = dayOfWeekFromDate(date);
      return allowedWeekdays.length ? allowedWeekdays.includes(weekday) : true;
    });

    return {
      columnas: ['Nro', 'Nombre', ...displayDates],
      fechas_iso: isoDates,
      fechas: displayDates,
      editable,
      filas,
      alumno_ids: alumnoIds
    };
  },

  async saveSubjectAttendanceMatrix({ teacherId, courseId, subjectId, encabezados, asistencias }) {
    if (!teacherId) {
      throw new ApiError(httpStatus.FORBIDDEN, 'Solo los profesores pueden registrar asistencias por materia');
    }

    if (!Array.isArray(encabezados) || encabezados.length === 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debe indicar los encabezados de la tabla');
    }

    if (!Array.isArray(asistencias) || asistencias.length === 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debe indicar las asistencias a registrar');
    }

    const hasAssignment = await attendanceRepository.hasTeacherAssignment(teacherId, courseId, subjectId);
    if (!hasAssignment) {
      throw new ApiError(httpStatus.FORBIDDEN, 'No tenés asignada esa materia en este curso');
    }

    const allowedWeekdays = await attendanceRepository.getTeacherAllowedWeekdays(teacherId, courseId, subjectId);
    if (!allowedWeekdays.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'No hay horarios cargados para este curso y materia');
    }

    const fallbackYear = new Date().getUTCFullYear();
    const columnDateMap = new Map();

    encabezados.forEach((header, index) => {
      if (index < 2) {
        return;
      }
      const parsed = parseHeaderDate(header, fallbackYear);
      if (!parsed) {
        return;
      }
      const date = parseISODate(parsed);
      if (!date) {
        return;
      }
      const weekday = dayOfWeekFromDate(date);
      if (!allowedWeekdays.includes(weekday)) {
        return;
      }
      columnDateMap.set(index, formatISO(date));
    });

    if (!columnDateMap.size) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'No hay columnas válidas para guardar asistencias');
    }

    const students = await attendanceRepository.listCourseStudents(courseId);
    const orderedIds = students.map((student) => student.id);

    let applied = 0;
    let ignored = 0;

    for (const registro of asistencias) {
      let alumnoId = Number.parseInt(registro.alumno_id, 10);
      if (!alumnoId && registro.nro) {
        const index = Number.parseInt(registro.nro, 10) - 1;
        if (index >= 0 && index < orderedIds.length) {
          alumnoId = orderedIds[index];
        }
      }

      if (!alumnoId) {
        ignored += 1;
        continue;
      }

      const estados = registro.estados && typeof registro.estados === 'object' ? registro.estados : {};
      for (const [columnKey, estado] of Object.entries(estados)) {
        const columnIndex = Number.parseInt(columnKey, 10);
        if (Number.isNaN(columnIndex) || !columnDateMap.has(columnIndex)) {
          continue;
        }
        const isoDate = columnDateMap.get(columnIndex);
        const normalizedState = normalizeState(estado);
        if (normalizedState === 'NC') {
          continue;
        }
        await attendanceRepository.upsertSubjectAttendance({
          alumno_id: alumnoId,
          curso_id: courseId,
          materia_id: subjectId,
          fecha: isoDate,
          estado: normalizedState,
          creado_por: teacherId
        });
        applied += 1;
      }
    }

    return {
      applied,
      ignored
    };
  },

  async importProfessorAttendance({ preceptorId, courseId, subjectIds, date, dryRun }) {
    if (!preceptorId) {
      throw new ApiError(httpStatus.FORBIDDEN, 'Solo los preceptores pueden importar asistencias');
    }

    const parsedCourseId = Number(courseId);
    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    const materiaIds = toUniquePositiveIntegers(subjectIds);
    if (!materiaIds.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debe seleccionar al menos una materia');
    }

    const referenceDate = resolveReferenceDate(date);
    const { isoDate, displayDate, subjects, warnings, matchedInfo } = await buildCourseSubjectsForDate({
      courseId: parsedCourseId,
      subjectIds: materiaIds,
      referenceDate
    });

    const summary = {
      turno: createEmptySummary(),
      contraturno: createEmptySummary()
    };

    if (!subjects.length) {
      return {
        ok: true,
        simulacion: Boolean(dryRun),
        curso_id: parsedCourseId,
        fecha: isoDate,
        warnings,
        turno: summary.turno,
        contraturno: summary.contraturno,
        mensaje: 'No hay materias del curso que coincidan con ese día.'
      };
    }

    const students = await attendanceRepository.listCourseStudents(parsedCourseId);
    if (!students.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso no tiene alumnos asignados');
    }

    const studentIds = students.map((student) => Number(student.id));
    const studentIdSet = new Set(studentIds);
    const subjectIdSet = new Set(subjects.map((subject) => subject.id));

    const perShiftStates = {
      0: new Map(),
      1: new Map()
    };

    const registros = await attendanceRepository.listSubjectAttendancesForDate(
      parsedCourseId,
      Array.from(subjectIdSet),
      isoDate,
      studentIds
    );

    registros.forEach((registro) => {
      const alumnoId = Number(registro.alumno_id);
      const materiaId = Number(registro.materia_id);
      if (!studentIdSet.has(alumnoId) || !matchedInfo.has(materiaId)) {
        return;
      }

      const shift = matchedInfo.get(materiaId).es_contraturno ? 1 : 0;
      const normalizedState = normalizeState(registro.estado);
      const current = perShiftStates[shift].get(alumnoId) ?? 'NC';
      perShiftStates[shift].set(alumnoId, pickPriorityState(current, normalizedState));
    });

    [0, 1].forEach((shift) => {
      const summaryKey = shift === 1 ? 'contraturno' : 'turno';
      const summaryEntry = summary[summaryKey];
      studentIds.forEach((alumnoId) => {
        const normalizedState = perShiftStates[shift].get(alumnoId) ?? 'NC';
        perShiftStates[shift].set(alumnoId, normalizedState);
        summaryEntry[normalizedState] = (summaryEntry[normalizedState] ?? 0) + 1;
        summaryEntry.total += 1;
      });
    });

    if (dryRun) {
      return {
        ok: true,
        simulacion: true,
        curso_id: parsedCourseId,
        fecha: isoDate,
        warnings,
        turno: summary.turno,
        contraturno: summary.contraturno
      };
    }

    let applied = 0;
    for (const shift of [0, 1]) {
      for (const [alumnoId, estado] of perShiftStates[shift].entries()) {
        await attendanceRepository.record({
          alumno_id: alumnoId,
          curso_id: parsedCourseId,
          fecha: isoDate,
          estado,
          es_contraturno: shift,
          creado_por: preceptorId
        });
        applied += 1;
      }
    }

    return {
      ok: true,
      simulacion: false,
      curso_id: parsedCourseId,
      fecha: isoDate,
      warnings,
      turno: summary.turno,
      contraturno: summary.contraturno,
      resumen: {
        turno: summary.turno,
        contraturno: summary.contraturno,
        registros: applied
      },
      mensaje: `Importación realizada para ${displayDate}.`
    };
  },

  async importSubjectAttendanceFromGeneral({ teacherId, courseId, subjectId, date }) {
    if (!teacherId) {
      throw new ApiError(httpStatus.FORBIDDEN, 'Solo los profesores pueden importar asistencias');
    }

    const hasAssignment = await attendanceRepository.hasTeacherAssignment(teacherId, courseId, subjectId);
    if (!hasAssignment) {
      throw new ApiError(httpStatus.FORBIDDEN, 'No tenés asignada esa materia en este curso');
    }

    const subject = await attendanceRepository.getSubjectShift(subjectId);
    const esContraturno = subject?.es_contraturno ? Number(subject.es_contraturno) : 0;

    const registrosGenerales = await attendanceRepository.listGeneralByDateForShift(courseId, date, esContraturno);
    if (!registrosGenerales.length) {
      throw new ApiError(
        httpStatus.NOT_FOUND,
        esContraturno
          ? 'No hay asistencias generales para esa fecha en contraturno'
          : 'No hay asistencias generales para esa fecha'
      );
    }

    const students = await attendanceRepository.listCourseStudents(courseId);
    const idToOrder = new Map();
    students.forEach((student, index) => {
      idToOrder.set(Number(student.id), index + 1);
    });

    await attendanceRepository.deleteSubjectAttendanceByDate(courseId, subjectId, date);

    let applied = 0;
    const map = {};

    for (const registro of registrosGenerales) {
      const normalizedState = normalizeState(registro.estado);
      await attendanceRepository.upsertSubjectAttendance({
        alumno_id: registro.alumno_id,
        curso_id: courseId,
        materia_id: subjectId,
        fecha: date,
        estado: normalizedState,
        creado_por: teacherId
      });
      applied += 1;

      const order = idToOrder.get(Number(registro.alumno_id));
      if (order) {
        map[order] = normalizedState;
      }
    }

    return {
      applied,
      importados: applied,
      turno: esContraturno ? 'CONTRATURNO' : 'TURNO',
      map
    };
  },

  async getCourseSummary(courseId, date) {
    const effectiveDate = date || todayISO();
    const rows = await attendanceRepository.listGeneralSummary(courseId, effectiveDate);

    const summary = {
      turno: { presentes: 0, ausentes: 0, tarde: 0, total: 0 },
      contraturno: { presentes: 0, ausentes: 0, tarde: 0, total: 0 }
    };

    rows.forEach((row) => {
      const key = Number(row.es_contraturno) === 1 ? 'contraturno' : 'turno';
      summary[key] = {
        presentes: Number(row.presentes) || 0,
        ausentes: Number(row.ausentes) || 0,
        tarde: Number(row.tarde) || 0,
        total: Number(row.total) || 0
      };
    });

    const totals = {
      presentes: summary.turno.presentes + summary.contraturno.presentes,
      ausentes: summary.turno.ausentes + summary.contraturno.ausentes,
      tarde: summary.turno.tarde + summary.contraturno.tarde,
      total: summary.turno.total + summary.contraturno.total
    };

    return {
      fecha: effectiveDate,
      fecha_formateada: effectiveDate.split('-').reverse().join('/'),
      ...summary,
      totales: totals
    };
  },

  async getCourseAttendanceRange(courseId) {
    const parsedCourseId = Number(courseId);
    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    const row = await attendanceRepository.getCourseDateRange(parsedCourseId);
    const inicio = normalizeDateValue(row?.inicio ?? null);
    const fin = normalizeDateValue(row?.fin ?? null);

    return {
      curso_id: parsedCourseId,
      inicio,
      fin
    };
  }
};
