<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}
require_once __DIR__ . '/../../../backend/includes/api_client.php';

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

$id = filter_var($_GET['id'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
if ($id === null) {
    header('Location: ./usuarios.php?error=datos_invalidos');
    exit;
}

$usuario = $_SESSION['usuario'];
$formError = null;
$rolesError = null;

try {
    $response = miEt20ApiAuthenticatedRequest('GET', '/users/' . $id);
    $usuario_editado = isset($response['data']) && is_array($response['data']) ? $response['data'] : null;
    if (!$usuario_editado) {
        throw new RuntimeException('Usuario no encontrado');
    }
} catch (RuntimeException $exception) {
    $_SESSION['admin_usuarios_error'] = $exception->getMessage() ?: 'No se pudo cargar el usuario solicitado.';
    header('Location: ./usuarios.php?error=usuario_no_encontrado');
    exit;
}

try {
    $rolesResponse = miEt20ApiAuthenticatedRequest('GET', '/roles');
    $roles = isset($rolesResponse['data']) && is_array($rolesResponse['data']) ? $rolesResponse['data'] : [];
} catch (RuntimeException $exception) {
    $roles = [];
    $rolesError = $exception->getMessage() ?: 'No se pudo obtener el listado de roles disponible.';
}

$roles = array_values(array_filter(array_map(static function ($role) {
    if (!is_array($role)) {
        return null;
    }
    $id = isset($role['id']) ? (int) $role['id'] : null;
    $nombre = isset($role['nombre']) ? (string) $role['nombre'] : '';
    if ($id === null || $nombre === '') {
        return null;
    }

    return [
        'id' => $id,
        'nombre' => $nombre,
    ];
}, $roles)));

usort($roles, static function ($a, $b) {
    return $a['id'] <=> $b['id'];
});

$roles_adicionales = array_values(array_unique(array_filter(
    array_map('intval', $usuario_editado['rolesAdicionales'] ?? []),
    static fn ($value) => $value > 0
)));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $csrfToken = $_POST['csrf'] ?? '';
    $rol_principal = filter_var($_POST['rol_principal'] ?? null, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]) ?: null;
    $permNoticia = isset($_POST['permNoticia']) ? 1 : 0;
    $permSubidaArch = isset($_POST['permSubidaArch']) ? 1 : 0;

    $roles_secundarios = $_POST['roles_adicionales'] ?? [];
    if (!is_array($roles_secundarios)) {
        $roles_secundarios = [$roles_secundarios];
    }
    $roles_secundarios = array_values(array_unique(array_filter(array_map(
        static fn ($value) => filter_var($value, FILTER_VALIDATE_INT, ['options' => ['min_range' => 1]]),
        $roles_secundarios
    ), static fn ($value) => $value !== false)));
    $roles_secundarios = array_values(array_filter($roles_secundarios, static fn ($value) => (int) $value !== (int) $rol_principal));

    if ($csrfToken === '' || !hash_equals($_SESSION['csrf'], $csrfToken)) {
        $formError = 'La sesión expiró. Volvé a intentarlo.';
    } elseif ($rol_principal === null) {
        $formError = 'Seleccioná un rol principal válido.';
    } else {
        $payload = [
            'rol' => $rol_principal,
            'rolesAdicionales' => array_map('intval', $roles_secundarios),
            'permNoticia' => $permNoticia,
            'permSubidaArch' => $permSubidaArch,
        ];

        try {
            miEt20ApiAuthenticatedRequest('PUT', '/users/' . $id, $payload);
            header('Location: ./usuarios.php?ok=actualizado');
            exit;
        } catch (RuntimeException $exception) {
            $formError = $exception->getMessage() ?: 'No se pudo actualizar el usuario.';
        }
    }

    if ($rol_principal !== null) {
        $usuario_editado['rol'] = $rol_principal;
    }
    $usuario_editado['permNoticia'] = $permNoticia;
    $usuario_editado['permSubidaArch'] = $permSubidaArch;
    $roles_adicionales = array_map('intval', $roles_secundarios);
}
?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Editar Usuario</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Poppins', sans-serif;
        }

        .sidebar-item {
            min-height: 3.5rem;
            width: 100%;
        }
    </style>
    <!-- DRAWER:CSS START -->
    <style>
        :root {
            --drawer-th: 780px;
        }

        /* Sidebar fijo (modo normal) */
        #sidebar {
            position: fixed;
            left: 0;
            top: 0;
            bottom: 0;
            width: 15rem;
            /* ~ w-60 */
            transform: translateX(0);
            transition: transform .2s ease-in-out;
            z-index: 40;
            background: #fff;
        }

        #sidebar .scroll-area {
            height: 100dvh;
            overflow-y: auto;
            overscroll-behavior: contain;
        }

        /* Contenido con espacio lateral en modo normal */
        main#content {
            padding-left: 15rem;
            margin-left: 20px;
        }

        /* Botón burger y overlay ocultos por defecto */
        #drawerToggle {
            display: none;
        }

        .drawer-overlay {
            display: none;
        }

        /* ===== Drawer responsive ===== */
        @media screen and (max-width: 1800px) {
            #sidebar {
                transform: translateX(-100%);
            }

            body.drawer-open #sidebar {
                transform: translateX(0);
            }

            main#content {
                padding-left: 0 !important;
            }

            #drawerToggle {
                display: inline-flex;
            }

            body.drawer-open .drawer-overlay {
                display: block;
            }
        }

        /* Estética mínima del botón */
        #drawerToggle {
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            border-radius: 12px;
            background: #fff;
            border: 1px solid #e5e7eb;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .06);
        }
    </style>
    <!-- DRAWER:CSS END -->

