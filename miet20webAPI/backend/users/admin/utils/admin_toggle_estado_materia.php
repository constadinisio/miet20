<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

$materiaId = $_POST['materia_id'] ?? null;
$estadoActual = $_POST['estado'] ?? '';

$materiaId = filter_var($materiaId, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
$estadoActual = is_string($estadoActual) ? trim($estadoActual) : '';

if ($materiaId === null || !in_array($estadoActual, ['activo', 'inactivo'], true)) {
    header("Location: /users/admin/materias.php?error=estado_invalido");
    exit;
}

try {
    admin_call_api('PATCH', '/materias/' . $materiaId . '/toggle-estado', ['estado' => $estadoActual]);
    header("Location: /users/admin/materias.php?ok=estado_actualizado");
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_materias_error'] = 'No se pudo actualizar el estado: ' . $exception->getMessage();
    header("Location: /users/admin/materias.php?error=api");
    exit;
}
