import winston from 'winston';
import { env, isProduction } from '../config/env.js';

const logFormat = winston.format.printf(({ level, message, timestamp, stack }) => {
  if (stack) {
    return `${timestamp} [${level}] ${stack}`;
  }
  return `${timestamp} [${level}] ${message}`;
});

export const logger = winston.createLogger({
  level: isProduction ? 'info' : 'debug',
  format: winston.format.combine(
    winston.format.errors({ stack: true }),
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    logFormat
  ),
  transports: [new winston.transports.Console()]
});

logger.debug(`Logger inicializado en modo ${env.nodeEnv}`);
