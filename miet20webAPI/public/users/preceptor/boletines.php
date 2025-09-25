<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 2) {
    header("Location: /login.php?error=rol");
    exit;
}
$usuario = $_SESSION['usuario'];
require_once __DIR__ . '/../../../backend/includes/db.php';
require_once __DIR__ . '/../../../backend/users/preceptor/utils/boletin_helpers.php';

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

// Todos los cursos del sistema
$cursos = [];
$sql = "SELECT id, anio, division FROM cursos ORDER BY anio, division";
$result = $conexion->query($sql);
while ($row = $result->fetch_assoc()) {
    $cursos[] = $row;
}

$curso_id = $_GET['curso_id'] ?? null;
$alumnos = [];
if ($curso_id) {
    $sql = "SELECT u.id, u.nombre, u.apellido FROM alumno_curso ac JOIN usuarios u ON ac.alumno_id = u.id WHERE ac.curso_id = ? ORDER BY u.apellido, u.nombre";
    $stmt = $conexion->prepare($sql);
    $stmt->bind_param("i", $curso_id);
    $stmt->execute();
    $result = $stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $alumnos[] = $row;
    }
    $stmt->close();
}

$alumno_id = $_GET['alumno_id'] ?? null;
$boletines = [];
if ($curso_id && $alumno_id) {
    $sql = "SELECT * FROM boletin WHERE curso_id = ? AND alumno_id = ? ORDER BY anio_lectivo DESC, periodo DESC";
    $stmt = $conexion->prepare($sql);
    $stmt->bind_param("ii", $curso_id, $alumno_id);
    $stmt->execute();
    $result = $stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $boletines[] = $row;
    }
    $stmt->close();
}

$faltantes = [];
$periodo_actual = null;
if ($curso_id && $alumno_id) {
    $cursoIdInt = (int)$curso_id;
    $alumnoIdInt = (int)$alumno_id;
    $periodo_actual = obtenerUltimoPeriodoConNotas($conexion, $cursoIdInt, $alumnoIdInt);

    if ($periodo_actual) {
        $faltantes = obtenerMateriasSinNotasPorPeriodo($conexion, $cursoIdInt, $alumnoIdInt, $periodo_actual);
    } else {
        $materiasCurso = obtenerMateriasCurso($conexion, $cursoIdInt);
        if (!empty($materiasCurso)) {
            foreach ($materiasCurso as $materia) {
                $faltantes[] = $materia['nombre'];
            }
        }
    }
}

if (!empty($faltantes)) {
    $faltantes = array_values(array_unique($faltantes));
    sort($faltantes, SORT_NATURAL | SORT_FLAG_CASE);
}

