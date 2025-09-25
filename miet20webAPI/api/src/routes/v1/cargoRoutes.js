import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  addCargoSchedules,
  createCargo,
  getCargo,
  deactivateCargo,
  reactivateCargo,
  listCargoSchedules,
  listCargos,
  listCargoTypes,
  replaceCargoSchedules,
  saveCargoRelations,
  updateCargo,
  deleteCargoSchedule
} from '../../controllers/cargoController.js';
import {
  addScheduleValidator,
  cargoIdParam,
  cargoScheduleIdParam,
  createCargoValidator,
  replaceSchedulesValidator,
  saveRelationsValidator,
  updateCargoValidator
} from '../../validators/cargoValidator.js';

const router = Router();

router.use(authenticate);
router.use(authorizeRoles('admin'));

router.get('/tipos', listCargoTypes);
router.get('/', listCargos);
router.post('/', createCargoValidator, validateRequest, createCargo);
router.get('/:cargoId', cargoIdParam, validateRequest, getCargo);
router.put('/:cargoId', updateCargoValidator, validateRequest, updateCargo);
router.delete('/:cargoId', cargoIdParam, validateRequest, deactivateCargo);
router.post('/:cargoId/reactivar', cargoIdParam, validateRequest, reactivateCargo);
router.get('/:cargoId/horarios', cargoIdParam, validateRequest, listCargoSchedules);
router.post('/:cargoId/horarios', addScheduleValidator, validateRequest, addCargoSchedules);
router.put('/:cargoId/horarios', replaceSchedulesValidator, validateRequest, replaceCargoSchedules);
router.delete(
  '/:cargoId/horarios/:horarioId',
  cargoScheduleIdParam,
  validateRequest,
  deleteCargoSchedule
);
router.post('/:cargoId/relaciones', saveRelationsValidator, validateRequest, saveCargoRelations);

export default router;
