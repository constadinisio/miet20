<?php
declare(strict_types=1);

require __DIR__ . '/../public/includes/apiClient.php';

if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}

function tests_assert(bool $condition, string $message): void
{
    if (!$condition) {
        throw new RuntimeException($message);
    }
}

function tests_reset_api_client_state(): void
{
    $_SESSION = [];

    api_reset_http_client();
    api_clear_stored_tokens();
    api_reset_metrics();
}

function test_api_request_successful_response(): void
{
    tests_reset_api_client_state();

    $captured = [];
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options) use (&$captured) {
        $captured = compact('method', 'url', 'headers', 'body', 'options');

        return [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['foo' => 'bar'],
            ], JSON_THROW_ON_ERROR),
        ];
    });

    $data = api_get_data('demo/recurso');

    tests_assert($data['foo'] === 'bar', 'El helper debe devolver la clave "foo" con valor "bar".');
    tests_assert($captured['method'] === 'GET', 'La llamada debe usar el método GET.');
    tests_assert(strpos($captured['url'], '/demo/recurso') !== false, 'La URL debe incluir el path solicitado.');
    tests_assert(in_array('Accept: application/json', $captured['headers'], true), 'Debe enviar el header Accept JSON.');
}

function test_api_request_refreshes_token_on_unauthorized(): void
{
    tests_reset_api_client_state();

    api_store_tokens([
        'accessToken' => 'token-expirado',
        'refreshToken' => 'refresh-123',
        'expiresAt' => time() - 10,
    ]);

    $calls = [];
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$calls): array {
        $calls[] = compact('method', 'url', 'body');

        if (strpos($url, 'auth/refresh-token') !== false) {
            return [
                'status' => 200,
                'body' => json_encode([
                    'status' => 'success',
                    'data' => [
                        'accessToken' => 'token-nuevo',
                        'refreshToken' => 'refresh-nuevo',
                        'expiresAt' => time() + 3600,
                    ],
                ], JSON_THROW_ON_ERROR),
            ];
        }

        if (count($calls) === 1) {
            return [
                'status' => 401,
                'body' => json_encode([
                    'status' => 'error',
                    'message' => 'Token expirado',
                ], JSON_THROW_ON_ERROR),
            ];
        }

        return [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['ok' => true],
            ], JSON_THROW_ON_ERROR),
        ];
    });

    $response = api_get_data('privado/recurso');
    tests_assert(($response['ok'] ?? false) === true, 'Debe obtener datos tras refrescar el token.');
    tests_assert(count($calls) === 3, 'Deben registrarse tres llamadas (fallo inicial, refresh y reintento).');

    $tokens = api_token_storage()->getTokens();
    tests_assert(($tokens['accessToken'] ?? '') === 'token-nuevo', 'El token de acceso debe actualizarse en la sesión.');
}

function test_api_request_propagates_error_when_refresh_fails(): void
{
    tests_reset_api_client_state();

    api_store_tokens([
        'accessToken' => 'token-vencido',
        'refreshToken' => 'refresh-invalido',
        'expiresAt' => time() - 5,
    ]);

    $calls = [];
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$calls): array {
        $calls[] = compact('method', 'url', 'body');

        if (strpos($url, 'auth/refresh-token') !== false) {
            return [
                'status' => 400,
                'body' => json_encode([
                    'status' => 'error',
                    'message' => 'Refresh inválido',
                ], JSON_THROW_ON_ERROR),
            ];
        }

        return [
            'status' => 401,
            'body' => json_encode([
                'status' => 'error',
                'message' => 'No autorizado',
            ], JSON_THROW_ON_ERROR),
        ];
    });

    try {
        api_get_data('privado/recurso');
        tests_assert(false, 'Debe propagarse la excepción cuando el refresh falla.');
    } catch (ApiException $exception) {
        tests_assert($exception->getStatusCode() === 400, 'El error del refresh debe conservar su código HTTP.');
        tests_assert(strpos($exception->getMessage(), 'Refresh') !== false, 'El mensaje debe indicar la causa del fallo.');
    }

    tests_assert(count($calls) === 2, 'Solo deben realizarse la llamada original y el intento de refresh.');
}

function test_api_request_skips_refresh_when_token_is_forced(): void
{
    tests_reset_api_client_state();

    $callCounter = 0;
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$callCounter) {
        $callCounter++;

        return [
            'status' => 401,
            'body' => json_encode([
                'status' => 'error',
                'message' => 'Public endpoint',
            ], JSON_THROW_ON_ERROR),
        ];
    });

    try {
        api_get_data('publico', ['token' => '']);
        tests_assert(false, 'No debe refrescar ni ocultar el error para endpoints públicos.');
    } catch (ApiException $exception) {
        tests_assert($exception->getStatusCode() === 401, 'El error 401 debe propagarse para llamadas públicas.');
    }

    tests_assert($callCounter === 1, 'Solo debe ejecutarse una llamada sin intentar refresh.');
}

