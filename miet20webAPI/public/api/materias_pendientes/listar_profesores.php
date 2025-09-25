<?php
declare(strict_types=1);

require_once __DIR__ . '/../../../backend/materias_pendientes/_bootstrap.php';

materiasPendientesEnsureJsonResponse();

$pendienteId = isset($_GET['pendiente_id']) ? (int) $_GET['pendiente_id'] : 0;
if ($pendienteId <= 0) {
    materiasPendientesJsonResponse([
        'error' => 'La materia pendiente es obligatoria',
    ], 400);
}

try {
    $response = materiasPendientesApiRequest('GET', '/materias-pendientes/' . $pendienteId . '/docentes');
    $items = materiasPendientesResponseItems($response);
    materiasPendientesJsonResponse($items, $response['status']);
} catch (Throwable $exception) {
    materiasPendientesHandleThrowable($exception);
}
