<?php
declare(strict_types=1);

session_start();

require_once __DIR__ . '/../../../backend/includes/api_client.php';

header('Content-Type: application/json; charset=UTF-8');

if ((int) ($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    http_response_code(403);
    echo json_encode(['error' => 'Acceso denegado']);
    exit;
}

$courseId = isset($_GET['curso_id']) ? (int) $_GET['curso_id'] : 0;
$subjectId = isset($_GET['materia_id']) ? (int) $_GET['materia_id'] : 0;
$cargoId = isset($_GET['cargo_id']) ? (int) $_GET['cargo_id'] : 0; // Solo para compatibilidad con la UI
$baseDate = isset($_GET['fecha']) && $_GET['fecha'] !== '' ? (string) $_GET['fecha'] : null;

if ($courseId <= 0 || $subjectId <= 0 || $cargoId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Parámetros incompletos']);
    exit;
}

try {
    $query = [];
    if ($baseDate !== null) {
        $query['fecha'] = $baseDate;
    }

    $response = miEt20ApiAuthenticatedRequest(
        'GET',
        sprintf('/attendance/cursos/%d/materias/%d/semanal', $courseId, $subjectId),
        null,
        ['query' => $query]
    );

    $data = $response['data'] ?? [];
    if (!is_array($data) || empty($data)) {
        $data = $response['body']['data'] ?? $response['body'] ?? [];
    }

    echo json_encode($data, JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status < 400 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}
