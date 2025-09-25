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

// --- Variables de filtro ---
$curso_id   = isset($_GET['curso_id']) ? (int)$_GET['curso_id'] : null;
$materia_id = isset($_GET['materia_id']) ? (int)$_GET['materia_id'] : null;
$cargo_id   = isset($_GET['cargo_id']) ? (int)$_GET['cargo_id'] : null;
$periodo    = $_GET['periodo'] ?? '';

// --- Buscar cursos únicos asignados ---
$cursos = [];
$sql = "SELECT DISTINCT cu.id AS curso_id, cu.anio, cu.division
        FROM cargos cg
        JOIN cargo_materias cm ON cg.id = cm.cargo_id
        JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
        JOIN cursos cu ON cmc.curso_id = cu.id
        WHERE cg.docente_id = ?
        ORDER BY cu.anio, cu.division";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $docente_id);
$stmt->execute();
$res = $stmt->get_result();
while ($row = $res->fetch_assoc()) {
    $cursos[] = $row;
}
$stmt->close();

// --- Buscar materias del curso elegido ---
$materias_del_curso = [];
if ($curso_id) {
    $sql_m = "SELECT DISTINCT m.id AS materia_id, m.nombre AS materia, cg.id AS cargo_id
              FROM cargos cg
              JOIN cargo_materias cm ON cg.id = cm.cargo_id
              JOIN materias m ON cm.materia_id = m.id
              JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
              WHERE cg.docente_id = ? AND cmc.curso_id = ?
              ORDER BY m.nombre";
    $stmt_m = $conexion->prepare($sql_m);
    $stmt_m->bind_param("ii", $docente_id, $curso_id);
    $stmt_m->execute();
    $res_m = $stmt_m->get_result();
    while ($row_m = $res_m->fetch_assoc()) {
        $materias_del_curso[$row_m['materia_id']] = [
            'nombre'   => $row_m['materia'],
            'cargo_id' => $row_m['cargo_id']
        ];
    }
    $stmt_m->close();
}

// --- Resolver cargo_id si falta ---
if ($curso_id && $materia_id && !$cargo_id && isset($materias_del_curso[$materia_id])) {
    $cargo_id = $materias_del_curso[$materia_id]['cargo_id'];
}

// --- Buscar alumnos del curso ---
$alumnos = [];
if ($curso_id) {
    $sql2 = "SELECT u.id, u.nombre, u.apellido
             FROM alumno_curso ac
             JOIN usuarios u ON ac.alumno_id = u.id
             WHERE ac.curso_id = ? AND ac.estado = 'activo' AND u.rol = 4
             ORDER BY u.apellido, u.nombre";
    $stmt2 = $conexion->prepare($sql2);
    $stmt2->bind_param("i", $curso_id);
    $stmt2->execute();
    $res2 = $stmt2->get_result();
    while ($row = $res2->fetch_assoc()) {
        $alumnos[] = $row;
    }
    $stmt2->close();
}

// --- Notas bimestrales/cuatrimestrales ---
$notas = [];
if ($curso_id && $materia_id) {
    $wherePeriodo = '';
    $paramTipos = "ii";
    $params = [$materia_id, $curso_id];

    if ($periodo == '1er Cuatrimestre') {
        $wherePeriodo = " AND (n.periodo = '1er Bimestre' OR n.periodo = '2do Bimestre')";
    } elseif ($periodo == '2do Cuatrimestre') {
        $wherePeriodo = " AND (n.periodo = '3er Bimestre' OR n.periodo = '4to Bimestre')";
    } elseif ($periodo && !in_array($periodo, ['1er Cuatrimestre', '2do Cuatrimestre'])) {
        $wherePeriodo = " AND n.periodo = ?";
        $paramTipos .= "s";
        $params[] = $periodo;
    }

    $sql3 = "SELECT n.id, n.alumno_id, u.nombre, u.apellido, n.nota, n.fecha_carga, n.periodo
             FROM notas_bimestrales n
             JOIN usuarios u ON n.alumno_id = u.id
             WHERE n.materia_id = ? AND n.alumno_id IN
                   (SELECT alumno_id FROM alumno_curso WHERE curso_id = ? AND estado = 'activo')
                   AND u.rol = 4
                   $wherePeriodo
             ORDER BY u.apellido, u.nombre, n.periodo, n.fecha_carga DESC";
    $stmt3 = $conexion->prepare($sql3);

    if ($periodo && !in_array($periodo, ['1er Cuatrimestre', '2do Cuatrimestre'])) {
        $stmt3->bind_param($paramTipos, ...$params);
    } else {
        $stmt3->bind_param($paramTipos, $materia_id, $curso_id);
    }
    $stmt3->execute();
    $res3 = $stmt3->get_result();
    while ($row = $res3->fetch_assoc()) {
        $notas[] = $row;
    }
    $stmt3->close();
}

