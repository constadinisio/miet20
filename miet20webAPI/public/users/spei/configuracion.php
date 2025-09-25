<?php
session_start();
// Verifica que el usuario esté logueado, que su sesión sea un array válido y que su rol sea 5 (rol autorizado)
if (
  !isset($_SESSION['usuario']) ||
  !is_array($_SESSION['usuario']) ||
  (int)$_SESSION['usuario']['rol'] !== 5
) {
  // Si no cumple las condiciones, redirige al login con un error de rol
  header("Location: /login.php?error=rol");
  exit;
}

if (!isset($_SESSION['csrf'])) {
    $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];
$usuario = $_SESSION['usuario'];

// Guardar URL de origen
if (isset($_GET['origen'])) {
    // Mapear nombres a rutas reales
    switch ($_GET['origen']) {
        case 'usuarios':
            $prev_url = '/users/admin/usuarios.php';
            break;
        case 'asistencias':
            $prev_url = '/users/alumno/asistencias.php';
            break;
        default:
            $prev_url = '/users/admin/admin.php';
    }
    $_SESSION['config_prev_url'] = $prev_url;
} elseif (isset($_SERVER['HTTP_REFERER'])) {
    $_SESSION['config_prev_url'] = $_SERVER['HTTP_REFERER'];
}

$prev_url = $_SESSION['config_prev_url'] ?? '/users/spei/index.php';

require_once __DIR__ . '/../../../backend/includes/db.php';
?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Configuración de Usuario</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js" defer></script>
    <style>
        body {
            font-family: 'Poppins', sans-serif;
        }

        .sidebar-item {
            min-height: 3.5rem;
            width: 100%;
        }

        .w-16 .sidebar-item {
            justify-content: center !important;
        }

        .w-16 .sidebar-item span.sidebar-label {
            display: none;
        }

        .w-16 .sidebar-item span.text-xl {
            margin: 0 auto;
        }

        .input-text {
            @apply w-full border border-gray-300 rounded-xl px-3 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none shadow-sm;
        }
    </style>
</head>

