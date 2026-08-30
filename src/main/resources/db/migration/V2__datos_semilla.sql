-- Semilla de demostracion

INSERT INTO programa (codigo, nombre, origen, fecha_corte) VALUES
    ('TEL', 'Ingeniería Telemática', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO programa (codigo, nombre, origen, fecha_corte) VALUES
    ('SIS', 'Ingeniería de Sistemas', 'ERP', TIMESTAMP '2026-08-30 04:15:00');

INSERT INTO periodo_academico (codigo, fecha_inicio, fecha_fin, origen, fecha_corte) VALUES
    ('202620', DATE '2026-07-27', DATE '2026-11-28', 'ERP', TIMESTAMP '2026-08-30 04:15:00');

INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('09780', 'Ciberseguridad', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('09794', 'Proyecto integrador II', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('09791', 'Plataformas I', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('09663', 'Proyecto de grado I - TEL', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('00101', 'Programa de desarrollo profesional I', 0, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('09783', 'Sistemas operativos', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('09798', 'Analítica de datos', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('06221', 'Principios de Economía', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('12192', 'Innovación y emprendimiento I', 3, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO asignatura (codigo, nombre, creditos, origen, fecha_corte) VALUES
    ('07313', 'Professional communication for an interconnected world IV', 1, 'ERP', TIMESTAMP '2026-08-30 04:15:00');

-- Grupos del periodo. El NRC los identifica de forma unica dentro de 202620.
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('11008', '202620', '09780', '001', 'Ana María Restrepo', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('11384', '202620', '09794', '001', 'Carlos Andrés Zapata', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('11011', '202620', '09791', '001', 'Diana Lucía Ospina', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('10156', '202620', '09663', '001', 'Jorge Enrique Valencia', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('10387', '202620', '00101', '001', 'Claudia Patricia Nieto', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('10221', '202620', '09783', '001', 'Felipe Andrés Marín', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('11052', '202620', '09798', '003', 'Ana María Restrepo', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('10743', '202620', '06221', '015', 'Mónica Alejandra Gil', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('11311', '202620', '12192', '001', 'Ricardo León Osorio', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO grupo_curso (nrc, periodo_codigo, asignatura_codigo, grupo, docente, origen, fecha_corte) VALUES
    ('10998', '202620', '07313', '013', 'Laura Cristina Bedoya', 'ERP', TIMESTAMP '2026-08-30 04:15:00');


-- ---- A00123456: un solo programa ------------------------------------------

INSERT INTO estudiante (codigo_institucional, nombres, apellidos, correo, origen, fecha_corte) VALUES
    ('A00123456', 'Valentina', 'Ríos Cardona', 'valentina.rios@u.icesi.edu.co', 'ERP', TIMESTAMP '2026-08-30 04:15:00');

INSERT INTO matricula (estudiante_codigo, periodo_codigo, origen, fecha_corte) VALUES
    ('A00123456', '202620', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO matricula_programa (matricula_id, programa_codigo, orden) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00123456' AND periodo_codigo = '202620'), 'TEL', 1);

INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00123456' AND periodo_codigo = '202620'), '09780',
     (SELECT id FROM grupo_curso WHERE nrc = '11008' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'NUMERICA', 'PARCIAL', 4.20, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
-- La señal de alerta temprana: cancelo en la semana cuatro una materia que venia perdiendo.
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00123456' AND periodo_codigo = '202620'), '09794',
     (SELECT id FROM grupo_curso WHERE nrc = '11384' AND periodo_codigo = '202620'), 'TEL',
     'CANCELADA', DATE '2026-08-21', 'NUMERICA', 'PARCIAL', 2.10, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00123456' AND periodo_codigo = '202620'), '09791',
     (SELECT id FROM grupo_curso WHERE nrc = '11011' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'NUMERICA', 'PENDIENTE', NULL, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00123456' AND periodo_codigo = '202620'), '09663',
     (SELECT id FROM grupo_curso WHERE nrc = '10156' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'NUMERICA', 'PARCIAL', 4.50, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
-- Cero creditos y sin nota numerica: el caso que obliga al eje de escala (S-16).
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00123456' AND periodo_codigo = '202620'), '00101',
     (SELECT id FROM grupo_curso WHERE nrc = '10387' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'APROBACION', 'DEFINITIVA', NULL, 'APROBADA', 'ERP', TIMESTAMP '2026-08-30 04:15:00');


-- ---- A00987654: doble titulacion, TEL principal ---------------------------

INSERT INTO estudiante (codigo_institucional, nombres, apellidos, correo, origen, fecha_corte) VALUES
    ('A00987654', 'Santiago', 'Duque Herrera', 'santiago.duque@u.icesi.edu.co', 'ERP', TIMESTAMP '2026-08-30 04:15:00');

INSERT INTO matricula (estudiante_codigo, periodo_codigo, origen, fecha_corte) VALUES
    ('A00987654', '202620', 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO matricula_programa (matricula_id, programa_codigo, orden) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), 'TEL', 1);
INSERT INTO matricula_programa (matricula_id, programa_codigo, orden) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), 'SIS', 2);

INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), '09783',
     (SELECT id FROM grupo_curso WHERE nrc = '10221' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'NUMERICA', 'PARCIAL', 3.10, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), '09798',
     (SELECT id FROM grupo_curso WHERE nrc = '11052' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'NUMERICA', 'DEFINITIVA', 4.00, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), '06221',
     (SELECT id FROM grupo_curso WHERE nrc = '10743' AND periodo_codigo = '202620'), 'SIS',
     'INSCRITA', NULL, 'NUMERICA', 'PARCIAL', 2.80, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
-- Cancelacion temprana, sin calificacion: contrasta con la de A00123456.
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), '12192',
     (SELECT id FROM grupo_curso WHERE nrc = '11311' AND periodo_codigo = '202620'), 'SIS',
     'CANCELADA', DATE '2026-08-07', 'NUMERICA', 'PENDIENTE', NULL, NULL, 'ERP', TIMESTAMP '2026-08-30 04:15:00');
-- El otro valor de la escala de aprobacion, para que se vea que el modelo lo admite.
INSERT INTO inscripcion (matricula_id, asignatura_codigo, grupo_curso_id, programa_codigo,
        estado_inscripcion, fecha_cancelacion, escala_calificacion, estado_calificacion,
        nota, resultado, origen, fecha_corte) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00987654' AND periodo_codigo = '202620'), '07313',
     (SELECT id FROM grupo_curso WHERE nrc = '10998' AND periodo_codigo = '202620'), 'TEL',
     'INSCRITA', NULL, 'APROBACION', 'DEFINITIVA', NULL, 'REPROBADA', 'ERP', TIMESTAMP '2026-08-30 04:15:00');


-- ---- Marca de agua del sondeo (S-02) --------------------------------------

INSERT INTO marca_agua_sondeo (fuente, entidad, ultimo_corte, ultima_ejecucion,
        registros_leidos, estado, ultimo_error) VALUES
    ('ERP', 'matricula', TIMESTAMP '2026-08-30 04:15:00', TIMESTAMP '2026-08-30 04:15:12', 10, 'OK', NULL);
INSERT INTO marca_agua_sondeo (fuente, entidad, ultimo_corte, ultima_ejecucion,
        registros_leidos, estado, ultimo_error) VALUES
    ('ERP', 'condicion_academica', TIMESTAMP '2026-08-30 04:15:00', TIMESTAMP '2026-08-30 04:15:12', 0, 'OK', NULL);
