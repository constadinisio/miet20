<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

try {
    $cargos = cargosApiRequest('GET', '/cargos');
    cargosJsonResponse($cargos);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception);
}
