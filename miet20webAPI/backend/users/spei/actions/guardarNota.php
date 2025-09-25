<?php
require_once __DIR__ . '/../helpers.php';

header('Content-Type: application/json; charset=UTF-8');

try {
    spei_assert_authorized();
} catch (RuntimeException $exception) {
    $status = $exception->getCode() ?: 403;
    http_response_code($status);
    echo json_encode([
        'success' => false,
        'error' => $exception->getMessage()
    ]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'error' => 'Método no permitido'
    ]);
    exit;
}

$mensaje = trim((string) ($_POST['mensaje'] ?? ''));
if ($mensaje === '') {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'error' => 'El mensaje no puede estar vacío.'
    ]);
    exit;
}

$autor = trim((string) ($_SESSION['usuario']['nombre'] ?? ''));
$apellido = trim((string) ($_SESSION['usuario']['apellido'] ?? ''));
if ($apellido !== '') {
    $autor = $autor !== '' ? $autor . ' ' . $apellido : $apellido;
}

$payload = [
    'mensaje' => $mensaje,
    'autor' => $autor !== '' ? $autor : null
];

try {
    $response = spei_call_api('POST', '/devices/pizarron', $payload);
    $note = $response['data'] ?? [];

    echo json_encode([
        'success' => true,
        'nota' => $note
    ]);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 400 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'success' => false,
        'error' => $exception->getMessage() ?: 'No se pudo guardar la nota.'
    ]);
}
