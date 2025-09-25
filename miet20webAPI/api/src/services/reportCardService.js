import httpStatus from 'http-status';
import { reportCardRepository } from '../repositories/reportCardRepository.js';
import { studentRepository } from '../repositories/studentRepository.js';
import { courseRepository } from '../repositories/courseRepository.js';
import { ApiError } from '../utils/ApiError.js';

const STATUS_MAP = {
  draft: 'borrador',
  published: 'publicado',
  archived: 'archivado'
};

const REVERSE_STATUS_MAP = Object.entries(STATUS_MAP).reduce((acc, [apiStatus, dbStatus]) => {
  acc[dbStatus] = apiStatus;
  return acc;
}, {});

const normalizeStatusToDb = (status) => {
  if (!status) {
    return STATUS_MAP.draft;
  }

  const normalized = STATUS_MAP[status];
  if (!normalized) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'Estado de boletín inválido');
  }
  return normalized;
};

const mapStatusToApi = (dbStatus) => REVERSE_STATUS_MAP[dbStatus] ?? 'draft';

const mapReportCard = (reportCard) => {
  if (!reportCard) {
    return null;
  }

  return {
    id: reportCard.id,
    studentId: reportCard.studentId,
    courseId: reportCard.courseId,
    academicYear: reportCard.academicYear,
    term: reportCard.term,
    status: mapStatusToApi(reportCard.status),
    observations: reportCard.observations,
    issuedAt: reportCard.issuedAt,
    createdBy: reportCard.createdBy,
    updatedBy: reportCard.updatedBy,
    createdAt: reportCard.createdAt,
    updatedAt: reportCard.updatedAt,
    student: reportCard.student
      ? {
          id: reportCard.student.id,
          firstName: reportCard.student.firstName,
          lastName: reportCard.student.lastName,
          dni: reportCard.student.dni,
          code: reportCard.student.code
        }
      : null,
    course: reportCard.course
      ? {
          id: reportCard.course.id,
          year: reportCard.course.year,
          division: reportCard.course.division,
          turno: reportCard.course.turno
        }
      : null
  };
};

const normalizeGradePayload = (grade) => ({
  id: grade.id ? Number(grade.id) : undefined,
  subjectId: Number(grade.subjectId),
  numericGrade: grade.numericGrade !== undefined && grade.numericGrade !== '' ? Number(grade.numericGrade) : null,
  conceptualGrade: grade.conceptualGrade ?? null,
  observations: grade.observations ?? null
});

const ensureStudentExists = async (studentId) => {
  if (!studentId) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El alumno es obligatorio');
  }
  const student = await studentRepository.findById(studentId);
  if (!student) {
    throw new ApiError(httpStatus.NOT_FOUND, 'El alumno indicado no existe');
  }
  return student;
};

const ensureCourseExists = async (courseId) => {
  if (!courseId) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
  }
  const course = await courseRepository.findById(courseId);
  if (!course) {
    throw new ApiError(httpStatus.NOT_FOUND, 'El curso indicado no existe');
  }
  return course;
};

