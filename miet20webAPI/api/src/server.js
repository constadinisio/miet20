import http from 'http';
import app from './app.js';
import { env } from './config/env.js';
import { logger } from './utils/logger.js';
import { getDatabase } from './config/database.js';

const server = http.createServer(app);

const start = async () => {
  try {
    await getDatabase().raw('select 1+1 as result');
    server.listen(env.port, () => {
      logger.info(`API escuchando en ${env.appUrl}`);
    });
  } catch (error) {
    logger.error('No se pudo iniciar la API', error);
    process.exit(1);
  }
};

start();

process.on('SIGTERM', () => {
  logger.info('SIGTERM recibido, cerrando servidor');
  server.close(() => {
    logger.info('Servidor cerrado correctamente');
  });
});
