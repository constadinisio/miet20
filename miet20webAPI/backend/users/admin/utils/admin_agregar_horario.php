<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

$cargo_id = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : 0;
$curso_id = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : 0;
$materia_id = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : 0;
$dia = isset($_POST['dia']) ? trim((string) $_POST['dia']) : '';
$hora_inicio = isset($_POST['hora_inicio']) ? trim((string) $_POST['hora_inicio']) : '';
$hora_fin = isset($_POST['hora_fin']) ? trim((string) $_POST['hora_fin']) : '';

if ($cargo_id <= 0 || $curso_id <= 0 || $materia_id <= 0 || $dia === '' || $hora_inicio === '' || $hora_fin === '') {
    header('Location: /users/admin/horarios.php?error=faltan_campos'
        . '&cargo_id=' . urlencode((string) $cargo_id)
        . '&curso_id=' . urlencode((string) $curso_id)
        . '&materia_id=' . urlencode((string) $materia_id));
    exit;
}

$tipo = 'clase';
try {
    $subject = admin_call_api('GET', '/materias/' . $materia_id);
    if (is_array($subject) && (int) ($subject['es_contraturno'] ?? 0) === 1) {
        $tipo = 'extraclase';
    }
} catch (RuntimeException $exception) {
    $_SESSION['admin_horarios_warning'] = $exception->getMessage();
}

try {
    admin_call_api('POST', '/cargos/' . $cargo_id . '/horarios', [
        'dias' => [$dia],
        'hora_inicio' => $hora_inicio,
        'hora_fin' => $hora_fin,
        'tipo' => $tipo,
    ]);
    header('Location: /users/admin/horarios.php?ok=horario_agregado'
        . '&cargo_id=' . urlencode((string) $cargo_id)
        . '&curso_id=' . urlencode((string) $curso_id)
        . '&materia_id=' . urlencode((string) $materia_id));
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_horarios_error'] = $exception->getMessage();
    $status = (int) $exception->getCode();
    $errorCode = $status === 404 ? 'no_encontrado' : 'api';

    header('Location: /users/admin/horarios.php?error=' . $errorCode
        . '&cargo_id=' . urlencode((string) $cargo_id)
        . '&curso_id=' . urlencode((string) $curso_id)
        . '&materia_id=' . urlencode((string) $materia_id));
    exit;
}
