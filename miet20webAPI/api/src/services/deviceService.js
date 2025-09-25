import { deviceRepository } from '../repositories/deviceRepository.js';
import { ApiError } from '../utils/ApiError.js';
import httpStatus from 'http-status';

const NORMALIZED_STATE_MAP = {
  'en uso': 'En uso',
  'en_uso': 'En uso',
  enUso: 'En uso',
  disponible: 'En uso',
  danada: 'Dañada',
  dañada: 'Dañada',
  hurto: 'Hurto',
  obsoleta: 'Obsoleta',
  reparacion: 'Dañada',
  reparación: 'Dañada'
};

const normalizeEstado = (estado) => {
  if (!estado) return undefined;
  const clave = estado.toLowerCase().replace(/\s+/g, ' ');
  return NORMALIZED_STATE_MAP[clave] ?? estado;
};

const parseDateFilter = (value, label) => {
  if (!value && value !== 0) {
    return undefined;
  }

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return value.toISOString().slice(0, 10);
  }

  const raw = String(value).trim();
  if (!raw) {
    return undefined;
  }

  const isoMatch = raw.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (isoMatch) {
    return `${isoMatch[1]}-${isoMatch[2]}-${isoMatch[3]}`;
  }

  const latinMatch = raw.match(/^(\d{2})[-/](\d{2})[-/](\d{4})$/);
  if (latinMatch) {
    const day = Number.parseInt(latinMatch[1], 10);
    const month = Number.parseInt(latinMatch[2], 10);
    const year = Number.parseInt(latinMatch[3], 10);

    if (day >= 1 && day <= 31 && month >= 1 && month <= 12) {
      const paddedDay = String(day).padStart(2, '0');
      const paddedMonth = String(month).padStart(2, '0');
      return `${year}-${paddedMonth}-${paddedDay}`;
    }
  }

  throw new ApiError(
    httpStatus.BAD_REQUEST,
    `El formato de ${label} es inválido. Usá DD/MM/AAAA.`
  );
};

const normalizeLoanFilters = (filters = {}) => {
  const normalized = {};

  if (filters.estado) {
    const estado = String(filters.estado).toLowerCase();
    if (['abierto', 'abiertos'].includes(estado)) {
      normalized.estado = 'abierto';
    } else if (['cerrado', 'cerrados'].includes(estado)) {
      normalized.estado = 'cerrado';
    }
  }

  const rawDeviceId =
    filters.dispositivo_id ?? filters.dispositivoId ?? filters.deviceId;
  if (rawDeviceId !== undefined) {
    const parsed = Number.parseInt(rawDeviceId, 10);
    if (!Number.isNaN(parsed)) {
      normalized.dispositivo_id = parsed;
    }
  }

  if (filters.search) {
    normalized.search = String(filters.search);
  }

  const rawFechaDesde = filters.fecha_desde ?? filters.fechaDesde;
  if (rawFechaDesde) {
    normalized.fechaDesde = parseDateFilter(rawFechaDesde, 'la fecha desde');
  }

  const rawFechaHasta = filters.fecha_hasta ?? filters.fechaHasta;
  if (rawFechaHasta) {
    normalized.fechaHasta = parseDateFilter(rawFechaHasta, 'la fecha hasta');
  }

  return normalized;
};

export const deviceService = {
  async registerDevice(payload) {
    const estado = normalizeEstado(payload.estado) ?? 'En uso';

    return deviceRepository.createDevice({
      carrito: payload.carrito ?? null,
      numero: payload.numero ?? null,
      numero_serie: payload.numero_serie ?? null,
      fecha_adquisicion: payload.fecha_adquisicion ?? null,
      observaciones: payload.observaciones ?? null,
      estado,
      codigo: payload.codigo ?? (payload.carrito && payload.numero ? `${payload.carrito}${payload.numero}` : null)
    });
  },

  async listDevices(filters = {}) {
    return deviceRepository.listDevices(filters);
  },

  async getStockSummary() {
    return deviceRepository.computeStockStats();
  },

  async updateDevice(id, updates) {
    const payload = { ...updates };
    if (payload.estado) {
      payload.estado = normalizeEstado(payload.estado);
    }
    if (payload.carrito && payload.numero && !payload.codigo) {
      payload.codigo = `${payload.carrito}${payload.numero}`;
    }

    return deviceRepository.updateDevice(id, payload);
  },

  async removeDevice(id) {
    return deviceRepository.removeDevice(id);
  },

  async resolveDeviceId({ dispositivo_id, codigo }) {
    if (dispositivo_id) {
      const device = await deviceRepository.findDeviceById(dispositivo_id);
      if (!device) {
        throw new ApiError(httpStatus.NOT_FOUND, 'El dispositivo indicado no existe');
      }
      return device.id;
    }

    if (codigo) {
      const device = await deviceRepository.findDeviceByCodigo(codigo);
      if (!device) {
        throw new ApiError(httpStatus.NOT_FOUND, 'No se encontró un dispositivo con el código indicado');
      }
      return device.id;
    }

    throw new ApiError(httpStatus.BAD_REQUEST, 'Debe indicar el dispositivo asociado al préstamo');
  },

  async createLoan(payload) {
    const dispositivoId = await this.resolveDeviceId({
      dispositivo_id: payload.dispositivo_id,
      codigo: payload.dispositivo_codigo ?? payload.codigo ?? payload.Netbook_ID
    });

    return deviceRepository.createLoan({
      dispositivo_id: dispositivoId,
      alumno: payload.alumno,
      curso: payload.curso,
      tutor: payload.tutor,
      fecha_prestamo: payload.fecha_prestamo,
      hora_prestamo: payload.hora_prestamo,
      observaciones: payload.observaciones ?? null,
      estado: 'abierto'
    });
  },

  async closeLoan(id, updates) {
    const payload = {
      fecha_devolucion: updates.fecha_devolucion,
      hora_devolucion: updates.hora_devolucion ?? null,
      estado: updates.estado ?? 'cerrado'
    };
    return deviceRepository.closeLoan(id, payload);
  },

  async deleteLoan(id) {
    const deleted = await deviceRepository.deleteLoan(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'El préstamo indicado no existe');
    }
  },

  async listLoans(filters = {}) {
    return deviceRepository.listLoans(normalizeLoanFilters(filters));
  },

  async addNote(payload) {
    return deviceRepository.addNote({
      dispositivo_id: payload.dispositivo_id,
      contenido: payload.contenido,
      creado_por: payload.creado_por
    });
  },

  async listNotes(deviceId) {
    return deviceRepository.listNotes(deviceId);
  },

  async addBoardNote(payload) {
    return deviceRepository.addBoardNote({
      autor: payload.autor,
      mensaje: payload.mensaje
    });
  },

  async listBoardNotes() {
    return deviceRepository.listBoardNotes();
  },

  async deleteBoardNote(id) {
    const deleted = await deviceRepository.deleteBoardNote(id);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'La nota indicada no existe');
    }
  }
};
