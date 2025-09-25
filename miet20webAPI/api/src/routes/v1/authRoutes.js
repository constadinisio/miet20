import { Router } from 'express';
import { login, loginWithGoogle, logout, refreshToken, switchRole } from '../../controllers/authController.js';
import {
  googleLoginValidator,
  loginValidator,
  logoutValidator,
  refreshTokenValidator,
  switchRoleValidator
} from '../../validators/authValidator.js';
import { validateRequest } from '../../middlewares/validateRequest.js';
import { authenticate } from '../../middlewares/authMiddleware.js';

const router = Router();

router.post('/login', loginValidator, validateRequest, login);
router.post('/google-login', googleLoginValidator, validateRequest, loginWithGoogle);
router.post('/refresh-token', refreshTokenValidator, validateRequest, refreshToken);
router.post('/switch-role', authenticate, switchRoleValidator, validateRequest, switchRole);
router.post('/logout', authenticate, logoutValidator, validateRequest, logout);

export default router;
