<?php
declare(strict_types=1);

require_once __DIR__ . '/_bootstrap.php';

cargosEnsureAdminJson();

$cargoId = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : null;
if (!$cargoId) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Falta cargo_id'
    ], 400);
}

$payloads = [];

if (isset($_POST['dias'], $_POST['hora_inicio'], $_POST['hora_fin'])) {
    $dias = $_POST['dias'];
    if (is_string($dias)) {
        $dias = json_decode($dias, true);
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

    if (!empty($diasNormalizados)) {
        $payloads[] = [
            'dias' => array_values(array_unique($diasNormalizados)),
            'hora_inicio' => $_POST['hora_inicio'],
            'hora_fin' => $_POST['hora_fin'],
            'tipo' => $_POST['tipo'] ?? 'clase'
        ];
    }
} else {
    $horariosRaw = $_POST['horarios'] ?? '[]';
    if (is_string($horariosRaw)) {
        $horarios = json_decode($horariosRaw, true);
    } else {
        $horarios = $horariosRaw;
    }

    if (is_array($horarios)) {
        foreach ($horarios as $horario) {
            $dias = [];
            if (!empty($horario['dias']) && is_array($horario['dias'])) {
                foreach ($horario['dias'] as $dia) {
                    $diaStr = is_string($dia) ? trim($dia) : (is_scalar($dia) ? (string) $dia : '');
                    if ($diaStr !== '') {
                        $dias[] = $diaStr;
                    }
                }
            }

            $horaInicio = $horario['hora_inicio'] ?? $horario['horaInicio'] ?? null;
            $horaFin = $horario['hora_fin'] ?? $horario['horaFin'] ?? null;
            $tipo = $horario['tipo'] ?? 'clase';

            if ($dias && $horaInicio && $horaFin) {
                $payloads[] = [
                    'dias' => array_values(array_unique($dias)),
                    'hora_inicio' => $horaInicio,
                    'hora_fin' => $horaFin,
                    'tipo' => $tipo
                ];
            }
        }
    }
}

if (!$payloads) {
    cargosJsonResponse([
        'ok' => false,
        'error' => 'Datos incompletos'
    ], 400);
}

try {
    $response = cargosApiRequest('PUT', '/cargos/' . $cargoId . '/horarios', [
        'horarios' => $payloads
    ]);

    $horarios = is_array($response) ? ($response['horarios'] ?? $response) : [];

    cargosJsonResponse([
        'ok' => true,
        'horarios' => $horarios
    ]);
} catch (Throwable $exception) {
    cargosHandleThrowable($exception, ['ok' => false]);
}
