package co.edu.icesi.vista360.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticacion por token de la plataforma de identidad y autorizacion por estudiante.
 */
@Configuration
public class SeguridadConfig {

    /** Rutas del contrato de un estudiante concreto. */
    private static final String RUTA_ESTUDIANTE = "/api/v1/estudiantes/{estudianteId}/**";

    private static final String[] RUTAS_PUBLICAS = {
            "/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    SecurityFilterChain cadenaFiltros(HttpSecurity http, AutorizacionEstudiante autorizacion,
            RespuestasDeSeguridad respuestas) throws Exception {
        return http
                // Sin sesion ni formulario, asi que no hay cookie que un tercero pueda montar.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers(RUTA_ESTUDIANTE).access(autorizacion)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                }).authenticationEntryPoint(respuestas))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas))
                .build();
    }
}
