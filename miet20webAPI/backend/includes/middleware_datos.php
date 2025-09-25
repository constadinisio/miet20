<?php
require_once 'config_campos.php';
require_once __DIR__ . '/api_client.php';

if (session_status() !== PHP_SESSION_ACTIVE || !isset($_SESSION['usuario']['id'])) {
    return;
}

$usuario = $_SESSION['usuario'];
$usuarioId = (int) $usuario['id'];
$rolActivo = isset($usuario['rol']) ? (int) $usuario['rol'] : null;

$campos_faltantes = [];

try {
    $opciones = [];
    if ($rolActivo !== null) {
        $opciones['query']['roleId'] = $rolActivo;
    }

    $respuesta = miEt20ApiAuthenticatedRequest('GET', '/users/' . $usuarioId . '/missing-fields', null, $opciones);
    if (isset($respuesta['data']['missing']) && is_array($respuesta['data']['missing'])) {
        $campos_faltantes = $respuesta['data']['missing'];
    }
} catch (RuntimeException $exception) {
    foreach ($CAMPOS_OBLIGATORIOS_COMUNES as $campo => $label) {
        $valor = $usuario[$campo] ?? null;
        if ($valor === null || (is_string($valor) && trim($valor) === '') || $valor === '0000-00-00') {
            $campos_faltantes[$campo] = $label;
        }
    }

    if (!empty($CAMPOS_OBLIGATORIOS_EXTRA_ACTIVOS)) {
        foreach ($CAMPOS_OBLIGATORIOS_EXTRA_ACTIVOS as $campo => $label) {
            $valor = $usuario[$campo] ?? null;
            if ($valor === null || (is_string($valor) && trim($valor) === '')) {
                $campos_faltantes[$campo] = $label;
            }
        }
    }
}

if (!empty($campos_faltantes)) {
    $_SESSION['completar_datos'] = $campos_faltantes;
} else {
    unset($_SESSION['completar_datos']);
}