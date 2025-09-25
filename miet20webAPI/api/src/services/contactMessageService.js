import httpStatus from 'http-status';
import { contactMessageRepository } from '../repositories/contactMessageRepository.js';
import { ApiError } from '../utils/ApiError.js';

const ALLOWED_STATUS = new Set(['new', 'in_progress', 'resolved']);

const sanitizeAttachments = (attachments) => {
  if (!Array.isArray(attachments)) {
    return [];
  }

  return attachments
    .map((attachment) => {
      if (typeof attachment === 'number') {
        return { id: Number(attachment) };
      }

      if (typeof attachment === 'string') {
        const numeric = Number(attachment);
        if (Number.isFinite(numeric) && numeric > 0) {
          return { id: numeric };
        }
        return null;
      }

      if (attachment && typeof attachment === 'object') {
        const numeric = Number(attachment.id);
        if (Number.isFinite(numeric) && numeric > 0) {
          return {
            id: numeric,
            name: attachment.name ?? null
          };
        }
      }

      return null;
    })
    .filter(Boolean);
};

const sanitizeMetadata = (metadata) => {
  if (!metadata || Array.isArray(metadata) || typeof metadata !== 'object') {
    return null;
  }

  return metadata;
};

const normalizeMessage = (message) => {
  if (!message) {
    return null;
  }

  return {
    id: message.id,
    name: message.name,
    email: message.email,
    subject: message.subject,
    message: message.message,
    status: message.status,
    attachments: message.attachments ?? [],
    metadata: message.metadata,
    assignedTo: message.assignedTo,
    notes: message.notes,
    resolvedAt: message.resolvedAt,
    createdAt: message.createdAt,
    updatedAt: message.updatedAt
  };
};

const ensureValidStatus = (status) => {
  if (!ALLOWED_STATUS.has(status)) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'Estado de mensaje inválido');
  }
};

const ensureMessageExists = async (messageId) => {
  const message = await contactMessageRepository.findById(messageId);
  if (!message) {
    throw new ApiError(httpStatus.NOT_FOUND, 'El mensaje solicitado no existe');
  }
  return message;
};

export const contactMessageService = {
  async submitMessage(payload) {
    const record = await contactMessageRepository.create({
      name: payload.name.trim(),
      email: payload.email.trim().toLowerCase(),
      subject: payload.subject.trim(),
      message: payload.message.trim(),
      status: 'new',
      attachments: sanitizeAttachments(payload.attachments),
      metadata: sanitizeMetadata(payload.metadata)
    });

    return normalizeMessage(record);
  },

  async listMessages(filters) {
    if (filters.status) {
      ensureValidStatus(filters.status);
    }

    const result = await contactMessageRepository.list(filters);
    return {
      items: result.items.map(normalizeMessage),
      pagination: result.pagination
    };
  },

  async getMessage(messageId) {
    const message = await ensureMessageExists(messageId);
    return normalizeMessage(message);
  },

  async updateMessage(messageId, payload) {
    const existing = await ensureMessageExists(messageId);

    const updates = {};

    if (payload.status) {
      ensureValidStatus(payload.status);
      updates.status = payload.status;
      if (payload.status === 'resolved') {
        updates.resolvedAt = existing.resolvedAt ?? new Date();
      } else if (existing.resolvedAt) {
        updates.resolvedAt = null;
      }
    }

    if (payload.attachments !== undefined) {
      updates.attachments = sanitizeAttachments(payload.attachments);
    }

    if (payload.notes !== undefined) {
      updates.notes = payload.notes?.trim() ?? null;
    }

    if (payload.metadata !== undefined) {
      updates.metadata = sanitizeMetadata(payload.metadata);
    }

    if (payload.assignedTo !== undefined) {
      const numeric = Number(payload.assignedTo);
      updates.assignedTo = Number.isFinite(numeric) && numeric > 0 ? numeric : null;
    }

    const updated = await contactMessageRepository.update(messageId, updates);
    return normalizeMessage(updated ?? existing);
  }
};
