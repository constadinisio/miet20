import httpStatus from 'http-status';
import { courseService } from '../services/courseService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const createCourse = catchAsync(async (req, res) => {
  const course = await courseService.createCourse(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: course });
});

export const listCourses = catchAsync(async (req, res) => {
  const courses = await courseService.listCourses(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: courses });
});

export const getCourse = catchAsync(async (req, res) => {
  const course = await courseService.getCourse(Number(req.params.id));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: course });
});

export const updateCourse = catchAsync(async (req, res) => {
  const course = await courseService.updateCourse(Number(req.params.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: course });
});

export const deleteCourse = catchAsync(async (req, res) => {
  await courseService.deleteCourse(Number(req.params.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Curso eliminado correctamente' }
  });
});

export const assignSubject = catchAsync(async (req, res) => {
  const assignment = await courseService.assignSubject({
    cursoId: Number(req.params.id),
    materiaId: Number(req.body.materia_id),
    profesorId: req.body.profesor_id ? Number(req.body.profesor_id) : null,
    esContraturno: req.body.es_contraturno
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: assignment });
});

export const assignTeacher = catchAsync(async (req, res) => {
  const assignment = await courseService.assignTeacher({
    cursoId: Number(req.params.id),
    materiaId: Number(req.body.materia_id),
    profesorId: Number(req.body.profesor_id),
    esContraturno: req.body.es_contraturno
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: assignment });
});

export const removeAssignment = catchAsync(async (req, res) => {
  await courseService.removeAssignment(Number(req.params.assignmentId));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Asignación eliminada correctamente' }
  });
});

export const listCourseAssignments = catchAsync(async (req, res) => {
  const filters = {
    profesor_id: req.query.profesor_id ? Number(req.query.profesor_id) : undefined,
    curso_id: req.query.curso_id ? Number(req.query.curso_id) : undefined,
    materia_id: req.query.materia_id ? Number(req.query.materia_id) : undefined,
    estado: req.query.estado
  };

  if (req.user?.role === 'profesor') {
    filters.profesor_id = Number(req.user.id);
  }

  const assignments = await courseService.listAssignments(filters);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: assignments });
});

export const getCourseAssignment = catchAsync(async (req, res) => {
  const assignment = await courseService.getAssignment(Number(req.params.assignmentId));
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: assignment });
});

export const listCourseStudents = catchAsync(async (req, res) => {
  const students = await courseService.listCourseStudents(Number(req.params.id), {
    teacherId: req.user?.role === 'profesor' ? Number(req.user.id) : null
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: students });
});
