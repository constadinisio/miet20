<?php
session_start();
if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

$usuario = $_SESSION['usuario'];
$flashError = $_SESSION['error_cargos'] ?? null;
unset($_SESSION['error_cargos']);
$apiError = null;

if (!function_exists('cargos_call_api')) {
    function cargos_call_api(string $method, string $path, ?array $payload = null, array $query = []): array
    {
        $token = $_SESSION['api_tokens']['accessToken'] ?? null;
        if (!$token) {
            throw new RuntimeException('No hay un token de acceso válido en la sesión.', 401);
        }

        $baseUrl = rtrim(getenv('API_BASE_URL') ?: 'http://localhost:3000/api/v1', '/');
        $url = $baseUrl . '/' . ltrim($path, '/');

        if (!empty($query)) {
            $url .= '?' . http_build_query($query);
        }

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, strtoupper($method));
        curl_setopt($ch, CURLOPT_TIMEOUT, 15);

        $headers = [
            'Accept: application/json',
            'Authorization: Bearer ' . $token,
        ];

        if ($payload !== null) {
            $body = json_encode($payload, JSON_UNESCAPED_UNICODE);
            if ($body === false) {
                throw new RuntimeException('No se pudo serializar el cuerpo de la petición.', 500);
            }
            $headers[] = 'Content-Type: application/json';
            curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
        }

        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

        $response = curl_exec($ch);
        if ($response === false) {
            $error = curl_error($ch);
            curl_close($ch);
            throw new RuntimeException('No se pudo contactar con la API: ' . $error, 502);
        }

        $status = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        $decoded = null;
        if ($response !== '' && $status !== 204) {
            $decoded = json_decode($response, true);
            if ($decoded === null && json_last_error() !== JSON_ERROR_NONE) {
                throw new RuntimeException('La API devolvió una respuesta inválida.', 502);
            }
        }

        if ($status >= 400) {
            $message = is_array($decoded) ? ($decoded['message'] ?? $decoded['error'] ?? 'Error en la API') : 'Error en la API';
            throw new RuntimeException($message, $status);
        }

        if ($decoded === null || $decoded === []) {
            return [];
        }

        return $decoded['data'] ?? $decoded ?? [];
    }
}

$cargo_id = isset($_GET['id']) ? (int) $_GET['id'] : null;
$cargo = null;
$relaciones = [];

$tiposCargo = [];
try {
    $tiposCargo = cargos_call_api('GET', '/cargos/tipos');
} catch (RuntimeException $exception) {
    $apiError = $apiError ?? 'No se pudo obtener el listado de tipos de cargo: ' . $exception->getMessage();
    $tiposCargo = [];
}

$tiposCargo = array_values(array_filter(array_map(function ($tipo) {
    $id = isset($tipo['id']) ? (int) $tipo['id'] : null;
    if (!$id) {
        return null;
    }

    return [
        'id' => $id,
        'nombre' => $tipo['nombre'] ?? ('Tipo #' . $id),
    ];
}, is_array($tiposCargo) ? $tiposCargo : []), fn($item) => $item !== null));

usort($tiposCargo, function ($a, $b) {
    return strcasecmp($a['nombre'], $b['nombre']);
});

$docentes = [];
try {
    $docentes = cargos_call_api('GET', '/usuarios', null, ['rol' => 3, 'status' => 1]);
} catch (RuntimeException $exception) {
    $apiError = $apiError ?? 'No se pudo obtener el listado de docentes: ' . $exception->getMessage();
    $docentes = [];
}

$docentes = array_values(array_filter(array_map(function ($docente) {
    $id = isset($docente['id']) ? (int) $docente['id'] : null;
    if (!$id) {
        return null;
    }

    return [
        'id' => $id,
        'nombre' => $docente['nombre'] ?? '',
        'apellido' => $docente['apellido'] ?? '',
    ];
}, is_array($docentes) ? $docentes : []), fn($item) => $item !== null));

usort($docentes, function ($a, $b) {
    $cmp = strcasecmp($a['apellido'], $b['apellido']);
    if ($cmp !== 0) {
        return $cmp;
    }
    return strcasecmp($a['nombre'], $b['nombre']);
});

$materias = [];
try {
    $materias = cargos_call_api('GET', '/materias', null, ['estado' => 'activo']);
} catch (RuntimeException $exception) {
    $apiError = $apiError ?? 'No se pudo obtener el listado de materias: ' . $exception->getMessage();
    $materias = [];
}

