<?php
session_start();
header('Content-Type: application/json; charset=UTF-8');

require_once __DIR__ . '/api_client.php';
require_once __DIR__ . '/config_campos.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['ok' => false, 'mensaje' => 'Método no permitido']);
    exit;
}

if (!isset($_SESSION['usuario']['id'])) {
    echo json_encode(['ok' => false, 'mensaje' => 'Usuario no autenticado']);
    exit;
}

$csrf = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrf)) {
    echo json_encode(['ok' => false, 'mensaje' => 'CSRF inválido']);
    exit;
}

$usuarioId = (int) $_SESSION['usuario']['id'];

$allowedFields = array_unique(array_merge(
    array_keys($CAMPOS_OBLIGATORIOS_COMUNES),
    array_keys($CAMPOS_OBLIGATORIOS_EXTRA),
    ['telefono', 'direccion', 'foto_url']
));

$payload = [];
foreach ($_POST as $campo => $valor) {
    if ($campo === 'csrf') {
        continue;
    }

    if (!in_array($campo, $allowedFields, true)) {
        continue;
    }

    $payload[$campo] = is_string($valor) ? trim($valor) : $valor;
}

if (empty($payload)) {
    echo json_encode(['ok' => false, 'mensaje' => 'No se recibieron datos']);
    exit;
}

try {
    $response = miEt20ApiAuthenticatedRequest('PATCH', '/users/' . $usuarioId, $payload);
    $updatedUser = $response['data'] ?? null;

    if (is_array($updatedUser)) {
        miEt20ApiApplyAuthData(['user' => $updatedUser], ['setActiveRole' => false]);
    }

    unset($_SESSION['completar_datos']);

    echo json_encode(['ok' => true, 'mensaje' => 'Datos guardados correctamente']);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    $message = $exception->getMessage();

    if ($status === 409) {
        $message = 'El correo electrónico o DNI ya están registrados en otro usuario';
    }

    echo json_encode([
        'ok' => false,
        'mensaje' => $message ?: 'No se pudo actualizar la información'
    ]);
}
