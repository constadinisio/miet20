import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'contact_messages';
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
      table.string('nombre', 120).notNullable();
      table.string('email', 180).notNullable();
      table.string('asunto', 180).notNullable();
      table.text('mensaje').notNullable();
      table.string('estado', 40).notNullable().defaultTo('new');
      table.text('adjuntos');
      table.text('metadata');
      table.integer('asignado_a').unsigned();
      table.text('notas');
      table.timestamp('resuelto_en');
      table.timestamp('created_at').defaultTo(db.fn.now());
      table.timestamp('updated_at').defaultTo(db.fn.now());
    });
  }

  initialized = true;
};

const safeParseJSON = (value) => {
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

const parseMetadata = (value) => {
  if (!value) {
    return null;
  }

  try {
    return JSON.parse(value);
  } catch (error) {
    return null;
  }
};

const mapRowToEntity = (row) => {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    name: row.nombre,
    email: row.email,
    subject: row.asunto,
    message: row.mensaje,
    status: row.estado,
    attachments: safeParseJSON(row.adjuntos),
    metadata: parseMetadata(row.metadata),
    assignedTo: row.asignado_a,
    notes: row.notas,
    resolvedAt: row.resuelto_en,
    createdAt: row.created_at,
    updatedAt: row.updated_at
  };
};

const serializeAttachments = (attachments) =>
  attachments && attachments.length ? JSON.stringify(attachments) : null;

const serializeMetadata = (metadata) => (metadata ? JSON.stringify(metadata) : null);

export const contactMessageRepository = {
  async create(payload) {
    await ensureTable();
    const record = await insertAndFetch(TABLE_NAME, {
      nombre: payload.name,
      email: payload.email,
      asunto: payload.subject,
      mensaje: payload.message,
      estado: payload.status ?? 'new',
      adjuntos: serializeAttachments(payload.attachments),
      metadata: serializeMetadata(payload.metadata),
      asignado_a: payload.assignedTo ?? null,
      notas: payload.notes ?? null,
      resuelto_en: payload.resolvedAt ?? null
    });

    return mapRowToEntity(record);
  },

  async update(id, payload) {
    await ensureTable();
    const updates = {
      ...(payload.name !== undefined ? { nombre: payload.name } : {}),
      ...(payload.email !== undefined ? { email: payload.email } : {}),
      ...(payload.subject !== undefined ? { asunto: payload.subject } : {}),
      ...(payload.message !== undefined ? { mensaje: payload.message } : {}),
      ...(payload.status !== undefined ? { estado: payload.status } : {}),
      ...(payload.attachments !== undefined
        ? { adjuntos: serializeAttachments(payload.attachments) }
        : {}),
      ...(payload.metadata !== undefined ? { metadata: serializeMetadata(payload.metadata) } : {}),
      ...(payload.assignedTo !== undefined ? { asignado_a: payload.assignedTo ?? null } : {}),
      ...(payload.notes !== undefined ? { notas: payload.notes ?? null } : {}),
      ...(payload.resolvedAt !== undefined ? { resuelto_en: payload.resolvedAt ?? null } : {}),
      updated_at: getDatabase().fn.now()
    };

    const updated = await updateAndFetch(TABLE_NAME, id, updates);
    return mapRowToEntity(updated);
  },

  async findById(id) {
    await ensureTable();
    const record = await getDatabase()(TABLE_NAME).where({ id }).first();
    return mapRowToEntity(record);
  },

  async list(filters = {}) {
    await ensureTable();
    const db = getDatabase();
    const page = Number(filters.page) > 0 ? Number(filters.page) : 1;
    const limit = Number(filters.limit) > 0 ? Math.min(Number(filters.limit), 50) : 20;
    const offset = (page - 1) * limit;

    const baseQuery = db(TABLE_NAME);

    if (filters.status) {
      baseQuery.where('estado', filters.status);
    }

    if (filters.assignedTo) {
      baseQuery.where('asignado_a', Number(filters.assignedTo));
    }

    if (filters.search) {
      baseQuery.andWhere((builder) => {
        builder
          .where('nombre', 'like', `%${filters.search}%`)
          .orWhere('email', 'like', `%${filters.search}%`)
          .orWhere('asunto', 'like', `%${filters.search}%`);
      });
    }

    const countQuery = baseQuery.clone().clearSelect().clearOrder().count({ total: '*' }).first();
    const rows = await baseQuery.clone().orderBy('created_at', 'desc').offset(offset).limit(limit);
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
  }
};
