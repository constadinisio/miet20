<?php
require_once __DIR__ . '/../helpers.php';

try {
    spei_assert_authorized();
} catch (RuntimeException $exception) {
    http_response_code($exception->getCode() ?: 403);
    exit($exception->getMessage());
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit('Método no permitido');
}

$carrito = strtoupper(trim((string) ($_POST['carrito'] ?? '')));
$numero = trim((string) ($_POST['numero'] ?? ''));
$numeroSerie = trim((string) ($_POST['numero_serie'] ?? ''));
$observaciones = trim((string) ($_POST['observaciones'] ?? ''));
$estado = trim((string) ($_POST['estado'] ?? ''));

try {
    $fechaAdquisicion = spei_normalize_date_input($_POST['fecha_adquisicion'] ?? null);
} catch (RuntimeException $exception) {
    spei_set_flash('error', $exception->getMessage());
    spei_redirect('/users/spei/stock.php');
}

$payload = [
    'carrito' => $carrito !== '' ? $carrito : null,
    'numero' => $numero !== '' ? $numero : null,
    'numero_serie' => $numeroSerie !== '' ? $numeroSerie : null,
    'fecha_adquisicion' => $fechaAdquisicion,
    'observaciones' => $observaciones !== '' ? $observaciones : null,
    'estado' => $estado !== '' ? $estado : 'En uso'
];

try {
    spei_call_api('POST', '/devices', $payload);
    spei_set_flash('success', 'La netbook se registró correctamente.');
} catch (RuntimeException $exception) {
    spei_set_flash('error', 'No se pudo registrar la netbook: ' . $exception->getMessage());
}

spei_redirect('/users/spei/stock.php');
