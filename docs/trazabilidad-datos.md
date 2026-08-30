# Trazabilidad de datos y comunicación entre componentes

Se responde: de dónde sale cada dato que Vista 360° necesita, y cómo se comunican los componentes de la arquitectura entre sí.

## Trazabilidad de datos

| Dato | Origen | Mecanismo | Latencia | ¿Se persiste? | Por qué |
|---|---|---|---|---|---|
| Identidad (perfil del estudiante) | ERP institucional | Asíncrono, sondeo incremental |  Intervalo de sondeo | Sí, copia local | Copia local evita depender del ERP en cada acceso (S-01, S-02) |
| Académico (materias matriculadas y notas) | ERP institucional | Asíncrono, sondeo incremental |  Intervalo de sondeo | Sí, copia local | Tolera ventana de actualización; se consolida por periodo (S-01, S-02, S-09) |
| Condición académica | ERP institucional | Asíncrono, mismo sondeo incremental |  Intervalo de sondeo | Sí, con historial de transiciones | El ERP es dueño de la regla; Vista 360° solo reacciona (S-02, S-08) |
| Estado financiero | ERP institucional (vista/procedimiento publicado) | Síncrono, consulta directa (sin integración) | Tiempo real | No | Un saldo desactualizado afecta trámites; se evita el salto de la integración (S-01, S-03, S-04) |
| Actividad de campus virtual | Plataforma LMS | Asíncrono, sondeo periódico a la API de resumen | Último valor conocido (según ciclo de sondeo) | Sí, solo el último valor, sin histórico de eventos | El detalle no aporta al acompañamiento; queda en el LMS y en el data warehouse (S-13) |
| Asignación estudiante-acompañante | Vista 360° (dato propio) | Interno, sin integración externa | No aplica, se origina en Vista 360° | Sí, con fecha de inicio y fin | Ningún sistema del ecosistema la describe (S-05) |
| Reportes de acompañamiento | Vista 360° (dato propio) | Interno; se publica como evento hacia el data warehouse | No aplica, se origina en Vista 360° | Sí, hecho estructurado y texto libre por separado | Se separa el hecho del detalle sensible (S-06, S-07, S-12) |
| Alertas | Vista 360° (dato propio) | Interno; se publica como evento hacia el data warehouse | No aplica, se origina en Vista 360° | Sí | Dato nuevo, nace en Vista 360° (S-07, S-12) |
| Solicitudes | Vista 360° (dato propio) | Interno; se publica como evento hacia el data warehouse | No aplica, se origina en Vista 360° | Sí | Nace en Vista 360° (S-07, S-12) |
| Salida hacia el data warehouse | Vista 360° (reportes, alertas y solicitudes) | Asíncrono, eventos vía plataforma de integración (outbox) | Según ciclo de publicación de eventos | Sí, en el registro de salida hasta confirmar la publicación | El data warehouse ya recibe ERP y LMS por su cuenta; se entrega solo lo propio (S-07) |

## Cómo se comunican los componentes

**Camino síncrono.** La consulta que el usuario espera ver de inmediato, el estado financiero, va directa al contrato publicado del sistema de origen, una vista sobre el ERP, sin pasar por la plataforma de integración (S-03). El token de quien hizo la petición se propaga a lo largo de esa cadena, de modo que cada componente sabe quién originó la consulta (S-10). El identificador que viaja en esas llamadas es el código institucional del estudiante. (S-11).

**Camino asíncrono.** Los cambios que ocurren en el ERP, identidad, académico y condición académica, se detectan por sondeo periódico incremental, porque el ERP no publica eventos hoy (S-02). Los datos que nacen en Vista 360°, reportes, alertas y solicitudes, salen hacia el data warehouse como eventos publicados en la plataforma de integración, con un registro de salida que evita pérdidas si la publicación falla (S-07). En este camino cada servicio usa su propia credencial, sin depender de que haya un usuario detrás (S-10).

**Acceso al ERP cuando no hay API.** La única puerta permitida es una vista o un procedimiento publicado por el equipo dueño del ERP. Está prohibido leer las tablas transaccionales de forma directa (S-04).

**Dirección del flujo.** Vista 360° solo lee del ecosistema. Lo único que escribe es lo que nace en ella: reportes, alertas, solicitudes y la asignación estudiante-acompañante. Nunca escribe de vuelta hacia el ERP ni hacia el LMS (S-12).
