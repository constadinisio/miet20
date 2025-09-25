<?php
if (!function_exists('admin_call_api')) {
    function admin_call_api(string $method, string $path, ?array $payload = null, array $query = []): array
    {
        $token = $_SESSION['api_tokens']['accessToken'] ?? null;
        if (!$token) {
            throw new RuntimeException('No hay un token de acceso válido en la sesión.', 401);
        }

        $baseUrl = rtrim(getenv('API_BASE_URL') ?: 'http://localhost:3000/api/v1', '/');
        $url = $baseUrl . '/' . ltrim($path, '/');

        if (!empty($query)) {
            $url .= '?' . http_build_query($query);
        }

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, strtoupper($method));
        curl_setopt($ch, CURLOPT_TIMEOUT, 15);

        $headers = [
            'Accept: application/json',
            'Authorization: Bearer ' . $token,
        ];

        if ($payload !== null) {
            $body = json_encode($payload, JSON_UNESCAPED_UNICODE);
            if ($body === false) {
                throw new RuntimeException('No se pudo serializar el cuerpo de la petición.', 500);
            }
            $headers[] = 'Content-Type: application/json';
            curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
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

        if ($response === '' || $status === 204) {
            if ($status >= 400) {
                throw new RuntimeException('Error en la API', $status);
            }
            return [];
        }

        $decoded = json_decode($response, true);
        if ($decoded === null && json_last_error() !== JSON_ERROR_NONE) {
            throw new RuntimeException('La API devolvió una respuesta inválida.', 502);
        }

        if ($status >= 400) {
            $message = is_array($decoded) ? ($decoded['message'] ?? $decoded['error'] ?? 'Error en la API') : 'Error en la API';
            throw new RuntimeException($message, $status);
        }

        return $decoded['data'] ?? $decoded ?? [];
    }
}