export const reportCardService = {
  async listReportCards(filters = {}) {
    const normalizedFilters = { ...filters };

    if (normalizedFilters.courseId) {
      normalizedFilters.courseId = Number(normalizedFilters.courseId);
    }

    if (normalizedFilters.studentId) {
      normalizedFilters.studentId = Number(normalizedFilters.studentId);
    }

    if (normalizedFilters.academicYear) {
      normalizedFilters.academicYear = Number(normalizedFilters.academicYear);
    }

    if (normalizedFilters.status) {
      normalizedFilters.status = normalizeStatusToDb(normalizedFilters.status);
    }

    const reportCards = await reportCardRepository.list(normalizedFilters);
    return reportCards.map(mapReportCard);
  },

  async getReportCard(reportCardId) {
    const reportCard = await reportCardRepository.findById(reportCardId);
    if (!reportCard) {
      throw new ApiError(httpStatus.NOT_FOUND, 'El boletín solicitado no existe');
    }
    const grades = await reportCardRepository.listGrades(reportCardId);
    const subjects = reportCard.courseId ? await reportCardRepository.listCourseSubjects(reportCard.courseId) : [];

    return {
      ...mapReportCard(reportCard),
      grades: grades.map((grade) => ({ ...grade })),
      subjects
    };
  },

  async createReportCard(userId, payload) {
    const studentId = Number(payload.studentId);
    const courseId = Number(payload.courseId);
    await ensureStudentExists(studentId);
    await ensureCourseExists(courseId);

    const academicYear = payload.academicYear ? Number(payload.academicYear) : new Date().getFullYear();
    const term = payload.term ?? '1er Bimestre';
    const status = normalizeStatusToDb(payload.status ?? 'draft');

    const created = await reportCardRepository.create({
      studentId,
      courseId,
      academicYear,
      term,
      status,
      observations: payload.observations ?? null,
      issuedAt: null,
      createdBy: userId,
      updatedBy: userId
    });

    const subjects = await reportCardRepository.listCourseSubjects(courseId);
    const providedGrades = Array.isArray(payload.grades) ? payload.grades.map(normalizeGradePayload) : [];
    const providedSubjectIds = new Set(providedGrades.map((grade) => grade.subjectId));

    if (providedGrades.length > 0) {
      await Promise.all(
        providedGrades.map((grade) => reportCardRepository.upsertGrade(created.id, grade))
      );
    }

    const missingSubjects = subjects.filter((subject) => !providedSubjectIds.has(subject.id));
    if (missingSubjects.length > 0) {
      await Promise.all(
        missingSubjects.map((subject) =>
          reportCardRepository.upsertGrade(created.id, {
            subjectId: subject.id,
            numericGrade: null,
            conceptualGrade: null,
            observations: null
          })
        )
      );
    }

    return this.getReportCard(created.id);
  },

  async updateReportCard(reportCardId, userId, payload) {
    const existing = await reportCardRepository.findById(reportCardId);
    if (!existing) {
      throw new ApiError(httpStatus.NOT_FOUND, 'El boletín solicitado no existe');
    }

    const updates = {
      observations: payload.observations,
      term: payload.term,
      academicYear: payload.academicYear !== undefined ? Number(payload.academicYear) : undefined,
      updatedBy: userId
    };

    if (payload.status) {
      updates.status = normalizeStatusToDb(payload.status);
    }

    await reportCardRepository.update(reportCardId, updates);

    if (Array.isArray(payload.grades)) {
      const normalizedGrades = payload.grades.map(normalizeGradePayload);
      const persistedGrades = await Promise.all(
        normalizedGrades.map((grade) => reportCardRepository.upsertGrade(reportCardId, grade))
      );
      const idsToKeep = persistedGrades.filter((grade) => grade.id).map((grade) => grade.id);
      await reportCardRepository.deleteGradesNotIn(reportCardId, idsToKeep);
    }

    return this.getReportCard(reportCardId);
  },

  async updateReportCardStatus(reportCardId, userId, status) {
    const existing = await reportCardRepository.findById(reportCardId);
    if (!existing) {
      throw new ApiError(httpStatus.NOT_FOUND, 'El boletín solicitado no existe');
    }

    const normalizedStatus = normalizeStatusToDb(status);
    const updates = {
      status: normalizedStatus,
      updatedBy: userId,
      issuedAt: normalizedStatus === STATUS_MAP.published ? new Date() : existing.issuedAt
    };

    await reportCardRepository.update(reportCardId, updates);
    return this.getReportCard(reportCardId);
  },

  async getReportCardExport(reportCardId) {
    const reportCard = await reportCardRepository.findById(reportCardId);
    if (!reportCard) {
      throw new ApiError(httpStatus.NOT_FOUND, 'El boletín solicitado no existe');
    }

    const grades = await reportCardRepository.listGrades(reportCardId);

    return {
      reportCard: mapReportCard(reportCard),
      grades,
      subjects: reportCard.courseId ? await reportCardRepository.listCourseSubjects(reportCard.courseId) : []
    };
  },

  async listCourseSubjects(courseId) {
    const normalizedCourseId = Number(courseId);
    if (!Number.isInteger(normalizedCourseId) || normalizedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    await ensureCourseExists(normalizedCourseId);
    const subjects = await reportCardRepository.listCourseSubjects(normalizedCourseId);

    return subjects.map((subject) => ({
      id: Number(subject.id),
      name: subject.name
    }));
  },

  async getLatestTermWithGrades({ courseId, studentId, academicYear }) {
    const normalizedCourseId = Number(courseId);
    const normalizedStudentId = Number(studentId);
    const normalizedYear = academicYear ? Number(academicYear) : new Date().getFullYear();

    if (!Number.isInteger(normalizedCourseId) || normalizedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    if (!Number.isInteger(normalizedStudentId) || normalizedStudentId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El alumno es obligatorio');
    }

    await ensureCourseExists(normalizedCourseId);
    await ensureStudentExists(normalizedStudentId);

    const latestTerm = await reportCardRepository.findLatestTermWithGrades(
      normalizedCourseId,
      normalizedStudentId,
      normalizedYear
    );

    return latestTerm;
  },

  async listSubjectsMissingGrades({ courseId, studentId, term, academicYear }) {
    const normalizedCourseId = Number(courseId);
    const normalizedStudentId = Number(studentId);
    const normalizedYear = academicYear ? Number(academicYear) : new Date().getFullYear();
    const normalizedTerm = typeof term === 'string' ? term.trim() : '';

    if (!Number.isInteger(normalizedCourseId) || normalizedCourseId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El curso es obligatorio');
    }

    if (!Number.isInteger(normalizedStudentId) || normalizedStudentId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El alumno es obligatorio');
    }

    if (!normalizedTerm) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El período es obligatorio');
    }

    await ensureCourseExists(normalizedCourseId);
    await ensureStudentExists(normalizedStudentId);

    const subjects = await reportCardRepository.listSubjectsMissingGrades({
      courseId: normalizedCourseId,
      studentId: normalizedStudentId,
      term: normalizedTerm,
      academicYear: normalizedYear
    });

    return subjects;
  }
};
