-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 30-12-2024 a las 09:23:54
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `et20plataforma`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `apellido` varchar(255) NOT NULL,
  `mail` varchar(250) NOT NULL,
  `rol` int(11) NOT NULL,
  `contrasena` varchar(255) DEFAULT NULL,
  `id_admin` bigint(20) DEFAULT NULL,
  `id_preceptor` bigint(20) UNSIGNED DEFAULT NULL,
  `id_docente` bigint(20) UNSIGNED DEFAULT NULL,
  `id_alumno` bigint(20) UNSIGNED DEFAULT NULL,
  `alumno_dni` bigint(20) DEFAULT NULL,
  `status` bigint(20) DEFAULT NULL,
  `dni` bigint(20) DEFAULT NULL,
  `telefono` bigint(20) DEFAULT NULL,
  `direccion` bigint(20) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `local_password` varchar(255) DEFAULT NULL,
  `modified_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `anio` int(255) DEFAULT NULL,
  `division` int(255) DEFAULT NULL,
  `foto_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `apellido`, `mail`, `rol`, `contrasena`, `id_admin`, `id_preceptor`, `id_docente`, `id_alumno`, `alumno_dni`, `status`, `dni`, `telefono`, `direccion`, `fecha_nacimiento`, `created_at`, `local_password`, `modified_at`, `anio`, `division`, `foto_url`) VALUES
(1, 'User Test', 'Test', 'test@google.com', 1, '', 0, NULL, NULL, NULL, NULL, 0, 0, 0, 0, '0000-00-00', '2024-12-22 20:37:55', '', '2024-12-22 20:37:55', 0, 0, NULL),
(8, 'Nicolas Nahuel', 'Fernandez Bogarin', 'nicola.fernandez@bue.edu.ar', 4, 'default_password', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, '2024-12-30 08:21:10', NULL, '2024-12-30 08:21:10', 1, 6, 'https://lh3.googleusercontent.com/a/ACg8ocJbGKkb3dnspfrTpcmGzAP5wVwnbqteV9-oG8uzMQ0l2mYyFCc=s96-c');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `mail_2` (`mail`),
  ADD UNIQUE KEY `mail_idx` (`mail`),
  ADD UNIQUE KEY `id_alumno` (`id_alumno`),
  ADD UNIQUE KEY `alumno_dni_2` (`alumno_dni`),
  ADD UNIQUE KEY `alumno_dni_3` (`alumno_dni`),
  ADD UNIQUE KEY `alumno_dni_4` (`alumno_dni`),
  ADD UNIQUE KEY `idx_alumno_dni` (`alumno_dni`),
  ADD UNIQUE KEY `alumno_dni_5` (`alumno_dni`),
  ADD UNIQUE KEY `alumno_dni_6` (`alumno_dni`),
  ADD UNIQUE KEY `alumno_dni_idx` (`alumno_dni`),
  ADD UNIQUE KEY `id_docente_unique` (`id_docente`),
  ADD KEY `idx_usuarios_alumno_dni` (`alumno_dni`),
  ADD KEY `usuarios_id_preceptor_foreign` (`id_preceptor`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
