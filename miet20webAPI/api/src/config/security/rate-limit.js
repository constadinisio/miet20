import rateLimit from 'express-rate-limit';
import { env } from '../env.js';

const buildRateLimiter = () => {
  const config = env.security?.rateLimit ?? {};

  if (!config.enabled) {
    return null;
  }

  return rateLimit({
    windowMs: config.windowMs,
    max: config.max,
    standardHeaders: config.standardHeaders,
    legacyHeaders: config.legacyHeaders,
    skipSuccessfulRequests: config.skipSuccessfulRequests,
    message: config.message,
    handler: (req, res) => {
      res.status(429).json({
        success: false,
        message: config.message,
        data: null
      });
    }
  });
};

export const getRateLimiter = () => buildRateLimiter();
