<?php
// public/users/preceptor/preceptor_materias_curso.php
session_start();
header('Content-Type: application/json; charset=UTF-8');

require_once __DIR__ . '/../../../includes/api_client.php';

if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 2) {
    http_response_code(403);
    echo json_encode(['ok'=>false,'mensaje'=>'Acceso denegado']);
    exit;
}

$curso_id = isset($_GET['curso_id']) ? (int)$_GET['curso_id'] : 0;
$fecha    = isset($_GET['fecha']) && $_GET['fecha'] !== '' ? (string) $_GET['fecha'] : date('Y-m-d');

if ($curso_id <= 0) {
    http_response_code(400);
    echo json_encode(['ok'=>false,'mensaje'=>'curso_id inválido']);
    exit;
}

try {
    $response = miEt20ApiAuthenticatedRequest(
        'GET',
        '/attendance/cursos/' . $curso_id . '/materias-del-dia',
        null,
        ['query' => ['fecha' => $fecha]]
    );
} catch (RuntimeException $exception) {
    $statusCode = $exception->getCode();
    if ($statusCode < 100 || $statusCode > 599) {
        $statusCode = 500;
    }
    http_response_code($statusCode);
    echo json_encode([
        'ok' => false,
        'mensaje' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$statusCode = isset($response['status']) ? (int)$response['status'] : 200;
if ($statusCode < 100 || $statusCode > 599) {
    $statusCode = 200;
}
http_response_code($statusCode);

$data = $response['data'] ?? null;
if (!is_array($data)) {
    echo json_encode([
        'ok' => false,
        'mensaje' => 'Respuesta inválida de la API',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

echo json_encode($data, JSON_UNESCAPED_UNICODE);