</head>

<body class="bg-gray-100 min-h-screen flex">
    <!-- DRAWER:HTML START -->
    <div class="drawer-overlay fixed inset-0 bg-black/40 z-30"></div>
    <button id="drawerToggle" class="fixed top-4 left-4 z-50 text-2xl hover:text-indigo-600 transition" aria-label="Abrir menú">☰</button>
    <!-- DRAWER:HTML END -->

    <!-- Sidebar -->
    <nav id="sidebar" class="w-60 transition-all duration-300 bg-white shadow-lg border-r">
        <div class="scroll-area px-4 py-4 flex flex-col gap-2">
            <div class="flex justify-center items-center p-2 mb-4 border-b border-gray-400 h-28">
                <img src="/images/et20ico.ico" class="h-full w-auto object-contain">
            </div>
            <a href="admin.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
                <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
            </a>
            <a href="usuarios.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Usuarios">
                <span class="text-xl">👥</span><span class="sidebar-label">Usuarios</span>
            </a>
            <a href="cursos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Cursos">
                <span class="text-xl">🏫</span><span class="sidebar-label">Cursos</span>
            </a>
            <a href="alumnos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Alumnos">
                <span class="text-xl">👤</span><span class="sidebar-label">Alumnos</span>
            </a>
            <a href="materias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Materias">
                <span class="text-xl">📚</span><span class="sidebar-label">Materias</span>
            </a>
            <a href="cargos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Cargos">
                <span class="text-xl">💼</span><span class="sidebar-label">Cargos</span>
            </a>
            <a href="progresion.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Progresión">
                <span class="text-xl">📈</span><span class="sidebar-label">Progresión</span>
            </a>
            <a href="materias_pendientes.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Panel de Notificaciones">
                <span class="text-xl">📙</span><span class="sidebar-label">Materias Pendientes</span>
            </a>
            <a href="historial.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Historial p/ Curso">
                <span class="text-xl">📋</span><span class="sidebar-label">Historial p/ Curso</span>
            </a>
            <a href="notificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Panel de Notificaciones">
                <span class="text-xl">🔔</span><span class="sidebar-label">Panel de Notificaciones</span>
            </a>
            <button onclick="window.location='/includes/logout.php'" class="sidebar-item flex items-center justify-center gap-2 mt-auto py-2 px-3 rounded-xl text-white bg-red-500 hover:bg-red-600">
                <span class="text-xl">🚪</span><span class="sidebar-label">Salir</span>
            </button>
        </div>
    </nav>
    <main id="content" class="flex-1 p-10">
        <?php if ($rolesError): ?>
            <div class="mb-6 rounded-xl border border-yellow-200 bg-yellow-50 px-4 py-3 text-yellow-800">
                ⚠️ <?= htmlspecialchars($rolesError) ?>
            </div>
        <?php endif; ?>

        <?php if ($formError): ?>
            <div class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-red-800">
                <?= htmlspecialchars($formError) ?>
            </div>
        <?php endif; ?>

        <h1 class="text-2xl font-bold mb-6">Editar Usuario</h1>
        <form method="post" class="max-w-xl bg-white rounded-xl shadow p-6 space-y-4">
            <input type="hidden" name="csrf" value="<?= htmlspecialchars($csrf) ?>">
            <div>
                <label class="font-bold">Nombre:</label>
                <input type="text" value="<?= htmlspecialchars($usuario_editado['nombre']) ?>" class="border rounded px-3 py-2 w-full bg-gray-100" disabled>
            </div>
            <div>
                <label class="font-bold">Apellido:</label>
                <input type="text" value="<?= htmlspecialchars($usuario_editado['apellido']) ?>" class="border rounded px-3 py-2 w-full bg-gray-100" disabled>
            </div>
            <div>
                <label class="font-bold">Email:</label>
                <input type="text" value="<?= htmlspecialchars($usuario_editado['mail']) ?>" class="border rounded px-3 py-2 w-full bg-gray-100" disabled>
            </div>
            <div>
                <label class="font-bold">Rol principal:</label>
                <select name="rol_principal" required class="border rounded px-3 py-2 w-full">
                    <?php foreach ($roles as $rol): ?>
                        <option value="<?= $rol['id'] ?>"
                            <?= ($usuario_editado['rol'] == $rol['id']) ? 'selected' : '' ?>>
                            <?= htmlspecialchars($rol['nombre']) ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div>
                <label class="font-bold">Roles adicionales:</label><br>
                <?php foreach ($roles as $rol): ?>
                    <?php if ($rol['id'] != $usuario_editado['rol']): ?>
                        <label class="inline-flex items-center mr-4">
                            <input type="checkbox" name="roles_adicionales[]" value="<?= $rol['id'] ?>"
                                <?= in_array($rol['id'], $roles_adicionales) ? 'checked' : '' ?>>
                            <span class="ml-2"><?= htmlspecialchars($rol['nombre']) ?></span>
                        </label>
                    <?php endif; ?>
                <?php endforeach; ?>
            </div>
            <div>
                <label class="font-bold">Permisos Especiales:</label><br>
                <label class="inline-flex items-center mr-4">
                    <input type="checkbox" name="permNoticia" value="1" <?= !empty($usuario_editado['permNoticia']) ? 'checked' : '' ?>>
                    <span class="ml-2">Acceso a Panel de Noticias</span>
                </label>
                <label class="inline-flex items-center mr-4">
                    <input type="checkbox" name="permSubidaArch" value="1" <?= !empty($usuario_editado['permSubidaArch']) ? 'checked' : '' ?>>
                    <span class="ml-2">Acceso a Subida de Imágenes</span>
                </label>
            </div>
            <div class="flex gap-2 mt-6">
                <button type="submit" class="px-6 py-2 bg-green-600 text-white rounded hover:bg-green-700 font-bold">
                    Guardar cambios
                </button>
                <a href="./usuarios.php" class="px-6 py-2 bg-gray-400 text-white rounded hover:bg-gray-500">Volver</a>
            </div>
        </form>
    </main>
    <!-- DRAWER:JS START -->
    <script>
        (function() {
            const body = document.body;
            const toggle = document.getElementById('drawerToggle');
            const overlay = document.querySelector('.drawer-overlay');
            if (!toggle || !overlay) return;

            function setOpen(open) {
                body.classList.toggle('drawer-open', open);
                body.classList.toggle('overflow-hidden', open); // bloquea scroll del fondo
                toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
            }

            function toggleDrawer() {
                setOpen(!body.classList.contains('drawer-open'));
            }

            toggle.addEventListener('click', toggleDrawer);
            overlay.addEventListener('click', () => setOpen(false));
            document.addEventListener('keydown', (e) => {
                if (e.key === 'Escape') setOpen(false);
            });

            // Si la altura vuelve a > umbral, cerrar drawer
            function onResize() {
                const th = parseInt(getComputedStyle(document.documentElement).getPropertyValue('--drawer-th'));
                if (window.innerHeight > th) setOpen(false);
            }
            window.addEventListener('resize', onResize);
        })();
    </script>
    <!-- DRAWER:JS END -->
</body>

</html>