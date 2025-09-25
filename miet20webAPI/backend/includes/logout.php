<?php
session_start();
require_once __DIR__ . '/api_client.php';

$refreshToken = $_SESSION['api_tokens']['refreshToken'] ?? null;

if ($refreshToken) {
    try {
        miEt20ApiAuthenticatedRequest('POST', '/auth/logout', [
            'refreshToken' => $refreshToken,
            'allDevices' => false,
        ]);
    } catch (RuntimeException $exception) {
        if (!in_array($exception->getCode(), [401, 403], true)) {
            error_log('Error al cerrar sesión en la API: ' . $exception->getMessage());
        }
    }
}

$_SESSION = [];
if (ini_get('session.use_cookies')) {
    $params = session_get_cookie_params();
    setcookie(session_name(), '', time() - 42000, $params['path'], $params['domain'], $params['secure'], $params['httponly']);
}
session_destroy();

header('Location: /login.php');
exit;
