<?php
if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}

require_once __DIR__ . '/../../../backend/notificaciones/notificaciones_utils.php';

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}

$csrf = $_SESSION['csrf'];
$mensaje = '';
$error = '';
$listError = '';
$titulo = trim($_POST['titulo'] ?? 'Notificación de prueba');
$contenido = trim($_POST['contenido'] ?? 'Este es el contenido de prueba.');
$destinatarioSeleccionado = (int) ($_POST['destinatario_id'] ?? 0);
$destinatarioManual = (int) ($_POST['destinatario_id_manual'] ?? 0);
$usuarios = [];

$rolActivo = isset($_SESSION['usuario']['rol']) ? (int) $_SESSION['usuario']['rol'] : 0;

try {
    if ($rolActivo === 1) {
        $response = miEt20ApiAuthenticatedRequest('GET', '/usuarios');
        $usuarios = array_map(
            static fn (array $usuario) => [
                'id' => isset($usuario['id']) ? (int) $usuario['id'] : 0,
                'nombre' => trim(($usuario['apellido'] ?? '') . ', ' . ($usuario['nombre'] ?? '')),
            ],
            $response['data'] ?? []
        );
    } elseif ($rolActivo === 3) {
        $response = miEt20ApiAuthenticatedRequest('GET', '/notificaciones/opciones');
        $usuarios = array_map(
            static fn (array $alumno) => [
                'id' => isset($alumno['id']) ? (int) $alumno['id'] : 0,
                'nombre' => trim(($alumno['apellido'] ?? '') . ', ' . ($alumno['nombre'] ?? '')),
            ],
            $response['data']['alumnos'] ?? []
        );
    }

    $usuarios = array_values(array_filter(
        $usuarios,
        static fn (array $usuario) => $usuario['id'] > 0 && $usuario['nombre'] !== ''
    ));
} catch (RuntimeException $exception) {
    $listError = $exception->getMessage();
}

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'POST') {
    if (!isset($_POST['csrf']) || $_POST['csrf'] !== $csrf) {
        $error = 'Token CSRF inválido.';
    } else {
        $destinatarioId = $destinatarioSeleccionado ?: $destinatarioManual;

        if ($destinatarioId <= 0) {
            $error = 'Debés seleccionar o ingresar un destinatario válido.';
        } elseif ($titulo === '' || $contenido === '') {
            $error = 'El título y el contenido son obligatorios.';
        } else {
            try {
                miEt20ApiAuthenticatedRequest('POST', '/notificaciones', [
                    'titulo' => $titulo,
                    'mensaje' => $contenido,
                    'tipo' => 'INDIVIDUAL',
                    'destinatario_ids' => [$destinatarioId],
                ]);

                $mensaje = '¡Notificación creada y enviada!';
            } catch (RuntimeException $exception) {
                $status = $exception->getCode();
                if ($status === 403) {
                    $error = 'No tenés permisos para enviar notificaciones.';
                } else {
                    $error = $exception->getMessage();
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
  <title>Test Notificaciones</title>
  <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
</head>
<body class="bg-gray-50 flex items-center justify-center min-h-screen">
  <form method="post" class="bg-white shadow-xl p-8 rounded-2xl flex flex-col gap-4 w-full max-w-xl">
    <h1 class="text-xl font-bold mb-2">Crear Notificación de Prueba</h1>

    <?php if ($mensaje): ?>
      <div class="bg-green-100 text-green-700 rounded p-2"><?= htmlspecialchars($mensaje, ENT_QUOTES, 'UTF-8') ?></div>
    <?php endif; ?>

    <?php if ($error): ?>
      <div class="bg-red-100 text-red-600 rounded p-2"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div>
    <?php endif; ?>

    <?php if ($listError): ?>
      <div class="bg-yellow-100 text-yellow-700 rounded p-2"><?= htmlspecialchars($listError, ENT_QUOTES, 'UTF-8') ?></div>
    <?php endif; ?>

    <input type="hidden" name="csrf" value="<?= htmlspecialchars($csrf, ENT_QUOTES, 'UTF-8') ?>">

    <label class="font-semibold">Destinatario:</label>
    <?php if (!empty($usuarios)): ?>
      <select name="destinatario_id" class="border rounded p-2" required>
        <option value="">Seleccioná un usuario</option>
        <?php foreach ($usuarios as $usuario): ?>
          <option value="<?= $usuario['id'] ?>" <?= $usuario['id'] === $destinatarioSeleccionado ? 'selected' : '' ?>>
            <?= htmlspecialchars($usuario['nombre'], ENT_QUOTES, 'UTF-8') ?>
          </option>
        <?php endforeach; ?>
      </select>
    <?php else: ?>
      <p class="text-sm text-gray-600">Ingresá el ID del destinatario para enviar la notificación.</p>
    <?php endif; ?>

    <label class="font-semibold">ID Destinatario (manual):</label>
    <input
      type="number"
      name="destinatario_id_manual"
      class="border rounded p-2"
      min="1"
      value="<?= $destinatarioManual > 0 ? $destinatarioManual : '' ?>"
      placeholder="Ej: 42"
    >

    <label class="font-semibold">Título:</label>
    <input type="text" name="titulo" class="border rounded p-2" value="<?= htmlspecialchars($titulo, ENT_QUOTES, 'UTF-8') ?>" required>

    <label class="font-semibold">Contenido:</label>
    <textarea name="contenido" class="border rounded p-2" required><?= htmlspecialchars($contenido, ENT_QUOTES, 'UTF-8') ?></textarea>

    <button type="submit" class="mt-4 bg-blue-600 text-white rounded-xl px-4 py-2 font-bold hover:bg-blue-700">Enviar</button>
  </form>
</body>
</html>
