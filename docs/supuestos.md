# Supuestos declarados

Como se menciona, el enunciado dejó intencionalmente varias opciones abiertas. Este documento detalla dichas opciones, indicando dónde una elección específica modifica el diseño de la arquitectura o modelo de datos, junto con la justificación adoptada en cada caso.

Cada supuesto recibe un identificador único que permite referenciarlos desde el resto de los entregables. Las cuatro primeras opciones condicionan a las demás, dado que definen qué información vive dentro de Vista 360°, de qué forma se entera la plataforma de un cambio en el ecosistema, por dónde viaja cada comunicación y qué se hace cuando el ERP no expone una API para los datos necesarios. Las otras trece opciones son independientes entre sí.

---

## S-01 · Qué se replica y qué se consulta en vivo

**Supuesto.** Vista 360° mantiene copia local de los datos de identidad e información académica. La información sobre el estado financiero se recupera del sistema ERP con cada solicitud y no se almacena.

**Fundamento.** Según el gráfico el ERP es la fuente de verdad del ecosistema. Los registros financieros pueden quedar desactualizados y pueden ofrecer a los estudiantes información inexacta o que ya no corresponde, lo que afecta directamente a trámites relacionados con matrícula, grado, etc. Por otro lado, la información académica tolera una ventana de desactualización y mantener estos registros en local permite responder las consultas sin depender de la disponibilidad del ERP en cada acceso, además de conservar el historial necesario para asistir a los estudiantes.

**Si resultara falso.** Si se exige que el estado financiero también resida en Vista 360°, aparece un proceso de sincronización para ese dato, junto con una ventana de actualizaciones declarada y un procedimiento de conciliación.

**Depende de este supuesto.** La estructura completa del modelo de datos y la separación de caminos de lectura en el diagrama.

---

## S-02 · Cómo se entera Vista 360° de un cambio

**Supuesto.** Se asume que el ERP no tiene la capacidad de emitir notificaciones o eventos en tiempo real. Vista 360° detecta los cambios mediante consultas periódicas incrementales, solicitando únicamente los registros que hayan sido modificados desde la última vez que se consultó.

**Fundamento.** El enunciado indica que el ERP es on-premise y expone "algunas APIs", lo que sugiere capacidades de integración limitadas. Modificar algo así para que publique eventos implica esfuerzos relativos a la infraestructura y riesgos que no están en el alcance del nuevo producto. Implementar un proceso programado, como un modelo pull, que traiga los cambios recientes, resultaría para este caso una solución que se puede construir y controlar en Vista 360°. Se asume, por tanto, que los datos tendrán una latencia o desactualización equivalente al tiempo que transcurre entre cada consulta.

**Si resultara falso.** Si el ERP puede publicar eventos a través de la plataforma de integración, la consulta periódica se retira y se reemplaza por una suscripción, un modelo push. La latencia se reduciría y eliminaría la necesidad de llevar un registro de la última fecha y hora consultada.

**Depende de este supuesto.** El Escenario B completo y la tabla de eventos procesados en el modelo de datos.

---

## S-03 · Por dónde viaja cada comunicación

**Supuesto.** Las consultas de datos en tiempo real (síncronas) se realizan directamente contra el contrato publicado del sistema de origen, sea una API o una vista según el S-04. La plataforma de Integración está destinada exclusivamente a la transmisión de mensajes asíncronos (eventos, notificaciones) y procesos que requieran orquestación entre múltiples aplicaciones.

**Fundamento.** El valor real de una plataforma de integración reside en el desacoplamiento de los sistemas: permite que un evento desencadene acciones en múltiples puntos sin que los sistemas necesiten conocer la existencia de los demás. Sin embargo, en el caso de consultas directas del usuario (como leer el estado financiero), usar un bus intermedio introduce un "salto" extra en la red. Esto aumenta la latencia y añade un punto único de fallo innecesario a una solicitud que requiere respuesta inmediata. La integridad de dichas consultas directas se garantiza respetando el contrato del sistema destino.

