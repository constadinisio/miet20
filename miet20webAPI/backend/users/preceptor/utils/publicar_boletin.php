<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 2) {
    header("Location: /login.php?error=rol");
    exit;
}

require_once __DIR__ . '/../../../includes/api_client.php';
require_once __DIR__ . '/../../../notificaciones/notificaciones_utils.php';

$boletin_id = isset($_GET['id']) ? (int) $_GET['id'] : 0;

if ($boletin_id > 0) {
    try {
        $response = miEt20ApiAuthenticatedRequest('PATCH', '/report-cards/' . $boletin_id . '/status', [
            'status' => 'published',
        ]);

        $reportCard = $response['data'] ?? [];
        if (is_array($reportCard) && ($reportCard['status'] ?? '') === 'published') {
            $student = is_array($reportCard['student'] ?? null) ? $reportCard['student'] : [];
            $studentId = isset($student['id']) ? (int) $student['id'] : null;

            if ($studentId) {
                $nombreAlumno = trim(
                    ($student['firstName'] ?? $student['nombre'] ?? '') .
                    ' ' .
                    ($student['lastName'] ?? $student['apellido'] ?? '')
                );

                if ($nombreAlumno === '') {
                    $nombreAlumno = 'Alumno';
                }

                try {
                    crear_notificacion('boletin_generado', [$nombreAlumno], [$studentId]);
                } catch (Throwable $notificationException) {
                    // Las notificaciones son deseables pero no críticas; se ignoran errores.
                }
            }
        }
    } catch (RuntimeException $exception) {
        // Se ignora el error y se redirige igualmente.
    }
}

header("Location: /users/preceptor/boletines.php");
exit;