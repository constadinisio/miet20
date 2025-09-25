<?php
require_once __DIR__ . '/../../../backend/users/spei/helpers.php';

spei_require_role();

if (!isset($_SESSION['csrf'])) {
  $_SESSION['csrf'] = bin2hex(random_bytes(32));
}
$csrf = $_SESSION['csrf'];
$usuario = $_SESSION['usuario'];

$flashMessages = spei_consume_flash();
$pageError = null;

$stats = [
  'disponibles' => 0,
  'noDisponibles' => 0,
  'prestamosActivos' => 0
];
$deviceRows = [];

try {
  $response = spei_call_api('GET', '/devices');
  $data = $response['data'] ?? [];

  if (isset($data['stats']) && is_array($data['stats'])) {
    $stats = array_merge($stats, $data['stats']);
  }

  $items = [];
  if (isset($data['items']) && is_array($data['items'])) {
    $items = $data['items'];
  } elseif (is_array($data)) {
    $items = $data;
  }

  foreach ($items as $item) {
    if (!is_array($item)) {
      continue;
    }

    $deviceRows[] = [
      'id' => isset($item['id']) ? (int) $item['id'] : null,
      'carrito' => trim((string) ($item['carrito'] ?? '')),
      'numero' => trim((string) ($item['numero'] ?? '')),
      'numero_serie' => trim((string) ($item['numero_serie'] ?? '')),
      'fecha_adquisicion' => spei_format_display_date($item['fecha_adquisicion'] ?? null),
      'estado' => trim((string) ($item['estado'] ?? 'En uso')),
      'observaciones' => trim((string) ($item['observaciones'] ?? ''))
    ];
  }
} catch (RuntimeException $exception) {
  $pageError = $exception->getMessage();
}