**Si resultara falso.** Si existiesen políticas que exigen que todo el tráfico debe pasar por la Plataforma de Integración, la consulta del estado financiero pasará a depender de tres cosas (Vista 360°, Plataforma de Integración y ERP, en ese orden). Para mitigar este riesgo de disponibilidad y lentitud, Vista 360° tendría que incorporar un caché de corta duración y manejar una respuesta degradada si el bus central llega a fallar.

**Depende de este supuesto.** Cada flecha del diagrama y el tiempo de respuesta del Escenario A.

---

## S-04 · Acceso al ERP cuando no hay API

**Supuesto.** Cuando el ERP carezca de una API expuesta para un dato necesario, el acceso a su base de datos se realizará exclusivamente a través de Vistas o Procedimientos Almacenados creados específicamente para la integración. Vista 360° tiene prohibido consultar las tablas transaccionales de forma directa.

**Fundamento.** Aunque el ecosistema ofrece acceso a la BD del ERP, consultar tablas internas genera una dependencia crítica al esquema privado de otro sistema. Un cambio en una tabla o columna realizado por el equipo del ERP invalidaría la consulta de Vista 360°. Utilizar una Vista establece un "contrato de datos" oficial y explícito de forma que aísla a Vista 360° de los cambios internos del ERP y permite al equipo dueño de la BD saber qué dependencias externas existen antes de realizar modificaciones.

**Si resultara falso.** Si no es viable pedirle al equipo del ERP que publique y mantenga esa vista, la alternativa es construir una capa propia de solo lectura sobre las tablas y asumir el costo de mantenerla al día ante cualquier cambio de esquema, con pruebas de contrato que detecten la ruptura antes que el usuario.

**Depende de este supuesto.** El Escenario A y la forma concreta de la integración financiera.

---

## S-05 · Propiedad del vínculo entre estudiante y acompañante

**Supuesto.** La asignación entre un estudiante y su acompañante la almacena Vista 360°, con fecha de inicio y fecha de fin.

**Fundamento.** El enunciado no describe esta relación en ningún sistema existente, y es el dato del que depende la autorización del personal de acompañamiento. Guardarla con vigencia permite responder quién era el acompañante de un estudiante en una fecha pasada, lo que hace explicable un reporte antiguo y sostiene la respuesta al reclamo del Escenario 4B.

**Si resultara falso.** Si el ERP ya deriva esta asignación a partir del programa, la facultad o el centro, la tabla propia es innecesaria y la autorización pasa a depender de una consulta externa. Habría que mantenerla en caché para no bloquear cada acceso y definir qué ocurre cuando esa consulta externa falla.

**Depende de este supuesto.** El modelo de datos y la autorización descrita en la Parte 3.1.

---

## S-06 · Sensibilidad de los reportes de acompañamiento

**Supuesto.** Los reportes pueden contener información sensible (por ejemplo, salud o situación socioeconómica). El hecho estructurado (esto es, los metadatos del contacto) y el detalle (texto libre) se almacenan por separado, restringiendo el acceso al texto libre según el rol. El cifrado en reposo se resuelve en la capa de infraestructura.

**Fundamento.** El enunciado menciona "intervención temprana" e "información sensible". En la práctica, esto pueden ser datos sensibles. Entonces, separar el hecho del relato aplica el principio de menor privilegio en tanto permite que otras áreas sepan que hubo una intervención para coordinar esfuerzos, sin exponer los detalles íntimos a quien no está autorizado.

**Si resultara falso.** Si los reportes se limitan a seguimiento estrictamente académico, la separación deja de ser necesaria y el modelo se reduce a una sola tabla de reporte, sin control de acceso diferenciado por campo.

**Depende de este supuesto.** El modelo de acompañamiento, lo que viaja hacia el data warehouse y la respuesta al Escenario 4B.

