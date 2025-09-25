<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['error' => 'No autorizado']);
    exit;
}

$rawInput = file_get_contents('php://input');
if ($rawInput === false) {
    http_response_code(400);
    echo json_encode(['error' => 'No se pudo leer la petición']);
    exit;
}

$data = json_decode($rawInput, true);
if (!is_array($data)) {
    http_response_code(400);
    echo json_encode(['error' => 'Formato de datos inválido']);
    exit;
}

if (!isset($_SESSION['csrf']) || ($data['csrf'] ?? '') !== $_SESSION['csrf']) {
    http_response_code(403);
    echo json_encode(['error' => 'CSRF inválido']);
    exit;
}

$courseOriginId = isset($data['curso_origen_id']) ? (int) $data['curso_origen_id'] : 0;
$students = isset($data['alumnos']) && is_array($data['alumnos']) ? $data['alumnos'] : [];

if ($courseOriginId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'El curso de origen es inválido']);
    exit;
}

if ($students === []) {
    http_response_code(400);
    echo json_encode(['error' => 'Debe indicar alumnos para procesar']);
    exit;
}

require_once __DIR__ . '/api_client.php';

try {
    $response = admin_call_api('POST', '/alumnos/progresion', [
        'curso_origen_id' => $courseOriginId,
        'alumnos' => $students,
    ]);

    $message = is_array($response) ? ($response['message'] ?? null) : null;
    $processed = is_array($response) ? ($response['processed'] ?? null) : null;
    $errors = is_array($response) ? ($response['errors'] ?? null) : null;

    echo json_encode([
        'ok' => empty($errors),
        'mensaje' => $message ?? 'Progresión aplicada con éxito.',
        'procesados' => $processed,
        'errores' => $errors,
    ], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
