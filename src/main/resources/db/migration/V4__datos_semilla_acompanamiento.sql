-- Asignaciones estudiante-acompanante para poder probar la autorizacion del rol (S-05).

-- Vigente y abierta. Ve a A00123456 hoy.
INSERT INTO asignacion_acompanamiento (estudiante_codigo, acompanante_id, vigente_desde, vigente_hasta, creado_en) VALUES
    ('A00123456', 'ana.perez', DATE '2026-07-27', NULL, TIMESTAMP '2026-07-27 08:00:00 +00:00');

-- Vencida. Vio a A00123456 durante el primer semestre y ya no.
INSERT INTO asignacion_acompanamiento (estudiante_codigo, acompanante_id, vigente_desde, vigente_hasta, creado_en) VALUES
    ('A00123456', 'luis.gomez', DATE '2026-01-19', DATE '2026-05-22', TIMESTAMP '2026-01-19 08:00:00 +00:00');

-- Vigente sobre el otro estudiante. Sirve para ver que la regla mira el par y no solo el rol.
INSERT INTO asignacion_acompanamiento (estudiante_codigo, acompanante_id, vigente_desde, vigente_hasta, creado_en) VALUES
    ('A00987654', 'ana.perez', DATE '2026-07-27', NULL, TIMESTAMP '2026-07-27 08:00:00 +00:00');
