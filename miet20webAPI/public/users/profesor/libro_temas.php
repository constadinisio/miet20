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
$materia_id = isset($_GET['materia_id']) ? (int)$_GET['materia_id'] : 0;

$cargo_id = 0;

if ($curso_id && $materia_id) {
    $sql = "SELECT cmc.cargo_materia_id AS cargo_id
            FROM cargo_materia_curso cmc
            JOIN cargo_materias cm ON cmc.cargo_materia_id = cm.id
            JOIN cargos c ON cm.cargo_id = c.id
            WHERE cmc.curso_id = ?
              AND cm.materia_id = ?
              AND c.docente_id = ?
            LIMIT 1";

    $stmt = $conexion->prepare($sql);
    $stmt->bind_param("iii", $curso_id, $materia_id, $docente_id);
    $stmt->execute();
    $res = $stmt->get_result();
    if ($row = $res->fetch_assoc()) {
        $cargo_id = (int)$row['cargo_id'];
    }
}


// --- Buscar cursos únicos asignados al docente ---
$cursos = [];
$sql = "SELECT DISTINCT 
            cu.id AS curso_id,
            cu.anio,
            cu.division
        FROM cargos cg
        JOIN cargo_materias cm ON cg.id = cm.cargo_id
        JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
        JOIN cursos cu ON cmc.curso_id = cu.id
        WHERE cg.docente_id = ?
        ORDER BY cu.anio, cu.division";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $docente_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $cursos[] = $row;
}
$stmt->close();

$materias = [];
if ($curso_id) {
    $sql_m = "SELECT DISTINCT 
                  m.id AS materia_id,
                  m.nombre AS materia,
                  cg.id AS cargo_id
              FROM cargos cg
              JOIN cargo_materias cm ON cg.id = cm.cargo_id
              JOIN materias m ON cm.materia_id = m.id
              JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
              WHERE cg.docente_id = ?
                AND cmc.curso_id = ?
              ORDER BY m.nombre";
    $stmt_m = $conexion->prepare($sql_m);
    $stmt_m->bind_param("ii", $docente_id, $curso_id);
    $stmt_m->execute();
    $result_m = $stmt_m->get_result();
    while ($row_m = $result_m->fetch_assoc()) {
        $materias[] = $row_m;
    }
    $stmt_m->close();
}

// Si no viene cargo_id, deducirlo a partir del curso+materia elegidos
if ($curso_id && $materia_id && !$cargo_id && !empty($materias)) {
    foreach ($materias as $m) {
        if ((int)$m['materia_id'] === (int)$materia_id) {
            $cargo_id = $m['cargo_id'];
            break;
        }
    }
}

