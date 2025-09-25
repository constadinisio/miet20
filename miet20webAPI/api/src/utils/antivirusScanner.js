import fs from 'fs/promises';
import os from 'os';
import path from 'path';
import { randomUUID } from 'crypto';
import { execFile } from 'child_process';
import { promisify } from 'util';
import httpStatus from 'http-status';
import { env } from '../config/env.js';
import { logger } from './logger.js';
import { ApiError } from './ApiError.js';

const execFileAsync = promisify(execFile);

const cleanupTempFile = async (filePath) => {
  try {
    await fs.rm(filePath, { force: true });
  } catch (error) {
    logger.warn('No fue posible eliminar el archivo temporal del antivirus', {
      filePath,
      error: error.message
    });
  }
};

export const scanBufferForThreats = async (buffer, { fileName = 'upload.bin', context = {} } = {}) => {
  const antivirusConfig = env.security?.antivirus;

  if (!antivirusConfig?.enabled) {
    return;
  }

  const tmpDir = await fs.mkdtemp(path.join(os.tmpdir(), 'miet20-av-'));
  const tmpFile = path.join(tmpDir, `${randomUUID()}-${fileName}`);

  try {
    await fs.writeFile(tmpFile, buffer);
  } catch (error) {
    await cleanupTempFile(tmpFile);
    logger.error('No fue posible escribir el archivo temporal para el antivirus', {
      filePath: tmpFile,
      error: error.message
    });
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      'No se pudo preparar el archivo para la validación de seguridad.'
    );
  }

  try {
    const args = Array.isArray(antivirusConfig.args) && antivirusConfig.args.length > 0
      ? antivirusConfig.args
      : ['--no-summary'];

    const { stdout } = await execFileAsync(antivirusConfig.command, [...args, tmpFile], {
      timeout: antivirusConfig.timeoutMs
    });

    logger.info('Escaneo antivirus finalizado', {
      filePath: tmpFile,
      stdout,
      context
    });
  } catch (error) {
    if (typeof error.code === 'number' && error.code === 1) {
      logger.warn('El antivirus detectó una amenaza en el archivo subido', {
        filePath: tmpFile,
        context,
        stdout: error.stdout,
        stderr: error.stderr
      });
      throw new ApiError(
        httpStatus.BAD_REQUEST,
        'El archivo enviado no superó la validación de seguridad.'
      );
    }

    if (error.code === 'ENOENT') {
      logger.error('No se encontró el comando del antivirus configurado', {
        command: antivirusConfig.command
      });
      if (antivirusConfig.failOnError) {
        throw new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          'No se pudo ejecutar el antivirus configurado.'
        );
      }
    } else if (antivirusConfig.failOnError) {
      logger.error('El antivirus falló durante el análisis del archivo', {
        error: error.message,
        context,
        stdout: error.stdout,
        stderr: error.stderr
      });
      throw new ApiError(
        httpStatus.INTERNAL_SERVER_ERROR,
        'No se pudo validar la seguridad del archivo enviado.'
      );
    } else {
      logger.warn('El antivirus presentó un error y se omitió la validación estricta', {
        error: error.message,
        context,
        stdout: error.stdout,
        stderr: error.stderr
      });
    }
  } finally {
    await cleanupTempFile(tmpFile);
    try {
      await fs.rm(tmpDir, { recursive: true, force: true });
    } catch (error) {
      logger.debug('No fue posible eliminar el directorio temporal del antivirus', {
        directory: tmpDir,
        error: error.message
      });
    }
  }
};
