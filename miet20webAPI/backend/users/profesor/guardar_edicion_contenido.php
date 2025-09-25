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

$fecha = $_POST['fecha_clase'] ?? $_POST['fecha'] ?? null;
$caracter = isset($_POST['caracter_clase']) ? trim((string) $_POST['caracter_clase']) : null;
$tema = isset($_POST['tema']) ? trim((string) $_POST['tema']) : null;
$actividades = $_POST['actividades_desarrolladas'] ?? $_POST['actividades'] ?? null;
$observaciones = isset($_POST['observaciones']) ? trim((string) $_POST['observaciones']) : null;

$payload = [];
if ($fecha) {
    $payload['fecha_clase'] = $fecha;
}
if ($caracter !== null && $caracter !== '') {
    $payload['caracter_clase'] = $caracter;
}
if ($tema !== null) {
    $payload['tema'] = $tema;
}
if ($actividades !== null) {
    $payload['actividades_desarrolladas'] = trim((string) $actividades);
}
if ($observaciones !== null) {
    $payload['observaciones'] = $observaciones;
}

try {
    miEt20ApiAuthenticatedRequest('PUT', '/lesson-book/' . $id, $payload);
    header('Location: /users/profesor/libro_temas.php?editado=1');
    exit;
} catch (RuntimeException $exception) {
    $mensaje = $exception->getMessage();
    header('Location: /users/profesor/libro_temas.php?error=' . urlencode($mensaje));
    exit;
}
