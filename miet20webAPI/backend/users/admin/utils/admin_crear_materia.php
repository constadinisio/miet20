<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/api_client.php';

$nombre = trim($_POST['nombre'] ?? '');
$codigo = trim($_POST['codigo'] ?? '');
$categoriaId = $_POST['categoria_id'] ?? null;
$esContraturno = isset($_POST['es_contraturno']) ? 1 : 0;

$categoriaId = filter_var($categoriaId, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;

if ($nombre === '' || $categoriaId === null) {
    header("Location: /users/admin/materias.php?error=faltan_campos");
    exit;
}

$payload = [
    'nombre' => $nombre,
    'codigo' => $codigo !== '' ? $codigo : null,
    'categoria_id' => $categoriaId,
    'es_contraturno' => $esContraturno,
];

try {
    admin_call_api('POST', '/materias', $payload);
    header("Location: /users/admin/materias.php?ok=nueva");
    exit;
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    if ($status === 409) {
        header("Location: /users/admin/materias.php?error=duplicado");
        exit;
    }

    if ($status === 400) {
        header("Location: /users/admin/materias.php?error=datos_invalidos");
        exit;
    }

    $_SESSION['admin_materias_error'] = 'No se pudo crear la materia: ' . $exception->getMessage();
    header("Location: /users/admin/materias.php?error=api");
    exit;
}
