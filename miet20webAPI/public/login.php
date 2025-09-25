<?php
if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi ET20 - Sistema de Login</title>
    <link rel="icon" type="image/x-icon" href="./images/et20png.png">
    <link rel="stylesheet" href="./output.css" />
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

<body class="bg-gray-50">
    <!-- Navbar -->
    <nav class="bg-white shadow-lg fixed w-full z-50">
        <div class="max-w-7xl mx-auto px-4">
            <div class="flex justify-center items-center h-16">
                <div class="flex items-center">
                    <a href="./index.php" class="flex items-center">
                        <i class="fas text-3xl text-blue-600 mr-4 -right-500"></i>
                        <h1><img src="./images/et20ico.ico" alt="Icono personalizado" class="w-10 h-10"></h1>
                        <span class="text-xl font-semibold text-gray-800 ml-2">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="relative h-screen w-full pt-16 text-white overflow-hidden">
        <!-- Imagen con blur -->
        <div class="absolute inset-0 bg-front-et20 bg-no-repeat bg-cover bg-center filter blur-sm scale-105"></div>

        <!-- Overlay opcional (oscurece un poco para mejorar legibilidad) -->
        <div class="absolute inset-0 bg-black/30"></div>

        <!-- Panel Login -->
        <div class="relative z-10 flex items-center justify-center h-full py-4 px-4">
            <div class="bg-gray-800 text-white rounded-xl shadow-lg w-full max-w-md p-8 space-y-6">
                <h2 class="text-2xl font-bold text-center">Inicio de Sesión</h2>

                <?php
                $error = trim($_GET['error'] ?? '');
                $errorMessage = '';

                switch ($error) {
                    case 'perm':
                        $errorMessage = 'No tenés permisos para acceder a ningún panel.';
                        break;
                    case 'login':
                        $errorMessage = 'Usuario o contraseña incorrectos.';
                        break;
                    case 'correo':
                        $errorMessage = 'Este correo no está autorizado para ingresar.';
                        break;
                    case 'sin_rol':
                        $errorMessage = 'Tu cuenta no tiene roles asignados. Comunicate con un administrador.';
                        break;
                    case 'oauth':
                        $errorMessage = 'No se pudo completar el inicio de sesión con Google. Intentá nuevamente.';
                        break;
                    default:
                        $errorMessage = '';
                }

                if ($errorMessage !== ''): ?>
                    <p class="text-red-600 bg-red-100 border border-red-400 px-4 py-2 rounded mb-4 text-center">
                        <?= htmlspecialchars($errorMessage) ?>
                    </p>
                <?php endif; ?>


                <form action="/includes/validar_login.php" method="POST" class="space-y-4">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <div>
                        <label for="email" class="block text-sm font-medium">Email</label>
                        <input type="email" name="mail" id="usuario"
                            class="mt-1 w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                    </div>

                    <div>
                        <label for="password" class="block text-sm font-medium">Contraseña</label>
                        <input type="password" name="contrasena" id="password" required
                            class="mt-1 w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    </div>

                    <button type="submit"
                        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 rounded-md transition">
                        Iniciar Sesión
                    </button>
                    <button type="button" onclick="window.location.href='./login_google.php'"
                        class="w-full bg-gray-600 hover:bg-gray-700 text-white font-semibold py-2 rounded-md transition flex items-center justify-center gap-2">
                        <img src="./images/google_icon.svg" alt="Google" class="w-4 h-4" />
                        Iniciar Sesión con Google
                    </button>
                </form>
            </div>
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