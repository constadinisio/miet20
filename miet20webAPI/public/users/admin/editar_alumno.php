<?php
session_start();
if (!isset($_SESSION['usuario']) || (int)$_SESSION['usuario']['rol'] !== 1) {
    header("Location: /login.php?error=rol");
    exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];

$usuario = $_SESSION['usuario'];
require_once __DIR__ . '/../../../backend/includes/db.php';

$alumno_id = $_GET['id'] ?? null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $alumno_id = $_POST['alumno_id'];
    $nombre = $_POST['nombre'];
    $apellido = $_POST['apellido'];
    $dni = $_POST['dni'];
    $mail = $_POST['mail'];
    $anio = $_POST['anio'];
    $division = $_POST['division'];

    $sql = "UPDATE usuarios SET nombre=?, apellido=?, dni=?, mail=?, anio=?, division=? WHERE id=?";
    $stmt = $conexion->prepare($sql);
    $stmt->bind_param("ssssssi", $nombre, $apellido, $dni, $mail, $anio, $division, $alumno_id);
    $stmt->execute();
    $stmt->close();

    header("Location: alumnos.php");
    exit;
}

// Traer datos actuales
$alumno = null;
if ($alumno_id) {
    $sql = "SELECT id, nombre, apellido, dni, mail, anio, division FROM usuarios WHERE id = ?";
    $stmt = $conexion->prepare($sql);
    $stmt->bind_param("i", $alumno_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $alumno = $result->fetch_assoc();
    $stmt->close();
}
?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Editar Alumno</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
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
            <a href="alumnos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Alumnos">
                <span class="text-xl">👤</span><span class="sidebar-label">Alumnos</span>
            </a>
            <a href="materias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Materias">
                <span class="text-xl">📚</span><span class="sidebar-label">Materias</span>
            </a>
            <a href="horarios.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Horarios">
                <span class="text-xl">⏰</span><span class="sidebar-label">Horarios</span>
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
    <main id="content" class="flex-1 p-10">
        <h1 class="text-2xl font-bold mb-6">Editar Alumno</h1>
        <?php if ($alumno): ?>
            <form method="post" class="bg-white rounded-xl p-8 shadow flex flex-col gap-4 max-w-xl">
                <input type="hidden" name="alumno_id" value="<?php echo $alumno['id']; ?>">
                <input type="hidden" name="csrf" value="<?= $csrf ?>">
                <div>
                    <label class="font-bold">Nombre:</label>
                    <input type="text" name="nombre" value="<?php echo htmlspecialchars($alumno['nombre']); ?>" class="border rounded px-4 py-2 w-full" required>
                </div>
                <div>
                    <label class="font-bold">Apellido:</label>
                    <input type="text" name="apellido" value="<?php echo htmlspecialchars($alumno['apellido']); ?>" class="border rounded px-4 py-2 w-full" required>
                </div>
                <div>
                    <label class="font-bold">DNI:</label>
                    <input type="text" name="dni" value="<?php echo htmlspecialchars($alumno['dni']); ?>" class="border rounded px-4 py-2 w-full" required>
                </div>
                <div>
                    <label class="font-bold">Mail:</label>
                    <input type="email" name="mail" value="<?php echo htmlspecialchars($alumno['mail']); ?>" class="border rounded px-4 py-2 w-full" required>
                </div>
                <div>
                    <label class="font-bold">Año:</label>
                    <input type="text" name="anio" value="<?php echo htmlspecialchars($alumno['anio']); ?>" class="border rounded px-4 py-2 w-full">
                </div>
                <div>
                    <label class="font-bold">División:</label>
                    <input type="text" name="division" value="<?php echo htmlspecialchars($alumno['division']); ?>" class="border rounded px-4 py-2 w-full">
                </div>
                <div>
                    <button type="submit" class="bg-indigo-600 text-white px-6 py-2 rounded-xl hover:bg-indigo-700 font-bold">Guardar cambios</button>
                    <a href="alumnos.php" class="ml-4 text-gray-600 hover:underline">Cancelar</a>
                </div>
            </form>
        <?php else: ?>
            <div class="text-red-600 font-bold">Alumno no encontrado.</div>
        <?php endif; ?>
    </main>
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
    <<<<<<< HEAD=======<!-- DRAWER:JS START -->
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
        >>>>>>> staging
        <!-- DRAWER:JS END -->
</body>

</html>