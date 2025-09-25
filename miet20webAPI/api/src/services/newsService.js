import httpStatus from 'http-status';
import { randomUUID } from 'crypto';
import { newsRepository } from '../repositories/newsRepository.js';
import { userRepository } from '../repositories/userRepository.js';
import { ApiError } from '../utils/ApiError.js';
import { saveNewsCover, deleteStoredFile } from '../utils/fileStorage.js';
import { scanBufferForThreats } from '../utils/antivirusScanner.js';

const ALLOWED_STATUS = new Set(['draft', 'scheduled', 'published', 'archived']);
const ALLOWED_COVER_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const ALLOWED_COVER_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp'];
const MAX_COVER_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

const ensureValidStatus = (status) => {
  if (!ALLOWED_STATUS.has(status)) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'Estado de noticia inválido');
  }
};

const normalizeAudience = (audience = {}) => ({
  roles: Array.isArray(audience.roles) ? audience.roles.map((role) => String(role)).filter((role) => role !== '') : [],
  courses: Array.isArray(audience.courses)
    ? audience.courses
        .map((course) => Number(course))
        .filter((course) => Number.isFinite(course) && course > 0)
    : [],
  users: Array.isArray(audience.users)
    ? audience.users
        .map((user) => Number(user))
        .filter((user) => Number.isFinite(user) && user > 0)
    : []
});

const normalizeNews = (news) => {
  if (!news) {
    return null;
  }

  return {
    id: news.id,
    title: news.title,
    content: news.content,
    status: news.status,
    requiresConfirmation: Boolean(news.requiresConfirmation),
    publishFrom: news.publishFrom,
    publishUntil: news.publishUntil,
    audience: normalizeAudience(news.audience),
    coverUrl: news.coverUrl,
    createdBy: news.createdBy,
    updatedBy: news.updatedBy,
    createdAt: news.createdAt,
    updatedAt: news.updatedAt
  };
};

const ensureNewsExists = async (newsId, { includeArchived = false } = {}) => {
  const existing = await newsRepository.findById(newsId, { includeArchived });
  if (!existing) {
    throw new ApiError(httpStatus.NOT_FOUND, 'La noticia solicitada no existe');
  }
  return existing;
};

const ensureUserHasNewsPermission = async (userId) => {
  const user = await userRepository.findById(userId);

  if (!user) {
    throw new ApiError(httpStatus.NOT_FOUND, 'Usuario no encontrado');
  }

  if (!user.permNoticia) {
    throw new ApiError(httpStatus.FORBIDDEN, 'No tiene permisos para administrar noticias');
  }

  return user;
};

export const newsService = {
  async listNews(userId, filters) {
    await ensureUserHasNewsPermission(userId);

    if (filters.status) {
      ensureValidStatus(filters.status);
    }

    const result = await newsRepository.list(filters);
    return {
      items: result.items.map(normalizeNews),
      pagination: result.pagination
    };
  },

  async listPublicNews(filters) {
    const result = await newsRepository.listPublished(filters);
    return {
      items: result.items.map((item) => ({
        id: item.id,
        title: item.title,
        content: item.content,
        coverUrl: item.coverUrl,
        publishFrom: item.publishFrom
      })),
      pagination: result.pagination
    };
  },

  async getNews(userId, newsId) {
    await ensureUserHasNewsPermission(userId);
    const existing = await ensureNewsExists(newsId);
    return normalizeNews(existing);
  },

  async createNews(userId, payload) {
    await ensureUserHasNewsPermission(userId);
    const status = payload.status ?? 'draft';
    ensureValidStatus(status);

    const news = await newsRepository.create({
      title: payload.title.trim(),
      content: payload.content ?? '',
      status,
      requiresConfirmation: Boolean(payload.requiresConfirmation),
      publishFrom: payload.publishFrom ?? null,
      publishUntil: payload.publishUntil ?? null,
      audience: normalizeAudience(payload.audience),
      createdBy: userId,
      updatedBy: userId
    });

    return normalizeNews(news);
  },

  async updateNews(newsId, userId, payload) {
    await ensureUserHasNewsPermission(userId);
    const existing = await ensureNewsExists(newsId);

    if (payload.status) {
      ensureValidStatus(payload.status);
    }

    const updated = await newsRepository.update(newsId, {
      title: payload.title !== undefined ? payload.title.trim() : undefined,
      content: payload.content !== undefined ? payload.content : undefined,
      status: payload.status,
      requiresConfirmation: payload.requiresConfirmation,
      publishFrom: payload.publishFrom ?? undefined,
      publishUntil: payload.publishUntil ?? undefined,
      audience: payload.audience ? normalizeAudience(payload.audience) : undefined,
      updatedBy: userId
    });

    return normalizeNews(updated ?? existing);
  },

  async updateStatus(newsId, userId, status) {
    await ensureUserHasNewsPermission(userId);
    ensureValidStatus(status);

    const existing = await ensureNewsExists(newsId, { includeArchived: true });

    if (existing.status === status) {
      return normalizeNews(existing);
    }

    const updates = {
      status,
      updatedBy: userId
    };

    if (status === 'archived') {
      const archived = await newsRepository.archive(newsId, { userId });
      return normalizeNews(archived);
    }

    const updated = await newsRepository.update(newsId, updates);
    return normalizeNews(updated);
  },

  async deleteNews(newsId, userId) {
    await ensureUserHasNewsPermission(userId);
    await ensureNewsExists(newsId);
    await newsRepository.archive(newsId, { userId });
  },

  async updateCover(newsId, userId, payload) {
    await ensureUserHasNewsPermission(userId);
    const existing = await ensureNewsExists(newsId);

    const coverMime = payload.coverMime;
    if (!ALLOWED_COVER_MIME_TYPES.includes(coverMime)) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Tipo de archivo de portada no permitido');
    }

    if (!payload.coverData) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'No se recibió la imagen de portada');
    }

    const buffer = Buffer.from(payload.coverData, 'base64');
    if (!buffer.length) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La imagen de portada es inválida');
    }

    if (buffer.length > MAX_COVER_SIZE_BYTES) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'La imagen de portada supera el tamaño máximo permitido (2MB)');
    }

    const extension = payload.coverName?.split('.').pop()?.toLowerCase();

    if (extension && !ALLOWED_COVER_EXTENSIONS.includes(extension)) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'Extensión de portada no permitida');
    }

    const fileIdentifier = randomUUID();
    const sanitizedFileName = payload.coverName || `${fileIdentifier}.${extension ?? 'bin'}`;

    await scanBufferForThreats(buffer, {
      fileName: sanitizedFileName,
      context: {
        module: 'news',
        newsId,
        userId
      }
    });

    const stored = await saveNewsCover({
      buffer,
      mimeType: coverMime,
      extension,
      identifier: fileIdentifier
    });

    if (existing.coverStoragePath) {
      await deleteStoredFile(existing.coverStoragePath);
    }

    const updated = await newsRepository.update(newsId, {
      coverUrl: stored.publicUrl,
      coverStoragePath: stored.storagePath,
      updatedBy: userId
    });

    return normalizeNews(updated ?? existing);
  }
};
