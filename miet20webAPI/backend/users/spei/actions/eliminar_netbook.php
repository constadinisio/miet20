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
    spei_set_flash('error', 'El identificador de la netbook es inválido.');
    spei_redirect('/users/spei/stock.php');
}

try {
    spei_call_api('DELETE', '/devices/' . $id);
    spei_set_flash('success', 'La netbook se eliminó correctamente.');
} catch (RuntimeException $exception) {
    spei_set_flash('error', 'No se pudo eliminar la netbook: ' . $exception->getMessage());
}

spei_redirect('/users/spei/stock.php');
