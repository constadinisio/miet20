<?php
session_start();

if (!isset($_SESSION['usuario']) || (int) $_SESSION['usuario']['rol'] !== 1) {
    http_response_code(403);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['error' => 'No autorizado']);
    exit;
}

require_once __DIR__ . '/api_client.php';

header('Content-Type: application/json; charset=utf-8');

$cursoId = isset($_GET['curso_id']) ? (int) $_GET['curso_id'] : 0;
if ($cursoId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Curso inválido']);
    exit;
}

try {
    $students = admin_call_api('GET', '/cursos/' . $cursoId . '/alumnos');

    if (!is_array($students)) {
        $students = [];
    }

    $normalized = array_map(static function ($student) {
        if (!is_array($student)) {
            return [];
        }

        $data = [];
        $data['id'] = isset($student['id']) ? (int) $student['id'] : null;
        $data['nombre'] = isset($student['nombre']) ? (string) $student['nombre'] : '';
        $data['apellido'] = isset($student['apellido']) ? (string) $student['apellido'] : '';
        $data['dni'] = isset($student['dni']) ? (string) $student['dni'] : '';
        $data['estado_academico'] = isset($student['estado_academico']) ? (string) $student['estado_academico'] : '';
        $data['promedio'] = isset($student['promedio']) && $student['promedio'] !== null
            ? (float) $student['promedio']
            : null;

        return $data;
    }, $students);

    echo json_encode($normalized, JSON_UNESCAPED_UNICODE);
    exit;
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
    exit;
}
