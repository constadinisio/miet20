import { jest } from '@jest/globals';

const mockRepository = {
  create: jest.fn(),
  list: jest.fn(),
  findById: jest.fn(),
  update: jest.fn()
};

jest.unstable_mockModule('../src/repositories/contactMessageRepository.js', () => ({
  contactMessageRepository: mockRepository
}));

const { contactMessageService } = await import('../src/services/contactMessageService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

describe('contactMessageService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('submitMessage normaliza adjuntos y email', async () => {
    mockRepository.create.mockResolvedValue({
      id: 1,
      name: 'Juan Pérez',
      email: 'test@example.com',
      subject: 'Consulta',
      message: 'Necesito información',
      status: 'new',
      attachments: [{ id: 3 }, { id: 5, name: 'Acta.pdf' }],
      metadata: null,
      assignedTo: null,
      notes: null,
      resolvedAt: null,
      createdAt: new Date(),
      updatedAt: new Date()
    });

    const payload = {
      name: ' Juan Pérez ',
      email: 'Test@Example.com',
      subject: 'Consulta',
      message: 'Necesito información adicional',
      attachments: ['3', { id: '5', name: 'Acta.pdf' }]
    };

    const result = await contactMessageService.submitMessage(payload);

    expect(mockRepository.create).toHaveBeenCalledWith(
      expect.objectContaining({
        email: 'test@example.com',
        attachments: [{ id: 3 }, { id: 5, name: 'Acta.pdf' }]
      })
    );
    expect(result.attachments).toHaveLength(2);
  });

  test('listMessages lanza error cuando el estado es inválido', async () => {
    await expect(contactMessageService.listMessages({ status: 'closed' })).rejects.toThrow(
      ApiError
    );
    expect(mockRepository.list).not.toHaveBeenCalled();
  });

  test('updateMessage establece resolvedAt cuando se resuelve', async () => {
    const existing = {
      id: 9,
      name: 'Test',
      email: 'test@example.com',
      subject: 'Consulta',
      message: 'Mensaje',
      status: 'in_progress',
      attachments: [],
      metadata: null,
      assignedTo: null,
      notes: null,
      resolvedAt: null,
      createdAt: new Date('2024-01-01T00:00:00Z'),
      updatedAt: new Date('2024-01-01T00:00:00Z')
    };

    mockRepository.findById.mockResolvedValue(existing);
    mockRepository.update.mockResolvedValue({
      ...existing,
      status: 'resolved',
      resolvedAt: new Date('2024-02-01T00:00:00Z')
    });

    const result = await contactMessageService.updateMessage(9, { status: 'resolved' });

    expect(mockRepository.update).toHaveBeenCalledWith(
      9,
      expect.objectContaining({
        status: 'resolved',
        resolvedAt: expect.any(Date)
      })
    );
    expect(result.status).toBe('resolved');
  });
});
