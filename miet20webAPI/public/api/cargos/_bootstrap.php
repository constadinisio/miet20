<?php
declare(strict_types=1);


require_once __DIR__ . '/../../../backend/includes/api_client.php';

function cargosEnsureAdminJson(): void
{
    if (session_status() !== PHP_SESSION_ACTIVE) {
        session_start();
    }

    header('Content-Type: application/json; charset=utf-8');

    $rol = isset($_SESSION['usuario']['rol']) ? (int) $_SESSION['usuario']['rol'] : 0;
    if ($rol !== 1) {
        http_response_code(403);
        echo json_encode([
            'ok' => false,
            'error' => 'Acceso denegado'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

/**
 * @param array<string,mixed>|null $payload
 * @param array<string,mixed> $options
 *
 * @return array<mixed>
 */
function cargosApiRequest(string $method, string $path, ?array $payload = null, array $options = []): array
{
    $response = miEt20ApiAuthenticatedRequest($method, $path, $payload, $options);

    $data = $response['data'] ?? [];
    if (is_array($data)) {
        return $data;
    }

    return [];
}

/**
 * @param mixed $payload
 */
function cargosJsonResponse($payload, int $statusCode = 200): void
{
    http_response_code($statusCode);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

/**
 * @param array<string,mixed>|null $errorPayload
 */
function cargosHandleThrowable(Throwable $throwable, ?array $errorPayload = null): void
{
    $status = (int) $throwable->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    $payload = $errorPayload ?? [];
    if (!array_key_exists('error', $payload)) {
        $payload['error'] = $throwable->getMessage();
    }

    cargosJsonResponse($payload, $status);
}
