<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'error' => 'Acceso denegado'], JSON_UNESCAPED_UNICODE);
    exit;
}

require_once __DIR__ . '/api_client.php';

$codigoCargo = $_POST['codigo_cargo'] ?? null;
$tipoCargoId = isset($_POST['tipo_cargo_id']) ? (int) $_POST['tipo_cargo_id'] : null;
$situacion = $_POST['situacion'] ?? ($_POST['tipo'] ?? null);
$estado = $_POST['estado'] ?? 'activo';
$docenteId = isset($_POST['docente_id']) && $_POST['docente_id'] !== '' ? (int) $_POST['docente_id'] : null;
$fechaInicio = isset($_POST['fecha_inicio']) && $_POST['fecha_inicio'] !== '' ? $_POST['fecha_inicio'] : null;
$fechaFin = isset($_POST['fecha_fin']) && $_POST['fecha_fin'] !== '' ? $_POST['fecha_fin'] : null;
$observaciones = $_POST['observaciones'] ?? null;

if (!$codigoCargo || !$tipoCargoId || !$situacion) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Faltan datos obligatorios'], JSON_UNESCAPED_UNICODE);
    exit;
}

$payload = [
    'codigo_cargo' => $codigoCargo,
    'tipo_cargo_id' => $tipoCargoId,
    'tipo' => $situacion,
    'situacion' => $situacion,
    'estado' => $estado,
    'docente_id' => $docenteId,
    'fecha_inicio' => $fechaInicio,
    'fecha_fin' => $fechaFin,
    'observaciones' => $observaciones,
];

try {
    $cargo = cargos_call_api('POST', '/cargos', $payload);
    $cargoId = is_array($cargo) ? ($cargo['id'] ?? $cargo['cargo_id'] ?? null) : null;

    echo json_encode([
        'ok' => true,
        'cargo_id' => $cargoId,
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
