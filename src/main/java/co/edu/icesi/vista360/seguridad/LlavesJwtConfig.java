package co.edu.icesi.vista360.seguridad;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Par de llaves con el que se valida la firma de los tokens en local.
 */
@Configuration
public class LlavesJwtConfig {

    @Bean
    KeyPair parDeLlaves() {
        try {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(2048);
            return generador.generateKeyPair();
        } catch (NoSuchAlgorithmException imposible) {
            throw new IllegalStateException("la JVM no ofrece RSA", imposible);
        }
    }

    @Bean
    JwtDecoder decodificador(KeyPair par) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) par.getPublic()).build();
    }
}
