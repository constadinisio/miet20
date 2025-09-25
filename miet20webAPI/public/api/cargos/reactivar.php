<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$cargoId = isset($_POST['id']) ? (int) $_POST['id'] : null;
if (!$cargoId) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'ID de cargo no especificado'
    ], 400);
}

try {
    $cargo = cargosApiRequest('POST', '/cargos/' . $cargoId . '/reactivar');
    cargosJsonResponse([
        'ok' => true,
        'data' => $cargo
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
