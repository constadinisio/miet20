<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

$usuario = $_SESSION['usuario'];
require_once __DIR__ . '/../../../backend/users/admin/utils/api_client.php';

$operationError = $_SESSION['admin_cursos_error'] ?? null;
unset($_SESSION['admin_cursos_error']);

$cursos = [];
$alumnos = [];
$total = 0;
$curso_id = null;
$pageError = null;

try {
    $courses = admin_call_api('GET', '/cursos');
    if (is_array($courses)) {
        $cursos = array_map(static function ($course) {
            if (!is_array($course)) {
                return [];
            }

            $data = $course;
            $data['id'] = isset($course['id']) ? (int) $course['id'] : null;
            $data['anio'] = isset($course['anio']) ? (int) $course['anio'] : null;
            $data['division'] = isset($course['division']) ? (string) $course['division'] : '';
            $data['turno'] = isset($course['turno']) ? (string) $course['turno'] : '';
            $data['estado'] = isset($course['estado']) ? (string) $course['estado'] : '';

            if (!isset($data['nombre']) || trim((string) $data['nombre']) === '') {
                $nombrePartes = [];
                if ($data['anio']) {
                    $nombrePartes[] = $data['anio'] . '°';
                }
                if ($data['division'] !== '') {
                    $nombrePartes[] = $data['division'];
                }
                $data['nombre'] = trim(implode(' ', $nombrePartes));
            }

            return $data;
        }, $courses);
    }
} catch (RuntimeException $exception) {
    $pageError = 'No se pudieron cargar los cursos: ' . $exception->getMessage();
}

