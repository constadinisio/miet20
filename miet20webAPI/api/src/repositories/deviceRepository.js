import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const DEVICES_TABLE = 'dispositivos';
const LOANS_TABLE = 'prestamos_dispositivos';
const NOTES_TABLE = 'notas_dispositivos';
const BOARD_TABLE = 'pizarron';

const mapDevice = (row = {}) => ({
  id: row.id,
  carrito: row.carrito,
  numero: row.numero,
  numero_serie: row.numero_serie,
  fecha_adquisicion: row.fecha_adquisicion,
  estado: row.estado,
  observaciones: row.observaciones,
  codigo: row.codigo ?? (row.carrito && row.numero ? `${row.carrito}${row.numero}` : null)
});

const mapLoan = (row = {}) => ({
  id: row.id,
  dispositivo_id: row.dispositivo_id,
  dispositivo_codigo: row.dispositivo_codigo,
  alumno: row.alumno,
  curso: row.curso,
  tutor: row.tutor,
  fecha_prestamo: row.fecha_prestamo,
  hora_prestamo: row.hora_prestamo,
  fecha_devolucion: row.fecha_devolucion,
  hora_devolucion: row.hora_devolucion,
  observaciones: row.observaciones,
  estado: row.estado,
  dispositivo: row.carrito || row.numero || row.numero_serie
    ? {
        id: row.dispositivo_id,
        carrito: row.carrito,
        numero: row.numero,
        numero_serie: row.numero_serie,
        estado: row.dispositivo_estado,
        codigo: row.dispositivo_codigo ?? (row.carrito && row.numero ? `${row.carrito}${row.numero}` : null)
      }
    : null
});

const mapBoardNote = (row = {}) => ({
  id: row.id,
  autor: row.autor,
  mensaje: row.mensaje,
  fecha: row.fecha ?? row.created_at
});

const addLegacyDateComparison = (builder, columnExpression, operator, isoDate, clientName) => {
  if (!isoDate) {
    return;
  }

  const comparator = operator.trim();
  if (clientName === 'pg' || clientName === 'postgresql') {
    builder.orWhereRaw(
      `TO_DATE(${columnExpression}, 'DD/MM/YYYY') ${comparator} TO_DATE(?, 'YYYY-MM-DD')`,
      [isoDate]
    );
    return;
  }

  builder.orWhereRaw(
    `STR_TO_DATE(${columnExpression}, '%d/%m/%Y') ${comparator} ?`,
    [isoDate]
  );
};

