<?php
declare(strict_types=1);

require_once __DIR__ . '/../../../backend/materias_pendientes/_bootstrap.php';

materiasPendientesEnsureJsonResponse();

$alumnoId = isset($_GET['alumno_id']) ? (int) $_GET['alumno_id'] : 0;
if ($alumnoId <= 0) {
    materiasPendientesJsonResponse([
        'error' => 'El alumno es obligatorio',
    ], 400);
}

try {
    $response = materiasPendientesApiRequest('GET', '/materias-pendientes/alumnos/' . $alumnoId);
    $items = materiasPendientesResponseItems($response);
    materiasPendientesJsonResponse($items, $response['status']);
} catch (Throwable $exception) {
    materiasPendientesHandleThrowable($exception);
}
