import { env } from './src/config/env.js';

const buildConnection = (overrides = {}) => ({
  host: env.database.host,
  port: env.database.port,
  user: env.database.user,
  password: env.database.password,
  database: env.database.database,
  ...overrides
});

const baseConfig = {
  client: env.database.client,
  pool: { min: 2, max: 10 },
  migrations: {
    tableName: 'knex_migrations',
    directory: './migrations'
  }
};

const testDatabaseName = process.env.DB_NAME_TEST || `${env.database.database}_test`;

export default {
  development: {
    ...baseConfig,
    connection: buildConnection()
  },
  test: {
    ...baseConfig,
    connection: buildConnection({ database: testDatabaseName })
  },
  production: {
    ...baseConfig,
    connection: buildConnection()
  }
};
