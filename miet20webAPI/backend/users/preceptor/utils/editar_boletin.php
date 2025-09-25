<?php
session_start();
if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 2) {
    header('Location: /login.php?error=rol');
    exit;
}

$usuario = $_SESSION['usuario'];

require_once __DIR__ . '/../../../includes/api_client.php';
require_once __DIR__ . '/boletin_helpers.php';

$curso_id = $_GET['curso_id'] ?? null;
$alumno_id = $_GET['alumno_id'] ?? null;
$nuevo = isset($_GET['nuevo']);
$boletin_id = $_GET['boletin_id'] ?? null;

$cursoIdInt = $curso_id ? (int) $curso_id : 0;
$alumnoIdInt = $alumno_id ? (int) $alumno_id : 0;
$boletinIdInt = $boletin_id ? (int) $boletin_id : 0;

$materias = $cursoIdInt ? obtenerMateriasCurso(null, $cursoIdInt) : [];

$nombre = '';
$apellido = '';
$dni = '';
$anio = '';
$division = '';
$error = null;

if ($cursoIdInt > 0) {
    try {
        $cursoResponse = miEt20ApiAuthenticatedRequest('GET', '/courses/' . $cursoIdInt);
        $cursoData = $cursoResponse['data'] ?? null;
        if (is_array($cursoData)) {
            $anio = $cursoData['anio'] ?? ($cursoData['year'] ?? '');
            $division = $cursoData['division'] ?? ($cursoData['division'] ?? '');
        }
    } catch (RuntimeException $exception) {
        $error = $error ?? $exception->getMessage();
    }
}

if ($alumnoIdInt > 0) {
    try {
        $alumnoResponse = miEt20ApiAuthenticatedRequest('GET', '/students/' . $alumnoIdInt);
        $alumnoData = $alumnoResponse['data'] ?? null;
        if (is_array($alumnoData)) {
            $nombre = $alumnoData['nombre'] ?? ($alumnoData['firstName'] ?? '');
            $apellido = $alumnoData['apellido'] ?? ($alumnoData['lastName'] ?? '');
            $dni = $alumnoData['dni'] ?? '';
        }
    } catch (RuntimeException $exception) {
        $error = $error ?? $exception->getMessage();
    }
}

if ($nuevo && $cursoIdInt && $alumnoIdInt) {
    $periodo_actual = obtenerUltimoPeriodoConNotas(null, $cursoIdInt, $alumnoIdInt);
    $faltantesNotas = [];

    if ($periodo_actual) {
        $faltantesNotas = obtenerMateriasSinNotasPorPeriodo(null, $cursoIdInt, $alumnoIdInt, $periodo_actual);
    } elseif (!empty($materias)) {
        foreach ($materias as $materia) {
            $faltantesNotas[] = $materia['nombre'];
        }
    }

    if (!$periodo_actual || !empty($faltantesNotas)) {
        $query = [
            'curso_id' => $curso_id,
            'alumno_id' => $alumno_id,
            'faltan_notas' => 1,
        ];
        if ($periodo_actual) {
            $query['periodo'] = $periodo_actual;
        }

        header('Location: boletines.php?' . http_build_query($query));
        exit;
    }

    try {
        $createResponse = miEt20ApiAuthenticatedRequest('POST', '/report-cards', [
            'studentId' => $alumnoIdInt,
            'courseId' => $cursoIdInt,
            'term' => $periodo_actual,
            'academicYear' => (int) date('Y'),
        ]);

        $created = $createResponse['data'] ?? [];
        if (is_array($created) && isset($created['id'])) {
            $boletinIdInt = (int) $created['id'];
            header("Location: editar_boletin.php?curso_id=$cursoIdInt&alumno_id=$alumnoIdInt&boletin_id=$boletinIdInt");
            exit;
        }

        $error = 'No se pudo crear el boletín.';
    } catch (RuntimeException $exception) {
        $error = $exception->getMessage();
    }
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $boletinIdInt > 0) {
    $observaciones = $_POST['observaciones'] ?? '';

    $gradesPayload = [];
    if (isset($_POST['notas']) && is_array($_POST['notas'])) {
        foreach ($_POST['notas'] as $califId => $nota) {
            $calificacionId = (int) $califId;
            $subjectId = isset($nota['subject_id']) ? (int) $nota['subject_id'] : 0;

            if ($calificacionId <= 0 || $subjectId <= 0) {
                continue;
            }

            $notaNumerica = $nota['numerica'] ?? '';
            $notaNumerica = $notaNumerica === '' ? null : (float) $notaNumerica;
            $notaConceptual = isset($nota['conceptual']) && $nota['conceptual'] !== ''
                ? trim((string) $nota['conceptual'])
                : null;
            $observacionMateria = isset($nota['observaciones']) && $nota['observaciones'] !== ''
                ? trim((string) $nota['observaciones'])
                : null;

            $gradesPayload[] = [
                'id' => $calificacionId,
                'subjectId' => $subjectId,
                'numericGrade' => $notaNumerica,
                'conceptualGrade' => $notaConceptual,
                'observations' => $observacionMateria,
            ];
        }
    }

    try {
        miEt20ApiAuthenticatedRequest('PUT', '/report-cards/' . $boletinIdInt, [
            'observations' => $observaciones,
            'grades' => $gradesPayload,
        ]);

        header("Location: editar_boletin.php?curso_id=$curso_id&alumno_id=$alumno_id&boletin_id=$boletinIdInt&guardado=1");
        exit;
    } catch (RuntimeException $exception) {
        $error = $exception->getMessage();
    }
}

