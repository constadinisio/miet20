<?php
session_start();
require_once __DIR__ . '/api_client.php';

if (!isset($_SESSION['usuario']) || !isset($_POST['rol'])) {
    header('Location: /login.php?error=rol');
    exit;
}

$rolesDisponibles = $_SESSION['roles_disponibles'] ?? [];
$rolId = (int) $_POST['rol'];
$rolValido = null;

foreach ($rolesDisponibles as $rol) {
    if ((int) ($rol['id'] ?? 0) === $rolId) {
        $rolValido = [
            'id' => (int) $rol['id'],
            'nombre' => $rol['nombre'] ?? '',
        ];
        break;
    }
}

if ($rolValido === null) {
    header('Location: /login.php?error=rol');
    exit;
}

try {
    $payload = ['roleId' => $rolId];
    $refreshToken = $_SESSION['api_tokens']['refreshToken'] ?? null;
    if ($refreshToken) {
        $payload['refreshToken'] = $refreshToken;
    }

    $response = miEt20ApiAuthenticatedRequest('POST', '/auth/switch-role', $payload);
    if (!is_array($response['data'] ?? null)) {
        throw new RuntimeException('La API devolvió una respuesta inesperada.', 502);
    }

    miEt20ApiApplyAuthData($response['data'], [
        'setActiveRole' => true,
        'activeRole' => $rolValido,
    ]);
    unset($_SESSION['usuario_pending_roles']);

    switch ($rolId) {
        case 1:
            header('Location: /users/admin/admin.php');
            break;
        case 2:
            header('Location: /users/preceptor/preceptor.php');
            break;
        case 3:
            header('Location: /users/profesor/profesor.php');
            break;
        case 4:
            header('Location: /users/alumno/alumno.php');
            break;
        case 5:
            header('Location: /users/spei/index.php');
            break;
        default:
            header('Location: /seleccionar_panel.php');
            break;
    }
    exit;
} catch (RuntimeException $exception) {
    header('Location: /login.php?error=rol');
    exit;
}
