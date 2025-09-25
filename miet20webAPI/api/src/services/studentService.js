import bcrypt from 'bcryptjs';
import httpStatus from 'http-status';
import { studentRepository } from '../repositories/studentRepository.js';
import { courseRepository } from '../repositories/courseRepository.js';
import { ApiError } from '../utils/ApiError.js';

const sanitizeStudent = (student) => {
  if (!student) {
    return student;
  }
  const { contrasena: _password, promedio, ...rest } = student;
  const sanitized = { ...rest };

  if (typeof promedio !== 'undefined') {
    sanitized.promedio = promedio === null || promedio === undefined ? null : Number(promedio);
    if (Number.isNaN(sanitized.promedio)) {
      sanitized.promedio = null;
    }
  }

  return sanitized;
};

export const studentService = {
  async createStudent(payload) {
    const studentData = {
      nombre: payload.nombre,
      apellido: payload.apellido,
      dni: payload.dni,
      fecha_nacimiento: payload.fecha_nacimiento,
      mail: payload.mail ?? null,
      telefono: payload.telefono ?? null,
      direccion: payload.direccion ?? null,
      rol: 4,
      status: payload.status ?? 1
    };

    if (payload.contrasena) {
      studentData.contrasena = await bcrypt.hash(payload.contrasena, 10);
    }

    const created = await studentRepository.create(studentData);
    return sanitizeStudent(created);
  },

  async listStudents(filters = {}) {
    if (filters && Object.prototype.hasOwnProperty.call(filters, 'curso_id')) {
      const courseId = Number(filters.curso_id);
      if (!Number.isNaN(courseId) && courseId > 0) {
        const studentsByCourse = await studentRepository.listByCourse(courseId);
        const items = studentsByCourse.map(sanitizeStudent);
        return {
          items,
          pagination: {
            page: 1,
            limit: items.length,
            total: items.length,
            totalPages: items.length ? 1 : 0
          }
        };
      }
    }

    const searchParam = [filters.search, filters.q, filters.busqueda]
      .map((value) => (typeof value === 'string' ? value.trim() : ''))
      .find((value) => value !== '') ?? '';

    const pageParam = [filters.page, filters.pagina]
      .map((value) => Number(value))
      .find((value) => Number.isInteger(value) && value > 0);
    const page = pageParam ?? 1;

    const limitParam = [filters.limit, filters.per_page, filters.resultados_por_pagina]
      .map((value) => Number(value))
      .find((value) => Number.isInteger(value) && value > 0);
    const limit = limitParam ?? 10;

    const status = filters.status ?? filters.estado;

    const repositoryResult = await studentRepository.list({
      search: searchParam,
      page,
      limit,
      status,
      anio: filters.anio,
      division: filters.division
    });

    return {
      items: repositoryResult.items.map(sanitizeStudent),
      pagination: repositoryResult.pagination
    };
  },

  async getStudent(id) {
    const student = await studentRepository.findById(id);
    if (!student) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }
    return sanitizeStudent(student);
  },

  async updateStudent(id, updates) {
    const data = { ...updates };
    if (updates.contrasena) {
      data.contrasena = await bcrypt.hash(updates.contrasena, 10);
    }
    const updated = await studentRepository.update(id, data);
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }
    return sanitizeStudent(updated);
  },

  async deleteStudent(id) {
    const deleted = await studentRepository.remove(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }
    return true;
  },

  async enrollStudent({ alumnoId, cursoId, estado }) {
    const student = await studentRepository.findById(alumnoId);
    if (!student) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }

    const course = await courseRepository.findById(cursoId);
    if (!course) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Curso no encontrado');
    }

    const existingEnrollment = await studentRepository.findEnrollment(alumnoId, cursoId);

    if (existingEnrollment) {
      if (estado && existingEnrollment.estado !== estado) {
        const updated = await studentRepository.updateEnrollment(alumnoId, cursoId, { estado });
        return { enrollment: updated, created: false };
      }

      return { enrollment: existingEnrollment, created: false };
    }

    const enrollment = await studentRepository.enroll({
      alumno_id: alumnoId,
      curso_id: cursoId,
      estado: estado ?? 'activo'
    });

    return { enrollment, created: true };
  },

  async unenrollStudent({ alumnoId, cursoId }) {
    return studentRepository.unenroll(alumnoId, cursoId);
  },

  async getFamilyData(studentId) {
    const student = await studentRepository.findById(studentId);
    if (!student) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }

    const family = await studentRepository.findFamilyByStudentId(studentId);
    if (!family) {
      return {
        usuario_id: studentId,
        padre_nombre: null,
        padre_tel: null,
        padre_mail: null,
        madre_nombre: null,
        madre_tel: null,
        madre_mail: null,
        emergencia_nombre: null,
        emergencia_tel: null
      };
    }

    return family;
  },

  async updateFamilyData(studentId, payload) {
    const student = await studentRepository.findById(studentId);
    if (!student) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }

    const sanitized = {
      padre_nombre: payload.padre_nombre ?? null,
      padre_tel: payload.padre_tel ?? null,
      padre_mail: payload.padre_mail ?? null,
      madre_nombre: payload.madre_nombre ?? null,
      madre_tel: payload.madre_tel ?? null,
      madre_mail: payload.madre_mail ?? null,
      emergencia_nombre: payload.emergencia_nombre ?? null,
      emergencia_tel: payload.emergencia_tel ?? null
    };

    return studentRepository.upsertFamily(studentId, sanitized);
  },

  async updateCensusData(studentId, payload) {
    const updated = await studentRepository.updateCensus(studentId, {
      ficha_censal: payload.ficha_censal ?? null
    });
    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Alumno no encontrado');
    }

    return sanitizeStudent(updated);
  },

  async progressStudents({ courseOriginId, students }) {
    const originId = Number(courseOriginId);
    if (!Number.isInteger(originId) || originId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso de origen es inválido');
    }

    if (!Array.isArray(students) || !students.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Debe especificar alumnos a procesar');
    }

    const movements = students
      .map((student) => ({
        studentId: Number(student.id ?? student.alumno_id),
        destination: student.destino ?? student.destination ?? null
      }))
      .filter((movement) => Number.isInteger(movement.studentId) && movement.studentId > 0);

    if (!movements.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'No se recibieron alumnos válidos para procesar');
    }

    const result = await studentRepository.bulkProgress({
      courseOriginId: originId,
      movements
    });

    return result;
  }
};
