<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header('Location: /login.php?error=rol');
    exit;
}

require_once __DIR__ . '/api_client.php';

$rawId = $_GET['id'] ?? null;
$studentId = filter_var($rawId, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;

if ($studentId === null) {
    header('Location: /users/admin/alumnos.php?error=datos_invalidos');
    exit;
}

try {
    admin_call_api('DELETE', '/alumnos/' . $studentId);
    header('Location: /users/admin/alumnos.php?ok=eliminado');
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_alumnos_error'] = $exception->getMessage();
    $status = (int) $exception->getCode();

    if ($status === 404) {
        header('Location: /users/admin/alumnos.php?error=alumno_no_encontrado');
        exit;
    }

    header('Location: /users/admin/alumnos.php?error=api');
    exit;
}