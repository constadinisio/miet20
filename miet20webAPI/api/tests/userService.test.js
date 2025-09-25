import { jest } from '@jest/globals';

const mockUserRepository = {
  findById: jest.fn()
};

const mockRoleRepository = {
  findForUser: jest.fn()
};

jest.unstable_mockModule('../src/repositories/userRepository.js', () => ({
  userRepository: mockUserRepository
}));

jest.unstable_mockModule('../src/repositories/roleRepository.js', () => ({
  roleRepository: mockRoleRepository
}));

const { userService } = await import('../src/services/userService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

describe('userService required fields', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('getRequiredProfileFields incluye campos extra para roles no alumnos', async () => {
    const result = await userService.getRequiredProfileFields({ roleId: 1 });

    expect(result.common).toHaveProperty('nombre', 'Nombre');
    expect(result.extra).toHaveProperty('ficha_censal', 'Ficha censal');
    expect(result.fields).toHaveProperty('ficha_censal');
  });

  test('getRequiredProfileFields omite campos extra para alumnos', async () => {
    const result = await userService.getRequiredProfileFields({ roleId: 4 });

    expect(result.extra).toEqual({});
    expect(result.fields).not.toHaveProperty('ficha_censal');
  });

  test('getUserMissingProfileFields detecta campos faltantes', async () => {
    mockUserRepository.findById.mockResolvedValue({
      id: 10,
      nombre: 'Ana',
      apellido: '',
      mail: 'ana@example.com',
      telefono: null,
      direccion: 'Calle 123',
      dni: null,
      fecha_nacimiento: '0000-00-00',
      rol: 1,
      ficha_censal: null
    });
    mockRoleRepository.findForUser.mockResolvedValue([]);

    const result = await userService.getUserMissingProfileFields(10, {});

    expect(result.missing).toHaveProperty('apellido', 'Apellido');
    expect(result.missing).toHaveProperty('dni', 'DNI');
    expect(result.missing).toHaveProperty('telefono', 'Teléfono');
    expect(result.missing).toHaveProperty('fecha_nacimiento', 'Fecha de nacimiento');
    expect(result.missing).toHaveProperty('ficha_censal', 'Ficha censal');
  });

  test('getUserMissingProfileFields usa rol adicional cuando no hay principal', async () => {
    mockUserRepository.findById.mockResolvedValue({
      id: 11,
      nombre: 'Docente',
      apellido: 'Perez',
      mail: 'docente@example.com',
      telefono: '12345',
      direccion: 'Escuela 123',
      dni: '40111222',
      fecha_nacimiento: '1980-03-10',
      rol: null,
      ficha_censal: 'en-regla'
    });
    mockRoleRepository.findForUser.mockResolvedValue([{ id: 4 }]);

    const result = await userService.getUserMissingProfileFields(11, {});

    expect(result.roleId).toBe(4);
    expect(result.missing).toEqual({});
  });

  test('getUserMissingProfileFields lanza error si el usuario no existe', async () => {
    mockUserRepository.findById.mockResolvedValue(null);

    await expect(userService.getUserMissingProfileFields(999, {})).rejects.toBeInstanceOf(ApiError);
  });
});
