<?php
session_start();

if (!isset($_SESSION['usuario'])) {
    header('Location: ./actions/validar_login.php');
    exit;
}

$usuario = $_SESSION['usuario'];

if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

require_once __DIR__ . '/includes/jsonLoader.php';

$csrfToken = $_SESSION['csrf_token'];
$flashMessage = $_SESSION['panel_noticias_flash'] ?? null;
unset($_SESSION['panel_noticias_flash']);

$noticias = [];
$listaError = '';

try {
    $resultado = noticiasListar([
        'limit' => 50,
        'sortDirection' => 'desc',
    ]);

    $noticias = $resultado['items'] ?? [];
} catch (RuntimeException $exception) {
    $listaError = $exception->getMessage();
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Noticias</title>
    <link rel="stylesheet" href="/../output.css">
    <link rel="icon" type="image/x-icon" href="/../images/et20png.png">
    <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
    <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>

<body class="bg-gray-50 font-[Poppins]">
    <!-- Navbar -->
    <nav class="bg-white shadow-lg w-full z-50">
        <div class="max-w-7xl mx-auto px-4">
            <div class="flex justify-center items-center h-16">
                <div class="flex items-center">
                    <a href="/../index.php" class="flex items-center">
                        <h1><img src="../images/et20png.png" alt="Icono personalizado" class="w-10 h-10"></h1>
                        <span class="text-xl font-semibold text-gray-800 ml-2">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <section class="relative min-h-screen w-full pt-2 text-black overflow-hidden px-4">
        <div class="absolute inset-0 bg-front-et20 bg-no-repeat bg-cover bg-center filter blur-sm scale-105"></div>
        <div class="absolute inset-0 bg-black/30"></div>

        <div class="max-w-3xl mx-auto relative bg-white p-4 sm:p-6 rounded shadow-md m-4 sm:m-8">
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
                <h2 class="text-2xl font-bold">Panel de Noticias</h2>
                <a href="/../includes/logout.php" class="bg-red-600 text-white px-4 py-2 rounded transition-colors hover:bg-red-700">Cerrar sesión</a>
            </div>

            <?php if ($flashMessage): ?>
                <div class="mb-4 p-3 rounded <?php
                    echo match ($flashMessage['type'] ?? 'info') {
                        'success' => 'bg-green-100 text-green-700',
                        'error' => 'bg-red-100 text-red-600',
                        'warning' => 'bg-yellow-100 text-yellow-700',
                        default => 'bg-blue-100 text-blue-700'
                    };
                ?>">
                    <?= htmlspecialchars($flashMessage['message'] ?? '', ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>

            <?php if ($listaError): ?>
                <div class="mb-4 p-3 rounded bg-red-100 text-red-700">
                    No se pudieron cargar las noticias: <?= htmlspecialchars($listaError, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>

            <!-- Formulario -->
            <form action="./actions/guardarNoticia.php" method="POST" enctype="multipart/form-data" class="mb-8 space-y-4">
                <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($csrfToken, ENT_QUOTES, 'UTF-8') ?>">
                <input type="text" name="titulo" placeholder="Título" required class="w-full border px-3 py-2 rounded">
                <div id="editor" class="bg-white h-48 mb-2 rounded border"></div>
                <input type="hidden" name="contenido" id="contenido">
                <input type="file" name="imagen" class="bg-yellow-500 text-white px-2 py-2 rounded transition-colors hover:bg-yellow-600" accept="image/jpeg,image/png,image/webp">
                <button type="submit" class="bg-green-600 text-white px-4 py-2 rounded transition-colors hover:bg-green-700">Publicar Noticia</button>
            </form>

            <!-- Noticias -->
            <h3 class="text-xl font-semibold mb-4">Noticias publicadas</h3>
            <input type="text" id="busqueda" placeholder="Buscar noticias..." class="w-full border px-3 py-2 rounded mb-4">
            <ul class="space-y-4">
                <?php foreach ($noticias as $noticia): ?>
                    <?php
                        $noticiaId = isset($noticia['id']) ? (int) $noticia['id'] : 0;
                        $titulo = htmlspecialchars($noticia['title'] ?? '', ENT_QUOTES, 'UTF-8');
                        $contenido = $noticia['content'] ?? '';
                        $coverUrl = $noticia['coverUrl'] ?? '';
                    ?>
                    <li class="border rounded p-4 bg-gray-50">
                        <h4 class="text-lg font-bold flex items-center gap-2">
                            <?= $titulo ?>
                            <?php if (!empty($noticia['status'])): ?>
                                <span class="text-xs uppercase tracking-wide bg-gray-200 text-gray-700 px-2 py-1 rounded"><?= htmlspecialchars($noticia['status'], ENT_QUOTES, 'UTF-8') ?></span>
                            <?php endif; ?>
                        </h4>
                        <?php if ($coverUrl): ?>
                            <div class="mt-2">
                                <img src="<?= htmlspecialchars($coverUrl, ENT_QUOTES, 'UTF-8') ?>" alt="Portada de la noticia" class="max-h-48 rounded shadow">
                            </div>
                        <?php endif; ?>
                        <div class="contenido-noticia mt-2"><?= $contenido ?></div>
                        <div class="mt-4 flex flex-col sm:flex-row sm:space-x-4 space-y-2 sm:space-y-0">
                            <a href="./actions/editarNoticia.php?id=<?= $noticiaId ?>" class="text-blue-600 hover:underline">Editar</a>
                            <form action="./actions/eliminarNoticia.php" method="POST" onsubmit="return confirm('¿Seguro que deseas eliminar esta noticia?');" class="inline">
                                <input type="hidden" name="id" value="<?= $noticiaId ?>">
                                <input type="hidden" name="csrf_token" value="<?= htmlspecialchars($csrfToken, ENT_QUOTES, 'UTF-8') ?>">
                                <button type="submit" class="text-red-600 hover:underline">Eliminar</button>
                            </form>
                        </div>
                    </li>
                <?php endforeach; ?>
            </ul>
        </div>
    </section>
    <!-- Footer -->
    <footer class="bg-gray-800 text-white py-12">
        <div class="max-w-7xl mx-auto px-4">
            <div class="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
                <p>&copy; 2025 Escuela Técnica 20 D.E. 20 "Carolina Muzilli". Todos los derechos reservados.</p>
            </div>
        </div>
    </footer>

    <!-- Script Quill -->
    <script>
        const quill = new Quill('#editor', {
            theme: 'snow'
        });

        document.querySelector('form').addEventListener('submit', function(e) {
            document.getElementById('contenido').value = quill.root.innerHTML;
        });
    </script>

    <!-- Script para filtrar noticias -->
    <script>
        document.getElementById('busqueda').addEventListener('input', function() {
            const texto = this.value.toLowerCase();
            const noticias = document.querySelectorAll('ul li');

            noticias.forEach(noticia => {
                const titulo = noticia.querySelector('h4').textContent.toLowerCase();
                const contenido = noticia.querySelector('.contenido-noticia').textContent.toLowerCase();

                if (titulo.includes(texto) || contenido.includes(texto)) {
                    noticia.style.display = '';
                } else {
                    noticia.style.display = 'none';
                }
            });
        });
    </script>

</body>

</html>
