<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

$cargoId = isset($_POST['id']) ? (int) $_POST['id'] : null;
if (!$cargoId) {
    $_SESSION['error_cargos'] = 'ID de cargo no especificado';
    header('Location: /users/admin/cargos.php');
    exit;
}

try {
    cargos_call_api('POST', '/cargos/' . $cargoId . '/reactivar');
    header('Location: /users/admin/cargos.php?msg=cargo_reactivado');
    exit;
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    $_SESSION['error_cargos'] = 'Error al reactivar cargo: ' . $exception->getMessage();
    header('Location: /users/admin/cargos.php');
    exit;
}
