<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

materiasPendientesEnsureJsonResponse();

$q = isset($_GET['q']) ? trim((string) $_GET['q']) : '';

try {
    $response = materiasPendientesApiRequest('GET', '/alumnos', null, [
        'query' => [
            'search' => $q,
            'limit' => 10,
        ],
    ]);

    $items = materiasPendientesResponseItems($response);

    materiasPendientesJsonResponse($items, $response['status']);
} catch (Throwable $exception) {
    materiasPendientesHandleThrowable($exception);
}
