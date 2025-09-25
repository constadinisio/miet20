import { body, param } from 'express-validator';

const estadosDispositivo = ['En uso', 'Dañada', 'Hurto', 'Obsoleta'];

export const registerDeviceValidator = [
  body('carrito').optional().isString().withMessage('El carrito debe ser texto'),
  body('numero').optional().isString().withMessage('El número debe ser texto'),
  body('numero_serie').optional().isString().withMessage('El número de serie debe ser texto'),
  body('fecha_adquisicion').optional().isISO8601().withMessage('La fecha de adquisición es inválida'),
  body('estado').optional().isIn(estadosDispositivo).withMessage('Estado inválido')
];

export const updateDeviceValidator = [
  param('id').isInt().withMessage('El dispositivo es inválido'),
  body('carrito').optional().isString().withMessage('El carrito debe ser texto'),
  body('numero').optional().isString().withMessage('El número debe ser texto'),
  body('numero_serie').optional().isString().withMessage('El número de serie debe ser texto'),
  body('fecha_adquisicion').optional().isISO8601().withMessage('La fecha de adquisición es inválida'),
  body('estado').optional().isIn(estadosDispositivo).withMessage('Estado inválido')
];

export const createLoanValidator = [
  param('id').optional().isInt().withMessage('El dispositivo es inválido'),
  body('dispositivo_id').optional().isInt().withMessage('El dispositivo es inválido'),
  body('dispositivo_codigo').optional().isString().withMessage('El código de dispositivo es inválido'),
  body('codigo').optional().isString().withMessage('El código de dispositivo es inválido'),
  body('Netbook_ID').optional().isString().withMessage('El código de dispositivo es inválido'),
  body('alumno').notEmpty().withMessage('El alumno es obligatorio'),
  body('curso').notEmpty().withMessage('El curso es obligatorio'),
  body('tutor').notEmpty().withMessage('El tutor es obligatorio'),
  body('fecha_prestamo').notEmpty().withMessage('La fecha de préstamo es obligatoria'),
  body('hora_prestamo').notEmpty().withMessage('La hora de préstamo es obligatoria')
];

export const closeLoanValidator = [
  param('loanId').isInt().withMessage('El préstamo es inválido'),
  body('fecha_devolucion').notEmpty().withMessage('La fecha de devolución es obligatoria'),
  body('hora_devolucion').optional().isString().withMessage('La hora de devolución es inválida'),
  body('estado').optional().isString().withMessage('El estado es inválido')
];

export const deleteLoanValidator = [
  param('loanId').isInt().withMessage('El préstamo es inválido')
];

export const addNoteValidator = [
  param('id').isInt().withMessage('El dispositivo es inválido'),
  body('contenido').notEmpty().withMessage('El contenido es obligatorio')
];

export const addBoardNoteValidator = [
  body('mensaje').notEmpty().withMessage('El mensaje es obligatorio'),
  body('autor').optional().isString().withMessage('El autor es inválido')
];

export const deleteBoardNoteValidator = [
  param('noteId').isInt().withMessage('La nota es inválida')
];
