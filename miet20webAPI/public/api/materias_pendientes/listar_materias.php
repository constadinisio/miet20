<?php
declare(strict_types=1);

require_once __DIR__ . '/../../../backend/materias_pendientes/_bootstrap.php';

materiasPendientesEnsureJsonResponse();

$q = isset($_GET['q']) ? trim((string) $_GET['q']) : '';

try {
    $response = materiasPendientesApiRequest('GET', '/materias-pendientes/materias', null, [
        'query' => [
            'q' => $q,
            'limit' => isset($_GET['limit']) ? (int) $_GET['limit'] : 15,
        ],
    ]);

    $items = materiasPendientesResponseItems($response);

    materiasPendientesJsonResponse($items, $response['status']);
} catch (Throwable $exception) {
    materiasPendientesHandleThrowable($exception);
}
