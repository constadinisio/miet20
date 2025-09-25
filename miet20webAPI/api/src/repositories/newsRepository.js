import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'noticias';

let isTableInitialized = false;

const ensureTable = async () => {
  if (isTableInitialized) {
    return;
  }

  const db = getDatabase();
  const exists = await db.schema.hasTable(TABLE_NAME);

  if (!exists) {
    throw new Error(
      'La tabla "noticias" no existe. Ejecuta las migraciones de base de datos (`npm run migrate`) antes de continuar.'
    );
  }

  isTableInitialized = true;
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

const parseAudience = (row) => ({
  roles: safeParseArray(row.audiencia_roles),
  courses: safeParseArray(row.audiencia_cursos),
  users: safeParseArray(row.audiencia_usuarios)
});

const mapRowToEntity = (row) => {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    title: row.titulo,
    content: row.contenido,
    status: row.estado,
    requiresConfirmation: Boolean(row.requiere_confirmacion),
    publishFrom: row.publicado_desde,
    publishUntil: row.publicado_hasta,
    audience: parseAudience(row),
    coverUrl: row.portada_url,
    coverStoragePath: row.portada_path,
    createdBy: row.creado_por,
    updatedBy: row.actualizado_por,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    archivedAt: row.archived_at
  };
};

const serializeAudience = (audience = {}) => ({
  audiencia_roles: audience.roles && audience.roles.length ? JSON.stringify(audience.roles) : null,
  audiencia_cursos: audience.courses && audience.courses.length ? JSON.stringify(audience.courses) : null,
  audiencia_usuarios: audience.users && audience.users.length ? JSON.stringify(audience.users) : null
});

export const newsRepository = {
  async create(payload) {
    await ensureTable();
    const audience = serializeAudience(payload.audience);
    const record = await insertAndFetch(TABLE_NAME, {
      titulo: payload.title,
      contenido: payload.content,
      estado: payload.status,
      requiere_confirmacion: Boolean(payload.requiresConfirmation),
      publicado_desde: payload.publishFrom ?? null,
      publicado_hasta: payload.publishUntil ?? null,
      ...audience,
      portada_url: payload.coverUrl ?? null,
      portada_path: payload.coverStoragePath ?? null,
      creado_por: payload.createdBy ?? null,
      actualizado_por: payload.updatedBy ?? null
    });

    return mapRowToEntity(record);
  },

  async update(id, payload) {
    await ensureTable();
    const audience = payload.audience ? serializeAudience(payload.audience) : {};

    const updates = {
      ...(payload.title !== undefined ? { titulo: payload.title } : {}),
      ...(payload.content !== undefined ? { contenido: payload.content } : {}),
      ...(payload.status !== undefined ? { estado: payload.status } : {}),
      ...(payload.requiresConfirmation !== undefined
        ? { requiere_confirmacion: Boolean(payload.requiresConfirmation) }
        : {}),
      ...(payload.publishFrom !== undefined ? { publicado_desde: payload.publishFrom ?? null } : {}),
      ...(payload.publishUntil !== undefined ? { publicado_hasta: payload.publishUntil ?? null } : {}),
      ...(payload.coverUrl !== undefined ? { portada_url: payload.coverUrl ?? null } : {}),
      ...(payload.coverStoragePath !== undefined ? { portada_path: payload.coverStoragePath ?? null } : {}),
      ...(payload.updatedBy !== undefined ? { actualizado_por: payload.updatedBy ?? null } : {}),
      updated_at: getDatabase().fn.now(),
      ...audience
    };

    const updated = await updateAndFetch(TABLE_NAME, id, updates);
    return mapRowToEntity(updated);
  },

  async findById(id, { includeArchived = false } = {}) {
    await ensureTable();
    const query = getDatabase()(TABLE_NAME).where({ id });

    if (!includeArchived) {
      query.andWhereNull('archived_at');
    }

    const record = await query.first();
    return mapRowToEntity(record);
  },

  async list(filters = {}) {
    await ensureTable();
    const db = getDatabase();
    const page = Number(filters.page) > 0 ? Number(filters.page) : 1;
    const limit = Number(filters.limit) > 0 ? Math.min(Number(filters.limit), 50) : 20;
    const offset = (page - 1) * limit;

    const baseQuery = db(TABLE_NAME);

    if (!filters.includeArchived) {
      baseQuery.whereNull('archived_at');
    }

    if (filters.status) {
      baseQuery.andWhere('estado', filters.status);
    }

    if (filters.search) {
      baseQuery.andWhere((builder) => {
        builder
          .where('titulo', 'like', `%${filters.search}%`)
          .orWhere('contenido', 'like', `%${filters.search}%`);
      });
    }

    const countQuery = baseQuery.clone().clearSelect().clearOrder().count({ total: '*' }).first();

    if (filters.sortBy === 'published_at') {
      baseQuery.orderBy('publicado_desde', filters.sortDirection === 'asc' ? 'asc' : 'desc');
    } else {
      baseQuery.orderBy('created_at', filters.sortDirection === 'asc' ? 'asc' : 'desc');
    }

    const rows = await baseQuery.clone().offset(offset).limit(limit).select('*');
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

  async listPublished(filters = {}) {
    await ensureTable();
    const db = getDatabase();
    const page = Number(filters.page) > 0 ? Number(filters.page) : 1;
    const limit = Number(filters.limit) > 0 ? Math.min(Number(filters.limit), 50) : 10;
    const offset = (page - 1) * limit;
    const now = db.fn.now();

    const baseQuery = db(TABLE_NAME)
      .where({ estado: 'published' })
      .andWhereNull('archived_at')
      .andWhere((builder) => {
        builder.whereNull('publicado_desde').orWhere('publicado_desde', '<=', now);
      })
      .andWhere((builder) => {
        builder.whereNull('publicado_hasta').orWhere('publicado_hasta', '>=', now);
      });

    const countQuery = baseQuery.clone().clearSelect().clearOrder().count({ total: '*' }).first();
    const rows = await baseQuery.clone().orderBy('publicado_desde', 'desc').offset(offset).limit(limit).select('*');
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

  async archive(id, { userId } = {}) {
    await ensureTable();
    const updates = {
      estado: 'archived',
      archived_at: getDatabase().fn.now(),
      actualizado_por: userId ?? null
    };

    const updated = await updateAndFetch(TABLE_NAME, id, updates);
    return mapRowToEntity(updated);
  }
};
