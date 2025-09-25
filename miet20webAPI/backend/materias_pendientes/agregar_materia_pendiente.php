<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

materiasPendientesEnsureJsonResponse();

$alumnoId = isset($_POST['alumno_id']) ? (int) $_POST['alumno_id'] : 0;
$materiaId = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : 0;

if ($alumnoId <= 0 || $materiaId <= 0) {
    materiasPendientesJsonResponse([
        'error' => 'Datos incompletos',
    ], 400);
}

try {
    $response = materiasPendientesApiRequest('POST', '/materias-pendientes', [
        'alumno_id' => $alumnoId,
        'materia_id' => $materiaId,
    ]);

    $data = $response['data'];
    if (!is_array($data)) {
        $data = [];
    }

    materiasPendientesJsonResponse($data, $response['status']);
} catch (Throwable $exception) {
    materiasPendientesHandleThrowable($exception);
}
