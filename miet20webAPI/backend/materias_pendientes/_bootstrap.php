<?php
declare(strict_types=1);

require_once __DIR__ . '/../includes/api_client.php';

function materiasPendientesEnsureSession(): void
{
    if (session_status() !== PHP_SESSION_ACTIVE) {
        session_start();
    }
}

function materiasPendientesEnsureJsonResponse(array $allowedRoles = [1]): void
{
    materiasPendientesEnsureSession();

    header('Content-Type: application/json; charset=utf-8');

    if (empty($allowedRoles)) {
        return;
    }

    $rolActual = isset($_SESSION['usuario']['rol']) ? (int) $_SESSION['usuario']['rol'] : 0;
    if (!in_array($rolActual, $allowedRoles, true)) {
        http_response_code(403);
        echo json_encode([
            'error' => 'Acceso denegado'
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

/**
 * @param array<string,mixed>|null $payload
 * @param array<string,mixed> $options
 *
 * @return array{status:int,data:mixed,body:mixed}
 */
function materiasPendientesApiRequest(string $method, string $path, ?array $payload = null, array $options = []): array
{
    materiasPendientesEnsureSession();

    $response = miEt20ApiAuthenticatedRequest($method, $path, $payload, $options);

    return [
        'status' => isset($response['status']) ? (int) $response['status'] : 200,
        'data' => $response['data'] ?? null,
        'body' => $response['body'] ?? null,
    ];
}

/**
 * @param array{status:int,data:mixed,body:mixed} $response
 * @return array<mixed>
 */
function materiasPendientesResponseItems(array $response): array
{
    $data = $response['data'] ?? null;

    if (is_array($data)) {
        if (array_is_list($data)) {
            return $data;
        }

        if (isset($data['items']) && is_array($data['items'])) {
            return $data['items'];
        }
    }

    return [];
}

/**
 * @param mixed $payload
 */
function materiasPendientesJsonResponse($payload, int $statusCode = 200): void
{
    http_response_code($statusCode);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

function materiasPendientesHandleThrowable(Throwable $throwable): void
{
    $status = (int) $throwable->getCode();
    if ($status < 100 || $status > 599) {
        $status = 500;
    }

    $message = $throwable->getMessage();
    if ($status === 401) {
        $message = 'Sesión expirada o inválida';
    }

    materiasPendientesJsonResponse([
        'error' => $message,
    ], $status);
}
