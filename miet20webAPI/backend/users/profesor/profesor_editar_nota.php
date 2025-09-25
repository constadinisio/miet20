<?php
session_start();
if (!isset($_SESSION['usuario']) || (int) ($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    header('Location: /login.php?error=rol');
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /users/profesor/calificaciones.php?error=metodo');
    exit;
}

require_once __DIR__ . '/../../includes/api_client.php';

$csrf = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || $csrf !== $_SESSION['csrf']) {
    header('Location: /users/profesor/calificaciones.php?error=csrf');
    exit;
}

$cursoId = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : 0;
$materiaId = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : 0;
$periodo = isset($_POST['periodo']) ? trim((string) $_POST['periodo']) : '';
$cargoId = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : 0;

$redirectParams = [];
if ($cursoId > 0) {
    $redirectParams['curso_id'] = $cursoId;
}
if ($materiaId > 0) {
    $redirectParams['materia_id'] = $materiaId;
}
if ($cargoId > 0) {
    $redirectParams['cargo_id'] = $cargoId;
}
if ($periodo !== '') {
    $redirectParams['periodo'] = $periodo;
}

$buildRedirectUrl = static function (array $extra = []) use ($redirectParams) {
    $base = '/users/profesor/calificaciones.php';
    $params = array_merge($redirectParams, $extra);
    $query = http_build_query($params);

    return $query ? $base . '?' . $query : $base;
};

if (!isset($_POST['notas']) || !is_array($_POST['notas'])) {
    header('Location: ' . $buildRedirectUrl(['error' => 'sin_datos']));
    exit;
}

foreach ($_POST['notas'] as $notaId => $valor) {
    $notaIdInt = (int) $notaId;
    if ($notaIdInt <= 0) {
        continue;
    }

    if ($valor === null || $valor === '') {
        continue;
    }

    if (!is_numeric($valor)) {
        header('Location: ' . $buildRedirectUrl(['error' => 'nota_invalida']));
        exit;
    }

    $nota = (float) $valor;
    if ($nota < 1 || $nota > 10) {
        continue;
    }

    try {
        miEt20ApiAuthenticatedRequest('PUT', '/notas/' . $notaIdInt, [
            'nota' => $nota,
        ]);
    } catch (RuntimeException $exception) {
        header('Location: ' . $buildRedirectUrl(['error' => $exception->getMessage()]));
        exit;
    }
}

header('Location: ' . $buildRedirectUrl(['ok' => 'notas_editadas']));
exit;