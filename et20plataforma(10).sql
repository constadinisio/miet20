-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 31, 2025 at 01:22 AM
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
-- Table structure for table `actividades_evaluables`
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

-- --------------------------------------------------------

--
-- Table structure for table `alumno_curso`
--

CREATE TABLE `alumno_curso` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `estado` varchar(50) DEFAULT 'activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `asistencia_general`
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
-- Table structure for table `asistencia_materia`
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

-- --------------------------------------------------------

--
-- Table structure for table `boletin`
--

CREATE TABLE `boletin` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `anio_lectivo` int(11) NOT NULL,
  `periodo` varchar(50) NOT NULL,
  `fecha_emision` datetime NOT NULL DEFAULT current_timestamp(),
  `observaciones` text DEFAULT NULL,
  `estado` varchar(50) DEFAULT 'borrador',
  `publicado_at` datetime DEFAULT NULL,
  `creado_por` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `calificaciones`
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

-- --------------------------------------------------------

--
-- Table structure for table `calificacion_boletin`
--

CREATE TABLE `calificacion_boletin` (
  `id` int(11) NOT NULL,
  `boletin_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `nota_numerica` decimal(5,2) DEFAULT NULL,
  `nota_conceptual` varchar(50) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `certificado`
--

CREATE TABLE `certificado` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `motivo` text NOT NULL,
  `codigo_qr` varchar(50) NOT NULL,
  `fecha_emision` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_validez` datetime DEFAULT NULL,
  `estado` varchar(50) DEFAULT 'borrador',
  `creado_por` int(11) NOT NULL,
  `firmado_por` int(11) DEFAULT NULL,
  `firmado_at` datetime DEFAULT NULL,
  `archivo_url` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `configuracion_notificaciones`
--

CREATE TABLE `configuracion_notificaciones` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `recibir_email` tinyint(1) DEFAULT 1,
  `recibir_push` tinyint(1) DEFAULT 1,
  `solo_urgentes` tinyint(1) DEFAULT 0,
  `horario_inicio` time DEFAULT '08:00:00',
  `horario_fin` time DEFAULT '18:00:00',
  `dias_semana` set('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') DEFAULT 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contenidos_libro`
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
-- Table structure for table `cursos`
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

-- --------------------------------------------------------

--
-- Table structure for table `detalle_certificado`
--

CREATE TABLE `detalle_certificado` (
  `id` int(11) NOT NULL,
  `certificado_id` int(11) NOT NULL,
  `materia_id` int(11) DEFAULT NULL,
  `descripcion` text DEFAULT NULL,
  `nota` decimal(5,2) DEFAULT NULL,
  `anio_cursado` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `firmas_libro`
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
-- Table structure for table `grupos_notificacion`
--

CREATE TABLE `grupos_notificacion` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `creador_id` int(11) NOT NULL,
  `tipo_grupo` enum('CURSO','MATERIA','COMISION','PERSONALIZADO') NOT NULL,
  `activo` tinyint(1) DEFAULT 1,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `grupos_notificacion_miembros`
--

CREATE TABLE `grupos_notificacion_miembros` (
  `id` int(11) NOT NULL,
  `grupo_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `fecha_agregado` timestamp NOT NULL DEFAULT current_timestamp(),
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `grupos_notificacion_personalizados`
--

CREATE TABLE `grupos_notificacion_personalizados` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `creador_id` int(11) NOT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT NULL,
  `fecha_eliminacion` datetime DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `horarios_materia`
--

CREATE TABLE `horarios_materia` (
  `id` int(11) NOT NULL,
  `profesor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `dia_semana` varchar(50) DEFAULT NULL,
  `hora_inicio` time DEFAULT NULL,
  `hora_fin` time DEFAULT NULL,
  `es_contraturno` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `libros_temas`
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
-- Table structure for table `materias`
--

CREATE TABLE `materias` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `codigo` varchar(255) DEFAULT NULL,
  `estado` varchar(50) DEFAULT NULL,
  `categoria` varchar(50) DEFAULT NULL,
  `es_contraturno` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `miembros_grupos_personalizados`
--

CREATE TABLE `miembros_grupos_personalizados` (
  `id` int(11) NOT NULL,
  `grupo_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `fecha_agregado` datetime DEFAULT current_timestamp(),
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `netbooks`
--

CREATE TABLE `netbooks` (
  `id` int(11) NOT NULL,
  `carrito` varchar(1) NOT NULL COMMENT 'Letra del carrito (A-Z)',
  `numero` varchar(2) NOT NULL COMMENT 'Número de netbook en el carrito (1-30)',
  `numero_serie` varchar(100) NOT NULL COMMENT 'Número de serie único',
  `fecha_adquisicion` varchar(20) DEFAULT NULL COMMENT 'Fecha de adquisición (DD/MM/YYYY)',
  `estado` varchar(20) NOT NULL DEFAULT 'En uso' COMMENT 'Estado: En uso, Dañada, Hurto, Obsoleta',
  `observaciones` text DEFAULT NULL COMMENT 'Observaciones adicionales',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notas`
--

CREATE TABLE `notas` (
  `id` int(11) NOT NULL,
  `alumno_id` varchar(20) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `trabajo_id` int(11) NOT NULL,
  `nota` decimal(4,2) NOT NULL,
  `fecha_carga` timestamp NOT NULL DEFAULT current_timestamp(),
  `creado_por` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notas_bimestrales`
--

CREATE TABLE `notas_bimestrales` (
  `id` int(11) NOT NULL,
  `alumno_id` varchar(20) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `periodo` varchar(20) NOT NULL,
  `nota` decimal(4,2) NOT NULL,
  `promedio_actividades` decimal(4,2) NOT NULL,
  `fecha_carga` timestamp NOT NULL DEFAULT current_timestamp(),
  `creado_por` int(11) DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'Normal'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notificaciones`
--

CREATE TABLE `notificaciones` (
  `id` int(11) NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `contenido` text NOT NULL,
  `tipo_notificacion` enum('INDIVIDUAL','ROL','GRUPO') NOT NULL,
  `remitente_id` int(11) NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_expiracion` timestamp NULL DEFAULT NULL,
  `prioridad` enum('BAJA','NORMAL','ALTA','URGENTE') DEFAULT 'NORMAL',
  `estado` enum('ACTIVA','ARCHIVADA','ELIMINADA') DEFAULT 'ACTIVA',
  `requiere_confirmacion` tinyint(1) DEFAULT 0,
  `icono` varchar(50) DEFAULT 'info',
  `color` varchar(20) DEFAULT '#007bff'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notificaciones_destinatarios`
--

CREATE TABLE `notificaciones_destinatarios` (
  `id` int(11) NOT NULL,
  `notificacion_id` int(11) NOT NULL,
  `destinatario_id` int(11) NOT NULL,
  `fecha_leida` timestamp NULL DEFAULT NULL,
  `fecha_confirmada` timestamp NULL DEFAULT NULL,
  `estado_lectura` enum('NO_LEIDA','LEIDA','CONFIRMADA') DEFAULT 'NO_LEIDA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `observaciones_asistencia`
--

CREATE TABLE `observaciones_asistencia` (
  `id` int(11) NOT NULL,
  `alumno_id` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `observacion` text NOT NULL,
  `creado_por` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `plantillas_notificacion`
--

CREATE TABLE `plantillas_notificacion` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `titulo_plantilla` varchar(200) NOT NULL,
  `contenido_plantilla` text NOT NULL,
  `variables_disponibles` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`variables_disponibles`)),
  `categoria` varchar(50) DEFAULT NULL,
  `creador_id` int(11) NOT NULL,
  `publica` tinyint(1) DEFAULT 0,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `plantilla_boletin`
--

CREATE TABLE `plantilla_boletin` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `contenido` text NOT NULL,
  `encabezado` text DEFAULT NULL,
  `pie` text DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `plantilla_certificado`
--

CREATE TABLE `plantilla_certificado` (
  `id` int(11) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `contenido` text NOT NULL,
  `encabezado` text DEFAULT NULL,
  `pie` text DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `preceptor_curso`
--

CREATE TABLE `preceptor_curso` (
  `id` int(11) NOT NULL,
  `preceptor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `estado` varchar(50) DEFAULT 'activo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `prestamos`
--

CREATE TABLE `prestamos` (
  `Prestamo_ID` int(11) NOT NULL,
  `Netbook_ID` varchar(10) NOT NULL COMMENT 'Identificador de la netbook (ej: A1, B5)',
  `Fecha_Prestamo` varchar(20) NOT NULL COMMENT 'Fecha del préstamo (DD/MM/YYYY)',
  `Fecha_Devolucion` varchar(20) DEFAULT NULL COMMENT 'Fecha de devolución (DD/MM/YYYY)',
  `Hora_Prestamo` varchar(10) NOT NULL COMMENT 'Hora del préstamo (HH:MM)',
  `Hora_Devolucion` varchar(10) DEFAULT NULL COMMENT 'Hora de devolución (HH:MM)',
  `Curso` varchar(10) NOT NULL COMMENT 'Curso del alumno (ej: 1°A, 2°B)',
  `Alumno` varchar(100) NOT NULL COMMENT 'Nombre completo del alumno',
  `Tutor` varchar(100) NOT NULL COMMENT 'Nombre completo del tutor/profesor',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `profesor_curso_materia`
--

CREATE TABLE `profesor_curso_materia` (
  `id` int(11) NOT NULL,
  `profesor_id` int(11) NOT NULL,
  `curso_id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `estado` varchar(50) DEFAULT 'activo',
  `es_contraturno` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `promedios`
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

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `stock`
--

CREATE TABLE `stock` (
  `ID` int(11) NOT NULL,
  `Cod_Barra` varchar(50) NOT NULL COMMENT 'Código de barras del equipo',
  `Estado` varchar(20) NOT NULL DEFAULT 'En uso' COMMENT 'Estado: En uso, Dañada, Hurto, Obsoleta',
  `Observaciones` text DEFAULT NULL COMMENT 'Observaciones del equipo',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `trabajos`
--

CREATE TABLE `trabajos` (
  `id` int(11) NOT NULL,
  `materia_id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `rol` varchar(50) DEFAULT NULL,
  `contrasena` varchar(255) DEFAULT NULL,
  `status` tinyint(1) DEFAULT 1,
  `dni` varchar(50) DEFAULT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `direccion` text DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `modified_at` timestamp NULL DEFAULT NULL,
  `anio` varchar(50) DEFAULT NULL,
  `division` varchar(50) DEFAULT NULL,
  `foto_url` text DEFAULT NULL,
  `ficha_censal` int(7) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usuario_roles`
--

CREATE TABLE `usuario_roles` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `rol_id` int(11) NOT NULL,
  `is_default` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `actividades_evaluables`
--
ALTER TABLE `actividades_evaluables`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `alumno_curso`
--
ALTER TABLE `alumno_curso`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`);

--
-- Indexes for table `asistencia_general`
--
ALTER TABLE `asistencia_general`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indexes for table `asistencia_materia`
--
ALTER TABLE `asistencia_materia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indexes for table `boletin`
--
ALTER TABLE `boletin`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indexes for table `calificaciones`
--
ALTER TABLE `calificaciones`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `calificacion_boletin`
--
ALTER TABLE `calificacion_boletin`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `boletin_materia` (`boletin_id`,`materia_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indexes for table `certificado`
--
ALTER TABLE `certificado`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo_qr` (`codigo_qr`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `creado_por` (`creado_por`),
  ADD KEY `firmado_por` (`firmado_por`);

--
-- Indexes for table `configuracion_notificaciones`
--
ALTER TABLE `configuracion_notificaciones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_usuario_config` (`usuario_id`);

--
-- Indexes for table `contenidos_libro`
--
ALTER TABLE `contenidos_libro`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `cursos`
--
ALTER TABLE `cursos`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `detalle_certificado`
--
ALTER TABLE `detalle_certificado`
  ADD PRIMARY KEY (`id`),
  ADD KEY `certificado_id` (`certificado_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indexes for table `firmas_libro`
--
ALTER TABLE `firmas_libro`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `grupos_notificacion`
--
ALTER TABLE `grupos_notificacion`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_creador` (`creador_id`),
  ADD KEY `idx_tipo` (`tipo_grupo`);

--
-- Indexes for table `grupos_notificacion_miembros`
--
ALTER TABLE `grupos_notificacion_miembros`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_grupo_usuario` (`grupo_id`,`usuario_id`),
  ADD KEY `idx_grupo` (`grupo_id`),
  ADD KEY `idx_usuario` (`usuario_id`);

--
-- Indexes for table `grupos_notificacion_personalizados`
--
ALTER TABLE `grupos_notificacion_personalizados`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_name_per_user` (`nombre`,`creador_id`,`activo`),
  ADD KEY `creador_id` (`creador_id`);

--
-- Indexes for table `horarios_materia`
--
ALTER TABLE `horarios_materia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profesor_id` (`profesor_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indexes for table `libros_temas`
--
ALTER TABLE `libros_temas`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `materias`
--
ALTER TABLE `materias`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `miembros_grupos_personalizados`
--
ALTER TABLE `miembros_grupos_personalizados`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_member_per_group` (`grupo_id`,`usuario_id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indexes for table `netbooks`
--
ALTER TABLE `netbooks`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `carrito_numero` (`carrito`,`numero`),
  ADD UNIQUE KEY `numero_serie` (`numero_serie`);

--
-- Indexes for table `notas`
--
ALTER TABLE `notas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `materia_id` (`materia_id`),
  ADD KEY `idx_notas_alumno_materia` (`alumno_id`,`materia_id`),
  ADD KEY `idx_notas_trabajo` (`trabajo_id`);

--
-- Indexes for table `notas_bimestrales`
--
ALTER TABLE `notas_bimestrales`
  ADD PRIMARY KEY (`id`),
  ADD KEY `materia_id` (`materia_id`),
  ADD KEY `idx_bimestrales_alumno_materia` (`alumno_id`,`materia_id`),
  ADD KEY `idx_bimestrales_periodo` (`periodo`);

--
-- Indexes for table `notificaciones`
--
ALTER TABLE `notificaciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `remitente_id` (`remitente_id`),
  ADD KEY `idx_fecha_creacion` (`fecha_creacion`),
  ADD KEY `idx_estado` (`estado`),
  ADD KEY `idx_tipo` (`tipo_notificacion`);

--
-- Indexes for table `notificaciones_destinatarios`
--
ALTER TABLE `notificaciones_destinatarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_notif_destinatario` (`notificacion_id`,`destinatario_id`),
  ADD KEY `idx_destinatario_estado` (`destinatario_id`,`estado_lectura`),
  ADD KEY `idx_notificacion` (`notificacion_id`);

--
-- Indexes for table `observaciones_asistencia`
--
ALTER TABLE `observaciones_asistencia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `alumno_id` (`alumno_id`),
  ADD KEY `creado_por` (`creado_por`);

--
-- Indexes for table `plantillas_notificacion`
--
ALTER TABLE `plantillas_notificacion`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_categoria` (`categoria`),
  ADD KEY `idx_creador` (`creador_id`);

--
-- Indexes for table `plantilla_boletin`
--
ALTER TABLE `plantilla_boletin`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `plantilla_certificado`
--
ALTER TABLE `plantilla_certificado`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `preceptor_curso`
--
ALTER TABLE `preceptor_curso`
  ADD PRIMARY KEY (`id`),
  ADD KEY `preceptor_id` (`preceptor_id`),
  ADD KEY `curso_id` (`curso_id`);

--
-- Indexes for table `prestamos`
--
ALTER TABLE `prestamos`
  ADD PRIMARY KEY (`Prestamo_ID`);

--
-- Indexes for table `profesor_curso_materia`
--
ALTER TABLE `profesor_curso_materia`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profesor_id` (`profesor_id`),
  ADD KEY `curso_id` (`curso_id`),
  ADD KEY `materia_id` (`materia_id`);

--
-- Indexes for table `promedios`
--
ALTER TABLE `promedios`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `stock`
--
ALTER TABLE `stock`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `Cod_Barra` (`Cod_Barra`);

--
-- Indexes for table `trabajos`
--
ALTER TABLE `trabajos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_trabajos_materia` (`materia_id`);

--
-- Indexes for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `usuario_roles`
--
ALTER TABLE `usuario_roles`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `alumno_curso`
--
ALTER TABLE `alumno_curso`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `asistencia_general`
--
ALTER TABLE `asistencia_general`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `asistencia_materia`
--
ALTER TABLE `asistencia_materia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `boletin`
--
ALTER TABLE `boletin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `calificacion_boletin`
--
ALTER TABLE `calificacion_boletin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `certificado`
--
ALTER TABLE `certificado`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `configuracion_notificaciones`
--
ALTER TABLE `configuracion_notificaciones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cursos`
--
ALTER TABLE `cursos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `detalle_certificado`
--
ALTER TABLE `detalle_certificado`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `grupos_notificacion`
--
ALTER TABLE `grupos_notificacion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `grupos_notificacion_miembros`
--
ALTER TABLE `grupos_notificacion_miembros`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `grupos_notificacion_personalizados`
--
ALTER TABLE `grupos_notificacion_personalizados`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `horarios_materia`
--
ALTER TABLE `horarios_materia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `materias`
--
ALTER TABLE `materias`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `miembros_grupos_personalizados`
--
ALTER TABLE `miembros_grupos_personalizados`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `netbooks`
--
ALTER TABLE `netbooks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notas`
--
ALTER TABLE `notas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notas_bimestrales`
--
ALTER TABLE `notas_bimestrales`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notificaciones`
--
ALTER TABLE `notificaciones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notificaciones_destinatarios`
--
ALTER TABLE `notificaciones_destinatarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `observaciones_asistencia`
--
ALTER TABLE `observaciones_asistencia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `plantillas_notificacion`
--
ALTER TABLE `plantillas_notificacion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `plantilla_boletin`
--
ALTER TABLE `plantilla_boletin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `plantilla_certificado`
--
ALTER TABLE `plantilla_certificado`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `preceptor_curso`
--
ALTER TABLE `preceptor_curso`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `prestamos`
--
ALTER TABLE `prestamos`
  MODIFY `Prestamo_ID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `profesor_curso_materia`
--
ALTER TABLE `profesor_curso_materia`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `stock`
--
ALTER TABLE `stock`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `trabajos`
--
ALTER TABLE `trabajos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `usuario_roles`
--
ALTER TABLE `usuario_roles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `alumno_curso`
--
ALTER TABLE `alumno_curso`
  ADD CONSTRAINT `alumno_curso_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `alumno_curso_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`);

--
-- Constraints for table `asistencia_general`
--
ALTER TABLE `asistencia_general`
  ADD CONSTRAINT `asistencia_general_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `asistencia_general_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `asistencia_general_ibfk_3` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `asistencia_materia`
--
ALTER TABLE `asistencia_materia`
  ADD CONSTRAINT `asistencia_materia_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `asistencia_materia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `asistencia_materia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`),
  ADD CONSTRAINT `asistencia_materia_ibfk_4` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `boletin`
--
ALTER TABLE `boletin`
  ADD CONSTRAINT `boletin_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `boletin_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `boletin_ibfk_3` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `calificacion_boletin`
--
ALTER TABLE `calificacion_boletin`
  ADD CONSTRAINT `calificacion_boletin_ibfk_1` FOREIGN KEY (`boletin_id`) REFERENCES `boletin` (`id`),
  ADD CONSTRAINT `calificacion_boletin_ibfk_2` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);

--
-- Constraints for table `certificado`
--
ALTER TABLE `certificado`
  ADD CONSTRAINT `certificado_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `certificado_ibfk_2` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `certificado_ibfk_3` FOREIGN KEY (`firmado_por`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `configuracion_notificaciones`
--
ALTER TABLE `configuracion_notificaciones`
  ADD CONSTRAINT `configuracion_notificaciones_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `detalle_certificado`
--
ALTER TABLE `detalle_certificado`
  ADD CONSTRAINT `detalle_certificado_ibfk_1` FOREIGN KEY (`certificado_id`) REFERENCES `certificado` (`id`),
  ADD CONSTRAINT `detalle_certificado_ibfk_2` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);

--
-- Constraints for table `grupos_notificacion`
--
ALTER TABLE `grupos_notificacion`
  ADD CONSTRAINT `grupos_notificacion_ibfk_1` FOREIGN KEY (`creador_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `grupos_notificacion_miembros`
--
ALTER TABLE `grupos_notificacion_miembros`
  ADD CONSTRAINT `grupos_notificacion_miembros_ibfk_1` FOREIGN KEY (`grupo_id`) REFERENCES `grupos_notificacion` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `grupos_notificacion_miembros_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `grupos_notificacion_personalizados`
--
ALTER TABLE `grupos_notificacion_personalizados`
  ADD CONSTRAINT `grupos_notificacion_personalizados_ibfk_1` FOREIGN KEY (`creador_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `horarios_materia`
--
ALTER TABLE `horarios_materia`
  ADD CONSTRAINT `horarios_materia_ibfk_1` FOREIGN KEY (`profesor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `horarios_materia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `horarios_materia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);

--
-- Constraints for table `miembros_grupos_personalizados`
--
ALTER TABLE `miembros_grupos_personalizados`
  ADD CONSTRAINT `miembros_grupos_personalizados_ibfk_1` FOREIGN KEY (`grupo_id`) REFERENCES `grupos_notificacion_personalizados` (`id`),
  ADD CONSTRAINT `miembros_grupos_personalizados_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `notas`
--
ALTER TABLE `notas`
  ADD CONSTRAINT `notas_ibfk_1` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`),
  ADD CONSTRAINT `notas_ibfk_2` FOREIGN KEY (`trabajo_id`) REFERENCES `trabajos` (`id`);

--
-- Constraints for table `notas_bimestrales`
--
ALTER TABLE `notas_bimestrales`
  ADD CONSTRAINT `notas_bimestrales_ibfk_1` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);

