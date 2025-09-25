import httpStatus from 'http-status';
import { galleryService } from '../services/galleryService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listGalleryCategories = catchAsync(async (req, res) => {
  const categories = await galleryService.listCategories();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: { categories } });
});

export const listGalleryItems = catchAsync(async (req, res) => {
  const result = await galleryService.listItems(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const createGalleryItem = catchAsync(async (req, res) => {
  const item = await galleryService.createItem(req.user.id, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: item });
});
