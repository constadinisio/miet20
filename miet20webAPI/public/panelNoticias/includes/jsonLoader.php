<?php

declare(strict_types=1);

if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}

require_once __DIR__ . '/../../../backend/includes/api_client.php';

const NEWS_ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const NEWS_MAX_COVER_SIZE_BYTES = 2097152; // 2MB

function noticiasListar(array $params = []): array
{
    $query = [];

    $page = isset($params['page']) ? (int) $params['page'] : 1;
    $limit = isset($params['limit']) ? (int) $params['limit'] : 20;

    $query['page'] = max($page, 1);
    $query['limit'] = max(min($limit, 50), 1);

    if (!empty($params['status'])) {
        $query['status'] = (string) $params['status'];
    }

    if (!empty($params['search'])) {
        $query['search'] = (string) $params['search'];
    }

    if (!empty($params['includeArchived'])) {
        $query['includeArchived'] = '1';
    }

    if (!empty($params['sortBy'])) {
        $query['sortBy'] = (string) $params['sortBy'];
    }

    if (!empty($params['sortDirection'])) {
        $sortDirection = strtolower((string) $params['sortDirection']);
        $query['sortDirection'] = $sortDirection === 'asc' ? 'asc' : 'desc';
    }

    $response = miEt20ApiAuthenticatedRequest('GET', '/news', null, ['query' => $query]);
    $data = is_array($response['data'] ?? null) ? $response['data'] : [];

    return [
        'items' => is_array($data['items'] ?? null) ? $data['items'] : [],
        'pagination' => is_array($data['pagination'] ?? null) ? $data['pagination'] : [],
    ];
}

function noticiasObtener(int $noticiaId): ?array
{
    $response = miEt20ApiAuthenticatedRequest('GET', '/news/' . $noticiaId);
    return is_array($response['data'] ?? null) ? $response['data'] : null;
}

function noticiasCrear(array $payload): array
{
    $response = miEt20ApiAuthenticatedRequest('POST', '/news', $payload);
    return is_array($response['data'] ?? null) ? $response['data'] : [];
}

function noticiasActualizar(int $noticiaId, array $payload): array
{
    $response = miEt20ApiAuthenticatedRequest('PUT', '/news/' . $noticiaId, $payload);
    return is_array($response['data'] ?? null) ? $response['data'] : [];
}

function noticiasEliminar(int $noticiaId): void
{
    miEt20ApiAuthenticatedRequest('DELETE', '/news/' . $noticiaId);
}

function noticiasActualizarPortadaDesdeArchivo(int $noticiaId, array $archivo): ?array
{
    $error = $archivo['error'] ?? UPLOAD_ERR_NO_FILE;
    if ($error === UPLOAD_ERR_NO_FILE) {
        return null;
    }

    if ($error !== UPLOAD_ERR_OK) {
        throw new RuntimeException('No se pudo procesar la imagen de portada subida.');
    }

    $rutaTemporal = $archivo['tmp_name'] ?? '';
    if ($rutaTemporal === '' || !is_uploaded_file($rutaTemporal)) {
        throw new RuntimeException('El archivo de portada no es válido.');
    }

    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mime = $finfo->file($rutaTemporal) ?: '';
    if (!in_array($mime, NEWS_ALLOWED_MIME_TYPES, true)) {
        throw new RuntimeException('Solo se permiten imágenes JPEG, PNG o WebP como portada.');
    }

    $size = isset($archivo['size']) ? (int) $archivo['size'] : 0;
    if ($size <= 0 || $size > NEWS_MAX_COVER_SIZE_BYTES) {
        throw new RuntimeException('La imagen de portada supera el tamaño máximo permitido (2MB).');
    }

    $contenido = file_get_contents($rutaTemporal);
    if ($contenido === false) {
        throw new RuntimeException('No se pudo leer la imagen de portada.');
    }

    $nombreOriginal = (string) ($archivo['name'] ?? 'portada');

    $response = miEt20ApiAuthenticatedRequest('POST', '/news/' . $noticiaId . '/cover', [
        'coverData' => base64_encode($contenido),
        'coverMime' => $mime,
        'coverName' => $nombreOriginal,
    ]);

    return is_array($response['data'] ?? null) ? $response['data'] : null;
}
