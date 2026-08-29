package co.edu.icesi.vista360.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI apiVista360() {
        return new OpenAPI().info(new Info()
                .title("Vista 360 del Estudiante")
                .version("v1")
                .description("""
                        Capa propia de la Universidad sobre el ecosistema existente.

                        este documento describe un slice vertical con datos
                        quemados en el controlador. El contrato es provisional y se define
                        cuando se sepa que puede entregar el ERP."""));
    }
}
