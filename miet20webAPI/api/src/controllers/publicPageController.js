import httpStatus from 'http-status';
import { publicPageService } from '../services/publicPageService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listPublishedPages = catchAsync(async (req, res) => {
  const result = await publicPageService.listPublishedPages(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const getPublishedPage = catchAsync(async (req, res) => {
  const result = await publicPageService.getPublishedPage(req.params.slug);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const createPublicPage = catchAsync(async (req, res) => {
  const result = await publicPageService.createPage(req.user?.id ?? null, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: result });
});

export const updatePublicPage = catchAsync(async (req, res) => {
  const result = await publicPageService.updatePage(req.params.slug, req.user?.id ?? null, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const deletePublicPage = catchAsync(async (req, res) => {
  await publicPageService.deletePage(req.params.slug);
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Página eliminada correctamente' }
  });
});