package co.edu.icesi.vista360.matricula;

import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Asignatura;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.EstadoCalificacion;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.EstadoInscripcion;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Materia;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Periodo;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Programa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implementa el contrato de api/openapi.yaml.
 * Los datos estan quemados: falta el modelo de datos, el repositorio y la capa de servicio.
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Matricula", description = "Consulta de la matricula del periodo vigente")
public class MatriculaController {

    /** Formato del codigo institucional, S-11. */
    private static final String PATRON_CODIGO = "^A00\\d{6}$";

    private static final Periodo PERIODO_VIGENTE =
            new Periodo("202620", LocalDate.of(2026, 7, 27), LocalDate.of(2026, 11, 28));

    /** Instante de la ultima sincronizacion. Con la replica real sale de la marca de agua. */
    private static final Instant ULTIMA_SINCRONIZACION = Instant.parse("2026-08-30T04:15:00Z");

    private static final Programa TELEMATICA = new Programa("TEL", "Ingeniería Telemática");
    private static final Programa SISTEMAS = new Programa("SIS", "Ingeniería de Sistemas");

    private static final Map<String, List<Materia>> MATERIAS_POR_ESTUDIANTE = Map.of(
            // Un solo programa. Muestra la combinacion CANCELADA + PARCIAL con nota baja.
            "A00123456", List.of(
                    new Materia(
                            new Asignatura("09780", "Ciberseguridad", 3),
                            "11008", "001", TELEMATICA, "Ana María Restrepo",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.PARCIAL,
                            new BigDecimal("4.2"), null),
                    // Señal de alerta temprana y solo se puede escribir con los dos ejes separados.
                    new Materia(
                            new Asignatura("09794", "Proyecto integrador II", 3),
                            "11384", "001", TELEMATICA, "Carlos Andrés Zapata",
                            EstadoInscripcion.CANCELADA, EstadoCalificacion.PARCIAL,
                            new BigDecimal("2.1"), LocalDate.of(2026, 8, 21)),
                    new Materia(
                            new Asignatura("09791", "Plataformas I", 3),
                            "11011", "001", TELEMATICA, "Diana Lucía Ospina",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.PENDIENTE, null, null),
                    new Materia(
                            new Asignatura("09663", "Proyecto de grado I - TEL", 3),
                            "10156", "001", TELEMATICA, "Jorge Enrique Valencia",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.PARCIAL,
                            new BigDecimal("4.5"), null),
                    new Materia(
                            new Asignatura("00101", "Programa de desarrollo profesional I", 0),
                            "10387", "001", TELEMATICA, "Claudia Patricia Nieto",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.PENDIENTE, null, null)),

            // Doble programa, las dos titulaciones conviven en un mismo listado por periodo (S-09).
            "A00987654", List.of(
                    new Materia(
                            new Asignatura("09783", "Sistemas operativos", 3),
                            "10221", "001", TELEMATICA, "Felipe Andrés Marín",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.PARCIAL,
                            new BigDecimal("3.1"), null),
                    new Materia(
                            new Asignatura("09798", "Analítica de datos", 3),
                            "11052", "003", TELEMATICA, "Ana María Restrepo",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.DEFINITIVA,
                            new BigDecimal("4.0"), null),
                    new Materia(
                            new Asignatura("06221", "Principios de Economía", 3),
                            "10743", "015", SISTEMAS, "Mónica Alejandra Gil",
                            EstadoInscripcion.INSCRITA, EstadoCalificacion.PARCIAL,
                            new BigDecimal("2.8"), null),
                    // Cancelacion temprana, sin notas cargadas todavia. Contrasta con la de
                    // A00123456 y muestra que la fecha es la que separa las dos historias.
                    new Materia(
                            new Asignatura("12192", "Innovación y emprendimiento I", 3),
                            "11311", "001", SISTEMAS, "Ricardo León Osorio",
                            EstadoInscripcion.CANCELADA, EstadoCalificacion.PENDIENTE,
                            null, LocalDate.of(2026, 8, 7))));

    @Operation(
            summary = "Matrícula del periodo vigente con el estado y la nota de cada materia",
            description = """
                    Devuelve las inscripciones del periodo académico vigente, con las canceladas
                    incluidas y sin parámetros de filtro. Las inscritas actualmente son las que
                    tienen estadoInscripcion en INSCRITA (S-14).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = """
                    Matrícula del periodo. Un estudiante matriculado que todavía no inscribe
                    materias responde 200 con la lista vacía.

                    Datos quemados: solo A00123456 y A00987654 devuelven contenido."""),
            @ApiResponse(responseCode = "400",
                    description = "El código institucional no cumple el formato A00NNNNNN (S-11)",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Sin credencial válida",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403",
                    description = "Autenticado sin autorización sobre ese estudiante",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = """
                    El código no corresponde a ningún estudiante. Un estudiante que existe y no
                    ha inscrito materias responde 200 con la lista vacía, no 404.""",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Fallo no previsto del servicio",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{estudianteId}/matricula-actual")
    public MatriculaActualResponse matriculaActual(
            @Parameter(description = "Código institucional del estudiante (S-11)",
                    example = "A00123456")
            @PathVariable @Pattern(regexp = PATRON_CODIGO,
                    message = "el código institucional debe tener el formato A00NNNNNN")
            String estudianteId) {

        List<Materia> materias = MATERIAS_POR_ESTUDIANTE.get(estudianteId);
        if (materias == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un estudiante con código " + estudianteId);
        }
        return new MatriculaActualResponse(
                estudianteId, PERIODO_VIGENTE, ULTIMA_SINCRONIZACION, materias);
    }
}
