<?php
if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}

require_once __DIR__ . '/../../includes/api_client.php';

function spei_is_authorized(): bool
{
    return isset($_SESSION['usuario']) && (int) ($_SESSION['usuario']['rol'] ?? 0) === 5;
}

function spei_require_role(bool $redirectOnFail = true): void
{
    if (spei_is_authorized()) {
        return;
    }

    if ($redirectOnFail) {
        header('Location: /login.php?error=rol');
        exit;
    }

    throw new RuntimeException('Acceso no autorizado', 403);
}

function spei_assert_authorized(): void
{
    if (!spei_is_authorized()) {
        throw new RuntimeException('Acceso no autorizado', 403);
    }
}

function spei_call_api(string $method, string $path, ?array $payload = null, array $options = []): array
{
    $apiOptions = $options;
    if (isset($options['query'])) {
        $apiOptions['query'] = $options['query'];
    }

    return miEt20ApiAuthenticatedRequest($method, $path, $payload, $apiOptions);
}

function spei_set_flash(string $type, string $message): void
{
    if (!isset($_SESSION['spei_flash']) || !is_array($_SESSION['spei_flash'])) {
        $_SESSION['spei_flash'] = [];
    }

    if (!isset($_SESSION['spei_flash'][$type]) || !is_array($_SESSION['spei_flash'][$type])) {
        $_SESSION['spei_flash'][$type] = [];
    }

    $_SESSION['spei_flash'][$type][] = $message;
}

function spei_consume_flash(): array
{
    $messages = $_SESSION['spei_flash'] ?? [];
    unset($_SESSION['spei_flash']);

    return is_array($messages) ? $messages : [];
}

function spei_redirect(string $path): void
{
    header('Location: ' . $path);
    exit;
}

function spei_normalize_date_input(?string $value): ?string
{
    $raw = trim((string) ($value ?? ''));
    if ($raw === '') {
        return null;
    }

    $formats = [
        'Y-m-d',
        'd/m/Y',
        'd-m-Y'
    ];

    foreach ($formats as $format) {
        $date = \DateTimeImmutable::createFromFormat($format, $raw);
        if ($date instanceof \DateTimeImmutable) {
            $errors = \DateTimeImmutable::getLastErrors();
            if (!empty($errors['warning_count']) || !empty($errors['error_count'])) {
                continue;
            }

            return $date->format('Y-m-d');
        }
    }

    throw new RuntimeException('La fecha indicada es inválida. Usá el formato DD/MM/AAAA.');
}

function spei_format_display_date(?string $value): string
{
    $raw = trim((string) ($value ?? ''));
    if ($raw === '' || $raw === '0000-00-00') {
        return '';
    }

    $iso = \DateTimeImmutable::createFromFormat('Y-m-d', $raw);
    if ($iso instanceof \DateTimeImmutable) {
        return $iso->format('d/m/Y');
    }

    $latin = \DateTimeImmutable::createFromFormat('d/m/Y', $raw);
    if ($latin instanceof \DateTimeImmutable) {
        return $latin->format('d/m/Y');
    }

    return $raw;
}

function spei_format_datetime(?string $date, ?string $time): string
{
    $formattedDate = spei_format_display_date($date);
    $formattedTime = trim((string) ($time ?? ''));

    if ($formattedDate === '') {
        return $formattedTime;
    }

    if ($formattedTime === '') {
        return $formattedDate;
    }

    return $formattedDate . ' ' . $formattedTime;
}
