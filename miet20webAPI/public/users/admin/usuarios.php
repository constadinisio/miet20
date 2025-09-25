<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}
$usuario = $_SESSION['usuario'];
require_once __DIR__ . '/../../../backend/includes/api_client.php';

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

$usuarios_pendientes = [];
$usuarios_activos = [];
$apiErrors = [];

$normalizeUser = static function ($user) {
    if (!is_array($user)) {
        return [];
    }

    return [
        'id' => isset($user['id']) ? (int) $user['id'] : null,
        'nombre' => isset($user['nombre']) ? (string) $user['nombre'] : '',
        'apellido' => isset($user['apellido']) ? (string) $user['apellido'] : '',
        'mail' => isset($user['mail']) ? (string) $user['mail'] : '',
        'rol' => isset($user['rol']) ? (int) $user['rol'] : null,
        'permNoticia' => !empty($user['permNoticia']) ? 1 : 0,
        'permSubidaArch' => !empty($user['permSubidaArch']) ? 1 : 0,
    ];
};

try {
    $pendingResponse = miEt20ApiAuthenticatedRequest('GET', '/users', null, [
        'query' => [
            'status' => 0,
            'rol' => 0,
        ],
    ]);

    $rawPendings = isset($pendingResponse['data']) && is_array($pendingResponse['data'])
        ? $pendingResponse['data']
        : [];
    $usuarios_pendientes = array_map($normalizeUser, array_filter($rawPendings, 'is_array'));
} catch (RuntimeException $exception) {
    $apiErrors['pendientes'] = $exception->getMessage() ?: 'No se pudo cargar la lista de usuarios pendientes.';
}

try {
    $activeResponse = miEt20ApiAuthenticatedRequest('GET', '/users', null, [
        'query' => [
            'status' => 1,
            'roleIn' => '1,2,3,5',
        ],
    ]);

    $rawActives = isset($activeResponse['data']) && is_array($activeResponse['data'])
        ? $activeResponse['data']
        : [];
    $usuarios_activos = array_map($normalizeUser, array_filter($rawActives, 'is_array'));
    $usuarios_activos = array_values(array_filter($usuarios_activos, static function ($user) {
        return in_array((int) ($user['rol'] ?? 0), [1, 2, 3, 5], true);
    }));
} catch (RuntimeException $exception) {
    $apiErrors['activos'] = $exception->getMessage() ?: 'No se pudo cargar la lista de usuarios activos.';
}

$accionError = $_SESSION['admin_usuarios_error'] ?? null;
unset($_SESSION['admin_usuarios_error']);

$accionOk = $_GET['ok'] ?? null;
$mensajeExito = null;
switch ($accionOk) {
    case 'aprobado':
        $mensajeExito = 'El usuario fue aprobado correctamente.';
        break;
    case 'rechazado':
        $mensajeExito = 'El usuario fue rechazado y eliminado.';
        break;
    case 'actualizado':
    case '1':
        $mensajeExito = 'Los cambios se guardaron correctamente.';
        break;
}

$queryError = $_GET['error'] ?? null;
$mensajeError = null;
switch ($queryError) {
    case 'csrf':
        $mensajeError = 'La sesión expiró. Volvé a intentarlo.';
        break;
    case 'datos_invalidos':
        $mensajeError = 'Los datos enviados no son válidos.';
        break;
    case 'usuario_no_encontrado':
        $mensajeError = 'El usuario indicado no existe.';
        break;
    case 'api':
        $mensajeError = 'La API rechazó la operación. Intentalo nuevamente.';
        break;
}

$errorMessages = [];
if ($mensajeError) {
    $errorMessages[] = $mensajeError;
}
if ($accionError && !in_array($accionError, $errorMessages, true)) {
    $errorMessages[] = $accionError;
}

