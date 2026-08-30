package co.edu.icesi.vista360.auditoria;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/** Baja la salud cuando se pierde una fila de auditoria, sin depender de que alguien lea logs. */
@Component("auditoria")
public class SaludDeAuditoria implements HealthIndicator {

    private final RegistroDeAcceso registro;

    SaludDeAuditoria(RegistroDeAcceso registro) {
        this.registro = registro;
    }

    @Override
    public Health health() {
        long fallos = registro.getFallos();
        if (fallos == 0) {
            return Health.up().withDetail("filasPerdidas", 0).build();
        }
        return Health.down()
                .withDetail("filasPerdidas", fallos)
                .withDetail("dondeBuscarlas", "log de RegistroDeAcceso, nivel ERROR")
                .build();
    }
}
