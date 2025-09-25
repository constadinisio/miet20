<?php
session_start();

if (!isset($_SESSION['usuario'])) {
    header('Location: /login.php');
    exit;
}

require_once __DIR__ . '/../../backend/includes/api_client.php';

$mensaje = '';
$mensajeTipo = '';
$categoria = '';
$autor = '';
$descripcion = '';

try {
    $categoriasResponse = miEt20ApiRequest('GET', '/gallery/categories', null, [
        'timeout' => 15,
    ]);
    $categoriasData = is_array($categoriasResponse['data'] ?? null) ? $categoriasResponse['data'] : [];
    $categoriasLista = is_array($categoriasData['categories'] ?? null) ? $categoriasData['categories'] : [];
} catch (RuntimeException $exception) {
    $mensaje = '⚠️ ' . $exception->getMessage();
    $mensajeTipo = 'alerta';
    $categoriasLista = [];
}

$categoriasOptions = [];
foreach ($categoriasLista as $categoria) {
    $clave = isset($categoria['key']) ? (string) $categoria['key'] : (string) ($categoria['name'] ?? '');
    $nombre = isset($categoria['name']) ? (string) $categoria['name'] : $clave;
    if ($clave !== '') {
        $categoriasOptions[$clave] = $nombre;
    }
}

if (empty($categoriasOptions)) {
    $categoriasOptions = [
        'Eventos' => 'Eventos',
        'Talleres' => 'Talleres',
        'Especialidades' => 'Especialidades',
    ];
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $categoria = isset($_POST['categoria']) ? (string) $_POST['categoria'] : '';
    $autor = trim($_POST['autor'] ?? '');
    $descripcion = trim($_POST['descripcion'] ?? '');
    $archivo = $_FILES['imagen'] ?? null;

    if ($categoria === '' || !array_key_exists($categoria, $categoriasOptions)) {
        $mensaje = '❌ Categoría inválida.';
        $mensajeTipo = 'error';
    } elseif ($autor === '' || $descripcion === '') {
        $mensaje = '❌ El autor y la descripción son obligatorios.';
        $mensajeTipo = 'error';
    } elseif (!$archivo || $archivo['error'] !== UPLOAD_ERR_OK) {
        $mensaje = '❌ Ocurrió un problema al subir la imagen.';
        $mensajeTipo = 'error';
    } else {
        $maxSize = 2 * 1024 * 1024; // 2MB
        if ($archivo['size'] > $maxSize) {
            $mensaje = '❌ El archivo excede el tamaño permitido (2MB).';
            $mensajeTipo = 'error';
        } else {
            $finfo = new finfo(FILEINFO_MIME_TYPE);
            $mime = $finfo->file($archivo['tmp_name']);
            $allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];

            if (!in_array($mime, $allowedMimes, true)) {
                $mensaje = '❌ Tipo de archivo no permitido. Solo JPG, PNG o WEBP.';
                $mensajeTipo = 'error';
            } else {
                $contenido = file_get_contents($archivo['tmp_name']);

                if ($contenido === false) {
                    $mensaje = '❌ No se pudo leer la imagen subida.';
                    $mensajeTipo = 'error';
                } else {
                    try {
                        miEt20ApiAuthenticatedRequest('POST', '/gallery/items', [
                            'category' => $categoria,
                            'author' => $autor,
                            'description' => $descripcion,
                            'fileName' => $archivo['name'],
                            'fileMime' => $mime,
                            'fileData' => base64_encode($contenido),
                        ]);

                        $mensaje = '✅ Imagen subida correctamente.';
                        $mensajeTipo = 'exito';
                        $categoria = '';
                        $autor = '';
                        $descripcion = '';
                    } catch (RuntimeException $exception) {
                        if ($exception->getCode() === 401) {
                            header('Location: /login.php?error=session');
                            exit;
                        }

                        $mensaje = '❌ ' . $exception->getMessage();
                        $mensajeTipo = 'error';
                    }
                }
            }
        }
    }
}
?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Panel - Galeria de Imagenes</title>
    <link rel="stylesheet" href="/output.css">
    <link rel="icon" type="image/x-icon" href="/images/et20png.png">
</head>

<body class="bg-gray-50">
    <!-- Navbar -->
    <nav class="bg-white shadow-lg fixed w-full z-50">
        <div class="max-w-7xl mx-auto px-4">
            <div class="flex justify-center items-center h-16">
                <div class="flex items-center">
                    <a href="/index.php" class="flex items-center">
                        <i class="fas text-3xl text-blue-600 mr-4 -right-500"></i>
                        <h1><img src="/images/et20ico.ico" alt="Icono personalizado" class="w-10 h-10"></h1>
                        <span class="text-xl font-semibold text-gray-800 ml-2">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <section class="relative min-h-screen w-full pt-16 text-white overflow-hidden">
        <!-- Imagen con blur -->
        <div class="absolute inset-0 bg-front-et20 bg-no-repeat bg-cover bg-center filter blur-sm scale-105"></div>

        <!-- Overlay opcional (oscurece un poco para mejorar legibilidad) -->
        <div class="absolute inset-0 bg-black/30"></div>

        <div class="relative max-w-xl mx-auto mt-12 mb-12 bg-white p-6 rounded shadow text-black">
            <div class="flex justify-between items-center mb-4">
                <h1 class="text-2xl font-bold">Subir nueva imagen</h1>
                <a href="/includes/logout.php" class="bg-red-600 text-white px-4 py-2 rounded transition-colors hover:bg-red-700">
                    Cerrar sesión
                </a>
            </div>

            <?php if ($mensaje): ?>
                <?php
                $alertaClases = 'bg-blue-100 border border-blue-300 text-blue-800';
                if ($mensajeTipo === 'error') {
                    $alertaClases = 'bg-red-100 border border-red-300 text-red-800';
                } elseif ($mensajeTipo === 'exito') {
                    $alertaClases = 'bg-green-100 border border-green-300 text-green-800';
                } elseif ($mensajeTipo === 'alerta') {
                    $alertaClases = 'bg-yellow-100 border border-yellow-300 text-yellow-800';
                }
                ?>
                <div class="mb-4 rounded p-3 <?= $alertaClases ?>"><?= htmlspecialchars($mensaje) ?></div>
            <?php endif; ?>

            <form method="POST" enctype="multipart/form-data" class="space-y-4 mb-12">
                <div>
                    <label class="block font-semibold">Categoría</label>
                    <select name="categoria" required class="w-full border rounded p-2">
                        <option value="">Seleccioná una</option>
                        <?php foreach ($categoriasOptions as $clave => $nombre): ?>
                            <option value="<?= htmlspecialchars($clave) ?>" <?= $categoria === $clave ? 'selected' : '' ?>>
                                <?= htmlspecialchars($nombre) ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <div>
                    <label class="block font-semibold">Autor</label>
                    <input type="text" name="autor" required class="w-full border rounded p-2" value="<?= htmlspecialchars($autor) ?>">
                </div>

                <div>
                    <label class="block font-semibold">Descripción</label>
                    <textarea name="descripcion" rows="3" required class="w-full border rounded p-2"><?= htmlspecialchars($descripcion) ?></textarea>
                </div>

                <div>
                    <label class="block font-semibold">Subir una imagen</label>
                    <input type="file" name="imagen" accept=".webp,.png,.jpg,.jpeg" required class="bg-yellow-500 text-white px-2 py-2 rounded transition-colors hover:bg-yellow-600">
                </div>

                <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition">
                    Subir
                </button>
            </form>
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
</body>

</html>