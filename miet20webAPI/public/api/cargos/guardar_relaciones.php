<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$cargoId = isset($_POST['id']) ? (int) $_POST['id'] : null;
$materiasInput = $_POST['materias'] ?? '[]';
$materias = is_string($materiasInput) ? json_decode($materiasInput, true) : $materiasInput;

if (!$cargoId || !is_array($materias)) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Datos incompletos'
    ], 400);
}

$materiasPayload = [];
foreach ($materias as $materia) {
    $materiaId = isset($materia['id']) ? (int) $materia['id'] : null;
    if (!$materiaId) {
        continue;
    }

    $cursosPayload = [];
    if (!empty($materia['cursos']) && is_array($materia['cursos'])) {
        foreach ($materia['cursos'] as $curso) {
            $cursoId = isset($curso['id']) ? (int) $curso['id'] : null;
            if (!$cursoId) {
                continue;
            }

            $horarios = [];
            if (!empty($curso['horarios']) && is_array($curso['horarios'])) {
                foreach ($curso['horarios'] as $horarioId) {
                    $horarioId = (int) $horarioId;
                    if ($horarioId > 0 && !in_array($horarioId, $horarios, true)) {
                        $horarios[] = $horarioId;
                    }
                }
            }

            $cursosPayload[] = [
                'id' => $cursoId,
                'horarios' => $horarios,
            ];
        }
    }

    $materiasPayload[] = [
        'id' => $materiaId,
        'cursos' => $cursosPayload,
    ];
}

if (empty($materiasPayload)) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Debés seleccionar al menos una materia'
    ], 400);
}

try {
    $response = cargosApiRequest('POST', '/cargos/' . $cargoId . '/relaciones', [
        'materias' => $materiasPayload,
    ]);

    cargosJsonResponse([
        'ok' => true,
        'data' => $response,
        'msg' => 'Relaciones guardadas'
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
