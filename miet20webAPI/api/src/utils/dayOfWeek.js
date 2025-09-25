const DAY_NAME_TO_NUMBER = new Map([
  ['lunes', 1],
  ['martes', 2],
  ['miércoles', 3],
  ['miercoles', 3],
  ['jueves', 4],
  ['viernes', 5],
  ['sábado', 6],
  ['sabado', 6],
  ['domingo', 7]
]);

const NUMBER_TO_DAY_NAME = {
  1: 'Lunes',
  2: 'Martes',
  3: 'Miércoles',
  4: 'Jueves',
  5: 'Viernes',
  6: 'Sábado',
  7: 'Domingo'
};

const normalizeString = (value) =>
  value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();

export const mapDayValueToNumber = (value) => {
  if (value === null || value === undefined) {
    return null;
  }

  if (typeof value === 'number') {
    if (Number.isInteger(value) && value >= 1 && value <= 7) {
      return value;
    }
    return null;
  }

  const trimmed = value.toString().trim();
  if (!trimmed) {
    return null;
  }

  const normalized = normalizeString(trimmed);
  if (DAY_NAME_TO_NUMBER.has(normalized)) {
    return DAY_NAME_TO_NUMBER.get(normalized);
  }

  const parsed = Number.parseInt(normalized, 10);
  if (!Number.isNaN(parsed) && parsed >= 1 && parsed <= 7) {
    return parsed;
  }

  return null;
};

export const dayNumberToName = (value) => NUMBER_TO_DAY_NAME[value] ?? null;

