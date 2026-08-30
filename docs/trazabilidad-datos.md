# Trazabilidad de datos y comunicación entre componentes

Se responde: de dónde sale cada dato que Vista 360° necesita, y cómo se comunican los componentes de la arquitectura entre sí.

## Trazabilidad de datos

| Dato | Origen | Mecanismo | Latencia | ¿Se persiste? | Por qué |
|---|---|---|---|---|---|
| Identidad (perfil del estudiante) | ERP institucional | Petición y respuesta, sondeo incremental programado | Intervalo de sondeo | Sí, copia local | Copia local evita depender del ERP en cada acceso (S-01, S-02) |
| Académico (materias matriculadas y notas) | ERP institucional | Petición y respuesta, sondeo incremental programado | Intervalo de sondeo | Sí, copia local | Tolera ventana de actualización; se consolida por periodo (S-01, S-02, S-09) |
| Condición académica | ERP institucional | Petición y respuesta, mismo sondeo programado | Intervalo de sondeo | Sí, con historial de transiciones | El ERP es dueño de la regla; Vista 360° solo reacciona (S-02, S-08) |
| Estado financiero | ERP institucional (vista/procedimiento publicado) | Petición y respuesta, consulta directa (sin integración) | Tiempo real | No | Un saldo desactualizado afecta trámites; se evita el salto de la integración (S-01, S-03, S-04) |
| Actividad de campus virtual | Plataforma LMS | Petición y respuesta, sondeo programado a la API de resumen | Último valor conocido (según ciclo de sondeo) | Sí, solo el último valor, sin histórico de eventos | El detalle no aporta al acompañamiento; queda en el LMS y en el data warehouse (S-13) |
| Asignación estudiante-acompañante | Vista 360° (dato propio) | Interno, sin integración externa | No aplica, se origina en Vista 360° | Sí, con fecha de inicio y fin | Ningún sistema del ecosistema la describe (S-05) |
| Reportes de acompañamiento | Vista 360° (dato propio) | Interno; se publica como evento hacia el data warehouse | No aplica, se origina en Vista 360° | Sí, hecho estructurado y texto libre por separado | Se separa el hecho del detalle sensible (S-06, S-07, S-12) |
| Alertas | Vista 360° (dato propio) | Interno; se publica como evento hacia el data warehouse | No aplica, se origina en Vista 360° | Sí | Dato nuevo, nace en Vista 360° (S-07, S-12) |
| Solicitudes | Vista 360° (dato propio) | Interno; se publica como evento hacia el data warehouse | No aplica, se origina en Vista 360° | Sí | Nace en Vista 360° (S-07, S-12) |
| Salida hacia el data warehouse | Vista 360° (reportes, alertas y solicitudes) | Mensajería, eventos vía plataforma de integración (outbox) | Según ciclo de publicación de eventos | Sí, en el registro de salida hasta confirmar la publicación | El data warehouse ya recibe ERP y LMS por su cuenta; se entrega solo lo propio (S-07) |

## Contenedores de Vista 360°

El límite del sistema propio contiene cinco piezas desplegables. El criterio para ubicar algo dentro de ese límite es que el equipo lo escriba, lo despliegue y lo opere. 

| Contenedor | Responsabilidad | De dónde sale |
|---|---|---|
| Aplicación web | Interfaz para el estudiante y para el personal de acompañamiento. | Enunciado: ambos perfiles consultan la plataforma. |
| Servicio de API | Expone los datos consolidados, resuelve la autorización y atiende las consultas síncronas. | Enunciado: la Parte 2 pide un servicio propio. |
| Base de datos propia | Guarda la réplica de identidad y de información académica, junto con los datos que nacen en la plataforma. | S-01, S-05, S-06, S-12 |
| Proceso de sincronización | Ejecuta el sondeo incremental contra el ERP y el LMS, actualiza la réplica y lleva la marca de agua. | S-02, S-13 |
| Publicador de eventos | Lee el registro de salida y publica los eventos hacia la plataforma de integración. | S-07 |


## Cómo se comunican los componentes

La distinción se traza por el mecanismo de la llamada, no por si hay un usuario esperando. Una consulta programada sigue siendo petición y respuesta aunque corra de madrugada y sin nadie conectado. Trazarla por el otro eje escondería la decisión del S-03, que es la que separa lo que va directo al origen de lo que pasa por la plataforma de integración.

**Petición y respuesta.** Cubre dos casos que comparten mecanismo y se diferencian en quién espera. El primero es la consulta que el usuario tiene enfrente, el estado financiero, que va directa al contrato publicado del sistema de origen, una vista sobre el ERP, sin pasar por la plataforma de integración (S-03). El segundo es el sondeo incremental del proceso de sincronización contra el ERP y el LMS, que usa ese mismo tipo de llamada aunque corra en su propio calendario (S-02, S-13). En el camino del usuario se propaga su token, de modo que cada componente sabe quién originó la consulta, y el identificador que viaja es el código institucional del estudiante (S-10, S-11). En el sondeo, que corre sin usuario detrás, el proceso se autentica con credencial de servicio (S-10).

**Mensajería.** Los datos que nacen en Vista 360°, reportes, alertas y solicitudes, salen hacia el data warehouse como eventos publicados en la plataforma de integración, con un registro de salida que evita pérdidas si la publicación falla (S-07). Es el único tramo del diseño donde un componente entrega algo y sigue adelante sin esperar respuesta.

**Acceso al ERP cuando no hay API.** La única puerta permitida es una vista o un procedimiento publicado por el equipo dueño del ERP. Está prohibido leer las tablas transaccionales de forma directa (S-04).

**Dirección del flujo.** Vista 360° solo lee del ecosistema. Lo único que escribe es lo que nace en ella: reportes, alertas, solicitudes y la asignación estudiante-acompañante. Nunca escribe de vuelta hacia el ERP ni hacia el LMS (S-12).
