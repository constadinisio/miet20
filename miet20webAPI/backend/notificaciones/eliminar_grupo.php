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

$grupoId = isset($_POST['grupo_id']) ? (int) $_POST['grupo_id'] : 0;

if ($grupoId <= 0) {
    http_response_code(422);
    echo '⚠️ El identificador del grupo es inválido.';
    exit;
}

try {
    miEt20NotificacionesEliminarGrupoRaw($grupoId);
    header('Location: ' . ($_SERVER['HTTP_REFERER'] ?? '/users/admin/notificaciones.php'));
    exit;
} catch (RuntimeException $exception) {
    $statusCode = $exception->getCode() >= 400 ? (int) $exception->getCode() : 500;
    http_response_code($statusCode);
    echo '❌ Error al eliminar el grupo: ' . htmlspecialchars($exception->getMessage(), ENT_QUOTES, 'UTF-8');
}
