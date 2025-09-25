<?php
if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}

require_once __DIR__ . '/../includes/api_client.php';

// Cargar plantillas
$templates = include __DIR__ . '/notificaciones_templates.php';

function miEt20NotificacionesListarRaw(): array
{
    return miEt20ApiAuthenticatedRequest('GET', '/notificaciones');
}

function miEt20NotificacionesMarcarLeidaRaw(int $recipientId): array
{
    return miEt20ApiAuthenticatedRequest(
        'PATCH',
        '/notificaciones/destinatarios/' . $recipientId . '/leida'
    );
}

function miEt20NotificacionesConfirmarRaw(int $recipientId): array
{
    return miEt20ApiAuthenticatedRequest(
        'PATCH',
        '/notificaciones/destinatarios/' . $recipientId . '/confirmar'
    );
}

function miEt20NotificacionesCrearGrupoRaw(string $nombre, ?string $descripcion, array $miembros = []): array
{
    $filteredMembers = array_values(array_unique(array_filter(
        array_map(
            static fn ($value) => (int) $value,
            $miembros
        ),
        static fn ($value) => $value > 0
    )));

    $payload = [
        'nombre' => $nombre,
        'descripcion' => $descripcion !== null && $descripcion !== '' ? $descripcion : null,
        'miembros' => $filteredMembers,
    ];

    return miEt20ApiAuthenticatedRequest('POST', '/notificaciones/grupos', $payload);
}

function miEt20NotificacionesEliminarGrupoRaw(int $grupoId): array
{
    return miEt20ApiAuthenticatedRequest('DELETE', '/notificaciones/grupos/' . $grupoId);
}

function crear_notificacion($template_key, $params, $destinatarios, $creador_id = null)
{
    global $templates;

    if (!isset($templates[$template_key])) {
        throw new InvalidArgumentException("Plantilla $template_key no definida");
    }

    if (!is_array($destinatarios) || empty($destinatarios)) {
        throw new InvalidArgumentException('Se requiere al menos un destinatario para la notificación');
    }

    $tpl = $templates[$template_key](...$params);

    $recipientIds = array_values(array_unique(array_filter(
        array_map(
            static fn ($value) => (int) $value,
            $destinatarios
        ),
        static fn ($value) => $value > 0
    )));

    if (empty($recipientIds)) {
        throw new InvalidArgumentException('Los destinatarios proporcionados no son válidos');
    }

    $payload = [
        'titulo' => $tpl['titulo'],
        'mensaje' => $tpl['mensaje'],
        'tipo' => 'INDIVIDUAL',
        'destinatario_ids' => $recipientIds,
    ];

    if (!empty($tpl['tipo'])) {
        $payload['tipo_especial'] = $tpl['tipo'];
    }

    if ($creador_id !== null) {
        $payload['remitente_id'] = (int) $creador_id;
    }

    $response = miEt20ApiAuthenticatedRequest('POST', '/notificaciones', $payload);

    return $response['data'] ?? [];
}
