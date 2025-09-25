<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

$rawCargoId = $_POST['cargo_id'] ?? null;
$rawCursoId = $_POST['curso_id'] ?? null;
$rawMateriaId = $_POST['materia_id'] ?? null;

$cargoId = filter_var($rawCargoId, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
$cursoId = filter_var($rawCursoId, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
$materiaId = filter_var($rawMateriaId, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;

if ($cargoId === null || $cursoId === null || $materiaId === null) {
    header("Location: /users/admin/materias.php?error=faltan_campos&cargo_id=" . urlencode((string) $rawCargoId));
    exit;
}

try {
    $cargo = admin_call_api('GET', '/cargos/' . $cargoId);
} catch (RuntimeException $exception) {
    $_SESSION['admin_materias_error'] = 'No se pudo obtener la información del cargo: ' . $exception->getMessage();
    header("Location: /users/admin/materias.php?error=api&cargo_id=" . urlencode((string) $cargoId));
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

$materiaEncontrada = false;
foreach ($relacionesActuales as &$relacionMateria) {
    if ((int) $relacionMateria['id'] === $materiaId) {
        $materiaEncontrada = true;
        $cursoExistente = false;

        foreach ($relacionMateria['cursos'] as $curso) {
            if ((int) $curso['id'] === $cursoId) {
                $cursoExistente = true;
                break;
            }
        }

        if (!$cursoExistente) {
            $relacionMateria['cursos'][] = [
                'id' => $cursoId,
                'horarios' => [],
            ];
        }
        break;
    }
}
unset($relacionMateria);

if (!$materiaEncontrada) {
    $relacionesActuales[] = [
        'id' => $materiaId,
        'cursos' => [
            [
                'id' => $cursoId,
                'horarios' => [],
            ],
        ],
    ];
}

try {
    admin_call_api('POST', '/cargos/' . $cargoId . '/relaciones', ['materias' => $relacionesActuales]);
    header("Location: /users/admin/materias.php?ok=asignada&cargo_id=" . urlencode((string) $cargoId));
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_materias_error'] = 'No se pudo guardar la asignación: ' . $exception->getMessage();
    header("Location: /users/admin/materias.php?error=api&cargo_id=" . urlencode((string) $cargoId));
    exit;
}
