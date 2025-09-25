import httpStatus from 'http-status';
import { reportCardService } from '../services/reportCardService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listReportCards = catchAsync(async (req, res) => {
  const reportCards = await reportCardService.listReportCards(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: reportCards });
});

export const getReportCard = catchAsync(async (req, res) => {
  const reportCardId = Number(req.params.reportCardId);
  const reportCard = await reportCardService.getReportCard(reportCardId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: reportCard });
});

export const createReportCard = catchAsync(async (req, res) => {
  const reportCard = await reportCardService.createReportCard(Number(req.user.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: reportCard });
});

export const updateReportCard = catchAsync(async (req, res) => {
  const reportCardId = Number(req.params.reportCardId);
  const reportCard = await reportCardService.updateReportCard(reportCardId, Number(req.user.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: reportCard });
});

export const updateReportCardStatus = catchAsync(async (req, res) => {
  const reportCardId = Number(req.params.reportCardId);
  const reportCard = await reportCardService.updateReportCardStatus(reportCardId, Number(req.user.id), req.body.status);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: reportCard });
});

export const getReportCardExport = catchAsync(async (req, res) => {
  const reportCardId = Number(req.params.reportCardId);
  const exportData = await reportCardService.getReportCardExport(reportCardId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: exportData });
});

export const listReportCardCourseSubjects = catchAsync(async (req, res) => {
  const courseId = Number(req.query.courseId);
  const subjects = await reportCardService.listCourseSubjects(courseId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: subjects });
});

export const getReportCardLatestTerm = catchAsync(async (req, res) => {
  const latestTerm = await reportCardService.getLatestTermWithGrades({
    courseId: req.query.courseId,
    studentId: req.query.studentId,
    academicYear: req.query.academicYear
  });

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: { term: latestTerm } });
});

export const listReportCardMissingSubjects = catchAsync(async (req, res) => {
  const missingSubjects = await reportCardService.listSubjectsMissingGrades({
    courseId: req.query.courseId,
    studentId: req.query.studentId,
    term: req.query.term,
    academicYear: req.query.academicYear
  });

  sendSuccess({ req, res, statusCode: httpStatus.OK, data: missingSubjects });
});
