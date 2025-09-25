<?php
// Importa asistencias de PROFESOR (asistencia_materia) a PRECEPTOR (asistencia_general)
declare(strict_types=1);
header('Content-Type: application/json; charset=UTF-8');

date_default_timezone_set('America/Argentina/Buenos_Aires');

set_exception_handler(function ($e) {
  http_response_code(500);
  echo json_encode(['ok' => false, 'mensaje' => 'Excepción: ' . $e->getMessage()]);
  exit;
});
set_error_handler(function ($sev, $msg, $file, $line) {
  http_response_code(500);
  echo json_encode(['ok' => false, 'mensaje' => "PHP error: $msg @ $file:$line"]);
  exit;
});

session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 2) {
  http_response_code(403);
  echo json_encode(['ok' => false, 'mensaje' => 'Acceso denegado']);
  exit;
}

require_once __DIR__ . '/../../../includes/api_client.php';

$in   = json_decode(file_get_contents('php://input'), true) ?? [];
$csrf = $in['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrf)) {
  echo json_encode(['ok' => false, 'mensaje' => 'CSRF inválido']);
  exit;
}

$curso_id     = (int)($in['curso_id'] ?? 0);
$materia_ids  = array_map('intval', (array)($in['materia_ids'] ?? []));
$materia_ids  = array_values(array_filter($materia_ids, fn($id) => $id > 0));
$fecha_in     = trim((string)($in['fecha'] ?? ''));
$dry_run      = (bool)($in['dry_run'] ?? false);

if ($curso_id <= 0 || empty($materia_ids) || $fecha_in === '') {
  http_response_code(400);
  echo json_encode(['ok' => false, 'mensaje' => 'Parámetros incompletos']);
  exit;
}

$payload = [
  'fecha' => $fecha_in,
  'materia_ids' => $materia_ids,
  'dry_run' => $dry_run,
];

try {
  $response = miEt20ApiAuthenticatedRequest(
    'POST',
    '/attendance/cursos/' . $curso_id . '/importar-profesor',
    $payload
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
