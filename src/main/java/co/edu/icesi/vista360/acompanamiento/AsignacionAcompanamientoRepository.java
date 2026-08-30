package co.edu.icesi.vista360.acompanamiento;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsignacionAcompanamientoRepository
        extends JpaRepository<AsignacionAcompanamiento, Long> {

    /**
     * Si el acompanante tenia asignado al estudiante en la fecha dada.
     */
    @Query("""
            SELECT COUNT(a) > 0 FROM AsignacionAcompanamiento a
             WHERE a.acompananteId = :acompananteId
               AND a.estudianteCodigo = :estudianteCodigo
               AND a.vigenteDesde <= :fecha
               AND (a.vigenteHasta IS NULL OR a.vigenteHasta >= :fecha)
            """)
    boolean teniaAsignado(@Param("acompananteId") String acompananteId,
            @Param("estudianteCodigo") String estudianteCodigo, @Param("fecha") LocalDate fecha);
}