$estados = ['En uso', 'Dañada', 'Hurto', 'Obsoleta'];
?>
<!DOCTYPE html>
<html lang="es">

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ATTP - Stock</title>
  <link rel="stylesheet" href="/output.css">
  <link rel="icon" type="image/x-icon" href="/images/et20png.png">
  <!-- Google Fonts -->
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
      <a href="index.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-700 hover:bg-indigo-100 transition" title="Inicio">
        <span class="text-xl">🏠</span><span class="sidebar-label">Inicio</span>
      </a>
      <a href="stock.php" class="sidebar-item flex gap-3 items-center py-2 px-3 rounded-xl text-gray-900 font-semibold hover:bg-gray-200 transition" title="Stock">
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

    <h1 class="text-3xl font-bold mb-6">Gestión de Netbooks</h1>
    <?php foreach ($flashMessages as $type => $messages): ?>
      <?php foreach ((array) $messages as $message): ?>
        <div class="mb-4 px-4 py-3 rounded border <?php echo $type === 'error' ? 'bg-red-100 border-red-200 text-red-800' : 'bg-green-100 border-green-200 text-green-800'; ?>">
          <?= htmlspecialchars($message) ?>
        </div>
      <?php endforeach; ?>
    <?php endforeach; ?>
    <?php if ($pageError): ?>
      <div class="mb-4 px-4 py-3 rounded border bg-red-100 border-red-200 text-red-800">
        <?= htmlspecialchars($pageError) ?>
      </div>
    <?php endif; ?>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 p-4">
      <div class="bg-white rounded shadow p-6">
        <h2 class="text-xl font-semibold mb-2">Computadoras disponibles</h2>
        <p class="text-5xl font-bold text-green-600"><?= (int) ($stats['disponibles'] ?? 0) ?></p>
      </div>
      <div class="bg-white rounded shadow p-6">
        <h2 class="text-xl font-semibold mb-2">Computadoras no disponibles</h2>
        <p class="text-5xl font-bold text-red-600"><?= (int) ($stats['noDisponibles'] ?? 0) ?></p>
      </div>
      <div class="bg-white rounded shadow p-6">
        <h2 class="text-xl font-semibold mb-2">Préstamos en curso</h2>
        <p class="text-5xl font-bold text-yellow-600"><?= (int) ($stats['prestamosActivos'] ?? 0) ?></p>
      </div>
    </div>

    <form action="agregar_netbook.php" method="POST" class="bg-white p-4 rounded shadow mb-6 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
      <input type="hidden" name="csrf" value="<?= $csrf ?>">
      <input name="carrito" required placeholder="Carrito (A-Z)" class="border p-2 rounded">
      <input name="numero" required placeholder="Número (01-30)" class="border p-2 rounded">
      <input name="numero_serie" required placeholder="Número de Serie" class="border p-2 rounded">
      <input name="fecha_adquisicion" placeholder="Fecha Adquisición (DD/MM/AAAA)" class="border p-2 rounded">
      <select name="estado" class="border p-2 rounded">
        <option value="En uso">En uso</option>
        <option value="Dañada">Dañada</option>
        <option value="Hurto">Hurto</option>
        <option value="Obsoleta">Obsoleta</option>
      </select>
      <textarea name="observaciones" placeholder="Observaciones" class="border p-2 rounded col-span-full"></textarea>
      <button type="submit" class="bg-green-600 text-white px-4 py-2 rounded">Añadir Netbook</button>
    </form>

    <div class="mb-4">
      <input id="searchInput" type="text" placeholder="Buscar netbook por carrito, número, serie, estado..." class="w-full p-2 border rounded" onkeyup="filtrarTabla()" />
    </div>

    <div class="overflow-x-auto w-full">
      <table class="min-w-full bg-white shadow rounded text-sm">
        <thead class="sticky top-0 bg-gray-50 z-10">
          <tr>
            <th class="py-2 px-4 border-b">Carrito</th>
            <th class="py-2 px-4 border-b">Número</th>
            <th class="py-2 px-4 border-b">Número de Serie</th>
            <th class="py-2 px-4 border-b">Fecha Adquisición</th>
            <th class="py-2 px-4 border-b">Estado</th>
            <th class="py-2 px-4 border-b">Observaciones</th>
            <th class="py-2 px-4 border-b">Acción</th>
          </tr>
        </thead>
        <tbody id="tablaNetbooks">
          <?php foreach ($deviceRows as $row): ?>
          <?php if (!isset($row['id'])) { continue; } ?>
            <tr class="hover:bg-gray-100">
              <td class="py-2 px-4 border-b text-center"><?= htmlspecialchars($row['carrito']) ?></td>
              <td class="py-2 px-4 border-b text-center"><?= htmlspecialchars($row['numero']) ?></td>
              <td class="py-2 px-4 border-b text-center"><?= htmlspecialchars($row['numero_serie']) ?></td>
              <td class="py-2 px-4 border-b text-center"><?= htmlspecialchars($row['fecha_adquisicion']) ?></td>
              <td class="py-2 px-4 border-b text-center">
                <select data-current="<?= htmlspecialchars($row['estado']) ?>" data-prev-value="<?= htmlspecialchars($row['estado']) ?>" onchange="actualizarEstado(this, <?= (int) ($row['id'] ?? 0) ?>)" class="border rounded px-2 py-1 text-center">
                  <?php foreach ($estados as $estado): ?>
                    <option value="<?= $estado ?>" <?= ($row['estado'] === $estado) ? 'selected' : '' ?>>
                      <?= $estado ?>
                    </option>
                  <?php endforeach; ?>
                </select>
              </td>
              <td class="py-2 px-4">
                <form action="editar_observacion.php" method="POST" class="flex">
                  <input type="hidden" name="csrf" value="<?= $csrf ?>">
                  <input type="hidden" name="id" value="<?= (int) ($row['id'] ?? 0) ?>">
                  <input name="observaciones" value="<?= htmlspecialchars($row['observaciones']) ?>" class="border p-1 rounded w-full text-sm">
                  <button class="ml-2 px-2 bg-yellow-500 text-white text-sm rounded">💾</button>
                </form>
              </td>
              <td class="py-2 px-4">
                <a href="eliminar_netbook.php?id=<?= (int) ($row['id'] ?? 0) ?>" onclick="return confirm('¿Eliminar esta netbook?')" class="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded text-sm">Eliminar</a>
              </td>
            </tr>
          <?php endforeach; ?>
        </tbody>
      </table>
    </div>

    <script>
      function filtrarTabla() {
        const input = document.getElementById("searchInput");
        const filter = input.value.toLowerCase();
        const tabla = document.getElementById("tablaNetbooks");
        const filas = tabla.getElementsByTagName("tr");

        for (let i = 0; i < filas.length; i++) {
          const fila = filas[i];
          const celdas = fila.getElementsByTagName("td");
          let textoFila = "";

          for (let j = 0; j < celdas.length; j++) {
            textoFila += celdas[j].textContent.toLowerCase() + " ";
          }

          fila.style.display = textoFila.indexOf(filter) > -1 ? "" : "none";
        }
      }

      function actualizarEstado(selectElement, id) {
        const nuevoEstado = selectElement.value;
        const previousValue = selectElement.dataset.prevValue || selectElement.dataset.current || selectElement.defaultValue || '';
        const params = new URLSearchParams();
        params.append('id', id);
        params.append('estado', nuevoEstado);

        fetch('update_estado.php', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
              'Accept': 'application/json'
            },
            body: params.toString()
          })
          .then(async response => {
            const data = await response.json().catch(() => null);
            if (!response.ok || !data || data.success !== true) {
              const message = data && data.error ? data.error : 'No se pudo actualizar el estado.';
              throw new Error(message);
            }
            selectElement.dataset.prevValue = nuevoEstado;
          })
          .catch(error => {
            alert('Error al actualizar el estado: ' + error.message);
            if (previousValue !== '') {
              selectElement.value = previousValue;
            }
          });
      }
    </script>
  </main>
  </div>
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

  <!-- Modal Créditos -->
  <div id="popupCreditos" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 hidden transition-shadow 0.3s">
    <div class="bg-white rounded-lg shadow-lg p-6 max-w-md text-center relative">
      <h2 class="text-2xl font-bold mb-4">Créditos</h2>
      <p class="text-gray-700 mb-4">
        Panel de Noticias desarrollado por el equipo de ATTP:<br>
        👨‍💻 Desarrolladores:
        <br>- Liz Vera
        - Uma Perez
        - Michael Martinez
        - Jaco Alfaro
        - Kevin Mamani
        - Brenda Huanca
        <br>🤗 Colaboradores:
        <br>- Fabricio Toscano
        <br>👨‍🏫 Profesores:
        <br>- Nicolas Bogarin
      </p>
      <button onclick="cerrarCreditos()" class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700">
        Cerrar
      </button>
    </div>
  </div>

  <!-- Función de Mostrar/Ocultar el Popup de Créditos -->
  <script>
    function mostrarCreditos() {
      document.getElementById('popupCreditos').classList.remove('hidden');
    }

    function cerrarCreditos() {
      document.getElementById('popupCreditos').classList.add('hidden');
    }
  </script>
</body>

</html>