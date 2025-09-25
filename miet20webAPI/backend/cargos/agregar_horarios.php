<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'error' => 'Acceso denegado']);
    exit;
}

$cargoId = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : null;
$dias = json_decode($_POST['dias'] ?? '[]', true);
$horaInicio = $_POST['hora_inicio'] ?? null;
$horaFin = $_POST['hora_fin'] ?? null;
$tipo = $_POST['tipo'] ?? 'clase';

require_once __DIR__ . '/api_client.php';

if (!$cargoId || !is_array($dias) || empty($dias) || !$horaInicio || !$horaFin) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'error' => 'Datos incompletos',
    ]);
    exit;
}

try {
    $payload = [
        'dias' => array_values(array_map('strval', $dias)),
        'hora_inicio' => $horaInicio,
        'hora_fin' => $horaFin,
        'tipo' => $tipo,
    ];

    $response = cargos_call_api('POST', '/cargos/' . $cargoId . '/horarios', $payload);
    $horarios = is_array($response) ? ($response['horarios'] ?? $response) : [];

    echo json_encode([
        'ok' => true,
        'horarios' => $horarios,
    ], JSON_UNESCAPED_UNICODE);
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