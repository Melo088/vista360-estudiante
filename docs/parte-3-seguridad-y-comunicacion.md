# Parte 3 · Seguridad y comunicación

Cada afirmación implementada cita el archivo y la clase que la sostienen. Lo que todavía no existe queda marcado como tal, con lo que haría falta para construirlo. Los códigos S-NN remiten a `docs supuestos.md`, donde está el razonamiento de cada decisión previa.

---

## 3.1 Autenticación y autorización

El problema se separa en dos mitades que se resuelven en lugares distintos. La autenticación se delega por completo en la plataforma de identidad, que ya existe en el ecosistema, y el servicio se limita a validar lo que ella emite. La autorización se resuelve dentro de Vista 360°, porque depende de un dato que solo ella conoce, que es qué estudiantes tiene asignados cada acompañante. La regla se aplica en un único punto del borde y no dentro de cada operación, para que ningún endpoint que se agregue después quede desprotegido por olvido.

Para llevar eso a código se construyó un servidor de recursos que valida el token de la plataforma de identidad sin emitir credenciales propias, y una sola regla de autorización registrada sobre la ruta que nombra al estudiante, con una rama por rol. Las dos ramas quedaron implementadas y cubiertas por pruebas, incluido el caso del acompañante cuya asignación ya venció.

### Quién dice quién es cada uno

La plataforma de identidad emite el token y Vista 360° lo valida. La plataforma nunca emite credenciales propias, porque mantener un segundo directorio de personas obligaría a decidir cuál de los dos vale cuando difieran.

La validación está implementada como servidor de recursos en `seguridad/SeguridadConfig`, con el decodificador en `seguridad/LlavesJwtConfig`. Esto es una arquitectura sin estado.

Del token se leen dos cosas (S-18). 

- El sujeto, que para un estudiante es su código institucional (S-11) y para el personal es el identificador con el que la plataforma lo nombra, el mismo que guarda `asignacion_acompanamiento`. 

- El rol, que admite dos valores. Un token sin rol, o con un rol que esta plataforma no conoce, no autoriza nada. Negar ante lo desconocido rompe de forma visible el día que la plataforma agregue un rol legítimo, mientras que aceptarlo abriría accesos.

En local no hay plataforma de identidad. `seguridad/LlavesJwtConfig` genera un par de llaves al arrancar y `seguridad/TokenDesarrolloController` emite tokens firmados en `GET /dev/token`. Los dos beans que dan esa capacidad viven bajo el perfil de desarrollo, en `seguridad/EmisorDesarrolloConfig`. Un emisor alcanzable sin autenticar entrega la identidad de cualquiera, por lo que protegerlo con una regla no alcanza. Fuera del perfil no se registra. `seguridad/EmisorDesarrolloTest` lo comprueba verificando que los beans estén ausentes del contexto, que dice más que una ruta respondiendo 401.

### Quién puede ver a quién

La regla vive en `seguridad/AutorizacionEstudiante` y tiene dos ramas.

Un estudiante consulta cuando su sujeto coincide con el código consultado. El personal de acompañamiento consulta cuando existe una asignación vigente sobre ese estudiante, que `acompanamiento/AsignacionAcompanamientoRepository.teniaAsignado` resuelve recibiendo la fecha como parámetro (S-05). Recibir la fecha en vez de tomar la del sistema cuesta lo mismo y habilita el Escenario 4B, donde hay que decir quién tenía acceso en un día pasado.

`seguridad/AutorizacionEstudianteTest` cubre los cinco desenlaces. Estudiante sobre sí mismo, estudiante sobre otro, acompañante con asignación vigente, con asignación vencida y sin asignación.

### Por qué la regla vive en un solo lugar

Se registra sobre el patrón de ruta que lleva el código del estudiante, no sobre cada método.

```java
.requestMatchers("/api/v1/estudiantes/{estudianteId}/**").access(autorizacion)
```

Cualquier endpoint que se agregue bajo esa ruta queda cubierto sin que nadie tenga que acordarse de anotarlo. Una anotación por método deja la protección a merced de la memoria de quien escribe el siguiente endpoint, y el error se ve solo cuando ya está en producción.

Una operación futura bajo esa misma ruta que necesite otra regla tendría que declararse aparte, y la uniformidad se rompería. Se aceptó porque hoy todas las operaciones sobre un estudiante responden a la misma pregunta.

### Cómo se comunican los servicios entre sí

El diagrama tiene dos caminos y cada uno resuelve la identidad distinto (S-10). En el síncrono se propaga el token de quien originó la petición, de modo que la identidad de la persona sobrevive toda la cadena y la auditoría registra a quien preguntó y no al servicio que pasó el mensaje. En el asíncrono no hay usuario detrás, así que cada proceso se autentica con credencial propia.

La cadena de hoy valida token de persona. La tabla `auditoria_acceso` ya distingue `sujeto_tipo` entre persona y servicio, así que el modelo admite el caso y falta el emisor de credenciales de servicio y su consumo.

