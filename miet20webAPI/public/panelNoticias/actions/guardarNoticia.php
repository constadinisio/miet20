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

$contenido = trim($_POST['contenido'] ?? '');
$titulo = trim($_POST['titulo'] ?? '');

if ($titulo === '' || $contenido === '') {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'El título y el contenido son obligatorios para crear una noticia.'
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

require_once __DIR__ . '/../includes/jsonLoader.php';

try {
    $noticia = noticiasCrear([
        'title' => $titulo,
        'content' => $contenido,
        'status' => 'published',
    ]);

    $noticiaId = isset($noticia['id']) ? (int) $noticia['id'] : 0;

    if ($noticiaId > 0 && !empty($_FILES['imagen'])) {
        try {
            noticiasActualizarPortadaDesdeArchivo($noticiaId, $_FILES['imagen']);
        } catch (RuntimeException $coverException) {
            $_SESSION['panel_noticias_flash'] = [
                'type' => 'warning',
                'message' => 'La noticia se creó, pero la imagen de portada no pudo subirse: ' . $coverException->getMessage(),
            ];
            header('Location: ../panelNoticias.php');
            exit;
        }
    } elseif (!empty($_FILES['imagen']) && $noticiaId <= 0) {
        $_SESSION['panel_noticias_flash'] = [
            'type' => 'warning',
            'message' => 'La noticia se creó correctamente, pero no se pudo asociar la imagen de portada.'
        ];
        header('Location: ../panelNoticias.php');
        exit;
    }

    $_SESSION['panel_noticias_flash'] = [
        'type' => 'success',
        'message' => 'La noticia se publicó correctamente.'
    ];
} catch (RuntimeException $exception) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo crear la noticia: ' . $exception->getMessage()
    ];
}

header('Location: ../panelNoticias.php');
exit;