---

## S-07 · Alimentación del data warehouse

**Supuesto.** El Data Warehouse ya recibe información del ERP y del LMS a través de sus propios canales. Vista 360° le entrega únicamente la información que nace dentro de la nueva plataforma: reportes de acompañamiento, alertas y solicitudes. Esta entrega se hace publicando eventos en la plataforma de integración, con un registro de salida que garantiza que nada se pierda si la publicación falla.

**Fundamento.** El enunciado pide que la información del ecosistema alimente el data warehouse, sin señalar a Vista 360° como canal único. Reenviar un dato que ya llega desde su fuente original produciría dos versiones del mismo hecho, con tiempos de corte distintos y sin criterio para decidir cuál vale.

**Si resultara falso.** Si el data warehouse no recibe directamente la información del ERP ni del LMS, Vista 360° pasa a ser el canal para todo el ecosistema y hereda la responsabilidad de mantener actualizado un dato del que no es dueña, además de convertirse en dependencia de la analítica institucional.

**Depende de este supuesto.** La flecha hacia el data warehouse en el diagrama y el diseño del componente de publicación.

---

## S-08 · Origen de la condición académica

**Supuesto.** La condición académica oficial la calcula y la declara el ERP. Vista 360° solo almacena su historial y reacciona a sus cambios, pero no construye un cálculo propio.

**Fundamento.** Las reglas sobre quién entra en prueba académica le pertenecen al reglamento de la Universidad, cuyo sistema natural es el ERP. Es más seguro consumir el dato oficial ya calculado.

**Si resultara falso.** Si el negocio requiere señales de riesgo anteriores a la declaración oficial del ERP, hay que sumar un motor de reglas propio. Además de separar en el modelo la "alerta predictiva" interna de la "condición oficial" del ERP para no confundir a los usuarios.

**Depende de este supuesto.** El Escenario B y la tabla de condición académica en el modelo de datos.

---

## S-09 · Doble Carrera

**Supuesto.** El modelo de datos conserva el programa bajo el cual se realizó cada inscripción, pero el servicio consolida las materias del estudiante en un único listado por periodo académico, sin exigir el programa como parámetro. La educación continua queda fuera del alcance.

**Fundamento.** En la realidad, un estudiante con doble titulación inscribe y cursa sus materias en un mismo calendario, y para él su carga del semestre es una sola. Por eso el contrato del servicio no debería obligarlo a preguntar dos veces ni a pasar el programa como parámetro. El modelo, en cambio, sí necesita conservar el programa de cada inscripción, porque la condición académica y el avance del plan se evalúan por programa y el ERP los declara por programa (S-08). Sin ese dato no se puede decir en cuál de los dos planes el estudiante va en riesgo, que es justamente lo que el equipo de acompañamiento necesita saber de un estudiante de doble titulación. La consolidación es entonces una decisión del contrato y no una simplificación del modelo. La educación continua se excluye por tener reglas de créditos y calificaciones totalmente distintas.

**Si resultara falso.** Si la universidad manejara los dobles programas completamente aislados, con calendarios o reglas de calificación propias, el servicio requeriría recibir el identificador del programa como parámetro adicional para saber qué lista devolver, y la consolidación por periodo dejaría de tener sentido.

**Depende de este supuesto.** El modelo de matrícula y el contrato del servicio de la Parte 2. El S-15 fija cuántas inscripciones admite una asignatura por periodo y qué declara el programa de cada una.

---

## S-10 · Identidad de los servicios internos

**Supuesto.** La plataforma de identidad emite credenciales tanto para personas como para aplicaciones (servicios). En el flujo sincrónico se propaga el token de quien originó la petición; en el camino asíncrono cada servicio se autentica con su propia credencial.

