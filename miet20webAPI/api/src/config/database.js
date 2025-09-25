import knex from 'knex';
import { env } from './env.js';
import { logger } from '../utils/logger.js';

let connection;

export const getDatabase = () => {
  if (!connection) {
    connection = knex({
      client: env.database.client,
      connection: {
        host: env.database.host,
        port: env.database.port,
        user: env.database.user,
        password: env.database.password,
        database: env.database.database
      },
      pool: {
        min: 2,
        max: 10
      },
      migrations: {
        tableName: 'knex_migrations'
      }
    });

    connection.on('query', (queryData) => {
      if (env.nodeEnv !== 'test') {
        logger.debug(`SQL: ${queryData.sql}`);
      }
    });
  }

  return connection;
};

export const closeDatabase = async () => {
  if (connection) {
    await connection.destroy();
    connection = null;
  }
};
