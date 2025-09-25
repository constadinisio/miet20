import { jest } from '@jest/globals';
import httpStatus from 'http-status';

const mockCargoRepository = {
  create: jest.fn(),
  findById: jest.fn(),
  softDelete: jest.fn(),
  reactivate: jest.fn(),
  clearRelations: jest.fn(),
  insertCargoMateria: jest.fn(),
  insertCargoMateriaCurso: jest.fn(),
  insertCargoMateriaCursoHorario: jest.fn(),
  insertHorarioMateriaFromSchedule: jest.fn(),
  listAll: jest.fn(),
  listAllSchedules: jest.fn(),
  listSchedulesForCargo: jest.fn(),
  upsertSchedule: jest.fn(),
  listReplacementIds: jest.fn(),
  detachReplacementByReplacementId: jest.fn(),
  deleteById: jest.fn(),
  insertReplacement: jest.fn()
};

const trxStub = { id: 'trx' };
const transactionMock = jest.fn(async (callback) => callback(trxStub));
const mockDb = { transaction: transactionMock };

jest.unstable_mockModule('../src/repositories/cargoRepository.js', () => ({
  cargoRepository: mockCargoRepository
}));

jest.unstable_mockModule('../src/config/database.js', () => ({
  getDatabase: () => mockDb
}));

const { cargoService } = await import('../src/services/cargoService.js');

describe('cargoService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    transactionMock.mockImplementation(async (callback) => callback(trxStub));
  });

  test('reactivateCargo lanza error si el cargo no existe', async () => {
    mockCargoRepository.findById.mockResolvedValueOnce(null);

    await expect(cargoService.reactivateCargo(999)).rejects.toMatchObject({
      statusCode: httpStatus.NOT_FOUND
    });
    expect(mockCargoRepository.findById).toHaveBeenCalledWith(999);
    expect(mockCargoRepository.reactivate).not.toHaveBeenCalled();
  });

  test('reactivateCargo reactiva y devuelve el cargo actualizado', async () => {
    const existingCargo = { id: 12 };
    const updatedCargo = {
      id: 12,
      codigo_cargo: 'C-001',
      tipo_cargo_id: 3,
      tipo: 'titular',
      estado: 'activo',
      observaciones: null,
      fecha_inicio: '2024-03-01',
      fecha_fin: null,
      docente_id: 7
    };

    mockCargoRepository.findById
      .mockResolvedValueOnce(existingCargo)
      .mockResolvedValueOnce(updatedCargo);
    mockCargoRepository.reactivate.mockResolvedValueOnce(undefined);

    const result = await cargoService.reactivateCargo(12);

    expect(mockCargoRepository.reactivate).toHaveBeenCalledWith(12);
    expect(result).toEqual({
      id: 12,
      codigo_cargo: 'C-001',
      tipo_cargo_id: 3,
      tipo: 'titular',
      estado: 'activo',
      observaciones: null,
      fecha_inicio: '2024-03-01',
      fecha_fin: null,
      docente_id: 7
    });
  });

  test('replaceRelations lanza error si el cargo no existe', async () => {
    mockCargoRepository.findById.mockResolvedValueOnce(null);

    await expect(cargoService.replaceRelations(55, [])).rejects.toMatchObject({
      statusCode: httpStatus.NOT_FOUND
    });
  });

  test('replaceRelations permite limpiar todas las relaciones con un arreglo vacío', async () => {
    mockCargoRepository.findById.mockResolvedValueOnce({ id: 77 });

    const result = await cargoService.replaceRelations(77, []);

    expect(transactionMock).toHaveBeenCalledTimes(1);
    expect(mockCargoRepository.clearRelations).toHaveBeenCalledWith(77, { trx: trxStub });
    expect(mockCargoRepository.insertCargoMateria).not.toHaveBeenCalled();
    expect(result).toEqual({ materias: [] });
  });

  test('replaceRelations lanza error si ninguna materia es válida', async () => {
    mockCargoRepository.findById.mockResolvedValueOnce({ id: 88 });

    await expect(
      cargoService.replaceRelations(88, [
        { id: null },
        { id: 'abc', cursos: [{ id: '0' }] }
      ])
    ).rejects.toMatchObject({ statusCode: httpStatus.BAD_REQUEST });
  });

  test('replaceRelations lanza error si el payload no es un arreglo', async () => {
    mockCargoRepository.findById.mockResolvedValueOnce({ id: 91 });

    await expect(cargoService.replaceRelations(91, null)).rejects.toMatchObject({
      statusCode: httpStatus.BAD_REQUEST
    });
  });

  test('replaceRelations borra relaciones previas e inserta las nuevas', async () => {
    mockCargoRepository.findById.mockResolvedValue({ id: 42 });
    mockCargoRepository.insertCargoMateria.mockResolvedValueOnce(501).mockResolvedValueOnce(502);
    mockCargoRepository.insertCargoMateriaCurso.mockResolvedValueOnce(601);

    const payload = [
      {
        id: '3',
        cursos: [
          { id: '8', horarios: ['1', '2', '2'] },
          { id: '', horarios: ['3'] }
        ]
      },
      {
        id: '4',
        cursos: []
      }
    ];

    const result = await cargoService.replaceRelations(42, payload);

    expect(transactionMock).toHaveBeenCalledTimes(1);
    expect(mockCargoRepository.clearRelations).toHaveBeenCalledWith(42, { trx: trxStub });
    expect(mockCargoRepository.insertCargoMateria).toHaveBeenCalledWith(
      { cargoId: 42, materiaId: 3 },
      { trx: trxStub }
    );
    expect(mockCargoRepository.insertCargoMateria).toHaveBeenCalledWith(
      { cargoId: 42, materiaId: 4 },
      { trx: trxStub }
    );
    expect(mockCargoRepository.insertCargoMateriaCurso).toHaveBeenCalledWith(
      { cargoMateriaId: 501, cursoId: 8 },
      { trx: trxStub }
    );
    expect(mockCargoRepository.insertCargoMateriaCursoHorario).toHaveBeenNthCalledWith(
      1,
      { cargoMateriaCursoId: 601, horarioId: 1 },
      { trx: trxStub }
    );
    expect(mockCargoRepository.insertCargoMateriaCursoHorario).toHaveBeenNthCalledWith(
      2,
      { cargoMateriaCursoId: 601, horarioId: 2 },
      { trx: trxStub }
    );
    expect(mockCargoRepository.insertHorarioMateriaFromSchedule).toHaveBeenCalledWith(
      { cursoId: 8, materiaId: 3, horarioId: 1 },
      { trx: trxStub }
    );
    expect(mockCargoRepository.insertHorarioMateriaFromSchedule).toHaveBeenCalledWith(
      { cursoId: 8, materiaId: 3, horarioId: 2 },
      { trx: trxStub }
    );

    expect(result).toEqual({
      materias: [
        {
          materia_id: 3,
          cursos: [
            {
              curso_id: 8,
              horarios: [1, 2]
            }
          ]
        },
        {
          materia_id: 4,
          cursos: []
        }
      ]
    });
  });
});
