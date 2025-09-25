<?php
require_once __DIR__ . '/notificaciones_utils.php';

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo '⛔ Usuario no autorizado.';
    exit;
}

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    http_response_code(405);
    echo '⚠️ Método no permitido.';
    exit;
}

if (!isset($_POST['csrf']) || !isset($_SESSION['csrf']) || $_POST['csrf'] !== $_SESSION['csrf']) {
    http_response_code(400);
    echo '⚠️ Token CSRF inválido.';
    exit;
}

$nombre = trim($_POST['nombre_grupo'] ?? '');
$descripcion = trim($_POST['descripcion_grupo'] ?? '');
$miembros = $_POST['miembros'] ?? [];

if ($nombre === '') {
    http_response_code(422);
    echo '⚠️ El nombre del grupo es obligatorio.';
    exit;
}

if (!is_array($miembros)) {
    $miembros = [];
}

try {
    miEt20NotificacionesCrearGrupoRaw($nombre, $descripcion, $miembros);
    header('Location: ' . ($_SERVER['HTTP_REFERER'] ?? '/users/admin/notificaciones.php'));
    exit;
} catch (RuntimeException $exception) {
    $statusCode = $exception->getCode() >= 400 ? (int) $exception->getCode() : 500;
    http_response_code($statusCode);
    echo '❌ Error al crear el grupo: ' . htmlspecialchars($exception->getMessage(), ENT_QUOTES, 'UTF-8');
}
