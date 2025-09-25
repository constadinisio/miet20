import { getDatabase } from '../config/database.js';
import { insertAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'imagenes';

let isTableInitialized = false;

const ensureTable = async () => {
  if (isTableInitialized) {
    return;
  }

  const db = getDatabase();
  const exists = await db.schema.hasTable(TABLE_NAME);

  if (!exists) {
    throw new Error(
      'La tabla "imagenes" no existe. Ejecuta las migraciones de base de datos (`npm run migrate`) antes de continuar.'
    );
  } else {
    const columns = [
      { name: 'archivo_url', builder: (table) => table.string('archivo_url', 512) },
      { name: 'archivo_path', builder: (table) => table.string('archivo_path', 512) },
      { name: 'subido_por', builder: (table) => table.integer('subido_por').unsigned() },
      { name: 'fecha_subida', builder: (table) => table.timestamp('fecha_subida').defaultTo(db.fn.now()) }
    ];

    // eslint-disable-next-line no-restricted-syntax
    for (const column of columns) {
      // eslint-disable-next-line no-await-in-loop
      const hasColumn = await db.schema.hasColumn(TABLE_NAME, column.name);
      if (!hasColumn) {
        // eslint-disable-next-line no-await-in-loop
        await db.schema.alterTable(TABLE_NAME, (table) => {
          column.builder(table);
        });
      }
    }
  }

  isTableInitialized = true;
};

const resolveFileUrl = (row) => {
  if (!row) {
    return null;
  }

  if (row.archivo_url) {
    return row.archivo_url;
  }

  if (row.categoria && row.archivo) {
    const encodedCategory = encodeURIComponent(row.categoria);
    const encodedFile = encodeURIComponent(row.archivo);
    return `/galeriaUtils/imagenes/${encodedCategory}/${encodedFile}`;
  }

  return null;
};

const mapRowToEntity = (row) => {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    category: row.categoria,
    fileName: row.archivo,
    fileUrl: resolveFileUrl(row),
    fileStoragePath: row.archivo_path,
    author: row.autor,
    description: row.descripcion,
    uploadedBy: row.subido_por,
    uploadedAt: row.fecha_subida
  };
};

export const galleryRepository = {
  async create(payload) {
    await ensureTable();

    const record = await insertAndFetch(TABLE_NAME, {
      categoria: payload.category,
      archivo: payload.fileName,
      archivo_url: payload.fileUrl,
      archivo_path: payload.fileStoragePath ?? null,
      autor: payload.author,
      descripcion: payload.description,
      subido_por: payload.uploadedBy ?? null
    });

    return mapRowToEntity(record);
  },

  async listCategories() {
    await ensureTable();
    const db = getDatabase();

    const aggregates = await db(TABLE_NAME)
      .select('categoria')
      .count({ total: '*' })
      .max({ lastUploadedAt: 'fecha_subida' })
      .groupBy('categoria')
      .orderBy('categoria', 'asc');

    if (!aggregates.length) {
      return [];
    }

    const latestItems = await db(TABLE_NAME)
      .select('categoria', 'archivo', 'archivo_url', 'archivo_path', 'fecha_subida')
      .whereIn(
        'categoria',
        aggregates.map((row) => row.categoria)
      )
      .orderBy('fecha_subida', 'desc');

    const latestByCategory = new Map();
    latestItems.forEach((item) => {
      if (!latestByCategory.has(item.categoria)) {
        latestByCategory.set(item.categoria, item);
      }
    });

    return aggregates.map((row) => {
      const latest = latestByCategory.get(row.categoria);
      const entity = mapRowToEntity(latest);

      return {
        category: row.categoria,
        itemsCount: Number(row.total ?? 0),
        lastUploadedAt: row.lastUploadedAt,
        coverUrl: entity?.fileUrl ?? null
      };
    });
  },

  async listItems(filters = {}) {
    await ensureTable();
    const db = getDatabase();

    const page = Number(filters.page) > 0 ? Number(filters.page) : 1;
    const limit = Number(filters.limit) > 0 ? Math.min(Number(filters.limit), 50) : 12;
    const offset = (page - 1) * limit;

    const baseQuery = db(TABLE_NAME).whereRaw('1 = 1');

    if (filters.category) {
      baseQuery.andWhere('categoria', filters.category);
    }

    if (filters.search) {
      baseQuery.andWhere((builder) => {
        builder
          .where('descripcion', 'like', `%${filters.search}%`)
          .orWhere('autor', 'like', `%${filters.search}%`);
      });
    }

    const countQuery = baseQuery.clone().clearSelect().clearOrder().count({ total: '*' }).first();

    baseQuery.orderBy('fecha_subida', filters.sortDirection === 'asc' ? 'asc' : 'desc');

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
  }
};