function test_api_request_retries_on_server_error_and_registers_metrics(): void
{
    tests_reset_api_client_state();

    $callCounter = 0;
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$callCounter) {
        $callCounter++;

        if ($callCounter < 3) {
            return [
                'status' => 503,
                'body' => json_encode([
                    'status' => 'error',
                    'message' => 'Servicio temporalmente no disponible',
                ], JSON_THROW_ON_ERROR),
            ];
        }

        return [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['ok' => true],
            ], JSON_THROW_ON_ERROR),
        ];
    });

    $response = api_get_data('salud/check');

    tests_assert($response['ok'] === true, 'La solicitud debe tener éxito tras reintentar.');
    tests_assert($callCounter === 3, 'Debe realizar dos reintentos antes de obtener una respuesta exitosa.');

    $entries = api_get_metrics_entries();
    tests_assert(count($entries) === 3, 'Se deben registrar métricas por cada intento.');
    tests_assert((int) $entries[0]['status'] === 503, 'La primera métrica debe reflejar el status 503.');
    tests_assert((int) $entries[2]['status'] === 200, 'La última métrica debe reflejar el éxito final.');
}

function test_api_request_honors_custom_max_retries_option(): void
{
    tests_reset_api_client_state();

    $callCounter = 0;
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$callCounter) {
        $callCounter++;

        return [
            'status' => 502,
            'body' => json_encode([
                'status' => 'error',
                'message' => 'Error de gateway',
            ], JSON_THROW_ON_ERROR),
        ];
    });

    try {
        api_get_data('estado', [
            'max_retries' => 1,
            'retry_delay_ms' => 0,
        ]);
        tests_assert(false, 'Debe lanzar excepción tras agotar reintentos personalizados.');
    } catch (ApiException $exception) {
        tests_assert($exception->getStatusCode() === 502, 'El código de error debe conservarse tras fallar todos los intentos.');
    }

    tests_assert($callCounter === 2, 'Con max_retries=1 deben realizarse dos intentos en total.');

    $summary = api_get_metrics_summary();
    tests_assert($summary['total'] === 2, 'El resumen debe reflejar ambos intentos.');
    tests_assert($summary['errors'] === 2, 'Todos los intentos fallidos deben contarse como errores.');
}

function test_token_monitor_registers_alerts_when_token_near_expiration(): void
{
    tests_reset_api_client_state();

    api_store_tokens([
        'accessToken' => 'token-demo',
        'refreshToken' => 'refresh-demo',
        'expiresAt' => time() + 120,
    ]);

    api_set_http_client(static function (string $method, string $url, array $headers, ?string $body, array $options = []): array {
        return [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['ok' => true],
            ], JSON_THROW_ON_ERROR),
        ];
    });

    api_get_data('demo/token-alert');

    $entries = api_get_metrics_entries();
    $alerts = array_values(array_filter($entries, static function ($entry): bool {
        if (!is_array($entry) || ($entry['path'] ?? '') !== 'tokens/expiration') {
            return false;
        }

        $context = $entry['context'] ?? [];

        return is_array($context) && ($context['state'] ?? '') === 'expiring';
    }));

    tests_assert(count($alerts) >= 1, 'Debe registrarse al menos una alerta de expiración próxima.');
    tests_assert(($alerts[0]['context']['secondsRemaining'] ?? 0) <= 300, 'La alerta debe incluir los segundos restantes dentro del umbral.');
}

