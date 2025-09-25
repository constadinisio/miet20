<?php
require_once __DIR__ . '/../../../backend/notificaciones/notificaciones_utils.php';

header('Content-Type: application/json');

try {
    $response = miEt20NotificacionesListarRaw();
    $statusCode = isset($response['status']) ? (int) $response['status'] : 200;
    http_response_code($statusCode);
    echo json_encode($response['data'] ?? [], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $statusCode = $exception->getCode() >= 400 ? (int) $exception->getCode() : 500;
    http_response_code($statusCode);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
