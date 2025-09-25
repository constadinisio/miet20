<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}
require_once __DIR__ . '/api_client.php';

$csrfToken = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrfToken)) {
    header('Location: /users/admin/cursos.php?error=csrf');
    exit;
}

$cursoId = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : 0;
$estadoActual = isset($_POST['estado']) ? trim((string) $_POST['estado']) : '';

if ($cursoId <= 0 || !in_array($estadoActual, ['activo', 'inactivo'], true)) {
    header('Location: /users/admin/cursos.php?error=estado_invalido');
    exit;
}

$nuevoEstado = $estadoActual === 'activo' ? 'inactivo' : 'activo';

try {
    admin_call_api('PUT', '/cursos/' . $cursoId, [
        'estado' => $nuevoEstado,
    ]);
    header('Location: /users/admin/cursos.php?ok=estado_cambiado');
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_cursos_error'] = $exception->getMessage();
    header('Location: /users/admin/cursos.php?error=api');
    exit;
}