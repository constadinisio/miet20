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

require_once __DIR__ . '/../../../backend/includes/db.php';

$usuario = $_SESSION['usuario'];

// Conseguir usuarios, roles y grupos de tu base
$usuarios = $conexion->query("SELECT id, nombre, apellido FROM usuarios WHERE status='1' ORDER BY apellido, nombre");
$roles = $conexion->query("SELECT id, nombre FROM roles WHERE id > 0"); // Excluye Pendiente
$grupos = $conexion->query("SELECT id, nombre FROM grupos_notificacion_personalizados ORDER BY nombre");
$cursos = $conexion->query("SELECT id, anio, division FROM cursos ORDER BY anio, division");
$cursos_array = [];
while ($c = $cursos->fetch_assoc()) $cursos_array[] = $c;

$mensaje = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $_POST['csrf'] === $csrf) {
    $titulo = $_POST['titulo'] ?? '';
    $contenido = $_POST['contenido'] ?? '';
    $tipo = $_POST['tipo'] ?? '';
    $destinos = $_POST['destino'] ?? [];
    $remitente_id = $usuario['id'];

    // Crear notificación
    $stmt = $conexion->prepare("INSERT INTO notificaciones 
        (titulo, contenido, tipo_notificacion, remitente_id, fecha_creacion, prioridad, estado, requiere_confirmacion) 
        VALUES (?, ?, ?, ?, NOW(), 'NORMAL', 'ACTIVA', 0)");
    $stmt->bind_param("sssi", $titulo, $contenido, $tipo, $remitente_id);
    $stmt->execute();
    $notificacion_id = $stmt->insert_id;
    $stmt->close();

    // Asignar destinatarios según tipo
    if ($tipo === 'INDIVIDUAL') {
        foreach ($destinos as $uid) {
            $conexion->query("INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) VALUES ($notificacion_id, $uid, 'NO_LEIDA')");
        }
    } elseif ($tipo === 'ROL') {
        foreach ($destinos as $rid) {
            // Asigna a todos los usuarios activos de ese rol
            $q = $conexion->query("SELECT id FROM usuarios WHERE rol = $rid AND status='1'");
            while ($u = $q->fetch_assoc()) {
                $conexion->query("INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) VALUES ($notificacion_id, {$u['id']}, 'NO_LEIDA')");
            }
        }
    } elseif ($tipo === 'GRUPO') {
        foreach ($destinos as $gid) {
            // Asigna a todos los miembros activos de ese grupo
            $q = $conexion->query("SELECT usuario_id FROM grupos_notificacion_miembros WHERE grupo_id = $gid AND activo=1");
            while ($u = $q->fetch_assoc()) {
                $conexion->query("INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) VALUES ($notificacion_id, {$u['usuario_id']}, 'NO_LEIDA')");
            }
        }
    } elseif ($tipo === 'GLOBAL') {
        // Todos los usuarios activos
        $q = $conexion->query("SELECT id FROM usuarios WHERE status='1'");
        while ($u = $q->fetch_assoc()) {
            $conexion->query("INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) VALUES ($notificacion_id, {$u['id']}, 'NO_LEIDA')");
        }
    } elseif ($tipo === 'CURSO') {
        foreach ($destinos as $curso_id) {
            // Buscá todos los usuarios de alumnos_curso en ese curso
            $q = $conexion->query("SELECT alumno_id FROM alumno_curso WHERE curso_id = $curso_id");
            while ($a = $q->fetch_assoc()) {
                $conexion->query("INSERT INTO notificaciones_destinatarios (notificacion_id, destinatario_id, estado_lectura) VALUES ($notificacion_id, {$a['alumno_id']}, 'NO_LEIDA')");
            }
        }
    }


    $mensaje = "✅ ¡Notificación enviada!";
}

?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Panel de Administración</title>
    <link href="/output.css?v=<?= time() ?>" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <style>
        body {
            font-family: 'Poppins', sans-serif;
            overflow-x: hidden;
        }

        .sidebar-item {
            min-height: 3.5rem;
            width: 100%;
        }

        .custom-scroll::-webkit-scrollbar {
            width: 0px;
            background: transparent;
        }

        .custom-scroll {
            -ms-overflow-style: none;
            /* IE */
            scrollbar-width: none;
            /* Firefox */
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
    width: 15rem; /* ~ w-60 */
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

<body class="bg-gray-100 min-h-screen flex relative">
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
            <a href="alumnos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Alumnos">
                <span class="text-xl">👤</span><span class="sidebar-label">Alumnos</span>
            </a>
            <a href="materias.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Materias">
                <span class="text-xl">📚</span><span class="sidebar-label">Materias</span>
            </a>
            <a href="cargos.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Cargos">
                <span class="text-xl">💼</span><span class="sidebar-label">Cargos</span>
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
            <a href="notificaciones.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Panel de Notificaciones">
                <span class="text-xl">🔔</span><span class="sidebar-label">Panel de Notificaciones</span>
            </a>
            <button onclick="window.location='/includes/logout.php'" class="sidebar-item flex items-center justify-center gap-2 mt-auto py-2 px-3 rounded-xl text-white bg-red-500 hover:bg-red-600">
                <span class="text-xl">🚪</span><span class="sidebar-label">Salir</span>
            </button>
        </div>
    </nav>
    <!-- Contenido principal -->
    <main id="content" class="flex-1 p-10">
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
                    <div class="mt-1 text-xs text-gray-500">Administrador</div>
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

        <div class="grid md:grid-cols-3 gap-4 w-full mt-6">
            <!-- Crear notificación -->
            <div class="bg-white rounded-2xl shadow-xl p-8 w-full">
                <h2 class="text-2xl font-bold mb-4">Crear nueva notificación</h2>
                <?php if ($mensaje): ?>
                    <div class="bg-green-100 text-green-800 rounded p-2 mb-4 font-semibold"><?= $mensaje ?></div>
                <?php endif; ?>
                <form method="POST" class="flex flex-col gap-4">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">
                    <label class="font-semibold">Título:</label>
                    <input type="text" name="titulo" required class="border rounded p-2" maxlength="100">

                    <label class="font-semibold">Contenido:</label>
                    <textarea name="contenido" required class="border rounded p-2" maxlength="500"></textarea>

                    <label class="font-semibold">Tipo de notificación:</label>
                    <select name="tipo" id="tipo" class="border rounded p-2" required>
                        <option value="">Seleccioná tipo</option>
                        <option value="INDIVIDUAL">A un usuario</option>
                        <option value="ROL">A un rol</option>
                        <option value="GRUPO">A un grupo</option>
                        <option value="CURSO">A un curso</option>
                        <option value="GLOBAL">Global (todos)</option>
                    </select>

                    <div id="destino-individual" class="hidden">
                        <label class="font-semibold mt-2">Usuarios:</label>
                        <input type="text" id="filtro-usuarios" class="border rounded p-2 mb-2 w-full" placeholder="Buscar usuario...">
                        <select id="select-usuarios" name="destino[]" class="border rounded p-2 w-full" size="10" multiple>
                            <?php foreach ($usuarios as $u): ?>
                                <option value="<?= $u['id'] ?>"><?= htmlspecialchars($u['apellido'] . ", " . $u['nombre']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <div id="destino-rol" class="hidden">
                        <label class="font-semibold mt-2">Roles:</label>
                        <select name="destino[]" class="border rounded p-2 w-full" multiple>
                            <?php while ($r = $roles->fetch_assoc()): ?>
                                <option value="<?= $r['id'] ?>"><?= htmlspecialchars($r['nombre']) ?></option>
                            <?php endwhile; ?>
                        </select>
                    </div>

                    <div id="destino-grupo" class="hidden">
                        <label class="font-semibold mt-2">Grupos:</label>
                        <select name="destino[]" class="border rounded p-2 w-full" multiple>
                            <?php while ($g = $grupos->fetch_assoc()): ?>
                                <option value="<?= $g['id'] ?>"><?= htmlspecialchars($g['nombre']) ?></option>
                            <?php endwhile; ?>
                        </select>
                    </div>

                    <div id="destino-curso" class="hidden">
                        <label class="font-semibold mt-2">Cursos:</label>
                        <select name="destino[]" class="border rounded p-2 w-full" multiple>
                            <?php foreach ($cursos_array as $c): ?>
                                <option value="<?= $c['id'] ?>"><?= htmlspecialchars($c['anio'] . "° " . $c['division']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <button type="submit" class="mt-4 bg-blue-600 text-white rounded-xl px-4 py-2 font-bold hover:bg-blue-700">Enviar notificación</button>
                </form>
            </div>

            <!-- Crear grupo -->
            <div class="bg-white rounded-2xl shadow-xl p-8 w-full">
                <h2 class="text-2xl font-bold mb-4">👥 Grupos de Notificación</h2>
                <form method="post" action="crear_grupo.php" class="mb-6">
                    <input type="hidden" name="csrf" value="<?= $csrf ?>">

                    <label class="font-semibold">Nombre del nuevo grupo:</label>
                    <input type="text" name="nombre_grupo" required class="border rounded p-2 w-full mb-4" maxlength="100">

                    <label class="font-semibold">Descripción: (OPCIONAL)</label>
                    <input type="text" name="descripcion_grupo" class="border rounded p-2 w-full mb-4" maxlength="100">

                    <label class="font-semibold">Seleccionar miembros:</label>
                    <input type="text" id="filtro-miembros" class="border rounded p-2 mb-2 w-full" placeholder="Buscar usuario...">

                    <div id="checkbox-miembros" class="w-full h-[18rem] overflow-y-auto overflow-x-hidden border rounded p-2 custom-scroll bg-white">
                        <?php
                        $usuarios_todos = $conexion->query("SELECT id, nombre, apellido FROM usuarios WHERE status='1' ORDER BY apellido, nombre");
                        while ($u = $usuarios_todos->fetch_assoc()):
                        ?>
                            <label class="block text-sm mb-1 break-words">
                                <input type="checkbox" name="miembros[]" value="<?= $u['id'] ?>" class="mr-2">
                                <?= htmlspecialchars($u['apellido'] . ", " . $u['nombre']) ?>
                            </label>
                        <?php endwhile; ?>
                    </div>

                    <button type="submit" class="mt-4 bg-green-600 text-white rounded-xl px-4 py-2 font-bold hover:bg-green-700">
                        Crear grupo
                    </button>
                </form>
            </div>

            <!-- Lista de grupos -->
            <div class="bg-white rounded-2xl shadow-xl p-8 w-full">
                <h2 class="text-2xl font-bold mb-4">📋 Grupos Existentes</h2>
                <?php
                $grupos_lista = $conexion->query("SELECT g.id, g.nombre FROM grupos_notificacion_personalizados g ORDER BY g.nombre");
                while ($g = $grupos_lista->fetch_assoc()):
                    $gid = $g['id'];
                    $miembros = $conexion->query("
                SELECT u.nombre, u.apellido
                FROM grupos_notificacion_miembros gm
                JOIN usuarios u ON u.id = gm.usuario_id
                WHERE gm.grupo_id = $gid AND gm.activo = 1
                ORDER BY u.apellido, u.nombre
            ");
                ?>
                    <div class="mb-3 p-3 bg-gray-100 rounded-xl">
                        <div class="flex justify-between items-center">
                            <span class="font-semibold"><?= htmlspecialchars($g['nombre']) ?></span>
                            <form method="post" action="eliminar_grupo.php" onsubmit="return confirm('¿Eliminar este grupo?')">
                                <input type="hidden" name="csrf" value="<?= $csrf ?>">
                                <input type="hidden" name="grupo_id" value="<?= $gid ?>">
                                <button class="text-sm text-red-600 hover:underline">Eliminar</button>
                            </form>
                        </div>
                        <ul class="text-sm text-gray-700 list-disc ml-5 mt-1">
                            <?php while ($m = $miembros->fetch_assoc()): ?>
                                <li><?= htmlspecialchars($m['apellido'] . ", " . $m['nombre']) ?></li>
                            <?php endwhile; ?>
                        </ul>
                    </div>
                <?php endwhile; ?>
            </div>
        </div>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const filtro = document.getElementById('filtro-usuarios');
            const select = document.getElementById('select-usuarios');
            if (filtro && select) {
                filtro.addEventListener('input', function() {
                    const valor = this.value.toLowerCase();
                    for (let option of select.options) {
                        const texto = option.textContent.toLowerCase();
                        option.style.display = texto.includes(valor) ? '' : 'none';
                    }
                });
            }
        });
    </script>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const filtro = document.getElementById('filtro-miembros');
            const contenedor = document.getElementById('checkbox-miembros');

            if (filtro && contenedor) {
                filtro.addEventListener('input', function() {
                    const valor = this.value.toLowerCase();
                    const etiquetas = contenedor.querySelectorAll('label');

                    etiquetas.forEach(label => {
                        const texto = label.textContent.toLowerCase();
                        label.style.display = texto.includes(valor) ? '' : 'none';
                    });
                });
            }
        });
    </script>

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
        function mostrarOpcionesDestino() {
            var individual = document.getElementById('destino-individual');
            var rol = document.getElementById('destino-rol');
            var grupo = document.getElementById('destino-grupo');
            var curso = document.getElementById('destino-curso');
            if (individual) individual.classList.add('hidden');
            if (rol) rol.classList.add('hidden');
            if (grupo) grupo.classList.add('hidden');
            if (curso) curso.classList.add('hidden');
            var tipo = document.getElementById('tipo').value;
            if (tipo === 'INDIVIDUAL' && individual) {
                const filtro = document.getElementById('filtro-usuarios');
                const select = document.getElementById('select-usuarios');
                if (filtro && select) {
                    filtro.value = '';
                    for (let option of select.options) {
                        option.style.display = '';
                    }
                }
                individual.classList.remove('hidden');
            } else if (tipo === 'ROL' && rol) {
                rol.classList.remove('hidden');
            } else if (tipo === 'GRUPO' && grupo) {
                grupo.classList.remove('hidden');
            } else if (tipo === 'CURSO' && curso) {
                curso.classList.remove('hidden');
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            var tipo = document.getElementById('tipo');
            if (tipo) tipo.addEventListener('change', mostrarOpcionesDestino);
            mostrarOpcionesDestino();
        });
    </script>
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
    <!-- DRAWER:JS END -->
</body>

</html>