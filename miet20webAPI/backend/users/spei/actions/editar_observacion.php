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

$id = filter_input(INPUT_POST, 'id', FILTER_VALIDATE_INT);
if (!$id) {
    spei_set_flash('error', 'El identificador de la netbook es inválido.');
    spei_redirect('/users/spei/stock.php');
}

$observaciones = trim((string) ($_POST['observaciones'] ?? ''));

$payload = [
    'observaciones' => $observaciones !== '' ? $observaciones : null
];

try {
    spei_call_api('PUT', '/devices/' . $id, $payload);
    spei_set_flash('success', 'Las observaciones se actualizaron correctamente.');
} catch (RuntimeException $exception) {
    spei_set_flash('error', 'No se pudieron actualizar las observaciones: ' . $exception->getMessage());
}

spei_redirect('/users/spei/stock.php');
