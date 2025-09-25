<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['error' => 'Acceso denegado']);
    exit;
}

require_once __DIR__ . '/api_client.php';

try {
    $cargos = cargos_call_api('GET', '/cargos');
    echo json_encode($cargos, JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