// --- ALTA DE NUEVO TEMA ---
$mensaje = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['nuevo_tema'])) {
    $fecha = $_POST['fecha'] ?? date('Y-m-d');
    $caracter = $_POST['caracter_clase'] ?? 'Explicativa';
    $tema = trim($_POST['tema'] ?? '');
    $actividades = trim($_POST['actividades'] ?? '');
    $observaciones = trim($_POST['observaciones'] ?? '');
    $curso_id = (int)$_POST['curso_id'];
    $materia_id = (int)$_POST['materia_id'];
    if (!$cargo_id) {
        die("No se pudo determinar el cargo para este profesor.");
    }

    if ($tema && $actividades && $curso_id && $materia_id && $cargo_id) {
        // 1. Buscar o crear libro de temas para este cargo/curso/materia y año actual
        $libro_id = null;
        $sql_libro = "SELECT id 
                      FROM libros_temas 
                      WHERE curso_id=? AND materia_id=? AND cargo_id=? AND anio_lectivo=YEAR(CURDATE())";
        $stmt_libro = $conexion->prepare($sql_libro);
        $stmt_libro->bind_param("iii", $curso_id, $materia_id, $cargo_id);
        $stmt_libro->execute();
        $stmt_libro->bind_result($libro_id_res);
        if ($stmt_libro->fetch()) {
            $libro_id = $libro_id_res;
        }
        $stmt_libro->close();

        if (!$libro_id) {
            $sql_new = "INSERT INTO libros_temas (curso_id, materia_id, cargo_id, anio_lectivo, estado)
                        VALUES (?, ?, ?, YEAR(CURDATE()), 'activo')";
            $stmt_new = $conexion->prepare($sql_new);
            $stmt_new->bind_param("iii", $curso_id, $materia_id, $cargo_id);
            $stmt_new->execute();
            $libro_id = $conexion->insert_id;
            $stmt_new->close();
        }

        // 2. Insertar nuevo tema diario
        $sql_insert = "INSERT INTO temas_diarios
            (libro_id, cargo_id, curso_id, materia_id, fecha_clase, caracter_clase, tema, actividades_desarrolladas, observaciones, fecha_carga)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        $stmt_insert = $conexion->prepare($sql_insert);
        $stmt_insert->bind_param(
            "iiiisssss",
            $libro_id,
            $cargo_id,
            $curso_id,
            $materia_id,
            $fecha,
            $caracter,
            $tema,
            $actividades,
            $observaciones
        );

        if ($stmt_insert->execute()) {
            $mensaje = '<div class="bg-green-100 text-green-700 rounded-xl p-3 mb-4">Tema guardado correctamente.</div>';
        } else {
            $mensaje = '<div class="bg-red-100 text-red-700 rounded-xl p-3 mb-4">Error al guardar: ' . $stmt_insert->error . '</div>';
        }
        $stmt_insert->close();
    } else {
        $mensaje = '<div class="bg-yellow-100 text-yellow-800 rounded-xl p-3 mb-4">Completá tema y actividades.</div>';
    }
}

