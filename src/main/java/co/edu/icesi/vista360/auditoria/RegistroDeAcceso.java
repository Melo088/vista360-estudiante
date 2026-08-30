package co.edu.icesi.vista360.auditoria;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Guarda cada acceso y sobrevive a sus propios fallos (Escenario 4B). */
@Service
public class RegistroDeAcceso {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroDeAcceso.class);

    private final AuditoriaAccesoRepository auditoria;
    private final AtomicLong fallos = new AtomicLong();

    RegistroDeAcceso(AuditoriaAccesoRepository auditoria) {
        this.auditoria = auditoria;
    }

    /** En transaccion propia para que un fallo suyo no arrastre a ninguna otra. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AuditoriaAcceso acceso) {
        try {
            auditoria.save(acceso);
        } catch (RuntimeException fallo) {
            // El log lleva la fila completa para poder reponerla, y el contador alimenta el
            // indicador de salud.
            fallos.incrementAndGet();
            LOG.error("No se pudo registrar el acceso. Fila perdida: {}", acceso, fallo);
        }
    }

    /** Cuantas filas de auditoria se perdieron desde que arranco el proceso. */
    public long getFallos() {
        return fallos.get();
    }
}
