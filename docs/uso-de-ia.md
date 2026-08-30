# Declaración de uso de inteligencia artificial

El enunciado permite el uso de herramientas de inteligencia artificial y pide declarar cuál se usó, en qué partes y con qué propósito.

## Herramienta

Claude Code

## Método

El trabajo se organizó como desarrollo dirigido por especificación SDD. El ciclo fue el mismo en cada bloque. Se define el problema y las decisiones ya tomadas. La herramienta devolvía dos o tres formas de resolverlo, cada una con su costo, tareas comprobales y lo que se sacrificaba al elegirla. Se toma una decisión. Y entonces se escribe código.

Ningún bloque de implementación empezó sin una especificación aprobada. 
Si se requería, se re-estructuraban lo planes antes de continuar.


## Reparto del trabajo

**Supuestos y decisiones de arquitectura.** Definidos por mi. La herramienta los cuestionó, buscó contradicciones entre documentos y refinó la redacción. Los dieciocho supuestos de `docs/supuestos.md` responden a mis criterios, y varias de sus reescrituras salieron de objeciones que la herramienta planteó.

**Diagrama y modelo de datos.** El diseño y las decisiones son también mias. La herramienta tradujo a la sintaxis del diagrama y al DDL, y sostuvo la coherencia entre el esquema, el contrato y los supuestos a medida que cada uno cambiaba.

**Implementación y pruebas.** Escritas por la herramienta contra las especificaciones aprobadas. Revisadas por mi y verificadas corriendo la aplicación.

**Redacción de los documentos.** La herramienta redactó sobre esqueletos y argumentos que yo definí, y corregí el resultado.

## No se delegó

La elección entre alternativas. Los supuestos y su justificación. Los trade-offs que cada documento declara. Y la verificación de que la aplicación arranca, responde y hace lo que los documentos afirman.
