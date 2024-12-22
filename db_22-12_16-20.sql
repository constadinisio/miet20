-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 22, 2024 at 08:18 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `et20plataforma`
--

-- --------------------------------------------------------

--
-- Table structure for table `usuarios`
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
  `division` int(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `apellido`, `mail`, `rol`, `contrasena`, `id_admin`, `id_preceptor`, `id_docente`, `id_alumno`, `alumno_dni`, `status`, `dni`, `telefono`, `direccion`, `fecha_nacimiento`, `created_at`, `local_password`, `modified_at`, `anio`, `division`) VALUES
(1, 'User Test', 'Test', 'test@google.com', 1, '', 0, NULL, NULL, NULL, NULL, 0, 0, 0, 0, '0000-00-00', '2024-12-22 17:37:55', '', '2024-12-22 17:37:55', 0, 0),
(2, 'Usuario de Google', 'Test', 'nicola.fernandez@bue.edu.ar', 1, '', 0, NULL, NULL, NULL, NULL, 0, 0, 0, 0, '0000-00-00', '2024-12-22 17:45:02', '', '2024-12-22 17:45:02', 0, 0),
(3, 'Usuario', 'constantino.dinisio1@gmail.com', 'constantino.dinisio1@gmail.com', 1, 'default_password', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, '2024-12-22 18:51:42', NULL, '2024-12-22 18:51:42', NULL, NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `usuarios`
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
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `usuarios_id_docente_foreign` FOREIGN KEY (`id_docente`) REFERENCES `evaluaciones` (`docente_id`),
  ADD CONSTRAINT `usuarios_id_preceptor_foreign` FOREIGN KEY (`id_preceptor`) REFERENCES `preceptor_curso` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
