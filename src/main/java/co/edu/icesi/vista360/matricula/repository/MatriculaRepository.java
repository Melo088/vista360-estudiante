package co.edu.icesi.vista360.matricula.repository;

import co.edu.icesi.vista360.matricula.model.Matricula;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /**
     * Matricula del periodo que contiene la fecha dada (S-17).
     */
    @Query("""
            SELECT m FROM Matricula m
              JOIN FETCH m.estudiante
              JOIN FETCH m.periodo p
              LEFT JOIN FETCH m.programas mp
              LEFT JOIN FETCH mp.programa
             WHERE m.estudiante.codigoInstitucional = :codigo
               AND :fecha BETWEEN p.fechaInicio AND p.fechaFin
             ORDER BY p.fechaInicio DESC
            """)
    List<Matricula> buscarVigenteConProgramas(@Param("codigo") String codigo,
            @Param("fecha") LocalDate fecha);

    /**
     * Identificadores de las matriculas del estudiante, de la mas reciente a la mas antigua.
     */
    @Query("""
            SELECT m.id FROM Matricula m
              JOIN m.periodo p
             WHERE m.estudiante.codigoInstitucional = :codigo
             ORDER BY p.fechaInicio DESC
            """)
    List<Long> idsPorRecencia(@Param("codigo") String codigo);

    @Query("""
            SELECT m FROM Matricula m
              JOIN FETCH m.estudiante
              JOIN FETCH m.periodo
              LEFT JOIN FETCH m.programas mp
              LEFT JOIN FETCH mp.programa
             WHERE m.id = :id
            """)
    Optional<Matricula> cargarConProgramas(@Param("id") Long id);
}
