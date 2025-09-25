<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

materiasPendientesEnsureJsonResponse();

$id = isset($_POST['id']) ? (int) $_POST['id'] : 0;
$nota = isset($_POST['nota']) ? (float) $_POST['nota'] : null;
$profesorId = isset($_POST['profesor_id']) ? (int) $_POST['profesor_id'] : 0;

if ($id <= 0 || $nota === null || $profesorId <= 0) {
    materiasPendientesJsonResponse([
        'error' => 'Datos incompletos',
    ], 400);
}

try {
    $response = materiasPendientesApiRequest('POST', '/materias-pendientes/' . $id . '/aprobar', [
        'nota' => $nota,
        'profesor_id' => $profesorId,
    ]);

    $data = $response['data'];
    if (!is_array($data)) {
        $data = [];
    }

    materiasPendientesJsonResponse($data, $response['status']);
} catch (Throwable $exception) {
    materiasPendientesHandleThrowable($exception);
}