?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Gestión de Usuarios</title>
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
    <!-- Contenido principal -->
    <main id="content" class="flex-1 p-10">
        <?php if ($mensajeExito): ?>
            <div class="mb-6 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-green-800">
                <?= htmlspecialchars($mensajeExito) ?>
            </div>
        <?php endif; ?>

        <?php if (!empty($errorMessages)): ?>
            <div class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-red-800">
                <?php foreach ($errorMessages as $message): ?>
                    <p><?= htmlspecialchars($message) ?></p>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>

        <?php if (!empty($apiErrors['pendientes']) || !empty($apiErrors['activos'])): ?>
            <div class="mb-6 rounded-xl border border-yellow-200 bg-yellow-50 px-4 py-3 text-yellow-800">
                <?php if (!empty($apiErrors['pendientes'])): ?>
                    <p>⚠️ <?= htmlspecialchars($apiErrors['pendientes']) ?></p>
                <?php endif; ?>
                <?php if (!empty($apiErrors['activos'])): ?>
                    <p>⚠️ <?= htmlspecialchars($apiErrors['activos']) ?></p>
                <?php endif; ?>
            </div>
        <?php endif; ?>

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
                    <div class="mt-1 text-xs text-gray-500">Administrador</div>
                </div>

                <!-- Selector de rol (si corresponde) -->
                <?php if (isset($_SESSION['roles_disponibles']) && count($_SESSION['roles_disponibles']) > 1): ?>
                    <form method="post" action="/includes/cambiar_rol.php" class="ml-4">
                        <input type="hidden" name="csrf" value="<?= htmlspecialchars($csrf) ?>">
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
        <h1 class="text-2xl font-bold mb-6">👥 Gestión de Usuarios Pendientes</h1>
        <div class="overflow-x-auto">
            <div class="max-h-[400px] overflow-y-auto rounded-xl shadow">
                <table class="min-w-full bg-white rounded-xl shadow">
                    <thead>
                        <tr>
                            <th class="py-2 px-4 text-left">Nombre</th>
                            <th class="py-2 px-4 text-left">Apellido</th>
                            <th class="py-2 px-4 text-left">Mail</th>
                            <th class="py-2 px-4 text-left">Rol</th>
                            <th class="py-2 px-4 text-left">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($usuarios_pendientes as $u): ?>
                            <tr>
                                <td class="py-2 px-4"><?= htmlspecialchars($u['nombre']) ?></td>
                                <td class="py-2 px-4"><?= htmlspecialchars($u['apellido']) ?></td>
                                <td class="py-2 px-4"><?= htmlspecialchars($u['mail']) ?></td>
                                <td class="py-2 px-4">
                                    <form method="post" action="admin_usuario_aprobar.php" class="flex items-center gap-2">
                                        <input type="hidden" name="csrf" value="<?= htmlspecialchars($csrf) ?>">
                                        <input type="hidden" name="usuario_id" value="<?php echo $u['id']; ?>">
                                        <select name="rol" class="border rounded px-2 py-1">
                                            <option value="1">Administrador</option>
                                            <option value="2">Preceptor</option>
                                            <option value="3">Profesor</option>
                                            <option value="4">Alumno</option>
                                        </select>
                                        <button type="submit" class="px-2 py-1 bg-green-600 text-white rounded hover:bg-green-700">Aprobar</button>
                                    </form>
                                </td>
                                <td class="py-2 px-4">
                                    <form method="post" action="admin_usuario_rechazar.php" onsubmit="return confirm('¿Estás seguro de rechazar este usuario?');">
                                        <input type="hidden" name="csrf" value="<?= htmlspecialchars($csrf) ?>">
                                        <input type="hidden" name="usuario_id" value="<?php echo $u['id']; ?>">
                                        <button type="submit" class="px-2 py-1 bg-red-500 text-white rounded hover:bg-red-600">Rechazar</button>
                                    </form>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($usuarios_pendientes)): ?>
                            <tr>
                                <td colspan="5" class="py-4 text-center text-gray-500">No hay usuarios pendientes de aprobación.</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
        <h2 class="text-xl font-bold mt-10 mb-4">👤 Usuarios Activos (No alumnos)</h2>
        <div class="overflow-x-auto">
            <div class="max-h-[400px] overflow-y-auto rounded-xl shadow">
                <table class="min-w-full bg-white rounded-xl shadow">
                    <thead>
                        <tr>
                            <th class="py-2 px-4 text-left">Nombre</th>
                            <th class="py-2 px-4 text-left">Apellido</th>
                            <th class="py-2 px-4 text-left">Mail</th>
                            <th class="py-2 px-4 text-left">Rol</th>
                            <th class="py-2 px-4 text-left">Editar</th>
                            <!-- Si querés, sumar Borrar/Desactivar -->
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($usuarios_activos as $u): ?>
                            <tr>
                                <td class="py-2 px-4"><?= htmlspecialchars($u['nombre']) ?></td>
                                <td class="py-2 px-4"><?= htmlspecialchars($u['apellido']) ?></td>
                                <td class="py-2 px-4"><?= htmlspecialchars($u['mail']) ?></td>
                                <td class="py-2 px-4">
                                    <?php
                                    switch ($u['rol']) {
                                        case 1:
                                            echo "Administrador";
                                            break;
                                        case 2:
                                            echo "Preceptor";
                                            break;
                                        case 3:
                                            echo "Profesor";
                                            break;
                                        case 5:
                                            echo "ATTP";
                                            break;
                                        default:
                                            echo "Desconocido";
                                            break;
                                    }
                                    ?>
                                </td>
                                <td class="py-2 px-4">
                                    <a href="editar_usuario.php?id=<?= $u['id'] ?>" class="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">Editar</a>
                                </td>
                                <!--
                                <td class="py-2 px-4">
                                    <form method="post" action="usuario_borrar.php" onsubmit="return confirm('¿Seguro que querés borrar este usuario?');">
                                        <input type="hidden" name="usuario_id" value="<?= $u['id'] ?>">
                                        <button type="submit" class="px-2 py-1 bg-red-500 text-white rounded hover:bg-red-600">Borrar</button>
                                    </form>
                                </td>
                                -->
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($usuarios_activos)): ?>
                            <tr>
                                <td colspan="5" class="py-4 text-center text-gray-500">No hay usuarios activos (Admin, Preceptor, Profesor, ATTP).</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
    <script>
        document.getElementById('btn-notificaciones').addEventListener('click', function() {
            const popup = document.getElementById('popup-notificaciones');
            popup.classList.toggle('hidden');
            cargarNotificaciones();
        });

        function cerrarPopup() {
            document.getElementById('popup-notificaciones').classList.add('hidden');
        }

        function marcarLeida(destinatarioId) {
            fetch('/../../../includes/notificaciones/marcar_leida.php', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: 'id=' + encodeURIComponent(destinatarioId)
                }).then(res => res.json())
                .then(data => {
                    if (data.ok) cargarNotificaciones();
                });
        }

        function confirmar(destinatarioId) {
            fetch('/../../../includes/notificaciones/confirmar.php', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: 'id=' + encodeURIComponent(destinatarioId)
                }).then(res => res.json())
                .then(data => {
                    if (data.ok) cargarNotificaciones();
                });
        }

        function cargarNotificaciones() {
            fetch('/../../../includes/notificaciones/listar.php')
                .then(res => res.json())
                .then(data => {
                    const lista = document.getElementById('lista-notificaciones');
                    const badge = document.getElementById('badge-notificaciones');
                    const campana = document.getElementById('icono-campana');
                    lista.innerHTML = '';
                    let sinLeer = 0;
                    if (data.length === 0) {
                        lista.innerHTML = '<div class="text-center text-gray-400 p-4">Sin notificaciones nuevas.</div>';
                        badge.classList.add('hidden');
                        // Ícono gris claro, sin detalles rojos
                        campana.classList.remove('text-red-500');
                        campana.classList.add('text-gray-400');
                        campana.classList.remove('fa-shake');
                    } else {
                        data.forEach(n => {
                            if (n.estado_lectura === 'NO_LEIDA') sinLeer++;
                            lista.innerHTML += `
                                <div class="rounded-xl px-3 py-2 mb-2 bg-gray-100 shadow hover:bg-gray-50 flex flex-col">
                                <div class="flex items-center gap-2 mb-1">
                                    <span class="text-base font-semibold">${n.titulo}</span>
                                    <span class="ml-auto text-xs">${n.fecha_creacion}</span>
                                </div>
                                <div class="text-sm text-gray-700 mb-2">${n.contenido}</div>
                                <div class="flex gap-2">
                                    ${n.estado_lectura === 'NO_LEIDA' ? `<button class="text-blue-600 text-xs" onclick="marcarLeida(${n.destinatario_row_id})">Marcar como leída</button>` : ''}
                                    ${(n.requiere_confirmacion == 1 && n.estado_lectura !== 'CONFIRMADA') ? `<button class="text-green-600 text-xs" onclick="confirmar(${n.destinatario_row_id})">Confirmar</button>` : ''}
                                    ${n.estado_lectura === 'LEIDA' ? '<span class="text-green-700 text-xs">Leída</span>' : ''}
                                    ${n.estado_lectura === 'CONFIRMADA' ? '<span class="text-green-700 text-xs">Confirmada</span>' : ''}
                                </div>
                                </div>`;
                        });

                        if (sinLeer > 0) {
                            badge.textContent = sinLeer;
                            badge.classList.remove('hidden');
                            // Ícono gris pero con detalle rojo (y/o animación, opcional)
                            campana.classList.remove('text-gray-400');
                            campana.classList.add('text-red-500');
                            campana.classList.add('fa-shake'); // animación de FA, opcional
                        } else {
                            badge.classList.add('hidden');
                            campana.classList.remove('text-red-500');
                            campana.classList.add('text-gray-400');
                            campana.classList.remove('fa-shake');
                        }
                    }
                });
        }
        document.addEventListener('DOMContentLoaded', function() {
            cargarNotificaciones(); // Esto chequea notificaciones ni bien se carga la página
            setInterval(cargarNotificaciones, 15000);
        });
    </script>
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