**Fundamento.** El enunciado describe la plataforma como autenticación con estándares abiertos de identidad, y esos estándares contemplan credenciales de servicio además de las de usuario. Resolver la comunicación interna con un mecanismo distinto agregaría al diseño un componente que el enunciado no menciona. Propagar el token del usuario conserva la identidad de quien preguntó a lo largo de toda la cadena, que es la base de la trazabilidad que exige el Escenario 4B.

**Si resultara falso.** Si la plataforma solo emite credenciales de persona, la comunicación entre servicios requiere otro mecanismo de confianza, y los procesos que corren sin usuario detrás quedan sin identidad propia que registrar en la auditoría.

**Depende de este supuesto.** Las flechas internas del diagrama y la respuesta al Escenario 4B sobre quién accedió a qué.

---

## S-11 · Identificador del estudiante en el servicio

**Supuesto.** El servicio recibe el código institucional del estudiante (formato A00XXXXXX) como identificador. El documento de identidad se usa únicamente como credencial de acceso frente a la plataforma de identidad y no viaja como parámetro en las URLs del servicio.

**Fundamento.** El enunciado pide un servicio que responda "dado el identificador de un estudiante" pero no especifica cuál de ellos. En la Universidad conviven dos: el código institucional, que es la llave con la que el estudiante existe dentro de los sistemas académicos, y el documento de identidad, con el que se registra e inicia sesión en las plataformas. Son cosas distintas, una es una credencial de acceso y la otra una llave de negocio, y conviene que no se mezclen. Poner el documento de identidad en la ruta de una URL lo dejaría registrado en los logs del servidor, en el historial del navegador y en cualquier proxy intermedio, lo que expone un dato personal sin necesidad. Esto implica que el token que entrega la plataforma de identidad debe poder resolverse al código institucional, ya sea porque lo trae como atributo o porque Vista 360° mantiene la correspondencia entre ambos.

**Si resultara falso.** Si el identificador que se debe usar es el documento de identidad, hay que sacarlo de la ruta y moverlo a un mecanismo que no quede registrado, además de tratarlo como dato personal en toda la cadena. Si el token no permite resolver el código institucional, hace falta una tabla de correspondencia entre el sujeto autenticado y el estudiante.

**Depende de este supuesto.** El contrato del servicio de la Parte 2, la autorización de la Parte 3.1 y lo que queda registrado en la auditoría del Escenario 4B.

---

## S-12 · Dirección del flujo hacia los sistemas de origen

**Supuesto.** Vista 360° solo lee de los sistemas del ecosistema. Escribe únicamente los datos que nacen dentro de la plataforma: reportes de acompañamiento, alertas, solicitudes y la asignación descrita en el S-05.

**Fundamento.** El ERP es la fuente de verdad y Vista 360° no reemplaza a ningún sistema existente, así que devolverle información escrita desde afuera abriría la posibilidad de que dos sistemas modifiquen el mismo dato sin un criterio claro de cuál prevalece. Mantener la lectura en una sola dirección deja explícito quién es el dueño de cada dato. Además simplifica la respuesta al Escenario 4B, porque cualquier alteración de información académica o financiera queda por definición fuera de lo que Vista 360° puede hacer.

**Si resultara falso.** Si más adelante se requiere que Vista 360° escriba en el ERP, por ejemplo para registrar el resultado de una solicitud, aparece la necesidad de un mecanismo de escritura controlada con confirmación y compensación ante fallo, y el diagrama gana flechas en la dirección contraria.

**Depende de este supuesto.** La dirección de las flechas en el diagrama y el alcance de la respuesta al Escenario 4B.

---

## S-13 · Tratamiento de la actividad del campus virtual

**Supuesto.** Vista 360° no replica la telemetría del LMS. La actividad del campus virtual se resuelve consumiendo un resumen agregado que el LMS expone por API, y de ese resumen solo se conserva el último valor conocido por estudiante.