function test_news_service_crud_flow_and_errors(): void
{
    tests_reset_api_client_state();

    $requests = [];
    $responses = [
        [
            'status' => 201,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['id' => 42, 'title' => 'Demo'],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['id' => 42, 'title' => 'Actualizada'],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
            ], JSON_THROW_ON_ERROR),
        ],
    ];

    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$requests, &$responses): array {
        $requests[] = compact('method', 'url', 'headers', 'body', 'options');

        $response = array_shift($responses);
        if ($response === null) {
            return [
                'status' => 500,
                'body' => json_encode([
                    'status' => 'error',
                    'message' => 'Sin respuesta prevista',
                ], JSON_THROW_ON_ERROR),
            ];
        }

        return $response;
    });

    $created = api_news_create([
        'title' => 'Demo',
        'body' => 'Contenido',
    ]);
    tests_assert((int) ($created['id'] ?? 0) === 42, 'La creación debe devolver el ID 42.');

    $updated = api_news_update(42, ['title' => 'Actualizada']);
    tests_assert(($updated['title'] ?? '') === 'Actualizada', 'La actualización debe propagar el nuevo título.');

    api_news_delete(42);
    tests_assert(count($requests) === 3, 'El flujo CRUD debe realizar tres solicitudes.');

    $entries = api_get_metrics_entries();
    tests_assert(count($entries) === 3, 'Se deben registrar métricas para cada operación.');
    tests_assert(($entries[0]['context']['module'] ?? '') === 'noticias', 'Las métricas deben etiquetarse con el módulo noticias.');

    api_reset_metrics();

    api_set_http_client(static function (): array {
        return [
            'status' => 422,
            'body' => json_encode([
                'status' => 'error',
                'message' => 'Título requerido',
            ], JSON_THROW_ON_ERROR),
        ];
    });

    try {
        api_news_create(['title' => '', 'body' => '']);
        tests_assert(false, 'Debe lanzarse ApiException ante errores de validación.');
    } catch (ApiException $exception) {
        tests_assert($exception->getStatusCode() === 422, 'El código de estado debe ser 422.');
        tests_assert(strpos($exception->getMessage(), 'Título') !== false, 'El mensaje debe mencionar el título requerido.');
    }
}

function test_courses_metrics_context_includes_module_mode(): void
{
    tests_reset_api_client_state();

    api_set_http_client(static function (string $method, string $url, array $headers, ?string $body, array $options = []): array {
        return [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => [
                    'items' => [
                        ['id' => 1, 'anio' => '1', 'division' => 'A'],
                    ],
                ],
            ], JSON_THROW_ON_ERROR),
        ];
    });

    $courses = api_courses_list();
    tests_assert(count($courses) === 1, 'Debe obtenerse un curso desde la API simulada.');

    $entries = api_get_metrics_entries();
    tests_assert(count($entries) === 1, 'La invocación debe registrar una única métrica.');

    $context = $entries[0]['context'] ?? [];
    tests_assert(($context['module'] ?? '') === 'cursos', 'El contexto debe incluir el módulo cursos.');
    tests_assert(($context['mode'] ?? '') === 'API_MODE', 'El contexto debe informar el modo API activo.');
}

function test_notifications_service_endpoints(): void
{
    tests_reset_api_client_state();

    $responses = [
        [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => [
                    'items' => [
                        [
                            'id' => 1,
                            'titulo' => 'Aviso',
                        ],
                    ],
                ],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['ok' => true],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['ok' => true],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => [
                    'items' => [
                        ['id' => 7, 'nombre' => 'Grupo'],
                    ],
                ],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 201,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['id' => 9],
            ], JSON_THROW_ON_ERROR),
        ],
        [
            'status' => 204,
            'body' => '',
        ],
    ];

    $requests = [];
    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$responses, &$requests) {
        $requests[] = compact('method', 'url', 'headers', 'body');
        $response = array_shift($responses);

        if ($response === null) {
            return [
                'status' => 500,
                'body' => json_encode([
                    'status' => 'error',
                    'message' => 'Sin respuesta de prueba configurada.',
                ], JSON_THROW_ON_ERROR),
            ];
        }

        return $response;
    });

    $list = api_notifications_list(['estado' => 'ACTIVA']);
    tests_assert(is_array($list) && count($list) === 1, 'El servicio debe devolver la lista normalizada de notificaciones.');
    tests_assert(strpos($requests[0]['url'], '/notifications') !== false, 'La primera solicitud debe apuntar al listado de notificaciones.');
    tests_assert(strpos($requests[0]['url'], 'estado=ACTIVA') !== false, 'Debe propagar los filtros como query string.');

    api_notifications_mark_read(12);
    tests_assert($requests[1]['method'] === 'PATCH', 'Marcar lectura debe usar PATCH.');
    tests_assert(strpos($requests[1]['url'], '/notifications/destinatarios/12/leida') !== false, 'La URL debe incluir el destinatario y la acción leida.');

    api_notifications_confirm(34);
    tests_assert($requests[2]['method'] === 'PATCH', 'Confirmar debe usar PATCH.');
    tests_assert(strpos($requests[2]['url'], '/notifications/destinatarios/34/confirmar') !== false, 'La URL debe incluir el destinatario y la acción confirmar.');

    $groups = api_notification_groups_list();
    tests_assert(is_array($groups) && count($groups) === 1, 'El servicio debe listar los grupos existentes.');
    tests_assert(strpos($requests[3]['url'], '/notifications/groups') !== false, 'La solicitud debe apuntar al listado de grupos.');

    api_notification_groups_create([
        'name' => 'Nuevo',
        'description' => 'Prueba',
        'memberIds' => [1, 2],
    ]);
    tests_assert($requests[4]['method'] === 'POST', 'Crear grupo debe usar POST.');
    tests_assert(strpos($requests[4]['url'], '/notifications/groups') !== false, 'La creación de grupos debe apuntar al endpoint correcto.');

    $body = json_decode((string) $requests[4]['body'], true);
    tests_assert($body['memberIds'] === [1, 2], 'Los miembros enviados deben conservarse como enteros.');

    api_notification_groups_delete(99);
    tests_assert($requests[5]['method'] === 'DELETE', 'Eliminar grupo debe usar DELETE.');
    tests_assert(strpos($requests[5]['url'], '/notifications/groups/99') !== false, 'La URL debe incluir el identificador del grupo.');
}

