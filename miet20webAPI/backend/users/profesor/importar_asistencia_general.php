<?php
declare(strict_types=1);

session_start();

require_once __DIR__ . '/../../../backend/includes/api_client.php';

header('Content-Type: application/json; charset=UTF-8');

if ((int) ($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'mensaje' => 'Acceso denegado']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['ok' => false, 'mensaje' => 'Método no permitido']);
    exit;
}

$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true);

if (!is_array($data)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'mensaje' => 'El cuerpo de la petición es inválido']);
    exit;
}

$csrf = (string) ($data['csrf'] ?? '');
if (!isset($_SESSION['csrf']) || !hash_equals((string) $_SESSION['csrf'], $csrf)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'mensaje' => 'CSRF inválido']);
    exit;
}

$courseId = isset($data['curso_id']) ? (int) $data['curso_id'] : 0;
$subjectId = isset($data['materia_id']) ? (int) $data['materia_id'] : 0;
$cargoId = isset($data['cargo_id']) ? (int) $data['cargo_id'] : 0; // Para compatibilidad con la interfaz
$date = isset($data['fecha']) && $data['fecha'] !== '' ? (string) $data['fecha'] : date('Y-m-d');

if ($courseId <= 0 || $subjectId <= 0 || $cargoId <= 0 || $date === '') {
    http_response_code(400);
    echo json_encode(['ok' => false, 'mensaje' => 'Parámetros incompletos']);
    exit;
}

try {
    $response = miEt20ApiAuthenticatedRequest(
        'POST',
        sprintf('/attendance/cursos/%d/materias/%d/importar-general', $courseId, $subjectId),
        [
            'fecha' => $date,
        ]
    );

    $payload = is_array($response['data'] ?? null) ? $response['data'] : [];
    $applied = isset($payload['applied']) ? (int) $payload['applied'] : (isset($payload['importados']) ? (int) $payload['importados'] : 0);
    $turno = isset($payload['turno']) ? (string) $payload['turno'] : '';
    $map = isset($payload['map']) && is_array($payload['map']) ? $payload['map'] : [];

    echo json_encode([
        'ok' => true,
        'mensaje' => $applied > 0
            ? sprintf('Se importaron %d asistencias del preceptor (%s).', $applied, $turno !== '' ? $turno : 'turno automático')
            : 'No hay asistencias generales para la fecha seleccionada.',
        'importados' => $applied,
        'turno' => $turno,
        'map' => $map,
    ], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 400 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'ok' => false,
        'mensaje' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