**Fundamento.** Un LMS genera un volumen muy alto de eventos de navegación: cada acceso, cada recurso abierto, cada entrega. Replicar ese detalle en la base de datos operativa de Vista 360° haría crecer el almacenamiento sin aportar nada al acompañamiento, que necesita saber si el estudiante está participando y cuándo fue su última conexión, no qué hizo en cada minuto. El detalle fino le pertenece al LMS, que es su dueño, y al Data Warehouse, que es el sistema pensado para analizarlo.

**Si resultara falso.** Si el LMS no expone un resumen agregado y solo permite consultar eventos individuales, Vista 360° tendría que construir la agregación por su cuenta en un proceso programado, con el costo de mantener esa lógica sincronizada con los cambios del LMS.

**Depende de este supuesto.** La tabla de actividad en el modelo de datos y la frescura declarada para ese dato en la tabla de trazabilidad.

---

## S-14 · Alcance de "materias matriculadas" e "inscritas actualmente"

**Supuesto.** Las dos expresiones del enunciado nombran el mismo periodo académico vigente, es decir, "matriculadas" es el conjunto completo del periodo e "inscritas actualmente" el subconjunto que no fue cancelado. El servicio devuelve el conjunto completo con el estado de cada inscripción, así que el subconjunto vigente se obtiene filtrando por INSCRITA. Un estudiante matriculado que todavía no inscribe materias recibe una lista vacía; el código de no encontrado se reserva para el identificador que no corresponde a ningún estudiante.

**Fundamento.** Incluir las materias canceladas aporta buen contexto para el equipo de acompañamiento. Al entregar el conjunto completo con su estado y fecha, se le da al cliente la flexibilidad de filtrar, evitando que el servidor oculte información de riesgo valiosa por omisión.

**Si resultara falso.** Si la universidad exigiera que el servicio retorne únicamente las inscripciones activas por defecto, habría que agregar un parámetro explícito de filtrado. Si "matriculadas" implicara el historial de toda la carrera, se requeriría un endpoint distinto y paginado.

**Depende de este supuesto.** El contrato del servicio de la Parte 2, el modelo de datos que lo soporta y la capacidad de detectar cancelaciones como señal de alerta temprana.

---

## S-15 · Unicidad de la inscripción y qué programa declara

**Supuesto.** Un estudiante inscribe una asignatura una sola vez por periodo, aunque curse dos programas y la asignatura cuente para ambos. La clave natural de la inscripción es (estudiante, periodo, asignatura) y es única. El programa que la inscripción declara es aquel bajo el cual el ERP la registró; si el ERP no registrara ninguno, se asume el programa principal de la matrícula. La homologación entre programas queda fuera de alcance.

**Fundamento.** Cursar dos veces la misma asignatura en un mismo semestre no ocurre en la práctica, así que admitirlo solo abriría la puerta a datos sucios que llegan del ERP sin que nada los detenga. La restricción se declara en el esquema y no en el código, para que valga también cuando el dato entre por el proceso de sincronización. Que una asignatura cuente para los dos planes es una equivalencia entre programas, un cálculo que le pertenece al ERP.

El programa se conserva porque la condición académica y el avance se miden por plan (S-08, S-09), pero pasa a declarar un solo valor. Los programas del estudiante viven en la matrícula, con uno marcado como principal, que es sobre el que el ERP calcula semestre y promedio. Esa separación hace visible una doble titulación aunque uno de los dos programas no tenga inscripciones ese semestre, caso que no se deduce de la lista de materias.

**Si resultara falso.** Si se admitiera inscribir la misma asignatura bajo dos programas en un periodo, la clave pasa a incluir el programa y el consumidor deja de poder indexar la respuesta por código de asignatura sin perder filas. Aflojar una restricción única es una migración barata; apretarla después, con datos cargados, no lo es.

**Depende de este supuesto.** La clave de la tabla de inscripción, el campo de programa del contrato y el listado de programas de la matrícula.

---

## S-16 · Escala de calificación

