<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header('Location: /login.php?error=rol');
    exit;
}

require_once __DIR__ . '/api_client.php';

$csrfToken = $_POST['csrf'] ?? '';
if ($csrfToken !== '' && (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrfToken))) {
    header('Location: /users/admin/usuarios.php?error=csrf');
    exit;
}

$userId = filter_var($_POST['usuario_id'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
if ($userId === null) {
    header('Location: /users/admin/usuarios.php?error=datos_invalidos');
    exit;
}

try {
    admin_call_api('POST', '/usuarios/' . $userId . '/rechazar', []);
    header('Location: /users/admin/usuarios.php?ok=rechazado');
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_usuarios_error'] = $exception->getMessage();
    $status = (int) $exception->getCode();

    if ($status === 404) {
        header('Location: /users/admin/usuarios.php?error=usuario_no_encontrado');
        exit;
    }

    header('Location: /users/admin/usuarios.php?error=api');
    exit;
}
