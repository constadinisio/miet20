<?php
require_once __DIR__ . '/../../../backend/notificaciones/notificaciones_utils.php';

header('Content-Type: application/json');

$recipientId = isset($_POST['id']) ? (int) $_POST['id'] : 0;

if ($recipientId <= 0) {
    http_response_code(400);
    echo json_encode([
        'error' => 'El identificador de la notificación es inválido',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $response = miEt20NotificacionesMarcarLeidaRaw($recipientId);
    $statusCode = isset($response['status']) ? (int) $response['status'] : 200;
    http_response_code($statusCode);
    echo json_encode($response['data'] ?? ['ok' => true], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $statusCode = $exception->getCode() >= 400 ? (int) $exception->getCode() : 500;
    http_response_code($statusCode);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
