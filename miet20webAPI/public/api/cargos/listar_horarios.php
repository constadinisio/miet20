<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$cargoId = isset($_GET['cargo_id']) ? (int) $_GET['cargo_id'] : null;
if (!$cargoId) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Falta cargo_id'
    ], 400);
}

try {
    $horarios = cargosApiRequest('GET', '/cargos/' . $cargoId . '/horarios');
    cargosJsonResponse([
        'ok' => true,
        'horarios' => $horarios
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