--
-- Constraints for table `notificaciones`
--
ALTER TABLE `notificaciones`
  ADD CONSTRAINT `notificaciones_ibfk_1` FOREIGN KEY (`remitente_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notificaciones_destinatarios`
--
ALTER TABLE `notificaciones_destinatarios`
  ADD CONSTRAINT `notificaciones_destinatarios_ibfk_1` FOREIGN KEY (`notificacion_id`) REFERENCES `notificaciones` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notificaciones_destinatarios_ibfk_2` FOREIGN KEY (`destinatario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `observaciones_asistencia`
--
ALTER TABLE `observaciones_asistencia`
  ADD CONSTRAINT `observaciones_asistencia_ibfk_1` FOREIGN KEY (`alumno_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `observaciones_asistencia_ibfk_2` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `plantillas_notificacion`
--
ALTER TABLE `plantillas_notificacion`
  ADD CONSTRAINT `plantillas_notificacion_ibfk_1` FOREIGN KEY (`creador_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `preceptor_curso`
--
ALTER TABLE `preceptor_curso`
  ADD CONSTRAINT `preceptor_curso_ibfk_1` FOREIGN KEY (`preceptor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `preceptor_curso_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`);

--
-- Constraints for table `profesor_curso_materia`
--
ALTER TABLE `profesor_curso_materia`
  ADD CONSTRAINT `profesor_curso_materia_ibfk_1` FOREIGN KEY (`profesor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `profesor_curso_materia_ibfk_2` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  ADD CONSTRAINT `profesor_curso_materia_ibfk_3` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);

--
-- Constraints for table `trabajos`
--
ALTER TABLE `trabajos`
  ADD CONSTRAINT `trabajos_ibfk_1` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
