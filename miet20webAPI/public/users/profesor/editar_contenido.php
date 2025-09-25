<?php
session_start();
require_once __DIR__ . '/../../../backend/includes/api_client.php';

if ((int)($_SESSION['usuario']['rol'] ?? 0) !== 3) {
    header('Location: /login.php?error=rol');
    exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}

$usuario = $_SESSION['usuario'];
$csrf = $_SESSION['csrf'];
$entryId = isset($_POST['id']) ? (int) $_POST['id'] : (int) ($_GET['id'] ?? 0);

if ($entryId <= 0) {
    header('Location: /users/profesor/libro_temas.php?error=tema');
    exit;
}

try {
    $response = miEt20ApiAuthenticatedRequest('GET', '/lesson-book/' . $entryId);
    $contenido = $response['data'] ?? null;
} catch (RuntimeException $exception) {
    header('Location: /users/profesor/libro_temas.php?error=' . urlencode($exception->getMessage()));
    exit;
}

if (!$contenido || (int)($contenido['profesor_id'] ?? 0) !== (int) $usuario['id']) {
    header('Location: /users/profesor/libro_temas.php?error=tema');
    exit;
}

$cursoId = (int)($contenido['curso_id'] ?? 0);
$materiaId = (int)($contenido['materia_id'] ?? 0);
$volverParams = [];
if ($cursoId > 0) {
    $volverParams['curso_id'] = $cursoId;
}
if ($materiaId > 0) {
    $volverParams['materia_id'] = $materiaId;
}
$volverUrl = '/users/profesor/libro_temas.php';
if (!empty($volverParams)) {
    $volverUrl .= '?' . http_build_query($volverParams);
}

$caracterActual = $contenido['caracter_clase'] ?? 'Explicativa';
$temaActual = $contenido['tema'] ?? '';
$actividadesActuales = $contenido['actividades_desarrolladas'] ?? '';
$observacionesActuales = $contenido['observaciones'] ?? '';
$fechaActual = $contenido['fecha_clase'] ?? date('Y-m-d');

