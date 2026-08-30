# Parte 4 · Operación y calidad

Cada afirmación implementada cita el archivo y la clase que la sostienen. Lo que todavía no existe queda marcado como tal. Los códigos S-NN remiten a `docs/supuestos.md`.

---

## Escenario A · La información académica no carga de forma intermitente

Ante un incidente que no se reproduce, lo primero es dejar de intentar reproducirlo y cambiar de método. En lugar de buscar el caso que falla, se acota el espacio de causas con preguntas que se puedan responder con datos ya guardados, de manera que cada respuesta descarte una familia entera de hipótesis. Eso obliga a haber decidido de antemano qué se registra de cada petición, porque cuando el incidente aparece ya es tarde para agregarlo. Por eso la respuesta tiene dos partes, cómo se investiga y qué había que dejar previsto para poder investigarlo.

Dado lo anterior, se dejaron puestas cinco piezas antes de que haga falta usarlas. Identificador de correlación por petición, desenlace de cada acceso, frescura del dato visible en la respuesta, salud por componente, y un número de consultas a la base que no crece con la cantidad de materias. La marca de agua del sondeo está modelada y espera al proceso que la escriba.

### Cómo se afronta

**¿Le pasa a cualquier estudiante o siempre a los mismos?** Se responde consultando `auditoria_acceso` por `estudiante_consultado`. Si son siempre los mismos, el problema está en sus datos y no en la plataforma. Si es cualquiera y aparece por rachas, apunta a infraestructura o a carga.

**¿Es un fallo de lectura o de permiso?** La columna `resultado` lo dice. Un `DENEGADO` intermitente sobre el mismo par de acompañante y estudiante señala una asignación que venció o un token emitido con otro rol, y no una caída. Es la confusión más frecuente en este tipo de reporte, porque desde el escritorio del director un 403 y un error de servidor se ven igual. Sin el desenlace registrado, esa distinción cuesta horas de logs.

**¿Empeora con la carga?** El número de consultas a la base por petición es fijo y no crece con la cantidad de materias del estudiante. `matricula/MatriculaConsultasTest` lo verifica comparando una matrícula de cinco inscripciones contra una de cero y exigiendo el mismo número de sentencias. Un N+1 introducido más adelante agotaría el pool bajo carga y daría exactamente este síntoma, intermitente y difícil de reproducir en un ambiente vacío. La prueba está justamente para que esa hipótesis se pueda descartar sin investigarla.

**¿La réplica está al día para ese estudiante?** El campo `actualizadoEn` de la respuesta trae la fecha de corte más vieja de las filas que la componen, calculada en `matricula/service/MatriculaMapper`. Si viene atrasado, el problema está en la ingesta y no en la lectura, y se ve sin entrar a la base.

**¿El sondeo corre?** `marca_agua_sondeo` guarda `ultima_ejecucion`, `estado` y `ultimo_error` por fuente y entidad. Una marca estancada o un estado degradado explican por qué unos estudiantes cargan y otros no, que es el síntoma exacto del reporte.

### Qué había que tener previsto desde el diseño

Lo que hace investigable a un incidente intermitente se decide antes de que ocurra.

**Implementado.**

- Identificador de correlación por petición, generado en `auditoria/RegistroDeAccesoFilter` y guardado en cada fila de `auditoria_acceso`.
- Desenlace de cada acceso, con recurso, dirección de origen y agente, en `auditoria_acceso`. `auditoria/AuditoriaAccesoTest` cubre los cuatro desenlaces posibles.
- Frescura visible en la respuesta, en `matricula/service/MatriculaMapper`. Se toma la fecha de corte más vieja porque un conjunto vale lo que su parte más antigua, así que una réplica parcial se delata sola.
- Salud por componente, en `auditoria/SaludDeAuditoria`, publicada en `/actuator/health`.
- Consultas de número fijo, en `matricula/repository/MatriculaRepository` y `InscripcionRepository`, con la prueba que lo sostiene.

**Creado y todavía sin uso.** `marca_agua_sondeo`, con `estado` y `ultimo_error`, esperando al proceso de sincronización que el S-02 describe y que no existe.

**Pendiente.**

- Logs estructurados con la correlación en cada línea. Hoy el identificador se genera y se guarda en la fila de auditoría, y no aparece en los logs. Sin eso hay que cruzar por hora, que es donde se pierde el tiempo en un problema intermitente.
- Salud del proceso de sondeo y alerta cuando la marca de agua se estanca. Hoy hay que mirarla a mano.
- Métricas de latencia por dependencia.

