<?php
session_start();

require_once __DIR__ . '/api_client.php';
require_once __DIR__ . '/config_campos.php';

if (!isset($_SESSION['usuario']['id'])) {
    header('Location: /login.php');
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
    if (!in_array($campo, $allowedFields, true)) {
        continue;
    }
    $payload[$campo] = is_string($valor) ? trim($valor) : $valor;
}

$redirect = $_SESSION['redirect_after_datos'] ?? '/index.php';
unset($_SESSION['redirect_after_datos']);

if (empty($payload)) {
    $_SESSION['completar_datos_error'] = 'No se recibieron datos para actualizar.';
    header('Location: ' . $redirect);
    exit;
}

try {
    $response = miEt20ApiAuthenticatedRequest('PATCH', '/users/' . $usuarioId, $payload);
    $updatedUser = $response['data'] ?? null;

    if (is_array($updatedUser)) {
        miEt20ApiApplyAuthData(['user' => $updatedUser], ['setActiveRole' => false]);
    }

    unset($_SESSION['completar_datos']);
    unset($_SESSION['completar_datos_error']);
} catch (RuntimeException $exception) {
    $_SESSION['completar_datos_error'] = $exception->getMessage() ?: 'No se pudo actualizar la información.';
}

header('Location: ' . $redirect);
exit;
