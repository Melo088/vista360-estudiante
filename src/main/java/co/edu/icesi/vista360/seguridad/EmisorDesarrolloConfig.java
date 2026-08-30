package co.edu.icesi.vista360.seguridad;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Emisor de tokens para probar en local, apagado fuera del perfil de desarrollo.
 */
@Configuration
@Profile("dev")
public class EmisorDesarrolloConfig {

    @Bean
    JwtEncoder codificador(KeyPair par) {
        RSAKey llave = new RSAKey.Builder((RSAPublicKey) par.getPublic())
                .privateKey((RSAPrivateKey) par.getPrivate())
                .keyID("vista360-dev")
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(llave)));
    }

    /**
     * Cadena aparte para las rutas de desarrollo, con su propio matcher.
     */
    @Bean
    @Order(1)
    SecurityFilterChain cadenaDesarrollo(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/dev/**", "/h2-console/**")
                .csrf(AbstractHttpConfigurer::disable)
                .headers(cabeceras -> cabeceras.frameOptions(marcos -> marcos.sameOrigin()))
                .authorizeHttpRequests(peticiones -> peticiones.anyRequest().permitAll())
                .build();
    }
}
