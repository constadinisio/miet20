<?php
session_start();

if (
    !isset($_SESSION['usuario']) ||
    !is_array($_SESSION['usuario']) ||
    ((int)$_SESSION['usuario']['rol'] !== 3)
) {
    header("Location: /login.php?error=rol");
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /users/profesor/calificaciones.php?error=metodo');
    exit;
}

require_once __DIR__ . '/../../includes/api_client.php';

if (!isset($_SESSION['csrf']) || ($_POST['csrf'] ?? '') !== $_SESSION['csrf']) {
    header('Location: /users/profesor/calificaciones.php?error=csrf');
    exit;
}

$cursoId = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : 0;
$materiaId = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : 0;
$cargoId = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : 0;
$periodo = isset($_POST['periodo']) ? trim((string) $_POST['periodo']) : '';
$alumnos = isset($_POST['alumno_id']) && is_array($_POST['alumno_id']) ? $_POST['alumno_id'] : [];
$notas = isset($_POST['nota']) && is_array($_POST['nota']) ? $_POST['nota'] : [];

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

if ($cursoId <= 0 || $materiaId <= 0 || $cargoId <= 0 || $periodo === '' || empty($alumnos) || empty($notas)) {
    header('Location: ' . $buildRedirectUrl(['error' => 'datos']));
    exit;
}

$totalAlumnos = count($alumnos);
$notasRegistradas = 0;

for ($i = 0; $i < $totalAlumnos; $i++) {
    $alumnoId = isset($alumnos[$i]) ? (int) $alumnos[$i] : 0;
    $notaValor = $notas[$i] ?? null;

    if ($alumnoId <= 0 || $notaValor === null || $notaValor === '') {
        continue;
    }

    if (!is_numeric($notaValor)) {
        header('Location: ' . $buildRedirectUrl(['error' => 'nota_invalida']));
        exit;
    }

    $nota = (float) $notaValor;
    if ($nota < 1 || $nota > 10) {
        header('Location: ' . $buildRedirectUrl(['error' => 'nota_rango']));
        exit;
    }

    try {
        miEt20ApiAuthenticatedRequest(
            'POST',
            '/notas/cursos/' . $cursoId . '/alumnos/' . $alumnoId,
            [
                'materia_id' => $materiaId,
                'periodo' => $periodo,
                'nota' => $nota,
                'promedio_actividades' => 0,
            ]
        );
        $notasRegistradas++;
    } catch (RuntimeException $exception) {
        header('Location: ' . $buildRedirectUrl(['error' => $exception->getMessage()]));
        exit;
    }
}

if ($notasRegistradas === 0) {
    header('Location: ' . $buildRedirectUrl(['error' => 'sin_notas_validas']));
    exit;
}

header('Location: ' . $buildRedirectUrl(['ok' => 'notas_cargadas']));
exit;