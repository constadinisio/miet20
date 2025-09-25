<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

$horario_id = isset($_POST['id']) ? (int) $_POST['id'] : 0;
$cargo_id = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : 0;
if ($cargo_id <= 0 && isset($_POST['asignacion_id'])) {
    $cargo_id = (int) $_POST['asignacion_id'];
}

$curso_id = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : 0;
$materia_id = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : 0;

if ($horario_id <= 0 || $cargo_id <= 0) {
    header('Location: /users/admin/horarios.php?error=faltan_campos');
    exit;
}

try {
    admin_call_api('DELETE', '/cargos/' . $cargo_id . '/horarios/' . $horario_id);
    header('Location: /users/admin/horarios.php?ok=horario_eliminado'
        . '&cargo_id=' . urlencode((string) $cargo_id)
        . ($curso_id > 0 ? '&curso_id=' . urlencode((string) $curso_id) : '')
        . ($materia_id > 0 ? '&materia_id=' . urlencode((string) $materia_id) : ''));
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_horarios_error'] = $exception->getMessage();
    $status = (int) $exception->getCode();
    $errorCode = $status === 404 ? 'no_encontrado' : 'api';

    header('Location: /users/admin/horarios.php?error=' . $errorCode
        . '&cargo_id=' . urlencode((string) $cargo_id)
        . ($curso_id > 0 ? '&curso_id=' . urlencode((string) $curso_id) : '')
        . ($materia_id > 0 ? '&materia_id=' . urlencode((string) $materia_id) : ''));
    exit;
}
