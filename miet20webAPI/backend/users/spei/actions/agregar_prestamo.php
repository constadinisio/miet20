<?php
require_once __DIR__ . '/../helpers.php';

try {
    spei_assert_authorized();
} catch (RuntimeException $exception) {
    http_response_code($exception->getCode() ?: 403);
    exit($exception->getMessage());
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit('Método no permitido');
}

$netbookId = strtoupper(trim((string) ($_POST['Netbook_ID'] ?? '')));
$curso = trim((string) ($_POST['Curso'] ?? ''));
$tutor = trim((string) ($_POST['Tutor'] ?? ''));
$alumno = trim((string) ($_POST['Alumno'] ?? ''));
$horaPrestamo = trim((string) ($_POST['Hora_Prestamo'] ?? ''));

try {
    $fechaPrestamo = spei_normalize_date_input($_POST['Fecha_Prestamo'] ?? null);
} catch (RuntimeException $exception) {
    spei_set_flash('error', $exception->getMessage());
    spei_redirect('/users/spei/prestamos.php');
}

if ($netbookId === '' || $curso === '' || $tutor === '' || $alumno === '' || $horaPrestamo === '' || $fechaPrestamo === null) {
    spei_set_flash('error', 'Debés completar todos los campos del préstamo.');
    spei_redirect('/users/spei/prestamos.php');
}

if (!preg_match('/^\d{2}:\d{2}$/', $horaPrestamo)) {
    spei_set_flash('error', 'La hora del préstamo debe tener el formato HH:MM.');
    spei_redirect('/users/spei/prestamos.php');
}

$payload = [
    'Netbook_ID' => $netbookId,
    'dispositivo_codigo' => $netbookId,
    'alumno' => $alumno,
    'curso' => $curso,
    'tutor' => $tutor,
    'fecha_prestamo' => $fechaPrestamo,
    'hora_prestamo' => $horaPrestamo
];

try {
    spei_call_api('POST', '/devices/prestamos', $payload);
    spei_set_flash('success', 'El préstamo se registró correctamente.');
} catch (RuntimeException $exception) {
    spei_set_flash('error', 'No se pudo registrar el préstamo: ' . $exception->getMessage());
}

spei_redirect('/users/spei/prestamos.php');
