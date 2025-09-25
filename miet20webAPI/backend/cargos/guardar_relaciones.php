<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'error' => 'Sin permisos'], JSON_UNESCAPED_UNICODE);
    exit;
}

require_once __DIR__ . '/api_client.php';

$cargoId = isset($_POST['id']) ? (int) $_POST['id'] : null;
$materiasInput = $_POST['materias'] ?? '[]';
$materias = json_decode($materiasInput, true);

if (!$cargoId || !is_array($materias)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Datos incompletos'], JSON_UNESCAPED_UNICODE);
    exit;
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
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Debés seleccionar al menos una materia'], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $response = cargos_call_api('POST', '/cargos/' . $cargoId . '/relaciones', [
        'materias' => $materiasPayload,
    ]);

    echo json_encode([
        'ok' => true,
        'data' => $response,
        'msg' => 'Relaciones guardadas',
    ], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'ok' => false,
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