$boletin = [];
$calificaciones = [];

if ($boletinIdInt > 0) {
    try {
        $boletinResponse = miEt20ApiAuthenticatedRequest('GET', '/report-cards/' . $boletinIdInt);
        $boletinData = $boletinResponse['data'] ?? null;

        if (is_array($boletinData)) {
            $boletin = [
                'id' => $boletinIdInt,
                'periodo' => $boletinData['term'] ?? '',
                'estado' => $boletinData['status'] ?? 'draft',
                'anio_lectivo' => $boletinData['academicYear'] ?? null,
                'observaciones' => $boletinData['observations'] ?? '',
            ];

            if (isset($boletinData['course']) && is_array($boletinData['course'])) {
                $anio = $anio ?: ($boletinData['course']['year'] ?? '');
                $division = $division ?: ($boletinData['course']['division'] ?? '');
            }

            if (isset($boletinData['student']) && is_array($boletinData['student'])) {
                $nombre = $nombre ?: ($boletinData['student']['firstName'] ?? '');
                $apellido = $apellido ?: ($boletinData['student']['lastName'] ?? '');
                $dni = $dni ?: ($boletinData['student']['dni'] ?? '');
            }

            if (!empty($boletinData['grades']) && is_array($boletinData['grades'])) {
                foreach ($boletinData['grades'] as $grade) {
                    if (!is_array($grade)) {
                        continue;
                    }

                    $gradeId = isset($grade['id']) ? (int) $grade['id'] : 0;
                    $subjectId = isset($grade['subjectId']) ? (int) $grade['subjectId'] : 0;
                    $subjectName = $grade['subjectName'] ?? '';

                    if ($gradeId <= 0 || $subjectId <= 0) {
                        continue;
                    }

                    $notaNumerica = $grade['numericGrade'];
                    if ($notaNumerica === null || $notaNumerica === '') {
                        $notaNumerica = '';
                    }

                    $calificaciones[] = [
                        'id' => $gradeId,
                        'materia_id' => $subjectId,
                        'nombre' => $subjectName,
                        'nota_numerica' => $notaNumerica,
                        'nota_conceptual' => $grade['conceptualGrade'] ?? '',
                        'observaciones' => $grade['observations'] ?? '',
                    ];
                }
            }
        }
    } catch (RuntimeException $exception) {
        $error = $error ?? $exception->getMessage();
    }
}

