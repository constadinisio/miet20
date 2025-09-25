<?php
session_start();

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'error' => 'Acceso denegado'], JSON_UNESCAPED_UNICODE);
    exit;
}

require_once __DIR__ . '/api_client.php';

$cargoId = isset($_POST['cargo_id']) ? (int) $_POST['cargo_id'] : null;
if (!$cargoId) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Falta cargo_id'], JSON_UNESCAPED_UNICODE);
    exit;
}

$payloads = [];

if (isset($_POST['dias'], $_POST['hora_inicio'], $_POST['hora_fin'])) {
    $dias = json_decode($_POST['dias'] ?? '[]', true);
    if (!is_array($dias)) {
        $dias = [];
    }

    $diasNormalizados = array_values(array_filter(array_map('strval', $dias)));
    if ($diasNormalizados) {
        $payloads[] = [
            'dias' => $diasNormalizados,
            'hora_inicio' => $_POST['hora_inicio'],
            'hora_fin' => $_POST['hora_fin'],
            'tipo' => $_POST['tipo'] ?? 'clase',
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
                $dias = array_values(array_filter(array_map('strval', $horario['dias'])));
            }

            $horaInicio = $horario['hora_inicio'] ?? $horario['horaInicio'] ?? null;
            $horaFin = $horario['hora_fin'] ?? $horario['horaFin'] ?? null;
            $tipo = $horario['tipo'] ?? 'clase';

            if (!$dias || !$horaInicio || !$horaFin) {
                continue;
            }

            $payloads[] = [
                'dias' => $dias,
                'hora_inicio' => $horaInicio,
                'hora_fin' => $horaFin,
                'tipo' => $tipo,
            ];
        }
    }
}

if (!$payloads) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Datos incompletos'], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $response = cargos_call_api('PUT', '/cargos/' . $cargoId . '/horarios', [
        'horarios' => $payloads,
    ]);

    $horarios = is_array($response) ? ($response['horarios'] ?? $response) : [];

    echo json_encode([
        'ok' => true,
        'horarios' => $horarios,
    ], JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'ok' => false,
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