### Qué se sacrificó

La autorización se resuelve en la cadena de filtros, antes de validar el formato del código y
antes de mirar si el estudiante existe. Un código mal formado sobre el que quien consulta no
tiene permiso responde 403 y no 400. Se eligió no darle retroalimentación de validación a
quien no tiene acceso, y queda declarado en `api/openapi.yaml`.

Un token robado sirve hasta que expire, y se compensa acortando su vigencia. La alternativa es guardar sesión, devuelve el estado que la decisión de arriba eliminó.

### Falta

- Credencial de servicio para el camino asíncrono (S-10).
- Control por campo sobre el detalle sensible de los reportes (S-06). El modelo ya separa
  `reporte_acompanamiento` de `reporte_acompanamiento_detalle`, y falta la regla que lo aplique.
- Límite de tasa. Sin él, un acompañante con muchos estudiantes asignados puede recorrerlos
  más rápido de lo que su trabajo justifica, y eso no se distingue de su uso normal.

---

## 3.2 Escenario A · El estudiante abre Vista 360° y necesita ver su estado financiero

El criterio que ordena este escenario es que el dato se lea de su fuente en el momento en que se pide, porque de él dependen trámites que el estudiante inicia sobre lo que ve en pantalla. De ahí se desprenden tres decisiones. Si se guarda una copia local o no, por dónde viaja la consulta hasta el origen, y qué se muestra cuando el origen no responde. Las tres se resuelven a favor de afirmar solo lo que el sistema sabe en ese instante, aun a costa de perder disponibilidad.

De lo anterior no se construyó nada todavía, porque el alcance de esta entrega fue el servicio de matrícula. Lo que sí quedó fijado es la decisión y su lugar en el diseño, con el estado financiero como el único dato del ecosistema que se lee en vivo, viaja fuera de la plataforma de integración y no se guarda en ninguna tabla.

El estado financiero se consulta en cada petición, directo al contrato publicado del ERP, sin persistirlo y sin pasar por la plataforma de integración.

**No implementado.** No existe cliente ni endpoint financiero. Lo que existe hoy es la
decisión declarada en `docs/trazabilidad-datos.md`, donde el estado financiero es la única fila con latencia en tiempo real y sin persistencia, y el camino dibujado en `docs/img/arquitectura-contenedores.png`.

### Por qué no se replica

Lo académico tolera una ventana de desactualización y por eso vive en réplica local (S-01). El estado financiero no la tolera, y la razón es lo que el estudiante hace con el dato.

Un saldo replicado que quedó viejo se muestra con la misma confianza que uno correcto, y quien lo lee no tiene con qué distinguirlos. El estudiante paga, vuelve a la pantalla, sigue viendo la deuda y concluye que el pago no entró. O al revés, ve deuda cero, se acerca a matricular y descubre en el mostrador que no puede.

Lo que se pierde con esta decisión es la capacidad de responder cuando el ERP está caído.

### Por qué directo y no por la plataforma de integración

El valor de una plataforma de integración es el desacoplamiento del camino asíncrono, donde un evento dispara acciones en varios sistemas que no se conocen entre sí (S-03). En una consulta que un usuario está esperando, ese mismo bus agrega un salto de red, suma su latencia a la del ERP y convierte dos dependencias en tres. La disponibilidad de la pantalla pasa a ser el producto de tres disponibilidades en vez de dos.

La integridad de la consulta directa se garantiza respetando el contrato del sistema destino, que por el S-04 es una vista o un procedimiento publicado por el equipo dueño del ERP y nunca sus tablas. Eso da un contrato de datos explícito en vez de una dependencia al esquema privado de otro sistema.

Si una política institucional exigiera que todo el tráfico pase por el bus, esta decisión cae y hay que introducir una caché de vida corta para compensar el riesgo de disponibilidad, que es el escenario que el S-03 ya anticipa.

### Consultar en cada petición o cachear por segundos

La resolución está en separar los dos objetivos. El objetivo es evitar el trabajo duplicado y proteger al sistema de origen (ERP) de una avalancha de peticiones, pero sin sacrificar la promesa de actualidad del dato.

- **Contra el trabajo duplicado**, memoria del alcance de la petición. Si una misma petición HTTP necesita el saldo dos veces, se resuelve una sola vez. Entre peticiones distintas se vuelve a consultar, así que no hay ventana de desactualización.
- **Contra muchas peticiones**, tiempo de espera corto, tope de llamadas simultáneas y corte de circuito. Cuando el ERP se degrada, el circuito abre y Vista 360° deja de golpearlo en vez de acumular hilos esperando.

Si la medición mostrara que aun así el ERP no soporta el volumen, se admite una caché de vida muy corta con dos condiciones. Que la respuesta declare su antigüedad, como ya hace `actualizadoEn` en el contrato de matrícula, y que la ventana quede declarada como supuesto. 

### Qué se muestra cuando el ERP no responde

