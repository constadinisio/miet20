<?php

require_once __DIR__ . '/../../../includes/api_client.php';

/**
 * Devuelve el listado de materias asociadas a un curso utilizando la API.
 *
 * @return array<int, array{id:int,nombre:string}>
 */
function obtenerMateriasCurso($conexionNoUsado, int $cursoId): array
{
    unset($conexionNoUsado);

    if ($cursoId <= 0) {
        return [];
    }

    try {
        $response = miEt20ApiAuthenticatedRequest('GET', '/report-cards/helpers/course-subjects', null, [
            'query' => ['courseId' => $cursoId],
        ]);
    } catch (RuntimeException $exception) {
        return [];
    }

    $data = $response['data'] ?? [];
    if (!is_array($data)) {
        return [];
    }

    $materias = [];
    foreach ($data as $item) {
        if (!is_array($item)) {
            continue;
        }

        $id = isset($item['id']) ? (int) $item['id'] : 0;
        $nombre = $item['name'] ?? ($item['nombre'] ?? '');

        if ($id <= 0 || $nombre === '') {
            continue;
        }

        $materias[] = [
            'id' => $id,
            'nombre' => $nombre,
        ];
    }

    return $materias;
}

/**
 * Obtiene el último período (por fecha de carga) con notas registradas para un alumno en un curso.
 */
function obtenerUltimoPeriodoConNotas($conexionNoUsado, int $cursoId, int $alumnoId, ?int $anioLectivo = null): ?string
{
    unset($conexionNoUsado);

    if ($cursoId <= 0 || $alumnoId <= 0) {
        return null;
    }

    $query = [
        'courseId' => $cursoId,
        'studentId' => $alumnoId,
    ];

    $anioLectivo = $anioLectivo ?: (int) date('Y');
    if ($anioLectivo > 0) {
        $query['academicYear'] = $anioLectivo;
    }

    try {
        $response = miEt20ApiAuthenticatedRequest('GET', '/report-cards/helpers/latest-term', null, [
            'query' => $query,
        ]);
    } catch (RuntimeException $exception) {
        return null;
    }

    $data = $response['data'] ?? [];
    $periodo = is_array($data) ? ($data['term'] ?? null) : null;

    return is_string($periodo) && $periodo !== '' ? $periodo : null;
}

/**
 * Lista las materias que aún no tienen nota para un período determinado.
 *
 * @return string[]
 */
function obtenerMateriasSinNotasPorPeriodo($conexionNoUsado, int $cursoId, int $alumnoId, string $periodo, ?int $anioLectivo = null): array
{
    unset($conexionNoUsado);

    if ($cursoId <= 0 || $alumnoId <= 0 || $periodo === '') {
        return [];
    }

    $query = [
        'courseId' => $cursoId,
        'studentId' => $alumnoId,
        'term' => $periodo,
    ];

    $anioLectivo = $anioLectivo ?: (int) date('Y');
    if ($anioLectivo > 0) {
        $query['academicYear'] = $anioLectivo;
    }

    try {
        $response = miEt20ApiAuthenticatedRequest('GET', '/report-cards/helpers/missing-subjects', null, [
            'query' => $query,
        ]);
    } catch (RuntimeException $exception) {
        return [];
    }

    $data = $response['data'] ?? [];
    if (!is_array($data)) {
        return [];
    }

    $faltantes = [];
    foreach ($data as $nombre) {
        if (!is_string($nombre) || $nombre === '') {
            continue;
        }
        $faltantes[] = $nombre;
    }

    return $faltantes;
}
