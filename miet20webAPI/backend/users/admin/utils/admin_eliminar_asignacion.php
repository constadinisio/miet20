<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header('Location: /login.php?error=rol');
    exit;
}

require_once __DIR__ . '/api_client.php';

$csrfToken = $_POST['csrf'] ?? '';
if ($csrfToken !== '' && (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrfToken))) {
    header('Location: /users/admin/materias.php?error=csrf');
    exit;
}

$cargoId = filter_var($_POST['cargo_id'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
$cursoId = filter_var($_POST['curso_id'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
$materiaId = filter_var($_POST['materia_id'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;

if ($cargoId === null || $cursoId === null || $materiaId === null) {
    header('Location: /users/admin/materias.php?error=faltan_campos');
    exit;
}

try {
    $cargo = admin_call_api('GET', '/cargos/' . $cargoId);
} catch (RuntimeException $exception) {
    $_SESSION['admin_materias_error'] = 'No se pudo obtener el cargo: ' . $exception->getMessage();
    header('Location: /users/admin/materias.php?error=api&cargo_id=' . urlencode((string) $cargoId));
    exit;
}

$relacionesActuales = [];
if (is_array($cargo) && isset($cargo['relaciones']) && is_array($cargo['relaciones'])) {
    foreach ($cargo['relaciones'] as $relacion) {
        $materiaRelacionId = isset($relacion['materia_id']) ? (int) $relacion['materia_id'] : 0;
        if ($materiaRelacionId <= 0) {
            continue;
        }

        $cursosRelacionados = [];
        if (isset($relacion['cursos']) && is_array($relacion['cursos'])) {
            foreach ($relacion['cursos'] as $cursoRelacionado) {
                $cursoRelacionId = isset($cursoRelacionado['curso_id']) ? (int) $cursoRelacionado['curso_id'] : 0;
                if ($cursoRelacionId <= 0) {
                    continue;
                }

                $horarios = [];
                if (isset($cursoRelacionado['horarios']) && is_array($cursoRelacionado['horarios'])) {
                    foreach ($cursoRelacionado['horarios'] as $horarioId) {
                        $horarioId = (int) $horarioId;
                        if ($horarioId > 0) {
                            $horarios[] = $horarioId;
                        }
                    }
                }

                $cursosRelacionados[] = [
                    'id' => $cursoRelacionId,
                    'horarios' => $horarios,
                ];
            }
        }

        $relacionesActuales[] = [
            'id' => $materiaRelacionId,
            'cursos' => $cursosRelacionados,
        ];
    }
}

$seModifico = false;
foreach ($relacionesActuales as $indice => &$relacionMateria) {
    if ((int) $relacionMateria['id'] !== $materiaId) {
        continue;
    }

    $cursosFiltrados = [];
    $cursoEncontrado = false;
    foreach ($relacionMateria['cursos'] as $cursoRelacionado) {
        if ((int) $cursoRelacionado['id'] === $cursoId) {
            $cursoEncontrado = true;
            continue;
        }

        $cursosFiltrados[] = $cursoRelacionado;
    }

    if (!$cursoEncontrado) {
        continue;
    }

    if (empty($cursosFiltrados)) {
        unset($relacionesActuales[$indice]);
    } else {
        $relacionMateria['cursos'] = array_values($cursosFiltrados);
    }

    $seModifico = true;
    break;
}
unset($relacionMateria);

if (!$seModifico) {
    header('Location: /users/admin/materias.php?ok=eliminada&cargo_id=' . urlencode((string) $cargoId));
    exit;
}

$relacionesActuales = array_values($relacionesActuales);

try {
    admin_call_api('POST', '/cargos/' . $cargoId . '/relaciones', ['materias' => $relacionesActuales]);
    header('Location: /users/admin/materias.php?ok=eliminada&cargo_id=' . urlencode((string) $cargoId));
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_materias_error'] = 'No se pudo actualizar las asignaciones: ' . $exception->getMessage();
    header('Location: /users/admin/materias.php?error=api&cargo_id=' . urlencode((string) $cargoId));
    exit;
}
