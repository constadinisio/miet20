import httpStatus from 'http-status';
import { notificationService } from '../services/notificationService.js';
import { catchAsync } from '../utils/catchAsync.js';
import { sendSuccess } from '../utils/ApiResponse.js';

export const listNotificationOptions = catchAsync(async (req, res) => {
  const options = await notificationService.listNotificationOptions({
    userId: req.user?.id,
    role: req.user?.role
  });
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: options });
});

export const createGroup = catchAsync(async (req, res) => {
  const group = await notificationService.createGroup({
    ...req.body,
    creador_id: req.user?.id ? Number(req.user.id) : null
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: group });
});

export const listGroups = catchAsync(async (req, res) => {
  const groups = await notificationService.listGroups();
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: groups });
});

export const deleteGroup = catchAsync(async (req, res) => {
  await notificationService.deleteGroup(Number(req.params.groupId));
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { message: 'Grupo eliminado correctamente' }
  });
});

export const listGroupMembers = catchAsync(async (req, res) => {
  const groupId = Number(req.params.groupId);
  const members = await notificationService.listGroupMembers(groupId);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: members });
});

export const sendNotification = catchAsync(async (req, res) => {
  const notification = await notificationService.sendNotification({
    ...req.body,
    remitente_id: req.user?.id
  });
  sendSuccess({ req, res, statusCode: httpStatus.CREATED, data: notification });
});

export const listNotifications = catchAsync(async (req, res) => {
  const notifications = await notificationService.listNotifications(req.user?.id);
  sendSuccess({ req, res, statusCode: httpStatus.OK, data: notifications });
});

export const markRecipientAsRead = catchAsync(async (req, res) => {
  const updated = await notificationService.markRecipientAsRead(
    Number(req.params.recipientId),
    req.user?.id
  );
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { ok: true, recipient: updated }
  });
});

export const confirmRecipient = catchAsync(async (req, res) => {
  const updated = await notificationService.confirmRecipient(
    Number(req.params.recipientId),
    req.user?.id
  );
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: { ok: true, recipient: updated }
  });
});
