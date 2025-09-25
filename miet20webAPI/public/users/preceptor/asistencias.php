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

// Todos los cursos del sistema
$cursos = [];
$sql = "SELECT id, anio, division FROM cursos ORDER BY anio, division";
$result = $conexion->query($sql);
while ($row = $result->fetch_assoc()) {
    $cursos[] = $row;
}

$curso_id = isset($_GET['curso_id']) ? (int)$_GET['curso_id'] : null;
$fecha = $_GET['fecha'] ?? date('Y-m-d');
$modo = $_GET['modo'] ?? 'ver';

// Traer alumnos y asistencias (turno y contraturno)
$alumnos = [];
if ($curso_id) {
    // Listar alumnos del curso
    $sql2 = "SELECT u.id, u.nombre, u.apellido
             FROM alumno_curso ac
             JOIN usuarios u ON ac.alumno_id = u.id
             WHERE ac.curso_id = ?
             ORDER BY u.apellido, u.nombre";
    $stmt2 = $conexion->prepare($sql2);
    $stmt2->bind_param("i", $curso_id);
    $stmt2->execute();
    $result2 = $stmt2->get_result();
    while ($row = $result2->fetch_assoc()) {
        $alumnos[$row['id']] = [
            'id' => $row['id'],
            'nombre' => $row['nombre'],
            'apellido' => $row['apellido'],
            'turno' => '-',         // Por defecto
            'contraturno' => '-',   // Por defecto
        ];
    }
    $stmt2->close();

    // Traer asistencias de ese día (para turno y contraturno)
    $sql3 = "SELECT alumno_id, estado, es_contraturno
             FROM asistencia_general
             WHERE curso_id = ? AND fecha = ?";
    $stmt3 = $conexion->prepare($sql3);
    $stmt3->bind_param("is", $curso_id, $fecha);
    $stmt3->execute();
    $result3 = $stmt3->get_result();
    while ($row = $result3->fetch_assoc()) {
        if (isset($alumnos[$row['alumno_id']])) {
            if ($row['es_contraturno']) {
                $alumnos[$row['alumno_id']]['contraturno'] = $row['estado'];
            } else {
                $alumnos[$row['alumno_id']]['turno'] = $row['estado'];
            }
        }
    }
    $stmt3->close();
}

$dias_es = [
    'Monday' => 'Lunes',
    'Tuesday' => 'Martes',
    'Wednesday' => 'Miércoles',
    'Thursday' => 'Jueves',
    'Friday' => 'Viernes',
    'Saturday' => 'Sábado',
    'Sunday' => 'Domingo',
];

// Guardar cambios (solo si está en modo edición)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['asistencias']) && isset($_POST['curso_id']) && isset($_POST['dias_semana'])) {
    $curso_id = $_POST['curso_id'];
    $preceptor_id = $usuario['id'];
    $dias_semana = $_POST['dias_semana']; // Array de fechas (lunes a viernes)
    $semana_lunes = $_POST['semana_lunes'] ?? '';

    foreach ($_POST['asistencias'] as $alumno_id => $asist_dias) {
        foreach ($dias_semana as $fecha) {
            foreach (['turno' => 0, 'contraturno' => 1] as $tipo => $es_contraturno) {
                $estado = $asist_dias[$fecha][$tipo] ?? '';
                $estado = strtoupper(trim($asist_dias[$fecha][$tipo] ?? ''));
                if ($estado === '' || $estado === 'NC') continue;

                // Buscar si ya existe
                $sql_check = "SELECT id FROM asistencia_general WHERE alumno_id=? AND curso_id=? AND fecha=? AND es_contraturno=?";
                $stmt_check = $conexion->prepare($sql_check);
                if (!$stmt_check) die("Error: " . $conexion->error);
                $stmt_check->bind_param("iisi", $alumno_id, $curso_id, $fecha, $es_contraturno);
                $stmt_check->execute();
                $stmt_check->store_result();

                if ($stmt_check->num_rows > 0) {
                    $stmt_check->bind_result($asist_id);
                    $stmt_check->fetch();
                    $sql_upd = "UPDATE asistencia_general SET estado=? WHERE id=?";
                    $stmt_upd = $conexion->prepare($sql_upd);
                    if (!$stmt_upd) die("Error: " . $conexion->error);
                    $stmt_upd->bind_param("si", $estado, $asist_id);
                    $stmt_upd->execute();
                    $stmt_upd->close();
                } else {
                    $sql_ins = "INSERT INTO asistencia_general (alumno_id, curso_id, fecha, estado, creado_por, es_contraturno)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE estado = VALUES(estado)";
                    $stmt_ins = $conexion->prepare($sql_ins);
                    if (!$stmt_ins) die("Error: " . $conexion->error);
                    $stmt_ins->bind_param("iissii", $alumno_id, $curso_id, $fecha, $estado, $preceptor_id, $es_contraturno);
                    if (!$stmt_ins->execute()) {
                        die("❌ Error al ejecutar: " . $stmt_ins->error);
                    }
                    $stmt_ins->close();
                }
                $stmt_check->close();
            }
        }
    }
    header("Location: asistencias.php?curso_id=$curso_id&semana_lunes=$semana_lunes&modo=editar&msg=ok");
    exit;
}
$msg = $_GET['msg'] ?? '';

