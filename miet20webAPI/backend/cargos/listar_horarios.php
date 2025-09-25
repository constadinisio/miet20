<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'error' => 'Acceso denegado']);
    exit;
}

$cargoId = isset($_GET['cargo_id']) ? (int) $_GET['cargo_id'] : null;
if (!$cargoId) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Falta cargo_id']);
    exit;
}

require_once __DIR__ . '/api_client.php';

try {
    $horarios = cargos_call_api('GET', '/cargos/' . $cargoId . '/horarios');
    echo json_encode(['ok' => true, 'horarios' => $horarios], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'ok' => false,
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
