<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header('Location: /login.php?error=rol');
    exit;
}

if (!isset($_POST['csrf']) || !isset($_SESSION['csrf']) || $_POST['csrf'] !== $_SESSION['csrf']) {
    http_response_code(403);
    echo 'Token CSRF inválido.';
    exit;
}

$studentId = isset($_POST['alumno_id']) ? (int) $_POST['alumno_id'] : 0;
if ($studentId <= 0) {
    http_response_code(400);
    echo 'ID de alumno inválido.';
    exit;
}

require_once __DIR__ . '/api_client.php';

try {
    admin_call_api('DELETE', '/alumnos/' . $studentId);
    header('Location: /users/admin/alumnos.php?ok=eliminado');
    exit;
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();
    if ($status < 300 || $status > 399) {
        http_response_code($status >= 400 && $status <= 599 ? $status : 500);
    }

    $_SESSION['admin_alumnos_error'] = $exception->getMessage();
    header('Location: /users/admin/alumnos.php?error=eliminar');
    exit;
}
