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

try {
    $courses = admin_call_api('GET', '/cursos');

    if (!is_array($courses)) {
        $courses = [];
    }

    $normalized = array_map(static function ($course) {
        if (!is_array($course)) {
            return [];
        }

        $data = $course;

        $data['id'] = isset($course['id']) ? (int) $course['id'] : null;
        $data['anio'] = isset($course['anio']) ? (int) $course['anio'] : null;
        $division = isset($course['division']) ? trim((string) $course['division']) : '';
        $data['division'] = $division;
        $anio = $data['anio'];

        $nombre = isset($course['nombre']) ? trim((string) $course['nombre']) : '';
        if ($nombre === '') {
            $parts = [];
            if ($anio !== null && $anio > 0) {
                $parts[] = $anio . '°';
            }
            if ($division !== '') {
                $parts[] = $division;
            }
            $nombre = trim(implode(' ', $parts));
        }
        $data['nombre'] = $nombre;

        return $data;
    }, $courses);

    echo json_encode($normalized, JSON_UNESCAPED_UNICODE);
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    http_response_code($status);
    echo json_encode([
        'error' => $exception->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}