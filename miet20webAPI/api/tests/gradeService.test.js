import { jest } from '@jest/globals';

const mockGradeRepository = {
  create: jest.fn(),
  update: jest.fn(),
  listByStudent: jest.fn(),
  listByCourse: jest.fn(),
  findProfessorAssignment: jest.fn(),
  findActiveEnrollment: jest.fn(),
  findById: jest.fn(),
  findProfessorAssignmentForStudent: jest.fn()
};

jest.unstable_mockModule('../src/repositories/gradeRepository.js', () => ({
  gradeRepository: mockGradeRepository
}));

const { gradeService } = await import('../src/services/gradeService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

describe('gradeService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('recordGrade delega en el repositorio y retorna la entidad creada', async () => {
    const payload = {
      profesor_id: 2,
      curso_id: 3,
      alumno_id: 5,
      materia_id: 8,
      periodo: '1° Bimestre',
      nota: 9,
      promedio_actividades: 8
    };
    const created = { id: 10, ...payload };
    mockGradeRepository.create.mockResolvedValue(created);
    mockGradeRepository.findProfessorAssignment.mockResolvedValue({ id: 20 });
    mockGradeRepository.findActiveEnrollment.mockResolvedValue({ id: 30 });

    const result = await gradeService.recordGrade(payload);

    expect(mockGradeRepository.findProfessorAssignment).toHaveBeenCalledWith({
      cursoId: payload.curso_id,
      materiaId: payload.materia_id,
      profesorId: payload.profesor_id
    });
    expect(mockGradeRepository.findActiveEnrollment).toHaveBeenCalledWith(
      payload.alumno_id,
      payload.curso_id
    );
    expect(mockGradeRepository.create).toHaveBeenCalledWith(
      expect.objectContaining({
        alumno_id: payload.alumno_id,
        materia_id: payload.materia_id,
        periodo: payload.periodo,
        nota: payload.nota,
        promedio_actividades: payload.promedio_actividades,
        fecha_carga: expect.any(Date)
      })
    );
    expect(result).toEqual(created);
  });

  test('updateGrade devuelve la calificación actualizada', async () => {
    const updated = { id: 3, nota: 7 };
    mockGradeRepository.findById.mockResolvedValue({
      id: 3,
      alumno_id: 5,
      materia_id: 8
    });
    mockGradeRepository.findProfessorAssignmentForStudent.mockResolvedValue({
      curso_id: 4
    });
    mockGradeRepository.update.mockResolvedValue(updated);

    const result = await gradeService.updateGrade(3, { nota: 7 }, 2);

    expect(mockGradeRepository.findById).toHaveBeenCalledWith(3);
    expect(mockGradeRepository.findProfessorAssignmentForStudent).toHaveBeenCalledWith({
      alumnoId: 5,
      materiaId: 8,
      profesorId: 2
    });
    expect(mockGradeRepository.update).toHaveBeenCalledWith(3, { nota: 7 });
    expect(result).toEqual(updated);
  });

  test('updateGrade lanza ApiError si la calificación no existe', async () => {
    mockGradeRepository.findById.mockResolvedValue(null);

    await expect(gradeService.updateGrade(99, { nota: 6 }, 3)).rejects.toThrow(ApiError);
  });

  test('listByStudent retorna las calificaciones del alumno solicitado', async () => {
    const grades = [{ id: 1, nota: 8 }];
    mockGradeRepository.listByStudent.mockResolvedValue(grades);

    const result = await gradeService.listByStudent(7);

    expect(mockGradeRepository.listByStudent).toHaveBeenCalledWith(7);
    expect(result).toEqual(grades);
  });

  test('listByCourse retorna las calificaciones del curso', async () => {
    const grades = [{ id: 4, nota: 10 }];
    mockGradeRepository.listByCourse.mockResolvedValue(grades);

    const result = await gradeService.listByCourse(12);

    expect(mockGradeRepository.listByCourse).toHaveBeenCalledWith(12, {});
    expect(result).toEqual(grades);
  });

  test('listByCourse aplica los filtros recibidos', async () => {
    const grades = [{ id: 8, nota: 6 }];
    mockGradeRepository.listByCourse.mockResolvedValue(grades);

    const filters = { materia_id: 5 };
    const result = await gradeService.listByCourse(3, filters);

    expect(mockGradeRepository.listByCourse).toHaveBeenCalledWith(3, filters);
    expect(result).toEqual(grades);
  });
});
