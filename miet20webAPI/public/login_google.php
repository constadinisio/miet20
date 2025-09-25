<?php
require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/../backend/includes/api_client.php';
require_once __DIR__ . '/../backend/includes/loadEnv.php';

cargarEntorno(__DIR__ . '/../config/.env');
session_start();

$client = new Google\Client();
$client->setClientId($_ENV['GOOGLE_CLIENT_ID'] ?? '');
$client->setClientSecret($_ENV['GOOGLE_CLIENT_SECRET'] ?? '');
$client->setRedirectUri($_ENV['GOOGLE_REDIRECT_URI'] ?? '');
$client->addScope('email');
$client->addScope('profile');

if (!isset($_GET['code'])) {
    $authUrl = $client->createAuthUrl();
    header('Location: ' . $authUrl);
    exit;
}

$token = $client->fetchAccessTokenWithAuthCode($_GET['code']);
if (!empty($token['error']) || empty($token['id_token'])) {
    header('Location: /login.php?error=oauth');
    exit;
}

$client->setAccessToken($token);
$oauth = new Google\Service\Oauth2($client);
$profile = $oauth->userinfo->get();
$email = $profile->email ?? null;

try {
    $response = miEt20ApiRequest('POST', '/auth/google-login', [
        'idToken' => $token['id_token'],
        'email' => $email,
    ]);

    if (!is_array($response['data'] ?? null)) {
        throw new RuntimeException('Respuesta inesperada de la API', 502);
    }

    session_regenerate_id(true);
    $csrf = $_SESSION['csrf'] ?? null;
    $_SESSION = [];
    if ($csrf !== null) {
        $_SESSION['csrf'] = $csrf;
    }

    $authData = miEt20ApiApplyAuthData($response['data'], [
        'setActiveRole' => false,
    ]);

    $roles = $authData['roles'] ?? [];
    if (empty($roles)) {
        header('Location: /login.php?error=sin_rol');
        exit;
    }

    unset($_SESSION['usuario_pending_roles']);

    $tienePermisoEspecial = !empty($_SESSION['usuario']['permNoticia']) || !empty($_SESSION['usuario']['permSubidaArch']);

    if (count($roles) === 1 && !$tienePermisoEspecial) {
        $rol = $roles[0];
        $_SESSION['usuario']['rol'] = $rol['id'];
        $_SESSION['usuario']['rol_nombre'] = $rol['nombre'];
        $_SESSION['rol_activo'] = $rol['nombre'];

        switch ((int) $rol['id']) {
            case 1:
                $redirect = '/users/admin/admin.php';
                break;
            case 2:
                $redirect = '/users/preceptor/preceptor.php';
                break;
            case 3:
                $redirect = '/users/profesor/profesor.php';
                break;
            case 4:
                $redirect = '/users/alumno/alumno.php';
                break;
            case 5:
                $redirect = '/users/spei/index.php';
                break;
            default:
                $redirect = '/seleccionar_panel.php';
                break;
        }

        header('Location: ' . $redirect);
        exit;
    }

    $_SESSION['usuario_pending_roles'] = $roles;
    header('Location: /seleccionar_panel.php');
    exit;
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();
    $message = strtolower($exception->getMessage() ?? '');

    if ($status === 404) {
        if ($email) {
            $_SESSION['google_email'] = $email;
        }
        header('Location: /registro_google.php');
        exit;
    }

    if ($status === 403 && str_contains($message, 'pendiente')) {
        $version = time();
        echo <<<HTML
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Pendiente de aprobación</title>
    <link href="/output.css?v={$version}" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>body {font-family: 'Poppins', sans-serif;}</style>
</head>
<body class="bg-gray-100 flex items-center justify-center min-h-screen">
    <div class="bg-white p-10 rounded-2xl shadow-xl text-center">
        <h1 class="text-2xl font-bold text-yellow-600 mb-4">Registro pendiente de aprobación</h1>
        <p class="text-gray-700 mb-6">
            Tu registro fue enviado correctamente.<br>
            Un administrador lo revisará y te habilitará el acceso.<br>
            Volvé a intentar en unas horas.
        </p>
        <a href="/login.php" class="inline-block px-4 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700">Ir al inicio</a>
    </div>
</body>
</html>
HTML;
        exit;
    }

    if ($status === 403 && str_contains($message, 'rol')) {
        header('Location: /login.php?error=sin_rol');
        exit;
    }

    if ($status === 403 && str_contains($message, 'activa')) {
        header('Location: /login.php?error=perm');
        exit;
    }

    if ($status === 401) {
        header('Location: /login.php?error=login');
        exit;
    }

    header('Location: /login.php?error=oauth');
    exit;
}
