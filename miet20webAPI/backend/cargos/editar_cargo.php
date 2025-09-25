<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

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
    $_SESSION['error_cargos'] = 'Faltan datos obligatorios.';
    header('Location: /users/admin/cargos_form.php?id=' . ($cargoId ?: ''));
    exit;
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
    cargos_call_api('PUT', '/cargos/' . $cargoId, $payload);
    header('Location: /users/admin/cargos.php?msg=editado');
    exit;
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    $_SESSION['error_cargos'] = 'Error al editar cargo: ' . $exception->getMessage();
    header('Location: /users/admin/cargos_form.php?id=' . $cargoId);
    exit;
}
