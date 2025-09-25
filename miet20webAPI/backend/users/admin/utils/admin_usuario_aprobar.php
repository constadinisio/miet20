<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header('Location: /login.php?error=rol');
    exit;
}

require_once __DIR__ . '/api_client.php';

$csrfToken = $_POST['csrf'] ?? '';
if ($csrfToken !== '' && (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrfToken))) {
    header('Location: /users/admin/usuarios.php?error=csrf');
    exit;
}

$userId = filter_var($_POST['usuario_id'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
if ($userId === null) {
    header('Location: /users/admin/usuarios.php?error=datos_invalidos');
    exit;
}

$roleValue = $_POST['rol'] ?? null;
$resolvedRole = null;
if ($roleValue !== null && $roleValue !== '') {
    if (is_numeric($roleValue)) {
        $roleNumeric = (int) $roleValue;
        if ($roleNumeric > 0) {
            $resolvedRole = $roleNumeric;
        }
    } else {
        $resolvedRole = trim((string) $roleValue);
    }
}

$additionalRoles = [];
if (isset($_POST['roles_adicionales'])) {
    $rawRoles = is_array($_POST['roles_adicionales']) ? $_POST['roles_adicionales'] : [$_POST['roles_adicionales']];
    foreach ($rawRoles as $rawRole) {
        $normalized = filter_var($rawRole, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]);
        if ($normalized !== false) {
            $additionalRoles[] = (int) $normalized;
        }
    }
    $additionalRoles = array_values(array_unique($additionalRoles));
}

$payload = [];
if ($resolvedRole !== null) {
    $payload['rol'] = $resolvedRole;
}
if (!empty($additionalRoles)) {
    $payload['rolesAdicionales'] = $additionalRoles;
}

try {
    admin_call_api('POST', '/usuarios/' . $userId . '/aprobar', $payload ?: []);
    header('Location: /users/admin/usuarios.php?ok=aprobado');
    exit;
} catch (RuntimeException $exception) {
    $_SESSION['admin_usuarios_error'] = $exception->getMessage();
    $status = (int) $exception->getCode();

    if ($status === 404) {
        header('Location: /users/admin/usuarios.php?error=usuario_no_encontrado');
        exit;
    }

    if ($status === 400) {
        header('Location: /users/admin/usuarios.php?error=datos_invalidos');
        exit;
    }

    header('Location: /users/admin/usuarios.php?error=api');
    exit;
}