// --- Mostrar los temas cargados ---
$temas = [];
if ($curso_id && $materia_id && $cargo_id) {
    $sql2 = "SELECT td.id, td.fecha_clase, td.caracter_clase, td.tema, td.actividades_desarrolladas, td.observaciones
             FROM libros_temas lt
             JOIN temas_diarios td ON lt.id = td.libro_id
             WHERE lt.curso_id = ? 
               AND lt.materia_id = ? 
               AND lt.cargo_id = ?
               AND lt.anio_lectivo = YEAR(CURDATE())
             ORDER BY td.fecha_clase DESC";
    $stmt2 = $conexion->prepare($sql2);
    $stmt2->bind_param("iii", $curso_id, $materia_id, $cargo_id);
    $stmt2->execute();
    $result2 = $stmt2->get_result();
    while ($row2 = $result2->fetch_assoc()) {
        $temas[] = $row2;
    }
    $stmt2->close();
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Libro de Temas</title>
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

        <h1 class="text-2xl font-bold mb-6">📚 Libro de Temas</h1>

        <form class="mb-8 flex gap-4" method="get" id="form-filtros">
            <input type="hidden" name="csrf" value="<?= $csrf ?>">

            <!-- Select curso -->
            <select name="curso_id" class="px-4 py-2 rounded-xl border" required onchange="this.form.submit()">
                <option value="">Seleccionar curso</option>
                <?php foreach ($cursos as $c): ?>
                    <option value="<?= $c['curso_id'] ?>" <?php if ($curso_id == $c['curso_id']) echo "selected"; ?>>
                        <?= $c['anio'] . "°" . $c['division']; ?>
                    </option>
                <?php endforeach; ?>
            </select>

            <!-- Select materia (solo si ya eligió curso) -->
            <select name="materia_id" class="px-4 py-2 rounded-xl border" required>
                <option value="">Seleccionar materia</option>
                <?php foreach ($materias as $m): ?>
                    <option value="<?= $m['materia_id'] ?>" <?php if ($materia_id == $m['materia_id']) echo "selected"; ?>>
                        <?= $m['materia']; ?>
                    </option>
                <?php endforeach; ?>
            </select>

            <button class="px-4 py-2 rounded-xl bg-indigo-600 text-white">Ver</button>
        </form>

        <?= $mensaje ?>

        <?php if ($curso_id && $materia_id): ?>
            <div class="flex flex-col md:flex-row gap-6 mb-10 items-start">
                <!-- FORMULARIO NUEVO TEMA -->
                <form method="post" class="bg-white rounded-xl shadow p-6 flex flex-col gap-3 w-full md:w-1/3">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <input type="hidden" name="curso_id" value="<?= $curso_id ?>">
                    <input type="hidden" name="materia_id" value="<?= $materia_id ?>">
                    <input type="hidden" name="nuevo_tema" value="1">

                    <div>
                        <label class="font-semibold">Fecha:</label>
                        <input type="date" name="fecha" value="<?= date('Y-m-d') ?>" class="px-4 py-2 border rounded-xl w-full" required>
                    </div>
                    <div>
                        <label class="font-semibold">Carácter de clase:</label>
                        <select name="caracter_clase" class="px-4 py-2 border rounded-xl w-full" required>
                            <option value="Analítica">Analítica</option>
                            <option value="Consultiva">Consultiva</option>
                            <option value="Dialógica">Dialógica</option>
                            <option value="Elaborativa">Elaborativa</option>
                            <option value="Evaluativa">Evaluativa</option>
                            <option value="Explicativa">Explicativa</option>
                            <option value="Interpretativa">Interpretativa</option>
                            <option value="Introductoria">Introductoria</option>
                            <option value="Investigativa">Investigativa</option>
                            <option value="Motivadora">Motivadora</option>
                            <option value="Participativa">Participativa</option>
                            <option value="Práctica">Práctica</option>
                            <option value="Reflexiva">Reflexiva</option>
                            <option value="Teórica">Teórica</option>
                            <option value="Otra...">Otra...</option>
                        </select>
                    </div>
                    <div>
                        <label class="font-semibold">Tema:</label>
                        <textarea name="tema" rows="2" class="w-full px-4 py-2 border rounded-xl" required></textarea>
                    </div>
                    <div>
                        <label class="font-semibold">Actividades desarrolladas:</label>
                        <textarea name="actividades" rows="2" class="w-full px-4 py-2 border rounded-xl" required></textarea>
                    </div>
                    <div>
                        <label class="font-semibold">Observaciones:</label>
                        <input name="observaciones" type="text" class="w-full px-4 py-2 border rounded-xl" placeholder="Opcional">
                    </div>
                    <button type="submit" class="mt-2 px-6 py-2 rounded-xl bg-green-600 text-white hover:bg-green-700 font-bold">
                        + Agregar tema
                    </button>
                </form>

                <!-- LISTADO -->
                <div class="w-full md:w-2/3 max-h-[400px] overflow-y-auto rounded-xl shadow border bg-white">
                    <table class="min-w-full text-sm">
                        <thead class="sticky top-0 bg-white shadow z-10">
                            <tr>
                                <th class="py-2 px-4 text-left">Fecha</th>
                                <th class="py-2 px-4 text-left">Carácter</th>
                                <th class="py-2 px-4 text-left">Tema</th>
                                <th class="py-2 px-4 text-left">Actividades</th>
                                <th class="py-2 px-4 text-left">Observaciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($temas as $t): ?>
                                <tr class="border-t">
                                    <td class="py-2 px-4"><?= date("d/m/Y", strtotime($t['fecha_clase'])) ?></td>
                                    <td class="py-2 px-4"><?= htmlspecialchars($t['caracter_clase']) ?></td>
                                    <td class="py-2 px-4"><?= nl2br(htmlspecialchars($t['tema'])) ?></td>
                                    <td class="py-2 px-4"><?= nl2br(htmlspecialchars($t['actividades_desarrolladas'])) ?></td>
                                    <td class="py-2 px-4"><?= nl2br(htmlspecialchars($t['observaciones'])) ?></td>
                                </tr>
                            <?php endforeach; ?>
                            <?php if (empty($temas)): ?>
                                <tr>
                                    <td colspan="5" class="py-4 text-center text-gray-500">No hay temas cargados.</td>
                                </tr>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        <?php else: ?>
            <div class="text-gray-500">Seleccioná un curso y una materia para ver el libro de temas.</div>
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