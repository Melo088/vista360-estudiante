package co.edu.icesi.vista360.auditoria;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, Long> {

    /** Historial de accesos a un estudiante, del mas reciente al mas antiguo. */
    List<AuditoriaAcceso> findByEstudianteConsultadoOrderByOcurridoEnDesc(String estudianteCodigo);
}
