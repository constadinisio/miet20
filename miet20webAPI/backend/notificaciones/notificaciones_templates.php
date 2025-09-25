<?php
// Plantillas de notificaciones automáticas y manuales

return [

    // 1. Avisos de Secretaría a docentes
    'secretaria_aviso' => function($texto) {
        return [
            'titulo' => "Aviso de Secretaría",
            'mensaje' => $texto,
            'tipo' => 'administrativa',
        ];
    },

    // 2. Nueva nota cargada para un alumno
    'nota_cargada' => function($materia, $nota) {
        return [
            'titulo' => "Nueva nota en $materia",
            'mensaje' => "Se cargó una nueva nota ($nota) en la materia $materia.",
            'tipo' => 'academica',
        ];
    },

    // 3. Boletín generado
    'boletin_generado' => function($alumno) {
        return [
            'titulo' => "Boletín disponible",
            'mensaje' => "Se generó un nuevo boletín para $alumno. Ya está disponible para su consulta.",
            'tipo' => 'academica',
        ];
    },

    // 4. Comunicación tutor → profesores de un curso
    'tutor_informe_profesores' => function($alumno, $texto) {
        return [
            'titulo' => "Informe del tutor sobre $alumno",
            'mensaje' => $texto,
            'tipo' => 'informe',
        ];
    },

    // 5. Coordinador → profes de un área
    'coordinador_aviso' => function($area, $texto) {
        return [
            'titulo' => "Aviso del Coordinador de $area",
            'mensaje' => $texto,
            'tipo' => 'administrativa',
        ];
    },

    // 6. Informe importante (ej: situación de un alumno a familias)
    'informe_importante' => function($alumno, $texto) {
        return [
            'titulo' => "Informe importante de $alumno",
            'mensaje' => $texto,
            'tipo' => 'informe',
        ];
    },

];