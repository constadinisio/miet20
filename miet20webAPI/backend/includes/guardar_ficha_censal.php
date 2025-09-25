<?php
session_start();
header('Content-Type: text/plain; charset=UTF-8');

require_once __DIR__ . '/api_client.php';

if (!isset($_SESSION['usuario']['id'])) {
    echo 'Sesión expirada.';
    exit;
}

$fichaCensal = isset($_POST['ficha_censal']) ? trim((string) $_POST['ficha_censal']) : '';
if ($fichaCensal === '') {
    echo 'El campo ficha censal es obligatorio.';
    exit;
}

$usuarioId = (int) $_SESSION['usuario']['id'];

try {
    $response = miEt20ApiAuthenticatedRequest('PATCH', '/users/' . $usuarioId, [
        'ficha_censal' => $fichaCensal,
    ]);

    if (is_array($response['data'] ?? null)) {
        miEt20ApiApplyAuthData(['user' => $response['data']], ['setActiveRole' => false]);
    }

    echo 'OK';
} catch (RuntimeException $exception) {
    $message = $exception->getMessage() ?: 'No se pudo guardar la ficha censal.';
    echo $message;
}
