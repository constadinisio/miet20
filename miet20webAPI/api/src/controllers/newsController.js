import httpStatus from 'http-status';
import { newsService } from '../services/newsService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listNews = catchAsync(async (req, res) => {
  const news = await newsService.listNews(Number(req.user.id), req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: news });
});

export const listPublicNews = catchAsync(async (req, res) => {
  const news = await newsService.listPublicNews(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: news });
});

export const getNews = catchAsync(async (req, res) => {
  const newsId = Number(req.params.newsId);
  const news = await newsService.getNews(Number(req.user.id), newsId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: news });
});

export const createNews = catchAsync(async (req, res) => {
  const news = await newsService.createNews(Number(req.user.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: news });
});

export const updateNews = catchAsync(async (req, res) => {
  const newsId = Number(req.params.newsId);
  const news = await newsService.updateNews(newsId, Number(req.user.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: news });
});

export const updateNewsStatus = catchAsync(async (req, res) => {
  const newsId = Number(req.params.newsId);
  const news = await newsService.updateStatus(newsId, Number(req.user.id), req.body.status);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: news });
});

export const deleteNews = catchAsync(async (req, res) => {
  const newsId = Number(req.params.newsId);
  await newsService.deleteNews(newsId, Number(req.user.id));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Noticia eliminada correctamente' }
  });
});

export const updateNewsCover = catchAsync(async (req, res) => {
  const newsId = Number(req.params.newsId);
  const news = await newsService.updateCover(newsId, Number(req.user.id), req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: news });
});
