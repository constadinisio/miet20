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

$id = filter_input(INPUT_POST, 'id', FILTER_VALIDATE_INT);
$estado = trim((string) ($_POST['estado'] ?? ''));

if (!$id) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => 'El identificador de la netbook es inválido.'
    ]);
    exit;
}

$estadosValidos = ['En uso', 'Dañada', 'Hurto', 'Obsoleta'];
if (!in_array($estado, $estadosValidos, true)) {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'error' => 'El estado seleccionado no es válido.'
    ]);
    exit;
}

try {
    spei_call_api('PUT', '/devices/' . $id, ['estado' => $estado]);
    echo json_encode([
        'success' => true,
        'message' => 'Estado actualizado correctamente.'
    ]);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 400 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'success' => false,
        'error' => $exception->getMessage() ?: 'No se pudo actualizar el estado.'
    ]);
}
