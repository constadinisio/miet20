<?php
session_start();
header('Content-Type: application/json; charset=UTF-8');

require_once __DIR__ . '/api_client.php';

if (!isset($_SESSION['usuario']['id'])) {
    echo json_encode(['ok' => false, 'mensaje' => 'Sesión expirada']);
    exit;
}

$usuarioId = (int) $_SESSION['usuario']['id'];
$input = $_POST;

try {
    $passwordUpdated = false;

    $currentPassword = trim($input['contrasena_actual'] ?? '');
    $newPassword = trim($input['contrasena_nueva'] ?? '');
    $confirmPassword = trim($input['confirmar_contrasena'] ?? '');

    if ($currentPassword !== '' || $newPassword !== '' || $confirmPassword !== '') {
        if ($newPassword === '' || $confirmPassword === '') {
            echo json_encode(['ok' => false, 'mensaje' => 'Debés completar todos los campos de contraseña']);
            exit;
        }

        if ($newPassword !== $confirmPassword) {
            echo json_encode(['ok' => false, 'mensaje' => 'Las contraseñas no coinciden']);
            exit;
        }

        $passwordResponse = miEt20ApiAuthenticatedRequest(
            'PATCH',
            '/users/' . $usuarioId . '/password',
            [
                'currentPassword' => $currentPassword,
                'newPassword' => $newPassword,
            ]
        );

        if (is_array($passwordResponse['data'] ?? null)) {
            miEt20ApiApplyAuthData(['user' => $passwordResponse['data']], ['setActiveRole' => false]);
        }

        $passwordUpdated = true;
    }

    $familyPayload = [
        'padre_nombre' => $input['padre_nombre'] ?? null,
        'padre_tel' => $input['padre_tel'] ?? null,
        'padre_mail' => $input['padre_mail'] ?? null,
        'madre_nombre' => $input['madre_nombre'] ?? null,
        'madre_tel' => $input['madre_tel'] ?? null,
        'madre_mail' => $input['madre_mail'] ?? null,
        'emergencia_nombre' => $input['emergencia_nombre'] ?? null,
        'emergencia_tel' => $input['emergencia_tel'] ?? null,
    ];

    miEt20ApiAuthenticatedRequest('PUT', '/users/' . $usuarioId . '/familia', $familyPayload);

    $mensaje = $passwordUpdated
        ? 'Contraseña y datos familiares actualizados correctamente'
        : 'Datos familiares actualizados correctamente';

    echo json_encode([
        'ok' => true,
        'mensaje' => $mensaje,
    ]);
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    $message = $exception->getMessage() ?: 'No se pudo actualizar la configuración';

    if ($status === 401) {
        $message = 'La contraseña actual es incorrecta';
    }

    echo json_encode([
        'ok' => false,
        'mensaje' => $message,
    ]);
}
