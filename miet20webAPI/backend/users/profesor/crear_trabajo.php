<?php
session_start();
require_once __DIR__ . '/../../../backend/includes/api_client.php';

if ((int)($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    header('Location: /login.php?error=rol');
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /users/profesor/calificaciones.php?error=metodo');
    exit;
}

$csrf = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || $csrf !== $_SESSION['csrf']) {
    header('Location: /users/profesor/calificaciones.php?error=csrf');
    exit;
}

$cursoId = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : (int) ($_GET['curso_id'] ?? 0);
$materiaId = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : (int) ($_GET['materia_id'] ?? 0);
$nombre = trim($_POST['nombre'] ?? '');
$tipo = $_POST['tipo'] ?? '';
$descripcion = isset($_POST['descripcion']) ? trim((string) $_POST['descripcion']) : null;

if ($cursoId <= 0 || $materiaId <= 0 || $nombre === '' || !in_array($tipo, ['tp', 'actividad'], true)) {
    header('Location: /users/profesor/trabajos.php?error=datos');
    exit;
}

try {
    miEt20ApiAuthenticatedRequest(
        'POST',
        '/assignments/cursos/' . $cursoId,
        array_filter(
            [
                'materia_id' => $materiaId,
                'nombre' => $nombre,
                'tipo' => $tipo,
                'descripcion' => $descripcion !== '' ? $descripcion : null,
            ],
            static fn($value) => $value !== null
        )
    );

    header('Location: /users/profesor/trabajos.php?curso_id=' . $cursoId . '&materia_id=' . $materiaId . '&ok=creado');
    exit;
} catch (RuntimeException $exception) {
    $mensaje = $exception->getMessage();
    header('Location: /users/profesor/trabajos.php?curso_id=' . $cursoId . '&materia_id=' . $materiaId . '&error=' . urlencode($mensaje));
    exit;
}
