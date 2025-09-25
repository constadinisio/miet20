import httpStatus from 'http-status';
import { contactMessageService } from '../services/contactMessageService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const submitContactMessage = catchAsync(async (req, res) => {
  const result = await contactMessageService.submitMessage(req.body);
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: result });
});

export const listContactMessages = catchAsync(async (req, res) => {
  const result = await contactMessageService.listMessages(req.query);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const getContactMessage = catchAsync(async (req, res) => {
  const messageId = Number(req.params.messageId);
  const result = await contactMessageService.getMessage(messageId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});

export const updateContactMessage = catchAsync(async (req, res) => {
  const messageId = Number(req.params.messageId);
  const result = await contactMessageService.updateMessage(messageId, req.body);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: result });
});