// Busca el rango total de fechas con asistencias cargadas para ese curso
$fecha_inicio = date('Y-m-d');
$fecha_fin    = date('Y-m-d');
if ($curso_id) {
    $stmtF = $conexion->prepare("SELECT MIN(fecha) AS inicio, MAX(fecha) AS fin FROM asistencia_general WHERE curso_id = ?");
    $stmtF->bind_param('i', $curso_id);
    $stmtF->execute();
    $fechas = $stmtF->get_result()->fetch_assoc();
    $stmtF->close();
    if (!empty($fechas['inicio'])) $fecha_inicio = $fechas['inicio'];
    if (!empty($fechas['fin']))    $fecha_fin    = $fechas['fin'];
}
$fecha_inicio = $fechas['inicio'] ?? date('Y-m-d');
$fecha_fin = $fechas['fin'] ?? date('Y-m-d');

// Rango escolar típico (ajustá si tu ciclo empieza/termina en otra fecha)
$año_actual = date('Y');
$primer_lunes = new DateTime("$año_actual-03-05");
if ($primer_lunes->format('N') != 1) {
    $primer_lunes->modify('next monday');
}

// El último lunes será el mayor entre hoy (semana actual) y último lunes de diciembre
$hoy = new DateTime();
$lunes_hoy = clone $hoy;
if ($lunes_hoy->format('N') != 1) {
    $lunes_hoy->modify('monday this week');
}
$ultimo_lunes_calendario = new DateTime("$año_actual-12-19");
if ($ultimo_lunes_calendario->format('N') != 1) {
    $ultimo_lunes_calendario->modify('last monday');
}
$ultimo_lunes = ($lunes_hoy > $ultimo_lunes_calendario) ? $lunes_hoy : $ultimo_lunes_calendario;

// Genera todos los lunes del ciclo (de marzo hasta el último lunes definido)
$luneses = [];
$dt = clone $primer_lunes;
while ($dt <= $ultimo_lunes) {
    $luneses[] = $dt->format('Y-m-d');
    $dt->modify('+1 week');
}

// Semana seleccionada: por defecto, la última semana con datos o la actual
$semana_lunes = $_GET['semana_lunes'] ?? (end($luneses) ?: date('Y-m-d'));
if (!in_array($semana_lunes, $luneses)) $semana_lunes = reset($luneses);

// Genera los 5 días hábiles de la semana seleccionada
$dias_semana = [];
$dt = new DateTime($semana_lunes);
for ($i = 0; $i < 5; $i++) {
    $dias_semana[] = $dt->format('Y-m-d');
    $dt->modify('+1 day');
}

// === MAPEO CONTRATURNO POR DÍA DE LA SEMANA PARA ESTE CURSO ===
$dias_contraturno_num = [];
if ($curso_id) {
    $sqlCT = "
        SELECT DISTINCT hm.dia_semana
        FROM horarios_materia hm
        INNER JOIN materias m ON hm.materia_id = m.id
        WHERE hm.curso_id = ?
          AND m.es_contraturno = 1
    ";
    $stmtCT = $conexion->prepare($sqlCT);
    $stmtCT->bind_param('i', $curso_id);
    $stmtCT->execute();
    $resCT = $stmtCT->get_result();

    // Mapear texto a número (1=lunes ... 7=domingo)
    $mapDias = [
        'Lunes' => 1,
        'Martes' => 2,
        'Miércoles' => 3,
        'Miercoles' => 3,   // por si viene sin tilde
        'Jueves' => 4,
        'Viernes' => 5,
        'Sábado' => 6,
        'Sabado' => 6,      // por si viene sin tilde
        'Domingo' => 7
    ];

    while ($r = $resCT->fetch_assoc()) {
        $diaTxt = trim($r['dia_semana']);
        if (isset($mapDias[$diaTxt])) {
            $dias_contraturno_num[] = $mapDias[$diaTxt];
        }
    }
    $stmtCT->close();
}