### Qué se sacrificó

Sin métricas por dependencia, una degradación de red contra el ERP se manifiesta como lentitud general y hay que descartarla a mano. Se aceptó porque el alcance de esta entrega es el servicio y no la plataforma de observabilidad, y porque las preguntas de arriba acotan el problema sin ellas. Es una decisión de alcance y no una omisión.

---

## Escenario B · Un estudiante sospecha que su información fue consultada o alterada

Responder con certeza exige haber guardado antes aquello que se va a afirmar después, porque una traza que se empieza a llevar el día del reclamo no dice nada sobre el día anterior. El estudiante hace dos preguntas distintas, si alguien vio su información y si alguien la modificó, y cada una se contesta con un mecanismo propio. La primera con un registro de accesos que incluya también los intentos rechazados, porque un intento denegado es parte de la respuesta. La segunda con la dirección del flujo de datos, que define qué pudo alterarse desde esta plataforma y qué quedó fuera de su alcance por diseño.

Para sostener la primera pregunta se construyó el registro de accesos, ubicado en la cadena de modo que vea también lo que se rechaza, y la consulta de asignaciones que recibe la fecha como parámetro para poder mirar hacia atrás. La segunda pregunta queda respondida por diseño para lo que viene del ERP, y sin resolver para los datos que nacen en la plataforma.

### Quién vio

`auditoria_acceso` responde la primera. La escribe `auditoria/RegistroDeAccesoFilter` y cada fila dice quién consultó (`sujeto_id`, `rol`), a quién (`estudiante_consultado`), cuándo (`ocurrido_en`), desde dónde (`direccion_ip`, `agente_usuario`), con qué resultado (`resultado`) y bajo qué correlación. `AuditoriaAccesoRepository` expone el historial ordenado por estudiante.

### Intentos rechazados

El filtro se registra dentro de la cadena de seguridad y antes del filtro que autentica, que es la única posición desde la que se ven los cuatro desenlaces. Más afuera la identidad ya fue limpiada al volver y no habría a quién atribuir el acceso. Más adentro, un token inválido corta la cadena antes de llegar.

Registrando solo lo permitido, la Universidad puede decir que nadie vio la información y no puede decir si alguien lo intentó, que suele ser lo que el estudiante está preguntando en realidad.

### Quién tenía acceso legítimo en esa fecha

`asignacion_acompanamiento` guarda `vigente_desde` y `vigente_hasta` (S-05), y `acompanamiento/AsignacionAcompanamientoRepository.teniaAsignado` recibe la fecha como parámetro. Con eso se puede afirmar que en marzo el acompañante sí tenía asignado a ese estudiante, aunque hoy no lo tenga. 

### Si alguien alteró

Vista 360° solo lee del ecosistema (S-12). La información académica y financiera no se pudo alterar desde esta plataforma, y esa afirmación se sostiene en el diseño y no en un registro,
porque no existe ningún camino de escritura hacia el ERP.

### Auditoría

Un fallo al registrar no puede alterar lo que el usuario ya recibió, y perder la consulta de un estudiante por un problema de registro sería malo.

La fila perdida se vuelca completa al log en nivel de error, con todos sus campos, de modo que se pueda reponer a mano. Y un contador alimenta `auditoria/SaludDeAuditoria`, que baja la salud del servicio con el detalle de cuántas filas se perdieron. `auditoria/FalloDeAuditoriaTest` verifica las dos mitades en el mismo caso, con la respuesta llegando completa y la salud caída.

Un servicio que responde bien y dejó de registrar quién consultó qué está roto para este escenario aunque ningún usuario lo note.

### Qué falta

- `resultado` registra la decisión de acceso y no si se entregó información. Un acceso autorizado a un estudiante que no existe queda como permitido. Distinguir quién vio datos de quién tenía permiso y no encontró nada pide una columna con el estado de la respuesta.
- Los datos propios no llevan historial de cambios. Un reporte de acompañamiento editado no deja rastro de quién lo editó ni de qué decía antes, así que la mitad de la pregunta sobre alteración solo se responde para lo que viene del ERP.
- No hay endpoint para que el estudiante consulte su propio historial de accesos. El repositorio existe y la consulta está escrita.
- No hay política de retención declarada. Una tabla de auditoría que crece sin criterio termina purgada por quien necesite espacio, y con ella la capacidad de responder.
- El registro no está protegido contra manipulación. Quien tenga acceso de escritura a la base puede borrar filas, así que la certeza que este escenario pide depende hoy de los controles de la base y no de la plataforma.