export const deviceRepository = {
  async createDevice(device) {
    return insertAndFetch(DEVICES_TABLE, device, { primaryKey: 'id' });
  },

  async updateDevice(id, updates) {
    return updateAndFetch(DEVICES_TABLE, id, updates, { primaryKey: 'id' });
  },

  async removeDevice(id) {
    return getDatabase()(DEVICES_TABLE).where({ id }).del();
  },

  async findDeviceById(id) {
    const row = await getDatabase()(DEVICES_TABLE).where({ id }).first();
    return row ? mapDevice(row) : null;
  },

  async findDeviceByCodigo(codigo) {
    if (!codigo) {
      return null;
    }

    const db = getDatabase();
    const row = await db(DEVICES_TABLE)
      .where('codigo', codigo)
      .orWhereRaw("CONCAT(carrito, numero) = ?", [codigo])
      .first();

    return row ? mapDevice(row) : null;
  },

  async listDevices(filters = {}) {
    const db = getDatabase();
    const query = db(DEVICES_TABLE).select('*');

    if (filters.estado) {
      query.where('estado', filters.estado);
    }

    if (filters.carrito) {
      query.where('carrito', filters.carrito);
    }

    if (filters.search) {
      const search = `%${filters.search}%`;
      query.andWhere((builder) => {
        builder
          .whereILike('carrito', search)
          .orWhereILike('numero', search)
          .orWhereILike('numero_serie', search)
          .orWhereILike('estado', search)
          .orWhereILike('observaciones', search)
          .orWhereILike('codigo', search);
      });
    }

    const rows = await query.orderBy([{ column: 'carrito', order: 'asc' }, { column: 'numero', order: 'asc' }]);
    const items = rows.map(mapDevice);

    return {
      items,
      stats: await this.computeStockStats()
    };
  },

  async computeStockStats() {
    const db = getDatabase();

    const disponiblesRow = await db(DEVICES_TABLE)
      .where('estado', 'En uso')
      .whereNotIn('id', function () {
        this.select('dispositivo_id').from(LOANS_TABLE).whereNull('fecha_devolucion');
      })
      .count({ cantidad: '*' })
      .first();

    const noDisponiblesRow = await db(DEVICES_TABLE)
      .whereIn('estado', ['Hurto', 'Obsoleta', 'Dañada'])
      .count({ cantidad: '*' })
      .first();

    const prestamosActivosRow = await db(LOANS_TABLE)
      .whereNull('fecha_devolucion')
      .count({ cantidad: '*' })
      .first();

    return {
      disponibles: Number(disponiblesRow?.cantidad ?? 0),
      noDisponibles: Number(noDisponiblesRow?.cantidad ?? 0),
      prestamosActivos: Number(prestamosActivosRow?.cantidad ?? 0)
    };
  },

  async createLoan(loan) {
    return insertAndFetch(LOANS_TABLE, loan, { primaryKey: 'id' });
  },

  async closeLoan(id, updates) {
    return updateAndFetch(LOANS_TABLE, id, updates, { primaryKey: 'id' });
  },

  async deleteLoan(id) {
    return getDatabase()(LOANS_TABLE).where({ id }).del();
  },

  async listLoans(filters = {}) {
    const db = getDatabase();
    const clientName = db?.client?.config?.client;
    const query = db(`${LOANS_TABLE} as l`)
      .select(
        'l.*',
        'd.carrito',
        'd.numero',
        'd.numero_serie',
        'd.estado as dispositivo_estado',
        'd.codigo as dispositivo_codigo'
      )
      .leftJoin(`${DEVICES_TABLE} as d`, 'l.dispositivo_id', 'd.id');

    if (filters.estado === 'abierto') {
      query.whereNull('l.fecha_devolucion');
    } else if (filters.estado === 'cerrado') {
      query.whereNotNull('l.fecha_devolucion');
    }

    if (filters.dispositivo_id) {
      query.where('l.dispositivo_id', filters.dispositivo_id);
    }

    if (filters.search) {
      const search = `%${filters.search}%`;
      query.andWhere((builder) => {
        builder
          .whereILike('l.alumno', search)
          .orWhereILike('l.curso', search)
          .orWhereILike('l.tutor', search)
          .orWhereILike('l.observaciones', search)
          .orWhereILike('d.numero_serie', search)
          .orWhereILike('d.carrito', search)
          .orWhereILike('d.codigo', search);
      });
    }

    if (filters.fechaDesde) {
      query.andWhere((builder) => {
        builder.where('l.fecha_prestamo', '>=', filters.fechaDesde);
        addLegacyDateComparison(builder, 'l.fecha_prestamo', '>=', filters.fechaDesde, clientName);
      });
    }

    if (filters.fechaHasta) {
      query.andWhere((builder) => {
        builder.where('l.fecha_prestamo', '<=', filters.fechaHasta);
        addLegacyDateComparison(builder, 'l.fecha_prestamo', '<=', filters.fechaHasta, clientName);
      });
    }

    const rows = await query
      .orderBy([{ column: 'l.fecha_prestamo', order: 'desc' }, { column: 'l.hora_prestamo', order: 'desc' }]);

    return rows.map(mapLoan);
  },

  async addNote(note) {
    return insertAndFetch(NOTES_TABLE, note, { primaryKey: 'id' });
  },

  async listNotes(deviceId) {
    return getDatabase()(NOTES_TABLE)
      .where({ dispositivo_id: deviceId })
      .orderBy('created_at', 'desc');
  },

  async addBoardNote(note) {
    return insertAndFetch(BOARD_TABLE, note, { primaryKey: 'id' });
  },

  async listBoardNotes() {
    const rows = await getDatabase()(BOARD_TABLE).select('*').orderBy('fecha', 'desc');
    return rows.map(mapBoardNote);
  },

  async deleteBoardNote(id) {
    return getDatabase()(BOARD_TABLE).where({ id }).del();
  }
};
