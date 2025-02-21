-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.2
-- Tiempo de generación: 21-02-2025 a las 05:36:26
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
-- Estructura de tabla para la tabla `actividades_evaluables`
--

CREATE TABLE `actividades_evaluables` (
  `id` int(11) NOT NULL,
  `curso_id` int(11) DEFAULT NULL,
  `materia_id` int(11) DEFAULT NULL,
  `profesor_id` int(11) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `periodo` varchar(50) DEFAULT NULL,
  `estado` varchar(50) DEFAULT NULL,
  `1B` decimal(5,2) DEFAULT NULL,
  `2B` decimal(5,2) DEFAULT NULL,
  `3B` decimal(5,2) DEFAULT NULL,
  `4B` decimal(5,2) DEFAULT NULL,
  `1C` decimal(5,2) DEFAULT NULL,
  `2C` decimal(5,2) DEFAULT NULL,
  `F` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `actividades_evaluables`
--

INSERT INTO `actividades_evaluables` (`id`, `curso_id`, `materia_id`, `profesor_id`, `nombre`, `descripcion`, `fecha`, `periodo`, `estado`, `1B`, `2B`, `3B`, `4B`, `1C`, `2C`, `F`) VALUES
(1, 1, 1, 13, 'Examen Primer Bimestre', 'Evaluación de contenidos del primer bimestre', '2024-04-15', '1B', 'pendiente', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 1, 1, 13, 'Trabajo Práctico', 'Trabajo grupal sobre funciones', '2024-05-01', '2B', 'pendiente', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alumno_curso`
--

CREATE TABLE `alumno_curso` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `estado` varchar(50) DEFAULT 'activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `alumno_curso`
--

INSERT INTO `alumno_curso` (`id`, `alumno_id`, `curso_id`, `estado`) VALUES
(1, 29, 1, 'activo'),
(2, 30, 1, 'activo'),
(3, 31, 1, 'activo'),
(4, 32, 1, 'activo'),
(5, 33, 1, 'activo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `asistencia_general`
--

CREATE TABLE `asistencia_general` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `estado` varchar(2) NOT NULL,
  `creado_por` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `es_contraturno` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `asistencia_materia`
--

CREATE TABLE `asistencia_materia` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `estado` varchar(2) NOT NULL,
  `observaciones` text DEFAULT NULL,
  `creado_por` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `asistencia_materia`
--

INSERT INTO `asistencia_materia` (`id`, `alumno_id`, `curso_id`, `materia_id`, `fecha`, `estado`, `observaciones`, `creado_por`, `created_at`) VALUES
(8, 29, 1, 1, '2025-02-03', 'P', NULL, 29, '2025-02-03 00:43:44'),
(9, 33, 1, 1, '2025-02-03', 'P', NULL, 33, '2025-02-03 00:43:44'),
(10, 30, 1, 1, '2025-02-03', 'A', NULL, 30, '2025-02-03 00:43:44'),
(11, 32, 1, 1, '2025-02-03', 'T', NULL, 32, '2025-02-03 00:43:44'),
(12, 31, 1, 1, '2025-02-03', 'A', NULL, 31, '2025-02-03 00:43:44');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `calificaciones`
--

CREATE TABLE `calificaciones` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) DEFAULT NULL,
  `actividad_id` int(11) DEFAULT NULL,
  `nota` decimal(5,2) DEFAULT NULL,
  `nota_conceptual` varchar(50) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `fecha_carga` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `calificaciones`
--

INSERT INTO `calificaciones` (`id`, `alumno_id`, `actividad_id`, `nota`, `nota_conceptual`, `observaciones`, `fecha_carga`) VALUES
(1, 16, 1, 8.50, 'MB', NULL, '2025-01-31 21:23:00'),
(2, 17, 1, 7.00, 'B', NULL, '2025-01-31 21:23:00'),
(3, 18, 1, 9.00, 'MB', NULL, '2025-01-31 21:23:00'),
(4, 19, 1, 7.50, 'B', NULL, '2025-01-31 21:23:00'),
(5, 20, 1, 8.00, 'MB', NULL, '2025-01-31 21:23:00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `contenidos_libro`
--

CREATE TABLE `contenidos_libro` (
  `id` int(11) NOT NULL,
  `libro_id` int(11) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `contenido` text DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT NULL,
  `fecha_modificacion` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cursos`
--

CREATE TABLE `cursos` (
  `id` int(11) NOT NULL,
  `anio` int(11) NOT NULL,
  `division` int(11) NOT NULL,
  `turno` varchar(50) DEFAULT NULL,
  `estado` varchar(50) DEFAULT 'activo',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cursos`
--

INSERT INTO `cursos` (`id`, `anio`, `division`, `turno`, `estado`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 'mañana', 'activo', '2025-02-02 19:01:02', '2025-02-02 19:01:02'),
(2, 2, 1, 'mañana', 'activo', '2025-02-02 19:01:02', '2025-02-02 19:01:02'),
(3, 3, 1, 'mañana', 'activo', '2025-02-02 19:01:02', '2025-02-02 19:01:02');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `firmas_asistencia`
--

CREATE TABLE `firmas_asistencia` (
  `id` int(11) NOT NULL,
  `profesor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora_firma` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `firmas_libro`
--

CREATE TABLE `firmas_libro` (
  `id` int(11) NOT NULL,
  `libro_id` int(11) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `hora_inicio` time DEFAULT NULL,
  `hora_fin` time DEFAULT NULL,
  `firmado` tinyint(1) DEFAULT NULL,
  `hora_firma` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horarios_materia`
--

CREATE TABLE `horarios_materia` (
  `id` int(11) NOT NULL,
  `profesor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `dia_semana` varchar(50) DEFAULT NULL,
  `hora_inicio` time DEFAULT NULL,
  `hora_fin` time DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `horarios_materia`
--

INSERT INTO `horarios_materia` (`id`, `profesor_id`, `curso_id`, `materia_id`, `dia_semana`, `hora_inicio`, `hora_fin`) VALUES
(6, 26, 1, 1, 'monday', '00:00:00', '23:59:59'),
(7, 26, 1, 1, 'tuesday', '00:00:00', '23:59:59'),
(8, 26, 1, 1, 'wednesday', '00:00:00', '23:59:59'),
(9, 26, 1, 1, 'thursday', '00:00:00', '23:59:59'),
(10, 26, 1, 1, 'friday', '00:00:00', '23:59:59'),
(11, 26, 1, 1, 'saturday', '00:00:00', '23:59:59'),
(12, 26, 1, 1, 'sunday', '00:00:00', '23:59:59');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `libros_temas`
--

CREATE TABLE `libros_temas` (
  `id` int(11) NOT NULL,
  `curso_id` int(11) DEFAULT NULL,
  `materia_id` int(11) DEFAULT NULL,
  `profesor_id` int(11) DEFAULT NULL,
  `anio_lectivo` int(11) DEFAULT NULL,
  `estado` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `materias`
--

CREATE TABLE `materias` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `codigo` varchar(255) DEFAULT NULL,
  `estado` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `materias`
--

INSERT INTO `materias` (`id`, `nombre`, `codigo`, `estado`) VALUES
(1, 'Matemática', 'MAT', 'activo'),
(2, 'Lengua', 'LEN', 'activo'),
(3, 'Historia', 'HIS', 'activo'),
(4, 'Geografía', 'GEO', 'activo'),
(5, 'Física', 'FIS', 'activo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `observaciones_asistencia`
--

CREATE TABLE `observaciones_asistencia` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `observacion` text NOT NULL,
  `creado_por` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `observaciones_asistencia`
--

INSERT INTO `observaciones_asistencia` (`id`, `alumno_id`, `fecha`, `observacion`, `creado_por`, `created_at`) VALUES
(4, 29, '2025-02-02', 'tiene mas de 5 faltas seguidas a taller', 24, '2025-02-02 19:04:44');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `preceptor_curso`
--

CREATE TABLE `preceptor_curso` (
  `id` int(11) NOT NULL,
  `preceptor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `estado` varchar(50) DEFAULT 'activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `preceptor_curso`
--

INSERT INTO `preceptor_curso` (`id`, `preceptor_id`, `curso_id`, `estado`) VALUES
(1, 24, 1, 'activo'),
(2, 24, 2, 'activo'),
(3, 25, 3, 'activo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `prestamos`
--

CREATE TABLE `prestamos` (
  `Prestamo_ID` int(11) NOT NULL,
  `Netbook_ID` varchar(4) NOT NULL,
  `Fecha_Prestamo` varchar(10) DEFAULT NULL,
  `Fecha_Devolucion` varchar(10) DEFAULT NULL,
  `Hora_Prestamo` varchar(5) DEFAULT NULL,
  `Hora_Devolucion` varchar(5) DEFAULT NULL,
  `Curso` varchar(50) DEFAULT NULL,
  `Alumno` varchar(100) DEFAULT NULL,
  `Tutor` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `prestamos`
--

INSERT INTO `prestamos` (`Prestamo_ID`, `Netbook_ID`, `Fecha_Prestamo`, `Fecha_Devolucion`, `Hora_Prestamo`, `Hora_Devolucion`, `Curso`, `Alumno`, `Tutor`) VALUES
(13, 'B1', '19/11/2024', '20/11/2025', '13:44', '17:10', '44', 'Juan', 'Nico'),
(17, 'C1', '26/11/2024', '26/11/2024', '14:00', '17:10', '44', 'MATI', 'WALTER');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `profesor_curso_materia`
--

CREATE TABLE `profesor_curso_materia` (
  `id` int(11) NOT NULL,
  `profesor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `estado` varchar(50) DEFAULT 'activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `profesor_curso_materia`
--

INSERT INTO `profesor_curso_materia` (`id`, `profesor_id`, `curso_id`, `materia_id`, `estado`) VALUES
(1, 26, 1, 1, 'activo'),
(2, 26, 1, 2, 'activo'),
(3, 26, 1, 3, 'activo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `promedios`
--

CREATE TABLE `promedios` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) DEFAULT NULL,
  `curso_id` int(11) DEFAULT NULL,
  `materia_id` int(11) DEFAULT NULL,
  `periodo` varchar(50) DEFAULT NULL,
  `nota_calculada` decimal(5,2) DEFAULT NULL,
  `nota_final` decimal(5,2) DEFAULT NULL,
  `nota_conceptual` varchar(50) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `1B` decimal(5,2) DEFAULT NULL,
  `2B` decimal(5,2) DEFAULT NULL,
  `3B` decimal(5,2) DEFAULT NULL,
  `4B` decimal(5,2) DEFAULT NULL,
  `1C` decimal(5,2) DEFAULT NULL,
  `2C` decimal(5,2) DEFAULT NULL,
  `F` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `promedios`
--

INSERT INTO `promedios` (`id`, `alumno_id`, `curso_id`, `materia_id`, `periodo`, `nota_calculada`, `nota_final`, `nota_conceptual`, `observaciones`, `1B`, `2B`, `3B`, `4B`, `1C`, `2C`, `F`) VALUES
(1, 16, 1, 1, '2024', 8.00, 8.00, NULL, NULL, 8.00, 7.50, 8.00, 8.50, NULL, NULL, NULL),
(2, 17, 1, 1, '2024', 7.50, 7.50, NULL, NULL, 7.00, 7.50, 8.00, 7.50, NULL, NULL, NULL),
(3, 18, 1, 1, '2024', 8.75, 9.00, NULL, NULL, 9.00, 8.50, 9.00, 8.50, NULL, NULL, NULL),
(4, 19, 1, 1, '2024', 7.75, 8.00, NULL, NULL, 7.50, 8.00, 7.50, 8.00, NULL, NULL, NULL),
(5, 20, 1, 1, '2024', 8.13, 8.00, NULL, NULL, 8.00, 8.00, 8.50, 8.00, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`id`, `nombre`) VALUES
(1, 'Admin'),
(2, 'Preceptor'),
(3, 'Profeso'),
(4, 'Alumno');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `stock`
--

CREATE TABLE `stock` (
  `ID` varchar(3) NOT NULL,
  `Cod_Barra` varchar(100) NOT NULL,
  `Estado` enum('Dañada','En uso','Hurto','Obsoleta') DEFAULT 'En uso',
  `Observaciones` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `stock`
--

INSERT INTO `stock` (`ID`, `Cod_Barra`, `Estado`, `Observaciones`) VALUES
('B1', '0303456nananananan', 'En uso', 'AYEDA'),
('C1', 'FAQ434313', 'Dañada', 'hsfihsifhsdf');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `rol` varchar(50) DEFAULT NULL,
  `contrasena` varchar(255) DEFAULT NULL,
  `id_admin` int(11) DEFAULT NULL,
  `id_preceptor` int(11) DEFAULT NULL,
  `id_docente` int(11) DEFAULT NULL,
  `id_alumno` int(11) DEFAULT NULL,
  `alumno_dni` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `dni` varchar(50) DEFAULT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `direccion` text DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `local_password` varchar(255) DEFAULT NULL,
  `modified_at` timestamp NULL DEFAULT NULL,
  `anio` varchar(50) DEFAULT NULL,
  `division` varchar(50) DEFAULT NULL,
  `foto_url` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `apellido`, `mail`, `rol`, `contrasena`, `id_admin`, `id_preceptor`, `id_docente`, `id_alumno`, `alumno_dni`, `status`, `dni`, `telefono`, `direccion`, `fecha_nacimiento`, `created_at`, `local_password`, `modified_at`, `anio`, `division`, `foto_url`) VALUES
(23, 'Juan', 'Admin', 'admin@test.com', '1', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '11111111', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(24, 'María', 'Preceptor', 'preceptor@test.com', '2', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '22222222', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(25, 'Carlos', 'Preceptor', 'preceptor2@test.com', '2', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '22222223', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(26, 'Laura', 'Profesor', 'profesor@test.com', '3', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '33333333', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(27, 'Pedro', 'Profesor', 'profesor2@test.com', '3', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '33333334', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(28, 'Ana', 'Profesor', 'profesor3@test.com', '3', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '33333335', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(29, 'Lucas', 'García', 'alumno1@test.com', '4', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '44444441', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(30, 'Martina', 'López', 'alumno2@test.com', '4', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '44444442', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(31, 'Santiago', 'Rodríguez', 'alumno3@test.com', '4', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '44444443', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(32, 'Valentina', 'Martínez', 'alumno4@test.com', '4', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '44444444', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL),
(33, 'Mateo', 'González', 'attp1@attp.com.ar', '5', 'test', NULL, NULL, NULL, NULL, NULL, 'activo', '44444445', NULL, NULL, NULL, '2025-02-01 01:01:45', NULL, NULL, NULL, NULL, NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `actividades_evaluables`
--
ALTER TABLE `actividades_evaluables`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `alumno_curso`
--
ALTER TABLE `alumno_curso`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`);

--
-- Indices de la tabla `asistencia_general`
--
ALTER TABLE `asistencia_general`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indices de la tabla `asistencia_materia`
--
ALTER TABLE `asistencia_materia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indices de la tabla `calificaciones`
--
ALTER TABLE `calificaciones`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `contenidos_libro`
--
ALTER TABLE `contenidos_libro`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `cursos`
--
ALTER TABLE `cursos`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `firmas_asistencia`
--
ALTER TABLE `firmas_asistencia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profesor_id` (`profesor_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indices de la tabla `firmas_libro`
--
ALTER TABLE `firmas_libro`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `horarios_materia`
--
ALTER TABLE `horarios_materia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profesor_id` (`profesor_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indices de la tabla `libros_temas`
--
ALTER TABLE `libros_temas`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `materias`
--
ALTER TABLE `materias`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `observaciones_asistencia`
--
ALTER TABLE `observaciones_asistencia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indices de la tabla `preceptor_curso`
--
ALTER TABLE `preceptor_curso`
  ADD PRIMARY KEY (`id`),
  ADD KEY `preceptor_id` (`preceptor_id`),
  ADD KEY `curso_id` (`curso_id`);

--
-- Indices de la tabla `profesor_curso_materia`
--
ALTER TABLE `profesor_curso_materia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profesor_id` (`profesor_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indices de la tabla `promedios`
--
ALTER TABLE `promedios`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `alumno_curso`
--
ALTER TABLE `alumno_curso`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `asistencia_general`
--
ALTER TABLE `asistencia_general`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `asistencia_materia`
--
ALTER TABLE `asistencia_materia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT de la tabla `cursos`
--
ALTER TABLE `cursos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `firmas_asistencia`
--
ALTER TABLE `firmas_asistencia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `horarios_materia`
--
ALTER TABLE `horarios_materia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT de la tabla `observaciones_asistencia`
--
ALTER TABLE `observaciones_asistencia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `preceptor_curso`
--
ALTER TABLE `preceptor_curso`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `profesor_curso_materia`
--
ALTER TABLE `profesor_curso_materia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `alumno_curso`
--
ALTER TABLE `alumno_curso`
  ADD CONSTRAINT `alumno_curso_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `alumno_curso_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`);

--
-- Filtros para la tabla `asistencia_general`
--
ALTER TABLE `asistencia_general`
  ADD CONSTRAINT `asistencia_general_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `asistencia_general_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `asistencia_general_ibfk_3` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `asistencia_materia`
--
ALTER TABLE `asistencia_materia`
  ADD CONSTRAINT `asistencia_materia_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `asistencia_materia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `asistencia_materia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`),
  ADD CONSTRAINT `asistencia_materia_ibfk_4` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `firmas_asistencia`
--
ALTER TABLE `firmas_asistencia`
  ADD CONSTRAINT `firmas_asistencia_ibfk_1` FOREIGN KEY (`profesor_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `firmas_asistencia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `profesor_curso_materia` (`curso_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `firmas_asistencia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `profesor_curso_materia` (`materia_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `horarios_materia`
--
ALTER TABLE `horarios_materia`
  ADD CONSTRAINT `horarios_materia_ibfk_1` FOREIGN KEY (`profesor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `horarios_materia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `horarios_materia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);

--
-- Filtros para la tabla `observaciones_asistencia`
--
ALTER TABLE `observaciones_asistencia`
  ADD CONSTRAINT `observaciones_asistencia_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `observaciones_asistencia_ibfk_2` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `preceptor_curso`
--
ALTER TABLE `preceptor_curso`
  ADD CONSTRAINT `preceptor_curso_ibfk_1` FOREIGN KEY (`preceptor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `preceptor_curso_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`);

--
-- Filtros para la tabla `profesor_curso_materia`
--
ALTER TABLE `profesor_curso_materia`
  ADD CONSTRAINT `profesor_curso_materia_ibfk_1` FOREIGN KEY (`profesor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `profesor_curso_materia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `profesor_curso_materia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
