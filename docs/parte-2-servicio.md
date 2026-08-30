# Parte 2 · Estructura de la base

Este documento responde el segundo entregable de la Parte 2, el diseño de la base que soporta el servicio. El contrato de la operación vive en `api/openapi.yaml`. Los códigos S-NN remiten a `docs/supuestos.md`.

## El esquema modela el alcance de la solución

De las diecisiete tablas de `src/main/resources/db/migration V1__esquema.sql`, ocho sirven a la consulta de matrícula. Las otras nueve sostienen afirmaciones concretas de las Partes 3 y 4, y existen desde el primer día porque un esquema que solo cubre el endpoint obligaría a rediseñarlo cuando aparezca lo demás. Cada tabla lleva en su `COMMENT ON` el supuesto que la justifica, de modo que la razón viaja con la base y no solo con el repositorio.

La frontera que ordena el conjunto es el S-12. Lo que viene del ERP es réplica y lleva `origen` y `fecha_corte`, así se sabe qué dato es propio y cuál es copia. Lo único propio de la plataforma es el acompañamiento, las alertas, las solicitudes y la asignación entre estudiante y acompañante.

## Las ocho que sirven al endpoint

- `programa`, catálogo de programas académicos replicado del ERP (S-01, S-12).
- `periodo_academico`, los periodos; el vigente delimita qué devuelve el servicio (S-14, S-17).
- `estudiante`, réplica de identidad académica con el código institucional como llave, nunca el documento (S-01, S-11).
- `asignatura`, catálogo sin programa a propósito, porque el programa pertenece a la inscripción (S-15).
- `grupo_curso`, el grupo como fila, con su NRC, su docente y su periodo.
- `matricula`, el acto semestral, que distingue al matriculado que no ha inscrito del que no tiene matrícula (S-14, S-17).
- `matricula_programa`, los programas de la matrícula, con el principal en el orden 1 (S-09, S-15).
- `inscripcion`, una inscripción con sus tres ejes de estado independientes (S-14, S-15, S-16).

## Las cinco del dominio propio

- `asignacion_acompanamiento`, el vínculo entre estudiante y acompañante, con vigencia (S-05).
- `reporte_acompanamiento`, el hecho estructurado del contacto, sin texto libre (S-06).
- `reporte_acompanamiento_detalle`, el relato sensible en tabla aparte (S-06).
- `alerta`, dato propio; `origen_senal` separa la que nace de una regla propia de la que reacciona al ERP (S-07, S-08, S-12).
- `solicitud`, dato propio, sin escritura de vuelta hacia el ERP (S-07, S-12).

## Las cuatro de operación e integración

- `condicion_academica`, historial de la condición que declara el ERP, con la anterior de cada transición (S-08).
- `evento_salida`, registro de salida hacia la plataforma de integración (S-07).
- `auditoria_acceso`, quién consultó qué, cuándo, desde dónde y con qué resultado (S-10).
- `marca_agua_sondeo`, hasta qué momento se leyó cada entidad de cada fuente (S-02).

## Restricciones


`uq_inscripcion (matricula_id, asignatura_codigo)` impide que una asignatura se inscriba dos veces en el mismo periodo, aunque cuente para los dos programas de una doble titulación (S-15). Obliga a repetir `asignatura_codigo` en `inscripcion` y en `grupo_curso`, que es el costo de poder declararla.

`uq_matricula_programa_orden (matricula_id, orden)` hace imposible que una matrícula tenga dos programas principales. Un campo booleano habría sido más directo de leer, y ninguna restricción portable habría garantizado que hubiera exactamente uno verdadero.

`uq_grupo_curso_nrc (nrc, periodo_codigo)` convierte en verdad la promesa que el contrato hace sobre el NRC. Sin la tabla de grupo, el NRC sería un atributo repetido en cada inscripción y nada impediría dos filas con el mismo número y distinto docente.

## Las invariantes del contrato viven en el esquema

`inscripcion` lleva cinco `CHECK` que reproducen lo que `api/openapi.yaml` promete. Una inscripción cancelada tiene fecha de cancelación y una vigente no la tiene. El estado de calificación pendiente implica que no hay nota ni resultado. La escala numérica trae nota y nunca resultado, y la escala de aprobación trae resultado, nunca nota, y no admite el estado parcial porque un aprobado a mitad de periodo no significa nada. La nota vive entre 0.0 y 5.0.

Se pusieron en la base y no en el servicio para que valgan también cuando el dato entre por el proceso de sincronización del S-02, que no pasa por el código de la aplicación. Un contrato cuyas invariantes solo se aplican en un camino de entrada es una promesa a medias.

## Puente con las Partes 3 y 4

`asignacion_acompanamiento` es lo que consulta la regla de autorización de la Parte 3.1 para decidir si un acompañante puede ver a un estudiante. `condicion_academica` y `evento_salida` son los dos extremos de la cadena del Escenario 3.2B, donde el cambio de condición y su evento se escriben en la misma transacción. `auditoria_acceso` es con lo que se responde el reclamo del Escenario 4B, y `marca_agua_sondeo` es el primer sospechoso del Escenario 4A. `reporte_acompanamiento_detalle` sostiene la separación del hecho y el relato que el S-06 exige.

## Qué tiene entidad y qué no

Ocho entidades JPA cubren las tablas del endpoint, más `asignacion_acompanamiento` y `auditoria_acceso`, que la autorización y la auditoría sí usan. Las demás existen en la base sin clase que las mapee. `ddl-auto: validate` no exige una entidad por tabla, y crear clases que nada usa sería código muerto que hay que mantener. La razón de cada una vive en su `COMMENT ON`, que es donde la va a buscar quien abra la base.
