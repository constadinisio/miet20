<?php
require_once __DIR__ . '/../helpers.php';

try {
    spei_assert_authorized();
} catch (RuntimeException $exception) {
    http_response_code($exception->getCode() ?: 403);
    exit($exception->getMessage());
}

$id = filter_input(INPUT_GET, 'id', FILTER_VALIDATE_INT);
if (!$id) {
    spei_set_flash('error', 'El identificador del préstamo es inválido.');
    spei_redirect('/users/spei/prestamos.php');
}

$payload = [
    'fecha_devolucion' => date('Y-m-d'),
    'hora_devolucion' => date('H:i'),
    'estado' => 'cerrado'
];

try {
    spei_call_api('PUT', '/devices/prestamos/' . $id . '/cerrar', $payload);
    spei_set_flash('success', 'El préstamo se marcó como devuelto.');
} catch (RuntimeException $exception) {
    spei_set_flash('error', 'No se pudo registrar la devolución: ' . $exception->getMessage());
}

spei_redirect('/users/spei/prestamos.php');
