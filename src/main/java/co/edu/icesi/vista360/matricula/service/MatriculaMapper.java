package co.edu.icesi.vista360.matricula.service;

import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Materia;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Periodo;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Programa;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.ProgramaMatriculado;
import co.edu.icesi.vista360.matricula.model.Inscripcion;
import co.edu.icesi.vista360.matricula.model.Matricula;
import co.edu.icesi.vista360.matricula.model.MatriculaPrograma;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Traduce las entidades al contrato publicado.
 */
@Component
public class MatriculaMapper {

    public MatriculaActualResponse aRespuesta(Matricula matricula, List<Inscripcion> inscripciones) {
        return new MatriculaActualResponse(
                matricula.getEstudiante().getCodigoInstitucional(),
                aPeriodo(matricula),
                aProgramas(matricula),
                calcularActualizadoEn(matricula, inscripciones),
                inscripciones.stream().map(MatriculaMapper::aMateria).toList());
    }

    /**
     * La fecha de corte mas vieja entre la matricula y sus inscripciones.
     */
    private static Instant calcularActualizadoEn(Matricula matricula, List<Inscripcion> inscripciones) {
        return inscripciones.stream()
                .map(Inscripcion::getFechaCorte)
                .reduce(matricula.getFechaCorte(), (masVieja, otra) -> otra.isBefore(masVieja) ? otra : masVieja);
    }

    private static Periodo aPeriodo(Matricula matricula) {
        var periodo = matricula.getPeriodo();
        return new Periodo(periodo.getCodigo(), periodo.getFechaInicio(), periodo.getFechaFin());
    }

    private static List<ProgramaMatriculado> aProgramas(Matricula matricula) {
        return matricula.getProgramas().stream()
                .sorted(Comparator.comparingInt(MatriculaPrograma::getOrden))
                .map(inscrito -> new ProgramaMatriculado(
                        inscrito.getPrograma().getCodigo(),
                        inscrito.getPrograma().getNombre(),
                        inscrito.esPrincipal()))
                .toList();
    }

    private static Materia aMateria(Inscripcion inscripcion) {
        var asignatura = inscripcion.getAsignatura();
        var grupo = inscripcion.getGrupoCurso();
        return new Materia(
                new MatriculaActualResponse.Asignatura(
                        asignatura.getCodigo(), asignatura.getNombre(), asignatura.getCreditos()),
                grupo.getNrc(),
                grupo.getGrupo(),
                new Programa(inscripcion.getPrograma().getCodigo(), inscripcion.getPrograma().getNombre()),
                grupo.getDocente(),
                inscripcion.getEstadoInscripcion(),
                inscripcion.getEscalaCalificacion(),
                inscripcion.getEstadoCalificacion(),
                inscripcion.getNota(),
                inscripcion.getResultado(),
                inscripcion.getFechaCancelacion());
    }
}
