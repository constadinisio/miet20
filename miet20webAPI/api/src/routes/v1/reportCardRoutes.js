import { Router } from 'express';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import {
  listReportCards,
  getReportCard,
  createReportCard,
  updateReportCard,
  updateReportCardStatus,
  getReportCardExport,
  listReportCardCourseSubjects,
  getReportCardLatestTerm,
  listReportCardMissingSubjects
} from '../../controllers/reportCardController.js';
import {
  listReportCardsValidator,
  reportCardIdParamValidator,
  createReportCardValidator,
  updateReportCardValidator,
  updateReportCardStatusValidator,
  reportCardExportValidator,
  reportCardCourseSubjectsQueryValidator,
  reportCardLatestTermQueryValidator,
  reportCardMissingSubjectsQueryValidator
} from '../../validators/reportCardValidator.js';

const router = Router();

router.use(authenticate, authorizeRoles('admin', 'preceptor'));

router.get('/', listReportCardsValidator, validateRequest, listReportCards);
router.post('/', createReportCardValidator, validateRequest, createReportCard);
router.get(
  '/helpers/course-subjects',
  reportCardCourseSubjectsQueryValidator,
  validateRequest,
  listReportCardCourseSubjects
);
router.get(
  '/helpers/latest-term',
  reportCardLatestTermQueryValidator,
  validateRequest,
  getReportCardLatestTerm
);
router.get(
  '/helpers/missing-subjects',
  reportCardMissingSubjectsQueryValidator,
  validateRequest,
  listReportCardMissingSubjects
);
router.get('/:reportCardId', reportCardIdParamValidator, validateRequest, getReportCard);
router.put('/:reportCardId', updateReportCardValidator, validateRequest, updateReportCard);
router.patch(
  '/:reportCardId/status',
  updateReportCardStatusValidator,
  validateRequest,
  updateReportCardStatus
);
router.get('/:reportCardId/export', reportCardExportValidator, validateRequest, getReportCardExport);

export default router;
