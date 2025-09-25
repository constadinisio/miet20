<?php
session_start();
require_once __DIR__ . '/../../../backend/includes/api_client.php';

if ((int)($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    header('Location: /login.php?error=rol');
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /users/profesor/libro_temas.php?error=metodo');
    exit;
}

$csrf = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || $csrf !== $_SESSION['csrf']) {
    header('Location: /users/profesor/libro_temas.php?error=csrf');
    exit;
}

$id = isset($_POST['id']) ? (int) $_POST['id'] : 0;
if ($id <= 0) {
    header('Location: /users/profesor/libro_temas.php?error=tema');
    exit;
}

try {
    miEt20ApiAuthenticatedRequest('DELETE', '/lesson-book/' . $id);
    header('Location: /users/profesor/libro_temas.php?eliminado=1');
    exit;
} catch (RuntimeException $exception) {
    $mensaje = $exception->getMessage();
    header('Location: /users/profesor/libro_temas.php?error=' . urlencode($mensaje));
    exit;
}
