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

$id = isset($_POST['id']) ? (int) $_POST['id'] : 0;
$titulo = trim($_POST['titulo'] ?? '');
$contenido = trim($_POST['contenido'] ?? '');

if ($id <= 0) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo identificar la noticia que querés editar.'
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

if ($titulo === '' || $contenido === '') {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'El título y el contenido no pueden quedar vacíos.'
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

require_once __DIR__ . '/../includes/jsonLoader.php';

try {
    noticiasActualizar($id, [
        'title' => $titulo,
        'content' => $contenido,
    ]);

    $_SESSION['panel_noticias_flash'] = [
        'type' => 'success',
        'message' => 'La noticia se actualizó correctamente.'
    ];
} catch (RuntimeException $exception) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo actualizar la noticia: ' . $exception->getMessage()
    ];
}

header('Location: ../panelNoticias.php');
exit;
