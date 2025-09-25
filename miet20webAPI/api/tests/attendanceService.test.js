import { jest } from '@jest/globals';

const mockAttendanceRepository = {
  record: jest.fn(),
  update: jest.fn(),
  listByCourseAndDate: jest.fn(),
  getTeacherAllowedWeekdays: jest.fn(),
  listCourseStudents: jest.fn(),
  listSubjectAttendances: jest.fn(),
  hasTeacherAssignment: jest.fn(),
  upsertSubjectAttendance: jest.fn(),
  getSubjectShift: jest.fn(),
  listGeneralByDateForShift: jest.fn(),
  deleteSubjectAttendanceByDate: jest.fn(),
  listGeneralSummary: jest.fn()
};

jest.unstable_mockModule('../src/repositories/attendanceRepository.js', () => ({
  attendanceRepository: mockAttendanceRepository
}));

const { attendanceService } = await import('../src/services/attendanceService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

describe('attendanceService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('recordAttendance crea un registro por cada alumno indicado', async () => {
    mockAttendanceRepository.record.mockImplementation(async (payload) => ({ id: Date.now(), ...payload }));

    const payload = {
      curso_id: 3,
      fecha: '2024-05-13',
      es_contraturno: 0,
      creado_por: 9,
      registros: [
        { alumno_id: 11, estado: 'P' },
        { alumno_id: 12, estado: 'A', es_contraturno: 1 }
      ]
    };

    await attendanceService.recordAttendance(payload);

    expect(mockAttendanceRepository.record).toHaveBeenCalledTimes(2);
    expect(mockAttendanceRepository.record).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({
        alumno_id: 11,
        curso_id: 3,
        fecha: '2024-05-13',
        estado: 'P',
        es_contraturno: 0,
        creado_por: 9
      })
    );
    expect(mockAttendanceRepository.record).toHaveBeenNthCalledWith(
      2,
      expect.objectContaining({
        alumno_id: 12,
        es_contraturno: 1
      })
    );
  });

  test('getAllowedDays calcula los días habilitados y el próximo válido', async () => {
    mockAttendanceRepository.getTeacherAllowedWeekdays.mockResolvedValue([1, 3, 5]);

    const result = await attendanceService.getAllowedDays({
      teacherId: 4,
      courseId: 2,
      subjectId: 7,
      baseDate: '2024-05-14'
    });

    expect(mockAttendanceRepository.getTeacherAllowedWeekdays).toHaveBeenCalledWith(4, 2, 7);
    expect(result.allowed_dows).toEqual([1, 3, 5]);
    expect(result.allowed_today).toBe(false);
    expect(result.next_valid).toBe('2024-05-15');
    expect(result.week).toHaveLength(5);
  });

  test('listWeeklySubjectMatrix arma filas y columnas a partir de asistencias existentes', async () => {
    mockAttendanceRepository.getTeacherAllowedWeekdays.mockResolvedValue([1, 2, 3, 4, 5]);
    mockAttendanceRepository.listCourseStudents.mockResolvedValue([
      { id: 11, apellido: 'Pérez', nombre: 'Ana' },
      { id: 12, apellido: 'López', nombre: 'Juan' }
    ]);
    mockAttendanceRepository.listSubjectAttendances.mockResolvedValue([
      { alumno_id: 11, fecha: '2024-05-13', estado: 'P' },
      { alumno_id: 12, fecha: '2024-05-14', estado: 'A' }
    ]);

    const matrix = await attendanceService.getWeeklySubjectMatrix({
      teacherId: 4,
      courseId: 2,
      subjectId: 7,
      baseDate: '2024-05-13'
    });

    expect(matrix.columnas[0]).toBe('Nro');
    expect(matrix.columnas[1]).toBe('Nombre');
    expect(matrix.filas[0][0]).toBe(1);
    expect(matrix.filas[0][2]).toBe('P');
    expect(matrix.filas[1][3]).toBe('A');
    expect(matrix.editable.every(Boolean)).toBe(true);
  });

  test('saveSubjectAttendanceMatrix valida permisos y normaliza estados', async () => {
    mockAttendanceRepository.hasTeacherAssignment.mockResolvedValue(true);
    mockAttendanceRepository.getTeacherAllowedWeekdays.mockResolvedValue([1, 2, 3, 4, 5]);
    mockAttendanceRepository.listCourseStudents.mockResolvedValue([
      { id: 21, apellido: 'Soto', nombre: 'Luis' },
      { id: 22, apellido: 'Tevez', nombre: 'Mia' }
    ]);

    const resultado = await attendanceService.saveSubjectAttendanceMatrix({
      teacherId: 4,
      courseId: 9,
      subjectId: 6,
      encabezados: ['Nro', 'Nombre', '2024-05-13', '2024-05-14'],
      asistencias: [
        { nro: '1', estados: { 2: 'presente', 3: 'ausente' } },
        { nro: '2', estados: { 2: 'T' } }
      ]
    });

    expect(mockAttendanceRepository.upsertSubjectAttendance).toHaveBeenCalledTimes(3);
    expect(mockAttendanceRepository.upsertSubjectAttendance).toHaveBeenCalledWith(
      expect.objectContaining({ alumno_id: 21, fecha: '2024-05-13', estado: 'P' })
    );
    expect(mockAttendanceRepository.upsertSubjectAttendance).toHaveBeenCalledWith(
      expect.objectContaining({ alumno_id: 21, fecha: '2024-05-14', estado: 'A' })
    );
    expect(resultado).toEqual({ applied: 3, ignored: 0 });
  });

  test('saveSubjectAttendanceMatrix lanza error si el docente no tiene la asignación', async () => {
    mockAttendanceRepository.hasTeacherAssignment.mockResolvedValue(false);

    await expect(
      attendanceService.saveSubjectAttendanceMatrix({
        teacherId: 1,
        courseId: 2,
        subjectId: 3,
        encabezados: ['Nro', 'Nombre', '2024-05-13'],
        asistencias: []
      })
    ).rejects.toThrow(ApiError);
  });

  test('importSubjectAttendanceFromGeneral copia registros generales y devuelve resumen', async () => {
    mockAttendanceRepository.hasTeacherAssignment.mockResolvedValue(true);
    mockAttendanceRepository.getSubjectShift.mockResolvedValue({ es_contraturno: 0 });
    mockAttendanceRepository.listGeneralByDateForShift.mockResolvedValue([
      { alumno_id: 21, estado: 'p' },
      { alumno_id: 22, estado: 'A' }
    ]);
    mockAttendanceRepository.listCourseStudents.mockResolvedValue([
      { id: 21 },
      { id: 22 }
    ]);

    const result = await attendanceService.importSubjectAttendanceFromGeneral({
      teacherId: 5,
      courseId: 9,
      subjectId: 8,
      date: '2024-05-13'
    });

    expect(mockAttendanceRepository.deleteSubjectAttendanceByDate).toHaveBeenCalledWith(9, 8, '2024-05-13');
    expect(mockAttendanceRepository.upsertSubjectAttendance).toHaveBeenCalledTimes(2);
    expect(result).toMatchObject({
      applied: 2,
      turno: 'TURNO',
      map: { 1: 'P', 2: 'A' }
    });
  });

  test('getCourseSummary consolida la asistencia de ambos turnos', async () => {
    mockAttendanceRepository.listGeneralSummary.mockResolvedValue([
      { es_contraturno: 0, presentes: 10, ausentes: 2, tarde: 1, total: 13 },
      { es_contraturno: 1, presentes: 4, ausentes: 1, tarde: 0, total: 5 }
    ]);

    const summary = await attendanceService.getCourseSummary(6, '2024-05-13');

    expect(summary.turno.presentes).toBe(10);
    expect(summary.contraturno.ausentes).toBe(1);
    expect(summary.totales.total).toBe(18);
    expect(summary.fecha).toBe('2024-05-13');
  });
});
