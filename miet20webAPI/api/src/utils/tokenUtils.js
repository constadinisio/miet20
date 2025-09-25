import crypto from 'crypto';

export const hashToken = (token) => {
  if (!token) {
    return '';
  }

  return crypto.createHash('sha256').update(token).digest('hex');
};

export const parseDate = (value) => {
  if (!value) {
    return null;
  }

  const parsed = value instanceof Date ? value : new Date(value);
  return Number.isNaN(Number(parsed)) ? null : parsed;
};
