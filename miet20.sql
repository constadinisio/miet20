CREATE TABLE `asistencia`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `alumno_id` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `fecha` DATE NULL DEFAULT 'DEFAULT NULL',
    `estado` VARCHAR(100) NULL DEFAULT 'DEFAULT NULL',
    `justificacion` BIGINT NULL,
    PRIMARY KEY(`id`)
);
CREATE TABLE `boletines`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `curso_id` BIGINT NULL,
    `alumno_id` BIGINT NULL DEFAULT 'DEFAULT NULL',
    `alumno_dni` BIGINT NULL,
    `bimestre` VARCHAR(255) NULL DEFAULT 'DEFAULT NULL',
    `materia_id` BIGINT NULL,
    `notas` BIGINT NULL,
    PRIMARY KEY(`id`)
);
CREATE TABLE `evaluaciones`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `alumno_id` BIGINT NULL,
    `docente_id` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `materia_id` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `fecha` DATE NULL DEFAULT 'DEFAULT NULL',
    `descripcion` TEXT NULL DEFAULT 'DEFAULT NULL',
    PRIMARY KEY(`id`)
);
CREATE TABLE `inventario`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `item` VARCHAR(255) NULL DEFAULT 'DEFAULT NULL',
    `cantidad` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `disponible` TINYINT(1) NULL DEFAULT 'DEFAULT NULL',
    PRIMARY KEY(`id`)
);
CREATE TABLE `libro_temas`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `id_docente` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `tema` TEXT NULL DEFAULT 'DEFAULT NULL',
    `fecha` DATE NULL DEFAULT 'DEFAULT NULL',
    `curso_id` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `estado_firma` BOOLEAN NULL,
    PRIMARY KEY(`id`)
);
CREATE TABLE `notas`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `alumno_id` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `materia_id` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `nota` DECIMAL(3, 2) NULL DEFAULT 'DEFAULT NULL',
    `tema` TEXT NULL DEFAULT 'DEFAULT NULL',
    `fecha` DATE NULL DEFAULT 'DEFAULT NULL',
    PRIMARY KEY(`id`)
);
CREATE TABLE `notificaciones`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `mail` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `asunto` TEXT NULL,
    `mensaje` TEXT NULL DEFAULT 'DEFAULT NULL',
    `leido` BOOLEAN NULL DEFAULT 'DEFAULT NULL',
    `fecha_envio` DATE NULL DEFAULT 'DEFAULT NULL',
    PRIMARY KEY(`id`)
);
CREATE TABLE `tickets`(
    `id` BIGINT(20) UNSIGNED NOT NULL,
    `mail` INT(11) NULL DEFAULT 'DEFAULT NULL',
    `nombre` BIGINT NOT NULL,
    `apellido` BIGINT NOT NULL,
    `descripcion` TEXT NULL DEFAULT 'DEFAULT NULL',
    `estado` VARCHAR(20) NULL DEFAULT 'DEFAULT NULL',
    `fecha_solicitud` DATE NULL DEFAULT 'DEFAULT NULL',
    `fecha_resolucion` DATE NULL DEFAULT 'DEFAULT NULL',
    PRIMARY KEY(`id`)
);
CREATE TABLE `usuarios`(
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `apellido` VARCHAR(255) NOT NULL,
    `mail` VARCHAR(250) NOT NULL,
    `rol` INT NOT NULL,
    `contrasena` VARCHAR(255) NOT NULL,
    `id_admin` BIGINT NOT NULL,
    `id_preceptor` BIGINT NOT NULL,
    `id_docente` BIGINT NOT NULL,
    `id_alumno` BIGINT NOT NULL,
    `alumno_dni` BIGINT NOT NULL,
    `status` BIGINT NOT NULL
);
CREATE TABLE `cursos`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `anio` BIGINT NOT NULL,
    `division` BIGINT NOT NULL,
    `turno` BIGINT NOT NULL,
    `especialidad` BIGINT NOT NULL,
    `ciclo_lectivo` BIGINT NOT NULL
);
CREATE TABLE `alumno_curso`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `alumno_id` BIGINT NOT NULL,
    `curso_id` BIGINT NOT NULL,
    `estado` ENUM('activo', 'baja') NOT NULL
);
CREATE TABLE `curso_materia`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `curso_id` BIGINT NOT NULL,
    `materia_id` BIGINT NOT NULL,
    `id_docente` BIGINT NOT NULL
);
CREATE TABLE `materias`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `materias` VARCHAR(255) NOT NULL
);
CREATE TABLE `preceptor_curso`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_preceptor` BIGINT NOT NULL,
    `curso_id` BIGINT NOT NULL,
    `ciclo_lectivo` BIGINT NOT NULL
);
ALTER TABLE
    `asistencia` ADD CONSTRAINT `asistencia_alumno_id_foreign` FOREIGN KEY(`alumno_id`) REFERENCES `usuarios`(`id_alumno`);
ALTER TABLE
    `usuarios` ADD CONSTRAINT `usuarios_id_alumno_foreign` FOREIGN KEY(`id_alumno`) REFERENCES `notas`(`alumno_id`);
ALTER TABLE
    `notificaciones` ADD CONSTRAINT `notificaciones_mail_foreign` FOREIGN KEY(`mail`) REFERENCES `usuarios`(`mail`);
ALTER TABLE
    `curso_materia` ADD CONSTRAINT `curso_materia_curso_id_foreign` FOREIGN KEY(`curso_id`) REFERENCES `cursos`(`id`);
ALTER TABLE
    `usuarios` ADD CONSTRAINT `usuarios_id_alumno_foreign` FOREIGN KEY(`id_alumno`) REFERENCES `alumno_curso`(`alumno_id`);
ALTER TABLE
    `curso_materia` ADD CONSTRAINT `curso_materia_materia_id_foreign` FOREIGN KEY(`materia_id`) REFERENCES `materias`(`id`);
ALTER TABLE
    `boletines` ADD CONSTRAINT `boletines_alumno_id_foreign` FOREIGN KEY(`alumno_id`) REFERENCES `usuarios`(`id_alumno`);
ALTER TABLE
    `usuarios` ADD CONSTRAINT `usuarios_alumno_dni_foreign` FOREIGN KEY(`alumno_dni`) REFERENCES `boletines`(`alumno_dni`);
ALTER TABLE
    `notas` ADD CONSTRAINT `notas_materia_id_foreign` FOREIGN KEY(`materia_id`) REFERENCES `materias`(`id`);
ALTER TABLE
    `evaluaciones` ADD CONSTRAINT `evaluaciones_materia_id_foreign` FOREIGN KEY(`materia_id`) REFERENCES `materias`(`id`);
ALTER TABLE
    `tickets` ADD CONSTRAINT `tickets_nombre_foreign` FOREIGN KEY(`nombre`) REFERENCES `usuarios`(`nombre`);
ALTER TABLE
    `notas` ADD CONSTRAINT `notas_materia_id_foreign` FOREIGN KEY(`materia_id`) REFERENCES `boletines`(`materia_id`);
ALTER TABLE
    `libro_temas` ADD CONSTRAINT `libro_temas_id_docente_foreign` FOREIGN KEY(`id_docente`) REFERENCES `usuarios`(`id_docente`);
ALTER TABLE
    `usuarios` ADD CONSTRAINT `usuarios_id_docente_foreign` FOREIGN KEY(`id_docente`) REFERENCES `curso_materia`(`id_docente`);
ALTER TABLE
    `boletines` ADD CONSTRAINT `boletines_curso_id_foreign` FOREIGN KEY(`curso_id`) REFERENCES `cursos`(`id`);
ALTER TABLE
    `evaluaciones` ADD CONSTRAINT `evaluaciones_alumno_id_foreign` FOREIGN KEY(`alumno_id`) REFERENCES `usuarios`(`id_alumno`);
ALTER TABLE
    `usuarios` ADD CONSTRAINT `usuarios_id_docente_foreign` FOREIGN KEY(`id_docente`) REFERENCES `preceptor_curso`(`id_preceptor`);
ALTER TABLE
    `tickets` ADD CONSTRAINT `tickets_apellido_foreign` FOREIGN KEY(`apellido`) REFERENCES `usuarios`(`apellido`);
ALTER TABLE
    `preceptor_curso` ADD CONSTRAINT `preceptor_curso_curso_id_foreign` FOREIGN KEY(`curso_id`) REFERENCES `cursos`(`id`);
ALTER TABLE
    `tickets` ADD CONSTRAINT `tickets_mail_foreign` FOREIGN KEY(`mail`) REFERENCES `usuarios`(`mail`);
ALTER TABLE
    `libro_temas` ADD CONSTRAINT `libro_temas_curso_id_foreign` FOREIGN KEY(`curso_id`) REFERENCES `cursos`(`id`);
ALTER TABLE
    `usuarios` ADD CONSTRAINT `usuarios_id_docente_foreign` FOREIGN KEY(`id_docente`) REFERENCES `evaluaciones`(`docente_id`);
ALTER TABLE
    `alumno_curso` ADD CONSTRAINT `alumno_curso_curso_id_foreign` FOREIGN KEY(`curso_id`) REFERENCES `cursos`(`id`);