**Supuesto.** No todas las asignaturas se califican con nota numérica. La escala se registra como un eje propio de la inscripción, con dos valores: numérica, de 0.0 a 5.0, y aprobación, que da un resultado cualitativo y nunca una nota. La escala de aprobación admite calificación pendiente o definitiva, pero no parcial.

**Fundamento.** El balance académico de la Universidad muestra varias asignaturas calificadas como aprobado en vez de un número. Un modelo con una sola columna numérica no puede escribir ese hecho, así que lo perdería o lo falsearía con un valor inventado.

La escala se declara como eje separado y no se deduce de cuál campo viene lleno, porque se conoce al inscribir, antes de que exista calificación alguna. Deducirla dejaría a una materia pendiente sin forma de decir qué tipo de calificación va a recibir. Y tiene una consecuencia concreta para la alerta temprana: una asignatura de escala de aprobación no puede tener nota baja, así que no debe entrar en ningún promedio de riesgo, y el consumidor solo puede saberlo si la escala viene declarada. El valor parcial se excluye de esa escala porque un aprobado a mitad de periodo no significa nada.

**Si resultara falso.** Si existieran más escalas, por ejemplo una de aprobación con distinción o una numérica sobre otro rango, se agregan valores al eje sin cambiar la forma de la respuesta, que es la ventaja de haberlo separado. Si en cambio toda asignatura se calificara con número, el eje sobra y el modelo vuelve a una sola columna de nota.

**Depende de este supuesto.** Los campos de escala y resultado del contrato, y las restricciones de la tabla de inscripción que los relacionan.

---

## S-17 · Qué periodo es el vigente

**Supuesto.** El periodo vigente es aquel cuyo rango de fechas contiene el día de hoy. Si ninguno lo contiene, porque el calendario está entre semestres, el servicio devuelve el último periodo cursado por el estudiante. En consecuencia, la respuesta de no encontrado por falta de matrícula solo se produce cuando el estudiante no tiene ninguna matrícula en ningún periodo.

**Fundamento.** El enunciado pide las materias que el estudiante "tiene inscritas actualmente" y no define qué es actualmente, así que la lectura por fecha es la que corresponde al término. En enero, con el semestre terminado y el siguiente sin empezar, el equipo de acompañamiento sigue necesitando ver el último periodo cursado, porque esa es la información relevante para preparar la intervención del semestre que viene. Un servicio que respondiera vacío durante las semanas de receso dejaría al acompañante sin datos justo cuando tiene tiempo de revisarlos.

El cliente puede distinguir un periodo en curso de uno cerrado comparando las fechas que ya recibe en la respuesta, así que el respaldo no lo obliga a adivinar. La fecha se toma de un reloj inyectado y no de una llamada estática, de modo que el camino de respaldo se puede comprobar sin esperar a que termine el semestre.

**Si resultara falso.** Si la Universidad declarara el periodo vigente de forma explícita, por ejemplo con una marca en el ERP, esa marca reemplaza el cálculo por fechas y el respaldo desaparece, porque siempre habría un periodo declarado. Si en cambio se exigiera que fuera del periodo la respuesta sea vacía, se retira el respaldo y aparece una tercera situación que el contrato tendría que nombrar: el estudiante con matrículas pero ninguna vigente.

**Depende de este supuesto.** La consulta que resuelve la matrícula, el significado del código de no encontrado por falta de matrícula y la descripción de la operación en el contrato.

---

## Cuestiones revisadas y descartadas

En el análisis del caso también se pensó en otras variables pero no se toman como supuestos como tal dado que no influyen en el diagrama ni en la tabla de las bases de datos requeridas. Por ejemplo, el volumen y la concurrencia (lógicamente el diseño de la arquitectura de la app y las relaciones del modelo de bases de datos van a ser las mismas), dónde se desplegará el programa, y la disponibilidad (define más las réplicas o estrategias de recuperación en la infraestructura, no la estructura del software en sí).