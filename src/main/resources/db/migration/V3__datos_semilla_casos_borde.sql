-- Casos que el contrato promete y que hasta ahora nada verificaba.


-- A00555000: matriculado y sin inscribir todavia.
INSERT INTO estudiante (codigo_institucional, nombres, apellidos, correo, origen, fecha_corte) VALUES
    ('A00555000', 'Mariana', 'Ospina Lozano', 'mariana.ospina@u.icesi.edu.co', 'ERP', TIMESTAMP '2026-08-30 04:15:00 +00:00');

INSERT INTO matricula (estudiante_codigo, periodo_codigo, origen, fecha_corte) VALUES
    ('A00555000', '202620', 'ERP', TIMESTAMP '2026-08-30 04:15:00 +00:00');

INSERT INTO matricula_programa (matricula_id, programa_codigo, orden) VALUES
    ((SELECT id FROM matricula WHERE estudiante_codigo = 'A00555000' AND periodo_codigo = '202620'), 'TEL', 1);


-- A00777111: existe en la replica y no tiene ninguna matricula, en ningun periodo.
INSERT INTO estudiante (codigo_institucional, nombres, apellidos, correo, origen, fecha_corte) VALUES
    ('A00777111', 'Tomás', 'Arango Vélez', 'tomas.arango@u.icesi.edu.co', 'ERP', TIMESTAMP '2026-08-30 04:15:00 +00:00');
