import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  addBoardNote,
  addNote,
  closeLoan,
  createLoan,
  deleteBoardNote,
  deleteDevice,
  deleteLoan,
  getStockSummary,
  listBoardNotes,
  listDevices,
  listLoans,
  listNotes,
  registerDevice,
  updateDevice
} from '../../controllers/deviceController.js';
import {
  addBoardNoteValidator,
  addNoteValidator,
  closeLoanValidator,
  createLoanValidator,
  deleteBoardNoteValidator,
  deleteLoanValidator,
  registerDeviceValidator,
  updateDeviceValidator
} from '../../validators/deviceValidator.js';

const router = Router();

router.use(authenticate);
router.use(authorizeRoles('spei', 'admin'));

router
  .route('/')
  .get(listDevices)
  .post(registerDeviceValidator, validateRequest, registerDevice);

router.get('/resumen', getStockSummary);

router
  .route('/pizarron')
  .get(listBoardNotes)
  .post(addBoardNoteValidator, validateRequest, addBoardNote);

router.delete('/pizarron/:noteId', deleteBoardNoteValidator, validateRequest, deleteBoardNote);

router
  .route('/:id')
  .put(updateDeviceValidator, validateRequest, updateDevice)
  .delete(updateDeviceValidator, validateRequest, deleteDevice);

router.post(
  '/:id/prestamos',
  createLoanValidator,
  validateRequest,
  createLoan
);

router.post('/prestamos', createLoanValidator, validateRequest, createLoan);

router.put(
  '/prestamos/:loanId/cerrar',
  closeLoanValidator,
  validateRequest,
  closeLoan
);

router
  .route('/prestamos/:loanId')
  .delete(deleteLoanValidator, validateRequest, deleteLoan);

router.get('/prestamos', listLoans);

router
  .route('/:id/notas')
  .get(listNotes)
  .post(addNoteValidator, validateRequest, addNote);

export default router;
