import httpStatus from 'http-status';
import { courseRepository } from '../repositories/courseRepository.js';
import { studentRepository } from '../repositories/studentRepository.js';
import { ApiError } from '../utils/ApiError.js';

const normalizeAssignment = (assignment) => {
  if (!assignment) {
    return null;
  }

  return {
    id: assignment.id,
    profesorId: assignment.profesor_id ? Number(assignment.profesor_id) : null,
    cursoId: assignment.curso_id ? Number(assignment.curso_id) : null,
    materiaId: assignment.materia_id ? Number(assignment.materia_id) : null,
    estado: assignment.estado,
    esContraturno: Number(assignment.es_contraturno ?? 0),
    curso: assignment.curso_anio
      ? {
          anio: assignment.curso_anio,
          division: assignment.curso_division,
          turno: assignment.curso_turno
        }
      : null,
    materia: assignment.materia_nombre
      ? {
          nombre: assignment.materia_nombre,
          codigo: assignment.materia_codigo ?? null
        }
      : null,
    profesor: assignment.profesor_nombre
      ? {
          nombre: assignment.profesor_nombre,
          apellido: assignment.profesor_apellido
        }
      : null
  };
};

export const courseService = {
  async createCourse(payload) {
    return courseRepository.create({
      anio: payload.anio,
      division: payload.division,
      turno: payload.turno,
      estado: payload.estado ?? 'activo'
    });
  },

  async listCourses(filters) {
    const courses = await courseRepository.list(filters);

    return courses.map((course) => ({
      ...course,
      nombre:
        course?.nombre ??
        `${course.anio ?? ''}${course.anio ? '°' : ''} ${course.division ?? ''}`.trim()
    }));
  },

  async getCourse(id) {
    const course = await courseRepository.findById(id);
    if (!course) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Curso no encontrado');
    }
    return course;
  },

  async updateCourse(id, updates) {
    const updated = await courseRepository.update(id, updates);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Curso no encontrado');
    }
    return updated;
  },

  async deleteCourse(id) {
    const deleted = await courseRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Curso no encontrado');
    }
    return true;
  },

  async assignSubject({ cursoId, materiaId, profesorId, esContraturno }) {
    const existingAssignment = await courseRepository.findAssignmentByProfessorCourseSubject({
      curso_id: cursoId,
      materia_id: materiaId,
      profesor_id: profesorId ?? null
    });

    if (existingAssignment) {
      throw new ApiError(httpStatus.CONFLICT, 'La asignación ya existe para ese profesor');
    }

    const assignment = await courseRepository.assignSubject({
      curso_id: cursoId,
      materia_id: materiaId,
      profesor_id: profesorId,
      estado: 'activo',
      es_contraturno: esContraturno ?? 0
    });
    return normalizeAssignment(assignment);
  },

  async assignTeacher({ cursoId, materiaId, profesorId, esContraturno }) {
    const assignment = await courseRepository.assignTeacher({
      curso_id: cursoId,
      materia_id: materiaId,
      profesor_id: profesorId,
      es_contraturno: esContraturno ?? 0
    });
    if (!assignment) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'No se pudo asignar el profesor a la materia');
    }
    return normalizeAssignment(assignment);
  },

  async removeAssignment(assignmentId) {
    const deleted = await courseRepository.removeAssignment(assignmentId);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Asignación no encontrada');
    }
    return true;
  },

  async listAssignments(filters = {}) {
    const assignments = await courseRepository.listAssignments(filters);

    return assignments.map((assignment) => normalizeAssignment(assignment));
  },

  async getAssignment(id) {
    const assignment = await courseRepository.findAssignmentById(id);
    if (!assignment) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Asignación no encontrada');
    }

    return normalizeAssignment(assignment);
  },

  async listCourseStudents(courseId, { teacherId } = {}) {
    const parsedCourseId = Number(courseId);

    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es inválido');
    }

    if (teacherId) {
      const assignments = await courseRepository.listAssignments({
        curso_id: parsedCourseId,
        profesor_id: Number(teacherId),
        estado: 'activo'
      });

      if (!assignments.length) {
        throw new ApiError(httpStatus.FORBIDDEN, 'No tenés asignado este curso');
      }
    }

    const students = await studentRepository.listByCourse(parsedCourseId);

    return students.map((student) => ({
      id: Number(student.id),
      nombre: student.nombre,
      apellido: student.apellido,
      dni: student.dni,
      estado_academico: student.estado_academico,
      promedio:
        student.promedio === null || student.promedio === undefined
          ? null
          : Number.isNaN(Number(student.promedio))
            ? null
            : Number(student.promedio)
    }));
  }
};