// Armar array para esta semana
$fecha_tiene_contraturno = []; // ['YYYY-mm-dd' => bool]
foreach ($dias_semana as $f) {
    $dowNum = (int)date('N', strtotime($f)); // 1..7
    $fecha_tiene_contraturno[$f] = in_array($dowNum, $dias_contraturno_num, true);
}

// Utilidad para resaltar el día actual
$hoy_str = date('Y-m-d');
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Asistencias | Preceptor</title>
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
                <img src="/images/et20ico.ico" class="h-full w-auto object-contain">
            </div>
            <a href="preceptor.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
                <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
            </a>
            <a href="asistencias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Asistencias">
                <span class="text-xl">📆</span><span class="sidebar-label">Asistencias</span>
            </a>
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Calificaciones">
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

        <h1 class="text-2xl font-bold mb-6">📆 Gestión de Asistencias</h1>
        <?php if ($msg == 'ok'): ?>
            <div class="bg-green-100 text-green-700 rounded-xl p-3 mb-4">Asistencias guardadas correctamente.</div>
        <?php endif; ?>

        <div class="bg-white p-6 rounded-2xl shadow-xl max-w-7xl mx-auto">
            <form
                class="grid grid-cols-1 md:grid-cols-3 gap-x-16 gap-y-2 mb-4 items-center"
                method="get">
                <input type="hidden" name="csrf" value="<?= $csrf ?>">

                <!-- Curso -->
                <div class="flex flex-col">
                    <label class="font-semibold">Curso:</label>
                    <select id="sel-curso-preceptor" name="curso_id" class="border rounded p-2" required>
                        <option value="">Seleccionar curso</option>
                        <?php foreach ($cursos as $c): ?>
                            <option value="<?php echo $c['id']; ?>" <?php if ($curso_id == $c['id']) echo "selected"; ?>>
                                <?php echo $c['anio'] . "°" . $c['division']; ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <!-- Fecha -->
                <div class="flex flex-col">
                    <label class="font-semibold">Fecha base:</label>
                    <div class="flex items-center gap-2">
                        <input type="date" id="selector-fecha" class="border rounded p-2" required>
                        <input type="hidden" name="semana_lunes" id="input-semana-lunes" value="<?= $semana_lunes ?>">
                        <div id="texto-rango" class="text-gray-600 font-semibold"></div>
                    </div>
                </div>

                <!-- Modo -->
                <div class="flex flex-col">
                    <label class="font-semibold">Modo:</label>
                    <div class="flex items-center gap-2">
                        <select name="modo" class="border rounded p-2">
                            <option value="ver" <?php if ($modo == 'ver') echo 'selected'; ?>>Ver</option>
                            <option value="editar" <?php if ($modo == 'editar') echo 'selected'; ?>>Editar</option>
                        </select>
                        <button class="px-4 py-2 rounded-xl bg-indigo-600 text-white">Ver</button>
                    </div>
                </div>
            </form>
        </div>

        <?php if ($curso_id): ?>
            <?php
            // Trae asistencias de todos los días de la semana seleccionada
            $asist_semana = [];
            if ($curso_id && $dias_semana) {
                $in = "'" . implode("','", $dias_semana) . "'";
                $sql = "SELECT alumno_id, fecha, estado, es_contraturno FROM asistencia_general WHERE curso_id = $curso_id AND fecha IN ($in)";
                $res = $conexion->query($sql);
                while ($row = $res->fetch_assoc()) {
                    $asist_semana[$row['alumno_id']][$row['fecha']][$row['es_contraturno'] ? 'contraturno' : 'turno'] = $row['estado'];
                }
            }
            ?>
            <?php
            // Fecha a resumir: hoy si cae en la semana seleccionada, si no el lunes de esa semana
            $hoy_str = date('Y-m-d');
            $fechaResumen = in_array($hoy_str, $dias_semana, true) ? $hoy_str : $dias_semana[0];

            // Conteo por turno y contraturno (0/1) en un solo query
            $sqlResumen = "
                    SELECT
                    es_contraturno,
                    SUM(CASE WHEN estado='P'  THEN 1 ELSE 0 END) AS presentes,
                    SUM(CASE WHEN estado='A'  THEN 1 ELSE 0 END) AS ausentes,
                    SUM(CASE WHEN estado='T'  THEN 1 ELSE 0 END) AS tarde,
                    COUNT(*) AS total
                    FROM asistencia_general
                    WHERE curso_id = ?
                    AND fecha = ?
                    GROUP BY es_contraturno
                ";

            $stmtR = $conexion->prepare($sqlResumen);
            $stmtR->bind_param('is', $curso_id, $fechaResumen);
            $stmtR->execute();
            $res = $stmtR->get_result();

            $R = [
                0 => ['presentes' => 0, 'ausentes' => 0, 'tarde' => 0, 'total' => 0], // Turno
                1 => ['presentes' => 0, 'ausentes' => 0, 'tarde' => 0, 'total' => 0], // Contraturno
            ];

            while ($row = $res->fetch_assoc()) {
                $k = (int)$row['es_contraturno'];
                $R[$k]['presentes'] = (int)$row['presentes'];
                $R[$k]['ausentes']  = (int)$row['ausentes'];
                $R[$k]['tarde']     = (int)$row['tarde'];
                $R[$k]['total']     = (int)$row['total'];
            }
            $stmtR->close();

            // Totales combinados (turno + contraturno)
            $Tot = [
                'presentes' => $R[0]['presentes'] + $R[1]['presentes'],
                'ausentes'  => $R[0]['ausentes']  + $R[1]['ausentes'],
                'tarde'     => $R[0]['tarde']     + $R[1]['tarde'],
                'total'     => $R[0]['total']     + $R[1]['total'],
            ];
            ?>
            <?php if ($modo == 'editar'): ?>
                <!-- EDICIÓN SEMANAL -->
                <form method="post" class="mt-4">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <input type="hidden" name="curso_id" value="<?= $curso_id ?>">
                    <input type="hidden" name="semana_lunes" value="<?= $semana_lunes ?>">
                    <?php foreach ($dias_semana as $dia): ?>
                        <input type="hidden" name="dias_semana[]" value="<?= $dia ?>">
                    <?php endforeach; ?>
                    <div class="overflow-y-auto rounded-xl shadow bg-white" style="max-height: 600px;">
                        <table class="min-w-full bg-white rounded-xl shadow">
                            <thead>
                                <!-- Fila 1: días con colspan -->
                                <tr>
                                    <th class="py-2 px-4 text-left">#</th>
                                    <th class="py-2 px-4 text-left">Alumno</th>
                                    <?php foreach ($dias_semana as $dia):
                                        $dt = new DateTime($dia);
                                        $nombre_es = $dias_es[$dt->format('l')];
                                    ?>
                                        <th class="py-2 px-4 text-center" colspan="2">
                                            <?= $nombre_es . " " . $dt->format('d/m') ?>
                                        </th>
                                    <?php endforeach; ?>
                                </tr>

                                <!-- Fila 2: controles "Aplicar a todos" -->
                                <tr class="bg-gray-50 text-sm text-center">
                                    <th colspan="2" class="py-2 px-4 font-medium text-left">Aplicar a todos:</th>
                                    <?php foreach ($dias_semana as $dia): ?>
                                        <?php foreach (['turno', 'contraturno'] as $tipo): ?>
                                            <th class="py-1 px-2">
                                                <select onchange="setAll('<?= $dia ?>','<?= $tipo ?>', this.value)" class="border rounded px-2 py-1 text-sm">
                                                    <option value="NC">NC</option>
                                                    <option value="P">P</option>
                                                    <option value="A">A</option>
                                                    <option value="AJ">AJ</option>
                                                    <option value="T">T</option>
                                                </select>
                                            </th>
                                        <?php endforeach; ?>
                                    <?php endforeach; ?>
                                </tr>

                                <!-- Fila 3: sub-encabezados Turno/Contra -->
                                <tr>
                                    <th></th>
                                    <th></th>
                                    <?php foreach ($dias_semana as $dia): ?>
                                        <th class="py-1 px-2 text-center">Turno</th>
                                        <th class="py-1 px-2 text-center">Contra</th>
                                    <?php endforeach; ?>
                                </tr>
                            </thead>
                            <tbody>
                                <?php $contador = 1; ?>
                                <?php foreach ($alumnos as $a): ?>
                                    <tr>
                                        <td class="py-2 px-4 text-gray-500 font-mono"><?= $contador++ ?></td>
                                        <td class="py-2 px-4"><?= $a['apellido'] . " " . $a['nombre']; ?></td>
                                        <?php foreach ($dias_semana as $dia): ?>
                                            <?php foreach (['turno', 'contraturno'] as $tipo): ?>
                                                <?php
                                                $esHoy = ($dia === $hoy_str);
                                                $claseHoyTd = $esHoy ? ' ring-2 ring-amber-300 rounded-md' : '';
                                                $bloquearContra = ($tipo === 'contraturno' && !$fecha_tiene_contraturno[$dia]);
                                                ?>
                                                <td class="py-2 px-4 text-center<?= $claseHoyTd ?>">
                                                    <select
                                                        name="asistencias[<?= $a['id'] ?>][<?= $dia ?>][<?= $tipo ?>]"
                                                        data-dia="<?= $dia ?>"
                                                        data-tipo="<?= $tipo ?>"
                                                        class="border rounded px-2 py-1 <?= $bloquearContra ? 'bg-gray-100 opacity-60 cursor-not-allowed' : '' ?>"
                                                        <?= $bloquearContra ? 'disabled' : '' ?>
                                                        title="<?= $bloquearContra ? 'Sin contraturno para este curso en este día' : '' ?>">
                                                        <option value="NC" <?= !in_array(($asist_semana[$a['id']][$dia][$tipo] ?? ''), ['P', 'A', 'T', 'AJ']) ? 'selected' : '' ?>>NC</option>
                                                        <option value="P" <?= ($asist_semana[$a['id']][$dia][$tipo] ?? '') == 'P'  ? 'selected' : '' ?>>P</option>
                                                        <option value="A" <?= ($asist_semana[$a['id']][$dia][$tipo] ?? '') == 'A'  ? 'selected' : '' ?>>A</option>
                                                        <option value="AJ" <?= ($asist_semana[$a['id']][$dia][$tipo] ?? '') == 'AJ' ? 'selected' : '' ?>>AJ</option>
                                                        <option value="T" <?= ($asist_semana[$a['id']][$dia][$tipo] ?? '') == 'T'  ? 'selected' : '' ?>>T</option>
                                                    </select>
                                                </td>
                                            <?php endforeach; ?>
                                        <?php endforeach; ?>
                                    </tr>
                                <?php endforeach; ?>
                                <?php if (empty($alumnos)): ?>
                                    <tr>
                                        <td colspan="<?= 2 + count($dias_semana) * 2 ?>" class="py-4 text-center text-gray-500">No hay alumnos cargados.</td>
                                    </tr>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                    <button type="submit" class="mt-4 px-6 py-2 rounded-xl bg-green-600 text-white hover:bg-green-700 font-bold">
                        Guardar asistencias
                    </button>
                    <button id="btn-importar-prof" type="button"
                        class="mt-4 px-4 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700 font-bold">
                        Importar desde Profesor
                    </button>
                </form>
            <?php else: ?>
                <!-- MODO VISUALIZACIÓN -->
                <div class="overflow-y-auto rounded-xl shadow bg-white mt-4" style="max-height: 600px;">
                    <table class="min-w-full bg-white rounded-xl shadow">
                        <thead>
                            <tr>
                                <th class="py-2 px-4 text-left">#</th>
                                <th class="py-2 px-4 text-left">Alumno</th>
                                <?php foreach ($dias_semana as $dia): ?>
                                    <?php
                                    $dt = new DateTime($dia);
                                    $nombre_es = $dias_es[$dt->format('l')]; // Nombre en español
                                    ?>
                                    <th class="py-2 px-4 text-center" colspan="2"><?= $nombre_es . " " . $dt->format('d/m') ?></th>

                                <?php endforeach; ?>
                            </tr>
                            <tr>
                                <th></th>
                                <th></th>
                                <?php foreach ($dias_semana as $dia): ?>
                                    <th class="py-1 px-2 text-center">Turno</th>
                                    <th class="py-1 px-2 text-center">Contra</th>
                                <?php endforeach; ?>
                            </tr>
                        </thead>
                        <tbody>
                            <?php $contador = 1; ?>
                            <?php foreach ($alumnos as $a): ?>
                                <tr>
                                    <td class="py-2 px-4 text-gray-500 font-mono"><?= $contador++ ?></td>
                                    <td class="py-2 px-4"><?= $a['apellido'] . " " . $a['nombre']; ?></td>
                                    <?php foreach ($dias_semana as $dia): ?>
                                        <?php foreach (['turno', 'contraturno'] as $tipo): ?>
                                            <td class="py-2 px-4 text-center">
                                                <?php
                                                $est = $asist_semana[$a['id']][$dia][$tipo] ?? '-';
                                                if ($est == 'P') echo '<span class="text-green-700 font-bold">P</span>';
                                                elseif ($est == 'A') echo '<span class="text-red-700 font-bold">A</span>';
                                                elseif ($est == 'AJ') echo '<span class="text-green-900 font-bold">AJ</span>';
                                                elseif ($est == 'T') echo '<span class="text-yellow-700 font-bold">T</span>';
                                                elseif ($est == 'NC' || $est == '') echo '<span class="text-gray-700 font-bold">NC</span>';
                                                else echo '-';
                                                ?>
                                            </td>
                                        <?php endforeach; ?>
                                    <?php endforeach; ?>
                                </tr>
                            <?php endforeach; ?>
                            <?php if (empty($alumnos)): ?>
                                <tr>
                                    <td colspan="<?= 2 + count($dias_semana) * 2 ?>" class="py-4 text-center text-gray-500">No hay alumnos cargados.</td>
                                </tr>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            <?php endif; ?>
            <div class="mt-6 bg-white border rounded-xl p-4 shadow text-sm w-full">
                <h2 class="font-bold mb-3 text-lg">
                    Resumen del día (<?= date('d/m/Y', strtotime($fechaResumen)) ?>)
                </h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 ">
                    <!-- Turno -->
                    <div class="rounded-lg border p-3">
                        <h3 class="font-semibold mb-2">Turno</h3>
                        <ul class="space-y-1">
                            <li><span class="font-medium text-green-700">✅ Presentes:</span> <?= $R[0]['presentes'] ?></li>
                            <li><span class="font-medium text-red-700">❌ Ausentes:</span> <?= $R[0]['ausentes'] ?></li>
                            <li><span class="font-medium text-yellow-700">🕒 Tarde:</span> <?= $R[0]['tarde'] ?></li>
                            <li class="text-gray-600">Total registros: <?= $R[0]['total'] ?></li>
                        </ul>
                    </div>
                    <!-- Contraturno -->
                    <div class="rounded-lg border p-3">
                        <h3 class="font-semibold mb-2">Contraturno</h3>
                        <ul class="space-y-1">
                            <li><span class="font-medium text-green-700">✅ Presentes:</span> <?= $R[1]['presentes'] ?></li>
                            <li><span class="font-medium text-red-700">❌ Ausentes:</span> <?= $R[1]['ausentes'] ?></li>
                            <li><span class="font-medium text-yellow-700">🕒 Tarde:</span> <?= $R[1]['tarde'] ?></li>
                            <li class="text-gray-600">Total registros: <?= $R[1]['total'] ?></li>
                        </ul>
                    </div>
                </div>
            </div>
        <?php endif; ?>
        <div id="modalImport" class="fixed inset-0 bg-black/40 hidden items-center justify-center z-50">
            <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl p-6">
                <h3 class="text-xl font-bold mb-3">Importar asistencias desde Profesor</h3>
                <div class="grid gap-3">
                    <div>
                        <label class="font-semibold">Curso seleccionado:</label>
                        <div id="imp-curso" class="text-gray-700"></div>
                    </div>
                    <div>
                        <label class="font-semibold">Fecha:</label>
                        <input type="date" id="imp-fecha" class="border rounded p-2" value="<?= date('Y-m-d') ?>">
                    </div>
                    <div>
                        <label class="font-semibold">Materias con clase ese día:</label>
                        <div id="imp-materias" class="grid grid-cols-1 md:grid-cols-2 gap-2 mt-1"></div>
                        <div id="imp-warn" class="text-sm text-amber-700 mt-2"></div>
                    </div>
                    <div class="flex gap-2 mt-4">
                        <button id="imp-simular" class="px-4 py-2 bg-gray-700 text-white rounded-xl">Simular</button>
                        <button id="imp-guardar" class="px-4 py-2 bg-green-600 text-white rounded-xl">Importar y Guardar</button>
                        <button id="imp-cerrar" class="ml-auto px-3 py-2 bg-red-100 text-red-700 rounded-xl">Cerrar</button>
                    </div>
                    <div id="imp-resumen" class="text-sm mt-2 hidden"></div>
                </div>
            </div>
        </div>
    </main>
    <script>
        /** Fetch robusto con:
         * - timeout
         * - chequeo res.ok
         * - parseo JSON/texto
         * - mensaje visible y log en consola
         */
        async function fetchJSON(url, options = {}) {
            const ctrl = new AbortController();
            const t = setTimeout(() => ctrl.abort(), 20000);
            try {
                const res = await fetch(url, {
                    ...options,
                    signal: ctrl.signal
                });
                const ct = res.headers.get('content-type') || '';
                let payload;
                if (ct.includes('application/json')) {
                    payload = await res.json().catch(() => ({}));
                } else {
                    payload = await res.text();
                }
                if (!res.ok) {
                    const msg = (payload && payload.mensaje) ? payload.mensaje : `HTTP ${res.status}`;
                    throw new Error(msg);
                }
                return payload;
            } catch (err) {
                console.error('fetchJSON error:', url, err);
                const msj = document.getElementById('mensaje');
                if (msj) {
                    msj.textContent = `⚠️ Error de red/servidor: ${err.message || err}`;
                    msj.className = "mt-4 text-center font-medium text-red-600";
                    msj.classList.remove('hidden');
                } else {
                    alert(`Error: ${err.message || err}`);
                }
                throw err;
            } finally {
                clearTimeout(t);
            }
        }
        window.addEventListener('unhandledrejection', (e) => {
            console.error('Unhandled promise rejection:', e.reason);
        });
    </script>
    <script>
        function aplicarColorSelect(select) {
            // Limpiar todas las clases de colores y text-white
            select.classList.remove(
                'bg-estadoA',
                'bg-estadoP',
                'bg-estadoT',
                'bg-estadoNC',
                'bg-estadoAJ',
                'text-white'
            );

            // Asignar color según valor
            if (select.value === 'A') select.classList.add('bg-estadoA', 'text-white');
            else if (select.value === 'P') select.classList.add('bg-estadoP', 'text-white');
            else if (select.value === 'T') select.classList.add('bg-estadoT', 'text-white');
            else if (select.value === 'NC' || select.value === 'N/C') select.classList.add('bg-estadoNC', 'text-white');
            else if (select.value === 'AJ') select.classList.add('bg-estadoAJ', 'text-white');
        }

        // Aplica colores solo en selects de alumnos
        document.addEventListener('change', e => {
            if (e.target.tagName === 'SELECT') {
                aplicarColorSelect(e.target);
            }
        });


        // Aplica a todos al cargar
        document.addEventListener('DOMContentLoaded', () => {
            document.querySelectorAll('select').forEach(sel => aplicarColorSelect(sel));
        });
    </script>
    <script>
        const modal = document.getElementById('modalImport');
        const impCurso = document.getElementById('imp-curso');
        const impFecha = document.getElementById('imp-fecha');
        const impMaterias = document.getElementById('imp-materias');
        const impWarn = document.getElementById('imp-warn');
        const impResumen = document.getElementById('imp-resumen');
        const csrf = "<?= $csrf ?>";

        function openImportModal() {
            // ANTES: const sel = document.querySelector('select[name="curso_id"]');
            const sel = document.getElementById('sel-curso-preceptor'); // <- usa el id único
            const curso_id = sel?.value;
            if (!curso_id) return alert('Seleccioná un curso primero.');

            const cursoTxt = sel.options[sel.selectedIndex].textContent.trim();
            impCurso.textContent = cursoTxt;

            // resto igual...
            fetchJSON('preceptor_materias_curso.php?curso_id=' +
                    encodeURIComponent(curso_id) +
                    '&fecha=' + encodeURIComponent(impFecha.value))
                .then(data => {
                    // data.ok ya garantizado por fetchJSON
                    impMaterias.innerHTML = '';
                    (data.materias || []).forEach(m => {
                        impMaterias.insertAdjacentHTML('beforeend', `
        <label class="flex items-center gap-2 p-2 border rounded">
          <input type="checkbox" value="${m.id}">
          <span>${m.nombre} ${m.es_contraturno?'<span class="text-xs text-indigo-600">(Contraturno)</span>':''}</span>
        </label>
      `);
                    });
                })
                .catch(err => {
                    console.error(err);
                    alert('No pude cargar las materias del día seleccionado.');
                });


            modal.classList.remove('hidden');
            modal.classList.add('flex');
        }

        document.getElementById('btn-importar-prof').addEventListener('click', openImportModal);
        document.getElementById('imp-cerrar').addEventListener('click', () => {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
        });
        impFecha.addEventListener('change', openImportModal); // recarga materias si cambia la fecha

        function collectSelectedMaterias() {
            return Array.from(impMaterias.querySelectorAll('input[type="checkbox"]:checked')).map(i => parseInt(i.value, 10));
        }

        function callImport(dryRun) {
            const sel = document.getElementById('sel-curso-preceptor');
            const curso_id = parseInt(sel.value, 10);
            const materia_ids = collectSelectedMaterias();
            if (!curso_id) return alert('Seleccioná un curso.');
            if (!impFecha.value) return alert('Seleccioná fecha.');
            if (materia_ids.length === 0) return alert('Seleccioná al menos una materia con clase ese día.');

            fetchJSON('/users/preceptor/preceptor_importar_prof.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    csrf,
                    curso_id,
                    fecha: impFecha.value,
                    materia_ids,
                    dry_run: dryRun
                })
            }).then(data => {
                const w = (data.warnings || []).length ? ('⚠️ ' + data.warnings.join(' ')) : '';
                if (data.simulacion) {
                    const t = (data.turno || {});
                    const c = (data.contraturno || {});
                    impResumen.innerHTML = `
      <div class="p-2 rounded border">
        <div class="font-semibold mb-1">Simulación para ${data.fecha}</div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <div>
            <div class="font-medium">Turno</div>
            <div>✅ P: ${t.P||0} / ❌ A: ${t.A||0} / 🕒 T: ${t.T||0} / AJ: ${t.AJ||0} / NC: ${t.NC||0} (Total ${t.total||0})</div>
          </div>
          <div>
            <div class="font-medium">Contraturno</div>
            <div>✅ P: ${c.P||0} / ❌ A: ${c.A||0} / 🕒 T: ${c.T||0} / AJ: ${c.AJ||0} / NC: ${c.NC||0} (Total ${c.total||0})</div>
          </div>
        </div>
        ${w ? `<div class="text-amber-700 mt-1">${w}</div>` : ''}
      </div>`;
                    impResumen.classList.remove('hidden');
                } else {
                    alert(data.mensaje || 'Importación realizada');
                    location.reload();
                }
            }).catch(() => alert('Error de red'));
        }

        document.getElementById('imp-simular').addEventListener('click', () => callImport(true));
        document.getElementById('imp-guardar').addEventListener('click', () => callImport(false));
    </script>
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
        function setAll(dia, tipo, valor) {
            const selects = document.querySelectorAll(`select[data-dia='${dia}'][data-tipo='${tipo}']`);
            selects.forEach(s => {
                if (!s.hasAttribute('disabled')) {
                    s.value = valor;
                    aplicarColorSelect(s); // 👈 fuerza el color acá mismo
                    s.dispatchEvent(new Event('change')); // por si hay más lógica asociada
                }
            });
        }
    </script>
    <script>
        function calcularLunesDesde(fechaStr) {
            const partes = fechaStr.split('-');
            const fecha = new Date(partes[0], partes[1] - 1, partes[2]); // local date
            const diaSemana = fecha.getDay(); // 0 = domingo ... 6 = sábado
            const diff = diaSemana === 0 ? -6 : 1 - diaSemana;
            fecha.setDate(fecha.getDate() + diff);
            return fecha;
        }

        function formatoFechaCorta(fecha) {
            const d = fecha.getDate().toString().padStart(2, '0');
            const m = (fecha.getMonth() + 1).toString().padStart(2, '0');
            return `${d}-${m}`;
        }

        function actualizarRangoVisual(fechaStr) {
            const lunes = calcularLunesDesde(fechaStr);
            const viernes = new Date(lunes);
            viernes.setDate(lunes.getDate() + 4);

            const lunesISO = lunes.toISOString().split('T')[0];
            document.getElementById('selector-fecha').value = lunesISO;
            document.getElementById('input-semana-lunes').value = lunesISO;

            document.getElementById('texto-rango').textContent =
                `${formatoFechaCorta(lunes)} / ${formatoFechaCorta(viernes)}`;
        }

        document.addEventListener('DOMContentLoaded', function() {
            const fechaActual = document.getElementById('input-semana-lunes').value || new Date().toISOString().split('T')[0];
            actualizarRangoVisual(fechaActual);

            document.getElementById('selector-fecha').addEventListener('change', function() {
                actualizarRangoVisual(this.value);
            });
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