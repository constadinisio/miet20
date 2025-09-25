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

$noteId = filter_input(INPUT_POST, 'id', FILTER_VALIDATE_INT);
if (!$noteId) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => 'El identificador de la nota es inválido.'
    ]);
    exit;
}

try {
    spei_call_api('DELETE', '/devices/pizarron/' . $noteId);
    echo json_encode(['success' => true]);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 400 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'success' => false,
        'error' => $exception->getMessage() ?: 'No se pudo borrar la nota.'
    ]);
}