// --- Trabajos (TP y actividades) de la materia ---
$trabajos = [];
if ($materia_id) {
    $sql_trab = "SELECT id, nombre FROM trabajos WHERE materia_id = ? ORDER BY fecha_creacion ASC";
    $stmt4 = $conexion->prepare($sql_trab);
    $stmt4->bind_param("i", $materia_id);
    $stmt4->execute();
    $res4 = $stmt4->get_result();
    while ($t = $res4->fetch_assoc()) {
        $trabajos[] = $t;
    }
    $stmt4->close();
}

// --- Notas de trabajos ---
$notas_existentes = [];
if (!empty($trabajos) && !empty($alumnos)) {
    $trabajo_ids = implode(",", array_column($trabajos, 'id'));
    $alumno_ids  = implode(",", array_column($alumnos, 'id'));
    if ($trabajo_ids && $alumno_ids) {
        $sql_notas = "SELECT * FROM notas 
                      WHERE trabajo_id IN ($trabajo_ids) AND alumno_id IN ($alumno_ids)";
        $res = $conexion->query($sql_notas);
        if ($res) {
            while ($n = $res->fetch_assoc()) {
                $notas_existentes[$n['alumno_id']][$n['trabajo_id']] = $n['nota'];
            }
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
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Calificaciones">
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

        <?php if (isset($_GET['ok']) && $_GET['ok'] === 'notas_cargadas'): ?>
            <div class="mb-4 px-4 py-3 rounded-xl bg-green-100 border border-green-400 text-green-800">
                ✅ Notas cargadas correctamente.
            </div>
        <?php endif; ?>
        <h1 class="text-2xl font-bold mb-6">📝 Cargar Calificaciones</h1>
        <form class="mb-8 flex gap-4" method="get" id="form-filtros">
            <input type="hidden" name="csrf" value="<?= $csrf ?>">
            <input type="hidden" name="cargo_id" id="cargo_id_field" value="<?= $cargo_id ?? '' ?>">
            <select name="curso_id" class="px-4 py-2 rounded-xl border" required onchange="document.getElementById('form-filtros').submit()">
                <option value="">Seleccionar curso</option>
                <?php foreach ($cursos as $c): ?>
                    <option value="<?php echo $c['curso_id']; ?>" <?php if ($curso_id == $c['curso_id']) echo "selected"; ?>>
                        <?php echo $c['anio'] . "°" . $c['division']; ?>
                    </option>
                <?php endforeach; ?>
            </select>
            <select name="materia_id" id="materia_id" class="px-4 py-2 rounded-xl border" required onchange="document.getElementById('form-filtros').submit()">
                <option value="">Seleccionar materia</option>
                <?php foreach ($materias_del_curso as $id => $info): ?>
                    <option value="<?php echo $id; ?>" data-cargo="<?php echo $info['cargo_id']; ?>" <?php if ((int)$materia_id === (int)$id) echo 'selected'; ?>>
                        <?php echo $info['nombre']; ?>
                    </option>
                <?php endforeach; ?>
            </select>
            <select name="periodo" class="px-4 py-2 rounded-xl border" onchange="document.getElementById('form-filtros').submit()">
                <option value="">Todos los bimestres/cuatrimestres</option>
                <option value="1er Bimestre" <?= $periodo == '1er Bimestre' ? 'selected' : '' ?>>1º Bimestre</option>
                <option value="2do Bimestre" <?= $periodo == '2do Bimestre' ? 'selected' : '' ?>>2º Bimestre</option>
                <option value="1er Cuatrimestre" <?= $periodo == '1er Cuatrimestre' ? 'selected' : '' ?>>1º Cuatrimestre</option>
                <option value="3er Bimestre" <?= $periodo == '3er Bimestre' ? 'selected' : '' ?>>3º Bimestre</option>
                <option value="4to Bimestre" <?= $periodo == '4to Bimestre' ? 'selected' : '' ?>>4º Bimestre</option>
                <option value="2do Cuatrimestre" <?= $periodo == '2do Cuatrimestre' ? 'selected' : '' ?>>2º Cuatrimestre</option>
                <option value="Diciembre" <?= $periodo == 'Diciembre' ? 'selected' : '' ?>>Diciembre</option>
                <option value="Febrero" <?= $periodo == 'Febrero' ? 'selected' : '' ?>>Febrero</option>
            </select>
        </form>
        <script>
            document.getElementById('materia_id').addEventListener('change', function() {
                var sel = this.options[this.selectedIndex];
                document.getElementById('cargo_id_field').value = sel.dataset.cargo || '';
            });
        </script>
        <?php if ($curso_id && $materia_id): ?>
            <!-- Carga de notas bimestrales/cuatrimestrales -->
            <div class="mb-8 bg-white rounded-xl shadow p-6">
                <h2 class="text-lg font-semibold mb-4">➕ Cargar nueva calificación</h2>

                <div class="mb-4 flex items-center gap-3">
                    <label class="font-semibold text-gray-700">Aplicar nota a todos:</label>
                    <?php if (in_array($periodo, ['1er Bimestre', '3er Bimestre'])): ?>
                        <select onchange="aplicarNotaNueva(this.value)" class="border rounded-xl px-3 py-2">
                            <option value="">—</option>
                            <option value="5">En Proceso</option>
                            <option value="7">Suficiente</option>
                            <option value="9">Avanzado</option>
                        </select>
                    <?php else: ?>
                        <select onchange="aplicarNotaNueva(this.value)" class="border rounded-xl px-3 py-2">
                            <option value="">—</option>
                            <?php for ($i = 10; $i >= 1; $i--): ?>
                                <option value="<?= $i ?>"><?= $i ?></option>
                            <?php endfor; ?>
                        </select>
                    <?php endif; ?>
                </div>

                <div class="max-h-[70vh] overflow-y-auto">
                    <form method="post" action="profesor_cargar_nota.php" class="flex flex-col gap-3">
                        <input type="hidden" name="csrf" value="<?= $csrf ?>">
                        <input type="hidden" name="curso_id" value="<?= $curso_id ?>">
                        <input type="hidden" name="materia_id" value="<?= $materia_id ?>">
                        <input type="hidden" name="cargo_id" value="<?= $cargo_id ?>">
                        <input type="hidden" name="periodo" value="<?= htmlspecialchars($periodo) ?>">

                        <?php foreach ($alumnos as $al): ?>
                            <div class="flex items-center gap-4 bg-gray-50 rounded-xl px-4 py-2 shadow-sm">
                                <label class="w-60 font-medium text-gray-700">
                                    <?php echo htmlspecialchars($al['apellido'] . ", " . $al['nombre']); ?>
                                </label>
                                <input type="hidden" name="alumno_id[]" value="<?= $al['id'] ?>">

                                <?php if (in_array($periodo, ['1er Bimestre', '3er Bimestre'])): ?>
                                    <select name="nota[]" class="nota-nueva border rounded-xl px-3 py-2 w-48">
                                        <option value="5">En Proceso</option>
                                        <option value="7">Suficiente</option>
                                        <option value="9">Avanzado</option>
                                    </select>
                                <?php else: ?>
                                    <input type="number" name="nota[]" min="1" max="10" step="0.01"
                                        placeholder="Nota" class="nota-nueva border rounded-xl px-3 py-2 w-24">
                                <?php endif; ?>
                            </div>
                        <?php endforeach; ?>

                        <div class="flex justify-end pt-4">
                            <button type="submit" class="bg-green-600 text-white px-5 py-2 rounded-xl hover:bg-green-700">
                                Guardar notas
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="overflow-y-auto max-h-[70vh] rounded-xl shadow">
                <form method="post" action="profesor_editar_nota.php">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <input type="hidden" name="curso_id" value="<?= $curso_id ?>">
                    <input type="hidden" name="materia_id" value="<?= $materia_id ?>">
                    <input type="hidden" name="periodo" value="<?= htmlspecialchars($periodo) ?>">

                    <table class="min-w-full bg-white rounded-xl shadow">
                        <thead>
                            <tr>
                                <th class="py-2 px-4 text-left">Alumno</th>
                                <th class="py-2 px-4 text-left">Nota</th>
                                <th class="py-2 px-4 text-left">Bimestre</th>
                                <th class="py-2 px-4 text-left">Cuatrimestre</th>
                                <th class="py-2 px-4 text-left">Desempeño</th>
                                <th class="py-2 px-4 text-left">Fecha</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($notas as $n): ?>
                                <tr>
                                    <td class="py-2 px-4"><?= $n['apellido'] . " " . $n['nombre'] ?></td>
                                    <td class="py-2 px-4">
                                        <?php if (in_array($n['periodo'], ['1er Bimestre', '3er Bimestre'])): ?>
                                            <select name="notas[<?= $n['id'] ?>]" class="border rounded px-2 py-1 w-40">
                                                <option value="5" <?= $n['nota'] == 5 ? 'selected' : '' ?>>En Proceso</option>
                                                <option value="7" <?= $n['nota'] == 7 ? 'selected' : '' ?>>Suficiente</option>
                                                <option value="9" <?= $n['nota'] == 9 ? 'selected' : '' ?>>Avanzado</option>
                                            </select>
                                        <?php else: ?>
                                            <input type="number" step="0.01" min="1" max="10"
                                                name="notas[<?= $n['id'] ?>]" value="<?= $n['nota'] ?>"
                                                class="border rounded px-2 py-1 w-20">
                                        <?php endif; ?>
                                    </td>
                                    <td class="py-2 px-4"><?= $n['periodo'] ?></td>
                                    <td class="py-2 px-4">
                                        <?php
                                        if (in_array($n['periodo'], ['1er Bimestre', '2do Bimestre'])) echo '1º Cuatrimestre';
                                        elseif (in_array($n['periodo'], ['3er Bimestre', '4to Bimestre'])) echo '2º Cuatrimestre';
                                        elseif (in_array($n['periodo'], ['Diciembre', 'Febrero'])) echo 'Llamado Extraordinario';
                                        else echo '-';
                                        ?>
                                    </td>
                                    <td class="py-2 px-4">
                                        <?php
                                        $nota = (float)$n['nota'];
                                        if ($nota >= 1 && $nota < 6) {
                                            echo '<span class="text-red-600 font-bold">En Proceso</span>';
                                        } elseif ($nota >= 6 && $nota < 8) {
                                            echo '<span class="text-yellow-700 font-bold">Suficiente</span>';
                                        } elseif ($nota >= 8 && $nota <= 10) {
                                            echo '<span class="text-green-700 font-bold">Avanzado</span>';
                                        } else {
                                            echo '-';
                                        }
                                        ?>
                                    </td>
                                    <td class="py-2 px-4"><?= date("d/m/Y", strtotime($n['fecha_carga'])) ?></td>
                                </tr>
                            <?php endforeach; ?>
                            <?php if (empty($notas)): ?>
                                <tr>
                                    <td colspan="6" class="py-4 text-center text-gray-500">No hay notas cargadas aún.</td>
                                </tr>
                            <?php endif; ?>
                        </tbody>
                    </table>
                    <?php if (!empty($notas)): ?>
                        <div class="flex justify-end pt-4">
                            <button type="submit" class="bg-blue-600 text-white px-5 py-2 rounded-xl hover:bg-blue-700">
                                💾 Guardar Cambios
                            </button>
                        </div>
                    <?php endif; ?>
                </form>
            </div>
        <?php else: ?>
            <div class="text-gray-500">Seleccioná curso y materia para continuar.</div>
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
    <script>
        document.querySelectorAll('.nota-input').forEach(input => {
            input.addEventListener('change', function() {
                const alumnoId = this.dataset.alumno;
                const trabajoId = this.dataset.trabajo;
                const nota = this.value;

                fetch('guardar_nota_trabajo.php', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        body: `csrf=<?= $csrf ?>&alumno_id=${alumnoId}&trabajo_id=${trabajoId}&nota=${nota}`
                    })
                    .then(res => res.json())
                    .then(data => {
                        if (data.ok) {
                            this.classList.remove('border-red-500');
                            this.classList.add('border-green-500');
                        } else {
                            this.classList.remove('border-green-500');
                            this.classList.add('border-red-500');
                            alert(data.error || 'Error al guardar');
                        }
                    });
            });
        });
    </script>
    <script>
        function aplicarNotaNueva(valor) {
            document.querySelectorAll('.nota-nueva').forEach(input => {
                if (input.tagName === 'SELECT') {
                    input.value = valor;
                } else if (input.tagName === 'INPUT') {
                    input.value = valor;
                }
            });
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