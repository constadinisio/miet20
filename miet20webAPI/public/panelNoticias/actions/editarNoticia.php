<?php
session_start();

if (!isset($_SESSION['usuario']) || empty($_SESSION['usuario']['permNoticia'])) {
    http_response_code(403);
    exit('Acceso no autorizado');
}

$noticiaId = isset($_GET['id']) ? (int) $_GET['id'] : 0;
if ($noticiaId <= 0) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo identificar la noticia seleccionada.'
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

require_once __DIR__ . '/../includes/jsonLoader.php';

try {
    $noticia = noticiasObtener($noticiaId);
} catch (RuntimeException $exception) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'No se pudo cargar la noticia: ' . $exception->getMessage()
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

if (!$noticia) {
    $_SESSION['panel_noticias_flash'] = [
        'type' => 'error',
        'message' => 'La noticia solicitada no existe o fue eliminada.'
    ];
    header('Location: ../panelNoticias.php');
    exit;
}

$csrfToken = $_SESSION['csrf_token'];
$title = (string) ($noticia['title'] ?? '');
$content = (string) ($noticia['content'] ?? '');
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel Noticias - Editar</title>
    <link rel="stylesheet" href="/../../output.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="icon" type="image/x-icon" href="/../../images/et20png.png">

    <!-- Quill -->
    <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
    <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>

    <style>
        body {
            font-family: Poppins;
        }
    </style>
</head>
<body class="bg-gray-100 min-h-screen p-8 bg-front-et20 bg-no-repeat bg-cover">
    <div class="max-w-xl mx-auto bg-white p-6 rounded shadow-md">
        <h2 class="text-xl font-bold mb-4">Editar Noticia</h2>
        <form action="guardarEdicion.php" method="POST" class="space-y-4">
            <input type="hidden" name="id" value="<?= htmlspecialchars((string) $noticiaId, ENT_QUOTES, 'UTF-8') ?>">
            <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($csrfToken, ENT_QUOTES, 'UTF-8') ?>">

            <label class="block font-semibold">Título:</label>
            <input
                type="text"
                name="titulo"
                value="<?= htmlspecialchars($title, ENT_QUOTES, 'UTF-8') ?>"
                class="w-full border px-3 py-2 rounded"
                required
            >

            <label class="block font-semibold">Contenido:</label>
            <!-- Editor visual -->
            <div id="editor" class="bg-white border px-3 py-2 rounded" style="min-height: 200px;"></div>
            <input type="hidden" name="contenido" id="contenido">

            <div class="text-center">
                <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Guardar cambios</button>
            </div>
        </form>
    </div>

    <script>
        const quill = new Quill('#editor', {
            theme: 'snow'
        });

        // Insertar contenido actual
        quill.root.innerHTML = <?= json_encode($content, JSON_UNESCAPED_UNICODE) ?>;

        // Copiar contenido HTML al enviar el formulario
        document.querySelector('form').addEventListener('submit', function () {
            document.getElementById('contenido').value = quill.root.innerHTML;
        });
    </script>
</body>
</html>
