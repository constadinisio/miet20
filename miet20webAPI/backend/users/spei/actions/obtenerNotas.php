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

try {
    $response = spei_call_api('GET', '/devices/pizarron');
    $notes = $response['data'] ?? [];
    if (!is_array($notes)) {
        $notes = [];
    }

    echo json_encode($notes);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 400 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'success' => false,
        'error' => $exception->getMessage() ?: 'No se pudieron obtener las notas.'
    ]);
}
