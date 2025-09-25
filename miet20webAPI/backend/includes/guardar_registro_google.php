<?php
require_once __DIR__ . '/api_client.php';

$mail = trim($_POST['mail'] ?? '');
$nombre = trim($_POST['nombre'] ?? '');
$apellido = trim($_POST['apellido'] ?? '');
$dni = trim($_POST['dni'] ?? '');
$telefono = trim($_POST['telefono'] ?? '');
$direccion = trim($_POST['direccion'] ?? '');
$fechaNacimiento = trim($_POST['fecha_nacimiento'] ?? '');
$contrasena = $_POST['contrasena'] ?? '';

if ($mail === '' || $nombre === '' || $apellido === '' || $dni === '' || $fechaNacimiento === '' || $contrasena === '') {
    echo 'Faltan campos obligatorios.';
    exit;
}

if (!filter_var($mail, FILTER_VALIDATE_EMAIL)) {
    echo 'El correo electrónico es inválido.';
    exit;
}

$payload = [
    'mail' => $mail,
    'nombre' => $nombre,
    'apellido' => $apellido,
    'dni' => $dni,
    'telefono' => $telefono !== '' ? $telefono : null,
    'direccion' => $direccion !== '' ? $direccion : null,
    'fecha_nacimiento' => $fechaNacimiento,
    'contrasena' => $contrasena,
];

try {
    miEt20ApiRequest('POST', '/registrations', $payload);

    $version = time();
    echo <<<HTML
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Pendiente de Aprobación</title>
    <link href="/output.css?v={$version}" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>body {font-family: 'Poppins', sans-serif;}</style>
</head>
<body class="bg-gray-100 flex flex-col min-h-screen">
    <nav class="bg-white shadow-lg fixed w-full z-50">
        <div class="max-w-7xl mx-auto px-4">
            <div class="flex justify-center items-center h-16">
                <div class="flex items-center">
                    <a href="/index.php" class="flex items-center">
                        <img src="/images/et20ico.ico" alt="Escuela Técnica 20" class="w-10 h-10 mr-4">
                        <span class="text-xl font-semibold text-gray-800">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>
            </div>
        </div>
    </nav>
    <main class="flex-grow flex justify-center items-center">
        <div class="bg-white p-10 rounded-2xl shadow-xl text-center">
            <h1 class="text-2xl font-bold text-yellow-600 mb-4">Registro pendiente de aprobación</h1>
            <p class="text-gray-700 mb-6">
                Tu registro fue enviado correctamente.<br>
                Un administrador lo revisará y te habilitará el acceso.<br>
                Volvé a intentar en unas horas.
            </p>
            <a href="/login.php" class="inline-block px-4 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700">Ir al inicio</a>
        </div>
    </main>
</body>
</html>
HTML;
} catch (RuntimeException $exception) {
    $status = $exception->getCode();
    $message = $exception->getMessage() ?: 'No se pudo registrar la solicitud.';

    if ($status === 409) {
        $message = 'Ya existe un registro pendiente o activo con el mismo DNI o correo electrónico.';
    }

    echo $message;
}
