import httpStatus from 'http-status';
import { gradeRepository } from '../repositories/gradeRepository.js';
import { ApiError } from '../utils/ApiError.js';

export const gradeService = {
  async recordGrade(payload) {
    const { profesor_id: profesorId, curso_id: courseId, alumno_id: studentId, materia_id: subjectId } = payload;

    if (!profesorId) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'Profesor no autenticado');
    }

    const assignment = await gradeRepository.findProfessorAssignment({
      cursoId: courseId,
      materiaId: subjectId,
      profesorId
    });

    if (!assignment) {
      throw new ApiError(httpStatus.FORBIDDEN, 'No tiene permiso para cargar notas en este curso y materia');
    }

    const enrollment = await gradeRepository.findActiveEnrollment(studentId, courseId);
    if (!enrollment) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El alumno no pertenece al curso indicado');
    }

    return gradeRepository.create({
      alumno_id: studentId,
      materia_id: subjectId,
      periodo: payload.periodo,
      nota: payload.nota,
      promedio_actividades: payload.promedio_actividades ?? 0,
      fecha_carga: new Date()
    });
  },

  async updateGrade(id, updates, profesorId) {
    if (!profesorId) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'Profesor no autenticado');
    }

    const existing = await gradeRepository.findById(id);
    if (!existing) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Calificación no encontrada');
    }

    const assignment = await gradeRepository.findProfessorAssignmentForStudent({
      alumnoId: existing.alumno_id,
      materiaId: existing.materia_id,
      profesorId
    });

    if (!assignment) {
      throw new ApiError(httpStatus.FORBIDDEN, 'No tiene permiso para editar esta calificación');
    }

    const data = { ...updates };
    if (updates.nota !== undefined) {
      data.nota = updates.nota;
    }
    const updated = await gradeRepository.update(id, data);
    return updated;
  },

  async listByStudent(studentId) {
    return gradeRepository.listByStudent(studentId);
  },

  async listByCourse(courseId, filters = {}) {
    const normalizedFilters = { ...filters };

    if (typeof normalizedFilters.periodo === 'string') {
      const trimmed = normalizedFilters.periodo.trim();
      normalizedFilters.periodo = trimmed === '' ? undefined : trimmed;
    }

    return gradeRepository.listByCourse(courseId, normalizedFilters);
  }
};
