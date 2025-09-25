import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'archivos';
let initialized = false;

const ensureTable = async () => {
  if (initialized) {
    return;
  }

  const db = getDatabase();
  const exists = await db.schema.hasTable(TABLE_NAME);

  if (!exists) {
    throw new Error(
      'La tabla "archivos" no existe. Ejecuta las migraciones de base de datos (`npm run migrate`) antes de continuar.'
    );
  }

  initialized = true;
};

const mapRowToEntity = (row) => {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    originalName: row.nombre_original,
    mimeType: row.mime_type,
    size: Number(row.tamanio_bytes),
    storagePath: row.ruta_storage,
    publicUrl: row.url_publica,
    isPublic: Boolean(row.es_publico),
    uploadedBy: row.subido_por,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    deletedAt: row.deleted_at
  };
};

export const fileRepository = {
  async create(payload) {
    await ensureTable();
    const record = await insertAndFetch(TABLE_NAME, {
      nombre_original: payload.originalName,
      mime_type: payload.mimeType,
      tamanio_bytes: payload.size,
      ruta_storage: payload.storagePath,
      url_publica: payload.publicUrl,
      es_publico: Boolean(payload.isPublic),
      subido_por: payload.uploadedBy ?? null
    });

    return mapRowToEntity(record);
  },

  async findById(id, { includeDeleted = false } = {}) {
    await ensureTable();
    const query = getDatabase()(TABLE_NAME).where({ id });

    if (!includeDeleted) {
      query.whereNull('deleted_at');
    }

    const record = await query.first();
    return mapRowToEntity(record);
  },

  async delete(id) {
    await ensureTable();
    const updated = await updateAndFetch(TABLE_NAME, id, {
      deleted_at: getDatabase().fn.now(),
      updated_at: getDatabase().fn.now()
    });
    return mapRowToEntity(updated);
  }
};