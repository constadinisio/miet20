<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$codigoCargo = $_POST['codigo_cargo'] ?? null;
$tipoCargoId = isset($_POST['tipo_cargo_id']) ? (int) $_POST['tipo_cargo_id'] : null;
$situacion = $_POST['situacion'] ?? ($_POST['tipo'] ?? null);
$estado = $_POST['estado'] ?? 'activo';
$docenteId = isset($_POST['docente_id']) && $_POST['docente_id'] !== '' ? (int) $_POST['docente_id'] : null;
$fechaInicio = isset($_POST['fecha_inicio']) && $_POST['fecha_inicio'] !== '' ? $_POST['fecha_inicio'] : null;
$fechaFin = isset($_POST['fecha_fin']) && $_POST['fecha_fin'] !== '' ? $_POST['fecha_fin'] : null;
$observaciones = $_POST['observaciones'] ?? null;

if (!$codigoCargo || !$tipoCargoId || !$situacion) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Faltan datos obligatorios'
    ], 400);
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
    $cargo = cargosApiRequest('POST', '/cargos', $payload);
    $cargoId = is_array($cargo) ? ($cargo['id'] ?? $cargo['cargo_id'] ?? null) : null;

    cargosJsonResponse([
        'ok' => true,
        'cargo_id' => $cargoId
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
