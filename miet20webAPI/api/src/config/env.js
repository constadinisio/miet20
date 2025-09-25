/* eslint-env node */
/* eslint-disable no-console */

import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const { console } = globalThis;

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Buscar el .env en la carpeta config (mismo nivel que este archivo)
const envPath = path.resolve(__dirname, '.env');
dotenv.config({ path: envPath, override: true });

// Debug: mostrar qué variables se están cargando
console.log('=== DEBUG CONFIGURACIÓN ===');
console.log('Archivo .env:', envPath);
console.log('DB_HOST:', process.env.DB_HOST);
console.log('DB_USER:', process.env.DB_USER);
console.log('DB_NAME:', process.env.DB_NAME);
console.log('DB_PASS definida:', !!process.env.DB_PASS);
console.log('JWT_SECRET definido:', !!process.env.JWT_SECRET);
console.log('==============================');

export const env = {
  nodeEnv: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT, 10) || 4000,
  appUrl: process.env.APP_URL || 'http://localhost:4000',
  
  jwt: {
    secret: process.env.JWT_SECRET || 'd9da1700b58a49fae89f4a559ee353945f542bc055b539df54f304180b008ff6ae9f8f7114632f4cbcdc6f8b82bddbb7b9687e3d9dd19c75746d6f144146880e',
    expiresIn: process.env.JWT_EXPIRATION || '24h',
    refreshSecret: process.env.REFRESH_TOKEN_SECRET || process.env.JWT_SECRET,
    refreshExpiresIn: process.env.REFRESH_TOKEN_EXPIRATION || '7d'
  },
  
  database: {
    client: process.env.DB_CLIENT || 'mysql2',
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT, 10) || 3306,
    user: process.env.DB_USER || 'adminweb',
    password: process.env.DB_PASS || 'wR17XN&u39M7',
    database: process.env.DB_NAME || 'et20plataforma'
  },
  
  google: {
    clientId: process.env.GOOGLE_CLIENT_ID || '886183905992-g1d8fe0k1v86b4ebb21950a5rdkm1ol7.apps.googleusercontent.com',
    clientSecret: process.env.GOOGLE_CLIENT_SECRET || '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',
    redirectUri: process.env.GOOGLE_REDIRECT_URI || 'https://magicalhour.com.ar/login_google.php',
    audience: (process.env.GOOGLE_OAUTH_AUDIENCE || process.env.GOOGLE_CLIENT_ID || '886183905992-g1d8fe0k1v86b4ebb21950a5rdkm1ol7.apps.googleusercontent.com')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean),
    hostedDomain: process.env.GOOGLE_HOSTED_DOMAIN || ''
  },
  
  security: {
    rateLimit: {
      enabled: (process.env.RATE_LIMIT_ENABLED ?? 'true').toLowerCase() !== 'false',
      windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS, 10) || 60_000,
      max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS, 10) || 120,
      standardHeaders: (process.env.RATE_LIMIT_STANDARD_HEADERS ?? 'true').toLowerCase() !== 'false',
      legacyHeaders: (process.env.RATE_LIMIT_LEGACY_HEADERS ?? 'false').toLowerCase() === 'true',
      skipSuccessfulRequests:
        (process.env.RATE_LIMIT_SKIP_SUCCESSFUL ?? 'false').toLowerCase() === 'true',
      message:
        process.env.RATE_LIMIT_BLOCK_MESSAGE ||
        'Se superó el límite de solicitudes permitidas. Intente nuevamente más tarde.'
    },
    antivirus: {
      enabled: (process.env.ANTIVIRUS_ENABLED ?? 'false').toLowerCase() === 'true',
      command: process.env.ANTIVIRUS_COMMAND || 'clamscan',
      args: (process.env.ANTIVIRUS_ARGS || '--no-summary')
        .split(' ')
        .map((value) => value.trim())
        .filter(Boolean),
      timeoutMs: parseInt(process.env.ANTIVIRUS_TIMEOUT_MS, 10) || 15_000,
      failOnError: (process.env.ANTIVIRUS_FAIL_ON_ERROR ?? 'false').toLowerCase() === 'true'
    }
  }
};

export const isProduction = env.nodeEnv === 'production';