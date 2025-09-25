<?php
// users/profesor/resumen_profesor.php
session_start();
header('Content-Type: application/json');

const DEBUG = false;

if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 3) {
    http_response_code(403);
    echo json_encode(['ok' => false, 'mensaje' => 'Acceso denegado']);
    exit;
}

require_once __DIR__ . '/../../../backend/includes/db.php';

// --------- Parámetros ---------
$curso_id = isset($_GET['curso_id']) ? (int)$_GET['curso_id'] : 0;
$in_fecha = $_GET['fecha'] ?? date('Y-m-d');
$ts = strtotime($in_fecha);
$fecha = $ts ? date('Y-m-d', $ts) : date('Y-m-d');

if ($curso_id <= 0) {
    echo json_encode(['ok' => false, 'mensaje' => 'curso_id inválido']);
    exit;
}

if (DEBUG) {
    error_log("[resumen_profesor] params: curso_id={$curso_id}, fecha={$fecha}");
}

// --------- Query ---------
$sql = "
    SELECT 
           SUM(CASE WHEN UPPER(TRIM(estado))='P'  THEN 1 ELSE 0 END) AS presentes,
           SUM(CASE WHEN UPPER(TRIM(estado))='A'  THEN 1 ELSE 0 END) AS ausentes,
           SUM(CASE WHEN UPPER(TRIM(estado))='T'  THEN 1 ELSE 0 END) AS tarde,
           COUNT(*) AS total
    FROM asistencia_materia
    WHERE curso_id = ?
      AND fecha = ?
";
$stmt = $conexion->prepare($sql);
if (!$stmt) {
    if (DEBUG) error_log("[resumen_profesor] prepare error: " . $conexion->error);
    http_response_code(500);
    echo json_encode(['ok' => false, 'mensaje' => 'Error preparando SQL']);
    exit;
}
$stmt->bind_param('is', $curso_id, $fecha);
$stmt->execute();
$row = $stmt->get_result()->fetch_assoc();
$stmt->close();

$R = [
  'presentes' => (int)($row['presentes'] ?? 0),
  'ausentes'  => (int)($row['ausentes'] ?? 0),
  'tarde'     => (int)($row['tarde'] ?? 0),
  'total'     => (int)($row['total'] ?? 0),
];

if (DEBUG) {
    error_log("[resumen_profesor] row=" . json_encode($R));
}

// --------- Respuesta ---------
echo json_encode([
  'ok'               => true,
  'curso_id'         => $curso_id,
  'fecha'            => $fecha,
  'fecha_formateada' => date('d/m/Y', strtotime($fecha)),
  'turno'            => $R,
  'totales'          => $R
]);