function test_profile_and_student_helpers_use_expected_routes(): void
{
    tests_reset_api_client_state();

    $requests = [];

    api_set_http_client(function (string $method, string $url, array $headers, ?string $body, array $options = []) use (&$requests) {
        $requests[] = compact('method', 'url', 'headers', 'body', 'options');

        return [
            'status' => 200,
            'body' => json_encode([
                'status' => 'success',
                'data' => ['ok' => true],
            ], JSON_THROW_ON_ERROR),
        ];
    });

    api_users_update_password(5, 'actual', 'nueva');
    tests_assert($requests[0]['method'] === 'PATCH', 'Actualizar contraseña debe usar PATCH.');
    tests_assert(strpos($requests[0]['url'], '/usuarios/5/password') !== false, 'La URL debe contener el usuario y el segmento password.');

    $passwordPayload = json_decode((string) $requests[0]['body'], true, 512, JSON_THROW_ON_ERROR);
    tests_assert($passwordPayload['currentPassword'] === 'actual', 'La contraseña actual debe propagarse al payload.');
    tests_assert($passwordPayload['newPassword'] === 'nueva', 'La contraseña nueva debe propagarse al payload.');

    api_students_update_family(5, [
        'padre_nombre' => 'Juan',
        'padre_tel' => '123',
        'madre_nombre' => 'Ana',
    ]);
    tests_assert($requests[1]['method'] === 'PUT', 'El guardado de datos familiares debe usar PUT.');
    tests_assert(strpos($requests[1]['url'], '/alumnos/5/familia') !== false, 'La URL debe apuntar al recurso de familia.');

    $familyPayload = json_decode((string) $requests[1]['body'], true, 512, JSON_THROW_ON_ERROR);
    tests_assert($familyPayload['padre_nombre'] === 'Juan', 'El nombre del padre debe mantenerse en el payload.');
    tests_assert($familyPayload['madre_nombre'] === 'Ana', 'El nombre de la madre debe mantenerse en el payload.');

    api_students_update_census(5, ['ficha_censal' => 'apto']);
    tests_assert($requests[2]['method'] === 'PUT', 'La ficha censal debe enviarse con PUT.');
    tests_assert(strpos($requests[2]['url'], '/alumnos/5/ficha-censal') !== false, 'La URL debe incluir el segmento ficha-censal.');

    $censusPayload = json_decode((string) $requests[2]['body'], true, 512, JSON_THROW_ON_ERROR);
    tests_assert($censusPayload['ficha_censal'] === 'apto', 'La ficha censal debe propagarse correctamente.');
}

$tests = [
    'test_api_request_successful_response',
    'test_api_request_refreshes_token_on_unauthorized',
    'test_api_request_propagates_error_when_refresh_fails',
    'test_api_request_skips_refresh_when_token_is_forced',
    'test_api_request_retries_on_server_error_and_registers_metrics',
    'test_api_request_honors_custom_max_retries_option',
    'test_notifications_service_endpoints',
    'test_profile_and_student_helpers_use_expected_routes',
    'test_token_monitor_registers_alerts_when_token_near_expiration',
    'test_news_service_crud_flow_and_errors',
    'test_courses_metrics_context_includes_module_mode',
];

$failures = 0;

foreach ($tests as $test) {
    try {
        $test();
        echo "[OK] {$test}" . PHP_EOL;
    } catch (Throwable $throwable) {
        $failures++;
        $message = $throwable->getMessage();
        echo "[FAIL] {$test}: {$message}" . PHP_EOL;
    }
}

if ($failures > 0) {
    exit(1);
}

echo PHP_EOL . "Todos los tests se ejecutaron correctamente." . PHP_EOL;