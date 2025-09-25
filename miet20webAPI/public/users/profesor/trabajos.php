<?php
session_start();
if (
    !isset($_SESSION['usuario']) ||
    !is_array($_SESSION['usuario']) ||
    (int)$_SESSION['usuario']['rol'] !== 3
) {
    header("Location: /login.php?error=rol");
    exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

$usuario = $_SESSION['usuario'];
$docente_id = $usuario['id'];
require_once __DIR__ . '/../../../backend/includes/db.php';

$curso_id   = isset($_GET['curso_id']) ? (int)$_GET['curso_id'] : 0;
$materia_id = isset($_GET['materia_id']) ? (int)$_GET['materia_id'] : null;
$cargo_id   = isset($_GET['cargo_id']) ? (int)$_GET['cargo_id'] : null;

// Cursos + materias asignadas
$cursosMaterias = [];
$sql = "SELECT cg.id AS cargo_id, cu.id AS curso_id, m.id AS materia_id, cu.anio, cu.division, m.nombre AS materia
        FROM cargos cg
        JOIN cargo_materias cm ON cg.id = cm.cargo_id
        JOIN materias m ON cm.materia_id = m.id
        JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
        JOIN cursos cu ON cmc.curso_id = cu.id
        WHERE cg.docente_id = ?
        ORDER BY cu.anio, cu.division, m.nombre";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $docente_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $cursosMaterias[] = $row;
}
$stmt->close();

// Agrupar materias por curso
$cursosUnicos = [];
foreach ($cursosMaterias as $cm) {
    $cid = (int)$cm['curso_id'];
    if (!isset($cursosUnicos[$cid])) {
        $cursosUnicos[$cid] = [
            'curso_id' => $cid,
            'curso_nombre' => $cm['anio'] . "°" . $cm['division'],
            'materias' => []
        ];
    }
    $cursosUnicos[$cid]['materias'][] = [
        'materia_id' => (int)$cm['materia_id'],
        'materia_nombre' => $cm['materia'],
        'cargo_id' => (int)$cm['cargo_id']
    ];
}

// Alumnos del curso
$alumnos = [];
if ($curso_id) {
    $stmt = $conexion->prepare("SELECT u.id, u.nombre, u.apellido FROM alumno_curso ac JOIN usuarios u ON ac.alumno_id = u.id WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4 ORDER BY u.apellido, u.nombre");
    $stmt->bind_param("i", $curso_id);
    $stmt->execute();
    $res = $stmt->get_result();
    while ($row = $res->fetch_assoc()) $alumnos[] = $row;
    $stmt->close();
}

// Trabajos de la materia
$trabajos = [];
if ($materia_id) {
    $stmt = $conexion->prepare("SELECT id, nombre, tipo FROM trabajos WHERE materia_id = ? ORDER BY fecha_creacion ASC");
    $stmt->bind_param("i", $materia_id);
    $stmt->execute();
    $res = $stmt->get_result();
    while ($t = $res->fetch_assoc()) $trabajos[] = $t;
    $stmt->close();
}

