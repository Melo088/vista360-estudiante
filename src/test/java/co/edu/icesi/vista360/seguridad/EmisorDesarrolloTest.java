package co.edu.icesi.vista360.seguridad;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * El emisor de tokens solo existe bajo el perfil de desarrollo.
 */
class EmisorDesarrolloTest {

    private static final String RUTA_TOKEN = "/dev/token";

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("dev")
    class BajoDesarrollo {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void emiteUnTokenUsableParaProbarElServicio() throws Exception {
            mockMvc.perform(get(RUTA_TOKEN)
                            .param("sujeto", "A00123456").param("rol", "ESTUDIANTE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.expiraEn").isNotEmpty());
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("produccion")
    class FueraDeDesarrollo {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ApplicationContext contexto;

        @Test
        void noExisteElControladorQueEmiteTokens() {
            assertFalse(contexto.getBeanNamesForType(TokenDesarrolloController.class).length > 0,
                    "el emisor de tokens quedo registrado fuera del perfil de desarrollo");
        }

        @Test
        void noExisteLaLlaveQueFirmaTokens() {
            assertFalse(contexto.getBeanNamesForType(JwtEncoder.class).length > 0,
                    "la capacidad de firmar tokens quedo disponible fuera de desarrollo");
        }

        @Test
        void laRutaNoEntregaNingunToken() throws Exception {
            int estado = mockMvc.perform(get(RUTA_TOKEN)
                            .param("sujeto", "A00123456").param("rol", "ESTUDIANTE"))
                    .andReturn().getResponse().getStatus();

            assertNotEquals(200, estado, "la ruta del emisor respondio fuera de desarrollo");
        }

        /** La consola H2 tampoco queda abierta, porque comparte la cadena de desarrollo. */
        @Test
        void laConsolaDeBaseDeDatosNoQuedaAbierta() throws Exception {
            mockMvc.perform(get("/h2-console"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
