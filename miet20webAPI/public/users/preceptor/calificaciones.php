<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 2) {
    header("Location: /login.php?error=rol");
    exit;
}
$usuario = $_SESSION['usuario'];
require_once __DIR__ . '/../../../backend/includes/db.php';

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

// Cursos
$cursos = [];
$sql = "SELECT id, anio, division FROM cursos ORDER BY anio, division";
$result = $conexion->query($sql);
while ($row = $result->fetch_assoc()) {
    $cursos[] = $row;
}

$curso_id = isset($_GET['curso_id']) ? (int)$_GET['curso_id'] : null;
$vista = $_GET['ver_por'] ?? '';
$materia_id = isset($_GET['materia_id']) ? (int)$_GET['materia_id'] : null;
$materias = [];
$calificaciones = [];
$alumnos = [];

// Limpiar materia_id si se cambió a vista por alumno
if ($vista === 'alumno') {
    $materia_id = null;
}

if ($curso_id) {
    $sql_materias = "SELECT m.id, m.nombre
                     FROM cargos c
                     JOIN cargos_cursos cc ON c.id = cc.cargo_id
                     JOIN cargos_materias cm ON c.id = cm.cargo_id
                     JOIN materias m ON cm.materia_id = m.id
                     WHERE cc.curso_id = ?
                     GROUP BY m.id, m.nombre
                     ORDER BY m.nombre";
    $stmt = $conexion->prepare($sql_materias);
    $stmt->bind_param("i", $curso_id);
    $stmt->execute();
    $materias = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
    $stmt->close();

    if ($vista === 'alumno') {
        $sql = "SELECT u.id, u.nombre, u.apellido FROM alumno_curso ac JOIN usuarios u ON ac.alumno_id = u.id WHERE ac.curso_id = ? ORDER BY u.apellido, u.nombre";
        $stmt = $conexion->prepare($sql);
        $stmt->bind_param("i", $curso_id);
        $stmt->execute();
        $alumnos = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt->close();
    } elseif ($vista === 'materia' && $materia_id) {
        $sql = "SELECT u.apellido, u.nombre, m.nombre AS materia, n.periodo, n.nota, n.fecha_carga
                FROM alumno_curso ac
                JOIN usuarios u ON ac.alumno_id = u.id
                JOIN notas_bimestrales n ON n.alumno_id = u.id
                JOIN materias m ON n.materia_id = m.id
                WHERE ac.curso_id = ? AND m.id = ?
                ORDER BY u.apellido, u.nombre, n.periodo";
        $stmt = $conexion->prepare($sql);
        $stmt->bind_param("ii", $curso_id, $materia_id);
        $stmt->execute();
        $calificaciones = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt->close();
    }
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Calificaciones | Preceptor</title>
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
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Calificaciones">
                <span class="text-xl">📝</span><span class="sidebar-label">Calificaciones</span>
            </a>
            <a href="boletines.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Boletines">
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

        <h1 class="text-2xl font-bold mb-6">📝 Calificaciones</h1>
        <form class="mb-8 flex gap-4 flex-wrap items-center" method="get" id="form-vista">
            <input type="hidden" name="csrf" value="<?= $csrf ?>">
            <select name="curso_id" class="px-4 py-2 rounded-xl border" onchange="document.getElementById('form-vista').submit()" required>
                <option value="">Seleccionar curso</option>
                <?php foreach ($cursos as $c): ?>
                    <option value="<?= $c['id'] ?>" <?= $curso_id == $c['id'] ? 'selected' : '' ?>><?= $c['anio'] . "°" . $c['division'] ?></option>
                <?php endforeach; ?>
            </select>

            <?php if ($curso_id): ?>
                <select name="ver_por" class="px-4 py-2 rounded-xl border" onchange="document.getElementById('form-vista').submit()" required>
                    <option value="">Seleccionar vista</option>
                    <option value="alumno" <?= $vista === 'alumno' ? 'selected' : '' ?>>Ver por alumno</option>
                    <option value="materia" <?= $vista === 'materia' ? 'selected' : '' ?>>Ver por materia</option>
                </select>
            <?php endif; ?>

            <?php if ($curso_id && $vista === 'materia'): ?>
                <select name="materia_id" class="px-4 py-2 rounded-xl border" onchange="document.getElementById('form-vista').submit()">
                    <option value="">Seleccionar materia</option>
                    <?php foreach ($materias as $m): ?>
                        <option value="<?= $m['id'] ?>" <?= $materia_id == $m['id'] ? 'selected' : '' ?>><?= $m['nombre'] ?></option>
                    <?php endforeach; ?>
                </select>
            <?php endif; ?>
        </form>
        <?php if ($curso_id && $materia_id && $vista): ?>
            <div class="overflow-x-auto">
                <table class="min-w-full bg-white rounded-xl shadow">
                    <thead>
                        <tr>
                            <th class="py-2 px-4 text-left">Alumno</th>
                            <th class="py-2 px-4 text-left">Materia</th>
                            <th class="py-2 px-4 text-left">Bimestre</th>
                            <th class="py-2 px-4 text-left">Cuatrimestre</th>
                            <th class="py-2 px-4 text-left">Nota</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($calificaciones as $cal): ?>
                            <tr>
                                <td class="py-2 px-4"><?= htmlspecialchars($cal['apellido'] . " " . $cal['nombre']) ?></td>
                                <td class="py-2 px-4"><?= htmlspecialchars($cal['materia']) ?></td>
                                <td class="py-2 px-4"><?= htmlspecialchars($cal['periodo']) ?></td>
                                <td class="py-2 px-4">
                                    <?php
                                    if (in_array($cal['periodo'], ['1er Bimestre', '2do Bimestre'])) echo '1º Cuatrimestre';
                                    elseif (in_array($cal['periodo'], ['3er Bimestre', '4to Bimestre'])) echo '2º Cuatrimestre';
                                    elseif (in_array($cal['periodo'], ['Diciembre', 'Febrero'])) echo 'Llamado Extraordinario';
                                    else echo '-';
                                    ?>
                                </td>
                                <td class="py-2 px-4 font-semibold"><?= $cal['nota'] ?></td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($calificaciones)): ?>
                            <tr>
                                <td colspan="7" class="py-4 text-center text-gray-500">No hay calificaciones encontradas.</td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
        <?php if ($curso_id && $vista === 'alumno'): ?>
            <table class="min-w-full bg-white rounded-xl shadow">
                <thead>
                    <tr>
                        <th class="py-2 px-4 text-left">Alumno</th>
                        <th class="py-2 px-4 text-left">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($alumnos as $al): ?>
                        <tr>
                            <td class="py-2 px-4"><?= htmlspecialchars($al['apellido'] . ', ' . $al['nombre']) ?></td>
                            <td class="py-2 px-4">
                                <button onclick="abrirReporte('<?= $al['id'] ?>')" class="bg-blue-600 text-white px-3 py-1 rounded-xl hover:bg-blue-700">
                                    🔎 Ver reporte
                                </button>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        <?php endif; ?>
        <!-- Modal oculto (uno por alumno) -->
        <?php foreach ($alumnos as $alumno): ?>
            <div id="reporte-<?= $alumno['id'] ?>" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl p-6 relative max-h-[90vh] overflow-y-auto">
                    <button onclick="cerrarReporte('<?= $alumno['id'] ?>')" class="absolute top-3 right-4 text-2xl text-gray-400 hover:text-red-500">&times;</button>
                    <h2 class="text-xl font-bold mb-4">📊 Reporte de <?= htmlspecialchars($alumno['apellido'] . ", " . $alumno['nombre']) ?></h2>

                    <!-- Promedios por materia -->
                    <h3 class="font-semibold text-indigo-600 mb-2">Promedio por materia</h3>
                    <ul class="mb-4 space-y-1">
                        <?php foreach ($reporteMaterias[$alumno['id']] ?? [] as $materia => $prom): ?>
                            <li><strong><?= $materia ?>:</strong> <?= number_format($prom, 2) ?></li>
                        <?php endforeach; ?>
                        <?php if (empty($reporteMaterias[$alumno['id']] ?? [])): ?>
                            <li class="text-gray-500">Sin calificaciones aún.</li>
                        <?php endif; ?>
                    </ul>

                    <!-- Promedio global -->
                    <div class="mb-4">
                        <strong class="text-gray-700">Promedio general:</strong>
                        <span class="text-lg text-green-700 font-bold">
                            <?= isset($promediosGlobales[$alumno['id']]) ? number_format($promediosGlobales[$alumno['id']], 2) : '-' ?>
                        </span>
                    </div>

                    <!-- Asistencias -->
                    <h3 class="font-semibold text-indigo-600 mb-2">Resumen de asistencias</h3>
                    <?php $asis = $asistenciasTotales[$alumno['id']] ?? ['P' => 0, 'A' => 0, 'AJ' => 0, 'T' => 0]; ?>
                    <ul class="space-y-1">
                        <li><strong>Presentes:</strong> <?= $asis['P'] ?></li>
                        <li><strong>Ausentes:</strong> <?= $asis['A'] ?></li>
                        <li><strong>Ausentes Justificados:</strong> <?= $asis['AJ'] ?></li>
                        <li><strong>Tardes:</strong> <?= $asis['T'] ?></li>
                    </ul>
                </div>
            </div>
        <?php endforeach; ?>
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
    <script>
        function abrirReporte(id) {
            document.getElementById('reporte-' + id).classList.remove('hidden');
        }

        function cerrarReporte(id) {
            document.getElementById('reporte-' + id).classList.add('hidden');
        }
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