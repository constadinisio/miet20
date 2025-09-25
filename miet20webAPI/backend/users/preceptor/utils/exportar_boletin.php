<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] < 1) {
    header("Location: /login.php?error=rol");
    exit;
}
require_once __DIR__ . '/../../../includes/api_client.php';
require_once __DIR__ . '/boletin_helpers.php';
require_once __DIR__ . '/../../../../vendor/autoload.php';

use PhpOffice\PhpSpreadsheet\IOFactory;

$boletin_id = $_GET['id'] ?? null;
if (!$boletin_id) {
    echo "ID de boletín no especificado.";
    exit;
}

// --- Cargar datos de boletín, alumno y calificaciones ---
try {
    $response = miEt20ApiAuthenticatedRequest('GET', '/report-cards/' . $boletin_id . '/export');
} catch (RuntimeException $exception) {
    $statusCode = $exception->getCode();
    if ($statusCode < 100 || $statusCode > 599) {
        $statusCode = 500;
    }
    http_response_code($statusCode);
    header('Content-Type: text/plain; charset=UTF-8');
    echo 'No se pudo obtener el boletín: ' . $exception->getMessage();
    exit;
}

$payload = $response['data'] ?? null;
$reportCard = is_array($payload) ? ($payload['reportCard'] ?? null) : null;
$grades = is_array($payload) ? ($payload['grades'] ?? []) : [];

if (!is_array($reportCard)) {
    http_response_code(404);
    header('Content-Type: text/plain; charset=UTF-8');
    echo 'Boletín no encontrado.';
    exit;
}

$student = is_array($reportCard['student'] ?? null) ? $reportCard['student'] : [];
$course = is_array($reportCard['course'] ?? null) ? $reportCard['course'] : [];

$anioBoletin = isset($reportCard['academicYear']) ? (int) $reportCard['academicYear'] : (int) date('Y');
$term = isset($reportCard['term']) ? (string) $reportCard['term'] : '';
$faltantesNotas = [];
if ($term !== '') {
    $faltantesNotas = obtenerMateriasSinNotasPorPeriodo(
        null,
        (int) ($reportCard['courseId'] ?? 0),
        (int) ($reportCard['studentId'] ?? 0),
        $term,
        $anioBoletin
    );
}

if (!empty($faltantesNotas)) {
    $faltantesNotas = array_values(array_unique($faltantesNotas));
    sort($faltantesNotas, SORT_NATURAL | SORT_FLAG_CASE);
    $materiasPendientes = array_map(function ($nombre) {
        return htmlspecialchars($nombre, ENT_QUOTES, 'UTF-8');
    }, $faltantesNotas);

    http_response_code(409);
    header('Content-Type: text/html; charset=UTF-8');
    echo 'No se puede exportar el boletín. Faltan notas de ' . htmlspecialchars($term, ENT_QUOTES, 'UTF-8') . ' en: ' . implode(', ', $materiasPendientes);
    exit;
}

$backendDir   = dirname(__DIR__, 3); // sube desde .../backend/users/preceptor/utils -> .../backend
$templatePath = $backendDir . '/utils/plantillas/PlantillaBoletines.xlsx';
$spreadsheet = IOFactory::load($templatePath);
$sheet = $spreadsheet->getActiveSheet();

// Rellenar encabezado
$cursoAnio = isset($course['year']) ? (string) $course['year'] : '';
$cursoDivision = isset($course['division']) ? (string) $course['division'] : '';
$cursoLabel = '';
if ($cursoAnio !== '') {
    $cursoLabel .= $cursoAnio . '°';
}
$cursoLabel .= $cursoDivision;
$sheet->setCellValue('C7', $cursoLabel);

$alumnoApellido = $student['lastName'] ?? '';
$alumnoNombre = $student['firstName'] ?? '';
$sheet->setCellValue('D7', trim($alumnoApellido . ', ' . $alumnoNombre, ' ,'));

$dni = $student['dni'] ?? '';
if ($dni !== '') {
    $sheet->setCellValueExplicit('J7', (string) $dni, \PhpOffice\PhpSpreadsheet\Cell\DataType::TYPE_STRING);
}

$codigo = $student['code'] ?? '';
if ($codigo !== '') {
    $sheet->setCellValue('L7', $codigo);
}

// Rellenar materias (fila 10 en adelante)
$fila = 10;
if (!is_array($grades)) {
    $grades = [];
}

foreach ($grades as $grade) {
    if (!is_array($grade)) {
        continue;
    }

    $subjectName = isset($grade['subjectName']) ? (string) $grade['subjectName'] : '';
    $numericGrade = $grade['numericGrade'] ?? $grade['nota_numerica'] ?? null;
    $conceptualGrade = $grade['conceptualGrade'] ?? $grade['nota_conceptual'] ?? null;

    $sheet->setCellValue('A' . $fila, $subjectName);
    if ($numericGrade !== null && $numericGrade !== '') {
        $sheet->setCellValue('J' . $fila, $numericGrade);
    }
    if ($conceptualGrade !== null && $conceptualGrade !== '') {
        $sheet->setCellValue('M' . $fila, $conceptualGrade);
    }
    $fila++;
}

// Descargar Excel
$writer = IOFactory::createWriter($spreadsheet, 'Xlsx');
header('Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
$nombreArchivo = 'Boletin_' . preg_replace('/[^A-Za-z0-9_-]+/', '_', $alumnoApellido) . '_' . preg_replace('/[^A-Za-z0-9_-]+/', '_', $alumnoNombre) . '.xlsx';
header('Content-Disposition: attachment;filename="' . $nombreArchivo . '"');
header('Cache-Control: max-age=0');
$writer->save('php://output');
exit;
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Boletín PDF</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #fff;
        }

        .title {
            font-size: 2em;
            font-weight: bold;
        }

        .info {
            margin-bottom: 20px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        th,
        td {
            border: 1px solid #888;
            padding: 6px;
        }

        th {
            background: #f0f0f0;
        }
    </style>
</head>

<body>
    <div class="title">Boletín de Calificaciones</div>
    <div class="info">
        <b>Alumno:</b> <?php echo $alumno['apellido'] . ", " . $alumno['nombre']; ?><br>
        <b>DNI:</b> <?php echo $alumno['dni']; ?><br>
        <b>Curso:</b> <?php echo $curso['anio'] . "°" . $curso['division']; ?><br>
        <b>Año lectivo:</b> <?php echo $boletin['anio_lectivo']; ?><br>
        <b>Periodo:</b> <?php echo $boletin['periodo']; ?><br>
        <b>Estado:</b> <?php echo ucfirst($boletin['estado']); ?><br>
        <b>Fecha emisión:</b> <?php echo $boletin['fecha_emision'] ? date('d/m/Y', strtotime($boletin['fecha_emision'])) : "-"; ?>
    </div>
    <table>
        <thead>
            <tr>
                <th>Materia</th>
                <th>Nota numérica</th>
                <th>Nota conceptual</th>
                <th>Observaciones</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($calificaciones as $c): ?>
                <tr>
                    <td><?php echo $c['nombre']; ?></td>
                    <td><?php echo $c['nota_numerica']; ?></td>
                    <td><?php echo htmlspecialchars($c['nota_conceptual']); ?></td>
                    <td><?php echo htmlspecialchars($c['observaciones']); ?></td>
                </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
    <div style="margin-top:20px">
        <b>Observaciones generales:</b><br>
        <?php echo nl2br(htmlspecialchars($boletin['observaciones'] ?? '')); ?>
    </div>
</body>
</html>