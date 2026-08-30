# Vista 360° del Estudiante

Servicio de consulta de matrícula construido como respuesta a la prueba técnica del Semillero de Arquitectura e Innovación TI de la Universidad Icesi. Dado el código institucional de un estudiante, devuelve sus materias del periodo académico vigente con el estado y la nota de cada una.

## Cómo correrlo

Requiere JDK 21 o superior. La primera run necesita conexión, porque el wrapper descarga Maven y las dependencias.

```bash
git clone https://github.com/Melo088/vista360-estudiante
cd vista360-estudiante
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080` con una base H2 en memoria que Flyway crea y siembra en cada arranque. No hace falta instalar ni configurar nada más.

- Documentación interactiva: `http://localhost:8080/swagger-ui.html`
- Salud del servicio: `http://localhost:8080/actuator/health`

## Cómo probar la autorización

Todas las consultas exigen un token. En producción lo emite la plataforma de identidad; en local lo emite la propia aplicación, y solo bajo el perfil de desarrollo.

**1.** Pedir un token para el estudiante `A00123456`.

```
GET http://localhost:8080/dev/token?sujeto=A00123456&rol=ESTUDIANTE
```

**2.** Copiar el valor de `token` y pegarlo en el botón **Authorize** de Swagger.

**3.** Consultar el propio código y después uno ajeno.

| Consulta | Respuesta |
|---|---|
| `GET /api/v1/estudiantes/A00123456/matricula-actual` | 200 con las cinco materias |
| `GET /api/v1/estudiantes/A00987654/matricula-actual` | 403 en `application/problem+json` |

**4.** El rol de acompañamiento es el caso que muestra el modelo de datos trabajando. La regla no mira solo el rol, mira el par de acompañante y estudiante, y mira la vigencia de la asignación.

| Token | Sobre | Respuesta | Por qué |
|---|---|---|---|
| `sujeto=ana.perez&rol=ACOMPANAMIENTO` | `A00123456` | 200 | Tiene la asignación abierta |
| `sujeto=luis.gomez&rol=ACOMPANAMIENTO` | `A00123456` | 403 | Su asignación cerró en mayo |
| `sujeto=ana.perez&rol=ACOMPANAMIENTO` | `A00555000` | 403 | No lo tiene asignado |

Sin token la respuesta es 401. Con un token de firma alterada, también.

## Cómo ver la base

La consola de H2 queda disponible bajo el perfil de desarrollo, en `http://localhost:8080/h2-console`.

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:vista360` |
| Usuario | `sa` |
| Contraseña | vacía |

Cada consulta al servicio deja una fila en `auditoria_acceso`, incluidas las rechazadas. Es la tabla con la que se responde el Escenario 4B.

## Datos de la semilla

Los códigos de asignatura, los NRC, los grupos y el periodo `202620` salen del catálogo real de la Universidad. Los estudiantes, sus matrículas y sus calificaciones son ficticios y no reproducen ningún balance académico real.

| Código | Caso que ilustra |
|---|---|
| `A00123456` | Un programa, con una materia cancelada que venía perdiendo y otra de escala de aprobación |
| `A00987654` | Doble titulación, con el programa principal marcado y una materia reprobada sin nota numérica |
| `A00555000` | Matriculado que todavía no inscribe materias, responde 200 con la lista vacía |
| `A00777111` | Existe y no tiene ninguna matrícula, responde 404 con detalle propio |

## Entregables

| Parte del enunciado | Dónde |
|---|---|
| Parte 1 · Diseño de la solución | `docs/img/arquitectura-contenedores.png`, `docs/supuestos.md`, `docs/trazabilidad-datos.md` |
| Parte 2 · Especificación del servicio | `api/openapi.yaml` |
| Parte 2 · Estructura de la base | `docs/parte-2-servicio.md`, `src/main/resources/db/migration/` |
| Parte 2 · Implementación | `src/main/java/`, 38 pruebas en `src/test/java/` |
| Parte 3 · Seguridad y comunicación | `docs/parte-3-seguridad-y-comunicacion.md` |
| Parte 4 · Operación y calidad | `docs/parte-4-operacion-y-calidad.md` |
| Declaración de uso de IA | `docs/uso-de-ia.md` |

Los dieciocho supuestos declarados en `docs/supuestos.md` son el vocabulario del resto. Los demás documentos los citan por código en vez de repetir el argumento.

## Por qué un servicio sencillo tiene autorización y auditoría

El servicio sigue teniendo un solo endpoint de negocio. Lo que se agregó alrededor es la evidencia de las otras dos partes. La autorización por asignación con vigencia es la respuesta ejecutable de la Parte 3.1, y el registro de accesos con sus intentos rechazados es la de la Parte 4B.

## Alcance implementado

- Contrato completo del servicio, con sus invariantes declaradas y sostenidas por restricciones en la base.
- Esquema de diecisiete tablas que cubre el alcance de la Parte 1, con semilla.
- Lectura desde la base con consultas de número fijo, verificado por prueba.
- Autenticación por token y autorización por sujeto y por asignación con vigencia.
- Emisor de tokens de desarrollo, con prueba de que no existe fuera de su perfil.
- Auditoría de acceso con sus cuatro desenlaces, que no tumba la respuesta si falla y que baja la salud del servicio cuando pierde una fila.

## Fuera de alcance

Lo que sigue está diseñado y declarado, y no construido. Cada punto dice por qué.

**Logs estructurados con correlación.** El identificador se genera por petición y viaja hasta la fila de auditoría, y todavía no aparece en cada línea de log. Falta el contexto de diagnóstico y el formato JSON. Sin eso, un incidente intermitente obliga a cruzar por hora.

**El campo `resultado` de la auditoría registra la decisión de acceso y no el desenlace.** Un acceso autorizado a un estudiante inexistente queda como permitido. Distinguir quién vio información de quién tenía permiso y no encontró nada pide una columna con el estado de la respuesta.

**El docente es texto y no una persona del ERP.** El enunciado pide materias y notas, y modelar al docente como entidad replicada agregaría una integración que ninguna respuesta necesita.

**La homologación entre programas.** Una asignatura que cuenta para las dos titulaciones se inscribe una sola vez y declara un solo programa (S-15). Reproducir el cálculo de equivalencias sería duplicar una regla que le pertenece al ERP.

**El cliente del ERP para el estado financiero.** La decisión está tomada y argumentada en la Parte 3, y no hay código. El alcance de la implementación fue el servicio de matrícula.

**El proceso de sondeo y el publicador de eventos.** `marca_agua_sondeo`, `condicion_academica` y `evento_salida` existen con sus restricciones, y ningún proceso las llena ni las vacía. Son la mitad de infraestructura del Escenario 3.2B.

## Notas de construcción

El esquema está escrito en dialecto Oracle y corre sobre H2 en modo de compatibilidad, para que la entrega se pueda ejecutar sin instalar una base. Esa decisión tiene costos visibles en el código, como las anotaciones de tipo JDBC en las entidades, y están comentados donde aparecen.
