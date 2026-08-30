package co.edu.icesi.vista360.matricula.service;

import co.edu.icesi.vista360.error.RecursoNoEncontradoException;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse;
import co.edu.icesi.vista360.matricula.model.Matricula;
import co.edu.icesi.vista360.matricula.repository.EstudianteRepository;
import co.edu.icesi.vista360.matricula.repository.InscripcionRepository;
import co.edu.icesi.vista360.matricula.repository.MatriculaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la matricula del periodo vigente a partir de la replica local.
 */
@Service
public class MatriculaService {

    private final MatriculaRepository matriculas;
    private final InscripcionRepository inscripciones;
    private final EstudianteRepository estudiantes;
    private final MatriculaMapper mapper;
    private final Clock reloj;

    MatriculaService(MatriculaRepository matriculas, InscripcionRepository inscripciones,
            EstudianteRepository estudiantes, MatriculaMapper mapper, Clock reloj) {
        this.matriculas = matriculas;
        this.inscripciones = inscripciones;
        this.estudiantes = estudiantes;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public MatriculaActualResponse consultarMatriculaActual(String estudianteId) {
        Matricula matricula = resolverMatricula(estudianteId);
        return mapper.aRespuesta(matricula, inscripciones.buscarPorMatricula(matricula.getId()));
    }

    /**
     * Aplica la definicion de periodo vigente del S-17
     */
    private Matricula resolverMatricula(String estudianteId) {
        List<Matricula> vigentes =
                matriculas.buscarVigenteConProgramas(estudianteId, LocalDate.now(reloj));
        if (!vigentes.isEmpty()) {
            return vigentes.getFirst();
        }

        List<Long> porRecencia = matriculas.idsPorRecencia(estudianteId);
        if (!porRecencia.isEmpty()) {
            return matriculas.cargarConProgramas(porRecencia.getFirst())
                    .orElseThrow(() -> RecursoNoEncontradoException.sinNingunaMatricula(estudianteId));
        }

        // no tiene ninguna, en ningun periodo.
        if (!estudiantes.existsById(estudianteId)) {
            throw RecursoNoEncontradoException.estudianteInexistente(estudianteId);
        }
        throw RecursoNoEncontradoException.sinNingunaMatricula(estudianteId);
    }
}
