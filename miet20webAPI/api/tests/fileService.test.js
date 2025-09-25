import { jest } from '@jest/globals';

const mockRepository = {
  create: jest.fn(),
  findById: jest.fn(),
  delete: jest.fn()
};

const mockStorage = {
  saveGenericFile: jest.fn(),
  deleteStoredFile: jest.fn(),
  buildSignedFileUrl: jest.fn()
};

jest.unstable_mockModule('../src/repositories/fileRepository.js', () => ({
  fileRepository: mockRepository
}));

jest.unstable_mockModule('../src/utils/fileStorage.js', () => mockStorage);

const { fileService } = await import('../src/services/fileService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

describe('fileService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('uploadFile rechaza datos vacíos', async () => {
    await expect(
      fileService.uploadFile(1, {
        fileName: 'documento.pdf',
        fileMime: 'application/pdf',
        fileData: ''
      })
    ).rejects.toThrow(ApiError);
    expect(mockRepository.create).not.toHaveBeenCalled();
  });

  test('uploadFile persiste metadata y retorna URL firmada', async () => {
    mockStorage.saveGenericFile.mockResolvedValue({
      fileName: 'uuid.pdf',
      storagePath: '/tmp/uuid.pdf',
      publicUrl: '/files/uuid.pdf',
      size: 1500
    });
    mockRepository.create.mockResolvedValue({
      id: 7,
      originalName: 'documento.pdf',
      mimeType: 'application/pdf',
      size: 1500,
      storagePath: '/tmp/uuid.pdf',
      publicUrl: '/files/uuid.pdf',
      isPublic: false,
      uploadedBy: 1,
      createdAt: new Date(),
      updatedAt: new Date()
    });
    mockStorage.buildSignedFileUrl.mockReturnValue({ url: '/signed/url', expiresAt: 999999 });

    const result = await fileService.uploadFile(1, {
      fileName: 'documento.pdf',
      fileMime: 'application/pdf',
      fileData: Buffer.from('contenido').toString('base64')
    });

    expect(mockRepository.create).toHaveBeenCalledWith(
      expect.objectContaining({
        originalName: 'documento.pdf',
        size: 1500
      })
    );
    expect(result.signedUrl).toBe('/signed/url');
  });

  test('getFile impide acceso sin autenticación cuando no es público', async () => {
    mockRepository.findById.mockResolvedValue({
      id: 9,
      originalName: 'privado.pdf',
      mimeType: 'application/pdf',
      size: 100,
      storagePath: '/tmp/privado.pdf',
      publicUrl: '/files/privado.pdf',
      isPublic: false,
      uploadedBy: 1,
      createdAt: new Date(),
      updatedAt: new Date()
    });

    await expect(fileService.getFile(9)).rejects.toThrow(ApiError);
  });
});
