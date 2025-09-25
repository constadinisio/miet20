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
require_once __DIR__ . '/../../../backend/includes/db.php';

error_reporting(E_ERROR | E_PARSE);
ini_set('display_errors', 0);

// Evitar notice en modal al pie
$mostrar_modal = $mostrar_modal ?? false;

// ---- Filtros base ----
$curso_id   = (isset($_GET['curso_id']) && ctype_digit($_GET['curso_id'])) ? (int)$_GET['curso_id'] : null;
$fecha_base = $_GET['fecha'] ?? date('Y-m-d');

// Armar semana (lunes a viernes)
$lunes = new DateTime($fecha_base);
if ($lunes->format('N') != 1) {
    $lunes->modify('monday this week');
}
$dias_semana = [];
for ($i = 0; $i < 5; $i++) {
    $dias_semana[] = $lunes->format('Y-m-d');
    $lunes->modify('+1 day');
}

// === ENDPOINT: días habilitados para curso+materia del profesor ===
// === ENDPOINT: días habilitados para cargo ===
if (isset($_GET['accion']) && $_GET['accion'] === 'dias_habilitados') {
    header('Content-Type: application/json');
    $cargoQ = isset($_GET['cargo_id']) && ctype_digit($_GET['cargo_id']) ? (int)$_GET['cargo_id'] : 0;
    $fechaQ = $_GET['fecha'] ?? date('Y-m-d');

    $allowed = [];
    if ($cargoQ) {
        $cursoQ   = isset($_GET['curso_id']) && ctype_digit($_GET['curso_id']) ? (int)$_GET['curso_id'] : 0;
        $materiaQ = isset($_GET['materia_id']) && ctype_digit($_GET['materia_id']) ? (int)$_GET['materia_id'] : 0;

        $q = "SELECT DISTINCT dia_semana
      FROM horarios_materia
      WHERE curso_id = ? AND materia_id = ?";
        $st = $conexion->prepare($q);
        if (!$st) {
            http_response_code(500);
            echo json_encode(['error' => 'Error al preparar la consulta']);
            exit;
        }
        $st->bind_param("ii", $cursoQ, $materiaQ);
        if (!$st->execute()) {
            http_response_code(500);
            echo json_encode(['error' => 'Error al ejecutar la consulta']);
            exit;
        }
        $rs = $st->get_result();

        $mapDias = [
            'Lunes'     => 1,
            'Martes'    => 2,
            'Miércoles' => 3,
            'Miercoles' => 3,
            'Jueves'    => 4,
            'Viernes'   => 5,
            'Sábado'    => 6,
            'Sabado'    => 6,
            'Domingo'   => 7
        ];

        while ($r = $rs->fetch_assoc()) {
            $valor = trim($r['dia_semana']);
            if (is_numeric($valor)) {
                $d = (int)$valor;
            } elseif (isset($mapDias[$valor])) {
                $d = $mapDias[$valor];
            } else {
                $d = 0;
            }
            if ($d >= 1 && $d <= 7) {
                $allowed[] = $d;
            }
        }
        $st->close();
    }
    sort($allowed);

    // Armar semana
    $week = [];
    if (!empty($_GET['fechas_tabla'])) {
        $fechas_tabla = explode(',', $_GET['fechas_tabla']);
        foreach ($fechas_tabla as $fecha_col) {
            $fecha_col = trim($fecha_col);
            if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $fecha_col)) continue;
            $dow = (int)date('N', strtotime($fecha_col));
            $week[] = [
                'fecha' => $fecha_col,
                'dow'   => $dow,
                'habilitado' => in_array($dow, $allowed, true)
            ];
        }
    } else {
        $lun = new DateTime($fechaQ);
        if ($lun->format('N') != 1) $lun->modify('monday this week');
        for ($i = 0; $i < 5; $i++) {
            $f = clone $lun;
            $f->modify("+$i day");
            $dow = (int)$f->format('N');
            $week[] = [
                'fecha' => $f->format('Y-m-d'),
                'dow'   => $dow,
                'habilitado' => in_array($dow, $allowed, true)
            ];
        }
    }

    $dowSel = (int)date('N', strtotime($fechaQ));
    $allowedToday = in_array($dowSel, $allowed, true);

    $prox = null;
    if (!$allowedToday && !empty($allowed)) {
        $ts = strtotime($fechaQ);
        for ($i = 0; $i < 14; $i++) {
            $t = strtotime("+$i day", $ts);
            $d = (int)date('N', $t);
            if (in_array($d, $allowed, true)) {
                $prox = date('Y-m-d', $t);
                break;
            }
        }
    }

    echo json_encode([
        'allowed_dows'   => $allowed,
        'allowed_today'  => $allowedToday,
        'next_valid'     => $prox,
        'week'           => $week
    ]);
    exit;
}

