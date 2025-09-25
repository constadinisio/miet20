import { createHash } from 'crypto';
import { promises as fs } from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const NEWS_COVER_DIR = path.resolve(__dirname, '../../..', 'public', 'panelNoticias', 'images');
const GALLERY_MEDIA_DIR = path.resolve(__dirname, '../../..', 'public', 'gallery', 'items');
const GENERIC_UPLOAD_DIR = path.resolve(__dirname, '../../..', 'storage', 'uploads');

const ensureDirectory = async (directoryPath) => {
  await fs.mkdir(directoryPath, { recursive: true });
};

const extensionFromMime = (mimeType, fallback) => {
  if (fallback) {
    return fallback.replace(/[^a-z0-9]/gi, '').toLowerCase();
  }

  switch (mimeType) {
    case 'image/jpeg':
      return 'jpg';
    case 'image/png':
      return 'png';
    case 'image/webp':
      return 'webp';
    default:
      return 'bin';
  }
};

export const saveNewsCover = async ({ buffer, mimeType, extension, identifier }) => {
  await ensureDirectory(NEWS_COVER_DIR);
  const fileExtension = extensionFromMime(mimeType, extension);
  const fileName = `${identifier}.${fileExtension}`;
  const storagePath = path.join(NEWS_COVER_DIR, fileName);

  await fs.writeFile(storagePath, buffer);

  return {
    fileName,
    storagePath,
    publicUrl: `/panelNoticias/images/${fileName}`
  };
};

export const saveGalleryMedia = async ({ buffer, mimeType, extension, identifier }) => {
  await ensureDirectory(GALLERY_MEDIA_DIR);
  const fileExtension = extensionFromMime(mimeType, extension);
  const fileName = `${identifier}.${fileExtension}`;
  const storagePath = path.join(GALLERY_MEDIA_DIR, fileName);

  await fs.writeFile(storagePath, buffer);

  return {
    fileName,
    storagePath,
    publicUrl: `/gallery/items/${fileName}`
  };
};

export const deleteStoredFile = async (filePath) => {
  if (!filePath) {
    return;
  }

  try {
    await fs.unlink(filePath);
  } catch (error) {
    if (error.code !== 'ENOENT') {
      throw error;
    }
  }
};

export const saveGenericFile = async ({ buffer, mimeType, extension, identifier }) => {
  await ensureDirectory(GENERIC_UPLOAD_DIR);
  const fileExtension = extensionFromMime(mimeType, extension);
  const fileName = `${identifier}.${fileExtension}`;
  const storagePath = path.join(GENERIC_UPLOAD_DIR, fileName);

  await fs.writeFile(storagePath, buffer);

  return {
    fileName,
    storagePath,
    publicUrl: `/files/${fileName}`,
    size: buffer.length
  };
};

export const buildSignedFileUrl = ({ publicUrl, expiresInSeconds = 300 }) => {
  const expiresAt = Math.floor(Date.now() / 1000) + expiresInSeconds;
  const signature = createHash('sha256').update(`${publicUrl}:${expiresAt}`).digest('hex');

  return {
    url: `${publicUrl}?token=${signature}&expires=${expiresAt}`,
    expiresAt
  };
};