$caracteresClase = [
    'Analítica',
    'Consultiva',
    'Dialógica',
    'Elaborativa',
    'Evaluativa',
    'Explicativa',
    'Interpretativa',
    'Introductoria',
    'Investigativa',
    'Motivadora',
    'Participativa',
    'Práctica',
    'Reflexiva',
    'Teórica',
    'Otra...'
];
?>
<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <title>Editar Contenido - Libro de Temas</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
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
    width: 15rem; /* ~ w-60 */
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

    <nav id="sidebar" class="w-60 transition-all duration-300 bg-white shadow-lg border-r">
        <div class="scroll-area px-4 py-4 flex flex-col gap-2">
            <div class="flex justify-center items-center p-2 mb-4 border-b border-gray-400 h-28">
                <img src="../../images/et20ico.ico" class="h-full w-auto object-contain">
            </div>
            <!-- Bloque usuario/rol/salir ELIMINADO DEL SIDEBAR -->
            <a href="profesor.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
                <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
            </a>
            <a href="libro_temas.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Libro de Temas">
                <span class="text-xl">📚</span><span class="sidebar-label">Libro de Temas</span>
            </a>
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Calificaciones">
                <span class="text-xl">📝</span><span class="sidebar-label">Calificaciones</span>
            </a>
            <a href="asistencias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Asistencias P/ Materia">
                <span class="text-xl">👋</span><span class="sidebar-label">Asistencias P/ Materia</span>
            </a>
            <a href="trabajos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Calificaciones">
                <span class="text-xl">📎</span><span class="sidebar-label">TPs y Actividades</span>
            </a>
            <a href="notificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Panel de Notificaciones">
                <span class="text-xl">🔔</span><span class="sidebar-label">Panel de Notificaciones</span>
            </a>
            <button onclick="window.location='../../includes/logout.php'" class="sidebar-item flex items-center justify-center gap-2 mt-auto py-2 px-3 rounded-xl text-white bg-red-500 hover:bg-red-600">
                <span class="text-xl">🚪</span><span class="sidebar-label">Salir</span>
            </button>
        </div>
    </nav>

    <!-- Contenido principal -->
    <main id="content" class="flex-1 p-10">
        <!-- BLOQUE DE USUARIO, ROL, CONFIGURACIÓN Y NOTIFICACIONES -->
        <div class="w-full flex justify-end mb-6">
            <div class="flex items-center gap-3 bg-white rounded-xl px-5 py-2 shadow border">

                <!-- Avatar -->
                <img src="<?php echo $usuario['foto_url'] ?? 'https://ui-avatars.com/api/?name=' . $usuario['nombre']; ?>"
                    class="rounded-full w-12 h-12 object-cover">

                <!-- Nombre y rol -->
                <div class="flex flex-col pr-2 text-right">
                    <div class="font-bold text-base leading-tight"><?php echo $usuario['nombre']; ?></div>
                    <div class="font-bold text-base leading-tight"><?php echo $usuario['apellido']; ?></div>
                    <div class="mt-1 text-xs text-gray-500">Profesor/a</div>
                </div>

                <!-- Selector de rol (si corresponde) -->
                <?php if (isset($_SESSION['roles_disponibles']) && count($_SESSION['roles_disponibles']) > 1): ?>
                    <form method="post" action="/includes/cambiar_rol.php" class="ml-4">
                        <input type="hidden" name="csrf" value="<?= $csrf ?>">
                        <select name="rol" onchange="this.form.submit()"
                            class="px-2 py-1 border text-sm rounded-xl text-gray-700 bg-white">
                            <?php foreach ($_SESSION['roles_disponibles'] as $r): ?>
                                <option value="<?php echo $r['id']; ?>"
                                    <?php if ($_SESSION['usuario']['rol'] == $r['id']) echo 'selected'; ?>>
                                    Cambiar a: <?php echo ucfirst($r['nombre']); ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </form>
                <?php endif; ?>

                <!-- Botón de Configuración -->
                <a href="configuracion.php"
                    class="relative focus:outline-none group ml-2">
                    <i class="fa-solid fa-gear text-2xl text-gray-500 group-hover:text-gray-700 transition-colors"></i>
                </a>

                <!-- Notificaciones -->
                <button id="btn-notificaciones" class="relative focus:outline-none group ml-2">
                    <i id="icono-campana" class="fa-regular fa-bell text-2xl text-gray-400 group-hover:text-gray-700 transition-colors"></i>
                    <span id="badge-notificaciones"
                        class="absolute -top-1 -right-1 bg-red-600 text-white text-xs rounded-full px-1 hidden border border-white font-bold"
                        style="min-width:1.2em; text-align:center;"></span>
                </button>
            </div>
        </div>

        <!-- POPUP DE NOTIFICACIONES -->
        <div id="popup-notificaciones" class="hidden fixed right-4 top-16 w-80 max-h-[70vh] bg-white shadow-2xl rounded-2xl border border-gray-200 z-50 flex flex-col">
            <div class="flex items-center justify-between px-4 py-3 border-b">
                <span class="font-bold text-gray-800 text-lg">Notificaciones</span>
                <button onclick="cerrarPopup()" class="text-gray-400 hover:text-red-400 text-xl">&times;</button>
            </div>
            <div id="lista-notificaciones" class="overflow-y-auto p-2">
                <!-- Notificaciones aquí -->
            </div>
        </div>

        <div class="max-w-xl mx-auto bg-white shadow-xl rounded-xl p-6">
            <h1 class="text-xl font-bold mb-4">✏️ Editar Contenido</h1>
            <form method="post" action="guardar_edicion_contenido.php" class="flex flex-col gap-4">
                <input type="hidden" name="id" value="<?= $contenido['id'] ?>">
                <input type="hidden" name="csrf" value="<?= $_SESSION['csrf'] ?>">
                <label>Fecha:
                    <input type="date" name="fecha_clase" value="<?= htmlspecialchars($fechaActual) ?>" class="w-full border px-4 py-2 rounded-xl" required>
                </label>
                <label>Carácter de clase:
                    <select name="caracter_clase" class="w-full border px-4 py-2 rounded-xl">
                        <?php foreach ($caracteresClase as $caracter): ?>
                            <option value="<?= $caracter ?>" <?php if ($caracter === $caracterActual) echo 'selected'; ?>><?= $caracter ?></option>
                        <?php endforeach; ?>
                    </select>
                </label>
                <label>Tema:
                    <textarea name="tema" rows="2" required class="w-full border px-4 py-2 rounded-xl"><?= htmlspecialchars($temaActual) ?></textarea>
                </label>
                <label>Actividades desarrolladas:
                    <textarea name="actividades" rows="3" class="w-full border px-4 py-2 rounded-xl"><?= htmlspecialchars($actividadesActuales) ?></textarea>
                </label>
                <label>Observaciones:
                    <input name="observaciones" value="<?= htmlspecialchars($observacionesActuales) ?>" class="w-full border px-4 py-2 rounded-xl">
                </label>
                <div class="flex justify-between items-center">
                    <a href="<?= $volverUrl ?>" class="text-gray-600 hover:underline">← Volver</a>
                    <button class="bg-blue-600 text-white px-4 py-2 rounded-xl font-bold hover:bg-blue-700" type="submit">Guardar Cambios</button>
                </div>
            </form>
        </div>
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
