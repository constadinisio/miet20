import httpStatus from 'http-status';
import { randomUUID } from 'crypto';
import { galleryRepository } from '../repositories/galleryRepository.js';
import { userRepository } from '../repositories/userRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { saveGalleryMedia } from '../utils/fileStorage.js';
import { scanBufferForThreats } from '../utils/antivirusScanner.js';

const ALLOWED_MEDIA_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const ALLOWED_MEDIA_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp'];
const MAX_MEDIA_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

const normalizeItem = (item) => {
  if (!item) {
    return null;
  }

  return {
    id: item.id,
    category: item.category,
    author: item.author,
    description: item.description,
    fileUrl: item.fileUrl,
    uploadedAt: item.uploadedAt
  };
};

const ensureGalleryPermission = async (userId) => {
  const user = await userRepository.findById(userId);
  if (!user) {
    throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
  }

  if (!user.permSubidaArch) {
    throw new ApiError(httpStatus.FORBIDDEN, 'No tiene permisos para administrar la galería');
  }

  return user;
};

export const galleryService = {
  async listCategories() {
    const categories = await galleryRepository.listCategories();
    return categories.map((category) => ({
      key: category.category,
      name: category.category,
      itemsCount: category.itemsCount,
      coverUrl: category.coverUrl,
      lastUploadedAt: category.lastUploadedAt
    }));
  },

  async listItems(filters = {}) {
    const result = await galleryRepository.listItems(filters);
    return {
      items: result.items.map(normalizeItem),
      pagination: result.pagination
    };
  },

  async createItem(userId, payload) {
    await ensureGalleryPermission(userId);

    const category = payload.category?.trim();
    if (!category) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La categoría es obligatoria');
    }

    if (!payload.fileData) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La imagen es obligatoria');
    }

    const fileMime = payload.fileMime;
    if (!ALLOWED_MEDIA_MIME_TYPES.includes(fileMime)) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Tipo de archivo no permitido');
    }

    const buffer = Buffer.from(payload.fileData, 'base64');
    if (!buffer.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El archivo de imagen es inválido');
    }

    if (buffer.length > MAX_MEDIA_SIZE_BYTES) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La imagen supera el tamaño máximo permitido (2MB)');
    }

    const identifier = randomUUID();
    const extension = payload.fileName?.split('.').pop()?.toLowerCase();

    if (extension && !ALLOWED_MEDIA_EXTENSIONS.includes(extension)) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Extensión de archivo no permitida');
    }

    await scanBufferForThreats(buffer, {
      fileName: payload.fileName || `${identifier}.${extension ?? 'bin'}`,
      context: {
        module: 'gallery',
        userId,
        category
      }
    });

    const stored = await saveGalleryMedia({
      buffer,
      mimeType: fileMime,
      extension,
      identifier
    });

    const record = await galleryRepository.create({
      category,
      fileName: stored.fileName,
      fileUrl: stored.publicUrl,
      fileStoragePath: stored.storagePath,
      author: payload.author.trim(),
      description: payload.description.trim(),
      uploadedBy: userId
    });

    return normalizeItem(record);
  }
};