$requestedCourseId = isset($_GET['curso_id']) ? (int) $_GET['curso_id'] : 0;
if ($requestedCourseId > 0) {
    $curso_id = $requestedCourseId;
    try {
        $students = admin_call_api('GET', '/cursos/' . $curso_id . '/alumnos');
        if (is_array($students)) {
            $alumnos = array_map(static function ($student) {
                if (!is_array($student)) {
                    return [];
                }

                $data = $student;
                $data['id'] = isset($student['id']) ? (int) $student['id'] : null;
                $data['nombre'] = isset($student['nombre']) ? (string) $student['nombre'] : '';
                $data['apellido'] = isset($student['apellido']) ? (string) $student['apellido'] : '';

                return $data;
            }, $students);
            $total = count($alumnos);
        }
    } catch (RuntimeException $exception) {
        $pageError = $pageError
            ? $pageError
            : 'No se pudieron cargar los alumnos del curso seleccionado: ' . $exception->getMessage();
        $alumnos = [];
        $total = 0;
    }
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Gestión de Cursos</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
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
            <a href="usuarios.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Usuarios">
                <span class="text-xl">👥</span><span class="sidebar-label">Usuarios</span>
            </a>
            <a href="cursos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Cursos">
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

        <?php if (isset($_GET['ok'])): ?>
            <div class="mb-4 px-4 py-3 rounded-xl bg-green-100 border border-green-400 text-green-800">
                <?php
                switch ($_GET['ok']) {
                    case 'nuevo':
                        echo '✅ Curso creado correctamente.';
                        break;
                    case 'estado_cambiado':
                        echo '🔁 Estado del curso actualizado.';
                        break;
                    case 'alumno_agregado':
                        echo '👥 Alumno agregado al curso.';
                        break;
                    case 'alumno_eliminado':
                        echo '🗑️ Alumno eliminado del curso.';
                        break;
                }
                ?>
            </div>
        <?php elseif (isset($_GET['error'])): ?>
            <div class="mb-4 px-4 py-3 rounded-xl bg-red-100 border border-red-400 text-red-800">
                <?php
                switch ($_GET['error']) {
                    case 'duplicado':
                        echo '⚠️ Ya existe un curso con ese año, división y turno.';
                        break;
                    case 'estado_invalido':
                        echo '❌ Estado no válido para cambiar.';
                        break;
                    case 'faltan_campos':
                        echo '❗ Por favor completá todos los campos.';
                        break;
                    case 'alumno_no_encontrado':
                        echo '❌ No se encontró un alumno con ese DNI.';
                        break;
                    case 'curso_no_encontrado':
                        echo '❌ El curso seleccionado no existe o ya no está disponible.';
                        break;
                    case 'api':
                        echo '⚠️ No se pudo completar la acción porque la API devolvió un error.';
                        if ($operationError) {
                            echo '<br><small>' . htmlspecialchars($operationError, ENT_QUOTES, 'UTF-8') . '</small>';
                        }
                        break;
                    case 'csrf':
                        echo '⚠️ La sesión expiró. Volvé a intentarlo.';
                        break;
                    default:
                        echo '⚠️ No se pudo completar la acción solicitada.';
                        if ($operationError) {
                            echo '<br><small>' . htmlspecialchars($operationError, ENT_QUOTES, 'UTF-8') . '</small>';
                        }
                        break;
                }
                ?>
            </div>
        <?php endif; ?>
        <?php if ($pageError): ?>
            <div class="mb-4 px-4 py-3 rounded-xl bg-yellow-100 border border-yellow-400 text-yellow-800">
                <?php echo htmlspecialchars($pageError, ENT_QUOTES, 'UTF-8'); ?>
            </div>
        <?php endif; ?>
        <h1 class="text-2xl font-bold mb-6">🏫 Gestión de Cursos</h1>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

            <!-- Card: Ver alumnos -->
            <div class="bg-white rounded-xl shadow p-6">
                <h2 class="text-xl font-semibold mb-4">🔍 Ver alumnos por curso</h2>
                <form class="mb-4 flex gap-4" method="get">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <select name="curso_id" class="px-4 py-2 rounded-xl border flex-1" required>
                        <option value="">Seleccionar curso</option>
                        <?php foreach ($cursos as $c): ?>
                            <option value="<?php echo $c['id']; ?>" <?php if ($curso_id == $c['id']) echo "selected"; ?>>
                                <?php echo $c['anio'] . "°" . $c['division'] . " (" . $c['turno'] . ")"; ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                    <button class="px-4 py-2 rounded-xl bg-indigo-600 text-white">Ver</button>
                </form>

                <?php if ($curso_id): ?>
                    <div class="text-sm mb-3 text-gray-700">👥 Total de alumnos: <span class="text-indigo-600 font-semibold"><?php echo $total; ?></span></div>
                    <div class="max-h-[300px] overflow-y-auto border rounded-xl">
                        <table class="w-full text-sm">
                            <thead class="bg-gray-100">
                                <tr>
                                    <th class="text-left px-4 py-2">Nombre</th>
                                    <th class="text-left px-4 py-2">Apellido</th>
                                    <th class="text-left px-4 py-2">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($alumnos as $a): ?>
                                    <tr class="border-b">
                                        <td class="px-4 py-2"><?php echo $a['nombre']; ?></td>
                                        <td class="px-4 py-2"><?php echo $a['apellido']; ?></td>
                                        <td class="px-4 py-2">
                                            <form method="post" action="admin_curso_eliminar_alumno.php" onsubmit="return confirm('¿Eliminar este alumno del curso?');">
                                                <input type="hidden" name="curso_id" value="<?php echo $curso_id; ?>">
                                                <input type="hidden" name="alumno_id" value="<?php echo $a['id']; ?>">
                                                <input type="hidden" name="csrf" value="<?= $csrf ?>">
                                                <button type="submit" class="text-red-600 hover:underline">Eliminar</button>
                                            </form>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                                <?php if (empty($alumnos)): ?>
                                    <tr>
                                        <td colspan="3" class="text-center text-gray-500 py-3">No hay alumnos en este curso.</td>
                                    </tr>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>

                    <form method="post" action="admin_curso_agregar_alumno.php" class="flex gap-2 items-end mt-4">
                        <input type="hidden" name="csrf" value="<?= $csrf ?>">
                        <input type="hidden" name="curso_id" value="<?php echo $curso_id; ?>">
                        <input type="text" name="dni" placeholder="DNI del alumno" class="px-4 py-2 border rounded-xl flex-1" required>
                        <button type="submit" class="px-4 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700">Agregar</button>
                    </form>
                <?php endif; ?>
            </div>

            <!-- Card: Crear y gestionar cursos -->
            <div class="bg-white rounded-xl shadow p-6">
                <h2 class="text-xl font-semibold mb-4">⚙️ Cambiar estado de cursos</h2>
                <h3 class="text-lg font-semibold mb-2">Cambiar estado de cursos</h3>
                <ul class="text-sm max-h-[400px] overflow-y-auto">
                    <?php foreach ($cursos as $c): ?>
                        <li class="flex justify-between items-center border-b py-2">
                            <span><?php echo $c['anio'] . "°" . $c['division'] . " (" . $c['turno'] . ")"; ?></span>
                            <form action="admin_curso_toggle_estado.php" method="post">
                                <input type="hidden" name="csrf" value="<?= $csrf ?>">
                                <input type="hidden" name="curso_id" value="<?php echo $c['id']; ?>">
                                <input type="hidden" name="estado" value="<?php echo $c['estado']; ?>">
                                <button type="submit" class="text-sm px-3 py-1 rounded-xl <?php echo $c['estado'] === 'activo' ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'; ?> text-white">
                                    <?php echo $c['estado'] === 'activo' ? 'Desactivar' : 'Activar'; ?>
                                </button>
                            </form>
                        </li>
                    <?php endforeach; ?>
                </ul>
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