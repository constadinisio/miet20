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

$courseId = isset($data['curso_id']) ? (int) $data['curso_id'] : 0;
$subjectId = isset($data['materia_id']) ? (int) $data['materia_id'] : 0;
$cargoId = isset($data['cargo_id']) ? (int) $data['cargo_id'] : 0; // Se recibe por compatibilidad, la API valida la asignación
$headers = $data['encabezados'] ?? null;
$attendances = $data['asistencias'] ?? null;

if ($courseId <= 0 || $subjectId <= 0 || $cargoId <= 0) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'mensaje' => 'Parámetros incompletos']);
    exit;
}

if (!is_array($headers) || empty($headers) || !is_array($attendances) || empty($attendances)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'mensaje' => 'Debe indicar encabezados y asistencias']);
    exit;
}

try {
    $response = miEt20ApiAuthenticatedRequest(
        'POST',
        sprintf('/attendance/cursos/%d/materias/%d/matriz', $courseId, $subjectId),
        [
            'encabezados' => $headers,
            'asistencias' => $attendances,
        ]
    );

    $payload = is_array($response['data'] ?? null) ? $response['data'] : [];
    $applied = isset($payload['applied']) ? (int) $payload['applied'] : 0;
    $ignored = isset($payload['ignored']) ? (int) $payload['ignored'] : 0;

    echo json_encode([
        'ok' => true,
        'mensaje' => sprintf('✅ Asistencias guardadas. Registros aplicados: %d. Ignorados: %d.', $applied, $ignored),
        'applied' => $applied,
        'ignored' => $ignored,
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
