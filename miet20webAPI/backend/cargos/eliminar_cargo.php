<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

if (!isset($_POST['id'])) {
    $_SESSION['error_cargos'] = 'ID de cargo no especificado';
    header('Location: /users/admin/cargos.php');
    exit;
}

$cargoId = (int) $_POST['id'];

try {
    cargos_call_api('DELETE', '/cargos/' . $cargoId);
    header('Location: /users/admin/cargos.php?msg=cargo_inactivo');
    exit;
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    $_SESSION['error_cargos'] = 'Error al desactivar cargo: ' . $exception->getMessage();
    header('Location: /users/admin/cargos.php');
    exit;
}
