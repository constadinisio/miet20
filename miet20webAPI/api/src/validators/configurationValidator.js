import { body } from 'express-validator';

export const updateConfigurationValidator = [
  body().custom((value) => {
    if (typeof value !== 'object' || Array.isArray(value)) {
      throw new Error('El cuerpo debe ser un objeto');
    }
    return true;
  })
];
