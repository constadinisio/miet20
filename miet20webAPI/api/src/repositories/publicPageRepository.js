import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'public_pages';
let initialized = false;

const ensureTable = async () => {
  if (initialized) {
    return;
  }

  const db = getDatabase();
  const exists = await db.schema.hasTable(TABLE_NAME);

  if (!exists) {
    await db.schema.createTable(TABLE_NAME, (table) => {
      table.increments('id').primary();
      table.string('slug', 120).notNullable().unique();
      table.string('titulo', 180).notNullable();
      table.text('contenido').notNullable();
      table.string('estado', 30).notNullable().defaultTo('draft');
      table.string('seo_titulo', 180);
      table.string('seo_descripcion', 255);
      table.text('secciones');
      table.text('adjuntos');
      table.timestamp('publicado_en');
      table.integer('creado_por').unsigned();
      table.integer('actualizado_por').unsigned();
      table.timestamp('created_at').defaultTo(db.fn.now());
      table.timestamp('updated_at').defaultTo(db.fn.now());
      table.timestamp('archived_at');
    });
  }

  initialized = true;
};

const safeParseArray = (value) => {
  if (!value) {
    return [];
  }

  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
};

const mapRowToEntity = (row) => {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    slug: row.slug,
    title: row.titulo,
    content: row.contenido,
    status: row.estado,
    seo: {
      title: row.seo_titulo,
      description: row.seo_descripcion
    },
    sections: safeParseArray(row.secciones),
    attachments: safeParseArray(row.adjuntos),
    publishedAt: row.publicado_en,
    createdBy: row.creado_por,
    updatedBy: row.actualizado_por,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    archivedAt: row.archived_at
  };
};

const serializeArray = (value) => (value && value.length ? JSON.stringify(value) : null);

export const publicPageRepository = {
  async create(payload) {
    await ensureTable();
    const record = await insertAndFetch(TABLE_NAME, {
      slug: payload.slug,
      titulo: payload.title,
      contenido: payload.content,
      estado: payload.status ?? 'draft',
      seo_titulo: payload.seo?.title ?? null,
      seo_descripcion: payload.seo?.description ?? null,
      secciones: serializeArray(payload.sections),
      adjuntos: serializeArray(payload.attachments),
      publicado_en: payload.publishedAt ?? null,
      creado_por: payload.createdBy ?? null,
      actualizado_por: payload.updatedBy ?? null
    });

    return mapRowToEntity(record);
  },

  async updateBySlug(slug, payload) {
    await ensureTable();
    const updates = {
      ...(payload.title !== undefined ? { titulo: payload.title } : {}),
      ...(payload.content !== undefined ? { contenido: payload.content } : {}),
      ...(payload.status !== undefined ? { estado: payload.status } : {}),
      ...(payload.seo !== undefined
        ? {
            seo_titulo: payload.seo?.title ?? null,
            seo_descripcion: payload.seo?.description ?? null
          }
        : {}),
      ...(payload.sections !== undefined ? { secciones: serializeArray(payload.sections) } : {}),
      ...(payload.attachments !== undefined ? { adjuntos: serializeArray(payload.attachments) } : {}),
      ...(payload.publishedAt !== undefined ? { publicado_en: payload.publishedAt ?? null } : {}),
      ...(payload.updatedBy !== undefined ? { actualizado_por: payload.updatedBy ?? null } : {}),
      updated_at: getDatabase().fn.now()
    };

    const updated = await updateAndFetch(TABLE_NAME, { slug }, updates, { primaryKey: 'slug' });
    return mapRowToEntity(updated);
  },

  async findBySlug(slug, { includeArchived = false } = {}) {
    await ensureTable();
    const query = getDatabase()(TABLE_NAME).where({ slug });

    if (!includeArchived) {
      query.whereNull('archived_at');
    }

    const record = await query.first();
    return mapRowToEntity(record);
  },

  async listPublished(filters = {}) {
    await ensureTable();
    const db = getDatabase();
    const page = Number(filters.page) > 0 ? Number(filters.page) : 1;
    const limit = Number(filters.limit) > 0 ? Math.min(Number(filters.limit), 50) : 20;
    const offset = (page - 1) * limit;

    const baseQuery = db(TABLE_NAME)
      .where({ estado: 'published' })
      .whereNull('archived_at');

    if (filters.search) {
      baseQuery.andWhere((builder) => {
        builder
          .where('titulo', 'like', `%${filters.search}%`)
          .orWhere('contenido', 'like', `%${filters.search}%`);
      });
    }

    const countQuery = baseQuery.clone().clearSelect().clearOrder().count({ total: '*' }).first();
    const rows = await baseQuery.clone().orderBy('publicado_en', 'desc').offset(offset).limit(limit);
    const totalResult = await countQuery;
    const total = Number(totalResult?.total ?? 0);
    const totalPages = Math.max(Math.ceil(total / limit), 1);

    return {
      items: rows.map(mapRowToEntity),
      pagination: {
        page,
        limit,
        total,
        totalPages
      }
    };
  },

  async archive(slug) {
    await ensureTable();
    const updated = await updateAndFetch(
      TABLE_NAME,
      { slug },
      {
        archived_at: getDatabase().fn.now(),
        updated_at: getDatabase().fn.now()
      },
      { primaryKey: 'slug' }
    );
    return mapRowToEntity(updated);
  }
};
