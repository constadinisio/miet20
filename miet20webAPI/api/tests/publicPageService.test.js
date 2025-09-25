import { jest } from '@jest/globals';

const mockRepository = {
  listPublished: jest.fn(),
  findBySlug: jest.fn(),
  create: jest.fn(),
  updateBySlug: jest.fn(),
  archive: jest.fn()
};

jest.unstable_mockModule('../src/repositories/publicPageRepository.js', () => ({
  publicPageRepository: mockRepository
}));

const { publicPageService } = await import('../src/services/publicPageService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

describe('publicPageService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('createPage rechaza slug inválido', async () => {
    await expect(
      publicPageService.createPage(1, {
        slug: 'Página Nueva',
        title: 'Título',
        content: 'Contenido largo para la página',
        status: 'draft'
      })
    ).rejects.toThrow(ApiError);
    expect(mockRepository.create).not.toHaveBeenCalled();
  });

  test('updatePage asigna publishedAt cuando se publica', async () => {
    const existing = {
      id: 1,
      slug: 'historia',
      title: 'Historia',
      content: 'Contenido',
      status: 'draft',
      seo: null,
      sections: [],
      attachments: [],
      publishedAt: null,
      createdAt: new Date('2024-01-01T00:00:00Z'),
      updatedAt: new Date('2024-01-01T00:00:00Z')
    };

    mockRepository.findBySlug.mockResolvedValue(existing);
    mockRepository.updateBySlug.mockResolvedValue({
      ...existing,
      status: 'published',
      publishedAt: new Date('2024-03-01T00:00:00Z')
    });

    const result = await publicPageService.updatePage('historia', 2, { status: 'published' });

    expect(mockRepository.updateBySlug).toHaveBeenCalledWith(
      'historia',
      expect.objectContaining({
        status: 'published',
        publishedAt: expect.any(Date)
      })
    );
    expect(result.status).toBe('published');
  });
});
