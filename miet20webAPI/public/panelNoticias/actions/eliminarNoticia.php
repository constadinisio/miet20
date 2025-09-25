<?php
session_start();

if (!isset($_SESSION['usuario']) || empty($_SESSION['usuario']['permNoticia'])) {
    http_response_code(403);
    exit('Acceso no autorizado');
}

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    http_response_code(405);
    exit('Método no permitido');
}

$csrfToken = $_POST['csrf_token'] ?? '';
if (empty($_SESSION['csrf_token']) || !hash_equals($_SESSION['csrf_token'], $csrfToken)) {
    http_response_code(403);
    exit('Token CSRF inválido');
}

$noticiaId = isset($_POST['id']) ? (int) $_POST['id'] : 0;
if ($noticiaId <= 0) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo identificar la noticia que intentás eliminar.'
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

require_once __DIR__ . '/../includes/jsonLoader.php';

try {
    noticiasEliminar($noticiaId);
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'success',
        'message' => 'La noticia se eliminó correctamente.'
    ];
} catch (RuntimeException $exception) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo eliminar la noticia: ' . $exception->getMessage()
    ];
}

header('Location: ../panelNoticias.php');
exit;
