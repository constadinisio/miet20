<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$cargoId = isset($_POST['id']) ? (int) $_POST['id'] : null;
$codigo = $_POST['codigo_cargo'] ?? null;
$tipoCargoId = isset($_POST['tipo_cargo_id']) ? (int) $_POST['tipo_cargo_id'] : null;
$tipo = $_POST['tipo'] ?? ($_POST['situacion'] ?? null);
$estado = $_POST['estado'] ?? 'activo';
$docenteId = isset($_POST['docente_id']) && $_POST['docente_id'] !== '' ? (int) $_POST['docente_id'] : null;
$fechaInicio = isset($_POST['fecha_inicio']) && $_POST['fecha_inicio'] !== '' ? $_POST['fecha_inicio'] : null;
$fechaFin = isset($_POST['fecha_fin']) && $_POST['fecha_fin'] !== '' ? $_POST['fecha_fin'] : null;
$observaciones = $_POST['observaciones'] ?? null;

if (!$cargoId || !$codigo || !$tipoCargoId || !$tipo) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Faltan datos obligatorios.'
    ], 400);
}

$payload = [
    'codigo_cargo' => $codigo,
    'tipo_cargo_id' => $tipoCargoId,
    'tipo' => $tipo,
    'situacion' => $tipo,
    'estado' => $estado,
    'docente_id' => $docenteId,
    'fecha_inicio' => $fechaInicio,
    'fecha_fin' => $fechaFin,
    'observaciones' => $observaciones,
];

try {
    $cargo = cargosApiRequest('PUT', '/cargos/' . $cargoId, $payload);
    cargosJsonResponse([
        'ok' => true,
        'data' => $cargo
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
