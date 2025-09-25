<?php
require_once __DIR__ . '/api_client.php';

$MIET20_CAMPOS_COMUNES_DEFECTO = [
    'nombre'           => 'Nombre',
    'apellido'         => 'Apellido',
    'mail'             => 'Correo electrónico',
    'dni'              => 'DNI',
    'telefono'         => 'Teléfono',
    'direccion'        => 'Dirección',
    'fecha_nacimiento' => 'Fecha de nacimiento',
];

$MIET20_CAMPOS_EXTRA_DEFECTO = [
    'ficha_censal' => 'Ficha censal',
];

function miEt20CamposObligatoriosFallback(?int $rolActivo): array
{
    global $MIET20_CAMPOS_COMUNES_DEFECTO, $MIET20_CAMPOS_EXTRA_DEFECTO;

    $requiereExtra = $rolActivo !== null && $rolActivo !== 4;
    $extraActivos = $requiereExtra ? $MIET20_CAMPOS_EXTRA_DEFECTO : [];

    return [
        'common' => $MIET20_CAMPOS_COMUNES_DEFECTO,
        'extra' => $MIET20_CAMPOS_EXTRA_DEFECTO,
        'activeExtra' => $extraActivos,
        'fields' => $extraActivos ? array_merge($MIET20_CAMPOS_COMUNES_DEFECTO, $extraActivos) : $MIET20_CAMPOS_COMUNES_DEFECTO,
    ];
}

function miEt20CamposObligatoriosDesdeApi(?int $rolActivo): array
{
    $claveCache = $rolActivo !== null ? 'rol_' . $rolActivo : 'default';
    static $cache = [];

    if (array_key_exists($claveCache, $cache)) {
        return $cache[$claveCache];
    }

    $fallback = miEt20CamposObligatoriosFallback($rolActivo);

    $consulta = [];
    if ($rolActivo !== null) {
        $consulta['roleId'] = $rolActivo;
    }

    $resultado = $fallback;

    $usarAutenticado = session_status() === PHP_SESSION_ACTIVE && !empty($_SESSION['api_tokens']['accessToken']);
    $metodo = $usarAutenticado ? 'miEt20ApiAuthenticatedRequest' : 'miEt20ApiRequest';

    try {
        $respuesta = $metodo('GET', '/users/required-fields', null, ['query' => $consulta]);
        $datos = is_array($respuesta['data'] ?? null) ? $respuesta['data'] : [];

        if (!empty($datos)) {
            $comunes = isset($datos['common']) && is_array($datos['common']) ? $datos['common'] : $fallback['common'];
            $extraActivos = isset($datos['extra']) && is_array($datos['extra']) ? $datos['extra'] : $fallback['activeExtra'];
            $camposActivos = isset($datos['fields']) && is_array($datos['fields'])
                ? $datos['fields']
                : ($extraActivos ? array_merge($comunes, $extraActivos) : $comunes);

            $resultado = [
                'common' => $comunes,
                'extra' => $fallback['extra'],
                'activeExtra' => $extraActivos,
                'fields' => $camposActivos,
            ];
        }
    } catch (RuntimeException $exception) {
        // Se utiliza el fallback en caso de cualquier error de comunicación con la API.
    }

    $cache[$claveCache] = $resultado;

    return $resultado;
}

$rolActivo = null;
if (session_status() === PHP_SESSION_ACTIVE && isset($_SESSION['usuario']['rol'])) {
    $rolActivo = (int) $_SESSION['usuario']['rol'];
}

$configCampos = miEt20CamposObligatoriosDesdeApi($rolActivo);

$CAMPOS_OBLIGATORIOS_COMUNES = $configCampos['common'];
$CAMPOS_OBLIGATORIOS_EXTRA = $configCampos['extra'];
$CAMPOS_OBLIGATORIOS_EXTRA_ACTIVOS = $configCampos['activeExtra'];
$CAMPOS_OBLIGATORIOS_ACTIVOS = $configCampos['fields'];