$guardado = $_GET['guardado'] ?? null;
$cursoEtiqueta = trim(($anio !== '' ? $anio . '°' : '') . ($division !== '' ? ' ' . $division : ''));
$estadoBoletin = is_array($boletin) && isset($boletin['estado']) ? $boletin['estado'] : 'draft';
$estadoEtiqueta = [
    'draft' => 'Borrador',
    'published' => 'Publicado',
    'archived' => 'Archivado',
][$estadoBoletin] ?? ucfirst((string) $estadoBoletin);
$periodoEtiqueta = is_array($boletin) && isset($boletin['periodo']) ? $boletin['periodo'] : '';
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Editar Boletín</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Poppins', sans-serif;
        }
    </style>
</head>

<body class="bg-gray-100 min-h-screen flex">
    <nav class="w-60 bg-white shadow-lg px-6 py-8 flex flex-col gap-2">
        <div class="flex justify-center items-center p-2 mb-4 border-b border-gray-400">
            <img src="/images/et20ico.ico" class="block items-center h-28 w-28">
        </div>
        <div class="flex items-center mb-10 gap-2">
            <img src="<?php echo $usuario['foto_url'] ?? 'https://ui-avatars.com/api/?name=' . $usuario['nombre']; ?>" class="rounded-full w-14 h-14">
            <div class="flex flex-col pl-3">
                <div class="font-bold text-lg leading-tight"><?php echo $usuario['nombre']; ?></div>
                <div class="font-bold text-lg leading-tight"><?php echo $usuario['apellido']; ?></div>
                <div class="mt-2 text-xs text-gray-500">Preceptor/a</div>
            </div>
        </div>
        <a href="/users/preceptor/preceptor.php" class="py-2 px-3 rounded-xl text-gray-700 hover:bg-gray-200 transition">🏠 Inicio</a>
        <a href="/users/preceptor/asistencias.php" class="py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100">📆 Asistencias</a>
        <a href="/users/preceptor/calificaciones.php" class="py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100">📝 Calificaciones</a>
        <a href="/users/preceptor/boletines.php" class="py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-indigo-100">📑 Boletines</a>
        <?php if (isset($_SESSION['roles_disponibles']) && count($_SESSION['roles_disponibles']) > 1): ?>
            <form method="post" action="/includes/cambiar_rol.php" class="mt-auto mb-3">
                <select name="rol" onchange="this.form.submit()" class="w-full px-3 py-2 border text-sm rounded-xl text-gray-700 bg-white">
                    <?php foreach ($_SESSION['roles_disponibles'] as $r): ?>
                        <option value="<?php echo $r['id']; ?>" <?php if ($_SESSION['usuario']['rol'] == $r['id']) echo 'selected'; ?>>
                            Cambiar a: <?php echo ucfirst($r['nombre']); ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            </form>
        <?php endif; ?>
        <button onclick="window.location='/includes/logout.php'" class="mt-auto py-2 px-3 rounded-xl text-white bg-red-500 hover:bg-red-600">Salir</button>
    </nav>
    <main class="flex-1 p-10 max-w-4xl">
        <h1 class="text-2xl font-bold mb-4">Boletín de <?php echo htmlspecialchars(trim("$apellido, $nombre")); ?></h1>
        <div class="mb-4">
            <?php echo htmlspecialchars($cursoEtiqueta ?: ''); ?> |
            <b>DNI:</b> <?php echo htmlspecialchars($dni); ?> |
            <b>Año lectivo:</b> <?php echo htmlspecialchars((string) ($boletin['anio_lectivo'] ?? date('Y'))); ?> |
            <b>Periodo:</b> <?php echo htmlspecialchars($periodoEtiqueta ?: ''); ?> |
            <b>Estado:</b> <?php echo htmlspecialchars($estadoEtiqueta); ?>
        </div>
        <?php if ($error): ?>
            <div class="bg-red-100 text-red-800 rounded-xl p-3 mb-4"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        <?php if ($guardado): ?>
            <div class="bg-green-100 text-green-800 rounded-xl p-3 mb-4">Boletín guardado.</div>
        <?php endif; ?>

        <?php if ($boletin && $boletin['estado'] == 'borrador'): ?>
            <form method="post">
                <div class="overflow-x-auto mb-6">
                    <table class="min-w-full bg-white rounded-xl shadow">
                        <thead>
                            <tr>
                                <th class="py-2 px-4">Materia</th>
                                <th class="py-2 px-4">Nota numérica</th>
                                <th class="py-2 px-4">Nota conceptual</th>
                                <th class="py-2 px-4">Observaciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($calificaciones as $c): ?>
                                <tr>
                                    <td class="py-2 px-4"><?php echo htmlspecialchars($c['nombre']); ?></td>
                                    <td class="py-2 px-4">
                                        <input type="hidden" name="notas[<?php echo $c['id']; ?>][subject_id]" value="<?php echo (int) $c['materia_id']; ?>">
                                        <input type="number" min="1" max="10" step="0.01" name="notas[<?php echo $c['id']; ?>][numerica]" value="<?php echo htmlspecialchars((string) $c['nota_numerica']); ?>" class="w-20 border rounded px-2">
                                    </td>
                                    <td class="py-2 px-4"><input type="text" name="notas[<?php echo $c['id']; ?>][conceptual]" value="<?php echo htmlspecialchars($c['nota_conceptual']); ?>" class="w-24 border rounded px-2"></td>
                                    <td class="py-2 px-4"><input type="text" name="notas[<?php echo $c['id']; ?>][observaciones]" value="<?php echo htmlspecialchars($c['observaciones']); ?>" class="w-40 border rounded px-2"></td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
                <div class="mb-4">
                    <label class="font-bold">Observaciones generales:</label><br>
                    <textarea name="observaciones" rows="3" class="w-full border rounded px-2 py-1"><?php echo htmlspecialchars($boletin['observaciones'] ?? ''); ?></textarea>
                </div>
                <button type="submit" class="bg-indigo-600 text-white px-6 py-2 rounded-xl hover:bg-indigo-700 font-bold">Guardar cambios</button>
                <a href="/users/preceptor/boletines.php?curso_id=<?php echo $curso_id; ?>&alumno_id=<?php echo $alumno_id; ?>" class="ml-4 text-gray-600 hover:underline">Volver</a>
            </form>
        <?php elseif ($boletin): ?>
            <!-- Boletín en modo solo lectura -->
            <div class="overflow-x-auto mb-6">
                <table class="min-w-full bg-white rounded-xl shadow">
                    <thead>
                        <tr>
                            <th class="py-2 px-4">Materia</th>
                            <th class="py-2 px-4">Nota numérica</th>
                            <th class="py-2 px-4">Nota conceptual</th>
                            <th class="py-2 px-4">Observaciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($calificaciones as $c): ?>
                            <tr>
                                <td class="py-2 px-4"><?php echo htmlspecialchars($c['nombre']); ?></td>
                                <td class="py-2 px-4"><?php echo htmlspecialchars((string) $c['nota_numerica']); ?></td>
                                <td class="py-2 px-4"><?php echo htmlspecialchars($c['nota_conceptual']); ?></td>
                                <td class="py-2 px-4"><?php echo htmlspecialchars($c['observaciones']); ?></td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
            <div class="mb-4">
                <b>Observaciones generales:</b><br>
                <div class="border rounded p-3 bg-gray-50"><?php echo nl2br(htmlspecialchars($boletin['observaciones'] ?? '')); ?></div>
            </div>
            <a href="/users/preceptor/boletines.php?curso_id=<?php echo $curso_id; ?>&alumno_id=<?php echo $alumno_id; ?>" class="text-gray-600 hover:underline">Volver</a>
        <?php endif; ?>
    </main>
</body>
</html>