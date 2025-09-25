<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}
$usuario = $_SESSION['usuario'];

require_once __DIR__ . '/api_client.php';

$asignaciones = [];
$horarios = [];
$cargo_id = isset($_GET['cargo_id']) ? (int) $_GET['cargo_id'] : 0;
$curso_id = isset($_GET['curso_id']) ? (int) $_GET['curso_id'] : 0;
$materia_id = isset($_GET['materia_id']) ? (int) $_GET['materia_id'] : 0;

$cargoResumen = [];
try {
    $cargos = admin_call_api('GET', '/cargos');
    if (is_array($cargos)) {
        foreach ($cargos as $cargo) {
            if (!is_array($cargo) || !isset($cargo['id'])) {
                continue;
            }
            $cargoResumen[(int) $cargo['id']] = $cargo;
        }
    }
} catch (RuntimeException $exception) {
    $_SESSION['admin_horarios_error'] = $exception->getMessage();
}

$cursoMapa = [];
try {
    $cursos = admin_call_api('GET', '/cursos');
    if (is_array($cursos)) {
        foreach ($cursos as $curso) {
            if (!is_array($curso) || !isset($curso['id'])) {
                continue;
            }
            $cursoMapa[(int) $curso['id']] = $curso;
        }
    }
} catch (RuntimeException $exception) {
    $_SESSION['admin_horarios_error'] = $exception->getMessage();
}

$materiaMapa = [];
try {
    $materias = admin_call_api('GET', '/materias');
    if (is_array($materias)) {
        foreach ($materias as $materia) {
            if (!is_array($materia) || !isset($materia['id'])) {
                continue;
            }
            $materiaMapa[(int) $materia['id']] = $materia;
        }
    }
} catch (RuntimeException $exception) {
    $_SESSION['admin_horarios_error'] = $exception->getMessage();
}

$cargoDetalle = null;
if ($cargo_id > 0) {
    try {
        $cargoDetalle = admin_call_api('GET', '/cargos/' . $cargo_id);
    } catch (RuntimeException $exception) {
        $_SESSION['admin_horarios_error'] = $exception->getMessage();
    }
}

if (is_array($cargoDetalle)) {
    $docente_nombre = $cargoResumen[$cargo_id]['docente_nombre'] ?? '';
    $docente_apellido = $cargoResumen[$cargo_id]['docente_apellido'] ?? '';

    $relaciones = isset($cargoDetalle['relaciones']) && is_array($cargoDetalle['relaciones'])
        ? $cargoDetalle['relaciones']
        : [];

    foreach ($relaciones as $relacion) {
        $relMateriaId = isset($relacion['materia_id']) ? (int) $relacion['materia_id'] : 0;
        $materiaNombre = $materiaMapa[$relMateriaId]['nombre'] ?? $materiaMapa[$relMateriaId]['materia_nombre'] ?? '';

        $cursosRelacion = isset($relacion['cursos']) && is_array($relacion['cursos']) ? $relacion['cursos'] : [];
        foreach ($cursosRelacion as $cursoRelacion) {
            $relCursoId = isset($cursoRelacion['curso_id']) ? (int) $cursoRelacion['curso_id'] : 0;
            $cursoInfo = $cursoMapa[$relCursoId] ?? [];

            if ($relCursoId <= 0) {
                continue;
            }

            $asignaciones[] = [
                'cargo_id' => $cargo_id,
                'prof_nombre' => $docente_nombre,
                'prof_apellido' => $docente_apellido,
                'curso_id' => $relCursoId,
                'anio' => $cursoInfo['anio'] ?? null,
                'division' => $cursoInfo['division'] ?? null,
                'materia_id' => $relMateriaId,
                'materia' => $materiaNombre,
            ];
        }
    }

    if ($curso_id > 0 && $materia_id > 0) {
        $horariosCargo = isset($cargoDetalle['horarios']) && is_array($cargoDetalle['horarios'])
            ? $cargoDetalle['horarios']
            : [];

        $horarioMapa = [];
        foreach ($horariosCargo as $horario) {
            if (!is_array($horario) || !isset($horario['id'])) {
                continue;
            }

            $horarioMapa[(int) $horario['id']] = [
                'id' => (int) $horario['id'],
                'dia_semana' => $horario['dia_semana'] ?? '',
                'hora_inicio' => isset($horario['hora_inicio']) ? substr((string) $horario['hora_inicio'], 0, 8) : null,
                'hora_fin' => isset($horario['hora_fin']) ? substr((string) $horario['hora_fin'], 0, 8) : null,
                'tipo' => $horario['tipo'] ?? ($horario['es_contraturno'] ?? 'clase'),
            ];
        }

        foreach ($relaciones as $relacion) {
            $relMateriaId = isset($relacion['materia_id']) ? (int) $relacion['materia_id'] : 0;
            if ($relMateriaId !== $materia_id) {
                continue;
            }

            $cursosRelacion = isset($relacion['cursos']) && is_array($relacion['cursos']) ? $relacion['cursos'] : [];
            foreach ($cursosRelacion as $cursoRelacion) {
                $relCursoId = isset($cursoRelacion['curso_id']) ? (int) $cursoRelacion['curso_id'] : 0;
                if ($relCursoId !== $curso_id) {
                    continue;
                }

                $horarioIds = isset($cursoRelacion['horarios']) && is_array($cursoRelacion['horarios'])
                    ? $cursoRelacion['horarios']
                    : [];

                foreach ($horarioIds as $horarioId) {
                    $horarioId = (int) $horarioId;
                    if (isset($horarioMapa[$horarioId])) {
                        $horarios[] = $horarioMapa[$horarioId];
                    }
                }
            }
        }

        usort($horarios, static function (array $a, array $b): int {
            $ordenDias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
            $posA = array_search($a['dia_semana'] ?? '', $ordenDias, true);
            $posB = array_search($b['dia_semana'] ?? '', $ordenDias, true);

            if ($posA === false) {
                $posA = PHP_INT_MAX;
            }
            if ($posB === false) {
                $posB = PHP_INT_MAX;
            }

            if ($posA === $posB) {
                return strcmp($a['hora_inicio'] ?? '', $b['hora_inicio'] ?? '');
            }

            return $posA <=> $posB;
        });
    }
}
?>