<body class="bg-gray-100 min-h-screen flex">
    <!-- Sidebar -->
    <nav id="sidebar" class="w-60 transition-all duration-300 bg-white shadow-lg px-4 py-4 flex flex-col gap-2">
        <button id="toggleSidebar" class="absolute top-4 left-4 z-50 text-2xl hover:text-indigo-600 transition">
            ☰
        </button>
        <div class="flex justify-center items-center p-2 mb-4 border-b border-gray-400 h-28">
            <img src="/images/et20ico.ico" class="sidebar-expanded block h-full w-auto object-contain">
            <img src="/images/et20ico.ico" class="sidebar-collapsed hidden h-10 w-auto object-contain">
        </div>
        <a href="index.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
            <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
        </a>
        <a href="stock.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Stock">
            <span class="text-xl">📂</span><span class="sidebar-label">Stock</span>
        </a>
        <a href="prestamos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Prestamos">
            <span class="text-xl">📑</span><span class="sidebar-label">Prestamos</span>
        </a>
        <a href="logs.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Logs">
            <span class="text-xl">📝</span><span class="sidebar-label">Logs</span>
        </a>
        <button onclick="window.location='/includes/logout.php'" class="sidebar-item flex items-center justify-center gap-2 mt-auto py-2 px-3 rounded-xl text-white bg-red-500 hover:bg-red-600">
            <span class="text-xl">🚪</span><span class="sidebar-label">Salir</span>
        </button>
    </nav>

    <!-- Contenido principal -->
    <main class="flex-1 p-10">
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
                    <div class="mt-1 text-xs text-gray-500">Alumno/a</div>
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

        <div class="max-w-5xl mx-auto">
            <!-- Panel Configuración -->
            <div class="bg-white rounded-2xl shadow-lg p-8">
                <div class="text-center mb-6">
                    <h1 class="text-3xl font-bold text-blue-500">⚙️ Configuración de Usuario</h1>
                    <p class="text-gray-500">Gestione sus datos personales y configuraciones</p>
                </div>

                <div x-data="{ tab: 'personales' }">
                    <!-- Tabs -->
                    <div class="flex justify-center space-x-2 mb-6">
                        <button
                            @click="tab='personales'"
                            :class="tab==='personales' ? 'tab-btn active' : 'tab-btn inactive'">
                            👤 Datos Personales
                        </button>
                    </div>

                    <form id="form-configuracion" class="space-y-6">
                        <input type="hidden" name="csrf" value="<?= $csrf ?>">

                        <!-- Datos Personales -->
                        <div x-show="tab==='personales'" class="space-y-6">
                            <h2 class="text-lg font-semibold text-blue-600 border-b pb-1">📋 Información Personal</h2>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <label class="input-text">Nombre: <?= htmlspecialchars($usuario['nombre']) ?></label>
                                <label class="input-text">Apellido: <?= htmlspecialchars($usuario['apellido']) ?></label>
                                <label class="input-text">DNI: <?= htmlspecialchars($usuario['dni'] ?? '') ?></label>
                                <label class="input-text">Fecha de nacimiento: <?= htmlspecialchars($usuario['fecha_nacimiento'] ?? '') ?></label>
                            </div>

                            <h2 class="text-lg font-semibold text-blue-600 border-b pb-1">📞 Información de Contacto</h2>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <label class="input-text">Email: <?= htmlspecialchars($usuario['mail'] ?? '') ?></label>
                                <label class="input-text">Teléfono: <?= htmlspecialchars($usuario['telefono'] ?? '') ?></label>
                                <label class="input-text md:col-span-2">Dirección: <?= htmlspecialchars($usuario['direccion'] ?? '') ?></label>
                            </div>

                            <h2 class="text-lg font-semibold text-blue-600 border-b pb-1">🔒 Cambio de Contraseña</h2>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-sm font-medium text-gray-700">Contraseña Actual</label>
                                    <input type="password" name="contrasena_actual"
                                        class="mt-1 w-full border rounded-lg px-3 py-2 focus:ring focus:ring-blue-300">
                                </div>
                                <div>
                                    <label class="block text-sm font-medium text-gray-700">Nueva Contraseña</label>
                                    <input type="password" name="contrasena_nueva"
                                        class="mt-1 w-full border rounded-lg px-3 py-2 focus:ring focus:ring-blue-300">
                                </div>
                                <div class="md:col-span-2">
                                    <label class="block text-sm font-medium text-gray-700">Confirmar Contraseña</label>
                                    <input type="password" name="confirmar_contrasena"
                                        class="mt-1 w-full border rounded-lg px-3 py-2 focus:ring focus:ring-blue-300">
                                </div>
                            </div>
                        </div>

                        <!-- Botones -->
                        <div class="mt-8 flex justify-center space-x-4">
                            <button type="submit" class="px-6 py-2 bg-green-600 hover:bg-green-700 text-white font-bold rounded-xl shadow transition">💾 Guardar Cambios</button>
                            <a href="<?php echo htmlspecialchars($prev_url); ?>"
                                class="px-6 py-2 bg-red-500 hover:bg-red-600 text-white font-bold rounded-xl shadow transition">
                                ❌ Cancelar
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>

    <!-- Toast -->
    <div id="toast" class="hidden fixed bottom-4 right-4 px-4 py-2 rounded-lg shadow font-semibold z-50"></div>

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
        document.getElementById('form-configuracion').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);

            fetch('guardar_configuracion.php', {
                    method: 'POST',
                    body: formData
                }).then(res => res.json())
                .then(data => {
                    if (data.ok) {
                        showToast("✅ " + data.mensaje, 'success');
                        this.querySelector('[name=contrasena_actual]').value = '';
                        this.querySelector('[name=contrasena_nueva]').value = '';
                        this.querySelector('[name=confirmar_contrasena]').value = '';
                    } else {
                        showToast("⚠️ " + data.mensaje, 'error');
                    }
                }).catch(err => {
                    showToast("❌ Error en la conexión", 'error');
                    console.error(err);
                });
        });

        function showToast(msg, type = 'success') {
            const toast = document.getElementById('toast');
            toast.textContent = msg;
            toast.className = 'fixed bottom-4 right-4 px-4 py-2 rounded-lg shadow font-semibold z-50 ' +
                (type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white');
            toast.classList.remove('hidden');
            setTimeout(() => toast.classList.add('hidden'), 3000);
        }
    </script>
</body>

</html>