$materias = array_values(array_filter(array_map(function ($materia) {
    $id = isset($materia['id']) ? (int) $materia['id'] : null;
    if (!$id) {
        return null;
    }

    return [
        'id' => $id,
        'nombre' => $materia['nombre'] ?? ('Materia #' . $id),
    ];
}, is_array($materias) ? $materias : []), fn($item) => $item !== null));

usort($materias, function ($a, $b) {
    return strcasecmp($a['nombre'], $b['nombre']);
});

$cursos = [];
try {
    $cursos = cargos_call_api('GET', '/cursos');
} catch (RuntimeException $exception) {
    $apiError = $apiError ?? 'No se pudo obtener el listado de cursos: ' . $exception->getMessage();
    $cursos = [];
}

$cursos = array_values(array_filter(array_map(function ($curso) {
    $id = isset($curso['id']) ? (int) $curso['id'] : null;
    if (!$id) {
        return null;
    }

    $anio = $curso['anio'] ?? '';
    $division = $curso['division'] ?? '';
    $turno = $curso['turno'] ?? '';
    $nombre = $curso['nombre'] ?? '';

    if ($nombre === '') {
        $nombre = trim(($anio ? $anio . '° ' : '') . ($division ?: '') . ($turno ? ' (' . $turno . ')' : ''));
    }

    return [
        'id' => $id,
        'nombre' => $nombre !== '' ? $nombre : ('Curso #' . $id),
        'anio' => $anio,
        'division' => $division,
        'turno' => $turno,
    ];
}, is_array($cursos) ? $cursos : []), fn($item) => $item !== null));

usort($cursos, function ($a, $b) {
    $anioCmp = ($a['anio'] ?? 0) <=> ($b['anio'] ?? 0);
    if ($anioCmp !== 0) {
        return $anioCmp;
    }
    return strcasecmp($a['division'] ?? '', $b['division'] ?? '');
});