// Notas por alumno-trabajo
$notas_existentes = [];
if (!empty($trabajos) && !empty($alumnos)) {
    $trabajo_ids = implode(',', array_column($trabajos, 'id'));
    $alumno_ids = implode(',', array_column($alumnos, 'id'));
    $sql = "SELECT * FROM notas WHERE trabajo_id IN ($trabajo_ids) AND alumno_id IN ($alumno_ids)";
    $res = $conexion->query($sql);
    if ($res) {
        while ($n = $res->fetch_assoc()) {
            $notas_existentes[$n['alumno_id']][$n['trabajo_id']] = $n['nota'];
        }
    }
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Cargar Calificaciones</title>
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
                <img src="../../images/et20ico.ico" class="h-full w-auto object-contain">
            </div>
            <!-- Bloque usuario/rol/salir ELIMINADO DEL SIDEBAR -->
            <a href="profesor.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
                <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
            </a>
            <a href="libro_temas.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Libro de Temas">
                <span class="text-xl">📚</span><span class="sidebar-label">Libro de Temas</span>
            </a>
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Calificaciones">
                <span class="text-xl">📝</span><span class="sidebar-label">Calificaciones</span>
            </a>
            <a href="asistencias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Asistencias P/ Materia">
                <span class="text-xl">👋</span><span class="sidebar-label">Asistencias P/ Materia</span>
            </a>
            <a href="trabajos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="TPs y Actividades">
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

        <h1 class="text-2xl font-bold mb-4">🧶 Trabajos y Actividades</h1>

        <form method="get" class="flex gap-4 mb-6" id="form-filtros">
            <input type="hidden" name="csrf" value="<?= $csrf ?>">
            <input type="hidden" name="cargo_id" id="cargo_id_field" value="<?= $cargo_id ?? '' ?>">

            <!-- SELECT CURSO -->
            <select id="curso" name="curso_id" class="border rounded px-3 py-2" required
                onchange="document.getElementById('form-filtros').submit()">
                <option value="">Seleccionar curso</option>
                <?php foreach ($cursosUnicos as $cu): ?>
                    <option value="<?= $cu['curso_id'] ?>" <?= $curso_id == $cu['curso_id'] ? 'selected' : '' ?>>
                        <?= $cu['curso_nombre'] ?>
                    </option>
                <?php endforeach; ?>
            </select>

            <!-- SELECT MATERIA -->
            <select id="materia" name="materia_id" class="border rounded px-3 py-2" required
                onchange="document.getElementById('form-filtros').submit()">
                <option value="">Seleccionar materia</option>
                <?php if ($curso_id && isset($cursosUnicos[$curso_id])): ?>
                    <?php foreach ($cursosUnicos[$curso_id]['materias'] as $m): ?>
                        <option value="<?= $m['materia_id'] ?>"
                            data-cargo="<?= $m['cargo_id'] ?>"
                            <?= $materia_id == $m['materia_id'] ? 'selected' : '' ?>>
                            <?= $m['materia_nombre'] ?>
                        </option>
                    <?php endforeach; ?>
                <?php endif; ?>
            </select>
        </form>
        <script>
            document.getElementById('materia').addEventListener('change', function() {
                var sel = this.options[this.selectedIndex];
                document.getElementById('cargo_id_field').value = sel.dataset.cargo || '';
            });
        </script>

        <?php if ($curso_id && $materia_id): ?>
            <div class="mb-8 bg-white rounded-xl shadow p-6">
                <h2 class="text-lg font-semibold mb-4">📌 Crear nuevo trabajo</h2>
                <form method="post" action="crear_trabajo.php" class="flex flex-wrap gap-4 items-end">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <input type="hidden" name="materia_id" value="<?= $materia_id ?>">
                    <input type="hidden" name="curso_id" value="<?= $curso_id ?>">
                    <input type="hidden" name="cargo_id" value="<?= $cargo_id ?>">

                    <div>
                        <label class="block text-sm font-medium">Nombre</label>
                        <input name="nombre" required class="border rounded px-3 py-2 w-64" placeholder="Ej. TP 1 - Electricidad">
                    </div>

                    <div>
                        <label class="block text-sm font-medium">Tipo</label>
                        <select name="tipo" required class="border rounded px-3 py-2">
                            <option value="tp">Trabajo Práctico</option>
                            <option value="actividad">Actividad</option>
                        </select>
                    </div>

                    <div>
                        <label class="block text-sm font-medium">Fecha entrega</label>
                        <input type="date" name="fecha_entrega" required class="border rounded px-3 py-2">
                    </div>

                    <button class="bg-indigo-600 text-white px-4 py-2 rounded-xl hover:bg-indigo-700">Crear</button>
                </form>
            </div>
        <?php endif; ?>

        <?php if ($curso_id && $materia_id): ?>
            <div class="overflow-y-auto max-h-[70vh] rounded-xl shadow border bg-white">
                <table class="min-w-full bg-white rounded-xl shadow">
                    <thead>
                        <tr>
                            <th class="p-3 text-left">Alumno</th>
                            <th class="p-3 text-center">Promedio</th>
                            <th class="p-3 text-center">Sugerencia</th>
                            <th class="p-3 text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($alumnos as $a): ?>
                            <?php
                            $suma = 0;
                            $cant = 0;
                            foreach ($trabajos as $t) {
                                $nota = $notas_existentes[$a['id']][$t['id']] ?? null;
                                if (is_numeric($nota)) {
                                    $suma += $nota;
                                    $cant++;
                                }
                            }
                            $prom = $cant > 0 ? round($suma / $cant, 2) : '-';
                            $sug = is_numeric($prom) ? round($prom) : '-';
                            ?>
                            <tr>
                                <td class="p-2 font-semibold"><?= $a['apellido'] . ', ' . $a['nombre'] ?></td>
                                <td class="p-2 text-center bg-indigo-50"><?= $prom ?></td>
                                <td class="p-2 text-center bg-indigo-50"><?= $sug ?></td>
                                <td class="p-2 text-center">
                                    <button onclick="abrirModal(<?= $a['id'] ?>, '<?= $a['apellido'] . ', ' . $a['nombre'] ?>')"
                                        class="text-indigo-600 hover:underline">📝 Ver trabajos</button>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>

            <!-- Modal -->
            <div id="modal-trabajos" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[80vh] overflow-y-auto relative p-6">
                    <button onclick="cerrarModal()" class="absolute top-2 right-4 text-2xl text-gray-500 hover:text-red-600 font-bold">&times;</button>
                    <h2 class="text-xl font-bold mb-4">📝 Notas de <span id="modal-nombre-alumno"></span></h2>
                    <form id="form-modal-notas" class="space-y-4">
                        <input type="hidden" name="materia_id" value="<?= $materia_id ?>">
                        <input type="hidden" name="csrf" value="<?= $csrf ?>">
                        <input type="hidden" name="alumno_id" id="modal-alumno-id">
                        <div id="modal-campos-trabajos"></div>
                        <div class="text-right pt-4 border-t">
                            <button type="submit" class="bg-green-600 text-white px-4 py-2 rounded-xl hover:bg-green-700">Guardar notas</button>
                        </div>
                    </form>
                </div>
            </div>

            <script>
                const cursoSel = document.getElementById('curso');
                const materiaSel = document.getElementById('materia');
                const cargoField = document.getElementById('cargo_id_field');

                // cuando cambia curso → enviar form
                cursoSel.addEventListener('change', () => {
                    document.getElementById('form-filtros').submit();
                });

                // cuando cambia materia → setea cargo_id y envía form
                materiaSel.addEventListener('change', function() {
                    const sel = this.options[this.selectedIndex];
                    cargoField.value = sel.dataset.cargo || '';
                    document.getElementById('form-filtros').submit();
                });

                // inicializar cargo_id si ya hay materia seleccionada
                if (materiaSel.value) {
                    const sel = materiaSel.options[materiaSel.selectedIndex];
                    cargoField.value = sel.dataset.cargo || '';
                }
            </script>

            <script>
                const trabajos = <?= json_encode($trabajos) ?>;
                const notasPorAlumno = <?= json_encode($notas_existentes) ?>;

                function abrirModal(alumnoId, nombreCompleto) {
                    document.getElementById('modal-trabajos').classList.remove('hidden');
                    document.getElementById('modal-alumno-id').value = alumnoId;
                    document.getElementById('modal-nombre-alumno').textContent = nombreCompleto;

                    const contenedor = document.getElementById('modal-campos-trabajos');
                    contenedor.innerHTML = '';

                    trabajos.forEach(t => {
                        const valor = (notasPorAlumno[alumnoId] && notasPorAlumno[alumnoId][t.id]) || '';
                        const tipoTraducido = t.tipo === 'tp' ? 'Trabajo Práctico' : (t.tipo === 'actividad' ? 'Actividad' : 'Otro');
                        contenedor.innerHTML += `
                                                <div class="flex items-center justify-between border-b py-2">
                                                    <label class="w-2/3">${t.nombre} <span class="text-sm text-gray-500">(${tipoTraducido})</span></label>
                                                    <input type="number" name="nota[${t.id}]" value="${valor}" class="border rounded px-3 py-1 w-24 text-center" min="1" max="10" step="0.01">
                                                </div>
                                                `;
                    });
                }

                function cerrarModal() {
                    document.getElementById('modal-trabajos').classList.add('hidden');
                }

                // Guardado por AJAX
                document.getElementById('form-modal-notas').addEventListener('submit', function(e) {
                    e.preventDefault();
                    const form = new FormData(this);
                    fetch('guardar_nota_trabajo.php', {
                            method: 'POST',
                            body: form
                        })
                        .then(res => res.json())
                        .then(data => {
                            if (data.ok) {
                                alert('✅ Notas guardadas');
                                cerrarModal();
                                location.reload(); // o recargar dinámicamente si querés
                            } else {
                                alert('❌ Error: ' + (data.error || 'al guardar'));
                            }
                        });
                });
            </script>
        <?php endif; ?>
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