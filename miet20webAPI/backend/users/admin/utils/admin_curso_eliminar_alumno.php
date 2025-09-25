<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}
require_once __DIR__ . '/api_client.php';

$csrfToken = $_POST['csrf'] ?? '';
if (!isset($_SESSION['csrf']) || !hash_equals($_SESSION['csrf'], $csrfToken)) {
    header('Location: /users/admin/cursos.php?error=csrf');
    exit;
}

$cursoId = isset($_POST['curso_id']) ? (int) $_POST['curso_id'] : 0;
$alumnoId = isset($_POST['alumno_id']) ? (int) $_POST['alumno_id'] : 0;

if ($cursoId <= 0 || $alumnoId <= 0) {
    header('Location: /users/admin/cursos.php?error=faltan_campos');
    exit;
}

try {
    admin_call_api('DELETE', '/alumnos/' . $alumnoId . '/cursos/' . $cursoId);
    header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&ok=alumno_eliminado');
    exit;
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();
    $error = 'api';

    if ($status === 404) {
        $message = $exception->getMessage();
        if (stripos($message, 'curso') !== false) {
            $error = 'curso_no_encontrado';
        } elseif (stripos($message, 'alumno') !== false) {
            $error = 'alumno_no_encontrado';
        }
        $_SESSION['admin_cursos_error'] = $message;
    } else {
        $_SESSION['admin_cursos_error'] = $exception->getMessage();
    }

    header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&error=' . $error);
    exit;
}
