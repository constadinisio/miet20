<?php
session_start();
$google_email = $_SESSION['google_email'] ?? '';

if ($google_email === '') {
    header('Location: /login.php?error=oauth');
    exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Registro de Usuario - MiET20</title>
    <link rel="stylesheet" href="/output.css" />
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">

    <style>
        body {
            font-family: 'Poppins', sans-serif;
        }
    </style>
</head>

<body class="bg-gray-100 flex flex-col min-h-screen">
    <!-- Header -->
    <nav class="bg-white shadow-lg fixed w-full z-50">
        <div class="max-w-7xl mx-auto px-4">
            <div class="flex justify-center items-center h-16">
                <div class="flex items-center">
                    <a href="/index.php" class="flex items-center">
                        <img src="/images/et20ico.ico" alt="Icono personalizado" class="w-10 h-10">
                        <span class="text-xl font-semibold text-gray-800 ml-2">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>
            </div>
        </div>
    </nav>
    <main class="flex-grow flex justify-center items-center">
        
        <form action="guardar_registro_google.php" method="POST" class="bg-white p-8 mt-20 mb-8 rounded-2xl shadow-xl w-full max-w-md space-y-5">
            <h2 class="text-2xl font-bold text-center mb-6 text-blue-700">Registro de usuario</h2>

            <input type="hidden" name="mail" value="<?= htmlspecialchars($google_email) ?>">
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre" required class="w-full border rounded-xl p-2 focus:outline-blue-500" value="<?= htmlspecialchars($google_nombre ?? '') ?>">
            </div>
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="apellido">Apellido</label>
                <input type="text" id="apellido" name="apellido" required class="w-full border rounded-xl p-2 focus:outline-blue-500" value="<?= htmlspecialchars($google_apellido ?? '') ?>">
            </div>
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="dni">DNI</label>
                <input type="text" id="dni" name="dni" required class="w-full border rounded-xl p-2" maxlength="15">
            </div>
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="telefono">Teléfono</label>
                <input type="tel" id="telefono" name="telefono" class="w-full border rounded-xl p-2" maxlength="20">
            </div>
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="direccion">Dirección</label>
                <input type="text" id="direccion" name="direccion" class="w-full border rounded-xl p-2" maxlength="60">
            </div>
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="fecha_nacimiento">Fecha de nacimiento</label>
                <input type="date" id="fecha_nacimiento" name="fecha_nacimiento" required class="w-full border rounded-xl p-2">
            </div>
            <div>
                <label class="block mb-1 font-semibold text-gray-700" for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena" required class="w-full border rounded-xl p-2" minlength="6" autocomplete="new-password">
                <small class="text-gray-500">Mínimo 6 caracteres.</small>
            </div>
            <input type="hidden" name="csrf" value="<?= $csrf ?>">
            <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 rounded-xl transition">Registrarme</button>
        </form>
    </main>
    <!-- Footer -->
    <footer class="bg-gray-800 text-white py-4 text-center">
        <div class="max-w-7xl mx-auto px-4">
            <div class="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
                <p>&copy; 2025 Escuela Técnica 20 D.E. 20 "Carolina Muzilli". Todos los derechos reservados.</p>
            </div>
        </div>
    </footer>
</body>

</html>