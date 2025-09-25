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
            <a href="materias_pendientes.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Panel de Notificaciones">
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

        <h1 class="text-3xl font-bold mb-4">📙 Gestión de Materias Pendientes</h1>

        <!-- Buscador de alumnos -->
        <div class="mb-4 relative">
            <input type="text" id="buscarAlumno" placeholder="Buscar alumno..."
                class="border p-2 rounded" style="width: 460px;">
            <ul id="listaAlumnos" class="absolute border rounded hidden bg-white z-10" style="width: 460px;"></ul>
        </div>

        <!-- Panel de materias -->
        <div id="materiasPendientes" class="hidden bg-white p-6 rounded-xl shadow">
            <h2 class="text-lg font-semibold mb-2">Materias pendientes de <span id="nombreAlumno"></span>
            </h2>

            <table class="w-full border border-gray-200 rounded-lg overflow-hidden shadow-sm mb-4">
                <thead class="bg-gray-100 text-gray-700 text-sm uppercase">
                    <tr>
                        <th class="px-4 py-2">Materia</th>
                        <th class="px-4 py-2">Estado</th>
                        <th class="px-4 py-2">Última Nota</th>
                        <th class="px-4 py-2">Fecha</th>
                        <th class="px-4 py-2">Acciones</th>
                    </tr>
                </thead>
                <tbody id="tablaMaterias" class="divide-y divide-gray-200">
                    <tr>
                        <td colspan="5" class="p-4 text-center text-gray-500">No hay materias pendientes</td>
                    </tr>
                </tbody>
            </table>

            <!-- Bloque para agregar materia -->
            <div class="mb-6">
                <input type="text" id="buscarMateria" placeholder="Buscar materia..."
                    class="border p-2 rounded w-full mb-2">
                <ul id="listaMaterias"
                    class="absolute border rounded hidden bg-white z-10 w-full max-h-48 overflow-y-auto"></ul>

                <button id="btnAgregarMateria"
                    class="bg-green-600 hover:bg-green-700 text-white font-medium px-4 py-2 rounded-lg shadow mt-2">
                    ➕ Agregar
                </button>
            </div>

            <!-- Historial -->
            <h3 class="text-md font-semibold mb-2">Historial</h3>
            <table class="w-full border">
                <thead class="bg-gray-100">
                    <tr>
                        <th class="p-2">Materia</th>
                        <th class="p-2">Nota</th>
                        <th class="p-2">Estado</th>
                        <th class="p-2">Profesor</th>
                        <th class="p-2">Fecha</th>
                    </tr>
                </thead>
                <tbody id="tablaHistorial"></tbody>
            </table>
        </div>
    </main>

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

    <?php require_once __DIR__ . '/../../../backend/includes/popup_datos.php'; ?>
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
    <!-- POPUP APROBAR MATERIA -->
    <div id="popupAprobar" class="hidden fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-lg w-96 p-6">
            <h2 class="text-lg font-semibold mb-4">Aprobar materia</h2>

            <form id="formAprobar">
                <input type="hidden" id="aprobarId">

                <div class="mb-3">
                    <label class="block text-sm font-medium mb-1">Nota final</label>
                    <input type="number" id="notaFinal" class="border rounded w-full p-2" min="1" max="10" required>
                </div>

                <div class="mb-3">
                    <label class="block text-sm font-medium mb-1">Profesor</label>
                    <select id="profesorId" class="border rounded w-full p-2" required>
                        <!-- Opciones cargadas por AJAX -->
                    </select>
                </div>

                <div class="flex justify-end gap-3 mt-4">
                    <button type="button" onclick="cerrarPopupAprobar()"
                        class="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400">Cancelar</button>
                    <button type="submit"
                        class="px-4 py-2 rounded bg-green-600 text-white hover:bg-green-700">Guardar</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        const API_BASE = "/api/materias_pendientes";
        const input = document.getElementById("buscarAlumno");
        const lista = document.getElementById("listaAlumnos");
        const tabla = document.getElementById("tablaMaterias");
        const tablaHistorial = document.getElementById("tablaHistorial");
        const seccionMaterias = document.getElementById("materiasPendientes");

        const inputMateria = document.getElementById("buscarMateria");
        const listaMaterias = document.getElementById("listaMaterias");
        let alumnoSeleccionado = null;
        let materiaSeleccionada = null;

        function mostrarError(mensaje) {
            alert(mensaje);
        }

        async function apiRequest(path, options = {}) {
            const response = await fetch(path, options);
            let payload = null;

            if (response.status !== 204) {
                try {
                    payload = await response.json();
                } catch (error) {
                    payload = null;
                }
            }

            if (!response.ok) {
                const mensaje = payload && typeof payload.error === "string"
                    ? payload.error
                    : "No se pudo completar la operación";
                throw new Error(mensaje);
            }

            return payload;
        }

        // --- BUSCADOR DE ALUMNOS ---
        input.addEventListener("input", async () => {
            const q = input.value.trim();
            if (q.length < 2) {
                lista.classList.add("hidden");
                return;
            }

            try {
                const alumnos = await apiRequest(`${API_BASE}/buscar_alumno.php?q=${encodeURIComponent(q)}`);

                lista.innerHTML = alumnos.map(a => `
                <li class="p-2 hover:bg-gray-200 cursor-pointer"
                    data-id="${a.id}"
                    data-nombre="${a.apellido}, ${a.nombre}">
                    ${a.apellido}, ${a.nombre} (${a.mail ?? ''})
                </li>`).join("");
                lista.classList.remove("hidden");
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
                lista.classList.add("hidden");
            }
        });

        lista.addEventListener("click", async e => {
            if (!e.target.dataset.id) return;
            alumnoSeleccionado = e.target.dataset.id;
            document.getElementById("nombreAlumno").innerText = e.target.dataset.nombre;
            lista.classList.add("hidden");

            await cargarMateriasPendientes();
            await cargarHistorial();
            seccionMaterias.classList.remove("hidden");
        });

        // --- BUSCADOR DE MATERIAS ---
        inputMateria.addEventListener("input", async () => {
            const q = inputMateria.value.trim();
            if (q.length < 2) {
                listaMaterias.classList.add("hidden");
                return;
            }

            try {
                const materias = await apiRequest(`${API_BASE}/listar_materias.php?q=${encodeURIComponent(q)}`);

                listaMaterias.innerHTML = materias.map(m => `
                <li class="p-2 hover:bg-gray-200 cursor-pointer"
                    data-id="${m.id}"
                    data-nombre="${m.nombre}">
                    ${m.nombre}
                </li>`).join("");
                listaMaterias.classList.remove("hidden");
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
                listaMaterias.classList.add("hidden");
            }
        });

        listaMaterias.addEventListener("click", e => {
            if (!e.target.dataset.id) return;
            materiaSeleccionada = e.target.dataset.id;
            inputMateria.value = e.target.dataset.nombre;
            listaMaterias.classList.add("hidden");
        });

        // --- TABLA DE PENDIENTES ---
        async function cargarMateriasPendientes() {
            if (!alumnoSeleccionado) {
                tabla.innerHTML = "";
                return;
            }

            try {
                const materias = await apiRequest(`${API_BASE}/listar_materias_pendientes.php?alumno_id=${alumnoSeleccionado}`);

                tabla.innerHTML = materias.map(m => `
                <tr>
                <td class="p-2">${m.materia}</td>
                <td class="p-2">${m.estado}</td>
                <td class="p-2">${m.ultima_nota ?? '-'}</td>
                <td class="p-2">${m.ultima_fecha ?? '-'}</td>
                <td class="p-2">
                    <button class="bg-green-600 text-white px-2 py-1 rounded" onclick="abrirPopupAprobar(${m.id})">Aprobar</button>
                </td>
                </tr>`).join("");
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
                tabla.innerHTML = "";
            }
        }

        // --- TABLA HISTORIAL ---
        async function cargarHistorial() {
            if (!alumnoSeleccionado) {
                tablaHistorial.innerHTML = "";
                return;
            }

            try {
                const historial = await apiRequest(`${API_BASE}/listar_historial.php?alumno_id=${alumnoSeleccionado}`);

                tablaHistorial.innerHTML = historial.map(h => `
                <tr>
                    <td class="p-2">${h.materia}</td>
                    <td class="p-2">${h.nota ?? '-'}</td>
                    <td class="p-2">${h.estado}</td>
                    <td class="p-2">${h.profesor ?? '-'}</td>
                    <td class="p-2">${h.fecha_resolucion ?? '-'}</td>
                </tr>`).join("");
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
                tablaHistorial.innerHTML = "";
            }
        }

        // --- AGREGAR MATERIA ---
        document.getElementById("btnAgregarMateria").addEventListener("click", async () => {
            if (!alumnoSeleccionado || !materiaSeleccionada) return;
            const formData = new FormData();
            formData.append("alumno_id", alumnoSeleccionado);
            formData.append("materia_id", materiaSeleccionada);

            try {
                await apiRequest(`${API_BASE}/agregar_materia_pendiente.php`, {
                    method: "POST",
                    body: formData
                });
                await cargarMateriasPendientes();
                inputMateria.value = "";
                materiaSeleccionada = null;
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
            }
        });

        async function abrirPopupAprobar(id) {
            document.getElementById("aprobarId").value = id;

            // Traer profesores que dictan la materia de este pendiente
            try {
                const profs = await apiRequest(`${API_BASE}/listar_profesores.php?pendiente_id=${id}`);

                const sel = document.getElementById("profesorId");
                if (!Array.isArray(profs) || profs.length === 0) {
                    sel.innerHTML = `<option value="">(Sin profesores asignados)</option>`;
                } else {
                    sel.innerHTML = profs.map(p => `<option value="${p.id}">${p.apellido}, ${p.nombre}</option>`).join("");
                }

                document.getElementById("popupAprobar").classList.remove("hidden");
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
            }
        }

        function cerrarPopupAprobar() {
            document.getElementById("popupAprobar").classList.add("hidden");
        }

        document.getElementById("formAprobar").addEventListener("submit", async (e) => {
            e.preventDefault();

            const id = document.getElementById("aprobarId").value;
            const nota = document.getElementById("notaFinal").value;
            const profesorId = document.getElementById("profesorId").value;

            const formData = new FormData();
            formData.append("id", id);
            formData.append("nota", nota);
            formData.append("profesor_id", profesorId);

            try {
                await apiRequest(`${API_BASE}/aprobar_materia.php`, {
                    method: "POST",
                    body: formData
                });

                cerrarPopupAprobar();
                await cargarMateriasPendientes();
                await cargarHistorial();
            } catch (error) {
                console.error(error);
                mostrarError(error.message);
            }
        });
    </script>
</body>

</html>