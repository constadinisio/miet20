# Configuración de Conexión a la API - Miet20 Desktop

## Archivo de configuración externo

- Ruta sugerida: `config/application.json` (editable por despliegue).
- Ejemplo:
  ```json
  {
    "apiBaseUrl": "https://api.miet20.test/api/v1",
    "timeoutMs": 10000,
    "connectRetryMs": 2000,
    "notificationsRefreshMs": 60000
  }
  ```
- Mantener el sufijo `/api/v1` para respetar el versionado de la API.

## Clase `ApiConfig`

- Responsabilidades:
  - Leer el archivo JSON usando `Files.readString(Path.of("config/application.json"))`.
  - Mapear a un POJO/record (`record ApiSettings(String apiBaseUrl, int timeoutMs, int connectRetryMs, int notificationsRefreshMs)`).
  - Exponer getters y permitir recarga (releer cuando se detecte cambio o se solicite manualmente).
  - Validar formato de URL y tiempos mayores a cero.

## Cliente HTTP reutilizable (`ApiClient`)

- Basado en `java.net.http.HttpClient` configurado con:
  - `connectTimeout` usando `timeoutMs`.
  - `Executor` dedicado para peticiones.
  - `Version.HTTP_2` si el backend lo soporta.
- Métodos helper: `sendGet`, `sendPost`, `sendPut`, `sendDelete` que reciben ruta relativa y payload opcional.
- Manejo de errores:
  - 401 → invocar `TokenManager.refresh()` y reintentar una sola vez.
  - 4xx → lanzar excepción custom (`ApiClientException`) con detalle del cuerpo.
  - 5xx → mostrar mensaje de mantenimiento y registrar logs.

## Token Manager

- Almacena `accessToken`, `refreshToken`, expiraciones.
- Persistencia opcional en archivo cifrado si se requiere “recordarme”.
- API:
  - `setTokens(TokenResponse response)`.
  - `getAuthorizationHeader()`.
  - `refreshIfNeeded()`.

## Servicios de ejemplo

### HealthService
```java
public class HealthService {
    private final ApiClient apiClient;

    public HealthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public HealthStatus check() {
        HttpResponse<String> response = apiClient.sendGet("/health");
        return objectMapper.readValue(response.body(), HealthStatus.class);
    }
}
```

### AuthService
```java
public class AuthService {
    private final ApiClient apiClient;
    private final TokenManager tokenManager;

    public AuthService(ApiClient apiClient, TokenManager tokenManager) {
        this.apiClient = apiClient;
        this.tokenManager = tokenManager;
    }

    public UserSession login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        HttpResponse<String> response = apiClient.sendPost("/auth/login", request);
        LoginResponse loginResponse = objectMapper.readValue(response.body(), LoginResponse.class);
        tokenManager.setTokens(loginResponse.tokens());
        return loginResponse.userSession();
    }
}
```

## Versionado

- Validar que `HealthStatus.version()` sea `"v1"`.
- Si se publica `v2`, el archivo de configuración debe actualizarse (`"https://api.miet20.test/api/v2"`).
- Mantener constantes en un solo lugar para evitar hardcodear rutas en la UI.

