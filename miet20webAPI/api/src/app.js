import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import httpStatus from 'http-status';
import routes from './routes/v1/index.js';
import { env } from './config/env.js';
import { getRateLimiter } from './config/security/rate-limit.js';
import { errorConverter, errorHandler } from './middlewares/errorHandler.js';
import { notFoundHandler } from './middlewares/notFound.js';
import { logger } from './utils/logger.js';
import { requestContext } from './middlewares/requestContext.js';
import { requestLogger } from './middlewares/requestLogger.js';
import { sendSuccess } from './utils/ApiResponse.js';

const app = express();

app.set('trust proxy', 1);
app.use(requestContext);
app.use(requestLogger);
app.use(helmet());
app.use(cors({ origin: '*', credentials: true }));
const rateLimiter = getRateLimiter();
if (rateLimiter) {
  app.use(rateLimiter);
}
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

app.get('/health', (req, res) => {
  sendSuccess({
    req,
    res,
    statusCode: httpStatus.OK,
    data: {
      environment: env.nodeEnv,
      version: 'v1'
    }
  });
});

app.use('/api/v1', routes);

app.use(notFoundHandler);
app.use(errorConverter);
app.use(errorHandler);

process.on('unhandledRejection', (error) => {
  logger.error('Unhandled Rejection', error);
});

process.on('uncaughtException', (error) => {
  logger.error('Uncaught Exception', error);
});

export default app;