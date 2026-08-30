package co.edu.icesi.vista360.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El reloj es una dependencia y no una llamada estatica.
 */
@Configuration
public class RelojConfig {

    @Bean
    Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
