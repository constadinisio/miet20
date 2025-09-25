import httpStatus from 'http-status';
import { pendingSubjectRepository } from '../repositories/pendingSubjectRepository.js';
import { studentRepository } from '../repositories/studentRepository.js';
import { subjectRepository } from '../repositories/subjectRepository.js';
import { userRepository } from '../repositories/userRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { getDatabase } from '../config/database.js';

const normalizeOptionalNumber = (value) => {
  if (value === null || value === undefined) {
    return null;
  }

  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
};

const buildProfessorDisplayName = (record) => {
  const apellido = record.profesor_apellido?.trim?.() ?? '';
  const nombre = record.profesor_nombre?.trim?.() ?? '';

  if (!apellido && !nombre) {
    return null;
  }

  if (!apellido) {
    return nombre;
  }

  if (!nombre) {
    return apellido;
  }

  return `${apellido}, ${nombre}`;
};

const mapPendingSubject = (record) => ({
  id: Number(record.id),
  alumno_id: Number(record.alumno_id),
  materia_id: Number(record.materia_id),
  materia: record.materia,
  estado: record.estado,
  ultima_nota: normalizeOptionalNumber(record.ultima_nota),
  ultima_fecha: record.ultima_fecha ?? null,
  profesor_id: normalizeOptionalNumber(record.profesor_id)
});

const mapHistoryEntry = (record) => ({
  id: Number(record.id),
  pendiente_id: record.pendiente_id ? Number(record.pendiente_id) : null,
  materia_id: Number(record.materia_id),
  alumno_id: Number(record.alumno_id),
  materia: record.materia,
  nota: normalizeOptionalNumber(record.nota),
  estado: record.estado,
  profesor_id: normalizeOptionalNumber(record.profesor_id),
  profesor: buildProfessorDisplayName(record),
  fecha_resolucion: record.fecha_resolucion
});

const ensureStudentExists = async (studentId) => {
  const student = await studentRepository.findById(studentId);

  if (!student) {
    throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
  }

  return student;
};

const ensureSubjectExists = async (subjectId) => {
  const subject = await subjectRepository.findById(subjectId);

  if (!subject) {
    throw new ApiError(httpStatus.NOT_FOUND, 'Materia no encontrada');
  }

  return subject;
};

const ensureTeacherExists = async (teacherId) => {
  const teacher = await userRepository.findById(teacherId);

  if (!teacher || Number(teacher.rol) !== 3) {
    throw new ApiError(httpStatus.NOT_FOUND, 'Profesor no encontrado');
  }

  return teacher;
};

export const pendingSubjectService = {
  async listPendingByStudent(studentId) {
    await ensureStudentExists(studentId);
    const records = await pendingSubjectRepository.listByStudent(studentId);
    return records.map(mapPendingSubject);
  },

  async listHistoryByStudent(studentId) {
    await ensureStudentExists(studentId);
    const records = await pendingSubjectRepository.listHistoryByStudent(studentId);
    return records.map(mapHistoryEntry);
  },

  async searchSubjects(term, limit = 15) {
    const parsedLimit = Number(limit);
    const effectiveLimit = Number.isInteger(parsedLimit) && parsedLimit > 0 ? parsedLimit : 15;

    const results = await pendingSubjectRepository.searchSubjects(term ?? '', effectiveLimit);
    return results.map((record) => ({
      id: Number(record.id),
      nombre: record.nombre
    }));
  },

  async createPendingSubject({ alumnoId, materiaId }) {
    const studentId = Number(alumnoId);
    const subjectId = Number(materiaId);

    if (!Number.isInteger(studentId) || studentId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El alumno es obligatorio');
    }

    if (!Number.isInteger(subjectId) || subjectId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La materia es obligatoria');
    }

    await ensureStudentExists(studentId);
    await ensureSubjectExists(subjectId);

    const existing = await pendingSubjectRepository.findByStudentAndSubject(studentId, subjectId);
    if (existing) {
      throw new ApiError(httpStatus.CONFLICT, 'La materia ya está registrada como pendiente');
    }

    const created = await pendingSubjectRepository.create({
      alumno_id: studentId,
      materia_id: subjectId,
      estado: 'pendiente'
    });

    return mapPendingSubject(created);
  },

  async approvePendingSubject(pendingId, { nota, profesorId }) {
    const id = Number(pendingId);

    if (!Number.isInteger(id) || id <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La materia pendiente es obligatoria');
    }

    const pending = await pendingSubjectRepository.findById(id);

    if (!pending) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia pendiente no encontrada');
    }

    const normalizedNota = nota === undefined || nota === null ? null : Number(nota);

    if (normalizedNota === null || Number.isNaN(normalizedNota)) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La nota es obligatoria');
    }

    const teacherId = Number(profesorId);
    if (!Number.isInteger(teacherId) || teacherId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El profesor es obligatorio');
    }

    await ensureTeacherExists(teacherId);

    const db = getDatabase();

    const history = await db.transaction(async (trx) => {
      const historyEntry = await pendingSubjectRepository.addHistory(
        {
          pendiente_id: id,
          materia_id: Number(pending.materia_id),
          alumno_id: Number(pending.alumno_id),
          nota: normalizedNota,
          estado: 'aprobada',
          profesor_id: teacherId,
          fecha_resolucion: trx.fn.now()
        },
        { trx }
      );

      await pendingSubjectRepository.remove(id, { trx });

      return historyEntry;
    });

    return mapHistoryEntry(history);
  },

  async listTeachersByPending(pendingId) {
    const id = Number(pendingId);

    if (!Number.isInteger(id) || id <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La materia pendiente es obligatoria');
    }

    const pending = await pendingSubjectRepository.findById(id);
    if (!pending) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Materia pendiente no encontrada');
    }

    const teachers = await pendingSubjectRepository.listTeachersBySubject(Number(pending.materia_id));

    return teachers.map((teacher) => ({
      id: Number(teacher.id),
      nombre: teacher.nombre,
      apellido: teacher.apellido
    }));
  }
};
