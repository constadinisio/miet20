<?php
session_start();

header('Content-Type: application/json');

if (!isset($_SESSION['usuario']) || (int) ($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    http_response_code(401);
    echo json_encode(['error' => 'Sesión inválida']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Método no permitido']);
    exit;
}

require_once __DIR__ . '/../../includes/api_client.php';

$csrf = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || $csrf !== $_SESSION['csrf']) {
    http_response_code(400);
    echo json_encode(['error' => 'CSRF inválido']);
    exit;
}

$alumnoId = isset($_POST['alumno_id']) ? (int) $_POST['alumno_id'] : 0;
$materiaId = isset($_POST['materia_id']) ? (int) $_POST['materia_id'] : 0;
$notas = isset($_POST['nota']) && is_array($_POST['nota']) ? $_POST['nota'] : [];

if ($alumnoId <= 0 || $materiaId <= 0 || empty($notas)) {
    http_response_code(400);
    echo json_encode(['error' => 'Datos incompletos']);
    exit;
}

$notasProcesadas = 0;

foreach ($notas as $trabajoId => $valor) {
    $trabajoIdInt = (int) $trabajoId;
    if ($trabajoIdInt <= 0) {
        continue;
    }

    if (!is_numeric($valor)) {
        continue;
    }

    $nota = (float) $valor;
    if ($nota < 1 || $nota > 10) {
        http_response_code(400);
        echo json_encode(['error' => 'La nota debe estar entre 1 y 10']);
        exit;
    }

    try {
        miEt20ApiAuthenticatedRequest(
            'POST',
            '/assignments/' . $trabajoIdInt . '/alumnos/' . $alumnoId . '/notas',
            [
                'materia_id' => $materiaId,
                'nota' => $nota,
            ]
        );
        $notasProcesadas++;
    } catch (RuntimeException $exception) {
        $status = (int) $exception->getCode();
        if ($status < 400 || $status >= 600) {
            $status = 500;
        }

        http_response_code($status);
        echo json_encode(['error' => $exception->getMessage()]);
        exit;
    }
}

if ($notasProcesadas === 0) {
    http_response_code(400);
    echo json_encode(['error' => 'No se enviaron notas válidas']);
    exit;
}

echo json_encode(['ok' => true]);
exit;