if ($cargo_id) {
    try {
        $cargoData = cargos_call_api('GET', '/cargos/' . $cargo_id);

        $cargo = [
            'id' => isset($cargoData['id']) ? (int) $cargoData['id'] : $cargo_id,
            'codigo_cargo' => $cargoData['codigo_cargo'] ?? '',
            'tipo_cargo_id' => $cargoData['tipo_cargo_id'] ?? null,
            'tipo' => $cargoData['tipo'] ?? ($cargoData['situacion'] ?? null),
            'estado' => $cargoData['estado'] ?? 'activo',
            'observaciones' => $cargoData['observaciones'] ?? null,
            'fecha_inicio' => $cargoData['fecha_inicio'] ?? null,
            'fecha_fin' => $cargoData['fecha_fin'] ?? null,
            'docente_id' => $cargoData['docente_id'] ?? null,
        ];
        $cargo['situacion'] = $cargo['tipo'];

        foreach ($cargoData['relaciones'] ?? [] as $materia) {
            $materiaId = isset($materia['materia_id']) ? (int) $materia['materia_id'] : null;
            if (!$materiaId) {
                continue;
            }

            if (!isset($relaciones[$materiaId])) {
                $relaciones[$materiaId] = [];
            }

            foreach ($materia['cursos'] ?? [] as $curso) {
                $cursoId = isset($curso['curso_id']) ? (int) $curso['curso_id'] : null;
                if (!$cursoId) {
                    continue;
                }

                $horarios = array_map('intval', $curso['horarios'] ?? []);
                $relaciones[$materiaId][$cursoId] = $horarios;
            }
        }
    } catch (RuntimeException $exception) {
        $apiError = 'No se pudo cargar el cargo solicitado: ' . $exception->getMessage();
        $cargo = null;
        $relaciones = [];
    }
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title><?= $cargo ? "Editar Cargo" : "Nuevo Cargo" ?></title>
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
            <a href="cursos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Cursos">
                <span class="text-xl">🏫</span><span class="sidebar-label">Cursos</span>
            </a>
            <a href="alumnos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Alumnos">
                <span class="text-xl">👤</span><span class="sidebar-label">Alumnos</span>
            </a>
            <a href="materias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Materias">
                <span class="text-xl">📚</span><span class="sidebar-label">Materias</span>
            </a>
            <a href="cargos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Cargos">
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

        <div class="max-w-4xl mx-auto bg-white p-6 rounded shadow">
            <h1 class="text-xl font-bold mb-4"><?= $cargo_id ? "Editar Cargo" : "Nuevo Cargo" ?></h1>

            <?php if ($flashError): ?>
                <div class="mb-4 rounded border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                    <?= htmlspecialchars($flashError, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>

            <?php if ($apiError): ?>
                <div class="mb-4 rounded border border-amber-200 bg-amber-50 px-4 py-2 text-sm text-amber-800">
                    <?= htmlspecialchars($apiError, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>

            <div id="form-error" class="hidden mb-4 rounded border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700"></div>

            <!-- ============================= -->
            <!-- FORMULARIO PRINCIPAL DEL CARGO -->
            <!-- ============================= -->
            <form id="form-cargo" method="POST" action="/api/cargos/<?= $cargo_id ? "editar.php" : "crear.php" ?>" class="space-y-4">
                <?php if ($cargo_id): ?>
                    <input type="hidden" name="id" value="<?= $cargo['id'] ?>">
                <?php endif; ?>

                <!-- Tipo de cargo -->
                <div>
                    <label class="block text-sm">Tipo de Cargo</label>
                    <select name="tipo_cargo_id" required class="border rounded p-2 w-full">
                        <option value="">-- Seleccionar --</option>
                        <?php foreach ($tiposCargo as $tipo): ?>
                            <?php $seleccionado = ($cargo && (int) ($cargo['tipo_cargo_id'] ?? 0) === (int) $tipo['id']) ? 'selected' : ''; ?>
                            <option value="<?= $tipo['id'] ?>" <?= $seleccionado ?>>
                                <?= htmlspecialchars($tipo['nombre'], ENT_QUOTES, 'UTF-8') ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <!-- Código -->
                <div>
                    <label class="block text-sm">Código</label>
                    <input type="text" name="codigo_cargo" value="<?= htmlspecialchars($cargo['codigo_cargo'] ?? '') ?>" required class="border rounded p-2 w-full">
                </div>

                <!-- Situación -->
                <div>
                    <label class="block text-sm">Situación</label>
                    <select name="situacion" required class="border rounded p-2 w-full">
                        <?php foreach (['titular', 'interino', 'suplente'] as $s): ?>
                            <option value="<?= $s ?>" <?= ($cargo && $cargo['tipo'] == $s) ? 'selected' : '' ?>><?= ucfirst($s) ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <!-- Estado -->
                <div>
                    <label class="block text-sm">Estado</label>
                    <select name="estado" class="border rounded p-2 w-full">
                        <?php foreach (['activo', 'licencia', 'renuncia', 'inactivo', 'desplazado'] as $e): ?>
                            <option value="<?= $e ?>" <?= ($cargo && $cargo['estado'] == $e) ? 'selected' : '' ?>><?= ucfirst($e) ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <!-- Docente -->
                <div>
                    <label class="block text-sm">Docente</label>
                    <select name="docente_id" class="border rounded p-2 w-full">
                        <option value="">-- Sin asignar --</option>
                        <?php foreach ($docentes as $d): ?>
                            <?php $docenteSeleccionado = ($cargo && (int) ($cargo['docente_id'] ?? 0) === (int) $d['id']) ? 'selected' : ''; ?>
                            <option value="<?= $d['id'] ?>" <?= $docenteSeleccionado ?>>
                                <?= htmlspecialchars(trim(($d['apellido'] ?? '') . ', ' . ($d['nombre'] ?? '')), ENT_QUOTES, 'UTF-8') ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <!-- Fechas -->
                <div>
                    <label class="block text-sm">Fecha Inicio</label>
                    <input type="date" name="fecha_inicio" value="<?= $cargo['fecha_inicio'] ?? '' ?>" class="border rounded p-2 w-full">
                </div>
                <div>
                    <label class="block text-sm">Fecha Fin</label>
                    <input type="date" name="fecha_fin" value="<?= $cargo['fecha_fin'] ?? '' ?>" class="border rounded p-2 w-full">
                </div>

                <!-- Observaciones -->
                <div>
                    <label class="block text-sm">Observaciones</label>
                    <textarea name="observaciones" class="border rounded p-2 w-full"><?= $cargo['observaciones'] ?? '' ?></textarea>
                </div>

                <!-- Botón guardar datos principales -->
                <div>
                    <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded">Guardar Datos del Cargo</button>
                </div>
            </form>

            <?php if ($cargo_id): ?>
                <!-- ============================= -->
                <!-- HORARIOS DEL CARGO -->
                <!-- ============================= -->
                <div class="mt-8">
                    <h2 class="font-bold mb-2">Definir horarios del cargo</h2>
                    <div id="bloques-horarios" class="space-y-3"></div>
                    <button type="button" onclick="agregarBloqueHorario()" class="mt-2 px-3 py-1 bg-indigo-600 text-white rounded">➕ Agregar más horarios</button>
                    <button type="button" onclick="cargarHorariosCargo()" class="mt-2 px-3 py-1 bg-blue-600 text-white rounded">Cargar horarios de cargo</button>
                </div>


                <!-- ============================= -->
                <!-- MATERIAS Y CURSOS -->
                <!-- ============================= -->
                <div class="mt-8">
                    <h2 class="font-bold mb-2">Materias y Cursos</h2>
                    <div id="materias-container" class="space-y-4"></div>
                    <button type="button" onclick="agregarMateria()" class="mt-2 px-3 py-1 bg-green-600 text-white rounded">➕ Agregar materia</button>
                    <div class="mt-4">
                        <button type="button" onclick="guardarRelaciones()"
                            class="px-4 py-2 bg-purple-600 text-white rounded">
                            Guardar Materias y Cursos
                        </button>
                    </div>
                </div>
            <?php endif; ?>
        </div>
    </main>

    <!-- Templates -->
    <template id="horario-template">
        <div class="bloque-horario border p-3 rounded bg-gray-50 relative">
            <button type="button" onclick="eliminarBloqueHorario(this)" class="absolute top-2 right-2 text-red-600 text-sm">🗑</button>

            <!-- Checkboxes de días en una sola fila -->
            <div class="flex flex-wrap gap-4 mb-2">
                <?php foreach (['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'] as $d): ?>
                    <label class="flex items-center gap-1">
                        <input type="checkbox" class="dia-cargo" value="<?= $d ?>"> <?= $d ?>
                    </label>
                <?php endforeach; ?>
            </div>

            <!-- Rango horario y tipo -->
            <div class="flex gap-2">
                <select class="hora-inicio border rounded p-1">
                    <?php
                    for ($h = 7; $h <= 17; $h++):
                        for ($m = 0; $m < 60; $m += 5):
                            // Saltar los minutos antes de 07:40
                            if ($h === 7 && $m < 40) continue;
                            // Saltar los minutos después de 17:40
                            if ($h === 17 && $m > 40) continue;

                            $val = str_pad($h, 2, '0', STR_PAD_LEFT) . ":" . str_pad($m, 2, '0', STR_PAD_LEFT);
                    ?>
                            <option value="<?= $val ?>"><?= $val ?></option>
                    <?php
                        endfor;
                    endfor;
                    ?>
                </select>
                <select class="hora-fin border rounded p-1">
                    <?php
                    for ($h = 7; $h <= 17; $h++):
                        for ($m = 0; $m < 60; $m += 5):
                            // Saltar los minutos antes de 07:40
                            if ($h === 7 && $m < 40) continue;
                            // Saltar los minutos después de 17:40
                            if ($h === 17 && $m > 40) continue;

                            $val = str_pad($h, 2, '0', STR_PAD_LEFT) . ":" . str_pad($m, 2, '0', STR_PAD_LEFT);
                    ?>
                            <option value="<?= $val ?>"><?= $val ?></option>
                    <?php
                        endfor;
                    endfor;
                    ?>
                </select>
                <select class="tipo border rounded p-1">
                    <option value="clase">Clase</option>
                    <option value="extraclase">Extraclase</option>
                </select>
            </div>
        </div>
    </template>

    <template id="materia-template">
        <div class="materia border p-3 rounded bg-gray-50" data-index="">
            <div>
                <label>Materia</label>
                <select class="materia-select border rounded p-2 w-full">
                    <option value="">-- Seleccionar --</option>
                    <?php foreach ($materias as $m): ?>
                        <option value="<?= $m['id'] ?>">
                            <?= htmlspecialchars($m['nombre'], ENT_QUOTES, 'UTF-8') ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="cursos space-y-2 mt-2"></div>
            <button type="button" onclick="agregarCurso(this)" class="mt-2 px-3 py-1 bg-indigo-600 text-white rounded">➕ Agregar curso</button>
        </div>
    </template>

    <template id="curso-template">
        <div class="curso border p-2 rounded">
            <label>Curso</label>
            <select class="curso-select border rounded p-2 w-full">
                <option value="">-- Seleccionar curso --</option>
                <?php foreach ($cursos as $c): ?>
                    <option value="<?= $c['id'] ?>">
                        <?= htmlspecialchars($c['nombre'], ENT_QUOTES, 'UTF-8') ?>
                    </option>
                <?php endforeach; ?>
            </select>
            <div class="mt-2 horarios-list flex flex-col gap-1"></div>
        </div>
    </template>

    <script>
        const cargoId = <?= $cargo_id ?? 'null' ?>;
        document.addEventListener("DOMContentLoaded", () => {
            if (cargoId) {
                cargarListaHorarios();
            }

            const form = document.getElementById("form-cargo");
            if (form) {
                form.addEventListener("submit", async (event) => {
                    event.preventDefault();

                    const errorBox = document.getElementById("form-error");
                    if (errorBox) {
                        errorBox.classList.add("hidden");
                        errorBox.textContent = "";
                    }

                    const formData = new FormData(form);

                    try {
                        const response = await fetch(form.action, {
                            method: "POST",
                            headers: {
                                'Accept': 'application/json'
                            },
                            body: formData
                        });
                        const data = await response.json();

                        if (!response.ok || (data && data.ok === false)) {
                            const message = (data && data.error) ? data.error : 'No se pudo guardar el cargo.';
                            throw new Error(message);
                        }

                        const target = cargoId
                            ? '/users/admin/cargos.php?msg=editado'
                            : '/users/admin/cargos.php?msg=cargo_creado';
                        window.location.href = target;
                    } catch (error) {
                        if (errorBox) {
                            errorBox.textContent = error.message || 'Error inesperado al guardar el cargo.';
                            errorBox.classList.remove("hidden");
                        }
                    }
                });
            }
        });

        function agregarBloqueHorario() {
            const tpl = document.getElementById("horario-template");
            document.getElementById("bloques-horarios").appendChild(tpl.content.cloneNode(true));
        }

        function eliminarBloqueHorario(btn) {
            btn.closest(".bloque-horario").remove();
        }

        function cargarHorariosCargo() {
            document.querySelectorAll(".bloque-horario").forEach(b => {
                let dias = [];
                b.querySelectorAll(".dia-cargo:checked").forEach(cb => dias.push(cb.value));
                if (dias.length === 0) return;
                const horaInicio = b.querySelector(".hora-inicio").value;
                const horaFin = b.querySelector(".hora-fin").value;
                const tipo = b.querySelector(".tipo").value;
                const data = new FormData();
                data.append("cargo_id", cargoId);
                data.append("dias", JSON.stringify(dias));
                data.append("hora_inicio", horaInicio);
                data.append("hora_fin", horaFin);
                data.append("tipo", tipo);
                fetch("/api/cargos/agregar_horario.php", {
                    method: "POST",
                    body: data
                })
            });
        }

        function cargarListaHorarios() {
            fetch("/api/cargos/listar_horarios.php?cargo_id=" + cargoId)
                .then(r => r.json())
                .then(res => {
                    if (res.ok) {
                        const grupos = {}; // agrupar por hora_inicio + hora_fin + tipo
                        res.horarios.forEach(h => {
                            const key = h.hora_inicio + "-" + h.hora_fin + "-" + h.tipo;
                            if (!grupos[key]) {
                                grupos[key] = {
                                    hora_inicio: h.hora_inicio.substring(0, 5),
                                    hora_fin: h.hora_fin.substring(0, 5),
                                    tipo: h.tipo,
                                    dias: []
                                };
                            }
                            grupos[key].dias.push(h.dia_semana);
                        });

                        const cont = document.getElementById("bloques-horarios");
                        cont.innerHTML = "";
                        Object.values(grupos).forEach(g => {
                            const tpl = document.getElementById("horario-template");
                            const clone = tpl.content.cloneNode(true);
                            clone.querySelector(".hora-inicio").value = g.hora_inicio;
                            clone.querySelector(".hora-fin").value = g.hora_fin;
                            clone.querySelector(".tipo").value = g.tipo;

                            clone.querySelectorAll(".dia-cargo").forEach(cb => {
                                if (g.dias.includes(cb.value)) cb.checked = true;
                            });

                            cont.appendChild(clone);
                        });
                    }
                });
        }

        let materiaIndex = 0;

        function agregarMateria() {
            const tpl = document.getElementById("materia-template");
            const cont = document.getElementById("materias-container");
            const clone = tpl.content.cloneNode(true);
            const materia = clone.querySelector(".materia");
            materia.dataset.index = materiaIndex;
            materia.querySelector(".materia-select").name = `materias[${materiaIndex}][id]`;
            cont.appendChild(clone);
            materiaIndex++;
        }

        if (cargoId) {
            const relaciones = <?= json_encode($relaciones) ?>;
            for (let materiaId in relaciones) {
                const mIndex = materiaIndex;
                agregarMateria();
                const materiaEl = document.querySelector(`.materia[data-index="${mIndex}"]`);
                materiaEl.querySelector(".materia-select").value = materiaId;

                for (let cursoId in relaciones[materiaId]) {
                    const horarios = relaciones[materiaId][cursoId];
                    agregarCurso(materiaEl.querySelector("button"), horarios);
                    const cursoEl = materiaEl.querySelector(".cursos .curso:last-child");
                    cursoEl.querySelector(".curso-select").value = cursoId;
                }
            }
        }

        function agregarCurso(btn, horariosMarcados = []) {
            const materia = btn.closest(".materia");
            const mIndex = materia.dataset.index;
            const cont = materia.querySelector(".cursos");
            const tpl = document.getElementById("curso-template");
            const clone = tpl.content.cloneNode(true);
            const curso = clone.querySelector(".curso");
            const select = curso.querySelector(".curso-select");
            const cIndex = cont.children.length;
            select.name = `materias[${mIndex}][cursos][${cIndex}][id]`;

            const hCont = curso.querySelector(".horarios-list");
            fetch("/api/cargos/listar_horarios.php?cargo_id=" + cargoId)
                .then(r => r.json())
                .then(res => {
                    if (res.ok) {
                        res.horarios.filter(h => h.tipo === "clase").forEach(h => {
                            const label = document.createElement("label");
                            label.className = "flex items-center gap-1 text-sm";
                            const cb = document.createElement("input");
                            cb.type = "checkbox";
                            cb.name = `materias[${mIndex}][cursos][${cIndex}][horarios][]`;
                            cb.value = h.id;
                            if (horariosMarcados.includes(parseInt(h.id))) {
                                cb.checked = true; // ✅ marcar si ya estaba guardado
                            }
                            label.appendChild(cb);
                            label.appendChild(document.createTextNode(
                                `${h.dia_semana} ${h.hora_inicio.substring(0,5)}-${h.hora_fin.substring(0,5)}`
                            ));
                            hCont.appendChild(label);
                        });
                    }
                });

            cont.appendChild(clone);
        }
    </script>
    <script>
        function guardarRelaciones() {
            const data = new FormData();
            data.append("id", cargoId);

            let materias = [];
            document.querySelectorAll(".materia").forEach((m) => {
                let mat = {
                    id: m.querySelector(".materia-select").value,
                    cursos: []
                };
                m.querySelectorAll(".curso").forEach((c) => {
                    let curso = {
                        id: c.querySelector(".curso-select").value,
                        horarios: []
                    };
                    c.querySelectorAll("input[type=checkbox]:checked").forEach(cb => {
                        curso.horarios.push(cb.value);
                    });
                    mat.cursos.push(curso);
                });
                materias.push(mat);
            });

            data.append("materias", JSON.stringify(materias));

            fetch("/api/cargos/guardar_relaciones.php", {
                    method: "POST",
                    body: data
                })
                .then(r => r.json())
                .then(res => {
                    if (res.ok) {
                        alert("✅ Relaciones guardadas");
                        window.location.href = "/users/admin/cargos.php?msg=relaciones_guardadas";
                    } else {
                        alert("❌ Error: " + res.error);
                        console.error(res);
                    }
                })
                .catch(err => console.error("Fetch error:", err));
        }
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
        function agregarHorarioMateria(btn) {
            const materia = btn.closest('.materia');
            const mIndex = materia.dataset.index;
            const cont = materia.querySelector('.horarios-materia');
            const tpl = document.getElementById('horario-materia-template');
            const clone = tpl.content.cloneNode(true);

            // Asignar names a los inputs
            clone.querySelector('.dia-materia').name = `materias[${mIndex}][horarios][dia][]`;
            clone.querySelector('.inicio-materia').name = `materias[${mIndex}][horarios][inicio][]`;
            clone.querySelector('.fin-materia').name = `materias[${mIndex}][horarios][fin][]`;
            clone.querySelector('.tipo-materia').name = `materias[${mIndex}][horarios][tipo][]`;

            cont.appendChild(clone);
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