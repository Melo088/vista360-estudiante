package co.edu.icesi.vista360.matricula;

import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse;
import co.edu.icesi.vista360.matricula.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementa el contrato de api/openapi.yaml.
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Matricula", description = "Consulta de la matricula del periodo vigente")
public class MatriculaController {

    /** Formato del codigo institucional, S-11. */
    private static final String PATRON_CODIGO = "^A00\\d{6}$";

    private final MatriculaService servicio;

    MatriculaController(MatriculaService servicio) {
        this.servicio = servicio;
    }

    @Operation(
            summary = "Matrícula del periodo vigente con el estado y la nota de cada materia",
            description = """
                    Devuelve las inscripciones del periodo vigente, con las canceladas incluidas y
                    sin filtros. Las inscritas actualmente son las que tienen estadoInscripcion en
                    INSCRITA (S-14). Vigente es el periodo que contiene hoy, o el último (S-17).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = """
                    Matrícula del periodo. Un estudiante matriculado que todavía no inscribe
                    materias responde 200 con la lista vacía."""),
            @ApiResponse(responseCode = "400", description = """
                    El código institucional no cumple el formato A00NNNNNN (S-11). La
                    autorización se resuelve antes, así que un código mal formado sobre el que
                    no hay permiso responde 403.""",
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
                    El código no corresponde a ningún estudiante, o el estudiante no tiene ninguna
                    matrícula registrada. El detail distingue los dos casos.""",
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

        return servicio.consultarMatriculaActual(estudianteId);
    }
}
