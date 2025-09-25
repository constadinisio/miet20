import httpStatus from 'http-status';
import { publicPageRepository } from '../repositories/publicPageRepository.js';
import { ApiError } from '../utils/ApiError.js';

const SLUG_REGEX = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const ALLOWED_STATUS = new Set(['draft', 'published']);

const normalizePage = (page) => {
  if (!page) {
    return null;
  }

  return {
    id: page.id,
    slug: page.slug,
    title: page.title,
    content: page.content,
    status: page.status,
    seo: page.seo,
    sections: page.sections ?? [],
    attachments: page.attachments ?? [],
    publishedAt: page.publishedAt,
    createdAt: page.createdAt,
    updatedAt: page.updatedAt
  };
};

const sanitizeAttachments = (attachments) => {
  if (!Array.isArray(attachments)) {
    return [];
  }

  return attachments
    .map((attachment) => {
      if (typeof attachment === 'number') {
        return { id: attachment };
      }

      if (typeof attachment === 'string' && /^\d+$/.test(attachment)) {
        return { id: Number(attachment) };
      }

      if (attachment && typeof attachment === 'object') {
        const numeric = Number(attachment.id);
        if (Number.isFinite(numeric) && numeric > 0) {
          return { id: numeric, name: attachment.name ?? null };
        }
      }

      return null;
    })
    .filter(Boolean);
};

const sanitizeSections = (sections) => {
  if (!Array.isArray(sections)) {
    return [];
  }

  return sections
    .map((section) => {
      if (!section || typeof section !== 'object') {
        return null;
      }

      return {
        title: section.title ?? null,
        content: section.content ?? null,
        order: Number.isFinite(Number(section.order)) ? Number(section.order) : null
      };
    })
    .filter(Boolean);
};

const sanitizeSeo = (seo) => {
  if (!seo || typeof seo !== 'object' || Array.isArray(seo)) {
    return null;
  }

  return {
    title: seo.title ?? null,
    description: seo.description ?? null
  };
};

const ensureSlugValid = (slug) => {
  if (!SLUG_REGEX.test(slug)) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'El slug debe usar solo minúsculas, números y guiones');
  }
};

const ensureStatusValid = (status) => {
  if (status && !ALLOWED_STATUS.has(status)) {
    throw new ApiError(httpStatus.BAD_REQUEST, 'Estado de página inválido');
  }
};

const ensurePageExists = async (slug) => {
  const page = await publicPageRepository.findBySlug(slug);
  if (!page) {
    throw new ApiError(httpStatus.NOT_FOUND, 'La página solicitada no existe');
  }
  return page;
};

export const publicPageService = {
  async listPublishedPages(filters) {
    const result = await publicPageRepository.listPublished(filters);
    return {
      items: result.items.map((item) => ({
        slug: item.slug,
        title: item.title,
        excerpt: item.content?.slice(0, 180) ?? '',
        publishedAt: item.publishedAt,
        seo: item.seo
      })),
      pagination: result.pagination
    };
  },

  async getPublishedPage(slug) {
    const page = await publicPageRepository.findBySlug(slug);
    if (!page || page.status !== 'published') {
      throw new ApiError(httpStatus.NOT_FOUND, 'La página solicitada no se encuentra publicada');
    }

    return normalizePage(page);
  },

  async createPage(userId, payload) {
    const slug = payload.slug.trim();
    ensureSlugValid(slug);
    ensureStatusValid(payload.status ?? 'draft');

    const existing = await publicPageRepository.findBySlug(slug, { includeArchived: true });
    if (existing) {
      throw new ApiError(httpStatus.CONFLICT, 'Ya existe una página con el slug proporcionado');
    }

    const status = payload.status ?? 'draft';
    const publishedAt = status === 'published' ? new Date() : null;

    const record = await publicPageRepository.create({
      slug,
      title: payload.title.trim(),
      content: payload.content.trim(),
      status,
      seo: sanitizeSeo(payload.seo),
      sections: sanitizeSections(payload.sections),
      attachments: sanitizeAttachments(payload.attachments),
      publishedAt,
      createdBy: userId,
      updatedBy: userId
    });

    return normalizePage(record);
  },

  async updatePage(slug, userId, payload) {
    ensureStatusValid(payload.status);
    const existing = await ensurePageExists(slug);

    const updates = {
      title: payload.title !== undefined ? payload.title.trim() : undefined,
      content: payload.content !== undefined ? payload.content.trim() : undefined,
      status: payload.status,
      seo: payload.seo !== undefined ? sanitizeSeo(payload.seo) : undefined,
      sections: payload.sections !== undefined ? sanitizeSections(payload.sections) : undefined,
      attachments:
        payload.attachments !== undefined ? sanitizeAttachments(payload.attachments) : undefined,
      publishedAt:
        payload.status === 'published'
          ? existing.publishedAt ?? new Date()
          : payload.status === 'draft'
          ? null
          : undefined,
      updatedBy: userId
    };

    const updated = await publicPageRepository.updateBySlug(slug, updates);
    return normalizePage(updated ?? existing);
  },

  async deletePage(slug) {
    await ensurePageExists(slug);
    await publicPageRepository.archive(slug);
  }
};
