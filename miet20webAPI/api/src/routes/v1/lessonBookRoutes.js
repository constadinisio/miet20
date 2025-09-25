import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  createLessonBookEntry,
  deleteLessonBookEntry,
  getLessonBookEntry,
  listLessonBookEntries,
  updateLessonBookEntry
} from '../../controllers/lessonBookController.js';
import {
  createLessonBookValidator,
  lessonBookEntryParam,
  listLessonBookValidator,
  updateLessonBookValidator
} from '../../validators/lessonBookValidator.js';

const router = Router();

router.use(authenticate, authorizeRoles('profesor'));

router
  .route('/')
  .get(listLessonBookValidator, validateRequest, listLessonBookEntries)
  .post(createLessonBookValidator, validateRequest, createLessonBookEntry);

router
  .route('/:entryId')
  .get(lessonBookEntryParam, validateRequest, getLessonBookEntry)
  .put(updateLessonBookValidator, validateRequest, updateLessonBookEntry)
  .delete(lessonBookEntryParam, validateRequest, deleteLessonBookEntry);

export default router;
