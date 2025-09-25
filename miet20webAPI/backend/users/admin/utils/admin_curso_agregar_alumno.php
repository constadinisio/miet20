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
$dni = isset($_POST['dni']) ? preg_replace('/\D+/', '', (string) $_POST['dni']) : '';

if ($cursoId <= 0 || $dni === '') {
    header('Location: /users/admin/cursos.php?error=faltan_campos');
    exit;
}

try {
    $students = admin_call_api('GET', '/usuarios', null, [
        'dni' => $dni,
        'rol' => 4,
    ]);
} catch (RuntimeException $exception) {
    $_SESSION['admin_cursos_error'] = $exception->getMessage();
    header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&error=api');
    exit;
}

$studentId = null;
if (is_array($students)) {
    foreach ($students as $student) {
        if (!is_array($student)) {
            continue;
        }

        $studentDni = isset($student['dni']) ? preg_replace('/\D+/', '', (string) $student['dni']) : '';
        if ($studentDni === $dni) {
            $studentId = isset($student['id']) ? (int) $student['id'] : null;
            break;
        }
    }
}

if (!$studentId) {
    header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&error=alumno_no_encontrado');
    exit;
}

try {
    admin_call_api('POST', '/alumnos/' . $studentId . '/cursos', [
        'curso_id' => $cursoId,
    ]);
    header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&ok=alumno_agregado');
    exit;
} catch (RuntimeException $exception) {
    $status = (int) $exception->getCode();

    if ($status === 404) {
        $message = $exception->getMessage();
        if (stripos($message, 'curso') !== false) {
            $redirectError = 'curso_no_encontrado';
        } else {
            $redirectError = 'alumno_no_encontrado';
        }
        $_SESSION['admin_cursos_error'] = $message;
        header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&error=' . $redirectError);
        exit;
    }

    $_SESSION['admin_cursos_error'] = $exception->getMessage();
    header('Location: /users/admin/cursos.php?curso_id=' . urlencode((string) $cursoId) . '&error=api');
    exit;
}
