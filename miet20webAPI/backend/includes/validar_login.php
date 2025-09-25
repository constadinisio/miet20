<?php
session_start();
require_once __DIR__ . '/api_client.php';

$mail = trim($_POST['mail'] ?? '');
$contrasena = trim($_POST['contrasena'] ?? '');

if ($mail === '' || $contrasena === '') {
    header('Location: /login.php?error=campos');
    exit;
}

try {
    $response = miEt20ApiRequest('POST', '/auth/login', [
        'email' => $mail,
        'password' => $contrasena,
    ]);

    if (!is_array($response['data'] ?? null)) {
        throw new RuntimeException('La API devolvió una respuesta inesperada.', 502);
    }

    session_regenerate_id(true);
    $_SESSION = [];

    $authData = miEt20ApiApplyAuthData($response['data'], [
        'setActiveRole' => false,
    ]);

    $roles = $authData['roles'];
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
        $_SESSION['roles_disponibles'] = $roles;

        switch ($rol['id']) {
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
    }

    $_SESSION['usuario_pending_roles'] = $roles;
    header('Location: /seleccionar_panel.php');
    exit;
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    $message = strtolower($exception->getMessage());

    if ($status === 403 && str_contains($message, 'rol')) {
        header('Location: /login.php?error=sin_rol');
    } elseif ($status === 403 && str_contains($message, 'activa')) {
        header('Location: /login.php?error=perm');
    } elseif ($status === 401) {
        header('Location: /login.php?error=login');
    } else {
        header('Location: /login.php?error=login');
    }
    exit;
}
