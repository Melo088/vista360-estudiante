package co.edu.icesi.vista360.matricula.repository;

import co.edu.icesi.vista360.matricula.model.Inscripcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /**
     * Inscripciones de una matricula, con todo lo que el DTO lee de cada una.
     */
    @Query("""
            SELECT i FROM Inscripcion i
              JOIN FETCH i.asignatura a
              JOIN FETCH i.grupoCurso
              JOIN FETCH i.programa
             WHERE i.matricula.id = :matriculaId
             ORDER BY a.codigo ASC
            """)
    List<Inscripcion> buscarPorMatricula(@Param("matriculaId") Long matriculaId);
}
