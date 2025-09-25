<?php
if (!function_exists('miEt20ApiRequest')) {
    require_once __DIR__ . '/loadEnv.php';

    function miEt20ApiEnsureEnvLoaded(): void
    {
        static $envLoaded = false;
        if ($envLoaded) {
            return;
        }

        if (function_exists('cargarEntorno')) {
            cargarEntorno(__DIR__ . '/../../config/.env');
        }
        date_default_timezone_set('America/Argentina/Buenos_Aires');
        $envLoaded = true;
    }

    function miEt20ApiBaseUrl(): string
    {
        miEt20ApiEnsureEnvLoaded();
        $baseUrl = getenv('API_BASE_URL');
        if (!$baseUrl) {
            $baseUrl = 'http://localhost:3000/api/v1';
        }

        return rtrim($baseUrl, '/');
    }

    function miEt20ApiBuildUrl(string $path, array $query = []): string
    {
        $url = miEt20ApiBaseUrl() . '/' . ltrim($path, '/');
        if (!empty($query)) {
            $url .= '?' . http_build_query($query);
        }

        return $url;
    }

    function miEt20ApiRequest(string $method, string $path, ?array $payload = null, array $options = []): array
    {
        miEt20ApiEnsureEnvLoaded();

        $url = miEt20ApiBuildUrl($path, $options['query'] ?? []);
        $timeout = isset($options['timeout']) ? (int) $options['timeout'] : 15;
        $token = $options['token'] ?? null;
        $additionalHeaders = $options['headers'] ?? [];

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, strtoupper($method));
        curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);

        $headers = array_merge(['Accept: application/json'], $additionalHeaders);
        if (!empty($token)) {
            $headers[] = 'Authorization: Bearer ' . $token;
        }

        if ($payload !== null) {
            $encoded = json_encode($payload, JSON_UNESCAPED_UNICODE);
            if ($encoded === false) {
                throw new RuntimeException('No se pudo serializar el cuerpo de la petición.', 500);
            }
            $headers[] = 'Content-Type: application/json';
            curl_setopt($ch, CURLOPT_POSTFIELDS, $encoded);
        }

        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

        $response = curl_exec($ch);
        if ($response === false) {
            $error = curl_error($ch);
            curl_close($ch);
            throw new RuntimeException('No se pudo contactar con la API: ' . $error, 502);
        }

        $status = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        $decoded = null;
        if ($response !== '' && $status !== 204) {
            $decoded = json_decode($response, true);
            if ($decoded === null && json_last_error() !== JSON_ERROR_NONE) {
                throw new RuntimeException('La API devolvió una respuesta inválida.', 502);
            }
        }

        if ($status >= 400) {
            $message = 'Error en la API';
            if (is_array($decoded)) {
                $message = $decoded['message'] ?? $decoded['error'] ?? $message;
            }

            throw new RuntimeException($message, $status);
        }

        $data = [];
        if (is_array($decoded)) {
            $data = array_key_exists('data', $decoded) ? $decoded['data'] : $decoded;
        }

        return [
            'status' => $status,
            'body' => $decoded,
            'data' => $data,
        ];
    }

    function miEt20ApiStoreTokens(array $tokens): void
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            throw new RuntimeException('La sesión no está activa.', 500);
        }

        $accessToken = isset($tokens['accessToken']) && is_string($tokens['accessToken']) ? $tokens['accessToken'] : null;
        $refreshToken = isset($tokens['refreshToken']) && is_string($tokens['refreshToken']) ? $tokens['refreshToken'] : null;
        $expiresIn = isset($tokens['expiresIn']) ? (int) $tokens['expiresIn'] : null;
        $expiresAt = $expiresIn ? time() + max($expiresIn, 0) : null;

        $_SESSION['api_tokens'] = [
            'accessToken' => $accessToken,
            'refreshToken' => $refreshToken,
            'expiresAt' => $expiresAt,
        ];
    }

    function miEt20ApiApplyAuthData(array $data, array $options = []): array
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            throw new RuntimeException('La sesión no está activa.', 500);
        }

        if (!isset($data['user']) || !is_array($data['user'])) {
            throw new RuntimeException('Respuesta de autenticación inválida.', 500);
        }

        $existingUser = $_SESSION['usuario'] ?? [];
        $user = array_merge($existingUser, $data['user']);
        $_SESSION['usuario'] = $user;

        $roles = [];
        if (!empty($user['roles']) && is_array($user['roles'])) {
            foreach ($user['roles'] as $role) {
                if (!is_array($role)) {
                    continue;
                }
                $roleId = isset($role['id']) ? (int) $role['id'] : null;
                if (!$roleId) {
                    continue;
                }
                $roles[] = [
                    'id' => $roleId,
                    'nombre' => $role['nombre'] ?? '',
                ];
            }
        }
        $_SESSION['roles_disponibles'] = $roles;

        $primaryRole = null;
        if (!empty($user['primaryRole']) && is_array($user['primaryRole'])) {
            $primaryRole = [
                'id' => isset($user['primaryRole']['id']) ? (int) $user['primaryRole']['id'] : null,
                'nombre' => $user['primaryRole']['nombre'] ?? '',
            ];
        } elseif (!empty($roles)) {
            $primaryRole = $roles[0];
        }

        $setActiveRole = $options['setActiveRole'] ?? true;
        $activeRoleOverride = $options['activeRole'] ?? null;

        if ($setActiveRole) {
            $targetRole = $activeRoleOverride ?? $primaryRole;
            if (!empty($targetRole['id'])) {
                $_SESSION['usuario']['rol'] = (int) $targetRole['id'];
                $_SESSION['usuario']['rol_nombre'] = $targetRole['nombre'] ?? '';
                $_SESSION['rol_activo'] = $targetRole['nombre'] ?? null;
            }
        } else {
            if (isset($existingUser['rol'])) {
                $_SESSION['usuario']['rol'] = $existingUser['rol'];
            }
            if (isset($existingUser['rol_nombre'])) {
                $_SESSION['usuario']['rol_nombre'] = $existingUser['rol_nombre'];
            }
            if (isset($existingUser['rol_activo'])) {
                $_SESSION['rol_activo'] = $existingUser['rol_activo'];
            }
        }

        $permissions = is_array($user['permissions'] ?? null) ? $user['permissions'] : [];
        $permNoticias = isset($user['permNoticia'])
            ? (int) $user['permNoticia']
            : (!empty($permissions['noticias']) ? 1 : 0);
        $permGaleria = isset($user['permSubidaArch'])
            ? (int) $user['permSubidaArch']
            : (!empty($permissions['galeria']) ? 1 : 0);

        $_SESSION['usuario']['permNoticia'] = $permNoticias;
        $_SESSION['usuario']['permSubidaArch'] = $permGaleria;

        if (!empty($data['tokens']) && is_array($data['tokens'])) {
            miEt20ApiStoreTokens($data['tokens']);
        }

        return [
            'user' => $user,
            'roles' => $roles,
            'tokens' => $data['tokens'] ?? [],
            'primaryRole' => $primaryRole,
        ];
    }

    function miEt20ApiRefreshSession(): array
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            throw new RuntimeException('La sesión no está activa.', 500);
        }

        $refreshToken = $_SESSION['api_tokens']['refreshToken'] ?? null;
        if (!$refreshToken) {
            throw new RuntimeException('No hay un refresh token disponible.', 401);
        }

        $response = miEt20ApiRequest('POST', '/auth/refresh-token', [
            'refreshToken' => $refreshToken,
        ]);

        if (!is_array($response['data'] ?? null)) {
            throw new RuntimeException('Respuesta de la API inválida al refrescar la sesión.', 502);
        }

        miEt20ApiApplyAuthData($response['data'], ['setActiveRole' => false]);

        return $response['data'];
    }

    function miEt20ApiAuthenticatedRequest(string $method, string $path, ?array $payload = null, array $options = []): array
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            throw new RuntimeException('La sesión no está activa.', 500);
        }

        $tokens = $_SESSION['api_tokens'] ?? [];
        $accessToken = $tokens['accessToken'] ?? null;
        $refreshToken = $tokens['refreshToken'] ?? null;

        if (!$accessToken && $refreshToken) {
            try {
                miEt20ApiRefreshSession();
                $tokens = $_SESSION['api_tokens'] ?? [];
                $accessToken = $tokens['accessToken'] ?? null;
            } catch (RuntimeException $exception) {
                if ($exception->getCode() !== 401) {
                    throw $exception;
                }
            }
        }

        if (!$accessToken) {
            throw new RuntimeException('No hay un token de acceso válido en la sesión.', 401);
        }

        try {
            return miEt20ApiRequest($method, $path, $payload, array_merge($options, [
                'token' => $accessToken,
            ]));
        } catch (RuntimeException $exception) {
            if ($exception->getCode() === 401 && $refreshToken) {
                miEt20ApiRefreshSession();
                $newAccessToken = $_SESSION['api_tokens']['accessToken'] ?? null;
                if ($newAccessToken) {
                    return miEt20ApiRequest($method, $path, $payload, array_merge($options, [
                        'token' => $newAccessToken,
                    ]));
                }
            }

            throw $exception;
        }
    }
}
