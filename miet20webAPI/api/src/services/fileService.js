import { randomUUID } from 'crypto';
import httpStatus from 'http-status';
import { fileRepository } from '../repositories/fileRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { buildSignedFileUrl, deleteStoredFile, saveGenericFile } from '../utils/fileStorage.js';

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

const normalizeFile = (file) => {
  if (!file) {
    return null;
  }

  return {
    id: file.id,
    originalName: file.originalName,
    mimeType: file.mimeType,
    size: file.size,
    publicUrl: file.publicUrl,
    isPublic: Boolean(file.isPublic),
    uploadedBy: file.uploadedBy,
    createdAt: file.createdAt,
    updatedAt: file.updatedAt
  };
};

const ensureFileExists = async (fileId) => {
  const file = await fileRepository.findById(fileId);
  if (!file) {
    throw new ApiError(httpStatus.NOT_FOUND, 'El archivo solicitado no existe');
  }
  return file;
};

export const fileService = {
  async uploadFile(userId, payload) {
    if (!payload.fileData) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El archivo codificado es obligatorio');
    }

    const buffer = Buffer.from(payload.fileData, 'base64');
    if (!buffer.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El archivo proporcionado es inválido');
    }

    if (buffer.length > MAX_FILE_SIZE_BYTES) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El archivo supera el tamaño máximo permitido (5MB)');
    }

    const identifier = randomUUID();
    const stored = await saveGenericFile({
      buffer,
      mimeType: payload.fileMime,
      extension: payload.fileName?.split('.').pop(),
      identifier
    });

    const record = await fileRepository.create({
      originalName: payload.fileName ?? stored.fileName,
      mimeType: payload.fileMime,
      size: stored.size,
      storagePath: stored.storagePath,
      publicUrl: stored.publicUrl,
      isPublic: Boolean(payload.isPublic),
      uploadedBy: userId ?? null
    });

    const signed = buildSignedFileUrl({ publicUrl: record.publicUrl });

    return {
      ...normalizeFile(record),
      signedUrl: signed.url,
      expiresAt: signed.expiresAt
    };
  },

  async getFile(fileId, { user } = {}) {
    const file = await ensureFileExists(fileId);

    if (!file.isPublic && !user) {
      throw new ApiError(httpStatus.UNAUTHORIZED, 'Se requiere autenticación para acceder al archivo');
    }

    const signed = buildSignedFileUrl({ publicUrl: file.publicUrl });
    return {
      ...normalizeFile(file),
      signedUrl: signed.url,
      expiresAt: signed.expiresAt
    };
  },

  async deleteFile(fileId) {
    const file = await ensureFileExists(fileId);
    await deleteStoredFile(file.storagePath);
    await fileRepository.delete(fileId);
  }
};