// Cargar cursos y materias del profesor
$cursosMaterias = [];
$stmt = $conexion->prepare("
    SELECT DISTINCT 
        cu.id AS curso_id,
        CONCAT(cu.anio, '°', cu.division) AS curso_nombre,
        m.id AS materia_id,
        m.nombre AS materia_nombre,
        cg.id AS cargo_id
    FROM cargos cg
    JOIN cargo_materias cm ON cg.id = cm.cargo_id
    JOIN materias m ON cm.materia_id = m.id
    JOIN cargo_materia_curso cmc ON cm.id = cmc.cargo_materia_id
    JOIN cursos cu ON cmc.curso_id = cu.id
    WHERE cg.docente_id = ?
    ORDER BY cu.anio, cu.division, m.nombre
");
$stmt->bind_param("i", $usuario['id']);
$stmt->execute();
$res = $stmt->get_result();
while ($row = $res->fetch_assoc()) {
    $cursosMaterias[] = $row;
}
$stmt->close();


// === RESUMEN DEL DÍA PARA PROFESOR ===
$hoy_str = date('Y-m-d');
$fechaResumen = $fecha_base;

$R = ['presentes' => 0, 'ausentes' => 0, 'tarde' => 0, 'total' => 0];

if ($curso_id) {
    $sqlResumen = "
        SELECT 
        SUM(CASE WHEN estado='P'  THEN 1 ELSE 0 END) AS presentes,
        SUM(CASE WHEN estado='A'  THEN 1 ELSE 0 END) AS ausentes,
        SUM(CASE WHEN estado='T'  THEN 1 ELSE 0 END) AS tarde,
        COUNT(*) AS total
        FROM asistencia_materia
        WHERE curso_id = ?
        AND fecha = ?
    ";
    $stmtR = $conexion->prepare($sqlResumen);
    $stmtR->bind_param('is', $curso_id, $fechaResumen);
    $stmtR->execute();
    $resR = $stmtR->get_result();
    error_log("DEBUG resumen curso:$curso_id fecha:$fechaResumen");
    if ($row = $resR->fetch_assoc()) {
        $R['presentes'] = (int)$row['presentes'];
        $R['ausentes']  = (int)$row['ausentes'];
        $R['tarde']     = (int)$row['tarde'];
        $R['total']     = (int)$row['total'];
    }
    error_log("DEBUG resumen row: " . print_r($row, true));
    $stmtR->close();
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Panel de Profesor</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="/images/et20png.png">
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
            <a href="libro_temas.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Libro de Temas">
                <span class="text-xl">📚</span><span class="sidebar-label">Libro de Temas</span>
            </a>
            <a href="calificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Calificaciones">
                <span class="text-xl">📝</span><span class="sidebar-label">Calificaciones</span>
            </a>
            <a href="asistencias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Asistencias P/ Materia">
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

        <!-- Sistema de Asistencia por Materia -->
        <div class="bg-white p-6 rounded-2xl shadow-xl max-w-7xl mx-auto">
            <h2 class="text-2xl font-bold mb-4">🗓️ Asistencias por materia</h2>

            <form id="form-asistencias" class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6 items-center">
                <input type="hidden" name="csrf" value="<?= $csrf ?>">
                <label class="font-semibold">Curso y Materia:</label>
                <select id="seleccion" name="seleccion" required class="border rounded p-2 col-span-3">
                    <option value="">Seleccionar...</option>
                    <?php foreach ($cursosMaterias as $cm): ?>
                        <option value="<?= $cm['curso_id'] ?>_<?= $cm['materia_id'] ?>_<?= $cm['cargo_id'] ?>">
                            <?= $cm['curso_nombre'] ?> - <?= $cm['materia_nombre'] ?>
                        </option>
                    <?php endforeach; ?>
                </select>

                <label class="font-semibold">Fecha base:</label>
                <input type="date" id="fecha" name="fecha" class="border rounded p-2 col-span-3" value="<?= date('Y-m-d') ?>">
            </form>
            <div id="contenedor-tabla" class="overflow-x-auto hidden">
                <table id="tabla-asistencias" class="min-w-full bg-white border border-gray-300 text-sm">
                    <thead class="bg-indigo-100 text-indigo-900 font-bold text-center"></thead>
                    <tbody></tbody>
                </table>
                <div class="flex gap-4 mt-4">
                    <button id="btn-guardar" class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-xl">
                        Guardar asistencias
                    </button>
                    <button id="btn-importar" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl">
                        Importar del preceptor
                    </button>
                </div>
            </div>
            <div id="mensaje" class="mt-4 text-center font-medium hidden"></div>
        </div>
        <div id="resumen-dia" class="mt-6"></div>
    </main>
    <script>
        // Actualiza la URL sin recargar (para poder compartir/recargar sin perder selección)
        function pushStateSinRecargar(curso_id, fecha) {
            const url = new URL(window.location.href);
            if (curso_id) url.searchParams.set('curso_id', curso_id);
            else url.searchParams.delete('curso_id');
            if (fecha) url.searchParams.set('fecha', fecha);
            else url.searchParams.delete('fecha');
            history.replaceState({}, '', url.toString());
        }

        async function cargarResumen() {
            const seleccion = document.getElementById('seleccion').value;
            const fecha = document.getElementById('fecha').value;
            const box = document.getElementById('resumen-dia');

            if (!seleccion || !fecha) {
                box.innerHTML = '';
                return;
            }

            const [curso_id] = seleccion.split('_');
            try {
                const res = await fetch(`resumen_profesor.php?curso_id=${encodeURIComponent(curso_id)}&fecha=${encodeURIComponent(fecha)}`);
                const data = await res.json();

                box.innerHTML = `
                    <div class="bg-white border rounded-xl p-4 shadow text-sm max-w-md mx-auto">
                    <h2 class="font-bold mb-3 text-lg">Resumen del día (${data.fecha_formateada})</h2>
                    <div class="rounded-lg border p-3">
                        <h3 class="font-semibold mb-2">Turno</h3>
                        <ul class="space-y-1">
                        <li><span class="font-medium text-green-700">✅ Presentes:</span> ${data.turno.presentes}</li>
                        <li><span class="font-medium text-red-700">❌ Ausentes:</span> ${data.turno.ausentes}</li>
                        <li><span class="font-medium text-yellow-700">🕒 Tarde:</span> ${data.turno.tarde}</li>
                        <li class="text-gray-600">Total registros: ${data.turno.total}</li>
                        </ul>
                    </div>
                    </div>`;
            } catch (e) {
                box.innerHTML = '';
                console.error('Resumen AJAX error', e);
            }
        }

        function onCambioSeleccionOFecha() {
            const seleccion = document.getElementById('seleccion').value;
            const fecha = document.getElementById('fecha').value;
            if (!seleccion || !fecha) return;

            const [curso_id] = seleccion.split('_');
            // Persistimos en la URL, sin recargar
            pushStateSinRecargar(curso_id, fecha);

            // Actualizamos tabla y resumen
            cargarAsistencias();
            cargarResumen();
        }

        document.getElementById('seleccion').addEventListener('change', onCambioSeleccionOFecha);
        document.getElementById('fecha').addEventListener('change', onCambioSeleccionOFecha);
        // (Dejamos también los listeners originales para compatibilidad)
    </script>
    <script>
        document.getElementById('seleccion').addEventListener('change', cargarAsistencias);
        document.getElementById('fecha').addEventListener('change', cargarAsistencias);

        // ---- Helpers UX bloqueo ----
        function deshabilitarBotones(estado, msg = '') {
            const btnG = document.getElementById('btn-guardar');
            const btnI = document.getElementById('btn-importar');
            const m = document.getElementById('mensaje');
            if (btnG) btnG.disabled = estado;
            if (btnI) btnI.disabled = estado;
            if (msg && m) {
                m.textContent = msg;
                m.className = "mt-4 text-center font-medium " + (estado ? 'text-orange-600' : 'text-green-600');
                m.classList.remove('hidden');
                setTimeout(() => m.classList.add('hidden'), 6000);
            }
        }

        function bloquearColumna(tabla, colIndex) {
            const filas = tabla.querySelectorAll('tbody tr');
            filas.forEach(tr => {
                const td = tr.children[colIndex];
                if (!td) return;
                const sel = td.querySelector('select');
                if (sel) {
                    const val = sel.value;
                    td.innerHTML = '';
                    const span = document.createElement('span');
                    span.textContent = val;
                    span.className = 'text-gray-400 italic';
                    td.appendChild(span);
                } else {
                    td.classList.add('text-gray-400', 'italic');
                }
            });
        }

        async function cargarAsistencias() {
            const seleccion = document.getElementById('seleccion').value;
            const fechaInput = document.getElementById('fecha');
            const fecha = fechaInput.value;
            if (!seleccion || !fecha) return;

            const [curso_id, materia_id, cargo_id] = seleccion.split('_');

            // 1) Obtener fechas de las columnas de la tabla (si ya existen)
            let fechasTabla = [];
            const tablaEnc = document.querySelector('#tabla-asistencias thead');
            if (tablaEnc) {
                const ths = Array.from(tablaEnc.querySelectorAll('th')).slice(2); // Saltar Nº y Nombre
                ths.forEach(th => {
                    const txt = th.textContent.trim();
                    let normalizada = null;
                    const m = txt.match(/^(\d{2})-(\d{2})-(\d{4})$/);
                    if (m) normalizada = `${m[3]}-${m[2]}-${m[1]}`;
                    else if (/^\d{4}-\d{2}-\d{2}$/.test(txt)) normalizada = txt;
                    if (normalizada) fechasTabla.push(normalizada);
                });
            }

            let resp;
            try {
                const r = await fetch(`?accion=dias_habilitados&cargo_id=${cargo_id}&curso_id=${curso_id}&materia_id=${materia_id}&fecha=${encodeURIComponent(fecha)}&fechas_tabla=${encodeURIComponent(fechasTabla.join(','))}`);
                resp = await r.json();
            } catch (e) {
                console.error('Error dias_habilitados', e);
                deshabilitarBotones(false);
            }

            // Ya no forzamos el cambio de fecha ni bloqueamos todo por allowed_today

            // 2) Cargar la tabla
            const fechaFinal = fechaInput.value;
            const res = await fetch(`obtener_asistencias_materia.php?curso_id=${curso_id}&materia_id=${materia_id}&cargo_id=${cargo_id}&fecha=${fechaFinal}`);
            const data = await res.json();

            const tabla = document.getElementById('tabla-asistencias');
            const thead = tabla.querySelector('thead');
            const tbody = tabla.querySelector('tbody');
            thead.innerHTML = '';
            tbody.innerHTML = '';

            const trHead = document.createElement('tr');
            data.columnas.forEach(col => {
                const th = document.createElement('th');
                th.className = 'border px-2 py-1';
                th.textContent = col;
                trHead.appendChild(th);
            });
            thead.appendChild(trHead);

            const trApply = document.createElement('tr');

            // columnas vacías en Nº y Nombre
            trApply.innerHTML = `<th class="border px-2 py-1 text-center bg-gray-50">Aplicar a todos:</th>
                     <th class="border px-2 py-1"></th>`;

            for (let i = 2; i < data.columnas.length; i++) {
                const th = document.createElement('th');
                th.className = 'border px-2 py-1 text-center';

                const sel = document.createElement('select');
                ['NC', 'P', 'A', 'T', 'AJ'].forEach(est => {
                    const opt = document.createElement('option');
                    opt.value = est;
                    opt.textContent = est;
                    sel.appendChild(opt);
                });

                // cuando cambie, aplicar a toda la columna
                sel.addEventListener('change', () => aplicarATodosColumna(i, sel.value));

                aplicarColorSelect(sel);
                th.appendChild(sel);
                trApply.appendChild(th);
            }
            thead.appendChild(trApply);

            data.filas.forEach((fila, idx) => {
                const tr = document.createElement('tr');
                tr.dataset.alumnoId = (data.alumno_ids && data.alumno_ids[idx]) ? data.alumno_ids[idx] : '';
                fila.forEach((valor, i) => {
                    const td = document.createElement('td');
                    td.className = 'border px-2 py-1 text-center';

                    if (i >= 2) {
                        const editable = data.editable[i - 2];
                        if (editable) {
                            const sel = document.createElement('select');
                            ['NC', 'P', 'A', 'T', 'AJ'].forEach(est => {
                                const opt = document.createElement('option');
                                opt.value = est;
                                opt.textContent = est;
                                if (est === valor) opt.selected = true;
                                sel.appendChild(opt);
                            });
                            aplicarColorSelect(sel);
                            td.appendChild(sel);
                        } else {
                            td.textContent = valor;
                            td.classList.add('text-gray-400', 'italic');
                        }
                    } else {
                        td.textContent = valor;
                    }
                    tr.appendChild(td);
                });
                tbody.appendChild(tr);
            });

            document.getElementById('contenedor-tabla').classList.remove('hidden');

            // 3) Bloquear columnas que NO están en horario para esa semana
            if (resp && Array.isArray(resp.week)) {
                const ths = Array.from(thead.querySelectorAll('th'));
                for (let i = 2; i < ths.length; i++) {
                    const txt = ths[i].textContent.trim();
                    let yyyy_mm_dd = null;
                    const m = txt.match(/^(\d{2})-(\d{2})-(\d{4})$/);
                    if (m) yyyy_mm_dd = `${m[3]}-${m[2]}-${m[1]}`;
                    else if (/^\d{4}-\d{2}-\d{2}$/.test(txt)) yyyy_mm_dd = txt;

                    if (yyyy_mm_dd) {
                        const inWeek = resp.week.find(w => w.fecha === yyyy_mm_dd);
                        if (!inWeek || !inWeek.habilitado) {
                            bloquearColumna(tabla, i);
                        }
                    }
                }

                // 4) Habilitar/deshabilitar botones según si hay al menos una columna editable
                const hayColumnaEditable = resp.week.some(w => w.habilitado);
                deshabilitarBotones(!hayColumnaEditable, hayColumnaEditable ? '' : 'No hay días habilitados esta semana.');
            } else {
                deshabilitarBotones(false);
            }
        }

        document.getElementById('btn-guardar').addEventListener('click', () => {
            const csrf = document.querySelector('input[name="csrf"]').value;
            const partes = document.getElementById('seleccion').value.split('_');
            const curso_id = parseInt(partes[0], 10);
            const materia_id = parseInt(partes[1], 10);
            const cargo_id = parseInt(partes[2], 10);

            // 1) Encabezados (para mapear índices a fechas en el PHP)
            const encabezados = [];
            document.querySelectorAll('#tabla-asistencias thead th').forEach(th => {
                encabezados.push(th.textContent.trim());
            });

            // 2) Filas de alumnos y estados (usando índice real de columna)
            const asistencias = [];
            document.querySelectorAll('#tabla-asistencias tbody tr').forEach(tr => {
                const alumno_id = parseInt(tr.dataset.alumnoId || '0', 10);
                const estados = {};

                tr.querySelectorAll('td').forEach((td, colIdx) => {
                    if (colIdx >= 2) { // desde la col de la primera fecha
                        const sel = td.querySelector('select');
                        estados[colIdx] = sel ? sel.value : 'NC';
                    }
                });

                if (alumno_id > 0) {
                    asistencias.push({
                        alumno_id,
                        estados
                    });
                } else {
                    const nro = parseInt(tr.children[0].textContent, 10);
                    asistencias.push({
                        nro,
                        estados
                    });
                }
            });

            // 3) Envío al backend
            fetch('guardar_asistencias_materia.php', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        curso_id,
                        materia_id,
                        cargo_id,
                        encabezados,
                        asistencias
                    })
                })
                .then(res => res.json())
                .then(resp => {
                    alert(resp.mensaje || 'Sin respuesta');
                })
                .catch(err => {
                    console.error('Error al guardar:', err);
                    alert('❌ Error al guardar asistencias.');
                });
        });

        document.getElementById('btn-importar').addEventListener('click', () => {
            const seleccion = document.getElementById('seleccion').value;
            const fecha = document.getElementById('fecha').value;
            const csrf = document.querySelector('input[name="csrf"]').value;

            if (!seleccion || !fecha) return alert("Seleccioná curso, materia y fecha.");

            const [curso_id, materia_id, cargo_id] = seleccion.split('_');

            fetch('importar_asistencia_general.php', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        csrf,
                        curso_id,
                        materia_id,
                        cargo_id,
                        fecha
                    })
                })
                .then(res => res.json())
                .then(data => {
                    if (!data.ok) {
                        alert(data.mensaje || 'No se pudo importar.');
                        return;
                    }

                    // Ubicar columna de la FECHA en el thead (a partir de la 3° col: 0=Nro, 1=Nombre, 2+=fechas)
                    const tabla = document.getElementById('tabla-asistencias');
                    const ths = Array.from(tabla.querySelectorAll('thead th'));
                    let colFechaIndex = -1;

                    for (let i = 2; i < ths.length; i++) {
                        const txt = ths[i].textContent.trim();
                        // Igualamos contra fecha exacta; si en encabezado mostrás dd-mm-YYYY, podrías normalizar como arriba
                        if (txt === fecha || txt.startsWith(fecha)) {
                            colFechaIndex = i;
                            break;
                        }
                    }

                    const map = data.map || {};
                    const filas = Array.from(tabla.querySelectorAll('tbody tr'));
                    let aplicados = 0;

                    filas.forEach(tr => {
                        const nroLista = tr.children[0]?.textContent?.trim();
                        if (!nroLista) return;

                        const estado = map[nroLista];
                        if (!estado) return;

                        const td = tr.children[colFechaIndex];
                        if (!td) return;
                        const sel = td.querySelector('select');
                        if (!sel) return;

                        sel.value = estado;
                        aplicados++;
                    });

                    const msj = document.getElementById('mensaje');
                    msj.textContent = (data.importados > 0) ?
                        `Importado del preceptor (${data.turno}). Coincidencias aplicadas: ${aplicados}/${filas.length}.` :
                        (data.mensaje || 'No había asistencias del preceptor para esa fecha/turno.');
                    msj.className = "mt-4 text-center font-medium " + (aplicados > 0 ? 'text-green-600' : 'text-orange-600');
                    msj.classList.remove('hidden');
                    setTimeout(() => msj.classList.add('hidden'), 6000);
                })
                .catch(() => alert('Error importando asistencias'));
        });
    </script>
    <script>
        function aplicarATodosColumna(colIndex, valor) {
            const filas = document.querySelectorAll('#tabla-asistencias tbody tr');
            filas.forEach(tr => {
                const td = tr.children[colIndex];
                if (!td) return;
                const sel = td.querySelector('select');
                if (sel) {
                    sel.value = valor;
                    aplicarColorSelect(sel);
                }
            });
        }
    </script>
    <script>
        function aplicarColorSelect(select) {
            // Limpiar cualquier clase vieja
            select.classList.remove('bg-estadoA', 'bg-estadoP', 'bg-estadoT', 'bg-estadoNC', 'bg-estadoAJ');

            // Asignar según el valor
            if (select.value === 'A') select.classList.add('bg-estadoA', 'text-white');
            else if (select.value === 'P') select.classList.add('bg-estadoP', 'text-white');
            else if (select.value === 'T') select.classList.add('bg-estadoT', 'text-white');
            else if (select.value === 'NC' || select.value === 'N/C') select.classList.add('bg-estadoNC', 'text-white');
            else if (select.value === 'AJ') select.classList.add('bg-estadoAJ', 'text-white');
        }

        // Escuchar cambios en cualquier select
        document.addEventListener('change', e => {
            if (e.target.tagName === 'SELECT') {
                aplicarColorSelect(e.target);
            }
        });
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
    <!-- Modal de ficha censal -->
    <div id="modalFichaCensal"
        class="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 <?= $mostrar_modal ? '' : 'hidden' ?>">
        <form id="fichaCensalForm"
            class="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md space-y-5"
            method="POST"
            autocomplete="off"
            style="min-width:300px">
            <h2 class="text-2xl font-bold text-center mb-3 text-blue-700">Completar ficha censal</h2>
            <p class="mb-2 text-gray-700 text-center">Para continuar, ingresá tu número de ficha censal:</p>
            <input type="text" id="ficha_censal" name="ficha_censal" required
                class="w-full border rounded-xl p-2" maxlength="30" autofocus>
            <button type="submit"
                class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 rounded-xl transition mt-2">
                Guardar
            </button>
            <p id="errorFichaCensal" class="text-red-600 text-center text-sm mt-2 hidden"></p>
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