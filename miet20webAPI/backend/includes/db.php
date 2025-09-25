<?php
require_once __DIR__ . '/loadEnv.php';
cargarEntorno(__DIR__ . '/../../config/.env');
date_default_timezone_set('America/Argentina/Buenos_Aires');


$host = $_ENV['DB_HOST'];
$user = $_ENV['DB_USER'];
$password = $_ENV['DB_PASS'];
$database = $_ENV['DB_NAME'];

$conexion = new mysqli($host, $user, $password, $database);

if ($conexion->connect_error) {
    error_log('DB error: '.$conexion->connect_error);
    die('Error interno, intente más tarde.');
}
?>