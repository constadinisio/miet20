import httpStatus from 'http-status';
import { assignmentRepository } from '../repositories/assignmentRepository.js';
import { courseRepository } from '../repositories/courseRepository.js';
import { gradeRepository } from '../repositories/gradeRepository.js';
import { ApiError } from '../utils/ApiError.js';

const ensureTeacherCourseAccess = async (courseId, teacherId) => {
  if (!teacherId) {
    return null;
  }

  const assignments = await courseRepository.listAssignments({
    curso_id: courseId,
    profesor_id: teacherId,
    estado: 'activo'
  });

  if (!assignments.length) {
    throw new ApiError(httpStatus.FORBIDDEN, 'No tenés asignado este curso');
  }

  return assignments;
};

const parseIdList = (value) => {
  if (!value && value !== 0) {
    return [];
  }

  const rawValues = Array.isArray(value)
    ? value
    : typeof value === 'string'
      ? value
          .split(',')
          .map((item) => item.trim())
          .filter((item) => item !== '')
      : [value];

  const ids = rawValues
    .map((item) => Number(item))
    .filter((item) => Number.isInteger(item) && item > 0);

  return Array.from(new Set(ids));
};

export const assignmentService = {
  async createAssignment(payload) {
    return assignmentRepository.create({
      materia_id: payload.materia_id,
      nombre: typeof payload.nombre === 'string' ? payload.nombre.trim() : payload.nombre,
      tipo: payload.tipo ?? 'tp',
      descripcion: payload.descripcion ?? null,
      fecha_creacion: new Date()
    });
  },

  async listByCourse(courseId, { subjectId, teacherId } = {}) {
    const parsedCourseId = Number(courseId);

    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es inválido');
    }

    const parsedSubjectId = subjectId ? Number(subjectId) : undefined;
    const parsedTeacherId = teacherId ? Number(teacherId) : undefined;

    let teacherAssignments = null;
    if (parsedTeacherId) {
      teacherAssignments = await ensureTeacherCourseAccess(parsedCourseId, parsedTeacherId);

      if (
        parsedSubjectId &&
        !teacherAssignments.some((assignment) => Number(assignment.materia_id) === parsedSubjectId)
      ) {
        throw new ApiError(httpStatus.FORBIDDEN, 'No tenés asignada esa materia en este curso');
      }
    }

    return assignmentRepository.listByCourse(parsedCourseId, {
      subjectId: parsedSubjectId,
      teacherId: parsedTeacherId
    });
  },

  async listStudentSubmissions(studentId) {
    return assignmentRepository.listStudentSubmissions(studentId);
  },

  async updateAssignment(id, updates) {
    const updatePayload = { ...updates };

    if (typeof updatePayload.nombre === 'string') {
      updatePayload.nombre = updatePayload.nombre.trim();
    }

    const updated = await assignmentRepository.update(id, updatePayload);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Trabajo no encontrado');
    }
    return updated;
  },

  async deleteAssignment(id) {
    const deleted = await assignmentRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Trabajo no encontrado');
    }
    return true;
  },

  async listCourseAssignmentGrades(courseId, { subjectId, studentIds, teacherId } = {}) {
    const parsedCourseId = Number(courseId);

    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es inválido');
    }

    const parsedSubjectId = subjectId ? Number(subjectId) : undefined;
    const parsedTeacherId = teacherId ? Number(teacherId) : undefined;
    const normalizedStudentIds = parseIdList(studentIds);

    let teacherAssignments = null;
    if (parsedTeacherId) {
      teacherAssignments = await ensureTeacherCourseAccess(parsedCourseId, parsedTeacherId);

      if (
        parsedSubjectId &&
        !teacherAssignments.some((assignment) => Number(assignment.materia_id) === parsedSubjectId)
      ) {
        throw new ApiError(httpStatus.FORBIDDEN, 'No tenés asignada esa materia en este curso');
      }
    }

    let subjectIds = [];
    if (parsedSubjectId) {
      subjectIds = [parsedSubjectId];
    } else if (teacherAssignments) {
      subjectIds = Array.from(
        new Set(
          teacherAssignments
            .map((assignment) => Number(assignment.materia_id))
            .filter((id) => Number.isInteger(id) && id > 0)
        )
      );
    }

    return assignmentRepository.listCourseAssignmentGrades(parsedCourseId, {
      subjectIds: subjectIds.length ? subjectIds : undefined,
      studentIds: normalizedStudentIds
    });
  },

  async createSubmission(payload) {
    const assignment = await assignmentRepository.findById(payload.trabajo_id);
    if (!assignment) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Trabajo no encontrado');
    }

    if (assignment.materia_id !== payload.materia_id) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La materia enviada no coincide con el trabajo');
    }

    if (payload.rol === 'profesor') {
      const assignmentAccess = await gradeRepository.findProfessorAssignmentForStudent({
        alumnoId: payload.alumno_id,
        materiaId: payload.materia_id,
        profesorId: payload.profesor_id
      });

      if (!assignmentAccess) {
        throw new ApiError(httpStatus.FORBIDDEN, 'No tiene permiso para calificar este trabajo');
      }
    }

    return assignmentRepository.createSubmission({
      alumno_id: payload.alumno_id,
      trabajo_id: payload.trabajo_id,
      materia_id: payload.materia_id,
      nota: payload.nota ?? 0
    });
  }
};
