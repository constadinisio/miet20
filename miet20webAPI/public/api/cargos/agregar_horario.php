<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$cargoId = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : null;
$rawDias = $_POST['dias'] ?? '[]';
$horaInicio = $_POST['hora_inicio'] ?? null;
$horaFin = $_POST['hora_fin'] ?? null;
$tipo = $_POST['tipo'] ?? 'clase';

if (is_string($rawDias)) {
    $dias = json_decode($rawDias, true);
} else {
    $dias = $rawDias;
}

$diasNormalizados = [];
if (is_array($dias)) {
    foreach ($dias as $dia) {
        $diaStr = is_string($dia) ? trim($dia) : (is_scalar($dia) ? (string) $dia : '');
        if ($diaStr !== '') {
            $diasNormalizados[] = $diaStr;
        }
    }
}

if (!$cargoId || empty($diasNormalizados) || !$horaInicio || !$horaFin) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Datos incompletos'
    ], 400);
}

try {
    $payload = [
        'dias' => array_values(array_unique($diasNormalizados)),
        'hora_inicio' => $horaInicio,
        'hora_fin' => $horaFin,
        'tipo' => $tipo,
    ];

    $response = cargosApiRequest('POST', '/cargos/' . $cargoId . '/horarios', $payload);
    $horarios = is_array($response) ? ($response['horarios'] ?? $response) : [];

    cargosJsonResponse([
        'ok' => true,
        'horarios' => $horarios
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