La sección financiera se muestra como no disponible, con la hora del último intento, y el resto de la Vista 360° carga normal. La vista es un agregado de varias fuentes y que una no responda no tiene por qué llevarse las otras. El estudiante sigue viendo lo académico y su actividad en el campus virtual.

Que falle la pantalla entera convierte una degradación parcial en una caída total, y castiga al estudiante que entró a ver sus notas. Mostrar un saldo viejo guardado por las dudas es peor.

El sistema dice que no sabe, y el estudiante actúa en consecuencia, reintentando o llamando a la oficina financiera. Un saldo viejo es una afirmación falsa emitida con el mismo aspecto que una verdadera.

### Qué faltaría para implementarlo

Un cliente hacia la vista publicada del ERP con tiempo de espera y corte de circuito, el endpoint que lo expone, la memoria de alcance de petición y la respuesta degradada con su marca de hora.

---

## 3.2 Escenario B · Cambia la condición académica de un estudiante

La solución se ordena en tres tramos. Enterarse del cambio, que depende de lo que el ERP pueda ofrecer. Registrarlo de modo que quede historia y no solo el estado de hoy. Y avisar hacia afuera, tanto al data warehouse como a cualquier proceso que deba reaccionar, sin que Vista 360° necesite conocerlos uno por uno. El punto delicado está en la unión entre registrar y avisar, porque es donde un cambio de condición se puede perder sin que nadie lo note.

De los tres tramos se construyó el modelo que los sostiene y ninguno de los procesos. Las tablas de condición académica, alerta y registro de salida existen con sus restricciones y con la justificación escrita en la propia base, y falta el sondeo que las llene y el publicador que las vacíe.

### Cómo se entera Vista 360°

El ERP declara la condición y Vista 360° no la calcula (S-08). Como el ERP no publica eventos (S-02), el cambio se detecta con sondeo incremental, que pide solo lo modificado desde la última run. `marca_agua_sondeo` guarda hasta qué momento se leyó cada entidad de cada fuente, junto con el estado de la última ejecución y su error.

### Qué ocurre al detectar la transición

1. Una fila nueva en `condicion_academica`, con `condicion_anterior` y `vigente_desde`, y el cierre de la vigencia de la anterior. La tabla es historial y no estado, para poder decir qué condición tenía el estudiante en una fecha pasada.
2. Una `alerta`, cuyo campo `origen_senal` distingue la que reacciona a una declaración del ERP de la que nace de una regla propia de la plataforma.
3. Una fila en `evento_salida`, que es el registro de salida.

### Por qué el evento se escribe dentro de la misma transacción

Si la publicación fuera un llamado directo a la plataforma de integración hecho dentro de la transacción, un fallo del bus haría fallar la transacción entera y el cambio de condición se perdería. El estudiante entraría en prueba académica, el ERP lo sabría, y Vista 360° no, porque el bus estaba caído en ese instante.

Si la publicación se hiciera fuera de la transacción, justo después de confirmarla, el cambio quedaría guardado y un fallo del proceso entre la confirmación y el envío dejaría el evento sin salir nunca. El data warehouse y los demás procesos quedarían desincronizados y nadie se enteraría, porque del lado de Vista 360° todo se ve correcto.

El registro de salida rompe el dilema escribiendo el cambio y el evento juntos, contra la misma base y en la misma transacción, así que ninguno de los dos puede quedar sin el otro. Un publicador aparte lee las filas con `publicado_en` nulo y las entrega a la plataforma de integración. Si el bus está caído, las filas se acumulan y salen cuando vuelve, con los intentos y el último error registrados en la misma fila.

### Qué se publica y qué no

Vista 360° entrega solo lo que nace dentro de ella (S-07). La condición académica ya llega al data warehouse por el canal propio del ERP, así que lo que se publica es la alerta y el seguimiento que la plataforma genera a partir del cambio. Reenviar el hecho del ERP produciría dos versiones del mismo dato con tiempos de corte distintos y sin criterio para decidir cuál vale.

Los demás procesos que deban reaccionar se suscriben al evento en la plataforma de integración, sin que Vista 360° tenga que conocerlos.

### Qué se sacrificó

La latencia es el intervalo de sondeo. Un cambio declarado justo después de ya haber ocurrido el llamado, espera hasta el siguiente. Se acepta porque el acompañamiento actúa en días y no en segundos, y porque la alternativa es pedirle al ERP que publique eventos, que es el cambio de infraestructura que el S-02 declara fuera de alcance.

El registro de salida entrega al menos una vez. Un fallo entre el envío y la marca de publicado reenvía el evento, así que el consumidor tiene que tolerar repetidos identificando el evento por su clave.


### Estado

Las tres tablas existen en `src/main/resources/db/migration/V1__esquema.sql`, cada una con su justificación en el `COMMENT ON`. **No hay proceso de sondeo ni publicador de eventos.** `marca_agua_sondeo` tiene dos filas sembradas en `V2__datos_semilla.sql` para que la forma se vea, y ningún proceso las actualiza.
