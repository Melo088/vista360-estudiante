package co.edu.icesi.vista360.matricula;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de contrato del endpoint. Fijan la forma de la respuesta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MatriculaControllerTest {

    private static final String RUTA = "/api/v1/estudiantes/{estudianteId}/matricula-actual";

    /** La inscripcion cancelada trae nota parcial y fecha: el estudiante cancelo perdiendo. */
    private static final String CANCELADA = "$.materias[?(@.asignatura.codigo=='09794')]";

    @Autowired
    private MockMvc mockMvc;

    /** Token del propio estudiante, que es el caso permitido de la regla (S-18). */
    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor comoEstudiante(
            String estudianteId) {
        return jwt().jwt(token -> token.subject(estudianteId).claim("rol", "ESTUDIANTE"));
    }

    @Test
    void devuelveLaMatriculaDelPeriodoConLaCanceladaIncluida() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value("A00123456"))
                .andExpect(jsonPath("$.periodo.codigo").value("202620"))
                .andExpect(jsonPath("$.actualizadoEn").value("2026-08-30T04:15:00Z"))
                .andExpect(jsonPath("$.materias", hasSize(5)))
                .andExpect(jsonPath(CANCELADA + ".estadoInscripcion").value("CANCELADA"))
                .andExpect(jsonPath(CANCELADA + ".estadoCalificacion").value("PARCIAL"))
                .andExpect(jsonPath(CANCELADA + ".nota").value(2.1))
                .andExpect(jsonPath(CANCELADA + ".fechaCancelacion").value("2026-08-21"))
                .andExpect(jsonPath(CANCELADA + ".nrc").value("11384"))
                .andExpect(jsonPath(CANCELADA + ".grupo").value("001"));
    }

    /** PENDIENTE y nota nula van juntos. */
    @Test
    void laMateriaSinCalificarNoTraeNota() throws Exception {
        String plataformas = "$.materias[?(@.asignatura.codigo=='09791')]";

        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath(plataformas + ".estadoCalificacion").value("PENDIENTE"))
                .andExpect(jsonPath(plataformas + ".nota").value(contains(nullValue())))
                .andExpect(jsonPath(plataformas + ".fechaCancelacion").value(contains(nullValue())));
    }

    /** El programa cuelga de la inscripcion: la doble titulacion cabe en un solo listado. */
    @Test
    void consolidaLosDosProgramasEnUnSoloListado() throws Exception {
        mockMvc.perform(get(RUTA, "A00987654").with(comoEstudiante("A00987654")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias[*].programa.codigo")
                        .value(containsInAnyOrder("TEL", "TEL", "TEL", "SIS", "SIS")));
    }

    /** Escala APROBACION: viene resultado y nunca nota, sin colapsar los dos ejes (S-16). */
    @Test
    void laMateriaDeEscalaDeAprobacionTraeResultadoYNoNota() throws Exception {
        String pdp = "$.materias[?(@.asignatura.codigo=='00101')]";

        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath(pdp + ".escalaCalificacion").value("APROBACION"))
                .andExpect(jsonPath(pdp + ".estadoCalificacion").value("DEFINITIVA"))
                .andExpect(jsonPath(pdp + ".resultado").value("APROBADA"))
                .andExpect(jsonPath(pdp + ".nota").value(contains(nullValue())));
    }

    /** El otro valor de la escala, para que el contrato no parezca de un solo resultado. */
    @Test
    void laMateriaDeAprobacionPuedeVenirReprobada() throws Exception {
        String ingles = "$.materias[?(@.asignatura.codigo=='07313')]";

        mockMvc.perform(get(RUTA, "A00987654").with(comoEstudiante("A00987654")))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ingles + ".escalaCalificacion").value("APROBACION"))
                .andExpect(jsonPath(ingles + ".resultado").value("REPROBADA"));
    }

    /** La doble titulacion se declara en la matricula, no se deriva de las inscripciones. */
    @Test
    void declaraLosProgramasDeLaMatriculaConSuPrincipal() throws Exception {
        mockMvc.perform(get(RUTA, "A00987654").with(comoEstudiante("A00987654")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.programas", hasSize(2)))
                .andExpect(jsonPath("$.programas[?(@.principal==true)].codigo")
                        .value(contains("TEL")))
                .andExpect(jsonPath("$.programas[?(@.principal==false)].codigo")
                        .value(contains("SIS")));
    }

    /** Matriculado y sin inscribir: 200 con lista vacia, nunca 404 (S-14). */
    @Test
    void elMatriculadoQueNoInscribioRecibeListaVacia() throws Exception {
        mockMvc.perform(get(RUTA, "A00555000").with(comoEstudiante("A00555000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias", hasSize(0)))
                .andExpect(jsonPath("$.programas", hasSize(1)))
                .andExpect(jsonPath("$.periodo.codigo").value("202620"));
    }

    /**
     * El estudiante existe y no tiene ninguna matricula. Es el unico caso que alcanza este
     * 404: con el respaldo del S-17, a quien solo le falta la del periodo vigente se le
     * devuelve el ultimo periodo cursado.
     */
    @Test
    void elEstudianteSinNingunaMatriculaRecibeUn404Distinguible() throws Exception {
        mockMvc.perform(get(RUTA, "A00777111").with(comoEstudiante("A00777111")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(
                        "El estudiante A00777111 no tiene ninguna matrícula registrada"));
    }

    /** El otro 404, con detalle propio para que el consumidor los separe. */
    @Test
    void elCodigoInexistenteRecibeElOtro404() throws Exception {
        mockMvc.perform(get(RUTA, "A00999999").with(comoEstudiante("A00999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(
                        "No existe un estudiante con código A00999999"));
    }

    /** El sujeto coincide con el codigo para pasar la autorizacion y llegar a la validacion. */
    @Test
    void rechazaElCodigoQueNoCumpleElFormato() throws Exception {
        mockMvc.perform(get(RUTA, "XYZ").with(comoEstudiante("XYZ")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void respondeNotFoundCuandoElCodigoNoCorrespondeAUnEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "A00999999").with(comoEstudiante("A00999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
