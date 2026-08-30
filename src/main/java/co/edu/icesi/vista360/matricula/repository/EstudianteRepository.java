package co.edu.icesi.vista360.matricula.repository;

import co.edu.icesi.vista360.matricula.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Solo se usa para separar los dos 404: el codigo que no corresponde a ningun estudiante y el
 * estudiante que existe pero no tiene ninguna matricula registrada. Alcanza con existsById.
 */
public interface EstudianteRepository extends JpaRepository<Estudiante, String> {
}
