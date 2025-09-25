<?php
function cargarEntorno($ruta)
{
    static $archivosCargados = [];

    if (!is_string($ruta) || $ruta === '') {
        return;
    }

    $realPath = realpath($ruta);
    if ($realPath === false || isset($archivosCargados[$realPath])) {
        return;
    }

    if (!is_readable($realPath)) {
        return;
    }

    $lineas = file($realPath, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lineas as $linea) {
        $linea = trim($linea);

        if ($linea === '' || $linea[0] === '#') {
            continue;
        }

        if (str_starts_with($linea, 'export ')) {
            $linea = trim(substr($linea, 7));
        }

        if (!str_contains($linea, '=')) {
            continue;
        }

        [$clave, $valor] = explode('=', $linea, 2);
        $clave = trim($clave);
        $valor = trim($valor);

        if ($valor !== '' && $valor[0] === '#') {
            continue;
        }

        if (
            ($valor !== '' && $valor[0] === '"' && str_ends_with($valor, '"')) ||
            ($valor !== '' && $valor[0] === "'" && str_ends_with($valor, "'"))
        ) {
            $valor = substr($valor, 1, -1);
            $valor = str_replace(['\\n', '\\r', '\\t', '\\"', "\\'", '\\\\'], ["\n", "\r", "\t", '"', "'", '\\'], $valor);
        }

        $_ENV[$clave] = $valor;
        $_SERVER[$clave] = $valor;
        putenv($clave . '=' . $valor);
    }

    $archivosCargados[$realPath] = true;
}