$puede_generar_boletin = $periodo_actual && empty($faltantes);
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Boletines | Preceptor</title>
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
                <img src="/images/et20ico.ico" class="h-full w-auto object-contain">
            </div>
            <a href="preceptor.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
                <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
            </a>
            <a href="asistencias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Asistencias">
                <span class="text-xl">📆</span><span class="sidebar-label">Asistencias</span>
            </a>
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Calificaciones">
                <span class="text-xl">📝</span><span class="sidebar-label">Calificaciones</span>
            </a>
            <a href="boletines.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Boletines">
                <span class="text-xl">📑</span><span class="sidebar-label">Boletines</span>
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
                    <div class="mt-1 text-xs text-gray-500">Preceptor/a</div>
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

        <h1 class="text-2xl font-bold mb-6">📑 Boletines</h1>
        <!-- Selección de curso -->
        <form class="mb-4 flex gap-4" method="get">
            <input type="hidden" name="csrf" value="<?= $csrf ?>">
            <select name="curso_id" class="px-4 py-2 rounded-xl border" required onchange="this.form.submit()">
                <option value="">Seleccionar curso</option>
                <?php foreach ($cursos as $c): ?>
                    <option value="<?php echo $c['id']; ?>" <?php if ($curso_id == $c['id']) echo "selected"; ?>>
                        <?php echo $c['anio'] . "°" . $c['division']; ?>
                    </option>
                <?php endforeach; ?>
            </select>
            <?php if ($curso_id): ?>
                <select name="alumno_id" class="px-4 py-2 rounded-xl border" required onchange="this.form.submit()">
                    <option value="">Seleccionar alumno</option>
                    <?php foreach ($alumnos as $a): ?>
                        <option value="<?php echo $a['id']; ?>" <?php if ($alumno_id == $a['id']) echo "selected"; ?>>
                            <?php echo $a['apellido'] . " " . $a['nombre']; ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            <?php endif; ?>
        </form>
        <?php if ($curso_id && $alumno_id): ?>
            <div class="mb-4 space-y-2">
                <?php if ($puede_generar_boletin): ?>
                    <a href="editar_boletin.php?curso_id=<?php echo $curso_id; ?>&alumno_id=<?php echo $alumno_id; ?>&nuevo=1" class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-xl">+ Nuevo boletín</a>
                <?php else: ?>
                    <button class="bg-gray-400 cursor-not-allowed text-white font-bold py-2 px-4 rounded-xl" disabled>
                        + Nuevo boletín
                    </button>
                    <?php if (!empty($faltantes)): ?>
                        <?php $materiasPendientes = array_map(function ($nombre) { return htmlspecialchars($nombre, ENT_QUOTES, 'UTF-8'); }, $faltantes); ?>
                        <div class="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-700 p-4 rounded-xl">
                            ⚠️
                            <?php if ($periodo_actual): ?>
                                Faltan notas de <?= htmlspecialchars($periodo_actual, ENT_QUOTES, 'UTF-8'); ?> en:
                            <?php else: ?>
                                Todavía no hay notas registradas para este alumno. Materias pendientes:
                            <?php endif; ?>
                            <?= implode(', ', $materiasPendientes); ?>
                        </div>
                    <?php else: ?>
                        <div class="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-700 p-4 rounded-xl">
                            ⚠️ No hay materias configuradas para este curso.
                        </div>
                    <?php endif; ?>
                <?php endif; ?>
            </div>
            <div class="overflow-x-auto">
                <table class="min-w-full bg-white rounded-xl shadow">
                    <thead>
                        <tr>
                            <th class="py-2 px-4">Año lectivo</th>
                            <th class="py-2 px-4">Periodo</th>
                            <th class="py-2 px-4">Estado</th>
                            <th class="py-2 px-4">Emisión</th>
                            <th class="py-2 px-4">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($boletines as $b): ?>
                            <tr>
                                <td class="py-2 px-4"><?php echo $b['anio_lectivo']; ?></td>
                                <td class="py-2 px-4"><?php echo $b['periodo']; ?></td>
                                <td class="py-2 px-4"><?php echo ucfirst($b['estado']); ?></td>
                                <td class="py-2 px-4"><?php echo $b['fecha_emision'] ? date('d/m/Y', strtotime($b['fecha_emision'])) : "-"; ?></td>
                                <td class="py-2 px-4 flex gap-2">
                                    <a href="editar_boletin.php?curso_id=<?php echo $curso_id; ?>&alumno_id=<?php echo $alumno_id; ?>&boletin_id=<?php echo $b['id']; ?>" class="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600">Ver/Editar</a>
                                    <a href="exportar_boletin.php?id=<?php echo $b['id']; ?>" target="_blank" class="bg-gray-700 text-white px-3 py-1 rounded hover:bg-gray-900">PDF</a>
                                    <?php if ($b['estado'] == 'borrador'): ?>
                                        <a href="publicar_boletin.php?id=<?php echo $b['id']; ?>" class="bg-green-700 text-white px-3 py-1 rounded hover:bg-green-800">Publicar</a>
                                    <?php endif; ?>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($boletines)): ?>
                            <tr>
                                <td colspan="5" class="py-4 text-center text-gray-500">No hay boletines para este alumno.</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        <?php elseif ($curso_id): ?>
            <div class="text-gray-500">Seleccioná un alumno para ver o crear boletines.</div>
        <?php endif; ?>
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