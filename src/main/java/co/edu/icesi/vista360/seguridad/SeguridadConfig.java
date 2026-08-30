package co.edu.icesi.vista360.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad PROVISIONAL del slice vertical
 */
@Configuration
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(peticiones -> peticiones.anyRequest().permitAll())
                .build();
    }
}
