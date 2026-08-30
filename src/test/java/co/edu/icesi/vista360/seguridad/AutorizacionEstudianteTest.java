package co.edu.icesi.vista360.seguridad;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Quien puede consultar a quien, con la regla registrada una sola vez en la cadena. */
@SpringBootTest
@AutoConfigureMockMvc
class AutorizacionEstudianteTest {

    private static final String RUTA = "/api/v1/estudiantes/{estudianteId}/matricula-actual";

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor comoEstudiante(String sujeto) {
        return jwt().jwt(token -> token.subject(sujeto).claim("rol", "ESTUDIANTE"));
    }

    private static RequestPostProcessor comoAcompanante(String sujeto) {
        return jwt().jwt(token -> token.subject(sujeto).claim("rol", "ACOMPANAMIENTO"));
    }

    @Test
    void elEstudianteVeSuPropiaInformacion() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value("A00123456"));
    }

    @Test
    void elEstudianteNoVeLaDeOtro() throws Exception {
        mockMvc.perform(get(RUTA, "A00987654").with(comoEstudiante("A00123456")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    /** El 403 llega incluso cuando el estudiante consultado no existe, y no un 404. */
    @Test
    void laAutorizacionSeResuelveAntesDeMirarSiElEstudianteExiste() throws Exception {
        mockMvc.perform(get(RUTA, "A00999999").with(comoEstudiante("A00123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenNoSePasa() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void elAcompananteConAsignacionVigenteVeAlEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoAcompanante("ana.perez")))
                .andExpect(status().isOk());
    }

    /** La asignacion de luis.gomez sobre A00123456 cerro en mayo (S-05). */
    @Test
    void elAcompananteConAsignacionVencidaYaNoVeAlEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoAcompanante("luis.gomez")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAcompananteSinAsignacionNoVeAlEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoAcompanante("sofia.rojas")))
                .andExpect(status().isForbidden());
    }

    /** La regla mira el par acompanante-estudiante y no alcanza con tener el rol. */
    @Test
    void elAcompananteSoloVeALosQueTieneAsignados() throws Exception {
        mockMvc.perform(get(RUTA, "A00555000").with(comoAcompanante("ana.perez")))
                .andExpect(status().isForbidden());
    }

    /** Un codigo mal formado ajeno se rechaza por permisos antes de mirar su forma. */
    @Test
    void laAutorizacionSeResuelveAntesDeValidarElFormato() throws Exception {
        mockMvc.perform(get(RUTA, "XYZ").with(comoEstudiante("A00123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unRolDesconocidoNoAbreNada() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456")
                        .with(jwt().jwt(t -> t.subject("A00123456").claim("rol", "RECTOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unTokenSinRolNoAbreNada() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(jwt().jwt(t -> t.subject("A00123456"))))
                .andExpect(status().isForbidden());
    }
}
